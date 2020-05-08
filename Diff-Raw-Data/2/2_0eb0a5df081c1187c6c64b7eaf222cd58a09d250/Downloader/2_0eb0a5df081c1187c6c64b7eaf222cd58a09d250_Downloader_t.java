 package com.njzk2.jsontolistview;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 
 public class Downloader extends AsyncTask<String, Void, JSONArray> {
 	private static final String TAG = Downloader.class.getSimpleName();
 	ArrayAdapter<String> mAdapter;
 
 	public Downloader(ArrayAdapter<String> mAdapter) {
 		super();
 		this.mAdapter = mAdapter;
 	}
 
 	@Override
 	protected JSONArray doInBackground(String... params) {
 		try {
 			JSONObject response = new JSONObject(EntityUtils.toString(
 					new DefaultHttpClient().execute(new HttpGet(params[0])).getEntity(), "UTF-8"));
 
 			if (response.optBoolean("success", false)) {				
 				return response.getJSONObject("response").getJSONArray("photos");
 			} else {
 				Log.w(TAG, "Request failed. Details: " + response.toString(2));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(JSONArray images) {
 		if (images != null) {
 			for (int i = 0; i < images.length(); i++) {
 				JSONObject object = images.optJSONObject(i);
 				if (object != null) {
					mAdapter.add(object.optString("description"));
 				}
 			}
 		}
 	}
 
 }
