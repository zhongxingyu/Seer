 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator.nodes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.ILocator;
 import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.listeners.interfaces.IChannelStateChangeListener;
 import org.eclipse.tcf.te.tcf.locator.Scanner;
 import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IScanner;
 import org.eclipse.tcf.te.tcf.locator.interfaces.ITracing;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelPeerNodeQueryService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.listener.ChannelStateChangeListener;
 import org.eclipse.tcf.te.tcf.locator.listener.LocatorListener;
 import org.eclipse.tcf.te.tcf.locator.services.LocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.services.LocatorModelPeerNodeQueryService;
 import org.eclipse.tcf.te.tcf.locator.services.LocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.services.LocatorModelUpdateService;
 
 
 /**
  * Default locator model implementation.
  */
 public class LocatorModel extends PlatformObject implements ILocatorModel {
 	// The unique model id
 	private final UUID uniqueId = UUID.randomUUID();
 	// Flag to mark the model disposed
 	private boolean disposed;
 
 	// The list of known peers
 	private final Map<String, IPeerModel> peers = new HashMap<String, IPeerModel>();
 	// The list of "proxied" peers per proxy peer id
 	private final Map<String, List<IPeerModel>> peerChildren = new HashMap<String, List<IPeerModel>>();
 
 	// Reference to the scanner
 	private IScanner scanner = null;
 
 	// Reference to the model locator listener
 	private ILocator.LocatorListener locatorListener = null;
 	// Reference to the model channel state change listener
 	private IChannelStateChangeListener channelStateChangeListener = null;
 
 	// The list of registered model listeners
 	private final List<IModelListener> modelListener = new ArrayList<IModelListener>();
 
 	// Reference to the refresh service
 	private final ILocatorModelRefreshService refreshService = new LocatorModelRefreshService(this);
 	// Reference to the lookup service
 	private final ILocatorModelLookupService lookupService = new LocatorModelLookupService(this);
 	// Reference to the update service
 	private final ILocatorModelUpdateService updateService = new LocatorModelUpdateService(this);
 	// Reference to the query service
 	private final ILocatorModelPeerNodeQueryService queryService = new LocatorModelPeerNodeQueryService(this);
 
 	/**
 	 * Constructor.
 	 */
 	public LocatorModel() {
 		super();
 		disposed = false;
 
 		channelStateChangeListener = new ChannelStateChangeListener(this);
 		Tcf.addChannelStateChangeListener(channelStateChangeListener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#addListener(org.eclipse.tcf.te.tcf.locator.core.interfaces.IModelListener)
 	 */
 	@Override
 	public void addListener(IModelListener listener) {
 		Assert.isNotNull(listener);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.addListener( " + listener + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		if (!modelListener.contains(listener)) modelListener.add(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#removeListener(org.eclipse.tcf.te.tcf.locator.core.interfaces.IModelListener)
 	 */
 	@Override
 	public void removeListener(IModelListener listener) {
 		Assert.isNotNull(listener);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.removeListener( " + listener + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		modelListener.remove(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel#getListener()
 	 */
 	@Override
 	public IModelListener[] getListener() {
 		return modelListener.toArray(new IModelListener[modelListener.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#dispose()
 	 */
 	@Override
 	public void dispose() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.dispose()", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$
 		}
 
 		// If already disposed, we are done immediately
 		if (disposed) return;
 
 		disposed = true;
 
 		final IModelListener[] listeners = getListener();
 		if (listeners.length > 0) {
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					for (IModelListener listener : listeners) {
 						listener.locatorModelDisposed(LocatorModel.this);
 					}
 				}
 			});
 		}
 		modelListener.clear();
 
 		if (locatorListener != null) {
 			Protocol.getLocator().removeListener(locatorListener);
 			locatorListener = null;
 		}
 
 		if (channelStateChangeListener != null) {
 			Tcf.removeChannelStateChangeListener(channelStateChangeListener);
 			channelStateChangeListener = null;
 		}
 
 		if (scanner != null) {
 			stopScanner();
 			scanner = null;
 		}
 
 		peers.clear();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#isDisposed()
 	 */
 	@Override
 	public boolean isDisposed() {
 		return disposed;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getPeers()
 	 */
 	@Override
 	public IPeerModel[] getPeers() {
 		return peers.values().toArray(new IPeerModel[peers.values().size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel#getChildren(java.lang.String)
 	 */
 	@Override
     public List<IPeerModel> getChildren(String parentPeerID) {
 		Assert.isNotNull(parentPeerID);
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.getChildren( " + parentPeerID + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		List<IPeerModel> children = peerChildren.get(parentPeerID);
 		if (children == null) children = Collections.emptyList();
 		return Collections.unmodifiableList(children);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel#setChildren(java.lang.String, java.util.List)
 	 */
 	@Override
     public void setChildren(String parentPeerID, List<IPeerModel> children) {
 		Assert.isNotNull(parentPeerID);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.setChildren( " + parentPeerID + ", " + children + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 
 		if (children == null || children.size() == 0) {
 			peerChildren.remove(parentPeerID);
 		} else {
 			peerChildren.put(parentPeerID, new ArrayList<IPeerModel>(children));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (adapter.isAssignableFrom(ILocator.LocatorListener.class)) {
 			return locatorListener;
 		}
 		if (adapter.isAssignableFrom(IScanner.class)) {
 			return scanner;
 		}
 		if (adapter.isAssignableFrom(ILocatorModelRefreshService.class)) {
 			return refreshService;
 		}
 		if (adapter.isAssignableFrom(ILocatorModelLookupService.class)) {
 			return lookupService;
 		}
 		if (adapter.isAssignableFrom(ILocatorModelUpdateService.class)) {
 			return updateService;
 		}
 		if (adapter.isAssignableFrom(ILocatorModelPeerNodeQueryService.class)) {
 			return queryService;
 		}
 		if (adapter.isAssignableFrom(Map.class)) {
 			return peers;
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 	    return uniqueId.hashCode();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public final boolean equals(Object obj) {
 		if (obj instanceof LocatorModel) {
 			return uniqueId.equals(((LocatorModel)obj).uniqueId);
 		}
 		return super.equals(obj);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getService(java.lang.Class)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public <V extends ILocatorModelService> V getService(Class<V> serviceInterface) {
 		Assert.isNotNull(serviceInterface);
 		return (V)getAdapter(serviceInterface);
 	}
 
 	/**
 	 * Check if the locator listener has been created and registered
 	 * to the global locator service.
 	 * <p>
 	 * <b>Note:</b> This method is not intended to be call from clients.
 	 */
 	public void checkLocatorListener() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(Protocol.getLocator());
 
 		if (locatorListener == null) {
 			locatorListener = doCreateLocatorListener(this);
 			Protocol.getLocator().addListener(locatorListener);
 		}
 	}
 
 	/**
 	 * Creates the locator listener instance.
 	 *
 	 * @param model The parent model. Must not be <code>null</code>.
 	 * @return The locator listener instance.
 	 */
 	protected ILocator.LocatorListener doCreateLocatorListener(ILocatorModel model) {
 		Assert.isNotNull(model);
 		return new LocatorListener(model);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getScanner()
 	 */
 	@Override
 	public IScanner getScanner() {
 		if (scanner == null) scanner = new Scanner(this);
 		return scanner;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#startScanner(long, long)
 	 */
 	@Override
 	public void startScanner(long delay, long schedule) {
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.startScanner( " + delay + ", " + schedule + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 
 		IScanner scanner = getScanner();
 		Assert.isNotNull(scanner);
 
 		// Pass on the schedule parameter
 		Map<String, Object> config = new HashMap<String, Object>(scanner.getConfiguration());
 		config.put(IScanner.PROP_SCHEDULE, Long.valueOf(schedule));
 		scanner.setConfiguration(config);
 
 		// The default scanner implementation is a job.
 		// -> schedule here if it is a job
 		if (scanner instanceof Job) {
 			Job job = (Job)scanner;
 			job.setSystem(true);
 			job.setPriority(Job.DECORATE);
 			job.schedule(delay);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#stopScanner()
 	 */
 	@Override
 	public void stopScanner() {
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.stopScanner()", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$
 		}
 
 		if (scanner != null) {
 			// Terminate the scanner
 			scanner.terminate();
 			// Reset the scanner reference
 			scanner = null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.ILocatorModel#validatePeerNodeForAdd(org.eclipse.tcf.te.tcf.locator.core.interfaces.nodes.IPeerModel)
 	 */
 	@Override
 	public IPeerModel validatePeerNodeForAdd(IPeerModel node) {
 		Assert.isNotNull(node);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the peer from the peer node
 		IPeer peer = node.getPeer();
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd( " + (peer != null ? peer.getID() : null) + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		IPeerModel result = node;
 
 		// Get the loopback address
 		String loopback = IPAddressUtil.getInstance().getIPv4LoopbackAddress();
 		// Get the peer IP
 		String peerIP = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
 
 		// If the peer node is for local host, we ignore all peers not being
 		// associated with the loopback address.
		if (IPAddressUtil.getInstance().isLocalHost(peerIP) && !loopback.equals(peerIP)) {
 			// Not loopback address -> drop the peer
 			result = null;
 
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 				CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: local host peer but not loopback address -> peer node dropped" //$NON-NLS-1$
 															, ITracing.ID_TRACE_LOCATOR_MODEL, this);
 			}
 		} else {
 			// Peers are filtered by agent id. Don't add the peer node
 			// if we have another peer node already having the same agent id
 			String agentId = peer.getAgentID();
 			IPeerModel[] previousNodes = agentId != null ? getService(ILocatorModelLookupService.class).lkupPeerModelByAgentId(agentId) : new IPeerModel[0];
 
 			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 				CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: agentId=" + agentId + ", Matching peer nodes " //$NON-NLS-1$ //$NON-NLS-2$
 															+ (previousNodes.length > 0 ? "found (" + previousNodes.length +")" : "not found --> DONE") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 															, ITracing.ID_TRACE_LOCATOR_MODEL, this);
 			}
 
 			for (IPeerModel previousNode : previousNodes) {
 				// Get the peer for the previous node
 				IPeer previousPeer = previousNode.getPeer();
 				if (previousPeer != null) {
 					// Get the ports
 					String peerPort = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
 					if (peerPort == null || "".equals(peerPort)) peerPort = "1534"; //$NON-NLS-1$ //$NON-NLS-2$
 					String previousPeerPort = previousPeer.getAttributes().get(IPeer.ATTR_IP_PORT);
 					if (previousPeerPort == null || "".equals(previousPeerPort)) previousPeerPort = "1534"; //$NON-NLS-1$ //$NON-NLS-2$
 
 					if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 						CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: peerIP=" + peerIP //$NON-NLS-1$
 										+ ", peerPort=" + peerPort + ", previousPeerPort=" + previousPeerPort //$NON-NLS-1$ //$NON-NLS-2$
 										, ITracing.ID_TRACE_LOCATOR_MODEL, this);
 					}
 
 					// If the ports of the agent instances are identical,
 					// than try to find the best representation of the agent instance
 					if (peerPort.equals(previousPeerPort))  {
 						// Drop the current node
 						result = null;
 
 						if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 							CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: Previous peer node kept, new peer node dropped" //$NON-NLS-1$
 											, ITracing.ID_TRACE_LOCATOR_MODEL, this);
 						}
 
 
 						// Break the loop if the ports matched
 						break;
 					}
 
 					if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 						CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: Previous peer node kept, new peer node added (Port mismatch)" //$NON-NLS-1$
 										, ITracing.ID_TRACE_LOCATOR_MODEL, this);
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel#validateChildPeerNodeForAdd(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
 	 */
 	@Override
 	public IPeerModel validateChildPeerNodeForAdd(final IPeerModel node) {
 		Assert.isNotNull(node);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.validateChildPeerNodeForAdd( " + node.getPeerId() + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		// Determine the parent node. If null, the child node is invalid
 		// and cannot be added
 		final IPeerModel parent = node.getParent(IPeerModel.class);
 		if (parent == null) return null;
 
 		return validateChildPeerNodeForAdd(parent, node);
 	}
 
 	/**
 	 * Validates the given child peer model node in relation to the given parent peer model node
 	 * hierarchy.
 	 * <p>
 	 * The method is recursive.
 	 *
 	 * @param parent The parent model node. Must not be <code>null</code>.
 	 * @param node The child model node. Must not be <code>null</code>.
 	 *
 	 * @return The validated child peer model node, or <code>null</code>.
 	 */
 	protected IPeerModel validateChildPeerNodeForAdd(IPeerModel parent, IPeerModel node) {
 		Assert.isNotNull(parent);
 		Assert.isNotNull(node);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.validateChildPeerNodeForAdd( " + parent.getPeerId() + ", " + node.getPeerId() + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 
 		// Validate against the given parent
 		if (doValidateChildPeerNodeForAdd(parent, node) == null) {
 			return null;
 		}
 
 		// If the parent node is child node by itself, validate the
 		// child node against the parent parent node.
 		if (parent.getParent(IPeerModel.class) != null) {
 			IPeerModel parentParentNode = parent.getParent(IPeerModel.class);
 			if (doValidateChildPeerNodeForAdd(parentParentNode, node) == null) {
 				return null;
 			}
 
 			// And validate the child node against all child nodes of the parent parent.
 			List<IPeerModel> childrenList = getChildren(parentParentNode.getPeerId());
 			IPeerModel[] children = childrenList.toArray(new IPeerModel[childrenList.size()]);
 			for (IPeerModel parentParentChild : children) {
 				if (node.equals(parentParentChild) || parent.equals(parentParentChild)) {
 					return null;
 				}
 				if (doValidateChildPeerNodeForAdd(parentParentChild, node) == null) {
 					return null;
 				}
 			}
 		}
 
 		return node;
 	}
 
 	/**
 	 * Validates the given child peer model node in relation to the given parent peer model node.
 	 * <p>
 	 * The method is non-recursive.
 	 *
 	 * @param parent The parent model node. Must not be <code>null</code>.
 	 * @param node The child model node. Must not be <code>null</code>.
 	 *
 	 * @return The validated child peer model node, or <code>null</code>.
 	 */
 	protected IPeerModel doValidateChildPeerNodeForAdd(IPeerModel parent, IPeerModel node) {
 		Assert.isNotNull(parent);
 		Assert.isNotNull(node);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
 			CoreBundleActivator.getTraceHandler().trace("LocatorModel.doValidateChildPeerNodeForAdd( " + parent.getPeerId() + ", " + node.getPeerId() + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 
 		// If the child node is already visible as root node, drop the child node
 		if (getService(ILocatorModelLookupService.class).lkupPeerModelById(node.getPeerId()) != null) {
 			return null;
 		}
 
 		// Get the peer from the peer node
 		IPeer peer = node.getPeer();
 
 		// If the child peer represents the same agent as the parent peer,
 		// drop the child peer
 		String parentAgentID = parent.getPeer().getAgentID();
 		if (parentAgentID != null && parentAgentID.equals(peer.getAgentID())) {
 			return null;
 		}
 		// If the child peer's IP address appears to be the address of the
 		// localhost, drop the child peer
 		if (peer.getAttributes().get(IPeer.ATTR_IP_HOST) != null && IPAddressUtil.getInstance().isLocalHost(peer.getAttributes().get(IPeer.ATTR_IP_HOST))) {
 			return null;
 		}
 		// If the child peer's IP address and port are the same as the parent's
 		// IP address and port, drop the child node
 		Map<String, String> parentPeerAttributes = parent.getPeer().getAttributes();
 		if (parentPeerAttributes.get(IPeer.ATTR_IP_HOST) != null && parentPeerAttributes.get(IPeer.ATTR_IP_HOST).equals(peer.getAttributes().get(IPeer.ATTR_IP_HOST))) {
 			String parentPort = parentPeerAttributes.get(IPeer.ATTR_IP_PORT);
 			if (parentPort == null) parentPort = "1534"; //$NON-NLS-1$
 			String port = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
 			if (port == null) port = "1534"; //$NON-NLS-1$
 
 			if (parentPort.equals(port)) return null;
 		}
 
 		return node;
 	}
 
 	/**
 	 * Fire the model listener for the given peer model.
 	 *
 	 * @param peer The peer model. Must not be <code>null</code>.
 	 * @param added <code>True</code> if the peer model got added, <code>false</code> if it got removed.
 	 */
 	protected void fireListener(final IPeerModel peer, final boolean added) {
 		Assert.isNotNull(peer);
 
 		final IModelListener[] listeners = getListener();
 		if (listeners.length > 0) {
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					for (IModelListener listener : listeners) {
 						listener.locatorModelChanged(LocatorModel.this, peer, added);
 					}
 				}
 			});
 		}
 	}
 }
