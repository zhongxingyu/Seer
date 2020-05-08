 package com.cnnic.whois.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cnnic.whois.dao.query.QueryEngine;
 import com.cnnic.whois.dao.query.cache.CacheQueryExecutor;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 
 @Controller
 @RequestMapping("/cache")
 public class CacheController extends BaseController {
 	@Autowired
 	private CacheQueryExecutor cacheQueryExecutor;
 	@Autowired
 	private QueryEngine queryEngine;
 
 	@RequestMapping(value = "/init", method = RequestMethod.GET)
 	@ResponseBody
	public void init(HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		cacheQueryExecutor.initCache();
 		response.getOutputStream().write("init cache success.".getBytes());
 	}
 }
