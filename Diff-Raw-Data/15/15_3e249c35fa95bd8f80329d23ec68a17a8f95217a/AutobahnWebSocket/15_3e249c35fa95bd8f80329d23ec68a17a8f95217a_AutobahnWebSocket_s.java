 package net.socian.phonegap.wsautobahn;
 
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.PluginResult;
 import org.apache.cordova.PluginResult.Status;
 
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 
 import de.tavendo.autobahn.WebSocket;
 import de.tavendo.autobahn.WebSocketConnection;
 import de.tavendo.autobahn.WebSocketException;
 
 public class AutobahnWebSocket extends CordovaPlugin implements
		WebSocket.ConnectionHandler, Runnable {
 
 	public static final String ACTION_CONNECT = "connect";
 	public static final String ACTION_ONOPEN = "onopen";
 	public static final String ACTION_ONMESSAGE = "onmessage";
 	public static final String ACTION_ONERROR = "onerror";
 
 	public static final String ACTION_SEND = "send";
 
 	private CallbackContext onopenConnextCallback;
 	private CallbackContext onmessageConnextCallback;
 	private CallbackContext onerrorConnextCallback;
 
 	private WebSocket wsConnection;
 	private String wsHost;
 
 	@Override
 	public boolean execute(String action, JSONArray args,
 			CallbackContext callbackContext) throws JSONException {
 		try {
 			if (ACTION_CONNECT.equals(action)) {
 				return actionConnect(args, callbackContext);
 			} else if (ACTION_ONOPEN.equals(action)) {
 				return actionOnOpen(args, callbackContext);
 			} else if (ACTION_ONMESSAGE.equals(action)) {
 				return actionOnMessage(args, callbackContext);
 			} else if (ACTION_ONERROR.equals(action)) {
 				return actionOnError(args, callbackContext);
 			} else if (ACTION_SEND.equals(action)) {
 				return actionSend(args, callbackContext);
 			}
 			return false;
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			if (onerrorConnextCallback != null)
 				onerrorConnextCallback.error(e.getMessage());
 			return false;
 		}
 	}
 
	public void run() {
 		try {
 			wsConnection = new WebSocketConnection();
 			wsConnection.connect(wsHost, this);
 
 		} catch (WebSocketException e) {
 			System.err.println("Exception: " + e.getMessage());
 			if (onerrorConnextCallback != null)
 				onerrorConnextCallback.error(e.getMessage());
 		}
	}

	private boolean actionConnect(JSONArray args, CallbackContext callbackContext) throws JSONException {
		JSONObject argObject = args.getJSONObject(0);
		wsHost = argObject.getString("wshost");
		cordova.getThreadPool().execute(this);
 		return true;
 	}
 
 	private boolean actionOnOpen(JSONArray args, CallbackContext callbackContext) {
 		onopenConnextCallback = callbackContext;
 		return true;
 	}
 
 	private boolean actionOnMessage(JSONArray args, CallbackContext callbackContext) {
 		onmessageConnextCallback = callbackContext;
 		return true;
 	}
 
 	private boolean actionOnError(JSONArray args, CallbackContext callbackContext) {
 		onerrorConnextCallback = callbackContext;
 		return true;
 	}
 
 	private boolean actionSend(JSONArray args, CallbackContext callbackContext)
 			throws JSONException {
 		JSONObject argObject = args.getJSONObject(0);
 		String message = argObject.getString("wsmessage");
 		wsConnection.sendTextMessage(message);
 		return true;
 	}
 
 	/**
 	 * Fired when the WebSockets connection has been established. After this
 	 * happened, messages may be sent.
 	 */
 	public void onOpen() {
 		if (onopenConnextCallback != null)
 			onopenConnextCallback.success();
 	}
 
 	/**
 	 * Fired when the WebSockets connection has deceased (or could not
 	 * established in the first place).
 	 * 
 	 * @param code
 	 *            Close code.
 	 * @param reason
 	 *            Close reason (human-readable).
 	 */
 	public void onClose(int code, String reason) {
 		if (onerrorConnextCallback != null)
 			onerrorConnextCallback.error("Closed: " + code + " because of "
 					+ reason);
 	}
 
 	/**
 	 * Fired when a text message has been received (and text messages are not
 	 * set to be received raw).
 	 * 
 	 * @param payload
 	 *            Text message payload or null (empty payload).
 	 */
 	public void onTextMessage(String payload) {
 		if (onmessageConnextCallback != null) {
 			PluginResult pr = new PluginResult(Status.OK, payload);
 			pr.setKeepCallback(true);
 			onmessageConnextCallback.sendPluginResult(pr);
 		}
 	}
 
 	/**
 	 * Fired when a text message has been received (and text messages are set to
 	 * be received raw).
 	 * 
 	 * @param payload
 	 *            Text message payload as raw UTF-8 or null (empty payload).
 	 */
 	public void onRawTextMessage(byte[] payload) {
 	}
 
 	/**
 	 * Fired when a binary message has been received.
 	 * 
 	 * @param payload
 	 *            Binar message payload or null (empty payload).
 	 */
 	public void onBinaryMessage(byte[] payload) {
 	}
 }
