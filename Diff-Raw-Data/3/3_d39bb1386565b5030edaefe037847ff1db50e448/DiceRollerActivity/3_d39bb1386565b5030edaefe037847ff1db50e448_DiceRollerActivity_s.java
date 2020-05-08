 package com.group5.diceroller;
 
 import java.util.List;
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.util.Log;
 
 public class DiceRollerActivity extends FragmentActivity
     implements ActionBar.TabListener, OnDiceRolledListener,
     OnSelectionChangedListener {
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
      * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
      * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     SectionsPagerAdapter mSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
 
     DiceRollerState state;
     SetChooserFragment chooser;
     CentralFragment central;
     StatisticsFragment statistics;
 
     @Override
     /**
      * Callback for creating the activity. It Creates the component fragments,
      * builds the view pager and its adapter, registers the listeners for
      * changing the current view with action bar tabs, and loads the dice sets
      * from the database.
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Log.i("DiceRollerActivity", "creating...");
 
         chooser    = new SetChooserFragment();
         central    = new CentralFragment();
         statistics = new StatisticsFragment();
 
         // Create the adapter that will return a fragment for each of the three primary sections
         // of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding tab.
         // We can also use ActionBar.Tab#select() to do this if we have a reference to the
         // Tab.
         mViewPager.setOnPageChangeListener(new PageChangeListener());
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(actionBar.newTab()
                             .setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
 
         // Move the pager to the center item
         mViewPager.setCurrentItem(1, false);
         state = DiceRollerState.getState();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     public void onDiceRolled() {
         // play possible animations/sound here
         state.rollHistory().add(0, new SetSelection(state.activeSelection()));
         if (state.rollHistory().size() > 20)
             state.rollHistory().remove(20);
 
        state.activeSelection().roll();
         statistics.update();
         mViewPager.setCurrentItem(2);
     }
 
     public void onSelectionChanged() {
         central.updateSelectionText();
     }
 
 
     class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
         ActionBar actionbar;
         public PageChangeListener() {
             actionbar = getActionBar();
         }
 
         @Override
         public void onPageSelected(int position) {
             actionbar.setSelectedNavigationItem(position);
         }
     }
 
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
             switch (i)
             {
                 case 0: return chooser;
                 case 1: return central;
                 case 2: return statistics;
             }
             return null;
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case 0: return getString(R.string.chooser_title).toUpperCase();
                 case 1: return getString(R.string.central_title).toUpperCase();
                 case 2: return getString(R.string.statistics_title).toUpperCase();
             }
             return null;
         }
     }
 }
