 /*
  * 功能主界面，包括三个按钮：规划、黑名单、设置。
  * By Adoni
  */
 package com.example.ui;
 
 import java.util.ArrayList;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.PopupWindow;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.example.skylark.Countdown;
 import com.example.skylark.R;
 import com.example.skylark.R.id;
 import com.example.skylark.R.layout;
 import com.example.skylark.R.menu;
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
 
 public class MainActivity extends BaseSampleActivity {
 	TimePicker tp;
 	Spinner sns_sp,bl_sp;
 	Button startButton;
 	String snsName="";
 	String blName="";
 	PopupWindow pop;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if(serviceIsAcitved("MonitorService"))
         {
         	SharedPreferences setting=getSharedPreferences("Setting", 0);
         	Countdown.Countdown(MainActivity.this,setting.getInt("Hour", 0), setting.getInt("Min", 0));
         	//return;
         }
         SharedPreferences setting=getSharedPreferences("Setting", 0);
         if(setting.getBoolean("isTheFirstTimeUsed", true))
         {
         	Intent intent=new Intent();
         	intent.setClass(MainActivity.this, HelpPager.class);
         	MainActivity.this.startActivity(intent);
         	Editor editor=setting.edit();
         	editor.putBoolean("isTheFirstTimeUsed", false);
         	editor.commit();
         	//return;
         }
         setContentView(R.layout.simple_titles);
         mAdapter = new TestTitleFragmentAdapter(getSupportFragmentManager(),MainActivity.this,pop);
         mPager = (ViewPager)findViewById(R.id.pager);
         mPager.setOnPageChangeListener(new OnPageChangeListener() {
 			
 			public void onPageSelected(int arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// TODO Auto-generated method stub
 				TitlePageIndicator indicator=(TitlePageIndicator)findViewById(R.id.indicator);
 				indicator.setBackground(getResources().getDrawable(R.drawable.icon));
 			}
 			
 			public void onPageScrollStateChanged(int arg0) {
 				// TODO Auto-generated method stub
 				TitlePageIndicator indicator=(TitlePageIndicator)findViewById(R.id.indicator);
 				indicator.setBackground(getResources().getDrawable(R.drawable.icon));
 				Toast.makeText(MainActivity.this, "asdf", Toast.LENGTH_SHORT).show();
 			}
 		});
         mPager.setAdapter(mAdapter);
         mPager.setCurrentItem(getSharedPreferences("Setting", 0).getInt("CurrentItem", 1));
         mPager.setOnTouchListener(new OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if(pop!=null && pop.isShowing())
 				{
 					pop.dismiss();
 					Toast.makeText(MainActivity.this, "fafa", Toast.LENGTH_LONG).show();
 				}
 				return false;
 			}
 		});
         TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
         indicator.setViewPager(mPager);
         indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
         mIndicator = indicator;
         mIndicator.setOnPageChangeListener(new OnPageChangeListener() {
 			
 			public void onPageSelected(int arg0) {
 				// TODO Auto-generated method stub
 				Log.v("myscroll","sdf");
 				//TitlePageIndicator indicator=(TitlePageIndicator)findViewById(R.id.indicator);
 				//indicator.setBackground(getResources().getDrawable(R.drawable.icon));
 			}
 			
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// TODO Auto-generated method stub
 			//	TitlePageIndicator indicator=(TitlePageIndicator)findViewById(R.id.indicator);
 				//indicator.setBackground(getResources().getDrawable(R.drawable.icon));
 				//Toast.makeText(MainActivity.this, "asdf", Toast.LENGTH_SHORT).show();
 				
 			}
 			
 			public void onPageScrollStateChanged(int arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
     }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		//Toast.makeText(MainActivity.this, "d", Toast.LENGTH_LONG).show();
 		Intent intent=new Intent("com.example.skylark.monitorservice");
 		stopService(intent);
 		
 		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
 		finish();
 		return super.onOptionsItemSelected(item);
 	}
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO Auto-generated method stub
     	Toast.makeText(this,"sf" ,Toast.LENGTH_LONG).show();
 		return super.onTouchEvent(event);
 	}
 	
 	public boolean serviceIsAcitved(String serviceName)
 	{
 		ActivityManager myManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
 		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager.getRunningServices(30);
 		for(int i = 0 ; i<runningService.size();i++)
 		{
 			//Log.v("myservice", serviceName);
 			String name=runningService.get(i).service.getClassName().toString();
 			Log.v("myservice", name);
 			if(name.contains(serviceName))
 			{
 				Log.v("myservice", name);
 				return true;
 			}
 		}
 		return false;
 	}
 }
