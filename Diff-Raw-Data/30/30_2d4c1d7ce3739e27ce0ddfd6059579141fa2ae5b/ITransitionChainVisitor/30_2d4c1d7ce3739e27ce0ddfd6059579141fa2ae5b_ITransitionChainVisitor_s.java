 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.etrice.generator.etricegen;
 
 import org.eclipse.etrice.core.room.CPBranchTransition;
 import org.eclipse.etrice.core.room.ContinuationTransition;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.Transition;
 
 public interface ITransitionChainVisitor {
 	String genTypedData();
 	String genActionOperationCall(Transition tr);
 	String genEntryOperationCall(State state);
 	String genExitOperationCall(State state);
	String genElseIfBranch(CPBranchTransition tr);
 	String genElseBranch(ContinuationTransition tr);
 	String genEndIf();
 	String genReturnState(State state);
 
 } // ITransitionChainVisitor
