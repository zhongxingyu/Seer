 package com.example.shutapp;
 
 import static com.example.shutapp.MiscResources.PROJECT_ID;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 
 import com.google.android.gcm.GCMRegistrar;
 
 
 import com.google.android.gcm.GCMRegistrar;
 
 
 //Got a lot of help and inspiration from http://avilyne.com/?p=267
 
 public class ChatActivity extends Activity {
 
 	// This tag is used in Log.x() calls
 	private static final String TAG = "MainActivity";
 
 
 
 	
 
 
 
 		// This string will hold the lengthy registration id that comes
 		// from GCMRegistrar.register()
 		private String regId = "";
 
 		// These strings are hopefully self-explanatory
 		private String registrationStatus = "Not yet registered";
 		private String broadcastMessage = "No broadcast message";
 
 		// This intent filter will be set to filter on the string "GCM_RECEIVED_ACTION"
 		private IntentFilter gcmFilter;
 
 		// textviews used to show the status of our app's registration, and the latest
 		// broadcast message.
 		private TextView tvRegStatusResult;
 		private TextView tvBroadcastMessage;
 		private TextView tvChatroomLabel;
 
 		private EditText etMessageInput;
 		private EditText etChatLogHistory;
 		ScrollView svChatLog;
 		LinearLayout llChatLog;
 
 
 
 		Chatroom chatroom;
 		// This broadcastreceiver instance will receive messages broadcast
 		// with the action "GCM_RECEIVED_ACTION" via the gcmFilter
 
 		// A BroadcastReceiver must override the onReceive() event.
 		private BroadcastReceiver gcmReceiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 
 				broadcastMessage = intent.getExtras().getString("gcm");
 				if (broadcastMessage != null) {
 					// display our received message
 					translateMessage(broadcastMessage);
 					//appendToChatLogHistory("test", broadcastMessage);
 				}
 			}
 
 
 		};
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			setContentView(R.layout.activity_chat);
 
 			Intent intent = getIntent();
 			String chatroomName = intent.getStringExtra(NearbyConversationsActivity.EXTRA_MESSAGE);
 
 			chatroom = Chatrooms.getByName(chatroomName);
 			getWindow().setSoftInputMode(
 				      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 			/* NO NEED FOR THESE ATM
 			tvChatroomLabel = (TextView) findViewById(R.id.tv_chatroom_label);
 			tvChatroomLabel.setText(chatroom.getName());
 		
 			tvBroadcastMessage = (TextView) findViewById(R.id.tv_message);
 			tvRegStatusResult = (TextView) findViewById(R.id.tv_reg_status_result);
 			*/
 
 			svChatLog = (ScrollView) findViewById(R.id.sv_chat_log);
 			llChatLog = (LinearLayout) findViewById(R.id.ll_chat_log);
 			etChatLogHistory = new EditText(this);
			
 			etChatLogHistory.setClickable(false);
 			etChatLogHistory.setKeyListener(null);
 			llChatLog.addView(etChatLogHistory);
 
 
 
 			// Create our IntentFilter, which will be used in conjunction with a
 			// broadcast receiver.
 			gcmFilter = new IntentFilter();
 			gcmFilter.addAction("GCM_RECEIVED_ACTION");
 
 			registerClient();
 		}
 
 		// This registerClient() method checks the current device, checks the
 		// manifest for the appropriate rights, and then retrieves a registration id
 		// from the GCM cloud.  If there is no registration id, GCMRegistrar will
 		// register this device for the specified project, which will return a
 		// registration id.
 		public void registerClient() {
 
 			try {
 				// Check that the device supports GCM (should be in a try / catch)
 				GCMRegistrar.checkDevice(this);
 
 				// Check the manifest to be sure this app has all the required
 				// permissions.
 				GCMRegistrar.checkManifest(this);
 
 				// Get the existing registration id, if it exists.
 				regId = GCMRegistrar.getRegistrationId(this);
 
 				if (regId.equals("")) {
 
 					//registrationStatus = "Registering...";
 
 					//tvRegStatusResult.setText(registrationStatus);
 
 					// register this device for this project
 					GCMRegistrar.register(this, PROJECT_ID);
 					regId = GCMRegistrar.getRegistrationId(this);
 
 					//registrationStatus = "Registration Acquired";
 
 					// This is actually a dummy function.  At this point, one
 					// would send the registration id, and other identifying
 					// information to your server, which should save the id
 					// for use when broadcasting messages.
 					sendRegistrationToServer();
 
 					Log.d(TAG, "sendregtoserver has been initialized");
 					//registrationStatus = "Registration of regid done";
 
 				} else {
 					//registrationStatus = "Already registered";
 
 				}
 
 
 			} catch (Exception e) {
 
 				e.printStackTrace();
 				registrationStatus = e.getMessage();
 
 			}
 
 			Log.d(TAG, registrationStatus);
 			//tvRegStatusResult.setText(registrationStatus);
 
 			// This is part of our CHEAT.  For this demo, you'll need to
 			// capture this registration id so it can be used in our demo web
 			// service.
 			Log.d(TAG, regId);
 
 		}
 
 		private void sendRegistrationToServer() {
 
 			new HttpMessage(StringLiterals.DBREGISTER_MESSAGE_TYPE, Settings.getNickname(), regId, null);
 
 
 		}
 
 		// If the user changes the orientation of his phone, the current activity
 		// is destroyed, and then re-created.  This means that our broadcast message
 		// will get wiped out during re-orientation.
 		// So, we save the broadcastmessage during an onSaveInstanceState()
 		// event, which is called prior to the destruction of the activity.
 		@Override
 		public void onSaveInstanceState(Bundle savedInstanceState) {
 
 			super.onSaveInstanceState(savedInstanceState);
 
 			savedInstanceState.putString("BroadcastMessage", broadcastMessage);
 
 		}
 
 		// When an activity is re-created, the os generates an onRestoreInstanceState()
 		// event, passing it a bundle that contains any values that you may have put
 		// in during onSaveInstanceState()
 		// We can use this mechanism to re-display our last broadcast message.
 
 		@Override
 		public void onRestoreInstanceState(Bundle savedInstanceState) {
 
 			super.onRestoreInstanceState(savedInstanceState);
 
 			broadcastMessage = savedInstanceState.getString("BroadcastMessage");
 			tvBroadcastMessage.setText(broadcastMessage);
 
 		}
 
 		// If our activity is paused, it is important to UN-register any
 		// broadcast receivers.
 		@Override
 		protected void onPause() {
 
 			unregisterReceiver(gcmReceiver);
 			super.onPause();
 		}
 
 		// When an activity is resumed, be sure to register any
 		// broadcast receivers with the appropriate intent
 		@Override
 		protected void onResume() {
 			super.onResume();
 			registerReceiver(gcmReceiver, gcmFilter);
 
 		}
 		// NOTE the call to GCMRegistrar.onDestroy()
 		@Override
 		public void onDestroy() {
 
 			GCMRegistrar.onDestroy(this);
 
 			super.onDestroy();
 		}
 
 		//method called when send button is clicked
 		public void sendMessage(View view){
 			etMessageInput = (EditText) findViewById(R.id.written_msg);
			String messageToSend = etMessageInput.getText().toString();
			if(messageToSend.equals("") || messageToSend == null)
				return;
			new HttpMessage(StringLiterals.CHATROOM_MESSAGE_TYPE, chatroom.getName(), Settings.getNickname(), messageToSend);
 			etMessageInput.setText("");
 		}
 
 		private void appendToChatLogHistory(String username, String message) {
 			if (username != null && message != null) {
 				etChatLogHistory.append(username + ": ");								
 				etChatLogHistory.append(message + "\n");	
 			}
 		}
 
 		private void translateMessage(String msg) {
 			String[] temp;
 			temp = msg.split(":");
 			String username = temp[0];
 			String message;
 			if(temp.length > 2){
 				StringBuffer sb = new StringBuffer();
 				for(int i = 1; i < temp.length; i++){
 					sb.append(temp[i]);
 					sb.append(":");
 				}
 				sb.deleteCharAt(sb.length()-1);
 				message = sb.toString();
 			}
 			else{
 				message = temp[1];
 			}
 
 			appendToChatLogHistory(username, message);
 
 		}
 
 		public void toGpsActivity(View view){
 			Intent intent = new Intent(this, GpsActivity.class);
 			startActivity(intent);
 			overridePendingTransition(0, 0);
 		} 
 
 		public void toNearbyConversationsActivity(View view){
 			Intent intent = new Intent(this, NearbyConversationsActivity.class);
 			startActivity(intent);
 			overridePendingTransition(0, 0);
 		}
 
 		public void toChatActivity(View view){
 			Intent intentToRedirect = new Intent(this, ChatActivity.class);
 			startActivity(intentToRedirect);
 			overridePendingTransition(0, 0);
 		}
 
 		public void toSettingsActivity(View view){
 			Intent intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 			overridePendingTransition(0, 0);
 		} 
 
 		/* save the message written in dialog*/
 		public String saveMessage(View view){
 			EditText thetext = (EditText)findViewById(R.id.written_msg);
 			String chatmessage = thetext.getText().toString();
 			return chatmessage;
 
 		}
 	}
 
 
 
