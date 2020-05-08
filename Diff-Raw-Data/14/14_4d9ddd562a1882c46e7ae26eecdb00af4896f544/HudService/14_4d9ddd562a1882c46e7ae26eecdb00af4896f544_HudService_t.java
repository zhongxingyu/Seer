 package com.openxc.hardware.hud;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 
 import android.app.Service;
 
 import android.bluetooth.BluetoothSocket;
 
 import android.content.Intent;
 
 import android.os.Binder;
 import android.os.IBinder;
 
 import android.util.Log;
 
 /**
  * The HudService manages the connection to the Bluetooth HUD.
  *
  * This is meant to be used as an in-process Android service, and all commands
  * to the HUD go through this class. Users of the service should call connect()
  * with the HUD's MAC address after the service connects. Once connected, any of
  * the other methods can be used to control the LEDs.
  */
 public class HudService extends Service implements BluetoothHudInterface {
     private final String TAG = "HudService";
     private final long RETRY_DELAY = 1000;
     private final long POLL_DELAY = 3000;
 
     private DeviceManager mDeviceManager;
     private PrintWriter mOutStream;
     private BufferedReader mInStream;
     private BluetoothSocket mSocket;
 
     private final IBinder mBinder = new LocalBinder();
     public class LocalBinder extends Binder {
         public HudService getService() {
             return HudService.this;
         }
     }
 
     private ConnectionKeepalive mConnectionKeepalive;
     private class ConnectionKeepalive implements Runnable {
         private boolean mRunning;
 
         public ConnectionKeepalive() {
             mRunning = true;
         }
 
         public void stop() {
             mRunning = false;
         }
 
         public void run() {
             while(mRunning) {
                 try {
                     connectSocket();
                 } catch(BluetoothException e) {
                     Log.w(TAG, "Unable to connect to socket", e);
                     try {
                         Thread.sleep(RETRY_DELAY);
                     } catch(InterruptedException e2) {
                         return;
                     }
                     continue;
                 }
 
                while(ping()){
                     try {
                         Thread.sleep(POLL_DELAY);
                     } catch(InterruptedException e) {
                         return;
                     }
                 }
 
                 Log.i(TAG, "Socket " + mSocket + " has been disconnected!");
             }
         }
     }
 
 
     @Override
     public void onCreate() {
         try {
             mDeviceManager = new DeviceManager(this);
         } catch(BluetoothException e) {
             Log.w(TAG, "Unable to open Bluetooth device manager", e);
         }
         mSocket = null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.i(TAG, "Received start id " + startId + ": " + intent);
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "Being destroyed");
         try {
             disconnect();
         } catch(BluetoothException e) {
             Log.d(TAG, "Unable to disconnect when being destroyed", e);
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         Log.d(TAG, "Binding from " + intent);
         return mBinder;
     }
 
     @Override
     public void disconnect() throws BluetoothException {
         if(!isConnected()) {
             Log.w(TAG, "Unable to disconnect -- not connected");
             throw new BluetoothException();
         }
 
         Log.d(TAG, "Disconnecting from the socket " + mSocket);
         mOutStream.close();
         try {
             mInStream.close();
         } catch(IOException e) {
             Log.w(TAG, "Unable to close the input stream", e);
         }
 
         try {
             mSocket.close();
         } catch(IOException e) {
             Log.w(TAG, "Unable to close the socket", e);
         }
         mSocket = null;
         Log.d(TAG, "Disconnected from the socket");
     }
 
     @Override
     public void set(int chan, double value) throws BluetoothException {
         if(!isConnected()) {
             Log.w(TAG, "Unable to set -- not connected");
             throw new BluetoothException();
         }
 
         mOutStream.write("S" + chan + Math.round(value * 255) + "M");
         mOutStream.flush();
     }
 
     @Override
     public void setAll(double value) throws BluetoothException {
         if(!isConnected()) {
             Log.w(TAG, "Unable to setAll -- not connected");
             throw new BluetoothException();
         }
 
         for(int i = 0; i < 5; i++) {
             set(i, value);
         }
     }
 
     @Override
     public void fade(int chan, long duration, double value)
             throws BluetoothException {
         if(!isConnected()) {
             Log.w(TAG, "Unable to fade -- not connected");
             throw new BluetoothException();
         }
 
         mOutStream.write("F" + chan + duration + "," +
                 Math.round(value * 255) + "M");
         mOutStream.flush();
     }
 
     @Override
     public int rawBatteryLevel() throws BluetoothException {
         if(!isConnected()) {
             Log.w(TAG, "Unable to check battery level -- not connected");
             throw new BluetoothException();
         }
 
         mOutStream.write("BM");
         inFlush(mInStream);
         mOutStream.flush();
         String response = getResponse(mInStream);
         if(response != null && response.indexOf("VAL:") >= 0) {
             return Integer.parseInt(response.substring(4));
         } else {
             throw new BluetoothException("Invalid battery level: " + response);
         }
     }
 
     @Override
     public void connect(String targetAddress) throws BluetoothException {
         if(mConnectionKeepalive != null) {
             mConnectionKeepalive.stop();
         }
         mDeviceManager.connect(targetAddress);
 
         mConnectionKeepalive = new ConnectionKeepalive();
         new Thread(mConnectionKeepalive).start();
     }
 
     @Override
     public boolean ping() {
         if(!isConnected()) {
             Log.w(TAG, "Unable to ping -- not connected");
             return false;
         }
 
        mOutStream.write("PM");
         inFlush(mInStream);
         mOutStream.flush();
         String response = getResponse(mInStream);
        if(response == null || !response.equals("ACK")) {
            Log.d(TAG, "Received unexpected ping response: " + response);
            try {
                disconnect();
            } catch(BluetoothException e) {
                Log.w(TAG, "Unable to disconnect after failed ping", e);
            }
             return false;
         }
         Log.d(TAG, "Ping? Pong!");
         return true;
     }
 
     /**
      * Clear any waiting characters from the input buffer.
      *
      * Disregard exceptions, this is only an attempt to clear the buffer
      */
     private void inFlush(BufferedReader reader){
         try {
             while(reader.ready()) {
                 //A guess at something larger than the buffer size of the BT
                 //adapter
                 reader.skip(1024);
             }
         } catch(IOException e) {}
     }
 
     private String getResponse(BufferedReader reader){
         String line = "";
         try {
             line = mInStream.readLine();
         } catch(IOException e) {
             Log.e(TAG, "unable to read response");
             return null;
         }
         if(line == null){
             Log.e(TAG, "device has dropped offline");
             return null;
         }
         return line;
     }
 
     private void connectSocket() throws BluetoothException {
         try {
             mSocket = mDeviceManager.setupSocket();
             mOutStream = new PrintWriter(new OutputStreamWriter(
                         mSocket.getOutputStream()));
             mInStream = new BufferedReader(new InputStreamReader(
                         mSocket.getInputStream()));
             Log.i(TAG, "Socket stream to HUD opened successfully");
         } catch(IOException e) {
             // We are expecting to see "host is down" when repeatedly
             // autoconnecting
             if(!(e.toString().contains("Host is Down"))){
                 Log.d(TAG, "Error opening streams "+e);
             } else {
                 Log.e(TAG, "Error opening streams "+e);
             }
             mSocket = null;
             throw new BluetoothException();
         }
     }
 
     private boolean isConnected() {
         return mSocket != null;
     }
 }
