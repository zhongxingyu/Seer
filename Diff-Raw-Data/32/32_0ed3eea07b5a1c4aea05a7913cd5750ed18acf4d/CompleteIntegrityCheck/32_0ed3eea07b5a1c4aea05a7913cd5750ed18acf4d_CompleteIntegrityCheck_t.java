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
 
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.integrityservice.workflow.step.CreateStatisticsEntryStep;
 import org.bitrepository.integrityservice.workflow.step.FindMissingChecksumsStep;
 import org.bitrepository.integrityservice.workflow.step.FindObsoleteChecksumsStep;
 import org.bitrepository.integrityservice.workflow.step.IntegrityValidationChecksumStep;
 import org.bitrepository.integrityservice.workflow.step.IntegrityValidationFileIDsStep;
 import org.bitrepository.integrityservice.workflow.step.RemoveDeletableFileIDsFromDatabaseStep;
 import org.bitrepository.integrityservice.workflow.step.UpdateChecksumsStep;
 import org.bitrepository.integrityservice.workflow.step.UpdateFileIDsStep;
 import org.bitrepository.service.workflow.Workflow;
 import org.bitrepository.service.workflow.WorkflowContext;
 import org.bitrepository.service.workflow.JobID;
 
 /**
  * Simple workflow for performing integrity checks of the system. 
  * Starts by updating the file ids in the integrity model, followed by updating the checksums in the integrity model.
  * Then the data is validated for integrity issues.
  * And finally it is verified whether any missing or obsolete checksums can be found.
  */
 public class CompleteIntegrityCheck extends Workflow {
     /** The context for the workflow.*/
     private IntegrityWorkflowContext context;
     /** The jobID */
     private JobID jobID;
     private String collectionID;
     /**
      * Remember to call the initialise method needs to be called before the start method.
      */
     public CompleteIntegrityCheck() {}
 
     @Override
     public void initialise(WorkflowContext context, String collectionID) {
         this.context = (IntegrityWorkflowContext)context;
         this.collectionID = collectionID;
        jobID = new JobID(getClass().getSimpleName(), collectionID);
     }
     
     @Override
     public void start() {
         if (context == null) {
             throw new IllegalStateException("The workflow can not be started before the initialise method has been " +
                     "called.");
         }
         super.start();
         try {
             UpdateFileIDsStep updateFileIDsStep = new UpdateFileIDsStep(context.getCollector(), context.getStore(),
                     context.getAlerter(), context.getSettings(), collectionID);
             performStep(updateFileIDsStep);
             
             UpdateChecksumsStep updateChecksumStep = new UpdateChecksumsStep(
                     context.getCollector(), context.getStore(), context.getAlerter(),
                     ChecksumUtils.getDefault(context.getSettings()), context.getSettings(), collectionID);
             performStep(updateChecksumStep);
 
             IntegrityValidationFileIDsStep validateFileidsStep = new IntegrityValidationFileIDsStep(context.getChecker(),
                     context.getAlerter(), collectionID);
             performStep(validateFileidsStep);
             
             RemoveDeletableFileIDsFromDatabaseStep removeDeletableFileIDsFromDatabaseStep 
                     = new RemoveDeletableFileIDsFromDatabaseStep(context.getStore(), validateFileidsStep.getReport(),
                             context.getAuditManager(), context.getSettings());
             performStep(removeDeletableFileIDsFromDatabaseStep);
             
             IntegrityValidationChecksumStep validateChecksumStep = new IntegrityValidationChecksumStep(
                     context.getChecker(), context.getAlerter(), collectionID);
             performStep(validateChecksumStep);
             
             FindMissingChecksumsStep findMissingChecksums = new FindMissingChecksumsStep(
                     context.getChecker(), context.getAlerter(), collectionID);
             performStep(findMissingChecksums);
             
             FindObsoleteChecksumsStep findObsoleteChecksums  = new FindObsoleteChecksumsStep(
                     context.getSettings(), context.getChecker(), context.getAlerter(), collectionID);
             performStep(findObsoleteChecksums);
             CreateStatisticsEntryStep createStatistics = new CreateStatisticsEntryStep(
                     context.getStore(), collectionID);
             performStep(createStatistics);
         } finally {
             finish();
         }
     }
 
     @Override
     public String getDescription() {
         return "Retrieves all fileIDs and checksums from all pillars and checks for all potential integrity " +
                 "problems."
                 //+ LINEFEED + getStepDescriptions()
                 ;
     }
     
     @Override 
     public JobID getJobID() {
         return jobID;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof CompleteIntegrityCheck)) return false;
 
         CompleteIntegrityCheck that = (CompleteIntegrityCheck) o;
 
         if (!jobID.equals(that.jobID)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return jobID.hashCode();
     }
 }
