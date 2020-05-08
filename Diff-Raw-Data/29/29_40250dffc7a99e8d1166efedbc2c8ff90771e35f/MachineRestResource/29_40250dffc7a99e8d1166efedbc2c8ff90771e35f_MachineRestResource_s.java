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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.resource;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiAction;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachine;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineCreate;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineDisk;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineNetworkInterface;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineNetworkInterfaceAddress;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineVolume;
 import org.ow2.sirocco.apis.rest.cimi.manager.CimiManager;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.apis.rest.cimi.request.ContextHelper;
 import org.ow2.sirocco.apis.rest.cimi.request.IdRequest;
 import org.ow2.sirocco.apis.rest.cimi.request.ResponseHelper;
 import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  * Machine REST resource.
  * <p>
  * Operations supports :
  * <ul>
  * <li>Create a machine</li>
  * <li>Delete a machine</li>
  * <li>Read a machine</li>
  * <li>Read a collection of machines</li>
  * <li>Update a machine</li>
  * </ul>
  * </p>
  */
 @Component
 @Path(ConstantsPath.MACHINE_PATH)
 public class MachineRestResource extends RestResourceAbstract {
 
     @Autowired
     @Qualifier("CimiManagerReadMachine")
     private CimiManager cimiManagerReadMachine;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineCollection")
     private CimiManager cimiManagerReadMachineCollection;
 
     @Autowired
     @Qualifier("CimiManagerDeleteMachine")
     private CimiManager cimiManagerDeleteMachine;
 
     @Autowired
     @Qualifier("CimiManagerUpdateMachine")
     private CimiManager cimiManagerUpdateMachine;
 
     @Autowired
     @Qualifier("CimiManagerCreateMachine")
     private CimiManager cimiManagerCreateMachine;
 
     @Autowired
     @Qualifier("CimiManagerActionMachine")
     private CimiManager cimiManagerActionMachine;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineDisk")
     private CimiManager cimiManagerReadMachineDisk;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineDiskCollection")
     private CimiManager cimiManagerReadMachineDiskCollection;
 
     @Autowired
     @Qualifier("CimiManagerCreateMachineDisk")
     private CimiManager cimiManagerCreateMachineDisk;
 
     @Autowired
     @Qualifier("CimiManagerDeleteMachineDisk")
     private CimiManager cimiManagerDeleteMachineDisk;
 
     @Autowired
     @Qualifier("CimiManagerUpdateMachineDisk")
     private CimiManager cimiManagerUpdateMachineDisk;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineNetworkInterface")
     private CimiManager cimiManagerReadMachineNetworkInterface;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineNetworkInterfaceCollection")
     private CimiManager cimiManagerReadMachineNetworkInterfaceCollection;
 
     @Autowired
     @Qualifier("CimiManagerCreateMachineNetworkInterface")
     private CimiManager cimiManagerCreateMachineNetworkInterface;
 
     @Autowired
     @Qualifier("CimiManagerDeleteMachineNetworkInterface")
     private CimiManager cimiManagerDeleteMachineNetworkInterface;
 
     @Autowired
     @Qualifier("CimiManagerUpdateMachineNetworkInterface")
     private CimiManager cimiManagerUpdateMachineNetworkInterface;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineNetworkInterfaceAddress")
     private CimiManager cimiManagerReadMachineNetworkInterfaceAddress;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineNetworkInterfaceAddressCollection")
     private CimiManager cimiManagerReadMachineNetworkInterfaceAddressCollection;
 
     @Autowired
     @Qualifier("CimiManagerCreateMachineNetworkInterfaceAddress")
     private CimiManager cimiManagerCreateMachineNetworkInterfaceAddress;
 
     @Autowired
     @Qualifier("CimiManagerDeleteMachineNetworkInterfaceAddress")
     private CimiManager cimiManagerDeleteMachineNetworkInterfaceAddress;
 
     @Autowired
     @Qualifier("CimiManagerUpdateMachineNetworkInterfaceAddress")
     private CimiManager cimiManagerUpdateMachineNetworkInterfaceAddress;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineVolume")
     private CimiManager cimiManagerReadMachineVolume;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineVolumeCollection")
     private CimiManager cimiManagerReadMachineVolumeCollection;
 
     @Autowired
     @Qualifier("CimiManagerCreateMachineVolume")
     private CimiManager cimiManagerCreateMachineVolume;
 
     @Autowired
     @Qualifier("CimiManagerDeleteMachineVolume")
     private CimiManager cimiManagerDeleteMachineVolume;
 
     @Autowired
     @Qualifier("CimiManagerUpdateMachineVolume")
     private CimiManager cimiManagerUpdateMachineVolume;
 
     /**
      * Get a machine.
      * 
      * @param id The ID of machine to get
      * @return The REST response
      */
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path("/{id}")
     public Response read(@PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), id);
         this.cimiManagerReadMachine.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Get a collection of machines.
      * 
      * @return The REST response
      */
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response read() {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos());
         this.cimiManagerReadMachineCollection.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Update a machine.
      * 
      * @param id The ID of machine to update
      * @param cimiData Data to update
      * @return The REST response
      */
     @PUT
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path("/{id}")
     public Response update(@PathParam("id") final String id, final CimiMachine cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), id, cimiData);
         this.cimiManagerUpdateMachine.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Create a machine.
      * 
      * @param cimiData Data to create
      * @return The REST response
      */
     @POST
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response create(final CimiMachineCreate cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), cimiData);
         this.cimiManagerCreateMachine.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Actions on machine.
      * 
      * @return The REST response
      */
     @POST
     @Path("/{id}")
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response action(@PathParam("id") final String id, final CimiAction cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), id, cimiData);
         this.cimiManagerActionMachine.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Delete a machine.
      * 
      * @param id The ID of machine to delete
      * @return The REST response
      */
     @DELETE
     @Path("/{id}")
     public Response delete(@PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), id);
         this.cimiManagerDeleteMachine.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a collection of disks of a machine.
      * 
      * @param idParent ID machine
      * @return The REST response
      */
     @GET
     @Path("{idParent}" + ConstantsPath.DISK_PATH)
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readDisks(@PathParam("idParent") final String idParent) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent));
         this.cimiManagerReadMachineDiskCollection.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a disk of a machine.
      * 
      * @param idParent ID machine
      * @param id ID disk to read
      * @return The REST response
      */
     @GET
     @Path("/{idParent}" + ConstantsPath.DISK_PATH + "/{id}")
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readDisk(@PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent));
         this.cimiManagerReadMachineDisk.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Update a disk of a machine.
      * 
      * @param idParent ID machine
      * @param id ID disk to update
      * @param cimiData Data to update
      * @return The REST response
      */
     @PUT
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path("/{idParent}" + ConstantsPath.DISK_PATH + "/{id}")
     public Response updateDisk(@PathParam("idParent") final String idParent, @PathParam("id") final String id,
         final CimiMachineDisk cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent), cimiData);
         this.cimiManagerUpdateMachineDisk.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Create a disk of a machine.
      * 
      * @param idParent ID machine
      * @param cimiData Data to create
      * @return The REST response
      */
     @POST
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path("/{idParent}" + ConstantsPath.DISK_PATH)
     public Response createDisk(@PathParam("idParent") final String idParent, final CimiMachineDisk cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent), cimiData);
         this.cimiManagerCreateMachineDisk.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Delete a disk of a machine.
      * 
      * @param idParent ID machine
      * @param id ID disk to delete
      * @return The REST response
      */
     @DELETE
     @Path("/{idParent}" + ConstantsPath.DISK_PATH + "/{id}")
     public Response deleteDisk(@PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent));
         this.cimiManagerDeleteMachineDisk.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a collection of volumes of a machine.
      * 
      * @param idParent ID machine
      * @return The REST response
      */
     @GET
     @Path("{idParent}" + ConstantsPath.VOLUME_PATH)
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readVolumes(@PathParam("idParent") final String idParent) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent));
         this.cimiManagerReadMachineVolumeCollection.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a volume of a machine.
      * 
      * @param idParent ID machine
      * @param id ID volume to read
      * @return The REST response
      */
     @GET
     @Path("/{idParent}" + ConstantsPath.VOLUME_PATH + "/{id}")
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readVolume(@PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent));
         this.cimiManagerReadMachineVolume.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Update a volume of a machine.
      * 
      * @param idParent ID machine
      * @param id ID volume to update
      * @param cimiData Data to update
      * @return The REST response
      */
     @PUT
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path("/{idParent}" + ConstantsPath.VOLUME_PATH + "/{id}")
     public Response updateVolume(@PathParam("idParent") final String idParent, @PathParam("id") final String id,
         final CimiMachineVolume cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent), cimiData);
         this.cimiManagerUpdateMachineVolume.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Create a volume of a machine.
      * 
      * @param idParent ID machine
      * @param cimiData Data to create
      * @return The REST response
      */
     @POST
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path("/{idParent}" + ConstantsPath.VOLUME_PATH)
     public Response createVolume(@PathParam("idParent") final String idParent, final CimiMachineVolume cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent), cimiData);
         this.cimiManagerCreateMachineVolume.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Delete a volume of a machine.
      * 
      * @param idParent ID machine
      * @param id ID volume to delete
      * @return The REST response
      */
     @DELETE
     @Path("/{idParent}" + ConstantsPath.VOLUME_PATH + "/{id}")
     public Response deleteVolume(@PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent));
         this.cimiManagerDeleteMachineVolume.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a collection of networkInterfaces of a machine.
      * 
      * @param idParent ID machine
      * @return The REST response
      */
     @GET
    @Path("{idParent}" + ConstantsPath.NETWORK_PATH)
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readNetworkInterfaces(@PathParam("idParent") final String idParent) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent));
         this.cimiManagerReadMachineNetworkInterfaceCollection.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a networkInterface of a machine.
      * 
      * @param idParent ID machine
      * @param id ID networkInterface to read
      * @return The REST response
      */
     @GET
    @Path("/{idParent}" + ConstantsPath.NETWORK_PATH + "/{id}")
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readNetworkInterface(@PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent));
         this.cimiManagerReadMachineNetworkInterface.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Update a networkInterface of a machine.
      * 
      * @param idParent ID machine
      * @param id ID networkInterface to update
      * @param cimiData Data to update
      * @return The REST response
      */
     @PUT
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{idParent}" + ConstantsPath.NETWORK_PATH + "/{id}")
     public Response updateNetworkInterface(@PathParam("idParent") final String idParent, @PathParam("id") final String id,
         final CimiMachineNetworkInterface cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent), cimiData);
         this.cimiManagerUpdateMachineNetworkInterface.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Create a networkInterface of a machine.
      * 
      * @param idParent ID machine
      * @param cimiData Data to create
      * @return The REST response
      */
     @POST
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{idParent}" + ConstantsPath.NETWORK_PATH)
     public Response createNetworkInterface(@PathParam("idParent") final String idParent,
         final CimiMachineNetworkInterface cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent), cimiData);
         this.cimiManagerCreateMachineNetworkInterface.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Delete a networkInterface of a machine.
      * 
      * @param idParent ID machine
      * @param id ID networkInterface to delete
      * @return The REST response
      */
     @DELETE
    @Path("/{idParent}" + ConstantsPath.NETWORK_PATH + "/{id}")
     public Response deleteNetworkInterface(@PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(id, idParent));
         this.cimiManagerDeleteMachineNetworkInterface.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a collection of addresses of a networkInterface of a machine.
      * 
      * @param idGrandParent ID machine
      * @param idParent ID networkInterface in machine
      * @return
      */
     @GET
    @Path("{idGrandParent}" + ConstantsPath.NETWORK_PATH + "/{idParent}" + ConstantsPath.ADDRESS_PATH)
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readAddresses(@PathParam("idGrandParent") final String idGrandParent,
         @PathParam("idParent") final String idParent) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent,
             idGrandParent));
         this.cimiManagerReadMachineNetworkInterfaceAddressCollection.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Read a address of a networkInterface of a machine.
      * 
      * @param idGrandParent ID machine
      * @param idParent ID networkInterface in machine
      * @param id ID address to read
      * @return The REST response
      */
     @GET
    @Path("{idGrandParent}" + ConstantsPath.NETWORK_PATH + "/{idParent}" + ConstantsPath.ADDRESS_PATH + "/{id}")
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response readAddress(@PathParam("idGrandParent") final String idGrandParent,
         @PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(),
             new IdRequest(id, idParent, idGrandParent));
         this.cimiManagerReadMachineNetworkInterfaceAddress.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Create a address of a networkInterface of a machine.
      * 
      * @param idGrandParent ID machine
      * @param idParent ID networkInterface in machine
      * @param cimiData Data to create
      * @return The REST response
      */
     @POST
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{idGrandParent}" + ConstantsPath.NETWORK_PATH + "/{idParent}" + ConstantsPath.ADDRESS_PATH)
     public Response createAddress(@PathParam("idGrandParent") final String idGrandParent,
         @PathParam("idParent") final String idParent, final CimiMachineNetworkInterfaceAddress cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(), new IdRequest(null, idParent,
             idGrandParent), cimiData);
         this.cimiManagerCreateMachineNetworkInterfaceAddress.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Delete a address of a networkInterface of a machine.
      * 
      * @param idGrandParent ID machine
      * @param idParent ID networkInterface in machine
      * @param id ID address to delete
      * @return The REST response
      */
     @DELETE
    @Path("{idGrandParent}" + ConstantsPath.NETWORK_PATH + "/{idParent}" + ConstantsPath.ADDRESS_PATH + "/{id}")
     public Response deleteAddress(@PathParam("idGrandParent") final String idGrandParent,
         @PathParam("idParent") final String idParent, @PathParam("id") final String id) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(),
             new IdRequest(id, idParent, idGrandParent));
         this.cimiManagerDeleteMachineNetworkInterfaceAddress.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
     /**
      * Update a address of a networkInterface of a machine.
      * 
      * @param idGrandParent ID machine
      * @param idParent ID networkInterface in machine
      * @param id ID address to update
      * @param cimiData Data to update
      * @return The REST response
      */
     @PUT
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{idGrandParent}" + ConstantsPath.NETWORK_PATH + "/{idParent}" + ConstantsPath.ADDRESS_PATH + "/{id}")
     public Response updateAddress(@PathParam("idGrandParent") final String idGrandParent,
         @PathParam("idParent") final String idParent, @PathParam("id") final String id,
         final CimiMachineNetworkInterfaceAddress cimiData) {
         CimiContext context = ContextHelper.buildContext(this.getJaxRsRequestInfos(),
             new IdRequest(id, idParent, idGrandParent), cimiData);
         this.cimiManagerUpdateMachineNetworkInterfaceAddress.execute(context);
         return ResponseHelper.buildResponse(context.getResponse());
     }
 
 }
