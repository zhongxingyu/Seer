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
 
 }
