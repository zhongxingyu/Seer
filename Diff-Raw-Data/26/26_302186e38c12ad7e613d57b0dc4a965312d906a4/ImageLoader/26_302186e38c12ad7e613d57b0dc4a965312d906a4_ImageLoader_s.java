 // Copyright (c) 2011, Chute Corporation. All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification,
 // are permitted provided that the following conditions are met:
 //
 // * Redistributions of source code must retain the above copyright notice, this
 // list of conditions and the following disclaimer.
 // * Redistributions in binary form must reproduce the above copyright notice,
 // this list of conditions and the following disclaimer in the documentation
 // and/or other materials provided with the distribution.
 // * Neither the name of the Chute Corporation nor the names
 // of its contributors may be used to endorse or promote products derived from
 // this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 // IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 // LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 package darko.imagedownloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLStreamHandler;
 import java.util.Collections;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.os.AsyncTask;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.ImageView;
 import darko.imagedownloader.PhotosQueue.PhotoToLoad;
 import darko.imagedownloader.PhotosQueue.QueueMethod;
 
 /**
  * @author Darko.Grozdanovski
  **/
 public class ImageLoader {
 
 	private final PhotosQueue photosQueue;
 	private final MemoryCache memoryCache;
 	private static final String TAG = ImageLoader.class.getSimpleName();
 	private boolean loggerEnable = false;
 	public static final int DEFAULT_HARD_CACHE_SIZE = 10;
 	private final FileCache fileCache;
 	private final boolean shouldCacheOnSD;
 	private final Map<ImageView, String> imageViews = Collections
 			.synchronizedMap(new WeakHashMap<ImageView, String>());
 	private final ContentURLStreamHandlerFactory streamFactory;
 	private int preloadDrawableId;
 	private int errorDrawableId;
 
 	/**
 	 * Use with {@link Context#getSystemService(String)} to retrieve an
 	 * {@link ImageLoader} for loading images.
 	 * <p>
 	 * Since {@link ImageLoader} is not a standard system service, you must
 	 * create a custom {@link Application} subclass implementing
 	 * {@link Application#getSystemService(String)} and add it to your
 	 * {@code AndroidManifest.xml}.
 	 * <p>
 	 * Using this constant is optional and it is only provided for convenience
 	 * and to promote consistency across deployments of this component.
 	 */
 	public static final String IMAGE_LOADER_SERVICE = "com.imagedownloader";
 	private int defaultBitmapSize = 75;
 
 	public ImageLoader(final Context context, final int defaultDrawableId) {
 		this(context, defaultDrawableId, true, DEFAULT_HARD_CACHE_SIZE,
 				QueueMethod.STACK);
 	}
 
 	public ImageLoader(final Context context, final int defaultDrawableId,
 			final boolean shouldCacheOnSD) {
 		this(context, defaultDrawableId, shouldCacheOnSD,
 				DEFAULT_HARD_CACHE_SIZE, QueueMethod.STACK);
 	}
 
 	/**
 	 * @param context
 	 *            application or activity context
 	 * @param defaultDrawableId
 	 *            the placeholder icon that will be displayed while the photo is
 	 *            loading or in case of an error
 	 * @param shouldCacheOnSD
 	 *            if the files should be cached on SD card
 	 * @param hardCacheCount
 	 *            the maximum amount of photos that should be kept in memory
 	 * @param queueMethod
 	 *            to set if the photos will be loaded FIFO(QUEUE) or LIFO(STACK)
 	 */
 	public ImageLoader(final Context context, final int defaultDrawableId,
 			final boolean shouldCacheOnSD, final int hardCacheCount,
 			final QueueMethod queueMethod) {
 		// Make the background thread low priority. This way it will not affect
 		// the UI performance
 		setDefaultDrawableResourceId(defaultDrawableId);
 		photosQueue = new PhotosQueue(queueMethod);
 		this.shouldCacheOnSD = shouldCacheOnSD;
 		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
 		fileCache = new FileCache(context);
 		memoryCache = new MemoryCache(hardCacheCount);
 		streamFactory = new ContentURLStreamHandlerFactory(
 				context.getContentResolver());
 
 	}
 
 	public static ImageLoader getLoader(Context context) {
 		if (context == null) {
 			throw new IllegalStateException(
 					"Context is null, please init the ImageLoader service in the Application class");
 		}
 		ImageLoader loader = (ImageLoader) context
 				.getSystemService(IMAGE_LOADER_SERVICE);
 		if (loader == null) {
 			context = context.getApplicationContext();
 			loader = (ImageLoader) context
 					.getSystemService(IMAGE_LOADER_SERVICE);
 		}
 		if (loader == null) {
 			throw new IllegalStateException(
 					"ImageLoader not available, please init the ImageLoader service in the Application class");
 		}
 		return loader;
 	}
 
 	public void setDefaultDrawableResourceId(final int defaultDrawableId) {
 		this.preloadDrawableId = defaultDrawableId;
 		this.errorDrawableId = defaultDrawableId;
 	}
 
 	public void setPreloadDrawableId(final int drawableId) {
 		this.preloadDrawableId = drawableId;
 	}
 
 	public void setLoadingErrorDrawableId(final int drawableId) {
 		this.errorDrawableId = drawableId;
 	}
 
 	public void setDefaultBitmapSize(final int size) {
 		this.defaultBitmapSize = size;
 	}
 
 	public void setLoggerStatus(final boolean active) {
 		this.loggerEnable = active;
 	}
 
 	/**
 	 * @see ImageLoader#fetchBitmapAsync(String, int, ImageLoaderListener)
 	 */
 	public void fetchBitmapAsync(final String url,
 			final ImageLoaderListener listener) {
 		fetchBitmapAsync(url, defaultBitmapSize, listener);
 	}
 
 	/**
 	 * This method executes in the background via {@link AsyncTask}. If
 	 * successful it returns a {@link Bitmap} of the image in the required
 	 * maximum size specified. It uses the SDcard cache to fetch the image if
 	 * already downloaded. This method doesn't store the images in the memory
 	 * cache!
 	 * 
 	 * @param url
 	 *            The URI of the photo
 	 * @param bitmapSizeInPixels
 	 *            the maximum size of the bigger side of the photo in pixels
 	 * @param listener
 	 *            callback for the result
 	 */
 	public void fetchBitmapAsync(final String url,
 			final int bitmapSizeInPixels, final ImageLoaderListener listener) {
 		if (listener == null) {
 			throw new IllegalArgumentException("Listener must not be null!");
 		}
 		new FetchBitmapTask(this, listener, bitmapSizeInPixels, url).execute();
 	}
 
 	/**
      * 
      */
 	public void displayImage(final String url, final ImageView imageView,
 			ImageLoaderListener listener) {
 		displayImage(url, imageView, defaultBitmapSize, listener);
 	}
 
 	/**
 	 * Binds a bitmap to the specified imageView. Uses in-memory cache and
 	 * SDCard cache.
 	 * 
 	 * @param url
 	 * @param imageView
 	 * @param bitmapSizeInPixels
 	 */
 	public void displayImage(final String url, final ImageView imageView,
 			final int bitmapSizeInPixels, final ImageLoaderListener listener) {
 		try {
 			imageViews.put(imageView, url);
 			final Bitmap bitmap = memoryCache.get(url);
 			if (bitmap != null) {
 				imageView.setImageBitmap(bitmap);
 				if (listener != null) {
 					listener.onImageLoadingComplete(url, bitmap);
 				}
 			} else {
 				queuePhoto(url, imageView, bitmapSizeInPixels, listener);
 				imageView.setImageResource(preloadDrawableId);
 			}
 		} catch (final Exception e) {
 			if (loggerEnable) {
 				Log.e(TAG, e.getMessage(), e);
 			}
 			if (listener != null) {
 				listener.onImageLoadingError();
 			}
 		}
 	}
 
 	private void queuePhoto(final String url, final ImageView imageView,
 			final int bitmapSizeInPixels, final ImageLoaderListener listener) {
 		// This ImageView may be used for other images before. So there may be
 		// some old tasks in the queue. We need to discard them.
 		photosQueue.clean(imageView);
 		final PhotoToLoad p = new PhotoToLoad(url, imageView,
 				bitmapSizeInPixels, listener);
 		synchronized (photosQueue.photosToLoad) {
 			photosQueue.photosToLoad.push(p);
 			photosQueue.photosToLoad.notifyAll();
 		}
 
 		// start thread if it's not started yet
 		if (photoLoaderThread.getState() == Thread.State.NEW) {
 			photoLoaderThread.start();
 		}
 	}
 
 	/**
 	 * @see ImageLoader#downloadBitmap(String, int)
 	 */
 	public Bitmap downloadBitmap(final String url) {
 		return downloadBitmap(url, defaultBitmapSize);
 	}
 
 	/**
 	 * This method runs in the same thread that it is called in. see
 	 * {@link ImageLoader#fetchBitmapAsync(String, int, ImageLoaderListener)}
 	 * for an Async Download task or use this one for a different async process.
 	 * 
 	 * @param url
 	 *            the URI of the file to load
 	 * @param bitmapSizeInPixels
 	 *            size the maximum size of the bigger side of the photo in
 	 *            pixels
 	 * @return a bitmap of the requested photo URL
 	 */
 	public Bitmap downloadBitmap(final String url, final int bitmapSizeInPixels) {
 		// TRY TO GET IT FROM SDCARD...
 		if (TextUtils.isEmpty(url)) {
 			return null;
 		}
 		File f = null;
 		if (shouldCacheOnSD) {
 			f = fileCache.getFile(url);
 			try {
 				if (f.exists()) {
 					// from SD cache
 					final Options options = Utils.decodeInBounds(f,
 							bitmapSizeInPixels);
 					final Bitmap b = BitmapFactory.decodeFile(f.getPath(),
 							options);
 					if (b != null) {
 						return b; // Can be null
 					}
 				}
 			} catch (final Exception e) {
 				if (loggerEnable) {
 					Log.e(TAG, e.getMessage(), e);
 				}
 			}
 		}
 		// IF IT DOESNT RETURN A BITMAP DOWNLOAD IT
 		try {
 			Bitmap bmp = null;
 			try {
 				final String protocol = Utils.getProtocol(url);
 				final URLStreamHandler streamHandler = streamFactory
 						.createURLStreamHandler(protocol);
 				final BitmapContentHandler handler = new BitmapContentHandler(
 						bitmapSizeInPixels, f);
				bmp = handler.getContent(new URL(null, url, streamHandler)
						.openConnection());
 				if (shouldCacheOnSD == false) {
 					f.delete();
 				}
 			} catch (final OutOfMemoryError e) {
 				if (loggerEnable) {
 					Log.e(TAG, e.getMessage(), e);
 				}
 				System.gc();
 			}
 			if (bmp != null) {
 				return bmp;
 			}
 		} catch (final IOException e) {
 			if (loggerEnable) {
 				Log.e(TAG,
 						"Connection error while downloading photo"
 								+ e.getMessage());
 			}
 		} catch (final Exception e) {
 			if (loggerEnable) {
 				Log.e(TAG, "General error downloading photo" + e.getMessage());
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * An InputStream that skips the exact number of bytes provided, unless it
 	 * reaches EOF.
 	 */
 
 	public void stopThread() {
 		photoLoaderThread.interrupt();
 	}
 
 	class PhotosLoader extends Thread {
 
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					// thread waits until there are any images to load in the
 					// queue
 					if (photosQueue.photosToLoad.size() == 0) {
 						synchronized (photosQueue.photosToLoad) {
 							photosQueue.photosToLoad.wait();
 						}
 					}
 					if (photosQueue.photosToLoad.size() != 0) {
 						PhotoToLoad photoToLoad;
 						synchronized (photosQueue.photosToLoad) {
 							photoToLoad = photosQueue.photosToLoad.pop();
 						}
 						final Bitmap bmp = downloadBitmap(photoToLoad.url,
 								photoToLoad.photoSizeInPixels);
 						if (bmp != null) {
 							memoryCache.put(photoToLoad.url, bmp);
 						}
 						final String tag = imageViews
 								.get(photoToLoad.imageView);
 						if (tag != null && tag.equals(photoToLoad.url)) {
 							final BitmapDisplayer bd = new BitmapDisplayer(bmp,
 									photoToLoad.imageView,
 									photoToLoad.listener, photoToLoad.url);
 							try {
 								final Activity a = (Activity) photoToLoad.imageView
 										.getContext();
 								a.runOnUiThread(bd);
 							} catch (final Exception e) {
 								if (loggerEnable) {
 									Log.w(TAG,
 											"Init the Adapter with an Activity context, NOT application!!!");
 								}
 							}
 						}
 					}
 					if (Thread.interrupted()) {
 						break;
 					}
 				}
 			} catch (final InterruptedException e) {
 				// allow thread to exit
 			}
 		}
 	}
 
 	private final PhotosLoader photoLoaderThread = new PhotosLoader();
 
 	// Used to display bitmap in the UI thread
 	class BitmapDisplayer implements Runnable {
 
 		private final Bitmap bitmap;
 		private final ImageView imageView;
 		private final ImageLoaderListener listener;
 		private final String imageUrl;
 
 		public BitmapDisplayer(final Bitmap b, final ImageView i,
 				ImageLoaderListener imageListener, String url) {
 			bitmap = b;
 			imageView = i;
 			listener = imageListener;
 			imageUrl = url;
 		}
 
 		@Override
 		public void run() {
 			if (bitmap != null) {
 				imageView.setImageBitmap(bitmap);
 				if (listener != null) {
 					listener.onImageLoadingComplete(imageUrl, bitmap);
 				}
 			} else {
 				imageView.setImageResource(errorDrawableId);
 			}
 		}
 	}
 
 	public void clearMemoryCache() {
 		if (memoryCache != null) {
 			memoryCache.clear();
 		}
 	}
 
 	public void clearSDCache() {
 		if (shouldCacheOnSD) {
 			fileCache.clearCache();
 		}
 	}
 
 	public String getMemoryLog() {
 		if (memoryCache == null) {
 			return "No memory cache initialized!";
 		}
 		return memoryCache.getObjectCounters() + " ImageViews in memory: "
 				+ imageViews.size();
 	}
 
 }
