 /*
  *  Copyright (C) 2010 GSyC/LibreSoft, Universidad Rey Juan Carlos.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see http://www.gnu.org/licenses/. 
  *
  *  Author : Raúl Román López <rroman@gsyc.es>
  *
  */
 
 package com.libresoft.apps.ARviewer.File;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.Calendar;
 
 import android.graphics.Bitmap;
 import android.os.Environment;
 import android.util.Log;
 
 public class FileManager{
 	public static final int STAT_FPS = 0;
 	public static final int STAT_OPENCV = 1;
 	
	private static final String SD_DIRECTORY = "/sdcard/arviewer/";
	private static final String SCREENSHOT_DIRECTORY = "screenshots/";
	private static final String STATS_DIRECTORY = "stats/";
	public static final String PHOTO_DIRECTORY = SD_DIRECTORY + "photo/";
	public static final String AUDIO_DIRECTORY = SD_DIRECTORY + "audio/";
 	private static final String SCREENSHOT = "screenshot_";
 	private static final String STATS_FPS = "fps_";
 	private static final String STATS_OPENCV = "opencv_";
 	
 	private static FileManager singleton = null;
 	
 	private FileManager(){
 		File dir = new File(SD_DIRECTORY);
 		if (!dir.exists())
 			dir.mkdir();
 		dir = new File(PHOTO_DIRECTORY);
 		if (!dir.exists())
 			dir.mkdir();
 		dir = new File(AUDIO_DIRECTORY);
 		if (!dir.exists())
 			dir.mkdir();
 	}
 	
 	public static FileManager getInstance(){
 		if(singleton == null)
 			singleton = new FileManager();
 		return singleton;
 	}
 	
 	private boolean checkSD(){
 		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
 	}
 	
 	public boolean exportStatFile(String data, int format){
 		if(!checkSD())
 			return false;
 		
 		File dir = new File(SD_DIRECTORY + STATS_DIRECTORY);
 		if (!dir.exists())
 			dir.mkdir();
 		
 		String date = Calendar.getInstance().get(Calendar.YEAR) + "-" +
 			Calendar.getInstance().get(Calendar.MONTH) + "-" +
 			Calendar.getInstance().get(Calendar.DAY_OF_MONTH + 1) + "-" +
 			Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "-" +
 			Calendar.getInstance().get(Calendar.MINUTE) + "-" +
 			Calendar.getInstance().get(Calendar.SECOND);
 		
 		String filename = "";
 		if(format == STAT_FPS)
 			filename += STATS_FPS + date + ".csv";
 		else
 			filename += STATS_OPENCV + date + ".csv";
 		
 		File outputFile = new File(SD_DIRECTORY + STATS_DIRECTORY + filename);
 		
 		try {
 			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile,false));
 			out.write(data);
 			out.flush();
 			out.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			Log.e("FileManager", "", e);
 			return false;
 		} 
 		
 		return true;
 	}
 	
 	public boolean exportImageFile(Bitmap bm){
 		if(!checkSD())
 			return false;
 		
 		File dir = new File(SD_DIRECTORY + SCREENSHOT_DIRECTORY);
 		if (!dir.exists())
 			dir.mkdir();
 		
 		String date = Calendar.getInstance().get(Calendar.YEAR) + "-" +
 		Calendar.getInstance().get(Calendar.MONTH) + "-" +
 		Calendar.getInstance().get(Calendar.DAY_OF_MONTH + 1) + "-" +
 		Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "-" +
 		Calendar.getInstance().get(Calendar.MINUTE) + "-" +
 		Calendar.getInstance().get(Calendar.SECOND);
 		
 		String filename = SCREENSHOT + date + ".jpg";
 		
 		try {
 			FileOutputStream out = new FileOutputStream(SD_DIRECTORY + SCREENSHOT_DIRECTORY + filename);
 			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			Log.e("FileManager", "", e);
 			return false;
 		} 
 		
 		return true;
 	}
 	
 }
