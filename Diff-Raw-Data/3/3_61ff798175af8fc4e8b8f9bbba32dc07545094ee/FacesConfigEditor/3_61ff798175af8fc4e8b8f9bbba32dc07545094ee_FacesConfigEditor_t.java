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
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
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
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jst.jsf.common.ui.internal.actions.IOpenPage;
 import org.eclipse.jst.jsf.core.IJSFCoreConstants;
 import org.eclipse.jst.jsf.facesconfig.edit.provider.FacesConfigItemProviderAdapterFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.ui.page.ComponentsPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.IntroductionPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.ManagedBeanPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.OthersPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.OverviewPage;
 import org.eclipse.jst.jsf.facesconfig.ui.page.WaitForLoadPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.DelegatingZoomManager;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.PageflowEditor;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.command.DelegatingCommandStack;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.command.EMFCommandStackGEFAdapter;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.layout.PageflowLayoutManager;
 import org.eclipse.jst.jsf.facesconfig.ui.preference.GEMPreferences;
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
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.common.ui.properties.internal.provisional.ITabbedPropertySheetPageContributor;
 import org.eclipse.wst.common.ui.properties.internal.provisional.TabbedPropertySheetPage;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 
 /**
  * This is the main editor for the faces-config file.  Note that the model
  * load can involve long-running socket operations (shouldn't but can),
  * so the editor UI is load asynchronously.  This is means that any 
  * operations that need to be executed on editor open should be run
  * using AddPagesTask.pageSafeExecute() to ensure that they occur
  * after all editor pages have finished loading.
  * 
  * @author sfshi
  * 
  */
 public class FacesConfigEditor extends FormEditor implements
 		IEditingDomainProvider, ISelectionProvider {
 
     /**
      * This editor's ID.  TODO: this should prob be in plugin.properties?
      */
     public static final String EDITOR_ID = "org.eclipse.jst.jsf.facesconfig.ui.FacesConfigEditor"; //$NON-NLS-1$
 
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
 
 	private Collection selectionChangedListeners = new ArrayList();
 
 	private ISelection editorSelection = StructuredSelection.EMPTY;
 
 	private IContentOutlinePage outlinePage;
 
 	private IProject currentProject;
 
 	private boolean isWebProject;
 	
 	private ModelLoader        _modelLoader;
 	
 	/**
 	 * only true once dispose() has been called
 	 * used to signal that the editor was disposed.
 	 */
 	private boolean _isDisposed; // = false;
 	
     /**
      * Used to load editor pages when the model is loaded
      */
     private final AddPagesTask     _addPagesTask = new AddPagesTask();
         
 	/**
 	 * Default constructor
 	 */
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
 
 						@SuppressWarnings("hiding") //$NON-NLS-1$
                         protected Collection changedResources = new ArrayList();
 
 						@SuppressWarnings("hiding") //$NON-NLS-1$
                         protected Collection removedResources = new ArrayList();
 
 						public boolean visit(IResourceDelta delta_) {
 							if (delta_.getFlags() != IResourceDelta.MARKERS
 									&& delta_.getResource().getType() == IResource.FILE) {
 								if ((delta_.getKind() & (IResourceDelta.CHANGED | IResourceDelta.REMOVED)) != 0) {
 									Resource resource = resourceSet
 											.getResource(URI.createURI(delta_
 													.getFullPath().toString()),
 													false);
 									if (resource != null) {
 										if ((delta_.getKind() & IResourceDelta.REMOVED) != 0) {
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
 									.getMessage() == null ? "" : exception //$NON-NLS-1$
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
 	protected void setInput(IEditorInput input) 
 	{
         isWebProject = matches(input);
         super.setInput(input);
 
 		IFile inputFile = (IFile) input.getAdapter(IFile.class);
 		if (inputFile != null) 
 		{
 			final IProject project = inputFile.getProject();
 			final IPath inputPath = inputFile.getFullPath();
 			
 			_modelLoader = new ModelLoader(); 
 			_modelLoader.load(project, inputPath, isWebProject, _addPagesTask);
 		}
 	}
 
 
 	protected void addPages() 
 	{
 	    // try loading wait page
 	    // if we get to here before model load completes,
 	    // then wait page will give the user the indication
 	    // that something is happening in the background before
 	    // the editor full loads.
 	    // if the model is already loaded, this call should do nothing
 	    _addPagesTask.maybeAddWaitPage();
 	}
 
 	/**
 	 * This runnable is used to used to manage the loading of the 
 	 * editor pages for editor in a deferred fashion.  Because the model
 	 * loading for this editor can be noticably long and (unfortunately)
 	 * may involve socket calls that block, loadModel(), runs this on a
 	 * separate thread. This class is intended to be used in two ways:
 	 * 
 	 * 1) by the model loading code to signal it is finished by executing
 	 * the run() via a display.asyncExec().
 	 * 
 	 * 2) by the addPages() call back on the the main editor as a way to
 	 * load a "Please wait for loading" page if the loading is still running
 	 * by the time the editor is ready to visualize itself.
 	 * 
 	 * Note that in both cases methods of this class *must* be running on the
 	 * main display thread.
 	 * 
 	 * @author cbateman
 	 *
 	 */
 	private class AddPagesTask extends ModelLoader.ModelLoaderComplete
 	{
 	    private final AtomicBoolean    _arePagesLoaded = new AtomicBoolean(false);     // set to true when the regular editor pages are loaded
 	    private FormPage               _waitPage;
 	    private List<Runnable>         _deferredRunnables = new ArrayList<Runnable>();
 	    
 	    /**
 	     * If the editor pages are loaded, runnable.run() is invoked immediately
 	     * If the editor pages are not loaded yet, runnable is queued and will be 
 	     * executed in the order they are added immediately after the pages are loaded
 	     * 
 	     * @param runnable
 	     */
 	    public synchronized void pageSafeExecute(Runnable runnable)
 	    {
 	        if (!_isDisposed)
 	        {
     	        if (!_arePagesLoaded.get())
     	        {
     	            _deferredRunnables.add(runnable);
     	        }
     	        else
     	        {
     	            runnable.run();
     	        }
 	        }
 	    }
 	    
         /**
          * @return true if the pages are loaded
          */
         public synchronized boolean getArePagesLoaded() 
         {
             return _arePagesLoaded.get();
         }
         
         /**
          * Remove the wait page if present.
          */
         public synchronized void removeWaitPage()
         {
             if (_waitPage != null)
             {
                 int index = _waitPage.getIndex();
                 
                 if (index >= 0)
                 {
                     removePage(index);
                 }
             }
         }
         
         /**
          * Add the wait page if the main pages aren't already loaded
          */
         public synchronized void maybeAddWaitPage()
         {
             // only load the wait page if the other pages haven't been loaded
             if (!getArePagesLoaded())
             {
                 _waitPage = new WaitForLoadPage(FacesConfigEditor.this, "WaitForLoad", EditorMessages.FacesConfigEditor_WaitForLoad_EditorTabTitle); //$NON-NLS-1$
                 
                 try
                 {
                     addPage(0,_waitPage);
                 }
                 catch(PartInitException pie)
                 {
                     _waitPage =null;
                     EditorPlugin.getDefault().getLog().log(
                             new Status(IStatus.ERROR, EditorPlugin.getPluginId(),
                                     IStatus.OK, pie.getMessage() == null ? "" : pie //$NON-NLS-1$
                                             .getMessage(), pie));
                 }
             }
         }
 
         /**
          * Must be run on the UI thread
          */
         public void doRun(FacesConfigArtifactEdit  edit) 
         {
             synchronized(this)
             {
                 // ensure wait page gets removed
                 removeWaitPage();
                 
                 if (!getArePagesLoaded()
                         && !_isDisposed)  // NOTE: we assume that access to variable does not need to
                                           // to be synchronous since this method must 
                                           // be run on the UI thread.  The only way
                                           // that isDisposed should be true is if model loading took a long
                                           // time and the user closed the editor before it completed (trigger dispose to be called)
                 {
                     try 
                     {
                        if (isWebProject && edit != null && edit.getFacesConfig() != null) 
                         {
                             // only add the intro editor if the preference
                             // is set to do so.
                             if (GEMPreferences.getShowIntroEditor())
                             {
                                 IntroductionPage page1 = new IntroductionPage(FacesConfigEditor.this);
                                 addPage(page1, null);
                             }
                             
                             IFormPage overviewPage = new OverviewPage(FacesConfigEditor.this);
                             addPage(overviewPage, null);
         
                             // Page flow
                             createAndAddPageflowPage();
         
                             // pages
                             IFormPage managedBeanPage = new ManagedBeanPage(FacesConfigEditor.this);
                             managedBeanPageID = addPage(managedBeanPage, null);
                             IFormPage componentsPage = new ComponentsPage(FacesConfigEditor.this);
                             componentsPageID = addPage(componentsPage, null);
                             IFormPage othersPage = new OthersPage(FacesConfigEditor.this);
                             othersPageID = addPage(othersPage, null);
                         }
         
                         sourcePage = new StructuredTextEditor();
         
                         sourcePage.setEditorPart(FacesConfigEditor.this);
         
                         sourcePageId = addPage(sourcePage, FacesConfigEditor.this.getEditorInput());
                         setPageText(sourcePageId,
                                 EditorMessages.FacesConfigEditor_Source_TabName);
                         sourcePage.update();
                         
                         // default active page to 0
                         setActivePage(0);
 
                         // execute deferred runnables
                         for (Runnable runnable : _deferredRunnables)
                         {
                             runnable.run();
                         }
                         
                         // flag the fact that the regular editor pages have been added
                         _arePagesLoaded.set(true);
                     } catch (PartInitException e) {
                         EditorPlugin.getDefault().getLog().log(
                                 new Status(IStatus.ERROR, EditorPlugin.getPluginId(),
                                         IStatus.OK, e.getMessage() == null ? "" : e //$NON-NLS-1$
                                                 .getMessage(), e));
                     }
                 }
             }
         }
 	}
 	
 	/**
 	 * Creates the pageflow page of the multi-page editor.
 	 * @throws PartInitException 
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
 
 	/**
 	 * TODO: this is used only for testing
 	 * @return the page flow editor
 	 */
 	public PageflowEditor getPageflowPage() {
 		return pageflowPage;
 	}
 
 	/**
 	 * get the action's registry of sub pages.
 	 * @param page 
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
 		if (null == actionRegistry)
 			actionRegistry = new ActionRegistry();
 
 		return actionRegistry;
 	}
 
 	/**
 	 * Returns the root object of the configuration model.
 	 * 
 	 * @return the root object.  Should not, but may return null.
 	 */
 	public FacesConfigType getFacesConfig() 
 	{
 	    FacesConfigArtifactEdit  edit = _modelLoader.getEdit();
 	    if (edit != null)
 	    {
 	        return edit.getFacesConfig();
 	    }
 	    return null;
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
 		 * @param editor 
 		 */
 		public void addCommandStack(CommandStack commandStack,
 				IEditorPart editor) {
 			if (commandStack == null)
 				return;
 
 			if (mapEditorCommandStack.get(commandStack) == editor)
 				return;
 
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
             // do nothing
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
 						.get(event.getSource()), true);
 				// at least one command stack is dirty,
 				// so the multi page editor is dirty too
 				setDirty(true);
 			} else {
 				// set the editor's model dirty status, if it is from not save
 				// location.
 				if (!saveLocation) {
 					setEditorDirty((IEditorPart) mapEditorCommandStack
 							.get(event.getSource()), true);
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
 		if (null == multiPageCommandStackListener)
 			multiPageCommandStackListener = new MultiPageCommandStackListener();
 
 		return multiPageCommandStackListener;
 	}
 
 	/*
 	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		// do the work within an operation because this is a long running
 		// activity that modifies the workbench
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			public void execute(IProgressMonitor monitor_) {
 				try {
 					if (isWebProject &&
 					        _modelLoader.getEdit() != null) {
 						// modelResource.save(Collections.EMPTY_MAP);
 						_modelLoader.getEdit()
 								.getDeploymentDescriptorResource().save(
 										Collections.EMPTY_MAP);
 						IFile file = ((IFileEditorInput) getEditorInput())
 								.getFile();
 						pageflowPage.doSave(file, monitor_);
 					}
 					sourcePage.doSave(monitor_);
 					getMultiPageCommandStackListener().markSaveLocations();
 				} catch (Exception e) {
 					EditorPlugin.getDefault().getLog().log(
 							new Status(IStatus.ERROR, EditorPlugin
 									.getPluginId(), IStatus.OK,
 									e.getMessage() == null ? "" : e //$NON-NLS-1$
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
 
 	/**
 	 * @param uri
 	 * @param editorInput
 	 */
 	protected void doSaveAs(URI uri, IEditorInput editorInput) {
 		editingDomain.getResourceSet().getResources().get(0)
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
 		if (!isValidInput(getEditorInput()) || !isWebProject || !_addPagesTask.getArePagesLoaded()) {
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
 				public void gotoMarker(final IMarker marker) {
 				    // this may be called on an editor open (i.e. double-click the Problems view)
 				    // so ensure it runs safely with respect to the page load
 				    _addPagesTask.pageSafeExecute(new Runnable()
 				    {
 				        public void run()
 				        {
 		                    FacesConfigEditor.this.gotoMarker(marker);
 				        }
 				    });
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
             IDocument doc = sourcePage.getDocumentProvider().getDocument(getEditorInput());
             if (doc instanceof IStructuredDocument) {
                 IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForEdit(doc);
                 if (model == null) {
                     model = StructuredModelManager.getModelManager().getModelForEdit((IStructuredDocument) doc);
                 }
                 sourceCommandStack = new EMFCommandStackGEFAdapter(
                         (BasicCommandStack) model.getUndoManager()
                                 .getCommandStack());
             }
             else
             {
                 EditorPlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR, EditorPlugin.getPluginId(), 0, 
                            "Error getting undo stack for Faces Config editor.  Undo may be disabled", //$NON-NLS-1$
                            new Throwable()));
             }
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
 
 	public void dispose() 
 	{
         // signal that we have been disposed
         // do this before anything else
 	    _isDisposed = true;
 	    _modelLoader.dispose();
 	    
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
 	 * @return IProject
 	 */
 	public IProject getProject() {
 		if (currentProject == null) {
 			if (_modelLoader.getEdit() != null) {
 				IFile file = _modelLoader.getEdit().getFile();
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
 
 	private void gotoMarker(IMarker marker) {
 		setActivePage(sourcePageId);
 		IDE.gotoMarker(this.sourcePage, marker);
 	}
 
 	/**
 	 * FIXME: this is used only for testing. Should isolate better
 	 * @return the action bar
 	 */
 	public FacesConfigActionBarContributor getActionBarContributor() {
 		return (FacesConfigActionBarContributor) getEditorSite()
 				.getActionBarContributor();
 	}
 
 	private IActionBars getActionBars() {
 		return getActionBarContributor().getActionBars();
 	}
 
 	/**
 	 * Shows a dialog that asks if conflicting changes should be discarded.
 	 * @return the user's response.
 	 */
 	protected boolean handleDirtyConflict() {
 		return MessageDialog
 				.openQuestion(
 						getSite().getShell(),
 						EditorMessages.FacesConfigEditor_ErrorHandlingUndoConflicts_DialogTitle,
 						EditorMessages.FacesConfigEditor_ErrorHandlingUndoConflicts_DialogMessage);
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
 										.getMessage() == null ? "" : exception //$NON-NLS-1$
 										.getMessage(), exception));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * TODO this is used only for testing.  Should be able to remove if we
 	 * go to true automated UI testing
 	 * @param pageID
 	 */
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
 
 	private boolean matches(IEditorInput input) {
 		final IResource file = (IResource) input.getAdapter(IResource.class);
 		boolean hasWebFacet = false;
 		boolean hasJSFFacet = false;
 
 		if (file != null) {
 			final IProject project = file.getProject();
 
 			if (project != null) {
 				try {
 					final IFacetedProject facetedProject = ProjectFacetsManager
 							.create(project);
 
 					if (facetedProject != null) {
 						final Set facets = facetedProject.getProjectFacets();
 
 						for (final Iterator it = facets.iterator(); it
 								.hasNext();) {
 							final IProjectFacetVersion version = (IProjectFacetVersion) it
 									.next();
 
 							IProjectFacet facet = version.getProjectFacet();
 							if (IJSFCoreConstants.JSF_CORE_FACET_ID.equals(facet.getId())) {
 								hasJSFFacet = true;
 							} else if ("jst.web".equals(facet.getId())) { //$NON-NLS-1$
 								hasWebFacet = true;
 							}
 						}
 					}
 				} catch (CoreException ex) {
 					EditorPlugin.getDefault().getLog().log(
 							new Status(IStatus.ERROR, EditorPlugin
 									.getPluginId(), IStatus.OK,
 									ex.getMessage() == null ? "" : ex //$NON-NLS-1$
 											.getMessage(), ex));
 				}
 			}
 		}
 
 		return hasWebFacet && hasJSFFacet;
 	}
 	
     /**
      * DANGER!  This call is for testing only!  Should not be used,
      * even internally, by production code.
      * @param timeoutMs the time to wait in milliseconds
      * @throws InterruptedException 
      */	
 	public void doPageLoad(long timeoutMs) throws InterruptedException
 	{
 	    _modelLoader.waitForLoad(timeoutMs);
 	    _addPagesTask.doRun(_modelLoader.getEdit());
 	}
 }
