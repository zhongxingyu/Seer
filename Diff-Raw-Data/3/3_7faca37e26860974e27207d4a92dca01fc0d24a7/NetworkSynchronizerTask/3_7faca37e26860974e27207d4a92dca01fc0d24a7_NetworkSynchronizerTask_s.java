 /*
  * Copyright 2013 Sergio Garcia Villalonga (yayalose@gmail.com)
  *
  * This file is part of PalmaBici.
  *
  *    PalmaBici is free software: you can redistribute it and/or modify
  *    it under the terms of the Affero GNU General Public License version 3
  *    as published by the Free Software Foundation.
  *
  *    PalmaBici is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    Affero GNU General Public License for more details
  *    (https://www.gnu.org/licenses/agpl-3.0.html).
  *    
  */
 
 package com.poguico.palmabici.network.synchronizer;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.poguico.palmabici.DatabaseManager;
 import com.poguico.palmabici.parsers.Parser;
 import com.poguico.palmabici.util.NetworkInformation;
 import com.poguico.palmabici.util.Station;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class NetworkSynchronizerTask {
 	private static final int    HTTP_STATUS_OK = 200;
 	private static final String STR_HTTP_STATUS_OK = "200";
 	private static final String URL = "http://api.citybik.es/palma.json";
 	
 	public static void synchronize(Context context, NetworkSyncCallback callback) {
 		SynchronizeTask task = new SynchronizeTask(context, callback);
 		task.execute((Void [])null);
 	}
 	
 	private static class SynchronizeTask extends AsyncTask<Void, Void, String> {
 		private Context             context;
 		private NetworkSyncCallback callback;
 		private NetworkInformation  network;
 		
 		public SynchronizeTask (Context context, NetworkSyncCallback callback) {
 			this.context  = context;
 			this.callback = callback;
 		}
 		
 		@Override
 		protected String doInBackground(Void... params) {
 			long   lastUpdateTime = 0;
 			String[] syncResult;
 			ArrayList <Station> parsedNetowrk = null;
 			DatabaseManager dbManager =
     				DatabaseManager.getInstance(context);
 			
 			syncResult = getNetworkInfo();
 			if (syncResult[0].equals("200")) {
 				lastUpdateTime = Calendar.getInstance().getTimeInMillis();
 				parsedNetowrk  = Parser.parseNetworkJSON(syncResult[1]);
 				
 				network = NetworkInformation.getInstance(context);
 				network.setNetwork(parsedNetowrk);
 	    		network.setLastUpdateTime(lastUpdateTime);
 	    		
 	    		dbManager.saveLastStationNetworkState(parsedNetowrk);
 	    		dbManager.saveLastUpdateTime(lastUpdateTime);
 			}
     		
 			return syncResult[0];
 		}
 		
 		protected void onPostExecute(String result) {
 			if (result.equals(STR_HTTP_STATUS_OK)) {
 				callback.onNetworkSynchronized(network.getLastUpdateTime());
 			} else {
 				callback.onNetworkError(result);
 			}
 		}
 	}
 	
 	private static String[] getNetworkInfo() {
 		StringBuilder builder = new StringBuilder();
 		HttpClient    client  = new DefaultHttpClient();
 		HttpGet 	  request = new HttpGet(URL);
 		String[]      ret     = new String[2];
 		String		  line;
 		
 		try {
 			HttpResponse response = client.execute(request);
 			StatusLine   status_line = response.getStatusLine();
 			ret[0] = String.valueOf(status_line.getStatusCode());
 			if (status_line.getStatusCode() == HTTP_STATUS_OK) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				InputStreamReader content_reader = new InputStreamReader(content);
 				BufferedReader reader = new BufferedReader(content_reader);
 				
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 				ret[1] = builder.toString();
 			} else {
 				Log.e(NetworkSynchronizerTask.class.toString(),
 						"Failed to download file");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 }
