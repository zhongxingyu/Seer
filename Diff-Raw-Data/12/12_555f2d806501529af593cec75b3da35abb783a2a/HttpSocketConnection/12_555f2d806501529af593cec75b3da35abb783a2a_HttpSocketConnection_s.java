 package net.djmacgyver.bgt.socket;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.session.Session;
 import net.djmacgyver.bgt.user.User;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources.NotFoundException;
 import android.location.Location;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.codebutler.android_websockets.WebSocketClient;
 
 public class HttpSocketConnection {
 	public static final int STATE_DISCONNECTED = 0;
 	public static final int STATE_CONNECTING = 1;
 	public static final int STATE_CONNECTED = 2;
 	public static final int STATE_DISCONNECTING = 3;
 	
 	private Context context;
 	private WebSocketClient socket;
 	private int requestCount = 0;
 	private HashMap <Integer, SocketCommand> requests = new HashMap<Integer, SocketCommand>();
 	private LinkedList<SocketCommand> queue;
 	private ArrayList<HttpSocketListener> listeners = new ArrayList<HttpSocketListener>();
 	private ArrayList<String> subscribed = new ArrayList<String>();
 	
 	private int state = STATE_DISCONNECTED;
 	
 	public HttpSocketConnection(Context applicationContext) {
 		this.context = applicationContext;
 	}
 	
 	private WebSocketClient getSocket()
 	{
 		if (socket == null) {
 			try {
 				URI url = new URI(context.getResources().getString(R.string.websocket));
 				socket = new WebSocketClient(url, new WebSocketClient.Handler() {
 					private boolean valid = true;
 					
 					@Override
 					public void onMessage(byte[] data) {
 						if (!valid) return;
 						System.out.println("OMG! Binary data!");
 					}
 					
 					@Override
 					public void onMessage(String message) {
 						if (!valid) return;
 						if (message.length() == 0) return;
 						try {
 							JSONObject response = new JSONObject(message);
 							if (response.has("requestId")) {
 								Integer id = response.getInt("requestId");
 								if (requests.containsKey(id)) {
 									((SocketCommand) requests.get(id)).updateResult(response);
 									requests.remove(id);
 								} else {
 									Log.e("SocketConnection", "received response for unknown command id: " + id);
 								}
 							} else if (response.has("event") && response.getString("event").equals("update")) {
 								if (response.has("data")) {
 									sendUpdate(response.getJSONObject("data"));
 								} else {
 									System.out.println("received message without data!");
 								}
 							} else if (response.has("command")) {
 								JSONObject data = response.has("data") ? response.getJSONObject("data") : new JSONObject();
 								sendCommand(response.getString("command"), data);
 							}
 						} catch (JSONException e) {
 							// propably an old XML message. ignore... for now we only support json
 							System.out.println("unable to parse message: " + message);
 						}
 						checkDisconnect();
 					}
 					
 					@Override
 					public void onError(Exception error) {
 						if (!valid) return;
 												
 						System.out.println("Error on websocket connection!");
 						error.printStackTrace();
 						
 						// try disconnecting the current socket (if not already disconnected)
 						if (queue == null) try {
 							// if the disconnect succeeds it should call onDisconnect()
 							socket.disconnect();
 						} catch (Exception e) {}
 						// we can never be sure whether onDisconnect() is called, so to be sure we call it at least 
 						// once here.
 						// onDisconnect() is protected against double execution using valid
 						onDisconnect(-1, error.getMessage());
 					}
 					
 					private void reconnect(){
 						System.out.println("reconnect()");
 
 						// put up a new queue
 						// this also prevents new commands from being sent
 						if (queue == null) queue = new LinkedList<SocketCommand>();
 						
 						// send state notification
 						setState(STATE_CONNECTING);
 						
 						// give the server 10s grace time
 						try {
 							Thread.sleep(10000);
 						} catch (InterruptedException e) {
 							System.out.println("interrupted!");
 						}
 						
 						// we might have changed our mind while we were sleeping...
 						if (doDisconnect) {
 							doDisconnect = false;
 							return;
 						}
 						
 						// if we get to here, we really want that connection to be back up
 						getSocket().connect();
 					}
 					
 					@Override
 					public void onDisconnect(int code, String reason) {
 						if (!valid) return;
 						
 						// invalidate this handler 
 						valid = false;
 						
 						// send state updates
 						setState(STATE_DISCONNECTED);
 						socket = null;
 						
 						// we got disconnected.
 						System.out.println("disconnected");
 
 						// cancel all outstanding requests (they won't receive data on a closed socket anyway)
 						cancelRequests();
 						
 						// the disconnection was intentional - ok. accept it and leave it that way.
 						if (doDisconnect) {
 							doDisconnect = false;
 							return;
 						}
 						
 						// otherwise: try to reconnect
 						reconnect();
 					}
 					
 					@Override
 					public void onConnect() {
 						if (!valid) return;
 						System.out.println("connected");
 						
 						synchronized (queue) {
 							// get the current queue
 							LinkedList<SocketCommand> q = queue;
 							queue = null;
 							
 							// first things first: send handshake & authentication.
 							sendHandshake();
 							authenticate();
 							
 							// send all queued commands
 							while (!q.isEmpty()) sendCommand(q.poll());
 						}
 						
 						sendSubscriptions();
 						
 						setState(STATE_CONNECTED);
 						
 						// check whether this connection is eligible for disconnection
 						checkDisconnect();
 					}
 
 				}, null);
 				WebSocketClient.setTrustManagers(new TrustManager[]{
 						new X509TrustManager() {
 							@Override
 							public X509Certificate[] getAcceptedIssuers() {
 								// TODO Auto-generated method stub
 								return null;
 							}
 							
 							@Override
 							public void checkServerTrusted(X509Certificate[] chain, String authType)
 									throws CertificateException {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void checkClientTrusted(X509Certificate[] chain, String authType)
 									throws CertificateException {
 								// TODO Auto-generated method stub
 								
 							}
 						}
 				});
 			} catch (NotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return socket;
 	}
 	
 	protected void cancelRequests() {
 		// outstanding requests will not be answered; update them as false
 		Iterator <SocketCommand> i = requests.values().iterator();
 		while (i.hasNext()) {
 			SocketCommand c = i.next();
 			c.updateResult(false);
 		}
 		requests.clear();
 	}
 
 	public SocketCommand authenticate() {
 		Session.setUser(null);
 		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
 		if (p.getBoolean("anonymous", true)) return null;
 		try {
 			// send our authentication data as soon as we are connected
 			JSONObject data = new JSONObject();
 			data.put("user", p.getString("username", ""));
 			data.put("pass", p.getString("password", ""));
 			final SocketCommand command = new SocketCommand("auth", data);
 			command.addCallback(new Runnable() {
 				@Override
 				public void run() {
 					if (command.wasSuccessful()) {
 						try {
 							Session.setUser(new User(command.getResponseData().getJSONObject(0)));
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			});
			return sendCommand(command);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private void sendHandshake() {
 		try {
 			JSONObject json = new JSONObject();
 			JSONObject handshake = new JSONObject();
 			handshake.put("platform", "android");
 			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
 			handshake.put("version", info.versionName);
 			json.put("handshake", handshake);
 			getSocket().send(json.toString());
 		} catch (JSONException e) {
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public SocketCommand sendCommand(String command) {
 		return sendCommand(new SocketCommand(command));
 	}
 	
	public SocketCommand sendCommand(SocketCommand command, boolean doQueue) {
		if (queue != null) {
 			if (!doQueue) return command;
 			synchronized (queue) {
 				queue.add(command);
 				return command;
 			}
 		}
 		requests.put(requestCount, command);
 		command.setRequestId(requestCount++);
 		getSocket().send(command.getJson());
 		return command;
 	}
 	
 	public SocketCommand sendCommand(SocketCommand command) {
 		return sendCommand(command, true);
 	}
 
 	public void connect() {
 		setState(STATE_CONNECTING);
 		if (queue != null) return;
 		queue = new LinkedList<SocketCommand>();
 		getSocket().connect();
 	}
 
 	public void disconnect() {
 		setState(STATE_DISCONNECTING);
 		doDisconnect = true;
 		checkDisconnect();
 	}
 	
 	private boolean doDisconnect = false;
 	
 	private void checkDisconnect() {
 		if (socket == null | !doDisconnect || queue != null || !requests.isEmpty()) return;
 		try {
 			getSocket().disconnect();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public SocketCommand sendLocation(Location location) {
 		try {
 			// build a json object to send to the server
 			JSONObject data = new JSONObject();
 			data.put("lat", location.getLatitude());
 			data.put("lon", location.getLongitude());
 			if (location.hasSpeed()) data.put("speed", location.getSpeed());
 			// send it
 			return sendCommand(new SocketCommand("log", data));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void sendQuit() {
 		// it is possible that the connection has been closed before we were able so send the "quit" message.
 		try {
 			sendCommand("quit");
 		} catch (NullPointerException e) {}
 	}
 	
 	public HttpSocketConnection subscribeUpdates(String[] categories) {
 		try {
 			JSONArray cats = new JSONArray();
 			int count = 0;
 			for (int i = 0; i < categories.length; i++) {
 				if (subscribed.contains(categories[i])) continue;
 				cats.put(count++, categories[i]);
 				subscribed.add(categories[i]);
 			}
 			if (count == 0) return this;
 			JSONObject data = new JSONObject();
 			data.put("category", cats);
 			sendCommand(new SocketCommand("subscribeUpdates", data), false);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return this;
 	}
 
 	public HttpSocketConnection subscribeUpdates(String category) {
 		return subscribeUpdates(new String[]{category});
 	}
 	
 	public HttpSocketConnection unSubscribeUpdates(String[] categories) {
 		try {
 			JSONArray cats = new JSONArray();
 			int count = 0;
 			for (int i = 0; i < categories.length; i++) {
 				if (!subscribed.contains(categories[i])) continue;
 				cats.put(count++, categories[i]);
 				subscribed.remove(categories[i]);
 			}
 			if (count == 0) return this;
 			JSONObject data = new JSONObject();
 			data.put("category", cats);
 			sendCommand(new SocketCommand("unSubscribeUpdates", data), false);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return this;
 	}
 	
 	public HttpSocketConnection unSubscribeUpdates(String category) {
 		return unSubscribeUpdates(new String[]{category});
 	}
 	
 	protected void sendUpdate(JSONObject update) {
 		synchronized (listeners) {
 			Iterator<HttpSocketListener> i = listeners.iterator();
 			while (i.hasNext()) i.next().receiveUpdate(update);
 		}
 	}
 	
 	private void sendCommand(String command, JSONObject data) {
 		synchronized (listeners) {
 			Iterator<HttpSocketListener> i = listeners.iterator();
 			while (i.hasNext()) i.next().receiveCommand(command, data);
 		}
 	}
 	
 	protected void fireStateChange(int newState) {
 		synchronized (listeners) {
 			Iterator<HttpSocketListener> i = listeners.iterator();
 			while (i.hasNext()) i.next().receiveStateChange(newState);
 		}
 	}
 	
 	public void addListener(HttpSocketListener listener)
 	{
 		synchronized (listeners) {
 			if (listeners.contains(listener)) return;
 			listeners.add(listener);
 		}
 	}
 	
 	public void removeListener(HttpSocketListener listener)
 	{
 		synchronized (listeners) {
 			if (!listeners.contains(listener)) return;
 			listeners.remove(listener);
 		}
 	}
 
 	private void sendSubscriptions() {
 		// re-subscribe to all categories (in case of a re-connect)
 		try {
 			Iterator<String> i = subscribed.iterator();
 			JSONArray cats = new JSONArray();
 			int count = 0;
 			while (i.hasNext()) {
 				String cat = i.next();
 				cats.put(count++, cat);
 			}
 			if (count == 0) return;
 			JSONObject data = new JSONObject();
 			data.put("category", cats);
 			sendCommand(new SocketCommand("subscribeUpdates", data));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void sendGpsUnavailable() {
 		sendCommand("gpsUnavailable");
 	}
 	
 	private void setState(int newState)	{
 		state = newState;
 		fireStateChange(newState);
 	}
 	
 	public int getState() {
 		return state;
 	}
 }
