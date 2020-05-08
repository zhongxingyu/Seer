 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.dltk.debug.dbgp.tests.DbgpBase64Tests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpBreakpointCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpContextCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpContinuationCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpFeatureCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpPropertyCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpRequestTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpStackCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpStackLevelTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpStatusCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpStatusTests;
 import org.eclipse.dltk.debug.dbgp.tests.DbgpStreamCommandsTests;
 import org.eclipse.dltk.debug.dbgp.tests.service.DbgpServiceDispatcherTests;
 import org.eclipse.dltk.debug.dbgp.tests.service.DbgpServiceTests;
 import org.eclipse.dltk.debug.tests.breakpoints.BreakpointTests;
 
 public class AllTests {
 
 	public static Test suite() {
 		final TestSuite suite = new TestSuite("org.eclipse.dltk.debug");
 
 		// // $JUnit-BEGIN$
 
 		// Dbgp
 		final TestSuite dbgpSuite = new TestSuite("DBGP tests");
 		dbgpSuite.addTestSuite(DbgpRequestTests.class);
 		dbgpSuite.addTestSuite(DbgpStackLevelTests.class);
 		dbgpSuite.addTestSuite(DbgpPropertyCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpFeatureCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpBreakpointCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpContextCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpContinuationCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpStackCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpStreamCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpStatusCommandsTests.class);
 		dbgpSuite.addTestSuite(DbgpBase64Tests.class);
 		dbgpSuite.addTestSuite(DbgpStatusTests.class);
 		suite.addTest(dbgpSuite);
 
 		final TestSuite serviceSuite = new TestSuite("DBGP Service tests");
 		serviceSuite.addTestSuite(DbgpServiceTests.class);
		serviceSuite.addTestSuite(DbgpServiceDispatcherTests.class);
 		suite.addTest(serviceSuite);
 
 		// Breakpoints
 		suite.addTest(BreakpointTests.suite());
 		// // $JUnit-END$
 		return suite;
 	}
 }
