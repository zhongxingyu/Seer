 package controllers;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
 import models.User;
 import models.UserStatistics;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.type.TypeReference;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class UserStatisticsREST extends Controller {
 
 	// GET
 	public static Result findByUserId(String userId) {
		boolean isConnectedUser = false;
 		final User connectedUser = Application.getLocalUser(session());
		if(connectedUser != null){
			isConnectedUser = (userId == connectedUser.getIdentifier());
		}
 		UserStatistics userStatistics = UserStatistics.findByUserId(userId);
 		if(userStatistics == null){
 			userStatistics = UserStatistics.init(userId).save();
		}else if(isConnectedUser) { // set to false the non-read data flags...
 			UserStatistics userStatisticsRead = userStatistics;
 			userStatisticsRead.setRead();
 			userStatisticsRead.save();
 		}
 		return ok(Json.toJson(UserStatistics.userStatisticsToObjectNode(userStatistics)));
 	}
 
 	// PUT
 	public static Result updateUserStatistics(String userId) {
		boolean isConnectedUser = false;
 		final User connectedUser = Application.getLocalUser(session());
		if(connectedUser != null){
			isConnectedUser = (userId == connectedUser.getIdentifier());
		}
 		ObjectMapper mapper = new ObjectMapper();
 		JsonNode json = request().body().asJson();
 		Map<String, Integer> statistics;
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		// Parse the received json:
 		try{
 			statistics = mapper
 				.readValue(json.findPath("statistics"), new TypeReference<Map<String,Integer>>() { });
 		}catch(JsonMappingException e){
 			return badRequest(Constants.STATISTICS_PARSE_ERROR.toString());
 		}catch(JsonParseException e){
 			return badRequest(Constants.STATISTICS_PARSE_ERROR.toString());
 		}catch(IOException e){
 			return badRequest(Constants.STATISTICS_PARSE_ERROR.toString());
 		}
 		// Retrieve the existing Statistics:
 		UserStatistics userStatistics = UserStatistics.findByUserId(userId);
 		if(userStatistics == null){
 			userStatistics = UserStatistics.init(userId);
 		}
 		Iterator<String> it = statistics.keySet().iterator();
 		String key;
 		while (it.hasNext()) {
 			key = it.next();
 			userStatistics.updateStatistic(key, statistics.get(key));
 		}
 		UserStatistics userStatisticsToUpdate = userStatistics;
 		if(isConnectedUser){
 			userStatisticsToUpdate.setRead();
 		}
 		userStatisticsToUpdate.update();
 		return ok(UserStatistics.userStatisticsToObjectNode(userStatistics));
 	}
 }
