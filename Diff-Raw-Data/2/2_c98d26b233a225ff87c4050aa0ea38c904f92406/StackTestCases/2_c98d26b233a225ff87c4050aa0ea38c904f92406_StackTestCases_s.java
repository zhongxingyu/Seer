 package org.hackmonkey.learning.java.algods;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class StackTestCases {
 	
 	protected IStack<Object> stack;
 	
 	@Before
 	public void setup() {
 		stack = null;
 		stack = new StackWithTwoQueues<Object>();
 		assertNotNull(stack);
 	}
 
 	@Test
 	public void testPush() {
 		Object in = new Object();
 		stack.push(in);
		assertEquals(stack.getItemCount(), 1);
 	}
 	
 	@Test
 	public void testPushPop() {
 		Object in = new Object();
 		stack.push(in);
 		Object out = stack.pop();
 		assertEquals(in, out);
 	}
 	
 	@Test
 	public void testUnderflow(){
 		Object out = stack.pop();
 		assertNull(out);
 	}
 	
 	@Test
 	public void testOverflow(){
 		int initialCapacity = stack.getCurrentCapacity();
 		for(int i=0;i<initialCapacity;i++){
 			stack.push(new Object());
 		}
 		stack.push(new Object());
 		assertEquals(initialCapacity+1,stack.getItemCount());
 		assertTrue((stack.getCurrentCapacity()>initialCapacity));
 	}
 
 }
