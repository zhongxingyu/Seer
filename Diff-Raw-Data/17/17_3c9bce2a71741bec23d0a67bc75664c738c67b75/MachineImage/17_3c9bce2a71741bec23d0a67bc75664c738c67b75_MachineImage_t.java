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
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.ow2.sirocco.cimi.domain.CimiJob;
 import org.ow2.sirocco.cimi.domain.CimiMachineImage;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineImageCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineImageCollectionRoot;
 import org.ow2.sirocco.cimi.sdk.CimiClient.CimiResult;
 
 /**
  * Machine image used to instantiate a Machine.
  */
 public class MachineImage extends Resource<CimiMachineImage> {
 
     /**
      * MachineImage state.
      */
     public static enum State {
         CREATING, AVAILABLE, DELETING, ERROR
     }
 
     /**
      * MachineImage type.
      */
     public static enum Type {
         IMAGE, SNAPSHOT, PARTIAL_SNAPSHOT
     }
 
     /**
      * Instantiates a new machine image.
      */
     public MachineImage() {
         super(null, new CimiMachineImage());
     }
 
     MachineImage(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiMachineImage());
         this.cimiObject.setHref(id);
     }
 
     MachineImage(final CimiClient cimiClient, final CimiMachineImage cimiMachineImage) {
         super(cimiClient, cimiMachineImage);
     }
 
     /**
      * Gets the state of this machine image.
      * 
      * @return the state of this machine image
      */
     public State getState() {
         if (this.cimiObject.getState() != null) {
             return State.valueOf(this.cimiObject.getState().toUpperCase());
         } else {
             return null;
         }
     }
 
     /**
      * Gets the type of this machine image.
      * 
      * @return the type of this machine image
      */
     public Type getType() {
         if (this.cimiObject.getType() != null) {
             return Type.valueOf(this.cimiObject.getType());
         } else {
             return null;
         }
     }
 
     /**
      * Gets the image location of this machine image.
      * 
      * @return the image location of this machine image
      */
     public String getImageLocation() {
        return this.cimiObject.getImageLocation();
     }
 
     /**
      * Sets the image location of this machine image.
      * 
      * @param imageLocation the image location of this machine image
      */
     public void setImageLocation(final String imageLocation) {
        this.cimiObject.setImageLocation(imageLocation);
     }
 
     /**
      * Sets the type of this machine image.
      * 
      * @param type the type of this machine image
      */
     public void setType(final Type type) {
         this.cimiObject.setType(type.toString());
     }
 
     /**
      * Deletes this machine image.
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
      * Creates a new machine image.
      * 
      * @param client the CIMI client
      * @param machineImage the machine image to create
      * @return creation result
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static CreateResult<MachineImage> createMachineImage(final CimiClient client, final MachineImage machineImage)
         throws CimiClientException, CimiProviderException {
         if (client.cloudEntryPoint.getMachineImages() == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiMachineImageCollection machineImagesCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getMachineImages().getHref()), CimiMachineImageCollectionRoot.class);
         String addRef = Helper.findOperation("add", machineImagesCollection);
         if (addRef == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiResult<CimiMachineImage> result = client.postCreateRequest(addRef, machineImage.cimiObject, CimiMachineImage.class);
         Job job = result.getJob() != null ? new Job(client, result.getJob()) : null;
         MachineImage createdMachineImage = result.getResource() != null ? new MachineImage(client, result.getResource()) : null;
         return new CreateResult<MachineImage>(job, createdMachineImage);
     }
 
     /**
      * Updates a machine image.
      * 
      * @param client the client
      * @param id the id
      * @param attributeValues the attribute values
      * @return the update result
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static UpdateResult<MachineImage> updateMachineImage(final CimiClient client, final String id,
         final Map<String, Object> attributeValues) throws CimiClientException, CimiProviderException {
         CimiMachineImage cimiObject = new CimiMachineImage();
         StringBuilder sb = new StringBuilder();
         for (Entry<String, Object> entry : attributeValues.entrySet()) {
             if (sb.length() > 0) {
                 sb.append(",");
             }
             String attribute = entry.getKey();
             sb.append(attribute);
             if (attribute.equals("name")) {
                 cimiObject.setName((String) entry.getValue());
             } else if (attribute.equals("description")) {
                 cimiObject.setDescription((String) entry.getValue());
             } else if (attribute.equals("properties")) {
                 cimiObject.setProperties((Map<String, String>) entry.getValue());
             } else if (attribute.equals("imageLocation")) {
                cimiObject.setImageLocation((String) entry.getValue());
             }
         }
         CimiResult<CimiMachineImage> cimiResult = client.partialUpdateRequest(id, cimiObject, sb.toString());
         Job job = cimiResult.getJob() != null ? new Job(client, cimiResult.getJob()) : null;
         MachineImage machineImage = cimiResult.getResource() != null ? new MachineImage(client, cimiResult.getResource())
             : null;
         return new UpdateResult<MachineImage>(job, machineImage);
     }
 
     /**
      * Retrieves the collection of machine images visible to the client.
      * 
      * @param client the client
      * @param queryParams optional query parameters
      * @return the machine images
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static List<MachineImage> getMachineImages(final CimiClient client, final QueryParams... queryParams)
         throws CimiClientException, CimiProviderException {
         if (client.cloudEntryPoint.getMachineImages() == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiMachineImageCollection machineImagesCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getMachineImages().getHref()), CimiMachineImageCollectionRoot.class,
             queryParams);
 
         List<MachineImage> result = new ArrayList<MachineImage>();
 
         if (machineImagesCollection.getCollection() != null) {
             for (CimiMachineImage cimiMachineImage : machineImagesCollection.getCollection().getArray()) {
                 result.add(new MachineImage(client, cimiMachineImage));
             }
         }
         return result;
     }
 
     /**
      * Retrieves the machine image with the given id.
      * 
      * @param client the client
      * @param id the id of the resource
      * @param queryParams optional query parameters
      * @return the machine image by reference
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static MachineImage getMachineImageByReference(final CimiClient client, final String id,
         final QueryParams... queryParams) throws CimiClientException, CimiProviderException {
         return new MachineImage(client, client.getCimiObjectByReference(id, CimiMachineImage.class, queryParams));
     }
 
 }
