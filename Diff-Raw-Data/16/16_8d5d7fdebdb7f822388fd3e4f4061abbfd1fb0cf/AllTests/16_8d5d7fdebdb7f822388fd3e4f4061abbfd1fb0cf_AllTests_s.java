// Copyright 2008 Google Inc. All rights reserved.
 
 package com.google.common.testing;
 
 import com.google.common.testing.junit3.JUnitAssertsTest;
 import com.google.common.testing.junit3.TearDownTestCaseTest;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
 
   public static Test suite() {
     TestSuite suite = new TestSuite("Test for com.google.common.testing");
     //$JUnit-BEGIN$
     suite.addTestSuite(TestLogHandlerTest.class);
     suite.addTestSuite(JUnitAssertsTest.class);
     suite.addTestSuite(TearDownTestCaseTest.class);
     //$JUnit-END$
     return suite;
   }
 
 }
