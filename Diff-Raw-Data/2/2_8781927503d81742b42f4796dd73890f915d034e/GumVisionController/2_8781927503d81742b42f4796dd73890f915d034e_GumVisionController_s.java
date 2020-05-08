 package com.gumvision.web.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: markkelly
  * Date: 29/08/2011
  * Time: 11:58
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class GumVisionController {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GumVisionController.class);
 
     @RequestMapping(value="/", method = RequestMethod.GET)
    public ModelAndView displayGumVision(HttpServletRequest request, HttpServletResponse response) {
 
         LOGGER.info("In the Gumvision Main Controller");
         Map<String, Object> model = new HashMap<String, Object>();
 
         return new ModelAndView("gumvision",model);
     }
 }
