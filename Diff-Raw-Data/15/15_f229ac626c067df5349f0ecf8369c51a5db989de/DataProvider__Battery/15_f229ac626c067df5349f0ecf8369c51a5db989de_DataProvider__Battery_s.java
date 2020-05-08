 package com.example.wsn03;
 
 import java.util.List;
 
 import com.example.wsn03.DataProvider.DbHelper;
 
 import android.content.Context;
 
 public class DataProvider__Battery extends DataProvider {
 	private static final String TABLE_BATTERYCAPACITY = "batterycapacity";
 	private Context context;
 	
 	/*
 	 * ctor
 	 */
 	public DataProvider__Battery(Context c) {
		super(c);
 		context = c;
 	}
 
 	
 	
 	public boolean onCreate() {
 		// clean up first
 		db = new DbHelper( context, TABLE_BATTERYCAPACITY );
 		return true;
 	}
 
 
 	
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
