 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.controllers;
 
 import com.team33.entities.VideoInfo;
 import com.team33.services.BrowseService;
 import com.team33.services.VideoAccessService;
 import java.util.List;
 import java.util.Map;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author Mark
  */
 @Controller
 public class BrowseVideosController {
     @Autowired
     private BrowseService browseService;
     
     @Autowired
     private VideoAccessService videoAccessService;
             
     public void setBrowseService(BrowseService browseService){
         this.browseService = browseService;
     }
 
     public void setVideoAccessService(VideoAccessService videoAccessService) {
         this.videoAccessService = videoAccessService;
     }
     
     @RequestMapping(value = "/browseVideos", method = RequestMethod.GET)
     public String getVideos(Map<String,Object> map) {
         /*List<Object> list = new ArrayList<Object>();
         list.add(new DummyVideoInfo("The Lion King", "Mufasa dies.", "G", "Family"));
         list.add(new DummyVideoInfo("Top Gun", "Goose dies.", "PG-13", "Action"));
         list.add(new DummyVideoInfo("Final Destination", "They all die.", "14A", "Horror"));
         map.put("videoInfoList", list);*/
         
         List<VideoInfo> list = this.browseService.displayAllVideoContent();
         map.put("videoInfoList", list);
        System.out.println("Done Add to List : " + list.size());
         return "/browseVideos";
     }
     
     public class DummyVideoInfo {
         String name;
         String description;
         String screenRating;
         String genre;
         
         public DummyVideoInfo(String name, String description, String screenRating, String genre) {
             this.name = name;
             this.description = description;
             this.screenRating = screenRating;
             this.genre = genre;
         }
 
         public String getName() {
             return name;
         }
 
         public String getDescription() {
             return description;
         }
 
         public String getScreenRating() {
             return screenRating;
         }
 
         public String getGenre() {
             return genre;
         }
     }
 }
