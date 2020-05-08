 package com.masenf.wtaandroid.async;
 
 import java.util.UUID;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.masenf.core.async.ProgressReportingTask;
 import com.masenf.wtaandroid.data.WtaDatastore;
 import com.masenf.wtaandroid.data.WtaDatastore.TagEntryType;
 
 public class LibraryUpdateTask extends ProgressReportingTask<JSONObject, Integer> {
 	public static final String TAG = "LibraryUpdateTask";
 	private WtaDatastore d = null;
 
 	private int total_records = 1;
 	private int raw_progress = 0;
 	
 	public LibraryUpdateTask(WtaDatastore d) {
		setTag(UUID.randomUUID().toString());
 		this.d = d;
 	}
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		postError("Unpacking library data on first use, please be patient. This will continue in the background");
 	}
 	@Override
 	protected Integer doInBackground(JSONObject... params) {
 		int count = params.length;
 		for (int ob=0;ob<count;ob++){
 			try {
 				JSONObject result = params[ob];
 				
 				// first count the number of entries
 				JSONArray landmarks = result.names();
 				total_records += landmarks.length();
 				for (int i = 0; i < landmarks.length(); i++) {
 					String landmark_name = landmarks.getString(i);
 					JSONObject landmark_map = result.getJSONObject(landmark_name);
 					JSONArray locations = landmark_map.names();
 					total_records += locations.length();
 					for (int j = 0; j < locations.length(); j++) {
 						JSONArray stops = landmark_map.getJSONArray(locations.getString(j));
 						total_records += stops.length();
 					}
 				}
 				postProgressMax(total_records);
 				
 				// now process the entries
 				for (int i = 0; i < landmarks.length(); i++) {
 					String landmark_name = landmarks.getString(i);
 					JSONObject landmark_map = result.getJSONObject(landmark_name);
 					JSONArray locations = landmark_map.names();
 					for (int j = 0; j < locations.length(); j++) {
 						String location_name = locations.getString(j);
 						JSONArray stops = landmark_map.getJSONArray(locations.getString(j));
 						for (int k = 0; k < stops.length(); k++) {
 							JSONArray stop = stops.getJSONArray(k);
 							int stop_id = stop.getInt(0);
 							String name = stop.getString(1);
 							String alias = null;
 							if (stop.length() > 2)
 								alias = stop.getString(2);
 							// add the stops to the location
 							d.addLocation(location_name, stop_id, name, alias);
 							raw_progress += 1;
 							postProgress(raw_progress);
 						}
 						// add the location to the landmark
 						long tag_id = d.createOrUpdateTag(null, location_name, null, false);
 						d.setTag(tag_id, landmark_name, TagEntryType.TAG_NAME, 50);
 						raw_progress += 1;
 					}
 					// add new landmarks to root
 					d.setTag(d.getTagId(landmark_name), WtaDatastore.TAG_ROOT, TagEntryType.TAG_NAME, 20);
 					raw_progress += 1;
 				}
 			} catch (JSONException e) {
 				appendError("Error unpacking json file, try again under options");
 			}
 		}
 		return Integer.valueOf(0);
 	}
 }
