 /*
  * Copyright 2012 Feedlr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.chalmers.feedlr.activity;
 
 import java.util.ArrayList;
 
 import com.chalmers.feedlr.R;
 import com.chalmers.feedlr.adapter.PageAdapter;
 import com.chalmers.feedlr.adapter.UserAdapter;
 import com.chalmers.feedlr.client.Clients;
 import com.chalmers.feedlr.client.ClientHandler;
 import com.chalmers.feedlr.database.DatabaseHelper;
 import com.chalmers.feedlr.service.DataServiceHelper;
 import com.chalmers.feedlr.listener.AuthListener;
 import com.chalmers.feedlr.listener.FeedListener;
 import com.chalmers.feedlr.model.Feed;
 import com.viewpagerindicator.CirclePageIndicator;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.PixelFormat;
 import android.graphics.Typeface;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewAnimator;
 import android.widget.ViewFlipper;
 
 public class FeedActivity extends FragmentActivity implements FeedListener {
 
 	private DataServiceHelper feedService;
 	private ClientHandler clientHandler;
 
 	private DatabaseHelper db;
 
 	// Twitter strings
 	public static final String TWITTER_USERS_UPDATED = "com.chalmers.feedlr.TWITTER_USERS_UPDATED";
 	public static final String TWITTER_USERS_PROBLEM_UPDATING = "com.chalmers.feedlr.TWITTER_USERS_PROBLEM_UPDATING";
 
 	// Facebook strings
 	public static final String FACEBOOK_TIMELINE_UPDATED = "com.chalmers.feedlr.FACEBOOK_TIMELINE_UPDATED";
 	public static final String FACEBOOK_USERS_UPDATED = "com.chalmers.feedlr.FACEBOOK_USERS_UPDATED";
 	public static final String FACEBOOK_USERS_PROBLEM_UPDATING = "com.chalmers.feedlr.FACEBOOK_USERS_PROBLEM_UPDATING";
 	public static final String FACEBOOK_USER_NEWS_UPDATED = "com.chalmers.feedlr.FACEBOOK_USER_NEWS_UPDATED";
 
 	public static final String FEED_UPDATED = "com.chalmers.feedlr.FEED_UPDATED";
 	public static final String FEED_PROBLEM_UPDATING = "com.chalmers.feedlr.FEED_PROBLEM_UPDATING";
 	public static final String NO_CONNECTION = "com.chalmers.feedlr.NO_CONNECTION";
 
 	// Android system helpers
 	private Resources res;
 	private LocalBroadcastManager lbm;
 	private LayoutInflater inflater;
 	private IntentFilter intentFilter;
 
 	// Adapters
 	private PageAdapter feedAdapter;
 	private UserAdapter userAdapter;
 
 	// Views
 	private ViewFlipper mainViewFlipper;
 	private ViewPager feedViewSwiper;
 	private ViewAnimator settingsViewFlipper;
 	private ListView userListView;
 	private LinearLayout userListLayout;
 
 	private Button facebookAuthButton;
 	private Button twitterAuthButton;
 
 	private TextView feedTitleTextView;
 
 	// Animations
 	private Animation slideOutLeft;
 	private Animation slideOutRight;
 	private Animation slideInLeft;
 	private Animation slideInRight;
 
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String broadcast = intent.getAction();
 
 			if (broadcast.equals(TWITTER_USERS_UPDATED)) {
 				userAdapter.swapCursor(db.getAllUsers());
 
 			} else if (broadcast.equals(FACEBOOK_USERS_UPDATED)) {
 				userAdapter.swapCursor(db.getAllUsers());
 
 			} else if (broadcast.equals(TWITTER_USERS_PROBLEM_UPDATING)) {
 				Toast.makeText(
 						context,
 						"The was a problem refreshing your twitter friends. Please check your connection and try again.",
 						Toast.LENGTH_SHORT).show();
 
 			} else if (broadcast.equals(FACEBOOK_USERS_PROBLEM_UPDATING)) {
 				Toast.makeText(
 						context,
 						"The was a problem refreshing your facebook friends. Please check your connection and try again.",
 						Toast.LENGTH_SHORT).show();
 			} else if (broadcast.equals(FEED_PROBLEM_UPDATING)) {
 				Toast.makeText(
 						context,
 						"The was a problem refreshing the feed. Please check your connection and try again.",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Log.wtf(getClass().getName(),
 						"broadcast from unknown intent recieved!");
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_view_flipper);
 
 		// get helpers from android system
 		res = getResources();
 		lbm = LocalBroadcastManager.getInstance(this);
 		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		// add intent filter to be used by broadcast reciever
 		intentFilter = new IntentFilter();
 		intentFilter.addAction(TWITTER_USERS_UPDATED);
 		intentFilter.addAction(TWITTER_USERS_PROBLEM_UPDATING);
 		intentFilter.addAction(FACEBOOK_TIMELINE_UPDATED);
 		intentFilter.addAction(FACEBOOK_USERS_UPDATED);
 		intentFilter.addAction(FACEBOOK_USERS_PROBLEM_UPDATING);
 		intentFilter.addAction(FACEBOOK_USER_NEWS_UPDATED);
 		intentFilter.addAction(FEED_PROBLEM_UPDATING);
 
 		// instanciate database helper
 		db = new DatabaseHelper(this);
 
 		// load typefaces from assets
 		Typeface robotoMedium = Typeface.createFromAsset(getAssets(),
 				"fonts/Roboto-Medium.ttf");
 
 		// find views inflated from xml
 		mainViewFlipper = (ViewFlipper) findViewById(R.id.main_view_flipper);
 		feedViewSwiper = (ViewPager) findViewById(R.id.feed_view_pager);
 		settingsViewFlipper = (ViewAnimator) findViewById(R.id.settings_view_flipper);
 
 		facebookAuthButton = (Button) findViewById(R.id.button_facebook);
 		twitterAuthButton = (Button) findViewById(R.id.button_twitter);
 		Button cfb = (Button) findViewById(R.id.button_create_feed);
 		Button s = (Button) findViewById(R.id.button_settings);
 		settingsViewFlipper.getBackground().setDither(true);
 
 		feedTitleTextView = (TextView) findViewById(R.id.feed_action_bar_title);
 
 		// Set typefaces manually since Android can't handle custom typefaces in
 		// xml in any way whatsoever. Shame on them.
 		twitterAuthButton.setTypeface(robotoMedium);
 		facebookAuthButton.setTypeface(robotoMedium);
 
 		feedTitleTextView = (TextView) findViewById(R.id.feed_action_bar_title);
 
 		cfb.setTypeface(robotoMedium);
 		s.setTypeface(robotoMedium);
 
 		feedTitleTextView.setTypeface(robotoMedium);
 
 		// set adapters
 		feedAdapter = new PageAdapter(getSupportFragmentManager(), db, this);
 		feedViewSwiper.setAdapter(feedAdapter);
 
 		// lets 3 feedsviews to each side of the current one be retained in an
 		// idle state.
 		feedViewSwiper.setOffscreenPageLimit(3);
 
 		CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
 		circleIndicator.setViewPager(feedViewSwiper);
 		circleIndicator
 				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 					@Override
 					public void onPageSelected(int feedIndex) {
 						String feedTitle = feedAdapter.getFeedTitle(feedIndex);
 						feedTitleTextView.setText(feedTitle);
 					}
 
 					@Override
 					public void onPageScrolled(int arg0, float arg1, int arg2) {
 						// TODO Auto-generated method stub'
 					}
 
 					@Override
 					public void onPageScrollStateChanged(int arg0) {
 						// TODO Auto-generated method stub
 					}
 				});
 
 		// instanciate client and service helpers
 		clientHandler = new ClientHandler(this);
 		feedService = new DataServiceHelper(this);
 		feedService.startService();
 
 		// load animations from res/anim
 		slideOutLeft = AnimationUtils
 				.loadAnimation(this, R.anim.slide_out_left);
 		slideOutRight = AnimationUtils.loadAnimation(this,
 				R.anim.slide_out_right);
 		slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
 		slideInRight = AnimationUtils
 				.loadAnimation(this, R.anim.slide_in_right);
 
 		// Display name correct
 		if (feedAdapter.getCount() > 0) {
 			String feedTitle = feedAdapter.getFeedTitle(0);
 			feedTitleTextView.setText(feedTitle);
 		}
		
 		// misc
 		settingsViewFlipper.setInAnimation(slideInRight);
 		settingsViewFlipper.setOutAnimation(slideOutLeft);
 
 		updateOverlay();
 	}
 
 	@Override
 	protected void onDestroy() {
 		feedService.stopService();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		feedService.bindService();
 	}
 
 	@Override
 	protected void onStop() {
 		feedService.unbindService();
 		super.onStop();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		boolean isFacebookAuthorized = Clients.isAuthorized(Clients.FACEBOOK,
 				this);
 		facebookAuthButton.setText(isFacebookAuthorized ? res
 				.getString(R.string.facebook_authorized) : res
 				.getString(R.string.authorize_facebook));
 		facebookAuthButton.setEnabled(!isFacebookAuthorized);
 		if (isFacebookAuthorized) {
 			facebookAuthButton.setTextColor(Color.parseColor("#919191"));
 			facebookAuthButton
 					.setBackgroundResource(R.drawable.facebook_logo_disabled);
 		}
 
 		boolean isTwitterAuthorized = Clients.isAuthorized(Clients.TWITTER,
 				this);
 		twitterAuthButton.setText(isTwitterAuthorized ? res
 				.getString(R.string.twitter_authorized) : res
 				.getString(R.string.authorize_twitter));
 		twitterAuthButton.setEnabled(!isTwitterAuthorized);
 		if (isTwitterAuthorized) {
 			twitterAuthButton.setTextColor(Color.parseColor("#919191"));
 			twitterAuthButton
 					.setBackgroundResource(R.drawable.twitter_logo_disabled);
 		}
 
 		lbm.registerReceiver(receiver, intentFilter);
 
 		// facebook kittens will die if this isn't called onResume
 		clientHandler.extendFacebookAccessTokenIfNeeded();
 	}
 
 	@Override
 	protected void onPause() {
 		lbm.unregisterReceiver(receiver);
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.feed_layout, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			toggleSettingsView(null);
 			break;
 		case R.id.menu_exit:
 			finish();
 			break;
 
 		}
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 		// TODO: Toggle animation for the create feed view. Currently sliding
 		// away in the wring direction.
 
 		if (mainViewFlipper.getCurrentView().getId() == R.id.settings_layout) {
 			if (settingsViewFlipper.getCurrentView().getId() == R.id.user_list_layout) {
 				settingsViewFlipper.showPrevious();
 			} else {
 				toggleSettingsView(null);
 			}
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == Activity.RESULT_OK) {
 			switch (requestCode) {
 			case Clients.TWITTER:
 				clientHandler.onTwitterAuthCallback(data);
 				break;
 			case Clients.FACEBOOK:
 				clientHandler.onFacebookAuthCallback(requestCode, resultCode,
 						data);
 				break;
 			default:
 				Log.wtf(getClass().getName(),
 						"Result callback from unknown intent");
 			}
 		}
 	}
 
 	@Override
 	public void onFeedUpdateRequest(String feedTitle) {
 		// if (isOnline()) {
 		feedService.updateFeed(new Feed(feedTitle));
 		// } else {
 		// Intent intent = new Intent();
 		// intent.setAction(NO_CONNECTION);
 		// lbm.sendBroadcast(intent);
 		// }
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo i = cm.getActiveNetworkInfo();
 		if (i == null)
 			return false;
 		if (!i.isConnected())
 			return false;
 		if (!i.isAvailable())
 			return false;
 		return true;
 	}
 
 	// Methods called on button press below. See xml files.
 
 	public void toggleSettingsView(View v) {
 		int currentView = mainViewFlipper.getCurrentView().getId();
 
 		if (currentView == R.id.main_layout) {
 			mainViewFlipper.setInAnimation(slideInLeft);
 			mainViewFlipper.setOutAnimation(slideOutRight);
 			mainViewFlipper.showNext();
 		} else {
 			mainViewFlipper.setInAnimation(slideInRight);
 			mainViewFlipper.setOutAnimation(slideOutLeft);
 			mainViewFlipper.showPrevious();
 		}
 	}
 
 	public void initCreateFeedView(View v) {
 		userListLayout = (LinearLayout) inflater.inflate(
 				R.layout.user_list_layout, null);
 		userListView = (ListView) userListLayout
 				.findViewById(R.id.user_list_view);
 
 		// if (isOnline()) {
 		feedService.updateUsers();
 		// } else {
 		// Toast.makeText(this, "No connection available", Toast.LENGTH_LONG)
 		// .show();
 		// }
 
 		Cursor cursor = db.getAllUsers();
 
 		String[] columns = new String[] { DatabaseHelper.USER_COLUMN_USERNAME,
 				DatabaseHelper.USER_COLUMN_USERID,
 				DatabaseHelper.USER_COLUMN_SOURCE };
 		int[] to = new int[] { R.id.user_item_text_view };
 
 		userAdapter = new UserAdapter(this, R.layout.user_list_item, cursor,
 				columns, to, CursorAdapter.NO_SELECTION);
 
 		userListView.setAdapter(userAdapter);
 
 		settingsViewFlipper.addView(userListLayout);
 		settingsViewFlipper.showNext();
 	}
 
 	public void createFeed(View button) {
 
 		// Animate switch to main view
 		toggleSettingsView(null);
 
 		// Extract new feed title
 		EditText titleEditText = (EditText) userListLayout
 				.findViewById(R.id.create_feed_action_bar_title);
 		String feedTitle = titleEditText.getText().toString();
 		Feed feed = new Feed(feedTitle);
 
 		// Extract new feed users
 		SparseBooleanArray checked = userListView.getCheckedItemPositions();
 		ArrayList<Integer> userIDs = new ArrayList<Integer>();
 
 		UserAdapter adapter = (UserAdapter) userListView.getAdapter();
 
 		Cursor c;
 		for (int i = 0; i < adapter.getCount(); i++) {
 			if (checked.get(i)) {
 				c = (Cursor) adapter.getItem(i);
 				userIDs.add(c.getInt(c
 						.getColumnIndex(DatabaseHelper.USER_COLUMN_USERID)));
 			}
 		}
 
 		// Save user list as a feed in database
 		db.addFeed(feed);
 		long feed_id = db.getFeed_id(feed);
 		for (Integer i : userIDs)
 			db.addFeedUserBridge(feed_id, i);
 		Log.i(getClass().getName(), "Added feed \"" + feed.getTitle()
 				+ "\" with " + userIDs.size() + " users.");
 
 		// Hide keyboard
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
 
 		// Animate switch to new feed view
 		this.feedAdapter.addFeed(feed);
 		feedViewSwiper.setCurrentItem(adapter.getCount());
 		feedTitleTextView.setText(feedTitle);
 
 		// Remove the createFeedView
 		View v = settingsViewFlipper.getCurrentView();
 		settingsViewFlipper.showPrevious();
 		settingsViewFlipper.removeView(v);
 		userListLayout = null;
 		userListView = null;
 
<<<<<<< HEAD
		LinearLayout bajs = (LinearLayout) mainViewFlipper
				.findViewById(R.id.main_layout);
		ImageView hej = (ImageView) bajs.findViewById(R.id.no_feed_image);
		hej.setVisibility(8);
=======
 		// Check overlay
 		updateOverlay();
 	}
 
 	private void updateOverlay() {
 		if (feedAdapter.getCount() > 0) {
 			LinearLayout overlayLayout = (LinearLayout) mainViewFlipper
 					.findViewById(R.id.main_layout);
 			ImageView overlay = (ImageView) overlayLayout
 					.findViewById(R.id.no_feed_image);
 			overlay.setVisibility(View.INVISIBLE);
 		}
>>>>>>> Created menu and made some small fixes
 	}
 
 	public void authorizeTwitter(View v) {
 		clientHandler.authorize(Clients.TWITTER, new AuthListener() {
 			@Override
 			public void onAuthorizationComplete() {
 				Toast.makeText(FeedActivity.this,
 						"Twitter authorization successful", Toast.LENGTH_SHORT)
 						.show();
 				twitterAuthButton.setText(res
 						.getString(R.string.twitter_authorized));
 				twitterAuthButton.setEnabled(false);
 				twitterAuthButton.setTextColor(Color.parseColor("#919191"));
 				twitterAuthButton
 						.setBackgroundResource(R.drawable.twitter_logo_disabled);
 			}
 
 			@Override
 			public void onAuthorizationFail() {
 				Toast.makeText(FeedActivity.this,
 						"Twitter authorization failed", Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 	}
 
 	public void authorizeFacebook(View v) {
 		clientHandler.authorize(Clients.FACEBOOK, new AuthListener() {
 			@Override
 			public void onAuthorizationComplete() {
 				Toast.makeText(FeedActivity.this,
 						"Facebook authorization successful", Toast.LENGTH_LONG)
 						.show();
 				facebookAuthButton.setText(res
 						.getString(R.string.facebook_authorized));
 				facebookAuthButton.setEnabled(false);
 				facebookAuthButton.setTextColor(Color.parseColor("#919191"));
 				facebookAuthButton
 						.setBackgroundResource(R.drawable.facebook_logo_disabled);
 			}
 
 			@Override
 			public void onAuthorizationFail() {
 				Toast.makeText(FeedActivity.this,
 						"Facebook authorization failed", Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 	}
 
 	public void testSomething(View v) {
 		// This button is for testing only. Use it for all your testing needs <3
 		db.clearItemTable();
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		Window window = getWindow();
 		window.setFormat(PixelFormat.RGBA_8888);
 	}
 }
