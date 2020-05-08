 package com.yugy.qianban.asisClass;
 
 import android.content.Context;
<<<<<<< HEAD
 
 public class FuncInt {
 
 	public static int dp(Context context, float dp){
 		final float scale = context.getResources().getDisplayMetrics().density;  
         return (int) (dp * scale + 0.5f);
 	}
 	
 	public static int getScreenWidth(Context context){
 		
 		return context.getResources().getDisplayMetrics().widthPixels;
 		
 	}
	
=======
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class FuncInt {
	
	public static int dp(Context context, double d) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (d * scale + 0.5f);

	}
	
	public static int getScreenWidth(Context context){
		DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
	}
>>>>>>> 04c47ac922fd4430cb0a2c647602c4494d62a84a
 }
