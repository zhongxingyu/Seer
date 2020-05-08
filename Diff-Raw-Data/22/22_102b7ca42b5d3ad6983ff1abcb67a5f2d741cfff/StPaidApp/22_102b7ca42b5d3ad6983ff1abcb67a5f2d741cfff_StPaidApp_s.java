 package com.dgsd.android.ShiftTracker;
 
 import android.preference.*;
 import android.text.TextUtils;
 import com.dgsd.android.ShiftTracker.Receiver.NextShiftWidgetProvider;
 import com.dgsd.android.ShiftTracker.Receiver.ShiftListWidgetProvider;
 import com.dgsd.android.ShiftTracker.Service.DbService;
 import com.dgsd.android.ShiftTracker.Util.Api;
 import com.dgsd.android.ShiftTracker.Util.Prefs;
 import com.dgsd.android.ShiftTracker.View.ListPreference;
 
 public class StPaidApp extends StApp implements DbService.OnDbEventListener, SettingsActivity.OnCreateSettingsListener {
 
     @Override
     public void onCreate() {
         super.onCreate();
         DbService.setOnDbEventListener(this);
         SettingsActivity.setOnCreateSettingsListener(this);
     }
 
     @Override
     public void onInsert() {
         ShiftListWidgetProvider.triggerUpdate(StPaidApp.this);
         NextShiftWidgetProvider.triggerUpdate(StPaidApp.this);
     }
 
     @Override
     public void onDelete() {
         ShiftListWidgetProvider.triggerUpdate(StPaidApp.this);
         NextShiftWidgetProvider.triggerUpdate(StPaidApp.this);
     }
 
     @Override
     public void onUpdate() {
         ShiftListWidgetProvider.triggerUpdate(StPaidApp.this);
         NextShiftWidgetProvider.triggerUpdate(StPaidApp.this);
     }
 
     @Override
     public void onSettingsCreated(final PreferenceActivity activity, PreferenceManager prefsManager, PreferenceScreen screen) {
        PreferenceCategory dataCategory = new PreferenceCategory(activity);
        dataCategory.setTitle(R.string.settings_category_data);
        dataCategory.setOrder(2);
        screen.addPreference(dataCategory);

        Preference exportPref = new Preference(activity);
        exportPref.setTitle(R.string.settings_title_export);
        exportPref.setSummary(R.string.settings_summary_export);
        exportPref.setKey(getString(R.string.settings_key_export));
        exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ExportDataTask.start(activity);

                return true;
            }
        });
        dataCategory.addPreference(exportPref);
 
         PreferenceCategory defaultsCateogry = (PreferenceCategory) screen.findPreference(getString(R.string.settings_category_defaults));
         Preference remindersPref = null;
         if(Api.isMin(Api.HONEYCOMB)) {
             remindersPref = new android.preference.ListPreference(activity);
             ((android.preference.ListPreference)remindersPref).setEntries(R.array.reminder_minutes_labels);
             ((android.preference.ListPreference)remindersPref).setEntryValues(R.array.reminder_minutes_labels);
         } else {
             remindersPref = new ListPreference(activity);
             ((ListPreference)remindersPref).setEntries(R.array.reminder_minutes_labels);
             ((ListPreference)remindersPref).setEntryValues(R.array.reminder_minutes_labels);
         }
 
         CharSequence val = Prefs.getInstance(this).get(getString(R.string.settings_key_default_reminder), null);
         remindersPref.setDefaultValue("None");
         remindersPref.setTitle(R.string.settings_title_default_reminder);
         remindersPref.setSummary(TextUtils.isEmpty(val) ? "None" : val);
         remindersPref.setKey(getString(R.string.settings_key_default_reminder));
         remindersPref.setOrder(4);
         remindersPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
             @Override
             public boolean onPreferenceChange(Preference preference, Object newVal) {
                 preference.setSummary(newVal.toString());
                 return true;
             }
         });
 
         defaultsCateogry.addPreference(remindersPref);
     }
 }
