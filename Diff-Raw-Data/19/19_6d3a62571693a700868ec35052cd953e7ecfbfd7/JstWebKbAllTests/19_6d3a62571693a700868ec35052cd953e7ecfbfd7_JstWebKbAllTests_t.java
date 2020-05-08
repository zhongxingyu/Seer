 /*******************************************************************************
  * Copyright (c) 2009 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.jboss.tools.common.model.util.test.XProjectImportTestSetUp;
 import org.jboss.tools.jst.web.kb.test.validation.KBValidationTest;
 import org.jboss.tools.test.util.ProjectImportTestSetup;
 
 /**
  * @author Alexey Kazakov
  */
 public class JstWebKbAllTests {
 
 	public static Test suite() {
		TestSuite suiteAll = new TestSuite("KB Tests");
		TestSuite suite = new TestSuite(KbModelStorageTest.class.getName());
 		suite.addTestSuite(KbModelStorageTest.class);
		ProjectImportTestSetup testSetup = new XProjectImportTestSetUp(suite,
				"org.jboss.tools.jst.web.kb.test",
				new String[]{"projects/TestKbModel2"},
				new String[]{"TestKbModel2"});
		suiteAll.addTest(testSetup);
		suite = new TestSuite(JstWebKbAllTests.class.getName());
 		suite.addTestSuite(KbModelTest.class);
 		suite.addTestSuite(MyFacesKbModelTest.class);
 		suite.addTestSuite(KbMockModelTest.class);
 		suite.addTestSuite(MyFacesKbModelWithMetadataInSourcesTest.class);
 		suite.addTestSuite(WebKbTest.class);
		testSetup = new XProjectImportTestSetUp(suite,
 				"org.jboss.tools.jst.web.kb.test",
				new String[]{"projects/TestKbModel", "projects/MyFaces", "projects/MyFaces2", "projects/TestKbModel3"},
				new String[]{"TestKbModel", "MyFaces", "MyFaces2", "TestKbModel3"});
 		suiteAll.addTest(testSetup);
 		suiteAll.addTestSuite(KBValidationTest.class);
 		return suiteAll;
 	}
 }
