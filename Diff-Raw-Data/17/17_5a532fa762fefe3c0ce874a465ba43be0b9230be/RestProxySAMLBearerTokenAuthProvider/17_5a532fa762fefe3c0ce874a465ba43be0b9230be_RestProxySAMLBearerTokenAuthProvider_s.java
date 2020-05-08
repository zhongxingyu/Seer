 /*
  * Copyright 2013 JBoss Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.overlord.gadgets.web.server.servlets;
 
 import java.net.HttpURLConnection;
 import java.security.KeyPair;
 import java.security.KeyStore;
 import java.util.Properties;
 
import org.overlord.commons.auth.jboss7.SAMLBearerTokenUtil;
 
 /**
  * An auth provider that uses SAML bearer token authentication.
  * @author eric.wittmann@redhat.com
  */
 public class RestProxySAMLBearerTokenAuthProvider implements RestProxyAuthProvider {
 
     private String proxyName;
     private Properties configProperties;
 
     /**
      * Constructor.
      */
     public RestProxySAMLBearerTokenAuthProvider() {
     }
 
     /**
      * @see org.overlord.gadgets.web.server.servlets.RestProxyAuthProvider#setConfiguration(java.lang.String, java.util.Properties)
      */
     @Override
     public void setConfiguration(String proxyName, Properties configProperties) {
         this.proxyName = proxyName;
         this.configProperties = configProperties;
     }
 
     /**
      * @see org.overlord.gadgets.web.server.servlets.RestProxyAuthProvider#provideAuthentication(java.net.HttpURLConnection)
      */
     @Override
     public void provideAuthentication(HttpURLConnection connection) {
         String headerValue = RestProxyBasicAuthProvider.createBasicAuthHeader("SAML-BEARER-TOKEN", createSAMLBearerTokenAssertion()); //$NON-NLS-1$
         connection.setRequestProperty("Authorization", headerValue); //$NON-NLS-1$
     }
 
     /**
      * Creates the SAML Bearer Token that will be used to authenticate to the
      * S-RAMP Atom API.
      */
     private String createSAMLBearerTokenAssertion() {
        String samlAssertion = SAMLBearerTokenUtil.createSAMLAssertion(getIssuer(), getService());
         if (isSignAssertions()) {
             try {
                 KeyStore keystore = SAMLBearerTokenUtil.loadKeystore(getKeystorePath(), getKeystorePassword());
                 KeyPair keyPair = SAMLBearerTokenUtil.getKeyPair(keystore, getAlias(), getAliasPassword());
                 samlAssertion = SAMLBearerTokenUtil.signSAMLAssertion(samlAssertion, keyPair);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return samlAssertion;
     }
 
     /**
      * @return the configured saml issuer
      */
     private String getIssuer() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.issuer";
         return this.configProperties.getProperty(propKey);
     }
 
     /**
      * @return the configured saml service
      */
     private String getService() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.service";
         return this.configProperties.getProperty(propKey);
     }
 
     /**
      * @return whether saml assertions should be digitally signed
      */
     private boolean isSignAssertions() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.sign-assertions";
         return "true".equals(this.configProperties.getProperty(propKey));
     }
 
     /**
      * @return the configured digital signature keystore
      */
     private String getKeystorePath() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.keystore";
         return this.configProperties.getProperty(propKey);
     }
 
     /**
      * @return the configured keystore password
      */
     private String getKeystorePassword() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.keystore-password";
         return this.configProperties.getProperty(propKey);
     }
 
     /**
      * @return the configured keystore alias
      */
     private String getAlias() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.key-alias";
         return this.configProperties.getProperty(propKey);
     }
 
     /**
      * @return the configured keystore alias password
      */
     private String getAliasPassword() {
         String propKey = "gadget-server.rest-proxy." + this.proxyName + ".authentication.saml.key-password";
         return this.configProperties.getProperty(propKey);
     }
 
 }
