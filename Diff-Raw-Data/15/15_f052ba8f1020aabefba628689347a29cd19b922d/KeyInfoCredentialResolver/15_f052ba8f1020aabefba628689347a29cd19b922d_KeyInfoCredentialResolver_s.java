 /*
  * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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
 
 package org.opensaml.xml.security.keyinfo;
 
 import java.security.Key;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.security.CriteriaSet;
 import org.opensaml.xml.security.SecurityException;
 import org.opensaml.xml.security.credential.AbstractCriteriaFilteringCredentialResolver;
 import org.opensaml.xml.security.credential.Credential;
 import org.opensaml.xml.signature.KeyInfo;
 import org.opensaml.xml.signature.KeyInfoHelper;
 import org.opensaml.xml.signature.KeyName;
 import org.opensaml.xml.signature.KeyValue;
 
 /**
  * Specialized credential resolver interface which resolves credentials based on a {@link KeyInfo}  element.
  * 
  * <p>
  * This resolver requires a {@link KeyInfoCriteria} to be supplied as the resolution criteria.
  * It is permissible, however, for the criteria's KeyInfo to be null.  This allows for more
  * convenient processing logic for example when a parent element allows an optional KeyInfo
  * and in fact a given instance does not contain one.  Specialized subclasses of this resolver
  * may still attempt to return credentials in an implementation or context-specific manner,
  * as described below.
  * </p>
  * 
  * <p>
  * Most of the processing of the KeyInfo and extraction of {@link Credential}'s from the KeyInfo
  * is handled by instances of {@link KeyInfoProvider}.  An ordered list of KeyInfoProviders must be
  * supplied to the resolver when it is constructed.
  * </p>
  * 
  * <p>
  * Processing of the supplied KeyInfo element proceeds as follows:
  * <ol>
  *   <li>A {@link KeyInfoResolutionContext} is instantiated.  This resolution context is used to hold state shared
  *       amongst all the providers and processing hooks which run within the resolver.</li>
  *   <li>This resolution context is initialized and populated with the actual KeyInfo object being processed as
  *       well as the values of any {@link KeyName} child elements present.</li>
  *   <li>An attempt is then made to resolve a credential from any {@link KeyValue} child elements as described for
 *       {@link #resolveKeyValue(org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver.KeyInfoResolutionContext, CriteriaSet, List)
  *       If a credential is so resolved, it will also be placed in the resolution context</li>
  *   <li>The remaining (non-KeyValue) children are then processed in document order.  Each child element is
  *       processed by the registered providers in provider list order. The credential or credentials resolved by the
  *       first provider to successfully do so are added to the effective set of credentials returned by the resolver,
  *       and processing of that child element terminates.  Processing continues with the next child element.</li>
  *   <li>At this point all KeyInfo children have been processed.  If the effective set of credentials to return 
  *       is empty, if a credential was resolved from a KeyValue element and available in the resolution context,
  *       it is added to the effective set. Since the KeyInfo may have a plain KeyValue representation of the key
  *       represented by the KeyInfo, in addition to a more specific key type/container (and hence credential)
  *       representation, this technique avoids the unnecessary return of duplicate keys, returning only the more
  *       specific credential representation of the key.</li>
  *   <li>A post-processing hook is then called: {@link #postProcess(org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver.KeyInfoResolutionContext, CriteriaSet, List)}.
  *       The default implementation is a no-op.  This is an extension point by which subclasses may implement custom
  *       post-processing of the effective credential set to be returned.  One example use case is when the KeyInfo
  *       being processed represents the public aspects (e.g. public key, or a key name or other identifier) of an
  *       encryption key belonging to the resolving entity.  The resolved public keys and other resolution context
  *       information may be used to further resolve the credential or credentials containing the associated decryption
  *       key (i.e. a private or symmetric key). For an example of such an implementation,
  *       see {@link LocalKeyInfoCredentialResolver}</li>
  *   <li>Finally, if no credentials have been otherwise resolved, a final post-processing hook is called: 
  *       {@link #postProcessEmptyCredentials(org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver.KeyInfoResolutionContext, CriteriaSet, List)}.
  *       The default implementation is a no-op.  This is an extension point by which subclasses may implement
  *       custom logic to resolve credentials in an implementation or context-specific manner, if no other
  *       mechanism has succeeded.  Example usages might be to return a default set of credentials,
  *       or to use non-KeyInfo-derived criteria or contextual information to determine the credential
  *       or credentials to return.</li>
  *   <li></li>
  * </ol>
  * </p>
  * 
  */
 public class KeyInfoCredentialResolver extends AbstractCriteriaFilteringCredentialResolver {
     
     /** Class logger. */
     private static Logger log = Logger.getLogger(KeyInfoCredentialResolver.class);
 
     /** List of KeyInfo providers that are registered on this instance.  */
     private List<KeyInfoProvider> providers;
     
     /**
      * Constructor.
      *
      * @param keyInfoProviders the list of KeyInfoProvider's to use in this resolver
      */
     public KeyInfoCredentialResolver(List<KeyInfoProvider> keyInfoProviders) {
         super();
         
         providers = new ArrayList<KeyInfoProvider>();
         providers.addAll(keyInfoProviders);
     }
     
     /**
      * Return the list of the KeyInfoProvider instances used in this resolver configuration.
      * 
      * @return the list of providers configured for this resolver instance
      */
     protected List<KeyInfoProvider> getProviders() {
         return providers;
     }
 
     /** {@inheritDoc} */
     protected Iterable<Credential> resolveFromSource(CriteriaSet criteriaSet) throws SecurityException {
         KeyInfoCriteria kiCriteria = criteriaSet.get(KeyInfoCriteria.class);
         if (kiCriteria == null) {
             log.error("No KeyInfo criteria supplied, resolver could not process");
             throw new SecurityException("Credential criteria set did not contain an instance of" 
                     + "KeyInfoCredentialCriteria");
         }
         KeyInfo keyInfo = kiCriteria.getKeyInfo();
         
         // This will be the list of credentials to return.
         List<Credential> credentials = new ArrayList<Credential>();
  
         KeyInfoResolutionContext kiContext = new KeyInfoResolutionContext(credentials);
         
         // Note: we allow KeyInfo to be null to handle case where application context, 
         // other accompanying criteria, etc, should be used to resolve credentials via hooks below.
         if (keyInfo != null) {
             // Initialize the resolution context that will be used by the provider plugins.
             // This processes the KeyName and the KeyValue children, if either are present.
             initResolutionContext(kiContext, keyInfo, criteriaSet);
             
             // Now process all (non-KeyValue) children
             processKeyInfoChildren(kiContext, criteriaSet, credentials);
             
             if (credentials.isEmpty()) {
                 // Add the credential based on plain KeyValue if no more specifc cred type was found
                 if (kiContext.getKeyValueCredential() != null) {
                     log.debug("No credentials extracted by registered non-KeyValue handling providers, " 
                             + "adding KeyValue credential to returned credential set");
                     credentials.add(kiContext.getKeyValueCredential());
                 }
             }
         } else {
             log.info("KeyInfo was null, any credentials will be resolved by post-processing hooks only");
         }
         
         // Postprocessing hook
         postProcess(kiContext, criteriaSet, credentials);
         
         // Final empty credential hook
         if (credentials.isEmpty()) {
             log.debug("No credentials were found, calling empty credentials post-processing hook");
             postProcessEmptyCredentials(kiContext, criteriaSet, credentials);
         }
         
         if (log.isDebugEnabled()) {
             log.debug("A total of " + credentials.size() + " credentials were resolved");
         }
         return credentials;
     }
 
     /**
      * Hook for subclasses to do post-processing of the credential set after all KeyInfo children have been processed.
      * 
      * For example, the previously resolved credentials might be used to index into a store
      * of local credentials, where the index is a key name or the public half of a key pair
      * extracted from the KeyInfo.
      * 
      * @param kiContext KeyInfo resolution context containing 
      * @param criteriaSet the credential criteria used to resolve credentials
      * @param credentials the list which will store the resolved credentials
      * @throws SecurityException thrown if there is an error during processing
      */
     protected void postProcess(KeyInfoResolutionContext kiContext, CriteriaSet criteriaSet, 
             List<Credential> credentials) throws SecurityException {
         
     }
 
     /**
      * Hook for processing the case where no credentials were returned by any resolution method
      * by any provider, nor by the processing of the
      * {@link #postProcess(org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver.KeyInfoResolutionContext, CriteriaSet, List)}
      * hook.
      * 
      * @param kiContext KeyInfo resolution context containing 
      * @param criteriaSet the credential criteria used to resolve credentials
      * @param credentials the list which will store the resolved credentials
      * 
      * @throws SecurityException thrown if there is an error during processing
      */
     protected void postProcessEmptyCredentials(KeyInfoResolutionContext kiContext,
             CriteriaSet criteriaSet, List<Credential> credentials) throws SecurityException {
         
     }
 
     /**
      * Use registered providers to process the non-KeyValue children of KeyInfo.
      * 
      * Each child element is processed in document order. Each child element is processed
      * by each provider in the ordered list of providers.  The credential or credentials
      * resolved by the first provider to successfully do so are added to the effective set
      * resolved by the KeyInfo resolver.
      * 
      * @param kiContext KeyInfo resolution context containing 
      * @param criteriaSet the credential criteria used to resolve credentials
      * @param credentials the list which will store the resolved credentials
      * @throws SecurityException thrown if there is a provider error processing the KeyInfo children
      */
     private void processKeyInfoChildren(KeyInfoResolutionContext kiContext, CriteriaSet criteriaSet,
             List<Credential> credentials) throws SecurityException {
         
         for (XMLObject keyInfoChild : kiContext.getKeyInfo().getXMLObjects()) {
             
             if (keyInfoChild instanceof KeyValue) {
                 continue;
             }
             
             if (log.isDebugEnabled()) {
                 log.debug("Processing KeyInfo child with qname: " + keyInfoChild.getElementQName());
             }
             
             Collection<Credential> childCreds = processKeyInfoChild(kiContext, criteriaSet, keyInfoChild);
             
             if (childCreds != null && ! childCreds.isEmpty()) {
                credentials.addAll(childCreds);
             } else {
                 // Not really an error or warning if KeyName doesn't produce a credential
                 if (keyInfoChild instanceof KeyName) {
                     if (log.isDebugEnabled()) {
                         log.debug("KeyName with value '" + ((KeyName) keyInfoChild).getValue() 
                                 + "' did not independently produce a credential based on any registered providers");
                     }
                     
                 } else {
                     log.warn("No credentials could be extracted from KeyInfo child with qname " 
                             + keyInfoChild.getElementQName() + " by any registered provider");
                 }
             }
         }
     }
 
     /**
      * Process the given KeyInfo child with the registered providers.
      * 
      * The child element is processed by each provider in the ordered list of providers.
      * The credential or credentials resolved by the first provider to successfully do so are
      * returned and processing of the child element is terminated.
      * 
      * @param kiContext KeyInfo resolution context containing 
      * @param criteriaSet the credential criteria used to resolve credentials
      * @param keyInfoChild the KeyInfo to evaluate
      * @return the collection of resolved credentials, or null
      * @throws SecurityException thrown if there is a provider error processing the KeyInfo child
      */
     private Collection<Credential> processKeyInfoChild(KeyInfoResolutionContext kiContext, 
             CriteriaSet criteriaSet, XMLObject keyInfoChild) throws SecurityException {
         
         for (KeyInfoProvider provider : getProviders()) {
             
             if (!provider.handles(keyInfoChild)) {
                 if (log.isDebugEnabled()) {
                    log.debug("Provider " + provider.getClass().getName() + " doesn't handle objects of type " 
                            + keyInfoChild.getElementQName() + ", skipping") ;
                 }
                 continue;
             }
             
             if (log.isDebugEnabled()) {
                 log.debug("Processing KeyInfo child " + keyInfoChild.getElementQName() 
                         + " with provider " + provider.getClass().getName());
             }
             
             Collection<Credential> creds = provider.process(this, keyInfoChild, criteriaSet, kiContext);
             
             if (creds != null && ! creds.isEmpty()) {
                 if (log.isDebugEnabled()) {
                     log.debug("Credentials (count = " + creds.size() + ") successfully extracted from child " 
                             + keyInfoChild.getElementQName() + " by provider " + provider.getClass().getName());
                 }
                 return creds;
             }
         }
         return null;
     }
     
     /**
      * Initialize the resolution context that will be used by the providers.
      * 
      * The supplied KeyInfo object is stored in the context, as well as
      * the values of any {@link KeyName} children present.  Finally
      * if a credential is resolveble by any registered provider from a
      * plain {@link KeyValue} child, that credential is also stored
      * in the context.
      * 
      * @param kiContext KeyInfo resolution context containing 
      * @param keyInfo the KeyInfo to evaluate
      * @param criteriaSet the credential criteria used to resolve credentials
      * @throws SecurityException thrown if there is an error processing the KeyValue children
      */
     private void initResolutionContext(KeyInfoResolutionContext kiContext, KeyInfo keyInfo, 
             CriteriaSet criteriaSet) throws SecurityException {
         
         kiContext.setKeyInfo(keyInfo);
         
         // Extract all KeyNames
         kiContext.setKeyNames(KeyInfoHelper.getKeyNames(keyInfo));
         if (log.isDebugEnabled()) {
             log.debug("Found " + kiContext.getKeyNames().size() + " key names: " + kiContext.getKeyNames());
         }
         
         // Extract the Credential based on the (singular) key from an existing KeyValue(s).
         resolveKeyValue(kiContext, criteriaSet, keyInfo.getKeyValues());
     }
 
     /**
      * Resolve the key from any KeyValue element that may be present, and store the resulting 
      * credential in the resolution context.
      * 
      * Each KeyValue element is processed in turn in document order. Each Keyvalue will be processed 
      * by each provider in the ordered list of registered providers. The first credential successfully
      * resolved from a KeyValue will be stored in the resolution context.
      * 
      * Note: This resolver implementation assumes that KeyInfo/KeyValue will not be abused via-a-vis the Signature
      * specificiation, and that therefore all KeyValue elements (if there is even more than one) will all resolve to
      * the same key value. The KeyInfo might, for example have multiple KeyValue children, containing different
      * representations of the same key.  Therefore, only the first credential derived from a KeyValue will be be
      * utilized.
      * 
      * @param kiContext KeyInfo resolution context 
      * @param criteriaSet the credential criteria used to resolve credentials
      * @param keyValues the KeyValue children to evaluate
      * @throws SecurityException  thrown if there is an error resolving the key from the KeyValue
      */
     protected void resolveKeyValue(KeyInfoResolutionContext kiContext, CriteriaSet criteriaSet, 
             List<KeyValue> keyValues)  throws SecurityException {
         
         for (KeyValue keyValue : keyValues) {
             Collection<Credential> creds = processKeyInfoChild(kiContext, criteriaSet, keyValue);
             if (creds != null && ! creds.isEmpty()) {
                 kiContext.setKeyValueCredential( creds.iterator().next() );
                 if (log.isDebugEnabled()) {
                     Key key = extractKeyValue(kiContext.getKeyValueCredential());
                     log.debug("Found a credential based on a KeyValue having key type: " 
                             + key.getAlgorithm());
                 }
                 return;
             }
         }
     }
     
     /**
      * Build a credential context based on the current KeyInfo context, for return 
      * in a resolved credential.
      * 
      * @param kiContext the current KeyInfo resolution context
      * 
      * @return a new KeyInfo credential context
      */
     public KeyInfoCredentialContext buildCredentialContext(KeyInfoResolutionContext kiContext) {
         // Simple for now, might do other stuff later.
         // Just want to provide a single place to build credential contexts for
         // the resolver and providers.
         return new KeyInfoCredentialContext(kiContext.getKeyInfo());
     }
 
     /**
      * Utility method to extract any key that might be present in the specified Credential.
      * 
      * @param cred the Credential to evaluate
      * @return the Key contained in the credential, or null if it does not contain a key.
      */
     protected Key extractKeyValue(Credential cred) {
         if (cred == null) {
             return null;
         }
         if (cred.getPublicKey() != null) {
             return cred.getPublicKey();
         } 
         // This could happen if key is derived, e.g. key agreement, etc
         if (cred.getSecretKey() != null) {
             return cred.getSecretKey();
         }
         // Perhaps unlikely, but go ahead and check
         if (cred.getPrivateKey() != null) {
             return cred.getPrivateKey(); 
         }
         return null;
     }
     
     /**
      *  Resolution context class that can be used to supply information to the providers and processing
      *  hooks within a given invocation of the resolver.
      *  
      *  <p>
      *  This resolution context is used to hold state shared amongst all the providers and processing hooks
      *  which run within the resolver.
      *  </p>
      *  
      *  <p>
      *  The extensible properties map available from {@link #getProperties()} may for example used to communicate
      *  state between two or more providers, or between a provider and custom logic in one of the resolver's
      *  post-processing hooks. It is recommended that providers and/or hooks define and use property names
      *  in such a way as to avoid collistions with those used by other providers and hooks, and to also
      *  clearly define the data type stored for each propery name.
      *  </p>
      *  
      */
     public class KeyInfoResolutionContext {
         
         /** The KeyInfo being processed. */
         private KeyInfo keyInfo;
         
         /** List of key names. */
         private List<String> names;
         
         /** The credential resolved based on a KeValue, if any.  */
         private Credential credential;
         
         /** This list provides a KeyInfo provider plugin access to credentials that 
          * may have already been resolved by a plugin earlier in the chain. */
         private Collection<Credential> resolvedCredentials;
         
         /** Extensible map of properties used to share state amongst providers and/or post-processing hooks. */
         private final Map<String, Object> properties;
         
         /**
          * Constructor.
          * 
          * @param credentials a reference to the collection in which the KeyInfo credential resolver 
          *          will store resolved credentials.
          */
         public KeyInfoResolutionContext(Collection<Credential> credentials) {
             //TODO should we make access to previously resolved creds modifiable by provider plugins?
             resolvedCredentials = Collections.unmodifiableCollection(credentials);
             properties = new HashMap<String, Object>();
         }
 
         /**
          * Gets the KeyInfo being processed.
          * 
          * @return Returns the keyInfo.
          */
         public KeyInfo getKeyInfo() {
             return keyInfo;
         }
 
         /**
          * Sets the KeyInfo being processed.
          * 
          * @param newKeyInfo The keyInfo to set.
          */
         public void setKeyInfo(KeyInfo newKeyInfo) {
             keyInfo = newKeyInfo;
         }
 
         /**
          * Get the names obtained from any KeyNames.
          * 
          * @return Returns the keyNames.
          */
         public List<String> getKeyNames() {
             return names;
         }
 
         /**
          * Set the names from any KeyNames.
          * 
          * @param keyNames The KeyNames to set.
          */
         public void setKeyNames(List<String> keyNames) {
             this.names = keyNames;
         }
 
         /**
          * Get the credential holding the key obtained from a KeyValue, if any.
          * 
          * @return Returns the keyValueCredential.
          */
         public Credential getKeyValueCredential() {
             return credential;
         }
 
         /**
          * Set the credential holding the key obtained from a KeyValue, if any.
          * 
          * @param keyValueCredential The credential to set.
          */
         public void setKeyValueCredential(Credential keyValueCredential) {
             this.credential = keyValueCredential;
         }
         
         /**
          * Get the set of credentials already resolved by other providers.
          * 
          * @return Returns the keyValueCredential.
          */
         public Collection<Credential> getResolvedCredentials() {
             return resolvedCredentials;
         }
         
         /**
          * Get the extensible properties map.
          * 
          * @return Returns the properties.
          */
         public Map<String, Object> getProperties() {
             return properties;
         }
     }
 
 }
