 package com.joy.launcher2.cache;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.joy.launcher2.R;
 import com.joy.launcher2.wallpaper.WallpaperInfo;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.util.Log;
 
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
     private ArrayList<WallpaperInfo> mImages = new ArrayList<WallpaperInfo>();
 	
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
 		return getJsonFromFile(fileName, getDirectoryFile(isNative));
 	}
 	
 	public JSONObject getJsonFromFile(String fileName ,File dirFile)
 	{
 		InputStream fis = null;
 		JSONObject json = null;
 		//File dirFile = getDirectoryFile(isNative);
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
 		
 		
 		return json;
 	}
 	
 	public void loadNativeWallpaper(JSONArray items)
 	{
 		findWallpapers();
 		if(items != null)
 		{
 			int count = mImages.size();
 			for(int i = 0; i < count; i++)
 			{
 				try
 				{
 					WallpaperInfo wi = mImages.get(i);
 					JSONObject item = new JSONObject();
 					item.put("id", wi.id);
 					item.put("size",wi.size);
 					item.put("name", wi.wallpaperName);
 					item.put("icon", wi.urls[0]);
 					item.put("preview", wi.url);
 					item.put("url", wi.urls[1]);
 					items.put(item);
 				}
 				catch(Exception e)
 				{
 					
 				}
 			}
 		}
 	}
 	
 	private void findWallpapers() {
 		mImages.clear();
 		final Resources resources = context.getResources();
 		final String packageName;
         packageName = resources.getResourcePackageName(R.array.wallpapers);
         int size = addWallpapers(resources, packageName, R.array.wallpapers);
         if(size <= 0)
         {
         	addWallpapers(resources, packageName, R.array.extra_wallpapers);
         }
 	}
 	
 	private int addWallpapers(Resources resources, String packageName, int list) {
         final String[] extras = resources.getStringArray(list);
         int size = 0;
         for (String extra : extras) {
         	String imageName = extra;
         	String thumbName = extra + "_small"; 
         	String previewbName = extra + "_preview"; 
             int res = resources.getIdentifier(extra, "drawable", packageName);
             if (res != 0) {
                 final int thumbRes = resources.getIdentifier(thumbName,
                         "drawable", packageName);
                 final int previewRes = resources.getIdentifier(previewbName,
                         "drawable", packageName);
 
                 if (thumbRes != 0 && previewRes != 0) {
                 	size ++;
                 	WallpaperInfo wi = new WallpaperInfo();
                 	wi.id = Integer.MIN_VALUE;
                 	wi.size = 0;
                 	wi.wallpaperName = imageName;
                 	wi.url = Integer.toString(previewRes);
                 	wi.urls[0] = Integer.toString(thumbRes);
                 	wi.urls[1] = Integer.toString(res);
                     mImages.add(wi);
                 }
             }
         }
         return size;
     }
 	
 	public void saveJsonToFile(JSONObject json, String fileName, boolean isNative)
 	{
 		saveJsonToFile(json, fileName, getDirectoryFile(isNative));
 	}
 	
 	public void saveJsonToFile(JSONObject json, String fileName, File dirFile)
 	{
 		
 		byte[] bytes = json.toString().getBytes();
 		BufferedOutputStream bos = null;
 		FileOutputStream fos = null;
 		//File dirFile = getDirectoryFile(isNative);
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
