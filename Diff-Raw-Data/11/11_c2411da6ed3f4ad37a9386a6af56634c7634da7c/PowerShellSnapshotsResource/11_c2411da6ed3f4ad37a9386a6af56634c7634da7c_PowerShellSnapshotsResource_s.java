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
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.resource.MediaType;
 
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.model.Snapshot;
 import com.redhat.rhevm.api.model.Snapshots;
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.powershell.model.PowerShellDisk;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 import com.redhat.rhevm.api.powershell.util.UUID;
 import com.redhat.rhevm.api.resource.SnapshotResource;
 import com.redhat.rhevm.api.resource.SnapshotsResource;
 
 @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
 public class PowerShellSnapshotsResource extends UriProviderWrapper implements SnapshotsResource {
 
     protected String vmId;
 
     static final String PROCESS_DISKS;
     static {
         StringBuilder buf = new StringBuilder();
         buf.append(" | ");
         buf.append("foreach { ");
         buf.append(PowerShellUtils.getDateHack("lastmodified"));
         buf.append("$_; ");
         buf.append("};");
         PROCESS_DISKS = buf.toString();
     }
 
     public PowerShellSnapshotsResource(String vmId,
                                        Executor executor,
                                        PowerShellPoolMap shellPools,
                                        PowerShellParser parser,
                                        UriInfoProvider uriProvider,
                                        HttpHeaders httpHeaders) {
         super(executor, shellPools, parser, uriProvider);
         setHttpHeaders(httpHeaders);
         this.vmId = vmId;
     }
 
     public String getVmId() {
         return vmId;
     }
 
     public List<PowerShellDisk> runAndParse(String command) {
         return PowerShellDisk.parse(getParser(), vmId, PowerShellCmd.runCommand(getPool(), command));
     }
 
     public PowerShellDisk runAndParseSingle(String command) {
         List<PowerShellDisk> disks = runAndParse(command);
         return !disks.isEmpty() ? disks.get(0) : null;
     }
 
     public Snapshot buildFromDisk(PowerShellDisk disk) {
         Snapshot snapshot = new Snapshot();
 
         snapshot.setId(disk.getVmSnapshotId());
         snapshot.setDescription(disk.getDescription());
         snapshot.setDate(disk.getLastModified());
 
         snapshot.setVm(new VM());
         snapshot.getVm().setId(vmId);
 
         if (!disk.getParentId().equals(UUID.EMPTY)) {
             UriBuilder uriBuilder = LinkHelper.getUriBuilder(getUriInfo(), snapshot.getVm()).path("snapshots");
 
             Link prev = new Link();
             prev.setRel("prev");
             prev.setHref(uriBuilder.clone().path(disk.getParentId()).build().toString());
             snapshot.getLinks().add(prev);
         }
 
         return LinkHelper.addLinks(getUriInfo(), snapshot);
     }
 
     public PowerShellDisk getDiskSnapshot(String vmSnapshotId) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$vm = get-vm " + PowerShellUtils.escape(vmId) + "; ");
         buf.append("foreach ($d in $vm.getdiskimages()) { ");
         buf.append("foreach ($s in get-snapshot -vmid $vm.vmid -drive $d.internaldrivemapping) { ");
         buf.append("if ($s.vmsnapshotid -eq " + PowerShellUtils.escape(vmSnapshotId) + ") { ");
         buf.append("$s; break");
        buf.append(" } } }");
 
         return runAndParseSingle(buf.toString() + PROCESS_DISKS);
     }
 
     private List<PowerShellDisk> getDiskSnapshots() {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$snaps = @(); ");
         buf.append("$vm = get-vm " + PowerShellUtils.escape(vmId) + "; ");
         buf.append("foreach ($d in $vm.getdiskimages()) { ");
         buf.append("try { ");
         buf.append("$snaps += get-snapshot -vmid $vm.vmid -drive $d.internaldrivemapping ");
         buf.append("} catch { [console]::error.writeline($_.exception) } ");
         buf.append("} ");
         buf.append("$snaps");
 
         return runAndParse(buf.toString() + PROCESS_DISKS);
     }
 
     @Override
     public Snapshots list() {
         Map<String, Snapshot> snapshots = new HashMap<String, Snapshot>();
 
         for (PowerShellDisk disk : getDiskSnapshots()) {
             if (snapshots.containsKey(disk.getVmSnapshotId())) {
                 continue;
             }
 
             snapshots.put(disk.getVmSnapshotId(), buildFromDisk(disk));
         }
 
         Snapshots ret = new Snapshots();
         for (String id : sortedKeys(snapshots)) {
             ret.getSnapshots().add(snapshots.get(id));
         }
         return ret;
     }
 
     @Override
     public Response add(Snapshot snapshot) {
         StringBuilder buf = new StringBuilder();
         Response response = null;
 
         buf.append("$vm = create-snapshot");
         buf.append(" -vmid " + PowerShellUtils.escape(vmId));
         if (snapshot.isSetDescription()) {
             buf.append(" -description " + PowerShellUtils.escape(snapshot.getDescription()));
         }
 
         boolean expectBlocking = expectBlocking();
         final String getDisks = ";$vm.getdiskimages()";
         if (expectBlocking) {
             buf.append(getDisks).append(PROCESS_DISKS);
         } else {
             buf.append(ASYNC_OPTION).append(getDisks).append(PROCESS_DISKS).append(ASYNC_TASKS); 
         }
 
         PowerShellDisk disk = runAndParseSingle(buf.toString());
         snapshot = buildFromDisk(disk);
 
         if (expectBlocking || disk.getTaskIds() == null) {
             UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(snapshot.getId());
             response = Response.created(uriBuilder.build()).entity(snapshot).build();
         } else {
             snapshot = addStatus(getUriInfo(), snapshot, disk);
             response = Response.status(202).entity(snapshot).build();
         }
         return response;
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("remove-snapshot");
         buf.append(" -vmid " + PowerShellUtils.escape(vmId));
         buf.append(" -vmsnapshotid " + PowerShellUtils.escape(id));
         buf.append(" -async");
 
         PowerShellCmd.runCommand(getPool(), buf.toString());
     }
 
     @Override
     public SnapshotResource getSnapshotSubResource(String id) {
         return new PowerShellSnapshotResource(id, getExecutor(), shellPools, getParser(), this, getUriProvider());
     }
 
     private List<String> sortedKeys(Map<String, Snapshot> map) {
         List<String> keys = new ArrayList<String>(map.keySet());
         Collections.sort(keys);
         return keys;
     }
 
     private Snapshot addStatus(UriInfo uriInfo, Snapshot snapshot, PowerShellDisk disk) {
         if (disk.getTaskIds() != null) {
             snapshot.setCreationStatus(disk.getCreationStatus());
             Link link = new Link();
             link.setRel(CREATION_STATUS);
             link.setHref(LinkHelper.getUriBuilder(uriInfo, snapshot).path(CREATION_STATUS).path(disk.getTaskIds()).build().toString());
             snapshot.getLinks().add(link);
         }
         return snapshot;
     }
 }
