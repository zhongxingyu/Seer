 package nl.q42.javahueapi;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.Type;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.q42.javahueapi.Networker.Result;
 import nl.q42.javahueapi.models.BridgeError;
 import nl.q42.javahueapi.models.FullConfig;
 import nl.q42.javahueapi.models.Light;
 import nl.q42.javahueapi.models.NupnpEntry;
 import nl.q42.javahueapi.models.SimpleConfig;
 import nl.q42.javahueapi.models.UserCreateRequest;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 // TODO: JSON string escaping
 @SuppressWarnings("serial")
 public class HueService implements Serializable {
 	
 	private String bridgeIp;
 	private String username;
 	
 	private static Gson gson = new Gson();
 	
 	public HueService(String ip, String username) {
 		this.bridgeIp = ip;
 		this.username = username;
 	}
 	
 	public static List<String> getBridgeIps() throws IOException, ApiException {
 		//Result result = Networker.get("http://www.meethue.com/api/nupnp");
 		Result result = Networker.get("http://connected-lamps-dev.appspot.com/api/nupnp");
 		
 		if (result.getResponseCode() == 200) {
 			Type collectionType = new TypeToken<List<NupnpEntry>>(){}.getType();
 			List<NupnpEntry> entries = gson.fromJson(result.getBody(), collectionType);
 			
 			List<String> ips = new ArrayList<String>();
 			for (NupnpEntry entry : entries) {
 				ips.add(entry.internalipaddress);
 			}
 			return ips;
 		}
 		throw new ApiException(result);
 	}
 	
 	public static SimpleConfig getSimpleConfig(String ip) throws IOException, ApiException {
 		Result result = Networker.get("http://" + ip + "/api/config");
 		
 		if (result.getResponseCode() == 200) {
 			return gson.fromJson(result.getBody(), SimpleConfig.class);
 		}
 		throw new ApiException(result);
 	}
 	
 	/**
 	 * Returns true if the given username is an existing user on the given bridge, false if it could not be concluded.
 	 */
 	public static boolean userExists(String ip, String username) throws IOException {
 		if (username.length() < 10) return false;
 		
 		Result result = Networker.get("http://" + ip + "/api/" + URLEncoder.encode(username, "utf-8"));
 		
 		if (result.getResponseCode() == 200) {
 			try {
 				gson.fromJson(result.getBody(), FullConfig.class);
 			} catch (Exception e) {
 				return false;
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Returns true if the user was created and false if the link button was not pressed.
 	 */
 	public static boolean createUser(String ip, String devicetype, String username) throws IOException, ApiException {
 		Result result = Networker.post("http://" + ip + "/api", gson.toJson(new UserCreateRequest(devicetype, username)));
 		
 		if (result.getResponseCode() == 200) {
 			Type collectionType = new TypeToken<List<Map<String, BridgeError>>>(){}.getType();
 			List<Map<String, BridgeError>> error = gson.fromJson(result.getBody(), collectionType);
 			
 			if (error.get(0).containsKey("success")) {
 				return true;
 			} else if (error.get(0).get("error").type == 101) {
 				return false;
 			}
 		}
 		throw new ApiException(result);
 	}
 	
 	
 	public FullConfig getFullConfig() throws IOException, ApiException {
 		Result result = Networker.get("http://" + bridgeIp + "/api/" + username + "/");
 		
 		if (result.getResponseCode() == 200) {
 			return gson.fromJson(result.getBody(), FullConfig.class);
 		}
 		throw new ApiException(result); 
 	}
 	
 	public Map<String, Light> getLights() throws IOException, ApiException {
 		Result result = Networker.get("http://" + bridgeIp + "/api/" + username + "/lights");
 		
 		if (result.getResponseCode() == 200) {
 			Type collectionType = new TypeToken<Map<String, Light>>(){}.getType();
 			return gson.fromJson(result.getBody(), collectionType);
 		}
 		throw new ApiException(result);
 	}
 	
 	public void turnLightOn(String id, boolean on) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/lights/" + id + "/state",
 				"{\"on\":" + on + "}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setLightName(String id, String name) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/lights/" + id,
 				"{\"name\":\"" + name.replace("\"", "\\\"") + "\"}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setLightXY(String id, float[] xy, int bri) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/lights/" + id + "/state",
 				"{\"on\":true,\"xy\":[" + xy[0] + "," + xy[1] + "],\"bri\":" + bri + "}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setLightCT(String id, int ct, int bri) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/lights/" + id + "/state",
 				"{\"on\":true,\"ct\":" + ct + ",\"bri\":" + bri + "}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setGroupXY(String id, float[] xy, int bri) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/groups/" + id + "/action",
 				"{\"on\":true,\"xy\":[" + xy[0] + "," + xy[1] + "],\"bri\":" + bri + "}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setGroupCT(String id, int ct, int bri) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/groups/" + id + "/action",
 				"{\"on\":true,\"ct\":" + ct + ",\"bri\":" + bri + "}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setGroupName(String id, String name) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/groups/" + id,
 				"{\"name\":\"" + name + "\"}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void setGroupLights(String id, ArrayList<String> lights) throws IOException, ApiException {
 		String lightsStr = "";
 		for (int i = 0; i < lights.size(); i++) {
 			lightsStr += "\"" + lights.get(i) + "\"";
 			if (i < lights.size() - 1) lightsStr += ",";
 		}
 		
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/groups/" + id,
 				"{\"lights\":[" + lightsStr + "]}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void turnGroupOn(String id, boolean on) throws IOException, ApiException {
 		Result result = Networker.put("http://" + bridgeIp + "/api/" + username + "/groups/" + id + "/action",
 				"{\"on\":" + on + "}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public void removeGroup(String id) throws IOException, ApiException {
 		Result result = Networker.delete("http://" + bridgeIp + "/api/" + username + "/groups/" + id);
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 	}
 	
 	public int createGroup(String name, List<String> lights) throws IOException, ApiException {
 		String lightsStr = "";
 		for (int i = 0; i < lights.size(); i++) {
 			lightsStr += "\"" + lights.get(i) + "\"";
 			if (i < lights.size() - 1) lightsStr += ",";
 		}
 		
 		Result result = Networker.post("http://" + bridgeIp + "/api/" + username + "/groups",
 				"{\"name\":\"" + name.replace("\"", "\\\"") + "\",\"lights\":[" + lightsStr + "]}");
 		if (result.getResponseCode() != 200)
 			throw new ApiException(result);
 		
		if (result.getBody().contains("error")) {
			throw new ApiException(result);
		}
		
 		// Parse new group id
 		Matcher m = Pattern.compile("[0-9]+").matcher(result.getBody());
 		m.find();
 		return Integer.valueOf(m.group());
 	}
 	
 	public void setBridgeIp(String bridgeIp) {
 		this.bridgeIp = bridgeIp;
 	}
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	
 	public static class ApiException extends Exception {
 		public ApiException(Result result) {
 			super("Error " + result.getResponseCode() + ": " + result.getBody());
 		}
 	}
 }
