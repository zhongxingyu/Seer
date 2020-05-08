 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.formatter.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
 
 	static final String CHARSET = "ISO-8859-1";
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite(
 				"Test for org.eclipse.dltk.ruby.formatter.tests");
 		// $JUnit-BEGIN$
 		suite.addTestSuite(ParserTest.class);
 		suite.addTestSuite(SimpleTests.class);
 		suite.addTest(ClassesAndMethodsTest.suite());
 		suite.addTest(ModulesTest.suite());
 		suite.addTest(BlocksTest.suite());
 		suite.addTest(RDocTest.suite());
 		suite.addTest(IfTest.suite());
 		suite.addTest(BeginTest.suite());
 		suite.addTest(DoTest.suite());
 		suite.addTest(CommentsTest.suite());
 		suite.addTest(UnaryPlusTest.suite());
 		suite.addTest(HereDocumentTest.suite());
 		suite.addTest(StringsTest.suite());
 		suite.addTest(RegexpTest.suite());
 		suite.addTest(RubyDoc1Test.suite());
 		suite.addTest(RubyDoc2Test.suite());
		// suite.addTestSuite(FormatRubyLibTest.class);
 		// $JUnit-END$
 		return suite;
 	}
 
 }
