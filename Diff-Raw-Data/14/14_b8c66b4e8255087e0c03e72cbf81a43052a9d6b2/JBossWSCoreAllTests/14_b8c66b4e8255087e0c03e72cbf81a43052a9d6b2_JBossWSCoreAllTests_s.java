 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.core.test;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.jboss.tools.ws.core.test.classpath.JBossWSRuntimeManagerTest;
 import org.jboss.tools.ws.core.test.command.JBossWSJavaFirstCommandTest;
 import org.jboss.tools.ws.core.test.command.JBossWSClientCommandTest;
 
 public class JBossWSCoreAllTests extends TestCase {
 	public static final String PLUGIN_ID = "org.jboss.tools.common.test";
 	public static Test suite ()
 	{
 		TestSuite suite = new TestSuite(JBossWSCoreAllTests.class.getName());
 		suite.addTestSuite(JBossWSRuntimeManagerTest.class);
 		suite.addTestSuite(JBossWSJavaFirstCommandTest.class);
 		suite.addTestSuite(JBossWSClientCommandTest.class);
	
 		return suite;
 	}
 }
