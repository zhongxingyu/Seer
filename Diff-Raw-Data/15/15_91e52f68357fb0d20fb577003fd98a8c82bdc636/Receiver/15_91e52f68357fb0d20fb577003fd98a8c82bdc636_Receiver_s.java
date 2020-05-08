 package sta.andswtch.network;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Enumeration;
 
 import android.util.Log;
 
 public class Receiver implements Runnable {
 
 	private static final String TAG = Receiver.class.getName();
 
 	/**
 	 * a status variable to determine if the server is currently running or not.
 	 */
 	private boolean inactive = true;
 	private IConnectionManager connectionManager;
 	private DatagramSocket socket;
 	private Thread thread;
 	private DatagramPacket packet;
 
 	public Receiver(IConnectionManager conManager) {
 		this.connectionManager = conManager;
 	}
 
 	public void start(int port) throws IOException {
 
 		InetAddress localIP = getLocalIpAddress();
		String localIPString = localIP.getHostAddress();
 
 		Log.d(TAG, "try to create a DatagramSocket" + localIPString + ":"
 				+ port);
 
 		this.thread = new Thread(this, "Andswtch UDP_Server");
 		this.socket = new DatagramSocket(port, localIP);
 
 		if (inactive == true) {
 			Log.d(TAG, "starting server at  " + localIPString + ":" + port);
 			this.thread.start();
 		} else {
 			Log.e(TAG, "there is already a server running");
 			// TODO error handling?
 		}
 
 	}
 
 	public void run() {
 		try {
 			this.socket.setSoTimeout(2000);
 
 			byte[] buf = new byte[400];
 
 			this.packet = new DatagramPacket(buf, buf.length);
 			Log.d(TAG, "Server: Receiving...");
 
 			/* Receive the UDP-Packet */
 			this.socket.receive(this.packet);
 			Log.d(TAG, "Server: Received: '"
 					+ new String(this.packet.getData()) + "'");
 			Log.d(TAG, "Server: Done.");
 
 			this.connectionManager.updateDatastructure(new String(this.packet
 					.getData()));
 
 		} catch (SocketTimeoutException e) {
 			this.connectionManager.errorAlert("Connection timed out");
 			this.socket.close();
 		} catch (Exception e) {
 			Log.e(TAG, "S: Error", e);
 		}
 		this.socket.close();
 		this.inactive = true;
 	}
 
 	/**
 	 * gets the first (!!) local ip address, which is not the loopbackaddress.
 	 * 
 	 * @return
 	 */
 	private InetAddress getLocalIpAddress() {
 		/**
 		 * works if you are connected to a wifi network. if you want to use it
 		 * over vpn connection, this should be changed
 		 */
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface
 					.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				for (Enumeration<InetAddress> enumIpAddr = intf
 						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 					InetAddress inetAddress = enumIpAddr.nextElement();
 					if (!inetAddress.isLoopbackAddress()) {
 						Log.d(TAG, "inetAddress is: "
 								+ inetAddress.getHostAddress().toString());
 						return inetAddress;
 					}
 				}
 			}
 		} catch (SocketException ex) {
 			this.connectionManager.errorAlert("no network permission");
 			Log.e(TAG,
 					"error while attemting to find out the local IP: "
 							+ ex.toString());
 		}
 		return null;
 	}
 
 }
