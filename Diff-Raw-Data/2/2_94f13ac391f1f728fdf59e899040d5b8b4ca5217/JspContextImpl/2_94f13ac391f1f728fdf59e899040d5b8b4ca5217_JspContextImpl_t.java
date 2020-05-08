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
 package org.jboss.tools.jst.web.kb.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.jst.web.kb.ICSSContainerSupport;
 import org.jboss.tools.jst.web.kb.IIncludedContextSupport;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.IResourceBundle;
 import org.jboss.tools.jst.web.kb.IXmlContext;
 import org.jboss.tools.jst.web.kb.PageContextFactory.CSSStyleSheetDescriptor;
 import org.jboss.tools.jst.web.kb.internal.taglib.NameSpace;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.INameSpaceExtended;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 /**
  * JSP page context
  * @author Alexey Kazakov
  */
 public class JspContextImpl extends XmlContextImpl implements IPageContext, IIncludedContextSupport, ICSSContainerSupport {
 	protected List<IResourceBundle> bundles;
 
 	protected List<ELContext> fIncludedContexts = null;
 	protected List<CSSStyleSheetDescriptor> fCSSStyleSheetDescriptors = null;
 
 	public void addIncludedContext(ELContext includedContext) {
 		if (fIncludedContexts == null) {
 			fIncludedContexts = new ArrayList<ELContext>();
 		}
 		fIncludedContexts.add(includedContext);
 	}
 
 	public List<ELContext> getIncludedContexts() {
 		return fIncludedContexts;
 	}
 
 	@Override
 	public Map<String, List<INameSpace>> getNameSpaces(int offset) {
 		Map<String, List<INameSpace>> superNameSpaces = super.getNameSpaces(offset);
 		
 		List<INameSpace> fakeForHtmlNS = new ArrayList<INameSpace>();
 		fakeForHtmlNS.add(new NameSpace("", "")); //$NON-NLS-1$ //$NON-NLS-2$
 		superNameSpaces.put("", fakeForHtmlNS); //$NON-NLS-1$
 		
 		return superNameSpaces;
 	}
 
 	@Override
 	public Var[] getVars(int offset) {
 		Var[] thisVars = super.getVars(offset);
 		
 		List<Var> includedVars = new ArrayList<Var>();
 		List<ELContext> includedContexts = getIncludedContexts();
 		if (includedContexts != null) {
 			for (ELContext includedContext : includedContexts) {
 				if (!(includedContext instanceof IXmlContext))
 					continue;
 				
 				Var[] vars = ((IXmlContext)includedContext).getVars(offset);
 				if (vars != null) {
 					for (Var b : vars) {
 						includedVars.add(b);
 					}
 				}
 			}
 		}
 		
 		Var[] result = new Var[thisVars == null ? 0 : thisVars.length + includedVars.size()];
 		if (thisVars != null && thisVars.length > 0) {
 			System.arraycopy(thisVars, 0, result, 0, thisVars.length);
 		}
 		if (!includedVars.isEmpty()) {
			System.arraycopy(includedVars.toArray(new Var[includedVars.size()]), 0, 
 					result, thisVars == null ? 0 : thisVars.length, includedVars.size());
 		}
 		return result;
 	}
 
 	public ITagLibrary[] getLibraries() {
 		List<ITagLibrary> libraries = new ArrayList<ITagLibrary>();
 		
 		for (Map<String, INameSpace> nsMap : nameSpaces.values()) {
 			for (INameSpace ns : nsMap.values()) {
 				if (ns instanceof INameSpaceExtended) {
 					ITagLibrary[] libs = ((INameSpaceExtended)ns).getTagLibraries();
 					if (libs != null) {
 						for(ITagLibrary lib : libs) {
 							libraries.add(lib);
 						}
 					}
 				}
 			}
 		}
 
 		List<ELContext> includedContexts = getIncludedContexts();
 		if (includedContexts != null) {
 			for (ELContext includedContext : includedContexts) {
 				ITagLibrary[] includedLibraries = includedContext instanceof IPageContext ? ((IPageContext)includedContext).getLibraries() : null;
 				if (includedLibraries != null) {
 					for (ITagLibrary lib : includedLibraries) {
 						libraries.add(lib);
 					}
 				}
 			}
 		}
 		
 		return libraries.toArray(new ITagLibrary[libraries.size()]);
 	}
 
 	/**
 	 * Adds resource bundle to the context
 	 * 
 	 * @param bundle
 	 */
 	public void addResourceBundle(IResourceBundle bundle) {
 		if (bundles == null) {
 			bundles = new ArrayList<IResourceBundle>();
 		}
 		bundles.add(bundle);
 	}
 
 	public IResourceBundle[] getResourceBundles() {
 		List<IResourceBundle> resourceBundles = new ArrayList<IResourceBundle>();
 		if (bundles != null) {
 			resourceBundles.addAll(bundles);
 		}
 		
 		List<ELContext> includedContexts = getIncludedContexts();
 		if (includedContexts != null) {
 			for (ELContext includedContext : includedContexts) {
 				IResourceBundle[] includedBundles = includedContext instanceof IPageContext ? ((IPageContext)includedContext).getResourceBundles() : null;
 				if (includedBundles != null) {
 					for (IResourceBundle b : includedBundles) {
 						resourceBundles.add(b);
 					}
 				}
 			}
 		}
 
 		return (IResourceBundle[])resourceBundles.toArray(new IResourceBundle[resourceBundles.size()]);
 	}
 
 	public void addCSSStyleSheetDescriptor(CSSStyleSheetDescriptor cssStyleSheetDescriptor) {
 		if (fCSSStyleSheetDescriptors == null) {
 			fCSSStyleSheetDescriptors = new ArrayList<CSSStyleSheetDescriptor>();
 		}
 		fCSSStyleSheetDescriptors.add(cssStyleSheetDescriptor);
 	}
 
 	public List<CSSStyleSheetDescriptor> getCSSStyleSheetDescriptors() {
 		List<CSSStyleSheetDescriptor> descrs = new ArrayList<CSSStyleSheetDescriptor>();
 		
 		if (fCSSStyleSheetDescriptors != null) {
 			for (CSSStyleSheetDescriptor descr : fCSSStyleSheetDescriptors) {
 				descrs.add(descr);
 			}
 		}
 		
 		List<ELContext> includedContexts = getIncludedContexts();
 		if (includedContexts != null) {
 			for (ELContext includedContext : includedContexts) {
 				if (includedContext instanceof ICSSContainerSupport) {
 					List<CSSStyleSheetDescriptor> includedSheetDescriptors = ((ICSSContainerSupport)includedContext).getCSSStyleSheetDescriptors();
 					if (includedSheetDescriptors != null) {
 						descrs.addAll(includedSheetDescriptors);
 					}
 				}
 			}
 		}
 		
 		return descrs;
 	}	
 }
