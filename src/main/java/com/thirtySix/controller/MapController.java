package com.thirtySix.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thirtySix.dto.AjaxRDTO;
import com.thirtySix.dto.SeatMapQDTO;
import com.thirtySix.model.Furnish;
import com.thirtySix.model.FurnishClass;
import com.thirtySix.model.SeatMap;
import com.thirtySix.service.MapService;
import com.thirtySix.util.ObjectConverter;
import com.thirtySix.webSocket.WebSocketUtil;

@Controller
@RequestMapping(value = { "/map" })
public class MapController {

	@Autowired
	private MapService mapService = null;

	@Autowired
	private ObjectConverter objConverter = null;

	@Autowired
	private WebSocketUtil webSocket = null;

	/**
	 * Get Furnish class for design map.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = { "/getFurnishClass" })
	public AjaxRDTO getFurnishClass(final HttpServletRequest request,
			final HttpServletResponse response) {
		final AjaxRDTO result = new AjaxRDTO();

		final Map<String, FurnishClass> furnishClass = this.mapService
				.findAllFurnishClass();

		result.setStatusOK();
		result.setData(furnishClass);

		return result;
	}

	/**
	 * 儲存座位表
	 * 
	 * @param request
	 * @param response
	 * @param seatMapDTO
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = { "/saveSeatMap" }, //
			consumes = "application/json", //
			produces = "application/json")
	@Secured("ROLE_ADMIN")
	public AjaxRDTO saveSeatMap(final HttpServletRequest request,
			final HttpServletResponse response,
			@RequestBody final List<SeatMapQDTO> mapList) {
		final AjaxRDTO result = new AjaxRDTO();

		mapList.forEach(map -> {
			SeatMap seatMap = new SeatMap();
			if (this.mapService.findAllSeatMap().containsKey(map.getMapID())) {
				seatMap = this.mapService.findAllSeatMap().get(map.getMapID());
			}

			seatMap.setMapID(map.getMapID());
			seatMap.setName(map.getName());
			seatMap.setWidth(map.getWidth());
			seatMap.setHeight(map.getHeight());
			/** Save map */
			this.mapService.saveSeatMap(seatMap);

			/** Save new furnish */
			final List<Furnish> newFurnishList = this.objConverter
					.furnishDTOtoPO(seatMap, map.getNewFurnishList());
			this.mapService.saveFurnish(newFurnishList);

			/** Remove furnish */
			this.mapService.deleteFurnish(map.getRemoveFurnishList());
		});

		/** Broadcase to client */
		this.webSocket.updateSeatMap(mapList);

		result.setStatusOK();
		return result;
	}

	/**
	 * 取得座位表
	 * 
	 * @param request
	 * @param response
	 * @param seatMapDTO
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = { "/getSeatMap" })
	public AjaxRDTO getSeatMap(final HttpServletRequest request,
			final HttpServletResponse response) {

		final AjaxRDTO result = new AjaxRDTO();

		final List<SeatMap> mapList = this.mapService.findAllSeatMap().values()
				.stream().collect(Collectors.toList());

		result.setStatusOK();
		result.setData(mapList);

		return result;
	}
}
