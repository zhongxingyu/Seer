 package com.joy.launcher2.cache;
 
 import static android.os.Environment.MEDIA_MOUNTED;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 
 /**
  * 缓存文件：各种文件读取操作
  * @author huangming
  *
  */
 public class UnLimitedImageFileCache 
 {
 	private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
 	
 	private final static String ONLINE_FILE = "online";
 	private final static String ONLINE_THUMBNAIL_FILE = "online_thumbnail";	
 	private final static String NATIVE_FILE = "native";
 	private final static String NATIVE_THUMBNAIL_FILE = "native_thumbnail";
 	private final static String RECOMMEND_FILE = "recommend";
 	private final static String NATIVE_ORIGIN_FILE = "native_origin";
 	
 	private final static String DIRCACHE = "joy_wallpaper_cache";
 	
 	private Context context;
 	
 	public UnLimitedImageFileCache(Context context)
 	{
 		this.context = context;
 	}
 	
 	private File getCacheDirectory()
 	{
 		File appCacheDir = null;
 		if (Environment.getExternalStorageState().equals(MEDIA_MOUNTED) && hasExternalStoragePermission(context)) {
 			appCacheDir = getExternalCacheDir();
 		}
 		return appCacheDir;
 	}
 	
	private static File getExternalCacheDir() {
 		File appCacheDir = new File(Environment.getExternalStorageDirectory(), DIRCACHE);
 		if (!appCacheDir.exists()) {
 			if (!appCacheDir.mkdirs()) {				
 				return null;
 			}
 		}
 		return appCacheDir;
 	}
 	
 	private static boolean hasExternalStoragePermission(Context context) {
 		int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
 		return perm == PackageManager.PERMISSION_GRANTED;
 	}
 	
 	public File getOrMakeFileDir(String fileName)
 	{
 		if(fileName == null)return null;
 		File dirFile = getCacheDirectory();
 		if(dirFile != null)
 		{
 			File file = new File(dirFile, fileName);
 			if(!file.exists())
 			{
 				if(file.mkdirs())
 				{
 					return file;
 				}
 			}
 			else
 			{
 				return file;
 			}
 		}
 		return null;
 	}
 	
 	public File getImageFileDir(int type)
 	{
 		File file = null;
 		switch(type) 
 		{
 		 case 1:
 			file = getNativeFile();
 			break;
          case 2:
         	file = getNativeThumFile();
 			break;
          case 3:
  			file = getOnlineFile();
  			break;
           case 4:
          	file = getOnlineThumFile();
  			break;
           case 5:
   			file = getRecommendFile();
   			break;
 
 		default:
 			break;
 		}
 		return file;
 	}
 	
 	private String convertUrlToFileName(String url) {
 		return url;
     }
 	
 	public boolean isImageOnDiscCache(String url)
 	{
 		return isImageOnDiscCache(url, NATIVE_ORIGIN_FILE);
 	}
 	
 	public boolean isImageOnDiscCache(String url, String dirFileName)
 	{
 		String fileName = convertUrlToFileName(url);
 		File imageFileDir = getOrMakeFileDir(dirFileName);
 		File imageFile = new File(imageFileDir, fileName);
 		return imageFile.exists();
 	}
 	
 	public File getNativeThumFile()
 	{
 		return getOrMakeFileDir(NATIVE_THUMBNAIL_FILE);
 	}
 	
 	public File getNativeFile()
 	{
 		return getOrMakeFileDir(NATIVE_FILE);
 	}
 	
 	public File getRecommendFile()
 	{
 		return getOrMakeFileDir(RECOMMEND_FILE);
 	}
 	
 	public File getOnlineFile()
 	{
 		return getOrMakeFileDir(ONLINE_FILE);
 	}
 	
 	public File getOnlineThumFile()
 	{
 		return getOrMakeFileDir(ONLINE_THUMBNAIL_FILE);
 	}
 	
 	public boolean saveBitmapToFile(Bitmap bm, File dirFile, String url)
 	{
 		boolean saveSuccessful = false;
 		String fileName = convertUrlToFileName(url);
 		if(bm != null && dirFile != null && fileName != null)
 		{
 			File imageFile = new File(dirFile, fileName);
 			try 
 			{
 				imageFile.createNewFile();
 				OutputStream outStream = new FileOutputStream(imageFile);
 	            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
 	            outStream.flush();
 	            outStream.close();
 	            saveSuccessful = true;
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		return saveSuccessful;
 	}
 	
 	public Bitmap getBitmapFromFileCache(File dirFile, String url)
 	{
 		String fileName = convertUrlToFileName(url);
 		Bitmap bm = null;
 		if(dirFile != null && fileName != null)
 		{
 			File imageFile = new File(dirFile, fileName);
 			if(imageFile.exists())
 			{
 				String path =  imageFile.getPath();		
 	    		bm = BitmapFactory.decodeFile(path);
 	    		if(bm == null)
 	    		{
 	    			imageFile.delete();
 	    		}
 	    		else
 	    		{
 	    			long newModifiedTime = System.currentTimeMillis();
 	    			imageFile.setLastModified(newModifiedTime);
 	    		}
 			}
 
 		}
 		return bm;
 	}
 	
 	public InputStream getInputStreamFromFileCache(String fileName)
 	{
 		InputStream is = null;
 		File dirFile = getOrMakeFileDir(NATIVE_ORIGIN_FILE);
 		File imageFile = new File(dirFile, fileName);
 		if(imageFile.exists() && imageFile.isFile())
 		{
 			try {
 				is = new FileInputStream(imageFile);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return is;
 	}
 	
 	public boolean saveInputStreamToFile(InputStream is, String fileName)
 	{
 		boolean successful = false;
 		FileOutputStream fos = null;
 		File dirFile = getOrMakeFileDir(NATIVE_ORIGIN_FILE);
 		if(dirFile != null && dirFile.exists())
 		{
 			File imageFile = new File(dirFile, fileName);
 	        try {
 	        	imageFile.createNewFile();
 				fos = new FileOutputStream(imageFile);
 				byte[] buffer=new byte[1024];
 				int count=0;
 				while ((count=is.read(buffer))>=0) {
 						fos.write(buffer,0,count);
 				}
 				successful = true;
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				
 					try {
 						if(is != null)is.close();
 						if(fos != null)fos.close();
 						if(!successful && imageFile.exists())
 						{
 							imageFile.delete();
 						}
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			}
 	       
 		}
 		return successful;
 	}
 	
 	public void copyFileFormOnlineToNative(String fileName, boolean isThum)
 	{
 		if(fileName == null)return;
 		File srcDir = getOrMakeFileDir(isThum?ONLINE_THUMBNAIL_FILE:ONLINE_FILE);
 		File dstDir = getOrMakeFileDir(isThum?NATIVE_THUMBNAIL_FILE:NATIVE_FILE);
 		File srcImageFile = new File(srcDir, fileName);
 		File dstImageFile = new File(dstDir, fileName);
 		BufferedInputStream inBuff = null;
 	    BufferedOutputStream outBuff = null;
 	    try {
 			dstImageFile.createNewFile();
 			inBuff = new BufferedInputStream(new FileInputStream(srcImageFile));
 			outBuff = new BufferedOutputStream(new FileOutputStream(dstImageFile));
 			 byte[] b = new byte[1024 * 5];
 	         int len;
 	         while ((len = inBuff.read(b)) != -1) {
 	                outBuff.write(b, 0, len);
 	         }
 	            // 刷新此缓冲的输出流
 	            outBuff.flush();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			 
 				try {
 					if (inBuff != null)inBuff.close();
 					if (outBuff != null)outBuff.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}	            
 		}
 	}
 
 }
