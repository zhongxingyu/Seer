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
 
 import java.util.Collection;
 
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.security.CriteriaSet;
 import org.opensaml.xml.security.SecurityException;
 import org.opensaml.xml.security.credential.Credential;
 import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver.KeyInfoResolutionContext;
 
 /**
  * Interface for providers of {@link KeyInfoCredentialResolver} which support extracting a {@link Credential} from 
  * a child element of {@link KeyInfo} elements.
  */
 public interface KeyInfoProvider {
     
     /**
      * Process a specified KeyInfo child (XMLobject) and attempt to resolve a credential from it.
      * 
      * @param resolver reference to the resolver which is calling the provider
      * @param keyInfoChild the KeyInfo child being processed
      * @param criteriaSet the credential criteria the credential must satisfy
      * @param kiContext the resolution context, used for sharing state between the resolver and the providers
      * 
      * @return a resolved Credential collection, or null
      * 
      * @throws SecurityException if there is an error during credential resolution.  
      *          Note: failure to resolve a credential is not an error.
      */
     public Collection<Credential> process(KeyInfoCredentialResolver resolver, XMLObject keyInfoChild, 
             CriteriaSet criteriaSet, KeyInfoResolutionContext kiContext) throws SecurityException;
     
     /**
      * Evaluate whether the given provider should attempt to handle resolving a credential
      * from the specified KeyInfo child.
      * 
      * An evaluation of <code>true</code> does not guarantee that a credential can or will be 
      * extracted form the particular KeyInfo child, only that processing should be attempted.
      * 
      * @param keyInfoChild the KeyInfo child object to consider
      * 
      * @return true if the provider should attempt to resolve credentials, false otherwise
      */
     public boolean handles(XMLObject keyInfoChild);
 
 }
