 /**
  * Copyright (c) 2004, Regents of the University of California
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
 
 package avrora.sim.util;
 
 import avrora.core.Instr;
 import avrora.sim.Simulator;
 import avrora.sim.State;
 
 /**
  * The <code>MulticastProbe</code> is a wrapper around multiple watches that allows them to act as a single
  * watch. It is useful for composing multiple watches into one and is used internally in the simulator.
  *
  * @author Ben L. Titzer
  * @see Simulator
  */
 public class MulticastWatch implements Simulator.Watch {
 
     /**
      * The <code>Link</code> class is used internally to implement the linked list of the watches. It exists
      * because a simple, custom list structure allows for the most efficient dispatching code possible.
      * Performance is critical since the multicast watch may be called for every instruction executed in the
      * simulator.
      */
     private static class Link {
         final Simulator.Watch probe;
         Link next;
 
         Link(Simulator.Watch p) {
             probe = p;
         }
     }
 
     private Link head;
     private Link tail;
 
     /**
      * The <code>add()</code> method allows another watch to be inserted into the multicast set. It will be
      * inserted at the end of the list of current watch and will therefore fire after any probes already in
      * the multicast set.
      *
      * @param b the watch to insert
      */
     public void add(Simulator.Watch b) {
         if (b == null) return;
 
         if (head == null) {
             head = tail = new Link(b);
         } else {
             tail.next = new Link(b);
             tail = tail.next;
         }
     }
 
     /**
      * The <code>remove</code> method removes a watch from the multicast set. The order of the remaining
      * probes is not changed. The comparison used is reference equality, not the <code>.equals()</code>
      * method.
      *
      * @param b the watch to remove
      */
     public void remove(Simulator.Watch b) {
         if (b == null) return;
 
         Link prev = null;
         Link pos = head;
         while (pos != null) {
             Link next = pos.next;
 
             if (pos.probe == b) {
                // remove the whole thing.
                if (prev == null)
                    head = pos.next;
                else
                    prev.next = pos.next;
             } else {
                 prev = pos;
             }
             pos = next;
         }
     }
 
     /**
      * The <code>isEmpty()</code> method tests whether the multicast set of this watch is empty.
      *
      * @return false otherwise
      */
     public boolean isEmpty() {
         return head == null;
     }
 
     /**
      * The <code>fireBeforeRead()</code> method is called before the probed address is read by the program. In
      * the implementation of the multicast probe, it simply calls the <code>fireBeforeRead()</code> method on
      * each of the probes in the multicast set in the order in which they were inserted.
      *
      * @param i       the instruction being probed
      * @param address the address at which this instruction resides
      * @param state   the state of the simulation
      * @param val     the value of the memory location being read
      */
     public void fireBeforeRead(Instr i, int address, State state, int data_addr, byte val) {
         for (Link pos = head; pos != null; pos = pos.next)
             pos.probe.fireBeforeRead(i, address, state, data_addr, val);
     }
 
     /**
      * The <code>fireAfterRead()</code> method is called after the probed address is read by the program. In
      * the implementation of the multicast probe, it simply calls the <code>fireAfterRead()</code> method on
      * each of the probes in the multicast set in the order in which they were inserted.
      *
      * @param i       the instruction being probed
      * @param address the address at which this instruction resides
      * @param state   the state of the simulation
      * @param val     the value of the memory location being read
      */
     public void fireAfterRead(Instr i, int address, State state, int data_addr, byte val) {
         for (Link pos = head; pos != null; pos = pos.next)
             pos.probe.fireAfterRead(i, address, state, data_addr, val);
     }
 
     /**
      * The <code>fireBeforeWrite()</code> method is called before the probed address is written by the
      * program. In the implementation of the multicast probe, it simply calls the
      * <code>fireBeforeWrite()</code> method on each of the probes in the multicast set in the order in which
      * they were inserted.
      *
      * @param i       the instruction being probed
      * @param address the address at which this instruction resides
      * @param state   the state of the simulation
      * @param val     the value being written to the memory location
      */
     public void fireBeforeWrite(Instr i, int address, State state, int data_addr, byte val) {
         for (Link pos = head; pos != null; pos = pos.next)
             pos.probe.fireBeforeWrite(i, address, state, data_addr, val);
     }
 
     /**
      * The <code>fireAfterWrite()</code> method is called after the probed address is written by the program.
      * In the implementation of the multicast probe, it simply calls the <code>fireAfterWrite()</code> method
      * on each of the probes in the multicast set in the order in which they were inserted.
      *
      * @param i       the instruction being probed
      * @param address the address at which this instruction resides
      * @param state   the state of the simulation
      * @param val     the value being written to the memory location
      */
     public void fireAfterWrite(Instr i, int address, State state, int data_addr, byte val) {
         for (Link pos = head; pos != null; pos = pos.next)
             pos.probe.fireAfterWrite(i, address, state, data_addr, val);
     }
 
 
 }
