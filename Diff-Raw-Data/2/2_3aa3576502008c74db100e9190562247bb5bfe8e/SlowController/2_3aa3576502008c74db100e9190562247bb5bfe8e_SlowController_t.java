 package de.smava.slowmotion.server.controllers;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aakhmerov
  * Date: 10/15/13
  * Time: 12:46 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
@RequestMapping("/**")
 public class SlowController {
     private static final Logger logger = LoggerFactory.getLogger(SlowController.class);
 
     private static final long TIME = 10000;
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView printWelcome(HttpServletRequest request) throws InterruptedException {
         logger.info("serving [" + request.getRequestURL() + "]");
         ModelAndView result = new ModelAndView("status");
         Thread.sleep(TIME);
         result.addObject("status", "running");
         return result;
     }
 }
