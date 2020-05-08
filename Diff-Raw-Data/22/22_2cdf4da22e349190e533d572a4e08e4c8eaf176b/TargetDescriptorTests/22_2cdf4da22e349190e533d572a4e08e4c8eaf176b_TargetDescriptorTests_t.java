 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.common.service;
 
 import java.net.MalformedURLException;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.service.TargetDescriptor;
 
 /**
  * Tests for class <code>TargetDescriptor</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class TargetDescriptorTests extends TestCase {
 
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
       return new TestSuite(TargetDescriptorTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>TargetDescriptorTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public TargetDescriptorTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void testTargetDescriptor() throws Exception {
 
       // Pass null to constructor
       try {
          new TargetDescriptor(null);
          fail("TargetDescriptor(String) should throw an IllegalArgumentException if the argument is null.");
       } catch (IllegalArgumentException ex) {
          // as expected
       }
 
       // Test some invalid URLs
       String[] invalidURLs = new String[] {
          "", " ", "\n", "http:8", "http:/8", "blablabla",
         "http://example%20com/", "http://", "http%3A%2F%2Fwww.example.com",
         "http:///index.html", "/usr/local/something", "C:\\xins\\src",
         "file://C:\\xins\\src", "http://http://www.example.com",
         "http://www.example.com /index.html"
       };
       for (int i = 0; i < invalidURLs.length; i++) {
          String url = invalidURLs[i];
          try {
             new TargetDescriptor(url);
             fail("TargetDescriptor(String) should throw a MalformedURLException if the argument is \"" + url + "\".");
          } catch (MalformedURLException ex) {
             // as expected
          }
       }
 
       // Test some valid URLs
       String[] validURLs = new String[] {
         "file://home/ernst/something.xml",
         "file://C/Documents%20and%20Settings/",
         "file://home/janwb/../ernst/something.xml",
          "ftp://someserver.co.au/",
          "ftp://someserver.co.au/pub/content/",
          "ftp://someserver.co.au/pub/content/a.ico",
          "http://abc123.com/something",
         "http://127.0.0.1/",
          "http://10.2.3.4",
          "https://1.2.3.4/",
         "jdbc:odbc://dataserv:80/mydomain",
         "http://www.example.com/some%20file",
         "http://www.example.com:8080/",
         "http://www.example.com/somedir/../index.html"
       };
      for (int i = 0; i < validURLs.length; i++) {
          String url = validURLs[i];
          new TargetDescriptor(url);
       }
    }
 }
