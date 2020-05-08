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
 package org.ow2.sirocco.apis.rest.cimi.manager.cep;
 
 import javax.ws.rs.core.Response;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCloudEntryPoint;
 import org.ow2.sirocco.apis.rest.cimi.domain.CloudEntryPointAggregate;
 import org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerReadAbstract;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.core.api.ICredentialsManager;
 import org.ow2.sirocco.cloudmanager.core.api.IJobManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
 import org.ow2.sirocco.cloudmanager.core.api.ISystemManager;
 import org.ow2.sirocco.cloudmanager.core.api.IVolumeManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  * Manage READ request of Cloud Entry Point.
  */
 @Component("CimiManagerReadCloudEntryPoint")
 public class CimiManagerReadCloudEntryPoint extends CimiManagerReadAbstract {
 
     @Autowired
     @Qualifier("IMachineManager")
     private IMachineManager machineManager;
 
     @Autowired
     @Qualifier("ICredentialsManager")
     private ICredentialsManager credentialsManager;
 
     @Autowired
     @Qualifier("IMachineImageManager")
     private IMachineImageManager machineImageManager;
 
     @Autowired
     @Qualifier("IJobManager")
     private IJobManager jobManager;
 
     @Autowired
     @Qualifier("IVolumeManager")
     private IVolumeManager volumeManager;
 
     @Autowired
     @Qualifier("ISystemManager")
     private ISystemManager systemManager;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#callService(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected Object callService(final CimiContext context, final Object dataService) throws Exception {
         CloudEntryPointAggregate out = new CloudEntryPointAggregate(this.machineManager.getCloudEntryPoint());
 
         out.setCredentials(this.credentialsManager.getCredentials());
         out.setCredentialsTemplates(this.credentialsManager.getCredentialsTemplates());
 
         out.setJobs(this.jobManager.getJobs());
 
         out.setMachineConfigs(this.machineManager.getMachineConfigurations());
         out.setMachineImages(this.machineImageManager.getMachineImages());
         out.setMachines(this.machineManager.getMachines());
         out.setMachineTemplates(this.machineManager.getMachineTemplates());
 
         out.setSystems(this.systemManager.getSystems());
         out.setSystemTemplates(this.systemManager.getSystemTemplates());
 
         out.setVolumeConfigurations(this.volumeManager.getVolumeConfigurations());
         out.setVolumeImages(this.volumeManager.getVolumeImages());
         out.setVolumes(this.volumeManager.getVolumes());
         out.setVolumeTemplates(this.volumeManager.getVolumeTemplates());
 
         // TODO Others resources :
         // resourceMetadata
         // systems OK
         // systemTemplates OK
         // machines OK
         // machineTemplates OK
         // machineConfigs OK
         // machineImages OK
         // credentials OK
         // credentialsTemplates OK
         // volumes OK
         // volumeTemplates OK
         // volumeConfigs OK
         // volumeImages OK
         // networks
         // networkTemplates
         // networkConfigs
         // networkPorts
         // networkPortTemplates
         // networkPortConfigs
         // addresses
         // addressTemplates
         // forwardingGroups
         // forwardingGroupTemplates
         // jobs OK
         // meters
         // meterTemplates
         // meterConfigs
         // eventLogs
         // events
 
         return out;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#convertToResponse(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected void convertToResponse(final CimiContext context, final Object dataService) throws Exception {
         CimiCloudEntryPoint cimi = (CimiCloudEntryPoint) context.convertToCimi(dataService, CimiCloudEntryPoint.class);
         context.getResponse().setCimiData(cimi);
         context.getResponse().setStatus(Response.Status.OK);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerReadAbstract#afterConvertToResponse(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected void afterConvertToResponse(final CimiContext context, final Object dataService) {
         super.afterConvertToResponse(context, dataService);
        // XXX UnsupportedOperation
        // CimiResource resource = (CimiResource)
        // context.getResponse().getCimiData();
        // resource.add(new CimiOperation(Operation.EDIT.getRel(),
        // resource.getId()));
     }
 }
