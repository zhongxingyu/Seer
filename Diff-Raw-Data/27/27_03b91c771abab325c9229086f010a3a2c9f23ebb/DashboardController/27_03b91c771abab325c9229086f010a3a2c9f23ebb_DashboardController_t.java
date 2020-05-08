 package me.gotdo.vp3.web.controller;
 
import me.gotdo.vp3.web.model.VPUser;

 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
 
 @Controller
@SessionAttributes("user")
 public class DashboardController {
 	
 	@RequestMapping(value = "/v", method = RequestMethod.GET)
	public String ventureHome(@ModelAttribute("user") VPUser user) {
 		return "home-venture";
 	}
 	
	@RequestMapping(value = "/p", method = RequestMethod.GET)
	public String primerHome(@ModelAttribute("user") VPUser user) {
		return "home-primer";
	}
	
 	@RequestMapping(value = { "/v/inbox", "/p/inbox" }, method = RequestMethod.GET)
 	public String inbox() {
 		return "inbox";
 	}
 
 }
