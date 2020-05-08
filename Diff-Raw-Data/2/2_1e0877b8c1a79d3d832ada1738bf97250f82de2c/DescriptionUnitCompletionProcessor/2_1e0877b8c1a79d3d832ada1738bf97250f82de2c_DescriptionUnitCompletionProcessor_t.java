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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.templates.TemplateProposal;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentDocumentProvider;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentPairMatcher;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 
 /**
  * Computes the completion proposals.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class DescriptionUnitCompletionProcessor extends AbstractIntentCompletionProcessor {
 
 	// Accurate contexts.
 
 	private static final int NULL_CONTEXT = -1;
 
 	private static final int DOCUMENT_CONTEXT = 0;
 
 	private static final int CHAPTER_CONTEXT = 1;
 
 	private static final int SECTION_CONTEXT = 2;
 
 	// Patterns by contexts.
 
 	private static final Pattern[] PATTERNS_BY_CONTEXT = new Pattern[] {Pattern.compile("Document\\s*\\{"),
 			Pattern.compile("Chapter\\s*\\{"), Pattern.compile("Section\\s*\\{"),
 	};
 
 	// Keywords by contexts.
 	// CHECKSTYLE:OFF Keywords have the same name than templates names & templates description, but merging
 	// their declaration have no sense.
 	private static final String[][] KEYWORDS_BY_CONTEXT = new String[][] {
 			// Document-level keywords
 			new String[] {"Chapter",
 			},
 			// Chapter-level keywords
 			new String[] {"Section",
 			},
 			// Section-level keywords
 			new String[] {"Section", "@M",
 			},
 	};
 
 	// CHECKSTYLE:ON
 
 	private int accurateContext;
 
 	private IntentPairMatcher blockMatcher;
 
 	/**
 	 * Creates a new {@link DescriptionUnitCompletionProcessor} with the given {@link IntentPairMatcher}.
 	 * 
 	 * @param matcher
 	 *            the block matcher
 	 */
 	public DescriptionUnitCompletionProcessor(IntentPairMatcher matcher) {
 		this.blockMatcher = matcher;
 	}
 
 	private void computeAccurateContext() throws BadLocationException {
 		int[] offsetsByContextType = new int[3];
 		final String startText = document.get(0, offset);
 		for (int i = 0; i < PATTERNS_BY_CONTEXT.length; i++) {
 			offsetsByContextType[i] = getLastIndexOf(startText, PATTERNS_BY_CONTEXT[i]);
 		}
 		int res = NULL_CONTEXT;
 		int maxValue = -1;
 		for (int i = 0; i < offsetsByContextType.length; i++) {
 			if (offsetsByContextType[i] > maxValue) {
 				IRegion region = blockMatcher.match(document, offsetsByContextType[i]);
 				if (region != null && region.getOffset() + region.getLength() > offset) {
 					maxValue = offsetsByContextType[i];
 					res = i;
 				}
 			}
 		}
 		accurateContext = res;
 	}
 
 	private int getLastIndexOf(String text, Pattern pattern) {
 		Matcher matcher = pattern.matcher(text);
 		int end = -1;
 		while (matcher.find()) {
 			end = matcher.end();
 		}
 		return end;
 	}
 
 	/**
 	 * Computes the completion proposals.
 	 * 
 	 * @return the completion proposals
 	 */
 	protected ICompletionProposal[] computeCompletionProposals() {
 		try {
 			computeAccurateContext();
 		} catch (BadLocationException e) {
 			IntentUiLogger.logError(e);
 		}
 		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		proposals.addAll(createKeyWordsProposals());
 		proposals.addAll(createTemplatesProposals());
 		return proposals.toArray(new ICompletionProposal[proposals.size()]);
 	}
 
 	/**
 	 * Creates the keywords proposals.
 	 * 
 	 * @return the keywords proposals
 	 */
 	protected Collection<ICompletionProposal> createKeyWordsProposals() {
 		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		if (accurateContext >= 0) {
 			for (String keyword : KEYWORDS_BY_CONTEXT[accurateContext]) {
 				if (!"".equals(start) && keyword.startsWith(start)) {
 					proposals.add(createKeyWordProposal(keyword));
 				}
 			}
 		}
 		return proposals;
 	}
 
 	/**
 	 * Creates the templates proposals.
 	 * 
 	 * @return the templates proposals
 	 */
 	protected List<TemplateProposal> createTemplatesProposals() {
 		TemplateProposal chapterProposal = createTemplateProposal("Chapter", "Chapter",
 				"Chapter {\n\t${Title}\n\t${Text}\n}\n", "icon/outline/chapter.gif");
 		TemplateProposal sectionProposal = createTemplateProposal("Section", "Section",
 				"Section {\n\t${Title}\n\t${Text}\n}\n", "icon/outline/section.gif");
		TemplateProposal modelingUnitProposal = createTemplateProposal("Modeling Unit", "Modeling Unit",
 				"@M\n${Code}\nM@\n", "icon/outline/modelingunit.png");
 
 		List<TemplateProposal> proposals = new ArrayList<TemplateProposal>();
 
 		switch (accurateContext) {
 			case DOCUMENT_CONTEXT:
 				proposals.add(chapterProposal);
 				break;
 			case CHAPTER_CONTEXT:
 				proposals.add(sectionProposal);
 				break;
 			case SECTION_CONTEXT:
 				proposals.add(sectionProposal);
 				proposals.add(modelingUnitProposal);
 				break;
 			default:
 				break;
 		}
 
 		return proposals;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.completion.AbstractIntentCompletionProcessor#getContextType()
 	 */
 	@Override
 	public String getContextType() {
 		return IntentDocumentProvider.INTENT_DESCRIPTIONUNIT;
 	}
 }
