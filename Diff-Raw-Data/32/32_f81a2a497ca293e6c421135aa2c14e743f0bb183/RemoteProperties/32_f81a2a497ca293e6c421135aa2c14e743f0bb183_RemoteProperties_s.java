 package com.ricex.rpi.remote.android.cache;
 
 
 public class RemoteProperties {
 
 	/** The singleton instance */
 	private static RemoteProperties _instance;
 	
 	/** Returns the singleton instance of this class */
 	public static RemoteProperties getInstance() {
 		if (_instance == null) {
 			_instance = new RemoteProperties();
 		}
 		return _instance;
 	}
 	
 	/** The address of the server to connect to */
 	private String serverAddress;
 	
 	/** The server's port */
 	private int serverPort;
 	
 	/** Private constructor, to keep this class singleton */
 	private RemoteProperties() {
 		//TODO: Implement a config/properties file for theese properties
		serverAddress = "192.168.1.161";
 		serverPort = 6520;
 	}
 
 	
 	/**
 	 * @return the serverAddress
 	 */
 	public String getServerAddress() {
 		return serverAddress;
 	}
 
 	
 	/**
 	 * @param serverAddress the serverAddress to set
 	 */
 	public void setServerAddress(String serverAddress) {
 		this.serverAddress = serverAddress;
 	}
 
 	
 	/**
 	 * @return the serverPort
 	 */
 	public int getServerPort() {
 		return serverPort;
 	}
 
 	
 	/**
 	 * @param serverPort the serverPort to set
 	 */
 	public void setServerPort(int serverPort) {
 		this.serverPort = serverPort;
 	}
 	
 	
 	
 	
 }
