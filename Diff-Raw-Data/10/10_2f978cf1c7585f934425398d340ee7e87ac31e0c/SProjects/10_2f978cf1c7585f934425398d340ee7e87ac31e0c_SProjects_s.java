 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.util.ArrayList;
 import java.util.List;
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
		List<STask> tasks = project.staks;
 		if (tasks == null) {
 			return ok("[]");
 		}
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
 		String sceneId = node.get("sceneId").asText();
 
 		SProject project = SProject.find.byId(projectId);
 		STask task = new STask(title, sceneId);
		if (project.staks == null) {
			project.staks = new ArrayList<STask>();
 		}
 		project.addTask(task);
 		project.save();
 
 		return ok(toJson(task));
 
 	}
 	
 	
 	
 	
 	
 	public static Result fetchTaskById(String taskId) {
 		
 		SProject project =  SProject.find.field("taskId").equal(taskId).get();
 		
 		STask task = null;
		for (STask p : project.staks) {
 			if (p.id.equals(taskId)) {
 				task = p;
 				break;
 			}
 		}
 		return ok(toJson(task));
 		
 	}
 	
 	
 	
 
 }
