 package sample.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 import sample.model.Post;
 import sample.model.User;
 import sample.utilities.MD5Encoder;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.Hashtable;
 
 /**
  * Created on IntelliJ IDEA.
  * User: dushyant
  * Date: 18/7/12
  * Time: 1:46 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class Store {
     SimpleJdbcTemplate jdbcTemplate;
     private final ThreadLocal<Long> userID;
     MD5Encoder md5Encoder;
 
     @Autowired
     public Store(SimpleJdbcTemplate jdbcTemplate, @Qualifier("userID") ThreadLocal<Long> userID, MD5Encoder md5Encoder){
         this.jdbcTemplate = jdbcTemplate;
         this.userID = userID;
         this.md5Encoder = md5Encoder;
     }
 
     public Post addPost(String userID, String post, String postID, String authorID) {
         PostRowMapper postRowMapper = new PostRowMapper();
         if (postID == null)
             jdbcTemplate.update("INSERT INTO posts (user_id, post) VALUES (?,?)", userID, post);
         else
             try{
                 jdbcTemplate.update("INSERT INTO posts (user_id, post, rtwt_id, author_id) VALUES (?,?,?,?)", userID, post, postID, authorID);
             }
             catch (DuplicateKeyException e){
                 return null;
             }
 
         jdbcTemplate.update("UPDATE users SET num_tweets=num_tweets+1 where id=?", userID);
 
         return (Post) jdbcTemplate.queryForObject("SELECT * FROM posts WHERE user_id=" + userID +
                 " AND post=\""+post+"\" order by time desc limit 1", postRowMapper);
     }
 
     public Post rePost(String userID, String postID) {
         Post post = getPost(postID);
         return addPost(userID, post.getPost(), postID, Integer.toString(post.getUser_id()));
     }
 
 
     public List<Post> getPosts(String userID, String since_id, String count, String max_id){
         if (getUser(userID) == null)
             return null;
 
         PostRowMapper postRowMapper = new PostRowMapper();
         String query;
         if (count== null)
             count = "20";
         if (since_id == null && max_id == null)
             query = "SELECT * from posts where user_id=" +userID + " ORDER BY posts.time DESC LIMIT "+ count;
         else if (max_id == null)
             query = "SELECT * from posts where user_id=" +userID +" AND posts.id>"+since_id + " ORDER BY posts.time DESC LIMIT "+ count;
         else
             query = "SELECT * from posts where user_id=" +userID +" AND posts.id<"+max_id + " ORDER BY posts.time DESC LIMIT "+ count;
 
         List<Post> posts = (List< Post>) jdbcTemplate.query(query, postRowMapper);
 
         return posts;
 
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
             jdbcTemplate.update("INSERT INTO users (username, email, password) VALUES (?,?,?)",username, email, password);
             UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
             User user = (User)jdbcTemplate.queryForObject("SELECT * from users where username=\"" + username + "\"", userRowMapper);
             return user;
         }
         catch (DuplicateKeyException e){
             return null;
         }
     }
 
     public User updateUser(String userID, String username, String email, String name, String description, String old_password, String new_password) {
         User user = this.getUserByUserID(userID, old_password);
         if (user!=null)
             jdbcTemplate.update("UPDATE users SET username=?, email=?, password=?, name=?, description=?",username, email, new_password, name, description);
         return user;
     }
 
     public User updateUserPassword(String userID, String old_password, String new_password) {
         User user = this.getUserByUserID(userID, old_password);
         if (user!=null)
             jdbcTemplate.update("UPDATE users SET password=? where id=?",new_password, userID);
         return user;
     }
 
     public User updateUserAccount(String userID, String username, String email) {
         User user = this.getUser(userID);
         try{
             jdbcTemplate.update("UPDATE users SET username=?, email=? where id=?",username, email, userID);
             return user;
         }
         catch (DuplicateKeyException e){
             return null;
         }
     }
 
     public boolean updateUserProfile(String userID, String name, String description) {
         jdbcTemplate.update("UPDATE users SET name=?, description=? where id=?", name, description, userID);
         return true;
     }
 
     public User getUser(String userID){
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID, userRowMapper);
             return user;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
     public User getUser(String userID, String callerUserID){
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         FollowRowMapper followRowMapper = new FollowRowMapper();
         try{
 //            User user = (User) jdbcTemplate.queryForObject("select * from users INNER JOIN followers on users.id=followers.user_id where users.id=" + userID + " AND followers.follower="+callerUserID, userRowMapper);
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID, userRowMapper);
             try{
                 Boolean follow = (Boolean) jdbcTemplate.queryForObject("select * from followers where user_id="+userID +" AND follower="+callerUserID, followRowMapper);
                 user.setFollowed(follow);
             }
             catch (EmptyResultDataAccessException e){
                 user.setFollowed(false);
             }
             return user;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
     public User getUserByUserID(String userID, String password) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID + " and password=\""+ password + "\"", userRowMapper);
             return user;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
     public Hashtable<String, String> getUserByEmail(String email, String password) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             Hashtable<String, String> hs = new Hashtable<String, String>();
             System.out.println("select * from users where email=\"" + email + "\" and password=\""+ password + "\"");
             User user = (User) jdbcTemplate.queryForObject("select * from users where email=\"" + email + "\" and password=\""+ password + "\"", userRowMapper);
 
             String auth_key = db_gen_auth_key(Integer.toString(user.getId()));
             hs.put("userID", Integer.toString(user.getId()));
             hs.put("auth_key", auth_key);
             return hs;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
     public Hashtable<String, String> getUserByUsername(String username, String password) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             Hashtable<String, String> hs = new Hashtable<String, String>();
             System.out.println("select * from users where username=\"" + username + "\" and password=\""+ password + "\"");
             User user = (User) jdbcTemplate.queryForObject("select * from users where username=\"" + username + "\" and password=\""+ password + "\"", userRowMapper);
 
             String auth_key = db_gen_auth_key(Integer.toString(user.getId()));
             hs.put("userID", Integer.toString(user.getId()));
             hs.put("auth_key", auth_key);
             return hs;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
     public User getUserByAuthKey(String userID, String auth_key) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         try{
             System.out.println("authorisation key from user: " + auth_key);
             User user = (User) jdbcTemplate.queryForObject("select * from users where id=" + userID + " and auth_key=\""+ auth_key + "\"", userRowMapper);
             return user;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
     }
 
     private String db_gen_auth_key(String userID) {
         long current_time_value = new Date().getTime();
         Random random_number = new Random();
         String auth_key = md5Encoder.encodeString(Long.toString(current_time_value).concat(random_number.toString()));
         jdbcTemplate.update("UPDATE users SET auth_key=? where id=?",auth_key, userID);
         System.out.println("authorisation key generated by system: " + auth_key);
         return auth_key;
     }
 
     public void invalidateAuthKey(String userID) {
         jdbcTemplate.update("UPDATE users SET auth_key=? where id=?","", userID);
         System.out.println("authorisation key destroyed");
     }
 
     public List<User> getFollowers(String userID, String count, String max_id) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         count = (count == null) ? "20" : count;
         String query;
 
         if (max_id == null)
             query = "select * from users where id in (select follower from followers where user_id="+ userID +" AND unfollow_time > NOW()) ORDER BY id DESC LIMIT "+count;
         else
             query = "select * from users where id in (select follower from followers where user_id="+ userID +" AND unfollow_time > NOW() AND follower<" + max_id+" ) ORDER BY id DESC LIMIT "+count;
 
         List<User> followers = jdbcTemplate.query(query, userRowMapper);
         for(User u:followers){
             try{
                 FollowRowMapper followRowMapper = new FollowRowMapper();
                 Boolean follow = (Boolean) jdbcTemplate.queryForObject("select * from followers where user_id="+u.getId()+" AND follower="+userID, followRowMapper);
                 u.setFollowed(follow);
             }
             catch (EmptyResultDataAccessException e){
                 u.setFollowed(false);
             }
         }
         return followers;
     }
 
     public List<User> getFollowings(String userID, String count, String max_id) {
         UserRowMapper userRowMapper = new UserRowMapper(md5Encoder);
         count = (count == null) ? "20" : count;
         String query;
 
         if (max_id == null)
             query = "select * from users where id in (select user_id from followers where follower="+ userID +" AND unfollow_time > NOW()) ORDER BY id DESC LIMIT "+count;
         else
             query = "select * from users where id in (select user_id from followers where follower="+ userID +" AND unfollow_time > NOW() AND user_id<" + max_id+" ) ORDER BY id DESC LIMIT "+count;
 
         List<User> followings = jdbcTemplate.query(query, userRowMapper);
         for(User u:followings){
             u.setFollowed(true);
         }
         return followings;
     }
 
     public List<Post> getSubscribedPosts(String userID, String since_id, String count, String max_id) {
         PostRowMapper postRowMapper = new PostRowMapper();
         String query;
         if (count== null)
                 count = "20";
 
         if (since_id == null && max_id == null)
              query = "select posts.id, posts.user_id, posts.post, posts.time, posts.rtwt_id, posts.author_id from followers INNER JOIN posts ON followers.user_id=posts.user_id" +
                                                             " WHERE followers.follower=" + userID + " AND posts.time<followers.unfollow_time ORDER BY posts.time DESC LIMIT "+ count;
         else if (max_id == null)
             query = "select posts.id, posts.user_id, posts.post, posts.time, posts.rtwt_id, posts.author_id from followers INNER JOIN posts ON followers.user_id=posts.user_id" +
                     " WHERE followers.follower=" + userID + " AND posts.time<followers.unfollow_time AND posts.id >" + since_id +" ORDER BY posts.time DESC LIMIT "+ count;
         else
             query = "select posts.id, posts.user_id, posts.post, posts.time, posts.rtwt_id, posts.author_id from followers INNER JOIN posts ON followers.user_id=posts.user_id" +
                     " WHERE followers.follower=" + userID + " AND posts.time<followers.unfollow_time AND posts.id <" + max_id +" ORDER BY posts.time DESC LIMIT "+ count;
 
         System.out.println(query);
         List<Post> subscribedPosts = jdbcTemplate.query(query, postRowMapper);
         return subscribedPosts;
     }
 
     public Post getPost(String postID) {
         PostRowMapper postRowMapper = new PostRowMapper();
         try{
             Post post = (Post) jdbcTemplate.queryForObject("select * from posts where posts.id=" + postID, postRowMapper);
             return post;
         }
         catch (EmptyResultDataAccessException e){
             return null;
         }
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
 
 }
 
 class FollowRowMapper implements RowMapper {
     Date today = new Date();
     Timestamp timeStamp = new java.sql.Timestamp(today.getTime());
 
     @Override
     public Boolean mapRow(ResultSet resultSet, int i) throws SQLException {
         return (resultSet.getTimestamp("unfollow_time").after(timeStamp));
     }
 }
 
 class UserRowMapper implements RowMapper {
     MD5Encoder md5Encoder;
 
     public UserRowMapper(MD5Encoder md5Encoder){
         this.md5Encoder = md5Encoder;
     }
 
     @Override
     public User mapRow(ResultSet resultSet, int i) throws SQLException {
         User user = new User();
         user.setId(resultSet.getInt("id"));
         user.setUsername(resultSet.getString("username"));
         String email = resultSet.getString("email");
         user.setCreated_at(resultSet.getTimestamp("created_at"));
         user.setDescription(resultSet.getString("description"));
         user.setName(resultSet.getString("name"));
 
         String baseUrl = "http://www.gravatar.com/avatar/";
         user.setImage_url(baseUrl.concat(md5Encoder.encodeString(email)));
 
         user.setNum_tweets(resultSet.getString("num_tweets"));
         user.setNum_followers(resultSet.getString("num_followers"));
         user.setNum_followings(resultSet.getString("num_followings"));
 
         return user;
     }
 }
 
 class PostRowMapper implements RowMapper {
 
     @Override
     public Post mapRow(ResultSet resultSet, int i) throws SQLException {
         Post post = new Post(resultSet.getInt("id"), resultSet.getInt("user_id"), resultSet.getString("post"), resultSet.getTimestamp("time"));
         post.setAuthor_id(resultSet.getInt("author_id"));
         post.setRtwt_id(resultSet.getInt("rtwt_id"));
         return post;
     }
 }
