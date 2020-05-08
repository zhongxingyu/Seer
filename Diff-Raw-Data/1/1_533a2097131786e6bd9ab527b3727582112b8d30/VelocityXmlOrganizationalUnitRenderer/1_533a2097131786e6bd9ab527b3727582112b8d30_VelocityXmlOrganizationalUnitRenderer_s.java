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
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.MetadataRecordsXmlProvider;
 import de.escidoc.core.common.util.xml.factory.OrganizationalUnitXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
 import de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit;
 import de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Organizational unit renderer implementation using the velocity template
  * engine.
  * 
  * @author MSC
  * 
  */
 public class VelocityXmlOrganizationalUnitRenderer
     implements OrganizationalUnitRendererInterface {
 
     private static final AppLogger log = new AppLogger(
         VelocityXmlOrganizationalUnitRenderer.class.getName());
 
     private static final int THREE = 3;
 
     private static final int PREDECESSOR_SET_SIZE = 4;
 
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
     public String render(final OrganizationalUnit organizationalUnit)
         throws SystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
 
         values.put("organizationalUnitName", organizationalUnit.getName());
         values.put("organizationalUnitHref",
             XmlUtility.getOrganizationalUnitHref(organizationalUnit.getId()));
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
      * @param organizationalUnit
      * @return
      * @throws WebserverSystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderProperties(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderProperties(final OrganizationalUnit organizationalUnit)
         throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put("isRootProperties", XmlTemplateProvider.TRUE);
         addPropertiesValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getPropertiesXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * @return
      * @throws WebserverSystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderResources(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderResources(final OrganizationalUnit organizationalUnit)
         throws WebserverSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put("isRootResources", XmlTemplateProvider.TRUE);
         addResourcesValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getResourcesXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * @return
      * @throws WebserverSystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#renderMdRecords(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderMdRecords(final OrganizationalUnit organizationalUnit)
         throws WebserverSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
             XmlTemplateProvider.TRUE);
         addMdRecordsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getMdRecordsXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      *            The OrganizationalUnit.
      * @param name
      *            The Name of the to render MdRecord
      * @return XML representation of MdRecord.
      * @throws WebserverSystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderMdRecord(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.lang.String)
      */
     @Override
     public String renderMdRecord(
         final OrganizationalUnit organizationalUnit, final String name)
         throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addMdRecordValues(organizationalUnit, name, values);
         if (values.isEmpty()) {
             return "";
         }
         addCommonValues(organizationalUnit, values);
 
         values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD,
             XmlTemplateProvider.TRUE);
         return MetadataRecordsXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * @return
      * @throws SystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderParents(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit)
      */
     @Override
     public String renderParents(final OrganizationalUnit organizationalUnit)
         throws SystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addCommonValues(organizationalUnit, values);
         values.put("isRootParents", XmlTemplateProvider.TRUE);
         addParentsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getParentsXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * @param children
      * @return
      * @throws SystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderChildObjects(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.util.List)
      */
     @Override
     public String renderChildObjects(
         final OrganizationalUnit organizationalUnit, final List<String> children)
         throws SystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addListNamespaceValues(values);
         values.put(XmlTemplateProvider.IS_ROOT_LIST, XmlTemplateProvider.TRUE);
         values.put("listTitle", "Children of organizational unit '"
             + organizationalUnit.getTitle() + '\'');
         values.put("listHref", XmlUtility
             .getOrganizationalUnitResourcesChildObjectsHref(organizationalUnit
                 .getId()));
         values.put("entries", children);
         return OrganizationalUnitXmlProvider.getInstance().getChildObjectsXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * @param parents
      * @return
      * @throws SystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderParents(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.util.List)
      */
     @Override
     public String renderParentObjects(
         final OrganizationalUnit organizationalUnit, final List<String> parents)
         throws SystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addListNamespaceValues(values);
 
         values.put(XmlTemplateProvider.IS_ROOT_LIST, XmlTemplateProvider.TRUE);
         values.put("listTitle", "Parents of organizational unit '"
             + organizationalUnit.getTitle() + '\'');
         values.put("listHref", XmlUtility
             .getOrganizationalUnitResourcesParentObjectsHref(organizationalUnit
                 .getId()));
         values.put("entries", parents);
         return OrganizationalUnitXmlProvider.getInstance().getParentObjectsXml(values);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param organizationalUnit
      * @param pathes
      * @return
      * @throws SystemException
      * @see de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface#
      *      renderPathList(de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit,
      *      java.util.List)
      */
     @Override
     public String renderPathList(
         final OrganizationalUnit organizationalUnit,
         final List<List<String>> pathes) throws SystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addPathListNamespaceValues(values);
 
         values.put(XmlTemplateProvider.IS_ROOT_LIST, XmlTemplateProvider.TRUE);
         values.put("listTitle", "Path list of organizational unit '"
             + organizationalUnit.getTitle() + '\'');
         values.put("listHref", XmlUtility
             .getOrganizationalUnitResourcesPathListHref(organizationalUnit
                 .getId()));
         final Iterator<List<String>> pathIter = pathes.iterator();
         final Collection<List<Map<String, String>>> pathList =
             new ArrayList<List<Map<String, String>>>();
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
     public String renderSuccessors(final OrganizationalUnit organizationalUnit)
         throws SystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
 
         values.put(XmlTemplateProvider.IS_ROOT, XmlTemplateProvider.TRUE);
         addSuccessorsNamespaceValues(values);
         addCommonValues(organizationalUnit, values);
 
         values.put("organizationalUnitName", organizationalUnit.getName());
         values.put("organizationalUnitHref",
             XmlUtility.getOrganizationalUnitHref(organizationalUnit.getId()));
         values.put("organizationalUnitId", organizationalUnit.getId());
 
         addPropertiesValues(organizationalUnit, values);
         addSuccessorsValues(organizationalUnit, values);
         return OrganizationalUnitXmlProvider.getInstance().getSuccessorsXml(values);
     }
 
     /**
      * Returns a <code>List</code> of <code>Maps</code> containing values for
      * the keys <code>id</code>, <code>href</code>, and <code>title</code> for
      * every id contained in <code>ids</code>.
      * 
      * @param ids
      *            The list of ids.
      * @return The expected <code>List</code> of <code>Maps</code>.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private List<Map<String, String>> retrieveRefValues(final Collection<String> ids)
         throws SystemException {
         final List<Map<String, String>> entries =
             new ArrayList<Map<String, String>>(ids.size());
         for (final String id : ids) {
             final Map<String, String> entry = new HashMap<String, String>(THREE);
             entry.put("id", id);
             entry.put("href", XmlUtility.getOrganizationalUnitHref(id));
             entry.put("title", TripleStoreUtility.getInstance().getTitle(id));
             entries.add(entry);
         }
         return entries;
     }
 
     /**
      * Adds the common values to the provided map.
      * 
      * @param organizationalUnit
      *            The organizational unit for that data shall be created.
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addCommonValues(
         final OrganizationalUnit organizationalUnit,
         final Map<String, Object> values) throws WebserverSystemException {
 
         String lmd = null;
         try {
             lmd = organizationalUnit.getLastModificationDate();
             final DateTime t = new DateTime(lmd, DateTimeZone.UTC);
             values.put(XmlTemplateProvider.VAR_LAST_MODIFICATION_DATE,
                 t.toString());
         }
         catch (Exception e) {
             throw new WebserverSystemException(
                 "Unable to parse last-modification-date '" + lmd
                     + "' of organizational-unit '" + organizationalUnit.getId()
                     + "'!", e);
         }
         addXlinkValues(values);
         addNamespaceValues(values);
     }
 
     /**
      * Adds the xlink values to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addXlinkValues(final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL,
             XmlUtility.getEscidocBaseUrl());
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE_PREFIX,
             Constants.XLINK_NS_PREFIX);
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE,
             Constants.XLINK_NS_URI);
     }
 
     /**
      * Adds the namespace values to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addNamespaceValues(final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put("organizationalUnitNamespacePrefix",
             Constants.ORGANIZATIONAL_UNIT_PREFIX);
         values.put("organizationalUnitNamespace",
             Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI);
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS_PREFIX,
             Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS,
             Constants.PROPERTIES_NS_URI);
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
             Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS,
             Constants.STRUCTURAL_RELATIONS_NS_URI);
     }
 
     /**
      * Adds the list namespace values to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addListNamespaceValues(final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put("organizationalUnitsNamespacePrefix",
             Constants.ORGANIZATIONAL_UNIT_LIST_PREFIX);
         values.put("organizationalUnitsNamespace",
             Constants.ORGANIZATIONAL_UNIT_LIST_NAMESPACE_URI);
     }
 
     /**
      * Adds the path list namespace values to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addPathListNamespaceValues(final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put("organizationalUnitPathListNamespacePrefix",
             Constants.ORGANIZATIONAL_UNIT_PATH_LIST_PREFIX);
         values.put("organizationalUnitPathListNamespace",
             Constants.ORGANIZATIONAL_UNIT_PATH_LIST_NAMESPACE_URI);
 
         values.put("organizationalUnitRefNamespacePrefix",
             Constants.ORGANIZATIONAL_UNIT_REF_PREFIX);
         values.put("organizationalUnitRefNamespace",
             Constants.ORGANIZATIONAL_UNIT_REF_NAMESPACE_URI);
 
     }
 
     /**
      * Adds namespace values for successor list to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addSuccessorsNamespaceValues(final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put("organizationalUnitNamespacePrefix",
             Constants.ORGANIZATIONAL_UNIT_SUCCESSORS_PREFIX);
         values.put("organizationalUnitNamespace",
             Constants.ORGANIZATIONAL_UNIT_SUCCESSORS_LIST_NAMESPACE_URI);
 
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
             Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS,
             Constants.STRUCTURAL_RELATIONS_NS_URI);
     }
 
     /**
      * Adds the properties values to the provided map.
      * 
      * @param organizationalUnit
      *            The organizational unit for that data shall be created.
      * @param values
      *            The map to add values to.
      * @oum
      */
     private void addPropertiesValues(
         final OrganizationalUnit organizationalUnit,
         final Map<String, Object> values) {
 
         try {
             values.put(XmlTemplateProvider.VAR_PROPERTIES_TITLE, "Properties");
             values.put(XmlTemplateProvider.VAR_PROPERTIES_HREF,
                 XmlUtility
                     .getOrganizationalUnitPropertiesHref(organizationalUnit
                         .getId()));
             values.put("organizationalUnitStatus",
                 organizationalUnit.getPublicStatus());
             values.put("organizationalUnitCreationDate",
                 organizationalUnit.getCreationDate());
             values.put("organizationalUnitCreatedByTitle",
                 organizationalUnit.getCreatedByTitle());
             values.put("organizationalUnitCreatedByHref", XmlUtility
                 .getUserAccountHref(organizationalUnit.getCreatedBy()));
             values.put("organizationalUnitCreatedById",
                 organizationalUnit.getCreatedBy());
 
             if (organizationalUnit.getModifiedBy() != null) {
                 values.put("organizationalUnitModifiedById",
                     organizationalUnit.getModifiedBy());
                 values.put("organizationalUnitModifiedByTitle",
                     organizationalUnit.getModifiedByTitle());
                 values.put("organizationalUnitModifiedByHref", XmlUtility
                     .getUserAccountHref(organizationalUnit.getModifiedBy()));
             }
 
             values.put(XmlTemplateProvider.VAR_NAME,
                 organizationalUnit.getName());
             values.put(XmlTemplateProvider.VAR_DESCRIPTION,
                 organizationalUnit.getDescription());
 
         }
         catch (TripleStoreSystemException e) {
             // actually shouldn't this happen
             log.error(e);
         }
         catch (WebserverSystemException e) {
             // actually shouldn't this happen
             log.error(e);
         }
 
         if (organizationalUnit.hasChildren()) {
             values.put("organizationalUnitHasChildren",
                 XmlTemplateProvider.TRUE);
         }
         else {
             values.put("organizationalUnitHasChildren",
                 XmlTemplateProvider.FALSE);
         }
     }
 
     /**
      * Add values of MdRecords to the value map.
      * 
      * @param organizationalUnit
      *            The OrganizationalUnit.
      * @param values
      *            The value map which is to extend.
      * @throws WebserverSystemException
      *             Thrown if mapping of MdRecord failed.
      */
     private void addMdRecordsValues(
         final OrganizationalUnit organizationalUnit,
         final Map<String, Object> values) throws WebserverSystemException {
 
         values.put(XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.MD_RECORDS_NAMESPACE,
             Constants.METADATARECORDS_NAMESPACE_URI);
         values.put("mdRecordsHref", XmlUtility
             .getOrganizationalUnitMdRecordsHref(organizationalUnit.getId()));
 
         values.put("mdRecordsTitle", "Metadata");
 
         try {
             final Map<String, Datastream> mdRecords = (HashMap<String, Datastream>) organizationalUnit.getMdRecords();
             final Iterator<Datastream> mdRecordsIter = mdRecords.values().iterator();
             StringBuffer mdRecordsContent = new StringBuffer();
             while (mdRecordsIter.hasNext()) {
                 final String mdRecordName = mdRecordsIter.next().getName();
                 final Datastream mdRecord;
                 try {
                     mdRecord = organizationalUnit.getMdRecord(mdRecordName);
                 }
                 catch (Exception e) {
                     throw new WebserverSystemException(
                         "Rendering of md-record failed. ", e);
                 }
                 if (!mdRecord.isDeleted()) {
                     final Map<String, Object> mdRecordValues =
                         new HashMap<String, Object>();
                     addCommonValues(organizationalUnit, mdRecordValues);
                     addMdRecordValues(organizationalUnit, mdRecordName,
                         mdRecordValues);
                     mdRecordValues.put(
                         XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                         XmlTemplateProvider.FALSE);
                    mdRecordsContent.append(mdRecordsContent);
                     mdRecordsContent.append(MetadataRecordsXmlProvider
                                 .getInstance().getMdRecordXml(mdRecordValues));
                 }
             }
             values.put("mdRecordsContent", mdRecordsContent);
         }
         catch (SystemException e) {
             throw new WebserverSystemException(
                 "Rendering of md-records failed. ", e);
         }
 
     }
 
     /**
      * Add values of MdRecord to value Map.
      * 
      * @param organizationalUnit
      *            The Orgnaizational Unit.
      * @param name
      *            Name of MdRecord for which the map is to compile.
      * @param values
      *            Map of values which is to extend.
      * @throws WebserverSystemException
      *             Thrown if conversion of characters to default encoding
      *             failed.
      */
     private void addMdRecordValues(
         final OrganizationalUnit organizationalUnit, final String name,
         final Map<String, Object> values) throws WebserverSystemException {
 
         final Datastream mdRecord;
         try {
             mdRecord = organizationalUnit.getMdRecord(name);
         }
         catch (Exception e) {
             throw new WebserverSystemException(
                 "Rendering of md-record failed. ", e);
         }
         addCommonValues(organizationalUnit, values);
         values.put(
             XmlTemplateProvider.VAR_MD_RECORD_HREF,
             XmlUtility.getOrganizationalUnitMdRecordHref(
                 organizationalUnit.getId(), mdRecord.getName()));
         values.put(XmlTemplateProvider.MD_RECORD_NAME, mdRecord.getName());
         values.put(XmlTemplateProvider.VAR_MD_RECORD_TITLE, mdRecord.getName()
             + " metadata set.");
         values.put(XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.MD_RECORDS_NAMESPACE,
             Constants.METADATARECORDS_NAMESPACE_URI);
         values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD,
             XmlTemplateProvider.FALSE);
         try {
             values.put(XmlTemplateProvider.MD_RECORD_CONTENT,
                 mdRecord.toStringUTF8());
         }
         catch (EncodingSystemException e) {
             log.warn("encoding error: " + e);
             throw new WebserverSystemException(
                 "Rendering of md-record failed. ", e);
         }
         final List<String> altIds = mdRecord.getAlternateIDs();
         if (!"unknown".equals(altIds.get(1))) {
             values.put(XmlTemplateProvider.MD_RECORD_TYPE, altIds.get(1));
         }
         if (!"unknown".equals(altIds.get(2))) {
             values.put(XmlTemplateProvider.MD_RECORD_SCHEMA, altIds.get(2));
         }
 
     }
 
     /**
      * Adds the resource values to the provided map.
      * 
      * @param organizationalUnit
      *            The organizational unit for that data shall be created.
      * @param values
      *            The map to add values to.
      * @oum
      */
     private void addResourcesValues(
         final FedoraResource organizationalUnit,
         final Map<String, Object> values) {
         values.put(XmlTemplateProvider.RESOURCES_TITLE, "Resources");
         values.put("resourcesHref", XmlUtility
             .getOrganizationalUnitResourcesHref(organizationalUnit.getId()));
         values.put("parentObjectsHref", XmlUtility
             .getOrganizationalUnitResourcesParentObjectsHref(organizationalUnit
                 .getId()));
         values.put("childObjectsHref", XmlUtility
             .getOrganizationalUnitResourcesChildObjectsHref(organizationalUnit
                 .getId()));
         values.put("pathListHref", XmlUtility
             .getOrganizationalUnitResourcesPathListHref(organizationalUnit
                 .getId()));
         values.put(XmlTemplateProvider.SUCCESSORS_HREF, XmlUtility
             .getOrganizationalUnitResourcesSuccessorsHref(organizationalUnit
                 .getId()));
 
     }
 
     /**
      * Adds the parents values to the provided map.
      * 
      * @param organizationalUnit
      *            The organizational unit for that data shall be created.
      * @param values
      *            The map to add values to.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @oum
      */
     private void addParentsValues(
         final OrganizationalUnit organizationalUnit,
         final Map<String, Object> values) throws SystemException {
         values.put("parentsHref", XmlUtility
             .getOrganizationalUnitParentsHref(organizationalUnit.getId()));
         values.put("parentsTitle", "Parents");
         final List<String> ids = organizationalUnit.getParents();
         final Iterator<String> idIter = ids.iterator();
         final Collection<Map<String, String>> entries =
             new ArrayList<Map<String, String>>(ids.size());
         while (idIter.hasNext()) {
             final Map<String, String> entry = new HashMap<String, String>(THREE);
             final String id = idIter.next();
             entry.put("id", id);
             entry.put("href", XmlUtility.getOrganizationalUnitHref(id));
             entry.put("title", TripleStoreUtility.getInstance().getTitle(id));
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProvider.VAR_PARENTS, entries);
         }
     }
 
     /**
      * Adds predecessor values to the provided map.
      * 
      * @param organizationalUnit
      *            The organizational unit for that data shall be created.
      * @param values
      *            The map to add values to.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private void addPredecessorsValues(
         final OrganizationalUnit organizationalUnit,
         final Map<String, Object> values) throws SystemException {
 
         values.put(XmlTemplateProvider.PREDECESSORS_HREF, XmlUtility
             .getOrganizationalUnitPredecessorsHref(organizationalUnit.getId()));
         values.put(XmlTemplateProvider.PREDECESSORS_TITLE, "Predecessors");
 
         final List<Predecessor> predecessors = organizationalUnit.getPredecessors();
         final Iterator<Predecessor> idIter = predecessors.iterator();
 
         final Collection<Map<String, String>> entries =
             new ArrayList<Map<String, String>>(predecessors.size());
 
         while (idIter.hasNext()) {
             final Map<String, String> entry =
                 new HashMap<String, String>(PREDECESSOR_SET_SIZE);
 
             final Predecessor pred = idIter.next();
             entry.put(XmlTemplateProvider.OBJID, pred.getObjid());
             entry.put(XmlTemplateProvider.HREF,
                 XmlUtility.getOrganizationalUnitHref(pred.getObjid()));
             entry.put(XmlTemplateProvider.TITLE, TripleStoreUtility
                 .getInstance().getTitle(pred.getObjid()));
             entry.put(XmlTemplateProvider.PREDECESSOR_FORM, pred
                 .getForm().getLabel());
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProvider.PREDECESSORS, entries);
         }
     }
 
     /**
      * Adds successor values to the provided map.
      * 
      * @param organizationalUnit
      *            The organizational unit for that data shall be created.
      * @param values
      *            The map to add values to.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private void addSuccessorsValues(
         final OrganizationalUnit organizationalUnit,
         final Map<String, Object> values) throws SystemException {
 
         values.put(XmlTemplateProvider.SUCCESSORS_HREF, XmlUtility
             .getOrganizationalUnitSuccessorsHref(organizationalUnit.getId()));
         values.put(XmlTemplateProvider.SUCCESSORS_TITLE, "Successors");
 
         final List<Predecessor> successors = organizationalUnit.getSuccessors();
         if (successors.isEmpty()) {
             return;
         }
         final Iterator<Predecessor> idIter = successors.iterator();
 
         final Collection<Map<String, String>> entries =
             new ArrayList<Map<String, String>>(successors.size());
 
         while (idIter.hasNext()) {
             final Map<String, String> entry =
                 new HashMap<String, String>(PREDECESSOR_SET_SIZE);
 
             final Predecessor pred = idIter.next();
             entry.put(XmlTemplateProvider.OBJID, pred.getObjid());
             entry.put(XmlTemplateProvider.HREF,
                 XmlUtility.getOrganizationalUnitHref(pred.getObjid()));
             entry.put(XmlTemplateProvider.TITLE, TripleStoreUtility
                 .getInstance().getTitle(pred.getObjid()));
             entry.put(XmlTemplateProvider.SUCCESSOR_FORM, pred
                 .getForm().getLabel());
 
             entries.add(entry);
         }
         if (!entries.isEmpty()) {
             values.put(XmlTemplateProvider.SUCCESSORS, entries);
         }
     }
 }
