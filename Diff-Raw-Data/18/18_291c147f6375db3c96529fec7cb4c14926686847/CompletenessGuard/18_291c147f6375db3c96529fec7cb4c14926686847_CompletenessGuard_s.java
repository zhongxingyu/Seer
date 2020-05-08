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
 package org.eclipse.jubula.client.core.businessprocess.compcheck;
 
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Set;
 
 import org.eclipse.jubula.client.core.businessprocess.CompNameResult;
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.problems.IProblem;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.LogicComponentNotManagedException;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.AbstractNonPostOperatingTreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.eclipse.jubula.tools.xml.businessmodell.ConcreteComponent;
 
 /**
  * @author BREDEX GmbH
  * @created 10.05.2005
  */
 @SuppressWarnings("synthetic-access")
 public final class CompletenessGuard {
     /**
      * Operation to set the CompleteSpecTc flag for a node
      */
     private static class CheckMissingTestCaseReferences extends
             AbstractNonPostOperatingTreeNodeOperation<INodePO> {
 
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (node instanceof IExecTestCasePO) {
                 IExecTestCasePO execTc = (IExecTestCasePO) node;
                 boolean isMissingSpecTc = execTc.getSpecTestCase() == null;
                 setCompletenessMissingTestCase(execTc, !isMissingSpecTc);
             }
             return !alreadyVisited;
         }
     }
 
     /**
      * Operation to reflect the activity status of a node
      */
     private static class InactiveNodesOperation extends
             AbstractNonPostOperatingTreeNodeOperation<INodePO> {
 
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (!node.isActive()) {
                 Set<IProblem> problemsToRemove = new HashSet<IProblem>(
                         node.getProblems());
                 for (IProblem problem : problemsToRemove) {
                     node.removeProblem(problem);
                 }
             }
             return true;
         }
     }
 
     /**
      * Updates the flags for the object mapping (complete or not?) in all nodes
      * down to the test step.
      */
     private static class CheckObjectMappingCompleteness implements
             ITreeNodeOperation<INodePO> {
         /**
          * The business process that performs component name operations.
          */
         private final CompNamesBP m_compNamesBP = new CompNamesBP();
         /**
          * The AUT
          */
         private IAUTMainPO m_aut;
 
         /**
          * Operates on the test step by setting its object mapping flag. The
          * node that is responsible for a component name is also updated.
          * 
          * @param ctx
          *            The tree traverser context
          * @param cap
          *            The test step
          * @return <code>true</code> if the component name of the test step is
          *         mapped, <code>false</code> otherwise
          */
         private boolean handleCap(ITreeTraverserContext<INodePO> ctx, 
             ICapPO cap) {
             IObjectMappingPO objMap = m_aut.getObjMap();
             String compName = cap.getComponentName();
             IComponentNamePO compNamePo = ComponentNamesBP.getInstance()
                     .getCompNamePo(compName);
             if (compNamePo != null) {
                 compName = compNamePo.getGuid();
             }
 
             final Component metaComponentType = cap.getMetaComponentType();
             if (metaComponentType instanceof ConcreteComponent
                     && ((ConcreteComponent) metaComponentType)
                             .hasDefaultMapping()) {
 
                 setCompletenessObjectMapping(cap, m_aut, true);
                 return true;
             }
 
             if (compName != null && objMap != null) {
                 IComponentIdentifier id = null;
                 CompNameResult result = m_compNamesBP.findCompName(
                         ctx.getCurrentTreePath(), cap, cap.getComponentName(),
                         ComponentNamesBP.getInstance());
                 try {
                     id = objMap.getTechnicalName(result.getCompName());
                 } catch (LogicComponentNotManagedException e) {
                     // ok
                 }
                 if (id == null) {
                     INodePO rNode = result.getResponsibleNode();
                     setCompletenessObjectMapping(rNode, m_aut, false);
                 } else {
                     setCompletenessObjectMapping(cap, m_aut, true);
                 }
             }
             boolean hasCompleteOM = !cap.getProblems().contains(
                     ProblemFactory.createIncompleteObjectMappingProblem(m_aut));
             return hasCompleteOM;
         }
 
         /**
          * Sets the object mapping flag of the passed node to <code>true</code>
          * 
          * {@inheritDoc}
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (node instanceof ITestSuitePO) {
                 ITestSuitePO ts = (ITestSuitePO) node;
                 m_aut = ts.getAut();
             }
             if (m_aut != null) {
                 setCompletenessObjectMapping(node, m_aut, true);
             }
             return true;
         }
 
         /**
          * Updates the object mapping flags of the passed node and its parent.
          * 
          * {@inheritDoc}
          */
         public void postOperate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
 
             if (m_aut != null) {
                 if (node instanceof ICapPO) {
                     handleCap(ctx, (ICapPO) node);
                 }
             }
         }
     }
 
     /**
      * Operation to set the CompleteTestData flag at TDManager.
      * 
      * @author BREDEX GmbH
      * @created 10.10.2005
      */
     private static class CheckTestDataCompleteness extends
             AbstractNonPostOperatingTreeNodeOperation<INodePO> {
 
         /** The Locale to check */
         private Locale m_locale;
 
         /**
          * @param loc
          *            The Locale to check
          */
         public CheckTestDataCompleteness(Locale loc) {
             m_locale = loc;
         }
 
         /**
          * Sets the CompleteTDFlag in all ParamNodePOs. {@inheritDoc}
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             checkLocalTestData(node, m_locale);
             return !alreadyVisited;
         }
     }
 
     /**
      * private constructor
      */
     private CompletenessGuard() {
         // nothing
     }
 
     /**
      * checks OM and TD Completeness of all TS
      * 
      * @param loc
      *            the Locale to check
      * @param root
      *            INodePO
      */
     public static void checkAll(Locale loc, INodePO root) {
         // Iterate Execution tree
         final TreeTraverser traverser = new TreeTraverser(root);
         traverser.addOperation(new CheckObjectMappingCompleteness());
         traverser.addOperation(new CheckTestDataCompleteness(loc));
         traverser.addOperation(new CheckMissingTestCaseReferences());
         traverser.addOperation(new InactiveNodesOperation());
         traverser.traverse(true);
     }
 
     /**
      * checks TD Completeness of all TS
      * 
      * @param loc
      *            the Locale to check
      * @param root
      *            INodePO
      */
     public static void checkTestData(Locale loc, INodePO root) {
         // Iterate Execution tree
         new TreeTraverser(root, new CheckTestDataCompleteness(loc))
                 .traverse(true);
     }
 
     /**
      * @param node
      *            the node
      * @param aut
      *            AUT, for which to set the omFlag
      * @param omFlag
      *            The omFlag to set.
      */
     private static void setCompletenessObjectMapping(INodePO node,
             IAUTMainPO aut, boolean omFlag) {
         setNodeProblem(node,
                 ProblemFactory.createIncompleteObjectMappingProblem(aut),
                 omFlag);
     }
 
     /**
      * @param node
      *            the node
      * @param problem
      *            the problem
      * @param deleteOrAdd
      *            true to delete; false to add problem to node
      */
     private static void setNodeProblem(INodePO node, IProblem problem,
             boolean deleteOrAdd) {
         if (deleteOrAdd) {
             node.removeProblem(problem);
         } else {
             node.addProblem(problem);
         }
     }
 
     /**
      * @param node
      *            the node
      * @param completeTCFlag
      *            true to delete; false to add problem to node
      */
     private static void setCompletenessMissingTestCase(INodePO node,
             boolean completeTCFlag) {
         setNodeProblem(node,
                 ProblemFactory.createMissingReferencedSpecTestCasesProblem(),
                 completeTCFlag);
     }
 
     /**
      * method to set the sumTdFlag for a given Locale
      * 
      * @param node
      *            the node
      * @param loc
      *            locale, for which to set the sumTdFlag
      * @param flag
      *            the state of sumTdFlag to set
      */
     public static void setCompletenessTestData(INodePO node, Locale loc,
             boolean flag) {
         setNodeProblem(node,
                 ProblemFactory.createIncompleteTestDataProblem(
                         loc, node), flag);
     }
 
     /**
      * clear all test data related problems for the given node
      * 
      * @param node
      *            the node
      */
     private static void resetTestDataCompleteness(INodePO node) {
        IProjectPO proj = GeneralStorage.getInstance().getProject();
        for (Locale locale : proj.getLangHelper().getLanguageList()) {
            setCompletenessTestData(node, locale, true);
         }
     }
 
     /**
      * @param node
      *            the node to check
      * @param locale the locale to check
      */
     public static void checkLocalTestData(INodePO node, Locale locale) {
         INodePO possibleDataSourceNode = node;
         if (node instanceof IExecTestCasePO) {
             IExecTestCasePO execTc = (IExecTestCasePO) node;
             if (execTc.getHasReferencedTD()) {
                 possibleDataSourceNode = execTc.getSpecTestCase();
             }
         }
         if (possibleDataSourceNode instanceof IParamNodePO) {
             IParamNodePO dataSourceNode = (IParamNodePO) possibleDataSourceNode;
             INodePO nodeToModify = null;
             if (!(node instanceof ISpecTestCasePO)) {
                 nodeToModify = node;
             }
             if (nodeToModify != null)  {
                 resetTestDataCompleteness(nodeToModify);
                 setCompletenessTestData(nodeToModify, locale,
                         dataSourceNode.isTestDataComplete(locale));
             }
         }
     }
 }
