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
 package org.ow2.sirocco.apis.rest.cimi.manager.system;
 
 import javax.ws.rs.core.Response;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiOperation;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiResource;
 import org.ow2.sirocco.apis.rest.cimi.domain.Operation;
import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiSystemNetworkCollectionRoot;
 import org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerReadAbstract;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.core.api.ISystemManager;
 import org.ow2.sirocco.cloudmanager.core.api.QueryResult;
import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemNetwork;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
 * Manage READ request of Networks collection of a System.
  */
@Component("CimiManagerReadSystemNetworkCollection")
 public class CimiManagerReadSystemNetworkPortCollection extends CimiManagerReadAbstract {
 
     @Autowired
     @Qualifier("ISystemManager")
     private ISystemManager manager;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#callService(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected Object callService(final CimiContext context, final Object dataService) throws Exception {
         Object out = null;
         if (false == context.hasParamsForReadingCollection()) {
            out = this.manager.getEntityListFromSystem(context.getRequest().getIdParent(), SystemNetwork.class);
         } else {
             QueryResult<?> result = this.manager.getEntityListFromSystem(context.getRequest().getIdParent(),
                SystemNetwork.class, context.valueOfFirst(), context.valueOfLast(), context.valuesOfFilter(),
                 context.valuesOfSelect());
             out = result.getItems();
         }
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
        CimiSystemNetworkCollectionRoot cimi = (CimiSystemNetworkCollectionRoot) context.convertToCimi(dataService,
            CimiSystemNetworkCollectionRoot.class);
         context.getResponse().setCimiData(cimi);
         context.getResponse().setStatus(Response.Status.OK);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.manager.CimiManagerAbstract#afterConvertToResponse(org.ow2.sirocco.apis.rest.cimi.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected void afterConvertToResponse(final CimiContext context, final Object dataService) {
         super.afterConvertToResponse(context, dataService);
 
         CimiResource resource = (CimiResource) context.getResponse().getCimiData();
         resource.add(new CimiOperation(Operation.ADD.getRel(), resource.getId()));
     }
 }
