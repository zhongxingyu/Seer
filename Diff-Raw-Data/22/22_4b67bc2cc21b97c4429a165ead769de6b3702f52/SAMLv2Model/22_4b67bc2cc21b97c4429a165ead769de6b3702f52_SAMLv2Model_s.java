 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SAMLv2Model.java,v 1.8 2007-11-14 21:17:32 asyhuang Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.federation.model;
 
 import com.sun.identity.console.base.model.AMModel;
 import com.sun.identity.console.base.model.AMConsoleException;
 import java.util.Map;
 import java.util.List;
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBException;
 
 public interface SAMLv2Model
     extends EntityModel {
     
     //SAMLv2 General attributes
     public static final String TF_NAME = "tfName";
     public static final String TF_KEY_NAME = "keySize";
     public static final String TF_ALGORITHM = "Algorithm";
     
     //SAMLv2 IDP Standard attributes
     public static final String WANT_AUTHN_REQ_SIGNED =
         "WantAuthnRequestsSigned";
     public static final String PROTOCOL_SUPP_ENUM =
         "protocolSupportEnumeration";
     public static final String ART_RES_LOCATION = "artLocation";
     public static final String ART_RES_INDEX = "index";
     public static final String ART_RES_ISDEFAULT = "isDefault";
     public static final String SINGLE_LOGOUT_HTTP_LOCATION =
         "slohttpLocation";
     public static final String SINGLE_LOGOUT_HTTP_RESP_LOCATION =
         "slohttpResponseLocation";
     public static final String SINGLE_LOGOUT_SOAP_LOCATION =
         "slosoapLocation";
     public static final String MANAGE_NAMEID_HTTP_LOCATION =
         "mnihttpLocation";
     public static final String MANAGE_NAMEID_HTTP_RESP_LOCATION =
         "mnihttpResponseLocation";
     public static final String MANAGE_NAMEID_SOAP_LOCATION =
         "mnisoapLocation";
     public static final String NAMEID_FORMAT = "nameidlist";
     public static final String SINGLE_SIGNON_HTTP_LOCATION =
         "ssohttpLocation";
     public static final String SINGLE_SIGNON_SOAP_LOCATION =
         "ssosoapLocation";
     
     //SAMLv2 SP Standard attributes
     public static final String IS_AUTHN_REQ_SIGNED = "AuthnRequestsSigned";
     public static final String WANT_ASSERTIONS_SIGNED =
         "WantAssertionsSigned";
     public static final String SP_PROTOCOL_SUPP_ENUM =
         "protocolSupportEnumeration";
     public static final String SP_SINGLE_LOGOUT_HTTP_LOCATION =
         "slohttpLocation";
     public static final String SP_SINGLE_LOGOUT_HTTP_RESP_LOCATION =
         "slohttpResponseLocation";
     public static final String SP_SINGLE_LOGOUT_SOAP_LOCATION =
         "slosoapLocation";
     public static final String SP_MANAGE_NAMEID_HTTP_LOCATION =
         "mnihttpLocation";
     public static final String SP_MANAGE_NAMEID_HTTP_RESP_LOCATION =
         "mnihttpResponseLocation";
     public static final String SP_MANAGE_NAMEID_SOAP_LOCATION =
         "mnisoapLocation";
     public static final String SP_MANAGE_NAMEID_SOAP_RESP_LOCATION =
         "mnisoapResponseLocation";
     public static final String HTTP_ARTI_ASSRT_CONS_SERVICE_DEFAULT =
         "isDefault";
     public static final String HTTP_ARTI_ASSRT_CONS_SERVICE_INDEX =
         "httpArtifactIndex";
     public static final String HTTP_ARTI_ASSRT_CONS_SERVICE_LOCATION =
         "httpArtifactLocation";
     public static final String HTTP_POST_ASSRT_CONS_SERVICE_INDEX =
         "httpPostIndex";
     public static final String HTTP_POST_ASSRT_CONS_SERVICE_LOCATION =
         "httpPostLocation";
     
     //SAML2 IDP Extended Attributes
     public static final String IDP_SIGN_CERT_ALIAS = "signingCertAlias";
     public static final String IDP_ENCRYPT_CERT_ALIAS =
         "encryptionCertAlias";
     public static final String IDP_BASIC_AUTH_ON = "basicAuthOn";
     public static final String IDP_BASIC_AUTH_USER = "basicAuthUser";
     public static final String IDP_BASIC_AUTH_PWD = "basicAuthPassword";
     public static final String IDP_AUTO_FED_ENABLED = "autofedEnabled";
     public static final String IDP_AUTO_FED_ATTR = "autofedAttribute";
     public static final String IDP_ATTR_MAP = "attributeMap";
     public static final String IDP_NAMEID_ENCRYPTED = "wantNameIDEncrypted";
     public static final String IDP_LOGOUT_REQ_SIGN =
         "wantLogoutRequestSigned";
     public static final String IDP_LOGOUT_RESP_SIGN =
         "wantLogoutResponseSigned";
     public static final String IDP_MNI_REQ_SIGN = "wantMNIRequestSigned";
     public static final String IDP_MNI_RESP_SIGN = "wantMNIResponseSigned";
     public static final String ASSERT_EFFECT_TIME = "assertionEffectiveTime";
     public static final String IDP_ACCT_MAPPER = "idpAccountMapper";
     public static final String IDP_AUTHN_CONT_MAPPER =
         "idpAuthncontextMapper";
     public static final String IDP_AUTHN_CONTCLASS_REF_MAPPING =
         "idpAuthncontextClassrefMapping";
     public static final String IDP_ATTR_MAPPER = "idpAttributeMapper";
     public static final String ASSERT_NOT_BEFORE_TIMESKEW =
         "assertionNotBeforeTimeSkew";
     public static final String BOOT_STRAP_ENABLED =
         "discoveryBootstrappingEnabled";
     public static final String ARTIF_RESOLVE_SIGN =
         "wantArtifactResolveSigned";
     
     //SAML2 SP Extended Attributes
     public static final String SP_SIGN_CERT_ALIAS = "signingCertAlias";
     public static final String SP_ENCRYPT_CERT_ALIAS = "encryptionCertAlias";
     public static final String SP_BASIC_AUTH_ON = "basicAuthOn";
     public static final String SP_BASIC_AUTH_USER = "basicAuthUser";
     public static final String SP_BASIC_AUTH_PWD = "basicAuthPassword";
     public static final String SP_AUTO_FED_ENABLED = "autofedEnabled";
     public static final String SP_AUTO_FED_ATTR = "autofedAttribute";
     public static final String SP_ATTR_MAP = "attributeMap";
     public static final String SP_NAMEID_ENCRYPTED = "wantNameIDEncrypted";
     public static final String SP_LOGOUT_REQ_SIGN =
         "wantLogoutRequestSigned";
     public static final String SP_LOGOUT_RESP_SIGN =
         "wantLogoutResponseSigned";
     public static final String SP_MNI_REQ_SIGN = "wantMNIRequestSigned";
     public static final String SP_MNI_RESP_SIGN = "wantMNIResponseSigned";
     public static final String TRANSIENT_USER = "transientUser";
     public static final String SP_ACCT_MAPPER = "spAccountMapper";
     public static final String SP_AUTHN_CONT_MAPPER = "spAuthncontextMapper";
     public static final String SP_ATTR_MAPPER = "spAttributeMapper";
     public static final String SP_AUTHN_CONTEXT_CLASS_REF_MAPPING =
         "spAuthncontextClassrefMapping";
     public static final String SP_AUTHN_CONTEXT_COMPARISON =
         "spAuthncontextComparisonType";
     public static final String SAML2_AUTH_MODULE = "saml2AuthModuleName";
     public static final String LOCAL_AUTH_URL = "localAuthURL";
     public static final String INTERMEDIATE_URL = "intermediateUrl";
     public static final String DEFAULT_RELAY_STATE = "defaultRelayState";
     public static final String ASSERT_TIME_SKEW = "assertionTimeSkew";
     public static final String WANT_ATTR_ENCRYPTED =
         "wantAttributeEncrypted";
     public static final String WANT_ASSERTION_ENCRYPTED =
         "wantAssertionEncrypted";
     public static final String WANT_ARTIF_RESP_SIGN =
         "wantArtifactResponseSigned";
     
     //IDP PROXY
     public static final String ENABLE_IDP_PROXY = "enableIDPProxy";
     public static final String IDP_PROXY_LIST = "idpProxyList";
     public static final String IDP_PROXY_COUNT = "idpProxyCount";
     public static final String IDP_PROXY_INTROD =
         "useIntroductionForIDPProxy";
     
     //ECP IDP
     public static final String ATTR_IDP_ECP_SESSION_MAPPER =
         "idpECPSessionMapper";
     
     //ECP SP
     public static final String ATTR_ECP_REQUEST_IDP_LIST_FINDER_IMPL =
         "ECPRequestIDPListFinderImpl";
     public static final String ATTR_ECP_REQUEST_IDP_LIST =
         "ECPRequestIDPList";
     public static final String ATTR_ECP_REQUEST_IDP_LIST_GET_COMPLETE =
         "ECPRequestIDPListGetComplete";
     
     // SAE IDP
     public static final String ATTR_SAE_IDP_APP_SECRET_LIST = "saeAppSecretList";
     public static final String ATTR_SAE_IDP_URL = "saeIDPUrl";
     //SAE SP
     public static final String ATTR_SAE_SP_APP_SECRET_LIST = "saeAppSecretList";
     public static final String ATTR_SAE_SP_URL =  "saeSPUrl";
     public static final String ATTR_SAE_LOGOUT_URL = "saeSPLogoutUrl";
     
     // XACML PDP/PEP
     public static final String ATTR_TXT_PROTOCOL_SUPPORT_ENUM =
         "txtProtocolSupportEnum";
     public static final String ATTR_XACML_AUTHZ_SERVICE_BINDING =
         "XACMLAuthzServiceBinding";
     public static final String ATTR_XACML_AUTHZ_SERVICE_LOCATION =
         "XACMLAuthzServiceLocation";
     public static final String ATTR_WANT_ASSERTION_SIGNED =
         "wantAssertionSigned";
     public static final String ATTR_SIGNING_CERT_ALIAS = "signingCertAlias";
     public static final String ATTR_ENCRYPTION_CERT_ALIAS =
         "encryptionCertAlias";
     public static final String ATTR_BASIC_AUTH_ON = "basicAuthOn";
     public static final String ATTR_BASIC_AUTH_USER = "basicAuthUser";
     public static final String ATTR_BASIC_AUTH_PASSWORD = "basicAuthPassword";
     public static final String ATTR_WANT_XACML_AUTHZ_DECISION_QUERY_SIGNED =
         "wantXACMLAuthzDecisionQuerySigned";
     public static final String ATTR_WANT_ASSERTION_ENCRYPTED =
         "wantAssertionEncrypted";
     public static final String ATTR_COTLIST = "cotlist";
     
     /**
      * Returns a map with standard identity provider attributes and values.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @return Map with standard attribute values of Identity Provider.
      * @throws AMConsoleException if unable to retrieve the Identity Provider
      *     attrubutes based on the realm and entityName passed.
      */
     public Map getStandardIdentityProviderAttributes(
         String realm,
         String entityName
         ) throws AMConsoleException;
     
     /**
      * Returns a map with extended identity provider attributes and values.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @return Map with extended attribute values of Identity Provider.
      * @throws AMConsoleException if unable to retrieve the Identity Provider
      *     attrubutes based on the realm and entityName passed.
      */
     public Map getExtendedIdentityProviderAttributes(
         String realm,
         String entityName
         ) throws AMConsoleException;
     
     /**
      * Returns a map with standard service provider attributes and values.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @return Map with standard attribute values of Service Provider.
      * @throws AMConsoleException if unable to retrieve the Service Provider
      *     attrubutes based on the realm and entityName passed.
      */
     public Map getStandardServiceProviderAttributes(
         String realm,
         String entityName
         ) throws AMConsoleException;
     
     /**
      * Returns a map with extended service provider attributes and values.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @return Map with extended attribute values of Service Provider.
      * @throws AMConsoleException if unable to retrieve the Service Provider
      *     attrubutes based on the realm and entityName passed.
      */
     public Map getExtendedServiceProviderAttributes(
         String realm,
         String entityName
         ) throws AMConsoleException;
     
     /**
      * Saves the standard attribute values for the Identiy Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param idpStdValues Map which contains the standard attribute values.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     void setIDPStdAttributeValues(
         String realm,
         String entityName,
         Map idpStdValues
         ) throws AMConsoleException;
     
     /**
      * Saves the extended attribute values for the Identity Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param idpExtValues Map which contains the standard attribute values.
      * @param location has the information whether remote or hosted.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     void setIDPExtAttributeValues(
         String realm,
         String entityName,
         Map idpExtValues,
         String location
         ) throws AMConsoleException;
     
     /**
      * Saves the standard attribute values for the Service Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param spStdValues Map which contains the standard attribute values.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     void setSPStdAttributeValues(
         String realm,
         String entityName,
         Map spStdValues
         ) throws AMConsoleException;
     
     /**
      * Saves the extended attribute values for the Service Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param spExtValues Map which contains the standard attribute values.
      * @param location has the information whether remote or hosted.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     void setSPExtAttributeValues(
         String realm,
         String entityName,
         Map spExtValues,
         String location
         ) throws AMConsoleException;
     
     /**
      * Returns SAMLv2 Extended Service Provider attribute values.
      *
      * @return SAMLv2 Extended Service Provider attribute values.
      */
     Map getSPEXDataMap();
     
     /**
      * Returns SAMLv2 Extended Identity Provider attribute values.
      *
      * @return SAMLv2 Extended Identity Provider attribute values.
      */
     Map getIDPEXDataMap();
     
     /**
      * Returns a Map of PEP descriptor data.(Standard Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @return key-value pair Map of PEP descriptor data.
      * @throws AMConsoleException if unable to retrieve the PEP
      *         standard metadata attribute
      */
     public Map getPEPDescriptor(String realm, String entityName)
     throws AMConsoleException;
     /**
      * Returns a Map of PDP descriptor data.(Standard Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @return key-value pair Map of PDP descriptor data.
      * @throws AMConsoleException if unable to retrieve the PDP
      *         standard metadata attribute
      */
     public Map getPDPDescriptor(String realm, String entityName)
     throws AMConsoleException;
     
     /**
      * Returns a Map of PEP config data.(Extended Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @param location entity is remote or hosted
      * @throws AMConsoleException if unable to retrieve the PEP
      *         extended metadata attribute
      */
     public Map getPEPConfig(
         String realm,
         String entityName,
         String location
         ) throws AMConsoleException;
     
     /**
      * Returns a Map of PDP Config data.(Extended Metadata)
      *
      * @param realm where entity exists.
      * @param entityName entity name of Entity Descriptor.
      * @param location entity is remote or hosted
      * @throws AMConsoleException if unable to retrieve the PEP
      *         extended metadata attribute
      */
     public Map getPDPConfig(
         String realm,
         String entityName,
         String location
         ) throws AMConsoleException;
     
     /**
      * save data for PDP descriptor data.(Standard Metadata)
      *
      * @param realm where entity exists.
      * @param entityName entity name of Entity Descriptor.
      * @param attrValues key-value pair Map of PDP standed data.
      * @throws AMConsoleException if there is an error
      */
     public void updatePDPDescriptor(
         String realm,
         String entityName,
         Map attrValues
         ) throws AMConsoleException;
     
     /**
      * save data for PDP Config data.(Extended Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @param location entity is remote or hosted
      * @param attrValues key-value pair Map of PDP extended config.
      */
     public void updatePDPConfig(
         String realm,
         String entityName,
         String location,
         Map attrValues
         ) throws AMConsoleException;
     
     /**
      * save data for PEP descriptor data.(Standard Metadata)
      *
      * @param realm realm of Entity
      * @param entityName name of Entity Descriptor.
      * @param attrValues key-value pair Map of PEP descriptor data.
      * @throws AMConsoleException if there is an error
      */
     public void updatePEPDescriptor(
         String realm,
         String entityName,
         Map attrValues
         ) throws AMConsoleException;
     
     
     /**
      * Save the configuration data for the policy enforcment point (PEP) entity.
      *
      * @param realm where entity exists.
      * @param entityName name of Entity Descriptor.
      * @param location entity is remote or hosted.
      * @param attrValues key-value pair Map of PEP extended config.
      * @throws AMConsoleException if there is an error
      */
     public void updatePEPConfig(
         String realm,
         String entityName,
         String location,
         Map attrValues
         ) throws AMConsoleException;
 }
