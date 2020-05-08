 /*
  * Licensed to Scoreflex (www.scoreflex.com) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. Scoreflex licenses this
  * file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package com.scoreflex;
 
 import java.io.UnsupportedEncodingException;
 import java.net.SocketException;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.http.Header;
 import org.apache.http.NoHttpResponseException;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.message.BasicHeader;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Handler;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Base64;
 import android.util.Log;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.scoreflex.Scoreflex.Response;
 
 /**
  * A REST client that lets you hit the Scoreflex REST server.
  *
  */
 class ScoreflexRestClient {
 
 	private enum HttpMethod {
 		GET, PUT, POST, DELETE
 	}
 
 	private static final int RETRY_INTERVAL = 1000; //60000;
 	private static final String ACCESS_TOKEN_PREF_NAME = "__scoreflex_access_token";
 	private static final String ACCESS_TOKEN_IS_ANONYMOUS_PREF_NAME = "__scoreflex_access_token_is_anonymous";
 	private static final String SID_PREF_NAME = "__scoreflex_sid";
 	private static final String PLAYER_ID_PREF_NAME = "__player_id";
 	private static boolean sIsFetchingAnonymousAccessToken = false;
 	private static List<Scoreflex.ResponseHandler> sPendingHandlers = new ArrayList<Scoreflex.ResponseHandler>();
 
 	private static AsyncHttpClient sClient = new AsyncHttpClient();
 
 	/**
 	 * A GET request
 	 *
 	 * @param resource
 	 *          The resource path, starting with /
 	 * @param params
 	 *          AsyncHttpClient request parameters
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler
 	 */
 	protected static void get(String resource, Scoreflex.RequestParams params,
 			Scoreflex.ResponseHandler responseHandler) {
 		requestAuthenticated(new Request(HttpMethod.GET, resource, params,
 				responseHandler));
 	}
 
 	/**
 	 * A POST request
 	 *
 	 * @param resource
 	 *          The resource path, starting with /
 	 * @param params
 	 *          AsyncHttpClient request parameters
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler
 	 */
 	protected static void post(String resource, Scoreflex.RequestParams params,
 			Scoreflex.ResponseHandler responseHandler) {
 		requestAuthenticated(new Request(HttpMethod.POST, resource, params,
 				responseHandler));
 	}
 
 	/**
 	 * A POST request that is guaranteed to be executed when a network connection
 	 * is present, surviving application reboot. The responseHandler will be
 	 * called only if the network is present when the request is first run.
 	 *
 	 * @param resource
 	 * @param params
 	 * @param responseHandler
 	 */
 	protected static void postEventually(String resource,
 			Scoreflex.RequestParams params,
 			final Scoreflex.ResponseHandler responseHandler) {
 
 		// Create a request
 		final Request request = new Request(HttpMethod.POST, resource, params, null);
 
 		// Wrap the provided handler with ours
 		request.setHandler(new Scoreflex.ResponseHandler() {
 
 			@Override
 			public void onFailure(Throwable e, Response errorResponse) {
 
 				// Post to vault on network error
 				if (e instanceof NoHttpResponseException
 						|| e instanceof UnknownHostException
 						|| e instanceof SocketException) {
 					try {
 						ScoreflexRequestVault.getDefaultVault().put(request);
 					} catch (JSONException e1) {
 						Log.e("Scoreflex", "Could not save request to vault", e1);
 					}
 					return;
 				}
 
 				// Forward to original handler otherwise
 				if (null != responseHandler)
 					responseHandler.onFailure(e, errorResponse);
 			}
 
 			@Override
 			public void onSuccess(Response response) {
 				if (null != responseHandler)
 					responseHandler.onSuccess(response);
 			}
 
 			@Override
 			public void onSuccess(int statusCode, Response response) {
 				if (null != responseHandler)
 					responseHandler.onSuccess(statusCode, response);
 			}
 
 		});
 		requestAuthenticated(request);
 
 	}
 
 	/**
 	 * A PUT request
 	 *
 	 * @param resource
 	 *          The resource path, starting with /
 	 * @param params
 	 *          AsyncHttpClient request parameters
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler
 	 */
 	protected static void put(String resource, Scoreflex.RequestParams params,
 			Scoreflex.ResponseHandler responseHandler) {
 		requestAuthenticated(new Request(HttpMethod.PUT, resource, params,
 				responseHandler));
 	}
 
 	/**
 	 * A DELETE request
 	 *
 	 * @param resource
 	 *          The resource path, starting with /
 	 * @param params
 	 *          AsyncHttpClient request parameters
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler
 	 */
 	protected static void delete(String resource,
 			Scoreflex.ResponseHandler responseHandler) {
 		requestAuthenticated(new Request(HttpMethod.DELETE, resource, null,
 				responseHandler));
 	}
 
 	/**
 	 * If no access token is found in the user's preferences, fetch an anonymous
 	 * access token
 	 *
 	 * @param onFetchedHandler
 	 *          a handler called if a request to fetch an access token has been
 	 *          executed successfully, never called if retreived from cache
 	 * @return whether or not a request has been executed to fetch an anonymous
 	 *         access token (true fetching, false retrived from local cache)
 	 */
 	protected static boolean fetchAnonymousAccessTokenIfNeeded(
 			Scoreflex.ResponseHandler onFetchedHandler) {
 		if (null == getAccessToken()) {
 			fetchAnonymousAccessToken(onFetchedHandler);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * If no access token is found in the user's preferences, fetch an anonymous
 	 * access token
 	 */
 	protected static void fetchAnonymousAccessTokenIfNeeded() {
 		fetchAnonymousAccessTokenIfNeeded(null);
 	}
 
 	/**
 	 * Runs the specified request and ensure a valid access token is fetched if
 	 * neccessary beforehand or afterwards (and re-run the request) if the request
 	 * fails for auth reasons
 	 *
 	 * @param request
 	 */
 	protected static void requestAuthenticated(final Request request) {
 		if (null == request)
 			return;
 
 		String accessToken = getAccessToken();
 
 		// User is authenticated
 		if (null != accessToken) {
 
 			// Add the access token to the params
 			Scoreflex.RequestParams params = request.getParams();
 			if (null == params) {
 				params = new Scoreflex.RequestParams();
 				request.setParams(params);
 			}
 
 			params.remove("accessToken");
 			params.put("accessToken", accessToken);
 
 			// Wrap the request handler with our own
 			Scoreflex.ResponseHandler wrapperHandler = new Scoreflex.ResponseHandler() {
 
 				@Override
 				public void onSuccess(int status, Scoreflex.Response response) {
 					if (null != request.getHandler())
 						request.getHandler().onSuccess(status, response);
 				}
 
 				@Override
 				public void onFailure(Throwable e, Scoreflex.Response errorResponse) {
 					Log.e("Scoreflex", "Request failed", e);
 					if (null != errorResponse
 							&& Scoreflex.ERROR_INVALID_ACCESS_TOKEN == errorResponse
 									.getErrorCode()) {
 
 						// null out the access token
 						setAccessToken(null, true);
 						setSID(null);
 						setPlayerId(null);
 
 						// retry in 60 secs
 						new Handler().postDelayed(new Runnable() {
 
 							@Override
 							public void run() {
 								requestAuthenticated(request);
 
 							}
 						}, RETRY_INTERVAL);
 
 					} else {
 						if (null == request.getHandler())
 							return;
 						// if (e instanceof ConnectTimeoutException)
 						request.getHandler().onFailure(e, errorResponse);
 					}
 				}
 
 				@Override
 				public void onSuccess(Response response) {
 
 				}
 			};
 			Request wrapperRequest = (Request) request.clone();
 			wrapperRequest.setHandler(wrapperHandler);
 
 			// Perform request
 			request(wrapperRequest);
 			return;
 		}
 
 		// User is not authenticated
 		// request a token
 		fetchAnonymousAccessTokenAndRunRequest(request);
 	}
 
 	/**
 	 * Thin wrapper to the {@link AsyncHttpClient} library
 	 *
 	 * @param request
 	 */
 	private static void request(final Request request) {
 
 		if (null == request) {
 			Log.e("Scoreflex", "Request with null request.");
 			return;
 		}
 
 		// Decorate parameters
 		ScoreflexRequestParamsDecorator.decorate(request.getResource(),
 				request.getParams());
 
 		// Generate signature
 		Header authorizationHeader = request.getAuthorizationHeader();
 
 		// Headers
 		Header[] headers = null;
 		if (null != authorizationHeader) {
 			headers = new Header[1];
 			headers[0] = authorizationHeader;
 		}
 
 		// Handler
 		JsonHttpResponseHandler jsonHandler = null;
 		if (null != request.getHandler()) {
 			jsonHandler = new JsonHttpResponseHandler() {
 
 				@Override
 				public void onFailure(Throwable arg0, JSONObject arg1) {
 					if (arg1 != null) {
 						if (Scoreflex.showDebug) {
 							Log.d("Scoreflex", "Requesting Error: " + arg1);
 						}
 						Scoreflex.setNetworkAvailable(true);
 						request.getHandler().onFailure(arg0, new Scoreflex.Response(arg1));
 					} else {
 						Scoreflex.setNetworkAvailable(false);
 						request.getHandler().onFailure(arg0, null);
 					}
 				}
 
 				@Override
 				public void onFailure(Throwable arg0, String arg1) {
 					Scoreflex.setNetworkAvailable(false);
 					request.getHandler().onFailure(arg0, null);
 				}
 
 				@Override
 				public void onSuccess(int arg0, JSONObject arg1) {
 					Scoreflex.setNetworkAvailable(true);
 					request.getHandler().onSuccess(arg0, new Scoreflex.Response(arg1));
 				}
 			};
 		}
 
 		String url = ScoreflexUriHelper.getAbsoluteUrl(request.getResource());
 		if (Scoreflex.showDebug) {
 			Log.d("Scoreflex", "requesting url["+request.getMethod()+"]: "+ url + "?" + request.getParams().getURLEncodedString());
 		}
 		// TODO: support other contentTypes such as "application/json"
 		String contentType = "application/x-www-form-urlencoded";
 		switch (request.getMethod()) {
 		case GET:
 			sClient.get(null, url, headers, request.getParams(), jsonHandler);
 			break;
 		case PUT:
 			sClient.put(null, url, headers, request.getParams() != null ? request
 					.getParams().getEntity() : null, contentType, jsonHandler);
 			break;
 		case POST:
 			sClient.post(null, url, headers, request.getParams(), contentType,
 					jsonHandler);
 			break;
 		case DELETE:
 			sClient.delete(null, url, headers, jsonHandler);
 			break;
 		}
 	}
 
 	protected static void fetchAnonymousAccessToken(
 			final Scoreflex.ResponseHandler handler) {
 		fetchAnonymousAccessToken(handler, 0);
 	}
 
 	protected static void fetchAnonymousAccessToken(
 			final Scoreflex.ResponseHandler handler, final int nbRetries) {
 		if (sIsFetchingAnonymousAccessToken) {
 			queueHandler(handler);
 			return;
 		}
 		sIsFetchingAnonymousAccessToken = true;
 		Scoreflex.RequestParams authParams = new Scoreflex.RequestParams();
 		authParams.put("clientId", Scoreflex.getClientId());
 		authParams.put("devicePlatform", "Android");
 		authParams.put("deviceModel", Scoreflex.getDeviceModel());
 		String udid = Scoreflex.getUDID();
 		if (null != udid)
 			authParams.put("deviceId", udid);
 
 		String resource = "/oauth/anonymousAccessToken";
 
 		request(new Request(HttpMethod.POST, resource, authParams,
 				new Scoreflex.ResponseHandler() {
 
 					@Override
 					public void onFailure(Throwable e, Response errorResponse) {
 
 						if (nbRetries <= 0) {
 							Log.e("Scoreflex", "Error request anonymous access token (aborting):"
 									+ (errorResponse != null ? errorResponse.getJSONObject()
 											.toString() : " null error response, aborting"), e);
 
 							sIsFetchingAnonymousAccessToken = false;
 							if (null != handler) {
 								handler.onFailure(e, errorResponse);
 							}
 							Scoreflex.ResponseHandler chainedHandler = null;
 							while ((chainedHandler = dequeueHandler()) != null) {
 								chainedHandler.onFailure(e, errorResponse);
 							}
 							return;
 						}
 						Log.e("Scoreflex", "Error request anonymous access token (retrying : "+nbRetries+"):"
 								+ (errorResponse != null ? errorResponse.getJSONObject()
 										.toString() : " null error response, retrying"), e);
 
 						new Handler().postDelayed(new Runnable() {
 
 							@Override
 							public void run() {
 								sIsFetchingAnonymousAccessToken = false;
 								fetchAnonymousAccessToken(handler, nbRetries -1);
 							}
 						}, RETRY_INTERVAL);
 					}
 
 					@Override
 					public void onSuccess(int statusCode, Response response) {
 						// Parse response
 						JSONObject json = response.getJSONObject();
 						if (json.has("accessToken") && json.has("sid")) {
 							JSONObject accessToken = json.optJSONObject("accessToken");
 							String sid = json.optString("sid");
 							JSONObject meObject = json.optJSONObject("me");
 							String playerId = meObject.optString("id");
 							if (null != accessToken && accessToken.has("token")) {
 								String token = accessToken.optString("token");
 
 								// Store access token
 								setAccessToken(token, true);
 								setSID(sid);
 								setPlayerId(playerId);
 								sIsFetchingAnonymousAccessToken = false;
 
 								Intent intent = new Intent(Scoreflex.INTENT_USER_LOGED_IN);
 								intent.putExtra(Scoreflex.INTENT_USER_LOGED_IN_EXTRA_SID, sid);
 								intent.putExtra(
 										Scoreflex.INTENT_USER_LOGED_IN_EXTRA_ACCESS_TOKEN, token);
 								LocalBroadcastManager.getInstance(
 										Scoreflex.getApplicationContext()).sendBroadcast(intent);
 
 								// call handlers
 								if (null != handler) {
 									handler.onSuccess(statusCode, response);
 								}
 								Scoreflex.ResponseHandler chainedHandler = null;
 								while ((chainedHandler = dequeueHandler()) != null) {
 									chainedHandler.onSuccess(statusCode, response);
 								}
 								return;
 							}
 						}
 						Log.e("Scoreflex",
 								"Could not obtain anonymous access token from server");
 
 					}
 
 					@Override
 					public void onSuccess(Response response) {
 
 					}
 
 				}));
 
 	}
 
 	/**
 	 * Fetches an anonymous access token and run the given request with that
 	 * token. Retries when access token cannot be fetched.
 	 *
 	 * @param request
 	 *          The request to be run
 	 */
 	public static void fetchAnonymousAccessTokenAndRunRequest(
 			final Request request) {
 		fetchAnonymousAccessToken(new Scoreflex.ResponseHandler() {
 
 			@Override
 			public void onSuccess(Response response) {
 				requestAuthenticated(request);
 			}
 
 			@Override
 			public void onFailure(Throwable e, Response errorResponse) {
 
 			}
 		});
 	}
 
 	private static void queueHandler(Scoreflex.ResponseHandler handler) {
 		if (null == handler) {
 			return;
 		}
 
 		synchronized (sPendingHandlers) {
 			sPendingHandlers.add(handler);
 		}
 	}
 
 	private static Scoreflex.ResponseHandler dequeueHandler() {
 		Scoreflex.ResponseHandler handler = null;
 		synchronized (sPendingHandlers) {
 			if (sPendingHandlers.size() > 0) {
 				handler = sPendingHandlers.get(0);
 				if (null != handler) {
 					sPendingHandlers.remove(0);
 				}
 			}
 		}
 		return handler;
 	}
 
 	/**
 	 * Get the access token stored in the user's shared preferences.
 	 *
 	 * @return
 	 */
 
 	protected static String getAccessToken() {
 		SharedPreferences prefs = Scoreflex.getSharedPreferences();
 		if (null == prefs) {
 			return null;
 		}
 		String token = prefs.getString(ACCESS_TOKEN_PREF_NAME, null);
 		return token;
 	}
 
 	/**
 	 * Is the access token stored in the user's shared preferences anonymous ?
 	 *
 	 * @return
 	 */
 
 	protected static boolean getAccessTokenIsAnonymous() {
 		SharedPreferences prefs = Scoreflex.getSharedPreferences();
 		if (null == prefs) {
 			return true;
 		}
 		return prefs.getBoolean(ACCESS_TOKEN_IS_ANONYMOUS_PREF_NAME, true);
 	}
 
 	/**
 	 * Set the SID stored in the user's shared preferences.
 	 *
 	 * @param accessToken
 	 *          The access token to be stored
 	 */
 	protected static void setSID(String sid) {
 
 		SharedPreferences preferences = Scoreflex.getSharedPreferences();
 		SharedPreferences.Editor editor = preferences.edit();
 		if (null == sid)
 			editor.remove(SID_PREF_NAME);
 		else
 			editor.putString(SID_PREF_NAME, sid);
 		editor.commit();
 
 	}
 
 	protected static void setPlayerId(String playerId) {
 		SharedPreferences preferences = Scoreflex.getSharedPreferences();
 		SharedPreferences.Editor editor = preferences.edit();
 		if (null == playerId)
 			editor.remove(PLAYER_ID_PREF_NAME);
		editor.putString(PLAYER_ID_PREF_NAME, playerId);
 		editor.commit();
 
 	}
 
 	protected static String getPlayerId(Context applicationContext) {
 		SharedPreferences prefs = Scoreflex
 				.getSharedPreferences(applicationContext);
 		if (null == prefs) {
 			return null;
 		}
 		String playerId = prefs.getString(PLAYER_ID_PREF_NAME, null);
 		return playerId;
 
 	}
 
 	protected static String getPlayerId() {
 		SharedPreferences prefs = Scoreflex.getSharedPreferences();
 		if (null == prefs) {
 			return null;
 		}
 		String playerId = prefs.getString(PLAYER_ID_PREF_NAME, null);
 		return playerId;
 
 	}
 
 	/**
 	 * Get the access token stored in the user's shared preferences.
 	 *
 	 * @return
 	 */
 
 	protected static String getSID() {
 		SharedPreferences prefs = Scoreflex.getSharedPreferences();
 		if (null == prefs) {
 			return null;
 		}
 		String sid = prefs.getString(SID_PREF_NAME, null);
 		return sid;
 	}
 
 	/**
 	 * Set the access token stored in the user's shared preferences.
 	 *
 	 * @param accessToken
 	 *          The access token to be stored
 	 * @param isAnonymous
 	 *          Is this access token anonymous
 	 */
 	protected static void setAccessToken(String accessToken, boolean isAnonymous) {
 		SharedPreferences preferences = Scoreflex.getSharedPreferences();
 		SharedPreferences.Editor editor = preferences.edit();
 		if (null == accessToken) {
 			editor.remove(ACCESS_TOKEN_PREF_NAME);
 			editor.remove(ACCESS_TOKEN_IS_ANONYMOUS_PREF_NAME);
 		} else {
 			editor.putString(ACCESS_TOKEN_PREF_NAME, accessToken);
 			editor.putBoolean(ACCESS_TOKEN_IS_ANONYMOUS_PREF_NAME, isAnonymous);
 		}
 		editor.commit();
 
 	}
 
 	/**
 	 * A serializable object that represents a request to the Scoreflex API.
 	 *
 	 *
 	 */
 
 	protected static class Request implements Cloneable {
 		HttpMethod mMethod;
 		Scoreflex.RequestParams mParams;
 		Scoreflex.ResponseHandler mHandler;
 		String mResource;
 
 		public Request(HttpMethod method, String resource,
 				Scoreflex.RequestParams params, Scoreflex.ResponseHandler handler) {
 			mMethod = method;
 			mParams = params;
 			mHandler = handler;
 			mResource = resource;
 		}
 
 		public Request(JSONObject data) throws JSONException {
 			mMethod = HttpMethod.values()[data.getInt("method")];
 			mResource = data.getString("resource");
 			JSONObject paramsJson = data.getJSONObject("params");
 			mParams = new Scoreflex.RequestParams();
 			@SuppressWarnings("unchecked")
 			Iterator<String> keys = paramsJson.keys();
 			while (keys.hasNext()) {
 				String key = keys.next();
 				mParams.put(key, paramsJson.getString(key));
 			}
 		}
 
 		public JSONObject toJSON() throws JSONException {
 			JSONObject result = new JSONObject();
 			result.put("method", mMethod.ordinal());
 			result.put("resource", mResource);
 			JSONObject params = new JSONObject();
 			if (null != mParams)
 				for (String key : mParams.getParamNames())
 					params.put(key, mParams.getParamValue(key));
 			result.put("params", params);
 			return result;
 		}
 
 		public HttpMethod getMethod() {
 			return mMethod;
 		}
 
 		public Scoreflex.RequestParams getParams() {
 			return mParams;
 		}
 
 		public Scoreflex.ResponseHandler getHandler() {
 			return mHandler;
 		}
 
 		public String getResource() {
 			return mResource;
 		}
 
 		public void setMethod(HttpMethod mMethod) {
 			this.mMethod = mMethod;
 		}
 
 		public void setParams(Scoreflex.RequestParams mParams) {
 			this.mParams = mParams;
 		}
 
 		public void setHandler(Scoreflex.ResponseHandler mHandler) {
 			this.mHandler = mHandler;
 		}
 
 		public void setResource(String resource) {
 			this.mResource = resource;
 		}
 
 		@Override
 		protected Object clone() {
 			return new Request(mMethod, mResource, mParams, mHandler);
 		}
 
 		/**
 		 * Generates X-Scoreflex-Authorization header with request signature
 		 *
 		 * @return The authorization header or null for GET requests
 		 */
 		@SuppressLint("DefaultLocale")
 		protected BasicHeader getAuthorizationHeader() {
 			try {
 				StringBuilder sb = new StringBuilder();
 
 				// Step 1: add HTTP method uppercase
 				switch (mMethod) {
 				case POST:
 					sb.append("POST");
 					break;
 				case PUT:
 					sb.append("PUT");
 					break;
 				case GET:
 					// No authorization header for GET requests
 					return null;
 				case DELETE:
 					sb.append("DELETE");
 					break;
 				}
 
 				sb.append('&');
 
 				// Step 2: add the URI
 				Uri uri = Uri.parse(mResource);
 
 				// Query string is stripped from resource
 				sb.append(encode(String.format("%s%s", Scoreflex.getBaseURL(),
 						uri.getEncodedPath())));
 
 				// Step 3: add URL encoded parameters
 				sb.append('&');
 				TreeSet<String> paramNames = new TreeSet<String>();
 
 				// Params from the URL
 				Scoreflex.RequestParams queryStringParams = QueryStringParser
 						.getRequestParams(uri.getQuery());
 				if (null != queryStringParams)
 					paramNames.addAll(queryStringParams.getParamNames());
 
 				// Params from the request
 				if (null != mParams)
 					paramNames.addAll(mParams.getParamNames());
 
 				if (paramNames.size() > 0) {
 
 					String last = paramNames.last();
 					for (String paramName : paramNames) {
 						String paramValue = null;
 
 						if (null != mParams)
 							paramValue = mParams.getParamValue(paramName);
 
 						if (null == paramValue && null != queryStringParams)
 							paramValue = queryStringParams.getParamValue(paramName);
 
 						sb.append(encode(String.format("%s=%s", encode(paramName),
 								encode(paramValue))));
 						if (!last.equals(paramName))
 							sb.append("%26");
 
 					}
 				}
 
 				// Step 4: add body
 				sb.append('&');
 				// TODO: add the body here when we support other content types
 				// than application/x-www-form-urlencoded
 				Mac mac = Mac.getInstance("HmacSHA1");
 				SecretKeySpec secret = new SecretKeySpec(Scoreflex.getClientSecret()
 						.getBytes("UTF-8"), mac.getAlgorithm());
 				mac.init(secret);
 				byte[] digest = mac.doFinal(sb.toString().getBytes());
 				String sig = Base64.encodeToString(digest, Base64.DEFAULT).trim();
 				String encodedSig = encode(sig.trim());
 				BasicHeader result = new BasicHeader("X-Scoreflex-Authorization",
 						String.format("Scoreflex sig=\"%s\", meth=\"0\"", encodedSig));
 				return result;
 			} catch (Exception e) {
 				Log.e("Scoreflex", "Could not generate signature", e);
 				return null;
 			}
 		}
 
 		private static String encode(String s) throws UnsupportedEncodingException {
 			return URLEncoder.encode(s, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
 		}
 
 		@Override
 		public String toString() {
 			String method = null;
 			switch (mMethod) {
 			case POST:
 				method = "POST";
 				break;
 			case PUT:
 				method = "PUT";
 				break;
 			case GET:
 				method = "GET";
 				break;
 			case DELETE:
 				method = "DELETE";
 			}
 			return String.format("%s %s", method, mResource);
 		}
 
 	}
 
 }
