 package mobi.monaca.framework.plugin;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 
 import mobi.monaca.framework.util.NetworkUtils;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.PluginResult.Status;
 import org.java_websocket.WebSocket;
 import org.java_websocket.handshake.ClientHandshake;
 import org.java_websocket.server.WebSocketServer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class WebSocketPlugin extends CordovaPlugin{
 	WebSocketServer server;
 	int port;
 	CallbackContext callbackContext;
 	HashMap<String, WebSocket> sockets = new HashMap<String, WebSocket>();
 	@Override
 	public boolean execute(String action, JSONArray args,
 			CallbackContext callbackContext) throws JSONException {
 		
 		if(action.equalsIgnoreCase("start")){
 			if(server == null){
 				JSONObject params = args.getJSONObject(0);
 				this.port = params.getInt("port");
 				createServer(port);
 				server.start();
 			}
 			JSONObject result = createAddressJSON();
 			result.put("event", "server:started");
 			PluginResult pluginResult = new PluginResult(Status.OK, result);
 			pluginResult.setKeepCallback(true);
 			callbackContext.sendPluginResult(pluginResult);
 			this.callbackContext = callbackContext;
 			return true;
 		}
 		
 		if(action.equalsIgnoreCase("getAddress")){
 			if(server == null){
 				callbackContext.error("You need to start server first");
 			}else{
 				JSONObject result = createAddressJSON();
 				callbackContext.success(result);
 			}
 			return true;
 		}
 		
 		if(action.equalsIgnoreCase("getClients")){
 			if(server == null){
 				callbackContext.error("You need to start server first");
 			}else{
 				JSONArray message = new JSONArray(sockets.keySet());
 				callbackContext.success(message);
 			}
 			return true;
 		}
 		
 		if(action.equalsIgnoreCase("send")){
 			if(server == null){
 				callbackContext.error("You need to start server before sending a message");
 			}else{
 				JSONObject params = args.getJSONObject(0);
 				String clientId = params.getString("clientId");
 				String message = params.getString("message");
 				if(sockets.containsKey(clientId)){
 					WebSocket webSocket = sockets.get(clientId);
 					webSocket.send(message);
 					callbackContext.success();
 				}else{
 					callbackContext.error("client " + clientId + " is not yet connected");
 				}
 			}
 			return true;
 		}
 		
 		if(action.equalsIgnoreCase("stop")){
 			if(server != null){
 				try {
					server.stop();
 					callbackContext.success("stopped server");
 				} catch (Exception e) {
 					e.printStackTrace();
 					callbackContext.error(e.getMessage());
 				}
 			}else{
 				callbackContext.error("server not yet started");
 			}
 			
 			return true;
 		}
 		return super.execute(action, args, callbackContext);
 	}
 
 	private JSONObject createAddressJSON() throws JSONException {
 		JSONObject result = new JSONObject();
 		result.put("ip", NetworkUtils.getIPAddress(true));
 		result.put("port", port);
 		return result;
 	}
 	
 	@Override
 	public void onDestroy() {
 		if(server != null){
 			try {
				server.stop();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		super.onDestroy();
 	}
 	
 	private void createServer(int port){
 		server = new WebSocketServer(new InetSocketAddress(port)){
 
 			@Override
 			public void onClose(WebSocket webSocket, int arg1, String msg,
 					boolean arg3) {
 				String clientId = getClientId(webSocket);
 				sockets.remove(clientId);
 				try {
 					JSONObject message = createJSONMessage("close", clientId);
 					message.put("message", msg);
 					PluginResult pluginResult = new PluginResult(Status.OK, message);
 					pluginResult.setKeepCallback(true);
 					WebSocketPlugin.this.callbackContext.sendPluginResult(pluginResult);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onError(WebSocket webSocket, Exception msg) {
 				String clientId = getClientId(webSocket);
 				try {
 					JSONObject message = createJSONMessage("error", clientId);
 					message.put("message", msg);
 					PluginResult pluginResult = new PluginResult(Status.OK, message);
 					pluginResult.setKeepCallback(true);
 					WebSocketPlugin.this.callbackContext.sendPluginResult(pluginResult);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onMessage(WebSocket webSocket, String msg) {
 				String clientId = getClientId(webSocket);
 				try {
 					JSONObject message = createJSONMessage("message", clientId);
 					message.put("message", msg);
 					PluginResult pluginResult = new PluginResult(Status.OK, message);
 					pluginResult.setKeepCallback(true);
 					WebSocketPlugin.this.callbackContext.sendPluginResult(pluginResult);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
 				String clientId = getClientId(webSocket);
 				sockets.put(clientId, webSocket);
 				
 				try {
 					JSONObject message = createJSONMessage("open", clientId);
 					PluginResult pluginResult = new PluginResult(Status.OK, message);
 					pluginResult.setKeepCallback(true);
 					WebSocketPlugin.this.callbackContext.sendPluginResult(pluginResult);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			private String getClientId(WebSocket webSocket) {
 				if(webSocket != null){
 					String clientId = webSocket.getRemoteSocketAddress().toString();
 					return clientId;
 				}
 				return null;
 			}
 		};
 	}
 	
 	private JSONObject createJSONMessage(String event, String clientId)
 			throws JSONException {
 			JSONObject message = new JSONObject();
 			message.put("event", event);
 			message.put("client", clientId);
 			return message;
 	}
 }
