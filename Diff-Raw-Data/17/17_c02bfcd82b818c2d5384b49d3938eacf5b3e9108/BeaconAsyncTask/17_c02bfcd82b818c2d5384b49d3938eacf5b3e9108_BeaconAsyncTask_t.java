 package net.antoniy.beacon.impl;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
import java.nio.CharBuffer;
 import java.nio.channels.DatagramChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.util.Log;
 
 class BeaconAsyncTask extends AsyncTask<Void, Void, Void> {
 	
 	private static final String TAG = "BeaconAsyncTask";
 
 	private final static int LOCAL_PORT = 10010;
 	private final static long SEND_BEACON_INTERVAL = 1000L;
 //	private final static String SEND_DATA = "Hello, Beacon app!";
 	
 	private Context context;
 	private DatagramSocket socket = null;
 	private DatagramChannel udpChannel = null;
 	private String data;
 	
 	public BeaconAsyncTask(Context context, String data) {
 		this.context = context;
 		this.data = data;
 	}
 	
 	private boolean isWifiConnected() {
 		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 		return wifiInfo.isConnected();
 	}
 	
 //	public static void main(String[] args) {
 //		byte[] array = {1, 2, 3, 4};
 //		
 //		for (byte b : array) {
 //			System.out.print(b + " ");
 //		}
 //		System.out.println();
 //		
 //		array = reverse(array);
 //		
 //		for (byte b : array) {
 //			System.out.print(b + " ");
 //		}
 //		System.out.println();
 //	}
 	
 	private byte[] reverse(byte[] array) {
 		int limit = array.length / 2;
 		
 		for (int i = 0; i < limit; i++) {
 			byte tmp = array[i];
 			array[i] = array[array.length - i - 1];
 			array[array.length - i - 1] = tmp;
 		}
 		
 		return array;
 	}
 	
 	private byte[] getMyInetAddress() {
 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 
 		return reverse(BigInteger.valueOf(wifiManager.getConnectionInfo().getIpAddress()).toByteArray());
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 	}
 
 	@Override
 	protected Void doInBackground(Void... params) {
 		Log.i(TAG, "*** JOB STARTED!");
 
 		try {
 			InetSocketAddress localPort = new InetSocketAddress(LOCAL_PORT);
 			udpChannel = DatagramChannel.open();
 			udpChannel.socket().bind(localPort);
 			udpChannel.configureBlocking(false);
 			
 			socket = new DatagramSocket();
 			socket.setBroadcast(true);
 			
 			Selector selector = Selector.open();
 			
 			udpChannel.register(selector, SelectionKey.OP_READ);
 			
 			InetAddress broadcastAddr = getNetworkBroadcastAddr();
			ByteBuffer buffer = null;
 			long lastRun = System.currentTimeMillis();
 			CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
 			while(true) {
 				if(isCancelled() || !isWifiConnected()) {
 					break;
 				}
 				
 				if(selector.selectNow() > 0) {
 					Set<SelectionKey> selectionKeys = selector.selectedKeys();
 					for(Iterator<SelectionKey> i = selectionKeys.iterator(); i.hasNext();) {
 						SelectionKey key = i.next();
 						
 						i.remove();
 						
 						if(key.isReadable()) {
							buffer = ByteBuffer.allocate(1024);
 							InetSocketAddress senderAddr = (InetSocketAddress) udpChannel.receive(buffer);
 							
 							if(senderAddr != null && !Arrays.equals(senderAddr.getAddress().getAddress(), getMyInetAddress())) {
								String receivedData = new String(buffer.array()).trim();
 								
								Log.i(TAG, "UDP[" + senderAddr.getHostName() + ":" + senderAddr.getPort() +"]: " + receivedData + ", " + data.getBytes().length);
 							}
 
 						}
 						
 					}
 				}
 
 				if(System.currentTimeMillis() - lastRun > SEND_BEACON_INTERVAL) {
 					DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), broadcastAddr, LOCAL_PORT);
 					socket.send(packet);
 					
 					lastRun = System.currentTimeMillis();
 				}
 			}
 		} catch (SocketException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} finally {
 			try {
 				if(udpChannel != null) {
 					udpChannel.socket().close();
 					udpChannel.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				if(socket != null) {
 					socket.close();
 				}
 			}
 			
 		}
 		
 		Log.i(TAG, "*** JOB TERMINATED!");
 		
 		return null;
 	}
 	
 	private InetAddress getNetworkBroadcastAddr() {
 		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		DhcpInfo dhcp = wifi.getDhcpInfo();
 		// handle null somehow
 
 		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
 		byte[] quads = new byte[4];
 		for (int k = 0; k < 4; k++)
 			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
 		
 		InetAddress broadcastAddr = null;
 		try {
 			broadcastAddr = Inet4Address.getByAddress(quads);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		
 		Log.i(TAG, "Broadcast: " + broadcastAddr.getHostAddress());
 		
 		return broadcastAddr;
 	}
 	
 	@Override
 	protected void onCancelled() {
 		Log.i(TAG, "*** JOB FINISHED!");
 		
 		try {
 			if(udpChannel != null) {
 				udpChannel.socket().close();
 				udpChannel.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if(socket != null) {
 				socket.close();
 			}
 		}
 		
 		super.onCancelled();
 	}
 	
 }
