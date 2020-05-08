 package com.technophobia.substeps.junit.ui;
 
 public interface TestRunStats {
 
     public enum TestRunState {
         STOPPED, //
        COMPLETE, //
         IN_PROGRESS
     }
 
 
     int getTotalCount();
 
 
     int getStartedCount();
 
 
     int getIgnoredCount();
 
 
     int getErrorCount();
 
 
     int getFailureCount();
 
 
     TestRunState getState();
 
 
     boolean hasErrorsOrFailures();
 }
