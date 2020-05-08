 /*******************************************************************************
  * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.navigator.runtime;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.model.PendingOperationModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.ModelManager;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IPendingOperationNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.nodes.PendingOperationNode;
 import org.eclipse.tcf.te.tcf.processes.ui.navigator.events.TreeViewerListener;
 
 
 /**
  * Runtime model content provider delegate implementation.
  */
 public class ContentProvider implements ITreeContentProvider {
 	private final static Object[] NO_ELEMENTS = new Object[0];
 
 	// Reference to the tree listener
 	/* default */ ITreeViewerListener listener = null;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
 	 */
 	@Override
     public Object[] getChildren(Object parentElement) {
 		Object[] children = NO_ELEMENTS;
 
 		// If the parent element is a peer model node, than return
 		// the children of the corresponding runtime model.
 		if (parentElement instanceof IPeerModel) {
 			IRuntimeModel model = ModelManager.getRuntimeModel((IPeerModel)parentElement);
 			return isRuntimeModelNodeVisible() ? new Object[] { model } : getChildren(model);
 		}
 
 		// If it is the runtime model, get the process contexts
 		if (parentElement instanceof IRuntimeModel) {
 			// Get the asynchronous refresh context adapter
 			final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)((IRuntimeModel)parentElement).getAdapter(IAsyncRefreshableCtx.class);
 			if (refreshable != null) {
 				if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.PENDING)) {
 					// Mark the refresh as in progress
 					refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 					// Create a new pending operation node and associate it with the refreshable
 					PendingOperationModelNode pendingNode = new PendingOperationNode();
 					pendingNode.setParent((IRuntimeModel)parentElement);
 					refreshable.setPendingOperationNode(pendingNode);
 					// Cast to the model instance
 					final IRuntimeModel model = (IRuntimeModel)parentElement;
 					// Create the runnable
 					Runnable runnable = new Runnable() {
 						@Override
 						public void run() {
 							// Don't send change events while refreshing
 							final boolean changed = model.setChangeEventsEnabled(false);
 							// Initiate the refresh of the model
 							model.getService(IModelRefreshService.class).refresh(new Callback() {
 								@Override
 								protected void internalDone(Object caller, IStatus status) {
 									// Mark the refresh as done
 									refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
 									// Reset the pending operation node
 									refreshable.setPendingOperationNode(null);
 									// Re-enable the change events if they had been enabled before
 									if (changed) model.setChangeEventsEnabled(true);
 									// Trigger a refresh of the view content.
 									ChangeEvent event = new ChangeEvent(model, IContainerModelNode.NOTIFY_CHANGED, null, null);
 									EventManager.getInstance().fireEvent(event);
 								}
 							});
 						}
 					};
 					Protocol.invokeLater(runnable);
 
 					// Return the pending operation node
 					return new Object[] { refreshable.getPendingOperationNode() };
 				}
 				else if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.IN_PROGRESS)) {
 					// Refresh is still running -> return the pending operation node (if set)
 					return refreshable.getPendingOperationNode() != null ? new Object[] { refreshable.getPendingOperationNode() } : NO_ELEMENTS;
 				}
 			}
 
 			children = ((IRuntimeModel)parentElement).getChildren(IProcessContextNode.class).toArray();
 		}
 
 		// If it is a system context, get the system context children
 		else if (parentElement instanceof IProcessContextNode) {
 			// Get the asynchronous refresh context adapter
 			final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)((IProcessContextNode)parentElement).getAdapter(IAsyncRefreshableCtx.class);
 			if (refreshable != null) {
 				if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.PENDING)) {
 					// Mark the refresh as in progress
 					refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
 					// Create a new pending operation node and associate it with the refreshable
 					PendingOperationModelNode pendingNode = new PendingOperationNode();
 					pendingNode.setParent((IProcessContextNode)parentElement);
 					refreshable.setPendingOperationNode(pendingNode);
 					// Cast to the context node instance
 					final IProcessContextNode node = (IProcessContextNode)parentElement;
 					// Create the runnable
 					Runnable runnable = new Runnable() {
 						@Override
 						public void run() {
 							// Don't send change events while refreshing
 							final boolean changed = node.setChangeEventsEnabled(false);
 							// Determine the runtime model
 							IRuntimeModel model = node.getParent(IRuntimeModel.class);
 							if (model != null) {
 								// Initiate the refresh of the node
 								model.getService(IModelRefreshService.class).refresh(node, new Callback() {
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
 									}
 								});
 							}
 						}
 					};
 					Protocol.invokeLater(runnable);
 
 					// Return the pending operation node
 					return new Object[] { refreshable.getPendingOperationNode() };
 				}
 				else if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.IN_PROGRESS)) {
 					// Refresh is still running -> return the pending operation node (if set)
 					return refreshable.getPendingOperationNode() != null ? new Object[] { refreshable.getPendingOperationNode() } : NO_ELEMENTS;
 				}
 			}
 
 			children = ((IProcessContextNode)parentElement).getChildren(IProcessContextNode.class).toArray();
 		}
 
 		return children;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
 	 */
 	@Override
     public Object getParent(final Object element) {
 		final AtomicReference<Object> parent = new AtomicReference<Object>();
 
 		// The parent of the runtime model is the peer model node
 		if (element instanceof IRuntimeModel) {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					parent.set(((IRuntimeModel)element).getPeerModel());
 				}
 			};
 
 			if (Protocol.isDispatchThread()) runnable.run();
 			else Protocol.invokeAndWait(runnable);
 		}
 		else if (element instanceof IProcessContextNode) {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					parent.set(((IProcessContextNode)element).getParent());
 				}
 			};
 
 			if (Protocol.isDispatchThread()) runnable.run();
 			else Protocol.invokeAndWait(runnable);
 		}
 		else if (element instanceof IPendingOperationNode) {
 			parent.set(((IPendingOperationNode)element).getParent());
 		}
 
 		if (parent.get() instanceof IRuntimeModel) {
 			parent.set(getParent(parent.get()));
 		}
 
 		return parent.get();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
 	 */
 	@Override
     public boolean hasChildren(final Object element) {
 		// Default to "no children". This applies to IProcessContextNode
 		// and IPendingOperationNode elements.
 		boolean hasChildren = false;
 
 		if (element instanceof IRuntimeModel) {
 			IRuntimeModel model = ((IRuntimeModel)element);
 			// Get the asynchronous refresh context adapter
 			final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)model.getAdapter(IAsyncRefreshableCtx.class);
 			if (refreshable != null && refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.PENDING)) {
 				hasChildren = true;
 			} else {
 				hasChildren = model.hasChildren();
 			}
 		} else if (element instanceof IProcessContextNode) {
 			final IProcessContextNode context = (IProcessContextNode)element;
 
 			// Get the asynchronous refresh context adapter
 			final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)context.getAdapter(IAsyncRefreshableCtx.class);
 			if (refreshable != null && refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.PENDING)) {
 				hasChildren = true;
 			} else {
 				// We need the real children list to determine if we have children
 				final Object[] children = getChildren(context);
 				if (children.length == 1 && children[0] instanceof IProcessContextNode) {
 					// Apply the single thread filter
 					final AtomicBoolean selected = new AtomicBoolean(true);
 					Runnable runnable = new Runnable() {
 						@Override
 						public void run() {
 							IProcessContextNode child = (IProcessContextNode)children[0];
							if (child != null && context.getSysMonitorContext().getPID() == child.getSysMonitorContext().getPID()) {
 								if (context.getName() != null) {
 									selected.set(!context.getName().equals(child.getName()));
 								}
 								else if (child.getName() != null) {
 									selected.set(!child.getName().equals(context.getName()));
 								}
 							}
 						}
 					};
 
 					Assert.isTrue(!Protocol.isDispatchThread());
 					Protocol.invokeAndWait(runnable);
 
 					hasChildren = selected.get();
 				} else {
 					hasChildren = children.length > 0;
 				}
 			}
 		}
 		else if (element instanceof IPeerModel) {
 			hasChildren = true;
 		}
 
 		return hasChildren;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	@Override
     public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	@Override
     public void dispose() {
 		ModelManager.disposeAllRuntimeModels();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
     public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		// Initialize the tree listener if necessary
 		if (listener == null && viewer instanceof TreeViewer) {
 			final TreeViewer treeViewer = (TreeViewer) viewer;
 
 			listener = new TreeViewerListener();
 			treeViewer.addTreeListener(listener);
 			treeViewer.getTree().addDisposeListener(new DisposeListener() {
 				@Override
 				public void widgetDisposed(DisposeEvent e) {
 					if (listener != null) {
 						treeViewer.removeTreeListener(listener);
 						listener = null;
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Returns if or if not the root node, the runtime model node, is
 	 * visible.
 	 *
 	 * @return <code>True</code> if the runtime model node is visible, <code>false</code> otherwise.
 	 */
 	protected boolean isRuntimeModelNodeVisible() {
 		return true;
 	}
 }
