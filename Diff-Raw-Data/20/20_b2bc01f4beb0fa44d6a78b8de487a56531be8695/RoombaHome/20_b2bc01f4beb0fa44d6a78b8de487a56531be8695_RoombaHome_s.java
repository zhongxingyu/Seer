 package io.moe.roomba;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.usb.UsbDevice;
 import android.hardware.usb.UsbManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * Created by Guy on 12/09/2013.
  */
 public class RoombaHome extends Activity {
 
     private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
     private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
     private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
     private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
     private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
     private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
     private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;
 
     private final static String TAG = "ROOMBA";
     private final static boolean DEBUG = true;
 
     private Boolean mIsReceiving;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_roomba);
 
         IntentFilter filter = new IntentFilter();
         filter.addAction(ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
         filter.addAction(ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
         registerReceiver(mReceiver, filter);
 
         Button clean = (Button) findViewById(R.id.home_button_clean);
         clean.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 sendBroadcastString("CLEAN", RoombaHome.this);
             }
         });
         Button dock = (Button) findViewById(R.id.home_button_dock);
         dock.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 sendBroadcastString("DOCK", RoombaHome.this);
             }
         });
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(mReceiver);
     }
 
     private void findDevice() {
         UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
         UsbDevice usbDevice = null;
         HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
         if (DEBUG) Log.d(TAG, "length: " + usbDeviceList.size());
         Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
         while (deviceIterator.hasNext()) {
             UsbDevice tempUsbDevice = deviceIterator.next();
 
             // Print device information. If you think your device should be able
             // to communicate with this app, add it to accepted products below.
             if (DEBUG) Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
             if (DEBUG) Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
             if (DEBUG) Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
             if (DEBUG) Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
             if (DEBUG) Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
             if (DEBUG) Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
             if (DEBUG) Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
             if (DEBUG) Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());
 
             if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) {
                 if (DEBUG) Log.i(TAG, "Arduino device found!");
 
                 switch (tempUsbDevice.getProductId()) {
                     case ARDUINO_UNO_USB_PRODUCT_ID:
                         Toast.makeText(getBaseContext(), "Arduino Uno " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                         usbDevice = tempUsbDevice;
                         break;
                     case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
                         Toast.makeText(getBaseContext(), "Arduino Mega 2560 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                         usbDevice = tempUsbDevice;
                         break;
                     case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
                         Toast.makeText(getBaseContext(), "Arduino Mega 2560 R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                         usbDevice = tempUsbDevice;
                         break;
                     case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                         Toast.makeText(getBaseContext(), "Arduino Uno R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                         usbDevice = tempUsbDevice;
                         break;
                     case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
                         Toast.makeText(getBaseContext(), "Arduino Mega 2560 ADK R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                         usbDevice = tempUsbDevice;
                         break;
                     case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
                         Toast.makeText(getBaseContext(), "Arduino Mega 2560 ADK " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                         usbDevice = tempUsbDevice;
                         break;
                 }
             }
         }
 
         if (usbDevice == null) {
             if (DEBUG) Log.i(TAG, "No device found!");
             Toast.makeText(getBaseContext(), getString(R.string.no_device_found), Toast.LENGTH_LONG).show();
         } else {
             if (DEBUG) Log.i(TAG, "Device found!");
             Intent startIntent = new Intent(getApplicationContext(), ArduinoCommunicatorService.class);
             PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, startIntent, 0);
             usbManager.requestPermission(usbDevice, pendingIntent);
         }
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
             findDevice();
         }
     }
 
 
     BroadcastReceiver mReceiver = new BroadcastReceiver() {
 
         private void handleTransferedData(Intent intent, boolean receiving) {
             final byte[] newTransferedData = intent.getByteArrayExtra(ArduinoCommunicatorService.DATA_EXTRA);
            try {
                Toast.makeText(RoombaHome.this, new String(newTransferedData, "US-ASCII"), Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                //
             }
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             final String action = intent.getAction();
             if (DEBUG) Log.d(TAG, "onReceive() " + action);
 
             if (ArduinoCommunicatorService.DATA_RECEIVED_INTENT.equals(action)) {
                 handleTransferedData(intent, true);
             } else if (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT.equals(action)) {
                 handleTransferedData(intent, false);
             }
         }
     };
 
     public static void sendBroadcastString(String s, Context c) {
         Intent i = new Intent();
         i.setAction("primavera.arduino.intent.action.SEND_DATA");
         i.putExtra("primavera.arduino.intent.extra.DATA", messagePacketFromString(s));
         c.sendBroadcast(i);
     }
 
     public static byte[] messagePacketFromString(String s) {
         try {
             byte[] a = s.getBytes("US-ASCII");
             byte[] c = new byte[a.length + 1];
             System.arraycopy(a, 0, c, 0, a.length);
             System.arraycopy(new byte[]{0x03}, 0, c, a.length, 1);
             return c;
         } catch (Exception ex) {
             //whatever
         }
         return null;
     }
 }
