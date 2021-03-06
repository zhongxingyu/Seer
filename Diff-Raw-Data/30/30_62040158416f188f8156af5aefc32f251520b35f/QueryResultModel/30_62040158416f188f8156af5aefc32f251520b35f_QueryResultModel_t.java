 package org.rpi.rpinfo;
 
 import java.io.Serializable;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * A frontend for a JSONObject with data on a particular person
  */
 public class QueryResultModel implements Serializable {
 	//A unique identifier for this class (for serialization)
 	private static final long serialVersionUID = -579697907972516780L;
	private String data = null;
 	
 	public QueryResultModel( JSONObject data ){
		//The question remains: why does JSONObject not implement Serializable?
		this.data = data.toString();
 	}
 	
 	/**
 	 * Get an element of the JSONObject that the QueryResultModel holds.
 	 * 
 	 * @param key The key to find
 	 * @param failure the object to return if the key is not found
 	 * @return The value that the key maps to or the failure object
 	 */
 	public Object getElement(String key, Object failure){
		JSONObject JSONData = null;
		
		//Convert the string into a JSONObject object.
 		try {
			JSONData = new JSONObject(this.data);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		/*
		 * Get the requested data from the JSONObject. If it does not exist,
		 * return the failure value
		 */
		try {
			return JSONData.get(key);
 		} catch (JSONException e) {
 			return failure;
 		}
 	}
 	
 }
