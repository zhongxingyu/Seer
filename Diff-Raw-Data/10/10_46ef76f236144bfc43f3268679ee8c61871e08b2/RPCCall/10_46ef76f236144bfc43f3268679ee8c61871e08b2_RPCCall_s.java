 package edu.uw.cs.cse461.Net.RPC;
 
import java.io.BufferedReader;
 import java.io.IOException;
import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.TCPMessageHandler.TCPMessageHandler;
 import edu.uw.cs.cse461.util.ConfigManager;
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
 
 	//-------------------------------------------------------------------------------------------
 	//-------------------------------------------------------------------------------------------
 	// The static versions of invoke() is just a convenience for caller's -- it
 	// makes sure the RPCCall service is actually running, and then invokes the
 	// the code that actually implements invoke.
 	
 	/**
 	 * Invokes method() on serviceName located on remote host ip:port.
 	 * @param ip Remote host's ip address
 	 * @param port RPC service port on remote host
 	 * @param serviceName Name of service to be invoked
 	 * @param method Name of method of the service to invoke
 	 * @param userRequest Arguments to call
 	 * @return Returns whatever the remote method returns.
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public static JSONObject invoke(
 			String ip,				  // ip or dns name of remote host
 			int port,                 // port that RPC is listening on on the remote host
 			String serviceName,       // name of the remote service
 			String method,            // name of that service's method to invoke
 			JSONObject userRequest    // arguments to send to remote method
 			) throws JSONException, IOException {
 		RPCCall rpcCallObj =  (RPCCall)NetBase.theNetBase().getService( "rpccall" );
 		if ( rpcCallObj == null ) throw new IOException("RPCCall.invoke() called but the RPCCall service isn't loaded");
 		return rpcCallObj._invoke(ip, port, serviceName, method, userRequest, true);
 	}
 	//-------------------------------------------------------------------------------------------
 	//-------------------------------------------------------------------------------------------
 
 	
 	/**
 	 * The infrastructure requires a public constructor taking no arguments.  Plus, we need a constructor.
 	 */
 	public RPCCall() {
 		super("rpccall", true);
 	}
 
 	/**
 	 * This private method performs the actual invocation, including the management of persistent connections.
 	 * 
 	 * @param ip
 	 * @param port
 	 * @param serviceName
 	 * @param method
 	 * @param userRequest
 	 * @return
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	private JSONObject _invoke(
 			String ip,				  // ip or dns name of remote host
 			int port,                 // port that RPC is listening on on the remote host
 			String serviceName,       // name of the remote service
 			String method,            // name of that service's method to invoke
 			JSONObject userRequest,   // arguments to send to remote method
 			boolean tryAgain          // true if an invocation failure on a persistent connection should cause a re-try of the call, false to give up
 			) throws JSONException, IOException {
 		Socket socket = new Socket(ip, port);
 		ConfigManager config = NetBase.theNetBase().config();
 		int timeOut = 1000*config.getAsInt("rpc.timeout", 0, TAG); // convert seconds to milliseconds
 		if ( timeOut != 0 ) {
 			try {
 				socket.setSoTimeout(timeOut);
 			} catch (SocketException e) {
 				e.printStackTrace();
 				JSONObject socketError = new JSONObject();
 				socketError.put("msg", "An error occurred while setting the socket timeout");
 				return socketError;
 			}
 		}
 		
 		TCPMessageHandler handler = new TCPMessageHandler(socket);
 		JSONObject handshake = new JSONObject();
 		int callid = 5; // This choice is arbitrary TODO figure out a better id
 		handshake.put("id", callid); 
 		handshake.put("host", ip);
 		handshake.put("action", "connect");
 		handshake.put("type", "control");
 		
 		// If a persistent connection is possible, this is where code would be added 
 		// Extra credit TODO
 		
 		Log.d(TAG, "Sending handshake to service");
 		handler.sendMessage(handshake);
 		
 		Log.d(TAG, "Attempting to receive response to handshake");
 		JSONObject response = handler.readMessageAsJSONObject();
 		
 		
 		String result = response.getString("type");
 		// If an error occurs, we want to be informed of it and stop executing this message.
 		if (result.equals("ERROR")) {
 			JSONObject message = new JSONObject();
 			message.put("msg", "Error: " + response.getString("msg"));
 			return message;
 		} else if (result.equals("OK")) {
 			// Makes sure that the response we are receiving was actually for the handshake we sent
 			if (response.getInt("callid") != callid) {
 				JSONObject msg = new JSONObject();
 				msg.put("msg", "Error: server says " + response.getString("msg"));
 				return msg;
 			}
 			Log.d(TAG, "Handshake successful");
 		} else {
 			JSONObject msg = new JSONObject();
 			msg.put("msg", "Error: server is behaving oddly");
 			return msg;
 		}
 		
 		// If we have reached this point, we know that the handshake was successful, so it is safe to send the user's message.
 		JSONObject call = new JSONObject();
 		call.put("type", "invoke");
 		call.put("id", callid*2);
 		call.put("app", serviceName);
 		call.put("method", method);
 		call.put("host", ip);
 		call.put("args", userRequest);
 		
 		Log.d(TAG, "Attempting to send the call");
 		handler.sendMessage(call);
 		
 		Log.d(TAG, "Attempting to receive a response");
 		response = handler.readMessageAsJSONObject();
 		result = response.getString("type");
 		if (result.equals("ERROR")) {
 			JSONObject msg = new JSONObject();
 			msg.put("msg", "Error: " + response.getString("message"));
 			return msg;
 		} else if (result.equals("OK")) {
 			// Makes sure that the response we are receiving was actually for the call we sent
 			if (response.getInt("callid") != callid*2) {
 				JSONObject msg = new JSONObject();
 				msg.put("msg", "Error: server says " + response.getString("message"));
 				Log.d(TAG, response.toString());
 				return msg;
 			} else {
 				Log.d(TAG, response.toString());
 				return response.getJSONObject("value");
 			}
 		} else {
 			JSONObject msg = new JSONObject();
 			msg.put("msg", "Error: unexpected response from the server");
 			return msg;
 		}
 	}
 	
 	/**
 	 * Called when entire infrastructure is coming down.
 	 */
 	@Override
 	public void shutdown() {
 	}
 
 	/**
 	 * Called when some client wants a representation of this server's state.  
 	 * (Mainly useful for debugging.)
 	 */
 	@Override
 	public String dumpState() {
 		return "Current peristed connections are ...";
 	}
 
 }
