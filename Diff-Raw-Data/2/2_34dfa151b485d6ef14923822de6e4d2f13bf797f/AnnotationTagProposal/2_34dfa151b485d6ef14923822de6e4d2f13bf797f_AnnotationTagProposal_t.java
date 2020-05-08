 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Sep 4, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.common.internal.annotations.ui;
 
 import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jst.common.internal.annotations.registry.AttributeValueProposalHelper;
 import org.eclipse.swt.graphics.Image;
 
 
 /**
  * @author kelleyp
  * 
  * A completion proposal especially for Annotation tag completions. This problem this class was
  * created to solve was the problem of attaching help text to a proposal. The mechanism inside of
  * JavaCompletionProposal was useless to us, since it was tied to the idea that the proposal would
  * be for an actual java element, that has javadoc attached to it, etc... So here we subclass
  * JavaCompletionProposal and override <code>getAdditionalProposalInfo</code> for a more suitable
  * way of associating help text with a proposal.
  */
 public class AnnotationTagProposal extends JavaCompletionProposal {
 	private static final char QUOTE = '"';
 	private boolean ensureBeginQuote = false;
 	private boolean ensureEndQuote = false;
 	private String localString;
 	//private String partialValueString;
 	//private AttributeValueProposalHelper helper;
 
 	/**
 	 * Localized help text.
 	 */
 	private String locText;
 
 	/**
 	 * @see JavaCompletionProposal#JavaCompletionProposal(java.lang.String, int, int,
 	 *      org.eclipse.swt.graphics.Image, java.lang.String, int)
 	 * @param replacementString
 	 * @param replacementOffset
 	 * @param replacementLength
 	 * @param image
 	 * @param displayString
 	 * @param relevance
 	 */
 	public AnnotationTagProposal(String replacementString, int replacementOffset, int replacementLength, Image image, String displayString, int relevance) {
 		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance);
 		this.localString = displayString;
 	}
 
 	/**
 	 * @see JavaCompletionProposal#JavaCompletionProposal(java.lang.String, int, int,
 	 *      org.eclipse.swt.graphics.Image, java.lang.String, int,
 	 *      org.eclipse.jface.text.ITextViewer)
 	 * @param replacementString
 	 * @param replacementOffset
 	 * @param replacementLength
 	 * @param image
 	 * @param displayString
 	 * @param relevance
 	 * @param viewer
 	 */
 	public AnnotationTagProposal(String replacementString, int replacementOffset, int replacementLength, Image image, String displayString, int relevance, ITextViewer viewer) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance);
 		this.localString = replacementString;
 	}
 
 	public AnnotationTagProposal(AttributeValueProposalHelper proposalHelper) {
 		this(proposalHelper.getReplacementString(), proposalHelper.getValueOffset(), proposalHelper.getReplacementLength(), null, proposalHelper.getValueDisplayString(), 1, null);
 		if (proposalHelper instanceof UIAttributeValueProposalHelper)
 			setImage(((UIAttributeValueProposalHelper) proposalHelper).getImage());
 		setEnsureBeginQuote(proposalHelper.ensureBeginQuote());
 		setEnsureEndQuote(proposalHelper.ensureEndQuote());
 	}
 
 	public AnnotationTagProposal(UIAttributeValueProposalHelper proposalHelper) {
 		this(proposalHelper.getReplacementString(), proposalHelper.getValueOffset(), proposalHelper.getReplacementLength(), proposalHelper.getImage(), proposalHelper.getValueDisplayString(), 1, null);
 		setEnsureBeginQuote(proposalHelper.ensureBeginQuote());
 		setEnsureEndQuote(proposalHelper.ensureEndQuote());
 	}
 
 	/**
 	 * Our override that uses <code>textHolder</code> to provide the help text.
 	 */
 	public String getAdditionalProposalInfo() {
 		return locText;
 	}
 
 	/**
 	 * Sets the holder of the help text that can be displayed with this proposal.
 	 * 
 	 * @param hld
 	 *            an LocalizedTextContainer
 	 */
 	public void setHelpText(String s) {
 		locText = s;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal#apply(org.eclipse.jface.text.IDocument,
 	 *      char, int)
 	 */
 	public void apply(IDocument document, char trigger, int offset) {
 		ensureQuotedIfNecessary(document, offset);
 		super.apply(document, trigger, offset);
 	}
 
 	/**
 	 * @param document
 	 * @param offset
 	 */
 	private void ensureQuotedIfNecessary(IDocument document, int offset) {
 		if (ensureBeginQuote || ensureEndQuote) {
 			try {
 				char begin = document.getChar(getReplacementOffset() - 1);
 				char end = document.getChar(offset);
 				if (ensureBeginQuote && ensureEndQuote && begin != QUOTE && end != QUOTE) {
 					StringBuffer b = new StringBuffer();
 					b.append(QUOTE).append(localString).append(QUOTE);
 					localString = b.toString();
 				} else if (ensureBeginQuote && begin != QUOTE)
 					localString = QUOTE + localString;
 				else if (ensureEndQuote && end != QUOTE)
 					localString = localString + QUOTE;
 				setReplacementString(localString);
 				setCursorPosition(localString.length());
 			} catch (BadLocationException e) {
 				// Do nothing
 			}
 		}
 	}
 
 	public void setEnsureQuoted(boolean ensureQuoted) {
 		setEnsureBeginQuote(ensureQuoted);
 		setEnsureEndQuote(ensureQuoted);
 	}
 
 	//public void setPartialValueString(String partialValueString) {
 	//	this.partialValueString = partialValueString;
 	//}
 	public void setEnsureBeginQuote(boolean ensureBeginQuote) {
 		this.ensureBeginQuote = ensureBeginQuote;
 	}
 
 	public void setEnsureEndQuote(boolean ensureEndQuote) {
 		this.ensureEndQuote = ensureEndQuote;
 	}
 }
