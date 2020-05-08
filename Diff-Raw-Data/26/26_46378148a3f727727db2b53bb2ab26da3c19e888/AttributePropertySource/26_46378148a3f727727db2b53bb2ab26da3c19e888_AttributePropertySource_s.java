 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.properties;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.jst.jsf.common.metadata.Entity;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataDomainContext;
 import org.eclipse.jst.jsf.common.metadata.query.internal.MetaDataQueryContextFactory;
 import org.eclipse.jst.jsf.common.metadata.query.internal.MetaDataQueryFactory;
 import org.eclipse.jst.jsf.common.metadata.query.internal.taglib.ITaglibDomainMetaDataQuery;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.ITagAttribute;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.ITagElement;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.ITaglibContextResolver;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IWorkspaceContextResolver;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.internal.IStructuredDocumentContextResolverFactory2;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.designtime.internal.resolver.ITagElementResolver;
 import org.eclipse.jst.jsf.metadataprocessors.MetaDataEnabledProcessingFactory;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.commands.single.ChangeAttributeCommand;
 import org.eclipse.jst.pagedesigner.editors.properties.IPropertyPageDescriptor;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.eclipse.ui.views.properties.PropertyDescriptor;
 import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.w3c.dom.Element;
 
 /**
  * 
  * @author mengbo
  */
 public class AttributePropertySource implements IPropertySource {
 	private IDOMElement _element;
 
 	private IPropertySource _innerSource;
 
 	private Entity _tagEntity;
 
 	private IStructuredDocumentContext _context;
 
 	private IPropertyDescriptor[] _descriptors;
 
 	private ITaglibDomainMetaDataQuery _query;
 
 	/**
 	 * Constructor
 	 * @param ele
 	 * @param source
 	 */
 	public AttributePropertySource(Element ele, IPropertySource source) {
 		_element = (IDOMElement) ele;
 		_innerSource = source;
 		_tagEntity = getTagEntity();
 	}
 
 	private Entity getTagEntity() {
 		_context = 
 				IStructuredDocumentContextFactory.INSTANCE.getContext(_element.getStructuredDocument(), _element);
 		if (_context == null) 
 			return null;
 		
 		IWorkspaceContextResolver wsresolver = 
 				IStructuredDocumentContextResolverFactory.INSTANCE.getWorkspaceContextResolver(_context);
 		if (wsresolver == null) 
 			return null;
 		
 		ITaglibContextResolver resolver = 
 				IStructuredDocumentContextResolverFactory.INSTANCE.getTaglibContextResolver(_context);
 		if (resolver == null) return null;
 		
 		String uri = resolver.getTagURIForNodeName(_element);
 		
 		//TODO: make below better
 		if (uri == null){
 			if (_element.getNamespaceURI() != null && _element.getNamespaceURI().equals("http://java.sun.com/JSP/Page")) //$NON-NLS-1$
 				uri = "JSP11"; //$NON-NLS-1$
 			else
 				uri = "HTML"; //$NON-NLS-1$
 		}
 		final IMetaDataDomainContext context = MetaDataQueryContextFactory.getInstance().createTaglibDomainModelContext((IFile)wsresolver.getResource());
 		_query = MetaDataQueryFactory.getInstance().createQuery(context);
 		return _query.getQueryHelper().getEntity(uri, _element.getLocalName());		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
 	 */
 	public Object getEditableValue() {
 		return _innerSource.getEditableValue();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
 	 */
 	public Object getPropertyValue(Object id) {
 		// CR377844: when the attribute in source is "a&gt;b", we would like to
 		// display
 		// "a>b" in cell editor. But _innerSource.getPropertyValue(id) will
 		// return the source
 		// of the attribute, so can't use that here.
 		// read QTS log for detail.
 		// return _innerSource.getPropertyValue(id);
 		if (id == null) {
 			return null;
 		}
 		String name = id.toString();
 		String value = _element.getAttribute(name);
 		if (value == null) {
 			value = ""; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
 	 */
 	public boolean isPropertySet(Object id) {
 		return _innerSource.isPropertySet(id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
 	 */
 	public void resetPropertyValue(Object id) {
 		_innerSource.resetPropertyValue(id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	public void setPropertyValue(final Object id, final Object value) {
 		Object oldValue = getPropertyValue(id);
 		if (oldValue == value || (oldValue != null && oldValue.equals(value))) {
 			return;
 		}
 		Command c = new ChangeAttributeCommand(
 				PDPlugin
 						.getResourceString("AttributePropertySource.CommandLabel.ChangeAttribute"), _element, (String) id, (String) value); //$NON-NLS-1$
 		c.execute();
 	}
 
 	/**
 	 * the major job of this wrapper is to provide tag attribute property descriptors 
 	 */
 	public IPropertyDescriptor[] getPropertyDescriptors() {
 		if (_descriptors == null) {
 			final List result = new ArrayList();
 			
 			final ITagElementResolver tagregResolver = IStructuredDocumentContextResolverFactory2.INSTANCE
 					.getResolver(_context, ITagElementResolver.class);
 			
 			if (tagregResolver != null) { //relies on getTagEntity() having been called to have setup the _element
 				final ITagElement tag = tagregResolver.getTagElement(_element);
 				if (tag != null) {
 					for (final ITagAttribute attr : tag.getAttributes().values()) {
 						final IPropertyDescriptor pd = getAttrPropertyDescriptor(attr
 								.getName());
 						if (pd != null)
 							result.add(new PropertyDescriptorWrapper(_element,
 									pd));// ,
 							// getStatusLineManager()));
 						else {
 							final PropertyDescriptor pd1 = new TextPropertyDescriptor(
 									attr.getName(), attr.getName());
 							pd1.setDescription(attr.getDescription());
 							pd1.setCategory(ITabbedPropertiesConstants.OTHER_CATEGORY);
 							result.add(new PropertyDescriptorWrapper(_element,
 									pd1));// ,
 							// getStatusLineManager()));
 						}
 
 					}
 				}
 			}
 			
 			if (result.size() == 0) {// for JSP and HTML cases
 														// this is still
 														// necessary
 				final IPropertyDescriptor[] descs = _innerSource.getPropertyDescriptors();
 				for (int i = 0; i < descs.length; i++) {
 					final IPropertyDescriptor pd = getAttrPropertyDescriptor((String) descs[i]
 							.getId());
 					if (pd != null)
 						result.add(new PropertyDescriptorWrapper(_element, pd));// ,
 					// getStatusLineManager()));
 					else {
 						if (descs[i] instanceof PropertyDescriptor)
 							((PropertyDescriptor) descs[i])
 									.setCategory(ITabbedPropertiesConstants.OTHER_CATEGORY);
 						result.add(new PropertyDescriptorWrapper(_element,
 								descs[i]));// ,
 						// getStatusLineManager()));
 					}
 
 				}
 			}
 
 			_descriptors = new IPropertyDescriptor[result.size()];
 			result.toArray(_descriptors);
 
 		}
 		return _descriptors;
 	}
 
 //	private IAttributeDescriptor findReferencedAttribute(
 //			IElementDescriptor elementDescriptor, IPropertyDescriptor desc) {
 //		return null;
 //	}
 
 //	private IElementDescriptor getElementDescriptor() {
 //		ICMRegistry registry = CMRegistry.getInstance();
 //		String uri = CMUtil.getElementNamespaceURI(_element);
 //		return registry.getElementDescriptor(uri, _element.getLocalName());
 //	}
 
 	private IPropertyDescriptor getAttrPropertyDescriptor(String attrName){
		Entity attrEntity = _query.findTagAttributeEntity(_tagEntity, attrName);
		List ppds = MetaDataEnabledProcessingFactory.getInstance().getAttributeValueRuntimeTypeFeatureProcessors(IPropertyPageDescriptor.class, _context, attrEntity);
		if (ppds.size() > 0)
			return (IPropertyDescriptor)((IPropertyPageDescriptor)ppds.get(0)).getAdapter(IPropertyDescriptor.class);
		
 		return null;
 			
 	}
 	
 //	private IStatusLineManager getStatusLineManager() {
 //		_page.getSite().getActionBars().getStatusLineManager();
 //	}
 }
