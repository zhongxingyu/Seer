 package edu.uw.cs.cse461.sp12.OS;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;
 import edu.uw.cs.cse461.sp12.util.TCPMessageHandler;
 
 /**
  * Implements the side of RPC that receives remote invocation requests.
  * 
  * @author zahorjan
  *
  */
 public class RPCService extends RPCCallable {
 	// used with the android idiom Log.x, as in Log.v(TAG, "some debug log message")
 	public static final String TAG="RPCService";
 	
 	private ServerSocket mServerSocket;
 	private Thread connectionListener;
 	private Map<String, RPCCallableMethod> callbacks;
 	
 	/**
 	 * This method must be implemented by RPCCallable's.  
 	 * "rpc" is the well-known name of this service.
 	 */
 	@Override
 	public String servicename() {
 		return "rpc";
 	}
 	
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
 	RPCService() throws Exception {
 
 		// Set some socket options.  
 		// setReuseAddress lets you reuse a server port immediately after terminating
 		// an application that has used it.  (Normally that port is unavailable for a while, for reasons we'll see
 		// later in the course.
 		// setSoTimeout causes a thread waiting for connections to timeout, instead of waiting forever, if no connection
 		// is made before the timeout interval expires.  (You don't have to use 1/2 sec. for this value - choose your own.)
 		String port = OS.config().getProperty("rpc.serverport");
 		if(port != null && port.length() > 0)
 			mServerSocket = new ServerSocket(Integer.parseInt(port));
 		else{
 			mServerSocket = new ServerSocket(0);
 		}
 		mServerSocket.setReuseAddress(true); // allow port number to be reused immediately after close of this socket
 		mServerSocket.setSoTimeout(500); // well, we have to wake up every once and a while to check for program termination
 		callbacks = new HashMap<String, RPCCallableMethod>();
 		ServerConnection newConnection = new ServerConnection(mServerSocket, callbacks);
 		connectionListener = new Thread(newConnection);
 		connectionListener.start();
 		//TODO: implement
 	}
 	
 	/**
 	 * System is shutting down imminently.  Do any cleanup required.
 	 */
 	public void shutdown() {
 		try {
 			mServerSocket.close();
 		} catch (IOException e) {}
 	}
 	
 	public Map<String, RPCCallableMethod> getHandlers() {
 		return Collections.unmodifiableMap(callbacks);
 	}
 	
 	/**
 	 * Services and applications with RPC callable methods register them with the RPC service using this routine.
 	 * Those methods are then invoked as callbacks when an remote RPC request for them arrives.
 	 * @param serviceName  The name of the service.
 	 * @param methodName  The external, well-known name of the service's method to call
 	 * @param method The descriptor allowing invocation of the Java method implementing the call
 	 * @throws Exception
 	 */
 	public synchronized void registerHandler(String serviceName, String methodName, RPCCallableMethod method) throws Exception {
 		//TODO: implement
 		callbacks.put(serviceName + methodName, method);
 	}
 	
 	/**
 	 * Returns the local IP address.
 	 * @return
 	 * @throws UnknownHostException
 	 */
 	public String localIP() throws UnknownHostException {
 		return InetAddress.getLocalHost().getHostAddress();
 	}
 
 	/**
 	 * Returns the port to which the RPC ServerSocket is bound.
 	 * @return
 	 */
 	public int localPort() {
 		return mServerSocket.getLocalPort();
 	}
 	
 	/**
 	 * Manages a single user's connection to the server / back end logic
 	 */
 	private class ServerConnection implements Runnable {
 
 		private ServerSocket connection;
 		
 		public ServerConnection(ServerSocket connection, Map<String, RPCCallableMethod> callbacks) {
 			this.connection = connection;
 		}
 		
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			while(!connection.isClosed()) {
 				try {
 					Socket newUser = connection.accept();
 					UserConnection thread = new UserConnection(newUser, callbacks);
 					thread.run();
 				} catch (IOException e) {}
 				
 			}
 		}
 
 	}
 	
 	private class UserConnection implements Runnable {
 
 		private Socket user;
 		private TCPMessageHandler handler;
 		private boolean listening;
 		private boolean handshook;
 		private int id;
 		
 		public UserConnection(Socket user, Map<String, RPCCallableMethod> callbacks) throws IOException {
 			System.out.println("connection made");
 			handler = new TCPMessageHandler(user);
 			listening = true;
 			handshook = false;
 			id = 1;
 			this.user = user;
 		}
 		
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			while(!user.isClosed()) {
 				try {
 					parseMessage(handler.readMessageAsJSONObject());
 				} catch (Exception e) {
 					e.printStackTrace();
 					System.out.println(e.getMessage());
 					break;
 				}
 			}
 		}
 		
 		public void parseMessage(JSONObject json) throws Exception{
 			if(!handshook){
 				try {
 					if(json.getString("action").equals("connect")){
 						JSONObject reply = new JSONObject();
 						reply.put("id", id);
 						reply.put("host", "");
 						reply.put("callid", json.getInt("id"));
 						reply.put("type", "OK");
 						try{
 							json.getString("connection").equals("keep-alive");
 							reply.put("connection", "keep-alive");
 						} catch (JSONException e) {}
 						handler.sendMessage(reply);
 						id++;
 						handshook = true;
 					}
 				} catch (JSONException e) {
 					//didn't contain the key "action"
 					JSONObject error = new JSONObject();
 					error.put("id", id);
 					error.put("host", "");
 					error.put("callid", json.getInt("id"));
 					error.put("type", "ERROR");
 					error.put("message", "handshake message malformed");
 					handler.sendMessage(error);
 					id++;
 				}
 			}else {
 				try {
 					if(json.getString("type").equals("invoke")) {
 						String key = "";
 						key += json.getString("app") + json.getString("method");
 						JSONObject reply = new JSONObject();
 						reply.put("value", callbacks.get(key).handleCall(json.getJSONObject("args")));
 						reply.put("id", id);
 						reply.put("host", "");
 						reply.put("callid", json.get("id"));
 						reply.put("type", "OK");
 						handler.sendMessage(reply);
 						id++;
 					}
 				} catch (JSONException e) {
 					JSONObject error = new JSONObject();
 					error.put("id", id);
 					error.put("host", "");
 					error.put("callid", json.getInt("id"));
 					error.put("type", "ERROR");
 					error.put("message", "message malformed");
 					JSONObject copy = new JSONObject();
 					JSONArray names = json.names();
 					for ( int i=0; i<names.length(); i++ ) {
 						String key = (String)names.getString(i);
 						copy.put(key, json.getString(key));
 					}
 					error.put("callargs", copy);
 					handler.sendMessage(error);
 					id++;
 				} catch (NullPointerException e) {
 					JSONObject error = new JSONObject();
 					error.put("id", id);
 					error.put("host", "");
 					error.put("callid", json.getInt("id"));
 					error.put("type", "ERROR");
 					error.put("message", "method not found");
 					JSONObject copy = new JSONObject();
 					JSONArray names = json.names();
 					for ( int i=0; i<names.length(); i++ ) {
 						String key = (String)names.getString(i);
 						copy.put(key, json.getString(key));
 					}
 					error.put("callargs", copy);
 					handler.sendMessage(error);
 					id++;
 				}
 			}
 		}
 	}
 }
 
