 package model;
 
 import java.awt.Image;
 import java.io.File;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.ImageIcon;
 
 import twitter4j.DirectMessage;
 import twitter4j.GeoLocation;
 import twitter4j.GeoQuery;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
 import twitter4j.Trends;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.User;
 
 /****************************************************
  * Twitter model.
  ***************************************************/
 public class TwitterModel {
 
     //private static final int CHICAGO = 2379574;
 
     private Twitter t;
 
     private String name;
     private String screenName;
     private String description;
     private String url;
     private String location;
     private ImageIcon profileImage;
     private ImageIcon smallerProfileImage;
 
     private Image profileBanner;
 
     private int tweetCount = 0;
 
     private long[] friendsIDs;
 
     private List<User> followers;
 
     private List<User> following;
 
     private int followersCount;
     private int friendsCount;
 
     private Trends trending;
 
     private Tweets homeTimeline;
 
     private Tweets userTimeline;
 
     private List<DirectMessage> messages;
 
     /****************************************************
      * Twitter model.
      ***************************************************/
 
     public TwitterModel(Twitter twitter) {
         t = twitter;
         refresh();
 
     }
 
     public void refresh() {
         List<Status> statuses;
         followers = new ArrayList<User>();
         following = new ArrayList<User>();
         homeTimeline = new Tweets(t);
         userTimeline = new Tweets(t);
         try {
             User u = t.showUser(t.getId());
             name = u.getName();
             screenName = "@" + u.getScreenName();
             description = u.getDescription();
             url = u.getURL();
             location = u.getLocation();
             tweetCount = u.getStatusesCount();
             friendsCount = u.getFriendsCount();
             followersCount = u.getFollowersCount();
             trending = t.getPlaceTrends(1);
 
             statuses = t.getHomeTimeline();
             if (statuses != null)
                 for (Status s : statuses)
                     homeTimeline.add(s);
             statuses = t.getUserTimeline();
             if (statuses != null)
                 for (Status s : statuses)
                     userTimeline.add(s);
 
             friendsIDs = t.getFriendsIDs(-1).getIDs();
 
             long[] list = t.getFollowersIDs(-1).getIDs();
             for (long l : list)
                 followers.add(t.showUser(l));
 
             list = t.getFriendsIDs(-1).getIDs();
             for (long l : list)
                 following.add(t.showUser(l));
 
             messages = t.getDirectMessages();
 
             refreshImage(u);
 
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (TwitterException e) {
             e.printStackTrace();
         }
 
     }
 
     private void refreshImage(User u) {
         try {
             profileImage = new ImageIcon(new URL(u.getBiggerProfileImageURL()));
             smallerProfileImage = new ImageIcon(new URL(u.getProfileImageURL()));
             profileBanner = (new ImageIcon(new URL(u.getProfileBannerURL()))
                     .getImage());
         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             // e.printStackTrace();
         } catch (IllegalStateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         if (profileBanner == null) {
             profileBanner = (new ImageIcon("src/banner.jpeg")).getImage();
         }
 
     }
 
     public String getName() {
         return name;
 
     }
 
     public String getScreenName() {
         return screenName;
     }
 
     public String getScreenName(long l) {
         try {
             return "@" + t.showUser(l).getScreenName();
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (TwitterException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public String getDescription() {
         return description;
     }
 
     public String getURL() {
         return url;
     }
 
     public String getLocation() {
         return location;
     }
 
     public ImageIcon getProfileImage() {
         return profileImage;
     }
 
     public ImageIcon getSmallerProfileImage() {
         return smallerProfileImage;
     }
 
     public ImageIcon getSmallerProfileImage(long userId) {
         try {
             return new ImageIcon(new URL(t.showUser(userId)
                     .getProfileImageURL()));
         } catch (MalformedURLException e) {
             return null;
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (TwitterException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     public Image getProfileBanner() {
         return profileBanner;
     }
 
     public boolean updateStatus(String str) {
         try {
             t.updateStatus(str);
             tweetCount++;
             return true;
 
         } catch (TwitterException e) {
             return false;
         }
     }
 
     public boolean tweetImage(File img, String message) {
         try {
             StatusUpdate status = new StatusUpdate(message);
             GeoQuery gq = new GeoQuery(InetAddress.getLocalHost().getHostAddress());
             status.setLocation(gq.getLocation());
             System.out.println(gq);
             status.setMedia(img);
            //t.updateStatus(status);
             tweetCount++;
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public int getTweetCount() {
         return tweetCount;
     }
 
     public long[] getFriendsIDs() {
         return friendsIDs;
     }
 
     public int getFriendsCount() {
         return friendsCount;
     }
 
     public List<User> getFollowers() {
         return followers;
     }
 
     public List<User> getFollowing() {
         return following;
     }
 
     public boolean unfollow(long l) {
         try {
             t.destroyFriendship(l);
             friendsCount--;
             return true;
         } catch (TwitterException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     public User showUser(long l) {
         try {
             return t.showUser(l);
         } catch (TwitterException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public int getFollowersCount() {
         return followersCount;
     }
 
     public boolean destroyStatus(Long l) {
         try {
             t.destroyStatus(l);
             tweetCount--;
             return true;
         } catch (TwitterException e) {
             return false;
         }
     }
 
     public Trends getTrending(int woeid) {
         try {
             trending = t.getPlaceTrends(woeid);
         } catch (TwitterException e) {
             e.printStackTrace();
         }
         return trending;
     }
 
     public Tweets getHomeTimeline() {
         return homeTimeline;
     }
 
     public Tweets getUserTimeline() {
         return userTimeline;
     }
 
     public List<DirectMessage> getAllMessages() {
         return messages;
     }
 
     public DirectMessage showDirectMessage(long messageId) {
         try {
             return t.showDirectMessage(messageId);
         } catch (TwitterException e) {}
         return null;
 
     }
 
     public boolean sendDirectMessage(long userId, String text) {
         try {
             t.sendDirectMessage(userId, text);
             return true;
         } catch (TwitterException e) {
             return false;
         }
     }
 
     public long getCurrentUserID() {
         try {
             return t.getId();
         } 
         catch (IllegalStateException e) {} 
         catch (TwitterException e) {}
         return 0;
     }
 
     public User getUser(long id) {
         try {
             return t.showUser(id);
         } catch (TwitterException e) {
 
         }
         return null;
     }
 
     public User follow(long l) {
         try {
             User u = t.createFriendship(l);
             friendsCount++;
             return u;
         } catch (TwitterException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public ResponseList<User> searchUsers(String text) {
         try {
             return t.searchUsers(text, 1);
         } catch (TwitterException e) {}
         return null;
     }
 
 }
