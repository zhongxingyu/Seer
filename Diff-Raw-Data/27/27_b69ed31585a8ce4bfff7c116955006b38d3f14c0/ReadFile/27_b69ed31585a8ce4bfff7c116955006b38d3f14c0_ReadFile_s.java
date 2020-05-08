 package com.group2.finger_occ_demo.io;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import org.apache.commons.io.IOUtils;
 
 import android.content.Context;
 import android.content.res.AssetManager;
import android.util.Log;
 
 /**
  * Reads in the specified JSON file, needs the AssetManager of the current
  * context (likely this in main class) to read files from local storage. 
  */
 
 public class ReadFile {
 	
 	private AssetManager assets;
 	private Context context;
 	
 	public ReadFile(AssetManager assets, Context context){
 		this.assets = assets;
 		this.context = context;
 	}
 	
 	/**
 	 * Returns a string of the file read. First tries assets then if not there
 	 * (this is then a fresh load), then go to internal memory. Returns nothing
 	 * if there was no file.
 	 */
 	public String get(String fileName){
 		String data = tryInternal(fileName);
 		if (data != null)
 			return data;
 		
         try{
         	return IOUtils.toString(assets.open(fileName), "UTF-8");
         }
         catch (Exception e){}
         
         return null;
 	}
 
 	/**
 	 * Try to read from an internal file. Success when file string is returned, null
 	 * when failure.
 	 */
 	private String tryInternal(String fileName) {
 		try {
 			return IOUtils.toString(context.openFileInput(fileName), "UTF-8");
 		} catch (FileNotFoundException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
 	}
 }
