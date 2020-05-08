 package com.macdems.planactivator;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 
 
 /**
  * Class for setting back alarms on boot
  */
 public class AlarmSetter extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
         DatabaseAdapter dbHelper = new DatabaseAdapter(context);
         dbHelper.open();
         Cursor cursor = dbHelper.fetchActivePlans();
 
        if (cursor.getCount() > 0)
	        do {
	        	long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
	        	Long next = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_VALIDUNTIL));
	    		Long delay = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELAY));
	    		if (next != null) {
	    			if (delay != null) next += delay * 1000;
	    			ActivateEvent.setNewAlarm(context, rowId, next, delay);
	    		}
	        } while (cursor.moveToNext());
 	}
 
 }
