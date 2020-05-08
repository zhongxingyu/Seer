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
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 
 /************
  *  PURPOSE: This class handles the notifications and
  *      sync timers used to automatically download the
  *      users schedule at set intervals
  *  AUTHOR: Devin Collins <agent14709@gmail.com>
  *************/
 public class AlarmReceiver extends BroadcastReceiver {
 
     private Context cContext;
     private Preferences pf;
 
     /************
      *  PURPOSE: This is what happens when the alarm signal is sent
      *  ARGUMENTS: Context, Intent
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     @Override
     public void onReceive(Context context, Intent i) {
         try {
             cContext = context;
             pf = new Preferences(cContext);
 
             String[] creds = getSettings();
 
             // This is our background service.  We create it and assign
             // variables that we'll be using.
             Intent intent = new Intent(context, CalendarHandler.class);
             Messenger messenger = new Messenger(handler);
             intent.putExtra("handler", messenger);
             intent.putExtra("calendarID", Integer.parseInt(creds[0]));
             intent.putExtra("username", creds[1]);
             intent.putExtra("password", creds[2]);
 
             // Start the service
             context.startService(intent);
         } catch (Exception e) {
             // TODO: Error reporting?
         }
     }
 
     /************
      *  PURPOSE: Gets our saved preferences
      *  ARGUMENTS: null
      *  RETURNS: String[]
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private String[] getSettings() {
         String info[] = new String[3];
         info[0] = String.valueOf(pf.getCalendarID());
         info[1] = pf.getUsername();
         info[2] = pf.getPassword();
         return info;
     }
 
     /************
      *  PURPOSE: This is what listens to our background service
      *      as it goes through each process.  It then reports
      *      the results based on a series of if statements
      *  ARGUMENTS: NULL
      *  RETURNS: HANDLER
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private Handler handler = new Handler() {
         // This intercepts all messages sent from our background service
         public void handleMessage(Message message) {
             // We read the information from the message and do something with it
             // based on what the result code is
             String result = message.getData().getString("status");
             if (result.equals("DONE")) {
                 displayNotification("Added " + message.getData().getInt("count", 0) + " shifts to schedule");
             } else if (result.equals("ERROR")) {
                 displayNotification("ERROR" + message.getData().getString("error"));
             }
             // This disperses the message and let's the program know it can let go of it
             super.handleMessage(message);
         }
     };
 
     /************
      *  PURPOSE: This handles the notifications for when we download the schedule in the background
      *  ARGUMENTS: String
      *  RETURNS: VOID
      *  AUTHOR: Devin Collins <agent14709@gmail.com>
      *************/
     private void displayNotification(String msg) {
         // Create the notifcation manager that assigns our manager
         NotificationManager notMan = (NotificationManager) cContext.getSystemService(Context.NOTIFICATION_SERVICE);
         // Create the notification with our icon and the message we received
         Notification not = new Notification(R.drawable.icon, msg, System.currentTimeMillis());
         if (msg.startsWith("ERROR")) {
             msg = msg.replace("ERROR", "");
             Intent intent = new Intent(cContext, MyTlc.class);
            PendingIntent contentIntent = PendingIntent.getActivity(cContext, 8416, intent, 0);
            not.setLatestEventInfo(cContext, "MyTLC", msg, contentIntent);
         } else {
            // Assign our information to the notification
            not.setLatestEventInfo(cContext, "MyTLC", msg, null);
         }
         // Show the notification
         notMan.notify(0, not);
     }
 
 }
