 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.powershell.resource;
 
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.ActionsBuilder;
 import com.redhat.rhevm.api.model.ActionValidator;
 import com.redhat.rhevm.api.model.Attachment;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
 import com.redhat.rhevm.api.resource.AttachmentResource;
 
 public class PowerShellAttachmentResource implements AttachmentResource {
 
     private Attachment model;
 
     /**
      * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
      *
      * @param attachment  encapsulated Attachment
      */
     PowerShellAttachmentResource(Attachment attachment) {
         model = attachment;
     }
 
     /**
      * Package-level accessor for encapsulated Attachment
      *
      * @return  encapsulated attachment
      */
     public Attachment getModel() {
         return model;
     }
 
     private void setStorageDomainHref(UriBuilder baseUriBuilder) {
         StorageDomain storageDomain = getModel().getStorageDomain();
 
         String href = PowerShellStorageDomainsResource.getHref(baseUriBuilder, storageDomain.getId());
 
         storageDomain.setHref(href);
     }
 
     private void setDataCenterHref(UriBuilder baseUriBuilder) {
         DataCenter dataCenter = getModel().getDataCenter();
 
         String href = PowerShellDataCentersResource.getHref(baseUriBuilder, dataCenter.getId());
 
         dataCenter.setHref(href);
     }
 
     public Attachment addLinks(UriInfo uriInfo, UriBuilder uriBuilder) {
         UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
 
         getModel().setHref(uriBuilder.build().toString());
 
         setStorageDomainHref(baseUriBuilder);
         setDataCenterHref(baseUriBuilder);
 
         ActionValidator actionValidator = new AttachmentActionValidator(getModel());
         ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, AttachmentResource.class, actionValidator);
         getModel().setActions(actionsBuilder.build());
 
         return getModel();
     }
 
     @Override
     public Attachment get(UriInfo uriInfo) {
         return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public void activate() {
         // FIXME: implement
     }
 
     @Override
     public void deactivate() {
         // FIXME: implement
     }
 
     private class AttachmentActionValidator implements ActionValidator {
         private Attachment attachment;
 
         public AttachmentActionValidator(Attachment attachment) {
             this.attachment = attachment;
         }
 
         @Override
         public boolean validateAction(String action) {
             switch (attachment.getStatus()) {
             case ACTIVE:
                 return action.equals("deactivate");
             case INACTIVE:
                 return action.equals("activate");
             case UNINITIALIZED:
             case UNATTACHED:
             case LOCKED:
             case MIXED:
             default:
                 assert false : attachment.getStatus();
                 return false;
             }
         }
     }
 }
