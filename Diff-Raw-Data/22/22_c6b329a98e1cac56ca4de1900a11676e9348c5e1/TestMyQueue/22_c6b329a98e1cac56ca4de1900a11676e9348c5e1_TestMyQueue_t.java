 package ru.yandex.shad.belova.java.problem1;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 import java.util.NoSuchElementException;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(value = JUnit4.class)
 public class TestMyQueue {
     private MyQueue queue;
 
     @Before
     public void setUp(){
         queue = new MyQueue();
     }
     
     @Rule
     public ExpectedException exception = ExpectedException.none();
     
     @Test
     public void testOffer(){
         queue.offer(2);
         assertArrayEquals("Fail offering in empty queue", new Object[]{2}, queue.toArray());
         queue.offer(4);
         assertArrayEquals("Fail offering in queue", new Object[]{2,4}, queue.toArray());
     }
     
     @Test
     public void testPeek(){
         queue.offer(3);
         queue.offer(10);
         queue.offer(145);
         queue.offer(-1);
         assertEquals("Wrong element peeked", 3, queue.peek());
         assertArrayEquals("Peek have changed array", new Object[]{3,10,145,-1}, queue.toArray());
     }
     
     @Test
     public void testPeekException(){
         exception.expect(NoSuchElementException.class);
         queue.peek();
     }
     
     @Test
     public void testPoll(){
         queue.offer(3);
         queue.offer(10);
         queue.offer(145);
         queue.offer(-1);
        assertEquals("Wrong element polled", 3, queue.poll());
         assertArrayEquals("Peek have changed array in wrong way", new Object[]{10,145,-1}, queue.toArray());
     }
     
     @Test
     public void testPollException(){
         exception.expect(NoSuchElementException.class);
         queue.poll();
     }
     
 }
 
