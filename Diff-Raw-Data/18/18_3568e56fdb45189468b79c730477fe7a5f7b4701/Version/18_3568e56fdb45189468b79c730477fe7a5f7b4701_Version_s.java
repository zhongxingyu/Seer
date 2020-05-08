 package com.htb.cnk.data;
 
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Log;
 
 public class Version {
 	final String TAG = "version";
 	public final String UPDATE_SAVENAME = "cnk.apk";
 	final int minor = 1;
	final int build = 1;
 	int ver;
 	
 	public Version(Context context) {
 		ver = -1;
 		try {
 			ver = context.getPackageManager().getPackageInfo(
 					"com.htb.cnk", 0).versionCode;
 			Log.i(TAG, "ver_major_local:" + ver);
 		} catch (NameNotFoundException e) {
 			Log.e(TAG, e.getMessage());
 		}
 	}
 	
 	public boolean isUpdateNeed(int ver, int minor, int build) {
 		if (this.ver > ver) {
 			return false;
 		}
 		if (this.minor > minor) {
 			return false;
 		}
 		
 		if (this.build >= build) {
 			return false;
 		}
 		return true;
 	}
 	
 	public String getVersion() {
 		return Integer.toString(ver) + "." + Integer.toString(minor) + "." + Integer.toString(build);
 	}
 }
