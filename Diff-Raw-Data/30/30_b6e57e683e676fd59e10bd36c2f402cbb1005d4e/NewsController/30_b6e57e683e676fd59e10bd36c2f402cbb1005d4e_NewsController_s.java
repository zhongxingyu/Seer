 package com.xlthotel.core.admin.controller;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.xlthotel.core.admin.service.NewsService;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.Order;
 import com.xlthotel.foundation.common.Page;
 import com.xlthotel.foundation.common.SimpleConditionImpl;
 import com.xlthotel.foundation.common.SimpleOrderImpl;
 import com.xlthotel.foundation.common.SimplePageImpl;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.orm.entity.News;
 
 @Controller
 public class NewsController {
 	@Autowired
 	private NewsService newsService;
 	
 	@RequestMapping(method=RequestMethod.GET, value="/admin/newslist.do")
 	public ModelAndView getNewsList() {
 		Page page = new SimplePageImpl();
 		page.setIndex(0);
 		page.setCount(25);
 		
 		Order order = new SimpleOrderImpl();
 		order.setOrderColumn("createDate");
 		order.setSequence(Order.Sequence.DESC.toString());
 		
 		List<News> newslist = newsService.getNewsByConditions(page, null, order);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("newslist", newslist);
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		return new ModelAndView("/news/newslist", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.POST, value="/admin/newslist.do")
 	public ModelAndView getNewsList(HttpServletRequest request, HttpServletResponse response) {
 		Page page = getPageFromRequest(request);
 		Order order = getOrderFromRequest(request);
 		String minDateStr = (String) SimpleServletRequestUtils.getStringParameter(request, "minDate", "");
 		String maxDateStr = (String) SimpleServletRequestUtils.getStringParameter(request, "maxDate", "");
 		String title = (String) SimpleServletRequestUtils.getStringParameter(request, "searchTitle", "");
 		Date minDate = null;
 		Date maxDate = null;
 		Condition condition = new SimpleConditionImpl();
 		if (StringUtils.isNotBlank(minDateStr) && StringUtils.isNotBlank(maxDateStr)) {
 			minDate = DateUtils.parse("yyyy-MM-dd", minDateStr);
 			maxDate = DateUtils.parse("yyyy-MM-dd", maxDateStr);
 			condition.putCondition("createDate", "between", "date", minDate, maxDate);
 		}
 		condition.setEntityName("News");
 		if (StringUtils.isNotBlank(title)) {
 			condition.putCondition("title", "like", "title", title);
 		}
 		List<News> newslist = newsService.getNewsByConditions(page, condition, order);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("newslist", newslist);
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("title", title);
 		returnModel.put("minDate", minDateStr);
 		returnModel.put("maxDate", maxDateStr);
 		return new ModelAndView("/news/newslist", returnModel);
 	}
 	
 	private Page getPageFromRequest(HttpServletRequest request) {
 		Page page = new SimplePageImpl();
		page.setIndex(SimpleServletRequestUtils.getIntParameter(request, "page.index", 0));
		page.setCount(SimpleServletRequestUtils.getIntParameter(request, "page.count", 20));
 		return page;
 	}
 	
 	private Order getOrderFromRequest(HttpServletRequest request) {
 		Order order = new SimpleOrderImpl();
		order.setOrderColumn(SimpleServletRequestUtils.getStringParameter(request, "order.orderColumn", "createDate"));
		order.setSequence(SimpleServletRequestUtils.getStringParameter(request, "order.sequence", "desc"));
 		return order;
 	}
 	
 	//test
 	// TODO delete it!
 	@RequestMapping(method=RequestMethod.GET, value="/admin/addNews.do")
 	public ModelAndView addToNewsList(HttpServletRequest request, HttpServletResponse response) {
 		newsService.test();
 		try {
 			response.sendRedirect("newslist.do");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
