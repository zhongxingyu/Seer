 package com.wallpaper.sundial;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import java.util.List;
 
 public class SundialWallpaperService extends WallpaperService {
 
 	@Override
 	public Engine onCreateEngine() {
 		return new SundialWallpaperEngine();
 	}
 
 	static {
 	    System.loadLibrary("sundial");
 	}
 	
 	private class SundialWallpaperEngine extends Engine {
 		private boolean mVisible = false;
 		private final Handler mHandler = new Handler();
 
 		// private LocationManager mgr = null;
 		private LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		private LocationListener onLocationChange = new LocationListener() {
 		    public void onLocationChanged(Location location) {
 		        update(location);
 		    }
 		    
 		    public void onProviderDisabled(String provider) {
 		      // required for interface, not used
 		    }
 		    
 		    public void onProviderEnabled(String provider) {
 		      // required for interface, not used
 		    }
 		    
 		    public void onStatusChanged(String provider, int status,
 		                                  Bundle extras) {
 		      // required for interface, not used
 		    }
 		};
 
 		private void update(Location location){
 		    double lng, lat;
 		    if(location != null){
 		        lat = location.getLatitude();
 		        lng = location.getLongitude();
 		    } else {
 		        lat = 37.871592;
 		        lng = -122.2937;
 		    }
 		    
 		    double[] sunPos = getSunPos(lat, lng);
 		}
 
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder)
 		{
		    // super.onCreate(savedInstanceState);
		    // mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		    mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 		                               3600000, 1000,
 		                               onLocationChange);
 		}
 
		public native double[]  getSunPos(double lat, double lng);

 		private final Runnable mUpdateDisplay = new Runnable() {
 		@Override
 		public void run() {
 			draw();
 		}};
 	
 		private void draw() {
 		   SurfaceHolder holder = getSurfaceHolder();
 		   Canvas c = null;
 		   try {
 		      c = holder.lockCanvas();
 		      if (c != null) {
 		      	double lng, lat;
		      	if(location != null){
		      	    lat = location.getLatitude();
		      	    lng = location.getLongitude();
		      	} else {
		      	    lat = 37.871592;
		      	    lng = -122.2937;
		      	}
 		      	
 		      	double[] sunPos = getSunPos(lat, lng);

 		    	 //android.os.Debug.waitForDebugger(); 
 		    	 Paint p = new Paint();
 		         p.setTextSize(20);
 		 		 p.setAntiAlias(true);
		 		 // String text = "system time: "+Long.toString(System.currentTimeMillis());
 		 		 String text = ( "Elevation: " + sunPos[0] + 
                     			 "\nAzimuth: " + sunPos[1]);
 		         float w = p.measureText(text, 0, text.length());
 		         int offset = (int) w / 2;
 				 int x = c.getWidth()/2 - offset;
 		         int y = c.getHeight()/2;
 		         p.setColor(Color.BLACK);
 		         c.drawRect(0, 0, c.getWidth(), c.getHeight(), p);
 		         p.setColor(Color.WHITE);
 		         c.drawText(text, x, y, p);
 		      }
 		   } finally {
 		      if (c != null)
 		         holder.unlockCanvasAndPost(c);
 		   }
 		   mHandler.removeCallbacks(mUpdateDisplay);
 	       if (mVisible) {
 	           mHandler.postDelayed(mUpdateDisplay, 100);
 	       }
 		}
 		
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			mVisible = visible;
 			if (visible) {
 				draw();
 			} else {
 				mHandler.removeCallbacks(mUpdateDisplay);
 			}
 		}
 		
 		 @Override
 	      public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	         draw();
 	      }
 		
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			mVisible = false;
 			mHandler.removeCallbacks(mUpdateDisplay);
 		}
 		
 		@Override
 	    public void onDestroy() {
 	         super.onDestroy();
 	         mVisible = false;
 	         mHandler.removeCallbacks(mUpdateDisplay);
 	    }
 	}    
 }
 
 
 
 // public class Sundial extends Activity
 // {
 //     private TextView tv = null;
 //     private LocationManager mgr = null;
 //     private LocationListener onLocationChange = new LocationListener() {
 //         public void onLocationChanged(Location location) {
 //             update(location);
 //         }
         
 //         public void onProviderDisabled(String provider) {
 //           // required for interface, not used
 //         }
         
 //         public void onProviderEnabled(String provider) {
 //           // required for interface, not used
 //         }
         
 //         public void onStatusChanged(String provider, int status,
 //                                       Bundle extras) {
 //           // required for interface, not used
 //         }
 //     };
 
 //     private void update(Location location){
 //         double lng, lat;
 //         if(location != null){
 //             lat = location.getLatitude();
 //             lng = location.getLongitude();
 //         } else {
 //             lat = 37.871592;
 //             lng = -122.2937;
 //         }
         
 //         double[] sunPos = getSunPos(lat, lng);
 
 //         tv.setText( "Elevation: " + sunPos[0] + 
 //                     "\nAzimuth: " + sunPos[1]);
 //         setContentView(tv);
 //     }
 
 //     @Override
 //     public void onCreate(Bundle savedInstanceState)
 //     {
 //         super.onCreate(savedInstanceState);
 //         mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 //         tv = new TextView(this);
 //         mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 //                                    3600000, 1000,
 //                                    onLocationChange);
 //         // lat = 37.871592;
 //         // lng = -122.2937;
 //     }
 
 //     public native double[]  getSunPos(double lat, double lng);
 
 //     static {
 //         System.loadLibrary("sundial");
 //     }
 // }
