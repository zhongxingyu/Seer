 package org.alabs.nolotiro;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
import android.os.Build;
 import android.os.Environment;
 import android.util.Log;
 
 import org.alabs.nolotiro.exceptions.NolotiroException;
 
 import java.io.File;
 
 
 public class Utils {
 
     private static final String TAG = "NolotiroUtils";
     public static final Integer DEBUG_WOEID = 766273;
     private static String NOLOTIRO_DIR = "Nolotiro";
 
     public static String getNolotiroCacheDir(Context ctx) throws NolotiroException {
         String state = Environment.getExternalStorageState();
         String dir = null;
         File f = ctx.getExternalCacheDir();
 
         if (Environment.MEDIA_MOUNTED.equals(state) && f != null) {
             dir = f.toString();
         } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             f = ctx.getCacheDir();
             if (f != null) {
                 dir = f.toString();
                 Log.i(TAG, "External cache storage is read only. Using internal memory.");
                 Log.i(TAG, "Internal cache storage directory is " + dir);
             } else {
                 Log.w(TAG, "media error: state=" + state);
                 throw new NolotiroException("Error trying to get Nolotiro cache directory");
             }
         }
 
         dir += File.separator + NOLOTIRO_DIR + File.separator;
         return dir;
     }
 
     public static String getNolotiroDir(Context ctx) throws NolotiroException {
         String state = Environment.getExternalStorageState();
         String dir = null;
 
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
         } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             File f = ctx.getFilesDir();
             if (f != null) {
                 dir = f.toString();
                 Log.i(TAG, "External storage is read only. Using internal memory.");
                 Log.i(TAG, "Internal storage directory is " + dir);
             } else {
                 Log.w(TAG, "media error: state=" + state);
                 throw new NolotiroException("Error trying to get Nolotiro directory");
             }
         }
 
         dir += File.separator + NOLOTIRO_DIR + File.separator;
         return dir;
     }
 
 
     // Check for Internet connection
     public static boolean isInternetAvailable(Context ctx)  {
         ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
         if (networkInfo == null || !networkInfo.isConnected()) {
             Log.w(TAG, "No networkinfo");
             return false;
         }
 
         return true;
     }
 
    public static int getContentViewCompat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                android.R.id.content : R.id.action_bar_activity_content;
    }
 }
