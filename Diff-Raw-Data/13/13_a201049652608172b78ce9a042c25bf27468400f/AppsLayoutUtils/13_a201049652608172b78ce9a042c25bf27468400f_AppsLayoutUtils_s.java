 package com.ps.sleek.utils;
 
 import com.ps.sleek.manager.AppsManager;
 
 import android.content.Context;
 
 
 public class AppsLayoutUtils {
 	
 	private static final int NUM_COLUMNS = 5;
 	
 	public static int getColumnPixels(Context context) {
 		return (DimensionUtils.getWidthPixels(context) - 
 				(NUM_COLUMNS + 1) * DimensionUtils.getPixelForDp(context, 10))/NUM_COLUMNS;
 	}
 	
 	public static int getRowPixels(Context context) {
		return (getColumnPixels(context) + 
 				2 * DimensionUtils.getPixelForSp(context, (float) (12 * 1.5)) + 
 				2 * DimensionUtils.getPixelForDp(context, 2));
 	}
 	
 	public static int getNumRows(Context context) {
 		return DimensionUtils.getHeightPixels(context) / getRowPixels(context);
 	}
 	
 	public static int getNumPages(Context context) {
		return AppsManager.getInstance(context).getApplications().size() / (NUM_COLUMNS * getNumRows(context));
 	}
 	
 	public static int getVerticalSpacing(Context context) {
 		int numRows = getNumRows(context);
 		return (DimensionUtils.getHeightPixels(context) - 
 				numRows * getRowPixels(context) - 
 				DimensionUtils.getPixelForDp(context, 10))/numRows;
 	}
 }
