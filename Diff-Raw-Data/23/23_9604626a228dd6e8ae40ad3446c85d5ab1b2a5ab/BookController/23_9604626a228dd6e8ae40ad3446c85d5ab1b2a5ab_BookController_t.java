 package com.hoiio.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.hoiio.controller.api.Add;
 import com.hoiio.controller.api.Get;
 import com.hoiio.model.Book;
 import com.hoiio.model.Job;
 import com.hoiio.util.DateUtil;
 
 @Controller
 @RequestMapping("/book")
 public class BookController {
     @Autowired
     Get get;
     @Autowired
     Add add;
 
     @RequestMapping(value="/names", method=RequestMethod.GET)
      public List<Book> getNames() {
 
       return returnData();
      }
 
      private List<Book> returnData() {
 
       ArrayList<Book> names = new ArrayList<Book>();
 
       Book b1 = new Book();
       Book b2 = new Book();
       Book b3 = new Book();
 
       b1.setName("book nro. 1");
       b1.setId(0);
       b2.setName("book nro. 2");
       b2.setId(1);
       b3.setName("book nro. 3");
       b3.setId(2);
 
       names.add(b1);
       names.add(b2);
       names.add(b3);
 
       return names;
      }
 
      @RequestMapping(value="/get", method=RequestMethod.GET)
      @ResponseBody
      public String get() {
         LoggerFactory.getLogger(BookController.class).debug("Testing");
          return get.get();
      }
 
      @RequestMapping(value="/add", method=RequestMethod.GET)
      @ResponseBody
      public String add() {
          Job job = new Job();
          job.setType(Job.Type.SMS);
          job.setFrom("+Test");
          job.setTo("+To");
          job.setDate(DateUtil.convertStringToDateTime("2012-11-30 17:01:01"));
          return add.add(job);
      }
 }
