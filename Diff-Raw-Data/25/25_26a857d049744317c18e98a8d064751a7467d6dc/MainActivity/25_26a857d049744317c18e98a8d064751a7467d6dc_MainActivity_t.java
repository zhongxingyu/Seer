 package com.johandahlberg.podr.ui;
 
 import java.util.ArrayList;
 
 import com.johandahlberg.podr.R;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener,
 		SubscriptionListFragment.OnSubscriptionSelectedListener {
 	private static final String LOG_TAG = ".ui.MainActivity";
 
 	private int currentSubscription = -1;
 
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
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections
 		// of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab.
 		// We can also use ActionBar.Tab#select() to do this if we have a
 		// reference to the
 		// Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter.
 			// Also specify this Activity object, which implements the
 			// TabListener interface, as the
 			// listener for when this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		/*
 		 * PodrBackupHelper backupHelper = new PodrBackupHelper(
 		 * getApplicationContext());
 		 */
 
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		case R.id.menu_newsubscription: 
 			intent = new Intent(this, NewSubscriptionActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			MainActivity.this.startActivity(intent); 
 			return true; 
 			/*case
 			 * R.id.menu_refresh: intent = new Intent(this,
 			 * UpdateService.class); startService(intent); return true; case
 			 * R.id.menu_export: backupHelper.backup(); return true; case
 			 * R.id.menu_import: backupHelper.restore(); return true; case
 			 * R.id.menu_about: intent = new Intent(this, AboutActivity.class);
 			 * startActivity(intent); return true; case R.id.menu_settings:
 			 * intent = new Intent(this, SettingsActivity.class);
 			 * startActivity(intent); return true;
 			 */
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onSubscriptionSelected(int subId) {
 		/*
 		 * EpisodeListFragment episodeList = (EpisodeListFragment)
 		 * getSupportFragmentManager() .findFragmentById(R.id.frag_episodelist);
 		 * 
 		 * currentSubscription = subId;
 		 * 
 		 * if (episodeList == null || !episodeList.isInLayout()) {
 		 */
 		Intent intent = new Intent(getApplicationContext(),
 				EpisodeListActivity.class);
 		intent.putExtra("subId", subId);
 		startActivity(intent);
 		/*
 		 * } else { episodeList.update(currentSubscription); }
 		 */
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the primary sections of the app.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			Fragment fragment = null;
 			switch (i) {
			case 0:
 				fragment = new SubscriptionListFragment();
				break;
			case 1:
 				fragment = new DummySectionFragment();
 				Bundle args = new Bundle();
 				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
 				fragment.setArguments(args);
				break;
 			}
 
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.title_activity_main_subscriptions)
 						.toUpperCase();
 			case 1:
 				return getString(R.string.title_activity_main_downloads)
 						.toUpperCase();
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		public DummySectionFragment() {
 		}
 
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			TextView textView = new TextView(getActivity());
 			textView.setGravity(Gravity.CENTER);
 			Bundle args = getArguments();
 			textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
 			return textView;
 		}
 	}
 }
