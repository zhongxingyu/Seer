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
 
 import java.io.Console;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import android.app.*;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.CalendarContract;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /************
  *  PURPOSE: Handles the UI for all of the settings in the app
  *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>
  *************/
 public class Settings extends SherlockActivity {
 
     private Spinner spinCal, spinSync, spinWeekly, spinTheme, spinNotifications;
     private Button btnSync;
     private TextView txtSync;
     int hour = 0;
     int minute = 0;
     private String[] calIds;
     private boolean timeSaved = false, themeChanged = false;
     private Preferences pf;
 
     static final int ID_TIMEPICKER = 0x0300;
     static final int RESTART_DIALOG = 0x0100;
 
     /************
      *  PURPOSE: Start of the app
      *  ARGUMENTS: Bundle savedInstanceState
      *  RETURNS: void
      *  AUTHOR: Casey Stark <starkca90@gmail.com>, Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>
      ************/
     @Override
     public void onCreate(Bundle savedInstanceState) {
         // Get a new reference to our stored preferences
         pf = new Preferences(this);
 
         // Apply the theme
         applyTheme();
 
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
 
         spinCal = (Spinner) findViewById(R.id.spinCals);
         spinSync = (Spinner) findViewById(R.id.spinSync);
         spinWeekly = (Spinner) findViewById(R.id.spinWeekly);
         spinTheme = (Spinner) findViewById(R.id.spinTheme);
         spinNotifications = (Spinner) findViewById(R.id.spinNotifications);
 
         // Make sure we have support for older devices
         ActionBar actionBar = getSupportActionBar();
         // If the user clicks the app button in the corner, make it go back
         actionBar.setDisplayHomeAsUpEnabled(true);
 
         /*********************
          * The following group of adapter lines are used to specify
          * the spinners on the view, and what arrays goes with them from the
          * strings.xml values file.  We then identify each item as a spinner
          * and make it look like a dropdown box and assign our adapter to the
          * correct spinner
          *********************/
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sync, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinSync.setAdapter(adapter);
 
         adapter = ArrayAdapter.createFromResource(this, R.array.daysOfWeek, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinWeekly.setAdapter(adapter);
 
 
         adapter = ArrayAdapter.createFromResource(this, R.array.themes, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinTheme.setAdapter(adapter);
 
         adapter = ArrayAdapter.createFromResource(this, R.array.alarms, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinNotifications.setAdapter(adapter);
 
         // Load the calendar list
         createCalendarList();
 
         // Check for a saved calendar already
         checkSavedSettings();
 
         /****************
         * Here we define what happens when we change the dropdown identifying
         * when we want to autosync our events
         *****************/
         spinSync.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 if (i == 0) {
                     // Hide the daily choice options
                     LinearLayout llDaily = (LinearLayout) findViewById(R.id.llDailySync);
                     llDaily.setVisibility(View.GONE);
                     // Hide the weekly choice options
                     LinearLayout llWeekly = (LinearLayout) findViewById(R.id.llWeeklySync);
                     llWeekly.setVisibility(View.GONE);
                 } else if (i == 1) {
                     // Show the daily choice options
                     LinearLayout llDaily = (LinearLayout) findViewById(R.id.llDailySync);
                     llDaily.setVisibility(View.VISIBLE);
                     // Hide the weekly choice options
                     LinearLayout llWeekly = (LinearLayout) findViewById(R.id.llWeeklySync);
                     llWeekly.setVisibility(View.GONE);
                     btnSync = (Button) findViewById(R.id.btnDaily);
                     txtSync = (TextView) findViewById(R.id.txtDailySync);
                 } else if (i == 2) {
                     // Hide the daily choice options
                     LinearLayout llDaily = (LinearLayout) findViewById(R.id.llDailySync);
                     llDaily.setVisibility(View.GONE);
                     // Show the weekly choice options
                     LinearLayout llWeekly = (LinearLayout) findViewById(R.id.llWeeklySync);
                     llWeekly.setVisibility(View.VISIBLE);
                     btnSync = (Button) findViewById(R.id.btnWeekly);
                     txtSync = (TextView) findViewById(R.id.txtWeeklySync);
                 }
                 if (i != 0) {
                     btnSync.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             showDialog(ID_TIMEPICKER);
                         }
                     });
                     showSyncTime();
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 // Do nothing
             }
         });
 
         /****************
          * This is here specifically to change the text and make
          * it more readable for the user.
          ****************/
         spinCal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 if (i == 0) {
                     hideSyncSpinner();
                 } else {
                     showSyncSpinner();
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 // Do nothing
             }
         });
 
         /************
          *  PURPOSE: If we change the theme, make sure to mark it as changed
          *      so we know if we need to restart the app or not
          *  ARGUMENTS: null
          *  RETURNS: null
          *  AUTHOR: Devin Collins <agent14709@gmail.com>
          ************/
         spinTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 if (i != pf.getTheme()) {
                     themeChanged = true;
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 // Do nothing
             }
         });
 
     }
 
     /************
      *  PURPOSE: Hides the layout containing the syncing options
      *  ARGUMENTS: null
      *  RETURNS: null
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     private void hideSyncSpinner() {
         LinearLayout syncLayout = (LinearLayout) findViewById(R.id.llSync);
         syncLayout.setVisibility(View.GONE);
     }
 
     /************
      *  PURPOSE: Show the layout containing the syncing options
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     private void showSyncSpinner() {
         LinearLayout syncLayout = (LinearLayout) findViewById(R.id.llSync);
         syncLayout.setVisibility(View.VISIBLE);
     }
 
     /************
      *  PURPOSE: Handles the dialogs we create and keeps them organized
      *  ARGUMENTS: Menu menu
      *  RETURNS: boolean
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case ID_TIMEPICKER:
                 // If we don't currently have the time saved
                 if (!timeSaved) {
                     // Show the current hour and minute
                     hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                     minute = Calendar.getInstance().get(Calendar.MINUTE);
                 }
                 return new TimePickerDialog(this, timeSetListener, hour, minute, DateFormat.is24HourFormat(this));
             case RESTART_DIALOG:
                 // Creates a new click listener for when the user clicks yes or no
                 DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         switch (which) {
                             case DialogInterface.BUTTON_POSITIVE:
                                 // If the user clicks yes, restart the app
                                 System.exit(0);
                                 break;
                         }
                     }
                 };
                 // This creates our alert dialog, adds the messages and buttons
                 // and then returns it
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage("We must restart the app to change the theme, do you want to do this now?");
                 builder.setPositiveButton("Yes", listener);
                 builder.setNegativeButton("No", listener);
                 return builder.create();
             default:
                 return null;
         }
     }
 
     /************
      *  PURPOSE: Handles when the user changes the time
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
         @Override
         public void onTimeSet(TimePicker timePicker, int i, int i1) {
             timeSaved = true;
             hour = i;
             minute = i1;
             showSyncTime();
         }
     };
 
     /************
      *  PURPOSE: Defines what our action bar looks like
      *  ARGUMENTS: Menu menu
      *  RETURNS: boolean
      *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>, Casey Stark <starkca90@gmail.com>
      ************/
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Create the menu
         super.onCreateOptionsMenu(menu);
         // Assign our layout from the settings res file
         getSupportMenuInflater().inflate(R.menu.settings, menu);
         return true;
     }
 
     /************
      *  PURPOSE: Handles the menu selection options
      *  ARGUMENTS: MenuItem item
      *  RETURNS: boolean
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // See which item was selected
         if (item.getItemId() == R.id.save) {
             // Save the settings
             saveCalendar();
             return true;
         } else if (item.getItemId() == R.id.reset) {
             // Reload all the saved settings
             checkSavedSettings();
             return true;
         } else if (item.getItemId() == android.R.id.home) {
             // Close out
             finish();
             return true;
         } else {
             return false;
         }
     }
 
     /************
      *  PURPOSE: Checks for all of our previously saved settings
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>, Casey Stark <starkca90@gmail.com>
      ************/
     private boolean checkSavedSettings() {
         // Get the calendar ID and the sync options
         int tempID = pf.getCalendarID();
         int sync = pf.getTimeSpinner();
         // Change the spinner to choose if we're syncing or not
         spinSync.setSelection(sync);
         // Determine which type of syncing we're doing and show the proper information
         if (sync == 1) {
             btnSync = (Button) findViewById(R.id.btnDaily);
             txtSync = (TextView) findViewById(R.id.txtDailySync);
         } else if (sync == 2) {
             int weekly = pf.getWeekSpinner();
             btnSync = (Button) findViewById(R.id.btnWeekly);
             txtSync = (TextView) findViewById(R.id.txtWeeklySync);
             spinWeekly.setSelection(weekly);
         } else {
             spinSync.setSelection(0);
         }
         // Get the time of day that we're syncing
         String time = pf.getSyncTime();
         // If there's a time...
         if (time != null) {
             // Notify that we have a saved time
             timeSaved = true;
             // Parse the hour and minute
             hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
             minute = Integer.parseInt(time.substring(time.indexOf(":") + 1));
             showSyncTime();
         }
         // Get our alarm settings
         int notification = pf.getNotification();
        Log.e("ERRORLOG", String.valueOf(notification));
         spinNotifications.setSelection(notification);
         // Get the saved theme if possible
         try {
             int theme = pf.getTheme();
             if (theme == -1) {
                 theme = 0;
             }
             spinTheme.setSelection(theme);
         } catch (Exception e) {
             // TODO: Error reporting?
         }
         int count = 1;
         // Load each saved calendar and add it to the spinner
         if (calIds!= null) {
             for (String id : calIds) {
                 if (id.equals(String.valueOf(tempID))) {
                     spinCal.setSelection(count);
                     return true;
                 } else {
                     count ++;
                 }
             }
         }
         return true;
     }
 
     /************
      *  PURPOSE: Formats our time in 12 or 24 hours
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      ************/
     private void showSyncTime() {
         SimpleDateFormat timeFormat;
         // Check for the users time format of 24 or 12 hours
         if (!DateFormat.is24HourFormat(getBaseContext())) {
             timeFormat = new SimpleDateFormat("hh:mm aa");
         } else {
             timeFormat = new SimpleDateFormat("HH:mm");
         }
 
         // Set the date time
         if (!timeSaved) {
             txtSync.setText(R.string.no_sync_time);
         } else {
             Calendar c = Calendar.getInstance();
             c.set(Calendar.HOUR_OF_DAY, hour);
             c.set(Calendar.MINUTE, minute);
             Date dt = new Date();
             dt.setTime(c.getTimeInMillis());
             //dt.setHours(hour);
             //dt.setMinutes(minute);
             txtSync.setText("Check schedule at " + timeFormat.format(dt));
         }
     }
 
     /************
      *  PURPOSE: Ge a list of all the calendars on the device
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>, Casey Stark <starkca90@gmail.com>
      ************/
     private void createCalendarList() {
         ArrayList<String> calendars = new ArrayList<String>();
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
                     // TODO: Error reporting?
                 }
             } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 try {
                     managedCursor = cr.query(Uri.parse("content://com.android.calendar/calendars/"), new String[] {"_id", "displayName"}, "selected=1", null, null);
                     nameColumn = 1;
                 } catch (Exception e) {
                     // TODO: Error reporting?
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
                     // TODO: Error reporting?
                 }
             }
 
             /************
              * Go to the first listing in our calendar
              *****************/
             if (managedCursor.moveToFirst()) {
                 calIds = new String[managedCursor.getCount()];
                 String calNames[] = new String[managedCursor.getCount()];
                 int count = 0;
                 /************
                  * The following 'do' statement checks for a valid calendar name.
                  * If one doesn't exist, such as in the case of an Exchange
                  * server, we give it a blank name.
                  *****************/
                 do {
                     calIds[count] = managedCursor.getString(idColumn);
                     if (managedCursor.getString(nameColumn) == null || managedCursor.getString(nameColumn).equals("")) {
                         calNames[count] = "Unnamed Calendar";
                     } else {
                         calNames[count] = managedCursor.getString(nameColumn);
                     }
                     count++;
                 } while (managedCursor.moveToNext());
                 if (calIds != null) {
                     calendars.add("Don't save calendar");
                     // Add the list of calendars
                     calendars.addAll(Arrays.asList(calNames));
                 } else {
                     // Disable the calendar selection
                     spinCal.setEnabled(false);
                     calendars.add("No calendars found on device");
                 }
             } else {
                 // Disable the calendar selection
                 spinCal.setEnabled(false);
                 calendars.add("No calendars found on device");
             }
         } catch (Exception e) {
             spinCal.setEnabled(false);
             calendars.add("No calendars found on device");
         } finally {
             // Create a new adapter of strings
             ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, calendars);
             // Make the spinner a drop down item
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             // Assign the adapter to the spinner
             spinCal.setAdapter(adapter);
         }
     }
 
     /************
      *  PURPOSE: Saves all of our settings
      *  ARGUMENTS: null
      *  RETURNS: void
      *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>, Casey Stark <starkca90@gmail.com>
      ************/
     private void saveCalendar() {
         try {
             if (!spinCal.isEnabled() || spinCal.getSelectedItemPosition() == 0) {
                 /*
                  * If the user has chosen not to save the calendar
                  * then we delete the saved calendar ID (deleteCalendarID),
                   * how often we sync (timeSpinner), what day of the week
                   * we sync (WeekSpinner), and the time we sync (syncTime)
                  */
                 pf.deleteCalendarID();
                 pf.deleteTimeSpinner();
                 pf.deleteWeekSpinner();
                 pf.deleteSyncTime();
             } else {
                 if (spinSync.getSelectedItemPosition() == 0) {
                     // If the user doesn't want to sync, erase all information
                     pf.setCalendarID(Integer.valueOf(calIds[spinCal.getSelectedItemPosition() - 1]));
                     pf.deleteTimeSpinner();
                     pf.deleteWeekSpinner();
                     pf.deleteSyncTime();
                     cancelAlarm();
                 } else if (spinSync.getSelectedItemPosition() == 1) {
                     // If the user wants to sync daily, save that information
                     pf.setCalendarID(Integer.valueOf(calIds[spinCal.getSelectedItemPosition() - 1]));
                     pf.deleteWeekSpinner();
                     pf.setTimeSpinner(1);
                     pf.setSyncTime(String.valueOf(String.valueOf(hour) + ":" + String.valueOf(minute)));
                     setAlarm(1, hour, minute, 0);
                 } else {
                     // If the user wants to sync weekly, save that
                     pf.setCalendarID(Integer.valueOf(calIds[spinCal.getSelectedItemPosition() - 1]));
                     pf.setTimeSpinner(2);
                     pf.setWeekSpinner(spinWeekly.getSelectedItemPosition());
                     pf.setSyncTime(String.valueOf(String.valueOf(hour) + ":" + String.valueOf(minute)));
                     setAlarm(2, hour, minute, spinWeekly.getSelectedItemPosition());
                 }
             }
            pf.setNotification(spinNotifications.getSelectedItemPosition());
             // Set the theme based on what the user has selected
             pf.setTheme(spinTheme.getSelectedItemPosition());
             // If the theme has changed...
             if (themeChanged) {
                 // Ask the user if they want to restart
                 showDialog(RESTART_DIALOG);
             }
             Toast.makeText(this, "Settings saved", Toast.LENGTH_LONG).show();
         } catch (Exception e) {
             Toast.makeText(this, "There was an error saving your settings", Toast.LENGTH_LONG).show();
         }
     }
 
     /************
      *  PURPOSE: This handles setting our alarm to the proper time
      *  ARGUMENTS: int code, int iHour, int iMinute, int iDay
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void setAlarm(int code, int iHour, int iMinute, int iDay) {
         // Get the current calendar
         Calendar c = Calendar.getInstance();
         // Create a new intent based on the AlarmReceiver
         Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
         // Create our alarm class with a unique ID
         PendingIntent sender = PendingIntent.getBroadcast(this, 8416, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         // Create a manager for our alarm
         AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
         int interval = 0;
         // Get the current time
         //Date now = c.getTime();
         switch (code) {
             case 1:
                 // Set the time based on what the user selected
                 c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), iHour, iMinute);
                 // If the date the user selected has already passed...
                 if (c.getTime().before(Calendar.getInstance().getTime())) {
                     // Add a month to the date of the next alarm
                     c.add(Calendar.DAY_OF_MONTH, 1);
                 }
                 // Add milliseconds for a whole day
                 interval = 86400000;
                 break;
             case 2:
                 // Get the current day of the week
                 int cDay = c.get(Calendar.DAY_OF_WEEK) - 1;
                 // Get the difference between the selected day and our day
                 cDay -= iDay;
                 // Set the time based on our difference
                 c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), iHour, iMinute);
                 // Check to see if the date selected has already passed
                 if (c.get(Calendar.DAY_OF_WEEK) - 1 >= iDay && iHour > c.get(Calendar.HOUR_OF_DAY) && iMinute > c.get(Calendar.MINUTE)) {
                     c.add(Calendar.DAY_OF_MONTH, 7 - cDay);
                 } else {
                     c.add(Calendar.DAY_OF_MONTH, Math.abs(cDay));
                 }
                 // Add one week of time
                 interval = 604800000;
                 break;
         }
         // Set the alarm based on the date we set
         am.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), interval, sender);
     }
 
     /************
      *  PURPOSE: Allows us to cancel the alarm if the user set one previously
      *      but no longer wants to use one
      *  ARGUMENTS: None
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void cancelAlarm() {
         // Create an alarm intent
         Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
         // Create an alarm with the unique ID
         PendingIntent sender = PendingIntent.getBroadcast(this, 8416, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         // Create our alarm manager
         AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
         // Cancel the alarm
         am.cancel(sender);
     }
 
     /************
      *  PURPOSE: Changes the saved theme based on the users selection
      *    and current operating system
      *  ARGUMENTS: None
      *  RETURNS: VOID
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
         } else if (theme == 3) {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 setTheme(R.style.Theme_Sherlock_LightBlueWhite);
             } else {
                 setTheme(R.style.Theme_LightBlueWhite);
             }
 //        } else if (theme == 4) {
 //            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 //                setTheme(R.style.Theme_Sherlock_YellowBlack);
 //            } else {
 //                setTheme(R.style.Theme_YellowBlack);
 //            }
         } else {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 setTheme(R.style.Theme_Sherlock_BlueWhite);
             } else {
                 setTheme(R.style.Theme_BlueWhite);
             }
         }
     }
 }
