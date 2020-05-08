 package com.forum.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.Map;
 
 @Controller
 public class QuestionPostingController {
     @RequestMapping(value = "/postQuestion", method = RequestMethod.GET)
     public ModelAndView postQuestion() {
         return new ModelAndView("postQuestion");
     }
 
     @RequestMapping(value = "/showPostedQuestion", method = RequestMethod.POST)
     public ModelAndView showPostedQuestion(@RequestParam Map<String, String> params){
 
         ModelAndView modelAndView = new ModelAndView("showPostedQuestion");
         modelAndView.addObject("questionTitle",params.get("questionTitle"));
        modelAndView.addObject("questionDescription",params.get("questionDescription"));
         return modelAndView;
     }
 }
