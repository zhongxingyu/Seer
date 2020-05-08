 package com.opitz.devices.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 /**
  * User: Pascal
  * Date: 03.09.13
  * Time: 22:06
  */
 
 @Controller
 @RequestMapping("/service/userstate")
 public class UserStateController {
 
 
     //@Autowired
     //private RuntimeService runtimeService;
 
     /**
      *
      * Method to set the current state of the user. For now only basic functionality
      * @param username
      * @param state expected states: present, absent
      * @return
      */
     @ResponseBody
     @RequestMapping(value="/{username}/setuserstate", method = RequestMethod.POST)
     public String setUserState(@PathVariable("username") String username, @RequestParam String state){
         return "foo";
     }
 
     /*@ResponseBody
     @RequestMapping(value="/triggerprocess", method = RequestMethod.GET)
     public String triggerProcess(){
         ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userstatechange");
         System.out.print(processInstance.toString());
         return processInstance.toString();
 
     }*/
 
 
 
 
 }
