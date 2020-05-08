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
 package org.eclipse.jubula.client.ui.rcp.handlers;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.businessprocess.CapBP;
 import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
 import org.eclipse.jubula.client.core.businessprocess.progress.ProgressMonitorTracker;
 import org.eclipse.jubula.client.core.businessprocess.treeoperations.CollectComponentNameUsersOp;
 import org.eclipse.jubula.client.core.datastructure.CompNameUsageMap;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ProjectState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecObjContPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.MultipleNodePM;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.MultipleTCBTracker;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.dialogs.ReusedProjectSelectionDialog;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.rcp.views.TestCaseBrowser;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.utils.ToolkitUtils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.messagehandling.MessageInfo;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.PlatformUI;
 
 
 /**
  * @author BREDEX GmbH
  * @created Oct 15, 2007
  */
 public class MoveTestCaseHandler extends AbstractHandler {
     /**
      * Represents problems with moving one or more Test Cases.
      *
      * @author BREDEX GmbH
      * @created Oct 17, 2007
      */
     private static class ProblemSet {
         
         /** valid problems */
         private Set<INodePO> m_problems = new HashSet<INodePO>();
         
         /** list of nodes that are being moved */
         private List<INodePO> m_nodesToMove;
         
         /**
          * Constructor
          * 
          * @param nodesToMove The nodes that are to be moved.
          */
         public ProblemSet(List<INodePO> nodesToMove) {
             setNodesToMove(new ArrayList<INodePO>());
             for (INodePO node : nodesToMove) {
                 addCatChildren(node, getNodesToMove());
             }
         }
         
         /**
          * @return List of valid problems.
          */
         public Set<INodePO> getProblems() {
             return m_problems;
         }
 
         /**
          * @return the nodesToMove
          */
         public List<INodePO> getNodesToMove() {
             return m_nodesToMove;
         }
 
         /**
          * @param nodesToMove the nodesToMove to set
          */
         private void setNodesToMove(List<INodePO> nodesToMove) {
             m_nodesToMove = nodesToMove;
         }
     }
     /**
      * @author BREDEX GmbH
      * @created 22.01.2013
      */
     private static class RefreshReusedProjectOperation 
         implements IRunnableWithProgress {
         /** the selected project  */
         private IReusedProjectPO m_selectedProject;
 
         /**
          * 
          * @param reusedProject
          *            the reused project which should be refreshed
          */
         public RefreshReusedProjectOperation(IReusedProjectPO reusedProject) {
             m_selectedProject = reusedProject;
             
         }
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) {
         
         
             monitor.beginTask(
                     Messages.RefreshProjectOperationRefreshing,
                     IProgressMonitor.UNKNOWN);
         
             ProgressMonitorTracker.getInstance().setProgressMonitor(monitor);
             try {
                 IProjectPO referencedProject = ProjectPM
                         .loadReusedProjectInMasterSession(m_selectedProject);
                 GeneralStorage.getInstance().getMasterSession().refresh(
                       referencedProject.getSpecObjCont());
                 DataEventDispatcher.getInstance()
                     .fireDataChangedListener(new DataChangedEvent(
                             referencedProject,
                         DataState.StructureModified, UpdateState.all));
             } catch (ProjectDeletedException e) {
                 PMExceptionHandler.handleGDProjectDeletedException();
             } catch (JBException e) {
                 ErrorHandlingUtil.createMessageDialog(e, null, null);
             } finally {
                 ProgressMonitorTracker.getInstance().setProgressMonitor(null);
                 monitor.done();
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public Object execute(ExecutionEvent event) {
         // Gather selected nodes
         TestCaseBrowser tcb = MultipleTCBTracker.getInstance().getMainTCB();
         if (!(tcb.getSelection() instanceof IStructuredSelection)) {
             return null;
         }
         IStructuredSelection sel = (IStructuredSelection)tcb.getSelection();
         List<INodePO> selectionList = sel.toList();
 
         List <INodePO> nodesToMove = new ArrayList<INodePO>();
         for (INodePO node : selectionList) {
             MultipleNodePM.collectAffectedNodes(nodesToMove, node);
         }
         if (!closeRelatedEditors(nodesToMove)) {
             return null;
         }
         // Check if move is valid
         ProblemSet moveProblems = getMoveProblem(nodesToMove);
 
         if (moveProblems.getProblems().isEmpty()) {
             Set<IReusedProjectPO> reusedProjects = 
                 GeneralStorage.getInstance().getProject().getUsedProjects();
 
             List<String> projectNamesList = new ArrayList<String>();
             for (IReusedProjectPO project : reusedProjects) {
                 projectNamesList.add(project.getName());
             }
 
             String [] projectNames = 
                 projectNamesList.toArray(new String [projectNamesList.size()]);
             ReusedProjectSelectionDialog dialog;
             if (hasRefDataCube(selectionList)) {
                 dialog = 
                         new ReusedProjectSelectionDialog(
                             Plugin.getShell(), projectNames, 
                             Messages.MoveTestCaseDialogShellTitle,
                             Messages.MoveTestCaseDialogCTDHint,
                             true,
                             IconConstants.MOVE_TC_DIALOG_STRING, 
                             Messages.MoveTestCaseDialogShellTitle);
             } else {
                 dialog = 
                         new ReusedProjectSelectionDialog(
                             Plugin.getShell(), projectNames, 
                             Messages.MoveTestCaseDialogShellTitle,
                             Messages.MoveTestCaseDialogMessage,
                             IconConstants.MOVE_TC_DIALOG_STRING, 
                             Messages.MoveTestCaseDialogShellTitle);
             }
             dialog.setHelpAvailable(true);
             dialog.create();
             DialogUtils.setWidgetNameForModalDialog(dialog);
             Plugin.getHelpSystem().setHelp(dialog.getShell(), 
                 ContextHelpIds.TESTCASE_MOVE_EXTERNAL);
             dialog.open();
             if (dialog.getReturnCode() == Window.OK) {
                 // Check which project was selected
                 String selectedName = dialog.getSelectedName();
                 IReusedProjectPO selectedProject = null;
                 for (IReusedProjectPO project : reusedProjects) {
                     if (selectedName.equals(project.getName())) {
                         selectedProject = project;
                         break;
                     }
                 }
                 doMove(tcb, selectionList, selectedProject);
             }
         } else {
             showProblems(moveProblems);
         }
 
         return null;
     }
     
     /** {@inheritDoc} */
     public void setEnabled(boolean enabled) {
         IProjectPO currentProject = GeneralStorage.getInstance().getProject();
         boolean projectAvailable = currentProject == null ? false
             : !currentProject.getUsedProjects().isEmpty();
         
         super.setEnabled(enabled && projectAvailable);
     }
     
     /**
      * Closes all editors that are related to elements in the given list.
      * 
      * @param selectionList List of GuiNodes.
      * @return <code>true</code> if all editors were successfully closed. 
      *         Otherwise, <code>false</code>.
      */
     private boolean closeRelatedEditors(List<INodePO> selectionList) {
         List<IEditorReference> editorsToClose = 
             new ArrayList<IEditorReference>(); 
         for (INodePO node : selectionList) {
             IEditorReference editor = 
                 Utils.getEditorRefByPO(node);
             if (editor != null) {
                 editorsToClose.add(editor);
             }
         }
 
         return Plugin.getActivePage().closeEditors(
                 editorsToClose.toArray(
                         new IEditorReference[editorsToClose.size()]), 
                 true);
     }
 
     /**
      * Performs the moving.
      * @param tcb the TestCase-Browser.
      * @param selectionList the selected Nodes to move.
      * @param selectedProject the selected Project to move to.
      */
     private void doMove(TestCaseBrowser tcb, List<INodePO> selectionList, 
         IReusedProjectPO selectedProject) {
         // Prepare modification to selected project
         EntityManager sess = null;
         try {
             IProjectPO extProject = ProjectPM.loadReusedProject(
                 selectedProject);
             sess = GeneralStorage.getInstance().getEntityManager();
             extProject = sess.find(NodeMaker.getProjectPOClass(),
                     extProject.getId());
             List<ICapPO> moveProblem = getMoveProblem(extProject, 
                 selectionList);
             if (!moveProblem.isEmpty()) {
                 ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.E_MOVE_TO_EXT_PROJ_ERROR_TOOLKITLEVEL,
                     null, null);
                 return;
             }
             ISpecObjContPO newParent = extProject.getSpecObjCont();
 
             List<MultipleNodePM.AbstractCmdHandle> commands = 
                 createCommands(selectionList, newParent, extProject);
 
             // Perform move
             MessageInfo errorMessageInfo = 
                 MultipleNodePM.getInstance().executeCommands(
                         commands, sess);
 
             DataEventDispatcher.getInstance().fireDataChangedListener(
                     newParent, DataState.StructureModified, UpdateState.all);
             
             if (errorMessageInfo == null) {
                 GeneralStorage.getInstance().getMasterSession().refresh(
                         GeneralStorage.getInstance().getProject()
                             .getSpecObjCont());
                 Plugin.startLongRunning(Messages
                         .RefreshTSBrowserActionProgressMessage);
                 try {
                     PlatformUI.getWorkbench().getProgressService()
                         .run(true, false,
                             new RefreshReusedProjectOperation(selectedProject));
                 } catch (InvocationTargetException e) {
                     // Already handled within the operation.
                 } catch (InterruptedException e) {
                     Utils.clearClient();
                 } finally {
                     Plugin.stopLongRunning();
                     DataEventDispatcher.getInstance().fireProjectStateChanged(
                             ProjectState.opened);
                 }
                 tcb.getTreeViewer().refresh();
             } else {
                 ErrorHandlingUtil.createMessageDialog(
                         errorMessageInfo.getMessageId(), 
                         errorMessageInfo.getParams(), 
                         null);
             }
         } catch (JBException e) {
             ErrorHandlingUtil.createMessageDialog(e, null, null);
         } catch (ToolkitPluginException tpie) {
             ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.E_GENERAL_TOOLKIT_ERROR);
         }
     }
 
     /**
      * Checks if the toolkit of the given selectionLists is compatible with 
      * the given {@link IProjectPO}
      * @param extProject the {@link IProjectPO} to move the given selectionList to.
      * @param selectionList the selectionList to move to the given {@link IProjectPO}
      * @return A List of {@link ICapPO}s which are incompatible or an empty List
      * if everything is OK.
      * @throws ToolkitPluginException in case of a ToolkitPlugin error.
      */
     private List<ICapPO> getMoveProblem(IProjectPO extProject, 
         List<INodePO> selectionList) throws ToolkitPluginException {
         
         final List<ICapPO> problemCaps = new ArrayList<ICapPO>();
         final String extToolkitId = extProject.getToolkit();
         final String extToolkitLevel = ToolkitSupportBP.getToolkitLevel(
             extToolkitId);
         final List<ICapPO> caps = getCaps(selectionList);
         for (ICapPO cap : caps) {
             final String capLevel = UsedToolkitBP.getInstance()
                 .getToolkitLevel(cap);
             final boolean capLessConcrete = !ToolkitUtils
                 .isToolkitMoreConcrete(capLevel, extToolkitLevel);
             final Component component = CapBP.getComponent(cap);
             final String capToolkitID = component.getToolkitDesriptor()
                 .getToolkitID();
             if (!(capLessConcrete || capToolkitID.equals(extToolkitId))) {
                 problemCaps.add(cap);
             }
         }
         return problemCaps;
     }
 
     /**
      * Gets all {@link ICapPO}s which are direct or indirect children of the 
      * given List of {@link GuiNode}s
      * @param selectionList a List of {@link GuiNode}s
      * @return a List of {@link ICapPO}s
      */
     private List<ICapPO> getCaps(List<INodePO> selectionList) {
         List<ICapPO> caps = new ArrayList<ICapPO>();
         for (INodePO node : selectionList) {
             CapBP.getCaps(node, caps);
         }
         return caps;
     }
     
     
     
     /**
      * Displays the problems for a proposed move operation.
      * 
      * @param moveProblems Valid problems with the proposed move operation.
      */
     private void showProblems(ProblemSet moveProblems) {
         // Display info as to why TCs could not be moved
         StringBuilder sb = new StringBuilder();
         for (INodePO moveProblem : moveProblems.getProblems()) {
             sb.append(moveProblem.getName());
             sb.append(StringConstants.NEWLINE);
         }
         ErrorHandlingUtil.createMessageDialog(MessageIDs.I_CANNOT_MOVE_TC, 
             null, new String [] {
                 NLS.bind(Messages.InfoDetailCannotMoveTc,
                     sb.toString())
             });
     }
 
     /**
      * 
      * @param selectionList All nodes that are to be moved.
      * @param newParent The new parent for the nodes.
      * @param extProject where selected nodes moved to
      * 
      * @return The commands necessary to move the given nodes.
      */
     private List<MultipleNodePM.AbstractCmdHandle> createCommands(
             List<INodePO> selectionList, ISpecObjContPO newParent,
             IProjectPO extProject) throws JBException {
 
         List<MultipleNodePM.AbstractCmdHandle> commands = 
                 new ArrayList<MultipleNodePM.AbstractCmdHandle>();
 
         CompNameUsageMap usageMap = new CompNameUsageMap();
         final IProjectPO currenProject = GeneralStorage.getInstance()
                 .getProject();
         final String projGuid = currenProject.getGuid();
         final Long projId = currenProject.getId();
         for (INodePO selNode : selectionList) {
             commands.add(new MultipleNodePM.MoveNodeHandle(selNode, selNode
                     .getParentNode(), newParent));
             
             List<INodePO> specTcs = new ArrayList<INodePO>();
             List<ISpecTestCasePO> specTcPOs = new ArrayList<ISpecTestCasePO>();
             addCatChildren(selNode, specTcs);
             for (INodePO spec : specTcs) {
                 ISpecTestCasePO specTestCasePo = (ISpecTestCasePO) spec;
                 specTcPOs.add(specTestCasePo);
                 CollectComponentNameUsersOp op = 
                         new CollectComponentNameUsersOp(projGuid, projId);
                 new TreeTraverser(specTestCasePo, op, true, 2).traverse();
                 usageMap.addAll(op.getUsageMap());
                 for (IExecTestCasePO execTc : NodePM.getInternalExecTestCases(
                         specTestCasePo.getGuid(),
                         specTestCasePo.getParentProjectId())) {
 
                     commands.add(new MultipleNodePM.UpdateTestCaseRefHandle(
                             execTc, specTestCasePo));
                 }
             }
             commands.add(new MultipleNodePM.UpdateParamNamesHandle(specTcPOs,
                     extProject));
         }
         commands.add(new MultipleNodePM.TransferCompNameHandle(usageMap,
                 projId, extProject));
 
         return commands;
     }
     
     /**
      * Indicates whether there is a problem with moving the given selection. If
      * there is a problem, it is described by the return value.
      * 
      * @param selectionList
      *            The elements that are to be moved
      * @return a set of problems
      */
     private ProblemSet getMoveProblem(List<INodePO> selectionList) {
         ProblemSet problems = new ProblemSet(selectionList);
         getMoveProblem(selectionList, problems);
         return problems;
     }
 
     /**
      * Indicates whether there is a problem with moving the given selection.
      * 
      * @param selectionList
      *            The elements that are to be moved
      * @param problems
      *            All problems with moving the given nodes.
      */
     private void getMoveProblem(List<INodePO> selectionList, 
         ProblemSet problems) {
         
         Long cProjId = GeneralStorage.getInstance().getProject().getId();
         for (INodePO node : selectionList) {
             if (node instanceof IExecTestCasePO) {
                 ISpecTestCasePO refTestCase = 
                         ((IExecTestCasePO)node).getSpecTestCase();
                 if (!problems.getNodesToMove().contains(refTestCase)
                         && cProjId.equals(refTestCase.getParentProjectId())) {
                     problems.getProblems().add(refTestCase);
                 }
             } else {
                 getMoveProblem(node.getUnmodifiableNodeList(), problems);
             }
         }
     }
     /**
      * checks if any of the node has ReferencedDataCubes
      * @param selectionList the list to be checked
      * @return returns <code>true</code> if there is one PO with a refDataCube 
      */
     private boolean hasRefDataCube(List<INodePO> selectionList) {
         for (INodePO selNode : selectionList) {            
             List<INodePO> specTcs = new ArrayList<INodePO>();
             addCatChildren(selNode, specTcs);
             for (INodePO spec : specTcs) {
                 if (spec instanceof ISpecTestCasePO) {
     
                     ISpecTestCasePO specTestCasePo = (ISpecTestCasePO)spec;
                     if (specTestCasePo.getReferencedDataCube() != null) {
                         return true;
                     }
                     Iterator<INodePO> execTcs = specTestCasePo
                             .getNodeListIterator();
                     while (execTcs.hasNext()) {
                         INodePO exec = execTcs.next();
                        IExecTestCasePO execTestCasePo = (IExecTestCasePO)exec; 
                        if (execTestCasePo.getReferencedDataCube() != null) {
                            return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
     
     /**
      * Adds all spec testcase descendants of the given node to the given 
      * list.
      * 
      * @param parentNode The parent node
      * @param nodeList The node list.
      */
     private static void addCatChildren(
         INodePO parentNode, Collection<INodePO> nodeList) {
         
         if (parentNode instanceof ICategoryPO) {
             for (INodePO node : parentNode.getUnmodifiableNodeList()) {
                 addCatChildren(node, nodeList);
             }
         } else if (parentNode instanceof ISpecTestCasePO) {
             nodeList.add(parentNode);
         }
     }
 }
