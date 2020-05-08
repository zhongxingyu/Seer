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
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 
 import com.scoreflex.facebook.ScoreflexFacebookWrapper;
 import com.scoreflex.facebook.ScoreflexFacebookWrapper.FacebookException;
 import com.scoreflex.google.ScoreflexGcmWrapper;
 import com.scoreflex.google.ScoreflexGoogleWrapper;
 import com.scoreflex.model.JSONParcelable;
 
 import android.content.res.Configuration;
 import org.OpenUDID.*;
 
 /**
  * The main class to access to Scoreflex
  *
  */
 public class Scoreflex {
 
 	private static Context sApplicationContext;
 	private static String sClientId;
 	private static String sClientSecret;
 	private static boolean sIsInitialized = false;
 	private static String sBaseURL;
 	private static String sLang;
 	private static Location sLocation;
 	private static boolean sIsReachable;
 	private static int sDefaultGravity = Gravity.BOTTOM;
 	private static WeakReference<ScoreflexView> mScoreflexView;
 	private static HashMap<String, ScoreflexView> mPreloadedViews;
 
 	protected static final String API_VERSION = "v1";
 
 	private static final String PRODUCTION_API_URL = "https://api.scoreflex.com/"
 			+ API_VERSION;
 	private static final String SANDBOX_API_URL = "https://sandbox.api.scoreflex.com/"
 			+ API_VERSION;
 
 	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
 
 	private static final String PREF_FILE = "scoreflex";
 
 	/**
 	 * the timeout for a webview request
 	 */
 	public static final int WEBVIEW_REQUEST_TOTAL_TIMEOUT = 10000;
 
 	protected static final int SUCCESS_LOGOUT = 200000;
 	protected static final int SUCCESS_CLOSE_WEBVIEW = 200001;
 	protected static final int SUCCESS_PLAY_LEVEL = 200002;
 	protected static final int SUCCESS_NEEDS_AUTH = 200003;
 	protected static final int SUCCESS_AUTH_GRANTED = 200004;
 	protected static final int SUCCESS_MOVE_TO_NEW_URL = 200005;
 	protected static final int SUCCESS_NEEDS_CLIENT_AUTH = 200006;
 	protected static final int SUCCESS_START_CHALLENGE = 200007;
 	protected static final int SUCCESS_LINK_SERVICE = 200008;
 	protected static final int SUCCESS_INVITE_WITH_SERVICE = 200009;
 	protected static final int SUCCESS_SHARE_WITH_SERVICE = 200010;
 
 	protected static final int ERROR_INVALID_PARAMETER = 10001;
 	protected static final int ERROR_MISSING_MANDATORY_PARAMETER = 10002;
 	protected static final int ERROR_INVALID_ACCESS_TOKEN = 11003;
 	protected static final int ERROR_SECURE_CONNECTION_REQUIRED = 10005;
 	protected static final int ERROR_INVALID_PREV_NEXT_PARAMETER = 12011;
 	protected static final int ERROR_INVALID_SID = 12017;
 	protected static final int ERROR_SANDBOX_URL_REQUIRED = 12018;
 	protected static final int ERROR_INACTIVE_GAME = 12019;
 	protected static final int ERROR_MISSING_PERMISSIONS = 12020;
 	protected static final int ERROR_PLAYER_DOES_NOT_EXIST = 12000;
 	protected static final int ERROR_DEVELOPER_DOES_NOT_EXIST = 12001;
 	protected static final int ERROR_GAME_DOES_NOT_EXIST = 12002;
 	protected static final int ERROR_LEADERBOARD_CONFIG_DOES_NOT_EXIST = 12004;
 	protected static final int ERROR_SERVICE_EXCEPTION = 12009;
 
 	/**
 	 * Intent extra key for GCM notification data when the user clicks the
 	 * notification.
 	 */
 	public static final String NOTIFICATION_EXTRA_KEY = "_sfxNotification";
 
 	/**
 	 * A push notification sent from the developer to the player (using
 	 * Scoreflex).
 	 */
 	public static final int NOTIFICATION_TYPE_DEVELOPER_TO_PLAYER = 1;
 
 	/**
 	 * A push notification sent from the player to another player (using your
 	 * game).
 	 */
 	public static final int NOTIFICATION_TYPE_PLAYER_TO_PLAYER = 2;
 
 	/**
 	 * A notification received when the player has been invited to a challenge.
 	 */
 	public static final int NOTIFICATION_TYPE_CHALLENGE_INVITATION = 100;
 
 	/**
 	 * A notification received when a challenge is ended.
 	 */
 	public static final int NOTIFICATION_TYPE_CHALLENGE_ENDED = 101;
 
 	/**
 	 * A notification received when it is the player's turn in a challenge.
 	 */
 	public static final int NOTIFICATION_TYPE_YOUR_TURN_IN_CHALLENGE = 102;
 
 	/**
 	 * A notification received when a friend of the player joins the game.
 	 */
 	public static final int NOTIFICATION_TYPE_FRIEND_JOINED_GAME = 103;
 
 	/**
 	 * A notification received when the player highscore has been beaten by a friend.
 	 */
 	public static final int NOTIFICATION_TYPE_FRIEND_BEAT_YOUR_HIGHSCORE = 104;
 
 	/**
 	 * A notification received when a player gets a new rating.
 	 */
 	public static final int NOTIFICATION_TYPE_PLAYER_LEVEL_CHANGED = 105;
 
 	/**
 	 * Local intent broadcasted when the game should load a challenge.
 	 */
 	public static final String INTENT_START_CHALLENGE = "scoreflexStartChallenge";
 
 	/**
 	 * The extra key for the full challenge instance (a JSONParcelable object)
 	 * in a {@link #INTENT_START_CHALLENGE} intent
 	 */
 	public static final String INTENT_START_CHALLENGE_EXTRA_INSTANCE = "challengeInstance";
 
 	/**
 	 * Local intent broadcasted when the reachability to Scoreflex has changed.
 	 */
 	public static final String INTENT_CONNECTIVITY_CHANGED = "scoreflexConnectivityChanged";
 
 	/**
 	 * The extra key for the reachability state in a
 	 * {@link #INTENT_CONNECTIVITY_CHANGED} intent.
 	 */
 	public static final String INTENT_CONNECTIVITY_EXTRA_CONNECTIVITY = "scoreflexConnectivityState";
 
 	/**
 	 * Local intent broadcasted when the Scoreflex sdk has been initialized and is
 	 * reachable.
 	 */
 	public static final String INTENT_SCOREFLEX_INTIALIZED = "scoreflexInitialized";
 
 	/**
 	 * Local intent broadcasted when a resource has been successfully preloaded.
 	 */
 	public static final String INTENT_RESOURCE_PRELOADED = "scoreflexResourcePreloaded";
 
 	/**
 	 * The extra key for the path of a preloaded resource in a
 	 * {@link #INTENT_RESOURCE_PRELOADED} intent.
 	 */
 	public static final String INTENT_RESOURCE_PRELOADED_EXTRA_PATH = "scoreflexResourcePreloadedPath";
 
 	/**
 	 * Local intent broadcasted when the current user has changed.
 	 */
 	public static final String INTENT_USER_LOGED_IN = "scoreflexUserLoggedIn";
 
 	/**
 	 * The extra key for the new SID of the player in a
 	 * {@link #INTENT_USER_LOGED_IN} intent.
 	 */
 	public static final String INTENT_USER_LOGED_IN_EXTRA_SID = "sid";
 
 	/**
 	 * The extra key for the new access token of the player in a
 	 * {@link #INTENT_USER_LOGED_IN} intent.
 	 */
 	public static final String INTENT_USER_LOGED_IN_EXTRA_ACCESS_TOKEN = "accessToken";
 
 	/**
 	 * Local intent broadcasted when the game should load a level.
 	 */
 	public static final String INTENT_PLAY_LEVEL = "scoreflexPlayLevel";
 
 	/**
 	 * The extra key for the leaderboard ID in a {@link #INTENT_PLAY_LEVEL} intent.
 	 */
 	public static final String INTENT_PLAY_LEVEL_EXTRA_LEADERBOARD_ID = "leaderboardId";
 
 	private static ConnectivityReceiver sConectivityReceiver = new ConnectivityReceiver();
 
 	protected static final String DEFAULT_LANGUAGE_CODE = "en";
 	protected static final String[] VALID_LANGUAGE_CODES = { "af", "ar", "be",
 			"bg", "bn", "ca", "cs", "da", "de", "el", "en", "en_GB", "en_US", "es",
 			"es_ES", "es_MX", "et", "fa", "fi", "fr", "fr_FR", "fr_CA", "he", "hi",
 			"hr", "hu", "id", "is", "it", "ja", "ko", "lt", "lv", "mk", "ms", "nb",
 			"nl", "pa", "pl", "pt", "pt_PT", "pt_BR", "ro", "ru", "sk", "sl", "sq",
 			"sr", "sv", "sw", "ta", "th", "tl", "tr", "uk", "vi", "zh", "zh_CN",
 			"zh_TW", "zh_HK", };
 	static final int FILECHOOSER_RESULTCODE = 0;
 	private static long playingSessionStart;
 
     /**
      * Checks if Scoreflex is initialized.
      *
 	 * @return True if the SDK is initialized.
      */
 	public static boolean isInitialized() {
 		return sIsInitialized;
 	}
 
 	/**
 	 * Initialize Scoreflex. Call this method before using Scoreflex. Will
 	 * initialize a production mode not sandbox
 	 *
 	 * @param clientId
 	 *          The clientId of your game.
 	 * @param clientSecret
 	 *          The clientSecret of your game.
 	 */
 	public static void initialize(Context context, String clientId,
 			String clientSecret) {
 		initialize(context, clientId, clientSecret, false);
 	}
 
 	/**
 	 * Initialize Scoreflex. Call this method before using Scoreflex.
 	 * A good place to initialize Scoreflex is in your main activity's onCreate method as follow
 	 * <pre>
 	 * <code>
 	 * protected void onCreate(Bundle savedInstance) {
 	 * 	Scoreflex.initialize(this, "clientId", "clientSecret", isSandbox);
 	 * }
 	 * </code>
 	 * </pre>
 	 *
 	 * @param clientId
 	 *          The clientId of your game.
 	 * @param clientSecret
 	 *          The clientSecret of your game.
 	 */
 	public static void initialize(Context context, final String clientId,
 			String clientSecret, boolean useSandbox) {
 		setNetworkAvailable(false);
 		sApplicationContext = context.getApplicationContext();
 		sClientId = clientId;
 		sClientSecret = clientSecret;
 		sBaseURL = useSandbox ? SANDBOX_API_URL : PRODUCTION_API_URL;
 		sIsInitialized = true;
 		// Initialize OpenUDID
 		OpenUDID_manager.sync(sApplicationContext);
 
 		// Wait for UDID to be ready and fetch anonymous token if needed.
 		new Runnable() {
 
 			@Override
 			public void run() {
 				if (OpenUDID_manager.isInitialized()) {
 					boolean isFetchingToken = ScoreflexRestClient
 							.fetchAnonymousAccessTokenIfNeeded(new ResponseHandler() {
 								@Override
 								public void onFailure(Throwable e, Response errorResponse) {
 								}
 
 								@Override
 								public void onSuccess(Response response) {
 									Intent broadcast = new Intent(INTENT_SCOREFLEX_INTIALIZED);
 									LocalBroadcastManager.getInstance(
 											Scoreflex.getApplicationContext()).sendBroadcast(
 											broadcast);
 								}
 							});
 					if (!isFetchingToken) {
 						// even if we have an access token, we need to ensure connectivity
 						// state
 						Scoreflex.get("/network/ping", null, new ResponseHandler() {
 							@Override
 							public void onFailure(Throwable e, Response errorResponse) {
 							}
 
 							@Override
 							public void onSuccess(Response response) {
 								Intent broadcast = new Intent(INTENT_SCOREFLEX_INTIALIZED);
 								LocalBroadcastManager.getInstance(
 										Scoreflex.getApplicationContext()).sendBroadcast(broadcast);
 							}
 						});
 					}
 				} else {
 					new Handler().postDelayed(this, 100);
 				}
 			}
 		}.run();
 
 		ScoreflexRequestVault.initialize();
 
 	}
 
 	/**
 	 * True if the SDK is running in sandbox mode.
 	 *
 	 * (@see {@link #initialize(Context, String, String, boolean) Sandbox}).
 	 * @return True if the SDK is running in sandbox mode.
 	 */
 	public static boolean usesSandbox() {
 		return SANDBOX_API_URL.equals(sBaseURL);
 	}
 
 	/**
 	 * Returns the base URL for the Scoreflex API. This is the URL used to prefix
 	 * every API resource path and might change depending if you're using
 	 * {@link #initialize(Context, String, String, boolean) Sandbox}.
 	 *
 	 * @return The base URL.
 	 */
 	public static String getBaseURL() {
 		return sBaseURL;
 	}
 
 	/**
 	 * Returns the base URL for the Scoreflex API with a <code>http:</code>
 	 * scheme.
 	 *
 	 * (@see {@link #getBaseURL()}).
 	 * @return The base URL.
 	 */
 	public static String getNonSecureBaseURL() {
 		return sBaseURL.replaceFirst("https:", "http:");
 	}
 
 	/**
 	 * A GET request.
 	 *
 	 * @param resource
 	 *          The resource path, starting with /.
 	 * @param params
 	 *          AsyncHttpClient request parameters.
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler.
 	 */
 	public static void get(String resource, Scoreflex.RequestParams params,
 			Scoreflex.ResponseHandler responseHandler) {
 		ScoreflexRestClient.get(resource, params, responseHandler);
 	}
 
 	/**
 	 * A POST request.
 	 *
 	 * @param resource
 	 *          The resource path, starting with /.
 	 * @param params
 	 *          AsyncHttpClient request parameters.
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler.
 	 */
 	public static void post(String resource, Scoreflex.RequestParams params,
 			Scoreflex.ResponseHandler responseHandler) {
 		ScoreflexRestClient.post(resource, params, responseHandler);
 	}
 
 	/**
 	 * A POST request that is guaranteed to be executed when a network connection
 	 * is present, surviving application reboot. The responseHandler will be
 	 * called only if the network is present when the request is first run.
 	 *
 	 * @param resource
 	 * @param params
 	 *          The request parameters. Only serializable parameters are
 	 *          guaranteed to survive a network error or device reboot.
 	 * @param responseHandler An AsyncHttpClient response handler.
 	 */
 	public static void postEventually(String resource,
 			Scoreflex.RequestParams params, Scoreflex.ResponseHandler responseHandler) {
 		ScoreflexRestClient.postEventually(resource, params, responseHandler);
 	}
 
 	/**
 	 * A PUT request.
 	 *
 	 * @param resource
 	 *          The resource path, starting with /.
 	 * @param params
 	 *          AsyncHttpClient request parameters.
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler.
 	 */
 	public static void put(String resource, Scoreflex.RequestParams params,
 			Scoreflex.ResponseHandler responseHandler) {
 		ScoreflexRestClient.put(resource, params, responseHandler);
 	}
 
 	/**
 	 * A DELETE request.
 	 *
 	 * @param resource
 	 *          The resource path, starting with /.
 	 * @param params
 	 *          AsyncHttpClient request parameters.
 	 * @param responseHandler
 	 *          An AsyncHttpClient response handler.
 	 */
 	public static void delete(String resource,
 			Scoreflex.ResponseHandler responseHandler) {
 		ScoreflexRestClient.delete(resource, responseHandler);
 	}
 
 	/**
 	 * Changes the default gravity.
 	 *
 	 * @param defaultGravity
 	 *          The new default gravity.
 	 */
 	public static void setDefaultGravity(int defaultGravity) {
 		if (Gravity.TOP == (defaultGravity & Gravity.VERTICAL_GRAVITY_MASK))
 			sDefaultGravity = Gravity.TOP;
 		else
 			sDefaultGravity = Gravity.BOTTOM;
 	}
 
 	/**
 	 * Returns the default gravity.
 	 *
 	 * @return The default gravity.
 	 */
 	public static int getDefaultGravity() {
 		return sDefaultGravity;
 	}
 
 	/**
 	 * Displays a Scoreflex panel on the provided activity using the default
 	 * gravity.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param leaderboardId
 	 *          The leaderboard id of the rankbox that you want to show.
 	 * @param score
 	 *          The last score the user did.
 	 */
 	public static ScoreflexView showRanksPanel(Activity activity,
 			String leaderboardId, long score) {
 		return showRanksPanel(activity, leaderboardId, score, sDefaultGravity);
 	}
 
 	/**
 	 * Displays a Scoreflex panel on the provided activity.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param leaderboardId
 	 *          The leaderboard id of the rankbox that you want to show.
 	 * @param score
 	 *          the last score the user did.
 	 * @param gravity
 	 *          Choose if the view should be up are down of the screen.
 	 */
 	public static ScoreflexView showRanksPanel(Activity activity,
 			String leaderboardId, long score, int gravity) {
 		// Params
 		Scoreflex.RequestParams params = new Scoreflex.RequestParams();
 		params.put("score", "" + score);
 
 		return showRanksPanel(activity, leaderboardId, gravity, params);
 	}
 
 	/**
 	 * Shows a view of the specified resource to the user.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The string of the Scoreflex resource you want to display.
 	 * @param params
 	 *          The parameter to be given to the resource (query string).
 	 * @return The displayed view call close() to hide it.
 	 */
 
 	public static ScoreflexView showFullScreenView(Activity activity,
 			String resource, Scoreflex.RequestParams params) {
 		ScoreflexView view = Scoreflex.view(activity, resource, params, true);
 
 		activity.addContentView(view, view.getLayoutParams());
 		view.requestFocus();
 		view.requestFocusFromTouch();
 		return view;
 	}
 
 	/**
 	 * Shows a panel view (small view) of the specified resource to the user.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The string of the Scoreflex resource you want to display.
 	 * @param params
 	 *          The parameter to be given to the resource (query string).
 	 * @param gravity
 	 *          Whether the panel should be shown on top or bottom of the screen.
 	 * @return The displayed view call close() to hide it.
 	 */
 	public static ScoreflexView showPanelView(Activity activity, String resource,
 			Scoreflex.RequestParams params, int gravity) {
 		ScoreflexView view = Scoreflex.view(activity, resource, params, false);
 		attachView(activity, view, gravity);
 		return view;
 	}
 
 
 	/**
 	 * Shows the player profile of the player (playerId) or the logged player if playerId is null. Endpoint: <code>/web/players/:id</code>.
 	 * @param activity The activity that will host the view.
 	 * @param playerId The identifier of the player or null for the current logged player.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showPlayerProfile(Activity activity, String playerId, Scoreflex.RequestParams params) {
 		if (null == playerId) {
 			playerId = "me";
 		}
 
 		return showFullScreenView(activity, "/web/players/"+playerId, params);
 	}
 
 	/**
 	 * Shows the friends of the player (playerId) or the logged player if playerId is null. Endpoint: <code>/web/players/:id/friends</code>.
 	 * @param activity The activity that will host the view.
 	 * @param playerId The identifier of the player or null for the current logged player.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showPlayerFriends(Activity activity, String playerId, Scoreflex.RequestParams params) {
 		if (null == playerId) {
 			playerId = "me";
 		}
 
 		return showFullScreenView(activity, "/web/players/"+playerId+"/friends", params);
 	}
 
 	/**
 	 * Shows the news feed of the logged player. Endpoint: <code>/web/players/me/newsfeed</code>.
 	 * @param activity The activity that will host the view.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showPlayerNewsFeed(Activity activity, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/players/me/newsfeed", params);
 	}
 
 	/**
 	 * Shows the edit profile form of the logged player. Endpoint: <code>/web/players/me/edit</code>.
 	 * @param activity The activity that will host the view.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showPlayerProfileEdit(Activity activity, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/players/me/edit", params);
 	}
 
 	/**
 	 * Shows the settings form of the logged player. Endpoint: <code>/web/players/me/settings</code>.
 	 * @param activity The activity that will host the view.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showPlayerSettings(Activity activity, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/players/me/settings", params);
 	}
 
 	/**
 	 * Shows the rating of the logged player. Endpoint: <code>/web/players/me/rating</code>
 	 * @param activity The activity that will host the view.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showPlayerRating(Activity activity, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/players/me/rating", params);
 	}
 
 	/**
 	 * Shows the profile of the developer (developerId). Endpoint: <code>/web/developers/:id</code>.
 	 * @param activity The activity that will host the view.
 	 * @param developerId The identifier of the developer.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showDeveloperProfile(Activity activity, String developerId, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/developers/"+developerId, params);
 	}
 
 	/**
 	 * Shows the games of the developer (developerId). Endpoint: <code>/web/developers/:id/games</code>.
 	 * @param activity The activity that will host the view.
 	 * @param developerId The identifier of the developer.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showDeveloperGames(Activity activity, String developerId, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/developers/"+developerId+"/games", params);
 	}
 
 	/**
 	 * Shows the details of the game (gameId). Endpoint: <code>/web/games/:id</code>
 	 * @param activity The activity that will host the view.
 	 * @param gameId The identifier the game.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showGameDetails(Activity activity, String gameId, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/games/"+gameId, params);
 	}
 
 	/**
 	 * Shows the players of the game (gameId). Endpoint: <code>/web/games/:id/players</code>.
 	 * @param activity The activity that will host the view.
 	 * @param gameId The identifier the game.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showGamePlayers(Activity activity, String gameId, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/games/"+gameId+"/players", params);
 	}
 
 	/**
 	 * Shows a leaderboard (leaderboardId). Endpoint: <code>/web/leaderboards/:leaderboardId</code>.
 	 * @param activity The activity that will host the view.
 	 * @param leaderboardId The identifier of the leaderboard.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showLeaderboard(Activity activity, String leaderboardId, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/leaderboards/"+leaderboardId, params);
 	}
 
 	/**
 	 * Shows the overview of the leaderboard (leaderboardId). Endpoint: <code>/web/leaderboards/:leaderboardId/overview</code>
 	 * @param activity The activity that will host the view.
 	 * @param leaderboardId The identifier of the leaderboard.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showLeaderboardOverview(Activity activity, String leaderboardId, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/leaderboards/"+leaderboardId+"/overview", params);
 	}
 
 
 	/**
 	 * Shows the challenges list of the current player. Endpoint: <code>/web/challenges</code>.
 	 * @param activity The activity that will host the view.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showChallenges(Activity activity, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/challenges", params);
 	}
 
 
 	/**
 	 * Shows the search form. Endpoint: <code>/web/search</code>
 	 * @param activity The activity that will host the view.
 	 * @param params The parameter to be given to the resource (query string).
 	 * @return The Scoreflex view on screen.
 	 */
 	public static ScoreflexView showSearch(Activity activity, Scoreflex.RequestParams params) {
 		return showFullScreenView(activity, "/web/search", params);
 	}
 
 	/**
 	 * Shows a Scoreflex panel on the provided activity.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param leaderboardId
 	 *          The leaderboard id of the rankbox that you want to show.
 	 * @param gravity
 	 *          Chooses if the view should be up are down of the screen.
 	 * @param params
 	 *          The parameter to be given to the resource (query string).
 	 */
 	public static ScoreflexView showRanksPanel(Activity activity,
 			String leaderboardId, int gravity, Scoreflex.RequestParams params) {
 		// Resource
 		String resource = String.format(Locale.getDefault(),
 				"/web/scores/%s/ranks", leaderboardId);
 
 		// Get the leaderboard & display
 		ScoreflexView leaderboardView = Scoreflex.view(activity, resource, params,
 				false);
 		attachView(activity, leaderboardView, gravity);
 		return leaderboardView;
 	}
 
 	/**
 	 * Attach the given view above the activity's view hierarchy.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param view
 	 *          The view to attach.
 	 * @param gravity
 	 *          Chooses if the view should be up are down of the screen.
 	 */
 
 	private static void attachView(Activity activity, View view, int gravity) {
 		ViewGroup contentView = (ViewGroup) activity.getWindow().getDecorView()
 				.findViewById(android.R.id.content);
 		// final float scale = activity.getResources().getDisplayMetrics().density;
 
 		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				getDensityIndependantPixel(activity.getResources()
 						.getDimensionPixelSize(R.dimen.scoreflex_panel_height)), gravity);
 		view.setLayoutParams(layoutParams);
 		view.setVisibility(View.GONE);
 		contentView.addView(view);
 		// activity.addContentView(view, layoutParams);
 		if (view instanceof ScoreflexView)
 			((ScoreflexView) view).addDropShadow(gravity);
 	}
 
 	/**
 	 * Build a view that displays Scoreflex content.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The REST resource corresponding to that view.
 	 * @return A view that you can attach to your view hierarchy.
 	 */
 	protected static ScoreflexView view(Activity activity, String resource) {
 		return view(activity, resource, null);
 	}
 
 	/**
 	 * Builds a view that displays Scoreflex content.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The REST resource corresponding to that view.
 	 * @param params
 	 *          The request parameters.
 	 * @return A view that you can attach to your view hierarchy.
 	 */
 	protected static ScoreflexView view(Activity activity, String resource,
 			Scoreflex.RequestParams params) {
 		ScoreflexView result = null;
 
 		if (mPreloadedViews != null) {
 			result = getPreloadedView(resource);
 		}
 
 		if (result == null) {
 			result = new ScoreflexView(activity);
 		} else {
 			removePreloadedView(resource, false);
 			setCurrentScoreflexView(result);
 			return result;
 		}
 		result.setResource(resource, params);
 		return result;
 	}
 
 	/**
 	 * Builds a fullscreen view to Scoreflex content and returns it.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The REST resource corresponding to that view.
 	 * @param params
 	 *          The request parameters.
 	 * @return A view that you can attach to your view hierarchy.
 	 */
 	public static ScoreflexView getFullscreenView(Activity activity,
 			String resource, Scoreflex.RequestParams params) {
 		return view(activity, resource, params, true);
 	}
 
 	/**
 	 * Builds a panel view to Scoreflex content and returns it.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The REST resource corresponding to that view.
 	 * @param params
 	 *          The request parameters.
 	 * @return A view that you can attach to your view hierarchy.
 	 */
 	public static ScoreflexView getPanelView(Activity activity, String resource,
 			Scoreflex.RequestParams params) {
 		return view(activity, resource, params, false);
 	}
 
 	/**
 	 * Builds a view that displays Scoreflex content.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The REST resource corresponding to that view.
 	 * @param params
 	 *          The request parameters.
 	 * @param forceFullScreen
 	 *          Set wether the view should be full screen.
 	 * @return A view that you can attach to your view hierarchy.
 	 */
 	protected static ScoreflexView view(Activity activity, String resource,
 			Scoreflex.RequestParams params, boolean forceFullScreen) {
 		ScoreflexView result = null;
 		if (mPreloadedViews != null) {
 			result = getPreloadedView(resource);
 		}
 
 		if (result == null) {
 			result = new ScoreflexView(activity);
 		} else {
 			removePreloadedView(resource, false);
 			setCurrentScoreflexView(result);
 			result.startOpeningAnimation();
 			return result;
 		}
 		result = new ScoreflexView(activity);
 		result.setResource(resource, params, forceFullScreen);
 		return result;
 	}
 
 	/**
 	 * Handles Scoreflex activity results. This method MUST be called from your Activity's
 	 * onActivityResult method in order to handle facebook / google login.
 	 * Your onActivityResult should look like this :
 	 * <pre>
 	 * <code>
 	 * 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	 *		super.onActivityResult(requestCode, resultCode, data);
 	 *		Scoreflex.onActivityResult(this, requestCode, resultCode, data);
 	 *	}
 	 *
 	 * </code>
 	 * </pre>
 	 *
 	 * @param activity
 	 *          The current activity that received the result.
 	 * @param requestCode
 	 *          The requestCode the activity received.
 	 * @param responseCode
 	 *          The responseCode the activity received.
 	 * @param intent
 	 *          The intent the activity received.
 	 */
 	public static void onActivityResult(Activity activity, int requestCode,
 			int responseCode, Intent intent) {
 
 		if (requestCode == FILECHOOSER_RESULTCODE && mScoreflexView != null) {
 			ScoreflexView view = mScoreflexView.get();
 			if (view != null) {
 				view.onActivityResult(activity, requestCode, responseCode, intent);
 				return;
 			}
 		}
 		ScoreflexGoogleWrapper.onActivityResult(activity, requestCode,
 				responseCode, intent);
 		ScoreflexFacebookWrapper.onActivityResult(activity, requestCode,
 				responseCode, intent);
 	}
 
 	/**
 	 * A helper method that submits a score.
 	 *
 	 * @param leaderboardId
 	 *          The leaderboad id to submit the score to.
 	 * @param score
 	 *          The score of the player.
 	 * @param params
 	 *          Other parameters that will be used for the api call.
 	 * @param responseHandler
 	 *          A response handler that will be called if the request is sent
 	 *          immediatly otherwise, will never get called (@see
 	 *          {@link #postEventually(String, RequestParams, ResponseHandler)}).
 	 */
 	public static void submitScore(String leaderboardId, long score,
 			Scoreflex.RequestParams params, Scoreflex.ResponseHandler responseHandler) {
 		if (params == null) {
 			params = new Scoreflex.RequestParams();
 		}
 
		params.put("score", Long.toString(score));
 		submitScore(leaderboardId, params, responseHandler);
 	}
 
 	protected static void submitScore(String leaderboardId,
 			Scoreflex.RequestParams params, Scoreflex.ResponseHandler responseHandler) {
 		final String scoreResource = "/scores/" + leaderboardId;
 		// RequestParams params = new RequestParams();
 		// params.put("score", Long.toString(score));
 
 		Scoreflex.postEventually(scoreResource, params, responseHandler);
 	}
 
 	/**
 	 * A helper method that submits a score to a leaderboard ID and show the
 	 * rank panel for the current player.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param leaderboardId
 	 *          The leaderboad id to submit the score to.
 	 * @param score
 	 *          The score of the player.
 	 * @param params
 	 *          Other parameters that will be used for the api call and the
 	 *          rankbox display.
 	 * @param gravity
 	 *          Chooses if the view should be up are down of the screen.
 	 * @return the Scoreflex view call close() to hide it.
 	 */
 	public static ScoreflexView submitScoreAndShowRanksPanel(
 			final Activity activity, final String leaderboardId, long score,
 			RequestParams params, final int gravity) {
 
 		if (params == null) {
 			params = new RequestParams();
 		}
 		params.put("score", Long.toString(score));
 
 		final RequestParams finalParams = params;
 
 		ScoreflexView rankbox = Scoreflex.showRanksPanel(activity, leaderboardId,
 				gravity, finalParams);
 
 		submitScore(leaderboardId, params, new Scoreflex.ResponseHandler() {
 
 			@Override
 			public void onFailure(Throwable e, Response errorResponse) {
 				Log.d("Scoreflex", "Could not submit score, Rankbox wont be shown");
 			}
 
 			@Override
 			public void onSuccess(Response response) {
 
 			}
 
 		});
 		return rankbox;
 	}
 
 	/**
 	 * Gets the current player id from the local cache.
 	 *
 	 * @return The current player id from the local cache.
 	 */
 	public static String getPlayerId() {
 		return ScoreflexRestClient.getPlayerId();
 	}
 
 	/**
 	 * Gets the access token from the local cache.
 	 *
 	 * @return The access token from the local cache.
 	 */
 	public static String getAccessToken() {
 		return ScoreflexRestClient.getAccessToken();
 	}
 
 
 	/**
 	 * A helper method that submits a turn to a challenge instance.
 	 *
 	 * @param challengeInstanceId
 	 *          The challenge instance id.
 	 * @param turn
 	 *          The turn data.
 	 * @param responseHandler
 	 *          A response handler if the request is sent immediatly otherwise,
 	 *          will never get called (@see
 	 *          {@link #postEventually(String, RequestParams, ResponseHandler)}).
 	 */
 	public static void submitTurn(String challengeInstanceId, RequestParams turn,
 			Scoreflex.ResponseHandler responseHandler) {
 		final String turnResource = "/challenges/instances/" + challengeInstanceId
 				+ "/turns";
 		JSONObject body = new JSONObject();
 		try {
 			Set<String> parameters = turn.getParamNames();
 			for (String parameterName : parameters) {
 				body.put(parameterName, turn.getParamValue(parameterName));
 			}
 			long playingTime = Scoreflex.getPlayingSessionTime();
 			if (playingTime > 0) {
 				body.put("playingTime", playingTime);
 			}
 			RequestParams params = new RequestParams();
 			params.put("body", body.toString());
 			Scoreflex.postEventually(turnResource, params, responseHandler);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * A helper method that submits turn data and directly show challenge detail.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param challengeInstanceId
 	 *          The challenge instance id.
 	 * @param turn
 	 *          The turn data.
 	 */
 	public static void submitTurnAndShowChallengeDetail(final Activity activity,
 			final String challengeInstanceId, RequestParams turn) {
 
 		final String resource = "/web/challenges/instances/" + challengeInstanceId;
 
 		submitTurn(challengeInstanceId, turn, new Scoreflex.ResponseHandler() {
 
 			@Override
 			public void onFailure(Throwable e, Response errorResponse) {
 				Log.d("Scoreflex",
 						"Could not submit score, Chalenge detail wont be shown");
 			}
 
 			@Override
 			public void onSuccess(Response response) {
 				View view = Scoreflex.view(activity, resource, null, true);
 				activity.addContentView(view, view.getLayoutParams());
 				view.requestFocus();
 				view.requestFocusFromTouch();
 			}
 
 		});
 	}
 
 	/**
 	 * A class that handles the parameter to provide to either an api call or a
 	 * view.
 	 *
 	 */
 	public static class RequestParams extends
 			com.loopj.android.http.RequestParams implements Parcelable {
 
 		public static final String TAG = "RequestParams";
 
 		public RequestParams(Parcel in) throws JSONException  {
 			JSONObject json = new JSONObject(in.readString());
 			Iterator<?> it = json.keys();
 			String key;
 			while (it.hasNext()) {
 				key = (String)it.next();
 				this.put(key, json.optString(key));
 			}
 		}
 
 		/**
 		 * Constructs a new empty <code>RequestParams</code> instance.
 		 */
 		public RequestParams() {
 			super();
 		}
 
 		/**
 		 * Constructs a new RequestParams instance containing the key/value string
 		 * params from the specified map.
 		 *
 		 * @param source
 		 *          The source key/value string map to add.
 		 */
 		public RequestParams(Map<String, String> source) {
 			super(source);
 		}
 
 		/**
 		 * Constructs a new RequestParams instance and populate it with multiple
 		 * initial key/value string param.
 		 *
 		 * @param keysAndValues
 		 *          A sequence of keys and values. Objects are automatically
 		 *          converted to Strings (including the value {@code null}).
 		 * @throws IllegalArgumentException
 		 *           If the number of arguments isn't even.
 		 */
 		public RequestParams(Object... keysAndValues) {
 			super(keysAndValues);
 		}
 
 		/**
 		 * Constructs a new RequestParams instance and populate it with a single
 		 * initial key/value string param.
 		 *
 		 * @param key
 		 *          The key name for the intial param.
 		 * @param value
 		 *          The value string for the initial param.
 		 */
 		public RequestParams(String key, String value) {
 			super(key, value);
 		}
 
 		/**
 		 * Return the names of all parameters.
 		 *
 		 * @return
 		 */
 		public Set<String> getParamNames() {
 			HashSet<String> result = new HashSet<String>();
 			result.addAll(this.fileParams.keySet());
 			result.addAll(this.urlParams.keySet());
 			result.addAll(this.urlParamsWithArray.keySet());
 			return result;
 		}
 
 		/**
 		 * Returns the value for the given param. If the given param is encountered
 		 * multiple times, the first occurrence is returned.
 		 *
 		 * @param paramName
 		 * @return
 		 */
 		public String getParamValue(String paramName) {
 
 			if (this.urlParams.containsKey(paramName))
 				return this.urlParams.get(paramName);
 
 			if (this.urlParamsWithArray.containsKey(paramName)) {
 				List<String> values = this.urlParamsWithArray.get(paramName);
 				if (0 < values.size())
 					return values.get(0);
 			}
 
 			return null;
 		}
 
 		/**
 		 * Checks whether the provided key has been specified as parameter.
 		 *
 		 * @param key
 		 * @return
 		 */
 		public boolean has(String key) {
 			return urlParams.containsKey(key) || fileParams.containsKey(key)
 					|| urlParamsWithArray.containsKey(key);
 		}
 
 		public String getURLEncodedString() {
 			return getParamString();
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		public JSONObject toJSONObject() {
 			JSONObject result = new JSONObject();
 			java.util.List<org.apache.http.message.BasicNameValuePair> params = getParamsList();
 			for (org.apache.http.message.BasicNameValuePair parameter : params ) { 
 				try {
 					result.put(parameter.getName(), parameter.getValue());
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			return result;
 		}
 
 		@Override
 		public void writeToParcel(Parcel destination, int flags) {
 			destination.writeString(toJSONObject().toString());
 		}
 
 		public static final Parcelable.Creator<Scoreflex.RequestParams> CREATOR =
 				new Parcelable.Creator<Scoreflex.RequestParams>() {
 
 				public RequestParams createFromParcel(Parcel in) {
 					try {
 						return new RequestParams(in);
 					} catch (JSONException e) {
 						Log.e(TAG, "Error while unserializing JSON from a Scoreflex.RequestParams", e);
 						return null;
 					}
 				}
 
 				public RequestParams[] newArray(int size) {
 					return new RequestParams[size];
 				}
 			};
 	}
 
 	/**
 	 * Http response handler.
 	 *
 	 *
 	 *
 	 */
 	public static abstract class ResponseHandler {
 		/**
 		 * Called when the request failed. Default implementation is empty.
 		 *
 		 * @param e
 		 * @param errorResponse
 		 */
 		public abstract void onFailure(Throwable e, Response errorResponse);
 
 		/**
 		 * Called on request success. Default implementation is empty.
 		 *
 		 * @param response
 		 */
 		public abstract void onSuccess(Response response);
 
 		/**
 		 * Called on request success. Default implementation calls
 		 * onSuccess(response).
 		 *
 		 * @param response
 		 */
 		public void onSuccess(int statusCode, Response response) {
 			onSuccess(response);
 		}
 
 	}
 
 	/**
 	 * An HTTP response object
 	 *
 	 */
 	public static class Response {
 		JSONObject mJson;
 
 		public Response(String responseContent) {
 			try {
 				mJson = new JSONObject(responseContent);
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid JSON in response content: "
 						+ responseContent, e);
 			}
 		}
 
 		public Response(JSONObject responseJson) {
 			mJson = responseJson;
 		}
 
 		public boolean isError() {
 			return mJson.has("error");
 		}
 
 		public String getErrorMessage() {
 			if (!isError())
 				return null;
 
 			return mJson.optJSONObject("error").optString("message");
 		}
 
 		public int getErrorStatus() {
 			if (!isError())
 				return 0;
 
 			return mJson.optJSONObject("error").optInt("status");
 		}
 
 		public int getErrorCode() {
 			if (!isError())
 				return 0;
 
 			return mJson.optJSONObject("error").optInt("code");
 		}
 
 		public JSONObject getJSONObject() {
 			return mJson;
 		}
 	}
 
 	/**
 	 * Gets the application context that was captured during the {@link
 	 * Scoreflex.initialize(Context, String, String} call.
 	 *
 	 * @return
 	 */
 	protected static Context getApplicationContext() {
 		if (null == sApplicationContext)
 			Log.e("Scoreflex",
 					"Application context is null, did you call Scoreflex.initialize ?");
 		return sApplicationContext;
 	}
 
 	protected static SharedPreferences getSharedPreferences(Context c) {
 		if (null == c)
 			throw new IllegalArgumentException("Context must be set");
 
 		return c.getSharedPreferences(PREF_FILE, 0);
 	}
 
 	/**
 	 * Gets the Scoreflex shared preferences for that application.
 	 *
 	 * @return
 	 */
 	protected static SharedPreferences getSharedPreferences() {
 		if (null == getApplicationContext())
 			return null;
 		return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
 	}
 
 	/**
 	 * Gets the clientId that was specified during the {@link
 	 * Scoreflex.initialize(Context, String, String} call.
 	 *
 	 * @return
 	 */
 	public static String getClientId() {
 		return sClientId;
 	}
 
 	/**
 	 * Gets the clientSecret that was specified during the {@link
 	 * Scoreflex.initialize(Context, String, String} call.
 	 *
 	 * @return
 	 */
 	protected static String getClientSecret() {
 		return sClientSecret;
 	}
 
 	/**
 	 * Gets the model of this android device.
 	 *
 	 * @return
 	 */
 	protected static String getDeviceModel() {
 		return String.format("%s - %s", Build.MANUFACTURER, Build.MODEL);
 	}
 
 	/**
 	 * Returns the UDID determined by OpenUDID.
 	 *
 	 * @return The UDID determined by OpenUDID or null if OpenUDID is not
 	 *         initialized.
 	 */
 	protected static String getUDID() {
 		if (OpenUDID_manager.isInitialized())
 			return OpenUDID_manager.getOpenUDID();
 		return null;
 	}
 
 	/**
 	 * Gets the current language. If language was specified using {@link
 	 * Scoreflex.setLang(String)}, this value is returned. Otherwise it is guessed
 	 * from the system.
 	 *
 	 * @return The locale in use.
 	 */
 	public static String getLang() {
 		if (null != sLang)
 			return sLang;
 
 		Locale locale = Locale.getDefault();
 
 		if (null == locale)
 			return DEFAULT_LANGUAGE_CODE;
 
 		String language = locale.getLanguage();
 		String country = locale.getCountry();
 		String localeString = String.format("%s_%s",
 				language != null ? language.toLowerCase(Locale.ENGLISH) : "",
 				country != null ? country.toUpperCase(Locale.ENGLISH) : "");
 
 		// 1. if no language is specified, return the default language
 		if (null == language)
 			return DEFAULT_LANGUAGE_CODE;
 
 		// 2. try to match the language or the entire locale string among the
 		// list of available language codes
 		String matchedLanguageCode = null;
 		for (int i = 0; i < VALID_LANGUAGE_CODES.length; i++) {
 
 			if (VALID_LANGUAGE_CODES[i].equals(localeString)) {
 				// return here as this is the most precise match we can get
 				return localeString;
 			}
 
 			if (VALID_LANGUAGE_CODES[i].equals(language)) {
 				// set the matched language code, and continue iterating as we
 				// may match the localeString in a later iteration.
 				matchedLanguageCode = language;
 			}
 		}
 
 		if (null != matchedLanguageCode)
 			return matchedLanguageCode;
 
 		return DEFAULT_LANGUAGE_CODE;
 	}
 
 	/**
 	 * Sets the language code used by the Scoreflex SDK. This language code will
 	 * affect the responses of the REST server as well as Scoreflex web content.
 	 *
 	 * @param lang
 	 *          Valid values are available in
 	 *          {@link Scoreflex.VALID_LANGUAGE_CODES}.
 	 * @throws IllegalArgumentException
 	 *           If the lang is not a valid language.
 	 */
 	public static void setLang(String lang) throws IllegalArgumentException {
 		for (int i = 0; i < VALID_LANGUAGE_CODES.length; i++) {
 			if (VALID_LANGUAGE_CODES[i].equals(lang)) {
 				sLang = lang;
 				return;
 			}
 		}
 		throw new IllegalArgumentException(String.format(
 				"%s is not a valid language code", lang));
 	}
 
 	/**
 	 * Sets the location of the user. If you are collecting user location, this
 	 * setting will allow the SDK to forward user location to the Scoreflex REST
 	 * server when appropriate and present the user location specific information.
 	 *
 	 * @param location
 	 */
 	public static void setLocation(Location location) {
 		sLocation = location;
 	}
 
 	/**
 	 * Returns the Location as set in {@link setLocation(Location)} or the best
 	 * last known location of the {@link LocationManager} or null if permission
 	 * was not given.
 	 */
 
 	public static Location getLocation() {
 		if (null != sLocation)
 			return sLocation;
 
 		Context applicationContext = getApplicationContext();
 
 		if (applicationContext == null)
 			return null;
 
 		LocationManager locationManager = (LocationManager) applicationContext
 				.getSystemService(Context.LOCATION_SERVICE);
 		try {
 			Location locations[] = {
 					locationManager
 							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
 					locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER),
 					locationManager
 							.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER), };
 
 			Location best = null;
 			for (int i = 0; i < locations.length; i++) {
 
 				// If this location is null, discard
 				if (null == locations[i])
 					continue;
 
 				// If we have no best yet, use this first location
 				if (null == best) {
 					best = locations[i];
 					continue;
 				}
 
 				// If this location is significantly older, discard
 				long timeDelta = locations[i].getTime() - best.getTime();
 				if (timeDelta < -1000 * 60 * 2)
 					continue;
 
 				// If we have no accuracy, discard
 				if (0 == locations[i].getAccuracy())
 					continue;
 
 				// If this location is less accurate, discard
 				if (best.getAccuracy() < locations[i].getAccuracy())
 					continue;
 
 				best = locations[i];
 			}
 
 			return best;
 		} catch (java.lang.SecurityException e) {
 			// Missing permission;
 			return null;
 		}
 	}
 
 	protected static void setCurrentScoreflexView(ScoreflexView view) {
 		mScoreflexView = new WeakReference<ScoreflexView>(view);
 	}
 
 	/**
 	 * Method to be called when the back button is pressed (hardware) to handle
 	 * history in a scoreflexView.
 	 * Call it in the activity's back button pressed method as follow:
 	 * <pre>
 	 * <code>
 	 * public void onBackPressed() {
 	 * 	if (Scoreflex.backButtonPressed() == false) {
 	 *		super.onBackPressed();
 	 *	}
 	 * }
 	 * </code>
 	 * </pre>
 	 *
 	 * @return <code>true</code> If handled <code>false</code> otherwise.
 	 */
 	public static boolean backButtonPressed() {
 		if (mScoreflexView == null) {
 			return false;
 		}
 
 		ScoreflexView view = mScoreflexView.get();
 		if (view != null) {
 			if (view.canGoBack()) {
 				view.goBack();
 				return true;
 			} else {
 				view.close();
 				mScoreflexView = null;
 				return true;
 			}
 
 		}
 		return false;
 	}
 
 	private static boolean checkPlayService(Activity activity) {
 		return  ScoreflexGcmWrapper.isGooglePlayServiceAvailable(activity);
 	}
 
 	/**
 	 * If network is available, preload a view with the specified ressource and hold a reference on it until the view is shown or freed.
 	 *
 	 * @param activity
 	 *          The activity that will host the view.
 	 * @param resource
 	 *          The ressource to preload.
 	 */
 	public static void preloadResource(Activity activity, String resource) {
 		if (activity == null) {
 			throw new IllegalArgumentException("activity can not be null");
 		}
 		if (resource == null) {
 			throw new IllegalArgumentException("resource can not be null");
 		}
 
 		if (mPreloadedViews == null) {
 			mPreloadedViews = new HashMap<String, ScoreflexView>();
 		}
 
 		if (mPreloadedViews.containsKey(resource)) {
 			return;
 		}
 
 		if (null == getPlayerId() || !isReachable()) {
 			return;
 		}
 
 		if (sApplicationContext == null) {
 			sApplicationContext = activity.getApplicationContext();
 		}
 		ScoreflexView preloadView = new ScoreflexView(activity);
 		preloadView.preloadResource(resource);
 		addPreloadedView(resource, preloadView);
 		return;
 	}
 
 	private static void addPreloadedView(String resource, ScoreflexView view) {
 		synchronized (mPreloadedViews) {
 			mPreloadedViews.put(resource, view);
 		}
 	}
 
 	private static void removePreloadedView(String resource) {
 		removePreloadedView(resource, true);
 	}
 
 	private static void removePreloadedView(String resource, boolean closeView) {
 		synchronized (mPreloadedViews) {
 			ScoreflexView view = mPreloadedViews.get(resource);
 			if (view != null) {
 				if (closeView) {
 					view.close();
 				}
 				mPreloadedViews.remove(resource);
 			}
 		}
 	}
 
 	private static void clearPreloadedView() {
 		synchronized (mPreloadedViews) {
 			for (Entry<String, ScoreflexView> entry : mPreloadedViews.entrySet()) {
 				entry.getValue().close();
 			}
 			mPreloadedViews.clear();
 		}
 	}
 
 	private static ScoreflexView getPreloadedView(String resource) {
 		ScoreflexView view = null;
 		synchronized (mPreloadedViews) {
 			view = mPreloadedViews.get(resource);
 		}
 		return view;
 	}
 
 	/**
 	 * Free the specified preloaded ressource from memory.
 	 *
 	 * @param resource
 	 *          The ressource to free (all preloaded resource if null).
 	 */
 	public static void freePreloadedResources(String resource) {
 		if (null == mPreloadedViews) {
 			return;
 		}
 
 		if (resource == null) {
 			clearPreloadedView();
 			return;
 		}
 		removePreloadedView(resource);
 	}
 
 	/**
 	 * Helper method that will register a device for google cloud messages
 	 * notification and register the device token to Scoreflex. This method must be
 	 * called after the initialize.
 	 *
 	 * @param senderId
 	 *          Google Cloud Message sender id to register to.
 	 * @param activity
 	 *          The current activity.
 	 */
 	public static void registerForPushNotification(Activity activity) {
 		if (checkPlayService(activity)) {
 			ScoreflexGcmClient.registerForPushNotification(activity);
 		}
 	}
 
 	/**
 	 * Method to call on your onCreate method to handle the Scoreflex notification, it must be added to the Activity implementation of the class you gave to
 	 * {@link #onBroadcastReceived(Context, Intent, int, Class)} (must be called after Scoreflex.initialize().
 	 * As follow:
 	 * <pre>
 	 * <code>
 	 * protected void onCreate(Bundle savedInstance) {
 	 * 	Scoreflex.onCreateMainActivity(this, getIntent());
 	 * }
 	 * </code>
 	 * </pre>
 	 * @param activity
 	 *          The current activity.
 	 * @param intent
 	 *          The intent the activity received.
 	 * @return <code>true</code> if handled, <code>false</code> otherwise.
 	 */
 	public static boolean onCreateMainActivity(Activity activity, Intent intent) {
 		if (intent.hasExtra(Scoreflex.NOTIFICATION_EXTRA_KEY)) {
 			String notificationString = intent
 					.getStringExtra(Scoreflex.NOTIFICATION_EXTRA_KEY);
 
 			try {
 				JSONObject notification = new JSONObject(notificationString);
 				JSONObject data = notification.getJSONObject("data");
 				int code = notification.getInt("code");
 				if (NOTIFICATION_TYPE_CHALLENGE_INVITATION == code
 						|| NOTIFICATION_TYPE_YOUR_TURN_IN_CHALLENGE == code
 						|| NOTIFICATION_TYPE_CHALLENGE_ENDED == code) {
 					showFullScreenView(
 							activity,
 							"/web/challenges/instances/"
 									+ data.getString("challengeInstanceId"), null);
 				} else if (NOTIFICATION_TYPE_FRIEND_JOINED_GAME == code) {
 					showFullScreenView(activity,
 							"/web/players/" + data.getString("friendId"), null);
 				} else if (NOTIFICATION_TYPE_FRIEND_BEAT_YOUR_HIGHSCORE == code) {
 					Scoreflex.RequestParams params = new RequestParams();
 					params.put("friendsOnly", "true");
 					params.put("focus", data.getString("friendId"));
 					showFullScreenView(activity,
 							"/web/leaderboards/" + data.getString("leaderboardId"),
 							params);
 				} else if (NOTIFICATION_TYPE_PLAYER_LEVEL_CHANGED == code) {
 					showFullScreenView(activity, "/web/players/me", null);
 				}
 				return true;
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return false;
 	}
 
 	/**
 	 * Starts listening to network connectivity change.
 	 *
 	 * @param context
 	 */
 	public static void registerNetworkReceiver(Context context) {
 		// registering network listener
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 		try {
 			context.registerReceiver(sConectivityReceiver, filter);
 		} catch (Exception e) {
 
 		}
 	}
 
 	/**
 	 * Stops listening to network connectivity change
 	 *
 	 * @param context
 	 */
 	public static void unregisterNetworkReceiver(Context context) {
 		try {
 			context.unregisterReceiver(sConectivityReceiver);
 		} catch (Exception e) {
 
 		}
 	}
 
 	/**
 	 * Method to be called in your Google Cloud Message Broadcast receiver to handle scoreflex
 	 * cloud messages and show the appropiate notification. Implement your onBroadcastReceived as follow :
 	 *
 	 * <pre>
 	 * <code>
 	 * public void onReceive(Context context, Intent intent) {
 	 * 	if (Scoreflex.onBroadcastReceived(context, intent, R.drawable.icon, GoblinsAttackActivity.class)) {
 	 *		return;
 	 *	}
 	 *	// do your own handling here
 	 * }
 	 * </code>
 	 * </pre>
 	 * For more information about Google Cloud Message visit: {@linkplain <a href="http://developer.android.com/google/gcm/index.html">http://developer.android.com/google/gcm/index.html</a>}.
 	 *
 	 * @param context
 	 *          The current context.
 	 * @param intent
 	 *          The received intent.
 	 * @param iconResource
 	 *          The icon you want to show in the notification.
 	 * @param activityClass
 	 *          The activity class you want to start when the user touches the.
 	 *          notification
 	 * @return <code>true</code> if handled, <code>false</code> otherwise.
 	 */
 	public static boolean onBroadcastReceived(Context context, Intent intent,
 			int iconResource, Class<? extends Activity> activityClass) {
 		return ScoreflexGcmClient.onBroadcastReceived(context, intent,
 				iconResource, activityClass);
 	}
 
 	/**
 	 * Returns the time interval between {@link #startPlayingSession()} and {@link #stopPlayingSession()} have been called.
 	 *
 	 * @return The current playing session time of the player in milliseconds.
 	 */
 	public static long getPlayingSessionTime() {
 		if (playingSessionStart != 0) {
 			return System.currentTimeMillis() - playingSessionStart;
 		}
 		return 0;
 	}
 
 	@SuppressWarnings("deprecation")
 	@SuppressLint("NewApi")
 	private static Point getScreenSize() {
 		final Point size = new Point();
 		WindowManager w = (WindowManager) getApplicationContext().getSystemService(
 				Context.WINDOW_SERVICE);
 		Display d = w.getDefaultDisplay();
 
 		try {
 			Method getSizeMethod = d.getClass().getDeclaredMethod("getSize", Point.class);
 			getSizeMethod.invoke(d, size);
 		} catch (Exception e) {
 			size.x = d.getWidth();
 			size.y = d.getHeight();
 		}
 		return size;
 	}
 
 	/**
 	 * Retuns whether Scoreflex is reachable or not.
 	 *
 	 * @return <code>true</code> if Scoreflex is reachable <code>false</code> otherwise
 	 */
 	public static boolean isReachable() {
 		return sIsReachable;
 	}
 
 	protected static void setNetworkAvailable(boolean state) {
 		if (state != sIsReachable) {
 			Intent connectivityChangedIntent = new Intent(
 					Scoreflex.INTENT_CONNECTIVITY_CHANGED);
 			connectivityChangedIntent.putExtra(
 					Scoreflex.INTENT_CONNECTIVITY_EXTRA_CONNECTIVITY, state);
 			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
 					connectivityChangedIntent);
 		}
 		sIsReachable = state;
 	}
 
 	protected static int getDensityIndependantPixel(int size) {
 		Point screenSize = getScreenSize();
 		float width = 480.0f;
 		float height = 320.0f;
 		if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			width = 320.0f;
 			height = 480.0f;
 		}
 
 		float xRatio = (float) ((float) screenSize.x / width);
 		float yRatio = (float) ((float) screenSize.y / height);
 		float ratio = yRatio < xRatio ? yRatio : xRatio;
 		return (int) ratio * size;
 	}
 
 	/**
 	 * Sends a google interactive post inviting a user (or a list of users) to install the game
 	 *
 	 * @param activity the current activity
 	 * @param text the message that will be prefilled in the invitation
 	 * @param friendIds a list of friend you want to invite
 	 * @param url the url your want to share on the interactive post button
 	 * @param deeplinkPath the deeplink that your application will receive on launch
 	 */
 	public static void sendGoogleInvitation(Activity activity, String text,List<String> friendIds, String url, String deeplinkPath) {
 		ScoreflexGoogleWrapper.sendInvitation(activity, text, friendIds, url, deeplinkPath);
 	}
 
 	/**
 	 * Share a link on google plus
 	 * @param activity the current activity
 	 * @param text the message that will be prefilled in the invitation
 	 * @param url the url your want to share
 	 */
 	public static void shareOnGoogle(Activity activity, String text, String url) {
 		ScoreflexGoogleWrapper.shareUrl(activity, text, url);
 	}
 
 
 	/**
 	 * Post on the facebook feed of the current logged user
 	 * @param activity the current activity
 	 * @param title the title of the link
 	 * @param text the message that will be prefilled in the invitation
 	 * @param url the url your want to share
 	 */
 	public static void shareOnFacebook(Activity activity, String title, String text, String url) {
 		try {
 			ScoreflexFacebookWrapper.shareUrl(activity, title, text, url);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sends a facebook app request inviting a user (or a list of users) to install the game
 	 *
 	 * @param activity the current activity
 	 * @param text the message that will be prefilled in the invitation
 	 * @param friendIds a list of friend you want to invite
 	 * @param suggestedFriendIds the suggested friend (appears in the invitation dialog)
 	 * @param data any data you want to attach to the invitation (deeplink)
 	 */
 	public static void sendFacebookInvitation(Activity activity, String text, List<String> friendIds, List<String> suggestedFriendIds, String data)  {
 		try {
 			ScoreflexFacebookWrapper.sendInvitation(activity, text, friendIds, suggestedFriendIds, data, new SocialShareCallback() {
 
 				@Override
 				public void OnSuccessShare(List<String> invitedFriends) {
 					String concatenatedFriends = "Facebook%3A"+TextUtils.join(",Facebook%3A", invitedFriends);
 					Scoreflex.postEventually("/social/invitations/" + concatenatedFriends, null, null);
 				}
 			});
 		} catch (FacebookException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Stops the playing session of a player (user for tracking player session
 	 * time).
 	 */
 	public static void stopPlayingSession() {
 		playingSessionStart = 0;
 	}
 
 	/**
 	 * Starts the playing session of a player (user for tracking player session
 	 * time).
 	 */
 	public static void startPlayingSession() {
 		Scoreflex.playingSessionStart = System.currentTimeMillis();
 	}
 }
