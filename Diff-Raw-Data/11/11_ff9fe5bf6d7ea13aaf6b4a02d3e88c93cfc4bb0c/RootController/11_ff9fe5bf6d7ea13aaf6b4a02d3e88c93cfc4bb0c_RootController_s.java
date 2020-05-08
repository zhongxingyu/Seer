 package com.checkers.server.controllers;
 
 import com.checkers.server.beans.ExceptionMessage;
 import org.apache.log4j.Logger;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  *
  *
  * @author Pavel_Kuchin
  */
 
 @Controller
 @RequestMapping(value="/")
 public class RootController {
     static Logger log = Logger.getLogger(RootController.class.getName());
 
     @RequestMapping(value="", method = RequestMethod.GET)
     public String getIndex(){
         log.info("Index page");
 
         return "index";
     }
 
    @RequestMapping(value="/error/405", method = RequestMethod.GET, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.NOT_FOUND)
     public @ResponseBody
     ExceptionMessage get405(){
         log.info("Method Not Allowed");
 
         ExceptionMessage eMsg = new ExceptionMessage();
 
         eMsg.setCode(5L);
         eMsg.setMessage("Method Not Allowed");
         eMsg.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-5");
 
         return eMsg;
     }
 
    @RequestMapping(value="/error/404", method = RequestMethod.GET, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.NOT_FOUND)
     public @ResponseBody
     ExceptionMessage get404(){
         log.info("Page not found");
 
         ExceptionMessage eMsg = new ExceptionMessage();
 
         eMsg.setCode(1L);
         eMsg.setMessage("Page not found");
         eMsg.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-1");
 
         return eMsg;
     }
 
    @RequestMapping(value="/error/403", method = RequestMethod.GET, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.FORBIDDEN)
     public @ResponseBody
     ExceptionMessage get403(){
         log.info("Access is denied");
 
         ExceptionMessage eMsg = new ExceptionMessage();
 
         eMsg.setCode(2L);
         eMsg.setMessage("Access is denied");
         eMsg.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-2");
 
         return eMsg;
     }
 
    @RequestMapping(value="/error/401", method = RequestMethod.GET, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.UNAUTHORIZED)
     public @ResponseBody
     ExceptionMessage get401(){
         log.info("Full authentication is required");
 
         ExceptionMessage eMsg = new ExceptionMessage();
 
         eMsg.setCode(3L);
         eMsg.setMessage("Full authentication is required to access this resource");
         eMsg.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-3");
 
         return eMsg;
     }
 }
