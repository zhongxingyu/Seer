 package com.example.smartalarm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 /**
  * Fills a given LinkedBlockingQueue with float[]s corresponding to accelerometer data {x, y, z}.
  */
 public class AccelThread extends Thread implements SensorEventListener {
 
 	private boolean running = false;
 	private ArrayList<Long> output = new ArrayList<Long>();
 	private SensorManager manager;
 	private SleepModeActivity activity;
 	private double sensitivity = 0.;
 	private float lastX = 0, lastY = 0, lastZ = 0;
 	private int fellAsleepIdx = -1; //output[fellAsleepIdx] is first data point when user is asleep
 	private long after, before;
 	
 	private List<Long> asleepData() {
 		if (fellAsleepIdx == -1)
 			return new ArrayList<Long>();
 		return output.subList(fellAsleepIdx, output.size());
 	}
 	
 	/**
 	 * New accelerometer monitoring thread
 	 * @param ctx App Context
 	 * @param sensty Minimum accelerometer value 
 	 */
 	public AccelThread(SleepModeActivity act, double sensty, long wakeAfter, long wakeBefore) {
 		setDaemon(true);
 		sensitivity = sensty * sensty; //square now so we don't have to square root all the time
 		activity = act;
 		after = wakeAfter;
 		before = wakeBefore;
 		manager = (SensorManager) act.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
 	}
 	
 	@Override
 	public void run() {
 		running = true;
 		manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 		
 		try {
 			while (true) {
 				try {
 					sleep(60 * 1000);
 				} catch (InterruptedException e) {}
 				if (checkWakeup()){
 					activity.triggerAlarm();
 					break;
 				}
 			}
 		} finally {
 			close(); //clean up even if something weird happens
 		}
 	}
 	
 	public void close() {
 		manager.unregisterListener(this);
 		running = false;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (!running || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
 			return;
 		
 		float x = event.values[0] - lastX, y = event.values[1] - lastY, z = event.values[2] - lastZ;
 		lastX = event.values[0]; lastY = event.values[1]; lastZ = event.values[2];
 			
 		if (x*x + y*y + z*z < sensitivity)
 			return;
 		
 		synchronized (output) {
 			//if they haven't moved over an hour they probably fell asleep
			if (fellAsleepIdx == -1 && output.size() != 0 && System.currentTimeMillis() - output.get(output.size()-1) > 60 * 60 * 1000)
 				fellAsleepIdx = output.size();
 			
 			output.add(System.currentTimeMillis());
 		}
 	}
 	
 	private boolean checkWakeup() {
 		if (fellAsleepIdx == -1)
 			return false; //f'kn insomniac...
 		
 		final long period = 90 * 60 * 1000; //assume sleep cycle length is 90 minutes
 		long phase;
 		
 		synchronized (output) {
 			//the phase is relative to the epoch
 			phase = Analytics.ClockRecover(asleepData(), period);
 		}
 		
 		long idealTime = ((after - phase)/period + 1) * period + phase;
 		long now = System.currentTimeMillis();
 		
 		return now > before //failsafe
 				|| now > idealTime;
 	}
 }
