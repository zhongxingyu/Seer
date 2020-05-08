 package com.codepath.apps.twitterapp;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.codepath.apps.twitterapp.models.Tweet;
 import com.codepath.apps.twitterapp.models.User;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

//import eu.erikw.PullToRefreshListView;
//import eu.erikw.PullToRefreshListView.OnRefreshListener;

 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class TimelineActivity extends Activity {
 
     private ListView lvTweets;
     private ArrayList<Tweet> tweets;
     private TweetsAdapter adapter;
     private String maxId;
     private static final int REQUEST_CODE = 1;
     private User currUser;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.activity_timeline);
 	lvTweets = (ListView) findViewById(R.id.lvTweets);
 	tweets = new ArrayList<Tweet>();
 	adapter = new TweetsAdapter(getBaseContext(), tweets);
 	lvTweets.setAdapter(adapter);
 	getTweets(null);
	/*lvTweets.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list contents
                // Make sure you call listView.onRefreshComplete()
                // once the loading is done. This can be done from here or any
                // place such as when the network request has completed successfully.
                getTweets(maxId);
            }
        });*/
 	lvTweets.setOnScrollListener(new EndlessScrollListener(){
 	    @Override
             public void loadMore(int page, int totalItemsCount) {
 	        // TODO Auto-generated method stub
 	        getTweets(maxId);
             }
 	});
 	getUser();
     }
     
     private void getUser() {
 	TwitterApp.getRestClient().getUserInfo(new JsonHttpResponseHandler() {
 	    @Override
 	    public void onSuccess(JSONObject jsonTweets){
 		User user = User.fromJson(jsonTweets);
 		currUser = user;
 	    }
 	});
     }
     private void getTweets(String max){
 	TwitterApp.getRestClient().getHomeTimeLine(max, new JsonHttpResponseHandler() {
 	    @Override
 	    public void onSuccess(JSONArray jsonTweets){
 		tweets = Tweet.fromJson(jsonTweets);
 		//get the maximum id to retrieve the next set of tweets.
 		maxId =tweets.get(tweets.size()-1).getIdStr();
 		adapter.addAll(tweets);
 		/*TweetsAdapter adapter = new TweetsAdapter(getBaseContext(), tweets);
 		lvTweets.setAdapter(adapter);*/
		//lvTweets.onRefreshComplete();
 		Log.d("DEBUG", jsonTweets.toString());
 	    }
	    
	    public void onFailure(Throwable e){
		Log.d("DEBUG", e.toString());
	    }
 	});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	// Inflate the menu; this adds items to the action bar if it is present.
 	getMenuInflater().inflate(R.menu.timeline, menu);
 	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem menu) {
 	Intent i = new Intent(getApplicationContext(), NewTweet.class);
 	i.putExtra("profile_url", currUser.getProfileImageUrl());
 	i.putExtra("user_name", currUser.getScreenName());
 	startActivityForResult(i, REQUEST_CODE);
 	return false;
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent i){
 	if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
 	    Toast.makeText(getBaseContext(), i.getStringExtra("tweet"), Toast.LENGTH_SHORT).show();
 	    
 	    TwitterApp.getRestClient().addTweet(i.getStringExtra("tweet"), new JsonHttpResponseHandler() {
 		@Override
 		public void onSuccess(JSONObject jsonResult) {
 		    User user = User.fromJson(jsonResult);
 		    maxId = user.getIdStr();
 		    getTweets(maxId);   
 		}
 		
 		@Override
 		public void onFailure(Throwable e, JSONObject jsonResult) {
 		    Log.d("DEBUG", e.toString());
 		}
 	    });
 	}
     }
 }
