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
 package org.eclipse.jubula.client.ui.editors;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.businessprocess.GuiNodeBP;
 import org.eclipse.jubula.client.ui.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.controllers.dnd.LocalSelectionClipboardTransfer;
 import org.eclipse.jubula.client.ui.controllers.dnd.TSEditorDndSupport;
 import org.eclipse.jubula.client.ui.controllers.dnd.TSEditorDropTargetListener;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.contentprovider.TestSuiteEditorContentProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.DisplayableLanguages;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 
 /**
  * Editor for ExecTestCases
  *
  * @author BREDEX GmbH
  * @created 05.09.2005
  */
 public class TestSuiteEditor extends AbstractTestCaseEditor {
 
     /**
      * {@inheritDoc}
      * @param parent
      */
     public void createPartControlImpl(Composite parent) {
         super.createPartControlImpl(parent);
         ActionListener actionListener = new ActionListener();
         getTreeViewer().addSelectionChangedListener(actionListener);
         addTreeListener();
         if (!Plugin.getDefault().anyDirtyStar())  {
             checkAndRemoveUnusedTestData();
         }
     }
 
     /**
      * Adds DoubleClickListener to Treeview.
      */
     private void addTreeListener() {
         getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 CommandHelper.executeCommand(
                         CommandIDs.REFERENCE_TC_COMMAND_ID, getSite());
             }
         });
     }
 
     /**
      * Sets the input of the tree viewer for specificaion.
      */
     public void setInitialInput() {
         getMainTreeViewer().setContentProvider(
                 new TestSuiteEditorContentProvider());  
         ITestSuitePO rootPO = 
             (ITestSuitePO)getEditorHelper().getEditSupport().getWorkVersion();
         
         try {
             getTreeViewer().getTree().setRedraw(false);
             getTreeViewer().setInput(new ITestSuitePO[] {rootPO});
         } finally {
             getTreeViewer().getTree().setRedraw(true);
             getMainTreeViewer().expandAll();
         }
     }
     
     /**
      * Checks, if all fields were filled in correctly.
      * @return True, if all fields were filled in correctly. 
      */
     protected boolean checkCompleteness() {
         ITestSuitePO tsWorkVersion = 
             (ITestSuitePO)getEditorHelper().getEditSupport().getWorkVersion();
         if (!checkWorkingLanguage(tsWorkVersion)) {
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR, null, 
                     new String[]{
                         Messages.TestCaseEditorUnsupportedAUTLanguage});
             return false;
         }
         if (tsWorkVersion.getName() == null
                 || StringConstants.EMPTY.equals(tsWorkVersion.getName())) {
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR, null, 
                     new String[]{Messages.TestCaseEditorNoTsuiteName});
             return false;
         } 
         if (tsWorkVersion.getName().startsWith(BLANK) 
             || tsWorkVersion.getName().endsWith(BLANK)) { 
             
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR, null, 
                     new String[]{Messages.TestCaseEditorWrongTsName});
             return false;
         }
         final IProjectPO project = GeneralStorage.getInstance().
             getProject();
 
         if (!tsWorkVersion.getName().equals(
                 getEditorHelper().getEditSupport().getOriginal().getName())
             && ProjectPM.doesTestSuiteExists(project.getId(), 
                 tsWorkVersion.getName())) {
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR, null,
                 new String[]{Messages.TestCaseEditorDoubleTsuiteName});
             return false;
         }
         if (tsWorkVersion.getStepDelay() == -1) { // empty step delay
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR, null, 
                     new String[]{Messages.TestSuiteEditorEmptyStepDelay});
             return false;
         }
         Iterator iter = tsWorkVersion.getNodeListIterator();
         while (iter.hasNext()) {
             INodePO child = (INodePO)iter.next();
             if (Hibernator.isPoSubclass(child, IExecTestCasePO.class)) {
                 IExecTestCasePO execTC = (IExecTestCasePO)child;
                 if (!checkExecTCCompleteness(execTC)) {
                     
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * @param tsWorkVersion the actual workversion
      * @return true, current working language is supported by the current AUT
      */
     private boolean checkWorkingLanguage(ITestSuitePO tsWorkVersion) {
         Locale workLang = WorkingLanguageBP.getInstance()
             .getWorkingLanguage();
         if (tsWorkVersion.getAut() != null) {
             for (IAUTMainPO aut 
                 : GeneralStorage.getInstance().getProject().getAutMainList()) {
                 
                 if (aut.equals(tsWorkVersion.getAut())) {
                     for (Locale autLang : aut.getLangHelper()
                             .getLanguageList()) {
                         
                         if (workLang.equals(autLang)) {
                             return true;
                         }
                     }
                 }
             }
             return false;
         }
         return true;
     }
 
     /**
      * Checks, if all fields were filled in correctly.
      * @return True, if all fields were filled in correctly.
      * @param testCase the checked testCase.
      */
     private boolean checkExecTCCompleteness(IExecTestCasePO testCase) {
         
         Object[] tcName = new Object[]{testCase.getName()};
         String name = testCase.getName();
         if (name == null || StringConstants.EMPTY.equals(name)) {
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_EX, 
                     tcName, new String[]{Messages.TestCaseEditorNoExecTcName});
             return false;
         } 
         if (testCase.getName().startsWith(BLANK) 
             || testCase.getName().endsWith(BLANK)) { 
             
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_EX, 
                     tcName, new String[]{
                         Messages.TestCaseEditorWrongExecTcName});
             return false;
         }
         for (ICompNamesPairPO compNamesPair : testCase.getCompNamesPairs()) {
             if (compNamesPair.getSecondName().equals(
                 StringConstants.EMPTY)) {
                 
                 Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_EX,
                         tcName, new String[]{
                             NLS.bind(Messages.TestCaseEditorCompNameError,
                                 new Object[]{compNamesPair.getFirstName()}) 
                             + Messages.TestCaseEditorEmptyCompName});
                 return false;
             }
             if (compNamesPair.getSecondName().startsWith(BLANK) 
                 || compNamesPair.getSecondName().endsWith(BLANK)) { 
                 Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_EX,
                         tcName, new String[]{NLS.bind(
                                 Messages.TestCaseEditorCompNameError,
                                 new Object[]{compNamesPair.getFirstName()}) 
                             + Messages.TestCaseEditorWrongCompName});
                 return false;
             }
         }
         return true;
     }
     
     /**
      * {@inheritDoc}
      */
     public String getEditorPrefix() {
         return Messages.PluginTS;
     }
     
     /**
      * {@inheritDoc}
      */
     protected void fillContextMenu(IMenuManager mgr) {
         IStructuredSelection selection = getStructuredSelection();
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         if (selection.getFirstElement() == null) {
             return;
         }
         MenuManager submenuAdd = new MenuManager(Messages.TestSuiteBrowserAdd,
                 ADD_ID);
         MenuManager submenuRefactor = new MenuManager(
             Messages.TestCaseEditorRefactor, REFACTOR_ID);
         MenuManager submenuInsert = new MenuManager(
                 Messages.TestSuiteEditorInsert, INSERT_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REFERENCE_TC_COMMAND_ID);
         mgr.add(submenuAdd);
         mgr.add(submenuInsert);
         mgr.add(submenuRefactor);
         CommandHelper.createContributionPushItem(submenuRefactor,
                 CommandIDs.EXTRACT_TESTCASE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REVERT_CHANGES_COMMAND_ID);
         mgr.add(new Separator());
         mgr.add(getCutTreeItemAction());
         mgr.add(getPasteTreeItemAction());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.TOGGLE_ACTIVE_STATE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_WHERE_USED_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.OPEN_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
     }
     
     /**
      * SelectionListener to en-/disable delete-action
      * 
      * @author BREDEX GmbH
      * @created 02.03.2006
      */
     private class ActionListener implements ISelectionChangedListener {
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("unchecked") 
         public void selectionChanged(SelectionChangedEvent event) {
             if (!(event.getSelection() instanceof IStructuredSelection)) {
                 return;
             }
             IStructuredSelection sel = 
                 (IStructuredSelection)event.getSelection();
 
             if (GeneralStorage.getInstance().getProject() == null
                     || (sel == null 
                             || sel.isEmpty())) {
                 
                 getCutTreeItemAction().setEnabled(false);
                 getPasteTreeItemAction().setEnabled(false);
             } else {
                 List<INodePO> selList = sel.toList();
                 enableCutAction(selList);
                 enablePasteAction(selList);
             }
         }
 
         /**
          * en-/disable cut-action
          * @param selList actual selection 
          */
         private void enableCutAction(List<INodePO> selList) {
             getCutTreeItemAction().setEnabled(true);
 
             for (INodePO node : selList) {
                 if (!(node instanceof IExecTestCasePO
                         || node instanceof ICapPO)) {
                     getCutTreeItemAction().setEnabled(false);
                     return;
                 }
             }
         }
 
         /**
          * en-/disable paste-action
          * @param selList actual selection 
          */
         private void enablePasteAction(List<INodePO> selList) {
             
             getPasteTreeItemAction().setEnabled(true);
             LocalSelectionClipboardTransfer transfer = 
                 LocalSelectionClipboardTransfer.getInstance();
             Object cbContents = 
                 getEditorHelper().getClipboard().getContents(transfer);
 
             if (cbContents instanceof IStructuredSelection) {
                 IStructuredSelection cbSelection = 
                     (IStructuredSelection)cbContents;
                 for (INodePO guiNode : selList) {
                     if (guiNode == null
                             || !(cbContents instanceof StructuredSelection)
                             || !TSEditorDndSupport.validateDrop(
                                     transfer.getSource(), 
                                     TestSuiteEditor.this.getTreeViewer(),
                                     cbSelection, guiNode, false)) {
                         
                         getPasteTreeItemAction().setEnabled(false);
                         return;
                     }
                 }
             } else {
                 getPasteTreeItemAction().setEnabled(false);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Class adapter) {
         if (adapter == DisplayableLanguages.class) {
             return getDisplayableLanguages();
         }
         return super.getAdapter(adapter);
     }
 
     /**
      * @return the displayable Languages
      */
     private DisplayableLanguages getDisplayableLanguages() {
        if (getTreeViewer().getSelection() instanceof IStructuredSelection) {
            Object firstSelectedElement = 
                ((IStructuredSelection)getTreeViewer().getSelection())
                     .getFirstElement();
             if (firstSelectedElement instanceof INodePO) {
                 ITestSuitePO testSuite = 
                     GuiNodeBP.getTestSuiteOfNode((INodePO)firstSelectedElement);
                List<Locale> langList = 
                    WorkingLanguageBP.getInstance()
                        .getLanguages(testSuite.getAut());
                if (langList.size() > 0) {
                     return new DisplayableLanguages(langList);
                 }
                langList = new ArrayList<Locale>(1);  
                langList.add(GeneralStorage.getInstance().getProject()
                        .getDefaultLanguage());
                return new DisplayableLanguages(langList);
             }
         }
         return new DisplayableLanguages(new ArrayList<Locale>());
     }
     
     /**
      * {@inheritDoc}
      */
     public Image getDisabledTitleImage() {
         return IconConstants.DISABLED_TS_EDITOR_IMAGE;
     }
     
     /**
      * Sets the help to the HelpSystem.
      * @param parent the parent composite to set the help id to
      */
     protected void setHelp(Composite parent) {
         Plugin.getHelpSystem().setHelp(parent, 
             ContextHelpIds.TEST_SUITE_EDITOR);     
     }
     
     /**
      * {@inheritDoc}
      */
     protected DropTargetListener getViewerDropAdapter() {
         return new TSEditorDropTargetListener(this);
     }
 }
