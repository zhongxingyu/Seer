 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.suite;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.cdo.CDOIntegrationTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.compare.ChangeEditorUpdateTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.compare.IntentMatchEngineTests;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.compare.SimpleOrderTests;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.compilation.CompileTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.opening.OpenEditorTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.synchronization.EcoreTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.synchronization.JavaTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.project.ProjectTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.refresher.RefresherTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.repository.IntentRepositoryStructurerTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.CompilerNotificationsTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.IntentAbstractResourceTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.IntentDocumentationUpdateDoesNotCauseResolvingIssuesTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario.IntentProjectReopeningTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.update.DragAndDropTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.update.QuickFixTest;
 
 /**
  * This suite will launch all the tests relative to the UI behavior.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class UITestSuite extends TestCase {
 
 	/**
 	 * Launches the collaborative test suite.
 	 * 
 	 * @param args
 	 *            the arguments
 	 */
 	public static void main(String[] args) {
 		TestRunner.run(suite());
 	}
 
 	/**
 	 * Creates the {@link junit.framework.TestSuite TestSuite} for all Intent UI tests.
 	 * 
 	 * @return The test suite containing all intent ui tests
 	 */
 	public static Test suite() {
 		final TestSuite suite = new TestSuite("Intent Global TestSuite");
 
 		/*
 		 * Intent Technical Tests
 		 */
 		final TestSuite clientSuite = new TestSuite("Intent Client tests");
 		suite.addTest(clientSuite);
 
 		// Match & merge tests
 		final TestSuite compareSuite = new TestSuite("Intent match and merge tests");
 		compareSuite.addTestSuite(IntentMatchEngineTests.class);
 		clientSuite.addTest(compareSuite);
 
 		/*
 		 * Intent UI Tests
 		 */
 		final TestSuite uiTestSuite = new TestSuite("Intent UI tests");
 		suite.addTest(uiTestSuite);
 
 		// Core tests
 		// All tests that test a technical concern (emf compare behavior, project lifecycle...)
 		final TestSuite basicTestSuite = new TestSuite("Technical tests");
 		basicTestSuite.addTestSuite(IntentRepositoryStructurerTest.class);
 		basicTestSuite.addTestSuite(ProjectTest.class);
 		basicTestSuite.addTestSuite(RefresherTest.class);
 		basicTestSuite.addTestSuite(ChangeEditorUpdateTest.class);
 		basicTestSuite.addTestSuite(SimpleOrderTests.class);
 		uiTestSuite.addTest(basicTestSuite);
 
 		// Scenario tests
 		// all tests that test an identified scenario for the end-user (very simple use case)
 		final TestSuite scenarioSuite = new TestSuite("Simple End-User Scenarios");
 		scenarioSuite.addTestSuite(CompilerNotificationsTest.class);
 		scenarioSuite.addTestSuite(IntentAbstractResourceTest.class);
 		scenarioSuite.addTestSuite(IntentDocumentationUpdateDoesNotCauseResolvingIssuesTest.class);
 		scenarioSuite.addTestSuite(IntentProjectReopeningTest.class);
 		uiTestSuite.addTest(scenarioSuite);
 
 		// Complete use case testSuite
 		// all tests that ensures the behavior of complete use cases
 		final TestSuite demoSuite = new TestSuite("Intent Demo TestSuite");
 		demoSuite.addTestSuite(OpenEditorTest.class);
 		demoSuite.addTestSuite(CompileTest.class);
 		demoSuite.addTestSuite(EcoreTest.class);
 		demoSuite.addTestSuite(JavaTest.class);
 		// uiTestSuite.addTest(demoSuite);
 
 		// Updates tests
 		final TestSuite updatesSuite = new TestSuite("Modeling Unit update tests");
 		updatesSuite.addTestSuite(QuickFixTest.class);
 		updatesSuite.addTestSuite(DragAndDropTest.class);
 		uiTestSuite.addTest(updatesSuite);
 
		/*
		 * CDO related test Suite
		 */
		final TestSuite cdoSuite = new TestSuite("CDO integration tests");
		suite.addTest(cdoSuite);

		cdoSuite.addTestSuite(CDOIntegrationTest.class);

 		return suite;
 	}
 
 }
