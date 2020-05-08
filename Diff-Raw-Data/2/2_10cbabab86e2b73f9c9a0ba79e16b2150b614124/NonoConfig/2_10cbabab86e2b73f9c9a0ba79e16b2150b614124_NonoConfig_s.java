 /**
  * CSE 403 AA
  * Project Nonogram: Backend
  * @author  HyeIn Kim (co-author Sean Wu)
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 
 package uw.cse403.nonogramfun.network;
 import java.io.IOException;
 import java.net.*;
 
 import uw.cse403.nonogramfun.utility.ParameterPolice;
 
 
 /**
  * NonoConfig stores and provides configuration information for client-server 
  * communication over a network, such as server name, server IP address, server port number
  * and NonoNetwork object with default configuration.
  */
 public class NonoConfig {
 	public static final String SERVER_NAME = "cubist.cs.washington.edu";
	//public static final String SERVER_NAME = "localhost";  // For testing 
 	public static final int BASE_PORT = 1027;
 	public static final int MAX_PORT = 10;
 	public static final int SOCKET_TIMEOUT = 5000;
 	public static final int MAX_READ_LENGTH = 1234567;
 
 	
 	// Private constructor
 	private NonoConfig() {}
 	
 	
 	/**
 	 * Returns a NonoNetwork object with default configuration for client - server communication.
 	 * MockNet is for mock testing. If given mockNet is not null, return itself. else return new NonoNetwork.
 	 * @return A NonoNetwork object with default configuration (Default server IP & port)
 	 * @throws IOException if there is an error in the underlying protocol, such as a TCP error.
 	 */
 	public static NonoNetwork getNonoNetwork(int port, NonoNetwork mockNet) throws IOException {
 		if(mockNet != null) {
 			return mockNet;
 		}else{
 			return new NonoNetwork(new Socket(getServerIP(), BASE_PORT + port));
 		}
 	}
 	
 	/**
 	 * Returns a Network object for server side. The boolean stub is for stub testing.
 	 * If stub is true, return a stub (fake) network. else return new NonoNetwork.
 	 * @return A Network object used at the server side.
 	 * @throws IOException if there is an error in the underlying protocol, such as a TCP error.
 	 */
 	public static Network getNetwork(Socket sock, boolean stub) throws IOException {
 		if(stub) {
 			return new NetworkStub();
 		}else{
 			return new NonoNetwork(sock);
 		}
 	}
 	
 	/**
 	 * Finds and returns a default server IP of this configuration. 
 	 * Returns null if cannot find default server IP.
 	 * @return A string represents the default serverIP.
 	 */
 	public static String getServerIP() {
 		return getServerIP(SERVER_NAME);
 	}
 	
 	/**
 	 * Finds and returns a server IP of the server with given name. 
 	 * Returns null if cannot find server IP.
 	 * @param serverName Name of a server
 	 * @return A string represents the serverIP of given server name.
 	 * @throws IllegalArgumentException if the given server name is null.
 	 */
 	public static String getServerIP(String serverName) {
 		ParameterPolice.checkIfNull(serverName, "Server Name");
 		
 		InetAddress inetAddress = null;
 		try {
 			inetAddress = InetAddress.getByName(serverName);
 		}catch (UnknownHostException e) {
 			return null;
 		}
 		return inetAddress.getHostAddress();
 	}
 }
