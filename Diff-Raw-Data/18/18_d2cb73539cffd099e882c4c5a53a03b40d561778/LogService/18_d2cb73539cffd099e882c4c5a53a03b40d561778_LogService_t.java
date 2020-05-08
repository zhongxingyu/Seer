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
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Date;
 
 import org.wahtod.wififixer.LegacySupport.VersionedLogFile;
 import org.wahtod.wififixer.LegacySupport.VersionedScreenState;
 import org.wahtod.wififixer.PrefConstants.Pref;
 
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.os.Environment;
 import android.util.Log;
 
 public class LogService extends IntentService {
 
     public LogService() {
 	super(LogService.class.getName());
     }
 
     public static final String APPNAME = "APPNAME";
     public static final String Message = "Message";
     private static final String BUILD = "Build:";
     private static final String SPACE = " ";
     private static final String COLON = ":";
     private static final String NEWLINE = "\n";
     public int VERSION = 0;
     private static String vstring = SPACE;
     private static String app_name = SPACE;
     private static String sMessage = SPACE;
     private static FileWriter fWriter;
     public static final String TIMESTAMP = "TIMESTAMP";
     public static final String DUMPBUILD = "DUMPBUILD";
     public static final String LOG = "LOG";
 
     // Log Timestamp
     private static final long TS_WAIT_SCREENON = 10000;
     private static final long TS_WAIT_SCREENOFF = 60000;
 
     private static VersionedScreenState vscreenstate;
     private static VersionedLogFile vlogfile;
 
     static String getBuildInfo() {
 
 	return Build.MODEL + NEWLINE + Build.VERSION.RELEASE + NEWLINE;
     }
 
     void getPackageInfo() {
 	PackageManager pm = getPackageManager();
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
 	    // ---display the versioncode--
 	    VERSION = pi.versionCode;
 	    vstring = pi.versionName;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * We will always find our own package name
 	     */
 	}
     }
    
    public static String getContextNameString(final Context context){
	return context.getClass().getSimpleName();
    }
 
     void handleStart(Intent intent) {
 
 	if (intent.hasExtra(APPNAME) && intent.hasExtra(Message)) {
 	    app_name = intent.getStringExtra(APPNAME);
 	    sMessage = intent.getStringExtra(Message);
 	    processLogIntent(this, app_name, sMessage);
 	}
     }
 
     public static void log(final Context context, final String APP_NAME,
 	    final String Message) {
 	Intent sendIntent = new Intent(context, LogService.class);
 	sendIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	sendIntent.putExtra(LogService.APPNAME, APP_NAME);
 	sendIntent.putExtra(LogService.Message, Message);
 	context.startService(sendIntent);
     }
 
     @Override
     public void onCreate() {
 	super.onCreate();
 	getPackageInfo();
 	vscreenstate = VersionedScreenState.newInstance(this);
 	vlogfile = VersionedLogFile.newInstance(this);
 
     }
 
     public boolean processCommands(final String command) {
 	/*
 	 * Incoming intents will have a command which we process here
 	 */
 	if (command.equals(TIMESTAMP)) {
 	    timeStamp();
 	    return true;
 	} else if (command.equals(DUMPBUILD)) {
 	    processLogIntent(this, WifiFixerService.APP_NAME, getBuildInfo());
 	    return true;
 	}
 
 	return false;
     }
 
     void timeStamp() {
 
 	Date time = new Date();
 	String message = BUILD + vstring + COLON + VERSION + SPACE + COLON
 		+ time.toString();
 	processLogIntent(this, WifiFixerService.APP_NAME, message);
 
 	/*
 	 * Schedule next timestamp or terminate
 	 */
 	if (PrefUtil.readBoolean(this, Pref.DISABLE_KEY))
 	    return;
 	else if (vscreenstate.getScreenState(this))
 	    ServiceAlarm.setLogTS(this, true, TS_WAIT_SCREENON);
 	else
 	    ServiceAlarm.setLogTS(this, true, TS_WAIT_SCREENOFF);
     }
 
     void processLogIntent(final Context context, final String APP_NAME,
 	    final String Message) {
 	if (processCommands(APP_NAME))
 	    return;
 	else {
 	    Log.i(APP_NAME, Message);
 	    writeToFileLog(context, Message);
 	}
     }
 
     static void writeToFileLog(final Context context, String message) {
 	if (Environment.getExternalStorageState() != null
 		&& !(Environment.getExternalStorageState()
 			.contains(Environment.MEDIA_MOUNTED))) {
 	    return;
 	}
 
 	File file = vlogfile.getLogFile(context);
 
 	try {
 	    if (!file.exists()) {
 		file.createNewFile();
 	    }
 
 	    fWriter = new FileWriter(file.getAbsolutePath(), true);
 	    fWriter.write(message + NEWLINE);
 	    fWriter.flush();
 	    fWriter.close();
 	} catch (Exception e) {
 
 	    e.printStackTrace();
 	}
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 	try {
 	    handleStart(intent);
 	} catch (NullPointerException e) {
 	    /*
 	     * Ignore null intents: system uses them to stop after processing
 	     */
 	}
     }
 }
