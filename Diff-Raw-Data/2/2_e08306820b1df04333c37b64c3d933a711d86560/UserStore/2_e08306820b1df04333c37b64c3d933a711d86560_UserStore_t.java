 package sample.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 import sample.model.FollowRowMapper;
 import sample.model.User;
 import sample.model.UserRowMapper;
 import sample.utilities.MD5Encoder;
 
 import java.util.Hashtable;
 import java.util.List;
 
 /**
  * Created on IntelliJ IDEA.
  * User: dushyant
  * Date: 18/7/12
  * Time: 1:46 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class UserStore {
     SimpleJdbcTemplate jdbcTemplate;
     AuthKeyStore authKeyStore;
     MD5Encoder md5Encoder;
 
     @Autowired
     public UserStore(SimpleJdbcTemplate jdbcTemplate, AuthKeyStore authKeyStore, MD5Encoder md5Encoder){
         this.jdbcTemplate = jdbcTemplate;
         this.authKeyStore = authKeyStore;
         this.md5Encoder = md5Encoder;
     }
 
     //TODO: return status
     public void addFollower(int following, String userID) {
         jdbcTemplate.update("INSERT INTO followers (user_id, follower) VALUES (?,?)", following, userID);
         jdbcTemplate.update("UPDATE users SET num_followings=num_followings+1 where id=?", userID);
         jdbcTemplate.update("UPDATE users SET num_followers=num_followers+1 where id=?", following);
     }
 
     public void removeFollower(int following, String userID) {
         jdbcTemplate.update("UPDATE followers SET unfollow_time = NOW() where user_id=? AND follower=?", following, userID);
         jdbcTemplate.update("UPDATE users SET num_followers=num_followers-1 where id=?", following);
         jdbcTemplate.update("UPDATE users SET num_followings=num_followings-1 where id=?", userID);
     }
 
 
     public User addUser(String username, String email, String password) {
         //todo : make sure username and password are unique
         try {
             String baseUrl = "http://www.gravatar.com/avatar/";
             String image_url = baseUrl.concat(md5Encoder.encodeString(email));
             jdbcTemplate.update("INSERT INTO users (username, email, password, image_url) VALUES (?,?,?,?)",username, email, password, image_url);
             UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
             User user = (User)jdbcTemplate.queryForObject("SELECT * from users where username=\"" + username + "\"", userRowMapper);
             return user;
         }
         catch (DuplicateKeyException e){
             return null;
         }
     }
 
     public User updateUserPassword(String userID, String old_password, String new_password) {
         User user = this.authUserByUserID(userID, old_password);
         if (user!=null)
             jdbcTemplate.update("UPDATE users SET password=? where id=?",new_password, userID);
         return user;
     }
 
     public Hashtable<String, String> updateUserAccount(String userID, String username, String email) {
         Hashtable<String, String> hs = new Hashtable();
         hs.put("status", "failed");
 
         if (validateUserById(userID))
             try{
                 jdbcTemplate.update("UPDATE users SET username=?, email=? where id=?",username, email, userID);
                 hs.put("status", "success");
             }
             catch (DuplicateKeyException e){
                 hs.put("message", "User already exists with same username or email");
                return hs;
             }
 
         return hs;
     }
 
     public boolean updateUserProfile(String userID, String name, String description) {
         jdbcTemplate.update("UPDATE users SET name=?, description=? where id=?", name, description, userID);
         return true;
     }
 
     public boolean validateUserById(String userID){
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID, userRowMapper);
             return true;
         }
         catch (EmptyResultDataAccessException e){
             return false;
         }
     }
 
     public User getUser(String userID, String callerUserID){
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         FollowRowMapper followRowMapper = new FollowRowMapper();
         try{
 //            User user = (User) jdbcTemplate.queryForObject("select * from users INNER JOIN followers on users.id=followers.user_id where users.id=" + userID + " AND followers.follower="+callerUserID, userRowMapper);
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID, userRowMapper);
             user.setFollowed(false);
 
             if (callerUserID != null) {
                 try{
                     Boolean follow = (Boolean) jdbcTemplate.queryForObject("select * from followers where user_id="+userID +" AND follower="+callerUserID, followRowMapper);
                     user.setFollowed(follow);
                 }
                 catch (EmptyResultDataAccessException e){
                 }
             }
             return user;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
     public User authUserByUserID(String userID, String password) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID + " and password=\""+ password + "\"", userRowMapper);
             return user;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
 
 
 
     public List<User> getFollowers(String userID, String count, String max_id, String callerUserID ) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         count = (count == null) ? "20" : count;
         String query;
 
         if (max_id == null)
             query = "select * from users where id in (select follower from followers where user_id="+ userID +" AND unfollow_time > NOW()) ORDER BY id DESC LIMIT "+count;
         else
             query = "select * from users where id in (select follower from followers where user_id="+ userID +" AND unfollow_time > NOW() AND follower<" + max_id+" ) ORDER BY id DESC LIMIT "+count;
 
         List<User> followers = jdbcTemplate.query(query, userRowMapper);
         if (callerUserID != null) {
             for(User u:followers){
                 try{
                     FollowRowMapper followRowMapper = new FollowRowMapper();
                     Boolean follow = (Boolean) jdbcTemplate.queryForObject("select * from followers where user_id="+u.getId()+" AND follower="+callerUserID, followRowMapper);
                     u.setFollowed(follow);
                 }
                 catch (EmptyResultDataAccessException e){
                     u.setFollowed(false);
                 }
             }
         }
         return followers;
     }
 
     public List<User> getFollowings(String userID, String count, String max_id, String callerUserID) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         count = (count == null) ? "20" : count;
         String query;
 
         if (max_id == null)
             query = "select * from users where id in (select user_id from followers where follower="+ userID +" AND unfollow_time > NOW()) ORDER BY id DESC LIMIT "+count;
         else
             query = "select * from users where id in (select user_id from followers where follower="+ userID +" AND unfollow_time > NOW() AND user_id<" + max_id+" ) ORDER BY id DESC LIMIT "+count;
 
         List<User> followings = jdbcTemplate.query(query, userRowMapper);
         if (callerUserID!=null) {
             if (userID == callerUserID)
                 for(User u:followings){
                     u.setFollowed(true);
                 }
             else {
                 for(User u:followings){
                     try{
                         FollowRowMapper followRowMapper = new FollowRowMapper();
                         Boolean follow = (Boolean) jdbcTemplate.queryForObject("select * from followers where user_id="+u.getId()+" AND follower="+callerUserID, followRowMapper);
                         u.setFollowed(follow);
                     }
                     catch (EmptyResultDataAccessException e){
                         u.setFollowed(false);
                     }
                 }
 
             }
         }
 
         return followings;
     }
 
     public boolean addFollowing(String followee_id, String follower_id) {
         try{
             jdbcTemplate.update("INSERT INTO followers (user_id, follower) VALUES (?,?)", followee_id, follower_id);
             jdbcTemplate.update("UPDATE users SET num_followings=num_followings+1 where id=?", follower_id);
             jdbcTemplate.update("UPDATE users SET num_followers=num_followers+1 where id=?", followee_id);
             return true;
         }
         catch (DuplicateKeyException e){
             jdbcTemplate.update("UPDATE followers SET unfollow_time='2038-01-01 00:00:00' where user_id=? AND follower=?",followee_id ,follower_id);
             jdbcTemplate.update("UPDATE users SET num_followings=num_followings+1 where id=?", follower_id);
             jdbcTemplate.update("UPDATE users SET num_followers=num_followers+1 where id=?", followee_id);
             return true;
         }
     }
 
     public void deleteFollowing(String followee_id, String follower_id) {
         jdbcTemplate.update("UPDATE followers SET unfollow_time = NOW() where user_id=? AND follower=?",followee_id ,follower_id);
         jdbcTemplate.update("UPDATE users SET num_followings=num_followings-1 where id=?", follower_id);
         jdbcTemplate.update("UPDATE users SET num_followers=num_followers-1 where id=?", followee_id);
 
     }
 
     public List<User> searchForUsers(String query, String callerUserID) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         query = "\"%"+query+"%\"";
         String db_query = "select * from users where name LIKE "+query+" OR username LIKE "+query;
 
         List<User> followers = jdbcTemplate.query(db_query, userRowMapper);
         if (callerUserID != null) {
             for(User u:followers){
                 try{
                     FollowRowMapper followRowMapper = new FollowRowMapper();
                     Boolean follow = (Boolean) jdbcTemplate.queryForObject("select * from followers where user_id="+u.getId()+" AND follower="+callerUserID, followRowMapper);
                     u.setFollowed(follow);
                 }
                 catch (EmptyResultDataAccessException e){
                     u.setFollowed(false);
                 }
             }
         }
         return followers;
     }
 
 
 }
 
