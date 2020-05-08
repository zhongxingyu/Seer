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
 
 package org.ow2.sirocco.apis.rest.cimi.sdk;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.ActionType;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiAction;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiAddress;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiJob;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachine;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineNetworkInterface;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineNetworkInterfaceAddress;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineCollectionRoot;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineNetworkInterfaceAddressCollectionRoot;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineNetworkInterfaceCollectionRoot;
 import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
 
 public class Machine extends Resource<CimiMachine> {
     public static enum State {
         CREATING, STARTING, STARTED, STOPPING, STOPPED, PAUSING, PAUSED, SUSPENDING, SUSPENDED, DELETING, DELETED, ERROR
     }
 
     public Machine(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiMachine());
         this.cimiObject.setHref(id);
     }
 
     Machine(final CimiClient cimiClient, final CimiMachine cimiMachine) {
         super(cimiClient, cimiMachine);
     }
 
     public CimiMachine getCimiMachine() {
         return this.cimiObject;
     }
 
     public State getState() {
         return State.valueOf(this.cimiObject.getState());
     }
 
     public int getCpu() {
         return this.cimiObject.getCpu();
     }
 
     public int getMemory() {
         return this.cimiObject.getMemory();
     }
 
     public List<NetworkInterface> getNetworkInterface() throws CimiException {
         List<NetworkInterface> nics = new ArrayList<NetworkInterface>();
        if (this.cimiObject.getNetworkInterfaces() != null && this.cimiObject.getNetworkInterfaces().getArray() != null) {
             for (CimiMachineNetworkInterface cimiNic : this.cimiObject.getNetworkInterfaces().getArray()) {
                 if (cimiNic.getAddresses().getArray() == null) {
                     CimiMachineNetworkInterfaceAddressCollectionRoot addresses = this.cimiClient.getRequest(
                         this.cimiClient.extractPath(cimiNic.getAddresses().getHref()),
                         CimiMachineNetworkInterfaceAddressCollectionRoot.class, -1, -1, null);
                     cimiNic.setAddresses(addresses);
                    if (addresses.getArray() != null) {
                        for (CimiMachineNetworkInterfaceAddress nicAddr : addresses.getArray()) {
                            CimiAddress cimiAddress = this.cimiClient.getCimiObjectByReference(nicAddr.getAddress().getHref(),
                                CimiAddress.class);
                            nicAddr.setAddress(cimiAddress);
                        }
                     }
                 }
                 String ip = "";
                if (cimiNic.getAddresses().getArray() != null && cimiNic.getAddresses().getArray().length > 0) {
                     ip = cimiNic.getAddresses().getArray()[0].getAddress().getIp();
                 }
                 NetworkInterface nic = new NetworkInterface(
                     cimiNic.getNetworkType().equalsIgnoreCase("public") ? NetworkInterface.Type.PUBLIC
                         : NetworkInterface.Type.PRIVATE, ip);
                 nics.add(nic);
             }
         }
         return nics;
     }
 
     public Job start() throws CimiException {
         CimiAction actionStart = new CimiAction();
         actionStart.setAction(ActionType.START.getPath());
         CimiJob cimiObject = this.cimiClient.postRequest(this.cimiClient.extractPath(this.getId()), actionStart, CimiJob.class);
         return new Job(this.cimiClient, cimiObject);
     }
 
     public Job stop() throws CimiException {
         CimiAction actionStart = new CimiAction();
         actionStart.setAction(ActionType.STOP.getPath());
         CimiJob cimiObject = this.cimiClient.postRequest(this.cimiClient.extractPath(this.getId()), actionStart, CimiJob.class);
         return new Job(this.cimiClient, cimiObject);
     }
 
     public Job delete() throws CimiException {
         CimiJob cimiObject = this.cimiClient.deleteRequest(this.cimiClient.extractPath(this.getId()), CimiJob.class);
         return new Job(this.cimiClient, cimiObject);
     }
 
     public static Job createMachine(final CimiClient client, final MachineCreate machineCreate) throws CimiException {
         CimiJob cimiObject = client.postRequest(ConstantsPath.MACHINE_PATH, machineCreate.cimiMachineCreate, CimiJob.class);
         return new Job(client, cimiObject);
     }
 
     public static List<Machine> getMachines(final CimiClient client, final int first, final int last,
         final String... filterExpression) throws CimiException {
         CimiMachineCollection machinesCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getMachines().getHref()), CimiMachineCollectionRoot.class, first, last,
             null, filterExpression);
         List<Machine> result = new ArrayList<Machine>();
 
         if (machinesCollection.getCollection() != null) {
             for (CimiMachine cimiMachine : machinesCollection.getCollection().getArray()) {
                 result.add(new Machine(client, cimiMachine));
             }
         }
         return result;
     }
 
     public static Machine getMachineByReference(final CimiClient client, final String ref) throws CimiException {
         Machine result = new Machine(client, client.getCimiObjectByReference(ref, CimiMachine.class));
        if (result.cimiObject.getNetworkInterfaces() != null) {
            String machineNicsRef = result.cimiObject.getNetworkInterfaces().getHref();
            CimiMachineNetworkInterfaceCollectionRoot nics = client.getRequest(client.extractPath(machineNicsRef),
                CimiMachineNetworkInterfaceCollectionRoot.class, -1, -1, null);
            result.cimiObject.getNetworkInterfaces().setArray(nics.getArray());
        }
         return result;
     }
 
     public static Machine getMachineById(final CimiClient client, final String id) throws Exception {
         String path = client.getMachinesPath() + "/" + id;
         return new Machine(client, client.getCimiObjectByReference(path, CimiMachine.class));
     }
 
 }
