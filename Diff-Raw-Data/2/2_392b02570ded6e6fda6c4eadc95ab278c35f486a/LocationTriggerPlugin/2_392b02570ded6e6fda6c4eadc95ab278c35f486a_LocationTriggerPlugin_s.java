 /**
  * 
  */
 package edu.ucla.cens.andwellness.mobile.plugin;
 
 import java.io.FileOutputStream;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.phonegap.api.Plugin;
 import com.phonegap.api.PluginResult;
 import com.phonegap.api.PluginResult.Status;
 
 import edu.ucla.cens.andwellness.triggers.types.location.LocTrigDesc;
 
 /**
  * @author mistralay
  *
  */
 public class LocationTriggerPlugin extends Plugin {
 	
 	/* (non-Javadoc)
 	 * @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
 	 */
 	@Override
 	public PluginResult execute(String action, JSONArray data, String callbackId) {
 		PluginResult result = null;
 		
 		if (action.equals("set")) { 
 			JSONObject dataObject;
 			try {
 								
 				dataObject = data.getJSONObject(0);
 //				String category = dataObject.getString("category");
 //				String label = dataObject.getString("label");
 //				String surveyId = dataObject.getString("survey_id");
 //				long latitude = dataObject.getLong("latitude");
 //				long longitude = dataObject.getLong("longitude");
 //				JSONArray repeatArray = dataObject.getJSONArray("repeat");
 				
			    FileOutputStream out = new FileOutputStream("/sdcard/location_temp.jpg");
 			   
 
 				
 				
 //				LocTrigDesc trigDesc = new LocTrigDesc();
 //				trigDesc.setRangeEnabled(false);
 //				trigDesc.setTriggerAlways(false);
 //				trigDesc.setLocation(label);
 //				trigDesc.setMinReentryInterval(120);
 				
 				
 				JSONObject apiResult = new JSONObject();
 				apiResult.put("result", "success");
 				result = new PluginResult(Status.OK, apiResult); 
 				
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else if (action.equals("getAll")) { 
 			JSONObject apiResult = new JSONObject();
 			try {
 				apiResult.put("result", "success");
 				
 				JSONArray repeatArray = new JSONArray();
 				repeatArray.put("M");
 				repeatArray.put("T");
 				repeatArray.put("W");
 				
 				JSONObject object1 = new JSONObject(); 
 				object1.put("category", "home");
 				object1.put("label", "palash1");
 				object1.put("latitude", 123.123);
 				object1.put("longitude", 123.123);
 				object1.put("survey_id", "exerciseAndActivity");
 				object1.put("repeat", repeatArray);
 				
 				JSONArray triggerArray = new JSONArray(); 
 				triggerArray.put(object1);
 				
 				apiResult.put("triggers", triggerArray);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			result = new PluginResult(Status.OK, apiResult); 			
 		}
 		
 		
 		return result;
 	}
 
 }
