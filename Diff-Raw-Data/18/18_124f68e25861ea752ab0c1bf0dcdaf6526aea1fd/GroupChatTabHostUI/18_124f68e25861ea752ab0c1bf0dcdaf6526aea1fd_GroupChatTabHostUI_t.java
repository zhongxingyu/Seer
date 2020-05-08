 package spam.me;
 
 import spam.me.R;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TabHost;
 import android.widget.Toast;
 
 public class GroupChatTabHostUI extends Activity{
 	//SpamMeFacade - API for application
 	private SpamMeFacade spamMeFacade = new SpamMeFacade();
 	
 	
 	EditText inputPhoneNo;
 	EditText inputMsg;
 	
 	/** Called when the activity is first created. */ 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //Setting xml file for UI
         setContentView(R.layout.groupchattabhost);
         
         //Setting up tabs
         TabHost tabHost = (TabHost) this.findViewById(R.id.groupchattabhost);  // The activity TabHost
         doTabSetup(tabHost);
        
        //spamMeFacade.addFriend();
 
         tabHost.setCurrentTab(0);
         
         inputPhoneNo = (EditText)findViewById(R.id.PhoneNoTxt);
         inputMsg = (EditText)findViewById(R.id.messageTxt);
         
     }
 	
 	//Handler for SendSMS button
 	public void SendSMSButtonClicked(View v){
 		Toast.makeText(getBaseContext(), 
 				"Send SMS got clicked", 
 				Toast.LENGTH_SHORT).show();
 		
 		
 		String number = inputPhoneNo.getText().toString();
 		String msg = inputMsg.getText().toString();
 		if (number.length()>0 && msg.length()>0){
 			/*
 			Bundle bundle = new Bundle();
 			bundle.putString("message", msg); 
 			bundle.putString("phoneNumber", number);
 			Intent phoneInterfaceIntent = new Intent(this.getApplicationContext(), PhoneInterface.class);
 			phoneInterfaceIntent.putExtras(bundle);
 			startActivityForResult(phoneInterfaceIntent, 0);
 			*/
 			spamMeFacade.sendMsg(msg, number);
 			//sendSMS(msg, number);
 		}
 		else 
 			Toast.makeText(getBaseContext(), 
 					"Please enter both number and message.", 
 					Toast.LENGTH_SHORT).show();
 					
 	}
 		
 	/*
 	public void sendSMS(String msg, String number){
 		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, GroupChatTabHostUI.class), 0);
 		SmsManager sms = SmsManager.getDefault();
 		sms.sendTextMessage(number, null, msg, pi, null);
 	}
 	*/
     /*
      * Set up the tabs in the tab host
      */
 	private void doTabSetup(TabHost tHost){
         Resources res = getResources(); // Resource object to get Drawables
         
         TabHost.TabSpec spec;  // Reusable TabSpec for each tab
         tHost.setup();
         
         // Set the tabs to change views when clicked
         spec = tHost.newTabSpec("messages");
         spec.setIndicator("Messages", res.getDrawable(R.drawable.icon));
         spec.setContent(R.id.messagetab);
         tHost.addTab(spec);
         
         spec = tHost.newTabSpec("members");
         spec.setIndicator("Members", res.getDrawable(R.drawable.icon));
         spec.setContent(R.id.memberstab);
         tHost.addTab(spec);	
         
         spec = tHost.newTabSpec("options");
         spec.setIndicator("Options", res.getDrawable(R.drawable.icon));
         spec.setContent(R.id.optionstab);
         tHost.addTab(spec);	
 		
 	}
 }
