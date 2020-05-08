 package edu.sjsu.cme.receivers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.BatteryManager;
 import android.os.Environment;
 import android.util.Log;
import android.widget.Toast;
 import edu.sjsu.cme.GraphActivity;
 import edu.sjsu.cme.Logger;
 import edu.sjsu.cme.R;
 
 /**
  * Making a separate receiver for the graph since we want it to be coupled with
  * it to push data. This BroadcastReceiver is set to receive
  * Intent.ACTION_BATTERY_CHANGED
  * 
  * @see Intent.ACTION_BATTERY_CHANGED
  * @author michael
  * 
  */
 public class BatteryChangeReceiverForGraph extends BroadcastReceiver {
 
 	private static final String TAG = "REIMC::BatteryChangeReceiverForGraph";
 
 	private GraphActivity graphActivity;
 
 	/**
 	 * Constructor that passes a reference of GraphActivity
 	 */
 	public BatteryChangeReceiverForGraph(GraphActivity graphActivity) {
 		this.graphActivity = graphActivity;
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
		long currTime = System.currentTimeMillis();
		int batteryPct = (int) ((intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (float) intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)) * 100);
 		Logger.log(TAG, "onReceive called with battery level "
				+ batteryPct);
		graphActivity.pushData(String.valueOf(currTime),
 				String.valueOf(batteryPct));
 
 		boolean mExternalStorageAvailable = false;
 		boolean mExternalStorageWriteable = false;
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageAvailable = mExternalStorageWriteable = true;
			writeToExternalFile(String.valueOf(currTime),
 			String.valueOf(batteryPct));
			Toast.makeText(context, "REIMC::Battery Level Logged", Toast.LENGTH_SHORT).show();
 			
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need
 			// to know is we can neither read nor write
 			mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 	}
 	
 	private void writeToExternalFile(String time,String batteryPct){
 	    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "datafile.json");
 	    PrintWriter pw = null;
 	    try {
 	        // Very simple code to copy a picture from the application's
 	        // resource into the external file.  Note that this code does
 	        // no error checking, and assumes the picture is small (does not
 	        // try to copy it in chunks).  Note that if external storage is
 	        // not currently mounted this will silently fail.
 //	        InputStream is = graphActivity.getResources().openRawResource(R.drawable.balloons);
	    	pw = new PrintWriter(new FileOutputStream(file,true),true);
	    	pw.println("["+time+","+batteryPct+"],");
	    
	    	
 	    	
 //	        OutputStream os = new FileOutputStream(file);
 //	        byte[] data = new byte[is.available()];
 //	        is.read(data);
 //	        os.write(data);
 //	       
 //	        is.close();
 //	        os.close();
 	    } catch (IOException e) {
 	        // Unable to create file, likely because external storage is
 	        // not currently mounted.
 	        Log.w("ExternalStorage", "Error writing " + file, e);
 	    }finally{
 	    	pw.close();
 	    }
 	    
 	    Logger.log("REIMC:::::::::", "write called");
 	    
 	}
 
 }
