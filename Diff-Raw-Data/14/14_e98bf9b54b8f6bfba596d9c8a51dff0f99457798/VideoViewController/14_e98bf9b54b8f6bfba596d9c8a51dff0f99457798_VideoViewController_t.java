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
        String videoLocation = "big_buck_bunny_480p_stereo";
        map.put("currentTime", "60");
        map.put("vidLocation_ogg", videoLocation + ".ogg");
         //map.put("vidLocation_mp4", videoLocation_mp4);
         //map.put("currentTime", 0);
         //logger.error("Loading Video");
         return "viewVideo";
     }
     @RequestMapping(value = "/viewVideo", method = RequestMethod.POST)
    public String saveVideoTimeLocation(@ModelAttribute("savedTime") String currentTime){
         // Save currentTime
        System.out.println("--------------------------------- Current Time is : " + currentTime);
         return "redirect:/viewVideo.htm";
     }
 }
