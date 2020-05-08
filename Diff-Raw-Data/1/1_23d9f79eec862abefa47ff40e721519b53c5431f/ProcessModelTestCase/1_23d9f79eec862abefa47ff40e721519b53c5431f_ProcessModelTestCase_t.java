 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.processes.model;
 
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.core.async.AsyncCallbackCollector;
 import org.eclipse.tcf.te.core.async.AsyncCallbackHandler;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.tcf.core.async.CallbackInvocationDelegate;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.processes.core.model.ModelManager;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tests.tcf.TcfTestCase;
 
 /**
  * Process model test cases.
  */
 public class ProcessModelTestCase extends TcfTestCase {
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("Test TCF process monitor model"); //$NON-NLS-1$
 
 			// add ourself to the test suite
 			testSuite.addTestSuite(ProcessModelTestCase.class);
 
 		return testSuite;
 	}
 
 	//***** BEGIN SECTION: Single test methods *****
 	//NOTE: All method which represents a single test case must
 	//      start with 'test'!
 
 	public void testProcessModel() {
 		assertNotNull("Test peer missing.", peer); //$NON-NLS-1$
 		assertNotNull("Test peer model missing.", peerModel); //$NON-NLS-1$
 
 		// Get the process model for the test peer model
 		final IRuntimeModel model = ModelManager.getRuntimeModel(peerModel);
 		assertNotNull("Failed to get runtime model for peer model.", model); //$NON-NLS-1$
 
 		// Create a callback handler to receive all callbacks necessary to
 		// traverse through the model
 		final AsyncCallbackHandler handler = new AsyncCallbackHandler();
 		assertNotNull("Failed to create asynchronous callback handler.", handler); //$NON-NLS-1$
 
 		final AtomicReference<IStatus> statusRef = new AtomicReference<IStatus>();
 
 		final Callback callback = new Callback() {
 			@Override
             protected void internalDone(Object caller, IStatus status) {
 				statusRef.set(status);
 				handler.removeCallback(this);
 			}
 		};
		handler.addCallback(callback);
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				final AsyncCallbackCollector collector = new AsyncCallbackCollector(new Callback() {
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						callback.done(caller, status != null ? status : Status.OK_STATUS);
 					}
 				}, new CallbackInvocationDelegate());
 
 				// Refresh the whole model from the top
 				final ICallback c1 = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 				model.getService(IModelRefreshService.class).refresh(new Callback() {
 					/* (non-Javadoc)
 					 * @see org.eclipse.tcf.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
 					 */
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						if (status.getSeverity() != IStatus.ERROR) {
 							// Get all processes, loop over them and refresh each of it
 							List<IProcessContextNode> processes = model.getChildren(IProcessContextNode.class);
 							for (IProcessContextNode process : processes) {
 								final ICallback c2 = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 								model.getService(IModelRefreshService.class).refresh(process, c2);
 							}
 						}
 						c1.done(caller, status);
 					}
 				});
 
 				collector.initDone();
 			}
 		};
 
 		Protocol.invokeLater(runnable);
 
 		waitAndDispatch(0, handler.getConditionTester());
 
 		IStatus status = statusRef.get();
 		assertNotNull("Missing return status.", status); //$NON-NLS-1$
 		assertFalse("Process runtime model refresh failed. Possible cause: " + status.getMessage(), status.getSeverity() == IStatus.ERROR); //$NON-NLS-1$
 
 		ModelManager.disposeRuntimeModel(peerModel);
 	}
 
 	//***** END SECTION: Single test methods *****
 }
