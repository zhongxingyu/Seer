 package edu.uw.cs.cse461.Net.DDNS;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.HTTP.HTTPProviderInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.ARecord;
 import edu.uw.cs.cse461.Net.RPC.RPCCall;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.Log;
 
 
 public class DDNSResolverService extends NetLoadableService implements HTTPProviderInterface, DDNSResolverServiceInterface {
 	private static final String TAG="DDNSResolverService";
 
 	private ARecord rootRecord;
 	private ARecord hostRecord;
 	private final String ddnsPassword;
 	
 	/**
 	 * Called to end execution.  Specifically, need to terminate any threads we've created.
 	 */
 	@Override
 	public void shutdown() {
 		try {
 			this.unregister(new DDNSFullName(NetBase.theNetBase().hostname()));
 		} catch (DDNSException e) {
 			Log.e(TAG, e.getMessage());
 			e.printStackTrace();			
 		} catch (JSONException e) {
 			Log.e(TAG, "unregister failed. Maybe.");
 		}
 		super.shutdown();
 	}
 		
 	/**
 	 * Serves web pages.  The 0th element of uriArray is always null.
 	 * The next element names this service ("ddnsresolver").  The optional third
 	 * component is a name to be resolved before dumping the cache.
 	 */
 	@Override
 	public String httpServe(String[] uriArray) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Host:  ").append(NetBase.theNetBase().hostname()).append("\n");
 		if ( uriArray.length > 2 ) {
 			sb.append("Resolving: ").append(uriArray[2]).append("\n");
 			// third component
 			
 			//  resolve uriArray[2] to an address and append the address to the stringbuilder
 			// ....
 			sb.append("You haven't updated DDNSResolverServer.httpServer()");
 		}
 		// add any additional information you want here
 		return sb.toString();
 	}
 	
 	
 	public DDNSResolverService() throws DDNSException {
 		super("ddnsresolver", true);
 		ConfigManager config = NetBase.theNetBase().config();
 
 		String rootIp = config.getProperty("ddns.rootserver"); 
 		int rootPort = Integer.parseInt(config.getProperty("ddns.rootport")); 
 		this.rootRecord = new DDNSRRecord.SOARecord(rootIp, rootPort);
 
 		this.ddnsPassword = config.getProperty("ddnsresolver.password");
 		
 		String ip = NetBase.theNetBase().myIP();
 		String port = config.getProperty("rpc.serverport");
 		this.hostRecord = new DDNSRRecord.ARecord(ip, Integer.parseInt(port));
 		
 		DDNSFullNameInterface fullName = new DDNSFullName(NetBase.theNetBase().hostname()); 
 		register(fullName, this.hostRecord.port());
 	}
 
 	/**
 	 * Unregisters a name.  
 	 * @param name
 	 * @throws DDNSException
 	 */
 	@Override
 	public void unregister(DDNSFullNameInterface name) throws DDNSException, JSONException {
 		JSONObject args = new JSONObject()
 			.put("name", name.toString())
 			.put("password", ddnsPassword);
 		JSONObject response = invokeDDNSService("unregister", args);
 	}
 	
 	/**
 	 * Registers a name as being on this host (IP) at the given port.
 	 * If the name already exists, update its address mapping.  If it doesn't exist, create it (as an ARecord).
 	 * @param name
 	 * @param ip
 	 * @param port
 	 * @throws DDNSException
 	 */
 	@Override
 	public void register(DDNSFullNameInterface name, int port) throws DDNSException {
 		JSONObject args;
 		try {
 			args = new JSONObject()
 				.put("name", name.toString())
 				.put("ip", hostRecord.ip())
 				.put("port", port)
 				.put("password", ddnsPassword);
 			
 			JSONObject response = invokeDDNSService("register", args);
 			//JSONObject node = response.getJSONObject("node");
 			
 			int lifetime = response.getInt("lifetime");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Resolves a name to an ARecord containing an address.  Throws an exception if no ARecord w/ address can be found.
 	 * @param name
 	 * @return The ARecord for the name, if one is found
 	 * @throws DDNSException
 	 */
 	@Override
 	public ARecord resolve(String nameStr) throws DDNSException, JSONException {
 
 		// invoke the name resolver
 		JSONObject args = new JSONObject().put("name", nameStr);
 		JSONObject response = invokeDDNSService("resolve", args);
 
 		JSONObject node = response.getJSONObject("node");
 
 		ARecord resultRecord = null;
		String type = response.getString("type");
 		if (type.equals("A")) {
 			resultRecord = new DDNSRRecord.ARecord(node);
 		} else if (type.equals("SOA")) {
 			resultRecord = new DDNSRRecord.SOARecord(node);
 		} else {
 			// SOMETHING WENT WRONG
 		}
 		return resultRecord;
 	}
 	
 	
 	@Override
 	public String dumpState() {
 		return "whatever you want";
 	}
 
 	private JSONObject invokeDDNSService(String method, JSONObject args) throws DDNSException {
 		boolean done = false;
 		JSONObject response = null;
 		String targetIp = rootRecord.ip();
 		int targetPort = rootRecord.port();
 		
 		try {
 			do {
 				Log.d(TAG, "rpc "+method+" request with args "+args);
 				// invoke the name resolver
 				Log.d(TAG, "rpc "+method+" request with args "+args);
 				Log.d(TAG, "port: " + targetPort + " ip: " + targetIp);
 				
 				response = RPCCall.invoke(targetIp, targetPort, "ddns", method, args);
 	
 	            Log.d(TAG, "response payload of "+response);
 				// if the response is an exception, then throw the exception
 				if (response.getString("resulttype").equals("ddnsexception")) {
 					throw parseDDNSException(response);
 				}
 				done = response.getBoolean("done");
 	
 				if (!done) {
 					// parse the node representation
 					JSONObject node = response.getJSONObject("node");
 					String type = node.getString("type");
 					// continue with resolving
 					if (type.equals("NS")) {
 						// update the target ip/port
 						targetIp = node.getString("ip");
 						targetPort = node.getInt("port");
 					} else if (type.equals("CNAME")) {
 						// resolve the name alias
 						args.put("name", node.getString("alias"));
 					} else {
 						// SOMETHING WENT WRONG
 					}
 				}
 			} while (!done);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return response;
 	}
 
 	private DDNSException parseDDNSException(JSONObject response) throws JSONException{
 		String message = response.getString("message");
 		DDNSFullName name = new DDNSFullName(response.getString("name"));
 
 		Log.i(TAG, "Exception returned with message: " + message);
 
 		switch (response.getInt("exceptionnum")) {
 			case 1: Log.i(TAG, "Specified name "+name+" not found");
 					return new DDNSException.DDNSNoSuchNameException(name);
 			case 2: Log.i(TAG, "node "+name+" has no valid address");
 					return new DDNSException.DDNSNoAddressException(name);
 			case 3: Log.i(TAG, "Password invalid for DDNS registration/unregistration");
 					return new DDNSException.DDNSAuthorizationException(name);
 			case 4: Log.w(TAG, "DDNS Runtime failure");
 					return new DDNSException.DDNSRuntimeException(message);
 			case 5: Log.i(TAG, "registration timeout expired for node "+name);
 					return new DDNSException.DDNSTTLExpiredException(name);
 			case 6: DDNSFullName zone = new DDNSFullName(response.getString("zone"));
 					Log.i(TAG, "Specified name "+name+" not found in zone "+zone);
 					return new DDNSException.DDNSZoneException(name, zone);
 			default: return new DDNSException(message);
 		}
 	}
 
 }
