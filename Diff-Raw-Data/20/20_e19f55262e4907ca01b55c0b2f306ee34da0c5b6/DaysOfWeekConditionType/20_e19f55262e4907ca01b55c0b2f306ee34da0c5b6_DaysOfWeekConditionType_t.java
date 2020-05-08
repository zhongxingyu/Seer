 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.CircularArrayList;
 import com.sun.identity.entitlement.TimeCondition;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class DaysOfWeekConditionType
     extends TimeConditionType
     implements Serializable {
     public ViewCondition newViewCondition() {
         ViewCondition vc = new DaysOfWeekCondition();
         vc.setConditionType(this);
 
         return vc;
     }
 
     public ViewCondition newViewCondition(TimeCondition tc) {
         assert(tc.getStartDay() != null);
         assert(tc.getEndDay() != null);
 
         DaysOfWeekCondition dowc = (DaysOfWeekCondition)newViewCondition();
 
         List<String> days = new CircularArrayList(Arrays.asList(DaysOfWeekCondition.DAYS));
         List<String> selectedDays = new ArrayList<String>();
 
         String startDay = tc.getStartDay();
         String endDay = tc.getEndDay();
         endDay = endDay.substring(1, endDay.length()-1);
 
         for (int i = days.indexOf(startDay); days.get(i).equals(endDay); i++) {
             selectedDays.add(days.get(i));
         }
         dowc.setSelectedDays(selectedDays.toArray(new String[0]));
         
         return dowc;
     }
 
 }
