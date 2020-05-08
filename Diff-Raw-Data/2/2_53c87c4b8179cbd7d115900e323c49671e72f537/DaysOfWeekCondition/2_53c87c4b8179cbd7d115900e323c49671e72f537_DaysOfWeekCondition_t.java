 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.OrCondition;
 import com.sun.identity.entitlement.TimeCondition;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.faces.model.SelectItem;
 
 public class DaysOfWeekCondition
     extends BaseViewCondition
     implements Serializable {
 
    private String[] days = new String[] { "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
     private String[] selectedDays = new String[7];
 
     public EntitlementCondition getEntitlementCondition() {
         OrCondition oc = new OrCondition();
         Set<EntitlementCondition> orConditions = new HashSet<EntitlementCondition>();
         for (String day: selectedDays) {
             TimeCondition tc = new TimeCondition();
             tc.setStartDay(day);
             tc.setEndDay(day);
             orConditions.add(tc);
         }
         oc.setEConditions(orConditions);
 
         return oc;
     }
 
     public List<SelectItem> getDayItems() {
         List<SelectItem> items = new ArrayList<SelectItem>();
 
         for (String day: days) {
             // TODO, localize day name
             SelectItem si = new SelectItem(day);
             items.add(si);
         }
 
         return items;
     }
 
     public String[] getSelectedDays() {
         return selectedDays;
     }
 
     public void setSelectedDays(String[] selectedDays) {
         this.selectedDays = selectedDays;
     }
 }
