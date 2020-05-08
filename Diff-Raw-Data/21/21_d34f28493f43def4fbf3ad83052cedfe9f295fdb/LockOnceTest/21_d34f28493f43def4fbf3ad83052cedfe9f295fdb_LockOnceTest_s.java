 package ch.claude_martin.lockonce;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class LockOnceTest {
   // any good code holds this constant:
   static final int THE_ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING = 42;
 
   // This does not even need to be volatile. Changes are visible on access
   // thanks to JMM.
   // Can't begin at 0, because that would just be the default value.
   static int check = THE_ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING;
 
   // a singleton is still be recommended to declared this as volatile!
   static volatile PseudoSingleton singleton = null;
 
   @SuppressWarnings("static-method")
   @Test
   public void loops() throws Throwable {
     final int loops = 20;
     for (int i = 0; i < loops; i++) {
       final int expected = check + 1;
       final LockOnce lo = new LockOnce();
       final int threads = 20;
       final ExecutorService threadPool = Executors.newFixedThreadPool(threads);
       final AtomicReference<Throwable> exceptionThrown = new AtomicReference<>(
           null);
       final AtomicBoolean singletonIsPoppulated = new AtomicBoolean(false);
       // runnable1 doesn't really test the code but the JVM, which usually is
       // not the idea of JUnit tests.
       // But this is just to show how this really is thread safe.
       final Runnable runnable1 = new Runnable() {
         @Override
         public void run() {
           try {
             for (int j = 0; singleton == null || !singletonIsPoppulated.get()
                 && j++ < 100;)
               Thread.sleep(100);
             // access to the fake "singleton" without using LockOnce:
             Assert.assertTrue("runnable1: Wrong value (foo1)",
                 singleton.foo1 != 0);
             Assert.assertTrue("runnable1: Wrong value (foo2)",
                 singleton.foo2 != 0);
             Assert.assertTrue("runnable1: Wrong value (foo3)",
                 singleton.getFoo3() != 0);
           } catch (final Throwable e) {
             exceptionThrown.set(e);
           }
         }
       };
       // runnable2 actually tests lockOnce():
       final Runnable runnable2 = new Runnable() {
         @Override
         public void run() {
           try {
             for (int j = 0; j < 10; j++)
               lo.toString(); // Just to keep the Threads busy.
             if (lo.lockOnce())
               try {
                 final int copy = check;
                 singleton = new PseudoSingleton(check + 1);
                 Thread.sleep(50);
                 singleton.foo2 = check + 1;
                 Thread.sleep(50);
                 singleton.setFoo3(check + 1);
                 singletonIsPoppulated.set(true);
                 check = copy + 1;
               } finally {
                 lo.unlock();
               }
             // value must be correct in any case:
             Assert.assertEquals("runnable2: Wrong value (check)", expected,
                 check);
             Assert.assertEquals("runnable2: Wrong value (foo1)", expected,
                 singleton.foo1);
             Assert.assertEquals("runnable2: Wrong value (foo2)", expected,
                 singleton.foo2);
             Assert.assertEquals("runnable2: Wrong value (foo3)", expected,
                 singleton.getFoo3());
           } catch (final Throwable e) {
             exceptionThrown.set(e);
           }
         }
       };
 
       threadPool.submit(runnable1);
       threadPool.submit(runnable1);
       for (int j = 0; j < threads; j++)
         threadPool.submit(runnable2);
       threadPool.shutdown();
       threadPool.awaitTermination(10, TimeUnit.SECONDS);
       if (exceptionThrown.get() != null)
         throw exceptionThrown.get();
       Assert.assertEquals("Wrong value", expected, check);
       singleton = null;
     }
 
     Assert.assertEquals("Wrong total amount", loops
         + THE_ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING, check);
   }
 
   @SuppressWarnings("static-method")
   @Test
   public void multipleCalls() {
     try {
       final LockOnce lo = new LockOnce();
       Assert.assertTrue(lo.lockOnce()); // first one must be ok.
       lo.lockOnce(); // second one makes no sense! it should fail quickly.
       Assert
       .fail("Second call to LockOnce.lockOnce() did not throw any Exception :-(");
     } catch (final IllegalMonitorStateException re) {
       // ok!
     }
   }
 
   @SuppressWarnings("static-method")
   @Test
   public void unlock() throws InterruptedException {
     try {
       final LockOnce lo = new LockOnce();
       lo.unlock();
       Assert.fail("LockOnce.unlock() did not throw any Exception :-(");
     } catch (final IllegalMonitorStateException re) {
       // ok!
     }
 
     {
       final LockOnce lo = new LockOnce();
       final AtomicBoolean check2 = new AtomicBoolean(false);
       final Thread thread = new Thread() {
         @Override
         public void run() {
           try {
             lo.unlock();
             Assert.fail("LockOnce.unlock() did not throw any Exception :-(");
           } catch (final IllegalMonitorStateException re) {
             // ok!
             check2.set(true);
           }
         }
       };
       thread.start();
       thread.join();
       Assert.assertTrue(check2.get());
     }
   }
 
   static final class PseudoSingleton {
     // All is volatile, final or synchronized. Visibility will be guaranteed by
     // JVM.
     final int foo1;
     volatile int foo2;
     private/* synchronized */int foo3;
 
     private static volatile PseudoSingleton instance = null;
 
     /** This is just here to give an example on how to use this correctly! */
     public static PseudoSingleton getInstance() {
      final LockOnce lo = new LockOnce();
      if (lo.lockOnce())
         try {
          instance = new PseudoSingleton(
              THE_ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING);
         } finally {
          lo.unlock();
         }
       return instance;
     }
 
     /** This is just here to show how its done with a double checked lock! */
     public static PseudoSingleton getInstanceDCL() {
       if (instance == null)
         synchronized (PseudoSingleton.class) {
           if (instance == null)
            instance = new PseudoSingleton(
                THE_ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING);
         }
       return instance;
     }
 
     private static final class InstanceHolder {
      public static PseudoSingleton instanceHolder = new PseudoSingleton(
          THE_ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING);
     }
 
     /** This is just here to show how its done with a nested class as a holder! */
     public static PseudoSingleton getInstanceHolder() {
       return InstanceHolder.instanceHolder;
     }
 
     public PseudoSingleton(final int value) {
       this.foo1 = value;
     }
 
     public synchronized int getFoo3() {
       return this.foo3;
     }
 
     public synchronized void setFoo3(final int foo3) {
       this.foo3 = foo3;
     }
   }
 }
