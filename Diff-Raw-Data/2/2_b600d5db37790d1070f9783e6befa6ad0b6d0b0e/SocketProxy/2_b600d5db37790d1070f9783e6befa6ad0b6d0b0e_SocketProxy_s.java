 /**
  * Ti.UDP Module
  * Copyright (c) 2010-2011 by Appcelerator, Inc. All Rights Reserved.
  * Please see the LICENSE included with this distribution for details.
  */
 package ti.udp;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.HashMap;
 
 import org.appcelerator.kroll.KrollDict;
 import org.appcelerator.kroll.KrollProxy;
 import org.appcelerator.kroll.annotations.Kroll;
 import org.appcelerator.kroll.common.Log;
 import org.appcelerator.titanium.TiApplication;
 import org.appcelerator.titanium.TiContext;
 import org.appcelerator.titanium.util.TiConvert;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.DhcpInfo;
 import android.net.wifi.WifiManager;
 
 // This proxy can be created by calling Udp.createExample({message: "hello world"})
 @Kroll.proxy(creatableInModule = UdpModule.class)
 public class SocketProxy extends KrollProxy {
 
 	// Standard Debugging Variables
 	private static final String LCAT = "SocketProxy";
 
 	// Private Instance Variables
 	private boolean _continueListening;
 	private Thread _listeningThread;
 	private DatagramSocket _socket;
 	private Integer _port;
 
 	// Constructor
 	public SocketProxy(TiContext tiContext) {
 		super(tiContext);
 	}
 
 	// Start Utility Methods
 
 	@Override
 	protected void finalize() throws Throwable {
 		stop();
 		super.finalize();
 	}
 
 	public String getLocalIpAddress() {
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 					InetAddress inetAddress = enumIpAddr.nextElement();
 					if (!inetAddress.isLoopbackAddress()) {
 						return inetAddress.getHostAddress().toString();
 					}
 				}
 			}
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	private InetAddress getBroadcastAddress() {
 		try {
 			Activity activity = TiApplication.getInstance().getCurrentActivity();
 			WifiManager wifi = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
 			DhcpInfo dhcp = wifi.getDhcpInfo();
 
 			// TODO: handle null (for WIFI, or DHCP)
 
 			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
 			byte[] quads = new byte[4];
 			for (int k = 0; k < 4; k++)
 				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
 
 			return InetAddress.getByAddress(quads);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private void fireStarted() {
 		fireEvent("started", new HashMap<String, Object>());
 	}
 
 	private void fireError(Object obj) {
 		HashMap<String, Object> evt = new HashMap<String, Object>();
		evt.put("error", obj);
 		fireEvent("error", evt);
 	}
 
 	private void startListening() {
 		if (_listeningThread != null) {
 			return;
 		}
 		_continueListening = true;
 		_listeningThread = new Thread() {
 			public void run() {
 				while (_continueListening) {
 					try {
 						byte[] buf = new byte[256];
 						DatagramPacket packet = new DatagramPacket(buf, buf.length);
 						_socket.receive(packet);
 
 						byte[] rawResponse = packet.getData();
 						byte[] byteResponse = new byte[packet.getLength()];
 						Integer[] arrayResponse = new Integer[byteResponse.length];
 						for (int i = 0; i < byteResponse.length; i++) {
 							byteResponse[i] = rawResponse[i];
 							arrayResponse[i] = new Integer(rawResponse[i] & 0xff);
 						}
 						HashMap<String, Object> evt = new HashMap<String, Object>();
 						evt.put("bytesData", arrayResponse);
 						evt.put("stringData", new String(byteResponse));
 						evt.put("address", packet.getAddress() + ":" + packet.getPort());
 						fireEvent("data", evt);
 					} catch (IOException e) {
 						if (e.getLocalizedMessage().contains("Interrupted system call")) {
 							_continueListening = false;
 						} else {
 							fireError(e);
 						}
 					}
 				}
 			}
 		};
 		_listeningThread.start();
 	}
 
 	private void stopListening() {
 		_continueListening = false;
 		_listeningThread.interrupt();
 		_listeningThread = null;
 	}
 
 	// End Utility Methods
 
 	// Start Public API
 
 	@Kroll.method
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void start(HashMap hm) {
 		KrollDict args = new KrollDict(hm);
 		try {
 			if (_socket != null) {
 				fireError("Socket already started! Explicitly call stop() before attempting to start it again!");
 				return;
 			}
 
 			_port = args.getInt("port");
 			_socket = new DatagramSocket(_port);
 			_socket.setSoTimeout(0);
 
 			startListening();
 
 			fireStarted();
 			Log.i(LCAT, "Socket Started!");
 
 		} catch (SocketException e) {
 			fireError(e);
 		}
 	}
 
 	@Kroll.method
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void sendString(HashMap hm) {
 		KrollDict args = new KrollDict(hm);
 		try {
 			if (_socket == null) {
 				fireError("Cannot send data before the socket is started as a client or server!");
 				return;
 			}
 			String data = args.getString("data");
 			byte[] bytes = data.getBytes();
 
 			String host = args.getString("host");
 			int port = args.optInt("port", _port);
 			InetAddress _address = host != null ? InetAddress.getByName(host) : getBroadcastAddress();
 			_socket.send(new DatagramPacket(bytes, bytes.length, _address, port));
 			Log.i(LCAT, "Data Sent!");
 		} catch (IOException e) {
 			fireError(e);
 		}
 	}
 
 	@Kroll.method
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void sendBytes(HashMap hm) {
 		KrollDict args = new KrollDict(hm);
 		try {
 			if (_socket == null) {
 				fireError("Cannot send data before the socket is started as a client or server!");
 				return;
 			}
 			Object[] data = (Object[]) args.get("data");
 			byte[] bytes = new byte[data.length];
 			for (int i = 0; i < bytes.length; i++) {
 				bytes[i] = (byte) TiConvert.toInt(data[i]);
 			}
 
 			String host = args.getString("host");
 			int port = args.optInt("port", _port);
 			InetAddress _address = host != null ? InetAddress.getByName(host) : getBroadcastAddress();
 			_socket.send(new DatagramPacket(bytes, bytes.length, _address, port));
 			Log.i(LCAT, "Data Sent!");
 		} catch (IOException e) {
 			fireError(e);
 		}
 	}
 
 	@Kroll.method
 	public void stop() {
 		if (_socket != null) {
 			stopListening();
 			_socket.close();
 			_socket = null;
 			Log.i(LCAT, "Stopped!");
 		}
 	}
 
 	// End Public API
 
 }
