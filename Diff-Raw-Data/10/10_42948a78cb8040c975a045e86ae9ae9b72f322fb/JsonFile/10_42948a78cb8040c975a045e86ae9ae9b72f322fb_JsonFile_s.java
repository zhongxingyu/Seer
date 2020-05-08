 package com.joy.launcher2.cache;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 /**
  * json对象和文件相互操作
  * @author huangming
  *
  */
 public class JsonFile 
 {
 	
 	private final static String NATIVE_WALLPAPER_FILE = "native_wallpaper";
 	private final static String ONLINE_WALLPAPER_FILE = "online_wallpaper";
 	
 	Context context;
 	
 	public JsonFile(Context context)
 	{
 		this.context = context;
 	}
 	
 	public File getDirectoryFile(boolean isNative)
 	{
 		String dirName = isNative?NATIVE_WALLPAPER_FILE:NATIVE_WALLPAPER_FILE;
 		return getDirectoryFile(dirName);
 	}
 	
 	private File getDirectoryFile(String dirName)
 	{
		File dirCache = context.getCacheDir();
 		if(dirCache != null && dirCache.exists())
 		{
 			File dirFile = new File(dirCache, dirName);
 			if(!dirFile.exists())
 			{
 				if(!dirFile.mkdirs())
 				{
 					return null;
 				}
 				else
 				{
 					return dirFile;
 				}
 			}
 			else
 			{
 				return dirFile;
 			}
 		}
 		return null;
 	}
 	
 	public JSONObject getJsonFromFile(String fileName ,boolean isNative)
 	{
 		InputStream fis = null;
 		JSONObject json = null;
 		File dirFile = getDirectoryFile(isNative);
 		File jsonFile = new File(dirFile, fileName);
 		if(jsonFile.exists() && jsonFile.isFile())
 		{
 			try {
 				fis = new FileInputStream(jsonFile);
 				StringBuffer out = new StringBuffer();   
 		        byte[]  b = new byte[4096];   
 		        int n;  
 		        while ((n = fis.read(b))!= -1){   
 		            out.append(new String(b,0,n));   
 		        }  
 		        json =  new JSONObject(out.toString());
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally{
 				if(fis != null)
 					try {
 						fis.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			}
 		}
 		
 		
 		if(json == null)
 		{
 			json = new JSONObject();
 		}
 		return json;
 	}
 	
 	public void saveJsonToFile(JSONObject json, String fileName, boolean isNative)
 	{
 		
 		byte[] bytes = json.toString().getBytes();
 		BufferedOutputStream bos = null;
 		FileOutputStream fos = null;
 		File dirFile = getDirectoryFile(isNative);
 		File jsonFile = new File(dirFile, fileName);
 		try {
 			jsonFile.createNewFile();
 			fos = new FileOutputStream(jsonFile);
 			bos = new BufferedOutputStream(fos);
 			bos.write(bytes);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		finally
 		{
 
 				try {
 					
 					if(bos != null)bos.close();
 					if(fos != null)fos.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			
 		}
 		
 	}
 
 }
