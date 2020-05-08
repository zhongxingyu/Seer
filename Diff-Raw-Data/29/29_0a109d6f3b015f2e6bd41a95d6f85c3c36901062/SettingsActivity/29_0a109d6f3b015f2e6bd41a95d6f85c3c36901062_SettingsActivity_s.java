 package de.team06.psychoapp;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import java.util.Calendar;
 
 /**
  * Created by Kevin on 03.07.13. It´s so fucking awesome.
  */
 public class SettingsActivity extends PreferenceActivity {
 
     private SharedPreferences preferences;
     private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         myPrefListner = new SharedPreferences.OnSharedPreferenceChangeListener() {
             public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                 // ProbandenCode was changed
                 if (key.equals("code")) {
                    // Create first socialInteraction in database with current time
                    DatabaseModel dbModel = new DatabaseModel(getApplicationContext());
                    dbModel.open();
                    SocialInteraction newSocialInteraction = dbModel.createSocialInteraction(System.currentTimeMillis(), preferences.getString("code", ""));
                    dbModel.close();

                    setStatusProbandencodeButton();
                 }
             }
         };
 
         // Register OnSharedPreferenceChangeListener
         PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(myPrefListner);
 
         addPreferencesFromResource(R.xml.preferences);
         preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 
         /*ProbandenCode Ceck-Routine*/
         setStatusProbandencodeButton();
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.settings, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.action_setDemoAlarm:
                 setTestAlarm();
                 break;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onPause() {
         setAlarm();
 
         if (preferences.getBoolean("firstRun", true) == true) {
             SharedPreferences.Editor editor = preferences.edit();
             editor.putBoolean("firstRun", false);
             editor.commit();
         }
 
         //PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).registerOnSharedPreferenceChangeListener(myPrefListner);
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         // Register OnSharedPreferenceChangeListener
         PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(myPrefListner);
         setStatusProbandencodeButton();
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         setStatusProbandencodeButton();
     }
 
     private void setStatusProbandencodeButton() {
     /*ProbandenCode Ceck-Routine*/
         String code = preferences.getString("code", "");
         if (code.length() != 0) {
             this.findPreference("code").setEnabled(false);
        }
        else
             this.findPreference("code").setEnabled(true);
     }
 
     public void setAlarm() {
 
         int alarm1 = Integer.parseInt(preferences.getString("alarm_morgen", "-1"));
         int alarm2 = Integer.parseInt(preferences.getString("alarm_mittag", "-1"));
         int alarm3 = Integer.parseInt(preferences.getString("alarm_nachmittag", "-1"));
         int alarm4 = Integer.parseInt(preferences.getString("alarm_abend", "-1"));
 
         Calendar cal1 = Calendar.getInstance();
 
         if (cal1.get(Calendar.HOUR_OF_DAY) >= alarm1)
             cal1.add(Calendar.DATE, 1);
         cal1.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH), alarm1, 0);
 
         Calendar cal2 = Calendar.getInstance();
         if (cal2.get(Calendar.HOUR_OF_DAY) >= alarm2)
             cal2.add(Calendar.DATE, 1);
         cal2.set(cal2.get(Calendar.YEAR), cal2.get(Calendar.MONTH), cal2.get(Calendar.DAY_OF_MONTH), alarm2, 0);
 
         Calendar cal3 = Calendar.getInstance();
         if (cal3.get(Calendar.HOUR_OF_DAY) >= alarm3)
             cal3.add(Calendar.DATE, 1);
         cal3.set(cal3.get(Calendar.YEAR), cal3.get(Calendar.MONTH), cal3.get(Calendar.DAY_OF_MONTH), alarm3, 0);
 
         Calendar cal4 = Calendar.getInstance();
         if (cal4.get(Calendar.HOUR_OF_DAY) >= alarm4)
             cal4.add(Calendar.DATE, 1);
         cal4.set(cal4.get(Calendar.YEAR), cal4.get(Calendar.MONTH), cal4.get(Calendar.DAY_OF_MONTH), alarm4, 0);
 
         AlarmMaker alarmMaker = new AlarmMaker(this.getApplicationContext());
         alarmMaker.addAlarm(cal1.getTimeInMillis(), TimeSection.FIRST_QUARTER);
         alarmMaker.addAlarm(cal2.getTimeInMillis(), TimeSection.SECOND_QUARTER);
         alarmMaker.addAlarm(cal3.getTimeInMillis(), TimeSection.THIRD_QUARTER);
         alarmMaker.addAlarm(cal4.getTimeInMillis(), TimeSection.FOURTH_QUARTER);
 
     }
 
     public void setTestAlarm() {
         /*
             test alarm maker
          */
         AlarmMaker testAlarm = new AlarmMaker(getApplicationContext());
         testAlarm.addAlarm(System.currentTimeMillis() + 10000, TimeSection.FOURTH_QUARTER);
 
         Toast.makeText(this.getApplicationContext(), "Demo Alarm gesetzt", Toast.LENGTH_LONG).show();
     }
 
 }
