 package com.springapp.mvc.data;
 
 /**
  * Created with IntelliJ IDEA.
  * User: root
  * Date: 7/10/13
  * Time: 8:56 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 import com.springapp.mvc.HelperClasses.ByteObjectConversion;
 import com.springapp.mvc.model.Friend;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 import com.springapp.mvc.model.User;
 import redis.clients.jedis.Jedis;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 @Repository
 public class FriendRepository {
 
     static Logger log = Logger.getLogger(TweetRepository.class);
     private final JdbcTemplate jdbcTemplate;
     private ByteObjectConversion byteObjectConverter;
     private final Jedis jedis;
 
     @Autowired
     public FriendRepository(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
         jedis = new Jedis("localhost");
         jedis.connect();
         byteObjectConverter = new ByteObjectConversion();
     }
 
     public List<User> fetchFollowing(String userName) {
         System.out.println("Fetching following from friendRepository.");
         List<User> followings;
         String query = "select username,name,email from users where username in (select following from following where follower="+userName+" and timestamp is null)";
         byte[] queryInBytes = byteObjectConverter.toBytes(query);
        if (jedis.get(queryInBytes) == null){
             System.out.println("Cache is empty. Filling Cache.");
 
             followings = jdbcTemplate.query("select username,name,email from users where username in (select following from following where follower=? and timestamp is null)",
                     new Object[]{userName}, new BeanPropertyRowMapper<>(User.class));
 
             byte[] inputBytes = byteObjectConverter.toBytes(followings);
             jedis.set(queryInBytes, inputBytes);
             System.out.println("Cache filled.");
 
             return followings;
         }
 
         System.out.println("Replying from the cache..");
         byte[] outputBytes = jedis.get(queryInBytes);
         Object outputObjectUser = byteObjectConverter.fromBytes(outputBytes);
         List<User> outputFollowings = (List<User>) outputObjectUser;
         return outputFollowings;
     }
 
     public List<User> fetchFollowers(String userName) {
         System.out.println("Fetching follower from friendRepository.");
         List<User> follower;
         String query = "select username,name,email from users where username in (select follower from following where following="+userName+" and timestamp is null)";
         byte[] queryInBytes = byteObjectConverter.toBytes(query);
        if (jedis.get(queryInBytes) == null){
             System.out.println("Cache is empty. Filling Cache.");
 
             follower = jdbcTemplate.query("select username,name,email from users where username in (select follower from following where following=? and timestamp is null)",
                     new Object[]{userName}, new BeanPropertyRowMapper<>(User.class));
 
             byte[] inputBytes = byteObjectConverter.toBytes(follower);
             jedis.set(queryInBytes, inputBytes);
             System.out.println("Cache filled.");
 
             return follower;
         }
 
         System.out.println("Replying from the cache..");
         byte[] outputBytes = jedis.get(queryInBytes);
         Object outputObjectUser = byteObjectConverter.fromBytes(outputBytes);
         List<User> outputFollower = (List<User>) outputObjectUser;
         return outputFollower;
     }
 
 
     public static String encodePassword(String password) {
         return DigestUtils.sha256Hex(password);
     }
 
     public void addFriend(String username, String toFollow) {
         System.out.println("addFriend request came from "+username+" to "+toFollow+".");
 
         if(!username.equals(toFollow)) {
             List<Friend> list = jdbcTemplate.query("select * from following where follower=? AND following=?",
                     new Object[]{username, toFollow}, new BeanPropertyRowMapper<>(Friend.class));
             if(list.size()>0) {
                 Friend friend = list.get(0);
                 System.out.println(friend.getTimestamp());
                 if(friend.timestamp==null) log.info("Relationship already exists");
                 else {
                     jdbcTemplate.execute("update following set timestamp = null where follower = '"+ username + "' AND following = '" + toFollow + "'");
                     log.info("Resetting timestamp to null value");
                 }
             }
             else jdbcTemplate.execute("INSERT into following VALUES('" + username + "','" + toFollow + "')");
             String query1 = "select username,name,email from users where username in (select following from following where follower="+username+" and timestamp is null)";
             String query2 = "select username,name,email from users where username in (select follower from following where following="+toFollow+" and timestamp is null)";
             byte[] query1InBytes = byteObjectConverter.toBytes(query1);
             byte[] query2InBytes = byteObjectConverter.toBytes(query2);
             jedis.del(query1InBytes);
             jedis.del(query2InBytes);
         }
         else log.info("Can't follow yourself");
     }
 
     public void deleteFollower(String username, String toUnfollow) {
         System.out.println("deleteFollower request came from "+username+" to "+toUnfollow+".");
         List<Friend> list = jdbcTemplate.query("select * from following where follower=? AND following=?",
                new Object[]{username, toUnfollow}, new BeanPropertyRowMapper<>(Friend.class));
         if(list.size()==0) log.info("No record exists");
         else {
             Friend friend = list.get(0);
             if(friend.timestamp==null) {
                 Timestamp timestamp = new Timestamp(new Date().getTime());
                 jdbcTemplate.execute("update following set timestamp='" + timestamp + "' where follower = '"+ username + "' AND following = '" + toUnfollow + "'");
                 log.info("Resetting timestamp to current time");
                 String query1 = "select username,name,email from users where username in (select following from following where follower="+username+" and timestamp is null)";
                 String query2 = "select username,name,email from users where username in (select follower from following where following="+toUnfollow+" and timestamp is null)";
                 byte[] query1InBytes = byteObjectConverter.toBytes(query1);
                 byte[] query2InBytes = byteObjectConverter.toBytes(query2);
                 jedis.del(query1InBytes);
                 jedis.del(query2InBytes);
             }
             else log.info("Already unfollowed");
         }
     }
 
     public boolean existsFriendShip(String username, String following) {
         int row = jdbcTemplate.queryForInt("select count(*) from following where follower=? and following=? and timestamp is null",new Object[]{username,following});
         return row != 0;
     }
 }
