 package edu.uw.cs.cse461.Net.RPC;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.Net.Base.DataThreadInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.TCPMessageHandler.TCPMessageHandler;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.Log;
 
 public class RPCThread implements DataThreadInterface {
 	private static final String TAG = "RPCThread";
 	
 	private boolean _timeToClose = false;		//flag set when end() is called to signal the thread to shut down
 	private int _timeOut; //time in MS between checks to see if its time to shut down
 	private int portNum;
 	private ServerSocket socket;
 	private RPCService parent;
 	
 	public RPCThread(int portNum, RPCService parent) throws Exception {
 		// Eclipse doesn't support System.console()
 		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 		ConfigManager config = NetBase.theNetBase().config();
 		_timeOut = 1000*config.getAsInt("rpc.timeout", 0, TAG); // convert seconds to milliseconds
 		if ( _timeOut == 0 ) {
 			System.out.print("Enter the timeout in ms, or empty line to exit: ");
 			String targetTimeoutString = console.readLine();
 			if ( targetTimeoutString == null || targetTimeoutString.trim().isEmpty() ) return;
			else portNum = Integer.parseInt(targetTimeoutString);
 		}
 		this.portNum = portNum;
 		this.parent = parent;
 	}
 	@Override
 	public void run() {
 		try {
 			socket = new ServerSocket(portNum);
 			Log.d(TAG, "Server set up at port: " + portNum);
 		} catch (IOException e) {
 			Log.e(TAG, "Failure to create ServerSocket");
 			return;
 		}
 		//attempt to set the timeout between successive checks of _timeToClose flag
 		try {
 			socket.setSoTimeout(_timeOut);
 		} catch (SocketException e) {
 			e.printStackTrace();
 			return;
 		}
 		Log.d("RPCThread", "Server ready to accept clients");
 		//loop until time to close
 		boolean error = false;
 		boolean timeout = false;
 		Socket s = null;
 		int id = 1;
 		while(true){
 			//set up socket to accept.  Code hangs on .accept() until a connection is established or
 			// TIMEOUT ms have passed.
 			TCPMessageHandler handler = null;
 			try {
 				s = socket.accept();
 				handler = new TCPMessageHandler(s);
 				Log.d(TAG, "run: TCP connection established.");
 			} catch (SocketTimeoutException e) {
 				timeout = true;
 			} catch (IOException e) {
 				Log.e(TAG, "run: IOException on accept.");
 				error = true;
 			}
 			if (!error && !timeout) {
 				String host = s.getInetAddress().getHostAddress();
 				try {
 					Log.d(TAG, "reading from client");
 					JSONObject handshake = handler.readMessageAsJSONObject();
 					if (handshake.has("type")) {
 						if (handshake.getString("type").equals("control")) {
 							JSONObject successMsg = new JSONObject();
 							successMsg.put("id", id);
 							id++;
 							successMsg.put("host", host);
 							successMsg.put("callid", handshake.getInt("id"));
 							successMsg.put("type", "OK");
 							handler.sendMessage(successMsg);
 							Log.d(TAG, "received handshake");
 						} else {
 							JSONObject errorMsg = new JSONObject();
 							errorMsg.put("id", id);
 							id++;
 							errorMsg.put("host", host);
 							errorMsg.put("callid", -1);
 							errorMsg.put("type", "ERROR");
 							errorMsg.put("msg", "Error establishing connection");
 							handler.sendMessage(errorMsg);
 							Log.d(TAG, "received improper handshake");
 							break;
 						}
 					} else {
 						JSONObject errorMsg = new JSONObject();
 						errorMsg.put("id", id);
 						id++;
 						errorMsg.put("host", host);
 						errorMsg.put("callid", -1);
 						errorMsg.put("type", "ERROR");
 						errorMsg.put("msg", "Error establishing connection");
 						handler.sendMessage(errorMsg);
 						Log.d(TAG, "received potential handshake that was not formulated correctly");
 						break;
 					}
 					Log.d(TAG, "reading request from client");
 					JSONObject request = handler.readMessageAsJSONObject();
 					if (request.has("type")) {
 						if (request.getString("type").equals("invoke")) {
 							String app = request.getString("app");
 							String method = request.getString("method");
 							JSONObject args = request.getJSONObject("args");
 							JSONObject response = new JSONObject();
 							response.put("id", id);
 							id++;
 							response.put("host", host);
 							response.put("callid", request.getInt("id"));
 							try {
 								Log.d(TAG, "Evaluating request");
 								JSONObject result = parent.accessMethod(app, method, args);
 								if (result == null) {
 									response.put("type", "ERROR");
 									response.put("message", "That method/app does not exist");
 									response.put("callargs", request);
 								} else {
 									response.put("type", "OK");
 									response.put("value", result);
 								}
 							} catch (Exception e) {
 								response.put("type", "ERROR");
 								response.put("message", "The method called caused an exception");
 								response.put("callargs", request);
 							}
 							handler.sendMessage(response);
 						} else {
 							JSONObject errorMsg = new JSONObject();
 							errorMsg.put("id", id);
 							id++;
 							errorMsg.put("host", host);
 							errorMsg.put("callid", -1);
 							errorMsg.put("type", "ERROR");
 							errorMsg.put("message", "Error receiving request");
 							errorMsg.put("callargs", request);
 							handler.sendMessage(errorMsg);
 							Log.d(TAG, "received potential request that was not formulated correctly");
 						}						
 					} else {
 						JSONObject errorMsg = new JSONObject();
 						errorMsg.put("id", id);
 						id++;
 						errorMsg.put("host", host);
 						errorMsg.put("callid", -1);
 						errorMsg.put("type", "ERROR");
 						errorMsg.put("message", "Error receiving request");
 						errorMsg.put("callargs", request);
 						handler.sendMessage(errorMsg);
 						Log.d(TAG, "received potential request that was not formulated correctly");
 					}
 					
 				} catch (IOException e) {
 					Log.e(TAG, "run: IOException when reading from socket");
 				} catch (JSONException e) {
 					Log.e(TAG, "run: JSONException when reading from socket");
 				} 
 			}
 			if (!timeout) {
 				try {
 					Log.d(TAG, "run: Attempting to close connection");
 					s.close();
 					handler.discard();
 					handler = null;
 					s = null;
 				} catch (IOException e) {
 					Log.e(TAG, "failed to close socket to client");
 				}
 			}
 			
 			//check if time to close; close if it is
 			if(_timeToClose){
 				try {
 					socket.close();
 					Log.d(TAG, "closed server socket");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return;				
 			}
 			error = false;
 			timeout = false;
 		}
 		
 	}
 
 	/*************
 	 * Call this function from the parent process to end this server thread.
 	 * May take up to TIMEOUT ms until the thread checks this value and shuts down.
 	 */
 	public void end() {
 		_timeToClose = true;
 	}
 
 }
