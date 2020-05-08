 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.tcf.locator.steps;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 
 /**
  * Set the waiting_for_ready state.
  */
 public class SetWaitForReadyStep extends AbstractPeerModelStep {
 
 	/**
 	 * Constructor.
 	 */
 	public SetWaitForReadyStep() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void validateExecute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void execute(final IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, final IProgressMonitor monitor, final ICallback callback) {
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				int state = getActivePeerModelContext(context, data, fullQualifiedId).getIntProperty(IPeerModelProperties.PROP_STATE);
				if (state == IPeerModelProperties.STATE_UNKNOWN || state == IPeerModelProperties.STATE_NOT_REACHABLE || state == IPeerModelProperties.STATE_ERROR) {
 					getActivePeerModelContext(context, data, fullQualifiedId).setProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_WAITING_FOR_READY);
 				}
 			}
 		});
 		callback.done(this, Status.OK_STATUS);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.steps.AbstractStep#getTotalWork(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
 	public int getTotalWork(IStepContext context, IPropertiesContainer data) {
 	    return 100;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.steps.AbstractStep#rollback(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IStatus, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void rollback(final IStepContext context, final IPropertiesContainer data, IStatus status, final IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
                 getActivePeerModelContext(context, data, fullQualifiedId).setProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_NOT_REACHABLE);
 			}
 		});
 		super.rollback(context, data, status, fullQualifiedId, monitor, callback);
 	}
 }
