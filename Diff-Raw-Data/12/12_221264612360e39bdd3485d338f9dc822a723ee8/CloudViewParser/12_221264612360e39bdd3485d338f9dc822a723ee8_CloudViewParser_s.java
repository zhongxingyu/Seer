 package com.cloudappstudio.utility;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.cloudappstudio.data.CloudAuthId;
 import com.cloudappstudio.data.CloudView;
 
 /**
  * CloudViewParser - Parses view data and encapsulates it in the form of CloudView objects
  * @author mrjanek <Jesper Lindberg>
  */
 public class CloudViewParser {
 	private CloudAuthId id;
 
 	public CloudViewParser(CloudAuthId id) {
 		CloudOAuth auth = new CloudOAuth(id);
 		this.id = auth.getCloudAuthId();
 	}
 	
 	public List<CloudView> parseFromString(String json) throws JSONException {
 		JSONArray outlineArray = new JSONArray(json);
 		List<CloudView> views = new ArrayList<CloudView>();
 		
 		for (int i = 0; i < outlineArray.length(); i++) {
 			JSONObject appObject = outlineArray.getJSONObject(i);
 			
 			String title = appObject.getString("title");
 			String viewName = appObject.getString("name");
 			
 			views.add(new CloudView(title, viewName));
 		}
 		
 		return views;
 	}
 	
 	public List<CloudView> parseFromCloud(String appName) throws IllegalStateException, IOException, JSONException {
 		CloudOAuth auth = new CloudOAuth(id);
 		
 		Map<String, String> params = new HashMap<String, String>();
		params.put("?appName=", appName);
 		
 		String content = auth.getContent(CloudConstants.CAS360_VIEWS_JSON, params);
 		Log.i("CLOUD", "Content:" + content);
 		return parseFromString(content);
 	}
 }
