 package com.forum.web.controller;
 
 import com.forum.repository.TestRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class DummyController {
 
     @RequestMapping(value = "/dummy", method = RequestMethod.GET)
     public ModelAndView dfsfds (){
        TestRepository testRepository = new TestRepository();
         testRepository.getNumber();
         return new ModelAndView("DBTest");
     }
 }
