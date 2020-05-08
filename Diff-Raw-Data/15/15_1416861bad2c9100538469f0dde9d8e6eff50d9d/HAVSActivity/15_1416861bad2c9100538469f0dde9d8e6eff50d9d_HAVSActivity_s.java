 /*
  * Copyright (C) 2011 Savaged-Zen
  *     Author: Mike Wielgosz <mwielgosz@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.savagedzen.szparts.activities;
 
 import com.savagedzen.szparts.R;
 import com.savagedzen.szparts.utils.ShellCommand;
 import com.savagedzen.szparts.utils.ShellCommand.CommandResult;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.widget.Toast;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 //
 // HAVS Settings
 //
 public class HAVSActivity extends PreferenceActivity implements
         Preference.OnPreferenceChangeListener {
 
     private static final String UV_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels_havs";
     private static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies"; 
 
     public static final String UV_PREF = "pref_uv";
     public static final String PROFILES_PREF = "pref_uv_profiles";
     public static final String HAVS_SOB_PREF = "pref_set_on_boot_havs";
     public static final String APPLY_PREF = "pref_apply";
 
     private static final String TAG = "HAVSSettings";
 
     private String mSetUV = "";
     private String mSetProfile = "";
     private String mUVFormat;
     private String mProfileFormat;
 
     private Preference mApply;
 
     private ListPreference mUndervolt;
     private ListPreference mProfiles;
 
     private CheckBoxPreference mSOB;
 
     private AlertDialog alertDialog;
 
     private String[] availableFrequencies;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mUVFormat = getString(R.string.uv_summary);
         mProfileFormat = getString(R.string.uv_profiles_summary);
 
         String[] availableProfiles = getResources().getStringArray(R.array.values_uv_profiles);
         String[] availableUV = getResources().getStringArray(R.array.values_uv_havs);
         availableFrequencies = readOneLine(FREQ_LIST_FILE).split(" ");
 
         setTitle(R.string.havs_title);
         addPreferencesFromResource(R.xml.havs_settings);
 
         PreferenceScreen PrefScreen = getPreferenceScreen();
 
         mApply = (Preference) PrefScreen.findPreference(APPLY_PREF);
         mSOB = (CheckBoxPreference) PrefScreen.findPreference(HAVS_SOB_PREF);
 
         // Set up the warning
         alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setTitle(R.string.performance_settings_warning_title);
         alertDialog.setMessage(getResources().getString(R.string.performance_settings_warning));
         alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 return;
             }
         });
 
         alertDialog.show();
 
         // UV Profiles radio-list
         mProfiles = (ListPreference) PrefScreen.findPreference(PROFILES_PREF);
         mProfiles.setEntryValues(availableProfiles);
         mProfiles.setEntries(availableProfiles);
         mProfiles.setValue(mSetProfile);
         mProfiles.setSummary(String.format(mProfileFormat, mSetProfile));
         mProfiles.setOnPreferenceChangeListener(this);
 
         // Undervolt radio-list
         mUndervolt = (ListPreference) PrefScreen.findPreference(UV_PREF);
         mUndervolt.setEntryValues(availableUV);
         mUndervolt.setEntries(availableUV);
         mUndervolt.setValue(mSetUV);
         mUndervolt.setSummary(String.format(mUVFormat, mSetUV));
         mUndervolt.setOnPreferenceChangeListener(this);
         
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
 
         if (newValue != null) {
             if (preference == mProfiles) {
                 mSetProfile = (String) newValue;
                 mProfiles.setSummary(String.format(mProfileFormat, mSetProfile));
                 return true;
             }
             if (preference == mUndervolt) {
                 mSetUV = (String) newValue;
                 mUndervolt.setSummary(String.format(mUVFormat, mSetUV));
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
 
         if (preference == mApply) {
                 changeHAVS(mSetProfile, mSetUV);
                 return true;
         }
 
         return false;
     }
 
     public boolean changeHAVS(String profile, String UV) {
         ShellCommand cmd = new ShellCommand();
         String filePath = "/system/etc/vdd_profiles/";
         String fileTOrun = null;
         String tmpProfile = null;
         String tmpUV = null;
 
         // Determins and sets short profile
         if ("Savaged-Zen".equals(profile)) {
             tmpProfile = "SZ";
             tmpUV = UV;
         }
         if ("SVS".equals(profile)) {
             tmpProfile = "SVS";
             tmpUV = UV;
         }
 
         // If tmpUV is empty, make null to show correct errors
         if("".equals(tmpUV)) {
             tmpUV = null;
         }
 
         // Attempt to run script based on user input
         if(tmpProfile != null && tmpUV != null) {
             fileTOrun = filePath + tmpUV + tmpProfile + ".sh";
             CommandResult r = cmd.su.runWaitFor(fileTOrun);
             if (!r.success()) {
                 Log.e(TAG, "Error " + r.stderr);
                 Log.e(TAG, "File: " + fileTOrun + " returned error: " + r.stderr);
                 Toast.makeText(this, "Error: Could not run script: " + tmpUV + tmpProfile, Toast.LENGTH_LONG).show();
                 return false;
             } else {
                 Log.i(TAG, "Successfully executed: " + fileTOrun);
                 Toast.makeText(this, "HAVS settings sucessfully changed!", Toast.LENGTH_LONG).show();
             }
 
             // Backup HAVS settings
             String fileTOwrite = "/data/customHAVS.sh";
 
             String currentHAVS[] = null;
             String lineTOwrite = "#!/system/bin/sh";
             InputStreamReader inputReader = null;
            StringBuilder data = null;
             try {
                data = new StringBuilder(2048);
                 char tmp[] = new char[2048];
                 int numRead;
                 inputReader = new FileReader(UV_PATH);
                 while ((numRead = inputReader.read(tmp)) >= 0) {
                    data.append(tmp, 0, numRead);
                 }
             } catch (IOException e) {
                 Log.e(TAG, "IOException: " + e);
             } finally {
                 try {
                     if (inputReader != null) {
                         inputReader.close();
                     }
                 } catch (IOException e) {
                     Log.e(TAG, "IOException: " + e);
                 }
             }
            String currentHAVSstring = data.toString();
             currentHAVSstring.replaceAll("\\n", " ");
             currentHAVS = currentHAVSstring.split("\\D+");
 
             try {
                 BufferedWriter bw = new BufferedWriter(new FileWriter(fileTOwrite));
                 try {
                     bw.write("#!/system/bin/sh");
                     bw.newLine(); 
                     int placement = 0;
                     for (int i = 1; i < (currentHAVS.length / 3); i++) {
                         placement = placement + 3;
                         String stringBuild = "echo " + currentHAVS[1 + placement] + " " + currentHAVS[2 + placement]
                                 + " " + currentHAVS[3 + placement] + " > " + UV_PATH;
                         bw.write(stringBuild);
                         bw.newLine();
                     }
                     bw.flush();
                 } finally {
                     bw.close();
                     Log.i(TAG, "Wrote: " + fileTOwrite);
                 }
             } catch (IOException e) {
                Log.e(TAG, "Error: Cannot write file: " + fileTOwrite + " Exception: ", e);
                Log.e(TAG, "IOException: ", e);
                 return false;
             }
 
         } else if (tmpProfile == null || tmpUV == null) {
             Log.e(TAG, "No profile or UV level selected");
             Toast.makeText(this, "No profile and/or Undervolt level selected!", Toast.LENGTH_LONG).show();
             return false;
         }
         return false;
     }
 
     public static String readOneLine(String fname) {
         BufferedReader br;
         String line = null;
 
         try {
             br = new BufferedReader(new FileReader(fname), 512);
             try {
                 line = br.readLine();
             } finally {
                 br.close();
             }
         } catch (Exception e) {
             Log.e(TAG, "IO Exception when reading /sys/ file", e);
         }
         return line;
     }
 }
