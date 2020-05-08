 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.TimeCondition;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class DateRangeConditionType 
     extends TimeConditionType
     implements Serializable {
 
     private static class Date {
         int year;
         int month;
         int day;
     }
 
     public ViewCondition newViewCondition() {
         ViewCondition vc = new DateRangeCondition();
         vc.setConditionType(this);
 
         return vc;
     }
 
     public ViewCondition newViewCondition(TimeCondition tc) {
         DateRangeCondition drc = (DateRangeCondition)newViewCondition();
 
         Date startDate = parseDate(tc.getStartDate());
         Date endDate = parseDate(tc.getStartDate());
 
         Calendar startCal = new GregorianCalendar(startDate.year, startDate.month, startDate.day);
         Calendar endCal = new GregorianCalendar(endDate.year, endDate.month, endDate.day);
 
         drc.setStartDate(startCal.getTime());
         drc.setEndDate(endCal.getTime());
         
         return drc;
     }
 
     private Date parseDate(String dateString) {
         assert(dateString != null);
        dateString = dateString.substring(1, dateString.length()-1);
         Date d = new Date();
 
         String[] dateArray = dateString.split("-");
         assert(dateArray.length == 3);
 
         d.year = Integer.valueOf(dateArray[0]);
         d.month = Integer.valueOf(dateArray[1]);
         d.day = Integer.valueOf(dateArray[2]);
 
         return d;
     }
 }
