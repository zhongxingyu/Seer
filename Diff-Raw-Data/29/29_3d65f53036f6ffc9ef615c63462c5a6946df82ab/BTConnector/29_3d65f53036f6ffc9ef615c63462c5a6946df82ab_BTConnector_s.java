 package com.robot.raido;
 import android.bluetooth.*;
 import java.util.*;
 import java.io.*;
 import android.widget.*;
 import android.content.*;
 import android.util.Log;
 import android.os.Handler;
 import android.os.*;
 
 public class BTConnector {
     static String TAG = "BTConnector";
 	static String BTNAME = "Nexus";
 	
     BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
 	BluetoothDevice device;
 	BluetoothSocket socket;
 	Context context;
 	Handler handler;
 	
 	static UUID SPP_UUID = UUID.fromString(
 		"00001101-0000-1000-8000-00805F9B34FB");
 		
 	public BTConnector(Context context, Handler handler) {
 		this.context = context;
 		this.handler = handler;
 	}
 	
 	public boolean init(){
 		Set<BluetoothDevice> devices = adapter.getBondedDevices();
 		for (BluetoothDevice d : devices) {
 			Toast.makeText(context,d.getName(),Toast.LENGTH_SHORT).show();
 			if (d.getName().indexOf(BTNAME) != -1) {
 				device = d;
 			}
 		}
 		if (device == null) 
 			return false;
 		return true;
 	}
 	
 	public boolean connect() {
 		adapter.cancelDiscovery();
 		try {
 			socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
             socket.connect();
 		} catch (IOException e) {
 			Toast.makeText(context,"Can't connect",Toast.LENGTH_LONG).show();
 			Log.e(TAG, "Error connecting to robot");
 			return false;
 		}
 		return true;
 	}
 	
 	public void send(byte b) {
 		try {
 			socket.getOutputStream().write(b);
 		} catch (IOException e) {
 			Toast.makeText(context, "Command was not sent",
 					Toast.LENGTH_SHORT);
 		} catch (NullPointerException e) {
 			Toast.makeText(context, "Not connected", 
 					Toast.LENGTH_LONG);
 		}
 		fake(Integer.toHexString(b));
 	}
 	
 	private void fake(String s) {
 		handler.sendMessage(Message.obtain(handler, 0, s));
 	}
 
     private class DeviceReader extends Thread {
        private static String TAG1 = "DeviceReader";
         private boolean reading = false;
         private BufferedInputStream in = null;
         
         private int available = 0;
 
         synchronized public void run() {
             try {
                 in = new BufferedInputStream(socket.getInputStream());
             } catch (IOException e) {
                Toast.makeToast(context, "Unable to read from device", 
                      Toast.LENGTH_LONG);
                 Log.e(TAG1, "Unable to get InputStream");
                 return;
             }
             if (in == null) {
                 Log.e(TAG1, "InputStream == null");
                 return;
             }
 
             reading = true;
             while(reading) {
                int available = in.available();
                     
                 try {
                     notify();
                     wait(300);
                 } catch (InterruptedException e) {
                     // ignore
                 }
             }
             Log.w(TAG1, "ran out");
         }
 
        synchronized public void stop() {
             reading = false;
         }
 
         synchronized public int available() {
             return available;
         }
 
         synchronized public byte[] read() {
            int a = in.available();
            byte[] buf = new byte[a];
            
            try {
                in.read(buf, 0, a);
             } catch (IOException e) {
                 Log.e(TAG1, "IOException while reading from stream");
                 return null;
             }
             notify();
             return buf;
         }
     }
 }
