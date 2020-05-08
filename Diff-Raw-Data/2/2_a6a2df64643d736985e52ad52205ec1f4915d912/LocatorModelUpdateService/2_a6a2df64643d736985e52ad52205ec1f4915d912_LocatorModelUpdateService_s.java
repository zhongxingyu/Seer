 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator.services;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 
 
 /**
  * Default locator model update service implementation.
  */
 public class LocatorModelUpdateService extends AbstractLocatorModelService implements ILocatorModelUpdateService {
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
 	 */
 	public LocatorModelUpdateService(ILocatorModel parentModel) {
 		super(parentModel);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.services.ILocatorModelUpdateService#add(org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.IPeerModel)
 	 */
 	@Override
 	public void add(final IPeerModel peer) {
 		Assert.isNotNull(peer);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		Map<String, IPeerModel> peers = (Map<String, IPeerModel>)getLocatorModel().getAdapter(Map.class);
 		Assert.isNotNull(peers);
 		peers.put(peer.getPeerId(), peer);
 
 		final IModelListener[] listeners = getLocatorModel().getListener();
 		if (listeners.length > 0) {
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					for (IModelListener listener : listeners) {
 						listener.locatorModelChanged(getLocatorModel(), peer, true);
 					}
 				}
 			});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.services.ILocatorModelUpdateService#remove(org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.IPeerModel)
 	 */
 	@Override
 	public void remove(final IPeerModel peer) {
 		Assert.isNotNull(peer);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		Map<String, IPeerModel> peers = (Map<String, IPeerModel>)getLocatorModel().getAdapter(Map.class);
 		Assert.isNotNull(peers);
 		peers.remove(peer.getPeerId());
 
 		getLocatorModel().setChildren(peer.getPeerId(), null);
 
 		final IModelListener[] listeners = getLocatorModel().getListener();
 		if (listeners.length > 0) {
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					for (IModelListener listener : listeners) {
 						listener.locatorModelChanged(getLocatorModel(), peer, false);
 					}
 				}
 			});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.services.ILocatorModelUpdateService#updatePeerServices(org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.IPeerModel, java.util.Collection, java.util.Collection)
 	 */
 	@Override
 	public void updatePeerServices(IPeerModel peerNode, Collection<String> localServices, Collection<String> remoteServices) {
 		Assert.isNotNull(peerNode);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		peerNode.setProperty(IPeerModelProperties.PROP_LOCAL_SERVICES, localServices != null ? makeString(localServices) : null);
 		peerNode.setProperty(IPeerModelProperties.PROP_REMOTE_SERVICES, remoteServices != null ? makeString(remoteServices) : null);
 	}
 
 	/**
 	 * Transform the given collection into a plain string.
 	 *
 	 * @param collection The collection. Must not be <code>null</code>.
 	 * @return The plain string.
 	 */
 	protected String makeString(Collection<String> collection) {
 		Assert.isNotNull(collection);
 
 		String buffer = collection.toString();
 		buffer = buffer.replaceAll("\\[", "").replaceAll("\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 
 		return buffer.trim();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService#addChild(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
 	 */
 	@Override
 	public void addChild(final IPeerModel child) {
 		Assert.isNotNull(child);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Determine the parent node
 		final IPeerModel parent = child.getParent(IPeerModel.class);
 		if (parent == null) return;
 
 		// Determine the peer id of the parent
 		String parentPeerId = parent.getPeerId();
 		Assert.isNotNull(parentPeerId);
 
 		// Get the list of existing children
 		List<IPeerModel> children = new ArrayList<IPeerModel>(getLocatorModel().getChildren(parentPeerId));
 		if (!children.contains(child)) {
 			children.add(child);
 			getLocatorModel().setChildren(parentPeerId, children);
 		}
 
 		// Notify listeners
 		parent.fireChangeEvent("changed", null, children); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService#removeChild(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
 	 */
 	@Override
 	public void removeChild(final IPeerModel child) {
 		Assert.isNotNull(child);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Determine the parent node
 		final IPeerModel parent = child.getParent(IPeerModel.class);
 		if (parent == null) return;
 
 		// Determine the peer id of the parent
 		String parentPeerId = parent.getPeerId();
 		Assert.isNotNull(parentPeerId);
 
 		// Get the list of existing children
 		List<IPeerModel> children = new ArrayList<IPeerModel>(getLocatorModel().getChildren(parentPeerId));
 		if (children.contains(child)) {
 			children.remove(child);
 			getLocatorModel().setChildren(parentPeerId, children);
 		}
 
 		// Notify listeners
 		parent.fireChangeEvent("changed", null, children); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService#mergeUserDefinedAttributes(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel, org.eclipse.tcf.protocol.IPeer, boolean)
 	 */
 	@Override
 	public void mergeUserDefinedAttributes(IPeerModel node, IPeer peer, boolean force) {
 		Assert.isNotNull(node);
 		Assert.isNotNull(peer);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// We can merge the peer attributes only if the destination peer is a AbstractPeer
 		IPeer dst = node.getPeer();
 		// If not of correct type, than we cannot update the attributes
 		if (!(dst instanceof PeerRedirector) && !(dst instanceof Peer)) return;
 		// If destination and source peer are the same objects(!) nothing to do here
 		if (dst == peer) return;
 
 		// If not forced, the peer id's of both attribute maps must be the same
 		if (!force) Assert.isTrue(dst.getID().equals(peer.getID())
 									|| (dst.getAttributes().get("remote.id.transient") != null && dst.getAttributes().get("remote.id.transient").equals(peer.getID()))); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// Get a modifiable copy of the destination peer attributes
 		Map<String, String> dstAttrs = new HashMap<String, String>(dst.getAttributes());
 
 		// Get a modifiable copy of the source peer attributes
 		Map<String, String> srcAttrs = new HashMap<String, String>(peer.getAttributes());
 
 		// Remove the URI from the destination if requested
 		boolean removeURI = srcAttrs.containsKey(IPersistableNodeProperties.PROPERTY_URI + ".remove"); //$NON-NLS-1$
 		removeURI = removeURI ? Boolean.parseBoolean(srcAttrs.remove(IPersistableNodeProperties.PROPERTY_URI + ".remove")) : false; //$NON-NLS-1$
 		if (removeURI) dstAttrs.remove(IPersistableNodeProperties.PROPERTY_URI);
 
 		// Determine the peer class
 		String peerClassSimpleName = peer.getClass().getSimpleName();
 		if (peer.getAttributes().containsKey("remote.transient")) { //$NON-NLS-1$
 			peerClassSimpleName = "RemotePeer"; //$NON-NLS-1$
 		}
 
 		// If the source is a RemotePeer and the destination not, attributes from
 		// the remote peer overwrites local attributes.
 		if ("RemotePeer".equals(peerClassSimpleName) && !"RemotePeer".equals(dst.getClass().getSimpleName())) { //$NON-NLS-1$ //$NON-NLS-2$
 			// The ID is not merged from remote to local
 			srcAttrs.remove(IPeer.ATTR_ID);
 			// The Name is not merged from remote to local
 			srcAttrs.remove(IPeer.ATTR_NAME);
 
 			// Eliminate all attributes already set in the destination attributes map
 			String merged = dstAttrs.get("remote.merged.transient"); //$NON-NLS-1$
 			for (String key : dstAttrs.keySet()) {
 				if (merged == null || !merged.contains(key)) {
 					srcAttrs.remove(key);
 				}
 			}
 		}
 
 		// Mark the peer as a remote peer and remember the remote peer id
 		if ("RemotePeer".equals(peerClassSimpleName) && !"RemotePeer".equals(dst.getClass().getSimpleName())) { //$NON-NLS-1$ //$NON-NLS-2$
 			srcAttrs.put("remote.transient", Boolean.TRUE.toString()); //$NON-NLS-1$
 			srcAttrs.put("remote.id.transient", peer.getID()); //$NON-NLS-1$
 			srcAttrs.put("remote.merged.transient", srcAttrs.keySet().toString()); //$NON-NLS-1$
 		}
 
 		// Copy all remaining attributes from source to destination
 		if (!srcAttrs.isEmpty()) {
 			dstAttrs.putAll(srcAttrs);
 		}
 
 		// If the ID's are different between the peers to merge and force is set,
 		// we have set the ID in dstAttrs to the original one as set in the destination peer.
 		if (force && !dst.getID().equals(dstAttrs.get(IPeer.ATTR_ID))) {
 			dstAttrs.put(IPeer.ATTR_ID, dst.getID());
 		}
 
 		// And update the destination peer attributes
 		if (dst instanceof PeerRedirector) {
 			((PeerRedirector)dst).updateAttributes(dstAttrs);
 		} else if (dst instanceof Peer) {
 			((Peer)dst).updateAttributes(dstAttrs);
 		}
 	}
 }
