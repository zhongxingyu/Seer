 package de.dsi8.vhackandroidgame.logic.impl;
 
 import de.dsi8.dsi8acl.connection.impl.SocketConnection;
 import de.dsi8.dsi8acl.connection.impl.TCPProtocol;
 import de.dsi8.dsi8acl.connection.model.ConnectionParameter;
 
 /**
  * Configuration-parameter of the App.
  *  
  * @author Sven Nobis <sven.nobis@gmail.com>
  *
  */
 public class VHackAndroidGameConfiguration {
 
 	private final static TCPProtocol protocol;
 	
 	private final static String DEFAULT_PASSWORD = "";
	private final static String URL_BASE = "http://androidrccar.sven.to/connect/";
 	private final static int PORT = 4254;
 	
 	static {
 		protocol = new TCPProtocol(URL_BASE, PORT);
 		SocketConnection.registerProtocol(protocol);
 	}
 	
 	public static final TCPProtocol getProtocol() {
 		return protocol;
 	}
 	
 	// TODO: Add some custom parameters 
 	public static ConnectionParameter getConnectionDetails() {
 		return protocol.getConnectionDetails(DEFAULT_PASSWORD);
 	}
 }
