 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.launch.ui.internal.services;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
 import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
 import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.services.AbstractService;
 import org.eclipse.tcf.te.runtime.services.interfaces.IDebugService;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;
 
 /**
  * Debug service implementations for TCF contexts.
  */
 public class DebugService extends AbstractService implements IDebugService {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.services.interfaces.IDebugService#attach(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void attach(final Object context, final IPropertiesContainer data, final ICallback callback) {
 		if (!Protocol.isDispatchThread()) {
 			internalAttach(context, data, callback);
 		}
 		else {
 			ExecutorsUtil.execute(new Runnable() {
 				@Override
 				public void run() {
 					internalAttach(context, data, callback);
 				}
 			});
 		}
 	}
 
 	protected void internalAttach(final Object context, final IPropertiesContainer data, final ICallback callback) {
 		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		Assert.isNotNull(context);
 		Assert.isNotNull(data);
 		Assert.isNotNull(callback);
 
 		if (context instanceof IModelNode) {
 			ILaunchConfigurationType launchConfigType =	DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ILaunchTypes.ATTACH);
 			try {
 				ILaunchSelection launchSelection = new LaunchSelection(ILaunchManager.DEBUG_MODE, new RemoteSelectionContext((IModelNode)context, true));
 				ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(launchConfigType, ILaunchManager.DEBUG_MODE);
 				if (delegate != null) {
 					// create an empty launch configuration specification to initialize all attributes with their default defaults.
 					ILaunchSpecification launchSpec = delegate.getLaunchSpecification(launchConfigType.getIdentifier(), launchSelection);
 					for (String key : data.getProperties().keySet()) {
 						launchSpec.addAttribute(key, data.getProperty(key));
 					}
 					delegate.validate(launchSpec);
 					if (launchSpec != null && launchSpec.isValid()) {
 						ILaunchConfiguration[] launchConfigs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(launchConfigType);
 						launchConfigs = delegate.getMatchingLaunchConfigurations(launchSpec, launchConfigs);
 
 						ILaunchConfiguration config = launchConfigs != null && launchConfigs.length > 0 ? launchConfigs[0] : null;
 
 						boolean skip = false;
 						if (config != null) {
 
 							ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
 							for (ILaunch launch : launches) {
								if (launch.getLaunchConfiguration().getType().getIdentifier().equals(ILaunchTypes.ATTACH) && !launch.isTerminated()) {
 									IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launch.getLaunchConfiguration());
 									if (contexts != null && contexts.length == 1 && contexts[0].equals(context)) {
 										skip = true;
 									}
 								}
 							}
 						}
 
 						if (!skip) {
 							config = LaunchManager.getInstance().createOrUpdateLaunchConfiguration(config, launchSpec);
 
 							delegate.validate(ILaunchManager.DEBUG_MODE, config);
 							DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
 						}
 					}
 				}
 				callback.done(this, Status.OK_STATUS);
 			}
 			catch (Exception e) {
 				callback.done(this, StatusHelper.getStatus(e));
 			}
 		}
 		else {
 			callback.done(this, Status.OK_STATUS);
 		}
 	}
 }
