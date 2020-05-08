 package com.android.settings;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.widget.Toast;
 
 public class CustomBackup extends SettingsPreferenceFragment  implements
 Preference.OnPreferenceChangeListener {
 
 	private final String JUNK_BACKUP = "junk_backup";
 	private final String JUNK_RESTORE = "junk_restore";
 	
 	Preference mBackupSettings;
 	Preference mRestoreSettings;
 
 
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.custom_backup_settings);
         
         mBackupSettings = (Preference) findPreference(JUNK_BACKUP);
 		mBackupSettings.setOnPreferenceChangeListener(this);	
         mRestoreSettings = (Preference) findPreference(JUNK_RESTORE);
 		mRestoreSettings.setOnPreferenceChangeListener(this);
 		
         File junkBackupDir = new File("/sdcard/.junk/backup/");
         junkBackupDir.mkdirs(); 
     }
 
 
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
     	
     	if (preference == mBackupSettings) {
     		BackupDialog();
     	} else if (preference == mRestoreSettings) {
     		RestoreDialog();
     	}
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }    
     
     
     
     private void BackupDialog()	{
 		
     	Builder alertDialog = new AlertDialog.Builder(getPreferenceScreen().getContext());
     	alertDialog.setTitle("Backup Junk Settings");
     	alertDialog.setMessage("Backup Junk settings and themes?");
     	alertDialog.setNegativeButton("Cancel", null);
     	alertDialog.setPositiveButton("Backup", backupDialogPositiveListener);
     	alertDialog.show();
     }
 
     private void RestoreDialog()	{
 		
     	Builder alertDialog = new AlertDialog.Builder(getPreferenceScreen().getContext());
     	alertDialog.setTitle("Restore Junk Settings");
     	alertDialog.setMessage("Restore Junk settings and themes?");
     	alertDialog.setNegativeButton("Cancel", null);
     	alertDialog.setPositiveButton("Restore", restoreDialogPositiveListener);
     	alertDialog.show();
     }       
     
       
     
     DialogInterface.OnClickListener backupDialogPositiveListener =
    		new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 			        try {
 			        	if (BackupUtils.settingsExist()) {
 			        		BackupUtils.copyBackup("data/data/com.android.settings/shared_prefs/Junk_Settings.xml",
 			        				"sdcard/.junk/backup/Junk_Settings.xml");
 			        		Toast.makeText(getActivity().getBaseContext(), "Backup Successful", Toast.LENGTH_SHORT).show();
 			        	}
 			        } catch (IOException e) {
 			        	Toast.makeText(getActivity().getBaseContext(), "ERROR Backing up settings", Toast.LENGTH_SHORT).show();
 			        	Log.e("JUNK: ","ERROR BACKING UP SETTINGS");
 			        };	
 			        
 			        
 			        try {        
 			        	if (BackupUtils.themeOneExist()) {
 			        		BackupUtils.copyBackup("data/data/com.android.settings/shared_prefs/Junk_Theme_One.xml",
 			        				"sdcard/.junk/backup/Junk_Theme_One.xml");
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR BACKING UP SETTINGS - Theme One");
 			        };	
 			        	
 			        try {        
 			        	if (BackupUtils.themeTwoExist()) {
 			        		BackupUtils.copyBackup("data/data/com.android.settings/shared_prefs/Junk_Theme_Two.xml",
 									"sdcard/.junk/backup/Junk_Theme_Two.xml");
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR BACKING UP SETTINGS - Theme Two");
 			        };	
 
 			        try {        
 			        	if (BackupUtils.themeThreeExist()) {
 			        		BackupUtils.copyBackup("data/data/com.android.settings/shared_prefs/Junk_Theme_Three.xml",
 									"sdcard/.junk/backup/Junk_Theme_Three.xml");
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR BACKING UP SETTINGS - Theme Three");
 			        };
 				}
 			};
     
     DialogInterface.OnClickListener restoreDialogPositiveListener =
             new DialogInterface.OnClickListener() {
 						
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					
 			        try {
 			        	if (BackupUtils.backupExist(
 			        			"sdcard/.junk/backup/Junk_Settings.xml")) {
 			        		BackupUtils.copyBackup("sdcard/.junk/backup/Junk_Settings.xml",
 									"data/data/com.android.settings/shared_prefs/Junk_Settings_Temp.xml");
 			        		Toast.makeText(getActivity().getBaseContext(), "Restore Successful", Toast.LENGTH_SHORT).show();
 
 			        		SharedPreferences prefTemp = getActivity().getBaseContext().getSharedPreferences("Junk_Settings_Temp", Context.MODE_PRIVATE);
 			        		SharedPreferences prefMgr = getActivity().getBaseContext().getSharedPreferences("Junk_Settings", Context.MODE_PRIVATE);
 			        		CustomSettingsUtils.GetSettings(prefTemp);
 			        		CustomSettingsUtils.WriteSettings(prefMgr);
 			        		
 			        		CustomSettingsUtils.SendIntents(getActivity().getBaseContext());
 			        		
 			        	} else {
 			        		Toast.makeText(getActivity().getBaseContext(), "No backup exists!", Toast.LENGTH_SHORT).show();	
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR RESTORING SETTINGS");
 			        };
 			        
 			        
 			        try {        
 			        	if (BackupUtils.backupExist("sdcard/.junk/backup/Junk_Theme_One.xml")) {
 			        		BackupUtils.copyBackup("sdcard/.junk/backup/Junk_Theme_One.xml",
 									"data/data/com.android.settings/shared_prefs/Junk_Theme_One.xml");
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR RESTORING Theme One");
 			        };	
 			        	
 			        try {        
 			        	if (BackupUtils.backupExist("sdcard/.junk/backup/Junk_Theme_Two.xml")) {
 			        		BackupUtils.copyBackup("sdcard/.junk/backup/Junk_Theme_Two.xml",
 									"data/data/com.android.settings/shared_prefs/Junk_Theme_Two.xml");
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR RESTORING Theme Two");
 			        };	
 
 			        try {        
 			        	if (BackupUtils.backupExist("sdcard/.junk/backup/Junk_Theme_Three.xml")) {
 			        		BackupUtils.copyBackup("sdcard/.junk/backup/Junk_Theme_Three.xml",
 									"data/data/com.android.settings/shared_prefs/Junk_Theme_Three.xml");
 			        	}
 			        } catch (IOException e) {
 			        	Log.e("JUNK: ","ERROR RESTORING Theme Three");
 			        };
 
 				}
 			};
 
 
 
			

 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 		 
 }
