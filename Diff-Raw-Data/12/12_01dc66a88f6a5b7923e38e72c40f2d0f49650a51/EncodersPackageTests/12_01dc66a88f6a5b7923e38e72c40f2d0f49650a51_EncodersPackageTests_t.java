 package org.jfree.chart.encoders;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class EncodersPackageTests extends TestCase {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite(EncodersPackageTests.class.getName());
 		// $JUnit-BEGIN$
 
		suite.addTestSuite(KeypointPNGEncoderAdapterTests.class);
 
 		// $JUnit-END$
 		return suite;
 	}
 }
