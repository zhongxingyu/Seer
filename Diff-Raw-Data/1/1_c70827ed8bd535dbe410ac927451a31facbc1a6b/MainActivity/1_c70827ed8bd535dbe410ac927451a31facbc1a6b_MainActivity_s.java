 package com.gatech.spark.activity;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.util.Log;
 
 import com.gatech.spark.R;
 import com.gatech.spark.database.SqliteHelper;
 import com.gatech.spark.fragment.SparkMapFragment;
 import com.gatech.spark.fragment.SubscriptionsFragment;
 import com.gatech.spark.fragment.WhatsHotFragment;
 import com.gatech.spark.helper.CustomViewPager;
 import com.google.android.gms.maps.SupportMapFragment;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener
 {
 
 	private static final String TAG = "spark.MainActivity";
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
      * derivative, which will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     AppSectionsPagerAdapter mAppSectionsPagerAdapter;
 
     /**
      * The {@link android.support.v4.view.ViewPager} that will display the three primary sections of the app, one at a
      * time.
      */
     CustomViewPager mViewPager;
 
     private SqliteHelper dbHelper;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         dbHelper = SqliteHelper.getDbHelper( getApplicationContext() );
 
         // Create the adapter that will return a fragment for each of the three primary sections
         // of the app.
         mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
 
         // Specify that the Home/Up button should not be enabled, since there is no hierarchical
         // parent.
         actionBar.setHomeButtonEnabled(false);
         actionBar.setDisplayShowHomeEnabled(false);
         actionBar.setDisplayShowTitleEnabled(false);
 
         // Specify that we will be displaying tabs in the action bar.
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager, attaching the adapter and setting up a listener for when the
         // user swipes between sections.
         mViewPager = (CustomViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mAppSectionsPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When swiping between different app sections, select the corresponding tab.
                 // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                 // Tab.
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
 
         actionBar.setSelectedNavigationItem(1);
         mViewPager.setOffscreenPageLimit(2);
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
 
 
 		public AppSectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
             Fragment fragment;
             switch (i) {
                 case 0:
                 	Log.d(TAG, "Creating new WhatsHotFragment");
                     fragment = new WhatsHotFragment();
                     return fragment;
                 case 1:
                 	Log.d(TAG, "Creating new SparkMapFragment");
                     fragment = new SparkMapFragment();
                     return fragment;
                 case 2:
                 	Log.d(TAG, "Creating new SubscriptionsFragment");
                     fragment = new SubscriptionsFragment();
                     return fragment;
                 default:
                     fragment = new SupportMapFragment();
                     return fragment;
             }
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             if(position == 0)
             {
                 return "What's Hot";
             }
             else if (position == 1)
             {
                   return "My Map";
             }
             else if (position == 2 )
             {
                 return "Subscriptions";
             }
             else {
                 return "tab";
             }
 
         }
     }
 
 }
