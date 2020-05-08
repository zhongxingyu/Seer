 package com.appchallenge.eventspark;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.appchallenge.eventspark.ReportDialogFragment.ReportDialogListener;
 import com.appchallenge.eventspark.ReportDialogFragment.ReportReason;
 import com.facebook.Session;
 import com.google.android.gms.maps.model.LatLng;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ActivityCompat;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.internal.view.menu.MenuBuilder;
 import android.util.Log;
 import android.view.Menu;
import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventDetails extends ActionBarActivity implements ReportDialogListener {
 	// Private members containing the Event information.
 	private Event event;
 	private LatLng userLocation;
 	
 	/**
 	 * Indicates whether we have attended the event already.
 	 */
 	private Boolean attended;
 
 	/**
 	 * The user's profile picture, which is loaded async and preserved through
 	 * configuration changes.
 	 */
 	private Bitmap profilePic;
 
     /**
      * Provides access to our local sqlite database.
      */
     private LocalDatabase localDB;
 
 
     /** Used to indicate to other activities that EventDetails is returning a result. */
     final static int REQUEST_CODE_EVENT_DETAILS = 105;
 
     /** Keeps track of whether we have made any modifications to the event. */
     private Boolean eventUpdated = false;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event_details);
 
 		// The home button takes the user back to the map display.
 		ActionBar bar = getSupportActionBar();
 		bar.setDisplayHomeAsUpEnabled(true);
 
 		// Avoid reloading data we can save between configurations.
 		if (savedInstanceState != null) {
 			this.event = savedInstanceState.getParcelable("event");
 			this.userLocation = savedInstanceState.getParcelable("userLocation");
 			this.attended = savedInstanceState.getBoolean("attended");
 			this.eventUpdated = savedInstanceState.getBoolean("eventUpdated");
 			this.profilePic = savedInstanceState.getParcelable("profilePic");
 			this.updateEventDetails();
 			return;
 		}
 
 		// Receive Event information to display via Intent.
 		Intent intent = getIntent();
 		this.event = intent.getParcelableExtra("event");
 		this.userLocation = intent.getParcelableExtra("userLocation");
 
 		if (localDB == null)
     		localDB = new LocalDatabase(this);
 
 		// We might still have a secret_id locally.
 		if (!this.event.isOurs()) {
 			String secretId = localDB.getEventSecretId(this.event);
 			if (secretId != null && !secretId.equals(""))
 				this.event.setSecretId(secretId);
 		}
 
 		// Learn whether we have already attended this event.
 		if (attended == null)
 		    attended = localDB.getAttendanceStatus(event.getId());
 
 		this.updateEventDetails();
 	}
 
 	@Override
     protected void onPause() {
     	super.onPause();
 
     	// Close our database helper if necessary.
     	if (localDB != null)
             localDB.close();
     }
 
 	public void onBackPressed() {
 		// Ensure that a proper result is passed to the waiting activity.
 		// If we have modified the event, the listener should be notified.
 		setResult(this.eventUpdated ? RESULT_OK : RESULT_CANCELED);
 	    super.onBackPressed();
 	}
 
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 
 		// Save member variables.
 		savedInstanceState.putParcelable("event", this.event);
 		savedInstanceState.putParcelable("userLocation", this.userLocation);
 		savedInstanceState.putBoolean("attended", this.attended);
 		savedInstanceState.putBoolean("eventUpdated", this.eventUpdated);
 		savedInstanceState.putParcelable("profilePic", this.profilePic);
 	}
 	/**
 	 * Updates the UI with the latest copy of the Event we have.
 	 */
 	private void updateEventDetails() {
 		// Update the actionbar border color.
 		findViewById(R.id.event_details_colorbar).setBackgroundColor(this.event.getType().color());
 
 		// Display the Event title and description.
 	    ((TextView)findViewById(R.id.event_details_title)).setText(this.event.getTitle());
 	    TextView descBox = (TextView)findViewById(R.id.event_details_description);
 	    if (this.event.getDescription().length() == 0) {
 	    	descBox.setText(R.string.event_description_empty);
 	    	descBox.setTypeface(null, Typeface.ITALIC);
 	    }
 	    else {
 	    	descBox.setText(this.event.getDescription());
 	    	descBox.setTypeface(null, Typeface.NORMAL);
 	    }
 
 	    // Update user information.
 	    if (this.event.getUserType().getValue() != 0) {
 	    	if (this.event.getUserName().trim().length() == 0) {
 	    		Log.e("EventDetails.updateEventDetails", "Invalid user name.");
 	    	    findViewById(R.id.event_details_userinfo).setVisibility(View.GONE);
 	    	} else {
 	    	    ((TextView)findViewById(R.id.event_details_user_name)).setText(this.event.getUserName());
 	    	    findViewById(R.id.event_details_userinfo).setVisibility(View.VISIBLE);
 	    	    findViewById(R.id.event_details_anonymous).setVisibility(View.GONE);
 	    	}
 	    }
 	    else {
 	    	findViewById(R.id.event_details_userinfo).setVisibility(View.GONE);
 	    	findViewById(R.id.event_details_anonymous).setVisibility(View.VISIBLE);
 	    }
 
 	    // Set profile picture, or initiate its download.
 	    if (this.profilePic != null) {
 	    	((LinearLayout)findViewById(R.id.event_details_userinfo)).setVisibility(View.VISIBLE);
 	    	((ImageView)findViewById(R.id.event_details_userpicture)).setImageBitmap(profilePic);
 	    } else if (this.event.getUserPicture().trim().length() != 0) {
 	    	new loadUserPicture().execute(this.event.getUserPicture());
 	    }
 	    
 	    // Inform how many users have attended the event using our app.)
 	    this.updateAttendingText(this.event.getAttendance());
 	    
 	    // Display different date strings based on the time of the event.
 	    Calendar today = Calendar.getInstance();
 	    Calendar startCalendar = Calendar.getInstance();
 	    startCalendar.setTime(this.event.getStartDate());
 	    Calendar endCalendar = Calendar.getInstance();
 	    endCalendar.setTime(this.event.getEndDate());
 
 	    String dateString = "";
 	    if (endCalendar.before(today)) {
 	    	dateString += "This event has ended.";
 	    	((TextView)findViewById(R.id.event_details_date_description)).setText(dateString);
 	    }
 	    else {
 		    if (startCalendar.after(today)) {
 		    	if (startCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
 		    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + " Today  ";
 		    	else
 		    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + " Tomorrow  ";
 		    }
 		    else
 		    	dateString += "Now  ";
 	
 		    if (endCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
 	    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + " Today";
 	    	else
 	    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + " Tomorrow";
 	    }
 	    
 	    ((TextView)findViewById(R.id.event_details_date_description)).setText(dateString);
 	}
 
 	/**
 	 * Separate method for updating the attending count. Allows us to set the
 	 * number without mutating the private event object.
 	 * @param attendingCount The number of users attending.
 	 */
 	private void updateAttendingText(int attendingCount) {
 		// Invalidate the options menu to ensure the value of attending is applied to the icon.
 		if (!ActivityCompat.invalidateOptionsMenu(this))
 			this.onCreateOptionsMenu(_menu);
 
 		// Change the string and image based on whether we are attending.
 		TextView attendingTextBox = ((TextView)findViewById(R.id.event_details_attendance));
 		String attending;
 		Drawable icon;
 		if (this.attended) {
 			// Prevent the count from going negative.
 			int count = attendingCount - 1 <= 0 ? 0 : attendingCount - 1;
 
 			attending = getResources().getQuantityString(R.plurals.users_you_attending, count, count);
 			icon = getResources().getDrawable(R.drawable.people);
 		}
 		else {
 			attending = getResources().getQuantityString(R.plurals.users_attending, attendingCount, attendingCount);
 			icon = getResources().getDrawable(R.drawable.person);
 		}
 		attendingTextBox.setText(attending);
 		attendingTextBox.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
 	}
 
 	private Menu _menu;
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (menu == null)
 			menu = new MenuBuilder(getApplicationContext());
         menu.clear();
 		getMenuInflater().inflate(R.menu.activity_event_details, menu);
 
 		// Change the attend actionbar icon depending on whether we have attended or not.
 		menu.findItem(R.id.menu_attend_event).setIcon(!this.attended ? R.drawable.attend : R.drawable.unattend);
 
 		// Change various visibilities depending on event status and ownership.
 		boolean weHaveOwnership = this.event.isOurs();
 		boolean eventHasEnded = this.event.getEndDate().before(new Date());
 		menu.findItem(R.id.menu_update_event).setVisible(weHaveOwnership && !eventHasEnded);
 		menu.findItem(R.id.menu_delete_event).setVisible(weHaveOwnership && !eventHasEnded);
 		menu.findItem(R.id.menu_report_event).setVisible((!eventHasEnded && !weHaveOwnership) || !weHaveOwnership);
 		menu.findItem(R.id.menu_attend_event).setVisible(!eventHasEnded);
 		menu.findItem(R.id.menu_get_directions).setVisible(!eventHasEnded);
 
 		// Prevent any issues with directions if we have no location.
 		if (this.userLocation == null)
 			menu.findItem(R.id.menu_get_directions).setVisible(false);
 
 		// Keep a reference to the menu for later uses (refresh indicator change).
         this._menu = menu;
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case android.R.id.home:
 	    	    // Returning OK indicates the event has been modified, CANCELED indicates no change.
 	        	// The activity requesting this result can choose to update information as needed.
 	        	setResult(this.eventUpdated ? RESULT_OK : RESULT_CANCELED);
 	            finish();
 	            return true;
 	        case R.id.menu_refresh_event:
 	        	// Grab a new copy of the event.
 	        	if (APICalls.isOnline(this))
 	        	    new refreshEventDetailsAPICaller().execute(this.event.getId());
 	        	else
 	        		APICalls.displayConnectivityMessage(this);
 	        	return true;
 	        case R.id.menu_get_directions:
 	        	// Prevent null exceptions if we did not receive a location.
 	        	if (this.userLocation == null)
 	        		return true;
 
 	        	// Prepare maps url query url parameters.
             	String startCoords = ((Double)this.userLocation.latitude).toString() + "," + ((Double)this.userLocation.longitude).toString();
             	String endCoords = ((Double)this.event.getLocation().latitude).toString() + "," + ((Double)this.event.getLocation().longitude).toString();
             	String url = "http://maps.google.com/maps?saddr=" + startCoords + "&daddr=" + endCoords;
             	Log.d("EventDetails.onOptionsItemSelected", "Get directions, " + url);
 
                 // Pass an intent to an activity that can provide directions.
             	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                 startActivity(intent);
                 return true;
 	        case R.id.menu_report_event:
 	        	if (!APICalls.isOnline(this)) {
 	        		APICalls.displayConnectivityMessage(this);
 	        		return true;
 	        	}
 
 	        	// Show dialog allowing the user to report an event.
 				DialogFragment reportDialog = new ReportDialogFragment();
 				reportDialog.show(getSupportFragmentManager(), "reportDialog");
 	        	return true;
             case R.id.menu_attend_event:
             	if (!APICalls.isOnline(this)) {
 	        		APICalls.displayConnectivityMessage(this);
 	        		return true;
 	        	}
 
                 // Attend or unattend the event.
             	if (!this.attended)
             	    new attendEventAPICaller().execute(this.event.getId());
             	else
             		new unattendEventAPICaller().execute(this.event.getId());
 	        	return true;
             case R.id.menu_update_event:
                 // Open the activity for updating the event.
             	Intent editEvent = new Intent(EventDetails.this, EditEvent.class);
     	    	editEvent.putExtra("event", this.event);
     	    	startActivityForResult(editEvent, EditEvent.REQUEST_CODE_EDIT_EVENT);
     	    	return true;
             case R.id.menu_delete_event:
             	if (!APICalls.isOnline(this)) {
 	        		APICalls.displayConnectivityMessage(this);
 	        		return true;
 	        	}
 
                 // Remove the event from the backend.
             	new deleteEventAPICaller().execute(event);
     	    	return true;
             case R.id.share:
             	connectFacebook();
             	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	   }
 	}
 
 	/**
      *  Checks if the person is signed into facebook and posts to their wall if they are else
      *  makes them connect through a Dialog.
      */
     public void connectFacebook() {
         Session session = Session.getActiveSession();
         final Context context = this;
         if (session == null || session.isClosed()) {
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(R.string.event_share_dialog);
             builder.setPositiveButton(R.string.log_in_to_facebook, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                     	   FacebookAuth facebook = new FacebookAuth();
                     	   facebook.startSession(context);
                     	   dialog.dismiss();
                     	   
                        }
                    });
             builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
             AlertDialog dialog = builder.create();
             dialog.show();
         } else if (session.isOpened()){
         	new shareEventAPICaller().execute(event.getId());
         }
     }
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("EventDetails.onActivityResult", "requestCode: " + requestCode + " resultCode: " + resultCode);
 
 		// TODO: Figure out the conditions that cause this to crash.
 		try {
 		    Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 		} catch (Exception e) {
 			Log.e("EventDetails.onActivityResult", "Facebook session code caused crash.");
 		}
 		if (resultCode == RESULT_CANCELED && requestCode == 64206) {
 			Session session = Session.getActiveSession();
 			session.closeAndClearTokenInformation();
 			Session.setActiveSession(null);
 		}
 		else if (resultCode == RESULT_OK && requestCode == 64206) {
 			new shareEventAPICaller().execute(event.getId());
 		}
 		else if (requestCode == EditEvent.REQUEST_CODE_EDIT_EVENT && resultCode == RESULT_OK) {
 			// The event was edited in some manner by the user.
 			new refreshEventDetailsAPICaller().execute(this.event.getId());
 			this.eventUpdated = true;
 		}
 	}
 
 	/**
 	 * Receives the ReportReason from the report dialog and submits the report.
 	 */
 	public void onReportDialogOKClick(DialogFragment dialog, ReportReason reason) {
 	    if (APICalls.isOnline(this))
             new reportEventAPICaller().execute(this.event.getId(), reason.ordinal());
 	    else
 	    	APICalls.displayConnectivityMessage(this);
 	}
 	
 	/**
 	 * Performs an async call to report an event for a violation.
 	 */
 	private class reportEventAPICaller extends AsyncTask<Integer, Void, String> {
 		@Override
 		protected void onPreExecute() {
 			// TODO: Establish progress UI changes?
 		}
 
 		@Override
 		protected String doInBackground(Integer... values) {
 			// Values contains [0] id, [1] ReportReason
 			return APICalls.reportEvent(values[0], values[1], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// TODO: Remove progress UI.
 
 			// Some sort of error occurred during the request.
 			if (result == null) {
 				(Toast.makeText(getApplicationContext(), "Could not submit report. Please try again!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Our request went through and we have not previously reported the event.
 			if (result.equals("OK")) {
 				// Display a message thanking the user for the report.
 				AlertDialog.Builder builder = new AlertDialog.Builder(EventDetails.this);
                 builder.setMessage("Thank you for submitting a report.");
                 builder.setPositiveButton(R.string.ok, null);
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			}
 			
 			// The user has already submitted a report.
 			else if (result.equals("PREVIOUSLY_REPORTED")) {
 				(Toast.makeText(getApplicationContext(), "You have already submitted a report. We will investigate the event as soon as possible.", Toast.LENGTH_LONG)).show();
 			}
 		}
 	}
 
 	/**
 	 * Performs an async call to submit attendance to an event.
 	 */
 	private class attendEventAPICaller extends AsyncTask<Integer, Void, String> {
 		/** Quick access to the attend button in the actionbar. */
 		MenuItem attendItem;
 
 		@Override
 		protected void onPreExecute() {
 			// Establish progress UI changes.
 			if (_menu != null) {
 				attendItem = _menu.findItem(R.id.menu_attend_event);
 				if (attendItem != null)
 					MenuItemCompat.setActionView(attendItem, R.layout.actionbar_refresh_progress);
 			}
 		}
 
 		@Override
 		protected String doInBackground(Integer... id) {
 			return APICalls.attendEvent(id[0], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// Remove progress UI.
 			if (attendItem != null)
 				MenuItemCompat.setActionView(attendItem, null);
 			attendItem = null;
 
 			// Some sort of error occurred during the request.
 			if (result == null) {
 				(Toast.makeText(getApplicationContext(), "Could not submit attendance request. Please try again!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Whether we have already attended before the request was sent, we for sure have attended now.
 			attended = true;
 			if (localDB == null)
 			    localDB = new LocalDatabase(EventDetails.this);
 			localDB.trackAttendance(event.getId());
 
 			// Our request went through and we have not yet attended previously.
 			if (result.equals("OK")) {
 				// Update the text display to reflect the changed number.
 				updateAttendingText(event.getAttendance() + 1);
 				(Toast.makeText(getApplicationContext(), "Thanks for attending!", Toast.LENGTH_LONG)).show();
 			}
 			// The user has already said they will attend the event.
 			else if (result.equals("PREVIOUSLY_ATTENDED")) {
 				updateAttendingText(event.getAttendance());
 				(Toast.makeText(getApplicationContext(), "You have already indicated you will attend.", Toast.LENGTH_LONG)).show();
 			}
 			//Need to refresh the event to get an accurate attendance count
 			new refreshEventDetailsAPICaller().execute(event.getId());
 		}
 	}
 
 	/**
 	 * Performs an async call to remove attendance to an event.
 	 */
 	private class unattendEventAPICaller extends AsyncTask<Integer, Void, String> {
 		/** Quick access to the attend button in the actionbar. */
 		MenuItem attendItem;
 
 		@Override
 		protected void onPreExecute() {
 			// Establish progress UI changes.
 			if (_menu != null) {
 				attendItem = _menu.findItem(R.id.menu_attend_event);
 				if (attendItem != null)
 					MenuItemCompat.setActionView(attendItem, R.layout.actionbar_refresh_progress);
 			}
 		}
 
 		@Override
 		protected String doInBackground(Integer... id) {
 			return APICalls.unattendEvent(id[0], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// Remove progress UI.
 			if (attendItem != null)
 				MenuItemCompat.setActionView(attendItem, null);
 			attendItem = null;
 
 			// Some sort of error occurred during the request.
 			if (result == null) {
 				(Toast.makeText(getApplicationContext(), "Could not unattend event!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Whether we have actually unattended or not, we must now allow the user to attend again.
 			attended = false;
 			if (localDB == null)
 			    localDB = new LocalDatabase(EventDetails.this);
 			localDB.removeAttendance(event.getId());
 			updateAttendingText(event.getAttendance());
 
 			// Our request went through and we have not yet attended previously.
 			if (result.equals("OK")) {
 				// Update the text display to reflect the changed number.
 				(Toast.makeText(getApplicationContext(), "Sorry to see you go!", Toast.LENGTH_LONG)).show();
 			}
 			// The user has not yet attended this event!
 			else if (result.equals("NO_ATTENDANCE_RECORD")) {
 				(Toast.makeText(getApplicationContext(), "You have not attended this event yet.", Toast.LENGTH_LONG)).show();
 			}
 			//Need to refresh the event to get an accurate attendance count
 			new refreshEventDetailsAPICaller().execute(event.getId());
 		}
 	}
 
 	private class deleteEventAPICaller extends AsyncTask<Event, Void, Boolean> {
 		/**
 	     * Informs the user that the event is being deleted.
 	     */
 	    ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			// Set up progress indication.
 			dialog = ProgressDialog.show(EventDetails.this, "Deleting...", "");
 		}
 
 		@Override
 		protected Boolean doInBackground(Event... event) {
 			return APICalls.deleteEvent(event[0], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			// Close any progress indication.
 			dialog.dismiss();
 			dialog = null;
 			if (result == false) {
 				(Toast.makeText(getApplicationContext(), "The event could not be deleted!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Update the local cache to recognize deletion.
 			if (localDB == null)
 	            localDB = new LocalDatabase(getApplicationContext());
 			boolean deleted = localDB.deleteEventFromCache(event);
 			if (!deleted)
 				Log.e("deleteEventAPICaller.onPostExecute", "Could not delete event from local cache");
 
 			// Exit the details page as the event no longer exists.
 			Toast.makeText(getApplicationContext(), "The event has been deleted.", Toast.LENGTH_LONG).show();
 			EventDetails.this.setResult(RESULT_OK);
 			EventDetails.this.finish();
 		}
 	}
 
 	/**
 	 * Performs an asynchronous API call receive any updates of the event we are viewing.
 	 */
 	private class refreshEventDetailsAPICaller extends AsyncTask<Integer, Void, Event> {
 		/**
 		 * Quick access to the refresh button in the actionbar.
 		 */
 		MenuItem refreshItem;
 
 		protected void onPreExecute() {
 			// Establish progress UI changes.
 			if (_menu != null) {
 		        refreshItem = _menu.findItem(R.id.menu_refresh_event);
 		        if (refreshItem != null)
 			        MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_refresh_progress);
 			}
 		}
 
 		protected Event doInBackground(Integer... id) {
 			return APICalls.getEvent(id[0]);
 		}
 
 		protected void onPostExecute(Event result) {
 			// Remove progress UI.
 			if (refreshItem != null)
 			    MenuItemCompat.setActionView(refreshItem, null);
 			refreshItem = null;
 
 			// If the event can't be found, no UI refresh should occur.
 			if (result == null) {
 				Toast.makeText(getApplicationContext(),
                                "The event could not be found or no longer exists!",
                                Toast.LENGTH_LONG).show();
 				return;
 			}
 
 			event = result;
 
 			// Invalidate the options menu to account for any needed visibility changes.
 			if (!ActivityCompat.invalidateOptionsMenu(EventDetails.this))
 				EventDetails.this.onCreateOptionsMenu(_menu);
 
 			updateEventDetails();
 		}
 	}
 	
 	private class shareEventAPICaller extends AsyncTask<Integer, Void, Boolean> {
 		String token;
 		ProgressDialog shareDialog;
 		Session session = Session.getActiveSession();
 		protected void onPreExecute() {
     		if (session != null) {
 			    token = session.getAccessToken();
 			}
 			shareDialog = ProgressDialog.show(EventDetails.this, "Sharing...", "");
 		}
 
 		protected Boolean doInBackground(Integer... id) {
 			if (token != null){
 				Log.d("Event Details", "Access Token: " + token);
 			    return APICalls.shareEvent(id[0], token);
 			}
 			return false;
 		}
 
 		protected void onPostExecute(Boolean result) {
             shareDialog.dismiss();
 			if (result == false) {
 				Toast.makeText(getApplicationContext(),
                                "The event could not be found or no longer exists!",
                                Toast.LENGTH_LONG).show();
 
 				return;
 			}
 			Toast.makeText(getApplicationContext(), "The Event has been shared.", Toast.LENGTH_LONG).show();
 			return;
 		}
 	}
 
 	/**
 	 * Downloads the user's profile picture asynchronously.
 	 */
 	private class loadUserPicture extends AsyncTask<String, Void, Bitmap> {
 		protected void onPreExecute() {
 			// Establish progress UI changes.
 		}
 
 		protected Bitmap doInBackground(String... url) {
 			HttpGet httpRequest = null;
 
 			try {
 			    httpRequest = new HttpGet(url[0]);
 			    HttpClient httpclient = new DefaultHttpClient();
                 HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
                 HttpEntity entity = response.getEntity();
                 BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                 InputStream instream = bufHttpEntity.getContent();
                 return BitmapFactory.decodeStream(instream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
             return null;
 		}
 
 		protected void onPostExecute(Bitmap result) {
             if (result != null) {
             	profilePic = getRoundedCornerBitmap(result, (int)(result.getHeight() / 10));
             	((ImageView)findViewById(R.id.event_details_userpicture)).setImageBitmap(profilePic);
             }
 		}
 
 		/** 
 		 * Rounds the edges of a bitmap.
 		 * @author http://ruibm.com/?p=184
 		 */
 		private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
 	        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
 	        Canvas canvas = new Canvas(output);
 
 	        final int color = 0xff424242;
 	        final Paint paint = new Paint();
 	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 	        final RectF rectF = new RectF(rect);
 	        final float roundPx = pixels;
 
 	        paint.setAntiAlias(true);
 	        canvas.drawARGB(0, 0, 0, 0);
 	        paint.setColor(color);
 	        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
 
 	        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 	        canvas.drawBitmap(bitmap, rect, rect, paint);
 
 	        return output;
 	    }
 	}  
 }
