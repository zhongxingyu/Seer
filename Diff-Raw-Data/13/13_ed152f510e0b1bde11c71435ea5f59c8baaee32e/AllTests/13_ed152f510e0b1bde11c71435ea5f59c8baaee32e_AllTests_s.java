 package it.polimi.chansonnier.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Acceptance tests for it.polimi.chansonnier");
        System.out.println("Begin: it.polimi.chansonnier.test.AllTests");
 		//$JUnit-BEGIN$
 		suite.addTestSuite(AddSongTest.class);
 		//$JUnit-END$
 		return suite;
 	}
 
 }
