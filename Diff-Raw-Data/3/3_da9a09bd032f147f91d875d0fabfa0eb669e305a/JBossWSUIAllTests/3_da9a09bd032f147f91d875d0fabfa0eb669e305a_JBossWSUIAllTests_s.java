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
 
 package org.jboss.tools.ws.ui.test;
 
 import org.jboss.tools.ws.ui.test.preferences.JBossWSRuntimePreferencePageTest;
 import org.jboss.tools.ws.ui.test.utils.TesterWSDLUtilsTest;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * @author Grid Qian
  */
 public class JBossWSUIAllTests extends TestCase {
 	public static final String PLUGIN_ID = "org.jboss.tools.common.test";
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite(JBossWSUIAllTests.class.getName());
 		suite.addTestSuite(JBossWSRuntimePreferencePageTest.class);
		suite.addTestSuite(TesterWSDLUtilsTest.class);
 
 		return suite;
 	}
 }
