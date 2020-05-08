 package com.codepath.apps.restclienttemplate;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 
 import com.codepath.apps.restclienttemplate.models.Tweet;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class TimeLineActivity extends Activity {

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_time_line);
 		
 		RestClientApp.getRestClient().getStatuses(new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonTweets) {
 				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.time_line, menu);
 		return true;
 	}
 
 }
