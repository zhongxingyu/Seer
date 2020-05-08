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
 package org.ow2.sirocco.cimi.server.manager.network.template;
 
 import javax.inject.Inject;
 import javax.ws.rs.core.Response;
 
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkTemplateCollectionRoot;
 import org.ow2.sirocco.cimi.server.manager.CimiManagerReadAbstract;
 import org.ow2.sirocco.cimi.server.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.core.api.INetworkManager;
 import org.ow2.sirocco.cloudmanager.core.api.QueryResult;
 
 /**
  * Manage READ request of NetworkTemplates collection.
  */
 @org.ow2.sirocco.cimi.server.manager.Manager("CimiManagerReadNetworkTemplateCollection")
 public class CimiManagerReadNetworkTemplateCollection extends CimiManagerReadAbstract {
 
     @Inject
     private INetworkManager manager;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.CimiManagerAbstract#callService(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected Object callService(final CimiContext context, final Object dataService) throws Exception {
         Object out = null;
         if (false == context.hasParamsForReadingCollection()) {
            out = this.manager.getNetworkTemplates();
         } else {
             QueryResult<?> results = this.manager.getNetworkTemplates(context.valueOfFirst(), context.valueOfLast(),
                 context.valuesOfFilter(), context.valuesOfSelect());
             out = results.getItems();
         }
         return out;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.CimiManagerAbstract#convertToResponse(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected void convertToResponse(final CimiContext context, final Object dataService) throws Exception {
         CimiNetworkTemplateCollectionRoot cimi = (CimiNetworkTemplateCollectionRoot) context.convertToCimi(dataService,
             CimiNetworkTemplateCollectionRoot.class);
         context.getResponse().setCimiData(cimi);
         context.getResponse().setStatus(Response.Status.OK);
     }
 }
