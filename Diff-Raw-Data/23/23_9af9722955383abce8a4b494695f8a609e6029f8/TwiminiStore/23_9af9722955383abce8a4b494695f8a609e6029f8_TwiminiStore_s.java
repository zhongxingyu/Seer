 package sample.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 import sample.model.Post;
 import sample.model.User;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dushyant
  * Date: 18/7/12
  * Time: 1:46 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class TwiminiStore {
     SimpleJdbcTemplate jdbcTemplate;
     private final ThreadLocal<Long> userID;
 
     @Autowired
     public TwiminiStore(SimpleJdbcTemplate jdbcTemplate, @Qualifier("userID") ThreadLocal<Long> userID){
         this.jdbcTemplate = jdbcTemplate;
         this.userID = userID;
     }
 
    public void addPost( String post) {
         jdbcTemplate.update("INSERT INTO posts (user_id, post) VALUES (?,?)", userID.get(), post);
     }
 
     public List<Post> getPosts(){
         PostRowMapper postRowMapper = new PostRowMapper();
         List<Post> posts = (List< Post>) jdbcTemplate.query("SELECT * from posts where user_id=" + userID.get(),
                 postRowMapper);
 
         if (posts==null)
             posts = new ArrayList< Post>();
         return posts;
 
    }
     //TODO: return status
     public void addFollower(int following) {
         jdbcTemplate.update("INSERT INTO followers (user_id, follower) VALUES (?,?)", following, userID.get());
     }
 
     public void addUser(String name, String email, String password) {
         jdbcTemplate.update("INSERT INTO users (username, email, password) VALUES (?,?,?)", name, email, password);
     }
 
     public User getUser(String email) {
         UserRowMapper userRowMapper = new UserRowMapper();
 
         User user = (User) jdbcTemplate.queryForObject("select * from users where email= \"" + email + "\"", userRowMapper);
         return user;  //To change body of created methods use File | Settings | File Templates.
     }
 
     public List<User> getFollowers() {
         UserRowMapper userRowMapper = new UserRowMapper();
         List<User> followers = jdbcTemplate.query("select * from users where id in (select follower from followers where user_id="+ userID.get() +")", userRowMapper);
         return followers;
     }
 
     public List<User> getFollowings() {
         UserRowMapper userRowMapper = new UserRowMapper();
         List<User> followings = jdbcTemplate.query("select * from users where id in (select user_id from followers where follower="+ userID.get() +")", userRowMapper);
         return followings;
     }
 
     public List<Post> getSubscribedPosts() {
         PostRowMapper postRowMapper = new PostRowMapper();
         List<Post> subscribedPosts = jdbcTemplate.query("select * from posts where user_id in (select user_id from followers where follower=" + userID.get() +")", postRowMapper);
         return subscribedPosts;
     }
 }
 
 class UserRowMapper implements RowMapper {
 
     @Override
     public User mapRow(ResultSet resultSet, int i) throws SQLException {
         User user = new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("email"), resultSet.getString("password"));
         return user;
     }
 }
 
 class PostRowMapper implements RowMapper {
 
     @Override
     public Post mapRow(ResultSet resultSet, int i) throws SQLException {
         Post post = new Post(resultSet.getInt("id"), resultSet.getInt("user_id"), resultSet.getString("post"), resultSet.getTimestamp("time"));
         return post;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
