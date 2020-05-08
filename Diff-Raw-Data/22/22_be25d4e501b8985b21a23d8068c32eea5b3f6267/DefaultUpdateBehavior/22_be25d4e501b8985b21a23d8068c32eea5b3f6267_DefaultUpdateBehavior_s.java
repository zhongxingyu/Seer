 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 329523 - Add notification of DiagramTypeProvider after saving a diagram
  *    mwenz - Bug 347152 - Do not log diagnostics errors as errors in the Eclipse error log
  *    mwenz - Bug 359928 - DiagramEditorBehavior does not initialize adapterActive field
 *    Bug 336488 - DiagramEditor API
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.IOperationHistoryListener;
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.OperationHistoryEvent;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.workspace.IWorkspaceCommandStack;
 import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.editor.DomainModelWorkspaceSynchronizerDelegate;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @since 0.9
  */
 public class DefaultUpdateBehavior extends PlatformObject implements IEditingDomainProvider, IOperationHistoryListener {
 
 	protected final DiagramEditor diagramEditor;
 
 	private TransactionalEditingDomain editingDomain;
 
 	/**
 	 * Closes editor if model object is deleted.
 	 */
 	private ElementDeleteListener elementDeleteListener = null;
 
 	/**
 	 * The update adapter is added to every {@link Resource} adapters in the
 	 * {@link ResourceSet} of the {@link TransactionalEditingDomain}. When
 	 * notified, it adds an {@link DefaultUpdateBehavior#updateAdapter} to the
 	 * adapters of the ResourceSet.
 	 * 
 	 * @see DefaultUpdateBehavior#initializeEditingDomain(TransactionalEditingDomain)
 	 */
 	private ResourceSetUpdateAdapter resourceSetUpdateAdapter;
 
 	/**
 	 * Is toggled by {@link DefaultUpdateBehavior#updateAdapter}.
 	 */
 	private boolean resourceDeleted = false;
 
 	/**
 	 * Is toggled by {@link DefaultUpdateBehavior#updateAdapter}.
 	 */
 	private boolean resourceChanged = false;
 
 	// Note: there are some concerns out there that the Synchronizer does not
 	// work for inter-EditingDomain-eventing, @see
 	// http://www.eclipse.org/forums/index.php?t=msg&th=140242&start=0&
 	private WorkspaceSynchronizer workspaceSynchronizer;
 
 	/**
 	 * Flag that indicates if the {@link #updateAdapter} shall be active or not.
 	 * It may be deactivated when mass operations (e.g. saving the diagram
 	 * editor with all its resources) take place. Use the methods
 	 * {@link #isAdapterActive()} and {@link #setAdapterActive(boolean)} to
 	 * access this field.
 	 */
 	private boolean adapterActive = true;
 
 	/**
 	 * The default update that cares about refreshing the diagram editor in case
 	 * of resource changes. May be disabled by overriding
 	 * {@link #isAdapterActive()} and returning false.
 	 */
 	private Adapter updateAdapter = new AdapterImpl() {
 		@Override
 		public void notifyChanged(Notification msg) {
 			if (!isAdapterActive()) {
 				return;
 			}
 			if (msg.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED) {
 				if (msg.getNewBooleanValue() == Boolean.FALSE) {
 					final Resource resource = (Resource) msg.getNotifier();
 					final URI uri = resource.getURI();
 					if (editingDomain.getResourceSet().getURIConverter().exists(uri, null)) {
 						// file content has changes
 						setResourceChanged(true);
 						final IEditorPart activeEditor = diagramEditor.getSite().getPage().getActiveEditor();
 						if (activeEditor == diagramEditor) {
 							getShell().getDisplay().asyncExec(new Runnable() {
 								public void run() {
 									handleActivate();
 								}
 							});
 						}
 					} else {
 						// file has been deleted
 						if (!diagramEditor.isDirty()) {
 							final IEditorInput editorInput = diagramEditor.getEditorInput();
 							if (editorInput instanceof IDiagramEditorInput) {
 								final IDiagramEditorInput input = (IDiagramEditorInput) editorInput;
 								URI inputUri = input.getUri();
 								URI diagUri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(uri);
 								if (diagUri.equals(inputUri)) {
 									startCloseEditorJob();
 								}
 							}
 						} else {
 							setResourceDeleted(true);
 							final IEditorPart activeEditor = diagramEditor.getSite().getPage().getActiveEditor();
 							if (activeEditor == diagramEditor) {
 								getShell().getDisplay().asyncExec(new Runnable() {
 									public void run() {
 										handleActivate();
 									}
 								});
 							}
 						}
 					}
 				}
 			}
 			super.notifyChanged(msg);
 		}
 
 		private void startCloseEditorJob() {
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					closeEditor();
 				}
 			});
 		}
 	};
 
 	/**
 	 * Creates a new {@link DefaultUpdateBehavior} instance associated with the
 	 * given {@link DiagramEditor}.
 	 * 
 	 * @param diagramEditor
 	 *            the part this model editor works on
 	 */
 	public DefaultUpdateBehavior(DiagramEditor diagramEditor) {
 		super();
 		this.diagramEditor = diagramEditor;
 	}
 
 	/**
 	 * Returns the flag that indicates if the underlying resource of the
 	 * {@link Diagram} has been deleted. Note that this flag will only be
 	 * updated in case the {@link #updateAdapter} is enabled, see
 	 * {@link #adapterActive}, {@link #isAdapterActive()} and
 	 * {@link #setAdapterActive(boolean)}. If this flag is set the editor will
 	 * close on receiving the next event.
 	 * 
 	 * @return <code>true</code> in case the resource has been deleted,
 	 *         <code>false</code> otherwise
 	 */
 	protected boolean isResourceDeleted() {
 		return resourceDeleted;
 	}
 
 	/**
 	 * Sets the flag that indicates if the underlying resource of the
 	 * {@link Diagram} has been deleted. Note that this flag should only be
 	 * updated by the {@link #updateAdapter}, see {@link #adapterActive},
 	 * {@link #isAdapterActive()} and {@link #setAdapterActive(boolean)}.
 	 * <p>
 	 * Should not be called by external clients.
 	 * 
 	 * @param resourceDeleted
 	 *            the value to set the flag to, <code>true</code> indicates that
 	 *            the resource has been deleted.
 	 * @noreference This method is not intended to be referenced by clients.
 	 */
 	public void setResourceDeleted(boolean resourceDeleted) {
 		this.resourceDeleted = resourceDeleted;
 	}
 
 	/**
 	 * Returns the flag that indicates if the underlying resource of the
 	 * {@link Diagram} has been changed. Note that this flag will only be
 	 * updated in case the {@link #updateAdapter} is enabled, see
 	 * {@link #adapterActive}, {@link #isAdapterActive()} and
 	 * {@link #setAdapterActive(boolean)}.
 	 * 
 	 * @return <code>true</code> in case the resource has been changed,
 	 *         <code>false</code> otherwise
 	 */
 	protected boolean isResourceChanged() {
 		return resourceChanged;
 	}
 
 	/**
 	 * Sets the flag that indicates if the underlying resource of the
 	 * {@link Diagram} has been changed. Note that this flag should only be
 	 * updated by the {@link #updateAdapter}, see {@link #adapterActive},
 	 * {@link #isAdapterActive()} and {@link #setAdapterActive(boolean)}.
 	 * <p>
 	 * Should not be called by external clients.
 	 * 
 	 * @param resourceChanged
 	 *            the value to set the flag to, <code>true</code> indicates that
 	 *            the resource has been changed.
 	 * @noreference This method is not intended to be referenced by clients.
 	 */
 	public void setResourceChanged(boolean resourceChanged) {
 		this.resourceChanged = resourceChanged;
 	}
 
 	/**
 	 * Handles activation of the editor. In case of the underlying diagram
 	 * resource being deleted ({@link #resourceDeleted} is <code>true</code>)
 	 * the editor will be closed after a call to {@link #handleDirtyConflict()}
 	 * that returns <code>true</code>. Also it will call
 	 * {@link #handleChangedResources()} in case the underlying diagram resource
 	 * has changed ({@link #resourceChanged} is <code>true</code>).
 	 */
 	public void handleActivate() {
 		if (isResourceDeleted()) {
 			if (handleDirtyConflict()) {
 				closeEditor();
 			} else {
 				setResourceDeleted(false);
 				setResourceChanged(false);
 			}
 		} else if (isResourceChanged()) {
 			handleChangedResources();
 			setResourceChanged(false);
 		}
 	}
 
 	/**
 	 * Returns the flag that indicates if the {@link #updateAdapter} shall be
 	 * active of not ({@link #adapterActive}). In case this method returns
 	 * <code>false</code>, the {@link #updateAdapter} will do nothing on being
 	 * called.
 	 * 
 	 * @return <code>true</code> in case the adapter shall run,
 	 *         <code>false</code> otherwise.
 	 */
 	protected boolean isAdapterActive() {
 		return adapterActive;
 	}
 
 	/**
 	 * Sets the flag that indicates if the {@link #updateAdapter} shall be
 	 * active of not ({@link #adapterActive}).
 	 * 
 	 * @param active
 	 *            the new value for the flag
 	 */
 	public void setAdapterActive(boolean active) {
 		adapterActive = active;
 	}
 
 	/**
	 * TODO Handles what to do with changed resources on activation.
 	 */
 	protected void handleChangedResources() {
 		if (!diagramEditor.isDirty() || handleDirtyConflict()) {
 			IUndoContext undoContext = ((IWorkspaceCommandStack) getEditingDomain().getCommandStack())
 					.getDefaultUndoContext();
 			getOperationHistory().dispose(undoContext, true, true, true);
 
 			// Disable adapters temporarily.
 			diagramEditor.disableAdapters();
 
 			try {
 				// We unload our resources such that refreshEditorContent does a
 				// complete diagram refresh.
 				EList<Resource> resources = getEditingDomain().getResourceSet().getResources();
 				for (Resource resource : resources) {
 					resource.unload();
 				}
 				diagramEditor.refreshContent();
 			} finally {
 				// Re-enable adapters again
 				diagramEditor.enableAdapters();
 			}
 		}
 	}
 
 	/**
 	 * Shows a dialog that asks if conflicting changes should be discarded or
 	 * not. See {@link #handleActivate()}.
 	 * 
 	 * @return <code>true</code> in case the editor shall be closed,
 	 *         <code>false</code> otherwise
 	 */
 	protected boolean handleDirtyConflict() {
 		return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				Messages.DiscardChangesDialog_0_xmsg, Messages.DiscardChangesDialog_1_xmsg);
 	}
 
 	/**
 	 * This returns the editing domain as required by the
 	 * {@link IEditingDomainProvider} interface.
 	 * 
 	 * @return The {@link TransactionalEditingDomain} that is used within this
 	 *         editor
 	 */
 	public TransactionalEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * Initializes listeners and adapters.
 	 */
 	public void init() {
 		for (final Resource r : getEditingDomain().getResourceSet().getResources()) {
 			r.eAdapters().add(updateAdapter);
 		}
 
 		// Retrieve the object from the editor input
 		final EObject object = (EObject) diagramEditor.getAdapter(Diagram.class);
 		// Register for object deletion
 		if (object != null) {
 			elementDeleteListener = new ElementDeleteListener(diagramEditor);
 			object.eAdapters().add(elementDeleteListener);
 		}
 
 		getOperationHistory().addOperationHistoryListener(this);
 	}
 
 	/**
 	 * Created the {@link TransactionalEditingDomain} that shall be used within
 	 * the diagram editor and initializes it by delegating to
 	 * {@link #initializeEditingDomain(TransactionalEditingDomain)}.
 	 */
 	protected void createEditingDomain() {
 		TransactionalEditingDomain editingDomain = GraphitiUiInternal.getEmfService()
 				.createResourceSetAndEditingDomain();
 		initializeEditingDomain(editingDomain);
 	}
 
 	/**
 	 * This sets up the editing domain for this model editor.
 	 * 
 	 * @param domain
 	 *            The {@link TransactionalEditingDomain} that is used within
 	 *            this model editor
 	 */
 	protected void initializeEditingDomain(TransactionalEditingDomain domain) {
 		editingDomain = domain;
 		final ResourceSet resourceSet = domain.getResourceSet();
 
 		resourceSetUpdateAdapter = new ResourceSetUpdateAdapter(updateAdapter);
 		resourceSet.eAdapters().add(resourceSetUpdateAdapter);
 
 		// Install synchronizer for editor-external changes to the files
 		// underlying the resources of the ED
 		workspaceSynchronizer = new WorkspaceSynchronizer(getEditingDomain(),
 				new DomainModelWorkspaceSynchronizerDelegate(diagramEditor));
 
 		// Problem analysis
 		diagramEditor.editingDomainInitialized();
 	}
 
 	/**
 	 * Disposes this {@link DefaultUpdateBehavior} and free all resources it
 	 * holds. In case you only want to omit or influence the disposal of the
 	 * {@link TransactionalEditingDomain}, you can also override
 	 * {@link #disposeEditingDomain()}.
 	 */
 	public void dispose() {
 		editingDomain.getResourceSet().eAdapters().remove(resourceSetUpdateAdapter);
 		resourceSetUpdateAdapter = null;
 
 		getOperationHistory().removeOperationHistoryListener(this);
 
 		for (Resource r : editingDomain.getResourceSet().getResources()) {
 			r.eAdapters().remove(updateAdapter);
 		}
 
 		EObject object = (EObject) diagramEditor.getAdapter(Diagram.class);
 		if (object != null) {
 			object.eAdapters().remove(elementDeleteListener);
 		}
 
 		workspaceSynchronizer.dispose();
 
 		// Remove reference
 		disposeEditingDomain();
 		editingDomain = null;
 	}
 
 	/**
 	 * Cares about disposing the {@link TransactionalEditingDomain} held in this
 	 * instance. Is called during the {@link #dispose()} method.
 	 */
 	protected void disposeEditingDomain() {
 		editingDomain.dispose();
 	}
 
 	/**
 	 * Is called by the operation history of the
 	 * {@link TransactionalEditingDomain} in case the history changes. Reacts on
 	 * undo and redo events and updates the dirty state of the editor.
 	 * 
 	 * @param event
 	 *            the {@link OperationHistoryEvent} to react upon
 	 */
 	public void historyNotification(OperationHistoryEvent event) {
 		switch (event.getEventType()) {
 		case OperationHistoryEvent.REDONE:
 		case OperationHistoryEvent.UNDONE:
 			diagramEditor.updateDirtyState();
 			break;
 		}
 	}
 
 	private Shell getShell() {
 		return diagramEditor.getSite().getShell();
 	}
 
 	private void closeEditor() {
 		if (diagramEditor == null) {
 			return;
 		}
 		IWorkbenchPartSite site = diagramEditor.getSite();
 		// Since we run asynchronously we have to check if our UI is still
 		// there.
 		if (site == null) {
 			return;
 		}
 		IWorkbenchPage page = site.getPage();
 		if (page == null) {
 			return;
 		}
 		page.closeEditor(diagramEditor, false);
 	}
 
 	private IOperationHistory getOperationHistory() {
 		IOperationHistory history = null;
 		TransactionalEditingDomain domain = getEditingDomain();
 		if (domain != null) {
 			IWorkspaceCommandStack commandStack = (IWorkspaceCommandStack) domain.getCommandStack();
 			if (commandStack != null) {
 				history = commandStack.getOperationHistory();
 			}
 		}
 		return history;
 	}
 }
