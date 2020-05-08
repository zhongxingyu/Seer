 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.aa.business.xacml.finder;
 
 import java.io.ByteArrayInputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import com.sun.xacml.EvaluationCtx;
 import com.sun.xacml.attr.StringAttribute;
 import com.sun.xacml.cond.EvaluationResult;
 
 import de.escidoc.core.aa.business.authorisation.FinderModuleHelper;
 import de.escidoc.core.aa.business.stax.handler.ComponentStaxHandler;
 import de.escidoc.core.aa.business.stax.handler.ContainerStaxHandler;
 import de.escidoc.core.aa.business.stax.handler.ItemStaxHandler;
 import de.escidoc.core.common.business.aa.authorisation.AttributeIds;
 import de.escidoc.core.common.exceptions.EscidocException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.stax.StaxParser;
 import de.escidoc.core.om.service.interfaces.ContainerHandlerInterface;
 import de.escidoc.core.om.service.interfaces.ItemHandlerInterface;
 
 /**
  * Class to retrieve an attribute values from eSciDoc resources.<br> This finder module is a fall back if an attribute
  * is referenced that is not retrievable from the triple store used within eSciDoc.<br> Currently, the object ref of an
  * grant is supported, only.<br> This should be (one of) the latest finder modules in the chain as this module should
  * only be asked if no other, faster way to get an attribute value is possible. It will fetch resources and parse the
  * xml data to extract the attribute values.
  * <p/>
  * Supported Attributes:<br> -info:escidoc/names:aa:1.0:resource:item:component<br> the ids of the components of the
  * item, multi value attribute -info:escidoc/names:aa:1.0:resource:item:component:valid-status<br> the valid-status of
  * the component of the item, single value attribute -info:escidoc/names:aa:1.0:resource:item:component:visibility<br>
  * the visibility of the component of the item, single value attribute -info:escidoc/names:aa:1.0:resource:item:component:content-category<br>
  * the content-category of the component of the item, single value attribute -info:escidoc/names:aa:1.0:resource:item:component:created-by<br>
  * the id of the user who created the component of the item, single value attribute
  * -info:escidoc/names:aa:1.0:resource:item:lock-owner<br> the id of the user who locked the item, single value
  * attribute -info:escidoc/names:aa:1.0:resource:item:modified-by<br> the id of the user who modified the item, single
  * value attribute -info:escidoc/names:aa:1.0:resource:item:public-status<br> the public-status of the item, single
  * value attribute -info:escidoc/names:aa:1.0:resource:item:version-status<br> the version-status of the item, single
  * value attribute -info:escidoc/names:aa:1.0:resource:container:member<br> the id of the member of the container, multi
  * value attribute -info:escidoc/names:aa:1.0:resource:container:lock-owner<br> the id of the user who locked the
  * container, single value attribute -info:escidoc/names:aa:1.0:resource:container:version-modified-by<br> the id of the
  * user who modified the version of the container, single value attribute -info:escidoc/names:aa:1.0:resource:container:public-status<br>
  * the public-status of the container, single value attribute -info:escidoc/names:aa:1.0:resource:container:version-status<br>
  * the version-status of the container, single value attribute
  *
  * @author Torsten Tetteroo
  */
 @Service("eSciDoc.core.aa.ResourceAttributeFinderModule")
 public class ResourceAttributeFinderModule extends AbstractAttributeFinderModule {
 
     /**
      * Pattern used to check if the attribute id is the id of a component attribute. This pattern extracts the component
      * specific part of the attribute, too.
      */
     private static final Pattern PATTERN_PARSE_COMPONENT_ATTRIBUTE_ID =
         Pattern.compile('(' + AttributeIds.ITEM_COMPONENT_ATTR_PREFIX + "[^:]+).*");
 
     /**
      * Pattern used to parse the attribute id and extract local part (that can be resolved), the "object-type" of the
      * local part, and the tailing part.
      */
     private static final Pattern PATTERN_PARSE_ATTRIBUTE_ID =
         Pattern.compile('(' + AttributeIds.RESOURCE_ATTR_PREFIX + "[^:]+:([^:]+)):{0,1}(.+){0,1}");
 
     @Autowired
     @Qualifier("service.ItemHandler")
     private ItemHandlerInterface itemHandler;
 
     @Autowired
     @Qualifier("service.ContainerHandler")
     private ContainerHandlerInterface containerHandler;
 
     /**
      * See Interface for functional description.
      *
      * @see de.escidoc.core.aa.business.xacml.finder.AbstractAttributeFinderModule#assertAttribute(java.lang.String,
      *      com.sun.xacml.EvaluationCtx, java.lang.String, java.lang.String, java.lang.String, int)
      */
     @Override
     protected boolean assertAttribute(
         final String attributeIdValue, final EvaluationCtx ctx, final String resourceId, final String resourceObjid,
         final String resourceVersionNumber, final int designatorType) throws EscidocException {
 
         return !(!super.assertAttribute(attributeIdValue, ctx, resourceId, resourceObjid, resourceVersionNumber,
             designatorType) || FinderModuleHelper.isNewResourceId(resourceId));
 
     }
 
     /**
      * See Interface for functional description.
      *
      * @see de.escidoc.core.aa.business.xacml.finder.AbstractAttributeFinderModule#resolveLocalPart(java.lang.String,
      *      com.sun.xacml.EvaluationCtx, java.lang.String, java.lang.String, java.lang.String)
      */
     @Override
     protected Object[] resolveLocalPart(
         final String attributeIdValue, final EvaluationCtx ctx, final String resourceId, final String resourceObjid,
         final String resourceVersionNumber) throws OptimisticLockingException, MissingAttributeValueException,
         SystemException, ComponentNotFoundException, ResourceNotFoundException, UniqueConstraintViolationException,
         ItemNotFoundException, InvalidXmlException, ContainerNotFoundException, WebserverSystemException {
 
         final EvaluationResult result;
         final String resolvedAttributeIdValue;
 
         final Matcher matcherComponent = PATTERN_PARSE_COMPONENT_ATTRIBUTE_ID.matcher(attributeIdValue);
         if (matcherComponent.find()) {
             resolvedAttributeIdValue = matcherComponent.group(1);
             result = fetchComponentAttribute(resolvedAttributeIdValue, ctx, resourceId);
         }
         else {
             final Matcher matcher = PATTERN_PARSE_ATTRIBUTE_ID.matcher(attributeIdValue);
             if (matcher.find()) {
                 resolvedAttributeIdValue = matcher.group(1);
                 result = fetchItemOrContainerAttribute(attributeIdValue, ctx, resourceId, resolvedAttributeIdValue);
             }
             else {
                 return null;
             }
         }
 
         if (result == null) {
             return null;
         }
 
         return new Object[] { result, resolvedAttributeIdValue };
 
     }
 
     /**
      * Fetches the attribute of a container or item. The component is identified by the provided item resource id and
      * the component-id attribute that must be specified.
      *
      * @param attributeIdValue         The attribute id for that the attribute value shall be fetched
      * @param ctx                      The evaluation context.
      * @param resourceId               The id of the container/item.
      * @param resolvedAttributeIdValue The attribute id for that the value shall be fetched.
      * @return Returns an <code>EvaluationResult</code> object containing the requested attribute.
      * @throws de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException
      * @throws de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      */
     private EvaluationResult fetchItemOrContainerAttribute(
         final String attributeIdValue, final EvaluationCtx ctx, final String resourceId,
         final String resolvedAttributeIdValue) throws MissingAttributeValueException, OptimisticLockingException,
         SystemException, UniqueConstraintViolationException, ItemNotFoundException, InvalidXmlException,
         ContainerNotFoundException, WebserverSystemException {
 
         // A previous parse process could have stored the found
         // attributes in the cache. Here, we try to get it from the cache.
         EvaluationResult result =
             (EvaluationResult) getFromCache(resourceId, null, null, resolvedAttributeIdValue, ctx);
         if (result == null) {
             if (attributeIdValue.startsWith(AttributeIds.ITEM_ATTR_PREFIX)) {
                 final String itemXml = retrieveItem(ctx, resourceId);
                 final StaxParser sp = new StaxParser(XmlUtility.NAME_ITEM);
                 ItemStaxHandler itemStaxHandler = new ItemStaxHandler(ctx, resourceId);
                 sp.addHandler(itemStaxHandler);
                 try {
                     sp.parse(new ByteArrayInputStream(itemXml.getBytes(XmlUtility.CHARACTER_ENCODING)));
                 }
                 catch (final MissingAttributeValueException e) {
                     throw e;
                 }
                 catch (final InvalidXmlException e) {
                     throw e;
                 }
                 catch (final OptimisticLockingException e) {
                     throw e;
                 }
                 catch (final UniqueConstraintViolationException e) {
                     throw e;
                 }
                 catch (final SystemException e) {
                     throw e;
                 }
                 catch (final Exception e) {
                     throw new WebserverSystemException(StringUtility.format("Error during parsing item XML", e
                         .getMessage()), e);
                 }
                 handleCache(itemStaxHandler);
             }
             else if (attributeIdValue.startsWith(AttributeIds.CONTAINER_ATTR_PREFIX)) {
                 final String containerXml = retrieveContainer(ctx, resourceId);
                 final StaxParser sp = new StaxParser(XmlUtility.NAME_CONTAINER);
                 ContainerStaxHandler containerStaxHandler = new ContainerStaxHandler(ctx, resourceId);
                 sp.addHandler(containerStaxHandler);
                 try {
                     sp.parse(new ByteArrayInputStream(containerXml.getBytes(XmlUtility.CHARACTER_ENCODING)));
                 }
                 catch (final MissingAttributeValueException e) {
                     throw e;
                 }
                 catch (final InvalidXmlException e) {
                     throw e;
                 }
                 catch (final OptimisticLockingException e) {
                     throw e;
                 }
                 catch (final UniqueConstraintViolationException e) {
                     throw e;
                 }
                 catch (final SystemException e) {
                     throw e;
                 }
                 catch (final Exception e) {
                     throw new WebserverSystemException(StringUtility.format("Error during parsing container XML", e
                         .getMessage()), e);
                 }
                 handleCache(containerStaxHandler);
             }
 
             // The parse process in one of the steps above stores the found
             // attributes in the cache. Here, we try to get it from the
             // cache, again.
             result = (EvaluationResult) getFromCache(resourceId, null, null, resolvedAttributeIdValue, ctx);
         }
         return result;
     }
 
     /**
      * Fetches the attribute of a component. The component is identified by the provided item resource id and the
      * component-id attribute that must be specified.
      *
      * @param attributeIdValue The attribute id for that the attribute value shall be fetched
      * @param ctx              The evaluation context.
      * @param itemId           The id of the item.
      * @return Returns an <code>EvaluationResult</code> object containing the requested attribute.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException
      * @throws de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException
      */
     private EvaluationResult fetchComponentAttribute(
         final String attributeIdValue, final EvaluationCtx ctx, final String itemId) throws SystemException,
         MissingAttributeValueException, OptimisticLockingException, ComponentNotFoundException,
         ResourceNotFoundException, UniqueConstraintViolationException, ItemNotFoundException, InvalidXmlException,
         WebserverSystemException {
 
         // to resolve a component attribute, the id of the component
         // must be known
         final String componentId = fetchSingleResourceAttribute(ctx, "", AttributeIds.URN_COMPONENT_ID);
         // A previous parse process could have stored the found
         // attributes in the cache. Here, we try to get it from the
         // cache.
         EvaluationResult result = (EvaluationResult) getFromCache(componentId, null, null, attributeIdValue, ctx);
         if (result == null) {
             final String componentXml = retrieveComponent(ctx, itemId, componentId);
             final StaxParser sp = new StaxParser(XmlUtility.NAME_COMPONENT);
             ComponentStaxHandler componentStaxHandler = new ComponentStaxHandler(ctx, componentId);
             sp.addHandler(componentStaxHandler);
             try {
                 sp.parse(new ByteArrayInputStream(componentXml.getBytes(XmlUtility.CHARACTER_ENCODING)));
             }
             catch (final MissingAttributeValueException e) {
                 throw e;
             }
             catch (final InvalidXmlException e) {
                 throw e;
             }
             catch (final OptimisticLockingException e) {
                 throw e;
             }
             catch (final UniqueConstraintViolationException e) {
                 throw e;
             }
             catch (final SystemException e) {
                 throw e;
             }
             catch (final Exception e) {
                 throw new WebserverSystemException(StringUtility.format("Error during parsing component XML", e
                     .getMessage()), e);
             }
             handleCache(componentStaxHandler);
 
             // The parse process stores the found
             // attributes in the cache. Here, we try to get it from the
             // cache, again. If it is not found, an empty result is created
             // and cached.
             result = (EvaluationResult) getFromCache(componentId, null, null, attributeIdValue, ctx);
         }
         return result;
     }
 
     /**
      * Retrieve Item from the system.
      *
      * @param ctx    The evaluation context, which will be used as key for the cache.
      * @param itemId The item id.
      * @return Returns the Xml representation of the item identified by the provided id.
      * @throws WebserverSystemException Thrown in case of an internal error.
      * @throws ItemNotFoundException    Thrown if no item with provided id exists.
      */
     private String retrieveItem(final EvaluationCtx ctx, final String itemId) throws WebserverSystemException,
         ItemNotFoundException {
 
         String itemXml = (String) getFromCache(XmlUtility.NAME_ID, null, null, itemId, ctx);
         if (itemXml == null) {
             try {
                 itemXml = itemHandler.retrieve(itemId);
                 putInCache(XmlUtility.NAME_ID, null, null, itemId, ctx, itemXml);
             }
             catch (final ItemNotFoundException e) {
                 throw e;
             }
             catch (final Exception e) {
                 throw new WebserverSystemException(StringUtility.format("Exception during retrieval of the item", e
                     .getMessage()), e);
             }
         }
 
         return itemXml;
     }
 
     /**
      * Retrieve Component from the system.
      *
      * @param ctx         The evaluation context, which will be used as key for the cache.
      * @param itemId      The item id.
      * @param componentId The component id.
      * @return Returns the xml representation of the component.
      * @throws WebserverSystemException   Thrown in case of an internal error.
      * @throws ItemNotFoundException      Thrown if no item with provided id exists.
      * @throws ComponentNotFoundException Thrown if no component with provided id exists.
      */
     private String retrieveComponent(final EvaluationCtx ctx, final String itemId, final String componentId)
         throws WebserverSystemException, ItemNotFoundException, ComponentNotFoundException {
 
         String componentXml = (String) getFromCache(XmlUtility.NAME_ID, null, null, componentId, ctx);
         if (componentXml == null) {
             try {
                 componentXml = itemHandler.retrieveComponent(itemId, componentId);
             }
             catch (final ItemNotFoundException e) {
                 throw e;
             }
             catch (final ComponentNotFoundException e) {
                 throw e;
             }
             catch (final Exception e) {
                 throw new WebserverSystemException(StringUtility.format("Exception during retrieval of the item", e
                     .getMessage()), e);
             }
         }
 
         return componentXml;
     }
 
     /**
      * Retrieve Container from the system.
      *
      * @param ctx         The evaluation context, which will be used as key for the cache.
      * @param containerId The container id.
      * @return Returns the Xml representation of the container identified by the provided id.
      * @throws WebserverSystemException   Thrown in case of an internal error.
      * @throws ContainerNotFoundException Thrown if no item with provided id exists.
      */
     private String retrieveContainer(final EvaluationCtx ctx, final String containerId)
         throws WebserverSystemException, ContainerNotFoundException {
 
         String containerXml = (String) getFromCache(XmlUtility.NAME_ID, null, null, containerId, ctx);
         if (containerXml == null) {
             try {
                 containerXml = containerHandler.retrieve(containerId);
                 putInCache(XmlUtility.NAME_ID, null, null, containerId, ctx, containerXml);
             }
             catch (final ContainerNotFoundException e) {
                 throw e;
             }
             catch (final Exception e) {
                 throw new WebserverSystemException(StringUtility.format("Exception during retrieval of the container",
                     e.getMessage()), e);
             }
         }
 
         return containerXml;
     }
 
     /**
      * Put values from ComponentStaxHandler in Cache.
      *
      * @param staxHandler ComponentStaxHandler.
      */
     private void handleCache(final ComponentStaxHandler staxHandler) {
         HashMap<String, String> attributes = staxHandler.getAttributes();
         for (final Entry<String, String> entry : attributes.entrySet()) {
            putInCache(staxHandler.getComponentId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
     }
 
     /**
      * Put values from ItemStaxHandler in Cache.
      *
      * @param staxHandler ItemStaxHandler.
      */
     private void handleCache(final ItemStaxHandler staxHandler) {
         HashMap<String, String> stringAttributes = staxHandler.getStringAttributes();
         for (final Entry<String, String> entry : stringAttributes.entrySet()) {
            putInCache(staxHandler.getResourceId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
         HashMap<String, String> superAttributes = staxHandler.getSuperAttributes();
         for (final Entry<String, String> entry : superAttributes.entrySet()) {
            putInCache(staxHandler.getResourceId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
         HashMap<String, Collection<StringAttribute>> attributeAttributes = staxHandler.getAttributeAttributes();
         for (final Entry<String, Collection<StringAttribute>> entry : attributeAttributes.entrySet()) {
            putInCache(staxHandler.getResourceId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
     }
 
     /**
      * Put values from ContainerStaxHandler in Cache.
      *
      * @param staxHandler ContainerStaxHandler.
      */
     private void handleCache(final ContainerStaxHandler staxHandler) {
         HashMap<String, String> stringAttributes = staxHandler.getStringAttributes();
         for (final Entry<String, String> entry : stringAttributes.entrySet()) {
            putInCache(staxHandler.getResourceId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
         HashMap<String, String> superAttributes = staxHandler.getSuperAttributes();
         for (final Entry<String, String> entry : superAttributes.entrySet()) {
            putInCache(staxHandler.getResourceId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
         HashMap<String, Collection<StringAttribute>> attributeAttributes = staxHandler.getAttributeAttributes();
         for (final Entry<String, Collection<StringAttribute>> entry : attributeAttributes.entrySet()) {
            putInCache(staxHandler.getResourceId(), null, null, entry.getKey(), staxHandler.getCtx(), entry.getValue());
         }
     }
 
     /**
      * Injects the container handler if "called" via Spring.
      *
      * @param containerHandler The container handler.
      */
     public void setContainerHandler(final ContainerHandlerInterface containerHandler) {
 
         this.containerHandler = containerHandler;
     }
 
     /**
      * Injects the item handler if "called" via Spring.
      *
      * @param itemHandler The item handler.
      */
     public void setItemHandler(final ItemHandlerInterface itemHandler) {
 
         this.itemHandler = itemHandler;
     }
 
 }
