 package org.motechproject.ghana.national.domain;
 
 public enum RegistrationToday {
    TODAY("Today"), IN_PAST("In the Past"), IN_PAST_IN_OTHER_FACILITY("In the past in another facility");
     private String description;
 
     private RegistrationToday(String description) {
         this.description = description;
     }
 
     public String getDescription() {
         return description;
     }
 }
