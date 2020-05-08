 /**
  * File: ServerID.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A class to hold the server number, IP address, and port number of servers
  * that is populated from a config file accessed by CloudServer
  */
 
 public class ServerID {
 	private int serverNumber;
 	private String serverAddress;
 	private int serverPort;
 	
 	/**
 	 * Constructor.
 	 *
 	 * @param _serverNumber
	 * @param _serverNumber
 	 * @param _serverPort
 	 */
 	public ServerID (int _serverNumber, String _serverAddress, int _serverPort) {
 		serverNumber = _serverNumber;
 		serverAddress = _serverAddress;
 		serverPort = _serverPort;
 	}
 
 	public int getNumber() {
 		return serverNumber;
 	}
 
 	public String getAddress() {
 		return serverAddress;
 	}
 	
 	public int getPort() {
 		return serverPort;
 	}
 }
