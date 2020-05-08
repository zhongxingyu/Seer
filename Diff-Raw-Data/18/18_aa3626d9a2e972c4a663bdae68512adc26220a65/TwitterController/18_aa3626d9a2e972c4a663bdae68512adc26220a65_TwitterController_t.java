 package controller;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.ListModel;
 
 import model.Tweets;
 import model.TwitterModel;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import twitter4j.DirectMessage;
 import twitter4j.ResponseList;
 import twitter4j.Trends;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 /****************************************************
  *Tweets Class.
  *@author Nick, Vincenzo, Corey, Russ
  ***************************************************/
 public class TwitterController {
 
     /** Tweets timeline. */
     private Tweets timeline;
 
     /** boolean isHomeTimeline. */
     private boolean isHomeTimeline;
 
     /** TwitterModel. */
     private static TwitterModel model;
 
     /** Boolean if the controller is set up. */
     private boolean isSetUp = false;
 
     /** Twitter. */
     private Twitter twitter;
 
     /** RequestToken. */
     private RequestToken requestToken;
 
     /** Consumer key. */
     public static final String CONSUMER_KEY = "yNBLlBrsFHz89PyCfjrAw";
 
     /** Consumer key secret. */
     public static final String CONSUMER_KEY_SECRET =
             "8SIq5OXfeIKabtB3B2CBHJVIkrjQbSPloHoTmxtis4";
 
     /** Trending WOEIDs. */
     private int[] woeidArray = {1, 23424975, 12, 727232, 23424747, 2357024,
             23424748, 638242, 2367105, 23424768, 23424775, 2379574,
             2383660, 2388929, 20070458, 2391585, 23424819, 23424829,
             2424766, 23424848, 23424846, 23424853, 2428184, 23424856,
             15015372, 2436704, 44418, 766273, 2449323, 23424900, 2450022,
             3534, 2122265, 2295411, 23424909, 2458833, 2459115,
             23424916, 2466256, 615702, 23424934, 2475687, 455825, 23424936,
             2486340, 2487610, 2487889, 2490383, 23424948, 23424942,
             23424950, 2486982, 1105779, 2503863, 1118370, 4118, 9807,
             23424982, 2514815};
 
     /****************************************************
      * Twitter Controller constructor.
      ***************************************************/
     public TwitterController() {
         twitter = new TwitterFactory().getInstance();
         twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
 
         String token = getAccessToken();
         String tokenSecret = getAccessTokenSecret();
 
         if (token != null && tokenSecret != null) {
             twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret));
             isSetUp = true;
 
         } else {
             try {
                 requestToken = twitter.getOAuthRequestToken();
             } catch (TwitterException e) {
                 e.printStackTrace();
             }
         }
 
         model = new TwitterModel(twitter);
     }
 
     /****************************************************
      * Gets Display Name.
      * @return String
      ****************************************************/
     public final String getDisplayName() {
         return model.getName();
     }
 
     /****************************************************
      * Gets twitter name.
      * @return String
      ***************************************************/
     public final String getTwitterName() {
         return model.getScreenName();
     }
 
     /****************************************************
      * Gets twitter name.
      * @param l Long
      * @return String
      ***************************************************/
     public final String getTwitterName(final long l) {
         return model.getScreenName(l);
     }
 
     /****************************************************
      * Gets description.
      * @return String
      ***************************************************/
     public final String getDescription() {
         return model.getDescription();
     }
 
     /****************************************************
      * Gets website.
      * @return String
      ***************************************************/
     public final String getWebsite() {
         return model.getURL();
     }
 
     /****************************************************
      * Gets location.
      * @return String
      ***************************************************/
     public final String getLocation() {
         return model.getLocation();
     }
 
     /****************************************************
      * gets Profile image.
      * @return ImageIcon profile picture
      ***************************************************/
     public final ImageIcon getProfileImage() {
         return model.getProfileImage();
     }
 
     /****************************************************
      * gets Smaller Profile image.
      * @return ImageIcon profile picture
      ***************************************************/
     public final ImageIcon getSmallerProfileImage() {
         return model.getSmallerProfileImage();
     }
 
     /****************************************************
      * gets Smaller Profile image.
      * @param userId long
      * @return ImageIcon profile picture
      ***************************************************/
     public final ImageIcon getSmallerProfileImage(final long userId) {
         return model.getSmallerProfileImage(userId);
     }
 
     /****************************************************
      * Gets profile banner image.
      * @return image
      ***************************************************/
     public final Image getProfileBanner() {
         return model.getProfileBanner();
     }
 
     /****************************************************
      * Gets text color.
      * @return Color white
      ***************************************************/
     public final Color getTextColor() {
         return Color.WHITE;
     }
 
     /****************************************************
      * Gets background image.
      * @return Banner
      ***************************************************/
     public final Image getBackgroundImage() {
         Image img = new ImageIcon("src/background.jpeg").getImage();
         return img;
     }
 
     /****************************************************
      * Post a new Tweet.
      * @param str String
      * @return Boolean
      ***************************************************/
     public final boolean tweet(final String str) {
         return model.updateStatus(str);
     }
 
     /****************************************************
      * Post an image as a Tweet.
      * @param img File
      * @param message String
      * @return Boolean
      ***************************************************/
     public final boolean tweetImage(final File img, final String message) {
         return model.tweetImage(img, message);
     }
 
     /****************************************************
      * Gets tweet count.
      * @return int
      ***************************************************/
     public final int getTweetCount() {
         return model.getTweetCount();
     }
 
     /****************************************************
      * Gets friends IDs.
      * @return long[]
      ***************************************************/
     public final long[] getFriendsIDs() {
         return model.getFriendsIDs();
     }
 
     /****************************************************
      * Gets friend count.
      * @return int number of friends
      ***************************************************/
     public final int getFriendsCount() {
         return model.getFriendsCount();
     }
 
     /****************************************************
      * Gets followers.
      * @return ArrayList<User>
      ***************************************************/
     public final List<User> getFollowers() {
         return model.getFollowers();
     }
 
     /****************************************************
      * Gets following.
      * @return ArrayList<User>
      ***************************************************/
     public final List<User> getFollowing() {
         return model.getFollowing();
     }
 
     /****************************************************
      * Unfollows user.
      * @param l long
      * @return boolean
      ***************************************************/
     public final boolean unfollow(final long l) {
         return model.unfollow(l);
     }
 
     /****************************************************
      * ShowsUser.
      * @param l long
      * @return User
      ***************************************************/
     public final User showUser(final long l) {
         return model.showUser(l);
     }
 
     /****************************************************
      * Gets follower Count.
      * @return int number of followers
      ***************************************************/
     public final int getFollowersCount() {
         return model.getFollowersCount();
     }
 
     /****************************************************
      * destroys Status.
      * @param l long
      * @return destroyStatus l
      ***************************************************/
     public final boolean destroyStatus(final Long l) {
         return model.destroyStatus(l);
     }
 
     /****************************************************
      * Gets trending.
      * @param position int woeid
      * @return Trends
      ***************************************************/
     public final Trends getTrending(final int position) {
         return model.getTrending(woeidArray[position]);
     }
 
     /****************************************************
      * Gets home timeline.
      * @return tweets from timeline
      ***************************************************/
     public final Tweets getHomeTimeline() {
         return model.getHomeTimeline();
     }
 
     /****************************************************
      * gets users timeline.
      * @return Tweets
      ***************************************************/
     public final Tweets getUserTimeline() {
         return model.getUserTimeline();
     }
 
     /****************************************************
      * gets authorsURL.
      * @return String Users URL
      ***************************************************/
     public final String getAuthUrl() {
         return requestToken.getAuthorizationURL();
 
     }
 
     /****************************************************
      * Gets all of the user's messages.
      * @return String Users URL
      ***************************************************/
     public final List<DirectMessage> getAllMessages() {
         return model.getAllMessages();
     }
 
     /****************************************************
      * Gets Direct Messages to the user.
      * @param messageId long
      * @return String Users URL
      ***************************************************/
     public final DirectMessage showDirectMessage(final long messageId) {
         return model.showDirectMessage(messageId);
     }
 
     /****************************************************
      * Gets Direct Messages to the user.
      * @param userId long
      * @param text String
      * @return String Users URL
      ***************************************************/
     public final boolean sendDirectMessage(final long userId,
             final String text) {
         return model.sendDirectMessage(userId, text);
     }
 
     /***************************************************
      * Get the current user's ID.
      * @return get user ID
      **************************************************/
     public final long getCurrentUserID() {
         return model.getCurrentUserID();
     }
 
     /***************************************************
      * Get the a specified user.
      * @param id long
      * @return id
      **************************************************/
     public final User getUser(final long id) {
         return model.getUser(id);
     }
 
     /****************************************************
      * adds access token to file.
      * @param accessToken AccessToken
      ***************************************************/
     @SuppressWarnings("unchecked")
     private void addToFile(final AccessToken accessToken) {
         JSONObject obj = new JSONObject();
 
         obj.put("AccessToken", accessToken.getToken());
         obj.put("AccessTokenSecret", accessToken.getTokenSecret());
 
         try {
             FileWriter file = new FileWriter("./auth.json");
             file.write(obj.toJSONString());
             file.flush();
             file.close();
 
             isSetUp = true;
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     /****************************************************
      * Gets access token.
      * @return String access token
      ***************************************************/
     private String getAccessToken() {
         JSONParser parser = new JSONParser();
         try {
 
             Object obj = parser.parse(new FileReader("./auth.json"));
 
             JSONObject jsonObject = (JSONObject) obj;
 
             return (String) jsonObject.get("AccessToken");
 
         } catch (Throwable e) {
             return null;
         }
     }
 
     /****************************************************
      * Gets access token.
      * @return String access token
      ***************************************************/
     private String getAccessTokenSecret() {
         JSONParser parser = new JSONParser();
         try {
 
             Object obj = parser.parse(new FileReader("./auth.json"));
 
             JSONObject jsonObject = (JSONObject) obj;
 
             return (String) jsonObject.get("AccessTokenSecret");
 
         } catch (Throwable e) {
             return null;
         }
     }
 
     /****************************************************
      * Boolean if controller is setup.
      * @return Boolean is set up
      ***************************************************/
     public final boolean getIsSetUp() {
         return isSetUp;
     }
 
     /****************************************************
      * sets up controller.
      * @param pin String
      ***************************************************/
     public final void setUp(final String pin) {
         AccessToken accessToken = null;
         try {
             accessToken = twitter.getOAuthAccessToken(requestToken, pin);
 
         } catch (TwitterException te) {
             te.printStackTrace();
         }
         addToFile(accessToken);
 
     }
 
     /****************************************************
      * ResponseList<User> searchUsers(String text).
      * @param text String
      * @return text
      * @throws TwitterException twitterException
      ***************************************************/
     public final ResponseList<User> searchUsers(final String text)
             throws TwitterException {
         return model.searchUsers(text);
     }
 
     /****************************************************
      * Follow.
      * @param l long
      * @return model.follow
      ***************************************************/
     public final User follow(final long l) {
         return model.follow(l);
 
     }
 
     /****************************************************
      * ResponseList<User> searchUsers(String text).
      * @return timeline
      ***************************************************/
     public final ListModel<String> initTimeline() {
         timeline = new Tweets(twitter);
         timeline.homeTimeline();
         isHomeTimeline = true;
         return timeline;
     }
    
    /****************************************************
     * Favorite Status.
     * @param index
     ***************************************************/
    public void favoriteStatus (int index) {}
 
     /****************************************************
      * ResponseList<User> searchUsers(String text).
      ***************************************************/
     public final void homeTimeline() {
         if (!isHomeTimeline) {
             timeline.homeTimeline();
             isHomeTimeline = true;
         }
 
     }
 
     /****************************************************
      * ResponseList<User> searchUsers(String text).
      ***************************************************/
     public final void userTimeline() {
         if (isHomeTimeline) {
             timeline.userTimeline();
             isHomeTimeline = false;
         }
 
     }
 
 }
