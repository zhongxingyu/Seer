 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ImageButton;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WifiFixerActivity extends Activity {
     // Is this the paid version?
     public boolean ISFREE = true;
     public boolean ISAUTHED = false;
     public boolean ABOUT = false;
     public boolean LOGGING_MENU = false;
     public boolean LOGGING = false;
 
     // constants
     private static final int MENU_LOGGING = 1;
     private static final int MENU_SEND = 2;
     private static final int MENU_PREFS = 3;
     private static final int MENU_HELP = 4;
     private static final int MENU_ABOUT = 5;
     private static final int LOGGING_GROUP = 42;
     SharedPreferences settings;
     // New key for About nag
     // Set this when you change the About xml
     static final String sABOUT = "ABOUT2";
 
 
     String getPrefs() {
 	settings = PreferenceManager.getDefaultSharedPreferences(this);
 	String prefs = "Settings:\n";
 
 	if (settings.getBoolean("Disable", false))
 	    prefs = prefs + "DISABLE:True\n";
 	else
 	    prefs = prefs + "DISABLE:False\n";
 
 	if (settings.getBoolean("WiFiLock", false))
 	    prefs = prefs + "LOCKPREF:True\n";
 	else
 	    prefs = prefs + "LOCKPREF:False\n";
 
 	if (settings.getBoolean("Notifications", false))
 	    prefs = prefs + "NOTIFPREF:True\n";
 	else
 	    prefs = prefs + "NOTIFPREF:False\n";
 
 	if (settings.getBoolean("SCREEN", false))
 	    prefs = prefs + "SCREENPREF:True\n";
 	else
 	    prefs = prefs + "SCREENPREF:False\n";
 
 	if (settings.getBoolean("SUPFIX", false))
 	    prefs = prefs + "SUPPREF:True\n";
 	else
 	    prefs = prefs + "SUPPREF:False\n";
 
 	return prefs;
 
     }
 
     boolean getLogging() {
 	settings = PreferenceManager.getDefaultSharedPreferences(this);
 	return settings.getBoolean("SLOG", false);
     }
 
     void launchHelp() {
 	Intent myIntent = new Intent(this, HelpActivity.class);
 	startActivity(myIntent);
     }
 
     void launchPrefs() {
 	Intent myIntent = new Intent(this, WifiFixerPreferences.class);
 	startActivity(myIntent);
     }
 
     void sendLog() {
 
 	if (Environment.getExternalStorageState() != null
 		&& !(Environment.getExternalStorageState().contains("mounted"))) {
 	    Toast.makeText(WifiFixerActivity.this, "SD Card Unavailable",
 		    Toast.LENGTH_LONG).show();
 
 	    return;
 	}
 
 	Intent sendIntent = new Intent(Intent.ACTION_SEND);
 	sendIntent.setType("text/plain");
 	sendIntent.putExtra(Intent.EXTRA_EMAIL,
 		new String[] { "zanshin.g1@gmail.com" });
 	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "WifiFixer Log");
 	sendIntent
 		.putExtra(
 			Intent.EXTRA_STREAM,
 			Uri
 				.parse("file:///sdcard/data/org.wahtod.wififixer/wififixer_log.txt"));
 	sendIntent.putExtra(Intent.EXTRA_TEXT,
 		"Please include time at which issue occurred and description of the issue:\n\n"
 			+ LogService.getBuildInfo() + getPrefs());
 
 	startActivity(Intent.createChooser(sendIntent, "Email:"));
     }
 
     void setIcon() {
 	SharedPreferences settings = PreferenceManager
 		.getDefaultSharedPreferences(this);
 	ImageButton serviceButton = (ImageButton) findViewById(R.id.ImageButton01);
 	if (settings.getBoolean("Disable", false)) {
 	    serviceButton.setImageResource(R.drawable.inactive);
 	} else {
 	    serviceButton.setImageResource(R.drawable.active);
 	}
     }
 
     void setLogging(boolean state) {
 	LOGGING = state;
 	SharedPreferences settings = PreferenceManager
 		.getDefaultSharedPreferences(this);
 	SharedPreferences.Editor edit = settings.edit();
 	edit.putBoolean("SLOG", state);
 	edit.commit();
 	Intent sendIntent = new Intent(WifiFixerService.class.getName());
 	startService(sendIntent);
     }
 
     void setText() {
 	PackageManager pm = getPackageManager();
 	String vers = "";
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo("org.wahtod.wififixer", 0);
 	    // ---display the versioncode--
 	    vers = pi.versionName;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * shouldn't ever be not found
 	     */
 	    e.printStackTrace();
 	}
 	TextView vButton = (TextView) findViewById(R.id.version);
 	vButton.setText(vers.toCharArray(), 0, vers.length());
     }
 
     void setToggleIcon(Menu menu) {
 	MenuItem logging = menu.getItem(MENU_LOGGING - 1);
 	if (LOGGING)
 	    logging.setIcon(R.drawable.logging_enabled);
 	else
 	    logging.setIcon(R.drawable.logging_disabled);
 
     }
 
     void showNotification() {
 
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	// The details of our message
 	CharSequence from = "WiFi Fixer";
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(this, About.class), 0);
 	// construct the Notification object.
 	Notification notif = new Notification(R.drawable.icon, "Please Read",
 		System.currentTimeMillis());
 
 	// Set the info for the views that show in the notification panel.
 	notif.setLatestEventInfo(this, from, "Tap Here to Read About Page",
 		contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	nm.notify(4145, notif);
 
     }
 
     void startwfService() {
 	Intent sendIntent = new Intent(WifiFixerService.class.getName());
 	startService(sendIntent);
     }
 
     void nagNotification() {
 
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(Intent.ACTION_VIEW, Uri
 			.parse("market://details?id=com.wahtod.wififixer")), 0);
 	Notification notif = new Notification(R.drawable.icon, "Thank You",
 		System.currentTimeMillis());
 
 	RemoteViews contentView = new RemoteViews(getPackageName(),
 		R.layout.nag_layout);
 	contentView.setImageViewResource(R.id.image, R.drawable.icon);
 	contentView
 		.setTextViewText(
 			R.id.text,
 			"Thank you for using Wifi Fixer. If you find this software useful, please tap here to purchase the donate version");
 	notif.contentView = contentView;
 	notif.contentIntent = contentIntent;
 
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 
 	// hax
 	nm.notify(31337, notif);
 
     }
 
     private void removeNag() {
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 	nm.cancel(31337);
     }
 
     void toggleLog() {
 
 	LOGGING = getLogging();
 	if (LOGGING) {
 	    Toast.makeText(WifiFixerActivity.this, "Disabling Logging",
 		    Toast.LENGTH_SHORT).show();
 	    setLogging(false);
 	} else {
 	    if (Environment.getExternalStorageState() != null
 		    && !(Environment.getExternalStorageState()
 			    .contains("mounted"))) {
 		Toast.makeText(WifiFixerActivity.this, "SD Card Unavailable",
 			Toast.LENGTH_SHORT).show();
 		return;
 	    }
 
 	    Toast.makeText(WifiFixerActivity.this, "Enabling Logging",
 		    Toast.LENGTH_SHORT).show();
 	    setLogging(true);
 	}
     }
 
     // On Create
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setTitle("Wifi Fixer");
 	setContentView(R.layout.main);
 	settings = PreferenceManager.getDefaultSharedPreferences(this);
 	// Set layout version code
 	setText();
 	// handle input
 
 	oncreate_setup();
 
     };
 
     private void oncreate_setup() {
 	ISAUTHED = (settings.getBoolean("ISAUTHED", false));
 	ABOUT = (settings.getBoolean(sABOUT, false));
 	LOGGING_MENU = (settings.getBoolean("Logging", false));
 	LOGGING = getLogging();
 	// Fire new About nag
 	if (!ABOUT) {
 	    showNotification();
 
 	}
 
 	// Here's where we fire the nag
 	if (!ISAUTHED) {
 	    // Handle Donate Auth
 	    Intent sendIntent = new Intent(
 		    "com.wahtod.wififixer.WFDonateService");
 	    startService(sendIntent);
 	    nagNotification();
 	}
 
 	startwfService();
 
     }
 
     @Override
     public void onStart() {
 	super.onStart();
 	setIcon();
 	LOGGING_MENU = (settings.getBoolean("Logging", false));
 	LOGGING = getLogging();
     }
 
     // Create menus
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	super.onCreateOptionsMenu(menu);
 	menu.add(LOGGING_GROUP, MENU_LOGGING, 0, "Toggle Logging").setIcon(
 		R.drawable.logging_enabled);
 
 	menu.add(LOGGING_GROUP, MENU_SEND, 1, "Send Log").setIcon(
 		R.drawable.ic_menu_send);
 
 	menu.add(Menu.NONE, MENU_PREFS, 2, "Preferences").setIcon(
 		R.drawable.ic_prefs);
 
	menu.add(Menu.NONE, MENU_HELP, 3, "Documentation").setIcon(
 		R.drawable.ic_menu_help);
 
 	menu.add(Menu.NONE, MENU_ABOUT, 4, "About").setIcon(
 		R.drawable.ic_menu_info);
 
 	return true;
     }
 
     /* Handles item selections */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	super.onOptionsItemSelected(item);
 
 	switch (item.getItemId()) {
 	
 	case MENU_LOGGING:
 	    toggleLog();
 	    return true;
 	    
 	case MENU_SEND:
 	    sendLog();
 	    return true;
 
 	case MENU_PREFS:
 	    launchPrefs();
 	    return true;
 
 	case MENU_HELP:
 	    launchHelp();
 	    return true;
 	case MENU_ABOUT:
 	    Intent myIntent = new Intent(this, About.class);
 	    startActivity(myIntent);
 	    return true;
 
 	}
 	return false;
     }
 
     @Override
     public void onPause() {
 	super.onPause();
 	removeNag();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 	super.onPrepareOptionsMenu(menu);
 	// Menu drawing stuffs
 
 	if (LOGGING_MENU) {
 	    menu.setGroupVisible(LOGGING_GROUP, true);
 	    setToggleIcon(menu);
 
 	} else {
 	    menu.setGroupVisible(LOGGING_GROUP, false);
 	}
 
 	return true;
     }
 }
