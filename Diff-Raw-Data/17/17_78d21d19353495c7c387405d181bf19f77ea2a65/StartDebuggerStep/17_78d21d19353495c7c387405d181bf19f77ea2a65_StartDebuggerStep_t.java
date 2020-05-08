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
 
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IDebugService;
 import org.eclipse.tcf.te.runtime.services.interfaces.IDelegateService;
 import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IStepAttributes;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.IPeerModelQueryService;
 
 /**
  * Start debugger step implementation.
  */
 public class StartDebuggerStep extends AbstractPeerNodeStep {
 
 	/**
 	 * Interface to be implemented by start debugger step delegates.
 	 */
 	public static interface IDelegate {
 
 		/**
 		 * Called once the debugger has been attached.
 		 *
 		 * @param node The peer model node. Must not be <code>null</code>.
 		 * @param monitor The progress monitor. Must not be <code>null</code>.
 		 * @param callback The callback to invoke if finished. Must not be <code>null</code>.
 		 */
 		public void postAttachDebugger(IPeerNode node, IProgressMonitor monitor, ICallback callback);
 	}
 
 	/**
 	 * Constructor.
 	 */
 	public StartDebuggerStep() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
     public void validateExecute(final IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, final IProgressMonitor monitor) throws CoreException {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
     public void execute(final IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, final IProgressMonitor monitor, final ICallback callback) {
 		final IPeerNode node = getActivePeerModelContext(context, data, fullQualifiedId);
 		Assert.isNotNull(node);
 		String value = getParameters().get("autoAttachAll"); //$NON-NLS-1$
 		final boolean autoAttachAll = value != null ? Boolean.parseBoolean(value) : false;
 
 		if (StepperAttributeUtil.getBooleanProperty(IStepAttributes.ATTR_START_DEBUGGER, fullQualifiedId, data)) {
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					// Don't attach the debugger if no run control is provided by the target
 					final IPeerModelQueryService queryService = node.getModel().getService(IPeerModelQueryService.class);
 					Assert.isNotNull(queryService);
 					String remoteServices = node.getStringProperty(IPeerNodeProperties.PROP_REMOTE_SERVICES);
 					Assert.isNotNull(remoteServices);
 					boolean canAttachDbg = false;
 					StringTokenizer tokenizer = new StringTokenizer(remoteServices, ","); //$NON-NLS-1$
 					while (tokenizer.hasMoreTokens()) {
 						String svc = tokenizer.nextToken().trim();
 						if (IRunControl.NAME.equals(svc)) {
 							canAttachDbg = true;
 							break;
 						}
 					}
 
 					if (canAttachDbg) {
 						Runnable runnable = new Runnable() {
 							@Override
 							public void run() {
 								IDebugService dbgService = ServiceManager.getInstance().getService(node, IDebugService.class, false);
 								if (dbgService != null) {
 									// Attach the debugger
 									IPropertiesContainer props = new PropertiesContainer();
 									dbgService.attach(node, props, monitor, new Callback() {
 										@Override
                                         protected void internalDone(Object caller, IStatus status) {
											if ((status == null || status.isOK()) && autoAttachAll) {
 												// Check if there is a delegate registered
 												IDelegateService service = ServiceManager.getInstance().getService(node, IDelegateService.class, false);
 												IDelegate delegate = service != null ? service.getDelegate(node, IDelegate.class) : null;
 
 												if (delegate != null) {
 													delegate.postAttachDebugger(node, monitor, callback);
 												} else {
 													callback(data, fullQualifiedId, callback, status, null);
 												}
 											} else {
 												callback(data, fullQualifiedId, callback, status, null);
 											}
 										}
 									});
 								}
 								else {
 									callback(data, fullQualifiedId, callback, Status.OK_STATUS, null);
 								}
 							}
 						};
 						Protocol.invokeLater(runnable);
 					} else {
 						callback(data, fullQualifiedId, callback, Status.OK_STATUS, null);
 					}
 				}
 			});
 		}
 		else {
 			callback(data, fullQualifiedId, callback, Status.OK_STATUS, null);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.steps.AbstractStep#rollback(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IStatus, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void rollback(IStepContext context, IPropertiesContainer data, IStatus status, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
 		final IPeerNode node = getActivePeerModelContext(context, data, fullQualifiedId);
 		IDebugService dbgService = ServiceManager.getInstance().getService(node, IDebugService.class, false);
 		if (dbgService != null) {
 			IPropertiesContainer props = new PropertiesContainer();
 			dbgService.detach(node, props, null, callback);
 		}
 		else {
 			callback(data, fullQualifiedId, callback, Status.OK_STATUS, null);
 		}
 	}
 }
