 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.internal.adapters;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelPeerNodeQueryService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.ui.views.editor.EditorInput;
 import org.eclipse.ui.IElementFactory;
 import org.eclipse.ui.IMemento;
 
 /**
  * The element factory to create an peer model editor input from a memento which is read
  * from an external persistent storage and holds a peer id.
  */
 public class PeerModelFactory implements IElementFactory {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public IAdaptable createElement(IMemento memento) {
 		final AtomicReference<IPeerModel> node = new AtomicReference<IPeerModel>();
 		final String peerId = memento.getString("peerId"); //$NON-NLS-1$
 		if (peerId != null) {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					node.set(Model.getModel().getService(ILocatorModelLookupService.class).lkupPeerModelById(peerId));
 				}
 			};
 
 			Assert.isTrue(!Protocol.isDispatchThread());
 			Protocol.invokeAndWait(runnable);
 
 			if (node.get() != null) {
 				ILocatorModel model = node.get().getModel();
 				ILocatorModelPeerNodeQueryService queryService = model.getService(ILocatorModelPeerNodeQueryService.class);
 				queryService.queryRemoteServices(node.get());
 			}
 		}
 
 		return node.get() != null ? new EditorInput(node.get()) : null;
 	}
 }
