 /*
  *  Entomologist Mobile/Trac
  *  Copyright (C) 2011 Matt Barringer
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  Author: Matt Barringer <matt@entomologist-project.org>
  */
 package org.entomologistproject.trac;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.ExecutionException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 // This is the receiver that handles the alarm triggers
 public class UpdateBroadcastReceiver extends BroadcastReceiver {
 
 	static private class BugsUpdatedHandler extends Handler {
 		private Context context;
 		public BugsUpdatedHandler(Context context)
 		{
 			this.context = context;
 		}
 		@Override
 		public void handleMessage(Message msg)
 		{
 			Bundle b = msg.getData();
 			if (!b.getBoolean("error"))
 			{
 				int updateCount = b.getInt("bugs_updated");
 				if (updateCount > 0)
 				{
 					boolean inForeground = false;
 					
 					// We need to see if the activity is in the foreground,
 					// since it would be annoying to notify the user if they were actively using the
 					// application.
 					try {
 						inForeground = new ForegroundTask().execute(context).get();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 					// If it is in the foreground, we'll send a message to refresh the
 					// bug list.
 					if (inForeground)
 					{
 						sendMessage();
 					}
 					else
 					{
 						// It's not running, so notify the user
 						showNotification();
 					}
 				}
 			}
 		}
 		
 		public void sendMessage()
 		{
 			Intent i = new Intent(BugList.UPDATE_MESSAGE);  
 			context.sendBroadcast(i);
 		}
 		
 		public void showNotification()
 		{
 			//Log.v("Entomologist", "Showing notification");
 			Intent notifyIntent = new Intent(context, BugList.class);
 			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
 
 			Notification notification = new Notification(R.drawable.icon,
 														null, System.currentTimeMillis());
 			notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
 			//notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
 			//notification.ledARGB = 0x50a8c24e;
 			notification.ledARGB = Color.BLUE;
 			notification.ledOffMS = 1000;
 			notification.ledOnMS = 300;
 			// TODO show the update count
 			notification.setLatestEventInfo(context, "Bugs Updated", "You have updated bugs.", contentIntent);
 			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 			manager.notify(BugList.UPDATED_BUGS_NOTIFICATION_ID, notification);
 		}
 	} // End of the handler class
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		try
 		{
 			//Log.v("Entomologist", "Update trigger");
 	        SharedPreferences settings = context.getSharedPreferences(BugList.PREFS_NAME, 0);
 	    	boolean onlyOverWifi = settings.getBoolean("only_wifi", true);
 	    	if (!BugList.canConnect(context, onlyOverWifi))
 	    	{
 	    		return;
 	    	}
 	    	
 			DBHelper helper = new DBHelper(context);
 	        Cursor trackerCursor = helper.getTracker();
         	trackerCursor.moveToFirst();
         	long trackerRowId = trackerCursor.getLong(0);
         	String trackerUrl = trackerCursor.getString(1);
         	String trackerUsername = trackerCursor.getString(2);
         	String trackerPassword = trackerCursor.getString(3);
         	Date trackerLastSync;
         	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
         	try
         	{
         		//Log.v("Entomologist", "Time: " + trackerCursor.getString(4));
 				trackerLastSync = df.parse(trackerCursor.getString(4));
 			}
         	catch (ParseException e) {
 				e.printStackTrace();
 				trackerLastSync = new Date(1970, 1, 1, 12,30);
 			}
         	trackerCursor.close();
         	helper.close();
         	
 			// Now call the update task
     		TracBackend backend = new TracBugListTask(context,
 					  trackerRowId,
 					  trackerUrl,
 					  trackerUsername,
 					  trackerPassword,
 					  trackerLastSync);
     		backend.execute(new BugsUpdatedHandler(context));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 }
