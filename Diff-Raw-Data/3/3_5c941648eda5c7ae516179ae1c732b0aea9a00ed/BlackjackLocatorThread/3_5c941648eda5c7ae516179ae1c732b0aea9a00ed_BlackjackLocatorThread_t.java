 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - BlackjackProtocol.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purposes: This thread monitors UDP broadcast messages on port 55556. In
  * particular, it is looking for messages that are in ASCII text that read
  * "WHERE BJP 1.0 <CRLF>". It responds by broadcasting a message of "HERE BJP 1.0 
  * <hostname> <CRLF>", identifying itself as a Blackjack Protocol 1.0 server.
  ******************************************************************************/
 package drexel.edu.blackjack.server.locator;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.MulticastSocket;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.logging.Logger;
 
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 /**
  * EXTRACREDIT: We are running a locator service with a
  * thread that monitors the UDP port 55556, checking for
  * broadcast messages looking for a BJP 1.0 server. The
  * multicast group that should be used is defined in
  * {@link #MULTICAST_GROUP}. 
  * <p>
  * The basic algorithm is this: a multicast socket is
  * opened on the agreed upon port, and the multicast
  * group is joined. The server then loops through its
  * network interfaces to find multicast-capable addresses
  * that it is associated with; there are quite likely
  * multiple addresses on multiple subnets. It makes a
  * list of these, and their associated submasks, with
  * only IPv4 addresses currently handled.
  * <p>
  * Then it waits for messages. When it gets one, it sees
  * if it's a BJP address request. If so, it looks at the
  * address of the requester, to guess what the best
  * network address is to use, as it wants to give them
  * one on the same subnet if possible. It then responds
  * to the message by broadcasting, over the same port,
  * its location information.
  * 
  * @author Jennifer
  */
 public class BlackjackLocatorThread extends Thread {
 
 	/**********************************************************
 	 * Local variables go here
 	 *********************************************************/
 
 	/**
 	 * The port for the BJP locator service
 	 */
 	public static final int PORT	= 55556;
 	
 	// Default buffer length. All the examples I see use size of
 	// 1024, though in IPv4 the max payload is 64K. Hrm...
 	public static final int DEFAULT_BUFFER_LENGTH	= 1024;
 	
 	/*
 	 *  This is what requests for the BJP locator service look like
 	 */
 	public static final String BJP_LOCATOR_REQUEST	= "WHERE BJP 1.0";
 	
 	// From the Javadocs: A multicast group is specified by a class D 
 	// IP address and by a standard UDP port number. Class D IP addresses 
 	// are in the range 224.0.0.0 to 239.255.255.255, inclusive. 
 	// The address 224.0.0.0 is reserved and should not be used.
 	/**
 	 * A fairly arbitrary but consistent multicast group that the server
 	 * will listen to, and which the client should broadcast to. It is
 	 * simply oe of the valid Class D IP addresses in the range that
 	 * are allowed for multicast groups, and has no special significance.
 	 */
 	public static final String MULTICAST_GROUP	= "233.5.128.17";
 	
 	/*
 	 *  This is what responses to the BJP locator service look like. This
 	 *  string is followed by the hostname.
 	 */
 	public static final String BJP_LOCATOR_RESPONSE	= "HERE BJP 1.0 ";
 
 	// And a logger for errors
 	private final static Logger LOGGER = BlackjackLogger.createLogger(BlackjackLocatorThread.class.getName()); 
 
 	/**********************************************************
 	 * Constructor goes here
 	 *********************************************************/
 
 	/**
 	 * Create a thread that is centered around processing input from,
 	 * and providing output to, a client that is connected through
 	 * this socket.
 	 */
 	public BlackjackLocatorThread() {
 		super( "BlackjackLocatorThread" );
 		LOGGER.finer( "Inside a blackjack locator thread constructor." );
 	}
 
 	/**********************************************************
 	 * This is the meat of the thread, the run() method. It
 	 * monitors UDP port 55556, looking for messages that are 
 	 * in ASCII text that read "WHERE BJP 1.0 <CRLF>". It 
 	 * responds by broadcasting a message of "HERE BJP 1.0 
 	 * <hostname> <CRLF>", identifying itself as a Blackjack 
 	 * Protocol 1.0 server.
 	 *********************************************************/
 	@Override
 	public void run() {
 		
 		MulticastSocket socket = null;
 		InetAddress group = null;
 		
 		try {
 			// Open the socket on the default port
 			socket = new MulticastSocket(PORT);
 			
 			// Figure out the group to join
 			group = InetAddress.getByName(MULTICAST_GROUP);
 			socket.joinGroup( group );
 			
 			// Create some byte buffers for dealing with the data
 			byte[] inputData	= new byte[DEFAULT_BUFFER_LENGTH];
 			byte[] outputData	= null;
 			
 			// Step through all the network interfaces available to get a
 			// list of potential addresses to use
 			List<AddressAndSubmask> serverAddresses = generatePossibleServerInfo();
 			
 			// Fall into an endless loop, reading data (unless we couldn't figure
 			// out any host addresses and have no output data to send requesters)
 			while( serverAddresses.size() > 0 ) {
 				
 				// For the incoming packet
 				DatagramPacket inputPacket = new DatagramPacket( 
 						inputData, inputData.length );
 				
 				// Read the next bit of input into it; not sure what happens if 
 				// it's over 1024 bytes, though
 				socket.receive( inputPacket );
 				
 				// I guess we use the default encoding to translate...
 				String input = new String( inputPacket.getData() );
 				
 				// Is it something we're interested in?
 				if( input != null && input.startsWith(BJP_LOCATOR_REQUEST) ) {
 					
 					// The client address is who sent this request, it really shouldn't be null
 					String clientAddress = null;
 					if( inputPacket != null && inputPacket.getAddress() != null && inputPacket.getAddress().getHostAddress() != null ) {
 						clientAddress = inputPacket.getAddress().getHostAddress();
 					}
 
 					if( clientAddress == null ) {
 						LOGGER.warning( "Got a locator request but could not figure out the client's address." );
 					} else {
 						// We need to figure out an appropriate address to offer them
 						// for us, our server address. Since we might go by multiple
 						// addresses, we look for one on the same subnet
 						String serverAddress = getServerAddressOnSameSubnet( clientAddress, serverAddresses);
 						
 						if( serverAddress == null ) {
 							LOGGER.warning( "Got a locator request from " + clientAddress + 
 									" but could not find an appropriate server address on the same subnet." );
 						} else {
 							
 							// Create the response as a string
 							StringBuilder str = new StringBuilder( BJP_LOCATOR_RESPONSE );
 							str.append( serverAddress );
 							
 							// And as bytes
 							outputData = str.toString().getBytes();
 
 							// Make a packet to broadcast in response
 							DatagramPacket outputPacket =
 						                  new DatagramPacket(outputData, outputData.length, group, PORT);
 
 							// And send it
 							socket.send( outputPacket );
 							
 							// Debug info
 							LOGGER.info( "Locator service responding to a query for service." );							
 							LOGGER.finer( "Client who sent it: " + clientAddress );
 							LOGGER.finer( "Address we gave them: " + serverAddress );
 						}						
 					}
 				}
 			}
 		} catch (SocketException e) {
 			LOGGER.severe( "Unable to start the locator service due to a socket error." );
 			LOGGER.severe( e.toString() );
 		} catch (IOException e) {
 			LOGGER.severe( "Unable to start the locator service due to an IO error." );
 			LOGGER.severe( e.toString() );
 		} finally {
 			// Clean up after ourselves
 			if( socket != null ) {
 				if( group != null ) {
 					try {
 						socket.leaveGroup(group);
 					} catch (IOException e) {
 						// At this point, we just ignore it
 					}
 				}
 				socket.close();
 			}
 		}
 	}
 
 	/**
 	 * Here, we know that some client somewhere has multicast
 	 * a message we heard, asking for our host information. Since
 	 * there might be possible host addresses to offer, we use
 	 * this method to find an appropriate one, whereby appropriate
 	 * means on the same subnet.
 	 * 
 	 * @param clientAddress Something like "127.4.58.28"
 	 * @param serverAddresses The potential server addresses (plus submask)
 	 * that we can use to satisfy this request
 	 * @return A host IP for ourselves as a server, located on the same subnet
 	 * as the requester, or null if there is no option on the same subnet
 	 */
 	private String getServerAddressOnSameSubnet(String clientAddress, List<AddressAndSubmask> serverAddresses) {
 		
 		String serverAddress = null;
 		
 		if( clientAddress != null && serverAddresses != null ) {
 				
 			// Now we need to find an appropriate server address given this hostAddress
 			for( AddressAndSubmask serverInfo : serverAddresses ) {
 				if( serverInfo.isOnSameSubnet(clientAddress) ) {
 					// We found one!
 					serverAddress = serverInfo.getAddress();
 				}
 			}
 		}
 		
 		return serverAddress;
 	}
 
 	/**
 	 * This comes up with a list of all the server addresses that we have which
 	 * we are capable of multicast, and pairs them up with their subnet mask,
 	 * then throws them all in a list. The reason we care about the subnet mask
 	 * is so that if there are multiple possible address/subnetmask combinations
 	 * (for example, virtual machines or multiple NIC cards), the locator can 
 	 * pick the one that's on the same subnet to send to them.
 	 * 
 	 * My laptop at least has multiple IP addresses from multiple NICs. One way
 	 * to handle this would be to pass in what the locator service should monitor
 	 * from, and what address it should supply, but figuring it out dynamically
 	 * (like this method attempts to do) seems better.
 	 * 
 	 * @return A non-null list of zero or more possible address&subnetmask pairings
 	 * @throws SocketException
 	 */
 	private List<AddressAndSubmask> generatePossibleServerInfo() throws SocketException {
 		
 		// We'll return this later
 		List<AddressAndSubmask> serverAddresses = new ArrayList<AddressAndSubmask>();
 		
 		// Step through all the network interfaces
 		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
 		while( interfaces.hasMoreElements() ) {
 			
 			// Only bother if multicast is supported
 		    NetworkInterface ni = interfaces.nextElement();
 		    if( ni.supportsMulticast() ) {
 		    	
 		    	// We need to consult the interface address to figure out the submask,
 		    	// which we can use to determine if hosts are on the same subnet. We'll
 		    	// just grab the first one we see on the network interface and assume
 		    	// they're all the same
 		    	if( ni.getInterfaceAddresses() != null && ni.getInterfaceAddresses().size() > 0 ) {
 		    		
 			    	// This will hold our subnet mask for this interface
 			    	String subnetMask = null;
 			    	
 		    		// Loop though the interface addresses, one by one
 		    		for( InterfaceAddress interfaceAddress : ni.getInterfaceAddresses() ) {
 		    			
 		    			if( subnetMask == null ) {
 				    		// The prefix length should determine our network class and the submask
 				    		int prefixLength = interfaceAddress.getNetworkPrefixLength();
 				    		if( prefixLength == 8 ) {
 				    			subnetMask = "255.0.0.0";
 				    		} else if( prefixLength == 16 ) {
 				    			subnetMask = "255.255.0.0";
 				    		} else {
 				    			// This probably isn't the best to default EVERYTHING to this...
 				    			subnetMask = "255.255.255.0";
 				    		}
 		    			}
 			    		
 			    		// If we found it, we're good
 			    		if( subnetMask != null &&  appearsToBeIPv4(interfaceAddress.getAddress())) {
 			                serverAddresses.add( new AddressAndSubmask(interfaceAddress.getAddress().getHostAddress(), subnetMask) );
 			                LOGGER.info( "Locator monitoring from " + interfaceAddress.getAddress().getHostAddress()
 			                		+ " with submask " + subnetMask + "." );
 			    		}
 		    		}		    		
 		    	}		    	
 		    }
 		}
 		
 		// And return it
 		return serverAddresses;
 	}
 
 	/**
 	 * The laziest method ever for deciding if something is IPv4, it 
 	 * just looks for at least one decimal point.
 	 * 
 	 * @param address The address to consider, maybe null
 	 * @return True if our shoddy algorithm thinks it's IPv4
 	 */
 	private boolean appearsToBeIPv4(InetAddress address) {
 		
 		boolean response = false;
 		
 		if( address != null && address.getHostAddress() != null ) {
 			response = address.getHostAddress().indexOf(".") > 0;
 		}
 		
 		return response;
 	}
 }
