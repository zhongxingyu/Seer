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
 package org.eclipse.jubula.client.core.businessprocess.problems;
 
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.core.Activator;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.utils.Languages;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Factory to create common problems and generic ones for external usage.
  * 
  * @author BREDEX GmbH
  * @created 24.01.2011
  */
 public final class ProblemFactory {
     /** 
      * private constructor because its a utility class 
      */
     private ProblemFactory() {
         // no-op
     }
 
     /**
      * @param loc
      *            Locale for which the test data are incomplete.
      * @param node
      *            the affected node
      * @return A Problem that is representing missing test data for this local.
      */
     public static IProblem createIncompleteTestDataProblem(Locale loc,
             INodePO node) {
         
         return new Problem(NLS.bind(
                 Messages.ProblemIncompleteTestDataMarkerText,
                 new Object[] { Languages.getInstance().getDisplayString(loc),
                         node.getName() }),
                 new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                         Messages.ProblemIncompleteTestDataTooltip),
                 node, ProblemType.REASON_TD_INCOMPLETE);
     }
 
     /**
      * 
      * @param aut
      *            AUT where the object mapping is incomplete
      * @return A problem which represents incomplete object mapping of this
      *         AUT.
      */
     public static IProblem createIncompleteObjectMappingProblem(
             IAUTMainPO aut) {
         return new Problem(NLS.bind(
                 Messages.ProblemIncompleteObjectMappingMarkerText,
                aut.getName()), new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        Messages.ProblemIncompleteObjectMappingTooltip), 
                        aut.getName(),
                        ProblemType.REASON_OM_INCOMPLETE);
     }
 
     /**
      * @return Problem that represents missing spectestcases.
      */
     public static IProblem createMissingReferencedSpecTestCasesProblem() {
         return new Problem(Messages.ProblemMissingReferencedTestCaseMarkerText,
                 new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
                         Messages.ProblemMissingReferencedTestCaseTooltip),
                 null, ProblemType.REASON_MISSING_SPEC_TC);
     }
 
     /**
      * @param status
      *            Status with which the problem will be initialized.
      * @return An instance of this problem
      */
     public static IProblem createProblem(IStatus status) {
         return new Problem(null, status, null, ProblemType.NO_QUICKFIX);
     }
 
     /**
      * @param status
      *            Status with which the problem will be intialized.
      * @param markerMessage
      *            message of the marker
      * @param data
      *            the affected object; if this is a INodePO it's also set as a
      *            problem of the node itself
      * @param probType
      *            the problem type
      * @return An instance of this problem which will create an marker when
      *         attached to a INodePO.
      */
     public static IProblem createProblemWithMarker(IStatus status,
             String markerMessage, Object data, ProblemType probType) {
         IProblem problem = new Problem(markerMessage, status, data, probType);
         if (data instanceof INodePO) {
             ((INodePO)data).addProblem(problem);
         }
         return problem;
     }
 
     /**
      * @param problems
      *            The list of problems which should be searched for the worst
      *            problem.
      * @return The problem with the worst severity or null if none is found.
      */
     public static IProblem getWorstProblem(Set<IProblem> problems) {
         IProblem worstProblem = null;
         for (IProblem problem : problems) {
             if (worstProblem == null
                     || worstProblem.getStatus().getSeverity() 
                         < problem.getStatus().getSeverity()) {
                 worstProblem = problem;
             }
         }
         return worstProblem;
     }
     
     /**
      * @param problems
      *            The list of problems which should be searched for the worst
      *            problem.
      * @return a list of problems with the worst severity
      */
     public static Set<IProblem> getWorstProblems(Set<IProblem> problems) {
         IProblem worstProblem = getWorstProblem(problems);
         Set<IProblem> worstProblems = new HashSet<IProblem>();
         if (worstProblem != null) {
             for (IProblem problem : problems) {
                 if (worstProblem.getStatus().getSeverity() 
                         == problem.getStatus().getSeverity()) {
                     worstProblems.add(problem);
                 }
             }
         }
         return worstProblems;
     }
     
     /**
      * @param node
      *            the node to check for problems.
      * @return true if problem is present; false otherwise
      */
     public static boolean hasProblem(INodePO node) {
         return node.getProblems().size() > 0;
     }
 }
