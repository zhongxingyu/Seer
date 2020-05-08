 package com.appacitive.android.model;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.util.Log;
 
 import com.appacitive.android.callbacks.AppacitiveAuthenticationCallback;
 import com.appacitive.android.callbacks.AppacitiveSignUpCallback;
 import com.appacitive.android.util.AppacitiveRequestMethods;
 import com.appacitive.android.util.Constants;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * AppacitiveUser represent your app's users whose management API's are provided
  * out of the box. They are internally simply articles of an inbuilt schema user
  * with added features of authorization, authentication , location tracking and
  * third-party social integration.
  * 
  * @author Sandeep Dhull
  */
 
 public class AppacitiveUser extends AppacitiveObject {
 
 	public String mUserToken;
 	public static AppacitiveUser currentUser;
 
 	private AppacitiveUser() {
 		super("user");
 	}
 
 	/**
 	 * Set the signed in user as current user.
 	 * 
 	 * @param user
 	 *            AppacitiveUser current user.
 	 */
 	public static void setCurrentUser(AppacitiveUser user) {
 		currentUser = user;
 	}
 
 	/**
 	 * Returns the currently logged in user.
 	 * 
 	 * @return the current logged in user.
 	 */
 	public static AppacitiveUser getCurrentUser() {
 		return currentUser;
 	}
 
 	/**
 	 * Authenticate a user using username and password.
 	 * 
 	 * @param userName
 	 *            Existing user username.
 	 * @param password
 	 *            user password required for authentication.
 	 */
 	public static void authenticate(String userName, String password) {
 		authenticate(userName, password, null);
 	}
 
 	/**
 	 * Authenticate a user using username and password.
 	 * 
 	 * @param userName
 	 *            Existing user username.
 	 * @param password
 	 *            user password.
 	 * @param callback
 	 *            callback invoked when the authentication is successful or
 	 *            failed.
 	 */
 	public static void authenticate(final String userName,
 			final String password,
 			final AppacitiveAuthenticationCallback callback) {
 		final Appacitive appacitive = Appacitive.getInstance();
 		if (appacitive != null && appacitive.getSessionId() != null) {
 			BackgroundTask<Void> autenticateTask = new BackgroundTask<Void>() {
 				Map<String, Object> responseMap = null;
 				AppacitiveError error;
 
 				@Override
 				public Void run() {
 
 					HashMap<String, String> requestMap = new HashMap<String, String>();
 					requestMap.put("username", userName);
 					requestMap.put("password", password);
 
 					Gson gson = new Gson();
 					String requestParams = gson.toJson(requestMap);
 
 					String urlString = Constants.USER_URL + "authenticate";
 					try {
 						URL url = new URL(urlString);
 						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
 						connection.setRequestProperty("Content-Type","application/json");
 						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
 						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
 						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
 						connection.setDoOutput(true);
 
 						OutputStream os = connection.getOutputStream();
 						os.write((requestParams.toString()).getBytes());
 						os.close();
 
 						InputStream inputStream;
 						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
 							Log.w("TAG",
 									"Request failed "
 											+ connection.getResponseMessage());
 							error = new AppacitiveError();
 							error.setStatusCode(connection.getResponseCode() + "");
 							error.setMessage(connection.getResponseMessage());
 						} else {
 							inputStream = connection.getInputStream();
 							InputStreamReader reader = new InputStreamReader(inputStream);
 							BufferedReader bufferedReader = new BufferedReader(reader);
 
 							StringBuffer buffer = new StringBuffer();
 							String response;
 							while ((response = bufferedReader.readLine()) != null) {
 								buffer.append(response);
 							}
 
 							Type typeOfClass = new TypeToken<Map<String, Object>>() {}.getType();
 							responseMap = gson.fromJson(buffer.toString(),typeOfClass);
 
 							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
 							if (error == null) {
 								AppacitiveUser user = new AppacitiveUser();
 								user.setNewPropertyValue(responseMap);
 								AppacitiveUser.currentUser = user;
 							}
 							inputStream.close();
 						}
 						if (callback != null) {
 							if (error == null) {
 								callback.onSuccess();
 							} else {
 								callback.onFailure(error);
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			};
 			autenticateTask.execute();
 		} else {
 			Log.w("Appacitive",
 					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			AppacitiveError error = new AppacitiveError();
 			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			error.setStatusCode("8002");
 			if (callback != null) {
 				callback.onFailure(error);
 			}
 		}
 	}
 
 	/**
 	 * Authenticate the user using user's facebook credentials.
 	 * 
 	 * @param userAccessToken
 	 *            facebook user access token.
 	 */
 	public static void authenticateWithFacebook(String userAccessToken) {
 		authenticateWithFacebook(userAccessToken, null);
 	}
 
 	/**
 	 * Authenticate a user using user's facebook credentials.
 	 * 
 	 * @param userAccessToken
 	 *            facebook user access token.
 	 * @param callback
 	 *            callback invoked when the authentication is successful or
 	 *            failed.
 	 */
 	public static void authenticateWithFacebook(final String userAccessToken,
 			final AppacitiveAuthenticationCallback callback) {
 		final Appacitive appacitive = Appacitive.getInstance();
 		if (appacitive != null && appacitive.getSessionId() != null) {
 			BackgroundTask<Void> autenticateTask = new BackgroundTask<Void>() {
 				AppacitiveError error = null;
 				Map<String, Object> responseMap = null;
 
 				@Override
 				public Void run() {
 
 					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String, Object> requestMap = new HashMap<String, Object>();
 					requestMap.put("type", "facebook");
 					requestMap.put("accesstoken", userAccessToken);
					requestMap.put("createNew", true);
 					Gson gson = new Gson();
 					String requestParams = gson.toJson(requestMap);
 					try {
 						URL url = new URL(urlString);
 						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
 						connection.setRequestProperty("Content-Type","application/json");
 						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
 						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
 						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
 						connection.setDoOutput(true);
 						
 						OutputStream os = connection.getOutputStream();
 						os.write((requestParams.toString()).getBytes());
 						os.close();
 						InputStream inputStream;
 						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
 							Log.d("TAG",
 									"Request failed "
 											+ connection.getResponseMessage());
 							error = new AppacitiveError();
 							error.setStatusCode(connection.getResponseCode() + "");
 							error.setMessage(connection.getResponseMessage());
 						} else {
 							inputStream = connection.getInputStream();
 							InputStreamReader reader = new InputStreamReader(inputStream);
 							BufferedReader bufferedReader = new BufferedReader(reader);
 							StringBuffer buffer = new StringBuffer();
 							String response;
 							while ((response = bufferedReader.readLine()) != null) {
 								buffer.append(response);
 							}
 							Type typeOfClass = new TypeToken<Map<String, Object>>() {}.getType();
 							responseMap = gson.fromJson(buffer.toString(),typeOfClass);
 							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
 							inputStream.close();
 						}
 						if (callback != null) {
 							if (error == null) {
 								AppacitiveUser user = new AppacitiveUser();
 								user.setNewPropertyValue(responseMap);
 								callback.onSuccess();
 							} else {
 								callback.onFailure(error);
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			};
 			autenticateTask.execute();
 		} else {
 			Log.w("Appacitive",
 					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			AppacitiveError error = new AppacitiveError();
 			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			error.setStatusCode("8002");
 			if (callback != null) {
 				callback.onFailure(error);
 			}
 		}
 	}
 
 	/**
 	 * Authenticate a user using user's twitter credentials.
 	 * 
 	 * @param oauthToken
 	 *            Twitter oauthToken key.
 	 * @param oauthSecret
 	 *            Twitter oauthSecret key.
 	 * @param consumerKey
 	 *            Twitter consumer Key.
 	 * @param consumerSecret
 	 *            Twitter consumer secret key.
 	 */
 	public static void authenticateWithTwitter(String oauthToken,
 			String oauthSecret, String consumerKey, String consumerSecret) {
 		authenticateWithTwitter(oauthToken, oauthSecret, consumerKey,
 				consumerSecret, null);
 	}
 
 	/**
 	 * Authenticate a user using user's twitter credentials.
 	 * 
 	 * @param oauthToken
 	 *            Twitter oauthToken key.
 	 * @param oauthSecret
 	 *            Twitter oauthSecret key.
 	 * @param consumerKey
 	 *            Twitter consumer Key.
 	 * @param consumerSecret
 	 *            Twitter consumer secret key.
 	 * @param callback
 	 *            callback invoked when the authentication is successful or
 	 *            failed.
 	 */
 	public static void authenticateWithTwitter(final String oauthToken,
 			final String oauthSecret, final String consumerKey,
 			final String consumerSecret,
 			final AppacitiveAuthenticationCallback callback) {
 		final Appacitive appacitive = Appacitive.getInstance();
 		if (appacitive != null && appacitive.getSessionId() != null) {
 			BackgroundTask<Void> autenticateTask = new BackgroundTask<Void>() {
 				AppacitiveError error;
 				Map<String, Object> responseMap = null;
 
 				@Override
 				public Void run() {
 
 					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String, Object> requestMap = new HashMap<String, Object>();
 					requestMap.put("type", "twitter");
					requestMap.put("createNew", true);
 					requestMap.put("oauthtoken", oauthToken);
 					requestMap.put("oauthtokensecret", oauthSecret);
 					Gson gson = new Gson();
 					String requestParams = gson.toJson(requestMap);
 					try {
 						URL url = new URL(urlString);
 						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
 						connection.setRequestProperty("Content-Type","application/json");
 						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
 						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
 						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
 						connection.setDoOutput(true);
 
 						OutputStream os = connection.getOutputStream();
 						os.write((requestParams.toString()).getBytes());
 						os.close();
 
 						InputStream inputStream;
 						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
 							Log.d("TAG",
 									"Request failed "
 											+ connection.getResponseMessage());
 							error = new AppacitiveError();
 							error.setStatusCode(connection.getResponseCode() + "");
 							error.setMessage(connection.getResponseMessage());
 						} else {
 							inputStream = connection.getInputStream();
 							InputStreamReader reader = new InputStreamReader(inputStream);
 							BufferedReader bufferedReader = new BufferedReader(reader);
 							StringBuffer buffer = new StringBuffer();
 							String response;
 							while ((response = bufferedReader.readLine()) != null) {
 								buffer.append(response);
 							}
 							Type typeOfClass = new TypeToken<Map<String, Object>>() {}.getType();
 							responseMap = gson.fromJson(buffer.toString(),typeOfClass);
 							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
 							inputStream.close();
 						}
 						if (callback != null) {
 							if (error == null) {
 								AppacitiveUser user = new AppacitiveUser();
 								user.setNewPropertyValue(responseMap);
 								callback.onSuccess();
 							} else {
 								callback.onFailure(error);
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			};
 			autenticateTask.execute();
 		} else {
 			Log.w("Appacitive",
 					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			AppacitiveError error = new AppacitiveError();
 			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			error.setStatusCode("8002");
 			if (callback != null) {
 				callback.onFailure(error);
 			}
 		}
 	}
 
 	/**
 	 * Create a new user with the provided user details.
 	 * 
 	 * @param userDetails
 	 *            User details.
 	 * @param callback
 	 *            callback invoked when the signup is successful or failed.
 	 */
 	public static void createUser(final AppacitiveUserDetail userDetails,
 			final AppacitiveSignUpCallback callback) {
 
 		final Appacitive sharedObject = Appacitive.getInstance();
 		if (sharedObject != null && sharedObject.getSessionId() != null) {
 
 			BackgroundTask<Void> createTask = new BackgroundTask<Void>() {
 				AppacitiveError error;
 				Map<String, Object> responseMap = null;
 
 				@Override
 				public Void run() {
 
 					String urlString = Constants.USER_URL + "create";
 					String requestParams = userDetails.createRequestParams();
 					try {
 						URL url = new URL(urlString);
 						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
 						connection.setRequestProperty("Content-Type","application/json");
 						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
 						connection.setRequestProperty("Appacitive-Session",sharedObject.getSessionId());
 						connection.setRequestProperty("Appacitive-Environment",sharedObject.getEnvironment());
 						connection.setDoOutput(true);
 
 						OutputStream os = connection.getOutputStream();
 						os.write((requestParams.toString()).getBytes());
 						os.close();
 
 						InputStream inputStream;
 						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
 							Log.d("TAG",
 									"Request failed "
 											+ connection.getResponseMessage());
 							error = new AppacitiveError();
 							error.setStatusCode(connection.getResponseCode() + "");
 							error.setMessage(connection.getResponseMessage());
 						} else {
 							inputStream = connection.getInputStream();
 							InputStreamReader reader = new InputStreamReader(inputStream);
 							BufferedReader bufferedReader = new BufferedReader(reader);
 							StringBuffer buffer = new StringBuffer();
 							String response;
 							while ((response = bufferedReader.readLine()) != null) {
 								buffer.append(response);
 							}
 							Gson gson = new Gson();
 							Type typeOfClass = new TypeToken<Map<String, Object>>() {}.getType();
 							responseMap = gson.fromJson(buffer.toString(),typeOfClass);
 							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
 							inputStream.close();
 						}
 						if (callback != null) {
 							if (error == null) {
 								AppacitiveUser user = new AppacitiveUser();
 								user.setNewPropertyValue(responseMap);
 								callback.onSuccess(user);
 							} else {
 								callback.onFailure(error);
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			};
 			createTask.execute();
 		} else {
 			Log.w("Appacitive",
 					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			AppacitiveError error = new AppacitiveError();
 			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
 			error.setStatusCode("8002");
 			if (callback != null) {
 				callback.onFailure(error);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void setNewPropertyValue(Map<String, Object> map) {
 		Map<String, Object> userMap = (Map<String, Object>) map.get("user");
 		this.mObjectId = new Long((String) userMap.get("__id"));
 		this.mSchemaId = new Long((String) userMap.get("__schemaid"));
 		this.mCreatedBy = (String) userMap.get("__createdby");
 		this.mLastModifiedBy = (String) userMap.get("__lastmodifiedby");
 		this.mRevision = new Long((String) userMap.get("__revision"));
 		try {
 			this.mUTCDateCreated = fromResponse((String) userMap
 					.get("__utcdatecreated"));
 			this.mUTCLastUpdatedDate = fromResponse((String) userMap
 					.get("__utclastupdateddate"));
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		this.mAttributes = (Map<String, Object>) userMap.get("__attributes");
 		this.mTags = (List<String>) userMap.get("__tags");
 		this.mProperties = AppacitiveHelper.getProperties(userMap);
 		if (map.containsKey("token")) {
 			this.mUserToken = (String) map.get("token");
 		}
 	}
 
 	private Date fromResponse(String dateString) throws ParseException {
 		SimpleDateFormat formatter = new SimpleDateFormat(
 				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 		Date date = formatter.parse(dateString);
 		return date;
 	}
 
 	/**
 	 * Returns the string representation of the object.
 	 * 
 	 * @return String representation of the object.
 	 */
 	@Override
 	public String toString() {
 		return "AppacitiveUser [mUserToken=" + mUserToken + "]"
 				+ super.toString();
 	}
 }
