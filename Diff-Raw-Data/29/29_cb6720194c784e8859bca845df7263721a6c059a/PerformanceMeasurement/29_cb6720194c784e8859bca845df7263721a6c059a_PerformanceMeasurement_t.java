 package com.networkbenchmark;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import android.content.Context;
 import android.telephony.TelephonyManager;
 
 import com.parse.ParseObject;
 
 
 public class PerformanceMeasurement {
 	public  String imei;
 	public Boolean perfCalculated = false;
 	public final String PerfName = "Download Speed Test";
 	public float perfResult;
 	public long perfTime;
 	public long perfBytes;
 	
 	public String temporary;
 	public String temporary2;
 
 	public PerformanceMeasurement(){
 	}
 	
 	// constructor
 	public PerformanceMeasurement(Context context){
 		// get IMEI of this device
 		this.imei = "temporary IMEI";
 		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
 		this.imei = telephonyManager.getDeviceId();
 	}
 
 	// code to calculate a performance measurement
 	public void Calculate(){
 		this.perfCalculated = true;
 		this.perfResult = 35;
 		
 		// start a thread to calculate download speed in background
 		Thread t = new Thread(new downloader(this));
 		t.start();
		try {
			t.join();
		} catch (Exception e){}
 	}
 	
 	// code to calculate download speed goes here
 	private static class downloader
 		extends PerformanceMeasurement
 		implements Runnable   {
 		
 		private PerformanceMeasurement PerfMeasure;
 		public downloader(PerformanceMeasurement perf){
 			this.PerfMeasure = perf;
 		}
 		
 		public void run(){
 			try {
 		
 				PerfMeasure.perfBytes = 0;
 				PerfMeasure.perfResult = (float)0.0;
 				PerfMeasure.perfTime = 0;
 				
 			    String line = "";
 			   
 			    // get time
 			    long l1 = System.currentTimeMillis();
 			    
 			    URL url = new URL("http://www-users.cs.umn.edu/~jtucker/");
 			    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
 		        InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());	        
 		        BufferedReader reader = new BufferedReader(isr);
 		        while((line = reader.readLine()) != null){
 		        	PerfMeasure.perfBytes += line.getBytes().length;
 		        }
 
 		        // get time
 			    long l2 = System.currentTimeMillis();
 		        
 			    PerfMeasure.perfTime = l2-l1;
 			    if (PerfMeasure.perfTime != 0){
 				    PerfMeasure.perfResult = (float) (PerfMeasure.perfBytes/(((float)PerfMeasure.perfTime)/1000.0));
 			    }
 
 		        urlConnection.disconnect();		        
 
 	        } catch (Exception e){
 			    PerfMeasure.temporary2 = e.toString();
 			    PerfMeasure.temporary = "FAILED TO CALCULATE SPEED!!";
 	        }
 		}
 	}
 	
 	// code to save a performance measurement
 	public void Save(double[] gps){
 		
 		ParseObject perfMeasure = new ParseObject("PerfMeasure");
 		perfMeasure.put("imei", this.imei);
 		perfMeasure.put("PerfName", this.PerfName);
 		perfMeasure.put("perfResult", this.perfResult);
 		perfMeasure.put("perfSaveTime", System.currentTimeMillis());
 		perfMeasure.put("perfMeasureTime", this.perfTime);
 		perfMeasure.put("receivedBytes", this.perfBytes);
 		perfMeasure.put("latitude", gps[0]);
 		perfMeasure.put("longitude", gps[1]);
 		perfMeasure.saveInBackground();
 		
 	}
 	
 	@Override
 	public String toString(){
 		return  "Device ID: " + this.imei +
 				"\nPerformance result is (Bytes/Sec):" + this.perfResult
 				+ "\nPerformance time is (msecs):" + this.perfTime
 				+ "\nnum bytes received is:" + this.perfBytes;
 		
 		//this.PerfName + "\n" + this.imei + "\n"
 			//	+ "\n" + this.temporary + "\n" + this.temporary2;
 		//return this.temporary2;
 	}
 	
 }
