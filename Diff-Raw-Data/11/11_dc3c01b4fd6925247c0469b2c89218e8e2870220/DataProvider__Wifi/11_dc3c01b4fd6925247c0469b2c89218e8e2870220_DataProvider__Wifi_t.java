 package com.example.wsn03;
 
 import java.util.List;
 
 import android.content.Context;
import android.util.Log;
 
 public class DataProvider__Wifi extends DataProvider {
 	private static String THIS = "DataProvider__Wifi";
 	private static final String TABLE_WIFICAPACITY = "wificapacity";
 	private Context context;
 	
 	/*
 	 * ctor
 	 */
 	public DataProvider__Wifi( Context c ){
 		context = c;
 	}
 
 	
 	
 	public boolean onCreate() {
 		Log.d(MainActivity.TAG, THIS + "::onCreate()");
 		db = new DbHelper( context, TABLE_WIFICAPACITY );
 		return true;
 	}
 
 
 	
 	public void batterySave( Integer val, boolean isCharging ){
 // TODO implement isCharging in db and test
 		super.db.addMeasurement( new DataElement( System.currentTimeMillis(), val ));
 	}
 		
 	public List<DataElement> batteryData(){
 		return db.getAllMeasurements( TABLE_WIFICAPACITY );
 	}
 		
 	public void batteryReset(){
 		db.deleteTable( TABLE_WIFICAPACITY );
 	}
 		
 	public void batteryDeleteId( int id ){
 		db.deleteMeasurement(id);
 	}
 		
 	public int count(){
 		return db.getMeasurementsCount();
 	}
 }
