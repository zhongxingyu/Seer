 package uk.co.vurt.taskhelper.client;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import uk.co.vurt.hakken.domain.job.JobDefinition;
 import uk.co.vurt.hakken.domain.job.Submission;
 import uk.co.vurt.hakken.domain.task.TaskDefinition;
 import uk.co.vurt.hakken.security.HashUtils;
 import uk.co.vurt.hakken.security.model.LoginResponse;
 import uk.co.vurt.taskhelper.domain.JSONUtil;
 import android.accounts.Account;
 import android.content.Context;
 import android.net.ParseException;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 final public class NetworkUtilities {
 
 	/** The tag used to log to adb console. **/
 	private static final String TAG = "NetworkUtilities";
 
 	/** The Intent extra to store password. **/
 	public static final String PARAM_PASSWORD = "password";
 
 	public static final String PARAM_AUTHTOKEN = "authToken";
 
 	/** The Intent extra to store username. **/
 	public static final String PARAM_USERNAME = "username";
 
 	public static final String PARAM_UPDATED = "timestamp";
 
 	public static final String USER_AGENT = "TaskHelper/1.0";
 
 	public static final int REQUEST_TIMEOUT_MS = 30 * 1000; // ms
 
 	// public static final String BASE_URL = "http://dev.vurt.co.uk/taskhelper";
 	// /**TODO: Load Server URL from resource file?? */
 
 	// public static final String BASE_URL =
 	// "http://10.32.48.36:8080/taskhelper_server"; /**TODO: Load Server URL
 	// from resource file?? */
 	// public static final String BASE_URL =
 	// "http://appsdev.wmfs.net:8280/taskhelper_server"; /**TODO: Load Server
 	// URL from resource file?? */
 
 	public static final String AUTH_URI = "/auth/login";
 
 	public static final String FETCH_JOBS_URI = "/jobs/for/[username]/[hmac]/since/[timestamp]";
 	
 	//getBaseUrl(context) + FETCH_JOBS_URI + "/"
 	//+ parameterMap.get("username") + "/"
 	//+ HashUtils.hash(parameterMap) + "/since/"
 	//+ parameterMap.get("timestamp")
 	
 	// public static final String FETCH_TASK_DEFINITIONS_URI =
 	// "/taskdefinitions";
 
 	public static final String SUBMIT_JOB_DATA_URI = "/submissions/job/";
 
 	private NetworkUtilities() {
 	}
 
 	private static String getBaseUrl(Context context) {
 		return PreferenceManager.getDefaultSharedPreferences(context)
 				.getString("sync_server", null);
 	}
 
 	/**
 	 * Configures the httpClient to connect to the URL provided.
 	 */
 	public static HttpClient getHttpClient() {
 		HttpClient httpClient = new DefaultHttpClient();
 		final HttpParams params = httpClient.getParams();
 		HttpConnectionParams.setConnectionTimeout(params, REQUEST_TIMEOUT_MS);
 		HttpConnectionParams.setSoTimeout(params, REQUEST_TIMEOUT_MS);
 		ConnManagerParams.setTimeout(params, REQUEST_TIMEOUT_MS);
 		return httpClient;
 	}
 
 	/**
 	 * Connects to the server, authenticates the provided username and password.
 	 * 
 	 * @param username
 	 *            The user's username
 	 * @param password
 	 *            The user's password
 	 * @param handler
 	 *            The hander instance from the calling UI thread.
 	 * @param context
 	 *            The context of the calling Activity.
 	 * @return boolean The boolean result indicating whether the user was
 	 *         successfully authenticated.
 	 */
 	public static LoginResponse authenticate(Context context, String username,
 			String password) {
 
 		final HttpResponse resp;
 		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(PARAM_USERNAME, username));
 		params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
 		HttpEntity entity = null;
 		try {
 			entity = new UrlEncodedFormEntity(params);
 		} catch (final UnsupportedEncodingException e) {
 			// this should never happen.
 			throw new AssertionError(e);
 		}
 		String baseUrl = getBaseUrl(context);
 		if (Log.isLoggable(TAG, Log.INFO)) {
 			Log.i(TAG, "Authentication to: " + baseUrl + AUTH_URI);
 		}
 		final HttpPost post = new HttpPost(baseUrl + AUTH_URI);
 		post.addHeader(entity.getContentType());
 		post.setEntity(entity);
 		String authToken = null;
 
 		LoginResponse response = new LoginResponse();
 
 		try {
 			resp = getHttpClient().execute(post);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 				JSONObject loginJson;
 				try {
 					loginJson = new JSONObject(EntityUtils.toString(resp
 							.getEntity()));
 
 					response.setSuccess(loginJson.getBoolean("success"));
 					if (loginJson.has("reason")) {
 						response.setReason(loginJson.getString("reason"));
 					}
 					if (loginJson.has("token")) {
 						response.setToken(loginJson.getString("token"));
 					}
 				} catch (org.apache.http.ParseException e) {
 					response.setSuccess(false);
 					response.setReason(e.getMessage());
 					Log.e(TAG, "Unable to parse login response", e);
 				} catch (JSONException e) {
 					response.setSuccess(false);
 					response.setReason(e.getMessage());
 					Log.e(TAG, "Unable to parse login response", e);
 				}
 
 				// InputStream inputStream = (resp.getEntity() != null) ?
 				// resp.getEntity().getContent() : null;
 				// if(inputStream != null){
 				// BufferedReader reader = new BufferedReader(new
 				// InputStreamReader(inputStream));
 				// authToken = reader.readLine().trim();
 				// }
 			}
 			// if((authToken != null) && (authToken.length() > 0)){
 			// if (Log.isLoggable(TAG, Log.INFO)) {
 			// Log.i(TAG, "Successful authentication: " + authToken);
 			// }
 			// } else {
 			// if (Log.isLoggable(TAG, Log.INFO)) {
 			// Log.i(TAG, "Error authenticating" + resp.getStatusLine());
 			// }
 			// }
 			if (Log.isLoggable(TAG, Log.INFO)) {
 				Log.i(TAG, "Login Response: " + response);
 			}
 		} catch (final IOException e) {
 			if (Log.isLoggable(TAG, Log.INFO)) {
 				Log.i(TAG, "IOException when getting authtoken", e);
 			}
 			response.setReason(e.getMessage());
 		} finally {
 			if (Log.isLoggable(TAG, Log.VERBOSE)) {
 				Log.v(TAG, "getAuthtoken completing");
 			}
 		}
 		return response;
 	}
 
 	/**
 	 * Submit job data back to the server
 	 * 
 	 * essentially json encoded version of the dataitems submitted as form data.
 	 * 
 	 * @param account
 	 * @param authToken
 	 * @return
 	 */
 	public static boolean submitData(Context context, Account account,
 			String authToken, Submission submission) {
 
 		StringEntity stringEntity;
 		try {
 			stringEntity = new StringEntity(JSONUtil.getInstance().toJson(submission));
 			final HttpPost post = new HttpPost(getBaseUrl(context)
 					+ SUBMIT_JOB_DATA_URI);
 			post.setEntity(stringEntity);
 			post.setHeader("Accept", "application/json");
 			post.setHeader("Content-type", "application/json");
 			final HttpResponse httpResponse = getHttpClient().execute(post);
 			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
 				return true;
 			} else {
 				Log.e(TAG, "Data submission failed: "
 						+ httpResponse.getStatusLine().getStatusCode());
 				return false;
 			}
 		} catch (UnsupportedEncodingException e) {
 			Log.e(TAG, "Unable to convert submission to JSON", e);
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "Error submitting json", e);
 		} catch (IOException e) {
 			Log.e(TAG, "Error submitting json", e);
 		}
 		return false;
 
 	}
 
 	public static List<JobDefinition> fetchJobs(Context context,
 			Account account, String authToken, Date lastUpdated)
 			throws JSONException, ParseException, IOException,
 			AuthenticationException {
 		final ArrayList<JobDefinition> jobList = new ArrayList<JobDefinition>();
 
 		SimpleDateFormat dateFormatter = new SimpleDateFormat(
 				"yyyy-MM-dd'T'HH:mm:ss");
 
 		Map<String, String> parameterMap = new HashMap<String, String>();
 		parameterMap.put("username", account.name);
 		parameterMap.put("timestamp", dateFormatter.format(lastUpdated));
 
 		String hmac = HashUtils.hash(parameterMap);
 		parameterMap.put("hmac", hmac);
 		
 		String data = fetchData(replaceTokens(getBaseUrl(context) + FETCH_JOBS_URI, parameterMap)/*, null, null, null*/);
 //				getBaseUrl(context) + FETCH_JOBS_URI + "/"
 //						+ parameterMap.get("username") + "/"
 //						+ HashUtils.hash(parameterMap) + "/since/"
 //						+ parameterMap.get("timestamp"), null, null, null);
 
 		Log.d(TAG, "JOBS DATA: " + data);
 		final JSONArray jobs = new JSONArray(data);
 
 		for (int i = 0; i < jobs.length(); i++) {
			jobList.add(JSONUtil.getInstance().parseJobDefinition(jobs.getJSONObject(i).toString()));
 		}
 		return jobList;
 	}
 
 	private static String fetchData(String url /*, Account account,
 			String authToken, ArrayList<NameValuePair> params*/)
 			throws ClientProtocolException, IOException,
 			AuthenticationException {
 		Log.d(TAG, "Fetching data from: " + url);
 		String data = null;
 //		if (params == null) {
 //			params = new ArrayList<NameValuePair>();
 //		}
 //		if (account != null) {
 //			params.add(new BasicNameValuePair(PARAM_USERNAME, account.name));
 //			params.add(new BasicNameValuePair(PARAM_AUTHTOKEN, authToken));
 //		}
 
 //		Log.i(TAG, params.toString());
 //		HttpEntity entity = new UrlEncodedFormEntity(params);
 
 //		final HttpPost post = new HttpPost(url);
 //		post.addHeader(entity.getContentType());
 //		post.setEntity(entity);
 
 		final HttpGet get = new HttpGet(url);
 
 //		final HttpResponse httpResponse = getHttpClient().execute(post);
 		final HttpResponse httpResponse = getHttpClient().execute(get);
 		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 			data = EntityUtils.toString(httpResponse.getEntity());
 		} else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
 			Log.e(TAG,
 					"Authentication exception in fetching remote task definitions");
 			throw new AuthenticationException();
 		} else {
 			Log.e(TAG, "Server error in fetching remote task definitions: "
 					+ httpResponse.getStatusLine());
 			throw new IOException();
 		}
 		return data;
 	}
 
 	public static List<TaskDefinition> fetchTaskDefinitions(Account account,
 			String authToken, Date lastUpdated) throws JSONException,
 			ParseException, IOException, AuthenticationException {
 		final ArrayList<TaskDefinition> definitionList = new ArrayList<TaskDefinition>();
 
 		/**
 		 * I'm commenting this out for the time being as the current emphasis is
 		 * on working with lists of pre-set jobs, rather than choosing from a
 		 * list of possible tasks. It'll be reinstated as and when required.
 		 */
 		// final ArrayList<NameValuePair> params = new
 		// ArrayList<NameValuePair>();
 		// params.add(new BasicNameValuePair(PARAM_USERNAME, account.name));
 		// params.add(new BasicNameValuePair(PARAM_AUTHTOKEN, authToken));
 		// if (lastUpdated != null) {
 		// final SimpleDateFormat formatter = new
 		// SimpleDateFormat("yyyy/MM/dd HH:mm");
 		// formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
 		// params.add(new BasicNameValuePair(PARAM_UPDATED,
 		// formatter.format(lastUpdated)));
 		// }
 		// Log.i(TAG, params.toString());
 		// HttpEntity entity = null;
 		// entity = new UrlEncodedFormEntity(params);
 		// final HttpPost post = new HttpPost(FETCH_TASK_DEFINITIONS_URI);
 		// post.addHeader(entity.getContentType());
 		// post.setEntity(entity);
 		//
 		// final HttpResponse resp = getHttpClient().execute(post);
 		// final String response = EntityUtils.toString(resp.getEntity());
 		// if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 		// // Succesfully connected to the samplesyncadapter server and
 		// // authenticated.
 		// final JSONArray definitions = new JSONArray(response);
 		// Log.d(TAG, response);
 		// for (int i = 0; i < definitions.length(); i++) {
 		// definitionList.add(TaskDefinition.valueOf(definitions.getJSONObject(i)));
 		// }
 		// } else {
 		// if (resp.getStatusLine().getStatusCode() ==
 		// HttpStatus.SC_UNAUTHORIZED) {
 		// Log.e(TAG,
 		// "Authentication exception in fetching remote task definitions");
 		// throw new AuthenticationException();
 		// } else {
 		// Log.e(TAG, "Server error in fetching remote task definitions: " +
 		// resp.getStatusLine());
 		// throw new IOException();
 		// }
 		// }
 
 		return definitionList;
 	}
 
 	/**
 	 * Utility method to replace named tokens in a string
 	 * 
 	 * @param text
 	 * @param replacements
 	 * @return
 	 */
 	public static String replaceTokens(String text,
 			Map<String, String> replacements) {
 		Pattern pattern = Pattern.compile("\\[(.+?)\\]");
 		Matcher matcher = pattern.matcher(text);
 		StringBuffer buffer = new StringBuffer();
 		while (matcher.find()) {
 			String replacement = replacements.get(matcher.group(1));
 			if (replacement != null) {
 				matcher.appendReplacement(buffer, "");
 				buffer.append(replacement);
 			}
 		}
 		matcher.appendTail(buffer);
 		return buffer.toString();
 	}
 
 }
