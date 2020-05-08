 /**
 See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  This code is licensed
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */	
 package edu.rit.csh.androidwebnews;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * The object that the app interfaces with to get all the information 
  * to and from webnews 
  * JD's baby
  */
 public class HttpsConnector {
 	SharedPreferences sharedPref;
 	String mainUrl = "https://webnews.csh.rit.edu";
 	String apiKey;
 	WebnewsHttpClient httpclient;
 	int idCounter = 1;
 	int numChildren;
 	Activity activity;
 	NoInternetDialog dialog;
 	Context context;
 
 	public HttpsConnector(Activity activity) {
 		this.activity = activity;
 		context = activity.getApplicationContext();
 		dialog = new NoInternetDialog(activity);
 		httpclient = new WebnewsHttpClient(activity.getApplicationContext());
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);	
 	}
 	
 	public HttpsConnector(Context context) {
 		httpclient = new WebnewsHttpClient(context);
 		this.context = context;
 		dialog = new NoInternetDialog(context);
 	    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 	}
 
 	/**
 	 * Gets a list of Newsgroup objects that represent the current newsgroup on webnews.
 	 * @return ArrayList<Newsgroup> - the current newsgroups located on webnews, null if
 	 * there was an error in the procedure.
 	 */
 	public void getNewsGroups() {
 		new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("newsgroups", new ArrayList<NameValuePair>()));
 	}
 	
 	/**
 	 * Gets the newsgroups from a string representation of a JSON Object. This is used
 	 * to parse out the output of the async task for getting the newsgroups.
 	 * @param jsonString - the string representation of a JSON object
 	 * @return ArrayList<Newsgroup> - the list of newsgroups
 	 */
 	public ArrayList<Newsgroup> getNewsGroupFromString(String jsonString)  {
 		ArrayList<Newsgroup> newsgroups = new ArrayList<Newsgroup>();
 		JSONObject jObj;
 		try {
 			jObj = new JSONObject(jsonString);
 			JSONArray jArray = new JSONArray(jObj.getString("newsgroups"));
 			for (int i = 0 ; i < jArray.length() ; i++) {
 				newsgroups.add(new Newsgroup(new JSONObject(jArray.getString(i)).getString("name"),
 						new JSONObject(jArray.getString(i)).getInt("unread_count"),
 						new JSONObject(jArray.getString(i)).getString("unread_class")));
 			}
 		} catch (JSONException e) {
 			
 			Log.d("jsonError", "JSONException");
 		}
 		return newsgroups;		
 	}
 	
 	/**
 	 * Gets the newest threads on webnews to display on the front page
 	 * @return ArrayList<Thread> - list of the 20 newest or sticky threads
 	 */
 	public void getNewest(boolean bol) {
 		if (checkInternet()) {
 			new HttpsGetAsyncTask(httpclient, bol, activity).execute(formatUrl("activity", new ArrayList<NameValuePair>()));
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Takes the string from the async task and makes a list of PostThreads
 	 * @param s - the string from the async task
 	 * @return ArrayList<PostThread> - list of recent threads
 	 */
 	public ArrayList<PostThread> getNewestFromString(String s) {
 		ArrayList<PostThread> threads = new ArrayList<PostThread>();
 		
 		try {
 			JSONObject jObj = new JSONObject(s);
 			JSONArray jArray = jObj.getJSONArray("activity");
 
 			for (int i = 0 ; i < jArray.length() ; i++) {
 				
 				JSONObject newObj = jArray.getJSONObject(i).getJSONObject("newest_post");
 				String count = jArray.getJSONObject(i).getString("unread_count");
 				if (jArray.getJSONObject(i).getInt("unread_count") == 0) {
 					count = "null";
 				}
 				//String count = jArray.getJSONObject(i).getString("unread_class");
 				threads.add(new PostThread(newObj.getString("date"), 
 						newObj.getInt("number"), 
 						newObj.getString("subject"),
 						newObj.getString("author_name"),
 						newObj.getString("author_email"),
 						newObj.getString("newsgroup"),
 						false,
 						count,
 						jArray.getJSONObject(i).getString("personal_class"),
 						newObj.getString("sticky_until")));
 			}
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		}
 		return threads;
 	}
 	
 	
 	/**
 	 * Gets the threads for a certain newsgroup. All the threads have an ArrayList
 	 * of their sub-threads.
 	 * @param name - the name of the newsgroup
 	 * @param amount - the amount of threads to return, has to be <= 20, -1 == default of 10 
 	 * @return ArrayList<Thread> - list of the top level threads for the newsgroup
 	 */
 	public void getNewsgroupThreads(String name, int amount) {
 		if (checkInternet()) {
 			if (amount == -1) {
 				amount = 10;
 			} else if (amount > 20) {
 				amount = 20;
 			}
 			List<NameValuePair> params = new LinkedList<NameValuePair>();
 			params.add(new BasicNameValuePair("limit", Integer.valueOf(amount).toString()));
 			params.add(new BasicNameValuePair("thread_mode", "normal"));
 			//String url = formatUrl(mainUrl + "/" + name + "/index", params);
 			new HttpsGetAsyncTask(httpclient, true, activity).execute(formatUrl(name + "/index", params));
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Gets the threads for a certain newsgroup. All the threads have an ArrayList
 	 * of their sub-threads.
 	 * @param name - the name of the newsgroup
 	 * @param amount - the amount of threads to return, has to be <= 20, -1 == default of 10 
 	 * @param bol - boolean to decide to display the progress wheel or not
 	 * @return ArrayList<Thread> - list of the top level threads for the newsgroup
 	 */
 	public void getNewsgroupThreads(String name, int amount, boolean bol) {
 		if (checkInternet()) {
 			if (amount == -1) {
 				amount = 10;
 			} else if (amount > 20) {
 				amount = 20;
 			}
 			List<NameValuePair> params = new LinkedList<NameValuePair>();
 			params.add(new BasicNameValuePair("limit", Integer.valueOf(amount).toString()));
 			params.add(new BasicNameValuePair("thread_mode", "normal"));
 			//String url = formatUrl(mainUrl + "/" + name + "/index", params);
 			new HttpsGetAsyncTask(httpclient, bol, activity).execute(formatUrl(name + "/index", params));
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Gets the threads for a certain newsgroup past a certain date. This is done
 	 * to work around the 20 max thread return of the webnews api. All of the threads
 	 * contain an ArrayList of their sub-threads
 	 * @param newsgroup - the newsgroup name
 	 * @param date - the date to get post older than
 	 * @param amount - the amount of threads to return
 	 * @return ArrayList<Thread> - a list of the threads in the newsgroup from the starting
 	 * date
 	 */
 	public void getNewsgroupThreadsByDate(String newsgroup, String date, int amount) {
 		ArrayList<PostThread> threads = new ArrayList<PostThread>();
 		if (amount == -1) {
 			amount = 10;
 		} else if (amount > 20) {
 			amount = 20;
 		}
 		List<NameValuePair> params = new LinkedList<NameValuePair>();
 		params.add(new BasicNameValuePair("limit", new Integer(amount).toString()));
 		params.add(new BasicNameValuePair("thread_mode", "normal"));
 		params.add(new BasicNameValuePair("from_older", date));
 	//	String url = formatUrl(mainUrl + "/" + newsgroup + "/index", params);
 		new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl(newsgroup + "/index", params));
 			
 	}
 	
 	/**
 	 * Pulls out the threads from the given string representation of the 
 	 * json object
 	 * @param s - the string representation of the json object
 	 * @return ArrayList<PostThread> - the list of threads
 	 */
 	public ArrayList<PostThread> getThreadsFromString(String s) {
 		ArrayList<PostThread> threads = new ArrayList<PostThread>();
 		
 		try {
 			JSONObject  jObj = new JSONObject(s);
 			JSONArray jArray = new JSONArray(jObj.getString("posts_older"));
 			for (int i = 0 ; i < jArray.length() ; i++) {
 				threads.add(createThread(new JSONObject(jArray.getString(i)), 0));
 			}
 			return threads;
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		} 
 		return new ArrayList<PostThread>();
 	}
 	
 	/**
 	 * Starts an async task to get the results of a search query
 	 * @param params - ArrayList<NameValuePair> of he parameters for the search query
 	 */
 	public void search(ArrayList<NameValuePair> params) {
 		if (checkInternet()) {
 			new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("search", params));
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Deals with the search results. This is a varient of getThreadsFromString but is
 	 * different since search results does not return children or unread statuses. Unread
 	 * statuses are set to read (null)
 	 * @param s
 	 * @return
 	 */
 	public ArrayList<PostThread> getSearchFromString(String s) {
 		ArrayList<PostThread> threads = new ArrayList<PostThread>();
 		
 		try {
 			JSONObject jObj = new JSONObject(s);
 			JSONArray jArray = jObj.getJSONArray("posts_older");
 
 			for (int i = 0 ; i < jArray.length() ; i++) {
 				JSONObject newObj = jArray.getJSONObject(i).getJSONObject("post");
 				threads.add(new PostThread(newObj.getString("date"), 
 						newObj.getInt("number"), 
 						newObj.getString("subject"),
 						newObj.getString("author_name"),
 						newObj.getString("author_email"),
 						newObj.getString("newsgroup"),
 						false,
 						"",
 						"",
 						newObj.getString("sticky_until")));
 			}
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		}
 		return threads;
 	}
 	
 	/**
 	 * Gets the post body for the post specified. This takes a newsgroup name and a
 	 * post ID number to find the post's body
 	 * @param newsgroup - the newsgroup name
 	 * @param id - the number of the post
 	 * @return String - the body of the post
 	 */
 	public void getPostBody(String newsgroup, int id) {
 		if (checkInternet()) {
 			new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl(newsgroup + "/" + id, new ArrayList<NameValuePair>()));
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Pulls out the post's body from the given string representation of the 
 	 * json object
 	 * @param jsonObj - the string representation of the json object
 	 * @return the post's body
 	 */
 	public String getPostBodyFromString(String jsonObj) {
 		try {	
 			JSONObject jObj = new JSONObject(jsonObj);
 			JSONObject jsonPost = jObj.getJSONObject("post");
 			String body = jsonPost.getString("body");
 			
 			return body;			
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		} 
 		return "";
 	}
 		
 	/**
 	 * Gets the statuses about unread threads
 	 * @return int[] - array of the statuses
 	 * 			[0] - number of unread threads
 	 * 			[1] - number of unread threads in a thread the user has posted in
 	 * 			[2] - the number of unread replies to a user's post
 	 */
 	public int[] getUnreadCount() {
 		URI url = formatUrl("unread_counts", new LinkedList<NameValuePair>());
 		int[] unreadStatuses = new int[3];
 		unreadStatuses[0] = -1;
 		unreadStatuses[0] = -1;
 		if (checkInternet()) {
 
 			try {
 				JSONObject  jObj = new JSONObject(new HttpsGetAsyncTask(httpclient, false, activity).execute(url).get()).getJSONObject("unread_counts");
 				Log.d("jddebug", jObj.toString());
 				unreadStatuses[0] = jObj.getInt("normal");
 				unreadStatuses[1] = jObj.getInt("in_thread");
 				unreadStatuses[2] = jObj.getInt("in_reply");
 			} catch (JSONException e) {
 				Log.d("jsonError", "JSONException");
 				return null;
 			} catch (InterruptedException e) {
 				Log.d("jsonError", "InterruptedException");
 			} catch (ExecutionException e) {
 				Log.d("jsonError", "ExecutionException");
 			}
 	
 		}
 		return unreadStatuses;
 		
 	}
 	
 	public void startUnreadCountTask() {
 		new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("unread_counts", new LinkedList<NameValuePair>()));
 		
 	}
 	public int[] getUnreadCountFromString(String jsonString) {
 		int[] unreadStatuses = new int[3];
 		try {
 			JSONObject  jObj = new JSONObject(jsonString).getJSONObject("unread_counts");
 			Log.d("jddebug", jObj.toString());
 			unreadStatuses[0] = jObj.getInt("normal");
 			unreadStatuses[1] = jObj.getInt("in_thread");
 			unreadStatuses[2] = jObj.getInt("in_reply");
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		}
 		return unreadStatuses;
 	}
 	
 	/**
 	 * Marks all post read
 	 */
 	public void markRead() {
 		if (checkInternet()) {
 			String url = formatUrl("mark_read", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			BasicNameValuePair allVP = new BasicNameValuePair("all_posts", "");
 			
 			Log.d("jsonurl", url);
 			try {
 				new HttpsPutAsyncTask(httpclient).execute(urlVP, allVP).get();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Marks the given thread as read
 	 * @param newsgroup - the name of the newsgroup 
 	 * @param id
 	 */
 	public void markRead(String newsgroup, int id) {
 		if (checkInternet()) {
 			String url = formatUrl("mark_read", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
 			BasicNameValuePair numberVP = new BasicNameValuePair("number", Integer.valueOf(id).toString());
 			
 			Log.d("jsonurl", url);
 			new HttpsPutAsyncTask(httpclient).execute(urlVP, newsgroupVP, numberVP);
 		} else {
 			dialog.show();
 		}
 	}
 	
 	public void markRead(String newsgroup) {
 		if (checkInternet()) {
 			String url = formatUrl("mark_read", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);			
 			Log.d("newdebug", "mark post read: " + newsgroup);
 			try {
 				new HttpsPutAsyncTask(httpclient).execute(urlVP, newsgroupVP).get();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Marks the given post as unread
 	 * @param newsgroup - the newsgroup name that the post is in
 	 * @param id - the id of the post
 	 */
 	public void markUnread(String newsgroup, int id) {
 		if (checkInternet()) {
 			String url = formatUrl("mark_read", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
 			BasicNameValuePair numberVP = new BasicNameValuePair("number", Integer.valueOf(id).toString());
 			BasicNameValuePair markUnreadVP = new BasicNameValuePair("mark_unread", "");
 			
 			new HttpsPutAsyncTask(httpclient).execute(urlVP, newsgroupVP, numberVP, markUnreadVP);
 		} else {
 			dialog.show();
 		}
 	}
 	
 	/**
 	 * Marks a given post as starred
 	 * @param newsgroup - the name of the newsgroup
 	 * @param id - the id of the post in the given newsgroup
 	 */
 	public void markStarred(String newsgroup, int id) {
 		if (checkInternet()) {
 			String url = formatUrl(newsgroup + "/" + id + "/star", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			new HttpsPutAsyncTask(httpclient).execute(urlVP);
 		} else {
 			dialog.show();
 		}
 	}
 	
 	public void composePost(String newsgroup, String subject, String body) {
 		if (checkInternet()) {
 			String url = formatUrl("compose", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
 			BasicNameValuePair subjectVP = new BasicNameValuePair("subject", subject);
 			BasicNameValuePair bodyVP = new BasicNameValuePair("body", body);
 			BasicNameValuePair stickyVP = new BasicNameValuePair("sticky_until" , "1 second from now");
 			
 			new HttpsPostAsyncTask(httpclient).execute(urlVP, newsgroupVP, subjectVP, bodyVP, stickyVP);
 		} else {
 			dialog.show();
 		}
 		
 	}
 	
 	public void composePost(String newsgroup, String subject, String body, String newsgroupParent, int parentId) {
 		if (checkInternet()) {
 			String url = formatUrl("compose", new ArrayList<NameValuePair>()).toString();
 			BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 			BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
 			BasicNameValuePair subjectVP = new BasicNameValuePair("subject", subject);
 			BasicNameValuePair bodyVP = new BasicNameValuePair("body", body);
 			BasicNameValuePair newsgroupParentVP = new BasicNameValuePair("reply_newsgroup", newsgroupParent);
			BasicNameValuePair idParentVP = new BasicNameValuePair("reply_number", Integer.valueOf(parentId).toString());
 			BasicNameValuePair stickyVP = new BasicNameValuePair("sticky_until" , "1 second from now");
 			
 			new HttpsPostAsyncTask(httpclient).execute(urlVP, newsgroupVP, subjectVP, bodyVP, newsgroupParentVP, idParentVP, stickyVP);
 		} else {
 			dialog.show();
 		}
 		
 	}
 	
 
 	/**
 	 * Validates that the API key is valid
 	 * @return True if the api key is good, false otherwise
 	 */
 	public boolean validApiKey() {
 		//String url = formatUrl(mainUrl + "/user", new ArrayList<NameValuePair>());
 		try {
 			
 			JSONObject jObj = new JSONObject(new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("/user", new ArrayList<NameValuePair>())).get());
 			if (jObj.has("user")) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		} catch (InterruptedException e) {
 			Log.d("jsonError", "InterruptedException");
 		} catch (ExecutionException e) {
 			Log.d("jsonError", "ExecutionException");
 		}
 		return false;
 		
 	}
 	
 	/**
 	 * Formats the URL String with the API key and all the extra parameters for GET requests
 	 * @param url - the add on to the url to format
 	 * @param addOns - List<NameValuePair> of extra parameters, can be empty
 	 * @return String - the formated String
 	 */
 	private URI formatUrl(String addOn, List<NameValuePair> addOns) {
 		//if (!url.endsWith("?")) {
 		//	url += "?";
 		//}
 		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 		Log.d("jddebug - HttpsConnector", sharedPref.getString("api_key", ""));
 		params.add(new BasicNameValuePair("api_key", sharedPref.getString("api_key", "")));
 		params.add(new BasicNameValuePair("api_agent", "Android_Webnews"));
 		if (addOns.size() != 0) {
 			params.addAll(addOns);
 		}
 		
 		String paramString = URLEncodedUtils.format(params,  "utf-8");
 		//url += paramString;
 		try {
 			Log.d("jddebug - HttpsConnector", URIUtils.createURI("https", "webnews.csh.rit.edu", -1, "/" + addOn, 
 				    URLEncodedUtils.format(params, "UTF-8"), null).toString());
 			return URIUtils.createURI("https", "webnews.csh.rit.edu", -1, "/" + addOn, 
 				    URLEncodedUtils.format(params, "UTF-8"), null);
 		} catch (URISyntaxException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Creates the threads with all of their sub-threads
 	 * @param obj - the JSONObject of the top level thread to use
 	 * @return Thread - the newly created thread with all of its sub-threads
 	 */
 	private PostThread createThread(JSONObject obj, int depthLevel) {
 		JSONObject post;
 		try {
 			post = new JSONObject(obj.getString("post"));
 			PostThread thread = new PostThread(post.getString("date"), 
 					post.getInt("number"), 
 					post.getString("subject"),
 					post.getString("author_name"),
 					post.getString("author_email"), 
 					post.getString("newsgroup"),
 					post.getBoolean("starred"),
 					post.getString("unread_class"),
 					post.getString("personal_class"),
 					post.getString("sticky_until"));
 			thread.depth = depthLevel;
 			Log.d("thread", thread.authorName + ": " + thread.depth);
 			if (obj.getJSONArray("children") != null ) {
 				for (int i = 0 ; i < obj.getJSONArray("children").length() ; i++) {
 					PostThread child = createThread(obj.getJSONArray("children").getJSONObject(i), depthLevel + 1);
 					child.parent = thread;
 					thread.children.add(child);
 				}
 			}
 			return thread;		
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks to see if there is network connection
 	 * @return boolean - true if there is internet, false otherwise
 	 */
 	public boolean checkInternet() 
 	{
 	    ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    //android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 	    //android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 	    NetworkInfo ni = connec.getActiveNetworkInfo();
 	    // Here if condition check for wifi and mobile network is available or not.
 	    // If anyone of them is available or connected then it will return true, otherwise false;
 
 	    if (ni != null && ni.isConnected()) {
 	        return true;
 	    }  else {
 	    	return false;
 	    }
 	}
 }
