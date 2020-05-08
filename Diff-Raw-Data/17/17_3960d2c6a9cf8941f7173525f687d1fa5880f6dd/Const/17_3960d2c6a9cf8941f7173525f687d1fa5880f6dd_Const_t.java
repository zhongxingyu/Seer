 package sw901e12.csp.util;
 
 public class Const {
 	/*
 	 * Limitation of the number of allowed hosts and
 	 * ports
 	 */
 	public static final byte MAX_NETWORK_HOSTS = 32;	
 
 	/* Incoming port range  */
 	public static final byte MAX_SERVICE_PORTS = 8;
	public static final byte MAX_BIND_PORTS = 40;
 	public static final byte MAX_INCOMING_PORTS = (MAX_SERVICE_PORTS + MAX_BIND_PORTS);
	public static final byte MAX_PORTS = MAX_BIND_PORTS + 1; /* +1 is for PORT_ANY */
 	
 	/* Outgoing port range */
	public static final byte MAX_OUTGOING_PORTS = 16;
 	
 	/*
 	 * Default Pool capacities that are used 
 	 * if the user does not supply any
 	 */
 	public static final byte DEFAULT_MAX_CONNECTIONS = 8;	
 	public static final byte DEFAULT_MAX_CONNECTION_PER_SOCKET = 2;
 	public static final byte DEFAULT_PACKET_QUEUE_SIZE_ROUTING = 32;
 	public static final byte DEFAULT_PACKET_QUEUE_SIZE_PER_CONNECTION = 4;
 	public static final byte DEFAULT_MAX_SOCKETS = 4;
 	public static final byte DEFAULT_MAX_PACKETS = 64;
 }
