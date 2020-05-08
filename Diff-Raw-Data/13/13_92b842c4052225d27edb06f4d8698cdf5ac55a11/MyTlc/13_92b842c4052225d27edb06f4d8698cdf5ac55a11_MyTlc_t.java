 /*
  * Copyright 2012, 2013 Devin Collins <agent1709@gmail.com>,
  * Bobby Ore <bob1987@gmail.com>, Casey Stark <starkca90@gmail.com>
  *
  * This file is part of MyTLC Sync.
  *
  * MyTLC Sync is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyTLC Sync is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyTLC Sync.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.layer8apps;
 
 import java.lang.reflect.Field;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.*;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.*;
 import android.provider.CalendarContract;
 import android.support.v4.app.DialogFragment;
 import android.text.InputType;
import android.text.TextUtils;
 import android.view.ViewConfiguration;
 import android.widget.*;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 /************
  *  PURPOSE: Primary UI thread that the user sees when
  *      they open the app
  *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>, Casey Stark <starkca90@gmail>
  *************/
 public class MyTlc extends SherlockFragmentActivity {
 
     // Public variables used throughout the entire class
 	private String username;
 	private String password;
     private boolean logonSaved;
     private int calID = -1;
 
 
 	private CheckBox remember;
 	private EditText txtUsername;
 	private EditText txtPassword;
     private TextView results;
 
     private Preferences pf;
 
     private MyHandler mHandler;
 
     /************
      *  PURPOSE: Primary thread that starts everything
      *  ARGUMENTS: <Bundle> savedInstanceState
      *  RETURNS: void
      *  AUTHOR: Casey Stark <starkca90@gmail.com>, Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>
      ************/
     @Override
     public void onCreate(Bundle savedInstanceState) {
         /*************
          * The following try statement makes the app think that the user doesn't
          * have a hard settings button.  This allows the options menu to always be
          * visible
          ************/
         try{
             // Get the configuration of the device
             ViewConfiguration config = ViewConfiguration.get(this);
             // Check if the phone has a hard settings key
             Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
             // If it does...
             if(menuKeyField != null) {
                 // Make our app think it doesn't!
                 menuKeyField.setAccessible(true);
                 menuKeyField.setBoolean(config, false);
             }
         }catch(Exception ex){
             // Do nothing
         }
 
         // Create a new preferences manager
         pf = new Preferences(this);
 
         // Set the theme of the app
         applyTheme();
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         txtUsername = (EditText) findViewById(R.id.txtUsername);
         txtUsername.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
         txtPassword = (EditText) findViewById(R.id.txtPassword);
         remember = (CheckBox) findViewById(R.id.remember);
         results = (TextView) findViewById(R.id.results);
 
         // Get a reference to our last handler
         final Object handler = getLastCustomNonConfigurationInstance();
         // If the last handler existed
         if (handler != null) {
             // Cast the handler to MyHandler so we can use it
             mHandler = (MyHandler) handler;
         } else {
             // Otherwise create a new handler
             mHandler = new MyHandler();
         }
         // Assign the activity for our handler so it knows the proper reference
         mHandler.setActivity(this);
 
         // Load all of the users saved options
         checkSavedSettings();
 
         // Show the ads
        //showAds();
     }
 
     /************
      *  PURPOSE: Shows the ad banner at the bottom of the main page
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
    /*private void showAds() {
         // Get reference to our adView
         AdView adView = (AdView) findViewById(R.id.ad);
         // Create a new request for ads
         AdRequest ar = new AdRequest();
 
         // The line below is for testing the ads and should be turned on for demoing
         // ar.setTesting(true);
 
         // Load the ad
         adView.loadAd(ar);
    }*/
 
     /************
      *  PURPOSE: Handles when the user changes states (ie: rotates)
      *  ARGUMENTS: null
      *  RETURNS: Object
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     @Override
     public Object onRetainCustomNonConfigurationInstance() {
         // Clear the current stored activity to prevent memory leaks
         mHandler.setActivity(null);
         // Return the handler
         return mHandler;
     }
 
     /************
      *  PURPOSE: Handles when the user presses menu buttons
      *  ARGUMENTS: <MenuItem> item
      *  RETURNS: boolean
      *      TRUE: The menu item click was handled properly
      *      FALSE: The menu item click wasn't handled properly
      *  AUTHOR: Casey Stark <starkca90@gmail.com>, Devin Collins <agent14709@gmail.com>
      ************/
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
         // Figure out which menu item was clicked
 		if (item.getItemId() == R.id.login) {
             // Start the login process
             login();
             return true;
         } else if (item.getItemId() == R.id.settings) {
             // Create a new instance of the Settings intent
 			Intent settingsInt = new Intent(getBaseContext(), Settings.class);
             // Start the Settings intent
 			startActivity(settingsInt);
 			return true;
 		} else if(item.getItemId() == R.id.deleteCreds) {
             // Delete the users credentials
 			deleteCredentials();
 			return true;
 		} else {
 			return true;
 		}
 	}
 
     /************
      *  PURPOSE: Determines how the menu is displayed
      *  ARGUMENTS: <Menu> menu
      *  RETURNS: boolean
      *      TRUE: The menu was created properly
      *      FALSE: There was an error creating the menu
      *  AUTHOR: Casey Stark <starkca90@gmail.com>, Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>
      ************/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         // Sets the menu to our menu layout
 		getSupportMenuInflater().inflate(R.menu.main, menu);
         // If the user has their information saved...
 		if(logonSaved)
 		{
             // Find the ID of the deleteCredentials menu
 			MenuItem item = menu.findItem(R.id.deleteCreds);
             // Enable the MenuItem
 			item.setEnabled(true);
 		}
         // Show the menu
 		super.onCreateOptionsMenu(menu);
 		return true;
 	}
 
     /************
      *  PURPOSE: Provides logic to:
      *      1. Determine if user wants login information stored
      *      2. Store the login information for future use
      *  ARGUMENTS: null
      *  RETURNS: boolean
      *      TRUE: The user had their username and password saved
      *      FALSE: The user didn't have a saved username and password
      *  AUTHOR: Casey Stark <starkca90@gmail.com>, Devin Collins <agent14709@gmail.com>
      ************/
 	private boolean storeCredentials() {
         // Try to store the user information into the preferences
 		if (pf.setUsername(username) && pf.setPassword(password)) {
             return true;
         } else {
             return false;
         }
 	}
 
     /************
      *  PURPOSE: Deletes the user credentials preferences
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
 	private void deleteCredentials() {
         username = null;
         password = null;
         pf.deleteUsername();
         pf.deletePassword();
         remember.setChecked(false);
         txtPassword.setText("");
         txtUsername.setText("");
 	}
 
     /************
      *  PURPOSE: Checks our saved preferences
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void checkSavedSettings() {
         // Get the username from preferences
         String user = pf.getUsername();
         // Make sure the username wasn't blank
         if (user != null) {
             // Store the username locally
             this.username = user;
             // Assign the text to the textbox
             txtUsername.setText(user);
         }
         // Get the password from preferences
         String password = pf.getPassword();
         // Make sure the password isn't blank
         if (password != null) {
             // Store the password locally
             this.password = password;
             // Fill the password box with the password
             txtPassword.setText(password);
         }
         // Check if the user had a stored password and username
         logonSaved = (user != null && password != null);
         // Check the box if the information is saved
         remember.setChecked(logonSaved);
         // Get the stored calendar ID
         calID = pf.getCalendarID();
 
     }
 
     /************
      *  PURPOSE: Makes sure we have a valid username and
      *      password
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void login() {
         // Store the username and password
         username = txtUsername.getText().toString();
         password = txtPassword.getText().toString();
         // If the user wants to store the information...
         if (remember.isChecked()) {
             // Try to save the credentials
             if (!storeCredentials()) {
                 // Notify the user that the saved failed
                 Toast.makeText(this, "Error saving credentials", Toast.LENGTH_LONG).show();
             }
         }
 
         // Check the saved settings
         checkSavedSettings();
         // Make sure we have a username and password set
        if ((!TextUtils.isEmpty(username)) && (!TextUtils.isEmpty(password))) {
             // If the user has a stored calendar...
             if (calID != -1) {
                 // Start getting the information
                 runEvents();
             } else {
                 // Load all the calendars
                 String calendars[][] = selectCalendar();
                 // Create a dialog to ask the user about which calendar
                 createCalendarDialog(calendars);
             }
         } else {
             // Notify the user they need to put in information
             Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_LONG).show();
         }
     }
 
     /************
      *  PURPOSE: Retrieves a list of all calendars on the device and
      *      returns the name of each calendar
      *  ARGUMENTS: null
      *  RETURNS: String[][]
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private String[][] selectCalendar() {
         try {
             Cursor managedCursor = null;
             int nameColumn = 0;
             int idColumn = 0;
             String[] projection;
 
             /*****************
              * Figure out which version of Android we're on to know how
              * we should be calling the calendar
              *****************/
             ContentResolver cr = getContentResolver();
             if(Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                 try {
                     managedCursor = cr.query(Uri.parse("content://calendar/calendars/"), new String[] {"_id", "displayName"}, "selected=1", null, null);
                     nameColumn = 1;
                 } catch (Exception e) {
                     return null;
                 }
             } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 try {
                     managedCursor = cr.query(Uri.parse("content://com.android.calendar/calendars/"), new String[] {"_id", "displayName"}, "selected=1", null, null);
                     nameColumn = 1;
                 } catch (Exception e) {
                     return null;
                 }
             } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                 Uri uri = CalendarContract.Calendars.CONTENT_URI;
                 projection = new String[]{CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars._ID};
                 try {
                     // Make sure the calendar loaded properly
                     managedCursor = cr.query(uri, projection, null, null, null);
                     nameColumn = managedCursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
                     idColumn = managedCursor.getColumnIndex(CalendarContract.Calendars._ID);
                 } catch (Exception e) {
                     return null;
                 }
             }
 
             /************
              * Go to the first listing in our calendar
              *****************/
             if (managedCursor.moveToFirst()) {
                 String calendars[][] = new String[managedCursor.getCount()][2];
                 int count = 0;
                 /************
                  * The following 'do' statement checks for a valid calendar name.
                  * If one doesn't exist, such as in the case of an Exchange
                  * server, we give it a blank name.
                  *****************/
                 do {
                     calendars[count][0] = managedCursor.getString(idColumn);
                     if (managedCursor.getString(nameColumn) == null || managedCursor.getString(nameColumn).equals("")) {
                         calendars[count][1] = "Unnamed calendar";
                     } else {
                         calendars[count][1] = managedCursor.getString(nameColumn);
                     }
                     count++;
                 } while (managedCursor.moveToNext());
                 return calendars;
             }
         } catch (Exception e) {
             return null;
         }
         return null;
     }
 
     /************
      *  PURPOSE: This creates a dialog for our user to choose their
      *      calendar from.
      *  ARGUMENTS: String[][]
      *  RETURNS: boolean
      *      TRUE: We successfully created a calendar dialog
      *      FALSE: We failed to create a calendar dialog
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private boolean createCalendarDialog(final String[][] calendars) {
         try {
             // Get the names for each calendar
             String names[] = new String[calendars.length];
             for (int i = 0; i < calendars.length; i++) {
                 names[i] = calendars[i][1];
             }
 
             // Create a new alert dialog and set the options
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Choose a calendar");
             builder.setCancelable(true);
             builder.setSingleChoiceItems(names, -1, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialoginterface, int i) {
                     // When we choose a calendar, get the ID and then get the events
                     calID = Integer.parseInt(calendars[i][0]);
                     dialoginterface.dismiss();
                     runEvents();
                 }
             });
             AlertDialog alert = builder.create();
             alert.show();
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     /************
      *  PURPOSE: Handles creating our intial setup when
      *      we run things in the background.
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void runEvents() {
         results.setText("");
         // This is our background service.  We create it and assign
         // variables that we'll be using.
         Intent intent = new Intent(this, CalendarHandler.class);
         Messenger messenger = new Messenger(mHandler);
         intent.putExtra("handler", messenger);
         intent.putExtra("username", username);
         intent.putExtra("password", password);
         intent.putExtra("calendarID", calID);
         // Start the service
         startService(intent);
         // Show the progress dialog
         showProgress();
     }
 
     /************
      *  PURPOSE: Shows our fragment dialog that we use for
      *      progress updates
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void showProgress() {
         DialogFragment fragment;
         // Create a new instance of the fragmentDialog
         fragment = TLCProgressDialog.newInstance();
         // Show the dialog with the "progress" tag for later reference
         fragment.show(getSupportFragmentManager(), "progress");
     }
 
     /************
      *  PURPOSE: Changes the saved theme based on the users selection
      *    and current operating system
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void applyTheme() {
         // Get the saved theme
         int theme = pf.getTheme();
         if (theme == 0) {
             // Check for the version of Android we're running and then
             // set the theme based on if we need ABS or no
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 setTheme(R.style.Theme_Sherlock_BlueWhite);
             } else {
                 setTheme(R.style.Theme_BlueWhite);
             }
         } else if (theme == 1) {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 setTheme(R.style.Theme_Sherlock_OrangeBlack);
             } else {
                 setTheme(R.style.Theme_OrangeBlack);
             }
         } else if (theme == 2) {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 setTheme(R.style.Theme_Sherlock_RedWhite);
             } else {
                 setTheme(R.style.Theme_RedWhite);
             }
         } else {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 setTheme(R.style.Theme_Sherlock_BlueWhite);
             } else {
                 setTheme(R.style.Theme_BlueWhite);
             }
         }
     }
 
     /************
      *  PURPOSE: Controls our dialogFragment used to show progress
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     public static class TLCProgressDialog extends SherlockDialogFragment {
 
         // Class variable for reference
         ProgressDialog dialog;
 
         /************
          *  PURPOSE: Empty constructor
          *  ARGUMENTS: null
          *  RETURNS: void
          *  AUTHOR: Devin Collins <agent14709@gmail.com>
          *************/
         public TLCProgressDialog() {}
 
         /************
          *  PURPOSE: Creates a new instance of our dialogFragment
          *  ARGUMENTS: null
          *  RETURNS: TLCProgressDialog
          *  AUTHOR: Devin Collins <agent14709@gmail.com>
          *************/
         static TLCProgressDialog newInstance() {
             // Create a new version of the dialogFragment
             TLCProgressDialog d = new TLCProgressDialog();
             return d;
         }
 
         /************
          *  PURPOSE: Handles creation of the default dialog
          *  ARGUMENTS: <Bundle> savedInstanceState
          *  RETURNS: Dialog
          *  AUTHOR: Devin Collins <agent14709@gmail.com>
          *************/
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Create a new progress dialog based on our activity
             dialog = new ProgressDialog(getActivity());
             // Set the title
             dialog.setTitle("Getting schedule");
             return dialog;
         }
 
         /************
          *  PURPOSE: Change the displayed message on the dialog
          *  ARGUMENTS: <String> message
          *  RETURNS: void
          *  AUTHOR: Devin Collins <agent14709@gmail.com>
          *************/
         public void setMessage(String message) {
             // Set the message
             dialog.setMessage(message);
         }
     }
 }
