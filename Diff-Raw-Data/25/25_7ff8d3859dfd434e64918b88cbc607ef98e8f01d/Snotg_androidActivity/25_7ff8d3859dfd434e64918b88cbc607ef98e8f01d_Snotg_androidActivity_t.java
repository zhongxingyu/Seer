 package edu.depaul.snotg_android.Activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
import edu.depaul.snotg_android.R;
import edu.depaul.snotg_android.Chat.XMPPClient;
import edu.depaul.snotg_android.Layout.TabBarWidget;
 
 public class Snotg_androidActivity extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
         
        
         //Just getting into my map activity from here with the login click for now
         View loginButton = findViewById(R.id.button1);
         loginButton.setOnClickListener(this);
 
         
     }
 
 	@Override
 	public void onClick(View v) {
 		
 		//go to main menu from login
 		Log.i("LOgin", "about to start Heartbeat....");
 		new HeartbeatTask().execute(null);
 		
 		Intent m = new Intent(this, TabBarWidget.class);
		//////placeholder here for testing chat without going through client.
		//Intent chat = new Intent(this, XMPPClient.class);
		
 		startActivity(m);		
		//startActivity(chat);
 	}
 }
