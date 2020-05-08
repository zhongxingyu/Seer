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
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.xml.core.internal.document.NodeContainer;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.IResourceBundle;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.internal.JspContextImpl;
 import org.jboss.tools.jst.web.kb.internal.ResourceBundle;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.TagLibriryManager;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * 
  * @author Jeremy
  *
  */
 public class JspContentAssistProcessor extends XmlContentAssistProcessor {
 
 	protected static final Image JSF_EL_PROPOSAL_IMAGE = JspEditorPlugin.getDefault().getImage(JspEditorPlugin.CA_JSF_EL_IMAGE_PATH);
 	
 	/**
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.XmlContentAssistProcessor#createContext()
 	 */
 	@Override
 	protected IPageContext createContext() {
 		ELContext superContext = super.createContext();
 		
 		IFile file = getResource();
 		
 		JspContextImpl context = new JspContextImpl();
 		context.setResource(superContext.getResource());
 		context.setElResolvers(superContext.getElResolvers());
 		context.setDocument(getDocument());
 		setVars(context, file);
 		setNameSpaces(context);
 		context.setLibraries(getTagLibraries(context));
 		context.setResourceBundles(getResourceBundles(context));
 		
 		return context;
 	}
 
 	/**
 	 * Collects the namespaces over the JSP-page and sets them up to the context specified.
 	 * 
 	 * @param context
 	 */
 	protected void setNameSpaces(JspContextImpl context) {
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null) 
 				return;
 			
 			Document xmlDocument = (sModel instanceof IDOMModel) ? 
 							((IDOMModel) sModel).getDocument() : 
 								null;
 
 			if (xmlDocument == null)
 				return;
 
 			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(getDocument());
 			List trackers = (manager == null? null : manager.getCMDocumentTrackers(getOffset()));
 			for (int i = 0; trackers != null && i < trackers.size(); i++) {
 				TaglibTracker tt = (TaglibTracker)trackers.get(i);
 				final String prefix = tt.getPrefix();
 				final String uri = tt.getURI();
 				if (prefix != null && prefix.trim().length() > 0 &&
 						uri != null && uri.trim().length() > 0) {
 						
 					IRegion region = new Region(0, getDocument().getLength());
 					INameSpace nameSpace = new INameSpace(){
 					
 						public String getURI() {
 							return uri.trim();
 						}
 					
 						public String getPrefix() {
 							return prefix.trim();
 						}
 					};
 					context.addNameSpace(region, nameSpace);
 				}
 			}
 
 			return;
 		}
 		finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	private static final ITagLibrary[] EMPTY_LIBRARIES = new ITagLibrary[0];
 	
 	/**
 	 * Returns the Tag Libraries for the namespaces collected in the context.
 	 * Important: The context must be created using createContext() method before using this method.
 	 * 
 	 * @param context The context object instance
 	 * @return
 	 */
	public ITagLibrary[] getTagLibraries(IPageContext context) {
 		Map<String, INameSpace> nameSpaces =  context.getNameSpaces(getOffset());
 		if (nameSpaces == null || nameSpaces.isEmpty())
 			return EMPTY_LIBRARIES;
 		
 		List<ITagLibrary> tagLibraries = new ArrayList<ITagLibrary>();
 		for (INameSpace nameSpace : nameSpaces.values()) {
 			ITagLibrary[] libs = TagLibriryManager.getLibraries(context.getResource().getProject(), nameSpace.getURI());
 			if (libs != null && libs.length > 0) {
 				for (ITagLibrary lib : libs) {
 					tagLibraries.add(lib);
 				}
 			}
 		} 
 		return (tagLibraries.isEmpty() ? EMPTY_LIBRARIES :
 				(ITagLibrary[])tagLibraries.toArray(new ITagLibrary[tagLibraries.size()]));
 	}
 	
 	/**
 	 * Returns the resource bundles  
 	 * 
 	 * @return
 	 */
 	protected IResourceBundle[] getResourceBundles(IPageContext context) {
 		List<IResourceBundle> list = new ArrayList<IResourceBundle>();
 		IStructuredModel sModel = StructuredModelManager.getModelManager().getExistingModelForRead(getDocument());
 		if (sModel == null) 
 			return new IResourceBundle[0];
 		try {
 			Document dom = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel).getDocument() : null;
 			if (dom != null) {
 				Element element = dom.getDocumentElement();
 				NodeList children = (NodeContainer)dom.getChildNodes();
 				if (element != null) {
 					for (int i = 0; children != null && i < children.getLength(); i++) {
 						IDOMNode xmlnode = (IDOMNode)children.item(i);
 						update((IDOMNode)xmlnode, context, list);
 					}
 				}
 			}
 		}
 		finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 			
 		return list.toArray(new IResourceBundle[list.size()]);
 	}
 
 	private void update(IDOMNode element, IPageContext context, List<IResourceBundle> list) {
 		if (element !=  null) {
 			registerBundleForNode(element, context, list);
 			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
 				if (child instanceof IDOMNode) {
 					update((IDOMNode)child, context, list);
 				}
 			}
 		}
 	}
 	private void registerBundleForNode(IDOMNode node, IPageContext context, List<IResourceBundle> list) {
 		if (node == null) return;
 		String name = node.getNodeName();
 		if (name == null) return;
 		if (!name.endsWith("loadBundle")) return; //$NON-NLS-1$
 		if (name.indexOf(':') == -1) return;
 		String prefix = name.substring(0, name.indexOf(':'));
 
 		Map<String, INameSpace> ns = context.getNameSpaces(node.getStartOffset());
 		if (!containsPrefix(ns, prefix)) return;
 
 		NamedNodeMap attributes = node.getAttributes();
 		if (attributes == null) return;
 		String basename = (attributes.getNamedItem("basename") == null ? null : attributes.getNamedItem("basename").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
 		String var = (attributes.getNamedItem("var") == null ? null : attributes.getNamedItem("var").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
 		if (basename == null || basename.length() == 0 ||
 			var == null || var.length() == 0) return;
 
 		list.add(new ResourceBundle(basename, var));
 	}
 	private boolean containsPrefix(Map<String, INameSpace> ns, String prefix) {
 		for (INameSpace n: ns.values()) {
 			if(prefix.equals(n.getPrefix())) return true;
 		}
 		return false;
 	}
 
 	
 
 	/**
 	 * Returns the <code>org.jboss.tools.common.el.core.resolver.ELContext</code> instance
 	 * 
 	 * @return
 	 */
 	@Override
 	public IPageContext getContext() {
 		return (IPageContext)super.getContext();
 	}
 
 	/**
 	 * Returns URI string for the prefix specified using the namespaces collected for 
 	 * the {@link IPageContext} context.
 	 * Important: The context must be created using createContext() method before using this method.
 	 * 
 	 * @param prefix
 	 * @return
 	 */
 	@Override
 	public String getUri(String prefix) {
 		if (prefix == null || prefix.length() == 0)
 			return null;
 		
 		Map<String, INameSpace> nameSpaces = getContext().getNameSpaces(getOffset());
 		if (nameSpaces == null || nameSpaces.isEmpty())
 			return null;
 		
 		for (INameSpace nameSpace : nameSpaces.values()) {
 			if (prefix.equals(nameSpace.getPrefix())) {
 				return nameSpace.getURI();
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * Checks if the specified attribute exists 
 	 * 
 	 * @param attrName Name of attribute to check
 	 */
 	protected boolean isExistingAttribute(String attrName) {
 		IStructuredModel sModel = StructuredModelManager.getModelManager()
 				.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null)
 				return false;
 
 			Document xmlDocument = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 
 			if (xmlDocument == null)
 				return false;
 
 			// Get Fixed Structured Document Region
 			IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 			if (sdFixedRegion == null)
 				return false;
 			
 			Node n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
 			if (n == null)
 				return false;
 			
 			// Find the first parent tag
 			if (!(n instanceof Element)) {
 				if (n instanceof Attr) {
 					n = ((Attr) n).getOwnerElement();
 				} else {
 					return false;
 				}
 			}
 			
 			if (n == null)
 				return false;
 
 			return (((Element)n).getAttribute(attrName) != null);
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 
 	}
 	
 	/**
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition) {
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix();
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = "<" + matchString; //$NON-NLS-1$
 					
 			KbQuery kbQuery = createKbQuery(Type.TAG_NAME, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				String replacementString = textProposal.getReplacementString();
 				String closingTag = textProposal.getLabel();
 				if (closingTag != null && closingTag.startsWith("<")) { //$NON-NLS-1$
 					closingTag = closingTag.substring(1);
 				}
 
 				if (!replacementString.endsWith("/>")) { //$NON-NLS-1$
 					replacementString += "</" + closingTag + ">"; //$NON-NLS-1$ //$NON-NLS-2$
 				}
 
 
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				if (image == null) {
 					image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 				}
 				String displayString = closingTag; //$NON-NLS-1$
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_TAG_INSERTION;
 				}
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 		return;
 	}
 
 
 	/**
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagNameProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition) {
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = "<" + matchString; //$NON-NLS-1$
 					
 			KbQuery kbQuery = createKbQuery(Type.TAG_NAME, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				String replacementString = textProposal.getReplacementString();
 				String closingTag = textProposal.getLabel();
 				if (closingTag != null && closingTag.startsWith("<")) { //$NON-NLS-1$
 					closingTag = closingTag.substring(1);
 				}
 				
 				if (replacementString.startsWith("<")) { //$NON-NLS-1$
 					// Because the tag starting char is already in the text
 					replacementString = replacementString.substring(1);
 				}
 				if (!replacementString.endsWith("/>")) { //$NON-NLS-1$
 					replacementString += "</" + closingTag + ">"; //$NON-NLS-1$ //$NON-NLS-2$
 				}
 
 			
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				if (image == null) {
 					image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 				}
 
 				String displayString = closingTag; //$NON-NLS-1$
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_TAG_INSERTION;
 				}
 
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 		return;
 	}
 	
 	
 	/**
 	 * Calculates and adds the attribute name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = matchString;
 					
 			KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_NAME, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				if (isExistingAttribute(textProposal.getLabel())) 
 					continue;
 				
 				String replacementString = textProposal.getReplacementString() + "=\"\""; //$NON-NLS-1$
 				
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				String displayString = textProposal.getLabel();
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 	}
 
 	/**
 	 * Calculates and adds the attribute value proposals to the Content Assist Request object
 	 */
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix();
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = matchString;
 
 			KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				String replacementString = "\"" + textProposal.getReplacementString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				String displayString = textProposal.getLabel();
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_JSP_ATTRIBUTE_VALUE;
 				}
 
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 	}
 
 	/**
 	 * Calculates and adds the EL proposals to the Content Assist Request object
 	 */
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest) {
 	}
 
 	/**
 	 * Calculates and adds the EL proposals to the Content Assist Request object
 	 */
 	@Override
 	protected void addAttributeValueELProposals(ContentAssistRequest contentAssistRequest) {
 		try {
 			TextRegion prefix = getELPrefix();
 			if (prefix == null) {
 				return;
 			}
 
 			if(!prefix.isELStarted()) {
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, "#{}",  //$NON-NLS-1$
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
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				int replacementOffset = beginChangeOffset;
 				int replacementLength = prefix.getLength();
 				String replacementString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 				int cursorPosition = replacementString.length();
 				Image image = textProposal.getImage();
 				
 				String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString(); 
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = (textProposal.getContextInfo() == null ? "" : textProposal.getContextInfo()); //$NON-NLS-1$
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
 		} finally {
 		}
 	}
 
 }
