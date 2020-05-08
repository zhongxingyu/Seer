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
 
 package com.quartercode.disconnected.sim.member;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlIDREF;
 import com.quartercode.disconnected.sim.comp.Computer;
 import com.quartercode.disconnected.sim.member.ai.AIController;
import com.quartercode.disconnected.sim.member.interest.Interest;
 import com.quartercode.disconnected.util.InfoString;
 
 /**
  * This class represents a member of a simulation (someone who has a brain containing specific interests and knowledge entries).
  * The member doesn't know his simulation, so you could use one member in multiple simulations.
 * A member also has interests and stores the reputation of a group to him.
  * 
 * @see Interest
 * @see Reputation
  */
 public class Member implements InfoString {
 
     @XmlID
     @XmlElement
     private String                name;
     private Computer              computer;
     private AIController          aiController;
     @XmlElement
     private final List<BrainData> brainData = new ArrayList<BrainData>();
 
     /**
      * Creates a new empty member.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     protected Member() {
 
     }
 
     /**
      * Creates a new member and sets the name.
      * 
      * @param name The name for the new member.
      */
     public Member(String name) {
 
         this.name = name;
     }
 
     /**
      * Returns the name of this member.
      * 
      * @return The name of this member.
      */
     public String getName() {
 
         return name;
     }
 
     /**
      * Returns the computer the member has access on.
      * 
      * @return The computer the member has access on.
      */
     @XmlIDREF
     public Computer getComputer() {
 
         return computer;
     }
 
     /**
      * Sets the computer the member has access on to a new one.
      * 
      * @param computer The new computer the member will have access on
      */
     public void setComputer(Computer computer) {
 
         this.computer = computer;
     }
 
     /**
      * Returns the ai controller which executes the update tick on this member.
      * 
      * @return The ai controller which executes the update tick on this member.
      */
     @XmlElement
     public AIController getAiController() {
 
         return aiController;
     }
 
     /**
      * Sets the ai controller which executes the update tick on this member to a new one.
      * 
      * @param aiController The new ai controller which executes the update tick on this member.
      */
     public void setAiController(AIController aiController) {
 
         this.aiController = aiController;
     }
 
     /**
      * Returns the brain data of this member.
      * 
      * @return The brain data of this member.
      */
     public List<BrainData> getBrainData() {
 
         return Collections.unmodifiableList(brainData);
     }
 
     /**
      * Returns the brain data entries in the computer which have the given type as a superclass.
      * 
      * @param type The type to use for the selection.
      * @return The brain data entries in the computer which have the given type as a superclass.
      */
     public <T> List<T> getBrainData(Class<T> type) {
 
         List<T> brainData = new ArrayList<T>();
         for (BrainData brainDataEntry : this.brainData) {
             if (type.isAssignableFrom(brainDataEntry.getClass())) {
                 brainData.add(type.cast(brainDataEntry));
             }
         }
         return brainData;
     }
 
     /**
      * Adds a brain data entry to the member.
      * 
      * @param brainData The brain data entry to add to the member.
      */
     public void addBrainData(BrainData brainData) {
 
         this.brainData.add(brainData);
     }
 
     /**
      * Removes a brain data entry from the member.
      * 
      * @param brainData The brain data entry to remove from the member.
      */
     public void removeBrainData(BrainData brainData) {
 
         this.brainData.remove(brainData);
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (brainData == null ? 0 : brainData.hashCode());
         result = prime * result + (computer == null ? 0 : computer.hashCode());
         result = prime * result + (name == null ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (! (obj instanceof Member)) {
             return false;
         }
         Member other = (Member) obj;
         if (brainData == null) {
             if (other.brainData != null) {
                 return false;
             }
         } else if (!brainData.equals(other.brainData)) {
             return false;
         }
         if (computer == null) {
             if (other.computer != null) {
                 return false;
             }
         } else if (!computer.equals(other.computer)) {
             return false;
         }
         if (name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toInfoString() {
 
         return name + ", computer " + computer.getId() + ", " + brainData.size() + " brain data entries";
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [" + toInfoString() + "]";
     }
 
 }
