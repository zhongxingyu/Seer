 package com.xlthotel.core.controller;
 
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.json.JSONObject;
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
 import com.xlthotel.core.service.PhotoService;
 import com.xlthotel.core.service.RegionService;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.Page;
 import com.xlthotel.foundation.common.PageOrder;
 import com.xlthotel.foundation.common.PageOrder.Sequence;
 import com.xlthotel.foundation.common.SimpleConditionImpl;
 import com.xlthotel.foundation.common.SimpleOrderImpl;
 import com.xlthotel.foundation.common.SimplePageImpl;
 import com.xlthotel.foundation.orm.entity.Activity;
 import com.xlthotel.foundation.orm.entity.Complaint;
 import com.xlthotel.foundation.orm.entity.Hotel;
 import com.xlthotel.foundation.orm.entity.HotelComment;
 import com.xlthotel.foundation.orm.entity.MediaItem;
 import com.xlthotel.foundation.orm.entity.News;
 import com.xlthotel.foundation.orm.entity.Region;
 import com.xlthotel.foundation.service.ActivityService;
 import com.xlthotel.foundation.service.ComplaintService;
 import com.xlthotel.foundation.service.HotelCommentService;
 import com.xlthotel.foundation.service.NewsService;
 
 @Controller
 public class PortalController {
 
 	@Autowired
 	private HotelService hotelService;
 
 	@Autowired
 	private RegionService regionService;
 
 	@Autowired
 	private PhotoService photoService;
 
 	@Autowired
 	private NewsService newsService;
 
 	@Autowired
 	private ActivityService activityService;
 	
 	@Autowired
 	private HotelCommentService hotelCommentService;
 	
 	@Autowired
 	private ComplaintService complaintService;
 
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/portal/index.do")
 	public ModelAndView getPortal(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@CookieValue(value = Constant.COOKIE_ORDER_DATE_KEY, required = false) Cookie cookie,
 			@RequestParam(value = "selectedRegionId", required = false, defaultValue = "-1") int regionId) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		retrieveOrderDateFromCookie(cookie, returnModel);
 		getRegionAndHotel(regionId, returnModel);
 		getNewsList(returnModel);
 		getActvtList(returnModel);
 		getHotelCommentList(returnModel);
 		getComplaintList(returnModel);
 		return new ModelAndView("/portal/index", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, value = "/servlet/portal/hotelsForRegion.do")
 	public void getHotelsForRegion(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "selectedRegionId") int regionId) {
 		List<Hotel> hotelList = hotelService.findHotelByRegion(regionId);
 		try {
 			response.setContentType("text/plain");
 			response.setCharacterEncoding("UTF-8");
 			Writer writer = response.getWriter();
 			JSONWriter jsonWriter = new JSONWriter(writer).array();
 			for (Hotel h : hotelList) {
 				Map<String, String> map = new HashMap<String, String>();
 				map.put("id", h.getId());
 				map.put("name", h.getName());
 				jsonWriter.value(new JSONObject(map));
 			}
 			jsonWriter.endArray();
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/portal/hotelsForFooter.do")
 	public void getHotelsForFooter(HttpServletResponse response) {
 		List<Hotel> hotelList = hotelService.findHotelsForFooter();
 		try {
 			response.setContentType("text/plain");
 			response.setCharacterEncoding("UTF-8");
 			Writer writer = response.getWriter();
 			JSONWriter jsonWriter = new JSONWriter(writer).array();
 			for (Hotel h : hotelList) {
 				Map<String, String> map = new HashMap<String, String>();
 				map.put("hotelId", h.getId());
 				map.put("hotelName", h.getName());
 				jsonWriter.value(new JSONObject(map));
 			}
 			jsonWriter.endArray();
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void getRegionAndHotel(int regionId, Map<String, Object> returnModel) {
 		List<Region> regionList = hotelService.getHotelRegions();
 		int selectedRegionId = regionId >= 0 ? regionId : regionList.get(0)
 				.getId();
 		Region region = regionService.find(selectedRegionId);
 		returnModel.put("regionList", regionList);
 		returnModel.put("selectedRegionId", selectedRegionId);
 		returnModel.put("selectedRegion", region);
 		List<Hotel> hotelList = hotelService
 				.findHotelByRegion(selectedRegionId);
 		List<List<Hotel>> hotelListPartition = Lists.partition(hotelList, 6);
 		returnModel.put("hotelList", hotelListPartition);
 		for (Hotel h : hotelList) {
 			setupHotelMediaItems(h);
 		}
 	}
 
 	private void getNewsList(Map<String, Object> returnModel) {
 		Condition cdt = new SimpleConditionImpl();
 		cdt.setEntityName("News");
 		cdt.putCondition("status", "=", "status", News.Status.Enable.getValue());
 		Page page = new SimplePageImpl();
 		page.setCount(12);
 		page.setIndex(0);
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("createDate");
 		order.setSequence(Sequence.DESC.toString());
 		List<News> newsList = newsService.getNewsByConditions(page, cdt, order);
 		returnModel.put("newsList", newsList);
 	}
 	private void getHotelCommentList(Map<String, Object> returnModel) {
 		Condition cdt = new SimpleConditionImpl();
 		cdt.setEntityName("HotelComment");
 		cdt.putCondition("status", "=", "status", 1);
 		Page page = new SimplePageImpl();
 		page.setCount(5);
 		page.setIndex(0);
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("createDate");
 		order.setSequence(Sequence.DESC.toString());
 		List<HotelComment> hotelCommentList = hotelCommentService.getHotelCommentByConditions(page, cdt, order);
 		returnModel.put("hotelCommentList", hotelCommentList);
 	}
 	
 	private void getComplaintList(Map<String, Object> returnModel) {
 		Condition cdt = new SimpleConditionImpl();
 		cdt.setEntityName("Complaint");
 		cdt.putCondition("status", "=", "status", 1);
 		Page page = new SimplePageImpl();
 		page.setCount(5);
 		page.setIndex(0);
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("createDate");
 		order.setSequence(Sequence.DESC.toString());
 		List<Complaint> complaintList = complaintService.getComplaintByConditions(page, cdt, order);
 		returnModel.put("complaintList", complaintList);
 	}
 
 	private void getActvtList(Map<String, Object> returnModel) {
 		Condition cdt = new SimpleConditionImpl();
 		cdt.setEntityName("Activity");
 		cdt.putCondition("status", "=", "status", Activity.Status.Enable.getValue());
 		Page page = new SimplePageImpl();
 		page.setCount(12);
 		page.setIndex(0);
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("createDate");
 		order.setSequence(Sequence.DESC.toString());
 		List<Activity> activityList = activityService.getActivityByConditions(
 				page, cdt, order);
 		returnModel.put("activityList", activityList);
 	}
 
 	private void setupHotelMediaItems(Hotel hotel) {
 		String photoRawString = hotel.getPhotos();
 		if (StringUtils.isBlank(photoRawString)) {
 			return;
 		}
 		String[] photoIds = photoRawString.split(",");
 		List<MediaItem> mediaItems = new ArrayList<MediaItem>();
 		for (String photoId : photoIds) {
 			MediaItem m = photoService.getMediaItemModel(photoId);
 			mediaItems.add(m);
 		}
 		hotel.setMediaItems(mediaItems);
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
 }
