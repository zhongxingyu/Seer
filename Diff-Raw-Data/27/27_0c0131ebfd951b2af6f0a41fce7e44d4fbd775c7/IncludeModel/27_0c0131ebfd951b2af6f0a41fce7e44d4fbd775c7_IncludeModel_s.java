 /******************************************************************************* 
  * Copyright (c) 2012 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.include;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.w3c.dom.Element;
 
 /**
  * 
  * @author Viacheslav Kabanovich
  *
  */
 public class IncludeModel implements IIncludeModel {
 	private Map<IPath, List<PageInclude>> directReferences = new HashMap<IPath, List<PageInclude>>();
 	private Map<IPath, List<PageInclude>> parentReferences = new HashMap<IPath, List<PageInclude>>();
 
 	public IncludeModel() {}
 
 	public synchronized void clean(IPath path) {
 		List<PageInclude> old = directReferences.remove(path);
 		if(old != null && !old.isEmpty()) {
 			for (IPath child: parentReferences.keySet()) {
 				Iterator<PageInclude> is = parentReferences.get(child).iterator();
 				while(is.hasNext()) {
 					PageInclude i = is.next();
 					if(i.getParent().equals(path)) {
 						is.remove();
 					}
 				}
 			}
 		}
 	}
 
 	public synchronized void addInclude(IPath path, PageInclude include) {
 		List<PageInclude> current = directReferences.get(path);
 		if(current == null) {
 			current = new ArrayList<PageInclude>();
 			directReferences.put(path, current);
 		}
 		current.add(include);
 		IPath child = include.getPath();
 		List<PageInclude> is = parentReferences.get(child);
 		if(is == null) {
 			is = new ArrayList<PageInclude>();
 			parentReferences.put(child, is);
 		}
 		is.add(include);
 	}
 
 	public synchronized List<Var> getVars(IPath path) {
 		List<Var> result = new ArrayList<Var>();
 		List<PageInclude> is = parentReferences.get(path);
 		if(is != null) {
 			for (PageInclude i: is) {
 				result.addAll(i.getVars());
 			}
 		}
 		return result;
 	}
 
 	static final String STORE_ELEMENT_INCLUDES = "includes"; //$NON-NLS-1$
 	static final String STORE_ELEMENT_PAGE = "page"; //$NON-NLS-1$
 	static final String STORE_ELEMENT_INCLUDE = "include"; //$NON-NLS-1$
 	static final String STORE_ELEMENT_VAR = "var"; //$NON-NLS-1$
 
 	static final String STORE_ATTR_PATH = "path"; //$NON-NLS-1$
 	static final String STORE_ATTR_NAME = "name"; //$NON-NLS-1$
 	static final String STORE_ATTR_VALUE = "value"; //$NON-NLS-1$
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidationContext#store(org.w3c.dom.Element)
 	 */
 	public synchronized void store(Element root) {
 		Map<String, String> pathAliases = loadAliases(root);
 		Element includes = XMLUtilities.createElement(root, STORE_ELEMENT_INCLUDES);
 		for (IPath path : directReferences.keySet()) {
 			Element page = XMLUtilities.createElement(includes, STORE_ELEMENT_PAGE);
 			String pathAlias = ELReference.getAlias(pathAliases, path.toString());
 			page.setAttribute(STORE_ATTR_PATH, pathAlias);
 			List<PageInclude> is = directReferences.get(path);
 			for (PageInclude i: is) {
 				Element includeElement = XMLUtilities.createElement(page, STORE_ELEMENT_INCLUDE);
 				String pathAlias1 = ELReference.getAlias(pathAliases, i.getPath().toString());
 				includeElement.setAttribute(STORE_ATTR_PATH, pathAlias1);
 				List<Var> vars = i.getVars();
 				for (Var var: vars) {
 					Element varElement = XMLUtilities.createElement(includeElement, STORE_ELEMENT_VAR);
 					varElement.setAttribute(STORE_ATTR_NAME, var.getName());
 					varElement.setAttribute(STORE_ATTR_VALUE, var.getValue());
 				}
 			}
 		}
 		
 		Element aliases = XMLUtilities.getUniqueChild(root, "aliases"); //$NON-NLS-1$
 		if(aliases == null) {
 			aliases = XMLUtilities.createElement(root, "aliases"); //$NON-NLS-1$
 		}
 		for (String path: pathAliases.keySet()) {
 			String value = pathAliases.get(path);
 			Element alias = XMLUtilities.createElement(aliases, "alias"); //$NON-NLS-1$
 			alias.setAttribute(STORE_ATTR_PATH, path);
 			alias.setAttribute(STORE_ATTR_VALUE, value);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidationContext#load(org.w3c.dom.Element)
 	 */
 	public synchronized void load(Element root) {
 		Map<String, String> pathAliases = loadAliases(root);
 
 		Element includes = XMLUtilities.getUniqueChild(root, STORE_ELEMENT_INCLUDES);
 		if(includes == null) return;
 		Element[] pages = XMLUtilities.getChildren(includes, STORE_ELEMENT_PAGE);
 		for (Element page : pages) {
 			String spath = page.getAttribute(STORE_ATTR_PATH);
 			if(spath == null || spath.trim().length() == 0) continue;
 			spath = ELReference.getPath(pathAliases, spath);
 			IPath path = new Path(spath);
 			clean(path);
 			Element[] is = XMLUtilities.getChildren(page, STORE_ELEMENT_INCLUDE);
 			for (Element includeElement: is) {
 				List<Var> vars = new ArrayList<Var>();
 				String path1 = includeElement.getAttribute(STORE_ATTR_PATH);
 				if(path1 == null || path1.trim().length() == 0) continue;
 				Element[] vs = XMLUtilities.getChildren(includeElement, STORE_ELEMENT_VAR);
 				for (Element v: vs) {
 					String name = v.getAttribute(STORE_ATTR_NAME);
 					String value = v.getAttribute(STORE_ATTR_VALUE);
 					Var var = new Var(ELParserUtil.getJbossFactory(), name, value, 0, 0);  //TODO
 					vars.add(var);
 				}
 				path1 = ELReference.getPath(pathAliases, path1);
 				addInclude(path, new PageInclude(path, new Path(path1), vars));
 			}
 		}
 	}
 
 	Map<String, String> loadAliases(Element root) {
 		Map<String, String> pathAliases = new HashMap<String, String>();
 		Element aliases = XMLUtilities.getUniqueChild(root, "aliases"); //$NON-NLS-1$
 		if(aliases != null) {
 			Element[] aliasArray = XMLUtilities.getChildren(aliases, "alias"); //$NON-NLS-1$
 			for (Element alias: aliasArray) {
 				String path = alias.getAttribute(STORE_ATTR_PATH);
 				String value = alias.getAttribute(STORE_ATTR_VALUE);
 				pathAliases.put(value, path);
 			}
 		}
 		return pathAliases;
 	}
 }
