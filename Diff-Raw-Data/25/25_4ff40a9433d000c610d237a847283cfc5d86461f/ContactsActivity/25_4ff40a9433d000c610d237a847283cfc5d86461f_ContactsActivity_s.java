 package com.weemo.sdk.helper.contacts;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.Toast;
 
 import com.weemo.sdk.Weemo;
 import com.weemo.sdk.WeemoCall;
 import com.weemo.sdk.WeemoCall.CallStatus;
 import com.weemo.sdk.WeemoEngine;
 import com.weemo.sdk.event.WeemoEventListener;
 import com.weemo.sdk.event.call.CallStatusChangedEvent;
 import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
 import com.weemo.sdk.helper.ChooseFragment;
 import com.weemo.sdk.helper.ChooseFragment.ChooseListener;
 import com.weemo.sdk.helper.ConnectedService;
 import com.weemo.sdk.helper.R;
 import com.weemo.sdk.helper.call.CallActivity;
 
 /*
  * This is the main activity when the user is connected.
  * From this activity, the user can:
  *  - Poll for the status of a remote contact
  *  - Call a remote contact.
  *  
  * It is also that activity that starts and stops the ConnectedService.
  * This service will run as long as the user is connected. 
  */
 public class ContactsActivity extends Activity implements ChooseListener {
 
 	private static final String LOGTAG = "ContactsActivity";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// This activity can only be launched if the user is connected
 		// Therefore, we launch the ConnectedService
 		startService(new Intent(this, ConnectedService.class));
 		
 		if(savedInstanceState == null) {
 			// Ensure that Weemo is initialized
 			WeemoEngine weemo = Weemo.instance();
 			if (weemo == null) {
 				Log.e(LOGTAG, "ContactsActivity was started while Weemo is not initialized");
 				finish();
 				return ;
 			}
 			
 			// Ensure that there is a connected user
 			String uid = weemo.getUserId();
 			if (uid == null) {
 				Log.e(LOGTAG, "ContactsActivity was started while Weemo is not authenticated");
 				finish();
 				return ;
 			}
 
 			// If a display name is not set, that getDisplayName will return null
 			// In which case we need to ask the user for its display name
 			String displayName = weemo.getDisplayName();
 			boolean askForDisplayName = displayName.isEmpty();
 			
 			// If display name was not set, we display a temporary one that is the user ID
 			if (askForDisplayName)
 				displayName = uid;
 
 			// Display the contact fragment
 			getFragmentManager()
 				.beginTransaction()
 				.add(android.R.id.content, ChooseFragment.newInstance(getString(R.string.check), displayName))
 				.commit();
 		
 			// If we need to ask for the display name, shows the apropriate popup
 			if (askForDisplayName) {
 				String defaultName = "";
 				if (ChooseFragment.ACCOUNTS.containsKey(uid))
 					defaultName = ChooseFragment.ACCOUNTS.get(uid);
 	            AskDisplayNameDialogFragment.newInstance(defaultName).show(getFragmentManager(), null);
 			}
 		}
 		
 		setTitleFromDisplayName();
 	}
 	
 	/*
 	 * When the user has chosen someone to check, starts the dialog fragment that will make the check
 	 */
 	@Override
 	public void onChoose(String contactId) {
 		ContactCheckDialogFragment.newInstance(contactId).show(getFragmentManager(), null);
 	}
 
 	/*
 	 * Set the activity title according to the display name
 	 * If there is no display name, then we use the user ID
 	 */
 	private void setTitleFromDisplayName() {
 		WeemoEngine weemo = Weemo.instance();
 		assert weemo != null;
 		String displayName = weemo.getDisplayName();
 		if (!displayName.isEmpty())
 			setTitle(displayName);
 		else {
 			String uid = weemo.getUserId();
 			assert uid != null;
 			setTitle("{" + uid + "}");
 		}
 	}
 	
 	/*
 	 * Set the display name
 	 */
 	public void setDisplayName(String displayName) {
 		WeemoEngine weemo = Weemo.instance();
 		assert weemo != null;
 		weemo.setDisplayName(displayName);
 
 		// Tell the service the user has changed display name, so the service can update the notification
 		startService(new Intent(this, ConnectedService.class));
 		setTitleFromDisplayName();
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		// This should always be the first statement of onStart
 		Weemo.onActivityStart();
 
 		// Register as event listener
 		Weemo.eventBus().register(this);
 		
 		// Enables or disable the call button according to weemo.canCreateCall()
 		WeemoEngine weemo = Weemo.instance();
 		assert weemo != null;
 		((ChooseFragment) getFragmentManager().findFragmentById(android.R.id.content)).setEnabled(weemo.canCreateCall());
 
 		// If there is a call currently going on,
 		// it's probably because the user has clicked in the notification after going on its device home.
 		// In which case we redirect him to the CallActivity
 		WeemoCall call = weemo.getCurrentCall();
 		if (call != null) {
 			startActivity(
 				new Intent(this, CallActivity.class)
 					.putExtra("callId", call.getCallId())
 			);
 		}
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
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		menu.add(R.string.disconnect).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override public boolean onMenuItemClick(MenuItem item) {
 				Weemo.destroy();
 				return true;
 			}
 		});
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/*
 	 * This listener method catches CanCreateCallChangedEvent
 	 * 1. It is annotated with @WeemoEventListener
 	 * 2. It takes one argument which type is CanCreateCallChangedEvent
 	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
 	 */
 	@WeemoEventListener
 	public void onCanCreateCallChanged(CanCreateCallChangedEvent e) {
 		CanCreateCallChangedEvent.Error error = e.getError();
 
 		// If there is an error, the action we'll take depends on the error
 		if (error != null) {
 			switch (error) {
 			// This is a loss of network. We cannot create call, but the network should be back anytime soon.
 			// We disable the call button.
 			case NETWORK_LOST:
 				((ChooseFragment) getFragmentManager().findFragmentById(android.R.id.content)).setEnabled(false);
 				break;
 				
 			// This is either a system error or the Weemo engine was destroyed (by the user when he clicks disconnect)
 			// We print the error message and finish the activity, the service (and the application along)
			case SYSTEM_ERROR:
			case DESTROYED:
 				Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
 				stopService(new Intent(ContactsActivity.this, ConnectedService.class));
 				finish();
 				break;
 			}
 			return ;
 		}
 
 		// If there is no error, we can create call
 		// We enable the call button.
 		((ChooseFragment) getFragmentManager().findFragmentById(android.R.id.content)).setEnabled(true);
 	}
 	
 	/*
 	 * This listener method catches CallStatusChangedEvent
 	 * 1. It is annotated with @WeemoEventListener
 	 * 2. It takes one argument which type is CallStatusChangedEvent
 	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
 	 */
 	@WeemoEventListener
 	public void onCallStatusChanged(CallStatusChangedEvent e) {
 		// If there's a call whose status is newly to PROCEEDING, this means the user has initiated an outgoing call
 		// and that this call is currently ringing on the remote user's device.
 		// In which case, we show the ContactCallingDialogFragment that will monitor the babysteps of this newborn call
 		if (e.getCallStatus() == CallStatus.PROCEEDING) {
 			ContactCallingDialogFragment dialog = ContactCallingDialogFragment.newInstance(e.getCall().getCallId());
 			dialog.setCancelable(false);
 			dialog.show(getFragmentManager(), null);
 		}
 	}
 }
