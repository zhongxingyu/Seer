 package edu.usf.eng.pie.avatars4change.dataInterface;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import edu.usf.eng.pie.avatars4change.R;
 import edu.usf.eng.pie.avatars4change.wallpaper.avatarWallpaperSettings;
 
 public class userData {
 	private static final String TAG = "userData";
 	
 	private static final int BOUNDARY_UPPER = 10;
 	private static final int BOUNDARY_VERYACTIVE_TO_ACTIVE = 6;
 	private static final int BOUNDARY_ACTIVE_TO_SEDENTARY = 3;
 	private static final int BOUNDARY_SEDENTARY_TO_IMMOBILE = 1;
 	private static final int BOUNDARY_LOWER = 0;
 
 	
 	// general user info:
     public static String USERID = "defaultUID";
     
     // available user context data:
     public static String currentActivityName = "???";
     public static int    currentActivityLevel = 0;	 // intstantaneously calculated level of activity {0,10}
 	public static int[]  recentActivityLevels = new int[20]; // last 20 activityLevels {0,10}
     public static float  recentAvgActivityLevel = 0; // currentLevel averaged over a window {0,10}
     public static double[] FFT = new double[65];	//fourier transform of accel data (unknown scale)
     
 	private static int recentSum = 0;	// sum of recent levels
 	
 	private static float min = 100;
 	private static float max = 0;
 	private static int[] activityFrequencies = new int[11];
 	
 	//returns the activity level name consistent with the proteus study activity level names 
 	//    for use like: avatar.setActivityLevel(user.getActivityLevelName())
 	public static String getPAlevelName(){
 		String activLvl = "UKNOWN";
 		if(userData.recentAvgActivityLevel > BOUNDARY_ACTIVE_TO_SEDENTARY){	//if user is walking or greater
 			activLvl = "active";
 		} else if(userData.recentAvgActivityLevel < BOUNDARY_SEDENTARY_TO_IMMOBILE){ //if user not moving at all
 			activLvl = "sleeping";
 		}else{	// user is not active
 			activLvl = "passive";	//TODO: this should be "sedentary" !!!
 		}
 		return activLvl;
 	}
 	
 	//returns a short description of user's physical activity classification for display 
 	//	designed to allow a greater level of detail of description
 	public static String getPAdescription(){
 		String desc = "unknown";
 		if (userData.recentAvgActivityLevel >= BOUNDARY_UPPER){
 			desc = "ERR: exceeds upper bound";
 		} else if(userData.recentAvgActivityLevel > BOUNDARY_VERYACTIVE_TO_ACTIVE){
 			desc = "SUPER active!";
 		} else if(userData.recentAvgActivityLevel > BOUNDARY_ACTIVE_TO_SEDENTARY){
 			desc = "active";
 		} else if(userData.recentAvgActivityLevel > BOUNDARY_SEDENTARY_TO_IMMOBILE){
 			desc = "sedentary";
 		} else if(userData.recentAvgActivityLevel >= BOUNDARY_LOWER){
 			desc = "still";
 		}else{
 			desc = "ERR: exeeds lower bound";
 		}
 		return desc;
 	}
 		
 	//allows restart of personalization of levels
 	public static void resetPAmeasures(){
 		min = 100;
 		max = 0;
 		for (int i = 0; i < activityFrequencies.length; i++) activityFrequencies[i]=0;
 	}
 
 	public static void appendValueAndRecalc(Context c, Intent i, int newV){
 		appendValueAndRecalc(c,i,(float)newV);
 	}
 	public static void appendValueAndRecalc(Context c, Intent i, double newV){
 		appendValueAndRecalc(c,i,(float)newV);
 	}
 	
 	public static void appendValueAndRecalc(Context ctx, Intent intnt, float newV){
 		if (newV < min) {min = newV; if (min > max) max=min+1;}
 		if (newV > max) {max = newV; if (min > max) min=max-1;}
 		
 		currentActivityLevel = scaleValue(newV);
 		Log.d(TAG,"incrementing activityFreq["+Integer.toString(currentActivityLevel)+"]");
 		activityFrequencies[currentActivityLevel] += 1;
 		
 		recentSum = 0;
 		//push buffer to make room for new value
 		for (int i = 0 ; i < recentActivityLevels.length-1 ; i++){
 			recentActivityLevels[i] = recentActivityLevels[i+1];
 			recentSum += recentActivityLevels[i];
 		}
 		recentActivityLevels[recentActivityLevels.length-1] = currentActivityLevel;
 		// add final value to new sum
 		recentSum += recentActivityLevels[recentActivityLevels.length-1];
 		// compute new recent average
 		recentAvgActivityLevel = ((float)recentSum) / ((float)recentActivityLevels.length);
 		
		boolean debug = Boolean.parseBoolean(ctx.getSharedPreferences(ctx.getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
			.getString(ctx.getString(R.string.key_debugmode),"true"));
 		
 		if (debug){
 			showDebugNotification(ctx,intnt);
 		}
 	}
 	
 	//scales the given value to the range {0,10} using min & max
 	private static int scaleValue(float v){
 		float boundary_max = 10, boundary_min = 0;
 		float a = boundary_max - boundary_min;
 		float c = a / (max - min); 
 		return (int)Math.round(c * ((float)v - min) + boundary_min);
 	}
 	
 	public static void setCurrentActivityName(String newName){
     	//Toast.makeText(context, url, Toast.LENGTH_SHORT).show();
 	}
 	
 	public static String getCurrentActivityName(){
 		return currentActivityName;
 	}	
 	
 	//shows a notification with incoming data information
 	private static void showDebugNotification(Context contx, Intent intnt){
 		//send notification to show output:
 		//get ref to NotificationManager
 		String ns = Context.NOTIFICATION_SERVICE;
 		NotificationManager mNotificationManager = (NotificationManager) contx.getSystemService(ns);
 			//initiate the notification
 		int icon = R.drawable.thumb; //(notification icon)
 	
 		CharSequence tickerText = "Avatar debug info available";
 		long when = System.currentTimeMillis();
 		
 		Notification notification = new Notification(icon, tickerText, when);
 			//Define notificaiton's message and PendingIntent:
 		CharSequence contentTitle = "Avatar receiving data from "+activityMonitor.getActivityMonitor();
 		CharSequence contentText = "current:"+Integer.toString(currentActivityLevel)+
 				" avg:"+String.format("%.2f", recentAvgActivityLevel)+
 				" min:"+String.format("%.2f", min)+
 				" max:"+String.format("%.2f", max);
 		PendingIntent contentIntent = PendingIntent.getActivity(contx,0, intnt, 0);
 		notification.setLatestEventInfo(contx, contentTitle, contentText, contentIntent);
 			//Pass notification to manager
 		final int HELLO_ID = 1;
 		mNotificationManager.notify(HELLO_ID, notification);
 		
 		/*
 		//To add vibration, you must include vibrate access permission
 		//vibrate
 		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
 		vibrator.vibrate(2000);
 		*/
 	}
 }
