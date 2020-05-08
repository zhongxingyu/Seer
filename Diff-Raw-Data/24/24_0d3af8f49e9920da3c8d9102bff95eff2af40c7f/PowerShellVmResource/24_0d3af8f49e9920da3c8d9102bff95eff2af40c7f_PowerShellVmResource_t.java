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
 
 import java.net.URI;
 import java.util.ArrayList;
 
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.resource.VmResource;
 import com.redhat.rhevm.api.powershell.model.PowerShellVM;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 import static com.redhat.rhevm.api.powershell.util.PowerShellUtils.runCommand;
 
 public class PowerShellVmResource implements VmResource {
     /*
      * FIXME: would like to do: private @Context UriInfo uriInfo;
      */
 
     private String id;
 
    public PowerShellVmResource(String id) {
         this.id = id;
     }
 
    public static ArrayList<VM> runAndParse(String command) {
         return PowerShellVM.parse(PowerShellUtils.runCommand(command));
     }
 
    public static VM runAndParseSingle(String command) {
         ArrayList<VM> vms = runAndParse(command);
 
         return !vms.isEmpty() ? vms.get(0) : null;
     }
 
    public static VM addLink(VM vm, URI uri) {
         vm.setLink(new Link("self", uri));
         return new VM(vm);
     }
 
     @Override
     public VM get(UriInfo uriInfo) {
         return addLink(runAndParseSingle("get-vm " + id), uriInfo.getRequestUriBuilder().build());
     }
 
     @Override
     public VM update(UriInfo uriInfo, VM vm) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$v = get-vm " + vm.getId() + "\n");
 
         if (vm.getName() != null) {
             buf.append("$v.name = \"" + vm.getName() + "\"");
         }
 
         buf.append("\n");
         buf.append("update-vm -vmobject $v");
 
         return addLink(runAndParseSingle(buf.toString()), uriInfo.getRequestUriBuilder().build());
     }
 
     @Override
     public void start() {
         runCommand("start-vm -vmid " + id);
     }
 
     @Override
     public void stop() {
         runCommand("stop-vm -vmid " + id);
     }
 
     @Override
     public void shutdown() {
         runCommand("shutdown-vm -vmid " + id);
     }
 
     @Override
     public void suspend() {
         runCommand("suspend-vm -vmid " + id);
     }
 
     @Override
     public void restore() {
         runCommand("restore-vm -vmid " + id);
     }
 
     @Override
     public void migrate() {
     }
 
     @Override
     public void move() {
     }
 
     @Override
     public void detach() {
     }
 
     @Override
     public void changeCD() {
     }
 
     @Override
     public void ejectCD() {
     }
 }
