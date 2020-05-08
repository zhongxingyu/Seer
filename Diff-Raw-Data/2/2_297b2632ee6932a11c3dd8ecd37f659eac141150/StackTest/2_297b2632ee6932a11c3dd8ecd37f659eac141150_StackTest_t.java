 package com.colabug.stack;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static junit.framework.Assert.*;
 
 public class StackTest
 {
     private Stack<Integer> stack;
     private static Integer nodeData = 5;
     private static Integer nodeData2 = 6;
     private static Integer nodeData3 = 7;
 
     @Before
     public void setUp()
     {
         stack = new Stack<Integer>();
     }
 
     @Test
     public void testEmptyStack()
     {
         assertTrue( stack.isEmpty() );
     }
 
     @Test
     public void testPush()
     {
         createArbitraryStack();
         assertFalse( stack.isEmpty() );
         assertEquals( nodeData, stack.top() );
     }
 
     private void createArbitraryStack()
     {
         stack.push( nodeData );
         stack.push( nodeData2 );
         stack.push( nodeData3 );
         stack.push( nodeData );
     }
 
     @Test
     public void testPopEmptyStack()
     {
        assertNull( stack.pop() );
     }
 
     @Test
     public void testPop()
     {
         // Populate stack
         createArbitraryStack();
 
         // Check top element
         Integer data = stack.pop();
         assertEquals( nodeData, data );
     }
 }
