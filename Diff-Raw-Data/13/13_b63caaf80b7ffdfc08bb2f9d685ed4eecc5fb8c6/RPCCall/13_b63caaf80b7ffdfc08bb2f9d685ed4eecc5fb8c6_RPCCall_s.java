 package edu.uw.cs.cse461.net.rpc;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.net.base.NetBase;
 import edu.uw.cs.cse461.net.base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.net.tcpmessagehandler.TCPMessageHandler;
 import edu.uw.cs.cse461.util.Log;
 
 
 /**
  * Class implementing the caller side of RPC -- the RPCCall.invoke() method.
  * The invoke() method itself is static, for the convenience of the callers,
  * but this class is a normal, loadable, service.
  * <p>
  * <p>
  * This class is responsible for implementing persistent connections. 
  * (What you might think of as the actual remote call code is in RCPCallerSocket.java.)
  * Implementing persistence requires keeping a cache that must be cleaned periodically.
  * We do that using a cleaner thread.
  * 
  * @author zahorjan
  *
  */
 public class RPCCall extends NetLoadableService {
 	private static final String TAG="RPCCall";
 	private static final String connection = "keep-alive";
	//private static final long DELAY = 300000; //5 minutes
	private static final long DELAY = 20000; //for testing purpose 
 	
 	//private Task cleanCache;
 	
 	// a cache for persistent connection
 	private HashMap<HashMap<String, Integer>, Socket> socketCache;
 
 	boolean persistentConnection;
 	private Timer timer;
 	
 
 	//-------------------------------------------------------------------------------------------
 	//-------------------------------------------------------------------------------------------
 	// The static versions of invoke() are just a convenience for caller's -- it
 	// makes sure the RPCCall service is actually running, and then invokes the
 	// the code that actually implements invoke.
 	
 	/**
 	 * Invokes method() on serviceName located on remote host ip:port.
 	 * @param ip Remote host's ip address
 	 * @param port RPC service port on remote host
 	 * @param serviceName Name of service to be invoked
 	 * @param method Name of method of the service to invoke
 	 * @param userRequest Arguments to call
 	 * @param socketTimeout Maximum time to wait for a response, in msec.
 	 * @return Returns whatever the remote method returns.
 	 * @throws Exception 
 	 */
 	public static JSONObject invoke(
 			String ip,				  // ip or dns name of remote host
 			int port,                 // port that RPC is listening on on the remote host
 			String serviceName,       // name of the remote service
 			String method,            // name of that service's method to invoke
 			JSONObject userRequest,   // arguments to send to remote method,
 			int socketTimeout         // timeout for this call, in msec.
 			) throws Exception {
 		RPCCall rpcCallObj =  (RPCCall)NetBase.theNetBase().getService( "rpccall" );
 		if ( rpcCallObj == null ) throw new IOException("RPCCall.invoke() called but the RPCCall service isn't loaded");
 		return rpcCallObj._invoke(ip, port, serviceName, method, userRequest, socketTimeout, true);
 	}
 	
 	/**
 	 * A convenience implementation of invoke() that doesn't require caller to set a timeout.
 	 * The timeout is set to the net.timeout.socket entry from the config file, or 2 seconds if that
 	 * doesn't exist.
 	 * @throws Exception 
 	 */
 	public static JSONObject invoke(
 			String ip,				  // ip or dns name of remote host
 			int port,                 // port that RPC is listening on on the remote host
 			String serviceName,       // name of the remote service
 			String method,            // name of that service's method to invoke
 			JSONObject userRequest    // arguments to send to remote method,
 			) throws Exception {
 		int socketTimeout  = NetBase.theNetBase().config().getAsInt("net.timeout.socket", 2000);
 		return invoke(ip, port, serviceName, method, userRequest, socketTimeout);
 	}
 
 	//-------------------------------------------------------------------------------------------
 	//-------------------------------------------------------------------------------------------
 	
 	/**
 	 * The infrastructure requires a public constructor taking no arguments.  Plus, we need a constructor.
 	 */
 	public RPCCall() {
 		super("rpccall");
 		socketCache = new HashMap<HashMap<String, Integer>, Socket>();
 		//set a new timer and set delay for 5 minutes
 		timer = new Timer();
 		persistentConnection = false;
 	}
 
 	/**
 	 * This private method performs the actual invocation, including the management of persistent connections.
 	 * Note that because we may issue the call twice, we  may (a) cause it to be executed twice at the server(!),
 	 * and (b) may end up blocking the caller for around twice the timeout specified in the call. (!)
 	 * 
 	 * @param ip
 	 * @param port
 	 * @param serviceName
 	 * @param method
 	 * @param userRequest
 	 * @param socketTimeout Max time to wait for this call
 	 * @param tryAgain Set to true if you want to repeat call if a socket error occurs; e.g., persistent socket is no good when you use it
 	 * @return
 	 * @throws Exception 
 	 */
 	private JSONObject _invoke(
 			String ip,				  // ip or dns name of remote host
 			int port,                 // port that RPC is listening on on the remote host
 			String serviceName,       // name of the remote service
 			String method,            // name of that service's method to invoke
 			JSONObject userRequest,   // arguments to send to remote method
 			int socketTimeout,        // max time to wait for reply
 			boolean tryAgain          // true if an invocation failure on a persistent connection should cause a re-try of the call, false to give up
 	) throws Exception {
 		HashMap<String, Integer> key = new HashMap<String, Integer>();
 		key.put(ip, port);
 		Socket socket =  socketCache.get(key);
 		boolean firstConnect = false;
 
 		if (socket == null){
 			firstConnect = true;
 			socket = new Socket(ip, port);
 			socket.setKeepAlive(true);
 			socket.setReuseAddress(true);
 			socketCache.put(key, socket);
 		}
 
 		TCPMessageHandler tcpMessageHandlerSocket = new TCPMessageHandler(socket);
 		tcpMessageHandlerSocket.setTimeout(socketTimeout);
 		tcpMessageHandlerSocket.setNoDelay(true);
 
 		/* Initial control handshake: calling RPC service */
 		if (firstConnect){
 			// send connect msg
 			JSONObject option = new JSONObject().put("connection", connection);
 			RPCMessage rpcConnect = new RPCMessage();
 			JSONObject jsonConnect = rpcConnect.marshall();
 			jsonConnect.put("action", "connect").put("type", "control").put("options", option);
 			persistentConnection = option.getString("connection").equalsIgnoreCase("keep-alive");
 			RPCMessage connectMSG = RPCMessage.unmarshall(jsonConnect.toString());
 			tcpMessageHandlerSocket.sendMessage(connectMSG.marshall());
 
 			// read response msg
 			JSONObject connectResponse = tcpMessageHandlerSocket.readMessageAsJSONObject();
 			int callid = connectResponse.getInt("callid");
 			String type = connectResponse.getString("type");
 			if ( ! type.equalsIgnoreCase("OK") || callid != connectMSG.id()){
 				throw new Exception("Bad connect response: '"  + "'");
 			}
 		}
 		/* RPC Invocation */
 		// send invoke msg
 		RPCMessage rpcInvoke = new RPCMessage();
 		JSONObject jsonInvoke = rpcInvoke.marshall();
 
 		jsonInvoke.put("app", serviceName).put("args", userRequest)
 		.put("type", "invoke").put("method", method);
 
 		RPCMessage invokeMSG = RPCMessage.unmarshall(jsonInvoke.toString());
 		tcpMessageHandlerSocket.sendMessage(invokeMSG.marshall());
 
 		// read response invocation msg
 		JSONObject invokeResponse = tcpMessageHandlerSocket.readMessageAsJSONObject();
 		String type = invokeResponse.getString("type");
 		int callid = invokeResponse.getInt("callid");
 		if ( ! type.equalsIgnoreCase("OK") || callid != invokeMSG.id() || !invokeResponse.has("value"))
 			throw new Exception("Bad invoke response");
 
 		//if initially the header send to service without requiring persistent connection
 		//close socket here
 
 		if (!persistentConnection){
 			socket.close();
 		}
 		
		timer.cancel();
 		timer = new Timer();
 		timer.schedule(new Task(), DELAY);
 		
 		return invokeResponse.getJSONObject("value");
 	}
 	
 	@Override
 	public void shutdown() {
 		
 	}
 	
 	@Override
 	public String dumpState() {
 		return "Current persistent connections are ...";
 	}
 	
 	class Task extends TimerTask {
 
 		@Override
 		public void run() {
 			//remove all the mapping in socketcache
 			for(HashMap<String, Integer> key: socketCache.keySet()){
 				for(String s: key.keySet()){
 					key.remove(s);
 				}
 				socketCache.remove(key);
 			}
 		}	
 	}
 }
