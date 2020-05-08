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
 
 import com.quartercode.disconnected.sim.Simulation;
 import com.quartercode.disconnected.sim.member.Member;
 import com.quartercode.disconnected.sim.member.MemberGroup;
 
 /**
  * This interface defines a class which is able to provide possible reputation changes.
  */
 public interface ReputationChangeProvider {
 
     /**
      * Returns the reputation change of a group to a member if the member executes the interest.
      * 
      * @param simulation The simulation which contains the given member.
      * @param member The member which reputation changes.
      * @param group The group which holds the change perspective.
      * @return The reputation change of a group to a member if the member executes the interest.
      */
     public abstract int getReputationChange(Simulation simulation, Member member, MemberGroup group);
 
}
