 package at.roadrunner.android.couchdb;
 
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import at.roadrunner.android.Config;
 import at.roadrunner.android.controller.TimeController;
 import at.roadrunner.android.model.Log;
 import at.roadrunner.android.model.Log.LogType;
 
 public class RequestWorker {
 	private final Context _context;
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "RequestWorker";
 
 	public RequestWorker(Context context) {
 		_context = context;
 	}
 
 	/*
 	 * save the log of an item
 	 */
 	public void saveLog(JSONArray itemIds, LogType logType, String value, JSONObject attachments)  {
 		JSONObject log = new JSONObject();
 		try {
 			log.put(Log.TYPE_KEY, Log.TYPE_VALUE);
 			log.put(Log.LOG_TYPE_KEY, logType.name());
 			log.put(Log.ITEMS_KEY, itemIds);
 			log.put(Log.TIMESTAMP_KEY, new TimeController().getTimestampForDatabase());
 			log.put(Log.VALUE_KEY, value);
 			if (attachments != null)  {
 				log.put(Log.ATTACHMENTS_KEY, attachments);
 			}
 			Log.addDoctrineMetadata(log);
 			
 			HttpPut put = RequestFactory.createHttpPut(getNextId());
 			StringEntity body = new StringEntity(log.toString());
 			put.setEntity(body);
 
 			new HttpExecutor().executeForResponse(put);
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * get local stored items
 	 */
 	public String getLoadedItems() throws CouchDBException {
 		String result = null;
 		@SuppressWarnings("unused")
 		String IPandPort;
 		String dbName;
 		@SuppressWarnings("unused")
 		String user;
 		@SuppressWarnings("unused")
 		String password;
 
 		// get the ip and name of the database
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(_context);
 		IPandPort = prefs.getString("ip", Config.ROADRUNNER_SERVER_IP + ":"
 				+ Config.ROADRUNNER_SERVER_PORT);
 		dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 		user = prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER);
 		password = prefs.getString("password",
 				Config.ROADRUNNER_AUTHENTICATION_PASSWORD);
 
 		HttpGet get = RequestFactory.createLocalHttpGet(dbName
 				+ "/_design/roadrunnermobile/_view/loaded?group=true");
 		result = new HttpExecutor().executeForResponse(get);
 
 		return result;
 	}
 
 	/*
 	 * get local stored items
 	 */
 	public String getReplicatedItems() throws CouchDBException {
 		String result = null;
 		@SuppressWarnings("unused")
 		String IPandPort;
 		String dbName;
 		@SuppressWarnings("unused")
 		String user;
 		@SuppressWarnings("unused")
 		String password;
 
 		// get the ip and name of the database
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(_context);
 		IPandPort = prefs.getString("ip", Config.ROADRUNNER_SERVER_IP + ":"
 				+ Config.ROADRUNNER_SERVER_PORT);
 		dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 		user = prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER);
 		password = prefs.getString("password",
 				Config.ROADRUNNER_AUTHENTICATION_PASSWORD);
 
 		HttpGet get = RequestFactory.createLocalHttpGet(dbName
 				+ "/_design/roadrunnermobile/_view/items");
 		result = new HttpExecutor().executeForResponse(get);
 
 		return result;
 	}
 
 	/*
 	 * replicate the DB with the server TODO: add use/password authentication
 	 */
 	public void replicateLogsToRemoteDatabase() {
 		JSONObject repl = new JSONObject();
 		try {
 			repl.put("source", Config.DATABASE);
 			repl.put("target", getAuthenticatedRemoteUrl());
 			repl.put("filter", "roadrunnermobile/logfilter");
 
 			HttpPost post = RequestFactory.createLocalHttpPost("_replicate");
 			StringEntity body = new StringEntity(repl.toString());
 			post.setEntity(body);
 
 			new HttpExecutor().executeForResponse(post);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * replicate the DB with the server
 	 */
 	public void replicateFromServer(JSONArray itemIdArray)
 			throws CouchDBException {
 
 		JSONObject repl = new JSONObject();
 		try {
 
 			JSONObject items = new JSONObject();
 			items.put("items", itemIdArray);
 
 			repl.put("source", getAuthenticatedRemoteUrl());
 			repl.put("target", Config.DATABASE);
 			repl.put("filter", "roadrunner/itemfilter");
 			repl.put("query_params", items);
 
 			HttpPost post = RequestFactory.createLocalHttpPost("_replicate");
 			StringEntity body = new StringEntity(repl.toString());
 			post.setEntity(body);
 
 			new HttpExecutor().executeForResponse(post);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized boolean replicateInitialDocuments() {
 		// create the list of initial documents to be replicated
 		JSONArray docIds = new JSONArray();
 		docIds.put("_design/roadrunnermobile");
 
 		JSONObject repl = new JSONObject();
 		try {
 			repl.put("source", getAuthenticatedRemoteUrl());
 			repl.put("target", Config.DATABASE);
 			repl.put("doc_ids", docIds);
 
 			HttpPost post = RequestFactory
 					.createLocalHttpPostAuth("_replicate");
 			StringEntity body = new StringEntity(repl.toString());
 			post.setEntity(body);
 
 			JSONObject response = new JSONObject(new HttpExecutor()
 					.executeForResponse(post));
 			android.util.Log.v("replicate design", response.toString());
 
 			return response.isNull("error");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 	
 	public boolean replicateSelectedContainer() {
 		String result = null;
 		JSONObject repl = new JSONObject();
 		SharedPreferences prefs = PreferenceManager
 			.getDefaultSharedPreferences(_context);
 		String containerId = prefs.getString("transportationId", Config.DEFAULT_TRANSPORTATION);
 		try {
 			repl.put("source", getAuthenticatedRemoteUrl());
 			repl.put("target", Config.DATABASE);
 			repl.put("doc_ids", new JSONArray().put(containerId));
 	
 			HttpPost post = RequestFactory.createLocalHttpPost("_replicate");
 			StringEntity body = new StringEntity(repl.toString());
 			post.setEntity(body);
 
 			result = new HttpExecutor().executeForResponse(post);
 			return new JSONObject(result).has("ok");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean isLocalDocumentExisting(String id) {
 		String result = null;
 		JSONObject response = null;
 		String dbName;
 	
 		// get name of database
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
 		dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 
 		HttpGet get = RequestFactory.createLocalHttpGet(dbName + "/" + id);
 		try {
 			result = new HttpExecutor().executeForResponse(get);
 			response = new JSONObject(result);
 			return (!response.has("error"));
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public String getContainerNames() throws CouchDBException {
 		String result = null;
 		String user;
 		String password;
 
 		// get the ip and name of the database
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(_context);
 		user = prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER);
 		password = prefs.getString("password",
 				Config.ROADRUNNER_AUTHENTICATION_PASSWORD);
 		
 		HttpGet get = RequestFactory.createRemoteDbHttpGet("roadrunner/_design/roadrunner/_view/containernames", user, password);
 		result = new HttpExecutor().executeForResponse(get);
 		return result;
 	}
 	
 	public boolean isAuthenticatedAtServer(String username, String password)  {
 		String result = null;
 		JSONObject response = null;
 		
 		HttpGet get = RequestFactory.createRemoteDbHttpGet("roadrunner", username, password);
 		try {
 			result = new HttpExecutor().executeForResponse(get);
 			response = new JSONObject(result);
 			return (!response.has("error"));
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public JSONArray getSensorsForSelectedContainer()  {
 		SharedPreferences prefs = PreferenceManager
 			.getDefaultSharedPreferences(_context);
 		String containerId = prefs.getString("transportationId", Config.DEFAULT_TRANSPORTATION);
 		try {
 			HttpGet get = RequestFactory.createLocalHttpGet(Config.DATABASE + "/" + containerId);
 			String contStr = new HttpExecutor().executeForResponse(get);
 			JSONObject container = new JSONObject(contStr);
 			return container.getJSONArray("sensors");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public JSONArray getDeliveries()  {
 		try {
 			HttpGet get = RequestFactory.createLocalHttpGet("roadrunner/_design/roadrunnermobile/_view/deliveries?include_docs=true");
 			String contStr = new HttpExecutor().executeForResponse(get);
 			return new JSONObject(contStr).getJSONArray("rows");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public long getServerTime() throws CouchDBException {
 		HttpGet get = RequestFactory.createRemoteTimeHttpGet();
 		String contStr = new HttpExecutor().executeForResponse(get);
		try {
			return Long.valueOf(contStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
 	}
 
 	private String getAuthenticatedRemoteUrl()  {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
 		String iPandPort = prefs.getString("ip", Config.ROADRUNNER_SERVER_IP + ":" + Config.ROADRUNNER_SERVER_PORT);
 		String dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 		String user = prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER);
 		String password = prefs.getString("password", Config.ROADRUNNER_AUTHENTICATION_PASSWORD);
 		
 		return new StringBuilder()
 			.append("http://")
 			.append(user)
 			.append(':')
 			.append(password)
 			.append('@')
 			.append(iPandPort)
 			.append('/')
 			.append(dbName)
 			.toString();
 	}
 		
 	/*
 	 * returns the next ID
 	 */
 	static public String getNextId() {
 		try {
 			HttpGet get = RequestFactory.createLocalHttpGet("_uuids");
 			JSONObject content = new JSONObject(new HttpExecutor()
 					.executeForResponse(get));
 			JSONArray array = new JSONArray(content.getString("uuids"));
 			return array.getString(0);
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (CouchDBException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
