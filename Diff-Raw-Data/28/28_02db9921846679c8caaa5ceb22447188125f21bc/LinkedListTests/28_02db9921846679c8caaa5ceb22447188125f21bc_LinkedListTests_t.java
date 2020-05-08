 package com.jeromegagner.example.linkedlist;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: jgagner
  * Date: Apr 18, 2010
  * Time: 9:54:16 AM
  * To change this template use File | Settings | File Templates.
  */
 public class LinkedListTests {
 
     @Test
     public void findTest() {
         LinkedList list = LinkedList.createList(10);
         assertEquals(list.find(5).data, 5);
     }
 
     @Test
     public void findTestWithNonExistentElement() {
         LinkedList list = LinkedList.createList(10);
         assertNull(list.find(11));
     }
 
     @Test
     public void sizeTest() {
         LinkedList list = LinkedList.createList(100);
         assertEquals(100, list.length());
     }
 
 
     @Test
     public void findTailTest() {
         LinkedList list = LinkedList.createList(100);
         assertEquals(99, list.findTail().data);
 
         //Test to make sure that tail is properly set on construction
         /** memory leak?? **/
         list = LinkedList.createList(50);
         assertEquals(49, list.tail.data);
     }
 
     @Test
     public void removeHeadTest() {
         LinkedList list = LinkedList.createList(10);
         Element expectedNewHead = list.head.next;
         assertTrue(list.remove(list.head));
         assertEquals(expectedNewHead, list.head);
         assertEquals(9, list.length());
     }
 
     @Test
     public void removeTailTest() {
         LinkedList list = LinkedList.createList(10);
         Element expectedTail = list.find(8);
         assertTrue(list.remove(list.tail));
         assertEquals(expectedTail, list.tail);
     }
 
     @Test
     public void removeTailNonexisting() {
         LinkedList list = LinkedList.createList(10);
         Element notThere = new Element(0);
         assertFalse(list.remove(notThere));
         assertEquals(10, list.length());
     }
 
     @Test
     public void insertAfterElementNotFoundTest(){
      LinkedList list = LinkedList.createList(10);
         Element notThere = new Element(0);
         assertFalse(list.insertAfter(notThere,11));
         assertEquals(10, list.length());
     }
 
     @Test
     public void insertAfterWithNull(){
         LinkedList list = LinkedList.createList(10);
         assertTrue(list.insertAfter(null,-1));
         assertEquals(-1,list.head.data);
         assertEquals(11,list.length());
     }
 
     @Test
     public void testInMiddle(){
       LinkedList list = LinkedList.createList(10);
         assertTrue(list.insertAfter(list.head,-1));
         assertEquals(-1,list.head.next.data);
         assertEquals(11,list.length());
     }
 
     @Test
     public void testInsertNewTail()
     {
         LinkedList list = LinkedList.createList(10);
         assertTrue(list.insertAfter(list.tail,-1));
         assertEquals(-1,list.tail.data);
         assertEquals(11,list.length());
     }

    @Test
    public void testInsertEmptyList()
    {
        LinkedList list = new LinkedList(null);
        assertTrue(list.insertAfter(null,1));
        assertEquals(list.head,list.tail);
        assertEquals(1,list.tail.data);
        assertEquals(1,list.head.data);

    }
 }
