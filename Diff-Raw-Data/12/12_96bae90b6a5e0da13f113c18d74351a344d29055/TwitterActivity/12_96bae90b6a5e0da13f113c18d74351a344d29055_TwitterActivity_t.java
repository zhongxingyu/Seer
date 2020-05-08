 package edu.berkeley.eecs.ruzenafit.controller;
 
 import edu.berkeley.eecs.ruzenafit.R;
 import edu.berkeley.eecs.ruzenafit.R.layout;
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class TwitterActivity extends Activity {
 	static final String TAG = "StatusActivity";
 	EditText editStatus;
 
 	/** Called when the activity is first created. **/
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		Log.d(TAG, "onCreated with Bundle" + bundle);
 
 		setContentView(R.layout.twitter);
 
 		// editStatus = (EditText) findViewById(R.id.edit_status);
 
 	}
 
 	public void onClick(View v) {
 		final String statusText = editStatus.getText().toString();
 		// statusText = "The user burned " + "insert variable for calories here"
 		// + "calories";
 
 		new Thread() {
 			public void run() {
 				try {
 					Twitter twitter = new Twitter("student", "password");
 					// twitter.setAPIRootUrl("http://yamba.markana.com/api");
 					twitter.setStatus(statusText);
 					Log.d(TAG, "Successfully posted: " + statusText);
					Toast.makeText(TwitterActivity.this,
 							"Successfully posted: " + statusText,
 							Toast.LENGTH_LONG).show();
 
 				} catch (TwitterException e) {
 					Log.e(TAG, "Died", e);
 					e.printStackTrace();
					Toast.makeText(TwitterActivity.this,
 							"Failed posting: " + statusText,
 							Toast.LENGTH_LONG).show();
 				}
 			}
 		}.start();
 
 		Log.d(TAG, "onClicked with text: " + statusText);
 	}
 	
 	class PostToTwitter extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			return null;
		}
 		
 	}
 }
