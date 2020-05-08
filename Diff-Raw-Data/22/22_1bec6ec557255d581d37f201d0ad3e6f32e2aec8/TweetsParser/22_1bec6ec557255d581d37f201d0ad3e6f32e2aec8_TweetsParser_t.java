 package jbeer.dev.twittermonitor.parser.json;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import jbeer.dev.twittermonitor.data.domain.Tweet;
 import jbeer.dev.twittermonitor.data.domain.TweetMonitor;
 
 public class TweetsParser {
 
 	/*{"completed_in":0.021,
 	 * 	"max_id":302334521796349952,
 	 * 	"max_id_str":"302334521796349952",
 	 * 	"next_page":"?page=2&max_id=302334521796349952&q=Android&rpp=2&result_type=recent",
 	 * 	"page":1,
 	 * 	"query":"Android",
 	 * 	"refresh_url":"?since_id=302334521796349952&q=Android&result_type=recent",
 	 * 	"results":[{
 	 * 		"created_at":"Fri, 15 Feb 2013 08:32:21 +0000",
 	 * 		"from_user":"Android_ay",
 	 * 		"from_user_id":1181506544,
 	 * 		"from_user_id_str":"1181506544",
 	 * 		"from_user_name":"\u30a2\u30f3\u30c9\u30ed\u30a4\u30c9",
 	 * 		"geo":null,
 	 * 		"id":302334521796349952,
 	 * 		"id_str":"302334521796349952",
 	 * 		"iso_language_code":"ja",
 	 * 		"metadata":{
 	 * 			"result_type":"recent"},
 	 * 		"profile_image_url":"http:\/\/a0.twimg.com\/sticky\/default_profile_images\/default_profile_1_normal.png",
 	 * 		"profile_image_url_https":"https:\/\/si0.twimg.com\/sticky\/default_profile_images\/default_profile_1_normal.png",
 	 * 		"source":"&lt;a href=&quot;http:\/\/twitter.com\/download\/iphone&quot;&gt;Twitter for iPhone&lt;\/a&gt;",
 	 * 		"text":"\u3086\u3068\u300c\u30b0\u30fc\u30b0\u30eb\u3067\u30af\u30b0\u308b\u300d",
 	 * 		"to_user":null,
 	 * 		"to_user_id":0,
 	 * 		"to_user_id_str":"0",
 	 * 		"to_user_name":null}
 	 * 		,
 	 * 		{
 	 * 		"created_at":"Fri, 15 Feb 2013 08:32:21 +0000",
 	 * 		"from_user":"kazuchin1172",
 	 * 		"from_user_id":311204478,
 	 * 		"from_user_id_str":"311204478",
 	 * 		"from_user_name":"\u30ab\u30ba","geo":null,
 	 * 		"id":302334521074925568,
 	 * 		"id_str":"302334521074925568",
 	 * 		"iso_language_code":"ja",
 	 * 		"metadata":{
 	 * 			"result_type":"recent"},
 	 * 		"profile_image_url":"http:\/\/a0.twimg.com\/profile_images\/2155544890\/imageCAIL8MCS_normal.jpg",
 	 * 		"profile_image_url_https":"https:\/\/si0.twimg.com\/profile_images\/2155544890\/imageCAIL8MCS_normal.jpg",
 	 * 		"source":"&lt;a href=&quot;http:\/\/www.nicovideo.jp\/&quot;&gt;\u30cb\u30b3\u30cb\u30b3\u52d5\u753b&lt;\/a&gt;",
 	 * 		"text":"\u3010\u751f\u653e\u9001\u3011[Android]\u96d1\u8ac7\uff06\u8eca\u8f09 \u3092\u958b\u59cb\u3057\u307e\u3057\u305f\u3002 http:\/\/t.co\/28zNIwu7 #lv126591943",
 	 * 		"to_user":null,
 	 * 		"to_user_id":0,
 	 * 		"to_user_id_str":"0",
 	 * 		"to_user_name":null}],
 	 * 	"results_per_page":2,
 	 * 	"since_id":0,
 	 * 	"since_id_str":"0"}
 	
 	*/
 	
 	
 	public TweetMonitor parse(String webResult) throws JSONException {
 		
 		TweetMonitor tweetMonitor = new TweetMonitor();
 		JSONObject json = new JSONObject(webResult);
		String nextPage = null;
		if(json.has("next_page")){
			nextPage = json.getString("next_page");
		}
 		JSONArray tweetsJson = json.getJSONArray("results");
 		
 		List<Tweet> tweetList = new ArrayList<Tweet>();
 		for (int i = 0; i < tweetsJson.length(); i++) {  
 			 Tweet tweet = new Tweet();
 		     JSONObject tweetJson = tweetsJson.getJSONObject(i);
 		     tweet.setTweet(tweetJson.getString("text"));
 		     tweet.setAvatarURl(tweetJson.getString("profile_image_url"));
 		     tweet.setUserName(tweetJson.getString("from_user"));
 		     tweet.setTime(tweetJson.getString("created_at"));
 		     tweetList.add(tweet);
 		}
 		
 		tweetMonitor.setNextPage(nextPage);
 		tweetMonitor.setTweetList(tweetList);
 		return tweetMonitor;
 	}
 	
 	
 	
 	
 	
 	
 
 }
