 package com.rimproject.main;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.rimproject.andsensor.R;
 import com.rimproject.andsensor.R.id;
 import com.rimproject.andsensor.R.layout;
 import com.rimproject.andsensor.R.string;
 import com.rimproject.contextanalyzer.DeviceStatus;
 import com.rimproject.fileio.FileLoggingIO;
 import com.rimproject.logger.ContextLogger;
 import com.rimproject.logger.LoggerManager;
 import com.rimproject.logreader.AccelerometerReading;
 import com.rimproject.logreader.BasicLogReading;
 import com.rimproject.logreader.ContextReading;
 import com.rimproject.logreader.LocationGPSReading;
 
 public class MainView extends Activity implements OnClickListener {
 	Button toggleLogging;
 	Button refresh;
 	boolean isLogging;
 	TextView outputView;
 	LoggerManager logger;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		isLogging = false;
 
 		toggleLogging = (Button) findViewById(R.id.toggleLogging);
 		toggleLogging.setText(R.string.start);
 		toggleLogging.setOnClickListener(this);
 
 		refresh = (Button) findViewById(R.id.refresh);
 		refresh.setText(R.string.refresh);
 		refresh.setOnClickListener(this);
 
 		outputView = (TextView) findViewById(R.id.output);
 
 		this.logger = new LoggerManager();
 	}
 
 	public void onClick(View v) {
 		
 		/*Code to test if device is charging
 		 * 
 		DeviceStatus deviceStatus = new DeviceStatus();
 		
 		if(deviceStatus.isDeviceCharging() == 2 || deviceStatus.isDeviceCharging() == 1){
 			outputView.setText("Device is charging");
 		}
 		else{
 			outputView.setText("Device is not charging");
 		}
 		*/
 			
 		
 		// Debugging code to test that what is loaded in the HashMap is same as what is in the file
 		// it is also useful to demo how to use the read method
 		//-------------------------
 
 //		 Date d1 = BasicLogReading.getDateFromString("07-30-2011 20:42:23");
 //		    Date d2 = BasicLogReading.getDateFromString("07-30-2011 20:42:28");
 //		    
 //    	    FileLoggingIO<AccelerometerReading> fio = new FileLoggingIO<AccelerometerReading>();
 //            HashMap<Date,List<AccelerometerReading>> map = fio.readFromTXTLogFile(AccelerometerLogger.SENSOR_NAME, new AccelerometerReading(), null,d1,d2);
 //    		String hm = map.toString();
 //    		fioA.write2test("testAcc", hm);
     		
     		//Test Bluetooth
 //    	    d1 = BasicLogReading.getDateFromString("07-11-2011 17:03:18");
 //		    d2 = BasicLogReading.getDateFromString("07-11-2011 17:05:03");
 //		    FileLoggingIO<BluetoothReading> fioB = new FileLoggingIO<BluetoothReading>();
 //		    HashMap<Date,List<BluetoothReading>> mapB = fioB.readFromTXTLogFile(NearbyBluetoothLogger.SENSOR_NAME, new BluetoothReading(), null,d1,d2);
 //    		hm = mapB.toString();
 //    		fioB.write2test("testBlue", hm);
     	   
     		// Test Light 
 //    		d1 = BasicLogReading.getDateFromString("07-11-2011 17:02:58");
 //		    d2 = BasicLogReading.getDateFromString("07-11-2011 17:05:59");
 //		    FileLoggingIO<LightReading> fioL = new FileLoggingIO<LightReading>();
 //		    HashMap<Date,List<LightReading>> mapL = fioL.readFromTXTLogFile(LightSensorLogger.SENSOR_NAME, new LightReading(), null,d1,d2);
 //    	    hm = mapL.toString();
 //    	    fioL.write2test("testLight", hm);
     	   
     	    // Test Wifi
 //    	    d1 = BasicLogReading.getDateFromString("07-11-2011 17:02:32");
 //		    d2 = BasicLogReading.getDateFromString("07-11-2011 17:03:58");
 //		    FileLoggingIO<WifiReading> fioW = new FileLoggingIO<WifiReading>();
 //		    HashMap<Date,List<WifiReading>> mapW = fioW.readFromTXTLogFile(NearbyWifiLogger.SENSOR_NAME, new WifiReading(), null,d1,d2);
 //    		hm = mapW.toString();
 //    		fioW.write2test("testWifi", hm); 
     		
     		
 //    	     Test GeoLoc
 //    	    d1 = BasicLogReading.getDateFromString("07-30-2011 04:23:16");
 //		    d2 = BasicLogReading.getDateFromString("07-30-2011 10:24:16");
 //		    FileLoggingIO<LocationGPSReading> fioG = new FileLoggingIO<LocationGPSReading>();
 //		    HashMap<Date,List<LocationGPSReading>> mapG = fioG.readFromTXTLogFile(LocationGPSLogger.SENSOR_NAME, new LocationGPSReading(), null,d1,d2);
 //		    String hm = mapG.toString();
 //    		fioG.write2test("testGeo", hm);
     	//-------------------------
 
 		   /*Test getLocation*/
 //		    FileLoggingIO<LocationGPSReading> fioG = new FileLoggingIO<LocationGPSReading>();
 //		    HashMap<Date,List<LocationGPSReading>> mapG = fioG.readFromTXTLogFile(LocationGPSLogger.SENSOR_NAME, new LocationGPSReading(), null);
 //		    DeviceStatus deviceStatus = new DeviceStatus();
 //		    
 //		    String location = deviceStatus.getLocation(mapG);
 //		    outputView.setText("Current Location : "+location);
 		
 		
 		if (v.getId() == refresh.getId()) {
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.MINUTE, -5);
 			Date d1 = cal.getTime();
 		    Date d2 = Calendar.getInstance().getTime();
 
     	    FileLoggingIO<ContextReading> fio = new FileLoggingIO<ContextReading>();
             HashMap<Date,List<ContextReading>> map = fio.readFromTXTLogFile(ContextLogger.SENSOR_NAME, new ContextReading(), null,d1,d2);
     		String outputString = "";//map.toString();
     		
     		
     		Set<Date> es = map.keySet();
     		TreeSet<Date> ts = new TreeSet<Date>(es);
    		String lastReading="";
     		for (Date key : ts) {
     			outputString += "-------------" + key.toString() + "-------------\n";
 				for (ContextReading cr: map.get(key)) {
 					 String s = String.format
 					 ("%.2f probability of %s [%s]\n", cr.getProbability(), cr.getContextName(), cr.getOptionalTags());
 					outputString += s;
					lastReading = BasicLogReading.getStringFormattedDateTime(key)+"--"+cr.getProbability()+"--"+ cr.getContextName();
 				}
 				outputString += "--------------------------------------------------------------------\n\n";
 			}
 			
    		outputView.setText(lastReading);
     		
 			//DeviceStatus ds = new DeviceStatus();
 			//outputView.setText(ds.checkIfWIFIChanging(60)+"");
 			
 		} else {
 			isLogging = !isLogging;
 			if(isLogging) {
 				toggleLogging.setText(R.string.stop);
 				this.logger.initiateAllLogging();
 			} else {
 				toggleLogging.setText(R.string.start);
 				this.logger.terminateAllLogging();
 
 			}
 		}
   
 	}
 
 
 
 }
