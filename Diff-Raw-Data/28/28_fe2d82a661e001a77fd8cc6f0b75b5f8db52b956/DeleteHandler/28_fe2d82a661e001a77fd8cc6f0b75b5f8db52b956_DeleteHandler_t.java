 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.handler;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IUIService;
 import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerUtil;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.interfaces.handler.IDeleteHandlerDelegate;
 import org.eclipse.tcf.te.ui.views.Managers;
 import org.eclipse.tcf.te.ui.views.ViewsUtil;
 import org.eclipse.tcf.te.ui.views.editor.EditorInput;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategoryManager;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * Delete handler implementation.
  */
 public class DeleteHandler extends AbstractHandler {
 	// Remember the shell from the execution event
 	private Shell shell = null;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		// Get the shell
 		shell = HandlerUtil.getActiveShell(event);
 		// Get the current selection
 		ISelection selection = HandlerUtil.getCurrentSelection(event);
 		// Delete the selection
 		if (selection != null) {
 			delete(selection, new Callback() {
 				@Override
 				protected void internalDone(Object caller, IStatus status) {
 					// Refresh the view
 					ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
 				}
 			});
 		}
 		// Reset the shell
 		shell = null;
 
 		return null;
 	}
 
 	/**
 	 * Tests if this delete handler can delete the elements of the given
 	 * selection.
 	 *
 	 * @param selection The selection. Must not be <code>null</code>.
 	 * @return <code>True</code> if the selection can be deleted by this handler, <code>false</code> otherwise.
 	 */
 	public boolean canDelete(ISelection selection) {
 		Assert.isNotNull(selection);
 
 		boolean canDelete = false;
 
 		// The selection must be a tree selection and must not be empty
 		if (selection instanceof ITreeSelection && !selection.isEmpty()) {
 			// Assume the selection to be deletable
 			canDelete = true;
 			// Iterate the selection. All elements must be of type IPeerModel
 			for (TreePath treePath : ((ITreeSelection)selection).getPaths()) {
 				// Get the element
 				Object element = treePath.getLastSegment();
 				// This handler will take care of peer model nodes only
 				if (!(element instanceof IPeerModel)) {
 					canDelete = false;
 					break;
 				}
 
 				// Check if there is a delete handler delegate for the element
 				IUIService service = ServiceManager.getInstance().getService(element, IUIService.class);
 				IDeleteHandlerDelegate delegate = service != null ? service.getDelegate(element, IDeleteHandlerDelegate.class) : null;
 				// If a delegate is available, ask the handler first if the given element is currently deletable
 				if (delegate != null) canDelete = delegate.canDelete(treePath);
 				// If the element is still marked deletable, apply the default check too
 				if (canDelete) {
 					// Determine if the selected peer model is static
 					boolean isStatic = isStatic((IPeerModel)element);
 					// Determine if the selected peer model represents an agent
 					// started by the current user
 					boolean isStartedByCurrentUser = isStartedByCurrentUser((IPeerModel)element);
 					// Static nodes can be handled the one way or the other.
 					// For dynamic nodes, "delete" means "remove from <category>",
 					// and this works only if the parent category is not "Neighborhood".
 					if (!isStatic) {
 						// Determine the parent category of the current tree path
 						ICategory category = treePath.getFirstSegment() instanceof ICategory ? (ICategory)treePath.getFirstSegment() : null;
 						if (category != null) {
 							if (IUIConstants.ID_CAT_NEIGHBORHOOD.equals(category.getId())) {
 								canDelete = false;
 								break;
 							}
 							else if (IUIConstants.ID_CAT_MY_TARGETS.equals(category.getId()) && isStartedByCurrentUser) {
 								canDelete = false;
 								break;
 							}
 						}
 					}
 				}
 
 				if (!canDelete) {
 					break;
 				}
 			}
 		}
 
 		return canDelete;
 	}
 
 	/**
 	 * Determines if the given peer model node is a static node.
 	 *
 	 * @param node The peer model node. Must not be <code>null</code>.
 	 * @return <code>True</code> if the node is static, <code>false</code> otherwise.
 	 */
 	private boolean isStatic(final IPeerModel node) {
 		Assert.isNotNull(node);
 
 		final AtomicBoolean isStatic = new AtomicBoolean();
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				isStatic.set(node.isStatic());
 			}
 		};
 
 		if (Protocol.isDispatchThread()) {
 			runnable.run();
 		}
 		else {
 			Protocol.invokeAndWait(runnable);
 		}
 
 		return isStatic.get();
 	}
 
 	/**
 	 * Determines if the given peer model node represents an agent started
 	 * by the current user.
 	 *
 	 * @param node The peer model node. Must not be <code>null</code>.
 	 * @return <code>True</code> if the node represents and agent started by the current user,
 	 *         <code>false</code> otherwise.
 	 */
 	private boolean isStartedByCurrentUser(final IPeerModel node) {
 		Assert.isNotNull(node);
 
 		final AtomicReference<String> username = new AtomicReference<String>();
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				username.set(node.getPeer().getUserName());
 			}
 		};
 
 		if (Protocol.isDispatchThread()) {
 			runnable.run();
 		}
 		else {
 			Protocol.invokeAndWait(runnable);
 		}
 
 		return System.getProperty("user.name").equals(username.get()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Internal helper class to describe the delete operation to perform.
 	 */
 	private static class Operation {
 		// The operation types
 		public enum TYPE { Remove, Unlink }
 
 		// The element to operate on
 		public IPeerModel node;
 		// The operation type to perform
 		public TYPE type;
 		// In case of an "unlink" operation, the parent category
 		// is required.
 		public ICategory parentCategory;
 
 		/**
 		 * Constructor.
 		 */
 		public Operation() {
 		}
 
 		/**
 		 * Executes the operation.
 		 *
 		 * @throws Exception if the operation fails.
 		 */
 		public void execute() throws Exception {
 			Assert.isNotNull(node);
 			Assert.isNotNull(type);
 
 			if (TYPE.Remove.equals(type)) {
				// Remove the node from the persistence storage
 				IURIPersistenceService service = ServiceManager.getInstance().getService(IURIPersistenceService.class);
				if (service == null) throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
 				service.delete(node, null);
 
				// Close the configuration editor if open
 				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 				IEditorPart editor = page.findEditor(new EditorInput(node));
 				if (editor != null) {
 					page.closeEditor(editor, false);
 				}

				// Check if there is a delete handler delegate for the element
				IUIService uiService = ServiceManager.getInstance().getService(node, IUIService.class);
				IDeleteHandlerDelegate delegate = uiService != null ? uiService.getDelegate(node, IDeleteHandlerDelegate.class) : null;
				// If a delegate is available, signal the execution of the remove
				if (delegate != null) delegate.postDelete(node);
 			}
 			else if (TYPE.Unlink.equals(type)) {
 				Assert.isNotNull(parentCategory);
 
 				ICategoryManager manager = Managers.getCategoryManager();
 				Assert.isNotNull(manager);
 
 				ICategorizable categorizable = (ICategorizable)node.getAdapter(ICategorizable.class);
 				if (categorizable == null) {
 					categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(node, ICategorizable.class);
 				}
 				Assert.isNotNull(categorizable);
 
 				manager.remove(parentCategory.getId(), categorizable.getId());
 			}
 		}
 	}
 
 	/**
 	 * Deletes all elements from the given selection and invokes the
 	 * given callback once done.
 	 *
 	 * @param selection The selection. Must not be <code>null</code>.
 	 * @param callback The callback. Must not be <code>null</code>.
 	 */
 	public void delete(final ISelection selection, final ICallback callback) {
 		Assert.isNotNull(selection);
 		Assert.isNotNull(callback);
 
 		// The callback needs to be invoked in any case. However, if called
 		// from an asynchronous callback, set this flag to false.
 		boolean invokeCallback = true;
 
 		// The selection must be a tree selection and must not be empty
 		if (selection instanceof ITreeSelection && !selection.isEmpty()) {
 			// Determine the operations to perform for each of the selected elements
 			Operation[] operations = selection2operations((ITreeSelection)selection);
 
 			// Seek confirmation for the "remove" operations. If the user deny it,
 			// everything, including the "unlink" operations are cancelled.
 			boolean confirmed = confirmDelete(operations);
 
 			// Execute the operations
 			if (confirmed) {
 				// If one of the operation is a "remove" operation, the locator
 				// model needs to be refreshed
 				boolean refreshModel = false;
 
 				try {
 					for (Operation op : operations) {
 						if (Operation.TYPE.Remove.equals(op.type)) {
 							refreshModel = true;
 						}
 						op.execute();
 					}
 				} catch (Exception e) {
 					String template = NLS.bind(Messages.DeleteHandler_error_deleteFailed, Messages.PossibleCause);
 					StatusHandlerUtil.handleStatus(StatusHelper.getStatus(e), selection, template,
 													Messages.DeleteHandler_error_title, IContextHelpIds.MESSAGE_DELETE_FAILED, this, null);
 				}
 
 				if (refreshModel) {
 					// Trigger a refresh of the model
 					invokeCallback = false;
 					Protocol.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							ILocatorModelRefreshService service = Model.getModel().getService(ILocatorModelRefreshService.class);
 							// Refresh the model now (must be executed within the TCF dispatch thread)
 							if (service != null) service.refresh(new Callback() {
 								@Override
 								protected void internalDone(Object caller, IStatus status) {
 									// Invoke the callback
 									callback.done(DeleteHandler.this, Status.OK_STATUS);
 								}
 							});
 						}
 					});
 				}
 			}
 		}
 
 		if (invokeCallback) {
 			callback.done(this, Status.OK_STATUS);
 		}
 	}
 
 	/**
 	 * Analyze the given selection and convert it to an list of operations
 	 * to perform.
 	 *
 	 * @param selection The selection. Must not be <code>null</code>.
 	 * @return The list of operations.
 	 */
 	private Operation[] selection2operations(ITreeSelection selection) {
 		Assert.isNotNull(selection);
 
 		List<Operation> operations = new ArrayList<Operation>();
 
 		// Iterate the selection. All elements must be of type IPeerModel
 		for (TreePath treePath : selection.getPaths()) {
 			// Get the element
 			Object element = treePath.getLastSegment();
 			Assert.isTrue(element instanceof IPeerModel);
 			IPeerModel node = (IPeerModel)element;
 
 			boolean isStatic = isStatic(node);
 			ICategory category = treePath.getFirstSegment() instanceof ICategory ? (ICategory)treePath.getFirstSegment() : null;
 
 			if (category == null && isStatic) {
 				Operation op = new Operation();
 				op.node = node;
 				op.type = Operation.TYPE.Remove;
 				operations.add(op);
 			} else if (category != null) {
 				// If the parent category is "Favorites", it is always
 				// an "unlink" operation
 				if (IUIConstants.ID_CAT_FAVORITES.equals(category.getId())) {
 					Operation op = new Operation();
 					op.node = node;
 					op.type = Operation.TYPE.Unlink;
 					op.parentCategory = category;
 					operations.add(op);
 				}
 				// If the parent category is "My Targets", is is an
 				// "remove" operation for static peers and "unlink" for
 				// dynamic peers
 				else if (IUIConstants.ID_CAT_MY_TARGETS.equals(category.getId())) {
 					Operation op = new Operation();
 					op.node = node;
 
 					if (isStatic) {
 						op.type = Operation.TYPE.Remove;
 					} else {
 						op.type = Operation.TYPE.Unlink;
 						op.parentCategory = category;
 					}
 
 					operations.add(op);
 				}
 				else {
 					Operation op = new Operation();
 					op.node = node;
 					op.type = Operation.TYPE.Remove;
 
 					operations.add(op);
 				}
 			}
 		}
 
 		return operations.toArray(new Operation[operations.size()]);
 	}
 
 	/**
 	 * Confirm the deletion with the user.
 	 *
 	 * @param state The state of delegation handler.
 	 * @return true if the user agrees to delete or it has confirmed previously.
 	 */
 	private boolean confirmDelete(Operation[] operations) {
 		Assert.isNotNull(operations);
 
 		boolean confirmed = false;
 
 		// Find all elements to remove
 		List<Operation> toRemove = new ArrayList<Operation>();
 		for (Operation op : operations) {
 			if (Operation.TYPE.Remove.equals(op.type)) {
 				toRemove.add(op);
 			}
 		}
 
 		// If there are node to remove -> ask for confirmation
 		if (!toRemove.isEmpty()) {
 			String question = getConfirmQuestion(toRemove);
 			Shell parent = shell != null ? shell : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 			confirmed = MessageDialog.openQuestion(parent, Messages.DeleteHandlerDelegate_DialogTitle, question);
 		} else {
 			confirmed = true;
 		}
 
 		return confirmed;
 	}
 
 	/**
 	 * Get confirmation question displayed in the confirmation dialog.
 	 *
 	 * @param toRemove The list of nodes to remove.
 	 * @return The question to ask the user.
 	 */
 	private String getConfirmQuestion(List<Operation> toRemove) {
 		Assert.isNotNull(toRemove);
 
 		String question;
 		if (toRemove.size() == 1) {
 			final Operation op = toRemove.get(0);
 			final AtomicReference<String> name = new AtomicReference<String>();
 
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					name.set(op.node.getPeer().getName());
 				}
 			};
 
 			if (Protocol.isDispatchThread()) {
 				runnable.run();
 			}
 			else {
 				Protocol.invokeAndWait(runnable);
 			}
 
 			question = NLS.bind(Messages.DeleteHandlerDelegate_MsgDeleteOnePeer, name.get());
 		}
 		else {
 			question = NLS.bind(Messages.DeleteHandlerDelegate_MsgDeleteMultiplePeers, Integer.valueOf(toRemove.size()));
 		}
 		return question;
 	}
 }
