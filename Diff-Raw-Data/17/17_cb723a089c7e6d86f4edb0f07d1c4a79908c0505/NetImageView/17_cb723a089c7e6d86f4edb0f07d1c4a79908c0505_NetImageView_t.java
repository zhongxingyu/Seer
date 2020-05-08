 package com.tools.tvguide.views;
 
 import java.lang.ref.WeakReference;
 
 import com.tools.tvguide.utils.Utility;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
import android.support.v4.util.LruCache;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 public class NetImageView extends ImageView
 {
     private static final String TAG = "NetImageView";
    private static LruCache<String, Bitmap> sCache = new LruCache<String, Bitmap>(100);
     private ImageUrlAsyncTask mCurrentTask;
     private Bitmap mBitmap;
     private String mUrl;
     
     public static interface ImageLoadListener
     {
         void onImageLoaded(Bitmap bitmap);
     }
     
     public NetImageView(Context context, AttributeSet attrs) 
     {
         super(context, attrs);
     }
 
     public void loadImage(String url)
     {
         if (url == null)
             return;
 
         if (mUrl != null && mUrl.equals(url))
        {
            setImageBitmap(mBitmap);
            return;
        }
        
        if (sCache.get(url) != null)
        {
            mBitmap = sCache.get(url);
             setImageBitmap(mBitmap);
            return;
        }
         
         if (mCurrentTask != null)
             mCurrentTask.cancel(true);
 
         mUrl = url;
         mCurrentTask = new ImageUrlAsyncTask(this, new ImageLoadListener() 
         {
             @Override
             public void onImageLoaded(Bitmap bitmap) 
             {
                 mBitmap = bitmap;
                sCache.put(mUrl, bitmap);
                 mCurrentTask = null;
             }
         });
         mCurrentTask.execute(url);
     }
     
     public String getUrl()
     {
         return mUrl;
     }
     
     private class ImageUrlAsyncTask extends AsyncTask<String, Void, Bitmap>
     {
         private WeakReference<NetImageView> mImageViewRef;
         private ImageLoadListener mListener;
         private String mTaskUrl;
         
         public ImageUrlAsyncTask(NetImageView imageView, ImageLoadListener listener)
         {
             assert (imageView != null);
             mImageViewRef = new WeakReference<NetImageView>(imageView);
             mListener = listener;
         }
         
         @Override
         protected Bitmap doInBackground(String... urls) 
         {
             mTaskUrl = urls[0];
             Bitmap bitmap = Utility.getNetworkImage(urls[0]);
             return bitmap;
         }
         
         @Override
         protected void onPostExecute(Bitmap bitmap)
         {
             if (bitmap == null)
                 return;
             
             if (mImageViewRef.get() != null && mTaskUrl.equals(mImageViewRef.get().getUrl()))
             {
                 mImageViewRef.get().setImageBitmap(bitmap);
             }
             
             if (mListener != null)
                 mListener.onImageLoaded(bitmap);
         }
     }
 }
