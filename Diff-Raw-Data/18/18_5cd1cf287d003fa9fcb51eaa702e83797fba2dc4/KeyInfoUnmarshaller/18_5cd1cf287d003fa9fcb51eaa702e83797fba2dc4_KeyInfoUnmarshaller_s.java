 /*
  * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensaml.xml.signature;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.security.Init;
 import org.apache.xml.security.exceptions.XMLSecurityException;
 import org.apache.xml.security.keys.content.X509Data;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.io.Unmarshaller;
 import org.opensaml.xml.io.UnmarshallingException;
 import org.w3c.dom.Element;
 
 /**
 * A marshaller for {@link org.opensaml.xml.signature.impl.KeyInfoImpl} objects. This class, along with it's
 * respective builder and unmarshaller use the Apache XMLSec 1.3 APIs to perform signing and verification.
  */
 public class KeyInfoUnmarshaller implements Unmarshaller {
 
     /** Logger */
     private static final Logger LOG = Logger.getLogger(KeyInfoUnmarshaller.class);
 
     /**
      * Constructor
      */
     public KeyInfoUnmarshaller() {
         if (!Init.isInitialized()) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Initializing XML security library");
             }
             Init.init();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public XMLObject unmarshall(Element element) throws UnmarshallingException {
        KeyInfo keyInfoObj = new KeyInfo(element.getNamespaceURI(), element.getLocalName(), element
                .getPrefix());
 
         try {
             org.apache.xml.security.keys.KeyInfo keyInfoElem = new org.apache.xml.security.keys.KeyInfo(element, "#");
 
             int numOfKeyNames = keyInfoElem.lengthKeyName();
             for (int i = 0; i < numOfKeyNames; i++) {
                 keyInfoObj.getKeyNames().add(keyInfoElem.itemKeyName(i).getKeyName());
             }
 
             int numOfKeys = keyInfoElem.lengthKeyValue();
            if(numOfKeys > 1){
                LOG.warn("KeyInfo element contains more than one public key, only the first one will be used");
             }
            keyInfoObj.setPublicKey(keyInfoElem.itemKeyValue(0).getPublicKey());
 
             int numOfX509Data = keyInfoElem.lengthX509Data();
             int numOfCerts;
             X509Data x509Data;
             for (int i = 0; i < numOfX509Data; i++) {
                 x509Data = keyInfoElem.itemX509Data(i);
                 numOfCerts = x509Data.lengthCertificate();
                 for (int j = 0; j < numOfCerts; j++) {
                     keyInfoObj.getCertificates().add(x509Data.itemCertificate(j).getX509Certificate());
                 }
             }
 
             return keyInfoObj;
         } catch (XMLSecurityException e) {
             throw new UnmarshallingException("Unable to build XMLSec KeyInfo object from KeyInfo XML element");
         }
     }
 }
