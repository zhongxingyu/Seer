 package com.directi.train.tweetapp.controllers;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elricl
  * Date: 23/7/12
  * Time: 1:24 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class GenericController {
     @RequestMapping("/")
    public String index() {
         return "index";
     }
 }
