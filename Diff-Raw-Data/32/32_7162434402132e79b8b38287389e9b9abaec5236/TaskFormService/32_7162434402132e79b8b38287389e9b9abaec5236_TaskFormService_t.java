 package org.inherit.taskform.engine;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import org.hibernate.criterion.Restrictions;
 import org.inherit.bonita.client.BonitaEngineServiceImpl;
 import org.inherit.service.common.domain.ActivityInstanceItem;
 import org.inherit.service.common.domain.ActivityInstanceLogItem;
 import org.inherit.service.common.domain.ActivityInstancePendingItem;
 import org.inherit.service.common.domain.ActivityWorkflowInfo;
 import org.inherit.service.common.domain.CommentFeedItem;
 import org.inherit.service.common.domain.DashOpenActivities;
 import org.inherit.service.common.domain.InboxTaskItem;
 import org.inherit.service.common.domain.PagedProcessInstanceSearchResult;
 import org.inherit.service.common.domain.ProcessInstanceDetails;
 import org.inherit.service.common.domain.ProcessInstanceListItem;
 import org.inherit.service.common.domain.Tag;
 import org.inherit.service.common.domain.TimelineItem;
 import org.inherit.service.common.domain.UserInfo;
 import org.inherit.taskform.engine.persistence.TaskFormDb;
 import org.inherit.taskform.engine.persistence.entity.ActivityFormDefinition;
 import org.inherit.taskform.engine.persistence.entity.ProcessActivityFormInstance;
 import org.inherit.taskform.engine.persistence.entity.StartFormDefinition;
 import org.inherit.taskform.engine.persistence.entity.UserEntity;
 
 public class TaskFormService {
 	
 	public static final Logger log = Logger.getLogger(TaskFormService.class.getName());
 	
 	TaskFormDb taskFormDb;
 	BonitaEngineServiceImpl bonitaClient;
 	ActorSelectorDirUtils aSelectorDirUtils;
 	
 	public TaskFormService() {
 		taskFormDb = new TaskFormDb();
 		bonitaClient = new BonitaEngineServiceImpl();
 // TODO hostname,port and base DN should be resolved from configuration
 		aSelectorDirUtils = new ActorSelectorDirUtils("localhost",
 				"1389",
 				"ou=IDMGroups,OU=Organisation,OU=Malmo,DC=adm,DC=malmo,DC=se"); //Base DN
 		}	
 	
 	/**
 	 * 
 	 * @param userId
 	 * @param remainingDays atRisk activities is calculated as the number of activities that will be overdue within remainingDays
 	 * @return
 	 */
 	public DashOpenActivities getDashOpenActivitiesByUserId(String userId, int remainingDays) {
 		DashOpenActivities result = bonitaClient.getDashOpenActivitiesByUserId(userId, remainingDays);
 		return result;
 	}
 	
 	/**
 	 * Get inbox task items a) partially filled start forms, not started processes 
 	 * b) started process with tasks associated to userId, check if partially filled form exist
 	 * @param userId
 	 * @return
 	 */
 	public List<InboxTaskItem> getInboxTaskItems(String userId) {
 		// activityDefinitionUUID vill bonita form engine ha det? lägg i så fall till uppslag på lämplig plats....
 		List<InboxTaskItem> unsubmittedStartForms = new ArrayList<InboxTaskItem>();
 		
 		log.severe("=======> getInboxTaskItems " + userId); 
 		// Find partially filled not submitted forms
 		List<ProcessActivityFormInstance> pending;
 		pending = taskFormDb.getPendingProcessActivityFormInstances(userId);
 		
 		HashMap<String, ProcessActivityFormInstance> forms = new HashMap<String, ProcessActivityFormInstance>();
 		for (ProcessActivityFormInstance form : pending) {
 			log.severe("=======> PENDING form: " + form);
 			String activityInstanceUuid = form.getActivityInstanceUuid();
 			if (activityInstanceUuid != null && activityInstanceUuid.trim().length()>0) {
 				forms.put(activityInstanceUuid, form);
 			}
 			else {
 				// this is a pending start form i.e. no process instance exist 
 				InboxTaskItem startFormItem = new InboxTaskItem();
 				startFormItem.setProcessActivityFormInstanceId(null);
 				startFormItem.setActivityCreated(null); //TODO fill with timestamp ?
 				startFormItem.setActivityLabel("Ansökan"); // TODO bonita login 
 				startFormItem.setProcessActivityFormInstanceId(form.getProcessActivityFormInstanceId());
 				if (form.getStartFormDefinition()!=null) {
 					startFormItem.setProcessLabel("Inte inskickad"); //TODO
 					//startFormItem.setProcessLabel(bonitaClient.getProcessLabel(form.getStartFormDefinition().getProcessDefinitionUuid()));
 					startFormItem.setStartedByFormPath(form.getStartFormDefinition().getFormPath());
 				}
 				else {
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
 			ProcessActivityFormInstance startedByForm = taskFormDb.getSubmittedStartProcessActivityFormInstanceByProcessInstanceUuid(item.getProcessInstanceUuid());
 			if (startedByForm != null) {
 				item.setStartedByFormPath(startedByForm.getFormPath());
 			}
 			
 			ProcessActivityFormInstance form = forms.get(taskUuid);
 			if (form != null) {
 				// partial filled not submitted form exist for task	
 				// assign id to inbox item that can be used later to edit/view existing form
 				item.setProcessActivityFormInstanceId(form.getProcessActivityFormInstanceId());
 			
 				
 				forms.remove(form);
 			}
 			//else {
 				// no one has opened this activity form yet
 				// i.e. no ProcessActivityFormInstance has been stored in database so far
 				// The ProcessActivityFormInstance is created on first time 
 			//}
 		}
 		
 		// partially filled forms 
 		for (ProcessActivityFormInstance partForm : forms.values()) {
 			if (partForm.getProcessInstanceUuid() == null) {
 				// partially filled start form that will start a process if submitted
 
 				log.warning("Unexpected partially filled form=" + partForm);
 			}
 			else {
 				// there is a partially filled form associated to this user but the corresponding BPMN 
 				// activity is no longer associated to this user
 				log.info("This partial filled form is associated to a process/task instance but is not associated to an active task in user's inbox: " + partForm );
 			}
 		}
 		
 		inbox.addAll(unsubmittedStartForms);
 		
 		log.severe("=======> getInboxTaskItems " + userId + " size=" + (inbox==null ? 0 : inbox.size())); 
 		return inbox;
 	}
 	
 	public ProcessInstanceDetails getProcessInstanceDetails(String processInstanceUuid) {
 		ProcessInstanceDetails details = bonitaClient.getProcessInstanceDetails(processInstanceUuid);
 		appendTaskFormServiceData(details);
 		return details;
 	}
 	
 	public ProcessInstanceDetails getProcessInstanceDetailsByActivityInstance(String activityInstanceUuid) {
 		ProcessInstanceDetails details = bonitaClient.getProcessInstanceDetailsByActivityInstance(activityInstanceUuid);
 		appendTaskFormServiceData(details);
 		return details;
 	}
 	
 	private void appendTaskFormServiceData(ProcessInstanceDetails details) {
 		if (details != null) {
 			for (ActivityInstancePendingItem pendingItem : details.getPending()) {
 				// pending activity => set edit url if ProcessActivityFormInstance is initialized
 				ProcessActivityFormInstance activity = taskFormDb.getProcessActivityFormInstanceByActivityInstanceUuid(pendingItem.getActivityInstanceUuid());
 				if (activity != null) {
 					pendingItem.setFormUrl(activity.calcEditUrl());
 				}
 				appendCandidateUserInfo(pendingItem);
 				pendingItem.setAssignedUser(appendTaskFormServiceUserData(pendingItem.getAssignedUser()));
 			}
 			
 			for (TimelineItem timelineItem : details.getTimeline().getItems()) {
 				if (timelineItem instanceof ActivityInstanceLogItem) {
 				  ActivityInstanceLogItem logItem = (ActivityInstanceLogItem)timelineItem;
 					ProcessActivityFormInstance activity = taskFormDb.getProcessActivityFormInstanceByActivityInstanceUuid(logItem.getActivityInstanceUuid());
 					// log item => set view url
 					if (activity != null) {
 						String viewUrl = activity.calcViewUrl();
 						logItem.setFormUrl(viewUrl);
 						logItem.setViewUrl(viewUrl);
 					}
 				}
 			}
 			ActivityInstanceLogItem startLogItem = getStartFormActivityInstanceLogItem(details.getProcessInstanceUuid());
 			details.getTimeline().addAndSort(startLogItem);
 		}
 	}
 	
 	public int addComment(String activityInstanceUuid, String comment, String userId) {
 		int result = bonitaClient.addComment(activityInstanceUuid, comment, userId); 
 		return result;
 	}
 	
 	public List<CommentFeedItem> getCommentFeed(String activityInstanceUuid, String userId) {
 		List<CommentFeedItem> result = bonitaClient.getProcessInstanceCommentFeedByActivity(activityInstanceUuid); 
 		return result;
 	}
 	
 	public ActivityWorkflowInfo getActivityWorkflowInfo(String activityInstanceUuid) {
 		ActivityWorkflowInfo result = bonitaClient.getActivityWorkflowInfo(activityInstanceUuid); 
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo assignTask(String activityInstanceUuid, String userId) {
 		ActivityWorkflowInfo result = bonitaClient.assignTask(activityInstanceUuid, userId); 
 		appendTaskFormServiceData(result);
 		return result;
 	}
 	
 	private void appendTaskFormServiceData(ActivityWorkflowInfo activityWorkflowInfo) {
 		activityWorkflowInfo.setAssignedUser(appendTaskFormServiceUserData(activityWorkflowInfo.getAssignedUser()));
 	}
 	
 	public ActivityWorkflowInfo unassignTask(String activityInstanceUuid) {
 		ActivityWorkflowInfo result = bonitaClient.unassignTask(activityInstanceUuid); 
 		appendTaskFormServiceData(result);
 		return result;		
 	}
 
 	public ActivityWorkflowInfo addCandidate(String activityInstanceUuid, String userId) {
 		ActivityWorkflowInfo result = bonitaClient.addCandidate(activityInstanceUuid, userId); 
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo removeCandidate(String activityInstanceUuid, String userId) {
 		ActivityWorkflowInfo result = bonitaClient.removeCandidate(activityInstanceUuid, userId); 
 		appendTaskFormServiceData(result);
 		return result;
 	}
 
 	public ActivityWorkflowInfo setActivityPriority(String activityInstanceUuid, int priority) {
 		ActivityWorkflowInfo result = bonitaClient.setPriority(activityInstanceUuid, priority);
 		appendTaskFormServiceData(result);
 		return result;		
 	}
 
 	public ActivityInstanceLogItem getStartFormActivityInstanceLogItem(String processInstanceUuid) {
 		ActivityInstanceLogItem result = null;
 		if (processInstanceUuid != null) {
 			
 			ProcessActivityFormInstance startActivity = taskFormDb.getStartProcessActivityFormInstanceByProcessInstanceUuid(processInstanceUuid);
 			if (startActivity != null) {
 				result = new ActivityInstanceLogItem();
 				result.setViewUrl(startActivity.calcViewUrl());
 				result.setFormUrl(startActivity.calcViewUrl());
 				result.setEndDate(startActivity.getSubmitted());
 				result.setActivityLabel(startActivity.getFormPath());
 				result.setPerformedByUser(taskFormDb.getUserByUuid(startActivity.getUserId()));
 			}
 		}
 		
 		return result;
 	}
 
 	public String submitStartForm(String formPath, String docId, String userId) {
 		log.severe("formPath=[" + formPath + "] docId=[" + docId + "] userId=[" + userId + "]" );
 		// lookup the process this start form is configured to start.
 		StartFormDefinition startFormDef = taskFormDb.getStartFormDefinitionByFormPath(formPath);
 		String processDefinitionUUIDStr = startFormDef.getProcessDefinitionUuid();
 		
 		// start the process
 		String processInstanceUuid = bonitaClient.startProcess(processDefinitionUUIDStr, userId);
 		
 		// store the start activity 
 		ProcessActivityFormInstance activity = new ProcessActivityFormInstance();
 		activity.setFormDocId(docId);
 		activity.setStartFormDefinition(startFormDef);
 		activity.setFormPath(formPath);
 		activity.setProcessInstanceUuid(processInstanceUuid);
 		activity.setSubmitted(new Date());
 		activity.setUserId(userId);
 		taskFormDb.saveProcessActivityFormInstance(activity);
 		
 		return processInstanceUuid;
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
 		}
 		else {
 			log.severe(dbUi.getLabel());
 		}
 		return dbUi;
 	}
 	
 	private void appendCandidateUserInfo(ActivityInstancePendingItem activityInstancePendingItem) {
 		log.severe("UserInfo appending UserEntity data..."); 
 		if (activityInstancePendingItem.getCandidates() != null) {
 			Set<UserInfo> candidates = new TreeSet<UserInfo>();  
 			for (UserInfo ui : activityInstancePendingItem.getCandidates()) {
 				candidates.add(appendTaskFormServiceUserData(ui));
 			}
 			activityInstancePendingItem.setCandidates(candidates);
 		}
 	}
 	
 	private void apppendTaskFormServiceData (ActivityInstanceItem item, ProcessActivityFormInstance activityFormInstance) {
 		if (item instanceof ActivityInstanceLogItem) {
 			item.setFormUrl(activityFormInstance.calcViewUrl());
 		}
 		else if (item instanceof ActivityInstancePendingItem) {
 			ActivityInstancePendingItem aipi = (ActivityInstancePendingItem) item;
 			aipi.setFormUrl(activityFormInstance.calcEditUrl());
 			appendCandidateUserInfo(aipi);
 		}
 		
 		if (activityFormInstance.getProcessActivityFormInstanceId()!=null) {
 			item.setProcessActivityFormInstanceId(activityFormInstance.getProcessActivityFormInstanceId().longValue());
 		}
 	}
 	
 	public ActivityInstanceItem getActivityInstanceItem(String activityInstanceUuid, String userId) {
 		ActivityInstanceItem result = null;
 		ProcessActivityFormInstance formInstance = taskFormDb.getProcessActivityFormInstanceByActivityInstanceUuid(activityInstanceUuid);
 		
 		if (formInstance == null) {
 			result = this.initializeActivityForm(activityInstanceUuid, userId);
 		}
 		else {
 			result = bonitaClient.getActivityInstanceItem(activityInstanceUuid);
 			apppendTaskFormServiceData(result, formInstance);
 		}
 		return result;
 	}
 	 
 	public ActivityInstanceItem getActivityInstanceItem(Long processActivityFormInstanceId) {
 		ActivityInstanceItem result = null;
 		ProcessActivityFormInstance formInstance = taskFormDb.getProcessActivityFormInstanceById(processActivityFormInstanceId);		
 		if (formInstance != null) {
 			if (formInstance.isStartForm()) {
 				result = getStartFormActivityInstancePendingItem(formInstance.getFormDocId());
 			}
 			else {
 				result = bonitaClient.getActivityInstanceItem(formInstance.getActivityInstanceUuid());
 			}
 			apppendTaskFormServiceData(result, formInstance);
 		}
 		return result;
 	}
 	
 	public ActivityInstanceItem getStartActivityInstanceItem(String formPath, String userId) {
 		ActivityInstanceItem result = null;
 		ProcessActivityFormInstance formInstance = taskFormDb.getStartProcessActivityFormInstanceByFormPathAndUser(formPath, userId);
 		
 		if (formInstance == null) {
 			result = this.initializeStartForm(formPath, userId);
 		}
 		else {
 			result = getStartFormActivityInstancePendingItem(formInstance.getFormDocId());
 			apppendTaskFormServiceData(result, formInstance);
 		}
 		return result;
 	}
 
 	private ActivityInstanceItem getStartFormActivityInstancePendingItem(String formDocId) {
 		ProcessActivityFormInstance src =  taskFormDb.getProcessActivityFormInstanceByFormDocId(formDocId);
 		return processActivityFormInstance2ActivityInstancePendingItem(src);
 	}
 		
 	/**
 	 * This conversion is only valid on StartForms
 	 * @param src
 	 * @return
 	 */
 	private ActivityInstancePendingItem processActivityFormInstance2ActivityInstancePendingItem(ProcessActivityFormInstance src) {
 		// TODO this method name is confusing, it is only working on start forms...
 		ActivityInstancePendingItem dst = new ActivityInstancePendingItem();
 		try {
 			dst.setProcessDefinitionUuid(src.getStartFormDefinition().getProcessDefinitionUuid());
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
 		}
 		catch (RuntimeException re) {
 			log.severe("Cannot convert ProcessActivityFormInstance " + src + " to ActivityInstancePendingItem. RuntimeException: " + re);
 			throw re;
 		}
 			
 		return dst;
 	}
 
 	
 	private ActivityInstanceItem initializeActivityForm(String activityInstanceUuid, String userId) {
 		ActivityInstanceItem activity = bonitaClient.getActivityInstanceItem(activityInstanceUuid);
 		
 		log.severe("===> activity=" + activity);
 		StartFormDefinition startFormDefinition = taskFormDb.getStartFormDefinition(activity.getActivityDefinitionUuid(), activity.getProcessInstanceUuid());
 		log.severe("===> startFormDefinition=" + startFormDefinition);
 		ActivityFormDefinition activityFormDefinition = null;
 		Long startFormId = null;
 		if (startFormDefinition != null) {
 			startFormId = startFormDefinition.getStartFormDefinitionId();
 		}
 		log.severe("===> activityInstanceUuid=" + activityInstanceUuid + ", startFormId=" + startFormId + ", userId=" + userId);
 		activityFormDefinition = taskFormDb.getActivityFormDefinition(activity.getActivityDefinitionUuid(), startFormId);
 		
 		// generate a type 4 UUID
 		String docId = java.util.UUID.randomUUID().toString();
 		
 		// store the start activity 
 		ProcessActivityFormInstance activityInstance = new ProcessActivityFormInstance();
 		activityInstance.setFormDocId(docId);
 		activityInstance.setStartFormDefinition(startFormDefinition);
 		activityInstance.setFormPath(activityFormDefinition.getFormPath());
 		activityInstance.setProcessInstanceUuid(activity.getProcessInstanceUuid());
 		activityInstance.setActivityInstanceUuid(activity.getActivityInstanceUuid());
 		activityInstance.setUserId(userId);
 		taskFormDb.saveProcessActivityFormInstance(activityInstance);
 		
 		apppendTaskFormServiceData(activity, activityInstance);
 		return activity;
 	}
 	
 	private ActivityInstanceItem initializeStartForm(String formPath, String userId) {
 		log.severe("formPath=[" + formPath + "] userId=[" + userId + "]" );
 		
 		// lookup the process this start form is configured to start.
 		StartFormDefinition startFormDefinition = taskFormDb.getStartFormDefinitionByFormPath(formPath);
 		
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
 	}
 	
 	/**
 	 * submit form
 	 * @param docId
 	 * @param userId
 	 * @return confirmation form viewUrl. null if submission fails.
 	 */
 	public String submitActivityForm(String docId, String userId) {
 		String viewUrl = null;
 		ProcessActivityFormInstance activity = taskFormDb.getProcessActivityFormInstanceByFormDocId(docId);
 		
 		if (activity == null) {
 			log.severe("This should never happen :) cannot find activity with docId=[" + docId + "]" + " userId=[" + userId + "]");
 		}
 		else {			
 			Date tstamp = new Date();
 			activity.setSubmitted(tstamp);
 			activity.setUserId(userId);
 			
 			boolean success = false;
 			if (activity.isStartForm()) {
 				// start the process
 				String pDefUuid = activity.getStartFormDefinition().getProcessDefinitionUuid();
 				String processInstanceUuid = bonitaClient.startProcess(pDefUuid, userId);
 				activity.setProcessInstanceUuid(processInstanceUuid);
 				success = (processInstanceUuid!=null && processInstanceUuid.trim().length()>0);
 			}
 			else if(bonitaClient.executeTask(activity.getActivityInstanceUuid(), userId)) {
 				// execute activity task
 				success = true;
 			}
 			
 			if (success) {
 				// if process/task in BPM engine is started/executed successful 
 				// => update status of ProcessActivityFormInstance to submitted
 				taskFormDb.saveProcessActivityFormInstance(activity);
 				// calculate URL that can be used to render the confirmation
 				viewUrl = activity.calcViewUrl();				
 			}
 		}
 		return viewUrl;
 	}
 		
    /**
     * Find process instances that is started by a specified user
     * @param searchForUserId The user to search for. 
     * @param userId The user that is actually performing the search, make it possibly to exclude hits because of privacy reasons for instance 
     * @return
     */
     public List<ProcessInstanceListItem> searchProcessInstancesStartedByUser(String searchForUserId, String user) { 
 		List<ProcessInstanceListItem> result = bonitaClient.getProcessInstancesStartedBy(user);
 		appendProcessAndActivityLabelsFromTaskFormDb(result);
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
     public PagedProcessInstanceSearchResult searchProcessInstancesStartedByUser(String searchForUserId, int fromIndex, int pageSize, String sortBy, String sortOrder, String filter, String userId) { 
             PagedProcessInstanceSearchResult result = bonitaClient.getProcessInstancesStartedBy(searchForUserId, fromIndex, pageSize, sortBy, sortOrder, filter, userId);
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
            PagedProcessInstanceSearchResult result = bonitaClient.getProcessInstancesWithInvolvedUser(searchForUserId, fromIndex, pageSize, sortBy, sortOrder, filter, userId);
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
 
 	
 	public Tag addTag(Long processActivityFormInstanceId, Long tagTypeId, String value, String userId) {
 		return taskFormDb.addTag(processActivityFormInstanceId, tagTypeId, value, userId);
 	}
 
 	public boolean deleteTag(String processInstanceUuid, String value, String userId) {
 		return taskFormDb.deleteTag(processInstanceUuid, value, userId);
 	}
 	
 	public List<Tag> getTagsByProcessInstance(String processInstanceUuid) {
 		return taskFormDb.getTagsByProcessInstance(processInstanceUuid);
 	}
 	
 	public Set<String> getUsersByRoleAndActivity(String roleName, String activityInstanceUuid) {
 	//TODO implement getDepartmentByactivityInstanceUuid, for now just hardwire "Miljöförvaltningen"
 		Set<String> result =  null;
 				try {
 				    	result =   
 					aSelectorDirUtils.getUsersByDepartmentAndRole("Miljöförvaltningen", roleName); 
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
 			
 			if (cn!=null && cn.trim().length()>0) {
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
