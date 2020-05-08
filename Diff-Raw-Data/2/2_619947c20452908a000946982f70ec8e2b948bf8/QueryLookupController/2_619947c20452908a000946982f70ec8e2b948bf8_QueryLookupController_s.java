 package com.mpower.controller;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.support.PagedListHolder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import com.mpower.domain.QueryLookup;
 import com.mpower.service.QueryLookupService;
 import com.mpower.service.SessionServiceImpl;
 
 public class QueryLookupController implements Controller {
 
 	/** Logger for this class and subclasses */
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private QueryLookupService queryLookupService;
 
 	public void setQueryLookupService(QueryLookupService queryLookupService) {
 		this.queryLookupService = queryLookupService;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		Map<String, String> queryParams = new HashMap<String, String>();
 		Enumeration<String> enu = request.getParameterNames();
 		while (enu.hasMoreElements()) {
 			String param = enu.nextElement();
 			String paramValue = StringUtils.trimToNull(request.getParameter(param));
			if (paramValue != null && !param.equalsIgnoreCase("fieldDef") && !param.equalsIgnoreCase("view")) {
 				queryParams.put(param, paramValue);
 			}
 		}
 
 		// List<String> displayColumns = new ArrayList<String>();
 		// displayColumns.add("lastName");
 		// displayColumns.add("firstName");
 		String fieldDef = StringUtils.trimToNull(request.getParameter("fieldDef"));
 		QueryLookup queryLookup = queryLookupService.readQueryLookup(SessionServiceImpl.lookupUserSiteName(), fieldDef);
 		List<Object> objects = queryLookupService.executeQueryLookup(SessionServiceImpl.lookupUserSiteName(), fieldDef,
 				queryParams);
 		ModelAndView mav = new ModelAndView("queryLookup");
 		mav.addObject("objects", objects);
 
 		PagedListHolder pagedListHolder = new PagedListHolder(objects);
 		pagedListHolder.setMaxLinkedPages(3);
 		pagedListHolder.setPageSize(50);
 		String page = request.getParameter("page");
 
 		Integer pg = 0;
 		if (!StringUtils.isBlank(page)) {
 			pg = Integer.valueOf(page);
 		}
 
 		pagedListHolder.setPage(pg);
 		mav.addObject("pagedListHolder", pagedListHolder);
 		mav.addObject("queryLookup", queryLookup);
 
 		// mav.addObject("displayColumns", displayColumns);
 		// mav.addObject("parameterMap", request.getParameterMap());
 		return mav;
 	}
 
 }
