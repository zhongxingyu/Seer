 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.xins.server.AccessRuleList;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 
 /**
  * Tests for class <code>AccessRuleList</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class AccessRuleListTests extends TestCase {
 
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
       return new TestSuite(AccessRuleListTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AccessRuleListTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public AccessRuleListTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Performs setup for the tests.
     */
    protected void setUp() {
       // empty
    }
 
    public void testParseAccessRuleList() throws Throwable {
 
       try {
          AccessRuleList.parseAccessRuleList(null, 0);
         fail("AccessRule.parseAccessRuleList(null,0) should throw an IllegalArgumentException.");
         return;
      } catch (IllegalArgumentException exception) {
         // as expected
      }
      try {
         AccessRuleList.parseAccessRuleList(null);
          fail("AccessRule.parseAccessRuleList(null) should throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
      AccessRuleList arl = AccessRuleList.parseAccessRuleList("");
      assertNotNull(arl);
      assertEquals(0, arl.getRuleCount());

      arl = AccessRuleList.parseAccessRuleList("", 1);
       assertNotNull(arl);
       assertEquals(0, arl.getRuleCount());
 
      arl = AccessRuleList.parseAccessRuleList(" \t\n\r", 1);
       assertNotNull(arl);
       assertEquals(0, arl.getRuleCount());
 
      arl = AccessRuleList.parseAccessRuleList(" \r\nallow 194.134.168.213/32 *\t");
       assertNotNull(arl);
       assertEquals(1, arl.getRuleCount());
       boolean allow = arl.allow("194.134.168.213", "_GetVersion");
       if (! allow) {
          fail("Expected AccessRuleList(" + arl + ") to allow 194.134.168.213 to access function \"_GetVersion\".");
          return;
       }
 
      arl = AccessRuleList.parseAccessRuleList(" \r\nallow 194.134.168.213/32 *\t", 1);
      assertNotNull(arl);
      assertEquals(1, arl.getRuleCount());
      allow = arl.allow("194.134.168.213", "_GetVersion");
      if (! allow) {
         fail("Expected AccessRuleList(" + arl + ") to allow 194.134.168.213 to access function \"_GetVersion\".");
         return;
      }

      arl = AccessRuleList.parseAccessRuleList(" \r\nallow 194.134.168.213/32 *\t;\ndeny 1.2.3.4/0 * ");
       assertNotNull(arl);
       assertEquals(2, arl.getRuleCount());
       // TODO: More tests
    }
 }
