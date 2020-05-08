 package com.aha.services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Vector;
 
 import com.aha.R;
 import com.aha.models.AppInfo;
 import com.aha.models.Constants;
 import com.aha.models.DataObject;
 import com.aha.models.NetworkInfo;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Binder;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.net.wifi.WifiInfo;
 
 public class NetService extends Service {
 
 	private static WifiManager wfMan;
 	private final IBinder mBinder = new LocalBinder();
 	private final Device device = new Device();
 	private DatagramSocket outSocket;
 	private ReceiveMessageThread rmt;
 	private NetworkThread nt;
 
 	@Override
 	public void onCreate() {
 
 		wfMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
 	}
 
 	public class LocalBinder extends Binder {
 		public NetService getService() {
 			// Return this instance of LocalService so clients can call public
 			// methods
 			return NetService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	/** method for clients */
 	public String getConnectionInfo() {
 
 		WifiInfo wfInfo = wfMan.getConnectionInfo();
 
 		return wfInfo.toString();
 
 	}
 
 	public String enableWifi() {
 
 		wfMan.setWifiEnabled(true);
 
 		return "Wifi Enabled";
 	}
 
 	public String disableWifi() {
 
 		wfMan.setWifiEnabled(false);
 		return "Wifi Disabled";
 	}
 
 	public String advertiseNet(int lRank, Context context, Handler handler) {
 
 		device.connectDevice();
 
 		return "Advertising Network";
 	}
 
 	public String netOff() {
 
 		device.disconnectDevice();
 		
 		NetworkInfo ni = NetworkInfo.getInstance();
 		
 		ni.setDeviceInitiated(false);
 		ni.setNetworkUp(false);
 		ni.setJoined(false);
 		ni.network.clear();
 		ni.conversations.clear();
 		
 		Handler alertsHandler = AppInfo.getInstance().getAlertsHandler();
 		
 		Handler convoHandler = AppInfo.getInstance().getConversationHandler();
 		
 		Handler netHandler = AppInfo.getInstance().getNetworkHandler();
 		
 		int ip = ni.getInitIP();
 		
 		if (netHandler != null)
 		{
			netHandler.obtainMessage(2, -1, -1, ip).sendToTarget();		
 		}
 		if (alertsHandler != null)
 			alertsHandler.obtainMessage(3, -1, -1, "")
 					.sendToTarget();
 		
 		if (convoHandler != null)
 			convoHandler.obtainMessage(3, -1, -1, "")
 					.sendToTarget();
 		
 		
 		// device.getStatus();
 
 		return "Network Off";
 	}
 
 	public String getStatus() {
 
 		if (device.doesNetworkExist())
 			return "Network is up";
 		else
 			return "Network off";
 
 	}
 
 	public synchronized String sendMessage(DataObject dataObject) {
 
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutputStream oos = new ObjectOutputStream(baos);
 			oos.writeObject(dataObject);
 			oos.flush();
 
 			byte[] buf = baos.toByteArray();
 
 			int l = buf.length;
 
 			byte[] len = new byte[4];
 
 			for (int i = 0; i < 4; ++i) {
 				int shift = i << 3; // i * 8
 				len[3 - i] = (byte) ((l & (0xff << shift)) >>> shift);
 			}
 
 			byte[] packet = new byte[512];
 
 			System.arraycopy(len, 0, packet, 0, len.length);
 			System.arraycopy(buf, 0, packet, len.length, buf.length);
 
 			outSocket = new DatagramSocket();
 			outSocket.setBroadcast(true);
 
 			DatagramPacket sendPacket;
 			sendPacket = new DatagramPacket(packet, packet.length,
 					InetAddress.getByName( Constants.BASE_ADDRESS + dataObject.getDestinationAddress()),
 					11111);
 
 			outSocket.send(sendPacket);
 
 			outSocket.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		// Toast.makeText(AdHoc.this, msg, Toast.LENGTH_SHORT).show();
 
 		return "hello";
 
 	}
 
 	public void startDaemon() {
 		try {
 			rmt = new ReceiveMessageThread();
 			rmt.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private class ReceiveMessageThread extends Thread {
 
 		DatagramSocket datagramSocket;
 		ObjectInputStream oos;
 
 		public ReceiveMessageThread() {}
 
 		public void run() {
 			try {
 				datagramSocket = new DatagramSocket(11111);
 				datagramSocket.setBroadcast(true);
 
 				while (NetworkInfo.getInstance().isNetworkUp()) {
 					try {
 						// 52kb buffer
 						byte[] buffer = new byte[512];
 
 						DatagramPacket brodcastReceivePacket = new DatagramPacket(
 								buffer, buffer.length);
 
 						datagramSocket.receive(brodcastReceivePacket);
 
 						int len = 0;
 						// byte[] -> int
 						for (int i = 0; i < 4; ++i) {
 							len |= (buffer[3 - i] & 0xff) << (i << 3);
 						}
 
 						
 						//System.out.println("Length = " + len);
 
 						byte[] packet = new byte[len];
 
 						System.arraycopy(buffer, 4, packet, 0, len);
 
 						ByteArrayInputStream baos = new ByteArrayInputStream(
 								packet);
 						oos = new ObjectInputStream(baos);
 						DataObject inObject = (DataObject) oos.readObject();
 
 						System.out.println("YOU HAVE A MESSAGE");
 						System.out.println("OIP: " + inObject.getOrginAddress());
 						System.out.println("DIP: " + inObject.getDestinationAddress());
 						System.out.println("TYPE: " + inObject.getMessageType());
 						
 						handleMessage(inObject);
 
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				
 				oos.close();
 				
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}// end run
 
 	}// end inner class
 
 	public void handleMessage(DataObject dataObject) {
 
 		MessageHandler mh = new MessageHandler(dataObject);
 		mh.start();
 
 	}
 
 	private class MessageHandler extends Thread {
 
 		DataObject inObject;
 		Handler netHandler;
 		//NetworkInfo ni;
 
 		public MessageHandler(DataObject dataObject) {
 
 			this.inObject = dataObject;
 			//ni = NetworkInfo.getInstance();
 
 		}
 
 		public void run() {
 			try {
 				int orginIP = inObject.getOrginAddress();
 
 				NetworkInfo ni = NetworkInfo.getInstance();
 
 				if (inObject.getOrginAddress() != ni.getMyIP()) {
 					
 					switch (inObject.getMessageType()) {
 					case Constants.JOIN:
 						
 						int highIP = 0;
 						
 						if (ni.network.size() == 0 || ni.network == null) {
 							highIP = 13;
 						}
 						else
 						{
 							Collections.sort(ni.network);
 							highIP = ni.network.get(ni.network.size()-1) + 1;
 	
 						}
 						
 						System.out.println(highIP);
 						
 						DataObject outObject = new DataObject();
 						outObject.setDestinationAddress(inObject
 								.getOrginAddress());
 						outObject.setOrginAddress(ni.getMyIP());
 						outObject.setMessageType(Constants.JOIN_ACK);
 						outObject.setMessage( "" + highIP);
 						sendMessage(outObject);
 						ni.network.add(highIP);	
 						
 						netHandler = AppInfo.getInstance().getNetworkHandler();
 						if (netHandler != null)
 							netHandler.obtainMessage(2, -1, -1, "").sendToTarget(); 						
 						
 						break;
 					case Constants.JOIN_ACK:
 						
 						System.out.println("THE JOIN HAS BEEN ACKED MYIP: " + inObject.getMessage());
 						ni.network.add(inObject.getOrginAddress());
 						String ip = "";
 
 						ip = inObject.getMessage();
 						ni.setMyIP(new Integer(inObject.getMessage()));
						device.changeIP(inObject.getMessage());
 						
 						netHandler = AppInfo.getInstance().getNetworkHandler();
 						if (netHandler != null)
 							netHandler.obtainMessage(2, -1, -1, ip).sendToTarget(); 
 					
 						
 						break;
 					case Constants.ALERT:
 						if (ni.conversations.containsKey(orginIP)) {
 							Vector<DataObject> v = ni.conversations
 									.get(orginIP);
 							v.add(inObject);
 						} else {
 							Vector<DataObject> v = new Vector<DataObject>();
 							v.add(inObject);
 							ni.conversations.put(orginIP, v);
 						}
 
 						Handler alertsHandler = AppInfo.getInstance()
 								.getAlertsHandler();
 						Handler convoHandler = AppInfo.getInstance()
 								.getConversationHandler();
 
 						if (alertsHandler != null)
 							alertsHandler.obtainMessage(3, -1, -1, "")
 									.sendToTarget();
 
 						if (convoHandler != null)
 							convoHandler.obtainMessage(3, -1, -1, "")
 									.sendToTarget();
 						break;
 					}
 
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}// end run
 	}// end inner class
 
 	public void startNetwork(int lRank) {
 
 		nt = new NetworkThread();
 		nt.start();
 
 	}
 
 	private class NetworkThread extends Thread {
 
 		NetworkInfo ni;
 
 		public NetworkThread() {
 
 			ni = NetworkInfo.getInstance();
 
 		}
 
 		public void run() {
 			try {
 				if (!ni.isNetworkUp()) {
 					if (!ni.isDeviceInitiated()) {
 						device.connectDevice();
 						ni.setDeviceInitiated(true);
 					}
 					
 
 					NetworkThread.sleep(5000); // (80000);
 
 					if (!device.doesNetworkExist()) {
 						device.disconnectDevice();
 						//handler.obtainMessage(3, -1, -1,
 						//		"There is no network available at this time")
 						//		.sendToTarget();
 					} else {
 						ni.setNetworkUp(device.doesNetworkExist());
 
 						//handler.obtainMessage(3, -1, -1, "The network is up")
 						//		.sendToTarget();
 
 						startDaemon();
 						
 						NetworkThread.sleep(7000);      
 						
 						
 						DataObject dataObject = new DataObject();
 						dataObject.setDestinationAddress(Constants.BROADCAST);
 						dataObject.setOrginAddress(ni.getMyIP());
 						dataObject.setMessageType(Constants.JOIN);
 						sendMessage(dataObject);		
 										
 					}
 
 
 
 					/*
 					 * 
 					 * NetworkThread.sleep(5000);
 					 * 
 					 * if(!ni.isJoined()) { //shut it down time is up //if an
 					 * eris keep working }
 					 */
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}// end run
 	}// end inner class
 
 	public void test() {
 		System.out.println("service connectewd!!");
 	}
 
 }
