 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ui.tests.navigator.scriptexplorer;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class PackageExplorerTests {
 
 	public static Test suite() { 
 		TestSuite suite= new TestSuite("Test for org.eclipse.dltk.ui.tests.packageview");
 		//$JUnit-BEGIN$
 		suite.addTestSuite(ContentProviderTests1.class);
 		suite.addTestSuite(ContentProviderTests2.class);
 		suite.addTestSuite(ContentProviderTests3.class);
 		suite.addTestSuite(ContentProviderTests4.class);
 		suite.addTestSuite(ContentProviderTests5.class);
		suite.addTest(PackageExplorerShowInTests.suite());
 		//suite.addTestSuite(WorkingSetDropAdapterTest.class);
 		//$JUnit-END$
 		return suite;
 	}
 }
