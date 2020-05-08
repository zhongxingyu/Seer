 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.sim.comp.hardware;
 
 import java.util.List;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import com.quartercode.disconnected.sim.comp.Computer;
 import com.quartercode.disconnected.sim.comp.Hardware;
 import com.quartercode.disconnected.sim.comp.Version;
 import com.quartercode.disconnected.sim.comp.Vulnerability;
 
 /**
  * This class represents a cpu of a computer.
  * A cpu has a count of possible threads running at the same time and a frequency (given in hertz).
  */
 @XmlAccessorType (XmlAccessType.FIELD)
 public class CPU extends Hardware {
 
     private static final long serialVersionUID = 1L;
 
     private int               threads;
     private long              frequency;
 
     /**
      * Creates a new empty cpu.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     public CPU() {
 
     }
 
     /**
      * Creates a new cpu and sets the computer, the name, the version, the vulnerabilities, the count of possible threads and the frequency.
      * 
      * @param name The name the cpu has.
      * @param version The current version the cpu has.
      * @param vulnerabilities The vulnerabilities the cpu has.
      * @param threads The count of possible threads running at the same time.
      * @param frequency The frequency of the cpu, given in hertz.
      */
     public CPU(Computer computer, String name, Version version, List<Vulnerability> vulnerabilities, int threads, long frequency) {
 
         super(computer, name, version, vulnerabilities);
 
         this.threads = threads;
         this.frequency = frequency;
     }
 
     /**
      * Returns the count of possible threads running at the same time.
      * 
      * @return The count of possible threads running at the same time.
      */
     public int getThreads() {
 
         return threads;
     }
 
     /**
      * Returns the frequency of the cpu, given in hertz.
      * 
      * @return The frequency of the cpu, given in hertz.
      */
     public long getFrequency() {
 
         return frequency;
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + (int) (frequency ^ frequency >>> 32);
         result = prime * result + threads;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (!super.equals(obj)) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         CPU other = (CPU) obj;
         if (frequency != other.frequency) {
             return false;
         }
         if (threads != other.threads) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [threads=" + threads + ", frequency=" + frequency + ", getName()=" + getName() + ", getVersion()=" + getVersion() + ", getVulnerabilities()=" + getVulnerabilities() + "]";
     }
 
 }
