 /*
  * #%L
  * Bitrepository Integrity Service
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
 
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.service.scheduler.JobScheduler;
 import org.bitrepository.settings.referencesettings.Schedule;
 import org.bitrepository.settings.referencesettings.WorkflowConfiguration;
 import org.bitrepository.settings.referencesettings.WorkflowSettings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public abstract class WorkflowManager {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     private final JobScheduler scheduler;
     private final WorkflowContext context;
     private final Map<JobID, SchedulableJob> workflows = new HashMap<JobID, SchedulableJob>();
 
     public WorkflowManager(
             WorkflowContext context,
             WorkflowSettings configuration,
             JobScheduler scheduler) {
         this.context = context;
         this.scheduler = scheduler;
         loadWorkFlows(configuration);
     }
 
     public String startWorkflow(JobID jobID) {
         SchedulableJob workflowToStart = workflows.get(jobID);
        if (workflowToStart != null) {
             throw new IllegalArgumentException("Unknown workflow" + jobID);
         }
         return scheduler.startJob(workflowToStart);
     }
 
     public Collection<JobTimerTask> getWorkflows(String collectionID) {
         return scheduler.getJobs(collectionID);
     }
 
     private void loadWorkFlows(WorkflowSettings configuration) {
         for (WorkflowConfiguration workflowConf:configuration.getWorkflow()) {
             log.info("Scheduling from configuration: " + workflowConf);
             List<String> unscheduledWorkFlows = new LinkedList<String>(SettingsUtils.getAllCollectionsIDs());
             try {
                 if (workflowConf.getSchedules() != null) {
                     for (Schedule schedule:workflowConf.getSchedules().getSchedule()) {
                         List<String> collectionsToScheduleWorkflowFor;
                         if (schedule.isSetCollections()) {
                             collectionsToScheduleWorkflowFor = schedule.getCollections().getCollectionID();
                         } else {
                             collectionsToScheduleWorkflowFor = SettingsUtils.getAllCollectionsIDs();
                         }
                         for (String collectionID:collectionsToScheduleWorkflowFor) {
                             Workflow workflow =
                                     (Workflow)lookupClass(workflowConf.getWorkflowClass()).newInstance();
                             workflow.initialise(context, collectionID);
                             scheduler.schedule(workflow, schedule.getWorkflowInterval());
                             workflows.put(workflow.getJobID(), workflow);
                             unscheduledWorkFlows.remove(collectionID);
                         }
                     }
                 }
                 // Create a instance of all workflows not explicitly scheduled.
                 for (String collection:unscheduledWorkFlows) {
                     SchedulableJob workflow =
                             (SchedulableJob)Class.forName(workflowConf.getWorkflowClass()).newInstance();
                     workflow.initialise(context, collection);
                     workflows.put(workflow.getJobID(), workflow);
                     scheduler.schedule(workflow, null);
                 }
             } catch (Exception e) {
                 log.error("Unable to load workflow " + workflowConf.getWorkflowClass(), e);
             }
         }
     }
 
     private Class lookupClass(String settingsDefinedClass) throws ClassNotFoundException {
         String fullClassName;
         if (settingsDefinedClass.indexOf('.') == -1) {
             fullClassName = getDefaultWorkflowPackage() + "." + settingsDefinedClass;
         } else {
             fullClassName = settingsDefinedClass;
         }
         return Class.forName(fullClassName);
     }
 
     /**
      * Allows subclasses to define a workflow package where workflow classes defined with a simplename in the settings
      * will be prefixed with the namespace defined here.
      * @return
      */
     protected abstract String getDefaultWorkflowPackage();
 }
