 package com.dbstar.service;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.dbstar.app.GDApplication;
 
 public class DeviceInitController {
 	public static final String TAG = "DeviceInitController";
 	public static final String FlagFile = "flag";
 
 	public static boolean isBootFirstTime() {
 		boolean firstTime = true;
 
 		Context context = GDApplication.getAppContext();
 
 		try {
 			int count = 0;
 			byte[] buf = new byte[100];
 			FileInputStream in = context.openFileInput(FlagFile);
 			BufferedInputStream bIn = new BufferedInputStream(in);
 			count = bIn.read(buf, 0, buf.length);
 			bIn.close();
 
 			if (count > 0) {
 				String vlues = new String(buf, 0, count);
 
 				if ((vlues.compareTo("1")) == 0) {
 					firstTime = false;
 				}
 			}
 
 		} catch (FileNotFoundException e1) {
 			;
 		} catch (IOException e) {
 			e.printStackTrace();
 			context.deleteFile(FlagFile);
 		}
 
 		return firstTime;
 	}
 
 	public static void handleBootFirstTime() {
 
 		Context context = GDApplication.getAppContext();
 
 		try {
 			String setflagValues = "1";
 			byte[] setflag = setflagValues.getBytes();
 			FileOutputStream fos = context.openFileOutput(FlagFile,
 					Context.MODE_PRIVATE);
 			fos.write(setflag);
 
 			fos.close();
 		} catch (Exception e) {
 			Log.e(TAG,
 					"Exception Occured: Trying to add set setflag : "
 							+ e.toString());
 			Log.e(TAG, "Finishing the Application");
 		}
 	}
 
 }
