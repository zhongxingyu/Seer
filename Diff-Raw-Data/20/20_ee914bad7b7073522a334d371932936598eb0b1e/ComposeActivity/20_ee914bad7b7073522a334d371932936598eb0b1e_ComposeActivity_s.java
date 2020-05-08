 package com.codepath.apps.restclienttemplate;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class ComposeActivity extends Activity {
 
 	EditText bodyTweet;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_compose);
 		bodyTweet = (EditText) findViewById(R.id.compose_body_tweet);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.compose, menu);
 		return true;
 	}
 	
 	public boolean postTweet(View view) {
 		RestClientApp.getRestClient().postTweet(bodyTweet.getText().toString(), new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(int arg, JSONArray objects) {
				finish();
 			}
 			
 			@Override
 			public void onFailure(Throwable arg0, JSONObject arg1) {
				finish();
 			}
 		});
 		return true;
 	}
 
 }
