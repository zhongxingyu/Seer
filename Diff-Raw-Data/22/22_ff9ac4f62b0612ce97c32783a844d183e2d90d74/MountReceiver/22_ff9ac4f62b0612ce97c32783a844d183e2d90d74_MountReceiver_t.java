 package com.tortel.externalize;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 public class MountReceiver extends BroadcastReceiver {
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.v("Mount receiver");
 		Log.v("Data: "+intent.getDataString());
		SharedPreferences prefs = context.getSharedPreferences("com.tortel.externalize_preferences", 0);
 		if(prefs.getBoolean("insert", false)){
 			if( (intent.getDataString()+"/" ).equals("file://"+Paths.external) ){
 				Log.v("Enabled, copying and linking dirs");
 				//If were supposed to do images, copy anything over and link
 				if(prefs.getBoolean("images", true)){
 					FileMover tmp = new FileMover(null, Paths.IMAGES);
 					tmp.start();
 				}
 				//If were supposed to move downloads
 				if(prefs.getBoolean("downloads", false)){
 					FileMover tmp = new FileMover(null, Paths.DOWNLOADS);
 					tmp.start();
 				}
 			}
 		} else {
 			Log.v("Not enabled, doing nothing");
 		}
 		
 	}
 
 }
