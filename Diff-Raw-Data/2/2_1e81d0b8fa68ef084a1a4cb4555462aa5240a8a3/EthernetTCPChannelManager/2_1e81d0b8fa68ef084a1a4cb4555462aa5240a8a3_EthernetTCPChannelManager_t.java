 package org.unbiquitous.uos.network.socket.channelManager;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.unbiquitous.uos.core.UOSLogging;
 import org.unbiquitous.uos.core.network.cache.CacheController;
 import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
 import org.unbiquitous.uos.core.network.exceptions.NetworkException;
 import org.unbiquitous.uos.core.network.model.NetworkDevice;
 import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
 import org.unbiquitous.uos.network.socket.EthernetDevice;
 import org.unbiquitous.uos.network.socket.connection.EthernetTCPClientConnection;
 import org.unbiquitous.uos.network.socket.connection.EthernetTCPServerConnection;
 import org.unbiquitous.uos.network.socket.connectionManager.EthernetConnectionManager.EthernetConnectionType;
 
 
 public class EthernetTCPChannelManager implements ChannelManager{ 
 	
 	private static final Logger logger = UOSLogging.getLogger();
 	
 	/*********************************
 	 * ATTRIBUTES
 	 *********************************/
 	
 	private List<NetworkDevice> freePassiveDevices;
 	
 	private Map<String, EthernetTCPServerConnection> startedServers;
 	
 	private int defaultPort;
 	private int controlPort;
 	
 	/**
      * Controller responsible for the active connections cache. 
      */
     private CacheController cacheController;
 
 	private List<Integer> validPorts;
 	
 	/*********************************
 	 * CONSTRUCTORS
 	 * @param cacheController 
 	 *********************************/
 	
 	public EthernetTCPChannelManager(int defaultPort, int controlPort ,String portRange, CacheController cacheController){
 		
 		this.defaultPort = defaultPort;
 		this.controlPort = controlPort;
 		
 		this.cacheController = cacheController;
 		
 		this.startedServers = new HashMap<String, EthernetTCPServerConnection>();
 		
 		freePassiveDevices = new ArrayList<NetworkDevice>();
 		validPorts = new ArrayList<Integer>();
 		validPorts.add(defaultPort);
 		validPorts.add(controlPort);
 		String[] limitPorts = portRange.split("-");
 		int inferiorPort = Integer.parseInt(limitPorts[0]);
 		int superiorPort = Integer.parseInt(limitPorts[1]);
 		for(int port = inferiorPort; port <= superiorPort; port++){
 			validPorts.add(port);
 			freePassiveDevices.add(new EthernetDevice("0.0.0.0",port,EthernetConnectionType.TCP));
 		}
 	}
 	
 	/********************************
 	 * PUBLIC METHODS
 	 ********************************/
 	
 	public ClientConnection openActiveConnection(String networkDeviceName) throws NetworkException, IOException{
 		String[] address = networkDeviceName.split(":");
 		
 		String host ;
 		int port ;
 		if (address.length == 1){
 			port = defaultPort;
 		}else if(address.length == 2){
 			port = Integer.parseInt(address[1]);
 		}else{
 			throw new NetworkException("Invalid parameters for creation of the channel.");
 		}
 		
     	host = address[0];
     	
     	if (!validPorts.contains(port) ){
     		port = defaultPort;
     	}
     	
    	ClientConnection cached = cacheController.getConnection(networkDeviceName+':'+port);
 		if (cached != null){
 			logger.info("EthernetTCPChannelManager: openActiveConnection: Returning cached connection for host '"+host+"'\n\n"); 
 			return cached;
 		}
     	
 		try {
 			return new EthernetTCPClientConnection(host, port, cacheController);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	
 	public ClientConnection openPassiveConnection(String networkDeviceName) throws NetworkException, IOException{
 		String[] address = networkDeviceName.split(":");
 		
 		if(address.length != 2){
 			throw new NetworkException("Invalid parameters for creation of the channel.");
 		}
 		
 		EthernetTCPServerConnection server = startedServers.get(networkDeviceName);
 		if(server == null){
 			String host = address[0];
 	    	int port = Integer.parseInt(address[1]);
 			// Passive (Stream) connections shouldn't be cached
 	    	server = new EthernetTCPServerConnection(new EthernetDevice(host, port, EthernetConnectionType.TCP), null);
 	    	startedServers.put(networkDeviceName, server);
 		}
 		
 		return server.accept();
 	}
 	
 	
 	public NetworkDevice getAvailableNetworkDevice(){
 		NetworkDevice networkDevice = freePassiveDevices.remove(0);
 		freePassiveDevices.add(networkDevice);
 		return networkDevice;
 	}
 	
 	
 	public void tearDown() throws NetworkException, IOException {
 		for(EthernetTCPServerConnection server : startedServers.values()){
 			server.closeConnection();
 		}
 	}
 }
