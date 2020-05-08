 package de.scrum_master.util;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 
 /**
 * PortUtil is used for some checks on a local system regarding tcp and udp ports
  */
 public final class PortUtil {
 
 	private static int MIN_PORT_NUMBER = 1024;
 	private static int MAX_PORT_NUMBER = 65535;
 
 	/**
	 * Checks to see if a specific port is available.
 	 *
	 * @param port the port to check for availability
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
	 * Checks all ports, starting with 1024, if they are available. Will return the first one found.
	 * @return a free tcp/udp port
 	 * @throws IOException if no port is available at all
 	 */
 	public static int getAvailablePort() throws IOException {
		int round = (int)Math.round(Math.random() * 3000) % 3000;
 		for (int i = MIN_PORT_NUMBER + round; i <=MAX_PORT_NUMBER; i++) {
 			if (isAvailable(i)) {
 				return i;
 			}
 		}
 		throw new IOException("no port available");
 	}

 }
