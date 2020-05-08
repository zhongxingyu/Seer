 package com.carolineleung.clickcontrols.content;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.carolineleung.clickcontrols.R;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 public class BaseActivity extends SlidingFragmentActivity {
 
 	private int mTitle;
 	protected ListFragment mFragment;
 
 	public BaseActivity(int titleRes) {
 		mTitle = titleRes;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setTitle(mTitle);
 
 		// set the Behind View
 		setBehindContentView(R.layout.menu_frame);
 		FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
 		mFragment = new SampleListFragment();
 		fragmentTransaction.replace(R.id.menu_frame, mFragment);
 		fragmentTransaction.commit();
 
 		// customize the SlidingMenu
 		SlidingMenu sm = getSlidingMenu();
 		sm.setShadowWidthRes(R.dimen.shadow_width);
 		sm.setShadowDrawable(R.drawable.shadow);
 		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
 		sm.setFadeDegree(0.35f);
 		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle();
 			return true;
 		case R.id.github:
			Uri uriUrl = Uri.parse("http://branchout.com");
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			this.getApplicationContext().startActivity(launchBrowser);

 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public class BasePagerAdapter extends FragmentPagerAdapter {
 		private List<Fragment> mFragments = new ArrayList<Fragment>();
 		private ViewPager mPager;
 
 		public BasePagerAdapter(FragmentManager fm, ViewPager vp) {
 			super(fm);
 			mPager = vp;
 			mPager.setAdapter(this);
 			for (int i = 0; i < 3; i++) {
 				addTab(new SampleListFragment());
 			}
 		}
 
 		public void addTab(Fragment frag) {
 			mFragments.add(frag);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			return mFragments.get(position);
 		}
 
 		@Override
 		public int getCount() {
 			return mFragments.size();
 		}
 	}
 
 }
