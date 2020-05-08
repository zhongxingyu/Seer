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
 
 import java.util.Arrays;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class AdjacencyIteratorTest {
 
     private List<String> list;
     private AdjacencyIterator<String> it;
 
     @Before
     public void setUp() {
         list = Arrays.asList("foo", "bar", "baz", "brr", "booh");
         it = AdjacencyIterator.create(list.iterator(), 2);
     }
 
     @Test
     public void testNext() {
         assertTrue(it.hasNext());
         assertEquals("foo", it.next());
         it.peekNext();
         it.peekPrevious();
         assertTrue(it.hasNext());
         assertEquals("bar", it.next());
         it.getAdjacent();
         assertTrue(it.hasNext());
         assertEquals("baz", it.next());
         it.peekNext();
         assertTrue(it.hasNext());
         assertEquals("brr", it.next());
         it.peekPrevious();
         assertTrue(it.hasNext());
         assertEquals("booh", it.next());
         assertFalse(it.hasNext());
     }
 
     @Test
     public void testPeekPrevious() {
         assertEquals(Arrays.asList(null, null), it.peekPrevious());
         assertEquals(Arrays.asList(null, null), it.peekPrevious());
         it.next();
         assertEquals(Arrays.asList(null, null), it.peekPrevious());
         it.next();
         assertEquals(Arrays.asList(null, "foo"), it.peekPrevious());
         it.next();
         assertEquals(Arrays.asList("foo", "bar"), it.peekPrevious());
     }
 
     @Test
     public void testPeekNext() {
         assertEquals(Arrays.asList("foo", "bar"), it.peekNext());
         assertEquals(Arrays.asList("foo", "bar"), it.peekNext());
         it.next();
         assertEquals(Arrays.asList("bar", "baz"), it.peekNext());
         it.next();
         assertEquals(Arrays.asList("baz", "brr"), it.peekNext());
         it.next();
         assertEquals(Arrays.asList("brr", "booh"), it.peekNext());
         it.next();
         assertEquals(Arrays.asList("booh", null), it.peekNext());
         it.next();
         assertEquals(Arrays.asList(null, null), it.peekNext());
     }
 
     @Test
     public void testGetCurrent() {
         assertEquals(null, it.getCurrent());
         it.next();
         assertEquals("foo", it.getCurrent());
         it.next();
         assertEquals("bar", it.getCurrent());
         it.peekPrevious();
         it.next();
         assertEquals("baz", it.getCurrent());
         it.peekNext();
         it.next();
         assertEquals("brr", it.getCurrent());
         it.next();
         assertEquals("booh", it.getCurrent());
     }
 
     @Test
     public void testAdjacent() {
         assertEquals(Arrays.asList(null, null, null, "foo", "bar"), it.getAdjacent());
         it.next();
         assertEquals(Arrays.asList(null, null, "foo", "bar", "baz"), it.getAdjacent());
         it.next();
         assertEquals(Arrays.asList(null, "foo", "bar", "baz", "brr"), it.getAdjacent());
         it.next();
         assertEquals(Arrays.asList("foo", "bar", "baz", "brr", "booh"), it.getAdjacent());
         it.next();
         assertEquals(Arrays.asList("bar", "baz", "brr", "booh", null), it.getAdjacent());
         it.next();
         assertEquals(Arrays.asList("baz", "brr", "booh", null, null), it.getAdjacent());
         it.next();
         assertFalse(it.hasNext());
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testArguments() {
        new AdjacencyIterator(Arrays.asList("foo", "bar").iterator(), 0);
     }
 }
