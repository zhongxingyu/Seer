 package com.elclcd.orientation;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.app.ActivityManager;
 import android.app.Instrumentation;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 public class FloatWindowBigView extends LinearLayout {
 
     /**
      * 记录大悬浮窗的宽度
      */
     public static int viewWidth;
 
     /**
      * 记录大悬浮窗的高度
      */
     public static int viewHeight;
 
     public FloatWindowBigView(final Context context) {
         super(context);
         LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
         View view = findViewById(R.id.big_window_layout);
         viewWidth = view.getLayoutParams().width;
         viewHeight = view.getLayoutParams().height;
         Button opens = (Button) findViewById(R.id.open_setting);
         Button closes = (Button) findViewById(R.id.close_setting);
         Button closem = (Button) findViewById(R.id.close_myself);
         Button back = (Button) findViewById(R.id.back);
         opens.setOnClickListener(onClickListener);
         closes.setOnClickListener(onClickListener);
         closem.setOnClickListener(onClickListener);
         back.setOnClickListener(onClickListener);
     }
 
     private OnClickListener onClickListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             MyWindowManager.invisibleBigWindow();
             MyWindowManager.visibleSmallWindow();
             switch (v.getId()) {
             case R.id.open_setting: // 打开系统设置
                 getContext().startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                 break;
             case R.id.close_setting: // 关系系统设置
 //                PackageManager packageManager = getContext().getPackageManager(); //删除包...
 //                packageManager.setApplicationEnabledSetting("com.android.settings",PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
                 
                 ActivityManager activityMgr = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
 //                activityMgr.forceStopPackage("com.android.settings");
                 try {
                     Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
                     method.invoke(activityMgr, "com.android.settings");
                 } catch (NoSuchMethodException e) {
                     e.printStackTrace();
                 } catch (ClassNotFoundException e) {
                     e.printStackTrace();
                 } catch (IllegalArgumentException e) {
                     e.printStackTrace();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 }
                 break;
             case R.id.close_myself: //关闭悬浮窗
                 Intent intent = new Intent(getContext(), OrientationService.class);
                 intent.putExtra(OrientationService.FLOAT_SWITCH, true);
                 getContext().startService(intent);
                 break;
             case R.id.back:
                 MyWindowManager.invisibleBigWindow();
                 MyWindowManager.visibleSmallWindow();
                 break;
             }
         }
     };
 
     public void onBack() {
         new Thread() {
             public void run() {
                 try {
                     Instrumentation inst = new Instrumentation();
                     inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                 } catch (Exception e) {
                     Log.e("Exception when onBack", e.toString());
                 }
             }
         }.start();
 
     }
 }
