 package com.zero.star.tabnyan;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.TabHost.TabSpec;
 
 public class TabNyanActivity extends FragmentActivity implements TabHost.OnTabChangeListener {
 
     /** parameter for root fragment */
 	static final String ROOT_FRAGMENT_ARGS = "root_nyan";
 
     /** TabHost */
     private TabHost mTabHost;
 
     /** last select Tab Information */
     private TabInfo mLastTabInfo;
 
     /** tag and TabInfo Map */
     private Map<String, TabInfo> mMapTaInfo;
 
     /** View id of Fragment */
     private int mContentId;
 
     /** tab change listener */
     private TabHost.OnTabChangeListener mOnTabChangeListener;
 
     @Override
     public void onBackPressed() {
         if (getCurrentFragment() == null || !getCurrentFragment().popBackStack()) {
             // empty BackStack
             super.onBackPressed();
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mTabHost = null;
     }
 
     @Override
     public void onTabChanged(String tabId) {
         TabInfo newTab = mMapTaInfo.get(tabId);
 
         if (mLastTabInfo != newTab) {
             FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
             if (mLastTabInfo != null) {
                 fragmentTransaction.detach(mLastTabInfo.fragment);
             }
            if (getSupportFragmentManager().findFragmentById(mContentId) != null) {
                fragmentTransaction.detach(getSupportFragmentManager().findFragmentById(mContentId));
            }
             if (newTab.fragment == null) {
                 newTab.fragment = createTabRootFragment(newTab);
                 fragmentTransaction.add(mContentId, newTab.fragment);
             } else {
                 fragmentTransaction.attach(newTab.fragment);
             }
 
             mLastTabInfo = newTab;
 
             fragmentTransaction.commit();
         }
 
         if (mOnTabChangeListener != null) {
         	mOnTabChangeListener.onTabChanged(tabId);
         }
     }
 
     /**
      * setup tab.
      * @param contentId View id
      * @param listener Tab change listener. If you do not want to use, please pass the null
      */
     protected void setup(int contentId, TabHost.OnTabChangeListener listener) {
 
     	mContentId = contentId;
     	mOnTabChangeListener = listener;
 
         mTabHost = (TabHost) findViewById(android.R.id.tabhost);
         mTabHost.setup();
 
         mMapTaInfo = new HashMap<String, TabInfo>();
 
         mTabHost.setOnTabChangedListener(this);
 
     }
 
     /**
      * Add tab.
      * @param tag tag
      * @param indicator String indicator
      * @param fragmentClass first view of Fragment
      */
     protected void addTab(String tag, String indicator, Class<? extends Fragment> fragmentClass) {
     	addTab(tag, null, indicator, fragmentClass, new Bundle());
     }
 
     /**
      * Add tab.
      * @param tag tag
      * @param indicator View indicator
      * @param fragmentClass first view of Fragment
      */
     protected void addTab(String tag, View indicator, Class<? extends Fragment> fragmentClass) {
     	addTab(tag, indicator, null, fragmentClass, new Bundle());
     }
 
     /**
      * Add tab.
      * @param tag tag
      * @param indicator String indicator
      * @param fragmentClass first view of Fragment
      * @param args pass arguments for Fragment
      */
     protected void addTab(String tag, String indicator, Class<? extends Fragment> fragmentClass, Bundle args) {
     	addTab(tag, null, indicator, fragmentClass, args);
     }
 
     /**
      * Add tab.
      * @param tag tag
      * @param indicator View indicator
      * @param fragmentClass first view of Fragment
      * @param args pass arguments for Fragment
      */
     protected void addTab(String tag, View indicator, Class<? extends Fragment> fragmentClass, Bundle args) {
     	addTab(tag, indicator, null, fragmentClass, args);
     }
 
     /**
      * Add tab.
      * @param tag tag
      * @param viewIndicator View indicator
      * @param stringIndicator String indicator
      * @param fragmentClass first view of Fragment
      * @param args pass arguments for Fragment
      */
     private void addTab(String tag, View viewIndicator, String stringIndicator, Class<? extends Fragment> fragmentClass, Bundle args) {
 
     	if (args != null && args.containsKey(ROOT_FRAGMENT_ARGS)) {
     		throw new IllegalArgumentException("arguments bundle key of the '" + ROOT_FRAGMENT_ARGS + "' is invalid");
     	}
 
     	mMapTaInfo.put(tag, new TabInfo(fragmentClass.getName(), args));
 
         TabSpec tabSpec = mTabHost.newTabSpec(tag);
         if (viewIndicator != null) {
         	tabSpec.setIndicator(viewIndicator);
         }
         if (stringIndicator != null) {
         	tabSpec.setIndicator(stringIndicator);
         }
         tabSpec.setContent(new DummyTabFactory(this));
 
         mTabHost.addTab(tabSpec);
 
     }
 
     /**
      * Create Fragment for first view.
      * @param tabInfo Tab Information
      * @return TabNyanRootFragment
      */
     private Fragment createTabRootFragment(TabInfo tabInfo) {
 
     	tabInfo.args.putString(ROOT_FRAGMENT_ARGS, tabInfo.className);
 
         TabNyanRootFragment fragment = new TabNyanRootFragment();
         fragment.setArguments(tabInfo.args);
 
         return fragment;
     }
 
     /**
      * Get current Fragment.
      * @return TabRootFragment
      */
     private TabNyanRootFragment getCurrentFragment() {
         return (TabNyanRootFragment) getSupportFragmentManager().findFragmentById(mContentId);
     }
 
     /**
      * Get the Fragment that is displayed in the selected tab
      * @return Fragment
      */
     public Fragment getTabSelectedFragment() {
         TabNyanRootFragment rootFragment = getCurrentFragment();
         return rootFragment.getCurrentFragment();
     }
 
     /**
      * Dummy view for android:id/tabcontent.
      */
     private static class DummyTabFactory implements TabContentFactory {
 
         /** Context */
         private final Context mContext;
 
         /**
          * Constructor
          * @param context Context
          */
         DummyTabFactory(Context context) {
             mContext = context;
         }
 
         @Override
         public View createTabContent(String tag) {
             View v = new View(mContext);
             return v;
         }
 
     }
 
     /**
      * Tab information.
      */
     private class TabInfo {
 
         /** Fragment class name */
         private String className;
         /** Fragment arguments */
         private Bundle args;
         /** Instance of Fragment */
         private Fragment fragment;
 
         /**
          * Constructor
          * @param className Fragment class name
          * @param args Fragment arguments
          */
         TabInfo(String className, Bundle args) {
         	this.className = className;
         	this.args = args;
         }
     }
 
 }
