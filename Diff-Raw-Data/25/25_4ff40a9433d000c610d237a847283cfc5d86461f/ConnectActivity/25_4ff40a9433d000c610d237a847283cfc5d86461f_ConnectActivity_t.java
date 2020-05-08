 package com.weemo.sdk.helper;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.weemo.sdk.Weemo;
 import com.weemo.sdk.WeemoEngine;
 import com.weemo.sdk.event.WeemoEventListener;
 import com.weemo.sdk.event.global.AuthenticatedEvent;
 import com.weemo.sdk.event.global.ConnectedEvent;
 import com.weemo.sdk.helper.ChooseFragment.ChooseListener;
 import com.weemo.sdk.helper.contacts.ContactsActivity;
 
 /*
  * This is the first activity being launched.
  * Its role is to handle connection and authentication of the user.
  */
 public class ConnectActivity extends Activity implements ChooseListener {
 
 	private boolean hasLoggedIn = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Checks if Weemo is already initialized and authenticated.
 		// If it is, it is probably because the user clicked on the service notification.
 		// In which case, the user is redirected to the second screen
 		WeemoEngine weemo = Weemo.instance();
 		if (weemo != null && weemo.isAuthenticated()) {
 			hasLoggedIn = true;
 			startActivity(
 				new Intent(this, ContactsActivity.class)
 					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
 				);
 			finish();
 			return ;
 		}
 		
 		// This activity starts with a LoadingFragment (which makes the user wait while Weemo is connecting)
 		// The connection is started in onStart, after registering the listener
 		// (so that the ConnectedEvent can't be launched while we are not yet listening).
 		if (savedInstanceState == null) {
 			LoadingDialogFragment dialog = LoadingDialogFragment.newFragmentInstance(getString(R.string.connection_title), getString(R.string.connection_text));
 			dialog.setCancelable(false);
 			dialog.show(getFragmentManager(), "dialog");
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		// This should always be the first statement of onStart
 		Weemo.onActivityStart();
 
 		// Register the activity as event listener
 		Weemo.eventBus().register(this);
 
 		// Initialize Weemo, can be called multiple times
 		Weemo.initialize(getString(R.string.weemo_mobileAppId), this);
 	}
 
 	@Override
 	protected void onStop() {
 		// Unregister as event listener
 		Weemo.eventBus().unregister(this);
 
 		// This should always be the last statement of onStop
 		Weemo.onActivityStop();
 
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		// If this activity is destroyed with hasLoggedIn is true,
 		// this means it is destroyed after CallActivity being displayed.
 		// However, if hasLoggedIn is false, it means that the activity is destroyed
 		// because the user has got out of the application without logging in.
 		// In this case we need to stop the Weemo engine.
 		if (!hasLoggedIn)
 			Weemo.destroy();
 		
 		super.onDestroy();
 	}
 	
 	/*
 	 * This is called by the ChoseFragment when user clicks on "login" button
 	 */
 	@Override
 	public void onChoose(String userId) {
 		WeemoEngine weemo = Weemo.instance();
 		// Weemo must be instanciated at this point
 		if (weemo == null) {
 			finish();
 			return ;
 		}
 
 		// Start authentication with the userId chosen by the user.
		weemo.authenticate(userId, WeemoEngine.UserType.INTERNAL);
 		
		LoadingDialogFragment dialog = LoadingDialogFragment.newFragmentInstance(userId, getString(R.string.authentication_title));
		dialog.setCancelable(false);
		dialog.show(getFragmentManager(), "dialog");
 	}
 
 	/*
 	 * This listener catches ConnectedEvent
 	 * 1. It is annotated with @WeemoEventListener
 	 * 2. It takes one argument which type is ConnectedEvent
 	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
 	 * */
 	@WeemoEventListener
 	public void onConnected(ConnectedEvent e) {
 		ConnectedEvent.Error error = e.getError();
 		
 		// If there is an error, this means that connection failed
 		// So we display the English description of the error
 		// We then finish the application as nothing can be done (in this app) without being connected
 		if (error != null) {
 			Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
 			finish();
 			return ;
 		}
 		
 		// If there is no error, everything went normal, connection succeeded.
 		// In this case, we stop the loading dialog and show login fragment
 		Log.i("=====", "LAUNCHING LOGIN");
 		DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
 		if (dialog != null)
 			dialog.dismiss();
         getFragmentManager().beginTransaction().replace(android.R.id.content, ChooseFragment.newInstance(getString(R.string.log_in))).commit();
 	}
 
 	/*
 	 * This listener catches AuthenticatedEvent
 	 * 1. It is annotated with @WeemoEventListener
 	 * 2. It takes one argument which type is AuthenticatedEvent
 	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
 	 */
 	@WeemoEventListener
 	public void onAuthenticated(AuthenticatedEvent e) {
 		AuthenticatedEvent.Error error = e.getError();
 		
 		// If there is an error, this means that authentication failed
 		// So we display the English description of the error
 		// We then go back to the login fragment so that authentication can be tried again
 		if (error != null) {
 			Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
 			DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
 			if (dialog != null)
 				dialog.dismiss();
 			return ;
 		}
 		
 		// If there is no error, everything went normal, go to call activity
 		hasLoggedIn = true;
 		startActivity(new Intent(this, ContactsActivity.class));
 		finish();
 	}
 
 
 }
