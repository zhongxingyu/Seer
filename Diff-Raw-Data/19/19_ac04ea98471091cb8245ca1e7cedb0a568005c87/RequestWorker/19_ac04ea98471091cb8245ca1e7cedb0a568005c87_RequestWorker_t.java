 package at.roadrunner.android.couchdb;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
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
 import at.roadrunner.android.couchdb.CouchDBException.CouchDBNotReachableException;
 import at.roadrunner.android.model.Log;
 import at.roadrunner.android.model.Log.LogType;
 
 public class RequestWorker {
 	private Context m_context;
 	
 	public RequestWorker(Context context) {
 		m_context = context;
 	}
 	
 	/**
 	 * Logs a Message of Type logType 
 	 * 
	 * @param logType the Type of Log Message
	 * @param msgKey the key of the log entry
 	 * @param logMsg the Log Message
 	 */
	public void log(LogType logType, String msgKey, String logMsg) {
 		
 		JSONObject log = new JSONObject();
 		try {
 			log.put(Log.TYPE_KEY, Log.TYPE_VALUE);
 			log.put(Log.LOG_TYPE_KEY, logType.name());
			log.put(Log.ERROR, logMsg);
 			log.put(Log.TIMESTAMP_KEY, new Date().getTime());
 			
 			HttpPut put = RequestFactory.createHttpPut(getNextId());
 	        StringEntity body = new StringEntity(log.toString());
 	        put.setEntity(body);
 			
 	        HttpExecutor.getInstance().executeForResponse(put);
 		} catch (CouchDBNotReachableException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	/*
 	 * save the log of an item
 	 */
 	public void saveLog(String itemId, LogType logType)  {
 		
 		JSONObject log = new JSONObject();
 		try {
 			log.put(Log.TYPE_KEY, Log.TYPE_VALUE);
 			log.put(Log.LOG_TYPE_KEY, logType.name());
 			log.put(Log.ITEM_KEY, itemId);
 			log.put(Log.TIMESTAMP_KEY, new Date().getTime());
 			
 			HttpPut put = RequestFactory.createHttpPut(getNextId());
 	        StringEntity body = new StringEntity(log.toString());
 	        put.setEntity(body);
 			
 	        HttpExecutor.getInstance().executeForResponse(put);
 		} catch (CouchDBNotReachableException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	/*
 	 * get local stored items
 	 * TODO: FIX Return Statement maybe?
 	 * TODO: use user authentiaction maybe?
 	 */
 	public String getLocalItems() {
 		String result = null;
 		@SuppressWarnings("unused")
 		String IPandPort;
 		String dbName;
 		@SuppressWarnings("unused")
 		String user;
 		@SuppressWarnings("unused")
 		String password;
 		
 		// get the ip and name of the database
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
 		IPandPort = prefs.getString("ip", Config.ROADRUNNER_SERVER_IP + ":" + Config.ROADRUNNER_SERVER_PORT);
 		dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 		user = prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER);
 		password = prefs.getString("password", Config.ROADRUNNER_AUTHENTICATION_PASSWORD);
 		
 		try {
 			HttpGet get = RequestFactory.createHttpGet(dbName + "/_design/roadrunnermobile/_view/loaded");
 			result = HttpExecutor.getInstance().executeForResponse(get);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (CouchDBNotReachableException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	/*
 	 * replicate the DB with the server
 	 * TODO: add use/password authentication
 	 */
 	public void replicate() {
 		String IPandPort;
 		String dbName;
 		@SuppressWarnings("unused")
 		String user;
 		@SuppressWarnings("unused")
 		String password;
 		
 		// get the ip and name of the database
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
 		IPandPort = prefs.getString("ip", Config.ROADRUNNER_SERVER_IP + ":" + Config.ROADRUNNER_SERVER_PORT);
 		dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 		user = prefs.getString("user", Config.ROADRUNNER_AUTHENTICATION_USER);
 		password = prefs.getString("password", Config.ROADRUNNER_AUTHENTICATION_PASSWORD);
 
 		JSONObject repl = new JSONObject();
 		try {
 			//repl.put("source", Config.DATABASE);
 			repl.put("source", dbName);
 			repl.put("target", "http://" + IPandPort + "/" + dbName);
 			repl.put("filter", "roadrunnermobile/logfilter");
 			
 			HttpPost post = RequestFactory.createLocalHttpPost("_replicate");
 			StringEntity body = new StringEntity(repl.toString());
 			post.setEntity(body);
 			
 	        String result = HttpExecutor.getInstance().executeForResponse(post);
 	        result.toString();
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (CouchDBNotReachableException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * replicate the DB with the server
 	 */
 	public void replicateFromServer(JSONArray itemIdArray) {
 		String IPandPort;
 		String dbName;
 		
 		// get the ip and name of the database
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
 		IPandPort = prefs.getString("ip", Config.ROADRUNNER_SERVER_IP + ":" + Config.ROADRUNNER_SERVER_PORT);
 		dbName = prefs.getString("database", Config.ROADRUNNER_SERVER_NAME);
 
 		JSONObject repl = new JSONObject();
 		try {
 			
 			JSONObject items = new JSONObject();
 			items.put("items", itemIdArray);
 
 			repl.put("source", "http://" + IPandPort + "/" + dbName);
 			repl.put("target", dbName);
 			repl.put("filter", "roadrunner/itemfilter");
 			repl.put("query_params", items);
 
 			HttpPost post = RequestFactory.createLocalHttpPost("_replicate");
 			StringEntity body = new StringEntity(repl.toString());
 			post.setEntity(body);
 			
 	        String result = HttpExecutor.getInstance().executeForResponse(post);
 	        result.toString();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (CouchDBNotReachableException e) {
 			e.printStackTrace();
 		}
 	}
 		
 	/*
 	 * returns the next ID
 	 */
 	public String getNextId()  {
 		try {
 			HttpGet get = RequestFactory.createHttpGet("_uuids");		
 			JSONObject content = new JSONObject(HttpExecutor.getInstance().executeForResponse(get)); 
 			JSONArray array = new JSONArray(content.getString("uuids"));
 			return array.getString(0);
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (CouchDBNotReachableException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
