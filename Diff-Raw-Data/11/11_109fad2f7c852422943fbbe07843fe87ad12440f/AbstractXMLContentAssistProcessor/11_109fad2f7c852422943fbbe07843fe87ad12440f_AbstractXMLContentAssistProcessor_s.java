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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 abstract public class AbstractXMLContentAssistProcessor extends AbstractContentAssistProcessor {
 	private static final char[] PROPOSAL_AUTO_ACTIVATION_CHARS = new char[] {
 		'<', '=', '"', '\'', '.'
 	};
 	
 	private IDocument fDocument;
 	private int fDocumentPosition;
 	private ELContext fContext;
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
 	 */
 	@Override
 	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
 			int offset) {
 		this.fDocument = (viewer == null ? null : viewer.getDocument());
 		this.fDocumentPosition = offset;
 		try {
 			this.fContext = createContext();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new ICompletionProposal[0];
 		}
 		System.out.println("AbstractXMLContentAssistProcessor: computeCompletionProposals() invoked");
 		try {
 			return super.computeCompletionProposals(viewer, offset);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new ICompletionProposal[0];
 		} finally {
 			System.out.println("AbstractXMLContentAssistProcessor: computeCompletionProposals() exited");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
 	 */
 	@Override
 	public IContextInformation[] computeContextInformation(ITextViewer viewer,
 			int offset) {
 		this.fDocument = (viewer == null ? null : viewer.getDocument());
 		this.fDocumentPosition = offset;
 		this.fContext = createContext();
 		
 		return super.computeContextInformation(viewer, offset);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
 	 */
 	@Override
 	public char[] getCompletionProposalAutoActivationCharacters() {
 		return PROPOSAL_AUTO_ACTIVATION_CHARS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#getContextInformationAutoActivationCharacters()
 	 */
 	@Override
 	public char[] getContextInformationAutoActivationCharacters() {
 		return super.getContextInformationAutoActivationCharacters();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#getContextInformationValidator()
 	 */
 	@Override
 	public IContextInformationValidator getContextInformationValidator() {
 		return super.getContextInformationValidator();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#getErrorMessage()
 	 */
 	@Override
 	public String getErrorMessage() {
 		return super.getErrorMessage();
 	}
 	
 	
 
 	/* the methods to be overriden in derived classes */
 
 	
 
 	/**
 	 * Calculates and adds the attribute name proposals to the Content Assist Request object
 	 */
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addAttributeNameProposals() invoked");
 	}
 
 	/**
 	 * Calculates and adds the attribute value proposals to the Content Assist Request object
 	 */
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addAttributeValueProposals() invoked");
 /*
 		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
 
 		// Find the attribute region and name for which this position should
 		// have a value proposed
 		IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
 		ITextRegionList openRegions = open.getRegions();
 		int i = openRegions.indexOf(contentAssistRequest.getRegion());
 		if (i < 0) {
 			return;
 		}
 		ITextRegion nameRegion = null;
 		while (i >= 0) {
 			nameRegion = openRegions.get(i--);
 			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
 				break;
 			}
 		}
 
 		// the name region is REQUIRED to do anything useful
 		if (nameRegion != null) {
 			// Retrieve the declaration
 			CMElementDeclaration elementDecl = getCMElementDeclaration(node);
 
 			// String attributeName = nameRegion.getText();
 			String attributeName = open.getText(nameRegion);
 			String currentValue = node.getAttributes().getNamedItem(attributeName).getNodeValue();
 			String currentValueText = ((IDOMAttr)node.getAttributes().getNamedItem(attributeName)).getValueRegionText();
 			ITextRegion currentValueRegion = ((IDOMAttr)node.getAttributes().getNamedItem(attributeName)).getValueRegion();
 			
 			
 			
 			ITextRegion invokeRegion = contentAssistRequest.getRegion();
 			int pos = contentAssistRequest.getRegion().getStart();
 			int replBegin = contentAssistRequest.getReplacementBeginPosition();
 			int invokeRegionEnd = invokeRegion.getStart() + invokeRegion.getLength();
 			IDOMAttr attrNode = (IDOMAttr)node.getAttributes().getNamedItem(attributeName);
 			int valueRegionStartOffset = attrNode.getValueRegionStartOffset();
 			ITextRegion eqRegion = attrNode.getEqualRegion();
 			int eqRegionEnd= eqRegion.getStart() + eqRegion.getLength();
 			int attrValueEnd=eqRegionEnd + currentValueText.length();
 			int attrTextEnd = invokeRegion.getTextEnd();
 			// attrNode.getValueRegionText()
 			System.out.println("AbstractXMLContentAssistProcessor: addAttributeValueProposals() invoked");
 		}
 		else {
 			setErrorMessage(UNKNOWN_CONTEXT);
 		}
 		*/
 	}
 	
 	/*
 	 * Calculates and adds the comment proposals to the Content Assist Request object
 	 */
 	protected void addCommentProposal(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addCommentProposal() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the doc type proposals to the Content Assist Request object
 	 */
 	protected void addDocTypeProposal(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addDocTypeProposal() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the empty document proposals to the Content Assist Request object
 	 */
 	protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addEmptyDocumentProposals() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 */
 	protected void addEndTagNameProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addEndTagNameProposals() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the end tag proposals to the Content Assist Request object
 	 */
 	protected void addEndTagProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addEndTagProposals() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the enttity proposals to the Content Assist Request object
 	 */
 	protected void addEntityProposals(ContentAssistRequest contentAssistRequest, int documentPosition, ITextRegion completionRegion, IDOMNode treeNode) {
 		System.out.println("AbstractXMLContentAssistProcessor: addEntityProposals() invoked");
 		super.addEntityProposals(contentAssistRequest, documentPosition, completionRegion, treeNode);
 	}
 	
 	/*
 	 * Calculates and adds the PCDATA proposals to the Content Assist Request object
 	 */
 	protected void addPCDATAProposal(String nodeName, ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addPCDATAProposal() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the start document proposals to the Content Assist Request object
 	 */
 	protected void addStartDocumentProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addStartDocumentProposals() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the tag close proposals to the Content Assist Request object
 	 */
 	protected void addTagCloseProposals(ContentAssistRequest contentAssistRequest) {
 		System.out.println("AbstractXMLContentAssistProcessor: addTagCloseProposals() invoked");
 	}
 	
 	/*
 	 * Calculates and adds the tag insertion proposals to the Content Assist Request object
 	 */
 	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
 		System.out.println("AbstractXMLContentAssistProcessor: addTagInsertionProposals() invoked");
 	}
 	
 	/**
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest 
 	 * @param childPosition  	
 	 */
 	abstract protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition);
 
 	
 	/**
 	 * Calculates and adds the EL proposals in attribute value to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest 
 	 */
 	abstract protected void addAttributeValueELProposals(ContentAssistRequest contentAssistRequest);
 	
 	/**
 	 * Calculates and adds the EL proposals in text to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest 
 	 */
 	abstract protected void addTextELProposals(ContentAssistRequest contentAssistRequest);
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#computeCompletionProposals(int, java.lang.String, org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion, org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode, org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode)
 	 */
 	protected ContentAssistRequest computeCompletionProposals(int documentPosition, String matchString, ITextRegion completionRegion, IDOMNode treeNode, IDOMNode xmlnode) {
 		ContentAssistRequest contentAssistRequest = super.computeCompletionProposals(documentPosition, matchString, completionRegion, treeNode, xmlnode);
 		
 		String regionType = completionRegion.getType();
 		IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 
 		/*
 		 * Jeremy: Add attribute name proposals before  empty tag close
 		 */
 		if ((xmlnode.getNodeType() == Node.ELEMENT_NODE) || (xmlnode.getNodeType() == Node.DOCUMENT_NODE)) {
 			if (regionType == DOMRegionContext.XML_EMPTY_TAG_CLOSE) {
 				addAttributeNameProposals(contentAssistRequest);
 			} else if ((regionType == DOMRegionContext.XML_CONTENT) || (regionType == DOMRegionContext.XML_CHAR_REFERENCE) || (regionType == DOMRegionContext.XML_ENTITY_REFERENCE) || (regionType == DOMRegionContext.XML_PE_REFERENCE)) {
 				addTextELProposals(contentAssistRequest);
 			}
 		}
 
 		return contentAssistRequest;
 	}
 	
 	
 	protected ContentAssistRequest computeAttributeValueProposals(int documentPosition, String matchString, ITextRegion completionRegion, IDOMNode nodeAtOffset, IDOMNode node) {
 		ContentAssistRequest contentAssistRequest = super.computeAttributeValueProposals(documentPosition, matchString, completionRegion, nodeAtOffset, node);
 		
 		IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 		if ((documentPosition <= sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength()) || 
 				(sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength() == sdRegion.getStartOffset(completionRegion) + completionRegion.getLength())) {
 			// setup to replace the existing value
 			if (nodeAtOffset.getFirstStructuredDocumentRegion().isEnded() || (documentPosition >= sdRegion.getStartOffset(completionRegion))) {
 				addAttributeValueELProposals(contentAssistRequest);
 			}
 		}
 		return contentAssistRequest;
 	}
 
 	/**
 	 * Creates and fulfills the <code>org.jboss.tools.common.el.core.resolver.ELContext</code> 
 	 * instance
 	 * 
 	 * @return
 	 */
 	abstract protected ELContext createContext();
 
 	/**
 	 * Creates and fulfills the <code>org.jboss.tools.jst.web.kb.KbQuery</code> 
 	 * instance
 	 * Important: the Context is to be set before any call to createKbQuery
 	 * 
 	 * @return
 	 */
 	
 	/**
 	 * Returns the <code>org.jboss.tools.jst.web.kb.KbQuery</code> instance
 	 * 
 	 * @param type One of the <code>org.jboss.tools.jst.web.kb.KbQuery.Type</code> values
 	 * @param query The value for query
 	 * @param stringQuery the full text of the query value
 	 * 
 	 * @return The <code>org.jboss.tools.jst.web.kb.KbQuery</code> instance
 	 */
 	abstract protected KbQuery createKbQuery(Type type, String query, String stringQuery);
 	
 	/**
 	 * Returns the <code>org.jboss.tools.common.el.core.resolver.ELContext</code> instance
 	 * 
 	 * @return
 	 */
 	protected ELContext getContext() {
 		return this.fContext;
 	}
 	
 	/**
 	 * Returns the document position where the CA is invoked
 	 * @return
 	 */
 	protected int getOffset() {
 		return this.fDocumentPosition;
 	}
 	
 	/**
 	 * Returns the document
 	 * 
 	 * @return
 	 */
 	protected IDocument getDocument() {
 		return this.fDocument;
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
 				IFile resource = FileBuffers.getWorkspaceFileAtLocation(location);
 				return resource;
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
 	 * Returns array of the <code>org.jboss.tools.common.el.core.resolver.ELResolver</code> 
 	 * instances.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	protected ELResolver[] getELResolvers(IResource resource) {
 		ELResolverFactoryManager elrfm = ELResolverFactoryManager.getInstance();
 		return elrfm.getResolvers(resource);
 	}
 	
 	private static final String[] EMPTY_TAGS = new String[0];
 	/**
 	 * Returns array of the parent tags 
 	 * 
 	 * @return
 	 */
 	protected String[] getParentTags(boolean includeThisTag) {
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
 			
 			Node n = findNodeForOffset(xmlDocument, getOffset());
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
 			while (n != null) {
 				String tagName = n.getNodeName();
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
 	
 	/**
 	 * Returns name of the parent attribute/tag name
 	 * 
 	 * @return
 	 */
 	protected String getParent(boolean returnAttributeName) {
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
 			
 			Node n = findNodeForOffset(xmlDocument, getOffset());
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
 				n = n.getParentNode();
 			}
 			if (n == null)
 				return null;
 			
 			String parentTagName = n.getNodeName();
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
 	protected String getTagPrefix() {
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
 			
 			Node n = findNodeForOffset(xmlDocument, getOffset());
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
 	protected String getTagUri() {
 		String nodePrefix = getTagPrefix();
 		return getUri(nodePrefix);
 	}
 	
 	/**
 	 * Returns URI string for the prefix specified 
 	 * 
 	 * @param prefix
 	 * @return
 	 */
 	abstract protected String getUri(String prefix); 
 	
 	/* Utility functions */
 	Node findNodeForOffset(IDOMNode node, int offset) {
 		if(node == null) return null;
 		if (!node.contains(offset)) return null;
 			
 		if (node.hasChildNodes()) {
 			// Try to find the node in children
 			NodeList children = node.getChildNodes();
 			for (int i = 0; children != null && i < children.getLength(); i++) {
 				IDOMNode child = (IDOMNode)children.item(i);
 				if (child.contains(offset)) {
 					return findNodeForOffset(child, offset);
 				}
 			}
 		}
 			// Not found in children or nave no children
 		if (node.hasAttributes()) {
 			// Try to find in the node attributes
 			NamedNodeMap attributes = node.getAttributes();
 			
 			for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
 				IDOMNode attr = (IDOMNode)attributes.item(i);
 				if (attr.contains(offset)) {
 					return attr;
 				}
 			}
 		}
 		// Return the node itself
 		return node;
 	}
 
 	Node findNodeForOffset(Node node, int offset) {
 		return (node instanceof IDOMNode) ? findNodeForOffset((IDOMNode)node, offset) : null;
 	}
 
 	/**
 	 * this is the position the cursor should be in after the proposal is
 	 * applied
 	 * 
 	 * @param proposedText
 	 * @return the position the cursor should be in after the proposal is
 	 *         applied
 	 */
 	protected int getCursorPositionForProposedText(String proposedText) {
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
 	 * Returns URI for the current/parent tag
 	 * @return
 	 */
 	protected TextRegion getELPrefix() {
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
 			
 			Node n = findNodeForOffset(xmlDocument, getOffset());
 			if (n == null)
 				return null;
 
 			String text = null;
 			ITextRegion region = null;
 			int startOffset = -1;
 			if (n instanceof IDOMAttr) {
 				text = ((IDOMAttr)n).getValueRegionText();
 				region = ((IDOMAttr)n).getValueRegion();
 				startOffset = ((IndexedRegion)((IDOMAttr)n).getOwnerElement()).getStartOffset(); 
 				startOffset += 	region.getStart();
 			} else if (n instanceof IDOMText) {
 				text = ((IDOMText)n).getNodeValue();
 				region = ((IDOMText)n).getFirstStructuredDocumentRegion();
 				startOffset = ((IDOMText)n).getStartOffset(); 
 			} else {
 				// The EL may appear only in TEXT and ATTRIBUTE VALUE types of node 
 				return null;
 			}
 
 			int inValueOffset = getOffset() - startOffset;
 			if (text.length() < inValueOffset) // probably, the attribute value ends before the document position
 				return null;
 			
 			String matchString = text.substring(0, inValueOffset);
 			
 			ELParser p = ELParserUtil.getJbossFactory().createParser();
 			ELModel model = p.parse(text);
 			
			ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELInstance
 			
			boolean isELStarted = (model != null && (model.toString().startsWith("#{") || 
 					model.toString().startsWith("${")));
			boolean isELClosed = (model != null && model.toString().endsWith("}"));
 			TextRegion tr = new TextRegion(startOffset,  ie == null ? inValueOffset : ie.getStartPosition(), ie == null ? 0 : inValueOffset - ie.getStartPosition(), ie == null ? "" : ie.getText(), isELStarted, isELClosed);
 			
 			return tr;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	protected class TextRegion {
 		private int startOffset;
 		private int offset;
 		private int length;
 		private String text;
 		private boolean isELStarted;
 		private boolean isELClosed;
 		
 		TextRegion(int startOffset, int offset, int length, String text, boolean isELStarted, boolean isELClosed) {
 			this.startOffset = startOffset;
 			this.offset = offset;
 			this.length = length;
 			this.text = text;
 			this.isELStarted = isELStarted;
 			this.isELClosed = isELClosed;
 		}
 		
 		public int getStartOffset() {
 			return startOffset;
 		}
 		
 		public int getOffset() {
 			return offset;
 		}
 		
 		public int getLength() {
 			return length;
 		}
 		
 		public String getText() {
 			StringBuffer sb = new StringBuffer(length);
 			sb = sb.append(text.substring(0, length));
 			sb.setLength(length);
 			return sb.toString();
 		}
 		
 		public boolean isELStarted() {
 			return isELStarted;
 		}
 
 		public boolean isELClosed() {
 			return isELClosed;
 		}
 	}
 	/*
 	 * Checks if the EL operand starting characters are present
 	 * @return
 	 */
 	private int getELStartPosition(String matchString) {
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(matchString);
 		ELInstance is = ELUtil.findInstance(model, matchString.length());
 		return is == null ? -1 : is.getStartPosition();
 	}
 	
 	/*
 	 * Checks if the EL operand ending character is present
 	 * @return
 	 */
 	private int getELEndPosition(String matchString, String currentValue) {
 		if (matchString == null || matchString.length() == 0 ||
 				currentValue == null || currentValue.length() == 0 || 
 				currentValue.length() < matchString.length())
 			return -1;
 
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(currentValue);
 		ELInstance is = ELUtil.findInstance(model, matchString.length());
 		if(is == null || is.getCloseInstanceToken() == null) return -1;
 
 		return is.getEndPosition();
 	}
 	
 }
