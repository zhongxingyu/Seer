 package controllers;
 
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.db.ebean.Model.Finder;
 import play.libs.Json;
 import play.mvc.*;
 import models.Task;
 import play.mvc.Controller;
 
 public class JsonController extends Controller
 { 
 	
 	public static Result all()
 	{		
		Finder<Long, Task> finder = new Finder<>(Long.class, Task.class);			
		List<Task> tasks = finder.all();
 		return ok(Json.toJson(tasks));
 	}
 						
 	public static Result create()
 	{		
 		String header = request().getHeader(CONTENT_TYPE);
 		String text = request().body().asText();
 		System.out.println(text);
 		JsonNode node = request().body().asJson();
 		if(node == null)
 		{
 			return badRequest("Expecting Json data");
 		}
 		
 		Task task = Json.fromJson(node, Task.class);
 		task.save();
 		return ok(Json.toJson(task.id));
 	}
 	
 	public static Result delete()
 	{		
 		JsonNode node = request().body().asJson();
 		if(node == null)
 		{
 			return badRequest("Expecting Json data");
 		}
 		
 		Long id = Json.fromJson(node, Long.class);
		Finder<Long, Task> finder = new Finder<>(Long.class, Task.class);		
 		Task task = finder.where().eq("id", id).findUnique();
 		task.delete();
 		return ok(Json.toJson("deleted"));
 	}
 	
 }
