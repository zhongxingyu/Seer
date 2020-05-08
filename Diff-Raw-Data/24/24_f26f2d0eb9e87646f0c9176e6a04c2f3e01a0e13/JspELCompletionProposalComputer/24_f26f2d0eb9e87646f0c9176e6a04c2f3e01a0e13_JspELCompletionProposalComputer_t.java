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
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Region;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
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
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 
 /**
  * EL Proposal computer for JSP pages
  * 
  * @author Jeremy
  *
  */
 @SuppressWarnings("restriction")
 public class JspELCompletionProposalComputer extends XmlELCompletionProposalComputer {
 
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
 
 	protected void setVars(ELContextImpl context, IFile file) {
 		ELCompletionEngine fakeEngine = new ELCompletionEngine() {
 
 			public ELResolution resolveELOperand(IFile file,
 					ELExpression operand, boolean returnEqualedVariablesOnly,
 					List<Var> vars, ElVarSearcher varSearcher, int offset)
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
 
 	@Override
 	protected int getTagInsertionBaseRelevance() {
 		return TextProposal.R_TAG_INSERTION;
 	}
 
 	
 }
