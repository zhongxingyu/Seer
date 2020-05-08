 package models.data;
 
 import java.util.Calendar;
 import java.lang.Runnable;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Assert;
 
 import models.data.DataDaemon;
 
 /**
  * Tests the DataDaemon class.
  * @author Felix Van der Jeugt
  */
 public class DataDaemonTest {
 
     // How many tasks should run.
     public static final int TASKS = 20;
 
     // The daemon, shortcut variable.
     private DataDaemon daemon = DataDaemon.getInstance();
 
     /** Before anf after any test, the queue should be empty. */
     @Before @After public void testEmpty() {
         Assert.assertTrue(daemon.empty());
     }
 
     /** Tests if all added tasks are run. */
     @Test public void testPersistence() {
         final Counter counter = new Counter(TASKS);
         final Bool bool = new Bool();
         Runnable task = new Runnable() {
             public void run() { if(counter.down()) bool.set(true); }
         };
         try {
             for(int i = 0; i < TASKS; i++) {
                 daemon.runAt(task, Calendar.getInstance());
             }
             Thread.sleep(1000);
         } catch(InterruptedException e) {
             Assert.fail(e.getMessage());
         }
         Assert.assertTrue("Not all tasks run within a second.", bool.get());
     }
 
     private static class Counter {
         private int count;
         public Counter(int count) { this.count = count; }
         public synchronized boolean down() { return (--count == 0); }
     }
 
     private static class Bool {
         private boolean bool = false;
         public synchronized void set(boolean bool) { this.bool = bool; }
         public synchronized boolean get() { return bool; }
     }
 
     /** Test if the tasks are run at the set time. */
     @Test public void testPrecision() {
        // TODO
     }
 
 }
