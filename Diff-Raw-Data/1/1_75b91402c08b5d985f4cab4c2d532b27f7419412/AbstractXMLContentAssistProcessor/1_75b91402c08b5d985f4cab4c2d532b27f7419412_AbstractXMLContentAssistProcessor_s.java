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
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
 import org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
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
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.internal.KbBuilder;
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
 
 	protected final static ICompletionProposal[] EMPTY_PROPOSAL_LIST = new ICompletionProposal[0];
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
 	 */
 	@Override
 	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
 			int offset) {
 		this.fDocument = (viewer == null ? null : viewer.getDocument());
 		this.fDocumentPosition = offset;
 		this.fContext = createContext();
 		
 		checkKBBuilderInstalled();
 		return super.computeCompletionProposals(viewer, offset);
 	}
 
 	
 	private void checkKBBuilderInstalled() {
 		ELContext context = getContext();
 		IFile resource = context == null ? null : context.getResource();
 		IProject project = resource == null ? null : resource.getProject();
 		if (project == null) 
 			return; // Cannot check anything
 		
 		boolean kbNatureFound = false;
 		boolean kbBuilderFound = false;
 		try {
 			kbNatureFound = (project.getNature(IKbProject.NATURE_ID) != null);
 		
 			IProjectDescription description = project.getDescription();
 			ICommand command = null;
 			ICommand commands[] = description.getBuildSpec();
 			for (int i = 0; i < commands.length && command == null; ++i) {
 				if (commands[i].getBuilderName().equals(KbBuilder.BUILDER_ID)) {
 					kbBuilderFound = true;
 					break;
 				}
 			}
 		}  catch (CoreException ex) {
 			JspEditorPlugin.getPluginLog().logError(ex);
 		}
 		
 		if (kbNatureFound && kbBuilderFound) {
 			// Find existing KBNATURE problem marker and kill it if exists
 			IMarker[] markers = getOwnedMarkers(project);
 			if (markers != null && markers.length > 0) {
 				for (IMarker m : markers) {
 					try {
 						project.deleteMarkers(KB_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_ONE);
 						project.setPersistentProperty(KbProjectFactory.NATURE_MOCK, null);
 					} catch (CoreException ex) {
 						JspEditorPlugin.getPluginLog().logError(ex);
 					}
 				}
 			}
 			return;
 		}
 		
 		// Find existing KBNATURE problem marker and install it if doesn't exist
 		IMarker[] markers = getOwnedMarkers(project);
 		
 		if (markers == null || markers.length == 0) {
 			try {
 				IMarker m = createOrUpdateKbProblemMarker(null, project, !kbNatureFound, !kbBuilderFound);
 			} catch (CoreException ex) {
 				JspEditorPlugin.getPluginLog().logError(ex);
 			}
 		} else {
 			for (IMarker m : markers) {
 				try {
 					m = createOrUpdateKbProblemMarker(m, project, !kbNatureFound, !kbBuilderFound);
 				} catch (CoreException ex) {
 					JspEditorPlugin.getPluginLog().logError(ex);
 				}
 			}
 		}
 		return;
 		
 	}
 	
 	public static final String KB_PROBLEM_MARKER_TYPE = "org.jboss.tools.jst.jsp.kbproblemmarker"; //$NON-NLS-1$
 	
 	private IMarker createOrUpdateKbProblemMarker(IMarker m, IResource r, boolean kbNatureIsAbsent, boolean kbBuilderIsAbsent) throws CoreException {
 		ArrayList<String> args = new ArrayList<String>();
 		args.add(kbNatureIsAbsent ? JstUIMessages.KBNATURE_NOT_FOUND : ""); //$NON-NLS-1$
 		args.add(kbBuilderIsAbsent ? JstUIMessages.KBBUILDER_NOT_FOUND : ""); //$NON-NLS-1$
 
 		String message = MessageFormat.format(JstUIMessages.KBPROBLEM, args.toArray());
 		if (m == null) {
 			m = r.createMarker(KB_PROBLEM_MARKER_TYPE);
 			r.setPersistentProperty(KbProjectFactory.NATURE_MOCK, "true");
 			KbProjectFactory.getKbProject(r.getProject(), true);
 		}
 		m.setAttribute(IMarker.MESSAGE, message);
 		m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
 		m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
 		return m;
 	}
 	
 	private IMarker[] getOwnedMarkers(IResource r) {
 		ArrayList<IMarker> l = null;
 		try {
 			IMarker[] ms = r.findMarkers(null, false, 1);
 			if(ms != null) {
 				for (int i = 0; i < ms.length; i++) {
 					if(ms[i] == null) continue;
 
 					String _type = ms[i].getType();
 					if(_type == null) continue;
 					if(!_type.equals(KB_PROBLEM_MARKER_TYPE)) continue;
 					if(!ms[i].isSubtypeOf(IMarker.PROBLEM)) continue;
 	
 					if(l == null) 
 						l = new ArrayList<IMarker>();
 					
 					l.add(ms[i]);
 				}
 			}
 		} catch (CoreException e) {
 			//ignore
 		}
 		return (l == null) ? null : l.toArray(new IMarker[0]);
 	}
 	
 	/**
 	 * The reason of overriding is that the method returns wrong region in case of incomplete tag (a tag with no '>'-closing char)
 	 * In this case we have to return that previous incomplete tag instead of the current tag)
 	 */
 	public IStructuredDocumentRegion getStructuredDocumentRegion(int pos) {
 		IStructuredDocumentRegion sdRegion = null;
 		if (fDocument == null)
 			return null;
 
 		int lastOffset = pos;
 		IStructuredDocument doc = (IStructuredDocument) fDocument;
 
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
 		IStructuredDocumentRegion sdNormalRegion = super.getStructuredDocumentRegion(documentPosition);
 		// Get Fixed Structured Document Region
 		IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(documentPosition);
 
 		// If original and fixed regions are different we have to replace domnode with its parent node
 		if (sdFixedRegion != null && !sdFixedRegion.equals(sdNormalRegion)) {
 			Node prevnode = domnode.getParentNode();
 			if (prevnode != null) {
 				domnode = prevnode;
 			}
 		}
 		return super.getCompletionRegion(documentPosition, domnode);
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#computeTagNameProposals(int, java.lang.String, org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion, org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode, org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode)
 	 */
 	protected ContentAssistRequest computeTagNameProposals(int documentPosition, String matchString, ITextRegion completionRegion, IDOMNode nodeAtOffset, IDOMNode node) {
 		ContentAssistRequest contentAssistRequest = null;
 		IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 
 		if (sdRegion != nodeAtOffset.getFirstStructuredDocumentRegion()) {
 			// completing the *first* tag in "<tagname1 |<tagname2"
 			IDOMNode actualNode = (IDOMNode) node.getModel().getIndexedRegion(sdRegion.getStartOffset(completionRegion));
 			if (actualNode != null) {
 				if (actualNode.getFirstStructuredDocumentRegion() == sdRegion) {
 					// start tag
 					if ((documentPosition >= sdRegion.getStartOffset(completionRegion) + completionRegion.getLength()) && 
 						(documentPosition > sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength())){
 						// it's attributes
 						contentAssistRequest = newContentAssistRequest(actualNode, actualNode, sdRegion, completionRegion, documentPosition - matchString.length(), matchString.length(), matchString);
 						if (node.getStructuredDocument().getRegionAtCharacterOffset(sdRegion.getStartOffset(completionRegion) - 1).getRegionAtCharacterOffset(sdRegion.getStartOffset(completionRegion) - 1).getType() == DOMRegionContext.XML_TAG_OPEN) {
 							addAttributeNameProposals(contentAssistRequest);
 						}
 						addTagCloseProposals(contentAssistRequest);
 					}
 					else {
 						// it's name
 						contentAssistRequest = newContentAssistRequest(actualNode, actualNode.getParentNode(), sdRegion, completionRegion, documentPosition - matchString.length(), matchString.length(), matchString);
 						addTagNameProposals(contentAssistRequest, getElementPositionForModelQuery(actualNode));
 					}
 				}
 				else {
 					if (documentPosition >= sdRegion.getStartOffset(completionRegion) + completionRegion.getLength()) {
 						// insert name
 						contentAssistRequest = newContentAssistRequest(actualNode, actualNode.getParentNode(), sdRegion, completionRegion, documentPosition, 0, matchString);
 					}
 					else {
 						// replace name
 						contentAssistRequest = newContentAssistRequest(actualNode, actualNode.getParentNode(), sdRegion, completionRegion, sdRegion.getStartOffset(completionRegion), completionRegion.getTextLength(), matchString);
 					}
 					addEndTagNameProposals(contentAssistRequest);
 				}
 			}
 		}
 		else {
 			if (documentPosition > sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength()) {
 				// unclosed tag with only a name; should prompt for attributes
 				// and a close instead
 				contentAssistRequest = newContentAssistRequest(nodeAtOffset, node, sdRegion, completionRegion, documentPosition - matchString.length(), matchString.length(), matchString);
 				addAttributeNameProposals(contentAssistRequest);
 				addTagCloseProposals(contentAssistRequest);
 			}
 			else {
 				if (sdRegion.getRegions().get(0).getType() != DOMRegionContext.XML_END_TAG_OPEN) {
 					int replaceLength = documentPosition - sdRegion.getStartOffset(completionRegion);
 					contentAssistRequest = newContentAssistRequest(node, node.getParentNode(), sdRegion, completionRegion, sdRegion.getStartOffset(completionRegion), replaceLength, matchString);
 					addTagNameProposals(contentAssistRequest, getElementPositionForModelQuery(nodeAtOffset));
 				}
 				else {
 					IDOMNode actualNode = (IDOMNode) node.getModel().getIndexedRegion(documentPosition);
 					if (actualNode != null) {
 						if (documentPosition >= sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength()) {
 							contentAssistRequest = newContentAssistRequest(actualNode, actualNode.getParentNode(), sdRegion, completionRegion, documentPosition, 0, matchString);
 						}
 						else {
 							contentAssistRequest = newContentAssistRequest(actualNode, actualNode.getParentNode(), sdRegion, completionRegion, sdRegion.getStartOffset(completionRegion), completionRegion.getTextLength(), matchString);
 						}
 						addEndTagNameProposals(contentAssistRequest);
 					}
 				}
 			}
 		}
 		return contentAssistRequest;
 	}
 
 	private int getElementPositionForModelQuery(Node child) {
 		return getElementPosition(child);
 		// return -1;
 	}
 	/**
 	 * Helper method to reuse functionality for getting context when no proposals are needed.
 	 * @param viewer
 	 * @param offset
 	 */
 	public void createContext(ITextViewer viewer, int offset) {
 		this.fDocument = (viewer == null ? null : viewer.getDocument());
 		this.fDocumentPosition = offset;
 		this.fContext = createContext();
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
 	 * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor#getMatchString(org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion, org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion, int)
 	 */
 	@Override
 	protected String getMatchString(IStructuredDocumentRegion parent, ITextRegion aRegion, int offset) {
 		String matchString =  super.getMatchString(parent, aRegion, offset);
 		String regionType = aRegion.getType();
 		if(regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE && matchString.startsWith("\"")) { //$NON-NLS-1$
 			matchString = matchString.substring(1);
 		}
 		return matchString;
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
 	}
 
 	/**
 	 * Calculates and adds the attribute value proposals to the Content Assist Request object
 	 */
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 //		System.out.println("AbstractXMLContentAssistProcessor: addAttributeValueProposals() invoked"); //$NON-NLS-1$
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
 	}
 	
 	/*
 	 * Calculates and adds the doc type proposals to the Content Assist Request object
 	 */
 	protected void addDocTypeProposal(ContentAssistRequest contentAssistRequest) {
 	}
 	
 	/*
 	 * Calculates and adds the empty document proposals to the Content Assist Request object
 	 */
 	protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest) {
 	}
 	
 	/*
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 */
 	protected void addEndTagNameProposals(ContentAssistRequest contentAssistRequest) {
 	}
 	
 	/*
 	 * Calculates and adds the end tag proposals to the Content Assist Request object
 	 */
 	protected void addEndTagProposals(ContentAssistRequest contentAssistRequest) {
 	}
 	
 	/*
 	 * Calculates and adds the enttity proposals to the Content Assist Request object
 	 */
 	protected void addEntityProposals(ContentAssistRequest contentAssistRequest, int documentPosition, ITextRegion completionRegion, IDOMNode treeNode) {
 		super.addEntityProposals(contentAssistRequest, documentPosition, completionRegion, treeNode);
 	}
 	
 	/*
 	 * Calculates and adds the PCDATA proposals to the Content Assist Request object
 	 */
 	protected void addPCDATAProposal(String nodeName, ContentAssistRequest contentAssistRequest) {
 	}
 	
 	/*
 	 * Calculates and adds the start document proposals to the Content Assist Request object
 	 */
 	protected void addStartDocumentProposals(ContentAssistRequest contentAssistRequest) {
 	}
 	
 	/*
 	 * Calculates and adds the tag close proposals to the Content Assist Request object
 	 * 
 	 */
 	protected void addTagCloseProposals(ContentAssistRequest contentAssistRequest) {
 		IDOMNode node = (IDOMNode) contentAssistRequest.getParent();
 		if (node.getNodeType() == Node.ELEMENT_NODE) {
 			int contentType = CMElementDeclaration.ANY;
 			// if it's XML and content doesn't HAVE to be element, add "/>"
 			// proposal.
 			boolean endWithSlashBracket = (getXML(node) && (contentType != CMElementDeclaration.ELEMENT));
 
 			Image image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 
 			// is the start tag ended properly?
 			if ((contentAssistRequest.getDocumentRegion() == node.getFirstStructuredDocumentRegion()) && !(node.getFirstStructuredDocumentRegion()).isEnded()) {
 				setErrorMessage(null);
 				// prompt with a close for the start tag
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, ">", //$NON-NLS-1$
 							getOffset(),
 							0, 2, image, NLS.bind(XMLUIMessages.Close_with__, (new Object[]{" '>'"})), //$NON-NLS-1$
 							null, null, TextProposal.R_CLOSE_TAG);
 				contentAssistRequest.addProposal(proposal);
 
 				// prompt with the closer for the start tag and an end tag
 				// if one is not present
 				if (node.getEndStructuredDocumentRegion() == null) {
 					// make sure tag name is actually what it thinks it
 					// is...(eg. <%@ vs. <jsp:directive)
 					IStructuredDocumentRegion sdr = contentAssistRequest.getDocumentRegion();
 					String openingTagText = (sdr != null) ? sdr.getFullText() : ""; //$NON-NLS-1$
 					if ((openingTagText != null) && (openingTagText.indexOf(node.getNodeName()) != -1)) {
 						proposal = new AutoContentAssistantProposal(true, "></" + node.getNodeName() + ">", //$NON-NLS-2$//$NON-NLS-1$
 								getOffset(),
 								0, 1, image, NLS.bind(XMLUIMessages.Close_with____, (new Object[]{node.getNodeName()})), null, null, TextProposal.R_CLOSE_TAG);
 						contentAssistRequest.addProposal(proposal);
 					}
 				}
 				// prompt with slash bracket "/>" incase if it's a self
 				// ending tag
 				if (endWithSlashBracket) {
 					proposal = new AutoContentAssistantProposal(true, "/>", //$NON-NLS-1$
 							getOffset(),
 							0, 1, image, NLS.bind(XMLUIMessages.Close_with__, (new Object[]{" \"/>\""})), //$NON-NLS-1$
 							null, null, TextProposal.R_CLOSE_TAG + 1); // +1 to bring to top of list
 					contentAssistRequest.addProposal(proposal);
 				}
 			}
 			else if ((contentAssistRequest.getDocumentRegion() == node.getLastStructuredDocumentRegion()) && !node.getLastStructuredDocumentRegion().isEnded()) {
 				setErrorMessage(null);
 				// prompt with a closing end character for the end tag
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, ">", //$NON-NLS-1$
 						getOffset(),
 						0, 1, image, NLS.bind(XMLUIMessages.Close_with__, (new Object[]{" '>'"})), //$NON-NLS-1$
 						null, null, TextProposal.R_CLOSE_TAG);
 				contentAssistRequest.addProposal(proposal);
 			}
 		}
 		else if (node.getNodeType() == Node.DOCUMENT_NODE) {
 			setErrorMessage(UNKNOWN_CONTEXT);
 		}
 	}
 	
 	/*
 	 * Calculates and adds the tag insertion proposals to the Content Assist Request object
 	 */
 	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
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
 		if (contentAssistRequest == null) {
 			IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 			contentAssistRequest = newContentAssistRequest((Node) treeNode, treeNode.getParentNode(), sdRegion, completionRegion, documentPosition, 0, ""); //$NON-NLS-1$
 		}
 		
 		String regionType = completionRegion.getType();
 		IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 
 		/*
 		 * Jeremy: Add attribute name proposals before  empty tag close
 		 */
 		if ((xmlnode.getNodeType() == Node.ELEMENT_NODE) || (xmlnode.getNodeType() == Node.DOCUMENT_NODE)) {
 			if (regionType == DOMRegionContext.XML_EMPTY_TAG_CLOSE) {
 				addAttributeNameProposals(contentAssistRequest);
 			} else if ((regionType == DOMRegionContext.XML_CONTENT) 
 					|| (regionType == DOMRegionContext.XML_CHAR_REFERENCE) 
 					|| (regionType == DOMRegionContext.XML_ENTITY_REFERENCE) 
 					|| (regionType == DOMRegionContext.XML_PE_REFERENCE)
 					|| (regionType == DOMRegionContext.BLOCK_TEXT)) {
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
 		if (resource == null)
 			return null;
 		
 		ELResolverFactoryManager elrfm = ELResolverFactoryManager.getInstance();
 		return elrfm.getResolvers(resource);
 	}
 	
 	private static final String[] EMPTY_TAGS = new String[0];
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
 				if(region != null) {
 					startOffset += region.getStart();
 				} else {
 					region = ((IDOMAttr)n).getEqualRegion();
 					if(region != null) {
 						startOffset += 	region.getStart() + region.getLength();
 					} else {
 						startOffset = ((IDOMAttr)n).getEndOffset();
 					}
 				}
 			} else if (n instanceof IDOMText) {
 				text = ((IDOMText)n).getNodeValue();
 				region = ((IDOMText)n).getFirstStructuredDocumentRegion();
 				startOffset = ((IDOMText)n).getStartOffset(); 
 			} else {
 				// The EL may appear only in TEXT and ATTRIBUTE VALUE types of node 
 				return null;
 			}
 
 			int inValueOffset = getOffset() - startOffset;
 			if (text != null && text.length() < inValueOffset) { // probably, the attribute value ends before the document position
 				return null;
 			}
 			if (inValueOffset<0) {
 				return null;
 			}
 			
 			String matchString = text.substring(0, inValueOffset);
 			
 			ELParser p = ELParserUtil.getJbossFactory().createParser();
 			ELModel model = p.parse(text);
 			
 			ELInstance is = ELUtil.findInstance(model, inValueOffset);// ELInstance
 			ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 			
 			boolean isELStarted = (model != null && is != null && (model.toString().startsWith("#{") ||  //$NON-NLS-1$
 					model.toString().startsWith("${"))); //$NON-NLS-1$
 			boolean isELClosed = (model != null && is != null && model.toString().endsWith("}")); //$NON-NLS-1$
 			
 //			boolean insideEL = startOffset + model.toString().length() 
 			TextRegion tr = new TextRegion(startOffset,  ie == null ? inValueOffset : ie.getStartPosition(), ie == null ? 0 : inValueOffset - ie.getStartPosition(), ie == null ? "" : ie.getText(), isELStarted, isELClosed); //$NON-NLS-1$
 			
 			return tr;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	public static class TextRegion {
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
