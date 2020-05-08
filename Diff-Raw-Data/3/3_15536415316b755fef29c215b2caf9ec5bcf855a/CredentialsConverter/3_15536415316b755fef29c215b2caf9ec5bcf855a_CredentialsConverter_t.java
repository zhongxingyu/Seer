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
 package org.ow2.sirocco.apis.rest.cimi.converter;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCredentials;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.model.cimi.Credentials;
 
 /**
  * Convert the data of the CIMI model and the service model in both directions.
  * <p>
  * Converted classes:
  * <ul>
  * <li>CIMI model: {@link CimiCredentials}</li>
  * <li>Service model: {@link Credentials}</li>
  * </ul>
  * </p>
  */
 public class CredentialsConverter extends ObjectCommonConverter {
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#toCimi(org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object)
      */
     @Override
     public Object toCimi(final CimiContext context, final Object dataService) {
         CimiCredentials cimi = new CimiCredentials();
         this.copyToCimi(context, dataService, cimi);
         return cimi;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#copyToCimi(org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object, java.lang.Object)
      */
     @Override
     public void copyToCimi(final CimiContext context, final Object dataService, final Object dataCimi) {
         this.doCopyToCimi(context, (Credentials) dataService, (CimiCredentials) dataCimi);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#toService(org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object)
      */
     @Override
     public Object toService(final CimiContext context, final Object dataCimi) {
         Credentials service = new Credentials();
         this.copyToService(context, dataCimi, service);
         return service;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#copyToService
      *      (org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object, java.lang.Object)
      */
     @Override
     public void copyToService(final CimiContext context, final Object dataCimi, final Object dataService) {
         this.doCopyToService(context, (CimiCredentials) dataCimi, (Credentials) dataService);
     }
 
     /**
      * Copy data from a service object to a CIMI object.
      * 
      * @param context The current context
      * @param dataService Source service object
      * @param dataCimi Destination CIMI object
      */
     protected void doCopyToCimi(final CimiContext context, final Credentials dataService, final CimiCredentials dataCimi) {
         this.fill(context, dataService, dataCimi);
         if (true == context.mustBeExpanded(dataCimi)) {
             dataCimi.setUserName(dataService.getUserName());
            dataCimi.setKey(dataService.getPublicKey());
             // Next write only
             if (true == context.isConvertedWriteOnly()) {
                 dataCimi.setPassword(dataService.getPassword());
             }
         }
     }
 
     /**
      * Copy data from a CIMI object to a service object.
      * 
      * @param context The current context
      * @param dataCimi Source CIMI object
      * @param dataService Destination Service object
      */
     protected void doCopyToService(final CimiContext context, final CimiCredentials dataCimi, final Credentials dataService) {
         this.fill(context, dataCimi, dataService);
         dataService.setPublicKey(dataCimi.getKey());
         dataService.setPassword(dataCimi.getPassword());
         dataService.setUserName(dataCimi.getUserName());
     }
 }
