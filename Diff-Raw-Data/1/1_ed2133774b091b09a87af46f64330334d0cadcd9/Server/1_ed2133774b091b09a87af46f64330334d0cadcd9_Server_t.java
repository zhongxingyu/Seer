 package uk.ac.aber.dcs.cs221.monstermash.data.remote;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Server {
 
 	private volatile String httpAddress;
 	private volatile String serverName;
 	
 	
 	public Server readJSON(JSONObject json) throws JSONException {
 		serverName = "Group"+json.getInt("serverNumber");
 		httpAddress = json.getString("httpRoot");
 		return this;		
 	}
 
 	public synchronized String getName() {
 		return serverName;
 	}
 	
 	public synchronized String getAddress() {
 		return httpAddress;
 	}
 	
 	
 	public synchronized RemoteUser lookup(long uid) {
		return null;
 		// TODO: implement this
 	}
 
 }
