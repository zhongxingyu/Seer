 package com.jperla.badge;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.Camera;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorEvent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Surface;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import com.qualcomm.QCARSamples.FrameMarkers.FrameMarkers;
 import com.jperla.badge.VCard;
 
 public class BadgeActivity extends Activity implements SensorEventListener {
 
 
 
     ViewSwitcher switcher;
     TextView textView;
     ListView lv;
     CameraSurfaceView cam_surface;
     SensorManager sm;
     Sensor acc_sensor;
     Sensor mag_sensor;
     float[] acceleration_vals = null;
     float[] magnetic_vals = null;
     
     BluetoothAdapter bt_adapter;
     VCard my_vcard;
     ArrayList<VCard> vcard_list;
     String display_name;
     Handler handler;
     AcceptThread outstanding_accept = null;
     ConnectThread outstanding_connect = null;
     ConnectedThread open_connection = null;
 
     TreeMap<Integer, String> macs = new TreeMap<Integer, String>();
 
     static final int BT_ENABLE_ACTIVITY = 1;
     
     static final String brandon_mac = "9C:02:98:70:23:67";
     static final String zhao_mac = "B0:D0:9C:38:8C:A2";
     static final String joe_laptop_mac = "10:93:E9:0C:62:45";
 
     FrameMarkers fm;
 
     int msg_length_total = 0;
     int msg_length_cur = 0;
     byte[] cur_buffer = new byte[4096];
 
     boolean active_pair = false;
     int active_partner = 0;
     Timer timer = new Timer();
     TimeoutTimerTask timer_task = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Hide the window title.
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         setContentView(R.layout.main);
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         switcher = (ViewSwitcher) findViewById(R.id.modeSwitcher);
         textView = (TextView) findViewById(R.id.textView);
 
         // Fetch the sensor manager.
         Context c = switcher.getContext();
         sm = (SensorManager) c.getSystemService(SENSOR_SERVICE);
 
         // Register a listener for the accelerometer.
         acc_sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         sm.registerListener(this, acc_sensor, SensorManager.SENSOR_DELAY_NORMAL);
 
         // Register a listener for the magnetic field sensor.
         mag_sensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
         sm.registerListener(this, mag_sensor, SensorManager.SENSOR_DELAY_NORMAL);
 
         // Fetch the Bluetooth adapter and make sure it is enabled.
         bt_adapter = BluetoothAdapter.getDefaultAdapter();
         if (bt_adapter == null) {
             Log.d(Constants.LOG_TAG, "ERROR: No Bluetooth adapter found");
         }
         else {
             Log.d(Constants.LOG_TAG, "Successfully found Bluetooth adapter");
         }
 
         // Fetch my VCard by looking up my mac address
         String my_mac = bt_adapter.getAddress();
         if (my_mac.equals(zhao_mac)) {
             Log.d(Constants.LOG_TAG, "Setting VCard to Zhao's");
             my_vcard = VCard.getZhao();
         } else if (my_mac.equals(joe_laptop_mac)) {
             Log.d(Constants.LOG_TAG, "Setting VCard to Joe's laptop");
             my_vcard = VCard.getJoeLaptop();
         } else {
             Log.d(Constants.LOG_TAG, "Settting VCard to Brandon's");
             my_vcard = VCard.getBrandon();
         }
         display_name = my_vcard.name + "\n" + my_vcard.institution + "\n\n";
         ((TextView) findViewById(R.id.display_name)).setText(display_name);
         vcard_list = new ArrayList<VCard>();
        // vcard_list.add(VCard.getJoeLaptop();
         String[] vcard_ary = new String[] {""};
 
         lv = (ListView) findViewById(R.id.list);
         lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, vcard_ary));
 
         lv.setClickable(true);
         lv.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                 Intent myIntent = new Intent(BadgeActivity.this, VCardActivity.class);
                 myIntent.putExtra("card", vcard_list.get((int)id).toString());
                 BadgeActivity.this.startActivity(myIntent);
             }
         });
 
         // Set up VCard list
         /*
         ListView lv = (ListView) findViewById(R.id.list);
         lv.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item, vcard_ary));
 
         */
 
 
         Log.d(Constants.LOG_TAG, Constants.BT_UUID.toString());
 
         // Initialize the table of MAC addresses.
         initializeMACs();
 
         handler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 int what = msg.what;
     
                 int result;
                 BluetoothSocket s;
 
                 switch (what) {
                     case Constants.BT_CONN_ACCEPTED:
                     case Constants.BT_CONN_CONNECTED:
                         result = msg.arg1;
                         if (what == Constants.BT_CONN_ACCEPTED) {
                             outstanding_accept = null;
                             Log.d(Constants.LOG_TAG, "Handler got BT_CONN_ACCEPTED: " + result);
                         }
                         else {
                             outstanding_connect = null;
                             Log.d(Constants.LOG_TAG, "Handler got BT_CONN_CONNECTED: " + result);
                         }
 
                         if (result == Constants.SUCCESS) {
                             s = (BluetoothSocket) msg.obj;
                             handleConnection(s);
                             active_pair = true;
 
                             // Register a timeout for this pairing.
                             timer_task = new TimeoutTimerTask();
                             timer.schedule(timer_task, (long) (1000*Constants.DISPLAY_TIMEOUT));
                         }
                         break;
 
                     case Constants.BT_MSG_RCVD:
                         Log.d(Constants.LOG_TAG, "Handler got BT_MSG_RCVD");
                         int bytes = msg.arg1;
                         byte[] buffer = (byte[]) msg.obj;
                         rcvMessage(bytes, buffer);
                         break;
 
                     case Constants.PHONE_ID_DETECTED:
                         int phone_id = msg.arg1;
                         Log.d(Constants.LOG_TAG, "Handler got phone id: " + phone_id);
 
                         // Make sure there is no active pair.
                         if (active_pair) {
                             Log.d(Constants.LOG_TAG, "Already have an active pairing with " + active_partner);
         
                             // If it is our partner, extend the timeout.
                             if (active_partner == phone_id) {
                                 Log.d(Constants.LOG_TAG, "Extending timeout.");
                                 timer_task.cancel();
                                 timer_task = new TimeoutTimerTask();
                                 timer.schedule(timer_task, 1000*10);
                             }
 
                             break;
                         }
 
                         // Look up the phone.
                         String mac = macs.get(phone_id);
                         if (mac == null) {
                             Log.d(Constants.LOG_TAG, "ERROR: Phone id does not map to MAC address");
                             break;
                         }
 
                         active_partner = phone_id;
                         tryConnection(mac);
                         break;
 
                     case Constants.CLEAR_SCREEN:
                         textView.setText("");
                         break;
 
                 }
 
             }
         };
 
         fm = new FrameMarkers(savedInstanceState, c, this, handler);
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         fm.onPause();
         sm.unregisterListener(this);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         fm.onResume();
 
         sm.registerListener(this, acc_sensor, SensorManager.SENSOR_DELAY_NORMAL);
         sm.registerListener(this, mag_sensor, SensorManager.SENSOR_DELAY_NORMAL);
 
         if (bt_adapter.isEnabled()) {
             Log.d(Constants.LOG_TAG, "Bluetooth already enabled");
             startListening();
         }
         else {
             Log.d(Constants.LOG_TAG, "Requesting to enable Bluetooth");
             Intent enable_bt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enable_bt, BT_ENABLE_ACTIVITY);
         }
 
     }
 
     public void onConfigurationChanged(Configuration config) {
         super.onConfigurationChanged(config);
         fm.onConfigurationChanged(config);
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
     public void onSensorChanged(SensorEvent event) {
         // Get the data from the sensor.
         switch (event.sensor.getType()) {
             case Sensor.TYPE_ACCELEROMETER:
                 acceleration_vals = event.values.clone();
                 break;
             case Sensor.TYPE_MAGNETIC_FIELD:
                 magnetic_vals = event.values.clone();
                 break;
         }
 
 
         // If we have all the necessary data, update the orientation.
         if (acceleration_vals != null && magnetic_vals != null) {
             float[] R = new float[16];
             float[] I = new float[16];
 
             SensorManager.getRotationMatrix(R, I, acceleration_vals, 
                 magnetic_vals);
 
             float[] orientation = new float[3];
             SensorManager.getOrientation(R, orientation);
 
             acceleration_vals = null;
             magnetic_vals = null;
 
             // Use the pitch to determine whether we are in ID mode or
             // conference mode.
             if (orientation[1] <= -0.2) {   // we're in conference mode
                 this.setRequestedOrientation(
                     ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                 showScheduleView();
             } else if (orientation[1] >= 0.2) {   // we're in ID mode
                 this.setRequestedOrientation(
                     ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                 showBadgeView();
             }
         }
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         switch (requestCode)
         {
             case BT_ENABLE_ACTIVITY:
                 if (resultCode == RESULT_OK) {
                     Log.d(Constants.LOG_TAG, "Bluetooth successfully enabled");
                     startListening();
                 }
                 else {
                     Log.d(Constants.LOG_TAG, "ERROR: Could not enable Bluetooth.");
                 }
                 break;
         }
     }
 
     void startListening()
     {
         // 1 connection at a time
         if (outstanding_accept != null || open_connection != null) {
             return;
         }
 
         outstanding_accept = new AcceptThread(bt_adapter, handler);
         outstanding_accept.start();
     }
 
     void tryConnection(String mac)
     {
 
         // Act as client, reaching out to make a new connection.
         if (outstanding_connect != null || open_connection != null) {
             return;
         }
 
         Log.d(Constants.LOG_TAG, "Attempting to connect to " + mac);
 
         BluetoothDevice dev = bt_adapter.getRemoteDevice(mac);
         outstanding_connect = new ConnectThread(dev, handler);
         outstanding_connect.start();
 
     }
 
     private class TimeoutTimerTask extends TimerTask {
 
         public TimeoutTimerTask() { }
 
         // End the current connection state.
         public void run() {
             active_pair = false;
             onDisconnect();
         }
     }
 
     public void handleConnection(BluetoothSocket s)
     {
         if (open_connection != null) {
             Log.d(Constants.LOG_TAG, "ERROR: active connection in handleConnection");
             try {
                 s.close();
             } catch (IOException e) {}
             return;
         }
 
         // Send out the v-card.
         open_connection = new ConnectedThread(s, handler);
         open_connection.start();
 
         sendVCard(open_connection);
         Log.d(Constants.LOG_TAG, "Sent card data");
     }
 
     public void sendVCard(ConnectedThread ct) 
     {
         String card_string = my_vcard.toJSON().toString();
         byte[] card_data;
 
         try {
             card_data = card_string.getBytes("ASCII");
         }
         catch (UnsupportedEncodingException e) {
             Log.d(Constants.LOG_TAG, "ERROR: Unsupported encoding");
             return;
         }
 
         Log.d(Constants.LOG_TAG, "Sending size " + card_data.length);
         Log.d(Constants.LOG_TAG, "Sending this v-card:");
         Log.d(Constants.LOG_TAG, card_string);
 
         // Send the message size
         ByteBuffer bb = ByteBuffer.allocate(4).putInt(card_data.length);
         Log.d(Constants.LOG_TAG, "SND BYTES: " + bb.get(0) + ", " + bb.get(1) + ", " + bb.get(2) + ", " + bb.get(3));
         byte[] size = bb.array();
         ct.write(size);
 
         // And then the message.
         ct.write(card_data);
     }
 
     public void rcvMessage(int bytes, byte[] buffer)
     {
         VCard their_card;
 
         ByteBuffer bb = ByteBuffer.wrap(buffer);
 
         // Fetch the bytes of this message.
         if (msg_length_total == 0) {
             Log.d(Constants.LOG_TAG, "RCV BYTES: " + bb.get(0) + ", " + bb.get(1) + ", " + bb.get(2) + ", " + bb.get(3));
             msg_length_total = bb.getInt();
             bb.get(cur_buffer, 0, bytes - 4);
             msg_length_cur += bytes - 4;
         }
         else {
             bb.get(cur_buffer, msg_length_cur, bytes);
             msg_length_cur += bytes;
         }
 
         Log.d(Constants.LOG_TAG, "Received " + bytes + " bytes (total " + msg_length_cur
                                  + " of " + msg_length_total + ")");
 
         String vcard_string;
 
         // Check if we have received the whole v-card.
         if (msg_length_cur == msg_length_total) {
             try {
                 vcard_string = new String(
                     Arrays.copyOfRange(cur_buffer, 0, msg_length_cur), "ASCII");
                 their_card = new VCard(vcard_string);
             }
             catch (UnsupportedEncodingException e) {
                 Log.d(Constants.LOG_TAG, "ERROR: Unsupported encoding");
                 open_connection.close();
                 open_connection = null;
                 startListening();
                 return;
             }
 
             Log.d(Constants.LOG_TAG, "Received this string:");
             Log.d(Constants.LOG_TAG, vcard_string);
 
             Log.d(Constants.LOG_TAG, "Received this v-card:");
             Log.d(Constants.LOG_TAG, their_card.toJSON().toString());
 
             msg_length_cur = 0;
             msg_length_total = 0;
 
             open_connection.close();
             open_connection = null;
             startListening();
 
             // Notify that we have received this v-card.
             onReceiveOtherVCard(their_card);
 
         }
     }
 
     public void onReceiveOtherVCard(VCard other) {
         String common = VCard.extractCommonalities(my_vcard, other);
         textView.setText(common);
         textView.setTextSize(30);
 
         // Save their VCard
         if (!vcard_list.contains(other)) {
             vcard_list.add(other);
         }
     }
 
     public void onDisconnect() {
         Log.d(Constants.LOG_TAG, "Disconnected from device " + active_partner);
         handler.obtainMessage(Constants.CLEAR_SCREEN, -1, -1, null).sendToTarget();
     }
 
     void showBadgeView() {
         switcher.setDisplayedChild(0);
     }
 
     void showScheduleView() {
         String[] ary = new String[vcard_list.size()];
         for (int i = 0; i < vcard_list.size(); i++) {
             ary[i] = vcard_list.get(i).name + "\n" + vcard_list.get(i).institution;
         }
         lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, ary));
         switcher.setDisplayedChild(1);
     }
 
     protected void onDestroy() {
         super.onDestroy();
         fm.onDestroy();
     }
 
     // MAC addresses for our demo.
     void initializeMACs() {
         macs.put(0, zhao_mac);
         macs.put(2, brandon_mac);
         macs.put(3, joe_laptop_mac);
     }
 
 }
 
