 /**
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.data.SynoProtocol;
 import com.bigpupdev.synodroid.data.TaskSort;
 import com.bigpupdev.synodroid.preference.EditTextPreferenceWithValue;
 import com.bigpupdev.synodroid.preference.ListPreferenceMultiSelectWithValue;
 import com.bigpupdev.synodroid.preference.ListPreferenceWithValue;
 import com.bigpupdev.synodroid.preference.PreferenceFacade;
 import com.bigpupdev.synodroid.preference.PreferenceProcessor;
 import com.bigpupdev.synodroid.preference.PreferenceWithValue;
 import com.bigpupdev.synodroid.utils.SearchResultsOpenHelper;
 import com.bigpupdev.synodroid.utils.SynodroidSearchSuggestion;
 import com.bigpupdev.synodroid.utils.UIUtils;
 import com.bigpupdev.synodroid.utils.Utils;
 import com.bigpupdev.synodroid.wizard.AddServerWizard;
 import com.bigpupdev.synodroid.wizard.ServerWizard;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.provider.SearchRecentSuggestions;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.WindowManager;
 import android.widget.BaseAdapter;
 import android.widget.Toast;
 
 /**
  * The preference activity
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class DownloadPreferenceActivity extends BasePreferenceActivity implements PreferenceProcessor {
 
 	// Menu Create server
 	public static final int MENU_CREATE = 1;
 	// Menu Delete
 	public static final int MENU_DELETE = 2;
 	// Menu Wizard
 	public static final int MENU_WIZARD = 3;
 
 	private static final String PREFERENCE_AUTO = "auto";
 	private static final String PREFERENCE_AUTO_CREATENOW = "auto.createnow";
 	private static final String PREFERENCE_FULLSCREEN = "general_cat.fullscreen";
 	private static final String PREFERENCE_GENERAL = "general_cat";
 	private static final String PREFERENCE_DEBUG = "debug_cat";
 	private static final String PREFERENCE_DEBUG_LOG = "debug_cat.debug_logging";
 	private static final String PREFERENCE_AUTO_DSM = "general_cat.auto_detect_DSM";
 	private static final String PREFERENCE_DEF_SRV = "servers_cat.default_srv";
 	private static final String PREFERENCE_SERVER = "servers_cat";
 	
 	// Store the current max server id
 	private int maxServerId = 0;
 	// The dynamic servers category
 	private PreferenceCategory serversCategory;
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// ignore orientation change
 		super.onConfigurationChanged(newConfig);
 	}
 
 	/**
 	 * Create the UI
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Add the preference screen
 		addPreferencesFromResource(R.xml.preference);
 
 		// Retreive the preference screen
 		PreferenceScreen prefScreen = getPreferenceScreen();
 		// The general category
 		PreferenceCategory generalCategory = (PreferenceCategory) prefScreen.getPreferenceManager().findPreference(PREFERENCE_GENERAL);
 		final ListPreferenceWithValue orderPref = ListPreferenceWithValue.create(this, "sort", R.string.label_process_sort, R.string.hint_process_sort, null);
 		orderPref.setOrder(0);
 		generalCategory.addPreference(orderPref);
 		// Build the sort list
 		String[] sortLabels = new String[TaskSort.values().length];
 		String[] sortValues = new String[TaskSort.values().length];
 		for (int iLoop = 0; iLoop < TaskSort.values().length; iLoop++) {
 			sortLabels[iLoop] = getString(TaskSort.values()[iLoop].getResId());
 			sortValues[iLoop] = TaskSort.values()[iLoop].name();
 		}
 		orderPref.setEntries(sortLabels);
 		orderPref.setEntryValues(sortValues);
 		// Strange behaviour: I was unable to create the CheckBoxPreference at
 		// runtime. Well it ran and the state was correclty saved, but the checkbox
 		// was unable to reflect (checked, unchecked) to correct state. So I decided
 		// to create the CheckBoxPreference in the XML layout and use it at runtime
 		// rather than create it at runtime
 		final CheckBoxPreference asc = (CheckBoxPreference) generalCategory.findPreference("asc");
 		// Set listeners to update the server sort
 		final Synodroid app = (Synodroid) getApplication();
 		try{
 			orderPref.setValue(app.getServerSort());
 		}
 		catch (Exception e){}
 		orderPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				app.setServerSort((String) newValue, asc.isChecked());
 				return true;
 			}
 		});
 		asc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				app.setServerSort(orderPref.getCurrentValue(), (Boolean) newValue);
 				return true;
 			}
 		});
 
 		// Fullscreen preference
 		final CheckBoxPreference fullPref = new CheckBoxPreference(this);
 		fullPref.setKey(PREFERENCE_FULLSCREEN);
 		fullPref.setTitle(R.string.fullscreen_preference);
 		fullPref.setSummary(R.string.summary_fullscreen_preference);
 		generalCategory.addPreference(fullPref);
 		fullPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 				if (newValue.toString().equals("true")) {
 					preferences.edit().putBoolean(PREFERENCE_FULLSCREEN, true).commit();
 					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 				} else {
 					preferences.edit().putBoolean(PREFERENCE_FULLSCREEN, false).commit();
 					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 				}
 				return true;
 			}
 		});
 		
 		//Set AUTODSM Default value...
 		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		
 		final CheckBoxPreference autoDSM = new CheckBoxPreference(this);
 		autoDSM.setKey(PREFERENCE_AUTO_DSM);
 		autoDSM.setTitle(R.string.auto_DSM);
 		autoDSM.setSummary(R.string.hint_auto_DSM);
 		autoDSM.setChecked(preferences.getBoolean(PREFERENCE_AUTO_DSM, true));
 		generalCategory.addPreference(autoDSM);
 		autoDSM.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 				if (newValue.toString().equals("true")) {
 					preferences.edit().putBoolean(PREFERENCE_AUTO_DSM, true).commit();
 				} else {
 					preferences.edit().putBoolean(PREFERENCE_AUTO_DSM, false).commit();
 				}
 				return true;
 			}
 		});
 		
 		final Preference clearHistory = new Preference(this);
 		clearHistory.setTitle(R.string.clear_search_history);
 		generalCategory.addPreference(clearHistory);
 		clearHistory.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 			public boolean onPreferenceClick(Preference arg0) {
 				clearSearchHistory();
 				Toast toast = Toast.makeText(DownloadPreferenceActivity.this, getString(R.string.cleared_search_history), Toast.LENGTH_SHORT);
 				toast.show();
 				return false;
 			}
 
 		});
 		
 		PreferenceCategory debugPreference = (PreferenceCategory) prefScreen.getPreferenceManager().findPreference(PREFERENCE_DEBUG);
 		
 		final CheckBoxPreference dbgLog = new CheckBoxPreference(this);
 		dbgLog.setKey(PREFERENCE_DEBUG_LOG);
 		dbgLog.setTitle(R.string.debug);
 		dbgLog.setSummary(R.string.hint_debug);
 		debugPreference.addPreference(dbgLog);
 		dbgLog.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				SharedPreferences preferences = getSharedPreferences(PREFERENCE_DEBUG, Activity.MODE_PRIVATE);
 				if (newValue.toString().equals("true")) {
 					preferences.edit().putBoolean(PREFERENCE_DEBUG_LOG, true).commit();
 					((Synodroid)getApplication()).enableDebugLog();
 				} else {
 					preferences.edit().putBoolean(PREFERENCE_DEBUG_LOG, false).commit();
 					((Synodroid)getApplication()).disableDebugLog();
 				}
 				return true;
 			}
 		});
 
 		if (UIUtils.isJB()){
 			final Preference sendDebugLogs = new Preference(this);
 			sendDebugLogs.setTitle(R.string.send_debug_logs);
 			debugPreference.addPreference(sendDebugLogs);
 			sendDebugLogs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 	
 				public boolean onPreferenceClick(Preference arg0) {
 					Intent next = new Intent();
 					next.setClass(DownloadPreferenceActivity.this, DebugActivity.class);
 					startActivity(next);
 					return false;
 				}
 	
 			});
 		}
 		
 		// The dynamic servers category
 		serversCategory = (PreferenceCategory) prefScreen.getPreferenceManager().findPreference(PREFERENCE_SERVER);
 		
 		// Load currents servers
 		reloadCurrentServers();
 	}
 
 	@Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         if (UIUtils.isHoneycomb()){
         	getActivityHelper().setupSubActivity();
         }
     }
 	
 	private void clearSearchHistory() {
 		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(DownloadPreferenceActivity.this, SynodroidSearchSuggestion.AUTHORITY, SynodroidSearchSuggestion.MODE);
 		suggestions.clearHistory();
 		
 		SearchResultsOpenHelper db_helper = new SearchResultsOpenHelper(this);
 		SQLiteDatabase cache = db_helper.getWritableDatabase();
 		cache.execSQL("DELETE FROM "+ SearchResultsOpenHelper.TABLE_CACHE);
 		cache.close();
 	}
 	
 	private void reloadCurrentServers() {
 		serversCategory.removeAll();
 		maxServerId = 0;
 		
 		//Create default server selection
 		final ListPreferenceWithValue defSrvPref = ListPreferenceWithValue.create(DownloadPreferenceActivity.this, PREFERENCE_DEF_SRV, R.string.label_def_srv, R.string.hint_def_srv, null);
 		defSrvPref.setOrder(0);
 		
 		// Load current servers
 		PreferenceFacade.processLoadingServers(getPreferenceScreen().getSharedPreferences(), this, defSrvPref, getString(R.string.srv_always_ask));
 		
 		serversCategory.addPreference(defSrvPref);
 		defSrvPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				SharedPreferences preferences = getSharedPreferences(PREFERENCE_SERVER, Activity.MODE_PRIVATE);
 				preferences.edit().putString(PREFERENCE_DEF_SRV, (String) newValue).commit();
 				return true;
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		try{
 			if (((Synodroid)getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadPreferenceActivity: Resuming download preference activity.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		// Check for fullscreen
 		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)) {
 			// Set fullscreen or not
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		} else {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 
 		if (hasFocus) {
 			SharedPreferences preferences = getSharedPreferences(PREFERENCE_AUTO, Activity.MODE_PRIVATE);
 			if (preferences.getBoolean(PREFERENCE_AUTO_CREATENOW, false)) {
 				autoCreate();
 				preferences.edit().putBoolean(PREFERENCE_AUTO_CREATENOW, false).commit();
 			}
 		}
 	}
 
 	private void autoCreate(){
 		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		boolean wifiOn = wifiMgr.isWifiEnabled();
 		final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
 		boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
 		if (wifiConnected) {
 			ServerWizard wiz = new ServerWizard(DownloadPreferenceActivity.this, wifiMgr.getConnectionInfo().getSSID(), ((Synodroid)getApplication()).DEBUG);
 			wiz.start();
 		}
 		else{
 			AddServerWizard wiz = new AddServerWizard(DownloadPreferenceActivity.this, ((Synodroid)getApplication()).DEBUG);
 			wiz.start();
 		}
 	}
 	
 	/**
 	 * Create the option menu of this activity
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.pref_menus, menu);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		boolean wizardPossible = Integer.parseInt(android.os.Build.VERSION.SDK) > 3;
 		if (wizardPossible) {
 			MenuItem wizardItem = menu.getItem(0);
 			if (wizardItem != null) {
 				WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 				boolean wifiOn = wifiMgr.isWifiEnabled();
 				final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
 				boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
 				wizardItem.setEnabled(wifiConnected);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Interact with the user
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_wizard) {
 			try{
 				if (((Synodroid)getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadPreferenceActivity: Menu find server selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 			boolean wifiOn = wifiMgr.isWifiEnabled();
 			final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
 			boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
 			if (wifiConnected) {
 				ServerWizard wiz = new ServerWizard(DownloadPreferenceActivity.this, wifiMgr.getConnectionInfo().getSSID(), ((Synodroid)getApplication()).DEBUG);
 				wiz.start();
 			}
 			else{
 				//TODO: Works only on wifi
 				
 			}
 			return true;
 		// Create a new server
 		}else if (item.getItemId() == R.id.menu_create) {
 			try{
 				if (((Synodroid)getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadPreferenceActivity: Menu add server selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			AddServerWizard wiz = new AddServerWizard(DownloadPreferenceActivity.this, ((Synodroid)getApplication()).DEBUG);
 			wiz.start();
 		// Delete one or more servers
 		}else if (item.getItemId() == R.id.menu_delete) {
 			try{
 				if (((Synodroid)getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadPreferenceActivity: Menu delete servers selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			// Load servers list
 			final ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
 			PreferenceFacade.processLoadingServers(getPreferenceScreen().getSharedPreferences(), new PreferenceProcessor() {
 				public void process(int idP, String keyP, Properties propsP) {
 					ServerInfo deletion = new ServerInfo();
 					deletion.id = idP;
 					String title = propsP.getProperty(PreferenceFacade.NICKNAME_SUFFIX);
 					deletion.title = title;
 					deletion.delete = false;
 					deletion.key = keyP;
 					servers.add(deletion);
 				}
 			});
 			// Sort the list
 			Collections.sort(servers, new Comparator<ServerInfo>() {
 				public int compare(ServerInfo obj0, ServerInfo obj1) {
 					int id0 = obj0.id;
 					int id1 = obj1.id;
 					if (id0 == id1)
 						return 0;
 					return (id0 > id1 ? 1 : -1);
 				}
 			});
 			// Build titles
 			String[] servsTitle = new String[servers.size()];
 			for (int iLoop = 0; iLoop < servers.size(); iLoop++) {
 				servsTitle[iLoop] = servers.get(iLoop).title;
 			}
 			// Create the dialog
 			AlertDialog.Builder builder = new AlertDialog.Builder(DownloadPreferenceActivity.this);
 			builder.setTitle(getString(R.string.menu_delete_server));
 			builder.setMultiChoiceItems(servsTitle, null, new OnMultiChoiceClickListener() {
 				// Change delete state
 				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 					servers.get(which).delete = isChecked;
 				}
 			});
 			// When deleting remove from ServerCategory
 			builder.setPositiveButton(getString(R.string.button_delete), new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					SharedPreferences serverPref = DownloadPreferenceActivity.this.getSharedPreferences(PREFERENCE_SERVER, Activity.MODE_PRIVATE);
 					String defaultSrv = serverPref.getString(PREFERENCE_DEF_SRV, "0");
 					
 					Editor editor = getPreferenceScreen().getEditor();
 					// Loop on children
 					for (int iLoop = 0; iLoop < serversCategory.getPreferenceCount(); iLoop++) {
 						Preference pref = serversCategory.getPreference(iLoop);
 						String key = pref.getKey();
 						// Try to find the corresponding server
 						ServerInfo fake = new ServerInfo();
 						fake.key = key;
 						int index = servers.indexOf(fake);
 						if (index != -1) {
 							ServerInfo serv = servers.get(index);
 							// If we want to delete it then remove from the ServerCategory
 							if (serv.delete) {
 								editor.remove(serv.key + PreferenceFacade.USEEXT_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.USEWIFI_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.NICKNAME_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.USER_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.PASSWORD_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.DSM_SUFFIX);
 								
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.SSID_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.PROTOCOL_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.HOST_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.PORT_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.SHOWUPLOAD_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.REFRESHSTATE_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.REFRESHVALUE_SUFFIX);
 
 								editor.remove(serv.key + PreferenceFacade.PROTOCOL_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.HOST_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.PORT_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.SHOWUPLOAD_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.REFRESHSTATE_SUFFIX);
 								editor.remove(serv.key + PreferenceFacade.REFRESHVALUE_SUFFIX);
 								
 								if (serv.key.equals(defaultSrv)){
 									editor.putString(PREFERENCE_DEF_SRV, "0");
 								}
 							}
 						}
 					}
 					editor.commit();
 					// Reload servers preferences
 					reloadCurrentServers();
 				}
 			});
 			builder.setNegativeButton(getString(R.string.button_cancel), null);
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.ds.ServerProcessor#process(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public void process(int idP, String keyP, Properties propertiesP) {
 		String summary = null, summary2 = null;
 		String usewifi = propertiesP.getProperty(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.USEWIFI_SUFFIX);
 		String useext = propertiesP.getProperty(PreferenceFacade.USEEXT_SUFFIX);
 		
 		if (usewifi != null && usewifi.equals("true")) {
 			String SSIDs = propertiesP.getProperty(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.SSID_SUFFIX);
 			if (SSIDs != null && !SSIDs.equals("")){
 				summary = buildURL(propertiesP.getProperty(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.PROTOCOL_SUFFIX), propertiesP.getProperty(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.HOST_SUFFIX), propertiesP.getProperty(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.PORT_SUFFIX));	
 			}
 		}
 		if (useext != null && useext.equals("true")) {
 			summary2 = buildURL(propertiesP.getProperty(PreferenceFacade.PROTOCOL_SUFFIX), propertiesP.getProperty(PreferenceFacade.HOST_SUFFIX), propertiesP.getProperty(PreferenceFacade.PORT_SUFFIX));
 		}
 		summary = getServerSummary(summary, summary2);
 		if (idP > maxServerId) {
 			maxServerId = idP;
 		}
 		String title = propertiesP.getProperty(PreferenceFacade.NICKNAME_SUFFIX);
 		createServerPreference(idP, serversCategory, keyP, title, summary);
 	}
 
 	/**
 	 * Set the summary of a server
 	 * 
 	 * @param summary1P
 	 * @param summary2P
 	 */
 	private String getServerSummary(String summary1P, String summary2P) {
 		// If local connection exists
 		if (summary1P != null && summary1P.length() > 0) {
 			// And public connection too, then show both
 			if (summary2P != null && summary2P.length() > 8) {
 				summary1P += "\n" + summary2P + " (P)";
 			}
 		}
 		// Else if only public connection exists
 		else if (summary2P != null && summary2P.length() > 0) {
 			summary1P = summary2P + " (P)";
 		}
 		// Otherwise show default text
 		else {
 			summary1P = getString(R.string.hint_default_server);
 		}
 		return summary1P;
 	}
 
 	/**
 	 * Create a preference screen from a SynoServer instance
 	 * 
 	 * @return The instance of the PreferenceScreen
 	 */
 	private PreferenceScreen createServerPreference(int idP, PreferenceGroup parentP, String keyP, String titleP, String summaryP) {
 
 		// Create the server preference
 		final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
 		parentP.addPreference(screen);
 		screen.setOrder(idP);
 		screen.setKey(keyP);
 		screen.setPersistent(true);
 		screen.setTitle(titleP);
 		screen.setSummary(summaryP);
 		screen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
 				// At this point the dialog was created ! We can add our dismiss
 				// callback
 				screen.getDialog().setOnDismissListener(new OnDismissListener() {
 					public void onDismiss(DialogInterface dialog) {
 						// Don't forget to call the screen onDismiss method
 						try {
 							screen.onDismiss(dialog);
 						} catch (Exception e) {
 						}
 						// Then do our job: refresh summaries
 						int catCount = screen.getPreferenceCount();
 						String nickname = null, protWLAN = null, prot = null, hostWLAN = null, host = null, portWLAN = null, port = null, ssids = null;
 						boolean usewifi = false, useext = false;
 						for (int cLoop = 0; cLoop < catCount; cLoop++) {
 							Preference cat = screen.getPreference(cLoop);
 							if (cat instanceof PreferenceCategory) {
 								int prefCount = ((PreferenceCategory) cat).getPreferenceCount();
 								for (int iLoop = 0; iLoop < prefCount; iLoop++) {
 									Preference pref = ((PreferenceCategory) cat).getPreference(iLoop);
 									String key = pref.getKey();
 									if (key != null) {
 										if (key.endsWith(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.PROTOCOL_SUFFIX)) {
 											protWLAN = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.PROTOCOL_SUFFIX)) {
 											prot = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.HOST_SUFFIX)) {
 											hostWLAN = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.HOST_SUFFIX)) {
 											host = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.PORT_SUFFIX)) {
 											portWLAN = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.PORT_SUFFIX)) {
 											port = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.NICKNAME_SUFFIX)) {
 											nickname = ((PreferenceWithValue) pref).getPrintableValue();
 										} else if (key.endsWith(PreferenceFacade.USEWIFI_SUFFIX)) {
 											usewifi = ((CheckBoxPreference) pref).isChecked();
 										} else if (key.endsWith(PreferenceFacade.USEEXT_SUFFIX)) {
 											useext = ((CheckBoxPreference) pref).isChecked();
 										} else if (key.endsWith(PreferenceFacade.WLAN_RADICAL + PreferenceFacade.SSID_SUFFIX)){
 											ssids = ((PreferenceWithValue) pref).getPrintableValue();
 										}
 									}
 								}
 							}
 						}
 						// Build title
 						if (nickname != null) {
 							screen.setTitle(nickname);
 						}
 						// Build summaries
 						String summary1 = null, summary2 = null;
 						if (usewifi) {
 							if (ssids != null && !ssids.equals("")){
 								summary1 = buildURL(protWLAN, hostWLAN, portWLAN);	
 							}
 							
 						}
 						if (useext) {
 							summary2 = buildURL(prot, host, port);
 						}
 						summary1 = getServerSummary(summary1, summary2);
 						screen.setSummary(summary1);
 						// Notify the root PreferenceScreen that a child has been updated
 						PreferenceScreen rootScreen = getPreferenceScreen();
 						BaseAdapter adapt = (BaseAdapter) rootScreen.getRootAdapter();
 						adapt.notifyDataSetChanged();
 					}
 				});
 				return false;
 			}
 		});
 		// ----------------------------------------------
 		// Create a category to show general parameters
 		PreferenceCategory generalCategory = new PreferenceCategory(this);
 		generalCategory.setTitle(getString(R.string.title_cat_server));
 		screen.addPreference(generalCategory);
 		// ---- Nickname
 		final EditTextPreferenceWithValue nickPref = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.NICKNAME_SUFFIX, R.string.label_nickname, R.string.hint_nickname, true);
 		nickPref.setText(titleP);
 		nickPref.setDefaultValue(titleP);
 		generalCategory.addPreference(nickPref);
 
 		// ---- Username
 		generalCategory.addPreference(EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.USER_SUFFIX, R.string.label_username, R.string.hint_username, true));
 		// ---- Password
 		generalCategory.addPreference(EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.PASSWORD_SUFFIX, R.string.label_password, R.string.hint_password, false).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
 		// --- DSM Version
 		generalCategory.addPreference(ListPreferenceWithValue.create(this, keyP + PreferenceFacade.DSM_SUFFIX, R.string.label_dsm_version, R.string.hint_dsm_version, DSMVersion.getValues()));
 		
 		// Create local connection category
 		addConnectionCategory(keyP, screen, true, R.string.title_cat_connection_local);
 		// Create public connection category
 		addConnectionCategory(keyP, screen, false, R.string.title_cat_connection);
 
 		return screen;
 	}
 
 	/**
 	 * Add a preference category
 	 * 
 	 */
 	private void addConnectionCategory(String keyP, PreferenceScreen screen, boolean showWifiP, int titleResIdP) {
 		// Change the key if it's a wifi connection
 		if (showWifiP) {
 			keyP += PreferenceFacade.WLAN_RADICAL;
 		}
 
 		PreferenceCategory connectionCategory = new PreferenceCategory(this);
 		connectionCategory.setTitle(getString(titleResIdP));
 		screen.addPreference(connectionCategory);
 
 		final WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		// ---- Create Wifi list (ONLY for wifi connection)
 		if (showWifiP) {
 			// ---- Use Wifi
 			CheckBoxPreference useWifi = new CheckBoxPreference(this);
 			useWifi.setKey(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			useWifi.setTitle(R.string.label_usewifi);
 			// It looks like by using the set check function, the preference is not save
 			// properly. Removing it seems to make default preference better
 			// autoRefresh.setChecked(true);
 			useWifi.setDefaultValue(false);
 			useWifi.setSummaryOn(R.string.hint_usewifi_on);
 			useWifi.setSummaryOff(R.string.hint_usewifi_off);
 			connectionCategory.addPreference(useWifi);
 
 			List<WifiConfiguration> wifis = wifiMgr.getConfiguredNetworks();
 			int w_size = 0;
 			try{
 				w_size = wifis.size();
 			}
 			catch (NullPointerException e){}
 			
 			String[] wifiSSIDs = new String[w_size];
 			for (int iLoop = 0; iLoop < w_size; iLoop++) {
 				String ssid = wifis.get(iLoop).SSID;
 				if (ssid != null) {
 					if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
 						ssid = ssid.substring(1, ssid.length() - 1);
 					}
 					wifiSSIDs[iLoop] = ssid;
 				}
 			}
 			WifiInfo currentWifi = wifiMgr.getConnectionInfo();
 			String cur_ssid = Utils.validateSSID(currentWifi.getSSID());
 
 			final ListPreferenceMultiSelectWithValue wifiSSIDPref = ListPreferenceMultiSelectWithValue.create(this, keyP + PreferenceFacade.SSID_SUFFIX, R.string.label_wifissid, R.string.hint_wifissid, wifiSSIDs, cur_ssid);
 			connectionCategory.addPreference(wifiSSIDPref);
 			if (!wifiMgr.isWifiEnabled() || wifiSSIDs.length == 0) {
 				connectionCategory.setEnabled(false);
 			}
 		} else {
 			// ---- Use External Connection
 			CheckBoxPreference useExt = new CheckBoxPreference(this);
 			useExt.setKey(keyP + PreferenceFacade.USEEXT_SUFFIX);
 			useExt.setTitle(R.string.label_useext);
 			// It looks like by using the set check function, the preference is not save
 			// properly. Removing it seems to make default preference better
 			// autoRefresh.setChecked(true);
 			useExt.setDefaultValue(false);
 			useExt.setSummaryOn(R.string.hint_useext_on);
 			useExt.setSummaryOff(R.string.hint_useext_off);
 			connectionCategory.addPreference(useExt);
 
 		}
 
 		// ---- Protocol
 		final ListPreferenceWithValue protocolPref = ListPreferenceWithValue.create(this, keyP + PreferenceFacade.PROTOCOL_SUFFIX, R.string.label_protocol, R.string.hint_protocol, SynoProtocol.getValues());
 		protocolPref.setDefaultValue(SynoProtocol.getValues()[0]);
 		connectionCategory.addPreference(protocolPref);
 		// ---- Host
 		final EditTextPreferenceWithValue hostPref = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.HOST_SUFFIX, R.string.label_host, R.string.hint_host, true);
 		connectionCategory.addPreference(hostPref);
 		// ---- Port
 		final EditTextPreferenceWithValue portPref = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.PORT_SUFFIX, R.string.label_port, R.string.hint_port, true).setInputType(InputType.TYPE_CLASS_NUMBER);
 		connectionCategory.addPreference(portPref);
 
 		// ---- Show upload
 		CheckBoxPreference showUpload = new CheckBoxPreference(this);
 		showUpload.setKey(keyP + PreferenceFacade.SHOWUPLOAD_SUFFIX);
 		showUpload.setTitle(R.string.label_showupload);
 		// It looks like by using the set check function, the preference is not save
 		// properly. Removing it seems to make default preference better
 		// autoRefresh.setChecked(true);
 		showUpload.setDefaultValue(false);
 		showUpload.setSummaryOn(R.string.hint_showupload_on);
 		showUpload.setSummaryOff(R.string.hint_showupload_off);
 		connectionCategory.addPreference(showUpload);
 		// ---- Auto refresh
 		CheckBoxPreference autoRefresh = new CheckBoxPreference(this);
 		autoRefresh.setKey(keyP + PreferenceFacade.REFRESHSTATE_SUFFIX);
 		autoRefresh.setTitle(R.string.label_autorefresh);
 		// It looks like by using the set check function, the preference is not save
 		// properly. Removing it seems to make default preference better
 		// autoRefresh.setChecked(true);
 		autoRefresh.setDefaultValue(true);
 		autoRefresh.setSummaryOn(R.string.hint_autorefresh_on);
 		autoRefresh.setSummaryOff(R.string.hint_autorefresh_off);
 		connectionCategory.addPreference(autoRefresh);
 		// -- Refresh value
 		final EditTextPreferenceWithValue autoRefreshValue = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.REFRESHVALUE_SUFFIX, R.string.label_refreshinterval, R.string.hint_refreshinterval, true).setInputType(InputType.TYPE_CLASS_NUMBER);
 		autoRefreshValue.setDefaultValue("15");
 		connectionCategory.addPreference(autoRefreshValue);
 		// Add dependencies. DON'T use 'setDependency()' when building Preferences
 		// at runtime
 
 		if (showWifiP) {
 			connectionCategory.findPreference(keyP + PreferenceFacade.SSID_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.PROTOCOL_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.HOST_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.PORT_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.SHOWUPLOAD_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.REFRESHSTATE_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.REFRESHVALUE_SUFFIX).setDependency(keyP + PreferenceFacade.USEWIFI_SUFFIX);
 		} else {
 			connectionCategory.findPreference(keyP + PreferenceFacade.PROTOCOL_SUFFIX).setDependency(keyP + PreferenceFacade.USEEXT_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.HOST_SUFFIX).setDependency(keyP + PreferenceFacade.USEEXT_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.PORT_SUFFIX).setDependency(keyP + PreferenceFacade.USEEXT_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.SHOWUPLOAD_SUFFIX).setDependency(keyP + PreferenceFacade.USEEXT_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.REFRESHSTATE_SUFFIX).setDependency(keyP + PreferenceFacade.USEEXT_SUFFIX);
 			connectionCategory.findPreference(keyP + PreferenceFacade.REFRESHVALUE_SUFFIX).setDependency(keyP + PreferenceFacade.USEEXT_SUFFIX);
 		}
 
 		connectionCategory.findPreference(keyP + PreferenceFacade.REFRESHVALUE_SUFFIX).setDependency(keyP + PreferenceFacade.REFRESHSTATE_SUFFIX);
 	}
 
 	/**
 	 * Build a end-user String which represents the URL used
 	 * 
 	 * @param protoP
 	 * @param hostP
 	 * @param portP
 	 * @return
 	 */
 	private String buildURL(String protoP, String hostP, String portP) {
 		String result = "";
 		// If at least a non null value
 		if ((protoP != null && protoP.length() > 0) && (hostP != null && hostP.length() > 0) && (portP != null && portP.length() > 0)) {
 			result = result + (protoP != null ? protoP : "") + "://";
 			result = result + (hostP != null ? hostP : "") + ":";
 			result = result + (portP != null ? portP : "");
 		}
 		return result.toLowerCase();
 	}
 
 	/**
 	 * This method is called when the wizard finished. The metadata should contain information collected by the wizard. A null paramter means that no information have been collected.
 	 * 
 	 * @param metaDataP
 	 */
 	public void onWizardFinished(HashMap<String, Object> metaDataP) {
 		try{
 			if (((Synodroid)getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadPreferenceActivity: Wizard finished.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		if (metaDataP != null) {
 			try{
 				if (((Synodroid)getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadPreferenceActivity: Adding server connection.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			maxServerId++;
 			Editor editor = getPreferenceScreen().getEditor();
 			// Write commons datas
 			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.NICKNAME_SUFFIX, metaDataP.get(ServerWizard.META_NAME).toString());
 			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.USER_SUFFIX, metaDataP.get(ServerWizard.META_USERNAME).toString());
 			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.PASSWORD_SUFFIX, metaDataP.get(ServerWizard.META_PASSWORD).toString());
 			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.DSM_SUFFIX, metaDataP.get(ServerWizard.META_DSM).toString());
 
 			// Local connection
 			if (!((String) metaDataP.get(ServerWizard.META_WIFI)).equals("")) {
 				writeConnectionValues(editor, true, metaDataP);
 			}
 			// If the user also want to access to his server from internet
 			if (((Boolean) metaDataP.get(ServerWizard.META_DDNS))) {
 				writeConnectionValues(editor, false, metaDataP);
 			}
 			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.SSID_SUFFIX, metaDataP.get(ServerWizard.META_WIFI).toString());
 			editor.commit();
 			// Reload the servers list
 			reloadCurrentServers();
 		}
 		// Display a message for the end user
 		else {
 			try{
 				if (((Synodroid)getApplication()).DEBUG) Log.i(Synodroid.DS_TAG,"DownloadPreferenceActivity: No server where found by the wizard.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(DownloadPreferenceActivity.this);
 			builder.setTitle(R.string.dialog_title_information).setMessage(R.string.wizard_no_server_found).setCancelable(false).setPositiveButton(R.string.button_ok, null).create().show();
 		}
 	}
 
 	/**
 	 * 
 	 * @param editorP
 	 * @param localP
 	 * @param metaDataP
 	 */
 	private void writeConnectionValues(Editor editorP, boolean localP, HashMap<String, Object> metaDataP) {
 		String localRadical = "";
 		String host = metaDataP.get(ServerWizard.META_DDNS_NAME).toString();
 		boolean showUpload = false;
 		String refreshRate = "20";
 		// If it is a local connection then adjust some values
 		if (localP) {
 			localRadical = PreferenceFacade.WLAN_RADICAL;
 			host = metaDataP.get(ServerWizard.META_HOST).toString();
 			showUpload = true;
 			refreshRate = "5";
 			editorP.putBoolean(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.USEWIFI_SUFFIX, true);
 		} else {
 			editorP.putBoolean(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.USEEXT_SUFFIX, true);
 		}
 
 		// Verify if at least host has been set
 		if (host != null && host.length() > 0) {
 			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.PROTOCOL_SUFFIX, ((Boolean) metaDataP.get(ServerWizard.META_HTTPS)) ? "HTTPS" : "HTTP");
 			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.HOST_SUFFIX, host);
			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.PORT_SUFFIX, ((Boolean) metaDataP.get(ServerWizard.META_HTTPS)) ? "5001" : Integer.toString((Integer)metaDataP.get(ServerWizard.META_PORT)));
 			editorP.putBoolean(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.SHOWUPLOAD_SUFFIX, showUpload);
 			editorP.putBoolean(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.REFRESHSTATE_SUFFIX, true);
 			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.REFRESHVALUE_SUFFIX, refreshRate);
 		}
 	}
 
 	/**
 	 * An inner class which provide minimal information about a server
 	 */
 	class ServerInfo {
 		public boolean delete = false;
 		public String key = "";
 		public String title = "";
 		public int id = 0;
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((key == null) ? 0 : key.hashCode());
 			return result;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			ServerInfo other = (ServerInfo) obj;
 			if (key == null) {
 				if (other.key != null)
 					return false;
 			} else if (!key.equals(other.key))
 				return false;
 			return true;
 		}
 	}
 }
