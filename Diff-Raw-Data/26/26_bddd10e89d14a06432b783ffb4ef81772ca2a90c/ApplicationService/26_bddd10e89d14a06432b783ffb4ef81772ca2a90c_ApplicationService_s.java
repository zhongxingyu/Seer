 package com.eventu.webtier;
 
 import geohash.GeoHash;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class ApplicationService {
 
 	DataService myDS;
 	
 	public ApplicationService(DataService userDB) {
 		myDS = userDB;
 	}
 
 	public  JsonObject register(HttpServletRequest request, JsonObject jObj) {
 		
 		//JsonObject jData = newObj.get("data").getAsJsonObject();
 		String uEmail = jObj.get("userEmail").getAsString();
 		String uPass = jObj.get("password").getAsString();
 
 		if( myDS.userEmailExist(uEmail) ){
 			return JsonHelper.failJson("email ready exits");
 		}
 		else{
 			myDS.registerUser(uEmail, uPass);
 			return login(request, jObj);
 			//JsonObject retJson = new JsonObject();
 			//retJson.addProperty("userID", userID);
 			//TODO Session
 			//return JsonHelper.succJson(retJson);
 			//TODO call login function!
 			
 		}
 	}
 
 	public JsonObject login(HttpServletRequest request, JsonObject jObj) {
 		
 		String uEmail = jObj.get("userEmail").getAsString();
		String uPass = jObj.get("userPasswork").getAsString();
 		
 		Integer uid = myDS.login(uEmail, uPass);
 		//null if fail
 		
 		if( uid!=null ){
 			//generate session id
 			Integer sessionId = SessionService.generateSessionId(request, uEmail, uPass);
 			JsonObject retJson = new JsonObject();
 			retJson.addProperty("userID", sessionId);
 			return JsonHelper.succJson(retJson);
 		}
 		else
 		{
 			//return a Json Object that says the reason of login fail
 			return JsonHelper.failJson("Email and Password dont match!");
 		}		
 		
 		
 	}
 
 	
 	//TODO use email to view user profile.
 	
 	public JsonObject addFriend(HttpServletRequest request,
 			JsonObject jObj) {
 		
 		//check doc 
 		Integer sessionID = jObj.get("userID").getAsInt();
 		Integer friendId = jObj.get("friendId").getAsInt();
 		
 		Integer userID = SessionService.getUID(sessionID);
 		
 		if (userID == -1){
 			return JsonHelper.failJson("login fail");
 		}
 		else{
 			int ret = myDS.addFriend(userID, friendId);
 			if (ret != 0 ){
 				return JsonHelper.failJson("already your friend");
 			}
 			else{
 				return JsonHelper.succJson( new JsonObject() );
 			}
 		}
 		
 
 	}
 
 	public  JsonObject removeFriend(HttpServletRequest request,
 			JsonObject jObj) {
 		
 		//check doc 
 		Integer sessionID = jObj.get("userID").getAsInt();
 		Integer friendId = jObj.get("friendId").getAsInt();
 		
 		Integer userID = SessionService.getUID(sessionID);
 		
 		if (userID == -1){
 			return JsonHelper.failJson("login fail");
 		}
 		else{
 			int ret = DataService.removeFriend(userID, friendId);
 			if (ret != 0 ){
 				return JsonHelper.failJson("Delete fail!");
 			}
 			else{
 				return JsonHelper.succJson( new JsonObject() );
 			}
 		}
 		
 
 	}
 	
 	
 	public static JsonObject updateLocation(HttpServletRequest request,
 			JsonObject jObj) {
 		Long latitude = jObj.get("latitude").getAsLong();
 		Long longitude = jObj.get("longitude").getAsLong();
 		
 		GeoHash geoHash = new GeoHash(latitude, longitude, 60);
 		
 		Integer sessionID = jObj.get("userID").getAsInt();
 		Integer userID = SessionService.getUID(sessionID);
 		
 		if (userID == -1){
 			return JsonHelper.failJson("login fail");
 		}
 		else{
 			int eventID = DataService.updateLocation(userID, latitude, longitude, geoHash);
 			
 			if (eventID == -1 ){
 				return JsonHelper.failJson("update location failed!..");
 			}
 			else{
 				JsonObject retJson = new JsonObject();
 				retJson.addProperty("eventID", eventID);
 				return JsonHelper.succJson( retJson );
 			}
 			
 		}
 		
 	}
 
 	public JsonObject getEvent(HttpServletRequest request,
 			JsonObject jObj) {
 		
 		//TODO
 		
 		Integer sessionID = jObj.get("userID").getAsInt();
 		Integer userID = SessionService.getUID(sessionID);
 		
 		return null;
 	}
 
 	public  JsonObject createEvent(HttpServletRequest request,
 			JsonObject jObj) {
 		
 		Integer sessionID = jObj.get("userID").getAsInt();
 		Integer userID = SessionService.getUID(sessionID);
 
 		String eventName = jObj.get("eventName").getAsString();
 		int eventPremission = jObj.get("eventPremission").getAsInt(); //0 - public 1-private 2-....
 		String eventTime = jObj.get("eventTime").getAsString();
 		
 		Long latitude = jObj.get("latitude").getAsLong();
 		Long longitude = jObj.get("longitude").getAsLong();
 		GeoHash geoHash = new GeoHash(latitude, longitude, 60);
 		
 		String description = jObj.get("description").getAsString();
 		
 		if (userID == -1){
 			return JsonHelper.failJson("login fail");
 		}
 		else{
 			int ret = myDS.createEvent(userID, eventName, eventPremission, eventTime, latitude, longitude, geoHash);
 			
 			if (ret != 0 ){
 				return JsonHelper.failJson("creat event failed!..");
 			}
 			else{
 				return JsonHelper.succJson( new JsonObject() );
 			}
 			
 		}
 
 	}
 
 	public static JsonObject allFriendsQuery(HttpServletRequest request,
 			JsonObject newObj) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static JsonObject nearbyFriends(HttpServletRequest request,
 			JsonObject newObj) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	
 }
