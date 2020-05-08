 package ru.tulupov.nsuconnect;
 
 import android.app.Application;
 import android.graphics.Bitmap;
 import ru.tulupov.nsuconnect.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.ExceptionReporter;
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Logger;
 import com.google.analytics.tracking.android.Tracker;
 import com.rampo.updatechecker.UpdateChecker;
 
 import org.codechimp.apprater.AppRater;
 
 import java.sql.SQLException;
 
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.helper.VibrateHelper;
 import ru.tulupov.nsuconnect.images.ImageCacheManager;
 import ru.tulupov.nsuconnect.request.RequestManager;
 import ru.tulupov.nsuconnect.helper.SoundHelper;
 
 
 public class NsuConnectApplication extends Application {
     private static final String TAG = NsuConnectApplication.class.getSimpleName();
     private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 100;
     private static int MEMORY_IMAGECACHE_SIZE = 1024 * 1024 * 10;
     private static Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
     private static int DISK_IMAGECACHE_QUALITY = 100;  //PNG is lossless so quality is ignored but must be provided
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         System.setProperty("http.keepAlive", "false");
 
         Thread.UncaughtExceptionHandler handler = new ExceptionReporter(
                 EasyTracker.getInstance(this),
                 GAServiceManager.getInstance(),
                 Thread.getDefaultUncaughtExceptionHandler(),
                 this);
 
         Thread.setDefaultUncaughtExceptionHandler(handler);
 
 //        if (BuildConfig.DEBUG) {
 //            GoogleAnalytics.getInstance(this)
 //                    .getLogger()
 //                    .setLogLevel(Logger.LogLevel.VERBOSE);
 //        }
 
         SoundHelper.init(this);
         VibrateHelper.init(this);
 
         HelperFactory.setHelper(getApplicationContext());
         init();
 
 
         try {
             HelperFactory.getHelper().getChatDao().deactivateAllChats();
         } catch (SQLException e) {
             Log.e(TAG, "Cannot deactivate chats", e);
         }
 
 
 //        GoogleAnalytics.getInstance(this).setDryRun(BuildConfig.DEBUG);
 
         try {
 //            if (!BuildConfig.DEBUG)
                 BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_api_key));
        } catch (Throwable e) {
         }
     }
 
 
     /**
      * Intialize the request manager and the image cache
      */
     private void init() {
         RequestManager.init(this);
         createImageCache();
     }
 
     /**
      * Create the image cache. Uses Memory Cache by default. Change to Disk for a Disk based LRU implementation.
      */
     private void createImageCache() {
         ImageCacheManager.getInstance().init(this,
                 this.getPackageCodePath()
                 , DISK_IMAGECACHE_SIZE
                 , MEMORY_IMAGECACHE_SIZE
                 , DISK_IMAGECACHE_COMPRESS_FORMAT
                 , DISK_IMAGECACHE_QUALITY
                 , ImageCacheManager.CacheType.BOTH);
     }
 }
