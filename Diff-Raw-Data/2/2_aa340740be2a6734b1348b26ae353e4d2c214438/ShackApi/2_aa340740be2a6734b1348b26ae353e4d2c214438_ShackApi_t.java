 package cc.hughes.droidchatty;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.FileEntity;
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
     static final String USER_AGENT = "DroidChatty/0.8";
     
     static final String IMAGE_LOGIN_URL = "http://chattypics.com/users.php?act=login_go";
     static final String IMAGE_UPLOAD_URL = "http://chattypics.com/upload.php";
     
     static final String LOGIN_URL = "http://www.shacknews.com/login_laryn.x";
     static final String MOD_URL = "http://www.shacknews.com/mod_chatty.x";
     static final String POST_URL = "http://www.shacknews.com/api/chat/create/17.json";
     static final String LOL_URL = "http://www.lmnopc.com/greasemonkey/shacklol/report.php";
     
     static final String LOL_VERSION = "20090513";
     
     static final String BASE_URL = "http://shackapi.hughes.cc/";
     static final String FAKE_STORY_ID = "17";
     static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
     
     public static String modPost(String userName, String password, int rootPostId, int postId, String moderation) throws Exception
     {
         BasicResponseHandler response_handler = new BasicResponseHandler();
         DefaultHttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(LOGIN_URL);
         post.setHeader("User-Agent", USER_AGENT);
         
         List<NameValuePair> values = new ArrayList<NameValuePair>();
         values.add(new BasicNameValuePair("username", userName));
         values.add(new BasicNameValuePair("password", password));
         values.add(new BasicNameValuePair("type", "login"));
         
         UrlEncodedFormEntity e = new UrlEncodedFormEntity(values);
         post.setEntity(e);
         
         String content = client.execute(post, response_handler);
         if (content.contains("do_iframe_login"))
         {
             int mod_type_id = getModTypeId(moderation);
            String mod = MOD_URL + "?root=" + rootPostId + "&post_id=" + postId + "&mod_type_id=" + mod_type_id;
             Log.d("DroidChatty", "Modding: " + mod);
             HttpGet get = new HttpGet(mod);
             get.setHeader("User-Agent", USER_AGENT);
             
             content = client.execute(get, response_handler);
             
             Log.d("DroidChatty", content);
             
             Pattern p = Pattern.compile("alert\\(\\s*\\\"(.+?)\\\"");
             Matcher match = p.matcher(content);
                             
             if (match.find())
                 return match.group(1);
             
             return "Maybe it worked!";
         }
         
         return "Couldn't login";
     }
     
     static int getModTypeId(String moderation) throws Exception
     {
         if (moderation.equalsIgnoreCase("interesting"))
             return 1;
         else if (moderation.equalsIgnoreCase("nws"))
             return 2;
         else if (moderation.equalsIgnoreCase("stupid"))
             return 3;
         else if (moderation.equalsIgnoreCase("tangent"))
             return 4;
         else if (moderation.equalsIgnoreCase("ontopic"))
             return 5;
         else if (moderation.equalsIgnoreCase("nuked"))
             return 8;
         else if (moderation.equalsIgnoreCase("political"))
             return 9;
         
         throw new Exception("Invalid mod type: " + moderation);
     }
     
     public static String loginToUploadImage(String userName, String password) throws Exception
     {
         BasicResponseHandler response_handler = new BasicResponseHandler();
         DefaultHttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(IMAGE_LOGIN_URL);
         post.setHeader("User-Agent", USER_AGENT);
         
         List<NameValuePair> values = new ArrayList<NameValuePair>();
         values.add(new BasicNameValuePair("user_name", userName));
         values.add(new BasicNameValuePair("user_password", password));
         
         UrlEncodedFormEntity e = new UrlEncodedFormEntity(values);
         post.setEntity(e);
         
         String content = client.execute(post, response_handler);
         if (content.contains("successfully been logged in"))
         {
             List<Cookie> cookies = client.getCookieStore().getCookies();
             return cookies.get(0).getName() + "=" + cookies.get(0).getValue();
         }
         
         return null;
     }
     
     public static String uploadImage(String imageLocation, String cookie) throws Exception
     {
         File file = new File(imageLocation);
         String name = file.getName();
         FileEntity e = new FileEntity(file, "image");
         
         String BOUNDARY = "648f67b67d304b01f84ceb0e0c56c8b7";
         
         URL post_url = new URL(IMAGE_UPLOAD_URL);
         URLConnection con = post_url.openConnection();
         con.setRequestProperty("User-Agent", USER_AGENT);
         if (cookie != null)
             con.setRequestProperty("Cookie", cookie);
         con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
         
         // write our request
         con.setDoOutput(true);
         java.io.OutputStream os = con.getOutputStream();
         try
         {
             DataOutputStream dos = new DataOutputStream(os);
             dos.writeBytes("--" + BOUNDARY + "\r\n");
             dos.writeBytes("Content-Disposition: form-data; name=\"userfile[]\";filename=\"" + name + "\"\r\n\r\n");
             
             e.writeTo(os);
             
             dos.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
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
             return readStream(input);
         }
         finally
         {
             input.close();
         }
     }
     
     public static ArrayList<SearchResult> search(String term, String author, String parentAuthor, int pageNumber) throws Exception
     {
         String url = BASE_URL + "search.php?terms=" + URLEncoder.encode(term, "UTF8") + "&author=" + URLEncoder.encode(author, "UTF8") + "&parentAuthor=" + URLEncoder.encode(parentAuthor, "UTF8") + "&page=" + URLEncoder.encode(Integer.toString(pageNumber), "UTF-8");
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
             
             // convert time to local timezone
             posted = convertTime(posted);
             
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
     
     public static ArrayList<Thread> getThreads(int pageNumber, String userName) throws ClientProtocolException, IOException, JSONException
     {
         JSONObject json = getJson(BASE_URL + "page.php?page=" + Integer.toString(pageNumber) + "&user=" + URLEncoder.encode(userName, "UTF8"));
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
             String category = comment.getString("category");
             boolean replied = comment.getBoolean("replied");
 
             // convert time to local timezone
             date = convertTime(date);
             
             Thread thread = new Thread(id, userName, body, date, replyCount, category, replied);
             threads.add(thread);
         }
 
         return threads;
     }
 
     public static ArrayList<Post> getPosts(int threadId) throws ClientProtocolException, IOException, JSONException
     {
         ArrayList<Post> posts = new ArrayList<Post>();
         
         if (threadId == 0)
         {
             posts.add(new Post(0, "Error", "No post here dude.", "Error", 0, "")); 
             return posts;
         }
 
         try
         {
             //JSONObject json = getJson(BASE_URL + "thread/" + threadId + ".json");
             JSONObject json = getJson(BASE_URL + "thread.php?id=" + threadId);
             
             Hashtable<Integer, Post> post_map = new Hashtable<Integer, Post>();
             
             // go through each of the comments and pull out the data that is used
             JSONArray comments = json.getJSONArray("replies");
             for (int i = 0; i < comments.length(); i++)
             {
                 JSONObject comment = comments.getJSONObject(i);
     
                 int postId = comment.getInt("id");
                 String userName = comment.getString("author");
                 String body = comment.getString("body");
                 String date = comment.getString("date");
                 String category = comment.getString("category");
                 int depth = comment.getInt("depth");
                 
                 // convert time to local timezone
                 date = convertTime(date);
     
                 Post post = new Post(postId, userName, body, date, depth, category);
                 posts.add(post);
                 
                 post_map.put(postId, post);
             }
             
             Vector<Integer> ids = new Vector<Integer>(post_map.keySet());
             Collections.sort(ids, Collections.reverseOrder());
             
             // set the order on the first 10 posts, so they can be highlighted
             for (int i = 0; i < Math.min(posts.size(), 10); i++)
             {
                 Post post = post_map.get(ids.get(i));
                 post.setOrder(i);
             }
             
         }
         catch (Exception ex)
         {
             Log.e("DroidChatty", "Error getting posts.", ex);
         }
 
         return posts;
     }
     
     public static void tagPost(int postId, String tag, String userName) throws Exception
     {
         // construct the lol stuff
         String url = LOL_URL + "?who=" + URLEncoder.encode(userName, "UTF8") + "&what=" + postId + "&tag=" + URLEncoder.encode(tag, "UTF8") + "&version=" + URLEncoder.encode(LOL_VERSION, "UTF-8");
         
         // make things work
         String content = get(url);
         
         Log.d("DroidChatty", "LOL response: " + content);
         
         // see if it did work
         if (!content.startsWith("ok"))
             throw new Exception(content);
     }
     
     private static String get(String url) throws ClientProtocolException, IOException 
     {
         Log.d("DroidChatty", "Requested: " + url);
         
         BasicResponseHandler response_handler = new BasicResponseHandler();
         DefaultHttpClient client = new DefaultHttpClient();
         HttpGet get = new HttpGet(url);
         get.setHeader("User-Agent", USER_AGENT);
 
         return client.execute(get, response_handler);
     }
 
     private static JSONObject getJson(String url) throws ClientProtocolException, IOException, JSONException
     {
         String content = get(url);
         
         return new JSONObject(content);
     }
     
     private static JSONObject postJson(String url, String userName, String password, List<NameValuePair> values) throws Exception
     {
         UrlEncodedFormEntity e = new UrlEncodedFormEntity(values);
         
         URL post_url = new URL(url);
         URLConnection con = post_url.openConnection();
         con.setRequestProperty("User-Agent", USER_AGENT);
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
     
     static final SimpleDateFormat _shackDateFormat = new SimpleDateFormat("MMM dd, yyyy h:mma zzz");
     static final DateFormat _displayDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
     
     private static String convertTime(String original)
     {
         try
         {
             Date dt = _shackDateFormat.parse(original);
             return _displayDateFormat.format(dt);
         }
         catch (Exception ex)
         {
             Log.e("DroidChatty", "Error parsing date", ex);
         }
         
         return original;
     }
 
 }
