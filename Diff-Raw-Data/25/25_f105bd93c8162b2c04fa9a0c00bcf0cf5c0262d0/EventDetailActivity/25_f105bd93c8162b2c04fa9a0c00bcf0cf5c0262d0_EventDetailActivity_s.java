 package com.hasgeek.funnel.activity;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 
 import com.hasgeek.funnel.R;
 import com.hasgeek.funnel.fragment.DaysListFragment;
 import com.hasgeek.funnel.fragment.EventVenueFragment;
 import com.hasgeek.funnel.fragment.ExploreEventFragment;
 
 
 public class EventDetailActivity extends FragmentActivity implements ActionBar.TabListener {
 
     private ViewPager mViewPager;
     private EventSectionsPagerAdapter mPagerAdapter;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_eventdetail);
 
         mPagerAdapter = new EventSectionsPagerAdapter(getSupportFragmentManager());
 
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 getActionBar().setSelectedNavigationItem(position);
             }
         });
 
         ActionBar ab = getActionBar();
         ab.setDisplayHomeAsUpEnabled(true);
         ab.setTitle(getIntent().getStringExtra("name"));
         ab.setSubtitle(getIntent().getStringExtra("dateString"));
 
         ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         ab.addTab(ab.newTab().setText(R.string.schedule).setTabListener(this));
         ab.addTab(ab.newTab().setText(R.string.explore).setTabListener(this));
         ab.addTab(ab.newTab().setText(R.string.venue).setTabListener(this));
     }
 
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
 
     /**
      * Adapter for the schedule and explore tabs on the event detail page
      */
     private class EventSectionsPagerAdapter extends FragmentPagerAdapter {
 
         public EventSectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
             super(fm);
         }
 
 
         @Override
         public int getCount() {
             return 3;
         }
 
 
         @Override
         public Fragment getItem(int i) {
             switch (i) {
                 case 0:
                     return new DaysListFragment();
                 case 1:
                     return new ExploreEventFragment();
                 case 2:
                     return new EventVenueFragment();
                 default:
                     throw new RuntimeException("We don't have so many fragments!");
             }
         }
     }
 }
