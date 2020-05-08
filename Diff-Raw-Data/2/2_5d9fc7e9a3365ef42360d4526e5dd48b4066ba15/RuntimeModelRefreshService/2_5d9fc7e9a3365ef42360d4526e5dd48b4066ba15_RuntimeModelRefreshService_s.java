 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model.runtime.services;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IProcesses;
 import org.eclipse.tcf.services.ISysMonitor;
 import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
 import org.eclipse.tcf.te.runtime.callback.AsyncCallbackCollector;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IDelegateService;
 import org.eclipse.tcf.te.tcf.core.async.CallbackInvocationDelegate;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelChannelService;
 import org.eclipse.tcf.te.tcf.core.model.services.AbstractModelService;
 import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode.TYPE;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNodeProperties;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelRefreshService;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelUpdateService;
 import org.eclipse.tcf.te.tcf.processes.core.nls.Messages;
 
 /**
  * Runtime model refresh service implementation.
  * <p>
  * <b>Service implementation assumptions</b>
  * <ul>
  * <li>Refresh operations do not update the model or the existing process context model nodes directly. Any
  *     refresh operation is creating a parallel tree of nodes which is merged into the model <i>at the end</i>
  *     of the refresh operation.</li>
  * <li>Refresh operations do not modify the status of asynchronous refreshable context of the refresh operation
  *     root. The caller of the refresh service is responsible for handling the asynchronous refreshable context
  *     state of the refresh operation root.</li>
  * <li>Refresh operations requested for the same root while still running are queued and the callbacks are fired
  *     all at once when the first refresh operation completes.</li>
  * <li>Auto-refresh operations are walking the whole process context model node tree, starting from the model root,
  *     and triggers an refresh of all process context model nodes found where the child list query marker is set to done.</li>
  * </ul>
  */
 public class RuntimeModelRefreshService extends AbstractModelService<IRuntimeModel> implements IRuntimeModelRefreshService {
 	// For each root context to refresh, remember the callbacks to invoke.
 	private final Map<IModelNode, List<ICallback>> ctx2cb = new HashMap<IModelNode, List<ICallback>>();
 	// The default processes runtime model refresh service delegate
 	/* default */ final IRuntimeModelRefreshService.IDelegate defaultDelegate = new DefaultDelegate();
 
 	/**
 	 * Default processes runtime model refresh service delegate implementation.
 	 */
 	/* default */ final class DefaultDelegate implements IRuntimeModelRefreshService.IDelegate {
 		private final String[] managedPropertyNames = new String[] { IProcessContextNodeProperties.PROPERTY_CMD_LINE };
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelRefreshService.IDelegate#setNodeType(java.lang.String, org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode)
 		 */
 		@Override
 		public void setNodeType(String parentContextId, IProcessContextNode node) {
     		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
     		Assert.isNotNull(node);
 
 			node.setType(parentContextId == null ? TYPE.Process : TYPE.Thread);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelRefreshService.IDelegate#postRefreshContext(org.eclipse.tcf.protocol.IChannel, org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 		 */
         @Override
         public void postRefreshContext(final IChannel channel, final IProcessContextNode node, final ICallback callback) {
     		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
     		Assert.isNotNull(channel);
     		Assert.isNotNull(node);
     		Assert.isNotNull(callback);
 
     		// The channel must be opened, otherwise the query cannot run
     		if (channel.getState() != IChannel.STATE_OPEN) {
     			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.RuntimeModelRefreshService_error_channelClosed);
     			callback.done(RuntimeModelRefreshService.this, status);
     			return;
     		}
 
     		// Get the required services
     		final ISysMonitor sysMonService = channel.getRemoteService(ISysMonitor.class);
 
     		// The system monitor service must be available
     		if (sysMonService == null) {
     			callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
     			return;
     		}
 
     		// The context id must be set
     		String contextId = node.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
     		if (contextId == null) {
     			callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
     			return;
     		}
 
 			// Get the command line of the context
 			sysMonService.getCommandLine(contextId, new ISysMonitor.DoneGetCommandLine() {
 				@Override
 				public void doneGetCommandLine(IToken token, Exception error, String[] cmd_line) {
 					node.setProperty(IProcessContextNodeProperties.PROPERTY_CMD_LINE, error == null ? cmd_line : null);
 	    			callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 				}
 			});
         }
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelRefreshService.IDelegate#getManagedPropertyNames()
 		 */
         @Override
         public String[] getManagedPropertyNames() {
 	        return managedPropertyNames;
         }
 	}
 
 	/**
 	 * Constructor
 	 *
 	 * @param model The parent model. Must not be <code>null</code>.
 	 */
 	public RuntimeModelRefreshService(IRuntimeModel model) {
 		super(model);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService#refresh(org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void refresh(final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the parent model
 		final IRuntimeModel model = getModel();
 		Assert.isNotNull(model);
 
 		// If the parent model is already disposed, the service will drop out immediately
 		if (model.isDisposed()) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// A refresh for the passed in node is already running if the model
 		// is associated with a callback list in 'ctx2cb'.
 		final boolean isRefreshAlreadyRunning = ctx2cb.containsKey(model);
 
 		// Queue the callback to invoke once the refresh is done
 		List<ICallback> callbacks = ctx2cb.get(model);
 		if (callbacks == null) {
 			callbacks = new ArrayList<ICallback>();
 			ctx2cb.put(model, callbacks);
 		}
 		Assert.isNotNull(callbacks);
 		if (callback != null) callbacks.add(callback);
 
 		// If a refresh is already running, drop out. The callback is already
 		// queued and will be invoked once the refresh operation is done.
 		if (isRefreshAlreadyRunning) return;
 
 		// The refresh operation is building up a parallel data tree. Pass in an empty container
 		// to receive the fetched children.
 		final IProcessContextNode container = model.getFactory().newInstance(IProcessContextNode.class);
 		// Mark the container as refresh in progress
 		final IAsyncRefreshableCtx containerRefreshable = (IAsyncRefreshableCtx)container.getAdapter(IAsyncRefreshableCtx.class);
 		Assert.isNotNull(containerRefreshable);
 		containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 		// Initiate the refresh of the level 1 children
 		refreshChildrenLevel1(null, 2, container, new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				// Mark the container refresh as done
 				containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 
 				// If the refresh succeeded, update the tree
 				if (status.isOK()) {
 					// Process the new child list and merge it with the model
 					model.getService(IRuntimeModelUpdateService.class).updateChildren(model, container);
 
 					// Walk the tree on check the children at level 2 to determine if there are
 					// nodes which got expanded by the user and must be refreshed therefore too.
 					final List<IProcessContextNode> children = new ArrayList<IProcessContextNode>();
 					// Get the first level children from the model
 					List<IProcessContextNode> candidates = model.getChildren(IProcessContextNode.class);
 					for (IProcessContextNode candidate : candidates) {
 						// If the child list got not queried for the candidate, skip it
 						IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)candidate.getAdapter(IAsyncRefreshableCtx.class);
 						Assert.isNotNull(refreshable);
 						if (refreshable.getQueryState(QueryType.CHILD_LIST) != QueryState.DONE) continue;
 						// Get the second level children and find those candidates where
 						// the child list query is marked done. Add those candidates to
 						// the list of children to refresh too.
 						List<IProcessContextNode> candidates2 = candidate.getChildren(IProcessContextNode.class);
 						for (IProcessContextNode candidate2 : candidates2) {
 							// Get the asynchronous refreshable for the candidate
 							refreshable = (IAsyncRefreshableCtx)candidate2.getAdapter(IAsyncRefreshableCtx.class);
 							Assert.isNotNull(refreshable);
 							if (refreshable.getQueryState(QueryType.CHILD_LIST) != QueryState.DONE) continue;
 							// This child needs an additional refresh
 							children.add(candidate2);
 						}
 					}
 
 					// Run the auto-refresh logic for all children we have found
 					final ICallback callback = new Callback() {
 						@Override
 						protected void internalDone(Object caller, IStatus status) {
 							// Invoke the callbacks
 							invokeCallbacks(model, RuntimeModelRefreshService.this, status);
 						}
 					};
 
 					// Create the callback collector to fire once all refresh operations are completed
 					final AsyncCallbackCollector collector = new AsyncCallbackCollector(callback, new CallbackInvocationDelegate());
 
 					// Get the first level of children and check if they are need to be refreshed
 					if (children.size() > 0) {
 						// Initiate the refresh of the children
 						doAutoRefresh(model, children.toArray(new IProcessContextNode[children.size()]), 0, collector);
 					}
 
 					// Mark the collector initialization done
 					collector.initDone();
 				} else {
 					// Invoke the callbacks
 					invokeCallbacks(model, RuntimeModelRefreshService.this, status);
 				}
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService#refresh(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void refresh(final IModelNode node, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(node);
 
 		// Get the parent model
 		final IRuntimeModel model = getModel();
 		Assert.isNotNull(model);
 		// If the model is already disposed, drop out immediately
 		if (model.isDisposed() || !(node instanceof IProcessContextNode)) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// Get the context id
 		final String contextId = node.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
 		if (contextId == null) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// A refresh for the passed in node is already running if the node
 		// is associated with a callback list in 'ctx2cb'.
 		final boolean isRefreshAlreadyRunning = ctx2cb.containsKey(node);
 
 		// Queue the callback to invoke once the refresh is done
 		List<ICallback> callbacks = ctx2cb.get(node);
 		if (callbacks == null) {
 			callbacks = new ArrayList<ICallback>();
 			ctx2cb.put(node, callbacks);
 		}
 		Assert.isNotNull(callbacks);
 		// Add the current callback to the list of callbacks
 		if (callback != null) callbacks.add(callback);
 
 		// If a refresh is already running, drop out. The callback is already
 		// queued and will be invoked once the refresh operation is done.
 		if (isRefreshAlreadyRunning) return;
 
 		// The refresh operation is building up a parallel data tree. Pass in an empty container
 		// to receive the fetched context properties and context children.
 		final IProcessContextNode container = model.getFactory().newInstance(IProcessContextNode.class);
 		// Mark the container as refresh in progress
 		final IAsyncRefreshableCtx containerRefreshable = (IAsyncRefreshableCtx)container.getAdapter(IAsyncRefreshableCtx.class);
 		Assert.isNotNull(containerRefreshable);
 		containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 		// The context id must be set to the container (asserted by the update service)
 		container.setProperty(IProcessContextNodeProperties.PROPERTY_ID, contextId);
 		// The container has the same type as the original node
 		container.setType(((IProcessContextNode)node).getType());
 		// Initiate the refresh of context
 		refreshContextLevel1(contextId, 2, container, new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				// Mark the container refresh as done
 				containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 
 				// If the refresh succeeded, update the original node
 				if (status.isOK()) {
 					// Process the new context node and merge it with the original context node
 					model.getService(IRuntimeModelUpdateService.class).update(node, container);
 
 					// Walk the tree on check the children at level 2 to determine if there are
 					// nodes which got expanded by the user and must be refreshed therefore too.
 					final List<IProcessContextNode> children = new ArrayList<IProcessContextNode>();
 					// Get the first level children from the model
 					List<IProcessContextNode> candidates = ((IProcessContextNode)node).getChildren(IProcessContextNode.class);
 					for (IProcessContextNode candidate : candidates) {
 						// If the child list got not queried for the candidate, skip it
 						IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)candidate.getAdapter(IAsyncRefreshableCtx.class);
 						Assert.isNotNull(refreshable);
 						if (refreshable.getQueryState(QueryType.CHILD_LIST) != QueryState.DONE) continue;
 						// Get the second level children and find those candidates where
 						// the child list query is marked done. Add those candidates to
 						// the list of children to refresh too.
 						List<IProcessContextNode> candidates2 = candidate.getChildren(IProcessContextNode.class);
 						for (IProcessContextNode candidate2 : candidates2) {
 							// Get the asynchronous refreshable for the candidate
 							refreshable = (IAsyncRefreshableCtx)candidate2.getAdapter(IAsyncRefreshableCtx.class);
 							Assert.isNotNull(refreshable);
 							if (refreshable.getQueryState(QueryType.CHILD_LIST) != QueryState.DONE) continue;
 							// This child needs an additional refresh
 							children.add(candidate2);
 						}
 					}
 
 					// Run the auto-refresh logic for all children we have found
 					final ICallback callback = new Callback() {
 						@Override
 						protected void internalDone(Object caller, IStatus status) {
 							// Invoke the callbacks
							invokeCallbacks(model, RuntimeModelRefreshService.this, status);
 						}
 					};
 
 					// Create the callback collector to fire once all refresh operations are completed
 					final AsyncCallbackCollector collector = new AsyncCallbackCollector(callback, new CallbackInvocationDelegate());
 
 					// Get the first level of children and check if they are need to be refreshed
 					if (children.size() > 0) {
 						// Initiate the refresh of the children
 						doAutoRefresh(model, children.toArray(new IProcessContextNode[children.size()]), 0, collector);
 					}
 
 					// Mark the collector initialization done
 					collector.initDone();
 				} else {
 					// Invoke the callbacks
 					invokeCallbacks(node, RuntimeModelRefreshService.this, status);
 				}
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelRefreshService#autoRefresh(org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void autoRefresh(ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the parent model
 		final IRuntimeModel model = getModel();
 
 		// If the parent model is already disposed, the service will drop out immediately
 		if (model.isDisposed()) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// Determine if there is already a model refresh running.
 		// A model refresh can be initiated via refresh(...) or autoRefresh(...).
 		final boolean isRefreshAlreadyRunning = ctx2cb.containsKey(model);
 
 		// Queue the callback to invoke once the refresh is done
 		List<ICallback> callbacks = ctx2cb.get(model);
 		if (callbacks == null) {
 			callbacks = new ArrayList<ICallback>();
 			ctx2cb.put(model, callbacks);
 		}
 		Assert.isNotNull(callbacks);
 		// Add the current callback to the list of callbacks
 		if (callback != null) callbacks.add(callback);
 
 		// If a refresh is already running, drop out. The callback is already
 		// queued and will be invoked once the refresh operation is done.
 		if (isRefreshAlreadyRunning) return;
 
 		// Create the inner callback which will invoke all queued callbacks
 		final ICallback innerCallback = new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				// Invoke the callbacks
 				invokeCallbacks(model, RuntimeModelRefreshService.this, status);
 			}
 		};
 
 		// Create the callback collector to fire once all refresh operations are completed
 		final AsyncCallbackCollector collector = new AsyncCallbackCollector(innerCallback, new CallbackInvocationDelegate());
 
 		// Get the first level of children and check if they are need to be refreshed
 		List<IProcessContextNode> children = model.getChildren(IProcessContextNode.class);
 		if (children.size() > 0) {
 			// Initiate the refresh of the children
 			doAutoRefresh(model, children.toArray(new IProcessContextNode[children.size()]), 0, collector);
 		}
 
 		// Mark the collector initialization done
 		collector.initDone();
 	}
 
 	// ----- Non-API refresh methods -----
 
 	/**
 	 * Performs the auto refresh of the given nodes.
 	 *
 	 * @param model The runtime model. Must not be <code>null</code>.
 	 * @param nodes The nodes. Must not be <code>null</code>.
 	 * @param index The index of the node to refresh within the nodes array. Must be greater or equal than 0 and less than the array length.
 	 * @param collector The callback collector. Must not be <code>null</code>.
 	 */
 	/* default */ void doAutoRefresh(final IRuntimeModel model, final IProcessContextNode[] nodes, final int index, final AsyncCallbackCollector collector) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(model);
 		Assert.isNotNull(nodes);
 		Assert.isTrue(index >= 0 && index < nodes.length);
 		Assert.isNotNull(collector);
 
 		final IProcessContextNode node = nodes[index];
 
 		// Get the asynchronous refresh context adapter
 		final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)node.getAdapter(IAsyncRefreshableCtx.class);
 		if (refreshable != null) {
 			// Schedule a refresh if the node got refreshed before
 			if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.DONE)) {
 				// Create a new callback for the collector to wait for
 				final ICallback callback = new Callback() {
 					@Override
                     protected void internalDone(Object caller, IStatus status) {
 						// We need a reference to the outer callback (== this)
 						final ICallback outerCallback = this;
 						// Create the inner callback
 						final ICallback innerCallback = new Callback() {
 							@Override
 							protected void internalDone(Object caller, IStatus status) {
 								// More nodes to process?
 								int newIndex = index + 1;
 								if (newIndex < nodes.length) {
 									doAutoRefresh(model, nodes, newIndex, collector);
 								}
 								// Remove the outer callback from the collector
 								collector.removeCallback(outerCallback);
 							}
 						};
 
 						// If the node has children, process them first
 						List<IProcessContextNode> children = node.getChildren(IProcessContextNode.class);
 						if (children.size() > 0) {
 							// Create a new callback collector for processing the children
 							final AsyncCallbackCollector childCollector = new AsyncCallbackCollector(innerCallback, new CallbackInvocationDelegate());
 							// Initiate the refresh of the children
 							doAutoRefresh(model, children.toArray(new IProcessContextNode[children.size()]), 0, childCollector);
 							// Mark the collector initialization done
 							childCollector.initDone();
 						} else {
 							// Invoke the inner callback right away
 							innerCallback.done(this, Status.OK_STATUS);
 						}
 					}
 				};
 				collector.addCallback(callback);
 
 				// Get the context id
 				final String contextId = node.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
 				if (contextId == null) {
 					callback.done(this, Status.OK_STATUS);
 					return;
 				}
 
 				// The refresh operation is building up a parallel data tree. Pass in an empty container
 				// to receive the fetched context properties and context children.
 				final IProcessContextNode container = model.getFactory().newInstance(IProcessContextNode.class);
 				// Mark the container as refresh in progress
 				final IAsyncRefreshableCtx containerRefreshable = (IAsyncRefreshableCtx)container.getAdapter(IAsyncRefreshableCtx.class);
 				Assert.isNotNull(containerRefreshable);
 				containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 				// The context id must be set to the container (asserted by the update service)
 				container.setProperty(IProcessContextNodeProperties.PROPERTY_ID, contextId);
 				// The container has the same type as the original node
 				container.setType(node.getType());
 				// Initiate the refresh of context (only node and the direct children)
 				refreshContextLevel1(contextId, 1, container, new Callback() {
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						// Mark the container refresh as done
 						containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 
 						// If the refresh succeeded, update the original node
 						if (status.isOK()) {
 							// Auto refresh requires to update the children of any new child found for
 							// the node refreshed. Collect all child nodes not being children of the original node.
 							List<IProcessContextNode> oldChildren = node.getChildren(IProcessContextNode.class);
 							List<IProcessContextNode> newChildren = container.getChildren(IProcessContextNode.class);
 							for (IProcessContextNode child : oldChildren) {
 								// Get the context id of the exiting child
 								String id = child.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
 								if (id == null) continue;
 								// Find the context id in the new children list
 								IProcessContextNode node = null;
 								for (IProcessContextNode candidate : newChildren) {
 									if (id.equals(candidate.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID))) {
 										node = candidate;
 										break;
 									}
 								}
 								// If found in the new children list, remove it
 								if (node != null) newChildren.remove(node);
 							}
 
 							// Process the new context node and merge it with the original context node
 							model.getService(IRuntimeModelUpdateService.class).update(node, container);
 
 							// If there are any new children detected, update the child list of those new children
 							if (newChildren.size() > 0) {
 								// Create the collector firing the final callback at the end
 								final AsyncCallbackCollector collector = new AsyncCallbackCollector(callback, new CallbackInvocationDelegate());
 								// Get the real children list
 								oldChildren = node.getChildren(IProcessContextNode.class);
 
 								for (IProcessContextNode child : newChildren) {
 									// Get the context id of the child
 									String id = child.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
 									if (id == null) continue;
 
 									// Find the real child node
 									IProcessContextNode realChild = null;
 									for (IProcessContextNode candidate : oldChildren) {
 										if (id.equals(candidate.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID))) {
 											realChild = candidate;
 											break;
 										}
 									}
 									if (realChild == null) continue;
 
 									// The refresh operation is building up a parallel data tree. Pass in an empty container
 									// to receive the fetched context properties and context children.
 									final IProcessContextNode container = model.getFactory().newInstance(IProcessContextNode.class);
 									// Mark the container as refresh in progress
 									final IAsyncRefreshableCtx containerRefreshable = (IAsyncRefreshableCtx)container.getAdapter(IAsyncRefreshableCtx.class);
 									Assert.isNotNull(containerRefreshable);
 									containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 									// The context id must be set to the container (asserted by the update service)
 									container.setProperty(IProcessContextNodeProperties.PROPERTY_ID, contextId);
 									// The container has the same type as the original node
 									container.setType(node.getType());
 
 									// Create the callback to invoke
 									final IProcessContextNode finRealChild = realChild;
 									final ICallback cb = new Callback() {
 										@Override
                                         protected void internalDone(Object caller, IStatus status) {
 											// Mark the container refresh as done
 											containerRefreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 
 											// If the refresh succeeded, update the original child node
 											if (status.isOK()) {
 												model.getService(IRuntimeModelUpdateService.class).updateChildren(finRealChild, container);
 											}
 
 											// Remove the callback from the collector
 											if (status.getException() != null) {
 												collector.handleError(status.getException());
 											} else {
 												collector.removeCallback(this);
 											}
 										}
 									};
 									collector.addCallback(cb);
 
 									// Refresh the children of the new child
 									refreshChildrenLevel1(id, 1, container, cb);
 								}
 
 								collector.initDone();
 							} else {
 								// Invoke the callback
 								callback.done(RuntimeModelRefreshService.this, status);
 							}
 						} else {
 							// Invoke the callback
 							callback.done(RuntimeModelRefreshService.this, status);
 						}
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Refresh the context properties for the given context id.
 	 * <p>
 	 * The method fetches the children of the given context id and generates a new set of process
 	 * context nodes to represent the children until the max depth is reached.
 	 * <p>
 	 * The fetched properties are added to the given container.
 	 *
 	 * @param contextId The context id. Must not be <code>null</code>.
 	 * @param maxDepth The max depth of the tree to refresh. Must be greater than 0.
 	 * @param container The container. Must not be <code>null</code>.
 	 * @param callback The callback to invoke once the operation is completed. Must not be <code>null</code>.
 	 */
 	protected void refreshContextLevel1(final String contextId, final int maxDepth, final IProcessContextNode container, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(contextId);
 		Assert.isTrue(maxDepth > 0);
 		Assert.isNotNull(container);
 		Assert.isNotNull(callback);
 
 		// Make sure that the callback is invoked even for unexpected cases
 		try {
 			// Get an open channel
 			IModelChannelService channelService = getModel().getService(IModelChannelService.class);
 			channelService.openChannel(new IModelChannelService.DoneOpenChannel() {
 				@Override
 				public void doneOpenChannel(Throwable error, final IChannel channel) {
 					if (error == null) {
 						// Query the context properties
 						refreshContext(channel, contextId, container, new Callback() {
 							@Override
                             protected void internalDone(Object caller, IStatus status) {
 								if (status.isOK()) {
 									// Query the first level child contexts
 									refreshChildContexts(channel, contextId, container, new Callback() {
 										@Override
 			                            protected void internalDone(Object caller, IStatus status) {
 											// Refresh the next level if the depth is still larger than 0
 											if (maxDepth - 1 > 0) {
 												List<IProcessContextNode> children = container.getChildren(IProcessContextNode.class);
 												Assert.isNotNull(children);
 												refreshChildrenLevelN(channel, children.toArray(new IProcessContextNode[children.size()]), maxDepth - 1, callback);
 											} else {
 												// Refresh completed, invoke the callback
 												callback.done(RuntimeModelRefreshService.this, status);
 											}
 										}
 									});
 								} else {
 									callback.done(RuntimeModelRefreshService.this, status);
 								}
 							}
 						});
 					} else {
 						callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), error.getLocalizedMessage(), error));
 					}
 				}
 			});
 		} catch (Throwable e) {
 			callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e));
 		}
 	}
 
 	/**
 	 * Fetches the first level of children for the given parent context id.
 	 * <p>
 	 * The method fetches the first level of children and generates a new set of process
 	 * context nodes to represent the children. Each child is refresh itself until the max
 	 * depth is reached.
 	 * <p>
 	 * The fetched first level children are added to the given container.
 	 *
 	 * @param parentContextId The parent context id or <code>null</code> for the root context.
 	 * @param maxDepth The max depth of the tree to refresh. Must be greater than 0.
 	 * @param container The container. Must not be <code>null</code>.
 	 * @param callback The callback to invoke once the operation is completed. Must not be <code>null</code>.
 	 */
 	/* default */ void refreshChildrenLevel1(final String parentContextId, final int maxDepth, final IProcessContextNode container, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isTrue(maxDepth > 0);
 		Assert.isNotNull(container);
 		Assert.isNotNull(callback);
 
 		// Make sure that the callback is invoked even for unexpected cases
 		try {
 			// Get an open channel
 			IModelChannelService channelService = getModel().getService(IModelChannelService.class);
 			channelService.openChannel(new IModelChannelService.DoneOpenChannel() {
 				@Override
 				public void doneOpenChannel(Throwable error, final IChannel channel) {
 					if (error == null) {
 						// Query the first level child contexts
 						refreshChildContexts(channel, parentContextId, container, new Callback() {
 							@Override
                             protected void internalDone(Object caller, IStatus status) {
 								// Refresh the next level if the depth is still larger than 0
 								if (maxDepth - 1 > 0) {
 									List<IProcessContextNode> children = container.getChildren(IProcessContextNode.class);
 									Assert.isNotNull(children);
 									refreshChildrenLevelN(channel, children.toArray(new IProcessContextNode[children.size()]), maxDepth - 1, callback);
 								} else {
 									// Refresh completed, invoke the callback
 									callback.done(RuntimeModelRefreshService.this, status);
 								}
 							}
 						});
 					} else {
 						callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), error.getLocalizedMessage(), error));
 					}
 				}
 			});
 		} catch (Throwable e) {
 			callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e));
 		}
 	}
 
 	/**
 	 * Fetches the children of the given parent context nodes.
 	 * <p>
 	 * The method calls itself recursively until <code>maxDepth - 1 == 0</code> is true.
 	 *
 	 * @param channel An open channel. Must not be <code>null</code>.
 	 * @param parents The parent contexts. Must not be <code>null</code>.
 	 * @param maxDepth The max depth of the tree to refresh. Must be greater than 0.
 	 * @param callback The callback to invoke once the operation is completed. Must not be <code>null</code>.
 	 */
 	/* default */ void refreshChildrenLevelN(final IChannel channel, final IProcessContextNode[] parents, final int maxDepth, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(channel);
 		Assert.isNotNull(parents);
 		Assert.isTrue(maxDepth > 0);
 		Assert.isNotNull(callback);
 
 		// The channel must be opened, otherwise the query cannot run
 		if (channel.getState() != IChannel.STATE_OPEN) {
 			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.RuntimeModelRefreshService_error_channelClosed);
 			callback.done(RuntimeModelRefreshService.this, status);
 			return;
 		}
 
 		// If the parents list is empty, there is nothing to refresh
 		if (parents.length == 0) {
 			callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 			return;
 		}
 
 		// The callback collector to be fired if the children of all parents got fully refreshed
 		final AsyncCallbackCollector collector = new AsyncCallbackCollector(new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				// Refresh the next level if the depth is still larger than 0
 				if (maxDepth - 1 > 0) {
 					// The callback collector to be fired if the children of all children of all parent contexts got fully refreshed
 					final AsyncCallbackCollector collector2 = new AsyncCallbackCollector(callback, new CallbackInvocationDelegate());
 
 					for (IProcessContextNode parent : parents) {
 						List<IProcessContextNode> children = parent.getChildren(IProcessContextNode.class);
 						Assert.isNotNull(children);
 						refreshChildrenLevelN(channel, children.toArray(new IProcessContextNode[children.size()]), maxDepth - 1, new AsyncCallbackCollector.SimpleCollectorCallback(collector2));
 					}
 
 					collector2.initDone();
 				} else {
 					// Refresh completed, invoke the callback
 					callback.done(RuntimeModelRefreshService.this, status);
 				}
 			}
 		}, new CallbackInvocationDelegate());
 
 		// Loop the parent contexts and refresh the children of each one.
 		for (final IProcessContextNode parent : parents) {
 			// Get the context id of the parent. Must be not null here.
 			String parentContextId = parent.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
 			if (parentContextId == null) continue;
 			// Create the callback
 			final ICallback cb = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 			// Get the asynchronous refresh context adapter
 			final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)parent.getAdapter(IAsyncRefreshableCtx.class);
 			Assert.isNotNull(refreshable);
 			// If not IN_PROGRESS, initiate the refresh
 			if (!refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.IN_PROGRESS)) {
 				// Mark the refresh as in progress
 				refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 				// Don't send change events while refreshing
 				final boolean changed = parent.setChangeEventsEnabled(false);
 				// Refresh the children of the parent context
 				refreshChildContexts(channel, parentContextId, parent, new Callback() {
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						// Mark the refresh as done
 						refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 						// Re-enable the change events if they had been enabled before
 						if (changed) parent.setChangeEventsEnabled(true);
 						// Invoke the callback
 						cb.done(RuntimeModelRefreshService.this, status);
 					}
 				});
 			} else {
 				// Invoke the callback
 				cb.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 			}
 		}
 
 		collector.initDone();
 	}
 
 	/**
 	 * Refresh the properties of the given context id using the given channel.
 	 * All context properties are <i>added</i> to the passed in node.
 	 *
 	 * @param channel An open channel. Must not be <code>null</code>.
 	 * @param contextId The context id. Must not be <code>null</code>.
 	 * @param node The node. Must not be <code>null</code>.
 	 * @param callback The callback to invoke once the operation is completed. Must not be <code>null</code>.
 	 */
 	/* default */ void refreshContext(final IChannel channel, final String contextId, final IProcessContextNode node, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(channel);
 		Assert.isNotNull(contextId);
 		Assert.isNotNull(node);
 		Assert.isNotNull(callback);
 
 		// The channel must be opened, otherwise the query cannot run
 		if (channel.getState() != IChannel.STATE_OPEN) {
 			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.RuntimeModelRefreshService_error_channelClosed);
 			callback.done(RuntimeModelRefreshService.this, status);
 			return;
 		}
 
 		// Get the required services
 		final IProcesses service = channel.getRemoteService(IProcesses.class);
 		final ISysMonitor sysMonService = channel.getRemoteService(ISysMonitor.class);
 
 		// At least the processes and the system monitor service must be available
 		if (service == null || sysMonService == null) {
 			callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 			return;
 		}
 
 		// Callback collector to fire once the system monitor and process context queries completed
 		final AsyncCallbackCollector collector = new AsyncCallbackCollector(new Callback() {
 			@Override
             protected void internalDone(Object caller, IStatus status) {
 				// Determine if a delegate is registered
 				IDelegateService service = ServiceManager.getInstance().getService(channel.getRemotePeer(), IDelegateService.class, false);
 				IRuntimeModelRefreshService.IDelegate delegate = service != null ? service.getDelegate(channel.getRemotePeer(), IRuntimeModelRefreshService.IDelegate.class) : null;
 
 				// Run the post refresh context delegate
 				if (delegate == null) delegate = defaultDelegate;
 				Assert.isNotNull(delegate);
 				delegate.postRefreshContext(channel, node, callback);
 			}
 		}, new CallbackInvocationDelegate());
 
 		// Query the system monitor context object
 		final ICallback cb1 = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 		sysMonService.getContext(contextId, new ISysMonitor.DoneGetContext() {
 			@Override
 			public void doneGetContext(IToken token, Exception error, SysMonitorContext context) {
 				// Ignore errors. Some of the context might be OS context we do not have
 				// permissions to read the properties from.
 				node.setSysMonitorContext(context);
 				// Invoke the callback
 				cb1.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 			}
 		});
 
 		// Query the process context object
 		final ICallback cb2 = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 		service.getContext(contextId, new IProcesses.DoneGetContext() {
 			@Override
 			public void doneGetContext(IToken token, Exception error, IProcesses.ProcessContext context) {
 				// Errors are ignored
 				node.setProcessContext(context);
 				// Set the context name from the process context if available
 				if (context != null) node.setProperty(IProcessContextNodeProperties.PROPERTY_NAME, context.getName());
 				// Invoke the callback
 				cb2.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 			}
 		});
 
 		collector.initDone();
 	}
 
 	/**
 	 * Refresh the child contexts of the given parent context id using the given channel.
 	 * All child contexts are <i>added</i> to the passed in container.
 	 *
 	 * @param channel An open channel. Must not be <code>null</code>.
 	 * @param parentContextId The parent context id or <code>null</code> for the root context.
 	 * @param container The container. Must not be <code>null</code>.
 	 * @param callback The callback to invoke once the operation is completed. Must not be <code>null</code>.
 	 */
 	/* default */ void refreshChildContexts(final IChannel channel, final String parentContextId, final IContainerModelNode container, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(channel);
 		Assert.isNotNull(container);
 		Assert.isNotNull(callback);
 
 		// The channel must be opened, otherwise the query cannot run
 		if (channel.getState() != IChannel.STATE_OPEN) {
 			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.RuntimeModelRefreshService_error_channelClosed);
 			callback.done(RuntimeModelRefreshService.this, status);
 			return;
 		}
 
 		// Get the required services
 		final IProcesses service = channel.getRemoteService(IProcesses.class);
 		final ISysMonitor sysMonService = channel.getRemoteService(ISysMonitor.class);
 
 		// At least the processes and the system monitor service must be available
 		if (service == null || sysMonService == null) {
 			callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 			return;
 		}
 
 		// Get the child context id's of the given parent context id
 		sysMonService.getChildren(parentContextId, new ISysMonitor.DoneGetChildren() {
 			@Override
 			public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
 				if (error == null) {
 					if (context_ids != null && context_ids.length > 0) {
 						// Callback collector to fire the passed in callback once all child contexts got fully refreshed
 						final AsyncCallbackCollector collector = new AsyncCallbackCollector(callback, new CallbackInvocationDelegate());
 
 						// Loop the returned context id's and query the context data
 						for (String id : context_ids) {
 							final String contextId = id;
 
 							// Create the context node for the current context id
 							final IProcessContextNode node = createContextNodeFrom(contextId);
 							Assert.isNotNull(node);
 							// Add the node to the container
 							container.add(node);
 
 							// Callback collector to fire once the system monitor and process context queries completed
 							final ICallback innerCallback = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 							final AsyncCallbackCollector innerCollector = new AsyncCallbackCollector(new Callback() {
 								@Override
                                 protected void internalDone(Object caller, IStatus status) {
 									// Determine if a delegate is registered
 									IDelegateService service = ServiceManager.getInstance().getService(channel.getRemotePeer(), IDelegateService.class, false);
 									IRuntimeModelRefreshService.IDelegate delegate = service != null ? service.getDelegate(channel.getRemotePeer(), IRuntimeModelRefreshService.IDelegate.class) : null;
 
 									// Determine the node type
 									if (delegate != null) delegate.setNodeType(parentContextId, node);
 									// Fallback to the default delegate if node type is not set by delegate
 									if (node.getType() == TYPE.Unknown) defaultDelegate.setNodeType(parentContextId, node);
 
 									// Run the post refresh context delegate
 									if (delegate == null) delegate = defaultDelegate;
 									Assert.isNotNull(delegate);
 									delegate.postRefreshContext(channel, node, innerCallback);
 								}
 							}, new CallbackInvocationDelegate());
 
 							// Query the system monitor context object
 							final ICallback cb1 = new AsyncCallbackCollector.SimpleCollectorCallback(innerCollector);
 							sysMonService.getContext(contextId, new ISysMonitor.DoneGetContext() {
 								@Override
 								public void doneGetContext(IToken token, Exception error, SysMonitorContext context) {
 									// Ignore errors. Some of the context might be OS context we do not have
 									// permissions to read the properties from.
 									node.setSysMonitorContext(context);
 									// Invoke the callback
 									cb1.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 								}
 							});
 
 							// Query the process context object
 							final ICallback cb2 = new AsyncCallbackCollector.SimpleCollectorCallback(innerCollector);
 							service.getContext(contextId, new IProcesses.DoneGetContext() {
 								@Override
 								public void doneGetContext(IToken token, Exception error, IProcesses.ProcessContext context) {
 									// Errors are ignored
 									node.setProcessContext(context);
 									// Set the context name from the process context if available
 									if (context != null) node.setProperty(IProcessContextNodeProperties.PROPERTY_NAME, context.getName());
 									// Invoke the callback
 									cb2.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 								}
 							});
 
 							innerCollector.initDone();
 						}
 
 						collector.initDone();
 					} else {
 						callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 					}
 				} else {
 					callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), error.getLocalizedMessage(), error));
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create a process context node instance for the given context id.
 	 *
 	 * @param contextId The context id. Must not be <code>null</code>.
 	 * @return The process context node instance.
 	 */
 	/* default */ IProcessContextNode createContextNodeFrom(String contextId) {
 		Assert.isNotNull(contextId);
 
 		// Create a context node and associate the given context
 		IProcessContextNode node = getModel().getFactory().newInstance(IProcessContextNode.class);
 
 		// Set the context id
 		node.setProperty(IProcessContextNodeProperties.PROPERTY_ID, contextId);
 
 		return node;
 	}
 
 	// ----- Utility methods ------
 
 	/**
 	 * Invoke all pending callbacks for the given context.
 	 * <p>
 	 * Each callback is invoked as single runnable dispatched to the
 	 * TCF event dispatch thread.
 	 *
 	 * @param context The context. Must not be <code>null</code>.
 	 * @param caller The caller of the callback or <code>null</code>.
 	 * @param status The status. Must not be <code>null</code>.
 	 */
 	protected void invokeCallbacks(final IModelNode context, final Object caller, final IStatus status) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(context);
 		Assert.isNotNull(status);
 
 		List<ICallback> callbacks = ctx2cb.remove(context);
 		if (callbacks != null) {
 			for (final ICallback callback : callbacks) {
 				Protocol.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						callback.done(caller, status);
 					}
 				});
 			}
 		}
 	}
 }
