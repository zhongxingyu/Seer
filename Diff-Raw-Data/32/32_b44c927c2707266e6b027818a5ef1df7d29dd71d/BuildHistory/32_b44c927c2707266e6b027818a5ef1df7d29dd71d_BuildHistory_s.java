 /*******************************************************************************
  *
  * Copyright (c) 2004-2013 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *  Roy Varghese
  *
  *******************************************************************************/ 
 package hudson.model;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Retrieve the history of {@link Run} objects associated with a {@link Job}.
  * 
  * Getting history records is a more light-weight than iterating through
  * {@link Run} objects.
  * 
  * @author Roy Varghese
  */
 public interface BuildHistory<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> 
     extends BuildStatusInfo<JobT,RunT>, Iterable<BuildHistory.Record<JobT,RunT>> {
     
     
     Record<JobT,RunT> getFirst();
     Record<JobT,RunT> getLast();
     Record<JobT,RunT> getLastCompleted();
     Record<JobT,RunT> getLastFailed();
     Record<JobT,RunT> getLastStable();
     Record<JobT,RunT> getLastUnstable();
     Record<JobT,RunT> getLastSuccessful();
     Record<JobT,RunT> getLastUnsuccessful();
     List<Record<JobT,RunT>> getLastRecordsOverThreshold(int numberOfRecords, Result threshold);
     
     List<Record<JobT,RunT>> allRecords();
     
 
     /**
      * Summary of a single run or build.
      */        
     public interface Record<JobT extends Job<JobT, RunT>,RunT extends Run<JobT, RunT>> 
         extends BuildNavigator {
         int getNumber();
         
         JobT getParent();
         RunT getBuild();
         
         Result getResult();
         Run.State getState();
         
         long getTimeInMillis();
         Calendar getTimestamp();
         Date getTime();
         long getDuration();
         
         String getBuiltOnNodeName();
         
         String getDisplayName();
         String getDescription();
         String getTruncatedDescription();
         String getFullDisplayName();
         String getUrl();
         
         
         Record<JobT,RunT> getPrevious();
         Record<JobT,RunT> getNext();
         
         Record<JobT,RunT> getPreviousCompleted();
         Record<JobT,RunT> getPreviousInProgress();
         Record<JobT,RunT> getPreviousBuilt();
         Record<JobT,RunT> getPreviousNotFailed();
         Record<JobT,RunT> getPreviousFailed();
         Record<JobT,RunT> getPreviousSuccessful();
         
         List<Record<JobT,RunT>> getPreviousOverThreshold(int numberOfRecords, Result threshold);
         
         
     }
 }
