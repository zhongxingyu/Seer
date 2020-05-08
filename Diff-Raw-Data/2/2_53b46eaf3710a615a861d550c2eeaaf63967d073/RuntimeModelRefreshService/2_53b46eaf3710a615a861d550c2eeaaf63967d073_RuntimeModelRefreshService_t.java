 /*******************************************************************************
  * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model.runtime.services;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IProcesses;
 import org.eclipse.tcf.services.IProcessesV1;
 import org.eclipse.tcf.services.ISysMonitor;
 import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
 import org.eclipse.tcf.te.core.async.AsyncCallbackCollector;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.model.PendingOperationModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.tcf.core.async.CallbackInvocationDelegate;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.IModel;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelChannelService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelUpdateService;
 import org.eclipse.tcf.te.tcf.core.model.services.AbstractModelService;
 import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode.TYPE;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNodeProperties;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.nodes.PendingOperationNode;
 
 /**
  * Runtime model refresh service implementation.
  */
 public class RuntimeModelRefreshService extends AbstractModelService<IRuntimeModel> implements IModelRefreshService {
 
 	/**
 	 * Constructor.
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
 	public void refresh(ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		refresh(NONE, callback);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService#refresh(int, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void refresh(final int flags, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the parent model
 		final IRuntimeModel model = getModel();
 
 		// If the parent model is already disposed, the service will drop out immediately
 		if (model.isDisposed()) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)model.getAdapter(IAsyncRefreshableCtx.class);
 		final AtomicBoolean resetPendingNode = new AtomicBoolean(false);
 		if (refreshable != null && refreshable.getQueryState(QueryType.CHILD_LIST) != QueryState.IN_PROGRESS) {
 			resetPendingNode.set(true);
 			// Mark the refresh as in progress
 			refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 			// Create a new pending operation node and associate it with the refreshable
 			PendingOperationModelNode pendingNode = new PendingOperationNode();
 			pendingNode.setParent(model);
 			refreshable.setPendingOperationNode(pendingNode);
 			// Trigger a refresh of the view content.
 			ChangeEvent event = new ChangeEvent(model, IContainerModelNode.NOTIFY_CHANGED, null, null);
 			EventManager.getInstance().fireEvent(event);
 		}
 
 		// Get the list of old children (update node instances where possible)
 		final List<IProcessContextNode> oldChildren = model.getChildren(IProcessContextNode.class);
 
 		// Refresh the process contexts from the agent
 		refreshContextChildren(oldChildren, model, null, 2, new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				final AtomicBoolean isDisposed = new AtomicBoolean();
 				Runnable runnable = new Runnable() {
 					@Override
 					public void run() {
 						isDisposed.set(model.isDisposed());
 					}
 				};
 				if (Protocol.isDispatchThread()) runnable.run();
 				else Protocol.invokeAndWait(runnable);
 
 				if (!isDisposed.get()) {
 					// If there are remaining old children, remove them from the model (non-recursive)
 					for (IProcessContextNode oldChild : oldChildren) model.getService(IModelUpdateService.class).remove(oldChild);
 				}
 
 				if (refreshable != null && resetPendingNode.get()) {
 					resetPendingNode.set(false);
 					// Mark the refresh as done
 					refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 					// Reset the pending operation node
 					refreshable.setPendingOperationNode(null);
 					// Trigger a refresh of the view content.
 					ChangeEvent event = new ChangeEvent(model, IContainerModelNode.NOTIFY_CHANGED, null, null);
 					EventManager.getInstance().fireEvent(event);
 				}
 
 				// Invoke the callback
 				if (callback != null) callback.done(this, Status.OK_STATUS);
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService#refresh(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void refresh(IModelNode node, ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		refresh(node, NONE, callback);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService#refresh(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode, int, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void refresh(IModelNode node, int flags, ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Get the parent model
 		final IRuntimeModel model = getModel();
 
 		// If the parent model is already disposed, the service will drop out immediately
 		if (model.isDisposed() || !(node instanceof IProcessContextNode)) {
 			if (callback != null) callback.done(this, Status.OK_STATUS);
 			return;
 		}
 
 		// Perform the refresh of the node
 		doRefresh(model, node, flags, callback);
 	}
 
 	/**
 	 * Performs the refresh of the given model node.
 	 *
 	 * @param model The runtime model. Must not be <code>null</code>.
 	 * @param node  The node. Must not be <code>null</code>.
 	 * @param flags The flags. See the defined constants for details.
 	 * @param callback The callback to invoke once the refresh operation finished, or <code>null</code>.
 	 */
 	protected void doRefresh(final IRuntimeModel model, final IModelNode node, final int flags, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(model);
 		Assert.isNotNull(node);
 
 		// Refresh the process context from the agent
 		refreshContext(model, node, new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				if (status.getSeverity() == IStatus.ERROR) {
 					if (callback != null) callback.done(caller, status);
 					return;
 				}
 
 				// Get the list of old children (update node instances where possible)
 				final List<IProcessContextNode> oldChildren = ((IProcessContextNode)node).getChildren(IProcessContextNode.class);
 
 				// Refresh the children of the process context node from the agent
 				refreshContextChildren(oldChildren, model, (IProcessContextNode)node, 2, new Callback() {
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						final AtomicBoolean isDisposed = new AtomicBoolean();
 						Runnable runnable = new Runnable() {
 							@Override
 							public void run() {
 								isDisposed.set(model.isDisposed());
 							}
 						};
 						if (Protocol.isDispatchThread()) runnable.run();
 						else Protocol.invokeAndWait(runnable);
 
 						if (!isDisposed.get()) {
 							// If there are remaining old children, remove them from the parent node (recursive)
 							for (IProcessContextNode oldChild : oldChildren) ((IProcessContextNode)node).remove(oldChild, true);
 						}
 
 						// Invoke the callback
 						if (callback != null) callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 					}
 				});
 			}
 		});
 	}
 
 	/**
 	 * Process the given map of process contexts and update the given model.
 	 *
 	 * @param contexts The map of contexts to process. Must not be <code>null</code>.
 	 * @param oldChildren The list of old children. Must not be <code>null</code>.
 	 * @param model The model. Must not be <code>null</code>.
 	 * @param parent The parent context node or <code>null</code>.
 	 */
 	protected void processContexts(Map<UUID, IProcessContextNode> contexts, List<IProcessContextNode> oldChildren, IModel model, IProcessContextNode parent) {
 		Assert.isNotNull(contexts);
 		Assert.isNotNull(oldChildren);
 		Assert.isNotNull(model);
 
 		for (Entry<UUID, IProcessContextNode> entry : contexts.entrySet()) {
 			// Get the context instance for the current id
 			IProcessContextNode candidate = entry.getValue();
 			// Try to find an existing context node first
 			IModelNode[] nodes = model.getService(IModelLookupService.class).lkupModelNodesById(candidate.getStringProperty(IModelNode.PROPERTY_ID));
 			// If found, update the context node properties from the new one
 			if (nodes.length > 0) {
 				for (IModelNode node : nodes) {
 					model.getService(IModelUpdateService.class).update(node, candidate);
 					oldChildren.remove(node);
 				}
 			} else {
 				if (parent == null) {
 					model.getService(IModelUpdateService.class).add(candidate);
 				} else {
 					// Validate the the children are added to the real parent node
 					nodes = model.getService(IModelLookupService.class).lkupModelNodesById(parent.getStringProperty(IModelNode.PROPERTY_ID));
 					if (nodes.length > 0) {
 						// In fact we should have found just one parent
 						Assert.isTrue(nodes.length == 1);
 						Assert.isTrue(nodes[0] instanceof IProcessContextNode);
 						// Add to the real parent node
 						((IProcessContextNode)nodes[0]).add(candidate);
 					} else {
 						// Add to the passed in parent node
 						parent.add(candidate);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Refresh the given process context node.
 	 *
 	 * @param model The runtime model. Must not be <code>null</code>.
 	 * @param node  The node. Must not be <code>null</code>.
 	 * @param callback The callback to invoke once the refresh operation finished, or <code>null</code>.
 	 */
 	protected void refreshContext(final IRuntimeModel model, final IModelNode node, final ICallback callback) {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(model);
 		Assert.isNotNull(node);
 
 		// Get an open channel
 		IModelChannelService channelService = getModel().getService(IModelChannelService.class);
 		channelService.openChannel(new IModelChannelService.DoneOpenChannel() {
 			@Override
 			public void doneOpenChannel(Throwable error, final IChannel channel) {
 				if (error == null) {
 					final IProcesses service = channel.getRemoteService(IProcesses.class);
 					Assert.isNotNull(service);
 					final IProcessesV1 serviceV1 = channel.getRemoteService(IProcessesV1.class);
 					final ISysMonitor sysMonService = channel.getRemoteService(ISysMonitor.class);
 					Assert.isNotNull(sysMonService);
 					final String contextId = ((IProcessContextNode)node).getStringProperty(IModelNode.PROPERTY_ID);
 					sysMonService.getContext(contextId, new ISysMonitor.DoneGetContext() {
 						@Override
 						public void doneGetContext(IToken token, Exception error, SysMonitorContext context) {
 							((IProcessContextNode)node).setSysMonitorContext(context);
 
 							// Get the command line of the context
 							sysMonService.getCommandLine(contextId, new ISysMonitor.DoneGetCommandLine() {
 								@Override
 								public void doneGetCommandLine(IToken token, Exception error, String[] cmd_line) {
 									node.setProperty(IProcessContextNodeProperties.PROPERTY_CMD_LINE, error == null ? cmd_line : null);
 
 									// Get the process context
 									service.getContext(contextId, new IProcesses.DoneGetContext() {
 										@Override
 										public void doneGetContext(IToken token, Exception error, IProcesses.ProcessContext context) {
 											((IProcessContextNode)node).setProcessContext(context);
 											if (serviceV1 != null && context != null) {
 												serviceV1.getCapabilities(context.getID(), new IProcessesV1.DoneGetCapabilities() {
 													@Override
                                                     public void doneGetCapabilities(IToken token, Exception error, Map<String, Object> properties) {
 														((IProcessContextNode)node).setProperty(IProcessContextNodeProperties.PROPERTY_CAPABILITIES, properties);
 
 														callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
                                                     }
 												});
 											}
 											else
 												callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 										}
 									});
 								}
 							});
 						}
 					});
 				} else {
 					callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), error.getLocalizedMessage(), error));
 				}
 			}
 		});
 	}
 
 	/**
 	 * Refresh the children of the given process context node.
 	 *
 	 * @param oldChildren The list of old children. Must not be <code>null</code>.
 	 * @param model The model. Must not be <code>null</code>.
 	 * @param parent The parent context node or <code>null</code>.
 	 * @param depth Until which depth the tree gets refreshed.
 	 * @param callback The callback to invoke at the end of the operation. Must not be <code>null</code>.
 	 */
 	protected void refreshContextChildren(final List<IProcessContextNode> oldChildren, final IModel model, final IProcessContextNode parent, final int depth, final ICallback callback) {
 		Assert.isNotNull(oldChildren);
 		Assert.isNotNull(model);
 		Assert.isNotNull(callback);
 
 		// Make sure that the callback is invoked even for unexpected cases
 		try {
 			// The map of contexts created from the agents response
 			final Map<UUID, IProcessContextNode> contexts = new HashMap<UUID, IProcessContextNode>();
 
 			// Get an open channel
 			IModelChannelService channelService = getModel().getService(IModelChannelService.class);
 			channelService.openChannel(new IModelChannelService.DoneOpenChannel() {
 				@Override
 				public void doneOpenChannel(Throwable error, final IChannel channel) {
 					if (error == null) {
 						// Determine the parent context id
 						String parentContextId = null;
 						if (parent != null) parentContextId = parent.getStringProperty(IModelNode.PROPERTY_ID);
 
 						// Get the required services
 						final IProcesses service = channel.getRemoteService(IProcesses.class);
 						final IProcessesV1 serviceV1 = channel.getRemoteService(IProcessesV1.class);
 						final ISysMonitor sysMonService = channel.getRemoteService(ISysMonitor.class);
 
 						// At least the processes and the system monitor service must be available
 						if (service == null || sysMonService == null) {
 							callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 							return;
 						}
 
 						sysMonService.getChildren(parentContextId, new ISysMonitor.DoneGetChildren() {
 							@Override
 							public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
 								if (error == null) {
 									if (context_ids != null && context_ids.length > 0) {
 										final AsyncCallbackCollector collector = new AsyncCallbackCollector(new Callback() {
 											@Override
 											protected void internalDone(Object caller, IStatus status) {
 												Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 												if (status.getSeverity() == IStatus.OK) {
 													// Process the read process contexts
 													if (!contexts.isEmpty()) processContexts(contexts, oldChildren, model, parent);
 													callback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 												} else {
 													callback.done(RuntimeModelRefreshService.this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), status.getMessage(), status.getException()));
 												}
 											}
 										}, new CallbackInvocationDelegate());
 
 										// Loop the returned context id's and query the context data
 										for (String id : context_ids) {
 											final String contextId = id;
 											final ICallback innerCallback = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 											sysMonService.getContext(contextId, new ISysMonitor.DoneGetContext() {
 												@Override
 												public void doneGetContext(IToken token, Exception error, SysMonitorContext context) {
 													// Ignore errors. Some of the context might be OS context we do not have
 													// permissions to read the properties from.
 													if (context != null) {
 														final IProcessContextNode node = createContextNodeFrom(context);
 														Assert.isNotNull(node);
 														node.setType(parent == null ? TYPE.Process : TYPE.Thread);
 														contexts.put(node.getUUID(), node);
 
 														// Get the command line of the context
 														sysMonService.getCommandLine(contextId, new ISysMonitor.DoneGetCommandLine() {
 															@Override
 															public void doneGetCommandLine(IToken token, Exception error, String[] cmd_line) {
 																node.setProperty(IProcessContextNodeProperties.PROPERTY_CMD_LINE, error == null ? cmd_line : null);
 
 																// Query the corresponding process context
 																service.getContext(contextId, new IProcesses.DoneGetContext() {
 																	@Override
 																	public void doneGetContext(IToken token, Exception error, IProcesses.ProcessContext context) {
 																		// Errors are ignored
 																		node.setProcessContext(context);
 
 																		if (context != null) node.setProperty(IProcessContextNodeProperties.PROPERTY_NAME, context.getName());
 
																		if (serviceV1 != null && context != null) {
 																			serviceV1.getCapabilities(context.getID(), new IProcessesV1.DoneGetCapabilities() {
 																				@Override
 							                                                    public void doneGetCapabilities(IToken token, Exception error, Map<String, Object> properties) {
 																					node.setProperty(IProcessContextNodeProperties.PROPERTY_CAPABILITIES, properties);
 																					// Get the asynchronous refresh context adapter
 																					final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)node.getAdapter(IAsyncRefreshableCtx.class);
 
 																					// Refresh the children of the node if the depth is still larger than 0
 																					if (depth - 1 > 0 && (refreshable == null || !refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.IN_PROGRESS))) {
 																						if (refreshable != null) {
 																							// Mark the refresh as in progress
 																							refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 																							// Create a new pending operation node and associate it with the refreshable
 																							PendingOperationModelNode pendingNode = new PendingOperationNode();
 																							pendingNode.setParent(node);
 																							refreshable.setPendingOperationNode(pendingNode);
 																						}
 
 																						// Don't send change events while refreshing
 																						final boolean changed = node.setChangeEventsEnabled(false);
 																						// Initiate the refresh
 																						List<IProcessContextNode> oldChildren = node.getChildren(IProcessContextNode.class);
 																						refreshContextChildren(oldChildren, model, node, depth - 1, new Callback() {
 																							@Override
 					                                                                        protected void internalDone(Object caller, IStatus status) {
 																								// Mark the refresh as done
 																								refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 																								// Reset the pending operation node
 																								refreshable.setPendingOperationNode(null);
 																								// Re-enable the change events if they had been enabled before
 																								if (changed) node.setChangeEventsEnabled(true);
 																								// Trigger a refresh of the view content
 																								ChangeEvent event = new ChangeEvent(node, IContainerModelNode.NOTIFY_CHANGED, null, null);
 																								EventManager.getInstance().fireEvent(event);
 																								// Finally invoke the callback
 																								innerCallback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 																							}
 																						});
 																					} else {
 																						innerCallback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 																					}
 							                                                    }
 																			});
 																		}
 																		else {
 																			// Get the asynchronous refresh context adapter
 																			final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)node.getAdapter(IAsyncRefreshableCtx.class);
 
 																			// Refresh the children of the node if the depth is still larger than 0
 																			if (depth - 1 > 0 && (refreshable == null || !refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.IN_PROGRESS))) {
 																				if (refreshable != null) {
 																					// Mark the refresh as in progress
 																					refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 																					// Create a new pending operation node and associate it with the refreshable
 																					PendingOperationModelNode pendingNode = new PendingOperationNode();
 																					pendingNode.setParent(node);
 																					refreshable.setPendingOperationNode(pendingNode);
 																				}
 
 																				// Don't send change events while refreshing
 																				final boolean changed = node.setChangeEventsEnabled(false);
 																				// Initiate the refresh
 																				List<IProcessContextNode> oldChildren = node.getChildren(IProcessContextNode.class);
 																				refreshContextChildren(oldChildren, model, node, depth - 1, new Callback() {
 																					@Override
 			                                                                        protected void internalDone(Object caller, IStatus status) {
 																						// Mark the refresh as done
 																						refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 																						// Reset the pending operation node
 																						refreshable.setPendingOperationNode(null);
 																						// Re-enable the change events if they had been enabled before
 																						if (changed) node.setChangeEventsEnabled(true);
 																						// Trigger a refresh of the view content
 																						ChangeEvent event = new ChangeEvent(node, IContainerModelNode.NOTIFY_CHANGED, null, null);
 																						EventManager.getInstance().fireEvent(event);
 																						// Finally invoke the callback
 																						innerCallback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 																					}
 																				});
 																			} else {
 																				innerCallback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 																			}
 
 																		}
 																	}
 																});
 															}
 														});
 													} else {
 														innerCallback.done(RuntimeModelRefreshService.this, Status.OK_STATUS);
 													}
 												}
 											});
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
 	 * Create a context node instance from the given process context.
 	 *
 	 * @param context The system monitor context. Must not be <code>null</code>.
 	 * @return The context node instance.
 	 */
 	public IProcessContextNode createContextNodeFrom(SysMonitorContext context) {
 		Assert.isNotNull(context);
 
 		// Create a context node and associate the given context
 		IProcessContextNode node = getModel().getFactory().newInstance(IProcessContextNode.class);
 		node.setSysMonitorContext(context);
 
 		// Re-create the context properties from the context
 		node.setProperty(IProcessContextNodeProperties.PROPERTY_ID, context.getID());
 
 		return node;
 	}
 }
