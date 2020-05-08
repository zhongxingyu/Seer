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
 
import java.util.HashMap;

 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Attachment;
 import com.redhat.rhevm.api.model.Attachments;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.model.StorageDomainStatus;
 import com.redhat.rhevm.api.resource.AttachmentResource;
 import com.redhat.rhevm.api.resource.AttachmentsResource;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 public class PowerShellAttachmentsResource implements AttachmentsResource {
 
     private String storageDomainId;
 
     public PowerShellAttachmentsResource(String storageDomainId) {
         this.storageDomainId = storageDomainId;
     }
 
     private static Attachment buildAttachment(String dataCenterId, String storageDomainId, StorageDomainStatus status) {
         Attachment attachment = new Attachment();
 
         attachment.setDataCenter(new DataCenter());
         attachment.getDataCenter().setId(dataCenterId);
 
         attachment.setStorageDomain(new StorageDomain());
         attachment.getStorageDomain().setId(storageDomainId);
 
         attachment.setStatus(status);
 
         return attachment;
     }
 
     @Override
     public Attachments list(UriInfo uriInfo) {
         Attachments ret = new Attachments();
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("get-datacenter -storagedomainid " + storageDomainId);
 
         for (DataCenter dataCenter : PowerShellDataCenterResource.runAndParse(buf.toString())) {
             buf = new StringBuilder();
 
             buf.append("get-storagedomain");
             buf.append(" -datacenterid " + dataCenter.getId());
             buf.append(" -storagedomainid " + storageDomainId);
 
             StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());
 
             // FIXME: we need the "Status" property not "SharedStatus" here
             Attachment attachment = buildAttachment(dataCenter.getId(), storageDomainId, storageDomain.getStatus());
 
             PowerShellAttachmentResource resource = new PowerShellAttachmentResource(attachment);
             UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(dataCenter.getId());
             ret.getAttachments().add(resource.addLinks(uriInfo, uriBuilder));
         }
 
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, Attachment attachment) {
         DataCenter dataCenter = attachment.getDataCenter();
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("attach-storagedomain");
         buf.append(" -datacenterid " + dataCenter.getId());
         buf.append(" -storagedomainid " + storageDomainId);
 
         StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());
 
         // FIXME: we need the "Status" property not "SharedStatus" here
         attachment = buildAttachment(dataCenter.getId(), storageDomainId, storageDomain.getStatus());
 
         PowerShellAttachmentResource resource = new PowerShellAttachmentResource(attachment);
 
         UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(dataCenter.getId());
 
         attachment = resource.addLinks(uriInfo, uriBuilder);
 
         return Response.created(uriBuilder.build()).entity(attachment).build();
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("detach-storagedomain");
         buf.append(" -datacenterid " + id);
         buf.append(" -storagedomainid " + storageDomainId);
 
         PowerShellUtils.runCommand(buf.toString());
     }
 
     @Override
     public AttachmentResource getAttachmentSubResource(UriInfo uriInfo, String id) {
         Attachment attachment = buildAttachment(id, storageDomainId, null);
 
         return new PowerShellAttachmentResource(attachment);
     }
 
     /**
      * Build a URI for any existing attachment to the given data center
      *
      * @param uriInfo  URI context of the current request
      * @param dataCenterId  the ID of the data center
      * @return  a URI representing the attachment
      */
     public String getAttachmentHref(UriInfo uriInfo, String dataCenterId) {
 
         // FIXME: check whether the storage domain is actually attached to this data center
 
         UriBuilder uriBuilder =
             uriInfo.getBaseUriBuilder().path("storagedomains").path(storageDomainId)
                                        .path("attachments").path(dataCenterId);
 
         return uriBuilder.build().toString();
     }
 }
