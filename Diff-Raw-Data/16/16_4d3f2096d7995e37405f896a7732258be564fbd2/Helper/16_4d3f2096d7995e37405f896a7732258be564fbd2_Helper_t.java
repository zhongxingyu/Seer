 /***
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package de.mangelow.syncwifi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import de.mangelow.syncwifi.Ac;
 import de.mangelow.syncwifi.At;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SyncAdapterType;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo.State;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Helper {
 
 	private final String TAG = "SW";
 	private final boolean D = true;
 
 	private final String PREF_FILE = "Prefs";
 
 	public final boolean SYNC = false;
 	public final int PERIODIC_SYNC = 0;
 
 	public final int CELL = 0;
 	public final int WIFI = 1;
 
 	//
 
	public static ArrayList<Object> accounts;	
 
 	public boolean populateAccountsList(Context context) {
 		if(D)Log.d(TAG, "populateAccountsList");
 
 		//
 
 		String [] autorities_values_res = context.getResources().getStringArray(R.array.authorities_values);
 		ArrayList<String> autorities_values = new ArrayList<String>();
 		for (int i = 0; i < autorities_values_res.length; i++)autorities_values.add(autorities_values_res[i]);
 
 		String [] autorities_names = context.getResources().getStringArray(R.array.authorities_names);
 
 
 		//
 
 		try {
 			accounts = new ArrayList<Object>();
 
 			AccountManager manager = (AccountManager) context
 					.getSystemService(Context.ACCOUNT_SERVICE);
 			Account[] list = manager.getAccounts();
 
 			for (Account account : list) {
 				Ac a = new Ac();
 				a.setAccount(account);
 
 				ArrayList<At> authorities = a.getAuthorities();
 				Account[] acct = null;
 				SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();
 				for (SyncAdapterType type : types) {
 					acct = manager.getAccountsByType(type.accountType);
 					int length = acct.length;
 					if (length > 0) {
 						for (int i = 0; i < acct.length; i++) {
 							if (acct[i].name.equals(account.name)
 									&& acct[i].type.equals(account.type)) {
 
 								At at = new At();								
 								at.setAuthorityValue(type.authority);
 								String authority_name = type.authority;
 								if(autorities_values.contains(type.authority))authority_name = autorities_names[autorities_values.indexOf(type.authority)];
 								at.setAuthorityName(authority_name);
 
 								authorities.add(at);
 							}
 						}
 					}
 				}
 				a.setAuthority(authorities);
 				accounts.add(a);
 			}
 
 			Collections.sort(accounts, new MyNameComparable());
 
 		} 
 		catch (Exception e) {
 			if(D)e.printStackTrace();
 			return false;			
 		}
 		return true;
 	}
 	public void setAccounts(Context context, int conn, boolean auto) {
 
 		if(Helper.accounts==null) {
 			if(!populateAccountsList(context))return;
 		}
 
 		//
 
 		int enabled = 0;
 		int disabled = 0;
 
 		for (int i = 0; i < Helper.accounts.size(); i++) {
 			final Ac ac = (Ac) Helper.accounts.get(i);					
 			final ArrayList<At> ats = ac.getAuthorities();					
 			for (int j = 0; j < ats.size(); j++) {
 
 				At at = (At) ats.get(j);
 
 				String whichone =  CELL + "_" + ac.getAccount().name + "_" +  ac.getAccount().type + "_" + at.getAuthorityValue();
 				boolean sync = loadBooleanPref(context, whichone , SYNC);
 				if(getSync(ac.getAccount(), at.getAuthorityValue())!=sync&&conn==CELL) {
 					if(D)Log.d(TAG, "CELL - setSync - " + ac.getAccount().name + " - " + at.getAuthorityName() + " - " + sync);
 					setSync(ac.getAccount(), at.getAuthorityValue(), sync);
 					if(sync) {
 						enabled++;
 					}
 					else {
 						disabled++;
 					}
 				}
 
 				whichone =  WIFI + "_" + ac.getAccount().name + "_" +  ac.getAccount().type + "_" + at.getAuthorityValue();
 				sync = loadBooleanPref(context, whichone , SYNC);
 				if(getSync(ac.getAccount(), at.getAuthorityValue())!=sync&&conn==WIFI) {
 					if(D)Log.d(TAG, "WIFI - setSync - " + ac.getAccount().name + " - " + at.getAuthorityName() + " - " + sync);
 					setSync(ac.getAccount(), at.getAuthorityValue(), sync);
 					if(sync) {
 						enabled++;
 					}
 					else {
 						disabled++;
 					}
 				}
 			}			
 		}
 
 		if(D&&auto&&enabled>0|disabled>0) {
 			String msg = "Mobile Data - ";
 			if(conn==WIFI)msg = "Wifi - ";			
 			if(enabled>0)msg += enabled + " enabled";
 			if(enabled>0&&disabled>0)msg += ", ";
 			if(disabled>0)msg += disabled + " disabled";
 			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
 		}
 	}
 	public boolean getSync(Account account, String authority) {
 		return ContentResolver.getSyncAutomatically(account, authority);
 	}
 	public void setSync(Account account, String authority, boolean value) {
 		ContentResolver.setSyncAutomatically(account, authority, value);
 	}	
 	public boolean isConnected(Context context, int conn) {
 		final ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		State state = conMan.getNetworkInfo(conn).getState();		
 		if(state==State.CONNECTED)return true;
 		return false;
 	}
 	public void fireOrCancelRTC(Context context, int minutes) {
 		Intent intent = new Intent(context, Receiver.class);
 		intent.setAction(Receiver.ACTION_PERIODIC_SYNC);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 543543234, intent, 0);
 		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		if(minutes==0) {
 			alarmManager.cancel(pendingIntent);
 		}
 		else {
 			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (minutes * 60 * 1000), (minutes * 60 * 1000), pendingIntent);
 		}
 	}
 	public void saveBooleanPref(Context context,String name, boolean value) {
 		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
 		prefs.putBoolean(name, value);
 		prefs.commit();
 	}
 	public Boolean loadBooleanPref(Context context, String name, boolean defaultvalue) {
 		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
 		boolean bpref = prefs.getBoolean(name, defaultvalue);
 		return bpref;
 	}
 	public void saveIntPref(Context context,String name, int value) {
 		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
 		prefs.putInt(name, value);
 		prefs.commit();
 	}
 	public int loadIntPref(Context context, String name, int defaultvalue) {
 		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
 		int lpref = prefs.getInt(name, defaultvalue);
 		return lpref;
 	}
 	public void saveLongPref(Context context,String name, long value) {
 		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
 		prefs.putLong(name, value);
 		prefs.commit();
 	}
 	public long loadLongPref(Context context, String name, long defaultvalue) {
 		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
 		long lpref = prefs.getLong(name, defaultvalue);
 		return lpref;
 	}	
 }
