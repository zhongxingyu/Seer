 package edu.wsn.phoneusage.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.util.Pair;
 import android.widget.ArrayAdapter;
 import edu.wsn.phoneusage.main.SystemInfo;
 /**
  * 
  * @author Lothar Rubusch
  */
 public class DataProvider_battery extends DataProvider {
 	public DataProvider_battery(Context context){
 		super(context);
 		this._tablename = SystemInfo.DB_TABLENAME_BATTERY;
 		this._tableargs = new ArrayList<Pair<String, String>>();
		this._tableargs.add(new Pair<String, String>("KEY_VALUE", "INTEGER"));
		this._tableargs.add(new Pair<String, String>("KEY_CHARGING", "INTEGER"));
 		db.onCreate();
 	}
 
 	public void generatePlotData( ArrayAdapter< String > arrayadapter, List<Number> xseries, List<Number> yseries ){
 		arrayadapter.add("Time : " + SystemInfo.DB_TABLENAME_BATTERY);
 
 		List<DataElement> dataelements = null;
 		try{
 			dataelements = data();
 		}catch( Exception e ){ // general exception, so far...
 			Log.w(SystemInfo.TIG, TAG + "::displayData() - no data returned");
 			dataelements = new ArrayList<DataElement>();
 		}
 		long value;
 		String charging = "";
 		int dataelements_size = dataelements.size();
 		for( int idx=0; idx<dataelements_size; ++idx ){
 			String timestamp = dataelements.get(idx).getTimestamp();
 
 //			value = dataelements.get(idx).getValue();
			value = Long.parseLong( dataelements.get(idx).getValue("KEY_VALUE") );
 
 //			switch ( ((DataElement_battery) dataelements.get(idx)).charging() ){
			switch( Integer.parseInt( dataelements.get(idx).getValue("KEY_CHARGING"))){
 				case 0:
 					charging = SystemInfo.BATTERY_NOTCHARGING;
 					break;
 				case 1:
 					charging = SystemInfo.BATTERY_CHARGINGAC;
 					break;
 				case 2:
 					charging = SystemInfo.BATTERY_CHARGINGUSB;
 					break;
 				default:
 					break;
 			}
 	
 			// set line
 			arrayadapter.add( timestamp + ": " + value + "% (" + charging + ")" );
 
 			// only show last 10 values (I'm sure there are better ways to do this)
 			if( idx > (dataelements_size - SystemInfo.PLOT_BATTERY_VALS)){
 				xseries.add( idx );
 				yseries.add( value );
 			}
 		}
 	}
 }
