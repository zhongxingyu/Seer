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
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
 import com.redhat.rhevm.api.common.resource.StorageDomainActionValidator;
 import com.redhat.rhevm.api.common.util.JAXBHelper;
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.ActionsBuilder;
 import com.redhat.rhevm.api.model.ActionValidator;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.Storage;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.resource.AttachmentsResource;
 import com.redhat.rhevm.api.resource.StorageDomainResource;
 import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 public class PowerShellStorageDomainResource extends AbstractActionableResource<StorageDomain> implements StorageDomainResource {
 
     private PowerShellStorageDomainsResource parent;
     private StorageDomain staged;
 
     public PowerShellStorageDomainResource(String id,
                                            PowerShellStorageDomainsResource parent,
                                            StorageDomain staged) {
         super(id, parent.getExecutor());
         this.parent = parent;
         this.staged = staged;
     }
 
     public PowerShellStorageDomainResource(String id, PowerShellStorageDomainsResource parent) {
         this(id, parent, null);
     }
 
     public StorageDomain getStaged() {
         return staged;
     }
 
     /**
      * Run a powershell command and parse the output as a list of storage
      * domains.
      * <p>
      * If the resulting storage domains are being viewed in the context
      * of a specific data center, then the caller wants the value of the
      * 'status' property. In this case, @sharedStatus should be #false.
      * <p>
      * If the storage domain is being viewed outside of the context of any
      * data center, then the 'sharedStatus' property contains the required
      * status and the caller should supply #true for @sharedStatus.
      *
      * @param command the powershell command to execute
      * @param sharedStatus whether the 'sharedStatus' property is needed
      * @return a list of storage domains
      */
     public static ArrayList<StorageDomain> runAndParse(String command, boolean sharedStatus) {
         ArrayList<PowerShellStorageDomain> storageDomains =
             PowerShellStorageDomain.parse(PowerShellCmd.runCommand(command));
         ArrayList<StorageDomain> ret = new ArrayList<StorageDomain>();
 
         for (PowerShellStorageDomain storageDomain : storageDomains) {
            if (sharedStatus) {
                 storageDomain.setStatus(storageDomain.getSharedStatus());
             }
             ret.add(storageDomain);
         }
 
         return ret;
     }
 
     /**
      * Run a powershell command and parse the output as a list of storage
      * domains. The 'sharedStatus' property in the output from the command
      * is ignored.
      *
      * @param command the powershell command to execute
      * @return a list of storage domains
      */
     public static ArrayList<StorageDomain> runAndParse(String command) {
         return runAndParse(command, false);
     }
 
     /**
      * Run a powershell command and parse the output as a single storage
      * domain.
      *
      * @param command the powershell command to execute
      * @param whether the 'sharedStatus' property is needed
      * @return a single storage domain, or null
      */
     public static StorageDomain runAndParseSingle(String command, boolean sharedStatus) {
         ArrayList<StorageDomain> storageDomains = runAndParse(command, sharedStatus);
 
         return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
     }
 
     /**
      * Run a powershell command and parse the output as a single storage
      * domain. The 'sharedStatus' property in the output from the command
      * is ignored.
      *
      * @param command the powershell command to execute
      * @return a single storage domain, or null
      */
     public static StorageDomain runAndParseSingle(String command) {
         return runAndParseSingle(command, false);
     }
 
     public StorageDomain addLinks(StorageDomain storageDomain, UriBuilder uriBuilder) {
         storageDomain.setHref(uriBuilder.build().toString());
 
         ActionValidator actionValidator = new StorageDomainActionValidator(storageDomain);
         ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, StorageDomainResource.class, actionValidator);
         storageDomain.setActions(actionsBuilder.build());
 
         Link link = new Link();
         link.setRel("attachments");
         link.setHref(uriBuilder.clone().path("attachments").build().toString());
         storageDomain.getLinks().clear();
         storageDomain.getLinks().add(link);
 
         return storageDomain;
     }
 
     @Override
     public StorageDomain get(UriInfo uriInfo) {
         StorageDomain storageDomain;
         if (staged != null) {
             storageDomain = staged;
         } else {
             storageDomain = parent.mapFromRhevmId(runAndParseSingle("get-storagedomain " + getId(), true));
         }
         storageDomain = JAXBHelper.clone(OBJECT_FACTORY.createStorageDomain(storageDomain));
         return addLinks(storageDomain, uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public StorageDomain update(HttpHeaders headers, final UriInfo uriInfo, StorageDomain storageDomain) {
         validateUpdate(storageDomain, headers);
 
         StringBuilder buf = new StringBuilder();
         if (staged != null) {
             // update writable fields only
             staged.setName(storageDomain.getName());
 
             storageDomain = staged;
         } else {
             buf.append("$h = get-storagedomain " + getId() + "\n");
 
             if (storageDomain.getName() != null) {
                 buf.append("$h.name = '" + storageDomain.getName() + "'");
             }
 
             buf.append("\n");
             buf.append("update-storagedomain -storagedomainobject $v");
 
             storageDomain = parent.mapFromRhevmId(runAndParseSingle(buf.toString(), true));
         }
         storageDomain = JAXBHelper.clone(OBJECT_FACTORY.createStorageDomain(storageDomain));
         return addLinks(storageDomain, uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public Response initialize(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new StorageDomainInitializer(action));
     }
 
 
     @Override
     public Response teardown(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new StorageDomainTeardowner(action));
     }
 
     @Override
     public AttachmentsResource getAttachmentsResource() {
         return new PowerShellAttachmentsResource(getId());
     }
 
     private abstract class StorageDomainActionTask extends AbstractActionTask {
         protected String id;
         protected StorageDomain staged;
         protected PowerShellStorageDomainsResource parent;
         public StorageDomainActionTask(Action action) {
             super(action);
             this.id = PowerShellStorageDomainResource.this.getId();
             this.staged = PowerShellStorageDomainResource.this.staged;
             this.parent = PowerShellStorageDomainResource.this.parent;
         }
     }
 
     private class StorageDomainInitializer extends StorageDomainActionTask {
         public StorageDomainInitializer(Action action) {
             super(action);
         }
         public void run() {
             StringBuilder buf = new StringBuilder();
 
             buf.append("add-storagedomain");
 
             if (staged.getName() != null) {
                 buf.append(" -name '" + staged.getName() + "'");
             }
 
             buf.append(" -hostid " + action.getHost().getId());
 
             buf.append(" -domaintype ");
             switch (staged.getType()) {
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
                 assert false : staged.getType();
                 break;
             }
 
             Storage storage = staged.getStorage();
 
             buf.append(" -storagetype " + storage.getType().toString());
             buf.append(" -storage ");
 
             switch (storage.getType()) {
             case NFS:
                 buf.append("'" + storage.getAddress() + ":" + storage.getPath() + "'");
                 break;
             case ISCSI:
             case FCP:
             default:
                 assert false : storage.getType();
                 break;
             }
 
             StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString(), true);
 
             parent.unstageDomain(id, storageDomain.getId());
         }
     }
 
     private class StorageDomainTeardowner extends StorageDomainActionTask {
         public StorageDomainTeardowner(Action action) {
             super(action);
         }
         public void run() {
             StorageDomain storageDomain = new StorageDomain();
             storageDomain.setId(id);
             parent.mapToRhevmId(storageDomain);
 
             storageDomain = runAndParseSingle("get-storagedomain " + storageDomain.getId(), true);
 
             StringBuilder buf = new StringBuilder();
 
             buf.append("remove-storagedomain -force");
 
             buf.append(" -storagedomainid " + storageDomain.getId());
 
             buf.append(" -hostid " + action.getHost().getId());
 
             PowerShellCmd.runCommand(buf.toString());
 
             staged = parent.mapFromRhevmId(storageDomain);
             parent.stageDomain(id, PowerShellStorageDomainResource.this);
         }
     }
 }
