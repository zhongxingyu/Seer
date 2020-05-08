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
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Window;
 
 public class AccountsActivity extends PreferenceActivity {
 
 	private final String TAG = "SW";
	private final boolean D = true;
 
 	private Helper mHelper = new Helper();
 	private Context context;
 	private Resources res;
 
 
 	//
 
 	private PreferenceScreen root;
 	private PreferenceCategory pc_cell;
 	private PreferenceCategory pc_wifi;
 
 	//
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		if(D)Log.d(TAG, "onCreate");
 
 		if(Build.VERSION.SDK_INT<11)requestWindowFeature(Window.FEATURE_LEFT_ICON);
 
 		super.onCreate(savedInstanceState);
 		context = getApplicationContext();
 		res = context.getResources();
 	}   
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(D)Log.d(TAG, "onResume");
 
 		root = getPreferenceManager().createPreferenceScreen(context);
 		setPreferenceScreen(root);
 		if(Build.VERSION.SDK_INT<11)getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R.drawable.ic_launcher);
 
 		Preference p_setting = new Preference(context);
 		p_setting.setTitle(res.getString(R.string.accountsandsyncs_title));
 		p_setting.setSummary(res.getString(R.string.accountsandsyncs_summary));
 		p_setting.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
 			public boolean onPreferenceClick(Preference preference) {
 				startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS));
 				return false;
 			}
 		});
 		root.addPreference(p_setting);
 
 		//		
 
 		new AccountsTask().execute();
 
 	}
 	@Override
 	public void onPause() {
 		super.onResume();
 		if(D)Log.d(TAG, "onPause");
 
 	}
 	private class AccountsTask extends AsyncTask<Void, String, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(Void... arg0) {	
 			return mHelper.populateAccountsList(context);
 		}
 		protected void onPostExecute(Boolean result) {
 			if(D)Log.d(TAG, "onPostExecute - "  + result);	
 
 			if(result) {
 
 				boolean enabled = mHelper.loadBooleanPref(context, "enabled", mHelper.SYNC);
 
 				CheckBoxPreference cbp_enabled = new CheckBoxPreference(context);
 				cbp_enabled.setTitle(res.getString(R.string.enabled_title));
 				cbp_enabled.setChecked(enabled);
 				cbp_enabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 					public boolean onPreferenceChange(Preference p, Object o) {
 						boolean newvalue = Boolean.parseBoolean(o.toString());
 						mHelper.saveBooleanPref(context, "enabled", newvalue);
 
 						//
 
 						pc_cell.setEnabled(newvalue);
 						pc_wifi.setEnabled(newvalue);
 
 						return true;
 					}
 				});
 
 				//
 
 				pc_cell = new PreferenceCategory(context);
 				if(Build.VERSION.SDK_INT>10)pc_cell.setIcon(R.drawable.cell_light);
 				pc_cell.setTitle(res.getString(R.string.mobiledata) + " - " + res.getString(R.string.accounts));
 				pc_cell.setEnabled(enabled);
 
 				pc_wifi = new PreferenceCategory(context);
 				pc_wifi.setEnabled(false);
 				if(Build.VERSION.SDK_INT>10)pc_wifi.setIcon(R.drawable.wifi_light);
 				pc_wifi.setTitle(res.getString(R.string.wifi) + " - " + res.getString(R.string.accounts));
 				pc_wifi.setEnabled(enabled);
 				
 				int length = Helper.accounts.size();
 				if(length>0) {
 					root.addPreference(cbp_enabled);
 
 					// MobileData Accounts
 					root.addPreference(pc_cell);	
 
 					for (int i = 0; i < length; i++) {
 
 						final Ac ac = (Ac) Helper.accounts.get(i);
 
 						@SuppressWarnings("deprecation")
 						PreferenceScreen ps = getPreferenceManager().createPreferenceScreen(AccountsActivity.this);	
 						if(Build.VERSION.SDK_INT>10)ps.setIcon(R.drawable.cell_light);
 						ps.setTitle(ac.getAccount().name + " - " + res.getString(R.string.mobiledata));
 						ps.setSummary(ac.getAccount().type);
 						pc_cell.addPreference(ps);
 
 
 						final ArrayList<At> ats = ac.getAuthorities();
 						for (int j = 0; j < ats.size(); j++) {
 
 							final At at = (At) ats.get(j);
 
 							final CheckBoxPreference cbp = new CheckBoxPreference(context);
 							cbp.setTitle(at.getAuthorityName());
 							cbp.setSummary(at.getAuthorityValue());
 
 							final String whichone =  mHelper.CELL + "_" + ac.getAccount().name + "_" +  ac.getAccount().type + "_" + at.getAuthorityValue();
 							boolean sync = mHelper.loadBooleanPref(context, whichone , mHelper.SYNC);
 							cbp.setChecked(sync);
 
 							cbp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 								public boolean onPreferenceChange(Preference p, Object o) {
 
 									boolean newvalue = Boolean.parseBoolean(o.toString());
 									mHelper.saveBooleanPref(context, whichone, newvalue);
 
 									//
 
 									mHelper.setAccounts(context, mHelper.CELL, false);								
 
 									return true;
 								}
 							});
 
 							ps.addPreference(cbp);
 						}
 					}
 
 					// 
 
 					int ps_cell = mHelper.loadIntPref(context, "ps_cell", mHelper.PERIODIC_SYNC);
 
 					final String [] times = res.getStringArray(R.array.times);
 					final String [] times_values = res.getStringArray(R.array.times_values);
 
 					ListPreference lp_cell = new ListPreference(AccountsActivity.this);
 					lp_cell.setTitle(res.getString(R.string.periodicsync) + " - " + res.getString(R.string.mobiledata));
 					lp_cell.setEntries(times);
 					lp_cell.setEntryValues(times_values);
 					lp_cell.setDialogTitle(res.getString(R.string.pleasechoose));
 					lp_cell.setSummary(times[ps_cell]);
 					lp_cell.setValue(String.valueOf(ps_cell));
 					lp_cell.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 						public boolean onPreferenceChange(Preference preference, Object newValue) {
 							final String summary = newValue.toString();
 							ListPreference lp = (ListPreference) preference;						
 							int newvalue = lp.findIndexOfValue(summary);
 							lp.setSummary(times[newvalue]);
 							mHelper.saveIntPref(context, "ps_cell", Integer.parseInt(times_values[newvalue]));
 							if(mHelper.isConnected(context, mHelper.CELL))mHelper.setAccounts(context, mHelper.CELL, false);	
 
 							return true;
 						}
 					}); 
 					//pc_cell.addPreference(lp_cell);
 
 					// Wifi-Accounts
 					root.addPreference(pc_wifi);
 
 					for (int i = 0; i < length; i++) {
 
 						final Ac ac = (Ac) Helper.accounts.get(i);
 
 						@SuppressWarnings("deprecation")			
 						PreferenceScreen ps = getPreferenceManager().createPreferenceScreen(AccountsActivity.this);	
 						if(Build.VERSION.SDK_INT>10)ps.setIcon(R.drawable.wifi_light);
 						ps.setTitle(ac.getAccount().name + " - " + res.getString(R.string.wifi));
 						ps.setSummary(ac.getAccount().type);
 						pc_wifi.addPreference(ps);
 
 						final ArrayList<At> ats = ac.getAuthorities();
 						for (int j = 0; j < ats.size(); j++) {
 
 							final At at = (At) ats.get(j);
 
 							final CheckBoxPreference cbp = new CheckBoxPreference(context);
 							cbp.setTitle(at.getAuthorityName());
 							cbp.setSummary(at.getAuthorityValue());
 
 							final String whichone =  mHelper.WIFI + "_" + ac.getAccount().name + "_" +  ac.getAccount().type + "_" + at.getAuthorityValue();
 							boolean sync = mHelper.loadBooleanPref(context, whichone , mHelper.SYNC);
 							cbp.setChecked(sync);
 
 							cbp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 								public boolean onPreferenceChange(Preference p, Object o) {
 
 									boolean newvalue = Boolean.parseBoolean(o.toString());
 									mHelper.saveBooleanPref(context, whichone, newvalue);
 
 									//
 
 									mHelper.setAccounts(context, mHelper.WIFI, false);	
 
 									return true;
 								}
 							});
 
 							ps.addPreference(cbp);
 						}
 					}
 
 					// 
 
 					int ps_wifi = mHelper.loadIntPref(context, "ps_wifi", mHelper.PERIODIC_SYNC);
 
 					ListPreference lp_wifi = new ListPreference(AccountsActivity.this);
 					lp_wifi.setTitle(res.getString(R.string.periodicsync) + " - " + res.getString(R.string.wifi));
 					lp_wifi.setEntries(times);
 					lp_wifi.setEntryValues(times_values);
 					lp_wifi.setDialogTitle(res.getString(R.string.pleasechoose));
 					lp_wifi.setSummary(times[ps_wifi]);
 					lp_wifi.setValue(String.valueOf(ps_wifi));
 					lp_wifi.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 						public boolean onPreferenceChange(Preference preference, Object newValue) {
 							final String summary = newValue.toString();
 							ListPreference lp = (ListPreference) preference;						
 							int newvalue = lp.findIndexOfValue(summary);
 							lp.setSummary(times[newvalue]);
 							mHelper.saveIntPref(context, "ps_wifi", Integer.parseInt(times_values[newvalue]));
 							if(mHelper.isConnected(context, mHelper.WIFI))mHelper.setAccounts(context, mHelper.WIFI, false);	
 
 							return true;
 						}
 					}); 
 					//pc_wifi.addPreference(lp_wifi);
 
 				}
 				else {
 
 					Preference p_noaccountsfound = new Preference(context);
 					p_noaccountsfound.setTitle(res.getString(R.string.noaccounts_title));
 					p_noaccountsfound.setSummary(res.getString(R.string.noaccounts_summary));
 					p_noaccountsfound.setEnabled(false);
 					root.addPreference(p_noaccountsfound);
 				}
 			}			
 		}
 	}	
 }
