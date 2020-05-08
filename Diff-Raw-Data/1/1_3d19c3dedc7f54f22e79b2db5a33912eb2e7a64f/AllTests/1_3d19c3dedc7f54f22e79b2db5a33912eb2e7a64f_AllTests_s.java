 package org.eclipse.dltk.ui.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.dltk.ui.tests.core.ScriptElementLabelsTest;
 import org.eclipse.dltk.ui.tests.navigator.scriptexplorer.PackageExplorerTests;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Test for org.eclipse.dltk.ui.tests");
 		//$JUnit-BEGIN$
 		suite.addTestSuite(ScriptElementLabelsTest.class);
 		
 		suite.addTest(PackageExplorerTests.suite());
 		//$JUnit-END$
 		return suite;
 	}
 
 }
