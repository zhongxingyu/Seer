 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.core;
 
 import com.google.common.collect.Sets;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 public class ListNodeTest2 {
 
     private static class TestElement extends ObjectNode {
         @Property String s;
     }
 
     @Test
     public void testAdd() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         TestElement element0 = new TestElement();
         list.add(element0);
         TestElement element1 = new TestElement();
         list.add(new TestElement());
         list.add(1, element1);
 
         assertEquals(Sets.newHashSet("0"), element0.getParentPaths().get(list));
         assertEquals(Sets.newHashSet("1"), element1.getParentPaths().get(list));
         assertEquals(Sets.newHashSet("2"), list.get(2).getParentPaths().get(list));
 
         TestElement elementMultiple = new TestElement();
 
         list.add(elementMultiple);
         list.add(2, elementMultiple);
 
         assertEquals(Sets.newHashSet("2", "4"), elementMultiple.getParentPaths().get(list));
     }
 
     @Test
     public void testAddAll() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         List<TestElement> contents = Arrays.asList(new TestElement(), new TestElement(), new TestElement());
         contents.get(0).set("s", "content 0");
         contents.get(1).set("s", "content 1");
         contents.get(2).set("s", "content 2");
 
         list.add(new TestElement());
         list.add(new TestElement());
 
         list.addAll(1, contents);
 
         assertEquals("content 0", list.get(1).s);
         assertEquals("content 1", list.get(2).s);
         assertEquals("content 2", list.get(3).s);
         assertEquals(Sets.newHashSet("2"), list.get(2).getParentPaths().get(list));
         assertEquals(Sets.newHashSet("4"), list.get(4).getParentPaths().get(list));
     }
 
     @Test
     public void testClear() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         list.add(new TestElement());
         list.add(new TestElement());
         list.add(new TestElement());
         list.add(new TestElement());
 
         TestElement element = list.get(2);
 
         list.clear();
 
         assertNull(element.getParentPaths().get(list));
     }
 
     @Test
     public void testListIterator() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
         list.add(new TestElement());
         list.add(new TestElement());
         list.add(new TestElement());
         list.add(new TestElement());
 
         Iterator<TestElement> it = list.listIterator(2);
         it.next().set("s", "third element");
         it.next().set("s", "fourth element");
         assertFalse(it.hasNext());
 
         it = list.listIterator(1);
         TestElement removedElement = it.next();
         assertNull(removedElement.s);
         it.remove();
         assertNull(removedElement.getParentPaths().get(list));
 
         assertEquals("third element", it.next().s);
 
         it = list.iterator();
         assertNull(it.next().s);
         assertEquals("third element", it.next().s);
         assertEquals("fourth element", it.next().s);
         assertFalse(it.hasNext());
     }
 
     @Test
     public void testRemove() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         TestElement element0 = new TestElement();
         TestElement element1 = new TestElement();
 
         list.add(element0);
         list.add(element1);
 
         assertTrue(list.remove(element0));
         assertNull(element0.getParentPaths().get(list));
 
         list.add(0, element0);
 
         list.remove(1);
         assertNull(element1.getParentPaths().get(list));
     }
 
     @Test
     public void testRemoveAll() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         TestElement aElement = new TestElement();
         TestElement repeated = new TestElement();
         TestElement another = new TestElement();
         TestElement remains = new TestElement();
        TestElement out = new TestElement();
         list.add(aElement);
         list.add(remains);
         list.add(another);
         list.add(repeated);
         list.add(repeated);
         list.add(remains);
 
        list.removeAll(Arrays.asList(aElement, another, repeated, out));
 
         assertNull(aElement.getParentPaths().get(list));
         assertNull(another.getParentPaths().get(list));
         assertNull(repeated.getParentPaths().get(list));
 
         assertEquals(Sets.newHashSet("0", "1"), remains.getParentPaths().get(list));
     }
 
     @Test
     public void testRetainAll() throws Exception {
        ListNode<TestElement> list = new ListNode<>(TestElement.class);

        TestElement remains = new TestElement();
        TestElement repeated = new TestElement();
        TestElement aElement = new TestElement();
        TestElement another = new TestElement();
        TestElement out = new TestElement();
        list.addAll(Arrays.asList(remains, repeated, aElement, repeated, another));

        list.retainAll(Arrays.asList(remains, repeated, out));

        assertNull(aElement.getParentPaths().get(list));
        assertNull(another.getParentPaths().get(list));
 
        assertEquals(Sets.newHashSet("0"), remains.getParentPaths().get(list));
        assertEquals(Sets.newHashSet("1", "2"), repeated.getParentPaths().get(list));
     }
 
     @Test
     public void testSet() throws Exception {
 
     }
 
     @Test
     public void testSetAll() throws Exception {
 
     }
 
     @Test
     public void testGetElementType() throws Exception {
 
     }
 
     @Test
     public void testGetPropertyType() throws Exception {
 
     }
 
     @Test
     public void testGetAll() throws Exception {
 
     }
 
     @Test
     public void testTriggers() throws Exception {
 
     }
 
 }
