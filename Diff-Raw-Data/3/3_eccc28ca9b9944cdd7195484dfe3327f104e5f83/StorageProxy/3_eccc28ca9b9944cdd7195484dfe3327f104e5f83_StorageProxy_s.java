 package com.delin.speedlogger.Utils;
 
 import java.io.File;
 
 import android.content.Context;
 import android.os.Environment;
 
 public class StorageProxy {
 	public final static String APP_NAME = "SpeedLogger";
 	public final static String FILE_GPS = "FileGPS";
 	String mWorkingDir = new String();
 	String mFileGPSDir = new String();
 	static StorageProxy instance = null;
 	static public StorageProxy GetInstance(Context context) {
 		return (instance == null) ? instance = new StorageProxy(context) : instance;
 	}
 	
 	static public StorageProxy GetInstance() { // see the great architecture!
 		return instance;
 	}
 	
 	private StorageProxy(Context context) {
 		final String status = Environment.getExternalStorageState();
 		if (status.equals(Environment.MEDIA_MOUNTED)) { // debug works on sd
 			mWorkingDir = Environment.getExternalStorageDirectory().getPath() + File.separator + APP_NAME;
 			File dir = new File(mWorkingDir);
 			if (dir.isDirectory() == false) { // create dir if needed
 				dir.mkdir();
 			}
 		}
 		else { // otherwise use internal memory			
 			mWorkingDir = context.getApplicationInfo().dataDir; // should exist
 		}
 		
 		mFileGPSDir = mWorkingDir + File.separator + FILE_GPS;
 		File dir = new File(mFileGPSDir);
 		if (dir.isDirectory() == false) { // create dir to storage file gpxies
 			dir.mkdir();
 		}
 		
 	}
 	
 	public String GetWorkingDir() {
 		return mWorkingDir;
 	}
 
 	public String GetFileGPSDir() {
 		return mFileGPSDir;
 	}
 }
