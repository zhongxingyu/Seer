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
 import android.widget.ImageButton;
 import android.widget.Button;
 
 public class MessageActivity extends Activity implements OnClickListener {
 	private ImageButton btnMapView, btnHome, btnMsg, btnPref, btnFriend;
 	private final String SOCIALITE_MAP_STRING="ca.ryerson.scs.rus.socialite.SOCIALITE_MAP";
 	private final String MESSAGE_STRING="ca.ryerson.scs.rus.messenger.MESSAGES_LIST";
 	private final String PREFERENCE_STRING="ca.ryerson.scs.rus.PREFERENCES";
 	private final String FRIEND_STRING="ca.ryerson.scs.rus.messenger.FRIENDS_LIST";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.messages);
 
 		btnMapView = (ImageButton) findViewById(R.id.IBLook);
 		btnHome = (ImageButton) findViewById(R.id.IBHome);
 		btnMsg = (ImageButton) findViewById(R.id.IBMsg);
 		btnPref = (ImageButton) findViewById(R.id.IBPref);
 		btnFriend = (ImageButton) findViewById(R.id.IBFriend);
 		
				
 		btnMapView.setFocusable(true);
 		btnHome.setFocusable(true);
 		btnMsg.setFocusable(true);
 		btnPref.setFocusable(true);
 		btnFriend.setFocusable(true);
 		
 		btnMapView.setOnClickListener(this);
 		btnHome.setOnClickListener(this);
 		btnMsg.setOnClickListener(this);
 		btnPref.setOnClickListener(this);
 		btnFriend.setOnClickListener(this);
 	}
 
 	@Override
 	  public void onClick(View v) {
 			if (v == btnHome) {
 				if (SplashActivity.DEBUG) {
 					if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Home button");
 				}
 				// TODO: Make it go back to the main page while finishing all other activities
 				
 			}else if (v == btnMapView) {
 				if (SplashActivity.DEBUG) {
 					if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Map View Button");
 				}
 				finish();
 				startActivity(new Intent(SOCIALITE_MAP_STRING));
 			
 			}else if (v == btnMsg) {
 				if (SplashActivity.DEBUG){
 					if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Message Button");
 				}
 				finish();
 				startActivity(new Intent(MESSAGE_STRING));
 			
 			}else if (v == btnFriend) {
 				if (SplashActivity.DEBUG){
 					if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Friend Button");
 				}
 				finish();
 				startActivity(new Intent(FRIEND_STRING));
 				
 			}else if (v == btnPref) {
 				if (SplashActivity.DEBUG){
 					if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Preference Button");
 					}
 				finish();
 				startActivity(new Intent(PREFERENCE_STRING));
 				}
 			}
 
 }
