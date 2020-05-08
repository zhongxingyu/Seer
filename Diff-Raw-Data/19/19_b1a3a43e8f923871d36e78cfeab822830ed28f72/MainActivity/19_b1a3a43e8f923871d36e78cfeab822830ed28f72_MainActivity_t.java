 package com.omerjerk.cheatbox;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
 import com.omerjerk.cheatbox.fragments.mainFragment;
 
 public class MainActivity extends SlidingFragmentActivity {
 
 	protected ListFragment mFrag;
 	private Fragment mContent;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) { 
 		super.onCreate(savedInstanceState);
 		
 		
 		
 		mFrag = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
 		
 		// set the Above View
 				if (savedInstanceState != null)
 					mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
 				if (mContent == null)
 					mContent = new mainFragment();	
 
 				// set the Above View
 				setContentView(R.layout.content_frame);
 				getSupportFragmentManager()
 				.beginTransaction()
 				.replace(R.id.content_frame, mContent)
 				.commit();
 
 				// set the Behind View
 				setBehindContentView(R.layout.menu_frame);
 				getSupportFragmentManager()
 				.beginTransaction()
 				.replace(R.id.menu_frame, new ColorMenuFragment())
 				.commit();
 		
 		
 		
 				SlidingMenu sm = getSlidingMenu();
 				sm.setShadowWidthRes(R.dimen.shadow_width);
 				sm.setShadowDrawable(R.drawable.shadow);
 				sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
 				sm.setFadeDegree(0.35f);
 				sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 				setSlidingActionBarEnabled(false);
 				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		
 	}
 	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
 	public void switchContent(Fragment fragment) {
 		mContent = fragment;
 		getSupportFragmentManager()
 		.beginTransaction()
 		.replace(R.id.content_frame, fragment)
 		.commit();
 		getSlidingMenu().showContent();
 	}
 
 
 }
