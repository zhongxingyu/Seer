 /*
  * #%L
  * Bitrepository Integrity Client
  * 
  * $Id$
  * $HeadURL$
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
 package org.bitrepository.integrityservice.web;
 
 import org.bitrepository.common.utils.FileSizeUtils;
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.common.utils.TimeUtils;
 import org.bitrepository.integrityservice.IntegrityServiceManager;
 import org.bitrepository.integrityservice.cache.IntegrityModel;
 import org.bitrepository.service.workflow.JobID;
 import org.bitrepository.service.workflow.JobTimerTask;
 import org.bitrepository.service.workflow.WorkflowManager;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 @Path("/IntegrityService")
 public class RestIntegrityService {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private IntegrityModel model;
     private WorkflowManager workflowManager;
 
     public RestIntegrityService() {
         this.model = IntegrityServiceManager.getIntegrityModel();
         this.workflowManager = IntegrityServiceManager.getWorkflowManager();
     }
 
     /**
      * Method to get the checksum errors per pillar in a given collection. 
      * @param collectionID, the collectionID from which to return checksum errors
      * @param pillarID, the ID of the pillar in the collection from which to return checksum errors
      * @param pageNumber, the page number for calculating offsets (@see pageSize)
      * @param pageSize, the number of checksum errors per page. 
      */
     @GET
     @Path("/getChecksumErrorFileIDs/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getChecksumErrors(
             @QueryParam("collectionID") String collectionID,
             @QueryParam("pillarID") String pillarID,
             @QueryParam("pageNumber") int pageNumber,
             @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
         
         int firstID = (pageNumber - 1) * pageSize;
         int lastID = (pageNumber * pageSize) - 1;
         
         List<String> ids = model.getFilesWithChecksumErrorsAtPillar(pillarID, firstID, lastID, collectionID);
         return ids;
     }
 
     /**
      * Method to get the list of missing files per pillar in a given collection. 
      * @param collectionID, the collectionID from which to return missing files
      * @param pillarID, the ID of the pillar in the collection from which to return missing files
      * @param pageNumber, the page number for calculating offsets (@see pageSize)
      * @param pageSize, the number of checksum errors per page. 
      */
     @GET
     @Path("/getMissingFileIDs/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getMissingFileIDs(
             @QueryParam("collectionID") String collectionID,
             @QueryParam("pillarID") String pillarID,
             @QueryParam("pageNumber") int pageNumber,
             @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
         
         int firstID = (pageNumber - 1) * pageSize;
         int lastID = (pageNumber * pageSize) - 1;
         
         List<String> ids = model.getMissingFilesAtPillar(pillarID, firstID, lastID, collectionID);
         return ids;
     }
     
     /**
      * Method to get the list of present files on a pillar in a given collection. 
      * @param collectionID, the collectionID from which to return present file list
      * @param pillarID, the ID of the pillar in the collection from which to return present file list
      * @param pageNumber, the page number for calculating offsets (@see pageSize)
      * @param pageSize, the number of checksum errors per page. 
      */
     @GET
     @Path("/getAllFileIDs/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getAllFileIDs(
             @QueryParam("collectionID") String collectionID,
             @QueryParam("pillarID") String pillarID,
             @QueryParam("pageNumber") int pageNumber,
             @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
         
         int firstID = (pageNumber - 1) * pageSize;
         int lastID = (pageNumber * pageSize) - 1;
         
         List<String> ids = model.getFilesOnPillar(pillarID, firstID, lastID, collectionID);
         return ids;
     }
 
     /**
      * Get the listing of integrity status as a JSON array
      */
     @GET
     @Path("/getIntegrityStatus/")
     @Produces(MediaType.APPLICATION_JSON)
     public String getIntegrityStatus(@QueryParam("collectionID") String collectionID) {
         JSONArray array = new JSONArray();
         for(String pillar : SettingsUtils.getPillarIDsForCollection(collectionID)) {
             array.put(makeIntegrityStatusObj(pillar, collectionID));
         }
         return array.toString();
     }
 
     /***
      * Get the current workflows setup as a JSON array 
      */
     @GET
     @Path("/getWorkflowSetup/")
     @Produces(MediaType.APPLICATION_JSON)
     public String getWorkflowSetup(@QueryParam("collectionID") String collectionID) {
         try {
             JSONArray array = new JSONArray();
             Collection<JobTimerTask> workflows = workflowManager.getWorkflows(collectionID);
             for(JobTimerTask workflow : workflows) {
                 array.put(makeWorkflowSetupObj(workflow));
             }
             return array.toString();
         } catch (RuntimeException e) {
             log.error("Failed to getWorkflowSetup ", e);
             throw e;
         }
     }
 
     /**
      * Get the list of possible workflows as a JSON array 
      */
     @GET
     @Path("/getWorkflowList/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getWorkflowList(@QueryParam("collectionID") String collectionID) {
         Collection<JobTimerTask> workflows = workflowManager.getWorkflows(collectionID);
         List<String> workflowIDs = new ArrayList<String>();
         for(JobTimerTask workflow : workflows) {
             workflowIDs.add(workflow.getWorkflowID().getWorkflowName());
         }
         return workflowIDs;
     }
 
     /**
      * Start a named workflow.  
      */
     @POST
    @Path("/startJob/")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("text/html")
     public String startWorkflow(@FormParam("workflowID") String workflowID,
                                 @FormParam("collectionID") String collectionID) {
         return workflowManager.startWorkflow(new JobID(workflowID, collectionID));
     }
 
     /**
      * Start a named workflow.  
      */
     @GET
     @Path("/getCollectionInformation/")
     @Produces(MediaType.APPLICATION_JSON)
     public String getCollectionInformation(@QueryParam("collectionID") String collectionID) {
         JSONObject obj = new JSONObject();
         Date lastIngest = model.getDateForNewestFileEntryForCollection(collectionID);
         Long collectionDataSize = model.getCollectionFileSize(collectionID);
         Long numberOfFiles = model.getNumberOfFilesInCollection(collectionID);
         String lastIngestStr = lastIngest == null ? "No files ingested yet" : TimeUtils.shortDate(lastIngest);
         try {
             obj.put("lastIngest", lastIngestStr);
             obj.put("collectionSize", FileSizeUtils.toHumanShort(collectionDataSize == null ? 0 : collectionDataSize));
             obj.put("numberOfFiles", numberOfFiles == null ? 0 : numberOfFiles);
         } catch (JSONException e) {
             obj = (JSONObject) JSONObject.NULL;
         }
         return obj.toString();
     }
  
     private JSONObject makeIntegrityStatusObj(String pillarID, String collectionID) {
         JSONObject obj = new JSONObject();
         try {
             obj.put("pillarID", pillarID);
             obj.put("totalFileCount", model.getNumberOfFiles(pillarID, collectionID));
             obj.put("missingFilesCount", model.getNumberOfMissingFiles(pillarID, collectionID));
             obj.put("checksumErrorCount", model.getNumberOfChecksumErrors(pillarID, collectionID));
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
 
     private JSONObject makeWorkflowSetupObj(JobTimerTask workflowTask) {
         JSONObject obj = new JSONObject();
         try {
             obj.put("workflowID", workflowTask.getWorkflowID().getWorkflowName());
             obj.put("workflowDescription", workflowTask.getDescription());
             obj.put("nextRun", TimeUtils.shortDate(workflowTask.getNextRun()));
             if (workflowTask.getLastRunStatistics().getFinish() == null) {
                 obj.put("lastRun", "Workflow hasn't finished a run yet");
             } else {
                 obj.put("lastRun", TimeUtils.shortDate(workflowTask.getLastRunStatistics().getFinish()));
             }
             obj.put("lastRunDetails", workflowTask.getLastRunStatistics().getFullStatistics());
             obj.put("executionInterval", TimeUtils.millisecondsToHuman(workflowTask.getIntervalBetweenRuns()));
             obj.put("currentState", workflowTask.getCurrentRunStatistics().getPartStatistics());
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
 }
