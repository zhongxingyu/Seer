 package com.zsoltfabok.kata.stack;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.lang.reflect.Field;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class StackTest {
 
     private Stack stack;
 
     @Before
     public void setUp() {
         stack = new Stack();
     }
 
     @Test
     public void shouldPopTheLastPushedElement() {
         stack.push(3);
         assertEquals(3, stack.pop());
     }
 
     @Test
     public void shouldPopStackedElementsInReverseOrder() {
         stack.push(1);
         stack.push(2);
         stack.push(3);
         assertEquals(3, stack.pop());
         assertEquals(2, stack.pop());
         assertEquals(1, stack.pop());
     }
 
     @Test
     public void shouldReturnTheCurrentSizeOfTheStack() {
         for (int i = 0; i < 3; i++) {
             stack.push(i);
         }
         assertEquals(3, stack.size());
     }
 
     @Test
     public void shouldHaveAllTheElementsAfterEnlargingInternalCapacity() {
         for (int i = 0; i < 11; i++) {
             stack.push(i);
         }
 
         for (int i = 10; i >= 0; i--) {
             assertEquals(i, stack.pop());
         }
     }
 
     @Test
     public void shouldHaveAllTheElementsAfterShrinkingInternalCapacity() {
         for (int i = 0; i < 21; i++) {
             stack.push(i);
         }
 
         for (int i = 20; i >= 5; i--) {
             assertEquals(i, stack.pop());
         }
     }
 
     @Test
     public void shouldShrinkingInternalCapacity() {
         for (int i = 0; i < 21; i++) {
             stack.push(i);
         }
 
         for (int i = 20; i >= 5; i--) {
             stack.pop();
         }
 
         assertEquals(10, getContainerArrayLength());
     }
 
     private int getContainerArrayLength() {
         try {
             Class<?> clazz = stack.getClass();
             Field field = clazz.getDeclaredField("container");
             field.setAccessible(true);
             int[] container = (int[]) field.get(stack);
             return container.length;
         } catch (Exception e) {
             fail();
         }
         return -1;
     }
 }
