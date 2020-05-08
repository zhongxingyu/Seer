 package edu.uw.cs.cse461.sp12.OS;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class DDNSService extends RPCCallable{
 	public static final String[] ddnsHostNames = {};
 	private static final int TTL = 60;
 	
 	// A variable capable of describing a method that can be invoked by RPC.
 	private RPCCallableMethod<DDNSService> ddns;
 	private Map<String, DDNSRRecord> ddnsMap;
 	private Map<String, String> ddnsHostAndPassword;
 	private String hostName;
 	private DDNSRRecord ddnsRecordType;
 	
     @Override
     public String servicename() {
         return "ddns";
     }
 
     @Override
     public void shutdown() {
 		// nothing to do, but have to implement to fulfill the interface promise
     }
     
     public DDNSService() throws Exception{
     	// Set up the method descriptor variable to refer to this->_register()
 		ddns = new RPCCallableMethod<DDNSService>(this, "_register");
 		// Register the method with the RPC service as externally invocable method "register"
 		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "register", ddns );
 		
 		int serverport = Integer.parseInt(OS.config().getProperty("rpc.serverport"));
     	this.hostName = OS.config().getProperty("host.name");
 		String ip =	IPFinder.getInstance().getIp();
 		
     	((DDNSResolverService)OS.getService("ddnsresolver")).register(new DDNSFullName(this.hostName), serverport);
     	ddnsMap = new HashMap<String, DDNSRRecord>();
     	ddnsHostAndPassword = new HashMap<String, String>();
     	
     	Properties config= new Properties();
 		config.load(new FileInputStream("ddnsservice.config.ini"));
 		String ddnsRecordType = config.getProperty("ddnsrecordtype");
 		this.ddnsRecordType = new DDNSRRecord(ddnsRecordType, hostName, ip, serverport);
     	setupddns();
     }
     
     public JSONObject _register(JSONObject args) throws JSONException, IOException {
     	try {
     		String name = args.getString("name");
     		String password = args.getString("password");
     		String ip = args.getString("ip");
     		int port = args.getInt("port");
     		
     		if (!ddnsMap.containsKey(name))
     			return exceptionMsg(new DDNSNoSuchNameException(), name);
     		
     		if (!ddnsHostAndPassword.get(name).equals(password))
     			return exceptionMsg(new DDNSAuthorizationException(), name);
     		
     		DDNSRRecord temp = ddnsMap.get(name);
     		temp.schedule(TTL, ip, port);
     		
     		return successMsg(temp, "registerresult");
     	} catch (JSONException e) {
     		return exceptionMsg(new DDNSRuntimeException(), "");
     	// If the value of a filed is not following the protocol
     	} catch (Exception e) {
     		return exceptionMsg(new DDNSRuntimeException(), "");
     	}
     }
     
     public JSONObject _resolve(JSONObject args) throws JSONException, IOException {
     	String name = args.getString("name");
    	if (this.ddnsRecordType.getName().equals(name)))
    		
     	return null;
     }
     
     private JSONObject successMsg(DDNSRRecord record, String resulttype) {
     	JSONObject successMsg = new JSONObject();
 		try {
 			successMsg.put("node", record.toJSON());
 			successMsg.put("lifetime",TTL);
 			successMsg.put("resulttype", resulttype);
 			if (record.getDDNSRecordType().equals("NS"))
 				successMsg.put("done", false);
 			else
 				successMsg.put("done", true);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
     	return successMsg;
     }
     
     private JSONObject exceptionMsg(DDNSException exception, String name) {
     	JSONObject exceptionMsg = new JSONObject();
 		try {
 			exceptionMsg.put("resulttype", exception.resulttype);
 			exceptionMsg.put("exceptionnum", exception.exceptionnum);
 			exceptionMsg.put("message", exception.message+name);
 			if (!(exception instanceof DDNSRuntimeException))
 				exceptionMsg.put("name", name);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
     	return exceptionMsg;
     }
     
     private void setupddns() {
     	try {
 			
 			if (ddnsRecordType.equals("A") || ddnsRecordType.equals("SOA") ||
 					ddnsRecordType.equals("CNAME"))
 				throw new Exception();
 			
 			if (ddnsRecordType.equals("SOA")) {
 				for (String hostName: ddnsHostNames) {
 					String ddnsRecordType = config.getProperty(hostName+"recordtype");
 					ddnsMap.put(hostName, new DDNSRRecord(ddnsRecordType, hostName));
 					String hostNamePassword = config.getProperty(hostName+"password");
 					ddnsHostAndPassword.put(hostName, hostNamePassword);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("failed to load the config file");
 		} catch (IOException e) {
 			System.out.println("error occured during reading the config file");
 		} catch (NullPointerException e) {
 			System.out.println("A ddnsHostName is not in the config file");
 		} catch (Exception e) {
 			System.out.println("error occured in the ddns config file");
 		}
     }
 }
