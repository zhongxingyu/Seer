 package com.example.hotellocator;
 
 import java.util.Locale;
 
 import com.google.android.gms.maps.model.LatLng;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
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
 import android.widget.SearchView;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide
      * fragments for each of the sections. We use a
      * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
      * will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a
      * {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     SectionsPagerAdapter mSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
     final Hotel[] hotels = {
 			new Hotel("Pegasus", "7 Knutsford Boulevard", "Lorem Ipsum is simply dummy text of the printing and typesetting industry.", 1000, new LatLng(25.002856, -76.795659), 5),
     		new Hotel("Hilton", "7 Hibiscus Drive", "Lorem ipsum", 1001, new LatLng(18.002856, -76.795659), 4),
     		new Hotel("Four Seasons", "2A Shalimar Close", "Eureka", 1002, new LatLng(28.002856, -76.795659), 6),
     		new Hotel("Holiday Inn", "83 Lore Lane", "Exciting", 1003, new LatLng(18.002856, 76.795659), 3),
     		new Hotel("Grande Palladium", "3 Old Way", "Boring", 1004, new LatLng(18.002856, 45.795659), 2),
     		new Hotel("Riu", "Ocho Rios", "nice food", 1005, new LatLng(14.002856, -7.795659), 4)
     };
     
 //    final Attraction[] attraction = {
 //    		new Attraction("Montego Bay", )
//    }
 //    
 //    final Event[] events = {
 //    		
 //    }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab. We can also use ActionBar.Tab#select() to do this if we have
         // a reference to the Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by
             // the adapter. Also specify this Activity object, which implements
             // the TabListener interface, as the callback (listener) for when
             // this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         // Configure the search info and add any event listeners
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in
         // the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the sections/tabs/pages.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a DummySectionFragment (defined as a static inner class
             // below) with the page number as its lone argument.
 
         	Fragment fragment = null;
         	
         	if(position==0)
         	{
         		fragment = new FeaturedFragment();
         	}
         	else
         		if(position==1)
         			fragment = new SearchFragment();
         	else{
         		fragment = new DummySectionFragment();
                 Bundle args = new Bundle();
                 args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                 fragment.setArguments(args);
         	}
         	
         	//            
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Show 3 total pages.
             return 4;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             //Locale l = Locale.getDefault();
             switch (position) {
                 case 0:
                     //return getString(R.string.title_section1).toUpperCase(l);
                     return "Home";
                 case 1:
                     return "All";
                 case 2:
                 	return "Map";
                 case 3:
                 	return "Favourites";
             }
             return null;
         }
     }
     
     public static class DummySectionFragment extends Fragment {
       /**
        * The fragment argument representing the section number for this
        * fragment.
        */
       public static final String ARG_SECTION_NUMBER = "section_number";
 
       public DummySectionFragment() {
       }
 
       @Override
       public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
       	
       	View rootView = null;
       	rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
           	TextView dummyTextView = (TextView) rootView.findViewById(R.id.textView1);
           	dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
       	return rootView;
       }
   }
 
 }
