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
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLModelQueryCompletionProposalComputer;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.internal.KbProject;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Abstract base class for XML proposal computers
  * 
  * @since 3.2.0
  * @author Jeremy
  *
  */
 @SuppressWarnings("restriction")
 abstract public class AbstractXmlCompletionProposalComputer extends AbstractXMLModelQueryCompletionProposalComputer {
 	protected static final ICompletionProposal[] EMPTY_PROPOSAL_LIST = new ICompletionProposal[0];
 	private static final String[] EMPTY_TAGS = new String[0];
 	public static final String EL_DOLLAR_PREFIX = "${"; //$NON-NLS-1$
 	public static final String EL_NUMBER_PREFIX = "#{"; //$NON-NLS-1$
 	public static final String EL_SUFFIX = "}"; //$NON-NLS-1$
 	public static final String[] EL_PREFIXES = {EL_DOLLAR_PREFIX, EL_NUMBER_PREFIX};
 	
 	protected CompletionProposalInvocationContext fCurrentContext;
 	
 	protected ELContext fContext;
 
 	/**
 	 * <p>Determine if the document is XHTML or not, then compute the proposals</p>
 	 * 
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLCompletionProposalComputer#computeCompletionProposals(org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@SuppressWarnings("rawtypes")
 	public List computeCompletionProposals(
 			CompletionProposalInvocationContext context,
 			IProgressMonitor monitor) {
 
 		this.fCurrentContext = context;
 		
 		this.fContext = createContext();
 
 		IFile resource = fContext == null ? null : fContext.getResource();
 		KbProject.checkKBBuilderInstalled(resource);
 
 		//compute the completion proposals
 		return super.computeCompletionProposals(context, monitor);
 	}
 	
 	/**
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	abstract protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition, CompletionProposalInvocationContext context);
 
 	/**
 	 * Calculates and adds the tag attribute value proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 	abstract protected void addAttributeValueProposals(
 			ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context);
 
 	/**
 	 * Creates and fulfills the <code>org.jboss.tools.common.el.core.resolver.ELContext</code> 
 	 * instance
 	 * 
 	 * @return
 	 */
 	abstract protected ELContext createContext();
 
 	/**
 	 * Returns the <code>org.jboss.tools.jst.web.kb.KbQuery</code> instance. The prefix and URI for the tags 
 	 * are calculated from the current node
 	 * 
 	 * @param type One of the <code>org.jboss.tools.jst.web.kb.KbQuery.Type</code> values
 	 * @param query The value for query
 	 * @param stringQuery the full text of the query value
 	 * 
 	 * @return The <code>org.jboss.tools.jst.web.kb.KbQuery</code> instance
 	 */
 	abstract protected KbQuery createKbQuery(Type type, String query, String stringQuery);
 	
 	/**
 	 * Returns the <code>org.jboss.tools.jst.web.kb.KbQuery</code> instance
 	 * 
 	 * @param type One of the <code>org.jboss.tools.jst.web.kb.KbQuery.Type</code> values
 	 * @param query The value for query
 	 * @param prefix the prefix for the tag
 	 * @param uri the URI for the tag
 	 * 
 	 * @return The <code>org.jboss.tools.jst.web.kb.KbQuery</code> instance
 	 */
 	abstract protected KbQuery createKbQuery(Type type, String query, String stringQuery, String prefix, String uri);
 
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
 	abstract public String getTagUri();
 	
 	/**
 	 * Returns URI string for the prefix specified using the namespaces collected for 
 	 * the {@link IPageContext} context.
 	 * 
 	 * 	@Override org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#getUri(String)
 	 */
 	abstract protected String getUri(String prefix);
 
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
 
 	protected ContentAssistRequest computeCompletionProposals(String matchString, ITextRegion completionRegion, IDOMNode treeNode, IDOMNode xmlnode, CompletionProposalInvocationContext context) {
 		ContentAssistRequest contentAssistRequest = super.computeCompletionProposals(matchString, completionRegion, treeNode, xmlnode, context);
 		if (contentAssistRequest == null) {
 			IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(context.getInvocationOffset());
 			contentAssistRequest = newContentAssistRequest((Node) treeNode, treeNode.getParentNode(), sdRegion, completionRegion, context.getInvocationOffset(), 0, ""); //$NON-NLS-1$
 		}
 		
 		String regionType = completionRegion.getType();
 
 		/*
 		 * Jeremy: Add attribute name proposals before  empty tag close
 		 */
 		if ((xmlnode.getNodeType() == Node.ELEMENT_NODE) || (xmlnode.getNodeType() == Node.DOCUMENT_NODE)) {
 			if (regionType == DOMRegionContext.XML_EMPTY_TAG_CLOSE) {
 				addAttributeNameProposals(contentAssistRequest, context);
 			} else if ((regionType == DOMRegionContext.XML_CONTENT) 
 					|| (regionType == DOMRegionContext.XML_CHAR_REFERENCE) 
 					|| (regionType == DOMRegionContext.XML_ENTITY_REFERENCE) 
 					|| (regionType == DOMRegionContext.XML_PE_REFERENCE)
 					|| (regionType == DOMRegionContext.BLOCK_TEXT)
 					|| (regionType == DOMRegionContext.XML_END_TAG_OPEN)) {
 				addTextELProposals(contentAssistRequest, context);
 			}
 		}
 
 		return contentAssistRequest;
 	}
 	
 	protected ContentAssistRequest newContentAssistRequest(Node node, Node possibleParent, IStructuredDocumentRegion documentRegion, ITextRegion completionRegion, int begin, int length, String filter) {
 		return new ContentAssistRequest(node, possibleParent, documentRegion, completionRegion, begin, length, filter);
 	}
 	/**
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest 
 	 * @param childPosition  	
 	 */
 	abstract protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition,
 			CompletionProposalInvocationContext context);
 
 	
 	/**
 	 * Calculates and adds the EL proposals in attribute value to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest 
 	 */
 	abstract protected void addAttributeValueELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context);
 	
 	/**
 	 * Calculates and adds the EL proposals in text to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest 
 	 */
 	abstract protected void addTextELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context);
 
 	/**
 	 * Returns default prefix for ELs
 	 * 
 	 * @return default EL prefix
 	 */
 	public String getDefaultELPrefix() {
		return EL_DOLLAR_PREFIX;
 	}
 	
 	/**
 	 * Checks if the specified text begins with a one of possible EL prefixes
 	 *  
 	 * @param text
 	 * @return true if one of EL prefixes found 
 	 */
 	protected boolean startsWithELBeginning(String text) {
 		return (text != null && (text.startsWith(EL_DOLLAR_PREFIX) || text.startsWith(EL_NUMBER_PREFIX)));
 	}
 	protected boolean endsWithELBeginning(String text) {
 		return (text != null && text.endsWith(EL_SUFFIX));
 	}
 	
 	public static class TextRegion {
 		private int startOffset;
 		private int offset;
 		private int length;
 		private String text;
 		private boolean isELStarted;
 		private boolean isELClosed;
 		private boolean isAttributeValue;
 		private boolean hasOpenQuote;
 		private boolean hasCloseQuote;
 		private char quoteChar;
 		
 		public TextRegion(int startOffset, int offset, int length, String text, boolean isELStarted, boolean isELClosed) {
 			this(startOffset, offset, length, text, isELStarted, isELClosed, false, false, false, (char)0);
 		}
 
 		public TextRegion(int startOffset, int offset, int length, String text, boolean isELStarted, boolean isELClosed,
 				boolean isAttributeValue, boolean hasOpenQuote, boolean hasCloseQuote, char quoteChar) {
 			this.startOffset = startOffset;
 			this.offset = offset;
 			this.length = length;
 			this.text = text;
 			this.isELStarted = isELStarted;
 			this.isELClosed = isELClosed;
 			this.isAttributeValue = isAttributeValue;
 			this.hasOpenQuote = hasOpenQuote;
 			this.hasCloseQuote = hasCloseQuote;
 			this.quoteChar = quoteChar;
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
 
 		public boolean isAttributeValue() {
 			return isAttributeValue;
 		}
 
 		public char getQuoteChar() {
 			return quoteChar;
 		}
 
 		public boolean hasOpenQuote() {
 			return hasOpenQuote;
 		}
 
 		public boolean hasCloseQuote() {
 			return hasCloseQuote;
 		}
 	}
 
 }
