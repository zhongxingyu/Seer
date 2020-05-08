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
 import org.bitrepository.integrityservice.cache.CollectionStat;
 import org.bitrepository.integrityservice.cache.IntegrityModel;
 import org.bitrepository.integrityservice.cache.PillarStat;
 import org.bitrepository.service.workflow.JobID;
 import org.bitrepository.service.workflow.Workflow;
 import org.bitrepository.service.workflow.WorkflowManager;
 import org.bitrepository.service.workflow.WorkflowStatistic;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.*;
 
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
         List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
         Map<String, PillarStat> stats = new HashMap<String, PillarStat>();
         for(PillarStat stat : model.getLatestPillarStats(collectionID)) {
             if(pillars.contains(stat.getPillarID())) {
                 stats.put(stat.getPillarID(), stat);
             }
         }
         for(String pillar : pillars) {
             if(!stats.containsKey(pillar)) {
                 PillarStat emptyStat = new PillarStat(pillar, collectionID, 0L, 0L, 0L, 0L, 
                         new Date(0), new Date(0));;
                 stats.put(pillar, emptyStat);
             }
         }
         for(PillarStat stat : stats.values()) {
              array.put(makeIntegrityStatusObj(stat));
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
             for(JobID workflowID : workflowManager.getWorkflows(collectionID)) {
                 array.put(makeWorkflowSetupObj(workflowID));
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
         List<String> workflowIDs = new LinkedList<String>();
         for(JobID workflowID : workflowManager.getWorkflows(collectionID)) {
             workflowIDs.add(workflowID.getWorkflowName());
         }
         return workflowIDs;
     }
 
     /**
      * Start a named workflow.  
      */
     @POST
     @Path("/startWorkflow/")
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
         List<CollectionStat> stats = model.getLatestCollectionStat(collectionID, 1);
         Date lastIngest = model.getDateForNewestFileEntryForCollection(collectionID);
         String lastIngestStr = lastIngest == null ? "No files ingested yet" : TimeUtils.shortDate(lastIngest);
         Long collectionSize;
         Long numberOfFiles;
         if(stats == null || stats.isEmpty()) {
             collectionSize = 0L;
             numberOfFiles = 0L;
         } else {
             CollectionStat stat = stats.get(0);
             collectionSize = stat.getDataSize();
             numberOfFiles = stat.getFileCount();
         }
         try {
             obj.put("lastIngest", lastIngestStr);
             obj.put("collectionSize", FileSizeUtils.toHumanShort(collectionSize));
             obj.put("numberOfFiles", numberOfFiles);
         } catch (JSONException e) {
             obj = (JSONObject) JSONObject.NULL;
         }
         return obj.toString();
     }
     
     private JSONObject makeIntegrityStatusObj(PillarStat stat) {
         JSONObject obj = new JSONObject();
         try {
             obj.put("pillarID", stat.getPillarID());
             obj.put("totalFileCount", stat.getFileCount());
             obj.put("missingFilesCount", stat.getMissingFiles());
             obj.put("checksumErrorCount", stat.getChecksumErrors());
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
 
     private JSONObject makeWorkflowSetupObj(JobID workflowID) {
         JSONObject obj = new JSONObject();
         Workflow workflow = workflowManager.getWorkflow(workflowID);
         WorkflowStatistic lastRunStatistic = workflowManager.getLastCompleteStatistics(workflowID);
         try {
             obj.put("workflowID", workflowID.getWorkflowName());
             obj.put("workflowDescription", workflow.getDescription());
             obj.put("nextRun", TimeUtils.shortDate(workflowManager.getNextScheduledRun(workflowID)));
            if (lastRunStatistic.getFinish() == null) {
                 obj.put("lastRun", "Workflow hasn't finished a run yet");
             } else {
                 obj.put("lastRun", TimeUtils.shortDate(lastRunStatistic.getFinish()));
             }
             obj.put("lastRunDetails", lastRunStatistic.getFullStatistics());
             long runInterval = workflowManager.getRunInterval(workflowID);
             String intervalString;
             if (runInterval == -1 ) {
                 intervalString = "Never";
             }  else {
                 intervalString = TimeUtils.millisecondsToHuman(runInterval);
             }
             obj.put("executionInterval", intervalString);
             obj.put("currentState", workflowManager.getCurrentStatistics(workflowID).getPartStatistics());
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
 }
