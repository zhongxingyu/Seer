 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.FeedItem;
 import com.directi.train.tweetapp.model.UserItem;
 import com.directi.train.tweetapp.model.UserProfileItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import java.io.Console;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 /**
  * Created with IntelliJ IDEA.
  * User: elricl
  * Date: 20/7/12
  * Time: 12:44 AM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class UserStore {
     private SimpleJdbcTemplate db;
 
     @Autowired
     public UserStore(SimpleJdbcTemplate template) {
         this.db = template;
     }
 
     public long getUserId(String userName) {
         return db.queryForInt(String.format("select id from users where username='%s'", userName));
     }
 
     public List<UserProfileItem> followingList(String userName) {
         long userId = getUserId(userName);
         List<UserProfileItem> users = db.query(String.format("select username, id, email from users inner join following on following.following_id = users.id where user_id =%d", userId), UserProfileItem.rowMapper);
         return applyFollowing(userId, users);
     }
 
     public List<UserProfileItem> followerList(String userName) {
         long userId = getUserId(userName);
         List<UserProfileItem> users = db.query(String.format("select username, id, email from users inner join followers on followers.follower_id = users.id where user_id =%d", userId), UserProfileItem.rowMapper);
         return applyFollowing(userId, users);
     }
 
     private List<UserProfileItem> applyFollowing(long userId, List<UserProfileItem> users) {
         for (UserProfileItem user : users) {
             user.setFollowing(db.queryForInt(String.format("select count(*) from following where user_id = %d and following_id = %d", userId, user.getId())) > 0);
         }
         return users;
     }
 
     public String registerUser(String email,String userName,String password) {
         List<UserItem> userData = db.query(String.format("select * from users where username='%s' or email='%s'",
                                   userName,email), UserItem.rowMapper);
         UserItem userItem;
         try {
             userItem = userData.get(0);
             if(userItem.getEmail().equals(email) ){
                 return "1";
             }
             if(userItem.getUsername().equals(userName)){
                 return "2";
             }
         }
         catch (IndexOutOfBoundsException e) {
             password = SHA(password);
             db.update("insert into users (email, username, password) values(?, ?, ?)",email, userName, password);
         }
         return "0";
     }
 
     private String SHA(String password) {
         MessageDigest md = null;
         byte [] passwordBytes = password.getBytes();
         try {
             md = MessageDigest.getInstance("SHA1");
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         }
         md.update(passwordBytes,0,passwordBytes.length);
         byte[] encodedPassword = md.digest();
         return toHexString(encodedPassword);
     }
 
     public static String toHexString(byte[] buf) {
         char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
         'A', 'B', 'C', 'D', 'E', 'F' };
 
         StringBuffer strBuf = new StringBuffer(buf.length * 2);
         for (int i = 0; i < buf.length; i++) {
             strBuf.append(hexChar[(buf[i] & 0xf0) >>> 4]);
             strBuf.append(':');
             strBuf.append(hexChar[buf[i] & 0x0f]);
         }
         return strBuf.toString();
     }
 
     public UserItem checkLogin(String userName,String password) throws Exception{
         UserItem userData;
         try {
             userData = db.query("select * from users where username='"+ userName +"'", UserItem.rowMapper).get(0);
             if (userData.getPassword().equals(SHA(password))) {
                 userData.getId();
             } else {
                 System.out.println(userData.getPassword());
                 throw new Exception("Invalid Password");
             }
         }
         catch (EmptyResultDataAccessException e) {
             throw new Exception("User does not exist.Please Register");
         }
         return userData;
     }
 
     public int followUser(String userName, Long loggedUserId) {
         try {
             System.out.println(loggedUserId);
             long otherUserId = getUserId(userName);
             System.out.println(otherUserId);
 
             if (loggedUserId.equals(otherUserId)) {
                 System.err.println("User #" + loggedUserId + " can't follow itself!");
                 return 1;
             }
 
             db.update("insert into following (user_id, following_id) values (? ,?)", loggedUserId, otherUserId);
             db.update("insert into followers (user_id, follower_id) values  (?, ?)", otherUserId, loggedUserId);
             return 0;
         }
         catch (IndexOutOfBoundsException E) {
             System.out.println("User " + userName + "doesn't exist !");
             return 1;
         }
         catch (Exception E) {
             System.out.println("Follow operation unsuccessful !");
             E.printStackTrace();
             return 1;
         }
     }
 
     public int unFollowUser(String userName, Long loggedUserId) {
         try {
             System.out.println(loggedUserId);
             long otherUserId = getUserId(userName);
             System.out.println(otherUserId);
 
             if (loggedUserId.equals(otherUserId)) {
                 System.out.println("User #" + loggedUserId + " can't unFollow itself!");
                 return 1;
             }
 
             db.update(String.format("delete from following where user_id = %d and following_id = %d", loggedUserId, otherUserId));
             db.update(String.format("delete from followers where user_id = %d and follower_id = %d", otherUserId, loggedUserId));
             return 0;
         }
         catch (IndexOutOfBoundsException E) {
             System.out.println("User " + userName + "doesn't exist !");
             return 1;
         }
         catch (Exception E) {
             System.out.println("unFollow operation unsuccessful !");
             E.printStackTrace();
             return 1;
         }
     }
 
     public List<FeedItem> tweetList(String userName) {
         long userId = getUserId(userName);
        String conditionalSQL = "feeds.user_id = %d and feeds.user_id = feeds.receiver_id and feeds.id > %d";
         String orderingSQL = "desc limit %d";
         return FeedStore.feedQueryAndFavoriteStatus(userId, conditionalSQL, orderingSQL, getMinFeedId(), getFeedLimit());
     }
 
     public Integer checkFollowingStatus(String curUser,String otherUser) {
         return db.queryForInt(String.format("select count(*) from followers where user_id = %d and follower_id = %d",
                 getUserId(otherUser),getUserId(curUser)));
     }
 
     public Integer noOfFollowers(String userName) {
         return db.queryForInt(String.format("select count(*) from followers where user_id=%d",getUserId(userName)));
     }
 
     public Integer noOfFollowing(String userName) {
         return db.queryForInt(String.format("select count(*) from following where user_id=%d",getUserId(userName)));
     }
 
     public List<Long> getFavoriteTweetsOfAUser(String userName) {
         long userId = getUserId(userName);
         return db.query(String.format("select tweet_id from favorites where user_id = %d", userId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("tweet_id");
             }
         } );
     }
 
     public List<Long> getReTweetsOfAUser(String userName) {
         long userId = getUserId(userName);
         return db.query(String.format("select tweet_id from retweets where user_id = %d", userId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("tweet_id");
             }
         } );
     }
 
     public int noOfTweets(String userName) {
         return db.queryForInt(String.format("select count(*) from feeds where user_id = receiver_id and user_id='%d'",getUserId(userName)));
     }
 
     public static String getPostSQL() {
         final String postConditionSQL = " ) something inner join users " +
                 "on something.creator_id = users.id " +
                 "order by something.id ";
 
         return postConditionSQL;
     }
 
     public static String getPreSQL() {
         final String preConditionSQL = " select something.id, user_id, something.username, tweet_id, tweet, creator_id, users.username as creatorname,users.email as creatoremail " +
                 "from ( select feeds.id, feeds.user_id , users.username, feeds.tweet_id, feeds.tweet, feeds.creator_id " +
                 "from feeds inner join users " +
                 "on users.id = feeds.user_id " +
                 "where ";
 
         return preConditionSQL;
     }
 
     public static Long getMaxFeedLimit() {
         return 10000L;
     }
 
     public static Long getFeedLimit() {
         return 20L;
     }
 
     public static Long getMinFeedId() {
         return 0L;
     }
 
 }
