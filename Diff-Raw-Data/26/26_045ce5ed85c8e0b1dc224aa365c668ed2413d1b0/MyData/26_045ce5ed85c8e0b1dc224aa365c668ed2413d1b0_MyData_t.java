 package com.threedigital.weatherme;
 
 /**
  * Created by 3digital on 9/6/13.
  */
 public class MyData {
 
     private static double mCurrentTemp;
     private static double mWindSpeed;
     private static double mPercipitation;
    private static String mTodaysStatus;
 
 
 
 
     public static double getmCurrentTemp() {
         return mCurrentTemp;
     }
 
     public void setmCurrentTemp(double mCurrentTemp) {
         this.mCurrentTemp = mCurrentTemp;
     }
 
     public static double getmWindSpeed() {
         return mWindSpeed;
     }
 
     public void setmWindSpeed(double mWindSpeed) {
         this.mWindSpeed = mWindSpeed;
     }
 
     public static double getmPercipitation() {
         return mPercipitation;
     }
 
     public void setmPercipitation(double mPercipitation) {
         this.mPercipitation = mPercipitation;
     }
 
    public static String getmTodaysStatus() {
         return mTodaysStatus;
     }
 
     public void setmTodaysStatus(String mTodaysStatus) {
         this.mTodaysStatus = mTodaysStatus;
     }
 
 
 
 }
 
