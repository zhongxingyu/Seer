 package edu.uw.cs.cse461.sp12.OS;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.sp12.OS.HTTPDService.HTTPProvider;
 import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;
 
 public class DDNSService extends RPCCallable implements HTTPProvider {
 
 	private RPCCallableMethod<DDNSService> register;
 	private RPCCallableMethod<DDNSService> unregister;
 	private RPCCallableMethod<DDNSService> resolve;
 	
 	private RPCCallerSocket root;
 	private Map<String, Node> nodes;
 	private String soaIp = ((RPCService)OS.getService("rpc")).localIP();
 	private int soaPort = ((RPCService)OS.getService("rpc")).localPort();
 	
 	public DDNSService() throws Exception {
 		//Read in config file for tree
 		//Fill out storage tree
 		
 		register = new RPCCallableMethod<DDNSService>(this, "_register");
 		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "register", register);
 		unregister = new RPCCallableMethod<DDNSService>(this, "_unregister");
 		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "unregister", unregister);
 		resolve = new RPCCallableMethod<DDNSService>(this, "_resolve");
 		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "resolve", resolve);
 		
 //		root = new RPCCallerSocket(OS.config().getProperty("ddns.rootserver"), 
 //				OS.config().getProperty("ddns.rootserver"), OS.config().getProperty("ddns.rootport"));
 		
 		
 		nodes = new TreeMap<String, Node>();
 		String[] namespace = OS.config().getProperty("ddns.namespace").split(",");
 		for(String s : namespace) {
 			String type = OS.config().getProperty(s);
 			nodes.put(s, new Node(s, type));
 //				if(type.equals("SOA")){
 //					Node soa = new Node(s, type);
 //					soa.ip = soaIp;
 //					soa.port = soaPort;
 //					nodes.put(s, soa);
 //			}
 		}
 		
 	}
 	
 	@Override
 	public String servicename() {
 		return "ddns";
 	}
 
 	@Override
 	public void shutdown() {
 
 	}
 	
 	public JSONObject _register(JSONObject args) throws JSONException, IOException {
 //		{port:34562, name:"jz.cse461.","password":"jzpassword","ip":"192.168.0.77"}
 		try {args.getString("name");}
 		catch (JSONException e) {return runtimeExep("", "no name supplied");}
 		JSONObject pw = checkPW(args);
 		JSONObject check = checkArgs(args);
 		if(check != null) return check;
 		else if(pw != null) return pw;
 		else{
 			Node location = search(args.getString("name"));
 			JSONObject result = new JSONObject();
			if(location.dirty)
				return noAddressExep(args.getString("name"));
 			if(location == null){
 				noNameExep(args.getString("name"));
 			}else if(location.type.equals("NS") || location.type.equals("CNAME")) {
 				if(location.dirty)
 					return noAddressExep(args.getString("name"));
 				result.put("done", false);
 			} else {
 				result.put("done", true);
 				result.put("lifetime", Integer.parseInt(OS.config().getProperty("ddns.cachettl")));
 				try{
 					location.register(args.getString("ip"), args.getInt("port"));
 				}catch(Exception e) {
 					return runtimeExep(args.getString("name"), "incorrect arguments");
 				}
 			}
 			result.put("node", location.toJSON());
 			result.put("resulttype", "registerresult");
 			return result;
 		}
 //		{node:[node representation described next], lifetime:600, resulttype:"registerresult", "done":true}
 	}
 	
 	public JSONObject _unregister(JSONObject args) throws JSONException, IOException {
 //		unregister() works exactly like register(), except for two things. First, it marks 
 //		the node as having no current address rather than updating its address. Second, 
 //		if done is true, no node is returned.
 		//Remove info from storage
 		try {args.getString("name");}
 		catch (JSONException e) {return runtimeExep("", "no name supplied");}
 		JSONObject pw = checkPW(args);
 		JSONObject check = checkArgs(args);
 		if(check != null)
 			return check;
 		else if(pw != null)
 			return pw;
 		else{
 			Node location = search(args.getString("name"));
 			JSONObject result = new JSONObject();
			if(location.dirty)
				return noAddressExep(args.getString("name"));
 			if(location == null){
 				noNameExep(args.getString("name"));
 			}else if(location.type.equals("NS") || location.type.equals("CNAME")) {
 				result.put("done", false);
 				result.put("node", location.toJSON());
 			} else {
 				result.put("done", true);
 				location.unregister();
 			}
 			result.put("resulttype", "unregisterresult");
 			return result;
 		}
 	}
 	
 	public JSONObject _resolve(JSONObject args) throws JSONException, IOException {
 		try {args.getString("name");}
 		catch (JSONException e) {return runtimeExep("", "no name supplied");}
 		JSONObject check = checkArgs(args);
 		if(check != null)
 			return check;
 		else{
 			Node location = search(args.getString("name"));
 			JSONObject result = new JSONObject();
			if(location.dirty)
				return noAddressExep(args.getString("name"));
 			if(location == null){
 				noNameExep(args.getString("name"));
 			}else if(location.type.equals("NS") || location.type.equals("CNAME")) {
 				result.put("done", false);
 			} else {
 				result.put("done", true);
 			}
 			result.put("node", location.toJSON());
 			result.put("resulttype", "resolveresult");
 			return result;
 		}
 	}
 
 	private JSONObject checkPW(JSONObject args) throws JSONException {
 		try {
 			if(!args.getString("password").equals(OS.config().getProperty("ddns.password")))
 				return authorizationExep(args.getString("name"));
 			return null;
 		} catch (JSONException e) {
 			return runtimeExep(args.getString("name"), "incorrect arguments");
 		}
 	}
 	
 	private JSONObject checkArgs(JSONObject args) throws JSONException {
 		try {
 			String hostname = new DDNSFullName(OS.config().getProperty("ddns.hostname")).toString();
 			String requestName = new DDNSFullName(args.getString("name")).toString();
 			if(!requestName.endsWith(hostname))
 				return zoneExep(args.getString("name"));
 			return null;
 		} catch (JSONException e) {
 			return runtimeExep(args.getString("name"), "incorrect arguments");
 		}
 	}
 	
 	private Node search(String name) {
 		String[] tokens = name.split("\\.");
 		String host = OS.config().getProperty("ddns.hostname");
 		int index = host.split("\\.").length;
 		
 		for(int i = tokens.length - 1 - index ; i >= 0 && i < Integer.parseInt(OS.config().getProperty("ddns.edgesteps")); i--){
 			host = tokens[i] + "." + host;
 			Node current = nodes.get(host);
 			if(current == null || current.type.equals("NS") || current.type.equals("CNAME")){
 				return current;	
 			}
 		}
 		return nodes.get(host);
 	}
 	
 	private JSONObject noAddressExep(String name) {
 		try{
 		JSONObject result = new JSONObject();
 		result.put("resulttype", "ddnsexception");
 		result.put("exceptionnum", 2);
 		result.put("name", name);
 		result.put("message", "no address to return");
 		return result;
 		}catch(Exception e) {return null;}
 	}
 	
 	private JSONObject authorizationExep(String name) {
 		try{
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "ddnsexception");
 			result.put("exceptionnum", 3);
 			result.put("name", name);
 			result.put("message", "password incorrect");
 			return result;
 			}catch(Exception e) {return null;}
 	}
 	
 	private JSONObject noNameExep(String name) {
 		try{
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "ddnsexception");
 			result.put("exceptionnum", 1);
 			result.put("name", name);
 			result.put("message", "this name (" + name + ") does not exist");
 			return result;
 			}catch(Exception e) {return null;}
 	}
 	
 	private JSONObject runtimeExep(String name, String message) {
 		try{
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "ddnsexception");
 			result.put("exceptionnum", 4);
 			result.put("name", name);
 			result.put("message", message);
 			return result;
 			}catch(Exception e) {return null;}
 	}
 	
 	private JSONObject expiredExep(String name) {
 		try{
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "ddnsexception");
 			result.put("exceptionnum", 5);
 			result.put("name", name);
 			result.put("message", "step limit exceeded before resolution");
 			return result;
 			}catch(Exception e) {return null;}
 	}
 	
 	private JSONObject zoneExep(String name) {
 		try{
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "ddnsexception");
 			result.put("exceptionnum", 6);
 			result.put("name", name);
 			result.put("message", "the name supplied (" + name + ") does not exist in this namespace");
 			return result;
 			}catch(Exception e) {return null;}
 	}
 	
 	private class Node {
 		
 		private String ip;
 		private int port;
 		private String name;
 		private String type;
 		private String alias;
 		private boolean dirty;
 		private Timer t;
 		
 		public Node(String name, String type){
 			this.name = name;
 			this.type = type;
 			dirty = true;
 			port = 0;
 			ip = "";
 			t = new Timer();
 		}
 		
 		public void register(String ip, int port) {
 			this.ip = ip;
 			this.port = port;
 			this.dirty = false;
 			t.cancel();
 			t = new Timer();
 			t.schedule(new unreg(), 1000 * Integer.parseInt(OS.config().getProperty("ddns.cachettl")));
 		}
 		
 		public class unreg extends TimerTask{
 			@Override
 			public void run() {
 				unregister();
 			}
 		}
 		
 		public void unregister() {
 			dirty = true;
 		}
 		
 		public JSONObject toJSON() throws JSONException {
 			JSONObject result = new JSONObject();
 			result.put("name", name);
 			result.put("type", type);
 			if(type.equals("CNAME"))
 				result.put("alias", alias);
 			else {
 				result.put("ip", ip);
 				result.put("port", port);
 			}
 			return result;
 		}
 		
 		public String toString(){
 			StringBuilder out = new StringBuilder();
 			if (!dirty) {
 				out.append("[Type: " + type);
 				if (type.equals("CNAME")) {
 					out.append(" Alias: " + alias);
 				} else {
 					out.append(" IP: " + ip);
 					out.append(" Port : " + port);
 				}
 				out.append("]");
 			} else {
 				out.append("[No current address]");
 			}
 			return out.toString();
 		}
 		
 	}
 	
 	@Override
 	public String toString(){
 		String zoneName = OS.config().getProperty("ddns.hostname");
 		int offset = zoneName.split("\\.").length;
 		StringBuilder out = new StringBuilder();
 		out.append("Zone: " + zoneName + "\n");
 		for(Entry<String, Node> n : nodes.entrySet()){
 			String name = n.getKey();
 			for(int i = 0; i < name.split("\\.").length - offset; i++){
 				out.append("    ");
 			}
 			out.append("'" + name + "'\t" + n.getValue().toString() + "\n");
 		}
 		return out.toString();
 	}
 	@Override
 	public String httpServe(String[] uriArray) {
 		return toString();//TODO: make prettier
 	}
 }
