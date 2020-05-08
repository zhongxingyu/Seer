 package com.wallpaper.sundial;
 
 import java.util.List;
 import java.util.Calendar;
 import java.util.TimeZone;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Bitmap;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.graphics.PixelFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.Display;
 import android.view.WindowManager;
 
 public class SundialWallpaperService extends WallpaperService {
 
     @Override
     public Engine onCreateEngine() {
         return new SundialWallpaperEngine();
     }
 
     public native double[] getSunPos(int hour, int min, int sec, double lat, double lng);
     public native double[] getMoonPos(int hour, int min, int sec, double lat, double lng);
 
     static {
         System.loadLibrary("sundial");
     }
     
     private class SundialWallpaperEngine extends Engine {
         private boolean mVisible = false;
         private final Handler mHandler = new Handler();
        private double i = 16.0;
         private double j = 0.0;
         private double aspectRatio;
         private int screenWidth;
         private int screenHeight;
         private double lat;
         private double lng;
         private int celestialOffset = 350;
         private double setStart;
         private double setEnd;
 
         private Bitmap moonBitmap;
         private Bitmap sunBitmap;
         private Bitmap bgBitmap;
         private Bitmap bgBitmapSmall;
         private Bitmap moonBitmapSmall;
         private GradientDrawable bgGrad;
         private Paint p;
 
         private LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
         private LocationListener onLocationChange = new LocationListener() {
             public void onLocationChanged(Location location) {
                 update(location);
             }
             
             public void onProviderDisabled(String provider) {}
             public void onProviderEnabled(String provider) {}
             public void onStatusChanged(String provider, int status, Bundle extras) {}
         };
 
         private void update(Location location){
             if(location != null){
                 lat = location.getLatitude();
                 lng = location.getLongitude();
             } else {
                 lat = 37.871592;
                 lng = -122.2937;
             }
         }
 
         // moon's opacity is a function of how far the sun has set
         private double getMoonOpacity(double setStart, double setEnd, double y, int hour, int min, int sec){
             
             // int setStart = celestialOffset;
             // int setEnd = celestialOffset/2;
             if(y < setStart){
                 if(y < setEnd){
                     Log.v("com.wallpaper.sundial", "moon opacity:"+1.0);
                     return 1.0;
                 } else {
                     // double time = hour/24 + min/60 + sec/60/60;
                    double yScale = (setEnd-15) / y;
                     Log.v("com.wallpaper.sundial", "moon opacity:"+ yScale);
 
                     return yScale;
                 }
             } else {
                 Log.v("com.wallpaper.sundial", "moon opacity:"+0.0);
                 return 0.0;
             }
         }
 
         private int[] getBgColors(int hour, int min, int sec){
             int[] colors = {0,0};
 
             return colors;
         }
 
         private double[] scaleCelestialPosition(Bitmap img, double x, double y, int screenWidth, int screenHeight){
            // double newX = x / 360.0 * screenWidth;
            // double newY = y / 90.0 * (screenHeight-celestialOffset);
             double newX = x / 360.0 * screenWidth;
            double newY = y / 90.0 - celestialOffset);
             newY = newY + celestialOffset;
             newX = newX - img.getWidth()/2.0;
             newY = newY - img.getWidth()/2.0;
            return new double[] {newX, newY};
            // if(newY < 0.0){
            //     return new double[] {-100, -100};
            // } else {
            //     return new double[] {newX, newY};
            // }
         }
 
         // Device represents 0,0 as the top left corner of the screen
         private void toDeviceSpace(Point p, int screenHeight){
             p.y = screenHeight - p.y;
         }
 
         private double toDeviceSpace(double y, int screenHeight){
             return screenHeight - y;
         }
 
         private boolean isVisible(Point p){
             return p.y > celestialOffset;
         }
 
         @Override
         public void onCreate(SurfaceHolder surfaceHolder)
         {
             update(null);
             surfaceHolder.setFormat(PixelFormat.RGBA_8888);
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inPreferredConfig = Bitmap.Config.ARGB_8888;
             
             int colors[] = {0xFFEEFFFF, 0xFF2F3B47};
             bgGrad = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
 
             p = new Paint(){
                 {
                     setAntiAlias(true);
                     setFilterBitmap(true);
                     setDither(true);
                 }
             };
             moonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moon, options);
             sunBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun, options);
             bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg, options);
 
            setStart = 200;
             setEnd = 150;
 
             mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        3600000, 1000,
                                        onLocationChange);
         }
 
         private final Runnable mUpdateDisplay = new Runnable() {
             @Override
             public void run() {
                 draw();
         }};
 
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
             screenWidth = width;
             screenHeight = height;
             aspectRatio = (double) screenWidth / (double) screenHeight;
 
             // this could have fixed-aspect ratio scaling
             bgBitmapSmall = Bitmap.createScaledBitmap(bgBitmap, 
                 screenWidth, 
                 screenHeight, 
                 false);
 
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
     
         private void draw() {
             
             SurfaceHolder holder = getSurfaceHolder();
             holder.setFormat(PixelFormat.RGBA_8888);
 
             Canvas c = null;
             try {
                 c = holder.lockCanvas();
                 if (c != null) {
                     Calendar now = Calendar.getInstance(TimeZone.getDefault());
                     int sec = now.get(Calendar.SECOND);
                     int min = now.get(Calendar.MINUTE);
                     int hour = now.get(Calendar.HOUR_OF_DAY); // 24h format
                     hour = (int) i;
                     min = (int) j;
 
                     Log.v("com.wallpaper.sundial", "android log: time: hour: " + hour + " min: " + min);
 
                     double[] sunPos = getSunPos(hour, min, sec, lat, lng);
                     double[] moonPos = getMoonPos(hour, min, sec, lat, lng);
 
                     double[] moon = scaleCelestialPosition(moonBitmap, moonPos[1], moonPos[0], screenWidth, screenHeight);
                     double[] sun = scaleCelestialPosition(sunBitmap, sunPos[1], sunPos[0], screenWidth, screenHeight);
 
                     double moonOpacity = getMoonOpacity(setStart, setEnd, sun[1], hour, min, sec);
 
                     Point moonCoord = new Point((int) Math.round(moon[0]), (int) Math.round(moon[1]));
                     Point sunCoord = new Point((int) Math.round(sun[0]), (int) Math.round(sun[1]));
 
                     // sunCoord.y = celestialOffset;
 
                     toDeviceSpace(moonCoord, screenHeight);
                     toDeviceSpace(sunCoord, screenHeight);
 
                     Log.v("com.wallpaper.sundial", "android log: moon alt " + moonPos[0] + " moon azi "+ moonPos[1] + "i: "+i +"\n");
                     Log.v("com.wallpaper.sundial", "android log: sun alt " + sunPos[0] + " sun azi "+ sunPos[1] + "i: "+i +"\n");
 
                     // Drawing
                     ////////////////////////////
 
                     p.setColor(Color.BLACK);
                     c.drawRect(0, 0, c.getWidth(), c.getHeight(), p);
                     p.setColor(Color.WHITE);
 
                     bgGrad.setBounds(0,0,screenWidth,screenHeight);
                     bgGrad.draw(c);
 
                     if(sunCoord.y > toDeviceSpace(setStart, screenHeight)){
                         p.setAlpha((int) Math.round(moonOpacity*255));
                         Log.v("com.wallpaper.sundial", "alpha: "+ moonOpacity);
                         c.drawBitmap(moonBitmap, moonCoord.x, moonCoord.y, p);
                         p.setAlpha(255);
                     }
                     if(isVisible(sunCoord)){
                         c.drawBitmap(sunBitmap, sunCoord.x, sunCoord.y, p);
                     }
                     c.drawBitmap(bgBitmapSmall, 0, 0, p);
                 }
             } finally {
                 if (c != null)
                 holder.unlockCanvasAndPost(c);
             }
 
             mHandler.removeCallbacks(mUpdateDisplay);
             if (mVisible) {
                 mHandler.postDelayed(mUpdateDisplay, 100);
             }
 
             // Time mock
            j+=5;
             if(j > 60) { 
                 j = 0;
                 i++;
             }
             if(i > 23) i = 0;
         }
     }
 }
