 package com.craigstjean.workflow.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.ProcessInstance;
 import org.jbpm.task.identity.UserGroupCallback;
 import org.jbpm.task.identity.UserGroupCallbackManager;
 import org.jbpm.task.query.TaskSummary;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class WorkflowServiceImpl implements WorkflowService {
 	@Autowired
 	private JbpmService jbpmService;
 	
 	@Autowired
 	private RoomResolver roomResolver;
 	
 	public WorkflowServiceImpl() {
 		UserGroupCallbackManager.getInstance().setCallback(new UserGroupCallback() {
 			@Override
 			public boolean existsUser(String userId) {
 				return true;
 			}
 			
 			@Override
 			public boolean existsGroup(String groupId) {
 				return true;
 			}
 			
 			@Override
 			public List<String> getGroupsForUser(String userId, List<String> taskGroupIds, List<String> knownGroupIds) {
 				if (userId.endsWith("Customer")) {
 					List<String> result = new ArrayList<String>();
 					result.add("Customers");
 					return result;
 				} else if (userId.endsWith("Clerk")) {
 					List<String> result = new ArrayList<String>();
 					result.add("Clerks");
 					return result;
 				}
 				
 				List<String> result = new ArrayList<String>();
 				result.add("NONE");
 				return result;
 			}
 		});
 	}
 	
 	@Override
 	public Long startProcess() {
 		StatefulKnowledgeSession ksession = jbpmService.getSession("com/craigstjean/workflow/bpmn/hotel.bpmn");
 		
 		Map<String, Object> variables = new HashMap<String, Object>();
 		variables.put("roomResolver", roomResolver);
		variables.put("roomNumber", -1);
 		ProcessInstance processInstance = ksession.startProcess("com.craigstjean.workflow.bpmn.hotel", variables);
 		return processInstance.getId();
 	}
 	
 	@Override
 	public List<TaskSummary> getTasksForUser(String user) {
 		return jbpmService.getTaskService().getTasksAssignedAsPotentialOwner(user, "en-UK");
 	}
 	
 	@Override
 	public void startTask(String userId, Long taskId) {
 		jbpmService.getTaskService().start(taskId, userId);
 	}
 	
 	@Override
 	public void completeTask(String userId, Long taskId) {
 		jbpmService.getTaskService().complete(taskId, userId, null);
 	}
 }
