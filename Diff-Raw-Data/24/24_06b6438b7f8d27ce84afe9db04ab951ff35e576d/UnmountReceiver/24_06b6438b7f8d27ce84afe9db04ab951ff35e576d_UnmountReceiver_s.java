 package com.tortel.externalize;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 public class UnmountReceiver extends BroadcastReceiver {
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.v("Unmount receiver");
 		intent.getExtras();
 		//If the mSD was removed
		if(!Paths.extFile.exists()){
 			SharedPreferences prefs = context.getSharedPreferences("default", 0);
 			Shell sh = new Shell();
 			if(prefs.getBoolean("images", true)){
 				//Unmount it
 				sh.exec("umount "+Paths.internal+Paths.dir[Paths.IMAGES]);
 			}
 			if(prefs.getBoolean("downloads", false)){
 				sh.exec("umount "+Paths.internal+Paths.dir[Paths.DOWNLOADS]);
 				//Unmount
 			}
 			sh.exit();
 		}
 	}
 
 }
