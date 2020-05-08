 package com.daniel.gpslogger;
 
 import com.daniel.gpslogger.modules.GpsModule;
 import com.daniel.gpslogger.modules.SensorModule;
 import com.daniel.gpslogger.util.Writer;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 
 
 public class DataLogger extends Service {
 	
 	private Context context;
 	private Boolean shouldLog = false;
 	private Writer writer;
 	private GpsModule gpsModule;
 	private SensorModule sensorModule;
 	private SharedPreferences prefs;
 	
 	
 	//*****************************************
 	//*               Constructor             *
 	//*****************************************
 	
 	/**
 	 * Constructor of the GPSLogger Class
 	 * 
 	 * @param context	the context of the invoking class (usually the mainActivity)
 	 */
 	public DataLogger(Context c) {
 		//misc
 		this.context = c;
 		this.writer = new Writer("nmea", "gps", context);
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		
 		this.gpsModule = new GpsModule(this.context, (DataLogger) this);
 		this.sensorModule = new SensorModule(this.context);
 		
 	}
 	
 	
 	//*****************************************
 	//*  Methods to be called by MainActivity *
 	//*****************************************
 	
 	/**
 	 * Starts Location and Environment sensors
 	 */
 	public void startGettingData() {
 		this.showMessage("Started Measuring");
 		this.gpsModule.startModuleMeasurement();
 		this.sensorModule.startModuleMeasurement();
 	}
 	
 	/**
 	 * Stops Location and Environment sensors
 	 */
 	public void stopGettingData() {
 		this.showMessage("Stopped Measuring");
 		this.gpsModule.stopModuleMeasurement();
 		this.sensorModule.stopModuleMeasurement();
 	}
 	
 	/**
 	 * Sets the Boolean shouldLog to true, so that GPSLogger starts to actually Log data 
 	 */
 	public void startLogging() {
 		Log.i("DataLogger", "Start Logging");
 		this.shouldLog = true;
 		this.showMessage("Started Logging");
 	}
 	
 	/**
 	 * Stops logging data
 	 */
 	public void stopLogging() {
 		if(this.shouldLog){
 			Log.i("DataLogger", "Stop Logging");
 			writer.finalize();
 			this.shouldLog = false;
 			this.showMessage("Stopped Logging");
 		}
 	}
 	
 	
 	//*****************************************
 	//*    Interface and/or Listner Methods   *
 	//*****************************************
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	//*****************************************
 	//*    Helper Methods, Getters, Setter    *
 	//*****************************************
 	public void collectData() {
 		//Log.i("DataLogger", "retrieving data from sensors");
 		
 		//Get description for location and holding from preferences
 		String separator = ", ";
 		String measurementString = 
 				this.getTime() + separator +
 				prefs.getString("location_value", "err") + separator +
 				prefs.getString("location_extra", "none") + separator +
 				prefs.getString("holding_value", "err") + separator +
 				prefs.getString("holding_extra", "none") + separator +
 				this.gpsModule.getModuleMeasurement() + separator +
 				this.sensorModule.getModuleMeasurement() + separator +
				prefs.getString("cell_id", "none") +  "\n";
 		
 		if(this.shouldLog) {
 			this.writer.addToGps(measurementString);
 		}
 		
 		((MainActivity) this.context).updateView(measurementString);
 	}
 	
 	
 	public void collectNmea(long timestamp, String nmeaSentence) {
 		if(this.shouldLog) {
 			this.writer.addToNmea(Double.toString(timestamp) + ":" + nmeaSentence);
 		}
 	}
 	
 	public Boolean checkDataModulesAvalability() {
 		return this.gpsModule.checkModuleAvailability();
 	}
 	
 	private String getTime() {
 		return Long.toString(System.currentTimeMillis());
 	}
 	
 	private void showMessage(String m) {
 		Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
 	}
 	
 	public Boolean isLogging() {
 		return this.shouldLog;
 	}
 }
