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
 package org.ow2.sirocco.apis.rest.cimi.manager.machine.image;
 
import org.ow2.sirocco.apis.rest.cimi.converter.PathHelper;
import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachine;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineImage;
 import org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerCreateAbstract;
 import org.ow2.sirocco.apis.rest.cimi.manager.MergeReferenceHelper;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  * Manage CREATE request of Machine Image.
  */
 @Component("CimiManagerCreateMachineImage")
 public class CimiManagerCreateMachineImage extends CimiManagerCreateAbstract {
 
     @Autowired
     @Qualifier("IMachineImageManager")
     private IMachineImageManager manager;
 
     @Autowired
     @Qualifier("MergeReferenceHelper")
     private MergeReferenceHelper mergeReference;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#callService(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected Object callService(final CimiContext context, final Object dataService) throws Exception {
        Object out = null;
        String idMachineToCapture = null;
        MachineImage image = (MachineImage) dataService;
        // Extract ID MachineImage of imageLocation and verify it before using
        // the service to create or capture image
        if (null != image.getImageLocation()) {
            idMachineToCapture = PathHelper.extractIdString(image.getImageLocation());
            if (false == image.getImageLocation().equals(context.makeHref(CimiMachine.class, idMachineToCapture))) {
                idMachineToCapture = null;
            }
        }
        // Call services
        if (null != idMachineToCapture) {
            out = this.manager.captureMachine((MachineImage) dataService, idMachineToCapture);
        } else {
            out = this.manager.createMachineImage((MachineImage) dataService);
        }
        return out;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#beforeConvertToDataService(org.ow2.sirocco.apis.rest.cimi.request.CimiContext)
      */
     @Override
     protected void beforeConvertToDataService(final CimiContext context) throws Exception {
         this.mergeReference.merge(context, (CimiMachineImage) context.getRequest().getCimiData());
     }
 }
