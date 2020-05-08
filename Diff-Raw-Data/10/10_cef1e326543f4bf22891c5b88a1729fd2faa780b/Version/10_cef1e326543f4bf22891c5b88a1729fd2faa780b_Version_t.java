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
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.Preferences;
 import org.mustard.android.provider.OAuthInstance;
 import org.mustard.android.provider.OAuthLoader;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.statusnet.StatusNetService;
 import org.mustard.util.MustardUtil;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class Version extends Service {
 
 	private static final String TAG = "VersionService";
 	private static final String FORCE_EXECUTION="forceExecution";
 	
 	private SharedPreferences mPreferences;
 
 	private MustardDbAdapter mDbHelper;
 	private StatusNet mStatusNet = null;
 
 	private boolean mSendSnapshot = false;
 
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		
 		super.onStart(intent, startId);
 		Log.i(TAG, "onStart");
 		mDbHelper = new MustardDbAdapter(this);
 		mDbHelper.open();
 		MustardApplication _ma = (MustardApplication) getApplication();
 		mStatusNet = _ma.checkAccount(mDbHelper);
 
 		
 		if (mStatusNet == null) {
 			Log.i(TAG, "Not logged in.");
 			stopSelf();
 			return;
 		}
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		if (mPreferences.getBoolean(Preferences.SEND_SNAPSHOT_KEY, false)) {
 			mSendSnapshot=true;
 		}
 		boolean forceExecution = false;
		if(intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null && extras.containsKey(FORCE_EXECUTION)) {
				forceExecution=extras.getBoolean(FORCE_EXECUTION);
			}
 		}
 		long lastVersionCheck = mPreferences.getLong(Preferences.LAST_VERSION_CHECK, 0);
 		Date now = new Date();
 		if (now.getTime()-lastVersionCheck > 86400000 || forceExecution) {
 			new VersionTask().execute();
 			mPreferences.edit().putLong(Preferences.LAST_VERSION_CHECK, now.getTime()).commit();
 		} else {
 			stopSelf();
 		}
 	}
 
 	public static void schedule(Context context) {
 		Intent checkVersion = new Intent(context,Version.class);
 		context.startService(checkVersion);
 	}
 
 	public static void schedule(Context context, boolean forceExecution) {
 		Intent checkVersion = new Intent(context,Version.class);
 		checkVersion.putExtra(FORCE_EXECUTION, true);
 		context.startService(checkVersion);
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if(MustardApplication.DEBUG) Log.i(TAG, "onDestroy");
 		if(mDbHelper != null) {
 			try {
 				mDbHelper.close();
 			} catch (Exception e) {
 				if (MustardApplication.DEBUG) e.printStackTrace();
 			}
 		}
 		
 	}
 
 	private void onEndVersion() {
 		stopSelf();
 	}
 	private enum RetrieveResult {
 		OK, EMPTY, IO_ERROR, AUTH_ERROR, CANCELLED
 	}
 
 	private class VersionTask extends  AsyncTask<Void, Void, RetrieveResult> {
 
 		@Override
 		public RetrieveResult doInBackground(Void... params) {
 			
 			Cursor c = null;
 			try {
 				c = mDbHelper.fetchAllAccounts();
 
 				String mid = mDbHelper.getMustardId() ;
 				if ("".equals(mid)) {
 					mid=mDbHelper.creareMustardId();
 				}
 				if(mSendSnapshot) {
 					try {
 						MustardUtil.snapshot(mid, MustardApplication.sVersionName, String.valueOf(c.getCount()));
 					} catch (Exception e) {
 						if (MustardApplication.DEBUG) Log.e(getClass().getSimpleName()," error sending snapshot.. ");
 					}
 				}
 				ArrayList<String> alInstances = new ArrayList<String>(c.getCount());
 				while(c.moveToNext()) {
 					StatusNet sn = new StatusNet(getBaseContext());
 					String instance = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_INSTANCE));
 					if(alInstances.contains(instance))
 						continue;
 					// Skip twitter version
 					if(instance.indexOf("twitter.com")>0)
 						continue;
 					alInstances.add(instance);
 					//long _id = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_ROWID));
 					if (instance!=null && !"".equals(instance)) {
 						int userIndex = c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER);
 		                int passwordIndex = c.getColumnIndexOrThrow(MustardDbAdapter.KEY_PASSWORD);
 		                int tokenIndex = c.getColumnIndexOrThrow(MustardDbAdapter.KEY_TOKEN);
 		                int tokenSecretIndex = c.getColumnIndexOrThrow(MustardDbAdapter.KEY_TOKEN_SECRET);
 		    			String user=c.getString(userIndex);
 		    			String password=c.getString(passwordIndex);
 		    			String token=c.getString(tokenIndex);
 		    			String tokenSecret=c.getString(tokenSecretIndex);
 		    			sn.setURL(new URL(instance));
 		    			if (token==null || tokenSecret == null || "".equals(token) || "".equals(tokenSecret))
 		    				sn.setCredentials(user,password);
 		    			else {
 		    				
 		    				String instanceNoPrefix = instance.startsWith("https") ? instance.substring(8) : instance.substring(7);
 		    				
 		    				OAuthLoader om = new OAuthLoader(mDbHelper) ;
 		    			    OAuthInstance oi =  om.get(instanceNoPrefix);
 		    			    
 		    			    CommonsHttpOAuthConsumer consumer =  new CommonsHttpOAuthConsumer (
 		    			    		oi.key,
 		    			            oi.secret);
 		    				
 		    				consumer.setTokenWithSecret( token, tokenSecret);
 		    				sn.setCredentials(consumer, user);
 		    			}
 
 						String version = sn.getVersion();
 						if(version.startsWith("\""))
 							version=version.replaceAll("\"", "");
 						mDbHelper.setVersionInstance(instance, version);
 						
 						StatusNetService configuration = sn.getConfiguration();
 						mDbHelper.setTextlimitInstance(instance, configuration.site.textlimit);
 						 
 					}
 				}
 			} catch (Exception e) {
 				if (MustardApplication.DEBUG) Log.e(getClass().getSimpleName()," error updating version.. " +e.toString());
 			} finally {
 				if (c != null) {
 					try {
 						c.close();
 						mDbHelper.close();
 					} catch (Exception e) {}
 				}
 			}
 			return RetrieveResult.OK;
 		}
 
 		@Override
 		public void onPostExecute(RetrieveResult result) {
 			onEndVersion();
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
