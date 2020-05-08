 /**
  * 
  */
 package bzb.android.logger;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.GpsSatellite;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.GpsStatus.Listener;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 
 /**
  * @author bzb
  *
  */
 public class LoggingService extends Service implements SensorEventListener, LocationListener, Listener {
 
 	private PowerManager.WakeLock wl;
 
 	public LoggingService () {
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent arg0) {return null;}
 
 	public void onCreate() {
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE, getClass().getName());
         wl.acquire();
         Log.i(getClass().getName(),"Acquired wakelock");
         
 		super.onCreate();
 		Log.i(getClass().getName(),"Started service");
 		
 		if (Config.logSensors) {
 			try {
 				startLogging();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (Config.logGps) {
 			try {
 				startTracking();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 		if (Config.logMedia == Config.MEDIA_LOG_AUDIO) {
 			try {
 				startAudioCapture();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void onPause() {
 		Log.i(getClass().getName(), "Service paused; why?");
 	}
 
 	public void onDestroy() {
 		if (wl != null) {
 			wl.release();
 			Log.i(getClass().getName(),"Released wakelock");
 		}
 		
 		if (sensorManager != null) {
 			sensorManager.unregisterListener(this);
 			Log.i(getClass().getName(), "Unregistered sensor listener");
 		}
 		
 		if (locationManager != null) {
 			locationManager.removeGpsStatusListener(this);
 			locationManager.removeUpdates(this);
 			Log.i(getClass().getName(), "Unregistered GPS listeners");
 		}
 		
 		if (audioRecorder != null) {
 			try {
 				audioRecorder.stop();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			}
 		    audioRecorder.release();
 		    Log.i(getClass().getName(), "Stopped audio recorder");
 		}
 		
 		super.onDestroy();
 		
 		if (sCaptureFile != null) {
 			sCaptureFile.close();
 			Log.i(getClass().getName(),"Closed sensor log file");
 		}
 		
 		if (gCaptureFile != null) {
 			gCaptureFile.close();
 			Log.i(getClass().getName(),"Closed GPS log file");
 		}
 		
 		Log.i(getClass().getName(),"Destroyed service");
 	}
 	
 	//////////////////////////////////
 	
 	private SensorManager sensorManager;
 	private PrintWriter sCaptureFile;
 	
 	private void startLogging () throws IOException {
		File captureFileName = new File( Environment.getExternalStorageDirectory(), "slog.csv" );
 		Log.i(getClass().getName(),"Linked to sensor log file");
 		sCaptureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
 		Log.i(getClass().getName(),"Opened file " + captureFileName.getName());
 		
         sensorManager = 
 		    (SensorManager)getSystemService(SENSOR_SERVICE);
 		List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
 		Log.i(getClass().getName(),"Listed sensors");
 		
 		for (Sensor sensor : sensors) {
 			sensorManager.registerListener( 
 		            this, 
 		            sensor,
 		            SensorManager.SENSOR_DELAY_UI );
 			Log.i(getClass().getName(),"Registered listener for sensor " + sensor.getName());
 			sCaptureFile.println(sensor.getType() + "," + sensor.getName() + "," + sensor.getResolution() + "," + sensor.getMaximumRange() + "," + sensor.getPower());
 		}
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
         sCaptureFile.print(System.currentTimeMillis() + "," + event.sensor.getType() + ",");
         //Log.i( getClass().getName(), event.sensor.getName() + " onSensorChanged" );
         if( sCaptureFile != null ) {
                 for( int i = 0 ; i < event.values.length ; ++i ) {
                     if( i > 0 )
                         sCaptureFile.print( "," );
                     sCaptureFile.print( Float.toString( event.values[i] ) );
                 }
                 sCaptureFile.println();
         }
 	}
 	
 	//////////////////////////////////
 	
 	private LocationManager locationManager;
 	private PrintWriter gCaptureFile;
 	
 	private void startTracking () throws IOException {
		File captureFileName = new File( Environment.getExternalStorageDirectory(), "glog.csv" );
 		Log.i(getClass().getName(),"Linked to GPS log file");
 		gCaptureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
 		Log.i(getClass().getName(),"Opened file " + captureFileName.getName());
 		
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		/*List<String> providers = locationManager.getProviders(true);
 		Log.i(getClass().getName(),"Listed providers");
 		
 		for (String provider : providers) {
 			Log.i(getClass().getName(),"Enabled provider " + provider);
 		}*/
 		
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			locationManager.addGpsStatusListener(this);
 			
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
 			Log.i(getClass().getName(),"Started GPS tracking");
 		} else {
 			Log.i(getClass().getName(),"GPS is not enabled; no GPS tracking");
 		}
 	}
 	
 	@Override
 	public void onLocationChanged(Location arg0) {
 		gCaptureFile.println(System.currentTimeMillis() + "," + arg0.getLatitude() + "," + arg0.getLongitude() + "," + arg0.getAccuracy() + "," + arg0.getAltitude() + "," + arg0.getBearing() + "," + arg0.getSpeed());
 		Log.i(getClass().getName(), "GPS location changed: lat="+arg0.getLatitude()+", lon="+arg0.getLongitude());
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {}
 
 	@Override
 	public void onProviderEnabled(String arg0) {}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
 
 	private int satellitesSeen = -1;
 	
 	@Override
 	public void onGpsStatusChanged(int arg0) {
 		switch (arg0) {
 		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
 			GpsStatus status = locationManager.getGpsStatus(null);
 			int i = 0;
 			for (GpsSatellite gps : status.getSatellites()) {
 				i++;
 			}
 			if (i != satellitesSeen) {
 				satellitesSeen = i;
 				gCaptureFile.println(System.currentTimeMillis() + "," + satellitesSeen);
 				Log.i(getClass().getName(), satellitesSeen + " satellites seen");
 			}
 			break;
 		case GpsStatus.GPS_EVENT_FIRST_FIX:
 			Log.i(getClass().getName(), "GPS fix acquired");
 			break;
 		case GpsStatus.GPS_EVENT_STARTED:
 			Log.i(getClass().getName(), "GPS fired up");
 			break;
 		case GpsStatus.GPS_EVENT_STOPPED:
 			Log.i(getClass().getName(), "GPS powered down");
 			break;
 		}	
 	}
 	
 	//////////////////////////////////
 	
 	private MediaRecorder audioRecorder;
 	
 	private void startAudioCapture () throws IllegalStateException, IOException {
 		audioRecorder = new MediaRecorder();
 		
 	    // could use setPreviewDisplay() to display a preview to suitable View here
 	    
 	    audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 	    audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 	    audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 	    audioRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/alog_" + System.currentTimeMillis() + ".3gp");
 	    
 	    audioRecorder.prepare();
 	    Log.i(getClass().getName(), "Prepared audio recorder");
 	    
 	    audioRecorder.start();
 	    Log.i(getClass().getName(), "Started audio recorder");
 	}
 
 }
