 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *   UWSchedule student class and registration sharing interface
  *   Copyright (C) 2013 Sherman Pay, Jeremy Teo, Zachary Iqbal
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by`
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.amgems.uwschedule.api.local;
 
 import com.amgems.uwschedule.model.Account;
 import com.amgems.uwschedule.model.Course;
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import java.util.List;
 
 import retrofit.Callback;
 import retrofit.RestAdapter;
 import retrofit.converter.GsonConverter;
 
 /**
  * @author Sherman Pay, Zachary Iqbal, Jeremy Teo on 2/10/14.
  * Class that interacts the Web Services provided for UW Schedule.
  */
 public class WebService {
     private final String TAG = getClass().getSimpleName();
     private static ScheduleRequest request;
    public static final String WEB_SERVICE_URL = "http://shermanpay.com:8080/uw_schedule/";
 
     public WebService() {
         Gson gson = new GsonBuilder()
                 .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                 .excludeFieldsWithoutExposeAnnotation()
                 .create();
         RestAdapter restAdapter = new RestAdapter.Builder()
                 .setEndpoint(WEB_SERVICE_URL)
                 .setConverter(new GsonConverter(gson))
                 .build();
         request = restAdapter.create(ScheduleRequest.class);
     }
 
     /**
      * Gets the user account information from the web service
      * Stores the information in an Account object
      * @param userName String representing the userName to get the account
      * @param callback Callback that stores the result in an Account object
      */
     public void getAccount(String userName, Callback<Account> callback) {
         request.getAccount(userName, callback);
     }
 
     /**
      * Adds the user account to the database given the String representing the userName
      * @param userName String representing the userName of the account
      * @param studentName String representing the studentName of the account.
      */
     public void putAccount(String userName, String studentName, Callback<Account> callback) {
         request.addAccount(userName, studentName, callback);
     }
 
     /**
      * Gets the List of courses a user is taking in a particular quarter
      * Stores the information in a List of Courses
      * @param userName String representing the username to get the courses
      * @param quarter String representing the quarter. eg "13wi" for winter 2013
      * @param callback Callback that stores the result in a List<Course> object
      */
     public void getCourses(String userName, String quarter, Callback<List<Course>> callback) {
         request.getCourses(userName, quarter, callback);
     }
 
     /**
      * Adds to the database a list of courses the user is taking.
      * @param userName String representing the username to store the information
      * @param quarter String representing the quarter. eg "13wi" for winter 2013
      * @param courses String representing the list of courses. Requires the toString of a Java
      *                Collection. eg "[12345, 56789]"
      * @param callback Callback that stores the result in a List<Course> object
      */
     public void putCourses(String userName, String quarter, String courses, Callback<List<Course>> callback) {
         request.addCourses(userName, quarter, courses, callback);
     }
 
     /**
      * Syncs to the database a list of courses a user is taking. Removes all old data before inserting.
      * @param userName String representing the username to store the information
      * @param quarter String representing the quarter. eg "13wi" for winter 2013
      * @param courses String representing the list of courses. Requires the toString of a Java
      *                Collection. eg "[12345, 56789]"
      * @param callback Callback that stores the result in a List<Course> object
      */
     public void syncCourses(String userName, String quarter, String courses, Callback<List<Course>> callback) {
         request.syncCourses(userName, quarter, courses, callback);
     }
 
 }
