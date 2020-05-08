 /*******************************************************************************
  * Copyright (c) 2007-2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.jboss.tools.jst.jsp.test.ca.ExternalizeCommandTest;
 import org.jboss.tools.jst.jsp.test.ca.JstJspJbide1585Test;
 import org.jboss.tools.jst.jsp.test.ca.JstJspJbide1641Test;
 import org.jboss.tools.jst.jsp.test.ca.NewEditorSideBySideCommandTest;
 import org.jboss.tools.jst.jsp.test.ca.SelectionBarTest;
 
 public class JstJspAllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Test for org.jboss.tools.jst.jsp.test"); //$NON-NLS-1$
 		
  		suite.addTestSuite(JstJspJbide1585Test.class);
 		suite.addTestSuite(JstJspJbide1641Test.class);
 		
 		/* 
 		 * TODO: Uncomment the following test case after https://jira.jboss.org/browse/JBIDE-7100 issue 
 		 * is resolved due to enable the test to run
 		*
 		suite.addTestSuite(Jbide1791Test.class);
 		*/
 		suite.addTestSuite(JspPreferencesPageTest.class);
 		
 		suite.addTestSuite(SelectionBarTest.class);
 		suite.addTestSuite(ExternalizeCommandTest.class);
 	
		suite.addTestSuite(NewEditorSideBySideCommandTest.class);
 		return suite;
 	}
 
 }
