 package controllers;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 import models.User;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class SubscriptionController extends Controller{
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public static Result UpdateSubscriptionStatus(){
 		
 		// Get json information sent in
 		JsonNode json = request().body().asJson();
 		
 		String entityType = json.get("entityType").asText();
 		String entityId = json.get("entityId").asText();
 		String username = json.get("username").asText();
 		
		User user = User.findByName(username);
 		// TODO this is totally a huge problem that appears to break everything!!!!
 
 
 		// create return object
 		ObjectNode result = Json.newObject();
 
 		//boolean resulted = User.doesUserSubscribeToEntity(user,entityId,entityType);
 		//result.put("newState",resulted);
 		result.put("newState", "true");
 
 		// swap subscription status
 		/*if( User.doesUserSubscribeToEntity(user, entityId, entityType )){
 			User.setUserEntitySubscriptionStatus(false, user, entityId, entityType);
 			result.put("newState","false");
 		}
 		else{
 			User.setUserEntitySubscriptionStatus(true, user, entityId, entityType);
 			result.put("newState","true");
 		}*/
 		
 		return ok(result);
 	}
 
 }
