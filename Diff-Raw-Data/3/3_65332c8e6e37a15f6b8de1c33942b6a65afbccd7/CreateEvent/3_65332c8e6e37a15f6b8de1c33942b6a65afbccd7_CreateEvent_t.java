 package com.appchallenge.eventspark;
 
 import android.accounts.AccountManager;
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ActivityCompat;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import java.text.DateFormat;
 
 import java.util.Calendar;
 import java.util.Date;
 import com.appchallenge.eventspark.Event.Type;
 import com.appchallenge.eventspark.Event.UserType;
 
 import com.facebook.Session;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 /**
  * Wizard activity for creating new events.
  */
 public class CreateEvent extends ActionBarActivity implements CreateEventInterface {
     /**
      * A pager widget that controls animating between fragments.
      */
     private ViewPager mPager;
 
     /**
      * The pager adapter, which provides the pages to the view pager widget.
      */
     private PagerAdapter mPagerAdapter;
 
     /**
      * Provides access to our local sqlite database.
      */
     private LocalDatabase localDB;
 
     /** Used to indicate to EventViewer that the wizard has been submitted. */
     final static int REQUEST_CODE_CREATE_EVENT = 101;
     
     /**
      * The local event we are creating.
      */
     private Event newEvent;
 
     // Method implementations for CreateEventInterface.
 	public void setTitle(String title) {
 		newEvent.setTitle(title);
 	}
 	public String getEventTitle() {
 		return newEvent.getTitle();
 	}
 
 	public void setType(Type type) {
 		newEvent.setType(type);
 	}
 	public Type getType() {
 		return newEvent.getType();
 	}
 
 	public void setDescription(String description) {
 		newEvent.setDescription(description);
 	}
 	public String getDescription() {
 		return newEvent.getDescription();
 	}
 
 	public void setStartDate(Date date) {
 		newEvent.setStartDate(date);
 	}
 	public Date getStartDate() {
 		return newEvent.getStartDate();
 	}
 
 	public void setEndDate(Date date) {
 		newEvent.setEndDate(date);
 	}
 	public Date getEndDate() {
 		return newEvent.getEndDate();
 	}
 
 	public void setLocation(LatLng location) {
 		newEvent.setLocation(location);
 	}
 	public LatLng getLocation() {
 		return newEvent.getLocation();
 	}
 	
 	public void setUserType(UserType userType) {
 		// Show a cautionary message about public information visibility.
 		findViewById(R.id.connect_message).setVisibility(userType == UserType.ANONYMOUS ? View.GONE : View.VISIBLE);
 
 		// If the type is the same, we are changing configurations and should already have handled token grabbing.
 		if (newEvent.userType == userType)
 			return;
 		newEvent.userType = userType;
 
 		// Receive a token if necessary
 		if (userType == UserType.ANONYMOUS) {
 			this.token = null;
 		}
 		else if (userType == UserType.GPLUS) {
 			startActivityForResult(GoogleAuth.getAccountPickerIntent(), GoogleAuth.REQUEST_CODE_GOOGLE_PLUS_ACCOUNTNAME);
 		}
 		else if (userType == UserType.FACEBOOK) {
 			Session session = Session.getActiveSession();
 			if (session == null || session.isClosed()) {
 			    FacebookAuth facebook = new FacebookAuth();
 			    facebook.startSession(this);
 			} else {
 			    this.setToken(session.getAccessToken());	
 			}
 		}	
 	}	
 	
 	public UserType getUserType() {
 		return newEvent.getUserType();
 	}
 
 	private String token;
 	public void setToken(String token) {
		if (token != null)
		    Log.d("CreateEvent.setToken", "Received token: " + token.substring(0, token.length() / 10) + "...");
 		this.token = token;
 	}
 
 	public MarkerOptions getMarker() {
 		return newEvent.toMarker(true);
 	}
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_create_event);
         
         // The home button takes the user back to the map display.
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         
         // Keeps keyboard from popping up unless invoked
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
         Intent receivedIntent = getIntent();
 
         // Set the initial date values for the event.
         if (savedInstanceState != null)
         	this.newEvent = savedInstanceState.getParcelable("newEvent");
         else {
         	// See if an event was passed in.
         	if (receivedIntent.hasExtra("event")) {
         		this.newEvent = new Event((Event)receivedIntent.getParcelableExtra("event"));
 
         		// Establish proper relative time intervals.
         		Calendar today = Calendar.getInstance();
         		Calendar c = Calendar.getInstance();
         		c.setTime(this.newEvent.getStartDate());
         		c.set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR));
         		this.newEvent.setStartDate(c.getTime());
         		c.setTime(this.newEvent.getEndDate());
         		c.set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR));
         		this.newEvent.setEndDate(c.getTime());
         		
         	}
         	else
         	    this.newEvent = new Event();
         }
 
         if (this.getStartDate() == null && this.getEndDate() == null) {
         	this.setStartDate(new Date());
         	Calendar c = Calendar.getInstance();
            	c.add(Calendar.HOUR_OF_DAY, 3);
             this.setEndDate(c.getTime());
         }
 
         // Set the map location to use the location passed in.
         if (this.getLocation() == null)
             this.setLocation((LatLng)receivedIntent.getParcelableExtra("location"));
 
         // Instantiate a ViewPager and a PagerAdapter.
         mPager = (ViewPager)findViewById(R.id.pager);
         mPagerAdapter = new CreateEventPagerAdapter(getSupportFragmentManager());
         mPager.setAdapter(mPagerAdapter);
         mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When changing pages, reset the action bar actions since they are dependent
                 // on which page is currently active. An alternative approach is to have each
                 // fragment expose actions itself (rather than the activity exposing actions),
                 // but for simplicity, the activity provides the actions.
                 if (!ActivityCompat.invalidateOptionsMenu(CreateEvent.this))
                 	CreateEvent.this.onCreateOptionsMenu(CreateEvent.this._menu);
             }
         });
     }
 
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	savedInstanceState.putParcelable("newEvent", this.newEvent);
     	super.onSaveInstanceState(savedInstanceState);
     }
 
     private Menu _menu;
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.clear();
         getMenuInflater().inflate(R.menu.activity_create_event, menu);
 
         menu.findItem(R.id.action_back).setEnabled(mPager.getCurrentItem() > 0);
 
         // Add either a "next" or "submit" button to the action bar.
         int action = R.string.action_next;
         int id = R.id.action_next;
         if (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
         	action = R.string.action_finish;
         	id = R.id.action_submit;
         }
 
         MenuItem item = menu.add(Menu.NONE, id, Menu.NONE, action);
         MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
         
         // Keep a reference to the menu for manual calls to onCreateOptionsMenu.
         this._menu = menu;
         
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {    
         int duration = Toast.LENGTH_SHORT;
 
         // Close the keyboard after each page change.
         try {
         	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
         } catch (Exception e) {
         	Log.w("CreateEvent.onOptionsItemSelected", "hideSoftInputFromWindow threw an error.");
         }
 
         if (item.getItemId() == android.R.id.home) {
 			// Handle navigating back to the EventViewer on Home press.
         	Intent viewerIntent = new Intent(this, EventViewer.class);
             if (NavUtils.shouldUpRecreateTask(this, viewerIntent))
                 NavUtils.navigateUpTo(this, viewerIntent);
             finish();
             return true;
 		} else if (item.getItemId() == R.id.action_back) {
 			// Go to the previous step in the wizard. If there is no previous step,
 			// setCurrentItem will do nothing.
 			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
 			return true;
 		} else if (item.getItemId() == R.id.action_next) {
 			// Advance to the next step in the wizard after performing error checking.
 			if (mPager.getCurrentItem() == 0 && this.getEventTitle().trim() == "") {
 				Toast.makeText(this, "Please enter a title for the event!", duration).show();
 				return true;
 			}
 
 			if (mPager.getCurrentItem() == 1) {
 				if (getStartDate().after(getEndDate()) || getEndDate().before(new Date())) {
 					Toast.makeText(this, "Please enter a valid timespan!", duration).show();
 					return true;
 				}
 			}
 
 			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
 			return true;
 		} else if (item.getItemId() == R.id.action_submit) {
 			// Prevent being offline from losing entered data.
 			if (!APICalls.isOnline(this)) {
 				APICalls.displayConnectivityMessage(this);
 				return true;
 			}
 
 			// Perform an asynchronous API call to create the new event.
 			createEventAPICaller apiCall = new createEventAPICaller();
 			apiCall.execute(this.newEvent);
 			return true;
 		}
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBackPressed() {
         // Have the back button go back one page instead of exit.
         if (mPager.getCurrentItem() != 0) {
             mPager.setCurrentItem(mPager.getCurrentItem() - 1);
             return;
         }
         super.onBackPressed(); 
     }
 
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
     	Log.d("CreateEvent.onActivityResult", "Received result intent. requestCode: " + requestCode + " resultCode: " + resultCode);
 
     	super.onActivityResult(requestCode, resultCode, data);
 
 		// TODO: Figure out the conditions that cause this to crash.
 		try {
 		    Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 		} catch (Exception e) {
 			Log.e("EventDetails.onActivityResult", "Facebook session code caused crash.");
 		}
     	
         if (requestCode == GoogleAuth.REQUEST_CODE_GOOGLE_PLUS_ACCOUNTNAME && resultCode == RESULT_OK) {
         	// Determine the account name and request a token.
             String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
             Log.d("CreateEvent.onActivityResult", "Got account name: " + accountName);
 
             // Request a token from Google+.
             GoogleAuth gAuth = new GoogleAuth(this);
             gAuth.getToken(accountName);
         }
         else if (requestCode == GoogleAuth.REQUEST_CODE_GOOGLE_PLUS_TOKEN && resultCode == RESULT_OK) {
         	Log.d("CreateEvent.onActivityResult", "Google+ permissions should have been given, so time to retry.");
         	String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
             Log.d("CreateEvent.onActivityResult", "Got account name: " + accountName);
             GoogleAuth gAuth = new GoogleAuth(this);
             gAuth.getToken(accountName);
         }
     	else if ((requestCode == GoogleAuth.REQUEST_CODE_GOOGLE_PLUS_ACCOUNTNAME && resultCode == RESULT_CANCELED) ||
     			 (requestCode == GoogleAuth.REQUEST_CODE_GOOGLE_PLUS_TOKEN && resultCode == RESULT_CANCELED)) {
     		// Revert the UserType spinner selection to Anonymous as the user cancelled selection.
     		if (mPager.getCurrentItem() != 0) {
     			Log.e("CreateEvent.onActivityResult", "Child fragment is not the first page.");
     		    return;
     		}
 
     		Spinner userTypeSpinner = (Spinner)findViewById(R.id.event_usertype_spinner);
     		if (userTypeSpinner != null)
     		    userTypeSpinner.setSelection(0);
     		else
     			Log.e("CreateEvent.onActivityResult", "UserType spinner could not be referenced.");
     	}
     	else if (resultCode == 0 && requestCode == 64206) {
     		Session session = Session.getActiveSession();
     		session.closeAndClearTokenInformation();
     	} else if (resultCode == -1 && requestCode == 64206) {
     		Session session = Session.getActiveSession();
     		this.setToken(session.getAccessToken());
     		//this.setToken(FacebookAuth.getToken());
     		Log.d("CreateEvent.onActivityResult", "Successfully Authenticated");
     	}
     }
 
     /**
      * Shows the time picker to allow changing the Event time.
      * @param v
      */
     public void showEventTimeDialog(View v) {
     	DialogFragment timePicker;
     	switch (v.getId()) {
 	    	case R.id.event_start_button:
 	            timePicker = new StartTimePicker();
 	            timePicker.show(getSupportFragmentManager(), "startTimePicker");
 	            break;
 	    	case R.id.event_end_button:
 	    		timePicker = new EndTimePicker();
 	    		timePicker.show(getSupportFragmentManager(), "endTimePicker");
 	            break;
     	}
     }
 
 	@SuppressLint("ValidFragment")
 	public class StartTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current time as the default values for the picker
 			Date startTime = ((CreateEventInterface)getActivity()).getStartDate();
 			Calendar c = Calendar.getInstance();
 			c.setTime(startTime);
 			
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Create a new Date object with the updated time.
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(((CreateEventInterface)getActivity()).getStartDate());
 			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			cal.set(Calendar.MINUTE, minute);
 
 			// Send this updated date back to the wizard activity.
 			Date newDate = cal.getTime();
 			((CreateEventInterface)getActivity()).setStartDate(newDate);
 
 			// Update the display text.
 			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
 		    ((Button) findViewById(R.id.event_start_button)).setText(timeString);
 		}
     }
 
 	@SuppressLint("ValidFragment")
 	public class EndTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			Date endTime = ((CreateEventInterface) getActivity()).getEndDate();
 			Calendar c = Calendar.getInstance();
 			c.setTime(endTime);
 
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Create a new Date object with the updated time.
 			Calendar c = Calendar.getInstance();
 			c.setTime(((CreateEventInterface)getActivity()).getEndDate());
 			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			c.set(Calendar.MINUTE, minute);
 
 			// Send this updated date back to the wizard activity.
 			Date newDate = c.getTime();
 			((CreateEventInterface)getActivity()).setEndDate(newDate);
 
 			// Update the display text.
 			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
 		    ((Button) findViewById(R.id.event_end_button)).setText(timeString);
 		}
     }
     
 	/**
      * A pager adapter that represents the wizard pages sequentially.
      */
     public class CreateEventPagerAdapter extends FragmentStatePagerAdapter {
     	// Instances of each wizard page, in order.
     	public Fragment[] wizardPages = new Fragment[] {
             new CreateEventPage1EventName(),
             new CreateEventPage2EventTime(),
             new CreateEventPage3EventLocation()
     	};
     	
         public CreateEventPagerAdapter(FragmentManager fragmentManager) {
             super(fragmentManager);
         }
 
         @Override
         public Fragment getItem(int position) {
             return wizardPages[position];
         }
 
         @Override
         public int getCount() {
             return wizardPages.length;
         }
     }
 
     /**
 	 * Performs an asynchronous API call create a new event.
 	 */
 	private class createEventAPICaller extends AsyncTask<Event, Void, Event> {
 		/**
 	     * Informs the user that the event is being created.
 	     */
 	    ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			// Set up progress indication.
 			dialog = ProgressDialog.show(CreateEvent.this, "Creating...", "");
 		}
 
 		@Override
 		protected Event doInBackground(Event... event) {
 			return APICalls.createEvent(event[0], Identity.getUserId(getApplicationContext()), token);
 		}
 
 		@Override
 		protected void onPostExecute(Event result) {
 			// Close the wizard and any progress indication.
 			dialog.dismiss();
 			dialog = null;
 			if (result == null) {
 				(Toast.makeText(getApplicationContext(), "Could not create event!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Save this event's secret_id to our local storage.
 	    	if (localDB == null)
 	    		localDB = new LocalDatabase(CreateEvent.this);
 	    	localDB.takeOwnership(result);
 			
 			// Pass the new event to the event viewer.
 			Intent intent = new Intent(CreateEvent.this, EventViewer.class);
 			intent.putExtra("event", result);
 			CreateEvent.this.setResult(RESULT_OK, intent);
 			CreateEvent.this.finish();
 		}
 	}
 }
