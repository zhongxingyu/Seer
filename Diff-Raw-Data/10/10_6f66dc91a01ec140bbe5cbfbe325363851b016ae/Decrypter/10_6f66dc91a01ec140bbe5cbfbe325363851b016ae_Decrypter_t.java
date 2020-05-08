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
 
 package org.opensaml.xml.encryption;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.security.Key;
 import java.security.KeyException;
import java.util.HashMap;
 import java.util.List;
 
 import javolution.util.FastList;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.security.encryption.XMLCipher;
 import org.apache.xml.security.encryption.XMLEncryptionException;
 import org.opensaml.xml.Configuration;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.io.Marshaller;
 import org.opensaml.xml.io.MarshallingException;
 import org.opensaml.xml.io.UnmarshallerFactory;
 import org.opensaml.xml.io.UnmarshallingException;
 import org.opensaml.xml.parse.ParserPool;
 import org.opensaml.xml.parse.XMLParserException;
 import org.opensaml.xml.security.KeyInfoResolver;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Decrypts XMLObject and their keys.
  */
 public class Decrypter {
     
     /** ParserPool used in parsing decrypted data. */
     private final ParserPool parserPool;
     
     /** Unmarshaller factory, used in decryption of EncryptedData objects. */
     private UnmarshallerFactory unmarshallerFactory;
     
     /** Load-and-Save DOM Implementation singleton. */
     //private DOMImplementationLS domImplLS;
     
     /** Class logger. */
     private Logger log = Logger.getLogger(Decrypter.class);
     
     /** Resolver for data encryption keys. */
     private KeyInfoResolver resolver;
     
     /** Resolver for key encryption keys. */
     private KeyInfoResolver kekResolver;
     
     /**
      * Constructor.
      *
      * @param newKEKResolver resolver for key encryption keys.
      * @param newResolver resolver for data encryption keys.
      */
     public Decrypter(KeyInfoResolver newKEKResolver, KeyInfoResolver newResolver) {
         kekResolver = newKEKResolver;
         resolver = newResolver;
        
         parserPool = new ParserPool();
        parserPool.setNamespaceAware(true);
        
        HashMap<String, Boolean> features = new HashMap<String, Boolean>();
        features.put("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE); 
        parserPool.setFeatures(features);
        
         
         unmarshallerFactory = Configuration.getUnmarshallerFactory();
     }
     
     public Decrypter(KeyInfoResolver newKEKResolver, KeyInfoResolver newResolver, ParserPool pool){
         kekResolver = newKEKResolver;
         resolver = newResolver;
         parserPool = pool;
     }
     
     /**
      * Set a new key encryption key resolver.
      * 
      * @param newKEKResolver the new key encryption key resolver
      */
     public void setKEKREsolver(KeyInfoResolver newKEKResolver) {
         this.kekResolver = newKEKResolver;
     }
     
     /**
      * Set a new data encryption key resolver.
      * 
      * @param newResolver the new data encryption key resolver
      */
     public void setKeyResolver(KeyInfoResolver newResolver) {
         this.resolver = newResolver;
     }
     
     /**
      * Decrypts the supplied EncryptedData and returns the resulting XMLObject.
      * 
      * This will only succeed if the decrypted EncryptedData contains exactly one
      * DOM node of type Element.
      * 
      * @param encryptedData encrypted data element containing the data to be decrypted
      * @return the decrypted XMLObject
      * @throws DecryptionException exception indicating a decryption error, possibly because
      *          the decrypted data contained more than one top-level Element, or some
      *          non-Element Node type.
      */
     public XMLObject decryptData(EncryptedData encryptedData) throws DecryptionException {
         
         List<XMLObject> xmlObjects = decryptDataToList(encryptedData);
         if (xmlObjects.size() != 1) {
             throw new DecryptionException("The decrypted data contained more than one XMLObject child");
         }
         
         return xmlObjects.get(0);
     }
     
     /**
      * Decrypts the supplied EncryptedData and returns the resulting list of XMLObjects.
      * 
      * This will only succeed if the decrypted EncryptedData contains at the top-level
      * only DOM Elements (not other types of DOM Nodes).
      * 
      * @param encryptedData encrypted data element containing the data to be decrypted
      * @return the list decrypted top-level XMLObjects
      * @throws DecryptionException exception indicating a decryption error, possibly because
      *          the decrypted data contained DOM nodes other than type of Element
      */
     public List<XMLObject> decryptDataToList(EncryptedData encryptedData) throws DecryptionException {
         List<XMLObject> xmlObjects = new FastList<XMLObject>();
         
         DocumentFragment docFragment = decryptDataToDOM(encryptedData);
         
         XMLObject xmlObject;
         Node node;
         Element element;
         
         NodeList children = docFragment.getChildNodes();
         for (int i=0; i<children.getLength(); i++) {
             node = children.item(i);
             if (node.getNodeType() != Node.ELEMENT_NODE) {
                 throw new DecryptionException("Top-level node was not of type Element: " + node.getNodeType());
             } else {
                 element = (Element) node;
             }
             
             try {
                 xmlObject = unmarshallerFactory.getUnmarshaller(element).unmarshall(element);
             } catch (UnmarshallingException e) {
                 throw new DecryptionException("Unmarshalling error during decryption", e);
             }
             
             xmlObjects.add(xmlObject);
         }
         
         return xmlObjects;
     }
     /**
      * Decrypts the supplied EncryptedData and returns the resulting DOM {@link DocumentFragment}.
      * 
      * @param encryptedData encrypted data element containing the data to be decrypted
      * @return the decrypted DOM {@link DocumentFragment}
      * @throws DecryptionException exception indicating a decryption error
      */
     public DocumentFragment decryptDataToDOM(EncryptedData encryptedData) throws DecryptionException {
         if (resolver == null && kekResolver == null) {
             throw new DecryptionException("Unable to decrypt EncryptedData, no key resolvers are set");
         }
         
         // TODO - Until Xerces supports LSParser.parseWithContext(), or we come up with another solution
         // to parse a bytestream into a DocumentFragment, we can only support encryption of Elements,
         // not content.
         if (encryptedData.getType().equals(EncryptionConstants.TYPE_CONTENT)) {
             throw new DecryptionException("Decryption of EncryptedData elements of type 'Content' " 
                     + "is not currently supported");
         }
         
         // This is the key that will ultimately be used to decrypt the EncryptedData
         Key dataEncKey = resolveDataDecryptionKey(encryptedData);
         
         if (dataEncKey == null) {
             throw new DecryptionException("Unable to resolve the data encryption key");
         }
         
         Element targetElement = encryptedData.getDOM();
         if (targetElement == null) {
            Marshaller marshaller = 
                Configuration.getMarshallerFactory().getMarshaller(EncryptedData.DEFAULT_ELEMENT_NAME);
            try {
                targetElement = marshaller.marshall(encryptedData);
            } catch (MarshallingException e) {
                throw new DecryptionException("Error marshalling EncryptedData for decryption", e);
            }
         }
         
         XMLCipher xmlCipher;
         try {
             xmlCipher = XMLCipher.getInstance();
             xmlCipher.init(XMLCipher.DECRYPT_MODE, dataEncKey);
         } catch (XMLEncryptionException e) {
             throw new DecryptionException("Error initialzing cipher instance on data decryption", e);
         }
         
         byte[] bytes = null;
         try {
             bytes = xmlCipher.decryptToByteArray(targetElement);
         } catch (XMLEncryptionException e) {
             throw new DecryptionException("Error decrypting the encrypted data element", e);
         }
         if (bytes == null) {
             throw new DecryptionException("EncryptedData could not be decrypted");
         }
  
         ByteArrayInputStream input = new ByteArrayInputStream(bytes);
         DocumentFragment docFragment = parseInputStream(input, encryptedData.getDOM().getOwnerDocument());
         
         return docFragment;
     }
     
     /**
      * Decrypts the supplied EncryptedKey and returns the resulting Java security Key object.
      * The algorithm of the decrypted key must be supplied by the caller based on knowledge of
      * the associated EncryptedData information.
      * 
      * @param encryptedKey encrypted key element containing the encrypted key to be decrypted
      * @param algorithm  the algorithm associated with the decrypted key
      * @return the decrypted key
      * @throws DecryptionException exception indicating a decryption error
      */
     public Key decryptKey(EncryptedKey encryptedKey, String algorithm) throws DecryptionException {
         if (kekResolver == null) {
             throw new DecryptionException("Unable to decrypt EncryptedKey, no key encryption key resolver is set");
         }
         
         Element targetElement = encryptedKey.getDOM();
         if (targetElement == null) {
            Marshaller marshaller = 
                Configuration.getMarshallerFactory().getMarshaller(EncryptedKey.DEFAULT_ELEMENT_NAME);
            try {
                targetElement = marshaller.marshall(encryptedKey);
            } catch (MarshallingException e) {
                throw new DecryptionException("Error marshalling EncryptedKey for decryption", e);
            }
         }
         
         Key kek = null;
         try {
             kek = kekResolver.resolveKey(encryptedKey.getKeyInfo());
         } catch (KeyException e) {
             throw new DecryptionException("Error resolving the key encryption key", e);
         }
         if (kek == null) {
             throw new DecryptionException("Failed to resolve key encryption key");
         }
         
         XMLCipher xmlCipher;
         try {
             xmlCipher = XMLCipher.getInstance();
             xmlCipher.init(XMLCipher.UNWRAP_MODE, kek);
         } catch (XMLEncryptionException e) {
             throw new DecryptionException("Error initialzing cipher instance on key decryption", e);
         }
         
         org.apache.xml.security.encryption.EncryptedKey encKey; 
         try {
             encKey = xmlCipher.loadEncryptedKey(targetElement.getOwnerDocument(), targetElement);
         } catch (XMLEncryptionException e) {
             throw new DecryptionException("Error when loading library native encrypted key representation", e);
         }
         
         Key key = null;
         try {
             key = xmlCipher.decryptKey(encKey, algorithm);
         } catch (XMLEncryptionException e) {
             throw new DecryptionException("Error decrypting kek encryption key", e);
         }
         if (key == null) {
             throw new DecryptionException("Key could not be decrypted");
         }
         return key;
     }
     
     /**
      * Resolve the data decryption key for the specified EncryptedData element.
      * 
      * @param encryptedData the EncryptedData which is to be decrypted
      * @return the data decryption key
      * @throws DecryptionException exception indicating a decryption error
      */
     private Key resolveDataDecryptionKey(EncryptedData encryptedData) throws DecryptionException {
         Key dataEncKey = null;
         
         // This logic is pretty much straight from the C++ decrypter...
         
         // First try and resolve the data encryption key directly
         if (resolver != null) {
             try {
                 dataEncKey = resolver.resolveKey(encryptedData.getKeyInfo());
             } catch (KeyException e) {
                 throw new DecryptionException("Error resolving data encryption key", e);
             }
         }
         
         // If no key was resolved and have a KEK resolver, try to resolve keys from
         // KeyInfo/EncryptedKey's obtained from directly within this EncryptedData.
         if (dataEncKey == null && kekResolver != null) {
             String algorithm = encryptedData.getEncryptionMethod().getAlgorithm();
             if (algorithm == null || algorithm.equals("")) {
                 throw new DecryptionException("No EncryptionMethod/@Algorithm set, key decryption cannot proceed.");
             }
            
             if (encryptedData.getKeyInfo() != null) {
                 List<EncryptedKey> encryptedKeys = encryptedData.getKeyInfo().getEncryptedKeys();
                 for (EncryptedKey encryptedKey: encryptedKeys) {
                     try {
                         dataEncKey = decryptKey(encryptedKey, algorithm);
                         if (dataEncKey != null) {
                             break;
                         }
                     } catch (DecryptionException e) {
                        log.warn(e.getMessage());
                     }
                 }
             }
             
             // If no key was resolved, and our data encryption key resolver is an EncryptedKey resolver
             // specialization capable of resolving EncryptedKey's from another context, try that.
             if (dataEncKey == null && resolver instanceof EncryptedKeyInfoResolver) {
                 EncryptedKeyInfoResolver ekr = (EncryptedKeyInfoResolver) resolver;
                 EncryptedKey encryptedKey = null;
                 try {
                     encryptedKey = ekr.resolveKey(encryptedData);
                 } catch (KeyException e) {
                     throw new DecryptionException("Error in EncryptedKeyInfoResolver", e);
                 }
                 if (encryptedKey != null) {
                     try {
                         dataEncKey = decryptKey(encryptedKey, algorithm); 
                     } catch (DecryptionException e) {
                         log.warn(e.getMessage());
                     }
                 }
             }
         }
         
         return dataEncKey;
     }
 
     /* NOTE: this currently won't work because Xerces doesn't implement LSParser.parseWithContext().
      * Hopefully they will in the future.
      */
     /*
     private DocumentFragment parseInputStreamLS(InputStream input, Document owningDocument)
             throws DecryptionException {
         
         DOMImplementationLS domImpl = getDOMImplemenationLS();
         
         LSParser parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
         if (parser == null) {
             throw new DecryptionException("LSParser was null");
         }
         
         //DOMConfiguration config=parser.getDomConfig();
         //DOMErrorHandlerImpl errorHandler=new DOMErrorHandlerImpl();
         //config.setParameter("error-handler", errorHandler);
         
         LSInput lsInput = domImpl.createLSInput();
         if (lsInput == null) {
             throw new DecryptionException("LSInput was null");
         }
         lsInput.setByteStream(input);
         
         DocumentFragment container = owningDocument.createDocumentFragment();
         //TODO Xerces currently doesn't support LSParser.parseWithContext()
         parser.parseWithContext(lsInput, container, LSParser.ACTION_REPLACE_CHILDREN);
         
         return container;
     }
     */
     
     /*
     private DOMImplementationLS getDOMImplemenationLS() throws DecryptionException {
         if (domImplLS != null) {
             return domImplLS;
         }
 
         // get an instance of the DOMImplementation registry
         DOMImplementationRegistry registry;
         try {
             registry = DOMImplementationRegistry.newInstance();
         } catch (ClassCastException e) {
             throw new DecryptionException("Error creating new error of DOMImplementationRegistry", e);
         } catch (ClassNotFoundException e) {
             throw new DecryptionException("Error creating new error of DOMImplementationRegistry", e);
         } catch (InstantiationException e) {
             throw new DecryptionException("Error creating new error of DOMImplementationRegistry", e);
         } catch (IllegalAccessException e) {
             throw new DecryptionException("Error creating new error of DOMImplementationRegistry", e);
         }
 
         // get a new instance of the DOM Level 3 Load/Save implementation
         DOMImplementationLS newDOMImplLS = (DOMImplementationLS) registry.getDOMImplementation("LS 3.0");
         if (newDOMImplLS == null) {
             throw new DecryptionException("No LS DOMImplementation could be found");
         } else {
             domImplLS = newDOMImplLS;
         }
 
         return domImplLS;
     }
     */
 
     
     /**
      * Parse the specified input stream in a DOM DocumentFragment, owned by the specified
      * Document.
      * 
      * @param input the InputStream to parse
      * @param owningDocument the Document which will own the returned DocumentFragment
      * @return a DocumentFragment
      * @throws DecryptionException thrown if there is an error parsing the input stream
      */
     private DocumentFragment parseInputStream(InputStream input, Document owningDocument) throws DecryptionException {
         // Since Xerces currently seems not to handle parsing into a DocumentFragment
         // without a bit hackery,  use this to simulate, so we can keep the API 
         // the way it hopefully will look in the future.  Obviously this only works for 
         // input streams containing valid XML instances, not fragments.
         
         Document newDocument = null;
         try {
             newDocument = parserPool.parse(input);
         } catch (XMLParserException e) {
             throw new DecryptionException("Error parsing decrypted input stream", e);
         }
         
         Element element = newDocument.getDocumentElement();
         owningDocument.adoptNode(element);
         
         DocumentFragment container = owningDocument.createDocumentFragment();
         container.appendChild(element);
 
         return container;
     }
 }
