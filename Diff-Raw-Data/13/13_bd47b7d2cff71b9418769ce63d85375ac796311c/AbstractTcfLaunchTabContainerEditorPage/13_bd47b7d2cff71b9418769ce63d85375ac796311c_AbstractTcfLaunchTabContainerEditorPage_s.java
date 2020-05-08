 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.launch.ui.editor;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.tcf.core.AbstractPeer;
 import org.eclipse.tcf.core.TransientPeer;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
 import org.eclipse.tcf.te.launch.core.lm.LaunchSpecification;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
 import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
 import org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 
 /**
  * TCF launch configuration tab container page implementation.
  */
 public abstract class AbstractTcfLaunchTabContainerEditorPage extends AbstractLaunchTabContainerEditorPage {
 
 	protected static final String PROP_LAUNCH_CONFIG_WC = "launchConfigWorkingCopy.transient.silent"; //$NON-NLS-1$
 	protected static final String PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES = "launchConfigAttributes.transient.silent"; //$NON-NLS-1$
 
 	/**
 	 * Get the peer model from the editor input.
 	 * @param input The editor input.
 	 * @return The peer model.
 	 */
 	IPeerModel getPeerModel(Object input) {
 		return (IPeerModel)((IAdaptable)input).getAdapter(IPeerModel.class);
 	}
 
 	/**
 	 * Get the launch configuration from the peer model.
 	 * @param peerModel The peer model.
 	 * @return The launch configuration.
 	 */
 	ILaunchConfigurationWorkingCopy getLaunchConfig(final IPeerModel peerModel) {
 		ILaunchConfigurationWorkingCopy wc = null;
 		if (peerModel != null) {
 			IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 			Assert.isNotNull(service);
 			if (service.getProperty(peerModel, PROP_LAUNCH_CONFIG_WC) instanceof ILaunchConfigurationWorkingCopy) {
 				wc = (ILaunchConfigurationWorkingCopy)service.getProperty(peerModel, PROP_LAUNCH_CONFIG_WC);
 			}
 			else {
 				String launchConfigAttributes = peerModel.getPeer().getAttributes().get(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES);
 				ILaunchSpecification spec = new LaunchSpecification(ILaunchTypes.ATTACH, ILaunchManager.DEBUG_MODE);
 				LaunchContextsPersistenceDelegate.setLaunchContexts(spec, new IModelNode[]{peerModel});
 				try {
 					wc = LaunchManager.getInstance().getLaunchConfiguration(spec, true).getWorkingCopy();
 					LaunchContextsPersistenceDelegate.setLaunchContexts(wc, null);
 					IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(wc, String.class, false);
 					if (launchConfigAttributes != null && launchConfigAttributes.trim().length() > 0) {
 						delegate.read(wc, launchConfigAttributes, null);
 					}
 					launchConfigAttributes = (String)delegate.write(wc, String.class, null);
 					service.setProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES, launchConfigAttributes);
 					service.setProperty(peerModel, PROP_LAUNCH_CONFIG_WC, wc);
 				}
 				catch (Exception e) {
 				}
 			}
 		}
 		return wc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#setupData(java.lang.Object)
 	 */
 	@Override
 	public boolean setupData(Object input) {
 		ILaunchConfigurationWorkingCopy wc = getLaunchConfig(getPeerModel(input));
 		if (wc != null) {
 			getLaunchConfigurationTab().initializeFrom(wc);
 			checkLaunchConfigDirty();
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#extractData()
 	 */
 	@Override
 	public boolean extractData() {
 		ILaunchConfigurationWorkingCopy wc = getLaunchConfig(getPeerModel(getEditorInput()));
 		if (wc != null && checkLaunchConfigDirty()) {
 			getLaunchConfigurationTab().performApply(wc);
 			IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(wc, String.class, false);
 			try {
 				final String launchConfigAttributes = (String)delegate.write(wc, String.class, null);
 				final IPeerModel peerModel = getPeerModel(getEditorInput());
 				IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 				service.setProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES, launchConfigAttributes);
 				if (peerModel != null) {
 					Protocol.invokeAndWait(new Runnable() {
 						@Override
 						public void run() {
 							IPeer oldPeer = peerModel.getPeer();
 							Map<String, String> attributes = new HashMap<String, String>(peerModel.getPeer().getAttributes());
 							attributes.put(IPeerModelProperties.PROP_LAUNCH_CONFIG_ATTRIBUTES, launchConfigAttributes);
 							IPeer newPeer = new Peer(attributes);
							if (oldPeer instanceof TransientPeer && !(oldPeer instanceof AbstractPeer || oldPeer instanceof PeerRedirector || oldPeer instanceof Peer)) {
 								peerModel.setProperty(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties.PROP_INSTANCE, newPeer);
 							} else {
 								Model.getModel().getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(peerModel, newPeer, false);
 							}
 							checkLaunchConfigDirty();
 						}
 					});
 					return true;
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if the launch config has changed.
 	 * If it has changed, the page is set dirty.
 	 * @return <code>true</code> if the launch config has changed since last save.
 	 */
 	public boolean checkLaunchConfigDirty() {
 		boolean dirty = false;
 		IPeerModel peerModel = getPeerModel(getEditorInput());
 		IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 		String oldLaunchConfigAttributes = (String)service.getProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES);
 		IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(getLaunchConfig(peerModel), String.class, false);
 		String launchConfigAttributes = null;
 		try {
 			launchConfigAttributes = (String)delegate.write(getLaunchConfig(peerModel), String.class, null);
 			dirty = !launchConfigAttributes.equals(oldLaunchConfigAttributes);
 		}
 		catch (Exception e) {
 		}
 
 		setDirty(dirty);
 		getManagedForm().dirtyStateChanged();
 		return dirty;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		IPeerModel peerModel = getPeerModel(getEditorInput());
 		IPropertiesAccessService service = ServiceManager.getInstance().getService(peerModel, IPropertiesAccessService.class);
 		service.setProperty(peerModel, PROP_ORIGINAL_LAUNCH_CONFIG_ATTRIBUTES, null);
 		service.setProperty(peerModel, PROP_LAUNCH_CONFIG_WC, null);
 	}
 }
