 package de.scrum_master.util;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 
 /**
 * Provides utility methods for checking availability of TCP/UDP ports
  */
 public final class PortUtil {
 
 	private static int MIN_PORT_NUMBER = 1024;
 	private static int MAX_PORT_NUMBER = 65535;
 
 	/**
	 * Checks if a specific TCP/UDP port is available
 	 *
	 * @param port the port to be checked for availability
	 * @return true if port is available; false if port is unavailable or outside legal range of 1,024..65,535
 	 */
 	public static boolean isAvailable(int port) {
 		if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
 			return false;
 		}
 		ServerSocket ss = null;
 		DatagramSocket ds = null;
 		try {
 			ss = new ServerSocket(port);
 			ss.setReuseAddress(true);
 			ds = new DatagramSocket(port);
 			ds.setReuseAddress(true);
 			return true;
 		} catch (IOException e) {
 			// todo... nothing to do it is not free
 		} finally {
 			if (ds != null) {
 				ds.close();
 			}
 
 			if (ss != null) {
 				try {
 					ss.close();
 				} catch (IOException e) {
					/* should not be thrown */
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
	 * Checks all TCP/UDP ports in the range of (1024 + a random offset between 0 and 3,000) and 65,535
	 * for availability
	 * @return the first free port within the search range
 	 * @throws IOException if no port is available at all
 	 */
 	public static int getAvailablePort() throws IOException {
		int round = (int) Math.round(Math.random() * 3000) % 3000;
 		for (int i = MIN_PORT_NUMBER + round; i <=MAX_PORT_NUMBER; i++) {
 			if (isAvailable(i)) {
 				return i;
 			}
 		}
 		throw new IOException("no port available");
 	}
 }
