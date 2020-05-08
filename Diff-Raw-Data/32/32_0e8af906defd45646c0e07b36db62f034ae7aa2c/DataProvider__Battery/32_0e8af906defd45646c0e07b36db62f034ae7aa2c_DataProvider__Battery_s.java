 package com.example.wsn03;
 
 import java.util.List;
 
 import android.content.Context;
 
 public class DataProvider__Battery extends DataProvider {
 	private static final String TABLE_BATTERYCAPACITY = "batterycapacity";
 
 
 	public DataProvider__Battery(Context c) {
 		super(c);
		// TODO Auto-generated constructor stub
 	}
 
 	
	// TODO keep it like this, or use derrived classes? functionality in common?
 	public void batterySave( Integer val, boolean isCharging ){
	// TODO implement isCharging in db and test
 		super.db.addMeasurement( new DataElement( System.currentTimeMillis(), val ));
 	}
 		
 	public List<DataElement> batteryData(){
 		return db.getAllMeasurements();
 	}
 		
 	public void batteryReset(){
 		db.deleteTable( TABLE_BATTERYCAPACITY );
 	}
 		
 	public void batteryDeleteId( int id ){
 		db.deleteMeasurement(id);
 	}
 		
 	public int count(){
 		return db.getMeasurementsCount();
 	}
 }
