 package com.novo.util;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 public class DebugUtil {
 	private static boolean isDebugOpen = true;
 
 	public static boolean isDebugOpen() {
 		return isDebugOpen;
 	}
 
 	public static void setDebugOpen(boolean isDebugOpen) {
 		DebugUtil.isDebugOpen = isDebugOpen;
 	}
 	
 	public static void LogDebug(String tag, String msg){
 		if(isDebugOpen){
 			Log.d(tag,tag+": "+msg);
 		}
 	}
 	
 	public static void LogError(String tag, String msg){
 		if(isDebugOpen){
 			Log.e(tag,tag+": "+msg);
 		}
 	}
 	
 	public static void ToastShow(Context context, String content){
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
 	}
 }
