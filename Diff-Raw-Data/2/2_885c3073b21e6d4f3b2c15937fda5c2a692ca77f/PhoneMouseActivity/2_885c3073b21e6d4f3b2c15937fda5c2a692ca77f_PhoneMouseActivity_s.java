 package com.raphaelyu.phonemouse;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.DatagramChannel;
 import java.util.Arrays;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import com.raphaelyu.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class PhoneMouseActivity extends Activity implements SensorEventListener {
     final static short PHONE_MOUSE_PORT = 5329;
     final static int MAX_PACKET_LENGTH = 32;
     final static byte PACKET_TYPE_DISCOVER = 0x1;
     final static byte PACKET_TYPE_REPLY = 0x2;
     final static byte PACKET_TYPE_MOVE = 0x3;
     private static final float EPSILON = 0.00f;
 
     // private float mPosX;
 
     // private float mPosY;
 
     private float mLastX = 0.0f;
 
     private float mLastY = 0.0f;
 
     private TextView mTvAcceleration;
 
     private ToggleButton mTbSwitch;
 
     private CoordinateView mVwCoordinate;
 
     private SensorManager mSensorManager;
 
     private SocketAddress mServerAddr = null;
 
     private ViewGroup mLayoutNoServer;
 
     private ViewGroup mLayoutControl;
 
     private Button mBtnRetry;
 
     private class DiscoverTask extends AsyncTask<Void, Void, Void> {
 
         Dialog mDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             mDialog = ProgressDialog
                     .show(PhoneMouseActivity.this, null, "searching...", true, true);
         }
 
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
             if (mServerAddr != null) {
                 mLayoutControl.setVisibility(View.VISIBLE);
                 mLayoutNoServer.setVisibility(View.GONE);
             } else {
                 // TODO
             }
             mDialog.dismiss();
         }
 
         @Override
         protected Void doInBackground(Void... params) {
             byte[] buf = new byte[1];
             InetAddress broadcastIP;
             try {
                 broadcastIP = Inet4Address.getByName("255.255.255.255");
                 try {
                     DatagramSocket socket = new DatagramSocket();
                     socket.setSoTimeout(8000);
 
                     DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastIP,
                             PHONE_MOUSE_PORT);
                     buf[0] = PACKET_TYPE_DISCOVER;
                     socket.send(packet);
                     socket.receive(packet);
                     if (packet.getLength() == 1) {
                         byte[] buffer = packet.getData();
                         if (buffer[0] != PACKET_TYPE_REPLY) {
                             mServerAddr = null;
                             return null;
                         }
                     } else {
                         mServerAddr = null;
                         return null;
                     }
                     mServerAddr = packet.getSocketAddress();
                     socket.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } catch (UnknownHostException e1) {
                 e1.printStackTrace();
             }
             return null;
         }
     }
 
     private ByteBuffer mPacketBuffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
 
     private void sendMovePacket(float x, float y) {
         if (mServerAddr != null) {
            mPacketBuffer.reset();
             // ByteArrayOutputStream baos = new ByteArrayOutputStream();
             // DataOutputStream dos = new DataOutputStream(baos);
             try {
                 // dos.writeByte(PACKET_TYPE_MOVE);
                 // dos.writeLong(System.currentTimeMillis());
                 // dos.writeFloat(x);
                 // dos.writeFloat(y);
                 // dos.close();
                 mPacketBuffer.put(PACKET_TYPE_MOVE);
                 mPacketBuffer.putLong(System.currentTimeMillis());
                 mPacketBuffer.putFloat(x);
                 mPacketBuffer.putFloat(y);
                 mPacketBuffer.flip();
 
                 DatagramChannel channel = DatagramChannel.open();
                 channel.configureBlocking(false);
                 channel.send(mPacketBuffer, mServerAddr);
                 // byte[] data = baos.toByteArray();
                 // DatagramSocket socket = new DatagramSocket();
                 // DatagramPacket packet = new DatagramPacket(data, data.length,
                 // mServerAddr);
                 // socket.send(packet);
                 // socket.close();
                 channel.close();
             } catch (Exception e) {
                 // TODO: handle exception
             }
         }
     }
 
     private void onNewPosition(float x, float y) {
         // update the UI
         mVwCoordinate.setPosition((float) x, (float) y);
         mTvAcceleration.setText(String.format("%.4f %.4f", x, y));
         mVwCoordinate.invalidate();
 
         sendMovePacket(x, y);
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         mBtnRetry = (Button) findViewById(R.id.btn_retry);
         mVwCoordinate = (CoordinateView) findViewById(R.id.vw_coordinate);
         mTvAcceleration = (TextView) findViewById(R.id.tv_acc);
         mTbSwitch = (ToggleButton) findViewById(R.id.tb_switch);
         mLayoutControl = (ViewGroup) findViewById(R.id.layout_control);
         mLayoutNoServer = (ViewGroup) findViewById(R.id.layout_no_server);
         mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
         mTbSwitch.setChecked(false);
         mLayoutControl.setVisibility(View.GONE);
         mBtnRetry.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 new DiscoverTask().execute();
             }
         });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                 SensorManager.SENSOR_DELAY_GAME);
         new DiscoverTask().execute();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mTbSwitch.setChecked(false);
         mSensorManager.unregisterListener(this);
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         boolean valuesUpdated = false;
         float values[] = event.values;
 
         float newX = values[0] / 9.81f;
         float newY = -values[1] / 9.81f;
 
         if (mTbSwitch.isChecked()) {
             // 过滤传感器的误差
             float deltaX = 0f;
             float deltaY = 0f;
             if (Math.abs(newX - mLastX) > EPSILON) {
                 deltaX = newX - mLastX;
                 mLastX = newX;
                 valuesUpdated = true;
             }
             if (Math.abs(newY - mLastY) > EPSILON) {
                 deltaY = newY - mLastY;
                 mLastY = newY;
                 valuesUpdated = true;
             }
             if (valuesUpdated) {
                 onNewPosition(deltaX, deltaY);
             }
         } else {
             mLastX = newX;
             mLastY = newY;
         }
     }
 }
