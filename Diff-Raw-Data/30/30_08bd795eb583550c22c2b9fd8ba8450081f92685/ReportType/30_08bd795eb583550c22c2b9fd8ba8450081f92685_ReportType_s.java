 package com.freeroom.projectci.beans;
 
 public enum ReportType {
    UserStory(432), FunctionalTesting(71), Document(10),
    PerformanceTesting(20), IntegrationTesting(10),
    OverTime(999), BugFixing(60), Leave(120), Others(999);
 
     private long estimatedEffort;
 
     ReportType(long estimatedDays) {
         this.estimatedEffort = estimatedDays * 8;
     }
 
     public long getEstimatedEffort() {
         return estimatedEffort;
     }
 }
