 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.dltk.core.tests.buildpath.BuildpathTests;
 import org.eclipse.dltk.core.tests.ddp.CoreDDPTests;
import org.eclipse.dltk.core.tests.launching.EnvironmentResolverTests;
 import org.eclipse.dltk.core.tests.launching.InterpreterConfigTests;
 import org.eclipse.dltk.core.tests.model.BufferTests;
 import org.eclipse.dltk.core.tests.model.ModelMembersTests;
 import org.eclipse.dltk.core.tests.model.WorkingCopyTests;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite(
 				"Test for org.eclipse.dltk.core.tests.model");
 		// $JUnit-BEGIN$
 		suite.addTest(BuildpathTests.suite());
 
 		suite.addTest(CoreDDPTests.suite());
 
 		suite.addTest(BufferTests.suite());
 		suite.addTest(ModelMembersTests.suite());
 		suite.addTest(WorkingCopyTests.suite());
 
 		suite.addTest(InterpreterConfigTests.suite());
		
		suite.addTest(EnvironmentResolverTests.suite());
 		// $JUnit-END$
 		return suite;
 	}
 }
