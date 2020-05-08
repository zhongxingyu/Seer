 package org.robotics.nao.model;
 
 import org.robotics.nao.Application;
 import org.robotics.nao.bo.Nao;
 import org.robotics.nao.controller.AccelerometerController;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorManager;
 
 public class Accelerometer {
 	private AccelerometerController accCtrl;
 	private SensorManager sensorMgr;
 	private boolean accelSupported;
 	private boolean bSensorRunning;
 	private Application application;
 	public Accelerometer(Application application){
 		this.application=application;
 		accCtrl=new AccelerometerController(this);
 		sensorMgr = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
 		bSensorRunning=false;
 	}
 	
 	public void startAccelerometerActivity(){
 		if(!bSensorRunning){
			accelSupported = sensorMgr.registerListener(accCtrl, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
     		bSensorRunning=runAccelerometer();
     		if (!bSensorRunning)
     			application.showMessageBox("Accelerometer not supported!");
     	}
     	else
     		bSensorRunning=stopAccelerometer();
 	}
 	
 	public  boolean runAccelerometer(){
 		if(!accelSupported){
 			sensorMgr.unregisterListener(accCtrl, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
 			return false;
 		}
 		return true;
 	}
 
 	public boolean stopAccelerometer(){
 		sensorMgr.unregisterListener(accCtrl, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
 		return false;
 	}
 	
 	public boolean isbSensorRunning() {
 		return bSensorRunning;
 	}
 	public  void onAccelerometerChanged(SensorEvent event){
 		float x,y,z;
 		x = event.values[0];
 		y = event.values[1];
 		z = event.values[2];
 		Nao.getInstance().walkTo(x, y, z);
 	}
 
 }
