 package com.elclcd.orientation;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 
 /**
  * @Description 
  * 开启悬浮窗口(关闭设置,需要系统签名)
  *
  * @author elc_simayi
  * @date 2013-12-11 下午4:18:33 
  */
 public class MainActivity extends Activity {
     public static String TAG = "com.elclcd.orientation";
     public static Button btfloat;
     IntentFilter  i;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         btfloat = (Button) findViewById(R.id.bt_float);
         btfloat.setOnClickListener(clickListener);
         
         SharedPreferences settings = getSharedPreferences(TAG, 0);
         if(settings.getBoolean("float", false))
             btfloat.setText(R.string.bt_float_off);
         else{
             btfloat.setText(R.string.bt_float_on);
         }
     }
 
     OnClickListener clickListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             if (v == btfloat){
                 Intent intent = new Intent(MainActivity.this, OrientationService.class);
                 intent.putExtra(OrientationService.FLOAT_SWITCH, true);
                 startService(intent);
                 return;
             }
         }
     };
 }
