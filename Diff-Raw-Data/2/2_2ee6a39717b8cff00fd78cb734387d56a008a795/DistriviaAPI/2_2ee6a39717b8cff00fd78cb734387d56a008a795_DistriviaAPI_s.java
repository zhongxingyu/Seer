 package edu.rit.cs.distrivia.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 
 import android.util.Log;
 import edu.rit.cs.distrivia.model.GameData;
 import edu.rit.cs.distrivia.model.Question;
 
 /**
  * Main utility class which can talk to the Distrivia web service over
  * HTTP/HTTPS.
  */
 public class DistriviaAPI {
 
     private static HttpClient httpClient = createHttpClient();
     final private static String API_PROTOCOL = "https";
     final private static String API_ROOT = "distrivia.lame.ws";
     final private static String API_URL = API_PROTOCOL + "://" + API_ROOT;
     final private static int API_PORT = 443;
 
     /** Constant representing API error */
     final public static String API_ERROR = "err";
 
     /** Constant representing API success */
     final public static String API_SUCCESS = "suc";
 
     /**
      * API call to login to Distrivia with a specific user name.
      * 
      * @param userName
      *            The user name to attempt to login to distrivia with.
      * @param pass
      *            The user's password for authentication
      * 
      * @return The authorization users token on success, otherwise null on
      *         authentication failure.
      * 
      * @throws Exception
      */
     public static String login(final String userName, final String pass)
             throws Exception {
         String url = new String();
         url += "/login/" + userName;
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("password", pass));
 
         final String data = post(url, params);
 
         if (data == API_ERROR) {
             throw new DistriviaAPIException("Login Failed");
         } else {
             Log.d("DistriviaAPI", "The auth token: " + data);
             return data;
         }
     }
 
     /**
      * API Call to register a new user
      * 
      * @param username
      *            The user name to register with the service.
      * @param pass
      * @return True on register success, false on register failure.
      * @throws Exception
      */
     public static String register(final String username, final String pass)
             throws Exception {
 
         String url = new String();
         url += "/register/" + username;
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("password", pass));
 
         final String data = post(url, params);
         return data;
     }
 
     /**
      * @param gdata
      * @return The game id string
      * @throws Exception
      */
     public static GameData join(GameData gdata) throws Exception {
         String url = new String();
         url += "/public/join";
 
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("authToken", gdata.getAuthToken()));
         params.add(new BasicNameValuePair("user", gdata.getUserName()));
 
         String data = post(url, params);
         JSON jsonParser = new JSON(data);
 
         gdata.setGameId(jsonParser.gameid());
 
         return gdata;
     }
 
     /**
      * @param gdata
      * @return The modified GameData object
      * @throws Exception
      */
     public static GameData status(GameData gdata) throws Exception {
         String url = new String();
         url += "/game/" + gdata.getGameID();
 
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("authToken", gdata.getAuthToken()));
 
         String data = post(url, params);
         JSON jsonParser = new JSON(data);
 
         gdata.setGameId(jsonParser.gameid());
         gdata.setStatus(jsonParser.gamestatus());
 
         if ( gdata.hasStarted() ) {
             Question q = Question.create(jsonParser);
             gdata.setQuestion(q);
         }
        gdata.setLeaderboard(parser.localLeaderboard());
         return gdata;
     }
 
     /**
      * API call to get the next multiple choice question in a game.
      * 
      * @param gd
      * @param answer
      * @param time
      * 
      * @return The next question in the game, or null if the game is over.
      * @throws Exception
      */
     public static GameData answer(final GameData gd, final String answer,
             long time) throws Exception {
 
         String url = new String(API_URL);
         url += "/game/" + gd.getGameID() + "/question/" + gd.getCurrentQid();
 
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("authToken", gd.getAuthToken()));
         params.add(new BasicNameValuePair("user", gd.getUserName()));
         params.add(new BasicNameValuePair("time", String.valueOf(time)));
         params.add(new BasicNameValuePair("a", answer));
 
         String data = post(url, params);
 
         JSON parser = new JSON(data);
 
         Question q = Question.create(parser);
         gd.setQuestion(q);
         gd.setStatus(parser.gamestatus());
         gd.setScore(parser.score(gd.getUserName()));
         if (gd.isDone()) {
         	gd.setLeaderboard(parser.localLeaderboard());
         }
 
         return gd;
     }
 
     /**
      * API call to obtain the current leader board for a given game.
      * 
      * @param gdata
      * 
      * @return The game leader board at the time of the query, or null if the
      *         gameId does not exist, or the user is not properly logged in.
      * 
      * @throws Exception
      */
     public static String[][] leaderBoard(final GameData gdata) throws Exception {
 
         String url = new String();
         url += "/leaderboard/0";
 
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("authToken", gdata.getAuthToken()));
 
         String data = post(url, params);
 
         JSON parser = new JSON(data);
 
         String[][] lb = parser.leaderboard();
         return lb;
     }
 
     private static String post(String url, List<NameValuePair> params)
             throws Exception {
 
         HttpPost op = new HttpPost(url);
         if (params != null) {
             op.setEntity(new UrlEncodedFormEntity(params));
         }
         HttpResponse response = executeRequest(op);
         String data = responseToString(response);
         return data;
     }
 
     private static HttpClient createHttpClient() {
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("https", SSLSocketFactory
                 .getSocketFactory(), 443));
 
         HttpParams params = new BasicHttpParams();
 
         SingleClientConnManager mgr = new SingleClientConnManager(params,
                 schemeRegistry);
 
         return new DefaultHttpClient(mgr, params);
     }
 
     private static HttpResponse executeRequest(HttpRequestBase op)
             throws Exception {
         HttpHost host = new HttpHost(API_ROOT, API_PORT, API_PROTOCOL);
         HttpResponse response = null;
         try {
             response = httpClient.execute(host, op);
         } catch (ClientProtocolException e) {
             throw e;
         } catch (IOException e) {
             throw e;
         }
         return response;
     }
 
     /**
      * Convert a HttpResponse into a regular string for consumption.
      * 
      * @param res
      *            The response to parse.
      * @return
      * @throws Exception
      */
     private static String responseToString(HttpResponse res) throws Exception {
 
         InputStream is = null;
         try {
             is = res.getEntity().getContent();
         } catch (Exception e) {
             throw e;
         }
 
         if (is != null) {
 
             Writer writer = new StringWriter();
             char[] buffer = new char[1024];
 
             try {
                 Reader reader = new BufferedReader(new InputStreamReader(is,
                         "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } catch (UnsupportedEncodingException e) {
                 throw e;
             } catch (IOException e) {
                 throw e;
             } finally {
                 try {
                     is.close();
                 } catch (IOException e) {
                     // Nothing we can do now!
                 }
             }
             return writer.toString();
         } else {
             return "";
         }
     }
 
 }
