 package fi.testbed2.app;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.Application;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.support.v4.util.LruCache;
 import fi.testbed2.data.TestbedParsedPage;
 
 public class MainApplication extends Application {
 
 	public static final String LOG_IDENTIFIER = "TestbedViewer2";
 
 	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
     public static final int RESULT_OK = Activity.RESULT_OK;
     public static final int RESULT_REFRESH = 10;
 
     public static final String PREF_ANIM_FRAME_DELAY = "PREF_ANIM_FRAME_DELAY";
     public static final String PREF_ANIM_AUTOSTART = "PREF_ANIM_AUTOSTART";
     public static final String PREF_MAP_TYPE = "PREF_MAP_TYPE";
     public static final String PREF_MAP_TIME_STEP = "PREF_MAP_TIME_STEP";
     public static final String PREF_MAP_NUMBER_OF_IMAGES = "PREF_MAP_NUMBER_OF_IMAGES";
 
     private static TestbedParsedPage testbedParsedPage;
     private static LruCache<String, Bitmap> imageCache;
 
     public static TestbedParsedPage getTestbedParsedPage() {
         return testbedParsedPage;
     }
 
     public static void setTestbedParsedPage(TestbedParsedPage testbedParsedPage) {
         MainApplication.testbedParsedPage = testbedParsedPage;
     }
 
     @Override
     public void onCreate() {
         initImageCache();
     }
 
     private void initImageCache() {
 
         /*
          * Get memory class of this device, exceeding this amount
          * will throw an OutOfMemory exception.
          */
         final int memClass = ((ActivityManager) this.getSystemService(
                 Context.ACTIVITY_SERVICE)).getMemoryClass();
 
         final int cacheSizeInBytes = 1024 * 1024 * memClass;
 
         imageCache = new LruCache<String, Bitmap>(cacheSizeInBytes) {
             @Override
             protected int sizeOf(String key, Bitmap bitmap) {
                 return bitmap.getRowBytes()*bitmap.getHeight();
             }
             @Override
             protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                 if (evicted) {
                     oldValue.recycle();
                 }
             }
         };
 
     }
 
     public static void clearData() {
         testbedParsedPage = null;
         clearImageCache();
         System.gc();
     }
 
     private static void clearImageCache() {
         imageCache.evictAll();
     }
 
     public static void addBitmapToImageCache(String key, Bitmap bitmap) {
         if (getBitmapFromImageCache(key) == null) {
             imageCache.put(key, bitmap);
         }
     }
 
     public static Bitmap getBitmapFromImageCache(String key) {
         return imageCache.get(key);
     }
 }
