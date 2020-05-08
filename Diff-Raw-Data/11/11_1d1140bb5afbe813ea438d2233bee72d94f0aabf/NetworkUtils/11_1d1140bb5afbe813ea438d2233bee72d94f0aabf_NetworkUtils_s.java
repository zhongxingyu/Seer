 package com.jalinsuara.android.helper;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Array;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.text.format.Formatter;
 import android.util.Log;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.reflect.TypeToken;
 import com.jalinsuara.android.JalinSuaraSingleton;
 import com.jalinsuara.android.news.model.Comment;
 import com.jalinsuara.android.news.model.News;
 import com.jalinsuara.android.projects.model.District;
 import com.jalinsuara.android.projects.model.Province;
 import com.jalinsuara.android.projects.model.SubDistrict;
 import com.jalinsuara.android.projects.model.SubProject;
 import com.jalinsuara.android.search.SearchResult;
 
 /**
  * Helper for accessing web service API
  * 
  * @author tonoman3g
  * @author gabriellewp
  * 
  */
 public class NetworkUtils {
 
 	/**
 	 * Logger
 	 */
 	private static Logger log = LoggerFactory
 			.getLogger(JalinSuaraSingleton.class.getSimpleName());
 
 	/**
 	 * Gson for deserialize json response
 	 */
 	private static Gson mGson;
 
 	/** POST parameter name for the user's account name */
 	public static final String PARAM_USERNAME = "username";
 
 	/** POST parameter name for the user's password */
 	public static final String PARAM_PASSWORD = "password";
 
 	/** Timeout (in ms) we specify for each http request */
 	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
 
 	/**
 	 * without this params, it still returns about 30 item in one page
 	 */
 	public static final String PARAM_PER_PAGE = "per_page";
 	/**
 	 * Default value for {@link NetworkUtils#PARAM_PER_PAGE}
 	 */
 	public static final int DEFAULT_PER_PAGE = 30;
 
 	/** Base URL */
 	public static String BASE_URL = "http://jalinsuara.web.id/api/v1";
 
 	private static DefaultHttpClient mHttpClient;
 
 	/**
 	 * Get local ip address
 	 * 
 	 * @return
 	 */
 	public static String getLocalIpAddress(Context context) {
 		WifiManager wifi = (WifiManager) context
 				.getSystemService(Context.WIFI_SERVICE);
 
 		// Get WiFi status
 		WifiInfo info = wifi.getConnectionInfo();
 		return Formatter.formatIpAddress(info.getIpAddress());
 	}
 
 	/**
 	 * Check whether network is down
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static boolean isNetworkDown(Context context) {
 		ConnectivityManager connectivityManager = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		// getNetworkInfo(MOBILE) returns null in xoom, so check null first
 		NetworkInfo mobile_info = connectivityManager
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		NetworkInfo wifi_info = connectivityManager
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 		return !((mobile_info != null && mobile_info.getState() == NetworkInfo.State.CONNECTED) || (wifi_info != null && wifi_info
 				.getState() == NetworkInfo.State.CONNECTED));
 	}
 
 	/**
 	 * Configures the httpClient to connect to the URL provided.
 	 */
 	public static HttpClient getHttpClient() {
 		if (mHttpClient == null) {
 			mHttpClient = new DefaultHttpClient();
 			final HttpParams params = mHttpClient.getParams();
 			HttpConnectionParams.setConnectionTimeout(params,
 					HTTP_REQUEST_TIMEOUT_MS);
 			HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
 			ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
 		}
 		return mHttpClient;
 	}
 
 	/**
 	 * Get post from server
 	 * 
 	 * @return
 	 */
 	public static ArrayList<News> getPosts(int page) {
 		final HttpResponse resp;
 		String uri = BASE_URL + "/posts";
 
 		if (page <= 0) {
 			uri = BASE_URL + "/posts?page=1&" + PARAM_PER_PAGE + "="
 					+ DEFAULT_PER_PAGE;
 		} else {
 			uri = BASE_URL + "/posts?page=" + page + "&" + PARAM_PER_PAGE + "="
 					+ DEFAULT_PER_PAGE;
 		}
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 		try {
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					if (sb.length() <= 30) {
 						log.info("Response: " + sb.toString());
 					} else {
 						log.info("Response: " + sb.toString().substring(0, 29));
 					}
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Type collectionType = new TypeToken<ArrayList<News>>() {
 							}.getType();
 							ArrayList<News> retval = getGson().fromJson(
 									response, collectionType);
 							return retval;
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Search for news or sub projects by a certain query
 	 * 
 	 * @param query
 	 * @return
 	 */
 	public static ArrayList<SearchResult> getSearch(String query, int page) {
 		final HttpResponse resp;
		String uri = "http://jalinsuara.web.id/en" + "/home/search";
 
 		if (page <= 0) {
 
 		} else {
 
 		}
 
 		log.info("Request: " + uri);
 		final HttpPost request = new HttpPost(uri);
 
 		try {
 
 			// add query
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 
 			nameValuePairs.add(new BasicNameValuePair("query", query));
 			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					log.info("Response: " + sb.toString());
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							ArrayList<SearchResult> retval = new ArrayList<SearchResult>();
 							JsonParser parser = new JsonParser();
 							JsonElement resElmt = parser.parse(response);
 							if (resElmt.isJsonArray()) {
 								JsonArray resArr = resElmt.getAsJsonArray();
 								for (int i = 0; i < resArr.size(); i++) {
 									JsonElement elmt = resArr.get(i);
 									if (elmt.isJsonObject()) {
 										JsonObject obj = elmt.getAsJsonObject();
 										String objString = elmt.toString();
 										JsonElement blmAmountElmt = obj
 												.get("blm_amount");
 										SearchResult retvalElmt = new SearchResult();
 										if (blmAmountElmt != null) {
 											retvalElmt.setNews(false);
 
 											SubProject project = getGson()
 													.fromJson(objString,
 															SubProject.class);
 											retvalElmt.setProjects(project);
 
 										} else {
 											retvalElmt.setNews(true);
 											News news = getGson().fromJson(
 													objString, News.class);
 											retvalElmt.setNews(news);
 										}
 										retval.add(retvalElmt);
 									}
 								}
 							}
 
 							return retval;
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get sub project from server
 	 * 
 	 * @param page
 	 *            if page == -1 no parameter
 	 * @return
 	 */
 	public static ArrayList<SubProject> getSubProjects(int page) {
 		final HttpResponse resp;
 		String uri = null;
 		if (page <= 0) {
 			uri = BASE_URL + "/activities?page=1&" + PARAM_PER_PAGE + "="
 					+ DEFAULT_PER_PAGE;
 		} else {
 			uri = BASE_URL + "/activities?page=" + page + "&" + PARAM_PER_PAGE
 					+ "=" + DEFAULT_PER_PAGE;
 		}
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 		try {
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					log.info("Response retrieved");
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<SubProject>>() {
 							}.getType();
 							ArrayList<SubProject> retval = gson.fromJson(
 									response, collectionType);
 							return retval;
 
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Retrieve all provinces
 	 * 
 	 * @param page
 	 * @return
 	 */
 	public static ArrayList<Province> getProvinces() {
 		final HttpResponse resp;
 		String uri = null;
 		uri = BASE_URL + "/provinces";
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 		try {
 
 			resp = getHttpClient().execute(request);
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 					log.info("Response retrieved");
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<Province>>() {
 							}.getType();
 							ArrayList<Province> retval = gson.fromJson(
 									response, collectionType);
 							return retval;
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Get all districts
 	 * 
 	 * @param page
 	 * @return
 	 */
 	public static ArrayList<District> getDistricts(int page) {
 		final HttpResponse resp;
 		String uri = null;
 
 		if (page <= 0) {
 			uri = BASE_URL + "/districts?page=1&" + PARAM_PER_PAGE + "="
 					+ DEFAULT_PER_PAGE;
 		} else {
 			uri = BASE_URL + "/districts?page=" + page + "&" + PARAM_PER_PAGE
 					+ "=" + DEFAULT_PER_PAGE;
 		}
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 		try {
 
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					log.info("Response retrieved");
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<District>>() {
 							}.getType();
 							ArrayList<District> retval = gson.fromJson(
 									response, collectionType);
 							return retval;
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Get districts by province_id
 	 * 
 	 * @param page
 	 * @return
 	 */
 	public static ArrayList<District> getDistricts(long provinceId, int page) {
 		final HttpResponse resp;
 		String uri = null;
 
 		if (provinceId <= 0) {
 			return null;
 		} else {
 			if (page <= 0) {
 				uri = BASE_URL + "/provinces/" + provinceId + "/districts"
 						+ "?page=1&" + PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 			} else {
 				uri = BASE_URL + "/provinces/" + provinceId + "/districts"
 						+ "?page=" + page + "&" + PARAM_PER_PAGE + "="
 						+ PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 			}
 		}
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 
 		try {
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					log.info("Response retrieved");
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<District>>() {
 							}.getType();
 							ArrayList<District> retval = gson.fromJson(
 									response, collectionType);
 							return retval;
 
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Get Subdistrict
 	 * 
 	 * @param page
 	 * @return
 	 */
 	public static ArrayList<SubDistrict> getSubdistricts(int page) {
 		final HttpResponse resp;
 		String uri = null;
 		if (page <= 0) {
 			uri = BASE_URL + "/subdistricts?page=1&" + PARAM_PER_PAGE + "="
 					+ DEFAULT_PER_PAGE;
 		} else {
 			uri = BASE_URL + "/subdistricts?page=" + page + "&"
 					+ PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 		}
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 		try {
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 					log.info("Response retrieved");
 					ireader.close();
 					String response = sb.toString();
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<SubDistrict>>() {
 							}.getType();
 							ArrayList<SubDistrict> retval = gson.fromJson(
 									response, collectionType);
 							return retval;
 
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Get subdistricts by districtId
 	 * 
 	 * @param districtId
 	 * @param page
 	 * @return
 	 */
 	public static ArrayList<SubDistrict> getSubdistricts(long districtId,
 			int page) {
 		final HttpResponse resp;
 		String uri = null;
 		if (districtId <= 0) {
 			return null;
 		} else {
 			if (page <= 0) {
 				uri = BASE_URL + "/districts/" + districtId + "/subdistricts"
 						+ "?page=1&" + PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 			} else {
 				uri = BASE_URL + "/districts/" + districtId + "/subdistricts"
 						+ "?page=" + page + "&" + PARAM_PER_PAGE + "="
 						+ PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 			}
 
 		}
 
 		log.info("Request: " + uri);
 		final HttpGet request = new HttpGet(uri);
 		try {
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					log.info("Response retrieved");
 					ireader.close();
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<SubDistrict>>() {
 							}.getType();
 							ArrayList<SubDistrict> retval = gson.fromJson(
 									response, collectionType);
 							return retval;
 
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Get comment from server
 	 * 
 	 * @return
 	 */
 	public static ArrayList<Comment> getComment(long postId, int page) {
 		final HttpResponse resp;
 
 		String uri = null;
 		if (page <= 0) {
 			uri = BASE_URL + "/posts/" + postId + "/comments?page=1&"
 					+ PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 		} else {
 			uri = BASE_URL + "/posts/" + postId + "/comments?page=" + page
 					+ "&" + PARAM_PER_PAGE + "=" + DEFAULT_PER_PAGE;
 		}
 
 		log.info("Request: " + uri + ", page: " + page);
 		final HttpGet request = new HttpGet(uri);
 		try {
 			resp = getHttpClient().execute(request);
 
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 
 					log.info("Response: " + sb.toString());
 					ireader.close();
 
 					String response = sb.toString();
 
 					if (response.length() > 0) {
 						try {
 							Gson gson = getGson();
 							Type collectionType = new TypeToken<ArrayList<Comment>>() {
 							}.getType();
 							ArrayList<Comment> retval = gson.fromJson(response,
 									collectionType);
 							return retval;
 
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						return null;
 					}
 				}
 			} else {
 				log.error("Error: " + resp.getStatusLine());
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	/*
 	 * get token for register new user
 	 */
 	public static String registerNewUser(String name, String email,
 			String email_password) {
 		final HttpResponse resp;
 		String uri = null;
 		if (name != null && email != null && email_password != null) {
 			uri = BASE_URL + "/users";
 
 		}
 		final HttpPost request = new HttpPost(uri);
 		try {
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 			nameValuePairs.add(new BasicNameValuePair("user[email]",
 					"gabybongbong@gmail.com"));
 			nameValuePairs.add(new BasicNameValuePair("user[username]",
 					"gabrielle"));
 			nameValuePairs
 					.add(new BasicNameValuePair("user[password]", "1234"));
 			nameValuePairs.add(new BasicNameValuePair(
 					"user[password_confirmation]", "1234"));
 			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			resp = getHttpClient().execute(request);
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 					ireader.close();
 					// String response = sb.toString();
 					// if(response.length()>0){
 					// try{
 					// ArrayList<SearchResult> retval = new
 					// ArrayList<SearchResult>();
 					// JsonParser parser = new JsonParser();
 					// JsonElement resElmt = parser.parse(response);
 					// if (resElmt.isJsonArray()) {
 					// JsonArray resArr = resElmt.getAsJsonArray();
 					// for (int i = 0; i < resArr.size(); i++) {
 					// JsonElement elmt = resArr.get(i);
 					// if (elmt.isJsonObject()) {
 					// JsonObject obj = elmt.getAsJsonObject();
 					// String objString = elmt.toString();
 					// JsonElement blmAmountElmt = obj
 					// .get("authentication_token");
 					//
 					// }
 					// }
 					// }
 					// } catch(Exception e){
 					//
 					// }
 					// }
 				}
 
 			}
 
 		} catch (Exception ex) {
 
 		}
 
 		return "";
 	}
 
 	/*
 	 * Sign in to JalinSuara
 	 */
 	public static String signIn(String email, String email_password) {
 		final HttpResponse resp;
 		String uri = BASE_URL + "/users/sign_in";
 		if (email != null && email_password != null) {
 
 		}
 		final HttpPost request = new HttpPost(uri);
 		try {
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 			nameValuePairs.add(new BasicNameValuePair("user_login[email]",
 					email));
 			nameValuePairs.add(new BasicNameValuePair("user_login[password]",
 					email_password));
 			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			log.info("request to " + request.getRequestLine());
 			resp = getHttpClient().execute(request);
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				InputStream istream = (resp.getEntity() != null) ? resp
 						.getEntity().getContent() : null;
 				if (istream != null) {
 					BufferedReader ireader = new BufferedReader(
 							new InputStreamReader(istream));
 					String line = ireader.readLine();
 					StringBuilder sb = new StringBuilder();
 					while (line != null) {
 						sb.append(line);
 						line = ireader.readLine();
 					}
 					ireader.close();
 					String response = sb.toString();
 					log.info(response);
 					if (response.length() > 0) {
 						try {
 							ArrayList<SearchResult> retval = new ArrayList<SearchResult>();
 							JsonParser parser = new JsonParser();
 							JsonElement resElmt = parser.parse(response);
 							if (resElmt.isJsonArray()) {
 								JsonArray resArr = resElmt.getAsJsonArray();
 								for (int i = 0; i < resArr.size(); i++) {
 									JsonElement elmt = resArr.get(i);
 									if (elmt.isJsonObject()) {
 										JsonObject obj = elmt.getAsJsonObject();
 										String objString = elmt.toString();
 										JsonElement blmAmountElmt = obj
 												.get("authentication_token");
 
 									}
 								}
 							}
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 			} else {
 				// failed
 				log.error(resp.getStatusLine().getStatusCode() + "");
 
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		// return null because something error
 		return null;
 	}
 
 	/**
 	 * Sign out from JalinSuara
 	 * 
 	 * @param email
 	 * @return
 	 */
 	public static boolean signOut(String email) {
 		final HttpResponse resp;
 		String uri = BASE_URL + "/users/sign_out";
 		if (email != null) {
 
 		}
 		final HttpDeleteWithBody request = new HttpDeleteWithBody(uri);
 		try {
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 			nameValuePairs.add(new BasicNameValuePair("user_login[email]",
 					email));
 
 			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			log.info("request to " + request.getRequestLine());
 			resp = getHttpClient().execute(request);
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				return true;
 
 			} else {
 				// failed
 				log.error(resp.getStatusLine().getStatusCode() + "");
 				return false;
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Get gson
 	 * 
 	 * @return
 	 */
 	public static Gson getGson() {
 		if (mGson == null) {
 			GsonBuilder builder = new GsonBuilder();
 			builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
 			mGson = builder.create();
 		}
 		return mGson;
 	}
 
 }
