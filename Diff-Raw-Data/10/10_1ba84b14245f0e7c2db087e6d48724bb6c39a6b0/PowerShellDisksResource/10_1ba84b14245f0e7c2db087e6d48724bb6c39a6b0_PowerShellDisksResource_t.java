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
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Disk;
 import com.redhat.rhevm.api.model.Disks;
 import com.redhat.rhevm.api.model.DiskFormat;
 import com.redhat.rhevm.api.model.DiskInterface;
 import com.redhat.rhevm.api.model.DiskType;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.common.util.ReflectionHelper;
 import com.redhat.rhevm.api.powershell.enums.PowerShellDiskInterface;
 import com.redhat.rhevm.api.powershell.model.PowerShellDisk;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 import com.redhat.rhevm.api.resource.DevicesResource;
 
 import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
 
 public class PowerShellDisksResource
     extends PowerShellReadOnlyDisksResource
     implements DevicesResource<Disk, Disks> {
 
     public PowerShellDisksResource(String parentId,
                                    PowerShellPoolMap shellPools,
                                    PowerShellParser parser,
                                    String getCommand,
                                    UriInfoProvider uriProvider,
                                    HttpHeaders httpHeaders) {
         super(parentId, shellPools, parser, getCommand, uriProvider);
         setHttpHeaders(httpHeaders);
     }
 
     @Override
     public Response add(Disk disk) {
         Response response = null;
 
         validateParameters(disk, "size");
         StringBuilder buf = new StringBuilder();
 
         buf.append("$d = new-disk");
         buf.append(" -disksize " + Math.round((double)disk.getSize()/(1024*1024*1024)));
         if (disk.getFormat() != null) {
            DiskFormat format = DiskFormat.fromValue(disk.getFormat());
             if (format != null) {
                 buf.append(" -volumeformat " + format.name());
             }
         }
         if (disk.getType() != null) {
            DiskType type = DiskType.fromValue(disk.getType());
             if (type != null) {
                 buf.append(" -disktype " + ReflectionHelper.capitalize(type.name().toLowerCase()));
             }
         }
         if (disk.getInterface() != null) {
            DiskInterface diskInterface = DiskInterface.fromValue(disk.getInterface());
             if (diskInterface != null) {
                 buf.append(" -diskinterface " + PowerShellDiskInterface.forModel(diskInterface).name());
             }
         }
         if (disk.isSparse() != null) {
             buf.append(" -volumetype " + (disk.isSparse() ? "Sparse" : "Preallocated"));
         }
         if (disk.isWipeAfterDelete() != null && disk.isWipeAfterDelete()) {
             buf.append(" -wipeafterdelete");
         }
         if (disk.isPropagateErrors() != null) {
             buf.append(" -propagateerrors ");
             if (disk.isPropagateErrors()) {
                 buf.append("on");
             } else {
                 buf.append("off");
             }
         }
         buf.append(";");
 
         buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + ";");
 
         buf.append("add-disk -diskobject $d -vmobject $v");
         if (disk.getStorageDomain() != null) {
             buf.append(" -storagedomainid " + disk.getStorageDomain().getId());
         }
 
         boolean expectBlocking = expectBlocking();
         if (!expectBlocking) {
             buf.append(ASYNC_ENDING + ASYNC_TASKS);
         }
 
         PowerShellDisk created = (PowerShellDisk)runAndParseSingle(buf.toString());
 
         if (expectBlocking || created.getTaskIds() == null) {
             disk = addLinks(created);
             UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(disk.getId());
             response = Response.created(uriBuilder.build()).entity(disk).build();
         } else {
             disk = addStatus(getUriInfo(), addLinks(created), created.getTaskIds());
             response = Response.status(202).entity(disk).build();
         }
 
         return response;
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("remove-disk");
         buf.append(" -vmid " + PowerShellUtils.escape(parentId));
         buf.append(" -diskids " + PowerShellUtils.escape(id));
 
         PowerShellCmd.runCommand(getPool(), buf.toString());
     }
 
     @Override
     public PowerShellDeviceResource<Disk, Disks> getDeviceSubResource(String id) {
         return new PowerShellDeviceResource<Disk, Disks>(this, id);
     }
 
     private Disk addStatus(UriInfo uriInfo, Disk disk, String taskIds) {
         if (taskIds != null) {
             Link link = new Link();
             link.setRel(CREATION_STATUS);
             link.setHref(LinkHelper.getUriBuilder(uriInfo, disk).path(CREATION_STATUS).path(taskIds).build().toString());
             disk.getLinks().add(link);
         }
         return disk;
     }
 }
