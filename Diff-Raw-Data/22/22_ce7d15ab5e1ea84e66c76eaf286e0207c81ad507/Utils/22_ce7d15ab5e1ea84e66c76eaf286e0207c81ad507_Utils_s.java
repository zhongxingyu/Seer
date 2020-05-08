 /*
  * lo-lo Copyright (C) 091 Labs 2012 info@091labs.com
  *
  * lo-lo is free software: you can
  * redistribute it and/or modify it under the terms
  * of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later
  * version.
 
  * lo-lo is distributed in the hope that it
  * will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
 
  * You should have received a copy of the GNU
  * General Public License along with this program.
  * If not, see http://www.gnu.org/licenses.
  */
 
 package com.codeskraps.lolo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class Utils {
 	private static final String TAG = Utils.class.getSimpleName();
 	private static final String URL = "http://scruffy.091labs.com/lolo/json/status.json";
 
 	public static boolean isNetworkAvailable(Context context) {
 		ConnectivityManager cm = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) { return true; }
 		return false;
 	}
 
 	public static boolean getLolo() throws IOException {
 		long startTime = System.currentTimeMillis();
 		Log.d(TAG, "download begining");
 		Log.d(TAG, "download url:" + URL);
 
 		HttpParams httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
 		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
 
 		HttpClient client = new DefaultHttpClient(httpParameters);
 		HttpGet request = new HttpGet(URL);
 		HttpResponse response = client.execute(request);
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
 				.getContent(), "UTF-8"));
 		String json = reader.readLine();
 		reader.close();
 
 		JSONTokener tokener = new JSONTokener(json);
 
 		boolean lolo = false;
 		try {
 			JSONObject finalResult = new JSONObject(tokener);
			lolo = Boolean.getBoolean(finalResult.getString("open"));
 			Log.d(TAG, "lolo: " + lolo);
 		} catch (JSONException e) {
 			Log.e(TAG, e.getMessage());
 		}
 
 		Log.d(TAG, "download ready in " + ((System.currentTimeMillis() - startTime) / 1000)
 				+ " sec");
 		return lolo;
 	}
 	
 	public static PendingIntent getOnTouchIntent(Context context) {
 		PendingIntent pendingIntent = null;
 		Intent intent = null;
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		int onClick = Integer.parseInt(prefs.getString(Constants.ONCLICK, "0"));
 		
 		switch (onClick) {
 		case 0:
 			intent = new Intent("com.codeskraps.lol.DO_NOTHING");
 			pendingIntent = PendingIntent.getActivity(context, 0, intent,
 					PendingIntent.FLAG_UPDATE_CURRENT);
 			break;
 
 		case 1:
 			intent = new Intent();
 			intent.setAction(Constants.BROADCAST_RECEIVER);
 			pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
 					PendingIntent.FLAG_UPDATE_CURRENT);
 			break;
 
 		case 2:
 			intent = new Intent(context, PrefsActivity.class);
 			pendingIntent = PendingIntent.getActivity(context, 0, intent,
 					PendingIntent.FLAG_UPDATE_CURRENT);
 			break;
 
 		case 3:
 			intent = new Intent(Intent.ACTION_VIEW);
 			String url = prefs.getString(Constants.EURL,
 					context.getString(R.string.prefsURL_default));
 			if (!url.startsWith("http://")) url = "http://" + url;
 			intent.setData(Uri.parse(url));
 			pendingIntent = PendingIntent.getActivity(context, 0, intent,
 					PendingIntent.FLAG_UPDATE_CURRENT);
 			break;
 		}
 		return pendingIntent;
 	}
 }
