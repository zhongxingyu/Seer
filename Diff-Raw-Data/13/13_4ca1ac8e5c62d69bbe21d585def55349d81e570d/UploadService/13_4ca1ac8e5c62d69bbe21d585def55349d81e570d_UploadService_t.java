 package com.parent.management.service;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.parent.management.ManagementApplication;
 import com.parent.management.R;
 import com.parent.management.jsonclient.JSONClientException;
 import com.parent.management.jsonclient.JSONHttpClient;
 import com.parent.management.jsonclient.JSONParams;
 import com.parent.management.monitor.Monitor;
 import com.parent.management.monitor.Monitor.Type;
 
 public class UploadService extends Service {
     private static final String TAG = ManagementApplication.getApplicationTag() + "." +
             UploadService.class.getSimpleName();
     
     private static WifiManager.WifiLock mWifilock = null;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
     public void onCreate()
     {
 
     }
 	
 	@Override
     public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
     {
 	    Log.d(TAG, "----> Upload Service start.");
 	    if (mWifilock == null)
 	    {
 	        mWifilock = ((WifiManager)getSystemService("wifi")).createWifiLock("PM");
 	        mWifilock.acquire();
 	    }
 	    //getLock(this).acquire();
         uploadJob();
 	    mWifilock.release();
 	    mWifilock = null;
 	    stopSelf();
         return 1;
     }
 	
 	@Override
     public void onDestroy() {
 	    if (mWifilock != null) {
 	        mWifilock.release();
 	        mWifilock = null;
         }
 	}
 	
 	private void uploadJob() {
 	    if (ManagementApplication.monitorList == null) {
 	        return;
 	    }
 	    
 	    try {
     	    // prepare upload data
             JSONArray jsonParams = new JSONArray();
             Iterator<Entry<Type, Monitor>> iterator = ManagementApplication.monitorList.entrySet().iterator();
             while (iterator.hasNext()) {
                 Entry<Type, Monitor> entry = iterator.next();
                 Log.d(TAG, "----> prepare upload data for : " + entry.getKey());
 
                JSONArray data = entry.getValue().extractDataForSend();
                if (data != null) {
                     JSONObject jsonRows = new JSONObject();
                     jsonRows.put(JSONParams.DATA_TYPE, entry.getKey().ordinal());
                    jsonRows.put(JSONParams.DATA, data);
                     jsonParams.put(jsonRows);
                 }
             }
             
             if (jsonParams.length() > 0) {
                 // Create client specifying JSON-RPC version 2.0
                 JSONHttpClient client = new JSONHttpClient(this.getResources().getString(R.string.server_address));
                 client.setConnectionTimeout(2000);
                 client.setSoTimeout(2000);
                 
                 JSONArray failed = client.doUpload(jsonParams);
                 Log.d(TAG, "Failed [" + failed.length() + "] records for this upload");
                 
                 HashMap<Integer, JSONArray> failedList = new HashMap<Integer, JSONArray>();
                 for (int i=0; i<failed.length(); i++) {
                     JSONObject data = (JSONObject) failed.opt(i);
                     failedList.put(
                             Integer.valueOf(data.getInt(JSONParams.DATA_TYPE)), 
                             data.getJSONArray(JSONParams.RESPONSE_FAILED_LIST));
                     
                 }
                 
                 // prepare update DB
                 while (iterator.hasNext()) {
                     Entry<Type, Monitor> entry = iterator.next();
                     Integer key = Integer.valueOf(entry.getKey().ordinal());
                     
                     if (failedList.containsKey(key)) {
                         entry.getValue().updateStatusAfterSend(failedList.get(key));
                     } else {
                         entry.getValue().updateStatusAfterSend(null);
                     }
                 }
                 
                 // get the new configuration from server
                 int time = client.doConfiguration();
                 Log.d(TAG, "The new interval time is " + time);
                 ManagementApplication.getConfiguration().setIntervalTime(time);
             }
 	    } catch (JSONException e) {
 	        Log.e(TAG, "Invalid JSON request: " + e.getMessage());
 	    } catch (JSONClientException e1) {
 	        Log.e(TAG, "Upload failed: " + e1.getMessage());
 	    }
         
         return;
 	}
 }
