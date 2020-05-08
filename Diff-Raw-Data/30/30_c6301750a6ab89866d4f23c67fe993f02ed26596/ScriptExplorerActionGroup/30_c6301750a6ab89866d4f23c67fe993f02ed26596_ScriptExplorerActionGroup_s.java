 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.ui.scriptview;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IOpenable;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.ui.actions.BuildActionGroup;
 import org.eclipse.dltk.internal.ui.actions.CCPActionGroup;
 import org.eclipse.dltk.internal.ui.actions.CompositeActionGroup;
 import org.eclipse.dltk.internal.ui.actions.ImportActionGroup;
 import org.eclipse.dltk.internal.ui.actions.NavigateActionGroup;
 import org.eclipse.dltk.internal.ui.actions.NewWizardsActionGroup;
 import org.eclipse.dltk.internal.ui.actions.ProjectActionGroup;
 import org.eclipse.dltk.internal.ui.actions.refactoring.RefactorActionGroup;
 import org.eclipse.dltk.internal.ui.wizards.buildpath.newsourcepage.GenerateBuildPathActionGroup;
 import org.eclipse.dltk.internal.ui.workingsets.ViewActionGroup;
 import org.eclipse.dltk.internal.ui.workingsets.WorkingSetActionGroup;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.IContextMenuConstants;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.actions.CustomFiltersActionGroup;
 import org.eclipse.dltk.ui.actions.GenerateActionGroup;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.OpenStrategy;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.IWorkingSetManager;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ActionGroup;
 import org.eclipse.ui.actions.OpenInNewWindowAction;
 import org.eclipse.ui.views.framelist.BackAction;
 import org.eclipse.ui.views.framelist.ForwardAction;
 import org.eclipse.ui.views.framelist.Frame;
 import org.eclipse.ui.views.framelist.FrameAction;
 import org.eclipse.ui.views.framelist.FrameList;
 import org.eclipse.ui.views.framelist.GoIntoAction;
 import org.eclipse.ui.views.framelist.TreeFrame;
 import org.eclipse.ui.views.framelist.UpAction;
 
 
 public class ScriptExplorerActionGroup extends CompositeActionGroup {
 
 	private ScriptExplorerPart fPart;
 
 	private FrameList fFrameList;
 	private GoIntoAction fZoomInAction;
  	private BackAction fBackAction;
 	private ForwardAction fForwardAction;
 	private UpAction fUpAction;
 	//private GotoTypeAction fGotoTypeAction;
 	//private GotoPackageAction fGotoPackageAction;
 	//private GotoResourceAction fGotoResourceAction;
 	private CollapseAllAction fCollapseAllAction;
 	
 	
 	private ToggleLinkingAction fToggleLinkingAction;
 
 	private RefactorActionGroup fRefactorActionGroup;
 	private NavigateActionGroup fNavigateActionGroup;
 	private ViewActionGroup fViewActionGroup;
 	
 	private CustomFiltersActionGroup fCustomFiltersActionGroup;
 
	private IAction fGotoRequiredProjectAction;	
  	
 	public ScriptExplorerActionGroup(ScriptExplorerPart part) {
 		super();
 		fPart= part;
 		TreeViewer viewer= part.getTreeViewer();
 		
 		IPropertyChangeListener workingSetListener= new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				doWorkingSetChanged(event);
 			}
 		};
 		
 		IWorkbenchPartSite site = fPart.getSite();
 		setGroups(new ActionGroup[] {
 			new NewWizardsActionGroup(site),
 			fNavigateActionGroup= new NavigateActionGroup(fPart), 
 			new CCPActionGroup(fPart),
             new GenerateBuildPathActionGroup(fPart),
 			new GenerateActionGroup(fPart, IContextMenuConstants.GROUP_SHOW), 
 			fRefactorActionGroup= new RefactorActionGroup(fPart),
 			new ImportActionGroup(fPart),
 			new BuildActionGroup(fPart),
 			//new ScriptSearchActionGroup(fPart),
			new ProjectActionGroup(fPart), 
 			fViewActionGroup= new ViewActionGroup(fPart.getRootMode(), workingSetListener, site),
 			fCustomFiltersActionGroup= new CustomFiltersActionGroup(fPart, viewer),
 			new LayoutActionGroup(fPart),
 			// the working set action group must be created after the project action group
 			new WorkingSetActionGroup(fPart)});
 		
 
 		fViewActionGroup.fillFilters(viewer);
 		
 		ScriptFrameSource frameSource= new ScriptFrameSource(fPart);
 		fFrameList= new FrameList(frameSource);
 		frameSource.connectTo(fFrameList);
 			
 		fZoomInAction= new GoIntoAction(fFrameList);
 		fBackAction= new BackAction(fFrameList);
 		fForwardAction= new ForwardAction(fFrameList);
 		fUpAction= new UpAction(fFrameList);
 		
 		//fGotoTypeAction= new GotoTypeAction(fPart);
 		//fGotoPackageAction= new GotoPackageAction(fPart);
 		//fGotoResourceAction= new GotoResourceAction(fPart);
 		fCollapseAllAction= new CollapseAllAction(fPart);	
 		fToggleLinkingAction = new ToggleLinkingAction(fPart); 
 		//fGotoRequiredProjectAction= new GotoRequiredProjectAction(fPart);
 	}
 
 	public void dispose() {
 		super.dispose();
 	}
 	
 
 	//---- Persistent state -----------------------------------------------------------------------
 
 	/* package */ void restoreFilterAndSorterState(IMemento memento) {
 		fViewActionGroup.restoreState(memento);
 		fCustomFiltersActionGroup.restoreState(memento);
 	}
 	
 	/* package */ void saveFilterAndSorterState(IMemento memento) {
 		fViewActionGroup.saveState(memento);
 		fCustomFiltersActionGroup.saveState(memento);
 	}
 
 	//---- Action Bars ----------------------------------------------------------------------------
 
 	public void fillActionBars(IActionBars actionBars) {
 		super.fillActionBars(actionBars);
 		setGlobalActionHandlers(actionBars);
 		fillToolBar(actionBars.getToolBarManager());
 		fillViewMenu(actionBars.getMenuManager());		
 	}
 
 	protected void updateActionBars(IActionBars actionBars) {
 		actionBars.getToolBarManager().removeAll();
 		actionBars.getMenuManager().removeAll();
 		fillActionBars(actionBars);
 		actionBars.updateActionBars();
 		fZoomInAction.setEnabled(true);
 	}
 
 	protected void setGlobalActionHandlers(IActionBars actionBars) {
 		// Navigate Go Into and Go To actions.
 		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.GO_INTO, fZoomInAction);
 		actionBars.setGlobalActionHandler(ActionFactory.BACK.getId(), fBackAction);
 		actionBars.setGlobalActionHandler(ActionFactory.FORWARD.getId(), fForwardAction);
 		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.UP, fUpAction);
 		//actionBars.setGlobalActionHandler(IWorkbenchActionConstants.GO_TO_RESOURCE, fGotoResourceAction);
 		//actionBars.setGlobalActionHandler(DLTKActionConstants.GOTO_TYPE, fGotoTypeAction);
 		//actionBars.setGlobalActionHandler(DLTKActionConstants.GOTO_PACKAGE, fGotoPackageAction);
 		
 		fRefactorActionGroup.retargetFileMenuActions(actionBars);
 	}
 
 	protected void fillToolBar(IToolBarManager toolBar) {
 		toolBar.add(fBackAction);
 		toolBar.add(fForwardAction);
 		toolBar.add(fUpAction); 
 		
 		toolBar.add(new Separator());
 		toolBar.add(fCollapseAllAction);
 		toolBar.add(fToggleLinkingAction);
 	}
 	
 	protected void fillViewMenu(IMenuManager menu) {
 		menu.add(new Separator());
 		menu.add(fToggleLinkingAction);
 
 		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$		
 	}
 
 	//---- Context menu -------------------------------------------------------------------------
 
 	public void fillContextMenu(IMenuManager menu) {		
 		IStructuredSelection selection= (IStructuredSelection)getContext().getSelection();
 		int size= selection.size();
 		Object element= selection.getFirstElement();
 		
 		if (element instanceof BuildPathContainer.RequiredProjectWrapper) 
 			menu.appendToGroup(IContextMenuConstants.GROUP_SHOW, fGotoRequiredProjectAction);
 		
 		addGotoMenu(menu, element, size);
 		
 		addOpenNewWindowAction(menu, element);
 		
 		super.fillContextMenu(menu);
 	}
 	
 	 protected void addGotoMenu(IMenuManager menu, Object element, int size) {
 		boolean enabled= size == 1 && fPart.getTreeViewer().isExpandable(element) && (isGoIntoTarget(element) || element instanceof IContainer);
 		fZoomInAction.setEnabled(enabled);
 		if (enabled)
 			menu.appendToGroup(IContextMenuConstants.GROUP_GOTO, fZoomInAction);
 	}
 	
 	protected boolean isGoIntoTarget(Object element) {
 		if (element == null)
 			return false;
 		if (element instanceof IModelElement) {
 			int type= ((IModelElement)element).getElementType();
 			return type == IModelElement.SCRIPT_PROJECT || 
 				type == IModelElement.PROJECT_FRAGMENT || 
 				type == IModelElement.SCRIPT_FOLDER;
 		}
 		if (element instanceof IWorkingSet) {
 			return true;
 		}
 		return false;
 	}
 
 	protected void addOpenNewWindowAction(IMenuManager menu, Object element) {
 		if (element instanceof IModelElement) {
 			element= ((IModelElement)element).getResource();
 			
 		}
 		// fix for 64890 Package explorer out of sync when open/closing projects [package explorer] 64890  
 		if (element instanceof IProject && !((IProject)element).isOpen()) 
 			return;
 		
 		if (!(element instanceof IContainer))
 			return;
 		menu.appendToGroup(
 			IContextMenuConstants.GROUP_OPEN, 
 			new OpenInNewWindowAction(fPart.getSite().getWorkbenchWindow(), (IContainer)element));
 	}
 
 	//---- Key board and mouse handling ------------------------------------------------------------
 
 	protected void handleDoubleClick(DoubleClickEvent event) {
 		TreeViewer viewer= fPart.getTreeViewer();
 		IStructuredSelection selection= (IStructuredSelection)event.getSelection();
 		Object element= selection.getFirstElement();
 		if (viewer.isExpandable(element)) {
 			if (doubleClickGoesInto()) {
 				// don't zoom into compilation units and class files
 				if (element instanceof ISourceModule)
 					return;
 				if (element instanceof IOpenable || element instanceof IContainer || element instanceof IWorkingSet) {
 					fZoomInAction.run();
 				}
 			} else {
 				IAction openAction= fNavigateActionGroup.getOpenAction();
 				if (openAction != null && openAction.isEnabled() && OpenStrategy.getOpenMethod() == OpenStrategy.DOUBLE_CLICK)
 					return;
 				if (selection instanceof ITreeSelection) {
 					TreePath[] paths= ((ITreeSelection)selection).getPathsFor(element);
 					for (int i= 0; i < paths.length; i++) {
 						viewer.setExpandedState(paths[i], !viewer.getExpandedState(paths[i]));
 					}
 				} else {
 					viewer.setExpandedState(element, !viewer.getExpandedState(element));
 				}
 			}
 		}
 	}
 	
 	protected void handleOpen(OpenEvent event) {
 		IAction openAction= fNavigateActionGroup.getOpenAction();
 		if (openAction != null && openAction.isEnabled()) {
 			openAction.run();
 			return;
 		}
 	}
 	
 	protected void handleKeyEvent(KeyEvent event) {
 		if (event.stateMask != 0) 
 			return;		
 		
 		if (event.keyCode == SWT.BS) {
 			if (fUpAction != null && fUpAction.isEnabled()) {
 				fUpAction.run();
 				event.doit= false;
 			}
 		}
 	}
 	
 	private void doWorkingSetChanged(PropertyChangeEvent event) {
 		if (ViewActionGroup.MODE_CHANGED.equals(event.getProperty())) {
 			fPart.rootModeChanged(((Integer)event.getNewValue()).intValue());
 			Object oldInput= null;
 			Object newInput= null;
 			if (fPart.getRootMode() == ScriptExplorerPart.PROJECTS_AS_ROOTS) {
 				oldInput= fPart.getWorkingSetModel();
 				newInput= DLTKCore.create(ResourcesPlugin.getWorkspace().getRoot());
 			} else if (fPart.getRootMode() == ScriptExplorerPart.WORKING_SETS_AS_ROOTS) {
 				oldInput= DLTKCore.create(ResourcesPlugin.getWorkspace().getRoot());
 				newInput= fPart.getWorkingSetModel();
 			}
 			if (oldInput != null && newInput != null) {
 				Frame frame;
 				for (int i= 0; (frame= fFrameList.getFrame(i)) != null; i++) {
 					if (frame instanceof TreeFrame) {
 						TreeFrame treeFrame= (TreeFrame)frame;
 						if (oldInput.equals(treeFrame.getInput()))
 							treeFrame.setInput(newInput);
 					}
 				}
 			}
 		} else {
 			IWorkingSet workingSet= (IWorkingSet) event.getNewValue();
 			
 			String workingSetLabel= null;
 			if (workingSet != null)
 				workingSetLabel= workingSet.getLabel();
 			fPart.setWorkingSetLabel(workingSetLabel);
 			fPart.updateTitle();
 	
 			String property= event.getProperty();
 			if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property)) {
 				TreeViewer viewer= fPart.getTreeViewer();
 				viewer.getControl().setRedraw(false);
 				viewer.refresh();
 				viewer.getControl().setRedraw(true);
 			}
 		}
 	}
 
 	private boolean doubleClickGoesInto() {
 		return PreferenceConstants.DOUBLE_CLICK_GOES_INTO.equals(DLTKUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DOUBLE_CLICK));
 	}
 
 	public FrameAction getUpAction() {
 		return fUpAction;
 	}
 
 	public FrameAction getBackAction() {
 		return fBackAction;
 	}
 	public FrameAction getForwardAction() {
 		return fForwardAction;
 	}
 
 	public ViewActionGroup getWorkingSetActionGroup() {
 	    return fViewActionGroup;
 	}
 	
 	public CustomFiltersActionGroup getCustomFilterActionGroup() {
 	    return fCustomFiltersActionGroup;
 	}
 	
 	public FrameList getFrameList() {
 		return fFrameList;
 	}
 
 	/**
 	 * @return the fPart
 	 */
 	protected ScriptExplorerPart getPart() {
 		return fPart;
 	}
 }
