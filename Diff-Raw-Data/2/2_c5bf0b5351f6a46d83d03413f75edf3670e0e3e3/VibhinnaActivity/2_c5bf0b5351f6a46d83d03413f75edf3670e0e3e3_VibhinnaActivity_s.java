 package com.vibhinna.binoy;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class VibhinnaActivity extends SherlockFragmentActivity {
 	protected static final String ACTION_PROGRESS_UPDATE = "com.vibhinna.binoy.intent.action.ACTION_PROGRESS_UPDATE";
 	protected static final String ACTION_NEW_TASK = "com.vibhinna.binoy.intent.action.ACTION_NEW_TASK";
 
 	ViewPager mViewPager;
 	TabsAdapter mTabsAdapter;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.pager);
 
 		setContentView(mViewPager);
 		ActionBar bar = getSupportActionBar();
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		mTabsAdapter = new TabsAdapter(this, mViewPager);
 		mTabsAdapter.addTab(
 				bar.newTab().setText(getString(R.string.list_tab_title)),
 				VibhinnaFragment.class, null);
 		mTabsAdapter.addTab(
 				bar.newTab().setText(getString(R.string.list_tab_tasks)),
 				TasksQueueFragment.class, null);
 		mTabsAdapter.addTab(
 				bar.newTab().setText(getString(R.string.list_tab_info)),
 				SystemInfoFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setText("Tasks"),
				TasksQueueFragment.class, null);
 	}
 
 	public static class TabsAdapter extends FragmentPagerAdapter implements
 			ActionBar.TabListener, ViewPager.OnPageChangeListener {
 		private final Context mContext;
 		private final ActionBar mActionBar;
 		private final ViewPager mViewPager;
 		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
 		static final class TabInfo {
 			private final Class<?> clss;
 			private final Bundle args;
 
 			TabInfo(Class<?> _class, Bundle _args) {
 				clss = _class;
 				args = _args;
 			}
 		}
 
 		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
 			super(activity.getSupportFragmentManager());
 			mContext = activity;
 			mActionBar = activity.getSupportActionBar();
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
 			return Fragment.instantiate(mContext, info.clss.getName(),
 					info.args);
 		}
 
 		@Override
 		public void onPageScrolled(int position, float positionOffset,
 				int positionOffsetPixels) {
 		}
 
 		@Override
 		public void onPageSelected(int position) {
 			mActionBar.setSelectedNavigationItem(position);
 		}
 
 		@Override
 		public void onPageScrollStateChanged(int state) {
 		}
 
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			Object tag = tab.getTag();
 			for (int i = 0; i < mTabs.size(); i++) {
 				if (mTabs.get(i) == tag) {
 					mViewPager.setCurrentItem(i);
 				}
 			}
 		}
 
 		@Override
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		}
 
 		@Override
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		}
 	}
 
 }
