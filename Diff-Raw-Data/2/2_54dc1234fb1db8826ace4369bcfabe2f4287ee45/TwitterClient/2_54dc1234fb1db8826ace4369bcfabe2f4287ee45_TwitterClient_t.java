 package com.d2fn.jester.plugin.twitter;
 
 import com.d2fn.jester.plugin.twitter.response.Tweet;
 import com.d2fn.jester.plugin.twitter.response.TwitterUser;
 import com.yammer.dropwizard.json.Json;
 import com.yammer.dropwizard.logging.Log;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 
 import java.io.InputStream;
 import java.util.List;
 
 /**
  * TwitterClient
  * @author Dietrich Featherston
  */
 public class TwitterClient {
 
     private static final String userBase = "http://api.twitter.com/1/users/lookup.json?include_entities=true&screen_name=";
    private static final String statusBase = "https://api.twitter.com/1/statuses/show/";
     private HttpClient httpClient;
     private Json json = new Json();
     
     public TwitterClient(HttpClient httpClient) {
         this.httpClient = httpClient;
     }
 
     public boolean isTweet(String s) {
         return s.startsWith(("https://twitter.com")) || s.startsWith("http://twitter.com");
     }
     
     public Tweet getTweet(String url) throws Exception {
         return getTweetById(getTweetId(url));
     }
     
     public String getTweetId(String url) {
         // decompose url to get tweet id
         String[] parts = url.split("/");
         return parts[parts.length-1];
     }
     
     public Tweet getTweetById(String id) throws Exception {
         String url = statusBase + id + ".json";
         HttpGet get = new HttpGet(url);
         InputStream in = null;
         try {
             HttpResponse httpResponse = httpClient.execute(get);
             in = httpResponse.getEntity().getContent();
             return json.readValue(in, Tweet.class);
         }
         finally {
             try { if(in != null) { in.close(); } } catch(Exception e2) { log.error(e2, "error closing input stream");}
         }
     }
 
     public TwitterUser lookupUser(String screenName) throws Exception {
         String url = userBase + screenName;
         HttpGet get = new HttpGet(url);
         InputStream in = null;
         try {
             HttpResponse httpResponse = httpClient.execute(get);
             in = httpResponse.getEntity().getContent();
             TwitterUser[] users = json.readValue(in, TwitterUser[].class);
             return users[0];
         }
         finally {
             try { if(in != null) { in.close(); } } catch(Exception e2) { log.error(e2, "error closing input stream");}
         }
     }
     
     private static final Log log = Log.forClass(TwitterClient.class);
 }
