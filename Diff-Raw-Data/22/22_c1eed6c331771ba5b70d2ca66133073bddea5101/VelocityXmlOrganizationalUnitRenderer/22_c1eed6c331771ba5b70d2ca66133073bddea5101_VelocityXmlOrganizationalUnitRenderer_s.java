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
 package de.escidoc.core.oum.business.renderer;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.Predecessor;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.MetadataRecordsXmlProvider;
 import de.escidoc.core.common.util.xml.factory.OrganizationalUnitXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProviderConstants;
 import de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit;
 import de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Organizational unit renderer implementation using the velocity template engine.
  *
  * @author Michael Schneider
  */
 @Service
 public class VelocityXmlOrganizationalUnitRenderer implements OrganizationalUnitRendererInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(VelocityXmlOrganizationalUnitRenderer.class);
 
     private static final int THREE = 3;
 
     private static final int PREDECESSOR_SET_SIZE = 4;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected VelocityXmlOrganizationalUnitRenderer() {
     }
 
     /*
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * 
      * @return
      * 
      * @throws SystemException
      * 
      * @see de.escidoc.core.oum.business.renderer.interfaces.
      * OrganizationalUnitRendererInterface#
      * render(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String render(final OrganizationalUnit organizationalUnit) throws WebserverSystemException,
         TripleStoreSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
 
         values.put("organizationalUnitName", organizationalUnit.getName());
         values.put("organizationalUnitHref", XmlUtility.getOrganizationalUnitHref(organizationalUnit.getId()));
         values.put("organizationalUnitId", organizationalUnit.getId());
 
         addPropertiesValues(organizationalUnit, values);
         addMdRecordsValues(organizationalUnit, values);
         addResourcesValues(organizationalUnit, values);
         addParentsValues(organizationalUnit, values);
         addPredecessorsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getOrganizationalUnitXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @see OrganizationalUnitRendererInterface# renderProperties(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderProperties(final OrganizationalUnit organizationalUnit) throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put("isRootProperties", XmlTemplateProviderConstants.TRUE);
         addPropertiesValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getPropertiesXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @see OrganizationalUnitRendererInterface# renderResources(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderResources(final OrganizationalUnit organizationalUnit) throws WebserverSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put("isRootResources", XmlTemplateProviderConstants.TRUE);
         addResourcesValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getResourcesXml(values);
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public String renderMdRecords(final OrganizationalUnit organizationalUnit) throws WebserverSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put(XmlTemplateProviderConstants.IS_ROOT_SUB_RESOURCE, XmlTemplateProviderConstants.TRUE);
         addMdRecordsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getMdRecordsXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @param organizationalUnit The OrganizationalUnit.
      * @param name               The Name of the to render MdRecord
      * @return XML representation of MdRecord.
      * @see OrganizationalUnitRendererInterface# renderMdRecord(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.lang.String)
      */
     @Override
     public String renderMdRecord(final OrganizationalUnit organizationalUnit, final String name)
         throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addMdRecordValues(organizationalUnit, name, values);
         if (values.isEmpty()) {
             return "";
         }
         addCommonValues(organizationalUnit, values);
 
         values.put(XmlTemplateProviderConstants.IS_ROOT_MD_RECORD, XmlTemplateProviderConstants.TRUE);
         return MetadataRecordsXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @see OrganizationalUnitRendererInterface# renderParents(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderParents(final OrganizationalUnit organizationalUnit) throws WebserverSystemException,
         TripleStoreSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put("isRootParents", XmlTemplateProviderConstants.TRUE);
         addParentsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getParentsXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @see OrganizationalUnitRendererInterface# renderChildObjects(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.util.List)
      */
     @Override
     public String renderChildObjects(final OrganizationalUnit organizationalUnit, final List<String> children)
         throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addListNamespaceValues(values);
         values.put(XmlTemplateProviderConstants.IS_ROOT_LIST, XmlTemplateProviderConstants.TRUE);
         values.put("listTitle", "Children of organizational unit '" + organizationalUnit.getTitle() + '\'');
         values.put("listHref", XmlUtility.getOrganizationalUnitResourcesChildObjectsHref(organizationalUnit.getId()));
         values.put("entries", children);
         return OrganizationalUnitXmlProvider.getInstance().getChildObjectsXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @see OrganizationalUnitRendererInterface# renderParents(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.util.List)
      */
     @Override
     public String renderParentObjects(final OrganizationalUnit organizationalUnit, final List<String> parents)
         throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addListNamespaceValues(values);
 
         values.put(XmlTemplateProviderConstants.IS_ROOT_LIST, XmlTemplateProviderConstants.TRUE);
         values.put("listTitle", "Parents of organizational unit '" + organizationalUnit.getTitle() + '\'');
         values.put("listHref", XmlUtility.getOrganizationalUnitResourcesParentObjectsHref(organizationalUnit.getId()));
         values.put("entries", parents);
         return OrganizationalUnitXmlProvider.getInstance().getParentObjectsXml(values);
     }
 
     /**
      * See Interface for functional description.
      *
      * @see OrganizationalUnitRendererInterface# renderPathList(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.util.List)
      */
     @Override
     public String renderPathList(final OrganizationalUnit organizationalUnit, final List<List<String>> pathes)
         throws WebserverSystemException, TripleStoreSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addPathListNamespaceValues(values);
 
         values.put(XmlTemplateProviderConstants.IS_ROOT_LIST, XmlTemplateProviderConstants.TRUE);
         values.put("listTitle", "Path list of organizational unit '" + organizationalUnit.getTitle() + '\'');
         values.put("listHref", XmlUtility.getOrganizationalUnitResourcesPathListHref(organizationalUnit.getId()));
         final Iterator<List<String>> pathIter = pathes.iterator();
         final Collection<List<Map<String, String>>> pathList = new ArrayList<List<Map<String, String>>>();
         while (pathIter.hasNext()) {
             pathList.add(retrieveRefValues(pathIter.next()));
         }
         values.put("pathes", pathList);
         return OrganizationalUnitXmlProvider.getInstance().getPathListXml(values);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.oum.business.renderer.interfaces.
      * OrganizationalUnitRendererInterface
      * #renderSuccessorsList(de.escidoc.core.oum
      * .business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderSuccessors(final OrganizationalUnit organizationalUnit) throws WebserverSystemException,
         TripleStoreSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
 
         values.put(XmlTemplateProviderConstants.IS_ROOT, XmlTemplateProviderConstants.TRUE);
         addSuccessorsNamespaceValues(values);
         addCommonValues(organizationalUnit, values);
 
         values.put("organizationalUnitName", organizationalUnit.getName());
         values.put("organizationalUnitHref", XmlUtility.getOrganizationalUnitHref(organizationalUnit.getId()));
         values.put("organizationalUnitId", organizationalUnit.getId());
 
         addPropertiesValues(organizationalUnit, values);
         addSuccessorsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getSuccessorsXml(values);
     }
 
     /**
      * Returns a {@code List} of {@code Maps} containing values for the keys {@code id},
      * {@code href}, and {@code title} for every id contained in {@code ids}.
      *
      * @param ids The list of ids.
      * @return The expected {@code List} of {@code Maps}.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private List<Map<String, String>> retrieveRefValues(final Collection<String> ids) throws TripleStoreSystemException {
         final List<Map<String, String>> entries = new ArrayList<Map<String, String>>(ids.size());
         for (final String id : ids) {
             final Map<String, String> entry = new HashMap<String, String>(THREE);
             entry.put("id", id);
             entry.put("href", XmlUtility.getOrganizationalUnitHref(id));
             entry.put("title", this.tripleStoreUtility.getTitle(id));
             entries.add(entry);
         }
         return entries;
     }
 
     /**
      * Adds the common values to the provided map.
      *
      * @param organizationalUnit The organizational unit for that data shall be created.
      * @param values             The map to add values to.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static void addCommonValues(final OrganizationalUnit organizationalUnit, final Map<String, Object> values)
         throws WebserverSystemException {
 
         DateTime lmd = null;
         try {
             lmd = organizationalUnit.getLastModificationDate();
             values.put(XmlTemplateProviderConstants.VAR_LAST_MODIFICATION_DATE, lmd.toString());
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Unable to parse last-modification-date '" + lmd
                 + "' of organizational-unit '" + organizationalUnit.getId() + "'!", e);
         }
         addXlinkValues(values);
         addNamespaceValues(values);
     }
 
     /**
      * Adds the xlink values to the provided map.
      *
      * @param values The map to add values to.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static void addXlinkValues(final Map<String, Object> values) {
 
         values.put(XmlTemplateProviderConstants.VAR_ESCIDOC_BASE_URL, XmlUtility.getEscidocBaseUrl());
         values.put(XmlTemplateProviderConstants.VAR_XLINK_NAMESPACE_PREFIX, Constants.XLINK_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.VAR_XLINK_NAMESPACE, Constants.XLINK_NS_URI);
     }
 
     /**
      * Adds the namespace values to the provided map.
      *
      * @param values The map to add values to.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static void addNamespaceValues(final Map<String, Object> values) {
 
         values.put("organizationalUnitNamespacePrefix", Constants.ORGANIZATIONAL_UNIT_PREFIX);
         values.put("organizationalUnitNamespace", Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI);
         values.put(XmlTemplateProviderConstants.ESCIDOC_PROPERTIES_NS_PREFIX, Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.ESCIDOC_PROPERTIES_NS, Constants.PROPERTIES_NS_URI);
         values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS_PREFIX, Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS, Constants.STRUCTURAL_RELATIONS_NS_URI);
     }
 
     /**
      * Adds the list namespace values to the provided map.
      *
      * @param values The map to add values to.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static void addListNamespaceValues(final Map<String, Object> values) {
 
         values.put("organizationalUnitsNamespacePrefix", Constants.ORGANIZATIONAL_UNIT_LIST_PREFIX);
         values.put("organizationalUnitsNamespace", Constants.ORGANIZATIONAL_UNIT_LIST_NAMESPACE_URI);
     }
 
     /**
      * Adds the path list namespace values to the provided map.
      *
      * @param values The map to add values to.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static void addPathListNamespaceValues(final Map<String, Object> values) {
 
         values.put("organizationalUnitPathListNamespacePrefix", Constants.ORGANIZATIONAL_UNIT_PATH_LIST_PREFIX);
         values.put("organizationalUnitPathListNamespace", Constants.ORGANIZATIONAL_UNIT_PATH_LIST_NAMESPACE_URI);
 
         values.put("organizationalUnitRefNamespacePrefix", Constants.ORGANIZATIONAL_UNIT_REF_PREFIX);
         values.put("organizationalUnitRefNamespace", Constants.ORGANIZATIONAL_UNIT_REF_NAMESPACE_URI);
 
     }
 
     /**
      * Adds namespace values for successor list to the provided map.
      *
      * @param values The map to add values to.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static void addSuccessorsNamespaceValues(final Map<String, Object> values) {
 
        values.put("organizationalUnitNamespacePrefix", Constants.ORGANIZATIONAL_UNIT_SUCCESSORS_PREFIX);
        values.put("organizationalUnitNamespace", Constants.ORGANIZATIONAL_UNIT_SUCCESSORS_LIST_NAMESPACE_URI);

        values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS_PREFIX, Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
        values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS, Constants.STRUCTURAL_RELATIONS_NS_URI);
     }
 
     /**
      * Adds the properties values to the provided map.
      *
      * @param organizationalUnit The organizational unit for that data shall be created.
      * @param values             The map to add values to.
      */
     private static void addPropertiesValues(
         final OrganizationalUnit organizationalUnit, final Map<String, Object> values) {
 
         try {
             values.put(XmlTemplateProviderConstants.VAR_PROPERTIES_TITLE, "Properties");
             values.put(XmlTemplateProviderConstants.VAR_PROPERTIES_HREF, XmlUtility
                 .getOrganizationalUnitPropertiesHref(organizationalUnit.getId()));
             values.put("organizationalUnitStatus", organizationalUnit.getPublicStatus());
             values.put("organizationalUnitCreationDate", organizationalUnit.getCreationDate());
             values.put("organizationalUnitCreatedByTitle", organizationalUnit.getCreatedByTitle());
             values.put("organizationalUnitCreatedByHref", XmlUtility.getUserAccountHref(organizationalUnit
                 .getCreatedBy()));
             values.put("organizationalUnitCreatedById", organizationalUnit.getCreatedBy());
 
             if (organizationalUnit.getModifiedBy() != null) {
                 values.put("organizationalUnitModifiedById", organizationalUnit.getModifiedBy());
                 values.put("organizationalUnitModifiedByTitle", organizationalUnit.getModifiedByTitle());
                 values.put("organizationalUnitModifiedByHref", XmlUtility.getUserAccountHref(organizationalUnit
                     .getModifiedBy()));
             }
 
             values.put(XmlTemplateProviderConstants.VAR_NAME, organizationalUnit.getName());
             values.put(XmlTemplateProviderConstants.VAR_DESCRIPTION, organizationalUnit.getDescription());
 
         }
         catch (final TripleStoreSystemException e) {
             // actually shouldn't this happen
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on syncing with TripleStore.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on syncing with TripleStore.", e);
             }
         }
 
         if (organizationalUnit.hasChildren()) {
             values.put("organizationalUnitHasChildren", XmlTemplateProviderConstants.TRUE);
         }
         else {
             values.put("organizationalUnitHasChildren", XmlTemplateProviderConstants.FALSE);
         }
     }
 
     /**
      * Add values of MdRecords to the value map.
      *
      * @param organizationalUnit The OrganizationalUnit.
      * @param values             The value map which is to extend.
      * @throws WebserverSystemException Thrown if mapping of MdRecord failed.
      */
     private void addMdRecordsValues(final OrganizationalUnit organizationalUnit, final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put(XmlTemplateProviderConstants.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProviderConstants.MD_RECORDS_NAMESPACE, Constants.METADATARECORDS_NAMESPACE_URI);
         values.put("mdRecordsHref", XmlUtility.getOrganizationalUnitMdRecordsHref(organizationalUnit.getId()));
 
         values.put("mdRecordsTitle", "Metadata");
 
         try {
             final Map<String, Datastream> mdRecords = organizationalUnit.getMdRecords();
             final Iterator<Datastream> mdRecordsIter = mdRecords.values().iterator();
             final StringBuilder mdRecordsContent = new StringBuilder();
             while (mdRecordsIter.hasNext()) {
                 final String mdRecordName = mdRecordsIter.next().getName();
                 final Datastream mdRecord;
                 try {
                     mdRecord = organizationalUnit.getMdRecord(mdRecordName);
                 }
                 catch (final Exception e) {
                     throw new WebserverSystemException("Rendering of md-record failed. ", e);
                 }
                 if (!mdRecord.isDeleted()) {
                     final Map<String, Object> mdRecordValues = new HashMap<String, Object>();
                     addCommonValues(organizationalUnit, mdRecordValues);
                     addMdRecordValues(organizationalUnit, mdRecordName, mdRecordValues);
                     mdRecordValues.put(XmlTemplateProviderConstants.IS_ROOT_SUB_RESOURCE,
                         XmlTemplateProviderConstants.FALSE);
                     mdRecordsContent.append(MetadataRecordsXmlProvider.getInstance().getMdRecordXml(mdRecordValues));
                 }
             }
             values.put(XmlTemplateProviderConstants.VAR_MD_RECORDS_CONTENT, mdRecordsContent.toString());
         }
         catch (final SystemException e) {
             throw new WebserverSystemException("Rendering of md-records failed. ", e);
         }
 
     }
 
     /**
      * Add values of MdRecord to value Map.
      *
      * @param organizationalUnit The Orgnaizational Unit.
      * @param name               Name of MdRecord for which the map is to compile.
      * @param values             Map of values which is to extend.
      * @throws WebserverSystemException Thrown if conversion of characters to default encoding failed.
      */
     private void addMdRecordValues(
         final OrganizationalUnit organizationalUnit, final String name, final Map<String, Object> values)
         throws WebserverSystemException {
 
         final Datastream mdRecord;
         try {
             mdRecord = organizationalUnit.getMdRecord(name);
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Rendering of md-record failed. ", e);
         }
         addCommonValues(organizationalUnit, values);
         values.put(XmlTemplateProviderConstants.VAR_MD_RECORD_HREF, XmlUtility.getOrganizationalUnitMdRecordHref(
             organizationalUnit.getId(), mdRecord.getName()));
         values.put(XmlTemplateProviderConstants.MD_RECORD_NAME, mdRecord.getName());
         values.put(XmlTemplateProviderConstants.VAR_MD_RECORD_TITLE, mdRecord.getName() + " metadata set.");
         values.put(XmlTemplateProviderConstants.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProviderConstants.MD_RECORDS_NAMESPACE, Constants.METADATARECORDS_NAMESPACE_URI);
         values.put(XmlTemplateProviderConstants.IS_ROOT_MD_RECORD, XmlTemplateProviderConstants.FALSE);
         try {
             values.put(XmlTemplateProviderConstants.MD_RECORD_CONTENT, mdRecord.toStringUTF8());
         }
         catch (final EncodingSystemException e) {
             throw new WebserverSystemException("Rendering of md-record failed. ", e);
         }
         final List<String> altIds = mdRecord.getAlternateIDs();
         if (!Constants.UNKNOWN.equals(altIds.get(1))) {
             values.put(XmlTemplateProviderConstants.MD_RECORD_TYPE, altIds.get(1));
         }
         if (!Constants.UNKNOWN.equals(altIds.get(2))) {
             values.put(XmlTemplateProviderConstants.MD_RECORD_SCHEMA, altIds.get(2));
         }
 
     }
 
     /**
      * Adds the resource values to the provided map.
      *
      * @param organizationalUnit The organizational unit for that data shall be created.
      * @param values             The map to add values to.
      */
     private static void addResourcesValues(final FedoraResource organizationalUnit, final Map<String, Object> values) {
         values.put(XmlTemplateProviderConstants.RESOURCES_TITLE, "Resources");
         values.put("resourcesHref", XmlUtility.getOrganizationalUnitResourcesHref(organizationalUnit.getId()));
         values.put("parentObjectsHref", XmlUtility.getOrganizationalUnitResourcesParentObjectsHref(organizationalUnit
             .getId()));
         values.put("childObjectsHref", XmlUtility.getOrganizationalUnitResourcesChildObjectsHref(organizationalUnit
             .getId()));
         values.put("pathListHref", XmlUtility.getOrganizationalUnitResourcesPathListHref(organizationalUnit.getId()));
         values.put(XmlTemplateProviderConstants.SUCCESSORS_HREF, XmlUtility
             .getOrganizationalUnitResourcesSuccessorsHref(organizationalUnit.getId()));
 
     }
 
     /**
      * Adds the parents values to the provided map.
      *
      * @param organizationalUnit The organizational unit for that data shall be created.
      * @param values             The map to add values to.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private void addParentsValues(final OrganizationalUnit organizationalUnit, final Map<String, Object> values)
         throws TripleStoreSystemException {
         values.put("parentsHref", XmlUtility.getOrganizationalUnitParentsHref(organizationalUnit.getId()));
         values.put("parentsTitle", "Parents");
         final List<String> ids = organizationalUnit.getParents();
         final Iterator<String> idIter = ids.iterator();
         final Collection<Map<String, String>> entries = new ArrayList<Map<String, String>>(ids.size());
         while (idIter.hasNext()) {
             final Map<String, String> entry = new HashMap<String, String>(THREE);
             final String id = idIter.next();
             entry.put("id", id);
             entry.put("href", XmlUtility.getOrganizationalUnitHref(id));
             entry.put("title", this.tripleStoreUtility.getTitle(id));
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProviderConstants.VAR_PARENTS, entries);
         }
     }
 
     /**
      * Adds predecessor values to the provided map.
      *
      * @param organizationalUnit The organizational unit for that data shall be created.
      * @param values             The map to add values to.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private void addPredecessorsValues(final OrganizationalUnit organizationalUnit, final Map<String, Object> values)
         throws TripleStoreSystemException {
 
         values.put(XmlTemplateProviderConstants.PREDECESSORS_HREF, XmlUtility
             .getOrganizationalUnitPredecessorsHref(organizationalUnit.getId()));
         values.put(XmlTemplateProviderConstants.PREDECESSORS_TITLE, "Predecessors");
 
         final List<Predecessor> predecessors = organizationalUnit.getPredecessors();
         final Iterator<Predecessor> idIter = predecessors.iterator();
 
         final Collection<Map<String, String>> entries = new ArrayList<Map<String, String>>(predecessors.size());
 
         while (idIter.hasNext()) {
             final Map<String, String> entry = new HashMap<String, String>(PREDECESSOR_SET_SIZE);
 
             final Predecessor pred = idIter.next();
             entry.put(XmlTemplateProviderConstants.OBJID, pred.getObjid());
             entry.put(XmlTemplateProviderConstants.HREF, XmlUtility.getOrganizationalUnitHref(pred.getObjid()));
             entry.put(XmlTemplateProviderConstants.TITLE, this.tripleStoreUtility.getTitle(pred.getObjid()));
             entry.put(XmlTemplateProviderConstants.PREDECESSOR_FORM, pred.getForm().getLabel());
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProviderConstants.PREDECESSORS, entries);
         }
     }
 
     /**
      * Adds successor values to the provided map.
      *
      * @param organizationalUnit The organizational unit for that data shall be created.
      * @param values             The map to add values to.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private void addSuccessorsValues(final OrganizationalUnit organizationalUnit, final Map<String, Object> values)
         throws TripleStoreSystemException {
 
         values.put(XmlTemplateProviderConstants.SUCCESSORS_HREF, XmlUtility
             .getOrganizationalUnitSuccessorsHref(organizationalUnit.getId()));
         values.put(XmlTemplateProviderConstants.SUCCESSORS_TITLE, "Successors");
 
         final List<Predecessor> successors = organizationalUnit.getSuccessors();
         if (successors.isEmpty()) {
             return;
         }
         final Iterator<Predecessor> idIter = successors.iterator();
 
         final Collection<Map<String, String>> entries = new ArrayList<Map<String, String>>(successors.size());
 
         while (idIter.hasNext()) {
             final Map<String, String> entry = new HashMap<String, String>(PREDECESSOR_SET_SIZE);
 
             final Predecessor pred = idIter.next();
             entry.put(XmlTemplateProviderConstants.OBJID, pred.getObjid());
             entry.put(XmlTemplateProviderConstants.HREF, XmlUtility.getOrganizationalUnitHref(pred.getObjid()));
             entry.put(XmlTemplateProviderConstants.TITLE, this.tripleStoreUtility.getTitle(pred.getObjid()));
             entry.put(XmlTemplateProviderConstants.SUCCESSOR_FORM, pred.getForm().getLabel());
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProviderConstants.SUCCESSORS, entries);
         }
     }
 }
