 package com.vitaltech.bioink;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Looper;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Discovery {
 	private static final String TAG = Discovery.class.getSimpleName();
 
 	// Get debug setting from MainActivity
 	public static final Boolean DEBUG = MainActivity.DEBUG;
 
 	private BluetoothAdapter btAdapter;
 	private Context context;
 	private AtomicBoolean running;
 	private boolean listenerRunning;
 	private BroadcastReceiver receiver;
 	private IntentFilter filter;
 	private ArrayList<String> bhHeard;
 	private ArrayList<String> bhPaired;
 
 	public Discovery(Context _context, BluetoothAdapter _btAdapter) {
 		if (DEBUG) Log.v(TAG, "Bluetooth discovery instantiated");
 		context = _context;
 		btAdapter = _btAdapter;
 		running = new AtomicBoolean(false);
 		bhHeard = new ArrayList<String>();
 		bhPaired = new ArrayList<String>();
 		receiver = null;
 		filter = null;
 		listenerRunning = false;
 	}
 
 	/** atomic */
 	public void findDevices() {
 		if(running.getAndSet(true)){
 			Log.w(TAG, "findDevices() already running");
 			return;
 		}
 		if(Looper.myLooper() == Looper.getMainLooper()){
 			Log.e(TAG, "findDevices() must be run in background");
 			return;
 		}
 		if (btAdapter == null) {
 			Toast.makeText(context.getApplicationContext(),
 					"Bluetooth not available on this device", Toast.LENGTH_LONG)
 					.show();
 			Log.e(TAG, "findDevices() Bluetooth not available on this device");
 			return;
 		}
 		if (! btAdapter.isEnabled()) {
 			if (DEBUG) Log.w(TAG, "findDevices() bluetooth is off");
 			return;
 		}
 		if (DEBUG) Log.v(TAG, "findDevices() started");
 
 		if (DEBUG) Log.d(TAG, "findDevices() bluetooth adapter: " + btAdapter.getName() + ", state: "
 			+ btAdapter.getState() + ", scanmode: " + btAdapter.getScanMode());
 
 		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
 		if (DEBUG) Log.d(TAG, "findDevices() bluetooth set size: " + pairedDevices.size());
 		bhPaired = new ArrayList<String>();
 		if (pairedDevices.size() > 0) {
 			for (BluetoothDevice device : pairedDevices) {
 				if (DEBUG) Log.v(TAG, "findDevices(): " + device.getName() + ", "
 						+ device.getAddress() + ", " + device.getBondState());
 				if(device.getName().startsWith("BH")){
 					bhPaired.add(device.getName());
 				}
 			}
 			if(DEBUG) Log.d(TAG, "found " + bhPaired.size() + " bioharness pairs: " + bhPaired.toString());
 		}
 
 		if( ! listenerRunning){
 			if(DEBUG) Log.v(TAG, "add receiver, intent, discovery");
 			bhHeard = new ArrayList<String>();
 			receiver = new BroadcastReceiver() {
 				public void onReceive(final Context context, Intent intent) {
 					String action = intent.getAction();
 					if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 						// Get the BluetoothDevice object from the Intent
 						final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 						// Add the name and address to an array adapter to show in a ListView
 						//		            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
 						Log.v(TAG, "heard " + device.getName());
 						if(device.getName().startsWith("BH")){
 							bhHeard.add(device.getName());
 						}
 						((Activity)context).runOnUiThread(
 							new Runnable() {
 								public void run() {
 									if(DEBUG) Log.v(TAG,"discovery found new device: " + device.getName());
 									try{
 										((TextView)((Activity)context).findViewById(R.id.audibleTextView)).setText("Heard: " + bhHeard.toString());
 									}catch (Throwable e){
 										Log.e(TAG, "error updating heard devices");
 										Log.e(TAG, e.toString());
 									}
 								}
 							}
 						);
 					}
 				}
 			};
 			filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 			context.registerReceiver(receiver, filter);
 			listenerRunning = true;
 			btAdapter.startDiscovery();
			showProgress(true);
 		}else{
 			if(DEBUG) Log.d(TAG, "listener already running");
 		}
 
 		if(running.getAndSet(false)){
 			if(DEBUG) Log.v(TAG, "findDevices() finished clean");
 		}else{
 			Log.w(TAG, "findDevices() finished dirty");
 		}
 	}
 
 	public void showDevices(){
 		if(context == null){
 			Log.e(TAG, "context is null");
 			return;
 		}
 		Activity activity = (Activity) context;
 		if(activity == null){
 			Log.e(TAG, "cast to activity has failed");
 			return;
 		}
 		try{
 			if(btAdapter == null){
 				Log.e(TAG, "No Bluetooth");
 				((TextView)activity.findViewById(R.id.radioTextView)).setText("Radio does not exist");
 				((TextView)activity.findViewById(R.id.pairedTextView)).setVisibility(View.INVISIBLE);
 				((TextView)activity.findViewById(R.id.audibleTextView)).setVisibility(View.INVISIBLE);
 			}else if(btAdapter.isEnabled()){
 				if(DEBUG) Log.v(TAG, "Bluetooth enabled, showing devices");
 				((TextView)activity.findViewById(R.id.radioTextView)).setText("Radio is on");
 				((TextView)activity.findViewById(R.id.pairedTextView)).setVisibility(View.VISIBLE);
 				((TextView)activity.findViewById(R.id.pairedTextView)).setText("Paired: " + bhPaired.toString());
 				((TextView)activity.findViewById(R.id.audibleTextView)).setVisibility(View.VISIBLE);
 				((TextView)activity.findViewById(R.id.audibleTextView)).setText("Heard: " + bhHeard.toString());
 			}else{
 				if(DEBUG) Log.v(TAG, "Bluetooth disabled, radio is off");
 				((TextView)activity.findViewById(R.id.radioTextView)).setText("Radio is off");
 				((TextView)activity.findViewById(R.id.pairedTextView)).setVisibility(View.INVISIBLE);
 				((TextView)activity.findViewById(R.id.audibleTextView)).setVisibility(View.INVISIBLE);
 			}
 		} catch (NullPointerException nullExc){
 			Log.w(TAG, "null pointer exception: layout not set up?");
 		} catch (Exception e){
 			Log.w(TAG,"unknown exception occured: " + e);
 		} finally{
 			if(DEBUG) Log.v(TAG, "finished showing devices");
 		}
 	}
 
 	public void stopListener(){
 		if(! listenerRunning){
 			Log.w(TAG, "listener not running; not stopping");
 			return;
 		}
 		if(receiver == null || filter == null){
 			Log.e(TAG, "error in listener setup; not stopping");
 			return;
 		}
 		Log.d(TAG, "cancel discovery success: " + btAdapter.cancelDiscovery());
		showProgress(false);
 		try{
 			context.unregisterReceiver(receiver);
 		} catch (IllegalArgumentException arg){
 			Log.w(TAG, arg.toString());
 		}
 		listenerRunning = false;
 	}
 
 	public void showProgress(final boolean b){
 		((Activity)context).runOnUiThread(
 				new Runnable() {
 					public void run() {
 						try{
 							ProgressBar bar = ((ProgressBar)((Activity)context).findViewById(R.id.progress));
 							if(b){
 								bar.setVisibility(ProgressBar.VISIBLE);
 							}else{
 								bar.setVisibility(ProgressBar.INVISIBLE);
 							}
 						}catch (Throwable e){
 							Log.e(TAG, "error toggling progress indicator");
 							Log.e(TAG, e.toString());
 						}
 					}
 				}
 		);
 	}
 
 	// Turn a Set of 8-bit Bytes into a List of 10-bit Longs
 	public static List<Long> zephyParse(Set<Byte> samples) {
 		List<Long> decoded = new ArrayList<Long>();
 		Byte[] array = (Byte[])samples.toArray(new Byte[samples.size()]);
 
 		int shifter = 1;
 		for(int i=1; i<samples.size(); ++i){
 			if(shifter != 5){
 				long rightMask = ((long)255 << ((shifter-1) * 2)) & 255;
 				long right = (((long)array[i-1]) & rightMask) >> ((shifter-1)*2);
 				long leftMask = ((long)1 << (shifter * 2)) - 1;
 				long left = ((((long)array[i]) & leftMask) << (8-((shifter-1)*2)));
 				
 				decoded.add(
 					(left | right) & 1023
 				);
 			}
 
 			shifter++;
 			if(shifter > 5){
 				shifter = 1;
 			}
 		}
 		return decoded;
 	}
 }
 
