 package com.elclcd.orientation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.ActivityManager;
import android.app.AlertDialog;
 import android.app.Service;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Handler;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 
 /**
  * @Description 屏幕传感服务
  * 
  * @author elc_simayi
  * @date 2013-12-11 上午10:57:49
  */
 public class OrientationService extends Service {
     public final String TAG = "OrientationService";
     public static final String FLAG_VALUE = "flag_value";
     public static final String FLOAT_SWITCH = "float_switch";
     public static final int RESTFLAG = -9999;
     private WindowManager manager;
 
     private Handler handler = new Handler();
 
     private Timer timer;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         Log.d(TAG, "====onCreate====");
         super.onCreate();
         manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
         if (timer == null) {
             timer = new Timer();
             timer.scheduleAtFixedRate(new RefreshTask(), 0, 2000);
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         boolean float_switch = intent.getBooleanExtra(FLOAT_SWITCH, false);
         if (float_switch) {
             boolean b;
             if(MyWindowManager.isWindowShowing()){
                 MyWindowManager.removeSmallWindow(getApplicationContext());
                 MyWindowManager.removeBigWindow(getApplicationContext());
                 MainActivity.btfloat.setText(R.string.bt_float_on);
                 b = false;
             }else{
                 MyWindowManager.createSmallWindow(getApplicationContext());
                 MainActivity.btfloat.setText(R.string.bt_float_off);
                 b = true;
             }
             SharedPreferences settings = getSharedPreferences(MainActivity.TAG, 0);   
             SharedPreferences.Editor editor = settings.edit();   
             editor.putBoolean("float", b);
             editor.commit();
         }
         
         int rotation = intent.getIntExtra(FLAG_VALUE, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
         
         if (rotation == RESTFLAG) {
             stopSelf();
         }
 
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         timer.cancel();
         timer = null;
     }
 
     class RefreshTask extends TimerTask {
 
         @Override
         public void run() {
             if (MyWindowManager.isExistSmallWindow()) {
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                         if(MyWindowManager.getBigWindowVisible() != View.VISIBLE ){
                             MyWindowManager.visibleSmallWindow();
                         }
                     }
                 });
             }
             else if (MyWindowManager.isWindowShowing()) {
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                         if(MyWindowManager.isExistSmallWindow()){
                             MyWindowManager.invisibleSmallWindow();
                         }
                         if(MyWindowManager.isExistBigWindow()){
                             MyWindowManager.invisibleBigWindow();
                         }
                     }
                 });
             }
         }
     }
 
     private boolean isHome() {
         ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
         return getHomes().contains(rti.get(0).topActivity.getPackageName());
     }
 
     private List<String> getHomes() {
         List<String> names = new ArrayList<String>();
         PackageManager packageManager = this.getPackageManager();
         Intent intent = new Intent(Intent.ACTION_MAIN);
         intent.addCategory(Intent.CATEGORY_HOME);
         List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
         for (ResolveInfo ri : resolveInfo) {
             names.add(ri.activityInfo.packageName);
         }
         return names;
     }
 
 }
