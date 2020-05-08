 package com.markdanks.yamba;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StatusActivity extends Activity implements OnClickListener,
 		TextWatcher {
 	public static final String TAG = "StatusActivity";
 	EditText editText;
 	TextView textCharsRemaining;
 	Button updateButton;
 	public static final int MAX_CHARS = 140;
 
 	Twitter twitter;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.status);
 
 		editText = (EditText) findViewById(R.id.editText);
 		editText.addTextChangedListener(this);
 		updateButton = (Button) findViewById(R.id.buttonUpdate);
 
 		textCharsRemaining = (TextView) findViewById(R.id.textCharsRemaining);
 		textCharsRemaining.setText(Integer.toString(MAX_CHARS));
 		textCharsRemaining.setTextColor(Color.GREEN);
 
 		updateButton.setOnClickListener(this);
 
 		twitter = new Twitter("student", "password");
 		twitter.setAPIRootUrl("http://yamba.marakana.com/api");
 	}
 
 	// Asynchronously posts to twitter
 	class PostToTwitter extends AsyncTask<String, Integer, String> {
 		// Called to initiate the background activity
 		@Override
 		protected String doInBackground(String... statuses) { //
 			try {
 				Twitter.Status status = twitter.updateStatus(statuses[0]);
 				return status.text;
 			} catch (TwitterException e) {
 				Log.e(TAG, e.toString());
 				e.printStackTrace();
 				return "Failed to post";
 			}
 		}
 
 		// Called when there's a status to be updated
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values); // Not used in this case
 		}
 
 		// Called once the background activity has completed
 		@Override
 		protected void onPostExecute(String result) {
 			Toast.makeText(StatusActivity.this,
 					String.format("Status updated:\n%s", result),
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public void onClick(View v) {
 		new PostToTwitter().execute(editText.getText().toString());
 		Log.d(TAG, "onClicked");
 	}
 
 	public void afterTextChanged(Editable s) {
 		// TODO Auto-generated method stub
 		int charCount = editText.getText().length();
 		textCharsRemaining.setText(Integer.toString(MAX_CHARS - charCount));
 		if((MAX_CHARS - charCount) < 10){
 			textCharsRemaining.setTextColor(Color.YELLOW);
 		}
 		if((MAX_CHARS - charCount) <= 0){
 			textCharsRemaining.setTextColor(Color.RED);
			updateButton.setEnabled(false);
		}else{
			textCharsRemaining.setText(Color.GREEN);
 		}
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		// TODO Auto-generated method stub
 
 	}
 }
