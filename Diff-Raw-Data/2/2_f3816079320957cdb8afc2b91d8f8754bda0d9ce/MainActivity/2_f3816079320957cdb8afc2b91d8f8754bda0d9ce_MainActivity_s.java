 package cn.fortune.apps.sleepseconds;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.NumberPicker;
 
 
 import cn.fortune.apps.sleepseconds.helper.SharedPreferencesHelper;
 import cn.fortune.apps.sleepseconds.vibrate.Vibrate;
 
 public class MainActivity extends Activity {
     private Button btnStartSleep;
     private EditText txtConfigSleepMinutes;
     private static SharedPreferences sharedPreferences;
     private static int X;
     private static int Y;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         initVarForOther();
 
         initUI();
 
         listener();
 
         renderUi();
     }
 
 
     private void initVarForOther() {
 
         (new Vibrate()).init(this);
 
         sharedPreferences = getSharedPreferences("sleep_seconds_conf", MODE_PRIVATE);
         (new SharedPreferencesHelper()).init(this);
 
         Display display = getWindowManager().getDefaultDisplay();
         X = display.getWidth();
         Y = display.getHeight();
     }
 
 
     private void initUI() {
 
         btnStartSleep = (Button) findViewById(R.id.startSleep);
         txtConfigSleepMinutes = (EditText) findViewById(R.id.sleepMinutes);
     }
 
 
     private void listener() {
 
         btnStartSleep.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 recordSleepMinute();
 
                 if (btnStartSleep.getText().toString().equalsIgnoreCase("开始")) {
                     btnStartSleep.setText("结束");
                     (new Vibrate()).planVibrate();
                 } else {
                     (new Vibrate()).cancel();
                     btnStartSleep.setText("开始");
                 }
             }
         });
 
         txtConfigSleepMinutes.setInputType(InputType.TYPE_NULL);
 
         txtConfigSleepMinutes.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
 
 
                 NumberPicker numberPicker = new NumberPicker(MainActivity.this);
                 numberPicker.setMaxValue(60);
                 numberPicker.setMinValue(5);
 
                 numberPicker.setValue(Integer.parseInt(txtConfigSleepMinutes.getText().toString()));
 
                 numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                     @Override
                     public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                         txtConfigSleepMinutes.setText(String.valueOf(newVal));
 
                     }
                 });
                 numberPicker.setWrapSelectorWheel(true);
 
                 ((EditText) numberPicker.getChildAt(1)).setInputType(InputType.TYPE_NULL);
 
                 AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("休息分钟")
                         .setView(numberPicker).setPositiveButton("ok", null).create();
                 alertDialog.show();
 
             }
         });
 
 
     }
 
 
     private void renderUi() {
         int tipTime = SharedPreferencesHelper.getInstance().getTipTime();
         txtConfigSleepMinutes.setText(String.valueOf(tipTime));
 
         Log.d("-plan", Vibrate.getVibratePlanState() ? "true" : "false");
         if (Vibrate.getVibratePlanState()) {
             btnStartSleep.setText("结束");
         }
     }
 
     public static SharedPreferences getSharedPreferences() {
         return sharedPreferences;
     }
 
     private void recordSleepMinute() {
         SharedPreferences.Editor editor = sharedPreferences.edit();
 //        editor.putInt("sleepMinutes", Integer.parseInt(txtConfigSleepMinutes.getText().toString()));
         editor.putInt("sleepMinutes", 1);
         editor.commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         menu.add(Menu.NONE, Menu.FIRST + 1, 1, "退出"); //setIcon
         menu.add(Menu.NONE, Menu.FIRST + 2, 2, "设置");
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case Menu.FIRST + 1:
                 quit();
                 break;
             case Menu.FIRST + 2:
                 goToSettingActivity();
                 break;
             default:
                 break;
 
 
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     private void quit() {
         AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                 .setTitle("确定要退出吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         finish();
                         System.exit(0);
                     }
                 }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         return;
                     }
                 }).create();
         alertDialog.show();
 
     }
 
     private void goToSettingActivity() {
         Intent intent = new Intent();
         intent.setClass(MainActivity.this, SettingActivity.class);
         startActivity(intent);
         finish();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == event.KEYCODE_BACK) {
             quit();
         }
         return false;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             if (event.getX() < X / 2) {
                 increase();
             } else {
                 decrease();
             }
         }
 
         return super.onTouchEvent(event);
     }
 
     private void increase() {
         int minute = Integer.parseInt(txtConfigSleepMinutes.getText().toString());
        txtConfigSleepMinutes.setText(String.valueOf(minute - 1));
 
     }
 
     private void decrease() {
         int minute = Integer.parseInt(txtConfigSleepMinutes.getText().toString());
         txtConfigSleepMinutes.setText(String.valueOf(minute + 1));
     }
 }
