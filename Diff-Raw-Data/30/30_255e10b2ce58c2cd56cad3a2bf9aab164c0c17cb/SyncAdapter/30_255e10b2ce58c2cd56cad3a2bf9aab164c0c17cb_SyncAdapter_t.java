 package com.sportsfire.sync;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.sportsfire.Player;
 import com.sportsfire.db.InjuryTable;
 import com.sportsfire.db.InjuryUpdateTable;
 import com.sportsfire.db.PlayerTable;
 import com.sportsfire.db.SquadTable;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SyncResult;
 import android.database.Cursor;
import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class SyncAdapter extends AbstractThreadedSyncAdapter {
 	private AccountManager mAccountManager;
 	private ContentResolver mContentResolver;
 	
 	/** Base URL for the v2 Sample Sync Service */
     public static final String BASE_URL = "http://sportsfire4.cs.ucl.ac.uk";
     /** URI for authentication service */
     public static final String AUTH_URI = BASE_URL + "/auth";
     /** URI for sync service */
     public static final String SYNC_PLAYERS_URI = BASE_URL + "/players/";
     public static final String SYNC_SQUADS_URI = BASE_URL + "/squads/";
     public static final String SYNC_INJURIES_URI = BASE_URL + "/injuries/";
     public static final String SYNC_INJURYUPDATES_URI = BASE_URL + "/injuryupdates/";
     public static final int HTTP_REQUEST_TIMEOUT_MS = 70 * 1000;
     
     private static final String SYNC_MARKER_KEY = "com.sportsfire.sync.marker";
 
 
 
 	public SyncAdapter(Context context, boolean autoInitialize) {
 		super(context, autoInitialize);
 		mAccountManager = AccountManager.get(context);
 		mContentResolver = context.getContentResolver();
 	}
 	
 	 /**
      * Configures the httpClient to connect to the URL provided.
      */
     public static HttpClient getHttpClient() {
         HttpClient httpClient = new DefaultHttpClient();
         final HttpParams params = httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
         return httpClient;
     }
 
 	@Override
 	public void onPerformSync(Account account, Bundle extras, String authority,
 			ContentProviderClient provider, SyncResult syncResult) {
 		String authtoken = null;
 
 		loadSquadsAndPlayers();
 		updateInjuries(account);
 		
 		
 	}
 
 	private void updateInjuries(Account account) {
 		try{
 			
 			JSONArray jsonarray = new JSONArray();
 			String[] projection = { InjuryUpdateTable.KEY_INJURY_ID, InjuryUpdateTable.KEY_UPDATE_TYPE, InjuryUpdateTable.KEY_DATA};
 			Cursor cursor = mContentResolver.query(Provider.CONTENT_URI_INJURIES_UPDATES, projection, null, null, null);
 			if (cursor.moveToFirst()) {
 	            do {
 	            	JSONObject jsonentry = new JSONObject();
 	            	for(int i = 0;i<cursor.getColumnCount();i++){
 	            		jsonentry.put(cursor.getColumnName(i),cursor.getString(i));
 	            	}
 	            	jsonarray.put(jsonentry);
 	            } while (cursor.moveToNext());
 	        }
 			JSONObject params = new JSONObject();
 			params.accumulate("syncmarker", mAccountManager.getUserData(account, SYNC_MARKER_KEY));
 			params.accumulate("updates", jsonarray);
 	        final HttpPost post = new HttpPost(SYNC_INJURYUPDATES_URI);
 	        Log.e("post",post.toString());
 	        post.addHeader("Content-Type", "application/json");
 	       
 	        post.setEntity(new ByteArrayEntity(params.toString().getBytes("UTF8")));
 	        final HttpResponse resp = getHttpClient().execute(post);
 
 	        final String response = EntityUtils.toString(resp.getEntity());
 	        Log.e("response",response);
 	        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	        	final JSONObject serverResponse = new JSONObject(response);
	        
	
 	        	
	        	final JSONArray serverInjuries = new JSONArray(serverResponse.getString("updatedinjuries"));
 	            Log.d("Response", response);
	            	            
 	            LinkedList<ContentValues> resultsList = new LinkedList<ContentValues>();
 	            for (int i = 0; i < serverInjuries.length(); i++) {
 		            ContentValues values = new ContentValues();
 
 	            	JSONObject object = serverInjuries.getJSONObject(i);
 	           
 	            	@SuppressWarnings("unchecked")
 					Iterator<String> it = object.keys();
 	            	while(it.hasNext()){
 	            		String key = it.next();
	            		if(key != "_id"){
		            		values.put(key, object.getString(key));
	            		}
 	            	}	            	
 
	            	// do an upsert...
	            	// does the value exist?
	            	cursor = mContentResolver.query((Uri.withAppendedPath(Provider.CONTENT_URI_INJURIES, "'" + object.getString("_id") + "'")), null, null, null, null);
	            	if(cursor.getCount() == 0){
	            		values.put("_id", object.getString("_id"));
	            		mContentResolver.insert(Provider.CONTENT_URI_INJURIES, values);
	            	}
	            	else{
	            		mContentResolver.update((Uri.withAppendedPath(Provider.CONTENT_URI_INJURIES, "'" + object.getString("_id") + "'")), values, null, null);
	            	}
 	            	resultsList.add(values);
 	            }
 	            mContentResolver.delete(Provider.CONTENT_URI_INJURIES_UPDATES, null, null);
 	            mAccountManager.setUserData(account, SYNC_MARKER_KEY, (String)serverResponse.get("newsyncmarker"));
 	            
 	        }
 		}
 		catch (Exception e){
 			Log.e("Exception", e.toString());
 		}
 		
 		
 	}
 
 	private void loadSquadsAndPlayers() {
 
 		LinkedList<ContentValues> squads = loadSquads();
 		LinkedList<ContentValues> players = loadPlayers();
 		mContentResolver.delete(Provider.CONTENT_URI_PLAYERS, null, null);
 		mContentResolver.delete(Provider.CONTENT_URI_SQUADS, null, null);
 		ListIterator<ContentValues> it = squads.listIterator();
 		while(it.hasNext()){
 			mContentResolver.insert(Provider.CONTENT_URI_SQUADS, it.next());
 		}
 		it = players.listIterator();
 		while(it.hasNext()){
 			mContentResolver.insert(Provider.CONTENT_URI_PLAYERS, it.next());
 		}
 		
 		
 		
 	}
 
 	private LinkedList<ContentValues> loadPlayers() {
 
 		try{
 			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 	        final HttpEntity entity;
 	        try {
 	            entity = new UrlEncodedFormEntity(params);
 	        } catch (final UnsupportedEncodingException e) {
 	            // this should never happen.
 	            throw new IllegalStateException(e);
 	        }
 	        final HttpGet get = new HttpGet(SYNC_PLAYERS_URI);
 	        final HttpResponse resp = getHttpClient().execute(get);
 
 	        final String response = EntityUtils.toString(resp.getEntity());
 	        Log.e("response",response);
 	        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	            // Our request to the server was successful - so we assume
 	            // that they accepted all the changes we sent up, and
 	            // that the response includes the contacts that we need
 	            // to update on our side...
 	            final JSONArray serverPlayers = new JSONArray(response);
 	            Log.d("Response", response);
 	            
 	            LinkedList<ContentValues> resultsList = new LinkedList<ContentValues>();
 	            for (int i = 0; i < serverPlayers.length(); i++) {
 		            ContentValues values = new ContentValues();
 
 	            	JSONObject object = serverPlayers.getJSONObject(i);
 	            	values.put(PlayerTable.KEY_PLAYER_ID,object.getString("_id"));
 	            	values.put(PlayerTable.KEY_FIRST_NAME,object.getString("firstname"));
 	            	values.put(PlayerTable.KEY_SURNAME,object.getString("surname"));
 	            	values.put(PlayerTable.KEY_SQUAD_ID,object.getString("squadid"));
 	            	values.put(PlayerTable.KEY_DOB,object.getString("dateofbirth"));
 
 	            	//values.put(Database.COL_NAME, object.getString("firstname") + " " + object.getString("surname"));
 	    			//mContentResolver.insert(Provider.CONTENT_URI, values);
 	            	resultsList.add(values);
 	            }
 	            return resultsList;
 	        }
 		}
 		catch (Exception e){
 			Log.e("Exception", e.toString());
 		}
 		
 		return null;
 		
 	}
 	
 	private LinkedList<ContentValues> loadSquads() {
 
 		try{
 			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 	        final HttpEntity entity;
 	        try {
 	            entity = new UrlEncodedFormEntity(params);
 	        } catch (final UnsupportedEncodingException e) {
 	            // this should never happen.
 	            throw new IllegalStateException(e);
 	        }
 	        final HttpGet get = new HttpGet(SYNC_SQUADS_URI);
 	       // final HttpPost post = new HttpPost(SYNC_PLAYERS_URI);
 	       // Log.e("post",post.toString());
 	        //post.addHeader("Content-Type", "application/json");
 	        //post.setEntity(entity);
 	        final HttpResponse resp = getHttpClient().execute(get);
 
 	        final String response = EntityUtils.toString(resp.getEntity());
 	        Log.e("response",response);
 	        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	            // Our request to the server was successful - so we assume
 	            // that they accepted all the changes we sent up, and
 	            // that the response includes the contacts that we need
 	            // to update on our side...
 	            final JSONArray serverPlayers = new JSONArray(response);
 	            Log.d("Response", response);
 	           
 	            LinkedList<ContentValues> resultList = new LinkedList<ContentValues>();
 	            for (int i = 0; i < serverPlayers.length(); i++) {
 	            	ContentValues values = new ContentValues();
 	            	JSONObject object = serverPlayers.getJSONObject(i);
 	            	values.put(SquadTable.KEY_SQUAD_ID,object.getString("_id"));
 	            	values.put(SquadTable.KEY_SQUAD_NAME,object.getString("squadname"));
 	            	
 	            	//values.put(Database.COL_NAME, object.getString("firstname") + " " + object.getString("surname"));
 	    			//mContentResolver.insert(Provider.CONTENT_URI, values);
 	            	resultList.add(values);
 	            }
 	            return resultList;
 	        }
 		}
 		catch (Exception e){
 			Log.e("Exception", e.toString());
 		}
 		return null;
 		
 	}
 
 	 
 	    }
