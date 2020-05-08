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
 
 package com.quartercode.disconnected.test.sim;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import com.quartercode.disconnected.sim.Simulation;
 import com.quartercode.disconnected.sim.member.Member;
 
 public class SimulationTest {
 
     private Simulation simulation;
 
    public SimulationTest() {

    }

     @Before
     public void setUp() {
 
         simulation = new Simulation();
         simulation.addMember(new Member("member1"));
         simulation.addMember(new Member("member2"));
     }
 
     @Test
     public void testGetMembers() {
 
         Assert.assertEquals("Member count", 2, simulation.getMembers().size());
     }
 
     @Test
     public void testGetMember() {
 
         Assert.assertEquals("Member exists", simulation.getMembers().get(1), simulation.getMember("member2"));
     }
 
     @Test
     public void testAddMember() {
 
         simulation.addMember(new Member("member3"));
         Assert.assertEquals("Member count", 3, simulation.getMembers().size());
     }
 
     @Test
     public void testRemoveMember() {
 
         simulation.removeMember(simulation.getMembers().get(0));
         Assert.assertEquals("Member count", 1, simulation.getMembers().size());
     }
 
 }
