 package com.gumvision.web.controller;
 
 import com.google.gson.Gson;
 import com.gumtree.api.entity.Advert;
 import com.gumtree.api.service.impl.CApiAdvertsReader;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: markkelly
  * Date: 12/09/2011
  * Time: 17:12
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class MostRecentAdvertsJsonController {
 
     @Autowired
     private CApiAdvertsReader capiAdvertsReader;
 
     //Values Below are variables for Timing the Ads, the client(Browser) uses these
 
     //request another json for adverts when the size of the queue gets down to this variable
     @Value("${gumvision.advert.queue_reload:10}")
     private int queueReload;
 
     //Default of 1 second between popping up adverts
     @Value("${gumvision.advert.display_interval:1000}")
     private int adDisplayInterval;
 
     //Only display the ad for a certain period of time
     @Value("${gumvision.advert.display_for:60000}")
     private int adDisplayFor;
 
     private static final Gson gson = new Gson();
 
     @RequestMapping(value="/recentAdverts", method = RequestMethod.GET)
     public void getMostRecentAdvertsToDisplayJson(HttpServletRequest request, HttpServletResponse response) {
 
         List<Advert> ads;
 
         try {
             //Don't include any ads that do not meet the display requirements
             ads = filter(capiAdvertsReader.getMostRecentAdverts());
 
             //Add the display variables and the ads to display respectively
             List<Object> responseList = new LinkedList<Object>();
             responseList.add(queueReload);
             responseList.add(adDisplayInterval);
             responseList.add(adDisplayFor);
             responseList.addAll(ads);
 
             //add the json response
            response.setContentType("application/json");
             response.getWriter().write(gson.toJson(responseList));
             response.getOutputStream().close();
 
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private static List<Advert> filter(List<Advert> allAds) {
         List<Advert> ads = new LinkedList<Advert>();
 
         for(int i = 0; i < allAds.size(); i++) {
 
             if(allAds.get(i).canDisplay())
                 ads.add(allAds.get(i));
         }
 
         return ads;
     }
 }
