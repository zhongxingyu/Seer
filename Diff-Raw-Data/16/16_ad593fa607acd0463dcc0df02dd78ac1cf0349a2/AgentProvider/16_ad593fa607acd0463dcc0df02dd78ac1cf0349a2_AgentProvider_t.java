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
 * $Id: AgentProvider.java,v 1.17 2008-01-25 21:41:24 mrudul_uchil Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.wss.provider.plugins; 
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.security.KeyStore;
 import java.util.Iterator;
 import java.util.List;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Hashtable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.shared.debug.Debug;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.wss.provider.ProviderConfig;
 import com.sun.identity.wss.provider.ProviderUtils;
 import com.sun.identity.wss.provider.TrustAuthorityConfig;
 import com.sun.identity.wss.provider.ProviderException;
 import com.sun.identity.wss.security.PasswordCredential;
 
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdRepoException;
 
 
 /**
  * This class <code>AgentProvider</code> extends from 
  * <code>ProviderConfig</code> to manage the web services
  * server provider or the web services client configuration via Access
  * Manager Agents.
  */
 public class AgentProvider extends ProviderConfig {
 
      // Initialize the Attributes names set
      private static Set attrNames = new HashSet();;
 
      private static final String SEC_MECH = "SecurityMech";
      private static final String WSP_ENDPOINT = "WSPEndpoint";
      private static final String WSS_PROXY_ENDPOINT = "WSPProxyEndpoint";
      private static final String KS_FILE = "KeyStoreFile";
      private static final String KS_PASSWD = "KeyStorePassword";
      private static final String KEY_PASSWD = "KeyPassword";
      private static final String RESPONSE_SIGN = "isResponseSign";
      private static final String RESPONSE_ENCRYPT = "isResponseEncrypt";
      private static final String REQUEST_SIGN = "isRequestSign";     
      private static final String REQUEST_ENCRYPT = "isRequestEncrypt";
      private static final String REQUEST_HEADER_ENCRYPT = 
          "isRequestHeaderEncrypt";
      private static final String KEY_ALIAS = "privateKeyAlias";
      private static final String KEY_TYPE = "privateKeyType";
      private static final String PUBLIC_KEY_ALIAS = "publicKeyAlias";
      private static final String STS_TRUST_AUTHORITY = "STS";
      private static final String DISCOVERY_TRUST_AUTHORITY = "Discovery";
      private static final String PROPERTY = "Property:";
      private static final String USER_NAME = "UserName";
      private static final String USER_PASSWORD = "UserPassword";
      private static final String USER_CREDENTIAL = "UserCredential";
     private static final String SERVICE_TYPE = "serviceType";
      private static final String USE_DEFAULT_KEYSTORE = "useDefaultStore";
      private static final String FORCE_AUTHENTICATION = "forceUserAuthn";
      private static final String KEEP_SECURITY_HEADERS = "keepSecurityHeaders";
      private static final String AUTHENTICATION_CHAIN = "authenticationChain";  
 
      private AMIdentityRepository idRepo;
      private static Set agentConfigAttribute;
      private static Debug debug = ProviderUtils.debug;
 
      // Instance variables
      private SSOToken token;
      private boolean profilePresent;
 
      static {
          attrNames.add(SEC_MECH);
          attrNames.add(WSP_ENDPOINT);
          attrNames.add(WSS_PROXY_ENDPOINT);
          attrNames.add(KS_FILE);
          attrNames.add(KS_PASSWD);
          attrNames.add(KEY_PASSWD);
          attrNames.add(RESPONSE_SIGN);
          attrNames.add(RESPONSE_ENCRYPT);
          attrNames.add(REQUEST_HEADER_ENCRYPT);
          attrNames.add(REQUEST_SIGN);
          attrNames.add(REQUEST_ENCRYPT);
          attrNames.add(KEY_ALIAS);
          attrNames.add(KEY_TYPE);
          attrNames.add(PUBLIC_KEY_ALIAS);
          attrNames.add(STS_TRUST_AUTHORITY);
          attrNames.add(DISCOVERY_TRUST_AUTHORITY);
          attrNames.add(USER_CREDENTIAL);
          attrNames.add(SERVICE_TYPE);
          attrNames.add(USE_DEFAULT_KEYSTORE);
          attrNames.add(FORCE_AUTHENTICATION);
          attrNames.add(KEEP_SECURITY_HEADERS);
          attrNames.add(AUTHENTICATION_CHAIN);
      }
 
      public void init (String providerName, 
            String providerType, SSOToken token) throws ProviderException {
 
         this.providerName = providerName;
         this.providerType = providerType;
         this.token = token;
 
         // Obtain the provider from Agent profile
         try {
             if (idRepo == null) {
                 idRepo = new AMIdentityRepository(token, "/");
             }
 
             IdSearchControl control = new IdSearchControl();
             control.setAllReturnAttributes(true);
             IdSearchResults results = idRepo.searchIdentities(IdType.AGENTONLY,
                 providerName, control);
             Set agents = results.getSearchResults();
             if (!agents.isEmpty()) {
                 //Map attrs = (Map) results.getResultAttributes();
                 AMIdentity provider = (AMIdentity) agents.iterator().next();
                 profilePresent = true;
                 //Map attributes = (Map) attrs.get(provider);
                 Map attributes = (Map) provider.getAttributes(attrNames);
                 parseAgentKeyValues(attributes);
             }
             
         } catch (Exception e) {
             debug.error("AgentProvider.init: Unable to get idRepo", e);
             throw (new ProviderException("idRepo exception: "+ e.getMessage()));
         }
     }
 
      private void parseAgentKeyValues(Map attributes) throws ProviderException {
         if(attributes == null || attributes.isEmpty()) {
            return;
         }
 
         for (Iterator i = attributes.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             Set valSet = (Set)attributes.get(key);
             String value = null;
             if ((valSet != null) && (valSet.size() > 0)) {
                 Iterator iter = valSet.iterator();
                 StringBuffer sb =  new StringBuffer(100);
                 while(iter.hasNext()) {
                    sb.append((String)iter.next()).append(",");
                 }
                 sb = sb.deleteCharAt(sb.length() - 1);
                 value = sb.toString();
             }
             setConfig(key, value);
         }
 
     }
 
     private void setConfig(String attr, String value) {
  
         debug.message("Attribute name: " + attr + " Value: "+ value);
 
         if (attr.equals(SEC_MECH)) {
            if (secMech == null) {
                secMech = new ArrayList();
            }
 
            StringTokenizer st = new StringTokenizer(value, ","); 
            while(st.hasMoreTokens()) {
                secMech.add(st.nextToken());
            }
         } else if(attr.equals(WSP_ENDPOINT)) {
            this.wspEndpoint = value;
         } else if(attr.equals(WSS_PROXY_ENDPOINT)) {
            this.wssProxyEndpoint = value;
         } else if(attr.equals(KS_FILE)) {
            this.ksFile = value;
         } else if(attr.equals(KS_PASSWD)) {
            this.ksPasswd = value;
         } else if(attr.equals(KEY_PASSWD)) {
            this.keyPasswd = value;
         } else if(attr.equals(RESPONSE_SIGN)) {
            this.isResponseSigned = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(RESPONSE_ENCRYPT)) {
            this.isResponseEncrypted = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(REQUEST_SIGN)) {
            this.isRequestSigned = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(REQUEST_ENCRYPT)) {
            this.isRequestEncrypted = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(REQUEST_HEADER_ENCRYPT)) {
            this.isRequestHeaderEncrypted = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(KEY_ALIAS)) {
            this.privateKeyAlias = value;
         } else if(attr.equals(PUBLIC_KEY_ALIAS)) {
            this.publicKeyAlias = value;
         } else if(attr.equals(KEY_TYPE)) {
            this.privateKeyType = value;
         } else if(attr.equals(SERVICE_TYPE)) {
            this.serviceType = value;
         } else if(attr.equals(USE_DEFAULT_KEYSTORE)) {
            this.isDefaultKeyStore = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(DISCOVERY_TRUST_AUTHORITY)) {
             if ((value != null) && (value.length() != 0) 
                 && (!value.equals("[Empty]"))) {
                 try {
                     taconfig = TrustAuthorityConfig.getConfig(value, 
                         TrustAuthorityConfig.DISCOVERY_TRUST_AUTHORITY);                                              
                 } catch (ProviderException pe) {
                     ProviderUtils.debug.error("AgentProvider.setAttribute:error",pe);
                 }
             }
         } else if (attr.equals(STS_TRUST_AUTHORITY)) {
             if ((value != null) && (value.length() != 0) 
                 && (!value.equals("[Empty]"))) {
                 try {
                     taconfig = TrustAuthorityConfig.getConfig(value, 
                         TrustAuthorityConfig.STS_TRUST_AUTHORITY);
            
                 } catch (ProviderException pe) {
                     ProviderUtils.debug.error("AgentProvider.setAttribute:error",pe);
                 }
             }
         } else if(attr.startsWith(PROPERTY)) {
             properties.put(attr.substring(PROPERTY.length()), value);
 
         } else if(attr.equals(USER_CREDENTIAL)) {
             if(usercredentials == null) {
                 usercredentials = new ArrayList();
             }
             StringTokenizer stVal = new StringTokenizer(value, ","); 
             while(stVal.hasMoreTokens()) {
                 String tmpVal = (String)stVal.nextToken();
                 int index = tmpVal.indexOf("|");
                 if(index == -1) {
                     return;
                 }
                 String usertmp = tmpVal.substring(0, index);
                 String passwordtmp = tmpVal.substring(index+1, tmpVal.length()); 
 
                 String user = null;
                 String password = null;
                 StringTokenizer st = new StringTokenizer(usertmp, ":"); 
                 if(USER_NAME.equals(st.nextToken())) {
                     if(st.hasMoreTokens()) {
                         user = st.nextToken();
                     }               
                 }
                 StringTokenizer st1 = new StringTokenizer(passwordtmp, ":"); 
                 if(USER_PASSWORD.equals(st1.nextToken())) {
                     if(st1.hasMoreTokens()) {
                         password = st1.nextToken();
                     }              
                 }
 
                 if((user != null) && (password != null)) {
                     PasswordCredential credential = 
                         new PasswordCredential(user, password);
                     usercredentials.add(credential);
                 }
             }
         } else if(attr.equals(FORCE_AUTHENTICATION)) {
            if ((value != null) && (value.length() != 0)) {
                this.forceAuthn = Boolean.valueOf(value).booleanValue();
            }
         } else if(attr.equals(KEEP_SECURITY_HEADERS)) {
            this.preserveSecHeaders = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(AUTHENTICATION_CHAIN)) {
             if ((value != null) && (value.length() != 0) 
                 && (!value.equals("[Empty]"))) {
                 this.authenticationChain = value;
             }
         } else {
            if(ProviderUtils.debug.messageEnabled()) {
               ProviderUtils.debug.message("AgentProvider.setConfig: Invalid " +
               "Attribute configured." + attr);
            }
         }
     }
 
     public void store() throws ProviderException {
 
         Map config = new HashMap();
 
         if(wspEndpoint != null) {
            config.put(WSP_ENDPOINT, wspEndpoint);
         }
 
         if(wssProxyEndpoint != null) {
            config.put(WSS_PROXY_ENDPOINT, wssProxyEndpoint);
         }
 
         if(ksFile != null) {
            config.put(KS_FILE, ksFile);
         }
 
         if(ksPasswd != null) {
            config.put(KS_PASSWD, ksPasswd);
         }
 
         if(keyPasswd != null) {
            config.put(KEY_PASSWD, keyPasswd);
         }
 
         if(serviceType != null) {
            config.put(SERVICE_TYPE, serviceType);
         }
 
         Set secMechSet = new HashSet();
         if(secMech != null) {
            Iterator iter = secMech.iterator();
            while(iter.hasNext()) {
                secMechSet.add((String)iter.next());
            }
         }
 
         config.put(RESPONSE_SIGN, 
                             Boolean.toString(isResponseSigned));
         config.put(RESPONSE_ENCRYPT, 
                             Boolean.toString(isResponseEncrypted));
         config.put(REQUEST_SIGN, 
                             Boolean.toString(isRequestSigned));
         config.put(REQUEST_ENCRYPT, 
                             Boolean.toString(isRequestEncrypted));
         config.put(REQUEST_HEADER_ENCRYPT,
                             Boolean.toString(isRequestHeaderEncrypted));
         config.put(USE_DEFAULT_KEYSTORE, 
                        Boolean.toString(isDefaultKeyStore));
        if((providerType != null) && (providerType.equals(ProviderConfig.WSC))) {
            config.put(FORCE_AUTHENTICATION,Boolean.toString(forceAuthn));
        }
         config.put(KEEP_SECURITY_HEADERS,
                        Boolean.toString(preserveSecHeaders));
         if(authenticationChain != null) {
            config.put(AUTHENTICATION_CHAIN, authenticationChain);
         }
         
         if(privateKeyAlias != null) {
            config.put(KEY_ALIAS, privateKeyAlias);
         }
         
         if(privateKeyType != null) {
            config.put(KEY_TYPE, privateKeyType);
         }
 
         if(publicKeyAlias != null) {
            config.put(PUBLIC_KEY_ALIAS, publicKeyAlias);
         }
 
         Enumeration props = properties.propertyNames();
         while(props.hasMoreElements()) {
            String propertyName = (String)props.nextElement();
            String propertyValue = properties.getProperty(propertyName);
            config.put(PROPERTY + propertyName, propertyValue);
         }
 
         if(usercredentials != null) {
            Iterator iter = usercredentials.iterator();
            StringBuffer sb =  new StringBuffer(100);
            while(iter.hasNext()) {
               PasswordCredential cred = (PasswordCredential)iter.next();
               String user = cred.getUserName();
               String password = cred.getPassword();
               if(user == null || password == null) {
                  continue;
               }
               
               sb.append(USER_NAME).append(":").append(user)
                 .append("|").append(USER_PASSWORD).append(":").append(password).append(",");
            }
            sb = sb.deleteCharAt(sb.length() - 1);
            config.put(USER_CREDENTIAL, sb.toString());
         }
 
         String stsTA = null;
         String discoTA = null;
         if(taconfig != null) {
            if(taconfig.getType().equals(STS_TRUST_AUTHORITY)) {
               stsTA = taconfig.getName();                  
            }
            
            if(taconfig.getType().equals(DISCOVERY_TRUST_AUTHORITY)) {
               discoTA = taconfig.getName();                  
            } 
         }
                 
         if(stsTA != null) {
            config.put(STS_TRUST_AUTHORITY, stsTA);
         }
         if(discoTA != null) {
            config.put(DISCOVERY_TRUST_AUTHORITY, discoTA); 
         }
         
 
         // Save the entry in Agent's profile
         try {
             Map attributes = new HashMap();
             Set values = null ;
 
             for (Iterator i = config.keySet().iterator(); i.hasNext(); ) {
                 String key = (String)i.next();
                 String value = (String)config.get(key);
                 values = new HashSet();
                 values.add(value);
                 attributes.put(key, values);
             }
             if (secMechSet != null) {
                 attributes.put(SEC_MECH, secMechSet);
             }
 
             if (profilePresent) {
                 // Construct AMIdentity object and save
                 AMIdentity id = new AMIdentity(token,
                     providerName, IdType.AGENTONLY, "/", null);
                 debug.message("Attributes to be stored: " + attributes);
                 id.setAttributes(attributes);
                 id.store();
             } else {
                 // Create a new Agent profile
                 if (idRepo == null) {
                     idRepo = new AMIdentityRepository(token, "/");
                 }
                 idRepo.createIdentity(IdType.AGENTONLY,
                     providerName, attributes);
             }
         } catch (Exception e) {
             debug.error("AgentProvider.store: Unable to get idRepo", e);
             throw (new ProviderException("idRepo exception: "+ e.getMessage()));
         }
     }
 
     public void delete() throws ProviderException {
         if (!profilePresent) {
             return;
         }
 
         // Delete the agent profile
         try {
             if (idRepo == null) {
                 idRepo = new AMIdentityRepository(token, "/");
             }
             // Construct AMIdentity object to delete
             AMIdentity id = new AMIdentity(token,
                 providerName, IdType.AGENTONLY, "/", null);
             Set identities = new HashSet();
             identities.add(id);
             idRepo.deleteIdentities(identities);
         } catch (Exception e) {
             debug.error("AgentProvider.delete: Unable to get idRepo", e);
             throw (new ProviderException("idRepo exception: "+ e.getMessage()));
         }
     }
 
     private String getKeyValue(String key, String value) {
         return key + "=" + value;
     }
 
     /**
      * Checks if the agent profile exists for this provider.
      * @return true if the profile exists.
      */
     public boolean isExists() {
         return profilePresent;
     }
 
 }
