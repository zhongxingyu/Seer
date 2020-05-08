 package com.util.getconnected;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.util.Log;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Created with IntelliJ IDEA. User: johan_000 Date: 10/15/13 Time: 2:08 PM To
  * change this template use File | Settings | File Templates.
  */
 public class JSONParser {
 
 	private static JSONParser instance;
 
 	private JSONParser() {
 	}
 
 	public JSONObject getObjectFromJSON(JSONObject jsonObject, String keyword)
 			throws JSONException {
 		return jsonObject.getJSONObject(keyword);
 	}
 
 	public JSONObject getObjectFromRequest(String json) throws JSONException {
 		return new JSONObject(json);
 	}
 
 	public ArrayList<JSONObject> getArrayFromRequest(String json, String keyword)
 			throws JSONException {
 		JSONObject jsonObject = this.getObjectFromRequest(json);
 		return this.JSONArrayToArrayList(jsonObject.getJSONArray(keyword));
 	}
 
	public JSONObject parseList(List list, String identifier) throws JSONException{
 		String json = "{\"" + identifier + "\": [";
 		for (int i=0; i<list.size(); i++) {
 			json += "\"" + list.get(i).toString() + "\",";
 		}
 		json = json.substring(0, json.length()-1);
 		Log.d("tag", json);
 		return this.getObjectFromRequest(json + "]}");
 	}
 
 	private ArrayList<JSONObject> JSONArrayToArrayList(JSONArray array)
 			throws JSONException {
 		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
 		for (int i = 0; i < array.length(); i++) {
 			list.add(array.getJSONObject(i));
 		}
 		return list;
 	}
 
 	/**
 	 * @return JSONParser
 	 */
 	public static JSONParser getInstance() {
 		if (instance == null) {
 			instance = new JSONParser();
 		}
 		return instance;
 	}
 }
