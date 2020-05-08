 /**
  * Copyright (c) 2004-2005, Regents of the University of California
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
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
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
 
 package avrora.sim.radio;
 
 import avrora.sim.Simulator;
 import avrora.sim.SimulatorThread;
 import avrora.sim.clock.IntervalSynchronizer;
 import avrora.util.Verbose;
 import avrora.util.Arithmetic;
 import avrora.Avrora;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.TreeSet;
 
 /**
  * Very simple implementation of radio air. It assumes a lossless environment where all radios are able to
  * communicate with each other. This simple air is blind to the frequencies used in transmission (i.e. it
  * assumes that all frequencies are really the same).
  * <p/>
  * This class should provide the proper scheduling policy with respect to threads that more complicated radio
  * implementations can use the time scheduling policy and only overload the delivery policy.
  *
  * @author Daniel Lee
  * @author Ben L. Titzer
  */
 public class SimpleAir implements RadioAir {
 
     protected final HashSet radios;
 
     protected final Channel radioChannel;
 
     protected final IntervalSynchronizer synchronizer;
 
    private static final int INTERVALS = 1;
     private static final int sampleTime = 13 * 64;
     private static final int TRANSFER_TIME = Radio.TRANSFER_TIME;
     private static final int INTERVAL_TIME = TRANSFER_TIME * INTERVALS;
 
     public SimpleAir() {
         radios = new HashSet();
         radioChannel = new Channel(8 * INTERVALS, INTERVAL_TIME, true);
         synchronizer = new IntervalSynchronizer(INTERVAL_TIME, new MeetEvent());
     }
 
     /**
      * The <code>addRadio()</code> method adds a new radio to this radio model.
      * @param r the radio to add to this air implementation
      */
     public synchronized void addRadio(Radio r) {
         radios.add(r);
         r.setAir(this);
         synchronizer.addNode(r.getSimulatorThread());
     }
 
     /**
      * The <code>removeRadio()</code> method removes a radio from this radio model.
      * @param r the radio to remove from this air implementation
      */
     public synchronized void removeRadio(Radio r) {
         radios.remove(r);
         synchronizer.removeNode(r.getSimulatorThread());
     }
 
     /**
      * The <code>transmit()</code> method is called by a radio when it begins to transmit
      * a packet over the air. The radio packet should be delivered to those radios in
      * range which are listening, according to the radio model.
      * @param r the radio transmitting this packet
      * @param f the radio packet transmitted into the air
      */
     public synchronized void transmit(Radio r, Radio.Transmission f) {
         radioChannel.write(f.data, 8, r.getSimulator().getClock().getCount());
     }
 
     protected class MeetEvent implements Simulator.Event {
         long meets;
 
         public void fire() {
             radioChannel.advance();
         }
     }
 
     /**
      * The <code>sampleRSSI()</code> method is called by a radio when it wants to
      * sample the RSSI value of the air around it at the current time. The air may
      * need to block (i.e. wait for neighbors) because this thread may be ahead
      * of other threads in global time. The underlying air implementation should use
      * a <code>Synchronizer</code> for this purpose.
      * @param r the radio sampling the RSSI value
      * @return an integer value representing the received signal strength indicator
      */
     public int sampleRSSI(Radio r) {
         long t = r.getSimulator().getClock().getCount();
         synchronizer.waitForNeighbors(t);
         return radioChannel.occupied(t - sampleTime, t) ? 0x0 : 0x3ff;
     }
 
     public byte readChannel(Radio r) {
         Simulator sim = r.getSimulator();
         long time = sim.getClock().getCount();
         synchronizer.waitForNeighbors(time);
         return (byte)radioChannel.read(time, 8);
     }
 }
