 /*
  * Copyright (C) 2013 The Evervolv Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.evervolv.updater;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.MenuItem;
 import android.view.Window;
 
 import com.evervolv.updater.db.ManifestEntry;
 import com.evervolv.updater.misc.Constants;
 import com.evervolv.updater.tabs.*;
 
 import java.util.ArrayList;
 
 public class Updater extends Activity {
 
     private static final int TAB_POS_NIGHTLIES  = 0;
     private static final int TAB_POS_RELEASES   = 1;
     private static final int TAB_POS_TESTING    = 2;
     private static final int TAB_POS_GAPPS      = 3;
     private static final int TAB_POS_SETTINGS   = 4;
 
     private ViewPager mViewPager;
     private TabsAdapter mTabsAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.toolbox);
 
         mViewPager = (ViewPager) findViewById(R.id.view_pager);
 
         final ActionBar bar = getActionBar();
         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
         bar.setTitle(R.string.app_name);
         bar.setDisplayHomeAsUpEnabled(true);
 
         // Ordered relation to TAB_POS_*
         mTabsAdapter = new TabsAdapter(this, mViewPager);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_nightlies),
                 NightliesTab.class, null);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_releases),
                 ReleasesTab.class, null);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_testing),
                 TestingTab.class, null);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_gapps),
                 GappsTab.class, null);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_settings),
                 SettingsTab.class, null);
 
         mViewPager.setOffscreenPageLimit(mTabsAdapter.getCount());
     }
 
     @Override
     public void onStart() {
         super.onStart();
         ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                 .cancelAll();
 
         Intent intent = getIntent();
         ManifestEntry entry = intent.getParcelableExtra(
                 Constants.EXTRA_MANIFEST_ENTRY);
         if (entry != null) {
             if (entry.getType().equals(Constants.BUILD_TYPE_NIGHTLIES)) {
                 mViewPager.setCurrentItem(TAB_POS_NIGHTLIES);
             } else if (entry.getType().equals(Constants.BUILD_TYPE_RELEASE)) {
                 mViewPager.setCurrentItem(TAB_POS_RELEASES);
             } else if (entry.getType().equals(Constants.BUILD_TYPE_TESTING)) {
                 mViewPager.setCurrentItem(TAB_POS_TESTING);
             } else if (entry.getType().equals(Constants.BUILD_TYPE_GAPPS)) {
                 mViewPager.setCurrentItem(TAB_POS_GAPPS);
             } else {
                 Log.e(Constants.TAG, "Updater::onStart() - Unknown build type");
             }
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             onBackPressed();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     public UpdatesFragment findFragmentByPosition(int position) {
         return (UpdatesFragment) getFragmentManager().findFragmentByTag(
                 "android:switcher:" + mViewPager.getId() + ":"
                         + mTabsAdapter.getItemId(position));
     }
 
     class TabsAdapter extends FragmentPagerAdapter
             implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
         private final Context mContext;
         private final ActionBar mActionBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
         private int mLastPosition = 0;
 
         final class TabInfo {
             private final Class<?> clss;
             private final Bundle args;
 
             TabInfo(Class<?> _class, Bundle _args) {
                 clss = _class;
                 args = _args;
             }
         }
 
         public TabsAdapter(Activity activity, ViewPager pager) {
             super(activity.getFragmentManager());
             mContext = activity;
             mActionBar = activity.getActionBar();
             mViewPager = pager;
             mViewPager.setAdapter(this);
             mViewPager.setOnPageChangeListener(this);
         }
 
         public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
             TabInfo info = new TabInfo(clss, args);
             tab.setTag(info);
             tab.setTabListener(this);
             mTabs.add(info);
             mActionBar.addTab(tab);
             notifyDataSetChanged();
         }
 
         @Override
         public int getCount() {
             return mTabs.size();
         }
 
         @Override
         public Fragment getItem(int position) {
             TabInfo info = mTabs.get(position);
             return Fragment.instantiate(mContext, info.clss.getName(), info.args);
         }
 
         public void onPageSelected(int position) {
             mActionBar.setSelectedNavigationItem(position);
 
             /* We need to close the action view when scrolling, if opened.
              * But skip the last fragment, it's our settings. And skip when
              * frag is null.
              */
             if (mLastPosition != TAB_POS_SETTINGS) {
                 UpdatesFragment frag = findFragmentByPosition(mLastPosition);
                 if (frag != null) {
                     ActionMode actionMode = frag.getChildActionMode();
                     if (actionMode != null) {
                         actionMode.finish();
                     }
                 }
             }
             mLastPosition = position;
         }
 
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
             Object tag = tab.getTag();
             for (int i=0; i<mTabs.size(); i++) {
                 if (mTabs.get(i) == tag) {
                     mViewPager.setCurrentItem(i);
                 }
             }
         }
 
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
         public void onPageScrollStateChanged(int state) { }
         public void onTabUnselected(Tab tab, FragmentTransaction ft) { }
         public void onTabReselected(Tab tab, FragmentTransaction ft) { }
     }
 
 }
