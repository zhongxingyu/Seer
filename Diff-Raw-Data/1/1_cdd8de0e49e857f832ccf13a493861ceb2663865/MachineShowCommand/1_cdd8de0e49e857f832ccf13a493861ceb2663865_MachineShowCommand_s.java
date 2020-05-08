 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 package org.ow2.sirocco.cimi.tools;
 
 import java.util.List;
 
 import org.nocrala.tools.texttablefmt.Table;
 import org.ow2.sirocco.cimi.sdk.CimiClient;
 import org.ow2.sirocco.cimi.sdk.CimiClientException;
 import org.ow2.sirocco.cimi.sdk.Disk;
 import org.ow2.sirocco.cimi.sdk.Machine;
 import org.ow2.sirocco.cimi.sdk.MachineNetworkInterface;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.ParametersDelegate;
 
 @Parameters(commandDescription = "show machine")
 public class MachineShowCommand implements Command {
     @Parameter(description = "<machine id>", required = true)
     private List<String> machineIds;
 
     @ParametersDelegate
     private ResourceSelectExpandParams showParams = new ResourceSelectExpandParams();
 
     @Override
     public String getName() {
         return "machine-show";
     }
 
     @Override
     public void execute(final CimiClient cimiClient) throws CimiClientException {
         Machine machine;
         if (CommandHelper.isResourceIdentifier(this.machineIds.get(0))) {
             machine = Machine.getMachineByReference(cimiClient, this.machineIds.get(0), this.showParams.getQueryParams());
         } else {
             List<Machine> machines = Machine.getMachines(cimiClient,
                 this.showParams.getQueryParams().toBuilder().filter("name='" + this.machineIds.get(0) + "'").build());
             if (machines.isEmpty()) {
                 System.err.println("No machine with name " + this.machineIds.get(0));
                 System.exit(-1);
             }
             machine = machines.get(0);
         }
        machine = Machine.getMachineByReference(cimiClient, this.machineIds.get(0), this.showParams.getQueryParams());
         MachineShowCommand.printMachine(machine, this.showParams);
     }
 
     public static void printMachine(final Machine machine, final ResourceSelectExpandParams showParams)
         throws CimiClientException {
         Table table = CommandHelper.createResourceShowTable(machine, showParams);
 
         if (showParams.isSelected("state") && machine.getState() != null) {
             table.addCell("state");
             table.addCell(machine.getState().toString());
         }
         if (showParams.isSelected("cpu") && machine.getCpu() != null) {
             table.addCell("cpu");
             table.addCell(Integer.toString(machine.getCpu()));
         }
         if (showParams.isSelected("memory") && machine.getMemory() != null) {
             table.addCell("memory");
             table.addCell(CommandHelper.printKibibytesValue(machine.getMemory()));
         }
 
         if (showParams.isSelected("disks")) {
             List<Disk> disks = machine.getDisks();
             if (disks != null) {
                 table.addCell("disks");
                 StringBuffer sb = new StringBuffer();
 
                 for (int i = 0; i < disks.size(); i++) {
                     if (i > 0) {
                         sb.append(", ");
                     }
                     sb.append(CommandHelper.printKilobytesValue(disks.get(i).getCapacity()));
                 }
                 table.addCell((sb.toString()));
             }
         }
 
         if (showParams.isSelected("networkInterfaces")) {
             List<MachineNetworkInterface> nics = machine.getNetworkInterfaces();
             if (nics != null) {
                 for (MachineNetworkInterface nic : machine.getNetworkInterfaces()) {
                     if (!nic.getAddresses().isEmpty()) {
                         if (nic.getNetwork() != null) {
                             table.addCell(nic.getNetwork().getNetworkType().toLowerCase() + " IP");
                         } else {
                             table.addCell(nic.getType().toString().toLowerCase() + " IP");
                         }
                         table.addCell(nic.getAddresses().get(0).getIp());
                     }
                 }
             }
         }
 
         System.out.println(table.render());
     }
 
 }
