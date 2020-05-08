 package com.xlthotel.core.controller;
 
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.json.JSONWriter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.collect.Lists;
 import com.xlthotel.core.service.HotelService;
 import com.xlthotel.core.service.MailService;
 import com.xlthotel.core.service.OrderService;
 import com.xlthotel.core.service.PhotoService;
 import com.xlthotel.core.service.RegionService;
 import com.xlthotel.core.service.RoomService;
 import com.xlthotel.core.service.RoomTypeExtensionService;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.common.SystemPropertyConfig;
 import com.xlthotel.foundation.orm.entity.Hotel;
 import com.xlthotel.foundation.orm.entity.MediaItem;
 import com.xlthotel.foundation.orm.entity.Order;
 import com.xlthotel.foundation.orm.entity.Region;
 import com.xlthotel.foundation.orm.entity.RoomTypeExtension;
 import com.xlthotel.foundation.orm.entity.UserInfo;
 import com.xlthotel.foundation.service.UserInfoService;
 
 @Controller
 public class BookingListController {
 	
 	@Autowired
 	private HotelService hotelService;
 	
 	@Autowired
 	private RoomTypeExtensionService roomTypeExtensionService;
 	
 	@Autowired
 	private RoomService roomService;
 	
 	@Autowired
 	private RegionService regionService;
 	
 	@Autowired
 	private PhotoService photoService;
 	
 	@Autowired
 	private OrderService orderService;
 	
 	@Autowired
 	private UserInfoService userInfoService;
 	
 	@Autowired
 	private MailService mailService;
 	
 	@Autowired
 	private SystemPropertyConfig systemPropertyConfig;
 	
 	@RequestMapping(method = RequestMethod.GET, value="/servlet/book/test.do")
 	public ModelAndView getHotelMap() {
 		return new ModelAndView("/test");
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/book/region.do")
 	public ModelAndView getRegionList(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@CookieValue(value = Constant.COOKIE_ORDER_DATE_KEY, required = false) Cookie cookie,
 			@RequestParam(value = "selectedRegionId", required = false, defaultValue = "-1") int regionId) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		retrieveOrderDateFromCookie(cookie, returnModel);
 		List<Region> regionList = hotelService.getHotelRegions();
 		int selectedRegionId = regionId >= 0 ? regionId : regionList.get(0).getId();
 		Region region = regionService.find(selectedRegionId);
 		returnModel.put("regionList", regionList);
 		List<Hotel> hotelList = hotelService.findHotelByRegion(selectedRegionId);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("selectedRegionId", selectedRegionId);
 		returnModel.put("selectedRegion", region);
 		Date minDate = DateUtils.parseOrderDate((String) returnModel.get("minDate"));
 		Date maxDate = DateUtils.parseOrderDate((String) returnModel.get("maxDate"));
 		if (minDate != null && maxDate != null) {
 			List<Date> dateList = DateUtils.splitDate(minDate, maxDate);
 			returnModel.put("totalDays", dateList.size() - 1);
 			setupCalendar(dateList, returnModel);
 			for (Hotel hotel : hotelList) {
 				List<RoomTypeExtension> roomTypeExtList = roomTypeExtensionService.getRoomTypeExtensionByHotel(hotel.getId());
 				hotel.setRoomTypeExts(roomTypeExtList);
 				for (RoomTypeExtension roomTypeExt: roomTypeExtList) {
 					setupRoomTypeExtCoverPhoto(roomTypeExt);
 					roomTypeExt.setAvailableRoomCount(roomService.getAvailableRoomCount(dateList.subList(0, dateList.size() - 1), hotel.getId(), roomTypeExt.getRoomType().getId()));
 				}
 				setupHotelCoverPhoto(hotel);
 			}
 		}
 		return new ModelAndView("/booking/regionList", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, value = "/servlet/book/region.do")
 	public ModelAndView postRegionList(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@CookieValue(value = Constant.COOKIE_ORDER_DATE_KEY, required = false) Cookie cookie,
 			@RequestParam(value = "selectedRegionId", required = false) int regionId) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		List<Region> regionList = hotelService.getHotelRegions();
 		int selectedRegionId = regionId >= 0 ? regionId : regionList.get(0).getId();
 		Region region = regionService.find(selectedRegionId);
 		returnModel.put("regionList", regionList);
 		List<Hotel> hotelList = hotelService.findHotelByRegion(selectedRegionId);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("selectedRegionId", selectedRegionId);
 		returnModel.put("selectedRegion", region);
 		String minDateStr = SimpleServletRequestUtils.getStringParameter(request, "minDate", "");
 		String maxDateStr = SimpleServletRequestUtils.getStringParameter(request, "maxDate", "");
 		Date minDate = DateUtils.parseOrderDate(minDateStr);
 		Date maxDate = DateUtils.parseOrderDate(maxDateStr);
 		if (minDate != null && maxDate != null) {
 			List<Date> dateList = DateUtils.splitDate(minDate, maxDate);
 			returnModel.put("totalDays", dateList.size() - 1);
 			setupCalendar(dateList, returnModel);
 			for (Hotel hotel : hotelList) {
 				List<RoomTypeExtension> roomTypeExtList = roomTypeExtensionService.getRoomTypeExtensionByHotel(hotel.getId());
 				hotel.setRoomTypeExts(roomTypeExtList);
 				for (RoomTypeExtension roomTypeExt: roomTypeExtList) {
 					setupRoomTypeExtCoverPhoto(roomTypeExt);
 					roomTypeExt.setAvailableRoomCount(roomService.getAvailableRoomCount(dateList.subList(0, dateList.size() - 1), hotel.getId(), roomTypeExt.getRoomType().getId()));
 				}
 				setupHotelCoverPhoto(hotel);
 			}
 		}
 		setOrderDateToCookie(cookie, response, minDate, maxDate, returnModel);
 		return new ModelAndView("/booking/regionList", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/book/hotel.do")
 	public ModelAndView getHotel(HttpServletRequest request,
 			HttpServletResponse response,
 			@CookieValue(value = Constant.COOKIE_ORDER_DATE_KEY, required = false) Cookie cookie,
 			@RequestParam(value = "hotelId") String hotelId) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		retrieveOrderDateFromCookie(cookie, returnModel);
 		List<Region> regionList = hotelService.getHotelRegions();
 		Hotel hotel = hotelService.find(hotelId);
 		returnModel.put("regionList", regionList);
 		returnModel.put("selectedRegionId", hotel.getRegion().getId());
 		returnModel.put("selectedRegion", hotel.getRegion());
 		returnModel.put("centerLng", systemPropertyConfig.getCityLng(hotel.getRegion().getNodeId()));
 		returnModel.put("centerLat", systemPropertyConfig.getCityLat(hotel.getRegion().getNodeId()));
 		List<Hotel> hotelList = hotelService.findHotelByRegion(hotel.getRegion().getId());
 		returnModel.put("hotelList", hotelList);
 		Date minDate = DateUtils.parseOrderDate((String) returnModel.get("minDate"));
 		Date maxDate = DateUtils.parseOrderDate((String) returnModel.get("maxDate"));
 		returnModel.put("hotel", hotel);
 		if (minDate != null && maxDate != null) {
 			List<Date> dateList = DateUtils.splitDate(minDate, maxDate);
 			returnModel.put("totalDays", dateList.size() - 1);
 			setupCalendar(dateList, returnModel);
 			List<RoomTypeExtension> roomTypeExtList = roomTypeExtensionService.getRoomTypeExtensionByHotel(hotel.getId());
 			hotel.setRoomTypeExts(roomTypeExtList);
 			for (RoomTypeExtension roomTypeExt: roomTypeExtList) {
 				setupRoomTypeExtCoverPhoto(roomTypeExt);
 				roomTypeExt.setAvailableRoomCount(roomService.getAvailableRoomCount(dateList.subList(0, dateList.size() - 1), hotel.getId(), roomTypeExt.getRoomType().getId()));
 			}
 		}
 		createPhotosModelView(hotel, returnModel);
 		return new ModelAndView("/booking/hotel", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, value = "/servlet/book/hotel.do")
 	public ModelAndView postHotel(HttpServletRequest request,
 			HttpServletResponse response,
 			@CookieValue(value = Constant.COOKIE_ORDER_DATE_KEY, required = false) Cookie cookie,
 			@RequestParam(value = "hotelId") String hotelId) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		List<Region> regionList = hotelService.getHotelRegions();
 		Hotel hotel = hotelService.find(hotelId);
 		returnModel.put("regionList", regionList);
 		returnModel.put("selectedRegionId", hotel.getRegion().getId());
 		returnModel.put("selectedRegion", hotel.getRegion());
 		List<Hotel> hotelList = hotelService.findHotelByRegion(hotel.getRegion().getId());
 		returnModel.put("hotelList", hotelList);
 		String minDateStr = SimpleServletRequestUtils.getStringParameter(request, "minDate", "");
 		String maxDateStr = SimpleServletRequestUtils.getStringParameter(request, "maxDate", "");
 		Date minDate = DateUtils.parseOrderDate(minDateStr);
 		Date maxDate = DateUtils.parseOrderDate(maxDateStr);
 		returnModel.put("hotel", hotel);
 		if (minDate != null && maxDate != null) {
 			List<Date> dateList = DateUtils.splitDate(minDate, maxDate);
 			returnModel.put("totalDays", dateList.size() - 1);
 			setupCalendar(dateList, returnModel);
 			List<RoomTypeExtension> roomTypeExtList = roomTypeExtensionService.getRoomTypeExtensionByHotel(hotel.getId());
 			hotel.setRoomTypeExts(roomTypeExtList);
 			for (RoomTypeExtension roomTypeExt: roomTypeExtList) {
 				setupRoomTypeExtCoverPhoto(roomTypeExt);
 				roomTypeExt.setAvailableRoomCount(roomService.getAvailableRoomCount(dateList.subList(0, dateList.size() - 1), hotel.getId(), roomTypeExt.getRoomType().getId()));
 			}
 		}
 		createPhotosModelView(hotel, returnModel);
 		setOrderDateToCookie(cookie, response, minDate, maxDate, returnModel);
 		return new ModelAndView("/booking/hotel", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, value = "/servlet/book/saveBooking.do")
 	public ModelAndView saveBooking(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@CookieValue(value = Constant.COOKIE_ORDER_DATE_KEY, required = false) Cookie cookie) {
 		int selectedRegionIdInRequest = SimpleServletRequestUtils.getIntParameter(request, "regionId", -1);
 		List<Region> regionList = hotelService.getHotelRegions();
 		Region selectedRegion = regionService.find(selectedRegionIdInRequest);
 		int selectedRegionId = selectedRegion != null ? selectedRegion.getId() : regionList.get(0).getId();
 		Order order = bindOrder(request);
 		boolean errorFlag = false;
 		if (order != null) {
 			orderService.save(order);
 			mailService.sendOrderConfirmEmail(order);
 		} else {
 			errorFlag = true;
 		}
 		
 		try {
 			response.setContentType("text/plain; charset=UTF-8");
 			response.setCharacterEncoding("UTF-8");
 			String redirectUrl = "/servlet/book/bookingResult.do?";
 			redirectUrl += String.format("selectedRegionId=%d", selectedRegionId);
 			redirectUrl += String.format("&errorFlag=%s", errorFlag);
 			redirectUrl += String.format("&hotelId=%s", order.getHotel().getId());
 			response.getWriter().write(redirectUrl);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/book/bookingResult.do")
 	public ModelAndView redirectBookingResult(
 			@RequestParam(value = "selectedRegionId") int selectedRegionId,
 			@RequestParam(value = "errorFlag") boolean errorFlag,
 			@RequestParam(value = "hotelId") String hotelId) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		List<Region> regionList = hotelService.getHotelRegions();
 		Region region = regionService.find(selectedRegionId);
 		if (region == null) {
 			region = regionList.get(0);
 		}
 		List<Hotel> hotelList = hotelService.findHotelByRegion(region.getId());
 		Hotel hotel = hotelService.find(hotelId);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("regionList", regionList);
 		returnModel.put("selectedRegion", region);
 		returnModel.put("selectedRegionId", region.getId());
 		returnModel.put("errorFlag", errorFlag);
 		returnModel.put("hotel", hotel);
 		return new ModelAndView("/booking/bookingResult", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/book/checkLogin.do")
 	public void checkLoginAjax(HttpServletRequest request, HttpServletResponse response) {
 		Object ui = request.getSession().getAttribute(Constant.USER_WEBSEESION_KEY);
 		boolean result = false;
 		if (ui != null && (ui instanceof UserInfo)) {
 			result = true;
 		}
 		try {
 			response.setContentType("text/plain; charset=UTF-8");
 			response.setCharacterEncoding("UTF-8");
 			PrintWriter writer = response.getWriter();
 			JSONWriter jsonWriter = new JSONWriter(writer);
 			jsonWriter.object().key("isLogin").value(result).endObject();
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 	
 	private Order bindOrder(HttpServletRequest request) {
 		UserInfo ui = (UserInfo) request.getSession().getAttribute(Constant.USER_WEBSEESION_KEY);
 		UserInfo uiDB = userInfoService.findUserInfoById(ui.getId());
 		if (uiDB == null) {
 			return null;
 		}
 		
 		String clientName = SimpleServletRequestUtils.getStringParameter(request, "clientName", "");
 		if (StringUtils.isBlank(clientName)) {
 			return null;
 		}
 		String roomTypeExtId = SimpleServletRequestUtils.getStringParameter(request, "roomTypeExtId", "");
 		RoomTypeExtension roomTypeExt = roomTypeExtensionService.find(roomTypeExtId);
 		if (roomTypeExt == null) {
 			return null;
 		}
 		int roomCount = SimpleServletRequestUtils.getIntParameter(request, "roomCount", 0);
 		if (roomCount <= 0) {
 			return null;
 		}
 		String clientMobile = SimpleServletRequestUtils.getStringParameter(request, "clientMobile", "");
 		if (StringUtils.isBlank(clientMobile)) {
 			return null;
 		}
 		String confirmWay = SimpleServletRequestUtils.getStringParameter(request, "confirmWay", "");
 		if (StringUtils.isBlank(confirmWay)) {
 			return null;
 		}
 		String comment = SimpleServletRequestUtils.getStringParameter(request, "comment", "");
 		String email = SimpleServletRequestUtils.getStringParameter(request, "email", "");
 		if (StringUtils.isBlank(email)) {
 			return null;
 		}
 		String minDateStr = SimpleServletRequestUtils.getStringParameter(request, "minDate", "");
 		String maxDateStr = SimpleServletRequestUtils.getStringParameter(request, "maxDate", "");
 		String arrivalMinDateStr = SimpleServletRequestUtils.getStringParameter(request, "arrivalMinDate", "");
 		String arrivalMaxDateStr = SimpleServletRequestUtils.getStringParameter(request, "arrivalMaxDate", "");
 		Date minDate = DateUtils.parseOrderDate(minDateStr);
 		Date maxDate = DateUtils.parseOrderDate(maxDateStr);
 		Date arrivalMinDate = DateUtils.parse("yyyy-MM-dd HH:mm:ss", arrivalMinDateStr);
 		Date arrivalMaxDate = DateUtils.parse("yyyy-MM-dd HH:mm:ss", arrivalMaxDateStr);
 		if (minDate == null || maxDate == null || arrivalMinDate == null || arrivalMaxDate == null) {
 			return null;
 		}
 		Order order = new Order();
 		order.setEmail(email);
 		order.setBookingClientName(clientName);
 		order.setConfirmWay(confirmWay);
 		order.setCreateDate(new Date());
 		order.setEarliestArrival(arrivalMinDate);
 		order.setLatestArrival(arrivalMaxDate);
 		order.setOrderDateFrom(minDate);
 		order.setOrderDateTo(maxDate);
 		order.setPhoneNumber(clientMobile);
 		order.setRoomCount(roomCount);
 		order.setStatus(Order.Status.Create.getValue());
 		order.setHotel(roomTypeExt.getHotel());
 		order.setRoomType(roomTypeExt.getRoomType());
 		order.setComment(comment);
 		List<Date> dateList = DateUtils.splitDate(minDate, maxDate);
 		BigDecimal days = new BigDecimal(dateList.size() - 1);
 		BigDecimal price = roomTypeExt.getOnlinePrice();
 		BigDecimal count = new BigDecimal(roomCount);
 		order.setTotalPrice(days.multiply(count).multiply(price));
 		order.setUser(uiDB);
 		return order;
 	}
 	
 	private void retrieveOrderDateFromCookie(Cookie cookie, Map<String, Object> returnModel) {
 		String orderDateString = null;
 		String minDateStr = null;
 		String maxDateStr = null;
 		if (cookie != null) {
 			orderDateString = cookie.getValue();
 			if (StringUtils.isNotBlank(orderDateString) && orderDateString.split("\\|").length == 2) {
 				String[] parts = orderDateString.split("\\|");
 				minDateStr = parts[0];
 				maxDateStr = parts[1];
 			}
 		}
 		returnModel.put("minDate", minDateStr);
 		returnModel.put("maxDate", maxDateStr);
 		returnModel.put("todayDate", DateUtils.formatDate("yyyy-MM-dd", new Date()));
 	}
 	
 	private void setOrderDateToCookie(Cookie cookie, HttpServletResponse response, Date minDate, Date maxDate, Map<String, Object> returnObject) {
 		String minDateStr = DateUtils.formatDate("yyyy-MM-dd", minDate);
 		String maxDateStr = DateUtils.formatDate("yyyy-MM-dd", maxDate);
 		returnObject.put("minDate", minDateStr);
 		returnObject.put("maxDate", maxDateStr);
 		if (cookie != null) {
 			cookie.setValue(minDateStr + "|" + maxDateStr);
 		} else {
 			Cookie newCookie = new Cookie(Constant.COOKIE_ORDER_DATE_KEY, minDateStr + "|" + maxDateStr);
 			newCookie.setMaxAge(-1);
 			response.addCookie(newCookie);
 		}
 		returnObject.put("todayDate", DateUtils.formatDate("yyyy-MM-dd", new Date()));
 	}
 	
 	private void setupHotelCoverPhoto(Hotel hotel) {
 		String photoRawString = hotel.getPhotos();
 		if (StringUtils.isBlank(photoRawString)) {
 			return;
 		}
 		String[] photoIds = photoRawString.split(",");
 		MediaItem cover = photoService.getMediaItemModel(photoIds[0]);
 		hotel.setHotelCoverPhoto(cover.getPath());
 	}
 	
 	private void setupRoomTypeExtCoverPhoto(RoomTypeExtension roomTypeExt) {
 		String photoRawString = roomTypeExt.getPhotos();
 		if (StringUtils.isBlank(photoRawString)) {
 			return;
 		}
 		String[] photoIds = photoRawString.split(",");
 		MediaItem cover = photoService.getMediaItemModel(photoIds[0]);
 		roomTypeExt.setRoomTypeExtCoverPhoto(cover.getPath());
 	}
 	
 	private void createPhotosModelView(Hotel hotel, Map<String, Object> modelView) {
 		String photoRawString = hotel.getPhotos();
 		if (StringUtils.isBlank(photoRawString)) {
 			return;
 		}
 		String[] photoIds = photoRawString.split(",");
 		List<MediaItem> photos = new ArrayList<MediaItem>();
 		for (int i = 0; i < Math.min(photoIds.length, 6); i++) {
 			MediaItem item = photoService.getMediaItemModel(photoIds[i]);
 			photos.add(item);
 		}
 		modelView.put("photos", photos);
 	}
 	
 	private void setupCalendar(List<Date> dateList, Map<String, Object> returnObject) {
 		List<Date> days = dateList.subList(0, dateList.size() - 1);
 		String[] title = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
 		String result = "";
 		String line1 = "";
 		String line2 = "";
 		Map<String, String> map = new HashMap<String, String>();
 		Calendar c = Calendar.getInstance();
 		for (Date date : days) {
 			c.setTime(date);
 			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
 			switch (dayOfWeek) {
 			case 1:
 				map.put("周日", DateUtils.formatDate("yyyy-MM-dd", date));
 				break;
 			case 2:
 				map.put("周一", DateUtils.formatDate("yyyy-MM-dd", date));
 				break;
 			case 3:
 				map.put("周二", DateUtils.formatDate("yyyy-MM-dd", date));
 				break;
 			case 4:
 				map.put("周三", DateUtils.formatDate("yyyy-MM-dd", date));
 				break;
 			case 5:
 				map.put("周四", DateUtils.formatDate("yyyy-MM-dd", date));
 				break;
 			case 6:
 				map.put("周五", DateUtils.formatDate("yyyy-MM-dd", date));
 				break;
 			case 7:
 				map.put("周六", DateUtils.formatDate("yyyy-MM-dd", date));
 				line1 = "<tr>";
 				line2 = "<tr>";
 				for (int i = 0; i < 7; i++) {
 					String value = map.get(title[i]);
 					if (value != null) {
 						line1 += "<th class='selected'>" + title[i] + "</th>";
 						line2 += "<td class='selected'>" + value + "</td>";
 					} else {
 						line1 += "<th>" + title[i] + "</th>";
 						line2 += "<td>&nbsp;</td>";
 					}
 				}
 				line1 += "</tr>";
 				line2 += "</tr>";
 				result += line1 + line2;
 				line1 = "";
 				line2 = "";
 				map = new HashMap<String, String>();
 				break;
 			default:
 				break;
 			}
 		}
 		if (!map.isEmpty()) {
 			line1 = "<tr>";
 			line2 = "<tr>";
 			for (int i = 0; i < 7; i++) {
 				String value = map.get(title[i]);
 				if (value != null) {
 					line1 += "<th class='selected'>" + title[i] + "</th>";
 					line2 += "<td class='selected'>" + value + "</td>";
 				} else {
 					line1 += "<th>" + title[i] + "</th>";
 					line2 += "<td>&nbsp;</td>";
 				}
 			}
 			line1 += "</tr>";
 			line2 += "</tr>";
 			result += line1 + line2;
 		}
 		System.out.println(result);
 		returnObject.put("calendarHtml", result);
 	}
 }
