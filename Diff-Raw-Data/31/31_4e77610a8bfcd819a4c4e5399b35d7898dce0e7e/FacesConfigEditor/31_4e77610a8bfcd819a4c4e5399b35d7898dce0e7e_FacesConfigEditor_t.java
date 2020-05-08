 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackListener;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.EditorPartAction;
 import org.eclipse.gef.ui.actions.SaveAction;
 import org.eclipse.gef.ui.actions.UpdateAction;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jst.jsf.facesconfig.common.actions.IOpenPage;
 import org.eclipse.jst.jsf.facesconfig.edit.provider.FacesConfigItemProviderAdapterFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.ui.page.ComponentsPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.IntroductionPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.ManagedBeanPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.OthersPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.OverviewPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.DelegatingZoomManager;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.PageflowEditor;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.command.DelegatingCommandStack;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.command.EMFCommandStackGEFAdapter;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.layout.PageflowLayoutManager;
 import org.eclipse.jst.jsf.facesconfig.ui.util.WebrootUtil;
 import org.eclipse.jst.jsf.facesconfig.util.FacesConfigArtifactEdit;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.wst.common.ui.properties.internal.provisional.ITabbedPropertySheetPageContributor;
 import org.eclipse.wst.common.ui.properties.internal.provisional.TabbedPropertySheetPage;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 
 /**
  * 
  * @author sfshi
  * 
  */
 public class FacesConfigEditor extends FormEditor implements
 		IEditingDomainProvider, ISelectionProvider {
 
 	/**
 	 * editing domain that is used to track all changes to the model
 	 */
 	private AdapterFactoryEditingDomain editingDomain;
 
 	/**
 	 * adapter factory used for providing views of the model
 	 */
 	private ComposedAdapterFactory adapterFactory;
 
 	/** id of the pageflowPage */
 	private int pageflowPageID;
 
 	private int managedBeanPageID;
 
 	private int componentsPageID;
 
 	private int othersPageID;
 
 	private int sourcePageId;
 
 	private PageflowEditor pageflowPage;
 
 	/** The source text editor. */
 	private StructuredTextEditor sourcePage;
 
 	protected Collection selectionChangedListeners = new ArrayList();
 
 	private FacesConfigArtifactEdit facesConfigAtrifactEdit;
 
 	public static final String EDITOR_ID = "org.eclipse.jst.jsf.facesconfig.ui.FacesConfigEditor";
 
 	protected ISelection editorSelection = StructuredSelection.EMPTY;
 
 	private IContentOutlinePage outlinePage;
 
 	private IProject currentProject;
 
 	public FacesConfigEditor() {
 		initializeEMF();
 	}
 
 	/**
 	 * This listens for workspace changes. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
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
 						protected ResourceSet resourceSet = editingDomain
 								.getResourceSet();
 
 						protected Collection changedResources = new ArrayList();
 
 						protected Collection removedResources = new ArrayList();
 
 						public boolean visit(IResourceDelta delta) {
 							if (delta.getFlags() != IResourceDelta.MARKERS
 									&& delta.getResource().getType() == IResource.FILE) {
 								if ((delta.getKind() & (IResourceDelta.CHANGED | IResourceDelta.REMOVED)) != 0) {
 									Resource resource = resourceSet
 											.getResource(URI.createURI(delta
 													.getFullPath().toString()),
 													false);
 									if (resource != null) {
 										if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
 											removedResources.add(resource);
 										} else {
 											changedResources.add(resource);
 										}
 									}
 								}
 							}
 
 							return true;
 						}
 
 						public Collection getChangedResources() {
 							return changedResources;
 						}
 
 						public Collection getRemovedResources() {
 							return removedResources;
 						}
 					}
 
 					ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
 					delta.accept(visitor);
 
 					if (!visitor.getRemovedResources().isEmpty()) {
 						removedResources.addAll(visitor.getRemovedResources());
 						if (!isDirty()) {
 							getSite().getShell().getDisplay().asyncExec(
 									new Runnable() {
 										public void run() {
 											getSite().getPage().closeEditor(
 													FacesConfigEditor.this,
 													false);
 											FacesConfigEditor.this.dispose();
 										}
 									});
 						}
 					}
 
 					if (!visitor.getChangedResources().isEmpty()) {
 						changedResources.addAll(visitor.getChangedResources());
 					}
 				} catch (CoreException exception) {
 					// log it.
 					EditorPlugin.getDefault().getLog().log(
 							new Status(IStatus.ERROR, EditorPlugin
 									.getPluginId(), IStatus.OK, exception
 									.getMessage() == null ? "" : exception
 									.getMessage(), exception));
 				}
 			}
 		}
 	};
 
 	/**
 	 * Resources that have been removed since last activation.
 	 * 
 	 * @generated
 	 */
 	Collection removedResources = new ArrayList();
 
 	/**
 	 * Resources that have been changed since last activation.
 	 * 
 	 * @generated
 	 */
 	Collection changedResources = new ArrayList();
 
 	/**
 	 * Resources that have been saved.
 	 * 
 	 * @generated
 	 */
 	Collection savedResources = new ArrayList();
 
 	/**
 	 * Initializes the EMF support.
 	 */
 	private void initializeEMF() {
 		// create an adapter factory that yields item providers
 		List factories = new ArrayList();
 		factories.add(new ResourceItemProviderAdapterFactory());
 		factories.add(new FacesConfigItemProviderAdapterFactory());
 		factories.add(new ReflectiveItemProviderAdapterFactory());
 		adapterFactory = new ComposedAdapterFactory(factories);
 
 		// create the command stack that will notify this editor as commands are
 		// executed
 		BasicCommandStack commandStack = new BasicCommandStack();
 		commandStack
 				.addCommandStackListener(new org.eclipse.emf.common.command.CommandStackListener() {
 					public void commandStackChanged(final EventObject event) {
 						getContainer().getShell().getDisplay().asyncExec(
 								new Runnable() {
 									public void run() {
 										editorDirtyStateChanged();
										getActionBarContributor()
												.updateActionBars();
 									}
 								});
 					}
 				});
 		// commandStack.addCommandStackListener(this);
 		// create the editing domain with a special command stack
 		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
 				commandStack, new HashMap());
 	}
 
 	/*
 	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
 	 *      org.eclipse.ui.IEditorInput)
 	 */
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
		try {
			super.init(site, input);
		} catch (Exception e) {
			MessageDialog.openError(null,
					EditorMessages.FacesConfigEditor_Error_OpenModel_Title,
					EditorMessages.FacesConfigEditor_Error_OpenModel);
			throw new PartInitException(
					EditorMessages.FacesConfigEditor_Error_OpenModel);
		}

 		setPartName(input.getName());
 		if (!isValidInput(input)) {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().openEditor(input,
 							"org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
 
 			close(false);
 			return;
 		}
 
 		createActions();
 
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/*
 	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
 	 */
 	protected void setInput(IEditorInput input) {
 		super.setInput(input);
 
 		IFile inputFile = (IFile) input.getAdapter(IFile.class);
 		if (inputFile != null) {
 			IProject project = inputFile.getProject();
 			IPath inputPath = inputFile.getFullPath();
 			loadModel(project, inputPath);
 		}
 	}
 
 	/**
 	 * Loads the configuration model from the given path.
 	 * 
 	 */
 	private void loadModel(IProject project, IPath modelPath) {
 
 		IFolder webContentFolder = WebrootUtil.getWebContentFolder(project);
 		Assert.isTrue(webContentFolder != null && webContentFolder.exists());
 
 		IPath relativePath = modelPath;
 		if (webContentFolder.getFullPath().isPrefixOf(modelPath)) {
 			relativePath = modelPath.removeFirstSegments(webContentFolder
 					.getFullPath().segmentCount());
 		}
 		facesConfigAtrifactEdit = FacesConfigArtifactEdit
 				.getFacesConfigArtifactEditForWrite(project, relativePath
 						.toString());
 	}
 
 	protected void addPages() {
 		try {
 			IntroductionPage page1 = new IntroductionPage(this);
 			addPage(page1, null);
 
 			IFormPage overviewPage = new OverviewPage(this);
 			addPage(overviewPage, null);
 
 			// Page flow
 			createAndAddPageflowPage();
 
 			// pages
 			IFormPage managedBeanPage = new ManagedBeanPage(this);
 			managedBeanPageID = addPage(managedBeanPage, null);
 			IFormPage componentsPage = new ComponentsPage(this);
 			componentsPageID = addPage(componentsPage, null);
 			IFormPage othersPage = new OthersPage(this);
 			othersPageID = addPage(othersPage, null);
 
 			sourcePage = new StructuredTextEditor();
 
 			sourcePage.setEditorPart(this);
 
 			sourcePageId = addPage(sourcePage, this.getEditorInput());
 			setPageText(sourcePageId,
 					EditorMessages.FacesConfigEditor_Source_TabName);
 			sourcePage.update();
 
 		} catch (PartInitException e) {
 			EditorPlugin.getDefault().getLog().log(
 					new Status(IStatus.ERROR, EditorPlugin.getPluginId(),
 							IStatus.OK, e.getMessage() == null ? "" : e
 									.getMessage(), e));
 		}
 
 	}
 
 	/**
 	 * Creates the pageflow page of the multi-page editor.
 	 */
 	protected void createAndAddPageflowPage() throws PartInitException {
 		pageflowPage = new PageflowEditor(this);
 		pageflowPageID = addPage(pageflowPage, getEditorInput());
		setPageText(pageflowPageID,
				EditorMessages.FacesConfigEditor_Pageflow_TabName);
 		addPageActionRegistry(pageflowPage);
 		pageflowPage.getModelsTransform().setFacesConfig(getFacesConfig());
 		pageflowPage.getModelsTransform().setPageflow(
 				pageflowPage.getPageflow());
 		boolean fornew = pageflowPage.getModelsTransform()
 				.updatePageflowModelFromEMF();
 		pageflowPage.setGraphicalViewerContents(pageflowPage.getPageflow());
 		if (fornew) {
 			PageflowLayoutManager.getInstance().layoutPageflow(
 					pageflowPage.getPageflow());
 		}
 		pageflowPage.getModelsTransform().setListenToNotify(true);
 	}
 
 	public PageflowEditor getPageflowPage() {
 		return pageflowPage;
 	}
 
 	/**
 	 * get the action's registry of sub pages.
 	 * 
 	 */
 	protected void addPageActionRegistry(IEditorPart page) {
 		if (page != null) {
 			ActionRegistry pageActionRegisty = (ActionRegistry) page
 					.getAdapter(ActionRegistry.class);
 			if (pageActionRegisty != null) {
 				for (Iterator iter = pageActionRegisty.getActions(); iter
 						.hasNext();) {
 					getActionRegistry().registerAction((IAction) iter.next());
 				}
 			}
 		}
 	}
 
 	/** the editor's action registry */
 	private ActionRegistry actionRegistry = null;
 
 	/**
 	 * Returns the action registry of this editor.
 	 * 
 	 * @return - the action registry
 	 */
 	protected ActionRegistry getActionRegistry() {
 		if (null == actionRegistry) {
 			actionRegistry = new ActionRegistry();
 		}
 
 		return actionRegistry;
 	}
 
 	/**
 	 * Returns the root object of the configuration model.
 	 * 
 	 * @return the root object
 	 */
 	public FacesConfigType getFacesConfig() {
 		FacesConfigType facesConfig = facesConfigAtrifactEdit.getFacesConfig();
 		return facesConfig;
 	}
 
 	/*
 	 * @see org.eclipse.ui.ISaveablePart#isDirty()
 	 */
 	public boolean isDirty() {
 		return ((BasicCommandStack) editingDomain.getCommandStack())
 				.isSaveNeeded()
 				|| super.isDirty();
 	}
 
 	/**
 	 * This class listens for command stack changes of the pages contained in
 	 * this editor and decides if the editor is dirty or not.
 	 */
 	private class MultiPageCommandStackListener implements CommandStackListener {
 
 		/** the observed command stacks */
 		private List commandStacks = new ArrayList(2);
 
 		/** to get the editorpart from command stack */
 		private HashMap mapEditorCommandStack = new HashMap();
 
 		private boolean saveLocation = false;
 
 		/**
 		 * Adds a <code>CommandStack</code> to observe.
 		 * 
 		 * @param commandStack
 		 */
 		public void addCommandStack(CommandStack commandStack,
 				IEditorPart editor) {
 			if (commandStack == null) {
 				return;
 			}
 			if (mapEditorCommandStack.get(commandStack) == editor) {
 				return;
 			}
 			commandStacks.add(commandStack);
 			commandStack.addCommandStackListener(this);
 			mapEditorCommandStack.put(commandStack, editor);
 		}
 
 		/**
 		 * set the dirty status for the models of different editor
 		 * 
 		 * @param editor -
 		 *            editor, e.g., pageflow or databinding page.
 		 * @param dirty -
 		 *            true or false
 		 */
 		private void setEditorDirty(IEditorPart editor, boolean dirty) {
 
 		}
 
 		/** the list of action ids that are to CommandStack actions */
 		private List stackActionIDs = new ArrayList();
 
 		/**
 		 * Updates the specified actions.
 		 * 
 		 * @param actionIds -
 		 *            the list of ids of actions to update
 		 */
 		private void updateActions(List actionIds) {
 			for (Iterator ids = actionIds.iterator(); ids.hasNext();) {
 				IAction action = getActionRegistry().getAction(ids.next());
 				if (null != action && action instanceof UpdateAction) {
 					((UpdateAction) action).update();
 				}
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see CommandStackListener#commandStackChanged(java.util.EventObject)
 		 */
 		public void commandStackChanged(EventObject event) {
 			// enable or disable the actions
 			updateActions(stackActionIDs);
 			if (((CommandStack) event.getSource()).isDirty()) {
 				// set the editor's model dirty status
 				setEditorDirty((IEditorPart) mapEditorCommandStack
 						.get(((CommandStack) event.getSource())), true);
 				// at least one command stack is dirty,
 				// so the multi page editor is dirty too
 				setDirty(true);
 			} else {
 				// set the editor's model dirty status, if it is from not save
 				// location.
 				if (!saveLocation) {
 					setEditorDirty((IEditorPart) mapEditorCommandStack
 							.get(((CommandStack) event.getSource())), true);
 					setDirty(true);
 				} else {
 					setDirty(false);
 				}
 			}
 		}
 
 		/** the pageflow page editor's dirty state */
 		private boolean isDirty = false;
 
 		/**
 		 * Changes the dirty state.
 		 * 
 		 * @param dirty -
 		 *            dirty state
 		 */
 		public void setDirty(boolean dirty) {
 			if (isDirty != dirty) {
 				isDirty = dirty;
 				firePropertyChange(IEditorPart.PROP_DIRTY);
 			}
 		}
 
 		/**
 		 * Disposed the listener
 		 */
 		public void dispose() {
 			for (Iterator stacks = commandStacks.iterator(); stacks.hasNext();) {
 				((CommandStack) stacks.next()).removeCommandStackListener(this);
 			}
 			commandStacks.clear();
 		}
 
 		/**
 		 * Marks every observed command stack beeing saved. This method should
 		 * be called whenever the editor/model was saved.
 		 */
 		public void markSaveLocations() {
 			saveLocation = true;
 			for (Iterator stacks = commandStacks.iterator(); stacks.hasNext();) {
 				CommandStack stack = (CommandStack) stacks.next();
 				stack.markSaveLocation();
 			}
 			saveLocation = false;
 		}
 
 		/**
 		 * Flushes every observed command stack and resets the save location to
 		 * zero.
 		 */
 		public void flush() {
 			for (Iterator stacks = commandStacks.iterator(); stacks.hasNext();) {
 				CommandStack stack = (CommandStack) stacks.next();
 				stack.flush();
 			}
 		}
 	}
 
 	/** the <code>CommandStackListener</code> */
 	private MultiPageCommandStackListener multiPageCommandStackListener = null;
 
 	/**
 	 * Returns the global command stack listener.
 	 * 
 	 * @return the <code>CommandStackListener</code>
 	 */
 	protected MultiPageCommandStackListener getMultiPageCommandStackListener() {
 		if (null == multiPageCommandStackListener) {
 			multiPageCommandStackListener = new MultiPageCommandStackListener();
 		}
 		return multiPageCommandStackListener;
 	}
 
 	/*
 	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		// do the work within an operation because this is a long running
 		// activity that modifies the workbench
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			public void execute(IProgressMonitor monitor) {
 				try {
 					// modelResource.save(Collections.EMPTY_MAP);
 					facesConfigAtrifactEdit.getDeploymentDescriptorResource()
 							.save(Collections.EMPTY_MAP);
 					IFile file = ((IFileEditorInput) getEditorInput())
 							.getFile();
 					pageflowPage.doSave(file, monitor);
 					sourcePage.doSave(monitor);
 					getMultiPageCommandStackListener().markSaveLocations();
 				} catch (Exception e) {
 					EditorPlugin.getDefault().getLog().log(
 							new Status(IStatus.ERROR, EditorPlugin
 									.getPluginId(), IStatus.OK,
 									e.getMessage() == null ? "" : e
 											.getMessage(), e));
 				}
 			}
 		};
 		try {
 			// commit all pending changes in form pages
 			for (Iterator iter = pages.iterator(); iter.hasNext();) {
 				Object obj = iter.next();
 				if (obj instanceof FormPage) {
 					((FormPage) obj).doSave(monitor);
 				}
 				// else if (obj instanceof PageflowEditor) {
 				// ((PageflowEditor) obj).doSave(monitor);
 				// }
 
 			}
 			operation.run(null);// .run(true, false,
 			// operation;
 			// runs the operation, and shows progress
 			// new ProgressMonitorDialog();
 
 			// refresh the necessary state
 			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
 
 			editorDirtyStateChanged();
 		} catch (Exception e) {
 			EditorPlugin.getDefault().getLog().log(
 					new Status(IStatus.ERROR, EditorPlugin.getPluginId(),
 							IStatus.OK, e.getMessage(), e));
 		}
 	}
 
 	public void doSaveAs() {
 		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
 		saveAsDialog.open();
 		IPath path = saveAsDialog.getResult();
 		if (path != null) {
 			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 			if (file != null) {
 				doSaveAs(URI.createPlatformResourceURI(file.getFullPath()
 						.toString()), new FileEditorInput(file));
 			}
 		}
 	}
 
 	protected void doSaveAs(URI uri, IEditorInput editorInput) {
 		((Resource) editingDomain.getResourceSet().getResources().get(0))
 				.setURI(uri);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		IProgressMonitor progressMonitor = getActionBars()
 				.getStatusLineManager() != null ? getActionBars()
 				.getStatusLineManager().getProgressMonitor()
 				: new NullProgressMonitor();
 		doSave(progressMonitor);
 	}
 
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 
 	/**
 	 * Returns the <code>TabbedPropertySheetPage</code> for this editor.
 	 * 
 	 * @return - the <code>TabbedPropertySheetPage</code>
 	 */
 	protected IPropertySheetPage getPropertySheetPage() {
 		return new TabbedPropertySheetPage(
 				new ITabbedPropertySheetPageContributor() {
 
 					public String getContributorId() {
 						return EDITOR_ID;
 					}
 				});
 	}
 
 	/** the delegating ZoomManager */
 	private DelegatingZoomManager delegatingZoomManager = null;
 
 	/**
 	 * check whether the input is related with IFile.
 	 * 
 	 * @param input
 	 * @return
 	 */
 	private boolean isValidInput(IEditorInput input) {
 		if (input != null) {
 			IFile file = (IFile) input.getAdapter(IResource.class);
 			if (file != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the <code>DelegatingZoomManager</code> for this editor.
 	 * 
 	 * @return - the <code>DelegatingZoomManager</code>
 	 */
 	protected DelegatingZoomManager getDelegatingZoomManager() {
 		if (!isValidInput(getEditorInput())) {
 			return null;
 		}
 		if (null == delegatingZoomManager) {
 			delegatingZoomManager = new DelegatingZoomManager();
 			delegatingZoomManager
 					.setCurrentZoomManager((ZoomManager) pageflowPage
 							.getAdapter(ZoomManager.class));
 		}
 		return delegatingZoomManager;
 	}
 
 	/** the delegating CommandStack */
 	private DelegatingCommandStack delegatingCommandStack = null;
 
 	/**
 	 * Returns the <code>CommandStack</code> for this editor.
 	 * 
 	 * @return - the <code>CommandStack</code>
 	 */
 	public DelegatingCommandStack getDelegatingCommandStack() {
 		if (null == delegatingCommandStack) {
 			delegatingCommandStack = new DelegatingCommandStack();
 		}
 		return delegatingCommandStack;
 	}
 
 	/*
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (adapter == IEditingDomainProvider.class) {
 			return new IEditingDomainProvider() {
 				public EditingDomain getEditingDomain() {
 					return editingDomain;
 				}
 			};
 		}
 		if (adapter == EditingDomain.class) {
 			return editingDomain;
 		}
 		if (adapter == AdapterFactory.class) {
 			return adapterFactory;
 		}
 		if (adapter == IEditorPart.class) {
 			return getActiveEditor();
 		}
 
 		if (adapter == CommandStack.class) {
 			return getDelegatingCommandStack();
 		}
 		if (adapter == ZoomManager.class) {
 			return getDelegatingZoomManager();
 		}
 
 		if (adapter == ActionRegistry.class) {
 			return getActionRegistry();
 		}
 		if (adapter == IGotoMarker.class) {
 			return new IGotoMarker() {
 				public void gotoMarker(IMarker marker) {
 					FacesConfigEditor.this.gotoMarker(marker);
 				}
 			};
 		}
 		if (adapter == StructuredTextEditor.class) {
 			return sourcePage;
 		}
 
 		if (adapter == IContentOutlinePage.class) {
 			return getOutlinePage();
 		}
 
 		if (adapter == IPropertySheetPage.class) {
 			return getPropertySheetPage();
 		}
 
 		if (adapter == IProject.class) {
 			return getProject();
 		}
 
 		if (adapter == CTabFolder.class) {
 			return getContainer();
 		}
 
 		if (adapter == IOpenPage.class) {
 			return new IOpenPage() {
 
 				public void setActiveEditorPage(String pageID) {
 					FacesConfigEditor.this.setActiveEditorPage(pageID);
 
 				}
 			};
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 	private EMFCommandStackGEFAdapter sourceCommandStack;
 
 	/**
 	 * get or create the source page's GEF command stack based on its EMF
 	 * command stack.
 	 * 
 	 * @return
 	 */
 	private CommandStack getSourcePageCommandStack() {
 		if (sourceCommandStack == null) {
 			sourceCommandStack = new EMFCommandStackGEFAdapter(
 					(BasicCommandStack) sourcePage.getModel().getUndoManager()
 							.getCommandStack());
 		}
 		return sourceCommandStack;
 	}
 
 	/** the list of action ids that are to CommandStack actions */
 	// private List stackActionIDs = new ArrayList();
 	/** the list of action ids that are editor actions */
 	private List editorActionIDs = new ArrayList();
 
 	/**
 	 * Adds an editor action to this editor.
 	 * <p>
 	 * Editor actions are actions that depend and work on the editor.
 	 * 
 	 * @param action -
 	 *            the editor action
 	 */
 	protected void addEditorAction(EditorPartAction action) {
 		getActionRegistry().registerAction(action);
 		editorActionIDs.add(action.getId());
 	}
 
 	/**
 	 * Creates different kinds of actions and registers them to the
 	 * ActionRegistry.
 	 */
 	protected void createActions() {
 		// register save action
 		addEditorAction(new SaveAction(this));
 	}
 
 	/**
 	 * Indicates that the current page has changed.
 	 * <p>
 	 * We update the DelegatingCommandStack, OutlineViewer and other things
 	 * here. //
 	 */
 	protected void currentPageChanged() {
 		IEditorPart activeEditor = getActiveEditor();
 		if (activeEditor == null) {
 			return;
 		}
 
 		// update command stack
 		CommandStack cmdStack = null;
 
 		if (activeEditor == pageflowPage) {
 			cmdStack = (CommandStack) activeEditor
 					.getAdapter(CommandStack.class);
 		} else if (activeEditor == sourcePage)// other page will delegate the
 		// GEF command stack to source
 		// page's.
 		{
 			cmdStack = this.getSourcePageCommandStack();
 		}
 
 		// Add command stacks
 		getMultiPageCommandStackListener().addCommandStack(cmdStack,
 				activeEditor);
 		getDelegatingCommandStack().setCurrentCommandStack(cmdStack);
 
 		// enable or disable the actions
 		// updateActions(stackActionIDs);
 
 		// update zoom actions
 		ZoomManager zoomManager = null;
 		zoomManager = (ZoomManager) activeEditor.getAdapter(ZoomManager.class);
 
 		if (zoomManager != null) {
 			getDelegatingZoomManager().setCurrentZoomManager(zoomManager);
 		}
 
 		IEditorActionBarContributor contributor = getEditorSite()
 				.getActionBarContributor();
 		if (contributor != null
 				&& contributor instanceof FacesConfigActionBarContributor) {
 			((FacesConfigActionBarContributor) contributor)
 					.setActivePage(activeEditor);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see MultiPageEditorPart#pageChange(int)
 	 */
 	protected void pageChange(int newPageIndex) {
 		super.pageChange(newPageIndex);
 		// getActionBarContributor().setActivePage(getActiveEditor());
 		// refresh content depending on current page
 		currentPageChanged();
 	}
 
 	public void dispose() {
 		if (facesConfigAtrifactEdit != null)
 			facesConfigAtrifactEdit.dispose();
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
 				resourceChangeListener);
 
 		adapterFactory.dispose();
 
 		if (this.outlinePage != null) {
 			outlinePage.dispose();
 		}
 
 		super.dispose();
 	}
 
 	/**
 	 * get the project of the faces config file that the editor is working on.
 	 * 
 	 * @return
 	 */
 	public IProject getProject() {
 		if (currentProject == null) {
 			if (facesConfigAtrifactEdit != null) {
 				IFile file = facesConfigAtrifactEdit.getFile();
 				if (file != null)
 					currentProject = file.getProject();
 			}
 		}
 		return currentProject;
 	}
 
 	public EditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * Returns the <code>IContentOutlinePage</code> for this editor.
 	 * 
 	 * @return - the <code>IContentOutlinePage</code>
 	 */
 	protected IContentOutlinePage getOutlinePage() {
 		if (null == outlinePage) {
 			outlinePage = new MultiPageEditorOutlinePage();
 		}
 		return outlinePage;
 	}
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.add(listener);
 
 	}
 
 	public ISelection getSelection() {
 		return editorSelection;
 	}
 
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		selectionChangedListeners.remove(listener);
 	}
 
 	public void setSelection(ISelection selection) {
 		editorSelection = selection;
 		for (Iterator listeners = selectionChangedListeners.iterator(); listeners
 				.hasNext();) {
 			ISelectionChangedListener listener = (ISelectionChangedListener) listeners
 					.next();
 			listener
 					.selectionChanged(new SelectionChangedEvent(this, selection));
 		}
 	}
 
 	public void gotoMarker(IMarker marker) {
 		setActivePage(sourcePageId);
 		IDE.gotoMarker(this.sourcePage, marker);
 	}
 
 	public FacesConfigActionBarContributor getActionBarContributor() {
 		return (FacesConfigActionBarContributor) getEditorSite()
 				.getActionBarContributor();
 	}
 
 	public IActionBars getActionBars() {
 		return getActionBarContributor().getActionBars();
 	}
 
 	/**
 	 * Shows a dialog that asks if conflicting changes should be discarded.
 	 * 
 	 * @generated
 	 */
 	protected boolean handleDirtyConflict() {
 		return MessageDialog
 				.openQuestion(
 						getSite().getShell(),
 						"File Conflict",
 						" There are unsaved changes that conflict with changes made outside the editor.  Do you wish to discard this editor's changes?");
 	}
 
 	/**
 	 * Handles what to do with changed resources on activation.
 	 * 
 	 * @generated
 	 */
 	protected void handleChangedResources() {
 		if (!changedResources.isEmpty()
 				&& (!isDirty() || handleDirtyConflict())) {
 			editingDomain.getCommandStack().flush();
 
 			for (Iterator i = changedResources.iterator(); i.hasNext();) {
 				Resource resource = (Resource) i.next();
 				if (resource.isLoaded()) {
 					resource.unload();
 					try {
 						resource.load(Collections.EMPTY_MAP);
 					} catch (IOException exception) {
 						EditorPlugin.getDefault().getLog().log(
 								new Status(IStatus.ERROR, EditorPlugin
 										.getPluginId(), IStatus.OK, exception
 										.getMessage() == null ? "" : exception
 										.getMessage(), exception));
 					}
 				}
 			}
 		}
 	}
 
 	public void setActiveEditorPage(String pageID) {
 		if (pageID.equals(PageflowEditor.PAGE_ID)) {
 			setActivePage(pageflowPageID);
 		} else if (pageID.equals(ManagedBeanPage.PAGE_ID)) {
 			setActivePage(managedBeanPageID);
 		} else if (pageID.equals(ComponentsPage.PAGE_ID)) {
 			setActivePage(componentsPageID);
 		} else if (pageID.equals(OthersPage.PAGE_ID)) {
 			setActivePage(othersPageID);
 		}
 	}
 }
