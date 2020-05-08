 package com.directi.train.DiCon.services;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 import java.util.Map;
 
 @Service
 public class DAO {
 
     @Autowired
     private SimpleJdbcTemplate db;
 
     public int findUsername(String email) {
         return db.queryForInt("SELECT count(*) FROM twitter.login WHERE email = ?;", email);
     }
 
     public int matchPassword(String email, String password) {
         try {
             return db.queryForInt("SELECT user_id FROM twitter.login WHERE email = ? AND password = MD5(?);", email, password);
         } catch (DataAccessException e) {
             return -1;
         }
     }
 
     public Map<String, Object> getDetails(int user_id) {
         return db.queryForMap("SELECT * FROM twitter.details WHERE user_id = ?;", user_id);
     }
 
 
    public List<Map<String,Object>> getFollowersList(int user_id, int login_id){
        return db.queryForList(" select details.user_id, fullname, dp, status From twitter.details details" +
                " INNER JOIN (select f2.follower_id as user_id, CASE WHEN  f3.follower_id= ? AND f3.stop_time IS NULL THEN 'following' ELSE 'not-following' END as status " +
                "from twitter.follows f2 inner join twitter.follows f3 on f2.follower_id = f3.following_id WHERE f2.following_id= ?  AND f2.stop_time IS NULL) as follow  " +
                "ON details.user_id = follow.user_id;", user_id, login_id);    }
 
     public Integer getFollowerCount(int user_id) {
         return db.queryForInt("select count(1) from twitter.follows WHERE following_id = ? AND stop_time IS NULL;", user_id);
     }
 
 
    public List<Map<String, Object>> getFollowingList(int user_id, int login_id){
 
        return db.queryForList(" select details.user_id, fullname, dp, status From twitter.details details" +
                " INNER JOIN (select f2.following_id as user_id, CASE WHEN  f3.follower_id= ?  AND f3.stop_time IS NULL THEN 'following' ELSE 'not-following' END as status " +
                "from twitter.follows f2 inner join twitter.follows f3 on f2.following_id = f3.following_id WHERE f2.follower_id= ? AND f2.stop_time IS NULL) as follow  " +
                "ON details.user_id = follow.user_id;", user_id, login_id);
 
     }
 
     public Integer getFollowingCount(int user_id) {
         return db.queryForInt("select count(1) from twitter.follows WHERE follower_id = ? and stop_time IS NULL;", user_id);
     }
 
 
     public List<Map<String, Object>> getPostsByUser(int user_id, int latest_tweet_id) {
         return db.queryForList("SELECT D.fullname,D.dp,T.text,date_part('days',now()-T.timestamp) as days,date_part('hours',now()-T.timestamp) as hours,date_part('minutes',now()-T.timestamp) as minutes,T.tweet_id,T.user_id FROM twitter.tweets T INNER JOIN twitter.details D ON T.user_id = D.user_id WHERE T.user_id = ? AND T.tweet_id > ? ORDER BY T.timestamp ASC;", user_id, latest_tweet_id);
     }
 
     public List<Map<String, Object>> getNewsFeed(int user_id, int latest_feed_id) {
        return db.queryForList("SELECT DISTINCT D.dp,D.fullname,T.text,T.timestamp,T.tweet_id,T.user_id" +
                " FROM twitter.tweets T INNER JOIN twitter.follows  F ON T.user_id = F.following_id " +
                "INNER JOIN twitter.details D on T.user_id = D.user_id AND (T.timestamp BETWEEN F.start_time AND F.stop_time OR (F.stop_time IS NULL AND T.timestamp >= F.start_time)) " +
                "WHERE F.follower_id = ? AND tweet_id > ? ORDER BY T.timestamp ASC;", user_id, latest_feed_id);
     }
 
     public int newUser(String email, String password, String fullname, String description, String phone, String dp) {
         int user_id = db.queryForInt("SELECT MAX(user_id) FROM twitter.login;") + 1;
 
         return db.update("INSERT into twitter.login VALUES(?,?,MD5(?));", user_id, email, password)
                 & db.update("INSERT into twitter.details VALUES(?,?,?,?,?);", user_id, fullname, description, phone, dp);
     }
 
     public int follow(int follower_id, int following_id) {
         return db.update("INSERT INTO twitter.follows(follower_id,following_id) VALUES(?,?);", follower_id, following_id);
     }
 
     public int unFollow(int follower_id, int following_id) {
         return db.update("UPDATE twitter.follows SET stop_time = now() WHERE follower_id = ? AND following_id = ? AND stop_time IS NULL;", follower_id, following_id);
     }
 
     public int newTweet(int user_id, String text) {
         return db.update("INSERT INTO twitter.tweets(user_id, text) VALUES(?,?);", user_id, text);
     }
 
     public int updateFirstName(int user_id, String fullname) {
         return db.update("UPDATE twitter.details SET fullname = ? WHERE user_id = ?;", fullname, user_id);
     }
 
     public int updateLastName(int user_id, String description) {
         return db.update("UPDATE twitter.details SET description = ? WHERE user_id = ?;", description, user_id);
     }
 
     public int updatePhone(int user_id, String phone) {
         return db.update("UPDATE twitter.details SET phone = ? WHERE user_id = ?;", phone, user_id);
     }
 
     public int updateDP(int user_id, String dp) {
         return db.update("UPDATE twitter.details SET dp = ? WHERE user_id = ?;", dp, user_id);
     }
 
     public Map<String, Object> getLoginDetails(String email, String password) {
         return db.queryForMap("SELECT * from twitter.login WHERE email = ?", email);
     }
 
     public int getTweetCount(Integer userID) {
 
         return db.queryForInt("select count(1) from twitter.tweets where user_id = ?", userID);
     }
 
     public String getUsersStatus(Integer profileUserID, Integer userID) {
         if (userID.equals(profileUserID)) return "edit";
         int following = db.queryForInt("select count(1) from twitter.follows where follower_id = ? AND following_id = ? AND stop_time IS NULL;", userID, profileUserID);
         if (following > 0)
             return "following";
         return "not-following";
     }
 
     public String getFullName(Integer user_id) {
         return db.queryForObject("SELECT fullname FROM twitter.details WHERE user_id = ?;", String.class, user_id);
     }
 
     public List<Map<String, Object>> searchByFullname(String search_string) {
         return db.queryForList("SELECT user_id,fullname,dp FROM twitter.details WHERE fullname LIKE ?;", search_string + "%");
     }
 
     public String getDP(Integer following_id) {
         return db.queryForObject("SELECT dp FROM twitter.details WHERE user_id = ?;", String.class, following_id);
     }
 }
