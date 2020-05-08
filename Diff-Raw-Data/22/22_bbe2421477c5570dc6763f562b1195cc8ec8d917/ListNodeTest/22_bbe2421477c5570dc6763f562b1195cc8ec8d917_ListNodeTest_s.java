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
 import org.mockito.ArgumentCaptor;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class ListNodeTest {
 
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
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         TestElement original = new TestElement();
         TestElement replacement = new TestElement();
 
         list.add(original);
         original.set("s", "Original");
 
         list.set(0, replacement);
 
         assertNull(original.getParentPaths().get(list));
         assertEquals(Sets.newHashSet("0"), replacement.getParentPaths().get(list));
 
         list.set("0.s", "Replacement");
 
         assertEquals("Replacement", replacement.s);
         assertEquals("Original", original.s);
     }
 
     @Test
     public void testSetAll() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         list.add(new TestElement());
         list.add(new TestElement());
         list.add(new TestElement());
 
         list.set("*.s", "Set s for all");
 
         assertEquals("Set s for all", list.get(0).s);
         assertEquals("Set s for all", list.get(1).s);
         assertEquals("Set s for all", list.get(2).s);
     }
 
     @Test
     public void testGetPropertyType() throws Exception {
         ListNode<Node> list = new ListNode<>(Node.class);
 
         list.add(null);
         list.add(new TestElement());
 
         assertEquals(Node.class, list.getPropertyType("0", false));
         assertNull(list.getPropertyType("0", true));
         assertEquals(TestElement.class, list.getPropertyType("1", true));
         assertEquals(Node.class, list.getPropertyType("1", false));
     }
 
     @Test
     public void testGetAll() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         list.add(new TestElement());
         list.add(new TestElement());
         list.get(0).set("s", "first element");
         list.get(1).set("s", "second element");
 
         List<TestElement> getelements = list.get("*");
         assertEquals("first element", getelements.get(0).s);
         assertEquals("second element", getelements.get(1).s);
         assertEquals(list.get(0), getelements.get(0));
 
         List<String> gets = list.get("*.s");
         assertEquals("first element", gets.get(0));
         assertEquals("second element", gets.get(1));
     }
 
     @Test
     public void testTriggers() throws Exception {
         ListNode<TestElement> list = new ListNode<>(TestElement.class);
 
         list.add(new TestElement());
         list.add(new TestElement());
 
         Trigger trigger = mock(Trigger.class);
         when(trigger.getNode()).thenReturn(list);
         when(trigger.getControlledPaths()).thenReturn(Arrays.asList("*.s"));
 
         list.addTrigger(trigger);
 
         assertEquals(Arrays.asList("s"), list.get(0).getControlledProperties());
         assertEquals(Arrays.asList("s"), list.get(1).getControlledProperties());
 
         TestElement removed = list.remove(1);
         assertTrue(removed.getControlledProperties().isEmpty());
 
        list.get(0).set("s", "Trying to activate the trigger");

        verify(trigger, times(1)).setNode(list);

        verifyNoMoreInteractions(trigger);
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(trigger).check(captor.capture());
        assertEquals("0.s", captor.getValue().getPath());
     }
 
 }
