 package com.intro.compintro;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
 
 public class BaseActivity extends SlidingFragmentActivity {
 
 	private int mTitleRes;
 	protected ColorMenuFragment mFrag;
 
 	public BaseActivity(int titleRes) {
 		mTitleRes = titleRes;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setTitle(mTitleRes);
 
 		// customize the SlidingMenu
 		SlidingMenu sm = getSlidingMenu();
 		sm.setMode(SlidingMenu.LEFT_RIGHT);
 		sm.setShadowWidthRes(R.dimen.shadow_width);
 		sm.setShadowDrawable(R.drawable.shadow);
 		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
 		sm.setFadeDegree(0.35f);
 		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		// set the Behind View
 		setBehindContentView(R.layout.menu_frame);
 		if (savedInstanceState == null) {
 			FragmentTransaction t = this.getSupportFragmentManager()
 					.beginTransaction();
 			mFrag = new ColorMenuFragment();
 			t.replace(R.id.menu_frame, mFrag);
 			t.commit();
 		} else {
 			mFrag = (ColorMenuFragment) this.getSupportFragmentManager()
 					.findFragmentById(R.id.menu_frame);
 		}
 
 		// set the secondaryMenu
 		getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_two);
 		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.menu_frame_two, new SampleListFragment())
 				.commit();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle();
 			return true;
 		case 1:
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}

 }
