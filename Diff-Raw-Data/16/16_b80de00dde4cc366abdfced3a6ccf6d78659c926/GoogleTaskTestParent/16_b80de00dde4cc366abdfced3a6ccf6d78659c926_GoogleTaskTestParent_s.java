 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.google.task.automation.testcases;
 
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.Timeout;
 import org.mule.api.MuleEvent;
 import org.mule.api.config.MuleProperties;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.api.store.ObjectStore;
 import org.mule.api.store.ObjectStoreException;
 import org.mule.module.google.task.model.Task;
 import org.mule.module.google.task.model.TaskList;
import org.mule.module.google.task.oauth.GoogleTasksConnectorOAuthState;
 import org.mule.tck.junit4.FunctionalTestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class GoogleTaskTestParent extends FunctionalTestCase {
 	
 	// Set global timeout of tests to 10minutes
     @Rule
     public Timeout globalTimeout = new Timeout(600000);
     
 	protected static final String[] SPRING_CONFIG_FILES = new String[] { "AutomationSpringBeans.xml" };
 	protected static ApplicationContext context;
 	protected Map<String, Object> testObjects;
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Before
 	public void init() throws ObjectStoreException {
		ObjectStore objectStore = muleContext.getRegistry().lookupObject(MuleProperties.DEFAULT_USER_OBJECT_STORE_NAME);
		objectStore.store("accessTokenId", (GoogleTasksConnectorOAuthState)context.getBean("connectorOAuthState"));
 	}
 	
 	@Override
 	protected String getConfigResources() {
 		return "automation-test-flows.xml";
 	}
 
	protected MessageProcessor lookupFlowConstruct(String name) {
		return (MessageProcessor) muleContext.getRegistry()
				.lookupFlowConstruct(name);
	}
 
 	@BeforeClass
 	public static void beforeClass() {
 		context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
 	}
 	
 	protected void setTestObjects(Map<String, Object> testObjects) {
 		this.testObjects = testObjects;
 	}
 	
 	@SuppressWarnings("unchecked")
 	/**
 	 * All tasks lists which have the title taskListTitle will be deleted. The method returns the number of
 	 * lists deleted.
 	 * 
 	 * @param taskListTitle the task title
 	 * @return the number of lists deleted
 	 */
 	protected int deleteAllListsByTaskListTitle(String taskListTitle) {
 		if(taskListTitle == null) {
 			return 0;
 		}
 		
 		MuleEvent getTaskListResponse = null;
 		int count = 0;
 		try {
 			getTaskListResponse = lookupFlowConstruct("get-task-lists").process(getTestEvent(testObjects));
 			List<TaskList> tasks = (List<TaskList>) getTaskListResponse.getMessage().getPayload();
 			
 			for(TaskList task : tasks) {
 				if(taskListTitle.equals(task.getTitle())) {
 					testObjects.put("taskListId", task.getId());
 					lookupFlowConstruct("delete-task-list").process(getTestEvent(testObjects));
 					count++;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		return count;
 	}
 	
 	protected Task insertTask(Task task, TaskList taskList) throws Exception {
 		return insertTask(task, taskList.getId());
 	}
 
 	protected Task insertTask(Task task, String taskListId) throws Exception {
 		testObjects.put("taskRef", task);
 		testObjects.put("taskListId", taskListId);
 		
 		MessageProcessor flow = lookupFlowConstruct("insert-task");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Task) response.getMessage().getPayload();
 	}
 	
 }
