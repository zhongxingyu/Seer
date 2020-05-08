 package com.smartsms.controllers;
 
 import com.smartsms.repo.config.ApplicationTypeRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Sony
  * Date: 03/06/13
  * Time: 9:40 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class VotingAppController {
 
     @Autowired
     private ApplicationTypeRepository applicationTypeRepository;
 
     @RequestMapping(value = "/VotingAppCreate", method = RequestMethod.GET)
     public String redirect(){
         return "VotingAppCreate";
 
     }
 
     @RequestMapping(value = "/VotingAppConfirm", method = RequestMethod.GET)
     public String redirectConfirm(){
         return "VotingAppConfirm";
 
     }
 
     @RequestMapping(value = "/VotingAppSuccess", method = RequestMethod.GET)
     public String redirectSuccess(){
         return "VotingAppSuccess";
 
     }
 
     @RequestMapping(value = "/VotingHelp", method = RequestMethod.GET)
     public String redirectHelp(){
         return "VotingHelp";
 
     }
 
    @RequestMapping(value = "/MyApplications", method = RequestMethod.GET)
    public String redirectMyApplications(){
        return "MyApplications";

    }
 }
