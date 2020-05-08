 package com.rogoapp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 
 import android.content.Context;
 import android.util.Log;
 
 // how to write to files:
 // http://developer.android.com/guide/topics/data/data-storage.html#filesInternal
 
 // a detailed case here:
 // http://web.archive.org/web/20110803104357/http://www.flexjockey.com/2011/03/create-a-pretty-simple-cache-for-android/
 
 public class CacheClient {
 
 	final Context context;
 	
 	//Error messages
 	public static final String ERROR_1 = "ERROR_1: File not found";
 	public static final String ERROR_2 = "ERROR_2: File cannot be written to.";
 	public static final String ERROR_3 = "ERROR_3: File data cannot be accessed.";
 	
 	public CacheClient(Context context) { // from an Activity, pass "this"
 		this.context = context;
 	}
 	
 	public void saveFile(String filename, String content) {
 		File cacheDir = context.getCacheDir();
 		File file = new File(cacheDir, filename);
 
 		FileOutputStream fos;
 		try {
 			fos = new FileOutputStream(file);
 			fos.write(content.getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Log.d(ERROR_1, "Searching for " + filename);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.d(ERROR_2, "Writing to " + filename);
 		}
 	}
 	
 	public void addFile(String filename, String content){
 		File cacheDir = context.getCacheDir();
 		File file = new File(cacheDir, filename);
 
 		FileOutputStream fos;
 		try {
 			fos = new FileOutputStream(file, true);
 			fos.write(content.getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Log.d(ERROR_1, "Searching for " + filename);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.d(ERROR_2, "Writing to " + filename);
 		}
 	}
 
 	
 	public String loadFile(String filename){
 		File cacheDir = context.getCacheDir();
 		File file = new File(cacheDir, filename);
 		
 		StringBuilder contentString = new StringBuilder();
 
 		FileInputStream fis;
 		try {
 			fis = new FileInputStream(file);
 			int content;
 			while((content = fis.read()) != -1){
 				contentString.append((char)content);
 			}
 			fis.close();
 
 			return contentString.toString();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Log.d(ERROR_1, "Searching for " + filename);
 			return ERROR_1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.d(ERROR_3, "Reading from " + filename);
			return ERROR_3;
 		}
 
 	}
 	
 	public boolean isEmpty(String filename){
 		File cacheDir = context.getCacheDir();
 		File file = new File(cacheDir, filename);
 		return !((int) file.length() > 0);
 	}
 	
 	
 	
 	public int lines(String filename){
 		
 		try{
 			
 			File cacheDir = context.getCacheDir();
 			File file = new File(cacheDir,filename); 
 
 			if(file.exists()){
 
 				FileReader fr = new FileReader(file);
 				LineNumberReader lnr = new LineNumberReader(fr);
 
 				int linenumber = 0;
 
 				while (lnr.readLine() != null){
 					linenumber++;
 				}
 
 				lnr.close();
 				return linenumber;
 
 			}else{
 				System.out.println("File does not exists!");
 				return 0;
 			}
 
 		}catch(IOException e){
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 }
