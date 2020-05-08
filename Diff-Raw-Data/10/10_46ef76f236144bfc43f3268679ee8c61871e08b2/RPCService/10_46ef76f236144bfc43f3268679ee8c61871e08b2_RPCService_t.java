 package edu.uw.cs.cse461.Net.RPC;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.IPFinder;
 import edu.uw.cs.cse461.util.Log;
 
 /**
  * Implements the server side of RPC that receives remote invocation requests.
  * 
  * @author zahorjan
  *
  */
 public class RPCService extends NetLoadableService implements RPCServiceInterface {
 	private static final String TAG="RPCService";
 	
 	private Map<Pair<String, String>, RPCCallableMethod> map;
 	private RPCThread thread;
 	private int portNum;
 	/**
 	 * Constructor.  Creates the Java ServerSocket and binds it to a port.
 	 * If the config file specifies an rpc.serverport value, it should be bound to that port.
 	 * Otherwise, you should specify port 0, meaning the operating system should choose a currently unused port.
 	 * (The config file settings are available via the OS object.)
 	 * <p>
 	 * Once the port is created, a thread needs to be created to listen for connections on it.
 	 * 
 	 * @throws Exception
 	 */
 	public RPCService() throws Exception {
 		super("rpc", true);
 		map = new HashMap<Pair<String, String>, RPCCallableMethod>();
 		try {
 			// Eclipse doesn't support System.console()
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 			ConfigManager config = NetBase.theNetBase().config();
			portNum = config.getAsInt("rpc.serverport", 0, TAG);
 			if ( portNum == 0 ) {
 				System.out.print("Enter the server's TCP port, or empty line to exit: ");
 				String targetTCPPortStr = console.readLine();
 				if ( targetTCPPortStr == null || targetTCPPortStr.trim().isEmpty() ) return;
 				else portNum = Integer.parseInt(targetTCPPortStr);
 			}
 			// Gets the port number and starts the thread.
 			thread = new RPCThread(portNum, this);
 			Thread t = new Thread(thread);
 			t.start();
 		} catch (IOException e) {
 			return;
 		}
 	}
 	
 	/**
 	 * System is shutting down imminently.  Do any cleanup required.
 	 */
 	@Override
 	public void shutdown() {
 		Log.d(TAG, "Shutting down");
 		thread.end();
 	}
 	
 	// Class for storing a pair of objects, in this case to store the information about a particular method
 	private class Pair<L, R> {
 		public final L left;
 		public final R right;
 
 		public Pair(L left, R right) {
 			this.left = left;
 		    this.right = right;
 		}
 		// Not really necessary, since we can access the fields on our own, but still good to have
 		public L getLeft() { return left; }
 		public R getRight() { return right; }
 
 		public int hashCode() { return left.hashCode() ^ right.hashCode(); }
 
 		public boolean equals(Object o) {
 			if (o == null) return false;
 		    if (!(o instanceof Pair)) return false;
 		    Pair pairo = (Pair) o;
 		    return this.left.equals(pairo.getLeft()) &&
 		           this.right.equals(pairo.getRight());
 		}
 	}
 	
 	/**
 	 * Allows threads to access the appropriate methods if they exist.
 	 * @param serviceName  The name of the service.
 	 * @param methodName  The external, well-known name of the service's method to call
 	 * @param args 	The arguments the method takes
 	 * @throws Exception 
 	 * 
 	 */
 	public synchronized JSONObject accessMethod(String serviceName, String methodName, JSONObject args) throws Exception {
 		Pair<String, String> key = new Pair<String, String> (serviceName, methodName);
 		if (map.containsKey(key)) {
 			return map.get(key).handleCall(args);
 		}
 		return null;		
 	}
 	
 	/**
 	 * Services and applications with RPC callable methods register them with the RPC service using this routine.
 	 * Those methods are then invoked as callbacks when an remote RPC request for them arrives.
 	 * @param serviceName  The name of the service.
 	 * @param methodName  The external, well-known name of the service's method to call
 	 * @param method The descriptor allowing invocation of the Java method implementing the call
 	 * @throws Exception
 	 */
 	@Override
 	public synchronized void registerHandler(String serviceName, String methodName, RPCCallableMethod method) throws Exception {
 		Pair<String, String> key = new Pair<String, String>(serviceName, methodName);
 		map.put(key, method);
 		// So a callback occurs when method.handleCall({args}) is called?
 	}
 	
 	/**
 	 * Returns the local IP address.
 	 * @return
 	 * @throws UnknownHostException
 	 */
 	@Override
 	public String localIP() throws UnknownHostException {
 		return IPFinder.getMyIP();
 	}
 
 	/**
 	 * Returns the port to which the RPC ServerSocket is bound.
 	 * @return
 	 */
 	@Override
 	public int localPort() {
 		return portNum;
 	}
 	
 	@Override
 	public String dumpState() {
 		return "some string";
 	}
 	
 }
