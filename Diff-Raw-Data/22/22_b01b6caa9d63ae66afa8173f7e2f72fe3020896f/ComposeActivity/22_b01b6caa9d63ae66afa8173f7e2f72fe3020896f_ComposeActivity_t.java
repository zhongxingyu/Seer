 package com.twitterapp;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.twitterapp.models.Tweet;
 import com.twitterapp.models.TweetData;
 
 public class ComposeActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_compose);
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(false);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.compose, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void onTweet(View v){
 		EditText textView = (EditText) findViewById(R.id.etCompose);
 		String text = textView.getText().toString();
 		Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();		
 		TwitterApp.getRestClient().postTweet(new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject returnObject) {
 				Log.d("Debug", "Success");
 				try {
 
 					Tweet fromJson = Tweet.fromJson(returnObject);
 					TweetData data = new TweetData();
 					JSONObject user = returnObject
 							.getJSONObject("user");
 					data.setUserString(user.toString());
 					data.setId(fromJson.getId());
 					data.setTweet(fromJson.getBody());
 					data.setUserName(fromJson.getUser()
 							.getName());
 					data.setUserProfileImageURL(fromJson
 							.getUser().getProfileImageUrl());
 					data.setUserBackGroundImageURL(fromJson
 							.getUser()
 							.getProfileBackgroundImageUrl());
 					data.setJsonString(returnObject.toString());
					Intent in = new Intent(getBaseContext(),TimelineActivity.class);
 					in.putExtra("TweetData", data);
 					setResult(RESULT_OK, in);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				finish();
 			}
 
 			@Override
 			public void onFailure(Throwable arg0, JSONArray arg1) {
 				Log.d("Debug", arg0.toString());
 			}
 
 			@Override
 			public void onFailure(Throwable arg0, String arg1) {
 				Log.d("Debug", arg0.toString());
 			}
 	}, text);
 }
 
 	
 	public void onCancel(View v){
 		Toast.makeText(getBaseContext(), "Cancel", Toast.LENGTH_SHORT).show();
 	}
 
 }
