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
 package org.eclipse.jubula.client.ui.views;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
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
 import org.eclipse.jubula.client.core.businessprocess.db.TestCaseBP;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.actions.CutTreeItemActionTCBrowser;
 import org.eclipse.jubula.client.ui.actions.MoveTestCaseAction;
 import org.eclipse.jubula.client.ui.actions.NewTestCaseActionTCBrowser;
 import org.eclipse.jubula.client.ui.actions.PasteTreeItemActionTCBrowser;
 import org.eclipse.jubula.client.ui.actions.SearchTreeAction;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.controllers.JubulaStateController;
 import org.eclipse.jubula.client.ui.controllers.dnd.LocalSelectionClipboardTransfer;
 import org.eclipse.jubula.client.ui.controllers.dnd.LocalSelectionTransfer;
 import org.eclipse.jubula.client.ui.controllers.dnd.TCBrowserDndSupport;
 import org.eclipse.jubula.client.ui.controllers.dnd.TestSpecDropTargetListener;
 import org.eclipse.jubula.client.ui.controllers.dnd.TreeViewerContainerDragSourceListener;
 import org.eclipse.jubula.client.ui.editors.AbstractTestCaseEditor;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.DecoratingCellLabelProvider;
 import org.eclipse.jubula.client.ui.provider.contentprovider.TestCaseBrowserContentProvider;
 import org.eclipse.jubula.client.ui.provider.labelprovider.TestCaseBrowserLabelProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.DisplayableLanguages;
 import org.eclipse.jubula.client.ui.utils.SelectionChecker;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 
 
 /** 
  * @author BREDEX GmbH
  * @created 05.07.2004
  */
 public class TestCaseBrowser extends AbstractJBTreeView 
     implements ITreeViewerContainer, IJBPart {
     /** New-menu ID */
     public static final String NEW_ID = PlatformUI.PLUGIN_ID + ".NewSubMenu"; //$NON-NLS-1$
     /** Identifies the workbench plug-in */
     public static final String OPEN_WITH_ID = PlatformUI.PLUGIN_ID + ".OpenWithSubMenu"; //$NON-NLS-1$
     /** the newCAP menu ID */
     public static final String NEW_CAP_ID = PlatformUI.PLUGIN_ID + ".NewCAPMenu"; //$NON-NLS-1$    
     /** Add-Submenu ID */
     public static final String ADD_ID = PlatformUI.PLUGIN_ID + ".AddSubMenu"; //$NON-NLS-1$
     /** postfix for add-action id */
     private static final String ADD = "_ADD"; //$NON-NLS-1$
     /** standard logging */
     private static final Log LOG = LogFactory.getLog(TestCaseBrowser.class);
        
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
     /** action to add new test cases */
     private NewTestCaseActionTCBrowser m_newTestCaseAction;
     /** menu manager for context menu */
     private final MenuManager m_menuMgr = new MenuManager();
     /** menu listener for <code>m_menuMgr</code> */
     private MenuListener m_menuListener = new MenuListener();
     
     /**
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         super.createPartControl(parent);
         ColumnViewerToolTipSupport.enableFor(getTreeViewer());
         getTreeViewer().setContentProvider(
                 new TestCaseBrowserContentProvider());
         getTreeViewer().setLabelProvider(new DecoratingCellLabelProvider(
             new TestCaseBrowserLabelProvider(), Plugin.getDefault()
                     .getWorkbench().getDecoratorManager().getLabelDecorator()));
         m_cutTreeItemAction = new CutTreeItemActionTCBrowser();
         m_pasteTreeItemAction = new PasteTreeItemActionTCBrowser();
         m_moveTestCaseAction = new MoveTestCaseAction();
         m_newTestCaseAction = new NewTestCaseActionTCBrowser();
 
         int ops = DND.DROP_MOVE;
         Transfer[] transfers = new Transfer[] {LocalSelectionTransfer
             .getInstance()};
         getTreeViewer().addDragSupport(ops, transfers,
             new TreeViewerContainerDragSourceListener(getTreeViewer()));
         getTreeViewer().addDropSupport(ops, transfers,
             new TestSpecDropTargetListener(this));
         
         createContextMenu(); 
         JubulaStateController.getInstance().
             addSelectionListenerToSelectionService();
         Plugin.getHelpSystem().setHelp(getTreeViewer().getControl(),
             ContextHelpIds.TEST_SPEC_VIEW);
         
         configureActionBars();
         
         if (GeneralStorage.getInstance().getProject() != null) {
             handleProjectLoaded();
         }
         
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
         getViewSite().getActionBars().setGlobalActionHandler(
                 Constants.NEW_TC_ACTION_ID + ADD, m_newTestCaseAction);
         getViewSite().getWorkbenchWindow().getSelectionService()
             .addSelectionListener(m_actionListener);
         getViewSite().getActionBars().updateActionBars();
     }
 
     /**
      * Create context menu.
      */
     private void createContextMenu() {
         m_menuMgr.setRemoveAllWhenShown(true);
         m_menuMgr.addMenuListener(m_menuListener);
         // Create menu.
         Menu menu = m_menuMgr.createContextMenu(getTreeViewer().getControl());
         getTreeViewer().getControl().setMenu(menu);
         // Register menu for extension.
         getViewSite().registerContextMenu(m_menuMgr, getTreeViewer());
     }
 
     /**
      * Fills the context menu, if there is any selection in this view. 
      * @param mgr IMenuManager
      */
     protected void fillContextMenu(IMenuManager mgr) {
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         MenuManager submenuNew = new MenuManager(
                 Messages.TestSuiteBrowserNew, NEW_ID);
         MenuManager submenuAdd = new MenuManager(
                 Messages.TestSuiteBrowserAdd, ADD_ID);
         MenuManager submenuOpenWith = new MenuManager(
                 Messages.TestSuiteBrowserOpenWith, OPEN_WITH_ID);
         // build menu
         mgr.add(submenuNew);
         mgr.add(submenuAdd);
         submenuNew.add(m_newTestCaseAction);
         CommandHelper.createContributionPushItem(submenuNew,
                 CommandIDs.NEW_CATEGORY_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.RENAME_COMMAND_ID);
         mgr.add(m_moveTestCaseAction);
         mgr.add(SearchTreeAction.getAction());
         mgr.add(m_cutTreeItemAction);
         mgr.add(m_pasteTreeItemAction);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.OPEN_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_WHERE_USED_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.COPY_ID_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(submenuOpenWith,
                 CommandIDs.OPEN_TESTCASE_EDITOR_COMMAND_ID);
         mgr.add(new Separator());
         mgr.add(submenuOpenWith);
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
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         try {
             m_menuMgr.removeMenuListener(m_menuListener);
             JubulaStateController.getInstance()
                 .removeSelectionListenerFromSelectionService();
             DataEventDispatcher.getInstance().removeDataChangedListener(this);
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
             getTreeViewer().setInput(new IProjectPO[] {activeProject});
             getTreeViewer().expandToLevel(DEFAULT_EXPANSION);
         } else {
             getTreeViewer().setInput(null);
         }
     }
     
     /**
      * @author BREDEX GmbH
      * @created Jan 22, 2007
      */
     private final class MenuListener implements IMenuListener {
         /**
          * {@inheritDoc}
          */
         public void menuAboutToShow(IMenuManager mgr) {
             fillContextMenu(mgr);
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
             if (counter[SelectionChecker.PROJECT] 
                         == selection.size() 
                         || counter[SelectionChecker.CATEGORY] 
                                   == selection.size()) { 
                 if (m_newTestCaseAction.isEnabled()) {
                     m_newTestCaseAction.run();
                 }
             } else {
                 CommandHelper.executeCommand(
                         CommandIDs.OPEN_TESTCASE_EDITOR_COMMAND_ID, getSite());
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
         private void enableCutAction(List<INodePO> selList) {
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
         private void enablePasteAction(List<INodePO> selList) {
             m_pasteTreeItemAction.setEnabled(false);
             Object cbContents = getClipboard().getContents(
                     LocalSelectionClipboardTransfer.getInstance());
             for (INodePO guiNode : selList) {
                 if (!(guiNode instanceof ICategoryPO 
                         || guiNode instanceof ISpecTestCasePO)
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
         private void enableMoveAction(List<INodePO> selList) {
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
          * en-/disable new-action
          * @param selList actual selection list
          */
         private void enableNewAction(List<INodePO> selList) {
             if ((selList.size() > 0) && NodeBP.isEditable(selList.get(0))) {
                 m_newTestCaseAction.setEnabled(true);
             } else {
                 m_newTestCaseAction.setEnabled(false);
             }
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
                 m_newTestCaseAction.setEnabled(false);
                 return;
             }
             m_newTestCaseAction.setEnabled(isThisPart);
             
             if (!isThisPart) {
                 m_moveTestCaseAction.setEnabled(false);
             } else {
                 IStructuredSelection sel = (IStructuredSelection)selection;
                 List<INodePO> selList = sel.toList();
                 enableCutAction(selList);
                 enablePasteAction(selList);
                 enableMoveAction(selList);
                 enableNewAction(selList);
                 Object firstElement = sel.getFirstElement();
             }
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
                INodePO root = (INodePO)getTreeViewer().getInput();
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
                        handleDataStructureModified(po, root);
                         break;
                     default:
                         break;
                 }    
             }
         });
     }
 
     /**
      * @param po The persistent object for which the structure has changed
     * @param root The root of the GUI tree
      */
    private void handleDataStructureModified(final IPersistentObject po, 
        INodePO root) {
         
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
   
             getTreeViewer().update(po, null);
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
     
     /**
      * Adds the given test case to the selected test case as a reference. This
      * method is only allowed from an editor context since it relies on the
      * Session provided by the editors EditSupport.
      * @param origSpecTc the test case to reference.
      * @param targetSpecNode the target TestCase or TestSuite.
      * @param position the position to insert. If null, the position is 
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     public void addReferencedTestCase(ISpecTestCasePO origSpecTc, 
         INodePO targetSpecNode, Integer position) 
         throws PMReadException, PMAlreadyLockedException, 
         PMDirtyVersionException, PMException {
         
         ISpecTestCasePO workSpecTc = null;
         workSpecTc = createWorkVersionofSpecTc(origSpecTc);
         if (workSpecTc != null) {
             IExecTestCasePO execTc = 
                 TestCaseBP.addReferencedTestCase(getEditSupport(), 
                     targetSpecNode, workSpecTc, position);
             DataEventDispatcher.getInstance().fireDataChangedListener(
                 execTc, DataState.Added, UpdateState.onlyInEditor); 
         }
     } 
     
     /**
      * get the WorkVerstion to specTc
      * @param origSpecTc original specTc
      * @return workSpecTc or null
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     private ISpecTestCasePO createWorkVersionofSpecTc(
         ISpecTestCasePO origSpecTc) throws PMReadException, 
             PMAlreadyLockedException, PMDirtyVersionException, PMException {
         
         ISpecTestCasePO workSpecTc = null;
         EditSupport editSupport = getEditSupport();
         workSpecTc = (ISpecTestCasePO)editSupport.createWorkVersion(origSpecTc);
         return workSpecTc;
     }
     
     /**
      * @return the EditSupport for the current active editor. This methods
      * throws a GDFatalExecption if called with no IGDEditor subclass active.
      */
     private EditSupport getEditSupport() {
         AbstractTestCaseEditor edit = getTCEditor();            
         EditSupport editSupport = edit.getEditorHelper().getEditSupport();
         return editSupport;
     }
 
     /**
      * @return the actual active TC editor
      */
     private AbstractTestCaseEditor getTCEditor() {
         AbstractTestCaseEditor edit = Plugin.getDefault().getActiveTCEditor();
         if (edit == null) {
             String msg = Messages.NoActiveTCEditorPleaseFixTheMethod;
             LOG.fatal(msg); 
             throw new JBFatalException(msg, MessageIDs.E_NO_OPENED_EDITOR);
         }
         return edit;
     }
     
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Class adapter) {
         if (adapter == DisplayableLanguages.class) {
             IProjectPO currentProject = 
                 GeneralStorage.getInstance().getProject();
             if (currentProject != null) {
                 return new DisplayableLanguages(
                     currentProject.getLangHelper().getLanguageList());
             }
         }
         return super.getAdapter(adapter);
     }
 
 }
