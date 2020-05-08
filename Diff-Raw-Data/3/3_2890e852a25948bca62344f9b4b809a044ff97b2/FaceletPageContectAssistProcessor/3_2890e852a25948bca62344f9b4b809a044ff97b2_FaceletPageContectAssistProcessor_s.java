 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.contentassist;
 
 import java.util.Map;
 
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.web.kb.IFaceletPageContext;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.internal.FaceletPageContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.NameSpace;
 import org.jboss.tools.jst.web.kb.taglib.CustomTagLibManager;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * 
  * @author Jeremy
  *
  */
 public class FaceletPageContectAssistProcessor extends JspContentAssistProcessor {
 	private static final String JSFC_ATTRIBUTE_NAME = "jsfc"; //$NON-NLS-1$
 
 	private boolean replaceJsfcTags;
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#createContext()
 	 */
 	@Override
 	protected IPageContext createContext() {
 		IPageContext superContext = super.createContext();
 
 		FaceletPageContextImpl context = new FaceletPageContextImpl();
 		context.setResource(superContext.getResource());
 		context.setElResolvers(superContext.getElResolvers());
 		setVars(context, superContext.getResource());
 
		context.setResourceBundles(superContext.getResourceBundles());
 		context.setDocument(getDocument());
 		setNameSpaces(superContext, context);
 		context.setLibraries(getTagLibraries(context));
 
 //		IFaceletPageContext getParentContext();
 //		Map<String, String> getParams();
 
 		return context;
 	}
 
 	protected void setNameSpaces(IPageContext superContext, FaceletPageContextImpl context) {
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 		if (superContext != null) {
 			IRegion region = new Region (0, getDocument().getLength());
 			Map<String, INameSpace> nameSpaces = superContext.getNameSpaces(getOffset());
 			for (String prefix : nameSpaces.keySet()) {
 				context.addNameSpace(region, nameSpaces.get(prefix));
 			}
 		}
 
 		try {
 			if (sModel == null)
 				return;
 
 			Document xmlDocument = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 
 			if (xmlDocument == null)
 				return;
 
 			// Get Fixed Structured Document Region
 			IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 			if (sdFixedRegion == null)
 				return;
 			
 			Node n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
 			while (n != null) {
 				if (!(n instanceof Element)) {
 					if (n instanceof Attr) {
 						n = ((Attr) n).getOwnerElement();
 					} else {
 						n = n.getParentNode();
 					}
 					continue;
 				}
 
 				NamedNodeMap attrs = n.getAttributes();
 				for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
 					Attr a = (Attr) attrs.item(j);
 					String name = a.getName();
 					if (name.startsWith("xmlns:")) { //$NON-NLS-1$
 						final String prefix = name.substring("xmlns:".length()); //$NON-NLS-1$
 						final String uri = a.getValue();
 						if (prefix != null && prefix.trim().length() > 0 &&
 								uri != null && uri.trim().length() > 0) {
 
 							int start = ((IndexedRegion)n).getStartOffset();
 							int length = ((IndexedRegion)n).getLength();
 							
 							IDOMElement domElement = (n instanceof IDOMElement ? (IDOMElement)n : null);
 							if (domElement != null) {
 								start = domElement.getStartOffset();
 								length = (domElement.hasEndTag() ? 
 											domElement.getEndStructuredDocumentRegion().getEnd() :
 												domElement.getLength());
 								
 							}
 
 							Region region = new Region(start, length);
 							INameSpace nameSpace = new NameSpace(uri.trim(), prefix.trim());
 							context.addNameSpace(region, nameSpace);
 							if (CustomTagLibManager.FACELETS_UI_TAG_LIB_URI.equals(uri)) {
 								nameSpace = new NameSpace(CustomTagLibManager.FACELETS_HTML_TAG_LIB_URI, "");
 								context.addNameSpace(region, nameSpace);
 							}
 						}
 					}
 				}
 
 				n = n.getParentNode();
 			}
 
 			return;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#getContext()
 	 */
 	@Override
 	public IFaceletPageContext getContext() {
 		return (IFaceletPageContext)super.getContext();
 	}
 
 	/**
 	 * Calculates and adds the EL proposals to the Content Assist Request object
 	 */
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest) {
 		TextRegion prefix = getELPrefix();
 		if (prefix == null || !prefix.isELStarted()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, "#{}", //$NON-NLS-1$ 
 					contentAssistRequest.getReplacementBeginPosition(), 
 					0, 2, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_NewELExpression, null, 
 					JstUIMessages.FaceletPageContectAssistProcessor_NewELExpressionTextInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 			
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
 
 		for (int i = 0; proposals != null && i < proposals.length; i++) {
 			TextProposal textProposal = proposals[i];
 
 			int replacementOffset = beginChangeOffset;
 			int replacementLength = prefix.getLength();
 			String replacementString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			int cursorPosition = replacementString.length();
 			Image image = textProposal.getImage();
 
 			String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString(); 
 			IContextInformation contextInformation = null;
 			String additionalProposalInfo = textProposal.getContextInfo();
 			int relevance = textProposal.getRelevance();
 			if (relevance == TextProposal.R_NONE) {
 				relevance = TextProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 			}
 
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(replacementString, 
 					replacementOffset, replacementLength, cursorPosition, image, displayString, 
 					contextInformation, additionalProposalInfo, relevance);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 
 		if (prefix.isELStarted() && !prefix.isELClosed()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal("}", //$NON-NLS-1$
 					getOffset(), 0, 1, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_CloseELExpression, 
 					null, JstUIMessages.JspContentAssistProcessor_CloseELExpressionInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#addAttributeNameProposals(org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest)
 	 */
 	@Override
 	protected void addAttributeNameProposals(
 			ContentAssistRequest contentAssistRequest) {
 		super.addAttributeNameProposals(contentAssistRequest);
 		this.replaceJsfcTags = true;
 		super.addAttributeNameProposals(contentAssistRequest);
 		this.replaceJsfcTags = false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#addAttributeValueProposals(org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest)
 	 */
 	@Override
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		super.addAttributeValueProposals(contentAssistRequest);
 		this.replaceJsfcTags = true;
 		super.addAttributeValueProposals(contentAssistRequest);
 		this.replaceJsfcTags = false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#getTagName(org.w3c.dom.Node)
 	 */
 	@Override
 	protected String getTagName(Node tag) {
 		String tagName = tag.getNodeName();
 		if(replaceJsfcTags) {
 			// Only HTML tags
 			if(tagName.indexOf(':')>0) {
 				return tagName;
 			}
 			if (!(tag instanceof Element))
 				return tagName;
 			
 			Element element = (Element)tag;
 
 			NamedNodeMap attributes = element.getAttributes();
 			Node jsfC = attributes.getNamedItem(JSFC_ATTRIBUTE_NAME);
 			if(jsfC==null || (!(jsfC instanceof Attr))) {
 				return tagName;
 			}
 			Attr jsfCAttribute = (Attr)jsfC;
 			String jsfTagName = jsfCAttribute.getValue();
 			if(jsfTagName==null || jsfTagName.indexOf(':')<1) {
 				return tagName;
 			}
 			tagName = jsfTagName;
 		}
 		return tagName;
 	}
 }
