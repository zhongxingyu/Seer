 package ru.tulupov.nsuconnect.images;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.android.volley.toolbox.ImageLoader.ImageCache;
 import com.jakewharton.disklrucache.DiskLruCache;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import ru.tulupov.nsuconnect.BuildConfig;
 
 /**
  * Implementation of DiskLruCache by Jake Wharton
  * modified from http://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
  */
 public class DiskMemoryLruImageCache extends DiskLruImageCache {
 
 
     public DiskMemoryLruImageCache(Context context, String uniqueName, int diskCacheSize, int memCacheSize, CompressFormat compressFormat, int quality) {
         super(context, uniqueName, diskCacheSize, compressFormat, quality);
         bitmapLruImageCache = new BitmapLruImageCache(memCacheSize);
     }
 
     private BitmapLruImageCache bitmapLruImageCache;
 
     @Override
     public Bitmap getBitmap(String key) {
         Bitmap bitmap = bitmapLruImageCache.getBitmap(key);
         if (bitmap != null) {
 
 
             return bitmap;
         }
         bitmap = super.getBitmap(key);
        if (bitmap != null) {
            bitmapLruImageCache.putBitmap(key, bitmap);
        }
         return bitmap;
     }
 
     @Override
     public void putBitmap(String key, Bitmap data) {
 
         super.putBitmap(key, data);
     }
 }
