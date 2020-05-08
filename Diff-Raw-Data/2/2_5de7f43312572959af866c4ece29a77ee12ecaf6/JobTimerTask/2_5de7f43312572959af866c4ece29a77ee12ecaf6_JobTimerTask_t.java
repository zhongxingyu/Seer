 /*
  * #%L
  * Bitrepository Core
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.service.workflow;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Date;
 import java.util.TimerTask;
 
 /**
  * A timer task encapsulating a workflow.
  * Used for scheduling workflows to run continuously at a given interval.
  */
 public class JobTimerTask extends TimerTask {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The date for the next run of the workflow.*/
     private Date nextRun;
     /** The interval between triggers. */
     private final long interval;
     private final SchedulableJob workflow;
     private WorkflowStatistic lastWorkflowStatistics;
 
     /**
      * Initialise trigger.
      * @param interval The interval between triggering events in milliseconds.
      * @param workflow:  The  workflow.
      */
     public JobTimerTask(long interval, SchedulableJob workflow) {
         this.interval = interval;
         this.workflow = workflow;
         nextRun = new Date();
         lastWorkflowStatistics = new WorkflowStatistic("Not run yet");
 
     }
 
     /**
      * @return The date for the next time the encapsulated workflow should run.
      */
     public Date getNextRun() {
         return new Date(nextRun.getTime());
     }
 
     /**
      * @return The statistics object with information on the time statistics for the last run. May be null if
      * the workflow hasn't been run yet.
      */
     public WorkflowStatistic getLastRunStatistics() {
         return lastWorkflowStatistics;
     }
 
     /**
      * @return The statistics object with information on the time statistics for the current run.
      * May be null if the workflow hasn't been run yet.
      */
     public WorkflowStatistic getCurrentRunStatistics() {
         return workflow.getWorkflowStatistics();
     }
 
     /**
      * @return The interval between the runs in millis.
      */
     public long getIntervalBetweenRuns() {
         return interval;
     }
 
     public String getDescription() {
         return workflow.getDescription();
     }
 
     /**
      * Trigger the workflow.
      * Resets the date for the next run of the workflow.
      * @return String
      */
     public String runWorkflow() {
         try {
             if (workflow.currentState().equals(Workflow.NOT_RUNNING)) {
                 log.info("Starting the workflow: " + getName());
                 workflow.start();
                if (interval > 0) {
                     nextRun = new Date(System.currentTimeMillis() + interval);
                 }
                 lastWorkflowStatistics = workflow.getWorkflowStatistics();
                 return "Workflow '" + workflow.getClass().getSimpleName() + "' finished";
 
             } else {
                 log.info("Ignoring start request for " + getName() + " the workflow is already running");
                 return "Can not start " +getName() + ", it is already running in state " + workflow.currentState();
             }
         } catch (Throwable e) {
             log.error("Fault barrier for '" + getName() + "' caught unexpected exception.", e);
             throw new RuntimeException("Failed to run workflow" + e.getMessage() + ", see server log for details.");
         }
     }
 
     /**
      * @return The name of the workflow.
      */
     public String getName() {
         return workflow.getJobID().toString();
     }
     
     public JobID getWorkflowID() {
         return workflow.getJobID();
     }
 
     @Override
     public void run() {
         try {
             if( nextRun != null &&
                 getNextRun().getTime() <= System.currentTimeMillis()) {
                 runWorkflow();
             }
         } catch (Exception e) {
             log.error("Failed to run workflow", e);
         }
     }
 }
