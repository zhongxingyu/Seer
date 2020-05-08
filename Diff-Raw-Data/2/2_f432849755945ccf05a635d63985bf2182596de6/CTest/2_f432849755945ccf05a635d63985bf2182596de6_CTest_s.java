 package concurrency;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import java.util.*;
 import java.util.concurrent.*;
 import java.lang.*;
 import java.util.concurrent.atomic.AtomicInteger;
 /**
  * Tests for {@link Foo}.
  *
  * @author user@example.com (Jung Gyu Park)
  */
 interface Runnable1 {
     public void run(String s);
     public void emit(Vector<Map<String, Integer> > vm);
     public void clear();
 }
 
 class CheckIp implements Runnable1 {
     CheckIp() {
 	m = new HashMap();
     }
 	    
     public void run(String s) {
 	Integer count = m.get(s);
 	m.put(s, (count == null) ? 1 : count + 1);
     }
 
     public void emit(Vector<Map<String, Integer> > vm) {
 	vm.add(m);
     }
 
     public void clear() {
 	m.clear();
     }
     
     Map<String, Integer> m;
 }
 
 class UnsafeIntData
 {
     UnsafeIntData()
     {
 	count = 0;
     }
 
     void inc() {
 	for (int i = 0; i < 100; ++i)
 	    count++;
     }
 
     int count;
 }
 
 class AtomicIntData
 {
     AtomicIntData()
     {
 	count = new AtomicInteger(0);
     }
 
     void inc() {
 	for (int i = 0; i < 100; ++i)
 	    count.incrementAndGet();
     }
 
     AtomicInteger count;
 }
 
 class SafeIntData
 {
     SafeIntData()
     {
 	count = 0;
     }
 
     synchronized void inc() {
 	for (int i = 0; i < 100; ++i)
 	    count++;
     }
 
     int count;
 }
 
 
 public class CTest {
 
     @Ignore
     public void test1() {
 	final UnsafeIntData count = new UnsafeIntData();
 	
 	List<Runnable> r = new ArrayList();
 	for (int i = 0; i < 1000; ++i) {
 	    r.add(new Runnable() {
 		    public void run() {
 			count.inc();
 		    }
 		});
 	}
 	try {
 	    assertConcurrent("failed to run concurrently", r, 5);
 	} catch (final Throwable e) {
 	    System.out.println("Error failed" + e);
 	}
 	assertEquals("count should be 100000", 100000, count.count);
     }
 
     @Test
      public void test2() throws InterruptedException {
 	final SafeIntData count = new SafeIntData();
 	
 	List<Runnable> r = new ArrayList();
 	for (int i = 0; i < 1000; ++i) {
 	    r.add(new Runnable() {
 		    public void run() {
 			count.inc();
 		    }
 		});
 	}
 	assertConcurrent("failed to run concurrently", r, 5);
 	assertEquals("count should be 100000", 100000, count.count);
     }
 
    @Test
    public void test3() {
 	final AtomicIntData count = new AtomicIntData();
 	
 	List<Runnable> r = new ArrayList();
 	for (int i = 0; i < 1000; ++i) {
 	    r.add(new Runnable() {
 		    public void run() {
 			count.inc();
 		    }
 		});
 	}
 	try {
 	    assertConcurrent("failed to run concurrently", r, 5);
 	} catch (final Throwable e) {
 	    System.out.println("Error failed" + e);
 	}
 	assertEquals("count should be 100000", 100000, count.count.get());
     }
 
    @Test
    public void test4() {
 	List<Runnable1> r = new ArrayList();
 	for (int i = 0; i < 1000; ++i) {
 	    r.add(new CheckIp());
 	}
 	
 	Vector<Map<String, Integer> > vm = new Vector<Map<String, Integer> >();
 
 	List<String> ls = new ArrayList();
 	String[] s = {"163.152", "123.456", "192.111"};
 	int[] ai = new int[3];
 	// populate ls (input data) here
 	Random ran = new Random();
 	int size = s.length;
 	for (int i = 0; i < 10000; ++i) {
 	    int index = ran.nextInt(size);
 	    ls.add(s[index]);
 	    ai[index]++;
 	}
	assertEquals("total number of items shouls be same", size, ai[0] + ai[1] + ai[2]);
 
 	try {
 	    assertConcurrent2("failed to run concurrently", r, 5, ls, vm);
 	} catch (final Throwable e) {
 	    System.out.println("Error failed" + e);
 	}
     }
 
     public static void assertConcurrent(final String message,
 		final List<? extends Runnable> runnables,
 		final int maxTimeoutSeconds) throws InterruptedException {
 	final int numThreads = runnables.size();
 	final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
 	final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
 	try {
             final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
             final CountDownLatch afterInitBlocker = new CountDownLatch(1);
             final CountDownLatch allDone = new CountDownLatch(numThreads);
             for (final Runnable submittedTestRunnable : runnables) {
 		threadPool.submit(new Runnable() {
 			public void run() {
 			    allExecutorThreadsReady.countDown();
 			    try {
 				afterInitBlocker.await();
 				submittedTestRunnable.run();
 			    } catch (final Throwable e) {
 				exceptions.add(e);
 			    } finally {
 				allDone.countDown();
 			    }
 			}
 		    });
             }
             // wait until all threads are ready
             assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent",
 		       allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
             // start all test runners
             afterInitBlocker.countDown();
             assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
 	} finally {
             threadPool.shutdownNow();
 	}
 	assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
     }
     
     public static void assertConcurrent2(final String message,
 					 final List<? extends Runnable1> runnables,
 					 final int maxTimeoutSeconds,
 					 final List<String> data,
 					 final Vector<Map<String, Integer> > output) throws InterruptedException {
 	final int numThreads = runnables.size();
 	final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
 	final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
 	try {
             final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
             final CountDownLatch afterInitBlocker = new CountDownLatch(1);
             final CountDownLatch allDone = new CountDownLatch(numThreads);
 	    final AtomicInteger index = new AtomicInteger(0);
             for (final Runnable1 submittedTestRunnable : runnables) {
 		threadPool.submit(new Runnable() {
 			public void run() {
 			    allExecutorThreadsReady.countDown();
 			    try {
 				afterInitBlocker.await();
 				int i = index.incrementAndGet();
 				while (i < data.size()) {
 				    submittedTestRunnable.run(data.get(i));
 				    i = index.incrementAndGet();
 				}
 				submittedTestRunnable.emit(output);
 				submittedTestRunnable.clear();
 			    } catch (final Throwable e) {
 				exceptions.add(e);
 			    } finally {
 				allDone.countDown();
 			    }
 			}
 		    });
             }
             // wait until all threads are ready
             assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent",
 		       allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
             // start all test runners
             afterInitBlocker.countDown();
 	    // process data
             assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
 	} finally {
             threadPool.shutdownNow();
 	}
 	assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
     }
 }
