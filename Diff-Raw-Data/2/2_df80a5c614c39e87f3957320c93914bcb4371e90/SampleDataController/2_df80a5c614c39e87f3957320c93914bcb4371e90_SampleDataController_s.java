 package com.crowdplatform.controller;
 
 import java.security.SecureRandom;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.crowdplatform.model.Batch;
 import com.crowdplatform.model.BatchExecutionCollection;
 import com.crowdplatform.model.Execution;
 import com.crowdplatform.model.Field;
 import com.crowdplatform.model.PlatformUser;
 import com.crowdplatform.model.Project;
 import com.crowdplatform.model.Task;
 import com.crowdplatform.model.UserDataField;
 import com.crowdplatform.model.UserDataField.UserDataFieldAggregationType;
 import com.crowdplatform.service.BatchExecutionService;
 import com.crowdplatform.service.PlatformUserService;
 import com.crowdplatform.service.ProjectService;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 @Controller
 @RequestMapping("/sample")
 public class SampleDataController {
 
 	@Autowired
 	private PlatformUserService userService;
 	
 	@Autowired
 	private ProjectService projectService;
 	
 	@Autowired
 	private BatchExecutionService batchService;
 	
 	private Random random = new Random();
 	
 	@RequestMapping("/")
 	public String createSampleData(Map<String, Object> map) {
 		createSampleProject();
 		
 		return "redirect:/projects";
 	}
 	
 	@RequestMapping("/clean")
 	public String cleanSampleData() {
 		List<PlatformUser> users = userService.listUsers();
 		for (PlatformUser user : users) {
 			List<Project> projects = projectService.getProjectsForUser(user.getUsername());
 			for (Project project : projects) {
 				projectService.removeProject(project.getId());
 			}
 			userService.removeUser(user.getUsername());
 		}
 		List<BatchExecutionCollection> collections = batchService.listCollections();
 		for (BatchExecutionCollection collection : collections) {
			batchService.removeCollection(collection);
 		}
 		return "redirect:/projects";
 	}
 	
 	private static Map<String, Object> mapDefinition = Maps.newHashMap();
 	private static final Batch.State[] states = {Batch.State.RUNNING, Batch.State.PAUSED};
 	
 	private void createSampleProject() {
 		createMapDefinition();
 		
 		Project project = new Project();
 		project.setName("Dyseggxia project");
 		SecureRandom random = new SecureRandom();
 		project.setUid(random.nextLong());
 		createInputFields(project);
 		createOutputFields(project);
 		createUserFields(project);
 		createUserDataFields(project);
 		projectService.addProject(project);
 		
 		for (int i = 0; i < 1; ++i) {
 			Batch batch = createSampleBatch(i + 1);
 			project.addBatch(batch);
 		}
 		
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 	    if (auth != null) {
 	    	String username = auth.getName();
 		    PlatformUser user = userService.getUser(username);
 		    project.setOwnerId(user.getUsername());
 		    userService.saveUser(user);
 	    }
 	    projectService.saveProject(project);
 	}
 	
 	private void createMapDefinition() {
 		mapDefinition.put("id", 4);
 		mapDefinition.put("answers", Lists.newArrayList("ua", "ie", "i", "ei"));
 		mapDefinition.put("word", "actually");
 		mapDefinition.put("level", 1);
 		mapDefinition.put("type", "substitution");
 		mapDefinition.put("language", "EN");
 		mapDefinition.put("display", "act|ai|lly");
 	}
 	
 	private void createInputFields(Project project) {
 		Field field0 = new Field();
 		field0.setName("id");
 		field0.setType(Field.Type.INTEGER);
 		field0.setFieldType(Field.FieldType.INPUT);
 		project.addInputField(field0);
 		Field field1 = new Field();
 		field1.setName("type");
 		field1.setType(Field.Type.STRING);
 		field1.setFieldType(Field.FieldType.INPUT);
 		project.addInputField(field1);
 		Field field2 = new Field();
 		field2.setName("level");
 		field2.setType(Field.Type.INTEGER);
 		field2.setFieldType(Field.FieldType.INPUT);
 		project.addInputField(field2);
 		Field field3 = new Field();
 		field3.setName("language");
 		field3.setType(Field.Type.STRING);
 		field3.setFieldType(Field.FieldType.INPUT);
 		project.addInputField(field3);
 		Field field4 = new Field();
 		field4.setName("word");
 		field4.setType(Field.Type.STRING);
 		field4.setFieldType(Field.FieldType.INPUT);
 		project.addInputField(field4);
 		Field field5 = new Field();
 		field5.setName("display");
 		field5.setType(Field.Type.STRING);
 		field5.setFieldType(Field.FieldType.INPUT);
 		project.addInputField(field5);
 		Field field6 = new Field();
 		field6.setName("answers");
 		field6.setType(Field.Type.MULTIVALUATE_STRING);
 		field6.setFieldType(Field.FieldType.INPUT);
 		field6.setColumnNames(Sets.newHashSet("correct", "dis_1", "dis_2", "dis_3", "dis_4", "dis_5", "dis_6"));
 		project.addInputField(field6);
 	}
 	
 	private void createOutputFields(Project project) {
 		Field field0 = new Field();
 		field0.setName("timeSpent");
 		field0.setType(Field.Type.INTEGER);
 		field0.setFieldType(Field.FieldType.OUTPUT);
 		project.addOutputField(field0);
 		Field field1 = new Field();
 		field1.setName("failedAttempts");
 		field1.setType(Field.Type.INTEGER);
 		field1.setFieldType(Field.FieldType.OUTPUT);
 		project.addOutputField(field1);
 		Field field2 = new Field();
 		field2.setName("wrongAnswers");
 		field2.setType(Field.Type.MULTIVALUATE_STRING);
 		field2.setFieldType(Field.FieldType.OUTPUT);
 		project.addOutputField(field2);
 	}
 	
 	private void createUserFields(Project project) {
 		Field field = new Field();
 		field.setName("dyslexic");
 		field.setType(Field.Type.BOOL);
 		field.setFieldType(Field.FieldType.USER);
 		project.addUserField(field);
 		Field field1 = new Field();
 		field1.setName("age");
 		field1.setType(Field.Type.INTEGER);
 		field1.setFieldType(Field.FieldType.USER);
 		project.addUserField(field1);
 		Field field2 = new Field();
 		field2.setName("spanishSpeaker");
 		field2.setType(Field.Type.BOOL);
 		field2.setFieldType(Field.FieldType.USER);
 		project.addUserField(field2);
 	}
 	
 	private void createUserDataFields(Project project) {
 		UserDataField field = new UserDataField();
 		field.setName("averageTime");
 		field.setType(UserDataFieldAggregationType.AVERAGE);
 		field.setProjectFieldName("timeSpent");
 		project.addUserDataField(field);
 	}
 	
 	private Batch createSampleBatch(Integer id) {
 		Batch batch = new Batch();
 		batch.setId(id);
 		batch.setName("Wonderful" + random.nextInt(99));
 		int exPerTask = random.nextInt(10) + 1;
 		batch.setExecutionsPerTask(exPerTask);
 		int stateIndex = random.nextInt(states.length);
 		batch.setState(states[stateIndex]);
 		
 		BatchExecutionCollection collection = new BatchExecutionCollection();
 		batchService.saveExecutions(collection);
 		batch.setExecutionCollectionId(collection.getId());
 		
 		int numTasks = random.nextInt(15) + 1;
 		for (int i = 0; i < numTasks; ++i) {
 			Task task = new Task();
 			int numExecutions = random.nextInt(exPerTask + 1);
 			task.setNumExecutions(numExecutions);
 			task.setContents(mapDefinition);
 			task.setBatchId(batch.getId());
 			batch.addTask(task);
 			
 			for (int j = 0; j < numExecutions; ++ j) {
 				Map<String, Object> executionContents = Maps.newHashMap();
 				executionContents.put("timeSpent", 300);
 				executionContents.put("failedAttempts", 1);
 				executionContents.put("wrongAnswers", Lists.newArrayList("answer"));
 				Execution execution = new Execution(executionContents);
 				execution.setTaskId(task.getId());
 				collection.addExecution(execution);
 			}
 		}
 		batchService.saveExecutions(collection);
 		
 		return batch;
 	}
 }
