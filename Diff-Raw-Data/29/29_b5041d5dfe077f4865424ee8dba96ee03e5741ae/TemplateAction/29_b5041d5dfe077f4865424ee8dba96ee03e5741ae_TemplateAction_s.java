 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 public enum TemplateAction {
    PREVIEW_SCHEDULE,
     RELEASE_REVISION;
 
     public String getToken() {
         return toString();
     }
 }
