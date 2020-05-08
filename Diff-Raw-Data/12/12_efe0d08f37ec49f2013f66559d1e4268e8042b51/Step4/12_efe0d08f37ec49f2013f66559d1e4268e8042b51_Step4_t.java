 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: Step4.java,v 1.14 2008-08-26 04:36:31 veiming Exp $
  *
  */
 package com.sun.identity.config.wizard;
 import com.iplanet.am.util.SSLSocketFactoryManager;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.click.control.ActionLink;
 import com.sun.identity.config.util.AjaxPage;
 import com.sun.identity.setup.SetupConstants;
 import net.sf.click.Context;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPSearchResults;
 /**
  * Step 4 is the input of the remote user data store properties.
  */
 public class Step4 extends AjaxPage {
     public static final String LDAP_STORE_SESSION_KEY = "wizardCustomUserStore";
     public ActionLink validateUMHostLink = 
         new ActionLink("validateUMHost", this, "validateUMHost");
     public ActionLink setSSLLink = 
         new ActionLink("setSSL", this, "setSSL");
     public ActionLink setUMEmbedded = 
         new ActionLink("setUMEmbedded", this, "setUMEmbedded");
     public ActionLink resetUMEmbedded = 
         new ActionLink("resetUMEmbedded", this, "resetUMEmbedded");
     public ActionLink setHostLink = 
         new ActionLink("setHost", this, "setHost");
     public ActionLink setPortLink = 
         new ActionLink("setPort", this, "setPort");
     public ActionLink setRootSuffixLink = 
         new ActionLink("setRootSuffix", this, "setRootSuffix");
     public ActionLink setLoginIDLink = 
         new ActionLink("setLoginID", this, "setLoginID");
     public ActionLink setPasswordLink = 
         new ActionLink("setPassword", this, "setPassword");
     public ActionLink setStoreTypeLink = 
         new ActionLink("setStoreType", this, "setStoreType");    
 
     private String responseString = "ok";
     
     public Step4() {
     }
     
     public void onInit() {
         super.onInit();
         Context ctx = getContext();
         
         if (ctx.getSessionAttribute(SetupConstants.USER_STORE_HOST) == null) {
             String val = getAttribute(SetupConstants.CONFIG_VAR_DATA_STORE,
                 SetupConstants.SMS_EMBED_DATASTORE);
             if (!val.equals(SetupConstants.SMS_EMBED_DATASTORE)) {
 
                 val = getAttribute("configStoreSSL", "SIMPLE");
                 ctx.setSessionAttribute(SetupConstants.USER_STORE_SSL, val);
 
                 val = getAttribute("configStoreHost", getHostName());
                 ctx.setSessionAttribute(SetupConstants.USER_STORE_HOST, val);
 
                 val = getAttribute("configStorePort", "389");
                 ctx.setSessionAttribute(SetupConstants.USER_STORE_PORT, val);
 
                 val = getAttribute("configStoreLoginId",Wizard.defaultUserName);
                 ctx.setSessionAttribute(SetupConstants.USER_STORE_LOGIN_ID,val);
 
                 val = getAttribute("rootSuffix", Wizard.defaultRootSuffix);
                 ctx.setSessionAttribute(SetupConstants.USER_STORE_ROOT_SUFFIX,
                     val);
             }
             ctx.setSessionAttribute("EXT_DATA_STORE", "true");
             ctx.setSessionAttribute(SetupConstants.USER_STORE_TYPE,
                 "LDAPv3ForAMDS");
         }
 
        String smsType = getAttribute(SetupConstants.CONFIG_VAR_DATA_STORE,
            "embedded");
        if (!smsType.equals("embedded")) {
            ctx.setSessionAttribute("EXT_DATA_STORE", "true");
            addModel("radioDataTypeDisabled", "disabled");
        } else {
            addModel("radioDataTypeDisabled", "");
        }

         String val = getAttribute(SetupConstants.USER_STORE_HOST,getHostName());
         ctx.setSessionAttribute(SetupConstants.USER_STORE_HOST, val);
         addModel("userStoreHost", val);
         
         val = getAttribute(SetupConstants.USER_STORE_SSL, "SIMPLE");
         ctx.setSessionAttribute(SetupConstants.USER_STORE_SSL, val);
         if (val.equals("SSL")) {
             addModel("selectUserStoreSSL", "checked=\"checked\"");
         } else {
             addModel("selectUserStoreSSL", "");
         }
 
         val = getAttribute(SetupConstants.USER_STORE_PORT, "389");
         ctx.setSessionAttribute(SetupConstants.USER_STORE_PORT, val);
         addModel("userStorePort", val);
 
         val = getAttribute(SetupConstants.USER_STORE_LOGIN_ID,
             Wizard.defaultUserName);
         ctx.setSessionAttribute(SetupConstants.USER_STORE_LOGIN_ID, val);
         addModel("userStoreLoginId", val);
 
         val = getAttribute(SetupConstants.USER_STORE_ROOT_SUFFIX, 
             Wizard.defaultRootSuffix);
         ctx.setSessionAttribute(SetupConstants.USER_STORE_ROOT_SUFFIX, val);
         addModel("userStoreRootSuffix", val);
 
         val = getAttribute(SetupConstants.USER_STORE_TYPE, "LDAPv3ForAMDS");
         if (val.equals("LDAPv3ForAMDS")) {
             addModel("selectLDAPv3amds", "checked=\"checked\"");
             addModel("selectLDAPv3", "");
         } else {
             addModel("selectLDAPv3", "checked=\"checked\"");
             addModel("selectLDAPv3amds", "");
         }
 
         val = getAttribute("EXT_DATA_STORE", "true");
         addModel("EXT_DATA_STORE", val);
         if (val.equals("true")) {
             addModel("selectEmbeddedUM", "");
             addModel("selectExternalUM", "checked=\"checked\"");
         } else {
             addModel("selectEmbeddedUM", "checked=\"checked\"");
             addModel("selectExternalUM", "");
         }
     }
     
     public boolean setAll() {     
         setPath(null);
         return false;
     }
     
     public boolean setSSL() {
         String ssl = toString("ssl");
         if ((ssl != null) && ssl.length() > 0) {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_SSL, ssl);
         } else {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_SSL, "SIMPLE");
         }
         writeToResponse(getLocalizedString(responseString));
         setPath(null);
         return false;
     }
 
     public boolean setHost() {
         String host = toString("host");
         if ((host != null) && host.length() > 0) {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_HOST, host);
         } else {
             responseString = "missing.host.name";            
         }
         writeToResponse(getLocalizedString(responseString));
         setPath(null);
         return false;
     }
 
     public boolean setUMEmbedded() {
         getContext().setSessionAttribute("EXT_DATA_STORE", "false");
         setPath(null);
         return false;
     }
 
     public boolean resetUMEmbedded() {
         getContext().setSessionAttribute("EXT_DATA_STORE", "true");
         setPath(null);
         return false;
     }
         
     public boolean setPort() {
         String port = toString("port");
         
         if ((port != null) && port.length() > 0) {
             int intValue = Integer.parseInt(port);
             if ((intValue > 0) && (intValue < 65535)) {
                 getContext().setSessionAttribute(
                     SetupConstants.USER_STORE_PORT, port);
             } else {
                 responseString = "invalid.port.number";
             }
         } else {
             responseString = "missing.host.port";            
         }
         writeToResponse(getLocalizedString(responseString));
         setPath(null);
         return false;
     }
     
     public boolean setLoginID() {
         String dn = toString("dn");
         if ((dn != null) && dn.length() > 0) {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_LOGIN_ID, dn);
         } else {
             responseString = "missing.login.id";            
         }
         writeToResponse(getLocalizedString(responseString));
         setPath(null);
         return false;
     }
     
     public boolean setPassword() {
         String pwd = toString("password");
         if ((pwd != null) && pwd.length() > 0) {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_LOGIN_PWD, pwd);
         } else {
             responseString = "missing.password";            
         }
         writeToResponse(getLocalizedString(responseString));
         setPath(null);
         return false;
     }
     
     public boolean setRootSuffix() {
         String rootsuffix = toString("rootsuffix");
         if ((rootsuffix != null) && rootsuffix.length() > 0) {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_ROOT_SUFFIX, rootsuffix);
         } else {
             responseString = "missing.root.suffix";            
         }
         writeToResponse(getLocalizedString(responseString));
         setPath(null);
         return false;
     }
     
     public boolean setStoreType() {
         String type = toString("type");
         if ((type != null) && type.length() > 0) {
             getContext().setSessionAttribute(
                 SetupConstants.USER_STORE_TYPE, type);
         } 
         writeToResponse(responseString);
         setPath(null);
         return false;
     }
     
     public boolean validateUMHost() {
         Context ctx = getContext();
         String strSSL = (String)ctx.getSessionAttribute(
             SetupConstants.USER_STORE_SSL);
         boolean ssl = (strSSL != null) && (strSSL.equals("SSL"));
              
         String host = (String)ctx.getSessionAttribute(
             SetupConstants.USER_STORE_HOST);
         String strPort = (String)ctx.getSessionAttribute(
             SetupConstants.USER_STORE_PORT);
         int port = Integer.parseInt(strPort);
         String bindDN = (String)ctx.getSessionAttribute(
             SetupConstants.USER_STORE_LOGIN_ID);
         String rootSuffix = (String)ctx.getSessionAttribute(
             SetupConstants.USER_STORE_ROOT_SUFFIX);
         String bindPwd = (String)ctx.getSessionAttribute(
             SetupConstants.USER_STORE_LOGIN_PWD);
         
         LDAPConnection ld = null;
         try {
             ld = (ssl) ? new LDAPConnection(
                 SSLSocketFactoryManager.getSSLSocketFactory()) :
                 new LDAPConnection();
             ld.setConnectTimeout(300);
             ld.connect(3, host, port, bindDN, bindPwd);
             
             String filter = "cn=" + "\"" + rootSuffix + "\"";
             String[] attrs = {""};
             ld.search(rootSuffix, LDAPConnection.SCOPE_BASE, filter, 
                 attrs, false);
             writeToResponse("ok");
         } catch (LDAPException lex) {
             switch (lex.getLDAPResultCode()) {
                 case LDAPException.CONNECT_ERROR:
                     writeToResponse(getLocalizedString("ldap.connect.error")); 
                     break;
                 case LDAPException.SERVER_DOWN:
                     writeToResponse(getLocalizedString("ldap.server.down"));   
                     break;
                 case LDAPException.INVALID_DN_SYNTAX:
                     writeToResponse(getLocalizedString("ldap.invalid.dn"));  
                     break;
                 case LDAPException.NO_SUCH_OBJECT:
                     writeToResponse(getLocalizedString("ldap.nosuch.object"));
                     break;
                 case LDAPException.INVALID_CREDENTIALS:
                     writeToResponse(
                             getLocalizedString("ldap.invalid.credentials"));
                     break;
                 case LDAPException.UNWILLING_TO_PERFORM:
                     writeToResponse(getLocalizedString("ldap.unwilling"));
                     break;
                 case LDAPException.INAPPROPRIATE_AUTHENTICATION:
                     writeToResponse(getLocalizedString("ldap.inappropriate"));
                     break;
                 case LDAPException.CONSTRAINT_VIOLATION:
                     writeToResponse(getLocalizedString("ldap.constraint"));
                     break;
                 default:
                     writeToResponse(
                         getLocalizedString("cannot.connect.to.SM.datastore"));                                              
             }           
         } catch (Exception e) {
             writeToResponse(
                 getLocalizedString("cannot.connect.to.SM.datastore"));
         } finally {
             if (ld != null) {
                 try {
                     ld.disconnect();
                 } catch (LDAPException ex) {
                     //ignore
                 }
             }
         }
 
         
         setPath(null);
         return false;
     }
 }
