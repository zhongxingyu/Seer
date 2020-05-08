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
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.ActionsBuilder;
 import com.redhat.rhevm.api.model.ActionValidator;
 import com.redhat.rhevm.api.model.Storage;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.resource.AttachmentsResource;
 import com.redhat.rhevm.api.resource.StorageDomainResource;
 import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 public class PowerShellStorageDomainResource extends AbstractPowerShellResource<StorageDomain> implements StorageDomainResource, ActionValidator {
 
     private PowerShellStorageDomainsResource parent;
 
     /* Whether this storage domain exists yet in RHEV-M */
     private boolean staged;
 
     public PowerShellStorageDomainResource(StorageDomain storageDomain,
                                            PowerShellStorageDomainsResource parent,
                                            boolean staged) {
         super(storageDomain, storageDomain.getId());
         this.parent = parent;
         this.staged = staged;
     }
 
     public PowerShellStorageDomainResource(StorageDomain storageDomain, PowerShellStorageDomainsResource parent) {
         this(storageDomain, parent, false);
     }
 
     public void setId(String id) {
         getModel().setId(id);
     }
 
     public static ArrayList<StorageDomain> runAndParse(String command) {
         return PowerShellStorageDomain.parse(PowerShellUtils.runCommand(command));
     }
 
     public static StorageDomain runAndParseSingle(String command) {
         ArrayList<StorageDomain> storageDomains = runAndParse(command);
 
         return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
     }
 
     public StorageDomain addLinks(StorageDomain storageDomain, UriBuilder uriBuilder) {
         ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, StorageDomainResource.class, this);
         storageDomain = parent.mapId(storageDomain);
         storageDomain.setActions(actionsBuilder.build());
         storageDomain.setHref(uriBuilder.build().toString());
         return storageDomain;
     }
 
     @Override
     public StorageDomain get(UriInfo uriInfo) {
        return setModel(addLinks(refreshRepresentation(), uriInfo.getRequestUriBuilder()));
     }
 
     @Override
     public StorageDomain update(HttpHeaders headers, UriInfo uriInfo, StorageDomain storageDomain) {
         validateUpdate(storageDomain, getModel(), headers);
 
         StringBuilder buf = new StringBuilder();
         if (staged) {
             // update writable fields only
             getModel().setName(storageDomain.getName());
         } else {
             buf.append("$h = get-storagedomain " + id + "\n");
 
             if (storageDomain.getName() != null) {
                 buf.append("$h.name = \"" + storageDomain.getName() + "\"");
             }
 
             buf.append("\n");
             buf.append("update-storagedomain -storagedomainobject $v");
         }
 
        return setModel(addLinks(staged
                                 ? getModel()
                                 : runAndParseSingle(buf.toString()),
                                 uriInfo.getRequestUriBuilder()));
     }
 
     @Override
     public boolean validateAction(String action) {
         switch (getModel().getStatus()) {
         case UNINITIALIZED:
             return action.equals("initialize");
         case UNATTACHED:
             return action.equals("teardown");
         case ACTIVE:
         case INACTIVE:
             return false;
         case LOCKED:
         case MIXED:
         default:
             assert false : getModel().getStatus();
             return false;
         }
     }
 
     @Override
     public void initialize(UriInfo uriInfo, Action action) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("add-storagedomain");
 
         if (getModel().getName() != null) {
             buf.append(" -name " + getModel().getName());
         }
 
         buf.append(" -hostid " + action.getHost().getId());
 
         buf.append(" -domaintype ");
         switch (getModel().getType()) {
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
             assert false : getModel().getType();
             break;
         }
 
         Storage storage = getModel().getStorage();
 
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
 
         setModel(PowerShellStorageDomainResource.runAndParseSingle(buf.toString()));
         // unstage with fabricated ID, real RHEVM ID
         String rhevmId = getModel().getId();
         parent.unstageDomain(id, rhevmId);
         // set super-class id to real RHEVM ID for later unstaging
         id = rhevmId;
     }
 
     @Override
     public void teardown(UriInfo uriInfo, Action action) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("remove-storagedomain --force");
 
         buf.append(" --storagedomainid " + id);
 
         buf.append(" -hostid " + action.getHost().getId());
 
         PowerShellUtils.runCommand(buf.toString());
 
         // unstage with real RHEVM ID
         parent.stageDomain(id, this);
     }
 
     @Override
     public AttachmentsResource getAttachmentsResource() {
         return null;
     }
 
     protected StorageDomain refreshRepresentation() {
         return staged
                ? getModel()
                : runAndParseSingle("get-storagedomain " + id);
     }
 }
