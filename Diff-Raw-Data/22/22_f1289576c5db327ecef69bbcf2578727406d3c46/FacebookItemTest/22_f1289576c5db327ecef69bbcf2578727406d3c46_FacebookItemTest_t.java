 /**
  * @author Robin Hammarng
  */
 
 package com.chalmers.feedlr.model;
 
 import android.test.AndroidTestCase;
 
 public class FacebookItemTest extends AndroidTestCase {
 
 	private FacebookItem FbI;
	private User user;
 	/**
 	 * This is called before each test.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
		
		user = new User();
		user.setUserName("Olle");
		
 		FbI = new FacebookItem();
 		FbI.setText("Hej David!");
 		FbI.setId("1337");
		FbI.setUser(user);
		
 	}
 
 	/**
 	 * Tests the constructor, if the object is created properly. The various
 	 * objects should have been created with suitable values.
 	 */
 	public void testPreconditions() {
 		assertTrue(FbI != null);
		assertTrue(FbI.getUser() == user);
 		assertTrue(FbI.getText().equalsIgnoreCase("Hej David!"));
 		assertTrue(FbI.getId().equalsIgnoreCase("1337"));
 	}
 
 	/**
 	 * This is called after each test, to insure that each test is ran
 	 * individually.
 	 */
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();
		user = null;
 		FbI = null;
 	}
 
 }
