 /**
  * @author Robin Hammarng
  */
 
 package com.chalmers.feedlr.model;
 
import com.chalmers.feedlr.model.FacebookItem.From;

 import android.test.AndroidTestCase;
 
 public class FacebookItemTest extends AndroidTestCase {
 
 	private FacebookItem FbI;
	private From from;
 	/**
 	 * This is called before each test.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();

 		FbI = new FacebookItem();
 		FbI.setText("Hej David!");
 		FbI.setId("1337");
		from = FbI.getFrom();
		from.setUserName("Olle");
 	}
 
 	/**
 	 * Tests the constructor, if the object is created properly. The various
 	 * objects should have been created with suitable values.
 	 */
 	public void testPreconditions() {
 		assertTrue(FbI != null);
		assertTrue(FbI.getUser() == from);
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
		from = null;
 		FbI = null;
 	}
 
 }
