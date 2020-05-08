 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper;
 import org.eclipse.tcf.te.runtime.stepper.stepper.Stepper;
 import org.eclipse.tcf.te.tcf.core.interfaces.steps.ITcfStepAttributes;
 import org.eclipse.tcf.te.tests.tcf.TcfTestCase;
 
 /**
  * TCF Stepper tests.
  */
 public class TcfStepperTests extends TcfTestCase {
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("TCF Stepper tests"); //$NON-NLS-1$
 
 		// add ourself to the test suite
 		testSuite.addTestSuite(TcfStepperTests.class);
 
 		return testSuite;
 	}
 
 	public void testChannelSteps() {
 		assertNotNull("Precondition Failure: peer model is not available.", peerNode); //$NON-NLS-1$
 
 		final IStepper stepper = new Stepper("testExecuteStepGroup"); //$NON-NLS-1$
 
 		IPropertiesContainer properties = new PropertiesContainer();
 		IStepContext context = (IStepContext)Platform.getAdapterManager().getAdapter(peerNode, IStepContext.class);
 		assertNotNull("Failed to get step context adapter for peer model.", context); //$NON-NLS-1$
 
 		// Initialize the stepper
 		stepper.initialize(context, "org.eclipse.tcf.te.tests.channelStepTests", properties, null); //$NON-NLS-1$
 
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
 		assertFalse("Timeout executing step group", ExecutorsUtil.waitAndExecute(0, new IStepper.ExecutionFinishedConditionTester(stepper))); //$NON-NLS-1$
 
 		IChannel channel = (IChannel)properties.getProperty(ITcfStepAttributes.ATTR_CHANNEL);
 		assertNotNull("Failed to create channel.", channel); //$NON-NLS-1$
 		assertTrue("Failed to open channel.", properties.getBooleanProperty("ValidateChannelStep.result")); //$NON-NLS-1$ //$NON-NLS-2$
 		assertTrue("Failed to close channel.", channel.getState() == IChannel.STATE_CLOSED); //$NON-NLS-1$
 	}
 
 }
