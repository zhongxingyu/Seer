 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.TimeCondition;
 import java.io.Serializable;
 
 public class TimeRangeConditionType 
     extends TimeConditionType
     implements Serializable {
 
     private static class Time {
         int hour;
         int min;
         String period;
     }
 
     public ViewCondition newViewCondition() {
         ViewCondition vc = new TimeRangeCondition();
         vc.setConditionType(this);
 
         return vc;
     }
 
     public ViewCondition newViewCondition(TimeCondition tc) {
         TimeRangeCondition trc = (TimeRangeCondition)newViewCondition();
         Time startTime = parseTime(tc.getStartTime());
         Time endTime = parseTime(tc.getEndTime());
 
         trc.setStartHour(startTime.hour);
         trc.setStartMinute(startTime.min);
         trc.setStartPeriod(startTime.period);
         trc.setEndHour(endTime.hour);
         trc.setEndMinute(endTime.min);
         trc.setEndPeriod(endTime.period);
 
         return trc;
     }
 
     private Time parseTime(String timeString) {
         assert(timeString != null);
        timeString = timeString.substring(1, timeString.length()-1);

         Time t = new Time();
 
         String[] timeArray = timeString.split(":");
         assert(timeArray.length == 2);
 
         if (t.hour < 12) {
             t.hour = Integer.valueOf(timeArray[0]);
             t.period = "AM";
         } else {
             t.hour = Integer.valueOf(timeArray[0])-12;
             t.period = "PM";
         }
         t.min = Integer.valueOf(timeArray[1]);
 
         return t;
     }
 }
