 
 package com.polysfactory.coursera.api;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.WeakReference;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 import com.polysfactory.coursera.util.ImageCache;
 
 /**
  * This class has to be instantiated on UI thread.
  * 
  * @author horikawa
  */
 public class DownloadImagesTask {
 
     private static final String TAG = "DownloadImageTask";
 
     private final LoadInfo loadInfo;
 
     private final int mImageHeight;
 
     private final int mImageWidth;
 
     // private ExecutorService executer = Executors.newCachedThreadPool();
 
     private final ExecutorService executer = Executors.newFixedThreadPool(3);
 
     /**
      * main thread handler
      */
     final Handler mHandler = new Handler();
 
     public DownloadImagesTask(Context context, ImageView imageView, ProgressBar loadingView) {
         this.loadInfo = new LoadInfo(imageView, loadingView);
         this.mImageHeight = imageView.getHeight();
         this.mImageWidth = imageView.getWidth();
     }
 
     public void load(final String url) {
 
         onPreExecute();
 
         // queue command
         Loader command = new Loader(loadInfo, url, mHandler);
         executer.submit(command);
     }
 
     private void onPreExecute() {
         ImageView imageView = loadInfo.imageViewRef.get();
         ProgressBar progressBar = loadInfo.loadingViewRef.get();
         if (imageView != null) {
             // clear image
             imageView.setImageResource(android.R.color.transparent);
             if (progressBar != null) {
                 progressBar.setVisibility(View.VISIBLE);
             }
         }
     }
 
     static class LoadInfo {
 
         final WeakReference<ImageView> imageViewRef;
 
         final WeakReference<ProgressBar> loadingViewRef;
 
         public LoadInfo(ImageView imageView, ProgressBar loadingView) {
             this.imageViewRef = new WeakReference<ImageView>(imageView);
             this.loadingViewRef = new WeakReference<ProgressBar>(loadingView);
         }
 
     }
 
     static class Loader implements Runnable {
 
         private final LoadInfo mLoadInfo;
 
         private final String mUrl;
 
         private final Handler mHandler;
 
         Loader(LoadInfo loadInfo, String url, Handler handler) {
             this.mLoadInfo = loadInfo;
             this.mUrl = url;
             this.mHandler = handler;
         }
 
         @Override
         public void run() {
             Displayer result = null;
             Bitmap bitmap = ImageCache.get(mUrl);
             if (bitmap != null) {
                 Log.v(TAG, "cache hits!: " + mUrl);
             } else {
                 bitmap = downloadImage(mUrl, 2 /* TODO */);
             }
 
             // display on UI thread
             result = new Displayer(mLoadInfo, bitmap, mUrl);
             mHandler.post(result);
         }
 
         private Bitmap downloadImage(String url, int scale) {
             Bitmap bitmap = null;
             BitmapFactory.Options option = new BitmapFactory.Options();
             try {
                 option.inSampleSize = scale;
                 bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null,
                         option);
                 ImageCache.put(url, bitmap);
             } catch (MalformedURLException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             return bitmap;
         }
 
     }
 
     static class Displayer implements Runnable {
 
         private final LoadInfo mLoadInfo;
 
         private final Bitmap mBitmap;
 
         private final String mUrl;
 
         Displayer(LoadInfo loadInfo, Bitmap bitmap, String url) {
             this.mLoadInfo = loadInfo;
             this.mBitmap = bitmap;
             this.mUrl = url;
         }
 
         @Override
         public void run() {
             ImageView imageView = mLoadInfo.imageViewRef.get();
             ProgressBar progressBar = mLoadInfo.loadingViewRef.get();
             if (imageView != null) {
                 if (!((String) imageView.getTag()).equals(this.mUrl)) {
                     Log.w(TAG, "outdated image load:" + this.mUrl);
                    return;
                 }
                 imageView.setImageBitmap(this.mBitmap);
                 if (progressBar != null) {
                     progressBar.setVisibility(View.INVISIBLE);
                 }
             }
         }
     }
 }
