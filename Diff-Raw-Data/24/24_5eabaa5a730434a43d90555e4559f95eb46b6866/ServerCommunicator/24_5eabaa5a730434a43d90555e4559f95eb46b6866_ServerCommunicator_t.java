 package illinois.sweng.sctracker;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.res.Resources;
 import android.util.Base64;
 import android.util.Log;
 
 public class ServerCommunicator {
 	private final String TAG;
 	private DelegateActivity mDelegate;
 	private Resources mResources;
 	/**
 	 * Creates a new ServerCommunicator with the given context
 	 * 
 	 * @param delegate
 	 *            Context of the owner of the ServerCommunicator
 	 * @param delegateTag
 	 *            String identifying the owner of this ServerCommunicator
 	 */
 	public ServerCommunicator(DelegateActivity delegate, String delegateTag) {
 		mDelegate = delegate;
 		mResources = mDelegate.getResources(); 
 		TAG = "sc2TrackerServerCommunicator-" + delegateTag;
 	}
 
 	/**
 	 * Sends a request to the server to create a new user account with the given
 	 * username and password
 	 * 
 	 * @param username
 	 *            Username for the new user account
 	 * @param password
 	 *            Password for the new user account
 	 * 
 	 */
 	public void sendAccountCreationRequest(String username, String password) {
 		String urlString = buildAccountCreationURL(username, password);
 		HttpPost httpPost = new HttpPost(urlString);
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 		pairs.add(new BasicNameValuePair("username", username));
 		pairs.add(new BasicNameValuePair("password", password));
 
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 		} catch (IOException e) {
 			String message = mResources.getString(R.string.serverIOExceptionMessage);
 			mDelegate.handleServerError(message);
 			Log.e(TAG, message, e);
 			e.printStackTrace();
 		} 
 		
 		Log.d(TAG, "Sending account creation request");
 		executeHttpRequest(httpPost);
 	}
 
 
 
 	/**
 	 * Sends a request to the server to delete a user account with the given
 	 * username and password
 	 * 
 	 * @param username
 	 *            Username for the user account
 	 * @param password
 	 *            Password for the user account
 	 */
 	public void sendAccountDeletionRequest(String username, String password) {
 		String urlString = buildAccountDeletionURL(username, password);
 		HttpPost httpPost = new HttpPost(urlString);
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 		pairs.add(new BasicNameValuePair("username", username));
 		pairs.add(new BasicNameValuePair("password", password));
 		
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 		} catch (IOException e) {
 			String message = mResources.getString(R.string.serverIOExceptionMessage);
 			mDelegate.handleServerError(message);
 			Log.e(TAG, message, e);
 			e.printStackTrace();
 		} 
 
 		Log.d(TAG, "Sending account deletion request");
 		executeHttpRequest(httpPost);
 	}
 	
 	/**
 	 * Sends a GET request for authentication to the server with String userpass
 	 * as username:password
 	 * 
 	 * @param userpass
 	 *            String of the form username:password
 	 */
 	public void sendAuthenticationRequest(String userpass) {
 		String urlString = buildAuthenticateURL();
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 
		Log.d(TAG, "Sending authentication request" + userpass);
 		executeHttpRequest(request);
 	}
 	
 	/**
 	 * Sends a GET request to retrieve all player data from the server
 	 * @param userpass
 	 *            String of the form username:password
 	 */
 	public void sendGetAllPlayersRequest(String userpass) {
 		String urlString = buildGetAllPlayersURL();
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
		Log.d(TAG, "Sending get all players request:" + userpass);
 		executeHttpRequest(request);
 	}
 	
 	public void sendGetAllTeamsRequest(String userpass) {
 		String urlString = buildGetAllTeamsURL();
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all teams request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendGetAllEventsRequest(String userpass) {
 		String urlString = buildGetAllEventsURL();
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendGetEventRequest(String userpass, long eventPK){
 		String urlString = buildGetEventURL(eventPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic " + Base64.encodeToString(userpass.getBytes(), Base64.NO_WRAP));
 		Log.d(TAG, "Sending get event request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendGetAllFavoritesRequest(String userpass) {
 		String urlString = buildGetAllFavoritesURL();
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
		Log.d(TAG, "Sending get all favorites request"  + userpass);
 		executeHttpRequest(request);
 	}
 	
 	public void sendFavoritePlayerRequest(String userpass, String playerPK) {
 		String urlString = buildFavoritePlayerURL(playerPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendFavoriteTeamRequest(String userpass, String teamPK) {
 		String urlString = buildFavoriteTeamURL(teamPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendFavoriteEventRequest(String userpass, String eventPK) {
 		String urlString = buildFavoriteEventURL(eventPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendUnfavoritePlayerRequest(String userpass, String playerPK) {
 		String urlString = buildUnfavoritePlayerURL(playerPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendUnfavoriteTeamRequest(String userpass, String teamPK) {
 		String urlString = buildUnfavoriteTeamURL(teamPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendUnfavoriteEventRequest(String userpass, String eventPK) {
 		String urlString = buildUnfavoriteEventURL(eventPK);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending get all events request");
 		executeHttpRequest(request);
 	}
 	
 	public void sendDeviceRegistrationRequest(String userpass, String type, String regId) {
 		String urlString = buildDeviceRegistrationURL(type, regId);
 		HttpGet request = new HttpGet(urlString);
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeToString(userpass.getBytes(),
 								Base64.NO_WRAP));
 		Log.d(TAG, "Sending device registration request");
 		executeHttpRequest(request);
 	}
 
 	/**
 	 * Sends an Http request and handles the response from the server 
 	 * @param request HttpUriRequest to be executed
 	 */
 	private void executeHttpRequest(HttpUriRequest request) {
 		HttpClient httpClient = new DefaultHttpClient();
 		
 		try {
 			HttpResponse response = httpClient.execute(request);
 			readHttpResponse(response);
 
 		} catch (IOException e) {
 			String message = mResources.getString(R.string.serverIOExceptionMessage);
 			mDelegate.handleServerError(message);
 			Log.e(TAG, message, e);
 			e.printStackTrace();
 		} catch (JSONException e) {
 			String message = mResources.getString(R.string.serverJSONError);
 			mDelegate.handleServerError(message);
 			Log.e(TAG, message, e);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Read the httpResponse and display the appropriate success/failure
 	 * notification
 	 * 
 	 * @param httpResponse
 	 *            InputStream returned from the web server
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	private void readHttpResponse(HttpResponse httpResponse)
 			throws IOException, JSONException {
 		
 		StatusLine statusLine = httpResponse.getStatusLine();
 		int statusCode = statusLine.getStatusCode();
 		
 		if (statusCode == HttpStatus.SC_OK) {
 			HttpEntity httpEntity = httpResponse.getEntity();
 			InputStream in = httpEntity.getContent();			
 			String responseString = readStream(in);
 			
 			Log.d("YY", responseString);
 			JSONObject json = new JSONObject(responseString);
 			
 			int responseCode = json.getInt("status_code");
 			Log.d("Status code" + TAG, "" + responseCode);
 			if(responseCode == mResources.getInteger(R.integer.server_OK)) {
 				sendSuccessCallback(json);
 			} else {
 				String errorMessage = json.getString("response");
 				mDelegate.handleServerError(errorMessage);
 			}			
 		} else {
 			String response = "" + statusCode;
			Log.d(TAG, response);
 			String message = "An error occurred on the server";
 			mDelegate.handleServerError(message);
 		}
 	}
 
 	/**
 	 * When a request is successfully completed, call back to the delegate
 	 * that made the request with the response from the server
 	 * @param json JSONObject returned from the server
 	 */
 	private void sendSuccessCallback(JSONObject json) {
 		JSONArray array = json.optJSONArray("response");
 		if(array != null) {
 			mDelegate.handleServerResponseData(array);
 		} else {
 			String message = json.optString("response");
 			mDelegate.handleServerResponseMessage(message);
 		}
 	}
 
 	/**
 	 * Reads an InputStream into a String
 	 * @param in InputStream to be read
 	 * @return String containing the data from the InputStream
 	 * @throws IOException
 	 */
 	private String readStream(InputStream in) throws IOException {
 		BufferedReader r = new BufferedReader(new InputStreamReader(in));
 		StringBuilder total = new StringBuilder();
 		String line;
 		while ((line = r.readLine()) != null) {
 			total.append(line);
 		}
 		
 		return total.toString();
 	}
 
 	/**
 	 * Given a username and a password, builds the appropriate url to send a GET
 	 * to in order to create a new account
 	 * 
 	 * @param username
 	 *            User's new account email address/username
 	 * @param password
 	 *            User's password
 	 * @return String representing the URL to generate a new user account
 	 */
 	private String buildAccountCreationURL(String username, String password) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence registerURL = mResources.getText(R.string.serverRegisterURL);
 		
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(registerURL);
 		String urlString = sb.toString();
 		
 		return urlString;
 	}
 
 	/**
 	 * Given a username and a password, builds the appropriate url to send a GET
 	 * to in order to delete an account
 	 * 
 	 * @param username
 	 *            User's new account email address/username
 	 * @param password
 	 *            User's password
 	 * @return String representing the URL to generate a new user account
 	 */
 	private String buildAccountDeletionURL(String username, String password) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence unregisterURL = mResources.getText(R.string.serverUnregisterURL);
 
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(unregisterURL);
 		String urlString = sb.toString();
 
 		return urlString;
 	}
 
 	/**
 	 * Builds URL for authentication
 	 * 
 	 * @return String representing the URL to generate a new user account
 	 */
 	private String buildAuthenticateURL() {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence authURL = mResources.getText(R.string.serverAuthenticateURL);
 
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(authURL);
 		String urlString = sb.toString();
 
 		return urlString;
 	}
 	
 	/**
 	 * Builds URL for getting all player data
 	 * @return String representing the URL to get all player data
 	 */
 	private String buildGetAllPlayersURL() {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence getPlayersURL = mResources.getText(R.string.serverGetAllPlayersURL);
 
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(getPlayersURL);
 		String urlString = sb.toString();
 
 		return urlString;
 	}
 	
 	private String buildGetAllTeamsURL() {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence getTeamsURL = mResources.getText(R.string.serverGetAllTeamsURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(getTeamsURL);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildGetEventURL(long eventPK){
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence getEventURL = mResources.getText(R.string.serverGetEventURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(getEventURL);
 		sb.append(eventPK);
 		
 		return sb.toString();
 	}
 	
 	private String buildGetAllEventsURL() {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence getEventsURL = mResources.getText(R.string.serverGetAllEventsURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(getEventsURL);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildFavoritePlayerURL(String playerPK) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence favoritePlayerURL = mResources.getText(R.string.serverFavoritePlayerURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(favoritePlayerURL);
 		sb.append(playerPK);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildFavoriteTeamURL(String teamPK) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence favoriteTeamURL = mResources.getText(R.string.serverFavoriteTeamURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(favoriteTeamURL);
 		sb.append(teamPK);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildFavoriteEventURL(String eventPK) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence favoriteEventURL = mResources.getText(R.string.serverFavoriteEventURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(favoriteEventURL);
 		sb.append(eventPK);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildUnfavoritePlayerURL(String playerPK) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence unfavoritePlayerURL = mResources.getText(R.string.serverUnfavoritePlayerURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(unfavoritePlayerURL);
 		sb.append(playerPK);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildUnfavoriteTeamURL(String teamPK) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence unfavoriteTeamURL = mResources.getText(R.string.serverUnfavoriteTeamURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(unfavoriteTeamURL);
 		sb.append(teamPK);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildUnfavoriteEventURL(String eventPK) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence unfavoriteEventURL = mResources.getText(R.string.serverUnfavoriteEventURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(unfavoriteEventURL);
 		sb.append(eventPK);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildGetAllFavoritesURL() {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence unfavoriteEventURL = mResources.getText(R.string.serverGetAllFavoritesURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(unfavoriteEventURL);
 		String urlString = sb.toString();
 		return urlString;
 	}
 	
 	private String buildDeviceRegistrationURL(String type, String regId) {
 		CharSequence baseURL = mResources.getText(R.string.serverURL);
 		CharSequence registerDeviceURL = mResources.getText(R.string.serverRegisterDeviceURL);
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(registerDeviceURL);
 		sb.append("?type=" + type + "&rid=" + regId);
 		String urlString = sb.toString();
 		return urlString;
 	}
 }
