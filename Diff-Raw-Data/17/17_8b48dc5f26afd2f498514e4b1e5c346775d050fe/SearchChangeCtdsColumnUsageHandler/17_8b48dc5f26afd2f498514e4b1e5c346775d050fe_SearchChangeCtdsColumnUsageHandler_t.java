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
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.handlers.AbstractSelectionBasedHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.GeneralLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.search.result.BasicSearchResult.SearchResultElement;
 import org.eclipse.jubula.client.ui.rcp.wizards.refactor.param.ChangeCtdsColumnUsageWizard;
 import org.eclipse.jubula.client.ui.rcp.wizards.refactor.param.ExistingAndNewParameterData;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * Handler for context menu in a search result list to change CTDS column usage.
  * @author BREDEX GmbH
  */
 public class SearchChangeCtdsColumnUsageHandler
         extends AbstractSelectionBasedHandler {
 
     /**
      * {@inheritDoc}
      */
     protected Object executeImpl(ExecutionEvent event) {
         List selectionList = getSelection().toList();
         Set<ITestCasePO> testCaseSet = new LinkedHashSet<ITestCasePO>();
         EntityManager session = GeneralStorage.getInstance().getMasterSession();
         IParameterInterfacePO dataCube = null;
         for (Object element : selectionList) {
             @SuppressWarnings("unchecked")
             SearchResultElement<Long> searchResult =
                     (SearchResultElement<Long>) element;
             INodePO nodePO = session.find(NodeMaker.getNodePOClass(),
                     searchResult.getData());
             ITestCasePO testCase = (ITestCasePO) nodePO;
             IParameterInterfacePO currentDataCube =
                     testCase.getReferencedDataCube();
             if (testCase instanceof IExecTestCasePO) {
                 IExecTestCasePO exec = (IExecTestCasePO) testCase;
                 if (exec.getSpecTestCase().getReferencedDataCube() != null
                         && exec.getReferencedDataCube()
                             != exec.getSpecTestCase().getReferencedDataCube()) {
                    String execName = GeneralLabelProvider.getText(exec);
                     ErrorHandlingUtil.createMessageDialog(
                             MessageIDs.I_TC_WITH_TWO_CTDS,
                             new String[] {execName}, null);
                     return null;
                 }
             }
             if (dataCube == null && currentDataCube != null) {
                 dataCube = currentDataCube;
             } else if (currentDataCube == null
                     || !dataCube.equals(currentDataCube)) {
                 ErrorHandlingUtil.createMessageDialog(
                         MessageIDs.I_ALL_TCS_MUST_USE_SAME_CTDS);
                 return null;
             }
             testCaseSet.add(testCase);
         }
         // create for each parameter name a corresponding set of execution Test Cases
         ExistingAndNewParameterData paramData =
                 new ExistingAndNewParameterData(testCaseSet);
         if (paramData.getAllParamDescriptions().length == 0) {
             ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.I_TCS_HAVE_NO_CHANGEABLE_PARAMETER_NAME);
             return null;
         }
         if (Plugin.getDefault().saveAllDirtyEditors()) {
             showWizardDialog(paramData);
         }
         return null;
     }
 
     /**
      * Show the dialog for changing CTDS column usage.
      * @param paramData The parameter names of the selected Test Cases.
      */
     private void showWizardDialog(
             ExistingAndNewParameterData paramData) {
         WizardDialog dialog;
         dialog = new WizardDialog(getActiveShell(),
                 new ChangeCtdsColumnUsageWizard(paramData));
         dialog.setHelpAvailable(true); // show ? icon on left bottom
         dialog.open();
     }
 
 }
