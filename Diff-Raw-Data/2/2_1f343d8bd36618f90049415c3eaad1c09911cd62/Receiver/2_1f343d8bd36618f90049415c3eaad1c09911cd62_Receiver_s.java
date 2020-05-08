 /*
  * Copyright (C) 2010 Chris Boyle
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package name.boyle.chris.timer;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 
 public class Receiver extends BroadcastReceiver
 {
 	protected static final String ACTION_ALARM = "name.boyle.chris.timer.ALARM";
 
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
 		String action = intent.getAction();
 		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
 			// Alarms don't survive reboots or upgrades, so restore them all
 			TimerDB db = new TimerDB(context);
 			db.open();
 			Cursor c = db.getAllEntries();
 			if (c.moveToFirst()) {
 				do {
 					Timer t = db.cursorToEntry(c);
 					if (! t.enabled) continue;
 					t.setNextAlarm(context);
 				} while (c.moveToNext());
 			}
 			c.close();
 			db.close();
 		} else if (action.equals(ACTION_ALARM)) {
 			// It's time to sound/show an alarm
 			final long id;
 			try {
				id = Long.parseLong(intent.getData().getAuthority());
 			} catch (NumberFormatException e) {
 				return;
 			}
 			// We ask TimerActivity to do this (rather than say we've done it)
 			// to avoid conflicting modifications in the case where a delayed
 			// save has already been queued
 			Intent i = new Intent(TimerActivity.ACTION_RESET);
 			i.putExtra(TimerDB.KEY_ID, id);
 			context.sendOrderedBroadcast(i, null, new BroadcastReceiver() {
 				@Override
 				public void onReceive(Context context, Intent intent) {
 					if (getResultCode() != Activity.RESULT_CANCELED) return;  // Activity caught it
 					TimerDB db = new TimerDB(context);
 					db.open();
 					Timer t = db.getEntry(id);
 					if (t.notify(context)) db.saveEntry(t);
 					db.close();
 				}
 			}, null, Activity.RESULT_CANCELED, null, null);
 		}
 	}
 }
