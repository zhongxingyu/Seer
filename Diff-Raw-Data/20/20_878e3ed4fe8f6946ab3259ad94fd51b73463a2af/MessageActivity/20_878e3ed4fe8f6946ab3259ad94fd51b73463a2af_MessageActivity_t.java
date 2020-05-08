 package ca.ryerson.scs.rus.messenger;
 
 import ca.ryerson.scs.rus.MenuActivity;
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.SplashActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MessageActivity extends Activity implements OnClickListener {
 
 	private Button btnFriendsList, btnBack;
 	private final String FRIENDS_STRING = "ca.ryerson.scs.rus.messenger.FRIENDS_LIST";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.messages);
 
 		btnFriendsList = (Button) findViewById(R.id.BtnFriendsList);
		btnBack = (Button) findViewById(R.id.BtnBack);
 
 		btnFriendsList.setFocusable(true);
 		btnBack.setFocusable(true);
 
 		btnFriendsList.setOnClickListener(this);
 		btnBack.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == btnBack) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Back Button");
 			}
 			// TODO: Make it go back to the main page while finishing all other
 			// activities
 
 		} else if (v == btnFriendsList) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Friends List Button");
 			}
 			finish();
 			startActivity(new Intent(FRIENDS_STRING));
 		}
 	}
 
 }
