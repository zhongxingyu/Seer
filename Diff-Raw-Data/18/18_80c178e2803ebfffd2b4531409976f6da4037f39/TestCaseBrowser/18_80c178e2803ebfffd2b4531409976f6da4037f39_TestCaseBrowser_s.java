 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.views;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jubula.client.core.businessprocess.db.NodeBP;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProblemPropagationListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecObjContPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.actions.CutTreeItemActionTCBrowser;
 import org.eclipse.jubula.client.ui.rcp.actions.MoveTestCaseAction;
 import org.eclipse.jubula.client.ui.rcp.actions.PasteTreeItemActionTCBrowser;
 import org.eclipse.jubula.client.ui.rcp.constants.RCPCommandIDs;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionClipboardTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TCBrowserDndSupport;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TestSpecDropTargetListener;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TreeViewerContainerDragSourceListener;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.DecoratingCellLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.TestCaseBrowserContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.TestCaseBrowserLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.utils.SelectionChecker;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.menus.CommandContributionItem;
 
 
 /** 
  * @author BREDEX GmbH
  * @created 05.07.2004
  */
 public class TestCaseBrowser extends AbstractJBTreeView 
    implements ITreeViewerContainer, IJBPart, IProblemPropagationListener {
     /** New-menu ID */
     public static final String NEW_ID = PlatformUI.PLUGIN_ID + ".NewSubMenu"; //$NON-NLS-1$
     /** Identifies the workbench plug-in */
     public static final String OPEN_WITH_ID = PlatformUI.PLUGIN_ID + ".OpenWithSubMenu"; //$NON-NLS-1$
     /** Add-Submenu ID */
     public static final String ADD_ID = PlatformUI.PLUGIN_ID + ".AddSubMenu"; //$NON-NLS-1$
        
     /** The action to cut TreeItems */
     private CutTreeItemActionTCBrowser m_cutTreeItemAction;
     /** The action to paste TreeItems */
     private PasteTreeItemActionTCBrowser m_pasteTreeItemAction;
     /** The action to move Test Cases */
     private MoveTestCaseAction m_moveTestCaseAction;
     /** The actionlistener of the treeViewer */
     private ActionListener m_actionListener;
     /** <code>m_doubleClickListener</code> */
     private final DoubleClickListener m_doubleClickListener = 
         new DoubleClickListener();
     
     /**
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         m_cutTreeItemAction = new CutTreeItemActionTCBrowser();
         m_pasteTreeItemAction = new PasteTreeItemActionTCBrowser();
         m_moveTestCaseAction = new MoveTestCaseAction();
         super.createPartControl(parent);
         ColumnViewerToolTipSupport.enableFor(getTreeViewer());
         getTreeViewer().setContentProvider(
                 new TestCaseBrowserContentProvider());
         DecoratingCellLabelProvider lp = new DecoratingCellLabelProvider(
                 new TestCaseBrowserLabelProvider(), Plugin.getDefault()
                         .getWorkbench().getDecoratorManager()
                         .getLabelDecorator());
         getTreeViewer().setLabelProvider(lp);
 
         int ops = DND.DROP_MOVE;
         Transfer[] transfers = new Transfer[] {LocalSelectionTransfer
             .getInstance()};
         getTreeViewer().addDragSupport(ops, transfers,
             new TreeViewerContainerDragSourceListener(getTreeViewer()));
         getTreeViewer().addDropSupport(ops, transfers,
             new TestSpecDropTargetListener(this));
         
         registerContextMenu(); 
         Plugin.getHelpSystem().setHelp(getTreeViewer().getControl(),
             ContextHelpIds.TEST_SPEC_VIEW);
         
         configureActionBars();
         
         if (GeneralStorage.getInstance().getProject() != null) {
             handleProjectLoaded();
         }
         
        DataEventDispatcher.getInstance().addProblemPropagationListener(this);
         Plugin.getDefault().getTreeViewerContainer().add(this);
     }
 
     /**
      * Registers global action handlers and listeners. 
      */
     private void configureActionBars() {
         getTreeFilterText().addFocusListener(new FocusListener() {
             /** the default cut action */
             private IAction m_defaultCutAction = getViewSite()
                 .getActionBars().getGlobalActionHandler(
                         ActionFactory.CUT.getId()); 
             
             /** the default paste action */
             private IAction m_defaultPasteAction = getViewSite()
                 .getActionBars().getGlobalActionHandler(
                     ActionFactory.PASTE.getId());
             
             public void focusGained(FocusEvent e) {
                 getViewSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.CUT.getId(), m_defaultCutAction);
                 getViewSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), m_defaultPasteAction);
                 getViewSite().getActionBars().updateActionBars();
             }
 
             public void focusLost(FocusEvent e) {
                 getViewSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.CUT.getId(), m_cutTreeItemAction);
                 getViewSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), m_pasteTreeItemAction);
                 getViewSite().getActionBars().updateActionBars();
             }
         });
         
         getViewSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.CUT.getId(), m_cutTreeItemAction);
         getViewSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.PASTE.getId(), m_pasteTreeItemAction);
         getViewSite().getActionBars().setGlobalActionHandler(
             Constants.MOVE_TESTCASE_ACTION_ID, m_moveTestCaseAction);
         getViewSite().getWorkbenchWindow().getSelectionService()
             .addSelectionListener(m_actionListener);
         getViewSite().getActionBars().updateActionBars();
     }
 
     /** {@inheritDoc} */
     protected void createContextMenu(IMenuManager mgr) {
         MenuManager submenuNew = new MenuManager(
                 Messages.TestSuiteBrowserNew, NEW_ID);
         MenuManager submenuAdd = new MenuManager(
                 Messages.TestSuiteBrowserAdd, ADD_ID);
         MenuManager submenuOpenWith = new MenuManager(
                 Messages.TestSuiteBrowserOpenWith, OPEN_WITH_ID);
         // build menu
         mgr.add(submenuNew);
         mgr.add(submenuAdd);
         CommandHelper.createContributionPushItem(submenuNew,
                 RCPCommandIDs.NEW_TESTCASE_COMMAND_ID);
         CommandHelper.createContributionPushItem(submenuNew,
                 RCPCommandIDs.NEW_CATEGORY_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.RENAME_COMMAND_ID);
         mgr.add(m_moveTestCaseAction);
         mgr.add(CommandHelper.createContributionItem(
                 RCPCommandIDs.FIND_COMMAND_ID,
                 null, Messages.FindContextMenu,
                 CommandContributionItem.STYLE_PUSH));
         mgr.add(m_cutTreeItemAction);
         mgr.add(m_pasteTreeItemAction);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.OPEN_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.SHOW_WHERE_USED_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.COPY_ID_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(submenuOpenWith,
                 RCPCommandIDs.OPEN_TESTCASE_EDITOR_COMMAND_ID);
         mgr.add(new Separator());
         mgr.add(submenuOpenWith);
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
     } 
     
     /**
      * Adds DoubleClick-Support to Treeview. Adds SelectionChanged-Support to
      * TreeView.
      */
     protected void addTreeListener() {
         getTreeViewer().addDoubleClickListener(m_doubleClickListener);
         m_actionListener = new ActionListener();
     }  
     
     /**
      * Sets the focus and shows the status line.
      */
     public void setFocus() {
         getTreeViewer().getControl().setFocus();
         Plugin.showStatusLine(this);
     }
 
     /**
      * @return the actual selection
      */
     IStructuredSelection getActualSelection() {
         ISelection selection = 
             getViewSite().getSelectionProvider().getSelection();
         return selection instanceof IStructuredSelection 
             ? (IStructuredSelection)selection : StructuredSelection.EMPTY;
     }
     
     /** {@inheritDoc} */
     public void dispose() {
         try {
             DataEventDispatcher ded = DataEventDispatcher.getInstance();
             ded.removeDataChangedListener(this);
            ded.removeProblemPropagationListener(this);
             getViewSite().getWorkbenchWindow().getSelectionService()
                 .removeSelectionListener(m_actionListener);
             getTreeViewer().removeDoubleClickListener(m_doubleClickListener);
             Plugin.getDefault().getTreeViewerContainer().remove(this);
         } finally {
             super.dispose();
         }
     }
     
     /**
      * {@inheritDoc}
      */
     protected void rebuildTree() {
         IProjectPO activeProject = GeneralStorage.getInstance().getProject();
         if (activeProject != null) {
             getTreeViewer().setInput(
                     new ISpecObjContPO[] {activeProject.getSpecObjCont()});
             getTreeViewer().expandToLevel(DEFAULT_EXPANSION);
         } else {
             getTreeViewer().setInput(null);
         }
     }
 
     /**
      * @author BREDEX GmbH
      * @created Jan 22, 2007
      */
     private final class DoubleClickListener implements IDoubleClickListener {
         /**
          * {@inheritDoc}
          */
         public void doubleClick(DoubleClickEvent event) {
             IStructuredSelection selection = getActualSelection();
             int[] counter = SelectionChecker.selectionCounter(selection);
             if (counter[SelectionChecker.SPEC_CONT] == selection.size()
                     || counter[SelectionChecker.CATEGORY] == selection.size()) {
                 CommandHelper.executeCommand(
                         RCPCommandIDs.NEW_TESTCASE_COMMAND_ID, getSite());
             } else {
                 CommandHelper.executeCommand(
                         RCPCommandIDs.OPEN_TESTCASE_EDITOR_COMMAND_ID,
                         getSite());
             }
         }
     }
 
     /**
      * Listener to en-/disable actions pertaining to this view.
      * @author BREDEX GmbH
      * @created 02.03.2006
      */
     private final class ActionListener implements ISelectionListener {
 
         /**
          * en-/disable cut-action
          * @param selList actual selection list
          */
         private void enableCutAction(INodePO[] selList) {
             m_cutTreeItemAction.setEnabled(false);
             for (INodePO guiNode : selList) {
                 if (!(guiNode instanceof ICategoryPO 
                         || guiNode instanceof ISpecTestCasePO)
                         || !NodeBP.isEditable(guiNode)) {
                     
                     m_cutTreeItemAction.setEnabled(false);
                     return;
                 }
             }
             m_cutTreeItemAction.setEnabled(true);
         }
 
         /**
          * en-/disable cut-action
          * @param selList actual selection list
          */
         private void enablePasteAction(INodePO[] selList) {
             m_pasteTreeItemAction.setEnabled(false);
             Object cbContents = getClipboard().getContents(
                     LocalSelectionClipboardTransfer.getInstance());
             for (INodePO guiNode : selList) {
                 if (!(guiNode instanceof ICategoryPO 
                         || guiNode instanceof ISpecTestCasePO
                         || guiNode instanceof IProjectPO)
                         || !NodeBP.isEditable(guiNode)
                         || !(cbContents instanceof IStructuredSelection)
                         || !TCBrowserDndSupport.canMove(
                                 (IStructuredSelection)cbContents, guiNode)) {
                     
                     m_pasteTreeItemAction.setEnabled(false);
                     return;
                 }
             }
             m_pasteTreeItemAction.setEnabled(true);
         }
 
         /**
          * en-/disable move-action
          * @param selList actual selection list
          */
         private void enableMoveAction(INodePO[] selList) {
             m_moveTestCaseAction.setEnabled(false);
             for (INodePO guiNode : selList) {
                 if (!(guiNode instanceof ICategoryPO 
                         || guiNode instanceof ISpecTestCasePO)
                         || !NodeBP.isEditable(guiNode)) {
                     
                     m_moveTestCaseAction.setEnabled(false);
                     return;
                 }
             }
             m_moveTestCaseAction.setEnabled(true);
         }
 
         /**
          * {@inheritDoc}
          */
         public void selectionChanged(IWorkbenchPart part, 
             ISelection selection) {
             
             if (!(selection instanceof IStructuredSelection)) { 
                 // e.g. in Jubula plugin-version you can open an java editor, 
                 // that reacts on org.eclipse.jface.text.TextSelection, which
                 // is not a StructuredSelection
                 return;
             }
             boolean isThisPart = (part == TestCaseBrowser.this);
             final boolean isNullProject = (GeneralStorage.getInstance()
                 .getProject() == null);
             if (isNullProject || (selection == null || selection.isEmpty())) {
                 m_cutTreeItemAction.setEnabled(false);
                 m_pasteTreeItemAction.setEnabled(false);
                 m_moveTestCaseAction.setEnabled(false);
                 return;
             }
             
             if (!isThisPart) {
                 m_moveTestCaseAction.setEnabled(false);
             } else {
                 IStructuredSelection sel = (IStructuredSelection)selection;
                 Object[] selectedElements = sel.toArray();
                 INodePO[] selectedNodes = new INodePO[selectedElements.length];
                 for (int i = 0; i < selectedElements.length; i++) {
                     if (selectedElements[i] instanceof INodePO) {
                         selectedNodes[i] = (INodePO)selectedElements[i];
                     } else {
                         m_cutTreeItemAction.setEnabled(false);
                         m_pasteTreeItemAction.setEnabled(false);
                         m_moveTestCaseAction.setEnabled(false);
                         return;
                     }
                 }
                 
                 enableCutAction(selectedNodes);
                 enablePasteAction(selectedNodes);
                 enableMoveAction(selectedNodes);
             }
         }
     }
     
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         for (DataChangedEvent e : events) {
             handleDataChanged(e.getPo(), e.getDataState(),
                     e.getUpdateState());
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void handleDataChanged(final IPersistentObject po, 
         final DataState dataState, final UpdateState updateState) {
         
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 // changes on the aut do not affect this view
                 if ((po instanceof IAUTMainPO) 
                         || (po instanceof ITestSuitePO)) {
                     return;
                 }
                 if (updateState == UpdateState.onlyInEditor) {
                     return;
                 }
                 if (po instanceof IReusedProjectPO) {
                     // For right now, refresh the entire tree
                     handleProjectLoaded();
                     return;
                 }
                 switch (dataState) {
                     case Added:
                         handleDataAdded(po, new NullProgressMonitor());
                         break;
                     case Deleted:
                         handleDataDeleted(po);
                         break;
                     case Renamed:
                         handleDataRenamed(po);
                         break;
                     case StructureModified:
                         handleDataStructureModified(po);
                         break;
                     default:
                         break;
                 }    
             }
         });
     }
 
     /**
      * @param po The persistent object for which the structure has changed
      */
     private void handleDataStructureModified(final IPersistentObject po) {
         
         if (po instanceof INodePO) {  
             getTreeViewer().getTree().getParent().setRedraw(false);
             // retrieve tree state
             Object[] expandedElements = getTreeViewer().getExpandedElements();
             ISelection selection = getTreeViewer().getSelection();
 
             // update elements
             if (po instanceof IProjectPO) {
                 rebuildTree();
             }
 
             // refresh treeview
             getTreeViewer().refresh();
 
             // restore tree state
             getTreeViewer().setExpandedElements(expandedElements);
             getTreeViewer().setSelection(selection);
             getTreeViewer().getTree().getParent().setRedraw(true);
         }
     }
 
     /**
      * @param po The persistent object that was renamed
      */
     private void handleDataRenamed(final IPersistentObject po) {
         if ((po instanceof ISpecTestCasePO || po instanceof ICategoryPO 
                 || po instanceof IExecTestCasePO)) {
   
             getTreeViewer().refresh(true);
         }
     }
 
     /**
      * @param po The persistent object that was deleted
      */
     private void handleDataDeleted(final IPersistentObject po) {
         if (po instanceof ISpecTestCasePO
             || po instanceof ICategoryPO) {
             if (getTreeViewer() != null) {
                 Plugin.getDisplay().syncExec(new Runnable() {
                     public void run() {
                         getTreeViewer().refresh();
                     }
                 });
             }
         } else if (po instanceof IProjectPO) {
             Plugin.getDisplay().syncExec(new Runnable() {
                 public void run() {
                     getTreeViewer().setInput(null);
                     getTreeViewer().refresh();
                 }
             });
         }
     }
 
     /**
      * @param po The persistent object that was added
      * @param monitor The progress monitor for this potentially long-running 
      *                operation.
      */
     private void handleDataAdded(final IPersistentObject po, 
             IProgressMonitor monitor) {
         if (po instanceof ISpecTestCasePO
                 || po instanceof ICategoryPO) {
             getTreeViewer().refresh();
             getTreeViewer().setSelection(
                 new StructuredSelection(po), true);
         } else if (po instanceof IProjectPO) {
             handleProjectLoaded();
         }
     }

    /** {@inheritDoc} */
    public void problemPropagationFinished() {
        getTreeViewer().getTree().getDisplay().syncExec(new Runnable() {
            public void run() {
                getTreeViewer().refresh();
            }
        });
    }
 }
