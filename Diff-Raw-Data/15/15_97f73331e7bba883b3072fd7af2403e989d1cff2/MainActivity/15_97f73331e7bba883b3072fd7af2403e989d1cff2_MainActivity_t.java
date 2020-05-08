 	package org.techbridgeworld.bwt;
 	
 	import java.io.IOException;
 import java.util.Hashtable;
 	import java.util.concurrent.ExecutorService;
 	import java.util.concurrent.Executors;
 	
 	import android.app.Activity;
 	import android.content.Context;
 	import android.hardware.usb.UsbManager;
 	import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 	import android.widget.ScrollView;
 	import android.widget.TextView;
 	
 	import com.hoho.android.usbserial.driver.UsbSerialDriver;
 	import com.hoho.android.usbserial.driver.UsbSerialProber;
 	import com.hoho.android.usbserial.util.HexDump;
 import com.hoho.android.usbserial.util.SerialInputOutputManager;
 	
 	public class MainActivity extends Activity {
 		// Constants
 		static int BAUDRATE = 57600;
 		static int TIMEOUT = 1000;
 		static int DEBOUNCE = 800; //Milliseconds to disable a button for
 		
 		// Buffer / Debounce stuff
 		private byte[] dataBuffer = new byte[6]; 
 		private int bufferIdx = 0;
 		private Hashtable<String, Boolean> debounceHash = new Hashtable<String, Boolean>();
 		
 		private ScrollView scrollView; 
 		private TextView textView; 
 		
 		private UsbManager usbManager; 
 		private UsbSerialDriver usbDriver;
 		private SerialInputOutputManager serialManager; 
 		
 		private final ExecutorService executor = Executors.newSingleThreadExecutor();
 		
 		private final SerialInputOutputManager.Listener listener =
 				new SerialInputOutputManager.Listener() {
 	
 			
 			@Override
 			public void onRunError(Exception e) {
 				//Ignore
 			}
 	
 			@Override
 			public void onNewData(final byte[] data) {
 				MainActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 			            //textView.append("NOTE onNewData, about to run()\n");
 						MainActivity.this.updateReceivedData(data);
 					}
 				});
 			}
 		};
 	
 	    @Override
 	    protected void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.main);
 	        scrollView = (ScrollView) findViewById(R.id.scrollview);
 	        textView = (TextView) findViewById(R.id.textview);
 	        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
 	        textView.append("NOTE onCreate, created stuff\n");
 	    }
 	    
 	    @Override
 	    protected void onPause() {
 	        super.onPause();
 	        textView.append("NOTE onPause, about to stopIoManager()\n");
 	        stopIoManager();
 	        if (usbDriver != null) {
 	            try {
 	                textView.append("NOTE onPause, about to close usbDriver()\n");
 					usbDriver.setBaudRate(BAUDRATE);
 					byte[] bt = "bt".getBytes();
 					usbDriver.write(bt, TIMEOUT);
 	                usbDriver.close();
 	            } catch (IOException e) {
 	                // Ignore.
 	            }
 	            usbDriver = null;
 	        }
 	    }
 	    
 	    @Override
 	    protected void onResume() {
 	        super.onResume();
 	        usbDriver = UsbSerialProber.acquire(usbManager);
 	        if (usbDriver != null) {
 	            try {
 	                textView.append("NOTE onResume, about to open usbDriver()\n");
 	            	usbDriver.open();
 					usbDriver.setBaudRate(BAUDRATE);
 					byte[] bt = "bt".getBytes();
 					usbDriver.write(bt, TIMEOUT);
 	            } catch (IOException e) {
 	                try {
 	                    textView.append("ERROR onResume, about to close usbDriver()\n");
 	                	usbDriver.close();
 	                } catch (IOException e2) {
 	                    // Ignore.
 	                }
 	                usbDriver = null;
 	                return;
 	            }
 	        }
 	        textView.append("NOTE onResume, about to deviceStateChange()\n");
 	        onDeviceStateChange();
 	    }
 	    
 	    private void stopIoManager() {
 	        if (serialManager != null) {
 	            serialManager.stop();
 	            serialManager = null;
 	        }
 	    }
 	
 	    private void startIoManager() {
 	        if (usbDriver != null) {
 	            serialManager = new SerialInputOutputManager(usbDriver, listener);
 	            executor.submit(serialManager);
 	        }
 	    }
 	
 	    private void onDeviceStateChange() {
 	        stopIoManager();
 	        startIoManager();
 	    }
 	    
 	    // Passes a byte array to the debounce hashtable.
 	    private void debounceKey(String key){
 	    	final String newKey = key;
 	    	debounceHash.put(newKey, true);
 	    	
 	    	//Start a runnable to un-block the key after a set time.
 	    	Handler handler=new Handler();
 	    	Runnable r=new Runnable() {
 	    	    public void run() {
 	    	    	debounceHash.put(newKey, false);
 	    	    }
 	    	};
 	    	handler.postDelayed(r, DEBOUNCE);
 	    }
 	    
 	    // Returns true if a key is currently being ignored.
 	    private boolean isDebounced(String key){
 	    	boolean query = (debounceHash.get(key) == null? false : debounceHash.get(key));
 	    	return query;
 	    }
 	    
 	    
 	    // Takes the received data, checks to see if it should be ignored/debounced,
 	    // and print the results to device screen (triggers events later).
 	    private void updateReceivedData(byte[] data) {	
 	    	
 	    	//For every byte in the incoming data...
 	    	for (int i = 0; i < data.length; i++){
 	    		
 	    		Log.i("Salem", "currently parsing " + (char)data[i] + " (" + (int)data[i] + ")");
 	    		
 //	    		if(data[i] == 32){
 //	    		}
	    		
 	    		// If we are done, and if the buffer represents a non-blocked key, then
 	    		// log the buffer, clear it, and set its index to 0.
	    		

	    		if(data[i] == 110 || data[i] == 116){
	    			
	    			// This is to catch the initial "bt" received from the device.
		    		if(data[i] == 116){
	    				dataBuffer[bufferIdx] = data[i];	
	    				bufferIdx++;
		    		}
	    			
 	    			String message = "";
 	    			for(int j = 0; j < bufferIdx; j++){
 	    				message += (char)dataBuffer[j];
 	    			}
 	    			
 	    			if(!isDebounced(message)){
 			    		textView.append("\n\n----------------------");
 			    		textView.append("\nBuffer:  " + message);
 			    		
 		    			Log.i("Salem", "Buffer: " + message);
 		    			debounceKey(message);
 	    			}
 	    			else{
 	    				Log.i("Salem", "Button press blocked!");
 	    			}
 	    			
 	    			bufferIdx = 0;
 	    			dataBuffer = new byte[6];
 
 	    		}
 	    		else{
 	    			if(bufferIdx >= 6){
 	    				Log.e("Salem", "bufferIdx out of range: " + bufferIdx);
 	    			}
 	    			else{
 	    				dataBuffer[bufferIdx] = data[i];	
 	    				bufferIdx++;
 	    			}
 	    		}
 	    		
 	    		scrollView.smoothScrollTo(0, textView.getBottom());
 	    	}
 	    				    	
 
 	    }
 	}
