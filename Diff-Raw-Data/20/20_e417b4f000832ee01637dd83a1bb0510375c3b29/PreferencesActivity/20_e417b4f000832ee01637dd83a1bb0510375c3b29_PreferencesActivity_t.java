 /**
  * RxDroid - A Medication Reminder
  * Copyright (C) 2011-2013 Joseph Lehner <joseph.c.lehner@gmail.com>
  *
  *
  * RxDroid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * RxDroid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with RxDroid.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  */
 
 package at.jclehner.rxdroid;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageManager;
 import android.content.res.AssetManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceGroup;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.widget.Toast;
 import at.jclehner.rxdroid.db.Database;
 import at.jclehner.rxdroid.db.DatabaseHelper;
 import at.jclehner.rxdroid.db.Drug;
 import at.jclehner.rxdroid.db.Schedule;
 import at.jclehner.rxdroid.util.CollectionUtils;
 import at.jclehner.rxdroid.util.DateTime;
 import at.jclehner.rxdroid.util.Util;
 
 @SuppressWarnings("deprecation")
 public class PreferencesActivity extends PreferenceActivityBase implements
 		OnSharedPreferenceChangeListener, OnPreferenceClickListener, OnPreferenceChangeListener
 {
 	private static final String TAG = PreferencesActivity.class.getSimpleName();
 
 	private static final String[] KEEP_DISABLED = {
 		Settings.Keys.VERSION, Settings.Keys.DB_STATS
 	};
 
 	private static final int MENU_RESTORE_DEFAULTS = 0;
 
 	SharedPreferences mSharedPreferences;
 
 	@TargetApi(11)
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		mSharedPreferences = getPreferenceManager().getSharedPreferences();
 		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
 		addPreferencesFromResource(R.xml.preferences);
 
 		Preference p = findPreference(Settings.Keys.VERSION);
 		if(p != null)
 		{
 			final int format = BuildConfig.DEBUG ? Version.FORMAT_FULL : Version.FORMAT_SHORT;
 			String version = Version.get(format) + ", DB v" + DatabaseHelper.DB_VERSION;
 
 			if(BuildConfig.DEBUG)
 			{
 				try
 				{
 					final String apkModDate = new Date(new File(getPackageCodePath()).lastModified()).toString();
 					version = version + "\n" + apkModDate;
 				}
 				catch(NullPointerException e)
 				{
 					// eat
 				}
 			}
 			else
 			{
 				version = getString(R.string.app_name) + " " + version + "\n" +
 						"Copyright (C) 2011-2013 Joseph Lehner\n" +
 						"<joseph.c.lehner@gmail.com>";
 			}
 
 			p.setSummary(version);
 
 			p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 				@Override
 				public boolean onPreferenceClick(Preference preference)
 				{
 					final Intent intent = new Intent(Intent.ACTION_SEND);
 					intent.setType("plain/text");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "joseph.c.lehner+rxdroid-feedback@gmail.com" });
					intent.putExtra(Intent.EXTRA_SUBJECT, "RxDroid");
 
					try
					{
						startActivity(intent);
					}
					catch(ActivityNotFoundException e)
					{
						// Happens if no mail client is installed
					}

					return true;
 				}
 			});
 		}
 
 		p = findPreference(Settings.Keys.HISTORY_SIZE);
 		if(p != null)
 			Util.populateListPreferenceEntryValues(p);
 
 		p = findPreference(Settings.Keys.LICENSES);
 		if(p != null)
 			p.setOnPreferenceClickListener(this);
 
 		p = findPreference(Settings.Keys.THEME_IS_DARK);
 		if(p != null)
 			p.setOnPreferenceChangeListener(this);
 
 		p = findPreference(Settings.Keys.NOTIFICATION_LIGHT_COLOR);
 		if(p != null)
 			p.setOnPreferenceChangeListener(this);
 
 		p = findPreference(Settings.Keys.DONATE);
 		if(p != null)
 		{
 			final String uriString;
 			final int titleResId;
 			final String summary;
 
 			if(/*BuildConfig.DEBUG ||*/ Util.wasInstalledViaGooglePlay())
 			{
 				// Google Play doesn't allow donations using PayPal,
 				// so we show a link to the project's website instead.
 				uriString = "http://code.google.com/p/rxdroid";
 				titleResId = R.string._title_website;
 				summary = uriString;
 			}
 			else
 			{
 				uriString = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QTBC4AVEYASYU";
 				titleResId = R.string._title_donate;
 				summary = null;
 			}
 
 			final Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse(uriString));
 
 			p.setIntent(intent);
 			p.setEnabled(true);
 			p.setTitle(titleResId);
 			p.setSummary(summary);
 		}
 
 		p = findPreference(Settings.Keys.DB_STATS);
 		if(p != null)
 		{
 			final long millis = Database.getLoadingTimeMillis();
 			final String str = new Formatter((Locale) null).format("%1.3fs", millis / 1000f).toString();
 			p.setSummary(getString(R.string._msg_db_stats, str));
 		}
 
 		removeDisabledPreferences(getPreferenceScreen());
 		maybeAddDebugPreferences();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		updateLowSupplyThresholdPreferenceSummary();
 	}
 
 	@TargetApi(11)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuItem item = menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string._title_factory_reset)
 				.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 
 		if(Version.SDK_IS_HONEYCOMB_OR_NEWER)
 			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 			case MENU_RESTORE_DEFAULTS:
 				showDialog(R.id.preference_reset_dialog);
 				return true;
 
 			default:
 				// ignore
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
 	{
 		if(Settings.Keys.LOW_SUPPLY_THRESHOLD.equals(key))
 			updateLowSupplyThresholdPreferenceSummary();
 		else if(Settings.Keys.HISTORY_SIZE.equals(key))
 		{
 			if(Settings.getStringAsInt(Settings.Keys.HISTORY_SIZE, -1) >= Settings.Enums.HISTORY_SIZE_6M)
 				Toast.makeText(getApplicationContext(), R.string._toast_large_history_size, Toast.LENGTH_LONG).show();
 		}
 		else if(Settings.Keys.LAST_MSG_HASH.equals(key))
 			return;
 
 		NotificationReceiver.rescheduleAlarmsAndUpdateNotification(true);
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference preference)
 	{
 		if(Settings.Keys.LICENSES.equals(preference.getKey()))
 		{
 			showDialog(R.id.licenses_dialog);
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue)
 	{
 		final String key = preference.getKey();
 
 		if(Settings.Keys.THEME_IS_DARK.equals(key))
 		{
 			Theme.clearAttributeCache();
 
 			final Context context = RxDroid.getContext();
 
 			RxDroid.toastLong(R.string._toast_theme_changed);
 
 			final PackageManager pm = context.getPackageManager();
 			final Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 			RxDroid.doStartActivity(intent);
 
 			finish();
 		}
 		else if(Settings.Keys.NOTIFICATION_LIGHT_COLOR.equals(key))
 		{
 			final String value = (String) newValue;
 			if(!("".equals(value) || "0".equals(value)))
 			{
 				if(!Settings.wasDisplayedOnce("custom_led_color"))
 				{
 					RxDroid.toastLong(R.string._toast_custom_led_color);
 					Settings.setDisplayedOnce("custom_led_color");
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		if(id == R.id.licenses_dialog)
 		{
 			String license;
 			InputStream is = null;
 
 			try
 			{
 				final AssetManager aMgr = getResources().getAssets();
 				is = aMgr.open("licenses.html", AssetManager.ACCESS_BUFFER);
 
 				license = Util.streamToString(is);
 			}
 			catch(IOException e)
 			{
 				Log.w(TAG, e);
 				license = "Licensed under the GNU GPLv3";
 			}
 			finally
 			{
 				Util.closeQuietly(is);
 			}
 
 			final WebView wv = new WebView(this);
 			wv.loadDataWithBaseURL("file", license, "text/html", null, null);
 
 			final AlertDialog.Builder ab = new AlertDialog.Builder(this);
 			ab.setTitle(R.string._title_licenses);
 			ab.setView(wv);
 			ab.setPositiveButton(android.R.string.ok, null);
 
 			return ab.create();
 		}
 		else if(id == R.id.preference_reset_dialog)
 		{
 			final AlertDialog.Builder ab= new AlertDialog.Builder(this);
 			ab.setIcon(android.R.drawable.ic_dialog_alert);
 			ab.setTitle(R.string._title_restore_default_settings);
 			ab.setNegativeButton(android.R.string.cancel, null);
 			/////////////////////
 			ab.setPositiveButton(android.R.string.ok, new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 					mSharedPreferences.edit().clear().commit();
 				}
 			});
 
 			return ab.create();
 		}
 
 		return super.onCreateDialog(id);
 	}
 
 	@Override
 	protected Intent getHomeButtonIntent()
 	{
 		Intent intent = new Intent(getBaseContext(), DrugListActivity.class);
 		intent.setAction(Intent.ACTION_MAIN);
 		return intent;
 	}
 
 	private void updateLowSupplyThresholdPreferenceSummary()
 	{
 		Preference p = findPreference(Settings.Keys.LOW_SUPPLY_THRESHOLD);
 		if(p != null)
 		{
 			String value = mSharedPreferences.getString(Settings.Keys.LOW_SUPPLY_THRESHOLD, "10");
 			p.setSummary(getString(R.string._summary_min_supply_days, value));
 		}
 	}
 
 	private void removeDisabledPreferences(PreferenceGroup root)
 	{
 		final List<Preference> toRemove = new ArrayList<Preference>();
 
 		for(int i = 0; i != root.getPreferenceCount(); ++i)
 		{
 			final Preference p = root.getPreference(i);
 
 			if(CollectionUtils.contains(KEEP_DISABLED, p.getKey()))
 				continue;
 
 			if(p instanceof PreferenceGroup)
 				removeDisabledPreferences((PreferenceGroup) p);
 			else if(!p.isEnabled())
 				toRemove.add(p);
 		}
 
 		for(Preference p : toRemove)
 			root.removePreference(p);
 	}
 
 	private void maybeAddDebugPreferences()
 	{
 		if(!BuildConfig.DEBUG)
 			return;
 
 		addPreferencesFromResource(R.xml.debug_preferences);
 
 		Preference p = findPreference("debug_dump_db");
 		if(p != null)
 		{
 			p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 				@Override
 				public boolean onPreferenceClick(Preference preference)
 				{
 					try
 					{
 						final JSONObject json = Database.exportDatabaseToJson();
 						Database.importDatabaseFromJson(json);
 					}
 					catch(JSONException e)
 					{
 						Log.w(TAG, e);
 					}
 
 					return true;
 				}
 			});
 		}
 
 		p = findPreference("db_create_drug_with_schedule");
 		if(p != null)
 		{
 			p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 				@Override
 				public boolean onPreferenceClick(Preference preference)
 				{
 					final int drugCount = Database.countAll(Drug.class);
 
 					Fraction dose = new Fraction(1, 2);
 
 					Schedule schedule = new Schedule();
 					schedule.setDose(Schedule.TIME_MORNING, dose);
 					schedule.setDose(Schedule.TIME_EVENING, dose);
 
 					schedule.setBegin(DateTime.date(2013, 01, 01));
 					schedule.setEnd(DateTime.date(2013, 01, 14));
 
 					Drug drug = new Drug();
 					drug.setName("Drug #" + (drugCount + 1));
 					drug.addSchedule(schedule);
 					drug.setRepeatMode(Drug.REPEAT_CUSTOM);
 					drug.setActive(true);
 
 					Database.create(drug);
 					schedule.setOwner(drug);
 					Database.create(schedule);
 
 					return true;
 				}
 			});
 		}
 	}
 }
