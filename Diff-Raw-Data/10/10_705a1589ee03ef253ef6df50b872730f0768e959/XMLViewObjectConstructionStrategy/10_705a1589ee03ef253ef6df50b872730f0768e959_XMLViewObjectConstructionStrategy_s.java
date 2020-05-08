 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.designtime.internal.view;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jst.jsf.common.internal.types.TypeConstants;
 import org.eclipse.jst.jsf.common.runtime.internal.model.ViewObject;
 import org.eclipse.jst.jsf.common.runtime.internal.model.behavioural.ActionSourceInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.behavioural.ActionSourceInfo2;
 import org.eclipse.jst.jsf.common.runtime.internal.model.behavioural.EditableValueHolderInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.behavioural.INamingContainerInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.behavioural.ValueHolderInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentFactory;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ConverterDecorator;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ConverterTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ValidatorDecorator;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ValidatorTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IComponentPropertyHandler;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IComponentTagElement;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IConverterTagElement;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.ITagAttributeHandler;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.ITagElement;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IValidatorTagElement;
 import org.eclipse.jst.jsf.common.util.JDTBeanProperty;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewObjectMappingService.ElementData;
 import org.eclipse.jst.jsf.designtime.internal.view.mapping.ICustomViewMapper;
 import org.eclipse.jst.jsf.designtime.internal.view.mapping.ICustomViewMapper.PropertyMapping;
 import org.eclipse.jst.jsf.designtime.internal.view.mapping.viewmapping.CustomViewMapperExtensionLoader;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 
 /**
  * A strategy for constructing view objects.
  * 
  * @author cbateman
  * 
  */
 public class XMLViewObjectConstructionStrategy extends
         ViewObjectConstructionStrategy<Element>
 {
     private static final String               GENERATED_ID = "_generatedId"; //$NON-NLS-1$
     private final ComponentConstructionData   _constructionData;
     private final XMLViewDefnAdapter          _adapter;
     private final XMLViewObjectMappingService _mappingService;
 
     /**
      * @param adapter
      *            MUST NOT BE NULL
      * @param constructionData
      *            MUST NOT BE NULL
      * @param mappingService
      *            MAY BE NULL
      */
     public XMLViewObjectConstructionStrategy(final XMLViewDefnAdapter adapter,
             final ComponentConstructionData constructionData,
             final XMLViewObjectMappingService mappingService)
     {
         super();
         if (adapter == null || constructionData == null)
         {
             throw new IllegalArgumentException(
                     "adapter and constructionData must not be null"); //$NON-NLS-1$
         }
         _constructionData = constructionData;
         _adapter = adapter;
         _mappingService = mappingService;
     }
 
     @Override
     public ViewObject createViewObject(final Element element,
             final ITagElement tagElement)
     {
         try
         {
             if (tagElement instanceof IComponentTagElement)
             {
                 String id = null;
 
                 // only generate ids for non-viewroot components. This will
                 // make the generated id's more faithful to runtime since the
                 // running count won't be incremented for view roots (as they
                 // won't at runtime).
                 final ComponentTypeInfo typeInfo = ((IComponentTagElement) tagElement)
                         .getComponent();
 
                 if (!"javax.faces.ViewRoot".equals(typeInfo.getComponentType())) //$NON-NLS-1$
                 {
                     id = calculateId(element, _constructionData);
                 }
                 return findBestComponent(tagElement.getUri(), element, id,
                         (IComponentTagElement) tagElement);
             }
             else if (tagElement instanceof IConverterTagElement)
             {
                 final ConverterTypeInfo typeInfo = ((IConverterTagElement) tagElement)
                         .getConverter();
                 // TODO: validate when no parent
                 ComponentInfo parent = _constructionData.getParent();
                 parent = findFirstParent(
                         ComponentFactory.INTERFACE_VALUEHOLDER, parent);
                 if (parent != null)
                 {
                     parent.addDecorator(
                             new ConverterDecorator(parent, typeInfo),
                             ComponentFactory.CONVERTER);
                 }
                 // TODO: else validate problem
             }
             else if (tagElement instanceof IValidatorTagElement)
             {
                 final ValidatorTypeInfo typeInfo = ((IValidatorTagElement) tagElement)
                         .getValidator();
                 ComponentInfo parent = _constructionData.getParent();
                 parent = findFirstParent(
                         ComponentFactory.INTERFACE_EDITABLEVALUEHOLDER, parent);
                 if (parent != null)
                 {
                     parent.addDecorator(
                             new ValidatorDecorator(parent, typeInfo),
                             ComponentFactory.VALIDATOR);
                 }
                 // TODO :else validate problem
             }
         }
         catch (final Exception e)
         {
             // log and ignore if an individual construction fails
             JSFCorePlugin.log(e, "Error constructing view object"); //$NON-NLS-1$
         }
         return null;
     }
 
     private ComponentInfo findFirstParent(final String matchingType,
             final ComponentInfo start)
     {
         ComponentInfo parent = start;
 
         while (parent != null && parent.getComponentTypeInfo() != null
                 && !parent.getComponentTypeInfo().isInstanceOf(matchingType))
         {
             parent = parent.getParent();
         }
         return parent;
     }
 
     private ComponentInfo findBestComponent(final String uri,
             final Element srcElement, final String id,
             final IComponentTagElement tagElement)
     {
         ComponentInfo bestComponent = null;
 
         final ComponentInfo parent = _constructionData.getParent();
 
         final Map<String, Object> initMap = new HashMap<String, Object>();
         final Map<String, String> attributeToPropertyMap = new HashMap<String, String>();
         populateInitMap(uri, initMap, srcElement, tagElement,
                 attributeToPropertyMap);
 
         if (initMap.get("id") == null) //$NON-NLS-1$
         {
             // id must be set
             initMap.put("id", id); //$NON-NLS-1$
         }
 
         final ComponentTypeInfo typeInfo = tagElement.getComponent();
 
         // if we have a well-established base type, try that first
         // sub-classes must occur before superclasses to ensure most accurate
         // detection.
         if (typeInfo.isInstanceOf(ComponentFactory.BASE_CLASS_UIINPUT))
         {
             bestComponent = ComponentFactory.createUIInputInfo(parent,
                     typeInfo, initMap);
         }
         else if (typeInfo.isInstanceOf(ComponentFactory.BASE_CLASS_UIOUTPUT))
         {
             bestComponent = ComponentFactory.createUIOutputInfo(parent,
                     typeInfo, initMap);
         }
         else if (typeInfo.isInstanceOf(ComponentFactory.BASE_CLASS_UICOMMAND))
         {
             bestComponent = ComponentFactory.createUICommandInfo(parent,
                     typeInfo, initMap);
         }
         else if (typeInfo.isInstanceOf(ComponentFactory.BASE_CLASS_UIDATA))
         {
             bestComponent = ComponentFactory.createUIDataInfo(parent, typeInfo,
                     initMap);
         }
         else if (typeInfo.isInstanceOf(ComponentFactory.BASE_CLASS_UIFORM))
         {
             // TODO: how handle prepend ids?
             bestComponent = ComponentFactory.createUIFormInfo(parent, typeInfo,
                     initMap);
         }
         else
         {
             // default
             bestComponent = ComponentFactory.createComponentInfo(
                     _constructionData.getParent(), typeInfo, initMap);
         }
 
         addTypeAdapters(bestComponent);
         maybeMapXMLToViewObjects(bestComponent, srcElement,
                 attributeToPropertyMap, _constructionData.getDocument());
         maybeUpdateViewObject(bestComponent, srcElement, tagElement);
         return bestComponent;
     }
 
     // TODO: move to view definition adapter?
     private void populateInitMap(final String uri,
             final Map<String, Object> initMap, final Element srcElement,
             final IComponentTagElement tagElement,
             final Map<String, String> attributeToPropertyMap)
     {
         final ComponentTypeInfo typeInfo = tagElement.getComponent();
        final Map<String, JDTBeanProperty> properties = DTComponentIntrospector
                .getBeanProperties(typeInfo, _constructionData.getProject());
         final Map<String, ITagAttributeHandler> attributeHandlers = tagElement
                 .getAttributeHandlers();
 
         final NamedNodeMap nodeMap = srcElement.getAttributes();
 
         if (nodeMap != null && attributeHandlers != null)
         {
             for (int i = 0; i < nodeMap.getLength(); i++)
             {
                 final Attr attr = (Attr) nodeMap.item(i);
                 if (attr != null)
                 {
                     final String name = attr.getLocalName();
 
                     if (name != null)
                     {
                         final ITagAttributeHandler attrHandler = attributeHandlers
                                 .get(name);
                         if (attrHandler instanceof IComponentPropertyHandler)
                         {
                             mapComponentProperty(uri, srcElement, properties,
                                     (IComponentPropertyHandler) attrHandler,
                                     attr, name, initMap, attributeHandlers,
                                     attributeToPropertyMap);
                         }
                     }
                 }
             }
         }
     }
 
     private void mapComponentProperty(final String uri,
             final Element srcElement,
             final Map<String, JDTBeanProperty> properties,
             final IComponentPropertyHandler attrHandler, final Attr attr,
             final String attributeName, final Map initMap,
             final Map<String, ITagAttributeHandler> attributeHandlers,
             final Map<String, String> attributeToPropertyMap)
     {
         final String propertyName = attrHandler.getPropertyName();
         if (properties.containsKey(propertyName))
         {
             final String id = attrHandler.getCustomHandler();
 
             ICustomViewMapper mapper = null;
 
             if (id != null)
             {
                 mapper = CustomViewMapperExtensionLoader
                         .getCustomViewMapper(id);
                 if (mapper != null)
                 {
                     final PropertyMapping mapping = mapper
                             .mapToComponentProperty(uri, srcElement, attr);
                     if (mapping != null)
                     {
                         initMap.put(mapping.getName(), mapping.getProperty());
                         attributeToPropertyMap.put(attributeName, mapping
                                 .getName());
                         return;
                     }
                 }
             }
 
             final String value = attr.getValue();
             if (value != null)
             {
                 final Object convertedValue = convertFromString(value,
                         properties.get(propertyName));
                 initMap.put(propertyName, convertedValue);
             }
             attributeToPropertyMap.put(attributeName, propertyName);
         }
     }
 
     private void maybeMapXMLToViewObjects(final ViewObject mappedObject,
             final Element node,
             final Map<String, String> attributeToProperties,
             final IDocument document)
     {
         if (mappedObject != null && _mappingService != null)
         {
             final String uri = _adapter.getNamespace(node, document);
             final IStructuredDocumentContext context = IStructuredDocumentContextFactory.INSTANCE
                     .getContext(document, node);
             final ElementData elementData = XMLViewObjectMappingService
                     .createElementData(uri, node.getLocalName(), context,
                             attributeToProperties);
 
             _mappingService.createMapping(elementData, mappedObject);
         }
     }
 
     private void maybeUpdateViewObject(final ComponentInfo bestComponent,
             final Element srcElement, final ITagElement tagElement)
     {
         if (srcElement.getAttributes() == null)
             return;
 
         for (int i = 0; i < srcElement.getAttributes().getLength(); i++)
         {
             final Attr attr = (Attr) srcElement.getAttributes().item(i);
             final Map<String, ITagAttributeHandler> attributeHandlers = tagElement
                     .getAttributeHandlers();
 
             if (attributeHandlers != null)
             {
                 final ITagAttributeHandler handler = attributeHandlers.get(attr
                         .getLocalName());
 
                 if (handler != null)
                 {
                     final String id = handler.getCustomHandler();
 
                     ICustomViewMapper mapper = null;
 
                     if (id != null)
                     {
                         mapper = CustomViewMapperExtensionLoader
                                 .getCustomViewMapper(id);
                         if (mapper != null)
                         {
                             mapper.doAttributeActions(bestComponent,
                                     srcElement, attr);
                         }
                     }
                 }
             }
         }
     }
 
     private Object convertFromString(final String convertValue,
             final JDTBeanProperty ofThisType)
     {
         final String signature = ofThisType.getTypeSignature();
 
         Object result = null;
         switch (Signature.getTypeSignatureKind(signature))
         {
             case Signature.BASE_TYPE_SIGNATURE:
                 result = convertFromBaseType(convertValue, signature);
             break;
 
             case Signature.CLASS_TYPE_SIGNATURE:
                 if (TypeConstants.TYPE_STRING.equals(signature))
                 {
                     result = convertValue;
                 }
                 else if (TypeConstants.TYPE_JAVAOBJECT.equals(signature))
                 {
                     result = convertValue;
                 }
             break;
         }
 
         return result;
     }
 
     // TODO: does this belong somewhere else?
     private Object convertFromBaseType(final String convertValue,
             final String signature)
     {
         if (Signature.SIG_BOOLEAN.equals(signature))
         {
             return Boolean.valueOf(convertValue);
         }
         else if (Signature.SIG_INT.equals(signature)
                 || Signature.SIG_BYTE.equals(signature)
                 || Signature.SIG_SHORT.equals(signature))
         {
             try
             {
                 return Integer.valueOf(convertValue);
             }
             catch (final NumberFormatException nfe)
             {
                 return null;
             }
         }
         else if (Signature.SIG_LONG.equals(convertValue))
         {
             try
             {
                 return Long.valueOf(convertValue);
             }
             catch (final NumberFormatException nfe)
             {
                 return null;
             }
         }
 
         return null;
     }
 
     private void addTypeAdapters(final ComponentInfo component)
     {
         final String[] interfaceNames = component.getComponentTypeInfo()
                 .getInterfaces();
         final Set interfaceNameSets = new HashSet();
 
         for (final String interfaceName : interfaceNames)
         {
             interfaceNameSets.add(interfaceName);
         }
 
         // don't replace intrinsic adapters
         if (interfaceNameSets.contains(ComponentFactory.INTERFACE_ACTIONSOURCE))
         {
             // an ActionSource2 is-a ActionSource
             if (interfaceNameSets
                     .contains(ComponentFactory.INTERFACE_ACTIONSOURCE2)
                     && component.getAdapter(ComponentFactory.ACTION_SOURCE2) == null)
             {
                 component.addAdapter(ComponentFactory.ACTION_SOURCE2,
                         new ActionSourceInfo2(null, null, false, null));
             }
 
             if (component.getAdapter(ComponentFactory.ACTION_SOURCE) == null)
             {
                 component.addAdapter(ComponentFactory.ACTION_SOURCE,
                         new ActionSourceInfo(null, null, false));
             }
         }
 
         if (interfaceNameSets.contains(ComponentFactory.INTERFACE_VALUEHOLDER))
         {
             // a EditableValueHolder is-a ValueHolder
             if (interfaceNameSets
                     .contains(ComponentFactory.INTERFACE_EDITABLEVALUEHOLDER)
                     && component
                             .getAdapter(ComponentFactory.EDITABLE_VALUE_HOLDER) == null)
             {
                 component.addAdapter(ComponentFactory.EDITABLE_VALUE_HOLDER,
                         new EditableValueHolderInfo(null, null, null, false,
                                 false, true, false, null, null, null));
             }
 
             if (component.getAdapter(ComponentFactory.VALUE_HOLDER) == null)
             {
                 component.addAdapter(ComponentFactory.VALUE_HOLDER,
                         new ValueHolderInfo(null, null, null));
             }
         }
 
         if (interfaceNameSets
                 .contains(ComponentFactory.INTERFACE_NAMINGCONTAINER)
                 && component.getAdapter(ComponentFactory.NAMING_CONTAINER) == null)
         {
             component.addAdapter(ComponentFactory.NAMING_CONTAINER,
                     INamingContainerInfo.ADAPTER);
         }
     }
 
     /**
      * @param element
      * @param constructionData
      * @return the id for element either derived from the element using getId or
      *         if not present, using a generation algorithm
      */
     protected String calculateId(final Element element,
             final ComponentConstructionData constructionData)
     {
         final String id = _adapter.getId(element);
         if (id != null)
         {
             return id;
         }
         // TODO: improve this
         final String prefix = _adapter.getGeneratedIdPrefix();
         return (prefix != null ? prefix : GENERATED_ID)
                 + constructionData.increment();
     }
 
     /**
      * @return the construction data for this strategy
      */
     public final ComponentConstructionData getConstructionData()
     {
         return _constructionData;
     }
 }
