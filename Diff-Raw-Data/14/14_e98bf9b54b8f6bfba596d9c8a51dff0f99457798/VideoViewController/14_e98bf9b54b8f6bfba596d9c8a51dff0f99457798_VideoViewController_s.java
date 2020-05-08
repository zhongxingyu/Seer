 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.controllers;
 
 import java.util.Map;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author Caleb
  */
 @Controller
 public class VideoViewController {
     
     @RequestMapping(value = "/viewVideo", method = RequestMethod.GET)
     public String loadVideoOnStartup(Map<String, Object> map){
         System.out.println("------ Loading Video -------");
         // Load video location from DB
         
         // Tmp Until Service Layer added in
        String videoLocation = "happy";
        map.put("vidLocation_mp4", (videoLocation + ".mp4"));
        map.put("vidLocation_webm", (videoLocation + ".webm"));
        map.put("currentTime", new IntegerWrapper(159));
        map.put("savedTime", new IntegerWrapper());
         //map.put("vidLocation_mp4", videoLocation_mp4);
         //map.put("currentTime", 0);
         //logger.error("Loading Video");
         return "viewVideo";
     }
     @RequestMapping(value = "/viewVideo", method = RequestMethod.POST)
    public String saveVideoTimeLocation(@ModelAttribute("savedTime") IntegerWrapper currentTime){
         // Save currentTime
        System.out.println("--------------------------------- Current Time is : " + currentTime.getInteger());
         return "redirect:/viewVideo.htm";
     }
 }
