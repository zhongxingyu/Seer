 package ru.yandex.shad.belova.java.problem1;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(value = JUnit4.class)
 public class TestMyCollection {
 
     
     @Rule
     public ExpectedException exception = ExpectedException.none();
     
     @Test
     public void testCopy(){
         MyLinkedList a = new MyLinkedList();
         a.addAll(new Object[]{1,2,3,4});
         MyLinkedList b = new MyLinkedList();
         MyCollection.copy(b, a);
         assertArrayEquals("Copied arrays are not equal", a.toArray(), b.toArray());
 
         b = new MyLinkedList();
         a.add(3);
         MyCollection.copy(b, a);
         assertArrayEquals("There are have being copied only links", a.toArray(), b.toArray());
 
         b = new MyLinkedList();
         b.add(2);
         MyCollection.copy(b, a);
         a.addFirst(2);
         assertArrayEquals("Copying failed if there are elements in second collection", a.toArray(), b.toArray());
         
         a = new MyLinkedList();
         MyCollection.copy(b, a);
         assertArrayEquals("Copying failed if there are no elements in first collection", b.toArray(), b.toArray());
     }
     
     @Test
     public void testReverse(){
         MyLinkedList a = new MyLinkedList();
         a.addAll(new Object[]{1,2,3,4,10,-5,3});
         MyLinkedList b = new MyLinkedList();
         b.addAll(new Object[]{3,-5,10,4,3,2,1});
         MyCollection.reverse(a);
         assertArrayEquals("Failed on reverse", b.toArray(), a.toArray());
         
         a = new MyLinkedList();
         b = new MyLinkedList();
         MyCollection.reverse(a);
         assertArrayEquals("Failed if list is empty", b.toArray(), a.toArray());
         
         a = new MyLinkedList();
         a.add(2);
         b = new MyLinkedList();
         b.add(2);
         MyCollection.reverse(a);
         assertArrayEquals("Failed if list has one element", b.toArray(), a.toArray());
         }
     
     @Test
     public void testBinarySearch(){
         MyArrayList a = new MyArrayList();
         int index = MyCollection.binarySearch(a, 3);
         assertEquals("Failed if list is empty", -1, index);
         
         a.add(3);
         
         index = MyCollection.binarySearch(a, 3);
         assertEquals("Failed if list has one element equals to key", 0, index);
         
         index = MyCollection.binarySearch(a, 2);
         assertEquals("Failed if list has one element greater then key", -1, index);
         
         index = MyCollection.binarySearch(a, 4);
         assertEquals("Failed if list has one element less then key", -2, index);
         
         a.add(5);
         
         index = MyCollection.binarySearch(a, 3);
         assertEquals("Failed if list has two elements and first equals key", 0, index);
         
         index = MyCollection.binarySearch(a, 5);
         assertEquals("Failed if list has two elements and second equals key", 1, index);
         
         index = MyCollection.binarySearch(a, 4);
         assertEquals("Failed if list has two elements and key between them", -2, index);
         
         index = MyCollection.binarySearch(a, 6);
         assertEquals("Failed if list has two elements greater then both", -3, index);
         
         index = MyCollection.binarySearch(a, 2);
         assertEquals("Failed if list has two elements less then both", -1, index);
         
         a = new MyArrayList();
         a.addAll(new Object[]{1,2,3,4,5,6,7,8,9,10,11,12});
         index = MyCollection.binarySearch(a, 3);
         assertEquals("Failed on " + a.toString(), 2, index);
         
         a = new MyArrayList();
         a.addAll(new Object[]{1,2,3,8,10,15,20,1050});
         index = MyCollection.binarySearch(a, 4);
         assertEquals("Failed on " + a.toString(), -4, index);
         
         a = new MyArrayList();
         a.addAll(new Object[]{-50, -43, -4, 0, 1,2,3,8,10,15,20,1050});
         index = MyCollection.binarySearch(a, -40);
         assertEquals("Failed on " + a.toString(), -3, index);
         }
 }
     
