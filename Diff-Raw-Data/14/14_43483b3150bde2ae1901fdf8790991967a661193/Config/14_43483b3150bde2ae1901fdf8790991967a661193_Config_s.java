 package com.bettername.thepokemonone;
 import android.content.Context;
 import android.provider.Settings.Secure;
 
 
 public class Config {
 
 	private static Context context;
 	public static void setContext(Context context) {
 		Config.context = context;
 	}
 	
 	public static String getUdid() {
 		String android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
 		return android_id;
     }
 }
