 /*******************************************************************************
  * Copyright (c) 2011 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.text.completion;
 
 import java.util.List;
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.javascript.typeinfo.model.GenericType;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeVariable;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposalExtension2;
 import org.eclipse.dltk.ui.text.completion.LinkedModeScriptCompletionProposal;
 import org.eclipse.dltk.ui.text.completion.ProposalInfo;
 import org.eclipse.dltk.ui.text.completion.ReplacementBuffer;
 import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * @since 4.0
  */
 public class GenericTypeCompletionProposal extends
 		LinkedModeScriptCompletionProposal implements
 		IScriptCompletionProposalExtension2 {
 	/**
 	 * Triggers for method proposals. Do not modify.
 	 */
 	protected final static char[] TYPE_TRIGGERS = new char[] { '<', ' ' };
 
 	public GenericTypeCompletionProposal(CompletionProposal proposal,
 			ScriptContentAssistInvocationContext context) {
 		super(proposal, context);
 	}
 
 	public String getName() {
 		return fProposal.getName();
 	}
 
 	@Override
 	public CharSequence getPrefixCompletionText(IDocument document,
 			int completionOffset) {
 		String completion = String.valueOf(fProposal.getName());
 		if (isCamelCaseMatching()) {
 			String prefix = getPrefix(document, completionOffset);
 			return getCamelCaseCompound(prefix, completion);
 		}
 		return completion;
 	}
 
 	@Override
 	protected char[] computeTriggerCharacters() {
 		return TYPE_TRIGGERS;
 	}
 
 	@Override
 	protected char getOpenTrigger() {
 		return '<';
 	}
 
 	@Override
	protected char getExitTrigger() {
 		return '>';
 	}
 
 	@Override
 	protected void computeReplacement(ReplacementBuffer buffer) {
 		buffer.append(fProposal.getName());
 		buffer.append("<");
 		setCursorPosition(buffer.length());
 		final GenericType genericType = (GenericType) fProposal.getExtraInfo();
 		final List<TypeVariable> variables = genericType.getTypeParameters();
 		for (int i = 0; i < variables.size(); ++i) {
 			if (i != 0) {
 				buffer.append(COMMA);
 			}
 			buffer.addArgument(variables.get(i).getName());
 		}
 		buffer.append(">");
 	}
 
 	@Override
 	protected ProposalInfo computeProposalInfo() {
 		return new JavaScriptProposalInfo(fProposal.getExtraInfo());
 	}
 
 	@Override
 	protected Image computeImage() {
 		return DLTKUIPlugin.getImageDescriptorRegistry().get(
 				fInvocationContext.getLabelProvider()
 						.createTypeImageDescriptor(fProposal));
 	}
 
 }
