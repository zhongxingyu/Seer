 package com.lghs.stutor;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class session extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_session);
         /*
          * voids for the following actions are below.
          * Mark as Busy (tutor)
          * Start Server (tutor)
          * Start client (Local) (tutor)
          * Then client  connects (tutee)
         * Commence with blah blah blah
         */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void onBackPressed()
 	{
 		Toast msg = Toast.makeText(this, "GO BACK!", 3);
 		msg.show();
 	}
     
     public void markasbusy(){
     	//Only occurs on tutor side.
     }
     public void server(){
     	//Only starts on tutor side.
     	Server server = new Server();
     	
     }
     public void client(){
     	//Depending on login type this will connect to localhost or IP from the database.
     	Client client = new Client();
     }
     
 }
