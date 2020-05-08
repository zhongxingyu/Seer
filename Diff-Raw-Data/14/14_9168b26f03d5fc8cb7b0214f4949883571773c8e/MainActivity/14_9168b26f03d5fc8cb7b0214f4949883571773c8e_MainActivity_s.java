 package com.portman.touchtv;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Locale;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.GestureDetectorCompat;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 	static final int NUM_PAGES = 6;
 
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
     static ViewPager mViewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Create the adapter that will return a fragment for each of the 
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
                             //.setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setIcon(mSectionsPagerAdapter.getPageIcon(i))
                             .setTabListener(this));
         }
         
         // set to epg
         actionBar.setSelectedNavigationItem(2);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
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
         	Fragment fragment = null;
         	
             // getItem is called to instantiate the fragment for the given page.
         	switch ( position ) {
         		case  0:
         			fragment = new FriendsFragment();
         			break;
         		case  1:
         			fragment = new RemindersFragment();
         			break;
         		case  2:
         			fragment = new EPGFragment();
         			break;
         		case  3:
         			fragment = new UserFragment();
         			break;
         		case  4:
         			fragment = new BrowserFragment();
         			break;
         		case  5:
         			fragment = new SearchFragment();
         			break;
         	}
 
         	//Bundle args = new Bundle();
             //args.putInt(ARG_SECTION_NUMBER, position + 1);
             //fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Show 6 total pages.
             return NUM_PAGES;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             switch (position) {
                 case 0:
                     return getString(R.string.title_friends).toUpperCase(l);
                 case 1:
                     return getString(R.string.title_reminders).toUpperCase(l);
                 case 2:
                     return getString(R.string.title_epg).toUpperCase(l);
                 case 3:
                     return getString(R.string.title_user).toUpperCase(l);
                 case 4:
                     return getString(R.string.title_browser).toUpperCase(l);
                 case 5:
                     return getString(R.string.title_search).toUpperCase(l);
             }
             return null;
         }
         
         public int getPageIcon(int position) {
             switch (position) {
                 case 0:
                     return R.drawable.friends;
                 case 1:
                     return R.drawable.reminders;
                 case 2:
                     return R.drawable.epg;
                 case 3:
                     return R.drawable.user;
                 case 4:
                     return R.drawable.browser;
                 case 5:
                     return R.drawable.search;
             }
             return 0;
         }
     }
     
     /**
      * Friends fragment
      */
     public static class FriendsFragment extends Fragment {
     	private GestureDetectorCompat mDetector;
     	private Context context;
         
     	public FriendsFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
         	context = this.getActivity();
             // init gesture detector
             mDetector = new GestureDetectorCompat(this.getActivity(), new MyGestureListener());
 
         	View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
             ImageView imageView = (ImageView) rootView.findViewById(R.id.touchPad);
             imageView.setOnTouchListener(new View.OnTouchListener() {
 
                 @Override
                 public boolean onTouch(View v, MotionEvent event) {
                 	if (event.getAction() == MotionEvent.ACTION_MOVE) {
                 		mViewPager.requestDisallowInterceptTouchEvent(true); // tell viewpager not to move
                     }
                 	
                     if (mDetector.onTouchEvent(event)) {
                         return true;
                     }
                     return true;
                 }
             });
             
             return rootView;
         }
         
         class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
             private static final String DEBUG_TAG = "GestureDetetor"; 
             private static final int SWIPE_MIN_DISTANCE = 120;
             private static final int SWIPE_THRESHOLD_VELOCITY = 200;
             
             @Override
             public boolean onDown(MotionEvent event) { 
                 Log.d(DEBUG_TAG,"onDown: " + event.toString());
                 //Toast.makeText(context, "onDown: " + event.toString(), Toast.LENGTH_SHORT).show();
                 return true;
             }
 
             @Override
             public boolean onFling(MotionEvent event1, MotionEvent event2, 
                     float velocityX, float velocityY) {
                 Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
               
                 if(event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	Toast.makeText(context, "Right to Left", Toast.LENGTH_SHORT).show();
                	
                    new SendCommand().execute("http://192.168.42.1:5000//device/yes_max_hd/clicked/KEY_CHANNELUP");
                 }  else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	Toast.makeText(context, "Left to Right", Toast.LENGTH_SHORT).show();
                 	new SendCommand().execute("http://192.168.42.1:5000//device/yes_max_hd/clicked/KEY_CHANNELDOWN");
                 }
 
                 if(event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	Toast.makeText(context, "Bottom to Top", Toast.LENGTH_SHORT).show();
                 	new SendCommand().execute("http://192.168.42.1:5000//device/yes_max_hd/clicked/KEY_VOLUMEDOWN");
                 }  else if (event2.getY() - event1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	Toast.makeText(context, "Top to Bottom", Toast.LENGTH_SHORT).show();
                 	new SendCommand().execute("http://192.168.42.1:5000//device/yes_max_hd/clicked/KEY_VOLUMEUP");
                 }
                 
                 return true;
             }
         }
     }
     
     /**
      * Reminders fragment
      */
     public static class RemindersFragment extends Fragment {
         
     	public RemindersFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_reminders, container, false);
             return rootView;
         }
     }
     
     /**
      * EPG fragment
      */
     public static class EPGFragment extends Fragment {
         
     	public EPGFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_epg, container, false);
             return rootView;
         }
     }
     
     /**
      * User fragment
      */
     public static class UserFragment extends Fragment {
         
     	public UserFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_user, container, false);
             return rootView;
         }
     }
 
     /**
      * Browser fragment
      */
     public static class BrowserFragment extends Fragment {
         
     	public BrowserFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_browser, container, false);
             return rootView;
         }
     }
     
     /**
      * Search fragment
      */
     public static class SearchFragment extends Fragment {
         
     	public SearchFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_search, container, false);
             return rootView;
         }
     }
     
     public static class SendCommand extends AsyncTask<String,String,String> {
         protected String doInBackground(String... urls) {
               
             try {
                 InputStream is = null;
                 try {
                     URL url = new URL(urls[0]);
                     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                     conn.setReadTimeout(10000 /* milliseconds */);
                     conn.setConnectTimeout(15000 /* milliseconds */);
                     conn.setRequestMethod("GET");
                     conn.setDoInput(true);
                     // Starts the query
                     conn.connect();
                     int response = conn.getResponseCode();
                     Log.d("SendCommand", "The response is: " + response);
                     is = conn.getInputStream();
 
                     // Convert the InputStream into a string
                     String contentAsString = readIt(is, 500);
                     return contentAsString;
                     
                 // Makes sure that the InputStream is closed after the app is
                 // finished using it.
                 } finally {
                     if (is != null) {
                         is.close();
                     } 
                 }
             	
             } catch (IOException e) {
                 return "Unable to retrieve web page. URL may be invalid.";
             }
         }
         // onPostExecute displays the results of the AsyncTask.
         @Override
         protected void onPostExecute(String result) {
         	//Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
         
         // Reads an InputStream and converts it to a String.
         public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
             Reader reader = null;
             reader = new InputStreamReader(stream, "UTF-8");        
             char[] buffer = new char[len];
             reader.read(buffer);
             return new String(buffer);
         }
     }
 
 }
