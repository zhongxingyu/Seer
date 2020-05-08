 package com.example.wsn03;
 
 import android.util.Log;
 
 /**
  * database table entry class
  * 
  * @author Lothar Rubusch
  */
 public class DataElement{
 	
 	private int _id;
 	private Long _timestamp;
 	private Integer _value;
 
 	/*
 	 * ctor
 	 */
 	public DataElement(){
 	}
 	
 	/*
 	 * tools
 	 */
 // TODO function necessary?
 	private Long timeConversion( Long ts ){
 		Log.d(MainActivity.TAG, "DataElement::timeConversion()");
 //		Log.d(MainActivity.TAG, "ts(before): " + String.valueOf(ts));
 		// ts per min, in int
 //		return ts / 60000;
 		
 		// ts per hour, in int
 //		return ts / 3600000;
 
 //		ts = ts / 3600000;
		if( 0 == MainActivity.DB.count() ){
			MainActivity.DB.setInitialMillis( ts );
 		}
		ts -= MainActivity.DB.initialMillis();
 		
 //		Log.d(MainActivity.TAG, "ts(after): " + String.valueOf(ts));
 		return ts;
 	}
 	
 	/*
 	 * access
 	 */
 	
 	public DataElement( Long ts, Integer val){
 		this.setTimestamp( this.timeConversion( ts ));
 		this.setValue( val );
 	}
 	
 	public DataElement( int id, Long ts, Integer val){
 		this.setID( id );
 		this.setTimestamp( this.timeConversion( ts ));
 		this.setValue( val );
 	}
 	
 	/*
 	 * getter / setter
 	 */
 
 	public int getID(){
 		return this._id;
 	}
 	
 	public void setID( int id ){
 		this._id = id;
 	}
 	
 	public Long getTimestamp(){
 //		Log.d(MainActivity.TAG, "getTimestamp()");
 //		Log.d(MainActivity.TAG, "timestamp: " + String.valueOf(this._timestamp)); // XXX
 		return this._timestamp;
 	}
 	
 	public void setTimestamp( Long t ){
 		this._timestamp = t;
 	}
 	
 	public Integer getValue(){
 		return this._value;
 	}
 	
 	public void setValue( Integer v ){
 		this._value = v;
 	}
 }
