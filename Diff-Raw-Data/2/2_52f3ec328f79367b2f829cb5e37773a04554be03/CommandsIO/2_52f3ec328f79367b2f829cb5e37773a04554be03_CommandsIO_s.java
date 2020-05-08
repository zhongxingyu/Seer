 /*
  * Copyright (C) 2011 asksven
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.asksven.commandcenter.valueobjects;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.os.Environment;
 import android.util.Log;
 
 import com.asksven.commandcenter.exec.DataStorage;
 
 /**
  * @author sven
  *
  */
 public class CommandsIO
 {
 	private static String TAG = "CommandsIo";
 	private static CommandsIO m_instance = null;
 	private static Context m_ctx = null;
 	
 	public static CommandsIO getInstance(Context ctx)
 	{
 		if (m_instance == null)
 		{
 			m_instance = new CommandsIO();
 			m_ctx = ctx;
 		}
 		return m_instance;
 	}
 	
 	private CommandsIO()
 	{
 	}
 	
 	protected ArrayList<String> getCollectionFilenames()
 	{
 		ArrayList<String> myFiles = new ArrayList<String>();
 		
 		// create directories if the do not exist
 		if (!externalStorageEnvironmentReady())
 		{
 			createExternalStorageEnvironment();
 		}
 		
 		File path = getFileDir(m_ctx);
 
 		// list the files using our FileFilter
 		File[] files = path.listFiles(new CommandCollectionFileFilter());
 		for (File f : files)
 		{
 			myFiles.add(f.getName());
 		}
 		
 		return myFiles;
 	}
 
     protected boolean externalStorageEnvironmentReady()
     {
     	File path = getFileDir(m_ctx);
     	if (path != null)
     	{
     		return (path.exists());
     	}
     	else
     	{
     		return false;
     	}
     }
 
     protected void createExternalStorageEnvironment()
     {
     	File path = getFileDir(m_ctx);
     	
 		if (!DataStorage.isExternalStorageWritable())
 		{
 			Log.e(TAG, "External storage is not mounted or writable, aborting");
 			return;
 		}
 
 		try {
             // Make sure the application directory exists.
             path.mkdirs();
 
             // Very simple code to copy a picture from the application's
             // resource into the external file.  Note that this code does
             // no error checking, and assumes the picture is small (does not
             // try to copy it in chunks).  Note that if external storage is
             // not currently mounted this will silently fail.
 //            InputStream is = ctx.getResources().openRawResource(R.drawable.balloons);
 //            OutputStream os = new FileOutputStream(file);
 //            byte[] data = new byte[is.available()];
 //            is.read(data);
 //            os.write(data);
 //            is.close();
 //            os.close();
 
         } catch (Exception e) {
             // Unable to create file, likely because external storage is
             // not currently mounted.
             Log.e("TAG", "Error creating environment on external storage");
         }
     }
 
     /**
      * A class that implements the Java FileFilter interface for CommandCollections.
      */
     public class CommandCollectionFileFilter implements FileFilter
     {
       private final String[] okFileExtensions = 
         new String[] {"json"};
 
       public boolean accept(File file)
       {
         for (String extension : okFileExtensions)
         {
           if (file.getName().toLowerCase().endsWith(extension))
           {
             return true;
           }
         }
         return false;
       }
     }
     
     /**
      * Copy the *.json files to the private storage
      */
     protected void CopyAssets()
     {
         AssetManager assetManager = m_ctx.getAssets();
         String[] files = null;
         try
         {
             files = assetManager.list("");
         }
         catch (IOException e)
         {
             Log.e("tag", e.getMessage());
         }
         for(String filename : files)
         {
         	// consider only *.json files
         	if (filename.endsWith(".json"))
         	{
 	            InputStream in = null;
 	            OutputStream out = null;
 	            try
 	            {
 	              in = assetManager.open(filename);
 	              String strOutFile = DataStorage.getExternalStoragePath(m_ctx) + "/" + filename;
 	              out = new FileOutputStream(strOutFile);
 	              copyFile(in, out);
 	              in.close();
 	              in = null;
 	              out.flush();
 	              out.close();
 	              out = null;
 	            }
 	            catch(Exception e)
 	            {
	                Log.e("tag", e.getMessage());
 	            }
         	}
         }
     }
     
     /**
      * Write a single file
      * @param in the source (in assets)
      * @param out the target
      * @throws IOException
      */
     private void copyFile(InputStream in, OutputStream out) throws IOException
     {
         byte[] buffer = new byte[1024];
         int read;
         while((read = in.read(buffer)) != -1)
         {
           out.write(buffer, 0, read);
         }
     }
     
     private File getFileDir(Context ctx)
     {
     	File path = null;
     	try
     	{
     		path = m_ctx.getExternalFilesDir(null);
     	}
     	catch (NoSuchMethodError e)
     	{
     		// on Android 2.1 this method does not exist: alternate method
     		String packageName = ctx.getPackageName();
     		File externalPath = Environment.getExternalStorageDirectory();
     		path = new File(externalPath.getAbsolutePath() +
     		                         "/Android/data/" + packageName + "/files");
     	}
     	
     	return path;
     }
 }
