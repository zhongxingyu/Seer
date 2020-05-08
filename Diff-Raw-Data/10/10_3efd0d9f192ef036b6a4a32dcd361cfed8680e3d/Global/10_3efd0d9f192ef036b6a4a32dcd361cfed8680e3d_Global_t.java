 package edu.mit.csail.ada;
 
 import com.google.android.gms.location.DetectedActivity;
 
 import android.content.Context;
 
 public class Global {
 	/** Constants */
 	public static final int ACTIVITY_NUM = 5;
 	public static final int ACCEL_FEATURE_NUM = 3;
 	public static final String accelTrainingDataFilename = "accel_train";
 	public static final String wifiTrainingDataFilename = "wifi_15sec_train";
 	public static final String gpsTrainingDataFilename = "gps_train";
 	
 	public static final int ACCEL_TIMEWINDOW_SEC = 5; //SEC
 	public static final double ACCEL_FEATURE_KDE_BW = 0.2;
 	public static final double WIFI_FEATURE_KDE_BW = 0.3;
 	public static final double GPS_FEATURE_KDE_BW = 0.5;
 	public static final int STATIC = 0;
 	public static final int WALKING = 1;
 	public static final int RUNNING = 2;
 	public static final int BIKING = 3;
 	public static final int DRIVING = 4;
 	public static final int UNKNOWN = -1;
 	
 	public static final int LOOKBACK_NUM = 2;
 	public static final int INVALID_FEATURE = -1;
 	public static long startTime = 0;
 	public static Context context;
	public static final double EWMA_ALPHA = 0.4; // Current weight
 	
 	public static int GooglePrediction = -1;
 	public static int AdaPrediction = -1;
 	public static int gt = 0;
 	
 	public static final int UPDATE_UI_MSG = 2;
 	public static final int MSG_REGISTER_CLIENT = 0;
 	public static final int MSG_UNREGISTER_CLIENT = 1;
 	public static void setContext(Context ctx){
 		context = ctx;
 	}
 	
 	public static void setGroundTruth(String activity){
 		if (activity.equals("Static")){
 			gt = STATIC;
 		}else if(activity.equals("Walking")){
 			gt = WALKING;
 		}else if(activity.equals("Running")){
 			gt = RUNNING;
 		}else if(activity.equals("Biking")){
 			gt = BIKING;
 		}else if(activity.equals("Driving")){
 			gt = DRIVING;
 		}else{
 			gt = UNKNOWN;
 		}
 	}
 	
 	public static String getAdaFriendlyGroundTruth(int gt){
 	
 		switch (gt){
 		case STATIC:
 			return "Still";
 		case WALKING:
 			return "Walking";
 		case RUNNING:
 			return "Running";
 		case BIKING:
 			return "Biking";
 		case DRIVING:
 			return "Driving";
 		default:
 			return "Unknown";
 		}
 	}
 	
 	public static String getGoogleFriendlyName(int detected_activity_type) {
 		switch (detected_activity_type) {
 		case DetectedActivity.IN_VEHICLE:
 			return "Vehicle";
 		case DetectedActivity.ON_BICYCLE:
 			return "Biking";
 		case DetectedActivity.ON_FOOT:
 			return "Foot";
 		case DetectedActivity.TILTING:
 			return "Tilting";
 		case DetectedActivity.STILL:
 			return "Still";
 		default:
 			return "Unknown";
 		}
 	}
 	
 	public static int getGroundTruth(){
 		return gt;
 	}
 }
