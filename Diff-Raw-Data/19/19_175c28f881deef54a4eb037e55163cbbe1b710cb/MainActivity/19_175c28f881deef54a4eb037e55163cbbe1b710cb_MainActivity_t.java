 package com.jason.ocbcapp;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // ActionBarSherlock imports
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 // PullToRefreshList import
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 
 public class MainActivity extends SherlockFragmentActivity implements
         ActionBar.TabListener, TabHost.OnTabChangeListener {
 
     // The tag for Android Logger
     public final static String APP_TAG = "OCBCApp";
 
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
 
     TabHost mTabHost;
 
     private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MainActivity.TabInfo>();
 
     // Name of the preference SharedPreferences that we are using for the app
 
     MainActivity mMainActivity = this;
 
     private LinkedList<String> abDropdownList;
     private ArrayAdapter<String> abDropdownAdapter;
 
     private PullToRefreshListView mPullRefreshListView;
 
     // The request type when we want the SetupActivity to perform
     // setup for the user
     static final int SETUP_REQUEST = 1;
 
     // token that uniquely identifies the user on the server
     public String userToken = null;
 
     // state to store whether user has already done the setting up
     private boolean hasSetup = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // start SetupActivity if user has not setup the app
         startSetupActivity();
 
         initializeTabHost(savedInstanceState);
 
         initializeViewPager();
     }
 
    private void debugPreferences() {
        String userToken = CrossCutting.getUserTokenFromPreferences(getApplicationContext());
        Boolean hasSetup = CrossCutting.getHasSetupFromPreferences(getApplicationContext());
        CrossCutting.showToastMessage(getApplication(), String.format("setup = %b, token = %s", hasSetup, userToken));
        if (userToken == null) {
            CrossCutting.showToastMessage(getApplicationContext(), "OOPS!");
        }
    }

     private void initializeUserToken() {
         if (hasSetup)
             userToken = CrossCutting.getUserTokenFromPreferences(getApplicationContext());
         else {
             Log.wtf(APP_TAG, "can't init token without setting up");
         }
     }
 
     private void initializeViewPager() {
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(
                 getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.viewpager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab. We can also use ActionBar.Tab#select() to do this if we have
         // a reference to the Tab.
         mViewPager
                 .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                     @Override
                     public void onPageSelected(int position) {
                         mTabHost.setCurrentTab(position);
                     }
                 });
     }
 
     // starts the setup activity and waits for the user's token
     private void startSetupActivity() {
        hasSetup = CrossCutting.getHasSetupFromPreferences(getApplicationContext());
         if (!hasSetup) {
             Log.i("OCBCApp", "Starting setup");
             Intent intent = new Intent(this, SetupActivity.class);
             startActivityForResult(intent, SETUP_REQUEST);
         }
     }
 
     // work on the result that the activities return
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.d(APP_TAG, "returned from activity");
         // make sure we are responding to the setup request
         if (requestCode == SETUP_REQUEST) {
             // make sure the request was successful
             if (resultCode == RESULT_OK) {
                 storeToken(data.getStringExtra("userToken"));
                initializeUserToken();
             }
         }
     }
 
     // stores the token we got from SetupActivity
     private void storeToken(String token) {
         if (TextUtils.isEmpty(token)) {
             Log.wtf(APP_TAG, "token is empty!");
         } else {
             Log.d(APP_TAG, "Storing token: " + token);
             Log.d(APP_TAG, "Storing token, hasSetup : " + hasSetup);
             userToken = token;
             // we got the token, user has performed setup
             hasSetup = true;
             commitTokenToPrefs(userToken);
         }
     }
 
     private void commitTokenToPrefs(String userToken) {
         CrossCutting.setHasSetupPreference(getApplicationContext(), true);
         CrossCutting.setUserTokenPreference(getApplicationContext(), userToken);
         Log.d(APP_TAG, "after commiting, hasSetup: " + hasSetup);
     }
 
     /**
      * Initialise the Tab Host, add fragments to tab host.
      */
     private void initializeTabHost(Bundle args) {
         mTabHost = (TabHost) findViewById(android.R.id.tabhost);
         mTabHost.setup();
         TabInfo tabInfo = null;
 
         View indicator = getLayoutInflater().inflate(R.layout.tab_indicator,
                 null);
         TextView titleTv = (TextView) indicator.findViewById(R.id.title);
 
         String tabName = this.getString(R.string.title_section_branches)
                 .toUpperCase();
         titleTv.setText(tabName);
 
         MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(
                 tabName).setIndicator(indicator), (tabInfo = new TabInfo(
                 tabName, LeastWaitingTimeListFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
 
         indicator = getLayoutInflater().inflate(R.layout.tab_indicator, null);
         titleTv = (TextView) indicator.findViewById(R.id.title);
 
         tabName = this.getString(R.string.title_section_nearest_branches).toUpperCase();
         titleTv.setText(tabName);
         MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(
                 tabName).setIndicator(indicator), (tabInfo = new TabInfo(
                 tabName, NearestBranchesFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
 
         indicator = getLayoutInflater().inflate(R.layout.tab_indicator, null);
         titleTv = (TextView) indicator.findViewById(R.id.title);
         tabName = this.getString(R.string.title_section_appts).toUpperCase();
         titleTv.setText(tabName);
         MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(
                 tabName).setIndicator(indicator), (tabInfo = new TabInfo(
                 tabName, AppointmentsFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         // Default to first tab
         // this.onTabChanged("Tab1");
         //
         mTabHost.setOnTabChangedListener(this);
     }
 
     /**
      * Add Tab content to the Tabhost
      * 
      * @param activity
      * @param tabHost
      * @param tabSpec
      * @param clss
      * @param args
      */
     private static void AddTab(MainActivity activity, TabHost tabHost,
             TabHost.TabSpec tabSpec, TabInfo tabInfo) {
         // Attach a Tab view factory to the spec
         tabSpec.setContent(activity.new TabFactory(activity));
         tabHost.addTab(tabSpec);
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab,
             android.support.v4.app.FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in
         // the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab,
             android.support.v4.app.FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab,
             android.support.v4.app.FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         int tabPosition = this.mTabHost.getCurrentTab();
         Log.d(APP_TAG, "createIotionsmenu pos = " + tabPosition);
         getSupportMenuInflater().inflate(R.menu.activity_main, menu);
         switch (tabPosition) {
         case 0:
             return true;
         case 1:
             return true;
         case 2:
             menu.add(0, R.id.menu_my_appts, 0, R.string.menu_my_appts);
             return true;
         default:
             return true;
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.menu_make_walk_in_appt:
             startMakeWalkInApptActivity();
             return true;
         case R.id.menu_settings:
             return true;
         case R.id.menu_my_appts:
             startMyAppointmentsIntent();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void startMyAppointmentsIntent() {
         Intent intent = new Intent(this, MyAppointmentsActivity.class);
         Log.d(APP_TAG, "starting my appointments intent");
         startActivity(intent);
     }
 
     private void startMakeWalkInApptActivity() {
         Intent intent = new Intent(this, MakeWalkInApptActivity.class);
         startActivity(intent);
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
             switch (position) {
             case 0:
                 return new LeastWaitingTimeListFragment();
             case 1:
                 return new NearestBranchesFragment();
             case 2:
                 return new AppointmentsFragment();
             default:
                 return new Fragment();
             }
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
             case 0:
                 return getString(R.string.title_section_branches).toUpperCase();
             case 1:
                 return getString(R.string.title_section_nearest_branches).toUpperCase();
             case 2:
                 return getString(R.string.title_section_appts).toUpperCase();
             }
             return null;
         }
     }
 
     /**
      * Maintains extrinsic info of a tab's construct
      * 
      * @author Jason
      */
     private class TabInfo {
         private String tag;
         private Class<?> clss;
         private Bundle args;
         private Fragment fragment;
 
         TabInfo(String tag, Class<?> clazz, Bundle args) {
             this.tag = tag;
             this.clss = clazz;
             this.args = args;
         }
 
     }
 
     /**
      * A simple factory that returns dummy views to the Tabhost
      * 
      * @author Jason
      */
     class TabFactory implements TabContentFactory {
 
         private final Context mContext;
 
         /**
          * @param context
          */
         public TabFactory(Context context) {
             mContext = context;
         }
 
         /**
          * (non-Javadoc)
          * 
          * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
          */
         public View createTabContent(String tag) {
             View view = new View(mContext);
             view.setMinimumWidth(0);
             view.setMinimumHeight(0);
             return view;
         }
 
     }
 
     @Override
     public void onTabChanged(String arg0) {
         int pos = this.mTabHost.getCurrentTab();
         this.mViewPager.setCurrentItem(pos);
         // change option menu when swiping between tabs
         invalidateOptionsMenu();
     }
 
     public void showToast(String string) {
         Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT)
                 .show();
     }
 }
