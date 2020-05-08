 package com.horeca;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MyActivity extends Activity {
 
 	private boolean signedIn = false;
 	private MenuItem signUp = null;
 	private MenuItem signIn = null;
 	private MenuItem signOut = null;
 	private MenuItem profile = null;
 	
 	public void refreshMenuSigning() {
 		if (signUp != null) { // Sometimes they become null again, so let's just ignore it
 			                  // weird bug...
 			if (User.isSignedIn()) {
 				signUp.setVisible(false);
 				signIn.setVisible(false);
 				signOut.setVisible(true);
 				profile.setVisible(true);
 			} else {
 				signUp.setVisible(true);
 				signIn.setVisible(true);
 				signOut.setVisible(false);
 				profile.setVisible(false);
 			}
 		}
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         signUp = menu.findItem(R.id.action_signUp);
         signIn = menu.findItem(R.id.action_signIn);
         signOut = menu.findItem(R.id.action_signOut);
         profile = menu.findItem(R.id.action_profile);
         refreshMenuSigning();
         return true;
     }
     public boolean onOptionsItemSelected(MenuItem item) {
     	Log.i("Menu", String.valueOf(item.getItemId()));
     	switch (item.getItemId()) {
     	case R.id.action_signUp:
     			startActivity(new Intent(this, SignUpActivity.class));
     			
             return true;
         case R.id.action_signIn:
     		startActivity(new Intent(this, SignInActivity.class));
    		return true;
         case R.id.action_signOut:
         	User.signOut();
         	refreshMenuSigning();
         	refreshSigning();
         	return true;
         case R.id.action_profile:
        	startActivity(new Intent(this, ProfileActivity.class));
         	return true;
     	}
         return false;
     }
     
     @Override
     public void onRestart() {
     	super.onRestart();
     	refreshSigning();
     }
     
     protected void refreshSigning() {
 		boolean before = signedIn;
 		signedIn = User.isSignedIn();
 		if (before != signedIn) {
 			refreshMenuSigning();
 			if (signedIn) {
 				notifySignedIn();
 			} else {
 				notifySignedOut();
 			}
 		}
     }
     
     // Override to refresh display when the user sign out !!! (example in PlatActivity)
     protected void notifySignedOut() {
     }
     // Override to refresh display when the user sign in !!! (example in PlatActivity)
     protected void notifySignedIn() {
     }
 }
