 package edu.uw.cs.cse461.net.rpc;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.net.base.NetBase;
 import edu.uw.cs.cse461.net.base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.net.rpc.RPCMessage.RPCCallMessage;
 import edu.uw.cs.cse461.net.rpc.RPCMessage.RPCCallMessage.RPCControlMessage;
 import edu.uw.cs.cse461.net.rpc.RPCMessage.RPCCallMessage.RPCInvokeMessage;
 import edu.uw.cs.cse461.net.rpc.RPCMessage.RPCResponseMessage;
 import edu.uw.cs.cse461.net.rpc.RPCMessage.RPCResponseMessage.RPCErrorResponseMessage;
 import edu.uw.cs.cse461.net.rpc.RPCMessage.RPCResponseMessage.RPCNormalResponseMessage;
 import edu.uw.cs.cse461.net.tcpmessagehandler.TCPMessageHandler;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.IPFinder;
 import edu.uw.cs.cse461.util.Log;
 
 /**
  * Implements the side of RPC that receives remote invocation requests.
  * 
  * @author zahorjan
  *
  */
 public class RPCService extends NetLoadableService implements Runnable, RPCServiceInterface {
 	private static final String TAG="RPCService";
 	
 	private final HashMap<ServiceMethodTuple, RPCCallableMethod> callbacks = new HashMap<ServiceMethodTuple, RPCCallableMethod>();
 	private ServerSocket mServerSocket;
 	private int localPort;
 	
 	private class ServiceMethodTuple {
 		private String method;
 		private String service;
 		
 		public ServiceMethodTuple(String method, String service) { 
 			this.method = method;
 			this.service = service;
 		}
 		
 		public String getMethod() {
 			return method;
 		}
 		
 		public String getService() {
 			return service;
 		}
 		
 		@Override
 		public boolean equals(Object other) {
 			if (other instanceof ServiceMethodTuple) {
 				ServiceMethodTuple o = (ServiceMethodTuple) other;
 				return o.service.equals(this.service) && o.method.equals(this.method); 
 			}
 			return false;
 		}
		
		@Override

	    public int hashCode() {
			return method.hashCode()+service.hashCode();
	    }
 	}
 	
	private class SocketThread implements Runnable {
 		private Socket sock;
 		
 		public SocketThread(Socket sock) {
 			this.sock = sock;
 		}
 		@Override
 		public void run() {
 			// should really spawn a thread here, but the code is already complicated enough that we don't bother
 			try { 
				System.out.println("entered");
 				TCPMessageHandler tcpMsgHandler = new TCPMessageHandler(sock);
 				JSONObject request = tcpMsgHandler.readMessageAsJSONObject();
 				String type = request.getString("type");
 				if (!type.equals("control")) {
 					throw new IOException("The type was not of type control");
 				}
 				int callid = request.getInt("id");
 				JSONObject options = null;
 				boolean persist = false;
 				
 				if (request.has("options")) {
 					options = request.getJSONObject("options");
 					persist =  options.has("connection") && options.getString("connection").equals("keep-alive");
 				}
 				
 				RPCMessage msg = null;
 				
 				if (persist) {
 					msg = new RPCNormalResponseMessage(callid, options);
 				} else {
 					msg = new RPCResponseMessage(callid);
 				}
 				tcpMsgHandler.sendMessage(msg.marshall());
				System.out.println("help me");
 				
 				while (true) { 
 			
 					JSONObject invocation = tcpMsgHandler.readMessageAsJSONObject();
 					String invocationType = invocation.getString("type");
 					if (!invocationType.equals("invoke")) {
 						throw new IOException("The type was not of type invoke");
 					}
 					String serviceName = invocation.getString("app");
 					int invocationCallId = invocation.getInt("id");
 					String methodName = invocation.getString("method");
 					JSONObject args = invocation.getJSONObject("args");
 					
 					RPCCallableMethod method = getRegistrationFor(serviceName, methodName);
 					JSONObject value = method.handleCall(args);
 					RPCNormalResponseMessage reply = new RPCNormalResponseMessage(invocationCallId, value);
 					tcpMsgHandler.sendMessage(reply.marshall());
 					
 					if (!persist) {
 						break;
 					}
 				}
 								
 			} catch (Exception e) {
 
 			} finally {
 				try {
 					sock.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 	
 		}	
 	
 	}
 	
 	/**
 	 * Constructor.  Creates the Java ServerSocket and binds it to a port.
 	 * If the config file specifies an rpc.server.port value, it should be bound to that port.
 	 * Otherwise, you should specify port 0, meaning the operating system should choose a currently unused port.
 	 * <p>
 	 * Once the port is created, a thread needs to be spun up to listen for connections on it.
 	 * 
 	 * @throws Exception
 	 */
 	public RPCService() throws Exception {
 		super("rpc");
 		String serverIP = IPFinder.localIP();
 		ConfigManager config = NetBase.theNetBase().config();
 		localPort = config.getAsInt("rpc.server.port", 0);
 		mServerSocket = new ServerSocket();
 		mServerSocket.bind(new InetSocketAddress(serverIP, localPort));
 		mServerSocket.setSoTimeout( NetBase.theNetBase().config().getAsInt("net.timeout.granularity", 500));
		System.out.println("Constructor");
 
		Thread rpcThread = new Thread(this);
		rpcThread.start();
		
		/*Thread rpcThread = new Thread() {
 			public void run() {
 				run();
 			}
 		};
		rpcThread.start();*/
 	}
 	
 	
 	/**
 	 * Executed by an RPCService-created thread.  Sits in loop waiting for
 	 * connections, then creates an RPCCalleeSocket to handle each one.
 	 */
 	@Override
 	public void run() {
		System.out.println("RPC Service Run");
 		while ( !mAmShutdown ) {
 			Socket sock = null;
 			try {
 				sock = mServerSocket.accept();  // if this fails, we want out of the while loop...
 				// should really spawn a thread here, but the code is already complicated enough that we don't bother
 				Thread sockThread = new Thread(new SocketThread(sock));
 				
 				sockThread.start();
 		
 				
 
 			} catch (SocketTimeoutException e) {
 				// this is normal.  Just loop back and see if we're terminating.
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
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
 		ServiceMethodTuple methodServiceTuple = new ServiceMethodTuple(serviceName, methodName);
 		callbacks.put(methodServiceTuple, method);
 	}
 	
 	/**
 	 * Some of the testing code needs to retrieve the current registration for a particular service and method,
 	 * so this interface is required.  You probably won't find a use for it in your code, though.
 	 * 
 	 * @param serviceName  The service name
 	 * @param methodName The method name
 	 * @return The existing registration for that method of that service, or null if no registration exists.
 	 */
 	public RPCCallableMethod getRegistrationFor( String serviceName, String methodName) {
 		return callbacks.get(new ServiceMethodTuple(serviceName, methodName));
 	}
 	
 	/**
 	 * Returns the port to which the RPC ServerSocket is bound.
 	 * @return The RPC service's port number on this node
 	 */
 	@Override
 	public int localPort() {
 		return localPort;
 	}
 	
 	@Override
 	public String dumpState() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("\nListening on: ");
 		if ( mServerSocket != null ) sb.append(mServerSocket.toString());
 		sb.append("\n");
 		return sb.toString();
 	}
 }
