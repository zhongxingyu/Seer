 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.gmf.tests.gef.DiagramNodeTest;
 import org.eclipse.gmf.tests.gen.CompilationTest;
 import org.eclipse.gmf.tests.gen.HandcodedImplTest;
 import org.eclipse.gmf.tests.setup.TestSetupTest;
 import org.eclipse.gmf.tests.tr.EPNamingStrategyTest;
 import org.eclipse.gmf.tests.tr.GenModelTransformerBasicRTTest;
 
 public class AllTests {
 
 	public static void main(String[] args) {
 		junit.textui.TestRunner.run(AllTests.suite());
 	}
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Tests for org.eclipse.gmf");
 		//$JUnit-BEGIN$
 		suite.addTestSuite(TestSetupTest.class); // first, check sources/setups we use for rest of the tests
 		suite.addTestSuite(HandcodedImplTest.class); // then, check handcoded implementations are in place
 		suite.addTestSuite(CompilationTest.class);
 
 		suite.addTestSuite(DiagramNodeTest.class);
 		suite.addTestSuite(EPNamingStrategyTest.class);
 		suite.addTestSuite(GenModelTransformerBasicRTTest.class);
 
 //		suite.addTestSuite(RunTimeModelTransformerTest.class); #113966
 //		suite.addTestSuite(PropertiesTest.class); #113965 
 //		suite.addTestSuite(CanvasTest.class); Nothing there yet
 //		suite.addTestSuite(SpecificRTPropertiesTest.class); #113965 
 		//$JUnit-END$
 		suite.addTest(new TestCase("testCleanup") {
			public void testCleanup() {
 				try {
 					SessionSetup.cleanup();
 				} catch (RuntimeException ex) {
 					throw ex;
 				} catch (Exception ex) {
 					Plugin.logError("cleanup failed", ex);
 					fail(ex.getMessage());
 				}
 			}
 		});
 		return suite;
 	}
 
 }
