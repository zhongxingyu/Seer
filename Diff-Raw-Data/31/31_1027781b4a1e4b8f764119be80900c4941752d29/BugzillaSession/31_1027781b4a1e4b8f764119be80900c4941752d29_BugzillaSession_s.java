 package hudson.plugins.bugzilla;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
 
 public class BugzillaSession {
 	String url;
 	String username;
 	String password;
 	
 	// should only access from execute()/initClient()
 	transient XmlRpcClient client;
 	
 	public BugzillaSession(String u) throws XmlRpcException, MalformedURLException {
 		this(u, null, null);
 	}
 
 	public BugzillaSession(String u, String username, String password) throws XmlRpcException, MalformedURLException {
		try {
			this.url = u;
			this.username = username;
			this.password = password;
			initClient();
			HashMap<String, Object> result = this.execute("Bugzilla.version", null);
			String version = (String)result.get("version");
			LOGGER.log(Level.INFO, "Bugzilla server version is " + version);
		} catch (XmlRpcException e) {
			LOGGER.log(Level.WARNING, "XmlRpcException starting bugzilla session: " + e.getMessage());
			throw e;
		}
 	}
 	
 	private void initClient() throws MalformedURLException {
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 		URL urlobj = new URL(this.url + "/xmlrpc.cgi");
 		if(urlobj.getHost() == null) throw new MalformedURLException();
 		config.setServerURL(urlobj);
         client = new XmlRpcClient();
         XmlRpcCommonsTransportFactory transportFactory = new XmlRpcCommonsTransportFactory(client);
         // set the HttpClient so that we retain cookies
         transportFactory.setHttpClient(new HttpClient());
         client.setTransportFactory(transportFactory);
         client.setConfig(config);
 	}
 	
 	public boolean login() {
         if(username == null || password == null) {
         	LOGGER.fine("Username or password missing, assuming not necessary");
         	return true;
         }
         try {
         	HashMap<String, Object> args = new HashMap<String, Object>();
         	args.put("login", username);
         	args.put("password", password);
 			HashMap<String, Object> result = this.execute("User.login", args);
 			if(result.containsKey("id")) {
 				LOGGER.fine("Successful bugzilla login - ID " + result.get("id"));
 				return true;
 			} else {
 				LOGGER.log(Level.WARNING, "No 'id' after XMLRPC Bugzilla login");
 				return false;
 			}
 		} catch (XmlRpcException e) {
 			LOGGER.log(Level.WARNING, "XMLRPC cannot log in to bugzilla: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public HashMap<Integer, String> getBugSummaryMap(HashSet<Integer> bugIds) {
 		if(bugIds.size() == 0) return null;
 		HashMap<Integer, String> ret = new HashMap<Integer, String>();
         try {
 	        ArrayList<Integer> argIds = new ArrayList<Integer>(bugIds);
 	        HashMap<String, Object> params = new HashMap<String, Object>();
 	        params.put("ids", argIds);
 	        HashMap<String, Object> result = this.execute("Bug.get_bugs", params);
 	        Object[] bugs = (Object[])result.get("bugs");
 	        LOGGER.fine("get_bugs result has " + bugs.length);
 	        for(Object bug : bugs) {
 	        	HashMap<String, Object> bugMap = (HashMap<String, Object>)bug;
 	        	Integer id = (Integer)bugMap.get("id");
 	        	String summary = (String)bugMap.get("summary");
 	        	ret.put(id, summary);
 	        }
 		} catch (XmlRpcException e) {
 			LOGGER.log(Level.WARNING, "XMLRPC problem getting bug summaries: " + e.getMessage());
 			return null;
 		} 
 		return ret;
 	}
 
 	private HashMap<String, Object> execute(String method, HashMap<String, Object> args) throws XmlRpcException {
 		if(client == null) {
 			try {
 				initClient();
 			} catch (MalformedURLException e) {
 				// should be impossible
 				throw new Error(e);
 			}
 		}
 		boolean exit = false;
 		HashMap<String, Object> ret = null;
 		while(ret == null) {
 			try {
 				ret = (HashMap<String, Object>)client.execute(
 						method, 
 						args == null ? new Object[0] : new Object[]{args}
 				);
 			} catch (XmlRpcException e) {
 				if(exit || e.code != LOGIN_REQUIRED) {
 					LOGGER.log(Level.FINE, "XmlRpcException for \""+method+"\" error code '"+e.code+"'", e);
 					throw e;
 				}
 				LOGGER.log(Level.FINE, "Login required for \""+method+"\".  Attempting.");
 				exit = true;
 				login();
 			}
 		}
 		return ret;
 	}
     private static final Logger LOGGER = Logger.getLogger(BugzillaSession.class.getName());
     private static final int LOGIN_REQUIRED = 410;
 
 	public String getUrl() {
 		return url;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 }
