 /*******************************************************************************
  * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.ui.editors;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.ui.ViewerPane;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
 import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
 import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.virgo.ide.par.Par;
 import org.eclipse.virgo.ide.par.provider.ParItemProviderAdapterFactory;
 import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
 
 /**
  * @author Christian Dupuis
  */
 public class ParEditor extends SharedHeaderFormEditor implements ISelectionProvider {
 
 	public final static String EDITOR_ID = "org.eclipse.virgo.ide.par.ui.editor";
 
 	/**
 	 * This is the one adapter factory used for providing views of the model.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ComposedAdapterFactory adapterFactory;
 
 	/**
 	 * Resources that have been changed since last activation. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Collection<Resource> changedResources = new ArrayList<Resource>();
 
 	/**
 	 * This is the content outline page. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	protected IContentOutlinePage contentOutlinePage;
 
 	/**
 	 * This is a kludge... <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected IStatusLineManager contentOutlineStatusLineManager;
 
 	/**
 	 * This is the content outline page's viewer. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	protected TreeViewer contentOutlineViewer;
 
 	/**
 	 * This keeps track of the active content viewer, which may be either one of
 	 * the viewers in the pages or the content outline viewer. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Viewer currentViewer;
 
 	/**
 	 * This keeps track of the active viewer pane, in the book. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ViewerPane currentViewerPane;
 
 	/**
 	 * This keeps track of the editing domain that is used to track all changes
 	 * to the model. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected AdapterFactoryEditingDomain editingDomain;
 
 	/**
 	 * This keeps track of the selection of the editor as a whole. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ISelection editorSelection = StructuredSelection.EMPTY;
 
 	/**
 	 * This listens for when the outline becomes active <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected IPartListener partListener = new IPartListener() {
 		public void partActivated(IWorkbenchPart part) {
 			if (part instanceof ContentOutline) {
 				if (((ContentOutline) part).getCurrentPage() == contentOutlinePage) {
 					// getActionBarContributor().setActiveEditor(ParEditor.this);
 
 					setCurrentViewer(contentOutlineViewer);
 				}
 			}
 			else if (part == ParEditor.this) {
 				handleActivate();
 			}
 		}
 
 		public void partBroughtToTop(IWorkbenchPart p) {
 			// Ignore.
 		}
 
 		public void partClosed(IWorkbenchPart p) {
 			// Ignore.
 		}
 
 		public void partDeactivated(IWorkbenchPart p) {
 			// Ignore.
 		}
 
 		public void partOpened(IWorkbenchPart p) {
 			// Ignore.
 		}
 	};
 
 	/**
 	 * Resources that have been removed since last activation. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Collection<Resource> removedResources = new ArrayList<Resource>();
 
 	/**
 	 * This listens for workspace changes. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
 		public void resourceChanged(IResourceChangeEvent event) {
 			// Only listening to these.
 			// if (event.getType() == IResourceDelta.POST_CHANGE)
 			{
 				IResourceDelta delta = event.getDelta();
 				try {
 					class ResourceDeltaVisitor implements IResourceDeltaVisitor {
 						protected Collection<Resource> changedResources = new ArrayList<Resource>();
 
 						protected Collection<Resource> removedResources = new ArrayList<Resource>();
 
 						protected ResourceSet resourceSet = editingDomain.getResourceSet();
 
 						public Collection<Resource> getChangedResources() {
 							return changedResources;
 						}
 
 						public Collection<Resource> getRemovedResources() {
 							return removedResources;
 						}
 
 						public boolean visit(IResourceDelta delta) {
 							if (delta.getFlags() != IResourceDelta.MARKERS
 									&& delta.getResource().getType() == IResource.FILE) {
 								if ((delta.getKind() & (IResourceDelta.CHANGED | IResourceDelta.REMOVED)) != 0) {
 									Resource resource = resourceSet.getResource(
 											URI.createURI(delta.getFullPath().toString()), false);
 									if (resource != null) {
 										if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
 											removedResources.add(resource);
 										}
 										else if (!savedResources.remove(resource)) {
 											changedResources.add(resource);
 										}
 									}
 								}
 							}
 
 							return true;
 						}
 					}
 
 					ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
 					delta.accept(visitor);
 
 					if (!visitor.getRemovedResources().isEmpty()) {
 						removedResources.addAll(visitor.getRemovedResources());
 						if (!isDirty()) {
 							getSite().getShell().getDisplay().asyncExec(new Runnable() {
 								public void run() {
 									getSite().getPage().closeEditor(ParEditor.this, false);
 									ParEditor.this.dispose();
 								}
 							});
 						}
 					}
 
 					if (!visitor.getChangedResources().isEmpty()) {
 						changedResources.addAll(visitor.getChangedResources());
 						if (getSite().getPage().getActiveEditor() == ParEditor.this) {
 							getSite().getShell().getDisplay().asyncExec(new Runnable() {
 								public void run() {
 									handleActivate();
 								}
 							});
 						}
 					}
 				}
 				catch (CoreException exception) {
 					ServerIdeUiPlugin.getDefault().log(exception);
 				}
 			}
 		}
 	};
 
 	/**
 	 * Resources that have been saved. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * @generated
 	 */
 	protected Collection<Resource> savedResources = new ArrayList<Resource>();
 
 	/**
 	 * This listens to which ever viewer is active. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	protected ISelectionChangedListener selectionChangedListener;
 
 	/**
 	 * This keeps track of all the
 	 * {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are
 	 * listening to this editor. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
 	private Resource parResource;
 
 	private Par par;
 
 	/**
 	 * This creates a model editor. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * @generated
 	 */
 	public ParEditor() {
 		initializeEditingDomain();
 	}
 
 	@Override
 	protected void addPages() {
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.add(listener);
 	}
 
 	/**
 	 * This creates a context menu for the viewer and adds a listener as well
 	 * registering the menu for extension. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	protected void createContextMenuFor(StructuredViewer viewer) {
 		MenuManager contextMenu = new MenuManager("#PopUp");
 		contextMenu.add(new Separator("additions"));
 		contextMenu.setRemoveAllWhenShown(true);
 		// contextMenu.addMenuListener(this);
 		Menu menu = contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
 
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
 		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
 		viewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(editingDomain, viewer));
 	}
 
 	/**
 	 * This is the method called to load a resource into the editing domain's
 	 * resource set based on the editor's input. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	public void createModel() {
 		URI resourceURI = EditUIUtil.getURI(getEditorInput());
 		try {
 			// Load the resource through the editing domain.
 			//
 			parResource = editingDomain.getResourceSet().getResource(resourceURI, true);
 		}
 		catch (Exception e) {
 			ServerIdeUiPlugin.getDefault().log(e);
 			parResource = editingDomain.getResourceSet().getResource(resourceURI, false);
 		}
 
 		par = (Par) parResource.getContents().iterator().next();
 	}
 
 	/**
 	 * This is the method used by the framework to install your own controls.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void createPages() {
 		createModel();
 		addPages();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
 
 		getSite().getPage().removePartListener(partListener);
 
 		adapterFactory.dispose();
 
 		// if (getActionBarContributor().getActiveEditor() == this) {
 		// getActionBarContributor().setActiveEditor(null);
 		// }
 
 		if (contentOutlinePage != null) {
 			contentOutlinePage.dispose();
 		}
 
 		super.dispose();
 	}
 
 	/**
 	 * This is for implementing {@link IEditorPart} and simply saves the model
 	 * file. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void doSave(IProgressMonitor progressMonitor) {
 		// Save only resources that have actually changed.
 		//
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 
 		// Do the work within an operation because this is a long running
 		// activity that modifies the workbench.
 		//
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			// This is the method that gets invoked when the operation runs.
 			//
 			@Override
 			public void execute(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				//
 				boolean first = true;
 				for (Resource resource : editingDomain.getResourceSet().getResources()) {
 					if ((first || !resource.getContents().isEmpty() || isPersisted(resource))
 							&& !editingDomain.isReadOnly(resource)) {
 						try {
 							savedResources.add(resource);
 							resource.save(saveOptions);
 						}
 						catch (IOException exception) {
 							handleError(exception);
 						}
 						first = false;
 					}
 				}
 			}
 		};
 
 		try {
 			// This runs the options, and shows progress.
 			//
 			new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);
 
 			// Refresh the necessary state.
 			//
 			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
 			firePropertyChange(IEditorPart.PROP_DIRTY);
 		}
 		catch (Exception exception) {
 			// Something went wrong that shouldn't.
 			//
 			ServerIdeUiPlugin.getDefault().log(exception);
 		}
 	}
 
 	/**
 	 * This also changes the editor's input. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void doSaveAs() {
 		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
 		saveAsDialog.open();
 		IPath path = saveAsDialog.getResult();
 		if (path != null) {
 			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 			if (file != null) {
 				doSaveAs(URI.createPlatformResourceURI(file.getFullPath().toString(), true), new FileEditorInput(file));
 			}
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void doSaveAs(URI uri, IEditorInput editorInput) {
 		(editingDomain.getResourceSet().getResources().get(0)).setURI(uri);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		IProgressMonitor progressMonitor = new NullProgressMonitor();
 		// getActionBars().getStatusLineManager() != null ? getActionBars()
 		// .getStatusLineManager().getProgressMonitor() : new
 		// NullProgressMonitor();
 		doSave(progressMonitor);
 	}
 
 	/**
 	 * This is here for the listener to be able to call it. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void firePropertyChange(int action) {
 		super.firePropertyChange(action);
 	}
 
 	// /**
 	// * <!-- begin-user-doc --> <!-- end-user-doc -->
 	// * @generated
 	// */
 	// public EditingDomainActionBarContributor getActionBarContributor() {
 	// return (EditingDomainActionBarContributor)
 	// getEditorSite().getActionBarContributor();
 	// }
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	// public IActionBars getActionBars() {
 	// return getActionBarContributor().getActionBars();
 	// }
 	/**
 	 * This is how the framework determines which interfaces we implement. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Object getAdapter(Class key) {
 		if (key.equals(IContentOutlinePage.class)) {
 			return showOutlineView() ? getContentOutlinePage() : null;
 		}
 		// else if (key.equals(IPropertySheetPage.class)) {
 		// return getPropertySheetPage();
 		// }
 		// else if (key.equals(IGotoMarker.class)) {
 		// return this;
 		// }
 		else {
 			return super.getAdapter(key);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AdapterFactory getAdapterFactory() {
 		return adapterFactory;
 	}
 
 	/**
 	 * This accesses a cached version of the content outliner. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IContentOutlinePage getContentOutlinePage() {
 		if (contentOutlinePage == null) {
 			// The content outline is just a tree.
 			//
 			class MyContentOutlinePage extends ContentOutlinePage {
 				@Override
 				public void createControl(Composite parent) {
 					super.createControl(parent);
 					contentOutlineViewer = getTreeViewer();
 					contentOutlineViewer.addSelectionChangedListener(this);
 
 					// Set up the tree viewer.
 					//
 					contentOutlineViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
 					contentOutlineViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
 					contentOutlineViewer.setInput(parResource);
 
 					// Make sure our popups work.
 					//
 					createContextMenuFor(contentOutlineViewer);
 
 					if (!editingDomain.getResourceSet().getResources().isEmpty()) {
 						// Select the root object in the view.
 						//
 						contentOutlineViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet()
 								.getResources().get(0)), true);
 					}
 				}
 
 				@Override
 				public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager,
 						IStatusLineManager statusLineManager) {
 					super.makeContributions(menuManager, toolBarManager, statusLineManager);
 					contentOutlineStatusLineManager = statusLineManager;
 				}
 
 				@Override
 				public void setActionBars(IActionBars actionBars) {
 					// super.setActionBars(actionBars);
 					// getActionBarContributor().shareGlobalActions(this,
 					// actionBars);
 				}
 			}
 
 			contentOutlinePage = new MyContentOutlinePage();
 
 			// Listen to selection so that we can handle it is a special way.
 			//
 			contentOutlinePage.addSelectionChangedListener(new ISelectionChangedListener() {
 				// This ensures that we handle selections correctly.
 				//
 				public void selectionChanged(SelectionChangedEvent event) {
 					handleContentOutlineSelection(event.getSelection());
 				}
 			});
 		}
 
 		return contentOutlinePage;
 	}
 
 	/**
 	 * This returns the editing domain as required by the
 	 * {@link IEditingDomainProvider} interface. This is important for
 	 * implementing the static methods of {@link AdapterFactoryEditingDomain}
 	 * and for supporting {@link org.eclipse.emf.edit.ui.action.CommandAction}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AdapterFactoryEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	// /**
 	// * This accesses a cached version of the property sheet. <!--
 	// begin-user-doc
 	// * --> <!-- end-user-doc -->
 	// * @generated
 	// */
 	// public IPropertySheetPage getPropertySheetPage() {
 	// if (propertySheetPage == null) {
 	// propertySheetPage = new ExtendedPropertySheetPage(editingDomain) {
 	// @Override
 	// public void setActionBars(IActionBars actionBars) {
 	// super.setActionBars(actionBars);
 	// getActionBarContributor().shareGlobalActions(this, actionBars);
 	// }
 	//
 	// @Override
 	// public void setSelectionToViewer(List<?> selection) {
 	// ParEditor.this.setSelectionToViewer(selection);
 	// ParEditor.this.setFocus();
 	// }
 	// };
 	// propertySheetPage.setPropertySourceProvider(new
 	// AdapterFactoryContentProvider(adapterFactory));
 	// }
 	//
 	// return propertySheetPage;
 	// }
 
 	public Par getPar() {
 		return par;
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to
 	 * return this editor's overall selection. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	public ISelection getSelection() {
 		return editorSelection;
 	}
 
 	/**
 	 * This returns the viewer as required by the {@link IViewerProvider}
 	 * interface. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Viewer getViewer() {
 		return currentViewer;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void gotoMarker(IMarker marker) {
 		try {
 			if (marker.getType().equals(EValidator.MARKER)) {
 				String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
 				if (uriAttribute != null) {
 					URI uri = URI.createURI(uriAttribute);
 					EObject eObject = editingDomain.getResourceSet().getEObject(uri, true);
 					if (eObject != null) {
 						setSelectionToViewer(Collections.singleton(editingDomain.getWrapper(eObject)));
 					}
 				}
 			}
 		}
 		catch (CoreException exception) {
 			ServerIdeUiPlugin.getDefault().log(exception);
 		}
 	}
 
 	/**
 	 * Handles activation of the editor or it's associated views. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void handleActivate() {
 		// Recompute the read only state.
 		//
 		if (editingDomain.getResourceToReadOnlyMap() != null) {
 			editingDomain.getResourceToReadOnlyMap().clear();
 
 			// Refresh any actions that may become enabled or disabled.
 			//
 			setSelection(getSelection());
 		}
 
 		if (!removedResources.isEmpty()) {
 			if (handleDirtyConflict()) {
 				getSite().getPage().closeEditor(ParEditor.this, false);
 				ParEditor.this.dispose();
 			}
 			else {
 				removedResources.clear();
 				changedResources.clear();
 				savedResources.clear();
 			}
 		}
 		else if (!changedResources.isEmpty()) {
 			changedResources.removeAll(savedResources);
 			handleChangedResources();
 			changedResources.clear();
 			savedResources.clear();
 		}
 	}
 
 	/**
 	 * Handles what to do with changed resources on activation. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void handleChangedResources() {
 		if (!changedResources.isEmpty() && (!isDirty() || handleDirtyConflict())) {
 			editingDomain.getCommandStack().flush();
 
 			for (Resource resource : changedResources) {
 				if (resource.isLoaded()) {
 					resource.unload();
 					try {
 						resource.load(Collections.EMPTY_MAP);
 					}
 					catch (IOException exception) {
 						handleError(exception);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This deals with how we want selection in the outliner to affect the other
 	 * views. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void handleContentOutlineSelection(ISelection selection) {
 		if (currentViewerPane != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
 			Iterator<?> selectedElements = ((IStructuredSelection) selection).iterator();
 			if (selectedElements.hasNext()) {
 				// Get the first selected element.
 				//
 				// Object selectedElement = selectedElements.next();
 				// TODO select element in editor
 			}
 		}
 	}
 
 	/**
 	 * Shows a dialog that asks if conflicting changes should be discarded. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected boolean handleDirtyConflict() {
 		return MessageDialog.openQuestion(getSite().getShell(), "File changed detected",
 				"The file has changed on disk. Discard changes and reload?");
 	}
 
 	private void handleError(IOException exception) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * This is called during startup. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * @generated
 	 */
 	@Override
 	public void init(IEditorSite site, IEditorInput editorInput) {
 		setSite(site);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		site.setSelectionProvider(this);
 		site.getPage().addPartListener(partListener);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener,
 				IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/**
 	 * This sets up the editing domain for the model editor. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void initializeEditingDomain() {
 		// Create an adapter factory that yields item providers.
 		//
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new ParItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 
 		// Create the command stack that will notify this editor as commands are
 		// executed.
 		//
 		BasicCommandStack commandStack = new BasicCommandStack();
 
 		// Add a listener to set the most recent command's affected objects to
 		// be the selection of the viewer with focus.
 		//
 		commandStack.addCommandStackListener(new CommandStackListener() {
 			public void commandStackChanged(final EventObject event) {
 				getContainer().getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						firePropertyChange(IEditorPart.PROP_DIRTY);
 
 						// Try to select the affected objects.
 						//
 						Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
 						if (mostRecentCommand != null) {
 							setSelectionToViewer(mostRecentCommand.getAffectedObjects());
 						}
 					}
 				});
 			}
 		});
 
 		// Create the editing domain with a special command stack.
 		//
 		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
 	}
 
 	/**
 	 * This is for implementing {@link IEditorPart} and simply tests the command
 	 * stack. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean isDirty() {
 		return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 	}
 
 	/**
 	 * This returns whether something has been persisted to the URI of the
 	 * specified resource. The implementation uses the URI converter from the
 	 * editor's resource set to try to open an input stream. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected boolean isPersisted(Resource resource) {
 		boolean result = false;
 		try {
 			InputStream stream = editingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
 			if (stream != null) {
 				result = true;
 				stream.close();
 			}
 		}
 		catch (IOException e) {
 			// Ignore
 		}
 		return result;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	// /**
 	// * This implements {@link org.eclipse.jface.action.IMenuListener} to help
 	// * fill the context menus with contributions from the Edit menu. <!--
 	// * begin-user-doc --> <!-- end-user-doc -->
 	// * @generated
 	// */
 	// public void menuAboutToShow(IMenuManager menuManager) {
 	// ((IMenuListener)
 	// getEditorSite().getActionBarContributor()).menuAboutToShow(menuManager);
 	// }
 
 	/**
 	 * This is used to track the active viewer. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void pageChange(int pageIndex) {
 		super.pageChange(pageIndex);
 
 		if (contentOutlinePage != null) {
 			handleContentOutlineSelection(contentOutlinePage.getSelection());
 		}
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.remove(listener);
 	}
 
 	/**
 	 * This makes sure that one content viewer, either for the current page or
 	 * the outline view, if it has focus, is the current one. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCurrentViewer(Viewer viewer) {
 		// If it is changing...
 		//
 		if (currentViewer != viewer) {
 			if (selectionChangedListener == null) {
 				// Create the listener on demand.
 				//
 				selectionChangedListener = new ISelectionChangedListener() {
 					// This just notifies those things that are affected by
 					// the section.
 					//
 					public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
 						setSelection(selectionChangedEvent.getSelection());
 					}
 				};
 			}
 
 			// Stop listening to the old one.
 			//
 			if (currentViewer != null) {
 				currentViewer.removeSelectionChangedListener(selectionChangedListener);
 			}
 
 			// Start listening to the new one.
 			//
 			if (viewer != null) {
 				viewer.addSelectionChangedListener(selectionChangedListener);
 			}
 
 			// Remember it.
 			//
 			currentViewer = viewer;
 
 			// Set the editors selection based on the current viewer's
 			// selection.
 			//
 			setSelection(currentViewer == null ? StructuredSelection.EMPTY : currentViewer.getSelection());
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void setFocus() {
 		// TODO implement
 	}
 
 	/**
 	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to
 	 * set this editor's overall selection. Calling this result will notify the
 	 * listeners. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSelection(ISelection selection) {
 		editorSelection = selection;
 
 		for (ISelectionChangedListener listener : selectionChangedListeners) {
 			listener.selectionChanged(new SelectionChangedEvent(this, selection));
 		}
 		// setStatusLineManager(selection);
 	}
 
 	/**
 	 * This sets the selection into whichever viewer is active. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSelectionToViewer(Collection<?> collection) {
 		final Collection<?> theSelection = collection;
 		// Make sure it's okay.
 		//
 		if (theSelection != null && !theSelection.isEmpty()) {
 			// I don't know if this should be run this deferred
 			// because we might have to give the editor a chance to process the
 			// viewer update events
 			// and hence to update the views first.
 			//
 			//
 			Runnable runnable = new Runnable() {
 				public void run() {
 					// Try to select the items in the current content viewer
 					// of the editor.
 					//
 					if (currentViewer != null) {
 						currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
 					}
 				}
 			};
 			runnable.run();
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	// public void setStatusLineManager(ISelection selection) {
 	// IStatusLineManager statusLineManager = currentViewer != null &&
 	// currentViewer == contentOutlineViewer ? contentOutlineStatusLineManager :
 	// getActionBars().getStatusLineManager();
 	//
 	// if (statusLineManager != null) {
 	// if (selection instanceof IStructuredSelection) {
 	// Collection<?> collection = ((IStructuredSelection) selection).toList();
 	// switch (collection.size()) {
 	// case 0: {
 	// statusLineManager.setMessage(getString("_UI_NoObjectSelected"));
 	// break;
 	// }
 	// case 1: {
 	// String text = new
 	// AdapterFactoryItemDelegator(adapterFactory).getText(collection.iterator().
 	// next());
 	// statusLineManager.setMessage(getString("_UI_SingleObjectSelected",
 	// text));
 	// break;
 	// }
 	// default: {
 	// statusLineManager.setMessage(getString("_UI_MultiObjectSelected",
 	// Integer.toString(collection
 	// .size())));
 	// break;
 	// }
 	// }
 	// }
 	// else {
 	// statusLineManager.setMessage("");
 	// }
 	// }
 	// }
 	/**
 	 * Returns whether the outline view should be presented to the user. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected boolean showOutlineView() {
 		return true;
 	}
 
 	protected FormToolkit createToolkit(Display display) {
 		return new FormToolkit(ServerIdeUiPlugin.getDefault().getFormColors(display));
 	}
 
 }
