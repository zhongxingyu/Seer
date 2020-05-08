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
 package org.overlord.gadgets.web.server.http.auth;
 
 import java.security.KeyPair;
 import java.security.KeyStore;
 
 import org.apache.shindig.gadgets.http.HttpRequest;
import org.overlord.commons.auth.jboss7.SAMLBearerTokenUtil;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 /**
  * Provides SAML Bearer Token authentication.
  *
  * @author eric.wittmann@redhat.com
  */
 @Singleton
 public class SAMLBearerTokenAuthenticationProvider implements AuthenticationProvider {
 
     private String issuer;
     private String service;
     private boolean signAssertions;
     private String keystorePath;
     private String keystorePassword;
     private String alias;
     private String aliasPassword;
 
     /**
      * Constructor.
      * @param issuer
      * @param service
      */
     @Inject
     public SAMLBearerTokenAuthenticationProvider(
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_ISSUER) String issuer,
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_SERVICE) String service,
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_SIGN_ASSERTIONS) String signAssertions,
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_KEYSTORE) String keystorePath,
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_KEYSTORE_PASSWORD) String keystorePassword,
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_KEY_ALIAS) String alias,
             @Named(AuthenticationConstants.CONFIG_SAML_AUTH_KEY_PASSWORD) String aliasPassword) {
         this.issuer = issuer;
         this.service = service;
         this.signAssertions = "true".equals(signAssertions);
         this.keystorePath = keystorePath;
         this.keystorePassword = keystorePassword;
         this.alias = alias;
         this.aliasPassword = aliasPassword;
     }
 
     /**
      * @see org.overlord.gadgets.web.server.http.auth.AuthenticationProvider#provideAuthentication(org.apache.shindig.gadgets.http.HttpRequest)
      */
     @Override
     public void provideAuthentication(HttpRequest httpRequest) {
         String headerValue = BasicAuthenticationProvider.createBasicAuthHeader("SAML-BEARER-TOKEN", createSAMLBearerTokenAssertion()); //$NON-NLS-1$
         httpRequest.setHeader("Authorization", headerValue); //$NON-NLS-1$
     }
 
     /**
      * Creates the SAML Bearer Token that will be used to authenticate to the
      * S-RAMP Atom API.
      */
     private String createSAMLBearerTokenAssertion() {
        String samlAssertion = SAMLBearerTokenUtil.createSAMLAssertion(issuer, service);
         if (signAssertions) {
             try {
                 KeyStore keystore = SAMLBearerTokenUtil.loadKeystore(keystorePath, keystorePassword);
                 KeyPair keyPair = SAMLBearerTokenUtil.getKeyPair(keystore, alias, aliasPassword);
                 samlAssertion = SAMLBearerTokenUtil.signSAMLAssertion(samlAssertion, keyPair);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return samlAssertion;
     }
 
 }
