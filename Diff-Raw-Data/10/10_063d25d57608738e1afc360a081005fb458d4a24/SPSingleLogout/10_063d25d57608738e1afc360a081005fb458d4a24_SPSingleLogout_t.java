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
 * $Id: SPSingleLogout.java,v 1.5 2007-03-23 22:26:17 bina Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.saml2.profile;
 
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.saml2.common.SAML2Exception;
 import com.sun.identity.saml2.common.SAML2Utils;
 import com.sun.identity.saml2.common.NameIDInfoKey;
 import com.sun.identity.saml2.common.AccountUtils;
 import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.entityconfig.IDPSSOConfigElement;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import com.sun.identity.saml2.logging.LogUtil;
 import com.sun.identity.saml2.assertion.AssertionFactory;
 import com.sun.identity.saml2.assertion.Issuer;
 import com.sun.identity.saml2.assertion.NameID;
 import com.sun.identity.saml2.protocol.ProtocolFactory;
 import com.sun.identity.saml2.protocol.LogoutResponse;
 import com.sun.identity.saml2.protocol.LogoutRequest;
 import com.sun.identity.saml2.protocol.Status;
 import com.sun.identity.plugin.session.SessionManager;
 import com.sun.identity.plugin.session.SessionProvider;
 import com.sun.identity.plugin.session.SessionException;
 import com.sun.identity.shared.debug.Debug;
 
 
 import java.security.AccessController;
 import java.util.logging.Level;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * This class reads the required data from HttpServletRequest and
  * initiates the <code>LogoutRequest</code> from SP to IDP.
  */
 
 public class SPSingleLogout {
 
     static SAML2MetaManager sm = null;
     static AssertionFactory af = AssertionFactory.getInstance();
     static Debug debug = SAML2Utils.debug;
     static final Status SUCCESS_STATUS =
             SAML2Utils.generateStatus(SAML2Constants.STATUS_SUCCESS,
                                 SAML2Utils.bundle.getString("requestSuccess"));
     static final Status PARTIAL_LOGOUT_STATUS =
             SAML2Utils.generateStatus(SAML2Constants.RESPONDER_ERROR,
                                 SAML2Utils.bundle.getString("partialLogout"));
     static SessionProvider sessionProvider = null;
    
     static {
         try {
             sm = new SAML2MetaManager();
         } catch (SAML2MetaException sme) {
             debug.error("Error retrieving metadata.", sme);
         }
         try {
             sessionProvider = SessionManager.getProvider();
         } catch (SessionException se) {
             debug.error("Error retrieving session provider.", se);
         }
     }
 
     /**
      * Parses the request parameters and initiates the Logout
      * Request to be sent to the IDP.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param binding binding used for this request.
      * @param paramsMap Map of all other parameters.
      *       Following parameters names with their respective
      *       String values are allowed in this paramsMap.
      *       "RelayState" - the target URL on successful Single Logout
      *       "Destination" - A URI Reference indicating the address to
      *                       which the request has been sent.
      *       "Consent" - Specifies a URI a SAML defined identifier
      *                   known as Consent Identifiers.
      *       "Extension" - Specifies a list of Extensions as list of
      *                   String objects.
      * @throws SAML2Exception if error initiating request to IDP.
      */
     public static void initiateLogoutRequest(HttpServletRequest request,
         HttpServletResponse response,
         String binding,
         Map paramsMap)
     throws SAML2Exception {
 
         if (debug.messageEnabled()) {
             debug.message("SPSingleLogout:initiateLogoutRequest");
             debug.message("binding : " + binding);
             debug.message("paramsMap : " + paramsMap);
         }
 
         String metaAlias = (String)paramsMap.get(SAML2Constants.SP_METAALIAS);
         try {
             Object session = sessionProvider.getSession(request);
             if (session == null) {
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("nullSSOToken"));
             }
             if (metaAlias == null) {
                 String[] values =
                     sessionProvider.getProperty(
                         session, SAML2Constants.SP_METAALIAS);
                 if (values != null && values.length > 0) {
                     metaAlias = values[0];
                 }
             }
             
             if (metaAlias == null) {
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("nullSPMetaAlias"));
             }
             
             paramsMap.put(SAML2Constants.METAALIAS, metaAlias);
             String realm = SAML2Utils.
             getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));
             debug.message("realm : " + realm);
             String spEntityID = sm.getEntityByMetaAlias(metaAlias);
             if (spEntityID == null) {
                 debug.error("Service Provider ID is missing");
                 String[] data = {spEntityID};
                 LogUtil.error(
                     Level.INFO,LogUtil.INVALID_SP,data,null);
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("nullSPEntityID"));
             }
             debug.message("spEntityID : " + spEntityID);
 
             // clean up session index
             String tokenID = sessionProvider.getSessionID(session);
             String infoKeyString = null;            
             try {
                 String[] values = sessionProvider.getProperty(
                     session, AccountUtils.getNameIDInfoKeyAttribute());
                 if (values != null && values.length > 0) {
                     infoKeyString = values[0];
                 }
             } catch (SessionException se) {
                 debug.error("Unable to get infoKeyString from " +
                     "session.", se);
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("errorInfoKeyString"));
             }
             
             if (infoKeyString == null) {
                 debug.error("Unable to get infoKeyString from " +
                     "session.");
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("errorInfoKeyString"));
             }
             if (debug.messageEnabled()) {
                 debug.message("tokenID : " + tokenID);
                 debug.message("infoKeyString : " + infoKeyString);
             }
 
             // get SPSSODescriptor
             SPSSODescriptorElement spsso =
                 sm.getSPSSODescriptor(realm,spEntityID);
 
             if (spsso == null) {
                 String[] data = {spEntityID};
                 LogUtil.error(Level.INFO,LogUtil.SP_METADATA_ERROR,data,
                     null);
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("metaDataError"));
             }
             List extensionsList = LogoutUtil.getExtensionsList(paramsMap);
 
             String relayState = SAML2Utils.getParameter(paramsMap,
                 SAML2Constants.RELAY_STATE);
 
             StringTokenizer st =
                 new StringTokenizer(infoKeyString,SAML2Constants.SECOND_DELIM);
             if (st != null && st.hasMoreTokens()) {
                 while (st.hasMoreTokens()) {
                     String tmpInfoKeyString = (String)st.nextToken();
                     prepareForLogout(realm,tokenID,metaAlias,extensionsList,
                         binding,relayState,response,
                         paramsMap,tmpInfoKeyString);
                 }
             }
            // local log out
            sessionProvider.invalidateSession(session, request, response);
         } catch (SAML2MetaException sme) {
             debug.error("Error retreiving metadata",sme);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         } catch (SessionException ssoe) {
             debug.error("Session exception: ",ssoe);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         }
     }
 
     private static void prepareForLogout(String realm,
         String tokenID,
         String metaAlias,
         List extensionsList,
         String binding,
         String relayState,
         HttpServletResponse response,
         Map paramsMap,
         String infoKeyString) throws SAML2Exception, SessionException {
 
         SPFedSession fedSession = null;
         
         List list =
             (List)SPCache.fedSessionListsByNameIDInfoKey.get(infoKeyString);
         if (list != null) {
             synchronized (list) {
                 ListIterator iter = list.listIterator();
                 while (iter.hasNext()) {
                     fedSession = (SPFedSession)iter.next();
                     if (tokenID.equals(fedSession.spTokenID)) {
                         iter.remove();
                         if (list.size() == 0) {
                             SPCache.fedSessionListsByNameIDInfoKey.
                                 remove(infoKeyString);
                         }
                         break;
                     }
                     fedSession = null;
                 }
            }   
         }
         NameIDInfoKey nameIdInfoKey = NameIDInfoKey.parse(infoKeyString);
 
         if (fedSession == null) {
             // just do local logout
             if (debug.messageEnabled()) {
                 debug.message(
                     "No session partner, just do local logout.");
             }
             return;
         }
 
         // get IDPSSODescriptor
         IDPSSODescriptorElement idpsso =
             sm.getIDPSSODescriptor(realm,nameIdInfoKey.getRemoteEntityID());
 
         if (idpsso == null) {
             String[] data = {nameIdInfoKey.getRemoteEntityID()};
             LogUtil.error(Level.INFO,LogUtil.IDP_METADATA_ERROR,data,
                 null);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         }
 
         List slosList = idpsso.getSingleLogoutService();
         if (slosList == null) {
             String[] data = {nameIdInfoKey.getRemoteEntityID()};
             LogUtil.error(Level.INFO,LogUtil.SLO_NOT_FOUND,data,
                 null);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("sloServiceListNotfound"));
         }
         // get IDP entity config in case of SOAP, for basic auth info
         IDPSSOConfigElement idpConfig = null;
         if (binding.equals(SAML2Constants.SOAP)) {
             idpConfig = sm.getIDPSSOConfig(
                 realm,
                 nameIdInfoKey.getRemoteEntityID()
             );
         }
         
         StringBuffer requestID = LogoutUtil.doLogout(
             metaAlias,
             nameIdInfoKey.getRemoteEntityID(),
             slosList,
             extensionsList,
             binding,
             relayState,
             fedSession.idpSessionIndex,
             fedSession.info.getNameID(),
             response,
             paramsMap,
             idpConfig);
 
         String requestIDStr = requestID.toString();
         if (debug.messageEnabled()) {
             debug.message(
                 "\nSPSLO.requestIDStr = " + requestIDStr +
                 "\nbinding = " + binding);
         }
 
         if (requestIDStr != null &&
             requestIDStr.length() != 0 &&
             binding.equals(SAML2Constants.HTTP_REDIRECT)) {
             SPCache.logoutRequestIDs.add(requestIDStr);
         }
     }
 
     /**
      * Gets and processes the Single <code>LogoutResponse</code> from IDP,
      * destroys the local session, checks response's issuer
      * and inResponseTo.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param samlResponse <code>LogoutResponse</code> in the
      *          XML string format.
      * @param relayState the target URL on successful
      * <code>LogoutResponse</code>.
      * @throws SAML2Exception if error processing
      *          <code>LogoutResponse</code>.
      * @throws SessionException if error processing
      *          <code>LogoutResponse</code>.
      */
     public static void processLogoutResponse(
         HttpServletRequest request,
         HttpServletResponse response,
         String samlResponse,
         String relayState) throws SAML2Exception, SessionException  {
         String method = "processLogoutResponse : ";
         if (debug.messageEnabled()) {
             debug.message(method + "samlResponse : " + samlResponse);
             debug.message(method + "relayState : " + relayState);
         }
 
         String decodedStr =
             SAML2Utils.decodeFromRedirect(samlResponse);
         if (decodedStr == null) {
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString(
                 "nullDecodedStrFromSamlResponse"));
         }
         
         LogoutResponse logoutRes = 
             ProtocolFactory.getInstance().createLogoutResponse(decodedStr);
         String metaAlias =
                 SAML2MetaUtils.getMetaAliasByUri(request.getRequestURI()) ;
         String realm = SAML2Utils.
                 getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));
         String spEntityID = sm.getEntityByMetaAlias(metaAlias);
         String idpEntityID = logoutRes.getIssuer().getValue();
         Issuer resIssuer = logoutRes.getIssuer();
         String requestId = logoutRes.getInResponseTo();
         SAML2Utils.verifyResponseIssuer(
                             realm, spEntityID, resIssuer, requestId);
         boolean needToVerify = 
              SAML2Utils.getWantLogoutResponseSigned(realm, spEntityID, 
                              SAML2Constants.SP_ROLE);
         if (debug.messageEnabled()) {
             debug.message(method + "metaAlias : " + metaAlias);
             debug.message(method + "realm : " + realm);
             debug.message(method + "idpEntityID : " + idpEntityID);
             debug.message(method + "spEntityID : " + spEntityID);
         }
         
         if (needToVerify == true) {
             String queryString = request.getQueryString();
             boolean valid = SAML2Utils.verifyQueryString(queryString, realm,
                             SAML2Constants.SP_ROLE, idpEntityID);
             if (valid == false) {
                 debug.error("Invalid signature in SLO Response.");
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("invalidSignInResponse"));
             }
             SPSSODescriptorElement spsso =
                 sm.getSPSSODescriptor(realm, spEntityID);
             String loc = getSLOResponseLocationOrLocation(spsso); 
             if (!SAML2Utils.verifyDestination(logoutRes.getDestination(),
                 loc)) {
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("invalidDestination"));
             }    
         }
 
         String inResponseTo = logoutRes.getInResponseTo();
         if (inResponseTo == null ||
             inResponseTo.length() == 0) {
             if (debug.messageEnabled()) {
                 debug.message(
                     "LogoutResponse inResponseTo is null");
             }
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString(
                 "nullInResponseToFromSamlResponse"));
         }
 
         if (SPCache.logoutRequestIDs.remove(inResponseTo)) {
             if (debug.messageEnabled()) {
                 debug.message(
                     "LogoutResponse inResponseTo matches "+
                     "LogoutRequest ID.");
             }
         } else {
             if (debug.messageEnabled()) {
                 debug.message(
                     "LogoutResponse inResponseTo does not match " +
                     "LogoutRequest ID.");
             }
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString(
                 "LogoutRequestIDandInResponseToDoNotMatch"));
         }
 
         // if relay state is present, redirect to
         // relay state
         if (relayState != null && relayState.length() != 0) {
             try {
                 response.sendRedirect(relayState);
             } catch (java.io.IOException ioe) {
                 debug.message(
                     "Exception when redirecting to " +
                     relayState, ioe);
             }
         }
     }
 
     /**
      * Gets and processes the Single <code>LogoutRequest</code> from IDP.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param samlRequest <code>LogoutRequest</code> in the
      *          XML string format.
      * @param relayState the target URL on successful
      * <code>LogoutRequest</code>.
      * @throws SAML2Exception if error processing
      *          <code>LogoutRequest</code>.
      * @throws SessionException if error processing
      *          <code>LogoutRequest</code>.
      */
     public static void processLogoutRequest(
         HttpServletRequest request,
         HttpServletResponse response,
         String samlRequest,
         String relayState) throws SAML2Exception, SessionException {
         String method = "processLogoutRequest : ";
         if (debug.messageEnabled()) {
             debug.message(method + "samlRequest : " + samlRequest);
             debug.message(method + "relayState : " + relayState);
         }
         String decodedStr = SAML2Utils.decodeFromRedirect(samlRequest);
         if (decodedStr == null) {
             throw new SAML2Exception(SAML2Utils.bundle.getString(
                 "nullDecodedStrFromSamlRequest"));
         }
         LogoutRequest logoutReq = 
             ProtocolFactory.getInstance().createLogoutRequest(decodedStr);
         String metaAlias =
             SAML2MetaUtils.getMetaAliasByUri(request.getRequestURI()) ;
         String realm = 
             SAML2Utils.getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));
         String spEntityID = sm.getEntityByMetaAlias(metaAlias);
         String location = null;
         String idpEntityID = logoutReq.getIssuer().getValue();
 
         boolean needToVerify = 
             SAML2Utils.getWantLogoutRequestSigned(realm, spEntityID, 
                             SAML2Constants.SP_ROLE);
         if (debug.messageEnabled()) {
                 debug.message(method + "metaAlias : " + metaAlias);
                 debug.message(method + "realm : " + realm);
                 debug.message(method + "idpEntityID : " + idpEntityID);
                 debug.message(method + "spEntityID : " + spEntityID);
         }
         
         if (needToVerify == true) {
             String queryString = request.getQueryString();
             boolean valid = 
                         SAML2Utils.verifyQueryString(queryString, realm,
                                     SAML2Constants.SP_ROLE, idpEntityID);
             if (valid == false) {
                     debug.error("Invalid signature in SLO Request.");
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("invalidSignInRequest"));
             }
             SPSSODescriptorElement spsso =
                 sm.getSPSSODescriptor(realm, spEntityID);
             String loc = getSLOResponseLocationOrLocation(spsso);
             if (!SAML2Utils.verifyDestination(logoutReq.getDestination(),
                 loc)) {
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("invalidDestination"));
             } 
         }
         
         // get IDPSSODescriptor
         IDPSSODescriptorElement idpsso =
             sm.getIDPSSODescriptor(realm,idpEntityID);
         
         if (idpsso == null) {
             String[] data = {idpEntityID};
             LogUtil.error(Level.INFO,LogUtil.IDP_METADATA_ERROR,data,
                           null);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         }
         
         List slosList = idpsso.getSingleLogoutService();
         if (slosList == null) {
             String[] data = {idpEntityID};
             LogUtil.error(Level.INFO,LogUtil.SLO_NOT_FOUND,data,
                           null);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("sloServiceListNotfound"));
         }
         
         location =
             LogoutUtil.getSLOResponseServiceLocation(
                 slosList,
                 SAML2Constants.HTTP_REDIRECT);
         if (location == null || location.length() == 0) {
             location = LogoutUtil.getSLOServiceLocation(
                 slosList,
                 SAML2Constants.HTTP_REDIRECT);
             if (location == null || location.length() == 0) {
                 debug.error(
                     "Unable to find the IDP's single logout "+
                     "response service with the HTTP-Redirect binding");
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString(
                         "sloResponseServiceLocationNotfound"));
             } else {
                 if (debug.messageEnabled()) {
                     debug.message(
                         "SP's single logout response service location = "+
                         location);
                 }
             }
         } else {
             if (debug.messageEnabled()) {
                 debug.message(
                     "IDP's single logout response service location = "+
                     location);
             }
         }
 
         LogoutResponse logoutRes =
             processLogoutRequest(logoutReq, spEntityID, realm,
                                  request, response, false);
         logoutRes.setDestination(location);
         LogoutUtil.sendSLOResponse(response, logoutRes, location, relayState, 
                 realm, spEntityID, SAML2Constants.SP_ROLE, idpEntityID);
     }
 
     /**
      * Gets and processes the Single <code>LogoutRequest</code> from IDP
      * and return <code>LogoutResponse</code>.
      *
      * @param logoutReq <code>LogoutRequest</code> from IDP
      * @param spEntityID name of host entity ID.
      * @param realm name of host entity.
      * @param request HTTP servlet request.
      * @param request HTTP servlet response.
      * @param isLBReq true if the request is for load balancing.
      * @return LogoutResponse the target URL on successful
      * <code>LogoutRequest</code>.
      */
     public static LogoutResponse processLogoutRequest(
         LogoutRequest logoutReq, String spEntityID, String realm,
         HttpServletRequest request, HttpServletResponse response,
         boolean isLBReq) {
         final String method = "processLogoutRequest : "; 
         NameID nameID = null;
         Status status = null;
         Issuer issuer = null;
         String idpEntity = logoutReq.getIssuer().getValue();
         
         try {
             do {
                  // TODO: check the NotOnOrAfter attribute of LogoutRequest
                 issuer = logoutReq.getIssuer();
                 String requestId = logoutReq.getID();
                 SAML2Utils.verifyRequestIssuer(
                                     realm, spEntityID, issuer, requestId);
                     
                 issuer = SAML2Utils.createIssuer(spEntityID);
                 // get SessionIndex and NameID form LogoutRequest
                 List siList = logoutReq.getSessionIndex();
                 int numSI = 0;
                 if (siList != null) {
                     numSI = siList.size();
                     if (debug.messageEnabled()) {
                         debug.message(method +
                         "Number of session indices in the logout request is "
                         + numSI);
                     }
                 }
             
                 nameID = LogoutUtil.getNameIDFromSLORequest(logoutReq, realm, 
                                 spEntityID, SAML2Constants.SP_ROLE);
                 
                 if (nameID == null) {
                     debug.error(method +
                                     "LogoutRequest does not contain Name ID");
                     status = SAML2Utils.generateStatus(
                             SAML2Constants.RESPONDER_ERROR, 
                             SAML2Utils.bundle.
                             getString("missing_name_identifier"));
                     break;
                 }
 
                 String infoKeyString = null; 
                 infoKeyString = (new NameIDInfoKey(nameID.getValue(), 
                                          nameID.getSPNameQualifier(), 
                          nameID.getNameQualifier())).toValueString(); 
                 if (debug.messageEnabled()) {
                     debug.message(method + "infokey=" + infoKeyString);
                 }
                 List list = (List)SPCache.fedSessionListsByNameIDInfoKey
                                          .get(infoKeyString);
                 if (debug.messageEnabled()) {
                     debug.message(method + "SPFedsessions=" + list);
                 }
 
                 boolean foundPeer = false;
                 List remoteServiceURLs = null;
                 if (isLBReq) {
                     remoteServiceURLs =SAML2Utils.getRemoteServiceURLs(request);
                     foundPeer = remoteServiceURLs != null &&
                                 !remoteServiceURLs.isEmpty();
                 }
 
                 if (debug.messageEnabled()) {
                     debug.message(method + "isLBReq = " + isLBReq +
                                  ", foundPeer = " + foundPeer);
                 }
 
                 if (list == null || list.isEmpty()) {
                     if (foundPeer) {
                         boolean peerError = false;
                         LogoutRequest lReq = copyAndMakeMutable(logoutReq);
                         for(Iterator iter = remoteServiceURLs.iterator();
                             iter.hasNext();) {
 
                             String remoteLogoutURL = ((String)iter.next()) +
                                         request.getRequestURI() +
                                        (request.getQueryString() == null ?
                                          "?" : "&") + "isLBReq=false";
                             LogoutResponse logoutRes =
                                 LogoutUtil.forwardToRemoteServer(lReq,
                                                               remoteLogoutURL);
                             if (!isNameNotFound(logoutRes)) {
                                 if (isSuccess(logoutRes)) {
                                     if (numSI > 0) {
                                        siList =
                                          LogoutUtil.getSessionIndex(logoutRes);
                                        if (siList == null || siList.isEmpty()){
                                            peerError = false;
                                            break;
                                        }
                                        lReq.setSessionIndex(siList);
                                     }
                                 } else { 
                                     peerError = true;
                                 }
                             }
 
                         }
                         if (peerError ||
                             (siList != null && siList.size() > 0)) {
                             status = PARTIAL_LOGOUT_STATUS;
                         } else {
                             status = SUCCESS_STATUS;
                         }
                     } else {
                         debug.error(method + "invalid Name ID received");
                         status = SAML2Utils
                                .generateStatus(SAML2Constants.RESPONDER_ERROR, 
                                 SAML2Utils.bundle
                                         .getString("invalid_name_identifier"));
                     }
                     break;
                 }
 
                 if (numSI == 0) {
                     // logout all fed sessions for this user
                     // between this SP and the IDP
                     List tokenIDsToBeDestroyed = new ArrayList();
                     synchronized (list) {
                         Iterator iter = list.listIterator();
                         while (iter.hasNext()) {
                             SPFedSession fedSession =(SPFedSession) iter.next();
                             tokenIDsToBeDestroyed.add(fedSession.spTokenID);
                             iter.remove();
                         }
                     }
                     
                     for (Iterator iter = tokenIDsToBeDestroyed.listIterator();
                         iter.hasNext();) {                          
                         String tokenID =(String) iter.next();
                         Object token = null; 
                         try {
                             token = sessionProvider.getSession(tokenID);
                         } catch (SessionException se) {
                             debug.error(method
                                 + "Could not create session from token ID = " +
                                 tokenID);
                             continue;    
                         }
                         if (debug.messageEnabled()) {
                             debug.message(method
                                 + "destroy token " + tokenID);
                         }
                         sessionProvider.invalidateSession(token, request,
                             response);
                     }
                     if (foundPeer) {
                         boolean peerError = false;
                         for(Iterator iter = remoteServiceURLs.iterator();
                             iter.hasNext();) {
 
                             String remoteLogoutURL = ((String)iter.next()) +
                                         request.getRequestURI() +
                                        (request.getQueryString() == null ?
                                          "?" : "&") + "isLBReq=false";
                             LogoutResponse logoutRes =
                                     LogoutUtil.forwardToRemoteServer(logoutReq,
                                                               remoteLogoutURL);
                             if (!(isSuccess(logoutRes) ||
                                   isNameNotFound(logoutRes))) {
                                 peerError = true;
                             }
                         }
                         if (peerError) {
                             status = PARTIAL_LOGOUT_STATUS;
                         } else {
                             status = SUCCESS_STATUS;
                         }
                      }
                 } else {
                     // logout only those fed sessions specified
                     // in logout request session list
                     String sessionIndex = null;
                     List siNotFound = new ArrayList();
                     for (int i = 0; i < numSI; i++) {
                         sessionIndex = (String)siList.get(i);
                        
                         String tokenIDToBeDestroyed = null;
                         synchronized (list) {
                             Iterator iter = list.listIterator();
                             while (iter.hasNext()) {
                                 SPFedSession fedSession = 
                                     (SPFedSession) iter.next();
                                 if (sessionIndex
                                           .equals(fedSession.idpSessionIndex)) {
                                     if (debug.messageEnabled()) {
                                         debug.message(method + " found si + " +
                                             sessionIndex);
                                     }
                                     tokenIDToBeDestroyed = fedSession.spTokenID;
                                     iter.remove();
                                     break;
                                 }
                             }   
                         }
                         
                         if (tokenIDToBeDestroyed != null) {      
                             try {
                                  Object token = sessionProvider.getSession(
                                         tokenIDToBeDestroyed);
                                  if (debug.messageEnabled()) {
                                      debug.message(method 
                                          + "destroy token (2) " 
                                          + tokenIDToBeDestroyed);
                                  }
                                  sessionProvider.invalidateSession(
                                     token, request, response);
                             } catch (SessionException se) {
                                 debug.error(method + "Could not create " +
                                     "session from token ID = " +
                                     tokenIDToBeDestroyed);
                             }
                         } else {
                             siNotFound.add(sessionIndex);
                         }
                     }
 
                     if (isLBReq) {
                         if (foundPeer && !siNotFound.isEmpty()) {
                             boolean peerError = false;
                             LogoutRequest lReq = copyAndMakeMutable(logoutReq);
                             for(Iterator iter = remoteServiceURLs.iterator();
                                 iter.hasNext();) {
 
                                 lReq.setSessionIndex(siNotFound);
                                 String remoteLogoutURL = ((String)iter.next()) +
                                         request.getRequestURI() +
                                        (request.getQueryString() == null ?
                                          "?" : "&") + "isLBReq=false";
                                 LogoutResponse logoutRes =
                                     LogoutUtil.forwardToRemoteServer(lReq,
                                                               remoteLogoutURL);
                                 if (!isNameNotFound(logoutRes)) {
                                     if (isSuccess(logoutRes)) {
                                         siNotFound =
                                          LogoutUtil.getSessionIndex(logoutRes);
                                     } else { 
                                         peerError = true;
                                     }
                                 }
 
                                 if (debug.messageEnabled()) {
                                     debug.message(method 
                                          + "siNotFound = " 
                                          + siNotFound);
                                 }
                                 if (siNotFound == null ||
                                     siNotFound.isEmpty()) {
                                     peerError = false;
                                     break;
                                 }
                             }
                             if (peerError ||
                                 (siNotFound != null && !siNotFound.isEmpty())){
                                 status = PARTIAL_LOGOUT_STATUS;
                             } else {
                                 status = SUCCESS_STATUS;
                             }
                         } else {
                             status = SUCCESS_STATUS;
                         }
                     } else {
                         if (siNotFound.isEmpty()) {
                             status = SUCCESS_STATUS;
                         } else {
                             status = SAML2Utils.generateStatus(
                                 SAML2Constants.STATUS_SUCCESS,
                                 SAML2Utils.bundle.getString("requestSuccess"));
                             LogoutUtil.setSessionIndex(status, siNotFound);
                         }
                     }
                 }
             } while (false);
         } catch (SessionException se) {
             debug.error("processLogoutRequest: ", se);
             status = 
                     SAML2Utils.generateStatus(SAML2Constants.RESPONDER_ERROR, 
                         se.toString());
         } catch (SAML2Exception e) {
             debug.error("processLogoutRequest: " + 
                 "failed to create response", e);
             status = SAML2Utils.generateStatus(SAML2Constants.RESPONDER_ERROR, 
                             e.toString());
         }
         
         // create LogoutResponse
         if (spEntityID == null) {
             spEntityID = nameID.getSPNameQualifier();
         }
         
         return LogoutUtil.generateResponse(status, logoutReq.getID(), issuer,
                 realm, SAML2Constants.SP_ROLE, idpEntity);
     }
 
     static boolean isSuccess(LogoutResponse logoutRes) {
         return logoutRes.getStatus().getStatusCode().getValue()
                         .equals(SAML2Constants.STATUS_SUCCESS);
     }
 
     static boolean isNameNotFound(LogoutResponse logoutRes) {
         Status status = logoutRes.getStatus();
         String  statusMessage = status.getStatusMessage();
 
         return (status.getStatusCode().getValue()
                      .equals(SAML2Constants.RESPONDER_ERROR) &&
                 statusMessage != null &&
                 statusMessage.equals(
                      SAML2Utils.bundle.getString("invalid_name_identifier")));
     }
 
     private static LogoutRequest copyAndMakeMutable(LogoutRequest src) {
         LogoutRequest dest = ProtocolFactory.getInstance()
                                             .createLogoutRequest();
         try {
             dest.setNotOnOrAfter(src.getNotOnOrAfter());
             dest.setReason(src.getReason());
             dest.setEncryptedID(src.getEncryptedID());
             dest.setNameID(src.getNameID());
             dest.setBaseID(src.getBaseID());
             dest.setSessionIndex(src.getSessionIndex());
             dest.setIssuer(src.getIssuer());
             dest.setExtensions(src.getExtensions());
             dest.setID(src.getID());
             dest.setVersion(src.getVersion());
             dest.setIssueInstant(src.getIssueInstant());        
             dest.setDestination(src.getDestination());
             dest.setConsent(src.getConsent());
         } catch(SAML2Exception ex) {
             debug.error("SPLogoutUtil.copyAndMakeMutable:", ex);
         }
         return dest;
     }
    
     private static String getSLOResponseLocationOrLocation(
         SPSSODescriptorElement spsso) {
         String location = null;
         if (spsso != null) {
             List sloList = spsso.getSingleLogoutService();
             if (sloList != null && !sloList.isEmpty()) {
                 location = LogoutUtil.getSLOResponseServiceLocation(
                            sloList, SAML2Constants.HTTP_REDIRECT);
                 if (location == null || (location.length() == 0)) {
                     location = LogoutUtil.getSLOServiceLocation(
                           sloList, SAML2Constants.HTTP_REDIRECT);
                 }
             }
         }
         return location;
     }
 }
 
