 package com.tools.tvguide.managers;
 
 import java.io.IOException;
 
 import com.jakewharton.disklrucache.DiskLruCache;
 import com.tools.tvguide.components.Shutter;
 
 import android.content.Context;
 
 public class DiskCacheManager implements Shutter 
 {
     private static final int DISK_CACHE_VERSION = 1;
     private static final int DISK_CACHE_MAX_SIZE = 20 * 1024 * 1024;    // 20MB
     private Context mContext;
     private DiskLruCache mDiskLruCache;
     
     public DiskCacheManager(Context context)
     {
         assert (context != null);
         mContext = context;
         
         try 
         {
             mDiskLruCache = DiskLruCache.open(mContext.getCacheDir(), DISK_CACHE_VERSION, 1, DISK_CACHE_MAX_SIZE);
         } 
         catch (IOException e) 
         {
             e.printStackTrace();
         }
     }
     
     public void clearAll()
     {
         if (mDiskLruCache == null)
             return;
         
         try {
             mDiskLruCache.delete();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public synchronized String getString(String key)
     {
         if (mDiskLruCache == null)
             return null;
         
         String result = null;
         try 
         {
             DiskLruCache.Editor editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
             if (editor != null)
             {
                 result = editor.getString(0);
                 editor.abort();
             }
         } 
         catch (IOException e) 
         {
             e.printStackTrace();
         }
         
         return result;
     }
     
     public synchronized boolean setString(String key, String value)
     {
         if (mDiskLruCache == null || key == null || value == null)
             return false;
         
         boolean result = false;
         try 
         {
             DiskLruCache.Editor editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
             if (editor != null)
             {
                 editor.set(0, value);
                 editor.commit();
                 result = true;
             }
         } 
         catch (IOException e) 
         {
             e.printStackTrace();
         }
         
         return result;
     }
 
     @Override
     public void onShutDown() 
     {
         if (mDiskLruCache != null)
         {
             try {
                 mDiskLruCache.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
