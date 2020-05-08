 package com.mohammadag.xperiaflipsettings;

 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class SettingsActivity extends PreferenceActivity {
 	private boolean mDirty = false;

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		/* Consistency with Xposed, make the user feel this is part of it. */
 		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

 		super.onCreate(savedInstanceState);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mDirty) Toast.makeText(this, R.string.reboot_to_apply, Toast.LENGTH_SHORT).show();
 		finish();
 		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
 	}
 
 	@SuppressWarnings({ "deprecation" })
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
 		addPreferencesFromResource(R.xml.preferences);

 		OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				mDirty = true;
 				return true;
 			}
 		};

 		findPreference(Constants.SETTINGS_KEY_REPLACE_HANDLE_BAR).setOnPreferenceChangeListener(listener);
 		findPreference(Constants.SETTINGS_KEY_REPLACE_SETTINGS_ICON).setOnPreferenceChangeListener(listener);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			onBackPressed();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
