 /*******************************************************************************
  * Copyright (c) 2004, 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.analyze.impl.standard.analyze;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jubula.client.analyze.definition.IAnalyze;
 import org.eclipse.jubula.client.analyze.internal.AnalyzeParameter;
 import org.eclipse.jubula.client.analyze.internal.AnalyzeResult;
 import org.eclipse.jubula.client.analyze.internal.helper.ProjectContextHelper;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.utils.AbstractNonPostOperatingTreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 
 /**
  * This Analyze counts the different Elements, that are used in a TestProject. 
  * It uses the TreeTraverser to traverse the Tree from the given node top-down. 
  * Every Node is checked relating to his type. These TypeAmounts are counted ad
  * returned when the TreeTraverser has finished.
  * 
  * @author volker
  * 
  */
 public class NumericalProjectElementCounter implements IAnalyze {
     /**
      * The CountElementOperation which is given to the TreeTraverser
      * @author volker
      */
     static class CountElementOperation extends
             AbstractNonPostOperatingTreeNodeOperation<INodePO> {
         /**
          * This Map is used to save the amount of the different NodeTypes
          */
         private Map<Class<? extends INodePO>, Integer> m_amount = 
                 new HashMap<Class<? extends INodePO>, Integer>();
         
         /** The given ProgressMonitor */
         private IProgressMonitor m_pm;
         
         /**
          * The Constructor of the CountElementOperation
          * @param monitor
          *            The given ProgressMonitor
          */
         public CountElementOperation(IProgressMonitor monitor) {
             setProgressMonitor(monitor);
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
 
            // if (ProjectContextHelper.getObjContType().equals("project")) {
            // System.out.println(GeneralStorage.getInstance().getProject().getId()
            // + " " + node.getClass() + " " + node.getName() + " " +
            // node.getParentProjectId());
            if (node instanceof IProjectPO) {
                return !alreadyVisited;
            }
            long currentProjectID = GeneralStorage.getInstance().getProject()
                    .getId();
            long nodeParentProjectID = node.getParentProjectId();

            if (currentProjectID != nodeParentProjectID) {
                return alreadyVisited;
            }

             Class<? extends INodePO> nodeType = null;
            if (node instanceof ICategoryPO || node instanceof ISpecTestCasePO
                    || node instanceof ICapPO || node instanceof ITestSuitePO
                     || node instanceof IExecTestCasePO
                     || node instanceof ITestJobPO
                     || node instanceof IRefTestSuitePO
                     || node instanceof IEventExecTestCasePO) {
                 nodeType = node.getClass();
             }

             if (nodeType != null) {
                 Integer ccount = getAmount().get(nodeType);
                 if (ccount == null) {
                     ccount = new Integer(0);
                 }
                 Integer nCount = ccount + 1;
                 getAmount().put(nodeType, nCount);
 
                 getProgressMonitor().worked(1);
                 if (getProgressMonitor().isCanceled()) {
                     getProgressMonitor().done();
                     throw new OperationCanceledException();
                 }
             }
             return !alreadyVisited;
         }
         /**
          * @return The AmountMap containing the amounts of the different NodeTypes
          */
         public Map<Class<? extends INodePO>, Integer> getAmount() {
             return m_amount;
         }
 
         /**
          * @return The ProgressMonitor
          */
         public IProgressMonitor getProgressMonitor() {
             return m_pm;
         }
 
         /**
          * @param monitor
          *            The given ProgressMonitor
          */
         public void setProgressMonitor(IProgressMonitor monitor) {
             this.m_pm = monitor;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public AnalyzeResult execute(Object obj2analyze, IProgressMonitor monitor, 
             String resultType, List<AnalyzeParameter> param,
             String analyzeName) {
         int workAmount = 1;
         // get the number of nodes from the NodePersistenceManager to have a
         // representative workAmount value for the ProgressMonitor
         if (obj2analyze instanceof IProjectPO) {
             workAmount = (int) NodePM.getNumNodes(((IProjectPO) obj2analyze)
                     .getId(), GeneralStorage.getInstance().getMasterSession());
         } else if (obj2analyze instanceof INodePO) {
             workAmount = (int) NodePM.getNumNodes(((INodePO) obj2analyze)
                     .getParentProjectId(), GeneralStorage.getInstance().
                     getMasterSession());
         }
         monitor.beginTask("", workAmount);
         monitor.subTask(analyzeName);
 
         CountElementOperation c = new CountElementOperation(monitor);
         traverse(c, obj2analyze, ProjectContextHelper.getObjContType());
 
         monitor.worked(1);
         if (monitor.isCanceled()) {
             monitor.done();
             throw new OperationCanceledException();
         }
         return new AnalyzeResult(resultType, c.getAmount());
     }
 
     /**
      * Creates a new instance of the TreeTraverser, adds the given Operation and
      * traverses
      * 
      * @param count
      *            The instance of CountElementOperation
      * @param obj
      *            The given selection
      * @param objContType
      *            The given objContType
      */
     private void traverse(CountElementOperation count, Object obj,
             String objContType) {
 
         if (obj instanceof INodePO && objContType.equals("IExecObjContPO")) {
             final INodePO root = (INodePO) obj;
             
             TreeTraverser tt = new TreeTraverser(root, count, true, true) {
                 @Override
                 protected void traverseReusedProjectSpecPart(
                         ITreeTraverserContext<INodePO> context,
                         IProjectPO project) {
                     // ignore reused Projects
                 }
             };
             tt.traverse(true);
         } else if (obj instanceof INodePO
                 && objContType.equals("ISpecObjContPO")) {
             
             final INodePO root = (INodePO) obj;
             
             TreeTraverser tt = new TreeTraverser(root, count, true, false) {
                 @Override
                 protected void traverseReusedProjectSpecPart(
                         ITreeTraverserContext<INodePO> context,
                         IProjectPO project) {
                     // ignore reused Projects
                 }
             };
             tt.traverse(true);
         }
         
         if (obj instanceof INodePO && objContType.equals("project")) {
             final INodePO root = (INodePO) obj;
             
             TreeTraverser tt = new TreeTraverser(root, count, true, true) {
                 @Override
                 protected void traverseReusedProjectSpecPart(
                         ITreeTraverserContext<INodePO> context,
                         IProjectPO project) {
                     // ignore reused Projects
                 }
             };
             tt.traverse(true);
         }
         if (obj instanceof INodePO && objContType.equals("")) {
             final INodePO root = (INodePO) obj;
             
             TreeTraverser tt = new TreeTraverser(root, count, true, true) {
                 @Override
                 protected void traverseReusedProjectSpecPart(
                         ITreeTraverserContext<INodePO> context,
                         IProjectPO project) {
                     // ignore reused Projects
                 }
             };
             tt.traverse(true);
         }
     }
 }
