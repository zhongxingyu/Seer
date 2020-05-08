 /**
  * Copyright 2012 StackMob
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.roflcode.fitnessChallenge;
 
 import com.fitbit.api.FitbitAPIException;
 import com.fitbit.api.client.*;
 import com.fitbit.api.common.model.activities.Activities;
 import com.fitbit.api.common.model.activities.ActivitiesSummary;
 import com.fitbit.api.model.FitbitUser;
 import com.stackmob.core.DatastoreException;
 import com.stackmob.core.InvalidSchemaException;
 import com.stackmob.core.customcode.CustomCodeMethod;
 import com.stackmob.core.rest.ProcessedAPIRequest;
 import com.stackmob.core.rest.ResponseToProcess;
 import com.stackmob.sdkapi.*;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.HttpURLConnection;
 import java.util.*;
 
 
 public class UpdateFitbitActivities implements CustomCodeMethod {
 
 
     private static LoggerService logger;
 
     @Override
   public String getMethodName() {
     return "update_fitbit_activities";
   }
 
   @Override
   public List<String> getParams() {
     return Arrays.asList("stackmob_user_id", "start_date", "end_date");
   }
 
 
   @Override
   public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
       logger = serviceProvider.getLoggerService(UpdateFitbitActivities.class);
       logger.debug("update fitbit activities ------------------------------");
 
       String stackmobUserID = request.getParams().get("stackmob_user_id");
       String startDateStr = request.getParams().get("start_date");
       String endDateStr = request.getParams().get("end_date");
       if (endDateStr == "") {
           endDateStr = startDateStr;
       }
 
       if (stackmobUserID == null || stackmobUserID.isEmpty()) {
           HashMap<String, String> errParams = new HashMap<String, String>();
           errParams.put("error", "stackmobUserID was empty or null");
           return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
       }
 
       FitbitApiClientAgent agent = AgentInitializer.GetInitializedAgent(serviceProvider, stackmobUserID);
       if (agent == null) {
           HashMap<String, String> errParams = new HashMap<String, String>();
           errParams.put("error", "could not initialize fitbit client agent");
           return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errParams); // http 500 internal error
       }
 
 
       HashMap<String, String> credentials = AgentInitializer.GetStoredFitbitCredentials(serviceProvider, stackmobUserID);
       String fitbitUserID = credentials.get("fitbituserid");
       LocalUserDetail user = new LocalUserDetail(stackmobUserID);
       FitbitUser fitbitUser = new FitbitUser(fitbitUserID);
       //LocalDate today = new LocalDate(DateTimeZone.UTC);
 
 
       DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
       DateTime dt = formatter.parseDateTime(startDateStr);
       LocalDate startDate = new LocalDate(dt);
       dt = formatter.parseDateTime(endDateStr);
       LocalDate endDate = new LocalDate(dt);
 
       //TimeZone tz = TimeZone.getTimeZone("GMT-8:00");
       //LocalDate today = new LocalDate(DateTimeZone.forTimeZone(tz));
       //LocalDate today = new LocalDate(DateTimeZone.forTimeZone(tz));
       //LocalDate yesterday = today.minusDays(1);
       int addedCount = 0;
       int updatedCount = 0;
 
       logger.debug("entering date loop " + startDate.toString() + " end: " + endDate.toString());
       for (LocalDate date = startDate; date.isBefore(endDate) || date.isEqual(endDate); date = date.plusDays(1))
       {
           logger.debug("date: " + date.toString());
 
           Activities activities;
 
           try {
             activities = agent.getActivities(user, fitbitUser, date);
           }
           catch (FitbitAPIException ex) {
               logger.error("failed to get activities", ex);
               HashMap<String, String> errParams = new HashMap<String, String>();
               errParams.put("error", "could not get activities");
               return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errParams); // http 500 internal error
           }
 
           ActivitiesSummary summary = activities.getSummary();
           if (summary != null) {
         //          private Integer floors = null;
         //          private Double elevation = null;
         //          private List<ActivityDistance> distances;
 
               DataService dataService = serviceProvider.getDataService();
 
               DateMidnight dateMidnight = date.toDateMidnight();
               long millis = dateMidnight.getMillis();
 
               List<SMCondition> query;
               List<SMObject> result;
 
               // build a query
               query = new ArrayList<SMCondition>();
               query.add(new SMEquals("theusername", new SMString(stackmobUserID)));
               query.add(new SMEquals("activity_date", new SMInt(millis)));
 
 
 
               // execute the query
               try {
                   logger.debug("looking up activities");
                   logger.debug("query= username: " + stackmobUserID + " activity date millis: " + millis);
 
                   boolean newActivity = false;
                   SMValue activityId;
                   result = dataService.readObjects("activity", query);
 
                   SMObject activityObject;
 
                   logger.debug("readObjects completed");
                   //activity was in the datastore, so update
                   if (result != null && result.size() == 1) {
                       activityObject = result.get(0);
                       List<SMUpdate> update = new ArrayList<SMUpdate>();
                       update.add(new SMSet("active_score", new SMInt((long)summary.getActiveScore())));
                       update.add(new SMSet("steps", new SMInt((long)summary.getSteps())));
                       long floors = 0;
                       if (summary.getFloors() != null) { // uh, great work there fitbit api, floors can be null
                           floors = (long) summary.getFloors();
                       }
                      update.add(new SMSet("floors", new SMInt(floors)));
                       update.add(new SMSet("sedentary_minutes", new SMInt((long)summary.getSedentaryMinutes())));
                       update.add(new SMSet("lightly_active_minutes", new SMInt((long)summary.getLightlyActiveMinutes())));
                       update.add(new SMSet("fairly_active_minutes", new SMInt((long)summary.getFairlyActiveMinutes())));
                       update.add(new SMSet("very_active_minutes", new SMInt((long)summary.getVeryActiveMinutes())));
                       activityId = activityObject.getValue().get("activity_id");
                       logger.debug("update object");
                       dataService.updateObject("activity", activityId, update);
                       logger.debug("updated object");
                       updatedCount++;
                   }
                   else {
                       Map<String, SMValue> activityMap = new HashMap<String, SMValue>();
                       activityMap.put("theusername", new SMString(stackmobUserID));
                       activityMap.put("activity_date", new SMInt(millis));
                       activityMap.put("activity_date_str", new SMString(date.toString()));
                       activityMap.put("active_score", new SMInt((long) summary.getActiveScore()));
                       activityMap.put("steps", new SMInt((long) summary.getSteps()));
                       long floors = 0;
                       if (summary.getFloors() != null) { // uh, great work there fitbit api, floors can be null
                           floors = (long) summary.getFloors();
                       }
                       activityMap.put("floors", new SMInt(floors));
                       activityMap.put("sedentary_minutes", new SMInt((long) summary.getSedentaryMinutes()));
                       activityMap.put("lightly_active_minutes", new SMInt((long) summary.getLightlyActiveMinutes()));
                       activityMap.put("fairly_active_minutes", new SMInt((long) summary.getFairlyActiveMinutes()));
                       activityMap.put("very_active_minutes", new SMInt((long) summary.getVeryActiveMinutes()));
 
                       activityObject = new SMObject(activityMap);
                       logger.debug("create object");
                       activityObject = dataService.createObject("activity", activityObject);
                       logger.debug("created object");
                       activityId = activityObject.getValue().get("activity_id");
                       newActivity = true;
                       addedCount++;
 
                   }
 
               } catch (InvalidSchemaException e) {
                   HashMap<String, String> errMap = new HashMap<String, String>();
                   errMap.put("error", "invalid_schema");
                   errMap.put("detail", e.toString());
                   return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
               }
               catch (DatastoreException e) {
                   HashMap<String, String> errMap = new HashMap<String, String>();
                   errMap.put("error", "datastore_exception");
                   errMap.put("detail", e.toString());
                   return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
               }
               catch(Exception e) {
                   HashMap<String, String> errMap = new HashMap<String, String>();
                   errMap.put("error", "unknown");
                   StringWriter errors = new StringWriter();
                   e.printStackTrace(new PrintWriter(errors));
 
                   errMap.put("detail", errors.toString());
                   return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
               }
           }
       }
       Map<String, Object> returnMap = new HashMap<String, Object>();
       returnMap.put("days of activities updated", updatedCount);
       returnMap.put("days of activities added", addedCount);
 //      returnMap.put("activity_id", activityId);
 //      returnMap.put("newActivity", newActivity);
       //returnMap.put("activitiesJson", activities);
       logger.debug("completed get activities");
       return new ResponseToProcess(HttpURLConnection.HTTP_OK, returnMap);
   }
 }
