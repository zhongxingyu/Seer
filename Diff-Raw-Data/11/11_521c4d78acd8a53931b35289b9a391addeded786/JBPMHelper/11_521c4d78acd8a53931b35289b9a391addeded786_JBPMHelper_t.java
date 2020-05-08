 package com.duggan.workflow.server.helper.jbpm;
 
 import static com.duggan.workflow.server.helper.dao.DocumentDaoHelper.getDocument;
 
 import java.io.Closeable;
 import java.io.ObjectInputStream;
 import java.io.OptionalDataException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.drools.definition.process.Node;
 import org.drools.runtime.process.ProcessInstance;
 import org.jbpm.process.audit.JPAProcessInstanceDbLog;
 import org.jbpm.process.audit.NodeInstanceLog;
 import org.jbpm.process.audit.ProcessInstanceLog;
 import org.jbpm.process.core.Process;
 import org.jbpm.task.AccessType;
 import org.jbpm.task.Comment;
 import org.jbpm.task.Deadlines;
 import org.jbpm.task.Delegation;
 import org.jbpm.task.I18NText;
 import org.jbpm.task.OrganizationalEntity;
 import org.jbpm.task.PeopleAssignments;
 import org.jbpm.task.Status;
 import org.jbpm.task.Task;
 import org.jbpm.task.TaskData;
 import org.jbpm.task.User;
 import org.jbpm.task.query.TaskSummary;
 import org.jbpm.task.utils.ContentMarshallerHelper;
 import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
 import org.jbpm.workflow.core.node.EndNode;
 import org.jbpm.workflow.core.node.HumanTaskNode;
 import org.jbpm.workflow.core.node.StartNode;
 import org.jbpm.workflow.core.node.SubProcessNode;
 
 import com.duggan.workflow.client.model.TaskType;
 import com.duggan.workflow.server.dao.model.ADDocType;
 import com.duggan.workflow.server.dao.model.ProcessDefModel;
 import com.duggan.workflow.server.db.DB;
 import com.duggan.workflow.server.helper.auth.LoginHelper;
 import com.duggan.workflow.server.helper.dao.DocumentDaoHelper;
 import com.duggan.workflow.shared.exceptions.ProcessInitializationException;
 import com.duggan.workflow.shared.model.Actions;
 import com.duggan.workflow.shared.model.BooleanValue;
 import com.duggan.workflow.shared.model.DateValue;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentType;
 import com.duggan.workflow.shared.model.HTAccessType;
 import com.duggan.workflow.shared.model.HTComment;
 import com.duggan.workflow.shared.model.HTData;
 import com.duggan.workflow.shared.model.HTStatus;
 import com.duggan.workflow.shared.model.HTSummary;
 import com.duggan.workflow.shared.model.HTUser;
 import com.duggan.workflow.shared.model.HTask;
 import com.duggan.workflow.shared.model.LongValue;
 import com.duggan.workflow.shared.model.NodeDetail;
 import com.duggan.workflow.shared.model.SearchFilter;
 import com.duggan.workflow.shared.model.StringValue;
 import com.duggan.workflow.shared.model.UserGroup;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.ProcessMappings;
 
 /**
  * This is a Helper Class for all JBPM associated requests. It provides utility
  * methods to execute tasks and retrieve task information from the JBPM
  * environment
  * 
  * @author duggan
  * 
  */
 public class JBPMHelper implements Closeable {
 
 	private BPMSessionManager sessionManager;
 	private static JBPMHelper helper;
 	static Logger logger = Logger.getLogger(JBPMHelper.class);
 
 	private JBPMHelper() {
 		try {
 			// By Setting the jbpm.usergroup.callback property with the call
 			// back class full name, task service will use this to validate the
 			// user/group exists and its permissions are ok.
 			// System.setProperty("jbpm.usergroup.callback",
 			// "org.jbpm.task.identity.LDAPUserGroupCallbackImpl");
 
 			System.setProperty("jbpm.usergroup.callback",
 					"org.jbpm.task.identity.DBUserGroupCallbackImpl");
 			sessionManager = new BPMSessionManager();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	// not thread safe
 	public static JBPMHelper get() {
 		if (helper == null) {
 			helper = new JBPMHelper();
 		}
 
 		return helper;
 	}
 
 	@Override
 	public void close() {
 		assert sessionManager != null;
 
 		sessionManager.disposeSessions();
 	}
 
 	/**
 	 * This method clears the runtime environment when the application is
 	 * shutdown
 	 * 
 	 */
 	public static void clearRequestData() {
 		JBPMHelper h = JBPMHelper.get();
 
 		if (h != null) {
 			try {
 				h.close();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * This method creates initiates an approval process for a document.
 	 * 
 	 * @param summary
 	 *            This is the document to be submitted for approval
 	 */
 	public void createApprovalRequest(String userId, Document summary) {
 
 		Map<String, Object> initialParams = new HashMap<String, Object>();
 		// initialParams.put("user_self_evaluation", "calcacuervo");
 //		initialParams.put("subject", summary.getSubject());// Human Tasks need
 //															// this
 //		initialParams.put("description", summary.getDescription());// Human
 																	// Tasks
 																	// need this
 		initialParams.put("documentId", summary.getId().toString());
 		initialParams.put("ownerId", userId);
 		//initialParams.put("value", summary.getValue());
 		initialParams.put("priority", summary.getPriority());
 		
 		Document clone = summary.clone();
 		initialParams.put("document", clone);
		//System.err.println("[Document lines = "+summary.getDetails()+"]");
 		Map<String, Value> vals = summary.getValues();
 		Collection<Value> values = vals.values();
 		
 		for (Value val : values) {
 			if(val!=null){
 				initialParams.put(val.getKey(), val.getValue());
 			}
 		}
 		
 		ProcessInstance processInstance = sessionManager.startProcess(
 				getProcessId(summary.getType()), initialParams, summary);
 
 		// processInstance.getId(); - Use this to link a document with a process
 		// instance + later for history generation
 		// summary.setProcessInstanceId(processInstance.getId());
 		// DocumentDaoHelper.save(summary);
 
 		assert (ProcessInstance.STATE_ACTIVE == processInstance.getState());
 
 	}
 
 	public String getProcessId(DocumentType type) {
 
 		ADDocType adtype = DocumentDaoHelper.getType(type);
 		List<ProcessDefModel> processDefs = DB.getProcessDao()
 				.getProcessesForDocType(adtype);
 
 		if (processDefs == null || processDefs.isEmpty()) {
 			throw new ProcessInitializationException(
 					"Could not start process: "
 							+ "No process definition found for DocType= ["
 							+ type + "]");
 		}
 
 		if (processDefs.size() > 1) {
 			throw new ProcessInitializationException(
 					"Could not start process: More than 1 process definition "
 							+ "found for document [" + type + "]");
 		}
 
 		Iterator<ProcessDefModel> iter = processDefs.iterator();
 		ProcessDefModel model = null;
 		while (iter.hasNext()) {
 			model = iter.next();
 		}
 
 		String processId = model.getProcessId();
 
 		ProcessMigrationHelper.start(model, false);
 
 		return processId;
 	}
 
 	/**
 	 * Count the number of tasks - completed/ or new
 	 * 
 	 * @param userId
 	 * @param counts
 	 */
 	public void getCount(String userId, HashMap<TaskType, Integer> counts) {
 
 		List<UserGroup> groups = LoginHelper.getHelper().getGroupsForUser(
 				userId);
 		List<String> groupIds = new ArrayList<>();
 		for (UserGroup group : groups) {
 			groupIds.add(group.getName());
 		}
 
 		Long count = (Long) DB
 				.getEntityManager()
 				.createNamedQuery(
 						"TasksAssignedCountAsPotentialOwnerByStatusWithGroups")
 				.setParameter("userId", userId)
 				.setParameter("groupIds", groupIds)
 				.setParameter("language", "en-UK")
 				.setParameter("status",
 						getStatusesForTaskType(TaskType.APPROVALREQUESTNEW))
 				.getSingleResult();
 		counts.put(TaskType.APPROVALREQUESTNEW, count.intValue());
 
 		/**
 		 * If John & James share the Role HOD_DEV John Claims, Starts and
 		 * completes a task, should that task be presented to James as one of
 		 * his completed tasks? This mechanism creates the posibility of this
 		 * scenario happening TODO: Test the query with two users sharing a role
 		 */
 		Long count2 = (Long) DB.getEntityManager()
 				.createNamedQuery("TasksOwnedCount")
 				.setParameter("userId", userId)
 				.setParameter("language", "en-UK")
 				.setParameter("status", Status.Completed).getSingleResult();
 		counts.put(TaskType.APPROVALREQUESTDONE, count2.intValue());
 
 	}
 
 	public List<Status> getStatusesForTaskType(TaskType type) {
 
 		List<Status> statuses = new ArrayList<>();
 
 		switch (type) {
 		case APPROVALREQUESTNEW:
 			statuses = Arrays.asList(Status.Created, Status.InProgress,
 			// Status.Error,
 			// Status.Exited,
 			// Status.Failed,
 			// Status.Obsolete,
 					Status.Ready, Status.Reserved, Status.Suspended);
 			break;
 		case APPROVALREQUESTDONE:
 			statuses = Arrays.asList(Status.Completed);
 			break;
 		}
 		return statuses;
 	}
 
 	public List<HTSummary> getTasksForUser(String userId, Long processInstanceId) {
 		List<UserGroup> groups = LoginHelper.getHelper().getGroupsForUser(
 				userId);
 		List<String> groupIds = new ArrayList<>();
 		for (UserGroup group : groups) {
 			groupIds.add(group.getName());
 		}
 
 		@SuppressWarnings("unchecked")
 		List<TaskSummary> ts = DB.getEntityManager()
 				.createNamedQuery("TasksOwnedBySubject")
 				.setParameter("userId", userId)
 				.setParameter("language", "en-UK")
 				.setParameter("groupIds", groupIds)
 				.setParameter("processInstanceId", processInstanceId)
 				.getResultList();
 
 		return translateSummaries(ts);
 	}
 
 	public List<HTSummary> searchTasks(String userId, SearchFilter filter) {
 		List<TaskSummary> tasks = DB.getDocumentDao().searchTasks(userId,
 				filter);
 
 		return translateSummaries(tasks);
 	}
 
 	/**
 	 * 
 	 * @param processId
 	 * @param taskName
 	 * @return Input/Output parameter mappings for tasks
 	 */
 	public Collection<String> getProcessData(String processId, String taskName){
 		org.drools.definition.process.Process process = sessionManager.getProcess(processId);
 		
 		WorkflowProcessImpl workflow = (WorkflowProcessImpl)process;
 		
 		Node [] nodes = workflow.getNodes();
 		
 		for(Node node: nodes){
 			
 			if(node instanceof HumanTaskNode){
 				HumanTaskNode htnode = (HumanTaskNode)node;
 				Object name = htnode.getWork().getParameter("TaskName");
 				
 				if(name!=null && name.equals(taskName)){					
 					Collection<String> vals = htnode.getInMappings().values();
 					return vals;
 				}
 					
 					
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * This method retrieves all tasks assigned to a user.
 	 * <p>
 	 * 
 	 * @param userId
 	 *            This is the username of the user whose tasks are to be
 	 *            retrieved
 	 * @param type
 	 * @return List This is a list of human task summaries retrieved for the
 	 *         user
 	 * 
 	 */
 	public List<HTSummary> getTasksForUser(String userId, TaskType type) {
 
 		if (!LoginHelper.get().existsUser(userId)) {
 			throw new RuntimeException("User " + userId + " Unknown!!");
 		}
 
 		if (type == null) {
 			type = TaskType.APPROVALREQUESTNEW;
 		}
 
 		List<TaskSummary> ts = new ArrayList<>();
 
 		switch (type) {
 		case APPROVALREQUESTDONE:
 			// approvals - show only items I have approved
 			ts = sessionManager.getTaskClient().getTasksOwned(userId,
 					Arrays.asList(Status.Completed), "en-UK");
 
 			break;
 		case APPROVALREQUESTNEW:
 			ts = sessionManager.getTaskClient()
 					.getTasksAssignedAsPotentialOwner(userId, "en-UK");
 			break;
 
 		default:
 			break;
 		}
 
 		return translateSummaries(ts);
 
 	}
 
 	public List<HTSummary> translateSummaries(List<TaskSummary> ts) {
 
 		List<HTSummary> tasks = new ArrayList<>();
 		for (TaskSummary summary : ts) {
 
 			HTSummary task = new HTSummary(summary.getId());
 			Task master_task = sessionManager.getTaskClient().getTask(
 					summary.getId());
 
 			copy(task, master_task);
 			tasks.add(task);
 			// call for test
 			// getTask(userId, summary.getId());
 
 		}
 
 		return tasks;
 	}
 
 	private void copy(HTSummary task, Task master_task) {
 
 		Map<String, Object> content = getMappedData(master_task);
 		Document doc = getDocument(content);
 
 		assert doc != null;
 
 		task.setCreated(master_task.getTaskData().getCreatedOn());
 		task.setDateDue(master_task.getTaskData().getCreatedOn());
 		task.setSubject(doc.getSubject());
 		task.setDescription(doc.getDescription());
 		task.setPriority(doc.getPriority());
 		task.setDocumentRef(doc.getId());
 		task.setDetails(doc.getDetails());
 		task.setValues(doc.getValues());
 		// task.setTaskName(summary.getName()); //TODO: LOOK INTO JBPM
 		// TASKSUMMARY / TASK USAGES
 		// task.setTaskName(master_task.getNames().);
 
 		Status status = master_task.getTaskData().getStatus();
 		task.setStatus(HTStatus.valueOf(status.name().toUpperCase()));
 
 		List<I18NText> names = master_task.getNames();
 
 		if (names.size() > 0) {
 			task.setTaskName(names.get(0).getText());
 		}
 
 		task.setProcessInstanceId(master_task.getTaskData()
 				.getProcessInstanceId());
 
 		if (task instanceof HTask) {
 //			Set<String> keys = content.keySet();
 //
 //			for (String key : keys) {
 //				Value val = getValue(content.get(key));
 //				if(val!=null)
 //					task.setValue(key, val);
 //			}
 			
 			task.setPriority(doc.getPriority());
 			task.setDocumentDate(doc.getDocumentDate());
 			task.setDocStatus(doc.getStatus());
 			//task.setDocumentType(document.getType());
 			task.setOwner(doc.getOwner());
 		
 		}
 	}
 
 	private Value getValue(Object val) {
 
 		Value value = null;
 		
 		if (val instanceof Boolean) {
 			value = new BooleanValue((Boolean)val);
 		}
 
 		if (val instanceof String) {
 			value = new StringValue(val.toString());
 		}
 
 		if (val instanceof Integer) {
 			Long longVal = new Long((Integer)val);
 			value = new LongValue(longVal);
 		}
 		if (val instanceof Long) {
 			value = new LongValue((Long)val);
 		}
 		
 		if (val instanceof Date) {
 			value = new DateValue((Date)val);
 		}
 		
 		if(val instanceof Value){
 			value=(Value)val;
 		}
 		
 		return value;
 	}
 
 	/**
 	 * This method retrieves the full task object, which provides more
 	 * comprehensive details for a task
 	 * 
 	 * @param taskId
 	 *            This is the task Id of the task to be retrieved
 	 * @return HTask Human Task DTO object retrieved
 	 */
 	public HTask getTask(long taskId) {
 
 		// Human Task
 		HTask myTask = new HTask();
 
 		Task task = sessionManager.getTaskClient().getTask(taskId);
 		copy(myTask, task);
 		// task.get
 
 		List<I18NText> descriptions = task.getDescriptions();
 		if(myTask.getDescription()==null)
 		myTask.setDescription(descriptions.get(0).getText());
 
 		List<I18NText> names = task.getNames();
 		if(names.size()>0){
 			String taskName = names.get(0).getText();
 			myTask.setName(taskName);
 		}
 
 		List<I18NText> subjects = task.getSubjects();// translations
 		if(myTask.getSubject()==null)
 		myTask.setSubject(subjects.get(0).getText());
 
 		int priority = task.getPriority();
 		if(myTask.getPriority()==null)
 			myTask.setPriority(priority);
 
 		Long id = task.getId();
 		myTask.setId(id);
 		
 		if(task.getTaskData().getStatus()== Status.Completed){
 			long output = task.getTaskData().getOutputContentId();
 			Map<String, Object> content = getMappedData(output);
 			Set<String> keys = content.keySet();
 
 			for (String key : keys) {
 				Value val = getValue(content.get(key));
 				if(val!=null)
 					myTask.setValue(key, val);
 			}
 			
 		}
 		
 		// int version = task.getVersion();
 		// myTask.setVersion(version);
 
 		Deadlines deadlines = task.getDeadlines();
 		// deadlines.getEndDeadlines();
 		Delegation delegation = task.getDelegation();
 
 		PeopleAssignments assignments = task.getPeopleAssignments();
 		// User user = assignments.getTaskInitiator();
 
 		List<OrganizationalEntity> entities = assignments.getRecipients();
 
 		// HT DATA
 		HTData data = new HTData();
 
 		// TASK DATA
 		TaskData taskData = task.getTaskData();
 		String docType = taskData.getDocumentType();
 		data.setDocType(docType);
 
 		long workId = taskData.getWorkItemId();
 		data.setWorkId(workId);
 
 		// owner
 		User actualOwner = taskData.getActualOwner();
 		if (actualOwner != null) {
 			HTUser taskOwner = new HTUser(actualOwner.getId());
 			data.setActualOwner(taskOwner);
 		}
 
 		// comments
 		List<Comment> comments = taskData.getComments();
 		if (comments != null)
 			for (Comment comment : comments) {
 				HTComment htComment = new HTComment();
 				htComment.setAddedAt(comment.getAddedAt());
 				htComment.setId(comment.getId());
 				htComment.setText(comment.getText());
 
 				User addedBy = comment.getAddedBy();
 				if (addedBy != null)
 					htComment.setAddedBy(new HTUser(addedBy.getId()));
 
 			}
 
 		Date completedOn = taskData.getCompletedOn();
 		data.setCompletedOn(completedOn);
 
 		User createdBy = taskData.getCreatedBy();
 		if (createdBy != null) {
 			data.setCreatedBy(new HTUser(createdBy.getId()));
 		}
 
 		AccessType accessType = taskData.getDocumentAccessType();
 
 		if (accessType != null) {
 			data.setDocAccessType(HTAccessType.valueOf(accessType.name()
 					.toUpperCase()));
 		}
 
 		long contentId = taskData.getDocumentContentId();
 		data.setContentId(contentId);
 
 		Status taskDataStatus = taskData.getStatus();
 		if (taskDataStatus != null) {
 			data.setStatus(HTStatus
 					.valueOf(taskDataStatus.name().toUpperCase()));
 		}
 
 		String taskDataOutputType = taskData.getOutputType();
 		data.setOutputType(taskDataOutputType);
 
 		Date expiryTime = taskData.getExpirationTime();
 		data.setExpiryTime(expiryTime);
 
 		// AccessType faultAccessType = taskData.getFaultAccessType();
 
 		long parentId = taskData.getParentId();
 		data.setParentId(parentId);
 
 		Status previousStatus = taskData.getPreviousStatus();
 		if (previousStatus != null) {
 			data.setPreviousStatus(HTStatus.valueOf(previousStatus.name()
 					.toUpperCase()));
 		}
 
 		// myTask.setData(data);
 
 		return myTask;
 	}
 
 	public static Map<String, Object> getMappedData(Task task) {
 
 		Long contentId = task.getTaskData() == null ? null : task.getTaskData()
 				.getDocumentContentId();		
 		
 		return get().getMappedData(contentId);
 	}
 
 	public static Map<String, Object> getMappedDataByContentId(Long contentId) {
 		return get().getMappedData(contentId);
 	}
 
 	/**
 	 * <p>
 	 * This method returns the Values passed when the task was initiated.
 	 * Several methods of retrieving these parameters are offered online but the
 	 * use of {@link ContentMarshallerHelper} is what worked in this instance
 	 * 
 	 * <p>
 	 * The use of {@link ObjectInputStream} to read the bytes failed with an
 	 * {@link OptionalDataException}; trying to fix this did not work.
 	 * 
 	 * <p>
 	 * Further, if the Content value of the JBPM task(<i>JBPM Task
 	 * properties</i>) is set, it overrides any inputs(map) passed to the
 	 * process when the task is created
 	 * {@link #createApprovalRequest(HTSummary)}
 	 * 
 	 * <p>
 	 * 
 	 * @param task
 	 * @return Parameter-Value Map for a task
 	 */
 	private Map<String, Object> getMappedData(Long contentId) {
 
 		Map<String, Object> params = null;
 
 		if (contentId == null) {
 			return params;
 		}
 
 		params = new HashMap<>();
 
 		byte[] objectinBytes = sessionManager.getTaskClient()
 				.getContent(contentId).getContent();
 
 		assert objectinBytes.length > 0;
 
 		ObjectInputStream is = null;
 		try {
 			// is = new ObjectInputStream(new
 			// ByteArrayInputStream(objectinBytes));
 			Object o = ContentMarshallerHelper.unmarshall(objectinBytes, null);
 
 			if (o instanceof Map) {
 				params = (Map<String, Object>) o;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (Exception e) {
 				}
 			}
 
 		}
 
 		return params;
 	}
 
 	/**
 	 * This method provides a generic way for task execution.
 	 * 
 	 * @param taskId
 	 *            This is the taskId of the task
 	 * @param userId
 	 *            This is the user executing the task
 	 * @param action
 	 *            This is the action to be executed
 	 */
 	public void execute(long taskId, String userId, Actions action,
 			Map<String, Object> values) {
 
 		sessionManager.execute(taskId, userId, action, values);
 
 	}
 
 	public List<NodeDetail> getWorkflowProcessDia(long processInstanceId) {
 
 		ProcessInstanceLog log = JPAProcessInstanceDbLog
 				.findProcessInstance(processInstanceId);
 
 		if (log == null) {
 			// --
 			logger.warn("Invalid State : ProcessInstanceLog is null; ProcessInstanceId="
 					+ processInstanceId
 					+ "; Document = "
 					+ DocumentDaoHelper
 							.getDocumentByProcessInstance(processInstanceId));
 			return new ArrayList<>();
 		}
 
 		return getWorkflowProcessDia(log);
 	}
 
 	public List<NodeDetail> getWorkflowProcessDia(ProcessInstanceLog log) {
 
 		List<NodeDetail> details = new ArrayList<>();
 
 		long processInstanceId = log.getProcessInstanceId();
 
 		String processDefId = log.getProcessId();
 		// JPAProcessInstanceDbLog.findNodeInstances(processInstanceId);
 
 		org.drools.definition.process.Process process = sessionManager
 				.getProcess(processDefId);
 
 		WorkflowProcessImpl wfprocess = (WorkflowProcessImpl) process;
 
 		for (Node node : wfprocess.getNodes()) {
 
 			long nodeId = node.getId();
 			List<NodeInstanceLog> nodeLogInstance = JPAProcessInstanceDbLog
 					.findNodeInstances(processInstanceId,
 							new Long(nodeId).toString());
 
 			if (nodeLogInstance.size() > 0) {
 				// Executed nodes only
 				Object x = node.getMetaData().get("x");
 				Object y = node.getMetaData().get("x");
 				Object width = node.getMetaData().get("width");
 				Object height = node.getMetaData().get("height");
 
 				if (node instanceof SubProcessNode) {
 					SubProcessNode n = (SubProcessNode) node;
 					List<ProcessInstanceLog> list = JPAProcessInstanceDbLog
 							.findSubProcessInstances(processInstanceId);
 
 					for (ProcessInstanceLog subprocess : list) {
 						// n.get
 						// if(n.getProcessId().equals(
 						// subprocess.getProcessId())){
 						Long subprocessId = subprocess.getId();
 						assert !subprocessId.equals(processInstanceId);
 
 						details.addAll(getWorkflowProcessDia(subprocess));
 						// }
 					}
 				}
 				
 				// System.err.println(node.getName());
 				// Ignore all other nodes - Only work pick human Task Nodes
 				if (node instanceof HumanTaskNode || node instanceof StartNode
 						|| node instanceof EndNode) {
 					// HumanTaskNode ht = (HumanTaskNode) node;
 					assert nodeLogInstance.size() == 2;
 					NodeDetail detail = new NodeDetail();
 					String name = node.getName();
 					detail.setName(name);
 					detail.setStartNode(node instanceof StartNode);
 					detail.setEndNode(node instanceof EndNode);
 					detail.setCurrentNode(nodeLogInstance.size() == 1);
 
 					details.add(detail);
 
 					//((HumanTaskNode)node).getOutMappings().get("isApproved");
 				}
 			}
 		}
 
 		return details;
 
 	}
 
 	public HTSummary getSummary(Long taskId) {
 
 		HTSummary summary = new HTSummary(taskId);
 
 		Task master_task = sessionManager.getTaskClient().getTask(taskId);
 
 		copy(summary, master_task);
 
 		return summary;
 	}
 
 	/**
 	 * What happens to this process if you try to execute it and it fails/throws
 	 * an exception
 	 * 
 	 * @param processId
 	 * @param contextInfo
 	 */
 	public void startProcess(String processId, Map<String, Object> contextInfo) {
 		sessionManager.startProcess(processId, contextInfo);
 	}
 
 	public static String getProcessDetails(Long processInstanceId) {
 
 		ProcessInstanceLog log = JPAProcessInstanceDbLog
 				.findProcessInstance(processInstanceId);
 		String processId = log.getProcessId();
 
 		return "[Process " + processId + "; ProcessInstanceId "
 				+ processInstanceId + "L]";
 	}
 
 	public void loadKnowledge(byte[] bytes, String processName) {
 		sessionManager.loadKnowledge(bytes, processName);
 	}
 
 	public boolean isProcessingRunning(String processId) {
 		return sessionManager.isRunning(processId);
 	}
 
 	public void stop(String processId) {
 		sessionManager.unloadKnowledgeBase(processId);
 	}
 
 	public String getTaskName(Long taskId) {
 
 		Task task = sessionManager.getTaskClient().getTask(taskId);
 
 		String name = null;
 
 		if (task.getNames().size() > 0)
 			name = task.getNames().get(0).getText();
 
 		return name;
 	}
 	
 	public ProcessMappings getProcessDataMappings(String processId, String taskName){
 		ProcessMappings processData = new ProcessMappings();
 		org.drools.definition.process.Process droolsProcess = sessionManager.getProcess(processId);
 		WorkflowProcessImpl wfprocess = (WorkflowProcessImpl) droolsProcess;
 
 	//	System.err.println("Globals:: "+wfprocess.get);
 		
 		for (Node node : wfprocess.getNodes()) {
 			
 			if(node instanceof HumanTaskNode){
 				HumanTaskNode htnode = (HumanTaskNode) node;
 				Object nodeTaskName = htnode.getWork().getParameter("TaskName");
 				
 				if(nodeTaskName!=null)
 				if(nodeTaskName.equals(taskName)){
 					processData.setInputMappings(htnode.getInMappings());
 					processData.setOutMappings(htnode.getOutMappings());					
 				}
 				
 			}
 		}
 	
 		return processData;
 	}
 
 }
