 package controllers;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.codehaus.jackson.JsonNode;
 
 import models.cmu.sv.sensor.DBHandler;
 import models.cmu.sv.sensor.MessageBusHandler;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class Device extends Controller {
 	private static DBHandler dbHandler = null;
 	private static boolean testDBHandler(){
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
 		 
 		 // Parse JSON FIle 
 		 String deviceId = json.findPath("device_id").getTextValue();
 		 String deviceType = json.findPath("device_type").getTextValue();
 		 String deviceAgent = json.findPath("device_agent").getTextValue();
 		 String location = json.findPath("location").getTextValue();
 		 models.cmu.sv.sensor.Device device = new models.cmu.sv.sensor.Device(deviceId, deviceType, deviceAgent, location); 
 		 if(!device.save()){
 			System.err.println("device " + deviceId + " is not saved");
 			return ok("not saved");
 		 } 
          return ok("saved");		 
 	}
 
 	public static Result getDevice(String format) {
 		if(!testDBHandler()){
 			return internalServerError("database conf file not found");
 		}
 		response().setHeader("Access-Control-Allow-Origin", "*");
 		ArrayList<models.cmu.sv.sensor.Device> devices = dbHandler.getDevice();
 		if(devices == null || devices.isEmpty()){
 			return notFound("no devices found");
 		}
 		String ret = new String();
 		if (format.equals("json"))
 		{			
 			for (models.cmu.sv.sensor.Device device : devices) {
 				if (ret.isEmpty())
 					ret += "[";
 				else				
 					ret += ',';			
 				ret += device.toJSONString();
 			}
 			ret += "]";			
 		} else {			
 			for (models.cmu.sv.sensor.Device device : devices) {
 				if (!ret.isEmpty())
 					ret += '\n';
 				else
 					ret += device.getCSVHeader();
 				ret += device.toCSVString();
 			}
 		}
 		return ok(ret);
 	}
 
 	public static Result getSensorType(String deviceType, String format) {
 		if(!testDBHandler()){
 			return internalServerError("database conf file not found");
 		}
 		response().setHeader("Access-Control-Allow-Origin", "*");
 		// case insensitive search. device types in the database are in lower case
 		deviceType = deviceType.toLowerCase();
 		ArrayList<String> sensorTypes = dbHandler.getSensorType(deviceType);
 		if(sensorTypes == null || sensorTypes.isEmpty()){
 			return notFound("No sensor type found for " + deviceType);
 		}
 		String ret = new String();
 		if (format.equals("json"))
 		{
 			for (String sensorType : sensorTypes) {
				if (ret.isEmpty())
					ret += "{";
				else				
					ret += ',';
				ret += sensorType;
 			}
			ret += "}";
 		} else {
 			for (String sensorType : sensorTypes) {
 				if (!ret.isEmpty())
 					ret += '\n';
 				else
					ret += "SensorTypes\n";
 				ret += sensorType;
 			}
 		}
 		return ok(ret);
 	}	
 	
 }
