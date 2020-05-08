 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.helsinki.coderodde.util;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * This class tests <code>TreeList2</code>.
  */
 public class MyTreeListTest {
 
     /**
      * Test of size method, of class MyTreeList.
      */
     @Test
     public void testSize() {
         MyTreeList<String> list = new MyTreeList<String>(2);
         list.add("Hello");
         list.add("World");
         assertEquals(2, list.size());
     }
 
     /**
      * Test of isEmpty method, of class MyTreeList.
      */
     @Test
     public void testIsEmpty() {
         MyTreeList<Integer> list = new MyTreeList<Integer>(2);
 
         assertTrue(list.isEmpty());
         assertTrue(list.isHealthy());
 
         list.add(1);
 
         assertFalse(list.isEmpty());
         assertTrue(list.isHealthy());
     }
 
     /**
      * Test of contains method, of class MyTreeList.
      */
     @Test
     public void testContains() {
         String yo = "Hello, y'all!";
         String sayonara = "Gomen nasai, demoo sayonara, ahondara! ^^";
         MyTreeList<String> list = new MyTreeList<String>(1);
         list.add(yo);
         assertTrue(list.contains(yo));
         assertFalse(list.contains(sayonara));
     }
 
     /**
      * Test of iterator method, of class MyTreeList.
      */
     @Test
     public void testIterator() {
     }
 
     /**
      * Test of toArray method, of class MyTreeList.
      */
     @Test
     public void testToArray_0args() {
     }
 
     /**
      * Test of toArray method, of class MyTreeList.
      */
     @Test
     public void testToArray_GenericType() {
     }
 
     /**
      * Test of add method, of class MyTreeList.
      */
     @Test
     public void testAdd_GenericType() {
     }
 
     /**
      * Test of remove method, of class MyTreeList.
      */
     @Test
     public void testRemove_Object() {
     }
 
     /**
      * Test of containsAll method, of class MyTreeList.
      */
     @Test
     public void testContainsAll() {
     }
 
     /**
      * Test of addAll method, of class MyTreeList.
      */
     @Test
     public void testAddAll_Collection() {
     }
 
     /**
      * Test of addAll method, of class MyTreeList.
      */
     @Test
     public void testAddAll_int_Collection() {
     }
 
     /**
      * Test of removeAll method, of class MyTreeList.
      */
     @Test
     public void testRemoveAll() {
     }
 
     /**
      * Test of retainAll method, of class MyTreeList.
      */
     @Test
     public void testRetainAll() {
     }
 
     /**
      * Test of clear method, of class MyTreeList.
      */
     @Test
     public void testClear() {
         MyTreeList<Integer> list = new MyTreeList<Integer>(1);
         for (int i = 0; i < 10; i++) {
             list.add(i);
         }
         assertEquals(10, list.size());
         list.clear();
         assertEquals(0, list.size());
         assertTrue(list.isEmpty());
     }
 
     /**
      * Test of get method, of class MyTreeList.
      */
     @Test
     public void testGet() {
         MyTreeList<Integer> list = new MyTreeList<Integer>(2);
         for (int i = 0; i < 10; i++) {
             list.add(i);
         }
 
         for (int i = 0; i < 10; i++) {
             assertEquals(i, (int) list.get(i));
         }
 
         MyTreeList<Integer> list2 = new MyTreeList<Integer>(1);
         for (int i = 0; i < 10; i++) {
             list.add(i);
         }
 
         for (int i = 0; i < 10; i++) {
             assertEquals(i, (int) list.get(i));
         }
     }
 
     /**
      * Test of set method, of class MyTreeList.
      */
     @Test
     public void testSet() {
     }
 
     /**
      * Test of add method, of class MyTreeList.
      */
     @Test
     public void testAdd_int_GenericType() {
     }
 
     /**
      * Test of remove method, of class MyTreeList.
      */
     @Test
     public void testRemove_int() {
     }
 
     /**
      * Test of lastIndexOf method, of class MyTreeList.
      */
     @Test
     public void testLastIndexOf() {
     }
 
     /**
      * Test of listIterator method, of class MyTreeList.
      */
     @Test
     public void testListIterator_0args() {
     }
 
     /**
      * Test of listIterator method, of class MyTreeList.
      */
     @Test
     public void testListIterator_int() {
     }
 
     /**
      * Test of subList method, of class MyTreeList.
      */
     @Test
     public void testSubList() {
     }
 
     /**
      * Test of indexOf method, of class MyTreeList.
      */
     @Test
     public void testIndexOf() {
 
     }
 }
