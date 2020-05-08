 package com.yugy.qianban.asisClass;
 
 import android.content.Context;
 
 public class FuncInt {
 
 	public static int dp(Context context, float dp){
 		final float scale = context.getResources().getDisplayMetrics().density;  
         return (int) (dp * scale + 0.5f);
 	}
 	
 	public static int getScreenWidth(Context context){
 		
 		return context.getResources().getDisplayMetrics().widthPixels;
 		
 	}
 }
