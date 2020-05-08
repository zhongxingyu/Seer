 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.manuwebdev.mirageobjectlibrary.Alarm;
 
 import com.manuwebdev.mirageobjectlibrary.Authentication.User;
 
 /**
  *
 * @author manuel
  */
 public class Alarm {
     int hour;
     int minute;
     String message;
     User user;
             
     /**
      * Constructor for Alarm object.
      * 
      * @param Hour Integer representing hour of Alarm
      * @param Minute Integer representing minute of Alarm
      * @param Message Message that describes Alarm
      * @param user User Alarm is registered to
      */
     public Alarm(int Hour, int Minute, String Message, User User){
         hour=Hour;
         minute=Minute;
         message=Message;
         user=User;
     }
     
     /**
      * Returns the hour at which the Alarm will trigger.
      * 
      * @return Hour our at which Alarm will trigger
      */
     public int getAlarmHour(){
         return hour;
     }
     
     /**
      * Returns the minute at which the Alarm will trigger.
      * 
      * @return Minute Returns the minute at which the Alarm will trigger.
      */
     public int getAlarmMinute(){
         return minute;
     }
     
     /**
      * Return the message that the Alarm represents.
      * 
      * @return Message Message that Alarm represents.
      */
     public String getAlarmMessage(){
         return message;
     }
     
     /**
      * Returns the user that created the Alarm as
      * a User object.
      * 
      * @return User User that created the Alarm.
      */
     public User getAlarmUser(){
         return user;
     }
 }
