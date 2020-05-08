 /* 
  *  Process Aware Web Application Platform 
  * 
  *  Copyright (C) 2011-2013 Inherit S AB 
  * 
  *  This program is free software: you can redistribute it and/or modify 
  *  it under the terms of the GNU Affero General Public License as published by 
  *  the Free Software Foundation, either version 3 of the License, or 
  *  (at your option) any later version. 
  * 
  *  This program is distributed in the hope that it will be useful, 
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
  *  GNU Affero General Public License for more details. 
  * 
  *  You should have received a copy of the GNU Affero General Public License 
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  * 
  *  e-mail: info _at_ inherit.se 
  *  mail: Inherit S AB, Långsjövägen 8, SE-131 33 NACKA, SWEDEN 
  *  phone: +46 8 641 64 14 
  */ 
  
 package org.inheritsource.bonita.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.security.auth.login.LoginContext;
 
 import org.inheritsource.bonita.client.util.BonitaUtil;
 import org.inheritsource.service.common.domain.ActivityInstanceItem;
 import org.inheritsource.service.common.domain.ActivityInstanceLogItem;
 import org.inheritsource.service.common.domain.ActivityInstancePendingItem;
 import org.inheritsource.service.common.domain.ActivityWorkflowInfo;
 import org.inheritsource.service.common.domain.CommentFeedItem;
 import org.inheritsource.service.common.domain.DashOpenActivities;
 import org.inheritsource.service.common.domain.InboxTaskItem;
 import org.inheritsource.service.common.domain.PagedProcessInstanceSearchResult;
 import org.inheritsource.service.common.domain.ProcessInstanceDetails;
 import org.inheritsource.service.common.domain.ProcessInstanceListItem;
 import org.inheritsource.service.common.domain.UserInfo;
 import org.ow2.bonita.facade.BAMAPI;
 import org.ow2.bonita.facade.QueryDefinitionAPI;
 import org.ow2.bonita.facade.QueryRuntimeAPI;
 import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
 import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
 import org.ow2.bonita.facade.exception.ActivityNotFoundException;
 import org.ow2.bonita.facade.exception.InstanceNotFoundException;
 import org.ow2.bonita.facade.exception.ProcessNotFoundException;
 import org.ow2.bonita.facade.exception.UserAlreadyExistsException;
 import org.ow2.bonita.facade.identity.User;
 import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
 import org.ow2.bonita.facade.runtime.ActivityInstance;
 import org.ow2.bonita.facade.runtime.ActivityState;
 import org.ow2.bonita.facade.runtime.Comment;
 import org.ow2.bonita.facade.runtime.InstanceState;
 import org.ow2.bonita.facade.runtime.ProcessInstance;
 import org.ow2.bonita.facade.runtime.TaskInstance;
 import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
 import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
 import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
 import org.ow2.bonita.light.LightActivityInstance;
 import org.ow2.bonita.light.LightProcessInstance;
 import org.ow2.bonita.util.AccessorUtil;
 
 public class BonitaEngineServiceImpl {
 
 	public static final Logger log = Logger.getLogger(BonitaEngineServiceImpl.class.getName());
 	
 	HashMap<String, String> processDefinitionUuid2LabelCache = new HashMap<String,String>();
 	HashMap<String, String> processInstanceUuid2LabelCache = new HashMap<String,String>();
 	HashMap<String, String> activityInstanceUuid2LabelCache = new HashMap<String,String>();	
 	HashMap<String, String> processInstanceUuid2processDefinitionUuid = new HashMap<String,String>();
 	HashMap<String, String> activityInstanceUuid2activityDefinitionUuid = new HashMap<String,String>();
 	
 	public BonitaEngineServiceImpl() {
 		
 	}
 	
 	/**
 	 * 
 	 * @param processInstanceUuid
 	 * @param activityName
 	 * @return
 	 */
 	public String getActivityInstanceUuid(String processInstanceUuid, String activityName) {
 		String activityInstanceUuid = null;
 		try {
 			
 			if (activityName != null) {
 				LoginContext loginContext = BonitaUtil.login(); 
 				ProcessInstanceUUID processInstanceUUID = new ProcessInstanceUUID(processInstanceUuid);
 				ProcessInstance processInstance = AccessorUtil.getAPIAccessor().getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
 				
 				Set<ActivityInstance> ais = processInstance.getActivities();
 				for (ActivityInstance ai : ais) {
 					if (activityName.equals(ai.getActivityName())) {
 						activityInstanceUuid = ai.getUUID().getValue();
 					}
 				}
 				
 				BonitaUtil.logout(loginContext);
 			}
 			
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}	
 		return activityInstanceUuid;
 	}
 	
 	public boolean createUser(String userName) {
 		boolean result = false;
 		try {
 			LoginContext loginContext = BonitaUtil.login(); 
 			
 			User user = AccessorUtil.getIdentityAPI().addUser(userName, "bpm");
 			
 			BonitaUtil.logout(loginContext);
 			result = true;
 		}
 		catch (UserAlreadyExistsException aee) {
 			log.info("UserAlreadyExistsException: " + aee);
 			result = true;
 		}			
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}		
 		return result;
 	}
 	
 	public boolean executeTask(String activityInstanceUuid, String userId) {
 		boolean successful = false;
 		ActivityInstanceUUID activityInstanceUUID = new ActivityInstanceUUID(activityInstanceUuid);
 
 		try {
 			LoginContext loginContext = BonitaUtil.loginWithUser(userId); 
 			AccessorUtil.getRuntimeAPI().executeTask(activityInstanceUUID, false);
 			successful = true;
 			BonitaUtil.logoutWithUser(loginContext);
 		} catch (Exception e) {
 			log.severe("Could not execute task: " + e);
 		}
 
 		return successful;
 	}
 		
 	public ActivityInstanceItem getActivityInstanceItem(String activityInstanceUuid) {
 		ActivityInstanceItem result = null;
 		try {
 			log.severe("activityInstanceUuid=" + activityInstanceUuid);
 			LoginContext loginContext = BonitaUtil.login(); 
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			
 			ActivityInstance ai = AccessorUtil.getQueryRuntimeAPI().getActivityInstance(activityUUID);
 			result = this.createActivityInstanceItem(ai);
 			BonitaUtil.logout(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}		
 		return result;
 	}
 
 	
 	public List<ProcessInstanceListItem> getProcessInstancesStartedBy(String user) {
 		List<ProcessInstanceListItem> result = new ArrayList<ProcessInstanceListItem>();
 
 		try {
 			
 			
 			Set<ProcessInstance> piList = new HashSet<ProcessInstance>();
 			LoginContext loginContext = BonitaUtil.loginWithUser(user); 
 			piList = AccessorUtil.getQueryRuntimeAPI().getUserInstances();
 			
 			for (ProcessInstance pi : piList) {
 				ProcessInstanceListItem item = new ProcessInstanceListItem();
 				loadProcessInstanceBriefProperties(pi, item);
 				result.add(item);
 			}
 	
 			BonitaUtil.logoutWithUser(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 
 		return result;
 	}
 	
 	public PagedProcessInstanceSearchResult getProcessInstancesStartedBy(String searchForUserId, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) {
        PagedProcessInstanceSearchResult result = null;
 		
 		try {
 			ProcessInstanceCriterion criterion = this.str2ProcessInstanceCriterion(sortBy, sortOrder);
 			
 			Set<ProcessInstance> piList = new HashSet<ProcessInstance>();
 			LoginContext loginContext = BonitaUtil.loginWithUser(searchForUserId); 
 			piList = AccessorUtil.getQueryRuntimeAPI().getUserInstances();
 			
 			result = calcPagedProcessInstanceSearchResult(piList,  fromIndex, pageSize, sortBy, sortOrder, filter);
 			 
 			BonitaUtil.logoutWithUser(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 
 		return result;
 	}
 	
 	public PagedProcessInstanceSearchResult getProcessInstancesByUuids(List<String> processInstanceUuids, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) {
 		PagedProcessInstanceSearchResult result = null;
 		
 		Set<ProcessInstanceUUID> uuids = new HashSet<ProcessInstanceUUID>();
 		for (String processInstanceUuid : processInstanceUuids) {
 			ProcessInstanceUUID uuid = new ProcessInstanceUUID(processInstanceUuid);
 			uuids.add(uuid);
 		}
 		
 		try {
 			LoginContext loginContext = BonitaUtil.login();
 			
 			ProcessInstanceCriterion criterion = this.str2ProcessInstanceCriterion(sortBy, sortOrder);
 			
 			List<LightProcessInstance> piList = AccessorUtil.getQueryRuntimeAPI().getLightProcessInstances(uuids, 0, Integer.MAX_VALUE, criterion);
 			
 			result = calcPagedProcessInstanceSearchResult(piList,  fromIndex, pageSize, sortBy, sortOrder, filter);
 			/*
 			for (LightProcessInstance pi : piList) {
 				ProcessInstanceListItem item = new ProcessInstanceListItem();
 				loadProcessInstanceBriefProperties(pi, item);
 				result.add(item);
 			}*/
 		    BonitaUtil.logout(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		return result;
 	}
 		
 	public List<ProcessInstanceListItem> getProcessInstancesInvolvedUser(String user, int fromIndex, int pageSize) {
 		// TODO add sort order???
 		List<ProcessInstanceListItem> result = new ArrayList<ProcessInstanceListItem>();
 
 		try {
 			
 			
 			List<LightProcessInstance> piList = null;
 			LoginContext loginContext = BonitaUtil.loginWithUser(user); 
 			piList = AccessorUtil.getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser(user, fromIndex, pageSize);
 			
 			for (LightProcessInstance pi : piList) {
 				ProcessInstanceListItem item = new ProcessInstanceListItem();
 				loadProcessInstanceBriefProperties(pi, item);
 				result.add(item);
 			}
 	
 			BonitaUtil.logoutWithUser(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 
 		return result;
 	}
 	
 
 	/**
 	 * Get detailed process instance information. 
 	 * - performed manual activities
 	 * - current activities
 	 * - comment feed 
 	 * - 
 	 * @param processInstanceUuid
 	 * @return
 	 */
 	public ProcessInstanceDetails getProcessInstanceDetails(String processInstanceUuid) {
 		ProcessInstanceDetails result = null;
 		
 		try {
 			LoginContext loginContext = BonitaUtil.login(); 
 			ProcessInstanceUUID piUuid = new ProcessInstanceUUID(processInstanceUuid);
 			result = getProcessInstanceDetailsByUuid(piUuid);
 			BonitaUtil.logout(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		
 		
 		return result;
 	}
 
 	public ProcessInstanceDetails getProcessInstanceDetailsByActivityInstance(String activityInstanceUuid) {
 		ProcessInstanceDetails result = null;
 		
 		try {
 			LoginContext loginContext = BonitaUtil.login(); 
 			ActivityInstanceUUID aUuid = new ActivityInstanceUUID(activityInstanceUuid);
 			LightActivityInstance lai = AccessorUtil.getQueryRuntimeAPI().getLightActivityInstance(aUuid);
 			ProcessInstanceUUID piUuid = lai.getProcessInstanceUUID(); 
 			result = getProcessInstanceDetailsByUuid(piUuid);
 			BonitaUtil.logout(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		
 		
 		return result;
 	}
 	
 	public List<InboxTaskItem> getUserInbox(String userId) { // ArrayList<ProcessInstanceListItem>
 		List<InboxTaskItem> result = new ArrayList<InboxTaskItem>();
 
 		try {
 			LoginContext loginContext = BonitaUtil.login(); 
 			Collection<TaskInstance> taskList = AccessorUtil.getQueryRuntimeAPI().getTaskList(userId, ActivityState.READY);
 			
 			for (TaskInstance taskInstance : taskList) {
 				result.add(lightActivityInstance2InboxTaskItem(taskInstance));
 			}
 			BonitaUtil.logout(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		return result;
 	}
 	
 	
 	private ProcessInstanceCriterion str2ProcessInstanceCriterion(String sortBy, String sortOrder) {
 		ProcessInstanceCriterion result = ProcessInstanceCriterion.DEFAULT;
 		
 		if ("started".equalsIgnoreCase(sortBy)) {
 			if ("asc".equalsIgnoreCase(sortOrder)) {
 				result = ProcessInstanceCriterion.STARTED_DATE_ASC;
 			}
 			else {
 				result = ProcessInstanceCriterion.STARTED_DATE_DESC;
 			}
 		}
 		else if ("ended".equalsIgnoreCase(sortBy)) {
 			if ("asc".equalsIgnoreCase(sortOrder)) {
 				result = ProcessInstanceCriterion.ENDED_DATE_ASC;
 			}
 			else {
 				result = ProcessInstanceCriterion.ENDED_DATE_DESC;
 			}			
 		}
 		else if ("last_update".equalsIgnoreCase(sortBy)) {
 			if ("asc".equalsIgnoreCase(sortOrder)) {
 				result = ProcessInstanceCriterion.LAST_UPDATE_ASC;
 			}
 			else {
 				result = ProcessInstanceCriterion.LAST_UPDATE_DESC;
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param searchForUserId
 	 * @param fromIndex
 	 * @param pageSize
 	 * @param sortBy
 	 * @param sortOrder
 	 * @param filter 
 	 * @param userId
 	 * @return
 	 */
 	public PagedProcessInstanceSearchResult getProcessInstancesWithInvolvedUser(String searchForUserId, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) { 
 		PagedProcessInstanceSearchResult result = null;
 		
 		try {
 			ProcessInstanceCriterion criterion = this.str2ProcessInstanceCriterion(sortBy, sortOrder);
 			
 			LoginContext loginContext = BonitaUtil.login(); 
 
 			List<LightProcessInstance> piList = AccessorUtil.getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser(searchForUserId, 0, Integer.MAX_VALUE, criterion);
 			
 			result = calcPagedProcessInstanceSearchResult(piList,  fromIndex, pageSize, sortBy, sortOrder, filter);
 			
 			BonitaUtil.logout(loginContext);
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 
 		return result;
 	}
 	
 	private PagedProcessInstanceSearchResult calcPagedProcessInstanceSearchResult(Collection piList, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter) {
 		PagedProcessInstanceSearchResult result = new PagedProcessInstanceSearchResult();
 		
 		int numberOfHits = 0;
 
 		for (Object o : piList) {
 
 			LightProcessInstance pi = (LightProcessInstance)o; 
 			
 			if (InstanceState.FINISHED.equals(pi.getInstanceState()) && "FINISHED".equalsIgnoreCase(filter)) {
 				if (numberOfHits>=fromIndex && result.getHits().size()<pageSize) {
 					ProcessInstanceListItem item = new ProcessInstanceListItem();
 					loadProcessInstanceBriefProperties(pi, item);
 					result.getHits().add(item);
 				}
 				numberOfHits++;
 			}
 			else if (InstanceState.STARTED.equals(pi.getInstanceState()) && "STARTED".equalsIgnoreCase(filter)) {
 				if (numberOfHits>=fromIndex && result.getHits().size()<pageSize) {
 					ProcessInstanceListItem item = new ProcessInstanceListItem();
 					loadProcessInstanceBriefProperties(pi, item);
 					result.getHits().add(item);
 				}
 				numberOfHits++;
 			}
 		}
 		result.setFromIndex(fromIndex);
 		result.setPageSize(pageSize);
 		result.setSortBy(sortBy);
 		result.setSortOrder(sortOrder);
 		result.setNumberOfHits(numberOfHits);
 		return result;
 	}
 	
 	/**
 	 * @param processDefinitionUUIDStr Specifies which process to start in the BPM engine. The engine will try to find latest version of process if version is omitted. (process name instead of uuid)
 	 * @param userid The user that will be logged as process initiator
 	 * @return BPM engine's processInstanceUuid that identifies the created process instance in the BPM engine
 	 */
 	public String startProcess(String processDefinitionUUIDStr, String userid) {
 			String result = null;
 			
 	    	try {
 	    	
 	    		LoginContext loginContext = BonitaUtil.loginWithUser(userid);
 
 	    		ProcessDefinition processDefinition = null;
 	    		if (processDefinitionUUIDStr!=null && processDefinitionUUIDStr.indexOf("--")>=0) {
                     log.info("Start process with explicit version: " + processDefinitionUUIDStr);
     	    		ProcessDefinitionUUID processDefinitionUUID = new ProcessDefinitionUUID(processDefinitionUUIDStr);
     	    	    processDefinition = AccessorUtil.getQueryDefinitionAPI().getProcess(processDefinitionUUID);            }
 	    		else {
 	    			// no version in uuid, try to find last deployed process with processName=processDefinitionUUIDStr
                     log.info("Start process with latest version: " + processDefinitionUUIDStr);
                     processDefinition = AccessorUtil.getQueryDefinitionAPI().getLastProcess(processDefinitionUUIDStr);      
 	    		}
 	    		
 	    		if  (processDefinition != null) {
 	    			log.info("Start process: " + processDefinition.getUUID() + ", user: " + userid );
 	    			
 	    			Map<String,Object> variables = new HashMap<String, Object>();
 	    			//variables.put("docId", docId);
 	    			//variables.put("formId", formId);
 
 	    			ProcessInstanceUUID instanceUuid = AccessorUtil.getRuntimeAPI().instantiateProcess(processDefinition.getUUID(), variables);
 	    			if (instanceUuid != null) {
 	    				result = instanceUuid.getValue();
 	    			}
 	    		}
 	            
 		        BonitaUtil.logoutWithUser(loginContext);
 
 	    	} catch (Exception e) {
 	        	log.severe("Failed to start process: " + e); // instance=TestaCheckboxlist--1.0--8
 	        }
 	    	
 	        return result;
 		
 	}
 
 	public DashOpenActivities getDashOpenActivitiesByUserId(String userId, int remainingDays) {
 		DashOpenActivities dash = null;
 		
 		try {
     		LoginContext loginContext = BonitaUtil.loginWithUser(userId);
     		
     		BAMAPI bamApi = AccessorUtil.getBAMAPI();
     		int openSteps = bamApi.getNumberOfUserOpenSteps();
     		int overdueSteps = bamApi.getNumberOfUserOverdueSteps();
     		int atRiskSteps = bamApi.getNumberOfUserStepsAtRisk(remainingDays);
     		
     		dash = new DashOpenActivities();
     		dash.setOnTrack(openSteps-overdueSteps-atRiskSteps);
     		dash.setAtRisk(atRiskSteps);
     		dash.setOverdue(overdueSteps);
     		 
     		BonitaUtil.logoutWithUser(loginContext);
     	} catch (Exception e) {
         	log.severe("Failed to caclulate DashOpenActivities : " + e); // instance=TestaCheckboxlist--1.0--8
         }
 		
 		return dash;
 	}
 	
 	public int addComment(String activityInstanceUuid, String comment, String userId) {
 		int result = -1;
 		try {
 			LoginContext loginContext = BonitaUtil.login();
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			AccessorUtil.getRuntimeAPI().addComment(activityUUID, comment, userId);
 			result = 1;
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception when adding comment=[" + comment + "] to activityInstanceUuid=[" + activityInstanceUuid + "]");
 		}
 		return result;
 	}
 
 	
 	public ActivityWorkflowInfo getActivityWorkflowInfo(String activityInstanceUuid) {
 		ActivityWorkflowInfo result = null;
 		try {
 			LoginContext loginContext = BonitaUtil.login();
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			result = loadActivityWorkflowInfo(activityUUID);
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception when assigning task activityInstanceUuid=[" + activityInstanceUuid + "]");
 		}
 		return result;
 	}
 
 	
 	public ActivityWorkflowInfo assignTask(String activityInstanceUuid, String userId) {
 		ActivityWorkflowInfo result = null;
 		try {
 			LoginContext loginContext = BonitaUtil.login();
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			AccessorUtil.getRuntimeAPI().assignTask(activityUUID, userId);
 			result = loadActivityWorkflowInfo(activityUUID);
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception when assigning task activityInstanceUuid=[" + activityInstanceUuid + "] to userId=[" + userId + "]");
 		}
 		return result;
 	}
 
 	public ActivityWorkflowInfo addCandidate(String activityInstanceUuid, String userId) {
 		ActivityWorkflowInfo result = null;
 		try {
 			if (userId !=null) {
 				LoginContext loginContext = BonitaUtil.login();
 				ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 				Set<String> candidates = AccessorUtil.getQueryRuntimeAPI().getTaskCandidates(activityUUID);
 				if (candidates ==null) {
 					candidates = new HashSet<String>();
 				}
 				candidates.add(userId);
 				AccessorUtil.getRuntimeAPI().assignTask(activityUUID, candidates);
 				result = loadActivityWorkflowInfo(activityUUID);
 				loginContext.logout();
 			}
 		}
 		catch (Exception e) {
 			log.severe("Exception when assigning task candidate userId=[" + userId + "] to activityInstanceUuid=[" + activityInstanceUuid + "]");
 		}
 		return result;
 	}
 
 	public ActivityWorkflowInfo removeCandidate(String activityInstanceUuid, String userId) {
 		ActivityWorkflowInfo result = null;
 		try {
 			if (userId !=null) {
 				LoginContext loginContext = BonitaUtil.login();
 				ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 				Set<String> candidates = AccessorUtil.getQueryRuntimeAPI().getTaskCandidates(activityUUID);
 				if (candidates != null) {
 					candidates.remove(userId);
 					AccessorUtil.getRuntimeAPI().assignTask(activityUUID, candidates);
 				}
 				result = loadActivityWorkflowInfo(activityUUID);
 				loginContext.logout();
 			}
 		}
 		catch (Exception e) {
 			log.severe("Exception when assigning task candidate userId=[" + userId + "] to activityInstanceUuid=[" + activityInstanceUuid + "]");
 		}
 		return result;
 	}
 
 	public ActivityWorkflowInfo unassignTask(String activityInstanceUuid) {
 		ActivityWorkflowInfo result = null;
 		try {
 			LoginContext loginContext = BonitaUtil.login();
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			AccessorUtil.getRuntimeAPI().unassignTask(activityUUID);
 			result = loadActivityWorkflowInfo(activityUUID);
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception when unassigning task activityInstanceUuid=[" + activityInstanceUuid + "]");
 		}
 		return result;
 	}
 
 	public ActivityWorkflowInfo setPriority(String activityInstanceUuid, int priority) {
 		ActivityWorkflowInfo result = null;
 		try {
 			LoginContext loginContext = BonitaUtil.login();
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			AccessorUtil.getRuntimeAPI().setActivityInstancePriority(activityUUID, priority);
 			result = loadActivityWorkflowInfo(activityUUID);
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception in setPriority activityInstanceUuid=[" + activityInstanceUuid + "]: " + e );
 		}
 		return result;
 	}
 
 	private ActivityWorkflowInfo loadActivityWorkflowInfo(ActivityInstanceUUID activityUUID) throws Exception {
 		ActivityWorkflowInfo result = new ActivityWorkflowInfo();
 		ActivityInstance activity = AccessorUtil.getQueryRuntimeAPI().getActivityInstance(activityUUID);
 		
 		result.setCandidates(createTemporaryUserInfoSet(activity.getLastAssignUpdate().getCandidates()));
 		result.setAssignedUser(createTemporaryUserInfo(activity.getLastAssignUpdate().getAssignedUserId()));
 		result.setPriority(activity.getPriority());
 		
 		return result;
 	}
 	
 	private ProcessInstanceDetails getProcessInstanceDetailsByUuid(ProcessInstanceUUID piUuid) throws Exception {
 		ProcessInstanceDetails result = null;
 		
 		ProcessInstance pi = AccessorUtil.getQueryRuntimeAPI().getProcessInstance(piUuid);
 		if (pi != null) {
 			result = new ProcessInstanceDetails();
 			loadProcessInstanceBriefProperties(pi, result);
 			Set<ActivityInstance> ais = pi.getActivities();
 			for (ActivityInstance ai : ais) {
 				ActivityInstanceItem item = createActivityInstanceItem(ai);
 				if (item != null) {
 					result.addActivityInstanceItem(item);
 				}
 			}
 			// removed comments from timeline....think about it...  loadProcessInstanceCommentFeed(pi, result);
 		}
 		
 		return result;
 	}
 	
 	public List<CommentFeedItem> getProcessInstanceCommentFeedByProcess(String processInstanceUuid) {
 		List<CommentFeedItem> result = new ArrayList<CommentFeedItem>();
 		try {
 			log.severe("processInstanceUuid=" + processInstanceUuid);
 			LoginContext loginContext = BonitaUtil.login(); 
 			ProcessInstanceUUID piUUID = new ProcessInstanceUUID(processInstanceUuid);
 			result = loadProcessInstanceCommentFeed(piUUID);
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		return result;
 	}
 	
 	public List<CommentFeedItem> getProcessInstanceCommentFeedByActivity(String activityInstanceUuid) {
 		List<CommentFeedItem> result = new ArrayList<CommentFeedItem>();
 		try {
 			log.severe("activityInstanceUuid=" + activityInstanceUuid);
 			LoginContext loginContext = BonitaUtil.login(); 
 			ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(activityInstanceUuid);
 			
 			ActivityInstance ai = AccessorUtil.getQueryRuntimeAPI().getActivityInstance(activityUUID);
 			result = loadProcessInstanceCommentFeed(ai.getProcessInstanceUUID());
 			loginContext.logout();
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		return result;
 	}
 	
 	private List<CommentFeedItem> loadProcessInstanceCommentFeed(ProcessInstanceUUID processInstanceUUID) {
 		List<CommentFeedItem> result = new ArrayList<CommentFeedItem>();
 		try {
 			List<Comment> comments = AccessorUtil.getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getCommentFeed();
 			if (comments != null) {
 				for (Comment comment : comments) {
 					CommentFeedItem item = loadCommentFeedItem(comment);
 					result.add(item);
 				}
 			}
 		}
 		catch (Exception e) {
 			log.severe("Exception: " + e);
 		}
 		return result;
 	}
 	
 	private CommentFeedItem loadCommentFeedItem(Comment comment) {
 		CommentFeedItem result = new CommentFeedItem();
 		if (comment.getActivityUUID() != null) {
 			result.setActivityInstanceUuid(comment.getActivityUUID().getValue());
 			result.setActivityLabel(getActivityLabelByInstanceUuid(comment.getActivityUUID()));
 			result.setActivityDefinitionUuid(this.getActivityDefinitionUuidlByInstanceUuid(comment.getActivityUUID()));
 		}
 		result.setMessage(comment.getMessage());
 		if (comment.getInstanceUUID() != null) {
 			result.setProcessInstanceUuid(comment.getInstanceUUID().getValue());
 			result.setProcessLabel(getProcessLabelByInstanceUuid(comment.getInstanceUUID()));
 			result.setProcessDefinitionUuid(getProcessDefinitionUuidByInstanceUuid(comment.getInstanceUUID()));
 		}
 		
 		result.setTimestamp(comment.getDate());
 		result.setUser(this.createTemporaryUserInfo(comment.getUserId()));
 		return result;
 	}
 	
 	private void loadProcessInstanceCommentFeed(ProcessInstance src, ProcessInstanceDetails dst) {
 		List<Comment> comments = src.getCommentFeed();
 		if (comments != null) {
 			for (Comment comment : comments) {
 				CommentFeedItem item = loadCommentFeedItem(comment);
 				dst.getTimeline().add(item);
 			}
 		}
 	}
 
 	private List<CommentFeedItem> loadProcessInstanceCommentFeed(ProcessInstance src) {
 		List<CommentFeedItem> result = new ArrayList<CommentFeedItem> ();
 		List<Comment> comments = src.getCommentFeed();
 		if (comments != null) {
 			for (Comment comment : comments) {
 				CommentFeedItem item = loadCommentFeedItem(comment);
 				result.add(item);
 			}
 		}
 		return result;
 	}
 	
 
     private void loadProcessInstanceBriefProperties(LightProcessInstance src, ProcessInstanceListItem dst) {
         dst.setProcessInstanceUuid(src.getUUID().getValue());
         
         // find out process label
         dst.setProcessLabel(getProcessLabel(src.getProcessDefinitionUUID()));
         
         dst.setProcessInstanceLabel(dst.getProcessLabel() + " - #" + src.getNb());
         
         // find out process instance status
         if (InstanceState.FINISHED.equals(src.getInstanceState())) {
                 dst.setStatus(ProcessInstanceListItem.STATUS_FINISHED);
         }
         else if (InstanceState.CANCELLED.equals(src.getInstanceState())) {
                 dst.setStatus(ProcessInstanceListItem.STATUS_CANCELLED);
         } 
         else if (InstanceState.ABORTED.equals(src.getInstanceState())) {
                 dst.setStatus(ProcessInstanceListItem.STATUS_ABORTED);
         } 
         else {
                 dst.setStatus(ProcessInstanceListItem.STATUS_PENDING);
                 Set<InboxTaskItem> activities = new HashSet<InboxTaskItem>();
                 try {
                         @SuppressWarnings("rawtypes")
                         Set tasks = null;
                         if (src instanceof ProcessInstance) {
                                 tasks = ((ProcessInstance)src).getActivities();
                         }
                         else {
                                 tasks = AccessorUtil.getQueryRuntimeAPI().getLightActivityInstances(src.getProcessInstanceUUID());                              
                         }
                         for (Object taskObj : tasks) {
                                 LightActivityInstance task = (LightActivityInstance)taskObj;
                                 if (ActivityState.READY.equals(task.getState()) || ActivityState.EXECUTING.equals(task.getState())) {
                                         activities.add(lightActivityInstance2InboxTaskItem(task));
                                 }
                         }
                 }
             catch (InstanceNotFoundException e) {
                         log.severe("Exception while loading activities");
                         activities.clear();
                 }
                 dst.setActivities(activities);
         }
         
         dst.setEndDate(src.getEndedDate());
         dst.setStartDate(src.getStartedDate());
         dst.setStartedBy(src.getStartedBy());
         
     }
 
 	
 	private void loadActivityInstanceItem(ActivityInstance src, ActivityInstanceItem dst) {
 		if (dst != null) {
 			dst.setProcessDefinitionUuid(src.getProcessDefinitionUUID().getValue());
 			dst.setProcessInstanceUuid(src.getProcessInstanceUUID().getValue());
 			dst.setActivityDefinitionUuid(src.getActivityDefinitionUUID().getValue());
 			dst.setActivityInstanceUuid(src.getUUID().getValue());
 			dst.setActivityName(src.getActivityName());
 			dst.setActivityLabel(src.getActivityLabel());
 			dst.setStartDate(src.getStartedDate());
 			dst.setCurrentState(src.getState().name());
 			dst.setLastStateUpdate(src.getLastStateUpdate().getUpdatedDate());
 			dst.setLastStateUpdateByUserId(src.getLastStateUpdate().getUpdatedBy());
 			dst.setExpectedEndDate(src.getExpectedEndDate());
 			dst.setPriority(src.getPriority());
 			
 			TaskInstance task = src.getTask();
 			if (task != null) {
 				dst.setStartedBy(task.getStartedBy());
 			}
 			else {
 				dst.setStartedBy("SYSTEM");
 			}
 			
 		}
 		// TODO set type... 
 	}
 	
 	private void loadActivityInstanceLogItem(ActivityInstance src, ActivityInstanceLogItem dst) {
 		dst.setEndDate(src.getEndedDate());
 		dst.setPerformedByUser(this.createTemporaryUserInfo(src.getLastStateUpdate().getUpdatedBy())); // TODO check if this is correct userid
 	}
 	
 	private UserInfo createTemporaryUserInfo(String bonitaUser) {
		UserInfo userInfo = null;
		if (bonitaUser != null && bonitaUser.trim().length()>0) {
			userInfo = new UserInfo();
			userInfo.setUuid(bonitaUser);
			userInfo.setLabelShort(bonitaUser);
			userInfo.setLabel(bonitaUser);
		}
 		return userInfo;
 	}
 	
 	private Set<UserInfo> createTemporaryUserInfoSet(Set<String> bonitaUserIds) {
 		Set<UserInfo> candidates = new HashSet<UserInfo>();
 		for (String bonitaUser : bonitaUserIds) {
 			UserInfo userInfo = this.createTemporaryUserInfo(bonitaUser);
 			candidates.add(userInfo);
 		}
 		return candidates;
 	}
 	
 	private void loadActivityInstancePendingItem(ActivityInstance src, ActivityInstancePendingItem dst) {
 		dst.setCandidates(createTemporaryUserInfoSet(src.getLastAssignUpdate().getCandidates()));
 		dst.setAssignedUser(createTemporaryUserInfo(src.getLastAssignUpdate().getAssignedUserId()));
 		dst.setExpectedEndDate(src.getExpectedEndDate());
 	}
 	
 	private ActivityInstanceItem createActivityInstanceItem(ActivityInstance ai) {
 		ActivityInstanceItem result = null;
 		
 		if (ai != null) {
 			log.severe("BPMN activity uuid: " + ai.getActivityInstanceId() + " label=" + ai.getActivityLabel());
 			if (ai.getState().equals(ActivityState.FINISHED)) {
 				// Finished activity 
 				if (ai.getType() == ActivityDefinition.Type.Human) {
 					// only manual activities
 					result = new ActivityInstanceLogItem();
 					loadActivityInstanceLogItem(ai, (ActivityInstanceLogItem)result);
 				}
 			}
 			else {
 				// pending activity
 				result = new ActivityInstancePendingItem();
 				loadActivityInstancePendingItem(ai, (ActivityInstancePendingItem)result);
 			}
 			loadActivityInstanceItem(ai, result);
 		}
 		
 		log.severe("return: " + result);
 		return result;
 	}
 
 	private String getProcessLabel(ProcessDefinitionUUID processDefinitionUUID) {
 		String result = processDefinitionUuid2LabelCache.get(processDefinitionUUID.getValue());
 
 		if (result == null) {
 			QueryDefinitionAPI definitionAPI = AccessorUtil.getQueryDefinitionAPI();
 			try {
 				ProcessDefinition processDefinition = definitionAPI.getProcess(processDefinitionUUID);
 				result = processDefinition.getLabel();
 			} catch (ProcessNotFoundException e) {
 				result = "n/a";
 			}
 			processDefinitionUuid2LabelCache.put(processDefinitionUUID.getValue(), result);
 		}
 
 		return result;
 	}
 	
 	private String getProcessDefinitionUuidByInstanceUuid(ProcessInstanceUUID processInstanceUUID) {
 		String result = processInstanceUuid2processDefinitionUuid.get(processInstanceUUID.getValue());
 
 		if (result == null) {
 			QueryRuntimeAPI qrAPI = AccessorUtil.getQueryRuntimeAPI();
 			try {
 				LightProcessInstance ai = qrAPI.getLightProcessInstance(processInstanceUUID);
 				result = ai.getProcessDefinitionUUID().getValue();
 			} catch (InstanceNotFoundException e) {
 				// TODO Auto-generated catch block
 				result = "n/a";
 			}
 			processInstanceUuid2processDefinitionUuid.put(processInstanceUUID.getValue(), result);
 		}
 
 		return result;
 	}
 	
 	private String getActivityLabelByInstanceUuid(ActivityInstanceUUID activityInstanceUUID) {
 		String result = activityInstanceUuid2LabelCache.get(activityInstanceUUID.getValue());
 
 		if (result == null) {
 			QueryRuntimeAPI qrAPI = AccessorUtil.getQueryRuntimeAPI();
 			try {
 				LightActivityInstance ai = qrAPI.getLightActivityInstance(activityInstanceUUID);
 				result = ai.getActivityLabel();
 			} catch (ActivityNotFoundException e) {
 				result = "n/a";
 			}
 			activityInstanceUuid2LabelCache.put(activityInstanceUUID.getValue(), result);
 		}
 
 		return result;
 	}
 	
 	private String getActivityDefinitionUuidlByInstanceUuid(ActivityInstanceUUID activityInstanceUUID) {
 		String result = activityInstanceUuid2activityDefinitionUuid.get(activityInstanceUUID.getValue());
 
 		if (result == null) {
 			QueryRuntimeAPI qrAPI = AccessorUtil.getQueryRuntimeAPI();
 			try {
 				LightActivityInstance ai = qrAPI.getLightActivityInstance(activityInstanceUUID);
 				result = ai.getActivityDefinitionUUID().getValue();
 			} catch (ActivityNotFoundException e) {
 				result = "n/a";
 			}
 			activityInstanceUuid2activityDefinitionUuid.put(activityInstanceUUID.getValue(), result);
 		}
 
 		return result;
 	}
 
 	
 	private String getProcessLabelByInstanceUuid(ProcessInstanceUUID processInstanceUUID) {
 		String result = processInstanceUuid2LabelCache.get(processInstanceUUID.getValue());
 
 		if (result == null) {
 			QueryRuntimeAPI qrAPI = AccessorUtil.getQueryRuntimeAPI();
 			try {
 				LightProcessInstance ai = qrAPI.getLightProcessInstance(processInstanceUUID);
 				result = getProcessLabel(ai.getProcessDefinitionUUID());
 			} catch (InstanceNotFoundException e) {
 				result = "n/a";
 			}
 			processInstanceUuid2LabelCache.put(processInstanceUUID.getValue(), result);
 		}
 
 		return result;
 	}
 	
 	private InboxTaskItem lightActivityInstance2InboxTaskItem(
 			LightActivityInstance activity) {
 		InboxTaskItem taskItem = new InboxTaskItem();
 		taskItem.setActivityCreated(activity.getReadyDate()); // TODO we want the *process instance* start date
 		taskItem.setExpectedEndDate(activity.getExpectedEndDate());
 		taskItem.setActivityLabel(activity.getActivityLabel());
 		taskItem.setProcessLabel(getProcessLabel(activity.getProcessDefinitionUUID()));;
 		taskItem.setProcessActivityFormInstanceId(new Long(0)); // TODO
 		taskItem.setTaskUuid(activity.getUUID().getValue());
 		taskItem.setProcessInstanceUuid(activity.getProcessInstanceUUID().getValue());
 		taskItem.setActivityDefinitionUuid(activity.getActivityDefinitionUUID().getValue());
 		taskItem.setProcessDefinitionUuid(activity.getProcessDefinitionUUID().getValue());
 		
 		return taskItem;
 		
 	}
 
 }
