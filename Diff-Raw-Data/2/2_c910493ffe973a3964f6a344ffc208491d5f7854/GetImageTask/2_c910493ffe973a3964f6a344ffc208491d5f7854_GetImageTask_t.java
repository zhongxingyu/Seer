 package com.jinheyu.lite_mms;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.util.Log;
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.netutils.ImageCache;
 
 /**
  * Created by <a href='https://github.com/abc549825'>abc549825@163.com</a> at 09-23.
  */
 public class GetImageTask extends AsyncTask<Integer, Void, Bitmap> {
 
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
 
     @Override
     protected Bitmap doInBackground(Integer... params) {
         mImageView.setTag(mUrl);
         if (Utils.isEmptyString(mUrl)) {
             return null;
         }
         int sampleSize;
         try {
             sampleSize = params[0];
         } catch (IndexOutOfBoundsException e) {
             sampleSize = Constants.LARGE_SAMPLE_SIZE;
         }
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
                         intent.putExtra("imageUrl", mUrl);
                         mImageView.getContext().startActivity(intent);
                     }
                 });
             }
         } else {
            if (!Utils.isEmptyString(mUrl)) {
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
