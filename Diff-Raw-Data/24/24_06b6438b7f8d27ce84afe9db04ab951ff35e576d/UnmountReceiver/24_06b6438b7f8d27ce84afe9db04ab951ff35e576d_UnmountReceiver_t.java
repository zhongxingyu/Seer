 package com.tortel.externalize;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 public class UnmountReceiver extends BroadcastReceiver {
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.v("Unmount receiver");
		Log.v("Data: "+intent.getDataString());
 		intent.getExtras();
 		//If the mSD was removed
		if( (intent.getDataString()+"/" ).equals("file://"+Paths.external) ){
 			SharedPreferences prefs = context.getSharedPreferences("default", 0);
 			Shell sh = new Shell();
 			if(prefs.getBoolean("images", true)){
 				//Unmount it
				Log.d("Unlinking images");
 				sh.exec("umount "+Paths.internal+Paths.dir[Paths.IMAGES]);
 			}
 			if(prefs.getBoolean("downloads", false)){
				Log.d("Unlinking downloads");
 				sh.exec("umount "+Paths.internal+Paths.dir[Paths.DOWNLOADS]);
 				//Unmount
 			}
 			sh.exit();
 		}
 	}
 
 }
