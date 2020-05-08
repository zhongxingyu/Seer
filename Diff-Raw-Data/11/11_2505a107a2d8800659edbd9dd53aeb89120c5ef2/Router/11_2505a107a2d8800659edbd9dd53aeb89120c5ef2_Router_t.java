 /*
  * Copyright (C) 2012 Felix Wiemuth
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package hdyr.core;
 
 import static hdyr.util.Log.log;
 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class Router extends SimObject implements RouterInterface {
 
     private int buffersize; //buffer size in DATAUNIT - this is an "accumulated" buffer, so all buffers share this memory
     private int buffer; //available buffer
     private RoutingAlgorithm alg;
     //ports
     private ArrayList<Link> outPorts = new ArrayList<Link>();
     private ArrayList<RouterInPort> inPorts = new ArrayList<RouterInPort>();
     private LinkedBlockingQueue<SimPacket> fromLAN = new LinkedBlockingQueue<SimPacket>(); //packets received from LAN
     private Host lan = null; //the LAN this router is connected to - 'null' if not
 
     public Router(int buffersize, String name, RoutingAlgorithm alg, SimulationInfo info) {
         super(name, info);
         this.buffersize = buffersize;
         this.alg = alg;
         alg.setRouter(this);
         buffer = buffersize;
         alg.init();
     }
 
     /**
      * To be used when creating a new link.
      * Creates a new router input port for the new link.
      * @return the new input port
      */
     public RouterInPort newInPort() {
         inPorts.add(new RouterInPort(this));
         return inPorts.get(inPorts.size() - 1);
     }
 
     public void addLink(LineType type, Router dest) {
         outPorts.add(new Link(type, this, dest, logname() + "-" + dest.logname(), info()));
     }
 
     //TODO remove
     //TODO make Host.connect(LineType type, Router router) -- NOTE: HOST ^= LAN
     public void addInPort() {
         inPorts.add(new RouterInPort(this));
     }
 
     /**
      * Attach a host (LAN) to the router.
      * @param lan
      * @return false - another LAN is already attached
      *         true - success
      */
     public boolean setLAN(Host lan) {
        if (this.lan != null) {
             return false;
         }
         this.lan = lan;
         return true;
     }
 
     @Override
     public boolean useBuffer(int n) {
         if (buffer >= n) {
             buffer -= n;
             return true;
         }
         return false;
     }
 
     @Override
     public void freeBuffer(int n) {
         buffer += n;
     }
 
     /**
      * TODO still needed?
      * Update 'buffer' to reflect the currently available buffer
      */
     public void updateBuffer() {
         buffer = buffersize;
         for (RouterInPort in : inPorts) {
             buffer -= in.getQueueSize();
         }
         for (Link link : outPorts) {
             buffer -= link.getQueueSize();
         }
     }
 
     public void insertFromLAN(SimPacket p) {
         if (useBuffer(p.packet().getSize())) {
             fromLAN.add(p);
         } else {
             discard(p);
         }
     }
 
     public void simulateStep() {
         alg.onSimulationStep();
     }
 
     @Override
     public ArrayList<RouterInPort> inPorts() {
         return inPorts;
     }
 
     @Override
     public ArrayList<Link> outPorts() {
         return outPorts;
     }
 
     @Override
     public boolean push(RouterInPortInterface srcPort, RouterOutPortInterface destPort) {
         RouterInPort src = (RouterInPort) srcPort;
         Link dest = (Link) destPort;
         if (src.isEmpty()) {
             return false;
         }
         log(this, "Packet inserted into outgoing queue to router " + dest.getDest().logname() + ": " + src.peekSimPacket().name());
         dest.insert(src.poll());
         return true;
     }
 
     @Override
     public boolean pushToLAN(RouterInPortInterface srcPort) {
         RouterInPort src = (RouterInPort) srcPort;
         if (src.isEmpty() || lan == null) {
             return false;
         }
         //TODO log
         lan.insertFromRouter(src.poll());
         return true;
     }
 
     @Override
    public Packet peekLAN() {
        if (fromLAN.isEmpty()) {
            return null;
        }
        return fromLAN.peek().packet();
    }

    @Override
     public boolean pushFromLAN(RouterOutPortInterface destPort) {
         if (fromLAN.isEmpty()) {
             return false;
         }
         Link dest = (Link) destPort;
         //TODO log
         dest.insert(fromLAN.poll());
         return true;
     }
 
     public void discard(SimPacket p) {
         log(this, "Packet discarded because of no buffers available: " + p.name());
     }
 
     @Override
     public void discard(RouterInPortInterface inPort) {
         if (inPort.isEmpty()) {
             return;
         }
         SimPacket p = ((RouterInPort) inPort).poll();
         freeBuffer(p.packet().getSize());
         log(this, "Packet discarded by routing algorithm: " + p.name());
     }
 
     @Override
     public int bufferAvailable() {
         return buffer;
     }
 
     @Override
     public int bufferSize() {
         return buffersize;
     }
 }
