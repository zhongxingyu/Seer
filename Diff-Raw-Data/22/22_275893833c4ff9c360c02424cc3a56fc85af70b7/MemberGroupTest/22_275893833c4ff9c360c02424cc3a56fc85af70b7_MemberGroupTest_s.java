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
 
 package com.quartercode.disconnected.test.sim.member;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import com.quartercode.disconnected.sim.member.Member;
 import com.quartercode.disconnected.sim.member.MemberGroup;
 
 public class MemberGroupTest {
 
     private MemberGroup group;
 
    public MemberGroupTest() {

    }

     @Before
     public void setUp() {
 
         group = new MemberGroup();
         group.addMember(new Member("member1"));
         group.addMember(new Member("member2"));
         group.addInterest(new EmptyInterest(1));
         group.addInterest(new EmptyInterest(1));
     }
 
     @Test
     public void testGetMembers() {
 
         Assert.assertEquals("Member count", 2, group.getMembers().size());
     }
 
     @Test
     public void testGetMember() {
 
         Assert.assertEquals("Member exists", group.getMembers().get(1), group.getMember("member2"));
     }
 
     @Test
     public void testAddMember() {
 
         group.addMember(new Member("member3"));
         Assert.assertEquals("Member count", 3, group.getMembers().size());
     }
 
     @Test
     public void testRemoveMember() {
 
         group.removeMember(group.getMembers().get(0));
         Assert.assertEquals("Member count", 1, group.getMembers().size());
     }
 
     @Test
     public void testGetInterests() {
 
         Assert.assertEquals("Interest count", 2, group.getInterests().size());
     }
 
     @Test
     public void testAddInterest() {
 
         group.addInterest(new EmptyInterest(1));
         Assert.assertEquals("Interest count", 3, group.getInterests().size());
     }
 
     @Test
     public void testRemoveInterest() {
 
         group.removeInterest(group.getInterests().get(0));
         Assert.assertEquals("Member count", 1, group.getInterests().size());
     }
 
 }
