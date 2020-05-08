 package com.se.cronus;
 
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
 import com.se.cronus.Feeds.Feed;
 import com.se.cronus.items.ItemDoc;
 import com.se.cronus.items.ItemFragmentView;
 import com.se.cronus.items.TestFragView;
 import com.se.cronus.utils.CUtils;
 import com.se.cronus.utils.CronusApp;
 
 import android.R;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Service;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /************************************************************************
 Copyright 2012 Jeremy Feinstein
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **************************************************************************/
 
 /*
  * This will be extended by every activity in this app
  */
 public abstract class AbstractCActivity extends FragmentActivity implements
 OnClickListener {
 
 	/* for fragments */
 	protected final int MAIN = 0;
 	protected final int LEFT = 1;
 	protected final int RIGHT = 2;
 	protected int CUR;
 
 	// sliding menues
 	public SlidingMenu profile;
 	public SlidingMenu curAttatched;
 
 	// action bar stuff
 	protected ActionBar act;
 	protected ImageView item;
 	protected ImageView search;
 	protected ImageView refresh;
 	protected EditText searchTextE;
 	protected TextView searchTextV;
 	
 	public boolean reorderB;
 
 	FragmentTransaction ft;
 
 	// Keyboard stuff
 	InputMethodManager imm;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.se.cronus.R.layout.activity_abstract_c);
 
 		/* THIS SECTION DEALS WITH FRAGMENT HANDLING */// /however it doesnt
 														// work right now.
 		CUR = MAIN;
 
 		// set up list Adapter
 		setUpProfile();
 		ItemDoc faker = new ItemDoc();
 		setUpActionBar();
 		setUpItemFragment(new TestFragView(faker, this));
 		
 		// set onclicks
 		setUpOnClicks();
 
 
 	}
 
 
 	/**
 	 * 
 	 */
 	protected void setUpOnClicks() {
 		// extract soon
 		item.setOnClickListener(this);
 		search.setOnClickListener(this);
 		refresh.setOnClickListener(this);
 		searchTextV.setClickable(true);
 		searchTextV.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction() != KeyEvent.ACTION_DOWN)
 					return false;
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					// your necessary codes...
 					onSearchClick();
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_SEARCH) {
 					// your necessary codes...
 					onSearchClick();
 					return true;
 				}
 				return false;
 			}
 		});
 		searchTextV.setOnClickListener(this);
 	}
 	
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public boolean onMenuOpened(int featureId, Menu menu) {
 		// TODO Auto-generated method stub
 		if (menu.equals(profile)
 				|| ((SlidingMenu) menu).getId() == profile.getId()) {
 			System.out.print("PROFILE OPENED");
 
 		}
 
 		return super.onMenuOpened(featureId, menu);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	/**
 	 * 
 	 */
 
 	/**
 	 * set up array that saves past right fragments
 	 */
 	protected void setUpItemFragment(ItemFragmentView v) {
 
 		curAttatched = new SlidingMenu(this);
 		curAttatched.setMode(SlidingMenu.RIGHT);
 		curAttatched.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 		curAttatched.setShadowWidthRes(com.se.cronus.R.dimen.shadow_width);
 		// menu.setShadowDrawable(R.drawable.shadow);
 		curAttatched.setFadeDegree(0.35f);
 		// curAttatched.attachToActivity(this, //attatched with onclick
 		// SlidingMenu.SLIDING_CONTENT);
		if(v.getParent() != null)
 			((ViewGroup)v.getParent()).removeView(v);
 		curAttatched.setMenu(v);
 //		curAttatched
 //				.setBehindOffsetRes(com.se.cronus.R.dimen.slidingmenu_offset);
 
 //		curAttatched.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
 		curAttatched.setOnOpenedListener(new OnOpenedListener() {
 
 			@Override
 			public void onOpened() {
 				// TODO Auto-generated method stub
 				onOpenItem();
 			}
 
 		});
 		
 		curAttatched.setOnClosedListener(new OnClosedListener() {
 
 			@Override
 			public void onClosed() {
 				// TODO Auto-generated method stub
 				onOpenMain();
 			}
 
 		});
 	}
 
 	
 
 	/**
 	 * sets up left hand side
 	 */
 	protected void setUpProfile() {
 		profile = new SlidingMenu(this);
 		profile.setSlidingEnabled(true);
 		profile.setMode(SlidingMenu.LEFT);
 		profile.setMenu(com.se.cronus.R.layout.fragment_left);
 
 		profile.setShadowWidthRes(com.se.cronus.R.dimen.shadow_width);
 		// profile.setShadowDrawable(R.drawable.shadow);
 		profile.setBehindOffsetRes(com.se.cronus.R.dimen.slidingmenu_offset);
 //		profile.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
 		// profile.setBehindWidth(30);
 		profile.setBehindScrollScale(0.25f);
 		profile.setFadeDegree(0.35f);
 		profile.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 		profile.setBackgroundColor(Color.RED);// CUtils.CRONUS_BLUE_WHITE);
 		profile.setOnOpenedListener(new OnOpenedListener() {
 
 			@Override
 			public void onOpened() {
 				onOpenProfile();
 			}
 
 		});
 		profile.setOnClosedListener(new OnClosedListener() {
 
 			@Override
 			public void onClosed() {
 				onOpenMain();
 			}
 
 		});
 
 		profile.findViewById(com.se.cronus.R.id.testtestclick).setOnClickListener(
 				new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						//profileShowTest();
 					}
 
 				});
 		profile.getMenu().findViewById(com.se.cronus.R.id.test_profile_add_facebook).setOnClickListener(this);
 		profile.getMenu().findViewById(com.se.cronus.R.id.test_profile_add_twitter).setOnClickListener(this);
 		profile.getMenu().findViewById(com.se.cronus.R.id.test_profile_add_pintrest).setOnClickListener(this);
 		profile.getMenu().findViewById(com.se.cronus.R.id.test_profile_add_insta).setOnClickListener(this);
 		
 	}
 
 	// sloppy floppy, but It'll work dawg
 	protected void profileShowTest() {
 		Intent iinent = new Intent(this, ProfleActivity.class);
 		startActivity(iinent);
 	}
 
 	protected void setUpActionBar() {
 		ImageView icon = new ImageView(this);
 		icon.setBackgroundResource(com.se.cronus.R.drawable.temp_cronos_logo);
 		act = this.getActionBar();
 		act.setBackgroundDrawable(new ColorDrawable(CUtils.CRONUS_GREEN_DARK));
 		act.setIcon(com.se.cronus.R.drawable.temp_cronos_logo);
 		act.setCustomView(com.se.cronus.R.layout.action_bar);
 		act.setDisplayHomeAsUpEnabled(true);
 		act.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
 				| ActionBar.DISPLAY_SHOW_HOME);
 		((ViewGroup)act.getCustomView().getParent()).addView(icon);
 		// extra icons
 		search = (ImageView) findViewById(com.se.cronus.R.id.action_search_b);
 		refresh = (ImageView) findViewById(com.se.cronus.R.id.action_refresh);
 		searchTextE = (EditText) findViewById(com.se.cronus.R.id.action_search_et);
 		searchTextV = (TextView) findViewById(com.se.cronus.R.id.action_search_tv);
 		item = (ImageView) findViewById(com.se.cronus.R.id.action_item);
 
 		searchTextE.setTextColor(Color.WHITE);
 		searchTextE.setTextSize(15);
 		searchTextE.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
 
 		searchTextV.setTextColor(Color.WHITE);
 		searchTextV.setTextSize(15);
 		
 		ImageView logo = (ImageView) findViewById(com.se.cronus.R.id.temp_cronos_logo);
 		logo.setLayoutParams(new RelativeLayout.LayoutParams(act.getHeight(),act.getHeight()));
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			switch (CUR) {
 			case RIGHT:
 				viewMain();
 				return true;
 			case MAIN:
 				viewProfile();
 				return true;
 			case LEFT:
 				viewMain();
 				return true;
 			}
 
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		// getMenuInflater().inflate(R.layout.action_bar, menu);
 		act.setBackgroundDrawable(new ColorDrawable(CUtils.CRONUS_GREEN_DARK));
 		int h = act.getHeight();
 		int w = act.getHeight();// make it a squar
 
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case com.se.cronus.R.id.action_item:
 			switch (CUR) {
 			case MAIN:
 				viewCurItem();
 				return;
 			case LEFT:
 				viewMain();
 				return;
 			case RIGHT:
 				viewMain();
 				return;
 			}
 			return;
 		case com.se.cronus.R.id.action_search_b:
 			onSearchClick();
 			return;
 		case com.se.cronus.R.id.action_search_tv:
 			onSearchClick();
 			return;
 		case com.se.cronus.R.id.action_refresh:
 			updateAllFeeds();
 			return;
 		}
 
 	}
 
 	
 	protected abstract void updateAllFeeds();
 
 	// this section is to help with save state
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 	
 		super.onResume();
 	}
 
 	// This is going to handle activating buttons and all that
 	// disable profile
 	protected abstract void onOpenItem();
 
 	// disable curitem
 	protected abstract void onOpenProfile();
 
 	// enable both drares
 	protected abstract void onOpenMain();
 
 	public void viewProfile() {
 		profile.showMenu(true);
 		// ((ViewGroup)profile.getParent()).removeView(profile);
 
 	}
 
 	public void viewCurItem() {
 		item.setBackgroundResource(com.se.cronus.R.drawable.navigation_previous_item);
 		// go Right
 
 		curAttatched.showMenu(true);
 		if (!curAttatched.isMenuShowing()) {
 			System.out.println("Item didn't show, trying again");
 		}
 
 	}
 
 	protected void viewMain() {
 		// goleft
 		if (curAttatched.isMenuShowing()) {
 			item.setBackgroundResource(com.se.cronus.R.drawable.navigation_next_item);
 			curAttatched.showContent(true);
 		}
 		if (profile.isMenuShowing()) {
 			profile.showContent(true);
 		}
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		if(reorderB == true){
 			act.getCustomView().findViewById(com.se.cronus.R.id.action_done).performClick();
 			return;
 		}
 		if (CUR == MAIN)
 			super.onBackPressed();
 		else
 			viewMain();
 		
 	}
 	
 	protected abstract void onSearchClick();
 	protected abstract void offSearch();
 	protected abstract void onSearch(String toFind);
 
 
 	public abstract boolean changeItemFragment(ItemFragmentView testFragView);
 }
