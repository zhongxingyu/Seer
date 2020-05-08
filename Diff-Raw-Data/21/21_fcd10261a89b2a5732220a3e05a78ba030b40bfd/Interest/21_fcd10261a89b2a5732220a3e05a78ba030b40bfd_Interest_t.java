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
 
 package com.quartercode.disconnected.sim.member.interest;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlSeeAlso;
 import org.apache.commons.lang.Validate;
 import com.quartercode.disconnected.sim.Simulation;
 import com.quartercode.disconnected.sim.member.Member;
 import com.quartercode.disconnected.sim.member.MemberGroup;
 import com.quartercode.disconnected.sim.run.action.Action;
 
 /**
  * This abstract class represents an interest of a member or a global one from a member group.
  * The interest only stores a general idea. For the actual execution, you need to generate an action object.
  * 
  * @see Member
  * @see MemberGroup
  * @see Action
  */
 @XmlAccessorType (XmlAccessType.FIELD)
 @XmlSeeAlso (SabotageInterest.class)
 public abstract class Interest {
 
     @XmlAttribute
     private int priority;
 
     /**
      * Creates a new empty interest object.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     public Interest() {
 
     }
 
     /**
      * Creates a new interest and sets the priority.
      * 
      * @param priority The priority this interest has for the group or member (must be between 1 and 10, both inclusive).
      */
     public Interest(int priority) {
 
         Validate.isTrue(priority >= 1 && priority <= 10, "Priority must be in range 1 <= x <= 10");
 
         this.priority = priority;
     }
 
     /**
      * Returns the priority the interest has for the group or member.
      * The priority value is located between 1 and 10, both inclusive.
      * 
      * @return The priority the interest has for the group or member.
      */
     public int getPriority() {
 
         return priority;
     }
 
     /**
      * Returns the reputation change of a group to a member if the member executes the interest.
      * 
      * @param simulation The simulation which contains the given member.
      * @param member The member which reputation changes.
      * @param group The group which holds the change perspective.
      * @return The reputation change of a group to a member if the member executes the interest.
      */
     public abstract int getReputationChange(Simulation simulation, Member member, MemberGroup group);
 
     /**
      * Calculates the best avaiable action the given member would use for ecexuting the interest.
      * The calculated action may not be the perfect one, this method should also implement some human roughness.
     * This may also return null if no good action can be found, e.g. if every possibility is to risky.
      * 
      * @param simulation The simulation which contains the given member.
      * @param member The member which should execute the calculated action later.
     * @return The best avaiable action the given member would use for executing the interest. This may be null.
      */
     public abstract Action getAction(Simulation simulation, Member member);
 
 }
