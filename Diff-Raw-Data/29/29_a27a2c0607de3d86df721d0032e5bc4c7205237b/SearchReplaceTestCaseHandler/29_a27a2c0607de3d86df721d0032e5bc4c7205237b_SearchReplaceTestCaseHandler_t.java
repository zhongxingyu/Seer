 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.handlers;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.handlers.AbstractSelectionBasedHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.search.SearchResultPage;
 import org.eclipse.jubula.client.ui.rcp.search.result.BasicSearchResult.SearchResultElement;
 import org.eclipse.jubula.client.ui.rcp.utils.SearchPageUtils;
 import org.eclipse.jubula.client.ui.rcp.wizards.search.refactor.SearchReplaceTCRWizard;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * 
  * @author BREDEX GmbH
  *
  */
 public class SearchReplaceTestCaseHandler extends
         AbstractSelectionBasedHandler {
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     protected Object executeImpl(ExecutionEvent event) {
        if (!Plugin.getDefault().saveAllDirtyEditors()) {
            return null;
        }
         IStructuredSelection selectedObject = getSelection();
         List<SearchResultElement<Long>> selectionList =
                 (List<SearchResultElement<Long>>) selectedObject.toList();
         Set<IExecTestCasePO> execList = new LinkedHashSet<IExecTestCasePO>();
         EntityManager session = GeneralStorage.getInstance().getMasterSession();
         ISpecTestCasePO firstSpec = null;
         
         IProjectPO project = GeneralStorage.getInstance().getProject();
         boolean error = false;
         try {
 
             for (Iterator iterator = selectionList.iterator(); iterator
                     .hasNext();) {
                 Object object = (Object) iterator.next();
                 SearchResultElement<Long> searchResult = 
                         (SearchResultElement<Long>) object;
                 INodePO nodePO = session.find(NodeMaker.getNodePOClass(),
                         searchResult.getData());
                 if (nodePO instanceof IExecTestCasePO) {
                     IExecTestCasePO exec = (IExecTestCasePO) nodePO;
                     if (exec.getParentProjectId().equals(project.getId())) {
                         // This only adds execs if they are not from reused
                         // projects
                         execList.add(exec);
                     } else {
                         error = true;
                     }
                     ISpecTestCasePO spec = exec.getSpecTestCase();
                     if (firstSpec == null) {
                         firstSpec = spec;
                     }
                     if (!firstSpec.equals(spec)) {
                         ErrorHandlingUtil
                                 .createMessageDialog(
                                         MessageIDs.I_NOT_SAME_SPEC);
                         return null;
                     }
                 } else {
                     error = true;
                 }
             }
         } catch (Exception e) {
             ErrorHandlingUtil.createMessageDialog(MessageIDs.I_NO_EXEC);
             return null;
         }
         if (error) {
             if (MessageDialog.openQuestion(null,
                     Messages.ReplaceMultiTCRWizardActionDialog,
                     Messages.ReplaceMultiTCRWizardQuestionDeselect)) {
                 SearchResultPage page = SearchPageUtils
                         .getSearchResultPage(event);
                 List<ITestCasePO> bla = new LinkedList<ITestCasePO>();
                 bla.addAll(execList);
                 SearchPageUtils.selectTestCases(page,
                         selectionList, bla);
             }
             return null;
         }
        showWizardDialog(execList);
         return null;
     }
 
     /**
      * 
      * @param execList
      *            Set of all exec testcases which should be replaced
      */
     private void showWizardDialog(Set<IExecTestCasePO> execList) {
         WizardDialog dialog; 
         dialog = new WizardDialog(getActiveShell(), 
                 new SearchReplaceTCRWizard(execList)) {
             /** {@inheritDoc} */
             protected void configureShell(Shell newShell) {
                 super.configureShell(newShell);
             }
         };
         dialog.setMinimumPageSize(775, 300);
         dialog.setPageSize(775, 300);
         dialog.setHelpAvailable(true); // show ? icon on left bottom
         dialog.open();
     }
 
 }
