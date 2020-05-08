 package org.notificationengine.web.controller;
 
 import com.google.gson.Gson;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.notificationengine.constants.Constants;
 import org.notificationengine.domain.DecoratedNotification;
 import org.notificationengine.persistance.Persister;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Controller
 public class DeletedDecoratedNotificationController {
 
     private static Logger LOGGER = Logger.getLogger(DeletedDecoratedNotificationController.class);
 
     @Autowired
     private Persister persister;
 
     public DeletedDecoratedNotificationController() {
 
         LOGGER.debug("DeletedDecoratedNotificationController instantiated and listening");
 
     }
 
     @RequestMapping(value = "/countDeletedDecoratedNotifications.do", method = RequestMethod.GET)
     @ResponseBody
     public String getCountDeletedDecoratedNotifications() {
 
         Collection<DecoratedNotification> deletedDecoratedNotifications = this.persister.retrieveAllDeletedDecoratedNotifications();
 
         Integer countDeletedDecoratedNotifications = deletedDecoratedNotifications.size();
 
         JSONObject response = new JSONObject();
 
         response.put(Constants.COUNT, countDeletedDecoratedNotifications);
 
         return response.toString();
 
     }
 
    @RequestMapping(value = "/countDeletedDecoratedNotificationsForLastDays.do", method = RequestMethod.GET, params = {"days"})
     @ResponseBody
     public String getCountDeletedDecoratedNotificationsForLastDays(@RequestParam("days") Integer nbDays) {
 
         Date date = new Date();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         // Create all dates wanted to retrieve data
         Collection<Date> datesToGet = new ArrayList<>();
 
         for(Integer day = 0; day < nbDays; day ++) {
 
             datesToGet.add(cal.getTime());
 
             cal.add(Calendar.DAY_OF_MONTH, -1);
 
         }
 
         //Retrieve data and store it to be sent as response
 
         Map<String, Integer> stats = new HashMap<>();
 
         for(Date atDate : datesToGet) {
 
             Integer nbRowNotificationsCreated = this.persister.retrieveDeletedDecoratedNotificationForDate(atDate).size();
 
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
             String formattedDate = dateFormat.format(atDate);
 
             stats.put(formattedDate, nbRowNotificationsCreated);
 
         }
 
         Gson gson = new Gson();
 
         String result = gson.toJson(stats);
 
         return result;
     }
 
 }
