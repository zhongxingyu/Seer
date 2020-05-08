 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.contentassist.computers;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDocument;
 import org.eclipse.wst.html.core.internal.provisional.HTMLCMProperties;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.core.internal.ssemodelquery.ModelQueryAdapter;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.jst.web.kb.IFaceletPageContext;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * Tag Proposal computer for XHTML pages
  * 
  * @author Jeremy
  *
  */
 @SuppressWarnings("restriction")
 public class FaceletsTagCompletionProposalComputer extends JspTagCompletionProposalComputer {
 	private static final String JSFC_ATTRIBUTE_NAME = "jsfc"; //$NON-NLS-1$
 	private boolean replaceJsfcTags;
 
 	/** <code>true</code> if the document the proposal request is on is XHTML */
 	protected boolean isXHTML = false;
 	
 	/**
 	 * <p>Determine if the document is XHTML or not, then compute the proposals</p>
 	 * @TODO: move the XHTML determination to XHTML computer
 	 * 
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLCompletionProposalComputer#computeCompletionProposals(org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@SuppressWarnings("rawtypes")
 	public List computeCompletionProposals(
 			CompletionProposalInvocationContext context,
 			IProgressMonitor monitor) {
 
 		try {
 			//determine if the content is XHTML or not
 			IndexedRegion treeNode = ContentAssistUtils.getNodeAt(context.getViewer(),
 					context.getInvocationOffset());
 			IDOMNode node = (IDOMNode) treeNode;
 			boolean isXHTMLNode = isXHTMLNode(node);
 			if(this.isXHTML != isXHTMLNode) {
 				this.isXHTML = isXHTMLNode;
 			}
 			
 			//compute the completion proposals
 			return super.computeCompletionProposals(context, monitor);
 		} finally {
 			fCurrentContext = null;
 		}
 	}
 
 	/**
 	 * Determine if this Document is an XHTML Document. Operates solely off of
 	 * the Document Type declaration
 	 */
 	@SuppressWarnings("deprecation")
 	private static boolean isXHTMLNode(Node node) {
 		if (node == null) {
 			return false;
 		}
 
 		Document doc = null;
 		if (node.getNodeType() != Node.DOCUMENT_NODE)
 			doc = node.getOwnerDocument();
 		else
 			doc = ((Document) node);
 
 		if (doc instanceof IDOMDocument) {
 			return ((IDOMDocument) doc).isXMLType();
 		}
 
 		if (doc instanceof INodeNotifier) {
 			ModelQueryAdapter adapter = (ModelQueryAdapter) ((INodeNotifier) doc).getAdapterFor(ModelQueryAdapter.class);
 			CMDocument cmdoc = null;
 			if (adapter != null && adapter.getModelQuery() != null)
 				cmdoc = adapter.getModelQuery().getCorrespondingCMDocument(doc);
 			if (cmdoc != null) {
 				// treat as XHTML unless we've got the in-code HTML content
 				// model
 				if (cmdoc instanceof HTMLCMDocument)
 					return false;
 				if (cmdoc.supports(HTMLCMProperties.IS_XHTML))
 					return Boolean.TRUE.equals(cmdoc.getProperty(HTMLCMProperties.IS_XHTML));
 			}
 		}
 		// this should never be reached
 		DocumentType docType = doc.getDoctype();
 		return docType != null && docType.getPublicId() != null && docType.getPublicId().indexOf("-//W3C//DTD XHTML ") == 0; //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#createContext()
 	 */
 	@Override
 	protected ELContext createContext() {
 		return PageContextFactory.createPageContext(getResource(), PageContextFactory.FACELETS_PAGE_CONTEXT_TYPE);
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
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * The method is to be overridden here because xhtml allows to use EL-s inside a text region
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition,
 			CompletionProposalInvocationContext context) {
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		
		// This is a workaround for issues JBIDE-7100 and JBIDE-9092 ===>>>  
		ITextViewer textViewer = context.getViewer();
		IndexedRegion treeNode = ContentAssistUtils.getNodeAt(textViewer, context.getInvocationOffset());

		Node node = (Node) treeNode;
		while ((node != null) && (node.getNodeType() == Node.TEXT_NODE) && (node.getParentNode() != null)) {
			node = node.getParentNode();
		}
		IDOMNode xmlnode = (IDOMNode) node;

		ITextRegion completionRegion = getCompletionRegion(context.getInvocationOffset(), node);

		String regionType = completionRegion.getType();
		if (regionType == DOMRegionContext.XML_END_TAG_OPEN) {
			// Disable tag insertion proposals if it's end tag open region of <style /> tag
			if (xmlnode != null && xmlnode.getNodeName() != null && xmlnode.getNodeName().equalsIgnoreCase("style")) //$NON-NLS-1$
				return;
		}
		// This is a workaround for issues JBIDE-7100 and JBIDE-9092 <<<===  
		
 		addTagNameProposals(contentAssistRequest, childPosition, true, context);
 	}
 	
 	/**
 	 * Calculates and adds the EL proposals to the Content Assist Request object
 	 */
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		// No EL proposals are to be added here
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#addAttributeNameProposals(org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest)
 	 */
 	@Override
 	protected void addAttributeNameProposals(
 			ContentAssistRequest contentAssistRequest, 
 			CompletionProposalInvocationContext context) {
 		if (!(contentAssistRequest.getNode() instanceof Element))
 			return;
 		
 		super.addAttributeNameProposals(contentAssistRequest, context);
 		if (isExistingAttribute(JSFC_ATTRIBUTE_NAME)) {
 			this.replaceJsfcTags = true;
 			super.addAttributeNameProposals(contentAssistRequest, context);
 			this.replaceJsfcTags = false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#addAttributeValueProposals(org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest)
 	 */
 	@Override
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		super.addAttributeValueProposals(contentAssistRequest, context);
 		if (isExistingAttribute(JSFC_ATTRIBUTE_NAME)) {
 			this.replaceJsfcTags = true;
 			super.addAttributeValueProposals(contentAssistRequest, context);
 			this.replaceJsfcTags = false;
 		}
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
