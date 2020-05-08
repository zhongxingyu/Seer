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
 import android.util.Log;
 
 /**
  * The object that the app interfaces with to get all the information 
  * to and from webnews 
  * @author JD
  */
 public class HttpsConnector {
 	String mainUrl = "https://webnews.csh.rit.edu";
 	String apiKey;
 	WebnewsHttpClient httpclient;
 	int idCounter = 1;
 	int numChildren;
 	Activity activity;
 
 	public HttpsConnector(String apiKey, Activity activity) {
 		this.activity = activity;
 		httpclient = new WebnewsHttpClient(activity.getApplicationContext());
 		this.apiKey = apiKey;
 	}
 	public HttpsConnector(String apiKey, Context context) {
 		httpclient = new WebnewsHttpClient(context);
 		this.apiKey = apiKey;
 	}
 
 	/**
 	 * Gets a list of Newsgroup objects that represent the current newsgroup on webnews.
 	 * @return ArrayList<Newsgroup> - the current newsgroups located on webnews, null if
 	 * there was an error in the procedure.
 	 */
 	public ArrayList<Newsgroup> getNewsGroups() {
 		ArrayList<Newsgroup> newsgroups = new ArrayList<Newsgroup>();
 		//String url = formatUrl(mainUrl + "/newsgroups", new LinkedList<NameValuePair>());
 		//Log.d("jsonurl", url);
 		try {
 			JSONObject jObj = new JSONObject(new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("newsgroups", new ArrayList<NameValuePair>())).get());
 			JSONArray jArray = new JSONArray(jObj.getString("newsgroups"));
 			for (int i = 0 ; i < jArray.length() ; i++) {
 				newsgroups.add(new Newsgroup(new JSONObject(jArray.getString(i)).getString("name"),
 						new JSONObject(jArray.getString(i)).getInt("unread_count"),
 						new JSONObject(jArray.getString(i)).getString("unread_class")));
 			}
 			return newsgroups;				
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		} catch (InterruptedException e) {
 			Log.d("jsonError", "InterruptedException");
 		} catch (ExecutionException e) {
 			Log.d("jsonError", "ExecutionException");
 		}
 		return new ArrayList<Newsgroup>();
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
 		//String url = formatUrl(mainUrl + "/activity", new ArrayList<NameValuePair>());
 		new HttpsGetAsyncTask(httpclient, bol, activity).execute(formatUrl("activity", new ArrayList<NameValuePair>()));
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
 				Log.d("jddebug - recent", "" + jArray.getJSONObject(i).getInt("unread_count"));
 				threads.add(new PostThread(newObj.getString("date"), 
 						newObj.getInt("number"), 
 						newObj.getString("subject"),
 						newObj.getString("author_name"),
 						newObj.getString("author_email"),
 						newObj.getString("newsgroup"),
 						false,
 						count,
 						""));
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
 		ArrayList<PostThread> threads = new ArrayList<PostThread>();
 		if (amount == -1) {
 			amount = 10;
 		} else if (amount > 20) {
 			amount = 20;
 		}
 		List<NameValuePair> params = new LinkedList<NameValuePair>();
 		params.add(new BasicNameValuePair("limit", new Integer(amount).toString()));
 		params.add(new BasicNameValuePair("thread_mode", "normal"));
 		//String url = formatUrl(mainUrl + "/" + name + "/index", params);
 		new HttpsGetAsyncTask(httpclient, true, activity).execute(formatUrl(name + "/index", params));
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
 		new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("newsgroups", params));
 			
 	}
 	
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
 		//Log.d("newdebug", formatUrl(mainUrl + "/search", params));
 		new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("search", params));
 	}
 	
 	/**
 	 * Gets the post body for the post specified. This takes a newsgroup name and a
 	 * post ID number to find the post's body
 	 * @param newsgroup - the newsgroup name
 	 * @param id - the number of the post
 	 * @return String - the body of the post
 	 */
 	public void getPostBody(String newsgroup, int id) {
 		List<NameValuePair> params = new LinkedList<NameValuePair>();
 		//String url = formatUrl(mainUrl + "/" + newsgroup + "/" + id, params);
 		new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("newsgroups", new ArrayList<NameValuePair>()));
 	}
 	
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
 		//String url = formatUrl(mainUrl + "/unread_counts", new LinkedList<NameValuePair>());
 		int[] unreadStatuses = new int[3];
 		try {
 			JSONObject  jObj = new JSONObject(new HttpsGetAsyncTask(httpclient, false, activity).execute(formatUrl("newsgroups", new ArrayList<NameValuePair>())).get()).getJSONObject("unread_counts");
 			unreadStatuses[0] = jObj.getInt("normal");
 			unreadStatuses[1] = jObj.getInt("in_thread");
 			unreadStatuses[2] = jObj.getInt("in_reply");
 		} catch (JSONException e) {
 			Log.d("jsonError", "JSONException");
 		} catch (InterruptedException e) {
 			Log.d("jsonError", "InterruptedException");
 		} catch (ExecutionException e) {
 			Log.d("jsonError", "ExecutionException");
 		}
 		return unreadStatuses;
 	}
 	
 	/**
 	 * Marks all post read
 	 */
 	public void markRead() {
 		String url = "";//formatUrl(mainUrl + "/mark_read", new ArrayList<NameValuePair>());
 		BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 		BasicNameValuePair allVP = new BasicNameValuePair("all_posts", "");
 		
 		Log.d("jsonurl", url);
 		new HttpsPutAsyncTask(httpclient).execute(urlVP, allVP);
 	}
 	
 	/**
 	 * Marks the given thread as read
 	 * @param newsgroup - the name of the newsgroup 
 	 * @param id
 	 */
 	public void markRead(String newsgroup, int id) {
 		String url = "";//formatUrl(mainUrl + "/mark_read", new ArrayList<NameValuePair>());
 		BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 		BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
 		BasicNameValuePair numberVP = new BasicNameValuePair("number", Integer.valueOf(id).toString());
 		
 		Log.d("jsonurl", url);
 		new HttpsPutAsyncTask(httpclient).execute(urlVP, newsgroupVP, numberVP);
 	}
 	
 	public void markUnread(String newsgroup, int id) {
 		String url = "";//formatUrl(mainUrl + "/mark_read", new ArrayList<NameValuePair>());
 		BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
 		BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
 		BasicNameValuePair numberVP = new BasicNameValuePair("number", Integer.valueOf(id).toString());
 		BasicNameValuePair markUnreadVP = new BasicNameValuePair("mark_unread", "");
 		
 		new HttpsPutAsyncTask(httpclient).execute(urlVP, newsgroupVP, numberVP, markUnreadVP);
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
 		params.add(new BasicNameValuePair("api_key", apiKey));
 		params.add(new BasicNameValuePair("api_agent", "Android_Webnews"));
 		if (addOns.size() != 0) {
 			params.addAll(addOns);
 		}
 		
 		String paramString = URLEncodedUtils.format(params,  "utf-8");
 		//url += paramString;
 		try {
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
 					post.getString("personal_class"));
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
 	
 
 	
 
 
 }
