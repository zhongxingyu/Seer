 package com.examples.gg;
 
 import java.util.ArrayList;
 
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.examples.gg.adapters.EntryAdapter;
 import com.examples.gg.data.EntryItem;
 import com.examples.gg.data.Item;
 import com.examples.gg.data.SectionItem;
 import com.examples.gg.loadMore.LoadMore_H_Subscription;
 import com.examples.gg.loadMore.LoadMore_JD_News_Image;
 import com.examples.gg.loadMore.LoadMore_M_Subscription;
 import com.examples.gg.loadMore.LoadMore_News;
 import com.examples.gg.loadMore.LoadMore_Result;
 import com.examples.gg.loadMore.LoadMore_Twitch;
 import com.examples.gg.loadMore.LoadMore_UpcomingMatch;
 import com.examples.gg.settings.SettingsActivity;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.InterstitialAd;
 
 public class SideMenuActivity extends SherlockFragmentActivity implements
 		AdListener {
 
 	// Declare Variable
 	DrawerLayout mDrawerLayout;
 	ListView mDrawerList;
 	ActionBarDrawerToggle mDrawerToggle;
 	ArrayList<Item> items = new ArrayList<Item>();
 	ActionBar mActionBar;
 	EntryAdapter eAdapter;
 
 	private FragmentManager fm;
 	private InterstitialAd interstitial;
 
 	// private boolean doubleBackToExitPressedOnce = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.drawer_main);
 
 		// Create the interstitial
 		interstitial = new InterstitialAd(this,
 				"ca-app-pub-6718707824713684/7369125856");
 
 		// Create ad request
 		AdRequest adRequest = new AdRequest();
		adRequest.addTestDevice("5E4CA696BEB736E734DD974DD296F11A");
 		// Begin loading your interstitial
 		interstitial.loadAd(adRequest);
 
 		// Set Ad Listener to use the callbacks below
 		interstitial.setAdListener(this);
 
 		// Initial fragment manager
 		fm = this.getSupportFragmentManager();
 
 		// Locate DrawerLayout in drawer_main.xml
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 
 		// Locate ListView in drawer_main.xml
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// Do not allow list view to scroll over
 		// mDrawerList.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
 
 		// Set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 
 		// Pass results to MenuListAdapter Class
 		// mMenuAdapter = new MenuListAdapter(this, title, subtitle, icon);
 		items.add(new SectionItem("Everyday's Feed"));
 		items.add(new EntryItem("What's new", "Fresh meat!",
 				R.drawable.fresh_meat));
 
 		items.add(new EntryItem("Latest News", "From JoinDota, Gosugamer",
 				R.drawable.ic_action_news));
 
 		items.add(new SectionItem("Latest Videos"));
 		items.add(new EntryItem("Highlights", "Dota excitements",
 				R.drawable.highlights));
 		items.add(new EntryItem("Matches", "You don't wanna miss it",
 				R.drawable.swords));
 
 		items.add(new SectionItem("Lives"));
 		items.add(new EntryItem("Twitch Streams", "Battle begins!",
 				R.drawable.live));
 
 		items.add(new SectionItem("Match Table"));
 		items.add(new EntryItem("Upcomings", "Matches coming soon!",
 				R.drawable.upcoming));
 		items.add(new EntryItem("Recent Results", "It's in the bag!",
 				R.drawable.list_result));
 
 		// "About" section
 		items.add(new SectionItem("About App"));
 		items.add(new EntryItem("Feedback", "Help us make it better",
 				R.drawable.feedback));
 
 		items.add(new EntryItem("Share", "Share our app",
 				R.drawable.ic_action_social_share));
 		items.add(new EntryItem("Rate Dota2TV", "Like it?",
 				R.drawable.ic_action_rating_good));
 
 		eAdapter = new EntryAdapter(this, items);
 
 		// setListAdapter(adapter);
 
 		// Set the MenuListAdapter to the ListView
 		mDrawerList.setAdapter(eAdapter);
 
 		// Capture button clicks on side menu
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// Enable ActionBar app icon to behave as action to toggle nav drawer
 		mActionBar = getSupportActionBar();
 
 		mActionBar.setHomeButtonEnabled(true);
 		mActionBar.setDisplayHomeAsUpEnabled(true);
 		// mActionBar.setTitle("Main Menu");
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
 				R.drawable.ic_drawer, R.string.drawer_open,
 				R.string.drawer_close) {
 
 			public void onDrawerClosed(View view) {
 				// TODO Auto-generated method stub
 				super.onDrawerClosed(view);
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				// TODO Auto-generated method stub
 				super.onDrawerOpened(drawerView);
 			}
 		};
 
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		if (savedInstanceState == null) {
 			selectItem(1);
 
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		if (item.getItemId() == android.R.id.home) {
 
 			Handler handler = new Handler();
 
 			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
 				handler.postDelayed(new Runnable() {
 					public void run() {
 						mDrawerLayout.closeDrawer(mDrawerList);
 					}
 				}, 0);
 			} else {
 				handler.postDelayed(new Runnable() {
 					public void run() {
 						mDrawerLayout.openDrawer(mDrawerList);
 					}
 				}, 0);
 			}
 		}
 
 		if (item.getItemId() == R.id.menu_settings) {
 
 			// Start setting activity
 			Intent i = new Intent(this, SettingsActivity.class);
 			startActivity(i);
 
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	// The click listener for ListView in the navigation drawer
 	public class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			// if (row != null) {
 			// row.setBackgroundColor(Color.WHITE);
 			// }
 			// row = view;
 			// view.setBackgroundColor(Color.YELLOW);
 			//
 			selectItem(position);
 		}
 	}
 
 	private void selectItem(int position) {
 
 		// Set the indicator in drawer to correct position
 		if (position <= 10)
 			setDrawerIndicator(position);
 
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 
 		// Clear the fragment stack first
 		clearFragmentStack();
 
 		mDrawerList.setItemChecked(position, true);
 
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				mDrawerLayout.closeDrawer(mDrawerList);
 			}
 		}, 0);
 
 		switch (position) {
 
 		case 1:
 			// News
 			ft.replace(R.id.content_frame, new LoadMore_News());
 			break;
 
 		case 2:
 			// Gosu news
 			ft.replace(R.id.content_frame, new LoadMore_JD_News_Image());
 			break;
 
 		case 4:
 			// Highlight section
 			ft.replace(R.id.content_frame, new LoadMore_H_Subscription());
 			break;
 
 		case 5:
 			// Match section
 			ft.replace(R.id.content_frame, new LoadMore_M_Subscription());
 			break;
 
 		case 7:
 			// Twitch section
 			ft.replace(R.id.content_frame, new LoadMore_Twitch());
 			break;
 
 		case 9:
 			// upcoming section
 			ft.replace(R.id.content_frame, new LoadMore_UpcomingMatch());
 			break;
 
 		case 10:
 			// result section
 			ft.replace(R.id.content_frame, new LoadMore_Result());
 			break;
 
 		case 12:
 			// Feedback
 
 			Intent email = new Intent(Intent.ACTION_VIEW);
 			email.setData(Uri
 					.parse("mailto:dota2tv1@gmail.com?subject=Dota2TV Feedback"));
 			startActivity(Intent.createChooser(email, "Send feedback via.."));
 			// startActivity(email);
 			break;
 
 		case 13:
 			// Share Dota2TV
 			Intent sendIntent = new Intent();
 			sendIntent.setAction(Intent.ACTION_SEND);
 			sendIntent
 					.putExtra(Intent.EXTRA_TEXT,
 							"https://play.google.com/store/apps/details?id=com.examples.gg");
 			sendIntent.setType("text/plain");
 			startActivity(Intent
 					.createChooser(sendIntent, "Share Dota2TV to.."));
 			// startActivity(sendIntent);
 			break;
 
 		case 14:
 			// Rate Dota2TV
 			Intent rateIntent = new Intent(Intent.ACTION_VIEW);
 			// Try Google play
 			rateIntent
 					.setData(Uri.parse("market://details?id=com.examples.gg"));
 			if (tryStartActivity(rateIntent) == false) {
 				// Market (Google play) app seems not installed, let's try to
 				// open a webbrowser
 				rateIntent
 						.setData(Uri
 								.parse("https://play.google.com/store/apps/details?id=com.examples.gg"));
 				if (tryStartActivity(rateIntent) == false) {
 					// Well if this also fails, we have run out of options,
 					// inform the user.
 					Toast.makeText(
 							this,
 							"Could not open Google Play, please install Google Play.",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 
 			break;
 		}
 
 		ft.commit();
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggles
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	// Clear fragment back stack
 	public void clearFragmentStack() {
 		fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
 	}
 
 	// Handles exit
 	// @Override
 	// public void onBackPressed() {
 	// if (fm.getBackStackEntryCount() == 0) {
 	//
 	// // No fragment in back stack
 	//
 	// if (doubleBackToExitPressedOnce) {
 	// super.onBackPressed();
 	// return;
 	// }
 	// this.doubleBackToExitPressedOnce = true;
 	// Toast.makeText(this, "Please click BACK again to exit",
 	// Toast.LENGTH_SHORT).show();
 	//
 	// // reset doubleBackToExitPressedOnce to false after 2 seconds
 	// new Handler().postDelayed(new Runnable() {
 	//
 	// @Override
 	// public void run() {
 	// doubleBackToExitPressedOnce = false;
 	//
 	// }
 	// }, 2000);
 	// } else {
 	//
 	// // Fragment back stack is empty
 	//
 	// super.onBackPressed();
 	// }
 	// }
 
 	public void setDrawerIndicator(int position) {
 		for (Item i : items)
 			i.setUnchecked();
 		items.get(position).setChecked();
 		eAdapter.notifyDataSetChanged();
 	}
 
 	private boolean tryStartActivity(Intent aIntent) {
 		try {
 			startActivity(aIntent);
 			return true;
 		} catch (ActivityNotFoundException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public void onDismissScreen(Ad arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onLeaveApplication(Ad arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPresentScreen(Ad arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onReceiveAd(Ad ad) {
 		// TODO Auto-generated method stub
 		// Log.d("OK", "Received ad");
 //		Random mRand = new Random();
 //		if (mRand.nextInt(10) >= 5)
 			if (ad == interstitial) {
 				interstitial.show();
 			}
 	}
 
 }
