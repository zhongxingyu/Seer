 package edu.oregonstate.robotics;
 
 import java.net.*;
 import java.io.*;
 import java.nio.ByteBuffer;
 import android.util.Log;
 import android.hardware.Sensor;
 
 public class RosSensorIO implements Runnable {
 	Socket client;
 	DataOutputStream outStream;
 	String serverName;
 	int port;	
 	
 	RosSensorIO(String serverName, int port) {
 		this.serverName = serverName;
 		this.port = port;
 	}
 	
	private synchronized void sendPrefixedTriplet(int prefix, float a, float b, float c) {
 		try {			
 			// this can be deserialized in python with struct.unpack('>3f', data[1:])
 			// the first byte is a char representing which sensor the data is from
 			ByteBuffer bb = ByteBuffer.allocate(16);
			bb.putInt(prefix).putFloat(a).putFloat(b).putFloat(c);//.putInt(999).putInt(999);
 			this.outStream.write(bb.array());
 		} catch (IOException e) {
 			Log.w("RosSensorIO", "IO Exception: trying to reconnect");
 			try {
 				this.client.close();
 				this.run();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 		} catch (Exception e) {
 			@SuppressWarnings("unused")
 			int q = 5;
 		}
 	}
 	
 	public void sendAcceleration(float ax, float ay, float az) {
//		this.sendPrefixedTriplet(Sensor.TYPE_ACCELEROMETER, ax, ay, az);
 	}
 	
 	public void sendOrientation(float roll, float pitch, float yaw) {
//		this.sendPrefixedTriplet(Sensor.TYPE_ORIENTATION, -roll, pitch, -yaw+180);
		this.sendPrefixedTriplet(Sensor.TYPE_ORIENTATION, -roll, pitch+90, -yaw);

 	}
 
 	public void run() {
 		try {
 			this.client = new Socket(this.serverName, this.port);
 			OutputStream outToServer = client.getOutputStream();
 			outStream = new DataOutputStream(outToServer);
 		} catch (UnknownHostException e) {
 			Log.e("RosSensorIO", "UnknownHostException", e);
 		} catch (IOException e) {
 			Log.e("RosSensorIO", "IOException", e);
 		}
 	}
 
 }
