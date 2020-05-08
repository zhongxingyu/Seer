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
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.ILocator;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.locator.ScannerRunnable;
 import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.preferences.IPreferenceKeys;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.model.ModelLocationUtil;
 import org.eclipse.tcf.te.tcf.locator.nodes.LocatorModel;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 
 
 /**
  * Default locator model refresh service implementation.
  */
 public class LocatorModelRefreshService extends AbstractLocatorModelService implements ILocatorModelRefreshService {
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
 	 */
 	public LocatorModelRefreshService(ILocatorModel parentModel) {
 		super(parentModel);
 	}
 
 	/**
 	 * Asynchronously invoke the callback within the TCF dispatch thread.
 	 *
 	 * @param callback The callback to invoke or <code>null</code>.
 	 */
 	protected final void invokeCallback(final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (callback != null) {
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					callback.done(LocatorModelRefreshService.this, Status.OK_STATUS);
 				}
 			});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService#refresh(org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void refresh(final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the parent locator model
 		ILocatorModel model = getLocatorModel();
 
 		// If the parent model is already disposed, the service will drop out immediately
 		if (model.isDisposed()) {
 			invokeCallback(callback);
 			return;
 		}
 
 		// If the TCF framework isn't initialized yet, the service will drop out immediately
 		if (!Tcf.isRunning()) {
 			invokeCallback(callback);
 			return;
 		}
 
 		// Get the list of old children (update node instances where possible)
 		final List<IPeerModel> oldChildren = new ArrayList<IPeerModel>(Arrays.asList(model.getPeers()));
 
 		// Get the locator service
 		ILocator locatorService = Protocol.getLocator();
 		if (locatorService != null) {
 			// Check for the locator listener to be created and registered
 			if (model instanceof LocatorModel) {
 				((LocatorModel)model).checkLocatorListener();
 			}
 			// Get the map of peers known to the locator service.
 			Map<String, IPeer> peers = locatorService.getPeers();
 			// Process the peers
 			processPeers(peers, oldChildren, model);
 		}
 
 		// Refresh the static peer definitions
 		refreshStaticPeers(oldChildren, model);
 
 		// If there are remaining old children, remove them from the model (non-recursive)
 		for (IPeerModel oldChild : oldChildren) {
 			model.getService(ILocatorModelUpdateService.class).remove(oldChild);
 		}
 
 		// Invoke the callback
 		invokeCallback(callback);
 	}
 
 	/**
 	 * Process the given map of peers and update the given locator model.
 	 *
 	 * @param peers The map of peers to process. Must not be <code>null</code>.
 	 * @param oldChildren The list of old children. Must not be <code>null</code>.
 	 * @param model The locator model. Must not be <code>null</code>.
 	 */
 	protected void processPeers(Map<String, IPeer> peers, List<IPeerModel> oldChildren, ILocatorModel model) {
 		Assert.isNotNull(peers);
 		Assert.isNotNull(oldChildren);
 		Assert.isNotNull(model);
 
 		for (Entry<String, IPeer> entry : peers.entrySet()) {
 			// Get the peer instance for the current peer id
 			IPeer peer = entry.getValue();
 			// Try to find an existing peer node first
 			IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(entry.getKey());
 			// And create a new one if we cannot find it
 			if (peerNode == null) {
 				peerNode = new PeerModel(model, peer);
 			}
 			else {
 				oldChildren.remove(peerNode);
 			}
 
 			if (peerNode.getPeer() != peer) {
				if (!peerNode.isStatic()) {
 					peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
 				} else {
					String value = peerNode.getPeer().getAttributes().get(IPersistableNodeProperties.PROPERTY_URI);
					URI uri = value != null ? URI.create(value) : null;
					File file = uri != null && "file".equals(uri.getScheme()) ? new File(uri.normalize()) : null; //$NON-NLS-1$
					if (file != null && !file.exists()) {
						peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
					} else {
						// Merge user configured properties between the peers
						model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(peerNode, peer, false);
					}
 				}
 			}
 
 			// Validate the peer node before adding
 			peerNode = model.validatePeerNodeForAdd(peerNode);
 			if (peerNode != null) {
 				// There is still the chance that the node we add is a static node and
 				// there exist an dynamically discovered node with a different id but
 				// for the same peer. Do this check only if the peer to add is a static one.
 				boolean isStatic = peerNode.isStatic();
 				if (isStatic) {
 					for (IPeerModel candidate : oldChildren) {
 						String peerID = peerNode.getPeerId();
 						String clientID = candidate.getPeer().getAttributes().get("ClientID"); //$NON-NLS-1$
 						if (clientID != null && clientID.equals(peerID)) {
 							// Merge user configured properties between the peers
 							model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(candidate, peerNode.getPeer(), true);
 							peerNode = null;
 							break;
 						}
 
 						if (peerNode.getPeer().getTransportName() != null && peerNode.getPeer().getTransportName().equals(candidate.getPeer().getTransportName())) {
 							// Same transport name
 							if ("PIPE".equals(candidate.getPeer().getTransportName())) { //$NON-NLS-1$
 								// Compare the pipe name
 								String name1 = peerNode.getPeer().getAttributes().get("PipeName"); //$NON-NLS-1$
 								String name2 = candidate.getPeer().getAttributes().get("PipeName"); //$NON-NLS-1$
 								// Same pipe -> same node
 								if (name1 != null && name1.equals(name2)) {
 									// Merge user configured properties between the peers
 									model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(candidate, peerNode.getPeer(), true);
 									peerNode = null;
 									break;
 								}
 							} else if ("Loop".equals(candidate.getPeer().getTransportName())) { //$NON-NLS-1$
 								// Merge user configured properties between the peers
 								model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(candidate, peerNode.getPeer(), true);
 								peerNode = null;
 								break;
 							} else {
 								// Compare IP_HOST and IP_Port;
 								String ip1 = peerNode.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST);
 								String ip2 = candidate.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST);
 								if (IPAddressUtil.getInstance().isSameHost(ip1, ip2)) {
 									// Compare the ports
 									String port1 = peerNode.getPeer().getAttributes().get(IPeer.ATTR_IP_PORT);
 									if (port1 == null || "".equals(port1)) { //$NON-NLS-1$
 										port1 = "1534"; //$NON-NLS-1$
 									}
 									String port2 = candidate.getPeer().getAttributes().get(IPeer.ATTR_IP_PORT);
 									if (port2 == null || "".equals(port2)) { //$NON-NLS-1$
 										port2 = "1534"; //$NON-NLS-1$
 									}
 
 									if (port1.equals(port2)) {
 										// Merge user configured properties between the peers
 										model.getService(ILocatorModelUpdateService.class).mergeUserDefinedAttributes(candidate, peerNode.getPeer(), true);
 										peerNode = null;
 										break;
 									}
 								}
 							}
 						}
 					}
 				}
 				if (peerNode != null) {
 					// Add the peer node to model
 					model.getService(ILocatorModelUpdateService.class).add(peerNode);
 					// And schedule for immediate status update
 					Runnable runnable = new ScannerRunnable(model.getScanner(), peerNode);
 					Protocol.invokeLater(runnable);
 				}
 			}
 		}
 	}
 
 	private final AtomicBoolean REFRESH_STATIC_PEERS_GUARD = new AtomicBoolean(false);
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService#refreshStaticPeers()
 	 */
 	@Override
 	public void refreshStaticPeers() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// This method might be called reentrant while processing. Return immediately
 		// in this case.
 		if (REFRESH_STATIC_PEERS_GUARD.get()) {
 			return;
 		}
 		REFRESH_STATIC_PEERS_GUARD.set(true);
 
 		// Get the parent locator model
 		ILocatorModel model = getLocatorModel();
 
 		// If the parent model is already disposed, the service will drop out immediately
 		if (model.isDisposed()) {
 			return;
 		}
 
 		// Get the list of old children (update node instances where possible)
 		final List<IPeerModel> oldChildren = new ArrayList<IPeerModel>(Arrays.asList(model.getPeers()));
 
 		// Refresh the static peer definitions
 		refreshStaticPeers(oldChildren, model);
 
 		REFRESH_STATIC_PEERS_GUARD.set(false);
 	}
 
 	/**
 	 * Refresh the static peer definitions.
 	 *
 	 * @param oldChildren The list of old children. Must not be <code>null</code>.
 	 * @param model The locator model. Must not be <code>null</code>.
 	 */
 	protected void refreshStaticPeers(List<IPeerModel> oldChildren, ILocatorModel model) {
 		Assert.isNotNull(oldChildren);
 		Assert.isNotNull(model);
 
 		// Get the root locations to lookup the static peer definitions
 		File[] roots = getStaticPeerLookupDirectories();
 		if (roots.length > 0) {
 			// The map of peers created from the static definitions
 			Map<String, IPeer> peers = new HashMap<String, IPeer>();
 			// The list of peer attributes with postponed peer instance creation
 			List<Map<String, String>> postponed = new ArrayList<Map<String,String>>();
 			// Process the root locations
 			for (File root : roots) {
 				// List all "*.json" and "*.peer" files within the root location
 				File[] candidates = root.listFiles(new FileFilter() {
 					@Override
 					public boolean accept(File pathname) {
 						IPath path = new Path(pathname.getAbsolutePath());
 						return path.getFileExtension() != null &&
 										(path.getFileExtension().toLowerCase().equals("json") || path.getFileExtension().toLowerCase().equals("peer")); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				});
 				// If there are ini files to read, process them
 				if (candidates != null && candidates.length > 0) {
 
 					for (File candidate : candidates) {
 						try {
 							IURIPersistenceService service = ServiceManager.getInstance().getService(IURIPersistenceService.class);
 							IPeer tempPeer = (IPeer)service.read(IPeer.class, candidate.getAbsoluteFile().toURI());
 
 							Map<String,String> attrs = new HashMap<String, String>(tempPeer.getAttributes());
 
 							// Remember the file path within the properties
 							attrs.put(IPersistableNodeProperties.PROPERTY_URI, candidate.getAbsoluteFile().toURI().toString());
 							// Mark the node as static peer model node
 							attrs.put("static.transient", "true"); //$NON-NLS-1$ //$NON-NLS-2$
 
 							// Validate the name attribute. If not set, set
 							// it to the file name without the .ini extension.
 							String name = attrs.get(IPeer.ATTR_NAME);
 							if (name == null || "".equals(name.trim())) { //$NON-NLS-1$
 								name = new Path(candidate.getAbsolutePath()).removeFileExtension().lastSegment();
 								attrs.put(IPeer.ATTR_NAME, name);
 							}
 
 							// Validate the id attribute. If not set, generate one.
 							String id = attrs.get(IPeer.ATTR_ID);
 							if (id == null || "".equals(id.trim()) || "USR:".equals(id.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
 								String transport = attrs.get(IPeer.ATTR_TRANSPORT_NAME);
 								String host = attrs.get(IPeer.ATTR_IP_HOST);
 								String port = attrs.get(IPeer.ATTR_IP_PORT);
 
 								if (transport != null && host != null && !(id != null && "USR:".equals(id.trim()))) { //$NON-NLS-1$
 									id = transport.trim() + ":" + host.trim(); //$NON-NLS-1$
 									id += port != null ? ":" + port.trim() : ":1534"; //$NON-NLS-1$ //$NON-NLS-2$
 								} else {
 									id = "USR:" + System.currentTimeMillis(); //$NON-NLS-1$
 									// If the key is not unique, we have to wait a little bit an try again
 									while (peers.containsKey(id)) {
 										try { Thread.sleep(20); } catch (InterruptedException e) { /* ignored on purpose */ }
 										id = "USR:" + System.currentTimeMillis(); //$NON-NLS-1$
 									}
 								}
 								attrs.put(IPeer.ATTR_ID, id);
 							}
 
 							// If the redirect property is not set, create the peer right away
 							if (attrs.get(IPeerModelProperties.PROP_REDIRECT_PROXY) == null) {
 								// Construct the peer from the attributes
 								IPeer peer = new Peer(attrs);
 								// Add the constructed peer to the peers map
 								peers.put(peer.getID(), peer);
 							} else {
 								// Try to get the peer proxy
 								String proxyId = attrs.get(IPeerModelProperties.PROP_REDIRECT_PROXY);
 								IPeer proxy = peers.get(proxyId);
 								if (proxy == null) {
 									IPeerModel peerModel = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(proxyId);
 									if (peerModel != null) {
 										proxy = peerModel.getPeer();
 									}
 								}
 
 								if (proxy != null) {
 									// Construct the peer redirector
 									PeerRedirector redirector = new PeerRedirector(proxy, attrs);
 									// Add the redirector to the peers map
 									peers.put(redirector.getID(), redirector);
 								} else {
 									// Postpone peer creation
 									postponed.add(attrs);
 								}
 							}
 						} catch (IOException e) {
 							/* ignored on purpose */
 						}
 					}
 				}
 			}
 
 			// Process postponed peers if there are any
 			if (!postponed.isEmpty()) {
 				for (Map<String, String> attrs : postponed) {
 					String proxyId = attrs.get(IPeerModelProperties.PROP_REDIRECT_PROXY);
 					IPeer proxy = proxyId != null ? peers.get(proxyId) : null;
 					if (proxy == null) {
 						IPeerModel peerModel = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(proxyId);
 						if (peerModel != null) {
 							proxy = peerModel.getPeer();
 						}
 					}
 
 					if (proxy != null) {
 						// Construct the peer redirector
 						PeerRedirector redirector = new PeerRedirector(proxy, attrs);
 						// Add the redirector to the peers map
 						peers.put(redirector.getID(), redirector);
 					} else {
 						// Proxy not available -> reset redirection
 						attrs.remove(IPeerModelProperties.PROP_REDIRECT_PROXY);
 						// Construct the peer from the attributes
 						IPeer peer = new Peer(attrs);
 						// Add the constructed peer to the peers map
 						peers.put(peer.getID(), peer);
 					}
 				}
 			}
 
 			// Process the read peers
 			if (!peers.isEmpty()) {
 				processPeers(peers, oldChildren, model);
 			}
 
 			// Scan the peers for redirected ones ... and set up the peer model association
 			for (Entry<String, IPeer> entry : peers.entrySet()) {
 				IPeer peer = entry.getValue();
 				if (!(peer instanceof PeerRedirector)) {
 					continue;
 				}
 
 				// Get the peers peer model object
 				IPeerModel peerModel = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(entry.getKey());
 				Assert.isNotNull(peerModel);
 
 				// The peer is a peer redirector -> get the proxy peer id and proxy peer model
 				String proxyPeerId = ((PeerRedirector)peer).getParent().getID();
 				IPeerModel proxy = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(proxyPeerId);
 				Assert.isNotNull(proxy);
 
 				peerModel.setParent(proxy);
 				model.getService(ILocatorModelUpdateService.class).addChild(peerModel);
 			}
 		}
 	}
 
 	/**
 	 * Returns the list of root locations to lookup for static peers definitions.
 	 *
 	 * @return The list of root locations or an empty list.
 	 */
 	protected File[] getStaticPeerLookupDirectories() {
 		// The list defining the root locations
 		List<File> rootLocations = new ArrayList<File>();
 
 		// Check on the peers root locations preference setting
 		String roots = Platform.getPreferencesService().getString(CoreBundleActivator.getUniqueIdentifier(),
 						IPreferenceKeys.PREF_STATIC_PEERS_ROOT_LOCATIONS,
 						null, null);
 		// If set, split it in its single components
 		if (roots != null) {
 			String[] candidates = roots.split(File.pathSeparator);
 			// Check on each candidate to denote an existing directory
 			for (String candidate : candidates) {
 				File file = new File(candidate);
 				if (file.canRead() && file.isDirectory() && !rootLocations.contains(file)) {
 					rootLocations.add(file);
 				}
 			}
 		}
 
 		// always add default root location
 		IPath defaultPath = ModelLocationUtil.getStaticPeersRootLocation();
 		if (defaultPath != null) {
 			File file = defaultPath.toFile();
 			if (file.canRead() && file.isDirectory() && !rootLocations.contains(file)) {
 				rootLocations.add(file);
 			}
 		}
 
 		return rootLocations.toArray(new File[rootLocations.size()]);
 	}
 }
