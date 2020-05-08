 package tjenkinson.asteriskLiveComsLib.program;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import tjenkinson.asteriskLiveComsLib.program.events.AsteriskLiveComsEventListener;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.AccessAlreadyGrantedException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.AlreadyConnectedException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.ChannelIDInvalidException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.ChannelNotVerifiedException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.NotConnectedException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.RequestJSONInvalidException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.RequiresMoreChannelsException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.UnableToConnectException;
 import tjenkinson.asteriskLiveComsLib.program.exceptions.UnknownException;
 import douglascrockford.json.JSONArray;
 import douglascrockford.json.JSONException;
 import douglascrockford.json.JSONObject;
 
 public class LiveComsServer {
 		
 	private SocketManager socketManager;
 
 	
 	public LiveComsServer(String ip, int port) {
 		socketManager = new SocketManager(ip, port);
 	}
 	
 	public void connect() throws UnableToConnectException, AlreadyConnectedException {
 		socketManager.connect();
 	}
 	
 	public void disconnect() throws NotConnectedException {
 		socketManager.disconnect();
 	}
 	
 	public boolean isConnected() {
 		return socketManager.isConnected();
 	}
 	
 	public void addEventListener(AsteriskLiveComsEventListener a) {
 		socketManager.addEventListener(a);
 	}
 	
 	public void removeEventListener(AsteriskLiveComsEventListener a) {
 		socketManager.removeEventListener(a);
 	}
 	
 	public ArrayList<Hashtable<String,Object>> getChannels() throws JSONException, NotConnectedException, UnknownException {
 		JSONObject request = new JSONObject();
 		request.put("action", "getChannels");
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 		ArrayList<Hashtable<String,Object>> channels = new ArrayList<Hashtable<String,Object>>();
 		JSONArray channelsJSON = response.getJSONArray("payload");
 		for(int i=0; i<channelsJSON.length(); i++) {
 			Hashtable<String,Object> row = new Hashtable<String,Object>();
 			JSONObject rowJSON = channelsJSON.getJSONObject(i);
 			Object[] keys = rowJSON.keySet().toArray();
 			for(int j=0; j<keys.length; j++) {
 				row.put((String) keys[j], rowJSON.get((String) keys[j]));
 			}
 			channels.add(row);
 		}
 		return channels;
 	}
 	
 	public void grantAccess(int id, boolean enableHoldMusic) throws JSONException, NotConnectedException, UnknownException, RequestJSONInvalidException, ChannelIDInvalidException, AccessAlreadyGrantedException {
 		JSONObject request = new JSONObject();
 		request.put("action", "grantAccess");
 		request.put("id", id);
 		request.put("enableHoldMusic", enableHoldMusic);
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") == 101) {
 			throw (new RequestJSONInvalidException());
 		}
 		else if (response.getInt("code") == 102) {
 			throw (new ChannelIDInvalidException());
 		}
 		else if (response.getInt("code") == 103) {
 			throw (new AccessAlreadyGrantedException());
 		}
 		else if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 	}
 	
 	public void denyAccess(int id) throws JSONException, NotConnectedException, RequestJSONInvalidException, ChannelIDInvalidException, UnknownException {
 		JSONObject request = new JSONObject();
 		request.put("action", "denyAccess");
 		request.put("id", id);
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") == 101) {
 			throw (new RequestJSONInvalidException());
 		}
 		else if (response.getInt("code") == 102) {
 			throw (new ChannelIDInvalidException());
 		}
 		else if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 	}
 	
 	public void routeChannels(int[] ids) throws JSONException, NotConnectedException, RequestJSONInvalidException, ChannelIDInvalidException, ChannelNotVerifiedException, RequiresMoreChannelsException, UnknownException {
 		JSONArray channels = new JSONArray();
 		for(int i=0; i<ids.length; i++) {
			channels.put(ids[i]);
 		}
 		JSONObject request = new JSONObject();
 		request.put("action", "routeChannels");
 		request.put("channels", channels);
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") == 101) {
 			throw (new RequestJSONInvalidException());
 		}
 		else if (response.getInt("code") == 102) {
 			throw (new ChannelIDInvalidException());
 		}
 		else if (response.getInt("code") == 103) {
 			throw (new ChannelNotVerifiedException());
 		}
 		else if (response.getInt("code") == 104) {
			throw (new RequestJSONInvalidException());
		}
		else if (response.getInt("code") == 105) {
 			throw (new RequiresMoreChannelsException());
 		}
 		else if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 	}
 	
	public void sendToHolding(int id) throws JSONException, NotConnectedException, RequestJSONInvalidException, ChannelIDInvalidException, ChannelNotVerifiedException, UnknownException {
 		JSONObject request = new JSONObject();
 		request.put("action", "sendToHolding");
		request.put("id", id);
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") == 101) {
 			throw (new RequestJSONInvalidException());
 		}
 		else if (response.getInt("code") == 102) {
 			throw (new ChannelIDInvalidException());
 		}
 		else if (response.getInt("code") == 103) {
 			throw (new ChannelNotVerifiedException());
 		}
 		else if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 	}
 	
 	public void reset() throws JSONException, NotConnectedException, UnknownException {
 		JSONObject request = new JSONObject();
 		request.put("action", "reset");
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 	}
 	
 	public boolean isConnectedToAsterisk() throws JSONException, NotConnectedException, UnknownException {
 		JSONObject request = new JSONObject();
 		request.put("action", "isConnectedToAsterisk");
 		JSONObject response = socketManager.sendRequest(request);
 		if (response.getInt("code") != 0) {
 			throw(new UnknownException());
 		}
 		return response.getBoolean("payload");
 	}
 }
