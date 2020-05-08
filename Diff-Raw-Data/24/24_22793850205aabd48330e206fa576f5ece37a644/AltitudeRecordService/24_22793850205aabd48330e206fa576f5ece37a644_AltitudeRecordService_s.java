 
 package org.crocodile.altituderecorder;
 
 import java.io.*;
 import java.util.Date;
 
 import android.app.*;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.*;
 import android.location.GpsStatus.NmeaListener;
 import android.location.*;
 import android.os.*;
 import android.util.Log;
 
 public class AltitudeRecordService extends Service implements NmeaListener, LocationListener, SensorEventListener
 {
     private static final String DATA_FILE_SUFFIX   = ".txt";
     private static final String DATA_FILE_PREFIX   = "altitude";
 
     private int                 START_NOTIFICATION = R.string.start_notification;
     private int                 STOP_NOTIFICATION  = R.string.stop_notification;
 
     private IBinder             binder             = new LocalBinder();
 
     private String              fname;
     Writer                      fwriter;
 
     public class LocalBinder extends Binder
     {
         AltitudeRecordService getService()
         {
             return AltitudeRecordService.this;
         }
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return binder;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         startRecording();
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public void onDestroy()
     {
         stopRecording();
         super.onDestroy();
     }
 
     private void sendBroadcast(int token, String extra)
     {
         Intent i = new Intent(Constants.BROADCAST_TAG);
         i.putExtra(Constants.BROADCAST_TAG, token);
         i.putExtra(Constants.BROADCAST_FNAME, extra);
         this.sendBroadcast(i);
     }
 
     public void startRecording()
     {
         if(fwriter == null)
         {
             try
             {
                 fname = getNewLogFile();
                 fwriter = new FileWriter(fname);
                 Log.d(Constants.LOGTAG, "Will write data to file " + fname);
 
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                 lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_INTERVAL, 0, this);
                lm.addNmeaListener(this);
 
                SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
                 Sensor baro = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
                sm.registerListener(this, baro, Constants.UPDATE_INTERVAL);
 
                 showNotification(START_NOTIFICATION);
                 sendBroadcast(Constants.SERVICE_STARTED_TOKEN, fname);
             } catch(IOException e)
             {
                 Log.e(Constants.LOGTAG, "Error starting service", e);
                 if(fwriter != null)
                 {
                     try
                     {
                         fwriter.close();
                     } catch(IOException e1)
                     {
                         // ignore
                     }
                     fwriter = null;
                 }
             }
         }
     }
 
     public void stopRecording()
     {
         if(fwriter != null)
         {
             LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
             lm.removeUpdates(this);
             lm.removeNmeaListener(this);
 
             SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
             sm.unregisterListener(this);
 
             try
             {
                 fwriter.close();
             } catch(IOException e1)
             {
                 // ignore
             }
 
             fwriter = null;
             showNotification(STOP_NOTIFICATION);
             sendBroadcast(Constants.SERVICE_STOPPED_TOKEN, fname);
         }
     }
 
     public boolean isRecording()
     {
         return fwriter != null;
     }
 
     /**
      * Show a notification while this service is running.
      */
     private void showNotification(int n)
     {
         CharSequence text = getText(n);
 
         // The PendingIntent to launch our activity if the user selects this
         // notification
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
 
         Notification notification = new Notification.Builder(this).setContentTitle(getText(R.string.app_name))
                 .setContentText(text).setSmallIcon(R.drawable.ic_launcher).setContentIntent(contentIntent).build();
 
         NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         nm.notify(n, notification);
     }
 
     private String getNewLogFile() throws IOException
     {
         File fdir = this.getFilesDir();
         File f = File.createTempFile(DATA_FILE_PREFIX, DATA_FILE_SUFFIX, fdir);
         return f.getAbsolutePath();
     }
 
     @Override
     public void onNmeaReceived(long timestamp, String nmea)
     {
         // Units of timestamp in NmeaListener are not documented, but
         // seems to be in milliseconds since Java epoch (same as
         // System.currentTimeMillis())
         String msg = "" + timestamp + Constants.TIMESTAMP_SEPARATOR + nmea.trim();
         logData(msg);
     }
 
     @Override
     public void onSensorChanged(SensorEvent event)
     {
         if(Sensor.TYPE_PRESSURE == event.sensor.getType())
         {
             StringBuffer msg = new StringBuffer();
             // event.timestamp is nanoseconds since boot!
             // See: https://code.google.com/p/android/issues/detail?id=7981
             msg.append(uptimeNSToEpochMS(event.timestamp));
             msg.append(Constants.TIMESTAMP_SEPARATOR);
             msg.append(Constants.BARO_PREFIX);
             msg.append(',');
             msg.append(event.accuracy);
 
             /*
              * 3 - SensorManager.SENSOR_STATUS_ACCURACY_HIGH - high accuracy; 2
              * - SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM - medium accuracy;
              * 1 - SensorManager.SENSOR_STATUS_ACCURACY_LOW - low accuracy; 0 -
              * SensorManager.SENSOR_STATUS_UNRELIABLE - accuracy is unreliable
              * and cannot be trusted
              */
 
             msg.append(',');
             msg.append(event.values[0]); // hPa
             logData(msg.toString());
         }
     }
 
     private long uptimeNSToEpochMS(long uptime_ns)
     {
         // See
         // http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
        // This conversion could introduce insignificant error due to added delay of taking time. 
         long timeInMillis = (new Date()).getTime() + (uptime_ns - System.nanoTime()) / 1000000L;
         return timeInMillis;
     }
 
     private void logData(String msg)
     {
         // TODO: should be performed via async task!
         try
         {
             if(fwriter != null)
                 fwriter.write(msg + "\n");
         } catch(IOException e)
         {
             Log.e(Constants.LOGTAG, "Error writing log. Stopping service", e);
             stopRecording();
         }
     }
 
     // ----- Nothing interesting below -------
 
     @Override
     public void onLocationChanged(Location location)
     {
         // Intentionally left blank. We implement LocationListener only to allow
         // event flow to NmeaListener
     }
 
     @Override
     public void onProviderDisabled(String provider)
     {
         // Intentionally left blank. We implement LocationListener only to allow
         // event flow to NmeaListener
     }
 
     @Override
     public void onProviderEnabled(String provider)
     {
         // Intentionally left blank. We implement LocationListener only to allow
         // event flow to NmeaListener
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras)
     {
         // Intentionally left blank. We implement LocationListener only to allow
         // event flow to NmeaListener
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy)
     {
         // Intentionally left blank.
     }
 
 }
