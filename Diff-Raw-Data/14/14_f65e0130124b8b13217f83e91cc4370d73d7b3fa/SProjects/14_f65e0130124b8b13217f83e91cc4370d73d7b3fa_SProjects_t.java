 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonNode;
 import models.*;
 
 import play.mvc.*;
 
 /**
  * @author Muhammad Fahied
  */
 
 public class SProjects extends Controller {
 	
 	/*
 	 * 
 	 * Project Services
 	 * 
 	 * */
 	
 	public static Result fetchAllProjects() {
 
 		List<SProject> groups = SProject.find.asList();
 	    response().setContentType("application/json");
 	     
 		return ok(toJson(groups));
 	}
 	
 	/*
 	 * Map group = new HashMap();
 				group.put("name", "NewYork");
 				group.put("runId", 2);
 				
 				JsonNode node = Json.toJson(group);
 	 * */
 	
 	
 	
 	
 	public static Result fetchMenuItems() {
 
 		SProject menu = SProject.find.get();
 		
 		if (menu == null) {
 			int empty [] = null;
 			return ok(toJson(empty));
 		}
 		List<SScene> scenes = menu.sscenes;
 		List<STask> tasks = menu.stasks;
 		
 		List<Map<String, Object>> menuMap = new LinkedList<Map<String, Object>>();
 		
 		for(SScene scene : scenes)
 		{
 			Map<String, Object> sceneItem = new HashMap<String, Object>();
 			sceneItem.put("id", scene.id);
 			sceneItem.put("title", scene.title);
 			
 			List<Map<String, String>> taskMap = new LinkedList<Map<String, String>>();
 			if (tasks != null) {
 
 				for(STask task : tasks)
 				{
 					Map<String, String> taskItem = new HashMap<String, String>();
 					
 					if ( task.sceneId.equals(sceneItem.get("id"))) 
 					{
 						System.out.println("task found");
 						//if (taskItem.size() >= 1) 
 						{
 							taskItem.put("id", task.id);
 							taskItem.put("title", task.title);
 							taskItem.put("description", task.description);
 							taskItem.put("taskType", task.taskType);
 							taskMap.add(taskItem);
 						}
 					}
 					sceneItem.put("stasks", taskMap);
 				}
 				
 			}
 
 			menuMap.add(sceneItem);
 		}
 		
 	    response().setContentType("application/json");
 	     
 		return ok(toJson(menuMap));
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	public static Result addProject() {
 		
 		// parse JSON from request body
 		JsonNode node = ctx().request().body().asJson();
 		String title = node.get("title").asText();
 				
 		SProject project = new SProject(title);
 		project.save();
 		
 		return ok(toJson(project));
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	public static Result fetchProjectById( String projectId) {
 		
 		SProject project = SProject.find.byId(projectId);
 		return ok(toJson(project));
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static Result fetchProjectByTitle( String title) {
 		
 		SProject project = SProject.find.filter("title", title).get();
 		return ok(toJson(project));
 		
 	}
 	
 	
 	
 	
 	/*
 	 * 
 	 * Act Service
 	 * 
 	 * */
 	public static Result fetchActs(String projectId) {
 		
 		SProject project = SProject.find.byId(projectId);
 		List<SAct> acts = project.sacts;
 		return ok(toJson(acts));
 		
 	}
 	
 	
 	
 	/* POST - JSON request
 	
 		
 	{
 		"title"  : "Miracle"
 	}
 	
 	
 	*/
 	
 	public static Result addAct(String projectId) {
 		
 		// parse JSON from request body
 		JsonNode node = ctx().request().body().asJson();
 		String title = node.get("title").asText();
 				
 		SProject project = SProject.find.byId(projectId);
 		SAct act = new SAct(title);
 		if (project.sacts == null) {
 			project.sacts = new ArrayList<SAct>();
 		}
 		project.addAct(act);
 		project.save();
 		
 		return ok(toJson(act));
 		
 	}
 	
 	
 	
 	
 	/*
 	 * 
 	 * Scene Service
 	 * 
 	 * */
 	public static Result fetchScenes(String projectId) {
 		
 		SProject project = SProject.find.byId(projectId);
 		List<SScene> scenes = project.sscenes;
 		return ok(toJson(scenes));
 		
 	}
 	
 	
 	
 	/* POST - JSON request
 	
 	
 	{
 		"title"  : "Miracle"
 	}
 	
 	
 	*/
 	
 	public static Result addScene(String projectId) {
 		
 		// parse JSON from request body
 		JsonNode node = ctx().request().body().asJson();
 		String actId = node.get("actId").asText();
 		String title = node.get("title").asText();
 				
 		SProject project = SProject.find.byId(projectId);
 		SScene scene = new SScene(title, actId);
 		if (project.sscenes == null) {
 			project.sscenes = new ArrayList<SScene>();
 		}
 		project.addScene(scene);
 		project.save();
 		
 		return ok(toJson(scene));
 		
 	}
 	
 	
 	
 	/*
 	 * 
 	 * Task Service
 	 * 
 	 * */
 	public static Result fetchTasksByProject(String projectId) {
 		
 		SProject project = SProject.find.byId(projectId);
 		if (project.stasks == null) {
 			project.stasks = new ArrayList<STask>();;
 		}
 		List<STask> tasks = project.stasks;
 		return ok(toJson(tasks));
 	}
 	
 	
 	
 	/* POST - JSON request
 	
 	
 	{
 		"title"  : "Miracle"
 	}
 	
 	
 	*/
 	
 	public static Result addTask(String projectId) {
 
 		// parse JSON from request body
 		JsonNode node = ctx().request().body().asJson();
 		String title = node.get("title").asText();
 		String description = node.get("description").asText();
 		String sceneId = node.get("sceneId").asText();
 		String taskType = node.get("taskType").asText();
 
 		SProject project = SProject.find.byId(projectId);
 		
 		STask task = new STask(title, description, taskType, sceneId);
 		if (project.stasks == null) {
 			project.stasks = new ArrayList<STask>();
 		}
 		project.addTask(task);
 		project.save();
 
 		return ok(toJson(task));
 
 	}
 	
 	
 	
 	
 	
 	public static Result fetchTaskById(String taskId) {
 		
		//SProject project =  SProject.find.disableValidation().field("stasks.id").equal(taskId).get();
		//There is only one project in our experiment
		SProject project =  SProject.find.get();

 		if (project.stasks == null) {
 			project.stasks = new ArrayList<STask>();
 		}
 		
 		STask task = null;
 		for (STask p : project.stasks) {
 			if (p.id.equals(taskId)) {
 				task = p;
 				break;
 			}
 		}
		if (task == null) {
			return ok();
		}
 		return ok(toJson(task));
 		
 	}
 	
 	
 	public static Result getNameOfTask(String taskId) {
 		
 		SProject project =  SProject.find.disableValidation().field("stasks.id").equal(taskId).get();
 		
 		STask task = null;
 		for (STask p : project.stasks) {
 			if (p.id.equals(taskId)) {
 				task = p;
 				break;
 			}
 		}
 		return ok(task.title);
 	}
 	
 	public static String getNameOfTaskString(String taskId) {
 		
 		SProject project =  SProject.find.disableValidation().field("stasks.id").equal(taskId).get();
 		
 		STask task = null;
 		for (STask p : project.stasks) {
 			if (p.id.equals(taskId)) {
 				task = p;
 				break;
 			}
 		}
 		return task.title;
 	}
 	
 
 
 }
