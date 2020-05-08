 package com.bitty.notifyme;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
import android.widget.Toast;
 
 public class OnBootReceiver extends BroadcastReceiver
 {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// get all notifications from the database and use their data to re-set alarms!
 		//TODO May need to do this when deleting items since DB is reset
 		
 		NotifyDBAdapter db = new NotifyDBAdapter(context);
 		
 		Cursor cursor = db.getAllNotifications();
 		
 		ReminderManager reminderMngr = new ReminderManager(context);
 		
 		if (cursor.moveToFirst())
 		{
 			do{
 				int hour = cursor.getInt(cursor.getColumnIndex(NotifyDBAdapter.KEY_NOTIFY_HOUR));
 				int minutes = cursor.getInt(cursor.getColumnIndex(NotifyDBAdapter.KEY_NOTIFY_MINUTES));
 				int day = cursor.getInt(cursor.getColumnIndex(NotifyDBAdapter.KEY_NOTIFY_DAY));
 				long db_ID = cursor.getLong(cursor.getColumnIndex(NotifyDBAdapter.KEY_ID));
 
 				reminderMngr.setReminder(hour, minutes, day, db_ID);
 			}while(cursor.moveToNext());
 		}
 		
 		//Toast.makeText(context, "The Boot Receiver was received!", Toast.LENGTH_LONG).show();
 	}
 }
