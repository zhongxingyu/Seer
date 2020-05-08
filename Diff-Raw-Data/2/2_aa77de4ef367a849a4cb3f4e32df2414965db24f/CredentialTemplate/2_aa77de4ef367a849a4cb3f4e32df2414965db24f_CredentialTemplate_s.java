 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 
 package org.ow2.sirocco.cimi.sdk;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ow2.sirocco.cimi.domain.CimiCredentialTemplate;
 import org.ow2.sirocco.cimi.domain.CimiJob;
 import org.ow2.sirocco.cimi.domain.collection.CimiCredentialTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiCredentialTemplateCollectionRoot;
 import org.ow2.sirocco.cimi.sdk.CimiClient.CimiResult;
 
 /**
  * Configuration values for realizing a Credential resource.
  */
 public class CredentialTemplate extends Resource<CimiCredentialTemplate> {
 
     /**
      * Instantiates a new credential template.
      */
     public CredentialTemplate() {
         super(null, new CimiCredentialTemplate());
     }
 
     CredentialTemplate(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiCredentialTemplate());
         this.cimiObject.setHref(id);
     }
 
     CredentialTemplate(final CimiCredentialTemplate cimiObject) {
         super(null, cimiObject);
     }
 
     CredentialTemplate(final CimiClient cimiClient, final CimiCredentialTemplate cimiObject) {
         super(cimiClient, cimiObject);
     }
 
     /**
      * Deletes this credential template.
      * 
      * @return the job representing this operation or null if the CIMI provider
      *         does not support Jobs
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public Job delete() throws CimiClientException, CimiProviderException {
         String deleteRef = Helper.findOperation("delete", this.cimiObject);
         if (deleteRef == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiJob job = this.cimiClient.deleteRequest(deleteRef);
         if (job != null) {
             return new Job(this.cimiClient, job);
         } else {
             return null;
         }
     }
 
     /**
      * Creates a new credential template.
      * 
      * @param client the CIMI client
      * @param credentialTemplate the credential template tp create
      * @return creation result
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static CreateResult<CredentialTemplate> createCredentialTemplate(final CimiClient client,
         final CredentialTemplate credentialTemplate) throws CimiClientException, CimiProviderException {
         if (client.cloudEntryPoint.getCredentialTemplates() == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiCredentialTemplateCollection credentialTemplateCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getCredentialTemplates().getHref()),
             CimiCredentialTemplateCollectionRoot.class);
         String addRef = Helper.findOperation("add", credentialTemplateCollection);
         if (addRef == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiResult<CimiCredentialTemplate> result = client.postCreateRequest(addRef, credentialTemplate.cimiObject,
             CimiCredentialTemplate.class);
         Job job = result.getJob() != null ? new Job(client, result.getJob()) : null;
         CredentialTemplate credTemplate = result.getResource() != null ? new CredentialTemplate(client, result.getResource())
             : null;
         return new CreateResult<CredentialTemplate>(job, credTemplate);
     }
 
     /**
      * Retrieves the collection of credential templates visible to the client.
      * 
      * @param client the client
      * @param queryParams optional query parameters
      * @return the credential templates
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static List<CredentialTemplate> getCredentialTemplates(final CimiClient client, final QueryParams... queryParams)
         throws CimiClientException, CimiProviderException {
         if (client.cloudEntryPoint.getCredentialTemplates() == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiCredentialTemplateCollection credentialTemplateCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getCredentialTemplates().getHref()),
             CimiCredentialTemplateCollectionRoot.class, queryParams);
 
         List<CredentialTemplate> result = new ArrayList<CredentialTemplate>();
 
         if (credentialTemplateCollection.getCollection() != null) {
             for (CimiCredentialTemplate cimiCredentialTemplate : credentialTemplateCollection.getCollection().getArray()) {
                result.add(CredentialTemplate.getCredentialTemplateByReference(client, cimiCredentialTemplate.getHref()));
             }
         }
         return result;
     }
 
     /**
      * Retrieves the credential template with the given id.
      * 
      * @param client the client
      * @param id the id of the resource
      * @param queryParams optional query parameters
      * @return the credential template by reference
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static CredentialTemplate getCredentialTemplateByReference(final CimiClient client, final String id,
         final QueryParams... queryParams) throws CimiClientException, CimiProviderException {
         return new CredentialTemplate(client, client.getCimiObjectByReference(id, CimiCredentialTemplate.class, queryParams));
     }
 
 }
