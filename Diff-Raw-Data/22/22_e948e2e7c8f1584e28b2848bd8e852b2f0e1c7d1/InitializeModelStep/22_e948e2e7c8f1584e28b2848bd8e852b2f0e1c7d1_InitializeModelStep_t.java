 /*******************************************************************************
  * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.steps;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelChannelService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.steps.AbstractPeerNodeStep;
 import org.eclipse.tcf.te.tcf.processes.core.model.ModelManager;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 
 /**
  * Initialize the processes runtime model associated with the peer node.
  */
 public class InitializeModelStep extends AbstractPeerNodeStep {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void execute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, final ICallback callback) {
 		IPeerNode peerNode = getActivePeerModelContext(context, data, fullQualifiedId);
 		if (peerNode != null) {
 			IRuntimeModel model = ModelManager.getRuntimeModel(peerNode);
			final IModelChannelService service = model != null ? model.getService(IModelChannelService.class) : null;
 			if (service != null) {
				Runnable runnable = new Runnable() {
 					@Override
					public void run() {
						service.openChannel(new IModelChannelService.DoneOpenChannel() {
							@Override
							public void doneOpenChannel(Throwable error, IChannel channel) {
								callback.done(InitializeModelStep.this, StatusHelper.getStatus(error));
							}
						});
 					}
				};

				Protocol.invokeLater(runnable);
 			} else {
 				callback.done(InitializeModelStep.this, Status.OK_STATUS);
 			}
 		} else {
 			callback.done(InitializeModelStep.this, Status.OK_STATUS);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void validateExecute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
 	}
 
 }
