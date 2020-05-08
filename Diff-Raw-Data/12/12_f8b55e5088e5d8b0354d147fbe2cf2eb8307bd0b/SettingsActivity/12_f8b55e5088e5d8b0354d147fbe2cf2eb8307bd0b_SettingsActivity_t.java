 /* The following code was written by Menny Even Danan
  * and is released under the APACHE 2.0 license
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.menny.android.boxeethumbremote;
 
 import java.util.ArrayList;
 import java.util.WeakHashMap;
 
 import com.menny.android.boxeethumbremote.R;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // See:
 // http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=blob;f=src/com/android/settings/wifi/WifiSettings.java;h=cac77e3251fde365f3e32463f4c44712fd0b1944;hb=HEAD
 
 /**
  * Handles preference storage for BoxeeRemote.
  */
 public class SettingsActivity extends PreferenceActivity implements
 		DiscovererThread.Receiver, BoxeeRemote.ErrorHandler,
 		OnPreferenceClickListener, OnSharedPreferenceChangeListener {
 	/**
 	 * private constants
 	 */
 	private static final int DIALOG_CUSTOM = 1;
 
 	private WeakHashMap<String, BoxeeServer> mServers;
 	private PreferenceScreen mServersScreen;
 	private Settings mSettings;
 
 	public SettingsActivity() {
 		mServers = new WeakHashMap<String, BoxeeServer>();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mSettings = new Settings(this);
 
 		addPreferencesFromResource(R.layout.preferences);
 
 		onCreatePreferences();
 
 		DiscovererThread discoverer = new DiscovererThread(this, this);
 		setProgress(true);
 		discoverer.start();
 	}
 
 	@Override
 	protected void onPause() {
 		mSettings.unlisten(this);
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mSettings.listen(this);
 	}
 
 	private void setProgress(boolean b) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void onCreatePreferences() {
 		mServersScreen = (PreferenceScreen) getPreferenceScreen()
 				.findPreference(Settings.SERVER_NAME_KEY);
 
 		mServersScreen.setSummary(mSettings.getServerName());
 
 		Preference preference = new Preference(this);
 		preference.setTitle(getText(R.string.custom_server));
 		preference.setOrder(1000);
 		preference.setOnPreferenceClickListener(this);
 		mServersScreen.addPreference(preference);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		if (id != DIALOG_CUSTOM)
 			return null;
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		final View layout = inflater.inflate(R.layout.custom_host,
 				(ViewGroup) findViewById(R.id.layoutCustomHost));
 		((TextView) layout.findViewById(R.id.textAddress)).setText(mSettings
 				.getHost());
 		((TextView) layout.findViewById(R.id.textPort)).setText(new Integer(mSettings
 				.getPort()).toString());
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setPositiveButton(android.R.string.ok,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						String address = ((TextView) layout
 								.findViewById(R.id.textAddress)).getText()
 								.toString();
 
 						int port = Integer.parseInt(((TextView) layout
 								.findViewById(R.id.textPort)).getText()
 								.toString());
 
 						mSettings.putServer(address, port, "custom", false, true);
 						mServersScreen.getDialog().dismiss();
 					}
 				});
 
 		builder.setNegativeButton(android.R.string.cancel, null);
 		builder.setTitle(R.string.custom_server);
		builder.setIcon(R.drawable.app_icon);
 
 		return builder.create();
 	}
 
 	@Override
 	public void addAnnouncedServers(ArrayList<BoxeeServer> servers) {
 		for (BoxeeServer server : servers) {
 			Preference preference = new Preference(this);
 			preference.setOrder(mServers.size());
 			preference.setTitle(server.name());
 			preference.setOnPreferenceClickListener(this);
 			mServersScreen.addPreference(preference);
 			mServers.put(server.name(), server);
 		}
 		setProgress(false);
 	}
 
 	@Override
 	public void ShowError(int id, boolean longDelay) {
 	}
 
 	@Override
 	public void ShowError(String s, boolean longDelay) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference preference) {
 		BoxeeServer server = mServers.get(preference.getTitle());
 
 		if (server == null) {
 			showDialog(DIALOG_CUSTOM);
 			return true;
 		}
 
 		mSettings.putServer(server, false);
 		mServersScreen.getDialog().dismiss();
 
 		return true;
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals(Settings.SERVER_NAME_KEY)) {
 			String value = mSettings.getServerName();
 			Toast.makeText(this, "preference changed: " + value, 5000);
 			mServersScreen.setSummary(value);
 		}
 	}
 
 }
