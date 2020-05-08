 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.stepper;
 
 import java.util.Map;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.StepperManager;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupable;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper;
 import org.eclipse.tcf.te.runtime.stepper.stepper.Stepper;
 import org.eclipse.tcf.te.tests.CoreTestCase;
 
 /**
  * Stepper engine test cases.
  */
 public class StepperTests extends CoreTestCase {
 
 	protected static class TestStepContext implements IStepContext {
 
 		@Override
 		public Object getAdapter(Class adapter) {
 			return null;
 		}
 
 		@Override
 		public String getId() {
 			return "org.eclipse.tcf.te.tests.stepper.TestStepContext"; //$NON-NLS-1$
 		}
 
 		@Override
 		public String getSecondaryId() {
 			return null;
 		}
 
 		@Override
 		public String getName() {
 			return "TestStepContext"; //$NON-NLS-1$
 		}
 
 		@Override
 		public Object getContextObject() {
 			return this;
 		}
 
 		@Override
 		public String getInfo(IPropertiesContainer data) {
 			return getName();
 		}
 
 	}
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("Test stepper engine"); //$NON-NLS-1$
 
 		// add ourself to the test suite
 		testSuite.addTestSuite(StepperTests.class);
 
 		return testSuite;
 	}
 
 	/**
 	 * Test the stepper extension point mechanism.
 	 */
 	public void testStepperContributions() {
 		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Test the steps extension point mechanism.
 	 */
 	public void testStepContributions() {
 		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance()); //$NON-NLS-1$
 		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance().getStepExtManager()); //$NON-NLS-1$
 
 		IStep[] steps = StepperManager.getInstance().getStepExtManager().getSteps(false);
 		int testStepCount = 0;
 
 		for (IStep step : steps) {
 			if (step.getId().startsWith("org.eclipse.tcf.te.tests.stepper.step")) { //$NON-NLS-1$
 				testStepCount++;
 			} else {
 				continue;
 			}
 
 			if (step.getId().endsWith(".step1")) { //$NON-NLS-1$
 				assertEquals("Unexpected step label found.", "Test Step 1", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
 				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
 			}
 
 			if (step.getId().endsWith(".step2")) { //$NON-NLS-1$
 				assertEquals("Unexpected step label found.", "Test Step 2", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Unexpected step class type found.", step instanceof ParameterizedTestStep); //$NON-NLS-1$
 
 				Map<?,?> params = ((ParameterizedTestStep)step).params;
 				assertNotNull("Unexpected value 'null'.", params); //$NON-NLS-1$
 				assertEquals("Unexpected number of parameter found.", 1, params.keySet().size()); //$NON-NLS-1$
 				assertTrue("Missing expected key 'param1'.", params.containsKey("param1")); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Missing expected value 'value1'.", params.containsValue("value1")); //$NON-NLS-1$ //$NON-NLS-2$
 
 				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
 			}
 
 			if (step.getId().endsWith(".step3")) { //$NON-NLS-1$
 				assertEquals("Unexpected step label found.", "Test Step 3", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "Just another test step", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
 				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
 			}
 
 			if (step.getId().endsWith(".step4")) { //$NON-NLS-1$
 				assertEquals("Unexpected step label found.", "Test Step 4", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
 				assertEquals("Unexpected number of dependencies found.", 1, step.getDependencies().length); //$NON-NLS-1$
 			}
 
 			if (step.getId().endsWith(".step5")) { //$NON-NLS-1$
 				assertEquals("Unexpected step label found.", "Test Step 5", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
 				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
 			}
 		}
 
 		assertEquals("Unexpected number of test steps found.", 5, testStepCount); //$NON-NLS-1$
 	}
 
 	/**
 	 * Test the step group extension point mechanism.
 	 */
 	public void testStepGroupContributions() {
 		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance()); //$NON-NLS-1$
 		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance().getStepGroupExtManager()); //$NON-NLS-1$
 
 		IStepGroup[] stepGroups = StepperManager.getInstance().getStepGroupExtManager().getStepGroups(false);
 		int testStepGroupCount = 0;
 
 		for (IStepGroup stepGroup : stepGroups) {
 			if (stepGroup.getId().startsWith("org.eclipse.tcf.te.tests.stepper.stepGroup")) { //$NON-NLS-1$
 				testStepGroupCount++;
 			} else {
 				continue;
 			}
 
 			Throwable error = null;
 			String message = null;
 			IStepGroupable[] steps = null;
 
 			if (stepGroup.getId().endsWith(".stepGroup1")) { //$NON-NLS-1$
 				assertEquals("Unexpected step group label found.", "Test Step Group 1", stepGroup.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", stepGroup.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertFalse("Step group is locked but should not.", stepGroup.isLocked()); //$NON-NLS-1$
 				assertNull("Unexpected non-null value.", stepGroup.getStepGroupIterator()); //$NON-NLS-1$
 
 				try {
 					steps = stepGroup.getSteps(new TestStepContext());
 				} catch (CoreException e) {
 					error = e;
 					message = e.getLocalizedMessage();
 				}
 				assertNull("Failed to determine steps from step group. Possible cause: " + message, error); //$NON-NLS-1$
 				assertNotNull("Unexpected return value 'null'.", steps); //$NON-NLS-1$
 				assertEquals("Unexpected number of steps returned.", 0, steps.length); //$NON-NLS-1$
 			}
 
 			if (stepGroup.getId().endsWith(".stepGroup2")) { //$NON-NLS-1$
 				assertEquals("Unexpected step group label found.", "Test Step Group 2", stepGroup.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "Just a step group description", stepGroup.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertTrue("Step group is not locked but should.", stepGroup.isLocked()); //$NON-NLS-1$
 				assertNull("Unexpected non-null value.", stepGroup.getStepGroupIterator()); //$NON-NLS-1$
 
 				error = null; message = null; steps = null;
 				try {
 					steps = stepGroup.getSteps(new TestStepContext());
 				} catch (CoreException e) {
 					error = e;
 					message = e.getLocalizedMessage();
 				}
 				assertNull("Failed to determine steps from step group. Possible cause: " + message, error); //$NON-NLS-1$
 				assertNotNull("Unexpected return value 'null'.", steps); //$NON-NLS-1$
 				assertEquals("Unexpected number of steps returned.", 0, steps.length); //$NON-NLS-1$
 			}
 
 			if (stepGroup.getId().endsWith(".stepGroup3")) { //$NON-NLS-1$
 				assertEquals("Unexpected step group label found.", "Test Step Group 3", stepGroup.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", stepGroup.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertFalse("Step group is locked but should not.", stepGroup.isLocked()); //$NON-NLS-1$
 				assertNull("Unexpected non-null value.", stepGroup.getStepGroupIterator()); //$NON-NLS-1$
 
 				error = null; message = null; steps = null;
 				try {
 					steps = stepGroup.getSteps(new TestStepContext());
 				} catch (CoreException e) {
 					error = e;
 					message = e.getLocalizedMessage();
 				}
 				assertNull("Failed to determine steps from step group. Possible cause: " + message, error); //$NON-NLS-1$
 				assertNotNull("Unexpected return value 'null'.", steps); //$NON-NLS-1$
 				assertEquals("Unexpected number of steps returned.", 4, steps.length); //$NON-NLS-1$
 
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step1", steps[0].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step2", steps[1].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step3", steps[2].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step4", steps[3].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 
 
 			if (stepGroup.getId().endsWith(".stepGroup4")) { //$NON-NLS-1$
 				assertEquals("Unexpected step group label found.", "Test Step Group 4", stepGroup.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step description found.", "", stepGroup.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertFalse("Step group is locked but should not.", stepGroup.isLocked()); //$NON-NLS-1$
 				assertNull("Unexpected non-null value.", stepGroup.getStepGroupIterator()); //$NON-NLS-1$
 
 				error = null; message = null; steps = null;
 				try {
 					steps = stepGroup.getSteps(new TestStepContext());
 				} catch (CoreException e) {
 					error = e;
 					message = e.getLocalizedMessage();
 				}
 				assertNull("Failed to determine steps from step group. Possible cause: " + message, error); //$NON-NLS-1$
 				assertNotNull("Unexpected return value 'null'.", steps); //$NON-NLS-1$
 				assertEquals("Unexpected number of steps returned.", 5, steps.length); //$NON-NLS-1$
 
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step1", steps[0].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step2", steps[1].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step3", steps[2].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step4", steps[3].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 
 				error = null; message = null; steps = null;
 				try {
 					steps = stepGroup.getSteps(new TestStepContext());
 				} catch (CoreException e) {
 					error = e;
 					message = e.getLocalizedMessage();
 				}
 				assertNull("Failed to determine steps from step group. Possible cause: " + message, error); //$NON-NLS-1$
 				assertNotNull("Unexpected return value 'null'.", steps); //$NON-NLS-1$
 				assertEquals("Unexpected number of steps returned.", 5, steps.length); //$NON-NLS-1$
 
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step1", steps[0].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step2", steps[1].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step3", steps[2].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step4", steps[3].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 				assertEquals("Unexpected step order.", "org.eclipse.tcf.te.tests.stepper.step5", steps[4].getExtension().getId()); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		assertEquals("Unexpected number of test step groups found.", 4, testStepGroupCount); //$NON-NLS-1$
 	}
 
 	public void testExecuteStepGroup() {
 		final IStepper stepper = new Stepper("testExecuteStepGroup"); //$NON-NLS-1$
 
 		IPropertiesContainer properties = new PropertiesContainer();
 
 		// Initialize the stepper
		stepper.initialize(new TestStepContext(), "org.eclipse.tcf.te.tests.stepper.stepGroup4", properties, null); //$NON-NLS-1$
 
 		ExecutorsUtil.execute(new Runnable() {
 			@Override
 			public void run() {
 				// Execute
 				try {
 					stepper.execute();
 				}
 				catch (Exception e) {
 					assertNull("Unexpected exception when executing step group", e); //$NON-NLS-1$
 				}
 			}
 		});
 
 		// Wait for the stepper to be finished
 		assertFalse("Timeout executing step group", ExecutorsUtil.waitAndExecute(10000, new IStepper.ExecutionFinishedConditionTester(stepper))); //$NON-NLS-1$
 
 		assertNotNull("Missing executed step 1", properties.getProperty("org.eclipse.tcf.te.tests.stepper.step1")); //$NON-NLS-1$ //$NON-NLS-2$
 		assertNotNull("Missing executed step 2", properties.getProperty("org.eclipse.tcf.te.tests.stepper.step2")); //$NON-NLS-1$ //$NON-NLS-2$
 		assertNotNull("Missing executed step 3", properties.getProperty("org.eclipse.tcf.te.tests.stepper.step3")); //$NON-NLS-1$ //$NON-NLS-2$
 		assertNotNull("Missing executed step 4", properties.getProperty("org.eclipse.tcf.te.tests.stepper.step4")); //$NON-NLS-1$ //$NON-NLS-2$
 		assertNotNull("Missing executed step 5", properties.getProperty("org.eclipse.tcf.te.tests.stepper.step5")); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 }
