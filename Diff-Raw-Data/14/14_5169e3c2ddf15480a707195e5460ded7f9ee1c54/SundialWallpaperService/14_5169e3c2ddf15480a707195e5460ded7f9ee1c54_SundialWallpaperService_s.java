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
 // import java.lang.Math;
 
 public class SundialWallpaperService extends WallpaperService {
 
     @Override
     public Engine onCreateEngine() {
         return new SundialWallpaperEngine();
     }
 
     public native double[]  getSunPos(double lat, double lng);
     public native double[]  getMoonPos(double lat, double lng);
 
     static {
         System.loadLibrary("sundial");
     }
 
     public double[] test = getSunPos(100.0, 100.0);
     
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
             mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        3600000, 1000,
                                        onLocationChange);
         }
 
         private final Runnable mUpdateDisplay = new Runnable() {
         @Override
         public void run() {
             draw();
         }};
 
         private double testMoonPos(double lat, double lng){
             double jd = 2456296.74972;
             double T = (jd - 2451545.0) / 36525.0;
             // T = -0.0862137805;
             double eps = 23.0 + 26.0/60.0 + 21.448/3600.0 - 
                         (46.8150*T+ 0.00059*T*T- 0.001813*T*T*T)/3600.0;
             double X = Math.cos(lat)*Math.cos(lng);
             double Y = (Math.cos(eps)*Math.cos(lat)*Math.sin(lng)) - Math.sin(eps)*Math.sin(lat);
             double Z = (Math.sin(eps)*Math.cos(lat)*Math.sin(lng)) - Math.cos(eps)*Math.sin(lat);
             double R = Math.sqrt(1.0-(Z*Z));
 
             double delta = (180.0/Math.PI)*Math.atan2(Z,R); // in degrees
             double RA = (24.0/Math.PI)*Math.atan2(Y,X+R); // in hours
 
             double theta0 = 280.46061837 + 360.98564736629*(jd-2451545.0) + 0.000387933*T*T - (T*T*T/38710000.0); // degrees
 
             double theta = theta0 + lng;
             double tau = theta - RA;
 
             double h = Math.asin(Math.sin(lat )*Math.sin(delta) + Math.cos(lat)*Math.cos(delta)*Math.cos(tau));
             double az = Math.atan2(-Math.sin(tau), Math.cos(lat)*Math.tan(delta) - Math.sin(lat)*Math.cos(tau));
 
             // note moon distance in AU
             double horParal = 8.794 / (384400.0/14959787E6); // horizontal parallax (arcseconds)
             double p = Math.cos(h)*Math.sin(horParal/3600.0); // parallax in altitude (degrees)
 
             return h;
         }
     
         private void draw() {
                

            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
               c = holder.lockCanvas();
               if (c != null) {
                 double lng, lat;
                 lat = 37.871592;
                 lng = -122.2937;
                 
                 double[] sunPos = getSunPos(lat, lng);
                 double[] moonPos = getMoonPos(lat, lng);
                 double test = testMoonPos(lat, lng);
                 //android.os.Debug.waitForDebugger(); 
                 Paint p = new Paint();
                 p.setTextSize(20);
                 p.setAntiAlias(true);
                 // String text = ( "Elevation: " + sunPos[0] + 
                 //                 "\nAzimuth: " + sunPos[1]);
 
                  double midpoint = c.getWidth() / 2.0;
 
                 int x = (int) Math.round(sunPos[1] / 360.0 * c.getWidth());
                 int _y = (int) Math.round(sunPos[0] / 90.0 * c.getHeight());
                 // int _y = (int) Math.round(sunPos[0]);
                 int y = c.getHeight() - _y;
                 y = 300;
 
                 String text = y+"";
                text = moonPos[0]+"";
 
                  // x = c.getWidth() / 2;
                  // y = c.getHeight() /2;
 
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
