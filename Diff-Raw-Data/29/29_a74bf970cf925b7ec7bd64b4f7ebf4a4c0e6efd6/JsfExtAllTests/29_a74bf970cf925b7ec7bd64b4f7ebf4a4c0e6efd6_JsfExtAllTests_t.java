 package org.jboss.tools.jst.text.ext.test;
 
 import junit.framework.Test;
import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
public class JsfExtAllTests extends TestCase{
 	
 	// all tests were moved to jst.ui.test plugin
 	public void testJsfTextExt () {
 		
 	}
	public static Test suite() {
	TestSuite suite = new TestSuite("Test for default package");
		//$JUnit-BEGIN$

		suite.addTestSuite(JsfExtAllTests.class);
		
		//$JUnit-END$
		return suite;
	}
 }
