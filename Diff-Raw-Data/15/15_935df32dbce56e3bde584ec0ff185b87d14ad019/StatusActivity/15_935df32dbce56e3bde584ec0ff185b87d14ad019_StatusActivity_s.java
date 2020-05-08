 package com.markdanks.yamba;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StatusActivity extends Activity implements OnClickListener,
 		TextWatcher, OnSharedPreferenceChangeListener {
 	public static final String TAG = "StatusActivity";
 	EditText editText;
 	TextView textCharsRemaining;
 	Button updateButton;
 	SharedPreferences prefs;
 	public static final int MAX_CHARS = 140;
 
 	Twitter twitter;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.status);
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
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
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.itemPrefs:
 			// Launch the preferences activity
 			startActivity(new Intent(this, PrefsActivity.class));
 			break;
 		}
 		return true;
 	}
 
 	private Twitter getTwitter() {
 		if (twitter == null) {
 			twitter = new Twitter(prefs.getString("username", "student"),
 					prefs.getString("password", "password"));
 			twitter.setAPIRootUrl(prefs.getString("apiRoot",
 					"http://yamba.marakana.com/api"));
 		}
 
 		return twitter;
 	}
 
 	// Asynchronously posts to twitter
 	class PostToTwitter extends AsyncTask<String, Integer, String> {
 		// Called to initiate the background activity
 		@Override
 		protected String doInBackground(String... statuses) { //
 			try {
 				Twitter.Status status = getTwitter().updateStatus(statuses[0]);
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
 		if (v == updateButton) {
 			new PostToTwitter().execute(editText.getText().toString());
 			Log.d(TAG, "onClicked");
 		}
 	}
 
 	public void afterTextChanged(Editable s) {
 		int charCount = editText.getText().length();
 		textCharsRemaining.setText(Integer.toString(MAX_CHARS - charCount));
 		if ((MAX_CHARS - charCount) < 10) {
 			// Warning: Yellow text when less than 10 chars remaining
 			textCharsRemaining.setTextColor(Color.YELLOW);
 		}
 		if ((MAX_CHARS - charCount) <= 0) {
 			// Red when you can't post any more
 			// Also disable the button
 			textCharsRemaining.setTextColor(Color.RED);
 			updateButton.setEnabled(false);
 		} else {
 			// Re-enable the char count colour and re-enable the
 			textCharsRemaining.setTextColor(Color.GREEN);
 			updateButton.setEnabled(true);
 		}
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
 		// Replace the twitter object with one containing the new value
 		twitter = null;
 		twitter = getTwitter();
 	}
 }
