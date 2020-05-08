 package net.berinle.s3mvc.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class HelloWorldController {
 
 	//@RequestMapping(method=RequestMethod.GET, value="/welcome.do")
 	@RequestMapping(method=RequestMethod.GET, value="/welcome")
 	public String hello(){
 		System.out.println("doing welcome...");
 		return "welcome";
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/register/confirm")
 	public void confirm() {		
 		System.out.println("Serving confirmation page");
 	}
 }
