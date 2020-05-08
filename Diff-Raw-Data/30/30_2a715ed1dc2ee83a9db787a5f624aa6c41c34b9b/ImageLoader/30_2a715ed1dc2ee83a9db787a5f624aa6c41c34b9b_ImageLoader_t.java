 package com.tabbie.android.radar.core.cache;
 
 /**
  *  FileCache.java
  *
  *  Created on: August 9, 2012
  *      Author: Fedor Vlasov, edited Justin Knutson
  *      Source: https://github.com/thest1/LazyList/blob/master/src/com/fedorvlasov/lazylist/FileCache.java
  *      
  *  Loader structure that manages the creation of Bitmaps from a memory cache, a file stream, or a URL connection
  */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.tabbie.android.radar.R;
 
 public class ImageLoader {
     
     MemoryCache memoryCache = new MemoryCache();
     FileCache fileCache;
     private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
     ExecutorService executorService; 
     
     public ImageLoader(Context context) {
         fileCache = new FileCache(context);
         executorService = Executors.newFixedThreadPool(5);
     }
     
     /**
      * Retrieve an image from the memory cache or start the
      * thread that will build it
      * 
      * @param url - The URL location of the image (necessary to retrieve the file as well)
      * @param imageView - The view to be populated
      */
     public void displayImage(String url, ImageView imageView) {
         imageViews.put(imageView, url);
         Bitmap bitmap = memoryCache.get(url); // Attempt to retrieve a Bitmap from the cache
         if(bitmap!=null) {
        	((View) imageView.getParent()).setVisibility(View.VISIBLE);
        	((View) imageView.getParent().getParent()).findViewById(R.id.element_loader).setVisibility(View.GONE);
             imageView.setImageBitmap(bitmap);
         } else {
         	Log.d("ImageLoader", "Now in the Queue");
             queuePhoto(url, imageView); // Start loading the image, we'll display the default for now
        	((View) imageView.getParent()).setVisibility(View.GONE);
        	((View) imageView.getParent().getParent()).findViewById(R.id.element_loader).setVisibility(View.VISIBLE);
         }
     }
         
     /**
      * Private method to begin a thread that will load the requested image
      * 
      * @param url - The URL of the image to load
      * @param imageView - The View to be populated
      */
     private void queuePhoto(String url, ImageView imageView) {
         PhotoToLoad p = new PhotoToLoad(url, imageView);
         executorService.submit(new PhotosLoader(p));
     }
     
     /**
      * Helper method to restore a Bitmap from a file after trying the cache.
      * Note that this method will attempt to recover the file from the
      * internet if no local file is found
      * @param url - The URL identifier of the file
      * @return A Bitmap or null object if no Bitmap can be found
      */
     protected Bitmap getBitmap(String url) 
     {
     	Log.d("ImageLoader", "Getting Bitmap");
         File f = fileCache.getFile(url);
         
         //from SD cache
         Bitmap b = decodeFile(f);
         if(b!=null) {
         	Log.i("ImageLoader", "Found Bitmap as a file");
             return b;
         }
         
         //from web
         try {
         	Log.d("ImageLoader", "Retrieving Bitmap from web");
             Bitmap bitmap=null;
             URL imageUrl = new URL(url); // TODO When passing URL instead of String, this will be a simpler operation
             HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
             conn.setConnectTimeout(30000);
             conn.setReadTimeout(30000);
             conn.setInstanceFollowRedirects(true);
             InputStream is = conn.getInputStream();
             OutputStream os = new FileOutputStream(f);
             CopyStream(is, os);
             os.close();
             bitmap = decodeFile(f);
             return bitmap;
         } catch (Exception ex) {
            ex.printStackTrace();
            return null;
         }
     }
 
     /** 
      * Decodes image and scales it to reduce memory consumption
      * @param f - The file to decode
      * @return The Bitmap associated with this file
      */
     private Bitmap decodeFile(File f) {
         try {
             //decode image size
             BitmapFactory.Options o = new BitmapFactory.Options();
             o.inJustDecodeBounds = true;
             BitmapFactory.decodeStream(new FileInputStream(f),null,o);
             
             //Find the correct scale value. It should be the power of 2.
             final int REQUIRED_SIZE=70;
             int width_tmp=o.outWidth, height_tmp=o.outHeight;
             int scale=1;
             while(true) {
                 if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                     break;
                 width_tmp/=2;
                 height_tmp/=2;
                 scale*=2;
             }
             
             //decode with inSampleSize
             BitmapFactory.Options o2 = new BitmapFactory.Options();
             o2.inSampleSize = scale;
             return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
         } catch (FileNotFoundException e) {
         	return null;
         }
     }
     
     // Task for the queue
     private class PhotoToLoad {
         public String url;
         public ImageView imageView;
         public PhotoToLoad(String u, ImageView i) {
             url=u; 
             imageView=i;
         }
     }
     
     class PhotosLoader implements Runnable {
         PhotoToLoad photoToLoad;
         PhotosLoader(PhotoToLoad photoToLoad){
             this.photoToLoad=photoToLoad;
             // Yo dawg, I heard you liked PhotoToLoads
         }
         
         @Override
         public void run() {
         	Log.d("ImageLoader.PhotosLoader", "Running");
             if(imageViewReused(photoToLoad))
                 return;
             Bitmap bmp = getBitmap(photoToLoad.url);
             memoryCache.put(photoToLoad.url, bmp);
             if(imageViewReused(photoToLoad))
                 return;
             BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
             Activity a = (Activity) photoToLoad.imageView.getContext();
             a.runOnUiThread(bd);
         }
     }
     
     boolean imageViewReused(PhotoToLoad photoToLoad){
         String tag=imageViews.get(photoToLoad.imageView);
         if(tag==null || !tag.equals(photoToLoad.url))
             return true;
         return false;
     }
     
     /**
      * Used to display bitmap in the UI thread
      * @author Probably not Fedor Vlasov
      *
      */
     class BitmapDisplayer implements Runnable {
         Bitmap bitmap;
         PhotoToLoad photoToLoad;
         
         public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
         	bitmap=b;
         	photoToLoad=p;
         }
         
         public void run() {
         	Log.d("ImageLoader.BitmapDisplayer", "Running");
             if(imageViewReused(photoToLoad))
                 return;
             if(bitmap!=null) {
            	((View) photoToLoad.imageView.getParent()).setVisibility(View.VISIBLE);
            	((View) photoToLoad.imageView.getParent().getParent()).findViewById(R.id.element_loader).setVisibility(View.GONE);
                 photoToLoad.imageView.setImageBitmap(bitmap);
             } else {
            	((View) photoToLoad.imageView.getParent()).setVisibility(View.GONE);
            	((View) photoToLoad.imageView.getParent().getParent()).findViewById(R.id.element_loader).setVisibility(View.VISIBLE);
             }
         }
     }
 
     /**
      * While MemoryCache and FileCache both have clear methods,
      * this convenience method will happily take care of both
      */
     public void clearCache() {
         memoryCache.clear();
         fileCache.clear();
     }
 
     /**
      * This was originally part of a class called Utils
      * and has been consolidated here as a static method
      * @param is - InputStream, you fucktard
      * @param os - OutputStream, you dickshit
      */
     public static void CopyStream(InputStream is, OutputStream os) {
         final int buffer_size = 1024;
         try {
             byte[] bytes=new byte[buffer_size];
             for(;;)
             {
               int count=is.read(bytes, 0, buffer_size);
               if(count==-1)
                   break;
               os.write(bytes, 0, count);
             }
         }
         catch(Exception ex) {
         	throw new RuntimeException(); // Why the fuck not?
         }
     }
 }
