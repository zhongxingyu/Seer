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
 
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Storage;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.model.StorageDomainType;
 import com.redhat.rhevm.api.model.StorageDomains;
 import com.redhat.rhevm.api.resource.StorageDomainResource;
 import com.redhat.rhevm.api.resource.StorageDomainsResource;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 public class PowerShellStorageDomainsResource implements StorageDomainsResource {
 
     @Override
     public StorageDomains list(UriInfo uriInfo) {
         StorageDomains ret = new StorageDomains();
         for (StorageDomain storageDomain : PowerShellStorageDomainResource.runAndParse("select-storagedomain")) {
             PowerShellStorageDomainResource resource = new PowerShellStorageDomainResource(storageDomain);
             UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(storageDomain.getId());
             ret.getStorageDomains().add(resource.addLinks(uriBuilder));
         }
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("add-storagedomain");
 
         if (storageDomain.getName() != null) {
             buf.append(" -name " + storageDomain.getName());
         }
 
         // FIXME: we don't know this until initialize
         buf.append(" -hostid XXXX ");
 
        buf.append(" -domaintype ");
        switch (storageDomain.getType()) {
        case DATA:
            buf.append("Data");
            break;
        case ISO:
            buf.append("ISO");
            break;
        case EXPORT:
            buf.append("Export");
            break;
        default:
            assert false : storageDomain.getType();
            break;
        }
 
         Storage storage = storageDomain.getStorage();
 
         buf.append(" -storagetype " + storage.getType().toString());
         buf.append(" -storage ");
 
         switch (storage.getType()) {
         case NFS:
             buf.append(storage.getHost() + ":" + storage.getPath());
             break;
         case ISCSI:
         case FCP:
         default:
             assert false : storage.getType();
             break;
         }
 
         storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());
 
         PowerShellStorageDomainResource resource = new PowerShellStorageDomainResource(storageDomain);
 
         UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(storageDomain.getId());
 
         return Response.created(uriBuilder.build()).entity(resource.addLinks(uriBuilder)).build();
     }
 
     @Override
     public void remove(String id) {
         PowerShellUtils.runCommand("remove-storagedomain -storagedomainid " + id);
     }
 
     @Override
     public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
         return new PowerShellStorageDomainResource(id);
     }
 }
