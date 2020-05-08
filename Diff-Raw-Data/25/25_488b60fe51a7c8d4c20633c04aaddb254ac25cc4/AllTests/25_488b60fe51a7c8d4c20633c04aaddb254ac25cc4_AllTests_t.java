 package org.illithid.cccp.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("CCCP");
 		//$JUnit-BEGIN$
 		suite.addTestSuite(DalekTest.class);
		suite.addTestSuite(HeroTest.class);
 		//$JUnit-END$
 		return suite;
 	}
 
 }
