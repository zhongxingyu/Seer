 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.xml;
 
 import com.ctc.wstx.exc.WstxParsingException;
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.cache.SchemasCache;
 import de.escidoc.core.common.util.xml.stax.StaxAttributeEscapingWriterFactory;
 import de.escidoc.core.common.util.xml.stax.StaxTextEscapingWriterFactory;
 import de.escidoc.core.common.util.xml.stax.events.AbstractElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.handler.CheckRootElementStaxHandler;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 import de.escidoc.core.common.util.xml.transformer.PoolableTransformerFactory;
 import org.apache.commons.pool.impl.StackKeyedObjectPool;
 import org.codehaus.stax2.XMLOutputFactory2;
 import org.custommonkey.xmlunit.Diff;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.escidoc.core.utils.io.Charsets;
 import org.escidoc.core.utils.io.IOUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import javax.naming.directory.NoSuchAttributeException;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.Validator;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Helper class to support Xml stuff in eSciDoc.<br> This class provides the validation of XML data using specified
  * schemas.<br> The schemas are specified by providing their schema URIs from that {@code Schema} objects are
  * created. These {@code Schema} objects are thread-safe and are cached to prevent unnecessary recreation.
  *
  * @author Torsten Tetteroo
  */
 @Service("common.xml.XmlUtility")
 public final class XmlUtility {
 
     @Autowired
     @Qualifier("common.xml.SchemasCache")
     private SchemasCache schemasCache;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtility.class);
 
     /**
      * Pattern used to detect Object type is in resource type format, e.g. http://escidoc.de/core/01/resources/OrganizationalUnit
      */
     public static final Pattern PATTERN_RESOURCE_OBJECT_TYPE =
         Pattern.compile('^' + Constants.RESOURCES_NS_URI + ".*$");
 
     /**
      * The UTF-8 character encoding used in eSciDoc.
      */
     public static final String CHARACTER_ENCODING = "UTF-8";
 
     /**
      * The Mime-Type for XML.
      */
     public static final String MIME_TYPE_XML = "text/xml";
 
     /**
      * The XML version.
      */
     private static final String XML_VERSION = "1.0";
 
     /**
      * Head of document.
      */
     public static final String DOCUMENT_START =
         "<?xml version=\"" + XML_VERSION + "\" encoding=\"" + CHARACTER_ENCODING + "\"?>\n";
 
     /**
      * CDATA start.
      */
     public static final String CDATA_START = "<![CDATA[";
 
     /**
      * CDATA end.
      */
     public static final String CDATA_END = "]]>";
 
     private static final String AMPERSAND = "&";
 
     private static final String ESC_AMPERSAND = "&amp;";
 
     private static final String LESS_THAN = "<";
 
     private static final String ESC_LESS_THAN = "&lt;";
 
     private static final String GREATER_THAN = ">";
 
     private static final String ESC_GREATER_THAN = "&gt;";
 
     private static final String APOS = "'";
 
     private static final String ESC_APOS = "&apos;";
 
     private static final String QUOT = "\"";
 
     private static final String ESC_QUOT = "&quot;";
 
     private static final Pattern PATTERN_ESCAPE_NEEDED =
         Pattern.compile(AMPERSAND + '|' + LESS_THAN + '|' + GREATER_THAN + '|' + QUOT + '|' + APOS);
 
     private static final Pattern PATTERN_UNESCAPE_NEEDED =
         Pattern.compile(ESC_AMPERSAND + '|' + ESC_LESS_THAN + '|' + ESC_GREATER_THAN + '|' + ESC_QUOT + '|' + ESC_APOS);
 
     private static final Pattern PATTERN_AMPERSAND = Pattern.compile('(' + AMPERSAND + ')');
 
     private static final Pattern PATTERN_LESS_THAN = Pattern.compile('(' + LESS_THAN + ')');
 
     private static final Pattern PATTERN_GREATER_THAN = Pattern.compile('(' + GREATER_THAN + ')');
 
     private static final Pattern PATTERN_QUOT = Pattern.compile('(' + QUOT + ')');
 
     private static final Pattern PATTERN_APOS = Pattern.compile('(' + APOS + ')');
 
     private static final Pattern PATTERN_ESC_AMPERSAND = Pattern.compile('(' + ESC_AMPERSAND + ')');
 
     private static final Pattern PATTERN_ESC_LESS_THAN = Pattern.compile('(' + ESC_LESS_THAN + ')');
 
     private static final Pattern PATTERN_ESC_GREATER_THAN = Pattern.compile('(' + ESC_GREATER_THAN + ')');
 
     private static final Pattern PATTERN_ESC_QUOT = Pattern.compile('(' + ESC_QUOT + ')');
 
     private static final Pattern PATTERN_ESC_APOS = Pattern.compile('(' + ESC_APOS + ')');
 
     private static String containerRestSchemaLocation;
 
     private static String relationsSchemaLocation;
 
     private static String itemRestSchemaLocation;
 
     private static String organizationalUnitRestSchemaLocation;
 
     private static String filterSchemaLocationRest;
 
     private static String organizationalUnitListRestSchemaLocation;
 
     private static String organizationalUnitPathListRestSchemaLocation;
 
     private static String organizationalUnitRefListRestSchemaLocation;
 
     private static String pdpRequestsSchemaLocation;
 
     private static String tmeRequestsSchemaLocation;
 
     private static String containersFilterRestSchemaLocation;
 
     private static String semanticStoreQuerySchemaLocation;
 
     private static String containerMembersFilterRestSchemaLocation;
 
     private static String contextRestSchemaLocation;
 
     private static String contentRelationRestSchemaLocation;
 
     private static String contentModelRestSchemaLocation;
 
     private static String setDefinitionRestSchemaLocation;
 
     private static String contextsFilterSchemaLocationRest;
 
     private static String contextMembersFilterSchemaLocationRest;
 
     private static String xmlSchemaSchemaLocation;
 
     private static String updatePasswordTaskParamSchemaLocation;
 
     private static String revokeGrantTaskParamSchemaLocation;
 
     private static String revokeGrantsTaskParamSchemaLocation;
 
     private static String membersTaskParamSchemaLocation;
 
     private static String assignPidTaskParamSchemaLocation;
 
     private static String idSetTaskParamSchemaLocation;
 
     private static String relationTaskParamSchemaLocation;
 
     private static String statusTaskParamSchemaLocation;
 
     private static String optimiticLockingTaskParamSchemaLocation;
 
     private static String reindexTaskParamSchemaLocation;
 
     private static String stagingFileSchemaLocation;
 
     private static String statisticDataSchemaLocation;
 
     private static String aggregationDefinitionRestSchemaLocation;
 
     private static String reportDefinitionRestSchemaLocation;
 
     private static String reportRestSchemaLocation;
 
     private static String scopeRestSchemaLocation;
 
     private static String reportParametersRestSchemaLocation;
 
     private static String preprocessingInformationSchemaLocation;
 
     private static String stylesheetDefinition;
 
     public static final String CDATA_END_QUOTED = "]]&gt;";
 
     public static final String NAME_ACTIVE = "active";
 
     public static final String NAME_VALUE = "value";
 
     public static final String NAME_TYPE = "type";
 
     public static final String NAME_ASSIGNED_ON = "assigned-on";
 
     public static final String NAME_COMPONENT = "component";
 
     public static final String NAME_COMPONENTS = "components";
 
     public static final String NAME_CREATED_BY = "created-by";
 
     public static final String NAME_CREATION_DATE = "creation-date";
 
     public static final String NAME_SPECIFICATION = "specification";
 
     public static final String NAME_LOCK_OWNER = "lock-owner";
 
     public static final String NAME_MEMBER = "member";
 
     public static final String NAME_MODIFIED_BY = "modified-by";
 
     public static final String NAME_CONTAINER_REF = "container-ref";
 
     public static final String NAME_CONTENT_CATEGORY = "content-category";
 
     public static final String NAME_ITEM_REF = "item-ref";
 
     /**
      * The name of the last modification date attribute.
      */
     public static final String NAME_LAST_MODIFICATION_DATE = "last-modification-date";
 
     public static final String NAME_LATEST_RELEASE_NUMBER = "latest-release-number";
 
     public static final String NAME_LATEST_VERSION_MODIFIED_BY = "latest-version-modified-by";
 
     public static final String NAME_LATEST_VERSION_NUMBER = "latest-version-number";
 
     public static final String NAME_LATEST_VERSION_STATUS = "latest-version-status";
 
     public static final String NAME_LATEST_VERSION_VALID_STATUS = "latest-version-valid-status";
 
     public static final String NAME_PUBLIC_STATUS = "public-status";
 
     public static final String NAME_VALID_STATUS = "valid-status";
 
     public static final String NAME_STATUS = "status";
 
     public static final String NAME_VISIBILITY = "visibility";
 
     public static final String NAME_ITEM = "item";
 
     public static final String NAME_CONTEXT = "context";
 
     public static final String NAME_CONTAINER = "container";
 
     public static final String NAME_CONTENT_MODEL = "content-model";
 
     public static final String NAME_CONTENT_RELATION = "content-relation";
 
     public static final String NAME_USER_ACCOUNT = "user-account";
 
     public static final String NAME_USER_GROUP = "user-group";
 
     public static final String NAME_SET_DEFINITION = "set-definition";
 
     public static final String NAME_ROLE = "role";
 
     public static final String NAME_USER_ID = "userId";
 
     public static final String NAME_GROUP_ID = "groupId";
 
     public static final String NAME_ROLE_ID = "roleId";
 
     public static final String NAME_OBJECT_ID = "objectId";
 
     public static final String NAME_REVOCATION_DATE_FROM = "revocationDateFrom";
 
     public static final String NAME_REVOCATION_DATE_TO = "revocationDateTo";
 
     public static final String NAME_GRANTED_DATE_FROM = "grantedDateFrom";
 
     public static final String NAME_GRANTED_DATE_TO = "grantedDateTo";
 
     public static final String NAME_CREATOR_ID = "creatorId";
 
     public static final String NAME_REVOKER_ID = "revokerId";
 
     // Names of Statistic-Manager Resources
     public static final String NAME_STATISTIC_DATA = "statistic-data";
 
     public static final String NAME_SCOPE = "scope";
 
     public static final String NAME_AGGREGATION_DEFINITION = "aggregation-definition";
 
     public static final String NAME_REPORT = "report";
 
     public static final String NAME_REPORT_DEFINITION = "report-definition";
 
     public static final String NAME_PREPROCESSING_INFORMATION = "preprocessing-information";
 
     // /////////////////////////////////////////
 
     public static final String NAME_OBJECT = "object";
 
     public static final String NAME_ATTRIBUTES = "attributes";
 
     public static final String NAME_ATTRIBUTE = "attribute";
 
     private static final String NAME_OBJID = "objid";
 
     public static final String NAME_NAME = "name";
 
     public static final String NAME_PARAM = "param";
 
     public static final String NAME_PID = "pid";
 
     public static final String NAME_EXTERNAL_ID = "external-id";
 
     public static final String NAME_PROPERTIES = "properties";
 
     public static final String NAME_MDRECORDS = "md-records";
 
     public static final String NAME_MDRECORD = "md-record";
 
     private static final String NAME_RESOURCES = "resources";
 
     public static final String NAME_ID = "id";
 
     public static final String NAME_EMAIL = "email";
 
     public static final String NAME_FILTER = "filter";
 
     public static final String NAME_FORMAT = "format";
 
     public static final String NAME_LOGIN_NAME = "login-name";
 
     public static final String NAME_HANDLE = "handle";
 
     private static final String NAME_HREF = "href";
 
     public static final String NAME_DESCRIPTION = "description";
 
     public static final String NAME_GENRE = "genre";
 
     public static final String NAME_GRANT = "grant";
 
     public static final String NAME_ORGANIZATIONAL_UNIT = "organizational-unit";
 
     private static final String NAME_PARENT_OBJECTS = "parent-objects";
 
     public static final String NAME_PARENTS = "parents";
 
     public static final String NAME_PARENT = "parent";
 
     private static final String NAME_CHILD_OBJECTS = "child-objects";
 
     private static final String NAME_PATH_LIST = "path-list";
 
     public static final String NAME_PREDECESSORS = "predecessors";
 
     public static final String NAME_PREDECESSOR = "predecessor";
 
     private static final String NAME_SUCCESSORS = "successors";
 
     public static final String NAME_ORDER_BY = "order-by";
 
     public static final String NAME_SORTING = "sorting";
 
     public static final String NAME_LIMIT = "limit";
 
     public static final String NAME_OFFSET = "offset";
 
     public static final String NAME_UNSECURED_ACTION = "unsecured-action";
 
     public static final String NAME_UNSECURED_ACTIONS = "unsecured-actions";
 
     public static final String XPATH_USER_ACCOUNT_ORGANIZATIONAL_UNIT =
         "/user-account/properties/organizational-units/organizational-unit";
 
     private static final String BASE_AA = "/aa/";
 
     private static final String BASE_SM = "/statistic/";
 
     private static final String BASE_OUM = "/oum/";
 
     public static final String BASE_OM = "/ir/";
 
     public static final String BASE_ORGANIZATIONAL_UNIT = BASE_OUM + NAME_ORGANIZATIONAL_UNIT + '/';
 
     private static final String BASE_USER_ACCOUNT = BASE_AA + NAME_USER_ACCOUNT + '/';
 
     private static final String BASE_USER_GROUP = BASE_AA + NAME_USER_GROUP + '/';
 
     private static final String BASE_SET_DEFINITION = "/oai/" + NAME_SET_DEFINITION + '/';
 
     public static final String BASE_ROLE = BASE_AA + NAME_ROLE + '/';
 
     public static final String BASE_LOGIN = BASE_AA + "login" + '/';
 
     private static final String BASE_SCOPE = BASE_SM + NAME_SCOPE + '/';
 
     private static final String BASE_AGGREGATION_DEFINITION = BASE_SM + NAME_AGGREGATION_DEFINITION + '/';
 
     private static final String BASE_REPORT_DEFINITION = BASE_SM + NAME_REPORT_DEFINITION + '/';
 
     private static final Map<String, String> REST_SCHEMA_LOCATIONS = new HashMap<String, String>();
 
     public static final String XPATH_USER_ACCOUNT_PROPERTIES = '/' + NAME_USER_ACCOUNT + '/' + NAME_PROPERTIES;
 
     /**
      * The thread-safe compiled Pattern used to extract the object id from an provided URI/Fedora id, e.g. from
      * &lt;info:fedora/escidoc:1&gt; or from http://www.escidoc.de/some/path/escidoc:1
      */
     private static final Pattern PATTERN_GET_ID_FROM_URI_OR_FEDORA_ID = Pattern.compile(".*/([^/>]+)>{0,1}");
 
     /**
      * The thread-safe compiled pattern to extract an object id from an XML representation of a resource, either by
      * getting it from the attribute objid or extracting it from the attribute ...:href.
      */
     private static final Pattern PATTERN_OBJID_FROM_XML = Pattern.compile("(.*?objid=\"|.*?:href=\".*/)(.*?)\".*");
 
     /**
      * The thread-safe compiled pattern to extract a title from an XML representation of a resource.
      */
     private static final Pattern PATTERN_NAME_FROM_XML =
         Pattern
             .compile(".*?<.*?:" + NAME_NAME + ">(.*?)</.*?:" + NAME_NAME + ">.*", Pattern.DOTALL | Pattern.MULTILINE);
 
     private static final Pattern PATTERN_ID_WITHOUT_VERSION = Pattern.compile("([a-zA-Z]+:[a-zA-Z0-9]+):[0-9]+");
 
     private static final Pattern PATTERN_VERSION_NUMBER = Pattern.compile("[a-zA-Z]+:[a-zA-Z0-9]+:([0-9]+)");
 
     private static final String ERR_MSG_MISSING_ATTRIBUTE = "Missing attribute";
 
     private static final StackKeyedObjectPool TRANSFORMER_POOL =
         new StackKeyedObjectPool(new PoolableTransformerFactory());
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected XmlUtility() {
     }
 
     /**
      * Simple proxy method that can decide about the resource type and return the matching schema location.
      *
      * @param type The type of the resource.
      * @return Returns the location of the appropriate schema
      * @throws WebserverSystemException Thrown if retrieve of SchemaLocation failed.
      */
     public static String getSchemaLocationForResource(final ResourceType type) throws WebserverSystemException {
 
         final String schemaLocation;
         switch (type) {
             case ITEM:
                 schemaLocation = getItemSchemaLocation();
                 break;
             case CONTAINER:
                 schemaLocation = getContainerSchemaLocation();
                 break;
             case CONTEXT:
                 schemaLocation = getContextSchemaLocation();
                 break;
             case OU:
                 schemaLocation = getOrganizationalUnitSchemaLocation();
                 break;
             case CONTENT_RELATION:
                 schemaLocation = getContentRelationSchemaLocation();
                 break;
             case CONTENT_MODEL:
                 schemaLocation = getContentModelSchemaLocation();
                 break;
             default:
                 throw new WebserverSystemException("Unknown schema location for resoure type " + type);
         }
 
         return schemaLocation;
     }
 
     /**
      * Gets the organizational unit href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the provided organizational unit id.
      */
     public static String getOrganizationalUnitHref(final String organizationalUnitId) {
 
         return BASE_ORGANIZATIONAL_UNIT + organizationalUnitId;
     }
 
     /**
      * Gets the container md records href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of organizational unit.
      * @return Returns the href for the md records of the provided organizational unit id.
      */
     public static String getOrganizationalUnitMdRecordsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitHref(organizationalUnitId) + '/' + NAME_MDRECORDS;
     }
 
     /**
      * Gets the container properties href for the provided organizational unit id and name.
      *
      * @param organizationalUnitId The id of organizational unit.
      * @param name                 The name of the md record.
      * @return Returns the href for the md record of the provided organizational unit id and name.
      */
     public static String getOrganizationalUnitMdRecordHref(final String organizationalUnitId, final String name) {
 
         return getOrganizationalUnitMdRecordsHref(organizationalUnitId) + '/' + NAME_MDRECORD + '/' + name;
     }
 
     /**
      * Gets the container href for the provided container id.
      * <p/>
      * Use the getHref() methods of the resource objects itself (Container).
      *
      * @param containerId The id of the container.
      * @return Returns the href for the provided container id.
      */
     @Deprecated
     public static String getContainerHref(final String containerId) {
 
         return Constants.CONTAINER_URL_BASE + containerId;
     }
 
     /**
      * Gets the container properties href for the provided container id.
      *
      * @param containerHref The href of container.
      * @return Returns the href for the properties of the provided container id.
      */
     public static String getContainerMdRecordsHref(final String containerHref) {
 
         return containerHref + '/' + NAME_MDRECORDS;
     }
 
     /**
      * Gets the container properties href for the provided container id.
      *
      * @param containerHref The href of the container.
      * @return Returns the href for the properties of the provided container id.
      */
     public static String getContainerPropertiesHref(final String containerHref) {
 
         return containerHref + '/' + NAME_PROPERTIES;
     }
 
     /**
      * Gets the item href for the provided item id.
      *
      * @param itemId The id of the container.
      * @return Returns the href for the provided container id.
      */
     public static String getItemHref(final String itemId) {
 
         return Constants.ITEM_URL_BASE + itemId;
     }
 
     /**
      * Gets the item parents href for the provided item href.
      *
      * @param itemHref The href of the item.
      * @return Returns the href for the data of the provided item id.
      */
     public static String getItemParentsHref(final String itemHref) {
 
         return itemHref + '/' + NAME_RESOURCES + "/parents";
     }
 
     /**
      * Gets the content-relation href for the provided content-relation id.
      *
      * @param contentRelationId The id of the content-relation.
      * @return Returns the href for the provided content-relation id.
      */
     public static String getContentRelationHref(final String contentRelationId) {
         return Constants.CONTENT_RELATION_URL_BASE + contentRelationId;
     }
 
     /**
      * Gets the context href for the provided container id.
      *
      * @param contextId The id of the context.
      * @return Returns the href for the provided context id.
      */
     public static String getContextHref(final String contextId) {
 
         return Constants.CONTEXT_URL_BASE + contextId;
     }
 
     /**
      * Get the properties href for the provided context.
      *
      * @param contextId The id of the context.
      * @return Returns the href of the properties for the provided context id.
      */
     public static String getContextPropertiesHref(final String contextId) {
 
         return getContextHref(contextId) + '/' + NAME_PROPERTIES;
     }
 
     /**
      * Gets the context resources href for the provided context id.
      *
      * @param contextId The id of the context.
      * @return Returns the href for the data of the provided context id.
      */
     public static String getContextResourcesHref(final String contextId) {
 
         return getContextHref(contextId) + '/' + NAME_RESOURCES;
     }
 
     /**
      * Gets the content model href for the provided content model id.
      *
      * @param contentModelId The id of the content model.
      * @return Returns the href for the provided content model id.
      */
     public static String getContentModelHref(final String contentModelId) {
 
         return Constants.CONTENT_MODEL_URL_BASE + contentModelId;
     }
 
     /**
      * Gets the organizational unit properties href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the properties of the provided organizational unit id.
      */
     public static String getOrganizationalUnitPropertiesHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitHref(organizationalUnitId) + '/' + NAME_PROPERTIES;
     }
 
     /**
      * Gets the organizational unit parent-ous href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the parent-ous of the provided organizational unit id.
      */
     public static String getOrganizationalUnitParentsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitHref(organizationalUnitId) + '/' + NAME_PARENTS;
     }
 
     /**
      * Get href of organizational unit predecessor OUs for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the predecessor OUs of the provided organizational unit id.
      */
     public static String getOrganizationalUnitPredecessorsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitHref(organizationalUnitId) + '/' + NAME_PREDECESSORS;
     }
 
     /**
      * Get href of organizational unit successors for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the successor OUs of the provided organizational unit id.
      */
     public static String getOrganizationalUnitSuccessorsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitHref(organizationalUnitId) + '/' + NAME_SUCCESSORS;
     }
 
     /**
      * Gets the container resources href for the provided container id.
      *
      * @param containerHref The href of the container.
      * @return Returns the href for the data of the provided container id.
      */
     public static String getContainerResourcesHref(final String containerHref) {
 
         return containerHref + '/' + NAME_RESOURCES;
     }
 
     /**
      * Gets the container parents href for the provided container id.
      *
      * @param containerHref The href of the container.
      * @return Returns the href for the data of the provided container id.
      */
     public static String getContainerParentsHref(final String containerHref) {
 
         return containerHref + '/' + NAME_RESOURCES + "/parents";
     }
 
     /**
      * Gets the organizational unit resources href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the data of the provided organizational unit id.
      */
     public static String getOrganizationalUnitResourcesHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitHref(organizationalUnitId) + '/' + NAME_RESOURCES;
     }
 
     /**
      * Gets the organizational unit virtual resource parents href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the data of the provided organizational unit id.
      */
     public static String getOrganizationalUnitResourcesParentObjectsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitResourcesHref(organizationalUnitId) + '/' + NAME_PARENT_OBJECTS;
     }
 
     /**
      * Gets the organizational unit virtual resource children href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the data of the provided organizational unit id.
      */
     public static String getOrganizationalUnitResourcesChildObjectsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitResourcesHref(organizationalUnitId) + '/' + NAME_CHILD_OBJECTS;
     }
 
     /**
      * Gets the organizational unit virtual resource path-list href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the data of the provided organizational unit id.
      */
     public static String getOrganizationalUnitResourcesPathListHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitResourcesHref(organizationalUnitId) + '/' + NAME_PATH_LIST;
     }
 
     /**
      * Gets the organizational unit virtual resource successors href for the provided organizational unit id.
      *
      * @param organizationalUnitId The id of the organizational unit.
      * @return Returns the href for the data of the provided organizational unit id.
      */
     public static String getOrganizationalUnitResourcesSuccessorsHref(final String organizationalUnitId) {
 
         return getOrganizationalUnitResourcesHref(organizationalUnitId) + '/' + NAME_SUCCESSORS;
     }
 
     /**
      * Gets the user account href for the provided user account id.
      *
      * @param userAccountId The id of the user account.
      * @return Returns the href for the provided user account id.
      */
     public static String getUserAccountHref(final String userAccountId) {
 
         return BASE_USER_ACCOUNT + userAccountId;
     }
 
     /**
      * Gets the user group href for the provided user group id.
      *
      * @param userGroupId The id of the user group.
      * @return Returns the href for the provided user group id.
      */
     public static String getUserGroupHref(final String userGroupId) {
 
         return BASE_USER_GROUP + userGroupId;
     }
 
     /**
      * Gets the set definition href for the provided set definition id.
      *
      * @param setDefinitionId The id of the set definition.
      * @return Returns the href for the provided set definition id.
      */
     public static String getSetDefinitionHref(final String setDefinitionId) {
 
         return BASE_SET_DEFINITION + setDefinitionId;
     }
 
     /**
      * Gets the user group member href for the provided user group member id.
      *
      * @param userGroupHref     The href of the user group member.
      * @param userGroupMemberId The id of the user group member.
      * @return Returns the href for the provided user group member id.
      */
     public static String getUserGroupMemberHref(final String userGroupHref, final String userGroupMemberId) {
 
         return userGroupHref + "/selectors/selector/" + userGroupMemberId;
     }
 
     /**
      * Get the href for current grants.
      *
      * @param userAccountId objid of user account
      * @return href for the provided grant with user account id
      */
     public static String getCurrentGrantsHref(final String userAccountId) {
 
         return getUserAccountHref(userAccountId) + "/resources/current-grants";
     }
 
     public static String getPreferencesHref(final String userAccountId) {
 
         return getUserAccountHref(userAccountId) + "/resources/preferences";
     }
 
     public static String getAttributesHref(final String userAccountId) {
 
         return getUserAccountHref(userAccountId) + "/resources/attributes";
     }
 
     public static String getUserGroupCurrentGrantsHref(final String userGroupId) {
 
         return getUserGroupHref(userGroupId) + "/resources/current-grants";
     }
 
     public static String getUserAccountResourcesHref(final String userAccountId) {
 
         return getUserAccountHref(userAccountId) + "/resources";
     }
 
     public static String getUserGroupResourcesHref(final String userGroupId) {
 
         return getUserGroupHref(userGroupId) + "/resources";
     }
 
     public static String getUserAccountGrantsHref(final String userAccountId) {
 
         return getUserAccountHref(userAccountId) + "/resources/grants";
     }
 
     public static String getUserGroupGrantsHref(final String userGroupId) {
 
         return getUserGroupHref(userGroupId) + "/resources/grants";
     }
 
     /**
      * Get the href to the specified role grant of the specified user.
      *
      * @param userAccountId The account of the user account owning the grant.
      * @param grantId       The id of the grant.
      * @return The href of the provided role grant.
      */
     public static String getUserAccountGrantHref(final String userAccountId, final String grantId) {
 
         return getUserAccountGrantsHref(userAccountId) + "/grant/" + grantId;
     }
 
     /**
      * Get the href to the specified role grant of the specified user group.
      *
      * @param userGroupId id of the user group owning the grant
      * @param grantId     id of the grant
      * @return The href of the provided role grant.
      */
     public static String getUserGroupGrantHref(final String userGroupId, final String grantId) {
 
         return getUserGroupGrantsHref(userGroupId) + "/grant/" + grantId;
     }
 
     /**
      * Get the href to the specified role.
      *
      * @param roleId The id of the role.
      * @return Returns the href to the role with the specified id.
      */
     public static String getRoleHref(final String roleId) {
 
         return BASE_ROLE + roleId;
     }
 
     /**
      * Get the href to the specified scope.
      *
      * @param scopeId The id of the role.
      * @return Returns the href to the role with the specified id.
      */
     public static String getScopeHref(final String scopeId) {
 
         return BASE_SCOPE + scopeId;
     }
 
     /**
      * Get the href to the specified aggregation-definition.
      *
      * @param aggregationDefinitionId The id of the aggregationDefinition.
      * @return Returns the href to the aggregationDefinitionId with the specified id.
      */
     public static String getAggregationDefinitionHref(final String aggregationDefinitionId) {
 
         return BASE_AGGREGATION_DEFINITION + aggregationDefinitionId;
     }
 
     /**
      * Get the href to the specified report-definition.
      *
      * @param reportDefinitionId The id of the reportDefinition.
      * @return Returns the href to the reportDefinitionId with the specified id.
      */
     public static String getReportDefinitionHref(final String reportDefinitionId) {
 
         return BASE_REPORT_DEFINITION + reportDefinitionId;
     }
 
     /**
      * Adds a new element to the provided {@code XMLStreamWriter} object containing a {@code String} value.
      *
      * @param writer         The {@code XMLStreamWriter} object to add the element to.
      * @param elementName    The name of the new element.
      * @param elementContent The {@code String} that shall be set as the value of the new element.
      * @param namespaceUri   The namespace URI of the new element.
      * @param createEmpty    Flag indicating if a an empty element shall be created if the provided data is
      *                       {@code null} ( {@code true} ), or if the element shall not be created (
      *                       {@code false} ).
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addElement(
         final XMLStreamWriter writer, final String elementName, final String elementContent, final String namespaceUri,
         final boolean createEmpty) throws XMLStreamException {
         if (elementContent == null) {
             if (createEmpty) {
                 writer.writeEmptyElement(namespaceUri, elementName);
             }
         }
         else {
             writer.writeStartElement(namespaceUri, elementName);
             writer.writeCharacters(elementContent);
             writer.writeEndElement();
         }
     }
 
     /**
      * Adds a new element to the provided {@code XMLStreamWriter} object containing a date value.
      *
      * @param writer         The {@code XMLStreamWriter} object to add the element to.
      * @param elementName    The name of the new element.
      * @param elementContent The {@code Date} that shall be set as the value of the new element.
      * @param namespaceUri   The namespace URI of the new element.
      * @param createEmpty    Flag indicating if a an empty element shall be created if the provided data is
      *                       {@code null} ( {@code true} ), or if the element shall not be created (
      *                       {@code false} ).
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addElement(
         final XMLStreamWriter writer, final String elementName, final DateTime elementContent,
         final String namespaceUri, final boolean createEmpty) throws XMLStreamException {
 
         if (elementContent == null) {
             if (createEmpty) {
                 writer.writeEmptyElement(namespaceUri, elementName);
             }
         }
         else {
             writer.writeStartElement(namespaceUri, elementName);
             writer.writeCharacters(elementContent.withZone(DateTimeZone.UTC).toString(Constants.TIMESTAMP_FORMAT));
             writer.writeEndElement();
         }
     }
 
     /**
      * Adds the "last-modification-date" attribute to the provided {@code XMLStreamWriter}.<br> The value of the
      * attribute is set to the value of the provided date.<br> If no date is provided, nothing is added.
      *
      * @param writer       The {@code XMLStreamWriter} object to add the attribute to.
      * @param modifiedDate The date to set as the last modified date.
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addLastModificationDateAttribute(final XMLStreamWriter writer, final DateTime modifiedDate)
         throws XMLStreamException {
 
         if (modifiedDate == null) {
             return;
         }
 
         writer.writeAttribute("last-modification-date", modifiedDate.withZone(DateTimeZone.UTC).toString(
             Constants.TIMESTAMP_FORMAT));
     }
 
     /**
      * Adds the provided object id to the {@code XMLStreamWriter} that has been provided.<br> The object id is
      * added as the attribute "objid".
      *
      * @param writer The {@code XMLStreamWriter} object to add the attribute to.
      * @param objId  The object id to add.
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addObjectId(final XMLStreamWriter writer, final String objId) throws XMLStreamException {
 
         writer.writeAttribute("objid", objId);
     }
 
     /**
      * Adds a new element to the provided {@code XMLStreamWriter} containing a simple xlink with the provided
      * values. The new element is empty.
      *
      * @param writer       The {@code XMLStreamWriter} object to add the element to.
      * @param elementName  The name of the new element.
      * @param xlinkTitle   The title of the xlink contained in the new element.
      * @param xlinkHref    The href of the xlink contained in the new element.
      * @param namespaceUri The namespace URI of the new element.
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addReferencingElement(
         final XMLStreamWriter writer, final String elementName, final String xlinkTitle, final String xlinkHref,
         final String namespaceUri) throws XMLStreamException {
 
         writer.writeStartElement(namespaceUri, elementName);
         addXlinkAttributes(writer, xlinkTitle, xlinkHref);
         writer.writeEndElement();
     }
 
     /**
      * Adds the xlink attributes to the provided {@code Element}.<br> The attribute "xlink:type" is set to
      * "simple", the attributes "xlink:title" and "xlink:href" to the respective provided values.<br> If the provided
      * title is {@code null}. the title attribute is skipped.
      *
      * @param writer     The {@code XMLStreamWriter} object to add the attributes to.
      * @param xlinkTitle The title of the xlink.
      * @param xlinkHref  The href of the xlink.
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addXlinkAttributes(final XMLStreamWriter writer, final String xlinkTitle, final String xlinkHref)
         throws XMLStreamException {
 
         writer.writeAttribute(Constants.XLINK_NS_URI, "type", "simple");
         if (xlinkTitle != null) {
             writer.writeAttribute(Constants.XLINK_NS_URI, "title", xlinkTitle);
         }
         writer.writeAttribute(Constants.XLINK_NS_URI, "href", xlinkHref);
     }
 
     /**
      * Adds the "xml:base" attribute to the provided {@code XMLStreamWriter}.<br> The value of the attribute is set
      * to the value of the configuration property {@code escidoc.baseurl}.
      *
      * @param writer The {@code XMLStreamWriter} object to add the attribute to.
      * @throws IOException        Thrown if the base url cannot be determined.
      * @throws XMLStreamException Thrown in case of an xml stream error.
      */
     public static void addXmlBaseAttribute(final XMLStreamWriter writer) throws XMLStreamException {
 
         writer.writeAttribute(Constants.XML_NS_URI, "base", EscidocConfiguration.getInstance().get(
             EscidocConfiguration.ESCIDOC_CORE_BASEURL));
     }
 
     /**
      * Gets the {@code Schema} from the cache.<br> If none exists for the provided schema URL, it is created and
      * put into the cache.
      *
      * @param schemaUri The schema URI
      * @return Returns the validator for the schema specified by the provided URL.
      * @throws IOException              Thrown in case of an I/O error.
      * @throws WebserverSystemException Thrown if schema can not be parsed.
      */
     public Schema getSchema(final String schemaUri) throws IOException, WebserverSystemException {
 
         return schemasCache.getSchema(schemaUri);
     }
 
     /**
      * Validates the provided XML data using the specified schema and creates a {@code ByteArrayInputStream} for
      * the data.
      *
      * @param xmlData   The xml data.
      * @param schemaUri The URL identifying the schema that shall be used for validation.
      * @return Returns the xml data in a {@code ByteArrayInputStream}.
      * @throws XmlSchemaValidationException Thrown if data in not valid.
      * @throws XmlCorruptedException        Thrown if the XML data cannot be parsed.
      * @throws WebserverSystemException     Thrown in case of any other failure.
      */
     public ByteArrayInputStream createValidatedByteArrayInputStream(final String xmlData, final String schemaUri)
         throws XmlCorruptedException, WebserverSystemException, XmlSchemaValidationException {
 
         final ByteArrayInputStream byteArrayInputStream = convertToByteArrayInputStream(xmlData);
         validate(byteArrayInputStream, schemaUri);
         return byteArrayInputStream;
     }
 
     /**
      * Validates the provided XML data using the specified schema.<br> The provided {@code ByteArrayInputStream} is
      * reset after validation.
      *
      * @param byteArrayInputStream The XML data to validate in an {@code ByteArrayInputStream}.<br> This input
      *                             stream is reset after the validation.
      * @param schemaUri            The URL identifying the schema that shall be used for validation.
      * @throws XmlCorruptedException        Thrown if the XML data cannot be parsed.
      * @throws XmlSchemaValidationException Thrown if both validation fail or only one validation is executed and fails
      * @throws WebserverSystemException     Thrown in any other case.
      */
     public void validate(final ByteArrayInputStream byteArrayInputStream, final String schemaUri)
         throws XmlCorruptedException, XmlSchemaValidationException, WebserverSystemException {
 
         try {
             final Validator validator = getSchema(schemaUri).newValidator();
             validator.validate(new SAXSource(new InputSource(byteArrayInputStream)));
         }
         catch (final SAXParseException e) {
             final String errorMsg =
                 "Error in line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ". " + e.getMessage();
             if (e.getMessage().startsWith("cvc")) {
                 throw new XmlSchemaValidationException(errorMsg, e);
             }
             else {
                 throw new XmlCorruptedException(errorMsg, e);
             }
         }
         catch (final Exception e) {
             throw new WebserverSystemException(e.getMessage(), e);
         }
         finally {
             if (byteArrayInputStream != null) {
                 byteArrayInputStream.reset();
             }
         }
     }
 
     /**
      * Checks, if the provided XML data has the provided root element, afterwards validates the provided XML data using
      * the specified schema.
      *
      * @param xmlData   The XML data to validate.
      * @param schemaUri The URL identifying the schema that shall be used for validation.
      * @param root      Check for this root element.
      * @throws XmlCorruptedException        Thrown if the XML data cannot be parsed.
      * @throws XmlSchemaValidationException Thrown if both validation fail or only one validation is executed and fails
      * @throws WebserverSystemException     Thrown in any other case.
      * @throws XmlParserSystemException     Thrown if the expected root element raise an unexpected error.
      */
     public void validate(final String xmlData, final String schemaUri, final String root) throws XmlCorruptedException,
         XmlSchemaValidationException, WebserverSystemException, XmlParserSystemException {
 
         if (root.length() > 0) {
             checkRootElement(xmlData, root);
         }
         validate(xmlData, schemaUri);
     }
 
     /**
      * Check if the root element has the expected element.
      *
      * @param xmlData      The XML document which is to check.
      * @param expectedRoot The expected root element.
      * @throws XmlCorruptedException    Thrown if the document has not the expected element.
      * @throws XmlParserSystemException Thrown if the expected root element raise an unexpected error.
      */
     private static void checkRootElement(final String xmlData, final String expectedRoot) throws XmlCorruptedException,
         XmlParserSystemException {
 
         final StaxParser sp = new StaxParser();
         final CheckRootElementStaxHandler checkRoot = new CheckRootElementStaxHandler(expectedRoot);
         sp.addHandler(checkRoot);
         try {
             sp.parse(xmlData);
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException("Xml Document has wrong root element, expected '" + expectedRoot + "'.", e);
         }
         catch (final WstxParsingException e) {
             throw new XmlCorruptedException(e.getMessage(), e);
         }
         catch (final WebserverSystemException e) {
             // ignore, check was successful and parsing aborted
         }
         catch (final Exception e) {
             handleUnexpectedStaxParserException("Check for root '" + expectedRoot
                 + "' element raised unexpected exception! ", e);
         }
     }
 
     /**
      * Validates the provided XML data using the specified schema.
      *
      * @param xmlData   The XML data to validate.
      * @param schemaUri The URL identifying the schema that shall be used for validation.
      * @throws XmlCorruptedException        Thrown if the XML data cannot be parsed.
      * @throws XmlSchemaValidationException Thrown if both validation fail or only one validation is executed and fails
      * @throws WebserverSystemException     Thrown in any other case.
      */
     public void validate(final String xmlData, final String schemaUri) throws XmlCorruptedException,
         XmlSchemaValidationException, WebserverSystemException {
 
         validate(convertToByteArrayInputStream(xmlData), schemaUri);
     }
 
     /**
      * Validates the provided XML data using the specified resource type.
      *
      * @param xmlData      The XML data to validate.
      * @param resourceType The resourceType whose schema will be used for validation validation.
      * @throws XmlCorruptedException        Thrown if the XML data cannot be parsed.
      * @throws XmlSchemaValidationException # Thrown if both validation fail or only one validation is executed and
      *                                      fails
      * @throws WebserverSystemException     Thrown in any other case.
      */
     public void validate(final String xmlData, final ResourceType resourceType) throws XmlCorruptedException,
         XmlSchemaValidationException, WebserverSystemException {
         validate(xmlData, getSchemaLocationForResource(resourceType));
 
     }
 
     /**
      * Converts the provided String to a {@code ByteArrayInputStream}.
      *
      * @param str The string to get as {@code ByteArrayInputStream}.
      * @return Returns the {@code ByteArrayInputStream} for the provided string.
      */
     public static ByteArrayInputStream convertToByteArrayInputStream(final String str) {
 
         try {
             return new ByteArrayInputStream(str.getBytes(CHARACTER_ENCODING));
         }
         catch (final UnsupportedEncodingException e) {
             // this should not happen
             return new ByteArrayInputStream("".getBytes(Charsets.UTF8_CHARSET));
         }
     }
 
     /**
      * Converts the provided String to a {@code ByteArrayOutputStream}.
      *
      * @param str The string to get as {@code ByteArrayOutputStream}.
      * @return Returns the {@code ByteArrayOutputStream} for the provided string.
      */
     public static ByteArrayOutputStream convertToByteArrayOutputStream(final String str) {
         final ByteArrayOutputStream stream = new ByteArrayOutputStream();
         try {
             stream.write(str.getBytes(CHARACTER_ENCODING));
         }
         catch (final UnsupportedEncodingException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on writing to stream.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on writing to stream.", e);
             }
         }
         catch (final IOException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on writing to stream.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on writing to stream.", e);
             }
         }
         return stream;
     }
 
     /**
      * Get the objid from an URI/Fedora identifier, e.g. from &lt;info:fedora/escidoc:1&gt;<br/> If the provided value
      * does not match the expected pattern, it is returned as provided. Otherwise, the objid is extracted from it and
      * returned.
      *
      * @param uri The value to get the objid from
      * @return Returns the extracted objid or the provided value.
      */
     public static String getIdFromURI(final String uri) {
 
         if (uri == null) {
             return null;
         }
         final Matcher matcher = PATTERN_GET_ID_FROM_URI_OR_FEDORA_ID.matcher(uri);
         return matcher.find() ? matcher.group(1) : uri;
     }
 
     /**
      * Extracts the objid from the provided resource XML representation.<br/> Either the first occurence of objid="..."
      * is searched and the value is returned, or the first occurence of :href="..." is searched and from this value the
      * objid is extracted and returned.
      *
      * @param resourceXml The XML representation of the resource to get the objid from.
      * @return Returns the extracted objid or {@code null}.
      */
     public static String getIdFromXml(final CharSequence resourceXml) {
 
         final Matcher matcher = PATTERN_OBJID_FROM_XML.matcher(resourceXml);
         return matcher.find() ? matcher.group(2) : null;
     }
 
     /**
      * Extracts the objid from the provided element.<br/> Either the id is fetched from the attribute objid of the
      * provided element. If this fails, it is extracted from the attribute href. If this fials, too, an exception is
      * thrown.
      *
      * @param element The element to get the objid from.
      * @return Returns the objid value.
      * @throws MissingAttributeValueException Thrown if neither an objid nor an href attribute exists.
      */
 
     public static String getIdFromStartElement(final StartElement element) throws MissingAttributeValueException {
 
         try {
             return element.indexOfAttribute(null, NAME_OBJID) == -1 ? getIdFromURI(element.getAttributeValue(
                 Constants.XLINK_NS_URI, NAME_HREF)) : element.getAttributeValue(null, NAME_OBJID);
         }
         catch (final NoSuchAttributeException e) {
             throwMissingAttributeValueException(element, NAME_OBJID + '|' + NAME_HREF);
             return null;
         }
     }
 
     /**
      * Remove version information from given objid.
      *
      * @param objid The objid.
      * @return The objid without version information.
      */
     public static String getObjidWithoutVersion(final String objid) {
 
         String result = objid;
         final Matcher m = PATTERN_ID_WITHOUT_VERSION.matcher(objid);
         if (m.find()) {
             result = m.group(1);
         }
         return result;
     }
 
     /**
      * Extract version number from objid.
      *
      * @param objid The objid.
      * @return The number of version or null.
      */
     public static String getVersionNumberFromObjid(final CharSequence objid) {
         String version = null;
         final Matcher m = PATTERN_VERSION_NUMBER.matcher(objid);
         if (m.find()) {
             version = m.group(1);
         }
         return version;
     }
 
     /**
      * Extracts the name from the provided resource XML representation.<br> The first occurence of
      * &lt;...:name&gt;...&lt;/...:name&gt; is searched and the value is returned.
      *
      * @param resourceXml The XML representation of the resource to get the name from.
      * @return Returns the extracted name (trimmed) or {@code null}.
      */
     public static String extractNameFromXml(final CharSequence resourceXml) {
 
         final Matcher matcher = PATTERN_NAME_FROM_XML.matcher(resourceXml);
         return matcher.find() ? matcher.group(1).trim() : null;
     }
 
     /**
      * Convert the given date into a XML compliant format in UTC.
      *
      * @param date date
      * @return normalized date string
      */
     public static String normalizeDate(final Date date) {
         return new DateTime(date).withZone(DateTimeZone.UTC).toString(Constants.TIMESTAMP_FORMAT);
     }
 
     /**
      * Convert the given date string into a XML compliant format in UTC.
      *
      * @param date date string
      * @return normalized date string
      */
     public static String normalizeDate(final String date) {
         return new DateTime(date).withZone(DateTimeZone.UTC).toString(Constants.TIMESTAMP_FORMAT);
     }
 
     /**
      * Parse the task parameter structure.
      *
      * @param param The parameter structure.
      * @return The handler holding the extracted values.
      * @throws EncodingSystemException If a wrong Encoding is detected.
      * @throws XmlCorruptedException   If the given XML is not valid.
      */
     public static TaskParamHandler parseTaskParam(final String param) throws XmlCorruptedException,
         EncodingSystemException {
         return parseTaskParam(param, true);
     }
 
     public static TaskParamHandler parseTaskParam(final String param, final boolean checkLastModificationDate)
         throws XmlCorruptedException, EncodingSystemException {
 
         final StaxParser staxParser = new StaxParser();
         final TaskParamHandler result = new TaskParamHandler(staxParser);
         if (param != null) {
             result.setCheckLastModificationDate(checkLastModificationDate);
             final ByteArrayInputStream xmlDataIs;
             try {
                 xmlDataIs = new ByteArrayInputStream(param.getBytes(CHARACTER_ENCODING));
             }
             catch (final UnsupportedEncodingException e) {
                 throw new EncodingSystemException(e.getMessage(), e);
             }
             final List<DefaultHandler> handlerChain = new ArrayList<DefaultHandler>();
             handlerChain.add(result);
             staxParser.setHandlerChain(handlerChain);
             try {
                 staxParser.parse(xmlDataIs);
             }
             catch (final Exception e) {
                 throw new XmlCorruptedException(e.getMessage(), e);
             }
 
             staxParser.clearHandlerChain();
         }
         return result;
     }
 
     /**
      * Retrieves the base url for XML schemas for internal validation (i.e. escidoc-core.selfurl +
      * escidoc-core.xsd-path.<br>
      *
      * @return Returns the base url.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getSchemaBaseUrl() {
         return EscidocConfiguration.getInstance().appendToSelfURL(
             EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_XSD_PATH) + '/');
 
     }
 
     /**
      * Gets the stylesheet definition.
      *
      * @return Returns the stylesheet definition. This may be an empty string, if the xslt has not been defined with the
      *         eSciDoc configuration property escidoc.xslt.std.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getStylesheetDefinition() {
         if (stylesheetDefinition == null) {
             String xslt = EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_XSLT_STD);
 
             if (xslt == null || xslt.isEmpty()) {
 
                 // FIXME a non-existing values should be null and not an empty string
                 stylesheetDefinition = "";
             }
             else {
                 // add baseurl, if xslt is not http or https protocol
                 /*
                  * taking base url from configuration is actually an hacking, because the servlet context is missing in the
                  * deep of this methods.
                  */
                 if (!(xslt.startsWith("http://") || xslt.startsWith("https://"))) {
                     String baseurl = EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_BASEURL);
                     if (!baseurl.endsWith("/")) {
                         baseurl += "/";
                     }
 
                     if (xslt.startsWith("./")) {
                         xslt = baseurl + xslt.substring(2, xslt.length());
                     }
                     else if (xslt.startsWith("/")) {
                         xslt = baseurl + xslt.substring(1, xslt.length());
                     }
 
                     stylesheetDefinition = "<?xml-stylesheet type=\"text/xsl\" href=\"" + xslt + "\"?>\n";
                 }
             }
         }
         return stylesheetDefinition;
     }
 
     /**
      * @return Returns the adminDescriptorSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getAdminDescriptorSchemaLocation() {
         if (contextRestSchemaLocation == null) {
             final String subPath = "context/0.4/context.xsd";
             contextRestSchemaLocation = getSchemaBaseUrl() + "rest/" + subPath;
         }
         return contextRestSchemaLocation;
     }
 
     /**
      * @return Returns the containerSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContainerSchemaLocation() {
         if (containerRestSchemaLocation == null) {
             containerRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/container" + Constants.CONTAINER_NS_URI_SCHEMA_VERSION + "/container.xsd";
         }
         return containerRestSchemaLocation;
     }
 
     /**
      * @return Returns the Semantic Store Schema Location.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getSematicStoreQuerySchemaLocation() {
         if (semanticStoreQuerySchemaLocation == null) {
             semanticStoreQuerySchemaLocation = getSchemaBaseUrl() + "rest/common/0.4/semantic-store-query.xsd";
         }
         return semanticStoreQuerySchemaLocation;
     }
 
     /**
      * @return Returns the containerMembersFilterSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContainerMembersFilterSchemaLocation() {
         if (containerMembersFilterRestSchemaLocation == null) {
             containerMembersFilterRestSchemaLocation = getSchemaBaseUrl() + "rest/container/0.3/filter-members.xsd";
         }
         return containerMembersFilterRestSchemaLocation;
     }
 
     /**
      * @return Returns the containersFilterSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContainersFilterSchemaLocation() {
         if (containersFilterRestSchemaLocation == null) {
             containersFilterRestSchemaLocation = getSchemaBaseUrl() + "rest/container/0.3/filter-containers.xsd";
         }
         return containersFilterRestSchemaLocation;
     }
 
     /**
      * @return Returns the content relation schema location.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContentModelSchemaLocation() {
         if (contentModelRestSchemaLocation == null) {
             final String contentModelXsd =
                 "content-model" + Constants.CONTENT_MODEL_NS_URI_SCHEMA_VERSION + "/content-model.xsd";
             contentModelRestSchemaLocation = getSchemaBaseUrl() + "rest/" + contentModelXsd;
         }
         return contentModelRestSchemaLocation;
     }
 
     /**
      * @return Returns the contextSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContextSchemaLocation() {
         if (contextRestSchemaLocation == null) {
             final String contextXsd = "context" + Constants.CONTEXT_NS_URI_SCHEMA_VERSION + "/context.xsd";
             contextRestSchemaLocation = getSchemaBaseUrl() + "rest/" + contextXsd;
         }
         return contextRestSchemaLocation;
     }
 
     /**
      * @return Returns the content relation schema location.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContentRelationSchemaLocation() {
         if (contentRelationRestSchemaLocation == null) {
             final String contentRelationXsd =
                 "content-relation" + Constants.CONTENT_RELATION_NS_URI_SCHEMA_VERSION + "/content-relation.xsd";
             contentRelationRestSchemaLocation = getSchemaBaseUrl() + "rest/" + contentRelationXsd;
         }
         return contentRelationRestSchemaLocation;
     }
 
     /**
      * @return Returns the contextSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getSetDefinitionSchemaLocation() {
         if (setDefinitionRestSchemaLocation == null) {
             final String setDefinitionXsd = "set-definition/0.2/set-definition.xsd";
             setDefinitionRestSchemaLocation = getSchemaBaseUrl() + "rest/" + setDefinitionXsd;
         }
         return setDefinitionRestSchemaLocation;
     }
 
     /**
      * @return Returns the contextsFilterSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContextsFilterSchemaLocation() {
         if (contextsFilterSchemaLocationRest == null) {
             contextsFilterSchemaLocationRest = getSchemaBaseUrl() + "rest/context/0.3/filter-contexts.xsd";
         }
         return contextsFilterSchemaLocationRest;
     }
 
     /**
      * @return Returns the contextMembersFilterSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getContextMembersFilterSchemaLocation() {
         if (contextMembersFilterSchemaLocationRest == null) {
             contextMembersFilterSchemaLocationRest = getSchemaBaseUrl() + "rest/" + "context/0.3/filter-contexts.xsd";
         }
         return contextMembersFilterSchemaLocationRest;
     }
 
     /**
      * @return Returns the grantsSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getGrantsSchemaLocation() {
 
         return getSchemaLocation("user-account/0.5/grants.xsd");
     }
 
     /**
      * @return Returns the preferencesSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getPreferencesSchemaLocation() {
 
         return getSchemaLocation("user-account/0.1/preferences.xsd");
     }
 
     /**
      * @return Returns the preferencesSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getAttributesSchemaLocation() {
 
         return getSchemaLocation("user-account/0.1/attributes.xsd");
     }
 
     /**
      * @return Returns the itemSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getItemSchemaLocation() {
         if (itemRestSchemaLocation == null) {
             itemRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/item" + Constants.ITEM_NS_URI_SCHEMA_VERSION + "/item.xsd";
         }
         return itemRestSchemaLocation;
     }
 
     /**
      * @return Returns the relationsSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getRelationsSchemaLocation() {
         if (relationsSchemaLocation == null) {
             relationsSchemaLocation = getSchemaBaseUrl() + "rest/common/0.3/relations.xsd";
         }
         return relationsSchemaLocation;
     }
 
     /**
      * @return Returns the organizationalUnitSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getOrganizationalUnitSchemaLocation() {
         if (organizationalUnitRestSchemaLocation == null) {
             organizationalUnitRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/" + NAME_ORGANIZATIONAL_UNIT
                     + Constants.ORGANIZATIONAL_UNIT_NS_URI_SCHEMA_VERSION + "/organizational-unit.xsd";
         }
         return organizationalUnitRestSchemaLocation;
     }
 
     /**
      * @return Returns the organizationalUnitListSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getOrganizationalUnitListSchemaLocation() {
         if (organizationalUnitListRestSchemaLocation == null) {
             organizationalUnitListRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/" + NAME_ORGANIZATIONAL_UNIT
                     + Constants.CONTAINER_LIST_NS_URI_SCHEMA_VERSION + "/organizational-unit-list.xsd";
         }
         return organizationalUnitListRestSchemaLocation;
     }
 
     /**
      * @return Returns the organizationalUnitPathListSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getOrganizationalUnitPathListSchemaLocation() {
         if (organizationalUnitPathListRestSchemaLocation == null) {
             organizationalUnitPathListRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/organizational-unit/0.4/organizational-unit-path-list.xsd";
         }
         return organizationalUnitPathListRestSchemaLocation;
     }
 
     /**
      * @return Returns the organizationalUnitRefListSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getOrganizationalUnitRefListSchemaLocation() {
         if (organizationalUnitRefListRestSchemaLocation == null) {
             organizationalUnitRefListRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/organizational-unit/0.4/organizational-unit-ref-list.xsd";
         }
         return organizationalUnitRefListRestSchemaLocation;
     }
 
     /**
      * @return Returns the filterSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getFilterSchemaLocation() {
         if (filterSchemaLocationRest == null) {
             filterSchemaLocationRest = getSchemaBaseUrl() + "rest/common/0.4/filter.xsd";
         }
         return filterSchemaLocationRest;
     }
 
     /**
      * @return Returns the pdpRequestsSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getPdpRequestsSchemaLocation() {
         if (pdpRequestsSchemaLocation == null) {
             pdpRequestsSchemaLocation = getSchemaBaseUrl() + "rest/pdp/0.3/requests.xsd";
         }
         return pdpRequestsSchemaLocation;
     }
 
     /**
      * @return Returns the roleSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getRoleSchemaLocation() {
         return getSchemaLocation("role/0.5/role.xsd");
     }
 
     /**
      * @return Returns the stagingFileSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getStagingFileSchemaLocation() {
 
         if (stagingFileSchemaLocation == null) {
             stagingFileSchemaLocation = getSchemaBaseUrl() + "rest/staging-file/0.3/staging-file.xsd";
         }
         return stagingFileSchemaLocation;
     }
 
     /**
      * @return Returns the tmeRequestsSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getTmeRequestsSchemaLocation() {
         if (tmeRequestsSchemaLocation == null) {
             tmeRequestsSchemaLocation = getSchemaBaseUrl() + "tme/0.1/request.xsd";
         }
         return tmeRequestsSchemaLocation;
     }
 
     /**
      * @return Returns the unsecuredActionsSchemaLocation dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getUnsecuredActionsSchemaLocation() {
 
         return getSchemaLocation("role/0.4/unsecured-actions.xsd");
     }
 
     /**
      * @return Returns the userAccountSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getUserAccountSchemaLocation() {
 
         return getSchemaLocation("user-account" + Constants.USER_ACCOUNT_NS_URI_SCHEMA_VERSION + "/user-account.xsd");
     }
 
     /**
      * @return Returns the addSelectorsSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getAddSelectorsSchemaLocation() {
 
         return getSchemaLocation("user-group/0.6/add-selectors.xsd");
     }
 
     /**
      * @return Returns the removeSelectorsSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getRemoveSelectorsSchemaLocation() {
 
         return getSchemaLocation("user-group/0.6/remove-selectors.xsd");
     }
 
     /**
      * @return Returns the userGroupSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getUserGroupSchemaLocation() {
 
         return getSchemaLocation("user-group/0.6/user-group.xsd");
     }
 
     /**
      * @return Returns the statisticDataSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getStatisticDataSchemaLocation() {
 
         if (statisticDataSchemaLocation == null) {
             statisticDataSchemaLocation = getSchemaBaseUrl() + "rest/statistic-data/0.3/statistic-data.xsd";
         }
         return statisticDataSchemaLocation;
     }
 
     /**
      * @return Returns the aggregationDefinitionSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getAggregationDefinitionSchemaLocation() {
         if (aggregationDefinitionRestSchemaLocation == null) {
             aggregationDefinitionRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/aggregation-definition"
                     + Constants.AGGREGATION_DEFINITION_NS_URI_SCHEMA_VERSION + "/aggregation-definition.xsd";
         }
         return aggregationDefinitionRestSchemaLocation;
     }
 
     /**
      * @return Returns the reportDefinitionSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getReportDefinitionSchemaLocation() {
         if (reportDefinitionRestSchemaLocation == null) {
             reportDefinitionRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/report-definition" + Constants.REPORT_DEFINITION_NS_URI_SCHEMA_VERSION
                     + "/report-definition.xsd";
         }
         return reportDefinitionRestSchemaLocation;
     }
 
     /**
      * @return Returns the scopeSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getScopeSchemaLocation() {
         if (scopeRestSchemaLocation == null) {
             scopeRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/scope" + Constants.SCOPE_NS_URI_SCHEMA_VERSION + "/scope.xsd";
         }
         return scopeRestSchemaLocation;
     }
 
     /**
      * @return Returns the reportSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getReportSchemaLocation() {
         if (reportRestSchemaLocation == null) {
             reportRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/report" + Constants.REPORT_NS_URI_SCHEMA_VERSION + "/report.xsd";
         }
         return reportRestSchemaLocation;
     }
 
     /**
      * @return Returns the ReportParametersSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getReportParametersSchemaLocation() {
         if (reportParametersRestSchemaLocation == null) {
             reportParametersRestSchemaLocation =
                 getSchemaBaseUrl() + "rest/report" + Constants.REPORT_PARAMETERS_NS_URI_SCHEMA_VERSION
                     + "/report-parameters.xsd";
         }
         return reportParametersRestSchemaLocation;
     }
 
     /**
      * @return Returns the PreprocessingInformationSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getPreprocessingInformationSchemaLocation() {
 
         if (preprocessingInformationSchemaLocation == null) {
             preprocessingInformationSchemaLocation =
                 getSchemaBaseUrl() + "rest/preprocessing-information"
                     + Constants.PREPROCESSING_INFORMATION_NS_URI_SCHEMA_VERSION + "/preprocessing-information.xsd";
         }
         return preprocessingInformationSchemaLocation;
     }
 
     /**
      * @return Returns the UpdatePasswordTaskParamSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getUpdatePasswordTaskParamSchemaLocation() {
         if (updatePasswordTaskParamSchemaLocation == null) {
             updatePasswordTaskParamSchemaLocation =
                 getSchemaBaseUrl() + "rest/common/0.1/update-password-task-param.xsd";
         }
         return updatePasswordTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the revoke-grant-task-param schema location.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getRevokeGrantTaskParamSchemaLocation() {
         if (revokeGrantTaskParamSchemaLocation == null) {
             revokeGrantTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/revoke-grant-task-param.xsd";
         }
         return revokeGrantTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the revoke-grants-task-param schema location.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getRevokeGrantsTaskParamSchemaLocation() {
         if (revokeGrantsTaskParamSchemaLocation == null) {
             revokeGrantsTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/revoke-grants-task-param.xsd";
         }
         return revokeGrantsTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the MembersTaskParamSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getMembersTaskParamSchemaLocation() {
         if (membersTaskParamSchemaLocation == null) {
             membersTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/members-task-param.xsd";
         }
         return membersTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the AssignPidTaskParamSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getAssignPidTaskParamSchemaLocation() {
         if (assignPidTaskParamSchemaLocation == null) {
             assignPidTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/assign-pid-task-param.xsd";
         }
         return assignPidTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the DeletionTaskParamSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getIdSetTaskParamSchemaLocation() {
         if (idSetTaskParamSchemaLocation == null) {
             idSetTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/id-set-task-param.xsd";
         }
         return idSetTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the RelationTaskParamSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getRelationTaskParamSchemaLocation() {
         if (relationTaskParamSchemaLocation == null) {
             relationTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/relation-task-param.xsd";
         }
         return relationTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the StatusTaskParamSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getStatusTaskParamSchemaLocation() {
         if (statusTaskParamSchemaLocation == null) {
             statusTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/status-task-param.xsd";
         }
         return statusTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the schema location of optimitic-locking-task-param schema.
      * @throws WebserverSystemException
      *             In case of an error.
      */
     public static String getOptimisticLockingTaskParamSchemaLocation() {
         if (optimiticLockingTaskParamSchemaLocation == null) {
             optimiticLockingTaskParamSchemaLocation =
                 getSchemaBaseUrl() + "rest/common/0.1/optimistic-locking-task-param.xsd";
         }
         return optimiticLockingTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the schema location of reindex-task-param schema.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getReindexTaskParamSchemaLocation() {
         if (reindexTaskParamSchemaLocation == null) {
             reindexTaskParamSchemaLocation = getSchemaBaseUrl() + "rest/common/0.1/reindex-task-param.xsd";
         }
         return reindexTaskParamSchemaLocation;
     }
 
     /**
      * @return Returns the xmlSchemaSchemaLocation.
      * @throws WebserverSystemException In case of an error.
      */
     public static String getXmlSchemaSchemaLocation() {
         if (xmlSchemaSchemaLocation == null) {
             xmlSchemaSchemaLocation = getSchemaBaseUrl() + "common/0.2/xml-schema.xsd";
         }
         return xmlSchemaSchemaLocation;
     }
 
     /**
      * @param commonPart The tailing part of a schema location, e.g. role/0.4/role.xsd.
      * @return Returns the complete schema location for the provided value dependent on UserContext flag isRestAccess.
      * @throws WebserverSystemException In case of an error.
      */
     private static String getSchemaLocation(final String commonPart) {
         String result = REST_SCHEMA_LOCATIONS.get(commonPart);
         if (result == null) {
             result = getSchemaBaseUrl() + "rest/" + commonPart;
             REST_SCHEMA_LOCATIONS.put(commonPart, result);
         }
         return result;
     }
 
     /**
      * Replace forbidden characters in xml content with their escape sequence.<br/> This method escapes &, <, and > in
      * attributes and text content. In attributes, it additionally escapes " and '.
      *
      * @param xmlText The xml text.
      * @return The resulting text with escaped characters.
      */
     public static String escapeForbiddenXmlCharacters(final String xmlText) {
         String result = xmlText;
         if (result != null && PATTERN_ESCAPE_NEEDED.matcher(result).find()) {
             result = PATTERN_AMPERSAND.matcher(result).replaceAll(ESC_AMPERSAND);
             result = PATTERN_LESS_THAN.matcher(result).replaceAll(ESC_LESS_THAN);
             result = PATTERN_GREATER_THAN.matcher(result).replaceAll(ESC_GREATER_THAN);
             result = PATTERN_QUOT.matcher(result).replaceAll(ESC_QUOT);
             result = PATTERN_APOS.matcher(result).replaceAll(ESC_APOS);
         }
 
         return result;
     }
 
     /**
      * Replace all escape sequences for forbidden charcters with their readable.
      *
      * @param xmlText The xml text with escape sequences.
      * @return The resulting text with unescaped characters.
      */
     public static String unescapeForbiddenXmlCharacters(final String xmlText) {
         String result = xmlText;
         if (result != null && PATTERN_UNESCAPE_NEEDED.matcher(result).find()) {
             result = PATTERN_ESC_LESS_THAN.matcher(result).replaceAll(LESS_THAN);
             result = PATTERN_ESC_GREATER_THAN.matcher(result).replaceAll(GREATER_THAN);
             result = PATTERN_ESC_QUOT.matcher(result).replaceAll(QUOT);
             result = PATTERN_ESC_APOS.matcher(result).replaceAll(APOS);
             result = PATTERN_ESC_AMPERSAND.matcher(result).replaceAll(AMPERSAND);
         }
         return result;
     }
 
     /**
      * Throw a uniform escidoc system exception in case of an unexpected exception from the stax parser.
      *
      * @param message A handler specific message added to thrown exception message.
      * @param e       The unexcepcted exception.
      * @throws XmlParserSystemException Thrown in case of an internal system error.
      */
     public static void handleUnexpectedStaxParserException(final String message, final Exception e)
         throws XmlParserSystemException {
         final String text = message != null ? message + e.getMessage() : e.getMessage();
         throw new XmlParserSystemException(text, e);
     }
 
     /**
      * FIXME Often this method is not used but get(ESCIDOC_CORE_BASEURL) directly. And/or there is no such method for
      * every other property!?
      *
      * @return Return the configured escidoc baseurl.
      * @throws WebserverSystemException If an error occurs accessing the escidoc configuration
      */
     public static String getEscidocBaseUrl() {
         return EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_BASEURL);
     }
 
     /**
      * Gets an initilized {@code XMLOutputFactory2} instance.<br/> The returned instance is initialized as follows:
      * <ul> <li>If the provided parameter is set to {@code true}, IS_REPAIRING_NAMESPACES is set to true, i.e. the
      * created writers will automatically repair the namespaces, see {@code XMLOutputFactory} for details.</li>
      * <li>For writing escaped attribute values, the {@link StaxAttributeEscapingWriterFactory} is used<./li> <li>For
      * writing escaped text content, the {@link StaxTextEscapingWriterFactory} is used.</li> </ul>
      *
      * @param repairing Flag indicating if the factory shall create namespace repairing writers ({@code true}) or
      *                  non repairing writers ({@code false}).
      * @return Returns the initalized {@code XMLOutputFactory} instance.
      */
     private static XMLOutputFactory getInitilizedXmlOutputFactory(final boolean repairing) {
         final XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, repairing);
         if (repairing) {
             xmlof.setProperty(XMLOutputFactory2.P_AUTOMATIC_NS_PREFIX, "ext");
         }
         xmlof.setProperty(XMLOutputFactory2.P_ATTR_VALUE_ESCAPER, new StaxAttributeEscapingWriterFactory());
         xmlof.setProperty(XMLOutputFactory2.P_TEXT_ESCAPER, new StaxTextEscapingWriterFactory());
         return xmlof;
     }
 
     /**
      * Creates an {@code XMLStreamWriter} for the provided {@code OutputStream}.
      *
      * @param out The {@code OutputStream} to get the writer for.
      * @return Returns the {@code XMLStreamWriter}.
      * @throws XMLStreamException Thrown in case of an error during creating the writer.
      */
     public static XMLStreamWriter createXmlStreamWriter(final OutputStream out) throws XMLStreamException {
         return getInitilizedXmlOutputFactory(false).createXMLStreamWriter(out);
     }
 
     /**
      * Creates a namespace repairing {@code XMLStreamWriter} for the provided {@code OutputStream}.
      *
      * @param out The {@code OutputStream} to get the writer for.
      * @return Returns the {@code XMLStreamWriter}.
      * @throws XMLStreamException Thrown in case of an error during creating the writer.
      */
     public static XMLStreamWriter createXmlStreamWriterNamespaceRepairing(final OutputStream out)
         throws XMLStreamException {
 
         return getInitilizedXmlOutputFactory(true).createXMLStreamWriter(out);
     }
 
     /**
      * Creates an {@code XMLStreamWriter} for the provided {@code OutputStream}.
      *
      * @param writer The {@code Writer} to get the writer for.
      * @return Returns the {@code XMLStreamWriter}.
      * @throws XMLStreamException Thrown in case of an error during creating the writer.
      */
     public static XMLStreamWriter createXmlStreamWriter(final Writer writer) throws XMLStreamException {
 
         return getInitilizedXmlOutputFactory(false).createXMLStreamWriter(writer);
     }
 
     /**
      * Creates an {@code XMLEventWriter} for the provided {@code Writer}.
      *
      * @param writer The {@code Writer} to get the writer for.
      * @return Returns the {@code XMLEventWriter}.
      * @throws XMLStreamException Thrown in case of an error during creating the writer.
      */
     public static XMLEventWriter createXmlEventWriter(final Writer writer) throws XMLStreamException {
         return getInitilizedXmlOutputFactory(false).createXMLEventWriter(writer);
     }
 
     /**
      * Throws an {@code MissingAttributeValueException}.
      *
      * @param element       The element in that the attribute is missing.
      * @param attributeName The name of the missing attribute.
      * @throws MissingAttributeValueException Throws created exception.
      */
     public static void throwMissingAttributeValueException(final AbstractElement element, final String attributeName)
         throws MissingAttributeValueException {
         throw new MissingAttributeValueException(StringUtility.format(ERR_MSG_MISSING_ATTRIBUTE, element.getPath(),
             attributeName, element.getLocationString()));
     }
 
     /**
      * Create the content of the DC datastream to store in Fedora.
      *
      * @param nsUri       nsUri of the md record. Through this URI is the mapping schema selected.
      * @param mdRecordXml Xml representation of the md record to parse.
      * @param objID       The objid of the Fedora object. A triple is created with this objid.
      * @return The content of the DC datastream or null if content is empty.
      * @throws WebserverSystemException If an error occurs.
      */
     public static String createDC(final String nsUri, final String mdRecordXml, final String objID)
         throws WebserverSystemException {
         return createDC(nsUri, mdRecordXml, objID, null);
     }
 
     /**
      * Create the content of the DC datastream to store in Fedora.
      *
      * @param nsUri          nsUri of the md record. Through this URI is the mapping schema selected.
      * @param mdRecordXml    Xml representation of the md record to parse.
      * @param objID          The objid of the Fedora object. A triple is created with this objid.
      * @param contentModelID The objid of the content-model.
      * @return The content of the DC datastream or null if content is empty.
      * @throws WebserverSystemException If an error occurs.
      */
     public static String createDC(
         final String nsUri, final String mdRecordXml, final CharSequence objID, final String contentModelID)
         throws WebserverSystemException {
         String result = null;
         Transformer t = null;
         final String transformerKey = nsUri + ';' + contentModelID;
         try {
             t = (Transformer) TRANSFORMER_POOL.borrowObject(transformerKey);
             if (objID != null && objID.length() > 0) {
                 t.setParameter("ID", objID);
             }
             else {
                 t.clearParameters();
             }
             final ByteArrayOutputStream out = new ByteArrayOutputStream();
             t.transform(new StreamSource(new ByteArrayInputStream(mdRecordXml.getBytes(CHARACTER_ENCODING))),
                 new StreamResult(out));
 
             result = out.toString(CHARACTER_ENCODING).trim();
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Mapping of Metadata to DC failed.", e);
         }
         finally {
             try {
                 TRANSFORMER_POOL.returnObject(transformerKey, t);
             }
             catch (final Exception e) {
                 if (LOGGER.isWarnEnabled()) {
                     LOGGER.warn("Returning transformer to pool failed.");
                 }
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Returning transformer to pool failed.", e);
                 }
             }
         }
         // check if result is empty
         if (result != null && result.length() == 0) {
             result = null;
         }
         return result;
     }
 
     public static boolean isIdentical(final byte[] xml1, final byte[] xml2) {
         if (xml1 == null) {
             return xml2 == null;
         }
         if (xml2 == null) {
             return xml1 == null;
         }
         return isIdentical(IOUtils.newStringFromBytes(xml1), IOUtils.newStringFromBytes(xml2));
     }
 
     public static boolean isIdentical(final String xml1, final String xml2) {
         if (xml1 == null) {
             return xml2 == null;
         }
         if (xml2 == null) {
             return xml1 == null;
         }
         XMLUnit.setIgnoreComments(true);
         XMLUnit.setIgnoreAttributeOrder(true);
         XMLUnit.setIgnoreWhitespace(true);
         try {
             final Diff diff = XMLUnit.compareXML(xml1, xml2);
             return diff.identical();
         }
         catch (SAXException e) {
             throw new RuntimeException("Error on comparing XML.", e);
         }
         catch (IOException e) {
             throw new RuntimeException("Error on comparing XML.", e);
         }
     }
 
 }
