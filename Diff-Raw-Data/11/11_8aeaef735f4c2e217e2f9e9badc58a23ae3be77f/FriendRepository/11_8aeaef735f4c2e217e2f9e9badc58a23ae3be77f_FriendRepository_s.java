 package com.springapp.mvc.data;
 
 /**
  * Created with IntelliJ IDEA.
  * User: root
  * Date: 7/10/13
  * Time: 8:56 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 import com.springapp.mvc.model.User;
 
 import java.util.List;
 
 @Repository
 public class FriendRepository {
 
     static Logger log = Logger.getLogger(TweetRepository.class);
     private final JdbcTemplate jdbcTemplate;
 
     @Autowired
     public FriendRepository(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public List<User> fetchFollowing(String userName) {
         return jdbcTemplate.query("select username,name,email from users where username in (select following from following where follower=?)",
                 new Object[]{userName}, new BeanPropertyRowMapper<>(User.class));
     }
 
     public List<User> fetchFollowers(String userName) {
         return jdbcTemplate.query("select username,name,email from users where username in (select follower from following where following=?)",
                 new Object[]{userName}, new BeanPropertyRowMapper<>(User.class));
     }
 
 
     public static String encodePassword(String password) {
         return DigestUtils.sha256Hex(password);
     }
 
     public void addFollower(String username, String toFollow) {
        List<Object> list = jdbcTemplate.query("select following from following where follower=? AND following=?",
                new Object[]{username, toFollow}, new BeanPropertyRowMapper<>(Object.class));
        if(list.size()>0) log.info("Tuple Already exists");
        else jdbcTemplate.execute("INSERT into following VALUES('" + username + "','" + toFollow + "')");
     }
 }
