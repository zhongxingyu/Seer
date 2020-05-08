 package controllers;
 
 import java.sql.Connection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import models.Task;
 import models.User;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.db.DB;
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class TaskController extends Controller {
 
 	final static Connection connection = DB.getConnection();
 
 	public static Result taskList() {
 		ArrayNode arnode = Json.newObject().arrayNode();
 
 		String username = session("username");
 		List<Task> taskList = new ArrayList<Task>();
 
 		if (username != null && !"null".equals(username)) {
 			//taskList.addAll(Task.findByOwner(connection, username));
 			List<String> subList = User.findSubOrdinates(connection, username);
 			System.out.println("sublists :" + subList);
 			for (String subuserid : subList) {
 				taskList.addAll(Task.findByOwner(connection, subuserid));
 			}
 
 			
 			for (Task task : taskList) {
 				ObjectNode result = taskAsJson(task);
 				arnode.add(result);
 			}
 		}
 
 		return ok(arnode);
 	}
 	
 	
 	public static Result getSubOrdinates(String username) {
 	    System.out.println("getting subordinates for "+ username);
 	    	List<String> subList = User.findSubOrdinates(connection, username);
 	    	ArrayNode arnode = Json.newObject().arrayNode();
 	    	for(String sub : subList){
 	    	    arnode.add(sub);
 	    	}
 	    	
 		return ok(arnode);
 	}
 	
 
 	private static ObjectNode taskAsJson(Task task) {
 		ObjectNode result = Json.newObject();
 		result.put("id", task.id);
 		result.put("todo", task.todo);
 		result.put("due", dateToString(task.due));
 		result.put("details", task.details);
 		result.put("author", task.author);
 		result.put("createdAt", dateToString(task.createdAt));
 		result.put("done", task.done);
 		result.put("group", task.group);
 		result.put("owner", task.owner);
 		result.put("status", task.status);
 		
 		
 		return result;
 	}
 
 	private static String dateToString(Date date) {
 		SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy");
 		return formater.format(date);
 	}
 
 	private static Date stringToDate(String date) {
 		SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy");
 		Date parsedDate = null;
 		try {
 			parsedDate = formater.parse(date);
 		} catch (ParseException e) {
			//e.printStackTrace();
 		}
 		return parsedDate;
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result add() {
 		JsonNode json = request().body().asJson();
 
 		String username = session("username");
 
 		if (username != null && !"null".equals(username)) {
 			if ("".equals(json.findPath("todo").getTextValue())) {
 				return badRequest("Missing parameter [todo]");
 			} else {
 				Task task = createTaskFromJson(json);
 				Task.create(connection, task);
 			}
 		} else {
 			return badRequest("User not logged in");
 		}
 
 		return ok(json);
 	}
 
 	public static Result getTask(String id) {
 
 		Task task = Task.findById(connection, id);
 		ObjectNode result = taskAsJson(task);
 		return ok(result);
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result update(String id) {
 		System.out.println("in update");
 		JsonNode json = request().body().asJson();
 		String username = session("username");
 		System.out.println("with user" + username);
 		if (username != null && !"null".equals(username)) {
 
 			if (json.findPath("todo").getTextValue() == null) {
 				return badRequest("Missing parameter [task]");
 			} else {
 				Task task = createTaskFromJson(json);
 				task.id = id;
 				Task.update(connection, task);
 			}
 		} else {
 			return badRequest("User not logged in");
 		}
 		return ok(json);
 	}
 
 	private static Task createTaskFromJson(JsonNode json) {
 		String todo = json.findPath("todo").getTextValue();
 		String due = json.findPath("due").getTextValue();
 		String details = json.findPath("details").getTextValue();
 		String done = json.findPath("done").getTextValue();
 		String assignTo = json.findPath("assignTo").getTextValue();
 
 		Task task = new Task();
 		task.active = Boolean.TRUE;
 		task.author = task.todo = todo;
 		task.due = stringToDate(due);
 		task.status = done==null?"Open":"Completed";
 				
 		task.done = done==null?Boolean.FALSE:Boolean.TRUE;
 		task.details = details;
 		task.owner = assignTo!=null?assignTo:session("username");
 		
 		return task;
 	}
 
 	public static Result delete(String id) {
 		
 		if(id==null){
 			return badRequest("id cannot be null");
 		}
 
 		String username = session("username");
 		if (username != null && !"null".equals(username)) {
 			Task task = Task.findById(connection, id);
 			if(username.equals(task.owner))
 				Task.delete(connection, id);
 		}
 		ObjectNode result = Json.newObject();
 		result.put("id", id);
 		
 		return ok(result);
 	}
 
 }
