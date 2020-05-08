 /*
  * Copyright (C) 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.seattleonandroid;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.util.Log;
 
 import com.googlecode.android_scripting.BaseApplication;
 import com.googlecode.android_scripting.Constants;
 import com.googlecode.android_scripting.FeaturedInterpreters;
 import com.googlecode.android_scripting.FileUtils;
 import com.googlecode.android_scripting.interpreter.Interpreter;
 import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
 
 /**
  * 
  * Loosely based on the ScriptActivity found in the ScriptForAndroidTemplate package in SL4A
  * 
  * This class represents the main activity performed by the SeattleOnAndroid app 
  * 
  */
 public class ScriptActivity extends Activity {
 
 	// Use int values instead of enums for easier message handling 
 	public final static int SEATTLE_INSTALLED = 14;
 	public final static int INSTALL_FAILED = 15;
 	
 	// Names of the keys to use
 	public final static String AUTOSTART_ON_BOOT = "autostart_on_boot";
 	public final static String AUTOSTART_DELAY = "autostart_delay";
 	public final static String SEATTLE_PREFERENCES = "seattlepreferences";
 	public final static String RESOURCES_TO_DONATE = "resources_to_donate";
 	public final static String PERMITTED_INTERFACES = "permitted_interfaces";
 	public final static String OPTIONAL_ARGUMENTS = "optional_arguments";
 	public final static String UPDATE_MESSAGE_ID = "UPD";
 	// Constants used in calculating the percentage to donate  
 	public final static int MINIMUM_DONATE = 1;
 	public final static int MAXIMUM_DONATE = 100;
 	public final static int DEFAULT_DONATE = 20;
 	public final static int MAXIMUM_SEEKBAR= MAXIMUM_DONATE-MINIMUM_DONATE+1;
 	// Constants used in calculating the autostart delay
 	public final static int MINIMUM_AUTOSTART = 0;
 	public final static int MAXIMUM_AUTOSTART = 60;
 	public final static int STEP_AUTOSTART= 30;
 	public final static int UNIT_AUTOSTART= 1000; // Seconds
 	public final static int DEFAULT_DELAY = 300*UNIT_AUTOSTART; // 5 mins
 	// Some private variables
 	private int donate = -1;
 	private int currentContentView;
 	private File currentLogFile;
 	private ArrayList<File> files;
 
 	// Workaround -- status toggle-button could be set incorrectly right after installation
 	private static boolean autostartedAfterInstallation = false;
 
 	// Message handler used for notifying the activity
 	public static MyMessageHandler handler;
 
     private SharedPreferences settings;
 
     // Returns the root directory of seattle
 	// Not to be confused with seattle_repy directory, which is a subdirectory of seattle-root
 	public static String getSeattlePath(){
 		return Environment.getExternalStorageDirectory().getAbsolutePath()+"/sl4a/seattle/";
 	}
 
 	// Check if seattle is installed by checking if nmmain.py exists
 	private boolean isSeattleInstalled() {
 		return (new File(getSeattlePath()+"seattle_repy/nmmain.py")).exists();
 	}
 
 	// Message handler class
 	public class MyMessageHandler extends Handler {
 		private ScriptActivity a;
 
 		public MyMessageHandler(ScriptActivity a){
 			super();
 			this.a = a;
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			try{
 				// Installation finished -> refresh screen
 				if (msg.what == SEATTLE_INSTALLED || msg.what == INSTALL_FAILED){
 					// Start seattle automatically after installation
 					if(msg.what == SEATTLE_INSTALLED) {
 						ScriptActivity.autostartedAfterInstallation = true;
 						ScriptService.serviceInitiatedByUser = true;
 						startService(new Intent(getBaseContext(), ScriptService.class));
 					}
 					// If AUTOSTART_ON_BOOT key does not exist, create it
 					// Default value: true
 					if (!settings.contains(AUTOSTART_ON_BOOT)){
 						SharedPreferences.Editor editor = settings.edit();
 						editor.putBoolean(AUTOSTART_ON_BOOT, true);
 						editor.commit();
 					}
 					
 					// Reload layout
 					showFrontendLayout();
 
 					// Show dialog with information about the installation outcome					
 					String text;
 					if(msg.what == SEATTLE_INSTALLED) {
 						text = "Seattle installed successfully!";
 						Log.i(Common.LOG_TAG, Common.LOG_INFO_INSTALL_SUCCESS);
 					} else {
 						text = "Installation failed! Please check logs for more information.";
 						Log.i(Common.LOG_TAG, Common.LOG_INFO_INSTALL_FAILURE);
 					}
 					new AlertDialog.Builder(a)
 						.setTitle("SeattleOnAndroid")
 						.setMessage(text)
 						.setNeutralButton("Ok",new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) { }
 						})
 						.show();
 				}
 			} catch(Exception e) {
 				// Log exceptions
 				Log.e(Common.LOG_TAG, Common.LOG_EXCEPTION_MESSAGE_HANDLING, e);
 			}
 		}
 	}
 
 	// Prepares options menu
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 		MenuInflater inflater = getMenuInflater();
 		switch(currentContentView){
 			case R.layout.main:
 				inflater.inflate(R.menu.main_menu, menu);
 				return true;
 			case R.layout.install:
 			case R.layout.basic_install:
 				inflater.inflate(R.menu.install_menu, menu);
 				return true;
 			case R.layout.logfileview:
 			case R.layout.logmenuview:
 				inflater.inflate(R.menu.log_menu, menu);
 				return true;
 			case R.layout.about:
 				inflater.inflate(R.menu.about_menu, menu);
 				return true;
 		}
 		return false;
 	}
 
 	// Displays the contents of the file line by line
 	private void showLogFile(File file){
 		// Store reference to file, so that it can be found on refresh
 		currentLogFile = file;
 		this.setContentView(R.layout.logfileview);
 		currentContentView = R.layout.logfileview;
 		// Set up TextViews
 		TextView twDesc = (TextView) this.findViewById(R.id.textViewLogDescription);
 		twDesc.setText(file.getName()+":");
 		TextView twCont = (TextView) this.findViewById(R.id.textViewLogContents);
 		// File does not exist
 		if(!file.exists())
 			twCont.setText("File does not exist!");
 		else {
 		// File exists, iterate through it
 			try {
 				BufferedReader r = new BufferedReader(new FileReader(file));
 				String line;
 				twCont.setText("");
 				while((line=r.readLine())!=null){
 					twCont.append(line+"\n");
 				}
 			} catch (Exception e) {
 				// Log exception
 				Log.e(Common.LOG_TAG, Common.LOG_EXCEPTION_READING_LOG_FILE, e);
 			}	
 		}
 		// Post event to scroll down to the bottom of the page
 		final ScrollView sv = (ScrollView) this.findViewById(R.id.logFileScrollView);
 		sv.post(new Runnable(){
 			@Override
 			public void run() {
 				sv.fullScroll(ScrollView.FOCUS_DOWN);
 			}});
 		sv.scrollTo(0, sv.getBottom());
 	}
 
 	// Back button was pressed by the user
 	public void onBackPressed() {
		goBack();
 	}
 
 	// Get a listing of the (hopefully) seattle specific log files in the directory 
 	public static ArrayList<File> getLogFilesInDirectory(File directory){
 		// Accepts only directories
 		FileFilter directoryFilter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.isDirectory();
 			}
 		};
 		// Accepts files with .log extension or (nodemanager|softwareupdater|installInfo).(old|new)
 		FileFilter logFilter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				// No directories accepted by this filter
 				if(pathname.isDirectory())
 					return false;
 				String filename = pathname.getName();
 				return filename.endsWith(".log") ||
 						((filename.startsWith("nodemanager") || filename.startsWith("softwareupdater") || filename.startsWith("installInfo"))
 						&& (filename.endsWith(".old") || filename.endsWith(".new")));
 			}
 		};
 
 		// Iterate through subdirectories
 		return getLogFilesInDirectoryPrivate(directory, directoryFilter, logFilter);
 	}
 	
 	// Get listing of files in the directory tree conforming to some filters 
 	private static ArrayList<File> getLogFilesInDirectoryPrivate(File dir, FileFilter dirFilter, FileFilter logFilter){
 		ArrayList<File> result = new ArrayList<File>();
 		if (!dir.exists())
 			return result;
 		// Get files in this directory
 		File[] files = dir.listFiles(logFilter);
 		if(files != null)
 			result.addAll(Arrays.asList(files));
 		// Get subdirectories
 		File[] subdirs = dir.listFiles(dirFilter);
 		// Iterate through subdirectories
 		if(subdirs != null)
 			for(int i=0; i<subdirs.length; i++)
 			{
 				result.addAll(getLogFilesInDirectoryPrivate(subdirs[i], dirFilter, logFilter));
 			}
 		return result;
 	}
 
 	// Show a listing of the available log files
 	private void showAvailableLogListing(){
 		setContentView(R.layout.logmenuview);
 		currentContentView = R.layout.logmenuview;
 		ListView lv = (ListView) findViewById(R.id.listView1);
 		// Get log files
 		files = getLogFilesInDirectory(new File(getSeattlePath()));
 		ArrayList<String> strings = new ArrayList<String>();
 		final ScriptActivity instance = this;
 		for(int i=0; i<files.size(); i++){
 			strings.add(files.get(i).getName());
 		}
 		// Set up ListView
 		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, strings.toArray(new String[strings.size()])));
 		// Set up onClickListener
 		lv.setOnItemClickListener(new OnItemClickListener(){
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				instance.showLogFile(files.get(arg2));
 			}
 		});
 	}
 
 	// Handle click events from options menu
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.view_log_button:
 				// Show available log files
 				showAvailableLogListing();
 				return true;
 			case R.id.uninstall_seattle_button:
 				// Uninstall seattle
 				final ScriptActivity sa = this;
 				new AlertDialog.Builder(this)
 					.setMessage("Would you really like to uninstall Seattle?")
 					.setCancelable(false)
 					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							Log.i(Common.LOG_TAG, Common.LOG_INFO_UNINSTALL_INITIATED);
 							// Kill service, in case it was running
 							if(ScriptService.isServiceRunning())
 								killService();
 							// Remove Seattle folder
 							FileUtils.delete(new File(getSeattlePath()));
 							if(settings.contains(AUTOSTART_ON_BOOT)){
 								SharedPreferences.Editor editor = settings.edit();
 								editor.remove(AUTOSTART_ON_BOOT);
 								editor.commit();
 							}
 							new AlertDialog.Builder(sa)
 								.setMessage("Seattle uninstalled successfully!")
 								.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog, int which) {}
 								})
 							.create().show();
 							Log.i(Common.LOG_TAG, Common.LOG_INFO_UNINSTALL_SUCCESS);
 							showBasicInstallLayout();
 						}
 					})
 					.setNegativeButton("No", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					}).create().show();
 				return true;
 			case R.id.back:
 				// Back button
				goBack();
 				return true;
 			case R.id.refresh:
 				// Refresh
 				doRefresh();
 				return true;
 			case R.id.view_about_button:
 				showAboutLayout();
 				return true;
 			default:
 				return false;
 	    }
 	}
 
 	// Kills the ScriptService using startService, by sending a flagged intent 
 	private void killService(){
 		Intent intent = new Intent(getBaseContext(), ScriptService.class);
 		intent.putExtra("KILL_SERVICE", true);
 		startService(intent);
 	}
 
 	// Refresh view
 	private void doRefresh(){
 		if(currentContentView == R.layout.logmenuview){
 			showAvailableLogListing();
 		} else if(currentContentView == R.layout.logfileview){
 			showLogFile(currentLogFile);
 		}
 	}
 
	// Back button pressed
	private void goBack(){
		if(currentContentView == R.layout.logmenuview){
			showFrontendLayout();
		} else if(currentContentView == R.layout.logfileview){
			showAvailableLogListing();
		} else if(currentContentView == R.layout.about){
			showFrontendLayout();
		}
	}

 	// Show the main (seattle already installed) layout
 	private void showMainLayout() {
 		setContentView(R.layout.main);
 		currentContentView = R.layout.main;
 		TextView sensor = (TextView)findViewById(R.id.sensorlink);
 		sensor.setMovementMethod(LinkMovementMethod.getInstance());
 		//sensor.setText("Note: Installing a "+Html.fromHtml("<a href=\"https://seattle.cs.washington.edu/wiki/AvailableAndroidSensors\">sensor</a>")+" can provide a richer Seattle experience.");
 		sensor.setText(Html.fromHtml("Note: Installing a <a href=\"https://seattle.cs.washington.edu/wiki/AvailableAndroidSensors\">sensor</a> can provide a richer Seattle experience."));
 		// Set up status toggle button
 		final ToggleButton toggleStatus = (ToggleButton) findViewById(R.id.toggleStatus);
 		toggleStatus.setChecked(ScriptService.isServiceRunning());
 
 		if (ScriptActivity.autostartedAfterInstallation) {
 			ScriptActivity.autostartedAfterInstallation = false;
 			toggleStatus.setChecked(true);
 		}
 
 		toggleStatus.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (isChecked) {
 					// Start service
 					ScriptService.serviceInitiatedByUser = true;
 					startService(new Intent(getBaseContext(), ScriptService.class));
 				} else {
 					// Kill service
 					killService();
 				}
 			}
 		});
 		// Set up autostart textview and slider
 		final TextView twAutostart = (TextView) findViewById(R.id.textViewAutostart);
 		final SeekBar sbAutostart = (SeekBar) findViewById(R.id.seekBarAutostart);
 		sbAutostart.setMax(MAXIMUM_AUTOSTART-MINIMUM_AUTOSTART+1);
 		final ScriptActivity scrAct = this;
 		sbAutostart.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser) {
 					twAutostart.setText(scrAct.getString(R.string.startup_delay) + " " + (progress+MINIMUM_AUTOSTART)*STEP_AUTOSTART);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putInt(AUTOSTART_DELAY, (progress+MINIMUM_AUTOSTART)*STEP_AUTOSTART*UNIT_AUTOSTART);
 					editor.commit();
 				}
 			}
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			
 		});
 		int delay = settings.getInt(AUTOSTART_DELAY, -1);
 		if(delay == -1)
 			delay = DEFAULT_DELAY;
 		twAutostart.setText(getString(R.string.startup_delay) + " " + (delay)/UNIT_AUTOSTART);
 		sbAutostart.setProgress(delay/UNIT_AUTOSTART/STEP_AUTOSTART);
 		twAutostart.setVisibility(View.VISIBLE);
 		sbAutostart.setVisibility(View.VISIBLE);
 		// Set up autostart checkbox
 		final CheckBox checkBoxAutostart = (CheckBox) findViewById(R.id.checkBoxAutostart);
 		checkBoxAutostart.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
 				// Store changes
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putBoolean(AUTOSTART_ON_BOOT, isChecked);
 				editor.commit();
 				if(isChecked){
 					twAutostart.setVisibility(View.VISIBLE);
 					sbAutostart.setVisibility(View.VISIBLE);
 				} else {
 					twAutostart.setVisibility(View.INVISIBLE);
 					sbAutostart.setVisibility(View.INVISIBLE);
 				}
 			}
 		});
 		checkBoxAutostart.setChecked(settings.getBoolean(AUTOSTART_ON_BOOT, true));
 	}
 
 	// Show install layout
 	private void showBasicInstallLayout() {
 		setContentView(R.layout.basic_install);
 		currentContentView = R.layout.basic_install;
 		final Button buttonInstall = (Button) findViewById(R.id.basicinstallbutton);
 		final Button buttonAdvanced = (Button) findViewById(R.id.showadvancedoptionsbutton);
 		handler = new MyMessageHandler(this);
 		
 		String referrer = ReferralReceiver.retrieveReferralParams(getApplicationContext()).get("utm_content");
 		if (referrer == null)
 			referrer = "Altruistic donation";
 		else
 			referrer = "Donate to: " + referrer;
 		final TextView referrerView = (TextView) findViewById(R.id.referrerview);
 		referrerView.setText(referrer);
 		
 		donate = DEFAULT_DONATE;
 		// Set up install button listener
 		buttonInstall.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(getBaseContext(), InstallerService.class);
 				i.putExtra(RESOURCES_TO_DONATE, donate);
 				startService(i);
 				showInstallingLayout();
 			}
 		});
 		buttonAdvanced.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showAdvancedInstallLayout();
 			}
 		});
 	}
 
 	
 	// Show install layout
 	private void showAdvancedInstallLayout() {
 		setContentView(R.layout.install);
 		currentContentView = R.layout.install;
 		
 		final Button buttonBasic = (Button) findViewById(R.id.showbasicoptionsbutton);
 		buttonBasic.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showBasicInstallLayout();
 			}
 		});
 		
 		final Button button = (Button) findViewById(R.id.buttonInstall);
 		handler = new MyMessageHandler(this);
 		final TextView tw = (TextView) findViewById(R.id.textView6);
 		final String donateString = this.getString(R.string.resource_donate);
 		// Set up donation-percentage seekbar
 		final SeekBar sb = (SeekBar) findViewById(R.id.seekBar1);
 		sb.setMax(MAXIMUM_SEEKBAR);
 		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				// Update on changes to the seekbar
 				donate = progress+MINIMUM_DONATE;
 				if(donate>MAXIMUM_DONATE)
 					donate = MAXIMUM_DONATE;
 				tw.setText(donateString + ": " + Integer.toString(donate));
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 		});
 		
 		String referrer = ReferralReceiver.retrieveReferralParams(getApplicationContext()).get("utm_content");
 		if (referrer == null)
 			referrer = "Altruistic donation";
 		else
 			referrer = "Donate to: " + referrer;
 		
 		final TextView referrerView = (TextView) findViewById(R.id.referview);
 		referrerView.setText(referrer);
 		
 		String DownloadURLString = ReferralReceiver.retrieveReferralParams(getApplicationContext()).get("utm_source");
 		if (DownloadURLString == null)
 			DownloadURLString = Common.DEFAULT_DOWNLOAD_URL;
 		final TextView downloadView = (TextView) findViewById(R.id.downloadview);
 		downloadView.setText("URL: "+DownloadURLString);
 
 		// If donation was not yet set, default to DEFAULT_DONATE
 		if (donate == -1)
 			donate = DEFAULT_DONATE;
 		tw.setText(donateString + ": " + Integer.toString(donate));
 		sb.setProgress(donate-MINIMUM_DONATE);
 		
 		final EditText permittedInterfaces = (EditText) findViewById(R.id.permittedInterfaces);
 		final TextView permView = (TextView) findViewById(R.id.permifView);
 		final CheckBox allBox = (CheckBox) findViewById(R.id.checkBox1);
 		allBox.setChecked(true);
 		permittedInterfaces.setVisibility(View.INVISIBLE);
 		permView.setVisibility(View.INVISIBLE);
 		
 		final EditText optionalArgs = (EditText) findViewById(R.id.optionalEdit);
 		
 		allBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				if (isChecked)
 				{
 					permittedInterfaces.setVisibility(View.INVISIBLE);
 					permView.setVisibility(View.INVISIBLE);					
 				}
 				else
 				{
 					permittedInterfaces.setVisibility(View.VISIBLE);
 					permView.setVisibility(View.VISIBLE);
 				}
 			}
 			
 		});
 		
 		String iflist = null;
 		
 		Enumeration<NetworkInterface> networkInterfaces;
 		try {
 			networkInterfaces = NetworkInterface.getNetworkInterfaces();
 			while (networkInterfaces.hasMoreElements())
 			{
 			    NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
 			    if (iflist == null)
 			    	iflist = networkInterface.getName();
 			    else
 			    	iflist += "," + networkInterface.getName();
 			}
 		} catch (SocketException e) {
 			iflist = null;
 			Log.e(Common.LOG_TAG, Common.LOG_EXCEPTION_GETTING_IFS, e);
 		}
 		
 		permittedInterfaces.setText(iflist);
 		
 		// Set up install button listener
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				final Intent i = new Intent(getBaseContext(), InstallerService.class);
 				i.putExtra(RESOURCES_TO_DONATE, donate);
 				String permlist[] = null;
 				if (!allBox.isChecked())
 				{
 					String intlist = permittedInterfaces.getText().toString();
 					if (intlist.trim().compareTo("") != 0)
 						permlist = intlist.split(",");
 					i.putExtra(PERMITTED_INTERFACES, permlist);
 				}
 				i.putExtra(OPTIONAL_ARGUMENTS, optionalArgs.getText().toString());
 				
 				startService(i);
 				
 				showInstallingLayout();
 			}
 		});
 	}
 
 	// Show the most appropriate layout 
 	private void showFrontendLayout() {
 		if(InstallerService.isInstalling()) {
 			// Installer still running
 			showInstallingLayout();
 		} else if(isSeattleInstalled()) {
 			// Already installed
 			showMainLayout();
 		} else {
 			// Not yet installed
 			showBasicInstallLayout();
 		}
 	}
 
 	// Show installation in progress layout
 	private void showInstallingLayout() {
 		setContentView(R.layout.installing);
 		currentContentView = R.layout.installing;
 	}
 
 	// Show external storage device not mounted layout
 	private void showNotMountedLayout() {
 		setContentView(R.layout.notmounted);
 		currentContentView = R.layout.notmounted;
 	}
 
 	// Show about box layout
 	private void showAboutLayout() {
 		setContentView(R.layout.about);
 		
 		TextView tw = (TextView) findViewById(R.id.aboutVersionView);
 		try {
 			// Get version name
 			tw.setText("v"+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
 		} catch (NameNotFoundException e) {
 			// This exception should not occur
 			Log.e(Common.LOG_TAG, Common.LOG_EXCEPTION_ABOUT_NNF, e);
 		}
 		currentContentView = R.layout.about;
 	}
 	// Executed after the activity is started / resumed
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		ScriptApplication application = (ScriptApplication) getApplication();
 
 		// Load settings
 		settings = getSharedPreferences(SEATTLE_PREFERENCES, MODE_WORLD_WRITEABLE);
 		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
 			// External storage device not mounted
 			showNotMountedLayout();
 		} else {
 			if(application.readyToStart()){
 				// Check if a python interpreter is available
 				InterpreterConfiguration mInterpreterConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
 				Interpreter interpreter = mInterpreterConfiguration.getInterpreterForScript("foo.py");
 				// Interpreter was not found -> present user with the option to install it  
 				if (interpreter == null || !interpreter.isInstalled()) {
 					if (FeaturedInterpreters.isSupported("foo.py")) {
 						Intent i = new Intent(this, DialogActivity.class);
 						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 						i.putExtra(Constants.EXTRA_SCRIPT_PATH, "foo.py");
 						startActivity(i);
 					} else {
 						// No available python installers; should not happen
 						Log.e(Common.LOG_TAG, Common.LOG_EXCEPTION_NO_PYTHON_INTERPRETER);
 					}
 					// Finish activity
 					finish();
 				}
 
 				// Show layout
 				showFrontendLayout();
 			}
 		}
 	}
 
 	// Executed after the activity is created, calls onStart()
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.onStart();
 	}
 }
