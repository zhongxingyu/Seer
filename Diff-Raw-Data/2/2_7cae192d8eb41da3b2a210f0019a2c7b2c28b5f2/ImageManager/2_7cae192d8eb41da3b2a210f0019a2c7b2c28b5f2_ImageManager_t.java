 /*
  *  Copyright (c) 2012 Daniel Huckaby
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.handlerexploit.prime.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.ReentrantLock;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Looper;
 import android.util.Log;
 
 import com.handlerexploit.common.utils.DiskLruCache;
 import com.handlerexploit.common.utils.DiskLruCache.Editor;
 import com.handlerexploit.common.utils.DiskLruCache.Snapshot;
 import com.handlerexploit.common.utils.Hashing;
 import com.handlerexploit.common.utils.LruCache;
 import com.handlerexploit.prime.Configuration;
 import com.handlerexploit.prime.widgets.RemoteImageView;
 
 /**
  * This class is responsible for retrieving and caching all Bitmap images. Using
  * a two tier caching mechanism the images are saved both in memory and on disk
  * for both an efficient and clean user experience.</br>
  * 
  * <div class="special reference"> <b>Development Notes:</b></br> The most
  * fool-proof method of integration is to use
  * {@link RemoteImageView#setImageURL(String)
  * RemoteImageView.setImageURL(String)}. </div>
  * 
  * If you want to receive images asynchronously you can use
  * {@link ImageManager#get(String, OnImageReceivedListener)} or
  * {@link ImageManager#get(Request)}.</br></br>
  * 
  * <pre>
  * final String imageURL = &quot;http://example.com/image.png&quot;;
  * ImageManager imageManager = ImageManager.getInstance(context);
  * imageManager.get(imageURL, new OnImageReceivedListener() {
  * 
  *     &#064;Override
  *     public void onImageReceived(String source, Bitmap bitmap) {
  *         // Do something with the retrieved Bitmap
  *     }
  * });
  * 
  * imageManager.get(new Request() {
  * 
  *     &#064;Override
  *     public String getSource() {
  *         return imageURL;
  *     }
  * 
  *     &#064;Override
  *     public void onImageReceived(String source, Bitmap bitmap) {
  *         // Do something with the retrieved Bitmap
  *     }
  * });
  * </pre>
  * 
  * If you want to retrieve images synchronously you can use
  * {@link ImageManager#get(String)}.</br></br>
  * 
  * <pre>
  * ImageManager imageManager = ImageManager.getInstance(context);
  * String imageURL = &quot;http://example.com/image.png&quot;;
  * Bitmap bitmap = imageManager.get(imageURL);
  * </pre>
  */
 public final class ImageManager {
 
     private static final String TAG = "ImageManager";
 
     private static Object LOCK = new Object();
 
     private static ImageManager sInstance;
 
     private final String mCacheDirectory;
 
     private DiskLruCache mDiskLruCache;
 
     private Bitmap.Config mPreferredConfig = Bitmap.Config.ARGB_8888;
 
     private Handler mHandler = new Handler(Looper.getMainLooper());
 
     private LruCache<String, Bitmap> mLruCache = newConfiguredLruCache();
 
     private ExecutorService mNetworkExecutorService = newConfiguredThreadPool();
 
     private ExecutorService mDiskExecutorService = Executors.newCachedThreadPool(new LowPriorityThreadFactory());
 
     private LruCache<String, ReentrantLock> mLockCache = new LruCache<String, ReentrantLock>(100);
 
     private ImageManager(Context context) {
         mCacheDirectory = getCacheDirectory(context).getAbsolutePath();
         try {
             File directory = new File(mCacheDirectory, "/images/");
             mDiskLruCache = DiskLruCache.open(directory, 1, 1, Configuration.DISK_CACHE_SIZE_KB * 1024);
         } catch (IOException e) {
             Log.e(TAG, e.getMessage());
             throw new RuntimeException(e);
         }
     }
 
     public static synchronized ImageManager getInstance(Context context) {
         if (sInstance == null) {
             sInstance = new ImageManager(context);
         }
         return sInstance;
     }
 
     public synchronized void evictAll() {
         mLruCache.evictAll();
     }
 
     public void setPreferredConfig(Bitmap.Config preferredConfig) {
         mPreferredConfig = preferredConfig;
     }
 
     /**
      * Return the appropriate {@link Bitmap} associated with the provided
      * {@link String}. This is a synchronous call, if you need to asynchronously
      * retrieve an image use
      * {@link ImageManager#get(String, OnImageReceivedListener)} or
      * {@link ImageManager#get(Request)}.
      * 
      * @param source
      *            The URL of a remote image
      */
     public Bitmap get(String source) {
         Bitmap bitmap = getBitmapFromMemory(source, null);
         if (bitmap != null) {
             return bitmap;
         } else {
             String key = getKey(source);
             bitmap = getBitmapFromDisk(key, null);
             if (bitmap == null) {
                 bitmap = getBitmapFromNetwork(key, source, null);
             }
             return bitmap;
         }
     }
 
     /**
      * Return the appropriate {@link Bitmap} associated with the provided
      * {@link OnImageReceivedListener} synchronously or asynchronously depending
      * on the state of the internal cache state. <br>
      * <br>
      * This must only be executed on the main UI Thread.
      * 
      * @param source
      *            The URL of a remote image
      * @param listener
      *            Listener for being notified when image is retrieved, can be
      *            null
      */
     public void get(final String source, final OnImageReceivedListener listener) {
         get(new Request() {
 
             @Override
             public String getSource() {
                 return source;
             }
 
             @Override
             public void onImageReceived(String source, Bitmap bitmap) {
                 if (listener != null) {
                     listener.onImageReceived(source, bitmap);
                 }
             }
         });
     }
 
     /**
      * Return the appropriate {@link Bitmap} associated with the provided
      * {@link Request} synchronously or asynchronously depending on the state of
      * the internal cache state. <br>
      * <br>
      * This must only be executed on the main UI Thread.
      */
     public void get(Request request) {
         if (request == null) {
             throw new NullPointerException("Request cannot be null");
         } else if (request instanceof ExtendedRequest) {
             get((ExtendedRequest) request);
         } else {
             get(new SimpleRequest(request));
         }
     }
 
     private void get(final ExtendedRequest request) {
         final String source = request.getSource();
         if (source != null) {
             final String key = getKey(source);
             Bitmap bitmap = getBitmapFromMemory(key, request);
             if (bitmap != null) {
                 request.onImageReceived(source, bitmap);
             } else {
                 mDiskExecutorService.execute(new Runnable() {
 
                     @Override
                     public void run() {
                         if (verifySourceOverTime(source, request)) {
                             final Bitmap bitmap = getBitmapFromDisk(key, request);
                             if (bitmap != null) {
                                 mHandler.post(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         request.onImageReceived(source, bitmap);
                                     }
                                 });
                             } else {
                                 mNetworkExecutorService.execute(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         final Bitmap bitmap = getBitmapFromNetwork(key, source, request);
                                         mHandler.post(new Runnable() {
 
                                             @Override
                                             public void run() {
                                                 request.onImageReceived(source, bitmap);
                                             }
                                         });
                                     }
                                 });
                             }
                         }
                     }
                 });
             }
         }
     }
 
     private String getMemoryCacheKey(String key, ExtendedRequest request) {
         int height = request != null ? request.getHeight() : 0;
         int width = request != null ? request.getWidth() : 0;
         return key + "|" + height + "|" + width;
     }
 
     private Bitmap getBitmapFromMemory(String key, ExtendedRequest request) {
         return mLruCache.get(getMemoryCacheKey(key, request));
     }
 
     private ReentrantLock getLock(String key) {
         ReentrantLock lock = null;
         synchronized (mLockCache) {
             lock = mLockCache.get(key);
             if (lock == null) {
                 lock = new ReentrantLock();
                 mLockCache.put(key, lock);
             }
         }
         return lock;
     }
 
     private Bitmap getBitmapFromDisk(String key, ExtendedRequest request) {
         Bitmap bitmap = null;
         ReentrantLock lock = getLock(key);
         try {
             lock.lock();
             Snapshot snapShot = null;
             InputStream inputStream = null;
             BitmapFactory.Options bitmapFactoryOptions = null;
             try {
                 snapShot = mDiskLruCache.get(key);
                 if (snapShot != null) {
                     inputStream = snapShot.getInputStream(0);
                     bitmapFactoryOptions = decodeSampleSize(inputStream, request);
                 }
             } catch (IOException e) {
                 Log.w(TAG, e);
             } finally {
                 IOUtils.closeQuietly(snapShot);
                 IOUtils.closeQuietly(inputStream);
             }
 
             if (bitmapFactoryOptions != null) {
                 try {
                     snapShot = mDiskLruCache.get(key);
                     if (snapShot != null) {
                         inputStream = snapShot.getInputStream(0);
                         bitmap = decodeInputStream(inputStream, request, bitmapFactoryOptions);
                     }
                 } catch (IOException e) {
                     Log.w(TAG, e);
                 } finally {
                     IOUtils.closeQuietly(snapShot);
                     IOUtils.closeQuietly(inputStream);
                 }
             }
 
             if (bitmap != null) {
                 int bitmapByteSize = bitmap.getRowBytes() * bitmap.getHeight();
                 if (bitmapByteSize < mLruCache.maxSize()) {
                     mLruCache.put(getMemoryCacheKey(key, request), bitmap);
                 }
             }
         } finally {
             lock.unlock();
         }
         return bitmap;
     }
 
     private Bitmap getBitmapFromNetwork(String key, String source, ExtendedRequest request) {
         if (copyUrlToDiskLruCache(key, source)) {
             return getBitmapFromDisk(key, request);
         } else {
             return null;
         }
     }
 
     private static BitmapFactory.Options decodeSampleSize(InputStream inputStream, ExtendedRequest request) {
         BitmapFactory.Options bitmapFactoryOptions = getBitmapFactoryOptions();
         if (request != null) {
             int height = request.getHeight();
             int width = request.getWidth();
             if (height > 0 && width > 0) {
                 bitmapFactoryOptions.inJustDecodeBounds = true;
 
                 synchronized (LOCK) {
                     BitmapFactory.decodeStream(inputStream, null, bitmapFactoryOptions);
                 }
 
                 int actualWidth = bitmapFactoryOptions.outWidth;
                 int actualHeight = bitmapFactoryOptions.outHeight;
 
                 // Then compute the dimensions we would ideally like to decode to.
                 int desiredWidth = getResizedDimension(width, height, actualWidth, actualHeight);
                 int desiredHeight = getResizedDimension(height, width, actualHeight, actualWidth);
 
                 double wr = (double) actualWidth / desiredWidth;
                 double hr = (double) actualHeight / desiredHeight;
                 double ratio = Math.min(wr, hr);
                 float n = 1.0f;
                 while ((n * 2) <= ratio) {
                     n *= 2;
                 }
 
                 bitmapFactoryOptions.inSampleSize = (int) n;
                 bitmapFactoryOptions.inJustDecodeBounds = false;
             }
         }
         return bitmapFactoryOptions;
     }
 
     /**
      * Scales one side of a rectangle to fit aspect ratio.
      * 
      * @param maxPrimary
      *            Maximum size of the primary dimension (i.e. width for max
      *            width), or zero to maintain aspect ratio with secondary
      *            dimension
      * @param maxSecondary
      *            Maximum size of the secondary dimension, or zero to maintain
      *            aspect ratio with primary dimension
      * @param actualPrimary
      *            Actual size of the primary dimension
      * @param actualSecondary
      *            Actual size of the secondary dimension
      */
     private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
         // If no dominant value at all, just return the actual.
         if (maxPrimary == 0 && maxSecondary == 0) {
             return actualPrimary;
         }
 
         // If primary is unspecified, scale primary to match secondary's scaling
         // ratio.
         if (maxPrimary == 0) {
             double ratio = (double) maxSecondary / (double) actualSecondary;
             return (int) (actualPrimary * ratio);
         }
 
         if (maxSecondary == 0) {
             return maxPrimary;
         }
 
         double ratio = (double) actualSecondary / (double) actualPrimary;
         int resized = maxPrimary;
         if (resized * ratio > maxSecondary) {
             resized = (int) (maxSecondary / ratio);
         }
         return resized;
     }
 
     private static Bitmap decodeInputStream(InputStream inputStream, ExtendedRequest request, BitmapFactory.Options bitmapFactoryOptions) {
         try {
             Bitmap bitmap = null;
             synchronized (LOCK) {
                 bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapFactoryOptions);
             }
             if (request != null) {
                 bitmap = request.onPreProcess(bitmap);
             }
             return bitmap;
         } catch (Throwable t) {
             if (Configuration.DEBUGGING) {
                 Log.w(TAG, t);
             }
         }
         return null;
     }
 
     private static boolean copyUrlToDiskLruCache(String key, String source) {
         ReentrantLock lock = sInstance.getLock(key);
         Editor editor = null;
         InputStream inputStream = null;
         OutputStream outputStream = null;
         try {
             lock.lock();
 
             /*
              * We block here because Editor.edit will return null if another
              * edit is in progress
              */
             while (editor == null) {
                 editor = sInstance.mDiskLruCache.edit(key);
                 Thread.sleep(50);
             }
 
             URLConnection connection = new URL(source).openConnection();
             connection.setConnectTimeout(5000);
             connection.setReadTimeout(10000);
 
             inputStream = connection.getInputStream();
             outputStream = editor.newOutputStream(0);
 
             IOUtils.copy(inputStream, outputStream);
 
             editor.commit();
             return true;
         } catch (IOException e) {
             if (Configuration.DEBUGGING) {
                 Log.w(TAG, e);
             }
         } catch (InterruptedException e) {
             if (Configuration.DEBUGGING) {
                 Log.d(TAG, "Thread was interrupted");
             }
         } finally {
             IOUtils.closeQuietly(inputStream);
             IOUtils.closeQuietly(outputStream);
             if (editor != null) {
                 editor.abortUnlessCommitted();
             }
             lock.unlock();
         }
         return false;
     }
 
     private static boolean verifySourceOverTime(String source, Request request) {
         if (source != null && request != null) {
             try {
                 Thread.sleep(300);
             } catch (InterruptedException e) {
                 if (Configuration.DEBUGGING) {
                     Log.d(TAG, "Thread was interrupted");
                 }
             } finally {
                 if (source.equals(request.getSource())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private static String getKey(String source) {
         if (source == null) {
             return null;
         } else {
             return Hashing.hashString(source);
         }
     }
 
     private static Options getBitmapFactoryOptions() {
         Options options = new Options();
         options.inPurgeable = true;
         options.inInputShareable = true;
         options.inPreferredConfig = sInstance.mPreferredConfig;
         return options;
     }
 
     private static File getCacheDirectory(Context context) {
         File directory;
         switch (Configuration.DOWNLOAD_LOCATION) {
         case EXTERNAL:
             if (Environment.getExternalStorageDirectory() != null && Environment.getExternalStorageDirectory().canWrite()) {
                 directory = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + context.getApplicationContext().getPackageName() + "/cache");
                 directory.mkdirs();
             } else {
                 directory = context.getCacheDir();
             }
             break;
         case INTERNAL:
         default:
            directory = context.getCacheDir();
             break;
         }
         return directory;
     }
 
     /**
      * @hide
      */
     public static ExecutorService newConfiguredThreadPool() {
         int corePoolSize = 0;
         int maximumPoolSize = Configuration.ASYNC_THREAD_COUNT;
         long keepAliveTime = 60L;
         TimeUnit unit = TimeUnit.SECONDS;
         BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
         LowPriorityThreadFactory factory = new LowPriorityThreadFactory();
         RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
 
         return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory, handler);
     }
 
     private static LruCache<String, Bitmap> newConfiguredLruCache() {
         return new LruCache<String, Bitmap>(Configuration.MEM_CACHE_SIZE_KB * 1024) {
 
             @Override
             public int sizeOf(String key, Bitmap value) {
                 return value.getRowBytes() * value.getHeight();
             }
         };
     }
 
     /**
      * Listener for being notified when image is retrieved.
      */
     public static interface OnImageReceivedListener {
 
         /**
          * Notification that an image was retrieved, this is guaranteed to be
          * called on the UI thread.
          */
         public void onImageReceived(String source, Bitmap bitmap);
     }
 
     /**
      * Interface used to retrieve images remotely, used primarily with
      * {@link RemoteImageView} for optimization purposes.
      */
     public static interface Request extends OnImageReceivedListener {
 
         /**
          * Returns remote image URL, can be null.
          */
         public String getSource();
     }
 
     /**
      * Advanced interface for retrieving images in a non-standard way, this is
      * still under heavy development and will most likely change in the future.
      */
     public static interface ExtendedRequest extends Request {
 
         /**
          * Used in the processing of images after they are retrieved from the
          * remote source but before they are cached.
          */
         public Bitmap onPreProcess(Bitmap raw);
 
         /**
          * Used in the resizing of images intelligently.
          */
         public int getHeight();
 
         /**
          * Used in the resizing of images intelligently.
          */
         public int getWidth();
     }
 
     private static class SimpleRequest implements ExtendedRequest {
 
         private Request mRequest;
 
         public SimpleRequest(Request request) {
             mRequest = request;
         }
 
         @Override
         public void onImageReceived(String source, Bitmap bitmap) {
             mRequest.onImageReceived(source, bitmap);
         }
 
         @Override
         public String getSource() {
             return mRequest.getSource();
         }
 
         @Override
         public Bitmap onPreProcess(Bitmap raw) {
             return raw;
         }
 
         @Override
         public int getHeight() {
             return 0;
         }
 
         @Override
         public int getWidth() {
             return 0;
         }
     }
 
     /**
      * Create thread with low priority for use
      * {@link java.util.concurrent.Executor}.
      * 
      * @hide
      */
     public static class LowPriorityThreadFactory implements ThreadFactory {
         private static final AtomicInteger poolNumber = new AtomicInteger(1);
         private final ThreadGroup group;
         private final AtomicInteger threadNumber = new AtomicInteger(1);
         private final String namePrefix;
         private final int priority;
 
         public LowPriorityThreadFactory() {
             SecurityManager s = System.getSecurityManager();
             group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
             namePrefix = "lp-pool-" + poolNumber.getAndIncrement() + "-thread-";
             priority = Thread.MIN_PRIORITY + 1;
         }
 
         public Thread newThread(Runnable r) {
             Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
             if (t.isDaemon())
                 t.setDaemon(false);
             if (t.getPriority() != priority)
                 t.setPriority(priority);
             return t;
         }
     }
 }
