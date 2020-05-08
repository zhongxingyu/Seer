 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.businessprocess.compcheck;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.core.Activator;
 import org.eclipse.jubula.client.core.businessprocess.problems.IProblem;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ICompletenessCheckListener;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 
 /**
  * @author Markus Tiede
  * @created 27.10.2011
  */
 public class CompletenessPropagator implements ICompletenessCheckListener {
     /** this instance */
     private static CompletenessPropagator instance;
 
     /**
      * private constructor
      */
     private CompletenessPropagator() {
         DataEventDispatcher.getInstance().addCompletenessCheckListener(this);
     }
 
     /**
      * @return the ComponentNamesList
      */
     public static CompletenessPropagator getInstance() {
         if (instance == null) {
             instance = new CompletenessPropagator();
         }
         return instance;
     }
     
     /** {@inheritDoc} */
     public void completenessCheckFinished() {
         IProjectPO project = GeneralStorage.getInstance().getProject();
         final TreeTraverser traverser = new TreeTraverser(project, 
                 new CompletenessPropagationOperation());
         traverser.traverse(true);
     }
     
     /**
      * @author Marcell Salvage
      */
     public static class CompletenessPropagationOperation 
         implements ITreeNodeOperation<INodePO> {
         /** Represents a error in a child */
         public static final IProblem ERROR_IN_CHILD = ProblemFactory
                 .createProblem(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                         Messages.TooltipErrorInChildren));
 
         /** Represents a warning in a child */
         public static final IProblem WARNING_IN_CHILD = ProblemFactory
                 .createProblem(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                         Messages.TooltipWarningInChildren));
 
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
             INodePO parent, INodePO node, boolean alreadyVisited) {
             node.removeProblem(ERROR_IN_CHILD);
             node.removeProblem(WARNING_IN_CHILD);
            if (!node.isActive()) {
                return false;
            }
             return true;
         }
 
         /** {@inheritDoc} */
         public void postOperate(ITreeTraverserContext<INodePO> ctx, 
             INodePO parent, INodePO node, boolean alreadyVisited) {
             IProblem worstProblem = ProblemFactory.getWorstProblem(
                     node.getProblems());
             if (worstProblem != null) {
                 setParentProblem(parent, worstProblem.getSeverity());
             }
         }
 
         /**
          * @param node
          *            the node where the problem should be added.
          * @param severity
          *            severity of which the problem should be set
          */
         private void setParentProblem(INodePO node, int severity) {
             switch (severity) {
                 case IStatus.ERROR:
                     node.addProblem(ERROR_IN_CHILD);
                     break;
                 case IStatus.WARNING:
                     node.addProblem(WARNING_IN_CHILD);
                     break;
                 case IStatus.INFO:
                 default:
                     break;
             }
         }
     }
 }
