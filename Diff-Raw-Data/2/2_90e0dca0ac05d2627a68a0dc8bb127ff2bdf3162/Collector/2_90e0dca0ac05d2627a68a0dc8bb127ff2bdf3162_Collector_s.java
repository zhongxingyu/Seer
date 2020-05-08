 /*
  * Collector.java
  * Copyright (C) 2011,2012 Wannes De Smet
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
 package org.xenmaster.monitoring.engine;
 
 import com.lmax.disruptor.EventHandler;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 import org.apache.log4j.Logger;
 import org.xenmaster.api.Host;
 import org.xenmaster.controller.BadAPICallException;
 import org.xenmaster.monitoring.data.RRD;
 import org.xenmaster.monitoring.data.RRDUpdates;
 import org.xenmaster.monitoring.data.Record;
 
 /**
  *
  * @created Oct 14, 2011
  *
  * @author double-u
  */
 public class Collector implements EventHandler<Record> {
 
     protected long lastUpdate;
     protected static PriorityQueue<Slot> slots = new PriorityQueue<>();
     protected final static int RESPONSE_DELAY = 1000;
 
     public void boot() {
         createConnections();
     }
 
     /**
      * Do not, under *any* circumstance, put this into the ctor. It will
      * recursively create CachingFacilities
      */
     private void createConnections() {
         ArrayList<Slot> newSlots = new ArrayList<>();
         Slot[] current = slots.toArray(new Slot[slots.size()]);
         try {
             for (Host h : Host.getAll()) {
                 newSlots.add(new Slot(h));
             }
         }
         catch (BadAPICallException ex) {
             Logger.getLogger(getClass()).error("Failed to get all hosts", ex);
         }
 
         slots.clear();
 
         // We want to retain existing connections, remove all that aren't present in the newSlots
         for (Slot ns : newSlots) {
             boolean newSlot = true;
 
             for (int i = 0; i < current.length; i++) {
                 if (current[i].equals(ns)) {
 
                     // If it matches, re-add the current one
                     newSlot = false;
                     slots.add(current[i]);
                     break;
                 }
             }
 
             if (newSlot) {
                 slots.add(ns);
             }
         }
     }
 
     public static abstract class TimingProvider implements Runnable {
 
         protected final Slot getNextSlot() {
             boolean gotWorkToDo = false;
             Slot slot = null;
 
             while (!gotWorkToDo) {
                 slot = slots.peek();
                 if (slot == null) {
                     Logger.getLogger(getClass()).warn("No server slots available. Exiting ...");
                     return null;
                 }
 
                 // Wait until 5 seconds have passed
                 long delta = System.currentTimeMillis() - slot.lastPolled;
                 // 5 wait + maximum response delay = 6 seconds
                 if (delta < 5000 + RESPONSE_DELAY || slot.isBeingProcessed() || !slot.isStable()) {
                     try {
                         long sleepyTime = 5000 - delta;
 
                         if (Math.signum(sleepyTime) == -1.0) {
                             Thread.sleep(5000);
                         } else {
                             Thread.sleep(sleepyTime);
                         }
                     }
                     catch (InterruptedException ex) {
                         Logger.getLogger(getClass()).error("Failed to catch a shut-eye", ex);
                     }
                 }
 
                 if (!slot.isBeingProcessed() && slot.startProcessing()) {
                     gotWorkToDo = true;
                 }
             }
             return slot;
         }
     }
 
     @Override
     public void onEvent(Record t, long l, boolean bln) throws Exception {
         Slot slot = slots.peek();
        if (slot == null || !slot.isBeingProcessed()) {
             return;
         }
 
         if (slot.isUpdate()) {
             URLConnection connection = slot.getConnection();
             if (connection == null) {
                 slot.errorOccurred();
             } else {
                 RRDUpdates updates = RRDUpdates.parse(connection.getInputStream());
 
                 if (updates != null) {
                     t.addLatestData(updates);
                 } else {
                     slot.errorOccurred();
                 }
             }
 
         } else {
             t.setInitialData(RRD.parse(slot.getConnection().getInputStream()));
         }
         slot.processingDone();
     }
 }
