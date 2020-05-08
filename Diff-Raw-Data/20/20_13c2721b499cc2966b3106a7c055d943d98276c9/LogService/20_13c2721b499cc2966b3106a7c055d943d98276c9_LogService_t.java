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
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 public class LogService extends Service {
 
 	public static final String APPNAME = "APPNAME";
 	public static final String Message = "Message";
 	private static final long ALARMREPEAT = 10000;
 	public int VERSION = 0;
 	private static String VSTRING= " ";
 	private static String APP_NAME = " ";
 	private static String sMessage = " ";
 	public FileWriter fWriter;
 	private static boolean SCREENISOFF=false;
	private static boolean LOGGING=true;
 	//constants
 	static final String DIE="DIE";
 	static final String SCREEN_ON="SCREEN_ON";
 	static final String SCREEN_OFF="SCREEN_OFF";
	static final String LOG="LOG";
     
 	private Handler tsHandler = new Handler(){
 		@Override
         public void handleMessage(Message message) {
             timeStamp();
         }
 
 	  };
 
     static String getBuildInfo() {
     	
     	return Build.MODEL+"\n"+Build.VERSION.RELEASE+"\n";
     }
 	  
 	  
 	void getPackageInfo() {
 		PackageManager pm = getPackageManager();
         try {
             //---get the package info---
             PackageInfo pi =  
                 pm.getPackageInfo("org.wahtod.wififixer", 0);
             //---display the versioncode--           
            VERSION = pi.versionCode;
            VSTRING = pi.versionName;
         } catch (NameNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 	}
 	
 	void handleStart(Intent intent) {
 			
			if(!intent.getAction().contains(LOG))
 				return;
 			try {
 				 sMessage=intent.getStringExtra(Message);
 				 APP_NAME=intent.getStringExtra(APPNAME);
 				 wfLog(APP_NAME,sMessage);
 				}
 			 catch (NullPointerException e) {
 				e.printStackTrace();
 				
 			}
 		}
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		
 		getPackageInfo();
 		wfLog(WifiFixerService.APP_NAME,getBuildInfo());
 		timeStamp();
 		
 	}
 	
 
 	public void onStart(Intent intent, int startId) {
 		
 		try {
 			handleStart(intent);
 		} catch (NullPointerException e) {
 		
 		}
 	}
 	
 	
 public boolean processCommands(String Command) {
 	
 	if (Command.contains(DIE)){
 		LOGGING=false;
 		return true;
 	}
 	else
 	if (Command.contains(SCREEN_ON)){
 		SCREENISOFF=false;
 		return true;
 	}
 	else
 	if (Command.contains(SCREEN_OFF)){
 		SCREENISOFF=true;
 		return true;
 	}
 	
 	return false;
 }
 	
 void timeStamp() {
 	if (SCREENISOFF && LOGGING){
 			tsHandler.sendEmptyMessageDelayed(1, ALARMREPEAT);
 		return;
 	}
 		
 	Date time = new Date();
 	String message="Build:"+VSTRING+":"+VERSION+" " + ":" + time.toString();
 	wfLog("WifiFixerService", message);
 	if(LOGGING)
 		tsHandler.sendEmptyMessageDelayed(1, ALARMREPEAT);
 	else
 		stopSelf();
 }		
 
 
 void wfLog(String APP_NAME, String Message) {
 	if (processCommands(APP_NAME))
 		return;
 	Log.i(APP_NAME,Message);
 	writeToFileLog(Message);
 }
 
 	void writeToFileLog(String message) {
 		if(Environment.getExternalStorageState() != null && !(Environment.getExternalStorageState().contains("mounted"))){
 			return;
 		}
 		
 		message = message+"\n";
 		File dir = new File(Environment.getExternalStorageDirectory()+"/data/org.wahtod.wififixer");
 		if(!dir.exists())
 		{
 			dir.mkdirs();
 		}
 		File fiyul = new File(dir.getAbsolutePath()+"/wififixer_log.txt");  
 		//Remove if over 100k
 		
 		
 		try {
 			if(fiyul.length()>102400)
 				fiyul.delete();
 			if(!fiyul.exists())
 			{
 				fiyul.createNewFile();
 			}
 			
 			fWriter = new FileWriter(fiyul.getAbsolutePath(), true);
 			fWriter.write(message);
 			fWriter.flush();
 			fWriter.close();
 		} catch (Exception e) {
 
 			e.printStackTrace();
 		}
 	}
 }
