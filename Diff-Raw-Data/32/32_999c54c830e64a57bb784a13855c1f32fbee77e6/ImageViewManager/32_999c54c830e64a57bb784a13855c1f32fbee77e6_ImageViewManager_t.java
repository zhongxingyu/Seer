 package com.parworks.mars.view.siteexplorer;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.widget.ImageView;
 
 import com.parworks.mars.cache.BitmapCache;
 import com.parworks.mars.cache.BitmapWorkerTask;
 import com.parworks.mars.cache.BitmapWorkerTask.BitmapWorkerListener;
 
 public class ImageViewManager {
 	
 	public interface ImageLoadedListener {
 		public void onImageLoaded();
 	}
 	public static final String TAG = ImageViewManager.class.getName();	
 	
 	public void setImageView(String url, final ImageView imageView, final ImageLoadedListener listener) {
 		if(url != null) {
 			Bitmap posterImageBitmap = BitmapCache.get().getBitmap(
 					BitmapCache.getImageKeyFromURL(url));
 			if (posterImageBitmap == null) {
 				Log.d(TAG, "Bitmap not found in cache, start to download it.");
 				new BitmapWorkerTask(url, new BitmapWorkerListener() {					
 					@Override
 					public void bitmapLoaded(Bitmap bitmap) {
						imageView.setImageBitmap(bitmap);
 						if (listener != null) {
 							listener.onImageLoaded();
 						}
 					}
 				}).execute();
 			} else {
				imageView.setImageBitmap(posterImageBitmap);
 				if (listener != null) {
 					listener.onImageLoaded();
 				}
 			}
 			
 		}  else {
 			Log.e(TAG, "mapURL was null.");
 		}
 	}
 		
 //	private void setImageSizeMaintainAspectRatio(ImageView imageView,int width) {
 //		Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
 //		int height = ViewDimensionCalculator.calculateHeightToMaintainAspectRatio(width, bitmap);
 //		bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
 //		imageView.setImageBitmap(bitmap);		
 //	}
 }
