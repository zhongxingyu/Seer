 package com.warfactory.dpscalc;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
	private SectionsPagerAdapter sectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
	private ViewPager viewPager;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getApplicationContext());
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Set up the ViewPager with the sections adapter.
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(sectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab.
 		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				actionBar.setSelectedNavigationItem(position);
 			}
 		});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter.
 			// Also specify this Activity object, which implements the
 			// TabListener interface, as the
 			// listener for when this tab is selected.
 			actionBar.addTab(actionBar.newTab().setText(sectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO create a help/about page from here
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		// return true;
 		return false;
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// nothing to do
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		viewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// nothing to do
 	}
 
 }
