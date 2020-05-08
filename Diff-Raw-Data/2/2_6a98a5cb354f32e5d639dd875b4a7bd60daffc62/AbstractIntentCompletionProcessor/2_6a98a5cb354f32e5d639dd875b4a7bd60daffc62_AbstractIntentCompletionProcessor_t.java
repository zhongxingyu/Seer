 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor.completion;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.CompletionProposal;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 import org.eclipse.jface.text.templates.DocumentTemplateContext;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateContext;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateProposal;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Computes the completion proposals.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public abstract class AbstractIntentCompletionProcessor implements IContentAssistProcessor {
 	/** The auto activation characters for completion proposal. */
 	private static final char[] AUTO_ACTIVATION_CHARACTERS = new char[] {' ',
 	};
 
 	/**
 	 * The document.
 	 */
 	protected IDocument document;
 
 	/**
 	 * An offset within the text for which completions should be computed.
 	 */
 	protected int offset;
 
 	/**
 	 * The current word.
 	 */
 	protected String start;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
 	 *      int)
 	 */
 	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int currentOffset) {
 		document = viewer.getDocument();
 		ITextSelection selection = (ITextSelection)viewer.getSelectionProvider().getSelection();
 		if (selection != null && selection.getOffset() == currentOffset) {
 			offset = selection.getOffset() + selection.getLength();
 		} else {
 			offset = currentOffset;
 		}
 		// get the currently typed word
 		int index = offset;
 		String text = document.get();
 		while (index > 0 && isIntentIdentifierPart(text.charAt(index - 1))) {
 			index--;
 		}
 		start = text.substring(index, offset);
 		try {
 			return computeCompletionProposals();
 		} finally {
 			document = null;
 			offset = 0;
 		}
 	}
 
 	private boolean isIntentIdentifierPart(char c) {
 		return c == '@' || Character.isJavaIdentifierPart(c);
 	}
 
 	/**
 	 * Computes the completion proposals.
 	 * 
 	 * @return the completion proposals
 	 */
 	protected abstract ICompletionProposal[] computeCompletionProposals();
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
 	 *      int)
 	 */
 	public IContextInformation[] computeContextInformation(ITextViewer viewer, int currentOffset) {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
 	 */
 	public char[] getCompletionProposalAutoActivationCharacters() {
 		return AUTO_ACTIVATION_CHARACTERS;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
 	 */
 	public char[] getContextInformationAutoActivationCharacters() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
 	 */
 	public String getErrorMessage() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
 	 */
 	public IContextInformationValidator getContextInformationValidator() {
 		return null;
 	}
 
 	/**
 	 * Returns the context type related to the completion processor.
 	 * 
 	 * @return the context type related to the completion processor
 	 */
 	public abstract String getContextType();
 
 	/**
 	 * Create a keyword proposal with the given parameters.
 	 * 
 	 * @param keyword
 	 *            the keyword
 	 * @return the keyword proposal
 	 */
 	protected ICompletionProposal createKeyWordProposal(String keyword) {
		return new CompletionProposal(keyword, offset - start.length(), start.length(), keyword.length());
 	}
 
 	/**
 	 * Create a template proposal with the given parameters.
 	 * 
 	 * @param templateName
 	 *            the template name
 	 * @param templateDescription
 	 *            the template description
 	 * @param templatePattern
 	 *            the template pattern
 	 * @param templateImagePath
 	 *            the template image
 	 * @return the template proposal
 	 */
 	protected TemplateProposal createTemplateProposal(String templateName, String templateDescription,
 			String templatePattern, String templateImagePath) {
 		int startLength = start.length();
 		Template template = new Template(templateName, templateDescription, getContextType(),
 				templatePattern, true);
 		TemplateContextType type = new TemplateContextType(getContextType(), getContextType());
 		TemplateContext context = new DocumentTemplateContext(type, document, offset - startLength,
 				startLength);
 		Region region = new Region(offset - startLength, startLength);
 		Image image = IntentEditorActivator.getDefault().getImage(templateImagePath);
 		return new TemplateProposal(template, context, region, image);
 	}
 
 }
