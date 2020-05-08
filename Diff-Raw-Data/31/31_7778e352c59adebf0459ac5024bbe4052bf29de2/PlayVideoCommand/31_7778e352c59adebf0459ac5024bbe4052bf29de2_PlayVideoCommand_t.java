 package com.anderspersson.xbmcwidget.xbmc;
 
import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 
 public class PlayVideoCommand extends XbmcRequestBase<Boolean> {
 
 	private String path;
 	
 	public PlayVideoCommand(SharedPreferences preferences, String path) {
 		super(preferences);
 		this.path = path;
 	}
 
 	public Boolean execute() throws Exception {
 		
		if(tryPlayUsingEdenMethod())
			return true;
		
		return tryPlayUsingDharmaMethod();
	}

	private Boolean tryPlayUsingDharmaMethod() throws Exception {
		
		JSONObject request = new JSONObject();
		JSONObject parameters = new JSONObject();
		
		parameters.put("file", path);
		
		request.put("params", parameters);
		request.put("method", "XBMC.Play");
		request.put("id", 3);
		request.put("jsonrpc", "2.0");
		
		JSONObject reponse = super.jsonRequest(request);
		JSONObject error = reponse.optJSONObject("error");
		return error == null;
	}

	private boolean tryPlayUsingEdenMethod() throws JSONException, Exception {
 		JSONObject request = new JSONObject();
 		JSONObject item = new JSONObject();
 		JSONObject parameters = new JSONObject();
 		
 		item.put("file", path);
 		parameters.put("item", item);
 		request.put("params", parameters);
 		request.put("method", "Player.Open");
 		request.put("id", 2);
 		request.put("jsonrpc", "2.0");
 		
 		JSONObject reponse = super.jsonRequest(request);
 		JSONObject error = reponse.optJSONObject("error");
		return error == null;
 	}
 }
