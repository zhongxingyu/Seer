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
 * $Id: AgentProvider.java,v 1.4 2007-06-26 23:20:59 mallas Exp $
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
 
      private static final String AGENT_CONFIG_ATTR = 
                        "sunIdentityServerDeviceKeyValue";
      private static final String NAME = "Name";
      private static final String TYPE = "Type";
      private static final String SEC_MECH = "SecurityMech";
      private static final String WSP_ENDPOINT = "WSPEndpoint";
      private static final String KS_FILE = "KeyStoreFile";
      private static final String KS_PASSWD = "KeyStorePassword";
      private static final String KEY_PASSWD = "KeyPassword";
      private static final String RESPONSE_SIGN = "isResponseSign";
      private static final String KEY_ALIAS = "keyAlias";
      private static final String TRUST_AUTHORITY = "TrustAuthority";
      private static final String PROPERTY = "Property:";
      private static final String USER_NAME = "UserName";
      private static final String USER_PASSWORD = "UserPassword";
      private static final String USER_CREDENTIAL = "UserCredential";
      private static final String SERVICE_TYPE = "ServiceType";
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
 
             if (agentConfigAttribute == null) {
                 agentConfigAttribute = new HashSet();
                 agentConfigAttribute.add(AGENT_CONFIG_ATTR);
             }
             IdSearchControl control = new IdSearchControl();
             control.setReturnAttributes(agentConfigAttribute);
             IdSearchResults results = idRepo.searchIdentities(IdType.AGENT,
                 providerName + providerType, control);
             Set agents = results.getSearchResults();
             if (!agents.isEmpty()) {
                 Map attrs = (Map) results.getResultAttributes();
                 AMIdentity provider = (AMIdentity) agents.iterator().next();
                 profilePresent = true;
                 Map attributes = (Map) attrs.get(provider);
                 if(debug.messageEnabled()) {
                    debug.message("Attributes: " + attributes);
                 }
                 Set attributeValues = (Set) attributes.get(
                           AGENT_CONFIG_ATTR.toLowerCase());
                 if (attributeValues != null) {
                     // Get the values and initialize the properties
                     parseAgentKeyValues(attributeValues);
                 }
             }
         } catch (Exception e) {
             debug.error("AgentProvider.init: Unable to get idRepo", e);
             throw (new ProviderException("idRepo exception: "+ e.getMessage()));
         }
     }
 
     private void parseAgentKeyValues(Set keyValues) throws ProviderException {
         if(keyValues == null || keyValues.isEmpty()) {
            return;
         }
         Iterator iter = keyValues.iterator(); 
         while(iter.hasNext()) {
            String entry = (String)iter.next();
            int index = entry.indexOf("=");
            if(index == -1) {
               continue;
            }
            setConfig(entry.substring(0, index),
                       entry.substring(index+1, entry.length()));
         }
     }
 
     private void setConfig(String attr, String value) {
  
         debug.message("Attribute name: " + attr + "Value: "+ value);
 
         if(attr.equals(NAME)) {
            this.providerName = value;
         } else if(attr.equals(TYPE)) {
            this.providerType = value; 
         } else if(attr.equals(SEC_MECH)) {
            if (secMech == null) {
                secMech = new ArrayList();
            }
 
            StringTokenizer st = new StringTokenizer(value, ","); 
            while(st.hasMoreTokens()) {
                secMech.add(st.nextToken());
            }
         } else if(attr.equals(WSP_ENDPOINT)) {
            this.wspEndpoint = value;
         } else if(attr.equals(KS_FILE)) {
            this.ksFile = value;
         } else if(attr.equals(KS_PASSWD)) {
            this.ksPasswd = value;
         } else if(attr.equals(KEY_PASSWD)) {
            this.keyPasswd = value;
         } else if(attr.equals(RESPONSE_SIGN)) {
            this.isResponseSigned = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(KEY_ALIAS)) {
            this.keyAlias = value;
         } else if(attr.equals(SERVICE_TYPE)) {
            this.serviceType = value;
         } else if(attr.equals(USE_DEFAULT_KEYSTORE)) {
            this.isDefaultKeyStore = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(TRUST_AUTHORITY)) {
            try {
                if(trustAuthorities == null) {
                   trustAuthorities = new ArrayList();
                }
                TrustAuthorityConfig ta = TrustAuthorityConfig.getConfig(value, 
                     TrustAuthorityConfig.DISCOVERY_TRUST_AUTHORITY); 
                if(ta != null) {
                   trustAuthorities.add(ta);
                }
            } catch (ProviderException pe) {
                ProviderUtils.debug.error("AgentProvider.setAttribute:error",pe);
            }
 
         } else if(attr.startsWith(PROPERTY)) {
            properties.put(attr.substring(PROPERTY.length()), value);
 
         } else if(attr.equals(USER_CREDENTIAL)) {
            int index = value.indexOf("|");
            if(index == -1) {
               return;
            }
            String usertmp = value.substring(0, index);
            String passwordtmp = value.substring(index+1, value.length()); 
 
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
               if(usercredentials == null) {
                  usercredentials = new ArrayList();
               }
               usercredentials.add(credential);
            }
         } else if(attr.equals(FORCE_AUTHENTICATION)) {
            this.forceAuthn = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(KEEP_SECURITY_HEADERS)) {
            this.preserveSecHeaders = Boolean.valueOf(value).booleanValue();
         } else if(attr.equals(AUTHENTICATION_CHAIN)) {
            this.authenticationChain = value;
         } else {
            if(ProviderUtils.debug.messageEnabled()) {
               ProviderUtils.debug.message("AgentProvider.setConfig: Invalid " +
               "Attribute configured." + attr);
            }
         }
     }
 
     public void store() throws ProviderException {
 
         Set set = new HashSet(); 
         if(providerType != null) { 
            set.add(getKeyValue(TYPE, providerType));
         }
 
         if(wspEndpoint != null) {
            set.add(getKeyValue(WSP_ENDPOINT, wspEndpoint));
         }
         if(ksFile != null) {
            set.add(getKeyValue(KS_FILE, ksFile));
         }
 
         if(ksPasswd != null) {
            set.add(getKeyValue(KS_PASSWD, ksPasswd));
         }
 
         if(keyPasswd != null) {
            set.add(getKeyValue(KEY_PASSWD, keyPasswd));
         }
 
         if(serviceType != null) {
            set.add(getKeyValue(SERVICE_TYPE, serviceType));
         }
 
         if(secMech != null) {
            Iterator iter = secMech.iterator();
            StringBuffer sb =  new StringBuffer(100);
            while(iter.hasNext()) {
               sb.append((String)iter.next()).append(",");
            }
            sb = sb.deleteCharAt(sb.length() - 1);
            set.add(getKeyValue(SEC_MECH, sb.toString()));
         }
 
         set.add(getKeyValue(RESPONSE_SIGN, Boolean.toString(isResponseSigned)));
         set.add(getKeyValue(USE_DEFAULT_KEYSTORE, 
                        Boolean.toString(isDefaultKeyStore)));
         set.add(getKeyValue(FORCE_AUTHENTICATION,
                        Boolean.toString(forceAuthn)));
         set.add(getKeyValue(KEEP_SECURITY_HEADERS,
                        Boolean.toString(preserveSecHeaders)));
         if(authenticationChain != null) {
            set.add(getKeyValue(AUTHENTICATION_CHAIN, authenticationChain));
         }
         
         if(keyAlias != null) {
            set.add(getKeyValue(KEY_ALIAS, keyAlias));
         }
 
         Enumeration props = properties.propertyNames();
         while(props.hasMoreElements()) {
            String propertyName = (String)props.nextElement();
            String propertyValue = properties.getProperty(propertyName);
            set.add(getKeyValue(PROPERTY + propertyName, propertyValue));
         }
 
         if(usercredentials != null) {
            Iterator iter = usercredentials.iterator();
            while(iter.hasNext()) {
               PasswordCredential cred = (PasswordCredential)iter.next();
               String user = cred.getUserName();
               String password = cred.getPassword();
               if(user == null || password == null) {
                  continue;
               }
               StringBuffer sb = new StringBuffer(100);
               sb.append(USER_NAME).append(":").append(user)
                 .append("|").append(USER_PASSWORD).append(":").append(password);
               set.add(getKeyValue(USER_CREDENTIAL, sb.toString()));
            }
         }
 
         if((trustAuthorities != null) && (!trustAuthorities.isEmpty())) {
            Iterator iter = trustAuthorities.iterator();
            while(iter.hasNext()) {
                TrustAuthorityConfig taConfig = 
                           (TrustAuthorityConfig)iter.next();
                set.add(getKeyValue(TRUST_AUTHORITY, taConfig.getName()));
            }
         }
 
         // Save the entry in Agent's profile
         try {
             Map attributes = new HashMap();
             attributes.put(AGENT_CONFIG_ATTR, set);
             if (profilePresent) {
                 // Construct AMIdentity object and save
                 AMIdentity id = new AMIdentity(token,
                     providerName + providerType, IdType.AGENT, "/", null);
                 debug.message("Attributes to be stored: " + attributes);
                 id.setAttributes(attributes);
                 id.store();
             } else {
                 // Create a new Agent profile
                 if (idRepo == null) {
                     idRepo = new AMIdentityRepository(token, "/");
                 }
                 idRepo.createIdentity(IdType.AGENT,
                     providerName + providerType, attributes);
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
                 providerName + providerType, IdType.AGENT, "/", null);
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
