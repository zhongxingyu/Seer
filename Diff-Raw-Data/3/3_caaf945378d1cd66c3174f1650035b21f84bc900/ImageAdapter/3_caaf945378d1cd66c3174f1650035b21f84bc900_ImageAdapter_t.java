 package com.project.eden;
 
 import java.io.InputStream;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 public class ImageAdapter extends BaseAdapter {
     private Context mContext;
     String urls[];
     
     ImageAdapter(Context c, String[] urls) {
     	this.urls = urls;
     	mContext = c;
     }
     ImageAdapter(Context c) {
         mContext = c;
     }
     
     public int getCount() {
         return urls.length;
     }
 
     public Object getItem(int position) {
         return null;
     }
 
     public long getItemId(int position) {
         return 0;
     }
 
     // create a new ImageView for each item referenced by the Adapter
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ImageView imageView;
         if (convertView == null) {  // if it's not recycled, initialize some attributes
             imageView = new ImageView(mContext);
             imageView.setLayoutParams(new GridView.LayoutParams(170, 165));
             imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
             imageView.setPadding(3, 3, 3, 3);
         } else {
             imageView = (ImageView) convertView;
         }
         ((EdenMain) mContext).progressBarInvisible(true);
         new DownloadImageTask(imageView).execute(urls[position]);
         return imageView;
     }
     
     private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
         ImageView bmImage;
               
         public DownloadImageTask(ImageView bmImage) {
             this.bmImage = bmImage;
         }
         
         protected Bitmap doInBackground(String... urls) {
         	 
             String urldisplay = urls[0];
             Bitmap mIcon11 = null;
             try {
                 InputStream in = new java.net.URL(urldisplay).openStream();
                 mIcon11 = BitmapFactory.decodeStream(in);
             } catch (Exception e) {
                 Log.e("Error", e.getMessage());
                 e.printStackTrace();
             }
             return mIcon11;
         }
 
         protected void onPostExecute(Bitmap result) {
             bmImage.setImageBitmap(result);
             ((EdenMain) mContext).progressBarInvisible(false);
         }
     }
 }
