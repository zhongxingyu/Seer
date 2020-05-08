 package controllers;
 
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class Sheep extends Controller {
 	
 	  public static Result index() {
 		  // TODO require login
 		  List<models.Sheep> list = models.Sheep.findByProducerId(Long.valueOf(session("producerId")));
 		  
 		  for (models.Sheep sheep : list) {
 			  sheep.producer = null;
 			  sheep.events = null;
 		  }
 		  ObjectMapper mapper = new ObjectMapper();
 		  ObjectNode node = mapper.createObjectNode();
 		  node.put("data", Json.toJson(list));
 		  return ok(node);
 	  }
 	  
 	  public static Result add() {
		  return ok("ok");
 		  
 	  }
 	  
 	  public static Result show(Long id) {
 		  // TODO require owner of sheep
 		  Logger.debug(String.valueOf(id));
 		  models.Sheep sheep = models.Sheep.find.byId(id);
 		  sheep.producer = null;
 		  sheep.events = null;
 		  Logger.debug(sheep.toString());
 		  return ok(Json.toJson(sheep));
 	  }
 	  
 	  public static Result update(Long id) {
 		  return TODO;
 	  }
 	  
 	  public static Result delete(Long id) {
 		  models.Sheep.find.ref(id).delete();
 		  return ok();
 	  }
 
 }
