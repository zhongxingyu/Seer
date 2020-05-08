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
 
 import java.util.Locale;
 
 import org.eclipse.jface.action.GroupMarker;
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
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ICompletenessCheckListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ILanguageChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IExecObjContPO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingPO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.actions.SearchTreeAction;
 import org.eclipse.jubula.client.ui.rcp.constants.RCPCommandIDs;
 import org.eclipse.jubula.client.ui.rcp.controllers.JubulaStateController;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TestExecDropTargetListener;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TreeViewerContainerDragSourceListener;
 import org.eclipse.jubula.client.ui.rcp.editors.TestJobEditor;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.DecoratingCellLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.TestSuiteBrowserContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.TestSuiteBrowserLabelProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IDecoratorManager;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 05.07.2004
  */
 @SuppressWarnings("synthetic-access")
 public class TestSuiteBrowser extends AbstractJBTreeView implements
     ITreeViewerContainer, IJBPart,
     ILanguageChangedListener, ICompletenessCheckListener {
 
     /** Identifies the workbench plug-in */
     public static final String OPEN_WITH_ID = PlatformUI.PLUGIN_ID + ".OpenWithSubMenu"; //$NON-NLS-1$
     /** New-menu */
     public static final String NEW_ID = PlatformUI.PLUGIN_ID + ".NewSubMenu"; //$NON-NLS-1$  
     /** Add-Submenu ID */
     public static final String ADD_ID = PlatformUI.PLUGIN_ID + ".AddSubMenu"; //$NON-NLS-1$
     /** standard logging */
     static final Logger LOG = LoggerFactory.getLogger(TestSuiteBrowser.class);
     /** flag for initialization state of context menu */
     private boolean m_isContextMenuInitialized = false;
     /** menu manager for context menu */
     private final MenuManager m_mgr = new MenuManager();
     /** menu listener for <code>m_menuMgr</code> */
     private MenuListener m_menuListener = new MenuListener();
 
     /**
      * Creates the SWT controls for this workbench part.
      * @param parent Composite
      */
     public void createPartControl(Composite parent) {
         super.createPartControl(parent);
         ColumnViewerToolTipSupport.enableFor(getTreeViewer());
         getTreeViewer().setContentProvider(
                 new TestSuiteBrowserContentProvider());
         DecoratingCellLabelProvider lp = new DecoratingCellLabelProvider(
                 new TestSuiteBrowserLabelProvider(), Plugin.getDefault()
                         .getWorkbench().getDecoratorManager()
                         .getLabelDecorator());
 
         getTreeViewer().setLabelProvider(lp);
         getTreeViewer().setAutoExpandLevel(DEFAULT_EXPANSION);
         
         setViewerInput();
         Plugin.getHelpSystem().setHelp(getTreeViewer().getControl(),
                 ContextHelpIds.TEST_SUITE_VIEW);
         JubulaStateController.getInstance()
             .addSelectionListenerToSelectionService();
         
         int ops = DND.DROP_MOVE;
         Transfer[] transfers = new Transfer[] {LocalSelectionTransfer
             .getInstance()};
         getTreeViewer().addDragSupport(ops, transfers,
             new TreeViewerContainerDragSourceListener(getTreeViewer()));
         getTreeViewer().addDropSupport(ops, transfers,
             new TestExecDropTargetListener(this));
         
         m_mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         Menu menu = m_mgr.createContextMenu(getTreeViewer().getControl());
         getTreeViewer().getControl().setMenu(menu);
         getViewSite().registerContextMenu(m_mgr, getTreeViewer());
         m_mgr.addMenuListener(m_menuListener);
         // Register menu for extension.
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addLanguageChangedListener(this, true);
         ded.addCompletenessCheckListener(this);
         if (GeneralStorage.getInstance().getProject() != null) {
             handleProjectLoaded();
         }
     }
 
     /**
      * Adds DoubleClickListener to Treeview.
      */
     protected void addTreeListener() {
         getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 IStructuredSelection selection = getSuiteTreeSelection();
                 Object firstElement = selection.getFirstElement();
                 if (firstElement instanceof ITestSuitePO) {
                     runCommand(RCPCommandIDs.OPEN_TESTSUITE_EDITOR_COMMAND_ID);
                 } else if (firstElement instanceof ITestJobPO) {
                     runCommand(RCPCommandIDs.OPEN_TESTJOB_EDITOR_COMMAND_ID);
                 } else if (firstElement instanceof IExecObjContPO) {
                     runCommand(RCPCommandIDs.NEW_TESTSUITE_COMMAND_ID);
                 } else if (firstElement instanceof ICategoryPO) {
                     runCommand(RCPCommandIDs.NEW_TESTSUITE_COMMAND_ID);
                 }
             }
             
             /**
              * runs the given command
              * 
              * @param commandID the commandId to execute
              */
             private void runCommand(String commandID) {
                 CommandHelper.executeCommand(commandID, getSite());
             }
         });
     }
 
     /**
      * Create context menu.
      * @param mgr current menu manager
      */
     private void createContextMenu(IMenuManager mgr) {
         if (!m_isContextMenuInitialized) {
             MenuManager submenuNew = new MenuManager(
                     Messages.TestSuiteBrowserNew, NEW_ID);
             MenuManager submenuOpenWith = new MenuManager(
                     Messages.TestSuiteBrowserOpenWith, OPEN_WITH_ID);
             CommandHelper.createContributionPushItem(submenuNew,
                     RCPCommandIDs.NEW_TESTSUITE_COMMAND_ID);
             CommandHelper.createContributionPushItem(submenuNew,
                     RCPCommandIDs.NEW_TESTJOB_COMMAND_ID);
             CommandHelper.createContributionPushItem(submenuNew,
                     RCPCommandIDs.NEW_CATEGORY_COMMAND_ID);
             CommandHelper.createContributionPushItem(submenuOpenWith,
                     RCPCommandIDs.OPEN_TESTJOB_EDITOR_COMMAND_ID);
             CommandHelper.createContributionPushItem(submenuOpenWith,
                     RCPCommandIDs.OPEN_TESTSUITE_EDITOR_COMMAND_ID);
             CommandHelper.createContributionPushItem(submenuOpenWith,
                     RCPCommandIDs.OPEN_OBJECTMAPPING_EDITOR_COMMAND_ID);
             CommandHelper.createContributionPushItem(submenuOpenWith,
                     RCPCommandIDs.OPEN_CENTRAL_TESTDATA_EDITOR_COMMAND_ID);
             mgr.add(submenuNew);
             mgr.add(new Separator());
             CommandHelper.createContributionPushItem(mgr,
                     RCPCommandIDs.RENAME_COMMAND_ID);
             mgr.add(SearchTreeAction.getAction());
             CommandHelper.createContributionPushItem(mgr,
                     CommandIDs.DELETE_COMMAND_ID);
             CommandHelper.createContributionPushItem(mgr,
                     CommandIDs.OPEN_SPECIFICATION_COMMAND_ID);
             CommandHelper.createContributionPushItem(mgr,
                     CommandIDs.SHOW_SPECIFICATION_COMMAND_ID);
             CommandHelper.createContributionPushItem(mgr,
                     CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
             mgr.add(new Separator());
             CommandHelper.createContributionPushItem(mgr,
                     RCPCommandIDs.COPY_ID_COMMAND_ID);
             mgr.add(new Separator());
             CommandHelper.createContributionPushItem(mgr,
                     CommandIDs.REFRESH_COMMAND_ID);
             mgr.add(new Separator());
             mgr.add(submenuOpenWith);
             mgr.add(new Separator());
             CommandHelper.createContributionPushItem(mgr,
                     RCPCommandIDs.PROJECT_PROPERTIES_COMMAND_ID);
             m_isContextMenuInitialized = true;
         }
     }
 
 
     /**
      * Fills the context menu, if there is any selection in this view. 
      * @param mgr IMenuManager
      */
     protected void fillContextMenu(IMenuManager mgr) {
         if (!m_isContextMenuInitialized) {
             createContextMenu(mgr);
         }
     }
 
     /**
      * @return the selected tree item
      */
     public IStructuredSelection getSuiteTreeSelection() {
         return (getTreeViewer().getSelection() instanceof IStructuredSelection)
             ? (IStructuredSelection)getTreeViewer().getSelection()
                     : StructuredSelection.EMPTY;
     }
 
     /**
      * Asks this part to take focus within the workbench.
      */
     public void setFocus() {
         getTreeViewer().getControl().setFocus();
         Plugin.showStatusLine(this);
     }
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         try {
             JubulaStateController.getInstance()
                 .removeSelectionListenerFromSelectionService();
         } finally {
             m_mgr.removeMenuListener(m_menuListener);
             DataEventDispatcher ded = DataEventDispatcher.getInstance();
             ded.removeDataChangedListener(this);
             ded.removeLanguageChangedListener(this);
             ded.removeCompletenessCheckListener(this);
             super.dispose();
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
         public void menuAboutToShow(IMenuManager imgr) {
             fillContextMenu(imgr);
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     protected void rebuildTree() {
         setViewerInput();
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
                 // due to checkstyle BUG this indirection is necessary
                 handleDataChangedImpl(po, dataState, updateState);
             }
         });
     }
 
     /**
      * @param po
      *            the PO
      * @param dataState
      *            the data state
      * @param updateState
      *            the update state
      */
     private void handleDataChangedImpl(final IPersistentObject po,
             final DataState dataState, final UpdateState updateState) {
         // changes on the aut do not affect structure of this view
         if (po instanceof IAUTMainPO) {
             getTreeViewer().refresh();
             return;
         }
         if (updateState == UpdateState.onlyInEditor) {
             return;
         }
 
         switch (dataState) {
             case Added:
                 handleDataAdded(po);
                 break;
             case Deleted:
                 handleDataDeleted(po);
                 break;
             case Renamed:
                 if (po instanceof IProjectPO 
                         || po instanceof ITestSuitePO
                         || po instanceof ITestJobPO 
                        || po instanceof ITestCasePO
                        || po instanceof ICategoryPO) {
                     
                     getTreeViewer().refresh();
                 }
                 break;
             case StructureModified:
                 if (po instanceof IProjectPO) {
                     handleProjectLoaded();
                 }
                 if ((po instanceof ISpecTestCasePO)
                         || (po instanceof ITestSuitePO)
                         || (po instanceof ITestJobPO)) {
 
                     // retrieve tree state
                     Object[] expandedElements = 
                         getTreeViewer().getExpandedElements();
                     ISelection selection = getTreeViewer().getSelection();
 
                     // refresh treeview
                     getTreeViewer().refresh();
 
                     // restore tree status
                     getTreeViewer().setExpandedElements(expandedElements);
                     getTreeViewer().setSelection(selection);
                 }
                 if (po instanceof IObjectMappingPO) {
                     getTreeViewer().refresh();
                 }
                 break;
             default:
                 break;
         }
     }
     
     /**
      * @param po The persistent object that was deleted
      */
     private void handleDataDeleted(final IPersistentObject po) {
         
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 if (po instanceof ITestSuitePO
                         || po instanceof ITestJobPO
                         || po instanceof ICategoryPO) {
 
                     getTreeViewer().refresh();
 
                 } else if (po instanceof IProjectPO) {
                     setViewerInput();
                     getTreeViewer().refresh();
                 }
             }
         });
     }
 
     /**
      * @param po
      *            The persistent object that was added
      */
     private void handleDataAdded(IPersistentObject po) {
         if (po instanceof ISpecTestCasePO) {
             return;
         }
         getTreeViewer().refresh();
         getTreeViewer().expandToLevel(getTreeViewer().getAutoExpandLevel());
         getTreeViewer().setSelection(new StructuredSelection(po), true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleLanguageChanged(Locale locale) {
         getTreeViewer().refresh();
     }
 
     /**
      * {@inheritDoc}
      */
     public void completenessCheckFinished() {
         getTreeViewer().getTree().getDisplay().syncExec(new Runnable() {
             public void run() {
                 getTreeViewer().refresh();
                 IDecoratorManager dm = 
                     Plugin.getDefault().getWorkbench().getDecoratorManager();
                 dm.update(Constants.CC_DECORATOR_ID);
             }
         });
     }
 
     /**
      * Adds the given test suite to the selected test job as a reference. This
      * method is only allowed from an editor context since it relies on the
      * Session provided by the editors EditSupport.
      * @param ts the test suite to reference.
      * @param tj the target TestJob
      * @param position the position to insert. If null, the position is 
      * @return the referenced test suite.
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     public INodePO addReferencedTestSuite(ITestSuitePO ts, INodePO tj,
             int position) throws PMReadException, PMAlreadyLockedException,
             PMDirtyVersionException, PMException {
         IRefTestSuitePO exTcGUI = null;
         ITestSuitePO workTs = null;
         workTs = createWorkVersionofTs(ts);
         if (workTs != null) {
             IRefTestSuitePO refTs = TestSuiteBP.addReferencedTestSuite(
                     getEditSupport(), tj, workTs, position);
             DataEventDispatcher.getInstance().fireDataChangedListener(refTs,
                     DataState.Added, UpdateState.onlyInEditor);
         }
         return exTcGUI;
     }
     
     /**
      * @return the EditSupport for the current active editor. This methods
      * throws a GDFatalExecption if called with no IGDEditor subclass active.
      */
     private EditSupport getEditSupport() {
         TestJobEditor edit = getTJEditor();            
         EditSupport editSupport = edit.getEditorHelper().getEditSupport();
         return editSupport;
     }
     
     /**
      * @return the actual active TJ editor
      */
     private TestJobEditor getTJEditor() {
         TestJobEditor edit = Plugin.getDefault().getActiveTJEditor();
         if (edit == null) {
             String msg = Messages.NoActiveTCEditorPleaseFixTheMethod;
             LOG.error(msg); 
             throw new JBFatalException(msg, MessageIDs.E_NO_OPENED_EDITOR);
         }
         return edit;
     }
     
     /**
      * get the WorkVerstion to origTs
      * @param origTs original specTc
      * @return workorigTs or null
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     private ITestSuitePO createWorkVersionofTs(
             ITestSuitePO origTs) throws PMReadException, 
             PMAlreadyLockedException, PMDirtyVersionException, PMException {
         ITestSuitePO workTs = null;
         EditSupport editSupport = getEditSupport();
         workTs = (ITestSuitePO)editSupport.createWorkVersion(origTs);
         return workTs;
     }    
 
     /**
      * Sets the input for the tree viewer.
      */
     private void setViewerInput() {
         IProjectPO activeProject = GeneralStorage.getInstance().getProject();
         if (activeProject != null) {
             getTreeViewer().setInput(
                     new IExecObjContPO[] {activeProject.getExecObjCont()});
         } else {
             getTreeViewer().setInput(null);
         }
     }
 }
