 /*
  *  Copyright (C) 2012 Simon Robinson
  * 
  *  This file is part of Com-Me.
  * 
  *  Com-Me is free software; you can redistribute it and/or modify it 
  *  under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation; either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  Com-Me is distributed in the hope that it will be useful, but WITHOUT 
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
  *  Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with Com-Me.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ac.robinson.mediatablet.activity;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 
 import ac.robinson.mediatablet.MediaTablet;
 import ac.robinson.mediatablet.R;
 import ac.robinson.mediatablet.view.HomesteadSurfaceView;
 import ac.robinson.mediautilities.SelectDirectoryActivity;
 import ac.robinson.util.DebugUtilities;
 import ac.robinson.util.IOUtilities;
 import ac.robinson.util.StringUtilities;
 import ac.robinson.util.UIUtilities;
 import ac.robinson.util.UIUtilities.ReflectionTab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 public class PreferencesActivity extends PreferenceActivity {
 
 	// @SuppressWarnings("deprecation") because until we move to fragments this is the only way to provide custom
 	// formatted preferences (PreferenceFragment is not in the compatibility library)
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		UIUtilities.setPixelDithering(getWindow());
 		UIUtilities.configureActionBar(this, true, false, R.string.title_preferences, 0);
 		UIUtilities.addActionBarTabs(this, new ReflectionTab[] { new ReflectionTab(R.id.intent_preferences,
 				android.R.drawable.ic_menu_preferences, getString(R.string.title_preferences), true) }, null);
 		addPreferencesFromResource(R.xml.preferences);
 
 		// set up select bluetooth directory option
 		Preference button = (Preference) findPreference(getString(R.string.key_bluetooth_directory));
 		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				SharedPreferences mediaTabletSettings = preference.getSharedPreferences();
 				String currentDirectory = null;
 				try {
 					currentDirectory = mediaTabletSettings.getString(getString(R.string.key_bluetooth_directory), null);
 				} catch (Exception e) {
 				}
 				if (currentDirectory == null) {
 					currentDirectory = getString(R.string.default_bluetooth_directory);
 					if (!new File(currentDirectory).exists()) {
 						currentDirectory = getString(R.string.default_bluetooth_directory_alternative);
 						if (!new File(currentDirectory).exists() && IOUtilities.externalStorageIsReadable()) {
 							currentDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
 						} else {
 							currentDirectory = "/"; // default to storage root
 						}
 					}
 				}
 				final Intent intent = new Intent(getBaseContext(), SelectDirectoryActivity.class);
 				intent.putExtra(SelectDirectoryActivity.START_PATH, currentDirectory);
 				startActivityForResult(intent, R.id.intent_directory_chooser);
 				return true;
 			}
 		});
 
 		// set up the change panorama option
 		Preference panoramaButton = (Preference) findPreference(getString(R.string.key_change_panorama_image));
 		panoramaButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				LayoutInflater inflater = LayoutInflater.from(PreferencesActivity.this);
 				final View textEntryView = inflater.inflate(R.layout.password_input, null);
 				AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
 				builder.setMessage(R.string.panorama_change_password_prompt).setCancelable(false)
 						.setView(textEntryView)
 						.setPositiveButton(R.string.panorama_change_prompt, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								if (MediaTablet.ADMINISTRATOR_PASSWORD.equals(StringUtilities
 										.sha1Hash(((EditText) textEntryView.findViewById(R.id.text_password_entry))
 												.getText().toString()))) {
 									// remove the existing panorama image
 									SharedPreferences panoramaSettings = getSharedPreferences(
 											MediaTablet.APPLICATION_NAME, Context.MODE_PRIVATE);
 									SharedPreferences.Editor prefsEditor = panoramaSettings.edit();
 									prefsEditor.putString(getString(R.string.key_panorama_file), null);
 									prefsEditor.apply();
 
 									// remove existing cached images just in case
 									HomesteadSurfaceView.forceImageReload();
 
 									// clear all activities and reload the homestead browser
 									Intent reloadIntent = new Intent(PreferencesActivity.this,
 											HomesteadBrowserActivity.class);
 									reloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 									startActivity(reloadIntent);
 									finish();
 								} else {
 									UIUtilities.showToast(PreferencesActivity.this,
 											R.string.panorama_change_password_incorrect);
 								}
 							}
						}).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 				builder.show();
 				return true;
 			}
 		});
 
 		// add the contact us button
 		PreferenceScreen preferenceScreen = getPreferenceScreen();
 		Preference contactUsPreference = preferenceScreen.findPreference(getString(R.string.key_contact_us));
 		contactUsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 				emailIntent.setType("plain/text");
 				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
 						new String[] { getString(R.string.preferences_contact_us_email_address) });
 				emailIntent.putExtra(
 						android.content.Intent.EXTRA_SUBJECT,
 						getString(R.string.preferences_contact_us_email_subject, getString(R.string.app_name),
 								SimpleDateFormat.getDateTimeInstance().format(new java.util.Date())));
 				Preference aboutPreference = findPreference(getString(R.string.key_about_application));
 				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
 						getString(R.string.preferences_contact_us_email_body, aboutPreference.getSummary()));
 				try {
 					startActivity(Intent.createChooser(emailIntent, getString(R.string.preferences_contact_us_title)));
 				} catch (ActivityNotFoundException e) {
 					UIUtilities.showFormattedToast(PreferencesActivity.this,
 							R.string.preferences_contact_us_email_error,
 							getString(R.string.preferences_contact_us_email_address));
 				}
 				return true;
 			}
 		});
 
 		// add version and build information
 		Preference aboutPreference = preferenceScreen.findPreference(getString(R.string.key_about_application));
 		try {
 			PackageManager manager = this.getPackageManager();
 			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
 			aboutPreference.setTitle(getString(R.string.preferences_about_app_title, getString(R.string.app_name),
 					info.versionName));
 			aboutPreference.setSummary(getString(R.string.preferences_about_app_summary, info.versionCode,
 					DebugUtilities.getApplicationBuildTime(getPackageManager(), getPackageName()),
 					DebugUtilities.getDeviceDebugSummary(getWindowManager(), getResources())));
 		} catch (Exception e) {
 			PreferenceCategory aboutCategory = (PreferenceCategory) preferenceScreen
 					.findPreference(getString(R.string.key_about_category));
 			aboutCategory.removePreference(aboutPreference);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.save, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 			case R.id.menu_save:
 				onBackPressed();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
 		switch (requestCode) {
 			case R.id.intent_directory_chooser:
 				if (resultCode == Activity.RESULT_OK && resultIntent != null) {
 					String resultPath = resultIntent.getStringExtra(SelectDirectoryActivity.RESULT_PATH);
 					if (resultPath != null) {
 						File newPath = new File(resultPath);
 						if (newPath.canRead()) {
 							SharedPreferences mediaTabletSettings = PreferenceManager
 									.getDefaultSharedPreferences(PreferencesActivity.this);
 							SharedPreferences.Editor prefsEditor = mediaTabletSettings.edit();
 							prefsEditor.putString(getString(R.string.key_bluetooth_directory), resultPath);
 							prefsEditor.apply();
 						} else {
 							UIUtilities.showToast(PreferencesActivity.this,
 									R.string.preferences_bluetooth_directory_error);
 						}
 					}
 				}
 				break;
 
 			default:
 				super.onActivityResult(requestCode, resultCode, resultIntent);
 		}
 	}
 }
