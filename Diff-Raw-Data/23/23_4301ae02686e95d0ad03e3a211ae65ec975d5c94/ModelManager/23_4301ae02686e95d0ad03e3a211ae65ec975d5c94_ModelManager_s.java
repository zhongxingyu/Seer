 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator.model;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.IPeerModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.nodes.LocatorModel;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;
 
 
 /**
  * Helper class to instantiate and initialize the peer and locator model.
  */
 public final class ModelManager {
 	// Reference to the peer model
	/* default */ static volatile IPeerModel peerModel;
 
 	// Reference to the locator model
	/* default */ static volatile ILocatorModel locatorModel;
 
 	/**
 	 * Returns the shared peer model instance.
 	 * <p>
 	 * If the shared peer model instance has not been yet initialized,
 	 * the method does initialize the shared peer model instance.
 	 *
 	 * @return The shared peer model.
 	 */
 	public static IPeerModel getPeerModel() {
 		return getPeerModel(false);
 	}
 
 	/**
 	 * Returns the shared peer model instance.
 	 * <p>
 	 * If the shared peer model instance has not been yet initialized,
 	 * and <code>shutdown</code> is <code>false</code>, the method does
 	 * initialize the shared peer model instance.
 	 *
 	 * @param shutdown <code>True</code> if the method is called during shutdown and
 	 *                 the model should not be initialized if not done anyway. <code>
 	 *                 false</code> in any other case.
 	 *
 	 * @return The shared peer model.
 	 */
 	public static IPeerModel getPeerModel(boolean shutdown) {
 		// Access to the locator model must happen in the TCF dispatch thread
 		if (peerModel == null && !shutdown) {
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					initialize();
 				}
 			});
 		}
 		return peerModel;
 	}
 
 	/**
 	 * Returns the shared locator model instance.
 	 * <p>
 	 * If the shared locator model instance has not been yet initialized,
 	 * the method does initialize the shared locator model instance.
 	 *
 	 * @return The shared locator model.
 	 */
 	public static ILocatorModel getLocatorModel() {
 		return getLocatorModel(false);
 	}
 
 	/**
 	 * Returns the shared locator model instance.
 	 * <p>
 	 * If the shared locator model instance has not been yet initialized,
 	 * and <code>shutdown</code> is <code>false</code>, the method does
 	 * initialize the shared locator model instance.
 	 *
 	 * @param shutdown <code>True</code> if the method is called during shutdown and
 	 *                 the model should not be initialized if not done anyway. <code>
 	 *                 false</code> in any other case.
 	 *
 	 * @return The shared locator model.
 	 */
 	public static ILocatorModel getLocatorModel(boolean shutdown) {
 		// Access to the locator model must happen in the TCF dispatch thread
 		if (locatorModel == null && !shutdown) {
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					initialize();
 				}
 			});
 		}
 		return locatorModel;
 	}
 
 	/**
 	 * Initialize the root node. Must be called within the TCF dispatch thread.
 	 */
 	protected static void initialize() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// If peer model is set in the mean while, initialize got
 		// called twice. Return immediately in this case.
 		if (peerModel == null) {
 			// Create the model instance
			peerModel = new PeerModel();
 			// Refresh the model right away
			peerModel.getService(IPeerModelRefreshService.class).refresh(null);
 		}
 
 		// If locator model is set in the mean while, initialize got
 		// called twice. Return immediately in this case.
 		if (locatorModel == null) {
 			// Create the model instance
			locatorModel = new LocatorModel();
 			// Refresh the model right away
			locatorModel.getService(ILocatorModelRefreshService.class).refresh(null);
			((LocatorModel)locatorModel).checkLocatorListener();
 		}
 	}
 
 	/**
 	 * Dispose the root node.
 	 */
 	public static void dispose() {
 		if (peerModel != null) {
 			// Access to the peer model must happen in the TCF dispatch thread
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					peerModel.dispose();
 				}
 			});
 			peerModel = null;
 		}
 
 		if (locatorModel != null) {
 			// Access to the locator model must happen in the TCF dispatch thread
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					locatorModel.dispose();
 				}
 			});
 			locatorModel = null;
 		}
 	}
 
 }
