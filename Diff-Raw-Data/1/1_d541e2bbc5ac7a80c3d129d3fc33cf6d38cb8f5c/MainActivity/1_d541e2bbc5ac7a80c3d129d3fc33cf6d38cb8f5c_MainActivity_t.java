 package org.alpha.conference2013;
 
 import org.alpha.conference2013.R;
 import org.alpha.conference2013.data.DownloadService;
 import org.alpha.conference2013.diary.DiaryFragment;
 import org.alpha.conference2013.home.HomeVideoFragment;
 import org.alpha.conference2013.map.MapFragment;
 import org.alpha.conference2013.more.MoreFragment;
 import org.alpha.conference2013.speakers.SpeakersFragment;
 //import org.alpha.focus2012.programme.ProgrammeFragment;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
     private final BroadcastReceiver receiver = new BroadcastReceiver() {
         ProgressDialog progress = null;
         
         @Override
         public void onReceive(Context context, Intent intent) {
             if (Constants.SHOW_LOADING_MESSAGE_INTENT.equals(intent.getAction())) {
                 runOnUiThread(new Runnable() {
                     public void run() {
                         progress = new ProgressDialog(MainActivity.this);
                         progress.setMessage("Please wait while content is downloaded.");
                         progress.show();
                     }
                 });
             }
             else if (Constants.HIDE_LOADING_MESSAGE_INTENT.equals(intent.getAction())) {
                 runOnUiThread(new Runnable() {
                     public void run() {
                         if (progress != null) {
                             progress.hide();
                            progress.dismiss();
                             progress = null;
                         }
                     }
                 });
             }
             else if (Constants.SHOW_OFFLINE_INTENT.equals(intent.getAction())) {
                 runOnUiThread(new Runnable() {
                     public void run() {
                         Toast.makeText(MainActivity.this, "Sorry, cannot could not be downloaded while offline", Toast.LENGTH_LONG).show();
                     }
                 });
             }
         }
     };
     
     
     private ActionBar mActionBar;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Parse.initialize(this, "4TKzxN13GcKwwBitDbbWsuwys5atCZ6AMSX37ARL", "5HeFvLWCLStw8aZftXPLYDLBmIRFESju3hdEhIOJ");
         //Parse.initialize(this, "yYdPVCsM1GZGQWmJhxwQcszbG3802BJKWyqtZzFg","d8IVhmFzL6gzSAZEPxDCU9xmAjep08LVnUlRBfJG");
         PushService.setDefaultPushCallback(this, MainActivity.class);
         ParseInstallation.getCurrentInstallation().saveInBackground();
         ParseAnalytics.trackAppOpened(getIntent());
 
         mActionBar = getSupportActionBar();
         mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
         mActionBar.setDisplayShowTitleEnabled(true);   
                 
         //Home
         Tab homeTab  = mActionBar.newTab()
                 .setText(R.string.home_tab)
                 .setTabListener(new TabListener<HomeVideoFragment>(this, "home", HomeVideoFragment.class));
         mActionBar.addTab(homeTab);
                 
         // programme
         //Tab programmeTab  = mActionBar.newTab()
         //        .setText(R.string.programme_tab)
         //        .setTabListener(new TabListener<ProgrammeFragment>(this, "programme", ProgrammeFragment.class));
         //mActionBar.addTab(programmeTab);
         
         // diary
         Tab diaryTab  = mActionBar.newTab()
                 .setText(R.string.diary_tab)
                 .setTabListener(new TabListener<DiaryFragment>(this, "diary", DiaryFragment.class));
         mActionBar.addTab(diaryTab);   
         
         // speakers
         Tab speakersTab  = mActionBar.newTab()
                 .setText(R.string.speakers_tab)
                 .setTabListener(new TabListener<SpeakersFragment>(this, "speakers", SpeakersFragment.class));
         mActionBar.addTab(speakersTab); 
         
         // maps
         Tab mapTab  = mActionBar.newTab()
                 .setText(R.string.maps_tab)
                 .setTabListener(new TabListener<MapFragment>(this, "map", MapFragment.class));
         mActionBar.addTab(mapTab);
         
         // more
         Tab moreTab  = mActionBar.newTab()
                 .setText(R.string.more_tab)
                 .setTabListener(new TabListener<MoreFragment>(this, "more", MoreFragment.class));        
         mActionBar.addTab(moreTab);
        
         // restore previous selected tab
         if (savedInstanceState != null) {
             Integer activeTabIndex = savedInstanceState.getInt("activeTabIndex", 0);
             mActionBar.selectTab(mActionBar.getTabAt(activeTabIndex));
             
         }
     }  
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         Integer activeTabIndex = mActionBar.getSelectedNavigationIndex();
         outState.putInt("activeTabIndex", activeTabIndex);
         super.onSaveInstanceState(outState);
     }
     
     public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
         private Fragment mFragment;
         private final SherlockFragmentActivity mActivity;
         private final String mTag;
         private final Class<T> mClass;
 
         /** Constructor used each time a new tab is created.
           * @param activity  The host Activity, used to instantiate the fragment
           * @param tag  The identifier tag for the fragment
           * @param clz  The fragment's Class, used to instantiate the fragment
           */
         public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
             mActivity = activity;
             mTag = tag;
             mClass = clz;
         }
 
         /* The following are each of the ActionBar.TabListener callbacks */
 
         @Override
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
             FragmentManager fragMgr = mActivity.getSupportFragmentManager();
             ft = fragMgr.beginTransaction();
             ft.commit();            
             
             // restore an existing fragment
             mFragment = fragMgr.findFragmentByTag(mTag);
             
             // Check if the fragment is already initialised
             if (mFragment == null) {
                 // If not, instantiate and add it to the activity
                 mFragment = Fragment.instantiate(mActivity, mClass.getName());
                 ft.add(android.R.id.content, mFragment, mTag);
             } else {
                 // If it exists, simply attach it in order to show it
                 ft.attach(mFragment);
             }
         }
 
         @Override
         public void onTabUnselected(Tab tab, FragmentTransaction ft) {
             if (mFragment != null) {
                 // Detach the fragment, because another one is being attached
                 ft.detach(mFragment);
             }
         }
 
         @Override
         public void onTabReselected(Tab tab, FragmentTransaction ft) {
             // User selected the already selected tab. Usually do nothing.
         }
     }    
 
     @Override
     protected void onStart() {
         super.onStart();
         // trigger a refresh of the data
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(Constants.SHOW_LOADING_MESSAGE_INTENT);
         intentFilter.addAction(Constants.HIDE_LOADING_MESSAGE_INTENT);
         intentFilter.addAction(Constants.SHOW_OFFLINE_INTENT);
         LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
         startService(new Intent(this, DownloadService.class));
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
     }
 
 }
