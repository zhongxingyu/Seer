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
 
 import java.util.List;
 
 import retrofit.Callback;
 import retrofit.http.Field;
 import retrofit.http.FormUrlEncoded;
 import retrofit.http.GET;
 import retrofit.http.POST;
 import retrofit.http.Query;
 
 /**
  * Created by shermpay on 2/10/14.
  * Retrofit interface that allows simple interaction with a web server.
  * Methods are not meant to be overridden.
  * Interaction with the web service can be achieved via a RestAdapter.
  */
 public interface ScheduleRequest {
     @GET("/find_user")
     void getAccount(@Query("user_name") String userName, Callback<Account> callback);
 
     @FormUrlEncoded
     @POST("/add_user")
    void addAccount(@Field("user_name") String userName, @Field("student_name") String studentName,
            Callback<Account> callback);
 
     @GET("/find_courses")
     void getCourses(@Query("user_name") String userName, @Query("quarter") String quarter,
                     Callback<List<Course>> callback);
 
     @FormUrlEncoded
     @POST("/add_courses")
     void addCourses(@Field("user_name") String userName, @Field("quarter") String quarter,
                     @Field("courses") String courses, Callback<List<Course>> callback);
 
     @FormUrlEncoded
     @POST("/sync_courses")
     void syncCourses(@Field("user_name") String userName, @Field("quarter") String quarter,
               @Field("courses") String courses, Callback<List<Course>> callback);
 
 }
