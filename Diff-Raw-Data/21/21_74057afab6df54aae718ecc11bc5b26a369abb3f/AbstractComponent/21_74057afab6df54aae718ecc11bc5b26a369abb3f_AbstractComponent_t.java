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
 
 import org.jboss.tools.common.model.project.ext.IValueInfo;
 import org.jboss.tools.common.model.project.ext.event.Change;
 import org.jboss.tools.common.model.project.ext.store.XMLStoreConstants;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.internal.KbObject;
 import org.jboss.tools.jst.web.kb.internal.KbXMLStoreConstants;
 import org.jboss.tools.jst.web.kb.taglib.Facet;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.jboss.tools.jst.web.kb.taglib.IComponent;
 import org.w3c.dom.Element;
 
 /**
  * Abstract implementation of IComponent
  * @author Alexey Kazakov
  */
 public abstract class AbstractComponent extends KbObject implements IComponent {
 	public static final String DESCRIPTION = "description";
 	public static final String COMPONENT_CLASS = "component-class";
 	public static final String COMPONENT_TYPE = "component-type";
 	public static final String BODY_CONTENT = "bodycontent";
 
 	protected boolean canHaveBody;
 	protected String componentClass;
 	protected String componentType;
 	protected String description;
 	protected String name;
 	private Map<String, IAttribute> attributes = new HashMap<String, IAttribute>();
 	private IAttribute[] attributesArray;
 	private Map<String, IAttribute> preferableAttributes = new HashMap<String, IAttribute>();
 	private IAttribute[] preferableAttributesArray;
 	private Map<String, IAttribute> requiredAttributes = new HashMap<String, IAttribute>();
 	private IAttribute[] requiredAttributesArray;
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#canHaveBody()
 	 */
 	public boolean canHaveBody() {
 		return canHaveBody;
 	}
 
 	/**
 	 * @param canHaveBody
 	 */
 	public void setCanHaveBody(boolean canHaveBody) {
 		this.canHaveBody = canHaveBody;
 	}
 
 	/**
 	 * @param s
 	 */
 	public void setCanHaveBody(IValueInfo s) {
 		canHaveBody = s == null || "empty".equals(s.getValue());
 		attributesInfo.put(BODY_CONTENT, s);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getAttribute(java.lang.String)
 	 */
 	public IAttribute getAttribute(String name) {
 		return attributes.get(name);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getAttributes()
 	 */
 	public IAttribute[] getAttributes() {
 		if(attributesArray==null) {
 			synchronized (attributes) {
 				attributesArray = attributes.values().toArray(new IAttribute[attributes.size()]);
 			}
 		}
 		return attributesArray;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getAttributes(java.lang.String)
 	 */
 	public IAttribute[] getAttributes(String nameTemplate) {
 		List<IAttribute> list = new ArrayList<IAttribute>();
 		IAttribute[] atts = getAttributes();
 		for (int i = 0; i < atts.length; i++) {
 			if(atts[i].getName().startsWith(nameTemplate)) {
 				list.add(atts[i]);
 			}
 		}
 		return list.toArray(new IAttribute[list.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getAttributes(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.IPageContext)
 	 */
 	public IAttribute[] getAttributes(KbQuery query, IPageContext context) {
 		String attrName = null;
 		boolean mask = false;
 		if(query.getType()==KbQuery.Type.ATTRIBUTE_NAME) {
 			attrName = query.getValue();
 			mask = query.isMask();
 		} else if(query.getType()==KbQuery.Type.ATTRIBUTE_VALUE) {
 			attrName = query.getParent();
 		}
 		if(attrName == null) {
 			return null;
 		}
 		if(mask) {
 			return getAttributes(attrName);
 		}
 		return new IAttribute[]{getAttribute(attrName)};
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getComponentClass()
 	 */
 	public String getComponentClass() {
 		return componentClass;
 	}
 
 	/**
 	 * @param componentClass the component class name to set
 	 */
 	public void setComponentClass(String componentClass) {
 		this.componentClass = componentClass;
 	}
 
 	public void setComponentClass(IValueInfo s) {
 		componentClass = s == null ? null : s.getValue();
 		attributesInfo.put(COMPONENT_CLASS, s);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getComponentType()
 	 */
 	public String getComponentType() {
 		return componentType;
 	}
 
 	/**
 	 * @param componentType the component type to set
 	 */
 	public void setComponentType(String componentType) {
 		this.componentType = componentType;
 	}
 
 	/**
 	 * 
 	 * @param s
 	 */
 	public void setComponentType(IValueInfo s) {
 		componentType = s == null ? null : s.getValue();
 		attributesInfo.put(COMPONENT_TYPE, s);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getDescription()
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @param s
 	 */
 	public void setDescription(IValueInfo s) {
 		description = s == null ? null : s.getValue();
 		attributesInfo.put(DESCRIPTION, s);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getName()
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @param s
 	 */
 	public void setName(IValueInfo s) {
 		name = s == null ? null : s.getValue();
 		attributesInfo.put(XMLStoreConstants.ATTR_NAME, s);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getPreferableAttributes()
 	 */
 	public IAttribute[] getPreferableAttributes() {
 		if(preferableAttributesArray==null) {
 			synchronized (preferableAttributes) {
 				preferableAttributesArray = preferableAttributes.values().toArray(new IAttribute[preferableAttributes.size()]);
 			}
 		}
 		return preferableAttributesArray;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getRequiredAttributes()
 	 */
 	public IAttribute[] getRequiredAttributes() {
 		if(requiredAttributesArray==null) {
 			synchronized (requiredAttributes) {
 				requiredAttributesArray = requiredAttributes.values().toArray(new IAttribute[requiredAttributes.size()]);
 			}
 		}
 		return requiredAttributesArray;
 	}
 
 	/**
 	 * Facets are a feature of JSF only, they are included into 
 	 * the base interface and implementation
 	 * for the sake of common approach.
 	 */
 	public Facet getFacet(String name) {
 		return null;
 	}
 	
 	public static final Facet[] EMPTY_FACET_SET = new Facet[0];
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getFacets()
 	 */
 	public Facet[] getFacets() {
 		return EMPTY_FACET_SET;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#getFacets(java.lang.String)
 	 */
 	public Facet[] getFacets(String nameTemplate) {
 		return EMPTY_FACET_SET;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IProposalProcessor#getProposals(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.IPageContext)
 	 */
 	public TextProposal[] getProposals(KbQuery query, IPageContext context) {
 		IAttribute[] attributes = getAttributes(query, context);
 		if(attributes.length == 0) {
 			return EMPTY_PROPOSAL_LIST;
 		}
 		List<TextProposal> proposals = null;
 		if(query.getType() == KbQuery.Type.ATTRIBUTE_NAME) {
 			proposals = new ArrayList<TextProposal>();
 			for (int i = 0; i < attributes.length; i++) {
 				TextProposal proposal = new TextProposal();
 				proposal.setContextInfo(attributes[i].getDescription());
 				proposal.setReplacementString(attributes[i].getName());
 				proposal.setLabel(attributes[i].getName());
 				proposals.add(proposal);
 			}
 		} else if(query.getType() == KbQuery.Type.ATTRIBUTE_VALUE) {
 			for (int i = 0; i < attributes.length; i++) {
				TextProposal[] attributeProposals  = (attributes[i] == null ? null : attributes[i].getProposals(query, context));
				if(attributeProposals != null && attributeProposals.length > 0 && proposals == null) {
 					proposals = new ArrayList<TextProposal>();
 				}
				for (int j = 0; attributeProposals != null && j < attributeProposals.length; j++) {
 					proposals.add(attributeProposals[j]);
 				}
 			}
 		}
 		if(proposals == null || proposals.isEmpty()) {
 			return EMPTY_PROPOSAL_LIST;
 		}
 		return proposals.toArray(new TextProposal[proposals.size()]);
 	}
 
 	/**
 	 * Adds the attribute to the component.
 	 * @param attribute
 	 */
 	public void addAttribute(IAttribute attribute) {
 		adopt((KbObject)attribute);
 		attributes.put(attribute.getName(), attribute);
 		if(attribute.isPreferable()) {
 			preferableAttributes.put(attribute.getName(), attribute);
 		}
 		if(attribute.isRequired()) {
 			requiredAttributes.put(attribute.getName(), attribute);
 		}
 		clearAttributeArrays();
 	}
 
 	private void clearAttributeArrays() {
 		attributesArray = null;
 		preferableAttributesArray = null;
 		requiredAttributesArray = null;
 	}
 
 	/**
 	 * Removes the attribute from the component
 	 * @param attribute
 	 */
 	public void removeAttribute(IAttribute attribute) {
 		attributes.remove(attribute.getName());
 		preferableAttributes.remove(attribute.getName());
 		requiredAttributes.remove(attribute.getName());
 		clearAttributeArrays();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.taglib.IComponent#isExtended()
 	 */
 	public boolean isExtended() {
 		// Return false by default
 		return false;
 	}
 
 	/**
 	 * @param extended
 	 */
 	public void setExtended(boolean extended) {
 		// Do nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.KbObject#clone()
 	 */
 	@Override
 	public AbstractComponent clone() throws CloneNotSupportedException {
 		AbstractComponent copy = (AbstractComponent)super.clone();
 		copy.attributes = new HashMap<String, IAttribute>();
 		copy.preferableAttributes = new HashMap<String, IAttribute>();
 		copy.requiredAttributes = new HashMap<String, IAttribute>();
 		IAttribute[] as = getAttributes();
 		for (IAttribute a: as) {
 			copy.addAttribute(((AbstractAttribute)a).clone());
 		}
 		return copy;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.KbObject#merge(org.jboss.tools.jst.web.kb.internal.KbObject)
 	 */
 	@Override
 	public List<Change> merge(KbObject s) {
 		List<Change> changes = super.merge(s);
 	
 		AbstractComponent c = (AbstractComponent)s;
 		if(!stringsEqual(name, c.name)) {
 			changes = Change.addChange(changes, new Change(this, XMLStoreConstants.ATTR_NAME, name, c.name));
 			name = c.name;
 		}
 		if(!stringsEqual(description, c.description)) {
 			changes = Change.addChange(changes, new Change(this, DESCRIPTION, description, c.description));
 			description = c.description;
 		}
 		if(!stringsEqual(componentClass, c.componentClass)) {
 			changes = Change.addChange(changes, new Change(this, COMPONENT_CLASS, componentClass, c.componentClass));
 			componentClass = c.componentClass;
 		}
 		if(!stringsEqual(componentType, c.componentType)) {
 			changes = Change.addChange(changes, new Change(this, COMPONENT_CLASS, componentType, c.componentType));
 			componentType = c.componentType;
 		}
 		if(canHaveBody != c.canHaveBody) {
 			changes = Change.addChange(changes, new Change(this, BODY_CONTENT, "" + canHaveBody, "" + c.canHaveBody));
 			canHaveBody = c.canHaveBody;
 		}
 
 		Change children = new Change(this, null, null, null);
 		mergeAttributes(c, children);
 		changes = Change.addChange(changes, children);
 
 		return changes;
 	}
 
 	/**
 	 * @param c
 	 * @param children
 	 */
 	public void mergeAttributes(AbstractComponent c, Change children) {
 		Map<Object,AbstractAttribute> attributeMap = new HashMap<Object, AbstractAttribute>();
 		for (IAttribute a: getAttributes()) attributeMap.put(((KbObject)a).getId(), (AbstractAttribute)a);
 		for (IAttribute a: c.getAttributes()) {
 			AbstractAttribute loaded = (AbstractAttribute)a;
 			AbstractAttribute current = attributeMap.get(loaded.getId());
 			if(current == null) {
 				addAttribute(loaded);
 				Change change = new Change(this, null, null, loaded);
 				children.addChildren(Change.addChange(null, change));
 			} else {
 				List<Change> rc = current.merge(loaded);
 				if(rc != null) children.addChildren(rc);
 			}
 		}
 		for (IAttribute a: attributeMap.values()) {
 			AbstractAttribute removed = (AbstractAttribute)a;
 			if(attributes.get(removed.getName()) == removed) {
 				attributes.remove(removed.getName());
 				Change change = new Change(this, null, removed, null);
 				children.addChildren(Change.addChange(null, change));
 				clearAttributeArrays();
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.KbObject#getXMLName()
 	 */
 	@Override
 	public String getXMLName() {
 		return KbXMLStoreConstants.TAG_COMPONENT;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.KbObject#toXML(org.w3c.dom.Element, java.util.Properties)
 	 */
 	@Override
 	public Element toXML(Element parent, Properties context) {
 		Element element = super.toXML(parent, context);
 
 		if(attributesInfo.get(XMLStoreConstants.ATTR_NAME) == null && name != null) {
 			element.setAttribute(XMLStoreConstants.ATTR_NAME, name);
 		}
 
 		for (IAttribute c: getAttributes()) {
 			((KbObject)c).toXML(element, context);
 		}
 
 		return element;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.KbObject#loadXML(org.w3c.dom.Element, java.util.Properties)
 	 */
 	@Override
 	public void loadXML(Element element, Properties context) {
 		super.loadXML(element, context);
 	
 		setName(attributesInfo.get(XMLStoreConstants.ATTR_NAME));
 		if(name == null && element.hasAttribute(XMLStoreConstants.ATTR_NAME)) {
 			name = element.getAttribute(XMLStoreConstants.ATTR_NAME);
 		}
 		setDescription(attributesInfo.get(DESCRIPTION));
 		setComponentClass(attributesInfo.get(COMPONENT_CLASS));
 		setCanHaveBody(attributesInfo.get(BODY_CONTENT));
 		setComponentType(attributesInfo.get(COMPONENT_TYPE));
 
 		Element[] cs = XMLUtilities.getChildren(element, KbXMLStoreConstants.TAG_ATTRIBUTE);
 		for (Element e: cs) {
 			String cls = e.getAttribute(XMLStoreConstants.ATTR_CLASS);
 			AbstractAttribute c = null;
 			if(KbXMLStoreConstants.CLS_TLD_LIBRARY.equals(cls)) {
 				c = new TLDAttribute();
 			} else if(KbXMLStoreConstants.CLS_FACESCONFIG_LIBRARY.equals(cls)) {
 				c = new FacesConfigAttribute();
 			} else {
 				//consider other cases;
 			}
 			if(c != null) {
 				c.loadXML(e, context);
 				addAttribute(c);
 			}
 		}
 	}
 }
