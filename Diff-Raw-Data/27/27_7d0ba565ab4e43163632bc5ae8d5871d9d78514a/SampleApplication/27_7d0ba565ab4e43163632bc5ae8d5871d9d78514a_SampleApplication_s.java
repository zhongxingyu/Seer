 package com.marczych.androidimagemanager.sample;
 
 import android.app.Application;
 
 import android.graphics.Bitmap;
 import android.widget.ImageView;
 
 import com.marczych.androidimagemanager.ImageManager;
 
 public class SampleApplication extends Application {
    private ImageManager mImageManager;
 
    public ImageManager getImageManager() {
       if (mImageManager == null) {
          mImageManager = new ImageManager(this);
 
          mImageManager.setController(new ImageManager.Controller() {
             public boolean overrideDisplay(String url, ImageView imageView) {
                if (url.contains("0")) {
                   imageView.setImageResource(R.drawable.dog);
                   return true;
                }
 
                return false;
             }
 
             public void loading(ImageView imageView) {
                imageView.setImageResource(R.drawable.loading);
             }
 
             public boolean displayImage(ImageView imageView, Bitmap bitmap,
              String url) {
                if (url.contains("3")) {
                   return true;
                }
 
                return false;
             }
 
             public void fail(ImageView imageView) {
                imageView.setImageResource(R.drawable.oops);
             }
          });
       }
 
       return mImageManager;
    }
 }
