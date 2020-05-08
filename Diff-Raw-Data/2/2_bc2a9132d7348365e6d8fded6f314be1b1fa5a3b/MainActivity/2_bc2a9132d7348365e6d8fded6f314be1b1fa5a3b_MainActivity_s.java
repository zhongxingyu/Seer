 package com.tech_tec.android.simplecalendar.activity.main;
 
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.support.v7.app.ActionBarActivity;
 
 import com.tech_tec.android.simplecalendar.R;
 
 public class MainActivity extends ActionBarActivity {
     
     private ViewPager mViewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         mViewPager = (ViewPager)findViewById(R.id.pager);
         mViewPager.setAdapter(new CalendarFragmentPagerAdapter(getSupportFragmentManager()));
     }
     
     protected void onResume() {
         super.onResume();
         mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() / 2);
     }
 
 }
