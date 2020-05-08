 /*
  * Owl Platform Solver-Aggregator Library for Java
  * Copyright (C) 2012 Robert Moore and the Owl Platform
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package com.owlplatform.solver.rules;
 
 import java.util.Arrays;
 import java.util.Collection;
import java.util.HashSet;
 
 import com.owlplatform.common.SampleMessage;
import com.owlplatform.common.util.HashableByteArray;
 import com.owlplatform.solver.protocol.messages.Transmitter;
 
 /**
  * A class representing a single subscription request rule in the
  * Solver-Aggregator protocol. A subscription request rule is defined as a
  * physical layer identifier, a set of device ID and mask values (as an array of
  * {@link Transmitter}), and an update interval specified in milliseconds.
  * 
  * @author Robert Moore
  * 
  */
 public class SubscriptionRequestRule {
 
   /**
    * Physical layer identifier for the devices in this rule.
    */
   private byte physicalLayer = SampleMessage.PHYSICAL_LAYER_ALL;
 
   /**
    * The set of transmitter values that this rule contains.
    */
   private Transmitter[] transmitters;
 
   /**
    * The minimum frequency to accept updates from the aggregator, in
    * milliseconds.
    */
   private long updateInterval = 0l;
 
   /**
    * Gets the current physical layer identifier for this rule.
    * 
    * @return the current physical layer identifier for this rule.
    * @see SampleMessage#PHYSICAL_LAYER_ALL
    */
   public byte getPhysicalLayer() {
     return this.physicalLayer;
   }
 
   /**
    * Sets the new physical layer identifier value for this rule.
    * 
    * @param physicalLayer
    *          the new value.
    * @see SampleMessage#PHYSICAL_LAYER_ALL
    */
   public void setPhysicalLayer(byte physicalLayer) {
     this.physicalLayer = physicalLayer;
   }
 
   /**
    * The number of transmitter values this rule contains.
    * 
    * @return the number of transmitter values this rule contains.
    */
   public int getNumTransmitters() {
     if (this.transmitters == null) {
       return 0;
     }
     return this.transmitters.length;
   }
 
   /**
    * Returns the transmitters defined in this rule.
    * 
    * @return the transmitters defined in this rule.
    */
   public Transmitter[] getTransmitters() {
     return this.transmitters;
   }
 
   /**
    * Sets the transmitters for this rule. Any previous values are discarded.
    * 
    * @param transmitters
    *          the new transmitters.
    */
   public void setTransmitters(Transmitter[] transmitters) {
     this.transmitters = transmitters;
   }
 
   /**
    * Sets the transmitters for this rule. Any previous values are discarded.
    * 
    * @param transmitters
    *          the new transmitters.
    */
   public void setTransmitters(Collection<Transmitter> transmitters) {
     if (transmitters == null) {
       this.transmitters = null;
       return;
     }
     int size = transmitters.size();
     if (size == 0) {
       this.transmitters = null;
       return;
     }
    this.transmitters = transmitters.toArray(new Transmitter[] {});

   }
 
   /**
    * Returns the minimum interval, in milliseconds, that updates from the
    * aggregator should arrive.
    * 
    * @return the minimum update interval.
    */
   public long getUpdateInterval() {
     return this.updateInterval;
   }
 
   /**
    * Sets the new minimum interval, in milliseconds, that updates from the
    * aggregator should arrive.
    * 
    * @param updateInterval
    *          the new value.
    */
   public void setUpdateInterval(long updateInterval) {
     this.updateInterval = updateInterval;
   }
 
   /**
    * Generates a new {@code SubscriptionRequestRule} with a physical layer
    * identifier of {@link SampleMessage#PHYSICAL_LAYER_ALL}, an update interval
    * of 0 milliseconds, and no transmitters. This rule will effectively request
    * ALL data from an aggregator.
    * 
    * @return the new rule.
    */
   public static SubscriptionRequestRule generateGenericRule() {
     SubscriptionRequestRule rule = new SubscriptionRequestRule();
 
     rule.setUpdateInterval(0l);
 
     rule.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_ALL);
 
     return rule;
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("Subscription rule: ");
     sb.append("Phy (").append(this.getPhysicalLayer());
     sb.append(") Txers {");
     if (this.getNumTransmitters() > 0) {
       boolean isFirst = true;
       for (Transmitter txer : this.getTransmitters()) {
         if (!isFirst) {
           sb.append(", ");
         }
         sb.append(txer);
         isFirst = false;
       }
     }
     sb.append("} Interval: ").append(this.getUpdateInterval()).append("ms");
 
     return sb.toString();
   }
 
   @Override
   public boolean equals(Object o) {
     if (o instanceof SubscriptionRequestRule) {
       return this.equals((SubscriptionRequestRule) o);
     }
     return super.equals(o);
   }
 
   /**
    * Compares this to another {@code SubscriptionRequestRule} by comparing the
    * phsycial layer ID values, update intervals, and transmitters for equality.
    * 
    * @param rule
    *          another {@code SubscriptionRequestRule}
    * @return {@code true} if both rules are equivalent, else {@code false}.
    */
   public boolean equals(SubscriptionRequestRule rule) {
     if (this.physicalLayer != rule.physicalLayer) {
       return false;
     }
     if (this.updateInterval != rule.updateInterval) {
       return false;
     }
     if (this.getNumTransmitters() != rule.getNumTransmitters()) {
       return false;
     }
    HashSet<Transmitter> matchedTx = new HashSet<Transmitter>();
     if (this.transmitters != null) {
       // Check all transmitters
       for (Transmitter txer : this.transmitters) {
         boolean matched = false;
         for (Transmitter oTxer : rule.transmitters) {
          if (!matchedTx.contains(oTxer)
              && Arrays.equals(txer.getBaseId(), oTxer.getBaseId())
               && Arrays.equals(txer.getMask(), oTxer.getMask())) {
            matchedTx.add(oTxer);
             matched = true;
             break;
           }
         }
         if (!matched) {
           return false;
         }
       }
     }
     return true;
 
   }
 
   @Override
   public int hashCode() {
     int hashcode = this.physicalLayer;
     hashcode ^= this.updateInterval;
     hashcode ^= this.updateInterval >> 8;
     if (this.transmitters != null) {
       for (Transmitter t : this.transmitters) {
         hashcode ^= t.hashCode();
       }
     }
     return hashcode;
   }
 }
