 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *   UWSchedule student class and registration sharing interface
  *   Copyright (C) 2013 Sherman Pay, Jeremy Teo, Zachary Iqbal
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by`
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.amgems.uwschedule.ui;
 
 import android.app.LoaderManager;
 import android.content.AsyncQueryHandler;
 import android.content.Loader;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.view.MenuItem;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.amgems.uwschedule.R;
 import com.amgems.uwschedule.api.local.AsyncDataHandler;
 import com.amgems.uwschedule.api.uw.CookieStore;
 import com.amgems.uwschedule.loaders.GetSlnLoader;
 import com.amgems.uwschedule.provider.ScheduleContract;
 import com.amgems.uwschedule.provider.ScheduleDatabaseHelper;
 import com.amgems.uwschedule.util.Publisher;
 import com.amgems.uwschedule.util.Subscriber;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Activity that represents a home screen for the user.
  */
 public class HomeActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<GetSlnLoader.Slns>{
 
     private DrawerLayout mDrawerLayoutRoot;
     private ActionBarDrawerToggle mDrawerToggle;
     private ExpandableListView mDrawerListView;
     private ViewPager mCoursesViewPager;
     private TextView mDrawerEmailTextView;
 
     private CookieStore mCookieStore;
     private String mUsername;
 
     private static Publisher<String> mPublisher;
 
     private static final int GET_SLN_LOADER_ID = 1;
     public static final String EXTRAS_HOME_USERNAME = "mUsername";
     private static final String USER_EMAIL_POSTFIX = "@u.washington.edu";
     private static final String TAG = "HOME";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home_activity);
 
         this.deleteDatabase(ScheduleDatabaseHelper.DATABASE_NAME);
         // Initialize inbound data
         mUsername = getIntent().getStringExtra(EXTRAS_HOME_USERNAME);
         mCookieStore = CookieStore.getInstance(getApplicationContext());
 
         // Initialize view references
         mDrawerLayoutRoot = (DrawerLayout) findViewById(R.id.home_drawer_root);
         mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayoutRoot, R.drawable.ic_drawer,
                                                   R.string.drawer_open, R.string.drawer_closed);
         mDrawerLayoutRoot.setDrawerListener(mDrawerToggle);
         mCoursesViewPager = (ViewPager) findViewById(R.id.courses_pager);
         mDrawerListView = (ExpandableListView) findViewById(R.id.home_drawer_listview);
         mDrawerEmailTextView = (TextView) findViewById(R.id.home_drawer_email);
 
         // Set up navigation drawer items
         List<DrawerListAdapter.Group> drawerGroups = new ArrayList<DrawerListAdapter.Group>();
         drawerGroups.add(new DrawerListAdapter.Group(R.string.drawer_group_home, R.drawable.ic_nav_home));
         drawerGroups.add(new DrawerListAdapter.Group(R.string.drawer_group_friends, R.drawable.ic_nav_friends));
         drawerGroups.add(new DrawerListAdapter.Group(R.string.drawer_group_favorites, R.drawable.ic_nav_favorites));
         mDrawerListView.setAdapter(new DrawerListAdapter(this, drawerGroups));
         mCoursesViewPager.setAdapter(new CoursesFragmentPagerAdapter(getSupportFragmentManager()));
 
         mDrawerEmailTextView.setText(mUsername + USER_EMAIL_POSTFIX);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         LoaderManager manager = getLoaderManager();
         if (manager.getLoader(GET_SLN_LOADER_ID) == null) {
             manager.initLoader(GET_SLN_LOADER_ID, null, this);
         }
 
 
        mPublisher = new Publisher<String>() {
             private List<Subscriber<? super String>> mSubscriberList = new ArrayList<Subscriber<? super String>>();
             private String mData;
 
             @Override
             public void register(Subscriber<? super String> dataSubscriber) {
                 mSubscriberList.add(dataSubscriber);
                 dataSubscriber.update(mData);
             }
 
             @Override
             public void publish(String data) {
                 mData = data;
                 for (Subscriber<? super String> subscriber : mSubscriberList) {
                     subscriber.update(data);
                 }
             }
         };
 
         AsyncDataHandler asyncDataHandler = new AsyncDataHandler(this.getContentResolver());
        asyncDataHandler.putAccount(mUsername, mUsername);
      }
 
     /**
      * Helper method for enabling death penalty on strict mode.
      */
     private void enableDeathThreadPolicy() {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
                 .penaltyDeath()
                 .build());
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
     }
 
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public Loader<GetSlnLoader.Slns> onCreateLoader(int id, Bundle args) {
         Loader<GetSlnLoader.Slns> loader = new GetSlnLoader(this, mCookieStore.getActiveCookie());
         loader.forceLoad();
         return loader;
     }
 
     @Override
     public void onLoadFinished(Loader<GetSlnLoader.Slns> loader, GetSlnLoader.Slns data) {
         Toast.makeText(this, "Done loading!", Toast.LENGTH_SHORT).show();
         mPublisher.publish(data.getSlns().toString());
         AsyncDataHandler asyncDataHandler = new AsyncDataHandler(this.getContentResolver());
         asyncDataHandler.putCourses(mUsername, "14sp", data.getSlns().toString());
     }
 
     @Override
     public void onLoaderReset(Loader<GetSlnLoader.Slns> loader) { }
 
     /**
      * Used to allow swiping fragments on home screen.
      */
     private static class CoursesFragmentPagerAdapter extends FragmentPagerAdapter {
 
         private static int TAB_COUNT = 2;
 
         public CoursesFragmentPagerAdapter(FragmentManager fragmentManager) {
             super(fragmentManager);
         }
 
         @Override
         public Fragment getItem(int i) {
             if (i == 0) {
                 return ScheduleFragment.newInstance();
             } else {
                 DebugFragment debugFragment = DebugFragment.newInstance();
                 mPublisher.register(debugFragment);
                 return debugFragment;
             }
         }
 
         @Override
         public int getCount() {
             return TAB_COUNT;
         }
     }
 }
