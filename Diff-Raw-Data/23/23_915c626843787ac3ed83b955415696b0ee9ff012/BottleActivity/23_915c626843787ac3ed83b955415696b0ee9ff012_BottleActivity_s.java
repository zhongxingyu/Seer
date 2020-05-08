 package mamont.bottle;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 
 
 public class BottleActivity extends Activity 
 {
 	static String TAG = "bottle"; 
 	String urlServer = "http://mamont.co/post2.php";
 	String lineEnd = "\r\n";
 	String twoHyphens = "--";
 	String boundary =  "*****";
 	long periodMs = 5000;				//... First period.
 	
 	TextView log;
 	Camera camera = null;
 	SurfaceView preview = null;
 	SurfaceHolder previewHolder = null;
 	
 	public static final String ACTION_NAME = "mamont.bottle.ALARM"; 
 	private IntentFilter alarmFilter = new IntentFilter(ACTION_NAME); 
 	
 	
 	boolean locationListenerNetworkRegisterd = false;
 	int numNetworkLocationUpdates = 0;
 	boolean locationListenerGpsRegisterd = false;
 	int numGpsLocationUpdates = 0;
 	
 	int currentBatteryLevel = -1;
     int currentBatteryStatus = -1; 
     int currentBatteryTemperature = -1; 
     int currentBatteryVoltage = -1; 
    int currentSignalStrength;
     
     Location currentNetworkLocation;
     Location currentGpsLocation;
     long locationPeriodMs = 0;
     
     byte[] currentPicture;
     
     int gpsLocationsSent = 0;
     int picturesSent = 0;
     int bytesSent = 0;
     
     AlarmManager alarmMgr = null;
     PendingIntent alarmPending = null;
     //PowerManager powerManager;
     LocationManager locationManager = null;
 
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		Log.i(TAG, "-> onCreate");
 		
 		log = (TextView)findViewById(R.id.log);
 		log.setText("started\n");
 		
 		/*Window window = getWindow();
 		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); 
 		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);*/ 
 
 	    //powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
 
 		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
 
 		
 		// Alarm.
 
 		registerReceiver(alarmReceiverCB, alarmFilter); 
 		
 		final Intent intent = new Intent(ACTION_NAME);
 		alarmPending = PendingIntent.getBroadcast(this, 0, intent, 0);
 		
 		alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
 		alarmMgr.cancel(alarmPending);
 		alarmMgr.setRepeating(
 			AlarmManager.RTC_WAKEUP, 
 			System.currentTimeMillis() + periodMs,
 			periodMs, 
 			alarmPending);
 			
 		// 09	  Intent intent = new Intent(getBaseContext(), OnAlarmReceive.class);
 		// 10	  PendingIntent pendingIntent = PendingIntent.getBroadcast(
 		// 11	     MainActivity.this, 0, intent,
 		// 12	     PendingIntent.FLAG_UPDATE_CURRENT);
 
 
 		// Camera.
 		
 		preview = (SurfaceView)findViewById(R.id.preview);
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCB);
 		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		
 		// Battery status.
 		
 		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED); 
 	    registerReceiver(batteryReceiverCB, filter); 
 	    
 	    
 	    // Cellular network status.
 	    
         TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
         telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);  
 		
 
 	    // Done.
 	
 		log.append("initialized\n");
 		Log.i(TAG, "<- onCreate");
 	}
 
 	
 	
 	public void act()
 	{
 		Log.i(TAG, "-> act");
 				
 		
 		// Battery status and signal.
 		
 		new Thread(new Runnable() 
 		{
 	        public void run() 
 	        {
 	    		send("battery.txt", (currentBatteryLevel + "," + currentBatteryStatus + "," + currentBatteryTemperature + "," + currentBatteryVoltage + "," + currentSignalStrength).getBytes());
 	        }
 		}).start();
 		
 		
 		// Change alarm period.
 
 		int newPeriodMs;
 		if (currentBatteryLevel > 80)
 			newPeriodMs = 1 * 60000;
 		if (currentBatteryLevel > 50)
 			newPeriodMs = 5 * 60000;
 		else if (currentBatteryLevel > 25)
 			newPeriodMs = 15 * 60000;
 		else if (currentBatteryLevel > 10)
 			newPeriodMs = 60 * 60000;
 		else
 			newPeriodMs = 4 * 60 * 60000;
 
 		
 				newPeriodMs = 5 * 60 * 1000;		//...
 		
 		
 		if (newPeriodMs != periodMs)
 		{
 			periodMs = newPeriodMs;
 		
 			alarmMgr.cancel(alarmPending);
 			alarmMgr.setRepeating(
 				AlarmManager.RTC_WAKEUP, 
 				System.currentTimeMillis() + periodMs,
 				periodMs, 
 				alarmPending);
 		}
 		
 		
 		
 		// Location.
 		
 		if (!locationListenerNetworkRegisterd)
 		{
 			locationListenerNetworkRegisterd = true;
 			numNetworkLocationUpdates = 0;
 			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListenerNetworkCB);
 		}
 		
 		if (!locationListenerGpsRegisterd)
 		{
 			locationListenerGpsRegisterd = true;
 			numGpsLocationUpdates = 0;
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListenerGpsCB);
 		}
 
 		
 		// Camera.
 		
 		if (camera == null)
 		{
 			Log.i(TAG, "open camera");
 		
 			try
 			{	
 				camera = Camera.open();
 				initCamera();
 				camera.setPreviewDisplay(previewHolder);
 				camera.startPreview();
 				camera.takePicture(null, null, pictureCB);
 			}
 			catch (Throwable t)
 			{
 				Log.e(TAG, "camera failed", t);
 			}
 		}
 		else
 		{
 			Log.i(TAG, "camera skipped");
 		}
 		
 		
 		// Update on-screen info.
 		
 	    log.append("g=" + gpsLocationsSent + ", p=" + picturesSent + ", b=" + bytesSent + "   ");
 	    
 	    //powerManager.goToSleep(SystemClock.uptimeMillis() + 1);
 	    
 		Log.i(TAG, "<- act");
 	}
 	
 	
 	
 	public static class BootUpReceiverCB extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent) 
 		{
 			Log.i(TAG, "-> BootUpReceiverCB::onReceive"); 
 
 			Intent startIntent = new Intent(context, BottleActivity.class);  
 			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(startIntent);  
 
 			Log.i(TAG, "<- BootUpReceiverCB::onReceive"); 
 		}
 	}
 	
 
 
 	BroadcastReceiver alarmReceiverCB = new BroadcastReceiver() 
 	{ 
 		@Override 
 		public void onReceive(Context context, Intent intent) 
 		{ 
 			Log.i(TAG, "-> alarmReceiverCB::onReceive"); 
 			
 			/*Intent startIntent = new Intent(context, BottleActivity.class);
 			startIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			context.startActivity(startIntent);
 			*/
 			
 			act();
 			
 			Log.i(TAG, "<- alarmReceiverCB::onReceive"); 
 		}
 	}; 
 	
 	
 	
 	
 	BroadcastReceiver batteryReceiverCB = new BroadcastReceiver() 
 	{ 
         public void onReceive(Context context, Intent intent) 
         { 
             Log.i(TAG, "-> batteryReceiverCB::onReceive"); 
             
             int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); 
             int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); 
             currentBatteryLevel = 100 * level / scale;
             currentBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
             currentBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1); 
             currentBatteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1); 
             
             Log.i(TAG, "<- batteryReceiverCB::onReceive"); 
         } 
     }; 
 	
 	
 	
 	SurfaceHolder.Callback surfaceCB = new SurfaceHolder.Callback() 
 	{
 		public void surfaceCreated(SurfaceHolder holder) 
 		{
 			Log.i(TAG, "-> surfaceCB::surfaceCreated"); 
 			Log.i(TAG, "<- surfaceCB::surfaceCreated"); 
 		} 
 		
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
 		{
 			Log.i(TAG, "-> surfaceCB::surfaceChanged"); 
 			Log.i(TAG, "<- surfaceCB::surfaceChanged"); 
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {}
 	};
 	
 	
 	
 	private void initCamera() 
 	{
 		//TODO: Configure camera here.
 	}    
 
 
 
 	PictureCallback pictureCB = new PictureCallback() 
 	{
 		public void onPictureTaken(byte[] data, Camera in_camera) 
 		{
 			Log.i(TAG, "-> PictureCB::onReceive"); 
 		
 			currentPicture = data;
 			new Thread(new Runnable() 
 			{
 		        public void run() 
 		        {
 					send("cam.jpg", currentPicture);
 					picturesSent++;
 		        }
 			}).start();
 			
 			camera.stopPreview(); 
 			camera.setPreviewCallback(null); 
 			camera.release();
 			camera = null;
 
 			Log.i(TAG, "<- PictureCB::onReceive"); 
 		}
 	};
 	
 	
 	
 	LocationListener locationListenerNetworkCB = new LocationListener()
 	{
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 		public void onProviderEnabled(String provider) {}
 		public void onProviderDisabled(String provider) {}
 
 		public void onLocationChanged(Location location) 
 		{
 			Log.i(TAG, "-> LocationListenerNetworkCB::onLocationChanged");
 			
 			numNetworkLocationUpdates++;
 
 			if (numNetworkLocationUpdates == 2)
 			{
 				currentNetworkLocation = location;
 				new Thread(new Runnable() 
 				{
 			        public void run() 
 			        {
 						send(currentNetworkLocation.getProvider() + ".txt", currentNetworkLocation.toString().getBytes());
 			        }
 				}).start();
			}
 
			locationManager.removeUpdates(locationListenerNetworkCB);
			locationListenerNetworkRegisterd = false;
 			
 			Log.i(TAG, "<- LocationListenerNetworkCB::onLocationChanged"); 
 		}
 	};
 	
 
 	
 	LocationListener locationListenerGpsCB = new LocationListener()
 	{
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 		public void onProviderEnabled(String provider) {}
 		public void onProviderDisabled(String provider) {}
 
 		public void onLocationChanged(Location location) 
 		{
 			Log.i(TAG, "-> LocationListenerGpsCB::onLocationChanged"); 
 		
 			numGpsLocationUpdates++;
 
			if (numGpsLocationUpdates == 2)
 			{
 				currentGpsLocation = location;
 				new Thread(new Runnable() 
 				{
 			        public void run() 
 			        {
 						send(currentGpsLocation.getProvider() + ".txt", currentGpsLocation.toString().getBytes());
 						gpsLocationsSent++;
 			        }
 				}).start();
 			}
 			
			locationManager.removeUpdates(locationListenerGpsCB);
			locationListenerGpsRegisterd = false;

 			Log.i(TAG, "<- LocationListenerGpsCB::onLocationChanged"); 
 		}
 	};
 	
 	
 	
 	PhoneStateListener phoneStateListener = new PhoneStateListener()
 	{
       public void onSignalStrengthsChanged(SignalStrength signalStrength) 
       { 
     	  currentSignalStrength = signalStrength.getGsmSignalStrength();
       }
     }; 
 
     
 	
 	
 	
 	void send(String fileName, byte[] data)
 	{
 		try 
 		{
 			URL url = new URL(urlServer);
 			
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 			connection.setUseCaches(false);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Connection", "Keep-Alive");
 			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
 			
 			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
 			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
 			outputStream.writeBytes(lineEnd);
 			
 			outputStream.write(data);
 
 			outputStream.writeBytes(lineEnd);
 			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 			outputStream.flush();
 
 			//int serverResponseCode = connection.getResponseCode();
 			String serverResponseMessage = connection.getResponseMessage();
 			
 			//Log.e(TAG, serverResponseCode);
 			Log.e(TAG, serverResponseMessage);
 			
 			outputStream.close();
 			
 			bytesSent += data.length;
 		}
 		catch (UnknownHostException e) 
 		{
 			Log.e(TAG, "send: server not found");
 		} 
 		catch (IOException e) 
 		{
 			Log.e(TAG, "send: couldn't open socket");
 		}
 		catch (Throwable e) 
 		{
 			Log.e(TAG, "send: unknown exception");
 		}
 	}
 		
 	
 	
 	@Override
 	public void onResume() 
 	{
 		Log.i(TAG, "-> onResume");
 		
 		super.onResume();		
 
 		Log.i(TAG, "<- onResume");
 	}
 	  
 	
 	
 	@Override
 	public void onPause() 
 	{
 		Log.i(TAG, "-> onPause");
 			
 		super.onPause();
 	  
 		Log.i(TAG, "<- onPause");
 	}
 }
 
