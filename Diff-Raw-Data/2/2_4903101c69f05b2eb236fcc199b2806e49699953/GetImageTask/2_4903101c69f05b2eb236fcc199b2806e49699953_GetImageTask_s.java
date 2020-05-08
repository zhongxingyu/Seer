 package com.jinheyu.lite_mms;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.util.Log;
 import com.jinheyu.lite_mms.netutils.ImageCache;
 
 /**
  * Created by <a href='https://github.com/abc549825'>abc549825@163.com</a> at 09-23.
  */
 public class GetImageTask extends AsyncTask<Void, Void, Bitmap> {
 
     private Exception ex;
     private String mKey;
     private String mUrl;
     private boolean mShowToast;
     private ImageView mImageView;
     private ImageCache mImageCache;
     private static final String TAG = "GET_IMAGE_TASK";
     
     public GetImageTask(ImageView imageView, String url) {
         this(imageView, url, true);
     }
 
     public GetImageTask(ImageView imageView, String url, boolean showToast) {
         this.mImageView = imageView;
         this.mUrl = url;
         this.mKey = Utils.getMd5Hash(url);
         this.mShowToast = showToast;
         mImageCache = ImageCache.getInstance(mImageView.getContext());
     }
 
     private int calculateSampleSize(ImageView mImageView) {
         // 按500w（2560×1920）像素， 720×1280屏幕计算
         if (mImageView.getScaleType() == ImageView.ScaleType.MATRIX) {
             return 1;
         }
         if (mImageView.getMeasuredHeight() == 0 && mImageView.getMeasuredWidth() == 0) {
             return 2;
         }
         Log.d(TAG, "sample size: " + 16);
         return 16;
     }
 
     @Override
     protected Bitmap doInBackground(Void... params) {
         mImageView.setTag(mUrl);
         if (Utils.isEmptyString(mUrl)) {
             return null;
         }
         final int sampleSize = calculateSampleSize(mImageView);
         try {
             Log.d(TAG, "try to get bitmap from cache " + mKey);
             Bitmap bitmap = mImageCache.getBitmapFromDiskCache(mKey, sampleSize);
             if (bitmap == null) {
                 Log.d(TAG, "try to get bitmap from network " + mUrl);
                 mImageCache.addBitmapToCache(mKey, MyApp.getWebServieHandler().getSteamFromUrl(mUrl));
                 return mImageCache.getBitmapFromDiskCache(mKey, sampleSize);
             }
             return bitmap;
         } catch (Exception e) {
             ex = e;
         }
         return null;
     }
 
     @Override
     protected void onPostExecute(Bitmap bitmap) {
         if (mImageView.getTag() != mUrl) {
             return;
         }
         if (ex == null && bitmap != null) {
             mImageView.setImageBitmap(bitmap);
             if (mImageView instanceof ImageButton) {
                 mImageView.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Intent intent = new Intent(mImageView.getContext(), ImageActivity.class);
                        intent.putExtra("imageUrl", mKey);
                         mImageView.getContext().startActivity(intent);
                     }
                 });
             }
         } else {
             if (!Utils.isEmptyString(mKey)) {
                 mImageView.setImageResource(R.drawable.broken_image);
                 if (mShowToast) {
                     Toast.makeText(mImageView.getContext(), R.string.load_failure, Toast.LENGTH_SHORT).show();
                 }
             } else {
                 mImageView.setImageBitmap(null);
             }
         }
     }
 }
