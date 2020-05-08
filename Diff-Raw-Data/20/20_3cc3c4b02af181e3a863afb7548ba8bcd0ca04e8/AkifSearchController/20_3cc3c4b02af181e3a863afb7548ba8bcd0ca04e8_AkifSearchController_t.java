 package com.agroknow.search.web.controllers;
 
 import com.agroknow.domain.Akif;
 import com.agroknow.search.domain.AgroSearchRequest;
 import com.agroknow.search.domain.AgroSearchResponse;
 import com.agroknow.search.services.AkifSearchService;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Enumeration;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * @author aggelos
  */
 @Controller
 @RequestMapping("/v1/akif")
 public class AkifSearchController {
 
     @Autowired
     private AkifSearchService searchService;
 
     @RequestMapping(value = { "/", "" }, method = { RequestMethod.GET })
     public @ResponseBody AgroSearchResponse<Akif> root(HttpServletRequest request, HttpServletResponse response) throws IOException {
         AgroSearchRequest searchReq = buildSearchRequest(request);
         AgroSearchResponse<Akif> searchRes = searchService.search(searchReq);
         return searchRes;
     }
 
     @RequestMapping(value = "/{id}", method = { RequestMethod.GET })
     public @ResponseBody AgroSearchResponse<Akif> fetch(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
         AgroSearchResponse<Akif> searchRes = searchService.fetch(id.split(","));
         return searchRes;
     }
 
     private AgroSearchRequest buildSearchRequest(HttpServletRequest request) {
         AgroSearchRequest searchReq = new AgroSearchRequest();
         Enumeration<String> params = request.getParameterNames();
         String param, paramValue;
 
         while (params.hasMoreElements()) {
             param = params.nextElement();
             paramValue = request.getParameter(param);
 
             if("q".equalsIgnoreCase(param)) {
                 searchReq.addSearchFields("_all", StringUtils.trim(paramValue));
             } else if("sort_by".equalsIgnoreCase(param)) {
                 searchReq.setSortByField(paramValue);
             } else if("sort_order".equalsIgnoreCase(param) && "desc".equalsIgnoreCase(paramValue)) {
                 searchReq.setSortOrder("desc");
             } else if("facets".equalsIgnoreCase(param)) {
                 searchReq.setFacetFields(Arrays.asList(paramValue.split(",")));
             } else if("facet_size".equalsIgnoreCase(param)) {
                 searchReq.setFacetSize(Integer.parseInt(paramValue));
             } else if("page".equalsIgnoreCase(param)) {
                 searchReq.setPage(Integer.parseInt(paramValue));
             } else if("page_size".equalsIgnoreCase(param)) {
                 searchReq.setPageSize(Integer.parseInt(paramValue));
             } else {
                searchReq.addSearchFields(param, StringUtils.trim(paramValue));
             }
         }
 
         return searchReq;
     }
 
 }
