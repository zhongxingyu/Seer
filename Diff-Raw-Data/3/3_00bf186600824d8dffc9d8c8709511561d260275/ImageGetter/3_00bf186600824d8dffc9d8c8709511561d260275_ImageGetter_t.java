 package com.gnuton.newshub.tasks;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 import com.gnuton.newshub.adapters.ImageAdapter;
 import com.gnuton.newshub.utils.DiskLruImageCache;
 import com.gnuton.newshub.utils.MyApp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class ImageGetter implements Html.ImageGetter {
     private final View mContainer;
     private View mPageView;
     private ImageAdapter mImageAdapter;
     static private DiskLruImageCache mCache;
 
     public ImageGetter(View t) {
         this.mCache = MyApp.getImageCache();
         this.mContainer = t;
     }
 
     public void setAdapter(ImageAdapter adapter, View pageView) {
         mImageAdapter = adapter;
         mPageView = pageView;
     }
 
     public Drawable getDrawable(String source) {
         URLDrawable urlDrawable = new URLDrawable();
 
         // get the actual source
         ImageGetterAsyncTask asyncTask =
                 new ImageGetterAsyncTask( urlDrawable);
 
         asyncTask.execute(source);
 
         // return reference to URLDrawable where I will change with actual image from
         // the src tag
         return urlDrawable;
     }
 
     private class URLDrawable extends BitmapDrawable {
         // the drawable that you need to set, you could set the initial drawing
         // with the loading image if you need to
         protected Drawable drawable;
 
         @Override
         public void draw(Canvas canvas) {
             // override the draw to facilitate refresh function later
             if(drawable != null) {
                 drawable.draw(canvas);
             }
         }
     }
 
     public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
         URLDrawable urlDrawable;
 
         public ImageGetterAsyncTask(URLDrawable d) {
             this.urlDrawable = d;
         }
 
         @Override
         protected Drawable doInBackground(String... params) {
             String source = params[0];
             return fetchDrawable(source);
         }
 
         @Override
         protected void onPostExecute(Drawable result) {
            if (result == null)
                return;

             int intHeight = result.getIntrinsicHeight();
             int intWidth = result.getIntrinsicWidth();
 
             if (intHeight >= mPageView.getHeight()/4 && mImageAdapter != null){
                     mImageAdapter.mImages.add(result);
                     mImageAdapter.notifyDataSetChanged();
                     return;
             }
 
             // set the correct bound according to the result from HTTP call
             urlDrawable.setBounds(0, 0, intWidth, intHeight);
 
             // change the reference of the current drawable to the result
             // from the HTTP call
             urlDrawable.drawable = result;
 
             // redraw the image by invalidating the container
             ImageGetter.this.mContainer.invalidate();
             TextView tv = (TextView) ImageGetter.this.mContainer;
             /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                 tv.setHeight((tv.getHeight() + result.getMinimumHeight()));
             } else {
                 tv.setEllipsize(null);
             }*/
         }
 
         //FIXME Using drawable really sucks!
         public Drawable fetchDrawable(String urlString) {
             try {
                 String key = String.valueOf(urlString.hashCode());
                 Drawable drawable;
                 Bitmap bitmap;
                 Context c = MyApp.getContext();
                 if (mCache.containsKey(key)) {
                     bitmap = mCache.getBitmap(key);
 
                     drawable = new BitmapDrawable(c.getResources(),bitmap);
                     drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
                             + drawable.getIntrinsicHeight());
                 } else {
                     // Download
                     InputStream is = fetch(urlString);
                     bitmap = BitmapFactory.decodeStream(is);
                     drawable = new BitmapDrawable(c.getResources(),bitmap);
                     //drawable = Drawable.createFromStream(is, "src");
                     drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
                             + drawable.getIntrinsicHeight());
 
                     if (bitmap != null)
                         mCache.put(key, bitmap);
                 }
 
                 return drawable;
             } catch (Exception e) {
                 return null;
             }
         }
 
         // Fetch images from phone cache or web
         private InputStream fetch(String urlString) throws IOException {
             URL url = new URL(urlString);
             URLConnection connection = url.openConnection();
             connection.setUseCaches(true);
             return connection.getInputStream();
         }
     }
 
 }
