 package uw.changecapstone.tweakthetweet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 
 public class MainActivity extends Activity implements DialogListener{
 
 
 	private final static int ACTIVITY_COMPOSE = 5;
 
 	final static String TWEET_STRING = "TWEET_STRING";
 	public final static String LOCATION_TEXT = "uw.changecapstone.tweakthetweet.MESSAGE";
 
 	private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
 	private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
 	private static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";
 	private static final String LOGIN_DIALOG_TAG = "logindialog";
 	private static final String NETWORK_DIALOG_TAG = "networkdialog";
 	private final static String DATA_ON = "data";
 	
 	private SharedPreferences pref; 
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("COMPOSE", "activity result");
 		super.onActivityResult(requestCode, resultCode, data);
 		String tweet = data.getStringExtra(TWEET_STRING);
 		Intent intent = new Intent(this, TweetActivity.class);
 		intent.putExtra(TWEET_STRING, tweet);
 		startActivity(intent);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		checkNetworkStatus();
 		checkLogInStatus();
 	}
 	
 	private void checkLogInStatus() {
 	  if (!pref.getBoolean(PREF_KEY_TWITTER_LOGIN, false)) {
 		  DialogFragment logIn = new LoginDialogFragment();
 		  logIn.show(getFragmentManager(), LOGIN_DIALOG_TAG);
 	  }
 	}
 	
 	private void checkNetworkStatus() {
 		ConnectivityManager cm =
		        (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork.isConnectedOrConnecting();
 		if (!isConnected) {
 			DialogFragment network = new NetworkDialogFragment();
 			network.show(getFragmentManager(), NETWORK_DIALOG_TAG);
 		} else {
 			Editor e = pref.edit();
 			e.putBoolean(DATA_ON, true);
 			e.commit();
 		}
 		
 	}
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public void startSettings(View view) {
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 	}
 
 	public void printDebug(View view) {
 		Intent intent = new Intent(this, DebugActivity.class); 
 		startActivity(intent);
 	}
 
 	// debug compose button
 	public void composeTweet(View view) {
 		Intent intent = new Intent(this, ComposeActivity.class);
 		Log.d("COMPOSE", "startig compose");
 		startActivityForResult(intent, ACTIVITY_COMPOSE);
 	}
 	
 	// manually authenticate to twitter
 	public void authenticateTwitter(View view) {
 		Intent i = new Intent(this, OAuthTwitterActivity.class);
 		startActivity(i);
 	}
 		
 	public void testComposeString(View view) {
 		Intent i = new Intent(this, TestStringBuilder.class);
 		startActivity(i);
 		
 	}
 
 	public void showMap(View view){
 		Intent intent = new Intent(this, MapDisplayActivity.class);
 		startActivity(intent);
 	}
 	/*when the user clicks the "Enter" button, 
 	 * we are going to read the textfield content and 
 	 * do some validity checks before we show/zoom map*/
 	public void readLocationMessage(View view){
 		Intent intent = new Intent(this, LocationAndMapActivity.class);
 	    EditText editText = (EditText) findViewById(R.id.edit_message);
 	    String message = editText.getText().toString();
 	    intent.putExtra(LOCATION_TEXT, message);
 	    startActivity(intent);
 	}
 
 	
 	public void twitterLogout(View view) {
 		if (pref.getBoolean(PREF_KEY_TWITTER_LOGIN, false)) {
 			Editor e = pref.edit();
 			e.remove(PREF_KEY_OAUTH_TOKEN);
 			e.remove(PREF_KEY_OAUTH_SECRET);
 			e.remove(PREF_KEY_TWITTER_LOGIN);
 			e.remove("USERNAME");
 			e.commit();
 		}
 	
 		
 	}
 	
 	public void setUseSMS() {
 		Editor e = pref.edit();
		e.putBoolean(DATA_ON, true);
 		e.commit();
 	}
 
 
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		String tag = dialog.getTag();
 		if (tag.equals(LOGIN_DIALOG_TAG)) {
 			Intent i = new Intent(this, OAuthTwitterActivity.class);
 			startActivity(i);
 		} else if (tag.equals(NETWORK_DIALOG_TAG)) {
 			setUseSMS();
 		}
 		
 	}
 
 	@Override
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		
 		
 	}
 
 
 
 }
