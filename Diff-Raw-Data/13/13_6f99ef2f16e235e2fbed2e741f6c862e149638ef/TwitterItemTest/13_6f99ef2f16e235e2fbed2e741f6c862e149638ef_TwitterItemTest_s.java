 /**
  * @author Robin Hammarng
  */
 
 package com.chalmers.feedlr.model;
 
 import android.test.AndroidTestCase;
 
 public class TwitterItemTest extends AndroidTestCase {
 
 	private TwitterItem ti;
 	private User user;
 
 	/**
 	 * This is called before each test.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		user = new User();
 		user.setUserName("Olle");
 		ti = new TwitterItem();
 		ti.setText("Hej David!");
 		ti.setUser(user);
 		ti.setId("1337");
 	}
 
 	/**
 	 * Tests the constructor, if the object is created properly. The various
 	 * objects should have been created with suitable values.
 	 */
 	public void testPreconditions() {
 		assertTrue(ti != null);
 		assertTrue(ti.getUser() == user);
		assertTrue(ti.getText() == "Hej David!");
		assertTrue(ti.getId() == "1337");
 	}
 
 	/**
 	 * This is called after each test, to insure that each test is ran
 	 * individually.
 	 */
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();
 		user = null;
 		ti = null;
 	}
 
 }
