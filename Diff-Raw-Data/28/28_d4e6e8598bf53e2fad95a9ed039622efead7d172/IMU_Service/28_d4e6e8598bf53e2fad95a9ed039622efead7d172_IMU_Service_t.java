 package de.fraunhofer.ipa;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.WindowManager;
 import android.webkit.MimeTypeMap;
 import android.widget.Toast;
 
 public class IMU_Service extends Service implements SensorEventListener {
 
 	private SensorManager mSensorManager;
 	private float[] values_ = new float[9];
 	private boolean running = true;
	private MediaPlayer mPlayer = null;
 
 	public static USB_Interface usb_intf = null;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		usb_intf = new USB_Interface();
 
 		running = true;
 
 		// Get an instance of the SensorManager
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
 		onResume();
 		new Thread(new ServerThread()).start();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		running=false;
 		usb_intf.close();
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	protected void onResume() {
 		mSensorManager.registerListener(
 				this, 
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_GAME );
 		mSensorManager.registerListener(
 				this, 
 				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
 				SensorManager.SENSOR_DELAY_GAME );
 		mSensorManager.registerListener(
 				this, 
 				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
 				SensorManager.SENSOR_DELAY_UI );
		mSensorManager.registerListener(
				this, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
				SensorManager.SENSOR_DELAY_UI );
 	}
 
 	protected void onPause() {
 		mSensorManager.unregisterListener(this);
 	}
 
 	public class ServerThread implements Runnable {
 
 		private String fileExt(String url) {
 			if (url.indexOf("?")>-1) {
 				url = url.substring(0,url.indexOf("?"));
 			}
 			if (url.lastIndexOf(".") == -1) {
 				return null;
 			} else {
 				String ext = url.substring(url.lastIndexOf(".") );
 				if (ext.indexOf("%")>-1) {
 					ext = ext.substring(0,ext.indexOf("%"));
 				}
 				if (ext.indexOf("/")>-1) {
 					ext = ext.substring(0,ext.indexOf("/"));
 				}
 				return ext.toLowerCase();
 
 			}
 		}
 
 		private void startUri(String uri) {
 			try {
 				MimeTypeMap myMime = MimeTypeMap.getSingleton();
 
 				Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
 
 				//Intent newIntent = new Intent(Intent.ACTION_VIEW);
 				String mimeType = myMime.getMimeTypeFromExtension(fileExt(uri).substring(1));
 				newIntent.setDataAndType(Uri.parse(uri),mimeType);
 				newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
 				try {
 					startActivity(newIntent);
 				} catch (android.content.ActivityNotFoundException e) {
 					Toast.makeText(getApplicationContext(), "No handler for this type of file.", 4000).show();
 				}
 			} catch(Exception e) {
 				Log.e("cmd", ""+e);
 			}
 		}
 
 		private void startApp(String app) {
 			try {
 				Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(app);
 				LaunchIntent.setFlags(LaunchIntent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(LaunchIntent);
 			} catch(Exception e) {
 				Log.e("cmd", ""+e);
 			}
 		}
 
 		@Override
 		public void run() {
 			//TODO: should not be here
 			String s="";
 
 			while(running) {
 				s += usb_intf.read();
 				while(s.length()>0) {
 					String key;
 					int pos;
 
 					key="startLinphone";
 					if( (pos=s.indexOf(key))>=0 )
 					{
 						startApp("org.linphone");
 					}
 
 					if(pos<0) {
 						key="on";
 						if( (pos=s.indexOf(key))>=0 )
 						{
 							((Activity)getApplicationContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 						}
 					}
 
 					if(pos<0) {
 						key="off";
 						if( (pos=s.indexOf(key))>=0 )
 						{
 							WindowManager.LayoutParams attrs = ((Activity)getApplicationContext()).getWindow().getAttributes();
 							attrs.flags &= (~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 							((Activity)getApplicationContext()).getWindow().setAttributes(attrs);
 						}
 					}
 
 					if(pos<0) {
 						key="play";
 						if( (pos=s.indexOf(key))>=0 )
 						{
 							int pos2;
 							if( (pos2=s.indexOf(";"))<0) pos=-1;
 							else {
								mPlayer = MediaPlayer.create(null, Uri.parse(s.substring(pos+key.length(), pos2)));
 
 								try {
 									mPlayer.start();
 								} catch (Exception e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 
 							}
 							
 							pos = pos2;
 							key = ";";
 						}
 					}
 
 					if(pos<0) {
 						key="start";
 						if( (pos=s.indexOf(key))>=0 )
 						{
 							int pos2;
 							if( (pos2=s.indexOf(";"))<0) pos=-1;
 							else {
 								try {
									startUri(s.substring(pos+key.length(), pos2));
 								} catch (Exception e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
 							
 							pos = pos2;
 							key = ";";
 						}
 					}
 
 					if(pos>=0)
 						s = s.substring(pos+key.length());
 					else {
 						s = s.trim();
 						break;
 					}
 
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent evt) {
 		int type=evt.sensor.getType();
 		int pos = -1;
 
 		//Smoothing the sensor data a bit
 		if (type == Sensor.TYPE_MAGNETIC_FIELD) {
 			pos = 2;
 		} else if (type == Sensor.TYPE_ACCELEROMETER) {
 			pos = 0;
 		} else if (type == Sensor.TYPE_GYROSCOPE) {
 			pos = 1;
		} else if (type == Sensor.TYPE_ORIENTATION) {
			usb_intf.write(val2str6(evt.values));
 		}
 
 		if(pos<0) return;
 
 		for(int i=0; i<3; i++)
 			values_ [pos*3+i]=evt.values[i];
 
 		if (type == Sensor.TYPE_MAGNETIC_FIELD)
 			usb_intf.write(val2str5());
 		else 
 			usb_intf.write(val2str34());
 	}
 
	private String val2str6(float[] values) {
		//compatible to Wireless IMU
		String s = "0, 6";
		for(int i=0; i<3; i++)
			s += ", "+values[i]/180.*Math.PI;
		return s+"|\n";
	}

 	private String val2str() {
 		//compatible to Wireless IMU
 		String s = "0, 3";
 		for(int i=0; i<3; i++)
 			s += ", "+values_[i];
 		s += ", 4";
 		for(int i=3; i<6; i++)
 			s += ", "+values_[i];
 		s += ", 5";
 		for(int i=6; i<9; i++)
 			s += ", "+values_[i];
 		return s+"|\n";
 	}
 
 	private String val2str34() {
 		//compatible to Wireless IMU
 		String s = "0, 3";
 		for(int i=0; i<3; i++)
 			s += ", "+values_[i];
 		s += ", 4";
 		for(int i=3; i<6; i++)
 			s += ", "+values_[i];
 		return s+"|\n";
 	}
 
 	private String val2str5() {
 		//compatible to Wireless IMU
 		String s = "0, 5";
 		for(int i=6; i<9; i++)
 			s += ", "+values_[i];
 		return s+"|\n";
 	}
 } 
