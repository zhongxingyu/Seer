 package com.asu.edu;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 
 @Controller
 public class ErrorPageController {
 	
 	
 	@RequestMapping(value = "/error-page", method = RequestMethod.GET)
 	public String error(HttpServletRequest request,
 			@RequestParam("error") String error){
 		request.setAttribute("error", error);
 		return "error-page";
 	}
	
	@RequestMapping(value = "/error-code", method = RequestMethod.GET)
	public String errorCode(HttpServletRequest request){
		String error = request.getParameter("c");
		String msg="";
		if(error.equals("403"))
			msg = "Access Denied";
		if(error.equals("404"))
			msg = "Requested Resource not found";
		if(error.equals("400"))
			msg = "Bad request";
		if(error.equals("408"))
			msg = "Request Timed out";
		request.setAttribute("error", msg);
		return "error-code";
	}

 
 }
