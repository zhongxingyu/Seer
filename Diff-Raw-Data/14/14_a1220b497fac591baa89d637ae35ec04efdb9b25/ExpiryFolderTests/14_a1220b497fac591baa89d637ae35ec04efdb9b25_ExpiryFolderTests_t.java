 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.common.collections.expiry;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.collections.expiry.ExpiryFolder;
 import org.xins.common.collections.expiry.ExpiryStrategy;
 
 /**
  * Tests for class <code>ExpiryFolder</code>.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class ExpiryFolderTests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(ExpiryFolderTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ExpiryFolderTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public ExpiryFolderTests(String name) {
       super(name);
    }
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void testExpiryFolder() throws Throwable {
       ExpiryStrategy stategy = new ExpiryStrategy(60, 15);
       ExpiryFolder folder = new ExpiryFolder("Test1", stategy, false, 10l);
       assertEquals("Incorrect name.", "Test1", folder.getName());
       assertEquals("Incorrect strategy.", stategy, folder.getStrategy());
       assertNull(folder.get("hello"));
       assertNull(folder.find("hello"));
       try {
          folder.get(null);
          fail("Invalid argument accepted.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
       try {
          folder.find(null);
          fail("Invalid argument accepted.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
       try {
          folder.put("hello", null);
          fail("Invalid argument accepted.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
       try {
          folder.put(null, "hello");
          fail("Invalid argument accepted.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       folder.put("hello", "world");
      assertEquals("Got incorrect value.", "world", folder.get("hello"));
       assertEquals("Incorrect value found.", "world", folder.find("hello"));
 
       Thread.sleep(30);
      assertEquals("Got Incorrect value.", "world", folder.get("hello"));
 
      Thread.sleep(50);
       assertEquals("Incorrect value found.", "world", folder.find("hello"));
 
      Thread.sleep(50);
       assertNull("Incorrect value found.", folder.find("hello"));
      assertNull("Got incorrect value.", folder.get("hello"));
    }
 
    public void testStategy() throws Throwable {
       ExpiryStrategy strategy = new ExpiryStrategy(60, 15);
       assertEquals(15, strategy.getPrecision());
       assertEquals(60, strategy.getTimeOut());
       assertEquals(4, strategy.getSlotCount());
    }
 
    public void testRemove() throws Throwable {
       ExpiryStrategy stategy = new ExpiryStrategy(60, 15);
       ExpiryFolder folder = new ExpiryFolder("Test1", stategy, false, 10l);
       folder.put("hello", "world");
       try {
          Thread.sleep(20);
       } catch (Exception ex) {
          fail("Sleeping thread interrupted.");
       }
       assertEquals("Incorrect value found.", "world", folder.get("hello"));
       assertEquals("world", folder.remove("hello"));
       try {
          Thread.sleep(20);
       } catch (Exception ex) {
          fail("Sleeping thread interrupted.");
       }
       assertNull("Incorrect value found.", folder.find("hello"));
       assertNull("Incorrect value found.", folder.get("hello"));
 
       // remove a non existing object
       assertNull(folder.remove("hello2"));
    }
 }
