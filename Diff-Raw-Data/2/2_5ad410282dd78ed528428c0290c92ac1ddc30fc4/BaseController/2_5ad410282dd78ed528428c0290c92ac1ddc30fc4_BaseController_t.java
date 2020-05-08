 package com.cnnic.whois.controller;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.cnnic.whois.bean.DomainQueryParam;
 import com.cnnic.whois.bean.EntityQueryParam;
 import com.cnnic.whois.bean.IpQueryParam;
 import com.cnnic.whois.bean.PageBean;
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.service.QueryService;
 import com.cnnic.whois.util.WhoisUtil;
 import com.cnnic.whois.view.FormatType;
 import com.cnnic.whois.view.ViewResolver;
 
 public class BaseController {
 	@Autowired
 	protected ViewResolver viewResolver;
 
 	protected void setMaxRecordsForFuzzyQ(QueryParam queryParam) {
 		if (queryParam.getFormat().isJsonOrXmlFormat()) {
 			queryParam.getPage().setMaxRecords(
 					QueryService.MAX_SIZE_FUZZY_QUERY);
 		}
 	}
 
 	protected DomainQueryParam praseDomainQueryParams(HttpServletRequest request) {
 		FormatType formatType = getFormatType(request);
 		PageBean page = getPageParam(request);
 		return new DomainQueryParam(formatType, page);
 	}
 
 	protected EntityQueryParam praseEntityQueryParams(HttpServletRequest request) {
 		FormatType formatType = getFormatType(request);
 		PageBean page = getPageParam(request);
 		return new EntityQueryParam(formatType, page);
 	}
 
 	protected QueryParam praseQueryParams(HttpServletRequest request) {
 		FormatType formatType = getFormatType(request);
 		PageBean page = getPageParam(request);
 		return new QueryParam(formatType, page);
 	}
 
 	protected IpQueryParam praseIpQueryParams(HttpServletRequest request) {
 		FormatType formatType = getFormatType(request);
 		PageBean page = getPageParam(request);
 		return new IpQueryParam(formatType, page);
 	}
 
 	private PageBean getPageParam(HttpServletRequest request) {
 		Object currentPageObj = request.getParameter("currentPage");
 		PageBean page = new PageBean();
 		if (null != currentPageObj) {
 			page.setCurrentPage(Integer.valueOf(currentPageObj.toString()));
 		}
 		return page;
 	}
 
 	protected void renderResponse(HttpServletRequest request,
 			HttpServletResponse response, Map<String, Object> resultMap,
 			QueryParam queryParam) throws IOException, ServletException {
 		viewResolver.writeResponse(queryParam.getFormat(), request, response,
 				resultMap, 0);
 	}
 
 	protected void renderResponseError400(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException,
 			QueryException {
 		Map<String, Object> resultMap = WhoisUtil
 				.processError(WhoisUtil.COMMENDRRORCODE);
 		viewResolver.writeResponse(getFormatType(request), request, response,
 				resultMap, 0);
 	}
 
 	public static String getFormatCookie(HttpServletRequest request) {
 		Cookie[] cookies = request.getCookies();
 		String format = null;
 		if (cookies != null) {
 			for (Cookie cookie : cookies) {
 				if (cookie.getName().equals("Format")) {
 					return cookie.getValue();
 				}
 			}
 		}
 		return format;
 	}
 
 	public static FormatType getFormatType(HttpServletRequest request) {
 		String format = getFormatCookie(request);
 		if (StringUtils.isNotBlank(format)) {
 			return FormatType.getFormatType(format);
 		}
 		if(isWebBrowser(request)){
 			return FormatType.HTML;
 		}
 		String acceptHeader = request.getHeader("Accept");
		if (StringUtils.isNotBlank(acceptHeader) && acceptHeader.contains("html")) {
 			format = FormatType.HTML.getName();
 		}
 		if (StringUtils.isBlank(acceptHeader)) {
 			format = "application/json";
 		}
 		return FormatType.getFormatType(format);
 	}
 
 	public static boolean isWebBrowser(HttpServletRequest request) {
 		String userAgent = "";
 		try {
 			userAgent = request.getHeader("user-agent").toLowerCase();
 		} catch (Exception e) {
 			userAgent = "";
 		}
 		if (userAgent.contains("msie") || userAgent.contains("firefox")
 				|| userAgent.contains("chrome") || userAgent.contains("safiri")
 				|| userAgent.contains("opera")) {
 			return true;
 		}
 		return false;
 	}
 }
