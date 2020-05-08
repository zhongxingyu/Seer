 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.util;
 
 import com.google.common.collect.Lists;
 import java.lang.reflect.Constructor;
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class CollectionUtilsTest {
 
     private List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5);
 
     @Test
     public void testAdjacentTo() {
        assertEquals(
                Lists.newArrayList(2, 3, 4),
                CollectionUtils.<Integer>getAdjacentTo(list, 2, 1));
     }
 
     @Test
     public void testAdjacentToUpperBound() {
        assertEquals(
                Lists.newArrayList(4, 5),
                CollectionUtils.<Integer>getAdjacentTo(list, 4, 1));
     }
 
     @Test
     public void testAdjacentToLowerBound() {
        assertEquals(
                Lists.newArrayList(1, 2),
                CollectionUtils.<Integer>getAdjacentTo(list, 0, 1));
     }
 
     @Test
     public void testListAsSetEquals() {
         assertTrue(CollectionUtils.listAsSetEquals(
                 Lists.newArrayList("foo", "foo", "bar"),
                 Lists.newArrayList("bar", "foo")));
     }
 
     @Test
     public void testNullPaddingLeft() {
         assertEquals(
                 Lists.newArrayList(null, 1, 2),
                 CollectionUtils.<Integer>getNullPaddedAdjacentTo(list, 0, 1));
     }
 
     @Test
     public void testNullPaddingRight() {
         assertEquals(
                 Lists.newArrayList(4, 5, null),
                 CollectionUtils.<Integer>getNullPaddedAdjacentTo(list, 4, 1));
     }
 
     @Test
     public void testNullPaddingRight3() {
         assertEquals(
                 Lists.newArrayList(2, 3, 4, 5, null, null, null),
                 CollectionUtils.<Integer>getNullPaddedAdjacentTo(list, 4, 3));
     }
 
     /**
      * Hack to exclude the private constructor from code coverage metrics.
      */
     @Test
     public void testPrivateConstructor() throws Exception {
         Constructor<?>[] cons = CollectionUtils.class.getDeclaredConstructors();
         cons[0].setAccessible(true);
         cons[0].newInstance((Object[]) null);
     }
 }
