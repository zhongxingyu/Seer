 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.common.model.entity;
 
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 /**
  * @author Kirill Afonin
  * @author Alexey Malev
  */
 public class EntityTest {
     private class EntityObject extends Entity {
         public EntityObject(String uuid) {
             this.setUuid(uuid);
         }
 
         public EntityObject() {
             this.setUuid(java.util.UUID.randomUUID().toString());
         }
     }
 
     private Entity first;
     private Entity second;
     private Entity third;
 
     @Test
     public void testEqualsSymmetry() {
        String uuid = java.util.UUID.randomUUID().toString();
 
         first = new EntityObject(uuid);
         second = new EntityObject(uuid);
 
         assertTrue(first.equals(second));
         assertTrue(second.equals(first));
     }
 
     @Test
     public void testEqualsReflexivity() {
         first = new EntityObject();
 
         assertTrue(first.equals(first));
     }
 
     @Test
     public void testEqualsNull() {
         first = new EntityObject();
 
         assertFalse(first.equals(null));
     }
 
     @Test
     public void testEqualsTransitivity() {
        String uuid = java.util.UUID.randomUUID().toString();
 
         first = new EntityObject(uuid);
         second = new EntityObject(uuid);
         third = new EntityObject(uuid);
 
         assertTrue(first.equals(second));
         assertTrue(second.equals(third));
         assertTrue(first.equals(third));
     }
 
     @Test
     public void testEqualsWhenDifferentId() {
         first = new EntityObject("id1");
         second = new EntityObject("id2");
 
         assertFalse(first.equals(second));
         assertFalse(second.equals(first));
     }
 
     @Test
    public void testEqualsWhenDifferentClasses() {
        String id = "id";
        first = new EntityObject(id);
        second = new Entity() {
        };
        second.setUuid(id);

        assertFalse(first.equals(second));
    }

    @Test
     public void testEqualsInheritance(){
        first = new EntityObject("1");
         second = new Entity() {
         };
        second.setId(2);
 
         assertFalse(first.equals(second));
     }
 
     /***
      * Tests the persistency of Entity ID - positive case.
      */
     @Test
     public void testPersistencyPresence(){
         long id = 50;
         first = new Entity(){
         };
         first.setId(id);
 
         assertTrue(first.isPersistent());
     }
 
     /***
      * Tests the persistency of Entity ID - negative case.
      */
     @Test
     public void testPersistencyAbsence(){
         long zero = 0;
         first = new Entity(){
         };
         first.setId(zero);
 
         assertFalse(first.isPersistent());
     }
 
     @Test
     public void testHashCode() {
         first = new EntityObject("uid1");
         second = new EntityObject("uid1");
 
         assertEquals(first.hashCode(), second.hashCode());
     }
 }
