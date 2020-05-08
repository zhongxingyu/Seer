 package controllers;
 
 import org.codehaus.jackson.JsonNode;
 import play.mvc.Controller;
 import play.mvc.Result;
 import models.cmu.sv.sensor.*;
 
 
 public class SensorReading extends Controller {
 	private static DBHandler dbHandler = null;
 	private  static boolean testDBHandler(){
 		if(dbHandler == null){
 			dbHandler = new DBHandler("conf/database.properties");
 		}
 		return true;
 	}
 	
 	public static Result add() {
 		JsonNode json = request().body().asJson();
 		 if(json == null) {
 			    return badRequest("Expecting Json data");
 		 } 
 		 if(!testDBHandler()){
 			 return internalServerError("database conf file not found");
 		 }
 		 
 		// TODO parse Json File and add reading to server. 	 
 		 int deviceId = json.findPath("deviceId").getIntValue();
 		 int timeStamp = json.findPath("timeStamp").getIntValue();
 		 String sensorType = json.findPath("sensorType").getTextValue();
 		 double value = json.findPath("value").getDoubleValue();
 		 
 		 
 		 if (!dbHandler.addReading(deviceId, timeStamp, sensorType, value)){
 			 return internalServerError("record not saved");
 		 }
 	    return ok("saved");
 	}
 	public static Result search(int deviceId, int timeStamp){
 		if(!testDBHandler()){
 			return internalServerError("database conf file not found");
 		}
 		models.cmu.sv.sensor.SensorReading reading = dbHandler.searchReading(deviceId, timeStamp);
		
 		
 		return ok(reading.getSensorType()+ ":" + String.valueOf( reading.getValue()));
 	}
 	/*
 	public static void updateUser(Long id, User user) {
 	    User dbUser = User.findById(id);
 	    dbUser.updateDetails(user); // some model logic you would write to do a safe merge
 	    dbUser.save();
 	    user(id);
 	}
 
 	public static void deleteUser(Long id) {
 	    User.findById(id).delete();
 	    renderText("success");
 	}
 
 	public static void user(Long id)  {
 	    User user = User.findById(id)
 	    render(user);
 	}
 	*/
 }
