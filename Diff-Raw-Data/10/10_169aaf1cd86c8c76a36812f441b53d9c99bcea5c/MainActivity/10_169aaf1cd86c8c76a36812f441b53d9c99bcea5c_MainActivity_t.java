 package com.ucb.dcm;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.ucb.dcm.data.DBHelper;
 import com.ucb.dcm.data.DataService;
 
 import java.util.HashMap;
 
 public class MainActivity extends SherlockFragmentActivity {
     TabHost mTabHost;
     TabManager mTabManager;
 
     private static final String SHOWS_TAB = "Shows";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         //Set up services
         DataService.Initialize(this);
         DBHelper.Initialize(this);
 
         if(DataService.getSharedService().shouldUpdate()){
             DataService.getSharedService().updateFromServer(null);
         }
         //setTheme(SampleList.THEME); //Used for theme switching in samples
         super.onCreate(savedInstanceState);
 
 
         setTitle("Del Close Marathon 15");
 
         setContentView(R.layout.activity_main);
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
 
         mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
 
         mTabManager.addTab(mTabHost.newTabSpec("Now").setIndicator("Now"), NowActivity.class, null);
         mTabManager.addTab(mTabHost.newTabSpec(SHOWS_TAB).setIndicator("Shows"), ShowsFragment.class, null);
         mTabManager.addTab(mTabHost.newTabSpec("Venues").setIndicator("Venues"), VenuesFragment.class, null);
         mTabManager.addTab(mTabHost.newTabSpec("Favs").setIndicator("Favs"), FavoritesFragment.class, null);
 
         if (savedInstanceState != null) {
             mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString("tab", mTabHost.getCurrentTabTag());
     }
 
 
 
     /**`
      * This is a helper class that implements a generic mechanism for
      * associating fragments with the tabs in a tab host.  It relies on a
      * trick.  Normally a tab host has a simple API for supplying a View or
      * Intent that each tab will show.  This is not sufficient for switching
      * between fragments.  So instead we make the content part of the tab host
      * 0dp high (it is not shown) and the TabManager supplies its own dummy
      * view to show as the tab content.  It listens to changes in tabs, and takes
      * care of switch to the correct fragment shown in a separate content area
      * whenever the selected tab changes.
      */
     public static class TabManager implements TabHost.OnTabChangeListener {
         private final FragmentActivity mActivity;
         private final TabHost mTabHost;
         private final int mContainerId;
         private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
         TabInfo mLastTab;
 
         static final class TabInfo {
             private final String tag;
             private final Class<?> clss;
             private final Bundle args;
             private Fragment fragment;
 
             TabInfo(String _tag, Class<?> _class, Bundle _args) {
                 tag = _tag;
                 clss = _class;
                 args = _args;
             }
         }
 
         static class DummyTabFactory implements TabHost.TabContentFactory {
             private final Context mContext;
 
             public DummyTabFactory(Context context) {
                 mContext = context;
             }
 
             @Override
             public View createTabContent(String tag) {
                 View v = new View(mContext);
                 v.setMinimumWidth(0);
                 v.setMinimumHeight(0);
                 return v;
             }
         }
 
         public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
             mActivity = activity;
             mTabHost = tabHost;
             mContainerId = containerId;
             mTabHost.setOnTabChangedListener(this);
         }
 
         public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
             tabSpec.setContent(new DummyTabFactory(mActivity));
             String tag = tabSpec.getTag();
 
             TabInfo info = new TabInfo(tag, clss, args);
 
             // Check to see if we already have a fragment for this tab, probably
             // from a previously saved state.  If so, deactivate it, because our
             // initial state is that a tab isn't shown.
             info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
             if (info.fragment != null && !info.fragment.isDetached()) {
                 FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                 ft.detach(info.fragment);
                 ft.commit();
             }
 
             mTabs.put(tag, info);
             mTabHost.addTab(tabSpec);
         }
 
         @Override
         public void onTabChanged(String tabId) {
             TabInfo newTab = mTabs.get(tabId);
             if (mLastTab != newTab) {
                 FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                 if (mLastTab != null) {
                     if (mLastTab.fragment != null) {
                         ft.detach(mLastTab.fragment);
                     }
                 }
                 if (newTab != null) {
                     if (newTab.fragment == null) {
                         newTab.fragment = Fragment.instantiate(mActivity,
                                 newTab.clss.getName(), newTab.args);
                         ft.add(mContainerId, newTab.fragment, newTab.tag);
                     } else {
                         ft.attach(newTab.fragment);
                     }
                 }
 
                 mLastTab = newTab;
                 ft.commit();
                 mActivity.getSupportFragmentManager().executePendingTransactions();
             }
 
             //Do this so we can show the search button or not.
            mActivity.supportInvalidateOptionsMenu();
            //mActivity.invalidateOptionsMenu();
         }
     }
 
     public void onScheduleDownloaded(){
         mTabManager.mLastTab.fragment.onResume();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         getSupportMenuInflater().inflate(R.menu.main, menu);
         if(mTabHost.getCurrentTabTag().equals(SHOWS_TAB)){
             menu.findItem(R.id.menu_search).setVisible(true);
         }
         else
         {
             menu.findItem(R.id.menu_search).setVisible(false);
         }
 
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()){
             case R.id.menu_search:
                 SearchView sv = (SearchView) item.getActionView();
                 sv.setQueryHint("Title or Performer");
                 sv.setOnQueryTextListener(new SearchListener(sv));
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     private class SearchListener implements SearchView.OnQueryTextListener {
 
         SearchView view;
 
         public SearchListener(SearchView sv){
             this.view = sv;
         }
 
         @Override
         public boolean onQueryTextSubmit(String s) {
             if(mTabHost.getCurrentTabTag().equals(SHOWS_TAB)){
                 ShowsFragment frag = (ShowsFragment) mTabManager.mLastTab.fragment;
                 frag.setFilter(s);
             }
 
             view.clearFocus();
 
             return true;
         }
 
         @Override
         public boolean onQueryTextChange(String s) {
             if(mTabHost.getCurrentTabTag().equals(SHOWS_TAB)){
                 ShowsFragment frag = (ShowsFragment) mTabManager.mLastTab.fragment;
                 frag.setFilter(s);
             }
             return true;
         }
     }
 
 
 
 }
