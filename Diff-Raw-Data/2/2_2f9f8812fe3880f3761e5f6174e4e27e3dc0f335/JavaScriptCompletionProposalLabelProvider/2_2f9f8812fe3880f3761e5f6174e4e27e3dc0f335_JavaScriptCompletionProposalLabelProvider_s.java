 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.text.completion;
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference;
 import org.eclipse.dltk.ui.text.completion.CompletionProposalLabelProvider;
 
 public class JavaScriptCompletionProposalLabelProvider extends
 		CompletionProposalLabelProvider {
 	protected String createMethodProposalLabel(CompletionProposal methodProposal) {
 		if (methodProposal.extraInfo instanceof SelfCompletingReference) {
 			SelfCompletingReference cm = (SelfCompletingReference) methodProposal.extraInfo;
 			methodProposal.setParameterNames(cm.getParameterNames());
 		}
 		StringBuffer nameBuffer = new StringBuffer();
 
 		// method name
 		nameBuffer.append(methodProposal.getName());
 
 		// parameters
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
 		nameBuffer.append(')');
 
 		return nameBuffer.toString();
 	}
 
 	protected String createOverrideMethodProposalLabel(
 			CompletionProposal methodProposal) {
 		StringBuffer nameBuffer = new StringBuffer();
 
 		// method name
 		nameBuffer.append(methodProposal.getName());
 
 		// parameters
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
		nameBuffer.append(")  "); //$NON-NLS-1$
 
 		return nameBuffer.toString();
 	}
 }
