 package org.xbrlapi.loader.discoverer.tests;
 
import org.xbrlapi.data.bdbxml.tests.SecAsyncGrabberImplTest;

 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Test for org.xbrlapi.loader.discoverer.tests");
 		//$JUnit-BEGIN$
 		suite.addTestSuite(SecAsyncGrabberImplTest.class);
 		//$JUnit-END$
 		return suite;
 	}
 
 }
