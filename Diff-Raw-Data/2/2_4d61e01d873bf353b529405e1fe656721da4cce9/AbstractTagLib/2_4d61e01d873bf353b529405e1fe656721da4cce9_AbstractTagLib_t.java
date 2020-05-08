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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.project.ext.IValueInfo;
 import org.jboss.tools.common.model.project.ext.event.Change;
 import org.jboss.tools.common.model.project.ext.store.XMLStoreConstants;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.internal.KbObject;
 import org.jboss.tools.jst.web.kb.internal.KbXMLStoreConstants;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.jboss.tools.jst.web.kb.taglib.IComponent;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.w3c.dom.Element;
 
 /**
  * Abstract implementation of ITagLibrary
  * @author Alexey Kazakov
  */
 public abstract class AbstractTagLib extends KbObject implements ITagLibrary {
 	public static String URI = "uri";
 
 	protected INameSpace nameSpace;
 	protected String uri;
 	protected Map<String, IComponent> components = new HashMap<String, IComponent>();
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.TagLibrary#getAllComponents()
 	 */
 	public IComponent[] getComponents() {
 		synchronized (components) {
 			return components.values().toArray(new IComponent[components.size()]);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.TagLibrary#getComponent(java.lang.String)
 	 */
 	public IComponent getComponent(String name) {
 		return components.get(name);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.TagLibrary#getComponents(java.lang.String)
 	 */
 	public IComponent[] getComponents(String nameTemplate) {
 		List<IComponent> list = new ArrayList<IComponent>();
 		IComponent[] comps = getComponents();
 		for (int i = 0; i < comps.length; i++) {
 			if(comps[i].getName().startsWith(nameTemplate)) {
 				list.add(comps[i]);
 			}
 		}
 		return list.toArray(new IComponent[list.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.TagLibrary#getComponents(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.PageContext)
 	 */
 	public IComponent[] getComponents(KbQuery query, IPageContext context) {
 		String prefix = getPrefix(query, context);
 		return getComponents(query, prefix, context);
 	}
 
 	private String getPrefix(KbQuery query, IPageContext context) {
 		String prefix = null;
 		Map<String, INameSpace> nameSpaces = context.getNameSpaces(query.getOffset());
 		if(nameSpaces!=null) {
 			INameSpace nameSpace = nameSpaces.get(getURI());
 			if(nameSpace!=null) {
 				prefix = nameSpace.getPrefix();
 			}
 		}
 		return prefix;
 	}
 
 	private static final IComponent[] EMPTY_ARRAY = new IComponent[0];
 
 	private IComponent[] getComponents(KbQuery query, String prefix, IPageContext context) {
 		String fullTagName = null;
 		boolean mask = false;
 		if(query.getType()==KbQuery.Type.TAG_NAME) {
 			fullTagName = query.getValue();
 			mask = query.isMask();
 		} else {
 			fullTagName = query.getLastParentTag();
 		}
 		if(fullTagName == null) {
 			return null;
 		}
 		if(mask) {
 			if(fullTagName.length()==0) {
 				return getComponents();
 			}
 			if(prefix==null) {
 				return getComponents(fullTagName);
 			}
 		}
 		String tagName = fullTagName;
 		int prefixIndex = fullTagName.indexOf(':');
 		String queryPrefix = null;
 		if(prefix!=null && prefixIndex>-1) {
 			queryPrefix = fullTagName.substring(0, prefixIndex);
 			if(prefixIndex<fullTagName.length()-1) {
 				tagName = fullTagName.substring(prefixIndex+1);
 			} else {
 				tagName = null;
 			}
 		}
 		if(mask) {
 			if(prefixIndex<0) {
 				if(prefix.startsWith(tagName)) {
 					return getComponents();
 				}
 				return EMPTY_ARRAY;
 			}
 			if(prefix.equals(queryPrefix)) {
 				if(tagName == null) {
 					return getComponents();
 				}
 				return getComponents(tagName);
 			}
 			return EMPTY_ARRAY;
 		}
 		return new IComponent[]{getComponent(tagName)};
 	}
 
 	/**
 	 * Adds component to tag lib.
 	 * @param component
 	 */
 	public void addComponent(IComponent component) {
 		adopt((KbObject)component);
 		components.put(component.getName(), component);
 	}
 
 	/**
 	 * @param components the components to set
 	 */
 	protected void setComponents(Map<String, IComponent> components) {
 		this.components = components;
 	}
 
 	public IPath getSourcePath() {
 		return source;
 	}
 
 	public void setSourcePath(IPath source) {
 		this.source = source;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.TagLibrary#getResource()
 	 */
 	public IResource getResource() {
 		if(resource != null) return resource;
 		if(source != null) {
 			resource = ResourcesPlugin.getWorkspace().getRoot().getFile(source);
 		}
 		return resource;
 	}
 
 	/**
 	 * @param resource the resource to set
 	 */
 	public void setResource(IFile resource) {
 		this.resource = resource;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getNameSpace()
 	 */
 	public INameSpace getDefaultNameSpace() {
 		return nameSpace;
 	}
 
 	/**
 	 * @param nameSpace the name space to set
 	 */
 	public void setDefaultNameSpace(INameSpace nameSpace) {
 		this.nameSpace = nameSpace;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.TagLibrary#getURI()
 	 */
 	public String getURI() {
 		return uri;
 	}
 
 	/**
 	 * @param uri the URI to set
 	 */
 	public void setURI(String uri) {
 		this.uri = uri;
 	}
 
 	public void setURI(IValueInfo s) {
 		uri = s == null ? null : s.getValue();
 		attributesInfo.put(URI, s);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.ProposalProcessor#getProposals(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.PageContext)
 	 */
 	public TextProposal[] getProposals(KbQuery query, IPageContext context) {
 		String prefix = getPrefix(query, context);
 		List<TextProposal> proposals = new ArrayList<TextProposal>();
 		IComponent[] components = getComponents(query, prefix, context);
 		if(query.getType() == KbQuery.Type.TAG_NAME) {
 			for (int i = 0; i < components.length; i++) {
 				TextProposal proposal = new TextProposal();
 				proposal.setContextInfo(components[i].getDescription());
 				StringBuffer label = new StringBuffer();
 				if(prefix!=null) {
 					label.append(prefix + KbQuery.PREFIX_SEPARATOR);
 				}
 				label.append(components[i].getName());
 				proposal.setLabel(label.toString());
 
 				IAttribute[] attributes = components[i].getPreferableAttributes();
 				StringBuffer attributeSB = new StringBuffer();
 				for (int j = 0; j < attributes.length; j++) {
 					attributeSB.append(" ").append(attributes[j].getName()).append("=\"\"");
 				}
 				label.append(attributeSB);
 				if(!components[i].canHaveBody()) {
 					label.append(" /");
 				}
 
 				proposal.setReplacementString(label.toString());
 
 				int position = proposal.getReplacementString().indexOf('"');
 				if(position!=-1) {
 					position ++;
 				} else {
 					position = proposal.getReplacementString().length();
 				}
 				proposal.setPosition(position);
 				proposals.add(proposal);
 			}
 		} else {
 			for (int i = 0; i < components.length; i++) {
				if (components[i] == null)
					continue;
 				TextProposal[] componentProposals  = components[i].getProposals(query, context);
 				for (int j = 0; j < componentProposals.length; j++) {
 					proposals.add(componentProposals[j]);
 				}
 			}
 		}
 		return proposals.toArray(new TextProposal[proposals.size()]);
 	}
 
 	public AbstractTagLib clone() throws CloneNotSupportedException {
 		AbstractTagLib t = (AbstractTagLib)super.clone();
 		t.components = new HashMap<String, IComponent>();
 		for (IComponent c: components.values()) {
 			t.addComponent(((AbstractComponent)c).clone());
 		}
 		t.components.putAll(components);
 		return t;
 	}
 
 	public List<Change> merge(KbObject s) {
 		List<Change> changes = super.merge(s);
 		//TODO
 		return changes;
 	}
 
 	public String getXMLName() {
 		return KbXMLStoreConstants.TAG_LIBRARY;
 	}
 	
 	public Element toXML(Element parent, Properties context) {
 		Element element = super.toXML(parent, context);
 
 		XModelObject old = pushModelObject(context);
 
 		saveAttributeValues(element);
 
 		for (IComponent c: components.values()) {
 			((KbObject)c).toXML(element, context);
 		}
 
 		popModelObject(context, old);
 		return element;
 	}
 
 	protected void saveAttributeValues(Element element) {
 	}
 
 	public void loadXML(Element element, Properties context) {
 		super.loadXML(element, context);
 
 		XModelObject old = pushModelObject(context);
 
 		loadAttributeValues(element);
 
 		Element[] cs = XMLUtilities.getChildren(element, KbXMLStoreConstants.TAG_COMPONENT);
 		for (Element e: cs) {
 			String cls = e.getAttribute(XMLStoreConstants.ATTR_CLASS);
 			AbstractComponent c = null;
 			if(KbXMLStoreConstants.CLS_TLD_LIBRARY.equals(cls)) {
 				c = new TLDTag();
 			} else if(KbXMLStoreConstants.CLS_FACELET_LIBRARY.equals(cls)) {
 				c = new FaceletTag();
 			} else if(KbXMLStoreConstants.CLS_FACESCONFIG_LIBRARY.equals(cls)) {
 				c = new FacesConfigComponent();
 			} else {
 				//consider other cases;
 			}
 			if(c != null) {
 				c.loadXML(e, context);
 				addComponent(c);
 			}
 		}
 
 		popModelObject(context, old);
 	}
 
 	protected void loadAttributeValues(Element element) {
 		setURI(attributesInfo.get(URI));
 	}
 }
