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
 
 import com.sun.xacml.EvaluationCtx;
 import com.sun.xacml.attr.AttributeDesignator;
 import com.sun.xacml.attr.AttributeValue;
 import com.sun.xacml.attr.BagAttribute;
 import com.sun.xacml.attr.StringAttribute;
 import com.sun.xacml.cond.EvaluationResult;
 import com.sun.xacml.finder.AttributeFinderModule;
 import de.escidoc.core.aa.business.authorisation.Constants;
 import de.escidoc.core.aa.business.authorisation.CustomEvaluationResultBuilder;
 import de.escidoc.core.aa.business.authorisation.FinderModuleHelper;
 import de.escidoc.core.aa.business.cache.RequestAttributesCache;
 import de.escidoc.core.common.business.aa.authorisation.AttributeIds;
 import de.escidoc.core.common.exceptions.EscidocException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 import java.lang.reflect.Constructor;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Abstract class for an attribute finder module.<br>
  * Sub classes of this class must implement the method
  * <code>resolveLocalPart</code>. They may override the methods
  * <code>assertAttribute</code>, <code>fixObjectType</code>,
  * <code>getCacheKey</code>, and <code>getSupportedDesignatorTypes</code> if the
  * default implementations do not fit their requirements.
  * 
  * @author Torsten Tetteroo
  * 
  *
  */
 public abstract class AbstractAttributeFinderModule
     extends AttributeFinderModule {
 
     static final String RESOURCE_NOT_FOUND_EXCEPTION_PACKAGE_PREFIX =
         ResourceNotFoundException.class.getPackage().getName() + '.';
 
     /**
      * Pattern to check if an object type is an valid eScidoc (virtual) resource
      * and if we can find out the object-type by checking the id (only works for
      * unique fedora-generated ids).
      */
     public static final Pattern PATTERN_ID_VALIDATABLE_OBJECT_TYPE = Pattern
         .compile(XmlUtility.NAME_COMPONENT + '|' + XmlUtility.NAME_CONTAINER
             + '|' + XmlUtility.NAME_CONTENT_MODEL + '|'
             + XmlUtility.NAME_CONTEXT + '|' + XmlUtility.NAME_ITEM + '|'
             + XmlUtility.NAME_ORGANIZATIONAL_UNIT + '|' + XmlUtility.NAME_ROLE
             + '|' + XmlUtility.NAME_USER_ACCOUNT + '|'
             + XmlUtility.NAME_USER_GROUP + '|' + XmlUtility.NAME_GRANT);
 
     /**
      * Pattern used to parse the attribute id and extract local part (that can
      * be resolved), "current" object-type in the local part, and the tailing
      * part.
      */
     protected static final Pattern PATTERN_PARSE_ATTRIBUTE_ID = Pattern
         .compile('(' + AttributeIds.RESOURCE_ATTR_PREFIX
             + "([^:]+):[^:]+):{0,1}(.*){0,1}" + "|("
             + AttributeIds.RESOURCE_ATTR_PREFIX
             + "(object-type|object-type-new|[^:]+?-id))$");
 
     private static final int GROUP_INDEX_RESOURCE_OBJID = 1;
 
     private static final int GROUP_INDEX_RESOURCE_VERSION_NUMBER = 3;
 
     private final Map<String, String> convertToObjectType;
 
     /**
      * The constructor.
      */
     protected AbstractAttributeFinderModule() {
 
         this.convertToObjectType = new HashMap<String, String>();
 
         convertToObjectType.put(XmlUtility.NAME_COMPONENT,
             XmlUtility.NAME_COMPONENT);
         convertToObjectType.put(XmlUtility.NAME_COMPONENT + "-new",
             XmlUtility.NAME_COMPONENT);
         convertToObjectType.put(XmlUtility.NAME_CONTAINER,
             XmlUtility.NAME_CONTAINER);
         convertToObjectType.put(XmlUtility.NAME_CONTAINER + "-new",
             XmlUtility.NAME_CONTAINER);
         convertToObjectType.put(XmlUtility.NAME_CONTENT_MODEL,
             XmlUtility.NAME_CONTENT_MODEL);
         convertToObjectType.put(XmlUtility.NAME_CONTENT_MODEL + "-new",
             XmlUtility.NAME_CONTENT_MODEL);
         convertToObjectType.put(XmlUtility.NAME_CONTEXT,
             XmlUtility.NAME_CONTEXT);
         convertToObjectType.put(XmlUtility.NAME_CONTEXT + "-new",
             XmlUtility.NAME_CONTEXT);
         convertToObjectType.put(XmlUtility.NAME_GRANT, XmlUtility.NAME_GRANT);
         convertToObjectType.put(XmlUtility.NAME_GRANT + "-new",
             XmlUtility.NAME_GRANT);
         convertToObjectType.put(XmlUtility.NAME_ITEM, XmlUtility.NAME_ITEM);
         convertToObjectType.put(XmlUtility.NAME_ITEM + "-new",
             XmlUtility.NAME_ITEM);
         convertToObjectType.put(XmlUtility.NAME_ORGANIZATIONAL_UNIT,
             XmlUtility.NAME_ORGANIZATIONAL_UNIT);
         convertToObjectType.put(XmlUtility.NAME_ORGANIZATIONAL_UNIT + "-new",
             XmlUtility.NAME_ORGANIZATIONAL_UNIT);
         convertToObjectType.put(XmlUtility.NAME_USER_ACCOUNT,
             XmlUtility.NAME_USER_ACCOUNT);
         convertToObjectType.put(XmlUtility.NAME_USER_ACCOUNT + "-new",
             XmlUtility.NAME_USER_ACCOUNT);
         convertToObjectType.put(XmlUtility.NAME_USER_GROUP,
             XmlUtility.NAME_USER_GROUP);
         convertToObjectType.put(XmlUtility.NAME_USER_GROUP + "-new",
             XmlUtility.NAME_USER_GROUP);
         convertToObjectType.put(XmlUtility.NAME_STATISTIC_DATA,
             XmlUtility.NAME_STATISTIC_DATA);
         convertToObjectType.put(XmlUtility.NAME_STATISTIC_DATA + "-new",
             XmlUtility.NAME_STATISTIC_DATA);
         convertToObjectType.put(XmlUtility.NAME_SCOPE, XmlUtility.NAME_SCOPE);
         convertToObjectType.put(XmlUtility.NAME_SCOPE + "-new",
             XmlUtility.NAME_SCOPE);
         convertToObjectType.put(XmlUtility.NAME_AGGREGATION_DEFINITION,
             XmlUtility.NAME_AGGREGATION_DEFINITION);
         convertToObjectType.put(
             XmlUtility.NAME_AGGREGATION_DEFINITION + "-new",
             XmlUtility.NAME_AGGREGATION_DEFINITION);
         convertToObjectType.put(XmlUtility.NAME_REPORT, XmlUtility.NAME_REPORT);
         convertToObjectType.put(XmlUtility.NAME_REPORT + "-new",
             XmlUtility.NAME_REPORT);
         convertToObjectType.put(XmlUtility.NAME_REPORT_DEFINITION,
             XmlUtility.NAME_REPORT_DEFINITION);
         convertToObjectType.put(XmlUtility.NAME_REPORT_DEFINITION + "-new",
             XmlUtility.NAME_REPORT_DEFINITION);
 
         convertToObjectType.put(XmlUtility.NAME_CREATED_BY,
             XmlUtility.NAME_USER_ACCOUNT);
         convertToObjectType.put(XmlUtility.NAME_CREATED_BY + "-new",
             XmlUtility.NAME_USER_ACCOUNT);
     }
 
     /**
      * The Designator is supported. Therefore we always return true.
      * 
      * @return Always returns true.
      * @see AttributeFinderModuleInterface#isDesignatorSupported()
      *
      */
     @Override
     public boolean isDesignatorSupported() {
         // always return true, since this is a feature we always support
         return true;
     }
 
     /**
      * Gets the supported designator types.<br>
      * The default implementation returns a set only containing one value,
      * indicating that it supports Designators of type RESOURCE_TARGET.<br>
      * Sub classes may override this.
      * 
      * @return Set only containing the value RESOURCE_TARGET.
      * @see AttributeFinderModule
      *      #getSupportedDesignatorTypes()
      *
      */
     @Override
     public Set<Integer> getSupportedDesignatorTypes() {
         // return a single identifier that shows support for resource attributes
         final Set<Integer> set = new HashSet<Integer>();
         set.add(AttributeDesignator.RESOURCE_TARGET);
         return set;
     }
 
     /**
      * Asserts the finder module is responsible for the attribute that shall be
      * fetched. <br>
      * The default implementation asserts that the attribute is of designator
      * type RESOURCE_TARGET and starts with the escidoc resource attribute
      * prefix.<br>
      * Sub classes may override this.
      * 
      * @param attributeIdValue
      *            The value of the attributeId to retrieve the attribute for.
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param resourceId
      *            The id of the resource.
      * @param resourceObjid
      *            The objid part of the resource id.
      * @param resourceVersionNumber
      *            The version number part of the resource id.
      * @param designatorType
      *            The designator type.
      * @return Returns <code>true</code> if the attribute finder module is
      *         responsible for this attribute, <code>false</code> else.
      * 
      * @throws EscidocException
      *             Thrown in case of an error.
      */
     protected boolean assertAttribute(
         final String attributeIdValue, final EvaluationCtx ctx,
         final String resourceId, final String resourceObjid,
         final String resourceVersionNumber, final int designatorType)
         throws EscidocException {
 
         // make sure this is a resource attribute
         if (designatorType != AttributeDesignator.RESOURCE_TARGET) {
             return false;
         }
 
         // make sure attribute is in escidoc-internal format
         return PATTERN_PARSE_ATTRIBUTE_ID.matcher(attributeIdValue).find();
 
     }
 
     /**
      * Gets a {@link EvaluationResult} from the cache
      * identified by the provided values.
      * 
      * @param resourceId
      *            The resource Id.
      * @param resourceObjid
      *            The resource objid.
      * @param resourceVersionNumber
      *            The resource version number.
      * @param attributeIdValue
      *            The id of the attribute for that the result has been cached.
      * @param ctx
      *            The {@link EvaluationCtx} for that the result has been cached.
      * @return Returns the cached {@link EvaluationResult} or <code>null</code>.
      *
      */
     protected final EvaluationResult getFromCache(
         final String resourceId, final String resourceObjid,
         final String resourceVersionNumber, final String attributeIdValue,
         final EvaluationCtx ctx) {
 
         final String cacheKey =
             getCacheKey(resourceId, resourceObjid, resourceVersionNumber,
                 attributeIdValue);
         return (EvaluationResult) RequestAttributesCache.get(ctx, cacheKey);
     }
 
     /**
      * Puts the provided {@link EvaluationResult} into the
      * cache using the other provided keys to construct the cache key.
      * 
      * @param resourceId
      *            The resource Id.
      * @param resourceObjid
      *            The resource objid.
      * @param resourceVersionNumber
      *            The resource version number.
      * @param attributeIdValue
      *            The id of the attribute for that the result shall be cached.
      * @param ctx
      *            The {@link EvaluationCtx} for that the result shall be cached.
      * @param result
      *            The {@link EvaluationResult to cache}
      *
      */
     protected final void putInCache(
         final String resourceId, final String resourceObjid,
         final String resourceVersionNumber, final String attributeIdValue,
         final EvaluationCtx ctx, final EvaluationResult result) {
 
         final String cacheKey =
             getCacheKey(resourceId, resourceObjid, resourceVersionNumber,
                 attributeIdValue);
         RequestAttributesCache.put(ctx, cacheKey, result);
     }
 
     /**
      * Gets the cache key for the provided values. <br>
      * The default implementation concatenates the resource id and the attribute
      * id to construct the cache key.<br>
      * Subclasses may override this.
      * 
      * @param resourceId
      *            The id of the resource.
      * @param resourceObjid
      *            The objid part of the resource id.
      * @param resourceVersionNumber
      *            The version number part of the resource id.
      * @param attributeIdValue
      *            The attributeId to retrieve the attribute for.
      * @return Returns the cache key for the provided values.
      */
     protected String getCacheKey(
         final String resourceId, final String resourceObjid,
         final String resourceVersionNumber, final String attributeIdValue) {
 
         return StringUtility.concatenateWithColonToString(resourceId,
             attributeIdValue);
     }
 
     /**
      * Performs the "local" resolving, i.e. fetches the value for the part of
      * the attribute id that can be handled by this attribute finder module.
      * 
      * @param attributeIdValue
      *            THe value of the attribute id.
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param resourceId
      *            The id of the resource.
      * @param resourceObjid
      *            The objid part of the resource id.
      * @param resourceVersionNumber
      *            The version number part of the resource id.
      * 
      * @return Returns an array containing the <code>EvaluationResult</code>
      *         object holding the result for the resolved part of the attribute
      *         id, and the resolved part of the attribute id. If no value can be
      *         fetched, <code>null</code> is returned.
      * @throws EscidocException
      *             Thrown in case of a failure during resolving.
      */
     protected abstract Object[] resolveLocalPart(
         final String attributeIdValue, final EvaluationCtx ctx,
         final String resourceId, final String resourceObjid,
         final String resourceVersionNumber) throws EscidocException;
 
     /**
      * Finds the resource attribute for the provided input parameters.
      * <p/>
      * 
      * The attributeId parameter is mapped to the corresponding value in the
      * system. In order to be recognized by this method, the attributeId
      * parameter has to start with "info:escidoc/names:aa:1.0:resource:". All
      * that comes after this start-sequence defines the path to the system
      * object that contains the value. For example, if attributeId has value
      * "info:escidoc/names:aa:1.0:resource:item:context", the item is identified
      * with the ID provided in the evaluation context and the id of its context
      * is returned.
      * 
      * @param attributeType
      *            The attributeType to retrieve the attribute for.
      * @param attributeId
      *            The attributeId to retrieve the attribute for.
      * @param issuer
      *            The issuer to retrieve the attribute for. Normally null.
      * @param subjectCategory
      *            The subjectCategory to retrieve the attribute for.
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param designatorType
      *            The designatorType. Has to be RESOURCE_TARGET in this case.
      * @return EvaluationResult An EvaluationResult containing the requested
      *         attribute.
      * @see AttributeFinderModule#findAttribute(URI,
      *      URI, URI, URI,
      *      EvaluationCtx, int)
      *
      */
     @Override
     public final EvaluationResult findAttribute(
         final URI attributeType, final URI attributeId, final URI issuer,
         final URI subjectCategory, final EvaluationCtx ctx,
         final int designatorType) {
 
         // make sure they're asking for a string return value
         if (!attributeType.toString().equals(StringAttribute.identifier)) {
             return CustomEvaluationResultBuilder
                 .createEmptyEvaluationResult(attributeType);
         }
 
         // get the resource id and split resource id into objid and version
         // number
         final String resourceId =
             ((StringAttribute) ctx.getResourceId()).getValue();
         String resourceObjid = resourceId;
         String resourceVersionNumber = null;
         if (!FinderModuleHelper.isNewResourceId(resourceId)) {
             final Matcher matcher =
                 Constants.PATTERN_PARSE_RESOURCE_ID.matcher(resourceId);
             if (matcher.find()) {
                 // if a match is found, resourceObjid and resourceVersionNumber
                 // are fetched from the matcher
                 resourceObjid = matcher.group(GROUP_INDEX_RESOURCE_OBJID);
                 resourceVersionNumber =
                     matcher.group(GROUP_INDEX_RESOURCE_VERSION_NUMBER);
                 // Otherwise, if the parsing failed, this can be due to
                 // another kind of identifier like user-account/name or
                 // role/name. Therefore, resourceObjid and resourceVersion
                 // are kept initialized (as these kind of identifiers are
                 // identifiers for objects that are not under version control).
             }
         }
 
         final String attributeIdValue = attributeId.toString();
         EvaluationResult result;
         String resolvedAttributeId = null;
         try {
             // make sure they're asking for an attribute for that this finder
             // module
             // is responsible for.
             if (!assertAttribute(attributeIdValue, ctx, resourceId,
                 resourceObjid, resourceVersionNumber, designatorType)) {
                 return CustomEvaluationResultBuilder
                     .createEmptyEvaluationResult(attributeType);
             }
 
             // try to get it from the cache
             result =
                 getFromCache(resourceId, resourceObjid, resourceVersionNumber,
                     attributeIdValue, ctx);
             if (result != null) {
                 return result;
             }
 
             // perform local resolving
             final Object[] resultArray =
                 resolveLocalPart(attributeIdValue, ctx, resourceId,
                     resourceObjid, resourceVersionNumber);
             if (resultArray != null && resultArray.length == 2) {
                 result = (EvaluationResult) resultArray[0];
                 resolvedAttributeId = (String) resultArray[1];
             }
             else if (resultArray != null) {
                 CustomEvaluationResultBuilder
                     .createProcessingErrorResult(new WebserverSystemException(
                         StringUtility.format(
                             "Unexpected result from resolveLocalPart",
                             resultArray)));
             }
         }
         catch (final ResourceNotFoundException e) {
             if (ResourceNotFoundException.class.equals(e.getClass())) {
                 final ResourceNotFoundException e1 =
                     getResourceNotFoundException(attributeIdValue, resourceId,
                         e.getMessage(), e.getCause());
                 e1.setStackTrace(e.getStackTrace());
                 return CustomEvaluationResultBuilder
                     .createResourceNotFoundResult(e1);
             }
             else {
                 return CustomEvaluationResultBuilder
                     .createResourceNotFoundResult(e);
             }
         }
         catch (final Exception e) {
             return CustomEvaluationResultBuilder.createProcessingErrorResult(e);
         }
 
         if (result != null) {
             result =
                 recursivelyCallCtxGetResourceAttribute(attributeIdValue,
                     resolvedAttributeId, ctx, result);
 
             putInCache(resourceId, resourceObjid, resourceVersionNumber,
                 attributeIdValue, ctx, result);
         }
         else {
             result =
                 CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
         return result;
     }
 
     /**
      * Recursively calls the <code>getResourceAttribute</code> method of the
      * provided <code>EvaluationCtx</code> object.<br>
      * The current resource id of the <code>EvaluationCtx</code> is stored. Then
      * the provided newResourceId is set as the resource id and
      * <code>EvaluationCtx.getResourceAttribute</code> is called. Finally, the
      * stored resource id is reset as the <code>EvaluationCtx</code>'s resource
      * id.
      * 
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param newResourceId
      *            The id of the resource for that the attribute shall be found.
      * @param newAttributeId
      *            The attributeId to retrieve the attribute for.
      * 
      * 
      * @return Returns the result of <code>ctx.getResourceAttribute</code>.
      *
      */
     protected EvaluationResult recursivelyCallCtxGetResourceAttribute(
         final EvaluationCtx ctx, final String newResourceId,
         final String newAttributeId) {
 
         final AttributeValue storedResourceId = ctx.getResourceId();
         ctx.setResourceId(new StringAttribute(newResourceId));
         EvaluationResult result = null;
         try {
             result =
                 ctx.getResourceAttribute(Constants.URI_XMLSCHEMA_STRING,
                     new URI(newAttributeId), null);
         }
         catch (final URISyntaxException e) {
             return CustomEvaluationResultBuilder.createSyntaxErrorResult(e);
         }
         catch (final Exception e) {
             return CustomEvaluationResultBuilder.createProcessingErrorResult(e);
         }
         finally {
             ctx.setResourceId(storedResourceId);
         }
         return result;
     }
 
     /**
      * Converts the provided string to the corresponding eSciDoc resource object
      * type. E.g., "created-by" will be changed to "user-account".
      * 
      * @param objectType
      *            The object type string to convert.
      * @return Returns the corresponding object type or <code>null</code>.
      *
      */
     protected String fixObjectType(final String objectType) {
 
         return objectType == null ? null : convertToObjectType.get(objectType);
     }
 
     /**
      * Recursively calls the <code>getResourceAttribute</code> method of the
      * provided <code>EvaluationCtx</code> object.<br>
      * The current resource id of the <code>EvaluationCtx</code> is stored. Then
      * the provided newResourceId is set as the resource id and
      * <code>EvaluationCtx.getResourceAttribute</code> is called. Finally, the
      * stored resource id is reset as the <code>EvaluationCtx</code>'s resource
      * id.
      * 
      * @param attributeId
      *            The attributeId to retrieve the attribute for. This is the
      *            complete attribute id that has to be resolved.
      * @param resolvedAttributeId
      *            The currently resolved part of the <code>attributeId</code>.
      *            This may equal to the complete <code>attributeId</code>. In
      *            this case, the provided <code>EvaluationResult</code> object
      *            is returned, as resolving the attribute id has been finished.
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param result
      *            The <code>EvaluationResult</code> object holding the objid(s)
      *            previously fetched and that are used to resolve the provided
      *            new attribute id. If this is <code>null</code>, an empty
      *            result is returned.
      * 
      * @return Returns the result of <code>ctx.getResourceAttribute</code>.
      *
      */
     protected EvaluationResult recursivelyCallCtxGetResourceAttribute(
         final String attributeId, final String resolvedAttributeId,
         final EvaluationCtx ctx, final EvaluationResult result) {
 
         // check if resolving the attribute id is complete
         if (result == null) {
             return CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
         else if (isEmptyResult(result)) {
             return result;
         }
         final int resolvedLength = resolvedAttributeId.length();
         if (attributeId.length() <= resolvedLength) {
             return result;
         }
 
         // extract the new resource id (objid) from the result
         // if more than one is found, the correct one has to be determined by
         // asking for the object-type-related resource id, e.g. component-id.
         // If this is not specified, or the specified is not contained in the
         // provided result, an error result has to be returned.
         final AttributeValue resolvedAttributeValue =
             result.getAttributeValue();
         final String newResourceId;
         final boolean attributeValueIsBag = resolvedAttributeValue.isBag();
         String resolvedObjectType;
         if (!attributeValueIsBag
             || ((BagAttribute) resolvedAttributeValue).size() == 1) {
             // as we only support string attributes, the attribute value is a
             // StringAttribute
             newResourceId = attributeValueIsBag ? ((StringAttribute) ((BagAttribute) resolvedAttributeValue)
                     .iterator().next()).getValue() : ((StringAttribute) resolvedAttributeValue).getValue();
 
             // determine to which object-type the new resource id points
             resolvedObjectType =
                 fixObjectType(resolvedAttributeId.substring(resolvedAttributeId
                     .lastIndexOf(':') + 1));
 
             // now, this could fail in case of "object-type" that can point to
             // different object types like the object reference of a grant.
             // To get the object type, the object type attribute has to be
             // resolved for the new resource id.
             if (resolvedObjectType == null) {
                 try {
                     resolvedObjectType = fetchObjectType(ctx, newResourceId);
                 }
                 catch (final ResourceNotFoundException e) {
                     return CustomEvaluationResultBuilder
                         .createResourceNotFoundResult(e);
                 }
                 catch (final WebserverSystemException e) {
                     return CustomEvaluationResultBuilder
                         .createProcessingErrorResult(e);
                 }
             }
 
             // if determining the object type fails, an empty result has to be
             // returned.
             if (resolvedObjectType == null) {
                 return CustomEvaluationResultBuilder
                     .createEmptyEvaluationResult();
             }
         }
         else {
             // determine to which object-type the new resource id points
             resolvedObjectType =
                 fixObjectType(resolvedAttributeId.substring(resolvedAttributeId
                     .lastIndexOf(':') + 1));
             // multiple resolved objects of unknown type are not supported,
             // an empty result is returned in this case
             if (resolvedObjectType == null) {
                 return CustomEvaluationResultBuilder
                     .createEmptyEvaluationResult();
             }
 
             final BagAttribute bagAttribute =
                 (BagAttribute) resolvedAttributeValue;
             // to continue attribute resolving, the new resource id has to
             // be identified by a provided resource identifier, e.g.
             // component-id
             final String resourceIdentifierAttributeIdValue =
                 AttributeIds.RESOURCE_ATTR_PREFIX + resolvedObjectType + "-id";
             final String providedResourceIdentifier;
             try {
                 providedResourceIdentifier =
                     FinderModuleHelper.retrieveSingleResourceAttribute(ctx,
                         new URI(resourceIdentifierAttributeIdValue), true);
             }
             catch (final EscidocException e) {
                 return CustomEvaluationResultBuilder
                     .createProcessingErrorResult(e);
             }
             catch (final URISyntaxException e) {
                 return CustomEvaluationResultBuilder
                     .createProcessingErrorResult(new WebserverSystemException(e));
             }
 
             // check if the provided id is part of the resolved ids.
             // Otherwise, throw a resource not found exception.
             if (! bagAttribute.contains(new StringAttribute(providedResourceIdentifier))) {
 
                 final ResourceNotFoundException resourceNotFoundException =
                     getResourceNotFoundException(resolvedAttributeId,
                         providedResourceIdentifier);
                 return CustomEvaluationResultBuilder
                     .createResourceNotFoundResult(resourceNotFoundException);
             }
 
             // we have to continue with the found and checked
             // provided resource identifier.
             newResourceId = providedResourceIdentifier;
         }
 
         // determine next attribute id to resolve
         final String unresolvedTail = attributeId.substring(resolvedLength + 1);
         final String nextAttributeIdValue =
             AttributeIds.RESOURCE_ATTR_PREFIX + resolvedObjectType + ':'
                 + unresolvedTail;
 
         return recursivelyCallCtxGetResourceAttribute(ctx, newResourceId,
             nextAttributeIdValue);
     }
 
     /**
      * Gets the value of the specified attribute of the current resource from
      * the provided <code>EvaluationCtx</code>.<br>
      * 
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param attributeId
      *            The id of the attribute to fetch.
      * @return Returns the object type of the resource identified by the
      *         resource-id attribute of the context.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @throws ResourceNotFoundException
      *             Thrown if no resource with the provided id exists.
      *
      */
     protected String fetchSingleResourceAttribute(
         final EvaluationCtx ctx, final String attributeId)
         throws WebserverSystemException, ResourceNotFoundException {
 
         try {
             return FinderModuleHelper.retrieveSingleResourceAttribute(ctx,
                 new URI(attributeId), true);
         }
         catch (final URISyntaxException e) {
             throw new WebserverSystemException(e.getMessage(), e);
         }
     }
 
     /**
      * Recursively calls the <code>getResourceAttribute</code> method of the
      * provided <code>EvaluationCtx</code> object to get a single result.<br>
      * The current resource id of the <code>EvaluationCtx</code> is stored. Then
      * the provided newResourceId is set as the resource id and
      * <code>EvaluationCtx.getResourceAttribute</code> is called. Finally, the
      * stored resource id is reset as the <code>EvaluationCtx</code>'s resource
      * id.
      * 
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param newResourceId
      *            The id of the resource for that the attribute shall be found.
      * @param newAttributeId
      *            The attributeId to retrieve the attribute for.
      * 
      * 
      * @return Returns the result of <code>ctx.getResourceAttribute</code>.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @throws ResourceNotFoundException
      *             Thrown if no resource with the provided id exists.
      *
      */
     protected String fetchSingleResourceAttribute(
         final EvaluationCtx ctx, final String newResourceId,
         final String newAttributeId) throws WebserverSystemException,
         ResourceNotFoundException {
 
         final AttributeValue storedResourceId = ctx.getResourceId();
         ctx.setResourceId(new StringAttribute(newResourceId));
         String ret = null;
         try {
             ret =
                 FinderModuleHelper.retrieveSingleResourceAttribute(ctx,
                     new URI(newAttributeId), true);
         }
         catch (final URISyntaxException e) {
             throw new WebserverSystemException(e.getMessage(), e);
         }
         finally {
             ctx.setResourceId(storedResourceId);
         }
 
         return ret;
     }
 
     /**
      * Gets the object type of the current resource from the provided
      * <code>EvaluationCtx</code>.<br>
      * 
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @return Returns the object type of the resource identified by the
      *         resource-id attribute of the context.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @throws ResourceNotFoundException
      *             Thrown if no resource with the provided id exists.
      *
      */
     protected String fetchObjectType(final EvaluationCtx ctx)
         throws WebserverSystemException, ResourceNotFoundException {
 
         return FinderModuleHelper.retrieveSingleResourceAttribute(ctx,
             Constants.URI_OBJECT_TYPE, true);
     }
 
     /**
      * Gets the object type of the resource identified by the provided id.<br>
      * This method recursively calls attribute resolving for the object type
      * attribute using the provided id as the resource id. The object type is
      * extracted from the result of this call.
      * 
      * @param ctx
      *            The evaluation context to fetch request data from. This data
      *            is needed in order to find the system objects containing the
      *            request attribute.
      * @param newResourceId
      *            The id of the resource for that the attribute shall be found.
      * @return Returns the object type of the resource with the provided id.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @throws ResourceNotFoundException
      *             Thrown if no resource with the provided id exists.
      *
      */
     protected String fetchObjectType(
         final EvaluationCtx ctx, final String newResourceId)
         throws WebserverSystemException, ResourceNotFoundException {
 
         return fetchSingleResourceAttribute(ctx, newResourceId,
             AttributeIds.URN_OBJECT_TYPE);
     }
 
     /**
      * Gets the name of the appropriate resource not found exception in case of
      * an resource not found error during fetching the attribute value for the
      * provided attribute id.<br>
      * The "object-type" found in the attribute id is used to determine the
      * correct exception name. E.g., if the provided attribute is is
      * ...:resource:item:context:status, "...ItemNotFoundException" is returned.
      * 
      * @param attributeIdValue
      *            The value of the attribute id.
      * @return Returns the full name of the resource not found exception for the
      *         failed attribute fetching.
      */
     protected String getResourceNotFoundExceptionName(
         final CharSequence attributeIdValue) {
 
         final StringBuilder exceptionName =
                 new StringBuilder(RESOURCE_NOT_FOUND_EXCEPTION_PACKAGE_PREFIX);
 
         final Matcher matcher =
             PATTERN_PARSE_ATTRIBUTE_ID.matcher(attributeIdValue);
         if (matcher.find()) {
             final String objectType = matcher.group(2);
             exceptionName.append(StringUtility
                 .convertToUpperCaseLetterFormat(objectType));
            if (objectType == null || objectType.equals("")) {
                 System.out.println(
                     "MIH: objectType is empty, attributeValue was " 
                     + attributeIdValue);
             }
         }
         else {
             exceptionName.append("Resource");
         }
         exceptionName.append("NotFoundException");
 
         return exceptionName.toString();
     }
 
     /**
      * Gets the appropriate resource not found exception in case of an resource
      * not found error during resolving the provided attribute id.<br>
      * The "object-type" found in the attribute id is used to determine the
      * correct exception name. E.g., if the provided attribute is is
      * ...:resource:item:context:status, <code>ItemNotFoundException</code> is
      * returned.
      * 
      * @param attributeIdValue
      *            The value of the attribute id.
      * @param resourceId
      *            The id of the resource that could not be found.
      * @return Returns the full name of the resource not found exception for the
      *         failed attribute fetching.
      */
     protected ResourceNotFoundException getResourceNotFoundException(
         final String attributeIdValue, final String resourceId) {
 
         final String errorMsg =
             StringUtility.format("Resource not found",
                 resourceId);
         return getResourceNotFoundException(attributeIdValue, resourceId,
             errorMsg, null);
     }
 
     /**
      * Gets the appropriate resource not found exception in case of an resource
      * not found error during resolving the provided attribute id.<br>
      * The "object-type" found in the attribute id is used to determine the
      * correct exception name. E.g., if the provided attribute id is
      * ...:resource:item:context:status, <code>ItemNotFoundException</code> is
      * returned.
      * 
      * @param attributeIdValue
      *            The value of the attribute id.
      * @param resourceId
      *            The id of the resource that could not be found.
      * @param errorMsg
      *            The exception message.
      * @param cause
      *            The cause for the resource not found exception.
      * @return Returns the full name of the resource not found exception for the
      *         failed attribute fetching.
      */
     protected ResourceNotFoundException getResourceNotFoundException(
         final String attributeIdValue, final String resourceId,
         final String errorMsg, final Throwable cause) {
 
         try {
             final Class<ResourceNotFoundException> exceptionClass =
                 (Class<ResourceNotFoundException>) Class
                     .forName(getResourceNotFoundExceptionName(attributeIdValue));
             final Constructor<ResourceNotFoundException> constructor =
                 exceptionClass.getConstructor(new Class[] { String.class,
                     Throwable.class });
             return constructor.newInstance(errorMsg, cause);
         }
         catch (final Exception e) {
             return new ResourceNotFoundException(errorMsg, e);
         }
     }
 
     /**
      * Checks if the provided <code>EvaluationResult</code> does not contain a
      * attribute value.
      * 
      * @param result
      *            The <code>EvaluationResult</code> object to check.
      * @return Returns <code>true</code> if the provided
      *         <code>EvaluationResult</code>'s attribute value is null or empty.
      *
      */
     protected boolean isEmptyResult(final EvaluationResult result) {
 
         final BagAttribute bag = (BagAttribute) result.getAttributeValue();
         return bag == null || bag.isEmpty();
     }
 
 }
