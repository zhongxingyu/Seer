 package org.notificationengine.web.controller;
 
 import com.google.gson.Gson;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.notificationengine.constants.Constants;
 import org.notificationengine.domain.DecoratedNotification;
 import org.notificationengine.domain.Topic;
 import org.notificationengine.persistance.Persister;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Controller
 public class DecoratedNotificationController {
 
     private static Logger LOGGER = Logger.getLogger(DecoratedNotificationController.class);
 
     @Autowired
     private Persister persister;
 
     public DecoratedNotificationController() {
 
         LOGGER.debug("DecoratedNotificationController instantiated and listening");
     }
 
 
     @RequestMapping(value = "/notSentDecoratedNotificationsForTopic.do", method = RequestMethod.GET, params = {"topic"})
     @ResponseBody
     public String getNotSentDecoratedNotificationsForTopic(@RequestParam(value="topic") String topicName) {
 
         LOGGER.debug("getNotSentDecoratedNotificationsForTopic in DecoratedNotificationController");
 
         Topic topic = new Topic(topicName);
 
         Collection<DecoratedNotification> decoratedNotifications = this.persister.retrieveNotSentDecoratedNotificationsForTopic(topic);
 
         Gson gson = new Gson();
 
         String result = gson.toJson(decoratedNotifications);
 
         return result;
 
     }
 
     @RequestMapping(value = "/countNotSentDecoratedNotifications.do", method = RequestMethod.GET)
     @ResponseBody
     public String getCountNotSentDecoratedNotifications() {
 
         LOGGER.debug("getCountNotSentDecoratedNotifications in DecoratedNotificationController");
 
         Collection<DecoratedNotification> decoratedNotifications = this.persister.retrieveNotSentDecoratedNotifications();
 
         Integer countNotSentDecoratedNotifications = decoratedNotifications.size();
 
         JSONObject response = new JSONObject();
 
         response.put(Constants.COUNT_NOT_SENT_DECORATED_NOTIFICATIONS, countNotSentDecoratedNotifications);
 
         return response.toString();
 
     }
 
     @RequestMapping(value = "/countAllDecoratedNotifications.do", method = RequestMethod.GET)
     @ResponseBody
     public String getCountAllDecoratedNotifications() {
 
         LOGGER.debug("getCountNotSentDecoratedNotifications in DecoratedNotificationController");
 
         Collection<DecoratedNotification> decoratedNotifications = this.persister.retrieveAllDecoratedNotifications();
 
         Integer countDecoratedNotifications = decoratedNotifications.size();
 
         JSONObject response = new JSONObject();
 
         response.put(Constants.COUNT_ALL_DECORATED_NOTIFICATIONS, countDecoratedNotifications);
 
         return response.toString();
 
     }
 
     @RequestMapping(value = "/countAllDecoratedNotificationsForTopic.do", method = RequestMethod.GET, params = {"topic"})
     @ResponseBody
     public String getCountAllDecoratedNotificationsForTopic(@RequestParam(value="topic") String topicName) {
 
         LOGGER.debug("getCountNotSentDecoratedNotificationsForTopic " + topicName + " in DecoratedNotificationController");
 
         Topic topic = new Topic(topicName);
 
         Collection<DecoratedNotification> decoratedNotifications = this.persister.retrieveAllDecoratedNotificationsForTopic(topic);
 
         Integer countDecoratedNotificationsForTopic = decoratedNotifications.size();
 
         JSONObject response = new JSONObject();
 
         response.put(Constants.COUNT_ALL_DECORATED_NOTIFICATIONS_FOR_TOPIC, countDecoratedNotificationsForTopic);
 
         response.put(Constants.TOPIC, topic);
 
         return response.toString();
 
     }
 
     @RequestMapping(value = "/countNotSentDecoratedNotificationsForTopic.do", method = RequestMethod.GET, params = {"topic"})
     @ResponseBody
     public String getCountNotSentDecoratedNotificationsForTopic(@RequestParam(value="topic") String topicName) {
 
         LOGGER.debug("getCountNotSentDecoratedNotificationsForTopic in DecoratedNotificationController");
 
         Topic topic = new Topic(topicName);
 
         Collection<DecoratedNotification> decoratedNotifications = this.persister.retrieveNotSentDecoratedNotificationsForTopic(topic);
 
         Integer countNotSentDecoratedNotificationsForTopic = decoratedNotifications.size();
 
         JSONObject response = new JSONObject();
 
         response.put(Constants.COUNT_NOT_SENT_DECORATED_NOTIFICATIONS_FOR_TOPIC, countNotSentDecoratedNotificationsForTopic);
 
         response.put(Constants.TOPIC, topic);
 
         return response.toString();
 
     }
 
    @RequestMapping(value = "/countSentDecoratedNotificationsForLastDays.do", method = RequestMethod.GET, params = {"days"})
     @ResponseBody
     public String getSentDecoratedNotificationsForLastDays(@RequestParam("days") Integer nbDays) {
 
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
 
             Integer nbRowNotificationsCreated = this.persister.retrieveSentDecoratedNotificationsForDate(atDate).size();
 
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
             String formattedDate = dateFormat.format(atDate);
 
             stats.put(formattedDate, nbRowNotificationsCreated);
 
         }
 
         Gson gson = new Gson();
 
         String result = gson.toJson(stats);
 
         return result;
 
     }
 
    @RequestMapping(value = "/countCreatedDecoratedNotificationsForLastDays.do", method = RequestMethod.GET, params = {"days"})
     @ResponseBody
     public String getCreatedDecoratedNotificationsForLastDays(@RequestParam("days") Integer nbDays) {
 
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
 
             Integer nbRowNotificationsCreated = this.persister.retrieveDecoratedNotificationsForDate(atDate).size();
 
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
             String formattedDate = dateFormat.format(atDate);
 
             stats.put(formattedDate, nbRowNotificationsCreated);
 
         }
 
         Gson gson = new Gson();
 
         String result = gson.toJson(stats);
 
         return result;
 
     }
 
     public Persister getPersister() {
         return persister;
     }
 
     public void setPersister(Persister persister) {
         this.persister = persister;
     }
 
 
 }
