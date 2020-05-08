 package com.ultrashare.controller;
 
 import org.apache.log4j.Logger;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Resource;
 
 import com.ultrashare.component.facilities.Log;
 
 @Resource
 public class MainController {
 
 	private static Logger logger = Logger.getLogger(MainController.class);
 
	public MainController() {
 
 	}
 
 	@Get
 	public void about() {
 		logger.debug(Log.header("about"));
 		logger.debug(Log.footer("about"));
 	}
 
 	@Get
 	public void contact() {
 		logger.debug(Log.header("contact"));
 		logger.debug(Log.footer("contact"));
 	}
 }
