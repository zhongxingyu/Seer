 package BackEnd.EventSystem;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.lang.IllegalArgumentException;
 
 /**
  *
  * @author Ketty Lezama
  */
 
 public class TimeSchedule {
     private Calendar startDateTime, endDateTime;
     
     public TimeSchedule() {
         startDateTime = new GregorianCalendar();
         endDateTime = new GregorianCalendar();
     }
     
     public TimeSchedule(TimeSchedule timeSchedule){
         startDateTime = timeSchedule.getStartDateTimeCalendar();
         endDateTime = timeSchedule.getEndDateTimeCalendar();
     }
     
     public void setStartDateTime(int year, int month, int day, int hour, int minute) throws IllegalArgumentException {
         if (year >= 2013 && year <= 9999)
             startDateTime.set(Calendar.YEAR, year);
         else
             throw new IllegalArgumentException("Invalid year entered.");
         
         if (month >= 0 && month < 12)
             startDateTime.set(Calendar.MONTH, month - 1);
         else
             throw new IllegalArgumentException("Invalid numerical month entered.");
         
         if (day > 0 && day <= startDateTime.getMaximum(Calendar.DAY_OF_MONTH))
             startDateTime.set(Calendar.DAY_OF_MONTH, day);
         else
             throw new IllegalArgumentException("Invalid numerical day entered.");
         
         if (hour >= 0 && hour < 24)
             startDateTime.set(Calendar.HOUR_OF_DAY, hour);
         else
             throw new IllegalArgumentException("Invalid hour entered.");
         
         if (minute >= 0 && minute < 60)
             startDateTime.set(Calendar.MINUTE, minute);
         else
             throw new IllegalArgumentException("Invalid minute entered.");
     }
     
     public void setStartDateTime(Timestamp startDateTime){
         this.startDateTime.setTimeInMillis(startDateTime.getTime());
     }
     
     public Calendar getStartDateTimeCalendar() {
         return startDateTime;
     }
     
     public Timestamp getStartDateTimeTimestamp() {
         return new Timestamp(startDateTime.getTimeInMillis());
     }
     
     public void setEndDateTime(int year, int month, int day, int hour, int minute) throws IllegalArgumentException {
         if (year >= 2013 && year <= 9999)
             endDateTime.set(Calendar.YEAR, year);
         else
             throw new IllegalArgumentException("Invalid year entered.");
         
        if (month > 0 && month <= 12)
             endDateTime.set(Calendar.MONTH, month - 1);
         else
             throw new IllegalArgumentException("Invalid numerical month entered.");
         
         if (day > 0 && day <= endDateTime.getMaximum(Calendar.DAY_OF_MONTH))
             endDateTime.set(Calendar.DAY_OF_MONTH, day);
         else
             throw new IllegalArgumentException("Invalid numerical day entered.");
         
         if (hour >= 0 && hour < 24)
             endDateTime.set(Calendar.HOUR_OF_DAY, hour);
         else
             throw new IllegalArgumentException("Invalid hour entered.");
         
         if (minute >= 0 && minute < 60)
             endDateTime.set(Calendar.MINUTE, minute);
         else
             throw new IllegalArgumentException("Invalid minute entered.");
     }
     
     public void setEndDateTime(Timestamp endDateTime){
         this.endDateTime.setTimeInMillis(endDateTime.getTime());
     }
     
     public Calendar getEndDateTimeCalendar() {
         return endDateTime;
     }
     
     public Timestamp getEndDateTimeTimestamp() {
         return new Timestamp(endDateTime.getTimeInMillis());
     }
     
     public boolean equals(TimeSchedule timeSchedule) {
         if (this.getStartDateTimeCalendar().equals(timeSchedule.getStartDateTimeCalendar()) 
                 && this.getEndDateTimeCalendar().equals(timeSchedule.getEndDateTimeCalendar()))
             return true;
         else
             return false;
     }
     
     public String toString() {
         return "Start Date & Time: " + startDateTime.get(Calendar.MONTH) + "/" + startDateTime.get(Calendar.DAY_OF_MONTH) +
                 "/" + startDateTime.get(Calendar.YEAR) + " " + String.format("%02d", startDateTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                 String.format("%02d", startDateTime.get(Calendar.MINUTE)) + "\nEnd Date & Time: " + endDateTime.get(Calendar.MONTH) +
                 "/" + endDateTime.get(Calendar.DAY_OF_MONTH) + "/" + endDateTime.get(Calendar.YEAR) + " " + 
                 String.format("%02d", endDateTime.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", endDateTime.get(Calendar.MINUTE));
     }
 }
