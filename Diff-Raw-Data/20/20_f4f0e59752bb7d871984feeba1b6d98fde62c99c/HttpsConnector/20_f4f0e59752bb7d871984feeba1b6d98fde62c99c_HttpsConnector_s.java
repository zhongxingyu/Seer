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
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 
 /**
  * The object that the app interfaces with to get all the information
  * to and from webNews
  *
  * @author JD
  */
 class HttpsConnector {
     private final SharedPreferences sharedPref;
     private Activity activity;
     private final Context context;
     private final String noInternet = "Error: No Internet";
 
     public HttpsConnector(Activity activity) {
         this.activity = activity;
         context = activity.getApplicationContext();
         sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
     }
 
     public HttpsConnector(Context context) {
         this.context = context;
         sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
     }
 
     /**
      * Gets a list of Newsgroup objects that represent the current newsgroup on webnews.
      */
     public void getNewsGroups() {
         if (checkInternet())
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), false, activity).execute(formatUrl("newsgroups", null));
         else
             ((BaseActivity) activity).shouldUpdate(noInternet);
     }
 
     /**
      * Gets the newsgroups from a string representation of a JSON Object. This is used
      * to parse out the output of the async task for getting the newsgroups.
      *
      * @param jsonString - the string representation of a JSON object
      * @return ArrayList<Newsgroup> - the list of newsgroups
      */
     public ArrayList<Newsgroup> getNewsGroupFromString(String jsonString) {
         ArrayList<Newsgroup> newsgroups = new ArrayList<Newsgroup>();
         JSONObject jObj;
         try {
             jObj = new JSONObject(jsonString);
             JSONArray jArray = new JSONArray(jObj.getString("newsgroups"));
             for (int i = 0; i < jArray.length(); i++) {
                 newsgroups.add(new Newsgroup(new JSONObject(jArray.getString(i)).getString("name"),
                         new JSONObject(jArray.getString(i)).getInt("unread_count"),
                         new JSONObject(jArray.getString(i)).getString("unread_class")));
             }
         } catch (JSONException ignored) {
         }
         return newsgroups;
     }
 
     /**
      * Gets the newest threads on webnews to display on the front page
      */
     public void getNewest(boolean bol) {
         if (checkInternet())
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), bol, activity).execute(formatUrl("activity", null));
 
 
     }
 
     /**
      * Takes the string from the async task and makes a list of PostThreads
      *
      * @param s - the string from the async task
      * @return ArrayList<PostThread> - list of recent threads
      */
     public ArrayList<PostThread> getNewestFromString(String s) {
         ArrayList<PostThread> threads = new ArrayList<PostThread>();
 
         try {
             JSONObject jObj = new JSONObject(s);
             JSONArray jArray = jObj.getJSONArray("activity");
 
             for (int i = 0; i < jArray.length(); i++) {
 
                 JSONObject newObj = jArray.getJSONObject(i).getJSONObject("newest_post");
                 String count = jArray.getJSONObject(i).getString("unread_count");
                 if (jArray.getJSONObject(i).getInt("unread_count") == 0) {
                     count = "null";
                 }
 
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
         } catch (JSONException ignored) {
         }
         return threads;
     }
 
 
     /**
      * Gets the threads for a certain newsgroup. All the threads have an ArrayList
      * of their sub-threads.
      *
      * @param name   - the name of the newsgroup
      * @param amount - the amount of threads to return, has to be <= 20, -1 == default of 10
      */
     public void getNewsgroupThreads(String name, int amount) {
         if (checkInternet()) {
             if (amount == -1) {
                 amount = 10;
             } else if (amount > 20) {
                 amount = 20;
             }
             HashMap<String, String> params = new HashMap<String, String>();
             params.put("limit", Integer.valueOf(amount).toString());
             params.put("thread_mode", "normal");
             //String url = formatUrl(mainUrl + "/" + name + "/index", params);
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), true, activity).execute(formatUrl(name + "/index", params));
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
 
         }
     }
 
     /**
      * Gets the threads for a certain newsgroup. All the threads have an ArrayList
      * of their sub-threads.
      *
      * @param name   - the name of the newsgroup
      * @param amount - the amount of threads to return, has to be <= 20, -1 == default of 10
      * @param bol    - boolean to decide to display the progress wheel or not
      */
     public void getNewsgroupThreads(String name, int amount, boolean bol) {
         if (checkInternet()) {
 
             if (amount == -1) {
                 amount = 10;
             } else if (amount > 20) {
                 amount = 20;
             }
             HashMap<String, String> params = new HashMap<String, String>();
             params.put("limit", Integer.valueOf(amount).toString());
             params.put("thread_mode", "normal");
             //String url = formatUrl(mainUrl + "/" + name + "/index", params);
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), bol, activity).execute(formatUrl(name + "/index", params));
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Gets the threads for a certain newsgroup past a certain date. This is done
      * to work around the 20 max thread return of the webnews API. All of the threads
      * contain an ArrayList of their sub-threads
      *
      * @param newsgroup - the newsgroup name
      * @param date      - the date to get post older than
      * @param amount    - the amount of threads to return
      */
     public void getNewsgroupThreadsByDate(String newsgroup, String date, int amount) {
         if (checkInternet()) {
             if (amount == -1) {
                 amount = 10;
             } else if (amount > 20) {
                 amount = 20;
             }
             HashMap<String, String> params = new HashMap<String, String>();
             params.put("limit", Integer.valueOf(amount).toString());
             params.put("thread_mode", "normal");
             params.put("from_older", date);
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), false, activity).execute(formatUrl(newsgroup + "/index", params));
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Pulls out the threads from the given string representation of the
      * json object
      *
      * @param s - the string representation of the json object
      * @return ArrayList<PostThread> - the list of threads
      */
     public ArrayList<PostThread> getThreadsFromString(String s) {
         ArrayList<PostThread> threads = new ArrayList<PostThread>();
 
         try {
             JSONObject jObj = new JSONObject(s);
             JSONArray jArray = new JSONArray(jObj.getString("posts_older"));
             for (int i = 0; i < jArray.length(); i++) {
                 threads.add(createThread(new JSONObject(jArray.getString(i)), 0));
             }
             return threads;
         } catch (JSONException ignored) {
         }
         return new ArrayList<PostThread>();
     }
 
     /**
      * Starts an async task to get the results of a search query
      *
      * @param params - ArrayList<NameValuePair> of he parameters for the search query
      */
     public void search(HashMap<String, String> params) {
         if (checkInternet()) {
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), true, activity).execute(formatUrl("search", params));
         } else {
             ((BaseActivity) activity).update(noInternet);
         }
     }
 
     /**
      * Deals with the search results. This is a varient of getThreadsFromString but is
      * different since search results does not return children or unread statuses. Unread
      * statuses are set to read (null)
      *
      * @param jsonString - the String version of the JSON object
      * @return the threads found in the search
      */
     public ArrayList<PostThread> getSearchFromString(String jsonString) {
         ArrayList<PostThread> threads = new ArrayList<PostThread>();
 
         try {
             JSONObject jObj = new JSONObject(jsonString);
             JSONArray jArray = jObj.getJSONArray("posts_older");
 
             for (int i = 0; i < jArray.length(); i++) {
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
         } catch (JSONException ignored) {
         }
         return threads;
     }
 
     /**
      * Gets the post body for the post specified. This takes a newsgroup name and a
      * post ID number to find the post's body
      *
      * @param newsgroup - the newsgroup name
      * @param id        - the number of the post
      */
     public void getPostBody(String newsgroup, int id) {
         if (checkInternet()) {
             HashMap<String, String> params = new HashMap<String, String>();
             params.put("mark_read", "");
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), false, activity).execute(formatUrl(newsgroup + "/" + id, params));
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Pulls out the post's body from the given string representation of the
      * json object
      *
      * @param jsonObj - the string representation of the json object
      * @return the post's body
      */
     public String getPostBodyFromString(String jsonObj) {
         try {
             JSONObject jObj = new JSONObject(jsonObj);
             JSONObject jsonPost = jObj.getJSONObject("post");
             return jsonPost.getString("body");
         } catch (JSONException ignored) {
         }
         return "";
     }
 
     /**
      * Gets the statuses about unread threads
      *
      * @return int[] - array of the statuses
      *         [0] - number of unread threads
      *         [1] - number of unread threads in a thread the user has posted in
      *         [2] - the number of unread replies to a user's post
      */
     public int[] getUnreadCount() throws InvalidKeyException, NoInternetException, InterruptedException, ExecutionException {
         URI url = formatUrl("unread_counts", null);
         int[] unreadStatuses = new int[3];
         unreadStatuses[0] = -1;
         JSONObject jObj;
         String output;
 
         try {
             output = new HttpsGetAsyncTask(new WebnewsHttpClient(context), false, activity).execute(url).get();
             if (output.equals(""))
                 throw new NoInternetException();
             jObj = new JSONObject(output);
             if (jObj.has("error")) {
                 throw new InvalidKeyException();
             } else {
                 jObj = jObj.getJSONObject("unread_counts");
                 unreadStatuses[0] = jObj.getInt("normal");
                 unreadStatuses[1] = jObj.getInt("in_thread");
                 unreadStatuses[2] = jObj.getInt("in_reply");
             }
         } catch (JSONException ignored) {
             throw new NoInternetException();
         }
         return unreadStatuses;
     }
 
     public void startUnreadCountTask() {
         if (checkInternet())
             new HttpsGetAsyncTask(new WebnewsHttpClient(context), false, activity).execute(formatUrl("unread_counts", null));
         else
             ((BaseActivity) activity).shouldUpdate(noInternet);
 
     }
 
     public int[] getUnreadCountFromString(String jsonString) {
         int[] unreadStatuses = new int[3];
         try {
             JSONObject jObj = new JSONObject(jsonString).getJSONObject("unread_counts");
             unreadStatuses[0] = jObj.getInt("normal");
             unreadStatuses[1] = jObj.getInt("in_thread");
             unreadStatuses[2] = jObj.getInt("in_reply");
         } catch (JSONException ignored) {
         }
         return unreadStatuses;
     }
 
     /**
      * Marks all post read
      */
     public void markRead() {
         if (checkInternet()) {
             String url = formatUrl("mark_read", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             BasicNameValuePair allVP = new BasicNameValuePair("all_posts", "");
 
             try {
                 new HttpsPutAsyncTask(new WebnewsHttpClient(context)).execute(urlVP, allVP).get();
             } catch (InterruptedException ignored) {
             } catch (ExecutionException ignored) {
             }
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Marks the given thread as read
      *
      * @param newsgroup - the name of the newsgroup
      * @param id        - the id of the post
      */
     public void markRead(String newsgroup, int id) {
         if (checkInternet()) {
             String url = formatUrl("mark_read", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
             BasicNameValuePair numberVP = new BasicNameValuePair("number", Integer.valueOf(id).toString());
             new HttpsPutAsyncTask(new WebnewsHttpClient(context)).execute(urlVP, newsgroupVP, numberVP);
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Marks a newsgroup as all read
      *
      * @param newsgroup - the newsgroup to be marked read
      */
     public void markRead(String newsgroup) {
         if (checkInternet()) {
             String url = formatUrl("mark_read", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
             try {
                 new HttpsPutAsyncTask(new WebnewsHttpClient(context)).execute(urlVP, newsgroupVP).get();
             } catch (InterruptedException ignored) {
             } catch (ExecutionException ignored) {
             }
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Marks the given post as unread
      *
      * @param newsgroup - the newsgroup name that the post is in
      * @param id        - the id of the post
      */
     public void markUnread(String newsgroup, int id) {
         if (checkInternet()) {
             String url = formatUrl("mark_read", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
             BasicNameValuePair numberVP = new BasicNameValuePair("number", Integer.valueOf(id).toString());
             BasicNameValuePair markUnreadVP = new BasicNameValuePair("mark_unread", "");
 
             new HttpsPutAsyncTask(new WebnewsHttpClient(context)).execute(urlVP, newsgroupVP, numberVP, markUnreadVP);
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Marks a given post as starred
      *
      * @param newsgroup - the name of the newsgroup
      * @param id        - the id of the post in the given newsgroup
      */
     public void markStarred(String newsgroup, int id) {
         if (checkInternet()) {
             String url = formatUrl(newsgroup + "/" + id + "/star", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             new HttpsPutAsyncTask(new WebnewsHttpClient(context)).execute(urlVP);
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Composes a webnews_new post and uses an Async task to publish it
      *
      * @param newsgroup - the newsgroup to post to
      * @param subject   - the subject of the post
      * @param body      - the body of the post
      */
     public void composePost(String newsgroup, String subject, String body) {
         if (checkInternet()) {
             String url = formatUrl("compose", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
             BasicNameValuePair subjectVP = new BasicNameValuePair("subject", subject);
             BasicNameValuePair bodyVP = new BasicNameValuePair("body", body);
             BasicNameValuePair stickyVP = new BasicNameValuePair("unstick", "");
 
             new HttpsPostAsyncTask(new WebnewsHttpClient(context)).execute(urlVP, newsgroupVP, subjectVP, bodyVP, stickyVP);
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
     }
 
     /**
      * Composes a post as a response to another, to be sent using an Async task
      *
      * @param newsgroup       - the newsgroup to be posted to
      * @param subject         - the subject of the post
      * @param body            - the body of the message
      * @param newsgroupParent - the newsgroup of the parent
      * @param parentId        - the ID of the parent
      */
     public void composePost(String newsgroup, String subject, String body, String newsgroupParent, int parentId) {
         if (checkInternet()) {
             String url = formatUrl("compose", null).toString();
             BasicNameValuePair urlVP = new BasicNameValuePair("url", url);
             BasicNameValuePair newsgroupVP = new BasicNameValuePair("newsgroup", newsgroup);
             BasicNameValuePair subjectVP = new BasicNameValuePair("subject", subject);
             BasicNameValuePair bodyVP = new BasicNameValuePair("body", body);
             BasicNameValuePair newsgroupParentVP = new BasicNameValuePair("reply_newsgroup", newsgroupParent);
             BasicNameValuePair idParentVP = new BasicNameValuePair("reply_number", Integer.valueOf(parentId).toString());
             BasicNameValuePair stickyVP = new BasicNameValuePair("unstick", "");
             new HttpsPostAsyncTask(new WebnewsHttpClient(context)).execute(urlVP, newsgroupVP, subjectVP, bodyVP, newsgroupParentVP, idParentVP, stickyVP);
         } else {
             ((BaseActivity) activity).shouldUpdate(noInternet);
         }
 
     }
 
     /**
      * Formats the URL String with the API key and all the extra parameters for GET requests
      *
      * @param addOn  - the add on to the url to format
      * @param addOns - List<NameValuePair> of extra parameters, can be empty
      * @return String - the formated String
      */
     private URI formatUrl(String addOn, HashMap<String, String> addOns) {
         //if (!url.endsWith("?")) {
         //	url += "?";
         //}
         ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("api_key", sharedPref.getString("api_key", "")));
         params.add(new BasicNameValuePair("api_agent", "Android_Webnews"));
         if (addOns != null) {
             for (String key : addOns.keySet()) {
                 params.add(new BasicNameValuePair(key, addOns.get(key)));
             }
         }
         try {
             return URIUtils.createURI("https", "webnews.csh.rit.edu", -1, "/" + addOn,
                     URLEncodedUtils.format(params, "UTF-8"), null);
         } catch (URISyntaxException e) {
             return null;
         }
     }
 
     /**
      * Creates the threads with all of their sub-threads
      *
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
             thread.setDepth(depthLevel);
             if (obj.getJSONArray("children") != null) {
                 for (int i = 0; i < obj.getJSONArray("children").length(); i++) {
                     PostThread child = createThread(obj.getJSONArray("children").getJSONObject(i), depthLevel + 1);
                     child.setParent(thread);
                     thread.addChild(child);
                 }
             }
             return thread;
         } catch (JSONException ignored) {
         }
         return null;
     }
 
     /**
      * Checks to see if there is network connection
      *
      * @return boolean - true if there is internet, false otherwise
      */
     boolean checkInternet() {
         ConnectivityManager connect = (ConnectivityManager) activity.getApplicationContext()
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo ni = connect.getActiveNetworkInfo();
         return ni != null && ni.isConnected();
     }
 }
