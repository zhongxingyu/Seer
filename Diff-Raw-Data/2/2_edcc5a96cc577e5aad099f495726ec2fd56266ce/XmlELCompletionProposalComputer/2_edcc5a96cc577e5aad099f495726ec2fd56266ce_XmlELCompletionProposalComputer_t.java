 /*******************************************************************************
  * Copyright (c) 2010 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.contentassist.computers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.dtd.core.internal.contentmodel.DTDImpl.DTDBaseAdapter;
 import org.eclipse.wst.dtd.core.internal.contentmodel.DTDImpl.DTDElementReferenceContentAdapter;
 import org.eclipse.wst.html.core.internal.contentmodel.HTMLPropertyDeclaration;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentModelGenerator;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
 import org.jboss.tools.common.el.core.ca.ELTextProposal;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor.TextRegion;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.jst.jsp.contentassist.AutoELContentAssistantProposal;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * EL Proposal computer for XML pages
  * 
  * @author Jeremy
  *
  */
 @SuppressWarnings("restriction")
 public class XmlELCompletionProposalComputer extends AbstractXmlCompletionProposalComputer {
 	protected static final ICompletionProposal[] EMPTY_PROPOSAL_LIST = new ICompletionProposal[0];
 	private static final String[] EMPTY_TAGS = new String[0];
 	protected static final Image JSF_EL_PROPOSAL_IMAGE = JspEditorPlugin.getDefault().getImage(JspEditorPlugin.CA_JSF_EL_IMAGE_PATH);
 
 	@Override
 	protected XMLContentModelGenerator getContentGenerator() {
 		return 	new XMLContentModelGenerator();
 	}
 
 	@Override
 	protected boolean validModelQueryNode(CMNode node) {
 		boolean isValid = false;
 		if(node instanceof DTDElementReferenceContentAdapter) {
 			DTDElementReferenceContentAdapter content = (DTDElementReferenceContentAdapter)node;
 			if(content.getCMDocument() instanceof DTDBaseAdapter) {
 				DTDBaseAdapter dtd = (DTDBaseAdapter)content.getCMDocument();
 				//this maybe a little hacky, but it works, if you have a better idea go for it
 				String spec = dtd.getSpec();
 				isValid = spec.indexOf("html") != -1; //$NON-NLS-1$
 			}
 		} else if(node instanceof HTMLPropertyDeclaration) {
 			HTMLPropertyDeclaration propDec = (HTMLPropertyDeclaration)node;
 			isValid = !propDec.isJSP();
 		} else if (node instanceof CMAttributeDeclaration) {
 			isValid = true;
 		}
 		return isValid;
 	}
 
 	
 	/**
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition, CompletionProposalInvocationContext context) {
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix != null && prefix.isELStarted()) {
 			addTextELProposals(contentAssistRequest, context);
 		} else {
 			addELPredicateProposals(contentAssistRequest, getTagInsertionBaseRelevance(), true);
 		}
 	}
 	
 	@Override
 	protected void addAttributeValueProposals(
 			ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		
 		fCurrentContext = context;
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix != null && prefix.isELStarted()) {
 			addAttributeValueELProposals(contentAssistRequest, context);
 			return;
 		}
 
 		addELPredicateProposals(contentAssistRequest, TextProposal.R_JSP_ATTRIBUTE_VALUE, false);
 	}
 
 	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition,
 			CompletionProposalInvocationContext context) {
 	}
 
 	@SuppressWarnings("unused")
 	@Override
 	protected void addAttributeValueELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		
 		if (!isELCAToBeShown())
 			return;
 		
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix == null) {
 			return;
 		}
 
 		if(!prefix.isELStarted()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, 
 					"#{}" + (prefix.isAttributeValue() && prefix.hasOpenQuote() && !prefix.hasCloseQuote() ? String.valueOf(prefix.getQuoteChar()) : ""), //$NON-NLS-1$ //$NON-NLS-2$
 					getOffset(), 0, 2, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_NewELExpression, 
 					null, JstUIMessages.JspContentAssistProcessor_NewELExpressionAttrInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 
 			contentAssistRequest.addProposal(proposal);
 			return;
 		}
 		String matchString = "#{" + prefix.getText(); //$NON-NLS-1$
 		String query = matchString;
 		if (query == null)
 			query = ""; //$NON-NLS-1$
 		String stringQuery = matchString;
 		
 		int beginChangeOffset = prefix.getStartOffset() + prefix.getOffset();
 				
 		KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, query, stringQuery);
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 
 		if (proposals == null || proposals.length == 0)
 			return;
 
 		for (TextProposal textProposal : proposals) {
 			int replacementOffset = beginChangeOffset;
 			int replacementLength = prefix.getLength();
 			String replacementString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			
 			char quoteChar = prefix.isAttributeValue() && prefix.hasOpenQuote() ? prefix.getQuoteChar() : '"';
 //			if (prefix.isAttributeValue() && !prefix.hasOpenQuote()) {
 //				replacementString = String.valueOf(quoteChar) + replacementString;
 //			}
 			int cursorPosition = replacementString.length();
 			
 			if (!prefix.isELClosed()) {
 				replacementString += "}"; //$NON-NLS-1$
 			}
 			
 			if (prefix.isAttributeValue() && prefix.hasOpenQuote() && !prefix.hasCloseQuote()) {
 				replacementString += String.valueOf(quoteChar);
 			}
 									
 			Image image = textProposal.getImage();
 			
 			// JBIDE-512, JBIDE-2541 related changes ===>>>
 //				String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			String displayString = textProposal.getLabel();
 			if (displayString == null)
 				displayString = textProposal.getReplacementString() == null ? replacementString : textProposal.getReplacementString();
 			// <<<=== JBIDE-512, JBIDE-2541 related changes
 
 			int relevance = textProposal.getRelevance();
 			if (relevance == TextProposal.R_NONE) {
 				relevance = TextProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 			}
 
 			AutoContentAssistantProposal proposal = null;
 			if (textProposal instanceof ELTextProposal) {
 				IJavaElement[] javaElements = ((ELTextProposal)textProposal).getAllJavaElements();
 	
 				proposal = new AutoELContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						null, javaElements, relevance);
 			} else {
 				String additionalProposalInfo = (textProposal.getContextInfo() == null ? "" : textProposal.getContextInfo()); //$NON-NLS-1$
 
 				proposal = new AutoContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						null, additionalProposalInfo, relevance);
 			}
 			contentAssistRequest.addProposal(proposal);
 		}
 
 		if (prefix.isELStarted() && !prefix.isELClosed()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(
 					"}" + (prefix.isAttributeValue() && prefix.hasOpenQuote() && !prefix.hasCloseQuote() ? String.valueOf(prefix.getQuoteChar()) : ""), //$NON-NLS-1$ //$NON-NLS-2$
 					getOffset(), 0, 0, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_CloseELExpression, 
 					null, JstUIMessages.JspContentAssistProcessor_CloseELExpressionInfo, TextProposal.R_XML_ATTRIBUTE_VALUE + 1); //
 
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		if (!isELCAToBeShown())
 			return;
 		
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix == null || !prefix.isELStarted()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, "#{}", //$NON-NLS-1$ 
 					contentAssistRequest.getReplacementBeginPosition(), 
 					0, 2, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_NewELExpression, null, 
 					JstUIMessages.FaceletPageContectAssistProcessor_NewELExpressionTextInfo, TextProposal.R_TAG_INSERTION + 1);
 			
 			contentAssistRequest.addProposal(proposal);
 			return;
 		}
 		String matchString = "#{" + prefix.getText(); //$NON-NLS-1$
 		String query = matchString;
 		if (query == null)
 			query = ""; //$NON-NLS-1$
 		String stringQuery = matchString;
 
 		int beginChangeOffset = prefix.getStartOffset() + prefix.getOffset();
 
 		KbQuery kbQuery = createKbQuery(Type.TEXT, query, stringQuery);
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 
 		if (proposals == null || proposals.length == 0)
 			return;
 		
 		for (TextProposal textProposal : proposals) {
 			int replacementOffset = beginChangeOffset;
 			int replacementLength = prefix.getLength();
 			String replacementString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			int cursorPosition = replacementString.length();
 			
 			if (!prefix.isELClosed()) {
 				replacementString += "}"; //$NON-NLS-1$
 			}
 
 			Image image = textProposal.getImage();
 
 			// JBIDE-512, JBIDE-2541 related changes ===>>>
 //			String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			String displayString = textProposal.getLabel();
 			if (displayString == null)
 				displayString = textProposal.getReplacementString() == null ? replacementString : textProposal.getReplacementString();
 
 			// <<<=== JBIDE-512, JBIDE-2541 related changes
 			int relevance = textProposal.getRelevance();
 			if (relevance == TextProposal.R_NONE) {
 				relevance = TextProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 			}
 
 			AutoContentAssistantProposal proposal = null;
 			if (textProposal instanceof ELTextProposal) {
 				IJavaElement[] javaElements = ((ELTextProposal)textProposal).getAllJavaElements();
 	
 				proposal = new AutoELContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						null, javaElements, relevance);
 			} else {
 				String additionalProposalInfo = (textProposal.getContextInfo() == null ? "" : textProposal.getContextInfo()); //$NON-NLS-1$
 
 				proposal = new AutoContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						null, additionalProposalInfo, relevance);
 			}
 			contentAssistRequest.addProposal(proposal);
 		}
 
 		if (prefix.isELStarted() && !prefix.isELClosed()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal("}", //$NON-NLS-1$
 					getOffset(), 0, 1, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_CloseELExpression, 
 					null, JstUIMessages.JspContentAssistProcessor_CloseELExpressionInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 
 	/**
 	 * Calculates and adds EL predicate proposals based on the last word typed
 	 * To be used only outside the EL.
 	 * 
 	 * @param contentAssistRequest
 	 */
 	@SuppressWarnings("unused")
 	protected void addELPredicateProposals(ContentAssistRequest contentAssistRequest, int baseRelevance, boolean shiftRelevanceAgainstTagNameProposals) {
 		if (!isELCAToBeShown())
 			return;
 		
 		// Need to check if the cursor is placed right after a word part.
 		// If there is no word part found then just quit
 		TextRegion prefix = getELPredicatePrefix(contentAssistRequest);
 		if (prefix == null || prefix.isELStarted()) {
 			return;
 		}
 		String matchString = "#{" + prefix.getText(); //$NON-NLS-1$
 		String query = matchString;
 		if (query == null)
 			query = ""; //$NON-NLS-1$
 		String stringQuery = matchString;
 		int relevanceShift = -2; // Fix for JBIDE-5987: Relevance for predicate proposals is shifted down by default to show EL proposals lower than attr-value proposals 
 		if (shiftRelevanceAgainstTagNameProposals) {
			relevanceShift += prefix.getText() != null && prefix.getText().trim().length() > 0 ? 0 : -2;
 		}
 		
 		int beginChangeOffset = prefix.getStartOffset() + prefix.getOffset();
 
 		KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, query, stringQuery);
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 		
 		if (proposals == null || proposals.length == 0)
 			return;
 
 		for (TextProposal textProposal : proposals) {
 			int replacementOffset = beginChangeOffset;
 			int replacementLength = prefix.getLength();
 			String replacementString = "#{" + prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();  //$NON-NLS-1$
 			
 			char quoteChar = prefix.isAttributeValue() && prefix.hasOpenQuote() ? prefix.getQuoteChar() : '"';
 			int cursorPosition = replacementString.length();
 			
 			if (!prefix.isELClosed()) {
 				replacementString += "}"; //$NON-NLS-1$
 			}
 			
 			if (prefix.isAttributeValue() && prefix.hasOpenQuote() && !prefix.hasCloseQuote()) {
 				replacementString += String.valueOf(quoteChar);
 			}
 									
 			Image image = textProposal.getImage();
 			
 			// JBIDE-512, JBIDE-2541 related changes ===>>>
 //				String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			String displayString = textProposal.getLabel();
 			if (displayString == null)
 				displayString = textProposal.getReplacementString() == null ? replacementString : textProposal.getReplacementString();
 			// <<<=== JBIDE-512, JBIDE-2541 related changes
 
 			int relevance = textProposal.getRelevance();
 			if (relevance == TextProposal.R_NONE) {
 				relevance = baseRelevance; 
 			}
 			relevance += relevanceShift;
 
 			AutoContentAssistantProposal proposal = null;
 			if (textProposal instanceof ELTextProposal) {
 				IJavaElement[] javaElements = ((ELTextProposal)textProposal).getAllJavaElements();
 	
 				proposal = new AutoELContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						null, javaElements, relevance);
 			} else {
 				String additionalProposalInfo = (textProposal.getContextInfo() == null ? "" : textProposal.getContextInfo()); //$NON-NLS-1$
 
 				proposal = new AutoContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						null, additionalProposalInfo, relevance);
 			}
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 
 	/**
 	 * Checks is we need to show EL proposals
 	 * 
 	 * @return
 	 */
 	protected boolean isELCAToBeShown() {
 		ELResolver[] resolvers =  getContext().getElResolvers();
 		return (resolvers != null && resolvers.length > 0);
 	}
 
 	protected ELContext createContext() {
 		return PageContextFactory.createPageContext(getResource(), PageContextFactory.XML_PAGE_CONTEXT_TYPE);
 	}
 	
 	protected KbQuery createKbQuery(Type type, String query, String stringQuery) {
 		return createKbQuery(type, query, stringQuery, getTagPrefix(), getTagUri());
 	}
 
 	protected KbQuery createKbQuery(Type type, String query, String stringQuery, String prefix, String uri) {
 		KbQuery kbQuery = new KbQuery();
 
 		String[] parentTags = getParentTags(type == Type.ATTRIBUTE_NAME || type == Type.ATTRIBUTE_VALUE);
 		String	parent = getParent(type == Type.ATTRIBUTE_VALUE, type == Type.ATTRIBUTE_NAME);
 		String queryValue = query;
 		String queryStringValue = stringQuery;
 		
 		kbQuery.setPrefix(prefix);
 		kbQuery.setUri(uri);
 		kbQuery.setParentTags(parentTags);
 		kbQuery.setParent(parent); 
 		kbQuery.setMask(true); 
 		kbQuery.setType(type);
 		kbQuery.setOffset(fCurrentContext.getInvocationOffset());
 		kbQuery.setValue(queryValue); 
 		kbQuery.setStringQuery(queryStringValue);
 		
 		return kbQuery;
 	}
 
 	/**
 	 * this is the position the cursor should be in after the proposal is
 	 * applied
 	 * 
 	 * @param proposedText
 	 * @return the position the cursor should be in after the proposal is
 	 *         applied
 	 */
 	protected static int getCursorPositionForProposedText(String proposedText) {
 		int cursorAdjustment;
 		cursorAdjustment = proposedText.indexOf("\"\"") + 1; //$NON-NLS-1$
 		// otherwise, after the first tag
 		if (cursorAdjustment == 0) {
 			cursorAdjustment = proposedText.indexOf('>') + 1;
 		}
 		if (cursorAdjustment == 0) {
 			cursorAdjustment = proposedText.length();
 		}
 
 		return cursorAdjustment;
 	}
 
 	/**
 	 * Returns array of the <code>org.jboss.tools.common.el.core.resolver.ELResolver</code> 
 	 * instances.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	protected ELResolver[] getELResolvers(IResource resource) {
 		if (resource == null)
 			return null;
 		
 		ELResolverFactoryManager elrfm = ELResolverFactoryManager.getInstance();
 		return elrfm.getResolvers(resource);
 	}
 	
 	/**
 	 * Returns array of the parent tags 
 	 * 
 	 * @return
 	 */
 	public String[] getParentTags(boolean includeThisTag) {
 		List<String> parentTags = new ArrayList<String>();
 		
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null) 
 				return EMPTY_TAGS;
 			
 			Document xmlDocument = (sModel instanceof IDOMModel) 
 					? ((IDOMModel) sModel).getDocument()
 							: null;
 
 			if (xmlDocument == null)
 				return EMPTY_TAGS;
 			
 			
 			Node n = null;
 			if (includeThisTag) {
 				n = findNodeForOffset(xmlDocument, getOffset());
 			} else {
 				// Get Fixed Structured Document Region
 				IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 				if (sdFixedRegion == null)
 					return EMPTY_TAGS;
 				
 				n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
 			}
 			if (n == null)
 				return EMPTY_TAGS;
 
 			// Find the first parent tag 
 			if (!(n instanceof Element)) {
 				if (n instanceof Attr) {
 					n = ((Attr) n).getOwnerElement();
 				} else {
 					n = n.getParentNode();
 				}
 			} else if (!includeThisTag) {
 				n = n.getParentNode();
 			}
 
 			// Store all the parents
 			while (n != null && n instanceof Element) {
 				String tagName = getTagName(n);
 				parentTags.add(0, tagName);
 				n = n.getParentNode();
 			}	
 
 			return (String[])parentTags.toArray(new String[parentTags.size()]);
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	protected String getTagName(Node tag) {
 		return tag.getNodeName();
 	}
 
 	/**
 	 * Returns name of the parent attribute/tag name
 	 * 
 	 * @return
 	 */
 	protected String getParent(boolean returnAttributeName, boolean returnThisElement) {
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null) 
 				return null;
 			
 			Document xmlDocument = (sModel instanceof IDOMModel) 
 					? ((IDOMModel) sModel).getDocument()
 							: null;
 
 			if (xmlDocument == null)
 				return null;
 			
 			Node n = null;
 			if (returnAttributeName) {
 				n = findNodeForOffset(xmlDocument, getOffset());
 			} else {
 				// Get Fixed Structured Document Region
 				IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 				if (sdFixedRegion == null)
 					return null;
 				
 				n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
 			}
 			
 			if (n == null)
 				return null;
 
 			// Find the first parent tag 
 			if (!(n instanceof Element)) {
 				if (n instanceof Attr) {
 					if (returnAttributeName) {
 						String parentAttrName = n.getNodeName();
 						return parentAttrName;
 					}
 					n = ((Attr) n).getOwnerElement();
 				} else {
 					n = n.getParentNode();
 				}
 			} else {
 				if (!returnThisElement)
 					n = n.getParentNode();
 			}
 			if (n == null)
 				return null;
 
 			String parentTagName = getTagName(n);
 			return parentTagName;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	/**
 	 * Returns URI for the current/parent tag
 	 * @return
 	 */
 	public String getTagPrefix() {
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null) 
 				return null;
 			
 			Document xmlDocument = (sModel instanceof IDOMModel) 
 					? ((IDOMModel) sModel).getDocument()
 							: null;
 
 			if (xmlDocument == null)
 				return null;
 			
 			// Get Fixed Structured Document Region
 			IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 			if (sdFixedRegion == null)
 				return null;
 			
 			Node n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
 			if (n == null)
 				return null;
 
 			
 			if (!(n instanceof Element) && !(n instanceof Attr))
 				return null;
 			
 			if (n instanceof Attr) {
 				n = ((Attr) n).getOwnerElement();
 			}
 
 			if (n == null)
 				return null;
 
 			String nodePrefix = ((Element)n).getPrefix();
 			return nodePrefix;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	/**
 	 * Returns URI for the current/parent tag
 	 * @return
 	 */
 	public String getTagUri() {
 		String nodePrefix = getTagPrefix();
 		return getUri(nodePrefix);
 	}
 
 	/**
 	 * Returns URI string for the prefix specified using the namespaces collected for 
 	 * the {@link IPageContext} context.
 	 * 
 	 * 	@Override org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#getUri(String)
 	 */
 	protected String getUri(String prefix) {
 		return null;
 	}
 
 	/**
 	 * Returns the document position where the CA is invoked
 	 * @return
 	 */
 	protected int getOffset() {
 		return fCurrentContext.getInvocationOffset();
 	}
 
 	
 	/**
 	 * Returns the document
 	 * 
 	 * @return
 	 */
 	protected IDocument getDocument() {
 		return fCurrentContext.getDocument();
 	}
 
 	/**
 	 * Returns IFile resource of the document
 	 * 
 	 * @return
 	 */
 	protected IFile getResource() {
 		IStructuredModel sModel = StructuredModelManager.getModelManager().getExistingModelForRead(getDocument());
 		try {
 			if (sModel != null) {
 				String baseLocation = sModel.getBaseLocation();
 				IPath location = new Path(baseLocation).makeAbsolute();
 				return FileBuffers.getWorkspaceFileAtLocation(location);
 			}
 		}
 		finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the <code>org.jboss.tools.common.el.core.resolver.ELContext</code> instance
 	 * 
 	 * @return
 	 */
 	protected ELContext getContext() {
 		return this.fContext;
 	}
 
 
 	/**
 	 * Returns EL Prefix Text Region Information Object
 	 * 
 	 * @return
 	 */
 	protected TextRegion getELPrefix(ContentAssistRequest request) {
 		if (request == null || request.getRegion() == null)
 			return null;
 
 		IStructuredDocumentRegion documentRegion = request.getDocumentRegion();
 		ITextRegion completionRegion = request.getRegion();
 		String regionType = completionRegion.getType();
 		
 		if (DOMRegionContext.XML_END_TAG_OPEN.equals(regionType) || DOMRegionContext.XML_TAG_OPEN.equals(regionType)) {
 			documentRegion = documentRegion.getPrevious();
 			completionRegion = getCompletionRegion(request.getDocumentRegion().getStartOffset() + request.getRegion().getStart() - 1, request.getParent());
 		}
 		if (!DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(completionRegion.getType()) &&
 				!DOMRegionContext.XML_CONTENT.equals(completionRegion.getType()) &&
 				!DOMRegionContext.BLOCK_TEXT.equals(completionRegion.getType())) {
 				return null;
 		}
 		String text = documentRegion.getFullText(completionRegion);
 		int startOffset = documentRegion.getStartOffset() + completionRegion.getStart();
 
 		boolean isAttributeValue = false;
 		boolean hasOpenQuote = false;
 		boolean hasCloseQuote = false;
 		char quoteChar = (char)0;
 		if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(request.getRegion().getType())) {
 			isAttributeValue = true;
 			if (text.startsWith("\"") || text.startsWith("'")) {//$NON-NLS-1$ //$NON-NLS-2$
 				quoteChar = text.charAt(0);
 				hasOpenQuote = true;
 			}
 			if (hasOpenQuote && text.trim().endsWith(String.valueOf(quoteChar))) {
 				hasCloseQuote = true;
 			}
 		}
 		
 		int inValueOffset = getOffset() - startOffset;
 		if (text != null && text.length() < inValueOffset) { // probably, the attribute value ends before the document position
 			return null;
 		}
 		if (inValueOffset<0) {
 			return null;
 		}
 		
 //			String matchString = text.substring(0, inValueOffset);
 		
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInstance is = ELUtil.findInstance(model, inValueOffset);// ELInstance
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		
 		boolean isELStarted = (model != null && is != null && (model.toString().startsWith("#{") ||  //$NON-NLS-1$
 				model.toString().startsWith("${"))); //$NON-NLS-1$
 		boolean isELClosed = (model != null && is != null && model.toString().endsWith("}")); //$NON-NLS-1$
 		
 //			boolean insideEL = startOffset + model.toString().length() 
 		TextRegion tr = new TextRegion(startOffset,  ie == null ? inValueOffset : ie.getStartPosition(), 
 				ie == null ? 0 : inValueOffset - ie.getStartPosition(), ie == null ? "" : ie.getText(),  //$NON-NLS-1$ 
 				isELStarted, isELClosed,
 				isAttributeValue, hasOpenQuote, hasCloseQuote, quoteChar);
 		
 		return tr;
 	}
 	
 	/**
 	 * Returns EL Predicate Text Region Information Object
 	 * 
 	 * 
 	 * @return
 	 */
 	protected TextRegion getELPredicatePrefix(ContentAssistRequest request) {
 		if (request == null || request.getRegion() == null)
 			return null;
 
 		IStructuredDocumentRegion documentRegion = request.getDocumentRegion();
 		ITextRegion completionRegion = request.getRegion();
 		String regionType = completionRegion.getType();
 		
 		if (DOMRegionContext.XML_END_TAG_OPEN.equals(regionType) || DOMRegionContext.XML_TAG_OPEN.equals(regionType)) {
 			documentRegion = documentRegion.getPrevious();
 			completionRegion = getCompletionRegion(request.getDocumentRegion().getStartOffset() + request.getRegion().getStart() - 1, request.getParent());
 		}
 		if (!DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(completionRegion.getType()) &&
 				!DOMRegionContext.XML_CONTENT.equals(completionRegion.getType()) &&
 				!DOMRegionContext.BLOCK_TEXT.equals(completionRegion.getType())) {
 				return null;
 		}
 		String text = documentRegion.getFullText(completionRegion);
 		int startOffset = documentRegion.getStartOffset() + completionRegion.getStart();
 		
 		boolean isAttributeValue = false;
 		boolean hasOpenQuote = false;
 		boolean hasCloseQuote = false;
 		char quoteChar = (char)0;
 		if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(request.getRegion().getType())) {
 			isAttributeValue = true;
 			if (text.startsWith("\"") || text.startsWith("'")) {//$NON-NLS-1$ //$NON-NLS-2$
 				quoteChar = text.charAt(0);
 				hasOpenQuote = true;
 			}
 			if (hasOpenQuote && text.trim().endsWith(String.valueOf(quoteChar))) {
 				hasCloseQuote = true;
 			}
 		}
 		
 		int inValueOffset = getOffset() - startOffset;
 		if (inValueOffset<0 || // There is no a word part before cursor 
 				(text != null && text.length() < inValueOffset)) { // probably, the attribute value ends before the document position
 			return null;
 		}
 
 		String matchString = getELPredicateMatchString(text, inValueOffset);
 		if (matchString == null)
 			return null;
 		
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInstance is = ELUtil.findInstance(model, inValueOffset);// ELInstance
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		
 		boolean isELStarted = (model != null && is != null && (model.toString().startsWith("#{") ||  //$NON-NLS-1$
 				model.toString().startsWith("${"))); //$NON-NLS-1$
 		boolean isELClosed = (model != null && is != null && model.toString().endsWith("}")); //$NON-NLS-1$
 
 		TextRegion tr = new TextRegion(startOffset, getOffset() - matchString.length() - startOffset, 
 				matchString.length(), matchString, isELStarted, isELClosed,
 				isAttributeValue, hasOpenQuote, hasCloseQuote, quoteChar);
 		
 		return tr;
 	}
 
 	/**
 	 * Returns predicate string for the EL-related query. 
 	 * The predicate string is the word/part of word right before the cursor position, including the '.' and '_' characters, 
 	 * which is to be replaced by the EL CA proposal ('#{' and '}' character sequences are to be inserted too)
 	 *  
 	 * @param text
 	 * @param offset
 	 * @return
 	 */
 	protected String getELPredicateMatchString(String text, int offset) {
 		int beginningOffset = offset - 1;
 		while(beginningOffset >=0 && 
 				(Character.isJavaIdentifierPart(text.charAt(beginningOffset)) ||
 						'.' == text.charAt(beginningOffset) ||
 						'_' == text.charAt(beginningOffset))) {
 			beginningOffset--;
 		}
 		beginningOffset++; // move it to point the first valid character
 		return text.substring(beginningOffset, offset);
 	}
 
 	/**
 	 * The reason of overriding is that the method returns wrong region in case of incomplete tag (a tag with no '>'-closing char)
 	 * In this case we have to return that previous incomplete tag instead of the current tag)
 	 */
 	public IStructuredDocumentRegion getStructuredDocumentRegion(int pos) {
 		IStructuredDocumentRegion sdRegion = null;
 
 		int lastOffset = pos;
 		IStructuredDocument doc = (IStructuredDocument) getDocument();
 		if (doc == null)
 			return null;
 
 		do {
 			sdRegion = doc.getRegionAtCharacterOffset(lastOffset);
 			if (sdRegion != null) {
 				ITextRegion region = sdRegion.getRegionAtCharacterOffset(lastOffset);
 				if (region != null && region.getType() == DOMRegionContext.XML_TAG_OPEN &&  
 						sdRegion.getStartOffset(region) == lastOffset) {
 					// The offset is at the beginning of the region
 					if ((sdRegion.getStartOffset(region) == sdRegion.getStartOffset()) && (sdRegion.getPrevious() != null) && (!sdRegion.getPrevious().isEnded())) {
 						// Is the region also the start of the node? If so, the
 						// previous IStructuredDocumentRegion is
 						// where to look for a useful region.
 //						sdRegion = sdRegion.getPrevious();
 						sdRegion = null;
 					}
 					else {
 						// Is there no separating whitespace from the previous region?
 						// If not,
 						// then that region is the important one
 						ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(lastOffset - 1);
 						if ((previousRegion != null) && (previousRegion != region) && (previousRegion.getTextLength() == previousRegion.getLength())) {
 //							sdRegion = sdRegion.getPrevious();
 							sdRegion = null;
 						}
 					}
 				}
 			}
 			lastOffset--;
 		} while (sdRegion == null && lastOffset >= 0);
 		return sdRegion;
 	}
 	
 	/**
 	 * The reason of overriding is that the method returns wrong region in case of incomplete tag (a tag with no '>'-closing char)
 	 * In this case we have to return that previous incomplete tag instead of the current tag)
 	 */
 	protected ITextRegion getCompletionRegion(int documentPosition, Node domnode) {
 		if (domnode == null) {
 			return null;
 		}
 		// Get the original WTP Structured Document Region
 		IStructuredDocumentRegion sdNormalRegion = ContentAssistUtils.getStructuredDocumentRegion(fCurrentContext.getViewer(), documentPosition);
 		// Get Fixed Structured Document Region
 		IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(documentPosition);
 
 		// If original and fixed regions are different we have to replace domnode with its parent node
 		if (sdFixedRegion != null && !sdFixedRegion.equals(sdNormalRegion)) {
 			Node prevnode = domnode.getParentNode();
 			if (prevnode != null) {
 				domnode = prevnode;
 			}
 		}
 	
 		return getSuperCompletionRegion(documentPosition, domnode);
 	}
 	
 	/**
 	 * Return the region whose content's require completion. This is something
 	 * of a misnomer as sometimes the user wants to be prompted for contents
 	 * of a non-existant ITextRegion, such as for enumerated attribute values
 	 * following an '=' sign.
 	 */
 	private ITextRegion getSuperCompletionRegion(int documentPosition, Node domnode) {
 		if (domnode == null) {
 			return null;
 		}
 
 		ITextRegion region = null;
 		int offset = documentPosition;
 		IStructuredDocumentRegion flatNode = null;
 		IDOMNode node = (IDOMNode) domnode;
 
 		if (node.getNodeType() == Node.DOCUMENT_NODE) {
 			if (node.getStructuredDocument().getLength() == 0) {
 				return null;
 			}
 			ITextRegion result = node.getStructuredDocument().getRegionAtCharacterOffset(offset).getRegionAtCharacterOffset(offset);
 			while (result == null) {
 				offset--;
 				result = node.getStructuredDocument().getRegionAtCharacterOffset(offset).getRegionAtCharacterOffset(offset);
 			}
 			return result;
 		}
 
 		IStructuredDocumentRegion startTag = node.getStartStructuredDocumentRegion();
 		IStructuredDocumentRegion endTag = node.getEndStructuredDocumentRegion();
 
 		// Determine if the offset is within the start
 		// IStructuredDocumentRegion, end IStructuredDocumentRegion, or
 		// somewhere within the Node's XML content.
 		if ((startTag != null) && (startTag.getStartOffset() <= offset) && (offset < startTag.getStartOffset() + startTag.getLength())) {
 			flatNode = startTag;
 		}
 		else if ((endTag != null) && (endTag.getStartOffset() <= offset) && (offset < endTag.getStartOffset() + endTag.getLength())) {
 			flatNode = endTag;
 		}
 
 		if (flatNode != null) {
 			// the offset is definitely within the start or end tag, continue
 			// on and find the region
 			region = getCompletionRegion(offset, flatNode);
 		}
 		else {
 			// the docPosition is neither within the start nor the end, so it
 			// must be content
 			flatNode = node.getStructuredDocument().getRegionAtCharacterOffset(offset);
 			// (pa) ITextRegion refactor
 			// if (flatNode.contains(documentPosition)) {
 			if ((flatNode.getStartOffset() <= documentPosition) && (flatNode.getEndOffset() >= documentPosition)) {
 				// we're interesting in completing/extending the previous
 				// IStructuredDocumentRegion if the current
 				// IStructuredDocumentRegion isn't plain content or if it's
 				// preceded by an orphan '<'
 				if ((offset == flatNode.getStartOffset()) &&
 						(flatNode.getPrevious() != null) &&
 						(((flatNode.getRegionAtCharacterOffset(documentPosition) != null) &&
 								(flatNode.getRegionAtCharacterOffset(documentPosition).getType() != DOMRegionContext.XML_CONTENT)) ||
 								(flatNode.getPrevious().getLastRegion().getType() == DOMRegionContext.XML_TAG_OPEN) ||
 								(flatNode.getPrevious().getLastRegion().getType() == DOMRegionContext.XML_END_TAG_OPEN))) {
 					
 					// Is the region also the start of the node? If so, the
 					// previous IStructuredDocumentRegion is
 					// where to look for a useful region.
 					region = flatNode.getPrevious().getLastRegion();
 				}
 				else if (flatNode.getEndOffset() == documentPosition) {
 					region = flatNode.getLastRegion();
 				}
 				else {
 					region = flatNode.getFirstRegion();
 				}
 			}
 			else {
 				// catch end of document positions where the docPosition isn't
 				// in a IStructuredDocumentRegion
 				region = flatNode.getLastRegion();
 			}
 		}
 
 		return region;
 	}
 	
 
 	protected ITextRegion getCompletionRegion(int offset, IStructuredDocumentRegion sdRegion) {
 		ITextRegion region = getSuperCompletionRegion(offset, sdRegion);
 		if (region != null && region.getType() == DOMRegionContext.UNDEFINED) {
 			// FIX: JBIDE-2332 CA with proposal list for comonent's atributes doesn't work before double quotes. 
 			// Sometimes, especially if we have a broken XML node, the region returned has UNDEFINED type.
 			// If so, we're try to use the prevoius region, which probably will be the region of type XML_TAG_NAME.
 						
 			ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(offset - 1);
 			if ((previousRegion != null) && (previousRegion != region) && (previousRegion.getTextLength() < previousRegion.getLength())) {
 				region = previousRegion;
 			}
 		}
 		return region;
 	}
 	
 	private ITextRegion getSuperCompletionRegion(int offset, IStructuredDocumentRegion sdRegion) {
 		ITextRegion region = sdRegion.getRegionAtCharacterOffset(offset);
 		if (region == null) {
 			return null;
 		}
 
 		if (sdRegion.getStartOffset(region) == offset) {
 			// The offset is at the beginning of the region
 			if ((sdRegion.getStartOffset(region) == sdRegion.getStartOffset()) && (sdRegion.getPrevious() != null) && (!sdRegion.getPrevious().isEnded())) {
 				// Is the region also the start of the node? If so, the
 				// previous IStructuredDocumentRegion is
 				// where to look for a useful region.
 				region = sdRegion.getPrevious().getRegionAtCharacterOffset(offset - 1);
 			}
 			else {
 				// Is there no separating whitespace from the previous region?
 				// If not,
 				// then that region is the important one
 				ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(offset - 1);
 				if ((previousRegion != null) && (previousRegion != region) && (previousRegion.getTextLength() == previousRegion.getLength())) {
 					region = previousRegion;
 				}
 			}
 		}
 		else {
 			// The offset is NOT at the beginning of the region
 			if (offset > sdRegion.getStartOffset(region) + region.getTextLength()) {
 				// Is the offset within the whitespace after the text in this
 				// region?
 				// If so, use the next region
 				ITextRegion nextRegion = sdRegion.getRegionAtCharacterOffset(sdRegion.getStartOffset(region) + region.getLength());
 				if (nextRegion != null) {
 					region = nextRegion;
 				}
 			}
 			else {
 				// Is the offset within the important text for this region?
 				// If so, then we've already got the right one.
 			}
 		}
 
 		// valid WHITE_SPACE region handler (#179924)
 		if ((region != null) && (region.getType() == DOMRegionContext.WHITE_SPACE)) {
 			ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(sdRegion.getStartOffset(region) - 1);
 			if (previousRegion != null) {
 				region = previousRegion;
 			}
 		}
 
 		return region;
 	}
 
 	protected int getTagInsertionBaseRelevance() {
 		return TextProposal.R_XML_TAG_INSERTION;
 	}
 	
 }
