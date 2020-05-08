 package com.hackaton.duma.controller;
 
 import com.hackaton.duma.dao.DeputyDAO;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.annotation.Resource;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ernest
  * Date: 9/15/13
  * Time: 4:48 AM
  */
 @Controller
 @RequestMapping(value = "/laziest")
 public class LazyRatingController {
     @Resource(name = "deputyDAO")
     private DeputyDAO deputyDAO;
 
     @RequestMapping(method = RequestMethod.GET)
     public String laziestGET(Model model) {
         model.addAttribute("rating", deputyDAO.getLaziest(30, 0));
         return "ratings/laziest";
     }
 }
