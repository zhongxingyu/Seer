 package ru.yandex.shad.belova.java.problem1;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertArrayEquals;
 
 import java.util.NoSuchElementException;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(value = JUnit4.class)
 public class TestMyLinkedList {
     private MyLinkedList list;
 
     @Before
     public void setUp(){
         list = new MyLinkedList();
     }
     
     @Rule
     public ExpectedException exception = ExpectedException.none();
     
     @Test
     public void testAddFirst(){
         list.addFirst(1);
         assertArrayEquals("Fail adding to empty list", new Object[]{1}, list.toArray());
         list.addFirst(4);
         assertArrayEquals("Fail adding to list", new Object[]{4,1}, list.toArray());
     }
     
     @Test
     public void testAddLast(){
         list.addLast(1);
         assertArrayEquals("Fail adding to empty list", new Object[]{1}, list.toArray());
         list.addLast(4);
         assertArrayEquals("Fail adding to list", new Object[]{1,4}, list.toArray());
     }
     
     @Test
     public void testGetFirst(){
         list.add(4);
         assertEquals("Fail on list with one element", 4, list.getFirst());
         list.addAll(new Object[]{1,2,3});
         assertEquals("Fail on full list", 4, list.getFirst());
     }
     
     @Test
     public void testGetFirstException(){
         exception.expect(NoSuchElementException.class);
         list.getFirst();
     }
     
     @Test
     public void testGetLast(){
         list.add(4);
         assertEquals("Fail on list with one element", 4, list.getLast());
         list.addAll(new Object[]{1,2,3});
         assertEquals("Fail on full list", 3, list.getLast());
     }
     
     @Test
     public void testGetLastException(){
         exception.expect(NoSuchElementException.class);
         list.getLast();
     }
     
     @Test
     public void testRemoveFirst(){
         list.addAll(new Object[]{1});
         int first = (Integer)list.removeFirst();
         assertEquals("Fail element returned on list with one element", 1, first);
         assertArrayEquals("Fail removing on list with one element", new Object[]{}, list.toArray());
         list.addAll(new Object[]{5,6,7,8});
         first = (Integer)list.removeFirst();
         assertEquals("Fail element returned on list", 5, first);
         assertArrayEquals("Fail removing on list", new Object[]{6,7,8}, list.toArray());
     }
     
     @Test
     public void testRemoveFirstException(){
         exception.expect(NoSuchElementException.class);
         list.removeFirst();
     }
     
     @Test
     public void testRemoveLast(){
        list.add(new Object[]{1});
         Object last = list.removeLast();
         assertEquals("Fail element returned on list with one element", 1, last);
         assertArrayEquals("Fail removing on list with one element", new Object[]{}, list.toArray());
         list.addAll(new Object[]{5,6,7,8});
         last = list.removeLast();
         assertEquals("Fail element returned on list", 8, last);
         assertArrayEquals("Fail removing on list", new Object[]{5,6,7}, list.toArray());
     }
     
     @Test
     public void testRemoveLastException(){
         exception.expect(NoSuchElementException.class);
         list.removeLast();
     }
 }
