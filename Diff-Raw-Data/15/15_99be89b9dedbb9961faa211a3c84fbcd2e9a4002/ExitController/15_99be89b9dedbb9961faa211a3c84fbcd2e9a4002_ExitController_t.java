 package com.sproutinc.shimmer.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 @Controller
 public class ExitController {
     private static final Logger LOGGER = LoggerFactory.getLogger(ExitController.class);
     @RequestMapping(value = "/exit/{exitID}")
     @ResponseBody
     public ModelAndView handleExit(@PathVariable String exitID, HttpServletResponse response) throws IOException {
         LOGGER.info("handleExit /exit/" + exitID);
         ModelAndView view = null;
         if (exitID.equals("DessertRecipes") || exitID.equals("FloorWaxingChallenge")) {
             view = new ModelAndView(exitID);
         }
         else {
             response.sendError(404);
         }
         return view;
     }
 }
