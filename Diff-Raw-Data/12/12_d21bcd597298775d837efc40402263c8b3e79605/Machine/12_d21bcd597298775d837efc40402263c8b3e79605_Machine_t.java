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
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiJob;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachine;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineCollection;
import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineNetworkInterface;
 import org.ow2.sirocco.apis.rest.cimi.sdk.Machine.NetworkInterface.Type;
 import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
 
 public class Machine extends Resource<CimiMachine> {
     public static enum State {
         CREATING, STARTING, STARTED, STOPPING, STOPPED, PAUSING, PAUSED, SUSPENDING, SUSPENDED, DELETING, DELETED, ERROR
     }
 
     public static class NetworkInterface {
         public static enum Type {
             PUBLIC, PRIVATE
         }
 
         private final Type type;
 
         private final String ip;
 
         public Type getType() {
             return this.type;
         }
 
         public String getIp() {
             return this.ip;
         }
 
         public NetworkInterface(final Type type, final String ip) {
             super();
             this.type = type;
             this.ip = ip;
         }
     }
 
     public Machine(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiMachine());
         this.cimiObject.setHref(id);
         this.cimiObject.setId(id);
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
 
     public List<NetworkInterface> getNetworkInterface() {
         List<NetworkInterface> nics = new ArrayList<NetworkInterface>();
        for (CimiMachineNetworkInterface cimiNic : this.cimiObject.getNetworkInterfaces().getArray()) {
            String ip = "";
            if (cimiNic.getAddresses().getArray().length > 0) {
                ip = cimiNic.getAddresses().getArray()[0].getAddress().getIp();
            }
            NetworkInterface nic = new NetworkInterface(Type.PUBLIC, ip);
             nics.add(nic);
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
 
     public static List<Machine> getMachines(final CimiClient client) throws CimiException {
         CimiMachineCollection machinesCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getMachines().getHref()), CimiMachineCollection.class);
         List<Machine> result = new ArrayList<Machine>();
 
         if (machinesCollection.getCollection() != null) {
             for (CimiMachine cimiMachine : machinesCollection.getCollection().getArray()) {
                 result.add(new Machine(client, cimiMachine));
             }
         }
         return result;
     }
 
     public static Machine getMachineByReference(final CimiClient client, final String ref) throws CimiException {
         return new Machine(client, client.getCimiObjectByReference(ref, CimiMachine.class));
     }
 
     public static Machine getMachineById(final CimiClient client, final String id) throws Exception {
         String path = client.getMachinesPath() + "/" + id;
         return new Machine(client, client.getCimiObjectByReference(path, CimiMachine.class));
     }
 
 }
