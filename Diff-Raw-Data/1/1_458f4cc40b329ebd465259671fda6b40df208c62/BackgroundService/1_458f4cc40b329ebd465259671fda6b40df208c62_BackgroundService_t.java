 package com.application.wakeapp;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.widget.Toast;
 
 import java.util.Locale;
 
 /**
  * Created by Radovan on 2013-07-07.
  */
 public class BackgroundService extends Service {
     private int searchRadius;
     private int outsidethreshold;
     private int setradius;
 
     private Float currentSpeed;
     private LocationListener mLocationListener;
     private NotificationManager mNotificationManager;
     private Location finalDestination;
     private LocationManager locationManager;
     private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
     // The minimum time between updates in milliseconds
     private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
     private TextToSpeech tts;
     private SharedPreferences prefs;
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         System.out.println("Radde123 Service: onStartCommand");
         mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         Notification not = new Notification(R.drawable.ic_launcher, "WakeApp", System.currentTimeMillis());
         PendingIntent contentIntent =
                 PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                         Notification.FLAG_ONGOING_EVENT);
 
         not.flags = Notification.FLAG_ONGOING_EVENT;
         not.setLatestEventInfo(this, "WakeApp", "Running in background", contentIntent);
         mNotificationManager.notify(1, not);
 
         prefs =  PreferenceManager.getDefaultSharedPreferences(this);
         setradius = Integer.parseInt(prefs.getString("setradius","120"));
 
         System.out.println("Radde123 setradius: " + setradius);
 
         finalDestination = new Location("Destination");
         finalDestination.setLongitude(intent.getExtras().getDouble("lng"));
         finalDestination.setLatitude(intent.getExtras().getDouble("lat"));
 
         mLocationListener = new LocationListener() {
 
             @Override
             public void onLocationChanged(Location location) {
                 int distance;
                 currentSpeed = location.getSpeed();
                 distance = Math.round(location.distanceTo(finalDestination));
                 System.out.println("Radde123 onLocationChanged " + location.getProvider());
                 if ( distance < setradius ){
                    String msg = "You have reached your destination";
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                    notifyUserDestinationReached();
                 }
             }
 
             @Override
             public void onStatusChanged(String s, int i, Bundle bundle) {
 
             }
 
             @Override
             public void onProviderEnabled(String s) {
 
             }
 
             @Override
             public void onProviderDisabled(String s) {
 
             }
         };
 
         startGPS();
 
         return Service.START_NOT_STICKY;
     }
     public void startGPS(){
         locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 
         locationManager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,
                 mLocationListener);
 
         locationManager.requestLocationUpdates(
                 LocationManager.NETWORK_PROVIDER,
                 MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,
                 mLocationListener);
     }
     private void notifyUserDestinationReached(){
         tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener(){
             public void onInit(int status) {
                 if (status == TextToSpeech.SUCCESS) {
                     System.out.println("Radde123 SUCCESS ");
                     System.out.println("Radde123 engine: " + tts.getDefaultEngine());
 
                     int result = tts.setLanguage(Locale.US);
 
                     if (result == TextToSpeech.LANG_MISSING_DATA
                             || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                         System.out.println("Radde123 This Language is not supported");
                     } else {
                         AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
 
                         //If we do not have headset just vibrate
                         if (!amanager.isWiredHeadsetOn()){
                             System.out.println("Radde123 isWiredHeadsetOn");
                             int dot = 200;      // Length of a Morse Code "dot" in milliseconds
                             int dash = 500;     // Length of a Morse Code "dash" in milliseconds
                             int short_gap = 200;    // Length of Gap Between dots/dashes
                             int medium_gap = 500;   // Length of Gap Between Letters
                             int long_gap = 1000;    // Length of Gap Between Words
                             long[] pattern = {
                                     0,  // Start immediately
                                     dot, short_gap, dot, short_gap, dot,    // s
                                     medium_gap,
                                     dash, short_gap, dash, short_gap, dash, // o
                                     medium_gap,
                                     dot, short_gap, dot, short_gap, dot,    // s
                                     long_gap
                             };
 
                             Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                             v.vibrate(pattern,-1);
 
                         } else if (amanager.isWiredHeadsetOn() && amanager.isMusicActive()){
                             System.out.println("Radde123 music active");
                             //Turn off music
                             Intent intent = new Intent("com.android.music.musicservicecommand.togglepause");
                             getApplicationContext().sendBroadcast(intent);
 
                             amanager.setStreamVolume(AudioManager.STREAM_MUSIC,12,0);
 
                             result = tts.speak("You have arrived at your final destination",
                                     TextToSpeech.QUEUE_FLUSH, null);
 
                             if (result == TextToSpeech.ERROR)
                                 System.out.println("Radde123 speach failed");
 
                         } else {
                             amanager.setStreamVolume(AudioManager.STREAM_MUSIC,12,0);
                             result = tts.speak("You have arrived at your final destination",
                                     TextToSpeech.QUEUE_FLUSH, null);
 
                             if (result == TextToSpeech.ERROR)
                                 System.out.println("Radde123 speach failed");
                         }
 
                     }
                 } else {
                     System.out.println("Radde123 Initilization Failed!");
                 }
             }
         });
         stopGPS();
         stopSelf();
     }
     public void stopGPS(){
         locationManager.removeUpdates(mLocationListener);
     }
     @Override
     public void onDestroy(){
         mNotificationManager.cancelAll();
        stopGPS();
         System.out.println("Radde123 Service: onDestroy");
        // stopGPS();
         //stopSelf();
     }
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
