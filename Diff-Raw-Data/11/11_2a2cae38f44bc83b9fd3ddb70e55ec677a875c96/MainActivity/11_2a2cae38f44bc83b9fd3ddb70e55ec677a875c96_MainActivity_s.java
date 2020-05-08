 package com.googolmo.foursquare;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 import android.widget.TableLayout;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.googolmo.foursquare.app.NavCheckinActivity;
 import com.googolmo.foursquare.app.NavFriendActivity;
 import com.googolmo.foursquare.app.NavMeActivity;
 import com.googolmo.foursquare.app.OAuthAcitivity;
 import com.googolmo.foursquare.utils.LogUtil;
 import com.googolmo.foursquare.utils.PreferenceUtil;
 import com.viewpagerindicator.UnderlinePageIndicator;
 
 import java.util.ArrayList;
 
 public class MainActivity extends SherlockFragmentActivity {
 
 	private LogUtil mLog = LogUtil.getLog(MainActivity.class.getName());
 
     private String mOAuthToken;
 
 	private ViewPager mPager;
 
 //    private PagerAdapter mAdapter;
 
     private TabHost mTabHost;
     private TabsAdapter mTabsAdapter;
 
 	/** Called when the activity is first created. */
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.act_main);
 
 		mOAuthToken = PreferenceUtil.getOAuthToken(this);
 		if (mOAuthToken.equals("")) {
 			Intent intent = new Intent(MainActivity.this, OAuthAcitivity.class);
 			startActivityForResult(intent, 1);
 		} else {
             ((CheckInApplication)getApplication()).getApi().setoAuthToken(mOAuthToken);
 //            refresh();
             setupView();
             if (savedInstanceState != null) {
                 mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
             }
         }
         getSupportActionBar();
 
 
 
 	}
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag());
 //        super.onSaveInstanceState(outState);
     }
 
     private void setupView() {
 
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
         mPager = (ViewPager)findViewById(R.id.pager);
 
         mTabsAdapter = new TabsAdapter(this, mTabHost, mPager);
         mTabsAdapter.addTab(mTabHost.newTabSpec("NearBy").setIndicator("NearBy"), NavCheckinActivity.class, null);
         mTabsAdapter.addTab(mTabHost.newTabSpec("Friends").setIndicator("Friends"), NavFriendActivity.class, null);
         mTabsAdapter.addTab(mTabHost.newTabSpec("Me").setIndicator("Me"), NavMeActivity.class, null);
 //        mPager.setAdapter(mTabsAdapter);
 //        removeView();
 
 
 
 	}
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == 1 && resultCode == RESULT_OK) {
             if (data != null) {
                 Bundle bundle = data.getExtras();
                 mOAuthToken = bundle.getString(Constants.KEY_OAUTH_TOKEN);
                 if (mOAuthToken != null && !mOAuthToken.equals("")) {
                     ((CheckInApplication)getApplication()).getApi().setoAuthToken(mOAuthToken);
 //                    refresh();
                     setupView();
                 } else {
                     this.finish();
                 }
             }
         }
     }
 
     public static class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener{
 
         private final Context mContext;
         private final TabHost mTabHost;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
         static final class TabInfo{
             private final String tag;
             private final Class<?> cls;
             private final Bundle args;
 
             TabInfo(String _tag, Class<?> _class, Bundle _args) {
                 tag = _tag;
                 cls = _class;
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
 
         public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
             super(activity.getSupportFragmentManager());
             mContext = activity;
             mTabHost = tabHost;
             mViewPager = pager;
             mTabHost.setOnTabChangedListener(this);
             mViewPager.setAdapter(this);
             mViewPager.setOnPageChangeListener(this);
         }
 
         public void addTab(TabHost.TabSpec tabSpec, Class<?> cls, Bundle args) {
             tabSpec.setContent(new DummyTabFactory(mContext));
 
             String tag = tabSpec.getTag();
 
             TabInfo info = new TabInfo(tag, cls, args);
             mTabs.add(info);
             mTabHost.addTab(tabSpec);
             notifyDataSetChanged();;
         }
 
 
         @Override
         public int getCount() {
             return mTabs.size();
         }
 
         @Override
         public Fragment getItem(int i) {
             TabInfo info = mTabs.get(i);
             return Fragment.instantiate(mContext, info.cls.getName(), info.args);
 
 
 
         }
 
         @Override
         public void onPageScrolled(int i, float v, int i1) {
         }
 
         @Override
         public void onPageSelected(int i) {
             TabWidget widget = mTabHost.getTabWidget();
             int oldFocusability = widget.getDescendantFocusability();
             widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
             mTabHost.setCurrentTab(i);
             widget.setDescendantFocusability(oldFocusability);
         }
 
         @Override
         public void onPageScrollStateChanged(int i) {
         }
 
         @Override
         public void onTabChanged(String tabId) {
             int position = mTabHost.getCurrentTab();
             mViewPager.setCurrentItem(position);
         }
     }
 
 
 
 }
