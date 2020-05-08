 package com.codepath.apps.restclienttemplate;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.codepath.apps.restclienttemplate.helpers.EndlessScrollListener;
 import com.codepath.apps.restclienttemplate.models.Tweet;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import eu.erikw.PullToRefreshListView;
 import eu.erikw.PullToRefreshListView.OnRefreshListener;
 
 public class TimeLineActivity extends Activity {
 	TweetsAdapter tweetsAdapter = null;
 	PullToRefreshListView lvTweets = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_time_line);
 		lvTweets = (PullToRefreshListView) findViewById(R.id.lvTweets);
 		lvTweets.setOnRefreshListener(new OnRefreshListener() {
 
 			@Override
 			public void onRefresh() {
 				displayTweets();
 			}
 		});
 
 		lvTweets.setOnScrollListener(new EndlessScrollListener() {
 
 			@Override
 			public void onLoadMore(int page, int totalItemsCount) {
 				if(tweetsAdapter == null) {
 					displayTweets();
 				} else {
 					displayTweets(tweetsAdapter.getCount() + 10);
 				}
 			}
 		});
 
 		displayTweets();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.time_line, menu);
 		return true;
 	}
 
 	public boolean composeMessage(MenuItem item) {
 		Intent i = new Intent(getApplicationContext(), ComposeActivity.class);
 		startActivityForResult(i, 0);
 		return true;
 	}
 
 	public void displayTweets(int count) {
 		RestClientApp.getRestClient().getStatuses(count, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonTweets) {
 				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
 				if(tweetsAdapter == null) {
 					tweetsAdapter = new TweetsAdapter(getBaseContext(), tweets);
 					lvTweets.setAdapter(tweetsAdapter);
 				} else {
 					tweetsAdapter.clear();
 					tweetsAdapter.addAll(tweets);
 				}
 				lvTweets.onRefreshComplete();
 			}
 
 			public void onFailure(Throwable e) {
 				Log.d("DEBUG", "Fetch timeline error: " + e.toString());
 			}
 		});
 	}
 
 	public void displayTweets() {
 		if(tweetsAdapter != null) {
 			tweetsAdapter.clear();
 		}
		displayTweets(20);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				displayTweets();
			}
 		}
 	}
 }
