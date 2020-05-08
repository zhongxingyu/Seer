 package com.example.ucrinstagram;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import com.google.gson.Gson;
 
 import com.example.ucrinstagram.Models.*;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 // TODO: convert to a service when given the chance, and use string resources
 public class WebAPI {
 
 	private Gson gson = new Gson();
 	// private String apiURL = "http://mgx-dev.com/";
 	private String apiURL = "http://www.mgx-dev.sparkscene.com/";
 
 	public WebAPI() {
 	}
 
 	// ------------------------
 	// ----- User Methods -----
 	// ------------------------
 	public User[] getAllUsers() {
 		String url = apiURL + User.urlSuffix + ".json";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, User[].class);
 	}
 
 	public User getUser(int id) {
 		String url = apiURL + User.urlSuffix + "/" + Integer.toString(id)
 				+ ".json";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, User.class);
 	}
 
 	public User getUser(String username) {
 		String url = apiURL + User.urlSuffix + "/username/" + username
 				+ ".json";
 		Log.i("OC", "Attempting to get User info by username: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, User.class);
 	}
 
 	public Boolean userExists(String username) {
 		String url = apiURL + User.urlSuffix + "/user_exists/" + username;
 		Log.i("OC", "Attempting to check if username exists: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String exists = getJSONFromServer(new HTTPParams(requestMethod, url));
 		if (exists.equals("exists")) {
 			return true;
 		} else { // "does not exist"
 			return false;
 		}
 	}
 
 	// TODO: create some kind of ack, whether the save was successful or not
 	// TODO: boolean return value, or create a set of exceptions/error codes?
 	public User createUser(User user) {
 		String url = apiURL + User.urlSuffix + ".json";
 		Log.i("OC", "Attempting to create a new User: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url,
 				user.getNameValuePairs()));
         return gson.fromJson(json, User.class);
 	}
 
 	public void editUser(User user) {
 		saveUser(user);
 	}
 
 	public User saveUser(User user) {
 		String url = apiURL + User.urlSuffix + "/"
 				+ Integer.toString(user.getId()) + ".json";
 		Log.i("OC", "Attempting to save User info: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.PUT;
         String json = getJSONFromServer(new HTTPParams(requestMethod, url,
 				user.getNameValuePairs()));
         Log.i("OC", json);
         return gson.fromJson(json, User.class);
 	}
 
 	// --------------------------
 	// ----- Friend Methods -----
 	// --------------------------
 	public User[] getFriends(User user) {
 		String url = apiURL + User.urlSuffix + "/get_friends/"
 				+ Integer.toString(user.getId());
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, User[].class);
 	}
 
 	public User[] getFriendedBy(User user) {
 		String url = apiURL + User.urlSuffix + "/get_friends_of/"
 				+ Integer.toString(user.getId());
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, User[].class);
 	}
 
 	public void addFriend(int user_id, int friend_id) {
 		String url = apiURL + User.urlSuffix + "/add_friend";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("user_id", Integer
 				.toString(user_id)));
 		nameValuePairs.add(new BasicNameValuePair("friend_id", Integer
 				.toString(friend_id)));
 		getJSONFromServer(new HTTPParams(requestMethod, url, nameValuePairs));
 	}
 
 	public void addFriend(User user, User friend) {
 		addFriend(user.getId(), friend.getId());
 	}
 
 	public void removeFriend(int user_id, int friend_id) {
 		String url = apiURL + User.urlSuffix + "/remove_friend";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("user_id", Integer
 				.toString(user_id)));
 		nameValuePairs.add(new BasicNameValuePair("friend_id", Integer
 				.toString(friend_id)));
 		getJSONFromServer(new HTTPParams(requestMethod, url, nameValuePairs));
 	}
 
 	public void removeFriend(User user, User friend) {
 		removeFriend(user.getId(), friend.getId());
 	}
 
 	// ---------------------------
 	// ----- UserProfile Methods -----
 	// ---------------------------
 	public UserProfile[] getProfiles() {
 		return null;
 	}
 
 	public UserProfile getProfile(int user_id) {
 		String url = apiURL + User.urlSuffix + "/get_profile/" + user_id;
 		Log.i("OC", "Attempting to get UserProfile info by user id: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json,
 				UserProfile.class);
 	}
 
 	public UserProfile getProfile(User user) {
 		return getProfile(user.getId());
 	}
 
 	public UserProfile saveProfile(UserProfile userProfile) {
 		String url = apiURL + UserProfile.urlSuffix
 				+ "/" + Integer.toString(userProfile.getId()) + ".json";
 		Log.i("OC", "Attempting to edit UserProfile info: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.PUT;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url,
 				userProfile.getNameValuePairs()));
         Log.i("OC", json);
         return gson.fromJson(json,
                 UserProfile.class);
 	}
 
 	public void saveProfileFromUser(
 			UserProfile userProfile, User user) {
 		saveProfile(userProfile);
 	}
 
 	// -------------------------
 	// ----- Photo Methods -----
 	// -------------------------
 	public Photo[] getAllPhotos() {
 		String url = apiURL + Photo.urlSuffix + ".json";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Photo[].class);
 	}
 
 	public Photo getPhoto(int id) {
 		String url = apiURL + Photo.urlSuffix + "/" + Integer.toString(id)
 				+ ".json";
 		Log.i("OC", "Attempting to get Photo info by user id: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Photo.class);
 	}
 
 	public Photo[] getHomeScreenPhotos(int id) {
 		String url = apiURL + User.urlSuffix + "/get_home_screen_photos/"
 				+ Integer.toString(id);
 		Log.i("OC", "Attempting to get home screen photos by user id: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Photo[].class);
 	}
 
 	public Photo[] getHomeScreenPhotos(User user) {
 		return getHomeScreenPhotos(user.getId());
 	}
 
 	public Photo[] getPhotosFromUser(int id) {
 		String url = apiURL + User.urlSuffix + "/get_photos/"
 				+ Integer.toString(id);
 		Log.i("OC", "Attempting to get Photos by user id: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Photo[].class);
 	}
 
 	public Photo[] getPhotosFromUser(User user) {
 		return getPhotosFromUser(user.getId());
 	}
 
 	public void addPhotoToUser(Photo photo, User user) {
 		String url = apiURL + User.urlSuffix + "/add_photo/"
 				+ Integer.toString(user.getId());
 		Log.i("OC", "Attempting to add Photo by user id: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		getJSONFromServer(new HTTPParams(requestMethod, url,
 				photo.getNameValuePairs()));
 	}
 
 	public Comment[] getCommentsFromPhoto(Photo photo) {
 		String url = apiURL + Photo.urlSuffix + "/get_comments/"
 				+ Integer.toString(photo.getId());
 		Log.i("OC", "Attempting to get comments from photos: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Comment[].class);
 	}
 
 	public void addCommentToPhoto(Photo photo, Comment comment) {
 		String url = apiURL + Photo.urlSuffix + "/add_comment/"
 				+ Integer.toString(photo.getId());
 		Log.i("OC", "Attempting to add a Comment to Photo: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		getJSONFromServer(new HTTPParams(requestMethod, url,
 				comment.getNameValuePairs()));
 	}
 
 	public Photo savePhoto(Photo photo) {
 		String url = apiURL + Photo.urlSuffix + "/"
				+ Integer.toString(photo.getId());
 		Log.i("OC", "Attempting to edit Photo info: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.PUT;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url,
 				photo.getNameValuePairs()));
         return gson.fromJson(json, Photo.class);
 	}
 
 	public void removePhoto(Photo photo) {
 		String url = apiURL + Photo.urlSuffix + "/delete/"
 				+ Integer.toString(photo.getId()) + ".json";
 		Log.i("OC", "Attempting to delete photo: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET; // TODO:
 																	// rewrite
 																	// to POST
 		getJSONFromServer(new HTTPParams(requestMethod, url));
 	}
 
 	// -------------------------
 	// ----- Topic Methods -----
 	// -------------------------
 	public Topic[] getTopics() {
 		String url = apiURL + Topic.urlSuffix + ".json";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Topic[].class);
 	}
 
 	public Topic getTopic(int id) {
 		String url = apiURL + Topic.urlSuffix + "/" + Integer.toString(id);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Topic.class);
 	}
 
 	public void saveTopic(Topic topic) {
 	}
 
 	// ---------------------------
 	// ----- Comment Methods -----
 	// ---------------------------
 	public Comment[] getComments() {
 		String url = apiURL + Comment.urlSuffix + ".json";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Comment[].class);
 	}
 
 	public Comment getComment(int id) {
 		String url = apiURL + Comment.urlSuffix + "/" + Integer.toString(id);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Comment.class);
 	}
 
 	public void saveComment(Comment comment) {
 		String url = apiURL + Comment.urlSuffix + "/"
 				+ Integer.toString(comment.getId());
 		Log.i("OC", "Attempting to edit comment: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.PUT;
 		getJSONFromServer(new HTTPParams(requestMethod, url,
 				comment.getNameValuePairs()));
 	}
 
 	public void removeComment(Comment comment) {
 		String url = apiURL + Comment.urlSuffix + "/delete/"
 				+ Integer.toString(comment.getId()) + ".json";
 		Log.i("OC", "Attempting to delete comment: " + url);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET; // TODO:
 																	// rewrite
 																	// to POST
 		getJSONFromServer(new HTTPParams(requestMethod, url));
 	}
 
 	// ----------------------------
 	// ----- Favorite Methods -----
 	// ----------------------------
 
 	public Photo[] getFavorites(int user_id) {
 		String url = apiURL + User.urlSuffix + "/get_favorites/"
 				+ Integer.toString(user_id);
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.GET;
 		String json = getJSONFromServer(new HTTPParams(requestMethod, url));
 		return gson.fromJson(json, Photo[].class);
 	}
 
 	public Photo[] getFavorites(User user) {
 		return getFavorites(user.getId());
 	}
 
 	public void addFavorite(int user_id, int photo_id) {
 		String url = apiURL + User.urlSuffix + "/add_favorite";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("user_id", Integer
 				.toString(user_id)));
 		nameValuePairs.add(new BasicNameValuePair("photo_id", Integer
 				.toString(photo_id)));
 		getJSONFromServer(new HTTPParams(requestMethod, url, nameValuePairs));
 	}
 
 	public void addFavorite(User user, Photo photo) {
 		addFavorite(user.getId(), photo.getId());
 	}
 
 	public void removeFavorite(int user_id, int favorite_id) {
 		String url = apiURL + User.urlSuffix + "/remove_favorite";
 		HTTPRequestMethod requestMethod = HTTPRequestMethod.POST;
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("user_id", Integer
 				.toString(user_id)));
 		nameValuePairs.add(new BasicNameValuePair("favorite_id", Integer
 				.toString(favorite_id)));
 		getJSONFromServer(new HTTPParams(requestMethod, url, nameValuePairs));
 	}
 
 	public void removeFavorite(User user, Photo photo) {
 		removeFavorite(user.getId(), photo.getId());
 	}
 
 	// HELPER METHODS
 	private String getJSONFromServer(HTTPParams params) {
 		String json = null;
 		try {
 			return json = new getJSONFromServer().execute(params).get();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		return "{}";
 	}
 
 	// TODO: Refactor and use reflection when you have the time
 	// private User[] getObjectsFromJSON(Class c) {
 	// Gson gson = new Gson();
 	// String json = getJSONFromServer(apiURL.concat(c.urlSuffixJson));
 	// return gson.fromJson(json, c[].class);
 	// }
 
 }
 
 // TODO: just pass in HTTPClient or HttpURLConnection objects to
 // getJSONFromServer params?
 class HTTPParams {
 	public HTTPRequestMethod requestMethod;
 	public String url;
 	public List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 
 	public HTTPParams(HTTPRequestMethod requestMethod, String url) {
 		this.requestMethod = requestMethod;
 		this.url = url;
 	}
 
 	public HTTPParams(HTTPRequestMethod requestMethod, String url,
 			List<NameValuePair> nameValuePairs) {
 		this.requestMethod = requestMethod;
 		this.url = url;
 		this.nameValuePairs = nameValuePairs;
 	}
 }
 
 enum HTTPRequestMethod {
 	GET, POST, PUT, DELETE
 }
 
 // TODO: should the return value be a JSON value instead of String?
 // TODO: split this into get requests back and sending data
 class getJSONFromServer extends AsyncTask<HTTPParams, Void, String> {
 
 	@Override
 	protected String doInBackground(HTTPParams... params) {
 		HttpURLConnection connection = null;
 		try {
 			URL url = new URL(params[0].url);
 			connection = (HttpURLConnection) url.openConnection();
 			connection.setDoInput(true);
 
 			HTTPRequestMethod requestMethod = params[0].requestMethod;
 
 			if (requestMethod == HTTPRequestMethod.GET) {
 				connection.setRequestMethod("GET");
 			} else if (requestMethod == HTTPRequestMethod.POST) {
 				connection.setRequestMethod("POST");
 				connection.setDoOutput(true);
 
 				OutputStream os = connection.getOutputStream();
 				BufferedWriter writer = new BufferedWriter(
 						new OutputStreamWriter(os, "UTF-8"));
 				writer.write(getQuery(params[0].nameValuePairs));
 				writer.close();
 				os.close();
 			} else if (requestMethod == HTTPRequestMethod.PUT) {
 				connection.setRequestMethod("PUT");
 				connection.setDoOutput(true);
 
 				OutputStream os = connection.getOutputStream();
 				BufferedWriter writer = new BufferedWriter(
 						new OutputStreamWriter(os, "UTF-8"));
 				writer.write(getQuery(params[0].nameValuePairs));
 				writer.close();
 				os.close();
 			} else if (requestMethod == HTTPRequestMethod.DELETE) {
 				connection.setRequestMethod("DELETE");
 			} else {
 				return "{}";
 			}
 
 			Log.i("OC: JSON", "opened http connection");
 
 			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 				Log.i("OC: Open Con", "preparing to get input stream");
 				InputStream in = new BufferedInputStream(
 						connection.getInputStream());
 				return readStream(in);
 			} else {
 				// Server returned HTTP error code.
 				return "{}"; // TODO: create error codes
 			}
 
 		} catch (MalformedURLException e) {
 		} catch (IOException e) {
 		} finally {
 			if (connection != null)
 				connection.disconnect();
 		}
 		return "{}"; // TODO: create a connection error code
 	}
 
 	protected void onPostExecute(Void Result) {
 
 	}
 
 	// protected String getJSONFromServer(String modelURL) {
 	// HttpURLConnection connection = null;
 	// try {
 	// URL url = new URL(modelURL);
 	// connection = (HttpURLConnection) url.openConnection();
 	//
 	// Log.i("OC: JSON", "opened http connection");
 	//
 	// if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 	// Log.i("OC: Open Con", "preparing to get input stream");
 	// InputStream in = new BufferedInputStream(connection.getInputStream());
 	// return readStream(in);
 	// } else {
 	// // Server returned HTTP error code.
 	// return "{}"; //TODO: create error codes
 	// }
 	//
 	// } catch (MalformedURLException e) {
 	// } catch (IOException e) {
 	// } finally {
 	// if (connection != null)
 	// connection.disconnect();
 	// }
 	// return "{}"; //TODO: create a connection error code
 	// }
 
 	// --------------------------
 	// ----- Helper Methods -----
 	// --------------------------
 
 	private String readStream(InputStream is) {
 		try {
 			ByteArrayOutputStream bo = new ByteArrayOutputStream();
 			int i = is.read();
 			while (i != -1) {
 				bo.write(i);
 				i = is.read();
 			}
 			bo.close();
 			return bo.toString();
 		} catch (IOException e) {
 			return "";
 		}
 	}
 
 	private String getQuery(List<NameValuePair> params)
 			throws UnsupportedEncodingException {
 		StringBuilder result = new StringBuilder();
 		boolean first = true;
 
 		for (NameValuePair pair : params) {
 			if (first)
 				first = false;
 			else
 				result.append("&");
 
 			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
 			result.append("=");
 			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
 		}
 
 		return result.toString();
 	}
 
 }
