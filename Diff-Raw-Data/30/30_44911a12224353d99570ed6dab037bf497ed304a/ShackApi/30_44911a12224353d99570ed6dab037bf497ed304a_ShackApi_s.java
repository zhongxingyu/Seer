 package cc.hughes.droidchatty;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class ShackApi
 {
     static final String POST_URL = "http://www.shacknews.com/api/chat/create/17.json";
     
     static final String BASE_URL = "http://shackapi.stonedonkey.com/";
     static final String FAKE_STORY_ID = "17";
     static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
     
     public static ArrayList<SearchResult> search(String term, String author, String parentAuthor, int pageNumber) throws Exception
     {
         String url = BASE_URL + "Search/?json=true&SearchTerm=" + URLEncoder.encode(term, "UTF8") + "&Author=" + URLEncoder.encode(author, "UTF8") + "&ParentAuthor=" + URLEncoder.encode(parentAuthor, "UTF8") + "&Page=" + URLEncoder.encode(Integer.toString(pageNumber), "UTF-8");
         ArrayList<SearchResult> results = new ArrayList<SearchResult>();
         JSONObject result = getJson(url);
         
         JSONArray comments = result.getJSONArray("comments");
         for (int i = 0; i < comments.length(); i++)
         {
             JSONObject comment = comments.getJSONObject(i);
 
             int id = comment.getInt("id");
             String userName = comment.getString("author");
             String body = comment.getString("preview");
             String posted = comment.getString("date");
             
             results.add(new SearchResult(id, userName, body, posted));
         }
         
         return results;
     }
     
     public static int postReply(Context context, int replyToThreadId, String content) throws Exception
     {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
         String userName = prefs.getString("userName", null);
         String password = prefs.getString("password", null);
         
         List<NameValuePair> values = new ArrayList<NameValuePair>();
         values.add(new BasicNameValuePair("content_type_id", "17"));
         values.add(new BasicNameValuePair("content_id", FAKE_STORY_ID));
         values.add(new BasicNameValuePair("body", content));
         if (replyToThreadId > 0)
             values.add(new BasicNameValuePair("parent_id", Integer.toString(replyToThreadId)));
         
         JSONObject result = postJson(POST_URL, userName, password, values);
         
         if (!result.has("data"))
             throw new Exception("Missing response data.");
         
         JSONObject data = result.getJSONObject("data");
         return data.getInt("post_insert_id");
     }
     
     public static ArrayList<Thread> getThreads(int pageNumber) throws ClientProtocolException, IOException, JSONException
     {
         JSONObject json = getJson(BASE_URL + FAKE_STORY_ID + "." + Integer.toString(pageNumber) + ".json");
         return processThreads(json);
     }
     
     private static ArrayList<Thread> processThreads(JSONObject json) throws ClientProtocolException, IOException, JSONException
     {
         ArrayList<Thread> threads = new ArrayList<Thread>();
 
         // go through each of the comments and pull out the data that is used
         JSONArray comments = json.getJSONArray("comments");
         for (int i = 0; i < comments.length(); i++)
         {
             JSONObject comment = comments.getJSONObject(i);
 
             int id = comment.getInt("id");
             String userName = comment.getString("author");
             String body = comment.getString("body");
             String date = comment.getString("date");
             int replyCount = comment.getInt("reply_count");
 
             Thread thread = new Thread(id, userName, body, date, replyCount);
             threads.add(thread);
         }
 
         return threads;
     }
 
     public static ArrayList<Post> getPosts(int threadId) throws ClientProtocolException, IOException, JSONException
     {
         ArrayList<Post> posts = new ArrayList<Post>();
         HashSet<Integer> post_tracker = new HashSet<Integer>();
 
         JSONObject json = getJson(BASE_URL + "thread/" + threadId + ".json");
 
         // go through each of the comments and pull out the data that is used
         JSONArray comments = json.getJSONArray("comments");
         for (int i = 0; i < comments.length(); i++)
         {
             JSONObject comment = comments.getJSONObject(i);
 
             int postId = comment.getInt("id");
             String userName = comment.getString("author");
             String body = comment.getString("body");
             String date = comment.getString("date");
 
             Post post = new Post(postId, userName, body, date, 0);
             posts.add(post);
             post_tracker.add(postId);
 
             processPosts(comment, 1, posts, post_tracker);
         }
 
         return posts;
     }
 
     private static void processPosts(JSONObject comment, int level, ArrayList<Post> posts, HashSet<Integer> post_tracker) throws JSONException
     {
         JSONArray comments = comment.getJSONArray("comments");
 
         for (int i = 0; i < comments.length(); i++)
         {
             JSONObject p = comments.getJSONObject(i);
 
             int postId = p.getInt("id");
             String userName = p.getString("author");
             String body = p.getString("body");
             String date = p.getString("date");
 
             // only add this post if we haven't seen this post before
             // fixes duplicate posts coming back from the API
             // the duplicate post could end up in the wrong place though maybe?
             if (post_tracker.add(postId))
             {
                 Post post = new Post(postId, userName, body, date, level);
                 posts.add(post);
             }
             else
             {
                 Log.w("ShackAPI", "Skipped duplicate post #" + postId);
             }
 
             processPosts(p, level + 1, posts, post_tracker);
         }
     }
 
     private static JSONObject getJson(String url) throws ClientProtocolException, IOException, JSONException
     {
         Log.d("DroidChatty", "Requested: " + url);
         
         BasicResponseHandler response_handler = new BasicResponseHandler();
         DefaultHttpClient client = new DefaultHttpClient();
         HttpGet get = new HttpGet(url);
 
         String content = client.execute(get, response_handler);
         Log.d("DroidChatty", "Response: " + content);
         
         return new JSONObject(content);
     }
     
     private static JSONObject postJson(String url, String userName, String password, List<NameValuePair> values) throws Exception
     {
         UrlEncodedFormEntity e = new UrlEncodedFormEntity(values);
         
         URL post_url = new URL(url);
         URLConnection con = post_url.openConnection();
         con.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes((userName + ":" + password).getBytes()));
         
         // write our request
         con.setDoOutput(true);
         java.io.OutputStream os = con.getOutputStream();
         try
         {
             e.writeTo(os);
             os.flush();
         }
         finally
         {
             os.close();
         }
         
         // read the response
         java.io.InputStream input = con.getInputStream();
         try
         {
             String content = readStream(input);
             Log.d("DroidChatty", "response=" + content);
             return new JSONObject(content);
         }
         finally
         {
             input.close();
         }
     }
     
     private static String readStream(java.io.InputStream stream) throws IOException
     {
         StringBuilder output = new StringBuilder();
         InputStreamReader input = new InputStreamReader(stream);
 
         char[] buffer = new char[DEFAULT_BUFFER_SIZE];
         int count = 0;
         int n = 0;
         while (-1 != (n = input.read(buffer))) {
             output.append(buffer, 0, n);
             count += n;
         }
         return output.toString();
     }
 }
