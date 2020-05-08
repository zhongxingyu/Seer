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
 
 import android.app.IntentService;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.*;
 import android.provider.CalendarContract;
 import android.util.Log;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.util.*;
 
 /************
  *  PURPOSE: This class handles our HTTP requests in the background
  *      so as not to lock up our primary thread and make the program
  *      seemed locked up
  *  AUTHOR: Devin Collins <agent14709@gmail.com>
  *************/
 public class CalendarHandler extends IntentService {
 
     // Public variables used throughout the application
     private String username;
     private String password;
     private int calID = -1;
     private String loginToken = null;
     private List<String[]> finalDays;
     private Messenger messenger;
 
     public CalendarHandler() {
         super("CalendarHandler");
     }
 
     /************
      *  PURPOSE: Used to send our updates back to the main thread
      *  ARGUMENTS: String status
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void updateStatus(String status) {
         Message msg = Message.obtain();
         Bundle data = new Bundle();
         data.putString("status", status);
         msg.setData(data);
         try {
             messenger.send(msg);
         } catch (Exception e) {
             // TODO: Error reporting?
         }
     }
 
     /************
      *  PURPOSE: Used to send our errors back to the main thread
      *  ARGUMENTS: String error
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void showError(String error) {
         Message msg = Message.obtain();
         Bundle data = new Bundle();
         data.putString("status", "ERROR");
         data.putString("error", error);
         msg.setData(data);
         try {
             messenger.send(msg);
         } catch (Exception e) {
             // TODO: Error reporting?
         }
     }
 
     /************
      *  PURPOSE: Handles the primary thread in the service
      *  ARGUMENTS: Intent intent
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     @Override
     protected void onHandleIntent(Intent intent) {
         // Get our stored data from the intent
         username = intent.getStringExtra("username");
         password = intent.getStringExtra("password");
         messenger = (Messenger) intent.getExtras().get("handler");
         calID = intent.getIntExtra("calendarID", -1);
 
         // Create variables to be used through the application
         List<String[]> workDays = null;
         ConnectionManager conn = ConnectionManager.newConnection();
 
 
         /************
          * Once we verify that we have a valid token, we get the actual schedule
          *************/
         updateStatus("Logging in...");
         String tempToken = conn.getData("https://mytlc.bestbuy.com");
         if (tempToken != null) {
             loginToken = parseToken(tempToken);
         } else {
             String error = parseError(tempToken);
             if (error != null) {
                 showError(error);
             } else {
                 showError("Error retrieving your login token, make sure you have a valid network connection");
             }
             return;
         }
         String postResults = null;
         // This creates our login information
         List<NameValuePair> parameters = createParams();
         if (loginToken != null) {
             // Here we send the information to the server and login
             postResults = conn.postData("https://mytlc.bestbuy.com/etm/login.jsp", parameters);
         } else {
             String error = parseError(postResults);
             if (error != null) {
                 showError(error);
             } else {
                 showError("Error retrieving your login token, make sure you have a valid network connection");
             }
             return;
         }
         // If we logged in properly, then we download the schedule
         if (postResults != null && postResults.contains("etmMenu.jsp")) {
             // Here is the actual call for the schedule
             updateStatus("Retrieving schedule...");
             postResults = conn.getData("https://mytlc.bestbuy.com/etm/time/timesheet/etmTnsMonth.jsp");
         } else {
             String error = parseError(postResults);
             if (error != null) {
                 showError(error);
             } else {
                 showError("Error logging in, please verify your username and password");
             }
             return;
         }
         // If we successfully got the information, then parse out the schedule to read it properly
         String secToken = null;
         if (postResults != null) {
             updateStatus("Parsing schedule...");
             workDays = parseSchedule(postResults);
             secToken = parseSecureToken(postResults);
         } else {
             String error = parseError(postResults);
             if (error != null) {
                 showError(error);
             } else {
                 showError("Could not obtain user schedule, make sure you have a valid network connection");
             }
             return;
         }
         if (secToken != null) {
             parameters = createSecondParams(secToken);
             postResults = conn.postData("https://mytlc.bestbuy.com/etm/time/timesheet/etmTnsMonth.jsp", parameters);
         } else {
             String error = parseError(postResults);
             if (error != null) {
                 showError(error);
             } else {
                 showError("Error retrieving your login token, make sure you have a valid network connection");
             }
             return;
         }
         List<String[]> secondMonth = null;
         if (postResults != null) {
             secondMonth = parseSchedule(postResults);
         } else {
             String error = parseError(postResults);
             if (error != null) {
                 showError(error);
             } else {
                 showError("Could not obtain user schedule, make sure you have a valid network connection");
             }
             return;
         }
         if (secondMonth != null) {
             if (workDays == null) {
                 workDays = secondMonth;
             } else {
                 workDays.addAll(secondMonth);
             }
             finalDays = workDays;
         } else {
             String error = parseError(postResults);
             if (error != null) {
                 showError(error);
             } else {
                 showError("There was an error retrieving your schedule, please try again");
             }
             return;
         }
         // Add our shifts to the calendar
         updateStatus("Adding shifts to calendar...");
         if (finalDays != null && addDays()) {
             // Report back that we're successful!
             Message msg = Message.obtain();
             Bundle b = new Bundle();
             b.putString("status", "DONE");
             b.putInt("count", workDays.size());
             msg.setData(b);
             try {
                 messenger.send(msg);
             } catch (Exception e) {
                 // Nothing
             }
         } else {
             showError("Couldn't add your shifts to your calendar, please try again");
             return;
         }
     }
 
     /************
      *   PURPOSE: Attempts to parse out the error message from MyTLC
      *   ARGUMENTS: String data
      *   RETURNS: String
      *   AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private String parseError(String data) {
         String result = null;
         try {
             Log.e("ERRORLOG", data);
             if (!data.contains("<b>System Error</b>") && !data.contains("Message:")) {
                 return result;
             }
             result = data.substring(data.indexOf("Message:") + 8);
             result = result.substring(0, result.indexOf("<br>"));
             result = "MyTLC Error: " + result.trim();
         } catch (Exception e) {
             return null;
         }
         return result;
     }
 
     /************
      *   PURPOSE: Parses out the initial string to retrieve the connection
      *       token required by the website
      *   ARGUMENTS: String token
      *   RETURNS: String
      *   AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private String parseToken(String token) {
         String tempToken;
         try {
             tempToken = token.substring(token.indexOf("End Hotkey for submit"));
             tempToken = tempToken.substring(tempToken.indexOf("hidden") + 14, tempToken.indexOf("url_login_token") - 7);
             return tempToken;
         } catch (Exception e) {
             return null;
         }
 
     }
 
     /************
      *   PURPOSE: Parses out the a secure token used for HTTPS browsing
      *   ARGUMENTS: String token
      *   RETURNS: String
      *   AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private String parseSecureToken(String token) {
         String tempToken;
         try {
             tempToken = token.substring(token.indexOf("secureToken") + 20, token.indexOf("'/>"));
             return tempToken;
         } catch (Exception e) {
             return null;
         }
     }
 
     private List<NameValuePair> createAPIinfo() {
         List<NameValuePair> params = new LinkedList<NameValuePair>();
         try {
 //            params.add(new BasicNameValuePair("user_id", username));
             params.add(new BasicNameValuePair("device_type", "Android"));
             return params;
         } catch (Exception e) {
             return null;
         }
     }
 
     /************
      *   PURPOSE: Creates the list of paramters for our initial POST request
      *   ARGUMENTS: NULL
      *   RETURNS: List<NameValuePair>
      *   AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private List<NameValuePair> createParams() {
         List<NameValuePair> params = new LinkedList<NameValuePair>();
         try {
             params.add(new BasicNameValuePair("pageAction", "login"));
             params.add(new BasicNameValuePair("url_login_token", loginToken));
             params.add(new BasicNameValuePair("login", username));
             params.add(new BasicNameValuePair("password", password));
             params.add(new BasicNameValuePair("client", "DEFAULT"));
             params.add(new BasicNameValuePair("localeSelected", "false"));
             params.add(new BasicNameValuePair("STATUS_MESSAGE_HIDDEN", ""));
             params.add(new BasicNameValuePair("wbXpos", "0"));
             params.add(new BasicNameValuePair("wbYpos", "0"));
             return params;
         } catch (Exception e) {
             return null;
         }
     }
 
     /************
      *   PURPOSE: Creates the list of paramters for our second POST request
      *   ARGUMENTS: String
      *   RETURNS: List<NameValuePair>
      *   AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private List<NameValuePair> createSecondParams(String secToken) {
         List<NameValuePair> params = new LinkedList<NameValuePair>();
         try {
             Calendar c = Calendar.getInstance();
             String month = String.valueOf(c.get(Calendar.MONTH) + 2);
             String year = String.valueOf(c.get(Calendar.YEAR));
             if (month.equals("13")) {
                 month = "01";
                 year = String.valueOf(Integer.parseInt(year) + 1);
             } else if (Integer.parseInt(month) < 10) {
                 month = "0" + month;
             }
             params.add(new BasicNameValuePair("pageAction", ""));
             params.add(new BasicNameValuePair("NEW_MONTH_YEAR", month + "/" + year));
             params.add(new BasicNameValuePair("secureToken", secToken));
             params.add(new BasicNameValuePair("selectedTocID", "11"));
             params.add(new BasicNameValuePair("parentID", "10"));
             params.add(new BasicNameValuePair("homePageButtonWasSelected", "false"));
             params.add(new BasicNameValuePair("bid1_action", ""));
             params.add(new BasicNameValuePair("bid1_current_row", "0"));
             params.add(new BasicNameValuePair("STATUS_MESSAGE_HIDDEN", ""));
             params.add(new BasicNameValuePair("wbXpos", "0"));
             params.add(new BasicNameValuePair("wbYpos", "0"));
             return params;
         } catch (Exception e) {
             return null;
         }
     }
 
     /************
      *  PURPOSE: This parses out the users schedule and returns it
      *      in a redable format
      *  ARGUMENTS: List<String[]>
      *  RETURNS: String
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private List<String[]> parseSchedule(String data) {
         List<String[]> workingDays = new ArrayList<String[]>();
         try {
             String tempSchedule = data.substring(data.indexOf("calWeekDayHeader"));
             tempSchedule = tempSchedule.substring(0, tempSchedule.indexOf("document.forms[0].NEW_MONTH_YEAR"));
             String[] schedules = tempSchedule.split("</tr>");
             for (int x = 0; x < schedules.length - 1; x++) {
                 if (!schedules[x].contains("OFF")) {
                     if (schedules[x].contains("calendarCellRegularCurrent") || schedules[x].contains("calendarCellRegularFuture")) {
                         String date;
                         if (schedules[x].contains("calendarCellRegularCurrent")) {
                             date = schedules[x].substring(schedules[x].indexOf("calendarDateCurrent") + 23, schedules[x].indexOf("</span>"));
                         } else {
                             date = schedules[x].substring(schedules[x].indexOf("calendarDateNormal") + 22, schedules[x].indexOf("</span>"));
                         }
                         String shifts[] = schedules[x].split("<br>");
                         for (int i = 0; i < shifts.length - 1; i++) {
                             if (shifts[i].contains("AM") && !shifts[i].contains("<td>") || shifts[i].contains("PM") && !shifts[i].contains("<td>")) {
                                 String dept = "";
                                 if (i != shifts.length - 1) {
                                     dept = (shifts[i+1].startsWith("L-")) ? shifts[i + 1] : "";
                                 }
                                 workingDays.add(new String[]{date, shifts[i], dept});
                             }
                         }
 
                     }
                 }
             }
             return workingDays;
         } catch (Exception e) {
             return null;
         }
     }
 
     /************
      *  PURPOSE: Adds the parsed out events to the calendar
      *  ARGUMENTS: NULL
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>, Bobby Ore <bob1987@gmail.com>
      *************/
     private boolean addDays() {
         try {
             deleteOldEvents();
             Preferences pf = new Preferences(this);
             // Get our stored notification time
             int notification = pf.getNotification();
             // Convert the stored time into minutes
             switch (notification) {
                 case 0:
                 {
                     notification = 0;
                     break;
                 }
                 case 1:
                 {
                     notification = 5;
                     break;
                 }
                 case 2:
                 {
                     notification = 15;
                     break;
                 }
                 case 3:
                 {
                     notification = 30;
                     break;
                 }
                 case 4:
                 {
                     notification = 60;
                     break;
                 }
                 case 5:
                 {
                     notification = 120;
                     break;
                 }
                 case 6:
                 {
                     notification = 180;
                     break;
                 }
             }
             for (String[] work : finalDays) {
                 Calendar beginTime = Calendar.getInstance();
                 /************
                  * Below we create variables for our day, month and year.  We then
                  * check to see if the scheduled day is next month or next year so
                  * we know to increase our variables properly
                  *************/
                 int workDay = Integer.parseInt(work[0]);
                 int workMonth = beginTime.get(Calendar.MONTH);
                 int workYear = beginTime.get(Calendar.YEAR);
                 /************
                  * If the day of the month for work is prior to todays date, then
                  * we check if we're in December.  We increase the month and year
                  * where necessary
                  *************/
                 if (workDay < beginTime.get(Calendar.DAY_OF_MONTH) && workMonth != 12) {
                     workMonth += 1;
                 } else if (workDay < beginTime.get(Calendar.DAY_OF_MONTH)) {
                     workMonth = 1;
                     workYear += 1;
                 }
                 /************
                  * If the shift starts in the PM, add 12 hours
                  * to the time
                  *************/
                 int workSHour = Integer.parseInt(work[1].substring(0, 2));
                 if (work[1].substring(6, 8).equalsIgnoreCase("PM") && workSHour != 12) {
                     workSHour += 12;
                 }
                 int workSMinute = Integer.parseInt(work[1].substring(3, 5));
                 beginTime.set(workYear, workMonth, workDay, workSHour, workSMinute);
                 int workEHour = Integer.parseInt(work[1].substring(11, 13));
                 /************
                  * If the shift ends in the PM, add 12 hours
                  * to the time
                  *************/
                 if (work[1].substring(17, 19).equalsIgnoreCase("PM") && workEHour != 12) {
                     workEHour += 12;
                 }
                 int workEMinute = Integer.parseInt(work[1].substring(14, 16));
                 Calendar endTime = Calendar.getInstance();
                if (workEHour < workSHour) {
                     workDay += 1;
                 }
                 endTime.set(workYear, workMonth, workDay, workEHour, workEMinute);
 
                 /************
                  * ContentResolver and ContentValues are what we use to add
                  * our calendar events to the device
                  *************/
                 ContentResolver cr = this.getContentResolver();
                 ContentValues cv = new ContentValues();
                 TimeZone timeZone = TimeZone.getDefault();
 
                 /************
                  * Here we create our calendar event based on the version code
                  *************/
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                     cv.put(CalendarContract.Events.CALENDAR_ID, calID);
                     cv.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
                     cv.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
                     cv.put(CalendarContract.Events.EVENT_LOCATION, work[2]);
                     cv.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
                     cv.put(CalendarContract.Events.HAS_ALARM, (notification == 0) ? 0 : 1);
                     cv.put(CalendarContract.Events.TITLE, "Work@BestBuy");
                 } else {
                     cv.put("calendar_id", calID);
                     cv.put("dtend", endTime.getTimeInMillis());
                     cv.put("dtstart", beginTime.getTimeInMillis());
                     cv.put("eventLocation", work[2]);
                     cv.put("title", "Work@BestBuy");
                     cv.put("hasAlarm", (notification == 0) ? 0 : 1);
                 }
 
                 /************
                  * Add our events to the calendar based on the Uri we get
                  *************/
                 Uri uri = cr.insert(getEventsUri(), cv);
 
                 /************
                  * If we retrieved a Uri for the event, try to add the reminder
                  *************/
                 if (uri != null) {
                     // Get the ID of the calendar event
                     long eventID = Long.parseLong(uri.getLastPathSegment());
 
                     /************
                      * Build our reminder based on version code
                      *************/
                     if (notification != 0) {
                         ContentValues reminders = new ContentValues();
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                             reminders.put(CalendarContract.Reminders.EVENT_ID, eventID);
                             reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                             reminders.put(CalendarContract.Reminders.MINUTES, notification);
                         } else {
                             reminders.put("event_id", eventID);
                             reminders.put("method", 1);
                             reminders.put("minutes", notification);
                         }
                         // Add the reminder to the system
                         cr.insert(getRemindersUri(), reminders);
                     }
                 }
             }
         } catch (Exception e) {
             showError("Could not create calendar events on calendar, please make sure you have a calendar application on your device");
             return false;
         }
         return true;
     }
 
     /************
      *  PURPOSE: Deletes all current and future Work@BestBuy events from the calendar
      *  ARGUMENTS: NULL
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void deleteOldEvents() {
         Calendar c = Calendar.getInstance();
         ContentResolver cr = this.getContentResolver();
         c.set(Calendar.HOUR, 0);
         cr.delete(getEventsUri(), "CALENDAR_ID = " + calID + " AND TITLE = 'Work@BestBuy' AND DTEND >= " + c.getTimeInMillis(), null);
     }
 
     /************
      *  PURPOSE: Gets the Uri for events based on Android version number
      *  ARGUMENTS: NULL
      *  RETURNS: Uri
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private Uri getEventsUri() {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
             return Uri.parse("content://calendar/events/");
         } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             return Uri.parse("content://com.android.calendar/events/");
         } else {
             return CalendarContract.Events.CONTENT_URI;
         }
     }
 
     /************
      *  PURPOSE: Gets the Uri for reminders based on Android version number
      *  ARGUMENTS: NULL
      *  RETURNS: Uri
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private Uri getRemindersUri() {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
             return Uri.parse("content://calendar/reminders");
         } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             return Uri.parse("content://com.android.calendar/reminders");
         } else {
             return CalendarContract.Reminders.CONTENT_URI;
         }
     }
 }
