 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery.Tasks;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.ref.WeakReference;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 import de.raptor2101.GalDroid.WebGallery.GalleryCache;
 import de.raptor2101.GalDroid.WebGallery.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 
 public class ImageLoaderTask extends AsyncTask<Void, Progress, GalleryDownloadObject> {
 	private final static String ClassTag = "ImageLoaderTask";
 	private WebGallery mWebGallery;
 	private GalleryCache mCache;
 	private GalleryDownloadObject mDownloadObject;
 	private WeakReference<ImageLoaderTaskListener> mListener;
 	private LayoutParams mLayoutParams;
 	private boolean mDownloadRunning;
 	
 	public ImageLoaderTask(WebGallery webGallery, GalleryCache cache, GalleryDownloadObject downloadObject){
 		mWebGallery = webGallery;
 		mCache = cache;
 		mDownloadObject = downloadObject;
 		mListener = new WeakReference<ImageLoaderTaskListener>(null); 
 		mDownloadRunning = false;
 	}
 	
 	public void setListener(ImageLoaderTaskListener listener){
 		mListener = new WeakReference<ImageLoaderTaskListener>(listener); 
 	}
 	
 	public void setLayoutParams(LayoutParams layoutParams) {
 		mLayoutParams = layoutParams;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		Log.d(ClassTag, String.format("%s - Task started", mDownloadObject));
 		ImageLoaderTaskListener listener = mListener.get();
 		if(listener != null){
 			listener.onLoadingStarted(mDownloadObject.getUniqueId());
 		}
 	};
 	
 	@Override
 	protected void onCancelled() {
 		Log.d(ClassTag, String.format("%s - Task canceled", mDownloadObject));
 		if (mDownloadRunning) {
 			synchronized (mCache) {
 				mCache.removeCacheFile(mDownloadObject.getUniqueId());
 			}
 		}
 	}
 
 	@Override
 	protected GalleryDownloadObject doInBackground(Void... params) {
 		try {
 			Log.d(ClassTag, String.format("%s - Task running", mDownloadObject));
 			String uniqueId = mDownloadObject.getUniqueId();
 			InputStream inputStream = mCache.getFileStream(uniqueId);
 			
 			if(inputStream == null) {
 				DownloadImage(uniqueId);
 				ScaleImage(uniqueId);
 				inputStream = mCache.getFileStream(uniqueId);
 			}
 			
 			Options options = new Options();
 			options.inPreferQualityOverSpeed = true;
 			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 			options.inDither = true;
 			options.inScaled = false;
 			options.inPurgeable = true;
 			options.inInputShareable = true;
 				
 				
 				
 			synchronized (mCache) {
 				Log.d(ClassTag, String.format("%s - Decoding local image", mDownloadObject));
 				Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
 				mDownloadObject.setBitmap(bitmap);
 				mCache.cacheBitmap(uniqueId, bitmap);
 				Log.d(ClassTag, String.format("%s - Decoding local image - complete", mDownloadObject));
 			}
 				
 			
 			return mDownloadObject;
 		} catch (Exception e) {
 			Log.w(ClassTag, String.format("Something goes wrong while Downloading %s. ExceptionMessage: %s",mDownloadObject,e.getMessage()));
 			return mDownloadObject;
 		}
 	}
 
 	@Override
 	protected void onPostExecute(GalleryDownloadObject downloadObject) {
 		Log.d(ClassTag, String.format("%s - Task done", mDownloadObject));
 		Bitmap bitmap = null;
 		if(!isCancelled()&&downloadObject.isValid())
 		{
 			bitmap = downloadObject.getBitmap();
 		}
 		ImageLoaderTaskListener listener = mListener.get();
 		if(listener != null){
 			listener.onLoadingCompleted(mDownloadObject.getUniqueId(), bitmap);
 		}
 	}
 
 	private void DownloadImage(String uniqueId) throws IOException {
 		Log.d(ClassTag, String.format("%s - Downloading to local cache file", mDownloadObject));
 		mDownloadRunning = true;
 		InputStream networkStream = mWebGallery.getImageRawData(mDownloadObject.getGalleryObject(),mDownloadObject.getImageSize());
 		OutputStream fileStream = mCache.createCacheFile(uniqueId);
 		byte[] writeCache = new byte[1024];
 		int readCounter;
		while((readCounter = networkStream.read(writeCache)) > 0){
 			fileStream.write(writeCache, 0, readCounter);
 		}
 		fileStream.close();
 		networkStream.close();
 		
		mCache.refreshCacheFile(uniqueId);
		mDownloadRunning = false;;
 		Log.d(ClassTag, String.format("%s - Downloading to local cache file - complete", mDownloadObject));
 	}
 
 	private void ScaleImage(String uniqueId) throws IOException {
 		if(mLayoutParams != null) {
 			Log.d(ClassTag, String.format("%s - Decoding Bounds", mDownloadObject));
 			
 			FileInputStream bitmapStream = mCache.getFileStream(uniqueId);
 			Options options = new Options();
 			options.inJustDecodeBounds = true;
 			
 			synchronized (mCache) {
 				BitmapFactory.decodeStream( bitmapStream, null, options);	
 			}
 			
 			bitmapStream.close();
 			Log.d(ClassTag, String.format("%s - Decoding Bounds - done", mDownloadObject));
 			
 			int imgHeight = options.outHeight;
 			int imgWidth = options.outWidth;
 			
 			int highestLayoutDimension =  mLayoutParams.height > mLayoutParams.width? mLayoutParams.height : mLayoutParams.width;
 			int highestImageDimension = imgHeight > imgWidth ? imgHeight : imgWidth;
 			
 			int sampleSize = highestImageDimension / highestLayoutDimension;
 			
 			options = new Options();
 			options.inInputShareable = true;
 			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 			options.inDither = true;
 			options.inPurgeable = true;
 			options.inPreferQualityOverSpeed = true;
 			
 			if(sampleSize > 1) {
 				options.inSampleSize = sampleSize;
 			}
 			
 			synchronized (mCache) {
 				bitmapStream = mCache.getFileStream(uniqueId);
 				Log.d(ClassTag, String.format("%s - Resize Image", mDownloadObject));
 				Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream, null, options);
 				bitmapStream.close();
 				mCache.storeBitmap(uniqueId, bitmap);
 				bitmap.recycle();
 				Log.d(ClassTag, String.format("%s - Resize Image - done", mDownloadObject));
 			}
 		}
 	}
 }
