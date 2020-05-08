 package org.thounds.thoundsapi;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpProtocolParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.thounds.thoundsapi.utils.Base64Encoder;
 
 /**
  * This class provide a list of static methods to manage the communication
  * between a Java application and Thounds
  */
 
 public class RequestWrapper {
 	// private static String STAGE_HOST_PATH = "http://stage.thounds.com";
 	private static String HOST = "http://thounds.com";
 	private static String PROFILE_PATH = "/profile";
 	private static String HOME_PATH = "/home";
 	private static String USERS_PATH = "/users";
 	private static String BAND_PATH = "/band";
 	private static String FRIENDSHIPS_PATH = "/friendships";
 	private static String THOUNDS_PATH = "/thounds";
 	private static String TRACK_PATH = "/tracks";
 	private static String LIBRARY_PATH = "/library";
 	private static String NOTIFICATIONS_PATH = "/notifications";
 	private static String TRACK_NOTIFICATIONS_PATH = "/tracks_notifications";
 	private static DefaultHttpClient httpclient = null;
 
 	protected static String USERNAME = "";
 	protected static String PASSWORD = "";
 	private static boolean isLogged = false;
 
 	private static HttpResponse executeHttpRequest(HttpUriRequest request,
 			boolean useAuthentication) throws ThoundsConnectionException {
 		if (httpclient == null)
 			httpclient = new DefaultHttpClient();
 
 		HttpProtocolParams.setUseExpectContinue(httpclient.getParams(), false);
 		if (useAuthentication && !isLogged) {
 			httpclient.getCredentialsProvider().setCredentials(
 					new AuthScope(null, 80, "thounds", "Digest"),
 					new UsernamePasswordCredentials(USERNAME, PASSWORD));
 		}
 		HttpResponse responce = null;
 		try {
 			responce = httpclient.execute(request);
 			// client.getConnectionManager().shutdown();
 		} catch (IOException e) {
 			throw new ThoundsConnectionException();
 		}
 		return responce;
 	}
 
 	private static JSONObject httpResponseToJSONObject(HttpResponse response)
 			throws IllegalThoundsObjectException {
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new InputStreamReader(response.getEntity()
 					.getContent()));
 
 			StringBuffer sb = new StringBuffer("");
 			String line = "";
 			String NL = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null) {
 				sb.append(line + NL);
 			}
 			in.close();
 
 			String result = sb.toString();
 			return new JSONObject(result);
 		} catch (Exception e) {
 			throw new IllegalThoundsObjectException();
 		}
 	}
 
 	/**
 	 * Thounds login method.
 	 * 
 	 * @param username
 	 *            is the mail address of an active Thounds user
 	 * @param password
 	 *            is the password associated to the user
 	 * @return {@code true} for successfull login, {@code false} otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean login(String username, String password)
 			throws ThoundsConnectionException {
 
 		USERNAME = username;
 		PASSWORD = password;
 		JSONObject json;
 		try {
 			StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH);
 			HttpGet httpget = new HttpGet(uriBuilder.toString());
 			httpget.addHeader("Accept", "application/json");
 			HttpResponse response = executeHttpRequest(httpget, true);
 			json = httpResponseToJSONObject(response);
 
 			if ((json == null) || (!json.getString("email").equals(username))) {
 				USERNAME = "";
 				PASSWORD = "";
 				return false;
 			} else {
 				// Log.d("EMAIL",json.getString("email") );
 				// Log.d("CODE",response.getStatusLine().toString());
 				isLogged = true;
 				return true;
 			}
 		} catch (JSONException e) {
 			System.out.println("LOGIN: Catch JSONException");
 		} catch (IllegalThoundsObjectException e) {
 			System.out.println("LOGIN: Catch IllegalThoundsObjectException");
 		}
 		return false;
 	}
 
 	/**
 	 * Thounds logout method.
 	 */
 	public static void logout() {
 		USERNAME = "";
 		PASSWORD = "";
 		isLogged = false;
 		httpclient.getConnectionManager().shutdown();
 		httpclient = null;
 	}
 
 	/**
 	 * Return {@code true} if authentication credentials are set.
 	 * 
 	 * @return {@code true} if authentication credentials are set, {@code false}
 	 *         otherwise
 	 */
 	public static boolean isLogged() {
 		return isLogged;
 	}
 
 	/**
 	 * Method for retrieve the current user's informations. Require login.
 	 * 
 	 * @return A UserWrapper object that represents the user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static UserWrapper loadUserProfile()
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH);
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httpget, true);
 		return new UserWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method for retrieve a generic user's informations according to the user
 	 * code given as parameter. The user to retrieve must be a friend of the
 	 * current user. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @return A {@link UserWrapper} object that represent the user.{@code null}
 	 *         if the code is about a not friend user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static UserWrapper loadGenericUserProfile(int userId)
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + USERS_PATH + "/"
 				+ Integer.toString(userId));
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 
 		HttpResponse response = executeHttpRequest(httpget, true);
 		return new UserWrapper(httpResponseToJSONObject(response));
 
 	}
 
 	/**
 	 * Method for retrieve the current user's library. Require login.
 	 * 
 	 * @return A {@link ThoundsCollectionWrapper} object that represent the
 	 *         library of the current user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static ThoundsCollectionWrapper loadUserLibrary()
 			throws IllegalThoundsObjectException, ThoundsConnectionException {
 		return loadUserLibrary(1, 10);
 	}
 
 	/**
 	 * Method for retrieve the current user's library. Require login.
 	 * 
 	 * @param page
 	 *            page number
 	 * @param perPage
 	 *            number of thounds per page
 	 * @return A {@link ThoundsCollectionWrapper} object that represent the
 	 *         library of the user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static ThoundsCollectionWrapper loadUserLibrary(int page, int perPage)
 			throws IllegalThoundsObjectException, ThoundsConnectionException {
 
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH
 				+ LIBRARY_PATH);
 		uriBuilder.append("?page=" + Integer.toString(page));
 		uriBuilder.append("&per_page=" + Integer.toString(perPage));
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response;
 		try {
 			response = executeHttpRequest(httpget, true);
 			return new ThoundsCollectionWrapper(httpResponseToJSONObject(
 					response).getJSONObject("thounds-collection"));
 		} catch (JSONException e) {
 			throw new IllegalThoundsObjectException();
 		}
 	}
 
 	/**
 	 * Method for retrieve a generic user's library according to the user code
 	 * given as parameter. The library to retrieve must be a library of the
 	 * friend of the current user. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @return A {@link ThoundsCollectionWrapper} object that represent the
 	 *         library of the user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static ThoundsCollectionWrapper loadGenericUserLibrary(int userId)
 			throws IllegalThoundsObjectException, ThoundsConnectionException {
 		return loadGenericUserLibrary(userId, 1, 10);
 	}
 
 	/**
 	 * Method for retrieve a generic user's library according to the user code
 	 * given as parameter. The library to retrieve must be a library of the
 	 * friend of the current user. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @param page
 	 *            page number
 	 * @param perPage
 	 *            number of thounds to load at time
 	 * @return A {@link ThoundsCollectionWrapper} object that represent the
 	 *         library of the user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static ThoundsCollectionWrapper loadGenericUserLibrary(int userId,
 			int page, int perPage) throws IllegalThoundsObjectException,
 			ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + USERS_PATH
				+ LIBRARY_PATH + "/" + Integer.toString(userId));
 		uriBuilder.append("?page=" + Integer.toString(page));
 		uriBuilder.append("&per_page=" + Integer.toString(perPage));
 
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		try {
 			HttpResponse response = executeHttpRequest(httpget, true);
 			return new ThoundsCollectionWrapper(httpResponseToJSONObject(
 					response).getJSONObject("thounds-collection"));
 		} catch (JSONException e) {
 			throw new IllegalThoundsObjectException();
 		}
 	}
 
 	/**
 	 * Method for retrieve the friends list (band) of the current user. Require
 	 * login.
 	 * 
 	 * @return A {@link BandWrapper} object that represent the band of the
 	 *         current user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static BandWrapper loadUserBand() throws ThoundsConnectionException,
 			IllegalThoundsObjectException {
 		return loadUserBand(1, 10);
 	}
 
 	/**
 	 * Method for retrieve the friends list (band) of the current user. Require
 	 * login.
 	 * 
 	 * @param page
 	 *            page number
 	 * @param perPage
 	 *            number of friends to load at time
 	 * @return A {@link BandWrapper} object that represent the band of the
 	 *         current user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static BandWrapper loadUserBand(int page, int perPage)
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH
 				+ BAND_PATH);
 		uriBuilder.append("?page=" + Integer.toString(page));
 		uriBuilder.append("&per_page=" + Integer.toString(perPage));
 
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httpget, true);
 		return new BandWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method for retrieve the friends list (band) of the user. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @return A {@link BandWrapper} object that represent the band of the user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static BandWrapper loadGenericUserBand(int userId)
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		return loadGenericUserBand(userId, 1, 10);
 	}
 
 	/**
 	 * Method for retrieve the friends list (band) of the user. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @param page
 	 *            page number
 	 * @param perPage
 	 *            number of friends to load at time
 	 * @return A {@link BandWrapper} object that represent the band of the user
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static BandWrapper loadGenericUserBand(int userId, int page,
 			int perPage) throws ThoundsConnectionException,
 			IllegalThoundsObjectException {
 
 		StringBuilder uriBuilder = new StringBuilder(HOST + USERS_PATH + "/"
 				+ Integer.toString(userId) + BAND_PATH);
 		uriBuilder.append("?page=" + Integer.toString(page));
 		uriBuilder.append("&per_page=" + Integer.toString(perPage));
 
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httpget, true);
 		return new BandWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method for retrieve the Thounds home informations. Require login.
 	 * 
 	 * @return A {@link HomeWrapper} object that contain the informations about
 	 *         Thounds home
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static HomeWrapper loadHome() throws ThoundsConnectionException,
 			IllegalThoundsObjectException {
 		return loadHome(1, 10);
 	}
 
 	/**
 	 * Method for retrieve the Thounds home informations. Require login.
 	 * 
 	 * @param page
 	 *            page number
 	 * @param perPage
 	 *            number of thounds to load at time
 	 * @return A {@link HomeWrapper} object that contain the informations about
 	 *         Thounds home
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static HomeWrapper loadHome(int page, int perPage)
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + HOME_PATH);
 		uriBuilder.append("?page=" + Integer.toString(page));
 		uriBuilder.append("&per_page=" + Integer.toString(perPage));
 
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 
 		HttpResponse response = executeHttpRequest(httpget, true);
 		return new HomeWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method to perform a friendship request. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @return {@code true} if friendship request ends successfully, {@code
 	 *         false} otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean friendshipRequest(int userId)
 			throws ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + USERS_PATH + "/"
 				+ Integer.toString(userId) + FRIENDSHIPS_PATH);
 		HttpPost httppost = new HttpPost(uriBuilder.toString());
 		httppost.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httppost, true);
 		return (response.getStatusLine().getStatusCode() == 201);
 	}
 
 	/**
 	 * Method to accept a friendship request. Require login.
 	 * 
 	 * @param friendshipId
 	 *            Identification code of the friendship request
 	 * @return {@code true} if friendship request ends successfully, {@code
 	 *         false} otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean acceptFriendship(int friendshipId)
 			throws ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH
 				+ FRIENDSHIPS_PATH + "/" + Integer.toString(friendshipId)
 				+ "?accept=true");
 		HttpPut httpput = new HttpPut(uriBuilder.toString());
 		httpput.addHeader("Accept", "application/json");
 		httpput.addHeader("Content-type", "application/json");
 
 		@SuppressWarnings("unused")
 		HttpResponse response = executeHttpRequest(httpput, true);
 		return (response.getStatusLine().getStatusCode() == 200);
 	}
 
 	/**
 	 * Method to refuse a friendship request. Require login.
 	 * 
 	 * @param friendshipId
 	 *            Identification code of the friendship request
 	 * @return {@code true} if friendship request ends successfully, {@code
 	 *         false} otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean refuseFriendship(int friendshipId)
 			throws ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH
 				+ FRIENDSHIPS_PATH + "/" + Integer.toString(friendshipId));
 		HttpPut httpput = new HttpPut(uriBuilder.toString());
 		httpput.addHeader("Accept", "application/json");
 		httpput.addHeader("Content-type", "application/json");
 
 		HttpResponse response = executeHttpRequest(httpput, true);
 		return (response.getStatusLine().getStatusCode() == 200);
 	}
 
 	/**
 	 * Method to remove a friend from the current user band. Require login.
 	 * 
 	 * @param userId
 	 *            Identification code of the user
 	 * @return {@code true} if remove request ends successfully, {@code false}
 	 *         otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean removeUserFromBand(int userId)
 			throws ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH
 				+ FRIENDSHIPS_PATH + "/" + Integer.toString(userId));
 		HttpDelete httpdelete = new HttpDelete(uriBuilder.toString());
 		httpdelete.addHeader("Accept", "application/json");
 		httpdelete.addHeader("Content-type", "application/json");
 
 		HttpResponse response = executeHttpRequest(httpdelete, true);
 		return (response.getStatusLine().getStatusCode() == 200);
 	}
 
 	/**
 	 * Method for retrieve informations about a thound. Requires login only if
 	 * requesting private (must be thound owner) or contacts (must be friend of
 	 * thound owner) thounds.
 	 * 
 	 * @param thoundId
 	 *            Identification code of the thound
 	 * @return A {@link ThoundWrapper} object that contain the informations
 	 *         about the selected thound
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static ThoundWrapper loadThounds(int thoundId)
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + THOUNDS_PATH + "/"
 				+ Integer.toString(thoundId));
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httpget, isLogged);
 		return new ThoundWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method for retrieve informations about a thound. Requires login only if
 	 * requesting private (must be thound owner) or contacts (must be friend of
 	 * thound owner) thounds.
 	 * 
 	 * @param thoundHash
 	 *            Hash code of the thound
 	 * @return A {@link ThoundWrapper} object that contain the informations
 	 *         about the selected thound
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static ThoundWrapper loadThounds(String thoundHash)
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + THOUNDS_PATH + "/"
 				+ thoundHash);
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httpget, isLogged);
 		return new ThoundWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method to remove a thound. Require login.
 	 * 
 	 * @param thoundId
 	 * @return {@code true} if remove request ends successfully, {@code false}
 	 *         otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean removeThound(int thoundId)
 			throws ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + THOUNDS_PATH + "/"
 				+ Integer.toString(thoundId));
 		HttpDelete httpdelete = new HttpDelete(uriBuilder.toString());
 		httpdelete.addHeader("Accept", "application/json");
 		httpdelete.addHeader("Content-type", "application/json");
 
 		HttpResponse response = executeHttpRequest(httpdelete, true);
 		return (response.getStatusLine().getStatusCode() == 200);
 	}
 
 	/**
 	 * Method for retrieve the user's notifications. Require login.
 	 * 
 	 * @return {@link NotificationsWrapper} object that contains the user
 	 *         notifications
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 * @throws IllegalThoundsObjectException
 	 *             in case the retrieved object is broken
 	 */
 	public static NotificationsWrapper loadNotifications()
 			throws ThoundsConnectionException, IllegalThoundsObjectException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + PROFILE_PATH
 				+ NOTIFICATIONS_PATH);
 		HttpGet httpget = new HttpGet(uriBuilder.toString());
 		httpget.addHeader("Accept", "application/json");
 		HttpResponse response = executeHttpRequest(httpget, true);
 		return new NotificationsWrapper(httpResponseToJSONObject(response));
 	}
 
 	/**
 	 * Method to perform registration to Thounds.
 	 * 
 	 * @param name
 	 *            user full name
 	 * @param mail
 	 *            user email (used to login)
 	 * @param country
 	 *            user country
 	 * @param city
 	 *            user city
 	 * @param tags
 	 *            tags associated (instruments, genres, etc.)
 	 * @return {@code true} if user registration ends successfully, {@code
 	 *         false} otherwise
 	 * @throws ThoundsConnectionException
 	 *             in case the connection was aborted
 	 */
 	public static boolean registrateUser(String name, String mail,
 			String country, String city, String tags)
 			throws ThoundsConnectionException {
 
 		JSONObject userJSON = new JSONObject();
 		JSONObject userFieldJSON = new JSONObject();
 		try {
 			userFieldJSON.put("name", name);
 			userFieldJSON.put("email", mail);
 			userFieldJSON.put("country", country);
 			userFieldJSON.put("city", city);
 			userFieldJSON.put("tags", tags);
 			userJSON.put("user", userFieldJSON);
 		} catch (JSONException e) {
 			throw new RuntimeException("user JSONObject creation error");
 		}
 		StringBuilder uriBuilder = new StringBuilder(HOST + USERS_PATH);
 		HttpPost httppost = new HttpPost(uriBuilder.toString());
 		httppost.addHeader("Accept", "application/json");
 		httppost.addHeader("Content-type", "application/json");
 		StringEntity se = null;
 		try {
 			se = new StringEntity(userJSON.toString());
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(
 					"JSONObject to StringEntity conversion error");
 		}
 		httppost.setEntity(se);
 		HttpResponse response = executeHttpRequest(httppost, false);
 		return (response.getStatusLine().getStatusCode() == 201);
 	}
 
 	/**
 	 * 
 	 * @param title thound's title
 	 * @param tags
 	 * @param delay
 	 * @param offset
 	 * @param duration
 	 * @param lat
 	 * @param lng
 	 * @param thoundPath
 	 * @param coverPath
 	 * @return
 	 * @throws ThoundsConnectionException
 	 */
 	public static boolean createThound(String title, String tags, int delay,
 			int offset, int duration, double lat, double lng,
 			String thoundPath, String coverPath)
 			throws ThoundsConnectionException {
 
 		JSONObject thoundJSON = new JSONObject();
 		JSONObject thoundFieldJSON = new JSONObject();
 		try {
 			String encodedThound = null;
 			String encodedCover = null;
 			if (thoundPath != null && !thoundPath.equals(""))
 				encodedThound = Base64Encoder.Encode(thoundPath);
 			if (coverPath != null && !coverPath.equals(""))
 				encodedCover = Base64Encoder.Encode(coverPath);
 			thoundFieldJSON.put("title", title);
 			thoundFieldJSON.put("tag_list", tags);
 			thoundFieldJSON.put("lat", lat);
 			thoundFieldJSON.put("lng", lng);
 			thoundFieldJSON.put("delay", delay);
 			thoundFieldJSON.put("duration", duration);
 			thoundFieldJSON.put("offset", offset);
 			thoundFieldJSON.put("thoundfile", encodedThound);
 			thoundFieldJSON.put("coverfile", encodedCover);
 			thoundJSON.put("track", thoundFieldJSON);
 		} catch (JSONException e) {
 			throw new RuntimeException("user JSONObject creation error");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		StringBuilder uriBuilder = new StringBuilder(HOST + TRACK_PATH);
 		HttpPost httppost = new HttpPost(uriBuilder.toString());
 		httppost.addHeader("Accept", "application/json");
 		httppost.addHeader("Content-type", "application/json");
 		StringEntity se = null;
 		try {
 			se = new StringEntity(thoundJSON.toString());
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(
 					"JSONObject to StringEntity conversion error");
 		}
 		httppost.setEntity(se);
 		HttpResponse response = executeHttpRequest(httppost, true);
 		return (response.getStatusLine().getStatusCode() == 201);
 	}
 
 	/**
 	 * 
 	 * @param thound_id
 	 * @param title
 	 * @param tags
 	 * @param delay
 	 * @param offset
 	 * @param duration
 	 * @param lat
 	 * @param lng
 	 * @param thoundPath
 	 * @param coverPath
 	 * @return
 	 * @throws ThoundsConnectionException
 	 */
 	public static boolean createTrack(int thound_id, String title, String tags,
 			int delay, int offset, int duration, double lat, double lng,
 			String thoundPath, String coverPath)
 			throws ThoundsConnectionException {
 
 		JSONObject thoundJSON = new JSONObject();
 		JSONObject thoundFieldJSON = new JSONObject();
 		try {
 			String encodedThound = null;
 			String encodedCover = null;
 			if (thoundPath != null && !thoundPath.equals(""))
 				encodedThound = Base64Encoder.Encode(thoundPath);
 			if (coverPath != null && !coverPath.equals(""))
 				encodedCover = Base64Encoder.Encode(coverPath);
 			thoundFieldJSON.put("title", title);
 			thoundFieldJSON.put("tag_list", tags);
 			thoundFieldJSON.put("lat", lat);
 			thoundFieldJSON.put("lng", lng);
 			thoundFieldJSON.put("delay", delay);
 			thoundFieldJSON.put("duration", duration);
 			thoundFieldJSON.put("offset", offset);
 			thoundFieldJSON.put("thoundfile", encodedThound);
 			thoundFieldJSON.put("coverfile", encodedCover);
 			thoundJSON.put("track", thoundFieldJSON);
 		} catch (JSONException e) {
 			throw new RuntimeException("user JSONObject creation error");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		StringBuilder uriBuilder = new StringBuilder(HOST + TRACK_PATH
 				+ "?thound_id=" + Integer.toString(thound_id));
 		HttpPost httppost = new HttpPost(uriBuilder.toString());
 		httppost.addHeader("Accept", "application/json");
 		httppost.addHeader("Content-type", "application/json");
 		StringEntity se = null;
 		try {
 			se = new StringEntity(thoundJSON.toString());
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(
 					"JSONObject to StringEntity conversion error");
 		}
 		httppost.setEntity(se);
 		HttpResponse response = executeHttpRequest(httppost, true);
 		return (response.getStatusLine().getStatusCode() == 201);
 	}
 
 	/**
 	 * 
 	 * @param thoundId
 	 * @return
 	 * @throws ThoundsConnectionException
 	 */
 	public static boolean removeTrackNotification(int thoundId)
 			throws ThoundsConnectionException {
 		StringBuilder uriBuilder = new StringBuilder(HOST + TRACK_NOTIFICATIONS_PATH + "/"
 				+ Integer.toString(thoundId));
 		HttpDelete httpdelete = new HttpDelete(uriBuilder.toString());
 		httpdelete.addHeader("Accept", "application/json");
 		httpdelete.addHeader("Content-type", "application/json");
 
 		HttpResponse response = executeHttpRequest(httpdelete, true);
 		return (response.getStatusLine().getStatusCode() == 200);
 	}
 }
