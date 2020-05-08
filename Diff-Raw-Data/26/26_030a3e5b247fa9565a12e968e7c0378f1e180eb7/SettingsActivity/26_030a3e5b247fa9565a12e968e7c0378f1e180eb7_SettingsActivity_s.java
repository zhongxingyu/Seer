 package tk.newk.battery;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 
 import android.os.Bundle;
 
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
import android.preference.TwoStatePreference;
 
 import android.text.InputFilter;
 
 import static tk.newk.battery.Common.*;
 import static tk.newk.common.utils.*;
 import static tk.newk.common.log.*;
 
 class IntInputFilter implements InputFilter 
 {
   //cannot set the min value filter, because we can not guarantee that by a filter
   private int m_max;
   public IntInputFilter(int max)
   {
     m_max = max;
   }
   public CharSequence filter(CharSequence source, int start, int end,
       android.text.Spanned dest, int dstart, int dend) 
   {
     if (end > start) 
     {
       String destTxt = dest.toString();
       String resultingTxt = destTxt.substring(0, dstart) 
         + source.subSequence(start, end) 
         + destTxt.substring(dend);
       try
       {
         int value = Integer.parseInt(resultingTxt);
         if (value <= m_max)
         {
           return null;
         }
       }
       catch(NumberFormatException e)
       { }
     }
     return "";
   }
 };
 
 public class SettingsActivity extends PreferenceActivity
     implements OnSharedPreferenceChangeListener
 {
   @SuppressWarnings("deprecation")
   private Preference set_integer_title(String key, String format, 
       int default_value)
   {
     int value = read_setting_int(key, default_value);
     Preference pref = findPreference(key);
     pref.setTitle(String.format(format, value));
     return pref;
   }
 
   @SuppressWarnings("deprecation")
   @Override
   public void onCreate(Bundle saveInstanceState)
   {
     super.onCreate(saveInstanceState);
     addPreferencesFromResource(R.xml.preferences);
     getPreferenceScreen().getSharedPreferences()
       .registerOnSharedPreferenceChangeListener(this);
     InputFilter[] filters = new InputFilter[1];
     filters[0] = new IntInputFilter(500);
     ((EditTextPreference)findPreference(PREF_KEY_LIST_SIZE))
       .getEditText().setFilters(filters);
   }
 
   @SuppressWarnings("deprecation")
   @Override
   protected void onResume() 
   {
     super.onResume();
     Preference pref = findPreference(PREF_KEY_SERVICE_ENABLE);
    ((TwoStatePreference)pref).setChecked(
         read_setting_boolean(PREF_KEY_SERVICE_ENABLE, true));
     pref = findPreference(PREF_KEY_TTS_ENABLE); 
    ((TwoStatePreference)pref).setChecked(
         read_setting_boolean(PREF_KEY_TTS_ENABLE, false));
     set_integer_title(PREF_KEY_LIST_SIZE, 
         getString(R.string.pref_list_size) + ": %d", 200);
     set_integer_title(PREF_KEY_INTERVAL,
         getString(R.string.pref_interval) + ": %d minute(s)", 5);
   }
 
   @SuppressWarnings("deprecation")
   @Override
   protected void onDestroy() 
   {
     super.onDestroy();
     getPreferenceScreen().getSharedPreferences()
       .unregisterOnSharedPreferenceChangeListener(this);
   } 
 
   private int check_range(SharedPreferences sharePreferences, 
       String key, int default_value, int min, int max)
   {
     int value = Integer.parseInt(sharePreferences.getString(key, 
           Integer.toString(default_value)));
     int new_value = value;
     if (value < min)
       new_value = min;
     if (value > max)
       new_value = max;
     if (value != new_value)
     {
       SharedPreferences.Editor editor = sharePreferences.edit();
       editor.putString(key, str(new_value));
       editor.commit();
       value = new_value;
     }
     return value;
   }
 
   public void onSharedPreferenceChanged(SharedPreferences sharePreferences, 
       String key)
   {
     logv(this, "key", key);
     if (key.equals(PREF_KEY_SERVICE_ENABLE))
     {
       boolean service_enable = sharePreferences.getBoolean(key, true);
       if (service_enable)
       {
         Intent intent = new Intent(this, MonitorService.class);
         startService(intent);
       }
       else
       {
         Intent intent = new Intent(this, MonitorService.class);
         stopService(intent);
       }
     }
     else if (key.equals(PREF_KEY_LIST_SIZE))
     {
       int list_size = check_range(sharePreferences, key, 200, 50, 500);
       set_integer_title(PREF_KEY_LIST_SIZE, 
           getString(R.string.pref_list_size) + ": %d", 200);
       logv(this, "list_size change to", str(list_size));
     }
     else if (key.equals(PREF_KEY_INTERVAL))
     {
       int interval = Integer.parseInt(sharePreferences.getString(key, "5"));
       set_integer_title(PREF_KEY_INTERVAL,
           getString(R.string.pref_interval) + ": %d minute(s)", 5);
       logv(this, "interval change to", str(interval));
     }
   }
 
 }
 
 // vim: fdm=syntax fdl=1 fdn=2
 
