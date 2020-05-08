 /*
  * Copyright 2012 Aleksi Niiranen 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.blogspot.fwfaill.lunchbuddy;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import android.accounts.Account;
 import android.app.Service;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SyncResult;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 public class CoursesSyncAdapterService extends Service {
 	
 	private static final String TAG = "CoursesSyncAdapterService";
 	private static SyncAdapterImpl sSyncAdapter = null;
 	private static ContentResolver mContentResolver = null;
 	
 	private static final String[] PROJECTION = new String[] {
 		LunchBuddy.Courses._ID,
 		LunchBuddy.Courses.COLUMN_NAME_TITLE_FI,
 		LunchBuddy.Courses.COLUMN_NAME_TITLE_EN,
 		LunchBuddy.Courses.COLUMN_NAME_PRICE,
 		LunchBuddy.Courses.COLUMN_NAME_PROPERTIES,
 		LunchBuddy.Courses.COLUMN_NAME_TIMESTAMP,
 		LunchBuddy.Courses.COLUMN_NAME_REF_TITLE
 	};
 	
 	public CoursesSyncAdapterService() {
 		super();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		IBinder ret = null;
 		ret = getSyncAdapter().getSyncAdapterBinder();
 		return ret;
 	}
 	
 	private SyncAdapterImpl getSyncAdapter() {
 		if (sSyncAdapter == null) {
 			sSyncAdapter = new SyncAdapterImpl(this);
 		}
 		return sSyncAdapter;
 	}
 
 	private static void performSync(Context context, Account account, Bundle extras, String authority, 
 			ContentProviderClient provider, SyncResult syncResult) {
 		mContentResolver = context.getContentResolver();
 		Log.i(TAG, "performSync: " + account.toString());
 		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/Helsinki"), new Locale("Finnish", "Finland"));
     	cal.set(Calendar.HOUR_OF_DAY, 0);
     	cal.set(Calendar.MINUTE, 0);
     	cal.set(Calendar.SECOND, 0);
     	cal.set(Calendar.MILLISECOND, 0);
     	
     	long timestamp = cal.getTimeInMillis() / 1000;
     	
     	String where = LunchBuddy.Courses.COLUMN_NAME_TIMESTAMP + "=" + timestamp
     			+ " and " + LunchBuddy.Courses.COLUMN_NAME_REF_TITLE + "= ?";
     	
     	String[] args = new String[] { ""+0 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+1 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+2 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+4 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+5 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+6 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+7 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+8 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+9 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+10 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+11 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	args = new String[] { ""+12 };
     	mContentResolver.query(LunchBuddy.Courses.SYNC_CONTENT_URI, PROJECTION, where, args, LunchBuddy.Courses.DEFAULT_SORT_ORDER);
     	
     	where = LunchBuddy.Courses.COLUMN_NAME_TIMESTAMP + " < " + timestamp;
     	mContentResolver.delete(LunchBuddy.Courses.SYNC_CONTENT_URI, where, null);
 	}
 
 	private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
 		
 		private Context mContext;
 		
 		public SyncAdapterImpl(Context context) {
 			super(context, true);
 			mContext = context;
 		}
 		
 		@Override
 		public void onPerformSync(Account account, Bundle extras, String authority,
 				ContentProviderClient provider, SyncResult syncResult) {
 			CoursesSyncAdapterService.performSync(mContext, account, extras, authority, provider, syncResult);
 		}
 	}
 }
