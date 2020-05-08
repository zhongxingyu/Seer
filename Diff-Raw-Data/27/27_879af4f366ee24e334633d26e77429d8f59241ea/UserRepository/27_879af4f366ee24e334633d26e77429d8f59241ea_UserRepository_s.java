 package com.teamsierra.csc191.api.repository;
 
 import java.util.List;
 
 import com.teamsierra.csc191.api.model.GenericModel.UserType;
 import com.teamsierra.csc191.api.model.User;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.stereotype.Repository;
 
 import static org.springframework.data.mongodb.core.query.Criteria.where;
 import static org.springframework.data.mongodb.core.query.Query.query;
 
 /**
  * User: scott
  * Date: 9/9/13
  * Time: 3:18 PM
  */
 @Repository
 public class UserRepository {
 
     private static final Log L = LogFactory.getLog(UserRepository.class);
 
     private MongoTemplate mongoTemplate;
 
     @Autowired
     public UserRepository(MongoTemplate mongoTemplate) {
         this.mongoTemplate = mongoTemplate;
 
         if(!mongoTemplate.collectionExists(User.class)) {
             L.debug("Creating the users collection");
             mongoTemplate.createCollection(User.class);
         }
     }
 
 
     //   /$$$$$$ /$$   /$$  /$$$$$$  /$$$$$$$$ /$$$$$$$  /$$$$$$$$
     //  |_  $$_/| $$$ | $$ /$$__  $$| $$_____/| $$__  $$|__  $$__/
     //    | $$  | $$$$| $$| $$  \__/| $$      | $$  \ $$   | $$
     //    | $$  | $$ $$ $$|  $$$$$$ | $$$$$   | $$$$$$$/   | $$
     //    | $$  | $$  $$$$ \____  $$| $$__/   | $$__  $$   | $$
     //    | $$  | $$\  $$$ /$$  \ $$| $$      | $$  \ $$   | $$
     //   /$$$$$$| $$ \  $$|  $$$$$$/| $$$$$$$$| $$  | $$   | $$
     //  |______/|__/  \__/ \______/ |________/|__/  |__/   |__/
     public void insert(User user) {
         L.info("Inserting new user: "+ user);
         mongoTemplate.insert(user);
     }
 
 
     //   /$$$$$$$$ /$$$$$$ /$$   /$$ /$$$$$$$
     //  | $$_____/|_  $$_/| $$$ | $$| $$__  $$
     //  | $$        | $$  | $$$$| $$| $$  \ $$
     //  | $$$$$     | $$  | $$ $$ $$| $$  | $$
     //  | $$__/     | $$  | $$  $$$$| $$  | $$
     //  | $$        | $$  | $$\  $$$| $$  | $$
     //  | $$       /$$$$$$| $$ \  $$| $$$$$$$/
     //  |__/      |______/|__/  \__/|_______/
     public User findByEmail(String email) {
         L.info("Finding a user by email: " + email);
         return mongoTemplate.findOne(query(where("email").is(email)), User.class);
     }
     public User findById(String id) {
         L.info("Finding a user by id: "+ id);
         return mongoTemplate.findOne(query(where("_id").is(id)), User.class);
     }
     public User findByOAuthId(String id) {
         L.info("Finding a user by OAuth id: "+ id);
         return mongoTemplate.findOne(query(where("oauthId").is(id)), User.class);
     }
     public User findByToken(String token) {
         L.info("Finding a user by token: "+ token);
         return mongoTemplate.findOne(query(where("token").is(token)), User.class);
     }
     public List<User> findAllActive(){
     	L.info("Finding all active users: ");
     	return mongoTemplate.find(query(where("active").is(true)), User.class);
     }
     public List<User> findAll(){
     	L.info("Finding all users: ");
     	return mongoTemplate.findAll(User.class);
     }
     public List<User> findAllByGroup(UserType type){
     	L.info("Finding all users by group: " + type);
    	return mongoTemplate.find(query(where("type").is(type)).addCriteria(where("active").is(true)), User.class);
     }
 
 
     //    /$$$$$$   /$$$$$$  /$$    /$$ /$$$$$$$$
     //   /$$__  $$ /$$__  $$| $$   | $$| $$_____/
     //  | $$  \__/| $$  \ $$| $$   | $$| $$
     //  |  $$$$$$ | $$$$$$$$|  $$ / $$/| $$$$$
     //   \____  $$| $$__  $$ \  $$ $$/ | $$__/
     //   /$$  \ $$| $$  | $$  \  $$$/  | $$
     //  |  $$$$$$/| $$  | $$   \  $/   | $$$$$$$$
     //   \______/ |__/  |__/    \_/    |________/
     public void save(User user) {
         L.info("Saving the following user object: " + user);
         mongoTemplate.save(user);
     }
 }
