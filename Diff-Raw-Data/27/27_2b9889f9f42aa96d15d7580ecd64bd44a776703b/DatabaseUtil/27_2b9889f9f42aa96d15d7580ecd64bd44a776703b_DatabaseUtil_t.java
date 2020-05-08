 package com.thunderunited.meterstand;
 
 import android.os.Environment;
 
 public class DatabaseUtil {
 
 	public static final String DB_NAME = "/foto.db";
	private static final String TAG = DatabaseUtil.class.getName();
 
 	public static String getDatabasePath() {
 		return Environment.getExternalStorageDirectory() + "/"
 				+ DatabaseUtil.class.getPackage().getName() + "/"
 				+ DatabaseUtil.DB_NAME;
 	}
 
 }
