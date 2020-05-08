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
 package de.escidoc.core.common.business.fedora;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.Container;
 import de.escidoc.core.common.business.fedora.resources.Item;
 import de.escidoc.core.common.business.fedora.resources.Relation;
 import de.escidoc.core.common.business.fedora.resources.cmm.ContentModel;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource;
 import de.escidoc.core.common.business.fedora.resources.interfaces.VersionableResource;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.FileSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.AddNewSubTreesToDatastream;
 import de.escidoc.core.common.util.stax.handler.ItemRelsExtUpdateHandler;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.CommonFoXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 import de.escidoc.core.st.service.interfaces.StagingFileHandlerInterface;
 import org.apache.xpath.XPathAPI;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.access.BeanFactoryLocator;
 import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.transform.TransformerException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Some utilities.
  * 
  * @author MSC
  * @spring.bean id="business.Utility"
  */
 public class Utility {
 
     private static AppLogger log = new AppLogger(Utility.class.getName());
 
     private StagingFileHandlerInterface stagingFileHandler;
 
     /**
      * The pattern used to extract the redirect base url and path from the
      * staging file XML representation.
      */
     private static final Pattern REDIRECT_URL_PATTERN =
         Pattern.compile("xml:base=\"(.*?)\".*xlink:href=\"(.*?)\"");
 
     /**
      * Splits a Predicate at the last slash (/).
      * 
      * @param predicate
      *            Predicate.
      * @return split predicate
      */
     public String[] splitPredicate(final String predicate) {
         String[] result = new String[2];
         int index = predicate.lastIndexOf("/");
         if (index != -1) {
             result[0] = predicate.substring(predicate.lastIndexOf("/") + 1);
             result[1] = predicate.substring(0, index);
         }
         return result;
     }
 
     /**
      * Concatenates the two given path segments and returns a valid path, i.e.
      * the method takes care that there is only one path separator between the
      * path segments.
      * 
      * @param path
      *            The path.
      * @param appendix
      *            The path to append.
      * @return The concatenated path.
      * @st
      */
     public static String concatenatePath(
         final String path, final String appendix) {
         String result = path;
         String append = appendix;
         result = result.replace("\\", "/");
         append = append.replace("\\", "/");
         if (!result.endsWith("/")) {
             if (!append.startsWith("/")) {
                 result += "/" + append;
             }
             else {
                 result += append;
             }
         }
         else {
             if (!append.startsWith("/")) {
                 result += append;
             }
             else {
                 result += append.substring(1);
             }
         }
         return result;
     }
 
     /**
      * Fetches the id from the link. It is the String after the last '/' in the
      * link.
      * 
      * @param link
      *            The link
      * @return The extracted id.
      */
     public static String getId(final String link) {
         String result = link;
         int index = link.lastIndexOf("/");
         if (index != -1) {
             result = link.substring(link.lastIndexOf("/") + 1);
         }
         return result;
     }
 
     /**
      * Check if locked is set to false. If it set to true a LockingException is
      * thrown.
      * 
      * @param locked
      *            Indicates if the object is locked.
      * @param method
      *            The method which shall be executed.
      * @param label
      *            The label identifying the object.
      * @param lockOwner
      *            The lock owner if there was one found.
      * @return true if the object is not locked.
      * @throws LockingException
      *             Thrown if the object is locked.
      */
     public boolean checkUnlocked(
         final boolean locked, final String method, final String label,
         final String lockOwner) throws LockingException {
 
         if (locked) {
             throw new LockingException(method + " failed!" + label
                 + " is locked by " + lockOwner + ".");
         }
         return true;
     }
 
     /**
      * Checks if the given dates are equal. If so the change is permitted and
      * true is returned. Otherwise a LockingException is throws.
      * 
      * @param fedoraLatestVersionDate
      *            The date of the latest version stored in Fedora.
      * @param updateLatestVersionDate
      *            The date of the version the application retrieved and wants to
      *            update.
      * @param label
      *            A label to identify the object to change.
      * @throws OptimisticLockingException
      *             Thrown if a change of the object is not permitted.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     public void checkOptimisticLockingCriteria(
         final DateTime fedoraLatestVersionDate,
         final DateTime updateLatestVersionDate, final String label)
         throws OptimisticLockingException, WebserverSystemException {
 
         if (!fedoraLatestVersionDate.equals(updateLatestVersionDate)) {
 
             String text = label;
             if (text == null) {
                 text = "object to change";
             }
 
             String message =
                 "Optimistic locking error! Version of " + text
                     + " does not match most recent version (requested:"
                     + updateLatestVersionDate + " saved:"
                     + fedoraLatestVersionDate + ")! Changes are not permitted.";
             log.info(message);
             throw new OptimisticLockingException(message);
         }
     }
 
     /**
      * Checks if the given dates are equal. If so the change is permitted and
      * true is returned. Otherwise a LockingException is throws.
      * 
      * @param fedoraLatestVersionDate
      *            The date of the latest version stored in Fedora.
      * @param updateLatestVersionDate
      *            The date of the version the application retrieved and wants to
      *            update.
      * @param label
      *            A label to identify the object to change.
      * @return true if change is permitted
      * @throws OptimisticLockingException
      *             Thrown if a change of the object is not permitted.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     public boolean checkOptimisticLockingCriteria(
         final String fedoraLatestVersionDate,
         final String updateLatestVersionDate, final String label)
         throws OptimisticLockingException, WebserverSystemException {
 
         if ((fedoraLatestVersionDate != null)
             && (updateLatestVersionDate != null)) {
 
             DateTime tFedora = new DateTime(fedoraLatestVersionDate);
             DateTime tUpdate = new DateTime(updateLatestVersionDate);
 
             checkOptimisticLockingCriteria(tFedora, tUpdate, label);
         }
         return true;
     }
 
     /**
      * Get the id and the name of the current user from the UserContext.
      * 
      * @return The the id and the name of the current user. ([0] = id, [1] =
      *         name)
      * @throws WebserverSystemException
      *             If the current user could not be retrieved.
      */
     public String[] getCurrentUser() throws WebserverSystemException {
 
         String[] result = new String[2];
         if ((UserContext.getId() == null)
             || (UserContext.getRealName() == null)) {
             throw new WebserverSystemException(
                 "System fault: Current user not set!");
         }
         result[0] = UserContext.getId();
         result[1] = UserContext.getRealName();
 
         return result;
     }
 
     /**
      * Returns true if both objects specified by id have the same context.
      * 
      * @param id0
      *            of object
      * @param id1
      *            of object
      * @return true if objects has same context else false.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     public boolean hasSameContext(final String id0, final String id1)
         throws SystemException {
         String context0 = TripleStoreUtility.getInstance().getContext(id0);
         String context1 = TripleStoreUtility.getInstance().getContext(id1);
 
         if (context0 == null || !context0.equals(context1)) {
             return false;
         }
         return true;
     }
 
     /**
      * Returns an Utility instance.
      * 
      * @return An Utility instance.
      */
     public static Utility getInstance() {
         BeanFactoryLocator beanFactoryLocator =
             SingletonBeanFactoryLocator.getInstance();
         BeanFactory factory =
             beanFactoryLocator
                 .useBeanFactory("Om.spring.ejb.context").getFactory();
         return (Utility) factory.getBean("business.Utility");
     }
 
     /**
      * Checks if the given param is not null.
      * 
      * @param param
      *            The param to check.
      * @param label
      *            The label to add to the exception message.
      * @throws MissingMethodParameterException
      *             If the param is null.
      */
     public void checkNotNull(final Object param, final String label)
         throws MissingMethodParameterException {
         if (param == null) {
             throw new MissingMethodParameterException(label
                 + " must not be null!");
         }
 
     }
 
     /**
      * Checks if a component with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws ComponentNotFoundException
      *             If the component does not exist or if the object is no
      *             component.
      */
     public void checkIsComponent(final String id) throws SystemException,
         ComponentNotFoundException {
 
         try {
             checkIsOfObjectType(id, Constants.COMPONENT_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ComponentNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if a container with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws ContainerNotFoundException
      *             If the container does not exist or if the object is no
      *             container.
      */
     public void checkIsContainer(final String id) throws SystemException,
         ContainerNotFoundException {
 
         try {
             checkIsOfObjectType(id, Constants.CONTAINER_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ContainerNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if a context with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws ContextNotFoundException
      *             If the context does not exist or if the object is no context.
      */
     public void checkIsContext(final String id) throws SystemException,
         ContextNotFoundException {
 
         try {
             checkIsOfObjectType(id, Constants.CONTEXT_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ContextNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if a Content Relation with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws ContentRelationNotFoundException
      *             Thrown if Content Relation the does not exist.
      */
     public void checkIsContentRelation(final String id) throws SystemException,
         ContentRelationNotFoundException {
 
         try {
             checkIsOfObjectType(id, Constants.CONTENT_RELATION2_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ContentRelationNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if a content model with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws ContentModelNotFoundException
      *             If the content model does not exist or if the object is no
      *             content model.
      * @throws IntegritySystemException
      * @throws WebserverSystemException
      * @throws TripleStoreSystemException
      */
     public void checkIsContentModel(final String id)
         throws ContentModelNotFoundException, TripleStoreSystemException,
         WebserverSystemException, IntegritySystemException {
 
         try {
             checkIsOfObjectType(id, Constants.CONTENT_MODEL_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ContentModelNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if an item with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws ItemNotFoundException
      *             If the item does not exist or if the object is no item.
      * @throws WebserverSystemException
      *             Thrown if instance of TripleStore failed.
      * @throws TripleStoreSystemException
      *             Thrown in case of Triple Store request or connection failure.
      * @throws IntegritySystemException
      *             Thrown if there is an integrity error with the addressed
      *             object.
      */
     public void checkIsItem(final String id) throws ItemNotFoundException,
         TripleStoreSystemException, WebserverSystemException,
         IntegritySystemException {
 
         try {
             checkIsOfObjectType(id, Constants.ITEM_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ItemNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if a relation with id exists.
      * 
      * @param id
      *            The id.
      * @throws ContentRelationNotFoundException
      *             If the relation does not exist or if the object is no
      *             relation.
      * @throws WebserverSystemException
      *             Thrown if instance of TripleStore failed.
      * @throws TripleStoreSystemException
      *             Thrown in case of Triple Store request or connection failure.
      * @throws IntegritySystemException
      *             Thrown if there is an integrity error with the addressed
      *             object.
      */
     public void checkIsRelation(final String id)
         throws ContentRelationNotFoundException, TripleStoreSystemException,
         WebserverSystemException, IntegritySystemException {
 
         try {
             checkIsOfObjectType(id, Constants.RELATION_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new ContentRelationNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Checks if an organizational-unit with id exists.
      * 
      * @param id
      *            The id of the object.
      * @throws OrganizationalUnitNotFoundException
      *             If the organizational-unit does not exist or if the object is
      *             no organizational-unit.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     public void checkIsOrganizationalUnit(final String id)
         throws OrganizationalUnitNotFoundException, SystemException {
 
         try {
             checkIsOfObjectType(id, Constants.ORGANIZATIONAL_UNIT_OBJECT_TYPE);
         }
         catch (ResourceNotFoundException e) {
             throw new OrganizationalUnitNotFoundException(e.getMessage(), e);
         }
     }
 
     /**
      * Check if the object given by id exists and is of the given type.
      * 
      * @param id
      *            The id.
      * @param type
      *            The expected type.
      * @throws ResourceNotFoundException
      *             Thrown if resource with provided type does not exist.
      * @throws WebserverSystemException
      *             Thrown if instance of TripleStore failed.
      * @throws TripleStoreSystemException
      *             Thrown if request or connection to Triple Store failed.
      * @throws IntegritySystemException
      *             Thrown if no object type is found for an existing object.
      */
     public void checkIsOfObjectType(final String id, final String type)
         throws ResourceNotFoundException, TripleStoreSystemException,
         WebserverSystemException, IntegritySystemException {
 
         String idWithoutVersionNumber = XmlUtility.getObjidWithoutVersion(id);
         TripleStoreUtility tu = TripleStoreUtility.getInstance();
 
         final String objectType = tu.getObjectType(idWithoutVersionNumber);
         if (objectType == null) {
             if (!tu.exists(idWithoutVersionNumber)) {
                 throw new ResourceNotFoundException("Object with id "
                     + idWithoutVersionNumber + " does not exist!");
             }
             else {
                 // object exists but has no object-type
                 throw new IntegritySystemException(StringUtility
                     .concatenateWithBracketsToString(
                         "Object has no object-type ", idWithoutVersionNumber));
             }
         }
         else if (!objectType.equals(type)) {
             throw new ResourceNotFoundException("Object with id "
                 + idWithoutVersionNumber + " is not a " + type + "!");
         }
     }
 
     /**
      * Check if the provided object in the xmlData has the same Context id than
      * the object provided by id.
      * 
      * @param id
      *            It of the reference object.
      * @param xmlData
      *            The XML representation of an object.
      * @throws InvalidContextException
      *             Thrown if the contextId of xmlData differs from the ContextId
      *             of the reference object.
      * @throws SystemException
      *             Thrown in case of internal failure.
      */
     public void checkSameContext(final String id, final String xmlData)
         throws InvalidContextException, SystemException {
 
         // TODO This is a quick fix hack. Change to StAX parser. And, may be,
         // move the check to another level in create to avoid a double XML
         // parsing and validating.
 
         String dataContextId = null;
 
         try {
             Document result = null;
             DocumentBuilderFactory docBuilderFactory =
                 DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
             InputStream in =
                 new ByteArrayInputStream(xmlData
                     .getBytes(XmlUtility.CHARACTER_ENCODING));
             result = docBuilder.parse(new InputSource(in));
             result.getDocumentElement().normalize();
 
             if (UserContext.isRestAccess()) {
                 Node n =
                     XPathAPI.selectSingleNode(result,
                         "//properties/context/@href");
 
                 String contextHref = n.getNodeValue();
                 dataContextId =
                     contextHref.substring(contextHref.lastIndexOf("/") + 1);
             }
             else {
                 Node n =
                     XPathAPI.selectSingleNode(result,
                         "//properties/context/@objid");
 
                 dataContextId = n.getNodeValue();
             }
 
         }
         catch (ParserConfigurationException e) {
             throw new SystemException(e);
         }
         catch (SAXException e) {
             throw new SystemException(e);
         }
         catch (IOException e) {
             throw new SystemException(e);
         }
         catch (TransformerException e) {
             throw new SystemException(e);
         }
 
         if (!dataContextId.equals(TripleStoreUtility.getInstance().getContext(
             id))) {
             throw new InvalidContextException(
                 "Objects are not in same Context.");
         }
 
     }
 
     public String getPath(final String id, final String newVersionNumber)
         throws WebserverSystemException, TripleStoreSystemException {
 
         StringBuffer result = new StringBuffer("/");
 
         String objectType = TripleStoreUtility.getInstance().getObjectType(id);
         if (Constants.ITEM_OBJECT_TYPE.equals(objectType)
             || Constants.CONTAINER_OBJECT_TYPE.equals(objectType)
             || Constants.CONTEXT_OBJECT_TYPE.equals(objectType)) {
             result.append("ir/");
         }
         else {
             throw new WebserverSystemException(
                 "Can not create path for object-type " + objectType + ".");
         }
 
         result.append(objectType);
         result.append("/");
         result.append(id);
         if (newVersionNumber != null) {
             result.append(":");
             result.append(newVersionNumber);
         }
 
         return result.toString();
     }
 
     /**
      * Create new object version.
      * 
      * @param versionComment
      *            Comment of event (version or status change)
      * @param newStatus
      *            New status of object.
      * @param resource
      *            resource object.
      * @param fedoraUtility
      *            The {@link FedoraUtility} to use for accessing the data store
      *            back end.
      * @throws SystemException
      *             Thrown in case of an internal system error.
      */
     public void makeVersion(
         final String versionComment, final String newStatus,
         final VersionableResource resource, final FedoraUtility fedoraUtility)
         throws SystemException {
 
         boolean release = false;
         String comment = createComment(resource, newStatus, versionComment);
 
         Map<String, String> resBaseData = getResourceBaseData(resource);
         Map<String, StartElementWithChildElements> updateElementsRelsExt =
             new TreeMap<String, StartElementWithChildElements>();
         Map<String, Vector<StartElementWithChildElements>> removeElementsRelsExt =
             new TreeMap<String, Vector<StartElementWithChildElements>>();
         Map<String, String> currentVersionProperties =
             resource.getResourceProperties();
 
         // elements to update in RELS-EXT in any case
         // - latest-version.date
         // - latest-version.user
         StartElementWithChildElements modifiedBy =
             new StartElementWithChildElements(Elements.ELEMENT_MODIFIED_BY,
                 Constants.STRUCTURAL_RELATIONS_NS_URI,
                 Constants.STRUCTURAL_RELATIONS_NS_PREFIX, null,
                 getCurrentUserId(), null);
         Attribute resourceAttribute =
             new Attribute("resource", Constants.RDF_NAMESPACE_URI,
                 Constants.RDF_NAMESPACE_PREFIX, "info:fedora/"
                     + getCurrentUserId());
         modifiedBy.addAttribute(resourceAttribute);
         updateElementsRelsExt.put(Elements.ELEMENT_MODIFIED_BY, modifiedBy);
 
         updateElementsRelsExt.put(Elements.ELEMENT_MODIFIED_BY_TITLE,
             new StartElementWithChildElements(
                 Elements.ELEMENT_MODIFIED_BY_TITLE,
                 Constants.PROPERTIES_NS_URI, Constants.PROPERTIES_NS_PREFIX,
                 null, getCurrentUserRealName(), null));
 
         String buildNumber = Utility.getInstance().getBuildNumber();
         updateElementsRelsExt.put("build", new StartElementWithChildElements(
             "build", "http://escidoc.de/core/01/system/", "system", null,
             buildNumber, null));
 
         if (newStatus != null) {
 
             release = false;
             // set new event entry and update some values in RELS-EXT,
             // version
             // renew in RELS-EXT:
             // - status
             // - latest-version.date ( done )
             // - latest-version.user ( done )
             // - latest-version.comment ( done )
             // - latest-version.status
 
             // if status is already "released", leave it untouched
             // unless withdraw
             if (!Constants.STATUS_RELEASED.equals(currentVersionProperties
                 .get(PropertyMapKeys.PUBLIC_STATUS))
                 || Constants.STATUS_WITHDRAWN.equals(newStatus)) {
                 updateElementsRelsExt.put(Elements.ELEMENT_PUBLIC_STATUS,
                     new StartElementWithChildElements(
                         Elements.ELEMENT_PUBLIC_STATUS,
                         Constants.PROPERTIES_NS_URI,
                         Constants.PROPERTIES_NS_PREFIX, null, newStatus, null));
                 // - public-status-comment
                 updateElementsRelsExt.put(
                     Elements.ELEMENT_PUBLIC_STATUS_COMMENT,
                     new StartElementWithChildElements(
                         Elements.ELEMENT_PUBLIC_STATUS_COMMENT,
                         Constants.PROPERTIES_NS_URI,
                         Constants.PROPERTIES_NS_PREFIX, null, comment, null));
             }
             if (!Constants.STATUS_WITHDRAWN.equals(newStatus)) {
                 updateElementsRelsExt.put(Elements.ELEMENT_STATUS,
                     new StartElementWithChildElements(Elements.ELEMENT_STATUS,
                         Constants.VERSION_NS_URI, Constants.VERSION_NS_PREFIX,
                         null, newStatus, null));
                 // - latest-version.comment
                 updateElementsRelsExt.put(Elements.ELEMENT_COMMENT,
                     new StartElementWithChildElements(Elements.ELEMENT_COMMENT,
                         Constants.VERSION_NS_URI, Constants.VERSION_NS_PREFIX,
                         null, comment, null));
             }
 
             if (Constants.STATUS_RELEASED.equals(newStatus)) {
                 release = true;
                 resource.setVersionStatusChange();
                 resource.setVersionStatus(Constants.STATUS_RELEASED);
 
                 // renew in RELS-EXT:
                 // - public-status-comment
                 // - latest-release.number
                 // - latest-release.date
                 // - latest-release.pid
                 updateElementsRelsExt.put(
                     Elements.ELEMENT_PUBLIC_STATUS_COMMENT,
                     new StartElementWithChildElements(
                         Elements.ELEMENT_PUBLIC_STATUS_COMMENT,
                         Constants.PROPERTIES_NS_URI,
                         Constants.PROPERTIES_NS_PREFIX, null, comment, null));
                 updateElementsRelsExt.put(Elements.ELEMENT_NUMBER,
                     new StartElementWithChildElements(Elements.ELEMENT_NUMBER,
                         Constants.RELEASE_NS_URI, Constants.RELEASE_NS_PREFIX,
                         null, currentVersionProperties
                             .get(PropertyMapKeys.LATEST_VERSION_NUMBER), null));
 
                 // SWA: The release date has to be the same as the version/date
                 // which is written as latest.
 
                 // update latest-release/pid (properties/release/pid)
                 updateElementsRelsExt.put(Constants.RELEASE_NS_URI
                     + Elements.ELEMENT_PID, new StartElementWithChildElements(
                     Elements.ELEMENT_PID, Constants.RELEASE_NS_URI,
                     Constants.RELEASE_NS_PREFIX, null, currentVersionProperties
                         .get(PropertyMapKeys.CURRENT_VERSION_PID), null));
             }
 
             // now, everything except version-history is written. So
             // update the timestamp to use it in version-history
             // and write it to RELS-EXT. But the timestamp is obtained from
             // first RELS-EXT wrote, which is done in the persist method. That's
             // why here only the XML structure with a placeholder is written.
             // The placeholder is later (during the persist method) replaced
             // with the exact timestamp.
 
             // update version-history
             Map<String, StartElementWithChildElements> updateElementsWOV =
                 new HashMap<String, StartElementWithChildElements>();
 
             if (!Constants.STATUS_WITHDRAWN.equals(newStatus)) {
                 // change first occurrence of comment in version-history
                 updateElementsWOV.put(TripleStoreUtility.PROP_VERSION_COMMENT,
                     new StartElementWithChildElements(
                         TripleStoreUtility.PROP_VERSION_COMMENT,
                         Constants.WOV_NAMESPACE_URI,
                         Constants.WOV_NAMESPACE_PREFIX, null, comment, null));
                 // change first occurrence of version-status in version-history
                 // but not for withdraw
                 updateElementsWOV.put(TripleStoreUtility.PROP_VERSION_STATUS,
                     new StartElementWithChildElements(
                         TripleStoreUtility.PROP_VERSION_STATUS,
                         Constants.WOV_NAMESPACE_URI,
                         Constants.WOV_NAMESPACE_PREFIX, null, newStatus, null));
             }
             List<StartElementWithChildElements> elementsToAdd =
                 new Vector<StartElementWithChildElements>();
             // elementsToAdd.add(versionStatus);
 
             // add premis:event to version-history/version[1]/events as
             // first child
             String newEventEntry =
                 createEventXml(resource.getId(), resBaseData
                     .get("resourceBaseUrl"), getCurrentUserRealName(),
                     getCurrentUserId(),
                     XmlTemplateProvider.TIMESTAMP_PLACEHOLDER, newStatus,
                     comment, currentVersionProperties);
 
             // change /version-history/version[version-number='x']/timestamp
             // this method changes exactly the first occurrence of the timestamp
             // - which is in this case the same (A support for XPath expressions
             // is not given due this parsers.)
             updateElementsWOV.put(Constants.WOV_NAMESPACE_URI + "timestamp",
                 new StartElementWithChildElements("timestamp",
                     Constants.WOV_NAMESPACE_URI,
                     Constants.WOV_NAMESPACE_PREFIX, null,
                     XmlTemplateProvider.TIMESTAMP_PLACEHOLDER, null, 1));
 
             // do update WOV
             writeEvent(resource, newEventEntry, updateElementsWOV,
                 elementsToAdd);
 
             // Update the status in the resource
             if (!Constants.STATUS_WITHDRAWN.equals(newStatus)) {
                 resource.setResourceProperties(
                     PropertyMapKeys.CURRENT_VERSION_STATUS, newStatus);
             }
         }
         else { // if (newStatus == null)
             // this is an update
 
             // - latest-version.comment
             updateElementsRelsExt.put(Elements.ELEMENT_COMMENT,
                 new StartElementWithChildElements(Elements.ELEMENT_COMMENT,
                     Constants.VERSION_NS_URI, Constants.VERSION_NS_PREFIX,
                     null, comment, null));
 
             // create new version entry
             // renew in RELS-EXT:
             // - latest-version.number
             // - latest-version.date ( done )
             // - latest-version.status (unchanged)
             // - latest-version.valid-status (unchanged)
             // - latest-version.user ( done )
             // - latest-version.comment ( done )
 
             int newVersionNumberInt =
                 Integer.parseInt(currentVersionProperties
                     .get(PropertyMapKeys.LATEST_VERSION_NUMBER)) + 1;
 
             updateElementsRelsExt.put(Elements.ELEMENT_NUMBER,
                 new StartElementWithChildElements(Elements.ELEMENT_NUMBER,
                     Constants.VERSION_NS_URI, Constants.VERSION_NS_PREFIX,
                     null, Integer.toString(newVersionNumberInt), null));
 
             if (Constants.STATUS_RELEASED.equals(currentVersionProperties
                 .get(PropertyMapKeys.LATEST_VERSION_VERSION_STATUS))) {
                 // update of resource in state released
                 // change latest-version.status to pending
 
                 updateElementsRelsExt.put(Elements.ELEMENT_STATUS,
                     new StartElementWithChildElements(Elements.ELEMENT_STATUS,
                         Constants.VERSION_NS_URI, Constants.VERSION_NS_PREFIX,
                         null, Constants.STATUS_PENDING, null));
 
                 currentVersionProperties.put(
                     PropertyMapKeys.LATEST_VERSION_VERSION_STATUS,
                     Constants.STATUS_PENDING);
             }
 
             // remove version.pid
             StartElementWithChildElements propertyToDelete =
                 new StartElementWithChildElements(Constants.RELEASE_NS_URI
                     + Elements.ELEMENT_PID, Constants.VERSION_NS_URI,
                     Constants.VERSION_NS_PREFIX, null, null, null);
             Vector<StartElementWithChildElements> toRemove =
                 new Vector<StartElementWithChildElements>();
             toRemove.add(propertyToDelete);
             removeElementsRelsExt.put("/RDF/Description/pid", toRemove);
 
             // write version-history (Keep in mind, that at this point a
             // placeholder for the timestamp is written. This placeholder is
             // replaced during persist().)
             String newVersionXml =
                 createVersionXml(resource, resBaseData,
                     currentVersionProperties, newVersionNumberInt, comment);
 
             prependVersion(resource, newVersionXml);
         }
 
         // last operation is to update the timestamp in RELS-EXT
         if (release) {
             resource.setResourceProperties(
                 PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER,
                 currentVersionProperties
                     .get(PropertyMapKeys.CURRENT_VERSION_VERSION_NUMBER));
         }
         // write changes to RELS-EXT
         updateElementsInRelsExt(updateElementsRelsExt, removeElementsRelsExt,
             resource, currentVersionProperties
                 .get(PropertyMapKeys.PUBLIC_STATUS), release);
     }
 
     /**
      * Update elements in RELS-EXt of versionable resource.
      * 
      * @param updateElementsRelsExt
      *            elements to update
      * @param removeElementsRelsExt
      *            elements to remove from RELS-EXT
      * @param resource
      *            object resource
      * @param currentPublicStatus
      *            public-status of current version
      * @param release
      *            set true if version is release, false otherwise
      * @throws SystemException
      *             Thrown in case of internal failure.
      */
     private void updateElementsInRelsExt(
         final Map<String, StartElementWithChildElements> updateElementsRelsExt,
         final Map<String, Vector<StartElementWithChildElements>> removeElementsRelsExt,
         final VersionableResource resource, final String currentPublicStatus,
         final boolean release) throws SystemException {
 
         StaxParser sp = new StaxParser();
         ItemRelsExtUpdateHandler itemRelsExtUpdateHandler =
             new ItemRelsExtUpdateHandler(updateElementsRelsExt, sp);
         sp.addHandler(itemRelsExtUpdateHandler);
 
         HashMap<String, String> pathes = new HashMap<String, String>();
         pathes.put("/RDF", null);
         MultipleExtractor me = new MultipleExtractor(pathes, sp);
         me.removeElements(removeElementsRelsExt);
         sp.addHandler(me);
 
         try {
             ByteArrayInputStream relsExtBA = null;
             if (release
                 && !Constants.STATUS_RELEASED.equals(currentPublicStatus)) {
 
                 // FIXME if FIRST release add "latest-release" properties but
                 // not by string replace
                 String relsExtS =
                     new String(resource.getRelsExt().getStream(),
                         XmlUtility.CHARACTER_ENCODING);
                 relsExtS =
                     relsExtS.replaceFirst("(</rdf:Description>)", "<"
                         + Constants.RELEASE_NS_PREFIX + ":"
                         + Elements.ELEMENT_NUMBER + " xmlns:"
                         + Constants.RELEASE_NS_PREFIX + "=\""
                         + Constants.RELEASE_NS_URI + "\"/>\n<"
                         + Constants.RELEASE_NS_PREFIX + ":"
                         + Elements.ELEMENT_DATE + " xmlns:"
                         + Constants.RELEASE_NS_PREFIX + "=\""
                         + Constants.RELEASE_NS_URI + "\">---</"
                         + Constants.RELEASE_NS_PREFIX + ":"
                         + Elements.ELEMENT_DATE + ">\n" + "$1");
                 relsExtBA =
                     new ByteArrayInputStream(relsExtS
                         .getBytes(XmlUtility.CHARACTER_ENCODING));
             }
             else {
                 relsExtBA =
                     new ByteArrayInputStream(resource.getRelsExt().getStream());
             }
             sp.parse(relsExtBA);
             ByteArrayOutputStream relsExt =
                 (ByteArrayOutputStream) me.getOutputStreams().get("RDF");
             resource
                 .setRelsExt(relsExt.toString(XmlUtility.CHARACTER_ENCODING));
         }
         catch (NullPointerException e) {
             throw new XmlParserSystemException(e);
         }
         catch (XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (UnsupportedEncodingException e) {
             throw new EncodingSystemException(e);
         }
         catch (LockingException e) {
             throw new IntegritySystemException(e);
         }
         catch (Exception e) {
             throw new SystemException("Unexpected Exception.", e);
         }
 
     }
 
     /**
      * Create a new entry for the version-history (WOV).
      * 
      * @param resource
      *            The versionable resource.
      * @param resBaseData
      * @param currentVersionProperties
      * @param newVersionNumberInt
      * @param comment
      *            The version comment.
      * @return WOV entry for new version
      * @throws WebserverSystemException
      * @throws TripleStoreSystemException
      */
     private String createVersionXml(
         final VersionableResource resource,
         final Map<String, String> resBaseData,
         final Map<String, String> currentVersionProperties,
         final int newVersionNumberInt, final String comment)
         throws WebserverSystemException, TripleStoreSystemException {
 
         // Map for version entry values
         Map<String, String> newVersionEntry = new HashMap<String, String>();
 
         // compute new latest version data
         newVersionEntry.put(XmlTemplateProvider.VAR_NAMESPACE_PREFIX,
             Constants.WOV_NAMESPACE_PREFIX);
         newVersionEntry.put(XmlTemplateProvider.VAR_NAMESPACE,
             Constants.WOV_NAMESPACE_URI);
         newVersionEntry.put(XmlTemplateProvider.OBJID, resource.getId() + ":"
             + Integer.toString(newVersionNumberInt));
         newVersionEntry.put(XmlTemplateProvider.TITLE, "Version "
             + Integer.toString(newVersionNumberInt));
 
         newVersionEntry.put(XmlTemplateProvider.HREF, resource.getHref() + ":"
             + Integer.toString(newVersionNumberInt));
         newVersionEntry.put(XmlTemplateProvider.VERSION_NUMBER, Integer
             .toString(newVersionNumberInt));
         // real timestamp is not clear at this point (will pre fixed lated
         // during persist)
         // newVersionEntry.put(XmlTemplateProvider.TIMESTAMP,
         // newLatestModificationTimestamp);
         newVersionEntry.put(XmlTemplateProvider.TIMESTAMP,
             XmlTemplateProvider.TIMESTAMP_PLACEHOLDER);
         newVersionEntry.put(XmlTemplateProvider.VAR_STATUS,
             currentVersionProperties
                 .get(PropertyMapKeys.LATEST_VERSION_VERSION_STATUS));
         newVersionEntry.put(XmlTemplateProvider.VALID_STATUS,
             currentVersionProperties
                 .get(PropertyMapKeys.LATEST_VERSION_VALID_STATUS));
         newVersionEntry.put(XmlTemplateProvider.VAR_COMMENT, XmlUtility
             .escapeForbiddenXmlCharacters(comment));
         newVersionEntry.put(XmlTemplateProvider.VAR_AGENT_ID_VALUE,
             getCurrentUserId());
         newVersionEntry.put(XmlTemplateProvider.VAR_AGENT_ID_TYPE,
             Constants.PREMIS_ID_TYPE_ESCIDOC);
         newVersionEntry.put(XmlTemplateProvider.VAR_AGENT_BASE_URI,
             Constants.USER_ACCOUNT_URL_BASE);
         newVersionEntry.put(XmlTemplateProvider.VAR_AGENT_TITLE,
             getCurrentUserRealName());
 
         newVersionEntry.put(XmlTemplateProvider.VAR_EVENT_TYPE, "update");
         newVersionEntry.put(XmlTemplateProvider.VAR_EVENT_XMLID, "v"
             + Integer.toString(newVersionNumberInt) + "e"
             + System.currentTimeMillis());
         newVersionEntry.put(XmlTemplateProvider.VAR_EVENT_ID_VALUE, resBaseData
             .get("resourceBaseUrl")
             + resource.getId()
             + "/resources/"
             + Elements.ELEMENT_WOV_VERSION_HISTORY
             + "#"
             + newVersionEntry.get(XmlTemplateProvider.VAR_EVENT_XMLID));
         newVersionEntry.put(XmlTemplateProvider.VAR_EVENT_ID_TYPE,
             Constants.PREMIS_ID_TYPE_URL_RELATIVE);
         newVersionEntry.put(XmlTemplateProvider.VAR_OBJECT_ID_TYPE,
             Constants.PREMIS_ID_TYPE_ESCIDOC);
         newVersionEntry.put(XmlTemplateProvider.VAR_OBJECT_ID_VALUE, resource
             .getId());
 
         // get xml representation of new version
         String newVersionXml =
             CommonFoXmlProvider.getInstance().getWovVersionEntryXml(
                 newVersionEntry);
 
         return newVersionXml;
     }
 
     /**
      * Set event values to HashMap and call version-history event entry.
      * 
      * @param resourceId
      *            The id of the resource.
      * @param resourceBaseUrl
      *            The resource base URL
      * @param currentUserName
      *            The name of the current user.
      * @param currentUserId
      *            The id of the current user.
      * @param latestModificationTimestamp
      * @param newStatus
      *            New version-status
      * @param comment
      *            The version comment.
      * @param currentVersionProperties
      *            map with properties of current version
      * @return XML event entry for version-history
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     private String createEventXml(
         final String resourceId, final String resourceBaseUrl,
         final String currentUserName, final String currentUserId,
         final String latestModificationTimestamp, final String newStatus,
         final String comment, final Map<String, String> currentVersionProperties)
         throws WebserverSystemException {
 
         HashMap<String, String> eventValues = new HashMap<String, String>();
         eventValues.put(XmlTemplateProvider.VAR_EVENT_TYPE, newStatus);
         eventValues.put(XmlTemplateProvider.VAR_EVENT_XMLID, "v"
             + currentVersionProperties
                 .get(PropertyMapKeys.LATEST_VERSION_NUMBER) + "e"
             + System.currentTimeMillis());
         eventValues.put(XmlTemplateProvider.VAR_EVENT_ID_TYPE,
             Constants.PREMIS_ID_TYPE_URL_RELATIVE);
         eventValues.put(XmlTemplateProvider.VAR_EVENT_ID_VALUE, resourceBaseUrl
             + resourceId + "/resources/" + Elements.ELEMENT_WOV_VERSION_HISTORY
             + "#" + eventValues.get(XmlTemplateProvider.VAR_EVENT_XMLID));
         eventValues.put(XmlTemplateProvider.TIMESTAMP,
             latestModificationTimestamp);
         eventValues.put(XmlTemplateProvider.VAR_COMMENT, XmlUtility
             .escapeForbiddenXmlCharacters(comment));
         eventValues.put(XmlTemplateProvider.VAR_AGENT_BASE_URI,
             Constants.USER_ACCOUNT_URL_BASE);
         eventValues.put(XmlTemplateProvider.VAR_AGENT_TITLE, currentUserName);
         eventValues.put(XmlTemplateProvider.VAR_AGENT_ID_TYPE,
             Constants.PREMIS_ID_TYPE_ESCIDOC);
         eventValues.put(XmlTemplateProvider.VAR_AGENT_ID_VALUE, currentUserId);
         eventValues.put(XmlTemplateProvider.VAR_OBJECT_ID_TYPE,
             Constants.PREMIS_ID_TYPE_ESCIDOC);
         eventValues.put(XmlTemplateProvider.VAR_OBJECT_ID_VALUE, resourceId);
 
         return CommonFoXmlProvider.getInstance().getPremisEventXml(eventValues);
     }
 
     /**
      * Create comment for version update.
      * 
      * @param resource
      *            The versionable resource.
      * @param newStatus
      *            New status of resource.
      * @param versionComment
      *            Comment for version.
      * @return Comment
      */
     private String createComment(
         final VersionableResource resource, final String newStatus,
         final String versionComment) {
         String comment = versionComment;
         if (versionComment == null) {
             if (newStatus != null) {
                 comment = "Status changed to " + newStatus;
             }
             else {
                 comment = "New version created";
             }
             comment +=
                 " for " + resource.getClass().getSimpleName() + " "
                     + resource.getId() + ".";
         }
         return comment;
     }
 
     /**
      * @return id of current user.
      * @throws WebserverSystemException
      *             Thrown if current user is not set.
      */
     public String getCurrentUserId() throws WebserverSystemException {
         return getCurrentUser()[0];
     }
 
     /**
      * @return real name of current user.
      * @throws WebserverSystemException
      *             Thrown if current user is not set.
      */
     public String getCurrentUserRealName() throws WebserverSystemException {
         return getCurrentUser()[1];
     }
 
     /**
      * Get basic XML data from resource.
      * 
      * @param resource
      *            the resource.
      * @return Map with baseURL, xml namespace and prefix
      * @throws SystemException
      *             Thrown if anything fails.
      */
     private HashMap<String, String> getResourceBaseData(
         final VersionableResource resource) throws SystemException {
 
         HashMap<String, String> baseData = new HashMap<String, String>();
         if (resource instanceof Item) {
             baseData.put("resourceBaseUrl", Constants.ITEM_URL_BASE);
             baseData.put("resourcePrefix",
                 Constants.ITEM_PROPERTIES_NAMESPACE_PREFIX);
             baseData.put("resourceNamespace",
                 Constants.ITEM_PROPERTIES_NAMESPACE_URI);
         }
         else if (resource instanceof Container) {
             baseData.put("resourceBaseUrl", Constants.CONTAINER_URL_BASE);
             baseData.put("resourcePrefix",
                 Constants.CONTAINER_PROPERTIES_PREFIX);
             baseData.put("resourceNamespace",
                 Constants.CONTAINER_PROPERTIES_NAMESPACE_URI);
         }
         else if (resource instanceof ContentModel) {
             baseData.put("resourceBaseUrl", Constants.CONTENT_MODEL_URL_BASE);
             baseData.put("resourcePrefix",
                 Constants.CONTENT_MODEL_NAMESPACE_PREFIX);
             baseData.put("resourceNamespace",
                 Constants.CONTENT_MODEL_NAMESPACE_URI);
         }
         else if (resource instanceof Relation) {
             // FIXME resourceBaseUrl = Constants.CONTENT_RELATIONS_URL_BASE;
             baseData.put("resourcePrefix",
                 Constants.CONTENT_RELATIONS_NAMESPACE_PREFIX);
             baseData.put("resourceNamespace",
                 Constants.CONTENT_RELATIONS_NAMESPACE_URI);
         }
         else {
             throw new SystemException(
                 "'makeVersion' not supported for object-type '"
                     + resource.getClass().getName() + "'.");
         }
 
         return (baseData);
     }
 
     private void prependVersion(
         final VersionableResource resource, final String versionEntry)
         throws EncodingSystemException, FedoraSystemException,
         IntegritySystemException {
 
         // TODO insert new version in version-history
         try {
             String wov =
                 resource.getWov().toString(XmlUtility.CHARACTER_ENCODING);
             String newWov =
                 wov.replaceFirst("(<" + Constants.WOV_NAMESPACE_PREFIX
                     + ":version-history[^>]+>)", "$1" + versionEntry);
             Datastream ds =
                 new Datastream("version-history", resource.getId(), newWov
                     .getBytes(XmlUtility.CHARACTER_ENCODING), "text/xml");
             resource.setWov(ds);
         }
         catch (StreamNotFoundException e) {
             throw new IntegritySystemException(e);
         }
         catch (UnsupportedEncodingException e) {
             throw new EncodingSystemException(e);
         }
         catch (LockingException e) {
             // we just updated the item!
             throw new IntegritySystemException(e);
         }
         catch (SystemException e) {
             // FIXME remove SystemException from resource.setWov(ds)
             throw new IntegritySystemException(e);
         }
     }
 
     private void writeEvent(
         final VersionableResource resource, final String newEventEntry,
         final Map<String, StartElementWithChildElements> updateElementsWOV,
         final List<StartElementWithChildElements> elementsToAdd)
         throws WebserverSystemException {
 
         StaxParser sp = new StaxParser();
         ItemRelsExtUpdateHandler ireuh =
             new ItemRelsExtUpdateHandler(updateElementsWOV, sp);
         ireuh.setPath("/version-history/version/");
         sp.addHandler(ireuh);
         AddNewSubTreesToDatastream addNewSubtreesHandler =
             new AddNewSubTreesToDatastream("/version-history", sp);
         StartElement pointer =
             new StartElement("version", Constants.WOV_NAMESPACE_URI,
                 "escidocVersions", null);
         addNewSubtreesHandler.setPointerElement(pointer);
         addNewSubtreesHandler.setSubtreeToInsert(elementsToAdd);
         sp.addHandler(addNewSubtreesHandler);
 
         try {
             sp.parse(resource.getWov().getStream());
             sp.clearHandlerChain();
             ByteArrayOutputStream newWovStream =
                 addNewSubtreesHandler.getOutputStreams();
             // String debug = newWovStream.toString();
             String newWovString =
                 newWovStream
                     .toString(XmlUtility.CHARACTER_ENCODING).replaceFirst(
                         "(<" + Constants.WOV_NAMESPACE_PREFIX
                             + ":events[^>]*>)", "$1" + newEventEntry);
 
             resource.setWov(new Datastream(
                 Elements.ELEMENT_WOV_VERSION_HISTORY, resource.getId(),
                 newWovString.getBytes(XmlUtility.CHARACTER_ENCODING),
                 "text/xml"));
         }
         catch (Exception e) {
             throw new WebserverSystemException(e);
         }
 
     }
 
     /**
      * Adds and removes the provided elements to/from RELS-EXT.
      * 
      * @param addToRelsExt
      *            - Vector with elements, which should be added to RELS-EXT
      * @param deleteFromRelsExt
      *            - Map containing key-value pairs: keys - paths to elements,
      *            which should be deleted from RELS-EXT and values - elements
      *            thereself.
      * @param relsExtBytes
      *            - optional parameter: byte [] with content of RELS-EXT
      *            datastream. If no relsExtBytes provided (relsExtBytes is set
      *            to null), the method retrieves the RELS-EXT from Fedora
      * @param resource
      *            The resource which RELS-EXT is to alter.
      * @return byte [] RELS-EXT datastream content
      * @throws IntegritySystemException
      *             If the integrity of the repository is violated.
      * @throws XmlParserSystemException
      *             If parsing of xml data fails.
      * @throws WebserverSystemException
      *             In case of an internal error.
      * @throws FedoraSystemException
      *             If the Fedora reports an error
      */
     public static byte[] updateRelsExt(
         final List<StartElementWithChildElements> addToRelsExt,
         final Map<String, Vector<StartElementWithChildElements>> deleteFromRelsExt,
         final byte[] relsExtBytes, final FedoraResource resource,
         final Map<String, StartElementWithChildElements> updateProperties)
         throws IntegritySystemException, FedoraSystemException,
         XmlParserSystemException, WebserverSystemException {
 
         boolean updatedRelsExtProperties = false;
         byte[] relsExtContent = null;
         byte[] relsExtNewBytes = null;
         if (relsExtBytes == null) {
             try {
                 relsExtContent = resource.getRelsExt().getStream();
             }
             catch (StreamNotFoundException e1) {
                 throw new IntegritySystemException("Datastream not found.", e1);
             }
         }
         else {
             relsExtContent = relsExtBytes;
         }
         ByteArrayInputStream relsExtIs =
             new ByteArrayInputStream(relsExtContent);
 
         StaxParser sp = new StaxParser();
         if ((addToRelsExt != null) && (addToRelsExt.size() > 0)) {
             if ((updateProperties != null) && (updateProperties.size() > 0)) {
                 updatedRelsExtProperties = true;
                 ItemRelsExtUpdateHandler itemRelsExtUpdateHandler =
                     new ItemRelsExtUpdateHandler(updateProperties, sp);
                 sp.addHandler(itemRelsExtUpdateHandler);
 
             }
             AddNewSubTreesToDatastream addNewEntriesHandler =
                 new AddNewSubTreesToDatastream("/RDF", sp);
 
             StartElement pointer = new StartElement();
             pointer.setLocalName("Description");
             pointer.setPrefix(Constants.RDF_NAMESPACE_PREFIX);
             pointer.setNamespace(Constants.RDF_NAMESPACE_URI);
 
             addNewEntriesHandler.setPointerElement(pointer);
 
             addNewEntriesHandler.setSubtreeToInsert(addToRelsExt);
 
             sp.addHandler(addNewEntriesHandler);
 
             try {
                 sp.parse(relsExtIs);
             }
             catch (XMLStreamException e) {
                 log.error(e.getMessage());
                 throw new XmlParserSystemException(e.getMessage());
             }
             catch (NullPointerException e) {
                 throw new XmlParserSystemException(e);
             }
             catch (Exception e) {
                 throw new WebserverSystemException(e);
             }
             sp.clearHandlerChain();
             ByteArrayOutputStream relsExtNewStream =
                 addNewEntriesHandler.getOutputStreams();
             relsExtNewBytes = relsExtNewStream.toByteArray();
         }
 
         if ((deleteFromRelsExt != null) && (deleteFromRelsExt.size() > 0)) {
 
             if (relsExtNewBytes != null) {
                 relsExtIs = new ByteArrayInputStream(relsExtNewBytes);
             }
             if ((updateProperties != null) && (updateProperties.size() > 0)
                 && !updatedRelsExtProperties) {
                 updatedRelsExtProperties = true;
                 ItemRelsExtUpdateHandler itemRelsExtUpdateHandler =
                     new ItemRelsExtUpdateHandler(updateProperties, sp);
                 sp.addHandler(itemRelsExtUpdateHandler);
 
             }
             HashMap<String, String> extractPathes =
                 new HashMap<String, String>();
 
             extractPathes.put("/RDF", null);
 
             MultipleExtractor multipleExtractor =
                 new MultipleExtractor(extractPathes, sp);
 
             multipleExtractor.removeElements(deleteFromRelsExt);
             sp.addHandler(multipleExtractor);
 
             try {
                 sp.parse(relsExtIs);
             }
             catch (XMLStreamException e) {
                 log.error(e.getMessage());
                 throw new XmlParserSystemException(e.getMessage());
             }
             catch (NullPointerException e) {
                 throw new XmlParserSystemException(e);
             }
             catch (Exception e) {
                 throw new WebserverSystemException(e);
             }
             sp.clearHandlerChain();
             HashMap<String, Object> streams =
                 multipleExtractor.getOutputStreams();
             ByteArrayOutputStream relsExtNewStream =
                 (ByteArrayOutputStream) streams.get("RDF");
             relsExtNewBytes = relsExtNewStream.toByteArray();
         }
         if ((updateProperties != null) && (updateProperties.size() > 0)
             && !updatedRelsExtProperties) {
             ItemRelsExtUpdateHandler itemRelsExtUpdateHandler =
                 new ItemRelsExtUpdateHandler(updateProperties, sp);
             sp.addHandler(itemRelsExtUpdateHandler);
             HashMap<String, String> pathes = new HashMap<String, String>();
             pathes.put("/RDF", null);
             MultipleExtractor multipleExtractor =
                 new MultipleExtractor(pathes, sp);
             sp.addHandler(multipleExtractor);
             try {
                 sp.parse(relsExtIs);
             }
             catch (NullPointerException e) {
                 throw new XmlParserSystemException(e);
             }
             catch (XMLStreamException e) {
                 throw new XmlParserSystemException(e);
             }
 
             catch (LockingException e) {
                 throw new IntegritySystemException(e);
             }
             catch (Exception e) {
                 throw new WebserverSystemException("Unexpected Exception.", e);
             }
             sp.clearHandlerChain();
             HashMap<String, Object> streams =
                 multipleExtractor.getOutputStreams();
             ByteArrayOutputStream relsExtNewStream =
                 (ByteArrayOutputStream) streams.get("RDF");
             relsExtNewBytes = relsExtNewStream.toByteArray();
         }
         if (relsExtNewBytes != null) {
             return relsExtNewBytes;
         }
         else {
 
             return relsExtContent;
         }
 
     }
 
     /**
      * The method uses the staging file handler to create a new staging file
      * that stores the provided stream in to staging area. The URL to this
      * staging file is extracted from the XML representation of the staging file
      * and is returned as the redirect URL.
      * 
      * @param streamContent
      *            stream content
      * @param fileName
      *            file name, which will be included in returned redirect url
      * @param mimeType
      *            mime type
      * @return redirectUrl url
      * @throws FileSystemException
      *             In case of an internal error during storing the content.
      * @om
      */
     public String upload(
         final byte[] streamContent, final String fileName, final String mimeType)
         throws FileSystemException {
 
         EscidocBinaryContent content = new EscidocBinaryContent();
         content.setFileName(fileName);
         content.setMimeType(mimeType);
         content.setContent(new ByteArrayInputStream(streamContent));
         String stagingFileXml = null;
 
         try {
             stagingFileXml = stagingFileHandler.create(content);
         }
         catch (SystemException e) {
             throw new FileSystemException(e.getMessage(), e);
         }
         catch (Exception e) {
             throw new FileSystemException(
                 "Unexpected exception from StagingFileHandler.create", e);
         }
         Matcher matcher = REDIRECT_URL_PATTERN.matcher(stagingFileXml);
         if (matcher.find()) {
             String base = matcher.group(1);
             String path = matcher.group(2);
             if (base != null && path != null) {
                 return base + path;
             }
         }
         throw new FileSystemException(
             "Unparseable result from StagingFileHandler.create");
     }
 
     /**
      * Injects the staging file handler bean.
      * 
      * @param stagingFileHandler
      *            The stanging file handler bean to inject.
      * @spring.property ref="service.StagingFileHandlerBean"
      */
     public void setStagingFileHandler(
         final StagingFileHandlerInterface stagingFileHandler) {
 
         this.stagingFileHandler = stagingFileHandler;
     }
 
     /**
      * Get StagingFileHandlerBean.
      * 
      * @return the stagingFileHandler
      */
     public StagingFileHandlerInterface getStagingFileHandler() {
 
         return stagingFileHandler;
     }
 
     /**
      * Prepare the XML for the return of all task oriented methods.
      * 
      * TODO either use velocity template and/or move to an own class (ReturnXY)
      * 
      * @param lastModificationDate
      *            The last-modification-date of the resource.
      * @return The result XML structure.
      * @throws SystemException
      *             Thrown if parsing last modification or retrieving xml:base
      *             failed.
      */
     public String prepareReturnXml(final String lastModificationDate)
         throws SystemException {
 
         return prepareReturnXml(lastModificationDate, null);
     }
 
     /**
      * Prepare the XML for the return of all task oriented methods.
      * 
      * use method with DateTime as parameter
      * 
      * TODO either use velocity template and/or move to an own class (ReturnXY)
      * 
      * @param lastModificationDate
      *            The last-modification-date of the resource.
      * @param content
      *            The XML content elements of the result structure.
      * @return The result XML structure.
      * @throws SystemException
      *             Thrown if parsing last modification or retrieving xml:base
      *             failed.
      */
     @Deprecated
     public String prepareReturnXml(
         final String lastModificationDate, final String content)
         throws SystemException {
 
         DateTime t = new DateTime(lastModificationDate, DateTimeZone.UTC);
         String lmd = t.toString();
 
         String xml =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<result "
                 + "xmlns=\"" + Constants.RESULT_NAMESPACE_URI + "\" "
                 + "last-modification-date=\"" + lmd + "\"";
 
         if (content == null) {
             xml += " />";
         }
         else {
             xml += ">\n" + content + "</result>\n";
         }
 
         return xml;
     }
 
     /**
      * Prepare the XML for the return of all task oriented methods.
      * 
      * TODO either use velocity template and/or move to an own class (ReturnXY)
      * 
      * @param lastModificationDate
      *            The last-modification-date of the resource.
      * @param content
      *            The XML content elements of the result structure.
      * @return The result XML structure.
      * @throws SystemException
      *             Thrown if parsing last modification or retrieving xml:base
      *             failed.
      */
     public String prepareReturnXml(
         final DateTime lastModificationDate, final String content)
         throws SystemException {
 
         DateTime t = lastModificationDate;
         if (t == null) {
             t = new DateTime();
             t.withZone(DateTimeZone.UTC);
         }
 
         String xml =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<result "
                 + "xmlns=\"" + Constants.RESULT_NAMESPACE_URI + "\" "
                + "last-modification-date=\"" + t.toString() + "\"";
 
         if (content == null) {
             xml += " />";
         }
         else {
             xml += ">\n" + content + "</result>\n";
         }
 
         return xml;
     }
 
     /**
      * Makes a given URL full qualified by prepending eSciDoc base URL if
      * necessary. Returns null, if the IDs of an item and a component are given
      * and the URL refers to the content of that component of that item. Throws
      * an Exception if the URL is invalid in the given context; that means it is
      * of a form that is not allowed.
      * 
      * @param url
      *            A URL.
      * @param itemId
      *            The id of an Item or null.
      * @param componentId
      *            The id of a Component or null.
      * 
      * @return The full qualified URL or null if the URL refers to the content
      *         of the component, identified by componentId, of the item,
      *         identified by itemId.
      * 
      * @throws InvalidContentException
      *             If the String given as url is not a URI or invalid in the
      *             current context.
      * @throws WebserverSystemException
      *             If an error occurs.
      */
     public static String processUrl(
         final String url, final String itemId, final String componentId)
         throws InvalidContentException, WebserverSystemException {
         String escidocBaseUrl = XmlUtility.getEscidocBaseUrl();
 
         try {
             // // FIXME workaround issue 631
             // String fedoraBaseUrl =
             // FedoraUtility.getInstance().getFedoraUrl() + "/get";
             // if (url.startsWith(fedoraBaseUrl)
             // || url.startsWith(fedoraBaseUrl.replaceFirst("localhost",
             // "127.0.0.1"))) {
             // throw new InvalidContentException(
             // "Direct access to Fedora denied: '" + url + "'.");
             // }
             // given
             URI local;
             URI fq;
             if (url.charAt(0) == '/') {
                 checkESciDocLocalURL(url);
                 local = new URI(url);
                 fq = new URI(escidocBaseUrl + url);
             }
             else {
                 local = new URI(url.replaceFirst("http[s]?://[^/]+", ""));
                 // if (url.startsWith(escidocBaseUrl)) {
                 // checkESciDocLocalURL(local);
                 // }
                 fq = new URI(url);
             }
 
             if (itemId != null && componentId != null) {
                 // expected
                 String thisUrl =
                     Constants.ITEM_URL_BASE
                         + itemId
                         + de.escidoc.core.common.business.fedora.Constants.COMPONENT_URL_PART
                         + componentId
                         + de.escidoc.core.common.business.fedora.Constants.COMPONENT_CONTENT_URL_PART;
                 URI thisLocal = new URI(thisUrl);
                 URI thisFq = new URI(escidocBaseUrl + thisUrl);
 
                 // recognize the URL we send
                 if (local.equals(thisLocal) || fq.equals(thisFq)) {
                     return null;
                 }
             }
             return fq.toString();
         }
         catch (URISyntaxException e) {
             throw new InvalidContentException("No valid URL.", e);
         }
         // catch (FedoraSystemException e) {
         // throw new WebserverSystemException(e);
         // }
     }
 
     /**
      * Get number of build from escidoc.properties.
      * 
      * @return build number
      * @throws WebserverSystemException
      *             Thrown if obtaining from properties failed.
      */
     public String getBuildNumber() throws WebserverSystemException {
         String buildNumber = null;
         try {
             buildNumber =
                 EscidocConfiguration.getInstance().get(
                     EscidocConfiguration.BUILD_NUMBER);
         }
         catch (Exception e) {
             String errorMsg =
                 "Failed to retrieve configuration parameter "
                     + EscidocConfiguration.FEDORA_URL;
             log.error(errorMsg, e);
             throw new WebserverSystemException(errorMsg, e);
         }
         return buildNumber;
     }
 
     /**
      * Check if an URL is an URL to the framework itself.
      * 
      * @param url
      *            URL as String.
      * @throws InvalidContentException
      *             Thrown if URL points not to the framework itself.
      */
     private static void checkESciDocLocalURL(final String url)
         throws InvalidContentException {
         if (!(url.startsWith("/ir/") || url.startsWith("/st/"))) {
             String msg = "The local URL '"+ url+
                     "' does not point into an eSciDoc Core component.";
             log.debug(msg);
             throw new InvalidContentException(msg);
         }
 
     }
 
 }
