 /*
  * Copyright (C) 2012 Appunite.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.appunite.imageloader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.Build;
 import android.util.DisplayMetrics;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.provider.MediaStore;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.ImageView;
 
 /**
  * 
  * @author Jacek Marchwicki (jacek.marchwicki@gmail.com)
  * 
  */
 public class RemoteImageLoader {
 
 	private class DownloadImageThread extends Thread {
 
 		private final String TAG = DownloadImageThread.class.getCanonicalName();
 		private boolean mStop = false;
 
 		public DownloadImageThread() {
 		}
 
 		synchronized boolean isStopped() {
 			return this.mStop;
 		}
 
 		private Bitmap loadFromDiskCache(String resource) {
 			File diskCacheFile;
 			synchronized (RemoteImageLoader.this.mDiskCache) {
 				diskCacheFile = RemoteImageLoader.this.mDiskCache
 						.getCacheFile(resource);
 				if (!diskCacheFile.exists())
 					return null;
 			}
 			return receiveBitmapFromFile(diskCacheFile.getAbsolutePath());
 		}
 
 		private Bitmap receiveBitmapFromHttp(String resource) {
 			InputStream inputStream = null;
 			Bitmap bitmap = this.loadFromDiskCache(resource);
 			if (bitmap == null) {
 
 				try {
 					URL url;
 					if (RemoteImageLoader.this.mBaseUrl == null) {
 						url = new URL(resource);
 					} else {
 						url = new URL(RemoteImageLoader.this.mBaseUrl, resource);
 					}
 					URLConnection connection = url.openConnection();
 					connection.connect();
 					inputStream = connection.getInputStream();
 					this.saveInDiskCache(inputStream, resource);
 					bitmap = this.loadFromDiskCache(resource);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					try {
 						if (inputStream != null)
 							inputStream.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			return bitmap;
 		}
 
 		@Override
 		public void run() {
 				while (!this.isStopped()) {
 					try {
 					String resource;
 					resource = RemoteImageLoader.this.takeToProcess();
 					Uri uri = Uri.parse(resource);
 					String scheme = uri.getScheme();
 
 					
 					Log.v(RemoteImageLoader.this.RUNABLE_TAG,
 							"started downloading: " + resource);
 					Bitmap bitmap;
 					if (scheme == null) {
 						bitmap = this.receiveBitmapFromFile(resource);
 					} else if (scheme.equals("http") || scheme.equals("https")) {
 						bitmap = this.receiveBitmapFromHttp(resource);
 					} else if (scheme.equals("content")) {
 						bitmap = this.receiveBitmapFromContentProvider(uri);
 					} else if (scheme.equals("file")) {
 						bitmap = this.receiveBitmapFromFile(uri.getPath());
 					} else {
 						bitmap = null;
 					}
 					Log.v(RemoteImageLoader.this.RUNABLE_TAG,
 							"finished downloading: " + resource);
 
 					List<ImageHolder> imageHolders = RemoteImageLoader.this
 							.finishByResource(resource);
 					if (bitmap != null)
 						RemoteImageLoader.this.mCache.put(resource, bitmap);
 					RemoteImageLoader.this.receivedDrawable(bitmap, resource,
 							imageHolders);
 					} catch (InterruptedException e) {
 					}
 				}
 
 		}
 		
 		public int getImageOrientation(String filePath) {
 			try {
 				ExifInterface exifReader = new ExifInterface(filePath);
 				int exifRotation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
 				switch (exifRotation) {
 				case ExifInterface.ORIENTATION_NORMAL:
 					return 0;
 				case ExifInterface.ORIENTATION_ROTATE_90:
 					return 90;				
 				case ExifInterface.ORIENTATION_ROTATE_180:
 					return 180;
 				case ExifInterface.ORIENTATION_ROTATE_270:
 					return 270;
 				}
 				
 			} catch (IOException e) {
 			}
 			return 0;
 		}
 		
 		public int getImageOrientation(Uri uri) {
 			Cursor cursor = mActivity.getContentResolver().query(uri,
 					new String[] { MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.Images.ImageColumns.DATA},
 					null, null, null);
 			if (!cursor.moveToFirst())
 				return 0;
 			
 			int rotation = cursor.getInt(0);
 			if (rotation != 0)
 				return rotation;
 
 			String filePath = cursor.getString(1);
 			if (TextUtils.isEmpty(filePath))
 				return rotation;
 			
 			return getImageOrientation(filePath);
 		}
 
 		private Bitmap receiveBitmapFromContentProvider(Uri uri) {
 			try {
 				ContentResolver contentResolver = mActivity.getContentResolver();
 				
 				String contentType = contentResolver.getType(uri);
 				if (contentType != null && contentType.startsWith("video/")) {
 					Cursor cursor = mActivity.getContentResolver().query(uri, new String[] {MediaStore.Video.VideoColumns._ID},null,null,null);
 					if (cursor.moveToFirst()) {
 						long originId = cursor.getLong(0);
 						Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(
 							contentResolver, originId, MediaStore.Video.Thumbnails.MINI_KIND, null);
 						if (curThumb != null)
 							return curThumb;
 					}
 				} else if (contentType != null && contentType.startsWith("image/")) {
 					Cursor cursor = mActivity.getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns._ID},null,null,null);
 					if (cursor.moveToFirst()) {
 						long originId = cursor.getLong(0);
 						Bitmap curThumb = MediaStore.Images.Thumbnails.getThumbnail(
 							contentResolver, originId, MediaStore.Images.Thumbnails.MINI_KIND, null);
 						if (curThumb != null) {
 							return getRotatedBitmap(curThumb, getImageOrientation(uri));
 						}
 					}
 				}
 				InputStream inputStream = contentResolver.openInputStream(uri);
 				int imageScaleFactore = ImageLoader.getImageScaleFactore(inputStream, RemoteImageLoader.this.mImageRequestedHeight,
 					RemoteImageLoader.this.mImageRequestedWidth,
 					RemoteImageLoader.this.mImageMaxHeight,
 					RemoteImageLoader.this.mImageMaxWidth);
 				inputStream.close();
 				inputStream = mActivity.getContentResolver().openInputStream(uri);
 				Bitmap bitmap = ImageLoader.loadImage(inputStream, imageScaleFactore);
 				inputStream.close();
 				
 				bitmap = getRotatedBitmap(bitmap, getImageOrientation(uri));
 				
 				return bitmap;
 			} catch (FileNotFoundException e) {
 				Log.w(TAG, "Could not found Content provider file", e);
 				return null;
 			} catch (IOException e) {
 				Log.w(TAG, "Could not found Content provider file", e);
 				return null;
 			}
 		}
 
 		private Bitmap getRotatedBitmap(Bitmap bitmap, int imageOrientation) {
 			if (imageOrientation == 0) 
 				return bitmap;
 			if (bitmap == null)
 				return null;
 			Matrix matrix = new Matrix();
 			matrix.postRotate(imageOrientation);
 			return Bitmap.createBitmap(bitmap, 0, 0, 
 					bitmap.getWidth(), bitmap.getHeight(), 
 			                              matrix, true);
 		}
 
 		private Bitmap receiveBitmapFromFile(String resource) {
 			int imageOrientation = getImageOrientation(resource);
 			Bitmap bitmap = ImageLoader.loadImage(
 					resource,
 					RemoteImageLoader.this.mImageRequestedHeight,
 					RemoteImageLoader.this.mImageRequestedWidth,
 					RemoteImageLoader.this.mImageMaxHeight,
 					RemoteImageLoader.this.mImageMaxWidth);
 			return getRotatedBitmap(bitmap, imageOrientation);
 		}
 
 		private void saveInDiskCache(InputStream reader, String resource) {
 			synchronized (RemoteImageLoader.this.mDiskCache) {
 				File diskCacheFile = RemoteImageLoader.this.mDiskCache
 						.getCacheFile(resource);
 
 				try {
 					int bytesRead = 0;
 
 					OutputStream outputStream = new FileOutputStream(
 							diskCacheFile);
 
 					while ((bytesRead = reader
							.read(RemoteImageLoader.this.mBuffer)) > 0) {
 						outputStream.write(RemoteImageLoader.this.mBuffer, 0,
 								bytesRead);
 					}
 
 					outputStream.flush();
 					outputStream.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		synchronized public void stopSelf() {
 			this.mStop = true;
 		}
 
 	}
 
 	private static class FileCache extends LruCache<String, Bitmap> {
 		public FileCache(int maxSize) {
 			super(maxSize);
 		}
 
 		@TargetApi(12)
 		private int getByteCount12(Bitmap value) {
 			return value.getByteCount();
 		}
 
 		@Override
 		protected int sizeOf(String key, Bitmap value) {
 			if (Build.VERSION.SDK_INT >= 12) {
 				return this.getByteCount12(value);
 			} else {
 				return value.getWidth() * value.getHeight() * 4;
 			}
 
 		}
 	}
 
 	public interface ImageHolder {
 		void setRemoteBitmap(Bitmap bitmap);
 	}
 
 	public static class ImageViewHolder implements ImageHolder {
 		private final ImageView mImageView;
 
 		public ImageViewHolder(ImageView imageView) {
 			this.mImageView = imageView;
 		}
 
 		@Override
 		public void setRemoteBitmap(Bitmap bitmap) {
 			this.mImageView.setImageBitmap(bitmap);
 		}
 
 	}
 
 	private final String RUNABLE_TAG = DownloadImageThread.class
 			.getCanonicalName();
 
 	private static final String IMAGE_CACHE_DIR_PREFIX = "ImageCache";
 
 	private static final float MAX_IMAGE_DEFAULT_FACTOR = 3.0f;
 
 	private final LruCache<String, Bitmap> mCache;
 
 	private final DiskCache mDiskCache;
 
 	private final byte[] mBuffer = new byte[1024];
 	private final Lock mLock = new ReentrantLock();
 
 	private final Condition mNotEmpty = this.mLock.newCondition();
 	public Map<ImageHolder, String> mViewResourceMap = new HashMap<ImageHolder, String>();
 	public List<String> mResourcesProcessingQueue = new ArrayList<String>();
 
 	public List<String> mResourcesQueue = new ArrayList<String>();
 
 	private final float mImageRequestedHeight;
 
 	private float mImageRequestedWidth;
 
 	private final float mImageMaxHeight;
 
 	private float mImageMaxWidth;
 
 	private final URL mBaseUrl;
 
 	private final Bitmap mPlaceHolder;
 
 	private final Activity mActivity;
 
 	private DownloadImageThread[] mDownloadImageThread;
 
 	public RemoteImageLoader(Activity activity, Bitmap placeHolder,
 			float requestedHeight, float requestedWidth) {
 		this(activity, null, placeHolder, requestedWidth, requestedWidth,
 				requestedWidth * MAX_IMAGE_DEFAULT_FACTOR, requestedWidth
 						* MAX_IMAGE_DEFAULT_FACTOR);
 	}
 
 	public RemoteImageLoader(Activity activity, URL baseUrl,
 			Bitmap placeHolder, float requestedHeight, float requestedWidth) {
 		this(activity, baseUrl, placeHolder, requestedHeight, requestedWidth,
 				requestedHeight * MAX_IMAGE_DEFAULT_FACTOR, requestedWidth
 						* MAX_IMAGE_DEFAULT_FACTOR);
 	}
 
 	/**
 	 * Create class
 	 * 
 	 * @param activity
 	 *            activity that should be owner
 	 * @param baseUrl
 	 *            base url for all files, can be null
 	 * @param placeHolder
 	 *            bitmap that should be placed insted of downloaded image, while
 	 *            loading begun
 	 * @param requestedHeight
 	 *            requested height
 	 * @param requestedWidth
 	 *            requested width
 	 * @param maxHeight
 	 *            maximal height
 	 * @param maxWidth
 	 *            maximal width
 	 */
 	public RemoteImageLoader(Activity activity, URL baseUrl,
 			Bitmap placeHolder, float requestedHeight, float requestedWidth,
 			float maxHeight, float maxWidth) {
 		this.mActivity = activity;
 		this.mBaseUrl = baseUrl;
 		this.mPlaceHolder = placeHolder;
 		this.mImageRequestedHeight = requestedHeight;
 		this.mImageRequestedWidth = requestedWidth;
 		this.mImageMaxHeight = maxHeight;
 		this.mImageRequestedWidth = maxWidth;
 		this.mDiskCache = new DiskCache(activity, IMAGE_CACHE_DIR_PREFIX);
 
 		int cacheSize = 4 * 1024 * 1024; // 4MiB
 		this.mCache = new FileCache(cacheSize);
 		
 		int numberOfThreads = 1;
 		if (Build.VERSION.SDK_INT >= 10)
 			numberOfThreads = 3;
 		
 		this.mDownloadImageThread = new DownloadImageThread[numberOfThreads];
 
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 	}
 
 	private List<ImageHolder> finishByResource(String resource) {
 		List<ImageHolder> imageHolders = new ArrayList<ImageHolder>();
 		this.mLock.lock();
 		try {
 			for (ImageHolder imageHolder : this.mViewResourceMap.keySet()) {
 				String viewResource = this.mViewResourceMap.get(imageHolder);
 				if (viewResource.equals(resource))
 					imageHolders.add(imageHolder);
 			}
 			for (ImageHolder imageHolder : imageHolders) {
 				this.mViewResourceMap.remove(imageHolder);
 			}
 
 			this.mResourcesProcessingQueue.remove(resource);
 		} finally {
 			this.mLock.unlock();
 		}
 		return imageHolders;
 	}
 
 	/**
 	 * actualy downlad image and display to correct ImageView
 	 * 
 	 * @param imageView
 	 *            ImageView that should display image
 	 * @param resource
 	 *            url or its tail to download, can be null
 	 */
 	public synchronized void loadImage(ImageHolder imageHolder, String resource) {
 		this.removeFromProcess(imageHolder);
 		if (TextUtils.isEmpty(resource)) {
 			imageHolder.setRemoteBitmap(this.mPlaceHolder);
 			return;
 		}
 		Bitmap cachedBitmap = this.mCache.get(resource);
 		if (cachedBitmap != null) {
 			imageHolder.setRemoteBitmap(cachedBitmap);
 			return;
 		}
 
 		imageHolder.setRemoteBitmap(this.mPlaceHolder);
 		try {
 			this.putToProcess(resource, imageHolder);
 		} catch (InterruptedException e) {
 			// Ignore this error
 		}
 	}
 
 	/**
 	 * actualy downlad image and display to correct ImageView use loadImage with
 	 * ImageHolder
 	 * 
 	 * @deprecated
 	 * @param imageView
 	 *            ImageView that should display image
 	 * @param resource
 	 *            url or its tail to download, can be null
 	 */
 	@Deprecated
 	public synchronized void loadImage(ImageView imageView, String resource) {
 		this.loadImage(new ImageViewHolder(imageView), resource);
 	}
 
 	/**
 	 * Call it on activity Pause
 	 */
 	public void onActivityPause() {
 		for (int i = 0; i < this.mDownloadImageThread.length; i++) {
 			DownloadImageThread thread = this.mDownloadImageThread[i];
 			thread.stopSelf();
 			thread.interrupt();
 			thread = null;	
 			this.mDownloadImageThread[i] = null;
 		}
 	}
 	
 	/**
 	 * Call it on activity onLowMemory
 	 */
 	public void onActivityLowMemory() {
 		this.mCache.evictAll();
 	}
 
 	/**
 	 * Call it on activity Resume
 	 */
 	public void onActivityResume() {
 		for (int i = 0; i < this.mDownloadImageThread.length; i++) {
 			DownloadImageThread thread = new DownloadImageThread();
 			thread.setPriority(Thread.MIN_PRIORITY);
 			thread.setName(String.format("DownloadImageThread[%d]", i));
 			thread.start();
 			this.mDownloadImageThread[i] = thread;
 		}
 	}
 
 	private boolean putToProcess(String resource, ImageHolder imageHolder)
 			throws InterruptedException {
 		this.mLock.lock();
 		try {
 			this.mViewResourceMap.put(imageHolder, resource);
 			boolean contains = this.mResourcesQueue.contains(resource);
 			if (contains)
 				return false;
 			contains = this.mResourcesProcessingQueue.contains(resource);
 			if (contains)
 				return false;
 			this.mResourcesQueue.add(resource);
 			this.mNotEmpty.signal();
 			return true;
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	private synchronized void receivedDrawable(final Bitmap bitmap,
 			String resource, final List<ImageHolder> imageHolders) {
 		if (bitmap == null)
 			return;
 		this.mActivity.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				for (ImageHolder imageHolder : imageHolders) {
 					imageHolder.setRemoteBitmap(bitmap);
 				}
 			}
 		});
 	}
 
 	private void removeFromProcess(ImageHolder imageHolder) {
 		this.mLock.lock();
 		try {
 			String resource = this.mViewResourceMap.remove(imageHolder);
 			if (resource == null)
 				return;
 			boolean found = false;
 			for (ImageHolder imageHolderIter : this.mViewResourceMap.keySet()) {
 				String viewResource = this.mViewResourceMap
 						.get(imageHolderIter);
 				if (viewResource.equals(resource)) {
 					found = true;
 					break;
 				}
 			}
 			if (found)
 				return;
 			this.mResourcesQueue.remove(resource);
 
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	private String takeToProcess() throws InterruptedException {
 		this.mLock.lock();
 		try {
 			while (this.mResourcesQueue.size() == 0)
 				this.mNotEmpty.await();
 			String resource = this.mResourcesQueue.remove(0);
 			this.mResourcesProcessingQueue.add(resource);
 			return resource;
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	private static float convertDpToPixel(float dp, Resources resources) {
 		DisplayMetrics metrics = resources.getDisplayMetrics();
 		float px = (dp * (metrics.densityDpi / 160f));
 		return px;
 	}
 
 	public static RemoteImageLoader createUsingDp(Activity activity,
 			Bitmap placeHolder, float requestedWidthDp, float requestedHeightDp) {
 		Resources resources = activity.getResources();
 		float widthPx = convertDpToPixel(requestedWidthDp, resources);
 		float heightPx = convertDpToPixel(requestedHeightDp, resources);
 		return new RemoteImageLoader(activity, placeHolder, widthPx, heightPx);
 	}
 
 	public static RemoteImageLoader createUsingDp(Activity activity,
 			URL baseUrl, Bitmap placeHolder, float requestedWidthDp,
 			float requestedHeightDp) {
 		Resources resources = activity.getResources();
 		float widthPx = convertDpToPixel(requestedWidthDp, resources);
 		float heightPx = convertDpToPixel(requestedHeightDp, resources);
 		return new RemoteImageLoader(activity, baseUrl, placeHolder, widthPx,
 				heightPx);
 	}
 }
