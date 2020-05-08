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
 
 package com.quartercode.disconnected.test.sim.comp;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import com.quartercode.disconnected.sim.comp.Location;
 
 public class LocationTest {
 
     private Location location;
 
     @Before
     public void setUp() throws Exception {
 
         location = new Location(0.1F, 0.2F);
     }
 
     @Test
     public void testGetX() {
 
         Assert.assertEquals(0.1F, location.getX(), 0);
     }
 
     @Test
     public void testSetX() {
 
         location.setX(0.5F);
         Assert.assertEquals(0.5F, location.getX(), 0);
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void testSetXUnvalid() {
 
         location.setX(-0.5F);
     }
 
     @Test
     public void testGetY() {
 
         Assert.assertEquals(0.2F, location.getY(), 0);
     }
 
     @Test
     public void testSetY() {
 
         location.setY(0.5F);
         Assert.assertEquals(0.5F, location.getY(), 0);
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void testSetYUnvalid() {
 
         location.setY(-0.5F);
     }
 
     @Test
     public void testHashCode() {
 
         Location location2 = new Location(location.getX(), location.getY());
         Assert.assertTrue("Hash codes euqals", location.hashCode() == location2.hashCode());
     }
 
     @Test
     public void testEquals() {
 
         Location location2 = new Location(location.getX(), location.getY());
         Assert.assertTrue("Euqals", location.equals(location2));
     }
 
     @Test
     public void testNotEquals() {
 
         Location location2 = new Location(1, 1);
         Assert.assertFalse("Not euqals", location.equals(location2));
     }
 
 }
