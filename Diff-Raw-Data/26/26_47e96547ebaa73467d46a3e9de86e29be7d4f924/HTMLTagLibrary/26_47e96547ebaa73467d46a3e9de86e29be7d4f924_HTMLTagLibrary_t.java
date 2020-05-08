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
 package org.jboss.tools.jst.web.kb.internal.taglib;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Map;
 import java.util.Set;
 
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.taglib.IComponent;
import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Alexey Kazakov
  */
 public class HTMLTagLibrary extends CustomTagLibrary {
 
 	public HTMLTagLibrary(File file, String uri, String version, String name) {
 		super(file, uri, version, name);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.AbstractTagLib#getComponents(org.jboss.tools.jst.web.kb.KbQuery, java.lang.String, org.jboss.tools.jst.web.kb.IPageContext)
 	 */
 	@Override
 	protected IComponent[] getComponents(KbQuery query, String prefix, IPageContext context) {
 		String[] parentTags = query.getParentTags();
 		String parentHtmlTagName = null;
 		if(parentTags!=null && parentTags.length>0) {
 			for (int i = parentTags.length-1; i >= 0; i--) {
 				if(parentTags[i].indexOf(':')<0) {
 					parentHtmlTagName = parentTags[i];
 					break;
 				}
 			}
 		}
 		IComponent[] components = super.getComponents(query, prefix, context);
 		if(parentHtmlTagName==null) {
 			return components;
 		}
 		IComponent parentComponent = getComponent(parentHtmlTagName.toLowerCase());
 		if(parentComponent==null) {
 			return components;
 		}
 		List<IComponent> result = new ArrayList<IComponent>();
 		HTMLTag parentHtmlTag = (HTMLTag)parentComponent;
 		Set<String> allowedChildTags = parentHtmlTag.getChildTags();
 		for (IComponent component : components) {
 			if(allowedChildTags.contains(component.getName())) {
 				result.add(component);
 			}
 		}
 		return result.toArray(new IComponent[0]);
 	}
 
	private static final List<String> EMPTY_PREFIXES = new ArrayList<String>();
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.AbstractTagLib#getPrefixes(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.IPageContext)
 	 */
 	@Override
 	protected List<String> getPrefixes(KbQuery query, IPageContext context) {

		Map<String, List<INameSpace>> nameSpaces = context.getNameSpaces(query.getOffset());
		if(nameSpaces!=null) {
			List<INameSpace> nameSpace = nameSpaces.get(getURI());
			if(nameSpace!=null) {
				for (INameSpace n : nameSpace) {
					String sPrefix = n.getPrefix();
					
					// JBIDE-4923: This fake URI is added by JSP- or FaceletPage- Content Assist Processors 
					// due to indicate that HTML schemas are to be used is query.
					// For the Xml Content Assist Processor we need to return an empty prefix list (but not null)
					// due to prevent HTML schemas usage
					// 
					if ("".equals(sPrefix)) //$NON-NLS-1$
						return null;
				}
			}
		}
		return EMPTY_PREFIXES;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibrary#parseComponent(org.w3c.dom.Element)
 	 */
 	@Override
 	protected CustomTagLibComponent parseComponent(Element component) {
 		HTMLTag tag = (HTMLTag)super.parseComponent(component);
 		NodeList components = component.getElementsByTagName(COMPONENT);
 		for(int i=0; i<components.getLength(); i++) {
 			Element child = (Element)components.item(i);
 			String name = child.getAttribute(NAME);
 			tag.addChildTagName(name.toLowerCase());
 		}
 		return tag;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibrary#createComponent()
 	 */
 	@Override
 	protected CustomTagLibComponent createComponent() {
 		return new HTMLTag();		
 	}
 }
