 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.FeedItem;
 import com.directi.train.tweetapp.model.UserProfileItem;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 public class FeedStore {
     private SimpleJdbcTemplate db;
     private UserStore userStore;
 
     @Autowired
     public FeedStore(SimpleJdbcTemplate simpleJdbcTemplate, UserStore userStore) {
         this.db = simpleJdbcTemplate;
         this.userStore = userStore;
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
 
         List<UserProfileItem> followerIdList = userStore.followerList(userName);
         for (UserProfileItem userProfileItem : followerIdList) {
             Integer i = (Integer)userProfileItem.getId();
             System.out.println("followerId: " + i);
             db.update("insert into feeds (user_id, receiver_id, tweet, tweet_id, creator_id, timestamp) values(?, ?, ?, ?, ?, now())",
                     userId, i, feedItem.getTweet(), nextUniqueTweetId, creatorId);
         }
         db.update("insert into feeds (user_id, receiver_id, tweet, tweet_id, creator_id, timestamp) values(?, ?, ?, ?, ?, now())",
                 userId, userId, feedItem.getTweet(), nextUniqueTweetId, creatorId);
         int id = db.queryForInt(String.format("select id from feeds where user_id =%d order by id desc limit 1", userId));
 
         return db.queryForObject(String.format(userStore.preConditionSQL + "feeds.id = %d" + userStore.postConditionSQL + "desc", id), FeedItem.rowMapper);
     }
 
     public List<FeedItem> feed(Long userId) {
         List<FeedItem> feedItems = db.query(String.format(userStore.preConditionSQL + "feeds.receiver_id = %d" + userStore.postConditionSQL + "desc limit %d",
                                    userId, userStore.feedItemLimit), FeedItem.rowMapper);
         for (FeedItem feedItem : feedItems) {
                 feedItem.setFavorite(db.queryForInt(String.format("select count(*) from favorites where tweet_id = %d and user_id =  %d",
                                      feedItem.getTweetId(), feedItem.getUserId())) > 0);
         }
         return feedItems;
     }
 
 
     public List<FeedItem> newFeedsList(Long feedId, Long userId) {
         List<FeedItem> newFeedItems = db.query(String.format(userStore.preConditionSQL + "feeds.receiver_id = %d and feeds.id > %d" + userStore.postConditionSQL,
                                       userId, feedId), FeedItem.rowMapper);
         for (FeedItem newFeedItem : newFeedItems) {
             newFeedItem.setFavorite(db.queryForInt(String.format("select count(*) from favorites where tweet_id = %d and user_id =  %d",
                                     newFeedItem.getTweetId(), newFeedItem.getUserId())) > 0);
         }
         return newFeedItems;
     }
 
     public List<FeedItem> oldFeedsList(Long feedId, Long userId) {
         List<FeedItem> oldFeedItems = db.query(String.format(userStore.preConditionSQL + "feeds.receiver_id = %d and feeds.id < %d" + userStore.postConditionSQL + "desc limit %d",
                                       userId, feedId, userStore.feedItemLimit), FeedItem.rowMapper);
         for (FeedItem oldFeedItem : oldFeedItems) {
             oldFeedItem.setFavorite(db.queryForInt(String.format("select count(*) from favorites where tweet_id = %d and user_id =  %d",
                                     oldFeedItem.getTweetId(), oldFeedItem.getUserId())) > 0);
         }
         return oldFeedItems;
     }
 
     public boolean favoriteTweet(Long creatorId, Long tweetId, Long userId) {
         return db.update(String.format("insert into favorites (tweet_id, user_id) values (%d, %d)", tweetId, userId)) > 0;
     }
 
     public boolean unFavoriteTweet(Long creatorId, Long tweetId, Long userId) {
        return db.update(String.format("delete from favorites where tweet_id = %d and user_id = %d", tweetId, userId)) > 0;
     }
 
     public FeedItem reTweet(Long creatorId, Long tweetId, Long userId) {
         if (creatorId.equals(userId)) {
             System.out.println("User #" + userId + " can't retweet its own!");
             return null;
         }
         if (db.queryForInt(String.format("select count(*) from retweets where tweet_id = %d and user_id = %d", tweetId, userId)) > 0) {
             System.out.println("User #" + userId + " can't retweet same twice!");
             return null;
         }
         db.update("insert into retweets (tweet_id, user_id) values (?, ?)", tweetId, userId);
 
         FeedItem feedItem = new FeedItem();
         feedItem.setTweetId(tweetId);
         String tweet = db.queryForObject(String.format("select tweet from feeds where tweet_id = %d and user_id = creator_id and user_id = receiver_id",
                 tweetId), new RowMapper<String>() {
                 @Override
                 public String mapRow(ResultSet resultSet, int i) throws SQLException {
                     return resultSet.getString("tweet");
                 }
         });
         feedItem.setTweet(tweet);
         feedItem.setCreatorId(creatorId);
         return add(feedItem,userId);
     }
 
     public void unReTweet(Long creatorId, Long tweetId, Long userId) {
         if (creatorId.equals(userId)) {
             System.out.println("User #" + userId + " can't unretweet its own!");
             return;
         }
         db.update(String.format("delete from retweets where tweet_id = %d and user_id = %d", tweetId, userId));
     }
 
     public List<Long> favoritedUsers(Long tweetId) {
         return db.query(String.format("select user_id from favorites where tweet_id = %d", tweetId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("user_id");
             }
         });
     }
 
     public List<Long> reTweetedUsers(Long tweetId) {
         return db.query(String.format("select user_id from retweets where tweet_id = %d", tweetId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("user_id");
             }
         });
     }
 
 }
