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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibAttribute;
 import org.jboss.tools.jst.web.kb.taglib.CustomTagLibManager;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.jboss.tools.jst.web.kb.taglib.IComponent;
 import org.jboss.tools.jst.web.kb.taglib.IContextComponent;
 import org.jboss.tools.jst.web.kb.taglib.ICustomTagLibComponent;
 import org.jboss.tools.jst.web.kb.taglib.ICustomTagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.IFacesConfigTagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibRecognizer;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 /**
  * @author Alexey Kazakov
  */
 public class PageProcessor {
 
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
 
 	/**
 	 * 
 	 * @param query
 	 * @param context
 	 * @return
 	 */
 	public TextProposal[] getProposals(KbQuery query, ELContext context) {
 		return getProposals(query, context, false);
 	}
 
 	private List<TextProposal> excludeExtendedComponents(List<TextProposal> proposals) {
 		Map<String, Set<TextProposal>> runtimeComponentMap = new HashMap<String, Set<TextProposal>>();
 		Map<String, TextProposal> customComponentMap = new HashMap<String, TextProposal>();
 		Set<TextProposal> customNotExtendedComponents = new HashSet<TextProposal>();
 		for (TextProposal proposal : proposals) {
 			Object source = proposal.getSource();
 			if(source instanceof IComponent) {
 				IComponent component = (IComponent)source;
 				String name = component.getTagLib().getURI() + ":" + component.getName(); //$NON-NLS-1$
 				if(component instanceof ICustomTagLibComponent) {
 					if(component.isExtended()) {
 						customComponentMap.put(name, proposal);
 					} else {
 						customNotExtendedComponents.add(proposal);
 					}
 				} else {
 					Set<TextProposal> textProposals = runtimeComponentMap.get(name);
 					if(textProposals==null) {
 						textProposals = new HashSet<TextProposal>();
 					}
 					textProposals.add(proposal);
 					runtimeComponentMap.put(name, textProposals);
 				}
 			}
 		}
 		if(!customComponentMap.isEmpty()) {
 			proposals.clear();
 			for (String name : runtimeComponentMap.keySet()) {
 				TextProposal customProposal = customComponentMap.get(name);
 				if(customProposal!=null) {
 					proposals.add(customProposal);
 				} else {
 					proposals.addAll(runtimeComponentMap.get(name));
 				}
 			}
 			if(!customNotExtendedComponents.isEmpty()) {
 				proposals.addAll(customNotExtendedComponents);
 			}
 		}
 		return proposals;
 	}
 
 	/**
 	 * 
 	 * @param query
 	 * @param context
 	 * @return
 	 */
 	public TextProposal[] getProposals(KbQuery query, ELContext context, boolean preferCustomComponentExtensions) {
 		List<TextProposal> proposals = new ArrayList<TextProposal>();
 
 		if (!isQueryForELProposals(query, context)) {
 			if(context instanceof IPageContext) {
 				IPageContext pageContext = (IPageContext)context;
 				ITagLibrary[] libs =  pageContext.getLibraries();
 				for (int i = 0; libs != null && i < libs.length; i++) {
 					if(libs[i] instanceof IFacesConfigTagLibrary) {
 						continue;
 					}
 					TextProposal[] libProposals = libs[i].getProposals(query, pageContext);
 					for (int j = 0; libProposals != null && j < libProposals.length; j++) {
 						proposals.add(libProposals[j]);
 					}
 				}
 				if (query.getType() == KbQuery.Type.ATTRIBUTE_VALUE) {
 					Map<String, IAttribute> attrbMap = new HashMap<String, IAttribute>();
 					for (TextProposal proposal : proposals) {
 						if(proposal.getSource()!=null && proposal.getSource() instanceof IAttribute) {
 							IAttribute att = (IAttribute)proposal.getSource();
 							attrbMap.put(att.getName(), att);
 						}
 					}
 					IAttribute[] attrs = getAttributes(query, pageContext, false);
 					for (int i = 0; i < attrs.length; i++) {
 						attrbMap.put(attrs[i].getName(), attrs[i]);
 					}
 					for (int i = 0; i < componentExtensions.length; i++) {
 						if(attrbMap.containsKey(componentExtensions[i].getName())) {
 							TextProposal[] attProposals = componentExtensions[i].getProposals(query, pageContext);
 							for (int j = 0; j < attProposals.length; j++) {
 								proposals.add(attProposals[j]);
 							}
 						}
 					}
 				}
 				for (int i = 0; customTagLibs != null && i < customTagLibs.length; i++) {
 					if(shouldLoadLib(customTagLibs[i], context)) {
 						TextProposal[] libProposals = customTagLibs[i].getProposals(query, pageContext);
 						for (int j = 0; libProposals != null && j < libProposals.length; j++) {
 							proposals.add(libProposals[j]);
 						}
 					}
 				}
 				if(preferCustomComponentExtensions && query.getType() == KbQuery.Type.TAG_NAME) {
 					proposals = excludeExtendedComponents(proposals);
 				}
 			}
 		} else {
 			String value = query.getValue();
 			String elString = value;
 			ELResolver[] resolvers =  context.getElResolvers();
 			for (int i = 0; resolvers != null && i < resolvers.length; i++) {
 				List<TextProposal> pls = resolvers[i].getProposals(context, elString, query.getOffset());
 				if(pls!=null) {
 					proposals.addAll(pls);
 				}
 			}
 		}
 		return proposals.toArray(new TextProposal[proposals.size()]);
 	}
 
 	private boolean shouldLoadLib(ICustomTagLibrary lib, ELContext context) {
 		ITagLibRecognizer recognizer = lib.getRecognizer();
 		return recognizer==null || recognizer.shouldBeLoaded(lib, context);
 	}
 
 	private boolean isQueryForELProposals(KbQuery query, ELContext context) {
 		if (query.getType() == KbQuery.Type.ATTRIBUTE_VALUE ||
 				(query.getType() == KbQuery.Type.TEXT &&
 						(context instanceof IFaceletPageContext ||
 								context instanceof XmlContextImpl))) {
			return (query.getValue() != null && 
					(query.getValue().startsWith("#{") || //$NON-NLS-1$
						query.getValue().startsWith("${"))); //$NON-NLS-1$
 
 		}
 		return false;
 	}
 	
  	/**
 	 * Returns components
 	 * @param query
 	 * @param context
 	 * @return components
 	 */
 	public IComponent[] getComponents(KbQuery query, IPageContext context) {
 		return getComponents(query, context, false);
 	}
 
 	public IComponent[] getComponents(KbQuery query, IPageContext context, boolean includeComponentExtensions) {
 		ArrayList<IComponent> components = new ArrayList<IComponent>();
 		ITagLibrary[] libs =  context.getLibraries();
 		for (int i = 0; i < libs.length; i++) {
 			if(libs[i] instanceof IFacesConfigTagLibrary) {
 				continue;
 			}
 			IComponent[] libComponents = libs[i].getComponents(query, context);
 			for (int j = 0; j < libComponents.length; j++) {
 				if(includeComponentExtensions || !libComponents[j].isExtended()) {
 					components.add(libComponents[j]);
 				}
 			}
 		}
 		for (int i = 0; customTagLibs != null && i < customTagLibs.length; i++) {
 			if(shouldLoadLib(customTagLibs[i], context)) {
 				IComponent[] libComponents = customTagLibs[i].getComponents(query, context);
 				for (int j = 0; j < libComponents.length; j++) {
 					if(includeComponentExtensions || !libComponents[j].isExtended()) {
 						components.add(libComponents[j]);
 					}
 				}
 			}
 		}
 		return components.toArray(new IComponent[components.size()]);
 	}
 
 	private final static IAttribute[] EMPTY_ATTRIBUTE_ARRAY = new IAttribute[0];
 
 	/**
 	 * Returns attributes
 	 * @param query
 	 * @param context
 	 * @return attributes
 	 */
 	public IAttribute[] getAttributes(KbQuery query, IPageContext context) {
 		return getAttributes(query, context, true);
 	}
 
 	private IAttribute[] getAttributes(KbQuery query, IPageContext context, boolean includeComponentExtensions) {
 		if(query.getType() == KbQuery.Type.ATTRIBUTE_NAME || query.getType() == KbQuery.Type.ATTRIBUTE_VALUE) {
 			ArrayList<IAttribute> attributes = new ArrayList<IAttribute>();
 			Map<String, IAttribute> attrbMap = new HashMap<String, IAttribute>();
 			IComponent[] components  = getComponents(query, context, includeComponentExtensions);
 			for (int i = 0; i < components.length; i++) {
 				IComponent component = components[i];
 				IAttribute[] libAttributess;
 				if(component instanceof IContextComponent) {
 					libAttributess = ((IContextComponent)component).getAttributes(context, query, true);
 				} else {
 					libAttributess = component.getAttributes(query, context);
 				}
 
 				if(libAttributess!=null) {
 					for (int j = 0; j < libAttributess.length; j++) {
 						attributes.add(libAttributess[j]);
 						attrbMap.put(libAttributess[j].getName(), libAttributess[j]);
 					}
 				}
 			}
 			if(includeComponentExtensions) {
 				for (int i = 0; i < componentExtensions.length; i++) {
 					if(attrbMap.containsKey(componentExtensions[i].getName())) {
 						attributes.add(componentExtensions[i]);
 					}
 				}
 			}
 			return attributes.toArray(new IAttribute[attributes.size()]);
 		}
 		return EMPTY_ATTRIBUTE_ARRAY;
 	}
 
 	public Map<String, IAttribute> getAttributesAsMap(KbQuery query, IPageContext context) {
 		IAttribute[] as = getAttributes(query, context);
 		Map<String, IAttribute> map = new HashMap<String, IAttribute>();
 		for (IAttribute a: as) {
 			String n = a.getName();
 			if(map.containsKey(n)) {
 				IAttribute o = map.get(n);
 				int pa = (a.isPreferable() || a.isRequired()) ? 2 : 0;
 				int po = (o.isPreferable() || o.isRequired()) ? 2 : 0;
 				pa += (a instanceof CustomTagLibAttribute) ? 1 : 0;
 				po += (o instanceof CustomTagLibAttribute) ? 1 : 0;
 				if(pa <= po) {				
 					continue;
 				}
 			}
 			map.put(n, a);
 		}
 		return map;
 	}
 }
