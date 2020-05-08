 package com.nexusray.sqlsync.sync;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.nexusray.sqlsync.account.Authenticator;
 import com.nexusray.sqlsync.provider.SyncDatabaseHelper;
 import com.nexusray.sqlsync.settings.SyncSettings;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SyncResult;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteConstraintException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class SyncExecutor {
 
 	private static HttpClient httpClient = null;
 	private static final String logTag = "SQLsync";
 	private static volatile boolean isSyncActive = false;
 	private static Object syncLock = new Object();
 	private static long syncStart = 0;
 	private static volatile long syncTime = 0;
 
 	public static long elapsed() {
 		return syncTime;
 	}
 	
 	/**
 	 * Creates custom account for synchronization
 	 */
 	public static boolean createAccount(String name, String password, Context context) {
 		
 		Bundle prop = new Bundle();
 		prop.putString(AccountManager.KEY_ACCOUNT_NAME, name);
 		prop.putString(AccountManager.KEY_PASSWORD, password);
 		Authenticator auth = new Authenticator(context);
 		auth.addAccount(null, null, null, null, prop);
 		
 		return false;
 	}
 	
 	/**
 	 * Creates basic httpClient for connection to remote webservice
 	 */
 	private static synchronized void createHttpClient() {
 
 		if (httpClient == null) {			
 			
 			HttpParams params = new BasicHttpParams();
 			HttpConnectionParams.setConnectionTimeout(params, SyncSettings.HTTP_TIMEOUT);
 			HttpConnectionParams.setSoTimeout(params, SyncSettings.HTTP_TIMEOUT);
 			ConnManagerParams.setTimeout(params, SyncSettings.HTTP_TIMEOUT);
 			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 	        
 	        //registers schemes for both http and https
 	        SchemeRegistry registry = new SchemeRegistry();
 	        
 	        try
 	        {
 	        	KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
 	        	trustStore.load(null, null);
 	        	SSLSocketFactory sf = new BasicSSLSocketFactory(trustStore);
 	        	
 	        	registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		        registry.register(new Scheme("https", sf, 443));
 	        }
 	        catch (Exception e)
 	        {
 	        	Log.e(logTag, "Could not create SSL socket factory.");
 	        }
 	        
 
 	        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
 	        httpClient = new DefaultHttpClient(manager, params);
 		}
 	}
 
 	
 	/**
 	 * You don't need to run any synchronization methods, sync will happen automatically
 	 * after you change database. NotifyChange in SyncProvider will ensure that.
 	 * If sync doesn't occur sync might be badly configured, check if authority is set up
 	 * correctly.
 	 * DO NOT RUN this method in the context of the main thread, run it in a service.
 	 * Let SyncAdapter run this method, but if you must (want) use this method
 	 * run in always as a service in a different thread. If you want to force
 	 * synchronization it is preferred use requestSync() method instead.
 	 * Track data changes with ContentObservers and/or CursorAdapters.
 	 * 
 	 */
 	public static boolean preformSync(String name, String password, final Context context, SyncResult syncResult) {
 		
 		Log.i(logTag, "Starting sync");
 		
 		
 		// Two synchronizations in a row are disabled
 		synchronized(syncLock) {			
 			if (isSyncActive()) {
 				Log.w(logTag, "Sync IN PROGRESS");
 				return false;				
 			}
 			isSyncActive = true;
 		}
 		
 		syncStart = System.currentTimeMillis();
 		
 		ContentResolver cr = context.getContentResolver();
 		boolean success = false;
 		String response = null;
 		
 		try {		
 			// Creating HTTP request with all changes from local mobile database
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("name", name));
 			params.add(new BasicNameValuePair("pass", password));
 			
 			createHttpClient();
 		
 			JSONObject jDatabase = createReplicationInfo(cr, context);
 			
 			if (jDatabase == null) return false;
 			
 			String sDatabase = jDatabase.toString();
 
 			//Log.i(logTag, "sending:\n" + sDatabase);
 			
 			params.add(new BasicNameValuePair("sync", sDatabase));
 			
 			HttpEntity entity = null;
 			try {
 				entity = new UrlEncodedFormEntity(params,"UTF-8");
 			} catch (final UnsupportedEncodingException e) {
 				throw new AssertionError(e);
 			}
 	
 			HttpPost post = new HttpPost(SyncSettings.URL_WEBSERVICE + "?a=sync");
 			post.setEntity(entity);
 			
 			post.addHeader("Accept-Encoding", "gzip");
 		
 	        
 			// Sending and reading response	
 			// Sends JSON object
 			HttpResponse resp = httpClient.execute(post);
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				
 				InputStream inputStream = resp.getEntity().getContent();
 				Header encoding = resp.getFirstHeader("Content-Encoding");
 				if (encoding != null && encoding.getValue().equalsIgnoreCase("gzip")) {
 					inputStream = new GZIPInputStream(inputStream);
 				}
 				
 				// Recieves JSON object
 				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 				StringBuilder responseBuilder = new StringBuilder();
 				String line;
 				while ((line = reader.readLine()) != null) {
 					responseBuilder.append(line);
 				}
 							
 				response = responseBuilder.toString();
 				JSONObject repSync = new JSONObject(response);
 				//Log.i(logTag, "response:\n" + response);
 				
 				// Persisting changes
 				persistSyncResult(cr, context, repSync);
 				
 				success = true;
 				
 				return true;
 
 			} 
 			else {
 				Log.e(logTag, "Synchronization failed! Webservice responded but denied access. " +
 						"Check authorization credientials.");
 				syncResult.stats.numAuthExceptions++;
 			}
 				
 			
 		}
 		catch (IOException e) {
 			Log.e(logTag, "Synchronization failed! SyncExecutor could not connect to remote webservice. " +
 					"Check please your webservice URL.");
 			e.printStackTrace();
 			
 			syncResult.stats.numIoExceptions++;
 		} 
 		catch (JSONException e) {
 			Log.e(logTag, "Synchronization failed! Remote webservice hasn't returned valid JSON object. " +
 					"Webservice could have returned errors, check below:"); 
 			if (response != null) Log.e(logTag, response);
 			else Log.e(logTag, "<no response>");
 			
 			syncResult.stats.numParseExceptions++;
 			//e.printStackTrace();
 		}
 		catch (Exception e) {
 			Log.e(logTag, "Synchronization failed! Unknown error! Probably database error."); 
 			e.printStackTrace();
 			syncResult.databaseError = true;
 		} 
 		finally {
 			if (!success) { 
 				// synchronization must have failed, flags are removed
 				Log.i(logTag, "Cleaning syncStatus flags from tables due to error(s)!");
 				cleanUpSyncFlags(cr);
 				
 			}
 			isSyncActive = false;
 			
 			
 		}
 		
 		return false;
 	}
 	
 	
 	
 	
 	private static JSONObject createReplicationInfo(ContentResolver cr, Context context) {
 		JSONObject lDatabase = new JSONObject();
 		Cursor c = null;
 			
 		
 		try {
 			
 			// Set syncStatus flags for rows that are going to be synchronized with global DB
 			// also by doing this it marks them for synchronization
 			// if you want to exempt some rows from synchronization, you can do it here
 			cr.update(Uri.parse("content://" + SyncSettings.AUTHORITY + "/StartSync?sync=1"),
 					null, null, null);
 			
 			
 			
 			
 			JSONArray lTableNames = new JSONArray();
 			JSONArray lRowData;
 			
 			for (String tableName : SyncSettings.tablesToSync) {
 	
 
 				JSONArray lUpdateTableData = new JSONArray();
 				JSONArray lUpdateTableGID = new JSONArray();
 				JSONArray lUpdateTableLastChange = new JSONArray();
 				
 				JSONArray lTableStructure = new JSONArray();
 				
 				int idxLocalID, idxGlobalID, idxLastChange, idxSyncStatus;				
 				Uri tableUri = Uri.parse("content://" + SyncSettings.AUTHORITY + "/" + tableName + "?sync=1");
 				
 				// INSERTs and UPDATEs				
 				c = cr.query(tableUri, null, "_syncStatus=1", null, null);
 				
 				
 				// indices of columns to separate custom data from key synchronization values
 				idxLocalID = c.getColumnIndexOrThrow("_ID");
 				idxGlobalID = c.getColumnIndexOrThrow(SyncDatabaseHelper.syncID);
 				idxLastChange = c.getColumnIndexOrThrow("_lastChange");
 				idxSyncStatus = c.getColumnIndexOrThrow("_syncStatus");
 
 				while (c.moveToNext()) {
 					lRowData = new JSONArray();
 					 
 					for (int i = 0; i < c.getColumnCount(); i++) {
 						String value = c.getString(i);
 						
 						if (idxGlobalID == i) lUpdateTableGID.put(value);
 						else if (idxLastChange == i) lUpdateTableLastChange.put(value);
 						else if (idxLocalID != i && idxSyncStatus != i) lRowData.put(value);
 					}
 
 					lUpdateTableData.put(lRowData);
 					
 				}
 				
 				
 				lDatabase.put("updData_" + tableName, lUpdateTableData);	
 				lDatabase.put("updGID_" + tableName, lUpdateTableGID);
 				lDatabase.put("updLC_" + tableName, lUpdateTableLastChange);
 				
 				
 				
 				
 				// Structure - sends structure of data, order of columns might not be the same as in global DB
 				for (int i = 0; i < c.getColumnCount(); i++) {
 					if (idxLocalID != i && idxGlobalID != i && idxLastChange != i && idxSyncStatus != i) 
 						lTableStructure.put(c.getColumnName(i));
 				}
 
 
 				lDatabase.put("struct_" + tableName, lTableStructure);	
 				c.close();
 				c = null;
 				
 				lTableNames.put(tableName);
 			}
 			
 			lDatabase.put("tableNames", lTableNames);
 			
 			
 			// DELETEs
 			JSONArray lTableData = new JSONArray();
 			
 			c = cr.query(Uri.parse("content://" + SyncSettings.AUTHORITY + "/__SQLsyncDeletedRows"),
 					new String[] {"tableName", SyncDatabaseHelper.syncID, "_timeDeleted"}, "_syncStatus=1", null, null);
 
 			while (c.moveToNext()) {
 				lRowData = new JSONArray();
 
 				for (int i = 0; i < c.getColumnCount(); i++) {
 					lRowData.put(c.getString(i));
 				}
 
 				lTableData.put(lRowData);
 			}
 			
 			c.close();
 			c = null;
 			
 			lDatabase.put("deletedRows", lTableData);	
 			
 			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 			
 			lDatabase.put("deviceId", preferences.getInt("_sync_deviceid", 0) );
 			lDatabase.put("deviceIdStatus", preferences.getInt("_sync_deviceid_status", 0) );
 			lDatabase.put("androidId", preferences.getString("_sync_androidid", "null") );
 			lDatabase.put("lastSync", preferences.getString("_sync_lastsync", "1960-01-01 00:00:00+01") );
 			
 			
 			
 		} catch (JSONException e) {
 			Log.e(logTag, "Synchronization could not be started! JSON objects could not be created. " +
 					"Probably programming error. Check with the SQLsync developer.");
 			e.printStackTrace();
 			lDatabase = null;
 		} catch (IllegalArgumentException e) {
 			Log.e(logTag, "Synchronization could not be started! Synchronizable table is missing its " +
 					"key columns. Make sure you created your tables by using sync methods in " +
 					"SyncDatabaseHelper");
 			e.printStackTrace();
 			lDatabase = null;
 		} catch (SQLiteConstraintException e) {
 			Log.e(logTag, "Synchronization has failed! Same row got deleted twice.");
 			e.printStackTrace();
 		
 		} finally {
 			if (c != null) c.close();
 		}
 		
 		return lDatabase;
 	}
 	
 	
 	
 	
 	
 	private static void persistSyncResult(ContentResolver cr, Context context, JSONObject repSync) throws JSONException
 	{
 		
 		
 		for (String tableName : SyncSettings.tablesToSync) {
 			JSONArray structure = (JSONArray) repSync.get("struct_" + tableName);
 			int nCols = structure.length();
 			int idxGlobalID = -1;
 			Uri tableUri = Uri.parse("content://" + SyncSettings.AUTHORITY + "/" + tableName + "?sync=1");
 
 			for (int col = 0; col < nCols; col++) {				
 				if (structure.getString(col).equals(SyncDatabaseHelper.syncID)) 
 				{
 					idxGlobalID = col;
 					break;
 				}
 			}
 			
 			if (idxGlobalID == -1)
 			{
 				Log.e(logTag, "Synchronization Error: Respose from global database lacks _syncID column. " +
 						"Make sure tables of global (remote) database has all key columns needed for " +
 						"synchronization.");
 				return;
 			}
 			
 			// Persisting all new rows from global database
 			JSONArray data = (JSONArray) repSync.get("updData_" + tableName);	
 			int nRows = data.length();
 			ContentValues[] cv = new ContentValues[nRows];
 			
 			for (int row = 0; row < nRows; row++) {			
 				JSONArray rowData = data.getJSONArray(row);
 				cv[row] = new ContentValues(nCols);
 				
 				for (int col = 0; col < nCols; col++) {	
 					
 					if (rowData.isNull(col))
 						cv[row].putNull(structure.getString(col));
 					else
 						cv[row].put(
 								structure.getString(col),
 								rowData.getString(col));					
 				}
 				// This will make sure it won't be send back during next sync
 				cv[row].putNull("_lastChange");
 				cv[row].put("_syncStatus", 0);
 			}
 			cr.bulkInsert(tableUri, cv);
 		}
 		
 
 		// persist DELETEs
 		JSONArray deletedRows = (JSONArray) repSync.get("deletedRows");
 		
 		int noRows = deletedRows.length();
 		if (noRows > 0) {
 			
 			HashMap<String, String> deleteIDs = new HashMap<String, String>();
 			
 			for (int row = 0; row < noRows; row++) {
 				JSONArray rowData = deletedRows.getJSONArray(row);
 				
 				String tableName, allIDs;
 				tableName = rowData.getString(1);
 				allIDs = deleteIDs.get(tableName);
 				if (allIDs == null) allIDs = "";
 				else allIDs += ",";
 				allIDs += rowData.getString(0);
 				
 				deleteIDs.put(tableName, allIDs);
 			}
 			for (Map.Entry<String, String> entry : deleteIDs.entrySet()) {
 			    String tableName = entry.getKey();
 			    String allIDs = entry.getValue();
 			    
 				cr.delete(Uri.parse("content://" + SyncSettings.AUTHORITY + "/" + tableName + "?sync=1"),
 						SyncDatabaseHelper.syncID + " IN (" + allIDs+ ")", null);
 				
 				//todo: disable trigger?
 			}	
 		}
 		
 		// Delete all marked rows for DELETE permanently		
 		cr.delete(Uri.parse("content://" + SyncSettings.AUTHORITY + "/__SQLsyncDeletedRows?sync=1"), "_syncStatus=1", null);
 				
 		// Stores time when synchronization was executed in global DB
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);		
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putString("_sync_lastsync", repSync.getString("lastSync"));
 		int newId = repSync.getInt("newDeviceId");
 		if (newId > 0) {
 			editor.putInt("_sync_deviceid", newId);
 			editor.putInt("_sync_deviceid_status", 1);
 		}
 		editor.commit();
 		
 		isSyncActive = false;
 		
 		syncTime = System.currentTimeMillis() - syncStart;
 		
 		cr.update(Uri.parse("content://" + SyncSettings.AUTHORITY + "/EndSync?sync=1"),
 				null, null, null);
 	}
 	
 	
 	/*
 	 * In case of failed synchronization this returns database into
 	 * correct state.
 	 */
 	protected static void cleanUpSyncFlags(ContentResolver cr) {
 		
 		cr.update(Uri.parse("content://" + SyncSettings.AUTHORITY + "/EndSync?sync=1"),
 				null, null, null);
 		
 	}
 	
 	/**
 	 * Looks whether is synchronization active
 	 * @return boolean value, true if it is, otherwise false
 	 */
 	public static boolean isSyncActive() {
 		return isSyncActive;
 	}
 	
 	/**
 	 * Asks SyncManager to make synchronization as soon as possible.
 	 * If this method does not work, probably SyncAdapter is not set up correctly,
 	 * check SyncAdapter and Authority string if it matches.
 	 */
 	public static void requestSync() {
 		ContentResolver.requestSync(null, SyncSettings.AUTHORITY, new Bundle());
 	}
 	
 	public static void enableSync(Context context) {
 		AccountManager accountManager = AccountManager.get(context);
 		for (Account a: accountManager.getAccounts())
 			ContentResolver.setSyncAutomatically(a, SyncSettings.AUTHORITY, true);
 	}
 	
 	public static void disableSync(Context context) {
 		AccountManager accountManager = AccountManager.get(context);
 		for (Account a: accountManager.getAccounts())
 			ContentResolver.setSyncAutomatically(a, SyncSettings.AUTHORITY, false);
 	}	
 
 }
 
