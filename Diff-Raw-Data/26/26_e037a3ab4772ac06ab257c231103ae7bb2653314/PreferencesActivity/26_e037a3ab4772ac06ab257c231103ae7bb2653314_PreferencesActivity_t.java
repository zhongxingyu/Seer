 package uk.org.smithfamily.mslogger.activity;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.ExternalGPSManager;
 import uk.org.smithfamily.mslogger.GPSLocationManager;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.ecuDef.SettingGroup;
 import uk.org.smithfamily.mslogger.ecuDef.SettingGroup.SettingOption;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 
 /**
  * 
  * 
  */
 public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, LocationListener
 {
     public static final String DIRTY = "uk.org.smithfamily.mslogger.activity.PreferencesActivity.DIRTY";
     private Boolean ecuDirty = false;
     private Set<String> settingFlags = new HashSet<String>();
     private Set<String> alreadyDefined = new HashSet<String>();
 
     /**
      *
      */
     private class ECUPreferenceChangeListener implements OnPreferenceChangeListener
     {
         @Override
         public boolean onPreferenceChange(Preference preference, Object newValue)
         {
             resetECU();
             return true;
         }
     }
 
     /**
      * 
      * @param savedInstanceState
      */
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         ecuDirty = false;
 
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
 
         Preference p = this.getPreferenceManager().findPreference("temptype");
         p.setOnPreferenceChangeListener(new ECUPreferenceChangeListener());
         p = this.getPreferenceManager().findPreference("maptype");
         p.setOnPreferenceChangeListener(new ECUPreferenceChangeListener());
 
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         if (ecu != null)
         {
             PreferenceScreen ps = this.getPreferenceScreen();
 
             buildAlreadyDefined(R.array.egotypes);
             buildAlreadyDefined(R.array.maptypes);
             buildAlreadyDefined(R.array.tempvalues);
             populateProjectSettings(ps);
         }
 
         for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++)
         {
             initSummary(getPreferenceScreen().getPreference(i));
         }
     }
 
     protected void onResume()
     {
         super.onResume();
         // Set up a listener whenever a key changes
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 
         ExternalGPSManager.INSTANCE.addListener(this);
         ExternalGPSManager.INSTANCE.requestStatusUpdate();
         DebugLogManager.INSTANCE.log("onResume()", Log.DEBUG);
     }
 
     protected void onPause()
     {
         super.onPause();
         // Unregister the listener whenever a key changes
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 
         ExternalGPSManager.INSTANCE.removeListener(this);
         DebugLogManager.INSTANCE.log("onPause()", Log.DEBUG);
     }
 
     private void initSummary(Preference p)
     {
         if (p instanceof PreferenceCategory)
         {
             PreferenceCategory pCat = (PreferenceCategory) p;
             for (int i = 0; i < pCat.getPreferenceCount(); i++)
             {
                 initSummary(pCat.getPreference(i));
             }
         }
         else
         {
             updatePrefSummary(p);
         }
     }
 
     private void updatePrefSummary(Preference p)
     {
         if (p instanceof ListPreference)
         {
             ListPreference listPref = (ListPreference) p;
             p.setSummary(listPref.getEntry());
         }
 
         if (p instanceof EditTextPreference)
         {
             EditTextPreference editTextPref = (EditTextPreference) p;
             p.setSummary(editTextPref.getText());
         }
     }
 
     private void buildAlreadyDefined(int id)
     {
         String[] values = getResources().getStringArray(id);
         for (String value : values)
         {
             alreadyDefined.add(value);
         }
 
     }
 
     private void populateProjectSettings(PreferenceScreen ps)
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         List<SettingGroup> groups = ecu.getSettingGroups();
 
         if (groups == null || groups.size() == 0)
         {
             return;
         }
 
         PreferenceCategory msSettingsCat = (PreferenceCategory) ps.findPreference("MSSettings");
 
         for (SettingGroup g : groups)
         {
             ListPreference lp = new ListPreference(this);
             lp.setTitle(g.getDescription());
             lp.setKey(g.getName());
             lp.setEntries(getEntries(g));
             lp.setEntryValues(getEntryValues(g));
             msSettingsCat.addPreference(lp);
         }
         String[] flags = ecu.getControlFlags();
         Arrays.sort(flags, String.CASE_INSENSITIVE_ORDER);
 
         for (String flag : flags)
         {
             if (!settingFlags.contains(flag) && !alreadyDefined.contains(flag))
             {
                 ListPreference lp = new ListPreference(this);
                 lp.setTitle(flag);
                 lp.setKey(flag);
                 lp.setEntries(R.array.booleanTypes);
                 lp.setEntryValues(R.array.booleanDisplayValues);
                lp.setDefaultValue("false");
                 msSettingsCat.addPreference(lp);
             }
         }
 
     }
 
     private String[] getEntryValues(SettingGroup g)
     {
         List<SettingOption> options = g.getOptions();
         String[] values = new String[options.size()];
         for (int i = 0; i < options.size(); i++)
         {
             values[i] = options.get(i).getFlag();
             settingFlags.add(values[i]);
         }
 
         return values;
     }
 
     private String[] getEntries(SettingGroup g)
     {
         List<SettingOption> options = g.getOptions();
         String[] values = new String[options.size()];
         for (int i = 0; i < options.size(); i++)
         {
             values[i] = options.get(i).getDescription();
         }
 
         return values;
     }
 
     /**
      * 
      */
     @Override
     public void finish()
     {
         Intent intent = new Intent();
         intent.putExtra(DIRTY, ecuDirty);
 
         // Set result and finish this Activity
         setResult(Activity.RESULT_OK, intent);
         super.finish();
     }
 
     /**
      * 
      */
     protected void resetECU()
     {
         ApplicationSettings.INSTANCE.refreshFlags();
         ecuDirty = true;
     }
 
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
     {
         if (key.equals("externalgpsactive"))
         {
             CheckBoxPreference cbp = (CheckBoxPreference) findPreference(key);
             if (cbp.isChecked())
             {
                 ExternalGPSManager.INSTANCE.start();    
                 ExternalGPSManager.INSTANCE.addListener(GPSLocationManager.INSTANCE);       
             }
             else
             {
                 ExternalGPSManager.INSTANCE.stop();    
                 ExternalGPSManager.INSTANCE.removeListener(GPSLocationManager.INSTANCE);  
             }
         }
         else
             updatePrefSummary(findPreference(key));
     }
 
     public void onLocationChanged(Location location)
     {
     }
 
     public void onProviderDisabled(String provider)
     {
         DebugLogManager.INSTANCE.log("onProviderDisabled()", Log.DEBUG);
         runOnUiThread(new Runnable()
         {
             public void run()
             {
                 Preference p = getPreferenceManager().findPreference("externalgpsactive");
                 p.setSummary("GPS Disabled");
             }
         });
     }
 
     public void onProviderEnabled(String provider)
     {
         DebugLogManager.INSTANCE.log("onProviderEnabled()", Log.DEBUG);
         runOnUiThread(new Runnable()
         {
             public void run()
             {
                 Preference p = getPreferenceManager().findPreference("externalgpsactive");
                 p.setSummary("GPS Starting...");
             }
         });
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras)
     {
         DebugLogManager.INSTANCE.log("onStatusChanged()", Log.DEBUG);
         String s = "";
         switch (status)
         {
         case LocationProvider.AVAILABLE:
             s = "GPS Available";
             if (extras != null && extras.containsKey("satellites"))
             {
                 s += " (" + extras.get("satellites") + " sats in view)";
             }
             break;
 
         case LocationProvider.TEMPORARILY_UNAVAILABLE:
             s = "GPS Looking for satellites...";
             break;
 
         case LocationProvider.OUT_OF_SERVICE:
             s = "GPS Disabled";
             break;
         }
         final String sum = s;
         runOnUiThread(new Runnable()
         {
             public void run()
             {
                 Preference p = getPreferenceManager().findPreference("externalgpsactive");
                 p.setSummary(sum);
             }
         });
     }
 }
