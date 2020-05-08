 
 package com.tikal.share;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.FragmentTransaction;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.cacheyoutubedata.PreferencesDataCacheStore;
 import com.example.cacheyoutubedata.YouTubeDataCacher;
 import com.tikal.share.youtube.LookupChannel;
 import com.tikal.share.youtube.YoutubePlaylist;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	PlayListPagerAdapter mPlaylistPagerAdapter;
 	ViewPager mViewPager;
 
 	private YouTubeDataCacher myYTDC = null;
 	private PreferencesDataCacheStore myPDCS = null;
 	private static String myCacheID = "my_youtube_cache";
 
 	private int PLAYLIST_COUNT = 3;
 	public static final String DATA_UPDATE = "data_update";
 	private IntentFilter filter = new IntentFilter(DATA_UPDATE);
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Create cache data store
 		myPDCS = new PreferencesDataCacheStore(this);
 		myYTDC = new YouTubeDataCacher(myPDCS);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		// actionBar.
 
 		new myAsyncTask().execute();
 	}
 
 	class myAsyncTask extends AsyncTask<Void, Void, List<YoutubePlaylist>> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			// Start Animation
 			List<YoutubePlaylist> list = (List<YoutubePlaylist>) myYTDC.unchacheThis(myCacheID);
 			if (list != null) {
 				onUpdateRecieve(getActionBar(), list);
 				addActionbarTabs(getActionBar());
 			}
 		}
 
 		protected void onPostExecute(List<YoutubePlaylist> result) {
 			// Stop Animation
 			Toast.makeText(getApplicationContext(), "blal", 1).show();
 			// Parse the data
 			PLAYLIST_COUNT = result.size();
 			onUpdateRecieve(getActionBar(), result);
 			addActionbarTabs(getActionBar());
 		}
 
 		@Override
 		protected List<YoutubePlaylist> doInBackground(Void... params) {
 			LookupChannel lookup = new LookupChannel(false);
 			SharedPreferences sharedPreferences = PreferenceManager
 					.getDefaultSharedPreferences(MainActivity.this);
 			String userName = sharedPreferences.getString("userName",
 					"androiddev101");
 			boolean downloadThumbnail = sharedPreferences.getBoolean("downloadThumbnail", true);
 			List<YoutubePlaylist> list = lookup.getFullListByUser(userName, downloadThumbnail);
 			myYTDC.cacheThis(myCacheID, list);
 			return list;
 		}
 
 	}
 
 	/**
 	 * @param actionBar
 	 * @param result
 	 */
 	private void onUpdateRecieve(final ActionBar actionBar,
 			List<YoutubePlaylist> result) {
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mPlaylistPagerAdapter = new PlayListPagerAdapter(
 				getSupportFragmentManager(), result);
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mPlaylistPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 	}
 
 	/**
 	 * @param actionBar
 	 */
 	private void addActionbarTabs(final ActionBar actionBar) {
 		actionBar.removeAllTabs();
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mPlaylistPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mPlaylistPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(datareceiver, filter);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(datareceiver);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 
 			case R.id.action_refresh:
			new myAsyncTask().execute();
 				// Add tab action
 				/*
 				 * PLAYLIST_COUNT = 4; mViewPager.setAdapter(null); mPlaylistPagerAdapter = new
 				 * PlayListPagerAdapter(getSupportFragmentManager()); mViewPager.setAdapter(mPlaylistPagerAdapter); //
 				 * mPlaylistPagerAdapter.notifyDataSetChanged(); mViewPager.invalidate();
 				 * addActionbarTabs(getSupportActionBar());
 				 */
 				break;
 			case R.id.action_settings:
 				startActivity(new Intent(this, AppPreferenceFragment.class));
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
 	 */
 	public class PlayListPagerAdapter extends FragmentPagerAdapter {
 
 		private List<YoutubePlaylist> list;
 
 		public PlayListPagerAdapter(FragmentManager fm,
 				List<YoutubePlaylist> list) {
 			super(fm);
 			this.list = list;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new MyListFragment(
 					list.get(position));
 			return fragment;
 		}
 
 		@Override
 		public long getItemId(int position) {
 
 			return super.getItemId(position);
 
 			/*
 			 * switch (position) { case 0: case 1: case 2: return super.getItemId(position); case 3: return 4; }
 			 */
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			return super.getItemPosition(object);
 		}
 
 		@Override
 		public int getCount() {
 			return PLAYLIST_COUNT;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return list.get(position).getTitle();
 		}
 	}
 
 	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			Toast.makeText(getApplicationContext(), "received",
 					Toast.LENGTH_SHORT);
 			// onUpdateRecieve(getSupportActionBar());
 			addActionbarTabs(getActionBar());
 
 		}
 	};
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
