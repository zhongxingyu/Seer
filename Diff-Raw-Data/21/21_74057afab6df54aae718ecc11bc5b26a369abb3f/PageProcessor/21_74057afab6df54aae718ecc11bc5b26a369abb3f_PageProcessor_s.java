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
 package org.jboss.tools.jst.web.kb;
 
 import java.util.ArrayList;
 
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibAttribute;
 import org.jboss.tools.jst.web.kb.taglib.CustomTagLibManager;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.jboss.tools.jst.web.kb.taglib.IComponent;
 import org.jboss.tools.jst.web.kb.taglib.ICustomTagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 /**
  * @author Alexey Kazakov
  */
 public class PageProcessor implements IProposalProcessor {
 
 	private static final PageProcessor INSTANCE = new PageProcessor();
 	private ICustomTagLibrary[] customTagLibs;
 	private CustomTagLibAttribute[] componentExtensions;
 
 	/**
 	 * @return instance of PageProcessor
 	 */
 	public static PageProcessor getInstance() {
 		return INSTANCE;
 	}
 
 	private PageProcessor() {
 		customTagLibs = CustomTagLibManager.getInstance().getLibraries();
 		componentExtensions = CustomTagLibManager.getInstance().getComponentExtensions();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.ProposalProcessor#getProposals(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.PageContext)
 	 */
 	public TextProposal[] getProposals(KbQuery query, IPageContext context) {
 		ArrayList<TextProposal> proposals = new ArrayList<TextProposal>();
 		ITagLibrary[] libs =  context.getLibraries();
 		for (int i = 0; libs != null && i < libs.length; i++) {
 			TextProposal[] libProposals = libs[i].getProposals(query, context);
 			for (int j = 0; libProposals != null && j < libProposals.length; j++) {
 				proposals.add(libProposals[j]);
 			}
 		}
		for (int i = 0; i < customTagLibs.length; i++) {
 			// TODO			
 		}
 		if(query.getType() == KbQuery.Type.ATTRIBUTE_VALUE || ((query.getType() == KbQuery.Type.TEXT )&& context instanceof IFaceletPageContext)) {
 			String value = query.getValue();
 			 //TODO convert value to EL string.
 			String elString = value;
 			ELResolver[] resolvers =  context.getElResolvers();
 			for (int i = 0; resolvers != null && i < resolvers.length; i++) {
 				proposals.addAll(resolvers[i].getCompletions(elString, !query.isMask(), query.getOffset(), context));
 			}
 		}
 
 		return proposals.toArray(new TextProposal[proposals.size()]);
 	}
 
 	/**
 	 * Returns components
 	 * @param query
 	 * @param context
 	 * @return components
 	 */
 	public IComponent[] getComponents(KbQuery query, IPageContext context) {
 		ArrayList<IComponent> components = new ArrayList<IComponent>();
 		ITagLibrary[] libs =  context.getLibraries();
 		for (int i = 0; i < libs.length; i++) {
 			IComponent[] libComponents = libs[i].getComponents(query, context);
 			for (int j = 0; j < libComponents.length; j++) {
 				components.add(libComponents[i]);
 			}
 		}
 		return components.toArray(new IComponent[components.size()]);
 	}
 
 	/**
 	 * Returns attributes
 	 * @param query
 	 * @param context
 	 * @return attributes
 	 */
 	public IAttribute[] getAttributes(KbQuery query, IPageContext context) {
 		ArrayList<IAttribute> attributes = new ArrayList<IAttribute>();
 		if(query.getType() == KbQuery.Type.ATTRIBUTE_NAME || query.getType() == KbQuery.Type.ATTRIBUTE_VALUE) {
 			IComponent[] components  = getComponents(query, context);
 			for (int i = 0; i < components.length; i++) {
 				IAttribute[] libAttributess = components[i].getAttributes(query, context);
 				for (int j = 0; j < libAttributess.length; j++) {
 					attributes.add(libAttributess[i]);
 				}
 			}
 		}
 		return attributes.toArray(new IAttribute[attributes.size()]);
 	}
 }
