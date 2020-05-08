 package org.bitrepository.pillar.referencepillar.scheduler;
 
 import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
 import org.bitrepository.service.workflow.JobID;
 import org.bitrepository.service.workflow.SchedulableJob;
 import org.bitrepository.service.workflow.WorkflowContext;
 import org.bitrepository.service.workflow.WorkflowStatistic;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Workflow for recalculating the checksums of the files of a given collection.
  */
 public class RecalculateChecksumWorkflow implements SchedulableJob {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The id of the collection to recalculate the checksum for.*/
     private final String collectionID;
     /** The manager of the checksum and reference archive.*/
     private final ReferenceChecksumManager manager;
     
     /** The state of this workflow. */
     private String workflowState;
     private final JobID id;
     
     /**
      * Constructor.
      * @param collectionID The id of the collection to recalculate checksum for.
      * @param manager The manager of the checksums and reference archive.
      */
     public RecalculateChecksumWorkflow(String collectionID, ReferenceChecksumManager manager) {
         this.collectionID = collectionID;
         this.manager = manager;
         id = new JobID(collectionID, getClass().getSimpleName());
         workflowState = "Has not yet run.";
     }
     
     @Override
     public void start() {
         log.info("Recalculating old checksums.");
         workflowState = "Running";
         manager.ensureStateOfAllData(collectionID);
         workflowState = "Currently not running";
     }
 
     @Override
     public String currentState() {
         return workflowState;
     }
 
     @Override
     public String getDescription() {
         return "Recalculates the checksums for collection: '" + collectionID + "'.";
     }
 
     @Override
     public WorkflowStatistic getWorkflowStatistics() {
         return null;
     }
 
     @Override
     public JobID getJobID() {
        return id;
     }
 
     @Override
     public void initialise(WorkflowContext context, String collectionID) {
         //Not used as reference pillar workflows are defined compile time.
     }
 }
