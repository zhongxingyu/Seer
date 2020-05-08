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
 
 package com.quartercode.disconnected.sim.comp;
 
 import java.util.List;
 
 /**
  * This class stores information about a program.
  * This also contains a list of all vulnerabilities this program has, the operating system the program is written for and the required right level.
  * 
  * @see ComputerPart
  */
 public class Program extends ComputerPart {
 
     private static final long serialVersionUID = 1L;
 
     private String            rightLevel;
 
     /**
      * Creates a new empty program.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     public Program() {
 
     }
 
     /**
      * Creates a new program and sets the computer, the name, the version, the vulnerabilities and the required right level.
      * 
     * @param computer The computer this part is built in.
      * @param name The name the program has.
      * @param version The current version the program has.
      * @param vulnerabilities The vulnerabilities the program has.
      * @param rightLevel The required right level a user need for executing the program.
      */
     protected Program(Computer computer, String name, Version version, List<Vulnerability> vulnerabilities, String rightLevel) {
 
         super(computer, name, version, vulnerabilities);
 
         this.rightLevel = rightLevel;
     }
 
     /**
      * Returns the required right level a user need for executing the program.
      * 
      * @return The required right level a user need for executing the program.
      */
     public String getRightLevel() {
 
         return rightLevel;
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + (rightLevel == null ? 0 : rightLevel.hashCode());
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
         Program other = (Program) obj;
         if (rightLevel == null) {
             if (other.rightLevel != null) {
                 return false;
             }
         } else if (!rightLevel.equals(other.rightLevel)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [rightLevel=" + rightLevel + ", getName()=" + getName() + ", getVersion()=" + getVersion() + ", getVulnerabilities()=" + getVulnerabilities() + "]";
     }
 
 }
