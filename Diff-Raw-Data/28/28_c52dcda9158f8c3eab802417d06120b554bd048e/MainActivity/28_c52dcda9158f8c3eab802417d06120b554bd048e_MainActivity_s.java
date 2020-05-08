 package com.mates120.myword.ui;
 
 
 import com.mates120.myword.AvailableDictionaries;
 import com.mates120.myword.R;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener{
 	
 	private SectionsPagerAdapter mSectionsPagerAdapter;
 	private ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
 
         actionBar.setHomeButtonEnabled(false);
 
         // Specify that we will be displaying tabs in the action bar.
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager, attaching the adapter and setting up a listener for when the
         // user swipes between sections.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When swiping between different app sections, select the corresponding tab.
                 // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                 // Tab.
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         actionBar.addTab(actionBar.newTab()
                         .setText("Search")
                         .setTabListener(this));
         actionBar.addTab(actionBar.newTab()
                         .setText("Settings")
                         .setTabListener(this));
 	}
	
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		AvailableDictionaries.getInstance(this).refreshList();
 	};
 	
 	@Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 }
