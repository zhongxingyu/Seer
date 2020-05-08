 package com.janrain.android.quickshare;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ScaleDrawable;
 import android.os.AsyncTask;
 import android.util.Config;
 import android.util.Log;
 import android.view.Gravity;
 import com.janrain.android.engage.session.JRSessionData;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lillialexis
  * Date: 4/22/11
  * Time: 1:12 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Story implements Serializable {
     private static final String TAG = Story.class.getSimpleName();
 
     private String mTitle;
     private String mDate;
     private String mDescription;
     private String mPlainText;
     private String mLink;
     private ArrayList<String> mImageUrls;
     private transient Bitmap mImage;
 //    private transient BitmapDrawable mImage;
 //    private transient BitmapDrawable mImage;
 //    private transient ScaleDrawable mImage;
 
     private boolean mCurrentlyDownloading;
 
     public Story(String title, String date, String description,
                  String plainText, String link, ArrayList<String> imageUrls) {
         this.mTitle = title;
         this.mDate = date;
         this.mDescription = description;
         this.mPlainText = plainText;
         this.mLink = link;
         this.mImageUrls = imageUrls;
 
         if (mImageUrls != null)
             if (!mImageUrls.isEmpty())
                 startDownloadImage(mImageUrls.get(0));
     }
 
     public void downloadImage() {
         if (mImageUrls != null)
             if (!mImageUrls.isEmpty())
                 startDownloadImage(mImageUrls.get(0));
     }
 
     private void startDownloadImage(final String imageUrl) {
         if (Config.LOGD)
             Log.d(TAG, "[startDownloadImage] " + imageUrl);
 
         synchronized (this) {
             if (mCurrentlyDownloading) return;
             else mCurrentlyDownloading = true;
         }
 
         new AsyncTask<Void, Void, Void>(){
             public Void doInBackground(Void... s) {
                 if (Config.LOGD)
                     Log.d(TAG, "[doInBackground] downloading image");
                 
                 try {
                     URL url = new URL(imageUrl);
                     InputStream is = url.openStream();
 
 //                    BitmapDrawable bd = new BitmapDrawable(BitmapFactory.decodeStream(is));
                     Bitmap bd = BitmapFactory.decodeStream(is);
 
                     int width = bd.getWidth();// .getIntrinsicWidth();
                     int height = bd.getHeight();// .getIntrinsicHeight();
 
                     if (Config.LOGD)
                         Log.d(TAG, "[getView] original image size: " +
                                 ((Integer)width).toString() + ", " + ((Integer)height).toString());
 
                    int x_ratio = width/120, y_ratio = height/120, scale_ratio;
 
                     if (x_ratio <= 1 || y_ratio <= 1)
                         scale_ratio = 1;
                     else if (x_ratio < y_ratio)
                         scale_ratio = x_ratio;
                     else
                         scale_ratio = y_ratio;
 
                     mImage = Bitmap.createScaledBitmap(bd, width/scale_ratio, height/scale_ratio, true);
 
                     if (Config.LOGD)
                         Log.d(TAG, "[getView] scaled image size: " +
                                 ((Integer)mImage.getWidth()).toString() + ", " +
                                 ((Integer)mImage.getHeight()).toString());
 
                     //ScaleDrawable sd = ScaleDrawable.createFromStream(is, "stream");
 
 //                    int width = bd.getIntrinsicWidth();
 //                    int height = bd.getIntrinsicHeight();
 //
 //                    if (Config.LOGD)
 //                        Log.d(TAG, "[doInBackground] image size: " +
 //                                ((Integer)width).toString() + ", " + ((Integer)height).toString());
 //
 //                    if (width > 120 && height > 90)
 //                        mImage = new ScaleDrawable(bd, Gravity.CLIP_HORIZONTAL | Gravity.CLIP_VERTICAL, width/3, height/3);
 //                    else
 //                        mImage = new ScaleDrawable(bd, Gravity.CLIP_HORIZONTAL | Gravity.CLIP_VERTICAL, width, height);
 
                 } catch (MalformedURLException e) {
                     if (Config.LOGD) Log.d(TAG, e.toString());
                 } catch (IOException e) {
                     if (Config.LOGD) Log.d(TAG, e.toString());
                 } catch (RuntimeException e) {
                     if (Config.LOGD) Log.d(TAG, e.toString());
                 } catch (OutOfMemoryError e) {
                     if (Config.LOGD) Log.d(TAG, e.toString());
                 }
 
 
                 mCurrentlyDownloading = false;
                 return null;
             }
 
             protected void onPostExecute(Boolean loadSuccess) {
                 Log.d(TAG, "[onPostExecute] image download onPostExecute, result: " +
                         (loadSuccess ? "succeeded" : "failed"));
 
 //                if (loadSuccess)
 //                    mListener.AsyncFeedReadSucceeded();
 //                else
 //                    mListener.AsyncFeedReadFailed();
             }
         }.execute();
     }
 
     public String getTitle() {
             return mTitle;
     }
 
     public String getDate() {
         return mDate;
     }
 
     public String getDescription() {
         return mDescription;
     }
 
     public String getPlainText() {
         return mPlainText;
     }
 
     public String getLink() {
         return mLink;
     }
 
     public ArrayList<String> getImageUrls() {
         return mImageUrls;
     }
 
 //    public ScaleDrawable getImage() {
 //    public BitmapDrawable getImage() {
     public Bitmap getImage() {
         return mImage;
     }
 
 }
