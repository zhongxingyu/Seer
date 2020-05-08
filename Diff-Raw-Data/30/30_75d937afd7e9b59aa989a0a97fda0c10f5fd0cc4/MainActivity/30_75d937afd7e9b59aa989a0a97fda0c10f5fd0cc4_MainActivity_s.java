 /*
  * Copyright 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.xenon.greenup;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.xenon.greenup.api.APIServerInterface;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
      * derivative, which will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     AppSectionsPagerAdapter _AppSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will display the three primary sections of the app, one at a
      * time.
      */
     ViewPager _ViewPager;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Create the adapter that will return a fragment for each of the three primary sections of the app.
         _AppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
 
         // Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
         actionBar.setHomeButtonEnabled(false);
 
         // Specify that we will be displaying tabs in the action bar.
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager, attaching the adapter and setting up a listener for when the
         // user swipes between sections.
         _ViewPager = (ViewPager) findViewById(R.id.pager);
         _ViewPager.setAdapter(_AppSectionsPagerAdapter);
         _ViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When swiping between different app sections, select the corresponding tab.
                 // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                 // Tab.
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < _AppSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(_AppSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         _ViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
 
         public AppSectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
         
         /*
          * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
          * 
          * This is where the heavy lifting is done for the Swipe navigation. 
          * The case statement builds A fragment and gives it a navigation ID.
          * That ID is its place in the navigation from left to right
          * 
          * For now, we are just displaying 3 dummy fragments.
          * These will be replaced with the Section fragments defined below
          * 
          */
         @Override
         public Fragment getItem(int i)
         { 
         	switch (i) {
             //case 0:
         		//TODO: Launch HomeSectionFragment
             //case 1:
         		//TODO: Launch MapSectionFragment
        	//case 2:
         		//TODO: Launch FeedSectionFragment
         	default:
                 // The other sections of the app are dummy placeholders.
                 Fragment fragment = new DummySectionFragment();
                 Bundle args = new Bundle();
                 args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                 fragment.setArguments(args);
                 return fragment;
         	}
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return "Section " + (position + 1);
         }
     }
     
 
     /**
      * A dummy fragment representing a section of the app, but that simply displays dummy text.
      */
     public static class DummySectionFragment extends Fragment implements OnClickListener {
 
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
             Bundle args = getArguments();
             ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                     getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
             Button button = (Button)rootView.findViewById(R.id.testButton);
             button.setOnClickListener(this);
             return rootView;
         }
         
         public void onClick(View v) {
         	Log.i("button","I was clicked!");
         	APIServerInterface i = new APIServerInterface();
         	i.testConnection();
         }
     }
 }
