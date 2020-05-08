 package com.enrix835.ftpsentinel;
 
 import java.io.File;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.widget.Toast;
 
 public class FtpsentinelActivity extends PreferenceActivity {
 	
 	public final String FTP_DATA = "ftpdata";
 	
 	public boolean getWelcomeMsg;
 	
 	public EditTextPreference hostEdit;
     public EditTextPreference usernameEdit;
     public EditTextPreference passwordEdit;
     public EditTextPreference portEdit;
     public EditTextPreference directoryEdit;
     public CheckBoxPreference welcomeCheck;
     public EditTextPreference timerButton;
     public Preference connectButton;
     public Preference fileListButton;
     public Preference manualCheckButton;
     public Preference logoutButton;
     
     public Resources res;
     public Ftp newFtp;
     public Utils utils = new Utils();
     public Alert alert = new Alert(this);
     public NotificationMessage nMsg = new NotificationMessage(this);
     public String dateFile;
     public String hostname;
     
     public String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
     public final String fileList = externalStoragePath + "/.ftpsentinelList";
     public final String newFileList = externalStoragePath + "/.ftpsentinelList.new";
     public int timer;
     public Servizio checkService;
     
     public Intent intent;
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		
 		boolean isFirstTime = false;
 		
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.layout.layout);
         
         intent = new Intent(this, Servizio.class);
         res = getResources();
 
         dateFile = utils.getFileDate(fileList);
         
         hostEdit = (EditTextPreference) findPreference("host");
         usernameEdit = (EditTextPreference) findPreference("username");
         passwordEdit = (EditTextPreference) findPreference("password");
         portEdit = (EditTextPreference) findPreference("port");
         directoryEdit = (EditTextPreference) findPreference("directory");
         
         welcomeCheck = (CheckBoxPreference) findPreference("welcomeCheck");
         
         timerButton = (EditTextPreference) findPreference("timerButton");
         
         connectButton = findPreference("connectButton");
 		connectButton.setOnPreferenceClickListener(connectListener);
 		
 		fileListButton = findPreference("getListButton");
 		fileListButton.setSummary(res.getString(R.string.nextUpdates) + res.getString(R.string.getNewFileListSummary)+ " " + dateFile);
 		fileListButton.setOnPreferenceClickListener(fileListListener);
 		
 		manualCheckButton = findPreference("manualCheckButton");
 		manualCheckButton.setOnPreferenceClickListener(manualCheckListener);
 		
 		logoutButton = findPreference("disconnectButton");
 		logoutButton.setOnPreferenceClickListener(logoutListener);
 		
 		if(isFirstTime = !(new File(fileList).isFile())){
 			alert.createAlert(res.getString(R.string.firstTime), res.getString(R.string.firstTimeSummary), "OK").show();
 			//isFirstTime = true;
 		}
 		intent.putExtra("isFirstTime", isFirstTime);
     }
 
 	OnPreferenceClickListener connectListener = new OnPreferenceClickListener() {
 		
 		private String hostname;
 		private String username;
 		private String password;
 		private String directory;
 		private int port;
 
 		public boolean onPreferenceClick(Preference preference) {
 			
 	        SharedPreferences prefs = getSharedPreferences(FTP_DATA, Context.MODE_PRIVATE);
 	        SharedPreferences.Editor editor = prefs.edit();
 	        
 			hostname = hostEdit.getText();
 			username = usernameEdit.getText();
 			password = passwordEdit.getText();
 			directory = directoryEdit.getText();
 			port = Integer.parseInt(portEdit.getText());
 			timer = Integer.parseInt(timerButton.getText());
 			
 			if(welcomeCheck.isChecked()) {
 				getWelcomeMsg = true;
 			}
 			
 			if(hostname == null || username == null || password == null) {
 				alert.createAlert(res.getString(R.string.error), res.getString(R.string.allEntry), "OK").show();
 			} else {
 				if(timer < 60000) {
 					Toast.makeText(getBaseContext(), R.string.warningLowInterval, Toast.LENGTH_LONG).show();
 				}
 				
 				String IP = !utils.isIPAddress(hostname) ? utils.GetIP(hostname) : hostname;
 				
 				if(IP != null) {
 					editor.putString("hostname", hostname);
 					editor.putString("IP", IP);
 					editor.putString("username", username);
 					editor.putString("password", password);
 					editor.putString("directory", directory);
 					editor.putInt("port", port);
 					editor.putInt("interval", timer);
 					editor.putBoolean("welcome", getWelcomeMsg);
 					editor.putString("fileList", fileList);
 					editor.putString("newFileList", newFileList);
 					editor.commit();
 				
 					startService(intent);
 				} else {
 					alert.createAlert(res.getString(R.string.error), 
 						res.getString(R.string.unableToConnectFirst) +
 						" " + hostname + "\n" +
 						res.getString(R.string.unableToConnectSecond), "OK").show();
 				}
 			}
 			return true;
 		}
 
 	};
 	
 	OnPreferenceClickListener fileListListener = new OnPreferenceClickListener() {
 
 		public boolean onPreferenceClick(Preference preference) {
 			String host, username, password, directory;
 			SharedPreferences prefs = getSharedPreferences(FTP_DATA, Context.MODE_PRIVATE);
 			int port;
 			
 			username = prefs.getString("username", "");
 			password = prefs.getString("password", "");
 			host = prefs.getString("hostname", "");
 			directory = prefs.getString("directory", "/");
 			port = prefs.getInt("port", 21);
 			
 			Ftp nFtp = new Ftp(username, password, host, port, directory);
 			if(!nFtp.connect(false)) {
 				alert.createAlert(res.getString(R.string.error), 
 						res.getString(R.string.unableToConnectFirst) +
 						" " + host + "\n" +
 						res.getString(R.string.unableToConnectSecond), "OK").show();
 			}
 			nFtp.getFileList(fileList);
 			nFtp.disconnect();
 			return true;
 		}
 		
 	};
 	
 	OnPreferenceClickListener manualCheckListener = new OnPreferenceClickListener() {
 		
 		public boolean onPreferenceClick(Preference preference) {
 			String host, username, password, directory;
 			SharedPreferences prefs = getSharedPreferences(FTP_DATA, Context.MODE_PRIVATE);
 			int port, value;
 			
 			username = prefs.getString("username", "");
 			password = prefs.getString("password", "");
 			host = prefs.getString("hostname", "");
 			directory = prefs.getString("directory", "/");
 			port = prefs.getInt("port", 21);
 			
 			Ftp nFtp = new Ftp(username, password, host, port, directory);
 			nFtp.connect(false);
 			
 			if((value = nFtp.checkUpdates(utils, newFileList, fileList)) != 0) {
 				nMsg.show(res.getString(R.string.updatedDetected), 
 						res.getString(R.string.filesNotification) + " " +
 						(value == 1 ? res.getString(R.string.filesRemovedNotification) : 
 						res.getString(R.string.filesAddedNotification)) + " " +
 						res.getString(R.string.orChangedFilesNotification), 
						host + "/" + directory, 1);
 			}
 			
 			(new File(newFileList)).delete();
 			nFtp.disconnect();
 			return true;
 		}
 		
 	};
 	
 	OnPreferenceClickListener logoutListener = new OnPreferenceClickListener() {
 		
 		public boolean onPreferenceClick(Preference preference) {
 		   /* TODO:
 			*  check if service is running
 			*/
 			stopService(intent);
 			return true;
 		} 
 		
 	};
 	
 }
