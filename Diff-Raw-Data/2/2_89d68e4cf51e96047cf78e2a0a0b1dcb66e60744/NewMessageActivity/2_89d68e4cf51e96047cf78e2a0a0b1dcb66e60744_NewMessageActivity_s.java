 package ca.ryerson.scs.rus.messenger;
 
 import java.util.Calendar;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import ca.ryerson.scs.rus.MenuActivity;
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.SplashActivity;
 //import ca.ryerson.scs.rus.Preferences.UpdateHandler;
 import ca.ryerson.scs.rus.adapters.HttpRequestAdapter;
 import ca.ryerson.scs.rus.util.DefaultUser;
 import ca.ryerson.scs.rus.util.IntentRes;
 import ca.ryerson.scs.rus.util.URLResource;
 import ca.ryerson.scs.rus.util.ValidityCheck;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import android.widget.TextView;
 
 public class NewMessageActivity extends Activity implements OnClickListener {
 	private ImageButton btnMapView, btnHome, btnMsg, btnPref, btnFriend;
 	private TextView btnSend, tvUsername;
 	private EditText evMessage, evRep;
 
 	Intent intent = getIntent();
 	Context context;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.new_message);
 
 		context = this;
 
 		
 
 		btnMapView = (ImageButton) findViewById(R.id.IBLook);
 		btnHome = (ImageButton) findViewById(R.id.IBHome);
 		btnMsg = (ImageButton) findViewById(R.id.IBMsg);
 		btnPref = (ImageButton) findViewById(R.id.IBPref);
 		btnFriend = (ImageButton) findViewById(R.id.IBFriend);
 		btnSend = (TextView) findViewById(R.id.BtnSend);
 		evMessage = (EditText) findViewById(R.id.EVEmail);
 		evRep = (EditText) findViewById(R.id.EVReceiver);
 
 		btnMapView.setFocusable(true);
 		btnHome.setFocusable(true);
 		btnMsg.setFocusable(true);
 		btnPref.setFocusable(true);
 		btnFriend.setFocusable(true);
 		btnSend.setFocusable(true);
 
 		btnMapView.setOnClickListener(this);
 		btnHome.setOnClickListener(this);
 		btnMsg.setOnClickListener(this);
 		btnPref.setOnClickListener(this);
 		btnFriend.setOnClickListener(this);
 		btnSend.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == btnHome) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Home button");
 			}
 			// TODO: Make it go back to the main page while finishing all other
 			// activities
 
 		} else if (v == btnMapView) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Map View Button");
 			}
 			Intent newIntent = new Intent(IntentRes.SOCIALITE_MAP_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 
 		} else if (v == btnMsg) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Message Button");
 			}
 			Intent newIntent = new Intent(IntentRes.MESSAGE_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 
 		} else if (v == btnFriend) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Friend Button");
 			}
 			Intent newIntent = new Intent(IntentRes.FRIEND_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 
 		} else if (v == btnPref) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Preference Button");
 			}
 			Intent newIntent = new Intent(IntentRes.PREFERENCE_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 		} else if (v == btnSend) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Send Button");
 			}
 
 			if (evMessage.getText().toString() != "") {
 						
 				//needs  user, receiver, message, date
 				String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
 				mydate.replaceAll("\\s","");
 				String URLfinal = ValidityCheck.whiteSpace(URLResource.SEND_MESSAGES
						+ "?user=" + DefaultUser.getUser() + "&receiver=" + intent.getStringExtra("receiver")+ "&message=" + evMessage.getText().toString() + "&date=" + mydate); 
 						
 				System.out.println(URLfinal);
 				
 				
 
 				// Log.i("URLFINAL",URLfinal+"a");				
 				
 				try {
 					JSONObject json = new JSONObject();
 					json.put("username", DefaultUser.getUser());
 					//json.put("receiver", receiverString);
 					json.put("receiver", evRep.getText().toString());
 					json.put("message", evMessage.getText().toString());
 					json.put("date",mydate);
 
 					HttpRequestAdapter.httpRequest(this, URLfinal, new UpdateHandler());
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}else{
 				Toast.makeText(getApplicationContext(), "Message Cannot Be Empty",
 						Toast.LENGTH_LONG).show();
 			}
 
 		}
 
 	}
 
 	private class UpdateHandler implements HttpRequestAdapter.ResponseHandler{
 		@Override
 		public void postResponse(JSONObject response) {
 
 			try {
 				if (response.getString("Status").equals("Success")) {
 					Toast.makeText(context, response.getString("Status"),
 							Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(context, "Service Currently Unavailable",
 							Toast.LENGTH_LONG).show();
 				}
 				
 				finish();
 			}
 
 			catch (JSONException e) {
 				Toast.makeText(context, "Service Currently Unavailable",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 		@Override
 		public void postTimeout() {
 			Toast.makeText(context, "Connection timed out", Toast.LENGTH_LONG)
 					.show();
 		}
 	}
 	
 	
 	/*private class MessageRequestHandler implements
 			HttpRequestAdapter.ResponseHandler {
 
 		@Override
 		public void postResponse(JSONObject response) {
 			Toast.makeText(context, "Message Sent", Toast.LENGTH_LONG)
 					.show();
 			finish();
 		}
 
 		@Override
 		public void postTimeout() {
 			// TODO Auto-generated method stub
 
 		}
 
 	}*/
 
 }
