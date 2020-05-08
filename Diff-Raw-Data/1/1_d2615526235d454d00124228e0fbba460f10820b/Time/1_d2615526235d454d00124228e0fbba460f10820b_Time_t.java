 package cofoja;
 
 import com.google.java.contract.Requires;
 
 public class Time {
     private int hour;
     private int minute;
 
     // This only serves to illustrate a Cofoja annotation
     @Requires("hour >=0 && hour <= 23")
     public Time(int hour, int minute) {
         this.hour = hour;
         this.minute = minute;
     }
 
 }
