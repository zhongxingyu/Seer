 package com.ultrashare.controller;
 
 import org.apache.log4j.Logger;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
 
 import com.ultrashare.component.facilities.Log;
import com.ultrashare.component.facilities.Validate;
 
 @Resource
 public class MainController {
 
 	private static Logger logger = Logger.getLogger(MainController.class);
 
	private Result result;
 
	public MainController(Result result) {
		this.result = result;

	}

	public void error(String throwable) {
		logger.debug(Log.header("error", Log.entry("throwable", throwable)));
		if (Validate.ifAnyObjectIsNull(throwable)) {
			result.redirectTo(UploadController.class).form();
		}
		logger.debug(Log.footer("error"));
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
