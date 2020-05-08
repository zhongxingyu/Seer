 /*
  * Copyright 2004,2005 The Apache Software Foundation.
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
 
 package org.apache.rahas.impl;
 
 import java.io.FileInputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.OMAttribute;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMFactory;
 import org.apache.axiom.om.impl.builder.StAXOMBuilder;
 import org.apache.axis2.description.Parameter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.rahas.TrustException;
 import org.apache.rahas.impl.util.SAMLCallbackHandler;
 
 /**
  * Configuration manager for the <code>SAMLTokenIssuer</code>
  *
  * @see SAMLTokenIssuer
  */
 public class SAMLTokenIssuerConfig extends AbstractIssuerConfig {
 
 	
 	Log log = LogFactory.getLog(SAMLTokenIssuerConfig.class);
 	
     /**
      * The QName of the configuration element of the SAMLTokenIssuer
      */
     public final static QName SAML_ISSUER_CONFIG = new QName("saml-issuer-config");
 
     /**
      * Element name to include the alias of the private key to sign the response or
      * the issued token
      */
     private final static QName ISSUER_KEY_ALIAS = new QName("issuerKeyAlias");
 
     /**
      * Element name to include the password of the private key to sign the
      * response or the issued token
      */
     private final static QName ISSUER_KEY_PASSWD = new QName("issuerKeyPassword");
 
     /**
      * Element to specify the lifetime of the SAMLToken
      * Dafaults to 300000 milliseconds (5 mins)
      */
     private final static QName TTL = new QName("timeToLive");
 
     /**
      * Element to list the trusted services
      */
     private final static QName TRUSTED_SERVICES = new QName("trusted-services");
 
     private final static QName KEY_SIZE = new QName("keySize");
 
     private final static QName SERVICE = new QName("service");
     private final static QName ALIAS = new QName("alias");
 
     public final static QName USE_SAML_ATTRIBUTE_STATEMENT = new QName("useSAMLAttributeStatement");
 
     public final static QName ISSUER_NAME = new QName("issuerName");
     
     public final static QName SAML_CALLBACK_CLASS = new QName("dataCallbackHandlerClass");
         
     protected String issuerKeyAlias;
     protected String issuerKeyPassword;
     protected String issuerName;
     protected Map trustedServices = new HashMap();
     protected String trustStorePropFile;
     protected SAMLCallbackHandler callbackHander;
   
     /**
      * Create a new configuration with issuer name and crypto information
      * @param issuerName Name of the issuer
      * @param cryptoProviderClassName WSS4J Crypto impl class name
      * @param cryptoProps Configuration properties of crypto impl
      */
     public SAMLTokenIssuerConfig(String issuerName, String cryptoProviderClassName, Properties cryptoProps) {
         this.issuerName = issuerName;
         this.setCryptoProperties(cryptoProviderClassName, cryptoProps);
     }
     
     /**
      * Create a SAMLTokenIssuer configuration with a config file picked from the
      * given location.
      * @param configFilePath Path to the config file
      * @throws TrustException
      */
     public SAMLTokenIssuerConfig(String configFilePath) throws TrustException {
         FileInputStream fis;
         StAXOMBuilder builder;
         try {
             fis = new FileInputStream(configFilePath);
             builder = new StAXOMBuilder(fis);
         } catch (Exception e) {
             throw new TrustException("errorLoadingConfigFile",
                     new String[] { configFilePath });
         }
         this.load(builder.getDocumentElement());
     }
     
     /**
      * Create a  SAMLTokenIssuer configuration using the give config element
      * @param elem Configuration element as an <code>OMElement</code>
      * @throws TrustException
      */
     public SAMLTokenIssuerConfig(OMElement elem) throws TrustException {
         this.load(elem);
     }
 
     private void load(OMElement elem) throws TrustException {
         OMElement proofKeyElem = elem.getFirstChildWithName(PROOF_KEY_TYPE);
         if (proofKeyElem != null) {
             this.proofKeyType = proofKeyElem.getText().trim();
         }
 
         //The alias of the private key
         OMElement userElem = elem.getFirstChildWithName(ISSUER_KEY_ALIAS);
         if (userElem != null) {
             this.issuerKeyAlias = userElem.getText().trim();
         }
 
         if (this.issuerKeyAlias == null || "".equals(this.issuerKeyAlias)) {
             throw new TrustException("samlIssuerKeyAliasMissing");
         }
 
         OMElement issuerKeyPasswdElem = elem.getFirstChildWithName(ISSUER_KEY_PASSWD);
         if (issuerKeyPasswdElem != null) {
             this.issuerKeyPassword = issuerKeyPasswdElem.getText().trim();
         }
 
         if (this.issuerKeyPassword == null || "".equals(this.issuerKeyPassword)) {
             throw new TrustException("samlIssuerKeyPasswdMissing");
         }
 
         OMElement issuerNameElem = elem.getFirstChildWithName(ISSUER_NAME);
         if (issuerNameElem != null) {
             this.issuerName = issuerNameElem.getText().trim();
         }
 
         if (this.issuerName == null || "".equals(this.issuerName)) {
             throw new TrustException("samlIssuerNameMissing");
         }
 
         this.cryptoPropertiesElement = elem.getFirstChildWithName(CRYPTO_PROPERTIES);
         if (this.cryptoPropertiesElement != null) {
             if ((this.cryptoElement =
                 this.cryptoPropertiesElement .getFirstChildWithName(CRYPTO)) == null){
                 // no children. Hence, prop file should have been defined
                 this.cryptoPropertiesFile = this.cryptoPropertiesElement .getText().trim();
             }
             // else Props should be defined as children of a crypto element
         }
 
         OMElement keyCompElem = elem.getFirstChildWithName(KeyComputation.KEY_COMPUTATION);
        if (keyCompElem != null && keyCompElem.getText() != null && !"".equals(keyCompElem)) {
             this.keyComputation = Integer.parseInt(keyCompElem.getText());
         }
 
         //time to live
         OMElement ttlElem = elem.getFirstChildWithName(TTL);
         if (ttlElem != null) {
             try {
                 this.ttl = Long.parseLong(ttlElem.getText().trim());
             } catch (NumberFormatException e) {
                 throw new TrustException("invlidTTL");
             }
         }
 
         OMElement keySizeElem = elem.getFirstChildWithName(KEY_SIZE);
         if (keySizeElem != null) {
             try {
                 this.keySize = Integer.parseInt(keySizeElem.getText().trim());
             } catch (NumberFormatException e) {
                 throw new TrustException("invalidKeysize");
             }
         }
 
         this.addRequestedAttachedRef = elem
                 .getFirstChildWithName(ADD_REQUESTED_ATTACHED_REF) != null;
         this.addRequestedUnattachedRef = elem
                 .getFirstChildWithName(ADD_REQUESTED_UNATTACHED_REF) != null;
 
         //Process trusted services
         OMElement trustedServices = elem.getFirstChildWithName(TRUSTED_SERVICES);
 
         /*
         * If there are trusted services add them to a list
         * Only trusts myself to issue tokens to :
         * In this case the STS is embedded in the service as well and
         * the issued token can only be used with that particular service
         * since the response secret is encrypted by the service's public key
         */
         if (trustedServices != null) {
             //Now process the trusted services
             Iterator servicesIter = trustedServices.getChildrenWithName(SERVICE);
             while (servicesIter.hasNext()) {
                 OMElement service = (OMElement) servicesIter.next();
                 OMAttribute aliasAttr = service.getAttribute(ALIAS);
                 if (aliasAttr == null) {
                     //The certificate alias is a must
                     throw new TrustException("aliasMissingForService",
                                              new String[]{service.getText().trim()});
                 }
                 if (this.trustedServices == null) {
                     this.trustedServices = new HashMap();
                 }
 
                 //Add the trusted service and the alias to the map of services
                 this.trustedServices.put(service.getText().trim(), aliasAttr.getAttributeValue());
             }
 
             //There maybe no trusted services as well, Therefore do not 
             //throw an exception when there are no trusted in the list at the 
             //moment
         }
         
         
        	OMElement attrElemet = elem.getFirstChildWithName(SAML_CALLBACK_CLASS);
 		if (attrElemet != null) {
 				try {
 					String value = attrElemet.getText();
 					Class handlerClass = Class.forName(value);
 					this.callbackHander = (SAMLCallbackHandler)handlerClass.newInstance();
 				} catch (ClassNotFoundException e) {
 					log.debug("Error loading class" , e);
 					throw new TrustException("Error loading class" , e);
 				} catch (InstantiationException e) {
 					log.debug("Error instantiating class" , e);
 					throw new TrustException("Error instantiating class" , e);
 				} catch (IllegalAccessException e) {
 					log.debug("Illegal Access" , e);
 					throw new TrustException("Illegal Access" , e);
 				}
 		}
 				
 
     }
 
     /**
      * Generate an Axis2 parameter for this configuration
      * @return An Axis2 Parameter instance with configuration information
      */
     public Parameter getParameter() {
         Parameter param = new Parameter();
         
         OMFactory fac = OMAbstractFactory.getOMFactory();
         
         OMElement paramElem = fac.createOMElement("Parameter", null);
         paramElem.addAttribute("name", SAML_ISSUER_CONFIG.getLocalPart(), null);
         
         OMElement configElem = fac.createOMElement(SAML_ISSUER_CONFIG, paramElem);
         
         OMElement issuerNameElem = fac.createOMElement(ISSUER_NAME, configElem);
         issuerNameElem.setText(this.issuerName);
         
         OMElement issuerKeyAliasElem = fac.createOMElement(ISSUER_KEY_ALIAS, configElem);
         issuerKeyAliasElem.setText(this.issuerKeyAlias);
         
         OMElement issuerKeyPasswd = fac.createOMElement(ISSUER_KEY_PASSWD, configElem);
         issuerKeyPasswd.setText(this.issuerKeyPassword);
         
         configElem.addChild(this.cryptoPropertiesElement);
         
         OMElement keySizeElem = fac.createOMElement(KEY_SIZE, configElem);
         keySizeElem.setText(Integer.toString(this.keySize));
         
         if(this.addRequestedAttachedRef) {
             fac.createOMElement(ADD_REQUESTED_ATTACHED_REF, configElem);
         }
         if(this.addRequestedUnattachedRef) {
             fac.createOMElement(ADD_REQUESTED_UNATTACHED_REF, configElem);
         }
         
         OMElement keyCompElem = fac.createOMElement(KeyComputation.KEY_COMPUTATION, configElem);
         keyCompElem.setText(Integer.toString(this.keyComputation));
         
         OMElement proofKeyTypeElem = fac.createOMElement(PROOF_KEY_TYPE, configElem);
         proofKeyTypeElem.setText(this.proofKeyType);
         
         OMElement trustedServicesElem = fac.createOMElement(TRUSTED_SERVICES, configElem);
         for (Iterator iterator = this.trustedServices.keySet().iterator(); iterator.hasNext();) {
             String service = (String) iterator.next();
             OMElement serviceElem = fac.createOMElement(SERVICE, trustedServicesElem);
             serviceElem.setText(service);
             serviceElem.addAttribute("alias", (String)this.trustedServices.get(service), null);
             
         }
         
         param.setName(SAML_ISSUER_CONFIG.getLocalPart());
         param.setParameterElement(paramElem);
         param.setValue(paramElem);
         param.setParameterType(Parameter.OM_PARAMETER);
         
         return param;
     }
     
     public void setIssuerKeyAlias(String issuerKeyAlias) {
         this.issuerKeyAlias = issuerKeyAlias;
     }
 
     public void setIssuerKeyPassword(String issuerKeyPassword) {
         this.issuerKeyPassword = issuerKeyPassword;
     }
 
     public void setIssuerName(String issuerName) {
         this.issuerName = issuerName;
     }
 
     public void setTrustedServices(Map trustedServices) {
         this.trustedServices = trustedServices;
     }
 
     public void setTrustStorePropFile(String trustStorePropFile) {
         this.trustStorePropFile = trustStorePropFile;
     }
 
     /**
      * Add a new trusted service endpoint address with its certificate
      * @param address Service endpoint address
      * @param alias certificate alias
      */
     public void addTrustedServiceEndpointAddress(String address, String alias) {
         this.trustedServices.put(address, alias);
     }
     
     /**
      * Set crypto information using WSS4J mechanisms
      * 
      * @param providerClassName
      *            Provider class - an implementation of
      *            org.apache.ws.security.components.crypto.Crypto
      * @param props Configuration properties
      */
     public void setCryptoProperties(String providerClassName, Properties props) {
         OMFactory fac = OMAbstractFactory.getOMFactory();
         this.cryptoPropertiesElement= fac.createOMElement(CRYPTO_PROPERTIES);
         OMElement cryptoElem = fac.createOMElement(CRYPTO, this.cryptoPropertiesElement);
         cryptoElem.addAttribute(PROVIDER.getLocalPart(), providerClassName, null);
         Enumeration keys =  props.keys();
         while (keys.hasMoreElements()) {
             String prop = (String) keys.nextElement();
             String value = (String)props.get(prop);
             OMElement propElem = fac.createOMElement(PROPERTY, cryptoElem);
             propElem.setText(value);
             propElem.addAttribute("name", prop, null);
         }
     }
 
     /**
      * Return the list of trusted services as a <code>java.util.Map</code>.
      * The services addresses are the keys and cert aliases available under 
      * those keys. 
      * @return
      */
     public Map getTrustedServices() {
         return trustedServices;
     }
 
 	public SAMLCallbackHandler getCallbackHander() {
 		return callbackHander;
 	}
 
 	public void setCallbackHander(SAMLCallbackHandler callbackHander) {
 		this.callbackHander = callbackHander;
 	}
 
 	
     
 }
