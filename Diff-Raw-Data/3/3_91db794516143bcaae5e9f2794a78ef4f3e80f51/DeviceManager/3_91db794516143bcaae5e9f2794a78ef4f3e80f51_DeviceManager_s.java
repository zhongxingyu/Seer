 package com.openxc.hardware.hud;
 
 import java.io.IOException;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import java.util.Set;
 import java.util.UUID;
 
 import com.openxc.hardware.hud.BluetoothException;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.content.Intent;
 
 import android.util.Log;
 
 public class DeviceManager {
     private final static String TAG = "DeviceManager";
    private final static UUID RFCOMM_UUID = new UUID(0x00, 0x03);
 
     private Context mContext;
     private BluetoothAdapter mBluetoothAdapter;
     private BluetoothSocket mSocket;
     private BluetoothDevice mTargetDevice;
     private final Lock mDeviceLock = new ReentrantLock();
     private final Condition mDeviceChangedCondition =
             mDeviceLock.newCondition();
     private BroadcastReceiver mReceiver;
 
     public DeviceManager(Context context) throws BluetoothException {
         mContext = context;
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if(mBluetoothAdapter == null) {
             throw new BluetoothException();
         }
     }
 
     public boolean connected() {
         return mTargetDevice != null;
     }
 
     public void discoverDevices(final String targetAddress) {
         Log.d(TAG, "Starting device discovery");
         Set<BluetoothDevice> pairedDevices =
             mBluetoothAdapter.getBondedDevices();
         for(BluetoothDevice device : pairedDevices) {
             Log.d(TAG, "Found already paired device: " + device);
             if(deviceDiscovered(device, targetAddress)) {
                 captureDevice(device);
             }
         }
 
         mReceiver = new BroadcastReceiver() {
             public void onReceive(Context context, Intent intent) {
                 if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                     BluetoothDevice device = intent.getParcelableExtra(
                             BluetoothDevice.EXTRA_DEVICE);
                     if (device.getBondState() != BluetoothDevice.BOND_BONDED
                             && deviceDiscovered(device, targetAddress)) {
                         captureDevice(device);
                     }
                 }
             }
         };
 
         IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
         mContext.registerReceiver(mReceiver, filter);
 
         if(mBluetoothAdapter.isDiscovering()) {
             mBluetoothAdapter.cancelDiscovery();
         }
         mBluetoothAdapter.startDiscovery();
     }
 
     public BluetoothSocket setupSocket() throws BluetoothException {
         if(mTargetDevice == null) {
             Log.w(TAG, "Can't setup socket -- device is " + mTargetDevice);
             throw new BluetoothException();
         }
 
         Log.d(TAG, "Scanning services on " + mTargetDevice);
         try {
             mSocket = mTargetDevice.createRfcommSocketToServiceRecord(
                     RFCOMM_UUID);
         } catch(IOException e) {}
 
         try {
             mSocket.connect();
             return mSocket;
         } catch(IOException e) {
             Log.e(TAG, "Could not find required service on " + mTargetDevice);
             try {
                 mSocket.close();
             } catch(IOException e2) {}
             throw new BluetoothException();
         }
     }
 
     public void connect(String targetAddress) {
         discoverDevices(targetAddress);
         mDeviceLock.lock();
         while(mTargetDevice == null) {
             try {
                 mDeviceChangedCondition.await();
             } catch(InterruptedException e) {}
         }
         mDeviceLock.unlock();
     }
 
     private void captureDevice(BluetoothDevice device) {
         mDeviceLock.lock();
         mTargetDevice = device;
         mDeviceChangedCondition.signal();
         mDeviceLock.unlock();
 
         if(mReceiver != null) {
             mContext.unregisterReceiver(mReceiver);
             mBluetoothAdapter.cancelDiscovery();
         }
     }
 
     private boolean deviceDiscovered(BluetoothDevice device,
             String targetAddress) {
         Log.d(TAG, "Found Bluetooth device: " + device);
         if(device.getAddress().equals(targetAddress)) {
             Log.d(TAG, "Found matching device: " + device);
             return true;
         }
         return false;
     }
 }
