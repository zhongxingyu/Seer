 package communicator;
 
 import hypeerweb.*;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 
 /**
  * The deamon used to both send and receive commands from other applications that may be on this or other machines.
  * The Communicator is a singleton. Communicator must be in the same package of all applications.
  * @author Scott Woodfield
  */
 public class Communicator extends Thread{
 	public static final String className = Communicator.class.getName();
 	//Singleton
 	private static Communicator instance = null;
 	//Connection info for this server
 	private static RemoteAddress address;
 	//The socket this Communicator is listening on.
 	private static ServerSocket socket;
 	//The variable indicating whether this PeerCommunicator (a long running process) should stop.
 	private static boolean stop = false;
 	//Counter for local object ids
 	private static int LOCAL_ID_COUNTER = Integer.MIN_VALUE;
 	//Proxies that have been registered with the communicator
	private static enum ProxyType{NODE, LINKS, SEGMENT};
 	private static final HashMap<Class<?>, ProxyType> validProxies = new HashMap(){{
 		put(Node.class, ProxyType.NODE);
		put(Links.class, ProxyType.LINKS);
 		put(HyPeerWebSegment.class, ProxyType.SEGMENT);
 	}};
 	
 	/**
 	 * Starts up the communicator
 	 */
 	private Communicator(int port) {
 		try{
 	   		socket = new ServerSocket(port);
 			port = socket.getLocalPort();
 			address = new RemoteAddress(InetAddress.getLocalHost().getHostAddress(), port, 0);
 			System.out.println("Communicator: Listening on "+address);
 			this.start();
 		} catch(Exception e){
 			System.err.println("Fatal Error! Failed to start communicator!");
 			System.err.println(e.getMessage());
 			System.err.println(e.getStackTrace());
 		}
 	}
 	
 	/**
 	 * Creates the single PeerCommunicator listening on the indicated port number.
 	 * Must be invoked before a singleton is retrieved.
 	 * @param port port to listen on; must be within RemoteAddress.MAX/MIN_PORT constants;
 	 *	use zero to pick a port automatically
 	 */
 	public static void startup(int port){
 		instance = new Communicator(port);
 	}
 	/**
 	 * Shuts down the communicator thread
 	 * Also saves all Objects in the object database
 	 */
 	public static void shutdown(){
 		try {
 			stop = true;
 			socket.close();
 		} catch (IOException ex) {
 			System.err.println("Failed to close socket connection");
 		}
 	}
 
 	/**
 	 * Initializes the server listener thread
 	 * When it receives a command it forks a ServerThread that actually executes
 	 * the command on a local object or class.  ServerThreads are used so the
 	 * Communicator won't lose a command that comes soon after this current command.
 	 */
 	@Override
 	public void run(){
 		while (!stop){
 			try {
 				Socket client = socket.accept();
 				ServerThread serverThread = new ServerThread(client);
 				serverThread.start();
 			} catch(IOException e) {
 				System.err.println(e.getMessage());
 				System.err.println(e.getStackTrace());
 			}
 		}
 	}
 	
 	/**
 	 * Sends the indicated command to the target object indicated by the globalObjectId
 	 * @param raddr the identifier of the remote object
 	 * @param command the command to be sent to the remote object
 	 * @param sync should we wait for a response? (is this a synchronous command)
 	 * @return the results of the command, if sync is true
 	 */
 	public static Object request(RemoteAddress raddr, Command command, boolean sync){
 		Object result = null;
 		try {
 			command.sync = sync;
 			command.UID = raddr.UID;
 			//open a socket connection
 			Socket ext_socket = new Socket(raddr.ip, raddr.port);
 			//object streams for java objects
 			ObjectOutputStream oos = new ObjectOutputStream(ext_socket.getOutputStream());
 			ObjectInputStream ois = new ObjectInputStream(ext_socket.getInputStream());
 			oos.writeObject(command);
 			oos.flush();
 			//get the remote server's response message (if necessary)
 			if (sync) result = ois.readObject();
 			//close connection
 			oos.close();
 			ois.close();
 		} catch(IOException | ClassNotFoundException e){
 			String errmess = e.getMessage();
 			System.err.println(errmess == null ? e : errmess);
 			e.printStackTrace();
 		}
 		return result;
 	}
 	/**
 	 * Performs a handshake with a remote class, to make sure it exists
 	 * @param remoteClass the class to handshake with
 	 * @param addr the remote address of the class
 	 * @return whether handshake was successful
 	 */
 	public static boolean handshake(String remoteClass, RemoteAddress addr){
 		Command handshake = new Command(remoteClass, "handshake");
 		Object result = Communicator.request(addr, handshake, true);
 		return (result instanceof Boolean) && ((boolean) result);
 	}
 	
 	/**
 	 * Assigns a unique local object id
 	 * @return the unique id
 	 */
 	public static int assignId(){
 		return LOCAL_ID_COUNTER++;
 	}
 	/**
 	 * Resolve a UID to it's local object/proxy
 	 * @param class_type the class of the object to resolve
 	 * @param raw_uid the UID of the object to resolve
 	 * @return the local object with matching class type and UID; otherwise, a proxy
 	 **/
 	public static Object resolveId(Class<?> class_type, int raw_uid){
 		ProxyType type = validProxies.get(class_type);
 		if (type == null)
 			return null;
 		switch (type){
			case LINKS:
 			case NODE:
 				for (HyPeerWebSegment segment : HyPeerWebSegment.segmentList) {
 					Node node = segment.getSegmentNodeByUID(raw_uid);
 					if (node != null)
						return type == ProxyType.LINKS ? node.L : node;
 				}
 			//Segments are also nodes, so if NODE fails, check SEGMENT
 			case SEGMENT:
 				for (HyPeerWebSegment segment : HyPeerWebSegment.segmentList) {
 					if (segment.UID == raw_uid)
						return type == ProxyType.LINKS ? segment.L : segment;
 				}
				return null;				
 			default:
 				return null;
 		}
 	}
 	
 	/**
 	 * Retrieve the remote address for this communicator
 	 * @return the RemoteAddress object
 	 */
 	public static RemoteAddress getAddress(){
 		return address;
 	}
 }
