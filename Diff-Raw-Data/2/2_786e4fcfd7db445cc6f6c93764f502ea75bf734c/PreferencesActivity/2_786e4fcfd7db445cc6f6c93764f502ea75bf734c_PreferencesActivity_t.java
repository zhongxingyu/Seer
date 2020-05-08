 package com.danielpecos.gtdtm.activities;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.activities.tasks.LoadDataFileAsyncTask;
 import com.danielpecos.gtdtm.activities.tasks.OnFinishedListener;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Context;
 import com.danielpecos.gtdtm.model.persistence.GoogleTasksHelper;
 import com.danielpecos.gtdtm.utils.ActivityUtils;
 import com.danielpecos.gtdtm.utils.FileUtils;
 import com.danielpecos.gtdtm.utils.google.GoogleClient;
 
 public class PreferencesActivity extends PreferenceActivity {
 
 	public static final String FULL_RELOAD = "full_reload";
 	Intent resultIntent = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, true);
 
 		final SharedPreferences preferences = TaskManager.getPreferences();
 
 		final Preference clearGooglePreferences = (Preference) findPreference("settings_clear_google");
 		clearGooglePreferences.setEnabled(!preferences.getString(GoogleClient.GOOGLE_ACCOUNT_NAME, "").equalsIgnoreCase(""));
 
 		clearGooglePreferences.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
 				ActivityUtils.createConfirmDialog(PreferencesActivity.this, R.string.confirm_clear_google).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						SharedPreferences.Editor editor = preferences.edit();
 						editor.remove(GoogleClient.GOOGLE_ACCOUNT_NAME);
 						editor.commit();
 
 						clearGooglePreferences.setEnabled(false);
 						
 						TaskManager taskManager = TaskManager.getInstance(PreferencesActivity.this);
 						for (Context ctx : taskManager.getContexts()) {
 							if (ctx.getGoogleId() != null) {
 								Log.d(TaskManager.TAG, "Clearing sync data from context " + ctx.getName());
 								GoogleTasksHelper.resetSynchronizationData(ctx);
 							}
 						}
 					}
 				}).show();
 
 				return true;
 			}
 
 		});
 
 		Preference backupStore = findPreference("settings_backup_store");
 		backupStore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				String result = TaskManager.getInstance(PreferencesActivity.this).saveToFile(PreferencesActivity.this);
 				if (result != null) {
 					Toast.makeText(PreferencesActivity.this, result, Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(PreferencesActivity.this, R.string.error_unknown, Toast.LENGTH_LONG).show();
 				}
 				return true;
 			}
 		});
 
 		Preference backupRestore = findPreference("settings_backup_restore");
 		backupRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				final String[] options = FileUtils.listFilesMatching(TaskManager.SDCARD_DIR, "*.db");
 				ActivityUtils.createOptionsDialog(PreferencesActivity.this, R.string.file_loadOptions, options, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, final int elementPosition) {
 						dialog.dismiss();
 						ActivityUtils.createConfirmDialog(PreferencesActivity.this, R.string.confirm_file_load).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								loadFromFile(PreferencesActivity.this, options[elementPosition]);
 							}
 						}).show();
 					}
 				}).show();
 				return true;
 			}
 		});
 
 		if (TaskManager.isFullVersion(this)) {
 			PreferenceScreen settingsScreen = (PreferenceScreen)findPreference("settings_screen");
 			PreferenceCategory fullVersionCategory = (PreferenceCategory)findPreference("settings_category_fullVersion");
 			settingsScreen.removePreference(fullVersionCategory);
 		} else {
 			Preference fullVersion = findPreference("settings_fullVersion");
 			fullVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 				@Override
 				public boolean onPreferenceClick(Preference preference) {
 					Editor editor = preferences.edit();
 					editor.putBoolean("settings_fullVersion", false);
 					editor.commit();
 					
 					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.danielpecos.gtdtm.full"));
 					startActivity(intent);
 					return true;
 				}
 			});
 		}
 	}
 
 	public void loadFromFile(final android.content.Context ctx, String fileName) {
 		LoadDataFileAsyncTask loadDataFileAsyncTask = new LoadDataFileAsyncTask(ctx);
 		loadDataFileAsyncTask.setOnFinishedListener(new OnFinishedListener() {
 			@Override
 			public void onFinish(String response) {
 				if (response != null) {
 					resultIntent = new Intent();
 					resultIntent.putExtra(FULL_RELOAD, true);
 					Toast.makeText(ctx, response, Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(ctx, R.string.error_unknown, Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		loadDataFileAsyncTask.execute(fileName);
 	}
 
 	@Override
 	public void onBackPressed() {
 		//Handle the back button
 		this.setResult(RESULT_OK, this.resultIntent);
 		this.finish(); 
 		this.resultIntent = null;
 	}
 }
