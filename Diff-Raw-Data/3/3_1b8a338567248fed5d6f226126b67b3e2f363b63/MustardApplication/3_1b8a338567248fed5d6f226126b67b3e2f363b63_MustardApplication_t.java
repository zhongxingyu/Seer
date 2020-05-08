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
 
 package org.mustard.android;
 
 import java.net.URL;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 import org.mustard.android.provider.OAuthInstance;
 import org.mustard.android.provider.OAuthLoader;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.android.receiver.StartupReceiver;
 import org.mustard.statusnet.User;
 import org.mustard.util.ImageManager;
 import org.mustard.util.StringUtil;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.location.Location;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * 
  * sqlite3 /data/data/org.mustard.android/databases/data
  * 
  * market://search/?q=pname:org.mustard.android.Mustard
  * 
  * @author macno
  *
  */
 public class MustardApplication extends Application {
 
 	@Override
 	public void onTerminate() {
 		super.onTerminate();
 		try {
 			MustardDbAdapter dbAdapter = new MustardDbAdapter(this);
 			dbAdapter.open();
 			dbAdapter.deleteStatuses(MustardDbAdapter.ROWTYPE_ALL, null);
 			dbAdapter.close();
 		} catch(Exception e) {
 			if (DEBUG)
 				Log.d(TAG, e.getMessage());
 		}
 	}
 
 	public static final boolean DEBUG = false;
 	
 	public static final String TAG = "MustardApplication";
 	
 	public static final String APPLICATION_NAME = "mustard";
 	public static final String SN_MIN_VERSION = "0.8.1";
 	
 	public static String sVersionName;
 
 	public static ImageManager sImageManager;
 	//public static Position sPosition = new Position();
 	
 	public static Location sLocation = null;
 	
 	private SharedPreferences mSharedPreferences = null;
 	
 //	public static CommonsHttpOAuthProvider mOAuthProvider;
 		
 	public void onCreate() {
 		super.onCreate();
 		
 		if(DEBUG) 
 			Log.d(TAG,"onCreate");
 		
 		sImageManager = new ImageManager(this);
 
 		PackageManager pm = getPackageManager();
 		PackageInfo pi;
 		
 		try {
 			pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
 			sVersionName=pi.versionName;
 			if(DEBUG) Log.d(TAG,"Version: " + sVersionName);
 		} catch (Exception e) {
 			
 		}
 		
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());	
 		StartupReceiver.onStartupApplication(getApplicationContext());
 	}
 	
 	public boolean checkVersion(String version) {
 		return StringUtil.compareVersion(version, SN_MIN_VERSION) >= 0;
 	}
 	
 	public StatusNet loadAccount(MustardDbAdapter mDbHelper, long userid) {
 		return checkAccount(mDbHelper,false,userid);
 	}
 	
 	public StatusNet checkAccount(MustardDbAdapter mDbHelper) {
 		return checkAccount(mDbHelper,true,-1);
 	}
 	
 	public StatusNet checkAccount(MustardDbAdapter mDbHelper, long userid) {
 		return checkAccount(mDbHelper,false,userid);
 	}
 	
     public StatusNet checkAccount(MustardDbAdapter mDbHelper, boolean loadDefault, long userid) {
     	StatusNet sn = new StatusNet(this);
 		String s_maxNotices = mSharedPreferences.getString(Preferences.FETCH_MAX_ITEMS_KEY, getString(R.string.pref_rows_fetch_default));
 		int maxNotices = Integer.parseInt(s_maxNotices);
 		sn.setMaxNotices(maxNotices);
     	Cursor accountCursor = null;
     	try {
     		if(loadDefault)
     			accountCursor = mDbHelper.fetchDefaultAccount();
     		else
     			accountCursor = mDbHelper.fetchAccount(userid);
     	} catch (Exception e) {
     		e.printStackTrace();
     		if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
     		if (accountCursor!=null) try { accountCursor.close(); } catch (Exception ee) {}
     		return null;
     	} 
 
     	if (accountCursor != null && accountCursor.getCount()<1) {
     		if (accountCursor!=null) try { accountCursor.close(); } catch (Exception e) {}
     		return null;
     	}
     	try {
     		if(accountCursor.moveToNext()) {
     			int userIndex = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER);
     			int userIdIndex  = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID);
                 int passwordIndex = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_PASSWORD);
                 int instanceIndex = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_INSTANCE);
                 int rowidIndex = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_ROWID);
                 int tokenIndex = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_TOKEN);
                 int tokenSecretIndex = accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_TOKEN_SECRET);
                 int versionIndex =  accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_VERSION);
                 int textLimitIndex =  accountCursor.getColumnIndexOrThrow(MustardDbAdapter.KEY_TEXTLIMIT);
     			String user=accountCursor.getString(userIndex);
     			String password=accountCursor.getString(passwordIndex);
     			String instance=accountCursor.getString(instanceIndex);
     			long userId=accountCursor.getLong(rowidIndex);
     			long usernameId = accountCursor.getLong(userIdIndex);
     			int textLimit = accountCursor.getInt(textLimitIndex);
     			String version = accountCursor.getString(versionIndex);
    			if (version == null) { // Twitter creation fix
    				version = "0.9.4";
    			}
     			Account account = new Account();
     			account.setId(userId);
     			account.setUsername(user);
     			account.setVersion(version);
     			account.setTextLimit(textLimit);
     			account.setInstance(instance);
     			
     			String token=accountCursor.getString(tokenIndex);
     			String tokenSecret=accountCursor.getString(tokenSecretIndex);
     			sn.setURL(new URL(instance));
     			if (token==null && tokenSecret == null)
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
     			
     			if (usernameId <= 0 ) {
     				User u = sn.getUser(user);
     				usernameId = u.getId();
     				mDbHelper.setUserIdAccount(userId, usernameId);
     			}
     			sn.setUsernameId(usernameId);
     			sn.setUserId(userId);
     			sn.setAccount(account);
     			
     		}
     	} catch (Exception e) {
     		if (MustardApplication.DEBUG) e.printStackTrace();
     		return null;
     	} finally {
     		if(accountCursor != null)
     			try {accountCursor.close(); } catch (Exception e){}
     	}
     	return sn;
     }
 
     
 }
