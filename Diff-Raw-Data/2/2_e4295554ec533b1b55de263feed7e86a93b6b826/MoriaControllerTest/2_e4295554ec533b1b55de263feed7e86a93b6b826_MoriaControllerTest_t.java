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
 
 /**
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public class MoriaControllerTest extends TestCase {
 
     String validPrefix, validPostfix, validPrincipal;
     String[] validAttrs;
 
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
         if (System.getProperty("no.feide.moria.store.randomid.nodeid") == null)
             System.setProperty("no.feide.moria.store.randomid.nodeid", "no1");
 
         validPrefix = "http://moria.sf.net/";
         validPostfix = "&foo=bar";
         validPrincipal = "test";
         validAttrs = new String[]{"attr1", "attr2"};
     }
 
     /**
      * Test the init() method.
      *
      * @see MoriaController#init()
      */
     public void testInit() {
         MoriaController.init();
     }
 
     /**
      * Thest the initiateMoriaAuthentication method.
      *
      * @throws AuthorizationException
      * @throws IllegalInputException
      * @see MoriaController#initiateAuthentication(java.lang.String[], java.lang.String, java.lang.String, boolean, java.lang.String)
      */
     public void testInitiateMoriaAuthentication()
             throws AuthorizationException, IllegalInputException, UnknownTicketException, InoperableStateException {
 
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
         String[] attrs = new String[]{"illegal1", "illegal2"};
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
         String ticket;
         ticket = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
         assertNotNull("Login ticket should be valid", MoriaController.getServiceProperties(ticket));
 
         ticket = MoriaController.initiateAuthentication(new String[]{}, validPrefix, validPostfix, false, validPrincipal);
         // assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));
     }
 
     public void testAttemptLogin() {
         // TODO: Implement
     }
 
     public void testAttemptSingleSignOn() {
         // TODO: Implement
     }
 
     public void testGetUserAttributes() {
         // TODO: Implement
     }
 
     /**
      * Test the isLegalURL method.
      *
      * @see MoriaController#isLegalURL(java.lang.String)
      */
     public void testisLegalURL() {
         // TODO: Test more illegal URL constructs
         MoriaController.init();
 
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
         assertTrue("URL should be accepted", MoriaController.isLegalURL("http://moria.sf.net/index.html?foo=bar&bar=foo"));
     }
 
     /**
      * Test getServiceProperties method.
      *
      * @throws IllegalInputException
      * @throws AuthorizationException
      * @throws UnknownTicketException
      * @see MoriaController#getServiceProperties(java.lang.String)
      */
 
     public void testGetServiceProperties() throws IllegalInputException, AuthorizationException,
             UnknownTicketException, InoperableStateException {
         controllerInitialization();
         /* Invalid arguments */
         try {
             MoriaController.getServiceProperties(null);
             fail("IllegalArgumentException should be raised, null value");
         } catch (IllegalArgumentException success) {
         }
         try {
             MoriaController.getServiceProperties("");
             fail("IllegalArgumentException should be raised, empty string");
         } catch (IllegalArgumentException success) {
         }
 
         String ticket = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
         //assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));
         HashMap properties = MoriaController.getServiceProperties(ticket);
         assertEquals("Principal differs", validPrincipal, properties.get("name"));
     }
 
     public void testGetSecLevel() throws IllegalInputException, AuthorizationException,
             UnknownTicketException, InoperableStateException {
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
             //TODO: This returns null, but should maybe throw an IllegalTicketException?
             MoriaController.getSecLevel("doesNotExist");
             fail("InvalidTicketException should be raised");
         } catch (UnknownTicketException success) {
         }
 
         /* Seclevel 0 */
         String[] attributes = new String[]{"attr1"};
         String ticket = MoriaController.initiateAuthentication(attributes, validPrefix, validPostfix, false, validPrincipal);
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
      * Verify that the controller initialization checks works and then initates the conftroller.
      */
    private void controllerInitialization() throws InoperableStateException, IllegalInputException {
         /* Controller not initialized */
         MoriaController.stop();
         try {
             MoriaController.getServiceProperties("foobar");
             fail("IllegalStateException should be raised, controller not initialized.");
         } catch (IllegalStateException success) {
         } catch (UnknownTicketException e) {
             /* Should never get here */
         }
 
         MoriaController.init();
     }
 
 }
