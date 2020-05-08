 package au.edu.adelaide.physics.opticsstatusboard;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.content.LocalBroadcastManager;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class BackgroundManager extends IntentService {
 	private ArrayList<Person> people;
 	private URL website, updateWebsite;
 	private boolean showNameInList, newVersion, locationEnabled, locationNotification, locationVibrate, reminderEnabled, reminderVibrate;
 	private Person user;
 	private String username, password, sortMode, webAddress, updateAddress;
 	public static final int MAX_RETRIES = 3;
 	private int retries;
 	private final long[] vibratePattern = {0, 200, 0, 200, 0, 200};
 	
 	public BackgroundManager() {
 		super("BackgroundService");
 	}
 		
 	public boolean canShowNameInList() {
     	return showNameInList;
     }
 	
 	private void checkForUpdate() {
    	new UpdateChecker(this).check(updateWebsite);
     }
 	
 	private void createNotification(int type, boolean param) {
 		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
 		nBuilder.setSmallIcon(R.drawable.ic_launcher);
 		
 		switch (type) {
 		case 0:
 			if (param) {
 				nBuilder.setContentTitle("Location Based Signin");
 				nBuilder.setContentText("You are in proximity of the university and have thus been signed in.");
 			} else {
 				nBuilder.setContentTitle("Location Based Signout");
 				nBuilder.setContentText("You are out of proximity of the university and have thus been signed out.");
 			}
 			
 			if (locationVibrate)
 				nBuilder.setVibrate(vibratePattern);
 			
 			break;
 		case 1:
 			if (param) {
 				nBuilder.setContentTitle("Signin Reminder");
 				nBuilder.setContentText("This is a reminder to sign in");
 			} else {
 				nBuilder.setContentTitle("Signout Reminder");
 				nBuilder.setContentText("This is a reminder to sign out");
 			}
 			
 			if (reminderVibrate)
 				nBuilder.setVibrate(vibratePattern);
 			
 			break;
 		case 2:
 			nBuilder.setContentTitle("Debug notification");
 			
 			if (param)
 				nBuilder.setContentText("Status changed during widget update!!");
 			else
 				nBuilder.setContentText("Widget updated without status change.");
 			
 			nBuilder.setVibrate(vibratePattern);
 		default:
 			break;
 		}
 		
 
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.notify(0, nBuilder.build());
 	}
 	
     public void decRetries() {
     	retries -= 1;
     }
     
     public String getPassword() {
     	return password;
     }
     
     public ArrayList<Person> getPeople() {
     	return people;
     }
     
     public int getRetries() {
     	return retries;
     }
     
     public Person getUser() {
     	return user;
     }
     
     public String getUsername() {
     	return username;
     }
     
     public void notifyNewVersion(boolean newVersion) {
     	this.newVersion = newVersion;
     }
     
     @Override
 	public void onCreate() {
 		super.onCreate();
 		
 		website = null;
         retries = 0;
         user = null;
         newVersion = false;
         
         people = new ArrayList<Person>(0);
         
         refreshUserData();
 		
 		try {
 			website = new URL(webAddress);
 			updateWebsite = new URL(updateAddress);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void onDestroy () {
 		super.onDestroy();
 	}
 	
 	@Override
 	protected void onHandleIntent (Intent intent) {
 		Bundle data = intent.getExtras();
 		boolean preSignedIn = false;
 		
 		if (user != null)
 			if (user.getStatus() == 0)
 				preSignedIn = true;
 		
 		//Update the list of people and get the user
 		refreshList();
 		
 		//Set up the objects for returning data
 		Intent returnIntent = new Intent("BackgroundRefresh");
 		Bundle returnData = new Bundle();
 		
 		//If the service has been sent an updated user, post the changes to the sign in page
 		if (data != null) {
 			if (data.containsKey("updatedUser")) {
 				user = (Person) data.getParcelable("updatedUser");
 				String result = postData();
 				returnData.putString("postResult", result);
 			}
 			
 			//See if a request for a new version check was asked for
 			if (data.containsKey("requestVersionCheck")) {
 				if (data.getBoolean("requestVersionCheck")) {
 					checkForUpdate();
 					returnData.putBoolean("newVersion", newVersion);
 				}
 			}
 			
 			//Check if a widget has requested a sign in/out
 			if (data.containsKey("widgetSignIn")) {
 				if (data.getBoolean("widgetSignIn")) {
 					user.setStatus(0);
 					showToast("Widget sign in request sent");
 				} else {
 					user.setStatus(1);
 					showToast("Widget sign out request sent");
 				}
 				
 				postData();
 			}
 			
 			//Check if this intent was fired by the location proximity alert
 			if (data.containsKey(LocationManager.KEY_PROXIMITY_ENTERING) && locationEnabled) {
 				boolean entered = data.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING);
 				
 				if (entered) {
 					user.setStatus(0);
 				} else {
 					user.setStatus(1);
 				}
 				
 				if (locationNotification)
 					createNotification(0, entered);
 				
 				postData();
 			}
 			
 			//Check to see if fired by the alarm, and then send the required notification
 			if (data.containsKey("reminderAlarm") && reminderEnabled) {
 				if (reminderEnabled) {
 					if (data.getBoolean("reminderAlarm")) {
 						System.out.println("Reminder alarm to sign in went off");
 					} else {
 						System.out.println("Reminder alarm to sign out went off");
 						if (user.getStatus() == 0)
 							createNotification(1, false);
 					}
 				}
 			}
 			
 			if (data.containsKey("widgetAlarmCall")) {
 				boolean statusChange = false;
 				
 				if (user.getStatus() == 0 && !preSignedIn)
 					statusChange = true;
 				if (user.getStatus() > 0 && preSignedIn)
 					statusChange = true;
 				
 				createNotification(2, statusChange);
 			}
 		}
 		
 		//Refresh the widget
 		updateWidget();
 		
 		//Put the data to be returned into the bundle
 		if (getPeople() != null)
 			returnData.putParcelableArrayList("people", getPeople());
 		if (getUser() != null)
 			returnData.putParcelable("user", getUser());
 		//Put the bundle into the intent to send off
 		returnIntent.putExtras(returnData);
 		//Send off the intent
 		LocalBroadcastManager.getInstance(this).sendBroadcast(returnIntent);
 	}
     public String postData() {
     	refreshUserData();
     	
     	Poster poster = new Poster(website, this);
     	String result = poster.postToWebsite();
     	
     	return result;
     }
     public void refreshList() {
     	refreshUserData();
     	
     	WebParser parser = new WebParser(people, this, sortMode);
     	parser.parseWebsite(website);
     	user = parser.fetchUser();
 ;    	people = parser.fetchPeople();
     }
     private void refreshUserData() {
     	//Get the saved preferences
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         //Retrieve the required settings
         username = settings.getString("username", "").trim();
         password = settings.getString("password", "").trim();
         sortMode = settings.getString("sortMode", "3");
         webAddress = settings.getString("webAddress", "http://www.physics.adelaide.edu.au/cgi-bin/usignin/usignin.cgi");
         showNameInList = settings.getBoolean("showName", false);
         updateAddress = settings.getString("updateAddress", "https://dl.dropbox.com/u/11481054/OpticsStatusBoardApp/current_version.html");
         locationEnabled = settings.getBoolean("locationEnabled", false);
         locationNotification = settings.getBoolean("locationNotification", false);
         locationVibrate = settings.getBoolean("locationVibrate", false);
         reminderEnabled = settings.getBoolean("reminderEnabled", false);
         reminderVibrate = settings.getBoolean("reminderVibrate", false);
     }
     public void setRetries(int retries) {
     	this.retries = retries;
     }
     private void setWidgetCounter(RemoteViews views) {
     	Person current;
     	int counter = 0;
     	
     	for (int i = 0; i < people.size(); i++) {
     		current = people.get(i);
     		if (current.getStatus() == 0)
     			counter++;
     	}
     	
     	views.setTextViewText(R.id.inCounterW, "In: "+Integer.toString(counter));
     }
     private void setWidgetImage(int status, RemoteViews views) {
 		int imageId = 0;
 
 		switch (status) {
 		case 0:
 			imageId = R.drawable.in;
 			break;
 		case 1:
 			imageId = R.drawable.out;
 			break;
 		case 2:
 			imageId = R.drawable.meeting;
 			break;
 		case 3:
 			imageId = R.drawable.lunch;
 			break;
 		case 4:
 			imageId = R.drawable.sick;
 			break;
 		case 5:
 			imageId = R.drawable.vacation;
 			break;
 		default:
 			// TODO What should it do in this case?
 		}
 		
 		views.setImageViewResource(R.id.statusButtonW, imageId);
     }
     
     public void showToast(String data) {
     	Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
     }
     
     private void updateWidget() {
     	if (user != null) {
     		Context context = getApplicationContext();
     		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
     		ComponentName myWidget = new ComponentName(context, ToggleWidget.class);
     		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.toggle_widget);
 
     		// Register an onClickListener to sign in
     		if (user.getStatus() > 0) {
     			Intent signInIntent = new Intent(context, BackgroundManager.class);
     			signInIntent.putExtra("widgetSignIn", true);
     			PendingIntent pendingSignInIntent = PendingIntent.getService(context, 0, signInIntent, PendingIntent.FLAG_UPDATE_CURRENT);
     			remoteViews.setOnClickPendingIntent(R.id.statusButtonW, pendingSignInIntent);    			
     		} else {
     			//And to sign out
     			Intent signOutIntent = new Intent(context, BackgroundManager.class);
     			signOutIntent.putExtra("widgetSignIn", false);
     			PendingIntent pendingSignOutIntent = PendingIntent.getService(context, 0, signOutIntent, PendingIntent.FLAG_UPDATE_CURRENT);
     			remoteViews.setOnClickPendingIntent(R.id.statusButtonW, pendingSignOutIntent);
     		}
     		
     		setWidgetImage(user.getStatus(), remoteViews);
     		setWidgetCounter(remoteViews);
 
     		appWidgetManager.updateAppWidget(myWidget, remoteViews);
     	}
     }
 }
