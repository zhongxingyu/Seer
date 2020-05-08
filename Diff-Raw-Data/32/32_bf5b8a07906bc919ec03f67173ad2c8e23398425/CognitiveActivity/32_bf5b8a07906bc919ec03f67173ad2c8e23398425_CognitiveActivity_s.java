 package com.movisens.xs.android.cognition;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 
 /*
  * ??????
  */
 public class CognitiveActivity extends Activity {
 	public final String TAG = getClass().getSimpleName();
 	public final String CONFIG = "XS_config";
 
 	/*
 	 * ???
 	 */
	public void fillBoolean(Boolean parameter, String name)
 			throws JSONException {
 		String extra = getIntent().getStringExtra(CONFIG);
 		if (extra == null)
			return;
 		JSONObject config = new JSONObject(extra);
 		if (config.has(name))
 			parameter = config.getBoolean(name);
 	}
 
 	/*
 	 * ???
 	 */
	public void fillInt(Integer parameter, String name) throws JSONException {
 		String extra = getIntent().getStringExtra(CONFIG);
 		if (extra == null)
			return;
 		JSONObject config = new JSONObject(extra);
 		if (config.has(name))
 			parameter = config.getInt(name);
 	}
 
 	/**
 	 * @return new array on success or null if name not known.
 	 */
 	public String[] fillStringArray(String name)
 			throws JSONException {
 		String extra = getIntent().getStringExtra(CONFIG);
 		if (extra == null)
 			return null;
 		JSONObject config = new JSONObject(extra);
 		if (config.has(name)) {
 			JSONArray a = config.getJSONArray(name);
 			String[] parameter = new String[a.length()];
 			for (int i = 0; i < a.length(); i++) {
 				parameter[i] = a.getString(i);
 			}
 			return parameter;
 		}
 		return null;
 	}
 }
