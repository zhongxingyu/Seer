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
 package com.redhat.rhevm.api.command.vms;
 
 import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
 
 import com.redhat.rhevm.api.command.base.AbstractUpdateCommand;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.model.VMs;
 
 /**
  * Update a VM
  */
@Command(scope = "vms", name = "update", description = "Update Virtual Machine state")
 public class VmsUpdateCommand extends AbstractUpdateCommand {
 
     @Argument(index = 0, name = "name", description = "The name of the VM", required = true, multiValued = false)
     protected String name;
 
     protected Object doExecute() throws Exception {
         VM vm = doUpdate(client.getCollection("vms", VMs.class).getVMs(), VM.class, name);
         display(vm);
         return null;
     }
 
     protected void display(VM vm) {
         if (vm != null) {
             StringBuffer print = new StringBuffer("[NAME");
             pad(print, vm.getName(), "NAME").append("] [ID");
             pad(print, vm.getId(), "ID").append("]\n[");
             pad(print, "NAME", vm.getName()).append(vm.getName()).append("] [");
             pad(print, "ID", vm.getId()).append(vm.getId()).append("]\n");
             System.out.print(print.toString());
         }
     }
 
     protected StringBuffer pad(StringBuffer sb, String longer, String shorter) {
         for (int i = 0 ; i < longer.length() - shorter.length() ; i++) {
             sb.append(" ");
         }
         return sb;
     }
 }
 
