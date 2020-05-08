 package com.tommy.demo.controller;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/ajax/menuBar")
 public class MenuBarController {
 
 	protected static Logger logger = Logger.getLogger("controller");
 
 	@RequestMapping(method = RequestMethod.POST)
 	public @ResponseBody String getMenuBar() throws IOException {
		return "Menu <br/> in controller <br/> data";
 				
 	}
 
 }
