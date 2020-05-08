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
 
 import java.util.ArrayList;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Attachment;
 import com.redhat.rhevm.api.model.Attachments;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.resource.AttachmentResource;
 import com.redhat.rhevm.api.resource.AttachmentsResource;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.powershell.util.PowerShellException;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 
 public class PowerShellAttachmentsResource implements AttachmentsResource {
 
     private String storageDomainId;
     private Executor executor;
     private PowerShellPoolMap shellPools;
 
     public PowerShellAttachmentsResource(String storageDomainId, PowerShellPoolMap shellPools) {
         this.storageDomainId = storageDomainId;
         this.shellPools = shellPools;
     }
 
     public static Attachment buildAttachment(DataCenter dataCenter, StorageDomain storageDomain) {
         Attachment attachment = new Attachment();
 
         attachment.setId(dataCenter.getId());
         attachment.setName(dataCenter.getId());
 
         attachment.setDataCenter(new DataCenter());
         attachment.getDataCenter().setId(dataCenter.getId());
 
         attachment.setStorageDomain(new StorageDomain());
         attachment.getStorageDomain().setId(storageDomain.getId());
 
         attachment.setStatus(storageDomain.getStatus());
 
         if (storageDomain.isSetMaster()) {
             attachment.setMaster(storageDomain.isMaster());
         }
 
         return attachment;
     }
 
     @Override
     public Attachments list(UriInfo uriInfo) {
         Attachments ret = new Attachments();
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("get-datacenter -storagedomainid " + PowerShellUtils.escape(storageDomainId));
 
         ArrayList<DataCenter> dataCenters;
         try {
             dataCenters = PowerShellDataCenterResource.runAndParse(getShell(), buf.toString());
         } catch (PowerShellException e) {
             // Ignore 'Can not find any DataCenter by StorageDomainId' error
             // i.e. the storage domain isn't attached to any data center
             return ret;
         }
 
         for (DataCenter dataCenter : dataCenters) {
             buf = new StringBuilder();
 
             buf.append("get-storagedomain");
             buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenter.getId()));
             buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));
 
             StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(getShell(), buf.toString());
 
             Attachment attachment = buildAttachment(dataCenter, storageDomain);
 
             ret.getAttachments().add(PowerShellAttachmentResource.addLinks(attachment));
         }
 
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, Attachment attachment) {
         DataCenter dataCenter = attachment.getDataCenter();
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("attach-storagedomain");
         buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenter.getId()));
         buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));
 
         StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(getShell(), buf.toString());
 
         attachment = buildAttachment(dataCenter, storageDomain);
         attachment = PowerShellAttachmentResource.addLinks(attachment);
 
         UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(dataCenter.getId());
 
         return Response.created(uriBuilder.build()).entity(attachment).build();
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("detach-storagedomain");
         buf.append(" -datacenterid " + PowerShellUtils.escape(id));
         buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));
 
         PowerShellCmd.runCommand(getShell(), buf.toString());
     }
 
     @Override
     public AttachmentResource getAttachmentSubResource(UriInfo uriInfo, String id) {
        try {
            StringBuilder buf = new StringBuilder();

            buf.append("get-storagedomain");
            buf.append(" -datacenterid " + PowerShellUtils.escape(id));
            buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));
            PowerShellCmd.runCommand(getShell(), buf.toString());

            return new PowerShellAttachmentResource(id, storageDomainId, executor, shellPools);
        } catch (PowerShellException e) {
            return null;
        }
     }
 
     /**
      * Build a list of storage domains attached to a data center
      *
      * @param dataCenterId  the ID of the data center
      * @return  an encapsulation of the attachments
      */
     public static Attachments getAttachmentsForDataCenter(PowerShellCmd shell, String dataCenterId) {
         Attachments attachments = new Attachments();
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("get-storagedomain");
         buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenterId));
 
         ArrayList<StorageDomain> storageDomains;
         try {
             storageDomains = PowerShellStorageDomainResource.runAndParse(shell, buf.toString());
         } catch (PowerShellException e) {
             // Ignore 'There is no Storage Domains in DataCenter' error
             // i.e. no storage domains are attached to this data center
             return attachments;
         }
 
         for (StorageDomain storageDomain : storageDomains) {
             Attachment attachment = new Attachment();
             attachment.setId(dataCenterId);
             attachment.setStorageDomain(new StorageDomain());
             attachment.getStorageDomain().setId(storageDomain.getId());
             attachments.getAttachments().add(LinkHelper.addLinks(attachment));
 
         }
 
         return attachments;
     }
 
     public void setExecutor(Executor executor) {
         this.executor = executor;
     }
 
     protected PowerShellCmd getShell() {
         return shellPools.get().get();
     }
 }
