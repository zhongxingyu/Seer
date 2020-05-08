 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  *
  */
 
 package no.feide.moria.controller;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashSet;
 
 /**
  * Test suite for the MoriaController class.
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @see MoriaController
  * @version $Revision$
  */
 public final class MoriaControllerTest extends TestCase {
 
     private String validPrefix, validPostfix, validPrincipal, validUsername, validPassword;
     private String[] validAttrs;
     private HashMap expectedAttrs;
 
     /**
      * Initiate all tests.
      *
      * @return Junit test suite.
      */
     public static Test suite() {
         return new TestSuite(MoriaControllerTest.class);
     }
 
 
     public void setUp() {
         /* Property needed by the RandomId class */
         if (System.getProperty("no.feide.moria.store.nodeid") == null)
             System.setProperty("no.feide.moria.store.nodeid", "no1");
 
         if (System.getProperty("no.feide.moria.configuration.base") == null)
             System.setProperty("no.feide.moria.configuration.base",
                                System.getProperty("no.feide.moria.configuration.test.dir") +
                                "/moria-base-valid.properties");
 
         validPrefix = "http://moria.sf.net/";
         validPostfix = "&foo=bar";
         validPrincipal = "test";
         validAttrs = new String[]{"attr1", "attr2"};
         validUsername = "user@some.realm";
         validPassword = "password";
 
         expectedAttrs = new HashMap();
         expectedAttrs.put("attr1", new String[]{"value1"});
         expectedAttrs.put("attr2", new String[]{"value2"});
 
     }
 
     /**
      * Test the init() method.
      *
      * @see MoriaController#init()
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public static void testInit() throws MoriaControllerException {
         MoriaController.init();
     }
 
     /**
      * Verify that the controller initialization checks works and then initates the conftroller.
      *
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     private static void controllerInitialization() throws MoriaControllerException {
         /* Controller not initialized */
         MoriaController.stop();
         try {
             MoriaController.getServiceProperties("foobar");
             fail("InoperableStateException should be raised, controller not initialized.");
         } catch (InoperableStateException success) {
         } catch (UnknownTicketException e) {
             /* Should never get here */
         }
 
         MoriaController.init();
     }
 
     /**
      * Compares to Maps. The maps must have equal elements or the test will fail.
      *
      * @param expected the expected result
      * @param actual the actual result
      */
     private static void validateMaps(final Map expected, final Map actual) {
         assertEquals("Expected and actual attributes length differs", expected.size(), actual.size());
 
         final Iterator itMap = expected.keySet().iterator();
         while (itMap.hasNext()) {
             final String key = (String) itMap.next();
             final String[] expectedArr = (String[]) expected.get(key);
             final String[] actualArr = (String[]) actual.get(key);
 
             final HashSet expectedValues = new HashSet();
             for (int i = 0; i < expectedArr.length; i++) {
                 expectedValues.add(expectedArr[i]);
             }
 
             final HashSet actualValues = new HashSet();
             for (int i = 0; i < actualArr.length; i++) {
                 actualValues.add(actualArr[i]);
             }
 
             final Iterator itVals = expectedValues.iterator();
             while (itVals.hasNext()) {
                 final String value = (String) itVals.next();
                 assertTrue("Expected value does not exist: '"+value+"'", actualValues.contains(value));
             }
 
         }
     }
 
     /**
      * Thest the initiateMoriaAuthentication method.
      *
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      * @see MoriaController#initiateAuthentication(java.lang.String[], java.lang.String, java.lang.String, boolean,
             *      java.lang.String)
      */
     public void testInitiateMoriaAuthentication() throws MoriaControllerException {
 
         controllerInitialization();
 
         /* Illegal paramenters */
         try {
             MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, null);
             fail("IllegalInputException should be raised, principal is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, "");
             fail("IllegalInputException should be raised, principal is an empty string");
         } catch (IllegalInputException success) {
         }
 
         try {
             MoriaController.initiateAuthentication(null, validPrefix, validPostfix, false, validPrincipal);
             fail("IllegalInputException should be raised, attributes is null");
         } catch (IllegalInputException success) {
         }
 
         try {
             MoriaController.initiateAuthentication(validAttrs, null, validPostfix, false, validPrincipal);
             fail("IllegalInputException should be raised, prefix is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.initiateAuthentication(validAttrs, "", validPostfix, false, validPrincipal);
             fail("IllegalInputException should be raised, prefix is an empty string");
         } catch (IllegalInputException success) {
         }
 
         try {
             MoriaController.initiateAuthentication(validAttrs, validPrefix, null, false, validPrincipal);
             fail("IllegalInputException should be raised, postfix is null");
         } catch (IllegalInputException success) {
         }
 
         /* Illegal attribute request */
         final String[] attrs = new String[]{"illegal1", "illegal2"};
         try {
             MoriaController.initiateAuthentication(attrs, validPrefix, validPostfix, false, validPrincipal);
             fail("AuthorizationException should be raised, illegal attributes request.");
         } catch (AuthorizationException success) {
         }
 
         /* Illegal URL */
         try {
             MoriaController.initiateAuthentication(validAttrs, "foobar", validPostfix, false, validPrincipal);
             fail("IllegalInputException should be raised, illegal URL (no protocol).");
         } catch (IllegalInputException success) {
         }
 
         /* Legal use */
         String ticket = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         assertNotNull("Login ticket should be valid", MoriaController.getServiceProperties(ticket));
 
         ticket =
         MoriaController.initiateAuthentication(new String[]{}, validPrefix, validPostfix, false, validPrincipal);
         assertNotNull("Login ticket should be valid", MoriaController.getServiceProperties(ticket));
     }
 
     /**
      * Test the attemptLogin method.
      * @see MoriaController#attemptLogin(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testAttemptLogin() throws MoriaControllerException {
         final String validSSOTicketId = "1234";
         final String validLoginTicketId = "4321";
 
         /* Check controller initialization */
         controllerInitialization();
 
         /* Illegal arguments */
         try {
             MoriaController.attemptLogin(null, validSSOTicketId, validUsername, validPassword);
             fail("IllegalInputException should be raised, loginTicketId is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptLogin("", validSSOTicketId, validUsername, validPassword);
             fail("IllegalInputException should be raised, loginTicketId is an empty string");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptLogin(validLoginTicketId, validSSOTicketId, null, validPassword);
             fail("IllegalInputException should be raised, username is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptLogin(validLoginTicketId, validSSOTicketId, "", validPassword);
             fail("IllegalInputException should be raised, username is an empty string");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptLogin(validLoginTicketId, validSSOTicketId, validUsername, null);
             fail("IllegalInputException should be raised, username is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptLogin(validLoginTicketId, validSSOTicketId, validUsername, "");
             fail("IllegalInputException should be raised, username is an empty string");
         } catch (IllegalInputException success) {
         }
 
         /* SSO ticket can be empty */
         String loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                       validPrincipal);
         MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         MoriaController.attemptLogin(loginTicketId, "", validUsername, validPassword);
 
         /* Non-existing login ticket */
         try {
             MoriaController.attemptLogin("doesNotExist", validSSOTicketId, validUsername, validPassword);
             fail("UnknownTicketException should be raised, non-existing ticket");
         } catch (UnknownTicketException success) {
         }
 
         /* Non-existing SSO ticket (allowed) */
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         MoriaController.attemptLogin(loginTicketId, "doesNotExist", validUsername, validPassword);
 
         /* Removal of existing SSO ticket */
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         Map tickets = MoriaController.attemptLogin(loginTicketId, "doesNotExist", validUsername, validPassword);
         final String ssoTicketId = (String) tickets.get(MoriaController.SSO_TICKET);
 
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         MoriaController.attemptLogin(loginTicketId, ssoTicketId, validUsername, validPassword);
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         try {
             MoriaController.attemptSingleSignOn(loginTicketId, ssoTicketId);
             fail("UnknownTicketException should be raised, SSO ticket should be removed from the store");
         } catch (UnknownTicketException success) {
         }
 
         /* Use of same login ticket twice, authentication failure = OK */
         loginTicketId =
         MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
         try {
             MoriaController.attemptLogin(loginTicketId, null, validUsername, "wrongPassword");
         } catch (AuthenticationException success) {
         }
         MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
 
         /* Use of same login ticket twice when authentication was OK first time = ERROR */
         loginTicketId =
         MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
         MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         try {
             MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         } catch (UnknownTicketException success) {
         }
 
         /* Wrong username */
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         try {
             MoriaController.attemptLogin(loginTicketId, null, "wrong", validPassword);
             fail("AuthenticationFailedException should be raised, wrong username");
         } catch (AuthenticationException success) {
         }
 
         /* Wrong password */
         try {
             MoriaController.attemptLogin(loginTicketId, null, validUsername, "wrong");
             fail("AuthenticationFailedException should be raised, wrong username");
         } catch (AuthenticationException success) {
         }
 
         /* Normal use */
         loginTicketId =
         MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
         tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         final Map actualAttrs = MoriaController.getUserAttributes((String) tickets.get(MoriaController.SERVICE_TICKET),
                                                             validPrincipal);
 
         validateMaps(expectedAttrs, actualAttrs);
     }
 
     /**
      * Test the attemptSingleSignOn method.
      *
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      * @see MoriaController#attemptSingleSignOn(java.lang.String, java.lang.String)
      */
     public void testAttemptSingleSignOn() throws MoriaControllerException {
         /* Check controller initialization */
         controllerInitialization();
 
         /* Invalid arguments */
         try {
             MoriaController.attemptSingleSignOn(null, "foo");
             fail("IllegalInputException should be raised, loginTicketId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptSingleSignOn("", "foo");
             fail("IllegalInputException should be raised, loginTicketId is an empty string.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptSingleSignOn("foo", null);
             fail("IllegalInputException should be raised, ssoTicketId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.attemptSingleSignOn("foo", "");
             fail("IllegalInputException should be raised, ssoTicketId is an empty string.");
         } catch (IllegalInputException success) {
         }
 
 
         /* Invalid loginTicket */
         try {
             MoriaController.attemptSingleSignOn("doesNotExist", "foo");
             fail("UnknownTicketException should be raised, non-existing loginTicketId");
         } catch (UnknownTicketException success) {
         }
 
         /* Invalid ssoTicket */
         String loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                       validPrincipal);
         try {
             MoriaController.attemptSingleSignOn(loginTicketId, "doesNotExist");
             fail("UnknownTicketException should be raised, non-existing ssoTicketId");
         } catch (UnknownTicketException success) {
         }
 
         /* Force authentication */
           loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                         validPrincipal);
         Map tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         String ssoTicketId = (String) tickets.get(MoriaController.SSO_TICKET);
         String newLoginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, true,
                                                                          validPrincipal);
 
           try {
               MoriaController.attemptSingleSignOn(newLoginTicketId, ssoTicketId);
               fail("UnknownTicketException should be raised, authentication attempt requires interactive authentication.");
           } catch (UnknownTicketException success) {
           }
 
         /* Normal use */
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                         validPrincipal);
         tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         ssoTicketId = (String) tickets.get(MoriaController.SSO_TICKET);
 
         newLoginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                          validPrincipal);
         final String serviceTicketId = MoriaController.attemptSingleSignOn(newLoginTicketId, ssoTicketId);
         final Map actualAttrs = MoriaController.getUserAttributes(serviceTicketId, validPrincipal);
 
         validateMaps(expectedAttrs, actualAttrs);
     }
 
     /**
      * Test the getUserAttributes method.
      *
      * @see MoriaController#getUserAttributes(java.lang.String, java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testGetUserAttributes() throws MoriaControllerException {
         /* Check controller initialization */
         controllerInitialization();
 
         /* Illegal arguments */
         try {
             MoriaController.getUserAttributes(null, validPrincipal);
             fail("IllegalInputException should be raised, serviceTicketId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getUserAttributes("", validPrincipal);
             fail("IllegalInputException should be raised, serviceTicketId is an empty string.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getUserAttributes("foo", null);
             fail("IllegalInputException should be raised, servicePrincipal is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getUserAttributes("foo", "");
             fail("IllegalInputException should be raised, servicePrincipal is an empty string.");
         } catch (IllegalInputException success) {
         }
 
         /* Invalid service ticket */
         try {
             MoriaController.getUserAttributes("doesNotExist", validPrincipal);
             fail("UnknownTicketException should be raised, invalid serviceTicketId");
         } catch (UnknownTicketException success) {
         }
 
         /* Authorization */
         final String loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                       validPrincipal);
         final Map tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         final String serviceTicketId = (String) tickets.get(MoriaController.SERVICE_TICKET);
         try {
             MoriaController.getUserAttributes(serviceTicketId, "invalidPrincipal");
            fail("UnknownTicketException should be raised, wrong principal.");
        } catch (UnknownTicketException success) {
         }
 
         /* Content */
         final Map actualAttrs = MoriaController.getUserAttributes(serviceTicketId, validPrincipal);
 
         validateMaps(expectedAttrs, actualAttrs);
 
         /* Removal of authentication attempt */
         try {
             MoriaController.getUserAttributes(serviceTicketId, validPrincipal);
             fail("UnknownTicketException should be raised, tried to get attributes again.");
         } catch (UnknownTicketException success) {
         }
 
     }
 
     /**
      * Test the isLegalURL method.
      *
      * @see MoriaController#isLegalURL(java.lang.String)
      */
     public static void testisLegalURL() {
         // TODO: Test more illegal URL constructs
 
         /* Illegal parameters */
         try {
             MoriaController.isLegalURL(null);
             fail("IllegalArgumentException should be raised, null value.");
         } catch (IllegalArgumentException success) {
         }
         try {
             MoriaController.isLegalURL("");
             fail("IllegalArgumentException should be raised, empty string.");
         } catch (IllegalArgumentException success) {
         }
 
         /* Illegal URL content */
         assertFalse("URL should be rejected (no protocol)", MoriaController.isLegalURL("foobar"));
         assertFalse("URL should be rejected (newline)", MoriaController.isLegalURL("http://moria.sf.net\n"));
         assertFalse("URL should be rejected (wrong protocol)", MoriaController.isLegalURL("ftp://foo.bar.com"));
 
         /* Legal URL content */
         assertTrue("URL should be accepted", MoriaController.isLegalURL("http://moria.sf.net/"));
         assertTrue("URL should be accepted",
                    MoriaController.isLegalURL("http://moria.sf.net/index.html?foo=bar&bar=foo"));
     }
 
     /**
      * Test getServiceProperties method.
      *
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      * @see MoriaController#getServiceProperties(java.lang.String)
      */
     public void testGetServiceProperties() throws MoriaControllerException {
         controllerInitialization();
         /* Invalid arguments */
         try {
             MoriaController.getServiceProperties(null);
             fail("IllegalInputException should be raised, null value");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getServiceProperties("");
             fail("IllegalInputException should be raised, empty string");
         } catch (IllegalInputException success) {
         }
 
         final String ticket = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         //assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));
         final HashMap properties = MoriaController.getServiceProperties(ticket);
         assertEquals("Principal differs", validPrincipal, properties.get("name"));
     }
 
     /**
      * Tests the getSecLevel method.
      *
      * @see MoriaController#getSecLevel(java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testGetSecLevel() throws MoriaControllerException {
         controllerInitialization();
 
         /* Invalid arguments */
         try {
             MoriaController.getSecLevel(null);
             fail("IllegalArgumentException should be raised, null value");
         } catch (IllegalArgumentException success) {
         }
         try {
             MoriaController.getSecLevel("");
             fail("IllegalArgumentException should be raised, empty string");
         } catch (IllegalArgumentException success) {
         }
 
         /* Invalid ticket */
         try {
             MoriaController.getSecLevel("doesNotExist");
             fail("InvalidTicketException should be raised");
         } catch (UnknownTicketException success) {
         }
 
         /* Seclevel 0 */
         String[] attributes = new String[]{"attr1"};
         String ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false,
                                                                validPrincipal);
         assertEquals(0, MoriaController.getSecLevel(ticket));
 
         /* Seclevel 1 */
         attributes = new String[]{"attr2"};
         ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
         assertEquals(1, MoriaController.getSecLevel(ticket));
         attributes = new String[]{"attr2", "attr1"};
         ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
         assertEquals(1, MoriaController.getSecLevel(ticket));
 
         /* Seclevel 2*/
         attributes = new String[]{"attr3"};
         ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
         assertEquals(2, MoriaController.getSecLevel(ticket));
         attributes = new String[]{"attr3", "attr2"};
         ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
         assertEquals(2, MoriaController.getSecLevel(ticket));
         attributes = new String[]{"attr1", "attr3"};
         ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
         assertEquals(2, MoriaController.getSecLevel(ticket));
         attributes = new String[]{"attr1", "attr3", "attr2"};
         ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
         assertEquals(2, MoriaController.getSecLevel(ticket));
 
         /* Should be able to call this multiple times without exception */
         MoriaController.getSecLevel(ticket);
     }
 
     /**
      * Test the directNonInteractiveAuthentication method.
      *
      * @see MoriaController#directNonInteractiveAuthentication(java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testDirectNonInteractiveAuthentication() throws MoriaControllerException {
         controllerInitialization();
 
         /* Invalid arguments */
         try {
             MoriaController.directNonInteractiveAuthentication(null, validUsername, validPassword, validPrincipal);
             fail("IllegalInputException should be raised, attributes is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, null, validPassword, validPrincipal);
             fail("IllegalInputException should be raised, username is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, "", validPassword, validPrincipal);
             fail("IllegalInputException should be raised, username is an empty string");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, null, validPrincipal);
             fail("IllegalInputException should be raised, password is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, "", validPrincipal);
             fail("IllegalInputException should be raised, password is an empty string");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, validPassword, null);
             fail("IllegalInputException should be raised, servicePrincipal is null");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, validPassword, "");
             fail("IllegalInputException should be raised, servicePrincipal is an empty string");
         } catch (IllegalInputException success) {
         }
 
         /* Illegal attributes */
         try {
             MoriaController.directNonInteractiveAuthentication(new String[]{"illegal"}, validUsername, validPassword,
                                                                validPrincipal);
             fail("AuhtorizationException should be raised, illegal attributes requested.");
         } catch (AuthorizationException success) {
         }
 
         /* Illegal servicePrincipal */
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, validPassword, "invalid");
             fail("AuhtorizationException should be raised, invalid principal.");
         } catch (AuthorizationException success) {
         }
 
         /* Wrong username/password */
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, "wrong", validPassword, validPrincipal);
             fail("AuthenticationException should be raised, wrong username.");
         } catch (AuthenticationException success) {
         }
         try {
             MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, "wrong", validPrincipal);
             fail("AuthenticationException should be raised, wrong password.");
         } catch (AuthenticationException success) {
         }
 
         /* Empty set of attributes */
         Map actualAttrs = MoriaController.directNonInteractiveAuthentication(new String[]{}, validUsername,
                                                                              validPassword, validPrincipal);
         validateMaps(new HashMap(), actualAttrs);
 
         /* Correct username/password */
         actualAttrs = MoriaController.directNonInteractiveAuthentication(validAttrs, validUsername, validPassword,
                                                                          validPrincipal);
         validateMaps(expectedAttrs, actualAttrs);
 
     }
 
     /**
      * Test the proxyAuthentication method.
      *
      * @see MoriaController#proxyAuthentication(java.lang.String[], java.lang.String, java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testProxyAuthentication() throws MoriaControllerException {
         controllerInitialization();
 
         final String validProxyTicketId = "1234";
 
         /* Invalid arguments */
         try {
             MoriaController.proxyAuthentication(null, validProxyTicketId, validPrincipal);
             fail("IllegalInputException should be raised, attributes is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.proxyAuthentication(validAttrs, null, validPrincipal);
             fail("IllegalInputException should be raised, proxyTicketId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.proxyAuthentication(validAttrs, "", validPrincipal);
             fail("IllegalInputException should be raised, proxyTicketId is an emtpy string.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.proxyAuthentication(validAttrs, validProxyTicketId, null);
             fail("IllegalInputException should be raised, servicePrincipal is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.proxyAuthentication(validAttrs, validProxyTicketId, "");
             fail("IllegalInputException should be raised, servicePrincipal is an emtpy string.");
         } catch (IllegalInputException success) {
         }
 
         final String[] attrsProxy = new String[]{"tgt"};
         final String loginTicketId = MoriaController.initiateAuthentication(attrsProxy, validPrefix, validPostfix, false,
                                                                       validPrincipal);
         final Map tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         Map resultAttrs = MoriaController.getUserAttributes((String) tickets.get(MoriaController.SERVICE_TICKET),
                                                             validPrincipal);
         /* Ticket generation */
         final String tgt = (String) resultAttrs.get(MoriaController.TGT_IDENTIFIER);
         assertNotNull("TGT should not be null", tgt);
 
         /* Asking for more attributes than the ones that has been cached */
         String proxyTicket = MoriaController.getProxyTicket(tgt, "sub1", validPrincipal);
         try {
             MoriaController.proxyAuthentication(new String[]{"attr1", "attr2", "attr3"}, proxyTicket, "sub1");
             fail("AuthorizationException should be raised, asking for non-cached attributes.");
         } catch (AuthorizationException success) {
         }
 
         /* Ticket should be removed now due to the unauthorized request above */
         try {
             MoriaController.proxyAuthentication(new String[]{"attr1", "attr2"}, proxyTicket, "sub1");
             fail("UnknownTicketException should be raised, proxy ticket should have been removed.");
         } catch (UnknownTicketException success) {
         }
 
         /* Unauthorized attributes */
         proxyTicket = MoriaController.getProxyTicket(tgt, "sub1", validPrincipal);
         try {
             MoriaController.proxyAuthentication(new String[]{"doesNotExist"}, proxyTicket, "sub1");
             fail("AuthorizationException should be raised, asking for non-cached attributes.");
         } catch (AuthorizationException success) {
         }
 
         proxyTicket = MoriaController.getProxyTicket(tgt, "sub1", validPrincipal);
         resultAttrs = MoriaController.proxyAuthentication(new String[]{"attr1", "attr2"}, proxyTicket, "sub1");
 
         /* Check attributes */
         validateMaps(expectedAttrs, resultAttrs);
 
         /* Ticket should be removed after use */
         try {
             MoriaController.proxyAuthentication(new String[]{"attr1", "attr2"}, proxyTicket, "sub1");
             fail("UnknownTicketException should be raised, proxy ticket have been used.");
         } catch (UnknownTicketException success) {
         }
     }
 
     /**
      * Tests the getProxyTicket method.
      *
      * @see MoriaController#getProxyTicket(java.lang.String, java.lang.String, java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testGetProxyTicket() throws MoriaControllerException {
         controllerInitialization();
 
         final String validTGT = "1234";
 
         /* Validate arguments */
         try {
             MoriaController.getProxyTicket(null, validPrincipal, validPrincipal);
             fail("IllegalInputException should be raised, ticketGrantingTicket is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getProxyTicket("", validPrincipal, validPrincipal);
             fail("IllegalInputException should be raised, ticketGrantingTicket is an empty string.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getProxyTicket(validTGT, null, validPrincipal);
             fail("IllegalInputException should be raised, proxyServicePrincipal is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getProxyTicket(validTGT, "", validPrincipal);
             fail("IllegalInputException should be raised, proxyServicePrincipal is an empty string.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getProxyTicket(validTGT, validPrincipal, null);
             fail("IllegalInputException should be raised, servicePrincipal is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getProxyTicket(validTGT, validPrincipal, "");
             fail("IllegalInputException should be raised, servicePrincipal is an empty string.");
         } catch (IllegalInputException success) {
         }
 
         /* Client not allowed to perform proxy authentication */
         try {
             MoriaController.initiateAuthentication(new String[]{"tgt"}, validPrefix, validPostfix, false, "limited");
             fail("AuthorizationException should be raised, service not allowed to request TGT.");
         } catch (AuthorizationException success) {
         }
 
         final String loginTicketId = MoriaController.initiateAuthentication(new String[]{"tgt"}, validPrefix, validPostfix,
                                                                       false, validPrincipal);
         final Map tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         final Map resultAttrs = MoriaController.getUserAttributes((String) tickets.get(MoriaController.SERVICE_TICKET),
                                                             validPrincipal);
         /* Ticket generation */
         final String tgt = (String) resultAttrs.get("tgt");
         assertNotNull("TGT should not be null", tgt);
 
 
         /* Illegal subsystem */
         try {
             MoriaController.getProxyTicket(tgt, validPrincipal, "illegalSubsystem");
             fail("AuthorizationException should be raised, illegal subsystem.");
         } catch (AuthorizationException success) {
         }
 
         /* Validate proxy ticket */
         final String proxyTicket = MoriaController.getProxyTicket(tgt, "sub1", validPrincipal);
         assertNotNull("Proxy ticket should not be null", proxyTicket);
 
         /* Get attributes for proxy ticket */
         final Map actualAttrs = MoriaController.proxyAuthentication(new String[]{"attr1", "attr2"}, proxyTicket, "sub1");
         validateMaps(expectedAttrs, actualAttrs);
     }
 
     /**
      * Tests the verifyUserExistence method.
      *
      * @see MoriaController#verifyUserExistence(java.lang.String, java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testVerifyUserExistence() throws MoriaControllerException {
         controllerInitialization();
 
         /* Invalid arguments */
         try {
             MoriaController.verifyUserExistence(null, validPrincipal);
             fail("IllegalInputException should be raised, userId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.verifyUserExistence("", validPrincipal);
             fail("IllegalInputException should be raised, userId an empty string.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.verifyUserExistence(validUsername, null);
             fail("IllegalInputException should be raised, servicePrincipal is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.verifyUserExistence(validUsername, "");
             fail("IllegalInputException should be raised, servicePrincipal an empty string.");
         } catch (IllegalInputException success) {
         }
 
         /* Unauthorized request */
         try {
             MoriaController.verifyUserExistence(validUsername, "limited");
             fail("AuthorizationException should be raised, service is not allowed to perform operation.");
         } catch (AuthorizationException success) {
         }
         try {
             MoriaController.verifyUserExistence(validUsername, "doesNotExist");
             fail("AuthorizationException should be raised, non-existing service.");
         } catch (AuthorizationException success) {
         }
 
         /* Normal use */
         assertTrue("UserId should be valid: '" + validUsername + "'",
                    MoriaController.verifyUserExistence(validUsername, validPrincipal));
        assertFalse("UserId should not be valid: 'doesNotExist'",
                    MoriaController.verifyUserExistence("doesNotExist", validPrincipal));
 
     }
 
     /**
      * Tests the invalidateSSOTicket method.
      *
      * @see MoriaController#invalidateSSOTicket(java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testInvalidateSSOTicket() throws MoriaControllerException {
         controllerInitialization();
 
         /* Invalid arguments */
         try {
             MoriaController.invalidateSSOTicket(null);
             fail("IllegalInputException should be raised, ssoTicketId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.invalidateSSOTicket("");
             fail("IllegalInputException should be raised, ssoTicketId is an empty string.");
         } catch (IllegalInputException success) {
         }
 
         /* Non-existing SSO ticket, should be ignored */
         MoriaController.invalidateSSOTicket("doesNotExist");
 
         /* Existing SSO ticket */
         String loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                       validPrincipal);
         final Map tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         final String ssoTicketId = (String) tickets.get(MoriaController.SSO_TICKET);
 
         /* Verify that SSO is possible */
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         MoriaController.attemptSingleSignOn(loginTicketId, ssoTicketId);
 
         /* Invalidate ticket and verify that SSO is no longer possible */
         MoriaController.invalidateSSOTicket(ssoTicketId);
         loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,
                                                                validPrincipal);
         try {
             MoriaController.attemptSingleSignOn(loginTicketId, ssoTicketId);
             fail("UnknownTicketException should be raised, invalidated SSO-ticket");
         } catch (UnknownTicketException success) {
 
         }
     }
 
     /**
      * Tests the getRedirectURL method.
      *
      * @see MoriaController#getRedirectURL(java.lang.String)
      * @throws MoriaControllerException if any of the tests fail unexpectedly
      */
     public void testGetRedirectURL() throws MoriaControllerException {
         controllerInitialization();
 
         /* Invalid arguments */
         try {
             MoriaController.getRedirectURL(null);
             fail("IllegalInputException should be raised, serviceTicketId is null.");
         } catch (IllegalInputException success) {
         }
         try {
             MoriaController.getRedirectURL("");
             fail("IllegalInputException should be raised, serviceTicketId is an empty string.");
         } catch (IllegalInputException success) {
         }
 
         final String loginTicketId = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false,                                                                      validPrincipal);
         final Map tickets = MoriaController.attemptLogin(loginTicketId, null, validUsername, validPassword);
         final String serviceTicket = (String) tickets.get(MoriaController.SERVICE_TICKET);
         final String actualURL = MoriaController.getRedirectURL(serviceTicket);
         final String expectedURL = validPrefix+serviceTicket+validPostfix;
         assertEquals("Redirect URL differs.", expectedURL, actualURL);
     }
 }
