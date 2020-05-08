 package me.gotdo.vp3.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class DashboardController {
 	
 	@RequestMapping(value = "/v", method = RequestMethod.GET)
	public String ventureHome() {
 		return "home-venture";
 	}
 	
 	@RequestMapping(value = { "/v/inbox", "/p/inbox" }, method = RequestMethod.GET)
 	public String inbox() {
 		return "inbox";
 	}
 
 }
