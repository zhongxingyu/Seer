 package com.twitterapp;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.twitterapp.models.Tweet;
 import com.twitterapp.models.TweetData;
 import com.twitterapp.models.User;
 
 public class TimelineActivity extends Activity {
 	private TweetsAdapter tweetsAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_timeline);
         TwitterApp.getRestClient().getHomeFeed(new JsonHttpResponseHandler(){
         	public void onSuccess(JSONArray jsonTweets){
         		ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
         		
         		ListView lvTweets = (ListView) findViewById(R.id.lvTweets);
         		
        		TweetsAdapter adapter = new TweetsAdapter(getBaseContext(), tweets);
         		
        		lvTweets.setAdapter(adapter);
         	}
         	
 			public void onFailure(Throwable error) {
 				Log.d("Debug", "NOOO request failed.");
 				Log.d("Debug", error.getMessage());
 			}
         });
     }
     
     public void onCompose(MenuItem mi){
     	
     	Intent i = new Intent(getBaseContext(), ComposeActivity.class);
     	startActivityForResult(i, 10);
//    	startActivity(i);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.timeline, menu);
         return true;
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	Toast.makeText(getApplicationContext(), "onActivityResult", Toast.LENGTH_SHORT).show();
 		if (resultCode == RESULT_OK && requestCode == 10) {
 			TweetData tweetData = (TweetData) data
 					.getSerializableExtra("TweetData");
 			Toast.makeText(this, "tweet: " + tweetData.getTweet(),
 					Toast.LENGTH_SHORT).show();
 
 			Tweet tweet = new Tweet();
 			User user = new User();
 			JSONObject tweetAsJson;
			JSONObject userAsSjson;
 			try {
 				tweetAsJson = new JSONObject(tweetData.getJsonString());
 				tweet.setJsonObject(tweetAsJson);
				userAsSjson = new JSONObject(tweetData.getUserString());
				user.setJsonObject(userAsSjson);
 				tweet.setUser(user);
 				tweetsAdapter.insert(tweet, 0);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
     
 }
