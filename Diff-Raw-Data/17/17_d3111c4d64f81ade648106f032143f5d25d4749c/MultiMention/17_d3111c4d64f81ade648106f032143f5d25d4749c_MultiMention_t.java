 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.service;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.Preferences;
 import org.mustard.android.R;
 import org.mustard.android.activity.MustardMention;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.util.MustardException;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class MultiMention extends Service {
 
 	private static final String TAG = "MentionService";
 
 	private SharedPreferences mPreferences;
 
 	private NotificationManager mNotificationManager;
 
 	private WakeLock mWakeLock;
 
 	private MustardDbAdapter mDbHelper;
 //	private StatusNet mStatusNet = null;
 
 	private boolean mMerged=false;
 	private HashMap<String, Integer> mAccountsRetrieved;
 //	private long mAccountId;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onCreate");
 
 		mDbHelper = new MustardDbAdapter(this);
 		mDbHelper.open();
 		
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		mMerged = mPreferences.getBoolean(Preferences.CHECK_MERGED_TL_KEY, false);
 		
 		if (!mPreferences.getBoolean(Preferences.CHECK_UPDATES_KEY, false)) {
 			if(MustardApplication.DEBUG) Log.i(TAG, "Check update preference is false.");
 			stopSelf();
 			return;
 		} else {
 			if(MustardApplication.DEBUG) Log.i(TAG, "Check update preference is true.");
 		}
 		
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
 		mWakeLock.acquire();
 		
 		schedule(MultiMention.this);
 
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		new RetrieveTask().execute();
 		
 //		stopSelf();
 	}
 
 	private void processNewNotices() {
 
 		if (mAccountsRetrieved.size()< 1) {
 			return;
 		}
 		
 		String title = getString(R.string.new_notices_updates);
 		String text = getString(R.string.x_new_mention);
 
 //		Intent i = new Intent("android.intent.action.VIEW",Uri.parse("statusnet://mentions/"));
 		Intent i = null;
 		
 		if (mMerged) {
 			i=MustardMention.getActionHandleTimeline(this, "-1");
 			i.putExtra(MustardMention.MERGED, true);
 			if(mAccountsRetrieved.size()==1) {
 				String account = mAccountsRetrieved.keySet().iterator().next() ;
 				if(mAccountsRetrieved.get(account) > 1)
 					text=getString(R.string.x_multiple_account_mentions,account,""+mAccountsRetrieved.get(account));
 				else
 					text=getString(R.string.x_multiple_account_mention,account);
 					
 //				text=account + " has " + mAccountsRetrieved.get(account) + " new mention" + (mAccountsRetrieved.get(account) > 1 ? "s" : "");
 			} else {
 				Iterator<String> accounts = mAccountsRetrieved.keySet().iterator();
 				int totmentions=0;
 				while(accounts.hasNext()) {
 					String account = accounts.next();
 					totmentions+=mAccountsRetrieved.get(account);
 				}
 				text = getString(R.string.x_multiple_accounts_mentions,""+totmentions);
 //				text = "Your accounts have " + totmentions + " new mentions";
 			}
 		} else {
 			String account = mAccountsRetrieved.keySet().iterator().next() ;
 			if(mAccountsRetrieved.get(account) > 1)
 				text=getString(R.string.x_single_account_mentions,""+mAccountsRetrieved.get(account));
 			else
 				text=getString(R.string.x_single_account_mention);
 			i=MustardMention.getActionHandleTimeline(this);
 		}
 		i.putExtra("FROMSERVICE", true);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		PendingIntent intent = PendingIntent.getActivity(this, 0, i, 0);
 
 		Notification notification = new Notification(R.drawable.icon, getString(R.string.x_new_mention),
 				System.currentTimeMillis());
 
 		notification.setLatestEventInfo(this, title, text, intent);
 
 		notification.flags = Notification.FLAG_AUTO_CANCEL
 		| Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS;
 
 		notification.ledARGB = Color.YELLOW;
 		notification.ledOnMS = 700;
 		notification.ledOffMS = 5000;
 
 		// notification.defaults = Notification.DEFAULT_LIGHTS;
 
 	    String ringtoneUri = mPreferences.getString(Preferences.RINGTONE_KEY, null);
 
 	    if (ringtoneUri == null) {
 	      notification.defaults |= Notification.DEFAULT_SOUND;
 	    } else {
 	      notification.sound = Uri.parse(ringtoneUri);
 	    }
 
 	    if (mPreferences.getBoolean(Preferences.VIBRATE_KEY, false)) {
 	      notification.defaults |= Notification.DEFAULT_VIBRATE;
 	    }
 	    
 		mNotificationManager.notify(0, notification);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if(MustardApplication.DEBUG) Log.i(TAG, "onDestroy");
 		if(mWakeLock!=null)
 			mWakeLock.release();
 		if(mDbHelper != null) {
 			try {
 				mDbHelper.close();
 			} catch (Exception e) {
 				if (MustardApplication.DEBUG) e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void schedule(Context context) {
 		SharedPreferences preferences = PreferenceManager
 		.getDefaultSharedPreferences(context);
 		String intervalPref = preferences.getString(
 				Preferences.CHECK_UPDATE_INTERVAL_KEY, context
 				.getString(R.string.pref_check_updates_interval_default));
 		int interval = Integer.parseInt(intervalPref);
 		schedule(context,interval);
 	}
 	
 	public static void schedule(Context context,int interval) {
 		
 		Intent intent = new Intent(context, MultiMention.class);
 		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
 		Calendar c = new GregorianCalendar();
 		c.add(Calendar.MINUTE, interval);
 
 		DateFormat df = new SimpleDateFormat("h:mm a");
 		if (MustardApplication.DEBUG) Log.i(TAG, "Scheduling alarm at " + df.format(c.getTime()));
 
 		AlarmManager alarm = (AlarmManager) context
 		.getSystemService(Context.ALARM_SERVICE);
 		alarm.cancel(pending);
 		alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
 	}
 
 	public static void unschedule(Context context) {
 		Intent intent = new Intent(context, MultiMention.class);
 		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
 		AlarmManager alarm = (AlarmManager) context
 		.getSystemService(Context.ALARM_SERVICE);
 		if (MustardApplication.DEBUG) Log.i(TAG, "Cancelling alarms.");
 		alarm.cancel(pending);
 	}
 
 	private enum RetrieveResult {
 		OK, EMPTY, IO_ERROR, AUTH_ERROR, CANCELLED
 	}
 
 	private class RetrieveTask extends  AsyncTask<Void, Void, RetrieveResult> {
 
 		private int DB_ROW_TYPE=MustardDbAdapter.ROWTYPE_MENTION;
 		private String DB_ROW_EXTRA="MENTION";
 
 		@Override
 		public RetrieveResult doInBackground(Void... params) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "service start");
 			mAccountsRetrieved = new HashMap<String, Integer>();
 			MustardApplication _ma = (MustardApplication) getApplication();
 			if(mMerged) {
 				DB_ROW_EXTRA="-1";
 				Cursor c = mDbHelper.fetchAllAccountsToMerge();
 				while(c.moveToNext()) {
 					long _aid = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 					StatusNet _sn = _ma.checkAccount(mDbHelper,false,_aid);
 					if (_sn==null)
 						continue;
 					processStatusNet(_sn);
 				}
 				c.close();
 			} else {
 				StatusNet _sn = _ma.checkAccount(mDbHelper);
 				if (_sn==null)
 					return RetrieveResult.EMPTY;
 				DB_ROW_EXTRA=_sn.getMUsername();
 				processStatusNet(_sn);
 			}
 			
 			return RetrieveResult.OK;
 		}
 
 		private void processStatusNet(StatusNet _sn) {
 			ArrayList<org.mustard.statusnet.Status> al = null;
 			long maxId = mDbHelper.fetchMaxStatusesId(_sn.getUserId(), DB_ROW_TYPE,DB_ROW_EXTRA);
 			if (maxId < 1) {
 				try {
 					maxId = mDbHelper.getUserMentionMaxId(_sn.getUserId());
 				} catch (Exception e ) {
 					if (MustardApplication.DEBUG) e.printStackTrace();
 					Log.e(TAG, e.getMessage());
 					maxId=0;
 				}
 			}
 			try {
 				al=_sn.get(DB_ROW_TYPE,_sn.getMUsername(),maxId,true);
 			} catch (MustardException e) {
 				Log.e(TAG,e.toString());
 			}
 			if(al==null || al.size()< 1) {
 				return;
 			} else {
				int validNotice = 0;
 				for (org.mustard.statusnet.Status status : al) {
					long _id = status.getNotice().getId();
					Log.d(TAG, "Mention: max is " + maxId + " I found " + _id);
					if (_id>maxId) {
 						try {
 							mDbHelper.createStatus(_sn.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,status);
							validNotice++;
 						} catch (MustardException e) {
 							
 						}
 					}
 				}
 				// mDbHelper.createStatuses(_sn.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,al);
				if(validNotice>0) {
					mAccountsRetrieved.put(_sn.getMUsername() + "@" + _sn.getURL().getHost(), validNotice);
					maxId = mDbHelper.fetchMaxStatusesId(_sn.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA);
					mDbHelper.setUserMentionMaxId(_sn.getUserId(), maxId);
				}
 			}
 		}
 		
 		@Override
 		public void onPostExecute(RetrieveResult result) {
 			if (result == RetrieveResult.OK) {
 				processNewNotices();
 			}
 			stopSelf();
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
