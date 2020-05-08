 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.Resources;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.TimeCondition;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 import javax.faces.model.SelectItem;
 
 public class TimezoneCondition
     extends ViewCondition
     implements Serializable {
 
     private String timezoneId;
     private List<String> timezoneIds;
 
     public EntitlementCondition getEntitlementCondition() {
         TimeCondition tc = new TimeCondition();
         tc.setEnforcementTimeZone(timezoneId);
 
         return tc;
     }
 
     public List<SelectItem> getTimezoneIdItems() {
         List<SelectItem> items = new ArrayList<SelectItem>();
 
         for (String id: getTimezoneIds()) {
             TimeZone tz = TimeZone.getTimeZone(id);
             SelectItem si = new SelectItem();
             si.setValue(id);
            si.setLabel(tz.getDisplayName() + " (" + tz.getID() + ")");
 
             items.add(si);
         }
 
         return items;
     }
 
     public List<String> getTimezoneIds() {
         if (timezoneIds == null || timezoneIds.size() == 0) {
             String[] timezoneIdArray = TimeZone.getAvailableIDs();
             Arrays.sort(timezoneIdArray);
             timezoneIds = Arrays.asList(timezoneIdArray);
         }
         return timezoneIds;
     }
 
     public void setTimezoneIds(List<String> timezoneIds) {
         this.timezoneIds = timezoneIds;
     }
 
     public String getTimezoneId() {
         if (timezoneId == null) {
             timezoneId = TimeZone.getDefault().getID();
         }
         return timezoneId;
     }
 
     public void setTimezoneId(String timezoneId) {
         this.timezoneId = timezoneId;
     }
 
     @Override
     public String toString() {
         Locale l = new Resources().getLocale();
         String s = getTitle();
         if (timezoneId != null) {
            s += ":{" + TimeZone.getTimeZone(timezoneId).getDisplayName(l) + "}";
         }
 
         return s;
     }
 }
