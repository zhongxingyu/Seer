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
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.templates.DocumentTemplateContext;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateContext;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateProposal;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentDocumentProvider;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Computes the completion proposals.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class DescriptionUnitCompletionProcessor extends AbstractIntentCompletionProcessor {
 
 	private static final Pattern SECTION_PATTERN = Pattern.compile("Section\\s*\\{");
 
 	private static final Pattern CHAPTER_PATTERN = Pattern.compile("Chapter\\s*\\{");
 
 	private static final Pattern DOCUMENT_PATTERN = Pattern.compile("Document\\s*\\{");
 
 	private static final int DOCUMENT_CONTEXT = 0;
 
 	private static final int CHAPTER_CONTEXT = 1;
 
 	private static final int SECTION_CONTEXT = 2;
 
 	private int accurateContext;
 
 	/**
 	 * Computes the completion proposals.
 	 * 
 	 * @return the completion proposals
 	 */
 	protected ICompletionProposal[] computeCompletionProposals() {
 		computeAccurateContext();
 
 		// get the currently typed word
 		int index = offset;
 		while (index > 0 && Character.isJavaIdentifierPart(text.charAt(index - 1))) {
 			index--;
 		}
 		String start = text.substring(index, offset);
 
 		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		proposals.addAll(createTemplatesProposals(start));
 		// TODO keyword proposals
 		return proposals.toArray(new ICompletionProposal[proposals.size()]);
 	}
 
 	private void computeAccurateContext() {
 		int[] offsetsByContextType = new int[3];
 		final String startText = text.substring(0, offset);
 		offsetsByContextType[DOCUMENT_CONTEXT] = getLastIndexOf(startText, DOCUMENT_PATTERN);
 		offsetsByContextType[CHAPTER_CONTEXT] = getLastIndexOf(startText, CHAPTER_PATTERN);
 		offsetsByContextType[SECTION_CONTEXT] = getLastIndexOf(startText, SECTION_PATTERN);
		// TODO improve with pair matcher
 		int res = DOCUMENT_CONTEXT;
 		int maxValue = offsetsByContextType[0];
 		for (int i = 1; i < offsetsByContextType.length; i++) {
 			if (offsetsByContextType[i] > maxValue) {
 				maxValue = offsetsByContextType[i];
 				res = i;
 			}
 		}
 		accurateContext = res;
 	}
 
 	private int getLastIndexOf(String text, Pattern pattern) {
 		Matcher matcher = pattern.matcher(text);
 		if (matcher.find()) {
 			return matcher.end();
 		}
 		return -1;
 	}
 
 	private List<TemplateProposal> createTemplatesProposals(String start) {
 
 		TemplateProposal chapterProposal = createTemplateProposal(start.length(), "Chapter", "Chapter",
 				"Chapter {\n\t${Title}\n\t${Text}\n}\n", "icon/outline/chapter.gif");
 		TemplateProposal sectionProposal = createTemplateProposal(start.length(), "Section", "Section",
 				"Section {\n\t${Title}\n\t${Text}\n}\n", "icon/outline/section.gif");
 		TemplateProposal modelingUnitProposal = createTemplateProposal(start.length(), "Modeling Unit",
 				"Moduling Unit", "@M\n${Code}\nM@\n", "icon/outline/modelingunit.png");
 
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
 
 	private TemplateProposal createTemplateProposal(int startLength, String templateName,
 			String templateDescription, String templatePattern, String templateImagePath) {
 		Template template = new Template(templateName, templateDescription,
 				IntentDocumentProvider.INTENT_DESCRIPTIONUNIT, templatePattern, true);
 		TemplateContextType type = new TemplateContextType(IntentDocumentProvider.INTENT_DESCRIPTIONUNIT,
 				IntentDocumentProvider.INTENT_DESCRIPTIONUNIT);
 		TemplateContext context = new DocumentTemplateContext(type, textViewer.getDocument(), offset
 				- startLength, startLength);
 		Region region = new Region(offset - startLength, startLength);
 		Image image = IntentEditorActivator.getDefault().getImage(templateImagePath);
 		final TemplateProposal templateProposal = new TemplateProposal(template, context, region, image);
 		return templateProposal;
 	}
 }
