 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.controllers;
 
 import com.team33.services.playback.VideoPlaybackService;
 import java.util.Map;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 /**
  *
  * @author Caleb
  */
 @Controller
 @SessionAttributes("alreadyLogin")
 public class VideoViewController {
     
    @Autowired
     private VideoPlaybackService playbackService;
     
     public void setPlaybackService(VideoPlaybackService service){
         this.playbackService = service;
     }
     
     @RequestMapping(value = "/viewVideo.htm/${orderId}/${videoId}", method = RequestMethod.GET)
     public String loadVideoOnStartup(Map<String, Object> map, @RequestParam("orderId")String orderId, @RequestParam("videoId")String videoId, @ModelAttribute("alreadyLogin")Integer tokenId){
         System.out.println("------ Loading Video -------");
         // Load video location from DB
         
         String videoLocation = playbackService.getVideoFileInformation(tokenId.intValue(), Integer.parseInt(videoId));
         int currentTime = playbackService.getInformation(tokenId.intValue(), Integer.parseInt(orderId), Integer.parseInt(videoId));
         
         // Tmp Until Service Layer added in
         //String videoLocation = "big_buck_bunny_480p_stereo";
         map.put("currentTime", currentTime);
         map.put("vidLocation_ogg", videoLocation + ".ogg");
         //map.put("vidLocation_mp4", videoLocation_mp4);
         //map.put("currentTime", 0);
         //logger.error("Loading Video");
         map.put("orderId", orderId);
         map.put("videoId", videoId);
         
         return "viewVideo";
     }
     @RequestMapping(value = "/viewVideo", method = RequestMethod.POST)
     public String saveVideoTimeLocation(@ModelAttribute("savedTime") String currentTime, @ModelAttribute("orderId")String orderId, @ModelAttribute("videoId")String videoId, @ModelAttribute("alreadyLogin")Integer tokenId){
         // Save currentTime
         System.out.println("--------------------------------- Current Time is : " + currentTime);
                 
         playbackService.saveInformation(tokenId.intValue(), Integer.parseInt(orderId), Integer.parseInt(videoId), Integer.parseInt(currentTime));
         
         return "redirect:/viewVideo.htm";
     }
 }
