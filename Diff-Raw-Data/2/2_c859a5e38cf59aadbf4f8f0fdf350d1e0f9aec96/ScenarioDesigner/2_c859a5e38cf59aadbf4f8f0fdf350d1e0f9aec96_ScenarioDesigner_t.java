 /*
  * Copyright 2007 Pi4 Technologies Ltd
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * Change History:
  * Feb 14, 2007 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.editor;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 //import org.eclipse.emf.common.util.URI;
 //import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.gef.KeyHandler;
 import org.eclipse.gef.KeyStroke;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.GEFPlugin;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackListener;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.gef.editparts.ScalableRootEditPart;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.DeleteAction;
 import org.eclipse.gef.ui.actions.EditorPartAction;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.actions.RedoAction;
 import org.eclipse.gef.ui.actions.SelectionAction;
 import org.eclipse.gef.ui.actions.StackAction;
 import org.eclipse.gef.ui.actions.UndoAction;
 import org.eclipse.gef.ui.actions.UpdateAction;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 //import org.pi4soa.designer.util.ModelSupport;
 import org.eclipse.gef.ui.actions.ZoomInAction;
 import org.eclipse.gef.ui.actions.ZoomOutAction;
 import org.eclipse.gef.ui.actions.PrintAction;
 //import org.savara.tools.scenario.designer.editor.*;
 import org.savara.scenario.util.ScenarioModelUtil;
 import org.savara.tools.scenario.designer.editor.properties.DesignerTabbedPropertySheetPage;
 //import org.savara.tools.scenario.ScenarioManager;
 import org.savara.tools.scenario.designer.DesignerDefinitions;
 
 import org.eclipse.draw2d.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.events.*;
 import org.eclipse.ui.actions.*;
 import org.eclipse.ui.*;
 import org.eclipse.ui.part.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.gef.*;
 import org.eclipse.ui.views.contentoutline.*;
 
 /**
  * This class provides the implementation for the choreography
  * description editor within the pi4soa designer tool.
  */
 public class ScenarioDesigner
 					extends MultiPageEditorPart implements IAdaptable,
 					IGotoMarker, Editor,
 					ITabbedPropertySheetPageContributor {
 
 	public ScenarioDesigner() {
 	}
 	
 	public String getContributorId() {
         return getSite().getId();
     }
 
 	/**
      * This method creates the pages for the 
      * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
      */
     protected void createPages() {
         try {
             m_scenarioEditorPageID = addPage(new ScenarioEditorPage(this), getEditorInput());
             setPageText(m_scenarioEditorPageID,
             		getScenarioEditorPage().getPageName());
 
 			// Comment out while not displaying simulation log
             /*
             m_simulationLogPageID = addPage(new SimulationLogPage(this), getEditorInput());
             setPageText(m_simulationLogPageID,
         			getSimulationLogPage().getPageName());
 			*/
             
             // add command stacks
             getMultiPageCommandStackListener().addCommandStack(
             		getScenarioEditorPage().getCommandStack2());
 
             // activate delegating command stack
             getDelegatingCommandStack().setCurrentCommandStack(
             		getScenarioEditorPage().getCommandStack2());
 
             // set active page
             setActivePage(m_scenarioEditorPageID);
             
         } catch (PartInitException e) {
             ErrorDialog.openError(
                 getSite().getShell(),
                 "Open Error",
                 "An error occured during opening the editor.",
                 e.getStatus());
         }
     }
 
     /**
      * Method is notified when the active page is changed.
      * 
      */
     protected void pageChange(int index) {
     	super.pageChange(index);
     	
     	updateAfterPageChange(index);
     }
     
     /**
      * This method performs the updates following a page change.
      *
      */
     protected void updateAfterPageChange(int newPage) {
     	
     	if (getCurrentPage() != null) {
 	        // Update zoom actions
 	        getDelegatingZoomManager().setCurrentZoomManager(
 	            getZoomManager(getCurrentPage().getViewer()));
 	
 	        // Update delegating command stack
 	        getDelegatingCommandStack().setCurrentCommandStack(
 	            getCurrentPage().getCommandStack2());
     	} else {
     		getDelegatingZoomManager().setCurrentZoomManager(null);
     	}
     }
     
     /**
      * This method returns the Scenario.
      * 
      * @return The scenario
      */
     public org.savara.scenario.model.Scenario getScenario() {
     	return(m_scenario);
     }
     
     /**
      * This method returns the scenario view used for simulating
      * the scenario.
      * 
      * @return The scenario view
      */
     public org.savara.tools.scenario.designer.simulate.ScenarioSimulation getScenarioSimulation() {
     	return((org.savara.tools.scenario.designer.simulate.ScenarioSimulation)getScenarioEditorPage());
     }
    
     /**
      * This method return the primary description being
      * presented by the editor.
      * 
      * @return The description
      */
     public Object getDescription() {
     	return(getScenario());
     }
     
     /**
      * This method returns the simulation log page.
      * 
      * @return The simulation log page
      */
 	// Comment out while not displaying simulation log
     /*
     protected SimulationLogPage getSimulationLogPage() {
     	if (m_simulationLogPageID == -1) {
     		return(null);
     	}
         return((SimulationLogPage)
 				getEditor(m_simulationLogPageID));
     }
     
     /**
      * This method returns the scenario editor page.
      * 
      * @return The scenario editor page
      */
     protected ScenarioEditorPage getScenarioEditorPage() {
     	if (m_scenarioEditorPageID == -1) {
     		return(null);
     	}
         return((ScenarioEditorPage)
 				getEditor(m_scenarioEditorPageID));
     }
     
     /**
      * Returns the current active page. 
      * @return the current active page or <code>null</code>
      */
     private ScenarioEditorPage getCurrentPage() {
         if (getActivePage() == -1) {
             return null;
         }
         
         Object obj=getEditor(getActivePage());
         
         if (obj instanceof ScenarioEditorPage) {
         	return((ScenarioEditorPage)obj);
         }
 
         return(null);
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
      */
     public void init(IEditorSite site, IEditorInput input)
         				throws PartInitException {
     	
     	
 		// read choreography description from input
         try {
             // we expect IFileEditorInput here, 
             // ClassCassException is catched to force PartInitException
         	if (input instanceof IFileEditorInput) {
         		m_file = ((IFileEditorInput) input).getFile();
         	}
  
         	if (input instanceof IStorageEditorInput) {
         		m_scenario = create(input);
         	}
 
             // validate s
             if (null == getScenario()) {
                 throw new PartInitException("The specified input is not a scenario.");
             }
         } catch (CoreException e) {
             throw new PartInitException(e.getStatus());
         } catch (Exception e) {
             throw new PartInitException(
                 "The specified input is not a valid scenario.",
                 e);
         }
         
 		setPartName(input.getName());
 
         // choreography is ok         
         super.init(site, input);
 
         // add delegating CommandStackListener
         getDelegatingCommandStack().addCommandStackListener(
         			getDelegatingCommandStackListener());
 
         // add selection change listener
         getSite().getWorkbenchWindow().getSelectionService().
             		addSelectionListener(getSelectionListener());
 
         // initialize actions
         createActions();
 
         /* TODO: GPB: do we need a 'savara perspective'?
         org.eclipse.swt.widgets.Display.getCurrent().asyncExec(new Runnable() {     	
         	public void run() {
         		
         		try {
         			getSite().getWorkbenchWindow().getWorkbench().
         					showPerspective(SOAPerspective.ID,
         					getSite().getWorkbenchWindow());
         		} catch(Exception e) {
         			e.printStackTrace();
         		}
         	}
         });       
         */
     }
 
     public IFile getFile() {
     	return(m_file);
     }
     
     /**
      * Creates actions and registers them to the ActionRegistry.
      */
     protected void createActions() {
         addStackAction(new UndoAction(this));
         addStackAction(new RedoAction(this));
 
         addEditPartAction(new DeleteAction((IWorkbenchPart)this));
         addEditPartAction(new org.savara.tools.scenario.designer.editor.CopyAction((IWorkbenchPart) this));
         addEditPartAction(new org.savara.tools.scenario.designer.editor.PasteAction((IWorkbenchPart) this));
 
         m_printAction = new PrintAction(this);
         
         addAction(m_printAction);
 
         IAction zoomIn = new ZoomInAction(getDelegatingZoomManager());
         IAction zoomOut = new ZoomOutAction(getDelegatingZoomManager());
         addAction(zoomIn);
         addAction(zoomOut);
         getSite().getKeyBindingService().registerAction(zoomIn);
         getSite().getKeyBindingService().registerAction(zoomOut);
         
         // Add edit annotations action
         addEditPartAction(new CreateLinksAction((IWorkbenchPart)this));
         
         if (getEditorInput() instanceof IFileEditorInput) {
 	        addEditPartAction(new SimulateScenarioAction((IWorkbenchPart)this));
 	        addEditPartAction(new ResetSimulationAction((IWorkbenchPart)this));
         }
         
         //addEditPartAction(new ShowIdentityDetailsAction((IWorkbenchPart)this));
         
         addEditPartAction(new GenerateImageAction((IWorkbenchPart)this));
         
         addEditPartAction(new SimulationEntityFocusAction((IWorkbenchPart)this));
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
      */
     public void setInput(IEditorInput input)
     {   	
         if (getEditorInput() instanceof IFileEditorInput)
         {
         	IFile file = ((IFileEditorInput) getEditorInput()).getFile();
         	
         	/* TODO: GPB: resource tracker
             file.getWorkspace().removeResourceChangeListener(
                 getResourceTracker());
                 */
         }
 
         super.setInput(input);
 
         if (getEditorInput() instanceof IFileEditorInput) {
             m_file = ((IFileEditorInput) getEditorInput()).getFile();
            	/* TODO: GPB: resource tracker
             m_file.getWorkspace().addResourceChangeListener(getResourceTracker());
             */
         }
         
         if (getEditorInput() instanceof IStorageEditorInput) {
             setPartName(getEditorInput().getName());
             
             // If editor is not saving, then we need to reset the
             // choreography description
             if (!isEditorSaving()) {
             	try {
             		m_scenario = create(getEditorInput());
             			
             		AbstractEditorPage page=getScenarioEditorPage();
             		
             		if (page != null) {
             			page.refresh(input);
             		}            		
             		
             	} catch(Exception e) {
             		
             		// Need to close down after reporting failure
             		org.savara.tools.scenario.osgi.Activator.
             				logError("Failed to re-load Scenario " +
             						"after external update", e);
             		
             		closeEditor(false);
             	}
             }
         }
     }
     
     protected org.savara.scenario.model.Scenario create(IEditorInput input)
     						throws Exception {
     	org.savara.scenario.model.Scenario ret=null;
     	
     	if (input instanceof IFileEditorInput) {
     		ret = org.savara.scenario.util.ScenarioModelUtil.deserialize(
     						((IFileEditorInput)input).getFile().getContents());
     	} else if (input instanceof IStorageEditorInput) {
     		ret = org.savara.scenario.util.ScenarioModelUtil.deserialize(
     				((IStorageEditorInput)input).
     				getStorage().getContents());
     	}
     	
     	return(ret);
     }
 
     /**
      * Returns the resource tracker instance
      * @return
      */
     /*
     private ResourceTracker getResourceTracker() {
         if (m_resourceTracker == null) {
             m_resourceTracker = new ResourceTracker(this);
         }
 
         return(m_resourceTracker);
     }
     */
     
     /**
      * Adds an action to this editor's <code>ActionRegistry</code>.
      * (This is a helper method.)
      * 
      * @param action the action to add.
      */
     protected void addAction(IAction action) {
         getActionRegistry().registerAction(action);
     }
 
     /**
      * Adds an editor action to this editor.
      * 
      * <p><Editor actions are actions that depend
      * and work on the editor.
      * 
      * @param action the editor action
      */
     protected void addEditorAction(EditorPartAction action) {
         getActionRegistry().registerAction(action);
         m_editorActionIDs.add(action.getId());
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
      */
     protected void firePropertyChange(int propertyId) {
         super.firePropertyChange(propertyId);
         updateActions(m_editorActionIDs);
     }
 
     /**
      * Adds an <code>EditPart</code> action to this editor.
      * 
      * <p><code>EditPart</code> actions are actions that depend
      * and work on the selected <code>EditPart</code>s.
      * 
      * @param action the <code>EditPart</code> action
      */
     protected void addEditPartAction(SelectionAction action) {
         getActionRegistry().registerAction(action);
         m_editPartActionIDs.add(action.getId());
     }
 
     /**
      * Adds an <code>CommandStack</code> action to this editor.
      * 
      * <p><code>CommandStack</code> actions are actions that depend
      * and work on the <code>CommandStack</code>.
      * 
      * @param action the <code>CommandStack</code> action
      */
     protected void addStackAction(StackAction action) {
         getActionRegistry().registerAction(action);
         m_stackActionIDs.add(action.getId());
     }
 
     /**
 	 * This method indicates that the edited content is dirty.
 	 * 
 	 * @param dirty The dirty status
 	 */
 	public void setDirty(boolean dirty) {
         if (m_isDirty != dirty) {
             m_isDirty = dirty;
             firePropertyChange(IEditorPart.PROP_DIRTY);
         }
         
         // Refresh the visuals - required as model no longer provides notifications        
         getCurrentPage().refresh();
  	}
 	
     /**
      * Returns the test scenario object from the
      * specified file.
      * 
      * @param file The file
      * @return The test scenario object from the specified file
      */
 	/*
     private org.pi4soa.scenario.Scenario create(IFile file) throws CoreException {
     	org.pi4soa.scenario.Scenario ret=null;
 
         if (file.exists()) {
             try {
                 ret = org.pi4soa.scenario.ScenarioManager.load(file.getLocation().toString());
             } catch (Exception e) {
                 throw new CoreException(
                         new Status(
                             IStatus.ERROR,
                             DesignerDefinitions.DESIGNER_PLUGIN_ID,
                             0,
                             "Error loading the test scenario.",
                             e));
             }
 
             if (null == ret) {
                 throw new CoreException(
                     new Status(
                         IStatus.ERROR,
                         DesignerDefinitions.DESIGNER_PLUGIN_ID,
                         0,
                         "Error loading the test scenario.",
                         null));
             }
         }
         
         return(ret);
     }
     */
 	
     /**
      * This method returns the dirty status of the edited content.
      * 
      * @return Whether the content is dirty.
      * @see org.eclipse.ui.part.MultiPageEditorPart#isDirty()
      */
     public boolean isDirty() {
         return(m_isDirty);
     }
 
     /**
      * This method determines whether the contents can be saved.
      * 
      * @return Whether the contents can be saved
      * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
      */
     public boolean isSaveAsAllowed() {
         return(true);
     }
 
     /**
      * This method saves the content.
      * 
      * @param monitor Progress monitor
      * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
      */
     public void doSave(IProgressMonitor monitor) {
     	
     	if (getEditorInput() instanceof IFileEditorInput) {
 	        try {
 	            IFile file = ((IFileEditorInput) getEditorInput()).getFile();
 	            save(file, monitor);
 	            getMultiPageCommandStackListener().markSaveLocations();
 	        } catch (CoreException e) {
 	            ErrorDialog.openError(
 	                getSite().getShell(),
 	                "Error During Save",
 	                "The Choreography Description could not be saved.",
 	                e.getStatus());
 	        }
     	} else {
     		// Do save as, as edited version is not a local file
     		doSaveAs();
     	}
 	}
 
     /**
      * This method enables the user to select where the choreography
      * description should be saved to.
      * 
      * @see org.eclipse.ui.ISaveablePart#doSaveAs()
      */
     public void doSaveAs() {
         SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
         
         if (getEditorInput() instanceof IFileEditorInput) {
         	dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
         }
         
         dialog.open();
         IPath path = dialog.getResult();
 
         if (path == null)
             return;
 
         ProgressMonitorDialog progressMonitorDialog =
             new ProgressMonitorDialog(getSite().getShell());
         IProgressMonitor progressMonitor = null;
             //progressMonitorDialog.getProgressMonitor();
 
         try
         {
             save(ResourcesPlugin.getWorkspace().getRoot().getFile(path),
                 progressMonitor);
             getMultiPageCommandStackListener().markSaveLocations();
         }
         catch (CoreException e)
         {
             ErrorDialog.openError(
                 getSite().getShell(),
                 "Error During Save",
                 "The Scenario could not be saved.",
                 e.getStatus());
         }
     }
     
     /**
      * Saves the choreography description under the specified path.
      * 
      * @param file The file to be saved
      * @param path workspace relative path
      * @param progressMonitor
      */
     private synchronized void save(IFile file, IProgressMonitor progressMonitor)
         	throws CoreException {
 
     	// NOTE: Made method synchronized due to 'editor saving' flag, as
     	// another thread may cause this to be unset while a second invocation
     	// of the method is just starting, causing issues when it evaluates
     	// a resource change event in the ResourceTracker
     	//URI uri=m_scenario.eResource().getURI();
     	
         if (null == progressMonitor) {
             progressMonitor = new NullProgressMonitor();
         }
 
         progressMonitor.beginTask("Saving " + file, 2);
         
         m_editorSaving = true;
 
         try {
         	ByteArrayOutputStream baos=new ByteArrayOutputStream();
         	
         	ScenarioModelUtil.serialize(m_scenario, baos);
         	
             //ScenarioManager.save(m_scenario, baos, file.getCharset(true));
             //org.pi4soa.scenario.ScenarioManager.save(m_Scenario,
             //		baos, file.getCharset(true));
             
             baos.close();
             
             ByteArrayInputStream bais=
             		new ByteArrayInputStream(baos.toByteArray());
             
             if (file.exists() == false) {
             	file.create(bais, true, progressMonitor);
             } else {
             	file.setContents(bais, true, true, progressMonitor);
             }
             
             bais.close();
             
             progressMonitor.worked(1);
             file.refreshLocal(
                 IResource.DEPTH_ZERO,
                 new SubProgressMonitor(progressMonitor, 1));
             progressMonitor.done();
             
     		AbstractEditorPage page=
     					getCurrentPage();
     		
     		if (page != null) {
     			
     			// Get current focus
     			//Object focusComponent=page.getFocusComponent();
     			
     			page.refresh();
     			
     			//if (focusComponent != null) {
     			//	page.focus(focusComponent);
     			//}
     		}
     		
     		// Need to re-apply the URI, as this seems
     		// to get cleared when the scenario is saved
     		//m_scenario.eResource().setURI(uri);
     		            
         } catch (java.io.FileNotFoundException e) {
             IStatus status =
                 new Status(
                     IStatus.ERROR,
                     DesignerDefinitions.DESIGNER_PLUGIN_ID,
                     0,
                     "Error writing file.",
                     e);
             throw new CoreException(status);
         } catch (java.io.IOException e) {
             IStatus status =
                 new Status(
                     IStatus.ERROR,
                     DesignerDefinitions.DESIGNER_PLUGIN_ID,
                     0,
                     "Error writing file.",
                     e);
             throw new CoreException(status);
         } finally {
         	m_editorSaving = false;
         }
     }
     	
     /**
      * This method determines if the editor is currently in
      * the process of saving its modified content.
      * 
      * @return Whether the editor is currently saving
      */
     public boolean isEditorSaving() {
     	return(m_editorSaving);
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.ui.part.MultiPageEditorPart#dispose()
      */
     public void dispose() {
     	
         if (getEditorInput() instanceof IFileEditorInput) {
             IFile file = ((IFileEditorInput) getEditorInput()).getFile();
             
             /* TODO: GPB: resource tracker
             file.getWorkspace().removeResourceChangeListener(
                 getResourceTracker());
             */
         }
 
         // dispose multi page command stack listener
         getMultiPageCommandStackListener().dispose();
 
         // remove delegating CommandStackListener
         getDelegatingCommandStack().removeCommandStackListener(
             getDelegatingCommandStackListener());
 
         // remove selection listener
         getSite().getWorkbenchWindow().getSelectionService().
 				removeSelectionListener(getSelectionListener());
 
         // disposy the ActionRegistry (will dispose all actions)
         getActionRegistry().dispose();
 
         if (m_outlinePage != null) {
         	m_outlinePage.dispose();
         }
         
         // important: always call super implementation of dispose
         super.dispose();
     }    
 
     /**
      * Closes this editor.
      * @param save
      */
     public void closeEditor(final boolean save) {
         getSite().getShell().getDisplay().syncExec(new Runnable() {
             public void run()
             {
                 getSite().getPage().closeEditor(
                 		ScenarioDesigner.this, save);
             }
         });
     }
 
     /**
      * This method returns the multipage command stack listener.
      * 
      * @return The command stack listener
      */
     protected MultiPageCommandStackListener getMultiPageCommandStackListener() {
         if (null == m_multiPageCommandStackListener) {
             m_multiPageCommandStackListener = new MultiPageCommandStackListener(this);
         }
     	return(m_multiPageCommandStackListener);
     }
 
     /**
      * Returns the <code>CommandStack</code> for this editor.
      * @return the <code>CommandStack</code>
      */
     protected DelegatingCommandStack getDelegatingCommandStack() {
         if (null == m_delegatingCommandStack) {
         	m_delegatingCommandStack = new DelegatingCommandStack();
             if (null != getCurrentPage()) {
             	m_delegatingCommandStack.setCurrentCommandStack(
             			getCurrentPage().getCommandStack2());
             }
         }
 
         return(m_delegatingCommandStack);
     }
 
     /**
      * Returns the <code>CommandStackListener</code> for 
      * the <code>DelegatingCommandStack</code>.
      * @return the <code>CommandStackListener</code>
      */
     protected CommandStackListener getDelegatingCommandStackListener() {
         return(m_delegatingCommandStackListener);
     }
 
     /**
      * Returns the <code>DelegatingZoomManager</code> for this editor.
      * @return the <code>DelegatingZoomManager</code>
      */
     protected DelegatingZoomManager getDelegatingZoomManager() {
         if (null == m_delegatingZoomManager) {
         	m_delegatingZoomManager = new DelegatingZoomManager();
             if (null != getCurrentPage()
             		&& null != getCurrentPage().getViewer()) {
             	m_delegatingZoomManager.setCurrentZoomManager(
                     getZoomManager(getCurrentPage().getViewer()));
             }
         }
 
         return(m_delegatingZoomManager);
     }
 
     /**
      * Returns the undoable <code>PropertySheetPage</code> for
      * this editor.
      * 
      * @return the undoable <code>PropertySheetPage</code>
      */
     protected IPropertySheetPage getPropertySheetPage() {
         if (null == m_undoablePropertySheetPage) {
         	m_undoablePropertySheetPage =
         		new DesignerTabbedPropertySheetPage(this,
              		getDelegatingCommandStack());
         	
         	//m_undoablePropertySheetPage = new PropertySheetPage();
             //m_undoablePropertySheetPage.setRootEntry(
             //        GEFPlugin.createUndoablePropertySheetEntry(
             //            getDelegatingCommandStack()));
 // Change to make to get rid of the deprecation warning, however,
 // this makes the designer dependent upon GEF3.1
 //            		new org.eclipse.gef.ui.properties.UndoablePropertySheetEntry(
 //                    getDelegatingCommandStack()));
         }
 
         return(m_undoablePropertySheetPage);
     }
 
     /**
      * Returns the zoom manager of the specified viewer.
      * @param viewer the viewer to get the zoom manager from
      * @return the zoom manager
      */
     private ZoomManager getZoomManager(org.eclipse.gef.EditPartViewer viewer) {
         // get zoom manager from root edit part
         RootEditPart rootEditPart = viewer.getRootEditPart();
         ZoomManager zoomManager = null;
         if (rootEditPart instanceof ScalableFreeformRootEditPart) {
             zoomManager =
                 ((ScalableFreeformRootEditPart) rootEditPart).getZoomManager();
         } else if (rootEditPart instanceof ScalableRootEditPart) {
             zoomManager =
                 ((ScalableRootEditPart) rootEditPart).getZoomManager();
         }
         return zoomManager;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.part.EditorPart#gotoMarker(org.eclipse.core.resources.IMarker)
      */
     public void gotoMarker(IMarker marker) {
  
 		try {
 	       	/* TODO: GPB: handle navigation to object
 	       	 * 
 			if (marker.getType().equals(EValidator.MARKER)) {
 				String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
 				if (uriAttribute != null) {
 					URI uri = URI.createURI(uriAttribute);
 
 					org.eclipse.emf.ecore.EObject scenarioObject=
 								(org.eclipse.emf.ecore.EObject)
 						m_scenario.eResource().getEObject(uri.fragment());
 					
 					if (scenarioObject != null) {
 						getCurrentPage().focus(scenarioObject);
 					}
 				}
 			}
 			*/
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
     }
 
     /**
      * Sets the currently active page.
      *
      * @param pageIndex the index of the page to be activated; the index must be valid
      */
     protected void setActivePage(int pageIndex) {
     	super.setActivePage(pageIndex);
     	
     	updateAfterPageChange(pageIndex);
     }
     
     /**
      * Returns the default <code>PaletteRoot</code> for this editor and all
      * its pages.
      * @return the default <code>PaletteRoot</code>
      */
     public PaletteRoot getPaletteRoot() {
         if (null == m_paletteRoot) {
             // create root
          	m_paletteRoot = new ScenarioPaletteRoot();
         }
         return(m_paletteRoot);
     }
 
     /**
      * Returns the selection syncronizer object. 
      * The synchronizer can be used to sync the selection of 2 or more
      * EditPartViewers.
      * @return the syncrhonizer
      */
     public SelectionSynchronizer getSelectionSynchronizer() {
         if (m_synchronizer == null) {
             m_synchronizer = new SelectionSynchronizer();
         }
         return(m_synchronizer);
     }
 
     /**
      * Updates the specified actions.
      * 
      * @param actionIds the list of ids of actions to update
      */
     private void updateActions(java.util.List actionIds) {
         for (java.util.Iterator ids = actionIds.iterator();
         				ids.hasNext();) {
             IAction action = getActionRegistry().getAction(ids.next());
             if (null != action && action instanceof UpdateAction) {
                  ((UpdateAction) action).update();
             }
         }
     }
     
     protected void updateEditPartActions() {
     	updateActions(m_editPartActionIDs);
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
      */
     public Object getAdapter(Class type) {
     	
         if (type == IPropertySheetPage.class) {
             return getPropertySheetPage();
         } else if (type == CommandStack.class) {
             return getDelegatingCommandStack();
         } else if (type == ActionRegistry.class) {
             return getActionRegistry();
         } else if (type == IContentOutlinePage.class) {
             return getOutlinePage();
         } else if (type == ZoomManager.class) {
             return getDelegatingZoomManager();
         } else {
         	Object ret=null;
         	
         	if (getCurrentPage() != null) {
         		ret = getCurrentPage().getAdapter(type);
         	}
         	
         	if (ret != null) {
         		return(ret);
         	}
         }
 
         return super.getAdapter(type);
     }
 
     /**
      * Returns the selection listener.
      * 
      * @return the <code>ISelectionListener</code>
      */
     protected ISelectionListener getSelectionListener() {
         return(m_selectionListener);
     }
 
     /**
      * Returns the action registry of this editor.
      * @return the action registry
      */
     public ActionRegistry getActionRegistry() {
         if (m_actionRegistry == null) {
         	m_actionRegistry = new ActionRegistry();
     	}
 
         return(m_actionRegistry);
     }
 
     public KeyHandler getSharedKeyHandler() {
     	if (m_sharedKeyHandler == null) {
     		m_sharedKeyHandler = new KeyHandler();
     		m_sharedKeyHandler.put(
     			KeyStroke.getPressed(SWT.DEL, 127, 0),
     			getActionRegistry().getAction(org.eclipse.ui.actions.ActionFactory.DELETE.getId()));
     		m_sharedKeyHandler.put(
     			KeyStroke.getPressed(SWT.F2, 0),
     			getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
     	}
     	return(m_sharedKeyHandler);
     }
 
     protected FigureCanvas getEditor(){
     	return (FigureCanvas)getScenarioEditorPage().getViewer().getControl();
     }
     
     protected OutlinePage getOutlinePage() {
     	if (m_outlinePage == null) {
     		m_outlinePage = new OutlinePage();
     	}
     	return(m_outlinePage);
     }
     
     private boolean m_isDirty=false;
     private boolean m_editorSaving=false;
     private MultiPageCommandStackListener m_multiPageCommandStackListener=null;
     private PaletteRoot m_paletteRoot=null;
     private KeyHandler m_sharedKeyHandler=null;
     private SelectionSynchronizer m_synchronizer=null;
     private int m_scenarioEditorPageID=-1;
 	// Comment out while not displaying simulation log
     //private int m_simulationLogPageID=-1;
     private DelegatingCommandStack m_delegatingCommandStack=null;
     private DelegatingZoomManager m_delegatingZoomManager=null;    
     private org.savara.scenario.model.Scenario m_scenario=null;
     private org.eclipse.core.resources.IFile m_file=null;
     private java.util.List m_stackActionIDs = new java.util.ArrayList();
     private java.util.List m_editPartActionIDs = new java.util.ArrayList();
     private java.util.List m_editorActionIDs = new java.util.ArrayList();
     private ActionRegistry m_actionRegistry=null;
     private IPropertySheetPage m_undoablePropertySheetPage=null;
     //private ResourceTracker m_resourceTracker=null;
     private PrintAction m_printAction=null;
     
     private OutlinePage m_outlinePage=null;
 
     /**
      * The <code>CommandStackListener</code> that listens for
      * changes of the <code>DelegatingCommandStack</code>.
      */
     private CommandStackListener m_delegatingCommandStackListener =
         				new CommandStackListener() {
         public void commandStackChanged(java.util.EventObject event) {
             updateActions(m_stackActionIDs);
         }
     };
     
     private ISelectionListener m_selectionListener = new ISelectionListener() {
         public void selectionChanged(IWorkbenchPart part, ISelection selection) {
             updateActions(m_editPartActionIDs);
         }
     };
     
     class OutlinePage extends ContentOutlinePage
 				implements IAdaptable {
 		
 		public OutlinePage(){
 			super();
 		}
 		
 		public void init(IPageSite pageSite) {
 			super.init(pageSite);
 			ActionRegistry registry = getActionRegistry();
 			IActionBars bars = pageSite.getActionBars();
 			String id = ActionFactory.UNDO.getId();
 			bars.setGlobalActionHandler(id, registry.getAction(id));
 			id = ActionFactory.REDO.getId();
 			bars.setGlobalActionHandler(id, registry.getAction(id));
 			id = ActionFactory.DELETE.getId();
 			bars.setGlobalActionHandler(id, registry.getAction(id));
 			bars.updateActionBars();
 		}
 	
 		protected void configureOutlineViewer(){
 			if (thumbnail == null)
 				initializeOverview();
 			pageBook.showPage(overview);
 			thumbnail.setVisible(true);
 		}
 	
 		public void createControl(Composite parent){
 			pageBook = new PageBook(parent, SWT.NONE);
 			overview = new Canvas(pageBook, SWT.NONE);
			//pageBook.showPage(outline);
 			configureOutlineViewer();
 		}
 		
 		public void dispose(){
 			if (thumbnail != null) {
 				thumbnail.deactivate();
 				thumbnail = null;
 			}
 			//unhookOverview();
 			
 			super.dispose();
 		}
 		
 		public Object getAdapter(Class type) {
 			if (type == ZoomManager.class)
 				return getScenarioEditorPage().getViewer().getProperty(ZoomManager.class.toString());
 			return null;
 		}
 	
 		public Control getControl() {
 			return pageBook;
 		}
 				
 		protected void initializeOverview() {
 			LightweightSystem lws = new LightweightSystem(overview);
 			RootEditPart rep = getScenarioEditorPage().getViewer().getRootEditPart();
 			if (rep instanceof ScalableFreeformRootEditPart) {
 				ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart)rep;
 				thumbnail = new org.eclipse.draw2d.parts.ScrollableThumbnail((Viewport)root.getFigure());
 				thumbnail.setBorder(new MarginBorder(3));
 				thumbnail.setSource(root.getLayer(LayerConstants.PRINTABLE_LAYERS));
 				lws.setContents(thumbnail);
 				/*
 				disposeListener = new DisposeListener() {
 					public void widgetDisposed(DisposeEvent e) {
 						if (thumbnail != null) {
 							thumbnail.deactivate();
 							thumbnail = null;
 						}
 					}
 				};
 				getEditor().addDisposeListener(disposeListener);
 				*/
 			}
 		}
 		
 		public void setFocus() {
 		}
 		
 		public void setContents(Object contents) {
 			getScenarioEditorPage().getViewer().setContents(contents);
 		}
 		
 		/*
 		protected void unhookOverview(){
 			if (disposeListener != null && getEditor() != null && !getEditor().isDisposed())
 				getEditor().removeDisposeListener(disposeListener);
 		}
 		*/
 		
 		private PageBook pageBook;
 		private Control outline;
 		private Canvas overview;
 		private org.eclipse.draw2d.parts.Thumbnail thumbnail;
 		//private DisposeListener disposeListener;
     }
 }
