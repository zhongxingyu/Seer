 package com.tihonchik.lenonhonor360.services;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.util.Log;
 
 public class BlogPullService extends IntentService {
 	public static final String PARAM_IN_MSG = "IN_MSG";
     public static final String PARAM_OUT_MSG = "OUT_MSG";
 
 	public BlogPullService() {
 		super("BlogPullService");
 		Log.d("LH360", " > BlogPullServics started...");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent workIntent) {
 		Log.d("LH360", " > BlogPullServics checking-in...");
 		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
 		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 		broadcastIntent.putExtra(PARAM_OUT_MSG, "");
 		sendBroadcast(broadcastIntent);
 	}
 }
