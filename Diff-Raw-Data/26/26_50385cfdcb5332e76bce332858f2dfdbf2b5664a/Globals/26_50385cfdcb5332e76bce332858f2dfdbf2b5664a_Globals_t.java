 package edu.mit.haoqili.camera_cloud;
 
 import java.io.File;
 
 import android.os.Environment;
 
 
 public class Globals {
		// frequently changed contants
    	final static public String CLOUD_SERVER_NAME="hermes5.csail.mit.edu:8213";
    
	
 		// new region calculations
 		// road parameters, used to calculate region width
 		final static public double PHONE_RANGE_METERS = 60; // diagonal of region
 		final static public double ROAD_WIDTH_METERS = 30;
 		// to calculate buffer zone
 		final static public double REGION_WIDTH_BOUNDARY_METERS = 5;
 		
 		
 		final static public int NTHREADS=1;
         final static public boolean CACHE_ENABLED_ON_START = false;
         final static public double BENCHMARK_READ_DISTRIBUTION_ON_START = 0.9f;
         final static public long BENCHMARK_START_DELAY = 1000L; // milliseconds
         final static public String CSM_SERVER_NAME="128.30.87.130:5212"; //128.30.66.123:5212      
 
         final static public int SAMPLING_DURATION=1000;
         final static public int SAMPLING_DISTANCE=1;
         final static public String BROADCAST_ADDRESS="192.168.5.255"; //.255.255 also works
 
         
         // old region calculations
         final static public int REGION_WIDTH=17; // in meters, 35/2
         final static public int SOUTHEAST_LONG = -7110000;
         final static public int SOUTHEAST_LAT = 4236349;
         // Long is x
         final static public int MAX_X_REGIONS=10;
         final static public int MAX_Y_REGIONS=1;
         final static public int MINIMUM_LONGITUDE=SOUTHEAST_LONG - REGION_WIDTH*MAX_X_REGIONS;
         final static public int MINIMUM_LATITUDE=SOUTHEAST_LAT;
         
         
         final static public int SPARSE_NUM_ITER=100000;
 		public static final boolean DEBUG_SKIP_CLOUD = true;
 
 		// photo properties
 		final static public int COMP_QUALITY = 10; // 0 - 100, 100 is max quality
 		final static String PHOTO_PATH = Environment.getExternalStorageDirectory().getName() + File.separatorChar + "temp_photo.jpg";
 		final static int TARGET_SHORT_SIDE = 200;
 		final static int TARGET_LONG_SIDE = 240;
 		final static String PHOTO_KEY = "diplomaPhotos";
 } 
