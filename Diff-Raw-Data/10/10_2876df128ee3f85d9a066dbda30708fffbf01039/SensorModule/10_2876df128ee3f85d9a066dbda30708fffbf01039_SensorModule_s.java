 package com.daniel.gpslogger.modules;
 
 import com.daniel.gpslogger.R;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 
 
 public class SensorModule extends Service implements ModuleInterface, SensorEventListener {
 	
 	
 	//*****************************************
 	//*               Fields                  *
 	//*****************************************
 	private Context context;
 	private SensorType[] sensorList;
 	private PackageManager pm;
 	private SensorManager sm;
 	
 	
 	//*****************************************
 	//*               Constructor             *
 	//*****************************************
 	public SensorModule(Context c) {
 		
 		//misc
 		this.context = c;
 		this.pm = this.context.getPackageManager();
 		this.sm = (SensorManager) this.context.getSystemService(SENSOR_SERVICE);
 		
 		this.sensorList = new SensorType[4];
 		this.sensorList[0] = new SensorType(PackageManager.FEATURE_SENSOR_BAROMETER, Sensor.TYPE_PRESSURE, "Barometer");
 		this.sensorList[1] = new SensorType(PackageManager.FEATURE_SENSOR_COMPASS, Sensor.TYPE_MAGNETIC_FIELD, "Compass");
 		this.sensorList[2] = new SensorType(PackageManager.FEATURE_SENSOR_GYROSCOPE, Sensor.TYPE_GYROSCOPE, "Gyroscope");
 		this.sensorList[3] = new SensorType(PackageManager.FEATURE_SENSOR_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER, "Accelerometer");
 		
 		for(int i = 0 ; i < sensorList.length ; i++) {
 			this.sensorList[i].isAvailable = pm.hasSystemFeature(sensorList[i].featureSensor);
 		}
 	}
 	
 	
 	//*****************************************
 	//*  Methods to be called by MainActivity *
 	//*****************************************
 	@Override
 	public void startModuleMeasurement() {
 		
 		int delay = SensorManager.SENSOR_DELAY_NORMAL;
 		for(int i = 0; i < this.sensorList.length ; i++) {
 			if(sensorList[i].isAvailable) {
 				Log.i("SensorModule", "Start sensor " + this.sensorList[i].description);
 				sm.registerListener(this, sm.getDefaultSensor(this.sensorList[i].sensorType), delay);
 			} else {
 				Log.w("SensorModule", "Unit has no " + this.sensorList[i].description);
 				this.showMessage(context.getResources().getString(R.string.noSensor) + " " + this.sensorList[i].description);
 			}
 		}
 	}
 	
 	@Override
 	public void stopModuleMeasurement() {
 		for(int i = 0; i < this.sensorList.length ; i++) {
 			if(sensorList[i].isAvailable) {
 				Log.i("SensorModule", "Stop sensor " + this.sensorList[i].description);
 				sm.unregisterListener(this, sm.getDefaultSensor(this.sensorList[i].sensorType));
 			} 
 		}
 	}
 	
 	@Override
 	public Boolean checkModuleAvailability() {
 		boolean atLeastOne = false;
 		for(int i = 0; i < this.sensorList.length ; i++) {
 			if(sensorList[i].isAvailable) {
 				atLeastOne = true;
 			} 
 		}
 		return atLeastOne;
 	}
 	
 	@Override
 	public String getModuleMeasurement() {
 		//Log.i("SensorModule", "returning sensor data");
 		
 		String resultString = "{";
 		String separator = "##";
 		String separator2 = "**";
 		
 		for(int i = 0; i < this.sensorList.length ; i++) {
 			resultString += "{";
 			if(sensorList[i].isAvailable) {
 				resultString += this.sensorList[i].description + separator;
 				for(int y = 0 ; y < this.sensorList[i].sensorValues.length ; y++) {
 					resultString += this.sensorList[i].sensorValues[y] + separator;
 				}
 				//remove last separator
 				resultString = resultString.substring(0, resultString.length()-separator.length());
 			} 
 			resultString += "}" + separator2;
 		}
 		//remove last separator
 		resultString = resultString.substring(0, resultString.length()-separator2.length());
 		
 		resultString += "}";
 		return  resultString;
 	}
 	
 	
 	//*****************************************
 	//*    Interface and/or Listner Methods   *
 	//*****************************************
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		//Log.i("SensorModule", "SENSORDATA RECEIVED");
 		int index = -1;
 		switch(event.sensor.getType()) {
 			case Sensor.TYPE_PRESSURE:
 				index = 0;
 				break;
 				 
 			case Sensor.TYPE_MAGNETIC_FIELD:
 				index = 1;
 	            break;
 	            
 			case Sensor.TYPE_GYROSCOPE:
 				index = 2;
 				break;
 				
 			case Sensor.TYPE_ACCELEROMETER:
 				index = 3;
 				break;
 		}
 		
		this.sensorList[index].sensorValues = event.values;
 		
 	}
 	
 	
 	//*****************************************
 	//*    Helper Methods, Getters, Setter    *
 	//*****************************************
 	
 	private void showMessage(String m) {
 		Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
 	}
 }
