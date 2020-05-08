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
	/**
	 * @param parameter default value for parameter if name not known.
	 * @return parameter value which may be default if name not known.
	 */
	public boolean fillBoolean(boolean parameter, String name)
 			throws JSONException {
 		String extra = getIntent().getStringExtra(CONFIG);
 		if (extra == null)
			return parameter;
 		JSONObject config = new JSONObject(extra);
 		if (config.has(name))
 			parameter = config.getBoolean(name);
		return parameter;
 	}
 
 	/*
 	 * ???
 	 */
	/**
	 * @param parameter default value for parameter if name not known.
	 * @return parameter value which may be default if name not known.
	 */
	public int fillInt(int parameter, String name) throws JSONException {
 		String extra = getIntent().getStringExtra(CONFIG);
 		if (extra == null)
			return parameter;
 		JSONObject config = new JSONObject(extra);
 		if (config.has(name))
 			parameter = config.getInt(name);
		return parameter;
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
