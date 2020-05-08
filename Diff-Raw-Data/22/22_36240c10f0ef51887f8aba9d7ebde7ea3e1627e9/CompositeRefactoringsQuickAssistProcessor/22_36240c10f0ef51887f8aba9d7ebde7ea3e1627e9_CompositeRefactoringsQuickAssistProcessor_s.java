 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.steps;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.ui.text.java.IInvocationContext;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jdt.ui.text.java.IProblemLocation;
 import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
 import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
 
 
 public class CompositeRefactoringsQuickAssistProcessor implements IQuickAssistProcessor {
 
 	@Override
 	public boolean hasAssists(IInvocationContext context) throws CoreException {
 		ASTNode coveringNode= context.getCoveringNode();
 		if (coveringNode != null) {
 			return //new CreateNewSuperclass(context, coveringNode, false).hasInputs() ||
 			new MoveToImmediateSuperclass(context, coveringNode, false).hasInputs() ||
 					new MoveTypeToNewFile(context, coveringNode, false).hasInputs() ||
					new ReplaceTypeBySupertypeInVariableDeclarations(context, coveringNode, false).hasInputs() ||
					new ReplaceTypeBySupertypeInInstanceOf(context, coveringNode, false).hasInputs() ||
 					new AddMethodParameterForExpression(context, coveringNode, false).hasInputs() ||
 					new CreateNewTopLevelSuperClass(context, coveringNode, false).hasInputs();
 		}
 		return false;
 	}
 
 	@Override
 	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
 		ASTNode coveringNode= context.getCoveringNode();
 		if (coveringNode != null) {
 			ArrayList<ICommandAccess> proposals= new ArrayList<ICommandAccess>();
 //			proposals.addAll(new CreateNewSuperclass(context, coveringNode, false).getProposals());
 			proposals.addAll(new MoveToImmediateSuperclass(context, coveringNode, false).getProposals());
 			proposals.addAll(new MoveTypeToNewFile(context, coveringNode, false).getProposals());
			proposals.addAll(new ReplaceTypeBySupertypeInVariableDeclarations(context, coveringNode, false).getProposals());
			proposals.addAll(new ReplaceTypeBySupertypeInInstanceOf(context, coveringNode, false).getProposals());
 			proposals.addAll(new AddMethodParameterForExpression(context, coveringNode, false).getProposals());
 			proposals.addAll(new CreateNewTopLevelSuperClass(context, coveringNode, false).getProposals());
 			return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
 		}
 		return null;
 	}
 
 }
