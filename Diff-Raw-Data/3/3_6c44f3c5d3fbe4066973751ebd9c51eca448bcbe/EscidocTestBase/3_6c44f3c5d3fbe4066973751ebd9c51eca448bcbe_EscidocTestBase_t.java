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
 package de.escidoc.core.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.XMLConstants;
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.apache.xerces.dom.AttrImpl;
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.apache.xpath.XPathAPI;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.MediaType;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.InputSource;
 
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.adm.AdminClient;
 import de.escidoc.core.test.common.client.servlet.interfaces.ResourceHandlerClientInterface;
 import de.escidoc.core.test.common.client.servlet.om.ContainerClient;
 import de.escidoc.core.test.common.client.servlet.om.ContentRelationClient;
 import de.escidoc.core.test.common.client.servlet.om.ContextClient;
 import de.escidoc.core.test.common.client.servlet.om.DeviationClient;
 import de.escidoc.core.test.common.client.servlet.om.IngestClient;
 import de.escidoc.core.test.common.client.servlet.om.ItemClient;
 import de.escidoc.core.test.common.client.servlet.oum.OrganizationalUnitClient;
 import de.escidoc.core.test.common.client.servlet.st.StagingFileClient;
 import de.escidoc.core.test.common.resources.PropertiesProvider;
 import de.escidoc.core.test.common.resources.ResourceProvider;
 import de.escidoc.core.test.common.util.xml.SchemaBaseResourceResolver;
 import de.escidoc.core.test.om.OmTestBase;
 import de.escidoc.core.test.security.client.PWCallback;
 import etm.core.configuration.EtmManager;
 import etm.core.monitor.EtmMonitor;
 import etm.core.monitor.EtmPoint;
 
 /**
  * Base class for Escidoc tests.
  * 
  * @author Michael Schneider
  */
 public abstract class EscidocTestBase {
 
     public static final String VERSION_SUFFIX_SEPARATOR = ":";
 
     private static final String NAME_USER_ACCOUNT_LIST = "user-account-list";
 
     public static final String NAME_USER_ACCOUNT = "user-account";
 
     public static final String NAME_ATTRIBUTE = "attribute";
 
     private static final String NAME_USER_GROUP_LIST = "user-group-list";
 
     public static final String NAME_USER_GROUP = "user-group";
 
     private static final String NAME_SET_DEFINITION = "set-definition";
 
     public static final String NAME_LATEST_VERSION = "latest-version";
 
     public static final String NAME_AGG_DEF = "aggregation-definition";
 
     public static final String NAME_REP_DEF = "report-definition";
 
     public static final String NAME_SCOPE = "scope";
 
     private static Map<String, URL> urlCache = new HashMap<String, URL>();
 
     private static Map<URL, Schema> schemaCache = new HashMap<URL, Schema>();
 
     public static final String DEFAULT_CHARSET = "UTF-8";
 
     private static final Pattern PATTERN_ID_WITHOUT_VERSION = Pattern.compile("([a-zA-Z]+:[0-9]+):[0-9]+");
 
     private static final Logger LOGGER = LoggerFactory.getLogger(EscidocTestBase.class);
 
     protected static final EtmMonitor ETM_MONITOR = EtmManager.getEtmMonitor();
 
     private StagingFileClient stagingFileClient = null;
 
     /**
      * Id of a persistent content type object.
      */
     protected static final String CONTENT_TYPE_ID = "escidoc:persistent4";
 
     /**
      * Id of a persistent context object.
      */
     protected static final String CONTEXT_ID = "escidoc:persistent3";
 
     /**
      * another Id of a persistent context object.
      */
     protected static final String CONTEXT_ID1 = "escidoc:persistent23";
 
     /**
      * another Id of a persistent context object.
      */
     protected static final String CONTEXT_ID2 = "escidoc:persistent5";
 
     /**
      * another Id of a persistent context object.
      */
     protected static final String CONTEXT_ID3 = "escidoc:persistent10";
 
     /**
      * Id of a persistent statistic-scope.
      */
     protected static final String STATISTIC_SCOPE_ID = "escidoc:scope3";
 
     /**
      * Id of a persistent statistic-scope.
      */
     protected static final String STATISTIC_SCOPE_ID1 = "escidoc:scope4";
 
     /**
      * Id of a persistent org unit object.
      */
     protected static final String ORGANIZATIONAL_UNIT_ID = "escidoc:persistent11";
 
     /**
      * Id of a persistent org unit object.
      */
     protected static final String ORGANIZATIONAL_UNIT_ID1 = "escidoc:persistent1";
 
     /**
      * Id of a persistent user-group object containing a list of users.
      */
     protected static final String USER_GROUP_WITH_USER_LIST_ID = "escidoc:testgroupwithuser";
 
     /**
      * Id of a persistent user-group object containing a list of ous.
      */
     protected static final String USER_GROUP_WITH_OU_LIST_ID = "escidoc:testgroupwithorgunit";
 
     /**
      * Id of a persistent user-group object containing a list of groups.
      */
     protected static final String USER_GROUP_WITH_GROUP_LIST_ID = "escidoc:testgroupwithgroup";
 
     /**
      * Id of a persistent user-group object containing an attribute selector.
      */
     protected static final String USER_GROUP_WITH_EXTERNAL_SELECTOR = "escidoc:testgroupwithexternalselector";
 
     protected static final String TEST_SYSTEMADMINISTRATOR_ID1 = "escidoc:testsystemadministrator1";
 
     protected static final String TEST_USER_ACCOUNT_ID = "escidoc:test";
 
     protected static final String TEST_USER_ACCOUNT_ID1 = "escidoc:test1";
 
     protected static final String TEST_DEPOSITOR_ACCOUNT_ID = "escidoc:testdepositor";
 
     protected static final String TEST_USER_GROUP_ID = "escidoc:testgroup";
 
     /**
      * Id of a persistent aggregation-definition.
      */
     protected static final String TEST_AGGREGATION_DEFINITION_ID = "escidoc:aggdef1";
 
     /**
      * Pattern to detect place holder in Velocity templates that are not replaced by values.
      */
     private static final Pattern PATTERN_VELOCITY_PLACEHOLDER = Pattern.compile("\\$\\{.*?\\}");
 
     private static final Pattern PATTERN_VELOCITY_PLACEHOLDER2 = Pattern.compile("\\$esc\\.");
 
     public static final Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("objid=\"([^\"]*)\"");
 
     /**
      * Pattern used in modfyNamespacePrefixes to find and replace prefixes.
      */
     private static final Pattern PATTERN_MODIFY_NAMESPACE_PREFIXES_REPLACE_PREFIXES =
         Pattern.compile("(</{0,1}|[\\s])([a-zA-Z-]+?:[^ =/>]+)", Pattern.DOTALL | Pattern.MULTILINE);
 
     /**
      * Pattern used in modfyNamespacePrefixes to fix namespace declarations after changing prefixes.
      */
     private static final Pattern PATTERN_MODIFY_NAMESPACE_PREFIXES_FIX_NAMESPACE_DECLARATIONS =
         Pattern.compile("prefix-xmlns:([a-zA-Z-].+?)", Pattern.DOTALL | Pattern.MULTILINE);
 
     /**
      * Pattern used in modfyNamespacePrefixes to fix xml namespace declaration.
      */
     private static final Pattern PATTERN_MODIFY_NAMESPACE_PREFIXES_FIX_PREFIX_XML = Pattern.compile("prefix-xml");
 
     public static final String STATE_PENDING = "pending";
 
     public static final String STATE_IN_REVISION = "in-revision";
 
     public static final String STATE_SUBMITTED = "submitted";
 
     public static final String STATE_RELEASED = "released";
 
     public static final String STATE_WITHDRAWN = "withdrawn";
 
     public static final String STATE_LOCKED = "locked";
 
     public static final String STATE_UNLOCKED = "unlocked";
 
     public static final String NAME_ALTERNATIVE = "alternative";
 
     public static final String NAME_START_DATE = "start-date";
 
     public static final String NAME_END_DATE = "end-date";
 
     public static final String NAME_BASE = "base";
 
     public static final String NAME_CITY = "city";
 
     public static final String NAME_COMPONENT = "component";
 
     public static final String NAME_COMPONENTS = "components";
 
     public static final String NAME_COUNTRY = "country";
 
     public static final String NAME_CREATION_DATE = "creation-date";
 
     public static final String NAME_CURRENT_GRANTS = "current-grants";
 
     public static final String NAME_VERSION = "version";
 
     public static final String NAME_LATEST_RELEASE = "latest-release";
 
     public static final String NAME_CREATED_BY = "created-by";
 
     public static final String NAME_MD_RECORDS = "md-records";
 
     public static final String NAME_MD_RECORD = "md-record";
 
     public static final String NAME_DESCRIPTION = "description";
 
     public static final String NAME_IDENTIFIER = "identifier";
 
     public static final String NAME_FAX = "fax";
 
     public static final String NAME_GEO_COORDINATE = "geo-coordinate";
 
     public static final String NAME_GRANT_REMARK = "grant-remark";
 
     public static final String NAME_HAS_CHILDREN = "has-children";
 
     public static final String NAME_HREF = "href";
 
     public static final String NAME_ITEM = "item";
 
     public static final String XPATH_ITEM = "/" + NAME_ITEM;
 
     public static final String NAME_CONTAINER = "container";
 
     public static final String NAME_CONTENT_MODEL = "content-model";
 
     public static final String NAME_CONTENT_RELATION = "content-relation";
 
     public static final String NAME_GRANT = "grant";
 
     public static final String NAME_LAST_MODIFICATION_DATE = "last-modification-date";
 
     public static final String NAME_LOCATION_LATITUDE = "location-latitude";
 
     public static final String NAME_LOCATION_LONGITUDE = "location-longitude";
 
     public static final String NAME_MODIFIED_BY = "modified-by";
 
     public static final String NAME_NAME = "name";
 
     public static final String NAME_OBJID = "objid";
 
     public static final String NAME_ORGANIZATIONAL_UNIT = "organizational-unit";
 
     public static final String NAME_ORGANIZATIONAL_UNITS = "organizational-units";
 
     public static final String NAME_PARENT = "parent";
 
     public static final String NAME_PARENTS = "parents";
 
     public static final String NAME_POSTCODE = "postcode";
 
     public static final String NAME_PROPERTIES = "properties";
 
     public static final String NAME_PUBLIC_STATUS = "public-status";
 
     public static final String NAME_PUBLIC_STATUS_COMMENT = "public-status-comment";
 
     public static final String NAME_CONTEXT = "context";
 
     public static final String NAME_REGION = "region";
 
     public static final String NAME_RESOURCES = "resources";
 
     public static final String NAME_ROLE = "role";
 
     public static final String NAME_TELEPHONE = "telephone";
 
     public static final String NAME_TITLE = "title";
 
     public static final String NAME_URI = "uri";
 
     public static final String NAME_VERSION_STATUS = "status";
 
     public static final String NAME_ORGANIZATION_TYPE = "organization-type";
 
     public static final String NAME_TYPE = "type";
 
     public static final String PART_LAST_MODIFICATION_DATE = "/@" + NAME_LAST_MODIFICATION_DATE;
 
     public static final String PART_OBJID = "/@objid";
 
     public static final String PART_XLINK_TITLE = "/@title";
 
     public static final String PART_XLINK_TYPE = "/@type";
 
     public static final String PART_XLINK_HREF = "/@href";
 
     public static final String PART_XML_BASE = "/@" + NAME_BASE;
 
     public static final String PART_XLINK_NS = "/@xlink";
 
     public static final String GRANTS_PREFIX_TEMPLATES = "prefix-grants";
 
     public static final String GRANTS_PREFIX_ESCIDOC = "grants";
 
     public static final String ORGANIZATIONAL_UNIT_PREFIX_TEMPLATES = "prefix-organizational-unit";
 
     public static final String ORGANIZATIONAL_UNIT_PREFIX_ESCIDOC = "organizational-unit";
 
     public static final String ROLE_PREFIX_TEMPLATES = "prefix-role";
 
     public static final String ROLE_PREFIX_ESCIDOC = "role";
 
     public static final String SREL_PREFIX_TEMPLATES = "prefix-srel";
 
     public static final String SREL_PREFIX_ESCIDOC = "srel";
 
     public static final String USER_ACCOUNT_PREFIX_TEMPLATES = "prefix-user-account";
 
     public static final String USER_GROUP_PREFIX_TEMPLATES = "prefix-user-group";
 
     public static final String USER_ACCOUNT_PREFIX_ESCIDOC = NAME_USER_ACCOUNT;
 
     public static final String USER_GROUP_PREFIX_ESCIDOC = NAME_USER_GROUP;
 
     public static final String CONTAINER_NS_URI = "http://www.escidoc.de/schemas/container/0.7";
 
     public static final String PROPERTIES_NS_URI_04 = "http://escidoc.de/core/01/properties/";
 
     public static final String PROPERTIES_FILTER_PREFIX = "/properties/";
 
     public static final String RDF_NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
 
     public static final String RDF_TYPE_NS_URI = RDF_NS_URI + "type";
 
     public static final String STRUCTURAL_RELATIONS_NS_URI = "http://escidoc.de/core/01/structural-relations/";
 
     public static final String STRUCTURAL_RELATIONS_FILTER_PREFIX = "/structural-relations/";
 
     public static final String RESOURCES_NS_URI = "http://escidoc.de/core/01/resources/";
 
     public static final String TYPE_FEDORA_OBJECT_URI = "info:fedora/fedora-system:def/model#FedoraObject";
 
     public static final String CONTEXT_NS_URI = "http://www.escidoc.de/schemas/context/0.6";
 
     public static final String CONTENT_TYPE_NS_URI = "http://www.escidoc.de/schemas/content-type/0.2";
 
     public static final String DC_NS_URI = "http://purl.org/dc/elements/1.1/";
 
     public static final String DCTERMS_NS_URI = "http://purl.org/dc/terms/";
 
     public static final String GRANTS_NS_URI = "http://www.escidoc.de/schemas/grants/0.5";
 
     public static final String INTERNAL_METADATA_NS_URI = "http://www.escidoc.de/schemas/internalmetadata/0.2";
 
     public static final String ITEM_NS_URI = "http://www.escidoc.de/schemas/item/0.6";
 
     public static final String COMPONENTS_NS_URI = "http://www.escidoc.de/schemas/components/0.7";
 
     public static final String ITEM_PREFIX_TEMPLATES = "prefix-escidocItem";
 
     public static final String MEMBER_LIST_NS_URI = "http://www.escidoc.de/schemas/memberlist/0.6";
 
     public static final String MEMBER_REF_LIST_NS_URI = "http://www.escidoc.de/schemas/memberreflist/0.2";
 
     public static final String METADATARECORDS_NS_URI = "http://www.escidoc.de/schemas/metadatarecords/0.2";
 
     public static final String METADATA_NS_URI = "http://www.escidoc.de/schemas/metadata/0.2";
 
     public static final String ORGANIZATIONAL_UNIT_NS_URI = "http://www.escidoc.de/schemas/organizationalunit/0.8";
 
     public static final String PROPERTIES_NS_URI = "http://www.escidoc.de/schemas/properties/0.2";
 
     public static final String RELATIONS_NS_URI = "http://www.escidoc.de/schemas/relations/0.2";
 
     public static final String REQUESTS_NS_URI = "http://www.escidoc.de/schemas/pdp/0.2/requests";
 
     public static final String RESULTS_NS_URI = "http://www.escidoc.de/schemas/pdp/0.2/results";
 
     public static final String ROLE_NS_URI = "http://www.escidoc.de/schemas/role/0.2";
 
     public static final String SREL_NS_URI = "http://escidoc.de/core/01/structural-relations/";
 
     public static final String STAGING_FILE_NS_URI = "http://www.escidoc.de/schemas/stagingfile/0.2";
 
     public static final String SCHEMA_NS_URI = "http://www.escidoc.de/schemas/xml-schema/0.2";
 
     public static final String STRUCT_MAP_NS_URI = "http://www.escidoc.de/schemas/structmap/0.4";
 
     public static final String USER_ACCOUNT_NS_URI = "http://www.escidoc.de/schemas/useraccount/0.7";
 
     public static final String USER_ACCOUNT_PREFERENCE_NS_URI = "http://www.escidoc.de/schemas/preferences/0.1";
 
     public static final String USER_ACCOUNT_ATTRIBUTE_NS_URI = "http://www.escidoc.de/schemas/attributes/0.1";
 
     public static final String USER_GROUP_NS_URI = "http://www.escidoc.de/schemas/usergroup/0.6";
 
     public static final String XACML_CONTEXT_NS_URI = "urn:oasis:names:tc:xacml:1.0:context";
 
     public static final String XACML_POLICY_NS_URI = "urn:oasis:names:tc:xacml:1.0:policy";
 
     public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
 
     public static final String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";
 
     public static final String XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";
 
     public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
 
     public static final String UNKNOWN_ID = "escidoc:-1";
 
     public static final String TEMPLATE_BASE_PATH = "templates";
 
     public static final String TEMPLATE_EXAMPLE_PATH = "examples/escidoc";
 
     public static final String TEMPLATE_AA_PATH = TEMPLATE_BASE_PATH + "/aa/template";
 
     public static final String TEMPLATE_ST_PATH = TEMPLATE_BASE_PATH + "/st/template";
 
     public static final String TEMPLATE_REQUESTS_PATH = TEMPLATE_AA_PATH + "/requests";
 
     public static final String TEMPLATE_AA_ITEM_PATH = TEMPLATE_AA_PATH + "/item";
 
     public static final String TEMPLATE_ROLE_PATH = TEMPLATE_AA_PATH + "/role";
 
     public static final String TEMPLATE_CMM_PATH = TEMPLATE_BASE_PATH + "/cmm/template";
 
     public static final String TEMPLATE_OM_PATH = TEMPLATE_BASE_PATH + "/om/template";
 
     public static final String TEMPLATE_OAI_PATH = TEMPLATE_BASE_PATH + "/oai/template";
 
     public static final String TEMPLATE_OUM_PATH = TEMPLATE_BASE_PATH + "/oum/template";
 
     public static final String TEMPLATE_ADMIN_DESCRIPTOR_PATH = TEMPLATE_OM_PATH + "/admindescriptor";
 
     public static final String TEMPLATE_OM_COMMON_PATH = TEMPLATE_OM_PATH + "/common";
 
     public static final String TEMPLATE_CONTAINER_PATH = TEMPLATE_OM_PATH + "/container";
 
     public static final String TEMPLATE_CONTAINER_SEARCH_PATH = TEMPLATE_CONTAINER_PATH + "/search";
 
     public static final String TEMPLATE_XML_SCHEMA_PATH = TEMPLATE_OM_PATH + "/xmlschema";
 
     public static final String TEMPLATE_CONTEXT_PATH = TEMPLATE_OM_PATH + "/context";
 
     public static final String TEMPLATE_CONTENT_MODEL_PATH = TEMPLATE_CMM_PATH + "/content-model";
 
     public static final String TEMPLATE_CONTEXT_VERSION = "0.4";
 
     public static final String TEMPLATE_ITEM_PATH = TEMPLATE_OM_PATH + "/item";
 
     public static final String TEMPLATE_ITEM_SEARCH_PATH = TEMPLATE_ITEM_PATH + "/search";
 
     public static final String TEMPLATE_ITEM_SEARCH_ADMIN_PATH = TEMPLATE_ITEM_SEARCH_PATH + "/admin";
 
     public static final String TEMPLATE_INGEST_PATH = TEMPLATE_OM_PATH + "/ingest";
 
     public static final String TEMPLATE_LANGUAGE_ITEMS_PATH = TEMPLATE_ITEM_PATH + "/language";
 
     public static final String TEMPLATE_TME_PATH = TEMPLATE_BASE_PATH + "/tme/template/tme/0.1";
 
     public static final String TEMPLATE_LICENSE_TYPE_PATH = TEMPLATE_OM_PATH + "/licensetype";
 
     public static final String TEMPLATE_ORGANIZATIONAL_UNIT_PATH = TEMPLATE_OUM_PATH + "/organizationalunit/0.8";
 
     public static final String TEMPLATE_SB_PATH = TEMPLATE_BASE_PATH + "/sb/template";
 
     public static final String TEMPLATE_SB_ORGANIZATIONAL_UNIT_PATH = TEMPLATE_SB_PATH + "/organizationalunit";
 
     public static final String TEMPLATE_SB_CONTEXT_PATH = TEMPLATE_SB_PATH + "/context";
 
     public static final String TEMPLATE_SB_CONTENT_MODEL_PATH = TEMPLATE_SB_PATH + "/contentmodel";
 
     public static final String TEMPLATE_SB_CONTENT_RELATION_PATH = TEMPLATE_SB_PATH + "/contentrelation";
 
     public static final String TEMPLATE_USER_ACCOUNT_PATH = TEMPLATE_AA_PATH + "/useraccount";
 
     public static final String TEMPLATE_USER_GROUP_PATH = TEMPLATE_AA_PATH + "/usergroup";
 
     public static final String TEMPLATE_SET_DEFINITION_PATH = TEMPLATE_OAI_PATH + "/setdefinition";
 
     public static final String TEMPLATE_OAIPROVIDERTEST_ITEM_PATH = TEMPLATE_OAI_PATH + "/item";
 
     public static final String TEMPLATE_OAIPROVIDERTEST_CONTAINER_PATH = TEMPLATE_OAI_PATH + "/container";
 
     public static final String TEMPLATE_SM_PATH = TEMPLATE_BASE_PATH + "/sm/template";
 
     public static final String TEMPLATE_STAT_DATA_PATH = TEMPLATE_SM_PATH + "/statisticdata";
 
     public static final String TEMPLATE_AGG_DEF_PATH = TEMPLATE_SM_PATH + "/aggregationdefinition";
 
     public static final String TEMPLATE_REP_DEF_PATH = TEMPLATE_SM_PATH + "/reportdefinition";
 
     public static final String TEMPLATE_REPORT_PATH = TEMPLATE_SM_PATH + "/report";
 
     public static final String TEMPLATE_SCOPE_PATH = TEMPLATE_SM_PATH + "/scope";
 
     public static final String TEMPLATE_REP_PARAMETERS_PATH = TEMPLATE_SM_PATH + "/reportparameters";
 
     public static final String TEMPLATE_PREPROCESSING_INFO_PATH = TEMPLATE_SM_PATH + "/preprocessinginformation";
 
     public static final String TEMPLATE_ST_ITEM_PATH = TEMPLATE_ST_PATH + "/item";
 
     public static final String ESCIDOC_OBJECTS_SAVE_PATH = "build/escidoc";
 
     public static final String VAR_COMPONENT_ID = "\\$\\{COMPONENT_ID\\}";
 
     public static final String VAR_MD_RECORD_NAME = "\\$\\{MD_RECORD_NAME\\}";
 
     public static final String STATUS_PENDING = "pending";
 
     public static final String STATUS_SUBMITTED = "submitted";
 
     public static final String STATUS_IN_REVISION = "in-revision";
 
     public static final String STATUS_RELEASED = "released";
 
     public static final String STATUS_WITHDRAWN = "withdrawn";
 
     public static final String CONTEXT_STATUS_CREATED = "created";
 
     public static final String CONTEXT_STATUS_OPENED = "opened";
 
     public static final String CONTEXT_STATUS_CLOSED = "closed";
 
     public static final String CONTEXT_STATUS_DELETED = "deleted";
 
     public static final String CONTENT_MODEL_STATUS_CREATED = "created";
 
     public static final String CONTENT_MODEL_STATUS_UPDATED = "updated";
 
     public static final String CONTENT_MODEL_STATUS_DELETED = "deleted";
 
     public static final String ORGANIZATIONAL_UNIT_STATUS_CREATED = "created";
 
     public static final String ORGANIZATIONAL_UNIT_STATUS_OPENED = "opened";
 
     public static final String ORGANIZATIONAL_UNIT_STATUS_CLOSED = "closed";
 
     public static final String ORGANIZATIONAL_UNIT_STATUS_DELETED = "deleted";
 
     protected static final String WITHDRAW_COMMENT = "This is a &lt; withdraw comment.";
 
     private static Schema resourcesSchema;
 
     protected static Schema stagingFileSchema;
 
     protected static String startTimestamp = getNowAsTimestamp();
 
     /**
      * Xlink namespace prefix in templates.
      */
     public static final String XLINK_PREFIX_TEMPLATES = "prefix-xlink";
 
     /**
      * Xlink namespace prefix in documents retrieved from the eSciDoc.
      */
     public static final String XLINK_PREFIX_ESCIDOC = "xlink";
 
     /**
      * Xlink namespace declaration in templates.
      */
     public static final String XLINK_NS_DECL_TEPLATES =
         "xmlns:" + XLINK_PREFIX_TEMPLATES + "=\"" + XLINK_NS_URI + "\" ";
 
     /**
      * Xlink namespace declaration in documents retrieved from the eSciDoc.
      */
     public static final String XLINK_NS_DECL_ESCIDOC = "xmlns:" + XLINK_PREFIX_ESCIDOC + "=\"" + XLINK_NS_URI + "\" ";
 
     /**
      * Prefix and name of xlink href attribute in templates.
      */
     public static final String XLINK_HREF_TEMPLATES = XLINK_PREFIX_TEMPLATES + ":href";
 
     /**
      * Prefix and name of xlink href attribute in documents retrieved from the eSciDoc.
      */
     public static final String XLINK_HREF_ESCIDOC = XLINK_PREFIX_ESCIDOC + ":href";
 
     /**
      * Prefix and name of objid attribute in documents retrieved from the eSciDoc.
      */
     public static final String ID_ESCIDOC = "objid";
 
     /**
      * Prefix and name of xlink title attribute in templates.
      */
     public static final String XLINK_TITLE_TEMPLATES = XLINK_PREFIX_TEMPLATES + ":title";
 
     /**
      * Prefix and name of xlink title attribute in documents retrieved from the eSciDoc.
      */
     public static final String XLINK_TITLE_ESCIDOC = XLINK_PREFIX_ESCIDOC + ":title";
 
     /**
      * Prefix and name of xlink type attribute in templates.
      */
     public static final String XLINK_TYPE_TEMPLATES = XLINK_PREFIX_TEMPLATES + ":type";
 
     /**
      * Prefix and name of xlink type attribute in documents retrieved from the eSciDoc.
      */
     public static final String XLINK_TYPE_ESCIDOC = XLINK_PREFIX_ESCIDOC + ":type";
 
     public static final String XPATH_ATTRIBUTE = "/" + NAME_ATTRIBUTE;
 
     public static final String XPATH_RESOURCES = "/resources";
 
     public static final String XPATH_RESOURCES_BASE = XPATH_RESOURCES + "/@" + NAME_BASE;
 
     public static final String XPATH_RESOURCES_XLINK_HREF = XPATH_RESOURCES + "/@" + NAME_HREF;
 
     public static final String XPATH_RESOURCES_XLINK_TITLE = XPATH_RESOURCES + "/@" + NAME_TITLE;
 
     public static final String XPATH_RESOURCES_XLINK_TYPE = XPATH_RESOURCES + "/@" + NAME_TYPE;
 
     public static final String XPATH_RESOURCES_CURRENT_GRANTS = XPATH_RESOURCES + "/" + NAME_CURRENT_GRANTS;
 
     public static final String XPATH_RESOURCES_CURRENT_GRANTS_XLINK_HREF =
         XPATH_RESOURCES_CURRENT_GRANTS + "/@" + NAME_HREF;
 
     public static final String XPATH_RESOURCES_CURRENT_GRANTS_XLINK_TYPE =
         XPATH_RESOURCES_CURRENT_GRANTS + "/@" + NAME_TYPE;
 
     public static final String XPATH_RESOURCES_CURRENT_GRANTS_XLINK_TITLE =
         XPATH_RESOURCES_CURRENT_GRANTS + "/@" + NAME_TITLE;
 
     public static final String XPATH_USER_ACCOUNT = "/" + NAME_USER_ACCOUNT;
 
     public static final String XPATH_USER_ACCOUNT_LIST = "/" + NAME_USER_ACCOUNT_LIST;
 
     public static final String XPATH_USER_ACCOUNT_LIST_USER_ACCOUNT = XPATH_USER_ACCOUNT_LIST + "/" + NAME_USER_ACCOUNT;
 
     public static final String XPATH_USER_ACCOUNT_OBJID = XPATH_USER_ACCOUNT + "/@objid";
 
     public static final String XPATH_USER_ACCOUNT_XLINK_TITLE = XPATH_USER_ACCOUNT + "/@title";
 
     public static final String XPATH_USER_ACCOUNT_LAST_MOD_DATE = XPATH_USER_ACCOUNT + "/@last-modification-date";
 
     public static final String XPATH_USER_ACCOUNT_XLINK_TYPE = XPATH_USER_ACCOUNT + "/@type";
 
     public static final String XPATH_USER_ACCOUNT_XLINK_HREF = XPATH_USER_ACCOUNT + "/@href";
 
     public static final String XPATH_USER_ACCOUNT_PROPERTIES = XPATH_USER_ACCOUNT + "/properties";
 
     public static final String XPATH_USER_ACCOUNT_CREATED_BY = XPATH_USER_ACCOUNT_PROPERTIES + "/" + NAME_CREATED_BY;
 
     public static final String XPATH_USER_ACCOUNT_MODIFIED_BY = XPATH_USER_ACCOUNT_PROPERTIES + "/" + NAME_MODIFIED_BY;
 
     public static final String XPATH_USER_ACCOUNT_MODIFIED_BY_OBJID =
         XPATH_USER_ACCOUNT_MODIFIED_BY + "/@" + NAME_OBJID;
 
     public static final String XPATH_USER_ACCOUNT_MODIFIED_BY_XLINK_HREF =
         XPATH_USER_ACCOUNT_MODIFIED_BY + "/@" + NAME_HREF;
 
     public static final String XPATH_USER_ACCOUNT_MODIFIED_BY_XLINK_TITLE =
         XPATH_USER_ACCOUNT_MODIFIED_BY + "/@" + NAME_TITLE;
 
     public static final String XPATH_USER_ACCOUNT_MODIFIED_BY_XLINK_TYPE =
         XPATH_USER_ACCOUNT_MODIFIED_BY + "/@" + NAME_TYPE;
 
     public static final String XPATH_USER_ACCOUNT_CREATED_BY_OBJID = XPATH_USER_ACCOUNT_CREATED_BY + "/@" + NAME_OBJID;
 
     public static final String XPATH_USER_ACCOUNT_CREATED_BY_XLINK_HREF =
         XPATH_USER_ACCOUNT_CREATED_BY + "/@" + NAME_HREF;
 
     public static final String XPATH_USER_ACCOUNT_CREATED_BY_XLINK_TITLE =
         XPATH_USER_ACCOUNT_CREATED_BY + "/@" + NAME_TITLE;
 
     public static final String XPATH_USER_ACCOUNT_CREATED_BY_XLINK_TYPE =
         XPATH_USER_ACCOUNT_CREATED_BY + "/@" + NAME_TYPE;
 
     public static final String XPATH_USER_ACCOUNT_CREATION_DATE = XPATH_USER_ACCOUNT_PROPERTIES + "/creation-date";
 
     public static final String XPATH_USER_ACCOUNT_RESOURCES = XPATH_USER_ACCOUNT + "/" + NAME_RESOURCES;
 
     public static final String XPATH_USER_ACCOUNT_RESOURCES_XLINK_HREF = XPATH_USER_ACCOUNT_RESOURCES + PART_XLINK_HREF;
 
     public static final String XPATH_USER_ACCOUNT_RESOURCES_XLINK_TITLE =
         XPATH_USER_ACCOUNT_RESOURCES + PART_XLINK_TITLE;
 
     public static final String XPATH_USER_ACCOUNT_RESOURCES_XLINK_TYPE = XPATH_USER_ACCOUNT_RESOURCES + PART_XLINK_TYPE;
 
     public static final String XPATH_USER_ACCOUNT_CURRENT_GRANTS =
         XPATH_USER_ACCOUNT_RESOURCES + "/" + NAME_CURRENT_GRANTS;
 
     public static final String XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_TYPE =
         XPATH_USER_ACCOUNT_CURRENT_GRANTS + PART_XLINK_TYPE;
 
     public static final String XPATH_USER_ACCOUNT_ACTIVE = XPATH_USER_ACCOUNT_PROPERTIES + "/active";
 
     public static final String XPATH_USER_ACCOUNT_LOGINNAME = XPATH_USER_ACCOUNT_PROPERTIES + "/login-name";
 
     public static final String XPATH_USER_ACCOUNT_NAME = XPATH_USER_ACCOUNT_PROPERTIES + "/name";
 
     public static final String XPATH_USER_GROUP = "/" + NAME_USER_GROUP;
 
     public static final String XPATH_SET_DEFINITION = "/" + NAME_SET_DEFINITION;
 
     public static final String XPATH_SRW_RESPONSE_ROOT = "/searchRetrieveResponse";
 
     public static final String XPATH_SRW_RESPONSE_RECORD = XPATH_SRW_RESPONSE_ROOT + "/records/record";
 
     public static final String XPATH_SRW_RESPONSE_OBJECT_SUBPATH = "/recordData/search-result-record/";
 
     public static final String XPATH_SRW_RESPONSE_OBJECT =
         XPATH_SRW_RESPONSE_RECORD + XPATH_SRW_RESPONSE_OBJECT_SUBPATH;
 
     public static final String XPATH_USER_GROUP_LIST = "/" + NAME_USER_GROUP_LIST;
 
     public static final String XPATH_USER_GROUP_LIST_USER_GROUP = XPATH_USER_GROUP_LIST + "/" + NAME_USER_GROUP;
 
     public static final String XPATH_SRW_USER_GROUP_LIST_USER_GROUP = XPATH_SRW_RESPONSE_OBJECT + NAME_USER_GROUP;
 
     public static final String XPATH_SRW_CONTAINER_LIST_CONTAINER = XPATH_SRW_RESPONSE_OBJECT + NAME_CONTAINER;
 
     public static final String XPATH_SRW_ITEM_LIST_ITEM = XPATH_SRW_RESPONSE_OBJECT + NAME_ITEM;
 
     public static final String XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION =
         XPATH_SRW_RESPONSE_OBJECT + NAME_SET_DEFINITION;
 
     public static final String XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_ORGANIZATIONAL_UNIT =
         XPATH_SRW_RESPONSE_OBJECT + NAME_ORGANIZATIONAL_UNIT;
 
     public static final String XPATH_SRW_CONTENT_MODEL_LIST_CONTENT_MODEL =
         XPATH_SRW_RESPONSE_OBJECT + NAME_CONTENT_MODEL;
 
     public static final String XPATH_USER_GROUP_OBJID = XPATH_USER_GROUP + "/@objid";
 
     public static final String XPATH_USER_GROUP_XLINK_TITLE = XPATH_USER_GROUP + "/@title";
 
     public static final String XPATH_USER_GROUP_LAST_MOD_DATE = XPATH_USER_GROUP + "/@last-modification-date";
 
     public static final String XPATH_USER_GROUP_XLINK_TYPE = XPATH_USER_GROUP + "/@type";
 
     public static final String XPATH_USER_GROUP_XLINK_HREF = XPATH_USER_GROUP + "/@href";
 
     public static final String XPATH_USER_GROUP_PROPERTIES = XPATH_USER_GROUP + "/properties";
 
     public static final String XPATH_USER_GROUP_SELECTORS = XPATH_USER_GROUP + "/selectors";
 
     public static final String XPATH_USER_GROUP_SELECTOR = XPATH_USER_GROUP + "/selectors/selector";
 
     public static final String XPATH_USER_GROUP_CREATED_BY = XPATH_USER_GROUP_PROPERTIES + "/" + NAME_CREATED_BY;
 
     public static final String XPATH_USER_GROUP_MODIFIED_BY = XPATH_USER_GROUP_PROPERTIES + "/" + NAME_MODIFIED_BY;
 
     public static final String XPATH_USER_GROUP_MODIFIED_BY_OBJID = XPATH_USER_GROUP_MODIFIED_BY + "/@" + NAME_OBJID;
 
     public static final String XPATH_USER_GROUP_MODIFIED_BY_XLINK_HREF =
         XPATH_USER_GROUP_MODIFIED_BY + "/@" + NAME_HREF;
 
     public static final String XPATH_USER_GROUP_MODIFIED_BY_XLINK_TITLE =
         XPATH_USER_GROUP_MODIFIED_BY + "/@" + NAME_TITLE;
 
     public static final String XPATH_USER_GROUP_MODIFIED_BY_XLINK_TYPE =
         XPATH_USER_GROUP_MODIFIED_BY + "/@" + NAME_TYPE;
 
     public static final String XPATH_USER_GROUP_CREATED_BY_OBJID = XPATH_USER_GROUP_CREATED_BY + "/@" + NAME_OBJID;
 
     public static final String XPATH_USER_GROUP_CREATED_BY_XLINK_HREF = XPATH_USER_GROUP_CREATED_BY + "/@" + NAME_HREF;
 
     public static final String XPATH_USER_GROUP_CREATED_BY_XLINK_TITLE =
         XPATH_USER_GROUP_CREATED_BY + "/@" + NAME_TITLE;
 
     public static final String XPATH_USER_GROUP_CREATED_BY_XLINK_TYPE = XPATH_USER_GROUP_CREATED_BY + "/@" + NAME_TYPE;
 
     public static final String XPATH_USER_GROUP_CREATION_DATE = XPATH_USER_GROUP_PROPERTIES + "/creation-date";
 
     public static final String XPATH_USER_GROUP_RESOURCES = XPATH_USER_GROUP + "/" + NAME_RESOURCES;
 
     public static final String XPATH_USER_GROUP_RESOURCES_XLINK_HREF = XPATH_USER_GROUP_RESOURCES + PART_XLINK_HREF;
 
     public static final String XPATH_USER_GROUP_RESOURCES_XLINK_TITLE = XPATH_USER_GROUP_RESOURCES + PART_XLINK_TITLE;
 
     public static final String XPATH_USER_GROUP_RESOURCES_XLINK_TYPE = XPATH_USER_GROUP_RESOURCES + PART_XLINK_TYPE;
 
     public static final String XPATH_USER_GROUP_CURRENT_GRANTS = XPATH_USER_GROUP_RESOURCES + "/" + NAME_CURRENT_GRANTS;
 
     public static final String XPATH_USER_GROUP_CURRENT_GRANTS_XLINK_TYPE =
         XPATH_USER_GROUP_CURRENT_GRANTS + PART_XLINK_TYPE;
 
     public static final String XPATH_USER_GROUP_ACTIVE = XPATH_USER_GROUP_PROPERTIES + "/active";
 
     public static final String XPATH_USER_GROUP_NAME = XPATH_USER_GROUP_PROPERTIES + "/name";
 
     public static final String XPATH_USER_GROUP_LABEL = XPATH_USER_GROUP_PROPERTIES + "/label";
 
     public static final String XPATH_GRANT = "/grant";
 
     public static final String XPATH_GRANT_PROPERTIES = XPATH_GRANT + "/" + NAME_PROPERTIES;
 
     public static final String XPATH_GRANT_GRANT_REMARK = XPATH_GRANT_PROPERTIES + "/" + NAME_GRANT_REMARK;
 
     public static final String XPATH_GRANT_ROLE = XPATH_GRANT_PROPERTIES + "/" + NAME_ROLE;
 
     public static final String XPATH_GRANT_ROLE_XLINK_TITLE = XPATH_GRANT_ROLE + PART_XLINK_TITLE;
 
     public static final String XPATH_GRANT_ROLE_XLINK_HREF = XPATH_GRANT_ROLE + PART_XLINK_HREF;
 
     public static final String XPATH_GRANT_ROLE_OBJID = XPATH_GRANT_ROLE + "/@" + NAME_OBJID;
 
     public static final String XPATH_GRANT_OBJECT = XPATH_GRANT_PROPERTIES + "/" + EscidocAbstractTest.NAME_ASSIGNED_ON;
 
     public static final String XPATH_GRANT_OBJECT_XLINK_TITLE = XPATH_GRANT_OBJECT + PART_XLINK_TITLE;
 
     public static final String XPATH_GRANT_OBJECT_XLINK_HREF = XPATH_GRANT_OBJECT + PART_XLINK_HREF;
 
     public static final String XPATH_GRANT_OBJECT_OBJID = XPATH_GRANT_OBJECT + PART_OBJID;
 
     public static final String XPATH_SCOPE = "/scope";
 
     public static final String XPATH_STATISTIC_DATA_SCOPE = "/statistic-record/scope";
 
     public static final String XPATH_AGGREGATION_DEFINITION_SCOPE = "/aggregation-definition/scope";
 
     public static final String XPATH_REPORT_DEFINITION_SCOPE = "/report-definition/scope";
 
     public static final String XPATH_SCOPE_OBJID = XPATH_SCOPE + PART_OBJID;
 
     public static final String XPATH_STATISTIC_DATA_SCOPE_OBJID = XPATH_STATISTIC_DATA_SCOPE + PART_OBJID;
 
     public static final String XPATH_AGGREGATION_DEFINITION = "/aggregation-definition";
 
     public static final String XPATH_REPORT_DEFINITION = "//report-definition";
 
     public static final String XPATH_REPORT_DEFINITION_OBJID = XPATH_REPORT_DEFINITION + PART_OBJID;
 
     public static final String XPATH_REPORT = "/report";
 
     public static final String XPATH_REPORT_REPORT_DEFINITION = "/report/report-definition";
 
     public static final String ENTITY_REFERENCES =
         "A &lt; &gt; &amp; &quot; &apos; &amp;lt; &amp;gt; &amp;amp; &amp;quot; &amp;apos; Z";
 
     public static final String XLINK_TYPE_VALUE = "simple";
 
     public static final String NAME_ADMIN_DESCRIPTOR = "admin-descriptor";
 
     public static final String NAME_ADMIN_DESCRIPTORS = "admin-descriptors";
 
     private static final Pattern PATTERN_GET_ID_FROM_URI_OR_FEDORA_ID = Pattern.compile(".*/([^/>]+)>{0,1}");
 
     private static String REPOSITORY_VERSION = null;
 
     private static String baseHost = null;
 
     private static String basePort = null;
 
     private static String frameworkContext = null;
 
     private static String frameworkServiceUri = null;
 
     private static String fedoragsearchContext = null;
 
     private static String oaiproviderContext = null;
 
     private static String srwContext = null;
 
     private static String baseUrl = null;
 
     private AdminClient adminClient = null;
 
     private ItemClient itemClient = null;
 
     private IngestClient ingestClient = null;
 
     private ContainerClient containerClient = null;
 
     private ContextClient contextClient = null;
 
     private ContentRelationClient contentRelationClient = null;
 
     private DeviationClient deviationClient = null;
 
     private OrganizationalUnitClient ouClient = null;
 
     public EscidocTestBase() {
         this.stagingFileClient = new StagingFileClient();
     }
 
     /**
      * Tear down. Resets the user handle in <code>PWCallback</code>.
      * 
      * @throws Exception
      *             If an error occurs.
      */
     @After
     public void tearDown() throws Exception {
         PWCallback.resetHandle();
     }
 
     /**
      * @return Returns the client to use in the test.
      * @throws Exception
      *             If anything fails.
      */
     public ResourceHandlerClientInterface getClient() throws Exception {
 
         // get the current stack position
         try {
             throw new Exception();
         }
         catch (final Exception e) {
             throw new UnsupportedOperationException("getClient() not implemented by this test class.", e);
         }
     }
 
     /**
      * @return Returns the adminClient.
      */
     public AdminClient getAdminClient() {
         if (this.adminClient == null) {
             this.adminClient = new AdminClient();
         }
         return adminClient;
     }
 
     /**
      * @return Returns the itemClient.
      */
     public ItemClient getItemClient() {
         if (this.itemClient == null) {
             this.itemClient = new ItemClient();
         }
         return itemClient;
     }
 
     /**
      * @return Returns the IngestClient.
      */
     public IngestClient getIngestClient() {
         if (this.ingestClient == null) {
             this.ingestClient = new IngestClient();
         }
         return this.ingestClient;
     }
 
     /**
      * @return Returns the containerClient.
      */
     public ContainerClient getContainerClient() {
         if (this.containerClient == null) {
             this.containerClient = new ContainerClient();
         }
         return containerClient;
     }
 
     /**
      * @return Returns the contextClient.
      */
     public ContextClient getContextClient() {
         if (this.contextClient == null) {
             this.contextClient = new ContextClient();
         }
         return contextClient;
     }
 
     /**
      * @return Returns the contentRelationClient.
      */
     public ContentRelationClient getContentRelationClient() {
         if (this.contentRelationClient == null) {
             this.contentRelationClient = new ContentRelationClient();
         }
         return contentRelationClient;
     }
 
     /**
      * @return Returns the DeviationClient.
      */
     public DeviationClient getDeviationClient() {
         if (this.deviationClient == null) {
             this.deviationClient = new DeviationClient();
         }
         return this.deviationClient;
     }
 
     /**
      *
      * @return Returns the OrganizationalUnitClient.
      */
     public OrganizationalUnitClient getOrganizationalUnitClient() {
         if (this.ouClient == null) {
             this.ouClient = new OrganizationalUnitClient();
         }
         return this.ouClient;
     }
 
     /**
      * Returns the xml data of the provided result.
      * 
      * @param result
      *            The object holding the result.
      * @return Returns the xml string.
      * @throws Exception
      *             If anything fails.
      */
     protected String handleXmlResult(final Object result) throws Exception {
 
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
             assertContentTypeTextXmlUTF8OfMethod("", httpRes);
             xmlResult = EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8);
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Grab the object id from URI.
      * 
      * @param uri
      *            Fedora URI with objid .
      * @return object id
      */
     public static String getIdFromURI(final String uri) {
 
         if (uri == null) {
             return null;
         }
         Matcher matcher = PATTERN_GET_ID_FROM_URI_OR_FEDORA_ID.matcher(uri);
         if (matcher.find()) {
             return matcher.group(1);
         }
         else {
             return uri;
         }
     }
 
     /**
      * Test creating a resource using the specified resource handler.<br>
      * The client to use is determined by getClient() that must be implemented by the concrete test class.
      * 
      * @param resourceXml
      *            The xml representation of the resource.
      * @return The xml representation of the created resource.
      * @throws Exception
      *             If anything fails.
      */
     public String create(final String resourceXml) throws Exception {
 
         EtmPoint point = ETM_MONITOR.createPoint("EscidocTestBase:create");
         try {
             return handleXmlResult(getClient().create(resourceXml));
         }
         finally {
             point.collect();
         }
     }
 
     /**
      * Test deleting a resource from the framework.<br>
      * The client to use is determined by getClient() that must be implemented by the concrete test class.
      * 
      * @param id
      *            The id of the resource.
      * @throws Exception
      *             If anything fails.
      */
     public void delete(final String id) throws Exception {
 
         Object result = getClient().delete(id);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
     }
 
     /**
      * Test retrieving a resource from the framework.<br>
      * The client to use is determined by getClient() that must be implemented by the concrete test class.
      * 
      * @param id
      *            The id of the resource.
      * @return The retrieved resource.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieve(final String id) throws Exception {
 
         EtmPoint point = ETM_MONITOR.createPoint("EscidocTestBase:retrieve");
         try {
             return handleXmlResult(getClient().retrieve(id));
         }
         finally {
             point.collect();
         }
 
     }
 
     /**
      * Test retrieving the virtual resources of a resource from the framework.<br>
      * The client to use is determined by getClient() that must be implemented by the concrete test class.
      * 
      * @param id
      *            The id of the resource.
      * @return The retrieved virtual resources.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveResources(final String id) throws Exception {
 
         return handleXmlResult(getClient().retrieveResources(id));
     }
 
     /**
      * Test updating a resource of the framework.<br>
      * The client to use is determined by getClient() that must be implemented by the concrete test class.
      * 
      * @param id
      *            The id of the resource.
      * @param resourceXml
      *            The xml representation of the resource.
      * @return The updated resource.
      * @throws Exception
      *             If anything fails.
      */
     public String update(final String id, final String resourceXml) throws Exception {
 
         EtmPoint point = ETM_MONITOR.createPoint("EscidocTestBase:update");
         try {
             return handleXmlResult(getClient().update(id, resourceXml));
         }
         finally {
             point.collect();
         }
 
     }
 
     /**
      * Assert that Map is empty.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param map
      *            The map to check.
      */
     public static void assertEmptyMap(final String message, final Map map) {
         if (map == null) {
             fail(message + " Map is null");
         }
         if (!map.isEmpty()) {
             StringBuffer buf = new StringBuffer("");
             for (Object key : map.keySet()) {
                 buf.append(key);
             }
             fail(message + buf.toString());
         }
     }
 
     /**
      * Assert that the http request was successful.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param httpRes
      *            The http method.
      */
     public static void assertHttpStatusOK(final String message, final HttpResponse httpRes) {
         assertHttpStatus(message, HttpServletResponse.SC_OK, httpRes);
 
     }
 
     // Content-Type: text/xml;charset=UTF-8
 
     /**
      * Assert that the http request was successful.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param httpRes
      *            The http method.
      */
     public static void assertContentTypeTextXmlUTF8OfMethod(final String message, final HttpResponse httpRes) {
         assertContentType(message, MediaType.TEXT_XML.toString(), "utf-8", httpRes);
     }
 
     public static void assertContentType(
         final String message, final String expectedContentType, final String expectedCharset, final HttpResponse httpRes) {
         Header[] headers = httpRes.getAllHeaders();
         String contentTypeHeaderValue = null;
         for (int i = 0; i < headers.length && contentTypeHeaderValue == null; ++i) {
             if (headers[i].getName().toLowerCase(Locale.ENGLISH).equals("content-type")) {
                 contentTypeHeaderValue = headers[i].getValue();
             }
         }
         assertNotNull("No content-type header found, but expected 'content-type=" + expectedContentType + ";"
             + expectedCharset + "'", contentTypeHeaderValue);
         assertTrue("Wrong content-type found, expected '" + expectedContentType + "' but was '"
             + contentTypeHeaderValue + "'", contentTypeHeaderValue.indexOf(expectedContentType) > -1);
         assertTrue("Wrong charset found, expected '" + expectedCharset + "' but was '" + contentTypeHeaderValue + "'",
             contentTypeHeaderValue.indexOf(expectedContentType) > -1);
     }
 
     /**
      * Assert that the http request was successful.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param httpRes
      *            The http method.
      */
     public static void assertHttpStatusOfMethod(final String message, final HttpResponse httpRes) {
         // Delete Operation delivers Status code 206, HttpResponse doesn't
         // contain the original hhtp method
         if (httpRes.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
             assertHttpStatus(message, HttpServletResponse.SC_OK, httpRes);
 
         }
         else if ((httpRes.getStatusLine().getStatusCode() == HttpServletResponse.SC_NO_CONTENT)) {
             assertHttpStatus(message, HttpServletResponse.SC_NO_CONTENT, httpRes);
         }
 
     }
 
     /**
      * Assert that the http response has the expected status.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param expectedStatus
      *            The expected status.
      * @param httpRes
      *            The http method.
      */
     public static void assertHttpStatus(final String message, final int expectedStatus, final HttpResponse httpRes) {
         assertEquals(message + " Wrong response status!", expectedStatus, httpRes.getStatusLine().getStatusCode());
     }
 
     /**
      * Assert that the http response has the expected status.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param matchString
      *            expected String to match.
      * @param toTest
      *            toTest.
      */
     public static void assertMatches(final String message, final String matchString, final String toTest) {
         if (!toTest.matches("(?s).*" + matchString + ".*")) {
             fail(message);
         }
     }
 
     /**
      * Assert XML content is equal.<br/>
      * <p/>
      * This methods compares the attributes (if any exist) and either recursively compares the child elements (if any
      * exists) or the text content.<br/>
      * Therefore, mixed content is NOT supported by this method.
      * 
      * @param messageIn
      *            The message printed if assertion fails.
      * @param expected
      *            The expected XML content.
      * @param toBeAsserted
      *            The XML content to be compared with the expected content.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(final String messageIn, final Node expected, final Node toBeAsserted)
         throws Exception {
         // Assert both nodes are null or both nodes are not null
         if (expected == null) {
             assertNull(messageIn + "Unexpected node. ", toBeAsserted);
             return;
         }
         assertNotNull(messageIn + " Expected node. ", toBeAsserted);
         if (expected.equals(toBeAsserted)) {
             return;
         }
         String nodeName = getLocalName(expected);
         String message = messageIn;
         if (!message.contains("-- Asserting ")) {
             message = message + "-- Asserting " + nodeName + ". ";
         }
         else {
             message = message + "/" + nodeName;
         }
         // assert both nodes are nodes of the same node type
         // if thedocument container xslt directive than is the nodeName
         // "#document" is here compared
         assertEquals(message + " Type of nodes are different", expected.getNodeType(), toBeAsserted.getNodeType());
         if (expected.getNodeType() == Node.TEXT_NODE) {
             assertEquals(message + " Text nodes are different. ", expected.getTextContent().trim(), toBeAsserted
                 .getTextContent().trim());
         }
         // assert attributes
         NamedNodeMap expectedAttributes = expected.getAttributes();
         NamedNodeMap toBeAssertedAttributes = toBeAsserted.getAttributes();
         if (expectedAttributes == null) {
             assertNull(message + " Unexpected attributes. [" + nodeName + "]", toBeAssertedAttributes);
         }
         else {
             assertNotNull(message + " Expected attributes. ", toBeAssertedAttributes);
             final int expectedNumberAttributes = expectedAttributes.getLength();
             for (int i = 0; i < expectedNumberAttributes; i++) {
                 Node expectedAttribute = expectedAttributes.item(i);
                 String expectedAttributeNamespace = expectedAttribute.getNamespaceURI();
                 Node toBeAssertedAttribute = null;
                 if (expectedAttributeNamespace != null) {
                     final String localName = expectedAttribute.getLocalName();
                     toBeAssertedAttribute =
                         toBeAssertedAttributes.getNamedItemNS(expectedAttributeNamespace, localName);
                     assertNotNull(message + " Expected attribute " + expectedAttribute.getNodeName(),
                         toBeAssertedAttribute);
                 }
                 else {
                     // not namespace aware parsed. Attributes may have different
                     // prefixes which are now part of their node name.
                     // To compare expected and to be asserted attribute, it is
                     // first it is tried to find the appropriate to be asserted
                     // attribute by the node name. If this fails, xpath
                     // selection is used after extracting the expected
                     // attribute name
                     final String expectedAttributeNodeName = expectedAttribute.getNodeName();
                     toBeAssertedAttribute = toBeAssertedAttributes.getNamedItem(expectedAttributeNodeName);
                     if (toBeAssertedAttribute == null) {
                         final String attributeName = getLocalName(expectedAttribute);
                         final String attributeXpath = "@" + attributeName;
                         toBeAssertedAttribute = selectSingleNode(toBeAsserted, attributeXpath);
                     }
                     assertNotNull(message + " Expected attribute " + expectedAttributeNodeName, toBeAssertedAttribute);
                 }
                 assertEquals(message + " Attribute value mismatch [" + expectedAttribute.getNodeName() + "] ",
                     expectedAttribute.getTextContent(), toBeAssertedAttribute.getTextContent());
             }
         }
         // As mixed content (text + child elements) is not supported,
         // either the child elements or the text content have to be asserted.
         // Therefore, it is first tried to assert the children.
         // After that it is checked if children have been found. If this is not
         // the case, the text content is compared.
         NodeList expectedChildren = expected.getChildNodes();
         NodeList toBeAssertedChildren = toBeAsserted.getChildNodes();
         int expectedNumberElementNodes = 0;
         int toBeAssertedNumberElementNodes = 0;
         List<Node> previouslyAssertedChildren = new ArrayList<Node>();
         for (int i = 0; i < expectedChildren.getLength(); i++) {
             Node expectedChild = expectedChildren.item(i);
             if (expectedChild.getNodeType() == Node.ELEMENT_NODE) {
                 expectedNumberElementNodes++;
                 String expectedChildName = getLocalName(expectedChild);
                 String expectedUri = expectedChild.getNamespaceURI();
                 boolean expectedElementAsserted = false;
                 for (int j = 0; j < toBeAssertedChildren.getLength(); j++) {
                     final Node toBeAssertedChild = toBeAssertedChildren.item(j);
                     // prevent previously asserted children from being
                     // asserted again
                     if (previouslyAssertedChildren.contains(toBeAssertedChild)) {
                         continue;
                     }
                     if (toBeAssertedChild.getNodeType() == Node.ELEMENT_NODE
                         && expectedChildName.equals(getLocalName(toBeAssertedChild))
                         && (expectedUri == null || expectedUri.equals(toBeAssertedChild.getNamespaceURI()))) {
                         expectedElementAsserted = true;
                         toBeAssertedNumberElementNodes++;
                         assertXmlEquals(message, expectedChild, toBeAssertedChild);
                         // add asserted child to list of asserted children to
                         // prevent it from being asserted again.
                         previouslyAssertedChildren.add(toBeAssertedChild);
                         break;
                     }
                 }
                 if (!expectedElementAsserted) {
                     fail(new StringBuffer(message).append(" Did not found expected corresponding element [").append(
                         nodeName).append(", ").append(expectedChildName).append(", ").append(i).append("]").toString());
                 }
             }
         }
         // check if any element node in toBeAssertedChildren exists
         // that has not been asserted. In this case, this element node
         // is unexpected!
         for (int i = 0; i < toBeAssertedChildren.getLength(); i++) {
             Node toBeAssertedChild = toBeAssertedChildren.item(i);
             // prevent previously asserted children from being
             // asserted again
             if (previouslyAssertedChildren.contains(toBeAssertedChild)) {
                 continue;
             }
             if (toBeAssertedChild.getNodeType() == Node.ELEMENT_NODE) {
                 fail(new StringBuffer(message)
                     .append("Found unexpected element node [").append(nodeName).append(", ").append(
                         getLocalName(toBeAssertedChild)).append(", ").append(i).append("]").toString());
             }
         }
         // if no children have been found, text content must be compared
         if (expectedNumberElementNodes == 0 && toBeAssertedNumberElementNodes == 0) {
             String expectedContent = expected.getTextContent();
             String toBeAssertedContent = toBeAsserted.getTextContent();
             assertEquals(message, expectedContent, toBeAssertedContent);
         }
     }
 
     /**
      * Gets the local name (the node name without the namespace prefix) of the provided node.
      * 
      * @param node
      *            The node to extract the name from.
      * @return Returns <code>node.getLocalName</code> if this is set, or the value of <code>node.getNodeName</code>
      *         without the namespace prefix.
      */
     private static String getLocalName(final Node node) {
 
         String name = node.getLocalName();
         if (name == null) {
             name = node.getNodeName().replaceAll(".*?:", "");
         }
         return name;
     }
 
     /**
      * Assert XML content is equal.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param expected
      *            The expected XML content.
      * @param toBeAsserted
      *            The XML content to be compared with the expected content.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(final String message, final String expected, final String toBeAsserted)
         throws Exception {
 
         Document expectedDoc = EscidocAbstractTest.getDocument(expected);
         Document assertedDoc = EscidocAbstractTest.getDocument(toBeAsserted);
 
         assertXmlEquals(message, expectedDoc, assertedDoc);
     }
 
     /**
      * Get the host name of the framework (read from properties).
      * 
      * @return the host name of the framework
      */
     public static String getBaseHost() {
         if (baseHost == null) {
             baseHost = PropertiesProvider.getInstance().getProperty("server.name", "localhost");
         }
         return baseHost;
     }
 
     /**
      * Get the port number of the framework (read from properties).
      * 
      * @return the port number of the framework
      */
     public static String getBasePort() {
         if (basePort == null) {
             basePort = PropertiesProvider.getInstance().getProperty("server.port", "8080");
         }
         return basePort;
     }
 
     /**
      * Get the context-path of the framework (read from properties).
      * 
      * @return the context of the framework
      */
     public static String getFrameworkContext() {
         if (frameworkContext == null) {
             frameworkContext = PropertiesProvider.getInstance().getProperty("server.context", "/escidoc");
            if (!frameworkContext.startsWith("/")) {
                frameworkContext = "/" + frameworkContext;
            }
         }
         return frameworkContext;
     }
 
     /**
      * Get the service-uri of the framework (read from properties).
      * 
      * @return the service-uri of the framework
      */
     public static String getFrameworkServiceUri() {
         if (frameworkServiceUri == null) {
             frameworkServiceUri = PropertiesProvider.getInstance().getProperty("server.service.uri", "");
         }
         return frameworkServiceUri;
     }
 
     /**
      * Get the context-path of fedoragsearch (read from properties).
      * 
      * @return the context of fedoragsearch
      */
     public static String getFedoragsearchContext() {
         if (fedoragsearchContext == null) {
             fedoragsearchContext =
                 PropertiesProvider.getInstance().getProperty("fedoragsearch.context", "/fedoragsearch");
         }
         return fedoragsearchContext;
     }
 
     /**
      * Get the context-path of oaiprovider (read from properties).
      * 
      * @return the context of oaiprovider
      */
     public static String getOaiproviderContext() {
         if (oaiproviderContext == null) {
             oaiproviderContext =
                 PropertiesProvider.getInstance().getProperty("oaiprovider.context", "/escidoc-oaiprovider/");
         }
         return oaiproviderContext;
     }
 
     /**
      * Get the context-path of srw (read from properties).
      * 
      * @return the context of srw
      */
     public static String getSrwContext() {
         if (srwContext == null) {
             srwContext = PropertiesProvider.getInstance().getProperty("srw.context", "/srw");
         }
         return srwContext;
     }
 
     /**
      * Get the href of the framework (read from properties).
      * 
      * @return the href of the framework.
      */
     public static String getBaseUrl() {
         if (baseUrl == null) {
             baseUrl = Constants.PROTOCOL + "://" + getBaseHost() + ":" + getBasePort();
         }
         return baseUrl;
     }
 
     /**
      * Assert XML content is equal.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param expected
      *            The expected XML content.
      * @param toBeAsserted
      *            The XML content to be compared with the expected content.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(final String message, final InputStream expected, final String toBeAsserted)
         throws Exception {
 
         Document expectedDoc = EscidocAbstractTest.getDocument(ResourceProvider.getContentsFromInputStream(expected));
         Document assertedDoc = EscidocAbstractTest.getDocument(toBeAsserted);
         assertXmlEquals(message, expectedDoc, assertedDoc);
     }
 
     /**
      * Assert XML content is equal.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param expected
      *            The expected XML content.
      * @param toBeAsserted
      *            The XML content to be compared with the expected content.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(final String message, final File expected, final String toBeAsserted)
         throws Exception {
 
         assertXmlEquals(message, new FileInputStream(expected), toBeAsserted);
     }
 
     /**
      * Assert that the Element/Attribute selected by the xPath exists.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param xml
      *            The xml document as String.
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlExists(final String message, final String xml, final String xPath) throws Exception {
 
         assertXmlExists(message, EscidocAbstractTest.getDocument(xml), xPath);
     }
 
     /**
      * Assert that the Element/Attribute selected by the xPath exists.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param node
      *            The Node.
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlExists(final String message, final Node node, final String xPath) throws Exception {
 
         NodeList nodes = selectNodeList(node, xPath);
         assertTrue(message, nodes.getLength() > 0);
     }
 
     /**
      * Assert that the Element/Attribute selected by the xPath exists.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param node
      *            The Node.
      * @param xPath
      *            The xPath.
      * @param namespaceNode
      *            The namespace node.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlExists(
         final String message, final Node node, final String xPath, final Node namespaceNode) throws Exception {
 
         NodeList nodes = selectNodeList(node, xPath, namespaceNode);
         assertTrue(message, nodes.getLength() > 0);
     }
 
     /**
      * Assert that the value in the Document selected by the xPath equals the expected value.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param xml
      *            The xml document as String.
      * @param xPath
      *            The xPath.
      * @param expectedValue
      *            The expected value.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(
         final String message, final String xml, final String xPath, final String expectedValue) throws Exception {
         assertXmlEquals(message, EscidocAbstractTest.getDocument(xml), xPath, expectedValue);
     }
 
     /**
      * Assert that the value in the Document selected by the xPath equals the expected value.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param node
      *            The Node.
      * @param xPath
      *            The xPath.
      * @param expectedValue
      *            The expected value.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(
         final String message, final Node node, final String xPath, final String expectedValue) throws Exception {
         Node comp = selectSingleNode(node, xPath);
         assertNotNull(message + " Node selected by xpath not found [" + xPath + "]", comp);
         final String trimmed = comp.getTextContent().trim();
         assertEquals(message, expectedValue, trimmed);
     }
 
     /**
      * Assert that the value in the Document selected by the xPath equals the expected value.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param expected
      *            The expected node
      * @param result
      *            The result to be asserted.
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(final String message, final Node expected, final Node result, final String xPath)
         throws Exception {
 
         assertXmlEquals(message, expected, xPath, result, xPath);
     }
 
     /**
      * Assert that the value(s) in the to be asserted node selected by the xPath equals the expected value.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param expected
      *            The node from which the expected value is selected by the xpath.
      * @param expectedXpath
      *            The xpath expression navigating in the node to the expected value.
      * @param toBeAsserted
      *            The node for that the value selected by the xpath shall be asserted.
      * @param toBeAssertedXpath
      *            The xPath navigating to the value that shall be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlEquals(
         final String message, final Node expected, final String expectedXpath, final Node toBeAsserted,
         final String toBeAssertedXpath) throws Exception {
 
         final String msg = prepareAssertionFailedMessage(message);
 
         if (expected == toBeAsserted) {
             return;
         }
         final NodeList expectedNodes = selectNodeList(expected, expectedXpath);
         final NodeList toBeAssertedNodes = selectNodeList(toBeAsserted, toBeAssertedXpath);
         assertEquals(msg + "Number of selected nodes differ. ", expectedNodes.getLength(), toBeAssertedNodes
             .getLength());
         final int length = toBeAssertedNodes.getLength();
         for (int i = 0; i < length; i++) {
             assertXmlEquals(msg + "Asserting " + (i + 1) + ". node. ", expectedNodes.item(i), toBeAssertedNodes.item(i));
         }
     }
 
     /**
      * Assert that the value in the Document selected by the xPath NOT equals the unexpected value.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param unexpected
      *            The unexpected node
      * @param toBeAsserted
      *            The result to be asserted.
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlNotEquals(
         final String message, final Node unexpected, final Node toBeAsserted, final String xPath) throws Exception {
 
         Node toBeAssertedComp = selectSingleNode(toBeAsserted, xPath);
         Node unexpectedComp = selectSingleNode(unexpected, xPath);
         assertNotEquals(message, unexpectedComp.getTextContent(), toBeAssertedComp.getTextContent());
     }
 
     /**
      * Asserts that the timestamp has been updated.<br>
      * This assertion fails if the timestamp in the previous document is not less than the timestamp in the updated
      * document. The timestamp is identified by the root element's "last-modification-timestamp" attribute.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param previous
      *            The document containing the expected timestamp
      * @param toBeAsserted
      *            The document for that the timestamp shall be asserted.
      * @throws Exception
      *             If an error ocurres.
      */
     public static void assertXmlLastModificationDateUpdate(
         final String message, final Document previous, final Document toBeAsserted) throws Exception {
 
         final String previousLastModificationDateValue = getLastModificationDateValue(previous);
         final String toBeAssertedLastModificationDateValue = getLastModificationDateValue(toBeAsserted);
         assertDateBeforeAfter(previousLastModificationDateValue, toBeAssertedLastModificationDateValue);
     }
 
     /**
      * Asserts to objects are not equal.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param unexpected
      *            The unexpected node
      * @param toBeAsserted
      *            The result to be asserted.
      */
     public static void assertNotEquals(final String message, final Object unexpected, final Object toBeAsserted) {
 
         if (unexpected == null && toBeAsserted == null || unexpected.equals(toBeAsserted)) {
             if (message == null || message.isEmpty()) {
                 fail("Values are equal. Expected unequal");
             }
             else {
                 fail(message);
             }
         }
     }
 
     /**
      * Serialize the given Dom Object to a String.
      * 
      * @param xml
      *            The Xml Node to serialize.
      * @param omitXMLDeclaration
      *            Indicates if XML declaration will be omitted.
      * @return The String representation of the Xml Node.
      * @throws Exception
      *             If anything fails.
      */
     public static String toString(final Node xml, final boolean omitXMLDeclaration) throws Exception {
 
         String result = new String();
         if (xml instanceof AttrImpl) {
             result = xml.getTextContent();
         }
         else if (xml instanceof Document) {
             StringWriter stringOut = new StringWriter();
             // format
             OutputFormat format = new OutputFormat((Document) xml);
             format.setIndenting(true);
             format.setPreserveSpace(true);
             format.setOmitXMLDeclaration(omitXMLDeclaration);
             format.setEncoding(DEFAULT_CHARSET);
             // serialize
             XMLSerializer serial = new XMLSerializer(stringOut, format);
             serial.asDOMSerializer();
 
             serial.serialize((Document) xml);
             result = stringOut.toString();
         }
         else {
             DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
             DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
             LSOutput lsOutput = impl.createLSOutput();
             lsOutput.setEncoding(DEFAULT_CHARSET);
 
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             lsOutput.setByteStream(os);
             LSSerializer writer = impl.createLSSerializer();
             // result = writer.writeToString(xml);
             writer.write(xml, lsOutput);
             result = ((ByteArrayOutputStream) lsOutput.getByteStream()).toString(DEFAULT_CHARSET);
             if ((omitXMLDeclaration) && (result.indexOf("?>") != -1)) {
                 result = result.substring(result.indexOf("?>") + 2);
             }
             // result = toString(getDocument(writer.writeToString(xml)),
             // true);
         }
         return result;
     }
 
     /**
      * Delete an Element from a Node.
      * 
      * @param node
      *            the node.
      * @param xPath
      *            The xPath selecting the element.
      * @return The resulting node.
      * @throws Exception
      *             If anything fails.
      */
     public static Node deleteElement(final Node node, final String xPath) throws Exception {
 
         Node delete = selectSingleNode(node, xPath);
         assertNotNull("No node found for provided xpath [" + xPath + "]", delete);
         if (delete instanceof AttrImpl) {
             throw new Exception("Removal of Element not successful! " + "xPath selects an Attribute!");
         }
         else {
             delete.getParentNode().removeChild(delete);
         }
         return node;
     }
 
     /**
      * Delete all Elements from a Node.
      * 
      * @param node
      *            the node.
      * @param xPath
      *            The xPath selecting the element.
      * @return The resulting node.
      * @throws Exception
      *             If anything fails.
      */
     public static Node deleteElements(final Node node, final String xPath) throws Exception {
 
         NodeList nodes = selectNodeList(node, xPath);
         for (int i = 0; i < nodes.getLength(); ++i) {
             deleteElement(node, xPath);
         }
         return node;
     }
 
     /**
      * Return the text value of the selected attribute.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xpath to select the node contain the attribute,
      * @param attributeName
      *            The name of the attribute.
      * @return The text value of the selected attribute.
      * @throws Exception
      *             If anything fails.
      */
     public static String getAttributeValue(final Node node, final String xPath, final String attributeName)
         throws Exception {
         String result = null;
         Node element = selectSingleNode(node, xPath);
         if (element != null && element.hasAttributes()) {
             for (int i = 0; i < element.getAttributes().getLength(); ++i) {
                 String nodeName = element.getAttributes().item(i).getNodeName();
                 if (nodeName.endsWith(":" + attributeName) || nodeName.equals(attributeName)) {
                     result = element.getAttributes().getNamedItem(nodeName).getTextContent();
                     break;
                 }
             }
         }
         return result;
     }
 
     /**
      * Delete an Attribute from an Element of a Node.
      * 
      * @param node
      *            the node.
      * @param xPath
      *            The xPath selecting the element.
      * @param attributeName
      *            The name of the attribute.
      * @return The resulting node.
      * @throws Exception
      *             If anything fails.
      */
     public static Node deleteAttribute(final Node node, final String xPath, final String attributeName)
         throws Exception {
 
         if (node == null) {
             return node;
         }
         Node delete = selectSingleNode(node, xPath);
         if (delete == null) {
             return node;
         }
         if (delete.hasAttributes()) {
             for (int i = 0; i < delete.getAttributes().getLength(); ++i) {
                 String nodeName = delete.getAttributes().item(i).getNodeName();
                 if (nodeName.endsWith(":" + attributeName) || nodeName.equals(attributeName)) {
                     delete.getAttributes().removeNamedItem(nodeName);
                     break;
                 }
             }
         }
         return node;
     }
 
     /**
      * Delete an Attribute from an Element of a Node.
      * 
      * @param node
      *            the node.
      * @param xPath
      *            The xPath selecting the attribute.
      * @return The resulting node.
      * @throws Exception
      *             If anything fails.
      */
     public static Node deleteAttribute(final Node node, final String xPath) throws Exception {
 
         final int index = xPath.lastIndexOf('/');
         final String elementXpath = xPath.substring(0, index);
         final String attrName = xPath.substring(index + 2);
         return deleteAttribute(node, elementXpath, attrName);
     }
 
     /**
      * Substitute the element selected by the xPath in the given node with the new value.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param newValue
      *            The newValue.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public static Node substitute(final Node node, final String xPath, final String newValue) throws Exception {
         Node result = node;
         Node replace = selectSingleNode(result, xPath);
         assertNotNull("No node found for specified xpath [" + xPath + "]", replace);
         // if (replace.getNodeType() == Node.ELEMENT_NODE) {
         replace.setTextContent(newValue);
         // }
         // else if (replace.getNodeType() == Node.ATTRIBUTE_NODE) {
         // replace.setNodeValue(newValue);
         // }
         // else {
         // throw new Exception("Unsupported node type '"
         // + replace.getNodeType() + "' in EscidocTestBase.substitute.");
         // }
         return result;
     }
 
     /**
      * Substitute the element selected by the xPath in the given node with the new value.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param newValue
      *            The newValue.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public Node substituteId(final Node node, final String xPath, final String newValue) throws Exception {
         Node result = node;
         Node replace = null;
         String path = "";
         replace = selectSingleNode(result, xPath + "/@href");
         path = replace.getTextContent().substring(0, replace.getTextContent().lastIndexOf("/") + 1);
 
         assertNotNull("No node found for specified xpath [" + xPath + "]", replace);
         // if (replace.getNodeType() == Node.ELEMENT_NODE) {
         replace.setTextContent(path + newValue);
         // }
         // else if (replace.getNodeType() == Node.ATTRIBUTE_NODE) {
         // replace.setNodeValue(newValue);
         // }
         // else {
         // throw new Exception("Unsupported node type '"
         // + replace.getNodeType() + "' in EscidocTestBase.substitute.");
         // }
         return result;
     }
 
     /**
      * Gets the prefix of the provided node.<br>
      * This returns Node.getPrefix() if this is not null. Otherwise, ittries to extract the prefix from
      * Node.getNodeName(). If this fails, null is returned.
      * 
      * @param node
      *            The node to get the prefix from.
      * @return Returns the determined prefix or null.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static String getPrefix(final Node node) throws Exception {
 
         String prefix = node.getPrefix();
         if (prefix == null) {
             String nodeName = node.getNodeName();
             int index = nodeName.indexOf(":");
             if (index != -1) {
                 prefix = nodeName.substring(0, index);
             }
         }
         return prefix;
     }
 
     /**
      * Creates a new element node for the provided document. The created element is an element that refers to another
      * resource, i.e. it has xlink attributes and an objid attribute.
      * 
      * @param doc
      *            The document for that the node shall be created.
      * @param namespaceUri
      *            The name space uri of the node to create. This may be null.
      * @param prefix
      *            The prefix to use.
      * @param tagName
      *            The tag name of the node.
      * @param xlinkPrefix
      *            The prefix to use for the xlink attributes.
      * @param title
      *            The title of the referencing element (=xlink:title)
      * @param href
      *            The href of the referencing element (=xlink:href). The objid attribute value is extracted from this
      *            href.
      * @return Returns the created node.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public Element createReferencingElementNode(
         final Document doc, final String namespaceUri, final String prefix, final String tagName,
         final String xlinkPrefix, final String title, final String href) throws Exception {
 
         Element newElement = createElementNodeWithXlink(doc, namespaceUri, prefix, tagName, xlinkPrefix, title, href);
 
         Attr objidAttr = createAttributeNode(doc, null, null, NAME_OBJID, getObjidFromHref(href));
         newElement.getAttributes().setNamedItemNS(objidAttr);
 
         return newElement;
     }
 
     /**
      * Creates a new element node for the provided document. The created element is an element that that has xlink
      * attributes, but does not have an objid attribute.
      * 
      * @param doc
      *            The document for that the node shall be created.
      * @param namespaceUri
      *            The name space uri of the node to create. This may be null.
      * @param prefix
      *            The prefix to use.
      * @param tagName
      *            The tag name of the node.
      * @param xlinkPrefix
      *            The prefix to use for the xlink attributes.
      * @param title
      *            The title of the referencing element (=xlink:title)
      * @param href
      *            The href of the referencing element (=xlink:href). The objid attribute value is extracted from this
      *            href.
      * @return Returns the created node.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static Element createElementNodeWithXlink(
         final Document doc, final String namespaceUri, final String prefix, final String tagName,
         final String xlinkPrefix, final String title, final String href) throws Exception {
 
         Element newElement = createElementNode(doc, namespaceUri, prefix, tagName, null);
         Attr xlinkTypeAttr = createAttributeNode(doc, XLINK_NS_URI, xlinkPrefix, NAME_TYPE, "simple");
         Attr xlinkTitleAttr = createAttributeNode(doc, XLINK_NS_URI, xlinkPrefix, NAME_TITLE, title);
         Attr xlinkHrefAttr = createAttributeNode(doc, XLINK_NS_URI, xlinkPrefix, NAME_HREF, href);
         newElement.getAttributes().setNamedItemNS(xlinkTypeAttr);
         newElement.getAttributes().setNamedItemNS(xlinkTitleAttr);
         newElement.getAttributes().setNamedItemNS(xlinkHrefAttr);
 
         return newElement;
     }
 
     /**
      * Creates a new element node for the provided document.
      * 
      * @param doc
      *            The document for that the node shall be created.
      * @param namespaceUri
      *            The name space uri of the node to create. This may be null.
      * @param prefix
      *            The prefix to use.
      * @param tagName
      *            The tag name of the node.
      * @param textContent
      *            The text content of the node. This may be null.
      * @return Returns the created node.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static Element createElementNode(
         final Document doc, final String namespaceUri, final String prefix, final String tagName,
         final String textContent) throws Exception {
 
         Element newNode = doc.createElementNS(namespaceUri, tagName);
         newNode.setPrefix(prefix);
         if (textContent != null) {
             newNode.setTextContent(textContent);
         }
         return newNode;
     }
 
     /**
      * Creates a new attribute node for the provided document.
      * 
      * @param doc
      *            The document for that the node shall be created.
      * @param namespaceUri
      *            The name space uri of the node to create. This may be null.
      * @param prefix
      *            The prefix to use.
      * @param tagName
      *            The tag name of the node.
      * @param value
      *            The attribute value.
      * @return Returns the created node.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static Attr createAttributeNode(
         final Document doc, final String namespaceUri, final String prefix, final String tagName, final String value)
         throws Exception {
 
         Attr newAttribute = doc.createAttributeNS(namespaceUri, tagName);
         newAttribute.setPrefix(prefix);
         newAttribute.setValue(value);
         if (value != null) {
             newAttribute.setTextContent(value);
         }
         return newAttribute;
     }
 
     /**
      * Gets the prefix of the node selected by the xpath in the provided node.<br>
      * This returns Node.getPrefix() if this is not null. Otherwise, ittries to extract the prefix from
      * Node.getNodeName(). If this fails, null is returned.
      * 
      * @param node
      *            The node to get the prefix from.
      * @param xPath
      *            XPath to the Node to select the prefix from.
      * @return Returns the determined prefix or null.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static String getPrefix(final Node node, final String xPath) throws Exception {
 
         return getPrefix(selectSingleNode(node, xPath));
     }
 
     /**
      * Adds the provided new node as the child of the element selected by the xPath in the given node.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param newNode
      *            The new node.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public static Node addAsChild(final Node node, final String xPath, final Element newNode) throws Exception {
 
         Node result = node;
         Node parent = selectSingleNode(result, xPath);
         assertNotNull("No node for xpath found [" + xPath + "]", parent);
         // inserts at end of list
         parent.insertBefore(newNode, null);
 
         return result;
     }
 
     /**
      * Adds the provided new node after the element selected by the xPath in the given node.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param newNode
      *            The new node.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public static Node addAfter(final Node node, final String xPath, final Node newNode) throws Exception {
 
         Node result = node;
         Node before = selectSingleNode(result, xPath);
         assertNotNull("No node for xpath [" + xPath + "] found", before);
         Node parent = before.getParentNode();
         parent.insertBefore(newNode, before.getNextSibling());
         return result;
     }
 
     /**
      * Adds the provided new node before the element selected by the xPath in the given node.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param newNode
      *            The new node.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public static Node addBefore(final Node node, final String xPath, final Node newNode) throws Exception {
 
         Node result = node;
         Node after = selectSingleNode(result, xPath);
         assertNotNull("No node for xpath found [" + xPath + "]", after);
         Node parent = after.getParentNode();
         parent.insertBefore(newNode, after);
         return result;
     }
 
     /**
      * Adds the provided new attribute node to the element selected by the xPath in the given node.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param attributeNode
      *            The new attribute node.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public static Node addAttribute(final Node node, final String xPath, final Attr attributeNode) throws Exception {
 
         Node result = node;
         Node element = selectSingleNodeAsserted(result, xPath);
         NamedNodeMap attributes = element.getAttributes();
         attributes.setNamedItemNS(attributeNode);
         return result;
     }
 
     /**
      * Substitute the element selected by the xPath in the given node with the new node.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param newNode
      *            The new node.
      * @return The resulting node after the substitution.
      * @throws Exception
      *             If anything fails.
      */
     public static Node substitute(final Node node, final String xPath, final Node newNode) throws Exception {
         Node result = node;
         Node replace = selectSingleNode(result, xPath);
         assertNotNull("No node selected for substitute. ", replace);
         Node parent = replace.getParentNode();
         parent.replaceChild(newNode, replace);
         return result;
     }
 
     /**
      * Return the list of children of the node selected by the xPath.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @return The list of children of the node selected by the xPath.
      * @throws TransformerException
      *             If anything fails.
      */
     public static NodeList selectNodeList(final Node node, final String xPath) throws TransformerException {
         NodeList result = XPathAPI.selectNodeList(node, xPath);
         return result;
     }
 
     /**
      * Return the list of children of the node selected by the xPath.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param namespaceNode
      *            The namespace node.
      * @return The list of children of the node selected by the xPath.
      * @throws TransformerException
      *             If anything fails.
      */
     public static NodeList selectNodeList(final Node node, final String xPath, final Node namespaceNode)
         throws TransformerException {
         NodeList result = XPathAPI.selectNodeList(node, xPath, namespaceNode);
         return result;
     }
 
     public static int getNoOfSelections(final Node node, final String xPath) throws TransformerException {
         int result = 0;
         NodeList matches = selectNodeList(node, xPath);
         if (matches != null) {
             result = matches.getLength();
         }
         return result;
     }
 
     /**
      * Return the child of the node selected by the xPath.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @return The child of the node selected by the xPath.
      * @throws TransformerException
      *             If anything fails.
      */
     public static Node selectSingleNode(final Node node, final String xPath) throws TransformerException {
 
         Node result = XPathAPI.selectSingleNode(node, xPath);
         return result;
     }
 
     /**
      * Return the child of the node selected by the xPath.<br>
      * This method includes an assert that the specified node exists.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @return The child of the node selected by the xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static Node selectSingleNodeAsserted(final Node node, final String xPath) throws Exception {
 
         Node result = selectSingleNode(node, xPath);
         assertNotNull("Node does not exist [" + xPath + "]", result);
         return result;
     }
 
     /**
      * Return the child of the node selected by the xPath.
      * 
      * @param node
      *            The node.
      * @param xPath
      *            The xPath.
      * @param namespaceNode
      *            The namespace node.
      * @return The child of the node selected by the xPath.
      * @throws TransformerException
      *             If anything fails.
      */
     public static Node selectSingleNode(final Node node, final String xPath, final Node namespaceNode)
         throws TransformerException {
 
         Node result = XPathAPI.selectSingleNode(node, xPath, namespaceNode);
         return result;
     }
 
     /**
      * Return a filter parameter including only an empty filter.
      * 
      * @return The filter parameter including only an empty filter.
      * @throws Exception
      *             If anything fails.
      */
     public static String getEmptyFilter() throws Exception {
 
         return EscidocAbstractTest.getTemplateAsString(TEMPLATE_OM_COMMON_PATH, "emptyFilter.xml");
     }
 
     /**
      * Get the filter parameter for retrieving contexts matching the given filter criteria. If a criteria is null the
      * filter is deleted from the parameter.
      * 
      * @param user
      *            The expected user.
      * @param role
      *            The expected role.
      * @param contextType
      *            The expected type of the context.
      * @return The filter parameter.
      * @throws Exception
      *             If anything fails.
      */
     public static Map<String, String[]> getFilterRetrieveContexts(
         final String user, final String role, final String contextType) throws Exception {
         Map<String, String[]> result = new HashMap<String, String[]>();
         StringBuffer filter = new StringBuffer();
 
         if ((user != null) && (user.length() > 0)) {
             filter.append("user=\"" + user + "\"");
         }
         if ((role != null) && (role.length() > 0)) {
             if (filter.length() > 0) {
                 filter.append(" and ");
             }
             filter.append("role=\"" + role + "\"");
         }
         if ((contextType != null) && (contextType.length() > 0)) {
             if (filter.length() > 0) {
                 filter.append(" and ");
             }
             filter.append("\"/properties/type\"=\"" + contextType + "\"");
         }
         if (filter.length() > 0) {
             result.put("query", new String[] { filter.toString() });
         }
         result.put("maximumRecords", new String[] { "1000" });
         return result;
     }
 
     /**
      * Get the filter parameter for retrieving members of a context matching the given filter criteria. If a criteria is
      * null the filter is deleted from the parameter.
      * 
      * @param members
      *            A list of members to restrict the resulting members.
      * @param objectType
      *            The type of the object (item or container).
      * @param user
      *            The expected user.
      * @param role
      *            The expected role.
      * @param status
      *            The expected status of the resulting objects.
      * @param contentType
      *            The expected contentType of the resulting objects.
      * @return The filter parameter.
      * @throws Exception
      *             If anything fails.
      */
     public static String getFilterRetrieveMembersOfContext(
         final List<String> members, final String objectType, final String user, final String role, final String status,
         final String contentType) throws Exception {
 
         Document filter =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_OM_COMMON_PATH, "filterRetrieveMembersOfContext.xml");
         if ((members != null) && (members.size() > 0)) {
             for (int i = 0; i < 5; ++i) {
                 String value = null;
                 try {
                     value = members.get(i);
                     filter =
                         (Document) replaceInFilter(filter, value, "/param/filter[@name=\"members\"]/id[" + (i + 1)
                             + "]");
                 }
                 catch (final RuntimeException e) {
                     filter =
                         (Document) replaceInFilter(filter, null, "/param/filter[@name=\"members\"]/id["
                             + (members.size() + 1) + "]");
                 }
             }
         }
         else {
             filter = (Document) replaceInFilter(filter, null, "param/filter[@name=\"members\"]");
         }
 
         filter =
             (Document) replaceInFilter(filter, objectType, "param/filter[@name=\"http://www.w3.org/1999/02/"
                 + "22-rdf-syntax-ns#type\"]");
         filter = (Document) replaceInFilter(filter, user, "param/filter[@name=\"user\"]");
         filter = (Document) replaceInFilter(filter, role, "param/filter[@name=\"role\"]");
 
         filter =
             (Document) replaceInFilter(filter, status, "param/filter[@name=\"http://escidoc.de/core/01/"
                 + "properties/public-status\"]");
         filter =
             (Document) replaceInFilter(filter, contentType, "param/filter[@name=\"http://escidoc.de/core/01/"
                 + "structural-relations/content-model\"]");
         String result = toString(filter, true);
         return result;
     }
 
     /**
      * If value is null the element selected by xPath is removed from the filter otherwise the elements value is set to
      * vlaue.
      * 
      * @param filter
      *            The filter parameter.
      * @param value
      *            The value.
      * @param xPath
      *            The xPath.
      * @return The resulting filter parameter.
      * @throws Exception
      *             If anything fails.
      */
     public static Node replaceInFilter(final Node filter, final String value, final String xPath) throws Exception {
 
         if (value != null) {
             return substitute(filter, xPath, value);
         }
         else {
             return deleteElement(filter, xPath);
         }
 
     }
 
     /**
      * Gets the last modification date from the resource through retrieve.
      * 
      * @param id
      *            The id of the Resource.
      * @return last-modification-date
      * @throws Exception
      *             Thrown if anything fails.
      */
     public String getTheLastModificationDate(final String id) throws Exception {
 
         Document resource = EscidocAbstractTest.getDocument(retrieve(id));
         return getTheLastModificationDate(resource);
     }
 
     /**
      * Gets the last modification date from the Resource.
      * 
      * @param resource
      *            The Resource.
      * @return last-modification-date
      * @throws Exception
      *             Thrown if anything fails.
      */
     public String getTheLastModificationDate(final Document resource) throws Exception {
 
         // get last-modification-date
         NamedNodeMap atts = resource.getDocumentElement().getAttributes();
         Node lastModificationDateNode = atts.getNamedItem("last-modification-date");
         return (lastModificationDateNode.getNodeValue());
 
     }
 
     /**
      * Gets the task param containing the last modification date of the specified object.
      * 
      * @param includeComment
      *            Flag indicating if the comment shall be additionally included.
      * @param id
      *            The id of the object.
      * @return Returns the created task param xml.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public String getTheLastModificationParam(final boolean includeComment, final String id) throws Exception {
         return getTheLastModificationParam(includeComment, id, null);
     }
 
     /**
      * @param includeComment
      *            Flag indicating if the comment shall be additionally included.
      * @param id
      *            The id of the object.
      * @param comment
      *            The comment for the param structure (withdraw comment).
      * @return Returns the created task param xml.
      * @throws Exception
      *             If an error occurs.
      */
     @Deprecated
     public String getTheLastModificationParam(final boolean includeComment, final String id, final String comment)
         throws Exception {
 
         String xml;
         if (includeComment) {
             xml = TaskParamFactory.getStatusTaskParam(new DateTime(getTheLastModificationDate(id)), comment);
         }
         else {
             xml = TaskParamFactory.getStatusTaskParam(new DateTime(getTheLastModificationDate(id)), null);
         }
 
         return xml;
     }
 
     /**
      * Assert that the before timestamp is lower than the after timestamp.
      * 
      * @param before
      *            Timestamp before the event
      * @param after
      *            Timestamp after event
      * @throws Exception
      *             Thrown if the before timestamp is not lower than after.
      */
     public static void assertDateBeforeAfter(final String before, final String after) throws Exception {
         Calendar oldModCal = getCalendarFromXmlDateString(before);
         Calendar newModCal = getCalendarFromXmlDateString(after);
 
         if (!oldModCal.before(newModCal)) {
             fail("Old last modification date is not before new last" + " modification date.");
         }
     }
 
     /**
      * Creates a <code>java.util.Calendar</code> object from an xml dateTime string.
      * 
      * @param dateTime
      *            The xml dateTime string.
      * @return The Calendar object.
      * @throws ParseException
      *             If the dateTime string can not be correctly parsed.
      */
     public static Calendar getCalendarFromXmlDateString(String dateTime) throws ParseException {
         Calendar cal = null;
 
         if (dateTime.length() >= 20 && dateTime.length() <= 23) {
             // no timezone
             // ensure 3 digits for millis
             int add = 23 - dateTime.length();
             while (add > 0) {
                 dateTime += "0";
                 add--;
             }
             dateTime += "+0000";
         }
         else if (dateTime.length() == 19) {
             // no timezone
             // not 3 digits for millis
             // no dot
             dateTime += ".000+0000";
         }
         // else if (dateTime.length() == 18) {
         // // no timezone
         // // not 3 digits for millis
         // // no dot
         // dateTime += "0.000+0000";
         // }
         // else if (dateTime.length() == 17) {
         // // no timezone
         // // not 3 digits for millis
         // // no dot
         // dateTime += "00.000+0000";
         // }
         // else if (dateTime.length() == 16) {
         // // no timezone
         // // not 3 digits for millis
         // // no dot
         // dateTime += ":00.000+0000";
         // }
 
         TimeZone gmt = TimeZone.getTimeZone("GMT");
 
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         DateFormat tf = new SimpleDateFormat("HH:mm:ss.SSS");
 
         Date oldModDateDate = df.parse(dateTime);
         Date oldModDateTime = tf.parse(dateTime, new ParsePosition(11));
 
         // DateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
         // dateTime = dateTime.replace('T', '-');
         // dateTime = dateTime.trim();
 
         cal = Calendar.getInstance();
         // cal.setTime(f.parse(dateTime));
         cal.setTimeZone(gmt);
 
         cal.setTime(oldModDateDate);
         long oldModDateDateMillis = cal.getTimeInMillis();
 
         cal.setTime(oldModDateTime);
         long oldModDateTimeMillis = cal.getTimeInMillis();
 
         long oldModDateMillis = oldModDateDateMillis + oldModDateTimeMillis;
         cal.setTimeInMillis(oldModDateMillis);
 
         return cal;
     }
 
     /**
      * Asserts first value to be greater or equals than second value.
      * 
      * @param greater
      *            first value.
      * @param lower
      *            second value.
      */
     protected void assertGreaterOrEquals(final int greater, final int lower) {
 
         if (lower > greater) {
             fail(lower + " is greater than " + greater);
         }
     }
 
     /**
      * Validates Item XML against the XML Schema, checks if the xml:base exists and if all placeholders are replaced.
      * 
      * @param xmlData
      *            The xml document as string.
      * @throws Exception
      *             If an error occures.
      */
     public void assertXmlValidItem(final String xmlData) throws Exception {
 
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/item/0.10/item.xsd");
         assertXmlValid(xmlData, url);
         assertXlinkXmlBaseExists(xmlData);
         assertAllPlaceholderResolved(xmlData);
         assertItemXlinkTitles(xmlData);
     }
 
     /**
      * Validates Component XML against the XML Schema, checks if the xml:base exists and if all placeholders are
      * replaced.
      * 
      * @param xmlData
      *            The xml document as string.
      * @throws Exception
      *             If an error occures.
      */
     public void assertXmlValidComponent(final String xmlData) throws Exception {
 
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/item/0.10/item.xsd");
         assertXmlValid(xmlData, url);
         assertXlinkXmlBaseExists(xmlData);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Validates ContentRelation XML against the XML Schema, checks if the xml:base exists and if all placeholders are
      * replaced.
      * 
      * @param xmlData
      *            The xml document as string.
      * @throws Exception
      *             If an error occures.
      */
     public void assertXmlValidContentRelation(final String xmlData) throws Exception {
 
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/content-relation/0.1/content-relation.xsd");
         assertXmlValid(xmlData, url);
         assertXlinkXmlBaseExists(xmlData);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Validates ContentRelations reistered predicates list XML against the XML Schema, checks if the xml:base exists
      * and if all placeholders are replaced.
      * 
      * @param xmlData
      *            The xml document as string.
      * @throws Exception
      *             If an error occures.
      */
     public void assertXMLValidRegisteredPredicates(final String xmlData) throws Exception {
 
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/content-relation/0.1/predicate-list.xsd");
         assertXmlValid(xmlData, url);
         assertXlinkXmlBaseExists(xmlData);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Validates Content Model XML against the XML Schema, if the xml:base exists and if all placeholders are resolved.
      * 
      * @param xmlData
      *            The xml document as string.
      * @throws Exception
      *             If an error occures.
      */
     public void assertXmlValidContentModel(final String xmlData) throws Exception {
 
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/content-model/0.1/content-model" + ".xsd");
         assertXmlValid(xmlData, url);
         assertXlinkXmlBaseExists(xmlData);
         assertAllPlaceholderResolved(xmlData);
     }
 
     public void assertXmlValidSetDefinition(final String xmlData) throws Exception {
 
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/set-definition/0.2/set-definition.xsd");
         assertXmlValid(xmlData, url);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Asserts there is no namespace declaration for prefixes starting with 'xml'.
      * 
      * @param xmlData
      *            The xml document as string.
      */
     public static void assertXmlPrefixNotDeclared(final String xmlData) {
         if (xmlData.contains("xmlns:xml")) {
             fail("Namespace declaration for prefixes starting with 'xml'"
                 + " is not allowed. Even not the declaration of the XML "
                 + "Namespace because MS Internet Explorer perceive it as " + "an error.");
         }
     }
 
     /**
      * Asserts that all template placeholders are replaced by values.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      */
     protected void assertAllPlaceholderResolved(final String xmlData) {
 
         assertTrue("Placeholder not resolved during rendering\n." + xmlData, !PATTERN_VELOCITY_PLACEHOLDER.matcher(
             xmlData).find());
         assertTrue("Placeholder not resolved during rendering\n." + xmlData, !PATTERN_VELOCITY_PLACEHOLDER2.matcher(
             xmlData).find());
     }
 
     /**
      * Asserts that no local href (without protocol and host) appears without xml:base.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If an error ocurres.
      */
     protected void assertXlinkXmlBaseExists(final String xmlData) throws Exception {
 
         Document document = EscidocAbstractTest.getDocument(xmlData);
         NodeList localHrefs = selectNodeList(document, "//*[starts-with(@href, '/')]");
         NodeList xmlBase = selectNodeList(document, "//@base", document);
 
         assertTrue("xml:base needed", localHrefs.getLength() == 0 || xmlBase.getLength() != 0);
     }
 
     /**
      * Assert the XML structure of the return value (task oriented methods).
      * 
      * @param xmlData
      *            The return value of task oriented method
      * @throws Exception
      *             Thrown if the XML has not the expected structure or values.
      */
     public void assertXmlValidResult(final String xmlData) throws Exception {
 
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/common/0.1/result.xsd");
         assertXmlValid(xmlData, url);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * @param xmlData
      *            The xml document as string.
      * @throws Exception
      *             If an error occures.
      */
     public static void assertXmlValidTaskParam(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/common/0.2/add-relations.xsd");
         assertXmlValid(xmlData, url);
     }
 
     public void assertXmlValidItemList(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/item/0.10/item-list.xsd");
         assertXmlValid(xmlData, url);
         assertAllPlaceholderResolved(xmlData);
     }
 
     public void assertXmlValidItemRefList(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/item/0.4/item-ref-list.xsd");
         assertXmlValid(xmlData, url);
         assertAllPlaceholderResolved(xmlData);
     }
 
     public static void assertXmlValidXmlSchema(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/common/0.2/xml-schema.xsd");
         assertXmlValid(xmlData, url);
     }
 
     public void assertXmlValidContextMembersList(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/common/0.10/member-list.xsd");
         assertXmlValid(xmlData, url);
     }
 
     public static void assertXmlValidContextMemberRefsList(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/common/0.3/member-ref-list.xsd");
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is valid for the subresource resources.
      * 
      * @param toBeAsserted
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlValidResources(final String toBeAsserted) throws Exception {
 
         if (resourcesSchema == null) {
             resourcesSchema = getSchema(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/common/0.2/resources.xsd");
         }
         assertXmlValid(toBeAsserted, resourcesSchema);
     }
 
     /**
      * Asserts that the provided xml data is a valid scope.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidScope(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/scope/0.4/scope.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid scope-list.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidScopeList(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/scope/0.4/scope-list.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid SRW response.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidSrwResponse(final String xmlData) throws Exception {
         Schema srwListSchema =
             getSchema(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/common/0.4/srw-types.xsd");
 
         assertXmlValid(xmlData, srwListSchema);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Asserts that the provided XML data is a valid struct-map.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidStructMap(final String xmlData) throws Exception {
         Schema structMapSchema =
             getSchema(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/container/0.4/struct-map.xsd");
 
         assertXmlValid(xmlData, structMapSchema);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Asserts that the provided XML data is valid agains successors XSD.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidSuccessors(final String xmlData) throws Exception {
         Schema structMapSchema =
             getSchema(getBaseUrl() + Constants.ESCIDOC_BASE_URI
                 + "/xsd/rest/organizational-unit/0.8/organizational-unit-successors.xsd");
 
         assertXmlValid(xmlData, structMapSchema);
         assertAllPlaceholderResolved(xmlData);
     }
 
     /**
      * Asserts that the provided xml data is a valid aggregation-definition.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidAggregationDefinition(final String xmlData) throws Exception {
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI
                 + "/xsd/rest/aggregation-definition/0.4/aggregation-definition.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid aggregation-definition.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidTmeResult(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/tme/jhove.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid aggregation-definition-list.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidAggregationDefinitionList(final String xmlData) throws Exception {
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI
                 + "/xsd/rest/aggregation-definition/0.4/aggregation-definition-list.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid report-definition.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidReportDefinition(final String xmlData) throws Exception {
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/report-definition/0.4/report-definition.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid report-definition-list.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidReportDefinitionList(final String xmlData) throws Exception {
         URL url =
             new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI
                 + "/xsd/rest/report-definition/0.4/report-definition-list.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is a valid report-definition.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public void assertXmlValidReport(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/report/0.4/report.xsd");
         assertAllPlaceholderResolved(xmlData);
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is valid for a search result.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlValidSearchResult(final String xmlData) throws Exception {
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/search-result/0.8/srw-types.xsd");
         assertXmlValid(xmlData, url);
     }
 
     /**
      * Asserts that the provided xml data is valid for a explain plan.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlValidExplainPlan(final String xmlData) throws Exception {
         String replacedData = xmlData;
         if (replacedData.indexOf("explainResponse") > -1) {
             replacedData = replacedData.replaceFirst("(?s).*?(<[^>]*?explain[\\s>].*?<\\/[^>]*?explain.*?>).*", "$1");
         }
         URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + "/xsd/rest/search-result/0.4/zeerex-2.0.xsd");
         assertXmlValid(replacedData, url);
     }
 
     /**
      * Assert the provided XML data is valid against the provided schema.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @param schemaURL
      *            The URL of the schema.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlValid(final String xmlData, final URL schemaURL) throws Exception {
 
         Schema theSchema = getSchema(schemaURL);
         assertXmlValid(xmlData, theSchema);
     }
 
     /**
      * Assert the provided XML data is valid against the provided schema.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @param schema
      *            The schema inputstream.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlValid(final String xmlData, final InputStream schema) throws Exception {
 
         Schema theSchema = getSchema(schema);
         assertXmlValid(xmlData, theSchema);
     }
 
     /**
      * Assert the provided XML data is valid against the provided schema.
      * 
      * @param xmlData
      *            The xml data to be asserted.
      * @param schema
      *            The schema.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlValid(final String xmlData, final Schema schema) throws Exception {
 
         assertNotNull("No Xml data. ", xmlData);
         try {
             Validator validator = schema.newValidator();
             InputStream in = new ByteArrayInputStream(xmlData.getBytes(DEFAULT_CHARSET));
             validator.validate(new SAXSource(new InputSource(in)));
         }
         catch (final Exception e) {
             final StringBuffer errorMsg = new StringBuffer("XML invalid. ");
             errorMsg.append(e.getMessage());
             if (LOGGER.isDebugEnabled()) {
                 errorMsg.append(xmlData);
                 errorMsg.append("============ End of invalid xml ============\n");
             }
             fail(errorMsg.toString());
         }
         assertXmlPrefixNotDeclared(xmlData);
     }
 
     /**
      * Asserts that the objid and href attributes of the root element exist and are consistent.
      * 
      * @param document
      *            The document.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public void assertHrefObjidConsistency(final Document document) throws Exception {
 
         assertHrefObjidConsistency(document, null);
     }
 
     /**
      * Asserts that a given string has the structure of an eSciDoc objid.
      * 
      * @param objid
      *            The string.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public void assertObjid(final String objid) throws Exception {
 
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("[a-zA-Z]+:[a-zA-Z0-9_-]+");
         if (!PATTERN_OBJID_ATTRIBUTE.matcher(objid).find()) {
             fail("Does not look like an objid: " + objid);
         }
     }
 
     /**
      * Asserts that the objid and href attributes exist and are consistent.
      * 
      * @param document
      *            The document.
      * @param xPath
      *            The xpath to the element containing the objid and href attributes. If this parameter is
      *            <code>null</code>, the root element is used.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public void assertHrefObjidConsistency(final Document document, final String xPath) throws Exception {
 
         final String objid;
         final String objidFromHref;
         if (xPath == null) {
             objid = getRootElementAttributeValue(document, NAME_OBJID);
             objidFromHref = getObjidFromHref(getRootElementHrefValue(document));
             assertEquals("Href and objid are inconsistent in root element", objidFromHref, objid);
         }
         else {
             Node objidNode = selectSingleNode(document, xPath + "/@objid");
             Node hrefNode = selectSingleNode(document, xPath + "/@href");
             assertNotNull("Objid not found for element [" + xPath + "]", objidNode);
             assertNotNull("Href not found for element [" + xPath + "]", hrefNode);
             objid = objidNode.getTextContent();
             objidFromHref = getObjidFromHref(hrefNode.getTextContent());
             assertEquals("Href and objid are inconsistent [" + xPath + "]", objidFromHref, objid);
         }
 
     }
 
     /**
      * Assert if the framework created the necessary elements and attributes.
      * 
      * @param xmlData
      *            The xml representation of the context.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlCreatedContext(final String xmlData) throws Exception {
 
         Document document = EscidocAbstractTest.getDocument(xmlData);
         assertXmlNotNull("/context/@objid", document, "/context/@objid");
         assertXmlNotNull("/context/@last-modification-date", document, "/context/@last-modification-date");
 
         assertXmlNotNull("/context/properties/creator/@href", document, "/context/properties/creator/@href");
         assertXmlNotNull("/context/properties/creator/@title", document, "/context/properties/creator/@title");
 
         assertXmlNotNull("/context/admin-descriptor/@objid", document, "/context/admin-descriptor/@objid");
         assertXmlNotNull("/context/admin-descriptor/@href", document, "/context/admin-descriptor/@href");
 
         assertXmlNotNull("/context/admin-descriptor/properties/creator/@href", document,
             "/context/admin-descriptor/properties/creator/@href");
         assertXmlNotNull("/context/admin-descriptor/properties/creator/@title", document,
             "/context/admin-descriptor/properties/creator/@title");
 
         // TODO check this list for completeness?
 
     }
 
     /**
      * Assert that the Element/Attribute selected by the xPath does not exist.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param xml
      *            XML
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlNotExists(final String message, final String xml, final String xPath) throws Exception {
 
     }
 
     /**
      * Assert that the Element/Attribute selected by the xPath does not exist.
      * 
      * @param message
      *            The message printed if assertion fails.
      * @param node
      *            The Node.
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlNotExists(final String message, final Node node, final String xPath) throws Exception {
 
         NodeList nodes = selectNodeList(node, xPath);
         assertTrue(message, nodes.getLength() == 0);
     }
 
     /**
      * Assert that the node selected by the xpath exists int the given document and is not empty.
      * 
      * @param elementLabel
      *            The label for assertion messages.
      * @param document
      *            The document.
      * @param xPath
      *            The xPath.
      * @throws Exception
      *             If anything fails.
      */
     public static void assertXmlNotNull(final String elementLabel, final Node document, final String xPath)
         throws Exception {
         Node element = selectSingleNode(document, xPath);
         assertNotNull(elementLabel + " not found!", element);
         assertFalse(elementLabel + " must not be empty!", "".equals(element.getTextContent()));
     }
 
     /**
      * Extracts the id from the href attribute of the root element of the provided document.
      * 
      * @param document
      *            The document to retrieve the id from.
      * @return Returns the extracted id value.
      * @throws Exception
      *             If anything fails.
      */
     public static String getIdFromRootElementHref(final Document document) throws Exception {
 
         return getObjidFromHref(getRootElementHrefValue(document));
     }
 
     /**
      * Obtain the objid from the href.
      * 
      * @param val
      *            The href attribute.
      * @return objid
      */
     public String getIdFromHrefValue(final String val) {
         String result = null;
         // FIXME it's no objid pattern
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile(".*\\/([^\"\\/]*)");
 
         Matcher m1 = PATTERN_OBJID_ATTRIBUTE.matcher(val);
         if (m1.find()) {
             result = m1.group(1);
         }
         return result;
     }
 
     /**
      * Obtain the objid from the XML root element.
      * 
      * @param xml
      *            The XML data.
      * @return objid
      */
     public String getIdFromRootElement(final String xml) {
         String result = null;
         // FIXME PATTERN_OBJID_ATTRIBUTE is static field !
         // FIXME this pattern does not work for componentId
         Pattern pATTERNoBJIDaTTRIBUTEjUSTfORtHISmETHOD = Pattern.compile("href=\"/ir/[^/]+/([^\"]*)\"");
         Matcher m1 = pATTERNoBJIDaTTRIBUTEjUSTfORtHISmETHOD.matcher(xml);
         if (m1.find()) {
             result = m1.group(1);
         }
 
         return result;
     }
 
     /**
      * Gets the id from the provided uri (href).
      * 
      * @param href
      *            The uri to extract the id from.
      * @return Returns the extracted id.
      */
     public static String getObjidFromHref(final String href) {
 
         String grantId = href.substring(href.lastIndexOf('/') + 1);
         return grantId;
     }
 
     /**
      * Gets the href attribute of the root element from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @return Returns the attribute value.
      * @throws Exception
      *             If anything fails.
      */
     public static String getRootElementHrefValue(final Document document) throws Exception {
 
         return getRootElementAttributeValueNS(document, NAME_HREF, XLINK_NS_URI);
     }
 
     /**
      * Gets the objid attribute of the element selected in the provided node.<br>
      * It tries to get the objid attribute of the selected node. If this fails, it tries to get the xlink:href
      * attribute. If both fails, an assertion exception is "thrown".
      * 
      * @param node
      *            The node to select an element from.
      * @param xPath
      *            The xpath to select the element in the provided node.
      * @return Returns the attribute value.
      * @throws Exception
      *             If anything fails.
      */
     public String getObjidValue(final Node node, final String xPath) throws Exception {
 
         Node selected = selectSingleNode(node, xPath);
         assertNotNull("No Element selected to retrieve the object id from", selected);
         NamedNodeMap attributes = selected.getAttributes();
         assertNotNull("Selected node has no attributes (not an element?) ", attributes);
         Node objidAttr = attributes.getNamedItem(NAME_OBJID);
         if (objidAttr != null) {
             return objidAttr.getTextContent();
         }
         else {
             objidAttr = selectSingleNode(selected, "." + PART_XLINK_HREF);
             assertNotNull("Selected node neither has an objid " + "attribute nor an xlink href attribute", objidAttr);
             return getObjidFromHref(objidAttr.getTextContent());
         }
     }
 
     /**
      * Gets the objid attribute of the root element from the Xml.
      * 
      * @param xml
      *            The xml representation of an object to retrieve the value from.
      * @return Returns the objid.
      * @throws Exception
      *             If anything fails.
      */
     public String getObjidValue(final String xml) throws Exception {
 
         Matcher m = PATTERN_OBJID_ATTRIBUTE.matcher(xml);
         if (m.find()) {
             return m.group(1);
         }
         else {
             fail("Missing objid in provided xml data");
             return null;
         }
     }
 
     /**
      * Get objId with version part of latest version of document.
      * 
      * @param xml
      *            The Item XML.
      * @return The object id of the latest version.
      * @throws Exception
      *             If anything fails.
      */
     public final String getLatestVersionObjidValue(final String xml) throws Exception {
 
         Node latestVersionNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "//properties/latest-version/number");
         String id = getIdFromRootElement(xml) + VERSION_SUFFIX_SEPARATOR + latestVersionNode.getTextContent();
         return (id);
     }
 
     /**
      * Remove version informaion from given objid.
      * 
      * @param objid
      *            The objid.
      * @return The objid without version information.
      */
     public static String getObjidWithoutVersion(final String objid) {
 
         String result = objid;
         Matcher m = PATTERN_ID_WITHOUT_VERSION.matcher(objid);
         if (m.find()) {
             result = m.group(1);
         }
         return result;
     }
 
     /**
      * Gets the last-modification-date attribute of the root element from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @return Returns the attribute value.
      * @throws Exception
      *             If anything fails.
      */
     public static String getLastModificationDateValue(final Document document) throws Exception {
 
         return getRootElementAttributeValue(document, "last-modification-date");
     }
 
     /**
      * Gets the last-modification-date attribute of the root element from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @return Returns the attribute value.
      * @throws Exception
      *             If anything fails.
      */
     public static DateTime getLastModificationDateValue2(final Document document) throws Exception {
 
         String dateString = getRootElementAttributeValue(document, "last-modification-date");
         if (dateString == null) {
             return null;
         }
         final Calendar calendar = DatatypeConverter.parseDate(dateString);
         return new DateTime(calendar.getTimeInMillis(), DateTimeZone.forTimeZone(calendar.getTimeZone()));
     }
 
     /**
      * Gets the creation-date element of the first element named "properties" from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @return Returns the creation date value or <code>null</code>.
      * @throws Exception
      *             If anything fails.
      */
     public static String getCreationDateValue(final Document document) throws Exception {
 
         return getCreationDateValue(document, null);
     }
 
     /**
      * Gets the creation-date element of the specified properties element from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @param xPath
      *            The xpath to the parent element that contains the creation date element. If this is <code>null</code>,
      *            the first element named "properties" will be selected.
      * @return Returns the creation date value or <code>null</code>.
      * @throws Exception
      *             If anything fails.
      */
     public static String getCreationDateValue(final Document document, final String xPath) throws Exception {
 
         String creationDateXpath = "";
         if (xPath == null) {
             creationDateXpath = "//properties[1]/creation-date";
         }
         else {
             creationDateXpath = xPath + "/creation-date";
         }
         final Node creationDateElement = selectSingleNode(document, creationDateXpath);
         if (creationDateElement == null) {
             return null;
         }
         else {
             return creationDateElement.getTextContent();
         }
     }
 
     /**
      * Get objid from ordered component.
      * 
      * @param document
      *            the document.
      * @param componentNo
      *            the component order number.
      * @return component object id
      * @throws Exception
      *             Thrown in case of internal error.
      */
     // TODO should better be in OmTestBase
     public String getComponentObjidValue(final Document document, final int componentNo) throws Exception {
         return getObjidFromHref(getAttributeValue(document, OmTestBase.XPATH_ITEM_COMPONENTS + "/component["
             + componentNo + "]", "href"));
     }
 
     /**
      * Get objid from component.
      * 
      * @param document
      *            the document.
      * @param xpath
      *            tXPath identifying the component.
      * @return component object id
      * @throws Exception
      *             Thrown in case of internal error.
      */
     public String getComponentObjidValue(final Document document, final String xpath) throws Exception {
         return getObjidFromHref(getAttributeValue(document, xpath, "href"));
     }
 
     /**
      * Asserts that the creation-date element of the document exists.
      * 
      * @param message
      *            The fail message.
      * @param document
      *            The document to retrieve the value from.
      * @return Returns the creation date value.
      * @throws Exception
      *             If anything fails.
      */
     public static String assertCreationDateExists(final String message, final Document document) throws Exception {
 
         return assertCreationDateExists(message, document, null);
     }
 
     /**
      * Asserts that the creation-date element of the specified properties element from the document exists.
      * 
      * @param message
      *            The fail message.
      * @param document
      *            The document to retrieve the value from.
      * @param xPath
      *            The xpath to the parent element that contains the creation date element. If this is <code>null</code>,
      *            the first element named "properties" will be selected.
      * @return Returns the creation date value.
      * @throws Exception
      *             If anything fails.
      */
     public static String assertCreationDateExists(final String message, final Document document, final String xPath)
         throws Exception {
 
         final String creationDate = getCreationDateValue(document, xPath);
         assertNotNull(prepareAssertionFailedMessage(message) + "No creation-date", creationDate);
         return creationDate;
     }
 
     /**
      * Gets the value of the specified attribute of the root element from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @param attributeName
      *            The name of the attribute whose value shall be retrieved.
      * @param namespaceURI
      *            The namespace URI of the attribute.
      * @return Returns the attribute value.
      * @throws Exception
      *             If anything fails.
      */
     public static String getRootElementAttributeValueNS(
         final Document document, final String attributeName, final String namespaceURI) throws Exception {
 
         Node root = getRootElement(document);
         if (root.getNamespaceURI() != null) {
             // has been parsed namespace aware
             Node attr = root.getAttributes().getNamedItemNS(namespaceURI, attributeName);
             assertNotNull("Attribute not found [" + namespaceURI + ":" + attributeName + "]. ", attr);
             return attr.getTextContent();
         }
         else {
             // has not been parsed namespace aware.
             String xPath;
             if (attributeName.startsWith("@")) {
                 xPath = "/*/" + attributeName;
             }
             else {
                 xPath = "/*/@" + attributeName;
             }
             assertXmlExists("Attribute not found [" + xPath + "]. ", document, xPath);
             final Node attr = selectSingleNode(root, xPath);
             assertNotNull("Attribute not found [" + attributeName + "]. ", attr);
             String value = attr.getTextContent();
             return value;
         }
     }
 
     /**
      * Gets the value of the specified attribute of the root element from the document.
      * 
      * @param document
      *            The document to retrieve the value from.
      * @param attributeName
      *            The name of the attribute whose value shall be retrieved.
      * @return Returns the attribute value.
      * @throws Exception
      *             If anything fails.
      */
     public static String getRootElementAttributeValue(final Document document, final String attributeName)
         throws Exception {
 
         Node root = getRootElement(document);
 
         // has not been parsed namespace aware.
         String xPath;
         if (attributeName.startsWith("@")) {
             xPath = "/*/" + attributeName;
         }
         else {
             xPath = "/*/@" + attributeName;
         }
         assertXmlExists("Attribute not found [" + attributeName + "]. ", document, xPath);
         final Node attr = selectSingleNode(root, xPath);
         assertNotNull("Attribute not found [" + attributeName + "]. ", attr);
         String value = attr.getTextContent();
         return value;
     }
 
     /**
      * Gets the <code>Schema</code> object for the provided url.
      * 
      * @param urlString
      *            The <code>String</code> specifying the URL.
      * @return Returns the <code>Schema</code> object.
      * @throws Exception
      *             If anything fails.
      */
     public static Schema getSchema(final String urlString) throws Exception {
 
         URL url = urlCache.get(urlString);
         if (url == null) {
             url = new URL(urlString);
             urlCache.put(urlString, url);
         }
         return getSchema(url);
     }
 
     /**
      * Gets the <code>Schema</code> object for the provided <code>URL</code>.
      * 
      * @param url
      *            The url to get the schema for.
      * @return Returns the <code>Schema</code> object.
      * @throws Exception
      *             If anything fails.
      */
     public static Schema getSchema(final URL url) throws Exception {
 
         Schema schema = schemaCache.get(url);
         if (schema == null) {
             URLConnection conn = url.openConnection();
             InputStream schemaStream = conn.getInputStream();
             schema = getSchema(schemaStream);
             schemaCache.put(url, schema);
         }
         return schema;
     }
 
     /**
      * Gets the <code>Schema</code> object for the provided <code>InputStream</code>.
      * 
      * @param schemaStream
      *            The Stream containing the schema.
      * @return Returns the <code>Schema</code> object.
      * @throws Exception
      *             If anything fails.
      */
     private static Schema getSchema(final InputStream schemaStream) throws Exception {
 
         if (schemaStream == null) {
             throw new Exception("No schema input stream provided");
         }
 
         SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         // set resource resolver to change schema-location-host
         sf.setResourceResolver(new SchemaBaseResourceResolver());
         Schema theSchema = sf.newSchema(new SAXSource(new InputSource(schemaStream)));
         return theSchema;
     }
 
     /**
      * Asserts that ts1 depicts a time after ts2 (ts1 > ts2).
      * 
      * @param message
      *            The message is the assertion fails.
      * @param ts1
      *            The first timestamp.
      * @param ts2
      *            The second timestamp.
      * @throws Exception
      *             If anything fails (e.g. one timstamp has incorrect format)
      */
     public static void assertTimestampIsEqualOrAfter(final String message, final String ts1, final String ts2)
         throws Exception {
 
         assertTrue(message, compareTimestamps(ts1, ts2) >= 0);
     }
 
     /**
      * Asserts that ts1 and ts2 depict the same time (ts1 == ts2).
      * 
      * @param message
      *            The message is the assertion fails.
      * @param ts1
      *            The first timestamp.
      * @param ts2
      *            The second timestamp.
      * @throws Exception
      *             If anything fails (e.g. one timstamp has incorrect format)
      */
     public static void assertTimestampEquals(final String message, final String ts1, final String ts2) throws Exception {
 
         assertTrue(message, compareTimestamps(ts1, ts2) == 0);
     }
 
     /**
      * Returns a positive integer if ts1 depicts a time after ts2 (ts1 > ts2), 0 if ts1 and ts2 depict the same time
      * (ts1 == ts2), and a negative integer if if ts1 depicts a time before ts2 (ts1 < ts2).
      * 
      * @param ts1
      *            The first timestamp.
      * @param ts2
      *            The second timestamp.
      * @return The comparison result.
      * @throws Exception
      *             If anything fails (e.g. one timstamp has incorrect format).
      */
     public static int compareTimestamps(final String ts1, final String ts2) throws Exception {
 
         int result = 0;
 
         XMLGregorianCalendar date1 = DatatypeFactory.newInstance().newXMLGregorianCalendar(ts1);
         XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(ts2);
 
         int diff = date1.compare(date2);
         if (diff == DatatypeConstants.LESSER) {
             result = -1;
         }
         else if (diff == DatatypeConstants.GREATER) {
             result = 1;
         }
         else if (diff == DatatypeConstants.EQUAL) {
             result = 0;
         }
         else if (diff == DatatypeConstants.INDETERMINATE) {
             throw new Exception("Date comparing: INDETERMINATE");
         }
 
         return result;
     }
 
     /**
      * Get the current time as timestamp. The date format is yyyy-MM-dd'T'HH:mm:ss.SSSZ.
      * 
      * @return The current time as timestamp.
      */
     public static String getNowAsTimestamp() {
 
         DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         return dateFormat.format(new Date()).replaceAll("\\+0000", "Z").replaceAll("([+-][0-9]{2}):([0-9]{2})", "$1$2");
     }
 
     /**
      * 
      * @param timestamp
      * @return
      */
     public static String normalizeTimestamp(final String timestamp) {
 
         return timestamp.replaceAll("Z", "+0000").replaceAll("([+-][0-9]{2}):([0-9]{2})", "$1$2");
     }
 
     /**
      * Return a unique name. It is the concatenation of the prefix and the current time in milli seconds.
      * 
      * @param prefix
      *            The prefix.
      * @return The unique name.
      */
     public static String getUniqueName(final String prefix) {
 
         return prefix + System.currentTimeMillis();
     }
 
     /**
      * Makes the value of the provided node unique by adding a timestamp to it.
      * 
      * @param node
      *            The node to find the "name" element in and make it unique.
      * @return Returns the unique value.
      */
     public String setUniqueName(final Node node) {
 
         final String uniqueName = getUniqueName(node.getTextContent().trim());
         node.setTextContent(uniqueName);
         return uniqueName;
     }
 
     /**
      * Makes the value of the selected node of the provided node unique by adding a timestamp to it.<br>
      * If no node can be selected, an assertion fails.
      * 
      * @param node
      *            The node to find the "name" element in and make it unique.
      * @param xpath
      *            The xpath selecting the "name" element to change.
      * @return Returns the unique value.
      * @throws Exception
      *             If anything fails.
      */
     public String setUniqueValue(final Node node, final String xpath) throws Exception {
 
         assertNotNull("No node provided to select a node in.", node);
         Node selected = selectSingleNodeAsserted(node, xpath);
         return setUniqueName(selected);
     }
 
     /**
      * Changes the given template. If currentElement (e.g. /context/properties/creation-date) is not a key in the
      * elements Map, currentElement is interpreted as a xpath and the selected node is removed from the template. If the
      * value for key currentElement is different from the empty String, the selected node's value is substituted with
      * this value.
      * 
      * @param template
      *            The context template.
      * @param elements
      *            The elements Map.
      * @param currentElement
      *            The currentElemnt.
      * @return The resulting template.
      * @throws Exception
      *             If anything fails.
      */
     public static Node changeTemplateWithReadOnly(
         final Node template, final Map<String, String> elements, final String currentElement) throws Exception {
         Node result = template;
         if (elements.get(currentElement) == null) {
             deleteNodes(template, currentElement);
             //
             // if (currentElement.indexOf("@") != -1) {
             // String xpath = currentElement.substring(0, currentElement
             // .indexOf("@") - 1);
             // String attribute = currentElement.substring(currentElement
             // .indexOf("@") + 1);
             // deleteAttribute(result, xpath, attribute);
             // }
             // else {
             // result = deleteElement(result, currentElement);
             // }
         }
         else if (!"".equals(elements.get(currentElement))) {
             String element = currentElement;
             if (element.indexOf("@" + XLINK_HREF_TEMPLATES) != -1) {
                 element = element.replaceAll("@" + XLINK_HREF_TEMPLATES, "@href");
             }
             result = substitute(result, element, elements.get(currentElement));
         }
         return result;
     }
 
     /**
      * Gets the root element of the provided document.
      * 
      * @param doc
      *            The document to get the root element from.
      * @return Returns the first child of the document htat is an element node.
      * @throws Exception
      *             If anything fails.
      */
     public static Element getRootElement(final Document doc) throws Exception {
 
         Node node = doc.getFirstChild();
         while (node != null) {
             if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                 return (Element) node;
             }
             node = node.getNextSibling();
         }
         return null;
     }
 
     /**
      * Inserts the namespaces into the provided element node.
      * 
      * @param element
      *            The element into that the namespace definitions shall be inserted.
      * @return Returns the changed element node.
      * @throws Exception
      *             If anything fails.
      */
     public Element insertNamespaces(final Element element) throws Exception {
 
         element.setAttribute("xmlns:prefix-container", CONTAINER_NS_URI);
         element.setAttribute("xmlns:prefix-content-type", CONTENT_TYPE_NS_URI);
         element.setAttribute("xmlns:prefix-context", CONTEXT_NS_URI);
         element.setAttribute("xmlns:prefix-dc", DC_NS_URI);
         element.setAttribute("xmlns:prefix-dcterms", DCTERMS_NS_URI);
         element.setAttribute("xmlns:prefix-grants", GRANTS_NS_URI);
         element.setAttribute("xmlns:prefix-internal-metadata", INTERNAL_METADATA_NS_URI);
         element.setAttribute("xmlns:prefix-item", ITEM_NS_URI);
         element.setAttribute("xmlns:prefix-member-list", MEMBER_LIST_NS_URI);
         element.setAttribute("xmlns:prefix-member-ref-list", MEMBER_REF_LIST_NS_URI);
         element.setAttribute("xmlns:prefix-metadata", METADATA_NS_URI);
         element.setAttribute("xmlns:prefix-metadatarecords", METADATARECORDS_NS_URI);
         element.setAttribute("xmlns:prefix-organizational-unit", ORGANIZATIONAL_UNIT_NS_URI);
         element.setAttribute("xmlns:prefix-properties", PROPERTIES_NS_URI);
         element.setAttribute("xmlns:prefix-schema", SCHEMA_NS_URI);
         element.setAttribute("xmlns:prefix-staging-file", STAGING_FILE_NS_URI);
         element.setAttribute("xmlns:prefix-user-account", USER_ACCOUNT_NS_URI);
         element.setAttribute("xmlns:prefix-xacml-context", XACML_CONTEXT_NS_URI);
         element.setAttribute("xmlns:prefix-xacml-policy", XACML_POLICY_NS_URI);
         element.setAttribute("xmlns:prefix-xlink", XLINK_NS_URI);
         element.setAttribute("xmlns:prefix-xsi", XSI_NS_URI);
         return element;
     }
 
     /**
      * Inserts the namespaces into the root element of the provided document.
      * 
      * @param doc
      *            The document that shall be changed.
      * @return Returns the changed document.
      * @throws Exception
      *             If anything fails.
      */
     public Document insertNamespacesInRootElement(final Document doc) throws Exception {
 
         final Element rootElement = getRootElement(doc);
         assertNotNull("No root element found in the provided document", rootElement);
         insertNamespaces(rootElement);
         return doc;
     }
 
     /**
      * Inserts the namespaces into the root element of the provided xml data.
      * 
      * @param xmlData
      *            The xmlData that shall be changed.
      * @return Returns the changed XML data.
      * @throws Exception
      *             If anything fails.
      */
     public String insertNamespacesInRootElement(final String xmlData) throws Exception {
 
         return toString(insertNamespacesInRootElement(EscidocAbstractTest.getDocument(xmlData)), false);
     }
 
     protected static String prepareAssertionFailedMessage(final String message) {
 
         final String msg;
         if (message == null) {
             msg = "";
         }
         else if (!message.endsWith(" ")) {
             msg = message + " ";
         }
         else {
             msg = message;
         }
         return msg;
     }
 
     /**
      * Deletes the node selected by the given XPath from the provided node.
      * 
      * @param node
      *            The Node to delete the selected nodes from.
      * @param xPath
      *            The XPath selecting the sub nodes in the provided node.
      * @return returns the provided <code>Node</code> object. This <code>Node</code> object may be changed.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static Node deleteNodes(final Node node, final String xPath) throws Exception {
 
         NodeList nodes = selectNodeList(node, xPath);
         if (nodes == null || nodes.getLength() == 0) {
             return node;
         }
 
         for (int i = 0; i < nodes.getLength(); ++i) {
             Node delete = nodes.item(i);
             if (delete.getNodeType() == Node.ATTRIBUTE_NODE) {
                 final int index = xPath.lastIndexOf('/');
                 String attribute = delete.getNodeName();
                 attribute = attribute.substring(attribute.lastIndexOf(':') + 1);
                 String elementXpath = xPath.substring(0, index);
                 elementXpath += "[@" + attribute + "=\"" + delete.getTextContent().trim() + "\"]";
                 Node parent = selectSingleNode(node, elementXpath);
                 if (parent.hasAttributes()) {
                     parent.getAttributes().removeNamedItem(delete.getNodeName());
                 }
             }
             else {
                 delete.getParentNode().removeChild(delete);
             }
         }
 
         return node;
     }
 
     /**
      * Asserts that the value of the selected node of the provided node starts with the expected base.
      * 
      * @param node
      *            The node in that the node shall be selected abd asserted.
      * @param xPath
      *            The Xpath to select the node that shall be asserted.
      * @param expectedBase
      *            The expected value with that the selected node's value shall start.
      * @return Returns the value of the node that has been successfully checked.
      * @throws Exception
      *             Thrown if anything fails.
      */
     public static String assertHrefBase(final Node node, final String xPath, final String expectedBase)
         throws Exception {
 
         final String value = selectSingleNode(node, xPath).getTextContent();
         assertTrue("href does not start with " + expectedBase, value.startsWith(expectedBase));
 
         return value;
     }
 
     /**
      * Asserts that the selected xlink:type attribute of the provided node has the value "simple".
      * 
      * @param document
      *            The node from that the xlink:type attribute shall be selected and asserted.
      * @throws Exception
      *             If an error ocurres.
      */
     public static void assertXlinkType(final Document document, final String xPath) throws Exception {
 
         assertXmlExists("No xlink:type attribute found [" + xPath + "]", document,
             XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_TYPE);
         assertXmlEquals("Unexpected xlink:type [" + xPath + "]", document, xPath, "simple");
     }
 
     /**
      * Modifies the namespace prefixes of the provided xml data by adding a prefix to the namespac.
      * 
      * @param xml
      *            The xml data to change the namespace prefixes in.
      * @return Returns the modified xml data.
      */
     public static String modifyNamespacePrefixes(final String xml) {
 
         Matcher matcher = PATTERN_MODIFY_NAMESPACE_PREFIXES_REPLACE_PREFIXES.matcher(xml);
         String ret = matcher.replaceAll("$1prefix-$2");
         matcher = PATTERN_MODIFY_NAMESPACE_PREFIXES_FIX_NAMESPACE_DECLARATIONS.matcher(ret);
         ret = matcher.replaceAll("xmlns:prefix-$1");
         matcher = PATTERN_MODIFY_NAMESPACE_PREFIXES_FIX_PREFIX_XML.matcher(ret);
         ret = matcher.replaceAll("xml");
         return ret;
     }
 
     public void assertRdfList(
         final Document xmlDoc, final String objectTypeUri, final String orderByPropertyUri, final boolean descending,
         final int limit, final int offset) throws Exception {
         selectSingleNodeAsserted(xmlDoc, "/RDF");
         selectSingleNodeAsserted(xmlDoc, "/RDF/Description");
 
         NodeList descriptions = selectNodeList(xmlDoc, "/RDF/Description");
 
         if (limit != 0) {
             assertOrderNotAfter(descriptions.getLength(), limit);
         }
 
         for (int i = 0; i < descriptions.getLength(); i++) {
             NodeList nl = selectNodeList(descriptions.item(i), "type");
             boolean foundTypeObjectTypeUri = false;
             for (int j = 0; j < nl.getLength(); j++) {
                 Node n = nl.item(j);
                 NamedNodeMap nnm = n.getAttributes();
                 Node att = nnm.getNamedItem("rdf:resource");
                 String uri = att.getNodeValue();
                 if (uri.equals(objectTypeUri)) {
                     foundTypeObjectTypeUri = true;
                 }
             }
             if (!foundTypeObjectTypeUri) {
                 String about = selectSingleNode(descriptions.item(i), "@about").getNodeValue();
                 fail("Could not find type element refering " + objectTypeUri + " in RDF description of " + about + ".");
             }
         }
 
         if (orderByPropertyUri != null) {
             String localName = orderByPropertyUri.substring(orderByPropertyUri.lastIndexOf('/') + 1);
             Node orderNodeA = null;
             Node orderNodeB = null;
             // init order node A
             NodeList nl = selectNodeList(descriptions.item(0), localName);
             for (int j = 0; j < nl.getLength(); j++) {
                 // FIXME compare with namespace
                 orderNodeA = nl.item(j);
             }
             for (int i = 1; i < descriptions.getLength(); i++) {
                 nl = selectNodeList(descriptions.item(i), localName);
                 for (int j = 0; j < nl.getLength(); j++) {
                     // FIXME compare with namespace
                     // String curNsUri = nl.item(j).getNamespaceURI();
                     // if (nsUri.equals(curNsUri)) {
                     orderNodeB = nl.item(j);
                     // }
                 }
                 if (descending) {
                     assertOrderNotAfter(orderNodeB.getTextContent(), orderNodeA.getTextContent());
                 }
                 else {
                     assertOrderNotAfter(orderNodeA.getTextContent(), orderNodeB.getTextContent());
                 }
                 orderNodeA = orderNodeB;
             }
         }
         else {
             String orderValueXPath = "@about";
             // "/Description/"
             // + orderByPropertyUri.substring(orderByPropertyUri
             // .lastIndexOf('/') + 1);
             for (int i = 1; i < descriptions.getLength(); i++) {
                 int a, b;
                 if (descending) {
                     a = i;
                     b = i - 1;
                 }
                 else {
                     a = i - 1;
                     b = i;
                 }
                 String lower = selectSingleNodeAsserted(descriptions.item(a), orderValueXPath).getNodeValue();
                 String higher = selectSingleNodeAsserted(descriptions.item(b), orderValueXPath).getNodeValue();
                 assertOrderNotAfter(lower, higher);
             }
         }
 
     }
 
     public static void assertOrderNotAfter(final int lower, final int higher) throws Exception {
         if (lower > higher) {
             fail("Incorrect order: " + lower + " < " + higher + ".");
         }
     }
 
     public static void assertOrderNotAfter(final String lower, final String higher) throws Exception {
         if (lower.compareTo(higher) > 0) {
             LOGGER.debug("Incorrect order: " + lower + " < " + higher + ".");
         }
     }
 
     public void assertContentStreamsOf_escidoc_item_198_for_create_3content_streams(
         final String itemId, final Document itemDoc, final boolean isRootContentStreams) throws Exception {
 
         // there should be content streams
         selectSingleNodeAsserted(itemDoc, "//content-streams[1]");
         // but only once
         assertNull(selectSingleNode(itemDoc, "//content-streams[2]"));
         if (isRootContentStreams) {
             // there should be attributes xml:base, xlink:href and
             // last-modification-date in content streams container
             selectSingleNodeAsserted(itemDoc, "/content-streams[@base]");
             // = '" + Constants.PROTOCOL + "://" + Constants.HOST_PORT + "']");
             selectSingleNodeAsserted(itemDoc, "/content-streams[@href = '/ir/item/" + itemId + "/content-streams']");
             selectSingleNodeAsserted(itemDoc, "/content-streams[@last-modification-date]");
             // TODO check if latter is date
         }
         // there should be three content streams
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[3]");
         // not more
         assertNull(selectSingleNode(itemDoc, "//content-streams/content-stream[4]"));
         // one of each storage type
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='external-managed']");
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='internal-managed']");
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='external-url']");
         // check content URLs
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='external-managed'"
             + " and starts-with(@href,'/ir/item/')]");
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='internal-managed'"
             + " and starts-with(@href,'/ir/item/')]");
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='external-url'"
             + " and starts-with(@href,'http://')]");
 
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='external-managed'"
             + " and @name='external_image' and @mime-type='image/jpeg'" + " and @href = '/ir/item/" + itemId
             + "/content-streams/content-stream/external_image/content']");
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='internal-managed'"
             + " and @name='internal_xml' and @mime-type='text/xml'" + " and @href = '/ir/item/" + itemId
             + "/content-streams/content-stream/internal_xml/content']");
         selectSingleNodeAsserted(itemDoc, "//content-streams/content-stream[@storage='external-url'"
             + " and @name='redirect_image' and @mime-type='image/jpeg']");
 
     }
 
     /**
      * Uploading file to Staging Service and get URL back.
      * 
      * @param file
      *            The file which is to upload to the staging service.
      * @return The URL at the staging service.
      * @throws Exception
      *             Thrown if uploading failed.
      */
     public URL uploadFileToStagingServlet(final File file) throws Exception {
 
         String mimeType = file.toURI().toURL().openConnection().getContentType();
         return uploadFileToStagingServlet(file, mimeType);
     }
 
     /**
      * Uploading file to Staging Service and get URL back.
      * 
      * @param file
      *            The file (which is to upload).
      * @param mimeType
      *            The mime type of the content.
      * @return The URL at the staging service.
      * @throws Exception
      *             Thrown if uploading failed.
      */
     public URL uploadFileToStagingServlet(final File file, final String mimeType) throws Exception {
 
         InputStream fileInputStream = new FileInputStream(file);
 
         return uploadFileToStagingServlet(fileInputStream, file.getName(), mimeType);
     }
 
     /**
      * Uploading file to Staging Service and get URL back.
      * 
      * @param fileInputStream
      *            The filenInputStream (whic upload).
      * @param filename
      *            The name of the file.
      * @param mimeType
      *            The mime type of the content.
      * @return The URL fro the staging service.
      * @throws Exception
      *             Thrown if uploading failed.
      */
     public URL uploadFileToStagingServlet(
         final InputStream fileInputStream, final String filename, final String mimeType) throws Exception {
 
         Object result = getStagingFileClient().create(fileInputStream, mimeType, filename);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             final String stagingFileXml = EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8);
             EntityUtil.consumeContent(httpRes.getEntity());
             Document document = EscidocAbstractTest.getDocument(stagingFileXml);
             Node fileHref = selectSingleNode(document, "/staging-file/@href");
             URL url = new URL(getBaseUrl() + Constants.ESCIDOC_BASE_URI + fileHref.getTextContent());
 
             return url;
         }
         else {
             fail("Unsupported result type [" + result.getClass().getName() + "]");
             throw new Exception("Upload to staging service failed.");
         }
     }
 
     /**
      * @return Returns the itemClient.
      */
     public StagingFileClient getStagingFileClient() {
         return this.stagingFileClient;
     }
 
     /**
      * count the number of testMethods in the testClass.
      * 
      * @return number of testMethods.
      */
     public int getTestMethodCount() {
         Method[] methods = this.getClass().getMethods();
         int count = 0;
         for (int i = 0; i < methods.length; i++) {
             if (methods[i].getName().startsWith("test") && methods[i].getParameterTypes().length == 0
                 && methods[i].getReturnType().getName().equals("void")) {
                 count++;
             }
         }
         return count;
     }
 
     /**
      * count the number of testMethods in the testClass.
      * 
      * @return number of testMethods.
      */
     public int getTestAnnotationsCount() {
         Method[] methods = this.getClass().getMethods();
         int count = 0;
         for (int i = 0; i < methods.length; i++) {
             if (methods[i].getAnnotations() != null) {
                 for (Annotation annotation : methods[i].getAnnotations()) {
                     if (annotation.annotationType().equals(org.junit.Test.class)) {
                         count++;
                     }
                 }
             }
         }
         return count;
     }
 
     /**
      * Assert that all Xlink titles match the (eSciDoc) Xlink title conventions.
      * <p/>
      * TODO this test is not complete, because of an outstanding definition. (see issue INFR-865)
      * 
      * @param xmlData
      *            XML of the Item
      */
     public void assertItemXlinkTitles(final String xmlData) throws Exception {
         Document document = EscidocAbstractTest.getDocument(xmlData);
         // relations
         Node relations = XPathAPI.selectSingleNode(document, "/item/relations/@title", document);
         if (relations != null) {
             assertEquals("Xlink:title of relations differs from convention", "Relations of Item", relations
                 .getTextContent());
         }
     }
 
     /**
      * Assert that all Xlink titles match the (eSciDoc) Xlink title conventions.
      * <p/>
      * TODO this test is not complete, because of an outstanding definition. (see issue INFR-865)
      * 
      * @param xmlData
      *            XML of the Container
      */
     public void assertContainerXlinkTitles(final String xmlData) throws Exception {
         Document document = EscidocAbstractTest.getDocument(xmlData);
         // relations
         Node relations = XPathAPI.selectSingleNode(document, "/container/relations/@title", document);
         if (relations != null) {
             assertEquals("Xlink:title of relations differs from convention", "Relations of Container", relations
                 .getTextContent());
         }
     }
 
     /**
      * Obtain version number of framework by requesting it from Admin Service.
      * 
      * @return version number of framework.
      * @throws Exception
      *             Thrown if request failed.
      */
     public String obtainFrameworkVersion() throws Exception {
 
         if (REPOSITORY_VERSION == null) {
 
             AdminClient admClient = new AdminClient();
             String info = handleXmlResult(admClient.getRepositoryInfo());
 
             Pattern p = Pattern.compile(".*<entry key=\"escidoc-core.build\">([^<]*)</entry>.*");
             Matcher m = p.matcher(info);
             if (m.find()) {
                 REPOSITORY_VERSION = m.group(1);
             }
             else {
                 throw new Exception("Cannot obtain framework version " + "from eSciDoc Core installation.");
             }
         }
         return REPOSITORY_VERSION;
     }
 
 }
