 package controllers;
 
 import static play.libs.Json.toJson;
 
import java.io.IOException;
 import java.util.List;
 
 import models.Computer;
 
 import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 
 /**
  * Manage a database of computers
  */
 public class Application extends Controller {
     
     /**
      * This result directly redirect to application home.
      */
 //    public static Result GO_HOME = redirect(
 //    	
 //        //routes.Application.list(0, "name", "asc", "")
 //    );
     
     /**
      * Handle default path requests, redirect to computers list
      */
     public static Result index() {
         //return GO_HOME;
     	return null;
     }
 
     
     public static Result list() {
     	System.out.println("List : Controller");
     	List<Computer> computers = Computer.find.all();
     	System.out.println("nb computers : " + computers.size());
         return ok(toJson(computers));
     }
     
     public static Result save(){
     	System.out.println("Save : Controller");
     	JsonNode json = request().body().asJson();
     	ObjectMapper mapper = new ObjectMapper();
     	try {
 			
    		Computer computer = mapper.readValue(json, Computer.class);
     		computer.save();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return internalServerError();
 		} 
 		
		return ok();
     }
     
     public static Result update(Long id){
     	System.out.println("Update : Controller");
     	JsonNode json = request().body().asJson();
     	ObjectMapper mapper = new ObjectMapper();
     	try {
 			
     		Computer computer = mapper.readValue(json, Computer.class);
     		computer.update();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return internalServerError();
 		} 
 		
 		return ok();
     }
     
     public static Result get(Long id){
     	Computer computer = Computer.find.byId(id);
 		System.out.println("Get Computer : " + computer.id);
 		return ok(toJson(computer));
     }
  
 
 }
             
