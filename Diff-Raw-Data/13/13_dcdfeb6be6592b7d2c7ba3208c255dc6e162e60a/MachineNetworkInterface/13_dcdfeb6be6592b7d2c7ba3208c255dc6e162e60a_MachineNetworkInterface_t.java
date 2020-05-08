 /**
  *
  * SIROCCO
  * Copyright (C) 2012 France Telecom
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
 
 import org.ow2.sirocco.cimi.domain.CimiJob;
 import org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterface;
 import org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterfaceAddress;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineNetworkInterfaceAddressCollectionRoot;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineNetworkInterfaceCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineNetworkInterfaceCollectionRoot;
 import org.ow2.sirocco.cimi.sdk.CimiClient.CimiResult;
 
 /**
  * Network interface of a Machine.
  */
 public class MachineNetworkInterface extends Resource<CimiMachineNetworkInterface> {
 
     /** A unique URI denoting this resource type */
     public static final String TYPE_URI = "http://schemas.dmtf.org/cimi/1/MachineNetworkInterface";
 
     /**
      * NIC state.
      */
     public static enum State {
         Active, Passive, Disabled
     }
 
     /**
      * NIC type.
      */
     public static enum Type {
         PUBLIC, PRIVATE
     }
 
     /**
      * Instantiates a new machine network interface.
      */
     public MachineNetworkInterface() {
         super(null, new CimiMachineNetworkInterface());
     }
 
     MachineNetworkInterface(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiMachineNetworkInterface());
         this.cimiObject.setHref(id);
     }
 
     MachineNetworkInterface(final CimiClient cimiClient, final CimiMachineNetworkInterface cimiMachineNetworkInterface) {
         super(cimiClient, cimiMachineNetworkInterface);
     }
 
     /**
      * Gets the addresses of this network interface.
      * 
      * @return the addresses
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public List<Address> getAddresses() throws CimiClientException, CimiProviderException {
         String href = this.cimiObject.getAddresses().getHref();
         if (href == null) {
             href = this.cimiObject.getAddresses().getId();
         }
         CimiMachineNetworkInterfaceAddressCollectionRoot addresses = this.cimiClient.getRequest(
             this.cimiClient.extractPath(href), CimiMachineNetworkInterfaceAddressCollectionRoot.class, QueryParams.builder()
                 .expand("address").build());
         this.cimiObject.setAddresses(addresses);
         List<Address> result = new ArrayList<Address>();
         if (this.cimiObject.getAddresses().getArray() != null) {
             for (CimiMachineNetworkInterfaceAddress addr : this.cimiObject.getAddresses().getArray()) {
                 Address address = new Address(this.cimiClient, addr.getAddress());
                 result.add(address);
             }
         }
         return result;
     }
 
     /**
      * Gets the network of this network interface.
      * 
      * @return the network
      */
     public Network getNetwork() {
         return this.cimiObject.getNetwork() != null ? new Network(this.cimiClient, this.cimiObject.getNetwork()) : null;
     }
 
     /**
      * Gets the state of this network interface.
      * 
      * @return the state of this network interface
      */
     public State getState() {
         return State.valueOf(this.cimiObject.getState());
     }
 
     /**
      * Gets the type of this network interface.
      * 
      * @return the type of this network interface
      */
     public Type getType() {
        if (this.cimiObject.getNetworkType() == null) {
            return null;
        }
         return Type.valueOf(this.cimiObject.getNetworkType());
     }
 
     /**
      * Gets the mac address.
      * 
      * @return the mac address
      */
     public String getMacAddress() {
         return this.cimiObject.getMacAddress();
     }
 
     /**
      * Gets the mtu.
      * 
      * @return the mtu
      */
     public int getMtu() {
         return this.cimiObject.getMtu();
     }
 
     /**
      * Deletes this network interface.
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
      * Creates a new machine network interface.
      * 
      * @param client the CIMI client
      * @param machineId the machine id
      * @param machineNetworkInterface the machine network interface to create
      * @return creation result
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static CreateResult<MachineNetworkInterface> createMachineNetworkInterface(final CimiClient client,
         final String machineId, final MachineNetworkInterface machineNetworkInterface) throws CimiClientException,
         CimiProviderException {
         Machine machine = Machine.getMachineByReference(client, machineId, QueryParams.builder().expand("networkInterfaces")
             .build());
         String addRef = Helper.findOperation("add", machine.cimiObject.getNetworkInterfaces());
         if (addRef == null) {
             throw new CimiClientException("Unsupported operation");
         }
         CimiResult<CimiMachineNetworkInterface> result = client.postCreateRequest(addRef, machineNetworkInterface.cimiObject,
             CimiMachineNetworkInterface.class);
         Job job = result.getJob() != null ? new Job(client, result.getJob()) : null;
         MachineNetworkInterface createdMachineNetworkInterface = result.getResource() != null ? new MachineNetworkInterface(
             client, result.getResource()) : null;
         return new CreateResult<MachineNetworkInterface>(job, createdMachineNetworkInterface);
     }
 
     /**
      * Retrieves the machine network interfaces belonging to a given machine.
      * 
      * @param client the client
      * @param machineId the machine id
      * @param queryParams optional query parameters
      * @return the machine network interfaces
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static List<MachineNetworkInterface> getMachineNetworkInterfaces(final CimiClient client, final String machineId,
         final QueryParams... queryParams) throws CimiClientException, CimiProviderException {
         Machine machine = Machine.getMachineByReference(client, machineId);
         if (machine.cimiObject.getNetworkInterfaces() == null) {
             throw new CimiClientException("Unsupported operation");
         }
 
         CimiMachineNetworkInterfaceCollection machineNetworkInterfaceCollection = client.getRequest(
             client.extractPath(machine.cimiObject.getNetworkInterfaces().getHref()),
             CimiMachineNetworkInterfaceCollectionRoot.class, queryParams);
         List<MachineNetworkInterface> result = new ArrayList<MachineNetworkInterface>();
 
         if (machineNetworkInterfaceCollection.getCollection() != null) {
             for (CimiMachineNetworkInterface cimiMachineNetworkInterface : machineNetworkInterfaceCollection.getCollection()
                 .getArray()) {
                 result.add(new MachineNetworkInterface(client, cimiMachineNetworkInterface));
             }
         }
         return result;
     }
 
     /**
      * Retrieves the machine network interface with the given id.
      * 
      * @param client the client
      * @param id the id of the resource
      * @param queryParams optional query parameters
      * @return the machine network interface by reference
      * @throws CimiClientException If any internal errors are encountered inside
      *         the client while attempting to make the request or handle the
      *         response. For example if a network connection is not available.
      * @throws CimiProviderException If an error response is returned by the
      *         CIMI provider indicating either a problem with the data in the
      *         request, or a server side issue.
      */
     public static MachineNetworkInterface getMachineNetworkInterfaceByReference(final CimiClient client, final String id,
         final QueryParams... queryParams) throws CimiClientException, CimiProviderException {
         return new MachineNetworkInterface(client, client.getCimiObjectByReference(id, CimiMachineNetworkInterface.class,
             queryParams));
     }
 
 }
