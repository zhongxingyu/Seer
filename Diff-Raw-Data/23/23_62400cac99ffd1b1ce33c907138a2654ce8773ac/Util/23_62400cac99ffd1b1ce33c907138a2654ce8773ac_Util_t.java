 package com.worthwhilegames.cardgames.shared;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 
 import org.apache.http.conn.util.InetAddressUtils;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 /**
  * A Utility class that contains common generic methods relevant
  * to any object in the application
  */
 public class Util {
 
 	public static final String TAG_GENERIC = Util.class.getName();
 
 	/**
 	 * Represents whether this device is acting as the gameboard
 	 */
 	private static boolean isGameboard = false;
 
 	/**
 	 * Checks whether this is a debug build or not.
 	 *
 	 * This toggles different logging capabilities
 	 *
 	 * @return whether this is a production build or not
 	 */
 	public static boolean isDebugBuild() {
		return false;
 	}
 
 	/**
 	 * Returns whether this device is acting as the gameboard
 	 *
 	 * @return whether this device is acting as the gameboard
 	 */
 	public static boolean isGameboard() {
 		return Util.isGameboard;
 	}
 
 	/**
 	 * Set whether this device is acting as the gameboard
 	 *
 	 * @param isGameboard whether the device is acting as the gameboard
 	 */
 	public static void setIsGameboard(boolean isGameboard) {
 		Util.isGameboard = isGameboard;
 	}
 
 	/**
 	 * Make sure that the device is in Bluetooth "Discoverable" mode
 	 *
 	 * @param ctx
 	 * @param btAdapter
 	 */
 	public static void ensureDiscoverable(Context ctx) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG_GENERIC, "ensure discoverable");
 		}
 		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
 			ctx.startActivity(discoverableIntent);
 		}
 	}
 
 	/**
 	 * Get this device's IP address
 	 * 
 	 * @return the ip address of this device
 	 */
 	public static InetAddress getLocalIpAddress() {
 		try {
 			String addrToReturn;
 
 			// Check for IPv4 address
 			for (Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces(); inter.hasMoreElements();) {
 				NetworkInterface intf = inter.nextElement();
 				for (Enumeration<InetAddress> enumIP = intf.getInetAddresses(); enumIP.hasMoreElements();) {
 					InetAddress inet = enumIP.nextElement();
 					addrToReturn = inet.getHostAddress();
 
 					if (Util.isDebugBuild()) {
 						Log.d(TAG_GENERIC, "Address: " + addrToReturn);
 						Log.d(TAG_GENERIC, "isIPv4Address: " + InetAddressUtils.isIPv4Address(addrToReturn));
 					}
 
 					if (!inet.isLoopbackAddress() && InetAddressUtils.isIPv4Address(addrToReturn)) {
 						return inet;
 					}
 				}
 			}
 
 			// Check for IPv6 address
 			for (Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces(); inter.hasMoreElements();) {
 				NetworkInterface intf = inter.nextElement();
 				for (Enumeration<InetAddress> enumIP = intf.getInetAddresses(); enumIP.hasMoreElements();) {
 					InetAddress inet = enumIP.nextElement();
 					addrToReturn = inet.getHostAddress();
 
 					int indexOfPercent = addrToReturn.indexOf('%');
 					if (indexOfPercent > 0) {
 						addrToReturn = addrToReturn.substring(0, indexOfPercent);
 					}
 
 					if (Util.isDebugBuild()) {
 						Log.d(TAG_GENERIC, "Address: " + addrToReturn);
 						Log.d(TAG_GENERIC, "isIPv6Address: " + InetAddressUtils.isIPv6Address(addrToReturn));
 						Log.d(TAG_GENERIC, "isIPv6HexCompressedAddress: " + InetAddressUtils.isIPv6HexCompressedAddress(addrToReturn));
 						Log.d(TAG_GENERIC, "isIPv6StdAddress: " + InetAddressUtils.isIPv6StdAddress(addrToReturn));
 					}
 
 					if (!inet.isLoopbackAddress() && (InetAddressUtils.isIPv6Address(addrToReturn) || InetAddressUtils.isIPv6HexCompressedAddress(addrToReturn)
 							|| InetAddressUtils.isIPv6StdAddress(addrToReturn))) {
 						return inet;
 					}
 				}
 			}
 
 		} catch (SocketException ex) {
 			Log.e(TAG_GENERIC, ex.toString());
 		}
 
 		return null;
 	}
 }
