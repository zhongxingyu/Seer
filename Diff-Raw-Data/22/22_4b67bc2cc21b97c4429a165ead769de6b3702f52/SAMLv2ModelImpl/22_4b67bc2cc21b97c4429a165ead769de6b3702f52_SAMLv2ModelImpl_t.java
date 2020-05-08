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
 * $Id: SAMLv2ModelImpl.java,v 1.10 2007-11-21 02:27:39 asyhuang Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.federation.model;
 
 import com.sun.identity.console.base.model.AMAdminUtils;
 import com.sun.identity.console.base.model.AMModelBase;
 import com.sun.identity.console.base.model.AMConsoleException;
 import javax.servlet.http.HttpServletRequest;
 import com.sun.identity.saml2.jaxb.metadata.EntityDescriptorElement;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.jaxb.metadata.EntityDescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.entityconfig.IDPSSOConfigElement;
 import com.sun.identity.saml2.jaxb.entityconfig.AttributeElement;
 import com.sun.identity.saml2.jaxb.metadata.SingleSignOnServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.ArtifactResolutionServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.EndpointType;
 import com.sun.identity.saml2.jaxb.metadata.SingleLogoutServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.ManageNameIDServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.entityconfig.SPSSOConfigElement;
 import com.sun.identity.saml2.jaxb.metadata.SSODescriptorType;
 import com.sun.identity.saml2.jaxb.metadata.AssertionConsumerServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.XACMLAuthzDecisionQueryDescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.XACMLPDPDescriptorElement;
 import com.sun.identity.saml2.jaxb.entityconfig.XACMLAuthzDecisionQueryConfigElement;
 import com.sun.identity.saml2.jaxb.entityconfig.XACMLPDPConfigElement;
 import com.sun.identity.saml2.jaxb.entityconfig.AttributeType;
 import com.sun.identity.saml2.jaxb.entityconfig.BaseConfigType;
 import com.sun.identity.saml2.jaxb.entityconfig.EntityConfigElement;
 import com.sun.identity.saml2.jaxb.entityconfig.ObjectFactory;
 import com.sun.identity.saml2.jaxb.metadata.EntityDescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.XACMLAuthzServiceElement;
 import com.sun.identity.console.federation.model.EntityModel;
 import javax.xml.bind.JAXBException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class SAMLv2ModelImpl extends EntityModelImpl implements SAMLv2Model {    
     private SAML2MetaManager metaManager;
     private static Map extendedMetaIdpMap = new HashMap(42);
     private static Map extendedMetaSpMap = new HashMap(54);
     private static Map xacmlPDPExtendedMeta = new HashMap(18);
     private static Map xacmlPEPExtendedMeta = new HashMap(18);
 
     //extended metadata attributes for idp only
     static {
         extendedMetaIdpMap.put(IDP_SIGN_CERT_ALIAS, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_ENCRYPT_CERT_ALIAS, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_BASIC_AUTH_ON, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_BASIC_AUTH_USER, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_BASIC_AUTH_PWD, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_AUTO_FED_ENABLED, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_AUTO_FED_ATTR, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_ATTR_MAP, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_NAMEID_ENCRYPTED, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_LOGOUT_REQ_SIGN, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_LOGOUT_RESP_SIGN, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_MNI_REQ_SIGN, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_MNI_RESP_SIGN, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(ASSERT_EFFECT_TIME, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_ACCT_MAPPER, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_AUTHN_CONT_MAPPER, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_AUTHN_CONTCLASS_REF_MAPPING,
                 Collections.EMPTY_SET);
         extendedMetaIdpMap.put(IDP_ATTR_MAPPER, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(ASSERT_NOT_BEFORE_TIMESKEW,
                 Collections.EMPTY_SET);
         extendedMetaIdpMap.put(BOOT_STRAP_ENABLED, Collections.EMPTY_SET);
         extendedMetaIdpMap.put(ARTIF_RESOLVE_SIGN, Collections.EMPTY_SET);
         
         // ECP
         extendedMetaIdpMap.put(ATTR_IDP_ECP_SESSION_MAPPER, 
             Collections.EMPTY_SET);
         
         //SAE
         extendedMetaIdpMap.put(ATTR_SAE_IDP_APP_SECRET_LIST, 
             Collections.EMPTY_SET);
         extendedMetaIdpMap.put(ATTR_SAE_IDP_URL, 
             Collections.EMPTY_SET);                
     }
     
     //extended metadata attributes for sp only
     static {
         extendedMetaSpMap.put(SP_SIGN_CERT_ALIAS, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_ENCRYPT_CERT_ALIAS, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_BASIC_AUTH_ON, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_BASIC_AUTH_USER, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_BASIC_AUTH_PWD, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_AUTO_FED_ENABLED, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_AUTO_FED_ATTR, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_ATTR_MAP, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_NAMEID_ENCRYPTED, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_LOGOUT_REQ_SIGN, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_LOGOUT_RESP_SIGN, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_MNI_REQ_SIGN, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_MNI_RESP_SIGN, Collections.EMPTY_SET);
         extendedMetaSpMap.put(TRANSIENT_USER, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_ACCT_MAPPER, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_AUTHN_CONT_MAPPER, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_ATTR_MAPPER, Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_AUTHN_CONTEXT_CLASS_REF_MAPPING,
                 Collections.EMPTY_SET);
         extendedMetaSpMap.put(SP_AUTHN_CONTEXT_COMPARISON,
                 Collections.EMPTY_SET);
         extendedMetaSpMap.put(SAML2_AUTH_MODULE, Collections.EMPTY_SET);
         extendedMetaSpMap.put(LOCAL_AUTH_URL, Collections.EMPTY_SET);
         extendedMetaSpMap.put(INTERMEDIATE_URL, Collections.EMPTY_SET);
         extendedMetaSpMap.put(DEFAULT_RELAY_STATE, Collections.EMPTY_SET);
         extendedMetaSpMap.put(ASSERT_TIME_SKEW, Collections.EMPTY_SET);
         extendedMetaSpMap.put(WANT_ATTR_ENCRYPTED, Collections.EMPTY_SET);
         extendedMetaSpMap.put(WANT_ASSERTION_ENCRYPTED, 
                 Collections.EMPTY_SET);
         extendedMetaSpMap.put(WANT_ARTIF_RESP_SIGN, Collections.EMPTY_SET);
         
         //IDP PROXY
         extendedMetaSpMap.put(ENABLE_IDP_PROXY, Collections.EMPTY_SET);
         extendedMetaSpMap.put(IDP_PROXY_LIST, Collections.EMPTY_SET);
         extendedMetaSpMap.put(IDP_PROXY_COUNT, Collections.EMPTY_SET);
         extendedMetaSpMap.put(IDP_PROXY_INTROD, Collections.EMPTY_SET);
         
         //ECP
         extendedMetaSpMap.put(ATTR_ECP_REQUEST_IDP_LIST_FINDER_IMPL, 
             Collections.EMPTY_SET);
         extendedMetaSpMap.put(ATTR_ECP_REQUEST_IDP_LIST, 
             Collections.EMPTY_SET);
         extendedMetaSpMap.put(ATTR_ECP_REQUEST_IDP_LIST_GET_COMPLETE, 
             Collections.EMPTY_SET);
         
         //SAE 
         extendedMetaSpMap.put(ATTR_SAE_SP_APP_SECRET_LIST, Collections.EMPTY_SET); 
         extendedMetaSpMap.put(ATTR_SAE_SP_URL, Collections.EMPTY_SET);        
         extendedMetaSpMap.put(ATTR_SAE_LOGOUT_URL, Collections.EMPTY_SET); 

        //spAdapter
        extendedMetaSpMap.put(ATTR_SP_ADAPTER, Collections.EMPTY_SET);
        extendedMetaSpMap.put(ATTR_SP_ADAPTER_ENV, Collections.EMPTY_SET);
     }
 
     static {
         xacmlPDPExtendedMeta.put(ATTR_WANT_ASSERTION_SIGNED,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_SIGNING_CERT_ALIAS,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_ENCRYPTION_CERT_ALIAS,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_BASIC_AUTH_ON,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_BASIC_AUTH_USER,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_BASIC_AUTH_PASSWORD,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_WANT_XACML_AUTHZ_DECISION_QUERY_SIGNED,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_WANT_ASSERTION_ENCRYPTED,
             Collections.EMPTY_LIST);
         xacmlPDPExtendedMeta.put(ATTR_COTLIST,
             Collections.EMPTY_LIST);
     }
     static {
         xacmlPEPExtendedMeta.put(ATTR_WANT_ASSERTION_SIGNED,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_SIGNING_CERT_ALIAS,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_ENCRYPTION_CERT_ALIAS,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_BASIC_AUTH_ON,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_BASIC_AUTH_USER,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_BASIC_AUTH_PASSWORD,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_WANT_XACML_AUTHZ_DECISION_QUERY_SIGNED,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_WANT_ASSERTION_ENCRYPTED,
             Collections.EMPTY_LIST);
         xacmlPEPExtendedMeta.put(ATTR_COTLIST,
             Collections.EMPTY_LIST);
     }
     
     public SAMLv2ModelImpl(HttpServletRequest req, Map map) {
         super(req, map);
     }
     
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
     ) throws AMConsoleException {
         String[] params = {realm, entityName,"SAMLv2", "IDP-Standard"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         Map map = new HashMap();
         IDPSSODescriptorElement idpssoDescriptor = null;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             idpssoDescriptor = 
                     samlManager.getIDPSSODescriptor(realm,entityName);
             if (idpssoDescriptor != null) {
                 
                 // retrieve WantAuthnRequestsSigned
                 map.put(WANT_AUTHN_REQ_SIGNED,returnEmptySetIfValueIsNull(
                         idpssoDescriptor.isWantAuthnRequestsSigned()));
                 
                 //retrieve ArtifactResolutionService
                 List artList = 
                         idpssoDescriptor.getArtifactResolutionService();
                 if (!artList.isEmpty()) {
                     ArtifactResolutionServiceElement key =
                             (ArtifactResolutionServiceElement)artList.get(0);
                     map.put(ART_RES_LOCATION,
                             returnEmptySetIfValueIsNull(key.getLocation()));
                     map.put(ART_RES_INDEX,
                             returnEmptySetIfValueIsNull(Integer.toString(
                             key.getIndex())));
                     map.put(ART_RES_ISDEFAULT,
                             returnEmptySetIfValueIsNull(key.isIsDefault()));
                 }
                 
                 //retrieve SingleLogoutService
                 List logoutList = idpssoDescriptor.getSingleLogoutService();
                 if (!logoutList.isEmpty()) {
                     SingleLogoutServiceElement slsElem1 =
                         (SingleLogoutServiceElement)logoutList.get(0);
                     map.put(SINGLE_LOGOUT_HTTP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             slsElem1.getLocation()));
                     map.put(SINGLE_LOGOUT_HTTP_RESP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             slsElem1.getResponseLocation()));
                     SingleLogoutServiceElement slsElem2 =
                         (SingleLogoutServiceElement)logoutList.get(1);
                     map.put(SINGLE_LOGOUT_SOAP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             slsElem2.getLocation()));
                 }
                 
                 //retrieve ManageNameIDService
                 List manageNameIdList = 
                         idpssoDescriptor.getManageNameIDService();
                 if (!manageNameIdList.isEmpty()) {
                     ManageNameIDServiceElement mniElem1 =
                         (ManageNameIDServiceElement)manageNameIdList.get(0);
                     map.put(MANAGE_NAMEID_HTTP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             mniElem1.getLocation()));
                     map.put(MANAGE_NAMEID_HTTP_RESP_LOCATION,
                         returnEmptySetIfValueIsNull(
                             mniElem1.getResponseLocation()));
                     ManageNameIDServiceElement mniElem2 =
                         (ManageNameIDServiceElement)manageNameIdList.get(1);
                     map.put(MANAGE_NAMEID_SOAP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             mniElem2.getLocation()));
                 }
                 
                 //retrieve nameid format
                 List NameIdFormatList = idpssoDescriptor.getNameIDFormat();
                 if (!NameIdFormatList.isEmpty()) {
                     map.put(NAMEID_FORMAT, returnEmptySetIfValueIsNull(
                             convertListToSet(NameIdFormatList)));
                 }
                 
                 //retrieve SingleSignOnService
                 List signonList = idpssoDescriptor.getSingleSignOnService();
                 if (!signonList.isEmpty()) {
                     SingleSignOnServiceElement signElem1 =
                             (SingleSignOnServiceElement)signonList.get(0);
                     map.put(SINGLE_SIGNON_HTTP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             signElem1.getLocation()));
                     SingleSignOnServiceElement signElem2 =
                             (SingleSignOnServiceElement)signonList.get(1);
                     map.put(SINGLE_SIGNON_SOAP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             signElem2.getLocation()));
                 }
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             debug.warning
                 ("SAMLv2ModelImpl.getIdentityProviderAttributes:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "IDP-Standard", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES",
                     paramsEx);
             throw new AMConsoleException(strError);
         }
         return map;       
     }
     
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
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "IDP-Extended"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         Map map = null;
         IDPSSOConfigElement idpssoConfig = null;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             idpssoConfig = samlManager.getIDPSSOConfig(realm,entityName);
             if (idpssoConfig != null) {
                 BaseConfigType baseConfig = (BaseConfigType)idpssoConfig;
                 map = SAML2MetaUtils.getAttributes(baseConfig);
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             debug.warning
                 ("SAMLv2ModelImpl.getExtIdentityProviderAttributes:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "IDP-Extended", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES",
                     paramsEx);
             throw new AMConsoleException(strError);
         }
         return (map != null) ? map : Collections.EMPTY_MAP;
     }
     
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
     ) throws AMConsoleException {
         String[] params = {realm, entityName,"SAMLv2", "SP-Standard"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         Map map = new HashMap();
         SPSSODescriptorElement spssoDescriptor = null;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             spssoDescriptor = samlManager.getSPSSODescriptor(realm,entityName);
             if (spssoDescriptor != null) {
                 
                 // retrieve WantAuthnRequestsSigned
                 map.put(IS_AUTHN_REQ_SIGNED,
                         returnEmptySetIfValueIsNull(
                         spssoDescriptor.isAuthnRequestsSigned()));
                 map.put(WANT_ASSERTIONS_SIGNED,
                         returnEmptySetIfValueIsNull(
                         spssoDescriptor.isWantAssertionsSigned()));
                 
                 //retrieve SingleLogoutService
                 List splogoutList = spssoDescriptor.getSingleLogoutService();
                 if (!splogoutList.isEmpty()) {
                     SingleLogoutServiceElement spslsElem1 =
                         (SingleLogoutServiceElement)splogoutList.get(0);
                     map.put(SP_SINGLE_LOGOUT_HTTP_LOCATION,
                         returnEmptySetIfValueIsNull(
                             spslsElem1.getLocation()));
                     map.put(SP_SINGLE_LOGOUT_HTTP_RESP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             spslsElem1.getResponseLocation()));
                     SingleLogoutServiceElement spslsElem2 =
                         (SingleLogoutServiceElement)splogoutList.get(1);
                     map.put(SP_SINGLE_LOGOUT_SOAP_LOCATION,
                         returnEmptySetIfValueIsNull(spslsElem2.getLocation()));
                 }
                 
                 //retrieve ManageNameIDService
                 List manageNameIdList = 
                         spssoDescriptor.getManageNameIDService();
                 if (!manageNameIdList.isEmpty()) {
                     ManageNameIDServiceElement mniElem1 =
                         (ManageNameIDServiceElement)manageNameIdList.get(0);
                     map.put(SP_MANAGE_NAMEID_HTTP_LOCATION,
                         returnEmptySetIfValueIsNull(mniElem1.getLocation()));
                     map.put(SP_MANAGE_NAMEID_HTTP_RESP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             mniElem1.getResponseLocation()));
                     ManageNameIDServiceElement mniElem2 =
                         (ManageNameIDServiceElement)manageNameIdList.get(1);
                     map.put(SP_MANAGE_NAMEID_SOAP_LOCATION,
                         returnEmptySetIfValueIsNull(mniElem2.getLocation()));
                     map.put(SP_MANAGE_NAMEID_SOAP_RESP_LOCATION,
                             returnEmptySetIfValueIsNull(
                             mniElem2.getResponseLocation()));
                 }
                 
                 //retrieve AssertionConsumerService
                 List asconsServiceList = 
                         spssoDescriptor.getAssertionConsumerService();
                 if (!asconsServiceList.isEmpty()) {
                     AssertionConsumerServiceElement acsElem1 =
                             (AssertionConsumerServiceElement)
                             asconsServiceList.get(0);
                     map.put(HTTP_ARTI_ASSRT_CONS_SERVICE_DEFAULT,
                         returnEmptySetIfValueIsNull(acsElem1.isIsDefault()));
                     map.put(HTTP_ARTI_ASSRT_CONS_SERVICE_INDEX,
                             returnEmptySetIfValueIsNull(
                             Integer.toString(acsElem1.getIndex())));
                     map.put(HTTP_ARTI_ASSRT_CONS_SERVICE_LOCATION,
                         returnEmptySetIfValueIsNull(acsElem1.getLocation()));
                     AssertionConsumerServiceElement acsElem2 =
                             (AssertionConsumerServiceElement)
                             asconsServiceList.get(1);
                     map.put(HTTP_POST_ASSRT_CONS_SERVICE_INDEX,
                             returnEmptySetIfValueIsNull(
                             Integer.toString(acsElem2.getIndex())));
                     map.put(HTTP_POST_ASSRT_CONS_SERVICE_LOCATION,
                         returnEmptySetIfValueIsNull(acsElem2.getLocation()));
                 }
                 //retrieve nameid format
                 List NameIdFormatList = spssoDescriptor.getNameIDFormat();
                 if (!NameIdFormatList.isEmpty()) {
                     map.put(NAMEID_FORMAT, returnEmptySetIfValueIsNull(
                             convertListToSet(NameIdFormatList)));
                 }
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             debug.warning
                 ("SAMLv2ModelImpl.getStandardServiceProviderAttribute:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "SP-Standard", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES",
                     paramsEx);
             throw new AMConsoleException(strError);
         }
         return map;
     }
     
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
     ) throws AMConsoleException {
         String[] params = {realm, entityName,"SAMLv2", "SP-Extended"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
        Map map = null;
         SPSSOConfigElement spssoConfig = null;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             spssoConfig = samlManager.getSPSSOConfig(realm,entityName);
             if (spssoConfig != null) {
                 BaseConfigType baseConfig = (BaseConfigType)spssoConfig;
                 map = SAML2MetaUtils.getAttributes(baseConfig);
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             debug.warning(
                 "SAMLv2ModelImpl.getExtendedServiceProviderAttributes:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "SP-Extended", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES",
                     paramsEx);
             throw new AMConsoleException(strError);
         }
         return (map != null) ? map : Collections.EMPTY_MAP;
     }
     
     /**
      * Saves the standard attribute values for the Identiy Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param idpStdValues Map which contains the standard attribute values.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     public void setIDPStdAttributeValues(
         String realm,
         String entityName,
         Map idpStdValues 
     )  throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "IDP-Standard"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
         IDPSSODescriptorElement idpssoDescriptor = null;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             EntityDescriptorElement entityDescriptor =
                     samlManager.getEntityDescriptor(realm,entityName);
             idpssoDescriptor = 
                     samlManager.getIDPSSODescriptor(realm,entityName);
             if (idpssoDescriptor != null) {
                 boolean value = setToBoolean(
                         idpStdValues, WANT_AUTHN_REQ_SIGNED);
                 idpssoDescriptor.setWantAuthnRequestsSigned(value);
                 
                 // save for Artifact Resolution Service
                 String artLocation = getResult(
                         idpStdValues, ART_RES_LOCATION);
                 String indexValue = getResult(idpStdValues, ART_RES_INDEX);
                 boolean isDefault = 
                         setToBoolean(idpStdValues, ART_RES_ISDEFAULT);
                 List artList = 
                         idpssoDescriptor.getArtifactResolutionService();
                 if (!artList.isEmpty()) {
                     ArtifactResolutionServiceElement elem =
                             (ArtifactResolutionServiceElement)artList.get(0);
                     elem.setLocation(artLocation);
                     elem.setIndex(Integer.parseInt(indexValue));
                     elem.setIsDefault(isDefault);
                     idpssoDescriptor.getArtifactResolutionService().clear();
                     idpssoDescriptor.getArtifactResolutionService().add(elem);
                 }
                 
                 // save for Single Logout Service - Http-Redirect
                 String lohttpLocation = getResult(
                         idpStdValues, SINGLE_LOGOUT_HTTP_LOCATION);
                 String lohttpRespLocation = getResult(
                         idpStdValues, SINGLE_LOGOUT_HTTP_RESP_LOCATION);
                 String losoapLocation = getResult(
                         idpStdValues, SINGLE_LOGOUT_SOAP_LOCATION);
                 List logList = idpssoDescriptor.getSingleLogoutService();
                 if (!logList.isEmpty()) {
                     SingleLogoutServiceElement slsElem1 =
                             (SingleLogoutServiceElement)logList.get(0);
                     SingleLogoutServiceElement slsElem2 =
                             (SingleLogoutServiceElement)logList.get(1);
                     slsElem1.setLocation(lohttpLocation);
                     slsElem1.setResponseLocation(lohttpRespLocation);
                     slsElem2.setLocation(losoapLocation);
                     idpssoDescriptor.getSingleLogoutService().clear();
                     idpssoDescriptor.getSingleLogoutService().add(slsElem1);
                     idpssoDescriptor.getSingleLogoutService().add(slsElem2);
                 }
                 
                 // save for Manage Name ID Service
                 String mnihttpLocation = getResult(
                         idpStdValues, MANAGE_NAMEID_HTTP_LOCATION);
                 String mnihttpRespLocation = getResult(
                         idpStdValues, MANAGE_NAMEID_HTTP_RESP_LOCATION);
                 String mnisoapLocation = getResult(
                         idpStdValues, MANAGE_NAMEID_SOAP_LOCATION);
                 List manageNameIdList = 
                         idpssoDescriptor.getManageNameIDService();
                 if (!manageNameIdList.isEmpty()) {
                     ManageNameIDServiceElement mniElem1 =
                         (ManageNameIDServiceElement)manageNameIdList.get(0);
                     ManageNameIDServiceElement mniElem2 =
                         (ManageNameIDServiceElement)manageNameIdList.get(1);
                     mniElem1.setLocation(mnihttpLocation);
                     mniElem1.setResponseLocation(mnihttpRespLocation);
                     mniElem2.setLocation(mnisoapLocation);
                     idpssoDescriptor.getManageNameIDService().clear();
                     idpssoDescriptor.getManageNameIDService().add(mniElem1);
                     idpssoDescriptor.getManageNameIDService().add(mniElem2);
                 }
                 
                 //save nameid format
                 List NameIdFormatList = idpssoDescriptor.getNameIDFormat();
                 if (!NameIdFormatList.isEmpty()) {
                     saveNameIdFormat(idpssoDescriptor, idpStdValues);
                 }
                 
                 //save for SingleSignOnService
                 String ssohttpLocation = getResult(
                         idpStdValues, SINGLE_SIGNON_HTTP_LOCATION);
                 String ssopostLocation = getResult(
                         idpStdValues, SINGLE_SIGNON_SOAP_LOCATION);
                 List signonList = idpssoDescriptor.getSingleSignOnService();
                 if (!signonList.isEmpty()) {
                     SingleSignOnServiceElement signElem1 =
                             (SingleSignOnServiceElement)signonList.get(0);
                     SingleSignOnServiceElement signElem2 =
                             (SingleSignOnServiceElement)signonList.get(1);
                     signElem1.setLocation(ssohttpLocation);
                     signElem2.setLocation(ssopostLocation);
                     idpssoDescriptor.getSingleSignOnService().clear();
                     idpssoDescriptor.getSingleSignOnService().add(signElem1);
                     idpssoDescriptor.getSingleSignOnService().add(signElem2);
                 }
                 samlManager.setEntityDescriptor(realm, entityDescriptor);
             }
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (SAML2MetaException e) {
             debug.warning
                     ("SAMLv2ModelImpl.setIDPStdAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "IDP-Standard", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
             throw new AMConsoleException(strError);
         }
     }
     
     /**
      * Saves the extended attribute values for the Identiy Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param idpExtValues Map which contains the standard attribute values.
      * @param location has the information whether remote or hosted.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     public void setIDPExtAttributeValues(
         String realm,
         String entityName,
         Map idpExtValues,
         String location
     ) throws AMConsoleException {
          String[] params = {realm, entityName, "SAMLv2", "IDP-Extended"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
         String role = EntityModel.IDENTITY_PROVIDER;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             
             //entityConfig is the extended entity configuration object
             EntityConfigElement entityConfig =
                     samlManager.getEntityConfig(realm,entityName);
             
             //for remote cases
             if (entityConfig == null) {
                 createExtendedObject(realm, entityName, location, role);
                 entityConfig =
                         samlManager.getEntityConfig(realm,entityName);
             }
             IDPSSOConfigElement  idpssoConfig = 
                     samlManager.getIDPSSOConfig(realm,entityName);
             if (idpssoConfig != null) {
                 updateBaseConfig(idpssoConfig, idpExtValues);
             }
             
             //saves the attributes by passing the new entityConfig object
             samlManager.setEntityConfig(realm,entityConfig);
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (SAML2MetaException e) {
             debug.error("SAMLv2ModelImpl.setIDPExtAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "IDP-Extended", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
         } catch (JAXBException e) {
             debug.error("SAMLv2ModelImpl.setIDPExtAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "IDP-Extended", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
         } catch (AMConsoleException e) {
             debug.error("SAMLv2ModelImpl.setIDPExtAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "IDP-Extended", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
         }
     }
     
     /**
      * Saves the standard attribute values for the Service Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param spStdValues Map which contains the standard attribute values.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     public void setSPStdAttributeValues(
         String realm,
         String entityName,
         Map spStdValues 
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "SP-Standard"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
         SPSSODescriptorElement spssoDescriptor = null;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             EntityDescriptorElement entityDescriptor =
                     samlManager.getEntityDescriptor(realm,entityName);
             spssoDescriptor = 
                     samlManager.getSPSSODescriptor(realm,entityName);
             if (spssoDescriptor != null) {
                 
                 // save for Single Logout Service - Http-Redirect
                 String lohttpLocation = getResult(
                         spStdValues, SP_SINGLE_LOGOUT_HTTP_LOCATION);
                 String lohttpRespLocation = getResult(
                         spStdValues, SP_SINGLE_LOGOUT_HTTP_RESP_LOCATION);
                 String losoapLocation = getResult(
                         spStdValues, SP_SINGLE_LOGOUT_SOAP_LOCATION);
                 List logList = spssoDescriptor.getSingleLogoutService();
                 if (!logList.isEmpty()) {
                     SingleLogoutServiceElement slsElem1 =
                             (SingleLogoutServiceElement)logList.get(0);
                     SingleLogoutServiceElement slsElem2 =
                             (SingleLogoutServiceElement)logList.get(1);
                     slsElem1.setLocation(lohttpLocation);
                     slsElem1.setResponseLocation(lohttpRespLocation);
                     slsElem2.setLocation(losoapLocation);
                     spssoDescriptor.getSingleLogoutService().clear();
                     spssoDescriptor.getSingleLogoutService().add(slsElem1);
                     spssoDescriptor.getSingleLogoutService().add(slsElem2);
                 }
                 // save for Manage Name ID Service
                 String mnihttpLocation = getResult(
                         spStdValues, SP_MANAGE_NAMEID_HTTP_LOCATION);
                 String mnihttpRespLocation = getResult(
                         spStdValues, SP_MANAGE_NAMEID_HTTP_RESP_LOCATION);
                 String mnisoapLocation = getResult(
                         spStdValues, SP_MANAGE_NAMEID_SOAP_LOCATION);
                 String mnisoapResLocation = getResult(
                         spStdValues, SP_MANAGE_NAMEID_SOAP_RESP_LOCATION);
                 List manageNameIdList = 
                         spssoDescriptor.getManageNameIDService();
                 if (!manageNameIdList.isEmpty()) {
                     ManageNameIDServiceElement mniElem1 =
                         (ManageNameIDServiceElement)manageNameIdList.get(0);
                     ManageNameIDServiceElement mniElem2 =
                         (ManageNameIDServiceElement)manageNameIdList.get(1);
                     mniElem1.setLocation(mnihttpLocation);
                     mniElem1.setResponseLocation(mnihttpRespLocation);
                     mniElem2.setLocation(mnisoapLocation);
                     mniElem2.setResponseLocation(mnisoapResLocation);
                     spssoDescriptor.getManageNameIDService().clear();
                     spssoDescriptor.getManageNameIDService().add(mniElem1);
                     spssoDescriptor.getManageNameIDService().add(mniElem2);
                 }
                 //save for Assertion Consumer Service
                 boolean isassertDefault = setToBoolean(
                         spStdValues, HTTP_ARTI_ASSRT_CONS_SERVICE_DEFAULT);
                 String httpIndex = getResult(
                         spStdValues, HTTP_ARTI_ASSRT_CONS_SERVICE_INDEX);
                 String httpLocation = getResult(
                         spStdValues, HTTP_ARTI_ASSRT_CONS_SERVICE_LOCATION);
                 String postIndex =  getResult(
                         spStdValues, HTTP_POST_ASSRT_CONS_SERVICE_INDEX);
                 String postLocation = getResult(
                         spStdValues, HTTP_POST_ASSRT_CONS_SERVICE_LOCATION);
                 List asconsServiceList =
                         spssoDescriptor.getAssertionConsumerService();
                 if (!asconsServiceList.isEmpty()) {
                     AssertionConsumerServiceElement acsElem1 =
                         (AssertionConsumerServiceElement)
                         asconsServiceList.get(0);
                     AssertionConsumerServiceElement acsElem2 =
                         (AssertionConsumerServiceElement)
                         asconsServiceList.get(1);
                     acsElem1.setIsDefault(isassertDefault);
                     acsElem1.setIndex(Integer.parseInt(httpIndex));
                     acsElem1.setLocation(httpLocation);
                     acsElem2.setIndex(Integer.parseInt(postIndex));
                     acsElem2.setLocation(postLocation);
                 }
                 //save nameid format
                 List NameIdFormatList = spssoDescriptor.getNameIDFormat();
                 if (!NameIdFormatList.isEmpty()) {
                     saveNameIdFormat(spssoDescriptor, spStdValues);
                 }
                 
                 //save AuthenRequestsSigned
                 boolean authnValue = setToBoolean(
                         spStdValues, IS_AUTHN_REQ_SIGNED);
                 spssoDescriptor.setAuthnRequestsSigned(authnValue);
                 
                 //save WantAssertionsSigned
                 boolean assertValue = setToBoolean(
                         spStdValues, WANT_ASSERTIONS_SIGNED);
                 spssoDescriptor.setWantAssertionsSigned(assertValue);
                 samlManager.setEntityDescriptor(realm, entityDescriptor);
             }
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (SAML2MetaException e) {
             debug.warning
                     ("SAMLv2ModelImpl.setSPStdAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "SP-Standard", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
             throw new AMConsoleException(strError);
         }
     }
     
     /**
      * Saves the extended attribute values for the Service Provider.
      *
      * @param realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param spExtValues Map which contains the standard attribute values.
      * @param location has the information whether remote or hosted.
      * @throws AMConsoleException if saving of attribute value fails.
      */
     public void setSPExtAttributeValues(
         String realm,
         String entityName,
         Map spExtValues,
         String location
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "SP-Extended"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
         String role = EntityModel.SERVICE_PROVIDER;
         try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             
             //entityConfig is the extended entity configuration object
             EntityConfigElement entityConfig =
                     samlManager.getEntityConfig(realm,entityName);
             
             //for remote cases
             if (entityConfig == null) {
                 createExtendedObject(realm, entityName, location, role);
                 entityConfig =
                         samlManager.getEntityConfig(realm,entityName);
             }
             SPSSOConfigElement  spssoConfig = samlManager.getSPSSOConfig(
                     realm,entityName);
             if (spssoConfig != null){
                 updateBaseConfig(spssoConfig, spExtValues);
             }
             
             //saves the attributes by passing the new entityConfig object
             samlManager.setEntityConfig(realm,entityConfig);
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (SAML2MetaException e) {
             debug.error("SAMLv2ModelImpl.setSPExtAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx =
                 {realm, entityName, "SAMLv2", "SP Ext", strError}; 
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
         } catch (JAXBException e) {
             debug.error("SAMLv2ModelImpl.setSPExtAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx =
                 {realm, entityName, "SAMLv2", "SP Ext", strError}; 
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
         } catch (AMConsoleException e) {
             debug.error("SAMLv2ModelImpl.setSPExtAttributeValues:", e);
             String strError = getErrorString(e);
             String[] paramsEx =
                 {realm, entityName, "SAMLv2", "SP Ext", strError}; 
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR",
                     paramsEx);
         }
     }
         
     /**
      * Updates the BaseConfigElement.
      *
      * @param baseConfig is the BaseConfigType passed.
      * @param values the Map which contains the new attribute/value pairs.
      * @throws AMConsoleException if update of baseConfig object fails.
      */
     private void updateBaseConfig(
         BaseConfigType baseConfig,
         Map values
         ) throws AMConsoleException {
             List attrList = baseConfig.getAttribute();
             for (Iterator i = attrList.iterator(); i.hasNext(); ) {
                 AttributeElement avpnew = (AttributeElement)i.next();
                 String name = avpnew.getName();
                 Set set = (Set)values.get(name);
                 if (set != null) {
                    avpnew.getValue().clear();
                    avpnew.getValue().addAll(set);
                 }
             } 
     }
     
 
     /**
      * Saves the NameIdFormat.
      *
      * @param ssodescriptor is the SSODescriptorType which can be idpsso/spsso.
      * @param values the Map which contains the new attribute/value pairs.
      * @throws AMConsoleException if save fails.
      */
     private void saveNameIdFormat(
             SSODescriptorType ssodescriptor,
             Map values
     ) throws AMConsoleException {
         List listtoSave = convertSetToList(
                 (Set)values.get(NAMEID_FORMAT));
         ssodescriptor.getNameIDFormat().clear();
         Iterator itt = listtoSave.listIterator();
         while (itt.hasNext()) {
             String name =(String) itt.next();
             ssodescriptor.getNameIDFormat().add(name);
         }
     }
     
     /**
      * Creates the extended config object when it does not exist.
      * @param realm the realm to which the entity belongs.
      * @param entityName is the entity id.
      * @param location indicates whether hosted or remote
      * @param role can be SP, IDP or SP/IDP.
      * @throws SAML2MetaException, JAXBException,
      *     AMConsoleException if saving of attribute value fails.
      */
     private void createExtendedObject(
             String realm,
             String entityName,
             String location,
             String role
     ) throws SAML2MetaException, JAXBException, AMConsoleException {
         SAML2MetaManager samlManager = new SAML2MetaManager();
         EntityDescriptorElement entityDescriptor =
                 samlManager.getEntityDescriptor(realm, entityName);
         ObjectFactory objFactory = new ObjectFactory();
         EntityConfigElement entityConfigElement =
                 objFactory.createEntityConfigElement();
         entityConfigElement.setEntityID(entityName);
         if (location.equals("remote")) {
             entityConfigElement.setHosted(false);
         } else {
             entityConfigElement.setHosted(true);
         }
         List configList = 
             entityConfigElement.
                 getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig();
         BaseConfigType baseConfigIDP = null;
         BaseConfigType baseConfigSP = null;
         if (isDualRole(entityDescriptor)) {
             baseConfigIDP = objFactory.createIDPSSOConfigElement();
             baseConfigSP = objFactory.createSPSSOConfigElement();
             configList.add(baseConfigIDP);
             configList.add(baseConfigSP);
         }else if (role.equals("IDP")) {
             baseConfigIDP = objFactory.createIDPSSOConfigElement();
             configList.add(baseConfigIDP);
         } else if (role.equals("SP")) {
             baseConfigSP = objFactory.createSPSSOConfigElement();
             configList.add(baseConfigSP);
         }
         samlManager.setEntityConfig(realm, entityConfigElement);
     }
     
     /**
      * Retrieves information whether entity has dual role or not.
      * @param entityDescriptor is the standard metadata object.
      *
      * @return a boolean value which indicates entity has dual role or not.
      */
     private boolean isDualRole(EntityDescriptorElement entityDescriptor) {
         List roles = new ArrayList();
         boolean dual = false;
         if (entityDescriptor != null) {
             if ( (SAML2MetaUtils.getSPSSODescriptor(
                     entityDescriptor) != null) && (
                     SAML2MetaUtils.getIDPSSODescriptor(
                     entityDescriptor) != null) ) {
                 dual = true;
             }
         }
         return dual;
     }
     
     /**
      * Returns a Map of PEP descriptor data.(Standard Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @return key-value pair Map of PEP descriptor data.
      * @throws AMConsoleException if unable to retrieve the PEP 
      *         standard metadata attributes      
      */
     public Map getPEPDescriptor(
         String realm,
         String entityName
     ) throws AMConsoleException {
         String[] params = {realm, entityName,"SAMLv2", "XACML PEP"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         
         Map data = null;
         try {
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             XACMLAuthzDecisionQueryDescriptorElement xacmlAuthzDescriptor =
                     saml2Manager.getPolicyEnforcementPointDescriptor(
                     realm, entityName);            
             if (xacmlAuthzDescriptor != null) {
                 data = new HashMap(10);
                 
                 //ProtocolSupportEnum
                 data.put(ATTR_TXT_PROTOCOL_SUPPORT_ENUM,
                         returnEmptySetIfValueIsNull(
                         xacmlAuthzDescriptor.getProtocolSupportEnumeration()));                
                 if (xacmlAuthzDescriptor.isWantAssertionsSigned()) {
                     data.put(ATTR_WANT_ASSERTION_SIGNED, "true");
                 } else {
                     data.put(ATTR_WANT_ASSERTION_SIGNED, "false");
                 }
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PEP", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", paramsEx);
             throw new AMConsoleException(strError);
         } 
         return (data != null) ? data : Collections.EMPTY_MAP;
     }
     
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
         throws AMConsoleException 
     {
         String[] params = {realm, entityName,"SAMLv2", "XACML PDP"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
          
         Map data = null;
         try {
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             XACMLPDPDescriptorElement xacmlPDPDescriptor =
                     saml2Manager.getPolicyDecisionPointDescriptor(
                     realm,
                     entityName);            
             if (xacmlPDPDescriptor != null) {
                 data = new HashMap(10);
                 
                 //ProtocolSupportEnum
                 data.put(ATTR_TXT_PROTOCOL_SUPPORT_ENUM,
                     returnEmptySetIfValueIsNull(
 		    xacmlPDPDescriptor.getProtocolSupportEnumeration()));                
                 List authzServiceList = 
                     xacmlPDPDescriptor.getXACMLAuthzService();
                 if (authzServiceList.size() != 0) {
                     XACMLAuthzServiceElement authzService =
                         (XACMLAuthzServiceElement) authzServiceList.get(0);
                     data.put(ATTR_XACML_AUTHZ_SERVICE_BINDING,
                         returnEmptySetIfValueIsNull(
                         authzService.getBinding()));
                     data.put(ATTR_XACML_AUTHZ_SERVICE_LOCATION,
                         returnEmptySetIfValueIsNull(
                         authzService.getLocation()));
                 }
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PDP", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", paramsEx);
             throw new AMConsoleException(strError);
         } 
         return (data != null) ? data : Collections.EMPTY_MAP;
     }
 
     /**
      * Returns a <code>Map</code> containing the extended metadata for the PEP.
      *
      * @param realm where entity exists.
      * @param entityName name of entity descriptor.
      * @param location if the entity is remote or hosted.
      * @return key-value pair Map of PEP config data.
      * @throws AMConsoleException if unable to retrieve the PEP 
      *         extended metadata attribute     
      */
     public Map getPEPConfig(
         String realm,
         String entityName,
         String location
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "XACML PEP"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         
         Map data = null;
         List configList = null;
         String metaAlias = null;
         try {
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             XACMLAuthzDecisionQueryConfigElement xacmlAuthzConfigElement =
                     saml2Manager.getPolicyEnforcementPointConfig(
                     realm, entityName);
             
             if (xacmlAuthzConfigElement != null) {
                 data = new HashMap();
                 configList = xacmlAuthzConfigElement.getAttribute();
                 metaAlias = xacmlAuthzConfigElement.getMetaAlias();
                 int size = configList.size();
                 for (int i=0; i< size; i++) {
                     AttributeType atype = (AttributeType) configList.get(i);
                     String name = atype.getName();
                     java.util.List value = atype.getValue();
                     data.put(atype.getName(),
                         returnEmptySetIfValueIsNull(atype.getValue()));
                 }
                 data.put("metaAlias", metaAlias);
             }  else {
                 createEntityConfig(realm, entityName, "PEP", location);
             }            
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PEP", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", paramsEx);
             throw new AMConsoleException(strError);
         }
         return (data != null) ? data : Collections.EMPTY_MAP;
     }
     
     /**
      * Returns a Map of PDP Config data. (Extended Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor
      * @param location location of entity(hosted or remote) 
      * @return key-value pair Map of PPP config data.
      * @throws AMConsoleException if unable to retrieve the PDP 
      *         extended metadata attribute   
      */
     public Map getPDPConfig(
         String realm,
         String entityName,
         String location
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "XACML PDP"};
         logEvent("ATTEMPT_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         
         Map data = null;
         List configList = null;
         String metaAlias = null;
         try {
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             XACMLPDPConfigElement xacmlPDPConfigElement =
                     saml2Manager.getPolicyDecisionPointConfig(
                     realm, entityName);
             if (xacmlPDPConfigElement != null) {
                 data = new HashMap();
                 configList = xacmlPDPConfigElement.getAttribute() ;
                 metaAlias = xacmlPDPConfigElement.getMetaAlias();
                 int size = configList.size();
                 for (int i=0; i< size; i++) {
                     AttributeType atype = (AttributeType) configList.get(i);
                     String name = atype.getName();
                     java.util.List value = atype.getValue();
                     data.put(atype.getName(),
 		        returnEmptySetIfValueIsNull(atype.getValue()));
                 }
                 data.put("metaAlias", metaAlias);
             } else {
                 createEntityConfig(realm, entityName, "PDP", location);
             }
             logEvent("SUCCEED_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", params);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PDP", strError};
             logEvent("FEDERATION_EXCEPTION_GET_ENTITY_DESCRIPTOR_ATTR_VALUES", paramsEx);
             throw new AMConsoleException(strError);
         }
         return (data != null) ? data : Collections.EMPTY_MAP;
     }
     
     /**
      * Save standard metadata for PDP descriptor.
      *
      * @param realm realm of Entity.
      * @param entityName entity name of Entity Descriptor.
      * @param attrValues key-value pair Map of PDP standed data.
      * @throws AMConsoleException if fails to modify/save the PDP 
      *         standard metadata attribute   
      */
     public void updatePDPDescriptor(
         String realm,
         String entityName,
         Map attrValues
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "XACML PDP"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
       
         try {
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             EntityDescriptorElement entityDescriptor =
                 saml2Manager.getEntityDescriptor(realm, entityName) ;
             XACMLPDPDescriptorElement pdpDescriptor =
                 saml2Manager.getPolicyDecisionPointDescriptor(
                     realm,
                     entityName);
             
             if (pdpDescriptor != null) {
                 List authzServiceList = pdpDescriptor.getXACMLAuthzService();
                 if (authzServiceList.size() != 0) {
                     XACMLAuthzServiceElement authzService =
                         (XACMLAuthzServiceElement)authzServiceList.get(0);
                     authzService.setLocation((String)AMAdminUtils.getValue(
                         (Set)attrValues.get(
                             ATTR_XACML_AUTHZ_SERVICE_LOCATION)));
                 }
             }
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx =
             {realm, entityName, "SAMLv2", "XACML PDP", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR", paramsEx);
             throw new AMConsoleException(strError);
         }
     }
  
     /**
      * Save extended metadata for PDP Config.
      *
      * @param realm realm of Entity.
      * @param entityName entity name of Entity Descriptor.
      * @param location entity is remote or hosted.
      * @param attrValues key-value pair Map of PDP extended config.
      * @throws AMConsoleException if fails to modify/save the PDP 
      *         extended metadata attribute      
      */
     public void updatePDPConfig(
         String realm,
         String entityName,
         String location,
         Map attrValues
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "XACML PDP"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
            
         try {
             ObjectFactory objFactory = new ObjectFactory();
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             XACMLPDPConfigElement pdpEntityConfig =
                 saml2Manager.getPolicyDecisionPointConfig(
                 realm, entityName);            
             if (pdpEntityConfig == null) {
                 throw new AMConsoleException("invalid.xacml.configuration");
             } else {
                 List list = pdpEntityConfig.getAttribute();
                 Map values = convertSetToListInMap(attrValues);
                 for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
                     String key = (String)i.next();
                     AttributeType atype = objFactory.createAttributeType();
                     atype.setName(key);
                     atype.getValue().addAll((List)values.get(key));
                     list.add(atype);
                 }
             }
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (JAXBException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PDP", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR", paramsEx);
             throw new AMConsoleException(strError);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PDP", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR", paramsEx);
             throw new AMConsoleException(strError);
         }
     } 
     /**
      * Save the standard metadata for PEP descriptor.
      *
      * @param realm realm of Entity.
      * @param entityName entity name of Entity Descriptor.
      * @param attrValues key-value pair Map of PEP descriptor data.
      * throws AMConsoleException if there is an error.
      */
     public void updatePEPDescriptor(
         String realm,
         String entityName,
         Map attrValues
     ) throws AMConsoleException {
         // TBD : currently, there is nothing to save
     }
     
     /**
      * Save the extended metadata for PEP Config.
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @param location entity is remote or hosted
      * @param attrValues key-value pair Map of PEP extended config.
      * @throws AMConsoleException if fails to modify/save the PEP 
      *         extended metadata attributes   
      */
     public void updatePEPConfig(
         String realm,
         String entityName,
         String location,
         Map attrValues
     ) throws AMConsoleException {
         String[] params = {realm, entityName, "SAMLv2", "XACML PEP"};
         logEvent("ATTEMPT_MODIFY_ENTITY_DESCRIPTOR", params);
             
         try {
             ObjectFactory objFactory = new ObjectFactory();
             SAML2MetaManager saml2Manager = getSAML2MetaManager();
             XACMLAuthzDecisionQueryConfigElement pepEntityConfig =
                     saml2Manager.getPolicyEnforcementPointConfig(
                     realm, entityName);            
             if (pepEntityConfig == null) {
                 throw new AMConsoleException("invalid.xacml.configuration");
             } else {
                 List list = pepEntityConfig.getAttribute();
                 Map values = convertSetToListInMap(attrValues);
                 for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
                     String key = (String)i.next();
                     AttributeType atype = objFactory.createAttributeType();
                     atype.setName(key);
                     atype.getValue().addAll((List)values.get(key));
                     list.add(atype);
                 }
             }
             logEvent("SUCCEED_MODIFY_ENTITY_DESCRIPTOR", params);
         } catch (JAXBException e) {
             String strError = getErrorString(e);
             String[] paramsEx = 
                 {realm, entityName, "SAMLv2", "XACML PEP", strError};
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR", paramsEx);
             throw new AMConsoleException(strError);
         } catch (SAML2MetaException e) {
             String strError = getErrorString(e);
             String[] paramsEx =
                 {realm, entityName, "SAMLv2", "XACML PEP", strError}; 
             logEvent("FEDERATION_EXCEPTION_MODIFY_ENTITY_DESCRIPTOR", paramsEx);
             throw new AMConsoleException(strError);
         }
     } 
     /**
      * create Entity Config Object.(Extended Metadata)
      *
      * @param realm realm of Entity
      * @param entityName entity name of Entity Descriptor.
      * @param role role of provider (SP, IDP, PDP or PEP)
      * @param location entity is remote or hosted
      * @throws AMConsoleException if creation fails
      */
     private void createEntityConfig(
         String realm,
         String entityName,
         String role,
         String location
     ) throws AMConsoleException {
         String classMethod = "SAMLv2ModelImpl.createEntityConfig: ";
         
         try {
             SAML2MetaManager manager = getSAML2MetaManager();
             ObjectFactory objFactory = new ObjectFactory();
             // Check whether the entity id existed in the DS
             EntityDescriptorElement entityDesc =
                 manager.getEntityDescriptor(realm, entityName);
             
             if (entityDesc == null) {
                 throw new AMConsoleException(classMethod +
                     "invalid EntityName : " +
                     entityName);
             }
             EntityConfigElement entityConfig =
                 manager.getEntityConfig(realm, entityName);
             if (entityConfig == null) {
                 entityConfig =
                     objFactory.createEntityConfigElement();
                 // add to entityConfig
                 entityConfig.setEntityID(entityName);
                 if (location.equals("remote")) {
                     entityConfig.setHosted(false);
                 } else {
                     entityConfig.setHosted(true);
                 }
             }
             
             // create entity config and add the attribute
             BaseConfigType baseCfgType = null;
             
             // Decide which role EntityDescriptorElement includes
             // It could have one PDP and one PEP.
             if ((role.equals("PDP")) &&
                 (SAML2MetaUtils.getPolicyDecisionPointDescriptor(entityDesc) != null)) {
                 baseCfgType = objFactory.createXACMLPDPConfigElement();
                 for (Iterator iter = xacmlPDPExtendedMeta.keySet().iterator();
                 iter.hasNext(); ) {
                     AttributeType atype = objFactory.createAttributeType();
                     String key = (String)iter.next();
                     atype.setName(key);
                     atype.getValue().addAll((List)xacmlPDPExtendedMeta.get(key));
                     baseCfgType.getAttribute().add(atype);
                 }
                 entityConfig.getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig().add(baseCfgType);
             } else if ((role.equals("PEP")) &&
                 (SAML2MetaUtils.getPolicyEnforcementPointDescriptor(entityDesc) != null)) {
                 baseCfgType = objFactory.createXACMLAuthzDecisionQueryConfigElement();
                 for (Iterator iter = xacmlPEPExtendedMeta.keySet().iterator();
                 iter.hasNext(); ) {
                     AttributeType atype = objFactory.createAttributeType();
                     String key = (String)iter.next();
                     atype.setName(key);
                     atype.getValue().addAll((List)xacmlPEPExtendedMeta.get(key));
                     baseCfgType.getAttribute().add(atype);
                 }
                 entityConfig.getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig().add(baseCfgType);
             }
             manager.setEntityConfig(realm, entityConfig);
         } catch (JAXBException e) {
             throw new AMConsoleException(e);
         } catch (SAML2MetaException e){
             throw new AMConsoleException(e);
         }
     }
 
     protected SAML2MetaManager getSAML2MetaManager() throws SAML2MetaException {
         if (metaManager == null) {
             metaManager = new SAML2MetaManager();
         }
         return metaManager;
     }
        
     private boolean setToBoolean(Map map, String value) {
         Set set = (Set)map.get(value);
         return ((set != null) && !set.isEmpty()) ?
             Boolean.parseBoolean((String)set.iterator().next()) : false;
     }
 
     private String getResult(Map map, String value) {
         Set set = (Set)map.get(value);
         Iterator  i = set.iterator();
         String val = null;
         while ((i !=  null) && (i.hasNext())) {
             val = (String)i.next();
         }
         return val;
     }
     
     /**
      * Returns SAMLv2 Extended Service Provider attribute values.
      *
      * @return SAMLv2 Extended Service Provider attribute values.
      */
     public Map getSPEXDataMap() {
         return  extendedMetaSpMap;
     }
     
     /**
      * Returns SAMLv2 Extended Identity Provider attribute values.
      *
      * @return SAMLv2 Extended Identity Provider attribute values.
      */
     public Map getIDPEXDataMap() {
         return extendedMetaIdpMap;
     }
 }
