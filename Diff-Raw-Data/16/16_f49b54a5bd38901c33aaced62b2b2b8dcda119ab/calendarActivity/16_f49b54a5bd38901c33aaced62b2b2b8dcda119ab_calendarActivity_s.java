 /*
 Copyright (c) 2011 Sarah Cathey, Michelle Carter, Aren Edlund-Jermain
 This project is protected under the Apache license. 
 Please see COPYING file in the distribution for license terms.
 */
 
 package osse.android.moldhold;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.api.client.extensions.android2.AndroidHttp;
 import com.google.api.client.googleapis.GoogleHeaders;
 import com.google.api.client.googleapis.GoogleUrl;
 import com.google.api.client.googleapis.MethodOverride;
 import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
 import com.google.api.client.http.HttpExecuteInterceptor;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
 import com.google.api.client.sample.calendar.android.model.CalendarClient;
 import com.google.api.client.sample.calendar.android.model.CalendarEntry;
 import com.google.api.client.sample.calendar.android.model.CalendarFeed;
 import com.google.api.client.sample.calendar.android.model.CalendarUrl;
 import com.google.api.client.sample.calendar.android.model.EventEntry;
 import com.google.api.client.sample.calendar.android.model.Reminder;
 import com.google.api.client.sample.calendar.android.model.When;
 import com.google.api.client.util.DateTime;
 import com.google.common.collect.Lists;
 
  
 
 // This activity is started by mainActivity (after DataBaseActivity has
 // completed).
 //
 //This activity verifies the existence of the MoldHold calendar at the
 //beginning of each invocation. If the MoldHold calendar is not found, then
 //one will be created.
 public class calendarActivity extends Activity {
 	private String		productName = "";
 	private Long		shelfLife;		// in days
     private int 		cYear;			// current year
     private int 		cMonth;			// current month
     private int 		cDay;			// current day
     private String		calendarID;
 
 
 	private GoogleAccountManager 		accountManager;
 	private String 						gsessionid;
 	private String 						authToken;
 	private String 						accountName;
     private SharedPreferences			settings;
 	CalendarClient 						client;
 	private final List<CalendarEntry> 	calendars = Lists.newArrayList();
 	private String[] 					calendarNames;
 	private final HttpTransport 		transport = 
 			AndroidHttp.newCompatibleTransport();
 	
     private static final String 	PREF = "MoldHoldPrefs";   // prefernces name
 	private static final String		TAG = "calendarActivity"; // for Android logging
 	private static final String		CALENDAR_NAME = "MoldHold Expiration Dates";
 	private static final String 	AUTH_TOKEN_TYPE = "cl"; // for calendar
 	private static final int		MIN_BEFORE = 15; 		// for reminder
 	private static final int		DAYS_PRIOR = 3;			// for reminder
 	
 	// intent ids
 	private static final int 		REQUEST_AUTHENTICATE = 0;
 	private static final int 		DATE_DIALOG_ID = 1;
 	
 	
 	// UI
 	private TextView 				dateDisplay;
 	private Button					btnOk;
 	private Button					btnPickDate;
 
 	
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available. 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		accountManager = new GoogleAccountManager(this);
 		settings = getSharedPreferences(PREF, MODE_PRIVATE);
 		
 		// Try to get from preferences (will be empty at initial run)
 		authToken = settings.getString("AUTH_TOKEN", null);
 		gsessionid = settings.getString("GSESSION_ID", null);
 		accountName = settings.getString("ACCOUNT_NAME", null);
 		calendarID = settings.getString("CALENDAR_ID", null); 
 		
 		final MethodOverride override = new MethodOverride(); // needed for PATCH
 		client = new CalendarClient(transport.createRequestFactory(
 				new HttpRequestInitializer() {
 	     		
 			public void initialize(HttpRequest request) {	
 				Log.d(TAG, "in new CalendarClient - initialize()");
 				GoogleHeaders headers = new GoogleHeaders();
 				headers.setApplicationName("MoldHold/1.0");
 				headers.gdataVersion = "2";
 				request.headers = headers;
 				client.initializeParser(request);
 				request.interceptor = new HttpExecuteInterceptor() {
 	
 					public void intercept(HttpRequest request) 
 						throws IOException {
 						GoogleHeaders headers = (GoogleHeaders) request.headers;
 						headers.setGoogleLogin(authToken);
 						request.url.set("gsessionid", gsessionid);
 						override.intercept(request);
 					}
 				};
 				// redefining unsuccessfulResponseHandler
 				request.unsuccessfulResponseHandler = 
 					new HttpUnsuccessfulResponseHandler() {
 	
 					public boolean handleResponse(HttpRequest request, 
 							HttpResponse response, boolean retrySupported) {
 						
 						Log.d(TAG, "in new CalendarClient - handleResponse()");
 
 						switch (response.statusCode) {
 							case 302:	// found
 								GoogleUrl url = 
 									new GoogleUrl(response.headers.location);
 								gsessionid = (String) url.getFirst("gsessionid");
 								SharedPreferences.Editor editor = settings.edit();
 								editor.putString("GSESSION_ID", gsessionid);
 								editor.commit();
 								return true;
 							case 401:	// unauthorized -> bad authToken 
 								Log.d(TAG, "401 invalid token");
 
 								accountManager.invalidateAuthToken(authToken);
 								authToken = null;
 								SharedPreferences.Editor editor2 = settings.edit();
 								editor2.remove("AUTH_TOKEN");
 								editor2.commit();
 								return false;
 						} 
 						return false;	// any other status code
 					}
 				};
 			} // end public void initialize
 		}));  // end new calendarClient
 
 		Log.d(TAG, "calling gotAccount in onCreate()");
 		gotAccount();
 		Log.d(TAG, "calling executeRefreshCalendars in onCreate()");
 		executeRefreshCalendars();
 		
 		// trying to make main UI thread wait for new authToken to be
 		// obtained before continuing on...
 	/*	if (authToken == null) {
 			setContentView(R.layout.status);	
 			while(authToken == null) {
 				//Log.d(TAG, "main thread yielding...");
 				//Thread.yield();	// OOPS, can't do this...
 				;
 			}
 		}*/
 		
 		Log.d(TAG, "checking if calendar exists...");
 		boolean found = checkCalendarExists();
 		if (found == false) {
 			Log.d(TAG, "creating new calendar...");
 			createNewCalendar();  // saves calendarID to preferences
 		}
 		Log.d(TAG, "calendar was found, now checking cal id...");
 			
 		if (calendarID == null) {
 			Log.d(TAG, "Shit, calendarID is null... this should never happend...");
 			//finish(); 		// handle...
 		}
 		
 		
 		// Must setContentView before calling findViewById for buttons!!!
 		setContentView(R.layout.calendar);
 		
 		dateDisplay = (TextView) findViewById(R.id.dateDisplay);
 		btnOk = (Button) findViewById(R.id.btnOk);
 		btnOk.setOnClickListener(new OnClickListener() {
 			
 			@Override
             public void onClick(View v) {
 				// add event using date
 				EventEntry event = newEvent();
 				addNewEvent(event);
 				Intent intent = new Intent();
             	setResult(Activity.RESULT_OK, intent);
                 finish();	// return to mainActivity
             }
         }); 
 		
 		btnPickDate = (Button) findViewById(R.id.btnPickDate);
 		btnPickDate.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		}); 
 		
 		// get extras from mainActivity
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			productName = extras.getString("DESCRIPTION");
 			shelfLife = extras.getLong("SHELF_LIFE");
 			
 		}
 		setAlarmDate();
 		dateDisplay.setText(new StringBuilder()
 				.append("Your ").append(productName)
 				.append(" has a shelf life of ").append(shelfLife)
 				.append(" days. An alarm notifying you of its expiration ")
 				.append("will be set for:\n")
         		.append(cMonth + 1).append("-")	// Month is 0 based so add 1
         		.append(cDay).append("-")
         		.append(cYear).append(" "));
 	}
 	
 	
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available.
 	private void gotAccount() {
 		Log.d(TAG, "in gotAccount()");
 		
 		Account account = accountManager.getAccountByName(accountName);
 		if (account != null) {
 			// invalid token
 			if (authToken == null) {	
 				Log.d(TAG, "auth token is null");
 				
 				accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, 
 						true, new AccountManagerCallback<Bundle>() {
 
 							public void run(AccountManagerFuture<Bundle> future) {
 								try {
 									Bundle bundle = future.getResult();
 									if (bundle.containsKey(
 											AccountManager.KEY_INTENT)) {
 										Intent intent = bundle.getParcelable(
 												AccountManager.KEY_INTENT);
 										int flags = intent.getFlags();
 										flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK; // ???
 										intent.setFlags(flags);
 										startActivityForResult(intent, 
 												REQUEST_AUTHENTICATE);
 									} else if (bundle.containsKey(
 											AccountManager.KEY_AUTHTOKEN)) {
 										setAuthToken(bundle.getString(
 												AccountManager.KEY_AUTHTOKEN));
 										executeRefreshCalendars(); // needed??
 									}
 								} catch (Exception e) {
 									handleException(e);
 								}
 							}
 						}, null);
 			// valid token
 			} else 					
 				//executeRefreshCalendars();	// REMOVE??
 			return;
 		}	
 		chooseAccount();	// if account is null
 	}
 	
 	
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available.
 	//
 	// This method is called if getAccountByName fails in gotAccount() (see
 	// above). (Also called in onActivityResult()... 
 	private void chooseAccount() {
 		Log.d(TAG, "in chooseAccount()");
 		accountManager.manager.getAuthTokenByFeatures(
 					GoogleAccountManager.ACCOUNT_TYPE,
 			        AUTH_TOKEN_TYPE,	// calendar
 			        null,
 			        calendarActivity.this,
 			        null,				// no addAccountOptions
 			        null,				// no getAuthTokenOptions
 			        new AccountManagerCallback<Bundle>() {
 
 			            public void run(AccountManagerFuture<Bundle> future) {
 			            	Bundle bundle;
 			            	try {
 			            		Log.d(TAG, "calling bundle = future.getResult();");
 			            		bundle = future.getResult();
 			            		Log.d(TAG, "returned from bundle = future.getResult();");
 			            		setAccountName(bundle.getString(
 			            				AccountManager.KEY_ACCOUNT_NAME));
 			            		setAuthToken(bundle.getString(
 			            				AccountManager.KEY_AUTHTOKEN));
 			            		executeRefreshCalendars();
 			            	} catch (OperationCanceledException e) {
 			            		// user canceled
 			            	} catch (AuthenticatorException e) {
 			            		handleException(e);
 			            	} catch (IOException e) {
 			            		handleException(e);
 			            	}
 			            }
 			        },
 			        null);				// no handler
 	} 
 		
 	
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available.
 	//
 	// Saves the authorization token to preferences and to class variable
 	// "authToken."
 	void setAuthToken(String authToken) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("AUTH_TOKEN", authToken);
 		editor.commit();
 		this.authToken = authToken;
 	}
 	
 	
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available.
 	//
 	// Saves the account name to preferences and to class variable
 	// "accountName."
 	void setAccountName(String accountName) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("ACCOUNT_NAME", accountName);
 		editor.remove("GSESSION_ID");
 		editor.commit();
 		this.accountName = accountName;
 		gsessionid = null;
 	}
 	
 
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available.
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 	    switch (requestCode) {
 	    	case REQUEST_AUTHENTICATE:
 	            if (resultCode == RESULT_OK) 
 	                gotAccount();
 	            else {
 	            	Log.d(TAG, "in onActivityResult(), calling chooseAccount()");
 	                chooseAccount();
 	            }
 	    		break;
 	    }
 	}
 	  
 	  
 	
 	// Updates class variable "calendars" to contain all calendars currently 
 	// found in own calendars feed. 
 	private void executeRefreshCalendars() {
 		Log.d(TAG, "executing refreshCalendars");
 	    List<CalendarEntry> calendars = this.calendars;
 	    calendars.clear();
 	    try {
 	    	CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
 	    	// add all existing calendars to list
 	    	while (true) {
 	    		// calls to client.executeGetCalendarFeed(url) invoke
 	    		// client.initialize() (defined above)
 	    		// add all existing owned calendars to list
 	    		CalendarFeed feed = client.executeGetCalendarFeed(url);
 	    		if (feed.calendars != null) 
 	    			calendars.addAll(feed.calendars);
 	    		String nextLink = feed.getNextLink();
 	    		if (nextLink == null) 
 	    			break;
 	    	} 
 	        int numCalendars = calendars.size();
 	        calendarNames = new String[numCalendars];
 	        for (int i = 0; i < numCalendars; i++) {
 	        	Log.d(TAG, "EFC: cal name: " + calendars.get(i).title);
 	        	Log.d(TAG, "EFC: cal id: " + calendars.get(i).id);
 	        	
 	        	calendarNames[i] = calendars.get(i).title;
 	        }
 	    } catch (IOException e) {
 	    	handleException(e);
 	    	calendars.clear();
 	    } 
 	 }
 	
 	
 	
 	// Check to see if MoldHold calendar exits in list of calendar names 
 	// retrieved from own calendars feed. 
 	private boolean checkCalendarExists() {
 		boolean		found = false;	// initialize to false
 		
 		if (calendarNames == null) {
 			Log.d(TAG, "calendarNames is null...");
 			return found;
 		}
     	for (int i = 0; i < calendarNames.length; i++) {
     		if (calendarNames[i].equals(CALENDAR_NAME)) {
     			Log.d(TAG, "MoldHold Calendar found!!"); 
     			found = true;
     		}
     	}
     	if (found == false)
     		Log.d(TAG, "MoldHold Calendar not found!!");
     	return found;
 	}
 	
 	
 	
 	// Creates MoldHold calendar, saves calendar id to preferences
 	private void createNewCalendar() {
 		// https://www.google.com/calendar/feeds/default/owncalendars/full
         CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
         CalendarEntry calendar = new CalendarEntry();
         calendar.title = CALENDAR_NAME;
         calendar.summary = "This calendar contains the expiration dates " +
         		"managed by MoldHold";
         try {
         	CalendarEntry newCalendar = client.executeInsertCalendar(calendar, url);
           
         	// get calendar id and add to preferences
         	calendarID = newCalendar.id.substring(
         		  newCalendar.id.lastIndexOf('/') + 1); 
         	SharedPreferences.Editor editor = settings.edit();
   		  	editor.putString("CALENDAR_ID", calendarID);
   		  	editor.commit();
         } catch (IOException e) {
         	handleException(e);
         }
         executeRefreshCalendars();
 	}
 	
 	
 	
 	// The following is adapted from calendar-v2-atom-android-sample by
 	// Yaniv Inbar, thanks to Yaniv for making this example available.
 	//
 	// CHANGES???
 	void handleException(Exception e) {
 		e.printStackTrace();
 		Log.e(TAG, "error", e);
 		if (e instanceof HttpResponseException) {
 			HttpResponse response = ((HttpResponseException) e).response;
 		    int statusCode = response.statusCode;
 		    try {
 		    	response.ignore();
 		    } catch (IOException e1) {
 		        e1.printStackTrace();
 		    }
 		    // TODO(yanivi): should only try this once to avoid infinite loop ?????
 		    if (statusCode == 401) {
 		    	Log.d(TAG, "calling gotAccount from handleException 401");
 		    	gotAccount();
 		        return;
 		    }
 		}
 		Log.e(TAG, e.getMessage(), e);
 	}
 
 	
 	
 	// Calculates the suggested alarm date (3 days prior to expiration date).
 	private void setAlarmDate() {
         // get the current date
         final Calendar c = Calendar.getInstance();
         
         //Add the shelf life to the current date (minus 3 for early alarm)
         // to calculate the expiration date alarm.
         int i = shelfLife.intValue();
         
         c.add(Calendar.DAY_OF_MONTH, (i- DAYS_PRIOR));
         // set class variables
         cYear = c.get(Calendar.YEAR);
         cMonth = c.get(Calendar.MONTH);
         cDay = c.get(Calendar.DAY_OF_MONTH);
 	}
 	
 	
 	
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DATE_DIALOG_ID:
             return new DatePickerDialog(this,
                         mDateSetListener,
                         cYear, cMonth, cDay);
         }
         return null;
     }
 	
 	
     
     // The callback received when the user "sets" the date in the dialog
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year, 
                                       int monthOfYear, int dayOfMonth) {
                     cYear = year;
                     cMonth = monthOfYear;
                     cDay = dayOfMonth;
                 	
     				EventEntry event = newEvent();
     				addNewEvent(event);
     				
                 	Intent intent = new Intent();
                 	setResult(Activity.RESULT_OK, intent);
                 	finish();
                 }
             };          
        
     
     
     // Adds new event to calendar (want it to add to MoldHold calendar, but
     // currently only have it working with the user's default/main calendar...
     private void addNewEvent(EventEntry event) {
     	CalendarUrl url = CalendarUrl.forEventFeed("default", "private", "full");
         try {
 			EventEntry result = client.executeInsertEvent(event, url);
 			// do anything with result??
 			// save event id??
 		} catch (IOException e) {
 			handleException(e);
 		}
 		// toast to say alarm has been added
 		makeToast();
     }
     
     
     
     // Creates a new calendar event with a title, date, time, and reminder.
     private EventEntry newEvent() {
        EventEntry event = new EventEntry();
        event.title = "Your " + productName + " expires in 3 days";
        When when = new When();
         Calendar c = Calendar.getInstance();
         c.set(cYear, cMonth, cDay, 5, 15);
         when.startTime = new DateTime(c.getTime());	// convert Calendar to Date
         when.endTime = new DateTime(c.getTime());
         // alarm will always go off at noon...
         // add alarm
         Reminder reminder = new Reminder();
         reminder.minutes = MIN_BEFORE;	// # min b4 start time reminder is to go off
         reminder.method = "alert";
         when.reminder = reminder;
         event.when = when;
         return event;
     }
     
     
     
     // Toast pop-up message to tell user that alarm was successfully
     // added to calendar.
     private void makeToast() {
 		Toast msg = Toast.makeText(calendarActivity.this, "Alarm Added!", 
 				Toast.LENGTH_SHORT);
 		msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, 
 				msg.getYOffset() / 2);
 		msg.show();
     }
 }
