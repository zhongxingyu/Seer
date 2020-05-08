 package com.example.testvideocam;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 
 import ru.pvolan.trace.Trace;
 
 import android.os.*;
 
 public class MediaFileManager 
 {
 	private final static String subdirname = "TestCamApp";
 	
 	
 	public static File getOutputVideoFile()
 	{
 		try{
 		    // To be safe, you should check that the SDCard is mounted
 		    // using Environment.getExternalStorageState() before doing this.
 	
 		    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
		              Environment.DIRECTORY_MOVIES), subdirname);
 		    // This location works best if you want the created images to be shared
 		    // between applications and persist after your app has been uninstalled.
 	
 		    // Create the storage directory if it does not exist
 		    if (! mediaStorageDir.exists()){
 		        if (! mediaStorageDir.mkdirs()){
 		        	Trace.Print("failed to create video file directory");
 		            return null;
 		        }
 		    }
 	
 		    // Create a media file name
 		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		    File mediaFile;
 		    
 		    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 		        "VID_"+ timeStamp + ".mp4");
 		    
 	
 		    return mediaFile;
 		}
 		catch (Exception e) 
 		{
 			Trace.Print(e);
 			return null;
 		}
 	}
 }
