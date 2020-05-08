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
  * This class represents a ram module of a computer.
  * A ram module has a size (given in bytes) and an access frequency (given in hertz).
 * 
 * @see Hardware
  */
 @XmlAccessorType (XmlAccessType.FIELD)
 public class RAM extends Hardware {
 
     private static final long serialVersionUID = 1L;
 
     private long              size;
     private long              frequency;
 
     /**
      * Creates a new empty ram module.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     public RAM() {
 
     }
 
     /**
      * Creates a new ram module and sets the computer, the name, the version, the vulnerabilities, the size and the access frequency.
      * 
     * @param computer The computer this part is built in.
      * @param name The name the ram module has.
      * @param version The current version the ram module has.
      * @param vulnerabilities The vulnerabilities the ram module has.
      * @param size The size of the ram module, given in bytes.
      * @param frequency The access frequency of the ram module, given in hertz.
      */
     public RAM(Computer computer, String name, Version version, List<Vulnerability> vulnerabilities, long size, long frequency) {
 
         super(computer, name, version, vulnerabilities);
 
         this.size = size;
         this.frequency = frequency;
     }
 
     /**
      * Returns the size of the ram module, given in bytes.
      * 
      * @return The size of the ram module, given in bytes.
      */
     public long getSize() {
 
         return size;
     }
 
     /**
      * Returns the access frequency of the ram module, given in hertz.
      * 
      * @return The access frequency of the ram module, given in hertz.
      */
     public long getFrequency() {
 
         return frequency;
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + (int) (frequency ^ frequency >>> 32);
         result = prime * result + (int) (size ^ size >>> 32);
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
         RAM other = (RAM) obj;
         if (frequency != other.frequency) {
             return false;
         }
         if (size != other.size) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [size=" + size + ", frequency=" + frequency + ", getName()=" + getName() + ", getVersion()=" + getVersion() + ", getVulnerabilities()=" + getVulnerabilities() + "]";
     }
 
 }
