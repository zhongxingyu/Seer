 package com.livestrong.myplate.back.api;
 
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.Proxy.Type;
 import java.security.KeyStore;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.springframework.http.HttpBasicAuthentication;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.client.SimpleClientHttpRequestFactory;
 import org.springframework.http.converter.FormHttpMessageConverter;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.json.GsonHttpMessageConverter;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.HttpServerErrorException;
 import org.springframework.web.client.HttpStatusCodeException;
 import org.springframework.web.client.RestTemplate;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.State;
 import android.os.Looper;
 import android.text.Html;
 import android.util.Log;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.livestrong.myplate.MyPlateApplication;
 import com.livestrong.myplate.R;
 import com.livestrong.myplate.back.AsyncDataHelper;
 import com.livestrong.myplate.back.DataHelper;
 import com.livestrong.myplate.back.DataHelperDelegate;
 import com.livestrong.myplate.back.api.models.ExerciseSearchResponse;
 import com.livestrong.myplate.back.api.models.FoodSearchResponse;
 import com.livestrong.myplate.back.api.models.NewCommentResponse;
 import com.livestrong.myplate.back.api.models.NewMessageResponse;
 import com.livestrong.myplate.back.api.models.OAuth2TokenResponse;
 import com.livestrong.myplate.back.api.models.RegisterUserResponse;
 import com.livestrong.myplate.back.api.models.SyncDiaryObject;
 import com.livestrong.myplate.back.models.ActivityLevels;
 import com.livestrong.myplate.back.models.CommunityMessage;
 import com.livestrong.myplate.back.models.CommunityMessageComment;
 import com.livestrong.myplate.back.models.CommunityUser;
 import com.livestrong.myplate.back.models.Exercise;
 import com.livestrong.myplate.back.models.ExerciseDiaryEntry;
 import com.livestrong.myplate.back.models.Food;
 import com.livestrong.myplate.back.models.FoodDiaryEntry;
 import com.livestrong.myplate.back.models.Meal;
 import com.livestrong.myplate.back.models.MealItem;
 import com.livestrong.myplate.back.models.MealNutritionInfo;
 import com.livestrong.myplate.back.models.UserProfile;
 import com.livestrong.myplate.back.models.UserProfile.Gender;
 import com.livestrong.myplate.back.models.WaterDiaryEntry;
 import com.livestrong.myplate.back.models.WeightDiaryEntry;
 import com.livestrong.myplate.constants.BuildValues;
 
 @SuppressWarnings("unchecked")
 public class ApiHelper {
 
 	private static final String api_secret = "1000148";
 	private static final String api_key = "b6eef67d9a99504b82be4d67e1a0fd34";
 	public enum AuthUsing {NONE, HTTP_BASIC, OAUTH_TOKEN};
 	
 	public static Method METHOD_GET_ACCESS_TOKEN;
 
 	private static Context context;
 	private static String username = null;
 	private static String password = null;
 	private static List<Class<?>> knownModels;
 	private static String accessToken = null;
 
 	static {
 		try {
 			METHOD_GET_ACCESS_TOKEN = ApiHelper.class.getMethod("getAccessTokenSynchronous", DataHelperDelegate.class);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 				
 		knownModels = new ArrayList<Class<?>>();
 		knownModels.add(FoodSearchResponse.class);
 		knownModels.add(NewCommentResponse.class);
 		knownModels.add(NewMessageResponse.class);
 		knownModels.add(OAuth2TokenResponse.class);
 		knownModels.add(RegisterUserResponse.class);
 		knownModels.add(SyncDiaryObject.class);
 
 		knownModels.add(ActivityLevels.class);
 		knownModels.add(CommunityMessage.class);
 		knownModels.add(CommunityMessageComment.class);
 		knownModels.add(CommunityUser.class);
 		knownModels.add(Exercise.class);
 		knownModels.add(ExerciseDiaryEntry.class);
 		knownModels.add(Food.class);
 		knownModels.add(FoodDiaryEntry.class);
 		knownModels.add(Meal.class);
 		knownModels.add(MealItem.class);
 		knownModels.add(MealNutritionInfo.class);
 		knownModels.add(UserProfile.class);
 		knownModels.add(WaterDiaryEntry.class);
 		knownModels.add(WeightDiaryEntry.class);
 	}
 
 	private ApiHelper() {
 		// Prevents instantiation
 	}
 
 	public static String registerUser(String username, String password, String name, String email, Date birthday, Gender gender, String postalCode, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/User/User/";
 
 		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
 		body.add("username", username);
 		body.add("password", password);
 		body.add("name", name);
 		body.add("email", email);
 		body.add("birthdate", new SimpleDateFormat("yyyy-MM-dd").format(birthday));
 		body.add("gender", gender.equals(Gender.MALE) ? "m" : "f");
 		body.add("postal_code", postalCode);
 		body.add("agree_terms", "1");
 
 		RegisterUserResponse response = putRest(url, RegisterUserResponse.class, body, new MediaType("application", "x-www-form-urlencoded"), DataHelper.METHOD_REGISTER_USER, delegate, AuthUsing.HTTP_BASIC);
 		return response != null ? response.getUsername() : null;
 	}
 
 	/**
 	 * Load the currently logged user profile from the remote server.
 	 * @return UserProfile
 	 * @example UserProfile userProfile = ApiHelper.getUserProfile();
 	 */
 	public static UserProfile getUserProfile(DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/User/Profile/?fill=user_id,username,dob,gender,activity_level,height,goal,mode,calories,weight";
 
 		return getRest(url, UserProfile.class, DataHelper.METHOD_GET_USER_PROFILE, delegate, AuthUsing.OAUTH_TOKEN);
 	}
 
 	/**
 	 * Returns the list of activity levels that user can choose from.
 	 * @return ActivityLevels
 	 * @example ActivityLevels levels = ApiHelper.getActivityLevels();
 	 */
 	public static ActivityLevels getActivityLevels(DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Activity/Level/";
 
 		return getRest(url, ActivityLevels.class, DataHelper.METHOD_GET_ACTIVITY_LEVELS, delegate, AuthUsing.HTTP_BASIC);
 	}
 
 	/**
 	 * Search the remote database to find Food objects.
 	 * @param query: the string to search for
 	 * @return FoodSearchResponse
 	 * @example FoodSearchResponse foodSearchResult = ApiHelper.searchFood("sandwich"); 
 	 */
 	public static FoodSearchResponse searchFood(String query, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Food/Foods/?query=" + query + "&limit=50&fill=item_title,cals,serving_size,food_id,images.100,images,verification";
 
 		return getRest(url, FoodSearchResponse.class, DataHelper.METHOD_SEARCH_FOODS, delegate, AuthUsing.HTTP_BASIC);
 	}
 
 	/**
 	 * Search the remote database to find Exercise objects.
 	 * @param query: the string to search for
 	 * @return ExerciseSearchResponse
 	 * @example ExerciseSearchResponse exerciseSearchResult = ApiHelper.searchExercises("run"); 
 	 */
 	public static ExerciseSearchResponse searchExercises(String query, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Fitness/Exercises/?query=" + query + "&limit=50&fill=exercise,fitness_id,images,cal_factor";
 
 		return getRest(url, ExerciseSearchResponse.class, DataHelper.METHOD_SEARCH_EXERCISES, delegate, AuthUsing.HTTP_BASIC);
 	}
 
 	/**
 	 * Load complete Food information from multiple IDs (foodId).
 	 * @param foodIds
 	 * @return List<Food>
 	 * @example List<Food> foods = ApiHelper.getFood(foodIds);
 	 */
 	public static List<Food> getFoods(Collection<Integer> foodIds, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Food/Foods/?foodId=" + StringUtils.arrayToCommaDelimitedString(foodIds.toArray());
 
 		Food[] foods = getRest(url, Food[].class, DataHelper.METHOD_GET_FOODS, delegate, AuthUsing.HTTP_BASIC);
 		return foods != null ? Arrays.asList(foods) : null;
 	}
 
 	/**
 	 * Load complete Food information from multiple IDs (foodId).
 	 * @param exerciseIds
 	 * @return List<Exercise>
 	 * @example List<Exercise> exercises = ApiHelper.getExercises(exerciseIds);
 	 */
 	public static List<Exercise> getExercises(Collection<Integer> exerciseIds, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/fitness/exercises/?fitnessId=" + StringUtils.arrayToCommaDelimitedString(exerciseIds.toArray());
 
 		Exercise[] exercises = getRest(url, Exercise[].class, DataHelper.METHOD_GET_EXERCISES, delegate, AuthUsing.HTTP_BASIC);
 		return exercises != null ? Arrays.asList(exercises) : null;
 	}
 
 	/**
 	 * Load Community messages from the remote server.
 	 * @param pageNum
 	 * @return List<CommunityMessage>
 	 * @example List<CommunityMessage> messages = ApiHelper.getCommunityMessages(1);
 	 */
 	public static List<CommunityMessage> getCommunityMessages(int pageNum, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Dare/Posts/?dare_id=657&page=" + pageNum;
 
 		CommunityMessage[] messages = getRest(url, CommunityMessage[].class, DataHelper.METHOD_GET_COMMUNITY_MESSAGES, delegate, AuthUsing.HTTP_BASIC);
 		return messages != null ? Arrays.asList(messages) : null;
 	}
 
 	/**
 	 * Load the user's own Community messages from the remote server.
 	 * @param pageNum
 	 * @return List<CommunityMessage>
 	 * @example List<CommunityMessage> messages = ApiHelper.getUserOwnCommunityMessages(1);
 	 */
 	public static List<CommunityMessage> getUserOwnCommunityMessages(int pageNum, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Dare/Posts/?dare_id=657&filter=my&page=" + pageNum;
 
 		CommunityMessage[] messages = getRest(url, CommunityMessage[].class, DataHelper.METHOD_GET_USER_OWN_COMMUNITY_MESSAGES, delegate, AuthUsing.OAUTH_TOKEN);
 		return messages != null ? Arrays.asList(messages) : null;
 	}
 
 	/**
 	 * Load the comments on a specific Community message from the remote server.
 	 * @param postId
 	 * @param pageNum
 	 * @return List<CommunityMessageComment>
 	 * @example List<CommunityMessageComment> comments = ApiHelper.getCommunityMessageComments(418891);
 	 */
 	public static List<CommunityMessageComment> getCommunityMessageComments(int postId, int pageNum, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Dare/Comments/?dare_post_id=" + postId + "&page=" + pageNum;
 
 		CommunityMessageComment[] comments = getRest(url, CommunityMessageComment[].class, DataHelper.METHOD_GET_COMMUNITY_MESSAGE_COMMENTS, delegate, AuthUsing.HTTP_BASIC);
 		return comments != null ? Arrays.asList(comments) : null;
 	}
 
 	public static NewMessageResponse postNewMessage(String message, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Dare/Posts/";
 
 		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
 		body.add("dare_id", "657");
 		body.add("post", message);
 		
 		return putRest(url, NewMessageResponse.class, body, new MediaType("application", "x-www-form-urlencoded"), DataHelper.METHOD_POST_NEW_MESSAGE, delegate, AuthUsing.OAUTH_TOKEN);
 	}
 
 	public static NewCommentResponse postNewComment(int messageId, String comment, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/Dare/Comments/";
 
 		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
 		body.add("dare_post_id", messageId + "");
 		body.add("comment", comment);
 
 		return putRest(url, NewCommentResponse.class, body, new MediaType("application", "x-www-form-urlencoded"), DataHelper.METHOD_POST_NEW_COMMENT, delegate, AuthUsing.OAUTH_TOKEN);
 	}
 
 	public static SyncDiaryObject syncDiary(SyncDiaryObject localChanges, DataHelperDelegate delegate) {
 		String lastSyncToken = "";
 
 		long lastSyncMilliseconds = (Long) DataHelper.getPref(DataHelper.PREFS_LAST_SYNC_TOKEN, (long) 0);
 		if (lastSyncMilliseconds > 0) {
 			Date lastSyncDate = new Date();
 			lastSyncDate.setTime(lastSyncMilliseconds);
 			lastSyncToken = ISO8601DateParser.toString(lastSyncDate);
 		}
 
 		String url = getBaseUrl() + "/sync/diary/?since=" + lastSyncToken;
 		SyncDiaryObject remoteChanges = postRest(url, SyncDiaryObject.class, localChanges, new MediaType("application", "json"), DataHelper.METHOD_SYNC_DIARY, delegate, AuthUsing.OAUTH_TOKEN);
 		if (remoteChanges != null && remoteChanges.getSyncToken() != null) {
 			// Save the SyncToken we received from the API, to be used on the next syncDiary.
 			DataHelper.setPref(DataHelper.PREFS_LAST_SYNC_TOKEN, remoteChanges.getSyncToken().getTime());
 		}
 		remoteChanges.removeDeletedEntries();
 		return remoteChanges;
 	}
 
 	public static void syncUserProfile(UserProfile modifiedUserProfile, DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/user/profile/";
 		postRest(url, UserProfile.class, modifiedUserProfile, new MediaType("application", "json"), DataHelper.METHOD_SYNC_USER_PROFILE, delegate, AuthUsing.OAUTH_TOKEN);
 	}
 
 	@SuppressWarnings("unused")
 	private static String getBaseUrl() {
 		if (BuildValues.IS_STAGING) {
 			return "https://service.staging.livestrong.com/service";
 		}
 		return "https://service.livestrong.com/service";
 	}
 
 	@SuppressWarnings("unused")
 	private static String getBaseUrlWebsite() {
 		if (BuildValues.IS_STAGING) {
 			return "https://android.staging.livestrong.com";
 		}
 		return "https://www.livestrong.com";
 	}
 
 	public static void initialize(Context ctx) {
 		context = ctx;
 		username = DataHelper.getPref(DataHelper.PREFS_USERNAME, (String) null);
 		password = DataHelper.getPref(DataHelper.PREFS_PASSWORD, (String) null);
 		if (username != null && password != null) {
 			getAccessToken(null);
 		}
 	}
 
 	public static UserProfile authenticate(String user, String pass, final DataHelperDelegate delegate) {
 		username = user;
 		password = pass;
 		if (delegate != null) {
 			getAccessToken(new DataHelperDelegate () {
 				@Override
 				public void dataReceivedThreaded(Method methodCalled, Object data) {
 					if (accessToken != null) {
 						DataHelper.getUserProfile(delegate);
 					} else {
 						delegate.dataReceivedThreaded(DataHelper.METHOD_GET_USER_PROFILE, null);
 					}
 				}
 
 				@Override
 				public boolean errorOccurredThreaded(Method methodCalled, Exception error, String errorMessage) {
 					return delegate.errorOccurredThreaded(methodCalled, error, errorMessage);
 				}
 			});
 		} else {
 			getAccessToken(null);
 			return DataHelper.getUserProfile(null);
 		}
 		return null;
 	}
 	
 	public static void persistAuthData() {
 		// Persist the username & password
 		DataHelper.setPref(DataHelper.PREFS_USERNAME, username);
 		DataHelper.setPref(DataHelper.PREFS_PASSWORD, password);
 	}
 
 	public static void resetAuthData(){
 		username = null;
 		password = null;
 		persistAuthData();
 	}
 	public static void getAccessTokenSynchronous(DataHelperDelegate delegate) {
 		String url = getBaseUrl() + "/OAuth2/Token/";
 
 		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
 		body.add("grant_type", "password");
 		body.add("username", username);
 		body.add("password", password);
 		body.add("scope", "myplate,community");
 		
 		OAuth2TokenResponse response = postRest(url, OAuth2TokenResponse.class, body, new MediaType("application", "x-www-form-urlencoded"), METHOD_GET_ACCESS_TOKEN, delegate, AuthUsing.HTTP_BASIC);
 		if (response != null) {
 			accessToken = response.getAccessToken();
 			DataHelper.setPref(DataHelper.PREFS_ACCESS_TOKEN, accessToken);
 			DataHelper.setPref(DataHelper.PREFS_REFRESH_TOKEN, response.getRefreshToken());
 			persistAuthData();
 		}
 	}
 
 	public static void getAccessToken(DataHelperDelegate delegate) {
 		accessToken = DataHelper.getPref(DataHelper.PREFS_ACCESS_TOKEN, (String) null);
 		
 		if (accessToken == null) {
 			if (delegate == null) {
 				getAccessTokenSynchronous(delegate);
 				return;
 			}
 			AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, METHOD_GET_ACCESS_TOKEN);
 			asyncTask.execute(delegate);
 		}
 	}
 
 	private static void refreshAccessToken(Method methodCalled, DataHelperDelegate delegate) {
 		accessToken = null;
 		DataHelper.setPref(DataHelper.PREFS_ACCESS_TOKEN, null);
 
 		String refreshToken = DataHelper.getPref(DataHelper.PREFS_REFRESH_TOKEN, (String) null);
 		if (refreshToken == null) {
 			getAccessToken(null);
 			return;
 		}
 
 		String url = getBaseUrl() + "/OAuth2/Token/";
 
 		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
 		body.add("grant_type", "refresh_token");
 		body.add("refresh_token", refreshToken);
 
 		OAuth2TokenResponse response = postRest(url, OAuth2TokenResponse.class, body, new MediaType("application", "x-www-form-urlencoded"), methodCalled, delegate, AuthUsing.HTTP_BASIC);
 		if (response != null) {
 			accessToken = response.getAccessToken();
 			DataHelper.setPref(DataHelper.PREFS_ACCESS_TOKEN, accessToken);
 			DataHelper.setPref(DataHelper.PREFS_REFRESH_TOKEN, response.getRefreshToken());
 		}
 	}
 
 	public static class StagingTM implements TrustManager, X509TrustManager {
 		public X509Certificate[] getAcceptedIssuers() {
 			return null;
 		}
 
 		public boolean isServerTrusted(X509Certificate[] certs) {
 			return true;
 		}
 
 		public boolean isClientTrusted(X509Certificate[] certs) {
 			return true;
 		}
 
 		public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
 			return;
 		}
 
 		public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
 			return;
 		}
 	}
 
 	private static <D> D getRest(String url, Class<D> responseObjectClass, Method methodCalled, DataHelperDelegate delegate, AuthUsing authUsing) {
 		return requestRest(HttpMethod.GET, url, responseObjectClass, null, null, methodCalled, delegate, authUsing);
 	}
 
 	private static <D> D postRest(String url, Class<D> responseObjectClass, Object body, MediaType contentType, Method methodCalled, DataHelperDelegate delegate, AuthUsing authUsing) {
 		return requestRest(HttpMethod.POST, url, responseObjectClass, body, contentType, methodCalled, delegate, authUsing);
 	}
 
 	private static <D> D putRest(String url, Class<D> responseObjectClass, Object body, MediaType contentType, Method methodCalled, DataHelperDelegate delegate, AuthUsing authUsing) {
 		return requestRest(HttpMethod.PUT, url, responseObjectClass, body, contentType, methodCalled, delegate, authUsing);
 	}
 
 	private static <D> D requestRest(HttpMethod httpMethod, String url, Class<D> responseObjectClass, Object body, MediaType contentType, Method methodCalled, DataHelperDelegate delegate, AuthUsing authUsing) {
 		return requestRest(httpMethod, url, responseObjectClass, body, contentType, true, methodCalled, delegate, authUsing);
 	}
 
 	private static <D> D requestRest(HttpMethod httpMethod, String url, Class<D> responseObjectClass, Object body, MediaType contentType, boolean retry, Method methodCalled, DataHelperDelegate delegate, AuthUsing authUsing) {
 		ApiHelperResponse<D> apiResponse;
 		
 		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
 			//TODO: Remove this after completing the refactory of the network queries
 			AsyncApiHelper<D> asyncTask = new AsyncApiHelper<D>();
 			asyncTask.execute(httpMethod, url, responseObjectClass, body, contentType, retry, methodCalled, delegate, authUsing);
 			while (!asyncTask.done) {
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {}
 			}
 			apiResponse = asyncTask.response;
 		} else {
 			apiResponse = requestRestSynchronous(httpMethod, url, responseObjectClass, body, contentType, retry, methodCalled, delegate, authUsing);
 		}
 		
 		if (apiResponse.error) {
 			if (delegate == null || !delegate.errorOccurredThreaded(methodCalled, apiResponse.exception, apiResponse.errorMessage)) {
 				// Error wasn't handled in delegate.errorOccurred; try to display our own error message.
 				if (apiResponse.errorMessage == null || apiResponse.errorMessage.length() == 0) {
 					// Nothing to show.
 					return null;
 				}
 				try {
 					final ApiHelperResponse<D> apiResponseF = apiResponse;
 					((Activity) MyPlateApplication.getFrontMostActivity()).runOnUiThread(new Runnable() {
 						public void run() {
 							new AlertDialog.Builder(MyPlateApplication.getFrontMostActivity())
 						      .setMessage(apiResponseF.errorMessage)
 						      .setTitle(R.string.error)
 						      .setNeutralButton(android.R.string.ok,
 						         new DialogInterface.OnClickListener() {
 						         public void onClick(DialogInterface dialog, int whichButton){}
 						         })
 						      .show();
 						}
 					});
 				} catch (Exception e) {
 					e.printStackTrace();
 				}	
 			}
 		}
 		
 		return apiResponse.response;
 	}
 
 	public synchronized static <D> ApiHelperResponse<D> requestRestSynchronous(HttpMethod httpMethod, String url, Class<D> responseObjectClass, Object body, MediaType contentType, boolean retry, Method methodCalled, DataHelperDelegate delegate, AuthUsing authUsing) {
 		if (!isOnline()) {
 			return new ApiHelperResponse<D>(null, null);
 		}
 		try {
 			if (BuildValues.IGNORE_INVALID_SSL_CERTS || BuildValues.PROXY_PORT > 0) {
 				// Create a trust manager that does not validate certificate chains:
 				SSLContext sc = SSLContext.getInstance("SSL");
 				sc.init(null, new TrustManager[] { new StagingTM() }, null);
 				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
 				HostnameVerifier hv = new HostnameVerifier() {
 					public boolean verify(String urlHostName, SSLSession session) {
 						return true;
 					}
 				};
 				HttpsURLConnection.setDefaultHostnameVerifier(hv);
 			} else {
 				// Use a custom keystore
 				final KeyStore ks = KeyStore.getInstance("BKS");
 				final InputStream in = context.getResources().openRawResource(R.raw.livestrongstore);
 				try {
 					ks.load(in, "changeit".toCharArray());
 				} finally {
 					in.close();
 				}
 
 				HttpsURLConnection.setDefaultSSLSocketFactory(new AdditionalKeyStoresSSLSocketFactory(ks, HttpsURLConnection.getDefaultSSLSocketFactory()));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		GsonBuilder gsonBuilder = new GsonBuilder();
 
 		// Use LiveStrongDataObjectTypeAdapter to deserialize all our own objects
 		for (Class<?> modelClass : knownModels) {
 			gsonBuilder.registerTypeAdapter(modelClass, new LiveStrongDataObjectTypeAdapter(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));
 		}
 
 		Gson gson = gsonBuilder.create();
 		GsonHttpMessageConverter messageConverter = new GsonHttpMessageConverter(gson);
 		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
 		messageConverters.add(messageConverter);
 		messageConverters.add(new FormHttpMessageConverter());
 
 		HttpHeaders requestHeaders = new HttpHeaders();
 		requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
 		
 		switch (authUsing) {
 		case HTTP_BASIC:
 			requestHeaders.setAuthorization(new HttpBasicAuthentication(api_secret, api_key));
 			break;
 		case OAUTH_TOKEN:
 			requestHeaders.setAuthorization(new OAuth2Authentication(accessToken));
 			break;
 		}
 
 		if (contentType != null) {
 			requestHeaders.setContentType(contentType);
 		}
 
 		HttpEntity<?> requestEntity = null;
 		if (body != null) {
 			requestEntity = new HttpEntity<Object>(body, requestHeaders);
 		} else {
 			requestEntity = new HttpEntity<Object>(requestHeaders);
 		}
 
 		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
 
 		// Proxy (configured in Android settings)
 		String proxyHost = android.net.Proxy.getHost(context);
 		int proxyPort = android.net.Proxy.getPort(context);
 
 		// Hard-coded proxy from local.properties
 		if (BuildValues.PROXY_PORT > 0) {
 			proxyHost = BuildValues.PROXY_HOST;
 			proxyPort = BuildValues.PROXY_PORT;
 		}
 
 		if (proxyHost != null && proxyHost.trim().length() > 0 && proxyPort > 0) {
 			SimpleClientHttpRequestFactory factory = ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory());
 			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
 			factory.setProxy(proxy);
 		}
 
 		restTemplate.setMessageConverters(messageConverters);
 		Log.d(ApiHelper.class.getName(), "Sending API request: " + httpMethod + " " + url);
 		try {
 			ResponseEntity<D> responseEntity = restTemplate.exchange(url, httpMethod, requestEntity, responseObjectClass);
 			Log.d(ApiHelper.class.getName(), "Received API response from " + httpMethod + " " + url);
 			return new ApiHelperResponse<D>(responseEntity.getBody());
 		} catch (HttpClientErrorException e) {
 			// 3xx/4xx status codes
 			if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
 				if (url.contains("/OAuth2/Token/") && body != null && ((MultiValueMap<String, String>) body).get("refresh_token") != null) {
 					Log.i(ApiHelper.class.getName(), "Received 403 Forbidden status code to OAuth2 refresh request. Will try to get a new access token.");
 					DataHelper.setPref(DataHelper.PREFS_ACCESS_TOKEN, null);
 					getAccessToken(delegate);
 				} else if (retry) {
 					Log.i(ApiHelper.class.getName(), "Received 403 Forbidden status code; will refresh the access token and retry.");
 					refreshAccessToken(methodCalled, delegate);
 					return requestRestSynchronous(httpMethod, url, responseObjectClass, body, contentType, false, methodCalled, delegate, authUsing);
 				} else {
 					Log.e(ApiHelper.class.getName(), "Received 403 Forbidden status code; already retried. Aborting.");
 				}
 			}
			else if(e.getStatusCode() == HttpStatus.BAD_REQUEST)
			{
				Log.e(ApiHelper.class.getName(), "Received 400 Bad Request: Username already exists.");
			}
 			return handleApiErrors(gson, e);
 		} catch (HttpServerErrorException e) {
 			// 5xx status codes
 			return handleApiErrors(gson, e);
 		} catch (java.lang.NullPointerException e) {
 			Log.e(ApiHelper.class.getName(), "Can't connect to API:");
 			return new ApiHelperResponse<D>(e, "Can't connect to LIVESTRONG server.");
 		} catch (java.lang.IllegalArgumentException e) {
 			Log.e(ApiHelper.class.getName(), "Can't connect to API:");
 			return new ApiHelperResponse<D>(e, "Can't connect to LIVESTRONG server.");
 		} catch (org.springframework.web.client.ResourceAccessException e) {
 			Log.e(ApiHelper.class.getName(), "Can't connect to API (proxy error?):");
 			e.getMostSpecificCause().printStackTrace();
 			return new ApiHelperResponse<D>(e, "Can't connect to LIVESTRONG server.");
 		} catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
 			Log.e(ApiHelper.class.getName(), "JSON parsing error occurred:");
 			e.getMostSpecificCause().printStackTrace();
 			Log.e(ApiHelper.class.getName(), "Did you forget to add the class " + responseObjectClass.getName() + " to ApiHelper.knownModels?");
 			return new ApiHelperResponse<D>(e, "Error trying to decode the response from the LIVESTRONG server.");
 		}
 	}
 	
 	private static <D> ApiHelperResponse<D> handleApiErrors(Gson gson, HttpStatusCodeException e) {
		String json = e.getResponseBodyAsString();
 		String errorMessage = "An unknown error occurred on the server.";
 		try {
 			Map<String, Object> error = gson.fromJson(json, Map.class);
 			errorMessage = Html.fromHtml((String) error.get("error_msg")).toString();
 		} catch (Exception e2) {
 		}
 		
		if(e.getStatusCode() == HttpStatus.BAD_REQUEST)
		{
			errorMessage = "That username or email is already in use.";
		}

 		Log.e(ApiHelper.class.getName(), "API error occurred (" + e.getStatusCode() + "): " + errorMessage);
 		e.getMostSpecificCause().printStackTrace();
 		
 		return new ApiHelperResponse<D>(e, errorMessage);
 	}
 
 	public static boolean isOnline() {
 		ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeInfo = connec.getActiveNetworkInfo();
 		if (activeInfo != null && (activeInfo.isConnected() || activeInfo.getState().equals(State.CONNECTED))) {
 			return true;
 		}
 
 		NetworkInfo mobileInfo = connec.getNetworkInfo(0);
 		NetworkInfo wifiInfo = connec.getNetworkInfo(1);
 		NetworkInfo wimaxInfo = connec.getNetworkInfo(6);
 		boolean bm = mobileInfo != null ? (mobileInfo.isConnected() || mobileInfo.getState().equals(State.CONNECTED)) : false;
 		boolean bw = wimaxInfo != null ? (wimaxInfo.isConnected() || wimaxInfo.getState().equals(State.CONNECTED)) : false;
 		boolean bx = wifiInfo != null ? (wifiInfo.isConnected() || wifiInfo.getState().equals(State.CONNECTED)) : false;
 		
 		if (bm || bw || bx){
 			return true;
 		} else {
 			Log.d("ApiHelper", "WARNING: is NOT online");
 			return false;
 		}
 	}
 }
