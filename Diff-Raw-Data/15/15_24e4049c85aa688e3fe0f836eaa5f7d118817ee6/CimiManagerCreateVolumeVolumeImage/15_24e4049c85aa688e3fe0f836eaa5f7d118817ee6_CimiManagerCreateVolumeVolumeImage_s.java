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
 package org.ow2.sirocco.apis.rest.cimi.manager.volume;
 
 import org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerCreateAbstract;
 import org.ow2.sirocco.apis.rest.cimi.manager.MergeReferenceHelper;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.core.api.IVolumeManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  * Manage CREATE request of VolumeVolumeImage.
  */
 @Component("CimiManagerCreateVolumeVolumeImage")
 public class CimiManagerCreateVolumeVolumeImage extends CimiManagerCreateAbstract {
     @Autowired
     @Qualifier("MergeReferenceHelper")
     private MergeReferenceHelper mergeReference;
 
     @Autowired
     @Qualifier("IVolumeManager")
     private IVolumeManager manager;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#callService(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected Object callService(final CimiContext context, final Object dataService) throws Exception {
        // FIXME
        // return
        // this.manager.addVolumeImageToVolume(context.getRequest().getIdParent(),
        // (VolumeVolumeImage) dataService);
        return null;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#beforeConvertToDataService(org.ow2.sirocco.apis.rest.cimi.request.CimiContext)
      */
     @Override
     protected void beforeConvertToDataService(final CimiContext context) throws Exception {
        // FIXME
        // this.mergeReference.merge(context, (CimiVolumeVolumeImage)
        // context.getRequest().getCimiData());
     }
 
 }
