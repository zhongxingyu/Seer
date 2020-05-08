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
 
 package org.bitrepository.integrityservice.workflow;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.service.scheduler.TimerbasedScheduler;
 import org.bitrepository.settings.referencesettings.Collections;
 import org.bitrepository.settings.referencesettings.Schedule;
 import org.bitrepository.settings.referencesettings.Schedules;
 import org.bitrepository.settings.referencesettings.WorkflowConfiguration;
 import org.bitrepository.settings.referencesettings.WorkflowSettings;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 
 public class IntegrityWorkflowManagerTest extends ExtendedTestCase {
     private Settings settings;
     private WorkflowSettings workflowSettings;
     private TimerbasedScheduler scheduler;
     private String collection1ID, collection2ID;
     private TestWorkflow workflow1, workflow2;
 
 
     @BeforeMethod(alwaysRun = true)
     public void setup() {
         scheduler = mock(TimerbasedScheduler.class);
         settings = TestSettingsProvider.reloadSettings(this.getClass().getSimpleName());
         workflowSettings = new WorkflowSettings();
         WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration();
         workflowConfiguration.setWorkflowClass("org.bitrepository.integrityservice.workflow.TestWorkflow");
         Schedule schedule = new Schedule();
         schedule.setWorkflowInterval(IntegrityWorkflowManager.DAILY);
         workflowConfiguration.setSchedules(new Schedules());
         workflowConfiguration.getSchedules().getSchedule().add(schedule);
         workflowSettings.getWorkflow().add(workflowConfiguration);
         settings.getReferenceSettings().getIntegrityServiceSettings().setWorkflows(workflowSettings);
         SettingsUtils.initialize(settings);
         collection1ID = SettingsUtils.getAllCollectionsIDs().get(0);
         collection2ID = SettingsUtils.getAllCollectionsIDs().get(1);
 
         workflow1 = new TestWorkflow(collection1ID);
         workflow2 = new TestWorkflow(collection2ID);
     }
 
     @Test (groups = {"regressiontest"})
     public void normalWorkflowSettings() {
         addDescription("Verifies that the IntegrityWorkflowManager loads correctly for at normally defined workflow.");
 
         addStep("Create a IntegrityWorkflowManager based on a single Testworkflow with a daily schedule in a to " +
                 "collection system",
                 "Two Test workflows should be scheduled daily, one for each collection");
 
         new IntegrityWorkflowManager(new IntegrityWorkflowContext(settings, null, null, null, null, null), scheduler);
         verify(scheduler).schedule(eq(workflow1), eq(IntegrityWorkflowManager.DAILY));
         verify(scheduler).schedule(eq(workflow2), eq(IntegrityWorkflowManager.DAILY));
         verifyNoMoreInteractions(scheduler);
     }
 
     @Test (groups = {"regressiontest"})
     public void noWorkflowPackage() {
         addDescription("Verifies that the IntegrityWorkflowManager loads correctly for at workflow configuration with " +
                 "a workflow class name without a package scope (located in the deafult workflow package).");
 
         addStep("Create a IntegrityWorkflowManager based on a single Testworkflow with a daily schedule in a to " +
                 "collection system, where the className is just the simplename",
                 "Two Test workflows should be scheduled daily, one for each collection");
         workflowSettings.getWorkflow().get(0).setWorkflowClass("TestWorkflow");
 
         new IntegrityWorkflowManager(new IntegrityWorkflowContext(settings, null, null, null, null, null), scheduler);
         verify(scheduler).schedule(eq(workflow1), eq(IntegrityWorkflowManager.DAILY));
         verify(scheduler).schedule(eq(workflow2), eq(IntegrityWorkflowManager.DAILY));
         verifyNoMoreInteractions(scheduler);
     }
 
     @Test (groups = {"regressiontest"})
     public void noWorkflowSettings() {
         addDescription("Verifies that the IntegrityWorkflowManager loads correctly for missing reference settings a " +
                 "workflow settings element.");
 
         addStep("Create a IntegrityWorkflowManager based on a workflowsettingsless configuration",
                 "A default configuration should be used, which is:" +
                         "Two CompleteIntegrityCheck workflows should be scheduled daily, one for each collection");
         settings.getReferenceSettings().getIntegrityServiceSettings().setWorkflows(null);
         CompleteIntegrityCheck defaultWorkflow1 = new CompleteIntegrityCheck();
         defaultWorkflow1.initialise(null, collection1ID);
         CompleteIntegrityCheck defaultWorkflow2 = new CompleteIntegrityCheck();
         defaultWorkflow2.initialise(null, collection2ID);
 
 
         new IntegrityWorkflowManager(new IntegrityWorkflowContext(settings, null, null, null, null, null), scheduler);
         verify(scheduler).schedule(eq(defaultWorkflow1), eq(IntegrityWorkflowManager.DAILY));
         verify(scheduler).schedule(eq(defaultWorkflow2), eq(IntegrityWorkflowManager.DAILY));
         verifyNoMoreInteractions(scheduler);
     }
 
     @Test (groups = {"regressiontest"})
     public void collectionSpecificWorkflows() {
         addDescription("Verifies that the IntegrityWorkflowManager loads correctly for workflows configured for " +
                 "specific collection.");
 
         addStep("Create a IntegrityWorkflowManager based on a workflow with different schedules for collection 1 and " +
                 "2 (daily and hourly)",
                 "Two workflows should be scheduled, one daily and one hourly");
         WorkflowConfiguration workflowConfiguration =
                 settings.getReferenceSettings().getIntegrityServiceSettings().getWorkflows().getWorkflow().get(0);
         workflowConfiguration.getSchedules().getSchedule().clear();
         Schedule schedule1 = new Schedule();
         schedule1.setWorkflowInterval(IntegrityWorkflowManager.DAILY);
         schedule1.setCollections(new Collections());
         schedule1.getCollections().getCollectionID().add(collection1ID);
         workflowConfiguration.getSchedules().getSchedule().add(schedule1);
         Schedule schedule2 = new Schedule();
         schedule2.setWorkflowInterval(IntegrityWorkflowManager.HOURLY);
         schedule2.setCollections(new Collections());
         schedule2.getCollections().getCollectionID().add(collection2ID);
         workflowConfiguration.getSchedules().getSchedule().add(schedule2);
 
         new IntegrityWorkflowManager(new IntegrityWorkflowContext(settings, null, null, null, null, null), scheduler);
         verify(scheduler).schedule(eq(workflow1), eq(IntegrityWorkflowManager.DAILY));
         verify(scheduler).schedule(eq(workflow2), eq(IntegrityWorkflowManager.HOURLY));
         verifyNoMoreInteractions(scheduler);
     }
 
     @Test (groups = {"regressiontest"})
     public void unscheduledWorkflow() {
         addDescription("Verifies that the IntegrityWorkflowManager loads workflow correctly for workflows without a " +
                 "defined schedule meaning they are never run automatically.");
 
         addStep("Create a IntegrityWorkflowManager based on a single Testworkflow without a schedule",
                 "Two Test workflows should be create with a -1 interval between runs.");
         workflowSettings.getWorkflow().get(0).setSchedules(null);
 
         new IntegrityWorkflowManager(new IntegrityWorkflowContext(settings, null, null, null, null, null), scheduler);
         verify(scheduler).schedule(eq(workflow1), eq((Long)null));
         verify(scheduler).schedule(eq(workflow2), eq((Long)null));
         verifyNoMoreInteractions(scheduler);
     }
 
 }
