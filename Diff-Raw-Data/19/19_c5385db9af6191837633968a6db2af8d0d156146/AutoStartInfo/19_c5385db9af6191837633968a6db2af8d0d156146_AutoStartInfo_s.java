 package com.zjuhjz.yacleaner;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.util.Log;
 
 import com.zjuhjz.yacleaner.tool.Constants;;
 
 public class AutoStartInfo {
 	//all broadcast Actions
 	
 	List<HashMap<String,Object>> appInfoList = null;
 	
 	//Intents to App
 	List<HashMap<String,Object>> intentsInfoList = null;
 	List<HashMap<String,Object>> intentsAppInfoList = null;
 	HashMap<String,Object> intentItem = null;
 	HashMap<String,Object> intentsAppInfo = null;
 	
 	
 	
 	private static final String TAG = "yacleanerlog";
 	PackageManager packageManager = null;
 	Context context = null;
 	AutoStartInfo(Context context){
 		this.context = context;
 		intentsInfoList = new ArrayList<HashMap<String,Object>>();
 		packageManager = context.getPackageManager();
 		refresh();
 	}
 	public void refresh(){
 		loadIntentsInfo();
 	}
 	private void loadIntentsInfo(){
 		String intentName;
 		Log.d(TAG, "begin");
 		Intent intent;
 		List<ResolveInfo> activities;
 		for(int i = 0; i < Constants.broadcastActions.length; ++i){
 			intentName = Constants.broadcastActions[i][0];
			Log.d(TAG, intentName);
 			intent= new Intent(intentName);
 			activities = packageManager.queryBroadcastReceivers(intent, 0);
 			
 			for (ResolveInfo resolveInfo : activities) {
 	            ActivityInfo activityInfo = resolveInfo.activityInfo;
 	            if(intentItem==null){
 	            	intentItem = new HashMap<String,Object>();
 	            	intentItem.put("IntentName", intentName);
 	            	intentsAppInfoList = new ArrayList<HashMap<String,Object>>();
 	            	intentItem.put("appInfoList", intentsAppInfoList);
 	            	intentsInfoList.add(intentItem);
 	            }
 	            HashMap<String,Object> item = new HashMap<String,Object>();
 	            
 	            if (activityInfo != null){
 	            	intentsAppInfo = new HashMap<String,Object>();
 	            	intentsAppInfo.put("package_name", activityInfo.packageName);
 	            	intentsAppInfoList.add(intentsAppInfo);
 	            }
 	        }
 			
 			
 			
 			intentItem = null;
 		}
 		
         
         
 	}
 	
 	
 }
