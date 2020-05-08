 package com.marakane.yamba;
 
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
 	private static final String TAG = "StatusActivity";
 	private EditText editText;
 	private Button updateButton;
 	private Twitter twitter;
 	private TextView textCount;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.status);
 
 		editText = (EditText) findViewById(R.id.statusText);
 		editText.addTextChangedListener(this);
 
 		updateButton = (Button) findViewById(R.id.buttonUpdate);
 		updateButton.setOnClickListener(this);
 
 		textCount = (TextView) findViewById(R.id.textCount);
 		textCount.setText("140");
 		textCount.setTextColor(Color.GREEN);
 
 		twitter = new Twitter("student", "password");
 	}
 
 	public void onClick(View v) {
 		String status = editText.getText().toString();
 		new PostToTwitter().execute(status);
 		Log.d(TAG, "onClicked");
 	}
 
 	class PostToTwitter extends AsyncTask<String, Integer, String> {
 
 		@Override
 		protected String doInBackground(String... params) {
 			try {
 				winterwell.jtwitter.Status status = twitter
 						.updateStatus(params[0]);
 				return status.text;
 			} catch (TwitterException e) {
 				Log.d(TAG, e.toString());
 				e.printStackTrace();
 				return "Failed to post";
 			}
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG)
 					.show();
 		}
 
 	}
 
 	public void afterTextChanged(Editable statusText) {
 		int count = 140 - statusText.length();
		textCount.setText(count);
 		textCount.setTextColor(Color.GREEN);
 		if (count < 10) {
 			textCount.setTextColor(Color.YELLOW);
 		}
 		if (count < 10) {
 			textCount.setTextColor(Color.RED);
 		}
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 	}
 
 }
