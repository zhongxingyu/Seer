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
 package de.escidoc.core.common.util.xml.factory;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.resources.create.ContentRelationCreate;
 import de.escidoc.core.common.business.fedora.resources.create.MdRecordCreate;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * XML Provider of ContentRelation.
  * 
  * @author SWA
  * 
  */
 public final class ContentRelationXmlProvider extends InfrastructureXmlProvider {
 
     private static final String CONTENT_RELATION_PATH = "/content-relation";
 
     private static final String RESOURCES_PATH = CONTENT_RELATION_PATH;
 
     private static final String DATA_PATH = CONTENT_RELATION_PATH;
 
     private static final String CONTENT_RELATION_RESOURCE_NAME =
         "content-relation";
 
     private static final String RESOURCE_WITHDRAWN_NAME = "withdrawn";
 
     private static final String PROPERTIES_RESOURCE_NAME = "properties";
 
     private static final String RESOURCES_RESOURCE_NAME = "resources";
 
     public static final String COMMON_PATH = "/common";
 
     public static final String MD_RECORDS_RESOURCE_NAME = "md-records";
 
     public static final String MD_RECORDS_PATH = COMMON_PATH;
 
     public static final String MD_RECORD_PATH = COMMON_PATH;
 
     private static ContentRelationXmlProvider provider = null;
 
     private static AppLogger log = new AppLogger(
         ContentRelationXmlProvider.class.getName());
 
     /**
      * Private constructor to prevent initialization.
      * 
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @aa
      */
     private ContentRelationXmlProvider() throws WebserverSystemException {
         super();
     }
 
     /**
      * Gets the XML provider.
      * 
      * @return Returns the <code>ContentRelationXmlProvider</code> object.
      * @throws WebserverSystemException
      *             Thrown if the instance cannot be created due to an internal
      *             error.
      */
     public static ContentRelationXmlProvider getInstance()
         throws WebserverSystemException {
 
         if (provider == null) {
             provider = new ContentRelationXmlProvider();
         }
         return provider;
     }
 
     /**
      * Render XML representation of Content Relation.
      * 
      * @param cr
      *            ContentRelation
      * @return XML representation
      * @throws SystemException
      *             Thrown if an internal error occur.
      */
     public String getContentRelationXml(final ContentRelationCreate cr)
         throws SystemException {
 
         Map<String, Object> values = new HashMap<String, Object>();
         // put all relevant values from object into value Map
         Map<String, String> commonValues = getCommonValues();
 
         values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
             XmlTemplateProvider.FALSE);
         values.put(XmlTemplateProvider.VAR_PROPERTIES_HREF, "/ir"
             + CONTENT_RELATION_PATH + "/" + cr.getObjid() + "/properties");
         values.put(XmlTemplateProvider.VAR_PROPERTIES_TITLE,
             "Content Relation Properties");
 
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_CONTENT,
             renderMdRecords(cr, commonValues, false));
         values.putAll(getResourceValues(cr));
         values.putAll(getLockValues(cr));
         values.putAll(commonValues);
         values.putAll(getRelationValues(cr));
 
         return getXml(CONTENT_RELATION_RESOURCE_NAME, CONTENT_RELATION_PATH,
             values);
     }
 
     /**
      * Get XML representation of ContentRelation md-records.
      * 
      * @param cr
      *            ContentRelation
      * @return XML representation of md-records.
      * @throws SystemException
      *             If anything fails.
      */
     public String getContentRelationMdRecords(final ContentRelationCreate cr)
         throws SystemException, EncodingSystemException, FedoraSystemException,
         IntegritySystemException {
 
         Map<String, String> commonValues = getCommonValues();
         return renderMdRecords(cr, commonValues, true);
     }
 
     /**
      * Get XML representation of ContentRelation md-record with a provided name.
      * 
      * @param cr
      *            ContentRelation
      * @param mr
      *            meta date record
      * @return XML representation of md-record.
      * @throws SystemException
      *             If anything fails.
      */
     public String getContentRelationMdRecord(
         final ContentRelationCreate cr, final MdRecordCreate mr)
         throws MdRecordNotFoundException, FedoraSystemException,
         SystemException {
 
         Map<String, String> commonValues = getCommonValues();
         String mdRecord = renderMdRecord(cr, mr, commonValues, true);
         if (mdRecord.length() == 0) {
             String message =
                 "A content relation with id " + cr.getObjid()
                     + "does not contain a md-record with a name "
                     + mr.getName() + " .";
             log.debug(message);
             throw new MdRecordNotFoundException(message);
 
         }
         return mdRecord;
     }
 
     /**
      * Render XML representation of Content Relation.
      * 
      * @param cr
      *            ContentRelation
      * @return XML representation
      * @throws SystemException
      *             Thrown if an internal error occur.
      */
     public String getContentRelationPropertiesXml(final ContentRelationCreate cr)
         throws SystemException {
 
         Map<String, Object> values = new HashMap<String, Object>();
         // put all relevant values from object into value Map
         Map<String, String> commonValues = getCommonValues();
 
         values.putAll(getResourceValues(cr));
         values.putAll(getLockValues(cr));
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
             XmlTemplateProvider.TRUE);
         values.put(XmlTemplateProvider.VAR_PROPERTIES_HREF, "/ir"
             + CONTENT_RELATION_PATH + "/" + cr.getObjid() + "/properties");
         values.put(XmlTemplateProvider.VAR_PROPERTIES_TITLE,
             "Content Relation Properties");
 
         return getPropertiesXml(values);
     }
 
     /**
      * Get XML representation of ContentRelation properties.
      * 
      * @param values
      *            Map of values.
      * @return XML representation of Properties.
      * @throws WebserverSystemException
      *             If anything fails.
      */
     public String getPropertiesXml(final Map<String, Object> values)
         throws WebserverSystemException {
 
         return getXml(PROPERTIES_RESOURCE_NAME, CONTENT_RELATION_PATH, values);
     }
 
     /**
      * Get XML representation of ContentRelation (virtual-)resources.
      * 
      * @param values
      *            Map of values.
      * @return XML representation of resources.
      * @throws WebserverSystemException
      *             If anything fails.
      */
     public String getContentRelationResourcesXml(final ContentRelationCreate cr)
         throws WebserverSystemException {
 
         Map<String, String> values = getResourceValues(cr);
         values.putAll(getCommonValues());
         values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
             XmlTemplateProvider.TRUE);
         return getXml(RESOURCES_RESOURCE_NAME, CONTENT_RELATION_PATH, values);
     }
 
     /**
      * Get XML representation of Data.
      * 
      * @param values
      *            Map of values.
      * @return XML representation of context list.
      * @throws WebserverSystemException
      *             If anything fails.
      */
     public String getDataXml(final Map<String, Object> values)
         throws WebserverSystemException {
 
         return getXml(CONTENT_RELATION_RESOURCE_NAME, DATA_PATH, values);
     }
 
     /**
      * Get withdrawn message as XML snippet.
      * 
      * @param values
      *            Value Map.
      * @return XML snippet with withdrawn message.
      * @throws WebserverSystemException
      *             If anything fails.
      */
     public String getWithdrawnMessageXml(final Map<String, Object> values)
         throws WebserverSystemException {
         return getXml(RESOURCE_WITHDRAWN_NAME, RESOURCES_PATH, values);
     }
 
     /**
      * Get common values for ContentRelation.
      * 
      * @return Map with common ContentRelation values.
      * 
      * @throws WebserverSystemException
      *             Thrown if values extracting failed.
      */
     private Map<String, String> getCommonValues()
         throws WebserverSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProvider.CONTENT_RELATION_NAMESPACE_PREFIX,
             Constants.RELATION_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.CONTENT_RELATION_NAMESPACE,
             Constants.CONTENT_RELATION_NAMESPACE_URI);
 
         values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL,
             XmlUtility.getEscidocBaseUrl());
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE_PREFIX,
             Constants.XLINK_NS_PREFIX);
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE,
             Constants.XLINK_NS_URI);
 
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS_PREFIX,
             Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS,
             Constants.PROPERTIES_NS_URI);
 
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
             Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_SREL_NS,
             Constants.STRUCTURAL_RELATIONS_NS_URI);
 
         values.put(XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.MD_RECORDS_NAMESPACE,
             Constants.METADATARECORDS_NAMESPACE_URI);
 
         return values;
     }
 
     /**
      * Get values from ContentRelation.
      * 
      * @param cr
      *            The ContentRelation.
      * @return Map with common ContentRelation values.
      * 
      * @throws WebserverSystemException
      *             Thrown if values extracting failed.
      */
     private Map<String, String> getResourceValues(final ContentRelationCreate cr)
         throws WebserverSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProvider.OBJID, cr.getObjid());
         values.put(XmlTemplateProvider.TITLE, cr.getProperties().getTitle());
         values.put(XmlTemplateProvider.HREF, "/ir" + CONTENT_RELATION_PATH
             + "/" + cr.getObjid());
 
         values.put(XmlTemplateProvider.RESOURCES_TITLE, "Virtual Resources");
         values.put("resourcesHref",
             XmlUtility.getContentRelationHref(cr.getObjid()) + "/resources");
 
         values.put(XmlTemplateProvider.CREATED_BY_ID, cr
             .getProperties().getCreatedById());
         values.put(XmlTemplateProvider.CREATED_BY_HREF,
             Constants.USER_ACCOUNT_URL_BASE
                 + cr.getProperties().getCreatedById());
         values.put(XmlTemplateProvider.CREATED_BY_TITLE, cr
             .getProperties().getCreatedByName());
 
         values.put(XmlTemplateProvider.MODIFIED_BY_ID, cr
             .getProperties().getModifiedById());
         values.put(XmlTemplateProvider.MODIFIED_BY_HREF,
             Constants.USER_ACCOUNT_URL_BASE
                 + cr.getProperties().getModifiedById());
         values.put(XmlTemplateProvider.MODIFIED_BY_TITLE, cr
             .getProperties().getModifiedByName());
 
         values.put(XmlTemplateProvider.VAR_LAST_MODIFICATION_DATE, cr
             .getProperties().getLastModificationDate().toString());
         values.put(XmlTemplateProvider.VAR_CREATION_DATE, cr
             .getProperties().getCreationDate().toString());
 
         if (cr.getProperties().getDescription() != null) {
             values.put(XmlTemplateProvider.CONTENT_RELATION_DESCRIPTION, cr
                 .getProperties().getDescription());
         }
 
         values.put(XmlTemplateProvider.PUBLIC_STATUS, cr
             .getProperties().getStatus().toString());
         values.put(XmlTemplateProvider.PUBLIC_STATUS_COMMENT,
             XmlUtility.escapeForbiddenXmlCharacters(cr
                 .getProperties().getStatusComment()));
         values.put(XmlTemplateProvider.OBJECT_PID, cr.getProperties().getPid());
 
         return values;
     }
 
     /**
      * Prepare values for resource lock in map.
      * 
      * @param cr
      *            ContentRelation
      * @return Map with velocity keys
      */
     private Map<String, String> getLockValues(final ContentRelationCreate cr) {
 
         Map<String, String> values = new HashMap<String, String>();
         values.put(XmlTemplateProvider.LOCK_STATUS, cr
             .getProperties().getLockStatus().toString());
 
         if (cr.getProperties().isLocked()) {
             values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_OWNER_ID, cr
                 .getProperties().getLockOwnerId());
             values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_OWNER_HREF,
                 Constants.USER_ACCOUNT_URL_BASE
                     + cr.getProperties().getLockOwnerId());
             values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_OWNER_TITLE,
                 cr.getProperties().getLockOwnerName());
             values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_DATE, cr
                .getProperties().getLockDate().toString());
         }
 
         return values;
 
     }
 
     /**
      * Get relation values from ContentRelation.
      * 
      * @param cr
      *            The ContentRelation.
      * @return Map with common ContentRelation values.
      * 
      * @throws WebserverSystemException
      *             Thrown if values extracting failed.
      * @throws TripleStoreSystemException
      *             Thrown if obtaining resource type failed.
      */
     private Map<String, String> getRelationValues(final ContentRelationCreate cr)
         throws WebserverSystemException, TripleStoreSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
 
         // type -----------------
         values.put(XmlTemplateProvider.CONTENT_RELATION_TYPE, cr
             .getType().toString());
 
         // subject -----------------
         String objid = cr.getSubject();
         String version = cr.getSubjectVersion();
         String subjectId = objid;
         if (version != null) {
             subjectId = subjectId + ":" + version;
         }
         values.put(XmlTemplateProvider.CONTENT_RELATION_SUBJECT_ID, subjectId);
         String subjectType =
             TripleStoreUtility.getInstance().getObjectType(objid);
         String subHref = XmlUtility.getHref(subjectType, subjectId);
         values.put(XmlTemplateProvider.CONTENT_RELATION_SUBJECT_HREF, subHref);
         // TODO: the title of an old version in the case of a fixed reference
         // to the old subject version
         // WRONG!!!!!
         values.put(XmlTemplateProvider.CONTENT_RELATION_SUBJECT_TITLE,
             TripleStoreUtility.getInstance().getTitle(objid));
 
         // object -----------------
         objid = cr.getObject();
         version = cr.getObjectVersion();
         String objectId = objid;
         if (version != null) {
             objectId = objectId + ":" + version;
         }
         values.put(XmlTemplateProvider.CONTENT_RELATION_OBJECT_ID, objectId);
         String objectType =
             TripleStoreUtility.getInstance().getObjectType(objid);
         subHref = XmlUtility.getHref(objectType, objectId);
         values.put(XmlTemplateProvider.CONTENT_RELATION_OBJECT_HREF, subHref);
         // TODO: the title of an old version in the case of a fixed reference
         // to the old subject version
         // WRONG!!!!!
         values.put(XmlTemplateProvider.CONTENT_RELATION_OBJECT_TITLE,
             TripleStoreUtility.getInstance().getTitle(objid));
 
         return values;
     }
 
     /**
      * Render MD Record whereas the common values are not to recompile.
      * 
      * @param cr
      *            ContentRelationCreate
      * @param mdRecord
      *            md-record.
      * @param commonValues
      *            Common render values.
      * @param isRoot
      *            Set true is md-record is to render with XML root element
      * @return XMl representation of md-record.
      * 
      * @throws WebserverSystemException
      * @throws IntegritySystemException
      * @throws EncodingSystemException
      * @throws MdRecordNotFoundException
      * @throws TripleStoreSystemException
      */
     public String renderMdRecord(
         final ContentRelationCreate cr, final MdRecordCreate mdRecord,
         final Map<String, String> commonValues, final boolean isRoot)
         throws WebserverSystemException, IntegritySystemException,
         EncodingSystemException, MdRecordNotFoundException,
         TripleStoreSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF,
             Constants.CONTENT_RELATION_URL_BASE + cr.getObjid()
                 + Constants.MD_RECORD_URL_PART + "/" + mdRecord.getName());
         if (!mdRecord.getType().equals(Constants.UNKNOWN)) {
             values.put(XmlTemplateProvider.MD_RECORD_TYPE, mdRecord.getType());
         }
         if (!mdRecord.getSchema().equals(Constants.UNKNOWN)) {
             values.put(XmlTemplateProvider.MD_RECORD_SCHEMA,
                 mdRecord.getSchema());
         }
 
         // get md-record content
         String content = mdRecord.getContent();
         if (content != null) {
             values.put(XmlTemplateProvider.MD_RECORD_CONTENT, content);
         }
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD,
                 XmlTemplateProvider.TRUE);
         }
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.MD_RECORD_NAME, mdRecord.getName());
         values.put(XmlTemplateProvider.VAR_MD_RECORD_TITLE, mdRecord.getName());
         values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF,
             Constants.CONTENT_RELATION_URL_BASE + cr.getObjid()
                 + Constants.MD_RECORD_URL_PART + "/" + mdRecord.getName());
 
         return ItemXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     // public String renderMdRecord(
     // final ContentRelationCreate cr, final String name,
     // final Map<String, String> commonValues, final boolean isRoot)
     // throws MdRecordNotFoundException, FedoraSystemException,
     // SystemException {
     // Map<String, String> values = new HashMap<String, String>();
     // values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF, cr.getHref()
     // + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
     // + "/" + name);
     // values.putAll(commonValues);
     //
     // Datastream ds = cr.getMdRecord(name);
     // if (ds.isDeleted()) {
     // return new String("");
     // }
     // Vector<String> altIds = ds.getAlternateIDs();
     // if (altIds.size() > 1 && !altIds.get(1).equals("unknown")) {
     // values.put(XmlTemplateProvider.MD_RECORD_TYPE, altIds.get(1));
     // }
     // if (altIds.size() > 2 && !altIds.get(2).equals("unknown")) {
     // values.put(XmlTemplateProvider.MD_RECORD_SCHEMA, altIds.get(2));
     // }
     // try {
     // values.put(XmlTemplateProvider.MD_RECORD_CONTENT, ds
     // .toString(XmlUtility.CHARACTER_ENCODING));
     // }
     // catch (EncodingSystemException e) {
     // throw new EncodingSystemException(e.getMessage(), e);
     // }
     //
     // if (isRoot) {
     // values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD,
     // XmlTemplateProvider.TRUE);
     // }
     // values.put(XmlTemplateProvider.MD_RECORD_NAME, name);
     // values.put(XmlTemplateProvider.VAR_MD_RECORD_TITLE, name);
     // return ItemXmlProvider.getInstance().getMdRecordXml(values);
     // }
 
     // public String renderMdRecords(
     // final ContentRelationCreate cr, final Map<String, String> commonValues,
     // final boolean isRoot) throws SystemException {
     // Map<String, String> values = new HashMap<String, String>();
     // values.putAll(commonValues);
     // HashMap<String, Datastream> mdRecords =
     // (HashMap<String, Datastream>) cr.getMdRecords();
     // StringBuffer content = new StringBuffer();
     // Iterator<String> namesIter = mdRecords.keySet().iterator();
     // while (namesIter.hasNext()) {
     // String mdRecordName = namesIter.next();
     // try {
     // content.append(renderMdRecord(cr, mdRecordName, commonValues,
     // false));
     // }
     // catch (MdRecordNotFoundException e) {
     // throw new IntegritySystemException(e.getMessage(), e);
     // }
     // }
     // if (!isRoot && content.length() == 0) {
     // return new String("");
     // }
     // if (isRoot) {
     // values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
     // XmlTemplateProvider.TRUE);
     // }
     // values.putAll(commonValues);
     // values.put("mdRecordsHref", cr.getHref()
     // + de.escidoc.core.common.business.Constants.MD_RECORDS_URL_PART);
     // values.put("mdRecordsTitle", "Metadata Records of Component "
     // + cr.getObjid());
     // values.put("mdRecordsContent", content.toString());
     // return ItemXmlProvider.getInstance().getMdRecordsXml(values);
     // }
 
     /**
      * 
      * @param commonValues
      * @param isRoot
      * @return XML representation off //md-records
      * @throws WebserverSystemException
      * @throws EncodingSystemException
      * @throws FedoraSystemException
      * @throws IntegritySystemException
      * @throws TripleStoreSystemException
      */
     public String renderMdRecords(
         final ContentRelationCreate cr, final Map<String, String> commonValues,
         final boolean isRoot) throws WebserverSystemException,
         EncodingSystemException, FedoraSystemException,
         IntegritySystemException, TripleStoreSystemException {
 
         StringBuffer content = new StringBuffer();
 
         Vector<MdRecordCreate> mdRecords = cr.getMetadataRecords();
         if (mdRecords != null) {
             Iterator<MdRecordCreate> mdRecordIt = mdRecords.iterator();
             while (mdRecordIt.hasNext()) {
                 MdRecordCreate mdRecord = mdRecordIt.next();
                 try {
                     String mdRecordContent =
                         renderMdRecord(cr, mdRecord, commonValues, false);
                     content.append(mdRecordContent);
                 }
                 catch (MdRecordNotFoundException e) {
                     throw new WebserverSystemException(
                         "Metadata record previously found in list not found.",
                         e);
                 }
             }
         }
         if (content.length() == 0) {
             return "";
         }
 
         // prepare value map for Velocity
         Map<String, String> values = new HashMap<String, String>();
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                 XmlTemplateProvider.TRUE);
         }
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_HREF,
             Constants.CONTENT_RELATION_URL_BASE + cr.getObjid()
                 + Constants.MD_RECORDS_URL_PART);
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_TITLE,
             "Metadata Records of Content Relation " + cr.getObjid());
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_CONTENT,
             content.toString());
 
         return getXml(MD_RECORDS_RESOURCE_NAME, MD_RECORDS_PATH, values);
     }
 
 }
