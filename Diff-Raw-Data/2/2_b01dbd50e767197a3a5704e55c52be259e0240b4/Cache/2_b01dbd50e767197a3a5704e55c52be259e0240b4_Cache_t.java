 package com.quanturium.androcloud2.tools;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 
 public class Cache
 {
 
 	public static final int	CACHE_TIME_BITMAP	= 5040; // in hours (1 month)
 	public static final int	CACHE_TIME_STRING	= 2;	// in hours (2 hours)
 
 	public static void setCachedBitmap(Context context, String fileName, Bitmap bitmap)
 	{
 		if (isStorageWritable())
 		{
 			File file = new File(context.getExternalCacheDir(), fileName);
 
 			try
 			{
 				file.createNewFile();
 
 				FileOutputStream output = new FileOutputStream(file);
 
				bitmap.compress(CompressFormat.PNG, 100, output);
 				output.flush();
 
 				output.close();
 
 			} catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static Bitmap getCachedBitmap(Context context, String fileName, int cacheDurability)
 	{
 		if (isStorageAvailable())
 		{
 			Bitmap bitmap = null;
 			File file = new File(context.getExternalCacheDir(), fileName);
 
 			if (file.exists())
 			{
 				if ((long) (file.lastModified() + (long) (cacheDurability * 3600 * 1000)) < System.currentTimeMillis())
 					return null;
 
 				try
 				{
 
 					FileInputStream input = new FileInputStream(file);
 					bitmap = BitmapFactory.decodeStream(input);
 
 					input.close();
 					input = null;
 
 				} catch (FileNotFoundException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				return bitmap;
 			}
 			else
 			{
 				return null;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	public static boolean remove(Context context, String fileName)
 	{
 		if (isStorageWritable())
 		{
 			File dir = context.getExternalCacheDir();
 
 			if (dir.isDirectory())
 			{
 				File file = new File(context.getExternalCacheDir(), fileName);
 
 				if (file.exists())
 					file.delete();
 
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public static boolean removeAll(Context context)
 	{
 		if (isStorageWritable())
 		{
 			File dir = context.getExternalCacheDir();
 
 			if (dir.isDirectory())
 			{
 				String[] files = dir.list();
 				int i;
 				for (i = 0; i < files.length; i++)
 				{
 					File file = new File(context.getExternalCacheDir(), files[i]);
 
 					if (file.exists())
 						file.delete();
 				}
 
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public static boolean isStorageAvailable()
 	{
 		boolean mExternalStorageAvailable = false;
 
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state))
 			mExternalStorageAvailable = true;
 		else
 			if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
 				mExternalStorageAvailable = true;
 
 			else
 				mExternalStorageAvailable = false;
 
 		return mExternalStorageAvailable;
 	}
 
 	public static boolean isStorageWritable()
 	{
 		boolean mExternalStorageWriteable = false;
 
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state))
 			mExternalStorageWriteable = true;
 		else
 			if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
 				mExternalStorageWriteable = false;
 			else
 				mExternalStorageWriteable = false;
 
 		return mExternalStorageWriteable;
 	}
 }
