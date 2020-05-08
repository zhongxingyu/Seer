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
 
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
 import com.redhat.rhevm.api.common.resource.AttachmentActionValidator;
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.ActionsBuilder;
 import com.redhat.rhevm.api.model.ActionValidator;
 import com.redhat.rhevm.api.model.Attachment;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.resource.AttachmentResource;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 
public class PowerShellAttachmentResource extends AbstractActionableResource<StorageDomain> implements AttachmentResource {
 
     private String storageDomainId;
 
     /**
      * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
      *
      * @param attachment  encapsulated Attachment
      */
     PowerShellAttachmentResource(String dataCenterId, String storageDomainId, Executor executor) {
         super(dataCenterId, executor);
         this.storageDomainId = storageDomainId;
     }
 
     private static void setStorageDomainHref(Attachment attachment, UriBuilder baseUriBuilder) {
         StorageDomain storageDomain = attachment.getStorageDomain();
 
         String href = PowerShellStorageDomainsResource.getHref(baseUriBuilder, storageDomain.getId());
 
         storageDomain.setHref(href);
     }
 
     private static void setDataCenterHref(Attachment attachment, UriBuilder baseUriBuilder) {
         DataCenter dataCenter = attachment.getDataCenter();
 
         String href = PowerShellDataCentersResource.getHref(baseUriBuilder, dataCenter.getId());
 
         dataCenter.setHref(href);
     }
 
     public static Attachment addLinks(Attachment attachment, UriInfo uriInfo, UriBuilder uriBuilder) {
         UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
 
         attachment.setHref(uriBuilder.build().toString());
 
         setStorageDomainHref(attachment, baseUriBuilder);
         setDataCenterHref(attachment, baseUriBuilder);
 
         ActionValidator actionValidator = new AttachmentActionValidator(attachment);
         ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, AttachmentResource.class, actionValidator);
         attachment.setActions(actionsBuilder.build());
 
         return attachment;
     }
 
     @Override
     public Attachment get(UriInfo uriInfo) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("get-storagedomain");
         buf.append(" -datacenterid " + getId());
         buf.append(" -storagedomainid " + storageDomainId);
 
         StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());
 
         DataCenter dataCenter = new DataCenter();
         dataCenter.setId(getId());
 
         Attachment attachment = PowerShellAttachmentsResource.buildAttachment(dataCenter, storageDomain);
 
         return addLinks(attachment, uriInfo, uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public Response activate(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new AttachmentActionTask(action, "activate-storagedomain"));
     }
 
     @Override
     public Response deactivate(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new AttachmentActionTask(action, "deactivate-storagedomain"));
     }
 
     private class AttachmentActionTask extends AbstractActionTask {
         private String command;
         public AttachmentActionTask(Action action, String command) {
             super(action);
             this.command = command;
         }
         public void run() {
             StringBuilder buf = new StringBuilder();
 
             buf.append(command);
             buf.append(" -datacenterid " + getId());
             buf.append(" -storagedomainid " + storageDomainId);
 
             PowerShellCmd.runCommand(buf.toString());
         }
     }
 }
