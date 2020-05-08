 package ca.ryerson.scs.rus;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 
 import android.widget.ImageButton;
 
 public class MenuActivity extends Activity implements OnClickListener {
 	// Tag for debugging
 	public static final String TAG = "RyeSocial";
 
 	private final String SOCIALITE_LIST_STRING = "ca.ryerson.scs.rus.socialite.SOCIALITE_LIST";
 	private final String MESSAGES_STRING = "ca.ryerson.scs.rus.messenger.MESSAGES_LIST";
 	private final String FRIENDS_STRING = "ca.ryerson.scs.rus.messenger.FRIENDS_LIST";
 	private final String PREFERENCES_STRING = "ca.ryerson.scs.rus.PREFERENCES";
 
	private ImageButton btnLookAround, btnMessages, btnPreferences,
 			btnFriendsList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.menu);
 
		btnLookAround = (ImageButton) findViewById(R.id.IBLook);
		btnMessages = (ImageButton) findViewById(R.id.IBMsg);
		btnPreferences = (ImageButton) findViewById(R.id.IBPref);
		btnFriendsList = (ImageButton) findViewById(R.id.IBFriend);
 
 		btnLookAround.setFocusable(true);
 		btnMessages.setFocusable(true);
 		btnPreferences.setFocusable(true);
 		btnPreferences.setFocusable(true);
 		btnFriendsList.setFocusable(true);
 
 		btnLookAround.setOnClickListener(this);
 		btnMessages.setOnClickListener(this);
 		btnPreferences.setOnClickListener(this);
 		btnPreferences.setOnClickListener(this);
 		btnFriendsList.setOnClickListener(this);
 	}
 
 	public void onClick(View v) {
 		if (v == btnLookAround) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Look Around Button");
 			}
 			startActivity(new Intent(SOCIALITE_LIST_STRING));
 		}  else if (v == btnMessages) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Messenger Button");
 			}
 			startActivity(new Intent(MESSAGES_STRING));
 		} else if (v == btnPreferences) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Preferences Button");
 			}
 			startActivity(new Intent(PREFERENCES_STRING));
 		} else if (v == btnFriendsList) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Friends List Button");
 			}
 			startActivity(new Intent(FRIENDS_STRING));
 		}
 	}
 }
