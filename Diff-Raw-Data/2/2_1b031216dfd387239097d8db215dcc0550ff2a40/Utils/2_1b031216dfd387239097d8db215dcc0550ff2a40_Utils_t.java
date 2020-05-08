 package com.plingnote;
 import android.content.Context;
 import android.graphics.Rect;
 import android.util.DisplayMetrics;
 
 public class Utils {
 	public static Rect getScreenPixels(Context context){
 		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);
 	}
 }
