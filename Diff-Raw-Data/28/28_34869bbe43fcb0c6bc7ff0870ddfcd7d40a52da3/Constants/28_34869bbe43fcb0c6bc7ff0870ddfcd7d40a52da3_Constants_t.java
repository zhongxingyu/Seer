 package com.geek.exercise;
 
 /**
  * Created by Pequots34 on 7/9/13.
  */
 public class Constants {
 
     public static final String ACTION_RECOGNITION_SERVICE = "com.geek.exercise.ACTION_RECOGNITION_SERVICE";
 
     public static final String RECOGNITION_SERVICE_INTENT_EXTRA = "activity";
 
     public static final String ACCOUNT_PREFERENCE_NAME = "AccountPreferenceName";
 
    public static final String ACCOUNT_PREFERENCE_USERNAME = "username";
 
    public static final String ACCOUNT_PREFERENCE_CREATED = "created";
 
     public static final String EMPTY_STRING = new String();
 
     private Constants() {
         super();
 
         throw new InstantiationError();
     }
 }
