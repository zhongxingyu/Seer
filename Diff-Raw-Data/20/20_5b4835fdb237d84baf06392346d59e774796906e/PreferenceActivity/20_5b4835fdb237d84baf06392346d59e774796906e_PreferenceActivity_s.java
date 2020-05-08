 package by.fksis.schedule.app;
 
 import android.content.Intent;
 import android.os.Bundle;
 import by.fksis.schedule.API;
 import by.fksis.schedule.Preferences;
 import by.fksis.schedule.R;
 import by.fksis.schedule.dal.Database;
 import com.WazaBe.HoloEverywhere.preference.ListPreference;
 import com.WazaBe.HoloEverywhere.preference.Preference;
 import com.WazaBe.HoloEverywhere.preference.SharedPreferences;
 import com.WazaBe.HoloEverywhere.sherlock.SPreferenceActivity;
 
import java.util.ArrayList;
 import java.util.List;
 
 @SuppressWarnings("deprecation")
 public class PreferenceActivity extends SPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 
         getSupportActionBar().setTitle(R.string.preferences);
         getSupportActionBar().setIcon(R.drawable.logo);
 
         {
             Preference preference = findPreference(getString(R.string.log_out_preference));
             preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                 @Override
                 public boolean onPreferenceClick(Preference preference) {
                    API.updateCredentials(PreferenceActivity.this, null, null);
                     Intent intent = new Intent(PreferenceActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     startActivity(intent);
                     finish();
                     return true;
                 }
             });
         }
 
         {
             ListPreference preference = (ListPreference) findPreference(getString(R.string.subgroup_preference));
             preference.setSummary(getResources().getStringArray(R.array.subgroups)[new Preferences(this).getSubgroup()]);
         }
 
         {
             ListPreference preference = (ListPreference) findPreference(getString(R.string.group_preference));
             List<String> list = Database.getGroupList();
             if (list.size() > 0) {
                 String[] groups = list.toArray(new String[list.size()]);
                 preference.setEntries(groups);
                 preference.setEntryValues(groups);
                 String group = new Preferences(this).getGroup();
                 preference.setSummary(list.contains(group) ? group : getString(R.string.group_not_selected));
             }
             preference.setEnabled(list.size() > 0);
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
         {
             ListPreference preference = (ListPreference) findPreference(getString(R.string.subgroup_preference));
             preference.setSummary(preference.getEntry());
         }
 
         {
             ListPreference preference = (ListPreference) findPreference(getString(R.string.group_preference));
             preference.setSummary(preference.getValue());
         }
     }
 }
