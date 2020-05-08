 package de.team06.psychoapp;
 
 /**
  * Created by Malte on 28.06.13.
  */
 import android.content.ContentValues;
 
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Locale;
 
 public class SocialInteraction {
 
     // ID - Unique Identifer in Database
     private long id;
     // Probandencode as String
     private String code;
     // AlarmTime (int) as unix timestamp
     private int alarmTime;
     // ResponseTime (int) as unix timestamp
     private int responseTime;
     // User skipped Poll (boolean)
     private boolean skipped;
     // number of social Interactions since last alarm
     private int numberOfContacts;
     // hours of social interference since last alarm
     private int hours;
     // minutes of social interference since last alarm
     private int minutes;
 
     public SocialInteraction() {
         // init here
      }
 
     public long getId() {
         return id;
     }
 
     public String getCode() {
         return code;
     }
 
 
     /**
      * Returns alarmTime in msec since 1.1.1970 00:00 UTC
      * @return long alarmTime
      */
     public int getAlarmTime() {
         return alarmTime * 1000;
     }
 
     /**
      * Get the date of the Alarm formatted dd.mm.yyyy as string
      * @return String alarmDate
      */
     public String getAlarmDateCSV() {
         Date date = new Date();
         date.setTime((long)alarmTime*1000);
 
         DateFormat dformat = new SimpleDateFormat( "dd.MM.yy" );
 
         return dformat.format(date);
     }
 
     /**
      * Get the Alarm Time formatted hh:mm as string
      * @return String alarmTime
      */
     public String getAlarmTimeCSV() {
         Date date = new Date();
         date.setTime((long)alarmTime * 1000);
 
        DateFormat dformat = new SimpleDateFormat( "HH:mm" );
 
         return dformat.format(date);
     }
 
     /**
      * Get the Alarm Time formatted EEEE as string
      * @return String alarmTime
      */
     public String getAlarmDayCSV() {
         Date date = new Date();
         date.setTime((long)alarmTime * 1000);
 
         DateFormat dformat = new SimpleDateFormat( "EEEE", Locale.GERMAN );
 
         return dformat.format(date);
     }
 
 
     /**
      * Returns responseTime in msec since 1.1.1970 00:00 UTC
      * @return long responseTime
      */
     public long getResponseTime() {
         return responseTime * 1000;
     }
     /**
      * Get the Response Time formatted hh:mm as string
      * @return String ResponseTime
      */
     public String getResponseTimeCSV() {
         Date date = new Date();
         date.setTime((long)responseTime*1000);
 
         DateFormat dformat = new SimpleDateFormat( "hh:mm" );
 
         return dformat.format(date);
     }
 
     public boolean isSkipped() {
         return skipped;
     }
 
     /**
      * Get status whether poll was skipped
      * @return int skipped (0|1)
      */
     public int isSkippedCSV() {
         if (isSkipped()) return 1;
         else return 0;
     }
 
     public int getNumberOfContacts() {
         return numberOfContacts;
     }
 
 
     public int getHours() {
         return hours;
     }
 
     public int getMinutes() {
         return minutes;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public void setAlarmTime(int alarmTime) {
         this.alarmTime = alarmTime;
     }
 
     /**
      * Set responseTime in msec since 1.1.1970 00:00 UTC
      * @param long responseTime
      */
     public void setResponseTime(long responseTime) {
         this.responseTime =(int) responseTime / 1000;
     }
 
     public void setSkipped(int skipped) {
         if(skipped == 0)
             this.skipped = false;
         else
             this.skipped = true;
     }
 
     public void setNumberOfContacts(int numberOfContacts) {
         this.numberOfContacts = numberOfContacts;
     }
 
     public void setHours(int hours) {
         this.hours = hours;
     }
 
     public void setMinutes(int minutes) {
         this.minutes = minutes;
     }
 
     public void save(){
         // save socialInteraction to db for example
     }
 
     @Override
     public String toString() {
         return "SocialInteraction{" +
                 "id=" + id +
                 ", code='" + code + '\'' +
                 ", alarmTime=" + alarmTime +
                 ", responseTime=" + responseTime +
                 ", skipped=" + skipped +
                 ", numberOfContacts=" + numberOfContacts +
                 ", hours=" + hours +
                 ", minutes=" + minutes +
                 '}';
     }
 
     public ContentValues toContentValues(){
         ContentValues values = new ContentValues();
 
         values.put("code", code);
         values.put("alarmtime", alarmTime);
         values.put("responsetime", responseTime);
         values.put("skip", this.isSkippedCSV());
         values.put("contacts", numberOfContacts);
         values.put("hours", hours);
         values.put("minutes", minutes);
 
         return values;
     }
 }
