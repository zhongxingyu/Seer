 package src.test;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.Assert;
 import java.lang.Exception;
 
 import src.main.Queue;
 
 public class QueueTest {
     Queue<Integer> a;
     
     /**
      * Sets up the test fixture. 
      * (Called before every test case method.)
      */
     @Before
     public void setUp() {
         a = new Queue<Integer>();
     }
 
     /**
      * Tears down the test fixture. 
      * (Called after every test case method.)
      */
     @After
     public void tearDown() {
         
     }
     
     @Test
     public void testEnqueue() {
         a.enqueue(1);
        assertEquals(1, a.dequeue());
     }
 
     @Test
     public void testDequeue(){
        a.enqueue(1);
        a.enqueue(2);
        a.enqueue(3);
       assertEquals(1, a.dequeue());
       assertEquals(2, a.dequeue());
       assertEquals(3, a.dequeue());
        assertNull("Nothing in the queue", a.dequeue());
     }
 
     @Test
     public void testIsEmpty(){
         assertTrue(a.isEmpty());
         a.enqueue(1);
         assertFalse(a.isEmpty());
         a.dequeue();
         assertTrue(a.isEmpty());
     }
 
     public static void main(String args[]) {
         org.junit.runner.JUnitCore.main("src.test.ElementTest");
     }
 
 }
