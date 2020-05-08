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
 package de.escidoc.core.om.business.renderer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.ISODateTimeFormat;
 
 import de.escidoc.core.aa.service.interfaces.PolicyDecisionPointInterface;
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.Container;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.service.BeanLocator;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ContainerXmlProvider;
 import de.escidoc.core.common.util.xml.factory.MetadataRecordsXmlProvider;
 import de.escidoc.core.common.util.xml.factory.RelationsXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
 import de.escidoc.core.om.business.fedora.container.FedoraContainerHandler;
 import de.escidoc.core.om.business.fedora.item.FedoraItemHandler;
 import de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface;
 import de.escidoc.core.om.business.security.UserFilter;
 
 /**
  * Render XML representations of a Container.
  * 
  * 
  */
 public class VelocityXmlContainerRenderer implements ContainerRendererInterface {
 
     private static AppLogger log =
         new AppLogger(VelocityXmlContainerRenderer.class.getName());
 
     private static final int THREE = 3;
 
     private final VelocityXmlCommonRenderer commonRenderer =
         new VelocityXmlCommonRenderer();
 
     /** The policy decision point used to check access privileges. */
     private PolicyDecisionPointInterface pdp;
 
     private TripleStoreUtility tsu;
 
     /**
      * Gets the {@link PolicyDecisionPointInterface} implementation.
      * 
      * @return PolicyDecisionPointInterface
      */
     protected PolicyDecisionPointInterface getPdp() {
 
         return pdp;
     }
 
     /**
      * Injects the {@link PolicyDecisionPointInterface} implementation.
      * 
      * @param pdp
      *            the {@link PolicyDecisionPointInterface} to be injected.
      * @spring.property ref="service.PolicyDecisionPointBean"
      */
     public void setPdp(final PolicyDecisionPointInterface pdp) {
 
         this.pdp = pdp;
     }
 
     /**
      * Injects the triple store utility bean.
      * 
      * @param tsu
      *            The {@link TripleStoreUtility}.
      * @spring.property ref="business.TripleStoreUtility"
      * @aa
      */
     public void setTsu(final TripleStoreUtility tsu) {
         this.tsu = tsu;
     }
 
     /**
      * Constructor. Initialize Spring Beans.
      * 
      */
     public VelocityXmlContainerRenderer() throws WebserverSystemException {
         tsu = BeanLocator.locateTripleStoreUtility();
         pdp = BeanLocator.locatePolicyDecisionPoint();
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param container
      *            Container
      * @return XML Container representation.
      * 
      * @throws SystemException
      *             If an error occurs.
      * 
      * @see de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface#render(de.escidoc.core.om.business.fedora.container.FedoraContainerHandler)
      */
     public String render(final Container container) throws SystemException {
 
         // Container container = containerHandler.getContainer();
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("containerTitle", container.getTitle());
         values.put("containerHref", container.getHref());
         values.put("containerId", container.getId());
 
         addPropertiesValus(values, container);
         // addOrganizationDetailsValues(organizationalUnit, values);
         addResourcesValues(container, values);
 
         addStructMapValus(container, values);
         addMdRecordsValues(container, values);
         Vector<HashMap<String, String>> relations = container.getRelations();
         commonRenderer.addRelationsValues(relations, container.getHref(),
             values);
         commonRenderer.addRelationsNamespaceValues(values);
         values.put("contentRelationsTitle", "Relations of Container");
 
         return ContainerXmlProvider.getInstance().getContainerXml(values);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface
      * #
      * renderProperties(de.escidoc.core.common.business.fedora.resources.Container
      * )
      */
     public String renderProperties(final Container container)
         throws WebserverSystemException, SystemException {
 
         String result = null;
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("isRootProperties", XmlTemplateProvider.TRUE);
         addPropertiesValus(values, container);
         result = ContainerXmlProvider.getInstance().getPropertiesXml(values);
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface
      * #
      * renderResources(de.escidoc.core.common.business.fedora.resources.Container
      * )
      */
     public String renderResources(final Container container)
         throws WebserverSystemException {
         String result = null;
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("isRootResources", XmlTemplateProvider.TRUE);
         addResourcesValues(container, values);
         result = ContainerXmlProvider.getInstance().getResourcesXml(values);
         return result;
     }
 
     /**
      * Gets the representation of the sub resource <code>relations</code> of an
      * item/container.
      * 
      * @param container
      *            The Container.
      * @return Returns the XML representation of the sub resource
      *         <code>ou-parents</code> of an organizational unit.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     public String renderRelations(final Container container)
         throws WebserverSystemException, SystemException {
 
         String result = null;
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         // addNamespaceValues(values);
         values.put("isRootRelations", XmlTemplateProvider.TRUE);
 
         commonRenderer.addRelationsValues(container.getRelations(), container
             .getHref(), values);
         values.put("contentRelationsTitle", "Relations of Container");
         commonRenderer.addRelationsNamespaceValues(values);
         result = RelationsXmlProvider.getInstance().getRelationsXml(values);
         return result;
     }
 
     /**
      * Gets the representation of the virtual resource <code>parents</code> of
      * an item/container.
      * 
      * @param container
      *            The Container.
      * @return Returns the XML representation of the virtual resource
      *         <code>parents</code> of an container.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     public String renderParents(final String containerId)
         throws SystemException {
 
         String result = null;
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addXlinkValues(values);
         commonRenderer.addStructuralRelationsValues(values);
         values.put(XmlTemplateProvider.VAR_LAST_MODIFICATION_DATE,
             ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).print(
                 System.currentTimeMillis()));
         values.put("isRootParents", XmlTemplateProvider.TRUE);
         addParentsValues(containerId, values);
         commonRenderer.addParentsNamespaceValues(values);
         result = ContainerXmlProvider.getInstance().getParentsXml(values);
         return result;
     }
 
     /**
      * Adds the parents values to the provided map.
      * 
      * @param container
      *            The container for that data shall be created.
      * @param values
      *            The map to add values to.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private void addParentsValues(
         String containerId, final Map<String, Object> values)
         throws SystemException {
         values.put("parentsHref", XmlUtility.getContainerParentsHref(XmlUtility
             .getContainerHref(containerId)));
         values.put("parentsTitle", "parents of container " + containerId);
         final StringBuffer query =
             tsu
                 .getRetrieveSelectClause(true, TripleStoreUtility.PROP_MEMBER)
                 .append(
                     tsu.getRetrieveWhereClause(true,
                         TripleStoreUtility.PROP_MEMBER, containerId, null,
                         null, null));
         List<String> ids = new ArrayList<String>();
         try {
             ids = tsu.retrieve(query.toString());
         }
         catch (TripleStoreSystemException e) {
         }
 
         Iterator<String> idIter = ids.iterator();
         List<Map<String, String>> entries =
             new Vector<Map<String, String>>(ids.size());
         while (idIter.hasNext()) {
             Map<String, String> entry = new HashMap<String, String>(THREE);
             String id = idIter.next();
             entry.put("id", id);
             entry.put("href", XmlUtility.getContainerHref(id));
             entry.put("title", tsu.getTitle(id));
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProvider.VAR_PARENTS, entries);
         }
     }
 
     /**
      * Adds values for namespace declaration.
      * 
      * @param values
      *            Already added values.
      * 
      * @throws WebserverSystemException
      *             If an error occurs.
      */
     private void addNamespaceValues(final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put("containerNamespacePrefix",
             Constants.CONTAINER_NAMESPACE_PREFIX);
         values.put("containerNamespace", Constants.CONTAINER_NAMESPACE_URI);
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS_PREFIX,
             Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS,
             Constants.PROPERTIES_NS_URI);
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
             Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS,
             Constants.STRUCTURAL_RELATIONS_NS_URI);
         values.put("versionNamespacePrefix", Constants.VERSION_NS_PREFIX);
         values.put("versionNamespace", Constants.VERSION_NS_URI);
         values.put("releaseNamespacePrefix", Constants.RELEASE_NS_PREFIX);
         values.put("releaseNamespace", Constants.RELEASE_NS_URI);
         values.put("structmapNamespacePrefix", Constants.STRUCT_MAP_PREFIX);
         values.put("structmapNamespace", Constants.STRUCT_MAP_NAMESPACE_URI);
 
     }
 
     /**
      * Adds the properties values to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @param container
      *            The Container.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private void addPropertiesValus(
         final Map<String, Object> values, final Container container)
         throws SystemException {
 
         Map<String, String> properties = container.getResourceProperties();
         String id = container.getId();
         values.put(XmlTemplateProvider.VAR_PROPERTIES_TITLE, "Properties");
         values.put(XmlTemplateProvider.VAR_PROPERTIES_HREF, XmlUtility
             .getContainerPropertiesHref(container.getHref()));
         // status
         values.put("containerStatus", container.getStatus());
         values.put("containerCreationDate", container.getCreationDate());
         values.put(XmlTemplateProvider.VAR_CONTAINER_STATUS_COMMENT, XmlUtility
             .escapeForbiddenXmlCharacters(properties
                 .get(PropertyMapKeys.PUBLIC_STATUS_COMMENT)));
         // name
         values.put("containerName", container.getTitle());
         // description
         String description = container.getDescription();
         if (description != null) {
             values.put("containerDescription",
                 PropertyMapKeys.CURRENT_VERSION_DESCRIPTION);
         }
 
         // context
         values.put("containerContextId", properties
             .get(PropertyMapKeys.CURRENT_VERSION_CONTEXT_ID));
         values.put("containerContextHref", Constants.CONTEXT_URL_BASE
             + properties.get(PropertyMapKeys.CURRENT_VERSION_CONTEXT_ID));
         values.put("containerContextTitle", properties
             .get(PropertyMapKeys.CURRENT_VERSION_CONTEXT_TITLE));
         // content model
         String contentModelId =
             properties.get(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID);
         values.put("containerContentModelId", contentModelId);
         values.put("containerContentModelHref", XmlUtility
             .getContentModelHref(contentModelId));
         values.put("containerContentModelTitle", properties
             .get(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_TITLE));
 
         // created-by -----------
         String createdById = properties.get(PropertyMapKeys.CREATED_BY_ID);
         values.put("containerCreatedById", createdById);
         values.put("containerCreatedByHref", XmlUtility
             .getUserAccountHref(createdById));
         values.put("containerCreatedByTitle", properties
             .get(PropertyMapKeys.CREATED_BY_TITLE));
 
         // lock -status, -owner, -date
         if (container.isLocked()) {
             values.put("containerLocked", XmlTemplateProvider.TRUE);
             String lockOwnerId = container.getLockOwner();
             values.put("containerLockStatus", "locked");
             values.put("containerLockDate", container.getLockDate());
             values.put("containerLockOwnerHref", XmlUtility
                 .getUserAccountHref(lockOwnerId));
             values.put("containerLockOwnerId", lockOwnerId);
             values
                 .put("containerLockOwnerTitle", container.getLockOwnerTitle());
         }
         else {
             values.put("containerLocked", XmlTemplateProvider.FALSE);
             values.put("containerLockStatus", "unlocked");
         }
 
         final String currentVersionId = container.getFullId();
         // final StringBuffer versionIdBase =
         // new StringBuffer(container.getId()).append(":");
 
         String latestVersionNumber =
             properties.get(PropertyMapKeys.LATEST_VERSION_NUMBER);
         String curVersionNumber = container.getVersionId();
         if (curVersionNumber == null) {
             curVersionNumber = latestVersionNumber;
         }
 
         // pid ---------------
         String pid = container.getObjectPid();
         if ((pid != null) && (pid.length() > 0)) {
             values.put("containerPid", pid);
         }
         // current version
         values.put("containerCurrentVersionHref", container.getVersionHref());
         values.put("containerCurrentVersionTitle", "current version");
         values.put("containerCurrentVersionId", currentVersionId);
         values.put("containerCurrentVersionNumber", curVersionNumber);
         values.put("containerCurrentVersionComment", XmlUtility
             .escapeForbiddenXmlCharacters(properties
                 .get(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT)));
 
         // modified by
 
         String modifiedById =
             properties.get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID);
         values.put("containerCurrentVersionModifiedByTitle", properties
             .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));
         values.put("containerCurrentVersionModifiedByHref", XmlUtility
             .getUserAccountHref(modifiedById));
         values.put("containerCurrentVersionModifiedById", modifiedById);
 
         String versionPid = container.getVersionPid();
         // container
         // .getVersionData().get(Elements.ELEMENT_WOV_VERSION_PID);
         if ((versionPid != null) && !versionPid.equals("")) {
             values.put("containerCurrentVersionPID", versionPid);
         }
         values.put("containerCurrentVersionDate", container.getVersionDate());
 
         if (curVersionNumber.equals(latestVersionNumber)) {
             String latestVersionStatus =
                 properties.get(PropertyMapKeys.LATEST_VERSION_VERSION_STATUS);
             values.put("containerCurrentVersionStatus", latestVersionStatus);
 
             if (latestVersionStatus.equals(Constants.STATUS_RELEASED)) {
                 String latestReleasePid =
                     container.getResourceProperties().get(
                         PropertyMapKeys.LATEST_RELEASE_PID);
                 if ((latestReleasePid != null) && !latestReleasePid.equals("")) {
                     values.put("containerCurrentVersionPID", latestReleasePid);
                 }
             }
 
             // values.put("containerCurrentVersionValidStatus", container
             // .getLastVersionData().get(
             // TripleStoreUtility.PROP_LATEST_VERSION_VALID_STATUS));
 
         }
         else {
             values.put("containerCurrentVersionStatus", container
                 .getResourceProperties().get(
                     PropertyMapKeys.CURRENT_VERSION_STATUS));
 
             // values.put("containerCurrentVersionValidStatus", container
             // .getVersionData()
             // .get(Elements.ELEMENT_WOV_VERSION_VALID_STATUS));
 
             values.put("containerCurrentVersionComment", XmlUtility
                 .escapeForbiddenXmlCharacters(container
                     .getResourceProperties().get(
                         PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT)));
             values.put("containerCurrentVersionModifiedById", container
                 .getResourceProperties().get(
                     PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
 
             values.put("containerCurrentVersionModifiedByHref", container
                 .getResourceProperties().get(
                     PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_HREF));
 
             values.put("containerCurrentVersionModifiedByTitle", properties
                 .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));
         }
 
         // latest version
         values.put("containerLatestVersionHref", container
             .getLatestVersionHref());
         values.put("containerLatestVersionId", container.getLatestVersionId());
         values.put("containerLatestVersionTitle", "latest version");
         values.put("containerLatestVersionDate", properties
             .get(PropertyMapKeys.LATEST_VERSION_DATE));
         values.put("containerLatestVersionNumber", latestVersionNumber);
         // latest release
         String containerStatus = container.getStatus();
         if (containerStatus.equals(Constants.STATUS_RELEASED)
             || containerStatus.equals(Constants.STATUS_WITHDRAWN)) {
            values.put("containerLatestReleaseHref", container.getHrefWithoutVersionNumber()
                 + ":"
                 + container.getResourceProperties().get(
                     PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
 
             values.put("containerLatestReleaseId", id
                 + ":"
                 + container.getResourceProperties().get(
                     PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
             values.put("containerLatestReleaseTitle", "latest release");
             values.put("containerLatestReleaseNumber", container
                 .getResourceProperties().get(
                     PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
             values.put("containerLatestReleaseDate", container
                 .getResourceProperties().get(
                     PropertyMapKeys.LATEST_RELEASE_VERSION_DATE));
             String latestReleasePid =
                 container.getResourceProperties().get(
                     PropertyMapKeys.LATEST_RELEASE_PID);
             if ((latestReleasePid != null) && !latestReleasePid.equals("")) {
                 values.put("containerLatestReleasePid", latestReleasePid);
             }
         }
         // content model specific
         try {
             Datastream cmsDs = container.getCts();
             String xml = cmsDs.toStringUTF8();
             values.put(XmlTemplateProvider.CONTAINER_CONTENT_MODEL_SPECIFIC,
                 xml);
         }
         catch (StreamNotFoundException e) {
             throw new IntegritySystemException("Can not get stream '"
                 + Elements.ELEMENT_CONTENT_MODEL_SPECIFIC + "'.", e);
         }
 
     }
 
     /**
      * Adds the struct-map values to the provided map.
      * 
      * @param container
      *            The Container.
      * @param values
      *            The map to add values to.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addStructMapValus(
         final Container container, final Map<String, Object> values)
         throws SystemException {
 
         values.put("structMapTitle", "StructMap of Container");
         values.put("structMapHref", container.getHref() + "/struct-map");
 
         // values.put("structmapNamespacePrefix",
         // Constants.STRUCT_MAP_PREFIX);
         // values.put("structmapNamespace",
         // Constants.STRUCT_MAP_NAMESPACE_URI);
         try {
             addMemberRefs(container, null, values);
         }
         catch (MissingMethodParameterException e) {
             throw new WebserverSystemException(e);
         }
     }
 
     /**
      * 
      * @param containerHandler
      * @param itemHandler
      * @param filter
      * @param values
      * @throws SystemException
      * @throws MissingMethodParameterException
      * @throws AuthorizationException
      *             Thrown if access to origin Item is restricted.
      */
     private void addMembers(
         final FedoraContainerHandler containerHandler,
         final FedoraItemHandler itemHandler, final String filter,
         final Map<String, Object> values) throws SystemException,
         MissingMethodParameterException, AuthorizationException {
 
         values.put("memberListTitle", "list of members");
         values.put("memberListNamespacePrefix", Constants.MEMBER_LIST_PREFIX);
         values.put("memberListNamespace", Constants.MEMBER_LIST_NAMESPACE_URI);
         List<String> ids = null;
 
         ids = containerHandler.getMemberRefsList(filter);
 
         Iterator<String> idIter = ids.iterator();
         List<String> entries = new Vector<String>(ids.size());
         while (idIter.hasNext()) {
             String id = idIter.next();
             String objectType =
                 TripleStoreUtility.getInstance().getObjectType(id);
             try {
                 if (Constants.CONTAINER_OBJECT_TYPE.equals(objectType)) {
                     entries.add(containerHandler.retrieve(id));
                 }
                 else if (Constants.ITEM_OBJECT_TYPE.equals(objectType)) {
                     entries.add(itemHandler.retrieve(id));
                 }
                 else {
                     String msg =
                         "FedoraContainerHandler.getMembers: can not return"
                             + " object with unknown type: " + id
                             + ". Write comment.";
                     entries.add("<!-- " + msg + " -->");
                     log.error(msg);
                 }
             }
             catch (MissingMethodParameterException e) {
                 throw new WebserverSystemException("can not occure");
             }
             catch (ResourceNotFoundException e) {
                 String msg =
                     "FedoraContainerHandler.getMembers: can not retrieve object "
                         + id + ". ResourceNotFoundException: " + e.getCause()
                         + ". ";
                 entries.add("<!-- " + msg + " -->");
 
                 log.error(msg);
             }
         }
 
         if (!entries.isEmpty()) {
             values.put("containerMembers", entries);
         }
     }
 
     /**
      * 
      * @param container
      * @param filter
      * @param values
      * @throws SystemException
      * @throws MissingMethodParameterException
      */
     private void addMemberRefs(
         final Container container, final String filter,
         final Map<String, Object> values) throws SystemException,
         MissingMethodParameterException {
 
         UserFilter ufilter = new UserFilter();
 
         List<String> ids = ufilter.getMemberRefList(container, filter);
         Iterator<String> idIter = ids.iterator();
         List<Map<String, String>> items = new Vector<Map<String, String>>();
         List<Map<String, String>> containers =
             new Vector<Map<String, String>>();
 
         while (idIter.hasNext()) {
             Map<String, String> entry = new HashMap<String, String>(3);
             String id = idIter.next();
             String objectType =
                 TripleStoreUtility.getInstance().getObjectType(id);
             if ((Constants.ITEM_OBJECT_TYPE.equals(objectType) || Constants.CONTAINER_OBJECT_TYPE
                 .equals(objectType))) {
                 entry.put("memberId", id);
                 entry.put("memberTitle", TripleStoreUtility
                     .getInstance().getTitle(id));
                 if (objectType.equals(Constants.ITEM_OBJECT_TYPE)) {
 
                     items.add(entry);
                     entry.put("memberHref", XmlUtility.BASE_OM + "item/" + id);
                     entry.put("elementName", "item-ref");
                 }
                 else {
 
                     containers.add(entry);
                     entry.put("memberHref", XmlUtility.BASE_OM + "container/"
                         + id);
                     entry.put("elementName", "container-ref");
                 }
 
             }
             else {
                 String msg =
                     "FedoraContainerHandler.getMemberRefs: can not "
                         + "write member entry to struct-map for "
                         + "object with unknown type: " + id + ".";
                 log.error(msg);
                 // throw new IntegritySystemException(msg);
             }
 
         }
         if (!items.isEmpty()) {
             values.put("items", items);
         }
         if (!containers.isEmpty()) {
             values.put("containers", containers);
         }
     }
 
     /**
      * 
      * @param filter
      * @param values
      * @throws SystemException
      * @throws MissingMethodParameterException
      */
     private void addContainerRefs(
         final String filterXml, final String whereClause,
         final Map<String, Object> values) throws SystemException,
         MissingMethodParameterException {
 
         values.put("containerRefListNamespacePrefix",
             Constants.CONTAINER_REF_LIST_PREFIX);
         values.put("containerRefListNamespace",
             Constants.CONTAINER_REF_LIST_NAMESPACE);
         values.put("containerRefListTitle", "list of container references");
 
         Map<String, Object> filterMap = null;
         if (filterXml != null) {
 
             try {
                 filterMap =
                     XmlUtility.getFilterMap(Constants.DC_IDENTIFIER_URI,
                         filterXml);
             }
             catch (final Exception e) {
                 throw new XmlParserSystemException("While parse param filter.",
                     e);
             }
         }
 
         List<String> ids = null;
 
         ids =
             TripleStoreUtility.getInstance().evaluate(
                 Constants.CONTAINER_OBJECT_TYPE, filterMap, null, whereClause);
 
         Iterator<String> idIter = ids.iterator();
         List<Map<String, String>> entries =
             new Vector<Map<String, String>>(ids.size());
         while (idIter.hasNext()) {
             Map<String, String> entry = new HashMap<String, String>(3);
             String id = idIter.next();
             entry.put("containerId", id);
             entry.put("containerTitle", TripleStoreUtility
                 .getInstance().getTitle(id));
             entry.put("containerHref", XmlUtility.getContainerHref(id));
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put("containerRefs", entries);
         }
 
     }
 
     /**
      * Adds values for list of containers.
      * 
      * @param filter
      *            The filter to constrain list of containers.
      * @param values
      *            Already added values.
      * @param containerHandler
      *            Reference to ContainerHandler.
      * @throws SystemException
      *             If an error occurs.
      * @throws MissingMethodParameterException
      *             If a parameter is missing.
      */
     private void addContainers(
         final String filterXml, final String whereClause,
         final Map<String, Object> values,
         final FedoraContainerHandler containerHandler) throws SystemException,
         MissingMethodParameterException {
 
         values.put("containerListTitle", "list of containers");
         values.put("containerListNamespacePrefix",
             Constants.CONTAINER_LIST_PREFIX);
         values.put("containerListNamespace",
             Constants.CONTAINER_LIST_NAMESPACE_URI);
 
         Map<String, Object> filter = XmlUtility.getFilterMap(filterXml);
         String userCriteria = (String) filter.get("user");
         String roleCriteria = (String) filter.get("role");
 
         List<String> list =
             TripleStoreUtility.getInstance().evaluate(
                 Constants.CONTAINER_OBJECT_TYPE, filter, null, whereClause);
 
         UserFilter ufilter = new UserFilter();
         List<String> containerIds =
             ufilter.filterUserRole("container", roleCriteria, userCriteria,
                 list);
 
         Iterator<String> idIter = containerIds.iterator();
         List<String> entries = new Vector<String>(containerIds.size());
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 // entries.add(BeanLocator.locateContainerHandler().retrieve(id));
                 entries.add(containerHandler.retrieve(id));
             }
             catch (MissingMethodParameterException e) {
                 throw new WebserverSystemException("can not occure");
             }
             catch (ContainerNotFoundException e) {
                 String msg =
                     "FedoraContainerHandler.retrieveContainers: can not"
                         + " retrieve container " + id
                         + ". ContainerNotFoundException: " + e.getCause() + ".";
                 entries.add("<!-- " + msg + " -->");
                 log.error(msg);
             }
         }
 
         if (!entries.isEmpty()) {
             values.put("containers", entries);
         }
 
     }
 
     /**
      * Adds the resource values to the provided map.
      * 
      * @param container
      *            The Container for that data shall be created.
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             If an error occurs.
      * @oum
      */
     private void addResourcesValues(
         final Container container, final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put(XmlTemplateProvider.RESOURCES_TITLE, "Resources");
         values.put("resourcesHref", XmlUtility
             .getContainerResourcesHref(container.getHref()));
         values.put("membersHref", container.getHref() + "/resources/members");
         values.put("membersTitle", "Members ");
         values.put("versionHistoryTitle", "Version History");
         values.put("versionHistoryHref", XmlUtility
             .getContainerResourcesHref(container.getHref())
             + "/" + Elements.ELEMENT_RESOURCES_VERSION_HISTORY);
 
         // add operations from Fedora service definitions
         // FIXME use container properties instead of triplestore util
         try {
             values.put("resourceOperationNames", TripleStoreUtility
                 .getInstance().getMethodNames(container.getId()));
         }
         catch (TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
 
     }
 
     /**
      * Adds values for metadata XML of the container.
      * 
      * @param container
      *            The container object.
      * @param values
      *            Already added values.
      * @throws EncodingSystemException
      *             If an encoding error occurs.
      * @throws FedoraSystemException
      *             If Fedora throws an exception.
      * @throws WebserverSystemException
      *             If an error occurs.
      * @throws IntegritySystemException
      *             If the repository integrity is violated.
      */
     private void addMdRecordsValues(
         final Container container, final Map<String, Object> values)
         throws EncodingSystemException, FedoraSystemException,
         WebserverSystemException, IntegritySystemException {
 
         values.put(XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.MD_RECORDS_NAMESPACE,
             Constants.METADATARECORDS_NAMESPACE_URI);
         values.put("mdRecordsHref", XmlUtility
             .getContainerMdRecordsHref(container.getHref()));
         values.put("mdRecordsTitle", "Metadata Records of Container "
             + container.getId());
 
         HashMap<String, Datastream> mdRecords =
             (HashMap<String, Datastream>) container.getMdRecords();
         Collection<Datastream> mdRecordsDatastreams = mdRecords.values();
         Iterator<Datastream> it = mdRecordsDatastreams.iterator();
         StringBuffer content = new StringBuffer();
         while (it.hasNext()) {
             Datastream mdRecord = (Datastream) it.next();
             String md = renderMetadataRecord(container, mdRecord, false);
             content.append(md);
         }
         values.put("mdRecordsContent", content.toString());
 
     }
 
     /**
      * Renders a single meta data record to XML representation.
      * 
      * @param container
      *            The Container.
      * @param mdRecord
      *            The to render md record.
      * @param isRootMdRecord
      *            Set true if md-record is to render with root elements.
      * @throws EncodingSystemException
      *             If an encoding error occurs.
      * @throws FedoraSystemException
      *             If Fedora throws an exception.
      * @throws WebserverSystemException
      *             If an error occurs.
      * 
      * @return Returns the XML representation of the metadata records.
      */
     public String renderMetadataRecord(
         final Container container, final Datastream mdRecord,
         final boolean isRootMdRecord) throws EncodingSystemException,
         FedoraSystemException, WebserverSystemException {
 
         if (mdRecord.isDeleted()) {
             return "";
         }
 
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         values.put("mdRecordHref", XmlUtility
             .getContainerMdRecordsHref(container.getHref())
             + "/md-record/" + mdRecord.getName());
         values.put(XmlTemplateProvider.MD_RECORD_NAME, mdRecord.getName());
         values.put("mdRecordTitle", mdRecord.getName());
         values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD, isRootMdRecord);
         values.put(XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.MD_RECORDS_NAMESPACE,
             Constants.METADATARECORDS_NAMESPACE_URI);
         String mdRecordContent;
         mdRecordContent = mdRecord.toStringUTF8();
         values.put(XmlTemplateProvider.MD_RECORD_CONTENT, mdRecordContent);
         Vector<String> altIds = mdRecord.getAlternateIDs();
         if (!altIds.get(1).equals("unknown")) {
             values.put(XmlTemplateProvider.MD_RECORD_TYPE, altIds.get(1));
         }
         if (!altIds.get(2).equals("unknown")) {
             values.put(XmlTemplateProvider.MD_RECORD_SCHEMA, altIds.get(2));
         }
 
         return MetadataRecordsXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     /**
      * @param container
      *            The Container.
      * @throws EncodingSystemException
      *             If an encoding error occurs.
      * @throws FedoraSystemException
      *             If Fedora throws an exception.
      * @throws WebserverSystemException
      *             If an error occurs.
      * @throws IntegritySystemException
      *             If the repository integrity is violated.
      * 
      * @return Returns the XML representation of the metadata records.
      */
     public String renderMetadataRecords(final Container container)
         throws EncodingSystemException, FedoraSystemException,
         WebserverSystemException, IntegritySystemException {
 
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
             XmlTemplateProvider.TRUE);
         addMdRecordsValues(container, values);
 
         return MetadataRecordsXmlProvider.getInstance().getMdRecordsXml(values);
     }
 
     /**
      * Gets the representation of the virtual sub resource
      * <code>struct-map</code> of an organizational unit.
      * 
      * @param container
      *            The Container.
      * @return Returns the XML representation of the virtual sub resource
      *         <code>children</code> of an organizational unit.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     public String renderStructMap(final Container container)
         throws SystemException {
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("isRootStructMap", XmlTemplateProvider.TRUE);
         values.put("isSrelNeeded", XmlTemplateProvider.TRUE);
         addStructMapValus(container, values);
 
         return ContainerXmlProvider.getInstance().getStructMapXml(values);
 
     }
 
     /**
      * 
      * @throws AuthorizationException
      *             Thrown if access to origin Item is restricted.
      */
     public String renderMembers(
         final FedoraContainerHandler containerHandler,
         final FedoraItemHandler itemHandler, final String filter)
         throws SystemException, MissingMethodParameterException,
         AuthorizationException {
 
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addXlinkValues(values);
         addMembers(containerHandler, itemHandler, filter, values);
 
         return ContainerXmlProvider.getInstance().getMembersXml(values);
 
     }
 
     /**
      * 
      */
     public String renderMemberRefs(
         final Container container, final String filter) throws SystemException,
         MissingMethodParameterException {
 
         String result = null;
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addXlinkValues(values);
         values.put("isRootMemberRefs", XmlTemplateProvider.TRUE);
         values.put("memberRefListNamespacePrefix",
             Constants.MEMBER_REF_LIST_PREFIX);
         values.put("memberRefListNamespace",
             Constants.MEMBER_REF_LIST_NAMESPACE_URI);
         addMemberRefs(container, filter, values);
         result = ContainerXmlProvider.getInstance().getMemberRefsXml(values);
         return result;
 
     }
 
     /**
      * @param filter
      *            TODO
      * @return XML representation of Container reference
      */
     public String renderContainerRefs(final String filterXml)
         throws SystemException, MissingMethodParameterException {
 
         String result = null;
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addXlinkValues(values);
 
         Map<String, Object> filter = XmlUtility.getFilterMap(filterXml);
 
         String userCriteria = null;
         String roleCriteria = null;
         String whereClause = null;
         if (filter != null) {
             // filter out user permissions
             userCriteria = (String) filter.get("user");
             roleCriteria = (String) filter.get("role");
 
             try {
                 whereClause =
                     getPdp().getRoleUserWhereClause("container", userCriteria,
                         roleCriteria).toString();
             }
             catch (final SystemException e) {
                 // FIXME: throw SystemException?
                 throw new TripleStoreSystemException(
                     "Failed to retrieve clause for user and role criteria", e);
             }
         }
 
         addContainerRefs(filterXml, whereClause, values);
         result = ContainerXmlProvider.getInstance().getContainerRefsXml(values);
         return result;
 
     }
 
     /**
      * @param filter
      * @param containerHandler
      * @return
      */
     public String renderContainers(
         final String filterXml, final FedoraContainerHandler containerHandler)
         throws SystemException, MissingMethodParameterException {
 
         String result = null;
 
         Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addXlinkValues(values);
 
         Map<String, Object> filter = XmlUtility.getFilterMap(filterXml);
 
         String userCriteria = null;
         String roleCriteria = null;
         String whereClause = null;
         if (filter != null) {
             // filter out user permissions
             userCriteria = (String) filter.get("user");
             roleCriteria = (String) filter.get("role");
 
             try {
                 whereClause =
                     getPdp().getRoleUserWhereClause("container", userCriteria,
                         roleCriteria).toString();
             }
             catch (final SystemException e) {
                 // FIXME: throw SystemException?
                 throw new TripleStoreSystemException(
                     "Failed to retrieve clause for user and role criteria", e);
             }
         }
 
         addContainers(filterXml, whereClause, values, containerHandler);
         result = ContainerXmlProvider.getInstance().getContainersXml(values);
         return result;
 
     }
 
 }
