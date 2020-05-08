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
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.parser.ELParserFactory;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELCompletionEngine;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELContextImpl;
 import org.jboss.tools.common.el.core.resolver.ELResolution;
 import org.jboss.tools.common.el.core.resolver.ELResolutionImpl;
 import org.jboss.tools.common.el.core.resolver.ElVarSearcher;
 import org.jboss.tools.common.el.core.resolver.IRelevanceCheck;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * 
  * @author Jeremy
  *
  */
 @SuppressWarnings("restriction")
 public class JspContentAssistProcessor extends XmlContentAssistProcessor {
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#createContext()
 	 */
 	@Override
 	protected ELContext createContext() {
 		return PageContextFactory.createPageContext(getResource(), PageContextFactory.JSP_PAGE_CONTEXT_TYPE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor#getContext()
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
 		if (prefix == null)
 			return null;
 		
 		Map<String, List<INameSpace>> nameSpaces = getContext().getNameSpaces(getOffset());
 		if (nameSpaces == null || nameSpaces.isEmpty())
 			return null;
 		
 		for (List<INameSpace> nameSpace : nameSpaces.values()) {
 			for (INameSpace n : nameSpace) {
 				if (prefix.equals(n.getPrefix())) {
 					return n.getURI();
 				}
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
 
 			return (((Element)n).getAttributeNode(attrName) != null);
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	/**
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * The method is to be overridden here because jsp disallows to use EL-s inside a text region
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition) {
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		
 		addTagNameProposals(contentAssistRequest, childPosition, true);
 	}
 
 	/**
 	 * Calculates and adds the attribute name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
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
 			if (image == null) {
 				image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
 			}
 
 			String displayString = textProposal.getLabel() == null ? 
 					replacementString : 
 						textProposal.getLabel();
 			IContextInformation contextInformation = null;
 			String additionalProposalInfo = textProposal.getContextInfo();
 			int relevance = textProposal.getRelevance();
 
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 					replacementOffset, replacementLength, cursorPosition, image, displayString, 
 					contextInformation, additionalProposalInfo, relevance);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 
 	/**
 	 * Calculates and adds the EL proposals to the Content Assist Request object
 	 */
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest) {
 	}
 
 	protected void setVars(ELContextImpl context, IFile file) {
 		ELCompletionEngine fakeEngine = new ELCompletionEngine() {
 
 			public ELResolution resolveELOperand(IFile file,
 					ELExpression operand, boolean returnEqualedVariablesOnly,
					List<Var> vars, ElVarSearcher varSearcher)
 					throws BadLocationException, StringIndexOutOfBoundsException {
 				return new ELResolutionImpl(operand);
 			}
 
 			public ELParserFactory getParserFactory() {
 				return ELParserUtil.getJbossFactory();
 			}
 
 			public List<TextProposal> getProposals(ELContext context, String el, int offset) {
 				return Collections.emptyList();
 			}
 
 			public ELResolution resolve(ELContext context, ELExpression operand, int offset) {
 				return new ELResolutionImpl(operand);
 			}
 
 			public List<TextProposal> getProposals(ELContext context, int offset) {
 				return Collections.emptyList();
 			}
 
 			public IRelevanceCheck createRelevanceCheck(IJavaElement element) {
 				return null;
 			}
 		};
 		ElVarSearcher varSearcher = new ElVarSearcher(file, fakeEngine);
 		List<Var> vars = varSearcher.findAllVars(file, getOffset());
 
 		if (vars != null) {
 			for (Var var : vars) {
 				context.addVar(new Region(getOffset(), 0), var);
 			}
 		}
 	}
 }
