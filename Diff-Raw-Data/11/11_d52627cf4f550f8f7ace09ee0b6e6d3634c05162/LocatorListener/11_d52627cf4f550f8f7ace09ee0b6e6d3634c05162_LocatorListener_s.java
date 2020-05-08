 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator.listener;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.tcf.core.AbstractPeer;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.ILocator;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.locator.ScannerRunnable;
 import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
 import org.eclipse.tcf.te.tcf.locator.interfaces.ITracing;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 
 
 /**
  * Locator listener implementation.
  */
 public class LocatorListener implements ILocator.LocatorListener {
 	// Reference to the parent model
 	/* default */ final ILocatorModel model;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param model The parent locator model. Must not be <code>null</code>.
 	 */
 	public LocatorListener(ILocatorModel model) {
 		super();
 
 		Assert.isNotNull(model);
 		this.model = model;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.services.ILocator.LocatorListener#peerAdded(org.eclipse.tcf.protocol.IPeer)
 	 */
 	@Override
 	public void peerAdded(IPeer peer) {
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_LISTENER)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorListener.peerAdded( " + (peer != null ? peer.getID() : null) + " )", ITracing.ID_TRACE_LOCATOR_LISTENER, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		if (model != null && peer != null) {
 			// find the corresponding model node to remove (expected to be null)
 			IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
 			if (peerNode == null) {
 				// Double check with "ClientID" if set
 				String clientID = peer.getAttributes().get("ClientID"); //$NON-NLS-1$
 				if (clientID != null) {
 					peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(clientID);
 				}
 			}
 			// If not found, create a new peer node instance
 			if (peerNode == null) {
 				peerNode = new PeerModel(model, peer);
 				// Validate the peer node before adding
 				peerNode = model.validatePeerNodeForAdd(peerNode);
 				// Add the peer node to the model
 				if (peerNode != null) {
 					// If there are reachable static peers without an agent ID associated or
 					// static peers with unknown link state, refresh the agent ID's first.
 					List<IPeerModel> nodes = new ArrayList<IPeerModel>();
 
 					for (IPeerModel node : model.getPeers()) {
 						// Skip static nodes
 						if (!node.isStatic()) continue;
 						// We expect the agent ID to be set
 						if (node.getPeer().getAgentID() == null || "".equals(node.getPeer().getAgentID())) { //$NON-NLS-1$
 							nodes.add(node);
 						}
 					}
 
 					// Create the runnable to execute after the agent ID refresh (if needed)
 					final IPeerModel finPeerNode = peerNode;
 					final IPeer finPeer = peer;
 					final Runnable runnable = new Runnable() {
 						@Override
 						public void run() {
 							IPeerModel[] matches = model.getService(ILocatorModelLookupService.class).lkupMatchingStaticPeerModels(finPeerNode);
 							if (matches.length == 0) {
 								model.getService(ILocatorModelUpdateService.class).add(finPeerNode);
 								// And schedule for immediate status update
 								Runnable runnable2 = new ScannerRunnable(model.getScanner(), finPeerNode);
 								Protocol.invokeLater(runnable2);
 							} else {
 								for (IPeerModel match : matches) {
 									IPeer myPeer = model.validatePeer(finPeer);
 									if (myPeer != null) {
 										boolean changed = match.setChangeEventsEnabled(false);
 										// Merge user configured properties between the peers
 										model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(match, myPeer, true);
 										if (changed) match.setChangeEventsEnabled(true);
 										match.fireChangeEvent(IPeerModelProperties.PROP_INSTANCE, myPeer, match.getPeer());
 										// And schedule for immediate status update
 										Runnable runnable2 = new ScannerRunnable(model.getScanner(), match);
 										Protocol.invokeLater(runnable2);
 									}
 								}
 							}
 						}
 					};
 
 					if (nodes.size() > 0) {
 						// Refresh the agent ID's first
 						model.getService(ILocatorModelRefreshService.class).refreshAgentIDs(nodes.toArray(new IPeerModel[nodes.size()]), new Callback() {
 							@Override
 							protected void internalDone(Object caller, IStatus status) {
 								// Ignore errors
 								runnable.run();
 							}
 						});
 					} else {
 						// No need to refresh the agent ID's -> run runnable
 						runnable.run();
 					}
 				}
 			} else {
 				// Peer node found, update the peer instance
 				boolean isStatic = peerNode.isStatic();
 				if (isStatic) {
 					// Validate the peer node before updating
 					IPeer myPeer = model.validatePeer(peer);
 					if (myPeer != null) {
 						boolean changed = peerNode.setChangeEventsEnabled(false);
 						// Merge user configured properties between the peers
 						model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(peerNode, myPeer, true);
 						if (changed) peerNode.setChangeEventsEnabled(true);
 						peerNode.fireChangeEvent(IPeerModelProperties.PROP_INSTANCE, myPeer, peerNode.getPeer());
 						// And schedule for immediate status update
 						Runnable runnable = new ScannerRunnable(model.getScanner(), peerNode);
 						Protocol.invokeLater(runnable);
 					}
 				} else {
 					peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
 					// And schedule for immediate status update
 					Runnable runnable = new ScannerRunnable(model.getScanner(), peerNode);
 					Protocol.invokeLater(runnable);
 				}
 			}
 		}
 	}
 
 	// Map of guardian objects per peer
 	private final Map<IPeer, AtomicBoolean> PEER_CHANGED_GUARDIANS = new HashMap<IPeer, AtomicBoolean>();
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.services.ILocator.LocatorListener#peerChanged(org.eclipse.tcf.protocol.IPeer)
 	 */
 	@Override
 	public void peerChanged(IPeer peer) {
 		// Protect ourself from reentrant calls while processing a changed peer.
 		if (peer != null) {
 			AtomicBoolean guard = PEER_CHANGED_GUARDIANS.get(peer);
 			if (guard != null && guard.get()) return;
 			if (guard != null) guard.set(true);
 			else PEER_CHANGED_GUARDIANS.put(peer, new AtomicBoolean(true));
 		}
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_LISTENER)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorListener.peerChanged( " + (peer != null ? peer.getID() : null) + " )", ITracing.ID_TRACE_LOCATOR_LISTENER, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		if (model != null && peer != null) {
 			// find the corresponding model node to remove
 			IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
 			if (peerNode == null) {
 				// Double check with "ClientID" if set
 				String clientID = peer.getAttributes().get("ClientID"); //$NON-NLS-1$
 				if (clientID != null) {
 					peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(clientID);
 				}
 			}
 			// Update the peer instance
 			if (peerNode != null) {
 			    // Get the old peer instance
 			    IPeer oldPeer = peerNode.getPeer();
 			    // If old peer and new peer instance are the same _objects_, nothing to do
 			    if (oldPeer != peer) {
 			    	// Peers visible to the locator are replaced with the new instance
 			    	if (oldPeer instanceof AbstractPeer) {
 			    		peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
 			    	}
 			    	// Non-visible peers are updated
 			    	else {
 						// Validate the peer node before updating
 						IPeer myPeer = model.validatePeer(peer);
 						if (myPeer != null) {
 							boolean changed = peerNode.setChangeEventsEnabled(false);
 							// Merge user configured properties between the peers
 							model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(peerNode, myPeer, true);
 							if (changed) peerNode.setChangeEventsEnabled(true);
 							peerNode.fireChangeEvent(IPeerModelProperties.PROP_INSTANCE, myPeer, peerNode.getPeer());
 						}
 			    	}
 			    }
 			}
 			// Refresh static peers and merge attributes if required
 			model.getService(ILocatorModelRefreshService.class).refreshStaticPeers();
 		}
 
 		// Clean up the guardians
 		if (peer != null) PEER_CHANGED_GUARDIANS.remove(peer);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.services.ILocator.LocatorListener#peerRemoved(java.lang.String)
 	 */
 	@Override
 	public void peerRemoved(String id) {
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_LISTENER)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorListener.peerRemoved( " + id + " )", ITracing.ID_TRACE_LOCATOR_LISTENER, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		if (model != null && id != null) {
 			// find the corresponding model node to remove
 			IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(id);
 
 			// If we cannot find a model node, it is probably because the remove is sent for the
 			// non-loopback addresses of the localhost. We have to double check this.
 			if (peerNode == null) {
 				int beginIndex = id.indexOf(':');
 				int endIndex = id.lastIndexOf(':');
 				String ip = id.substring(beginIndex+1, endIndex);
 
 				// Get the loopback address
 				String loopback = IPAddressUtil.getInstance().getIPv4LoopbackAddress();
 				// Empty IP address means loopback
 				if ("".equals(ip)) ip = loopback; //$NON-NLS-1$
 				else {
 					if (IPAddressUtil.getInstance().isLocalHost(ip)) {
 						ip = loopback;
 					}
 				}
 				// Build up the new id to lookup
 				StringBuilder newId = new StringBuilder();
 				newId.append(id.substring(0, beginIndex));
 				newId.append(':');
 				newId.append(ip);
 				newId.append(':');
 				newId.append(id.substring(endIndex));
 
 				// Try the lookup again
 				peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(newId.toString());
 			}
 
 			// If the model node is found in the model, process the removal.
 			if (peerNode != null) {
 				if (peerNode.isStatic()) {
 					boolean changed = peerNode.setChangeEventsEnabled(false);
 					IPeer peer = peerNode.getPeer();
 
 					// Create a modifiable copy of the peer attributes
 					Map<String, String> attrs = new HashMap<String, String>(peerNode.getPeer().getAttributes());
 					// Remember the remote peer id before removing it
 					String remotePeerID = attrs.get("remote.id.transient"); //$NON-NLS-1$
 
 					// Remove all merged attributes from the peer instance
 					String merged = attrs.remove("remote.merged.transient"); //$NON-NLS-1$
 					if (merged != null) {
 						merged = merged.replace('[', ' ').replace(']', ' ').trim();
 						List<String> keysToRemove = Arrays.asList(merged.split(",\\ ")); //$NON-NLS-1$
 						String[] keys = attrs.keySet().toArray(new String[attrs.keySet().size()]);
 						for (String key : keys) {
 							if (keysToRemove.contains(key)) {
 								attrs.remove(key);
 							}
 						}
 
 						// Make sure the ID is set correctly
 						if (attrs.get(IPeer.ATTR_ID) == null) {
 							attrs.put(IPeer.ATTR_ID, peer.getID());
 						}
 
 						// Update the peer attributes
 						if (peer instanceof PeerRedirector) {
 							((PeerRedirector)peer).updateAttributes(attrs);
 						} else if (peer instanceof Peer) {
 							((Peer)peer).updateAttributes(attrs);
 						}
 					}
 
 					// Remove the attributes stored at peer node level
 					peerNode.setProperty(IPeerModelProperties.PROP_LOCAL_SERVICES, null);
 					peerNode.setProperty(IPeerModelProperties.PROP_REMOTE_SERVICES, null);
 
 					// Check if we have to remote the peer in the underlying locator service too
 					if (remotePeerID != null) {
 				        Map<String, IPeer> peers = Protocol.getLocator().getPeers();
 				        IPeer remotePeer = peers.get(remotePeerID);
 				        if (remotePeer instanceof AbstractPeer) ((AbstractPeer)remotePeer).dispose();
 					}
 
 					if (changed) peerNode.setChangeEventsEnabled(true);
 					peerNode.fireChangeEvent(IPeerModelProperties.PROP_INSTANCE, peer, peerNode.getPeer());
 
 					final IModelListener[] listeners = model.getListener();
 					if (listeners.length > 0) {
 						final IPeerModel finPeerNode = peerNode;
 						Protocol.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								for (IModelListener listener : listeners) {
 									listener.locatorModelChanged(model, finPeerNode, false);
 								}
 							}
 						});
 					}
 				} else {
 					// Dynamic peer -> Remove peer model node from the model
 					model.getService(ILocatorModelUpdateService.class).remove(peerNode);
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.services.ILocator.LocatorListener#peerHeartBeat(java.lang.String)
 	 */
 	@Override
 	public void peerHeartBeat(String id) {
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_LISTENER)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorListener.peerHeartBeat( " + id + " )", ITracing.ID_TRACE_LOCATOR_LISTENER, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 }
