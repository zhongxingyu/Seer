 package org.samples.multitenant.control;
 
 import org.samples.multitenant.model.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping("/log")
 class LoggerController {
 
 	@Autowired
 	Logger logger;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ResponseEntity<String> log(
 			@RequestParam(value = "msg", required = true) String message) {
 		logger.log(message);
		return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
 	}
 }
