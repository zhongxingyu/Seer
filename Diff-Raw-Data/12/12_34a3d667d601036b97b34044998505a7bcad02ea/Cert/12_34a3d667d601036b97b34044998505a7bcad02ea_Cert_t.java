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
 * $Id: Cert.java,v 1.3 2006-09-22 19:17:35 pbryan Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.authentication.modules.cert;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.X509CRL;
 import java.security.cert.X509Certificate;
 import java.security.Security;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.mail.internet.MimeUtility;
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import javax.servlet.http.HttpServletRequest;
 
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPUrl;
 
 import com.sun.identity.shared.datastruct.CollectionHelper;
 import com.iplanet.am.util.SSLSocketFactoryManager;
 import com.sun.identity.security.cert.CRLValidator;
 import com.sun.identity.security.cert.OCSPValidator;
 import com.sun.identity.security.cert.X509CRLValidatorFactory;
 import com.sun.identity.security.cert.X509OCSPValidatorFactory;
 import com.iplanet.security.x509.X500Name;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.authentication.service.X509CertificateCallback;
 import com.sun.identity.authentication.spi.AMLoginModule;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.authentication.util.ISAuthConstants;
 import com.sun.identity.security.cert.AMCRLStore;
 import com.sun.identity.security.cert.AMCertStore;
 import com.sun.identity.security.cert.AMLDAPCertStoreParameters;
 import com.sun.identity.security.cert.AMCertPath;
 
 public class Cert extends AMLoginModule {
 
     private static java.util.Locale locale = null;
     private ResourceBundle bundle = null;
 
     private String userTokenId = null;
     private X509Certificate thecert = null;
 
     // from profile server.
     // default: MUST HAVE where is the ldap server.
     private String amAuthCert_serverHost;  
     // default: values stored in auth.certificate.ldap.server.context; 
     // think ok to be nil.
     private String amAuthCert_startSearchLoc;  
     // none, simple or CRAM-MD5 (default to NONE)
     private String amAuthCert_securityType; 
     // ldap user name [if missing default to amAuthCert_securityType to none.]
     private String amAuthCert_principleUser;  
     // ldap user's passwd  
     // [if missing default to amAuthCert_securityType to none.]
     private String amAuthCert_principlePasswd;  
     // use ssl to talk to ldap. default is false.
     private String amAuthCert_useSSL;        
     // Field in Cert to user to access user profile.  default to DN
     private String amAuthCert_userProfileMapper;    
     // Alternate Field in Cert to userid to access user profile 
     // if above is "other"
     private String amAuthCert_altUserProfileMapper;
     // check user cert against revoke list in LDAP.
     private String amAuthCert_chkCRL;        
     // attr to use in search for user cert in CRL in LDAP
     private String amAuthCert_chkAttrCRL = null;
     // params to use in accessing CRL DP
     private String amAuthCert_uriParamsCRL = null;
     // check user cert with cert in LDAP.
     private String amAuthCert_chkCertInLDAP; 
     // attr to use in search for user cert in LDAP
     private String amAuthCert_chkAttrCertInLDAP = null;
     // this is what appears in the user selectable choice field.
     private String amAuthCert_emailAddrTag; 
     private int amAuthCert_serverPort =389;
     private static boolean portal_gw_cert_auth_enabled = false;
     private Set portalGateways = null;
     // HTTP Header name to have clien certificate in servlet request.
     private String certParamName = null;
     private boolean ocspEnabled = false;
     private AMLDAPCertStoreParameters ldapParam = null;
 
     // configurations
     private Map options;
     private CertAuthPrincipal userPrincipal;
     private CallbackHandler callbackHandler;
     static final int ldap_version = 3;
 
     private static final String amAuthCert = "amAuthCert";
     
     private static com.sun.identity.shared.debug.Debug debug = null;
 
     /**
      * Default module constructor does nothing
      */
     public Cert() {
     }
 
     /**
      * Initialize module 
      * @param subject for auth
      * @param sharedState with auth framework
      * @param options for auth
      */
     public void init(Subject subject, Map sharedState, Map options) {
         if (debug == null) {
             debug = com.sun.identity.shared.debug.Debug.getInstance(amAuthCert);
         }
         java.util.Locale locale = getLoginLocale();
         bundle = amCache.getResBundle(amAuthCert, locale);
 
         this.callbackHandler = getCallbackHandler();
         this.options = options;
         if (debug.messageEnabled()) {
             debug.message("Cert Auth resbundle locale="+locale);
             debug.message("Cert auth init() done");
         }
     } 
 
     private void initAuthConfig() throws AuthLoginException {
         if (options != null) {
             debug.message("Certificate: getting attributes."); 
             // init auth level
             String authLevel = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-auth-level");
             if (authLevel != null) {
                 try {
                     int tmp = Integer.parseInt(authLevel);
                     setAuthLevel(tmp);
                 } catch (Exception e) {
                     // invalid auth level
                     debug.error("Invalid auth level " + authLevel, e);
                 }
             } 
             // will need access control to ldap server; passwd and user name
             // will also need to yank out the user profile based on cn or dn
             //  out of "profile server"
             amAuthCert_securityType = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-security-type");
             amAuthCert_principleUser = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-principal-user");
                amAuthCert_principlePasswd = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-principal-passwd");
             amAuthCert_useSSL = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-use-ssl");
             amAuthCert_userProfileMapper = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-user-profile-mapper"); 
             amAuthCert_altUserProfileMapper = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-user-profile-mapper-other");
             amAuthCert_chkCRL = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-check-crl"); 
             if (amAuthCert_chkCRL.equalsIgnoreCase("true")) {
                 amAuthCert_chkAttrCRL = CollectionHelper.getMapAttr(
                     options, "iplanet-am-auth-cert-attr-check-crl");
                 if (amAuthCert_chkAttrCRL == null || 
                     amAuthCert_chkAttrCRL.equals("")) {
                     throw new AuthLoginException(amAuthCert, "noCRLAttr", null);
                 }
             }
 
             amAuthCert_uriParamsCRL = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-param-get-crl");
             amAuthCert_chkCertInLDAP = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-check-cert-in-ldap"); 
             if (amAuthCert_chkCertInLDAP.equalsIgnoreCase("true")) {
                 amAuthCert_chkAttrCertInLDAP = CollectionHelper.getMapAttr(
                     options, "iplanet-am-auth-cert-attr-check-ldap");
                 if (amAuthCert_chkAttrCertInLDAP == null ||
                     amAuthCert_chkAttrCertInLDAP.equals("")) {
                     throw new AuthLoginException(
                         amAuthCert, "noLDAPAttr", null);
                 }
             }
             String ocspChk = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-check-ocsp"); 
             ocspEnabled = (ocspChk != null && ocspChk.equalsIgnoreCase("true"));
 
              //
             //  portal-style gateway cert auth enabled if
             //  explicitly specified in cert service template.
             //  "none", empty list, or null means disabled;
             //  "any" or non-empty list means enabled.  also check
             //  non-empty list for remote client's addr.
             //
             String gwCertAuth = CollectionHelper.getMapAttr(
                 options, "iplanet-am-auth-cert-gw-cert-auth-enabled");
             certParamName = CollectionHelper.getMapAttr(
                 options,"sunAMHttpParamName");
 
             HttpServletRequest req = getHttpServletRequest();
             String client = null;
             if (req != null) {
                 client = req.getRemoteAddr();
             }
             portal_gw_cert_auth_enabled = false;
             if (gwCertAuth == null || gwCertAuth.equals("") 
                                 || gwCertAuth.equalsIgnoreCase("none")) {
                 if (debug.messageEnabled()) {
                     debug.message("iplanet-am-auth-cert-gw-cert-auth-enabled = "
                         + gwCertAuth);
                 }
             } else if (gwCertAuth.equalsIgnoreCase("any")) {
                 portal_gw_cert_auth_enabled = true;
             } else {
                 portalGateways = 
                   (Set)options.get("iplanet-am-auth-cert-gw-cert-auth-enabled");
                 if ((client !=null) && (portalGateways.contains(client))) {
                     portal_gw_cert_auth_enabled = true;
                 } else {
                     if (debug.messageEnabled()) {
                         debug.message("gateway list does not contain client");
                         Iterator clientIter = portalGateways.iterator();
                         while (clientIter.hasNext()) {
                             String clientStr = (String)clientIter.next();
                             debug.message("client list entry = " + clientStr);
                         }
                     }
                  }
             }
 
             amAuthCert_emailAddrTag = bundle.getString("emailAddrTag");
 
             amAuthCert_serverHost = CollectionHelper.getServerMapAttr(
                 options, "iplanet-am-auth-cert-ldap-provider-url");
             if (amAuthCert_serverHost == null 
                 && (amAuthCert_chkCertInLDAP.equalsIgnoreCase("true") || 
                     amAuthCert_chkCRL.equalsIgnoreCase("true"))) {
                 debug.error("Fatal error: LDAP Server and Port misconfigured");
                 throw new AuthLoginException(amAuthCert, 
                                 "wrongLDAPServer", null);
             }
 
             if (amAuthCert_serverHost != null) {
                 // set LDAP Parameters
                 try {
                     LDAPUrl ldapUrl = 
                             new LDAPUrl("ldap://"+amAuthCert_serverHost);
                     amAuthCert_serverPort = ldapUrl.getPort();
                     amAuthCert_serverHost = ldapUrl.getHost();
                 } catch (Exception e) {
                     throw new AuthLoginException(amAuthCert, "wrongLDAPServer",
                         null);
                 }
             }
 
             amAuthCert_startSearchLoc = CollectionHelper.getServerMapAttr(
                 options, "iplanet-am-auth-cert-start-search-loc");
             if (amAuthCert_startSearchLoc == null 
                 && (amAuthCert_chkCertInLDAP.equalsIgnoreCase("true") || 
                     amAuthCert_chkCRL.equalsIgnoreCase("true"))) {
                 debug.error("Fatal error: LDAP Start Search " +
                                 "DN is not configured");
                 throw new AuthLoginException(amAuthCert, "wrongStartDN", null);
             } 
             
             if (amAuthCert_startSearchLoc != null) {
                 try {
                     X500Name baseDN = new X500Name(amAuthCert_startSearchLoc);
                 }
                 catch (Exception e) {
                     debug.error("Fatal error: LDAP Start Search " +
                                     "DN misconfigured");
                     throw new AuthLoginException(amAuthCert, "wrongStartDN",
                         null);
                 }
             }
 
             if (debug.messageEnabled()) {
                 debug.message("\nldapProviderUrl="+ amAuthCert_serverHost +
                     "\n\tamAuthCert_serverPort = " + amAuthCert_serverPort +
                     "\n\tstartSearchLoc=" + amAuthCert_startSearchLoc +
                     "\n\tsecurityType=" + amAuthCert_securityType +
                     "\n\tprincipleUser=" + amAuthCert_principleUser +
                     "\n\tauthLevel="+authLevel+
                     "\n\tuseSSL=" + amAuthCert_useSSL +
                     "\n\tocspEnable=" + ocspEnabled +
                     "\n\tuserProfileMapper=" + amAuthCert_userProfileMapper +
                     "\n\taltUserProfileMapper=" + 
                         amAuthCert_altUserProfileMapper +
                     "\n\tchkCRL=" + amAuthCert_chkCRL +
                     "\n\tchkAttrCRL=" + amAuthCert_chkAttrCRL +
                     "\n\tchkCertInLDAP=" + amAuthCert_chkCertInLDAP +
                     "\n\tchkAttrCertInLDAP=" + amAuthCert_chkAttrCertInLDAP +
                     "\n\temailAddr=" + amAuthCert_emailAddrTag +
                     "\n\tgw-cert-auth-enabled="+portal_gw_cert_auth_enabled +
                     "\n\tclient=" + client);
             }
         } else {
             debug.error("options is null");
             throw new AuthLoginException(amAuthCert, "CERTex", null);
         }
     }
     
     /**
      * Process Certificate based auth request
      * @param callbacks for auth
      * @param state with auth framework
      * @return proper jaas state for auth framework
      * @throws AuthLoginException if auth fails
      */
     public int process (Callback[] callbacks, int state) 
         throws AuthLoginException {
         initAuthConfig();
         try {
             HttpServletRequest servletRequest = getHttpServletRequest();
             if (servletRequest != null) { 
                 X509Certificate[] allCerts = (X509Certificate[]) servletRequest.
                    getAttribute("javax.servlet.request.X509Certificate"); 
                 if (allCerts == null || allCerts.length == 0) {
                     debug.message(
                           "Certificate: checking for cert passed in the URL.");
                     if (!portal_gw_cert_auth_enabled) {
                         debug.error ("Certificate: cert passed " +
                                      "in URL not enabled for this client");
                         throw new AuthLoginException(amAuthCert, 
                             "noURLCertAuth", null);
                     }
 
                     thecert = getPortalStyleCert(servletRequest); 
                 } else {
                     if (debug.messageEnabled()) {
                         debug.message("Certificate: got all certs from " + 
                             "HttpServletRequest =" + allCerts.length);
                     }
                     thecert = allCerts[0];
                 }
             } else {
                 thecert = sendCallback();
             }
 
             if (thecert == null) {
                 debug.message("Certificate: no cert passed in.");
                 throw new AuthLoginException(amAuthCert, "noCert", null);
             }
 
             // moved this call from the bottom to here so that url redirection
             // can work.
             getTokenFromCert(thecert);
             storeUsernamePasswd(userTokenId, null);
             if(debug.messageEnabled()){
                 debug.message("in Certificate. userTokenId=" + 
                     userTokenId + " from getTokenFromCert");
             }
         } catch (AuthLoginException e) {
             setFailureID(userTokenId);
             debug.error("Certificate:  exiting validate with exception", e);
             throw new AuthLoginException(amAuthCert, "noCert", null);
         }
 
         /* debug statements added for cgi. */
         if (debug.messageEnabled()) {
             debug.message("Got client cert =\n" + thecert.toString());
         }
 
         if (amAuthCert_chkCertInLDAP.equalsIgnoreCase("false") && 
                 amAuthCert_chkCRL.equalsIgnoreCase("false") &&
                                 !ocspEnabled) {
                 return ISAuthConstants.LOGIN_SUCCEED;
         }
 
         if (ocspEnabled) {
             OCSPValidator ocsp = 
                 X509OCSPValidatorFactory.getInstance().createOCSPValidator();
             if ((ocsp == null) || 
                 (ocsp.validateCertificate(thecert) == false)) {
                        debug.error("OCSP cert validation failed");
                 setFailureID(userTokenId);
                 throw new AuthLoginException(amAuthCert, 
                                 "CertIsNotValid", null);
             }
         }
 
         /*
         * Based on the certificates presented, find the registered
         * (representation) of the certificate. If no certificates
         * match in the LDAP certificate directory return a failure
         * status.
         */
         if (amAuthCert_chkCertInLDAP.equalsIgnoreCase("true")) { 
             X509Certificate ldapcert = getRegisteredCertificate(thecert);
             if (ldapcert == null) {
                 debug.error("X509Certificate: getRegCertificate is null");
                 setFailureID(userTokenId);
                 throw new AuthLoginException(amAuthCert, "CertNoReg", null);
             }
         }
         
         if (amAuthCert_chkCRL.equalsIgnoreCase("true")) {
             boolean valid = false;
             CRLValidator validator =  
                     X509CRLValidatorFactory.getInstance().createCRLValidator();
             if (validator != null) {
                 if (ldapParam == null) {
                     setLdapStoreParam();   
                 }
             
                 validator.setLdapParams(ldapParam);
                 valid = 
                   validator.validateCertificate(thecert, amAuthCert_chkAttrCRL);
             }
             
             if (valid == false) {
                 debug.error("X509Certificate:CRL verify failed.");
                     setFailureID(userTokenId);
                 throw new AuthLoginException(amAuthCert, 
                                 "CertVerifyFailed", null);
             }
         }
         
         return ISAuthConstants.LOGIN_SUCCEED;
     }
 
     private void setLdapStoreParam() throws AuthLoginException {
     /*
      * Setup the LDAP certificate directory service context for
      * use in verification of the users certificates.
      */
         ldapParam = new AMLDAPCertStoreParameters
                             (amAuthCert_serverHost, amAuthCert_serverPort);
         try {
             AMLDAPCertStoreParameters.setLdapStoreParam(ldapParam,
                                     amAuthCert_principleUser,
                                        amAuthCert_principlePasswd,
                                     amAuthCert_startSearchLoc,
                                     amAuthCert_uriParamsCRL, 
                                     amAuthCert_useSSL.equalsIgnoreCase("true"));
         } catch (Exception e) {
             debug.error("validate.SSLSocketFactory", e);
             setFailureID(userTokenId);
             throw new AuthLoginException(amAuthCert,"sslSokFactoryFail", null);
         }
             
         return;
     }
 
     private void getTokenFromCert(X509Certificate cert)
         throws AuthLoginException {
     /*
      * The certificate has passed the authentication steps
      * so return the part of the certificate as specified 
      * in the profile server.
      */
         try {
         /*
          * Get the Attribute value of the input certificate
          */
             sun.security.x509.X500Name certDNname = 
                             (sun.security.x509.X500Name)cert.getSubjectDN();
             X500Name certDN = new X500Name(certDNname.getEncoded());
             if (debug.messageEnabled()) {
                 debug.message("getTokenFromCert: Subject DN : " + 
                                 certDN.getName());
             }
 
             if (amAuthCert_userProfileMapper.equalsIgnoreCase("subject DN")) { 
                 userTokenId = certDN.getName();
             }
 
             if (amAuthCert_userProfileMapper.equalsIgnoreCase("subject UID")) {
                 userTokenId = certDN.getAttributeValue("uid");
             }
 
             if (amAuthCert_userProfileMapper.equalsIgnoreCase("subject CN")) { 
                 userTokenId = certDN.getCommonName(); 
             }
 
             if (amAuthCert_userProfileMapper.equalsIgnoreCase
                                                   (amAuthCert_emailAddrTag)) {
                 userTokenId = certDN.getEmail();
                 if (userTokenId == null) {
                     userTokenId = certDN.getAttributeValue("mail");
                 }
             }
 
             if (amAuthCert_userProfileMapper.
                             equalsIgnoreCase("DER Certificate")) { 
                 userTokenId = String.valueOf(cert.getTBSCertificate());
             }
 
             //  "other" has been selected, so use attribute specified in the
             //  iplanet-am-auth-cert-user-profile-mapper-other attribute,
             //  which is in amAuthCert_altUserProfileMapper.
             if (amAuthCert_userProfileMapper.equals("other")) {
                 userTokenId = certDN.getAttributeValue
                                            (amAuthCert_altUserProfileMapper);
             }
 
             if (debug.messageEnabled()) {
                 debug.message("getTokenFromCert: " + 
                                 amAuthCert_userProfileMapper + userTokenId);
             }
 
             return;
         } catch (Exception e) {
             if (debug.messageEnabled()) {
                 debug.message("Certificate - Error in getTokenFromCert = " , e);
             }
             throw new AuthLoginException(amAuthCert, "CertNoReg", null);
         }
     }
 
     private X509Certificate getRegisteredCertificate (X509Certificate cert) {
                 X509Certificate ldapcert = null;
             /*
          * Get the CN of the input certificate
          */
         String attrValue = null;
         
         try {
             X500Name dn = AMCertStore.getSubjectDN(cert);
             // Retrieve attribute value of amAuthCert_chkAttrCertInLDAP
             if (dn != null) {
                 attrValue = dn.getAttributeValue(amAuthCert_chkAttrCertInLDAP);
             }
         }
         catch (Exception ex) {
             if (debug.messageEnabled()) {
                 debug.message("Certificate - cn substring: " + ex); 
             }
             return null;
         }
 
         if (attrValue == null)
             return null;
         
         if (debug.messageEnabled()) {
             debug.message("Certificate - cn substring: " + attrValue); 
         }
 
         /*
          * Lookup the certificate in the LDAP certificate
          * directory and compare the values.
          */ 
 
         try {
             String searchFilter = 
                     AMCertStore.setSearchFilter(amAuthCert_chkAttrCertInLDAP, 
                                                 attrValue);
 
             if (ldapParam == null) {
                     setLdapStoreParam();
                 }
 
             ldapParam.setSearchFilter(searchFilter);
             AMCertStore store = new AMCertStore(ldapParam);
             ldapcert = store.getCertificate(cert);
         } catch (Exception e) {
             if (debug.messageEnabled()) {
                 debug.message("Certificate - " +
                                 "Error finding registered certificate = " , e);
             }
         }
 
         return ldapcert;
     }
     
     public java.security.Principal getPrincipal() {
         if (userPrincipal != null) {
             return userPrincipal;
         } else if (userTokenId != null) {
             userPrincipal = new CertAuthPrincipal(userTokenId);
             return userPrincipal;
         } else {
             return null;
         }
     }
 
     /**
      * Return value of Certificate 
      * @return X509Certificate for auth
      */
     public X509Certificate getCertificate() {
        return thecert;
     }
 
     /**
      * Return value of Attribute Name for CRL checking 
      * @return value for attribute name to search crl from ldap store
      */
     public String getChkAttrCRL() {
        return amAuthCert_chkAttrCRL;
     }
         
     /**
      * Return value of Debug object for this module 
      *
      * @return debug 
      */
     public com.sun.identity.shared.debug.Debug getDebug() {
        return debug;
     }
 
     /**
      * Return value of URI parameter for getting CRL 
      *
      * @return value of URI parameter for getting CRL 
      */
     public String getUriParamsCRL() {
        return amAuthCert_uriParamsCRL;
     }
         
     /**
      * Return value of LDAP Search loc for directory server 
      *
      * @return value of LDAP Search loc for directory server  
      */
     public String getStartSearchLoc() {
        return amAuthCert_startSearchLoc;
     }
                         
     private X509Certificate sendCallback() throws AuthLoginException {
         if (callbackHandler == null) {
             throw new AuthLoginException(amAuthCert, "NoCallbackHandler", null);
         }
         X509Certificate cert = null;
         try {
             Callback[] callbacks = new Callback[1];
             callbacks[0] = 
                 new X509CertificateCallback (bundle.getString("certificate"));
             callbackHandler.handle(callbacks);
             cert = ((X509CertificateCallback)callbacks[0]).getCertificate();
             return cert; 
         } catch (IllegalArgumentException ill) {
             debug.message("message type missing");
             throw new AuthLoginException(amAuthCert, "IllegalArgs", null);
         } catch (java.io.IOException ioe) {
             throw new AuthLoginException(ioe);
         } catch (UnsupportedCallbackException uce) {
             throw new AuthLoginException(amAuthCert, "NoCallbackHandler", null);
         }
     }
 
     private X509Certificate getPortalStyleCert (HttpServletRequest request) 
        throws AuthLoginException {
        String certParam = null;
 
        if ((certParamName != null) && (certParamName.length() > 0)) {
            debug.message ("getPortalStyleCert: checking cert in HTTP header");
            StringTokenizer tok = new StringTokenizer(certParamName, ",");
            while (tok.hasMoreTokens()) {
                String key = tok.nextToken();
                 certParam = request.getHeader(key);
                 if (certParam == null) {
                     continue;
                 }
                 certParam = certParam.trim();
                 String begincert = "-----BEGIN CERTIFICATE-----";
                 String endcert = "-----END CERTIFICATE-----";
                 int idx = certParam.indexOf(endcert);
                 if (idx != -1) {
                     certParam = certParam.substring(begincert.length(), idx);
                     certParam = certParam.trim();
                 }
            }
        } else {
            debug.message("getPortalStyleCert: checking cert in userCert param");
            Hashtable requestHash = 
                getLoginState("getPortalStyleCert()").getRequestParamHash(); 
            if (requestHash != null) {
                certParam = (String) requestHash.get("IDToken0"); 
                if (certParam == null) {
                    certParam = (String) requestHash.get("Login.Token0"); 
                }
            }
        }
 
        if (debug.messageEnabled()) {
            debug.message ("in Certificate. validate certParam: " + certParam);
        }
        if (certParam == null || certParam.equals("")) {
            debug.message("Certificate: no cert from HttpServletRequest");
            throw new AuthLoginException(amAuthCert, "noCert", null);
        }
 
        byte certbytes [] = certParam.getBytes();
        debug.message("in Certificate: got certbytes"); 
 
        // use the base64 decoder from MimeUtility instead of writing our own
        ByteArrayInputStream barray = new ByteArrayInputStream(certbytes);
        InputStream carray = null;
        try {
            carray = MimeUtility.decode(barray, "base64");
        } catch (Exception e) {
            debug.error("CertificateFromParameter(decode): exception", e);
            throw new AuthLoginException(amAuthCert, "CERTex", null);
        }
 
        debug.message("Certificate: CertificateFactory.getInstance.");
        CertificateFactory cf = null;
        X509Certificate userCert = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            userCert = (X509Certificate) cf.generateCertificate(carray);
        } catch (Exception e) {
            debug.error("CertificateFromParameter(X509Cert): exception ", e);
            throw new AuthLoginException(amAuthCert, "CERTex", null);
        }
 
        if (userCert == null) {
            throw new AuthLoginException(amAuthCert, "CERTex", null);
        }
 
        if (debug.messageEnabled()) {
            debug.message("X509Certificate: principal is: " + 
                userCert.getSubjectDN().getName() +
                "\nissuer DN:" + userCert.getIssuerDN().getName() +
                "\nserial number:" + String.valueOf(userCert.getSerialNumber()) +
                "\nsubject dn:" + userCert.getSubjectDN().getName());
         }
         return userCert;
     }
 
     /**
      * Destroy the state of module
      */
     public void destroyModuleState() {
         userPrincipal = null;
         userTokenId = null;
     }
 
     /**
      * Initialize all member variables as null
      */
     public void nullifyUsedVars() {
         bundle = null;
         thecert = null;
         options = null;
         callbackHandler = null;
         amAuthCert_serverHost = null;
         amAuthCert_startSearchLoc = null;
         amAuthCert_securityType = null;
         amAuthCert_principleUser = null;
         amAuthCert_principlePasswd = null;
         amAuthCert_useSSL = null;
         amAuthCert_userProfileMapper = null;
         amAuthCert_altUserProfileMapper = null;
         amAuthCert_chkCRL = null;
         amAuthCert_chkAttrCRL = null;
         amAuthCert_uriParamsCRL = null;
         amAuthCert_chkCertInLDAP = null;
         amAuthCert_chkAttrCertInLDAP = null;
         amAuthCert_emailAddrTag = null;
         portalGateways = null;
     }
 }
