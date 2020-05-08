 package com.ese2013.mensaunibe;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBar.Tab;
 import android.support.v7.app.ActionBarActivity;
 
 import com.ese2013.mensaunibe.model.Model;
 
 public class MenuActivity extends ActionBarActivity implements ActionBar.TabListener
 {
 	private int mMensaId;
 	TabCollectionPagerAdapter mTabCollectionPagerAdapter;
 	ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu_list);
 
 		mMensaId = getIntent().getIntExtra("int_value", 0);
 		setTitle( Model.getInstance().getMensaById(mMensaId).getName() );
 
 		// swipe
 		mTabCollectionPagerAdapter = new TabCollectionPagerAdapter(getSupportFragmentManager());
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mTabCollectionPagerAdapter);
 		mViewPager.setOnPageChangeListener(
 				new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						// When swiping between pages, select the
 						// corresponding tab.
 						getSupportActionBar().setSelectedNavigationItem(position);
 					}
 				});
 		
 		//setup actionbar 
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mTabCollectionPagerAdapter.getCount(); i++) {
 			actionBar.addTab(actionBar.newTab()
 					.setText(mTabCollectionPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	@Override
 	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
 		mViewPager.setCurrentItem(tab.getPosition());
 		//ft.replace(R.id.menu_fragment_container, fragment);		
 	}
 
 	@Override
 	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public class TabCollectionPagerAdapter extends FragmentPagerAdapter {
 		private static final int FRAGMENT_COUNT = 2;
 
 		public TabCollectionPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public int getCount() {
 			return FRAGMENT_COUNT;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			String tabTitle = "";
 			// have to think about some more relevant title
 			// and/or maybe method
 			switch (position) {
 			case 0:
 				tabTitle = getString(R.string.today);
 				break;
 			case 1:
 				tabTitle = getString(R.string.comingDaysTabTitle);
 				break;
 			}
 			return tabTitle;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Fragment fragment = null;
 			Bundle args = new Bundle();
 
 			switch (position) {
 			case 0:
 				fragment = new CurrentDayMenuFragment();
 				args.putInt(CurrentDayMenuFragment.MENSA_ID, mMensaId);
 				break;
 			case 1:
 				fragment = new ComingDaysMenuFragment();
 				args.putInt(ComingDaysMenuFragment.MENSA_ID, mMensaId);
 				break;
 			}
 			fragment.setArguments(args);
 			return fragment;
 		}
 	}
 }
