 package com.yechengxiao.FlashLight;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import com.yechengxiao.FlashLight.settings.SettingsActivity;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class MainActivity extends Activity {
     private boolean isOpen;
     private boolean settings_light_or_not;
     private static final String TAG = "MAIN_ACTIVITY_TAG";
     private Timer timer;
     private Camera camera = Camera.open();
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.i(this.TAG, "onCreate method");
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // 设置默认值
         PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
         // 获取默认值
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         settings_light_or_not = sharedPreferences.getBoolean("pref_key_check_light_or_not", true);
 
         if (settings_light_or_not) { // 设置中的选项
             turnOn(R.drawable.light_on_icon);
         } else {
             resetLightToOff();
         }
     }
 
     /**
      * action bar
      *
      * @param menu
      * @return
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_activity_actions, menu); // 加载action bar
         return super.onCreateOptionsMenu(menu);
     }
 
     /**
      * @param item
      * @return
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) { // action bar上的按钮事件
             case R.id.action_setting:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.action_about:
                 // TODO 关于的内容
                 return true;
             case R.id.action_sos:
                 turnSOS();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void onStop() {
         Log.i(this.TAG, "onStop method");
 
         super.onStop();
         TimerTask task = new TimerTask() { // 暂停一段时间后，关灯
             @Override
             public void run() {
                 turnOff(R.drawable.light_off_icon);
             }
         };
         timer = new Timer();
         timer.schedule(task, 30000);
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
 
         if (null == this.camera) {
             camera = Camera.open();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         final ImageView imageView = (ImageView) findViewById(R.id.light_icon);
 
         imageView.setOnClickListener(new ImageView.OnClickListener() { // 主屏幕开关按钮事件监听
             public void onClick(View view) {
                 undoSOS();
 
                 if (isOpen) {
                     turnOff(R.drawable.light_off_icon);
                 } else {
                     turnOn(R.drawable.light_on_icon);
                 }
             }
         });
     }
 
     /**
      * 释放资源
      */
     @Override
     protected void onDestroy() {
         Log.i(this.TAG, "onDestroy method");
 
         super.onDestroy();
         releaseResource();
     }
 
     /**
      * 开启
      */
     void turnOn(int image) {
         try {
             // Camera.Parameters.FLASH_MODE_TORCH
             camera.setParameters(getCamParamsObj(Camera.Parameters.FLASH_MODE_TORCH));
             setImage(image);
             isOpen = true;
         } catch (Exception e) {
             Log.e(this.TAG, "turnOn error");
         }
     }
 
     /**
      * 关闭
      */
     void turnOff(int image) {
         try {
             camera.setParameters(getCamParamsObj(Camera.Parameters.FLASH_MODE_OFF));
             setImage(image);
             isOpen = false;
         } catch (Exception e) {
             Log.e(this.TAG, "turnOff error");
         }
     }
 
     /**
      * 开关SOS
      */
     void turnSOS() {
         try {
             if (undoSOS()) {
                 return;
             }
 
             class ToggleLightClass {  // 实现开关灯
                 boolean toggleBoolean = isOpen;
 
                 public void toggleLightInner() {
                     if (!toggleBoolean) {
                         turnOn(R.drawable.light_on_icon);
                         toggleBoolean = true;
                     } else {
                         turnOff(R.drawable.light_on_icon);
                         toggleBoolean = false;
                     }
                 }
             }
 
             final ToggleLightClass tc = new ToggleLightClass();
 
             TimerTask toggleLightTimerTask = new TimerTask() {
                 @Override
                 public void run() {
		     /*
		      http://stackoverflow.com/questions/18656813/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
		     */
			runOnUiThread(new Runnable() { // 在主线程上更新UI
			@Override
			public void run() {
				tc.toggleLightInner();
			}
	           });
                 }
             };
             SOSTimer = new Timer();
             SOSTimer.schedule(toggleLightTimerTask, 100, 200);
         } catch (Exception e) {
             Log.e(this.TAG, "turnSOS error");
         }
     }
 
     /**
      * 获取所需参数设置
      *
      * @param parameters
      * @return
      */
     Camera.Parameters getCamParamsObj(String parameters) {
         try {
             Camera.Parameters camParams = this.camera.getParameters();
             camParams.setFlashMode(parameters);
             return camParams;
         } catch (Exception e) {
             Log.e(this.TAG, "getCamParamsObj error");
         }
         return null;
     }
 
     /**
      * 设置图片
      *
      * @param drawable_id
      */
     void setImage(int drawable_id) {
         final ImageView imageView = (ImageView) findViewById(R.id.light_icon);
         Drawable drawable = getResources().getDrawable(drawable_id);   // 获取图片;
         imageView.setImageDrawable(drawable);
     }
 
     /**
      * 执行释放资源
      */
     void releaseResource() {
         if (null != camera) {
             camera.release();
         }
         if (null != timer) {
             timer.cancel();
             timer = null;
         }
         if (null != SOSTimer) {
             SOSTimer.cancel();
             SOSTimer = null;
         }
 
         Log.i(this.TAG, "release");
     }
 
     /**
      * 重置灯的状态为关闭
      */
     void resetLightToOff() {
         turnOff(R.drawable.light_off_icon);
         isOpen = false;
     }
 
     /**
      * 取消SOS
      */
     boolean undoSOS() {
         if (null != SOSTimer) {
             resetLightToOff();
             SOSTimer.cancel();
             SOSTimer = null;
             return true;
         }
         return false;
     }
 }
 
