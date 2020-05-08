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
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Disk;
 import com.redhat.rhevm.api.model.Disks;
 import com.redhat.rhevm.api.common.util.ReflectionHelper;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 import com.redhat.rhevm.api.resource.DevicesResource;
 
 
 public class PowerShellDisksResource
     extends PowerShellReadOnlyDisksResource
     implements DevicesResource<Disk, Disks> {
 
     public PowerShellDisksResource(String parentId, PowerShellPoolMap shellPools, String getCommand) {
         super(parentId, shellPools, getCommand);
     }
 
     @Override
     public Response add(UriInfo uriInfo, Disk disk) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$d = new-disk");
         buf.append(" -disksize " + Math.round((double)disk.getSize()/(1024*1024*1024)));
         if (disk.getFormat() != null) {
             buf.append(" -volumeformat " + disk.getFormat().toString());
         }
         if (disk.getType() != null) {
            buf.append(" -disktype " + ReflectionHelper.capitalize(disk.getType().toString()));
         }
         if (disk.getInterface() != null) {
             buf.append(" -diskinterface ");
             switch (disk.getInterface()) {
             case IDE:
             case SCSI:
                 buf.append(disk.getInterface().toString());
                 break;
             case VIRTIO:
                 buf.append("VirtIO");
                 break;
             default:
                 assert false : disk.getInterface();
                 break;
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
         buf.append("\n");
 
         buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + "\n");
 
         buf.append("add-disk -diskobject $d -vmobject $v");
         if (disk.getStorageDomain() != null) {
             buf.append(" -storagedomainid " + disk.getStorageDomain().getId());
         }
 
         disk = addLinks(runAndParseSingle(buf.toString()));
 
         UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(disk.getId());
 
         return Response.created(uriBuilder.build()).entity(disk).build();
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("remove-disk");
         buf.append(" -vmid " + PowerShellUtils.escape(parentId));
         buf.append(" -diskids " + PowerShellUtils.escape(id));
 
         PowerShellCmd.runCommand(getShell(), buf.toString());
     }
 }
