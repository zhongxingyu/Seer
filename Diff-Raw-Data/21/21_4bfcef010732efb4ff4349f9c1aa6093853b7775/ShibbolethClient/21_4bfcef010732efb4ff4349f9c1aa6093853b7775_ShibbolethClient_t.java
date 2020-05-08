 /*
  * Copyright (c) 2007-2009. Members of the EGEE Collaboration.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
 * $Id: ShibbolethClient.java,v 1.17 2009/09/15 15:22:31 vtschopp Exp $
  */
 package org.glite.slcs.shibclient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.httpclient.Cookie;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.cookie.CookieSpecBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.glite.slcs.AuthException;
 import org.glite.slcs.RemoteException;
 import org.glite.slcs.SLCSConfigurationException;
 import org.glite.slcs.ServiceException;
 import org.glite.slcs.UnknownResourceException;
 import org.glite.slcs.jericho.html.Element;
 import org.glite.slcs.jericho.html.FormControl;
 import org.glite.slcs.jericho.html.FormControlType;
 import org.glite.slcs.jericho.html.Source;
 import org.glite.slcs.jericho.html.Tag;
 import org.glite.slcs.shibclient.metadata.IdentityProvider;
 import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;
 
 /**
  * ShibbolethClient is a SSO login parser and a Shibboleth Browser/POST
  * (SAML/POST) or SAML/Artifact profile handler.
  * <p>
  * <b>This code is just a hack. Don't rely on it to develop something else, you
  * have been warned.</b>
  * 
  * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.17 $
  */
 public class ShibbolethClient {
 
     /** Logging */
     private static final Log LOG = LogFactory.getLog(ShibbolethClient.class);
 
     /** The HttpClient delegate */
     private HttpClient httpClient_;
 
     /** Shibboleth client metadata */
     private ShibbolethClientMetadata metadata_;
 
     /** Shibboleth client credentials */
     private ShibbolethCredentials credentials_;
 
     /** authenticated or not */
     private boolean isAuthenticated_ = false;
 
     /**
      * Creates a Shibboleth client wrapping a {@link HttpClient}. Requires the
      * Shibboleth client metadata and the Shibboleth credentials.
      * 
      * @param client
      *            The HttpClient engine.
      * @param metadata
      *            The Shibboleth client metadata
      * @param credentials
      *            The Shibboleth credentials (username and password)
      * @throws SLCSConfigurationException
      */
     public ShibbolethClient(HttpClient client,
             ShibbolethClientMetadata metadata, ShibbolethCredentials credentials)
             throws SLCSConfigurationException {
         // check consistancy of creds and metadata
         String id = credentials.getIdentityProviderID();
         IdentityProvider idp = metadata.getIdentityProvider(id);
         if (idp == null) {
             LOG.error("Unknown IdP " + id);
             throw new SLCSConfigurationException(
                     "IdP providerId: "
                             + id
                             + " is not defined in metadata file.\nPlease run the 'slcs-info' command.");
         }
 
         this.httpClient_ = client;
         this.metadata_ = metadata;
         this.credentials_ = credentials;
 
     }
 
     /**
      * Authenticates the user with his IdP for the default SLCS SP.
      * 
      * @return <code>true</code> iff the user have been authenticated by his
      *         Identity Provider.
      * @throws ServiceException
      * @throws RemoteException
      * @throws AuthException
      * @throws UnknownResourceException
      */
     public boolean authenticate() throws AuthException, RemoteException,
             ServiceException, UnknownResourceException {
         String slcsEntryURL = metadata_.getSLCS().getUrl();
         return authenticate(slcsEntryURL);
     }
 
     /**
      * Authenticates the user on his IdP for the given SP.
      * 
      * @param spEntryURL
      *            The Service Provider entry point URL
      * @return <code>true</code> iff the user have been authenticated by his
      *         Identity Provider.
      * @throws AuthException
      * @throws RemoteException
      * @throws ServiceException
      * @throws UnknownResourceException
      */
     public boolean authenticate(String spEntryURL) throws AuthException,
             RemoteException, ServiceException, UnknownResourceException {
 
         String idpProviderID = credentials_.getIdentityProviderID();
         IdentityProvider idp = metadata_.getIdentityProvider(idpProviderID);
         if (idp == null) {
             throw new UnknownResourceException("IdP " + idpProviderID
                     + " not found in Metadata");
         }
         LOG.info(spEntryURL + " IdP=" + idp.getUrl() + " AuthType="
                 + idp.getAuthTypeName());
         String idpURL = idp.getUrl();
         LOG.debug("idpURL=" + idpURL);
         // LOG.debug("wayfURL=" + wayfURL);
         try {
             URI idpSSOResponseURI = null;
 
             // 1. get the first redirection or already authenticated
             URI spLoginResponseURI = processSPEntry(spEntryURL);
             // either wayf or idp or same (already authenticated)
             String spLoginResponseURL = spLoginResponseURI.getEscapedURI();
             LOG.debug("spLoginResponseURL=" + spLoginResponseURL);
             // check if already authenticated
             if (spLoginResponseURL.startsWith(spEntryURL)) {
                 LOG.info("Already authenticated? " + isAuthenticated_ + ": "
                         + spLoginResponseURL);
                 return this.isAuthenticated_;
             }
             // checks if URL contains the shire= parameter (WAYF of IdP)
             if (spLoginResponseURL.indexOf("shire=") == -1
                     && spLoginResponseURL.indexOf("shire%3D") == -1) {
                 LOG.error("Unexpected spLoginResponseURL=" + spLoginResponseURL);
                 throw new ServiceException(
                         "SP response URL doesn't contain the 'shire=' parameter: "
                                 + spLoginResponseURL);
             }
             else {
                 LOG.debug("IdP SSO: " + idpURL);
                 // 2. process the IdP SSO login
                 idpSSOResponseURI = processIdPSSO(idp, spLoginResponseURI);
             }
 
             // 3. process the IdP SSO response -> Artifact or Browser/POST
             // profile
             URI idpResponseURI = processIdPSSOResponse(idp, idpSSOResponseURI);
             String url = idpResponseURI.getURI();
             if (url.equals(spEntryURL)) {
                 this.isAuthenticated_ = true;
                 LOG.info("Sucessful authentication");
             }
 
         } catch (URIException e) {
             LOG.error("URIException: " + e);
             e.printStackTrace();
         } catch (HttpException e) {
             LOG.error("HttpException: " + e);
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("IOException: " + e);
             e.printStackTrace();
             throw new RemoteException(e);
         }
         return this.isAuthenticated_;
     }
 
     /**
      * Processes the response of the IdP SSO and dispatches to the Browser/POST
      * or the Artificate processor.
      * 
      * @param idp
      *            The {@link IdentityProvider}.
      * @param idpSSOResponseURI
      *            The IdP SSO response {@link URI}.
      * @return
      * @throws RemoteException
      */
     private URI processIdPSSOResponse(IdentityProvider idp,
             URI idpSSOResponseURI) throws HttpException, IOException,
             RemoteException {
         URI idpResponseURI = null;
 
         String idpSSOResponseURL = idpSSOResponseURI.getURI();
         GetMethod getIdPSSOResponseMethod = new GetMethod(idpSSOResponseURL);
         
         // BUG FIX: set the JSESSIONID and IdP 2.X _idp_session 
         // cookies by hand.
         // Because default CookiePolicy is RFC2109 and doesn't handle
         // correctly FQDN hostname as cookie domain.
        /*
         String host = getIdPSSOResponseMethod.getURI().getHost();
         String path = getIdPSSOResponseMethod.getURI().getPath();
         Cookie cookies[] = getMatchingCookies(host, path);
         for (int j = 0; j < cookies.length; j++) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("setting Cookie: " + cookies[j]);
             }
             getIdPSSOResponseMethod.addRequestHeader("Cookie", cookies[j].toString());
         }
        */
 
         // BUG FIX: check if url have a SAML/Artifact endpoint
         boolean isPossiblyUsingArtifact = false;
         if (matchesSAMLArtifact(idpSSOResponseURL)) {
             isPossiblyUsingArtifact = true;
             LOG.info("The SP try to use a SAML/Artifact profile");
         }
 
         LOG.info("GET IdPSSOResponse: " + idpSSOResponseURL);
         int idpSSOResponseStatus = executeMethod(getIdPSSOResponseMethod);
         LOG.debug(getIdPSSOResponseMethod.getStatusLine());
 
         // SAML/Artifact are already processed.
         if (isPossiblyUsingArtifact) {
             // check if there is a loop. if so the SSO failed for some reason...
             if (idpSSOResponseURI.equals(getIdPSSOResponseMethod.getURI())) {
                 String htmlBody = inputStreamToString(getIdPSSOResponseMethod.getResponseBodyAsStream());
                 LOG.error("Something went wrong with the IdP SAML/Artifact response: "
                         + htmlBody);
                 throw new RemoteException(
                         "The Identity Provider SAML/Artifact response failed: "
                                 + idp.getUrl() + ". Please see the log file.");
             }
             idpResponseURI = getIdPSSOResponseMethod.getURI();
         }
         else {
             // try to parse the Browser/POST profile in the HTML source
             InputStream htmlStream = getIdPSSOResponseMethod.getResponseBodyAsStream();
             idpResponseURI = processIdPBrowserPOST(idp, idpSSOResponseURI, htmlStream);
         }
 
         LOG.debug("getIdPSSOResponseMethod.releaseConnection()");
         getIdPSSOResponseMethod.releaseConnection();
 
         return idpResponseURI;
     }
 
     /**
      * Fully reads an {@link InputStream} into a {@link String}.
      * 
      * @param is
      *            The InputStream to read.
      * @return The context of the input stream.
      */
     private String inputStreamToString(InputStream is) {
         StringBuffer sb = new StringBuffer(1024);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         try {
             char[] chars = new char[1024];
             while (reader.read(chars) > -1) {
                 sb.append(chars);
                 chars = new char[1024];
             }
             reader.close();
         } catch (IOException e) {
             LOG.error(e);
         }
         return sb.toString();
     }
 
     /**
      * Processes the IdP response as a Browser/POST
      * 
      * @param idp
      *            The {@link IdentityProvider}.
      * @param idpSSOResponseURI
      *            The IdP SSO reponse {@link URI}.
      * @return the SP URI to go to
      * @throws RemoteException
      */
     private URI processIdPBrowserPOST(IdentityProvider idp,
             URI idpSSOResponseURI, InputStream htmlStream)
             throws RemoteException {
         // return value
         URI browserPostResponseURI = null;
         RemoteException remoteException = null;
 
         try {
             Source source = new Source(htmlStream);
             List<Element> forms = source.findAllElements(Tag.FORM);
             if (!forms.isEmpty()) {
                 // check if form contains a valid SAML Browser/POST
                 for (Element form : forms) {
                     String spSAMLURL = form.getAttributeValue("ACTION");
                     LOG.debug("SAML Browser/POST URL=" + spSAMLURL);
                     if (spSAMLURL == null) {
                         // no SAML post URL found
                         String htmlBody = inputStreamToString(htmlStream);
                         LOG.error("No SAML Browser/POST FORM ACTION found: "
                                 + idpSSOResponseURI + ": " + htmlBody);
                         remoteException = new RemoteException(
                                 "No SAML Browser/POST FORM ACTION found: "
                                         + idpSSOResponseURI
                                         + ". Please see the log file.");
 
                         break; // exit loop
                     }
 
                     // create POST method
                     PostMethod postSPSAMLMethod = new PostMethod(spSAMLURL);
                     // add all HIDDEN fields to POST
                     List<FormControl> formControls = form.findFormControls();
                     for (FormControl control : formControls) {
                         FormControlType type = control.getFormControlType();
                         if (type.equals(FormControlType.HIDDEN)) {
                             String name = control.getName();
                             Collection<CharSequence> values = control.getValues();
                             for (CharSequence value : values) {
                                 LOG.debug("HIDDEN " + name + "=" + value);
                                 // add all hidden fields
                                 postSPSAMLMethod.addParameter(name,
                                         (String) value);
                             }
                         }
                     }
 
                     // execute the SAML post
                     LOG.info("POST SPSAMLMethod: " + postSPSAMLMethod.getURI());
                     int spSAMLResponseStatus = executeMethod(postSPSAMLMethod);
                     LOG.debug(postSPSAMLMethod.getStatusLine());
 
                     // status must be 302 and redirect Location
                     Header location = postSPSAMLMethod.getResponseHeader("Location");
                     if (spSAMLResponseStatus == 302 && location != null) {
                         String url = location.getValue();
                         browserPostResponseURI = new URI(url, false);
                         LOG.debug("Redirect: " + browserPostResponseURI);
 
                     }
                     else {
                         LOG.error("Unexpected SP response: Status="
                                 + spSAMLResponseStatus + " Location="
                                 + location);
                         remoteException = new RemoteException(
                                 "Unexpected SP response: Status="
                                         + spSAMLResponseStatus + " Location="
                                         + location);
                     }
 
                     LOG.trace("postSPSAMLMethod.releaseConnection()");
                     postSPSAMLMethod.releaseConnection();
 
                 } // forms loop
             }
             else {
                 // no SAML post found
                 String htmlBody = inputStreamToString(htmlStream);
                 LOG.error("No SAML Browser/POST profile found: "
                         + idpSSOResponseURI + ": " + htmlBody);
                 remoteException = new RemoteException(
                         "No SAML Browser/POST profile found: "
                                 + idpSSOResponseURI
                                 + ". Please see the log file.");
             }
 
         } catch (URIException e) {
             e.printStackTrace();
             remoteException = new RemoteException(e.getMessage(), e);
         } catch (IOException e) {
             e.printStackTrace();
             remoteException = new RemoteException(e.getMessage(), e);
         }
 
         if (browserPostResponseURI == null) {
             if (remoteException != null) {
                 throw remoteException;
             }
         }
 
         return browserPostResponseURI;
     }
 
     /**
      * Gets the Shibboleth session needed afterward.
      * 
      * @param entryURL
      * @return
      * @throws URIException
      * @throws HttpException
      * @throws IOException
      * @throws RemoteException
      */
     private URI processSPEntry(String entryURL) throws URIException,
             HttpException, IOException, RemoteException {
         GetMethod getSPEntryMethod = new GetMethod(entryURL);
         LOG.info("GET SPEntryMethod: " + getSPEntryMethod.getURI());
         int spEntryStatus = executeMethod(getSPEntryMethod);
         LOG.debug(getSPEntryMethod.getStatusLine());
 
         URI loginResponseURI = getSPEntryMethod.getURI();
 
         LOG.trace("getSPEntryMethod.releaseConnection()");
         getSPEntryMethod.releaseConnection();
 
         // check response status
         if (spEntryStatus != 200) {
             throw new RemoteException("Unexpected SP response status:"
                     + spEntryStatus + " " + entryURL);
         }
         return loginResponseURI;
     }
 
     /**
      * @param idp
      * @param query
      * @throws URIException
      * @throws HttpException
      * @throws IOException
      * @throws AuthException
      * @throws RemoteException
      * @throws ServiceException
      */
     private URI processIdPSSO(IdentityProvider idp, URI spResponseURI)
             throws URIException, HttpException, IOException, AuthException,
             RemoteException, ServiceException {
         String idpSSOURL = idp.getUrl();
         LOG.debug("IdP SSO URL: " + idpSSOURL);
 
         // create HttpMethod
         GetMethod getIdpSSOMethod = new GetMethod(idpSSOURL);
 
         // BUG FIX: Shib SP 2.0 send the ?service=... in the query
         String query = spResponseURI.getQuery();
         LOG.debug("IdP SSO Query: " + query);
         if (query != null) {
             if (query.startsWith("service=")) {
                 int i = query.indexOf("shire");
                 if (i > 0) {
                     query = query.substring(i);
                     LOG.debug("IdP SSO Query contains the 'service=...' part, cutting up to 'shire=': "
                             + query);
                 }
                 else {
                     LOG.error("IdP SSO Query contains the 'service=' parameter, but not the 'shire=' parameter: "
                             + query);
                     throw new RemoteException(
                             "IdP SSO Query contains the 'service=' parameter, but not the 'shire=' parameter: "
                                     + query);
                 }
             }
             getIdpSSOMethod.setQueryString(query);
             idpSSOURL += "?" + query;
         }
 
         URI idpSSOURI = getIdpSSOMethod.getURI();
         // set credential for basic or ntlm
         int authType = idp.getAuthType();
         LOG.debug("IdP authType: " + idp.getAuthTypeName());
         if (authType == IdentityProvider.SSO_AUTHTYPE_BASIC
                 || authType == IdentityProvider.SSO_AUTHTYPE_NTLM) {
             // enable BASIC or NTLM authN
             AuthScope scope = new AuthScope(idpSSOURI.getHost(),
                     idpSSOURI.getPort(), idp.getAuthRealm());
             LOG.info("Enable " + idp.getAuthTypeName() + " authentication: "
                     + credentials_ + ", scope: " + scope);
             httpClient_.getState().setCredentials(scope, credentials_);
             getIdpSSOMethod.setDoAuthentication(true);
         }
 
         // execute the method
         LOG.info("GET IdpSSOMethod: " + idpSSOURI);
         int idpSSOResponseStatus = executeMethod(getIdpSSOMethod);
         String idpSSOResponseStatusLine = getIdpSSOMethod.getStatusLine().toString();
         LOG.debug(idpSSOResponseStatusLine);
 
         URI idpSSOResponseURI = getIdpSSOMethod.getURI();
         LOG.debug("idpSSOResponseURI=" + idpSSOResponseURI);
         String idpSSOResponseQuery = idpSSOResponseURI.getEscapedQuery();
         LOG.debug("idpSSOResponseQuery=" + idpSSOResponseQuery);
 
         // XXX
         dumpHttpClientCookies();
 
         LOG.debug("idpSSOResponseStatus=" + idpSSOResponseStatus);
         // BASIC or NTLM response handling
         if (idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_BASIC
                 || idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_NTLM) {
             // !200 and same URL as before: if BASIC or NTLM failed on IdP
 
             LOG.debug("idpSSOURI=" + idpSSOURI);
             LOG.debug("idpSSOResponseURI=" + idpSSOResponseURI);
 
             if (idpSSOResponseStatus != 200
                     && idpSSOResponseURI.equals(idpSSOURI)) {
 
                 LOG.trace("getIdpSSOMethod.releaseConnection()");
                 getIdpSSOMethod.releaseConnection();
 
                 LOG.error("BASIC or NTLM authentication was required for "
                         + idpSSOURL);
 
                 throw new AuthException(idp.getAuthTypeName()
                         + " authentication failed: " + idpSSOResponseStatusLine
                         + " URL: " + idpSSOResponseURI + " Credentials: "
                         + this.credentials_);
             }
             else {
                 // SAML/Artifact: idpSSOResponseURI is already SP login and the
                 // XML <SLCSLoginResponse> is already there
                 // XXX: resend to same IdP SSO page once again
                 LOG.info("IdP BASIC or NTLM authN: resend again to the same IdP SSO URI: "
                         + idpSSOURL);
                 idpSSOResponseURI = new URI(idpSSOURL, false);
             }
 
         }
         // CAS sends 200 + Cookies and the login form directly
         else if (idpSSOResponseStatus == 200
                 && (idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS 
                 		|| idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_FORM)) {
             LOG.debug("Process " + idp.getAuthTypeName() + " login form...");
             // process CAS login form
             InputStream idpLoginForm = getIdpSSOMethod.getResponseBodyAsStream();
             LOG.debug("idpSSOURI Query=" + idpSSOURI.getQuery());
             idpSSOResponseURI = processIdPLoginForm(idp, idpSSOResponseURI, idpSSOURI.getQuery(), idpLoginForm);
             LOG.debug(idp.getAuthTypeName() + " idpSSOResponseURI=" + idpSSOResponseURI);
         }
         // FIXME: ETHZ use a new PubCookie
         // PUBCOOKIE sends 200 + onload form with hidden fields to post
         else if (idpSSOResponseStatus == 200
                 && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
 
             // parse <form> and extract hidden fields, then post
             PostMethod postPubcookieFormMethod = null;
             InputStream pubcookieFormStream= getIdpSSOMethod.getResponseBodyAsStream();
             Source source = new Source(pubcookieFormStream);
             List<Element> forms = source.findAllElements(Tag.FORM);
             for (Element form : forms) {
                 String formAction = form.getAttributeValue("ACTION");
                 LOG.debug("PubCookie form action=" + formAction);
                 if (!idp.getAuthUrl().equalsIgnoreCase(formAction)) {
                     // TODO: ERROR
                     throw new RemoteException("form action: " + formAction + " doesn't match IdP authN url: " + idp.getAuthUrl());
                 }
                 // create PubCookie POST
                 postPubcookieFormMethod= new PostMethod(formAction);
 
                 // add all HIDDEN fields to POST
                 List<FormControl> formControls = form.findFormControls();
                 for (FormControl control : formControls) {
                     FormControlType type = control.getFormControlType();
                     if (type.equals(FormControlType.HIDDEN)) {
                         String name = control.getName();
                         Collection<String> values = control.getValues();
                         for (String value : values) {
                             LOG.debug("add hidden: " + name + "=" + value);
                             // add all hidden fields
                             postPubcookieFormMethod.addParameter(name, value);
                         }
                     }
                 }
 
             } // for all forms
             
             LOG.trace("getIdpSSOMethod.releaseConnection()");
             getIdpSSOMethod.releaseConnection();
 
             if (postPubcookieFormMethod == null) {
                 // ERROR
                 LOG.error("PubCookie form not found");
                 throw new RemoteException("PubCookie form not found");
             }
             
             // 
             LOG.debug("POST " + postPubcookieFormMethod.getURI());
             int postPubcookieFormStatus= executeMethod(postPubcookieFormMethod);
             LOG.debug(postPubcookieFormMethod.getStatusLine());
             
             // XXX
             dumpHttpClientCookies();
 
             // process pubcookie login form
             InputStream loginFormStream = postPubcookieFormMethod.getResponseBodyAsStream();
             idpSSOResponseURI = processIdPLoginForm(idp, idpSSOResponseURI, idpSSOURI.getQuery(),
                     loginFormStream);
             LOG.debug("Pubcookie idpSSOResponseURI=" + idpSSOResponseURI);
 
             LOG.trace("postPubcookieFormMethod.releaseConnection()");
             postPubcookieFormMethod.releaseConnection();
 
         }
         else {
             // error handling
             InputStream htmlStream = getIdpSSOMethod.getResponseBodyAsStream();
             String htmlBody = inputStreamToString(htmlStream);
             LOG.error("Unexpected IdP SSO reponse: URL: " + idpSSOResponseURI
                     + " Status: " + idpSSOResponseStatusLine + " AuthType: "
                     + idp.getAuthTypeName() + " HTML:\n" + htmlBody);
             LOG.debug("getIdpSSOMethod.releaseConnection()");
             getIdpSSOMethod.releaseConnection();
             throw new RemoteException("Unexprected IdP SSO reponse: URL: "
                     + idpSSOResponseURI + " Status: "
                     + idpSSOResponseStatusLine + ". Please see the log file.");
         }
 
         LOG.debug("getIdpSSOMethod.releaseConnection()");
         getIdpSSOMethod.releaseConnection();
 
         return idpSSOResponseURI;
 
     }
 
     /**
      * Parses and processes Pubcookie or CAS login form.
      * 
      * @param idp
      * @param htmlForm
      * @throws IOException
      * @throws RemoteException
      * @throws ServiceException
      * @throws AuthException
      */
     private URI processIdPLoginForm(IdentityProvider idp, URI ssoLoginURI, String ssoQuery,
             InputStream htmlForm) throws IOException, RemoteException,
             ServiceException, AuthException {
         LOG.info("Parse and process " + idp.getAuthTypeName()
                 + " login form: " + ssoLoginURI);
 
         boolean formFound = false;
         URI idpLoginFormResponseURI = null;
 
         // Parse the FORM with Jericho HTML Parser
         Source source = new Source(htmlForm);
         List<Element> forms = source.findAllElements(Tag.FORM);
         for (Element form : forms) {
             String formName = form.getAttributeValue("NAME");
             // BUG FIX: UniL use a CAS login form with NO NAME defined.
             // first try with the form ID as NAME, otherwise use an empty name.
             // the metadata should also define an empty name for this particular
             // form.
             LOG.debug("form name= " + formName);
             if (formName == null) {
                 LOG.warn("form have no NAME, try form ID...");
                 String formId = form.getAttributeValue("ID");
                 if (formId == null) {
                     LOG.warn("form have no NAME and no ID, using empty name...");
                     formName = "";
                 }
                 else {
                     formName = formId;
                 }
             }
 
             if (formName.equals(idp.getAuthFormName())) {
                 formFound = true;
                 String formAction = form.getAttributeValue("ACTION");
                 LOG.debug("form action=" + formAction);
                 if (formAction == null || formAction.equals("")) {
                     // no form action to POST, use default from metadata
                     formAction = ssoLoginURI.getEscapedURI();
                     LOG.info("default form action=" + formAction);
                 }
                 else {
                     URI formActionURI= new URI(formAction,false);
                     if (formActionURI.isRelativeURI()) {
                         // action URL is not absolute like: http://localhost/cas/login?...
                         formActionURI= new URI(ssoLoginURI,formActionURI.getPathQuery(),true);
                     }
                     formAction= formActionURI.getEscapedURI();
                     LOG.info("corrected form action=" + formAction);
                 }
                 
                 String formMethod = form.getAttributeValue("METHOD");
                 LOG.debug("form name=" + formName + " action=" + formAction + " method=" + formMethod);
 
                 if (!formAction.equals("")
                         && formMethod.equalsIgnoreCase("POST")) {
 
                     PostMethod postLoginFormMethod = new PostMethod(formAction);
 
                     // add all HIDDEN fields to POST
                     List<FormControl> formControls = form.findFormControls();
                     for (FormControl control : formControls) {
                         FormControlType type = control.getFormControlType();
                         if (type.equals(FormControlType.HIDDEN)) {
                             String name = control.getName();
                             Collection<String> values = control.getValues();
                             for (String value : values) {
                                 LOG.debug("add hidden: " + name + "=" + value);
                                 // add all hidden fields
                                 postLoginFormMethod.addParameter(name, value);
                             }
                         }
                     }
                     // add username field
                     postLoginFormMethod.addParameter(idp.getAuthFormUsername(),
                             this.credentials_.getUserName());
                     // add the PASSWORD field
                     postLoginFormMethod.addParameter(idp.getAuthFormPassword(),
                             this.credentials_.getPassword());
 
                     // XXX
                     //dumpHttpClientCookies();
 
                     // BUG FIX: set the JSESSIONID and IdP 2.X _idp_authn_lc_key 
                     // cookies by hand.
                     // Because default CookiePolicy is RFC2109 and doesn't handle
                     // correctly FQDN hostname as cookie domain.
/*
                     String host = postLoginFormMethod.getURI().getHost();
                     String path = postLoginFormMethod.getURI().getPath();
                     Cookie cookies[] = getMatchingCookies(host, path);
                     for (int j = 0; j < cookies.length; j++) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("setting Cookie: " + cookies[j]);
                         }
                         postLoginFormMethod.addRequestHeader("Cookie", cookies[j].toString());
                     }
*/
                     // execute the login POST
                     LOG.info("POST LoginFormMethod: " + postLoginFormMethod.getURI());
 
                     int formLoginResponseStatus = executeMethod(postLoginFormMethod);
                     LOG.debug(postLoginFormMethod.getStatusLine());
 
                     // XXX
                     dumpHttpClientCookies();
 
                     // CAS, or FORM can, send a 302 + Location header back
                     if (formLoginResponseStatus == 302
                             && (idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS 
                                 || idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_FORM)) {
                         LOG.debug("Process " + idp.getAuthTypeName() + " redirect response (302 + Location header)...");
                         Header location = postLoginFormMethod.getResponseHeader("Location");
                         if (location != null) {
                             String locationURL = location.getValue();
                             LOG.debug("302 Location: " + locationURL);
                             // XXX: if location path (/cas/login) is not the IdP
                             // SSO path (/shibboleth-idp/SSO), then it's a wrong
                             // login
                             URI locationURI = new URI(locationURL, false);
                             String locationPath = locationURI.getPath();
                             String idpSSOURL = idp.getUrl();
                             URI idpSSOURI = new URI(idpSSOURL, false);
                             String idpSSOPath = idpSSOURI.getPath();
                             if (LOG.isDebugEnabled()) {
                                 LOG.debug("location path: " + locationPath);
                                 LOG.debug("IdP SSO path: " + idpSSOPath);
                             }
                             if (!locationPath.equals(idpSSOPath)) {
                                 LOG.error("Redirect response is not the SSO url ("
                                         + idpSSOURL + "): " + locationURL);
                                 throw new AuthException(idp.getAuthTypeName()
                                         + " Authentication failed: "
                                         + this.credentials_);
                             }
                             idpLoginFormResponseURI = new URI(locationURL,false);
                             LOG.debug("(" + idp.getAuthTypeName() + ": 302 + Location) idpLoginFormReponseURI= " + idpLoginFormResponseURI);
                         }
                         else {
                             LOG.error( idp.getAuthTypeName() + ": Status 302 but no redirect Location header");
                             throw new AuthException(idp.getAuthTypeName()
                                     + " Authentication failed: "
                                     + this.credentials_);
                         }
                     }
                     // IdP 2.1 FORM authN send 200 and directly the SAMLResponse form
                     else if (formLoginResponseStatus == 200
                             && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_FORM) {
                         // BUG FIX: check for Browser/POST hidden form element SAMLResponse for valid authentication
                         LOG.debug("check for SAMLResponse hidden element");
                         boolean samlResponseFound= false;
                         InputStream authnLoginResponse = postLoginFormMethod.getResponseBodyAsStream();
                         Source authnSource = new Source(authnLoginResponse);
                         List<Element> browserPOSTForms = authnSource.findAllElements(Tag.FORM);
                         for (Element browserPOSTForm : browserPOSTForms) {
                             List<FormControl> browserPOSTFormControls = browserPOSTForm.findFormControls();
                             for (FormControl control : browserPOSTFormControls) {
                                 FormControlType type = control.getFormControlType();
                                 if (type.equals(FormControlType.HIDDEN)) {
                                     String name = control.getName();
                                     if (name.equals("SAMLResponse")) {
                                         LOG.debug("Hidden element found: " + control.getName());
                                         samlResponseFound= true;
                                     }
                                 }
                             }
                         }
                         if (!samlResponseFound) {
                             LOG.error( idp.getAuthTypeName() + ": no Browser/POST SAMLResponse hidden element found");
                             throw new AuthException(idp.getAuthTypeName()
                                     + " Authentication failed: "
                                     + this.credentials_);
 
                         }
                         
                         LOG.debug("Process FORM (200 + full Browser/POST profile) response...");
                         idpLoginFormResponseURI= new URI(idp.getUrl(),false);
                         // re-set the original SSO query params
                         idpLoginFormResponseURI.setQuery(ssoQuery);
                         LOG.debug("(FORM: 200 + Browser/POST) idpLoginFormReponseURI= " + idpLoginFormResponseURI);                        
                     }
                     // Pubcookie send 200 + fucking HTML form relay with hidden fields!!!
                     // <form method=post action="https://aai-login.ethz.ch/PubCookie.reply" name=relay>
                     // then reply a redirect 302 + Location header
                     else if (formLoginResponseStatus == 200
                             && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
                         LOG.debug("Process Pubcookie (200 + relay FORM) response...");
                         InputStream pubcookieLoginResponse = postLoginFormMethod.getResponseBodyAsStream();
                         Source pubcookieSource = new Source(pubcookieLoginResponse);
                         PostMethod postPubcookieRelayMethod= null;
                         List<Element> relayForms = pubcookieSource.findAllElements(Tag.FORM);
                         for (Element relayForm : relayForms) {
                             String relayFormAction = relayForm.getAttributeValue("ACTION");
                             LOG.debug("Pubcookie relay form action= " + relayFormAction);
                             if (relayFormAction == null) {
                                 LOG.error("Pubcookie relay form action not found.");
                                 throw new RemoteException("Pubcookie relay form action not found");
                             }
                             // create PubCookie relay POST
                             postPubcookieRelayMethod= new PostMethod(relayFormAction);
 
                             // add all HIDDEN fields to POST
                             List<FormControl> relayFormControls = relayForm.findFormControls();
                             for (FormControl control : relayFormControls) {
                                 FormControlType type = control.getFormControlType();
                                 if (type.equals(FormControlType.HIDDEN)) {
                                     String name = control.getName();
                                     Collection<String> values = control.getValues();
                                     for (String value : values) {
                                         LOG.debug("add hidden: " + name + "=" + value);
                                         // add all hidden fields
                                         postPubcookieRelayMethod.addParameter(name, value);
                                     }
                                 }
                             }  // add hidden fields
                         } // for all relay forms
 
                         if (postPubcookieRelayMethod != null) {
                             LOG.debug("POST postPubcookieRelayMethod: " + postPubcookieRelayMethod.getURI());
                             int pubcookieRelayStatus= executeMethod(postPubcookieRelayMethod);
                             LOG.debug(postPubcookieRelayMethod.getStatusLine());
                             Header location = postPubcookieRelayMethod.getResponseHeader("Location");
                             LOG.debug("postPubcookieRelayMethod.releaseConnection()");
                             postPubcookieRelayMethod.releaseConnection();
                             if (location != null) {
                                 String locationURL = location.getValue();
                                 LOG.debug("302 Location: " + locationURL);
                                 // parse Location
                                 idpLoginFormResponseURI = new URI(locationURL,false);
                                 LOG.debug("(PubCookie: 302 + Location header) idpLoginFormReponseURI= " + idpLoginFormResponseURI);
                             }
                             else {
                                 LOG.error("Pubcookie relay response 302 + Location header not found");
                                 throw new AuthException(idp.getAuthTypeName()
                                                         + " Authentication failed: "
                                                         + this.credentials_);
                             }
                         }
                         else {
                             LOG.error("Pubcookie relay form not found");
                             throw new AuthException(idp.getAuthTypeName()
                                     + " Authentication failed: "
                                     + this.credentials_);
                         }
 
                         // XXX
                         dumpHttpClientCookies();
                     }
                     else {
                         LOG.error("Unexpected response status: "
                                 + formLoginResponseStatus + " AuthType:"
                                 + idp.getAuthTypeName());
                         throw new AuthException(idp.getAuthTypeName()
                                 + " Authentication failed: "
                                 + this.credentials_);
                     }
 
                     LOG.debug("POSTLoginFormMethod.releaseConnection()");
                     postLoginFormMethod.releaseConnection();
                     
                 } // end if form action is set and method is POST 
             } // end if form name match metadata
         } // end for all forms
 
         if (!formFound) {
             LOG.error("FORM name=" + idp.getAuthFormName() + " not found");
             throw new ServiceException("FORM name=" + idp.getAuthFormName()
                     + " not found");
         }
 
         return idpLoginFormResponseURI;
 
     }
 
     /**
      * @return isAuthenticated
      */
     public boolean isAuthenticated() {
         return this.isAuthenticated_;
     }
 
     /**
      * Returns <code>true</code> if the url contains a SAML/Artifact service
      * endpoint.
      * 
      * @param url
      *            The url (with query parameter) to check.
      * @return <code>true</code> if the url contains a SAML/Artifact service
      *         url.
      */
     private boolean matchesSAMLArtifact(String url) {
         if (url.indexOf("/SAML/Artifact") != -1) {
             return true;
         }
         else if (url.indexOf("/SAML2/Artifact") != -1) {
             return true;
         }
         return false;
     }
 
     /**
      * Delegates execution of the HttpMethod to the underlying HttpClient.
      * 
      * @param method
      *            The HttpMethod to execute.
      * @return The method's response code.
      * @throws HttpException
      *             If an I/O (transport) error occurs.
      * @throws IOException
      *             If a protocol exception occurs.
      */
     public int executeMethod(HttpMethod method) throws HttpException,
             IOException {
         if (LOG.isTraceEnabled())
             LOG.trace("exec: " + method.getName() + " " + method.getURI());
         // use delegate
         return this.httpClient_.executeMethod(method);
     }
 
     private void dumpHttpClientCookies() {
         if (LOG.isDebugEnabled()) {
             Cookie[] cookies = this.httpClient_.getState().getCookies();
             StringBuffer sb = new StringBuffer();
             sb.append("\n");
             sb.append("---[CookiePolicy=").append(
                     httpClient_.getParams().getCookiePolicy()).append("]---\n");
             for (int i = 0; i < cookies.length; i++) {
                 Cookie cookie= cookies[i];
                 String path = cookie.getPath();
                 String domain = cookie.getDomain();
                 boolean secure = cookie.getSecure();
                 int version= cookie.getVersion();
                 // sb.append(name).append('=').append(value).append("\n");
                 sb.append(i).append(": ").append(cookie);
                 sb.append(" domain:").append(domain);
                 sb.append(" path:").append(path);
                 sb.append(" secure:").append(secure);
                 sb.append(" version:").append(version);
                 sb.append("\n");
             }
             sb.append("---[End]---");
             LOG.debug(sb.toString());
         }
     }
 
     /**
      * Returns the <b>first</b> Cookie or <code>null</code> if not exists.
      * 
      * @param name
      *            The Cookie name
      * @return The cookie or <code>null</code>
      */
     private Cookie getCookie(String name) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Cookie name=" + name);
         }
         Cookie[] cookies = this.httpClient_.getState().getCookies();
         for (int i = 0; i < cookies.length; i++) {
             String cookieName = cookies[i].getName();
             if (name.equals(cookieName)) {
                 return cookies[i];
             }
         }
         return null;
     }
     
     private Cookie[] getMatchingCookies(String host, String path) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("search all Cookies matching for host:" + host
                     + " path:" + path);
         }
         List<Cookie> matchingCookies = new ArrayList<Cookie>();
         Cookie[] cookies = this.httpClient_.getState().getCookies();
         CookieSpecBase cookieSpecBase = new CookieSpecBase();
         for (int i = 0; i < cookies.length; i++) {
             Cookie cookie = cookies[i];
             if (cookieSpecBase.match(host, 443, path, true, cookie)) {
                 LOG.debug("Cookie " + cookie + " matched");
                 matchingCookies.add(cookie);
             }
         }
         return (Cookie[]) matchingCookies.toArray(new Cookie[matchingCookies.size()]);
     }
     
 
     private Cookie[] getMatchingCookies(String name, String host, String path) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("search Cookie matching name:" + name + " host:" + host
                     + " path:" + path);
         }
         List<Cookie> matchingCookies = new ArrayList<Cookie>();
         Cookie[] cookies = this.httpClient_.getState().getCookies();
         CookieSpecBase cookieSpecBase = new CookieSpecBase();
         for (int i = 0; i < cookies.length; i++) {
             Cookie cookie = cookies[i];
             if (cookieSpecBase.match(host, 443, path, true, cookie)) {
                 if (cookie.getName().equals(name)) {
                     LOG.debug("Cookie " + cookie + " matched");
                     matchingCookies.add(cookie);
                 }
             }
         }
         return (Cookie[]) matchingCookies.toArray(new Cookie[matchingCookies.size()]);
     }
 
 }
