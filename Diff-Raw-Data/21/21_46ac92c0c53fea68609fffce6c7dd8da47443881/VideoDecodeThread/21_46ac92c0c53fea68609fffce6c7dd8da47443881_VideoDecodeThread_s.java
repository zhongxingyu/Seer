 package com.pyrobot.droid;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class VideoDecodeThread extends Thread {
 	private static final String TAG = "VideoDecodeThread";
 	private boolean alive = false;
 	public static final String IP = "192.17.100.26";
	public static int PORT = 5305;
 	private int maxPacketSize = 10000;
 	private Handler handler;
 	
 	public VideoDecodeThread(Handler handler){
 		this.handler = handler;
 		alive = true;
 		this.start();
 	}
 	
 	public void shutdown(){
 		alive = false;
 	}
 	
 	public void run(){
 		try {
 			DatagramSocket s = new DatagramSocket(PORT);
 			byte[] data = new byte[maxPacketSize];
 			DatagramPacket p = new DatagramPacket(data, maxPacketSize);
 			Log.i(TAG, "Listening for packets..");
 			while(alive ) {
 				s.receive(p);
 				Message msg = new Message();
 				Bundle b = new Bundle();
 				b.putByteArray("packet", data);
 				msg.setData(b);
 				handler.sendMessage(msg);
 			}
			s.close();
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 		}
 	}
 
 }
