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
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.CpuTopology;
 import com.redhat.rhevm.api.model.Display;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.Ticket;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.model.VmType;
 import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
 import com.redhat.rhevm.api.resource.AssignedTagsResource;
 import com.redhat.rhevm.api.resource.VmResource;
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.common.util.JAXBHelper;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.common.util.ReflectionHelper;
 import com.redhat.rhevm.api.powershell.model.PowerShellTicket;
 import com.redhat.rhevm.api.powershell.model.PowerShellVM;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
 import static com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource.PROCESS_VMS;
 
 public class PowerShellVmResource extends AbstractPowerShellActionableResource<VM> implements VmResource {
 
     public PowerShellVmResource(String id,
                                 Executor executor,
                                 UriInfoProvider uriProvider,
                                 PowerShellPoolMap shellPools,
                                 PowerShellParser parser) {
         super(id, executor, uriProvider, shellPools, parser);
     }
 
     public static List<PowerShellVM> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
         return PowerShellVM.parse(parser, PowerShellCmd.runCommand(pool, command));
     }
 
     public static PowerShellVM runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
         List<PowerShellVM> vms = runAndParse(pool, parser, command);
 
         return !vms.isEmpty() ? vms.get(0) : null;
     }
 
     public PowerShellVM runAndParseSingle(String command) {
         return runAndParseSingle(getPool(), getParser(), command);
     }
 
     public static VM addLinks(UriInfo uriInfo, PowerShellVM vm) {
         VM ret = JAXBHelper.clone("vm", VM.class, vm);
 
         String [] deviceCollections = { "cdroms", "disks", "floppies", "nics", "snapshots", "tags" };
 
         ret.getLinks().clear();
 
         for (String collection : deviceCollections) {
             addSubCollection(uriInfo, ret, collection);
         }
 
         return LinkHelper.addLinks(uriInfo, ret);
     }
 
     @Override
     public VM get() {
         return addLinks(getUriInfo(), runAndParseSingle("get-vm " + PowerShellUtils.escape(getId()) + PROCESS_VMS));
     }
 
     @Override
     public VM update(VM vm) {
         validateUpdate(vm);
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("$v = get-vm " + PowerShellUtils.escape(getId()) + ";");
 
         if (vm.getName() != null) {
             buf.append("$v.name = " + PowerShellUtils.escape(vm.getName()) + ";");
         }
         if (vm.getDescription() != null) {
             buf.append("$v.description = " + PowerShellUtils.escape(vm.getDescription()) + ";");
         }
         if (vm.getType() != null) {
             buf.append("$v.vmtype = " + ReflectionHelper.capitalize(vm.getType().toString().toLowerCase()) + ";");
         }
         if (vm.isSetMemory()) {
             buf.append(" $v.memorysize = " + Math.round((double)vm.getMemory()/(1024*1024)) + ";");
         }
         if (vm.getCpu() != null && vm.getCpu().getTopology() != null) {
             CpuTopology topology = vm.getCpu().getTopology();
             if (topology.isSetSockets()) {
                 buf.append(" $v.numofsockets = " + topology.getSockets() + ";");
             }
             if (topology.isSetCores()) {
                 buf.append(" $v.numofcpuspersocket = " + topology.getCores() + ";");
             }
         }
         String bootSequence = PowerShellVM.buildBootSequence(vm.getOs());
         if (bootSequence != null) {
             buf.append(" $v.defaultbootsequence = '" + bootSequence + "';");
         }
         if (vm.isSetOs() && vm.getOs().isSetType()) {
             buf.append(" $v.operatingsystem = " + PowerShellUtils.escape(vm.getOs().getType()) + ";");
         }
         if (vm.isSetStateless()) {
             buf.append(" $v.stateless = " + PowerShellUtils.encode(vm.isStateless()) + ";");
         }
         if (vm.isSetHighlyAvailable()) {
             buf.append(" $v.highlyavailable = " + PowerShellUtils.encode(vm.getHighlyAvailable().isValue()) + ";");
             if (vm.getHighlyAvailable().isSetPriority()) {
                 buf.append(" $v.priority = " + Integer.toString(vm.getHighlyAvailable().getPriority()) + ";");
             }
         }
         if (vm.isSetDisplay()) {
             Display display = vm.getDisplay();
             if (display.isSetMonitors()) {
                 buf.append(" $v.numofmonitors = " + display.getMonitors() + ";");
             }
             if (display.isSetType()) {
                 buf.append(" $v.displaytype = '" + PowerShellVM.asString(display.getType()) + "';");
             }
             // REVISIT display port a read-only property => extend immutability
             // assertion to support "display.port" style syntax
         }
 
         buf.append("update-vm -vmobject $v");
 
         return addLinks(getUriInfo(), runAndParseSingle(buf.toString() + PROCESS_VMS));
     }
 
     protected String[] getStrictlyImmutable() {
         return addStrictlyImmutable("type");
     }
 
     @Override
     public Response start(Action action) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("start-vm");
         buf.append(" -vmid " + PowerShellUtils.escape(getId()));
 
         if (action.isSetPause() && action.isPause()) {
             buf.append(" -runandpause");
         }
 
         if (action.isSetVm()) {
             VM vm = action.getVm();
 
            if (vm.isSetDisplay() && vm.getDisplay().isSetType()) {
                 buf.append(" -displaytype '" + PowerShellVM.asString(vm.getDisplay().getType()) + "'");
             }
 
             String bootSequence = PowerShellVM.buildBootSequence(vm.getOs());
             if (bootSequence != null) {
                 buf.append(" -bootdevice '" + bootSequence + "'");
             }
 
             if (vm.isSetCdroms() && vm.getCdroms().isSetCdRoms()) {
                 String file = vm.getCdroms().getCdRoms().get(0).getFile().getId();
                 if (file != null) {
                     buf.append(" -isofilename " + PowerShellUtils.escape(file));
                 }
             }
 
             if (vm.isSetFloppies() && vm.getFloppies().isSetFloppies()) {
                 String file = vm.getFloppies().getFloppies().get(0).getFile().getId();
                 if (file != null) {
                     buf.append(" -floppypath " + PowerShellUtils.escape(file));
                 }
             }
         }
 
         return doAction(getUriInfo(), new CommandRunner(action, buf.toString(), getPool()));
     }
 
     @Override
     public Response stop(Action action) {
         return doAction(getUriInfo(), new CommandRunner(action, "stop-vm", "vm", getId(), getPool()));
     }
 
     @Override
     public Response shutdown(Action action) {
         return doAction(getUriInfo(), new CommandRunner(action, "shutdown-vm", "vm", getId(), getPool()));
     }
 
     @Override
     public Response suspend(Action action) {
         return doAction(getUriInfo(), new CommandRunner(action, "suspend-vm", "vm", getId(), getPool()));
     }
 
     @Override
     public Response detach(Action action) {
         return doAction(getUriInfo(), new CommandRunner(action, "detach-vm", "vm", getId(), getPool()));
     }
 
     @Override
     public Response migrate(Action action) {
         validateParameters(action, "host.id|name");
         StringBuilder buf = new StringBuilder();
 
         String hostArg;
         if (action.getHost().isSetId()) {
             hostArg = PowerShellUtils.escape(action.getHost().getId());
         } else {
             buf.append("$h = select-host -searchtext ");
             buf.append(PowerShellUtils.escape("name=" + action.getHost().getName()));
             buf.append(";");
             hostArg = "$h.hostid";
         }
 
         buf.append("migrate-vm");
         buf.append(" -vmid " + PowerShellUtils.escape(getId()));
         buf.append(" -desthostid " + hostArg);
 
         return doAction(getUriInfo(), new CommandRunner(action, buf.toString(), getPool()));
     }
 
     @Override
     public Response export(Action action) {
         validateParameters(action, "storageDomain.id|name");
 
         StringBuilder buf = new StringBuilder();
 
         String storageDomainArg;
         if (action.getStorageDomain().isSetId()) {
             storageDomainArg = PowerShellUtils.escape(action.getStorageDomain().getId());
         } else {
             buf.append("$dest = select-storagedomain ");
             buf.append("| ? { $_.name -eq ");
             buf.append(PowerShellUtils.escape(action.getStorageDomain().getName()));
             buf.append(" }; ");
             storageDomainArg = "$dest.storagedomainid";
         }
 
         buf.append("export-vm");
         buf.append(" -vmid " + PowerShellUtils.escape(getId()));
         buf.append(" -storagedomainid " + storageDomainArg);
 
         if (!action.isSetExclusive() || !action.isExclusive()) {
             buf.append(" -forceoverride");
         }
 
         if (!action.isSetDiscardSnapshots() || !action.isDiscardSnapshots()) {
             buf.append(" -copycollapse");
         }
 
         return doAction(getUriInfo(), new CommandRunner(action, buf.toString(), getPool()));
     }
 
     @Override
     public Response ticket(Action action) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("set-vmticket");
         buf.append(" -vmid " + PowerShellUtils.escape(getId()));
         if (action.isSetTicket()) {
             Ticket ticket = action.getTicket();
             if (ticket.isSetValue()) {
                 buf.append(" -ticket " + PowerShellUtils.escape(ticket.getValue()));
             }
             if (ticket.isSetExpiry()) {
                 buf.append(" -validtime " + PowerShellUtils.escape(Long.toString(ticket.getExpiry())));
             }
         }
 
         return doAction(getUriInfo(),
                         new CommandRunner(action, buf.toString(), getPool()) {
                             protected void handleOutput(String output) {
                                 action.setTicket(PowerShellTicket.parse(getParser(), output));
                             }
                         });
     }
 
     public class CdRomQuery extends PowerShellCdRomsResource.CdRomQuery {
         public CdRomQuery(String id) {
             super(id);
         }
         @Override protected String getCdIsoPath() {
             return runAndParseSingle("get-vm " + PowerShellUtils.escape(id)).getCdIsoPath();
         }
     }
 
     public class FloppyQuery extends PowerShellFloppiesResource.FloppyQuery {
         public FloppyQuery(String id) {
             super(id);
         }
         @Override protected String getFloppyPath() {
             return runAndParseSingle("get-vm " + PowerShellUtils.escape(id)).getFloppyPath();
         }
     }
 
     @Override
     public PowerShellCdRomsResource getCdRomsResource() {
         return new PowerShellCdRomsResource(getId(), shellPools, new CdRomQuery(getId()), getUriProvider());
     }
 
     @Override
     public PowerShellDisksResource getDisksResource() {
         return new PowerShellDisksResource(getId(), shellPools, getParser(), "get-vm", getUriProvider());
     }
 
     @Override
     public PowerShellFloppiesResource getFloppiesResource() {
         return new PowerShellFloppiesResource(getId(), shellPools, new FloppyQuery(getId()), getUriProvider());
     }
 
     @Override
     public PowerShellNicsResource getNicsResource() {
         return new PowerShellNicsResource(getId(), shellPools, getParser(), "get-vm", getUriProvider());
     }
 
     @Override
     public PowerShellSnapshotsResource getSnapshotsResource() {
         return new PowerShellSnapshotsResource(getId(), getExecutor(), shellPools, getParser(), getUriProvider());
     }
 
     @Override
     public AssignedTagsResource getTagsResource() {
         return new PowerShellAssignedTagsResource(VM.class, getId(), shellPools, getParser(), getUriProvider());
     }
 
     @Override
     public AssignedPermissionsResource getPermissionsResource() {
         return null;
     }
 
     private static void addSubCollection(UriInfo uriInfo, VM vm, String collection) {
         Link link = new Link();
         link.setRel(collection);
         link.setHref(LinkHelper.getUriBuilder(uriInfo, vm).path(collection).build().toString());
         vm.getLinks().add(link);
     }
 }
