 package com.tools.tvguide.views;
 
 import java.lang.ref.WeakReference;
 
 import com.tools.tvguide.components.AsyncTask;
 import com.tools.tvguide.utils.CacheControl;
 import com.tools.tvguide.utils.Utility;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 
 import android.support.v4.util.LruCache;
 import android.util.AttributeSet;
import android.util.Log;
 import android.widget.ImageView;
 
 public class NetImageView extends ImageView
 {
     private static final String TAG = "NetImageView";
     private static LruCache<String, Bitmap> sCache = new LruCache<String, Bitmap>(100);
     private ImageUrlAsyncTask mCurrentTask;
     private Bitmap mBitmap;
     private String mUrl;
     private CacheControl mCacheControl = CacheControl.Memory;
     
     public static interface ImageLoadListener
     {
         void onImageLoaded(String url, Bitmap bitmap);
     }
     
     public NetImageView(Context context, AttributeSet attrs) 
     {
         super(context, attrs);
     }
     
     public void setCacheControl(CacheControl control)
     {
     	mCacheControl = control;
     }
 
     public void loadImage(String... urls)
     {
         if (urls == null)
             return;
 
         for (String url : urls) 
         {
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
 		}
         
         execute(urls);
     }
     
     public String getUrl()
     {
         return mUrl;
     }
     
     private void execute(String... urls)
     {
     	if (mCurrentTask != null)
             mCurrentTask.cancel(true);
     	
     	mCurrentTask = new ImageUrlAsyncTask(this, new ImageLoadListener() 
         {
             @Override
             public void onImageLoaded(String url, Bitmap bitmap) 
             {
                 mBitmap = bitmap;
                 mUrl = url;
                 sCache.put(mUrl, bitmap);
                 mCurrentTask = null;
             }
         });
         mCurrentTask.execute(urls);
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
         	Bitmap bitmap = null;
             for (String url : urls) {
         		bitmap = Utility.getNetworkImage(url, mCacheControl);
         		if (bitmap != null) {
         			mTaskUrl = url;
         			break;
         		}
 			}
             return bitmap;
         }
         
         @Override
         protected void onPostExecute(Bitmap bitmap)
         {
             if (bitmap == null)
                 return;
             
            if (mImageViewRef.get() != null)
             {
            	Log.d(TAG, "onPostExecute url=" + mTaskUrl);
                 mImageViewRef.get().setImageBitmap(bitmap);
             }
             
             if (mListener != null)
                 mListener.onImageLoaded(mTaskUrl, bitmap);
         }
     }
 }
