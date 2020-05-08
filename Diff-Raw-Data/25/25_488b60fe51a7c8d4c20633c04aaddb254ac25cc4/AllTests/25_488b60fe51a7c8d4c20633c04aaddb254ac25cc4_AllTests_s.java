 package org.illithid.cccp.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("CCCP");
 		//$JUnit-BEGIN$
		suite.addTestSuite(HeroTest.class);
 		suite.addTestSuite(DalekTest.class);
 		//$JUnit-END$
 		return suite;
 	}
 
 }
