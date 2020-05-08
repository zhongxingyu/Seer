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
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 import de.raptor2101.GalDroid.WebGallery.ImageCache;
 import de.raptor2101.GalDroid.WebGallery.Stream;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 
 public class ImageLoaderTask extends RepeatingTask<ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener>, Progress, Bitmap> {
   protected final static String CLASS_TAG = "ImageLoaderTask";
 
   private ImageCache mCache;
 
   private LayoutParams mLayoutParams;
   private WebGallery mWebGallery;
 
   public ImageLoaderTask(WebGallery webGallery, ImageCache cache) {
     mThreadName = "ImageLoaderTask";
     mWebGallery = webGallery;
     mCache = cache;
   }
 
   public void setLayoutParams(LayoutParams layoutParams) {
     mLayoutParams = layoutParams;
   }
 
   public void download(GalleryDownloadObject galleryDownloadObject, ImageLoaderTaskListener listener) {
     Log.d(CLASS_TAG, String.format("Enqueue download for %s", galleryDownloadObject));
     ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter = new ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener>(galleryDownloadObject, listener);
     enqueueTask(parameter);
   }
 
   @Override
   protected void onPreExecute(ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter) {
     GalleryDownloadObject galleryDownloadObject = parameter.getObject();
     Log.d(CLASS_TAG, String.format("%s - Task started", galleryDownloadObject));
     ImageLoaderTaskListener listener = parameter.getListener();
     if (listener != null) {
       listener.onLoadingStarted(galleryDownloadObject.getUniqueId());
     }
   };
 
   @Override
   protected void onCancelled(ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter, Bitmap bitmap) {
     GalleryDownloadObject galleryDownloadObject = parameter.getObject();
     Log.d(CLASS_TAG, String.format("%s - Task canceled", galleryDownloadObject));
 
     synchronized (mCache) {
       mCache.removeCacheFile(galleryDownloadObject.getUniqueId());
     }
 
     ImageLoaderTaskListener listener = parameter.getListener();
     if (listener != null) {
       listener.onLoadingCancelled(galleryDownloadObject.getUniqueId());
     }
   }
 
   @Override
   protected Bitmap doInBackground(ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter) {
     GalleryDownloadObject galleryDownloadObject = parameter.getObject();
     try {
       Log.d(CLASS_TAG, String.format("%s - Task running", galleryDownloadObject));
       String uniqueId = galleryDownloadObject.getUniqueId();
       InputStream inputStream = mCache.getFileStream(uniqueId);
 
       if (inputStream == null) {
        DownloadImage(galleryDownloadObject, parameter.getListener());
         ScaleImage(galleryDownloadObject);
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
         if (isCancelled()) {
           return null;
         }
 
         Log.d(CLASS_TAG, String.format("%s - Decoding local image", galleryDownloadObject));
         Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
         if (bitmap != null) {
           mCache.cacheBitmap(uniqueId, bitmap);
           Log.i(CLASS_TAG, String.format("%s - Decoding local image - complete", galleryDownloadObject));
         } else {
           Log.w(CLASS_TAG, String.format("Something goes wrong while Decoding %s, removing CachedFile", galleryDownloadObject));
           mCache.removeCacheFile(uniqueId);
         }
         return bitmap;
       }
     } catch (Exception e) {
       Log.w(CLASS_TAG, String.format("Something goes wrong while Downloading %s. ExceptionMessage: %s", galleryDownloadObject, e.getMessage()));
       return null;
     }
   }
 
   @Override
   protected void onProgressUpdate(ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter, Progress progress) {
     GalleryDownloadObject galleryDownloadObject = parameter.getObject();
     ImageLoaderTaskListener listener = parameter.getListener();
     if (!isCancelled() && listener != null) {
       listener.onLoadingProgress(galleryDownloadObject.getUniqueId(), progress.curValue, progress.maxValue);
     }
   }
   
   @Override
   protected void onPostExecute(ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter, Bitmap bitmap) {
     GalleryDownloadObject galleryDownloadObject = parameter.getObject();
     Log.d(CLASS_TAG, String.format("%s - Task done", galleryDownloadObject));
     ImageLoaderTaskListener listener = parameter.getListener();
     if (!isCancelled() && listener != null) {
       listener.onLoadingCompleted(galleryDownloadObject.getUniqueId(), bitmap);
     }
   }
 
  private void DownloadImage(GalleryDownloadObject galleryDownloadObject, ImageLoaderTaskListener listener) throws IOException {
     Log.d(CLASS_TAG, String.format("%s - Downloading to local cache file", galleryDownloadObject));
 
     Stream networkStream = mWebGallery.getFileStream(galleryDownloadObject);
     String uniqueId = galleryDownloadObject.getUniqueId();
     OutputStream fileStream = mCache.createCacheFile(uniqueId);
 
     byte[] writeCache = new byte[10 * 1024];
     int readCounter;
     int currentPos = 0;
     int length = (int) networkStream.getContentLength();
     while ((readCounter = networkStream.read(writeCache)) > 0 && !isCancelled()) {
       fileStream.write(writeCache, 0, readCounter);
       currentPos += readCounter;
       
       publishProgress(new Progress(currentPos, length));
     }
     fileStream.close();
     networkStream.close();
 
     if (isCancelled()) {
       // if the download is aborted, the file is waste of bytes...
       Log.i(CLASS_TAG, String.format("%s - Download canceled", galleryDownloadObject));
       mCache.removeCacheFile(uniqueId);
     } else {
       mCache.refreshCacheFile(uniqueId);
     }
     Log.d(CLASS_TAG, String.format("%s - Downloading to local cache file - complete", galleryDownloadObject));
   }
 
   private void ScaleImage(GalleryDownloadObject galleryDownloadObject) throws IOException {
     if (mLayoutParams != null) {
       Log.d(CLASS_TAG, String.format("%s - Decoding Bounds", galleryDownloadObject));
 
       String uniqueId = galleryDownloadObject.getUniqueId();
       FileInputStream bitmapStream = mCache.getFileStream(uniqueId);
       Options options = new Options();
       options.inJustDecodeBounds = true;
 
       synchronized (mCache) {
         if (isCancelled()) {
           return;
         }
 
         BitmapFactory.decodeStream(bitmapStream, null, options);
       }
 
       bitmapStream.close();
       Log.d(CLASS_TAG, String.format("%s - Decoding Bounds - done", galleryDownloadObject));
 
       int imgHeight = options.outHeight;
       int imgWidth = options.outWidth;
 
       int highestLayoutDimension = mLayoutParams.height > mLayoutParams.width ? mLayoutParams.height : mLayoutParams.width;
       int highestImageDimension = imgHeight > imgWidth ? imgHeight : imgWidth;
 
       int sampleSize = highestImageDimension / highestLayoutDimension;
 
       options = new Options();
       options.inInputShareable = true;
       options.inPreferredConfig = Bitmap.Config.ARGB_8888;
       options.inDither = true;
       options.inPurgeable = true;
       options.inPreferQualityOverSpeed = true;
 
       if (sampleSize > 1) {
         options.inSampleSize = sampleSize;
       }
 
       synchronized (mCache) {
         if (isCancelled()) {
           return;
         }
 
         bitmapStream = mCache.getFileStream(uniqueId);
         Log.d(CLASS_TAG, String.format("%s - Resize Image", galleryDownloadObject));
         Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream, null, options);
         bitmapStream.close();
         mCache.storeBitmap(uniqueId, bitmap);
         bitmap.recycle();
         Log.d(CLASS_TAG, String.format("%s - Resize Image - done", galleryDownloadObject));
       }
     }
   }
 
   public boolean isDownloading(GalleryDownloadObject galleryDownloadObject) {
     if (galleryDownloadObject == null) {
       return false;
     }
     ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter = new ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener>(galleryDownloadObject, null);
     boolean isActive = isActive(parameter);
     boolean isEnqueued = isEnqueued(parameter);
     Log.d(CLASS_TAG, String.format("isActive: %s isEnqueued %s", isActive, isEnqueued));
     return isActive || isEnqueued;
   }
 
   public void cancel(GalleryDownloadObject downloadObject) {
     if (downloadObject == null) {
       return;
     }
 
     ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener> parameter = new ListenedParameter<GalleryDownloadObject, ImageLoaderTaskListener>(downloadObject, null);
     if (isEnqueued(parameter)) {
       removeEnqueuedTask(parameter);
     } else if (isActive(parameter)) {
       cancelCurrentTask();
     }
   }
 }
