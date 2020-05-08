 package com.allplayers.objects;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import com.google.gson.Gson;
 
 public class MessageData extends DataObject {
     private String thread_id = "";
     private String subject = "";
     private String is_new = "";
     private String last_message_sender = "";
     private String last_message_body = "";
     private String last_updated = "";
     private Date updatedDate = null;
 
     public MessageData() {
    	last_updated += "000"; //Converts to seconds.
     	updatedDate = parseTimestamp(last_updated);
         last_updated = Long.toString(updatedDate.getTime()); //update the string in case someone uses it
     }
 
     private Date parseTimestamp(String timestamp) {
         Date date = new Date(Long.parseLong(timestamp));
 
         TimeZone timezone = TimeZone.getDefault();
         int offset = timezone.getOffset(date.getTime());
         date = new Date(date.getTime() + offset);
         return date;
     }
 
     public String getTimestampString() {
         return last_updated;
     }
 
     public Date getDate() {
         return updatedDate;
     }
 
     public String getDateString() {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(updatedDate);
 
         int day = calendar.get(Calendar.DAY_OF_MONTH);
         int month = calendar.get(Calendar.MONTH) + 1; //because calendar uses 0-11 instead of 1-12
         int year = calendar.get(Calendar.YEAR);
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
         int minute = calendar.get(Calendar.MINUTE);
         String AmPm = "AM";
 
         if (hour >= 12) {
             AmPm = "PM";
 
             if (hour > 12) {
                 hour = hour - 12;
             }
         } else if (hour == 0) {
             hour = 12;
         }
 
         DecimalFormat df = new DecimalFormat("00");
 
         return "" + df.format(month) + "/" + df.format(day) + "/" + year + " " + df.format(hour) + ":" + df.format(minute) + AmPm;
     }
 
     public String getThreadID() {
         return thread_id;
     }
 
     public String getId() {
         return thread_id;
     }
 
     public String getMessageBody() {
         return last_message_body;
     }
 
     public String getLastSender() {
         return last_message_sender;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public String getNew() {
         return is_new;
     }
 }
