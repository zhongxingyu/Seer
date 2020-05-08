 package com.se.cronus;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Service;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
 import com.se.cronus.Feeds.Feed;
 import com.se.cronus.Feeds.FeedAdapter;
 import com.se.cronus.backend.ItemGenerator;
 import com.se.cronus.items.ItemFragmentView;
 import com.se.cronus.items.TestFragView;
 import com.se.cronus.utils.CUtils;
 import com.se.cronus.utils.CronusApp;
 
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
 
 /***
  * 
  * @author dj
  * 
  */
 public class MainActivity extends /* Sliding */AbstractCActivity {
 	public static final boolean TESTING = true;
 
 	/* general stuff */
 	FrameLayout parent;
 	boolean ifTrue;
 	ListView FeedList;
 	public FeedAdapter feedAdapt;
 	List<Feed> FeedArray;
 	public boolean searchB;
 
 	// Keyboard stuff
 	InputMethodManager imm;
 
 	// HListView newFeedList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.se.cronus.R.layout.activity_main);
 		searchB = false;
 		reorderB = false;
 		/* THIS SECTION DEALS WITH FEED ADAPTERS AND STUFFS */
 
 		// general utilities for app
 		imm = (InputMethodManager) this
 				.getSystemService(Service.INPUT_METHOD_SERVICE);
 
 		this.profile.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
 		this.curAttatched.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
 
 		if (TESTING) {
 			setTestingFeedArray();
 		} else {
 
 			setFeedArray();
 		}
 
 		setUpListAdapter();
 		parent = (FrameLayout) findViewById(R.id.fragment_container);
 
 		this.parent.setBackgroundColor(Color.rgb(62, 83, 93));
 
 		/* THIS SECTION IS FOR INIT AND DESIGN STUFF */
 	}
 
 	public boolean changeItemFragment(ItemFragmentView newf) {
 		// TODO: check for valid View first
 		((ItemFragmentView) curAttatched.getMenu()).destroy();
 		setUpItemFragment(newf);
 		this.curAttatched.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
 		curAttatched.showMenu(true);
 		return true;
 	}
 
 	private void setUpListAdapter() {
 		FeedList = (ListView) findViewById(com.se.cronus.R.id.feedlist);
 		feedAdapt = new FeedAdapter(this, FeedArray,
 				com.se.cronus.R.id.feedlist); // newFeedList.getId());
 
 		FeedList.setAdapter(feedAdapt);
 		FeedList.setOnScrollListener(new OnScrollListener() {
 
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				// Algorithm to check if the last item is visible or not
 				final int lastItem = firstVisibleItem + visibleItemCount;
 				if (lastItem == totalItemCount) {
 					// you have reached end of list, load more data
 //					updateAllFeeds();
 				}
 				if (firstVisibleItem == 0) {
 					// you have reached beginning of list, load more data
 //					updateAllFeeds();
 				}
 			}
 
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				// blank, not using this
 				if(this.SCROLL_STATE_TOUCH_SCROLL == scrollState){
 //					Feed topViewFeed = null; //TODO: make a feed that is just a spinner
 //					feedAdapt.add(topViewFeed);//find a way to remove it
 					//view.getChildAt(0);
 					
 					return;
 				}
 			}
 		});
 
 	}
 
 	public void updateAllFeeds() {
 		System.out.println("updateAllFeeds");
 		for(int i = 0; i < feedAdapt.getCount(); i++){
 			Thread genthread = new Thread(feedAdapt.getItem(i).itemgen);
 			feedAdapt.getItem(i).itemgen.numItemsRequested(5);
 			genthread.run();
 		}
 	}
 

 
 	private void setFeedArray() {
 		// TODO create A feed list consisting of all of users current feeds;
 
 		FeedArray = null;
 	}
 
 	/**
 	 * 
 	 */
 	private void setTestingFeedArray() {
 		FeedArray = new ArrayList<Feed>();
 
 	}
 
 	// this is the back end methods for search bar that is yet to be
 	// implemented!
 	protected void onSearch(String toFind) {
 		for (int i = 0; i < feedAdapt.getCount(); i++) {
 			feedAdapt.getFeed(i).onSearch(toFind);
 		}
 		feedAdapt.resetItems();
 	}
 
 	protected void setUpActionBar() {
 		super.setUpActionBar();
 
 	}
 
 	protected void offSearch() {
 		for (int i = 0; i < feedAdapt.getCount(); i++) {
 			feedAdapt.getFeed(i).offSearch();
 		}
 		feedAdapt.resetItems();
 	}
 
 	public void addFeed(Feed f) {
 		feedAdapt.add(f);
 
 	}
 
 	protected void onSearchClick() {
 		// enable search
 		if (searchTextE.getVisibility() == View.GONE && !searchB) {
 			searchTextV.setText("");
 			searchTextE.setVisibility(View.VISIBLE);
 			searchTextE.setFocusableInTouchMode(true);
 			searchTextE.setFocusable(true);
 			searchTextE.requestFocus();
 			searchTextE.setSelected(true);
 			imm.showSoftInput(searchTextE, 0);
 			searchB = !searchB;
 			return;
 		}
 		// clear search
 		if (searchTextE.getVisibility() == View.GONE && searchB) {
 			searchTextV.setText("Cronus");
 			// searchTextV.setVisibility(View.GONE);
 			searchTextE.setText("");
 			searchTextE.setVisibility(View.GONE);
 			offSearch();
 			searchB = !searchB;
 			return;
 		}
 		// search
 		if (searchTextE.getVisibility() == View.VISIBLE && searchB) {
 			imm.hideSoftInputFromWindow(searchTextE.getWindowToken(), 0);
 			String toSearch = searchTextE.getText().toString().trim();
 			String title;
 			if (toSearch.length() > 1) {
 				int max = 32;
 				int min = 16;
 				if (toSearch.length() > max)
 					title = toSearch.substring(0, max);
 				else if (toSearch.length() < min) {
 					title = toSearch;
 					for (int i = toSearch.length(); i < min; i++)
 						title += " ";
 				} else {
 					title = toSearch;
 				}
 				searchTextV.setText(title);
 				searchTextV.setVisibility(View.VISIBLE);
 				searchTextE.setText("");
 				searchTextE.setVisibility(View.GONE);
 				// TODO: do search
 				onSearch(toSearch);
 				viewMain();
 
 				return;
 			} else {
 				searchTextV.setText("Cronus");
 				// searchTextV.setVisibility(View.GONE);
 				searchTextE.setText("");
 				searchTextE.setVisibility(View.GONE);
 				searchB = false;
 				return;
 			}
 
 		}
 	}
 
 	protected void onOpenItem() {
 		System.out.println("onOpenItem");
 		item.setBackgroundResource(com.se.cronus.R.drawable.navigation_previous_item);
 		profile.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
 
 		CUR = RIGHT;
 	}
 
 	// disable curitem
 	protected void onOpenProfile() {
 		System.out.println("onOpenProfile");
 		curAttatched.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
 
 		CUR = LEFT;
 	}
 
 	// enable both drares
 	protected void onOpenMain() {
 		System.out.println("onOpenMain");
 		item.setBackgroundResource(com.se.cronus.R.drawable.navigation_next_item);
 		profile.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 		curAttatched.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 		CUR = MAIN;
 	}
 
 	protected void setUpOnClicks() {
 		super.setUpOnClicks();
 	}
 
 	@Override
 	public void onClick(View v) {
 		super.onClick(v);
 		Feed newf = null;
 		switch (v.getId()) {
 		case R.id.test_profile_add_facebook:
 			newf = new Feed(this, CUtils.FACEBOOK_FEED);
 			feedAdapt.add(newf);
 			
 
 			break;
 		case R.id.test_profile_add_twitter:
 			newf = new Feed(this, CUtils.TWITTER_FEED);
 			feedAdapt.add(newf);
 			break;
 		case R.id.test_profile_add_pintrest:
 			newf = new Feed(this, CUtils.PINTREST_FEED);
 			feedAdapt.add(newf);
 			break;
 		case R.id.test_profile_add_insta:
 			newf = new Feed(this, CUtils.INSTA_FEED);
 			feedAdapt.add(newf);
 			break;
 		}
 		this.viewMain();
		this.updateAllFeeds();
 	}
 
 }
