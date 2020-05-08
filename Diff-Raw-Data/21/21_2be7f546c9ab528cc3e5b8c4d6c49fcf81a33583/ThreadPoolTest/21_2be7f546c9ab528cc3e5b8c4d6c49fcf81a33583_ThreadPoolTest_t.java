 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.jclarity.advjava.ex8;
 
 import static org.junit.Assert.*;
 
 import com.jclarity.advjava.ex8.QueueReaderTask;
 import com.jclarity.advjava.ex8.ThreadPoolManager;
 import com.jclarity.advjava.ex8.WorkUnit;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author boxcat
  */
 public class ThreadPoolTest {
     
     private ThreadPoolManager mgr = null;

     
     // HINT HINT HINT There may well be bugs in this code! Be careful... 
     @Before
     public void setup() {
    	BlockingQueue<WorkUnit<String>> lbq = new LinkedBlockingQueue<>();
    	mgr = new ThreadPoolManager(lbq, 2);
     }
     
     @Test
    public void testSimpleExecution() throws InterruptedException {

         final AtomicInteger aInt = new AtomicInteger(0);

         final QueueReaderTask msgReader = new QueueReaderTask(100) {
             @Override
             public void doAction(String msg_) {
            	// What should we do here?
             }
         };
         // FIXME TEST GOES HERE
        // How do we get the task into the processing pipeline?

         // What does our test assertion look like??
         // Something to do with the value of aInt after the test has run????
     }
     
     @Test
     public void testScheduling() {
         // FIXME Test 
     }
 
     @Test
     public void testThreadPoolSize() {
         // FIXME May need to change code elsewhere...
     }
 
     
 }
