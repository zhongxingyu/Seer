 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.FeedItem;
 import com.directi.train.tweetapp.model.UserProfileItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sitesh
  * Date: 24/7/12
  * Time: 2:33 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class FeedStore  {
     private static SimpleJdbcTemplate db;
     private UserStore localUserStore;
 
     @Autowired
     public FeedStore(SimpleJdbcTemplate template, UserStore localUserStore) {
         this.db = template;
         this.localUserStore = localUserStore;
     }
 
     public FeedItem add(FeedItem feedItem,Long userId) {
         System.out.println("userId: " + userId);
         System.out.println("tweet: " + feedItem.getTweet());
 
         String userName = (String)db.query(String.format("select username from users where id = %d", userId),new RowMapper<Object>() {
             @Override
             public String mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getString("username");
             }
         }).get(0);
         System.out.println("userName: " + userName);
 
         Long nextUniqueTweetId = feedItem.getTweetId();
         if (nextUniqueTweetId == null)
             nextUniqueTweetId = 1 + db.queryForLong(String.format("SELECT MAX(tweet_id) from (SELECT tweet_id from feeds where creator_id = %d) tweetidtable",
                                                     userId));
         System.out.println("nextUniqueTweetId: " + nextUniqueTweetId);
 
         Long creatorId = feedItem.getCreatorId();
         if (creatorId == null)
             creatorId = userId;
         System.out.println("creatorId: " + creatorId);
 
         List<UserProfileItem> followerIdList = localUserStore.followerList(userName);
         for (UserProfileItem userProfileItem : followerIdList) {
             Integer i = (Integer)userProfileItem.getId();
             System.out.println("followerId: " + i);
             db.update(String.format("insert into feeds (user_id, receiver_id, tweet, tweet_id, creator_id, timestamp) values(%d, %d, '%s', %d, %d, now())",
                     userId, i, feedItem.getTweet(), nextUniqueTweetId, creatorId));
         }
         db.update(String.format("insert into feeds (user_id, receiver_id, tweet, tweet_id, creator_id, timestamp) values(%d, %d, '%s', %d, %d, now())",
                 userId, userId, feedItem.getTweet(), nextUniqueTweetId, creatorId));
         int id = db.queryForInt(String.format("select id from feeds where user_id =%d order by id desc limit 1", userId));
 
         return db.queryForObject(String.format(UserStore.getPreSQL() + "feeds.id = %d" + UserStore.getPostSQL() + "desc", id), FeedItem.rowMapper);
     }
 
     public List<FeedItem> feed(Long userId) {
         String conditionalSQL = "feeds.receiver_id = %d and feeds.id > %d";
         String orderingSQL = "desc limit %d";
         return feedQueryAndFavoriteStatus(userId, conditionalSQL, orderingSQL, UserStore.getMinFeedId(), UserStore.getFeedLimit());
     }
 
 
     public List<FeedItem> newFeedsList(Long feedId, Long userId) {
         String conditionalSQL = "feeds.receiver_id = %d and feeds.id > %d";
         String orderingSQL = "asc limit %d";
         return feedQueryAndFavoriteStatus(userId, conditionalSQL, orderingSQL, feedId, UserStore.getMaxFeedLimit());
     }
 
     public List<FeedItem> oldFeedsList(Long feedId, Long userId) {
         String conditionalSQL = "feeds.receiver_id = %d and feeds.id < %d";
         String orderingSQL = "desc limit %d";
         return feedQueryAndFavoriteStatus(userId, conditionalSQL, orderingSQL, feedId, UserStore.getFeedLimit());
     }
 
     public static List<FeedItem> feedQueryAndFavoriteStatus(Long userId, String conditionalSQL, String orderingSQL, Long feedId, Long feedLimit) {
         List<FeedItem> feedItems = db.query(String.format(UserStore.getPreSQL() + conditionalSQL + UserStore.getPostSQL() + orderingSQL,
                 userId, feedId, feedLimit), FeedItem.rowMapper);
 
         for (FeedItem feedItem : feedItems) {
             feedItem.setFavorite(isFavorited(feedItem.getCreatorId(), feedItem.getTweetId(), feedItem.getUserId()));
         }
         return feedItems;
     }
 
     public static boolean isFavorited(Long creatorId, Long tweetId, Long userId) {
         return db.queryForInt(String.format("select count(*) from favorites where tweet_id = %d and user_id = %d and creator_id = %d", tweetId, userId, creatorId)) > 0;
     }
 
     public boolean favoriteTweet(Long creatorId, Long tweetId, Long userId) {
         if (isFavorited(creatorId, tweetId, userId))
             return false;
         return db.update(String.format("insert into favorites (tweet_id, user_id, creator_id) values (%d, %d, %d)", tweetId, userId, creatorId)) > 0;
     }
 
     public boolean unFavoriteTweet(Long creatorId, Long tweetId, Long userId) {
         if (isFavorited(creatorId, tweetId, userId))
             return db.update(String.format("delete from favorites where tweet_id = %d and user_id = %d and creator_id = %d", tweetId, userId, creatorId)) > 0;
         return false;
     }
 
     private boolean isRetweeted(Long creatorId, Long tweetId, long userId) {
         return db.queryForInt(String.format("select count(*) from retweets where tweet_id = %d and user_id = %d and creator_id = %d", tweetId, userId, creatorId)) > 0;
     }
 
     public FeedItem reTweet(Long creatorId, Long tweetId, Long userId) {
         if (creatorId.equals(userId)) {
             System.out.println("User #" + userId + " can't retweet its own!");
             return null;
         }
         if (isRetweeted(creatorId, tweetId, userId)) {
             System.out.println("User #" + userId + " can't retweet same twice!");
             return null;
         }
         db.update(String.format("insert into retweets (tweet_id, user_id, creator_id) values (%d, %d, %d)", tweetId, userId, creatorId));
 
         FeedItem feedItem = new FeedItem();
         feedItem.setTweetId(tweetId);
        String tweet = db.queryForObject(String.format("select tweet from feeds where tweet_id = %d and creator_id = %d and user_id = creator_id and user_id = receiver_id",
                tweetId, creatorId), new RowMapper<String>() {
                 @Override
                 public String mapRow(ResultSet resultSet, int i) throws SQLException {
                     return resultSet.getString("tweet");
                 }
         });
         feedItem.setTweet(tweet);
         feedItem.setCreatorId(creatorId);
         return add(feedItem, userId);
     }
 
     public void unReTweet(Long creatorId, Long tweetId, Long userId) {
         if (creatorId.equals(userId)) {
             System.out.println("User #" + userId + " can't unretweet its own!");
             return;
         }
         if (!isRetweeted(creatorId, tweetId, userId)) {
             System.out.println("User #" + userId + " cam't unretweet without retweeting, right?");
             return;
         }
         db.update(String.format("delete from retweets where tweet_id = %d and user_id = %d", tweetId, userId));
     }
 
     public List<Long> favoritedUsers(Long creatorId, Long tweetId) {
         return db.query(String.format("select user_id from favorites where tweet_id = %d and creator_id = %d", tweetId, creatorId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("user_id");
             }
         });
     }
 
     public List<Long> reTweetedUsers(Long creatorId, Long tweetId) {
         return db.query(String.format("select user_id from retweets where tweet_id = %d and creator_id = %d", tweetId, creatorId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("user_id");
             }
         });
     }
 
 }
