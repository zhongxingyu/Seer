 package com.zotca.vbc.dbhistory;
 
 import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
 
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.view.MenuItem;
 
 public class CardViewActivity extends FragmentActivity {
 
 	public static final String ARG_ID = "id";
 	public static final String ARG_PAGE = "defaultPage";
 	
 	private int mId;
 	private ViewPager mViewPager;
 	private CardHistoryPagerAdapter mPagerAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mId = -1;
 		if (savedInstanceState != null) mId = savedInstanceState.getInt(ARG_ID, -1);
 		setContentView(R.layout.activity_database);
 		setupActionBar();
 		
 		Intent args = this.getIntent();
 		if (mId == -1) mId = args.getIntExtra(ARG_ID, 0);
 		final int page;
 		if (savedInstanceState != null) page = savedInstanceState.getInt(ARG_PAGE, -1);
 		else page = -1;
 		final long time = args.getLongExtra(ARG_PAGE, -1);
 		DatabaseFileManager.getManager(this,
 				new DatabaseFileManager.PostProcessHandler() {
 			
 			@Override
 			public void onPostProcess(DatabaseFileManager manager) {
 				mPagerAdapter = new CardHistoryPagerAdapter(
 						getSupportFragmentManager(), manager, mId);
 				mViewPager = (ViewPager) findViewById(R.id.pager);
 				mViewPager.setAdapter(mPagerAdapter);
 				if (page != -1)
 					mViewPager.setCurrentItem(page, false);
 				else if (time != -1)
 					mViewPager.setCurrentItem(mPagerAdapter.getIndexOf(time), false);
				setTitle(mPagerAdapter.getPageCardName(mViewPager.getCurrentItem()));
 				mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 
 					@Override
 					public void onPageScrollStateChanged(int arg0) {
 					}
 
 					@Override
 					public void onPageScrolled(int arg0, float arg1, int arg2) {
 					}
 
 					@Override
 					public void onPageSelected(int position) {
						setTitle(mPagerAdapter.getPageCardName(position));
 					}
 					
 				});
 			}
 		});
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(ARG_ID, mId);
 		outState.putInt(ARG_PAGE, mViewPager.getCurrentItem());
 	}
 	
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
