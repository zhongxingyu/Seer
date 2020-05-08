 package com.mamlambo.globals;
 
 import android.os.Environment;
 
 public class Global {
 
 	public static boolean isDebug = false;
 
 public static int deviceType = 0; // 0 degree correction : Slopeview 1.1 - Model GT-P3113
 //public static int deviceType = 1; // 4 degree correction : Slopeview 1.1 - Model GT-P6210
 	
 	public static String deviceTypeName[] = {"Slopeview 1.2 - Model GT-P3113", "Slopeview 1.2 - Model GT-P6210"};
 
 	public static int 	slopeMeasureType 		= 0;
 	public static float slopeMeasureMeter[] 		= {7.0f, 		20.0f, 		34.0f, 		40.0f, 		50.0f, 		62.5f};
 	
 	public static float slopeHeightOfMesureType[] 	= {135.0f, 		0.0f, 		0.0f, 		0.0f, 		0.0f, 		0.0f};	// Samsung Tablet 1024 * 600
 	
 	public static float slopeBaseMeasureMeter = 7.0f;
 	public static float slopeBaseHeightOfMesureType1 = 135.0f;	// Samsung 	Tablet 1024 * 600, hFOV == 54.8f vFOV == 42.5f , GT-P3113
 	public static float slopeBaseHeightOfMesureType2 = 100.0f;	// Samsung 	Tablet 1024 * 600, hFOV == 59.6f vFOV == 46.3f , GT-P6210
 	public static float slopeBaseHeightOfMesureType3 = 168.0f;	// ASUS 	Tablet 1280 * 800, hFOV == 60.0f vFOV == 60.0f 
 
 	public static float slopeBaseOfferHeight1 = 0.0f;	// Samsung 	Tablet 1024 * 600, hFOV == 54.8f vFOV == 42.5f 
 	public static float slopeBaseOfferHeight2 = 42.0f;	// Samsung 	Tablet 1024 * 600, hFOV == 59.6f vFOV == 46.3f 
 	public static float slopeBaseOfferHeight3 = 0.0f;	// ASUS 	Tablet 1280 * 800, hFOV == 60.0f vFOV == 60.0f 
 	
 	public static final String ARTUTIMAGE_CAPTURE_PATH = Environment.getExternalStorageDirectory() + "/SlopeView";
 	
	public static final float GALAXY_TAB_Z_AXIS_CORRECTION = 1.926f;
 	public static boolean isShowVersionText = true;
 	
 	public static float slopeLineMeter = 0.0f;
 }
