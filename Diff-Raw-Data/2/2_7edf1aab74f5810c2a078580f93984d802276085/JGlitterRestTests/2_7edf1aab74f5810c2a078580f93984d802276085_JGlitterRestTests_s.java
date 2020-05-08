 /*
  * Project Horizon
  *
  * (c) 2012 VMware, Inc. All rights reserved.
  * VMware Confidential.
  */
 package com.jglitter.test;
 
 import com.jglitter.domain.Tweet;
 import com.jglitter.domain.Tweets;
 import com.jglitter.domain.User;
 import com.jglitter.domain.Users;
 import liquibase.precondition.Precondition;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.RestTemplate;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Collection;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotNull;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 @Test
 public class JGlitterRestTests extends AbstractTests {
 
     @Autowired
     private RestTemplate restTemplate;
     private User follower;
     private User userToFollow;
 
     @BeforeMethod
     void setup() {
         follower = createUser("gavin@vmware.com", "Gavin Gray");
         userToFollow = createUser("brad@vmware.com", "Brad");
     }
 
     @AfterMethod(alwaysRun = true)
     void teardown() {
         deleteUser(follower.getId());
         deleteUser(userToFollow.getId());
     }
 
     @Test
     void canCreateAUser() {
         Users allUsers = restTemplate.getForEntity(wsRoot() + "/user", Users.class).getBody();
         assertTrue(allUsers.contains(follower), "All users didn't include newly added user.");
     }
 
     private void deleteUser(String id) {
         restTemplate.delete(wsRoot() + "/user/" + id);
     }
 
     private User createUser(String email, String username) {
         User aUser = restTemplate.postForEntity(wsRoot() + "/user", new User(email, username), User.class).getBody();
         assertNotNull(aUser, "Create user failed " + username);
         return aUser;
     }
 
     @Test
     void userCanAuthorATweet() {
        User author = restTemplate.postForEntity(wsRoot() + "/user", new User("auth@or.com", "JohnDoe"), User.class).getBody();
         Tweet tweet = restTemplate.postForEntity(wsRoot() + "/tweet", new Tweet(author, "This is my first tweet!"), Tweet.class).getBody();
         Tweets tweets = restTemplate.getForEntity(wsRoot() + "/user/" + author.getId() + "/tweets", Tweets.class).getBody();
         assertTrue(tweets.contains(tweet), "All tweets by the author includes the new tweet.");
     }
 
     @Test
     void unknownUserCannotAuthorATweet() {
         User unknownAuthor = new User("sneaky@bastard.com", "sneaky");
         try {
             restTemplate.postForEntity(wsRoot() + "/tweet", new Tweet(unknownAuthor, "This is my first tweet!"), Tweet.class).getBody();
             fail("Should have failed posting a tweet by an unknown author.");
         } catch (HttpClientErrorException exception) {
             assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "Error code wasn't NOT_FOUND");
         }
     }
 
     @Test
     void canFollowAnotherUser() {
         followUser(follower, userToFollow);
 
         Users followees = getFollowees(follower);
         assertEquals(1, followees.getUsers().size());
         assertTrue(followees.contains(userToFollow), "Expected followee not found");
 
 
     }
 
     private Users getFollowees(User aUser) {
         return restTemplate.getForEntity(wsRoot() + "/followees/" + aUser.getId(), Users.class).getBody();
     }
 
     private void followUser(User aUser, User userToFollow) {
         restTemplate.postForEntity(wsRoot() + "/followers/" + aUser.getId() + "/" + userToFollow.getId(), null, null);
     }
 
     private String wsRoot() {
         // putting this here now because I removed the URL helper class
         return "http://localhost:8080/jglitter/ws";
     }
 }
