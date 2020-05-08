 package com.codepath.apps.twitterclient;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.content.Intent;
 
 import android.nfc.Tag;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.codepath.apps.twitterclient.handlers.TweetJsonHttpResponseHandler;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 
 /**
  * Created by rhu on 10/23/13.
  */
 public class MainActivity extends FragmentActivity implements TimelineFragment.OnDataUpdateListener {
 
 	private int REQUEST_CODE = 123;
 	private int TWEET_PER_PAGE = 25;
 
 	TwitterClient client;
 
 	boolean offlineMode = false;
 
 	enum TabTypes {
 		HOME, MENTIONS
 	};
 
 	ActionBar actionBar;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setUpTabs();
 		client = RestClientApp.getRestClient();
 
 	}
 
 	private void setUpTabs() {
 		actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(true);
 
 		Tab tab1 = actionBar.newTab().setText("Home").setIcon(R.drawable.ic_launcher).setTag(TabTypes.HOME);
 		tab1.setTabListener(new FragmentTabListener<TimelineFragment>(R.id.fl1, this, "home", TimelineFragment.class));
 		actionBar.addTab(tab1);
 		actionBar.selectTab(tab1);
 
 		Tab tab2 = actionBar.newTab().setText("Mentions").setIcon(R.drawable.ic_launcher).setTag(TabTypes.MENTIONS);
 		tab2.setTabListener(new FragmentTabListener<TimelineFragment>(R.id.fl1, this, "mentions", TimelineFragment.class));
 		actionBar.addTab(tab2);
 
 	}
 
 	@Override
 	public void loadMore(final TimelineFragment.LoadType loadType) {
 
 		Tab selected = (Tab) actionBar.getSelectedTab();
 
 		if (selected == null) {
 			Log.d("debug", "No tab");
 			return;
 		}
 
 		TabTypes tabTag = (TabTypes) selected.getTag();
 
 		Method method;
 
 		try {
 			if (tabTag == TabTypes.HOME) {
 				method = client.getClass().getMethod("getHomeTimeline", long.class, long.class, int.class, AsyncHttpResponseHandler.class);
 			}
 			else {
 				method = client.getClass().getMethod("getMentionsTimeline", long.class, long.class, int.class, AsyncHttpResponseHandler.class);
 			}
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		TimelineFragment fragment = (TimelineFragment) getSupportFragmentManager().findFragmentById(R.id.fl1);
 			if (fragment == null) {
 				Log.d("debug", "fragment not there");
 			} else {
 				TweetJsonHttpResponseHandler handler = fragment.createTweetHandler(loadType);
 				try {
 					if (loadType == TimelineFragment.LoadType.NEW_TWEETS) {
 						Log.d("debug", "Looking for newer tweets");
 						method.invoke(client, fragment.getNewerTweets(), 0, TWEET_PER_PAGE, handler);
 					}
 					else {
 						Log.d("debug", "Looking for older tweets");
 						method.invoke(client, 0, fragment.getOlderTweets(), TWEET_PER_PAGE, handler);
 					}
 				} catch (IllegalAccessException e) {
 				}
 				catch (InvocationTargetException e) {
 				}
 			}
 	}
 
 	@Override
	public void onError(Throwable e, final String responseBody) {
		Log.d("debug", "onError here " + e.toString());

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, "Twitter error: " + responseBody, Toast.LENGTH_LONG).show();

			}
		});
 	}
 
 	/*
 	public void offlineMode(MenuItem item) {
 		if (item.isChecked() == false) {
 			item.setChecked(true);
 			offlineMode = true;
 			tweetAdapter.clear();
 			this.initMinMax();
 			List<Tweet> tweets = new Select().from(Tweet.class).orderBy("created_at DESC").execute();
 			if (tweets.size() > 0) {
 				tweetAdapter.addAll(tweets);
 			}
 		} else {
 			offlineMode = false;
 			item.setChecked(false);
 			tweetAdapter.clear();
 			this.initMinMax();
 			listener.loadMore();
 		}
 	}*/
 
 	public void composeTweet(MenuItem item) {
 		Intent i = new Intent(getBaseContext(), ComposeActivity.class);
 		startActivityForResult(i, REQUEST_CODE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
 //			this.refreshTweets();
 		}
 	}
 
 	public void logout(MenuItem item) {
 		TwitterClient client = RestClientApp.getRestClient();
 		client.clearAccessToken();
 		finish();
 	}
 
 }
