 package com.codepath.apps.twitterclient;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.codepath.apps.twitterclient.models.Tweet;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import eu.erikw.PullToRefreshListView;
 import eu.erikw.PullToRefreshListView.OnRefreshListener;
 
 public class TimelineActivity extends Activity {
 	private static int REQUEST_CODE_COMPOSE = 1;
 	
 	private long maxId = 0;
 	private long sinceId = Long.MAX_VALUE;
 	private TweetsAdapter tweetsAdapter;
 	private ArrayList<Tweet> tweets;
 	private PullToRefreshListView lvTweets;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timeline);
 		
 		tweets = new ArrayList<Tweet>();
 		lvTweets = (PullToRefreshListView) findViewById(R.id.lvTweets);
 		tweetsAdapter = new TweetsAdapter(getBaseContext(), tweets);
 		lvTweets.setAdapter(tweetsAdapter);
 		
 		final JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonTweets) {
 				ArrayList<Tweet> olderTweets = Tweet.fromJson(jsonTweets);
 				if (olderTweets.size() == 0) {
 					Log.d("DEBUG", "size zero array");
 					return;
 				}
 				tweetsAdapter.addAll(olderTweets);
 				maxId = olderTweets.get(olderTweets.size() - 1).getId() - 1;
 				sinceId = olderTweets.get(0).getId();
 				
 				lvTweets.onRefreshComplete();
 			}
 			
 			@Override
 			public void onFailure(Throwable t, JSONObject errorResponse) {
 				Log.d("DEBUG", "fail case");
 			}
 		};
 		
 		TwitterClientApp.getRestClient().getHomeTimeline(handler, null);
 		
 		lvTweets.setOnScrollListener(new EndlessScrollListener() {
 			@Override
 			public void onLoadMore(int page, int totalItemsCount) {
 				TwitterClientApp.getRestClient().getHomeTimeline(handler, maxId);
 			}
 		});
 		
 		lvTweets.setOnRefreshListener(new OnRefreshListener() {
 			@Override
 			public void onRefresh() {
 				Log.d("DEBUG", "calling load new tweets");
 				loadNewTweets();
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.timeline, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.action_compose:
 			compose();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void compose() {
 		Intent i = new Intent(getApplicationContext(), ComposeActivity.class);
 		startActivityForResult(i, REQUEST_CODE_COMPOSE);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("DEBUG", "inside on activity result " + requestCode + " " + resultCode);
 		if (resultCode == RESULT_OK &&
 				requestCode == REQUEST_CODE_COMPOSE) {
 			loadNewTweets();
 		}
 	}
 	
 	private void loadNewTweets() {
 		final JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonTweets) {
 				ArrayList<Tweet> newTweets = Tweet.fromJson(jsonTweets);
				Log.d("DEBUG", "having new tweets" + newTweets.size());
				for(Tweet tweet : newTweets) {
					tweets.add(0, tweet);
 				}
				tweetsAdapter.notifyDataSetChanged();
				maxId = newTweets.get(newTweets.size() - 1).getId() - 1;
				sinceId = newTweets.get(0).getId();
 				
 				lvTweets.onRefreshComplete();
 			}
 		};
 		
 		TwitterClientApp.getRestClient().getHomeTimelineSince(handler, sinceId);
 	}
 
 }
