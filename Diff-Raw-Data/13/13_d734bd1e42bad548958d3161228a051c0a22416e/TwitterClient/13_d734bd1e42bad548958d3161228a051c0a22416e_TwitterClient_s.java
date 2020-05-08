 package worker;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import common.Biography;
 import common.Tweet;
 
 public class TwitterClient {
 	private String username;
 	private String password;
 	private String encodedAuth;
 	
 	public TwitterClient(String username, String password) {
 		this.username = username;
 		this.password = password;
 		String auth = this.username + ":" + this.password;
 		this.encodedAuth = (new sun.misc.BASE64Encoder()).encode(auth.getBytes());
 	}
 	
 	public int getFollowersIDs(int userId, int page, List<Integer> aggregator) {
 		int statusCode = 0;
 		HttpURLConnection conn = null;
 		try {
 			URL url = new URL("http://www.twitter.com/followers/ids.json?page=" + page + "&user_id=" + userId);
 			conn = (HttpURLConnection)url.openConnection();
 			conn.setRequestProperty("Authorization", "Basic " + this.encodedAuth);
 			conn.connect();
 			
 			statusCode = conn.getResponseCode();
 			if (statusCode == HttpURLConnection.HTTP_OK) {
 				JSONArray array = (JSONArray)JSONValue.parse(new InputStreamReader(conn.getInputStream()));
 				if (array != null) {
 					for (int i = 0; i < array.size(); i++) {
 						String intString = array.get(i).toString();
 						if (intString.length() < 10) {
 							Integer followerId = Integer.parseInt(array.get(i).toString());
 							aggregator.add(followerId);
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("Code " + statusCode + " for " + userId + ": " + e);
 			if (statusCode == 0) e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Unexpected exception with code " + statusCode + " for " + userId + " page " + page + ": " + e);
 			e.printStackTrace();
 			statusCode = 0;
 		} finally {
 			if (conn != null) {
 				try {
 					conn.disconnect();
 					conn.getInputStream().close();
 				} catch (Exception e) {}
 			}
 		}
 		
 		return statusCode;
 	}
 	
 	public int getTweets(int userId, int count, int page, List<Tweet> aggregator, Biography biography) {
 		int statusCode = 0;
 		HttpURLConnection conn = null;
 		try {
 			URL url = new URL("http://www.twitter.com/statuses/user_timeline.json" + "?user_id=" + userId + "&count=" + count + "&page=" + page);
 			conn = (HttpURLConnection)url.openConnection();
 			conn.setRequestProperty("Authorization", "Basic " + this.encodedAuth);
 			conn.connect();
 			
 			statusCode = conn.getResponseCode();
 			if (statusCode == HttpURLConnection.HTTP_OK) {
 				JSONArray array = (JSONArray)JSONValue.parse(new InputStreamReader(conn.getInputStream()));
 				if (array != null) {
 					for (int i = 0; i < array.size(); i++) {
 						parseBiography((JSONObject)((JSONObject)array.get(i)).get("user"), biography);
 						Tweet tweet = parseTweet((JSONObject)array.get(i));
 						tweet.userId = userId;
 						aggregator.add(tweet);
 					}
 				}
 				biography.crawledAt = new Date();
 			}			
 		} catch (IOException e) {
 			System.out.println("Code " + statusCode + " for " + userId + ": " + e);
 			if (statusCode == 0) e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Unexpected exception with code " + statusCode + " for " + userId + " page " + page + ": " + e);
 			e.printStackTrace();
 			statusCode = 0;
 		} finally {
 			if (conn != null) {
 				try {
 					conn.disconnect();
 					conn.getInputStream().close();
 				} catch (Exception e) {}
 			}
 		}
 		
 		return statusCode;
 	}
 	
 	public int getBiography(int userId, Biography biography) {
 		int statusCode = 0;
 		HttpURLConnection conn = null;
 		try {
 			URL url = new URL("http://www.twitter.com/users/show.json?user_id=" + userId);
 			conn = (HttpURLConnection)url.openConnection();
 			conn.setRequestProperty("Authorization", "Basic " + this.encodedAuth);
 			conn.connect();
 			
 			statusCode = conn.getResponseCode();
 			if (statusCode == HttpURLConnection.HTTP_OK) {
 				JSONObject json = (JSONObject)JSONValue.parse(new InputStreamReader(conn.getInputStream()));
 				if (json != null) parseBiography(json, biography);
 			}
 		} catch (IOException e) {
 			System.out.println("Code " + statusCode + " for " + userId + ": " + e);
 			if (statusCode == 0) e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Unexpected exception with code " + statusCode + " for " + userId + ": " + e);
 			e.printStackTrace();
 			statusCode = 0;
 		} finally {
 			if (conn != null) {
 				try {
 					conn.disconnect();
 					conn.getInputStream().close();
 				} catch (Exception e) {}
 			}
 		}
 		
 		return statusCode;
 	}
 	
 	private void parseBiography(JSONObject json, Biography biography) {
 		biography.createdAt = parseDate(getStringFromJson(json, "created_at"));
 		biography.description = getStringFromJson(json, "description");
 		biography.favoritesCount = getLongFromJson(json, "favourites_count");
 		biography.followersCount = getLongFromJson(json, "followers_count");
 		biography.friendCount = getLongFromJson(json, "friends_count");
 		biography.statusCount = getLongFromJson(json, "statuses_count");
 		biography.id = getLongFromJson(json, "id");
 		biography.location = getStringFromJson(json, "location");
 		biography.name = getStringFromJson(json, "name");
 		biography.notifications = getBooleanFromJson(json, "notifications");
 		biography.profileBackgroundColor = getStringFromJson(json, "profile_background_color");
 		biography.profileBackgroundImageUrl = getStringFromJson(json, "profile_background_image_url");
 		biography.profileBackgroundTile = getBooleanFromJson(json, "profile_background_tile");
 		biography.profileImageUrl = getStringFromJson(json, "profile_image_url");
 		biography.profileLinkColor = getStringFromJson(json, "profile_link_color");
 		biography.profileSidebarBorderColor = getStringFromJson(json, "profile_sidebar_border_color");
 		biography.profileSidebarFillColor = getStringFromJson(json, "profile_side_fill_color");
 		biography.profileTextColor = getStringFromJson(json, "profile_text_color");
 	}
 	
 	private Tweet parseTweet(JSONObject json) {
 		Tweet tweet = new Tweet();
 		tweet.createdAt = parseDate(getStringFromJson(json, "created_at"));
 		tweet.favorited = getBooleanFromJson(json, "favorited");
 		tweet.id = getLongFromJson(json, "id");
 		tweet.inReplyToScreenName = getStringFromJson(json, "in_reply_to_screen_name");
 		tweet.inReplyToStatusId = getLongFromJson(json, "in_reply_to_status_id");
 		tweet.inReplyToUserId = getLongFromJson(json, "in_reply_to_user_id");
 		tweet.source = getStringFromJson(json, "source");
 		tweet.text = getStringFromJson(json, "text");
 		tweet.truncated = getBooleanFromJson(json, "truncated");
 		return tweet;
 	}
 	
 	private Date parseDate(String dateString) {
 		if (dateString == null) return null;
 		return new Date(dateString);
 	}
 	
 	private static long getLongFromJson(JSONObject json, String key) {
 		Object object = json.get(key);
 		if (object == null) return 0;
 		return (Long)object;
 	}
 	
 	private static String getStringFromJson(JSONObject json, String key) {
 		String data = (String)json.get(key);
 		if (data == null) return "";
 		return data;
 	}
 	
 	private static Boolean getBooleanFromJson(JSONObject json, String key) {
 		Object object = json.get(key);
 		if (object == null) return false;
 		return (Boolean)object;
 	}	
 }
