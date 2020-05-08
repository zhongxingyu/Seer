 package com.insta4j.instagram.util;
 
 import java.lang.reflect.Type;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.appengine.repackaged.org.json.JSONException;
 import com.google.appengine.repackaged.org.json.JSONObject;
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.insta4j.instagram.exception.HttpError;
 import com.insta4j.instagram.exception.InstagramError;
 import com.insta4j.instagram.exception.InstagramException;
 
 /**
  * Convert JSON to appropriate objects
  * 
  * @author nischal.shetty
  * 
  */
 public class JSONToObjectTransformer {
 
 	
 	private static Logger logger = Logger.getLogger(JSONToObjectTransformer.class.getName());
 	
 	/**
 	 * Gson would be singleton. Please take care not to include rules in the builder that aren't common for the entire
 	 * API.
 	 */
 	private static final Gson gson = new GsonBuilder()
 			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
 
 	public static <E> E getObject(String json, Class<E> e) throws InstagramException {
 		//If facebook returns an error then throw the error
 		errorCheck(json);
 		
 		try {
 			return gson.fromJson(json, e);
 		} catch(Exception exception){
 			logger.log(Level.SEVERE, "Data received from Facebook for class "+e.getName()+" is "+json,exception);
 			throw new InstagramException("Error while converting object. Send this to nischal@grabinbox.com : "+json, exception);
 		}
 	}
 
 	public static <E> E getObject(String json, Type type) throws InstagramException {
 		//If facebook returns an error then throw the error
 		errorCheck(json);
 		return gson.<E>fromJson(json, type);
 	}
 
 	private static void errorCheck(String json) throws InstagramException {
 		if(json.contains("error_code")){
 			
 			InstagramError error = null;
 			try {
 				error = gson.fromJson(json, InstagramError.class);
 			} catch(Exception exception){
 				throw new InstagramException("Error in converting facebook error to FacebookError object! Facebook data is: "+json,exception);
 			}
 			
 			throw new InstagramException(error);
 		}
 	}
 	
 	public static InstagramError getError(String response, int statusCode) {
     try {
       JSONObject jsonObject = new JSONObject(response);
       JSONObject obj = jsonObject.getJSONObject("meta");
      return new InstagramError(statusCode,
		      String.valueOf(obj.getInt("code")) + ": " + obj.getString("error_type") + ": " + obj.getString("error_message"),
		      obj.getString("error_type"),
		      null);
     }catch (JSONException e){}
    return new InstagramError(statusCode, "There was some error. Please try again", null, null);
 	}
 	
 	/*public static void main(String[] args) {
 		Type type = new TypeToken<Map<String, User>>(){}.getType();
 		Map<String, User> map = gson.fromJson("{'100000763980384':{'id':'100000763980384','name':'Manav Mehta','first_name':'Manav','last_name':'Mehta','link':'','gender':'male','locale':'en_US','updated_time':'2011-04-03T07:44:21+0000'},'1326276311':{'id':'1326276311','name':'Rupesh Chodankar','first_name':'Rupesh','last_name':'Chodankar','link':'','username':'rupesh.chodankar','gender':'male','locale':'en_GB','updated_time':'2011-04-02T12:13:04+0000'},'100000700842623':{'id':'100000700842623','name':'Prashant Dotiya','first_name':'Prashant','last_name':'Dotiya','link':'','gender':'male','locale':'en_US','updated_time':'2011-04-03T07:17:24+0000'}}", type);
 		for (String key : map.keySet()) {
 			System.out.println("map.get = " + map.get(key));
 		}
 	}*/
 
 }
