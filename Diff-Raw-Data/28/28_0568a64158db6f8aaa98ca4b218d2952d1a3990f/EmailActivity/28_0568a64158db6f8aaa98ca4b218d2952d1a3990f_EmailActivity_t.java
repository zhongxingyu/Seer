 package com.CMPUT301F12T07.crowdsource;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 
 public class EmailActivity extends Activity {
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_email);
         
         startEmail();
     }
 
     /**
      * startEmail will retrieve the data from the callers, and set up
      * its Uri and type. With these received 'Extras', they are passed to
      * startEmailIntent for actual emailing.
      */
     private void startEmail() {
         Intent intent = getIntent();
         
         String type = intent.getStringExtra("type");
         String email = intent.getStringExtra("email");
        String title = intent.getStringExtra("title");
         
         try {
 	        Uri received = Uri.parse(intent.getStringExtra("data"));
	        startEmailIntent(email, type, title, received);
         } catch (Exception e) {
         	Log.v("EmailActivity", "Error receiving Uri");
         }
         
         finish();
     }
     
     /**
      * startEmailIntent will setup the intent by attaching the data if need be, and
      * send it to the email client the user picks.
      * 
      * @param email This is the recipient's email address
      * @param type	This would be the fulfillment type
      * @param input	This would the data of the fulfillment
      */
    private void startEmailIntent(String email, String type, String title, Uri input) {
     	Intent intent = new Intent(android.content.Intent.ACTION_SEND);
  
     	intent.setType("message/rfc822");
     	intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
    	intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "CrowdSource: "+title);
     	
     	// if type is not equal to Text, then do not attach
     	if (type.compareTo("Text") != 0) {
     		intent.putExtra(android.content.Intent.EXTRA_TEXT, "This attachment contains your requested task");
     		intent.putExtra(Intent.EXTRA_STREAM, input);
     	} else {
     		intent.putExtra(android.content.Intent.EXTRA_TEXT, "Text:\n\n\n");
     	}
     	
     	intent.setType("text/plain");
     	startActivity(intent);
     }
     
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_email, menu);
         return true;
     }
 }
