 package com.TweetGet.Managers;
 
 import android.app.Activity;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.TweetGet.R;
import com.TweetGet.Fragments.BaseFeedFragment;
 import com.TweetGet.Fragments.MainFeedFragment;
 import com.TweetGet.Fragments.TimelineFragment;
 
 public class DrawerManager {
 
 	private String[] mDrawerOptions;
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mActionBarDrawerToggle;
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private Fragment mFragment;
 	private FragmentManager mSuppourtFragmentManger;
 	private Activity mActivity;
 
 	private static final int MAIN_FEED_FRAGMENT_POSITION = 0;
 	private static final int ACCOUNT_SEARCH_FRAGMENT_POSITION = 1;
 
 	public DrawerManager(final Activity activity, FragmentManager fm) {
 
 		mActivity = activity;
 		mSuppourtFragmentManger = fm;
 		mTitle = mDrawerTitle = activity.getTitle();
 		mDrawerOptions = activity.getResources().getStringArray(
 				R.array.nav_drawer_menu_options);
 		mDrawerLayout = (DrawerLayout) activity
 				.findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);
 
 		mDrawerList.setAdapter(new ArrayAdapter<String>(activity,
 				R.layout.drawer_list_item, mDrawerOptions));
 
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		setupActionBarDrawerToggle(activity);
 
 		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
 		activity.getActionBar().setHomeButtonEnabled(true);
 		mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
 		mDrawerList.setHeaderDividersEnabled(true);
 
 		selectItem(MAIN_FEED_FRAGMENT_POSITION, 3);
 	}
 
 	private void setupActionBarDrawerToggle(final Activity activity) {
 		mActionBarDrawerToggle = new ActionBarDrawerToggle(activity,
 				mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
 				R.string.drawer_close) {
 
 			public void onDrawerClosed(View view) {
 				activity.getActionBar().setTitle(mTitle);
 				activity.invalidateOptionsMenu();
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				activity.getActionBar().setTitle(mDrawerTitle);
 				activity.invalidateOptionsMenu();
 			}
 		};
 	}
 
 	public void syncActionBarToggleState() {
 		mActionBarDrawerToggle.syncState();
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return (mActionBarDrawerToggle.onOptionsItemSelected(item));
 	}
 
 	public ActionBarDrawerToggle getActionBarDrawerToggle() {
 		return mActionBarDrawerToggle;
 	}
 
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 
 		protected int currentPosition;
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 
 			selectItem(position, currentPosition);
 
 		}
 	}
 	
 	public Fragment getSelectedFragment()
 	{
 		return mFragment;
 	}
 
 	private void selectItem(int position, int currentPosition) {
 
 		String tag = "";
 		switch (position) {
 		case MAIN_FEED_FRAGMENT_POSITION:
 			mFragment = new MainFeedFragment();
 			currentPosition = MAIN_FEED_FRAGMENT_POSITION;
			tag = "HASHTAG";
 			break;
 		case ACCOUNT_SEARCH_FRAGMENT_POSITION:
 			mFragment = new TimelineFragment();
 			currentPosition = ACCOUNT_SEARCH_FRAGMENT_POSITION;
			tag = "USER";
 			break;
 
 		}
 
 		if (mFragment != null) {
 			mSuppourtFragmentManger.beginTransaction()
 					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
 					.replace(R.id.content_frame, mFragment, tag).commit();
 		}
 		mDrawerList.setItemChecked(position, true);
 		mActivity.setTitle(mDrawerOptions[position]);
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 
 }
