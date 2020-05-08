 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.api.ws;
 
 import javax.xml.namespace.QName;
 
 
 /**
  * Holds constants for the SafeOnline web services.
  *
  * @author fcorneli
  */
 public abstract class WebServiceConstants {
 
     public static final String SAFE_ONLINE_SAML_NAMESPACE = "urn:net:lin-k:safe-online:saml";
 
     public static final String SAFE_ONLINE_SAML_PREFIX = "sosaml";
 
     public static final QName MULTIVALUED_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "multivalued", SAFE_ONLINE_SAML_PREFIX );
    public static final QName ATTRIBUTE_ID          = new QName( SAFE_ONLINE_SAML_NAMESPACE, "attributeId", SAFE_ONLINE_SAML_PREFIX );

    public static final QName XML_DSIG_NS = new QName( "http://www.w3.org/2000/09/xmldsig#", "type", "ds" );
 
     public static final QName XML_SCHEMA_INSTANCE_TYPE = new QName( "http://www.w3.org/2001/XMLSchema-instance", "type", "xsi" );
    public static final QName XML_SCHEMA_INSTANCE_NIL  = new QName( "http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi" );
 
     public static final QName DATAMINING_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "datamining", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName DATATYPE_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "datatype", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName OPTIONAL_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "optional", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName CONFIRMATION_REQUIRED_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "confirmationRequired",
             SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName CONFIRMED_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "confirmed", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName GROUP_NAME_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "groupName", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName SAML_QUERY_STRING_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "queryString", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName SAML_REQUEST_URL_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "requestUrl", SAFE_ONLINE_SAML_PREFIX );
 
     public static final QName SAML_AUDIENCE_ATTRIBUTE = new QName( SAFE_ONLINE_SAML_NAMESPACE, "audience", SAFE_ONLINE_SAML_PREFIX );
 
     public static final String SAML_ATTRIB_NAME_FORMAT_BASIC = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";
 
     public static final String TOPIC_DIALECT_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";
 
     public static final String WS_TRUST_REQUEST_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/";
 }
