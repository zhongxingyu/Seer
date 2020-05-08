 package no.uka.findmyapp.ukaprogram.utils;
 
 import java.net.URISyntaxException;
 
 import no.uka.findmyapp.android.rest.client.IntentMessages;
 import no.uka.findmyapp.android.rest.client.RestServiceHelper;
 import no.uka.findmyapp.android.rest.client.UkappsServices;
 import no.uka.findmyapp.android.rest.contracts.UkaEvents.UkaEventContract;
 import no.uka.findmyapp.ukaprogram.activities.Main;
 import no.uka.findmyapp.ukaprogram.wrapper.EventDatabase;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 import android.widget.Toast;
 
 public class EventsUpdater {
 	private static final String debug = "EventsUpdater";
 	
 	private static RestServiceHelper serviceHelper = RestServiceHelper.getInstance(); 
 	private Context context; 
 	
 	public EventsUpdater(Context c) {
 		this.context = c; 
 	}
 	
 	public void initUpdate() {
 		Log.v(debug, "initUpdate starting update routine");
 		if(eventsDatabaseNotEmtpy()) {
 			Log.v(debug, "Event database not empty");
 			startMainActivity();
 		}
 		else {	
 			Log.v(debug, "Empty event database, updating on boot");
 			startupUpdate();
 		}
 	}
 	
 	public void updateEvents() {
 		Log.v(debug, "updateEvents called");
 		try {
 			Log.v(debug, "isOnline " + isOnline());
 			if(isOnline()) { 
 				update(); 
 			}
 			else { 
 				Log.v(debug, "No internet connection!");
 				throw new UpdateException(UpdateException.NO_CONNECTION_EXCEPTION); 
 			}	
 		} catch (UpdateException e) {
 			Log.e(debug, "updateEvents exception caught " + e.getException().getMessage());
 			Toast t = Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG);
 			t.show(); 
 		} 
 	}
 	
 	private void update() throws UpdateException{
 		Log.v(debug, "update called");
 		try {		
 			Log.v(debug, "inside");
 			serviceHelper.callStartService(this.context, UkappsServices.UKAEVENTS, new String[] {"uka11"}); 
 		} catch (URISyntaxException e) {
 			throw new UpdateException(UpdateException.URI_SYNTAX_EXCEPTION, e); 
 		} catch (IllegalAccessException e) {
 			throw new UpdateException(UpdateException.ILLEGAL_ACCESS_EXCEPTION, e); 
 		} catch (InstantiationException e) {
 			throw new UpdateException(UpdateException.INSTANTIATION_EXCEPTION, e); 
 		}
 	}
 	
 	private void startupUpdate() {
 		try {
 			Log.v(debug, "startUpdate setting up IntentReciever and IntentFilter");
 	        ReciveIntent intentReceiver = new ReciveIntent();
 			IntentFilter intentFilter = new IntentFilter(IntentMessages.BROADCAST_INTENT_TOKEN);
 			
 			context.registerReceiver(intentReceiver, intentFilter); 
 			
 			Log.v(debug, "Atempting to clear Events table");
 			EventDatabase.getInstance().clearEventTable(this.context.getContentResolver());
 			
 			updateEvents();
 		} 
 		catch (Exception e) {
 			Log.e(debug, "startupUpdate Runtime error!");
 			Toast t = Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG);
 			t.show(); 
 		} 
 	}
 	
 	private boolean eventsDatabaseNotEmtpy() {
 		Cursor eventCursor = context.getContentResolver().query(UkaEventContract.EVENT_CONTENT_URI, null, null, null, null);
 		if(eventCursor.getCount() > 0) {
 			return true; 
 		}
 		return false; 
 	}
 	
 	private boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 	        return true;
 	    }
 	    return false;
 	}
 	
 	private void startMainActivity() {
 		Log.v(debug, "startMainActivity called");
 		Intent i = new Intent(context, Main.class); 
 		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		this.context.startActivity(i);
 	}
 
 	private class ReciveIntent extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(IntentMessages.BROADCAST_INTENT_TOKEN)) {
 				Log.v(debug, "Intent recieved, starting Main activity");
 				startMainActivity();
 			}
 		}
 	}
 	
 	private static class UpdateException extends Exception {
 		private static final long serialVersionUID = 1042598607999418184L;
 		public static final String ILLEGAL_ACCESS_EXCEPTION = 
 			"Illegal access exception caught during update";
 		public static final String URI_SYNTAX_EXCEPTION = 
 			"URI syntax exception caught during update";
 		public static final String INSTANTIATION_EXCEPTION = 
 			"Instantiation exception caught during update";
 		public static final String NO_CONNECTION_EXCEPTION  = 
 			"No internet connection available!";
 		
 		private Exception e; 
 		
 		public UpdateException(String errorMessage) {
 			super(errorMessage);
 			e = new Exception(errorMessage); 
 		}
 		
 		public UpdateException(String errorMessage, Exception e) {
 			super(errorMessage);
 			this.e = e; 
 		}
 		
 		public Exception getException() { return this.e; }
 	}
 }
