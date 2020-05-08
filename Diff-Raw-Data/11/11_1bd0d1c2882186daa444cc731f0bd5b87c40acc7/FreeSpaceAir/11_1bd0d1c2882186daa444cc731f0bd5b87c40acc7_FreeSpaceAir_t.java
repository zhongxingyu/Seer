 /**
  * Created on 01.11.2004
  *
  * Copyright (c) 2004-2005, Olaf Landsiedel, Protocol Engineering and
  * Distributed Systems, University of Tuebingen
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the Protocol Engineering and Distributed Systems
  * Group, the name of the University of Tuebingen nor the names of its 
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.sim.radio.freespace;
 
 import avrora.sim.Simulator;
 import avrora.sim.SimulatorThread;
 import avrora.sim.radio.Radio;
 import avrora.sim.radio.RadioAir;
 import avrora.sim.clock.IntervalSynchronizer;
 import avrora.util.Verbose;
 import avrora.Avrora;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.TreeSet;
 import java.util.HashMap;
 
 /**
  * Implementation of the free space radio propagation model
  *
  * @author Olaf Landsiedel
  *         <p/>
  *         Yes, the free space radio model is not very realistic, but this is not the purpose of thie
  *         implementation. It shall model the characteristics of radio propagation and so enable multihop
  *         scenarios. This implementation bases heavily on the SimpleAir class by Daniel Lee. However, the
  *         changes needed, where to heavy to allow for standard class extension
  */
 public class FreeSpaceAir implements RadioAir {
 
     final HashMap airMap;
 
     // all radios
     protected final HashSet radios;
 
     final IntervalSynchronizer synchronizer;
 
     final Topology topology;
 
     /**
      * State for maintaining RSSI wait for neighbors
      */
     int rssi_count;
     int meet_count;
     TreeSet rssi_waiters;
 
     /**
      * The amount of cycles it takes for one byte to be sent.
      */
     public static final int bytePeriod = Radio.TRANSFER_TIME;
     public static final int bitPeriod = Radio.TRANSFER_TIME / 8;
     public static final int bitPeriod2 = Radio.TRANSFER_TIME / 16;
 
     //const. for radio propagation
     private static final double lightTemp = 299792458 / (4 * Math.PI);
     private static final double lightConst = lightTemp * lightTemp;
     private static final double noiseCutOff = 0.000009;
 
     /**
      * new free space air
      */
     public FreeSpaceAir(Topology top) {
         topology = top;
         radios = new HashSet();
         synchronizer = new IntervalSynchronizer(bytePeriod, new MeetEvent());
         rssi_waiters = new TreeSet();
         airMap = new HashMap();
     }
 
     protected class MeetEvent implements Simulator.Event {
         int meets;
 
         public void fire() {
             meets++;
             long globalTime = meets * bytePeriod;
             Iterator it = radios.iterator();
             while (it.hasNext()) {
                 Radio r = (Radio)it.next();
                 LocalAirImpl pr = getLocalAir(r);
                 pr.advanceChannel();
             }
         }
     }
 
     public synchronized void addRadio(Radio r) {
         Position p = topology.getPosition(r.getSimulator().getID());
         LocalAirImpl la = new LocalAirImpl(r, p, synchronizer);
         airMap.put(r, la);
         r.setAir(this);
 
         //add this radio to the other radio's neighbor list
         Iterator it = radios.iterator();
         while (it.hasNext()) {
             LocalAirImpl localAir = getLocalAir(((Radio)it.next()));
             //add the new radio to the other radio's neighbor list
             localAir.addNeighbor(getLocalAir(r));
             //add the other radios to this radio's neighbor list
             getLocalAir(r).addNeighbor(localAir);
         }
         radios.add(r);
         synchronizer.addNode(r.getSimulatorThread());
     }
 
     private LocalAirImpl getLocalAir(Radio r) {
         return (LocalAirImpl)airMap.get(r);
     }
 
     /**
      * remove radio
      *
      * @see avrora.sim.radio.RadioAir#removeRadio(avrora.sim.radio.Radio)
      */
     public synchronized void removeRadio(Radio r) {
         radios.remove(r);
         Iterator it = radios.iterator();
         while (it.hasNext()) {
             LocalAirImpl localAir = getLocalAir(((Radio)it.next()));
             //remove the radio from the other radio's neighbor list
             localAir.removeNeighbor(getLocalAir(r));
         }
     }
 
     /**
      * transmit packet
      *
      * @see avrora.sim.radio.RadioAir#transmit(avrora.sim.radio.Radio, avrora.sim.radio.Radio.Transmission)
      */
     public synchronized void transmit(Radio r, Radio.Transmission f) {
         //compute transmission range, incl. noise
         //first compute tranmission power in Watt
         double powerSet = (double)r.getPower();
         double power;
         //convert to dB (by linearization) and than to Watts
         //for linearization, we distinguish values less than 16
         //and higher ones. Probably a lookup table and Spline
         //interpolation would be nice here
         if (powerSet < 16)
             power = Math.pow(10, (0.12 * powerSet - 1.8));
         else
             power = Math.pow(10, (0.00431 * powerSet - 0.06459));
         
         //second compute free space formula (without distance)
         //SignalRec = SignalSend * lightTerm * (1 / ( distance * freq))^2;
         // where lightTerm is ( c / ( 4Pi ))^2
         double freq = r.getFrequency();
         double temp = power * lightConst * (1 / (freq * freq));
         // send packet to devices in ranges
         Iterator it = getLocalAir(r).getNeighbors();
         while (it.hasNext()) {
             Distance dis = (Distance)it.next();
             double powerRec = temp / (dis.distance * dis.distance);
             //check if device is in range
             if (powerRec > noiseCutOff) {
                 dis.radio.addPacket(f, powerRec, r);
             }
         }
 
     }
 
     /**
      * see simple air for more
      *
      * @see avrora.sim.radio.RadioAir#sampleRSSI(avrora.sim.radio.Radio)
      */
     public int sampleRSSI(Radio r) {
         Simulator s = r.getSimulator();
         long t = s.getState().getCycles();
         synchronizer.waitForNeighbors(t);
         // we just got woken up (or returned immediately from checkRSSIWaiters
         // this means we are now ready to proceed and compute our RSSI value.
        return getLocalAir(r).sampleRSSI(t);
     }
 
     public byte readChannel(Radio r) {
         LocalAirImpl lair = getLocalAir(r);
         return lair.readChannel();
     }
 }
