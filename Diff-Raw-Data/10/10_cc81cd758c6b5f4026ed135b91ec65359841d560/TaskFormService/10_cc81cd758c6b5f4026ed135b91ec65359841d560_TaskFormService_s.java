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
  
 package org.inheritsource.taskform.engine;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import org.inheritsource.bonita.client.BonitaEngineServiceImpl;
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
 import org.inheritsource.service.common.domain.StartLogItem;
 import org.inheritsource.service.common.domain.Tag;
 import org.inheritsource.service.common.domain.TimelineItem;
 import org.inheritsource.service.common.domain.UserInfo;
 import org.inheritsource.service.orbeon.OrbeonService;
 import org.inheritsource.taskform.engine.persistence.TaskFormDb;
 import org.inheritsource.taskform.engine.persistence.entity.ActivityFormDefinition;
 import org.inheritsource.taskform.engine.persistence.entity.ProcessActivityFormInstance;
 import org.inheritsource.taskform.engine.persistence.entity.StartFormDefinition;
 import org.inheritsource.taskform.engine.persistence.entity.UserEntity;
 
 public class TaskFormService {
 
 	public static final Logger log = Logger.getLogger(TaskFormService.class
 			.getName());
 
 	TaskFormDb taskFormDb;
 	BonitaEngineServiceImpl bonitaClient;
 	ActorSelectorDirUtils aSelectorDirUtils;
     OrbeonService orbeonService;
 
 	public TaskFormService() {
 		taskFormDb = new TaskFormDb();
 		orbeonService = new OrbeonService();
 		bonitaClient = new BonitaEngineServiceImpl();
 		// TODO hostname,port and base DN should be resolved from configuration
 		aSelectorDirUtils = new ActorSelectorDirUtils("localhost", "1389",
 				"ou=IDMGroups,OU=Organisation,OU=Malmo,DC=adm,DC=malmo,DC=se"); // Base
 																				// DN
 	}
 	
 	public String getPrevoiusActivitiesData(String currentActivityFormDocId) {
 		ProcessActivityFormInstance currentActivity = taskFormDb.getProcessActivityFormInstanceByFormDocId(currentActivityFormDocId);
 		return currentActivity==null ? "" : getProcessInstanceActivitiesData(currentActivity.getProcessInstanceUuid());
 	}
 	
 	public String getPreviousActivityDataByInstanceUuid(String currentActivityInstanceUuid, String previousActivityName, String uniqueXPathExpr) {
 		ProcessActivityFormInstance currentActivity = taskFormDb.getProcessActivityFormInstanceByActivityInstanceUuid(currentActivityInstanceUuid);
 		return currentActivity==null ? "" : getProcessInstanceActivityData(currentActivity.getProcessInstanceUuid(), previousActivityName, uniqueXPathExpr);
 	}
 	
 	public String getPreviousActivityDataByDocId(String currentActivityFormDocId, String previousActivityName, String uniqueXPathExpr) {
 		ProcessActivityFormInstance currentActivity = taskFormDb.getProcessActivityFormInstanceByFormDocId(currentActivityFormDocId);
 		return currentActivity==null ? "" : getProcessInstanceActivityData(currentActivity.getProcessInstanceUuid(), previousActivityName, uniqueXPathExpr);
 	}
 	
 	public String getProcessInstanceActivityData(String processInstanceUuid, String activityName, String uniqueXPathExpr) {
 		String result = "";
 		
 		ProcessActivityFormInstance prevoiusActivity = null;
 		if ("startform".equalsIgnoreCase(activityName)) {
 			prevoiusActivity = taskFormDb.getStartProcessActivityFormInstanceByProcessInstanceUuid(processInstanceUuid);
 		}
 		else {
 			String prevoiusActivityUuid = bonitaClient.getActivityInstanceUuid(processInstanceUuid, activityName);
 			if (prevoiusActivityUuid != null) {
 				prevoiusActivity = taskFormDb.getProcessActivityFormInstanceByActivityInstanceUuid(prevoiusActivityUuid);
 			}
 		}
 		if (prevoiusActivity!=null) {
 			result = orbeonService.getFormDataValue(prevoiusActivity.getFormPath(), prevoiusActivity.getFormDocId(), uniqueXPathExpr);
 		}
 		
 		return result;
 	}
 	
 	public String getProcessInstanceActivitiesData(String processInstanceUuid) {
 		StringBuffer result = new StringBuffer();
 		
 		result.append("<pawap><process processInstanceUuid=\"");
 		result.append(processInstanceUuid);
 		result.append("\">");
 		
 		List<ProcessActivityFormInstance> pafis = taskFormDb.getProcessActivityFormInstances(processInstanceUuid);
 
 		if (pafis != null) {
 			for (ProcessActivityFormInstance pafi : pafis) {
 				if (pafi.getSubmitted() != null) {
 					String formDataFragment = orbeonService.getFormData(
 							pafi.getFormPath(), pafi.getFormDocId());
 					if (formDataFragment != null) {
 						if (pafi.isStartForm()) {
 							result.append("<startform>");
 							result.append(formDataFragment);
 							result.append("</startform>");
 						} else {
 							// activity form
 
 							ActivityInstanceItem activityItem = bonitaClient
 									.getActivityInstanceItem(pafi
 											.getActivityInstanceUuid());
 
 							if (activityItem != null) {
 								result.append("<activity uuid=\"");
 								result.append(pafi.getActivityInstanceUuid());
 								result.append("\" activityName=\"");
 								result.append(activityItem.getActivityName());
 								result.append("\" activityDefinitionUuid=\"");
 								result.append(activityItem
 										.getActivityDefinitionUuid());
 								result.append("\">");
 								result.append(formDataFragment);
 								result.append("</activity>");
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		result.append("</process></pawap>");
 		
 		return result.toString();
 	}
  
 
 	/**
 	 * 
 	 * @param userId
 	 * @param remainingDays
 	 *            atRisk activities is calculated as the number of activities
 	 *            that will be overdue within remainingDays
 	 * @return
 	 */
 	public DashOpenActivities getDashOpenActivitiesByUserId(String userId,
 			int remainingDays) {
 		DashOpenActivities result = bonitaClient.getDashOpenActivitiesByUserId(
 				userId, remainingDays);
 		return result;
 	}
 
 	/**
 	 * Get inbox task items a) partially filled start forms, not started
 	 * processes b) started process with tasks associated to userId, check if
 	 * partially filled form exist
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public List<InboxTaskItem> getInboxTaskItems(String userId) {
 		// activityDefinitionUUID vill bonita form engine ha det? lägg i så fall
 		// till uppslag på lämplig plats....
 		List<InboxTaskItem> unsubmittedStartForms = new ArrayList<InboxTaskItem>();
 
 		log.severe("=======> getInboxTaskItems " + userId);
 		// Find partially filled not submitted forms
 		List<ProcessActivityFormInstance> pending;
 		pending = taskFormDb.getPendingProcessActivityFormInstances(userId);
 
 		HashMap<String, ProcessActivityFormInstance> forms = new HashMap<String, ProcessActivityFormInstance>();
 		for (ProcessActivityFormInstance form : pending) {
 			log.severe("=======> PENDING form: " + form);
 			String activityInstanceUuid = form.getActivityInstanceUuid();
 			if (activityInstanceUuid != null
 					&& activityInstanceUuid.trim().length() > 0) {
 				forms.put(activityInstanceUuid, form);
 			} else {
 				// this is a pending start form i.e. no process instance exist
 				InboxTaskItem startFormItem = new InboxTaskItem();
 				startFormItem.setProcessActivityFormInstanceId(null);
 				startFormItem.setActivityCreated(null); // TODO fill with
 														// timestamp ?
 				startFormItem.setActivityLabel("Ansökan"); // TODO bonita login
 				startFormItem.setProcessActivityFormInstanceId(form
 						.getProcessActivityFormInstanceId());
 				if (form.getStartFormDefinition() != null) {
 					startFormItem.setProcessLabel("Inte inskickad"); // TODO
 					// startFormItem.setProcessLabel(bonitaClient.getProcessLabel(form.getStartFormDefinition().getProcessDefinitionUuid()));
 					startFormItem.setStartedByFormPath(form
 							.getStartFormDefinition().getFormPath());
 				} else {
 					startFormItem.setProcessLabel("");
 				}
 
 				unsubmittedStartForms.add(startFormItem);
 			}
 		}
 
 		// find inbox tasks from BOS engine
 		List<InboxTaskItem> inbox = bonitaClient.getUserInbox(userId);
 		for (InboxTaskItem item : inbox) {
 			String taskUuid = item.getTaskUuid();
 			log.severe("=======> TASK bonita: " + item);
 
 			// TODO optimize ???
 			ProcessActivityFormInstance startedByForm = taskFormDb
 					.getSubmittedStartProcessActivityFormInstanceByProcessInstanceUuid(item
 							.getProcessInstanceUuid());
 			if (startedByForm != null) {
 				item.setStartedByFormPath(startedByForm.getFormPath());
 			}
 
 			ProcessActivityFormInstance form = forms.get(taskUuid);
 			if (form != null) {
 				// partial filled not submitted form exist for task
 				// assign id to inbox item that can be used later to edit/view
 				// existing form
 				item.setProcessActivityFormInstanceId(form
 						.getProcessActivityFormInstanceId());
 				item.setExternalUrl(calcExternalUrl(form.getFormPath(), form.getProcessActivityFormInstanceId(), taskUuid));
 				forms.remove(form);
 			} else {
 				// analyze activity form
 				ActivityFormDefinition activity = taskFormDb.getActivityFormDefinition(item.getActivityDefinitionUuid(), startedByForm == null ? null : startedByForm.getStartFormDefinition().getStartFormDefinitionId());
 				if (activity == null) {
 					// activity form is not defined
 					item.setExternalUrl(calcExternalUrl("none", null, taskUuid));
 				}
 				else {
 					item.setExternalUrl(calcExternalUrl(activity.getFormPath(), null, taskUuid));
 				}
 			}
 			
 			
 			// else {
 			// no one has opened this activity form yet
 			// i.e. no ProcessActivityFormInstance has been stored in database
 			// so far
 			// The ProcessActivityFormInstance is created on first time
 			// }
 			
 		}
 
 		// partially filled forms
 		for (ProcessActivityFormInstance partForm : forms.values()) {
 			if (partForm.getProcessInstanceUuid() == null) {
 				// partially filled start form that will start a process if
 				// submitted
 
 				log.warning("Unexpected partially filled form=" + partForm);
 			} else {
 				// there is a partially filled form associated to this user but
 				// the corresponding BPMN
 				// activity is no longer associated to this user
 				log.info("This partial filled form is associated to a process/task instance but is not associated to an active task in user's inbox: "
 						+ partForm);
 			}
 		}
 
 		inbox.addAll(unsubmittedStartForms);
 
 		Collections.sort(inbox);
 		log.severe("=======> getInboxTaskItems " + userId + " size="
 				+ (inbox == null ? 0 : inbox.size()));
 		return inbox;
 	}
 	
 	private String calcExternalUrl(String formPath, Long processActivityFormInstanceId, String taskUuid) {
 		String externalUrl = null;
 		
 		String activityUrlIdStr = processActivityFormInstanceId == null ? "taskUuid=" + taskUuid : "processActivityFormInstanceId=" + processActivityFormInstanceId; 
 		
 		if ("none".equals(formPath)) {
 			// activity form is not defined
 			externalUrl = "noform?" + activityUrlIdStr;
 		}
 		else if (formPath.startsWith("signstartform")) {
 			externalUrl = "signform?" + activityUrlIdStr;
 		}
 		else if (formPath.startsWith("signactivity/")) {
 			String activityName = formPath.substring(13);
 			externalUrl = "signform?" + activityUrlIdStr + "&signActivityName=" + activityName;
 		}
 		
 		return externalUrl;
 	}
 
 	public ProcessInstanceDetails getProcessInstanceDetails(
 			String processInstanceUuid) {
 		ProcessInstanceDetails details = bonitaClient
 				.getProcessInstanceDetails(processInstanceUuid);
 		appendTaskFormServiceData(details);
 		return details;
 	}
 
 	public ProcessInstanceDetails getProcessInstanceDetailsByActivityInstance(
 			String activityInstanceUuid) {
 		ProcessInstanceDetails details = bonitaClient
 				.getProcessInstanceDetailsByActivityInstance(activityInstanceUuid);
 		appendTaskFormServiceData(details);
 		return details;
 	}
 
 	private void appendTaskFormServiceData(ProcessInstanceDetails details) {
 		if (details != null) {
 			for (ActivityInstancePendingItem pendingItem : details.getPending()) {
 				// pending activity => set edit url if
 				// ProcessActivityFormInstance is initialized
 				ProcessActivityFormInstance activity = taskFormDb
 						.getProcessActivityFormInstanceByActivityInstanceUuid(pendingItem
 								.getActivityInstanceUuid());
 				if (activity != null) {
 					pendingItem.setFormUrl(activity.calcEditUrl());
 				}
 				appendCandidateUserInfo(pendingItem);
 				pendingItem
 						.setAssignedUser(appendTaskFormServiceUserData(pendingItem
 								.getAssignedUser()));
 			}
 
 			for (TimelineItem timelineItem : details.getTimeline().getItems()) {
 				if (timelineItem instanceof ActivityInstanceLogItem) {
 					ActivityInstanceLogItem logItem = (ActivityInstanceLogItem) timelineItem;
 					ProcessActivityFormInstance activity = taskFormDb
 							.getProcessActivityFormInstanceByActivityInstanceUuid(logItem
 									.getActivityInstanceUuid());
 					// log item => set view url
 					if (activity != null) {
 						String viewUrl = activity.calcViewUrl();
 						logItem.setFormUrl(viewUrl);
 						logItem.setViewUrl(viewUrl);
 						logItem.setFormDocId(activity.getFormDocId());
 						
 						logItem.setPerformedByUser(taskFormDb
 								.getUserByUuid(activity.getUserId()));
 					}
 				}
 			}
 			
 			StartLogItem startLogItem = getStartFormActivityInstanceLogItem(details.getProcessInstanceUuid());
 			
 			ProcessActivityFormInstance startedByForm = taskFormDb.getSubmittedStartProcessActivityFormInstanceByProcessInstanceUuid(details.getProcessInstanceUuid());
 			if (startedByForm != null) {
 				details.setStartedByFormPath(startedByForm.getFormPath());
 			}
 			
 			details.getTimeline().addAndSort(startLogItem);
 		}
 	}
 
 	public int addComment(String activityInstanceUuid, String comment,
 			String userId) {
 		int result = bonitaClient.addComment(activityInstanceUuid, comment,
 				userId);
 		return result;
 	}
 
 	public List<CommentFeedItem> getCommentFeed(String activityInstanceUuid,
 			String userId) {
 		List<CommentFeedItem> result = bonitaClient
 				.getProcessInstanceCommentFeedByActivity(activityInstanceUuid);
 		return result;
 	}
 
 	public ActivityWorkflowInfo getActivityWorkflowInfo(
 			String activityInstanceUuid) {
 		ActivityWorkflowInfo result = bonitaClient
 				.getActivityWorkflowInfo(activityInstanceUuid);
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo assignTask(String activityInstanceUuid,
 			String userId) {
 		ActivityWorkflowInfo result = bonitaClient.assignTask(
 				activityInstanceUuid, userId);
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	private void appendTaskFormServiceData(
 			ActivityWorkflowInfo activityWorkflowInfo) {
 		activityWorkflowInfo
 				.setAssignedUser(appendTaskFormServiceUserData(activityWorkflowInfo
 						.getAssignedUser()));
 	}
 
 	public ActivityWorkflowInfo unassignTask(String activityInstanceUuid) {
 		ActivityWorkflowInfo result = bonitaClient
 				.unassignTask(activityInstanceUuid);
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo addCandidate(String activityInstanceUuid,
 			String userId) {
 		ActivityWorkflowInfo result = bonitaClient.addCandidate(
 				activityInstanceUuid, userId);
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo removeCandidate(String activityInstanceUuid,
 			String userId) {
 		ActivityWorkflowInfo result = bonitaClient.removeCandidate(
 				activityInstanceUuid, userId);
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo setActivityPriority(
 			String activityInstanceUuid, int priority) {
 		ActivityWorkflowInfo result = bonitaClient.setPriority(
 				activityInstanceUuid, priority);
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public StartLogItem getStartFormActivityInstanceLogItem(String processInstanceUuid) {
 		StartLogItem result = null;
 
 		if (processInstanceUuid != null) {
 
 			ProcessActivityFormInstance startActivity = taskFormDb
 					.getStartProcessActivityFormInstanceByProcessInstanceUuid(processInstanceUuid);
 			if (startActivity != null) {
 				result = new StartLogItem();
 				result.setViewUrl(startActivity.calcViewUrl());
 				result.setFormUrl(startActivity.calcViewUrl());
 				result.setEndDate(startActivity.getSubmitted());
 				result.setActivityLabel(startActivity.getFormPath());
 				result.setPerformedByUser(taskFormDb
 						.getUserByUuid(startActivity.getUserId()));
 				result.setFormDocId(startActivity.getFormDocId());
 			}
 		}
 
 		return result;
 	}
 
 	public String submitStartForm(String formPath, String docId, String userId)
 			throws Exception {
 		String result;
 		log.severe("formPath=[" + formPath + "] docId=[" + docId + "] userId=["
 				+ userId + "]");
 		// lookup the process this start form is configured to start.
 		try {
 			StartFormDefinition startFormDef = taskFormDb
 					.getStartFormDefinitionByFormPath(formPath);
 			String processDefinitionUUIDStr = startFormDef
 					.getProcessDefinitionUuid();
 
 			// start the process
 			String processInstanceUuid = bonitaClient.startProcess(
 					processDefinitionUUIDStr, userId);
 
 			// store the start activity
 			ProcessActivityFormInstance activity = new ProcessActivityFormInstance();
 			activity.setFormDocId(docId);
 			activity.setStartFormDefinition(startFormDef);
 			activity.setFormPath(formPath);
 			activity.setProcessInstanceUuid(processInstanceUuid);
 			activity.setSubmitted(new Date());
 			activity.setUserId(userId);
 			taskFormDb.saveProcessActivityFormInstance(activity);
 			result = processInstanceUuid;
 		} catch (Exception e) {
 			// TODO catch block cleaning up possible inconsistencies. ROL
 			e.printStackTrace();
 			throw e;
 		} finally {
 		}
 		return result;
 	}
 
 	private UserInfo appendTaskFormServiceUserData(UserInfo ui) {
 		UserInfo dbUi = null;
 
 		if (ui != null) {
 			dbUi = taskFormDb.getUserByUuid(ui.getUuid());
 		}
 
		log.severe("UserInfo[" + ui.getUuid() + "]=...");
 		if (dbUi == null) {
			log.severe("null");
 			dbUi = ui;
 		} else {
 			log.severe(dbUi.getLabel());
 		}
 		return dbUi;
 	}
 
 	private void appendCandidateUserInfo(
 			ActivityInstancePendingItem activityInstancePendingItem) {
 		log.severe("UserInfo appending UserEntity data...");
 		if (activityInstancePendingItem.getCandidates() != null) {
 			Set<UserInfo> candidates = new TreeSet<UserInfo>();
 			for (UserInfo ui : activityInstancePendingItem.getCandidates()) {
				candidates.add(appendTaskFormServiceUserData(ui));
 			}
 			activityInstancePendingItem.setCandidates(candidates);
 		}
 	}
 
 	private void apppendTaskFormServiceData(ActivityInstanceItem item,
 			ProcessActivityFormInstance activityFormInstance) {
 		if (item instanceof ActivityInstanceLogItem) {
 			item.setFormUrl(activityFormInstance.calcViewUrl());
 		} else if (item instanceof ActivityInstancePendingItem) {
 			ActivityInstancePendingItem aipi = (ActivityInstancePendingItem) item;
 			aipi.setFormUrl(activityFormInstance.calcEditUrl());
 			appendCandidateUserInfo(aipi);
 		}
 
 		if (activityFormInstance.getProcessActivityFormInstanceId() != null) {
 			item.setProcessActivityFormInstanceId(activityFormInstance
 					.getProcessActivityFormInstanceId().longValue());
 		}
 		
 		item.setFormDocId(activityFormInstance.getFormDocId());
 	}
 
 	public ActivityInstanceItem getActivityInstanceItem(
 			String activityInstanceUuid, String userId) {
 		ActivityInstanceItem result = null;
 		ProcessActivityFormInstance formInstance = taskFormDb
 				.getProcessActivityFormInstanceByActivityInstanceUuid(activityInstanceUuid);
 
 		if (formInstance == null) {
 			result = this.initializeActivityForm(activityInstanceUuid, userId);
 		} else {
 			result = bonitaClient.getActivityInstanceItem(activityInstanceUuid);
 			apppendTaskFormServiceData(result, formInstance);
 		}
 		return result;
 	}
 
 	public ActivityInstanceItem getActivityInstanceItem(
 			Long processActivityFormInstanceId) {
 		ActivityInstanceItem result = null;
 		ProcessActivityFormInstance formInstance = taskFormDb
 				.getProcessActivityFormInstanceById(processActivityFormInstanceId);
 		if (formInstance != null) {
 			if (formInstance.isStartForm()) {
 				result = getStartFormActivityInstancePendingItem(formInstance
 						.getFormDocId());
 			} else {
 				result = bonitaClient.getActivityInstanceItem(formInstance
 						.getActivityInstanceUuid());
 			}
 			apppendTaskFormServiceData(result, formInstance);
 		}
 		return result;
 	}
 
 	public ActivityInstanceItem getActivityInstanceItemByUuId(
 			String activityInstanceUuid) {
 		ActivityInstanceItem result = bonitaClient.getActivityInstanceItem(activityInstanceUuid);
 		return result;
 	}
 	
 	public ActivityInstanceItem getStartActivityInstanceItem(String formPath,
 			String userId) throws Exception {
 		ActivityInstanceItem result = null;
 		ProcessActivityFormInstance formInstance = taskFormDb
 				.getStartProcessActivityFormInstanceByFormPathAndUser(formPath,
 						userId);
 
 		if (formInstance == null) {
 			result = this.initializeStartForm(formPath, userId);
 		} else {
 			result = getStartFormActivityInstancePendingItem(formInstance
 					.getFormDocId());
 			apppendTaskFormServiceData(result, formInstance);
 		}
 		return result;
 	}
 
 	private ActivityInstanceItem getStartFormActivityInstancePendingItem(
 			String formDocId) {
 		ProcessActivityFormInstance src = taskFormDb
 				.getProcessActivityFormInstanceByFormDocId(formDocId);
 		return processActivityFormInstance2ActivityInstancePendingItem(src);
 	}
 
 	/**
 	 * This conversion is only valid on StartForms
 	 * 
 	 * @param src
 	 * @return
 	 */
 	private ActivityInstancePendingItem processActivityFormInstance2ActivityInstancePendingItem(
 			ProcessActivityFormInstance src) {
 		// TODO this method name is confusing, it is only working on start
 		// forms...
 		ActivityInstancePendingItem dst = new ActivityInstancePendingItem();
 		try {
 			dst.setProcessDefinitionUuid(src.getStartFormDefinition()
 					.getProcessDefinitionUuid());
 			dst.setActivityName("StartCaseTODO");
 			dst.setActivityLabel("Starta ärende TODO");
 			dst.setStartDate(null);
 			dst.setCurrentState("TODO");
 			dst.setLastStateUpdate(null);
 			dst.setLastStateUpdateByUserId(src.getUserId());
 			dst.setExpectedEndDate(null);
 			dst.setPriority(0);
 			dst.setStartedBy("");
 			dst.setAssignedUser(taskFormDb.getUserByUuid(src.getUserId()));
 			dst.setExpectedEndDate(null);
 			dst.setFormUrl(src.calcEditUrl());
 
 			dst.setProcessInstanceUuid(null);
 			dst.setActivityInstanceUuid(null);
 			dst.setActivityDefinitionUuid(null);
 		} catch (RuntimeException re) {
 			log.severe("Cannot convert ProcessActivityFormInstance " + src
 					+ " to ActivityInstancePendingItem. RuntimeException: "
 					+ re);
 			throw re;
 		}
 
 		return dst;
 	}
 
 	private ActivityInstanceItem initializeActivityForm(
 			String activityInstanceUuid, String userId) {
 		ActivityInstanceItem activity = bonitaClient
 				.getActivityInstanceItem(activityInstanceUuid);
 
 		log.severe("===> activity=" + activity);
 		StartFormDefinition startFormDefinition = taskFormDb
 				.getStartFormDefinition(activity.getActivityDefinitionUuid(),
 						activity.getProcessInstanceUuid());
 		log.severe("===> startFormDefinition=" + startFormDefinition);
 		ActivityFormDefinition activityFormDefinition = null;
 		Long startFormId = null;
 		if (startFormDefinition != null) {
 			startFormId = startFormDefinition.getStartFormDefinitionId();
 		}
 		log.severe("===> activityInstanceUuid=" + activityInstanceUuid
 				+ ", startFormId=" + startFormId + ", userId=" + userId);
 		activityFormDefinition = taskFormDb.getActivityFormDefinition(
 				activity.getActivityDefinitionUuid(), startFormId);
 
 		// generate a type 4 UUID
 		String docId = java.util.UUID.randomUUID().toString();
 
 		// store the start activity
 		ProcessActivityFormInstance activityInstance = new ProcessActivityFormInstance();
 		activityInstance.setFormDocId(docId);
 		activityInstance.setStartFormDefinition(startFormDefinition);
 		activityInstance.setFormPath(activityFormDefinition.getFormPath());
 		activityInstance.setProcessInstanceUuid(activity
 				.getProcessInstanceUuid());
 		activityInstance.setActivityInstanceUuid(activity
 				.getActivityInstanceUuid());
 		activityInstance.setUserId(userId);
 		taskFormDb.saveProcessActivityFormInstance(activityInstance);
 
 		apppendTaskFormServiceData(activity, activityInstance);
 		return activity;
 	}
 
 	private ActivityInstanceItem initializeStartForm(String formPath,
 			String userId) throws Exception {
 		log.severe("formPath=[" + formPath + "] userId=[" + userId + "]");
 
 		// lookup the process this start form is configured to start.
 
 		try {
 			StartFormDefinition startFormDefinition = taskFormDb
 					.getStartFormDefinitionByFormPath(formPath);
 
 			// generate a type 4 UUID
 			String docId = java.util.UUID.randomUUID().toString();
 
 			// store the start form activity
 			ProcessActivityFormInstance activityInstance = new ProcessActivityFormInstance();
 			activityInstance.setFormDocId(docId);
 			activityInstance.setStartFormDefinition(startFormDefinition);
 			activityInstance.setFormPath(formPath);
 			activityInstance.setProcessInstanceUuid(null);
 			activityInstance.setActivityInstanceUuid(null);
 			activityInstance.setSubmitted(null);
 			activityInstance.setUserId(userId);
 
 			taskFormDb.saveProcessActivityFormInstance(activityInstance);
 			return processActivityFormInstance2ActivityInstancePendingItem(activityInstance);
 		} finally {
 
 		}
 	}
 
 	
 	/**
 	 * submit form
 	 * 
 	 * @param docId
 	 * @param userId 
 	 * @param newDocId Replace docId with a new one on submit. 
 	 * @return confirmation form viewUrl. null if submission fails.
 	 */
 	public String submitActivityForm(String docId, String userId, String newDocId)
 			throws Exception {
 		String viewUrl = null;
 		
 		// TODO, när userId är null 1) kolla om det finns i formulärdata i xpath angiven av startformdef 2) anonymous om startformdef tillåter anonym
 		
 		try {
 			ProcessActivityFormInstance activity = taskFormDb.getProcessActivityFormInstanceByFormDocId(docId);
 
 			if (activity == null) {
 				log.severe("This should never happen :) cannot find activity with docId=["
 						+ docId + "]" + " userId=[" + userId + "]");
 			} else {
 				
 				Date tstamp = new Date();
 				activity.setSubmitted(tstamp);
 				activity.setUserId(userId);
 				
 				if (newDocId != null) {
 					activity.setFormDocId(newDocId);
 				}
 				
 				boolean success = false;
 
 				
 				if (activity.isStartForm()) {
 					String startFormUser = null;
 					switch (activity.getStartFormDefinition().getAuthTypeReq()) {
     				    case USERSESSION: 
     				    	// do nothing userId is going to be used
     				    	break;
 					    case USERSESSION_FORMDATA: 
 					    	if (userId == null || userId.trim().length()==0) {
 						      startFormUser = orbeonService.getFormDataValue(activity.getFormPath(), activity.getFormDocId(), activity.getStartFormDefinition().getUserDataXPath());
 						      userId = startFormUser;
 					    	}
 							break;
 						case FORMDATA_USERSESSION:
 							startFormUser = orbeonService.getFormDataValue(activity.getFormPath(), activity.getFormDocId(), activity.getStartFormDefinition().getUserDataXPath());
 							if (startFormUser != null && startFormUser.trim().length()>0) {
 								userId = startFormUser;
 							}
 							break;
 						case USERSESSION_FORMDATA_ANONYMOUS: 
 					    	if (userId == null || userId.trim().length()==0) {
 						      startFormUser = orbeonService.getFormDataValue(activity.getFormPath(), activity.getFormDocId(), activity.getStartFormDefinition().getUserDataXPath());
 						      userId = startFormUser;
 						      if (userId == null || userId.trim().length()==0) {
 						    	  userId = UserInfo.ANONYMOUS_UUID;
 						      }
 						    }
 							break;
 						case FORMDATA_USERSESSION_ANONYMOUS: 
 							startFormUser = orbeonService.getFormDataValue(activity.getFormPath(), activity.getFormDocId(), activity.getStartFormDefinition().getUserDataXPath());
 							if (startFormUser != null && startFormUser.trim().length()>0) {
 								userId = startFormUser;
 							}
 							if (userId == null || userId.trim().length()==0) {
 						    	  userId = UserInfo.ANONYMOUS_UUID;
 						    }
 							break;
 						case ANONYMOUS:
 							userId = UserInfo.ANONYMOUS_UUID;
 							break;
 						default:
 							log.severe("Unknown AuthTypeReq in StartFormDefinition, this should never happen...");
 							break;
 					}
 					
 					// start the process
 					String pDefUuid = activity.getStartFormDefinition()
 							.getProcessDefinitionUuid();
 										
 					String processInstanceUuid = bonitaClient.startProcess(pDefUuid, userId);
 					activity.setProcessInstanceUuid(processInstanceUuid);
 					
 					// success if start process succeeded
 					success = (processInstanceUuid != null && processInstanceUuid.trim().length() > 0);
 
 				} else if (bonitaClient.executeTask(activity.getActivityInstanceUuid(), userId)) {
 					// execute activity task succeded
 					success = true;
 				}
 
 				if (success) {
 					// if process/task in BPM engine is started/executed
 					// successful
 					// => update status of ProcessActivityFormInstance to
 					// submitted
 					taskFormDb.saveProcessActivityFormInstance(activity);
 					// calculate URL that can be used to render the confirmation
 					viewUrl = activity.calcViewUrl();
 				}
 			}
 		} catch (Exception e) {
 			log.severe("Exception while submitting activity with docId=[" + docId + "] as userId=[" + userId + "] Exception: " + e);
 			// ROL cleanup inconsistencies...
 			throw e;
 		}
 		return viewUrl;
 	}
 
 
 	
 	private String searchForBonitaUser(String searchForUserId) {
 		String searchForBonitaUser = searchForUserId;
 		
 		if (searchForBonitaUser!=null) {
 			searchForBonitaUser = searchForBonitaUser.trim();
 		}
 		UserInfo userInfo = taskFormDb.getUserBySerial(searchForBonitaUser);
 		if (userInfo != null) {
 			searchForBonitaUser = userInfo.getUuid();
 		}
 		
 		return searchForBonitaUser;
 	}
 	
     
     /**
      * Find process instances with involved user
      * @param searchForUserId involved user to search for
      * @param fromIndex paging start index. starts with 0
      * @param pageSize 
      * @param sortBy
      * @param sortOrder
      * @param filter filter on instance state, valid values are STARTED, FINISHED
      * @param userId The user that is actually performing the search, make it possibly to exclude hits because of privacy reasons for instance
      * @return
      */
     public PagedProcessInstanceSearchResult searchProcessInstancesStartedByUser(String searchForUserId, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) { 
             PagedProcessInstanceSearchResult result = bonitaClient.getProcessInstancesStartedBy(searchForBonitaUser(searchForUserId), fromIndex, pageSize, sortBy, sortOrder, filter, userId);
             appendTaskFormData(result.getHits());
             appendProcessAndActivityLabelsFromTaskFormDb(result.getHits()); // TODO merge with appendTaskFormData
             return result;
     }
 
     
    /**
     * Find process instances with involved user
     * @param searchForUserId involved user to search for
     * @param fromIndex paging start index. starts with 0
     * @param pageSize 
     * @param sortBy
     * @param sortOrder
     * @param filter filter on instance state, valid values are STARTED, FINISHED
     * @param userId The user that is actually performing the search, make it possibly to exclude hits because of privacy reasons for instance
     * @return
     */
    public PagedProcessInstanceSearchResult searchProcessInstancesWithInvolvedUser(String searchForUserId, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) { 
            PagedProcessInstanceSearchResult result = bonitaClient.getProcessInstancesWithInvolvedUser(searchForBonitaUser(searchForUserId), fromIndex, pageSize, sortBy, sortOrder, filter, userId);
            appendTaskFormData(result.getHits());
            appendProcessAndActivityLabelsFromTaskFormDb(result.getHits()); // TODO merge with appendTaskFormData
            return result;
    }
    
    
    /**
     * Find process instances that has a specified associated tag
     * @param tagValue  Tag value to search for
     * @param userId    The user that is actually performing the search, make it possibly to exclude hits because of privacy reasons for instance 
 
     * @return
     */
        public PagedProcessInstanceSearchResult searchProcessInstancesListByTag(String tagValue, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) { 
                 List<String> uuids = taskFormDb.getProcessInstancesByTag(tagValue);
                PagedProcessInstanceSearchResult result = bonitaClient.getProcessInstancesByUuids(uuids, fromIndex, pageSize, sortBy, sortOrder, filter, userId);
                appendTaskFormData(result.getHits());
                appendProcessAndActivityLabelsFromTaskFormDb(result.getHits()); // TODO merge with appendTaskFormData
                return result;
         }
  
    
        private void appendTaskFormData(List<ProcessInstanceListItem> items) {
                HashMap<String, ProcessActivityFormInstance> forms = new HashMap<String, ProcessActivityFormInstance>();
                        
                if (items != null) {
                        for (ProcessInstanceListItem item : items) {
                                forms.clear();
                                List<ProcessActivityFormInstance> formInstances = taskFormDb.getProcessActivityFormInstances(item.getProcessInstanceUuid());
                                for (ProcessActivityFormInstance formInstance : formInstances) {
                                        String activityInstanceUuid = formInstance.getActivityInstanceUuid();
                                        if (activityInstanceUuid != null && activityInstanceUuid.trim().length()>0) {
                                                forms.put(activityInstanceUuid, formInstance);
                                        }
                                }
                        
                                if (item.getActivities() != null) {
                                        for (InboxTaskItem activity : item.getActivities()) {
                                                ProcessActivityFormInstance paf = forms.get(activity.getTaskUuid());
                                                if (paf != null) {
                                                        activity.setProcessActivityFormInstanceId(paf.getProcessActivityFormInstanceId());
                                                }
                                        }
                                }
                        }
                }
        }
 
     
 	private void appendProcessAndActivityLabelsFromTaskFormDb(List<ProcessInstanceListItem> items) {
 		
 		if (items != null ) {
 			for (ProcessInstanceListItem item : items) {
 				if (item != null) {
 					String startedByFormPath = null;
 					// TODO optimize ???
 					ProcessActivityFormInstance startedByForm = taskFormDb.getSubmittedStartProcessActivityFormInstanceByProcessInstanceUuid(item.getProcessInstanceUuid());
 					if (startedByForm != null) {
 						startedByFormPath = startedByForm.getFormPath();
 						item.setStartedByFormPath(startedByFormPath);
 					}
 					
 					if (item.getActivities() != null) {
 						for (InboxTaskItem taskItem : item.getActivities()) {
 							if (taskItem != null) {
 								taskItem.setStartedByFormPath(startedByFormPath);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public Tag addTag(Long processActivityFormInstanceId, Long tagTypeId,
 			String value, String userId) {
 		return taskFormDb.addTag(processActivityFormInstanceId, tagTypeId,
 				value, userId);
 	}
 
 	public boolean deleteTag(String processInstanceUuid, String value,
 			String userId) {
 		return taskFormDb.deleteTag(processInstanceUuid, value, userId);
 	}
 
 	public List<Tag> getTagsByProcessInstance(String processInstanceUuid) {
 		return taskFormDb.getTagsByProcessInstance(processInstanceUuid);
 	}
 
 	public Set<String> getUsersByRoleAndActivity(String roleName,
 			String activityInstanceUuid) {
 		// TODO implement getDepartmentByactivityInstanceUuid, for now just
 		// hardwire "Miljöförvaltningen"
 		Set<String> result = null;
 		try {
 			result = aSelectorDirUtils.getUsersByDepartmentAndRole(
 					"Miljöförvaltningen", roleName);
 		} catch (Exception e) {
 			log.severe(e.toString());
 		}
 
 		return result;
 	}
 
 	public UserInfo getUserByDn(String dn) {
 		UserInfo userInfo = taskFormDb.getUserByDn(dn);
 
 		if (userInfo == null) {
 			// new user in system
 
 			String gn = null, sn = null, cn = null;
 			String uuid = java.util.UUID.randomUUID().toString();
 			String serial = null;
 
 			// try to parse CN
 			StringTokenizer st = new StringTokenizer(dn, ",");
 			while (st.hasMoreTokens()) {
 				String s = st.nextToken();
 
 				StringTokenizer valuePairToken = new StringTokenizer(s, "=");
 				String key = null;
 				String value = null;
 				if (valuePairToken.hasMoreTokens()) {
 					key = valuePairToken.nextToken().trim();
 				}
 				if (valuePairToken.hasMoreTokens()) {
 					value = valuePairToken.nextToken().trim();
 				}
 				if (!valuePairToken.hasMoreTokens()) {
 					if ("CN".equalsIgnoreCase(key)) {
 						cn = value;
 					}
 				}
 
 			}
 
 			if (cn != null && cn.trim().length() > 0) {
 				// use cn as uuid...
 				uuid = cn;
 			}
 
 			// store user
 			UserEntity user = new UserEntity();
 			user.setCategory(UserInfo.CATEGORY_INTERNAL);
 			user.setSerial(serial);
 			user.setCn(cn);
 			user.setGn(gn);
 			user.setSn(sn);
 			user.setDn(dn);
 			user.setUuid(uuid);
 			userInfo = taskFormDb.createUser(user);
 
 			// create user in BOS engine
 			if (!bonitaClient.createUser(uuid)) {
 				log.severe("Failed to create user in BOS engine");
 			}
 		}
 
 		return userInfo;
 	}
 
 	public UserInfo getUserBySerial(String serial, String certificateSubject) {
 		UserInfo userInfo = taskFormDb.getUserBySerial(serial);
 
 		if (userInfo == null) {
 			// new user in system
 
 			String gn = null, sn = null, cn = null;
 			String uuid = java.util.UUID.randomUUID().toString();
 			String dn = null;
 
 			// try to parse certificateSubject
 			StringTokenizer st = new StringTokenizer(certificateSubject, ",");
 			while (st.hasMoreTokens()) {
 				String s = st.nextToken();
 
 				StringTokenizer valuePairToken = new StringTokenizer(s, "=");
 				String key = null;
 				String value = null;
 				if (valuePairToken.hasMoreTokens()) {
 					key = valuePairToken.nextToken().trim();
 				}
 				if (valuePairToken.hasMoreTokens()) {
 					value = valuePairToken.nextToken().trim();
 				}
 				if (!valuePairToken.hasMoreTokens()) {
 					if ("CN".equalsIgnoreCase(key)) {
 						cn = value;
 					}
 					if ("GIVENNAME".equalsIgnoreCase(key)) {
 						gn = value;
 					}
 					if ("SURNAME".equalsIgnoreCase(key)) {
 						sn = value;
 					}
 				}
 
 			}
 			// store user
 			UserEntity user = new UserEntity();
 			user.setCategory(UserInfo.CATEGORY_EXTERNAL);
 			user.setSerial(serial);
 			user.setCn(cn);
 			user.setGn(gn);
 			user.setSn(sn);
 			user.setDn(dn);
 			user.setUuid(uuid);
 			userInfo = taskFormDb.createUser(user);
 
 			// create user in BOS engine
 			if (!bonitaClient.createUser(uuid)) {
 				log.severe("Failed to create user in BOS engine");
 			}
 		}
 
 		return userInfo;
 	}
 
 }
