 package com.ksoft.android.model;
 
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Catalin Samarghitan EMail : catalin.samarghitan@gmail.com
  * Date: 4/17/13
  * Time: 10:42 AM
  */
 public class Reminder {
 
     public static final String REMINDER_TABLE_NAME = "REMINDER";
    public static final String _ID = "ID";
     public static final String LOCATION_ID = "LOCATION_ID";
     public static final String TYPE = "REMINDER_TYPE";
     public static final String REMINDER_TIME = "REMINDER_TIME";
     public static final String REMINDER_TEXT = "REMINDER_TEXT";
 
     private Long id;
     private ReminderType type;
     private Location location;
     private Date reminderTime;
     private String text;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public ReminderType getType() {
         return type;
     }
 
     public void setType(ReminderType type) {
         this.type = type;
     }
 
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     public Date getReminderTime() {
         return reminderTime;
     }
 
     public void setReminderTime(Date reminderTime) {
         this.reminderTime = reminderTime;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Reminder reminder = (Reminder) o;
 
         if (!id.equals(reminder.id)) return false;
         if (location != null ? !location.equals(reminder.location) : reminder.location != null) return false;
         if (!reminderTime.equals(reminder.reminderTime)) return false;
         if (type != reminder.type) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id.hashCode();
         result = 31 * result + (type != null ? type.hashCode() : 0);
         result = 31 * result + (location != null ? location.hashCode() : 0);
         result = 31 * result + reminderTime.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         return "Reminder{" +
                 "id=" + id +
                 ", type=" + type +
                 ", location=" + location +
                 ", reminderTime=" + reminderTime +
                 ", reminderText=" + text +
                 '}';
     }
 }
