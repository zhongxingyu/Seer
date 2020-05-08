 /**
  * @author Robin Hammarng
  */
 
 package com.chalmers.feedlr.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.test.AndroidTestCase;
 
 public class FeedTest extends AndroidTestCase {
 	
 	private Feed feed;
 	private List<User> twitterUsers;
 	private List<User> fbUsers;
 	private User user1;
 	private User user2;
 	private User user3;
 	
 	/**
 	 * This is called before each test.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		user1 = new User();
 		user2 = new User();
 		user3 = new User();
 		user1.setUserName("User 1");
 		user2.setUserName("User 2");
 		user3.setUserName("User 3");
 		
 		twitterUsers = new ArrayList<User>();
 		twitterUsers.add(user1);
 		twitterUsers.add(user2);
 		
 		fbUsers = new ArrayList<User>();
 		fbUsers.add(user1);
 		fbUsers.add(user3);
 		
 		feed = new Feed();
 		feed.setFacebookUsers(fbUsers);
 		feed.setTwitterUsers(twitterUsers);
 	}
 
 	/**
 	 * Tests the constructor, if the object is created properly. 
 	 * The various objects should have been created with suitable values.
 	 */
 	public void testPreconditions(){
 		assertTrue(feed != null);
 		assertTrue(feed.getFacebookUsers() == fbUsers);
 		assertTrue(feed.getTwitterUsers() == twitterUsers);
		assertTrue(feed.getTitle() == "Yeah buddy"); 
 	}
 
 	/**
 	 * This is called after each test, to insure that each test is ran
 	 * individually.
 	 */
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();
 		feed = null;
 		user1 = null;
 		user2 = null;
 		user3 = null;
 		fbUsers = null;
 		twitterUsers = null;
 		feed = null;
 	}
 
 }
