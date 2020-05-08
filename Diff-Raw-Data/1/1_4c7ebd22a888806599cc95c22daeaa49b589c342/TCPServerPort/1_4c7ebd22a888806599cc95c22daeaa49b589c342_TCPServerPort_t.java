 package org.kde.kdroid.net;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.util.Log;
 
 public class TCPServerPort extends TCPPort implements Runnable {
 
 	private ServerSocket socket;
 	private boolean running = false;
 
 	public TCPServerPort(int Port) {
 		try {
 			port=Port;
 			socket = new ServerSocket(Port);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void run() {
 		while (running) {
 			try {
 				Socket s = socket.accept();
 				address = s.getInetAddress();
 				Log.d("KDroid","Accept");
 				Reader in = new BufferedReader(new InputStreamReader(
 						s.getInputStream(), "UTF8"));
 				out = s.getOutputStream();
 				byte[] bytes = new byte[2048];
 				int ch;
 				int pos = 0;
 				while ((ch = in.read()) > -1) {
 					if (ch == packetSeparator) {
 						if (dispatcher != null) {
 							dispatcher.dispatch(new Packet(bytes));
 						}
 						bytes = new byte[2048];
 						pos=0;
 					} else {
 						bytes[pos]=(byte) ch;
 						++pos;
 					}
 				}
 				Log.d("KDroid","Done");
 				out.flush();
 				s.close();
 				out = null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void setPort(int Port) {
 		stop();
 		port=Port;
 		try {
 			socket.close();
 			socket = new ServerSocket(Port);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void start() {
 		if (!running) {
 			running = true;
 			new Thread(this).start();
 		}
 	}
 
 	public void stop() {
 		running = false;
 	}
 
 }
