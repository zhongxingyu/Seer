 package com.forum.web.controller;
 
 import com.forum.repository.TestRepository;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class DummyController {
 
    @Autowired
    @Qualifier("dataSource")
    BasicDataSource dataSource;

     @RequestMapping(value = "/dummy", method = RequestMethod.GET)
     public ModelAndView dfsfds (){
        TestRepository testRepository = new TestRepository(dataSource);
         testRepository.getNumber();
         return new ModelAndView("DBTest");
     }
 }
